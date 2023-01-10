/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */


package threepl.simulator;

import java.util.ArrayList;


/**
 * A SimVariable represents a threepl variable. It has a name and
 * a value (SimValue) of appropriate type. It has a behaviour()
 * method which is called to generate the new variable value at
 * each simulator cycle. If the SimValue (which is this SimVariable's
 * value) is fully connected to the output of a single SDE, the behaviour()
 * method calls the SDE behaviour() method in order to update the
 * value. If instead the SimValue has bitranges connected to a number of
 * different SDEs, the behaviour() of each of the source SDEs is called,
 * and the appropriate bit ranges then copied to the SimValue.
 * 
 * @version $Revision: 8254 $
 * @author Andrew Tulloh
 */
@SuppressWarnings("all")
public class SimVariable implements SimIdentifier {

    // Signal name
    private String id;

    // SDE which sources this variable, or null
    private SDE sourceSDE;

    // Index of output on SDE of this variable, where sourceSDE non-null
    private int oIndex;

    // List of SDEs which are sinks for this variable, or null
    private ArrayList sinkSDEList;
    
    // List of input indices corresponding to SDEs in sinkSDEList, if non-null
    private ArrayList iIndexList;
        
    // Signal value
    private SimValue value;

    // Flag used during initial connection phase
    private boolean connected;

    // Clock associated with this variable, or null if none
    private SimClock clock;
        
    /**
     * Constructor with name, SimValue, type
     * @param id The variable name
     * @param v The value for this variable
     */
    public SimVariable(String id, SimValue v) {
        this(v);
        this.id = id;
    }

    /**
     * Constructor with SimValue, type
     * @param v The value for this variable
     */
    public SimVariable(SimValue v) {
        value = v;
	sinkSDEList = new ArrayList();
	iIndexList = new ArrayList();
    }

    /**
     * Add an SDE which a the sink of this variable (there may be more than one).
     * @param sdeSink SDE which is a sink for this SimVariable
     * @param iIndex Index in sde input signal array
     */
    public void addSinkSDE (SDE sdeSink, int iIndex) {
        sinkSDEList.add(sdeSink);
	iIndexList.add(new Integer(iIndex));
    }

    /**
     * If this SimVariable is the output of an SDE, execute the behaviour of
     * that SDE. Otherwise, this variable may be the input to an SDE so execute
     * the behaviour of the variable's source, ie the behaviour of it's value.
     * In either case this SimVariable's value may be altered. 
     * @param time Current simulator time, ns
     * @param force True if behaviour should happen regardless of time
     */
    public void behaviour (double time, boolean force) {
       if (sourceSDE!=null)
          sourceSDE.behaviour(oIndex, time, force);
       else if (value!=null)
          value.behaviour(time, force);	  
    }
    
    /** Clear changed status. Required by SimIdentifier interface. */                               
    public void clearChanged () {
       if (value!=null)
          value.clearChanged();
    }
                       
    /**
     * Return (boolean) value. Required by SimIdentifier interface.
     * @return value as a boolean
     */
    public boolean getBit () {
    
       if (value==null)
          throw new SimException("cannot read a bit from a null type variable");
	  
       return value.getBit();
    }

    /**
     * Return the clock associated with this variable, or null if none
     * @return The clock associated with this variable, or null if none
     */
    public SimClock getClock () {
       return clock;
    }
                
    /**
     * Return (double) value. Required by SimIdentifier interface.
     * @return value as a double
     */
    public double getDouble () {

       if (value==null)
          throw new SimException("cannot read a number from a null type variable");

       return value.getDouble();
    }
                         
    /**
     * Return id. Required by SimIdentifier interface.
     * @return Variable id (name)
     */
    public String getId () {
       return id;
    }

    /**
     * Return the list of SDE input indices corresponding to the list of SDEs
     * which are sinks for this signal. The list is emptyif the signal has no sink SDEs.
     * @return The list of SDE input indices corresponding to the list of SDEs
     * which are sinks for this signal.
     */
    public ArrayList getIIndexList () {
       return iIndexList;
    }
                
    /**
     * If this variable is the output of an SDE, return the index of this
     * variable in the SDE output list. Otherwise throw an exception.
     * @return Index of this variable in the SDE output list
     */
    public int getOIndex () {
    
       if (sourceSDE==null)
          throw new SimException(
	     "SimVariable.getSDEIndex(): variable "+(id!=null?id:"")+" has no associated source sde");
	     
       return oIndex;	     
    }
             
    /**
     * Return the buffer space count in a queue.
     * This variable must be the output of a queue.
     * @return The buffer space count in this queue.
     */
    public int getQueuespaces () {
    
       // Check is queue
       if (!isQueue())
          throw new SimException((id!=null?id:"variable")+" is not a queue");
	  
       // Return buffer space
       return ((SDEQueuebuffer)sourceSDE).getQueuespaces();	  
    }
         
    /**
     * Return the buffer occupancy count in a queue.
     * This variable must be the output of a queue.
     * @return The buffer occupancy count in this queue.
     */
    public int getQueuewords () {
    
       // Check is queue
       if (!isQueue())
          throw new SimException((id!=null?id:"variable")+" is not a queue");
	  
       // Return buffer occupancy
       return ((SDEQueuebuffer)sourceSDE).getQueuewords();	  
    }

    /**
     * Return the read status in an async queue.
     * This variable must be the output of an async queue.
     * @return The read status in an async queue.
     */
    public SimValue getQueueReadStatus () {
    
       // Check is async queue
       if (!isAsyncQueue())
          throw new SimException((id!=null?id:"variable")+" is not an async queue");
	  
       // Return read status
       return ((SDEQueuebuffer)sourceSDE).getQueueReadStatus();	  
    }
         
    /**
     * Return the wriye status in an async queue.
     * This variable must be the output of an async queue.
     * @return The write status in an async queue.
     */
    public SimValue getQueueWriteStatus () {
    
       // Check is async queue
       if (!isAsyncQueue())
          throw new SimException((id!=null?id:"variable")+" is not an async queue");
	  
       // Return write status
       return ((SDEQueuebuffer)sourceSDE).getQueueWriteStatus();	  
    }
         
    /**
     * Return the list of SDEs which are sinks for this signal.
     * The list is emptyif the signal has no sink SDEs.        
     * @return The list of SDEs which are sinks for this signal
     */
    public ArrayList getSinkSDEList () {
       return sinkSDEList;       
    }     
    
    /**
     * Return the SDE which sources this signal, or null
     * if the signal has no source SDE.
     * @return The SDE which sources this signal, or null
     */
    public SDE getSourceSDE () {
       return sourceSDE;
    }

    /**
     * Return reference to internal SimValue which contains the variable value
     * @return The associated SimValue
     */
    public SimValue getValue() {
        return value;
    }

    /**
     * Return true if the source of this variable is the data
     * output of an asynchronous queuebuffer.
     * @return True if the source of this variable is the data output of an async queue
     */
    public boolean isAsyncQueue () {
       return isQueue() && ((SDEQueuebuffer)sourceSDE).isAsync();
    }

    /** Returns whether is a clock. Required by SimIdentifier interface.
     * @return False
     */
    public boolean isClock () {
       return false;
    }

    /**
     * Return true is this variable has already been connected to its source
     * variable(s). Only used if this variable appears on the lhs of a connect.
     * @return True if already connected
     */
    public boolean isConnected () {
       return connected;
    }
         
    /**
     * Return true if the source of this variable is a data output of a combinatorial ram.
     * @return True if the source of this variable is a data output of a combinatorial ram
     */
    public boolean isCram () {
       return sourceSDE!=null && sourceSDE instanceof SDECram && sourceSDE.getOutput(0)==this;
    }

    /**
     * Return true if the source of this variable is the data
     * output of a queuebuffer.
     * @return True if the source of this variable is the data output of a queue
     */
    public boolean isQueue () {
       return sourceSDE!=null && sourceSDE instanceof SDEQueuebuffer && sourceSDE.getOutput(0)==this;
    }

    /**
     * Return true if the source of this variable is a data output of a registered ram.
     * @return True if the source of this variable is a data output of a registered ram
     */
    public boolean isRram () {
       return sourceSDE!=null && sourceSDE instanceof SDERram && sourceSDE.getOutput(0)==this;
    }

    /**
     * Return true if the source of this variable is the data
     * output of a static.
     * @return True if the source of this variable is the data output of a static
     */
    public boolean isStatic () {
       return sourceSDE!=null && sourceSDE instanceof SDEReg && sourceSDE.getOutput(0)==this;
    }

    /**
     * Return whether is a variable. Required by SimIdentifier interface.
     * @return True
     */
    public boolean isVariable () {
       return true;
    }

    /**
     * Return formatted String representation. Required by SimIdentifier interface.
     * @param fmt A printf style format specification
     * @return Formatted String representation of value
     */
    public String print (String fmt) {
       return value!=null ? value.print(fmt) : "null";
    }
        
    /**
     * Return String representation of type. Required by SimIdentifier interface.
     * @return String representation of type
     */
    public String printType () {
       return value!=null ? value.printType() : "null";
    }

    /**
     * Put a double into this. Required by SimIdentifier interface.
     * @param fval The value to write
     */
    public void put (double fval) {

       if (value==null)
          throw new SimException("cannot write a number to a null type variable");
	  
       value.put(fval);
    }

    /**
     * Within the bit 'range' within this SimVariable, change the references to
     * corresponding SimValues within the value to references to the corresponding
     * SimValues represented by the src SimVariable and associated bit range rangeSrc.
     * The width of the ranges must be the same and the subset SimValue of this and
     * src (corresponding to the bit ranges) must be compatible; ie same structure
     * with same types. Do not need to handle null value in this method as it will
     * only be called for this and src with non-null type value.
     * @param src The source SimVariable
     * @param rangeSrc The bit range within the src SimVariable's value
     * @param range This bit range within this SimVariable's value
     */    
   public void referTo (SimVariable src, SimRange rangeSrc, SimRange range) {
   
      SimValue valueSrc = src.getValue();
      int nbitsSrc = rangeSrc.getWidth();
      int nbits = range.getWidth();

      // Check bit counts are same
      if (nbitsSrc!=nbits)
         throw new SimException(
	    "SimVariable.referTo(SimVariable,SimRange,SimRange): range widths differ");
      
      // If this value has primitive type..
      if (value.isPrimitive()) {

         // If src value has primitive type..
         if (valueSrc.isPrimitive()) {
 
            // this value and src value are primitive types..
      
            // Check ranges equate to full primitive values	
	    value.getPart(range);
	    valueSrc.getPart(rangeSrc);
	    	  
            // Check values are connectable
	    if (!value.isConnectable(valueSrc))
	       throw new SimException(
                  "SimVariable.referTo(SimVariable,SimRange,SimRange): "+
		  "unconnectable primitive values");
		  
	    // Replace value with direct reference to valueSrc	    
	    value = valueSrc;
         }
	 
	 // ..else src has compound type
	 else {
	 
	    // this value is primitive, src value is compound..

            // Get reference to SimValue which represents the src value range.
	    // (This should be a primitive type compatible with value)    
            SimValue partSrc = valueSrc.getPart(rangeSrc);
	    
	    // Check range equates to full primitive value
	    value.getPart(range);
	    
	    // Check selected range of src value is compatible with this value
	    if (!value.isConnectable(partSrc))
	       throw new SimException(
                  "SimVariable.referTo(SimVariable,SimRange,SimRange): "+
		  "primitive value unconnectable with range of compound value");
	       
	    // Replace value with reference to a SimValue which
	    // represents the range of the src value
	    value = partSrc;
         }	 
      }      
      
      // ..else this value has compound type, replace the reference(s) to the
      // primitive(s) within this value (which corresponds to the range within
      // this value) with appropriate reference(s) to the src value
      else      
	 value.referTo(valueSrc, rangeSrc, range);
   }
                 
    /** Reset */
    public void reset () {

       // Only clear value if sourceSDE!=null, otherwise assume it's a constant
       if (value!=null && sourceSDE!=null) {
          value.clear();  // need this to set ext inputs at reset ***INCOMPLETE***
          value.clearChanged();
       }
    }

    /**
     * Set the clock associated with this variable
     * @param clock is the clock associated with this variable
     */
    public void setClock (SimClock clock) {
       this.clock = clock;
    }
                
    /**
     * Set the internal connected flag, indicating this variable has been connected
     * to its source variable(s). Only used if this variable appears on the lhs of
     * a connect.
     */
    public void setConnected () {
       connected = true;
    }
         
    /**
     * Set SDE which is the source of this variable
     * @param sourceSDE SDE which is the source of this SimVariable
     * @param oIndex Index in sde output signal array
     */
    public void setSourceSDE (SDE sourceSDE, int oIndex) {
        this.sourceSDE = sourceSDE;
	this.oIndex = oIndex;
	if (value!=null)
	   value.setSrcVar(this);	
    }

    /**
     * Return default String representation. Required by SimIdentifier interface.
     * @return Default String representation of value
     */
    public String toString () {
       return value!=null ? value.toString() : "null";
    }

    /**
     * Get accessed status. Accessed = read || written.
     * Throw exception if this variable is not the data output variable
     * of a threepl queue, static or ram.
     * @return True if the associated queue/static/ram was read or written on its last clock edge
     */
    public boolean wasAcc () {
       if (sourceSDE!=null) {
	  if (sourceSDE instanceof SDEQueuebuffer)
             return ((SDEQueuebuffer)sourceSDE).wasAcc();
	  else if (sourceSDE instanceof SDEReg)	  
             return ((SDEReg)sourceSDE).wasAcc();
	  else if (sourceSDE instanceof SDECram)	  
             return ((SDECram)sourceSDE).wasAcc();
	  else if (sourceSDE instanceof SDERram)	  
             return ((SDERram)sourceSDE).wasAcc();
       }
       // Throw exception - illegal variable for wasAcc()
       throw new SimException("attempted wasAcc() on non-{queue,static} variable");	  
    }                                   

    /**
     * Return changed status. Required by SimIdentifier interface.
     * @return True is the value was changed
     */                               
    public boolean wasChanged () {
       return value!=null ? value.wasChanged() : false;
   }
                       
    /**
     * Get read status. Only valid for SimVariables which are data outputs
     * of threepl queues or rrams.
     * @return True if the associated queue,rram was read on its last clock edge
     */                               
    public boolean wasRead () {
       if (sourceSDE!=null) {
          if (sourceSDE instanceof SDEQueuebuffer)
             return ((SDEQueuebuffer)sourceSDE).wasRead();
          else if (sourceSDE instanceof SDERram)
             return ((SDERram)sourceSDE).wasRead();
       }	     
       // Throw exception - illegal variable for wasRead()
       throw new SimException("SimVariable.wasRead():attempted on non-{queue,registered ram} variable");	  
   }

    /**
     * Get written status.
     * Throw exception if this variable is not the data output variable
     * of a threepl queue, static or ram.
     * @return True if the associated queue/static/ram was written on its last clock edge
     */                               
    public boolean wasWrit () {
       if (sourceSDE!=null) {
          if (sourceSDE instanceof SDEQueuebuffer)
             return ((SDEQueuebuffer)sourceSDE).wasWrit();
          else if (sourceSDE instanceof SDEReg)	  
             return ((SDEReg)sourceSDE).wasWrit();
          else if (sourceSDE instanceof SDECram)	  
             return ((SDECram)sourceSDE).wasWrit();
          else if (sourceSDE instanceof SDERram)	  
             return ((SDERram)sourceSDE).wasWrit();	     
       }	     
       // Throw exception - illegal variable for wasWrit()
       throw new SimException(
          "attempted wasWrit() on non-{queue,static,ram} variable");	  
   }
}
