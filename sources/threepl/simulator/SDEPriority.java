/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */


package threepl.simulator;

import threepl.codegen.TDE;


/**
 * The SDEPriority class implements the TDEType.PRIORITY TDE in the threepl
 * simulator.
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
public class SDEPriority extends SDE {
    
   /**
    * Constructor
    * @param sim The single Sim object
    * @param tde Associated TDE
    */
   protected SDEPriority(Sim sim, TDE tde) {
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

      // Get index of highest priority active input
      int active = -1;
      for (int i=0; i<inputs.length; i++) {
         if (getInputBit(i, time, force)) {
	    active = i;
	    break;
         }
      }	 	     
      
      // Set this output if it corresponds to the highest priority
      // active input, else clear it
      putOutput(oIndex, oIndex==active);      

      oCalc[oIndex] = false;
      return true;     	  
   }
}
