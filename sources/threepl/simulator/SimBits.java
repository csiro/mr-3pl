/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */
package threepl.simulator;




/**
 * A SimBits is a collection of bits. It may be created only as
 * a BITS type.
 * 
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
public class SimBits extends SimUnsigned {

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
     * Copy another SimValue into this
     * @param src The SimValue to copy from
     * @return this SimValue
     */
    public SimValue put (SimValue src) {
    
       // No need to check copy compatibility..

       // Allow any SimValue to be cast to a SimBits. Clear
       // any unused upper bits in this. Note n is always 0
       // for SimBits.
       int nbits = Math.min(m, src.getWidth());
       put(src, nbits, 0, 0);
       if (nbits<m)
          clearBits(m-nbits, nbits);
       return this;	  
    }
    
    // Constructor for BITS with precision nbits
    // @param nbits Number of bits
    //
    protected SimBits(int nbits) {
        super(BITS, nbits);
    }

    // Return true if src SimValue can be cast to this SimBits.
    // Always true
    // @param src The SimValue to test
    // @return True
    //
    protected boolean isCastable (SimValue src) {
       return true;
    }
}
