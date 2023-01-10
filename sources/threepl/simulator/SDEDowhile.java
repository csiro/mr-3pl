/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */


package threepl.simulator;

import threepl.codegen.TDE;


/**
 * The SDEDowhile class implements the TDEType.DOWHILE TDE in the threepl
 * simulator.
 * 
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
public class SDEDowhile extends SDE {

    // State variables
    private boolean d, q;

    // Flag indicating presence of reset input
    private boolean resetConnected;
    
    // Value of reset input prior to clock, if present
    private boolean resetInput;
    
    /**
     * Constructor
     * @param sim The single Sim object
     * @param tde Associated TDE
     */
    protected SDEDowhile(Sim sim, TDE tde) {
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
       	  	
       // start	    = input(0)
       // test	    = input(1)
       // contin    = input(2)
       // clock     = input(3)
       // reset     = input(4) - optional
       //
       // startb      = output(0)
       // finish      = output(1)
       //
       // startb = start || (test && contin)
       // finish = q (=d = !test && contin)
       //
       switch (oIndex) {
       case 0:
          putOutput(0, getInputBit(0, time, force) || 
	               (getInputBit(1, time, force) && getInputBit(2, time, force)));
          break;
       case 1:
          putOutput(1, q);
	  break;
       }

       oCalc[oIndex] = false;
       return true;     	  
    }
    
    /** Build variables */
    public void buildVariables() {
        super.buildVariables();
        resetConnected = inputs.length>4 && inputConnected[4];	   
    }

    /**
     * Do clock edge. Reset input takes precedence if present.
     * @param index An index to the clock to use (ignored)
     */
    public void clock (int index) {
       if (resetConnected && resetInput)
          reset();
       else	  
          q = d;
    }    

    /**
     * Return the clock attached to this SDE which is associated with the index'th
     * input.
     * @return The clock
     */
    public SimClock getInputClock (int index) {
       return (SimClock)inputs[3];
    }       
              
    /**
     * Return the clock attached to this SDE which is associated with the index'th
     * output.
     * @return The clock
     */
    public SimClock getOutputClock (int index) {
       return (SimClock)inputs[3];
    }       
              
    /**
     * Determine input values in preparation for next clock.
     * Required for SDEDowhile because its flip flop d input
     * depends on inputs. If a user manually changes
     * an input between clock steps, this method ensures
     * the new value(s) are transmitted to the d before the next
     * clock edge.
     * @param time The current simulator time
     * @param force True if behaviour should happen regardless of time
     */
   public void inputBehaviour (double time, boolean force) {   
      d = !getInputBit(1, time, force) && getInputBit(2, time, force);
      if (resetConnected)
         resetInput = getInputBit(4, time, force);
   }
   
    /** Reset */
    public void reset() {
        super.reset();
        q = false;
    }    
}
