/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */


package threepl.simulator;



/**
 * This interface is used to access common methods of SimGui and SimCli, the
 * two user interface modes to the simulator.
 * 
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
public interface SimUserInt {

   /**
    * Return true if this is a SimGui
    *
    * @return True is this is a SimGui
    */
   public boolean isGui ();
       
   /**
    * Return true if this is a SimCli
    *
    * @return True is this is a SimCli
    */
   public boolean isCli ();

   /**
    * Print a string
    *
    * @param s The String to print
    */

   public void print (String s);

   /**
    * Print a string with appended newline
    *
    * @param s The String to print
    */
   public void println (String s);
   
   /**
    * Print an error string to standard error
    *
    * @param s The String to print
    */
   public void errorOut (String s);
   
   /**
    * Print an error string with appended newline
    *
    * @param s The String to print
    */
   public void errorOutLn (String s);  
   
   /** Close user interface */
   public void close ();   
   
   /** Print a prompt */
   public void prompt ();         
}
