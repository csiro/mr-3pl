/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */
package threepl.simulator;




/**
 * A SimNumber is a numeric SimValue. It can contain a signed or
 * unsigned integer, signed or unsigned fixed point, or floating
 * point number. The required precision in bits is specified at
 * creation. Precision is specified by two ints: for integer and
 * fixed point SimNumbers, m is the number of bits to the left of
 * the binary point, and n is the number of bits to the right.
 * For integers, n is 0 and m is referred to as nbits. For floating
 * point SimNumbers, m is the number if bits in the mantissa and
 * n is the number of bits in the exponent.
 * 
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
public abstract class SimNumber extends SimValue {

    // Number of bits in int
    protected static final int S = 32;

    // Mask info for copying between SimNumbers
    static int[] masks = new int[S];

    static {
        for (int i = 0; i < S-1; i++)
	   masks[i] = (1 << i+1) - 1;
        masks[S-1] = -1;	   
    }

    // Max field width for printing integer and floats
    protected static final int MAX_PRINT_WIDTH = 20;
     
    // Number value stored as int[]
    protected int[] data;

    // Precision to left of binary point, or of mantissa
    protected int m;

    // Precision to right of binary point, or of exponent
    protected int n;
    
    // Default print format
    protected String defaultFmt;

    /**
     * Bitwise AND
     * @param src The SimNumber to AND with this
     * @return this SimNumber
     */
    public SimValue and(SimValue src) {
    
        // Check src is compatible
	checkType(src);

        SimNumber v = ((SimNumber) src).resize(m, n);

        for (int i = 0; i < data.length; i++)
            putDataWord(i, getDataWord(i) & v.getDataWord(i));

        return this;
    }

    /**
     * Clear the SimNumber
     * @return this SimNumber
     */
    public SimValue clear() {
        return clearBits(m + n, 0);
    }

    /**
     * Equality test
     * Resize the smaller of 'this' and src to the width of the larger before the
     * comparison. This allows comparison of different width SimNumbers.
     * @param src SimNumber to test for equality with this
     * @return True if the SimNumbers are equal
     */
    public boolean eq(SimValue src) {

        // Check src is compatible
	checkType(src);
	   
        // Do compare
	int mWider = getMWider((SimNumber)src);
	int nWider = getNWider((SimNumber)src);	
        SimNumber vthis = resize(mWider, nWider);
        SimNumber vsrc = ((SimNumber) src).resize(mWider, nWider);

        for (int i = 0; i < vthis.data.length; i++)
            if (vthis.getDataWord(i) != vsrc.getDataWord(i))
                return false;

        return true;
    }

    /**
     * Equality test with a double
     * @param fval The value to compare
     * @return True if the value of this equals fval
     */
    public boolean eq(double fval) {
       return getDouble()==fval;
    }
         
    /**
     * Equality test with Object, which must be an Integer, Long
     * or Double.
     * @param o Object to test for equality with this
     * @return True if the Object value equals this numeric value
     */
    public boolean eq (Object o) {
    
       // Do test
       if (o instanceof Integer)
          return eq(((Integer)o).intValue());
       else if (o instanceof Long)
          return eq(((Long)o).longValue());
       else if (o instanceof Double)
          return eq(((Double)o).doubleValue());
       else
          throw new SimException(
	     "cannot compare a non-numeric initialiser to a "+
	     printType()+" value");
    }
    
    /**
     * Return logical value of bth bit
     * @param b Bit index (lsb index = 0)
     * @return The boolean value of the bth bit
     */
    public boolean getBit(int b) {
    
        // Check b valid
	checkBitBounds(b);
	   
        return testBit(b);
    }

    /**
     * Return logical value of a single bit quantity
     * @return The boolean value of this single bit SimNumber
     */
    public boolean getBit() {
    
        // Check number has only one bit
	if (m+n!=1)
	   throw new SimException("SimNumber.getBit():m+n!=1");
	   
        return testBit(0);
    }

   /**
    * Return value as a raw int. No sign extension. Upper bits lost if nbits>=32
    * @return Low order 32 bits of SimNumber value, as an int
    */
    public int getRawInt() {
        return getDataWord(0);
    }

    /**
     * Return width in bits
     * @return Total width of of SimNumber in bits
     */
    public int getWidth() {
        return m + n;
    }

    /**
     * Bitwise invert
     * @return this SimNumber
     */
    public SimValue inv() {
        for (int i = 0; i < data.length; i++)
            putDataWord(i, ~getDataWord(i));

        return this;
    }

    /**
     * Return true if the value is clear, ie zero
     * @return True is value is zero
     */
    public boolean isClear () {
       return eqZero();
    }
         
    /**
     * Bitwise left shift. We assume the shift count can be contained in an
     * int. Truncate value in count if it has a fractional part.     * 
     * @param count A SimNumber containing the number of bits to shift left (can be zero or negative)
     * @return this SimNumber
     */
    public SimValue lsh(SimValue count) {

        // Check src is compatible
	checkType(count);
	
        return shift(count.getInt());
    }

    /**
     * Bitwise left shift by int
     * @param count The number of bits to shift left (can be zero or negative)
     * @return this SimNumber
     */
    public SimValue lsh (int count) {
       return shift(count);
    }
                
    /**
     * Bitwise OR another SimNumber with this
     * @param src The SimNumber to OR with this
     * @return this SimNumber
     */
    public SimValue or(SimValue src) {

        // Check src is compatible
	checkType(src);

        SimNumber v = ((SimNumber) src).resize(m, n);

        for (int i = 0; i < data.length; i++)
            putDataWord(i, getDataWord(i) | v.getDataWord(i));

        return this;
    }

    /**
     * Return SimNumber type as a string
     * @return Formatted string showing SimNumber type, precision
     */
    public String printType () {
        String typeStr = SimType.printType(type)+":";
        switch (type) {
        case INT:
        case UINT:
	case BITS:
	   return typeStr+getWidth();
        case FIXED: 
        case UFIXED:
	case FLOAT:
	   return typeStr+m+":"+n;
	}
	return null;
    }

    /**
     * Copy another SimValue into this
     * @param src The SimValue to copy from
     * @return this SimValue
     */
    public SimValue put(SimValue src) {

       // Check copy compatibility
       checkCast(src);

       // If is same type, copy number..
       if (isSameType(src))
          putBits(((SimNumber) src).resize(m, n), m + n, 0, 0);
       
       // ..else if src is a SimBoolean, copy to lsb and clear the rest..
       if (src instanceof SimBoolean) {
          putBit(src.getBit(), 0);
	  if (m+n>1)
	     clearBits(m+n-1, 1);
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
     * @param startBit Starting bit index in this SimValue
     * @param startBitSrc Starting bit index in the source SimValue
     * @return this SimNumber
     */
    public SimValue put(SimValue src, int nbits, int startBit, int startBitSrc) {

        // Check this or src variable bounds not exceeded
	checkBitBounds(src, nbits, startBit, startBitSrc);

        return putBits(src, nbits, startBit, startBitSrc);
    }

    /**
     * Put a boolean in specified bit position in this SimNumber
     * @param l The boolean value to write
     * @param b Bit index in this to write to (lsb index = 0)
     * @return this SimNumber
     */
    public SimValue put(boolean l, int b) {

       // Check bit index does not exceed variable bounds
       checkBitBounds(b);

       return putBit(l, b);
    }

    /**
     * Put a boolean into this single bit SimNumber
     * @param l The boolean value to write
     * @return this SimNumber
     */
    public SimValue put(boolean l) {

       // Check variable is one bit wide
       if (getWidth()!=1)
          throw new SimException(
	     "SimNumber.put(boolean):cannot assign a single bit to a multi-bit variable");

        return putBit(l, 0);
    }

    /**
     * Put the value in the passed Object, which must be an Integer,
     * Long or Double, into this SimValue.
     * @param o Object containing value to copy
     */
    public void putObj (Object o) {

       // Copy object
       if (o instanceof Integer)
          put((double)((Integer)o).intValue());      	      
       else if (o instanceof Long)
          put(((Long)o).longValue());      	      
       else if (o instanceof Double)
          put(((Double)o).doubleValue());      	      
       else
          throw new SimException(
	     "cannot assign a non-numeric initialiser to a "
	     +printType()+" value");
    }

    /**
     * Bitwise right shift. We assume the shift count can be contained in an
     * int. Truncate value in count if it has a fractional part.
     * @param count A SimNumber containing the number of bits to shift right (can be zero or negative)
     * @return this SimNumber
     */
    public SimValue rsh(SimValue count) {

        // Check src is compatible
	checkType(count);
	
        return shift(-count.getInt());
    }

    /**
     * Bitwise right shift by int
     * @param count The number of bits to shift right (can be zero or negative)
     * @return this SimNumber
     */
    public SimValue rsh (int count) {
       return shift(-count);
    }
                
    /**
     * Logical or bitwise XOR another SimNumber with this
     * @param src The SimNumber to XOR with this
     * @return this SimNumber
     */
    public SimValue xor(SimValue src) {

        // Check src is compatible
	checkType(src);
	
        SimNumber v = ((SimNumber) src).resize(m, n);

        for (int i = 0; i < data.length; i++)
            putDataWord(i, getDataWord(i) ^ v.getDataWord(i));

        return this;
    }

    // Default constructor (m,n=default values)
    protected SimNumber () {
    
        // Make with m=32, n=0
        this(S, 0);
    }

    // Constructor with precision m, n
    // @param m Precision to left of binary point, or of mantissa
    // @param n Precision to right of binary point, or of exponent
    //
    protected SimNumber (int m, int n) {
    
        // Check m, n valid
	if (m<0 || n<0)
	   throw new SimException("SimNumber.SimNumber(m,n):invalid arg(s)");
	   
        this.m = m;
        this.n = n;
        data = makeData(m + n);
	
	// Number of decimal places required (for print) to include all
	// binary places, and default print format
	int decimalPlaces = (int)Math.ceil(n * Math.log(2)/Math.log(10));
	defaultFmt = "%."+decimalPlaces+"f";
    }
    
    // Set a bit
    // @param b Bit index (lsb index = 0)
    // @return this SimNumber
    //
    protected SimNumber setBit(int b) {
        return putBit(true, b);
    }

    // Set a contiguous range of bits
    // @param nbits The number of contiguous bits to set
    // @param startBit Start bit index (lsb index = 0)
    // @return this SimNumber
    //
    protected SimNumber setBits(int nbits, int startBit) {    
        int word, startBitWord;
	int numBits;
	int width = m+n;

        while (nbits>0) {	   
           word = startBit/S;	      
	   startBitWord = startBit % S;	      
	   if ((numBits = Math.min(nbits, Math.min(S-startBitWord, Math.min(width-startBit, S))))==0)
	      break;		 
           putDataWord(word, getDataWord(word) | mask(startBitWord, startBitWord+numBits-1));	      
	   nbits -= numBits;
	   startBit += numBits;
        }
        return this;
    }

    // Return an int word from the SimNumber value
    // @param i Index within data array of word to return
    // @return The int which is the ith word of the data array
    //
    protected int getDataWord(int i) {
        return (i == data.length-1)
               ? (data[i] & mask(0, ((m + n) - 1) % S)) : data[i];
    }

    // Clear a bit
    // @param b Bit index (lsb index = 0)
    // @return this SimNumber
    //
    protected SimNumber clearBit(int b) {
        return putBit(false, b);
    }

    // Clear a contiguous range of bits
    // @param nbits The number of contiguous bits to clear
    // @param startBit Start bit index (lsb index = 0)
    // @return this SimNumber
    //
    protected SimNumber clearBits(int nbits, int startBit) {
        int word, startBitWord;
	int numBits;
	int width = m+n;

        while (nbits>0) {	   
           word = startBit/S;	      
	   startBitWord = startBit % S;	      
	   if ((numBits = Math.min(nbits, Math.min(S-startBitWord, Math.min(width-startBit, S))))==0)
	      break;		 
           putDataWord(word, getDataWord(word) & ~mask(startBitWord, startBitWord+numBits-1));	      
	   nbits -= numBits;
	   startBit += numBits;
        }
        return this;
    }

    // Clone
    // @return A new SimNumber, clone of this
    //
    protected Object clone() throws CloneNotSupportedException {
        SimNumber v = (SimNumber) super.clone();
        v.data = v.makeData(m + n);

        for (int i = 0; i < data.length; i++)
            v.data[i] = data[i];

        return v;
    }

    // Resize to new precision
    // @param mNew The new precision to left of binary point, or mantissa width
    // @param nNew The new precision to right of binary point, or exponent width
    // @return A new SimNumber which contains the value of this, but with the new precision
    //
    protected abstract SimNumber resize(int mNew, int nNew);

    // Equals zero test
    // @return True if this SimNumber equals zero.
    //
    protected boolean eqZero() {
        for (int i = 0; i < data.length; i++) {
            if (getDataWord(i) != 0) {
                return false;
            }
        }
        return true;
    }

    // Make the data array based on total bit width required
    // @param nbits The total bit width (precision) required
    // @return A new int[] large enough to hold nbits
    //
    protected int[] makeData(int nbits) {
        return new int[(nbits / S) + (((nbits % S) > 0) ? 1 : 0)];
    }

    // Put a boolean value in bit index b
    // @param l The boolean value to write
    // @param b Bit index (lsb index = 0)
    // @return this SimNumber
    //
    protected SimNumber putBit(boolean l, int b) {
        int i = b / S;
        int v = getDataWord(i);
        return putDataWord(i, l ? (v | mask(b % S)) : (v & ~mask(b % S)));
    }

    // Copy another SimValue into this, with specified bit ranges
    // @param src The SimValue to copy from
    // @param n The number of bits to copy
    // @param startBit Starting bit index in this SimNumber
    // @param startBitSrc Starting bit index in the source SimValue
    // @return this SimNumber
    //
    protected SimNumber putBits(SimValue src, int nbits, int startBit, 
                               int startBitSrc) {
        if (nbits == 0) {
            return this;
        }

	// If single bit, copy it. This will happen where
	// src is a SimLog, or a SimNumber or SimArray of width 1)..
	if (nbits==1)
	   putBit(src.getBit(startBitSrc), startBit);
	   
	// ..else if src is SimNumber (width>1), use efficient bit range copy..
	else if (src instanceof SimNumber) {

           int word, wordSrc, startBitWord, startBitWordSrc;
	   int numBits, d, dSrc;
	   int width = m+n;
	   int widthSrc = src.getWidth();

           while (nbits>0) {	   
              word = startBit/S;
	      wordSrc = startBitSrc/S;
	      
	      startBitWord = startBit % S;
	      startBitWordSrc = startBitSrc % S;
	      
	      if ((numBits = Math.min(nbits,
	                   Math.min(S-startBitWord,
	                      Math.min(S-startBitWordSrc,
			         Math.min(width-startBit,
			            Math.min(widthSrc-startBitSrc, S))))))==0)
                 break;				    
		 
              d = getDataWord(word) & ~mask(startBitWord, startBitWord+numBits-1);
	      dSrc = ((SimNumber)src).getDataWord(wordSrc) &
	                mask(startBitWordSrc, startBitWordSrc+numBits-1);
              if (startBitWordSrc > startBitWord)
	         d |= dSrc >>> (startBitWordSrc-startBitWord);
              else if (startBitWordSrc < startBitWord)
	         d |= dSrc << startBitWord - startBitWordSrc;
              else
	         d |= dSrc;		 		 			
              putDataWord(word, d);
	      
	      nbits -= numBits;
	      startBit += numBits;
	      startBitSrc += numBits;
	   }
	}
	
	// ..else src is some other SimValue type (width>1), copy bit by bit..
	else
	   for (int i=0; i<nbits; i++)
	      putBit(src.getBit(startBitSrc+i), startBit+i);
		   
        return this;
    }

    // Put an int word into the data array
    // @param i Index within data array of word to write to
    // @param ival The int to write
    // @return this SimNumber
    //
    protected SimNumber putDataWord(int i, int ival) {

        // Trim unwanted bits
	int temp = (i == data.length-1) ? (ival & mask(0, ((m + n) - 1) % S)) : ival;
	
        // Check for changed value
        if (!changed && data[i] != temp)
	   changed = true;
	
	// Write this word   
        data[i] = temp;

        return this;
    }

    // Shift count bits to the left. Count negative means a right shift.
    // Right shifts are unsigned (upper bit filled with zero).
    // @param count Number of bits to shift left (can be zero or negative)
    // @return this SimNumber
    //
    protected SimNumber shift(int count) {
        SimValue v = duplicate();
        int nbits = m + n;

        if (count > 0) {
            count = (count >= nbits) ? nbits : count;
            clearBits(count, 0);

            return putBits(v, nbits - count, count, 0);
        } else if (count < 0) {
            count = -count;
            count = (count >= nbits) ? nbits : count;
            clearBits(count, nbits - count);

            return putBits(v, nbits - count, 0, count);
        }

        return this;
    }

    // Test a single bit
    // @param b Bit index to test (lsb index = 0)
    // @return True if the bit is non-zero
    //
    protected boolean testBit(int b) {
        return (getDataWord(b / S) & mask(b % S)) != 0;
    }

    // Return the wider m of two SimNumbers
    // @param src The SimNumber to compare in precision to this
    // @return The larger m value
    //
    protected int getMWider (SimNumber src) {    
       return  m > src.m ? m : src.m;
    }
         
    // Return the wider n of two SimNumbers
    // @param src The SimNumber to compare in precision to this
    // @return The larger n value
    //
    protected int getNWider (SimNumber src) {
       return  n > src.n ? n : src.n;    
    }

    // Return a (int)mask for the single bit specified. No check is made
    // for bit index out of bounds.
    // @param b Bit index within a 32 bit int (lsb index = 0)
    // @return The 32 bit mask corresponding to the bit index
    //
    private int mask(int b) {
       return mask(b, b);
    }

    // Return a (int)mask which is set to 1 between bits s and e inclusive, 0
    // otherwise. No check is made for s or e out of bounds.
    // @param l The start bit index for the mask (lsb index = 0)
    // @param h The end bit index for the mask
    // @return The 32 bit mask corresponding to bits set from start to end inclusive
    //
    private int mask(int s, int e) {

        // Ensure s is the lower bit
        if (s > e) {
	    int temp = s;
	    s = e;
	    e = temp;
        }

        // Generate mask
        return s>0 ? masks[e]-masks[s-1] : masks[e];
    }
}
