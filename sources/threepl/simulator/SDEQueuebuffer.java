/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */

package threepl.simulator;

import threepl.codegen.TDE;
import threepl.codegen.TDEVar;

/**
 * The SDEQueuebuffer class implements the TDEType.QUEUEBUFFER TDE in the threepl
 * simulator.
 * 
 * @version $Revision: 8254 $
 * @author Andrew Tulloh
 */
@SuppressWarnings("all")
public class SDEQueuebuffer extends SDE {

   // Queue buffer size
   private int bufferDepth;

   // The queue data buffer
   private SimFifo buffer;

   // A spare buffer to store "unused" data values
   private SimFifo spare;

   // Copy of data input value
   private SimValue d;

   // Push input control signal state
   private boolean push;

   // Pop output control signal state
   private boolean pop;

   // Reset input buffer
   private boolean r;

   // Copy of data output value
   private SimValue q;

   // d input connected indicator
   private boolean dConnected;

   // push input connected indicator
   private boolean pushConnected;
   
   // pop input connected indicator
   private boolean popConnected;
   
   // reset input connected indicator
   private boolean rConnected;

   // q output connected indicator
   private boolean qConnected;
   
   // Buffer occupancy count output connected indicator
   private boolean occConnected;
   
   // Buffer space count output connected indicator
   private boolean spaceConnected;

   // Async queue write status output connected indicator
   private boolean awConnected;
   
   // Async queue read status output connected indicator
   private boolean arConnected;

   // Async write status
   private SimValue awStatus;
      
   // Async read status
   private SimValue arStatus;
      
   // SimValue used as template for buffer values
   private SimValue bVal;
      
   // Queue output initialisation value
   private SimValue initial;

   // Load initial value at reset indicator
   private boolean loadInitial = false;

   // Flag indicating whether has two clocks
   private boolean twoClocks;

   // Queue written last clock edge indicator
   private boolean writ;

   // Queue read last clock edge indicator
   private boolean read;    

   // Null queue flag
   private boolean nullQueue;
   
   // Base queue name used with null queues
   private String id;
   
   /**
    * General constructor
    * @param sim The single Sim object
    * @param tde Associated TDE
    */
   protected SDEQueuebuffer(Sim sim, TDE tde) { 

       super(sim, tde);

       // Determine whether this is a null queue
       nullQueue = tde.getOutput(0)==null && tde.getInput(2)==null;

       // If a null queue, get base queue name. Used later to build dummy
       // output data variable.
       if (nullQueue) {

          TDEVar output;
          for (int i=1; i<3; i++) {
             if ((output = tde.getOutput(i)) != null) {
                id = output.getAliasId();
	        break;
             }	     
          }
          // If got an id (always should), remove any dot suffix
          if (id!=null) {
	     int i;
	     if ((i=id.lastIndexOf('.', id.length())) >= 0)
	        id = id.substring(0, i);
          }		
       }
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
      // 0 = data out, can be null
      // 1 = ne (oavail)
      // 2 = nf (iack)
      // 3 = buffer occupancy count, can be null
      // 4 = buffer empty count, can be null
      // 5 = queue write status (async queues only), can be null
      // 6 = queue read status (async queues only), can be null
      //
      // inputs
      // 0 = input clock (or null if only one clock)
      // 1 = output clock
      // 2 = data in, can be null
      // 3 = push, can be null?
      // 4 = pop, can be null?
      // 5 = reset (optional)
   
      // If already done for this time, no action..
      if (super.behaviour(oIndex, time, force))
         return true;

      // ..else do output behaviour
      oCalc[oIndex] = true;
      
      switch (oIndex) {
      case 0:
         if (qConnected)	  
            putOutput(0, q);
	 break;
      case 1:
         putOutput(1, !buffer.isEmpty());  // av
	 break;
      case 2:
         putOutput(2, !buffer.isFull());   // ra
	 break;
      case 3:
         if (occConnected)	 	 
            outputs[3].getValue().put(buffer.size()); // buffer occupancy count
	 break;
      case 4:
         if (spaceConnected)	 	 
            outputs[4].getValue().put(spare.size()); // buffer space count
	 break;
      case 5:
         if (awConnected)
	    putOutput(5, awStatus);   // async write status
         break;	     
      case 6:
         if (arConnected)
	    putOutput(6, arStatus);   // async read status
         break;	     
      }
	       
      oCalc[oIndex] = false;
      return true;     	  
   }

    /** Build outputs. Overrides SDE.buildOutputs() so can handle null queues */
    public void buildOutputs() {
    
       super.buildOutputs();
          
       // If this is a null queue, create a variable with a null value
       // for the data output. This is not connected to anything but allows
       // this variable to be accessed via simulator command line in expressions.
       if (nullQueue) {
          SimVariable s = sim.makeNullData(id);
          s.setSourceSDE(this, 0);
	  if (id!=null)
	     sim.getOutputMap().put(id, s);
          outputs[0] = s;
       }	  
   }          	  

   /**
    * Return a view of the internal fifo as a SimArray
    * Note the view array has the last value written to the queue
    * as its first value and the queue's current output value as
    * its last value.
    */
   public SimValue getBuffer () {
   
      int size = buffer.size();
      int width = bVal.getWidth();
      int low=0, high=width-1;
      SimRange rangeSrc = new SimRange(low, high);
      SimRange rangeDst = new SimRange(low, high);
      SimArray s = (SimArray)SimValue.createArray(size, bVal);
      for (int i=size-1; i>=0; i--) {
         SimValue v = (SimValue)buffer.peek(i);
         s.referTo(v, rangeSrc, rangeDst);  
         low += width;
	 high += width;
         rangeDst.put(low, high);
      }	 
      return s;	 
   }
       
   /**
    * Return buffer space count
    * @return Buffer space count
    */
   public int getQueuespaces () {
      return spare.size();
   }
       
   /**
    * Return buffer occupancy count
    * @return Buffer occupancy count
    */
   public int getQueuewords () {
      return buffer.size();
   }

   /**
    * Return queue read status (async queues only)
    * @return Queue read status (async queues only)
    */
   public SimValue getQueueReadStatus () {   
      if (!arConnected)
         throw new SimException("queue has no read status available");
      return outputs[6].getValue();      
   }
   
   /**
    * Return queue write status (async queues only)
    * @return Queue write status (async queues only)
    */
   public SimValue getQueueWriteStatus () {
      if (!awConnected)
	 throw new SimException("queue has no write status available");
      return outputs[5].getValue();      
   }   
          
   /**
    * Determine input values in preparation for next clock
    * @param time The current simulator time
    * @param force True if behaviour should happen regardless of time
    */
   public void inputBehaviour(double time, boolean force) {

      if (dConnected)
         d.put(getInputValue(2, time, force));

      if (pushConnected)
         push = getInputBit(3, time, force);
      
      if (popConnected)
         pop = getInputBit(4, time, force);    
      
      if (rConnected)
         r = getInputBit(5, time, force);
   }

   /**
    * Return true if this is an async queuebuffer
    * @return True if this is an async queuebuffer
    */
   public boolean isAsync () {
      return twoClocks;
   }    
    
   /** Build variables */
   public void buildVariables() {
      super.buildVariables();

      // Set indicators for optional inputs and outputs
      qConnected = outputConnected[0];
      occConnected = outputs.length>3 && outputConnected[3];
      spaceConnected = outputs.length>4 && outputConnected[4];
      awConnected = outputs.length>5 && outputConnected[5];
      arConnected = outputs.length>6 && outputConnected[6];

      // If output[5] and/or output[6] exist, create associated status variables
      if (awConnected)
         awStatus = outputs[5].getValue().duplicate();
      if (arConnected)
         arStatus = outputs[6].getValue().duplicate();
	 
      twoClocks = inputs[0]!=null;
      dConnected = inputConnected[2];
      pushConnected = inputConnected[3];
      popConnected = inputConnected[4];
      rConnected = inputs.length==6 && inputConnected[5];
            
      // Make SimValues for output and input registers if they exist
      if (qConnected)
         q = (SimValue) outputs[0].getValue().duplicate();
      if (dConnected)
         d = (SimValue) ((SimVariable)inputs[2]).getValue().duplicate();	

      // Find the type of value to use in the buffer. If a null queue, use SimLog.
      if (nullQueue)      
         bVal = SimValue.create(SimTypes.LOG);
      else if (qConnected)
         bVal = q.duplicate();
      else if (dConnected)
         bVal = d.duplicate();
	 
      // Get buffer size and create buffer. Default sizes depend on queue attributes..
      
      // If size supplied, use it..
      if (params.size() > 0)
	 bufferDepth = ((Integer)getParam(0)).intValue();
	 
      // ..else if a synchronous non-null queue..	 
      else if (!twoClocks && (dConnected || qConnected))
         bufferDepth = 2;
	 
      // ..else if any other type	 
      else
         bufferDepth = 16;	 	 

      buffer = new SimFifo(bufferDepth);
      spare = new SimFifo(bufferDepth);

      // Set up initial value if supplied.
      // Assume won't have an initial value if this is a null queue, so won't run into
      // type conflict with initial value.
      if (params.size() > 2) {
	 loadInitial = true;
         initial = (SimValue) bVal.duplicate();
         initial.put((String) getParam(2));
      }	    
   }

   /**
    * Do clock edge
    * @param index An index to the clock to use, ignored if only one clock. 
    */
   public void clock(int index) {

      // Synchronous reset overrides everything
      if (rConnected && r) {
         resetBuffer();
	 if (qConnected)
	    q.clear();
	 writ = read = false;
	 return;
      }

      // Otherwise..
      
      // If two clocks (this is an async queue), update read or write status with
      // the state BEFORE the clock edge
      if (index==0 && awStatus!=null)
          getBufStatus(awStatus);
      else if (index==1 && arStatus!=null)
	  getBufStatus(arStatus);

      // Check index if there are two clocks
      if (twoClocks && index> 1)
         throw new SimException(
	    "SDEQueuebuffer.clock(int):invalid clock index ("+index+")");
       
      // Get read and written states for after this clock edge
      read = popConnected && pop && !buffer.isEmpty();
      writ = pushConnected && push && !buffer.isFull();

      // If there is room in buffer and PUSH is set..
      if (writ && (!twoClocks || index==0)) {
      
         // If data input is connected, push current value into buffer..
         if (dConnected)
	    pushBuffer(d);
	    
         // ..else if data output isn't connected (either), it's a null
	 // queue, push a dummy value into the buffer	    
         else if (!qConnected)
	    pushBuffer(bVal);
      }            

      // Discard earliest buffer entry if was not empty and POP set
      if (read && (!twoClocks || index==1))
         popBuffer();
	  
      // If data output is connected, leave q pointing to earliest buffer entry
      if (qConnected && !buffer.isEmpty())	       
         q = (SimValue) buffer.peek();
	 
   }

    /**
     * Return the clock attached to this SDE which is associated with the index'th
     * input.
     * @return The clock
     */
    public SimClock getInputClock (int index) {
       return (SimClock)(twoClocks ? inputs[0] : inputs[1]);
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

      switch (index) {
      case 0:
         // If null queue, no action, but legal..
         if (nullQueue)
	   return;

         // ..else this queue has no output (won't ever get here since compiler won't allow
	 // queues without sinks)	   
         else if (!qConnected)	
	    throw new SimException("this queue has no data output");
	    
	 q.put(outputs[index].getValue());
	 break;
      default:	
         throw new SimException(
	 "cannot assign "+outputs[index].getId()+"; it is not an independent, clocked output variable");
      }
    }
                 
   /**
    * Reset. If an inital parameter was specified, load it into the buffer,
    * else clear the buffer.
    */
   public void reset() {
      super.reset();
      resetBuffer();
      if (loadInitial) {
	 pushBuffer(initial);
	 if (qConnected)
           q = (SimValue) buffer.peek();
      }	else if (qConnected)   
         q = (SimValue) spare.peek();
      writ = read = false;
   }

   /**
    * Return whether read last clock edge
    * @return True if queuebuffer was read last clock edge
    */
   public boolean wasRead () {
      return read;
   }

   /**
    * Return whether written last clock edge
    * @return True if queuebuffer was written last clock edge
    */
   public boolean wasWrit () {
      return writ;
   }

   /**
    * Return whether accessed last clock edge
    * @return True if queuebuffer was accessed (read or written) last clock edge
    */
   public boolean wasAcc () {
      return read || writ;
   }

   // Return buffer status for async queues
   // @param v The SimValue in which to return the status.
   // Must be a 5 bit quantity (actually struct{log,log,log,log,log}).
   // @return status as a 5 bit quantity
   //
   private void getBufStatus (SimValue v) {
   
      int bsize = buffer.size();
      int ssize = spare.size();
      int b1 = bufferDepth/4;     // 1/4 * bufferDepth
      int b2 = 2*b1;              // 1/2 * bufferDepth
      int b3 = b2+b1;             // 3/4 * bufferDepth
      
      v.put(bsize<=b1, 0);
      v.put(bsize>0&&bsize<=b2, 1);
      v.put(bsize>=b1&&bsize<=b3, 2);
      v.put(bsize>=b2, 3);
      v.put(bsize>=b3, 4);
   }
   
   // Pop the value at the head of the buffer and discard
   //   
   private void popBuffer () {
      spare.push(buffer.pop());      
   }

   // Put a new value into the buffer
   // @param v The SimValue to put in the buffer
   //
   private void pushBuffer (SimValue v) {
      ((SimValue)spare.peek()).put(v);
      buffer.push(spare.pop());    
   }

   // Reset buffer.
   // Note the values pushed into the spare buffer are cleared
   // so that initial queue outputs are zero. This ensures that
   // queue data outputs have defined values at reset, even if they
   // are unavailable (important because other signals may map
   // directly to these outputs).
   //
   private void resetBuffer () {
      buffer.clear();
      spare.clear();
      for (int i=0; i<bufferDepth; i++)
         spare.push(bVal.duplicate().clear()); 
	 
      // Clear status words for async queues	 
      if (awStatus!=null)
         getBufStatus(awStatus);	       
      if (arStatus!=null)
         getBufStatus(arStatus);	       
   }
}
