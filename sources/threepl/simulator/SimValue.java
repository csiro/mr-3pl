/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */
package threepl.simulator;

import java.util.ArrayList;

import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exec.Field;
import threepl.exec.Type;
import threepl.exec.WordSpec;
import threepl.parser.Constant;



/**
 * A SimValue is a general purpose data storage register. It can contain a
 * boolean value, signed or unsigned integer value, fixed point number,
 * floating point number, a struct, or an array of one of the above types.
 * Subclasses are used to represent the different types which may exist.
 * Refer SimNumber, SimInteger, SimSigned, SimUnsigned, SimFloat, SimLog,
 * SimStruct, SimArray.
 * SimValues are used to store the current values of Threepl variables.
 *
 * @version $Revision: 8240 $
 * @author Andrew Tulloh
 */
@SuppressWarnings("all")
public abstract class SimValue implements Cloneable, TDEConstants, Constant, SimTypes {
    
    // SimValue type from SimTypes: INT, UINT, etc.
    protected int type;

    // Set true when the value changes, cleared with clearChanged() */
    protected boolean changed;

    // SimVariable which corresponds to the SDE output which drives this
    // value (or null if not connected to an SDE output)
    protected SimVariable srcVar;

    // If non-null, single bit SimValue which should be copied to all bits of
    // this during the behaviour function
    protected SimValue singleBitSrc;
        
    /**
     * Add another SimValue to this (not implemented for this class).
     * @param src The SimValue to add to this
     * @return this SimValue
     */
    public SimValue add(SimValue src) {
       throw new SimException(
          "SimValue.add(SimValue):not implemented for "+getClass().getName());
    }

    /**
     * Logical or bitwise AND another SimValue with this (not implemented for this class).
     * @param src The SimValue to AND with this
     * @return this SimValue
     */
    public SimValue and(SimValue src) {
       throw new SimException(
          "SimValue.and(SimValue):not implemented for "+getClass().getName());
    }

    /**
     * If this SimValue has a source SimVariable, execute the behaviour of
     * that variable's source SDE. The result will be to possibly alter this
     * value.
     * Overridden by compound types.
     * @param time Simulator time; used to avoid calling behaviour() redundantly 
     * @param force True if behaviour should happen regardless of time
     */
    public void behaviour (double time, boolean force) {
       if (srcVar!=null)
          srcVar.behaviour(time, force);

       // Handle primitive values with width>1 that are connected to a single bit signal
       if (singleBitSrc!=null) {
          int w = getWidth();
	  boolean l = singleBitSrc.getBit();
          for (int b=0; b<w; b++)
	     put(l, b);
       }          	  
    }
    
    /**
     * Do bitwise AND with another SimValue. The AND is done from bit 0
     * to the highest bit index in src or this, whichever is lower.
     * @param src SimValue to bitwise AND with this
     * @return  this SimValue
     */
    public SimValue bitwiseAnd (SimValue src) {    
       int width = Math.min(getWidth(), src.getWidth());
       for (int b=0; b<width; b++)
          put(src.getBit(b) && getBit(b), b);
       return this;	  
    }
    
    /**
     * Do bitwise OR with another SimValue. The OR is done from bit 0
     * to the highest bit index in src or this, whichever is lower.
     * @param src SimValue to bitwise OR with this
     * @return  this SimValue
     */
    public SimValue bitwiseOr (SimValue src) {    
       int width = Math.min(getWidth(), src.getWidth());
       for (int b=0; b<width; b++)
          put(src.getBit(b) || getBit(b), b);
       return this;	  
    }
    
    /**
     * Do bitwise XOR with another SimValue. The XOR is done from bit 0
     * to the highest bit index in src or this, whichever is lower.
     * @param src SimValue to bitwise XOR with this
     * @return  this SimValue
     */
    public SimValue bitwiseXor (SimValue src) {    
       int width = Math.min(getWidth(), src.getWidth());
       for (int b=0; b<width; b++)
          put((src.getBit(b) && !getBit(b) || !src.getBit(b) && getBit(b)), b);
       return this;	  
    }
                 
    /**
     * Do bitwise NOT on this SimValue
     * @return  this SimValue
     */
    public SimValue bitwiseNot () {    
       int width = getWidth();
       for (int b=0; b<width; b++)
          put(!getBit(b), b);
       return this;	  
    }

    /**
     * Cast another SimValue into this
     * @param src SimValue to cast to this
     * @param sext If true, do sign extension, else zero pad (if relevant)
     * @return this SimValue
     */
    public abstract SimValue cast (SimValue src, boolean sext);
                             
    /**
     * Clear the SimValue (set to zero or false)
     * @return this SimValue
     */
    public abstract SimValue clear();

    /** Clear changed flag */
    public void clearChanged () {
       changed = false;
    }

   /**
    * Create a new SimValue of desired primitive type.
    * Uses default precision according to type.
    * @param type Primitive type from SimTypes
    */
   public static SimValue create (int type) {
      switch (type) {
      case INT:
      case FIXED:
         return new SimSigned(type);
      case ENUM:
      case UINT:
      case UFIXED:
         return new SimUnsigned(type);
      case CONTROL:
         return new SimControl();
      case LOG:
         return new SimLog();
      case FLOAT:
         return new SimFloat();
      default:
         // Note type BITS has no default width
         throw new SimException("SimValue.create(int): invalid type ("+type+")");
      }      
   }
       
   /**
    * Create a new SimValue of desired primitive type.
    * The width argument is interpreted as precision according to type.
    * If type is CONTROL or LOG, a SimArray with width elements is created.
    * @param type Primitive type from SimTypes
    */
   public static SimValue create (int type, int width) {
      switch (type) {
      case INT:
      case FIXED:
         return new SimSigned(type, width);
      case ENUM:
      case UINT:
      case UFIXED:
         return new SimUnsigned(type, width);
      case BITS:
         return new SimBits(width);	 
      case CONTROL:
         return new SimArray(width, new SimControl());
      case LOG:
         return new SimArray(width, new SimLog());
      case FLOAT:
         return new SimFloat(width, 0);
      default:
         throw new SimException("SimValue.create(int): invalid type ("+type+")");
      }      
   }
   
   /**
    * Create a new SimValue of desired primitive type and precision.
    * The m,n arguments are interpreted as precision according to type.
   * @param type Primitive type from SimTypes
    */
   public static SimValue create (int type, int m, int n) {
      switch (type) {
      case INT:
      case FIXED:
         return new SimSigned(type, m, n);
      case UFIXED:
      case UINT:
         return new SimUnsigned(type, m, n);	 
      case FLOAT:
         return new SimFloat(m, n);
      case BITS:
         if (n!=0)
	    throw new SimException("SimValue.create(int,int,int): n must be 0 for type BITS");
         return new SimBits(m);	    
      case ENUM:
         if (n!=0)
	    throw new SimException("SimValue.create(int,int,int): n must be 0 for type ENUM");
         return new SimUnsigned(type, m);	    
      default:
         throw new SimException("SimValue.create(int): invalid type ("+type+")");
      }         
   }

    /**
     * Create a SimArray
     * @param length Number of elements in the array
     * @param proto A prototype SimValue from which the array elements are cloned
     * @return The new SimArray
     */
    public static SimValue createArray (int length, SimValue proto) {
       return new SimArray(length, proto);
    }
         
    /**
     * Return a new (primitive type constant) SimValue based on the passed TDEVar.
     * WordSpec should be non-null and contains a single word whose width
     * defines the width of the constant.
     * @param tdeVar TDEVar containing constant information
     * @return A new SimValue representing the constant
     */
    public static SimValue createFromTDEVar (TDEVar tdeVar) {
       return createFromTDEVar(tdeVar, 0);
    }
    
    /**
     * Return a new (primitive type constant) SimValue based on the passed TDEVar.
     * The bit width is forced to be at least the passed minWidth (non-boolean
     * types only). This allows creation of constants where a larger width than that
     * specified in the TDEVar WordSpec is required. 
     * WordSpec should be non-null and contains a single word whose width
     * defines the width of the constant (which is overridden by minWidth if larger).
     * @param tdeVar TDEVar containing constant information
     * @param minWidth Minimum bit width for non-boolean types
     * @return A new SimValue representing the constant
     */
    public static SimValue createFromTDEVar (TDEVar tdeVar, int minWidth) {
    
       TDEVtype type = tdeVar.getType();
       WordSpec w = tdeVar.getWordSpec();
       SimValue v;
       
       // Check Wordspec non-null
       if (w==null)
          throw new SimException(
	     "SimValue.createFromTDEVar(TDEVar): TDEVar "+tdeVar.getAliasId()+" has null WordSpec");

       int width = Math.max(minWidth, w.getWidth(0));
       	     
       switch (type) {
       case UINT:
          v = create(UINT, width);
	  v.put(((Long)tdeVar.getConst()).longValue());
	  break;
       case BITS:
          v = create(BITS, width);
	  v.put(((Long)tdeVar.getConst()).longValue());
	  break;
       case INT:
          // Normally the minimum width of an INT is 2 bits to allow for the sign bit.
	  // However a TDEVar representing a constant INT with value 0 is allowed a
	  // width of 1. This special case is handled here by creating a 2 bit INT.
	  int val = (int)((Long)tdeVar.getConst()).longValue();
	  if (val==0 && width==1)
	     width = 2;
	  v = create(INT, width);
	  v.put(val);
	  break;
       case BOOL:
          if (width!=1)
	     throw new SimException(
	        "SimValue.createFromTDEVar(TDEVar): TDEVar "+tdeVar.getAliasId()+" is LOG with width "+width);
          v = create(LOG);
	  v.put(((Boolean)tdeVar.getConst()).booleanValue());
	  break;
       default:
	  throw new SimException(
	     "SimValue.convertType(int): invalid TDEVar.type ("+type+")");       
       }
       return v;
    }

    /**
     * Return a new SimValue based on the passed threepl Type
     * @param type Threepl Type
     * @return A new SimValue representing the Type
     */
    public static SimValue createFromType (Type type) {
    
       SimValue v;
       int prim_type;
       Type array_type;
       ArrayList field_index;
       
       // If is primitive type, return a primitive value..
       if (type.isPrimitive())
          v = createFromPrimType(type.getPrimType(), type.numBits());
       
       // ..else if is array type, return array..
       else if ((array_type = type.getArrayType()) != null)     
	  v = new SimArray(type.getArrayDim(), createFromType(array_type));
       
       // ..else if is struct type, return struct..
       else if ((field_index = type.getFieldIndex()) != null) {
          SimStruct struct = new SimStruct();
	  Field field;
	  for (int i=0; i<field_index.size(); i++) {
	     field = (Field)field_index.get(i);
	     struct.addField(field.getName(), createFromType(field.getType()));
          }
	  v = struct;	     
       }
       
       // ..else invalid type
       else
          throw new SimException("SimValue.createFromType(Type): invalid type");

       return v;    
    }

    /**
     * Return a new SimValue based on the passed threepl WordSpec. The WordSpec
     * must contain only a single word and cannot be an array. Creates a single
     * primitive value based on the word primitive type, with bit width described
     * by the word bitpair.
     * @param w Threepl WordSpec
     * @return A new SimValue representing the WordSpec
     */
    public static SimValue createFromWordSpec (WordSpec w) {
    
       int numWords;
       SimValue v;	     
       
       // If WordSpec contains multiple words, create a SimGroup to
       // represent them..
       if ((numWords = w.numWords()) > 1) {
          SimValue[] group = new SimValue[numWords];
	  for (int i=0; i<numWords; i++)
	     group[i] = createFromPrimType(w.getPrimType(i), w.getWidth(i));
          v = createGroup(group);
       }	  

       // ..else create a primitive type
       else
          v = createFromPrimType(w.getPrimType(0), w.getWidth(0));

       return v;       	     
    }

    /**
     * Create a SimGroup
     * @param values A prototype SimValue from which the array elements are cloned
     * @return The new SimArray
     */
    public static SimValue createGroup (SimValue[] values) {
       return new SimGroup(values);
    }
         
    /**
     * Return the number of array dimensions of this SimValue.
     * Overridden by SimArray.
     * @return 0 (for non-array)
     */
    public int dimensions () {
       return 0;
    }

    /**
     * Return the length of the nth dimension of this SimValue.
     * Overridden by Simarray.
     * @param n The index of the dimension
     * @throws SimException (for non-array)
     */   
    public int dimension (int n) {
       throw new SimException("not an array, does not have dimensions");
    }       
    
    /**
     * Divide this by another SimValue (not implemented for this class).
     * @param divisor The SimValue to divide into this
     * @return this SimValue
     */
    public SimValue div(SimValue divisor) {
       throw new SimException(
          "SimValue.div(SimValue):not implemented for "+getClass().getName());
    }

    /**
     * Divide this by another SimValue, generate remainder also (not implemented for this class).
     * @param divisor The SimValue to divide into this
     * @return A SimValue containing the remainder of the division
     */
    public SimValue divrem(SimValue divisor) {
       throw new SimException(
          "SimValue.divrem(SimValue):not implemented for "+getClass().getName());
   }
         
    /**
     * Duplicate this SimValue
     * @return A new SimValue, clone of this
     */
    public SimValue duplicate() {
        try {
            return (SimValue) clone();
        } catch (CloneNotSupportedException e) {
            // Cannot happen
            return null;
        }
    }

    /**
     * Equality test with Object
     * @param o Object to test for equality with this
     * @return True if the Object value(s) equal the value(s) within this
     */
    public abstract boolean eq (Object o);

    /**
     * Equality test with a double
     * @param fval The value to compare
     * @return True if the value of this equals fval
     */
    public boolean eq(double fval) {
       throw new SimException(
          "SimValue.eq(double):not implemented for "+getClass().getName());
    }
         
    /**
     * Equality test with a boolean
     * @param bval The value to compare
     * @return True if the value of this equals bval
     */
    public boolean eq(boolean bval) {
       throw new SimException(
          "SimValue.eq(boolean):not implemented for "+getClass().getName());
    }
         
    /**
     * Equality test
     * @param src SimValue to test for equality with this
     * @return True if the SimValues are equal
     */
    public abstract boolean eq(SimValue src);

    /**
     * Test whether this is greater than or equal to another SimValue
     * @param src The SimValue to compare to this
     * @return True if this is greater than or equal to the other SimValue
     */
    public boolean ge(SimValue src) {
        return !lt(src);
    }

    /**
     * Return logical value of bth bit
     * @param b Bit index starting from lsb
     * @return The boolean value of the bth bit
     */
    public abstract boolean getBit(int b);

    /**
     * Return logical value of a single bit quantity
     * @return The boolean value of this single bit SimValue
     */
    public boolean getBit() {
       throw new SimException(
          "SimValue.getBit():not implemented for "+getClass().getName());
    }
    
    /**
     * Return value as a double (not implemented for this class).
     * @return The Simvalue value as a double. Only valid for numeric types.
     */
    public double getDouble() {
       throw new SimException(
          "SimValue.getDouble():not implemented for "+getClass().getName());
    }

    /**
     * Return value as an int (not implemented for this class).
     * @return SimValue value as an int
     */
    public int getInt() {
       throw new SimException(
          "SimValue.getInt():not implemented for "+getClass().getName());
    }

    /**
     * Return a SimValue which is a subset of this SimValue as constrained by
     * the specs list. The specs list is a list of array subscripts and/or struct field names,
     * in any order. If the spec resolves to an array, the SimValue returned is a SimArray with
     * dimensions according to the specs. If the spec resolves to a struct, a SimStruct is
     * returned. Otherwise a primitive SimValue (SimInt etc.) is returned. The returned SimValue
     * may or may not be created (in some cases, this SimValue may be returned). If specs list is
     * null or empty, this SimValue is returned. This method is overridden by SimArray and SimStruct.
     * @param specs List of array subscripts, ranges or struct names.
     * @return A SimValue which is derived from this, constrained by the specs list.
     */                
    public SimValue getPart (ArrayList specs) {
    
       // specs must be null or empty (cannot apply array subscripts or field names
       // to a primitive variable)
       if (!(specs==null || specs.size()==0))
          throw new SimException(
	     "cannot apply array subscripts or field names to a primitive variable");
	     
       // Return this
       return this;	     
    }
    
    /**
     * Return a SimValue which is a subset of this SimValue as constrained by the
     * specified low and high bit indices, which index the entire variable.
     * Bit indices must fall on appropriate boundaries: the entire width for
     * primitive value and struct types, and on array element boundaries for arrays.
     * This method is overridden by SimArray.
     * @param range A SimRange containing the low and high bit indicies
     * @return A SimValue which is derived from this, constrained by the bit indices.
     */
    public SimValue getPart (SimRange range) {
    
       int bl = range.getLow();
       int bh = range.getHigh();
       
       // Bit indices must encompass entire variable for primitive types
       if (bl!=0 || bh!=getWidth()-1)
          throw new SimException("SimValue.getPart(int,int): invalid bit indices ");
       	  
       // Bit range is always the full variable for primitive types
       return this;	  
    }              

    /**
     * Return the low order 32 bits of the value as an int (not implemented for this class).
     * @return Low order 32 bits of SimValue value, as an int
     */
    public int getRawInt() {
       throw new SimException(
          "SimValue.getRawInt():not implemented for "+getClass().getName());
    }
    
    /**
     * Return the single underlying primitive SimValue, ie this for
     * primitive types
     * @return this SimValue
     */
    public SimValue getSingle () {
       return this;
    }
         
    /**
     * Return type of this SimValue
     * @return Type, INT, UINT.. etc, (from SimTypes)
     */
    public int getType() {
        return type;
    }

    /**
     * Return width in bits
     * @return Total width of of SimValue in bits
     */
    public abstract int getWidth();
     
    /**
     * Test whether this is greater than another SimValue
     * @param src The SimValue to compare to this
     * @return True if this is greater than the other SimValue
     */
    public boolean gt(SimValue src) {
        return !le(src);
    }

    /**
     * Logical or bitwise invert this SimValue (not implemented for this class).
     * @return this SimValue
     */
    public SimValue inv() {
       throw new SimException(
          "SimValue.inv():not implemented for "+getClass().getName());
    }

    /**
     * Return true if the value is clear, ie zero or false for primitive
     * types and all elements/fields zero or false for compound types
     * @return True if value is all zero/false
     */
    public abstract boolean isClear ();
         
    /**
     * Return true is the src SimValue is connectable with this
     * Must have same type and width. Overridden by some types.
     * @param src The SimValue to test for connectability
     * @return True if src is connectable with this
     */
    public boolean isConnectable (SimValue src) {
       return type==src.type && getWidth()==src.getWidth();    
    }

    /**
     * Return true if this group contains a single underlying primitive element.
     * Always true for primitive types. Overridden by SimArray and SimGroup.
     * @return True
     */
    public boolean isSingle () {
       return true;
    }
    
    /**
     * Return true if this is a primitive type (ie non-compound).
     * Overridden by compound types.
     * @return True    
     */
    public boolean isPrimitive () {
       return true;
    }
         
    /**
     * Test whether this is less than or equal to another SimValue
     * @param src The SimValue to compare to this
     * @return True if this is less than or equal to the other SimValue
     */
    public boolean le(SimValue src) {
        return lt(src) || eq(src);
    }

    /**
     * Bitwise left shift this SimValue (not implemented for this class).
     * @param count A SimValue containing the number of bits to shift left (can be zero or negative)
     * @return this SimValue
     */
    public SimValue lsh(SimValue count) {
       throw new SimException(
          "SimValue.lsh(SimValue):not implemented for "+getClass().getName());
    }

    /**
     * Bitwise left shift by int (not implemented for this class).
     * @param count The number of bits to shift left (can be zero or negative)
     * @return this SimValue
     */
    public SimValue lsh (int count) {
       throw new SimException(
          "SimValue.lsh(int):not implemented for "+getClass().getName());
    }

    /**
     * Test whether this is less than another SimValue (not implemented for this class).
     * @param src The SimValue to compare to this
     * @return True if this is less than the other SimValue
     */
    public boolean lt(SimValue src) {
       throw new SimException(
          "SimValue.lt(SimValue):not implemented for "+getClass().getName());
    }

    /**
     * Multiply this by another SimValue (not implemented for this class).
     * @param multiplier The SimValue to multiply this by
     * @return this SimValue
     */
    public SimValue mul(SimValue multiplier) {
       throw new SimException(
          "SimValue.mul(SimValue):not implemented for "+getClass().getName());
    }

    /**
     * Test whether this is not equal to another SimValue
     * @param src The SimValue to compare to this
     * @return True if this is not equal to the other SimValue
     */
    public boolean ne(SimValue src) {
        return !eq(src);
    }

    /**
     * Negate this SimValue (not implemented for this class).
     * @return this SimValue
     */
    public SimValue neg() {
       throw new SimException(
          "SimValue.neg():not implemented for "+getClass().getName());
    }

    /**
     * Logical or bitwise OR another SimValue with this (not implemented for this class).
     * @param src The SimValue to OR with this
     * @return this SimVAlue
     */
    public SimValue or(SimValue src) {
       throw new SimException(
          "SimValue.or(SimValue):not implemented for "+getClass().getName());
    }

    /**
     * Return formatted value
     * @param fmt A printf style format string, or null
     * @return The formatted value
     */
    public abstract String print (String fmt);

    /**
     * Return SimValue type as a string
     * @return Formatted string showing SimValue type, dimensions, bit width as appropriate
     */
    public abstract String printType ();
    
    /**
     * Copy another SimValue into this
     * @param src The SimValue to copy from
     * @return this SimValue
     */
    public abstract SimValue put(SimValue src);

    /**
     * Copy another SimValue into this, with specified bit ranges
     * @param src The SimValue to copy from
     * @param n The number of bits to copy
     * @param startBit Starting bit index in this SimValue
     * @param startBitSrc Starting bit index in the source SimValue
     * @return this SimValue
     */
    public abstract SimValue put(SimValue src, int n, int startBit, 
                                 int startBitSrc);

    /**
     * Put a boolean in specified bit position in this SimValue
      * @param l The boolean value to write
     * @param b Bit index in this to write to
     * 
     * @return this SimValue
     */
    public abstract SimValue put(boolean l, int b);

    /**
     * Put a boolean into this single bit SimValue
     * @param l The boolean value to write
     * @return this SimValue
     */
    public SimValue put(boolean l) {
       throw new SimException(
          "SimValue.put(boolean):not implemented for "+getClass().getName());
    }	  
    
    /**
     * Put an int into this SimValue (not implemented for this class).
     * @param ival The value to write
     * @return this SimValue
     */
    public SimValue put(int ival) {
       throw new SimException(
          "SimValue.put(int):not implemented for "+getClass().getName());
    }
    
    /**
     * Put a double into this SimValue (not implemented for this class).
     * @param fval The value to write
     * @return this SimValue
     */
    public SimValue put(double fval) {
       throw new SimException(
          "SimValue.put(double):not implemented for "+getClass().getName());
    }

    /**
     * Copy value from hex string.
     * Convert each hex digit starting from rhs of string and
     * fill SimValue starting from lsb. If the string is longer than
     * necessary to fill the SimValue, unused hex characters or part
     * thereof are ignored. If the string is shorter than necessary,
     * only affected bits are changed.
     * @param hex A hexadecimal string, with or without leading "0x"
     * @return This SimValue
     */
    public SimValue put(String hex) {
    
       int b=0, width=getWidth(), nibble, i;
       
        // Convert to lower case and trim leading 0x if there
        hex = hex.trim().toLowerCase();
        if ((i = hex.indexOf("x")) >= 0)
            hex = hex.substring(i + 1);

       // Convert 1 char at a time, moving from end to start of String
       for (i=hex.length()-1; i>=0 && b<width; i--) {
          nibble = Integer.parseInt(hex.substring(i, i+1), 16);
	      for (int j=0; j<4 && b<width; j++)
	         put((nibble&(1<<j))!=0, b++);
       }     
       return this;     
    }

    /**
     * Copy value from an array of hex strings.
     * @param hex An array of hexadecimal strings, with or without leading "0x"
     * @return This SimValue
     */
    public SimValue put(String[] hex) {
       throw new SimException(
          "SimValue.put(String[]):not implemented for "+getClass().getName());
    }	  

    /**
     * Put the values in the passed Object into this SimValue.
     * @param o Object containing value(s) to copy
     */
    public abstract void putObj (Object o);

    /**
     * Copy another SimValue to this, where mask bits set
     * @param src SimValue to copy from
     * @param mask SimValue containing mask specifying which bits to copy
     * @return  this SimValue
     */
    public SimValue putWithMask(SimValue src, SimValue mask) {

        // Check mask is no wider than this or src
	int mwidth = mask.getWidth();
        if (mwidth > getWidth() || mwidth > src.getWidth())
           throw new SimException(
	      "SimValue.putWithMask(SimValue,SimValue):incompatible mask width");	       
	
        /* Copy bits */
	for (int b=0; b<mwidth; b++)
	   if (mask.getBit(b))
	      put(src.getBit(b), b);	      
        return this;
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
     * @param range This bit range within this SimArray
     */    
    public void referTo (SimValue src, SimRange rangeSrc, SimRange range) {
       throw new SimException(
          "SimValue.referTo(SimValue,SimRange,SimRange):not implemented for "+
	  getClass().getName());
    }
    
    /**
     * Dvide this by another SimValue and leave the remainder in this
     * (not implemented for this class).
     * @param divisor The SimValue to divide by
     * @return this SimValue
     */
    public SimValue rem(SimValue divisor) {
       throw new SimException(
          "SimValue.rem(SimValue):not implemented for "+getClass().getName());
    }

    /**
     * Bitwise right shift this SimValue (not implemented for this class).
     * @param count A SimValue containing the number of bits to shift right (can be zero or negative)
     * @return this SimValue
     */
    public SimValue rsh(SimValue count) {
       throw new SimException(
          "SimValue.rsh(SimValue):not implemented for "+getClass().getName());
    }

    /**
     * Bitwise right shift this SimValue (not implemented for this class).
     * @param count An int containing the number of bits to shift right (can be zero or negative)
     * @return this SimValue
     */
    public SimValue rsh (int count) {
       throw new SimException(
          "SimValue.rsh(int):not implemented for "+getClass().getName());
    }

    /**
     * Record that the source value for this SimValue is a single bit
     * which should be copied to all bits of this value during the behaviour function
     */
    public void setSingleBitSrc (SimValue src) {
    
       // Check src is single bit
       if (src.getWidth()>1)
          throw new SimException(
	     "Simvalue.setSingleBitSrc(SimValue): src value must be single bit");
	     
       singleBitSrc = src;	     
    }
         
    /**
     * Set the source SimVariable for this value, where the SimVariable
     * is an SDE output. Compound SimValue types will override.
     * @param srcVar The source SimVariable
     */
    public void setSrcVar (SimVariable srcVar) {
       this.srcVar = srcVar;
    }
           
    /**
     * Return sign of this SimValue (not implemented for this class).
     * @return sign of this SimValue
     */
    public int sign() {
       throw new RuntimeException(
          "SimValue.sign():not implemented for "+getClass().getName());
    }

    /**
     * Return sizeof this SimValue, in elements (always 1 if not a SimArray)
     * @return size, in elements, of this SimValue
     */
    public int sizeof () {
       return 1;
    }
                                  
    /**
     * Subtract another SimValue from this (not implemented for this class).
     * @param src The SimValue to subtract
     * @return this SimValue
     */
    public SimValue sub(SimValue src) {
       throw new SimException(
          "SimValue.sub(SimValue):not implemented for "+getClass().getName());
    }

    /**
     * Test changed flag
     * @return True if SimValue value has changed since last clearChanged()
     */
    public boolean wasChanged () {
       return changed;
    }
                  
    /**
     * Logical or bitwise XOR another SimValue with this (not implemented for this class).
     * @param src The SimValue to XOR with this
     * @return this SimValue
     */
    public SimValue xor(SimValue src) {
       throw new SimException(
          "SimValue.xor(SimValue):not implemented for "+getClass().getName());
    }

    // Check a bit index is within a SimValue. Throw SimException if not.
    // @param b Bit index
    // 
    protected void checkBitBounds (int b) {
       if (b<0 || b>=getWidth())
          throw new SimException(
	     "SimValue.checkBitBounds(int): invalid bit index");
    }	     	   
	                                        
    // Check for valid arguments to put(SimValue, int, int, int) method.
    // Throw SimException if not.
    // @param src The source SimValue for a bit copy operation
    // @param nbits The number of bits to copy (to this)
    // @param startBit Starting bit index in this
    // @param startBitSrc Startinf bit index in src
    //
    protected void checkBitBounds (SimValue src, int nbits, int startBit, int startBitSrc) {
    
       if (nbits<0 ||
           startBit<0 || startBit+nbits>getWidth() ||
           startBitSrc<0 || startBitSrc+nbits>src.getWidth())
          throw new SimException(
	     "SimValue.checkBitBounds(SimValue,int,int,int): bit range specifier(s) exceed variable width");	       
    }

    // Check whether the src SimValue can be cast to this
    // @param src The SimValue to test
    // @throws SimException if cannot be cast
    //
    protected void checkCast (SimValue src) {
       if (!isCastable(src))
          throw new SimException("cannot cast "+src.printType()+" to "+printType());
    }    

    // Check src SimValue is of same type
    // @param The SimValue to test
    //
    protected void checkType (SimValue src) {
       if (!isSameType(src))
          throw new SimException("not a "+printType()+" type");
    }       

    // Return a new SimValue based on the passed threepl Type
    // @param type Threepl Type
    // @return A new SimValue representing the Type
    //
    protected static SimValue createFromPrimType (Ptype type, int width) { 
       SimValue v;   
       switch (type) {
       case ENUM:	  
       case UINT:
	  return create(UINT, width);
       case BITS:
          return create(BITS, width);
       case INT:
	  return create(INT, width);
       case LOG:
          return create(LOG);
       // typeNONE used for control signals	  
       case NONE:
          return create(CONTROL, width);
       default:
	  throw new SimException(
	     "SimValue.createFromPrimType(int,int): invalid primitive type ("+type+")");
       }
    }    
    
    // Return true if src SimValue can be cast to this type.
    // Some compound types override.
    // @param src The SimValue to test
    // @return True if src can be cast to this type
    //
    protected boolean isCastable (SimValue src) {
    
       // Anything can be cast from:
       // src is bits:n, or
       // src is []bits:1, or
       // src has same width as this
       
       return src instanceof SimBits || 
	      (src instanceof SimArray &&
	       ((SimArray)src).getElement(0) instanceof SimBits &&
	       ((SimArray)src).getElement(0).getWidth()==1) ||
	       src.getWidth()==getWidth();	      
    }

    // Return true if src SimValue is of same type
    // @param src The SimValue to test
    // @return True if src is of same type
    //
    protected abstract boolean isSameType (SimValue src);
}
