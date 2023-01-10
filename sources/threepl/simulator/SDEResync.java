/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */


package threepl.simulator;

import threepl.codegen.TDE;


/**
 * The SDEResync class implements the TDEType.RESYNC TDE in the threepl
 * simulator.
 * 
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
public class SDEResync extends SDE {

   // Input buffer
   private boolean in;
   
   // Clock enable, first input flip flop
   private boolean ce;
   
   // Output, first input flip flop
   private boolean cl;
   
   // Output, second input flip flop
   private boolean block;
   
   // Output, first output flip flop
   private boolean clr1;
   
   // Output, second output flip flop
   private boolean clr1_del;
   
   /**
    * General constructor
    * @param sim The single Sim object
    * @param tde Associated TDE
    */
   protected SDEResync(Sim sim, TDE tde) { 
      super(sim, tde);
   }

   /**
    * Do combinatorial behaviour
    * @param oIndex Index of the output to calculate
    * @param time The current simulator time
    * @param force True if behaviour should happen regardless of time
    * @return True (indicates behaviour is done for this output)
    */
   public boolean behaviour(int oIndex, double time, boolean force) {
    
      // If already done for this time, no action..
      if (super.behaviour(oIndex, time, force))
         return true;

      // ..else do output behaviour
      oCalc[oIndex] = true;

      // Handle asyncronous reset flip flop inputs
      if (clr1_del)
         cl = false;
      if (!cl)
         clr1 = clr1_del = false;
	 	 
      // Set output
      putOutput(0, clr1);

      oCalc[oIndex] = false;
      return true;     	  
   }

   /**
    * Do clock edges
    * @param index An index to the clock to use. 
    */
   public void clock (int index) {

      // If input clock edge..
      if (index==0) {
         cl = clr1_del ? false : (ce ? true : cl);
	 block = in;	 
     }

      // ..else output clock edge
      else {
         clr1 = cl ? !clr1 : false;
	 clr1_del = cl? clr1 : false;
      }	     	     
   }

    /**
     * Return the clock attached to this SDE which is associated with the index'th
     * input.
     * @return The clock
     */
    public SimClock getInputClock (int index) {
       return (SimClock)inputs[2];
    }       
              
    /**
     * Return the clock attached to this SDE which is associated with the index'th
     * output.
     * @return The clock
     */
    public SimClock getOutputClock (int index) {
       return (SimClock)inputs[2];
    }       
              
    /**
    * Determine input value in preparation for next input clock
    * @param time The current simulator time
    * @param force True if behaviour should happen regardless of time
    */
   public void inputBehaviour(double time, boolean force) {
      in = getInputBit(0, time, force);   
      ce = in && !block;
   }

   /** Reset */
   public void reset() {
      super.reset();
      cl = false;
      block = false;
      clr1 = false;
      clr1_del = false;   
   }
}
