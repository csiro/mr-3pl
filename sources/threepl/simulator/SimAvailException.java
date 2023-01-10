/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */

package threepl.simulator;



/**
 * A SimAvailException is created when an unavailable queue variable
 * is accessed during an expression evaluation.
 * 
 * @version $Revision: 8254 $
 * @author Andrew Tulloh
 */
public class SimAvailException extends SimException {

   /** Default constructor */
   public SimAvailException () {
      super();
   }
                
   /**
    * Constructor with message
    * @param mess The message to associate with this exception
    */
   public SimAvailException(String mess) {
      super(mess);
   }

   /**
    * Return the exception message if non-null, else return "unavailable".
    * @return The exception message String
    */
   public String getMessage() { 
      String mess;
      if ((mess = super.getMessage())!=null)
         return mess;
      else
         return "unavailable";
   }   
}
