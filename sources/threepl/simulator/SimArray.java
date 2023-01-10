/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */
package threepl.simulator;

import java.util.ArrayList;



/**
 * A SimArray contains an array of SimValues. Note these can be of any type
 * (SimArrays, SimStructs, or primitive types, eg SimInt) but all elements
 * in the array are of the same type. A SimArray has one dimension. Multidimensional
 * arrays are implemented by having SimArrays as the array elements.
 *
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
@SuppressWarnings("all")
public class SimArray extends SimValue {

    // Array data (elements)
    private SimValue[] array;
           
    /**
     * If this SimArray has a source SimVariable, execute the behaviour of
     * that variable's source SDE. Otherwise execute behaviour of the array elements,
     * as they may have individual sources. The result will be to possibly alter all or part of
     * this SimArray.
     * @param time Simulator time; used to avoid calling behaviour() redundantly 
     * @param force True if behaviour should happen regardless of time
     */
    public void behaviour (double time, boolean force) {
       if (srcVar!=null)
          srcVar.behaviour(time, force);
       else
          for (int i=0; i<array.length; i++)
	     array[i].behaviour(time, force);
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
     * Clear array
     * @return this SimArray
     */
    public SimValue clear() {
       for (int i=0; i<array.length; i++)
          array[i].clear();
       return this;	  
    }

    /** Clear changed flag */
    public void clearChanged () {
       for (int i=0; i<array.length; i++)
          array[i].clearChanged();
    }
    
    /**
     * Return the number of array dimensions of this array.
     * @return Number of array dimensions
     */
    public int dimensions () {
       return array.length>0 ? 1+array[0].dimensions() : 1;
    }

    /**
     * Return the length of the nth dimension of this array.
     * @param n The index of the dimension
     * @return Length of the nth dimension 
     */   
    public int dimension (int n) {
    
       // Check n valid
       if (n<0 || n>=dimensions()) {
          int dims = dimensions();
          throw new SimException(
	     "array has only "+dims+" dimension"+(dims>1?"s":""));
       }	  

       return n==0 ? array.length : array[0].dimension(n-1);	  
    }       
    
    /**
     * Equality test. Arrays must be of same dimensionality
     * and contain equal elements.
     * @param src SimValue to test for equality with this
     * @return True if the SimValues are equal
     */
    public boolean eq(SimValue src) {
    
       // Check compatibility
       checkType(src);
              	     
       // Check for element equality
       SimArray arraySrc = (SimArray)src;
       for (int i=0; i<array.length; i++)
          if (!array[i].eq(arraySrc.array[i]))
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
	     "SimArray.eq(Object): object must be an ArrayList");
       
       ArrayList list = (ArrayList)o;
                  
       // If list is not equal in length to the array, return false
       if (list.size()!=array.length)
          return false;	             

       // Test equality of each element..
       for (int i=0; i<list.size(); i++) {

          // If any list element null, it is "equivalent" to
	  // the corresponding array element
          if ((o=list.get(i))==null)
             continue;
	  
	  // Test element equality
	  if (!array[i].eq(o))
	     return false;
       } 
       return true;   
    }    

    /**
     * Return logical value of bth bit
     * @param b Bit index starting from lsb in 0th element
     * @return The boolean value of the bth bit
     */
    public boolean getBit(int b) {

       // Check bit index is within variable
       checkBitBounds(b);

       // Find bit within array and return it
       int w = getElementWidth();
       int i = b / w;
       b = b - i * w;              
       return array[i].getBit(b);    
    }

    /**
     * Return a selected element from the array.
     * @param index is the array index
     * @return The selected SimValue element
     */
    public SimValue getElement (int index) {
    
       // Check index
       if (index<0 || index>=array.length)
          throw new SimException(
	     "SimArray.getElement(int): array index out of bounds");
	     
       return array[index];
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
     *        an array subscript (null, Integer or SimRange) valid for this SimArray.
     * @return A SimValue which is derived from this, constrained by the specs list.
     */                
    public SimValue getPart (ArrayList specs) {
    
       // If spec list is null or empty, return this array
       if (specs==null || specs.size()==0)
          return this;

       SimValue v;
       
       // Remove first spec in list
       Object spec = specs.remove(0);       
       
       // If null, represents an empty subscript, behave as if full range..
       if (spec==null) {
       
          if (array.length==0)
	     return this;
	     
          specs.add(0, new SimRange(0, array.length-1));
          v = getPart(specs);
       }
       	  
       // ..else if a SimRange, refers to an element range..
       else if (spec instanceof SimRange) {
          SimRange range = (SimRange)spec;
	  int low = range.getLow();
	  int high = range.getHigh();
	  if (low<0 || high>=array.length)
	     throw new SimException("array subscript range out of bounds");
          int width = high-low+1;	     
          v = new SimArray(width);
	  for (int i=0; i<width; i++)
	     ((SimArray)v).array[i] = array[low+i].getPart((ArrayList)specs.clone());
       }
       
       // ..else if an Integer, refers to single element..
       else if (spec instanceof Integer) {
          int subscript = ((Integer)spec).intValue();
	  if (subscript<0 || subscript>=array.length)
	     throw new SimException("array subscript out of bounds");
          v = array[subscript].getPart(specs);
       }
       
       // ..else invalid spec for an array subscript, error
       else
          throw new SimException("cannot access an array by a field name");	     	  

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
       int elWidth = array[0].getWidth();
       int low = bl/elWidth;
       int high = bh/elWidth;

       // Check bitrange lies within array
       if (bl<0 || bh>=getWidth())
          throw new SimException("SimArray.getPart(SimRange): range is outside array bounds");

       // If bitrange is smaller than one element, return part of the element
       if (bh-bl+1 < elWidth)
          return array[low].getPart(new SimRange(bl-low*elWidth, bh-low*elWidth));
	  
       // Otherwise check bitrange falls on element boundaries
       if ((bh+1)%elWidth!=0 || bl%elWidth!=0)
          throw new SimException(
	     "SimArray.getPart(SimRange): bitrange does not fall on array element boundaries");	                      	  

       // If equates to one element, return the element..
       if (low==high)
          return array[low];
	  
       // ..else return a new array being part of the element range
       else
          return new SimArray(this, new SimRange(low, high));
    }              

    /**
     * Return the single underlying primitive SimValue, or error
     * @return The single underlying SimValue
     */
    public SimValue getSingle () {
       
       // Check isSingle
       if (!isSingle())
          throw new SimException("SimArray.getSingle(): is a compound value");
	  
       return array[0].getSingle();	  
    }
         
    /**
     * Return width of entire array in bits
     * @return Width of entire array in bits
     */
    public int getWidth() {
       return array.length*getElementWidth();
    }       
     
    /**
     * Logical or bitwise invert this SimArray.
     * Needed so can invert arrays of control signals generated in an SDEInv.
     * @return this SimValue
     */
    public SimValue inv() {
       for (int i=0; i<array.length; i++)
          array[i].inv();
       return this;
    }

    /**
     * Return true if the array is clear, ie all elements zero or false
     * @return True if array elements are all zero/false
     */
    public boolean isClear () {
       for (int i=0; i<array.length; i++)
          if (!array[i].isClear())
	     return false;
       return true;
    }       	     
         
    /**
     * Return true if the src SimValue is connectable with this for assignment
     * Must have same type and width. Overridden by compound types.
     * @param src The SimValue to test for compatibility
     */
    public boolean isConnectable (SimValue src) {    
       return src.type==ARRAY &&
              array.length==((SimArray)src).array.length &&
              array[0].isConnectable(((SimArray)src).array[0]);
    }

    /**
     * Return true if this array contains a single underlying primitive element
     * @return True if this array contains a single underlying primitive element
     */
    public boolean isSingle () {
       return array.length==1 && array[0].isSingle();
    }
    
    /**
     * Return true if this is a primitive type (ie non-compound).
     * Always false for SimArrays.
     * @return False     
     */
    public boolean isPrimitive () {
       return false;
    }
         
    /**
     * Return formatted array elements
     *
     * @param fmt A printf style format string, or null
     *
     * @return The formatted array elements, concatenated
     */
    public String print (String fmt) {
    
       StringBuffer b = new StringBuffer();       	  
    
       b.append("{");
       for (int i=0; i<array.length; i++) {
          if (i!=0)
	     b.append(",");
          b.append(array[i].print(fmt));
       }
       b.append("}");	  
       return b.toString();	         	  	  
    }

    /**
     * Return SimArray type as a string
     * 
     * @return Formatted string showing SimArray type, dimensions and element type
     */
    public String printType () {
       return "["+array.length+"]"+(array.length!=0?array[0].printType():"");
    }
        
    /**
     * Copy another SimValue into this. Src must be a SimArray of same dimensionality as this.
     * @param src The SimValue to copy from
     * @return this SimArray
     */
    public SimValue put(SimValue src) {

       // Check copy compatibility
       checkCast(src);
              	     
       // If same type, copy each element..                 
       if (isSameType(src)) {
          SimArray arraySrc = (SimArray)src;
          for (int i=0; i<array.length; i++)
             array[i].put(arraySrc.array[i]);
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
     * @param startBit Starting bit index in this SimArray
     * @param startBitSrc Starting bit index in the source SimValue
     * @return this SimArray
     */
    public SimValue put(SimValue src, int nbits, int startBit, int startBitSrc) {

       // Check this or src variable bounds not exceeded
       checkBitBounds(src, nbits, startBit, startBitSrc);

       // Copy bits in chunks as large as possible..

       // Find starting element and element width
       int elWidth = getElementWidth();
       int elStart = startBit/elWidth;
       int low = elStart * elWidth;
       int chunk, high;

       // Copy to each element within the bit range
       for (int i=elStart; i<array.length; i++, low=high) {
          high = low + elWidth;
          chunk = Math.min(nbits, high-startBit);
	  array[i].put(src, chunk, startBit-low, startBitSrc);
	  nbits -= chunk;
	  startBit += chunk;
	  startBitSrc += chunk;
       }
       return this;       
    }				 
    
    /**
     * Put a boolean in specified bit position in this SimArray. Bit index
     * refers to the array as a whole, not a single element.
     * @param l The boolean value to write
     * @param b Bit index in this to write to (index of lsb of zeroth element = 0)
     * @return this SimArray
     */
    public SimValue put(boolean l, int b) {
    
       // Check bit index is within variable
       checkBitBounds(b);
       
       // Find bit in array
       int elWidth = getElementWidth();
       int i = b / elWidth;
       b = b - i * elWidth;
       
       // Put the bit
       array[i].put(l, b);
       return this;    
    }

    /**
     * Copy value from an array of hex strings.
     * Convert each hex string and place in corresponding array element.
     * If hex is shorter than array, clear the remainder.
     * If hex is longer than array, ignore extra values.
     * @param hex An array of hexadecimal strings, with or without leading "0x"
     * @return This SimArray
     */
    public SimValue put(String[] hex) {
    
       int n = Math.min(array.length, hex.length);
       for (int i=0; i<n; i++)
          array[i].put(hex[i]);
	  
       if (hex.length<array.length)
          for (int i=n; i<array.length; i++)
	     array[i].clear();	
	          
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

       // Check list is not longer than array
       if (list.size()>array.length)
          throw new SimException(
	     "cannot assign an initialiser of length "+list.size()+
	     " to a "+printType());

       // Copy each list element..
       for (int i=0; i<list.size(); i++) {

          // No action if element is null (skip array index)
          if ((o=list.get(i))==null)
             continue;
	  
	  // Copy element
	  array[i].putObj(o);
       }
    }

    /**
     * Copy a value into a selected element of the array.
     * @param index is the array index
     * @param src is the SimValue to copy to the array element
     */
    public void putElement (int index, SimValue  src) {
    
       // Check index
       if (index<0 || index>=array.length)
          throw new SimException(
	     "SimArray.putElement(int,SimValue): array index out of bounds");

       array[index].put(src);    
    }
           	     
    /**
     * Within the bit range within this SimArray, change the references to
     * corresponding SimValues within the array to references to the corresponding
     * SimValues represented by the src SimValue and associated bit range rangeSrc.
     * The width of the ranges must be the same and the subset SimValue of this and
     * src (corresponding to the bit ranges) must be connectable; ie same structure
     * with same types. 
     * @param src The source SimValue
     * @param rangeSrc The bit range within the source SimValue
     * @param range This bit range within this SimArray
     */    
    public void referTo (SimValue src, SimRange rangeSrc, SimRange range) {

       SimValue partSrc = src.getPart(rangeSrc);

       // Check that the SimValues corresponding to the ranges are connectable.
       // This implicitly checks that the ranges are the same length.
       if (!getPart(range).isConnectable(partSrc))
          throw new SimException(
	     "SimArray.referTo(SimValue,SimRange,SimRange): values unconnectable");
         
       int bl = range.getLow();
       int bh = range.getHigh();
       int elWidth = array[0].getWidth();
       int low = bl/elWidth;
       int high = bh/elWidth;

       // Check bitrange lies within array
       if (bl<0 || bh>=getWidth())
          throw new SimException(
	     "SimArray.referTo(SimValue,SimRange,SimRange): range is outside array bounds");

       // If this range smaller than one element, referTo within the element
       // (if range is smaller than an element width then the elements are compound type)
       if (bh-bl+1 < elWidth) {
          array[low].referTo(src, rangeSrc, new SimRange(bl-low*elWidth, bh-low*elWidth));
	  return;
       }
       
       // Otherwise check this range falls on array element boundaries
       if ((bh+1)%elWidth!=0 || bl%elWidth!=0)
          throw new SimException(
	     "SimArray.referTo(SimValue,SimRange,SimRange): "+
	     "bitrange does not fall on array element boundaries");

       // If range equates to one element, referTo src element..
       if (low==high)
          array[low] = partSrc;
	  
       // ..else range equates to a number of elements, referTo each src element in turn
       else
          for (int i=low; i<high; i++)
	     array[i] = ((SimArray)partSrc).array[i];
    }

    /**
     * Set the source SimVariable for this value, where the SimVariable
     * is an SDE output.
     * @param srcVar The source SimVariable
     */
    public void setSrcVar (SimVariable srcVar) {
       this.srcVar = srcVar;
       for (int i=0; i<array.length; i++)
          array[i].setSrcVar(srcVar);
    }
           
    /**
     * Return sizeof the entire array SimValue, in elements
     * @return size, in elements, of this SimArray
     */
    public int sizeof () {
       int size = array.length;
       if (array.length>0 && array[0].type==ARRAY)
          size *= array[0].sizeof();
       return size;
    }

    /**
     * Print SimArray, default format
     */
    public String toString () {
       return print(null);
    }
    
    /**
     * Test changed flag
     * @return True if any SimArray array elements changed since last clearChanged()
     */
    public boolean wasChanged () {
       for (int i=0; i<array.length; i++)
         if (array[i].wasChanged())
	    return true;
       return false;	    
    }
                   
    // Constructor
    // @param length Length of array in elements
    // @param prototype A SimValue to clone for each array element
    //
    protected SimArray (int length, SimValue prototype) {
       this(length);
       for (int i=0; i<length; i++)
          array[i] = prototype.duplicate();
    }

    // Constructor with array length.
    // Creates a SimArray of desired length whose elements are null
    // @param length Length of array in elements
    //
    private SimArray (int length) {
       type = ARRAY;
       array = new SimValue[length];
    }

    // Constructor with SimArray and SimRange
    // @param v SimArray from which a subset will be generated
    // @param range The element range of the input array
    //
    private SimArray (SimArray v, SimRange range) {

       type = ARRAY;
           
       // Check range is compatible with input SimArray
       int low = range.getLow();
       int high = range.getHigh();
       if (low<0 || high>=v.array.length)
          throw new SimException("array subscript range out of bounds");
	  
       // Create new array using range 
       array = new SimValue[high-low+1];
       int j = 0;
       for (int i=low; i<=high; i++)
          array[j++] = v.array[i];	  
    }
    
    // Clone
    // @return A clone of this SimArray
    //
    protected Object clone() throws CloneNotSupportedException {
        SimArray v = (SimArray) super.clone();
	v.array = new SimValue[array.length];
	for (int i=0; i<array.length; i++)
	   v.array[i] = (SimValue)array[i].duplicate();
        return (Object) v;
    }

    // Return width of an array element
    // @return Element width in bits
    //
    private int getElementWidth () {
       return array[0].getWidth();
    } 
              
    // Return true if src SimValue can be cast to this type
    // @param src The SimValue to test
    // @return True if src can be cast to this type
    //
    protected boolean isCastable (SimValue src) {

       // Cast allowed if:
       // src is bits:n, or
       // src is []bits:1, or
       // src is same total size, or
       // this is []bits:1
       
       return super.isCastable(src) || src.getWidth()==getWidth() ||
              (array[0] instanceof SimBits && array[0].getWidth()==1);
    }
        
    // Return true if src SimValue is of same type
    // @param src The SimValue to test
    // @return True if src is of same type
    //
    protected boolean isSameType (SimValue src) {
    
       // src must be a SimArray of same length, with elements
       // same type as elements in this	  
       if (!(src instanceof SimArray))
          return false;
	  
       SimArray arrSrc = (SimArray) src;
       return array.length==arrSrc.array.length && array[0].isSameType(arrSrc.array[0]);      	  
    }    
}
