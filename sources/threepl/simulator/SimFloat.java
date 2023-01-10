/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */
package threepl.simulator;




/**
 * A SimFloat is a SimNumber containing a floating point number of arbitrary
 * mantissa and exponent precision. It may be created as a FLOAT type.
 * 
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
public class SimFloat extends SimNumber {

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

       // If src is a SimFloat, do a put which takes care of resize.
       // Note sext is irrelevant..
       if (src instanceof SimFloat)
          put(src);
              
       // ..else do bitwise copy
       else {
       	      
          // Copy common bits
          put(src, Math.min(w, ws), 0, 0);
       
          // If this is wider than src, zero pad or sign extend according to sext
          if (w>ws) {
             boolean msb = sext ? src.getBit(ws-1) : false;	  
             for (int i=ws; i<w; i++)
	        put(msb, i);
          }
       }	  	   
       return this;	     
    }
    
    /**
     * Return value as a double
     * @return The SimFloat value as a double
     */
    public double getDouble() {    
       throw new SimException("SimFloat.getDouble():not yet implemented");
    }

    /**
     * Return formatted value
     * @param fmt A printf style format string, or null
     * @return The formatted value
     */
    public String print (String fmt) {
    
       // If no format, use default format..
       if (fmt==null || fmt=="")
          return toString();
	  
       // ..else try to format accordingly
       else
          return SimPrintf.sprintf(fmt, getDouble());	         	  
    }

    /**
     * Put a double into the SimFloat
     * @param fval The value to write
     * @return This SimFloat
     */
    public SimValue put(double fval) {
       throw new SimException("SimFloat.put(double):not yet implemented");
    }

    /**
     * Print value, default format
     * @return Value in default format
     */
    public String toString () {
       return "" + getDouble();
    }

    // Default constructor (FLOAT of default precision)
    protected SimFloat() {
        super();
	type = FLOAT;
    }

    // Constructor for FLOAT with precision m, n
    // @param m Precision of the mantissa
    // @param n Precision of the exponent
    //
    protected SimFloat(int m, int n) {
        super(m, n);
	type = FLOAT;
    }

    // Return true if src SimValue can be cast to this type.
    // @param src The SimValue to test
    // @return True if src can be cast to this type
    //
    protected boolean isCastable (SimValue src) {
    
       // Cast allowed if:
       // src is bits:n, or
       // src is []bits:1, or
       // src is float, or
       // src is same width as this

       return super.isCastable(src) || isSameType(src) ||
          src.getWidth()==getWidth();
    }

    // Return true if src SimValue is of same type
    // @param src The SimValue to test
    // @return True if src is of same type
    //
    protected boolean isSameType (SimValue src) {
       return src instanceof SimFloat;
    }

    // Resize to new precision mNew,nNew.
    // @param mNew New precision of mantissa
    // @param nNew New precision of exponent
    // @return A new SimFloat containing same value as this, with new required precision
    //
    protected SimNumber resize(int mNew, int nNew) {
       throw new SimException("SimFloat.resize(int,int):not yet implemented");
    }
}
