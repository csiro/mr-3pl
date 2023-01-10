/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */

package threepl.simulator;

import threepl.codegen.TDE;

/**
 * The SDEDelayLine class implements the TDEType.SPECIAL, type 4 TDE in the threepl
 * simulator.
 * 
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
@SuppressWarnings("all")
public class SDEDelayLine extends SDE {

   // Max delay line length if variable delay is used (otherwise unlimited)
   private static final int MAX_DELAY = 16;
   
   // Delay line buffer size
   private int bufferDepth;

   // The delay line data buffer
   private SimFifo buffer;

   // Copy of data output value
   private SimValue q;

   // Copy of data input value
   private SimValue d;

   // delay variable indicator
   private boolean delVariable;
   
   // delay, steps
   private int del;
   
   // control input connected indicator
   private boolean cConnected;

   // control input
   private boolean control;
   
   // reset input connected indicator
   private boolean rConnected;

   // Reset input buffer
   private boolean r;

   // Initialisation array
   private SimValue[] initial;
   
   // Flag indicating output connected direct to input
   private boolean direct;
   
   /**
    * General constructor
    * @param sim The single Sim object
    * @param tde Associated TDE
    */
   protected SDEDelayLine(Sim sim, TDE tde) { 
       super(sim, tde);
   }

   /**
    * Do combinatorial behaviour
    * @param oIndex Index of the output to calculate
    * @param time The current simulator time
    * @return True (indicates behaviour is done for this output)
    * @param force True if behaviour should happen regardless of time
    */
   public boolean behaviour(int oIndex, double time, boolean force) {
       
      // outputs
      // 0 = data out
      //
      // inputs
      // 0 = clock
      // 1 = data in
      // 2 = delay index if variable, else null
      // 3 = control: if non-null, only advance line if true
      // 4 = reset or null
   
      // If already done for this time, no action..
      if (super.behaviour(oIndex, time, force))
         return true;

      // ..else do output behaviour
           
      oCalc[oIndex] = true;

      // If output connected direct to input, just copy..
      if (direct)
         putOutput(0, getInputValue(1, time, force));

      // ..else output the latest delay line value
      else
         putOutput(0, q);

      oCalc[oIndex] = false;
      return true;     	  
   }

   /**
    * Determine input values in preparation for next clock
    * @param time The current simulator time
    * @param force True if behaviour should happen regardless of time
    */
   public void inputBehaviour(double time, boolean force) {

      // If output connected direct to input, no action
      if (direct)
         return;

      // Otherwise prepare for next clock edge	 
      d.put(getInputValue(1, time, force));
      
      // If delay is variable, get the requested tap index and convert to delay count 
      if (delVariable)
         del = getInputValue(2, time, force).getInt() + 1;
	 
      // Get line-step control and reset inputs if present
      if (cConnected)
         control = getInputBit(3,time, force);	 
      if (rConnected)
         r = getInputBit(4, time, force);
   }

   /** Build variables */
   public void buildVariables() {

      super.buildVariables();

      // Get delay count and input indicator flags     
      if ((del = ((Integer)tde.getParam(1)).intValue()) < 0)
         throw new SimException("SDEDelayLine.buildVariables(): delay line length must be >= 0");	 
      delVariable = inputs.length>2 && inputConnected[2];

      // If delay is not variable but delay is zero, we just connect output
      // to input - no other action.
      if (direct = (!delVariable && del==0))
         return;

      // Create input and output registers
      d = (SimValue) ((SimVariable)inputs[1]).getValue().duplicate();	
      q = (SimValue) outputs[0].getValue().duplicate();
      
      cConnected = inputs.length>3 && inputConnected[3];
      rConnected = inputs.length>4 && inputConnected[4];

      // Create buffer
      bufferDepth = delVariable ? MAX_DELAY : del;
      buffer = new SimFifo(bufferDepth);

      // Build array of initialisation values. Filled with zero if none supplied in params.     
      Object o;
      initial = new SimValue[bufferDepth];
      for (int i=0; i<initial.length; i++) {
         initial[i] = d.duplicate();
	 if (params!=null && params.size()>i+2) {
	    o = getParam(i+2);	
	    if (o instanceof Integer)
               initial[i].put(((Integer)o).intValue());
	    else if (o instanceof Long)
               initial[i].put((double)((Long)o).longValue());
	    else if (o instanceof String)
               initial[i].put((String)o);	       
         } else
	    initial[i].clear();	    
      }	    
   }

   /**
    * Do clock edge
    * @param index An index to the clock to use, ignored if only one clock. 
    */
   public void clock(int index) {

      // No action if output connected direct to input
      if (direct)
         return;
	 
      // Synchronous reset overrides everything
      if (rConnected && r) {
         resetBuffer();
         q.put((SimValue)buffer.peek(bufferDepth-del));
	 return;
      }

      // Otherwise..

      // If control input is null, or control is true, discard earliest value,
      // push d into buffer
      if (!cConnected || control) {    
         buffer.pop(); 
         buffer.push(d.duplicate());
      }
      
      // Always point q to output	    
      q.put((SimValue)buffer.peek(bufferDepth-del));
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

       if (direct)
          throw new SimException(
	  "cannot assign "+outputs[index].getId()+"; it is a zero length delay line");

       q.put(outputs[index].getValue());
    }
                 
   /** Reset */
   public void reset() {
      super.reset();
      
      // No further action if output connected direct to input
      if (direct)
         return;
	 
      resetBuffer();
      
      // If del is variable, we don't know what the variable value is at reset so
      // set it to the delay line length
      q.put((SimValue)buffer.peek(delVariable ? 0 : bufferDepth-del));
   }

   // Reset buffer by filling it with the initial value
   //
   private void resetBuffer () {   
      for (int i=initial.length-1; i>=0; i--) {
         if (buffer.isFull())
	    buffer.pop();
         buffer.push(initial[i]);
      }	 
   }
}
