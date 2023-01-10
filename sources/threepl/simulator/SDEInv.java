/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */


package threepl.simulator;

import threepl.codegen.TDE;


/**
 * The SDEInv class implements the TDEType.INV TDE in the threepl simulator.
 * 
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
public class SDEInv extends SDE {

   // Copy of input to invert
   private SimValue inv;
   
   /**
    * Constructor
    * @param sim The single Sim object
    * @param tde Associated TDE
    */
   protected SDEInv(Sim sim, TDE tde) {
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

      // Invert the input, write to output
      inv.put(getInputValue(0, time, force));
      inv.inv();
      putOutput(0, inv);

      oCalc[oIndex] = false;
      return true;     	  
   }

   /** Build variables */
   public void buildVariables() {
      super.buildVariables();
      
      inv = ((SimVariable)inputs[0]).getValue().duplicate();
   }      

}
