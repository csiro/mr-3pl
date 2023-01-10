/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */


package threepl.simulator;


/**
 * This is a base class for SimTraceDetails and SimPlotDetails.
 * It contains a reference to the SimLogger associated with the trace or plot
 * (SimLogger handles data storage and retrieval); also contains the
 * enable expression for the trace or plot, and the associated enabled flag;
 * also the array of trace or plot expressions. Note that the enable related
 * fields and the expression array are not used for a pre-existing trace or
 * plot (ie one which already existed and is being reviewed, not currently
 * created).
 * 
 * @version $Revision: 8254 $
 * @author Andrew Tulloh
 */
public abstract class SimDetails {

   /** SimLogger associated with this */
   protected SimLogger logger;

   /** Name of this details */
   protected String name;

   /** Boolean expression for when details should be enabled, or null for immediately */
   protected SimExprSimpleNode enableExpr;

   /** Array of expression nodes from map, for runtime efficiency */
   protected SimExprSimpleNode[] exprArray;

   /** Flag indicating when details enabled */
   protected boolean enabled;

   /**
    * Constructor with name
    *
    * @param name Name of this details
    */
   protected SimDetails (String name) {
      this.name = name;
   }

   /**
    * Return the (String) type of this details
    *
    * @return The (String) type of this details
    */
   public abstract String getType ();
       	                             
   /**
    * Return the name
    *
    * @return Name of this details
    */
   public String getName () {
      return logger.getName();
   }

   /**
    * Get log filename
    *
    * @return The log filename used to store data for this details
    */
   public String getFilename () {
      return logger.getFilename();
   }

   /**
    * Return true if the currently open filename corresponds to the passed name
    *
    * @return True if the currently open filename corresponds to the passed name
    */
   public boolean hasFilename (String name) {
      return logger.hasFilename(name);   
   }
                                
   /**
    * Return true if details is pre-existing, ie details data comes from an existing file 
    *
    * @return True if details is pre-existing, ie details data comes from an existing file     
    */
   public boolean isNew () {
      return logger.isNew();
   }
       	  	
   /**
    * Get (concatenated) expression names. Resulting String is abbreviated if length
    * would be greater than the non-zero desiredWidth argument.
    *
    * @param desiredWidth Maximum length of returned String, or 0 for no limit
    * @param delimiter The delimiter String to use between expression names, or null for default (" ")
    *
    * @return The expression names for this trace, concatenated in a single String
    */
   public String getExprNames (int desiredWidth, String delimiter) {

      if (delimiter==null)
         delimiter = " ";
	 
      String s;
      
      /* Concatenate expression names, with delimiter between */
      String[] exprNames = logger.getExprNames();
      s = exprNames[0];
      for (int i=1; i<exprNames.length; i++)
         s += delimiter+exprNames[i];

      /* Abbreviate if necessary to fit within desired field width */
      if (desiredWidth!=0 && s.length()>desiredWidth) {
         int len = s.length();
	 s = s.substring(0, desiredWidth-10)+".. .."+s.substring(len-5, len);	     
      }	     	  	 
   
      return s;
   }

   /**
    * Get enable expression
    *
    * @return The (root) SimExprSimpleNode of the enable expression
    */
   public SimExprSimpleNode getEnableExpr () {
      return enableExpr;
   }
                       
   /**
    * Get current enabled state
    *
    * @return True if currently enabled
    */
   public boolean getEnableState () {
      return enabled;
   }   

   /**
    * Register the details enable condition expression. If expr is null,
    * the details is enabled immediately.
    *
    * @param expr The expression for when to enable the details, when true
    */
   public void enable (SimExprSimpleNode expr) {

      if (!isNew())
	 throw new SimException("cannot enable a pre-existing "+getType());

       this.enableExpr = expr;
       testEnableTrigger();	 
   }

   /** Disable the details */
   public void disable () {

      if (!isNew())
	 throw new SimException("cannot disable a pre-existing "+getType());

       enabled = false;
   }

   /**
    * Do details increment at clock edge for a non-preexisting trace or plot
    * This involves possibly enabling the trace or plot, and writing the latest
    * sample of expression results to the log file.
    */
   public void next (double time) {

      /* No action if this details is a pre-existing one */
      if (!isNew())
	 return;

      /* Enable if time */
      testEnableTrigger();

      /* Continue if enabled, write current values to logfile */
      if (enabled)
	 writeSample(time);
   }

   /**
    * Write the current expression values to file
    */
   protected abstract void writeSample (double time);       	

  /**
   * Show the details data in a window
   *
   * @param n An index used to offset the initial window position
   */
   public abstract void show (int n);

   /** Close the details */
   public abstract void close ();

   /* Test the enable expression and enable the details if the result is true */   
   private void testEnableTrigger () {
      try {
         if (!enabled && (enableExpr==null || enableExpr.eval().getBoolean()))
            enabled = true;
	    
      } catch (SimAvailException e) {
      
         // If queue unavailable in enable expression, don't enable and don't report
	 	    
      } catch (SimException e) {
      
         // Report other runtime evaluation errors
	 throw new SimException(e.getMessage()+" in "+getType()+" "+name+" on expression");
      }	       
   }
   
}      
