/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */


package threepl.simulator;

import threepl.codegen.TDE;


/**
 * The SDEAnd class implements the TDEType.AND TDE in the threepl
 * simulator.
 * 
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
public class SDEAnd extends SDE {

   /**
    * Constructor
    * @param sim The single Sim object
    * @param tde Associated TDE
    */
   protected SDEAnd(Sim sim, TDE tde) {
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

      // AND the inputs
      boolean q = getInputBit(0, time, force);
      for (int i=1; i<inputs.length; i++)
         q &= getInputBit(i, time, force);

      // Write to output
      putOutput(0, q);

      oCalc[oIndex] = false;
      return true;     	  
   }
}
