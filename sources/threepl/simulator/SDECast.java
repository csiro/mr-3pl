/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */


package threepl.simulator;

import threepl.codegen.TDE;


/**
 * The SDECast class implements the cast form of the TDEType.CONNECT TDE in
 * the threepl simulator.
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
public class SDECast extends SDE {

    // Sign extension flag. If true, sign extension is done during padding
    // else zero padding is done.
    private boolean sext;

    /**
     * Constructor
     * @param sim The single Sim object
     * @param tde Associated TDE
     */
    protected SDECast(Sim sim, TDE tde) {
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
       outputs[oIndex].getValue().cast(getInputValue(0, time, force), sext);
       oCalc[oIndex] = false;
       return true;
    }
    
    /** Build variables */
    public void buildVariables() {
       super.buildVariables();

       // Expect 1 param
       if (params.size()>0)
          sext = ((Boolean)getParam(0)).booleanValue();         
    }       
}
