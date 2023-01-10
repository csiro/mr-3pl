/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */


package threepl.simulator;


/**
 * This class stores plot setup details. It handles plot data logging,
 * retrieval and display via the SimPlotLogger class. SimPlotLogger interacts
 * with the SimPlotWindow class to handle the display.
 * 
 * @version $Revision: 8254 $
 * @author Andrew Tulloh
 */
public class SimPlotDetails extends SimDetails implements SimTypes {

   /* The SimSample which contains the expressions to be plotted */
   private SimSample sample;
   
   // Array of boolean values indicating when an associated expression
   // in exprs[] has just had an active clock edge
   private SimExprNodeValue[] clockEdges;
   
   /**
    * Constructor with clock for timebase
    *
    * @param sim The Sim object
    * @param name Name of this plot
    * @param exprs An array of expression nodes for which to plot values
    */
   public SimPlotDetails (Sim sim, String name, SimExprSimpleNode[] exprs) {
      super(name);                       

      // Check all expressions (except first, time ordinate) are primitive values
      boolean err = false;
      StringBuffer b = new StringBuffer();
      for (int i=1; i<exprs.length; i++) {
         if (!exprs[i].nodeValue.isPrimitive()) {
	    b.append(
	       (err?"\n":"")+"expression "+exprs[i].expr()+" is not a primitive value");
            err = true;
         }	    
      }
      if (err)
         throw new SimException(b.toString()+", plot \""+name+"\"");
      
      // Create an array of boolean values corresponding to the expressions in exprs[],
      // except for the first time expression. A value in this second array will be true if
      // the corresponding expression in the first array is associated with a clock and that
      // clock has just had an active edge.
      clockEdges = new SimExprNodeValue[exprs.length-1];
      for (int i=0; i<clockEdges.length; i++)
         clockEdges[i] = new SimExprNodeValue(LOG);
      
      exprArray = exprs;
      logger = new SimPlotLogger(sim, name, exprs);
      sample = new SimSample(exprArray, clockEdges);

      // Set plot to start immediately (enable feature not otherwise used in plotting)
      enable(null);	 
   }

   /**
    * Constructor with (existing) filename
    *
    * @param sim The Sim object
    * @param name Name of this plot
    * @param filename Filename with pre-existing plot data
    */
   public SimPlotDetails (Sim sim, String name, String filename) {
      super(name);
      logger = new SimPlotLogger(sim, name, filename);
   }
   	        
   /**
    * Return the (String) type of this
    *
    * @return The (String) type of this ("plot")
    */
   public String getType () {
      return "plot";
   }	  

                                
   /**
    * Evaluate and write current expression values to plot log file as a single
    * SimSample. If a runtime error occurs during expression evaluation, or the
    * result is multivalued (an array expression which results in a non-single
    * value), then mark that result within the sample as invalid before writing
    * the sample to the logfile. 
    */
   protected void writeSample (double time) {

      if (exprArray==null)
	 throw new SimException("cannot write to a pre-existing plot");

      boolean err = false;
      String errstr = null;
      SimClock clock;
      
      // Write time to first slot in sample
      sample.put(0, time);
      
       // Evaluate all expressions (except first, time ordinate) in the sample. Catch any runtime
       // expression errors.
      for (int i=1; i<exprArray.length; i++) {

	 // If expression evaluation fails it can't be plotted. However we still need to
	 // write this sample to the log file so random access by sample index always works.
	 // We mark this expression result as invalid so it can be specially handled during plotting.
         try {
	    exprArray[i].eval();
	    
         } catch (SimException e) {
	 
	    // Set sample invalid for plot
	    err = true;
	    sample.setValid(i, false);
	    
	    // If not a queue unavailable exception, append message to errstr	 
            if (!(e instanceof SimAvailException))
	       errstr = (errstr==null?"":errstr+", ")+
	          e.getMessage()+" in expression "+exprArray[i].expr();
         }	 

	 // Evaluate the clockEdges for this expression too, whether or not sample is valid.
	 // Set true if the expression has an associated clock which has an active edge
	 // at this time, otherwise set false.
	 if ((clock = exprArray[i].getAssociatedClock()) != null)
	    clockEdges[i-1].put(clock.hasActiveEdge());
         else
	    clockEdges[i-1].put(false);	    	    
      }
      
      // Always write sample (of same size) to file, otherwise random access won't work
      ((SimPlotLogger)logger).write(sample);

      // If had any errors, restore original sample for next time
      if (err)
         sample = new SimSample(exprArray, clockEdges);
      
      // Report runtime errors (not queue unavailable)  
      if (errstr!=null) {
         throw new SimException(errstr);   
      }	   
   }

   /**
    * Show the plot data in a window
    *
    * @param n An index used to offset the initial window position
    */
   public void show (int n) {
      logger.show("3PL Simulator - "+name, n);
   }

   /** Close the plot logfile and display window */
   public void close () {
      logger.close();
   } 
}      
