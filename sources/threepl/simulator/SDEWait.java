/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */


package threepl.simulator;

import threepl.codegen.TDE;


/**
 * The SDEWait class implements the TDEType.WAIT TDE in the threepl
 * simulator.
 * 
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
public class SDEWait extends SDE {

    // The number of "finish" signal inputs
    private int nfinish;

    // Flip-flop input values
    private boolean[] d;
    
    // Flip-flop output values
    private boolean[] q;
    
    // Copy of output
    private boolean finish;

    // Flag indicating presence of reset input
    private boolean resetConnected;
    
    // Value of reset input prior to clock, if present
    private boolean resetInput;
    
    /**
     * Constructor
     * @param sim The single Sim object
     * @param tde Associated TDE
     */
    protected SDEWait(Sim sim, TDE tde) {
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
      inputBehaviour(time, force);
      putOutput(0, finish);
      oCalc[oIndex] = false;
      return true;     	  
    }

    /** Build variables */
    public void buildVariables() {
        super.buildVariables();
        nfinish = inputs.length - 2;
        q = new boolean[nfinish];
        d = new boolean[nfinish];
	resetConnected = inputConnected[1];
    }


    /**
     * Do clock edge. Reset input takes precedence if present.
     * @param index An index to the clock to use, ignored for SDEWait. 
     */
    public void clock(int index) {
        if ((resetConnected && resetInput) || finish)
	   reset();
	else   
           for(int i = 0; i < nfinish; i++)
	      q[i] = d[i];
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
     * Required for SDEWait because its flip flop d inputs
     * depend on the module ack inputs. If a user manually changes
     * an SDE input between clock steps, this method ensures
     * the new value(s) are transmitted to the d's before the next
     * clock edge.
     * @param time The current simulator time
     * @param force True if behaviour should happen regardless of time
     */
   public void inputBehaviour (double time, boolean force) {
   
      // Get flip flop d inputs
      for(int i=0; i<nfinish; i++)
          d[i] = getInputBit(2+i, time, force) || q[i];

      // Get finish signal
      finish = true;
      for(int i=0; i<nfinish && finish; i++)
          finish &= d[i];
	  
      // If reset input present, get value for flip flop reset inputs
      if (resetConnected)
         resetInput = getInputBit(1, time, force);
   }
   

    /** Reset */
    public void reset() {
        super.reset();
        for(int i = 0; i < nfinish; i++)
            q[i] = false;
    }
}
