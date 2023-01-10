/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */


package threepl.simulator;


/**
 * This is a base class for SimTraceLogger and SimPlotLogger.
 * It contains the array of plot expressions; note this is not used for a pre-existing
 * plot (ie one which already existed and is being reviewed, not currently
 * created).
 * 
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
public abstract class SimLogger {

   /** The (single) Sim object */
   protected Sim sim;

   /** The logger name */
   protected String name;
      
   /** The logfile name */
   protected String filename;

   /** The expression names */
   protected String[] exprNames;
      
   /** Indicator for this logger has a new file (not preexisting) */
   protected boolean isNew=false;
   
   /**
    * Constructor with Sim and logger name
    *
    * @param sim The Sim object
    * @param name The logger name
    */      
   protected SimLogger (Sim sim, String name) {
      this.sim = sim;      
      this.name = name;      
   }	

   /**
    * Constructor with Sim, logger name and expressions
    *
    * @param sim The Sim object
    * @param name The logger name
    * @param exprs An array of expressions to be logged
    */      
   protected SimLogger (Sim sim, String name, SimExprSimpleNode[] exprs) {
      this(sim, name);
      
      /* Build array of expression names */
      if (exprs!=null) {
	 exprNames = new String[exprs.length]; 
         for (int i=0; i<exprNames.length; i++)
	    exprNames[i] = exprs[i].expr();
      }
      isNew=true;
   }	

   /** Close logger and the display window */
   public abstract void close ();
   
   /**
    * Get name associated with this logger 
    *
    * @return Name of this logger
    */
   public String getName () {
      return name;
   }
         
   /**
    * Get log filename associated with this logger 
    *
    * @return The log filename used to store data for this logger
    */
   public String getFilename () {
      return filename;
   }
         
   /**
    * Get expression names
    *
    * @return The concatenated expression names, as a single String
    */
   public String[] getExprNames () {
      return exprNames;
   }    

   /**
    * Convert name to filename with path. No change if name contains "/".  
    *
    * @return Full pathname to log file, based on name
    */
   protected abstract String nameToFilename (String name);

   /**
    * Return true if the currently open filename corresponds to the passed name
    *
    * @return True if the currently open filename corresponds to the passed name
    */
   public boolean hasFilename (String name) {
      if (nameToFilename(name).equals(filename))
         return true;
      return false;	 
   }

   /**
    * Return true if log file is pre-existing 
    *
    * @return True if log file is pre-existing     
    */
   public boolean isNew() {
      return isNew;
   }

  /**
   * Display the logger data in a window
   *
   * @param n An index used to offset the initial window position
   */
   public abstract void show (String title, int n);
}      
