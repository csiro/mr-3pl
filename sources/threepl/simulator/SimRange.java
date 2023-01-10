/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */


package threepl.simulator;



/**
 * A SimRange object contains two non-negative ints, the smaller being the low
 * range linit and the larger being the high range limit. The numbers can be
 * the same (single valued range). Ranges are used to specify array index
 * ranges and bitranges.
 * 
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
@SuppressWarnings("all")
public class SimRange {

    /**
     * Unique identifier string for this class and build used to help
     * debugging and binary tracking
     */
    private String _class_cvs_id = "$id$";

    /** low range identifier */
    protected int low;
    
    /** high range identifier */
    protected int high;

    /**
     * Constructor with start and end. Order does not matter.
     *
     * @param start One of the range limits
     * @param end The other range limit
     */
    public SimRange (int start, int end) {
       put(start, end);
    }

    /**
     * Return true if the passed Object (SimRange) equals this range.
     * @param obj the reference object with which to compare.
     * @return true if this object is the same as the obj argument; false otherwise.
     */
    public boolean equals (Object obj) {
       return (obj instanceof SimRange) &&
              ((SimRange)obj).low==low &&
              ((SimRange)obj).high==high;    
    }     
         
    /**
     * Return range width, high limit - low limit + 1
     * @return Range width
     */
    public int getWidth () {
       return high - low + 1;
    }

    /**
     * Return low range limit
     * @return Range low limit
     */
    public int getLow () {
       return low;
    }
           
    /**
     * Return high range limit
     * @return Range high limit
     */
    public int getHigh () {
       return high;
    }

   /**
    * Return the hashcode for this range.
    * @return A hashcode value for this object
    */
   public int hashCode () {
      return low+high;
   }
   
    /**
     * Return range as formatted pair
     * @param fmt A printf style format string, or null
     * @return The formatted range
     */
    public String print (String fmt) {
    
       // If no format, use default format..
       if (fmt==null || fmt=="")
          return toString();
	  
       // ..else format low and high values similarly
       else
          return "{"+
	         SimPrintf.sprintf(fmt, low)+
		 ", "+
	         SimPrintf.sprintf(fmt, high)+
		 "}";
    }
        
    /**
     * Copy another SimRange to this
     *
     * @param range The source SimRange
     */
    public void put (SimRange range) {
       low = range.low;
       high = range.high;
    }           

    /**
     * Set this range to a single value
     *
     * @param start The single value for this range
     */
    public void put (int start) {
       low = high = start;
    }
        
    /**
     * Set new limits for this SimRange.
     *
     * @param start One of the range limits
     * @param end The other range limit
     */     
    public void put (int start, int end) {
       low = start<end ? start : end;
       high = start<end? end : start;
    }
        
    /**
     * Return the overall width of an array of SimRanges
     *
     * @param ranges A SimRange[]
     *
     * @return The total width of the ranges
     */
    public static int getWidth (SimRange[] ranges) {
       int w = 0;
       if (ranges != null)
          for (int i=0; i<ranges.length; i++)
             w += ranges[i].getWidth();           
       return w;
    }
    
    /**
     * Print value, default format
     * @return Value in default format
     */
    public String toString () {
       return "{"+low+", "+high+"}";
    }    
}
