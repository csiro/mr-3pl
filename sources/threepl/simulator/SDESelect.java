/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */


package threepl.simulator;

import threepl.codegen.TDE;


/**
 * The SDESelect class implements the TDEType.SELECT TDE in the threepl
 * simulator.
 * 
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
public class SDESelect extends SDE {
    
    // Number of select input pairs
    private int nselects;
    
    // Copy of output value for clear operation on no inputs active
    private SimValue zeroOutput;

    /**
     * Constructor
     * @param sim The single Sim object
     * @param tde Associated TDE
     */
    protected SDESelect(Sim sim, TDE tde) {
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

      // Find the active select and route the corresponding input through.
      // It is assumed only one select input is active, if any. All inputs need
      // to be read to ensure the signal sources are activated each
      // clock cycle.
      boolean done=false;
      for(int i=0; i<nselects; i++) {
          if (getInputBit(i*2, time, force)) {
              putOutput(0, getInputValue((i*2)+1, time, force));
	      done = true;
          }
      }	  
	  
      // If no inputs were active, clear all output bits but let changed
      // status of output reflect whether changed (unlike a simple clear,
      // which always clears the changed status).
      if (!done)
         outputs[0].getValue().put(zeroOutput); 
	 
      oCalc[oIndex] = false;
      return true;     	  
    }

    /** Build variables */
    public void buildVariables() {
        super.buildVariables();
        nselects = inputs.length / 2;
	zeroOutput = outputs[0].getValue().duplicate().clear();
    }
}
