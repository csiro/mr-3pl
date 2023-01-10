/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */
package threepl.simulator;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A SimStruct contains a name, which is its 'type', and a number of named fields.
 * Each fields consists of a String name and a SimValue value.
 *
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
@SuppressWarnings("all")
public class SimStruct extends SimValue {
    
    // Fields accessed by name
    private HashMap fieldMap;
    
    // Field name/value pairs accessed by index
    protected ArrayList fieldList;
       
    /**
     * If this SimStruct has a source SimVariable, execute the behaviour of
     * that variable's source SDE. Otherwise execute behaviour of the struct
     * field values, as they may have individual sources. The result will be to
     * possibly alter all or part of this SimStruct.
     * @param time Simulator time; used to avoid calling behaviour() redundantly 
     * @param force True if behaviour should happen regardless of time
     */
    public void behaviour (double time, boolean force) {
       if (srcVar!=null)
          srcVar.behaviour(time, force);
       else
          for (int i=0; i<fieldList.size(); i++)
             getFieldValue(i).behaviour(time, force);
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
     * Clear struct
     * @return this SimStruct
     */
    public SimValue clear() {
       for (int i=0; i<fieldList.size(); i++)
          getFieldValue(i).clear(); 
       return this;	  
    }

    /** Clear changed flag */
    public void clearChanged () {
       for (int i=0; i<fieldList.size(); i++)
          getFieldValue(i).clearChanged();
    }
    
    /**
     * Equality test. Structs must be of same type and contain equal fields.
     * @param src SimValue to test for equality with this
     * @return True if the SimValues are equal
     */
    public boolean eq(SimValue src) {

       // Check src is a struct of same type
       checkType(src);
                 
       // Check equality of each field
       SimStruct structSrc = (SimStruct)src;
       for (int i=0; i<fieldList.size(); i++)
          if (!getFieldValue(i).eq(structSrc.getFieldValue(i)))
	     return false;
	     
       return true;	     
    }
           
    /**
     * Equality test with Object, which must be an ArrayList
     * @param o Object to test for equality with this
     * @return True if the values are equal
     */
    public boolean eq (Object o) {

       // Object must be an ArrayList
       if (!(o instanceof ArrayList))
          throw new SimException(
	     "SimStruct.eq(Object): object must be an ArrayList");
       
       ArrayList list = (ArrayList)o;
                  
       // If list is not equal in length to no. of struct fields, return false
       if (list.size()!=fieldList.size())
          return false;	             

       // Test equality of each element..
       for (int i=0; i<list.size(); i++) {

          // If any list element null, it is "equivalent" to
	  // the corresponding struct field
          if ((o=list.get(i))==null)
             continue;
	  
	  // Test element equality
	  if (!getFieldValue(i).eq(o))
	     return false;
       } 
       return true;   
    }    

    /**
     * Return logical value of bth bit in struct
     * @param b Bit index starting from lsb in first field
     * @return The boolean value of the bth bit
     */
    public boolean getBit (int b) {
    
       // Check bit index is within variable
       checkBitBounds(b);

       // Find bit within struct
       int high, low = 0;
       SimValue value;
       for (int i=0; i<fieldList.size(); i++, low=high) {
       
          // Get field width and low and high bit indices within the complete struct
	  value = getFieldValue(i);
          high = low + value.getWidth();
	  
	  // If bit index falls within this field, return its value
          if (b>=low && b<high)
             return value.getBit(b-low);
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
     *        a field name in this SimStruct.
     * @return A SimValue which is derived from this, constrained by the specs list.
     */                
    public SimValue getPart (ArrayList specs) {
    
       // If spec list is null or empty, return this struct
       if (specs==null || specs.size()==0)
          return this;
	 
       // Remove first spec in list
       Object spec = specs.remove(0);
       
       // If it's not a String, it's not a struct field name, so error
       if (!(spec instanceof String))
          throw new SimException("struct cannot be accessed as an array");

       // Check struct contains this field	
       Field field;  
       if ((field = (Field)fieldMap.get((String)spec)) == null)
          throw new SimException("struct does not contain field "+(String)spec);	  
       	  
       // Apply remaining specs to field value  
       return field.getValue().getPart(specs);
    }
            
    /**
     * Return a SimValue which is a subset of this SimValue as constrained by the
     * specified low and high bit indices, which index the entire variable.
     * Bit indices must fall on appropriate boundaries: the entire width for
     * a struct type.
     * @param range A SimRange containing the low and high bit indicies
     * @return A SimValue which is derived from this, constrained by the bit indices.
     */
    public SimValue getPart (SimRange range) {
     
       int bl = range.getLow();
       int bh = range.getHigh();
       int high, low = 0;
       SimValue value;

       // If the range equates to the entire struct width, return this..
       if (range.getWidth()==getWidth())
          return this;

       // ..otherwise range must match a single field exactly	         
       for (int i=0; i<fieldList.size(); i++, low=high) {
	  value = getFieldValue(i);
          high = low + value.getWidth();

          // If low bit falls within this field..
          if (bl>=low && bl<high) {
	  
	     // Check high bit also falls within this field
	     if (bh<low || bh>=high)
	        throw new SimException(
		   "SimStruct.getPart(SimRange): bitrange does not fall within a field boundary");
	  
             // If range is smaller than the field, return subset of the field..
	     // (If range is smaller than field value width the field value is compound type)  
	     if (bl>low || bh<high-1)
                return value.getPart(new SimRange(bl-low, bh-low));
	     
	     // ..else range equates to entire field value, return field value..
             else
	        return value;
          }
       }
       
       // range falls outside the struct		
       throw new SimException(
          "SimStruct.getPart(SimRange): range does not fall within struct");		
    }              

    /**
     * Return width of entire struct in bits
     * @return Width of entire struct in bits
     */
    public int getWidth() {
       int w = 0;
       for (int i=0; i<fieldList.size(); i++)
          w += getFieldValue(i).getWidth();   
       return w;
    }       
             
    /**
     * Return true if the struct is clear, ie all fields are zero or false.
     * @return True if all fields zero/false
     */
    public boolean isClear () {
       for (int i=0; i<fieldList.size(); i++)
          if (!getFieldValue(i).isClear())
	     return false;
       return true;
    }

    /**
     * Return true if the src SimValue can be connected to this
     * Must be a STRUCT type with same field types.
     * @param src The SimValue to test for connectability
     */
    public boolean isConnectable (SimValue src) {    
       return isSameType(src);
    }
        	 
    /**
     * Return true if this is a primitive type (ie non-compound).
     * Always false for SimStructs.
     * @return False     
     */
    public boolean isPrimitive () {
       return false;
    }
         
    /**
     * Return formatted array elements
     * @param fmt A printf style format string, or null
     * @return The formatted struct fields
     */
    public String print (String fmt) {

       StringBuffer b = new StringBuffer();       	  

       b.append("{");
       for (int i=0; i<fieldList.size(); i++) {
          if (i!=0)
	     b.append(",");
          // b.append(getFieldName(i)+": ");
	  b.append(getFieldValue(i).print(fmt));
       }	  
       b.append("}");
       return b.toString();
    }           

    /**
     * Return SimStruct type (name) as a string
     * @return struct name, String
     */
    public String printType () {
    
       StringBuffer b = new StringBuffer(SimType.printType(type));
       
       b.append("{");
       for (int i=0; i<fieldList.size(); i++) {
          if (i!=0)
	     b.append(",");
	  b.append(getFieldValue(i).printType());
       }	  
       b.append("}");
       return b.toString();
    }
        
    /**
     * Copy another SimValue into this. Src must be either a SimStruct of same type (name) as this.
     * @param src The SimValue to copy from
     * @return this SimStruct
     */
    public SimValue put(SimValue src) {

       // Check copy compatibility
       checkCast(src);

       // If same type, assign each field..                 
       if (isSameType(src)) {
          SimStruct structSrc = (SimStruct)src;
          for (int i=0; i<fieldList.size(); i++)
             getFieldValue(i).put(structSrc.getFieldValue(i));       
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
     * @param startBit Starting bit index in this SimStruct
     * @param startBitSrc Starting bit index in the source SimValue
     * @return this SimStruct
     */
    public SimValue put(SimValue src, int nbits, int startBit, int startBitSrc) {

       // Check this or src variable bounds not exceeded
       checkBitBounds(src, nbits, startBit, startBitSrc);
       
       // Copy bits in as large chunks as possible
       int chunk, width, high, low = 0;
       SimValue value;
       for (int i=0; i<fieldList.size() && nbits>0; i++, low=high) {
       
          // Get field width and low and high bit indices within the complete struct
	  value = getFieldValue(i);
          width = value.getWidth();
          high = low + width;
	  
	  // If startBit falls within this field, find number of bits to copy and do it
          if (startBit>=low && startBit<high) {
             chunk = Math.min(nbits, high-startBit);
             value.put(src, chunk, startBit-low, startBitSrc);
	     nbits -= chunk;
	     startBit += chunk;
	     startBitSrc += chunk;
          }
       }             
       return this;       
    }				 

    /**
     * Put a boolean in specified bit position in this SimValue
     * @param l The boolean value to write
     * @param b Bit index in this to write to
     * @return this SimValue
     */
    public SimValue put(boolean l, int b) {
    
       // Check bit index is within variable
       checkBitBounds(b);

       // Find bit within struct
       int high, low = 0;
       SimValue value;
       for (int i=0; i<fieldList.size(); i++, low=high) {
       
          // Get field width and low and high bit indices within the complete struct
	  value = getFieldValue(i);
          high = low + value.getWidth();
	  
	  // If bit index falls within this field, set it to l
          if (b>=low && b<high)
	     value.put(l, b-low);
       }                   
       return this;
    }
    
    /**
     * Put the value in the passed ArrayList into this SimValue.
     * No action if list is null or empty, or where a list
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

       // Check list does not have more elements than no. of struct fields
       if (list.size()>fieldList.size())
          throw new SimException(
	     "cannot assign an initialiser of length "+list.size()+
	     " to a "+printType());

       // Copy each list element..
       for (int i=0; i<list.size(); i++) {

          // No action if element is null (skip struct field)
          if ((o=list.get(i))==null)
             continue;
	  
	  // Copy element
	  getFieldValue(i).putObj(o);
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
	     "SimStruct.referTo(SimValue,SimRange,SimRange): values unconnectable");
         
       int bl = range.getLow();
       int bh = range.getHigh();
       int high, low = 0;
       SimValue value;
       
       for (int i=0; i<fieldList.size(); i++, low=high) {
	  value = getFieldValue(i);
          high = low + value.getWidth();

          // If low bit falls within this field..
          if (bl>=low && bl<high) {
	  
	     // Check high bit also falls within this field
	     if (bh<low || bh>=high)
	        throw new SimException(
		   "SimStruct.referTo(SimValue,SimRange,SimRange): "+
		   "bitrange does not fall within a field boundary");

             // If range is smaller than the field, referTo within the field..
	     // (If range is smaller than field value width the field value is compound type)  
	     if (bl>low || bh<high-1) {
	        value.referTo(src, rangeSrc, new SimRange(bl-low, bh-low));
		return;
	     }
	     
	     // ..else range equates to entire field value, refer to src field value..
             else {
	        putFieldValue(i, partSrc); 
                return;
             }		
          }
       }
       
       // range falls outside the struct
       throw new SimException(
          "SimStruct.referTo(SimValue,SimRange,SimRange): range does not fall within struct");		
    }
    
    /**
     * Set the source SimVariable for this value, where the SimVariable
     * is an SDE output.
     * @param srcVar The source SimVariable
     */
    public void setSrcVar (SimVariable srcVar) {
       this.srcVar = srcVar;
       for (int i=0; i<fieldList.size(); i++)
          getFieldValue(i).setSrcVar(srcVar);
    }
           
    /** Print SimStruct, default format */
    public String toString () {
       return print(null);
    }
    
    /**
     * Test changed flag
     * @return True if any SimStruct array elements changed since last clearChanged()
     */
    public boolean wasChanged () {    
       for (int i=0; i<fieldList.size(); i++)
          if (getFieldValue(i).wasChanged())
	     return true;
       return false;	    
    }
                   
    // Constructor
    //
    protected SimStruct () {
       type = STRUCT;
       fieldMap = new HashMap();
       fieldList = new ArrayList();
    }
    
    // Add a field to the struct
    // @param fieldName Field name
    // @param value A SimValue which is the field
    //
    protected void addField (String fieldName, SimValue value) {
    
       // Check field name does not already exist
       if (fieldMap.containsKey(fieldName))
          throw new SimException(
	     "SimStruct.addField(String,SimValue): field "+fieldName+
	     " already exists in struct");
	     
       // Add field
       Field f = new Field(fieldName, value);
       fieldMap.put(fieldName, f);
       fieldList.add(f);	     
    }

    // Clone
    // @return A clone of this SimStruct
    //
    protected Object clone() throws CloneNotSupportedException {

        SimStruct v = (SimStruct) super.clone();
	Field f;
	
        v.fieldMap = new HashMap();
        v.fieldList = new ArrayList();
        for (int i=0; i<fieldList.size(); i++) {
	   f = ((Field)fieldList.get(i)).duplicate();
	   v.fieldMap.put(f.getName(), f);
	   v.fieldList.add(f);
        }	   
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
        
    // Return true if src SimValue has same number and types of fields
    // @param src The SimValue to test
    // @return True if src has same number and types of fields
    //
    protected boolean isSameType (SimValue src) {

       if (!(src instanceof SimStruct))
          return false;

       SimStruct ss = (SimStruct)src;	  
       if (fieldList.size()!=ss.fieldList.size())
          return false;	  
       for (int i=0; i<fieldList.size(); i++)
          if (!getFieldValue(i).isSameType(ss.getFieldValue(i)))	  
	     return false;
       return true;	     
    }
 
    // Return the field name corresponding to the indexed field
    // @param index Index of field, starting at 0 for first field
    // @return The name of this field
    //
    private String getFieldName (int index) {
       return ((Field)(fieldList.get(index))).getName();
    }

    // Return the SimValue corresponding to the indexed field
    // @param index Index of field, starting at 0 for first field
    // @return The SimValue for this field
    //
    private SimValue getFieldValue (int index) {
       return ((Field)(fieldList.get(index))).getValue();
    }

    // Associate a SimValue with a field, by field index
    // @param index Index of field
    // @param value New SimValue to associate with field
    //
    private void putFieldValue (int index, SimValue value) {
       ((Field)(fieldList.get(index))).put(value);
    }
    
    // Class to hold name/value pairs for struct fields
    //
    private class Field implements Cloneable {
    
       // Field name
       String name;
       
       // Field value
       SimValue value;
       
       // Constructor
       // @param name Field name
       // @param value Field value
       //
       protected Field (String name, SimValue value) {
          this.name = name;
	  this.value = value;
       }
       
       // Return field name
       // @return Field name, String
       //
       protected String getName() {
          return name;
       }
       
       // Return field value
       // @return Field value, SimValue
       //
       protected SimValue getValue() {
          return value;
       }
       
       // Duplicate
       // @return This Field, cloned
       //
       protected Field duplicate () {
          try {
             Field f = (Field)super.clone();
	     f.name = new String(name);
	     f.value = (SimValue)value.duplicate();
	     return f;
          } catch (CloneNotSupportedException e) {
	     // Cannot happen
	     return null;
	  }	     
       }
       
       // Set field value
       // @param The value to set
       //
       protected void put (SimValue value) {
          this.value = value;
       }	  	  	  
    }
}
