/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */

package threepl.simulator;



/**
 * A SimException is the base class for most exceptions
 * used in the simulator. It is used for most runtime
 * errors.
 */
public class SimException extends RuntimeException {

   /** The message String */
   private String message;
   
   /**
    * Default constructor
    */
   public SimException() {
      super();
   }
   
   /**
    * Constructor with a Throwable which caused this exception
    *
    * @param e Throwable which caused this exception
    */
   public SimException (Throwable e) {
      super(e);
      message = super.getMessage();
   }

   /**
    * Constructor with message
    *
    * @param mess The message to associate with this exception
    */
   public SimException(String mess) {
      super(mess);
      message = super.getMessage();
   }

   /**
    * Get the message
    *
    * @return The exception message
    */
   public String getMessage () {
      return message;
   }
                 
   /**
    * Append another Throwable's message to this message.
    * Newline is inserted before the new message component.
    *
    * @param e The Throwable from which to take the message to add to this
    */
   public void append (Throwable e) {
      append(e.getMessage());
   }    
   
   /**
    * Append a String to this message.
    * Newline is inserted before the new String.
    *
    * @param s The String to append
    */
   public void append (String s) {
      if (message==null)
         message = s;
      else if (s!=null)
         message += "\n"+s;
   }
                  
}
