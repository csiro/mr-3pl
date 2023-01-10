/**
 * LinkedMap.java
 *
 * @author Andrew Tulloh April 2005
 * @version $Revision: 6003 $
 * @since April 2005
 */

package threepl.simulator;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;

//import motion.exceptions.*;

/**
 * This class implements an associative array whose address elements are a linked set.
 * Address elements may appear only once in the set, and may not be null. Array data
 * elements may appear more than once and may be null.
 */
@SuppressWarnings("all")
public class LinkedMap<K,V> extends AbstractMap<K,V>
                            implements SortedMap<K,V>, Cloneable, Serializable {

   // These fields only used by backing map..
   private HashMap<K,Entry> dMap; // HashMap underlying the backing map
   private Entry head;            // Backing map head, or null if map empty
   private Entry tail;            // Backing map tail, or null if map empty
   private int size;              // Backing map size
   private int maxSize;           // Backing map max size. Only used if > 0
   private Entry[] entryArr;      // Array of Entries in the backing map. Used for fast lookup by index when max size > 0.

   // These fields used by sub maps also..
   protected Entry minEntry;          // Minimum entry in map, exclusive, or null
   protected Entry maxEntry;          // Maximum entry in map, exclusive, or null
   protected int modCount;            // Used to detect illegal structural modification
   private LinkedMap<K,V> parent;     // This submap's parent map, or null if this is the backing map
   private LinkedMap<K,V> backingMap; // The backing map, always non-null
   protected EntrySet<K,V> entries;   // Entry set cache
      
   /**
    * Default constructor
    * (convention is that all Maps implement this constructor)
    */
   public LinkedMap () {
      dMap = new HashMap<K,Entry>();
      backingMap = this;
   }

   /**
    * Constructor with map
    * (convention is that all Maps implement this constructor)
    * @param m The map whose mappings are to be placed in this map
    */
   public LinkedMap (Map<? extends K,? extends V> m) {
      this();
      putAll(m);
   }

   /**
    * Constructor with initial capacity and load factor
    * @param initialCapacity is the initial capacity
    * @param loadFactor is the load factor
    */
   public LinkedMap (int initialCapacity, float loadFactor) {
      dMap = new HashMap<K,Entry>(initialCapacity, loadFactor);
      backingMap = this;
   }
          
   /**
    * Constructor with max size specified. If zero, there is no size limit.
    * If this constructor is used, extra overhead is introduced when
    * adding or removing map entries, and this overhead increases with the
    * map size. It should only be used when the maximum map size is small,
    * or if the map size remains static once created.
    */
   public LinkedMap (int maxSize) {
      this();
      if (maxSize<0)
         throw new IllegalArgumentException("maxSize must be >= 0");	 
      else if (maxSize>0) {
         // would like to just say entryArr = new Entry[maxSize];
         entryArr = (Entry[])java.lang.reflect.Array.newInstance(Entry[].class.getComponentType(), maxSize);
      }
      this.maxSize = maxSize;	 
   }

   // Constructor to create a sub map. The key in the parent map which will be first key in the
   // submap (inclusive) is passed as firstKey, or null if the first key (inclusive) in the submap
   // should be the same as that in the parent. The key in the parent map which will be last key in
   // the submap (exclusive) is passed as lastKey, or null if the last key (exclusive) in the submap
   // should be the same as that in the parent. 
   // @param parent The map of which is the direct parent of the new submap
   // @param fromKey The first key (inclusive) in the new submap, or null
   // @param toKey The last key (exclusive) in the new submap, or null  
   //
   protected LinkedMap (LinkedMap<K,V> parent, K fromKey, K toKey) {

      // Check parent
      if (parent==null)
         throw new IllegalArgumentException("parent map must be non-null");
      this.parent = parent;
      backingMap = getBackingMap();
      modCount = parent.modCount;
      
      // Check keys and get sub map entry limits (both exclusive)
      if (fromKey==null)
         minEntry = parent.getHead();
      else if ((minEntry = parent.getEntry(fromKey)) == null)
         throw new IllegalArgumentException("fromKey not in parent map");
      if (minEntry!=null)
         minEntry = minEntry.prev;  // Make minEntry exclusive

      if (toKey==null) {
         if ((maxEntry = parent.getTail()) != null)
	    maxEntry = maxEntry.next;
      } else if ((maxEntry = parent.getEntry(toKey)) == null)
         throw new IllegalArgumentException("toKey not in parent map");
	 
      if (minEntry!=null && maxEntry!=null && !minEntry.isLessThan(maxEntry))
        throw new IllegalArgumentException("fromKey > toKey");	   	    	    
   }

   /**
    * Add entry to map. If the mapping is not already in the map, the new entry is appended.
    * Throws an exception if key==null, or if key is already in this map with a different value,
    * or if the key is elsewhere in the backing map.
    * Throws an exception if this map was created with maxSize!=0 and
    * size()==maxSize before the add.
    * @param key Key for the entry to add to the map
    * @param value Value for the entry to add to the map
    * @return True if the map changed, false if map already contains the entry
    */
   public boolean add (K key, V value) {
   
      if (checkMapping (key, value) != null)
         return false;

      // Add
      checkCanAdd();
      addEntry(new Entry(key, value));
      return true;
   }

   /** 
    * Add entry to map after the specified existing key if exist is non-null.
    * If exist is null, do an add(key,value).
    * If exist is non-null, will throw an exception if exist key is not in the map
    * or if entry is already in the map at a location other than after exist.
    * Throws an exception if key==null, or if key is already in this map with a different value,
    * or if the key is elsewhere in the backing map.
    * Throws an exception if this map was created with maxSize!=0 and
    * size()==maxSize before the add.
    * @param key Key for the entry to add to the map
    * @param value Value for the entry to add to the map
    * @return True if the map changed, false if map already contains the entry, located
    * after the existing entry.
    */
   public boolean add (K exist, K key, V value) {
   
      // If exist is null, do an add
      if (exist==null)
         return add(key, value);
	
      // Check map contains exist key and get its entry
      Entry ee = checkContains(exist);

      // Check key doesn't already exist with a different value in this map, and
      // that the key doesn't exist elsewhere in the backing map.
      // If it exists in this map, check it is located after exist.
      // No action if mapping already exists in correct location.
      Entry e;
      if ((e = checkMapping (key, value)) != null) {
         if (e != ee.next)
     	   throw new IllegalArgumentException("entry is already in map but not after exist entry");
         return false;	   
      }

      // Add
      checkCanAdd();
      addEntry(ee, new Entry(key, value));
      return true;                     
   }

   /**
    * Clone this map with a deep copy, although keys and values are not cloned.
    * If this map is a sub map, the clone is a similar sub map of the parent map.
    * @return A deep copy of this. Keys and values are not cloned.
    */
   public Object clone () {

      LinkedMap<K,V> lm = null;      
      try {       
         lm = (LinkedMap<K,V>)super.clone();
	 if (parent==null) {
            lm.dMap = new HashMap<K,Entry>();
	    lm.head = null;
	    lm.tail = null;
	    lm.size = 0;
	    if (maxSize!=0) {
	       lm.maxSize = maxSize;
	       lm.entryArr = entryArr.clone();
	       Arrays.fill(lm.entryArr, null);
            }
	    lm.minEntry = null;
	    lm.maxEntry = null;
	    lm.modCount = 0;
	    lm.backingMap = lm;
	    lm.entries = null;
	    lm.putAll(this);
	 }
      } catch (CloneNotSupportedException e) {
         // Impossible to get here
      }	 
      return lm;
   }

   /**
    * Return the comparator associated with this sorted map, always null
    */
   public Comparator<? super K> comparator () {
      return null;
   } 
   
   /**
    * Return true if this map contains the key
    * @param    o is the key
    * @return True if the map contains the key
    */      
   public boolean containsKey (Object o) {   
      checkCompatible(o);      	 
      return getEntry(o) != null;
   }

   /**
    * Return the entry set of this map
    * @return The entry set of this map
    */   
   public EntrySet<K,V> entrySet () {

      // Create entry set cache if not there yet   
      if (entries==null)
         entries =  new Entries();
      return entries;	 
   }

   /**
    * Return the first key in the map.
    * Throws an exception if map empty.
    */
   public K firstKey () {
      Entry e;
      if ((e = getHead())==null)
         throw new NoSuchElementException();	 
      return e.key;
   }
   
   /**
    * Return the value for this key, or null if the key is not in the map
    * @param o The key object
    * @return The value for the key, or null if key not in map
    */
   public V get (Object o) {
      checkCompatible(o);
      Entry e;
      return (e = getEntry(o))==null ? null : e.value;
   }
      
   /**
    * Return a data entry by index. Preferably should only be used if the maxSize
    * form of the constructor was used, and on the backing map. Otherwise this involves
    * a search through the map, either from the head or the tail, whichever is closer.
    * Throws an exception if the index is out of bounds or the map is empty.
    * @param index Index in set of data to retrieve
    * @return The data
    */
   public V get (int index) {

      int sz = size();
      
      // Check size, index
      if (sz==0 || index<0 || index>=sz)
         throw new  IndexOutOfBoundsException("map empty or index out of bounds");

      // If this is the backing map and has a max size, the keys are indexed so just return the value..
      if (parent==null && maxSize>0)
         return entryArr[index].value;
	 
      // ..otherwise must search for the entry, forwards from head or in reverse
      // from tail, whichever is closer to index	 
      int i;
      Entry e;
      if (index<sz/2)
         for (i=0, e=getHead(); i!=index; e=e.next, i++);
      else
         for (i=sz-1, e=getTail(); i!=index; e=e.prev,i--);
      return e.value;       	 	 
   }

   /**
    * Return a view of the portion of this sorted map whose keys are strictly less than toKey.
    * Any operation on the submap will throw a ConcurrentModificationException
    * if the submap is structurally modified other than via itself or one of
    * its descendant submaps.
    */
   public LinkedMap<K,V> headMap (K toKey) {
      return subMap(null, toKey);
   }
      
   /**
    * Insert entry into map. If the mapping is not already in the map, the new entry is inserted
    * at the first mapping.
    * Throws an exception if key==null, or if key is already in this map with a different value,
    * or the key exists elswhere in the backing map.
    * Throws an exception if this map was created with maxSize!=0 and
    * size()==maxSize before the insert.
    * @param key Key for the entry to insert into the map
    * @param value Value for the entry to insert into the map
    * @return True if the map changed, false if map already contains the entry
    */   
   public boolean insert (K key, V value) {

      // Check key doesn't already exist in this map with a different value,
      // and the key doesn't exist elsewhere in the backing map.
      // No action if same mapping already exists.
      Entry e;      
      if ((e = checkMapping (key, value)) != null)
         return false;
      
      // Insert
      checkCanAdd();
      insertEntry(new Entry(key, value));
      return true;
   }
   
   /** 
    * Insert entry into map before the specified existing key if exist is non-null.
    * If exist is null, do an insert(key,value).
    * If exist is non-null, will throw an exception if exist key is not in the map
    * or if entry is already in the map at a location other than before exist.
    * Throws an exception if key==null, or if key is already in the map with a different value,
    * or the key exists elswhere in the backing map.
    * Throws an exception if this map was created with maxSize!=0 and
    * size()==maxSize before the insert.
    * @param key Key for the entry to insert into the map
    * @param value Value for the entry to insert into the map
    * @return True if the map changed, false if map already contains the entry, located
    * before the existing entry.
    */
   public boolean insert (K exist, K key, V value) {

      // If exist is null, do an insert
      if (exist==null)
         return insert(key, value);
	
      // Check map contains exist key and get its entry
      Entry ee = checkContains(exist);

      // Check key doesn't already exist with a different value,
      // and the key doesn't exist elsewhere in the backing map.
      // If mapping exists, check it is located before exist.
      // No action if mapping already exists in correct location.
      Entry e;
      if ((e = checkMapping (key, value)) != null) {
         if (e != ee.prev)
     	   throw new IllegalArgumentException("entry is already in map but not before exist entry");
         return false;	   
      }

      // Add
      checkCanAdd();
      insertEntry(ee, new Entry(key, value));
      return true;                     
   }
   
   /**
    * Return the last key in the map.
    * Throws an exception if map empty.
    */
   public K lastKey () {
      Entry e;
      if ((e = getTail())==null)
         throw new NoSuchElementException();	 
      return e.key;
   }

   /**
    * Return the next key in the map, or null if the exist key is the last.
    * If exist is null, return the first key in the map.
    * Throws an exception if map is empty.
    * Throws an exception if exist is non-null and not in the map.
    */
   public K nextKey (K exist) {
   
      // If exist is null, return the head..
      if (exist==null)
         return firstKey();

      Entry ee = checkContains(exist);
      return ee.next != maxEntry ? ee.next.key : null;       	    
   }
   
   /**
    * Return the previous key in the map, or null if the exist key is the first.
    * If exist is null, return the last key in the map.
    * Throws an exception if map is empty.
    * Throws an exception if exist is non-null and not in the map.
    */
   public K prevKey (K exist) {

      // If exist is null, return the tail..
      if (exist==null)
         return lastKey();

      Entry ee = checkContains(exist);
      return ee.prev != minEntry ? ee.prev.key : null;       	    
   }

   /**
    * Enter a mapping into the map. If the key already exists in the map,
    * replace the value, retaining its place in the map. Otherwise
    * append to the map. Returns the previous value if the key existed,
    * else null.
    * @param key The key
    * @param value The value
    * @return The previous value if the key existed, else null
    */
   public V put (K key, V value) {

      Entry ee;
      if ((ee = getEntry(key)) != null)
         return putEntry(ee, value);
      add(key, value);
      return null;	 
   }

   /**
    * Replace a mapping with a new one, retaining the same place in the map.
    * Returns the previous value. If the new key is equal to the exist key,
    * does a put(key, value).
    * Throws an exception if the key to replace is null or not in the map.
    * Throws an exception if the new key is null or already in the map and not
    * equal to exist.
    * @param exist The key of the existing mapping to replace
    * @param key The new key
    * @param value The new value
    * @return The old value
    */
   public V put (K exist, K key, V value) {

      // Check map contains exist and get its entry
      checkCompatible(exist);
      Entry ee = checkContains(exist);      	       

      // Check key is non-null
      if (key==null)
         throw new IllegalArgumentException("null keys not allowed");

      // If key equals exist, do a put(key,value)..
      if (key.equals(exist))
         return put(key, value);
	 
      // ..else check key not already anywhere in backing map
      else
         backingMap.checkNotContains(key);
	 	        
      // Replace exist with data in the existing entry and update map
      return putEntry(ee, key, value);
   }

   /**
    * Replace a mapping with a new one, retaining the same place in the map.
    * The existing mapping is located by the passed index into the map.
    * Returns the previous value.
    * Throws an exception if the index is out of bounds or the map is empty.
    * Throws an exception if the new key is null or already in the map at a
    * different index.
    * @param index The index of the existing mapping to replace
    * @param key The new key
    * @param value The new value
    * @return The old value
    */
   public V put (int index, K key, V value) {

      int sz = size();
      
      // Check size, index
      if (sz==0 || index<0 || index>=sz)
         throw new  IndexOutOfBoundsException("map empty or index out of bounds");

      // If this is the backing map and has a max size, the keys are indexed so no search required..
      if (parent==null && maxSize>0)
         return putEntry(entryArr[index], key, value);
	 
      // ..otherwise must search for the entry, forwards from head or in reverse
      // from tail, whichever is closer to index	 
      int i;
      Entry e;
      if (index<sz/2)
         for (i=0, e=getHead(); i!=index; e=e.next, i++);
      else
         for (i=sz-1, e=getTail(); i!=index; e=e.prev,i--);
      return putEntry(e, key, value);       	 	 
   }

   /**
    * Remove the mapping for an existing key. Returns the value for
    * the key or null if the key was not in the map (including if the
    * exist key is passed as null).
    * @return The value for the key if it existed, or null
    * @throws ClassCastException if the object to remove is not a key type
    */
   public V remove (Object o) {

      checkCompatible(o);
      Entry ee;
      if ((ee = getEntry(o)) == null)
         return null;	 
      removeEntry(ee);
      return ee.value;
   }

   /**
    * Remove the mapping for the first key. Returns the value for
    * the key or null if the map was empty.
    * @return The value for the first key if it existed, or null
    */
   public V removeFirst () {
      return remove(firstKey());
   }
   
   /**
    * Remove the mapping for the last key. Returns the value for
    * the key or null if the map was empty.
    * @return The value for the last key if it existed, or null
    */
   public V removeLast (Object o) {
      return remove(lastKey());
   }
   
   /**
    * Return the map size
    * This can be inefficient if the map is a submap and is large.
    */
   public int size () {

      // Size of backing map always known
      if (parent==null)
         return size;

      // Count size of a submap
      int count = 0;	 
      for (Entry e=getHead(); e!=maxEntry; e=e.next)
         count++;	
      return count;	  
   }
   
   /**
    * Returns a view of the portion of this sorted map whose keys range
    * from fromKey, inclusive, to toKey, exclusive.
    * Any operation on the submap will throw a ConcurrentModificationException
    * if the submap is structurally modified other than via itself or one of
    * its descendant submaps.
    */
   public LinkedMap<K,V> subMap (K fromKey, K toKey) {
      return new LinkedMap<K,V>(this, fromKey, toKey);
   }
      
   /**
    * Return a view of the portion of this sorted map whose keys are strictly less than
    * or equal to fromKey.
    * Any operation on the submap will throw a ConcurrentModificationException
    * if the submap is structurally modified other than via itself or one of
    * its descendant submaps.
    */
   public LinkedMap<K,V> tailMap (K fromKey) {
      return subMap(fromKey, null); 
   }
             
   /**
    * Return the upper key (exclusive) of this submap. Returns null if
    * this is not a submap.
    */
   public K toKey () {   
      return maxEntry==null ? null : maxEntry.getKey();
   }
       
   /**
    * The interface for the entry set
    */
   public interface EntrySet<K,V> extends Set<Map.Entry<K,V>> {

      public ListIterator<Map.Entry<K,V>> listIterator ();
      
      public ListIterator<Map.Entry<K,V>> listIterator (K exist);
   
   }
   
   // Append an entry to the map
   //
   protected void addEntry (Entry e) {

      if (parent!=null) {
         checkMod();
	 if (maxEntry!=null)
            parent.insertEntry(maxEntry, e);
         else
	    parent.addEntry(e);	    
	 modCount++;
	 return;
      }
      	 
      if (tail==null) {
         head = tail = e;
	 dMap.put(e.key, e);
	 setOrder(e);
         if (maxSize>0)
	    insertArr(0, e);
         modCount++;	    
	 size = 1;
      }	else
         addEntry(tail, e);         
   }

   // Add an entry to the map after existing entry ee
   //
   protected void addEntry (Entry ee, Entry e) {

      if (parent!=null) {
         checkMod();
         parent.addEntry(ee, e);
	 modCount++;
	 return;
      }

      dMap.put(e.key, e);	 
      e.prev = ee;
      e.next = ee.next;
      if (ee.next!=null)
         ee.next.prev = e;
      ee.next = e;
      if (tail.equals(ee))
         tail = e;	
      setOrder(e);  
      if (maxSize>0)
         insertArr(ee.index+1, e); 
      modCount++;	      
      size++;	    
   }

   // Check that a new element can be added/inserted to the map.
   // It is only illegal if backing map has a non-zero maxSize and size==maxSize already.
   //
   protected void checkCanAdd () {

      if (parent!=null)
         backingMap.checkCanAdd();   
      else if (maxSize>0 && size==maxSize)
         throw new IllegalStateException("cannot add to a full map with a non-zero max size");
   }

   // Check the passed existing key is in this map. If not, throw an exception
   // @return The Entry for this key
   //
   protected Entry checkContains (Object exist) {
      Entry ee;
      if ((ee = getEntry(exist)) == null)
         throw new IllegalStateException("exist key not in map");
      return ee;      	 
   }
   
   // Return true if the entry exists in this map with the same key-value mapping
   // defined by the arguments, or null if not.
   // Throws an exception if the key exists in this map with a different
   // value or if the key exists elsewhere in the backing map.
   // @param key The new key
   // @param value The new value
   // @return The map entry if it exists, or null
   //
   protected Entry checkMapping (K key, V value) {

      Entry e;
      
      if ((e = getEntry(key)) != null) {
         if (value==null ? e.value!=null : !value.equals(e.value))
	    throw new IllegalArgumentException("key is already in map with a different value");
      } else if (parent!=null)         
         backingMap.checkNotContains(key);
      return e;
   }

   // Check the passed new key is not in this map. If so, throw an exception
   //
   protected void checkNotContains (K key) {
      if (containsKey(key))
         throw new IllegalArgumentException("key already in map");
   
   }

   // Get the Entry for the existing key.
   // Returns null if the key is not in this map.
   //
   protected Entry getEntry (Object key) {

      Entry e = parent!=null ? backingMap.getEntry(key) : dMap.get(key);
      return e!=null && entryInRange(e) ? e : null;
   }
    
   // Insert an entry at the start of the map
   //
   protected void insertEntry (Entry e) {

      if (parent!=null) {
         checkMod();
	 if (minEntry!=null)
	    parent.addEntry(minEntry, e);
         else	    
            parent.insertEntry(e);
	 modCount++;
	 return;
      }
      	 
      if (head==null)
         addEntry(e);
      else
         insertEntry(head, e);   
   }
   
   // Insert an entry before the existing entry ee
   //
   protected void insertEntry (Entry ee, Entry e) {

      if (parent!=null) {
         checkMod();
         parent.insertEntry(ee, e);
	 modCount++;
	 return;
      }
      	 
      dMap.put(e.key, e);
      e.prev = ee.prev;
      e.next = ee;
      if (ee.prev!=null)
         ee.prev.next = e;
      ee.prev = e;
      if (head.equals(ee))
         head = e;
      setOrder(e);	 
      if (maxSize>0)
         insertArr(ee.index, e);	 
      modCount++;	 
      size++;	    
   }
   
   // Change the key and value in an entry
   //
   protected V putEntry (Entry e, K key, V value) {

      // No modCount check here as a put is not a structural mod
      
      if (parent!=null)
         return backingMap.putEntry(e, key, value);
      	 
      dMap.remove(e.key);
      dMap.put(key, e);
      e.key = key;
      return putEntry(e, value);      
   }
     
   // Change the value in an entry
   //      
   protected V putEntry (Entry e, V value) {
      return e.setValue(value);
   }
      
   // Remove an entry from the map
   //
   protected void removeEntry (Entry e) {
   
      if (parent!=null) {
         checkMod();
         parent.removeEntry(e);
	 modCount++;
	 return;
      }
      
      dMap.remove(e.key);
      if (e.prev!=null)
         e.prev.next = e.next;
      if (e.next != null)	 
         e.next.prev = e.prev;
      if (head.equals(e))
         head = e.next;
      if (tail.equals(e))
         tail = e.prev;
      if (maxSize>0)
         removeArr(e.index);	 
      modCount++;	 
      size--;    
   }    
      
   // Check the object is non-null and of type key for this map
   //
   private void checkCompatible (Object o) {
   
      if (o==null)
    	 throw new NullPointerException("This map cannot contain null keys");
   }	      

   // Check that the parent map hasn't been modified structurally other than through this
   //
   private void checkMod () {
      if (parent!=null && modCount!=parent.modCount)
         throw new ConcurrentModificationException();
   }
   	 	    
   // Return true if the passed entry is within this map.
   // It is assumed the entry is non-null and is a member of the backing map.
   //
   private boolean entryInRange (Entry e) {
      return e.isGreaterThan(minEntry) && e.isLessThan(maxEntry);
   }
   
   // Return the first entry in this map
   //
   protected Entry getHead () {
      return parent==null ? head : (minEntry==null ? parent.getHead() : minEntry.next);
   }

   // Return the last entry in this map 
   //
   protected Entry getTail () {
      return parent==null ? tail : (maxEntry==null ? parent.getTail() : maxEntry.prev);
   }
             
   // Return the backing map
   // 
   private LinkedMap<K,V> getBackingMap () {
      return parent==null ? this : parent.getBackingMap();
   }
         
   // Insert an entry into entryArr at the given index. All entries
   // above are shifted up one to make room, and their indices are
   // incremented accordingly.
   //
   private void insertArr (int index, Entry e) {
      
      // Move all existing array entries up one, starting from index
      // and increment their indices
      for (int i=size; i>index; i--) {
         entryArr[i] = entryArr[i-1];
         entryArr[i].index++;
      }	 
      	       
      // Enter the new entry
      e.index = index;
      entryArr[index] = e;
   }

   // Remove an entry from entryArr at the given index. All entries
   // above are shifted down one and their indices are decremented
   // accordingly. The highest element is set to null.
   //
   private void removeArr (int index) {
   
      // Move all existing array entries down one, starting from index+1
      // and decrement their indices
      for (int i=index; i<size-1; i++) {
         entryArr[i] = entryArr[i+1];
	 entryArr[i].index--;
      }
      entryArr[size-1] = null;	 
   }
      
   // Set the order value for an Entry just linked into the map
   //
   private void setOrder (Entry e) {
      
      if (e.prev==null)
         if (e.next==null)
	    e.order = 0;
         else
	    e.order = e.next.order-1;
      else if (e.next==null)
         e.order = e.prev.order+1;
      else
         e.order = (e.next.order+e.prev.order)/2;	 	    	    
   }
   
   // Class to represent map entries
   //
   protected class Entry implements Map.Entry<K,V>, Comparable<Entry> {

      // Key   
      protected K key;
      
      // Value
      protected V value;
      
      // Ordering number used in comparison
      protected double order;
      
      // Next entry pointer
      protected Entry next;
      
      // Previous entry pointer
      protected Entry prev;  
      
      // Index in entryArr
      protected int index;    

      // Constructor
      // @param key Key
      // @param value Value
      //
      private Entry (K key, V value) {
      
         // Null keys not allowed
	 if (key==null)
	    throw new IllegalArgumentException("null keys not allowed");
	    
         this.key = key;
	 this.value = value;
      }

      /**
       * Compare to another Entry
       */
      public int compareTo (Entry e) {
      
         if (e==null)
	    throw new ClassCastException("cannot compare null Entries");
	    
         return order>e.order ? 1 : (order<e.order ? -1 : 0);
      }
      
      /**
       * Test whether this entry is equal to another
       * @param o The object to be tested for equality with this
       * @return True if the passed object is equal to this
       */
      public boolean equals (Object o) {

         if (o==null || !(o instanceof Map.Entry<?,?>))
	    return false;
	    
         Entry e = (Entry)o;
	 return (key==null   ? e.key==null   : key.equals(e.key)) &&
	        (value==null ? e.value==null : value.equals(e.value));
      }

      /**
       * Return the key
       * @return The key for this entry
       */            	       
      public K getKey () {
         return key;
      }
      
      /**
       * Return the value
       * @return The value for this entry
       */            	       
      public V getValue () {
         return value;
      }

      /**
       * Return the hashcode
       * @return The hashcode
       */      	 	 
      public int hashcode () {      
         return (key==null   ? 0 : key.hashCode()) ^
	        (value==null ? 0 : value.hashCode());
      }
      
      /**
       * Set this entry's value to value
       * @param value The value to set
       */
      public V setValue (V value) {
         V old = this.value;
	 this.value = value;
	 return old;   
      }

      // Return the order
      //
      protected double getOrder () {
         return order;
      }
      	        
      // Return true if this entry occurs later in the map than
      // the passed entry. If the passed entry is null, return true
      // as this is equivalent to the start of the map.
      //
      protected boolean isGreaterThan (Entry e) {
         return e==null ? true : compareTo(e)>0;
      }

      // Return true if this entry occurs earlier in the map than
      // the passed entry. If the passed entry is null, return true
      // as this is equivalent to the end of the map.
      //
      protected boolean isLessThan (Entry e) {
         return e==null ? true : compareTo(e)<0;
      }
   }            

   // Class to represent the set of map entries
   //
   protected class Entries extends AbstractSet<Map.Entry<K,V>> implements EntrySet<K,V> {

      public void clear () {
      
         // Can't use iterator or get ConcurrentModificationException
         for (Entry next,e=getHead(); e!=maxEntry; e=e.next) {
            next = e.next;
	    removeEntry(e);
         }	    
      }
      
      public boolean contains (Object o) {
      
         checkCompatible(o);	    
         return containsKey(((Map.Entry<K,V>)o).getKey());
      }
      
      public Iterator<Map.Entry<K,V>> iterator () {
         return listIterator();
      }
      
      public ListIterator<Map.Entry<K,V>> listIterator () {
         return listIterator(null);
      }
      
      public ListIterator<Map.Entry<K,V>> listIterator (K exist) {
         return new EntryIterator(exist==null ? null : checkContains((Object)exist));
      }

      public boolean remove (Object o) {
	    
         if (!contains(o))
	    return false;
         removeEntry((Entry)o);
	 return true;	    
      }
      
      public int size () {
         return LinkedMap.this.size();
      }
      
      private void checkCompatible (Object o) {
      
         if (o==null)
            throw new NullPointerException("This set cannot contain null elements");
         if (!(o instanceof Map.Entry<?,?>))
            throw new ClassCastException("This set contains only Map.Entry<?,?>");
      }	  

      // Class to represent the entry set list iterator
      //
      protected class EntryIterator implements ListIterator<Map.Entry<K,V>> {

	 protected Entry lastReturned, prev, next;
	 protected int knownMod;
	 //private int lastIndex, nextIndex, prevIndex;

	 // Constructor with start entry, assumed to be a member of this map if non-null.
	 // @param startEntry The entry that would be reeturned by an initial call to next().
	 // If null, means the first accessible entry.
	 // @throws IllegalArgumentException if startEntry is non-null and does not lie between
	 // minEntry and maxEntry.
	 //      
	 protected EntryIterator (Entry startEntry) {

            // Check startEntry
	    if (startEntry!=null && !entryInRange(startEntry))
               throw new IllegalArgumentException("startEntry out of range");	     

	    next = startEntry==null ? getHead() : startEntry;
	    prev = next==null ? null : next.prev;	    
	    knownMod = modCount;

	    //// Get initial values for nextIndex and prevIndex
	    //nextIndex = 0;
	    //for (Entry e=getHead(); e!=next; e=e.next)	 
	    //   nextIndex++;
            //prevIndex = nextIndex;	    
	 }

         /**
          * Add
	  * Always throw an UnsupportedOperationException.
	  */
	 public void add (Map.Entry<K,V> o) {
	    throw new UnsupportedOperationException("add() not supported");
	 }
	 
	 /**
	  * Return true if there is another entry available,
	  * traversing map in forward direction.
	  */
	 public boolean hasNext () {
            checkMod();
            return next!=maxEntry;
	 }

	 /**
	  * Return true if there is another entry available,
	  * traversing map in reverse direction.
	  */
	 public boolean hasPrevious () {
            checkMod();
            return prev!=minEntry;
	 }

	 /**
	  * Return next entry,
	  * traversing set in forward direction.
	  */
	 public Map.Entry<K,V> next () {

            checkMod();
            if (next==maxEntry)
	       throw new NoSuchElementException();

            lastReturned = prev = next;
	    next = lastReturned.next;

	    //lastIndex = prevIndex = nextIndex;
	    //nextIndex++;

	    return (Map.Entry<K,V>)lastReturned;
	 }

	 /**
	  * Return index of entry that would be returned by a subsequent call to next().
	  * (Returns map size if the entry iterator is at the end of the map.)
	  * @throws NoSuchMethodException (always)
	  */
	 public int nextIndex () {      
            throw new UnsupportedOperationException("nextIndex() not supported");
            //return nextIndex;
	 }

	 /**
	  * Return next entry,
	  * traversing map in reverse direction.
	  */
	 public Map.Entry<K,V> previous () {

            checkMod();
            if (prev==minEntry)
	       throw new NoSuchElementException();

            lastReturned = next = prev;
	    prev = lastReturned.prev;

            //lastIndex = nextIndex = prevIndex;
	    //prevIndex--;

	    return (Map.Entry<K,V>)lastReturned;
	 }

	 /**
	  * Return index of entry that would be returned by a subsequent call to previous().
	  * (Returns -1 if the entry iterator is at the beginning of the map.)
	  * @throws NoSuchMethodException (always)
	  */
	 public int previousIndex () {
            throw new UnsupportedOperationException("previousIndex() not supported");
            //return prevIndex;
	 }

	 /**
	  * Remove from the map the entry that was last returned by next() or previous()
	  */
	 public void remove () {

            checkMod();
            if (lastReturned==null)
	       throw new IllegalStateException();

            next = lastReturned.next;
	    prev = lastReturned.prev;
            removeEntry(lastReturned);
	    lastReturned=null;
            knownMod = modCount;

            //if (nextIndex>lastIndex) {
	    //   nextIndex--;
	    //   prevIndex--;
            //}	    
	 } 

         /**
          * Set
	  * Always throw an UnsupportedOperationException.
	  */
	 public void set (Map.Entry<K,V> o) {
	    throw new UnsupportedOperationException("set() not supported");
	 }
	 
	 protected void checkMod () {      
            if (knownMod!=modCount)
	       throw new ConcurrentModificationException();
         }	               
      }
   }
            
}
