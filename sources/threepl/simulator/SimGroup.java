/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */
package threepl.simulator;

import java.util.ArrayList;



/**
 * A SimGroup contains a group of SimValues of possibly different types. Does not
 * correspond to a threepl type but is used to represent expression results
 * (variables with names like E7 etc.)
 *
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
@SuppressWarnings("all")
public class SimGroup extends SimValue {

    // Group of SimValues
    private SimValue[] group;
    
    /**
     * If this SimGroup has a source SimVariable, execute the behaviour of
     * that variable's source SDE. Otherwise execute behaviour of the group elements,
     * as they may have individual sources. The result will be to possibly alter all or part of
     * this SimGroup.
     * @param time Simulator time; used to avoid calling behaviour() redundantly 
     * @param force True if behaviour should happen regardless of time
     */
    public void behaviour (double time, boolean force) {
       if (srcVar!=null)
          srcVar.behaviour(time, force);
       else
          for (int i=0; i<group.length; i++)
	     group[i].behaviour(time, force);
    }

    /**
     * Cast another SimValue into this
     * @param src SimValue to cast to this
     * @param sext If true, do sign extension, else zero pad (if relevant)
     * @return this SimValue
     */
    public SimValue cast (SimValue src, boolean sext) {
    
       // Check can cast
       checkCast(src);

       int w = getWidth();
       int ws = src.getWidth();
       
       // Copy common bits
       put(src, Math.min(w, ws), 0, 0);
       
       // If this is wider than src, zero pad or sign extend according to sext
       if (w>ws) {
          boolean msb = sext ? src.getBit(ws-1) : false;	  
          for (int i=ws; i<w; i++)
	     put(msb, i);
       }	   
       return this;	     
    }
    
    /**
     * Clear group
     * @return this SimGroup
     */
    public SimValue clear() {
       for (int i=0; i<group.length; i++)
          group[i].clear();
       return this;	  
    }

    /** Clear changed flag */
    public void clearChanged () {
       for (int i=0; i<group.length; i++)
          group[i].clearChanged();
    }
    
    /**
     * Equality test. Groups must be of same size
     * and contain equal members.
     * @param src SimValue to test for equality with this
     * @return True if the SimValues are equal
     */
    public boolean eq(SimValue src) {
    
       // Check compatibility
       checkType(src);
              	     
       // Check for member equality
       SimGroup groupSrc = (SimGroup)src;
       for (int i=0; i<group.length; i++)
          if (!group[i].eq(groupSrc.group[i]))
	     return false;
	     
       return true;	     
    }

    /**
     * Equality test with Object, which must be an ArrayList.
     * @param o Object to test for equality with this
     * @return True if the values are equal
     */
    public boolean eq (Object o) {

       // Object must be an ArrayList
       if (!(o instanceof ArrayList))
          throw new SimException(
	     "SimGroup.eq(Object): object must be an ArrayList");
       
       ArrayList list = (ArrayList)o;
                  
       // If list is not equal in length to the group, return false
       if (list.size()!=group.length)
          return false;	             

       // Test equality of each element..
       for (int i=0; i<list.size(); i++) {

          // If any list element null, it is "equivalent" to
	  // the corresponding group member
          if ((o=list.get(i))==null)
             continue;
	  
	  // Test element equality
	  if (!group[i].eq(o))
	     return false;
       } 
       return true;   
    }    

    /**
     * Return logical value of bth bit
     * @param b Bit index starting from lsb in 1st member
     * @return The boolean value of the bth bit
     */
    public boolean getBit(int b) {

       // Check bit index is within variable
       checkBitBounds(b);

       // Find bit within group and return it
       int high, low = 0;
       for (int i=0; i<group.length; i++,low=high) {
          high = low + group[i].getWidth();
          if (b>=low && b<high)
	     return group[i].getBit(b-low);
       }
       
       // NEVER GET HERE
       return false;
    }

    /**
     * Return a SimValue which is a subset of this SimValue as constrained by
     * the specs list. The specs list is a list of array subscripts and/or struct field names,
     * in any order. If the spec resolves to an array, the SimValue returned is a SimArray with
     * dimensions according to the specs. If the spec resolves to a struct, a SimStruct is
     * returned. Otherwise a primitive SimValue (SimInt etc.) is returned. The returned SimValue
     * may or may not be created (in some cases, this SimValue may be returned). If specs list is
     * null or empty, this SimValue is returned.
     * @param specs List of array subscripts, ranges or struct names. The first element must be
     *        a member subscript (null, Integer or SimRange) valid for this SimGroup.
     * @return A SimValue which is derived from this, constrained by the specs list.
     */                
    public SimValue getPart (ArrayList specs) {
    
       // If spec list is null or empty, return this group
       if (specs==null || specs.size()==0)
          return this;

       SimValue v;
       
       // Remove first spec in list
       Object spec = specs.remove(0);       
       
       // If null, represents an empty subscript, behave as if full range..
       if (spec==null) {
          specs.add(0, new SimRange(0, group.length-1));
          v = getPart(specs);
       }
       	  
       // ..else if a SimRange, refers to a member range..
       else if (spec instanceof SimRange) {
          SimRange range = (SimRange)spec;
	  int low = range.getLow();
	  int high = range.getHigh();
	  if (low<0 || high>=group.length)
	     throw new SimException("member subscript range out of bounds");
          int width = high-low+1;	     
          v = new SimGroup(width);
	  for (int i=0; i<width; i++)
	     ((SimGroup)v).group[i] = group[low+i].getPart((ArrayList)specs.clone());
       }
       
       // ..else if an Integer, refers to single element..
       else if (spec instanceof Integer) {
          int subscript = ((Integer)spec).intValue();
	  if (subscript<0 || subscript>=group.length)
	     throw new SimException("member subscript out of bounds");
          v = group[subscript].getPart(specs);
       }
       
       // ..else invalid spec for a group member subscript, error
       else
          throw new SimException("cannot access a group by a field name");	     	  

       // If the resulting SimValue has only one underlying non-compound element,
       // return the element rather than the compound value
       if (v.isSingle())
          return v.getSingle();
	  
       // ..otherwise return the SimValue as is
       return v;	  
    }

    /**
     * Return a SimValue which is a subset of this SimValue as constrained by the
     * specified low and high bit indices, which index the entire variable.
     * Bit indices must fall on boundaries of SimValues which are non-compound.
     * @param range A SimRange containing the low and high bit indicies
     * @return A SimValue which is derived from this, constrained by the bit indices.
     */
    public SimValue getPart (SimRange range) {
     
       int bl = range.getLow();
       int bh = range.getHigh();
       int high, low = 0;
       
       for (int i=0; i<group.length; i++, low=high) {
          high = low + group[i].getWidth();

          // If low bit falls within this member..
          if (bl>=low && bl<high) {
	  
	     // Check high bit also falls within this member
	     if (bh<low || bh>=high)
	        throw new SimException(
		   "SimGroup.getPart(SimRange): bitrange does not fall within a member boundary");
	  
             // If range is smaller than the member, return subset of the field..
	     // (If range is smaller than member value width the member value is compound type)  
	     if (bl>low || bh<high-1)
                return group[i].getPart(new SimRange(bl-low, bh-low));
	     
	     // ..else range equates to entire member value, return member value..
             else
	        return group[i];
          }
       }
       
       // range falls outside the group		
       throw new SimException(
          "SimGroup.getPart(SimRange): range does not fall within group");		
    }              

    /**
     * Return the single underlying primitive SimValue, or error
     * @return The single underlying SimValue
     */
    public SimValue getSingle () {
       
       // Check isSingle
       if (!isSingle())
          throw new SimException("SimGroup.getSingle(): is a compound value");
	  
       return group[0].getSingle();	  
    }
         
    /**
     * Return width of entire group in bits
     * @return Width of entire group in bits
     */
    public int getWidth() {
       int w = 0;
       for (int i=0; i<group.length; i++)
          w += group[i].getWidth();   
       return w;
    }       
             
    /**
     * Return true if the group is clear, ie all members are zero or false.
     * @return True if all members zero/false
     */
    public boolean isClear () {
       for (int i=0; i<group.length; i++)
          if (!group[i].isClear())
	     return false;
       return true;
    }
         

    /**
     * Return true is the src SimValue is connectable with this for assignment
     * Must have same type and width.
     * @param src The SimValue to test for compatibility
     */
    public boolean isConnectable (SimValue src) { 
       if (!(src.type==GROUP && ((SimGroup)src).group.length==group.length))
          return false;
       for (int i=0; i<group.length; i++)
          if (!group[i].isConnectable(((SimGroup)src).group[i]))
	     return false;
       return true;	     
    }

    /**
     * Return true if this group contains a single underlying primitive element
     * @return True if this group contains a single underlying primitive element
     */
    public boolean isSingle () {
       return group.length==1 && group[0].isSingle();
    }
    
    /**
     * Return true if this is a primitive type (ie non-compound).
     * Always false for SimGroups.
     * @return False     
     */
    public boolean isPrimitive () {
       return false;
    }
    
    /**
     * Return formatted group members
     * @param fmt A printf style format string, or null
     * @return The formatted group members
     */
    public String print (String fmt) {

       StringBuffer b = new StringBuffer();       	  

       b.append("{");
       for (int i=0; i<group.length; i++) {
          if (i!=0)
	     b.append(",");
	  b.append(group[i].print(fmt));
       }	  
       b.append("}");
       return b.toString();
    }           

    /**
     * Return SimGroup type as a string
     * @return Formatted string showing SimGroup type, dimensions and element type
     */
    public String printType () {
    
       StringBuffer b = new StringBuffer();
       for (int i=0; i<group.length; i++) {   
          if (i!=0)
	     b.append(",");
          b.append(group[i].printType());
       }	  
       return b.toString();
    }
        
    /**
     * Copy another SimValue into this. Src must be a SimGroup of same dimensionality as this.
     * @param src The SimValue to copy from
     * @return this SimGroup
     */
    public SimValue put(SimValue src) {

       // Check copy compatibility
       checkCast(src);
              	     
       // Copy
       // If same type, assign each field..                 
       if (isSameType(src)) {
          SimGroup groupSrc = (SimGroup)src;
          for (int i=0; i<group.length; i++)
             group[i].put(groupSrc.group[i]);
       }
       
       // ..else do a cast
       else
          cast(src, false);
	  
       return this;	  
    }

    /**
     * Copy another SimValue into this, with specified bit ranges
     * @param src The SimValue to copy from
     * @param nbits The number of bits to copy
     * @param startBit Starting bit index in this SimGroup
     * @param startBitSrc Starting bit index in the source SimValue
     * @return this SimGroup
     */
    public SimValue put(SimValue src, int nbits, int startBit, int startBitSrc) {

       // Check this or src variable bounds not exceeded
       checkBitBounds(src, nbits, startBit, startBitSrc);

       // Copy bits in as large chunks as possible
       int chunk, width, high, low = 0;
       for (int i=0; i<group.length && nbits>0; i++, low=high) {
       
          // Get member width and low and high bit indices within the complete group
          width = group[i].getWidth();
          high = low + width;
	  
	  // If startBit falls within this field, find number of bits to copy and do it
          if (startBit>=low && startBit<high) {
             chunk = Math.min(nbits, high-startBit);
             group[i].put(src, chunk, startBit-low, startBitSrc);
	     nbits -= chunk;
	     startBit += chunk;
	     startBitSrc += chunk;
          }
       }             
       return this;       
    }				 

    /**
     * Put a boolean in specified bit position in this SimGroup. Bit index
     * refers to the group as a whole, not a single member.
     * @param l The boolean value to write
     * @param b Bit index in this to write to (index of lsb of zeroth element = 0)
     * @return this SimGroup
     */
    public SimValue put(boolean l, int b) {
    
       // Check bit index is within variable
       checkBitBounds(b);

       // Find bit within struct
       int high, low = 0;
       for (int i=0; i<group.length; i++, low=high) {
       
          // Get field width and low and high bit indices within the complete group
          high = low + group[i].getWidth();
	  
	  // If bit index falls within this field, set it to l
          if (b>=low && b<high)
	     group[i].put(l, b-low);
       }                   
       return this;
    }
    
    /**
     * Put the value in the passed Object, which must be an ArrayList,
     * into this SimValue. No action if list is empty, or where a list
     * element is null. Otherwise the list elements are copied.
     * @param o ArrayList containing values to copy
     */
    public void putObj (Object o) {

       // Object must be an ArrayList
       if (!(o instanceof ArrayList))
          throw new SimException("incompatible with "+printType());
       
       ArrayList list = (ArrayList)o;
                  
       // No action if list is empty
       if (list.size()==0)
          return;

       // Check list is not longer than group
       if (list.size()>group.length)
          throw new SimException(
	     "cannot assign an initialiser of length "+list.size()+
	     " to a "+printType());

       // Copy each list element..
       for (int i=0; i<list.size(); i++) {

          // No action if element is null (skip group member)
          if ((o=list.get(i))==null)
             continue;
	  
	  // Copy element
	  group[i].putObj(o);
       }
    }

    /**
     * Within the bit range within this SimValue, change the references to
     * corresponding SimValues within to references to the corresponding
     * SimValues represented by the src SimValue and associated bit range rangeSrc.
     * The width of the ranges must be the same and the subset SimValue of this and
     * src (corresponding to the bit ranges) must be compatible; ie same structure
     * with same types. Not used by non-compound SimValue types.
     * @param src The source SimValue
     * @param rangeSrc The bit range within the source SimValue
     * @param range This bit range within this SimGroup
     */    
    public void referTo (SimValue src, SimRange rangeSrc, SimRange range) {

       SimValue partSrc = src.getPart(rangeSrc);

       // Check that the SimValues corresponding to the ranges are connectable.
       // This implicitly checks that the ranges are the same length.
       if (!getPart(range).isConnectable(partSrc))
          throw new SimException(
	     "SimGroup.referTo(SimValue,SimRange,SimRange): values unconnectable");
         
       int bl = range.getLow();
       int bh = range.getHigh();
       int high, low = 0;
       
       for (int i=0; i<group.length; i++, low=high) {
          high = low + group[i].getWidth();

          // If low bit falls within this field..
          if (bl>=low && bl<high) {
	  
	     // Check high bit also falls within this field
	     if (bh<low || bh>=high)
	        throw new SimException(
		   "SimGroup.referTo(SimValue,SimRange,SimRange): "+
		   "bitrange does not fall within a member boundary");

             // If range is smaller than the member, referTo within the member..
	     // (If range is smaller than member value width the member value is compound type)  
	     if (bl>low || bh<high-1) {
	        group[i].referTo(src, rangeSrc, new SimRange(bl-low, bh-low));
		return;
	     }
	     
	     // ..else range equates to entire member value, refer to src member value..
             else {
	        group[i] = partSrc;
                return;
             }		
          }
       }
       
       // range falls outside the group
       throw new SimException(
          "SimGroup.referTo(SimValue,SimRange,SimRange): range does not fall within group");		
    }
    
    /**
     * Set the source SimVariable for this value, where the SimVariable
     * is an SDE output.
     * @param srcVar The source SimVariable
     */
    public void setSrcVar (SimVariable srcVar) {
       this.srcVar = srcVar;
       for (int i=0; i<group.length; i++)
          group[i].setSrcVar(srcVar);
    }
           
    // Constructor
    // @param group Array of SimValues which comprise the group
    //
    protected SimGroup (SimValue[] group) {
       type = GROUP;
       this.group = group;
    }
    
    // Constructor with group size.
    // Creates a SimGroup of desired size whose members are null
    // @param size Size of group in members
    //
    private SimGroup (int size) {
       type = GROUP;
       group = new SimValue[size];
    }

    // Clone
    // @return A clone of this SimGroup
    //
    protected Object clone() throws CloneNotSupportedException {
        SimGroup v = (SimGroup) super.clone();
	v.group = new SimValue[group.length];
	for (int i=0; i<group.length; i++)
	   v.group[i] = (SimValue)group[i].duplicate();
        return (Object) v;
    }
              
    // Return true if src SimValue can be cast to this type
    // @param src The SimValue to test
    // @return True if src can be cast to this type
    //
    protected boolean isCastable (SimValue src) {
    
       // Cast allowed if:
       // src is bits:n, or
       // src is []bits:1, or
       // src is same total size

       return super.isCastable(src) || src.getWidth()==getWidth();
    }
        
    // Return true if src SimValue is of same type
    // @param src The SimValue to test
    // @return True if src is of same type
    //
    protected boolean isSameType (SimValue src) {
    
       // src must be a SimGroup of same size, with all members
       // same type as corresponding members in this	  
       if (!(src instanceof SimGroup))
          return false;
	  
       SimGroup groupSrc = (SimGroup) src;
       if (groupSrc.group.length!=group.length)
          return false;
	  
       for (int i=0; i<group.length; i++)
          if (!group[i].isSameType(groupSrc.group[i]))
	     return false;
	     
       return true;	     	  
    }    
}
