/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */


package threepl.simulator;

import java.util.ArrayList;

import threepl.codegen.TDE;
import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.parser.Constant;


/**
 * The SDE class is an analogue of the TDE (topological description element) class, but is
 * used to implement the elements in the Threepl simulator. There are subclasses corresponding to
 * most TDE types, eg SDEQueuebuffer implements in the simulator what a TDE of type tdeQUEUEBUFFER
 * represents. The SDE base class contains fields and methods common to all SDEs. Fields
 * 'inputs' and 'outputs' define the input and output clock/variables respectively for this
 * SDE. Field 'time' indicates the last time the combinatorial behaviour of this SDE has
 * been invoked since the simulator reset.
 *
 * When the SDE is created according to its TDE analogue, the 'buildInputs' and
 * 'buildOutputs' methods are used to map clocks/variables to the SDE inputs and outputs. The
 * 'buildVariables' method creates any internal variables required and initialises the SDE
 * output variable(s) if initialisation value(s) were specified in the parameters list. The
 * 'reset' method is called after creation, and during a simulator reset, to put the SDE in a
 * defined state,
 * 
 * The main operations of an SDE occur in three stages. Firstly, the SDE is clocked via its
 * 'clock' method. Subclass SDEs with state will override the default method which does
 * nothing. For an SDE with state, the clock method applies a clock edge to the internal
 * state devices, without changing SDE outputs. This allows all SDEs to be clocked together
 * while keeping all SDE inputs unchanged. Secondly, the 'behaviour' method is called to
 * generate the SDE output signals. All subclass SDEs override with their own specific
 * behaviour. The behaviour method uses current internal state and SDE variable inputs to
 * generate outputs. Determining the state of an input usually requires calling behaviour for
 * the variable which supplies that input. The variable behaviour method in turn calls
 * behaviour for its source SDE. The search back through SDEs for the ultimate source
 * variable(s) of an input stops when a constant, external input variable, or an output variable
 * is encountered which depends only on state, not inputs, of an SDE. SDEs with state are
 * SDEQueuebuffer, SDEReg and SDEDel, among others.
 * Thirdly and finally, the 'inputBehaviour' method is called. The default
 * method does nothing and it is overridden only by SDEs with state. Its purpose is to
 * determine input values used at the next clock edge to determine the next state. This is
 * necessary firstly because the behaviour() method does not necessarily find all current input
 * values in SDEs with state (because outputs may depend only on state and not on inputs).
 * Secondly, inputBehaviour() is called initially when the user steps the simulator. This
 * ensures that any variables which the user has manually changed are used in the next clock
 * cycle with the modified values.
 * 
 * A number of other utility methods exist for obtaining references to input and output
 * variables.
 * 
 * @version $Revision: 8254 $
 * @author Andrew Tulloh
 */
@SuppressWarnings("all")
public class SDE
    implements TDEConstants, Constant, SimTypes, Cloneable {

    // Time since last reset
    protected double time;

    // Input connected flags. True==connected, false==unconnected
    protected boolean inputConnected[];
    
    // Output connected flags. True==connected, false==unconnected
    protected boolean outputConnected[];
    
    // Inputs. Can be variable or clock
    protected SimIdentifier[] inputs;

    // Outputs. Variable only
    protected SimVariable[] outputs;

    // List of initialisation parameters
    protected ArrayList params;

    // Reference to the single Sim object
    protected Sim sim; 

    // Associated TDE
    protected TDE tde;
    
    // Array of flags used for reentrant loop checking during behaviour()
    protected boolean[] oCalc;
    
    // Time of last behaviour calculation for each output
    private double[] oTime;
    
    /**
     * Factory create method
     * @param sim The single Sim object
     * @param tde Associated TDE
     * @return An SDE of subclass according to TDE type
     */
    public static SDE create(Sim sim, 
                             TDE tde) {
        switch(tde.getType()) {

        case AND:
	    return new SDEAnd(sim, tde);
        case CONNECT:
            // This is only for the cast version of the TDECONNECT.
            // It is distinguished by having a single param where the
            // ordinary connect has none.
	    return new SDECast(sim, tde);
        case CRAM:
	    return new SDECram(sim, tde);
        case DEL:
            return new SDEDel(sim, tde);
        case DFF:
	    return new SDEDff(sim, tde);
        case DIVERGE:
	    return new SDEDiverge(sim, tde);	    
        case DOWHILE:
            return new SDEDowhile(sim, tde);
        case EXECP:
            return new SDEExecp(sim, tde);
        case ILOOP:
            return new SDEIloop(sim, tde);
        case INV:
	    return new SDEInv(sim, tde);	    
        case OPERATOR:
            return new SDEOperator(sim, tde);
        case OR:
	    return new SDEOr(sim, tde);
        case QUEUEBUFFER:
            return new SDEQueuebuffer(sim, tde);
	case PRIORITY:
	    return new SDEPriority(sim, tde);
        case REG:
	    return new SDEReg(sim, tde);
        case RESYNC:
	    return new SDEResync(sim, tde);	    
        case RRAM:
	    return new SDERram(sim, tde);
        // TSELECT is same as SELECT for simulator
        case SELECT:
        case TSELECT:
            return new SDESelect(sim, tde);
        case SPECIAL:
	    int type = ((Integer)tde.getParam(0)).intValue();
	    switch (type) {
	    case 4:
	       return new SDEDelayLine(sim, tde);
            // Ignore others
            default:
	       return null;
	    }
        case WAIT:
            return new SDEWait(sim, tde);
        case WHEN:
	    return new SDEWhen(sim, tde);	    
        case WHILE:
            return new SDEWhile(sim, tde);
        case XOR:
	    return new SDEXor(sim, tde);	    
        default:
            throw new SimException(
	       "SDE.create(Sim,TDE):invalid TDE type ("+tde.getType()+")");
        }
    }

    /**
     * Do combinatorial behaviour. All SDEs overrided this method. Specific SDE behaviour
     * methods should first call this method to check for reentrant loops and to handle
     * time checking. If this method returns true the calling SDE behaviour method
     * should return true immediately, otherwise should execute output specific behaviour
     * before returning true. The calling SDE behaviour method should set oCalc[oIndex]
     * true while executing behaviour (this is used for reentrant loop checking).
     * @param oIndex Index of output for which to find behaviour
     * @param time Current simulator time; used to avoid executing behaviour redundantly.
     * @return False if calling SDE behaviour method should execute output behaviour
     * @param force True if behaviour should happen regardless of time
     */
    public boolean behaviour(int oIndex, double time, boolean force) {

       // Reentrant (combinatorial) loop checking only needs to be done once
       // as connections are static, however the extra overhead in doing the check
       // here each simulator cycle is minimal.
       
       // Check this output not dependent on itself (combinatorial loop)
       if (oCalc[oIndex])
          throw new SimException(
	     "SDE.behaviour(int,double,boolean): combinatorial loop, output "+oIndex+
	     ", variable "+outputs[oIndex].getId());

       // No action if already done for this time (unless force true)
       if (!force && oTime[oIndex] == time)
          return true;
	  
       // Update time for this output	  	  
       oTime[oIndex] = time;

       // Retun false to indicate SDE specific behaviour should be calculated
       // for this output
       return false;
    }

    /** Build inputs. Map inputs array to SimClocks or SimVariables. */
    public void buildInputs() {
    
        int ninputs = tde.getInputs().size();
        inputs = new SimIdentifier[ninputs];
        inputConnected = new boolean[ninputs];
	
        TDEVar tdeVar;
        String      id;
	SimClock    clock;

        // For each input, find the source clock or signal
        for(int i = 0; i < ninputs; i++) {
            tdeVar = (TDEVar) tde.getInput(i);
	    
	    // Ignore if tdeVar==null: input not connected
	    if (tdeVar==null) {
	       inputConnected[i] = false;
	       continue;
            }
            inputConnected[i] = true;	    	       
		       
	    id = tdeVar.getAliasId();
	   
	    // If a clock identifier, add this SDE to the clock's list..
	    if (sim.existsClock(id)) {
	       clock = sim.getClock(id);
	       clock.addSDE(this, i);
	       inputs[i] = clock;
            }
	    
	    // ..else find the signal
	    else {	      	    
	       SimVariable s = sim.findSignal(tdeVar);

               // This SDE is one of the variable's sinks
	       s.addSinkSDE(this, i);
	       
	       // If named, and not in outputMap, add to inputMap
	       id = s.getId();
               if (id!=null && !sim.getOutputMap().containsKey(id))	
	          sim.getInputMap().put(id, s);
               inputs[i] = s;		   
            }		   
        }
    }

    /** Build outputs. Map outputs array to SimVariables. */
    public void buildOutputs() {
        int noutputs = tde.getOutputs().size();
        outputs = new SimVariable[noutputs];
        outputConnected = new boolean[noutputs];
	oCalc = new boolean[noutputs];
	oTime = new double[noutputs];

        TDEVar tdeVar;
        String      id;
        SimVariable s;

        // For each output name, use the corresponding SimVariable in the variableMap */
        for(int i = 0; i < noutputs; i++) {
	
            tdeVar = (TDEVar) tde.getOutput(i);

	    // Ignore if tdeVar==null: output not connected
	    if (tdeVar==null) {
	       outputConnected[i] = false;
	       continue;
            }
            outputConnected[i] = true;	    	       

            s = (SimVariable) sim.findSignal(tdeVar);

	    // This SDE is the variable's source
            s.setSourceSDE(this, i);
	    
	    // If named, add to outputMap
	    id = s.getId();
	    if (id!=null)
	       sim.getOutputMap().put(id, s);

            outputs[i] = s;
        }
    }


    /** Build variables */
    public void buildVariables() {
    
        // Reference params list from tde. Subclasses will create additional state variables etc.
        params = tde.getParams();
    }

    /**
     * Do clock edge. Default is no action, corresponding to no state devices.
     * SDEs with state will override.
     * @param index An index to the clock to use, where SDE has more than one clock, else 0. 
     */
    public void clock (int index) {
        // Default is no action..
    }

    /**
     * Return the clock attached to this SDE which is associated with the index'th
     * input. Default is null. Clock driven SDEs will override.
     * @return null
     */
    public SimClock getInputClock (int index) {
       return null;
    }       
              
    /**
     * Return the clock attached to this SDE which is associated with the index'th
     * output. Default is null. Clock driven SDEs will override.
     * @return null
     */
    public SimClock getOutputClock (int index) {
       return null;
    }       
              
    /**
     * Return reference to desired output
     * @param index Index in output variable array
     * @return The SimVariable corresponding to the desired output
     */
    public SimVariable getOutput(int index) {

        if (index<0 || index>=outputs.length)
	   throw new SimException("SDE.getOutput(int):invalid index ("+index+")");

        return outputs[index];
    }

    /**
     * Find input values in preparation for next clock. Default is no behaviour,
     * SDEs with pure state (DEL, QUEUEBUFFER, REG) will override.
     * @param time Current simulator time; used to avoid executing inputBehaviour redundnantly.
     * @param force True if behaviour should happen regardless of time
     */
    public void inputBehaviour(double time, boolean force) {
        // Default is no behaviour..
    }

    /**
     * Take the value in the SimVariable connected to the index'th output and prime the
     * internal register, corresponding to this output, with the value. Most clocked SDEs
     * with registered outputs will override.
     * @param index The index of the output to prime
     */
    public void primeOutput (int index) {

       if (index<0 || index>=outputs.length)
          throw new SimException("SDE.primeOutput(int): invalid index ("+index+")");
	  
       // Can't prime an output by default
       throw new SimException(
       "cannot assign "+outputs[index].getId()+"; it is not an independent, clocked output");
    }
                 
    /**
     * Set up reference to this SDE in the associated TDE
     * @param tde Associated TDE
     */
    public void registerWithTDE(TDE tde) {
        //tde.registerSDE(this);  called procedure did nothing so has been commented out!
    }

    /** Reset. SDEs with state will override. */
    public void reset() {
       // Default is no action..
    }

    // Constructor
    // @param sim The single Sim object
    // @param tde Associated TDE
    //
    protected SDE(Sim sim, TDE tde) {
        this.sim = sim;
        this.tde = tde;
    }

    // Return value of a single bit input from the attached source variable. Valid only
    // for single bit variables.
    // @param index An index into the inputs array
    // @param time Current simulator time
    // @param force True if behaviour should happen regardless of time
    // @return The boolean value of the desired input
    //
    protected boolean getInputBit(int index, double time, boolean force) {
        return getInputValue(index, time, force).getBit();
    }

    // Return reference to the SimValue representing a signal input from the attached source
    // @param index An index into the inputs array
    // @param time Current simulator time
    // @param force True if behaviour should happen regardless of time
    // @return The SimValue underlying the desired input variable
    //
    protected SimValue getInputValue(int index, double time, boolean force) {
				     
        // Check index valid and refers to a variable, not a clock
        if (index<0 || index>=inputs.length)
	   throw new SimException("SDE.getInputValue(int,double,boolean):invalid index ("+index+")");
        else if (!(inputs[index] instanceof SimVariable))
	   throw new SimException(
	      "SDE.getInputValue(int,double,boolean):index ("+index+") does not refer to a SimVariable");
	
        ((SimVariable)inputs[index]).behaviour(time, force);
        return ((SimVariable)inputs[index]).getValue();
    }

    // Get initialisation parameter by index in list
    // @param i Index to desired param in initialisation parameter list
    // @return (Object) the desired parameter
    //
    protected Object getParam(int i) {
        return (params.get(i));
    }

    // Put an output value
    // @param index An index into the outputs array
    // @param v A SimValue to copy to the desired output
    //
    protected void putOutput(int index, SimValue v) {
			     
        if (index<0 || index>=outputs.length)
	   throw new SimException("SDE.putOutput(int,SimValue):invalid index ("+index+")");
	
        outputs[index].getValue().put(v);
    }

    // Put an output single bit. Valid only for single bit output variables.
    // @param index An index into the outputs array
    // @param l The boolean value to write to the output
    //
    protected void putOutput(int index, boolean l) {

        if (index<0 || index>=outputs.length)
	   throw new SimException("SDE.putOutput(int,boolean):invalid index ("+index+")");
	
        outputs[index].getValue().put(l);
    }
}
