/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */


package threepl.simulator;

import threepl.codegen.TDE;


/**
 * The SDEWhen class implements the TDEType.WHEN TDE in the threepl
 * simulator.
 * 
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
public class SDEWhen extends SDE {

    /**
     * Constructor
     * @param sim The single Sim object
     * @param tde Associated TDE
     */
    protected SDEWhen(Sim sim, TDE tde) {
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

      // Note: if finish output is null then inputs finisht and finishf are null.
      // Otherwise all three are non-null.
      
      // start_del = input(0)
      // test      = input(1)
      // finisht   = input(2)  may be null
      // finishf   = input(3)  may be null
      // startt    = output(0)
      // startf    = output(1)
      // finish    = output(2) may be nulll
      //
      // startt = start_del && test
      // startf = start_del && !test
      // if (finish!=null)
      //    finish = finisht || finishf

      switch (oIndex) {
      case 0:
	 putOutput(0, getInputBit(0, time, force) && getInputBit(1, time, force));
	 break;
      case 1:
	 putOutput(1,  getInputBit(0, time, force) && !getInputBit(1, time, force));
	 break;
      case 2:
         if (outputConnected[2])
	    putOutput(2, getInputBit(2, time, force) || getInputBit(3, time, force));
         break;
      }	 	    
      oCalc[oIndex] = false;
      return true;     	  
   }
}
