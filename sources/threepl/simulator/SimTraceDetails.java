/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */


package threepl.simulator;


/**
 * This class stores trace setup details. It handles trace data logging,
 * retrieval and display via the SimTraceLogger class. SimTraceLogger interacts
 * with the SimTerminal class to handle the display.
 * 
 * @version $Revision: 8254 $
 * @author Andrew Tulloh
 */
public class SimTraceDetails extends SimDetails {

   /**
    * Constructor with clock for timebase
    *
    * @param sim The Sim object
    * @param name Name of this trace
    * @param exprs An array of expression nodes for which to trace values
    */
   public SimTraceDetails (Sim sim, String name, SimExprSimpleNode[] exprs) {
      super(name);
      logger = new SimTraceLogger(sim, name, exprs);

      exprArray = exprs;

      /* Write expression names heading to logfile */
      ((SimTraceLogger)logger).write(getExprNames(0, "\t"));

      /* Initially set trace to start immediately */
      enable(null);
   }

   /**
    * Constructor with (existing) filename
    *
    * @param sim The Sim object
    * @param name Name of this trace
    * @param filename Filename with pre-existing trace data
    */
   public SimTraceDetails (Sim sim, String name, String filename) {
      super(name);
      logger = new SimTraceLogger(sim, name, filename);
   }
   	
   /**
    * Return the (String) type of this
    *
    * @return The (String) type of this ("trace")
    */
   public String getType () {
      return "trace";
   }	  
         
   /**
    * Evaluate and write current expression values to trace log file as a single
    * line String. If any runtime error(s) occur during expression evaluation, the
    * offending result(s) are replaced by the substring "<error>" within the result
    * String, and an exception is thrown after writing the completed line to the log file.
    */
   protected void writeSample (double time) {

      if (exprArray==null)
	 throw new SimException("cannot write to a pre-existing trace");

      SimExprNodeValue nodeValue; 
      String errstr = null;
      String s, line = null;

      // Evaluate each expression and concatenate the result, as a String, to a line of text
      for (int i=0; i<exprArray.length; i++) {
         try {
	    nodeValue = exprArray[i].eval();
	    s = nodeValue.toString();
	    
	 } catch (SimException e) {
	 
	    // Runtime error in expression evaluation
	    s = e.getMessage();
	    
	    // If not a queue unavailable exception, append to errstr
	    if (!(e instanceof SimAvailException))
	       errstr = (errstr==null?"":errstr+", ")+
	          e.getMessage()+" in expression "+exprArray[i].expr();
	 }

         // Append expression string to line
	 line = (line==null?"":line+"\t")+s;
      }	
      
      // Write line to trace log file
      ((SimTraceLogger)logger).write(line);
      
      // Throw an exception if evaluation error(s) occurred
      if (errstr!=null)
         throw new SimException(errstr);
   }

   /**
    * Show the trace data in a terminal emulation window
    *
    * @param n An index used to offset the initial window position
    */
   public void show (int n) {
      logger.show("3PL Simulator - "+name, n);
   }

   /** Close the trace logfile and display window */
   public void close () {
      logger.close();
   } 
}      
