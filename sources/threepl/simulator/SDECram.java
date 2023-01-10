/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */


package threepl.simulator;

import threepl.codegen.TDE;


/**
 * The SDECram class implements the TDEType.CRAM TDE in the threepl
 * simulator.
 * 
 * @version $Revision: 8254 $
 * @author Andrew Tulloh
 */
@SuppressWarnings("all")
public class SDECram extends SDE implements SimTypes {

   // Number of ports
   private int numPorts;
   
   // Data bit width
   private int dataWidth;
   
   // Address bit width
   private int addressWidth;
   
   // Hex strings for memory initialisation, or null
   private String[] initStrings;
   
   // Data port input connected flags
   private boolean[] dataConnected;
   
   // Write enables for next clock edge
   private boolean[] we;
   
   // Write addresses for next clock edge
   private SimValue[] address;
   
   // Data values to write on next clock edge
   private SimValue[] dataIn;
   
   // Data port output connected flags
   private boolean[] outputConnected;
   
   // Internal storage (simulates memory)
   private SimArray mem;
    
    // Queue written last clock edge indicator
    private boolean writ;

    // Temp storage of address as bits
    private SimValue addrBits;
        
    /**
     * General constructor
     * @param sim The single Sim object
     * @param tde Associated TDE
     */
    protected SDECram(Sim sim, TDE tde) { 
        super(sim, tde);
    }

    /**
     * Do combinatorial behaviour
     * @param oIndex Index of the output to calculate
     * @param time The current simulator time
     * @param force True if behaviour should happen regardless of time
     * @return True if behaviour was done
     */
    public boolean behaviour(int oIndex, double time, boolean force) {
    
       // If already done for this time, no action..
       if (super.behaviour(oIndex, time, force))
          return true;

       // ..else do output behaviour
       oCalc[oIndex] = true;

       // Drive appropriate output port
       if (outputConnected[oIndex]) {
       
          // Get associated input address as bits
          addrBits.put(getInputValue(oIndex*4+1, time, force));

          // Lookup memory and drive output port
	  putOutput(oIndex, mem.getElement((int)addrBits.getDouble()));
       }
       
       oCalc[oIndex] = false;
       return true;     	  
    }

    /**
     * Determine input values in preparation for next clock
     * @param time The current simulator time
     * @param force True if behaviour should happen regardless of time
     */
    public void inputBehaviour(double time, boolean force) {
   
       int inputBase, addressIndex, dataIndex, weIndex;
       
       for (int i=0; i<numPorts; i++) {       
          inputBase = i*4;
	  addressIndex = inputBase+1;
	  dataIndex = inputBase+2;
          weIndex = inputBase+3;
	  
	  // Get addresses for next clock
	  address[i].put(getInputValue(addressIndex, time, force));

	  // Get data inputs and write enables for next clock
	  if (dataConnected[i]) {
	     dataIn[i].put(getInputValue(dataIndex, time, force));
             we[i] = getInputBit(weIndex, time, force);
          }
       }       
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

	// Build input and output arrays..
	
	dataConnected = new boolean[numPorts];
	we = new boolean[numPorts];
	address = new SimValue[numPorts];
	dataIn = new SimValue[numPorts];
	outputConnected = new boolean[numPorts];
	addrBits = SimValue.create(BITS, addressWidth);
	
        // Get prototype address value. Will never be null and is the same for
	// all ports.
	SimValue paddr = ((SimVariable)inputs[1]).getValue().duplicate();

        // Build input/output arrays	       
        int inputBase, addressIndex, dataIndex, weIndex;
	
	for (int i=0; i<numPorts; i++) {
	   inputBase = i*4;
	   addressIndex = inputBase + 1;
	   dataIndex = inputBase + 2;
	   weIndex = inputBase + 3;

	   // Build input arrays
	   address[i] = paddr.duplicate();
	   if (dataConnected[i] = inputConnected[dataIndex])
	      dataIn[i] = ((SimVariable)inputs[dataIndex]).getValue().duplicate();
	   
	   // Build output arrays
	   outputConnected[i] = outputs[i]!=null;
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
	      pdata = ((SimVariable)outputs[i]).getValue().duplicate();
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
       return (SimClock)inputs[(index/4)*4];
    }       
              
    /**
     * Return the clock attached to this SDE which is associated with the index'th
     * output.
     * @return The clock
     */
    public SimClock getOutputClock (int index) {
       return (SimClock)inputs[index*4];
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
	writ = false;
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
       return writ;
    }
    
}
