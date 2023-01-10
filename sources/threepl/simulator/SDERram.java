/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */


package threepl.simulator;

import threepl.codegen.TDE;


/**
 * The SDERram class implements the TDEType.RRAM TDE in the threepl
 * simulator.
 * 
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
@SuppressWarnings("all")
public class SDERram extends SDE implements SimTypes {

   // Number of ports
   private int numPorts;
   
   // Data bit width
   private int dataWidth;
   
   // Address bit width
   private int addressWidth;
   
   // Hex string for memory initialisation, or null
   private String[] initStrings;
   
   // Data port input connected flags
   private boolean[] dataConnected;
   
   // Read enables for next clock edge
   private boolean[] re;
   
   // Write enables for next clock edge
   private boolean[] we;
   
   // Read/write addresses for next clock edge
   private SimValue[] address;
   
   // Data in values to write on next clock edge
   private SimValue[] dataIn;
   
   // Data port output connected flags
   private boolean[] outputConnected;

   // Data out buffers
   private SimValue[] dataOut;
       
   // Internal storage (simulates memory)
   private SimArray mem;
    
    // Memory read last clock edge indicator
    private boolean read;
    
    // Memory written last clock edge indicator
    private boolean writ;
    
    // Temp storage of address as bits
    private SimValue addrBits;
        
    /**
     * General constructor
     * @param sim The single Sim object
     * @param tde Associated TDE
     */
    protected SDERram(Sim sim, TDE tde) { 
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

      // Drive appropriate output port
      if (outputConnected[oIndex])
	 putOutput(oIndex, dataOut[oIndex]);
      
      oCalc[oIndex] = false;
      return true;     	  
    }

    /** Build variables */
    public void buildVariables() {
       super.buildVariables();

       // Get params. Expect at least 3.
       if (params.size()>0)
          numPorts = ((Integer)getParam(0)).intValue();
       if (params.size()>1)
          dataWidth = ((Integer)getParam(1)).intValue(); 
       if (params.size()>2)
          addressWidth = ((Integer)getParam(2)).intValue(); 
       if (params.size()>3)
          initStrings = (String[])getParam(3);
         
        // Get prototype address value. Will never be null and is the same for
	// all ports.
	SimValue paddr = ((SimVariable)inputs[1]).getValue().duplicate();

	// Build input and output arrays..
	
	dataConnected = new boolean[numPorts];
	re = new boolean[numPorts];
	we = new boolean[numPorts];
	address = new SimValue[numPorts];
	dataIn = new SimValue[numPorts];
	outputConnected = new boolean[numPorts];
	dataOut = new SimValue[numPorts];
	addrBits = SimValue.create(BITS, addressWidth);
	
        int inputBase, addressIndex, dataIndex;
	
	for (int i=0; i<numPorts; i++) {
	   inputBase = i*5;
	   addressIndex = inputBase + 1;
	   dataIndex = inputBase + 2;
	   
	   // Build input buffers
	   address[i] = paddr.duplicate();
	   if (dataConnected[i] = inputConnected[dataIndex])
	      dataIn[i] = ((SimVariable)inputs[dataIndex]).getValue().duplicate();
	   
	   // Build output buffers
	   if (outputConnected[i] = outputs[i]!=null)
	      dataOut[i] = ((SimVariable)outputs[i]).getValue().duplicate();
        }	      

        // Create an array to simulate memory storage. The length of the array
	// is 2^(addressWidth). Data type is the same as that of the first non-null
	// data input or output found (they are all the same where non-null). If
	// all data inputs/outputs are null (should never happen), create data as
	// BITS.
	SimValue pdata=null;
	for (int i=0; i<numPorts; i++) {
	   if (dataConnected[i]) {
	      pdata = dataIn[i];
	      break;
           }	      
        }
	if (pdata==null) for (int i=0; i<numPorts; i++) {
	   if (outputConnected[i]) {
	      pdata = dataOut[i];
	      break;
           }	      
	}
	if (pdata==null)
	   pdata = SimValue.create(BITS, dataWidth);
		      
        mem = (SimArray)SimValue.createArray((int)Math.pow(2, addressWidth), pdata);
    }

    /**
     * Do clock edge
     * @param index An index to the clock to use, ignored if only one clock. 
     */
    public void clock(int index) {

       // If read enabled, read memory to output buffer, flag whether was read
       if (outputConnected[index]) {
          if (re[index]) {
             addrBits.put(address[index]);
	     dataOut[index].put(mem.getElement((int)addrBits.getDouble())); 
	     read = true;
          } else
             read = false;
       }	     
	  	  
       // If write enabled, write the input data to memory, flag whether was written
       if (dataConnected[index]) {
	  if (we[index]) {
             addrBits.put(address[index]);
             mem.putElement((int)addrBits.getDouble(), dataIn[index]);
	     writ = true;
	  } else
             writ = false;       	
       }	       
    }

   /**
    * Return the internal SimArray
    */
   public SimValue getBuffer () {
      return mem;   
   }
       
    /**
     * Return the clock attached to this SDE which is associated with the index'th
     * input.
     * @return The clock
     */
    public SimClock getInputClock (int index) {
       return (SimClock)inputs[(index/5)*5];
    }       
              
    /**
     * Return the clock attached to this SDE which is associated with the index'th
     * output.
     * @return The clock
     */
    public SimClock getOutputClock (int index) {
       return (SimClock)inputs[index*5];
    }       
              
    /**
     * Determine input values in preparation for next clock
     * @param time The current simulator time
     * @param force True if behaviour should happen regardless of time
     */
    public void inputBehaviour(double time, boolean force) {
   
       int inputBase, addressIndex, dataIndex, reIndex, weIndex;
       
       for (int i=0; i<numPorts; i++) {       
          inputBase = i*5;
	  addressIndex = inputBase+1;
	  dataIndex = inputBase+2;
          reIndex = inputBase+3;
          weIndex = inputBase+4;
	  
	  // Get addresses for next clock
	  address[i].put(getInputValue(addressIndex, time, force));

	  // Get data inputs and read/write enables for next clock
	  if (dataConnected[i]) {
	     dataIn[i].put(getInputValue(dataIndex, time, force));
	     re[i] = getInputBit(reIndex, time, force);
             we[i] = getInputBit(weIndex, time, force);
          }	  
       }       
    }

    /**
     * Take the value in the SimVariable connected to the index'th output and prime the
     * internal register, corresponding to this output, with the value.
     * @param index The index of the output to prime
     */
    public void primeOutput (int index) {

       if (index<0 || index>=outputs.length)
          throw new SimException("SDE.primeOutput(int): invalid index ("+index+")");

       if (!outputConnected[index])
          throw new SimException("this RRAM has not output "+index);
	  
       dataOut[index].put(outputs[index].getValue());  
    }

    /**
     * Reset. If an inital parameter was specified, load it into the memory,
     * else clear memory.
     */
    public void reset() {
        super.reset();
	if (initStrings!=null)
	   mem.put(initStrings);
        else
	   mem.clear();	   
	read = writ = false;
    }
    
    /**
     * Return whether read last clock edge
     * @return True if memory was read last clock edge
     */
    public boolean wasRead () {
       return read;
    }
           
    /**
     * Return whether written last clock edge
     * @return True if memory was written last clock edge
     */
    public boolean wasWrit () {
       return writ;
    }
           
    /**
     * Return whether accessed last clock edge
     * @return True if memory was accessed (read or written) last clock edge
     */
    public boolean wasAcc () {
       return read || writ;
    }    
}
