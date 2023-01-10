/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */


package threepl.simulator;

import threepl.codegen.TDE;


/**
 * The SDEDecode class implements the TDEType.DECODE TDE in the threepl
 * simulator.
 * 
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
@SuppressWarnings("all")
public class SDEDecode extends SDE {
    
    // Constant value which must be matched by input variable
    private SimValue val;

    /**
     * Constructor
     * @param sim The single Sim object
     * @param tde Associated TDE
     */
    protected SDEDecode(Sim sim, TDE tde) {
        super(sim, tde);
    }

    /**
     * Do combinatorial behaviour
     * @param oIndex Index of the output to calculate
     * @param time The current simulator time
     * @param force True if behaviour should happen regardless of time
     * @return True if behaviour was done
     */
    public boolean behaviour(int oIndex, double time, boolean force) {

       // If already done for this time, no action..
       if (super.behaviour(oIndex, time, force))
          return true;

       // ..else do output behaviour
       oCalc[oIndex] = true;
       putOutput(0, getInputValue(0, time, force).eq(val));
       oCalc[oIndex] = false;
       return true;     	  
    }


    /** Build variables */
    public void buildVariables() {
        super.buildVariables();
        val = (SimValue)((SimVariable)inputs[0]).getValue().duplicate();

        // ***NOTE*** SimValue.put(double) is called here.
	// Will be fixed later if and when params are stored as hex strings.
        if(params.size() > 0)
            val.put(((Long) getParam(0)).longValue());
    }
}
