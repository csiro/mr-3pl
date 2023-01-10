/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */


package threepl.simulator;

import threepl.codegen.TDE;


/**
 * The SDEDel class implements the TDEType.DEL TDE in the threepl
 * simulator.
 * 
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
public class SDEDel extends SDE {
    
    // Constant Boolean used to improve fifo operation efficiency
    private static Boolean trueVal = new Boolean(true);
    
    // Constant Boolean used to improve fifo operation efficiency
    private static Boolean falseVal = new Boolean(false);
    
    // Delay buffer
    private SimFifo fifo;

    // Delay in number of clock cycles
    private int delay;

    // Copy of data input
    private boolean d;

    // Copy of data output
    private boolean q;

    // Flag indicating presence of reset input
    private boolean resetConnected;
    
    // Value of reset input prior to clock, if present
    private boolean resetInput;
    
    /**
     * Constructor
     * @param sim The single Sim object
     * @param tde Associated TDE
     */
    protected SDEDel(Sim sim, TDE tde) {
        super(sim, tde);
    }

    /**
     * Do combinatorial behaviour
     * @param oIndex Index of the output to calculate
     * @param time The current simulator time
     * @return True if behaviour was done
     * @param force True if behaviour should happen regardless of time
     */
    public boolean behaviour(int oIndex, double time, boolean force) {
    
       // If already done for this time, no action..
       if (super.behaviour(oIndex, time, force))
          return true;

       // ..else do output behaviour
       oCalc[oIndex] = true;
       putOutput(0, q);
       oCalc[oIndex] = false;
       return true;     	  
    }

    /**
     * Find input values in preparation for next clock.
     * @param time The current simulator time
     * @param force True if behaviour should happen regardless of time
     */
    public void inputBehaviour(double time, boolean force) {
        d = getInputBit(1, time, force);
	if (resetConnected)
	   resetInput = getInputBit(2, time, force);
    }


    /** Build variables */
    public void buildVariables() {
        super.buildVariables();
        delay = 1;

        if(params.size() > 0)
            delay = ((Integer) getParam(0)).intValue();

        if (delay>1)
           fifo = new SimFifo(delay);
	   
        resetConnected = inputs.length>2 && inputConnected[2];	   
    }

    /**
     * Do clock edge. Reset input takes precedence if present.
     * @param index An index to the clock to use, ignored for SDEDel. 
     */
    public void clock(int index) {
    
       // If reset input connected and active, do reset
       if (resetConnected && resetInput) {
          reset();
          return;
       }	  
	
       // Otherwise set q accordingly     
       if (delay>1) {
          q = ((Boolean) fifo.pop()).booleanValue();
          fifo.push(d ? trueVal : falseVal);
       } else
          q = d;
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
     * Take the value in the SimVariable connected to the index'th output and prime the
     * internal register, corresponding to this output, with the value.
     * @param index The index of the output to prime
     */
    public void primeOutput (int index) {

       if (index<0 || index>=outputs.length)
          throw new SimException("SDE.primeOutput(int): invalid index ("+index+")");

       q = outputs[index].getValue().getBit();
    }
                 
    /** Reset */
    public void reset() {
        super.reset();
	if (delay>1) {
	   // Fill fifo so delay works
           fifo.clear();
	   for (int i=0; i<delay; i++)
	      fifo.push(falseVal);
        }	   
        q = false;
    }
}
