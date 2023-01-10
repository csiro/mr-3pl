/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */


package threepl.simulator;

import threepl.codegen.TDE;


/**
 * The SDEDff class implements the TDECode.DFF TDE in the threepl simulator.
 * 
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
public class SDEDff extends SDE {

    // All inputs and outputs are single bit control signals
    
    // Copies of inputs
    private boolean d;
    private boolean ce;
    private boolean r;
    private boolean s;
    
    // Inputs connected indicators
    private boolean dConnected;
    private boolean ceConnected;
    private boolean rConnected;
    private boolean sConnected;

    // Copy of data output value */
    private boolean q;
    
    // Initial value
    private boolean initial;
    
    // Flag true whe set/reset inputs are aysnchronous
    private boolean rsAsync;

    /**
     * Constructor
     * @param sim The single Sim object
     * @param tde Associated TDE
    */
    protected SDEDff(Sim sim, TDE tde) {
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
       
       // Handle asyncronous set/reset
       if (rsAsync) {
          if (rConnected && getInputBit(3, time, force))
             q = false;
          else if (sConnected && getInputBit(4, time, force))
             q = true;	  
       }
       
       putOutput(0, q);
       oCalc[oIndex] = false;
       return true;     	  
    }

    /**
     * Find input values in preparation for next clock
     * @param time The current simulator time
     * @param force True if behaviour should happen regardless of time
     */
    public void inputBehaviour(double time, boolean force) {
       if (dConnected)
          d = getInputBit(0, time, force);
       if (ceConnected)
          ce = getInputBit(2, time, force);
	  
       // Only need to look at set/reset inputs here if these are synchronous
       if (!rsAsync) {	  
	  if (rConnected)
             r = getInputBit(3, time, force);
	  if (sConnected)
             s = getInputBit(4, time, force);	  
       }	  
    }

    /** Build variables */
    public void buildVariables() {
    
	// Inputs and outputs are as follows. Not all inputs are necessarily connected. If the
	// R and S inputs are connected they take priority over the D input.
	//
	// Inputs:
	// 0 D data 
	// 1 clock (assume always connected)
	// 2 clock enable
	// 3 R data
	// 4 S data
	// Outputs:
	// 0 Q data
	
        super.buildVariables();
	
	dConnected = inputConnected[0];
	ceConnected = inputConnected[2];
	rConnected = inputConnected[3];
	sConnected = inputConnected[4];	

        // Load initial value if supplied
       if (params.size() > 0 && getParam(0)!=null)
          initial = ((Boolean)getParam(0)).booleanValue();
	  
       // Record whether set/reset inputs are asynchronous
       if (params.size() > 1 && getParam(1)!=null)
          rsAsync = ((Boolean)getParam(1)).booleanValue();
       else
          rsAsync = false;	  	  
    }


    /**
     * Do clock edge
     * @param index An index to the clock to use, ignored for SDEDff. 
     */
    public void clock(int index) {

       // r, s and d inputs take priority in that order.

       // Ignore set/reset inputs here if they are asynchronous       
       if (!rsAsync) {
	  if (rConnected && r) {
             q = false;
	     return;
	  } else if (sConnected && s) {
             q = true;       
	     return;
          }	     
       }	  
       if (dConnected)
          q = ceConnected ? d && ce : d;
    }

    /**
     * Return the clock attached to this SDE which is associated with the index'th
     * input.
     * @return The clock
     */
    public SimClock getInputClock (int index) {
       return (SimClock)inputs[1];
    }       
              
    /**
     * Return the clock attached to this SDE which is associated with the index'th
     * output.
     * @return The clock
     */
    public SimClock getOutputClock (int index) {
       return (SimClock)inputs[1];
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
        q = initial;
    }
}
