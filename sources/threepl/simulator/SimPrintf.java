/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */


package threepl.simulator;



/**
 * SimPrintf is a wrapper class for the PrintfFormat class.
 * It provides methods to return a formatted String for values
 * of type double, long, int and Object. Integer type conversion
 * characters are allowed with double values. Exceptions
 * thrown by the PrintfFormat class are converted here to
 * SimExceptions.
 * 
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
@SuppressWarnings("all")
public class SimPrintf  {
    /**
     * Unique identifier string for this class and build used to help
     * debugging and binary tracking
     */
    private String _class_cvs_id = "$id$";

   /**
    * Check for a well formed format
    *
    * @param fmt A printf style format specification
    * @throws SimException If format spec is not well formed
    */
   public static void check (String fmt) {
      try {
         new PrintfFormat(fmt).sprintf();
      } catch (IllegalArgumentException e) {
         throw new SimException(e);
      }	 
   }
   
   /**
    * Format a double.
    * Allow integer type fmt, ie %d, i, x, X, o, c, C, b.
    * (Risk of truncation for very large values)
    *
    * @param fmt A printf style format specification
    * @param val A double value to be formatted
    * @throws SimException If format spec is not well formed
    *
    * @return The formatted String
    */
   public static String sprintf (String fmt, double val) {
      if (fmt.matches(".*[dixXocCb].*"))
         return sprintf(fmt, (long)val);
      else try {	 
         return new PrintfFormat(fmt).sprintf(val);
      } catch (IllegalArgumentException e) {
         throw new SimException(e);
      }	 
   }

   /**
    * Format a long
    *
    * @param fmt A printf style format specification
    * @param val A long value to be formatted
    * @throws SimException If format spec is not well formed
    *
    * @return The formatted String
    */
   public static String sprintf (String fmt, long val) {
      try {
         return new PrintfFormat(fmt).sprintf(val);
      } catch (IllegalArgumentException e) {
         throw new SimException(e);
      }	 
   }
   
   /**
    * Format an int
    *
    * @param fmt A printf style format specification
    * @param val An int value to be formatted
    * @throws SimException If format spec is not well formed
    *
    * @return The formatted String
    */
   public static String sprintf (String fmt, int val) {
      try {
         return new PrintfFormat(fmt).sprintf(val);
      } catch (IllegalArgumentException e) {
         throw new SimException(e);
      }	 
   }
   
   /**
    * Format an Object
    *
    * @param fmt A printf style format specification
    * @param val An Object to be formatted
    * @throws SimException If format spec is not well formed
    *
    * @return The formatted String
    */
   public static String sprintf (String fmt, Object val) {
      try {
         return new PrintfFormat(fmt).sprintf(val);
      } catch (IllegalArgumentException e) {
         throw new SimException(e);
      }	 
   }
   
}
