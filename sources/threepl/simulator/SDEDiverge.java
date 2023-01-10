/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */


package threepl.simulator;

import threepl.codegen.TDE;


/**
 * The SDEDiverge class implements the TDEType.DIVERGE TDE in the threepl
 * simulator.
 * @version $Revision: 8254 $
 * @author Andrew Tulloh
 */
public class SDEDiverge extends SDE {

   // The number of dest modules
   private int nmodules;

   // Copy of src POP output
   private boolean popSrc;

   // Flip-flop input values
   private boolean[] d;

   // Flip-flop output values
   private boolean[] q;

   // Copy of reset input
   private boolean r;
    
   /**
    * Constructor
    * @param sim The single Sim object
    * @param tde Associated TDE
    */
   protected SDEDiverge(Sim sim, TDE tde) {
      super(sim, tde);
   }

   /**
    * Do combinatorial behaviour
    * @param oIndex Index of the output to calculate
    * @param time The current simulator time
    * @return True (indicates behaviour is done for this output)
    * @param force True if behaviour should happen regardless of time
    */
   public boolean behaviour(int oIndex, double time, boolean force) {
    
      // If already done for this time, no action..
      if (super.behaviour(oIndex, time, force))
         return true;

      // ..else do output behaviour
      oCalc[oIndex] = true;

      // If only one dest module, connect src and dest POP and NE
      // signals directly..
      if (nmodules==1) {
      
         switch (oIndex) {
	 case 0:
	    putOutput(0, getInputBit(3, time, force));
	    break;
	 case 1:
	    putOutput(1, getInputBit(2, time, force));
            break;
         }	    
      }

      // ..else connect single src to multiple dests
      else {	   	   

         // If this is the src queue ack output..
         if (oIndex==0) {

            // Update flip flop d inputs
	    inputBehaviour(time, force);
	    	 
	    // Generate src queue ack output
	    popSrc = true;
	    for (int i=0; i<nmodules; i++)
	       popSrc = d[i] && popSrc;
	    putOutput(0, popSrc);
         }
	 
	 // ..else generate appropriate dst module av output
         else
            putOutput(oIndex, getInputBit(2, time, force) && !q[oIndex-1]);	    
      }
      
       oCalc[oIndex] = false;
       return true;     	  
   }

   /** Build variables */
   public void buildVariables() {
      super.buildVariables();
      nmodules = inputs.length - 3;
      q = new boolean[nmodules];
      d = new boolean[nmodules];
   }

   /**
    * Do clock edge
    * @param index An index to the clock to use, ignored for SDEDiverge. 
    */
   public void clock(int index) {

      // Only have state behaviour if more than one dest module
      if (nmodules>1)
       for(int i=0; i<nmodules; i++)
	  // If reset input or popSrc true, synchronous reset on all flip flops
	  q[i] = (r || popSrc) ? false : d[i];
   }

    /**
     * Return the clock attached to this SDE which is associated with the index'th
     * input.
     * @return The clock
     */
    public SimClock getInputClock (int index) {
       return (SimClock)inputs[0];
    }       
              
    /**
     * Return the clock attached to this SDE which is associated with the index'th
     * output.
     * @return The clock
     */
    public SimClock getOutputClock (int index) {
       return (SimClock)inputs[0];
    }       
              
    /**
     * Determine input values in preparation for next clock.
     * @param time The current simulator time
     * @param force True if behaviour should happen regardless of time
     */
   public void inputBehaviour (double time, boolean force) {
      if (nmodules>1) {
         for (int i=0; i<nmodules; i++)
            d[i] = getInputBit(i+3, time, force) || q[i];
         r = getInputBit(1, time, force);	    
      }	 
   }
   
   /** Reset */
   public void reset() {
      super.reset();
      for(int i=0; i<nmodules; i++)
         q[i] = false;
   }
}
