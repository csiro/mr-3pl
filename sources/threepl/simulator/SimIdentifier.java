/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */


package threepl.simulator;



/**
 * This interface is used to access common properties of SimVariables and SimClocks.
 * Objects in both these classes contain a value, can be read or written and can be
 * tested for changed status. The value or details about the Object can be printed.
 * 
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
public interface SimIdentifier {

   /** Clear changed since last simulator cycle */
   public void clearChanged ();
   
   /**
    * Return (boolean) value
    * @return The value as a boolean
    */
   public boolean getBit ();
       
   /**
    * Return (double) value
    * @return The value as a double
    */
   public double getDouble ();
       
   /**
    * Return the id (name)
    * @return Identifier name
    */
   public String getId ();
       
   /**
    * Return true if this is a SimClock
    * @return True if this is a SimClock
    */
   public boolean isClock();    

   /**
    * Return true if this is a SimVariable
    * @return True if this is a SimVariable
    */
   public boolean isVariable();
   
   /**
    * Return formatted String representation
    * @param fmt is a printf style format specification
    * @return The formatted String representation of the value
    */
   public String print (String fmt);

   /**
    * Return String representation of type
    * @return String representation of type
    */
   public String printType();
                  
   /**
    * Write a double to the value
    * @param fval The value to write
    */
   public void put (double fval);
   
   /**
    * Return default String representation
    * @return Default String representation of the value
    */           
   public String toString ();
   
   /**
    * Return whether changed since last simulator cycle
    * @return Whether changed since last simulator cycle
    */
   public boolean wasChanged ();    
}
