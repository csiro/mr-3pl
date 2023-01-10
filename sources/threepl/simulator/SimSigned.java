/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */
package threepl.simulator;




/**
 * A SimSigned is a signed SimInteger. It may be created as
 * an INT or FIXED type.
 * 
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
public class SimSigned extends SimInteger {

   /**
    * Divide (signed) this by another SimSigned, generate remainder also.
    * Quotient is left in this, remainder is returned.
    * @param dor The SimSigned to divide by
    * @return A SimSigned containing the remainder of the division
    */
   public SimValue divrem(SimValue dor) {

      // Check dor is a SimInteger
      checkType(dor);
      
      SimSigned dividend, remainder;
      SimInteger divisor;
      boolean dividend_negative, divisor_negative;

      // If either dividend or divisor is negative, convert to positive quantities first
      if ((dividend_negative = (sign() < 0)) | (divisor_negative = (dor.sign() < 0))) {
      
	 // Take abs value of dividend. If was negative, extend precision by 1 bit on LHS to
	 // ensure abs val is positive.
	 dividend = this;
         if (dividend_negative) {
            dividend = (SimSigned)((SimSigned)dividend.duplicate().neg()).resize(dividend.m+1, dividend.n);
	    dividend.putBit(false, dividend.getWidth()-1); 
         }
	 	 
	 // Take abs value of divisor. If was negative, extend precision by 1 bit on LHS to
	 // ensure abs val is positive.
	 divisor = (SimInteger)dor;
         if (divisor_negative) {
            divisor = (SimSigned)((SimSigned)divisor.duplicate().neg()).resize(divisor.m+1, divisor.n);
	    divisor.putBit(false, divisor.getWidth()-1); 
         }
	 	 
	 // Try again with positive quantities
	 remainder = (SimSigned)dividend.divrem(divisor);
	 
	 // Make remainder same sign as dividend; if dividend and divisor signs are different,
	 // negate quotient.
	 if (dividend_negative)
	    remainder.neg();
         if (dividend_negative != divisor_negative)
	    dividend.neg();
         put(dividend);
         return remainder.resize(m, n);	    	    
      }

      // Do unsigned division (dividend and divisor are guaranteed positive at this point)
      return super.divrem(dor);      
   }
   
    /**
     * Test whether this is less than another SimInteger
     * Resize the smaller of 'this' and src to the width of the larger before the
     * comparison. This allows comparison of different width quantities.
     * @param src The SimInteger to compare to this
     * @return True if this is less than the other SimInteger
     */
    public boolean lt(SimValue src) {
        int s1 = sign();
        int s2 = src.sign();

        if (s1 < s2)
            return true;
        else if ((s1 > s2) || (s1 == 0))
            return false;
        else
            return super.lt(src);
    }

    /**
     * Negate this SimSigned
     * @return this SimSigned
     */
    public SimValue neg() {
        return twosComplement();
    }

    /**
     * Copy another SimValue into this
     * @param src The SimValue to copy from
     * @return this SimValue
     */
    public SimValue put (SimValue src) {
    
       // If src is a SimSigned, and this and src have same n but this
       // has a larger m, do an efficient bit copy with sign extension..
       SimSigned srcInt;
       if (src instanceof SimSigned &&
           (srcInt=(SimSigned)src).n == n &&
	    srcInt.m < m) {
          int nbits = srcInt.m+srcInt.n;
	  int upperBits = m+n-nbits;	    
          put(src, nbits, 0, 0);
	  if (srcInt.sign()<0)
	     setBits(upperBits, nbits);
          else
	     clearBits(upperBits, nbits);	     
	  return this;
       }
       	   	    
       // ..else do integer copy
       else
          return super.put(src);
    }
    
    /**
     * Return sign, 1=positive, 0=zero, -1=negative
     * @return Sign, 1=positive, 0=zero, -1=negative
     */
    public int sign() {
        return testBit((m + n) - 1) ? (-1) : (eqZero() ? 0 : 1);
    }

    // Default constructor (INT of default width)
    protected SimSigned (int type) {
        super();
	this.type = type;
    }

    // Constructor for FIXED with precision m, n
    // @param m Precision to left of binary point
    // @param n Precision to right of binary point
    //
    protected SimSigned(int type, int m, int n) {
    
        super(m, n);
	this.type = type;

        // Check there are enough bits to hold sign
	if (m==0 || (m==1 && n==0))
	   throw new SimException(
	      "SimSigned.SimSigned(int,int):insufficient bits to hold sign");
    }

    // Constructor for INT with precision nbits
    // @param nbits Precision to the left of binary point
    //
    protected SimSigned(int type, int nbits) {

        super(nbits);
	this.type = type;

        // Check there are enough bits to hold sign
	if (m<2)
	   throw new SimException(
	      "SimSigned.SimSigned(int):insufficient bits to hold sign");
    }
}
