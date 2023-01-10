/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */


package threepl.simulator;

import threepl.codegen.TDE;


/**
 * The SDEReg class implements the TDEType.REG TDE in the threepl simulator.
 * 
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
@SuppressWarnings("all")
public class SDEReg extends SDE {
    
   // The clock enable input value
   private SimValue ce;

   // Copy of data input value
   private SimValue d;

   // d input connected indicator
   private boolean dConnected;

   // Reset input value
   private SimValue r;

   // reset input connected indicator
   private boolean rConnected;

   // Data output initialisation value
   private SimValue initial;

   // Copy of data output value
   private SimValue q;

   // Temp value used to determine whether written or not
   private SimValue temp;
   
   // Data input written last clock edge indicator
   private boolean writ;

   /**
    * Constructor
    * @param sim The single Sim object
    * @param tde Associated TDE
    */
   protected SDEReg(Sim sim, TDE tde) {
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
      putOutput(0, q);
      oCalc[oIndex] = false;
      return true;     	  
   }

   /**
    * Do clock edge
    * @param index An index to the clock to use, ignored for SDERg. 
    */
   public void clock(int index) {

      // Write output bits where ce bits set
      if (dConnected) {
	 q.putWithMask(d, ce);
         temp.put(ce);
      }

      // Reset output bits where reset bits set (takes precedence)      
      if (rConnected) {
         q.putWithMask(initial, r);
	 if (dConnected)
	    temp.bitwiseAnd(r.bitwiseNot());
      }
      
      // Reg was written if d connected and was written, and written
      // bits were not reset
      writ = dConnected && !temp.isClear();
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
    * Find input values in preparation for next clock
    * @param time The current simulator time
    * @param force True if behaviour should happen regardless of time
    */
   public void inputBehaviour(double time, boolean force) {
      if (dConnected) {
         d.put(getInputValue(1, time, force));
         ce.put(getInputValue(2, time, force));
      }	   
      if (rConnected)
         r.put(getInputValue(3, time, force));
   }

   /** Build variables  */
   public void buildVariables() {
      super.buildVariables();

      dConnected = inputConnected[1];

      q = (SimValue) outputs[0].getValue().duplicate();
      if (dConnected) {
      
         // Make d same width as q rather than a duplicate of input[1].
	 // input[1] will normally be same width as q but may be smaller
	 // if connected to a constant value(?). d and q must be same width
	 // for putWithMask() to work (see clock() below).
         d = q.duplicate();
         ce = (SimValue) ((SimVariable)inputs[2]).getValue().duplicate();
	 temp = ce.duplicate();
      }	
      rConnected = inputs.length==4 && inputConnected[3];
      if (rConnected)
         r = (SimValue)((SimVariable)inputs[3]).getValue().duplicate();
	 
      initial = q.duplicate();

      // First param is initial (String) value.
      // Second param is timing name (ignore).
      if(params.size() > 0)
         initial.put((String) getParam(0));	 
   }

    /**
     * Take the value in the SimVariable connected to the index'th output and prime the
     * internal register, corresponding to this output, with the value.
     * @param index The index of the output to prime
     */
    public void primeOutput (int index) {

       if (index<0 || index>=outputs.length)
          throw new SimException("SDE.primeOutput(int): invalid index ("+index+")");

       q.put(outputs[index].getValue());
    }
                 
   /** Reset */
   public void reset() {
      super.reset();
      q.put(initial);
      writ = false;
   }

   /**
    * Return whether written last clock edge
    * @return True if data input was written last clock edge
    */
   public boolean wasWrit () {
      return writ;
   }

   /**
    * Return whether accessed last clock edge
    * @return True if was accessed (read or written) last clock edge
    */
   public boolean wasAcc () {
      return writ;
   }
}
