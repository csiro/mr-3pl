/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */


package threepl.simulator;

import threepl.codegen.TDE;


/**
 * The SDEExecp class implements the TDEType.EXECP TDE in the threepl
 * simulator.
 * 
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
public class SDEExecp extends SDE {
    
    // Copy of flip flop output
    private boolean q;

    // Start input, also clock enable flip flop input
    private boolean start;

    // Avail signal, also synchronous reset flip flop input
    // and inverted flip flop d input
    private boolean avail;
    
    // priority output signal
    private boolean priout;
    
    // Flag indicating presence of avail input
    private boolean availConnected;
    
    // Flag indicating presence of priority input and output signals
    private boolean priConnected;
    
    // Flag indicating presence of reset input
    private boolean resetConnected;
    
    // Value of reset input prior to clock, if present
    private boolean resetInput;
    
    /**
     * Constructor
     * @param sim The single Sim object
     * @param tde Associated TDE
     */
    protected SDEExecp(Sim sim, TDE tde) {
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
       
       // Get inputs
       inputBehaviour(time, force); 
       
       // Set output
       if (priConnected) {
           putOutput(0, priout);
           if (availConnected)
               putOutput(1, (start || q) && avail);
           else
               putOutput(1, start || q);
       } else
           putOutput(0, (start || q) && avail);
        putOutput(2, start || q);

       oCalc[oIndex] = false;
       return true;     	  
    }


    /** Build variables */
    public void buildVariables() {
        super.buildVariables();
        availConnected = inputConnected[2];
        priConnected = inputConnected[3];     
        resetConnected = inputConnected[4];	   
    }

    /**
     * Do clock edge
     * @param index An index to the clock to use, ignored for SDEExecp. 
     */
    public void clock(int index) {
    
        // If reset active, or avail input set, reset..
	if ((resetConnected && resetInput) || (priConnected && priout) || (availConnected && avail))
	   reset();
	   	
	// ..else clock 1 into flip flop if clock enabled
	else if (start)
           q = true;
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
       start = getInputBit(1, time, force);
       if (availConnected)
           avail = getInputBit(2, time, force);
       if (priConnected)
            priout = getInputBit(3, time, force);
       if (resetConnected)
          resetInput = getInputBit(4, time, force);
   }
   
    /** Reset */
    public void reset() {
        super.reset();
        q = false;
    }
}
