/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */
package threepl.simulator;




/**
 * A SimBoolean is a SimValue containing a single boolean value. It may
 * be instantiated as a SimLog or SimControl.
 * 
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
public abstract class SimBoolean extends SimValue {

    // The boolean value
    protected boolean l;

    /**
     * Logical AND with another SimBoolean
     * @param src The SimBoolean to AND with this
     * @return This SimBoolean
     */
    public SimValue and(SimValue src) {

        // Check src is a SimBoolean
	checkType(src);
	
	if (!changed)
	   changed = l && !((SimBoolean) src).l;
	   
        l &= ((SimBoolean) src).l;
        return this;
    }

    /**
     * Cast another SimValue into this
     * @param src SimValue to cast to this
     * @param sext Ignored
     * @return this SimValue
     */
    public SimValue cast (SimValue src, boolean sext) {
    
       // Check can cast
       checkCast(src);

       // Copy src lsb
       l = src.getBit(0);       
       return this;	     
    }
    
    /**
     * Clear
     * @return This SimBoolean
     */
    public SimValue clear() {
    
        if (!changed)
	   changed = l;
    
        l = false;
        return this;
    }

    /**
     * Equality test
     * @param src SimBoolean to test for equality with this
     * @return True if the SimBooleans are equal
     */
    public boolean eq(SimValue src) {

        // Check src is a SimBoolean
	checkType(src);
	   
        // Do compare
        return l == ((SimBoolean) src).l;
    }

    /**
     * Equality test with a boolean
     *
     * @return True if the value of this equals bval
     */
    public boolean eq(boolean bval) {
       return l==bval;
    }
         
    /**
     * Equality test with Object, which must be a Boolean.
     * @param o Object to test for equality with this
     * @return True if the Object equals this boolean value
     */
    public boolean eq (Object o) {
    
       // If not a single Boolean, return false
       if (!(o instanceof Boolean))
          throw new SimException(
	     "cannot compare a non-boolean initialiser to a "+printType()+" value");
	  
       // Do test
       return eq(((Boolean)o).booleanValue());	  
    }
    
    /**
     * Return logical value of bth bit, b must be 0
     * @param b Bit index, must be 0
     * @return The boolean value
     */
    public boolean getBit(int b) {
    
        // Check b==0
	if (b!=0)
	   throw new SimException("SimBoolean.getBit(int):operand must be zero");

        return l;
    }

    /**
     * Return boolean value
     * @return The boolean value
     */
    public boolean getBit() {
        return l;
    }

    /**
     * Return width in bits
     * @return The bit width of the boolean value, ie 1
     */
    public int getWidth() {
        return 1;
    }

    /**
     * Logical invert
     * @return This SimBoolean
     */
    public SimValue inv() {
    
	changed = true;
	
        l = !l;
        return this;
    }

    /**
     * Return true if the value is clear, false 
     * @return True if value is false
     */
    public boolean isClear () {
       return !l;
    }       
         
    /**
     * Return true is the src SimValue is connectable with this.
     * Must have boolean type.
     * @param src The SimValue to test for compatibility
     * @return True if src is connectable with this
     */
    public boolean isConnectable (SimValue src) {
       return src instanceof SimBoolean;
    }

    /**
     * Logical OR with another SimBoolean
     * @param src The SimBoolean to OR with this
     * @return This SimBoolean
     */
    public SimValue or(SimValue src) {

        // Check src is a SimBoolean
	checkType(src);

        if (!changed)
	   changed = !l && ((SimBoolean) src).l;
	   
        l |= ((SimBoolean) src).l;
        return this;
    }

    /**
     * Return value as a formatted integer (false->0, true->1)
     * @param fmt A printf style format string, or null
     * @return The formatted value
     */
    public String print (String fmt) {
    
       // If no format, use default format..
       if (fmt==null || fmt=="")
          return toString();
	  
       // ..else try to format as integer 0 or 1
       else
          return SimPrintf.sprintf(fmt, l ? 1 : 0);	         	  
    }
        
    /**
     * Get SimValue type as a string
     * @return Type as a String
     */
    public String printType () {
        return SimType.printType(type);
    }

    /**
     * Copy another SimValue into this
     * @param src The SimValue to copy from
     * @return this SimBoolean
     */
    public SimValue put(SimValue src) {

       // Check copy compatibility
       checkCast(src);

       // Get src value
       boolean lSrc = src.getBit(0);
       
       // Record changed status
       if (!changed)
          changed = l != lSrc;
       	   
       // Do the copy
       l = lSrc;

       return this;
    }

    /**
     * Put a boolean in specified bit position in this SimBoolean
     * @param l The boolean value to write
     * @param b Bit index in this to write to (must be 0)
     * @return This SimBoolean
     */
    public SimValue put(boolean l, int b) {

        // Check b==0
	if (b!=0)
	   throw new SimException(
	      "SimBoolean.getBit(boolean,int):bit index must be zero");

        if (!changed)
	   changed = this.l != l;
	   
        this.l = l;
        return this;
    }

    /**
     * Put a boolean into this SimBoolean
     * @param l The boolean value to write
     * @return This SimValue
     */
    public SimValue put(boolean l) {

        if (!changed)
	   changed = this.l != l;
	   
        this.l = l;
        return this;
    }

    /**
     * Put the value in the passed Object, which must be a Boolean,
     * into this SimValue.
     * @param o Object containing value to copy
     */
    public void putObj (Object o) {

       // Check element is Boolean       
       if (!(o instanceof Boolean))
          throw new SimException(
	     "cannot assign a non-boolean initialiser to a "+printType()+" value");
        
       // Copy value
       put(((Boolean)o).booleanValue());      	      
    }
    
    /**
     * Copy another SimValue into this, with specified bit ranges
     * @param src The SimValue to copy from
     * @param nbits The number of bits to copy
     * @param startBit Starting bit index in this SimValue
     * @param startBitSrc Starting bit index in the source SimValue
     * @return this SimNumber
     */
    public SimValue put(SimValue src, int nbits, int startBit, int startBitSrc) {

        // Check this or src variable bounds not exceeded
	checkBitBounds(src, nbits, startBit, startBitSrc);

        return put(src.getBit(startBitSrc), startBit);
    }

    /**
     * Logical XOR another SimBoolean with this
     * @param src The SimBoolean to XOR with this
     * @return this SimBoolean
     */
    public SimValue xor(SimValue src) {

        // Check src is a SimBoolean
	checkType(src);

        if (!changed)
	   changed = ((SimBoolean) src).l;
	   
        l ^= ((SimBoolean) src).l;
        return this;
    }

    // Default constructor
    protected SimBoolean() {
        l = false;
    }

    // Return true if src SimValue can be cast to this type.
    // Note numeric cast to boolean is allowed: the lsb is copied.
    // @param src The SimValue to test
    // @return True if src can be cast to this type
    //
    protected boolean isCastable (SimValue src) {
    
       // Cast allowed if:
       // src is bits:n, or
       // src is []bits:1
       // src is boolean, or
       // src is int or uint, or
       // src is same width as this
        
       return super.isCastable(src) || isSameType(src) || src instanceof SimInteger ||
          src.getWidth()==getWidth();
    }

    // Return true if src SimValue is of same type
    // @param src The SimValue to test
    // @return True if src is of same type
    //
    protected boolean isSameType (SimValue src) {
       return src instanceof SimBoolean;
    }
}
