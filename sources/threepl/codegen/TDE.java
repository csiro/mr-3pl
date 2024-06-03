package threepl.codegen;

import static threepl.ThreePL.msg;
import static threepl.ThreePL.icl;
import static threepl.ThreePL.tdelist;

import java.util.ArrayList;
import java.util.Iterator;

import threepl.exceptions.ExEx;
import threepl.exec.Clock;
import threepl.exec.QueueRefs;
import threepl.exec.Var;
import threepl.netlist.TDECode;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;
//import threepl.simulator.SDE;


/**
 * A TDE is a Topological Description Element. TDEs describe generic hardware
 * elements such as registers, gates, selectors, decoders and queue buffers.
 */
public class TDE implements Constant, TDEConstants {
    private TDEType             type;       // element type
    private ArrayList<Object>   params;     // parameters
    private TDEVarList          inputs;     // inputs
    private TDEVarList          outputs;    // outputs
    private SrcLoc              loc;        // source location
    private boolean             active;     // element is still in use
    private boolean             no_delete;  // suppress deletion
    private boolean             deletedinput = false;
    
    private static final String[] execp_types = {
                            "priority, no queues",
                            "priority, buffered queues only",
                            "no priority, buffered queues only",
                            "no priority, unbuffered (+ buffered) queues"
                        };

    /**
     * Constructor without source file location.
     * @param   t type of TDE
     * @param   p parameter section list
     * @param   i input section list
     * @param   o output section list
     */
    public TDE (
        TDEType             t, 
        ArrayList<Object>   p, 
        TDEVarList          i, 
        TDEVarList          o
    ) {
        makeTDE(t, p, i, o);
    }

    /**
     * Constructor with no lists.
     * Lists will be added later.
     * @param t type of TDE
     * @param l source file location
     */
    public TDE (
        TDEType t, 
        SrcLoc  l
    ) {
        loc = l;
        makeTDE(t, null, null, null);
    }

    /**
     * Constructor with no source file location and no lists.
     * Lists will be added later.
     * @param   t type of TDE
     */
    public TDE (TDEType t) {
        makeTDE(t, null, null, null);
    }

    /**
     * Constructor for TDEList signal declaration (only used if simulator implemented and enabled).
     * @param   t type of TDE
     * @param   i input section list
     */
    public TDE(
        TDEType             t, 
        TDEVarList          i
    ) {
        makeTDE(t, null, i, null);
    }
    
    private void makeTDE (
        TDEType             t, 
        ArrayList<Object>   p, 
        TDEVarList          i, 
        TDEVarList          o
    ) {
        type = t;
        active = true;
        params = (p != null) ? p : new ArrayList<Object>();
        inputs = (i != null) ? i : new TDEVarList();
        outputs = (o != null) ? o : new TDEVarList();
    }

    /**
     * Remove crucial fields to preclude any attempt to add any
     * parameters, inputs or outputs to this TDE without a
     * fatal crash!
     */
    public void clear () {
        params = null;
        inputs = null;
        outputs = null;
        active = false;
    }
        
    /**
     * Determine if the TDE is active.
     * @return  true if the TDE is active
     */
    public boolean isActive () { return(active); }
        
    /**
     * Get the source file location for this TDE.
     * @return  the source file location
     */
    public SrcLoc getSrcLoc () { return(loc); }
 
    /**
     * Remove the input and output TDEVars from a TDE and remove this TDE from
     * those TDEVars. Set the TDE inactive.
     */
    public void deactivate () {
        Iterator<TDEVar>    it;
        
        // iterate through this TDE's inputs decrementing the destination
        // count for any clock variables
        for (TDEVar tdev: inputs) {
            if (tdev == null)
                continue;
            Var var = tdev.getVar();
            if (var == null)
                continue;
            if ((var != null) && var instanceof Clock)
                ((Clock)var).decrClkSinks();
        }
        
        // set inactive
        active = false;

        // iterate through this TDE's outputs
        //     for each output TDEVar remove this TDE from the TDEVar src list
        //     remove the TDEVar from this TDE outputs list
        it = outputs.iterator();
        while (it.hasNext()) {
            TDEVar  tdev = it.next();
            if (tdev != null)
                tdev.removeSrc(this);
            it.remove();
        }
        
        // iterate through this TDE's inputs
        //     remove this TDE from the TDEVar dest list
        //     remove the TDEVar from this TDE inputs list
        it = inputs.iterator();
        while (it.hasNext()) {
            TDEVar  tdev = it.next();
            if (tdev != null) {
                tdev.removeDest(this);
                Var var = tdev.getVar();
                if (var != null) {
                    if ((var != null) && var instanceof Clock)
                        ((Clock)var).decrClkSinks();
                }
            }
            it.remove();
        }
    }
    
    /**
     * Remove all input and output links.
     * All input and output TDEVar links to this TDE are removed and
     * source and destination TDEVar lists are cleared.
     * The TDE is not made inactive.
    public void strip () {
        setInActive(); // do this to decrement clock counts - 
        active = true; // but make active again!
        
        TDEVar      ov = getOutput(0);
        Iterator    it = ov.getSrcList().iterator();
        while (it.hasNext()) {
            TDE tde = (TDE)it.next();
            if (tde == this)
                it.remove();
        }
        
        for (TDEVar iv : inputs) {
            it = iv.getDestList().iterator();
            while (it.hasNext()) {
                TDE tde = (TDE)it.next();
                if (tde == this)
                    it.remove();
            }
        }
        inputs.clear();
        outputs.clear();
    }
     */
    
    /**
     * Mark a SELECT element as not to be eliminated as
     * redundant if input is not full width and there is not a
     * single execute signal.
     * This is explicitly required when writing to a queue variable so that
     * a struct field or array member that is not included in an assignment
     * has all bits driven low. If a field or array member is assigned in
     * only one of many assignments then the selector for that will
     * have only one input. If that selector is eliminated and the data is
     * connected straight through then that field or array member will be
     * assigned that data value on every assignment to that queue whereas
     * it should be assigned all zeros except on that one assignment that explicitly
     * addresses it.
     */
    public void setNoDelete () { no_delete = true; }

    /**
     * Get an input signal.
     * @param   i is the index of the member required
     * @return  the input TDEVar
     */
    public TDEVar getInput (int i) { return(inputs.get(i)); }

    /**
     * Get the inputs list.
     * @return the inputs list
     */
    public TDEVarList getInputs () { return(inputs); }

    /**
     * Get an output signal.
     * @param   i is the index of the member required
     * @return  the output TDEVar
     */
    public TDEVar getOutput (int i) { return(outputs.get(i)); }

    /**
     * Get the outputs list.
     * @return the outputs list
     */
    public TDEVarList getOutputs () { return(outputs); }

    /**
     * Get a member of the params list.
     * @param   i is the index of the member required
     * @return  the params list member
     */
    public Object getParam (int i) { return(params.get(i)); }

    /**
     * Get the params list.
     * @return  the params list
     */
    public ArrayList<Object> getParams () { return(params); }

    /**
     * Get the TDE type.
     * @return  the TDE type
     */
    public TDEType getType () { return type; }
    
    /**
     * Change the type of a TDE.
     * @param t is the new type
     */   
    public void changeType (TDEType t) { type = t; }

    /**
     * Add a null to the parameter section of a TDE.
     */
    public void add2p () { params.add(null); }

    /**
     * Add an Integer to the parameter section of a TDE.
     * @param   i is the integer to be appended to the parameter section
     */
    public void add2p (Integer i) { params.add(i); }

    /**
     * Add a Long to the parameter section of a TDE.
     * @param   l is the Long to be appended to the parameter section
     */
    public void add2p (Long l) { params.add(l); }

    /**
     * Add a String to the parameter section of a TDE.
     * @param   s is the string to be appended to the parameter section
     */
    public void add2p (String s) { params.add(s); }

    /**
     * Add a String array to the parameter section of a TDE.
     * @param   sa is the string array to be appended to the parameter section
     */
    public void add2p (String[] sa) { params.add(sa); }

    /**
     * Add a boolean to the parameter section of a TDE.
     * @param   b is the boolean to be appended to the parameter section
     */
    public void add2p (Boolean b) { params.add(b); }

    /**
     * Add a Double to the parameter section of a TDE.
     * @param   f is the Double to be appended to the parameter section
     */
    public void add2p (Double f) { params.add(f); }

    /**
     * Add an arithmetic operator to the parameter section of a TDE.
     * @param   t is the arithmetic operator to be appended to the
     *          parameter section
     */
    public void add2p (TDEOp t) { params.add(t); }

    /**
     * Add a primitive type to the parameter section of a TDE.
     * @param   t is the primitive type to be appended to the parameter
     *          section
     */
    public void add2p (Ptype t) { params.add(t); }

    /**
     * Add a Var to the parameter section of a TDE.
     * @param   v is the Var to be appended to the parameter section
     */
    public void add2p (Var v) { params.add(v); }

    /**
     * Add a QueueRefs to the parameter section of a TDE.
     * @param   v is the Var to be appended to the parameter section
     */
    public void add2p (QueueRefs v) { params.add(v); }

    /**
     * Add a TDEVar as a non-clock input to the input section of a TDE.
     * If the signal is a clock it will not have its sink count incremented
     * @param   v is the variable to be appended to the input section
     */
    public void add2i (TDEVar v) {
        if (v != null) {
            v.addDest(this);
            Var var = v.getVar();
            if (var != null) {
                var.setUsed();
                if (var.getMode() == Mode.CLOCK)
                    ((Clock)var).incrClkSinks();
            }
        }
        inputs.add(v);
    }

    /**
     * Add a TDEVar as a clock input to the input section of a TDE.
     * If the signal is a clock it will have its sink count incremented
     * @param   v is the variable to be appended to the input section
     */
    public void add2ic (TDEVar v) {
        if (v != null) {
            v.addDest(this);
            Var var = v.getVar();
            if (var != null) {
                var.setUsed();
                if (var.getMode() == Mode.CLOCK) {
                    ((Clock)var).incrClkSinks();
                    ((Clock)var).incrCE();
                }
            }
        }
        inputs.add(v);
    }

    /**
     * Add a TDEVar to the output section of a TDE.
     * @param   v is the variable to be appended to the output section
     */
    public void add2o (TDEVar v) {
        if (v != null) {
            v.addSrc(this);
            Var var = v.getVar();
            if (var != null) {
                if ((type != TDEType.CONNECT) && (var != null) && (var.getMode() == Mode.CLOCK))
                    ((Clock)var).setClkSourced();
            }
        }
        outputs.add(v);
    }
    
    /**
     * Finish off a single output TDE.
     * If there is no output parameter, create one and add it.
     * Add the TDE to the TDE list.
     * Return the output signal.
     * @return  the output TDEVar
     */
    public TDEVar finish () {
        TDEVar out;
        if (outputs.size() == 0) {
            out = tdelist.signal(type.name(), null);
            add2o(out);
        } else
            out = outputs.get(0);
        tdelist.addTDE(this);
        return(out);
    }
    
    /**
     * Add a non-array pin to a TDE ELEMENT.
     * Where a signal is already in the correct position in an input or output
     * list the third argument should be null.
     * Parameters are added in the order -
     *      pin name string
     *      type
     *      array format (0)
     * The signal 'v' is added to the input or output list as appropriate in order.
     * @param pin is the pin name
     * @param type is 0 for input, 1 for output, 2 for 3-state output, 3 for clock input
     * @param v is an optional signal
     */
    public void addPin2Element (String pin, int type, TDEVar v) { addPin2Element (pin, type, v, 0, 0); }
    
    /**
     * Add a pin to a TDE ELEMENT.
     * Where a signal is already in the correct position in an input or output
     * list the third argument should be null.
     * Parameters are added in the order -
     *      pin name string
     *      type
     *      array format
     * The signal 'v' is added to the input or output list as appropriate in order.
     * @param pin is the pin name
     * @param type is 0 for input, 1 for output, 2 for 3-state output, 3 for clock input
     * @param v is an optional signal
     * @param size is the size where the port is an array, 0 otherwise
     * @param arrayformat is the array form to use if the port is an array
     */
    public void addPin2Element (String pin, int type, TDEVar v, int size, int arrayformat) {
        add2p(pin);
        add2p(type);
        add2p(size);
        add2p(arrayformat);
        if (v == null)
            return;
        switch (type) {
        case 0:
            add2i(v);
            break;
        case 3:
            add2ic(v);
            break;
        default:
            add2o(v);
        }         
    }
    
    /**
     * Add a property to a TDE ELEMENT.
     * Parameters are added in the order -
     *      property name string
     *      4
     *      property value string
     * @param property is the property name
     * @param value is the property value
     */
    public void addProperty2Element (String property, String value) {
        add2p(property);
        add2p(4);
        add2p(value);
        add2p(0);
    }
    
    /**
     * Change a property of a TDE ELEMENT.
     * If the property is not present, append it.
     * Parameters are in the order -
     *      property name string
     *      4
     *      property value string
     * @param property is the property name
     * @param value is the property value
     */
    public void changePropertyOfElement (String property, String value) {
        for (int i=2 ; i<params.size() ; i+=4) {
            if (((String)params.get(i)).equals(property)) {
                params.set(i, property);
                params.set(i+1, 4);
                params.set(i+2, value);
                return;
            }               
        }
        add2p(property);
        add2p(4);
        add2p(value);
    }
        
    /**
     * Change or add a non-array pin and associated signal to a TDE ELEMENT.
     * The first parameter is the element name and the second is an element
     * netlist identifier or null (one is generated).
     * Parameters follow as triples in the order -
     *      pin name string
     *      type
     *      array format (0)
     * Signals in the input or output list occur in the order of the associated
     * parameter list entries.
     * @param pin is the pin name
     * @param type is 0 for input, 1 for output, 2 for 3-state output, 3 for clock input
     * @param v is the new signal
     */  
    public void setElementPin (String pin, int type, TDEVar v) {
        setElementPin (pin, type, v, 0, 0);
    }
    
    /**
     * Change or add a pin and associated signal to a TDE ELEMENT.
     * The first parameter is the element name and the second is an element
     * netlist identifier or null (one is generated).
     * Parameters follow as quads in the order -
     *      pin name string
     *      type
     *      array size
     *      array format
     * Signals in the input or output list occur in the order of the associated
     * parameter list entries.
     * @param pin is the pin name
     * @param type is 0 for input, 1 for output, 2 for 3-state output, 3 for clock input
     * @param v is the new signal
     * @param arraysize is the size if an array, 0 otherwise
     * @param arrayformat determines the form of an array port
     */  
    public void setElementPin (String pin, int type, TDEVar v, int arraysize, int arrayformat) {
        if (!params.contains(pin)) {
            addPin2Element(pin, type, v, arraysize, arrayformat);
            return;
        }
        int ii = -1;
        int io = -1;
        for (int i=2 ; i<params.size(); i+=4) {
            switch (((Integer)params.get(i+1))) {
            case 0:
            case 3:
                ii++;
                break;
            default:
                io++;
            }
            if (!((String)params.get(i)).equals(pin))
                continue;
            switch (type) {
            case 0:
            case 3:
                inputs.set(ii, v);
                v.addDest(this);
                return;
            default:
                outputs.set(io,  v);
                v.addSrc(this);
                return;
            }
        }
    }
    
    /**
     * Returns the signal associated with a specified pin for an ELEMENT TDE.
     * If the pin is not present, returns null.
     * @param pin is the element pin
     * @return the signal associated with the pin, or null
     */  
    public TDEVar getElementPinSignal (String pin) {
        if (!params.contains(pin))
            return(null);
        int ii = 0;
        int io = 0;
        for (int i=2 ; i<params.size(); i+=4) {
            int type = (Integer)params.get(i+1);
            if (!((String)params.get(i)).equals(pin)) {
                switch (type) {
                case 0:
                case 3:
                    ii++;
                    break;
                case 4:
                    break;
                default:
                    io++;
                }
                continue;
            }
            switch (type) {
            case 0:
            case 3:
                return(inputs.get(ii));
            default:
                return(outputs.get(io));
            }
        }
        return(null); // not really necessary - keeps compiler happy
    }
    
    /**
     * Returns a property String for an ELEMENT TDE.
     * If the property is not present, returns null.
     * @param p is the property
     * @return the value String for the property, or null
     */  
    public String getELementProperty (String p) {
        if (!params.contains(p))
            return(null);
        for (int i=2 ; i<params.size(); i+=4) {
            int type = (Integer)params.get(i+1);
            if (type != 4)
                continue;
            if (!((String)params.get(i)).equals(p))
                continue;
            return((String)params.get(i+2));
        }
        return(null);
    }
    
    /**
     * Add a constraint to the 'current' element via a TDE SPECIAL.
     * @param   c is the constraint name
     * @param   v is the constraint value
     */
    public static void addConstraint (String c, String v) {
        TDE ctde = new TDE(TDEType.SPECIAL);
        ctde.add2p(3);              // special code 3 - add to NCF file
        ctde.add2p((String)null);   // null to specify 'current' element
        ctde.add2p(c);              // constraint name string
        ctde.add2p(v);              // constraint value string
        tdelist.add(ctde);
    }

    /**
     * Compare two EXECP TDEs for identical availability conditions.
     * This is very simplistic - it only checks if the two availability inputs
     * have the same TDEVar, the two priority inputs have the same TDEVar
     * and the two priority output signals have the same TDEVar!
     * @param   tde is the TDE to compare with
     * @return  true if the two EXECP TDEs have the same availability inputs
     */
    public boolean availEquals (TDE tde) {
        // The 1st input is the clock.
        // The 2nd input is the execution signal.
        // The 3rd (subscript 2) is the availability signal or null.
        // The 4th input is the priority output signal or null.
        // The 5th input is the reset signal or null.
        // The 1st output is the delayed start signal.
        // The 2nd output is the priority input signal or null.
        TDEVar tdev1 = inputs.get(2);
        TDEVar tdev2 = tde.inputs.get(2);
        TDEVar tdev_po1 = inputs.get(3);
        TDEVar tdev_po2 = tde.inputs.get(3);
        TDEVar tdev_pi1 = outputs.get(1);
        TDEVar tdev_pi2 = tde.outputs.get(1);
        boolean aveq = (tdev1 == null) && (tdev2 == null) ||
                       (tdev1 != null) && (tdev2 != null) && tdev1.equals(tdev2);
        boolean pieq = (tdev_pi1 == null) && (tdev_pi2 == null) ||
                       (tdev_pi1 != null) && (tdev_pi2 != null) && tdev_pi1.equals(tdev_pi2);
        boolean poeq = (tdev_po1 == null) && (tdev_po2 == null) ||
                       (tdev_po1 != null) && (tdev_po2 != null) && tdev_po1.equals(tdev_po2);
        
        return (aveq && pieq && poeq);
    }

    /**
     * Check for redundant TDEs.
     * <UL>
     * <LI> A SELECT with one data input (leave queue ones alone!)
     * <LI> A logic operator with one input
     * <LI> A parallel WAIT with one input
     * </UL>
     * Instead connect a single input to the output. Return true if the
     * TDE is redundant. It is easier to generate these systematically
     * and then eliminate the trivial cases than to generate them
     * conditionally in the first place.
     *
     * Check for duplicated inputs and combine them with ORed select inputs.
     * Where duplicated data inputs are unrecognised because of intervening CONNECT TDEs
     * these will be resolved later in the final netlist (see XNetlistOptimise.convertSelectors()).
     *
     * This is called in two places -
     * <UL>
     * <LI> TDEList.addTDE()
     * <LI> TDEList.optimise()
     * </UL>
     * The boolean 'deletedinput' has been made a class variable rather than
     * a local variable to retain redundancy information between calls
     * from the above two locations for the same element.
     * @return  true if the TDE is redundant
     */
    public boolean redundant () {
        if (type == TDEType.SELECT) {
            int     i;
            if (!no_delete) {
            // If not a queue or selectvalue input select, remove any
            // GND data inputs.
                for (i=0 ; i<inputs.size() ; ) {
                    TDEVar  vis = getInput(i);
                    TDEVar  vid = getInput(i + 1);
                    boolean inputdelete = false;

                    switch (vid.getType()) {
                    case VAR:
                        if (vid.isGND())
                            inputdelete = true;
                        break;
                    case BITS:
                    case UINT:
                    case INT:
                    case UFIXED:
                    case FIXED:
                    case FLOAT:
                        if (((Long)vid.getConst()).longValue()==0)
                            inputdelete = true;
                        break;
                    case BOOL:
                        if (!((Boolean)vid.getConst()).booleanValue())
                            inputdelete = true;
                        break;
                    case NEG_CLK:
                        break;
                    case POS_CLK:
                        break;
                    }

                    if (inputdelete) {
                        vis.removeDest(this);
                        vid.removeDest(this);
                        inputs.remove(i);
                        inputs.remove(i);
                        deletedinput = true;
                    } else
                        i += 2;
                }
            }
            
            // combine any duplicated data inputs, ORing the select signals
            for (i=0 ; i<inputs.size() ; i+=2) {
                TDEVar  visi = getInput(i);
                TDEVar  vidi = getInput(i + 1);
                for (int j=inputs.size()-2 ; j>i ; j-=2) {
                    TDEVar  visj = getInput(j);
                    TDEVar  vidj = getInput(j + 1);
                    if (vidi.equals(vidj)) {
                        visi.removeDest(this);
                        visj.removeDest(this);
                        TDEVar  tdev = tdelist.or(visi, visj, null);
                        tdev.addDest(this);
                        inputs.set(i, tdev);
                        visi = tdev;
                        inputs.remove(j);
                        inputs.remove(j);
                    }
                }
            }

            if (inputs.size() == 0) {
                // If now has no inputs, connect output to GND.
                // This is a bit dubious. Probably should be a fatal error.
                // It probably never occurs.
                TDEVar vo = getOutput(0);

                vo.removeSrc(this);
                tdelist.connect(vo, TDEVar.GND, true);

                clear();
                return(true);
            }

            // Delete if only has one data input (2 inputs: control + data)
            // but not if -
            //  * we have just deleted one or more zero inputs
            //  * it is a marked queue input selector
            //  * it has an alu flag
            boolean aluflag = false;
            if (params.size() > 1)
                aluflag = ((Boolean)params.get(1)).booleanValue();

            if ((inputs.size() == 2) && !deletedinput && !no_delete && !aluflag) {
                TDEVar vis = getInput(0);
                TDEVar vid = getInput(1);
                TDEVar vo  = getOutput(0);
                
                switch (vid.getType()) {
                case VAR:
                case POS_CLK:
                case NEG_CLK:
                    // simply connect data input to output and
                    // return redundant status
                    vis.removeDest(this);
                    vid.removeDest(this);
                    vo.removeSrc(this);
                    tdelist.connect(vo, vid);
                    clear();
                    return(true);
                default:
                    // is a constant - change TDE to TDEType.CONNECT
                    type = TDEType.CONNECT;
                    vis.removeDest(this);
                    inputs.remove(0);   // get rid of select input
                    params.clear();     // clear any parameters
                    return(false);
                }
            }

            return(false);
        }

        // Remove single input AND, OR or XOR gates, connecting the output to the single input.
        if ((type == TDEType.AND) || (type == TDEType.OR) || (type == TDEType.XOR)) {
            if (inputs.size() == 0)
                throw new ExEx("SYSTEM ERROR: gate with no inputs!");
            if (inputs.size() == 1) {
                TDEVar vi = getInput(0);
                TDEVar vo = getOutput(0);

                vi.removeDest(this);
                vo.removeSrc(this);
                tdelist.connect(vo, vi);

                clear();
                return(true);
            }

            return(false);
        }
        
        // Remove a WAIT with a single input, connecting the output to the single input.
        if ((type == TDEType.WAIT) && (inputs.size() == 1)) {
            TDEVar vi = getInput(0);
            TDEVar vo  = getOutput(0);

            vi.removeDest(this);
            vo.removeSrc(this);
            tdelist.connect(vo, vi);

            clear();
            return(true);
        }

        return(false);
    }

    /**
     * Record a reference to the corresponding simulator object
     * (the SDE parameter) in this TDE.
     * @param sde The SDE to be associated
     */
//    public void registerSDE (SDE sde) {
//    }

    /**
     * Remove a control signal TDEVar from the inputs list and remove
     * this TDE from the TDEVar destination list. It is assumed that the
     * context is such that the variable occurs only once in the list.
     * @param   v is the TDEVar to be removed
     */
    public void removeInput (TDEVar v) {
        int index = inputs.indexOf(v);
        if (index >= 0) {
            inputs.remove(index);
            v.removeDest(this);
        } else {
            for (index=0 ; index<inputs.size() ; index++) {
                TDEVar  tdev = inputs.get(index);
                if (tdev == null)
                    continue; // skip null input
                if (tdev.equalOrLinked(v)) {
                    inputs.remove(index);
                    tdev.removeDest(this);
                    return;
                }
            }
            throw new ExEx("SYSTEM ERROR: TDEVar.removeInput() error");
        }
    }

    /**
     * Ensure all TDEVars in the TDE are resolved to their source TDEVars.
     * @param   tdelist is the TDE list
     */
    /*public void rename (TDEList tdelist) {
        TDEVar  t;
        TDEVar  a;

        for(int i = 0; i < inputs.size(); i++) {
            t = inputs.get(i);
            if ((a = t.getSource()) != null) {
                inputs.set(i, a);
                a.addDest(this);
            } else if (t.getWordSpec() == null)
                t.addDest(this);
        }

        for(int i = 0; i < outputs.size(); i++) {
            t = outputs.get(i);
            if ((a = t.getSource()) != null) {
                outputs.set(i, a);
                a.addSrc(this);
            } else if (t.getWordSpec() == null)
                t.addSrc(this);
        }
    }*/

    /**
     * Replace a TDEVar in the inputs list. It is assumed that the
     * context is such that the old TDEVar occurs only once in the list.
     * @param   old is the TDEVar to be replaced
     * @param   v_new is the new TDEVar
     */
    public void replaceInput (TDEVar old, TDEVar v_new) {
        int index = inputs.indexOf(old);
        inputs.set(index, v_new);
    }

    /**
     * Replace a TDEVar in the inputs list.
     * context is such that the old variable occurs only once in the list.
     * @param   index is the index into the input list
     * @param   v_new is the new TDEVar
     */
    public void replaceInput (int index, TDEVar v_new) { inputs.set(index, v_new); }

    /**
     * Replace a TDEVar in the outputs list. It is assumed that the
     * context is such that the old variable occurs only once in the list.
     * @param   index is the index into the output list
     * @param   v_new is the new TDEVar
     */
    public void replaceOutput (int index, TDEVar v_new) { outputs.set(index, v_new); }
    
    /**
     * If this TDE is active, check the control signal links to TDEVars
     * are doubly linked. If an inconsistency is found, report this and
     * continue.
     */
    public void checkLinks () {
        if (!active)
            return;

        Iterator<TDEVar>    it;

        it = inputs.iterator();
        // take each source TDEVar in turn
        while (it.hasNext()) {
            TDEVar  v1 = it.next();
            if (v1 == null)
                continue;
            // ignore non-control signal TDEVars
            //if ((v1.getWordSpec() != null) || (v1.getType() != TDEVtype.VAR))
            //    continue;
            // check that there is a destination entry for the TDE
            if (!v1.getDestList().contains(this))
                msg("TDE source TDEVar has no link");
            
            // check the TDEVar for link consistency
            //v1.checkLinks();
        }

        it = outputs.iterator();
        // take each destination TDEVar in turn
        while (it.hasNext()) {
            TDEVar  v1 = it.next();
            if (v1 == null)
                continue;
            // ignore non-control signal TDEVars
            //if ((v1.getWordSpec() != null) || (v1.getType() != TDEVtype.VAR))
            //    continue;
            // check that there is a source entry for the TDE
            if (!v1.getSrcList().contains(this))
                msg("TDE destination TDEVar has no link");
            
            // check the TDEVar for link consistency
            //v1.checkLinks();
        }
    }

    /**
     * Print the TDE. More common TDEs are printed with tailored formats to
     * give a more compact listing.
     * @param   dump_loc the source file location
     */
    public void dump (boolean dump_loc) {
        Iterator<?>     it;
        StringBuffer    b;
        TDEVar          tdev;
        Object          o = null;

        if (!active)
            return;
        
        
        switch (type) {
        case AND:
        case OR:
        case XOR:
            b = new StringBuffer();
            b.append(getOutput(0) + " = ");
            for (int i=0 ; i<inputs.size() ; i++) {
                if (i > 0)
                    b.append(" " + type.typename() + " ");
                b.append(getInput(i));
            }
            if (dump_loc && (loc != null))
                icl(b + "\t" + loc);
            else
                icl(b);
            return;

        case CONNECT:
            TDEVar  tdevin = getInput(0);
            TDEVar  tdevout = getOutput(0);
            // IS THIS NECESSARY?
            String      ssi = tdevin.toString();
            String      sso = tdevout.toString();
            if (ssi.equals(sso))
                return;
            
            b = new StringBuffer();
            b.append(sso);
            b.append(" = ");
            b.append(ssi);
            if ((params.size() != 0) && (getParam(0) != null)) {
                if (((Boolean)getParam(0)).booleanValue())
                    b.append(" cast - sign_extend");
                else
                    b.append(" cast - pad");
            }
            if ((params.size() == 0) && (tdevin.numBits() == 1) && (tdevout.numBits() > 1))
                b.append(" expand");
            if (dump_loc && (loc != null))
                b.append("\t" + loc);
            icl(b);
            return;

        case CRAM:
            b = new StringBuffer();
            icl("CRAM - " + getParam(0) +" ports {");
            for (int i=0 ; i<outputs.size() ; i++)
                icl("\tOUT" + i + "\t" + getOutput(i));
            icl("\t<-");
            for (int i=0 ; 4*i<inputs.size() ; i++) {
                icl("\tCLK" + i + "\t" + getInput(4*i));
                icl("\tADDR" + i + "\t" + getInput(4*i+1));
                if (inputs.size() > (4*i+2))
                    icl("\tDATA" + i + "\t" + (((tdev=getInput(4*i+2)) == null) ? "-" : tdev));
                if (inputs.size() > (4*i+3))
                    icl("\tWE" + i + "\t" + (((tdev=getInput(4*i+3)) == null) ? "-" : tdev));
            }
            if ((params.size() > 3) && (getParam(3) != null))
                icl("    initialised");
            if (params.size() > 4) {
                b.append(" {");
                for (int i=4 ; i<params.size() ; ) {
                    b.append(getParam(i++) + "=" + getParam(i++));
                    if (i < params.size())
                        b.append(", ");
                }
                b.append("}");
            }
            b.append("}");
            if (dump_loc && (loc != null))
                b.append("\t\t\t\t" + loc);
            icl(b);
            return;

        case DEL:
            b = new StringBuffer();
            b.append("DEL ");
            b.append(getOutput(0));
            b.append(" <- ");
            b.append(getInput(1));
            b.append(" CLK " + getInput(0));
            if  (getParam(0) != null)
                o = getParam(0);
            if ((o != null) && (o instanceof Integer)) {
                int i = ((Integer)getParam(0)).intValue();
                b.append(" delay " + i);
            }
            if ((inputs.size() > 2) && (getInput(2) != null))
                b.append(" VDEL " + getInput(2));
            if ((inputs.size() > 3) && (getInput(3) != null))
                b.append(" EN " + getInput(3));
            if ((inputs.size() > 4) && (getInput(4) != null))
                b.append(" R " + getInput(4));
            if (params.size() > 1)
                b.append(" initialised");
            if (dump_loc && (loc != null))
                b.append("\t" + loc);
            icl(b);
            return;
        
        case DFF:
            String  init = ((String)getParam(0));
            boolean async = ((Boolean)getParam(1)).booleanValue();
            if (async)
                icl("DFF FDCPE {");
            else
                icl("DFF FDRSE {");
            icl("\tOUT      " + (((tdev=getOutput(0)) == null) ? "-" : tdev));
            icl("\t<-");
            icl("\tD        " + (((tdev=getInput(0)) == null) ? "-" : tdev));
            icl("\tC        " + (((tdev=getInput(1)) == null) ? "-" : tdev));
            icl("\tCE       " + (((tdev=getInput(2)) == null) ? "-" : tdev));
            if (async) {
                icl("\tCLR      " + (((tdev=getInput(3)) == null) ? "-" : tdev));
                icl("\tPRE      " + (((tdev=getInput(4)) == null) ? "-" : tdev));
            } else {
                icl("\tR        " + (((tdev=getInput(3)) == null) ? "-" : tdev));
                icl("\tS        " + (((tdev=getInput(4)) == null) ? "-" : tdev));
            }
            b = new StringBuffer();
            b.append("init = " + init + "\n");
            b.append("}");
           if (dump_loc && (loc != null))
                b.append("\t\t\t\t" + loc);
            icl(b);
            return;
        
        // case DIVERGE - see end of switch statement
        
        case DOWHILE:
            icl("DO WHILE {\n");
            icl("\tSTART_B  " + getOutput(0));
            icl("\tFINISH   " + getOutput(1));           
            icl("\t<-");
            icl("\tSTART    " + getInput(0));
            icl("\tTEST     " + getInput(1));
            icl("\tCONTIN   " + getInput(2));
            icl("\tCLK      " + getInput(3));
            if (inputs.size() > 4)
                icl("\tRESET      " + getInput(4));
            b = new StringBuffer();
            b.append("}");
            if (dump_loc && (loc != null))
                b.append("\t\t\t\t" + loc);
            icl(b);
            return;
            
        case ELEMENT:
            int ii = 0;
            int jj = 0;
            int io = 0;
            b = new StringBuffer();
            b = new StringBuffer();
            b.append("ELEMENT ");
            b.append(getParam(jj++) + " ");
            if (getParam(jj) != null)
                b.append("block " + getParam(jj));
            jj++;
            icl(b);
            while (jj < params.size()) {;
                b = new StringBuffer();
           
                int type = ((Integer)getParam(jj+1)).intValue();
                if (type == 4) {
                    b.append("\tproperty " + getParam(jj) + " = ");
                    b.append((String)getParam(jj+2));
                } else {
                    int size = ((Integer)getParam(jj+2)).intValue();
                    b.append("\tPIN " + getParam(jj));
                    switch (type) {
                    case 0:
                        b.append(" I ");
                        b.append(getInput(ii++));
                        break;
                    case 1:
                        b.append(" O ");
                        b.append(getOutput(io++));
                        break;
                    case 2:
                        b.append(" O3 ");
                        b.append(getOutput(io++));
                       break;
                    case 3:
                        b.append(" C ");
                        b.append(getInput(ii++));
                    }
                    if (size != 0) {
                        b.append(" array size ");
                        b.append(getParam(jj+2));
                        b.append(" array format ");
                        b.append(getParam(jj+3));
                    }
                }
                icl(b);
                jj += 4;
            }
            return;

        case EXECP:
            int etype = (int)params.get(0);
            icl("EXECP " + execp_types[etype] + " {");
            icl("\tSTART_DEL " + getOutput(0));
            if (etype < 2)
                icl("\tPRI_IN    " + getOutput(1));
            if (etype == 3) {
                icl("\tWPENDING   " + getOutput(2));
                icl("\tRPENDING   " + getOutput(3));
            }
            icl("\t<-");
            icl("\tCLK       " + (((tdev=getInput(0)) == null) ? "-" : tdev));
            icl("\tSTART_IN  " + (((tdev=getInput(1)) == null) ? "-" : tdev));
            if (etype != 0)
                icl("\tBQAV      " + getInput(2));
            if (etype == 3) {
                icl("\tUBQWAV    " + getInput(3));
                icl("\tUBQRAV    " + getInput(4));
            }
            if (etype < 2)
                icl("\tPRI_OUT   " + getInput(5));
            if ((tdev=getInput(6)) != null)
                icl("\tRESET     " +  tdev);
            b = new StringBuffer();
            b.append("}");
            if (dump_loc && (loc != null))
                b .append("\t\t" + loc);
            icl(b);
            return;
        
        case IBUF:
            b = new StringBuffer();
            b.append("IBUF  " + getOutput(0));
            if (getOutput(1) != null)
                b.append(", " + getOutput(1));
            b.append(" <- " + getInput(0));
            if (inputs.size() == 2)
                b.append(", " + getInput(1));
            b.append(" loc=");
            b.append((String)getParam(1));
            if (getParam(2) != null) {
                b.append(",");
                b.append((String)getParam(2));
            }
            if (getParam(0) != null)
                b.append(" id " + (String)getParam(0));
            b.append(" IBUF");
            if ((Boolean)getParam(3))
                b.append("G");
            if ((Boolean)getParam(4))
                b.append("DS");
            if (getOutput(1) != null)
                b.append("_DIFF_OUT");
            icl(b);
            return;

        case ILOOP:
            if (inputs.size() < 3) {
                b = new StringBuffer();
                b.append("ILOOP  ");
                b.append(getOutput(0));
                b.append(" <- ");
                b.append(getInput(0));
                b.append(" ");
                b.append(getInput(1));
                if (dump_loc && (loc != null))
                    b.append("\t" + loc);
                icl(b);
                return;
            }
            break;

        case INV:
            b = new StringBuffer();
            b.append(getOutput(0) + " = inv(");
            b.append(getInput(0));
            b.append(")");
            if (dump_loc && (loc != null))
                icl(b + "\t" + loc);
            else
                icl(b);
            return;

        case OBUF:
            b = new StringBuffer();
            b.append("OBUF " + getOutput(0));
            if (getOutput(1) != null)
                b.append(", " + getOutput(1));
            b.append(" <- " + getInput(0));
            if (getInput(1) != null)
                b.append(" _EN=" + getInput(1));
            b.append(" loc=");
            b.append((String)getParam(1));
            if (getParam(2) != null) {
                b.append(",");
                b.append((String)getParam(2));
            }
            if (getParam(0) != null)
                b.append(" id " + (String)getParam(0));
            b.append(" OBUF");
            if ((Boolean)getParam(4))
                b.append("DS");
            icl(b);
            return;

        case OPERATOR:
            b = new StringBuffer();
            b.append("OP ");
            b.append(getOutput(0));
            if (outputs.size() > 1)
                b.append(", " + getOutput(1));
            b.append(" = ");

            if (inputs.size() == 1) {
                // unary operator
                b.append(((TDEOp)getParam(0)).opstring());
                b.append(getInput(0));
            } else if (inputs.size() == 2) {
                // binary operator
                b.append(getInput(0) + " ");
                b.append(((TDEOp)getParam(0)).opstring());
                b.append(" " + getInput(1));
            } else if (inputs.size() == 3) {
                // ternary operator
                b.append(getInput(0) + " ");
                b.append(" ? ");
                b.append(" " + getInput(1));
                b.append(" : ");
                b.append(" " + getInput(2));
            } else {
                // must be addsub or ALU
                b.append(getInput(0) + " ");
                b.append(((TDEOp)getParam(0)).opstring());
                b.append(" " + getInput(1));
                if (getInput(2) != null)
                    b.append(", ADD=" + getInput(2));
                if (getInput(3) != null)
                    b.append(", GATE=" + getInput(3));
                if ((inputs.size() > 4) && (getInput(4) != null))
                    b.append(", CI=" + getInput(4));
            }

            b.append("\t(");
            for (int i=1 ; i<params.size() ; i++) {
                if (i > 1)
                    b.append(", ");
                b.append((Boolean)getParam(i) ? "signed" : "unsigned");
            }
            b.append(")");
            if (dump_loc && (loc != null))
                b.append("\t" + loc);
            icl(b);
            return;

        case PORT:
            b = new StringBuffer();
            b.append("PORT " + getParam(1));
            switch (((Integer)getParam(0)).intValue()) {
            case 0:
                b.append(" input ");
                break;
            case 1:
                b.append(" output ");
                break;
            case 2:
                b.append(" 3state ");
            }
            b.append(getInput(0));
            icl(b);
            return;

        case PRIORITY:
            icl("PRIORITY {");
            icl("\tOUT   " + getOutput(0));
            if (getOutput(1) != null)
                icl("\tELSE  " + getOutput(1));
            icl("\t<-");
            icl("\tIN    " + getInput(0));
            b = new StringBuffer();
            b.append("}");
            if (dump_loc && (loc != null))
                b.append("\t\t\t\t" + loc);
            icl(b);
            return;
        
        case QUEUEBUFFER:
            int j = 0;
            int depth = (Integer)getParam(0);
            if (depth == 0) {
                icl("QUEUEBUFFER  UNBUFFERED {");
                icl("\tOUT      " + (((tdev=getOutput(0)) == null) ? "-" : tdev));
                icl("\tRAVAIL   " + getOutput(1));
                icl("\tWAVAIL   " + getOutput(2));
            } else {
                icl("QUEUEBUFFER  depth " + getParam(0) + " {");
                icl("\tOUT      " + (((tdev=getOutput(0)) == null) ? "-" : tdev));
                icl("\tNE       " + getOutput(1));
                icl("\tNF       " + getOutput(2));
                if ((tdev=getOutput(3)) != null)
                    icl("\tCNT      " + tdev);
                if ((tdev=getOutput(4)) != null)
                icl("\tSPC      " + tdev);
                if ((tdev=getOutput(5)) != null)
                icl("\tWSTAT    " + tdev);
                if ((tdev=getOutput(6)) != null)
                icl("\tRSTAT    " + tdev);
                if (outputs.size() > 7)
                    icl("\tRRESET   " + getOutput(7));
            }
            icl("\t<-");

            if ((tdev=getInput(j++)) == null)
                icl("\tCLK      " + getInput(j++));
            else {
                icl("\tCLKIN    " + (((tdev=getInput(j++)) == null) ? "-" : tdev));
                icl("\tCLKOUT   " + getInput(j++));
            }
            icl("\tDATA     " + (((tdev=getInput(j++)) == null) ? "-" : tdev));
            if (depth == 0) {
                icl("\tWPEND    " + getInput(j++));
                icl("\tRPEND    " + getInput(j++));
            } else {
                icl("\tPUSH     " + getInput(j++));
                icl("\tPOP      " + getInput(j++));
            }
            if (inputs.size() > j)
                icl("\tRESET    " + getInput(j).toString());
            if (params.size() > 3)
                icl("    {0x" + (String)getParam(3) + "}");
            b = new StringBuffer();
            b.append("}");
            if (dump_loc && (loc != null))
                b.append("\t\t\t\t" + loc);
            icl(b);
            return;

        case REG:
            b = new StringBuffer();
            icl("REG");
            icl("\tOUT  " + getOutput(0));
            icl("\t<-");
            icl("\tCLK  " + getInput(0));
            icl("\tD    " + (((tdev=getInput(1)) == null) ? "-" : tdev));
            icl("\tCE   " + (((tdev=getInput(2)) == null) ? "-" : tdev));
            if (inputs.size() == 4)
                icl("\tR    " + getInput(3));
            b.append("    {0x");
            b.append((String)getParam(1));
            b.append("}");
            if ((Boolean)getParam(2))
                b.append(" IOB ");
            if ((Boolean)getParam(3))
                b.append(" DSP ");
            if (getInput(0) == null)
                b.append("\tmake const!");
            if (dump_loc && (loc != null))
                icl(b + "\t" + loc);
            else
                icl(b);
            return;

        case RESYNC:
            b = new StringBuffer();
            icl("RESYNC {");
            icl("\tOUT   " + getOutput(0));
            icl("\t<-");
            icl("\tIN    " + getInput(0));
            icl("\tICLK  " + getInput(1));
            icl("\tOCLK  " + getInput(2));
            b.append("}");
            if (dump_loc && (loc != null))
                b.append("\t\t\t\t" + loc);
            icl(b);
            return;
            
        case RRAM:
            b = new StringBuffer();
            icl("RRAM - " + getParam(0) +" ports {");
            for (int i=0 ; i<outputs.size() ; i++)
                icl("\tOUT" + i + "\t" + (((tdev=getOutput(i)) == null) ? "-" : tdev));
            icl("\t<-");
            for (int i=0 ; 5*i<inputs.size() ; i++) {
                icl("\tCLK" + i + "\t" + getInput(5*i));
                icl("\tADDR" + i + "\t" + (((tdev=getInput(5*i+1)) == null) ? "-" : tdev));
                icl("\tDATA" + i + "\t" + (((tdev=getInput(5*i+2)) == null) ? "-" : tdev));
                icl("\tRE" + i + "\t" + (((tdev=getInput(5*i+3)) == null) ? "-" : tdev));
                icl("\tWE" + i + "\t" + (((tdev=getInput(5*i+4)) == null) ? "-" : tdev));
            }
            if (params.size() > 3)
                icl("    initialised");
            if (params.size() > 4) {
                b.append(" {");
                for (int i=4 ; i<params.size() ; ) {
                    b.append(getParam(i++) + "=" + getParam(i++));
                    if (i < params.size())
                        b.append(", ");
                }
                b.append("}");
            }
            b.append("}");
            if (dump_loc && (loc != null))
                b.append("\t\t\t\t" + loc);
            icl(b);
            return;

        case SAMPLE:
            icl("SAMPLE {");
            icl("\tOUT   " + getOutput(0));
            icl("\t<-");
            icl("\tIN    " + getInput(0));
            icl("\tICLK  " + getInput(1));
            icl("\tOCLK  " + getInput(2));
            b = new StringBuffer();
            b.append("}");
            if (dump_loc && (loc != null))
                b.append("\t\t\t\t" + loc);
            icl(b);
            return;

        case SELECT:
            b = new StringBuffer();
            icl("SELECT {");
            icl("\tOUT  " + getOutput(0));
            icl("\t<-");
            for (int i=0 ; i<inputs.size() ; ) {
                icl("\tSEL  " + getInput(i++));
                icl("\tIN   " + getInput(i++));
            }
            if (params.size() > 0)
                icl("    unselected out 0x" + (String)getParam(0));
            b.append("}");
            if (dump_loc && (loc != null))
                icl(b + "\t" + loc);
            else
                icl(b);
            return;

        case SIG:
            if ((inputs.size() != 1) || (getInput(0).getClockMode() == ClkType.NEG))
                return;
            b = new StringBuffer();
            b.append("SIG\t");
            b.append(getInput(0).toDecString());
            if (dump_loc && (loc != null))
                b.append("\t" + loc);
            icl(b);
            return;
        
        case SPECIAL:
            b = new StringBuffer();
            switch (((Integer)getParam(0)).intValue()) {
            case 0:
                b.append("NCF \"" + (String)getParam(1) + "\"");
                break;
            case 1:
                b.append("XDC \"" + (String)getParam(1) + "\"");
                break;
            }
            if (params.size() > 2)
                b.append(" port " + (String)getParam(2));
            icl(b);
            return;

        case START:
            icl("START { ");
            icl("\tOUT  " + getOutput(0));
            icl("\t<-");
            icl("\tIN   " + getInput(0));
            icl("\tCLK  " + getInput(1));
            b = new StringBuffer();
            b.append("}");
            if (dump_loc && (loc != null))
                b .append("\t\t" + loc);
            icl(b);
            return;

        case TSELECT:
            b = new StringBuffer();
            icl("TSELECT {");
            icl("\tOUT  " + getOutput(0));
            icl("\t<-");
            for (int i=0 ; i<inputs.size() ; ) {
                icl("\tSEL  " + getInput(i++));
                icl("\tIN   " + getInput(i++));
            }
            if (params.size() > 0)
                icl("    unselected out 0x" + (String)getParam(0));
            b.append("}");
            if (dump_loc && (loc != null))
                icl(b + "\t" + loc);
            else
                icl(b);
            return;

        // case WAIT - see end of switch statement
        
        case WHEN:
            b = new StringBuffer();
            icl("WHEN {");
            icl("\tT_START  " + (((tdev=getOutput(0)) == null) ? "-" : tdev));
            icl("\tF_START  " + (((tdev=getOutput(1)) == null) ? "-" : tdev));
            icl("\tFINISH   " + (((tdev=getOutput(2)) == null) ? "-" : tdev));           
            icl("\t<-");
            icl("\tSTART    " + getInput(0));
            icl("\tTEST     " + getInput(1));
            icl("\tT_FINISH " + (((tdev=getInput(2)) == null) ? "-" : tdev));
            icl("\tF_FINISH " + (((tdev=getInput(3)) == null) ? "-" : tdev));
            b.append("}");
            if (dump_loc && (loc != null))
                b.append("\t\t\t\t" + loc);
            icl(b);
            return;

        case WHILE:
            b = new StringBuffer();
            icl("WHILE {");
            icl("\tSTART_B    " + getOutput(0));
            icl("\tFINISH     " + getOutput(1));
            icl("\t<-");
            icl("\tSTART      " + getInput(0));
            icl("\tTEST       " + getInput(1));
            icl("\tCONTIN     " + getInput(2));
            icl("\tC          " + getInput(3));
            if (inputs.size() > 4)
                icl("\tRESET      " + getInput(4));
            b.append("}");
            if (dump_loc && (loc != null))
                b.append("\t\t\t\t" + loc);
            icl(b);
            return;
        
        // For these see below -
        case DIVERGE:
            break;
        case SIMREAD:
            break;
        case SIMVAR:
            break;
        case SIMWRITE:
            break;
        case WAIT:
            break;
        }

        // General dump of above 5 simple cases which do not have specific code
        icl(type.typename() + " {");
        /*it = params.iterator();

        if (it.hasNext())
            icl("    par:");

        while (it.hasNext())
            print(it.next());*/

        it = outputs.iterator();

        while (it.hasNext())
            print(it.next());
        
        icl("<-");

        it = inputs.iterator();

        while (it.hasNext())
            print(it.next());
        
        if (dump_loc && (loc != null))
            icl("}" + "\t" + loc);
        else
            icl("}");
    }

    /**
     * Generate EDIF code from the TDE.
     * @param   family is 
     */
    public void ncode (TDECode family) {
        switch (type) {
        case SIG:
            // Declare signals.
            // Params:
            //      none
            // Inputs:
            //      input
            //      input
            //      .
            //      .
            // Outputs:
            //      none
            // There may be any number of inputs. Each input
            // may be a single signal or an array. Declarations
            // are created which will be dimensioned if arrays.
            return; // ignore in EDIF
        case CONNECT:
            // Connect signals.
            // Params:
            //      Boolean optional pad/sign_extend
            // Inputs:
            //      input array
            // Outputs:
            //      output array
            // may be -
            //  single output = single input
            //  multiple output = single input
            //  multiple output = constant
            //  multiple output = multiple input     (same size!)
            // The input should be a source and the output a sink.
            TDEVar      si = getInput(0);
            TDEVar      so = getOutput(0);
            if (si.equals(so))
                return;
            family.TDECONNECT(params, inputs, outputs);
            break;
        case SELECT:
            // A selector.
            // Params:
            //      unselected out  - output state when no input selected
            //      use ALU         - use an ALU for multiple arithmetic operators
            //      DSP             - use a DSP block for an ALU
            // Inputs:
            //      select signal
            //      input data array
            //      .
            //      .
            // Outputs:
            //      output data array
            // The inputs are in pairs. The first of each pair is an
            // input signal which selects the second of the pair (a signal
            // array) to be connected to the output signal array.
            // The input arrays may be smaller than the output array.
            family.TDESELECT(params, inputs, outputs);
            return;
        case TSELECT:
            // A 3-state driver.
            // Params:
            //      unselected out  - output state when no input selected
            // Inputs:
            //      input data array
            //      enable signal (+ve enable, -ve high-Z)
            // Outputs:
            //      output data array
            // The input and output arrays must be the same size.
            family.TDETSELECT(params, inputs, outputs);
            return;
        case REG:
            // A static register.
            // Params:
            //      String  initial value
            //      String  optional attribute key
            //      String  optional data for attribute key
            //      ...     other attribute pairs
            // Inputs:
            //      clock signal (may be null if .D null)
            //      input data array (.D - may be null)
            //      clock enable signal array (.CE)
            //      reset signal array (optional)
            // Outputs:
            //      output data array
            // If the clock is null, there are no inputs to
            // this static so generate a constant instead.
            // The data input must be the same width as the output.
            // The clock enable and reset inputs may be arrays in which case
            // they must be the same width as the input and output. Alternatively
            // they may both be width 1 in which case all bits will share the
            // same clock enable and reset. Either or both may be null.
            // The 2nd and following parameters are key/value attribute pairs.
            // These can only be "timingname" and one or more "loc".
             family.TDEREG(params, inputs, outputs);
            return;
        case WAIT:
            // A parallel wait.
            // Params:
            //      none
            // Inputs:
            //      clock signal
            //      reset signal or null
            //      input signal
            //      .
            //      .
            // Outputs:
            //      output signal
            // There may be any number of inputs. Each input is a finish
            // signal. If all input signals go high simultaneously the
            // output signal also goes high, otherwise the output signal
            // goes high when the last input signal goes high. The output
            // signal is high for one clock cycle.
            family.TDEWAIT(inputs, outputs);
            return;
        case DIVERGE:
            // A queue diverge.
            // Params:
            //      none
            // Inputs:
            //      clock signal
            //      reset signal
            //      queue available signal
            //      module queue acknowledge signal
            //      module queue acknowledge signal
            //      .
            //      .
            // Outputs:
            //      queue acknowledge signal
            //      module queue available signal
            //      module queue available signal
            //      .
            //      .
            // This splits the source queue .NE and .POP signals
            // into a pair per destination module.
            // The 1st input is the clock for the queue.
            // The 2nd input is a reset which is asserted if the source queue
            // is reset.
            // The 3rd input is the .NE signal from the queue.
            // The 1st output signal is the .POP signal to the queue.
            // The 4th input is the .POP signal from the 1st module.
            // The 2nd output is the .NE signal to the 1st module.
            // Further input and output signals to other modules
            // may follow.
            // If there is only one module the .NE and .POP signals
            // should be just connected through.
            // If all destination acknowledge signals go high simultaneously the
            // acknowledge signal to the source also goes high, otherwise it goes
            // high when the last destination acknowledge signal goes high. The
            // destination acknowledge signal is high for one clock cycle. When each
            // destination acknowledge signal goes high the matching destination
            // availability signal will go low at the next clock cycle unless all
            // destination modules have also acknowledged in which case destination
            // avaiulability signals will be the be the same as the source
            // availability signal.
            family.TDEDIVERGE(inputs, outputs);
            return;
        case INV:
            // A single signal inverter
            family.TDEINV(inputs, outputs);
            return;
        case AND:
            // An AND gate for control signals.
            // Params:
            // Inputs:
            //      input signal
            //      .
            //      .
            // Outputs:
            //      output signal
            // There may be any number of inputs.
            family.TDEAND(inputs, outputs);
            return;
        case OR:
            // An OR gate for control signals.
            // Params:
            // Inputs:
            //      input signal
            //      .
            //      .
            // Outputs:
            //      output signal
            // There may be any number of inputs.
            family.TDEOR(inputs, outputs);
            return;
        case XOR:
            // An XOR gate for control signals.
            // Params:
            // Inputs:
            //      input signal
            //      .
            //      .
            // Outputs:
            //      output signal
            // There may be any number of inputs.
            family.TDEXOR(inputs, outputs);
            return;
        case OPERATOR:
            // An operator.
            // Params:
            //      Integer operator code
            //      Integer 1st operand type
            //      Integer 2nd operand type if binary or ternary
            //      Integer 3rd operand type if ternary
            //      Integer result type
            // Inputs:
            //      1st operand array
            //      2nd operand array if binary or ternary
            //      3rd operand array if ternary or control input for opADDSUB
            //      second control input for opADDSUB
            // Outputs:
            //      1st or only output array
            //      2nd output array for opDIVREM
            // The 2nd or 3rd Integer and String parameters are omitted
            // where appropriate.
            //
            // opADDSUB has additional inputs - its format is
            //  Params:
            //      opADDSUB
            //      1st operand type
            //      2nd operand type
            //      result type
            //  Inputs:
            //      1st operand array
            //      2nd operand array
            //      add/subtract control (high for add)
            //      pass 2nd operand control (2nd operand 0 if this is low)
            //      XOR to carry in
            //  Outputs:
            //      result array
            //
            // opDIVREM has an additional parameter - its format is
            //  Params:
            //      opDIVREM
            //      1st operand type
            //      2nd operand type
            //      1st result type
            //      2nd result type
            //  Inputs:
            //      1st operand array
            //      2nd operand array
            //  Outputs:
            //      quotient array
            //      remainder array
            //
            family.TDEOPERATOR(params, inputs, outputs);
            return;
        case EXECP:
            // A queue wait.
            // Params:
            //      none
            // Inputs:
            //      clock signal
            //      start signal
            //      queue availability signal
            //      optional reset signal or null
            // Outputs:
            //      delayed start signal
            // If the queue availability signal is high the start signal
            // passes straight through to the delayed start signal output.
            // If the queue availability signal is low the start signal
            // is delayed until the queue availability signal is high.
            family.TDEEXECP(params, inputs, outputs);
            return;
        case ILOOP:
            // An infinite loop.
            // Params:
            //      none
            // Inputs:
            //      start signal
            //      block finish signal
            // Outputs:
            //      block start signal
            // The block start signal is just the OR of the start signal,
            // and the block finish signal.
            family.TDEILOOP(inputs, outputs);
            return;
        case START:
            // Generate a start signal for an execution stream.
            // Params:
            //      none
            // Inputs:
            //      start level signal
            //      start level clock signal
            //      output clock signal
            // Outputs:
            //      output start pulse signal
            // The start may be a pulse or a level. When the start signal
            // rises the rise is captured by the next start clock edge and an
            // output pulse of one period duration is generated on the next
            // output clock edge. The logic is locked after this pulse so that
            // any further start signal rises are ignored.
            family.TDESTART(inputs, outputs);
            return;
        case DEL:
            // A delay.
            // Params:
            //      Integer number of clock cycles
            // Inputs:
            //      clock signal
            //      input signal
            //      optional reset signal or null
            // Outputs:
            //      output signal
            family.TDEDEL(params, inputs, outputs);
            return;
        case WHEN:
            // A WHEN statement.
            // Params:
            //      none
            // Inputs:
            //      start signal
            //      test signal
            //      true block finish signal or null
            //      false block finish signal or null
            // Outputs:
            //      true block start signal
            //      false block start signal
            //      WHEN finish signal or null
            // A missing true or false block will have the block
            // start signal connected back to the associated block
            // finish signal via a 1-cycle delay (TDEType.DEL).
            // If both the true block and the false block require a single
            // TDEType.DEL for their finish signals these will be optimised to
            // a separate common TDEType.DEL from the start signal to the finish
            // signal. In that case -
            //      true block finish signal
            //      false block finish signal
            //      WHEN finish signal
            // arguments to the TDEType.WHEN will all be null.
            family.TDEWHEN(inputs, outputs);
            return;
        case WHILE:
            // A WHILE statement.
            // Params:
            //      none
            // Inputs:
            //      start signal, possibly delayed by queues
            //      test signal to select loop entry
            //      continuation signal from the body, possibly delayed by queues
            //      clock
            //      optional reset signal or null
            // Outputs:
            //      start signal for the body
            //      finish signal for the while
            // The 'continuation signal' from the body is its finish signal
            // possibly delayed by a queue wait on the test.
            family.TDEWHILE(inputs, outputs);
            return;
        case DOWHILE:
            // A DO WHILE statement.
            // Params:
            //      none
            // Inputs:
            //      start signal
            //      test signal (high to continue loop)
            //      block finish delayed signal
            //      clock
            //      optional reset signal or null
            // Outputs:
            //      start block signal
            //      loop finish signal
            // The 'block finish delayed signal' is the block finish signal
            // possibly delayed by a queue wait (TDEType.EXECP).
            family.TDEDOWHILE(inputs, outputs);
            return;
        case PRIORITY:
            // A priority encoder.
            // Params:
            //      none
            // Inputs:
            //      input signal 0
            //      input signal 1
            //      .
            //      .
            // Outputs:
            //      output signal 0
            //      output signal 1
            //      .
            //      .
            // The number of outputs is equal to the number of inputs or the
            // number of inputs + 1. In the latter case the last output is
            // the 'else' case, i.e. no inputs high. When one or more inputs
            // are high the highest priority matching output signal will be
            // high and all other outputs low. The inputs are ordered in
            // decreasing priority (the 1st is highest).
            family.TDEPRIORITY(inputs, outputs);
            return;
        case RESYNC:
            // a pulse or level resynchroniser
            // Params:
            //      none
            // Inputs:
            //      input signal
            //      input clock signal
            //      output clock signal
            // Outputs:
            //      output signal
            // The input may be a pulse or a level. When the
            // input rises the rise is captured by the next input clock edge
            // and an output pulse of one period duration is generated on the
            // next output clock edge.
            family.TDERESYNC(inputs, outputs);
            return;
        case SAMPLE:
            // sample a value in a different clock domain
            // Params:
            //      Boolean input clock frequency > output clock frequency
            // Inputs:
            //      input data array
            //      input clock signal
            //      output clock signal
            // Outputs:
            //      output data array
            family.TDESAMPLE(params, inputs, outputs);
            return;
        case QUEUEBUFFER:
            // A queue buffer.
            // Params:
            //      Integer depth
            //      Boolean - true if hardware FIFO requested or null
            //      Boolean - true if extra output register requested or null
            //      String  initial value (hexadecimal string) or null
            // Inputs:
            //      input clock signal, or null if same as output clock
            //      output clock signal
            //      input data array
            //      write signal signal (.PUSH)
            //      output acknowledge signal (.POP)
            //      reset signal    (optional)
            // Outputs:
            //      output data array
            //      output available signal (.NE)
            //      register available signal (.NF)
            //      synchronous buffer occupancy count or null
            //      synchronous buffer space count or null
            //      asynchronous buffer write status
            //      asynchronous buffer read status
            // A normal queue buffer has a depth of 2.
            // If the depth is > 2, an initial value is not allowed.
            // If a separate input clock is supplied, depths start
            // at 16 and are various powers of 2 depending on the
            // size ranges of the block RAMs in the particular FPGA.
            // If the input data array and output data array arguments are
            // null then this is a null data queue buffer and the buffer
            // depth is a power of 2 greater than 3.
            //
            // If the 4th output is not null this gives the
            // number of words in a synchronous queue buffer
            // synchronised to the queue clock (read and write).
            //
            // If the 5th output is not null this gives the
            // number of free spaces in a synchronous queue buffer
            // synchronised to the queue clock (read and write).
            //
            // If the 6th output is not null this gives the
            // buffer status for an asynchronous queue buffer
            // synchronised to the write (input) clock.
            //
            // If the 7th output is not null this gives the
            // buffer status for an asynchronous queue buffer
            // synchronised to the read (output) clock.
            //
            // If there is an 8th output this is a reset signal. This
            // signal goes high for one clock cycle when the queuebuffer is
            // reset via the reset() inbuilt procedure. For an asynchronous
            // queuebuffer this signal is synchronised to the read (output)
            // clock and is timed to follow the reset of the input (write)
            // side of the queuebuffer.
            //
            // The asynchronous queue buffer status is a 5-bit word
            // whose bits are -
            //      bit 0   Empty to 1/4 Full
            //      bit 1   1 word to 1/2 Full
            //      bit 2   1/4 full to 3/4 Full
            //      bit 3   1/2 Full to Full
            //      bit 4   3/4 Full to Full
            //
            //
            // Synchronous data queue buffers have a default depth of 2.
            // Synchronous null queue buffers have a default depth of 16.
            // Asynchronous data queue buffers have a default depth of 16.
            // Asynchronous null queue buffers have a default depth of 16.
            // Null queue buffers may have a depth which is 4 or greater
            // and which is a power of 2.
            // Data queue buffers may have the following depths -
            //      Virtex - 16, 256, 512, 1024, 2048 or 4096
            //      Virtex-II - 16, 512, 1024, 2048 or 4096, 8192 or 16384
            // and for synchronous buffers depth 2 is also allowed and is
            // the default.
            //
            family.TDEQUEUEBUFFER(params, inputs, outputs);
            return;
        case IBUF:
            // An input external signal.
            // Params:
            // The parameter list contains the following parameters -
            // <ul>
            // <li> String  - optional element identifier
            // <li> String  - element package pin
            // <li> String  - 2nd element package pin for differential input, or null
            // <li> boolean - ibufg indicator
            // <li> boolean - differential indicator
            // </ul>
            // Inputs:
            //      signal from pad to IBUF
            // Outputs:
            //      output signal from IBUF
            // The parameters are key/value attribute pairs.
            family.TDEIBUF(params, inputs, outputs);
            return;
        case OBUF:
            // An output external signal.
            // Params:
            //      String  optional attribute key
            //      String  optional data for attribute key
            //      ...     other attribute pairs
            // Inputs:
            //      data to external output
            //      3-state enable signal (+ve for high-Z) (optional)
            // Outputs:
            //      none
            // Note that the enable is low to enable and high for
            // high impedance!
            // The parameters are key/value attribute pairs.
            family.TDEOBUF(params, inputs, outputs);
            return;
        case DFF:
            // A D-type flip-flop.
            // Params:
            //      Boolean initial value or null   (optional)
            //      Boolean false or omitted if set and reset are synchronous
            //              true if set and reset are asynchronous
            //      String  optional timing group identifier
            //      String  optional further constraint
            //      .       .
            // Inputs:
            //      data_in signal, or null
            //      clock signal, or null
            //      clock enable signal, or null
            //      reset/clear signal, or null
            //      set/preset signal, or null
            // Outputs:
            //      data_out signal
            family.TDEDFF(params, inputs, outputs);
            return;
        case CRAM:
            // Combinatorial-output RAM.
            //
            // Params:
            //      Integer     number of ports
            //      Integer     data width in bits
            //      Integer     address width in bits
            //      String[]    initialisation (optional)
            //      String      optional attribute key
            //      String      optional data for attribute key
            //      ...         other attribute pairs
            // Inputs:
            //      clock signal                port0
            //      address array               port0
            //      data_in array, or null      port0
            //      write signal, or null       port0
            //      clock signal                port1
            //      address array               port1
            //      data_in array, or null      port1
            //      write signal, or null       port1
            //      ..etc..
            // Outputs:
            //      data_out array, or null     port0
            //      data_out array, or null     port1
            //
            // The 2nd parameter gives the data width and the address
            // width gives the memory depth (number of words). All
            // addresses must be the same width and all data
            // inputs and outputs must be the same width.
            // The initialisation string array has one entry for each initial
            // value. Each entry is a hexadecimal string. The initialisation
            // array may be smaller than the depth of the RAM but not larger.
            // The initialisation parameter may be omitted in which
            // case the RAM is explicitly initialised to all 0.
            // Ports which are not written have null entries for the data in
            // and the write enable. Ports which are not read have null
            // entries for the data out.
            // The 5th and following parameters are key/value attribute pairs.
            // These can only be one or more "loc".
            family.TDECRAM(params, inputs, outputs);
            return;
        case RRAM:
            // Registered-output RAM.
            //
            // Params:
            //      Integer     number of ports
            //      Integer     memory data width in bits
            //      Integer     memory addres width in bits
            //      String[]    initialisation (optional)
            //      String      optional attribute key
            //      String      optional data for attribute key
            //      ...         other attribute pairs
            // Inputs:
            //      clock signal                port0
            //      address array               port0
            //      data_in array, or null      port0
            //      read signal, or null        port0
            //      write signal, or null       port0
            //      clock signal                port1
            //      address array               port1
            //      data_in array, or null      port1
            //      read signal, or null        port1
            //      write signal, or null       port1
            //      ..etc..
            // Outputs:
            //      data_out array, or null     port0
            //      data_out array, or null     port1
            //
            // The 2nd parameter gives the memory data width and the address
            // width gives the memory depth (number of words). All
            // addresses must be the same width and all data
            // inputs and outputs must be the same width.
            // The initialisation string array has one entry for each initial
            // value. Each entry is a hexadecimal string. The initialisation
            // array may be smaller than the depth of the RAM but not larger.
            // The initialisation parameter may be omitted in which
            // case the RAM is explicitly initialised to all 0.
            // Ports which are not written have null entries for the data in
            // and the write enable. Ports which are not read have null
            // entries for the data out and the read enable.
            // The parameter section may contain additional trailing entries
            // which are constraint strings. These will be applied to all
            // component blocks implementing the TDE.
            // The 5th and following parameters are key/value attribute pairs.
            // These can only be "timingname", "writemode" and one or more "loc".
            family.TDERRAM(params, inputs, outputs);
            return;
        case ELEMENT:
            // Add an element to the EDIF file.
            // Params:
            //  String  the element name
            //  String  optional netlist block name (or null)
            // Inputs:
            //  none
            // Outputs:
            //  none
            family.TDEELEMENT(params, inputs, outputs);
            return;
        case PORT:
            // an external port
            // Params:
            //  Integer 0 for input, 1 for output, 2 for three-state output
            //  string  port name
            // Inputs:
            //  signal or signal array
            // Outputs: 
            //  none
            family.TDEPORT(params, inputs);
            return;
        case SPECIAL:
            // Special functions.
            // The first parameter gives the function.
            //
            // add line to UCF
            //     Params:
            //          0
            //          String  line to add to the UCF file
            //     Inputs:
            //          signal  optional signal
            //     Outputs:
            //     If an input signal is supplied then "NET " followed by
            //     the signal identifier and a space will be prepended
            //     to the line.
            //
            // add line to NCF
            //     Params:
            //          1
            //          String  line to add to the NCF file
            //     Inputs:
            //          signal  optional signal
            //     Outputs:
            //     If an input signal is supplied then "NET " followed by
            //     the signal identifier and a space will be prepended
            //     to the line.
            //
            // add UCF line for element
            //     Params:
            //          2
            //          String  line to add to the UCF file
            //     Inputs:
            //     Outputs:
            //     The line is prepended with "INST" and the current element
            //     identifier (see TDEType.ELEMENT above).
            //
            // add NCF line for element
            //     Params:
            //          3
            //          String  line to add to the NCF file
            //     Inputs:
            //     Outputs:
            //     The line is prepended with "INST" and the current element
            //     identifier (see TDEType.ELEMENT above).
            //
            // a delay line
            //     Params:
            //          4
            //          Integer number of clock cycles to delay if constant,
            //                  else 0
            //          String  1st initial word in line as a hexadecimal string
            //          .       2nd
            //          .       3rd
            //          . etc
            //     Inputs:
            //          clock signal
            //          input signal array
            //          delay signal array  number of clock cycles to delay if
            //                              variable, else null
            //          enable signal       delay steps if high - null for
            //                              every cycle
            //          reset or null
            //     Outputs:
            //          output signal array
            //    
            //     If the variable delay is used, this value selects the point
            //     in the delay line from which the output is taken. 0 selects
            //     the output of the 1st delay element and 15 the output of the
            //     16th delay element. Note that changing this value will result
            //     in data being skipped (decreasing) or repeated (increasing).
            //    
            //     If there is initialisation data the number of words will
            //     be equal to the delay line length.
            //     If there is no initialisation data these parameters will
            //     be omitted.
            //    
            //     To comply with CCGL syntax, trailing null input arguments are
            //     omitted.
            //
            // unused
            //     Params:
            //          5
            //     Inputs:
            //     Outputs: 
            //          
            // an XC2VP GT_CUSTOM
            //     Params:
            //          6
            //          .
            //          .
            //     Inputs:
            //          see manual
            //     Outputs:
            //          see manual
            //
            // an XC2VP GT_AURORA
            //     Params:
            //          7
            //          .
            //          .
            //     Inputs:
            //          see manual
            //     Outputs:
            //          see manual
            //
            // an XC5V GTP_PAIR
            //     Params:
            //          8
            //          .
            //          .
            //     Inputs:
            //          see manual
            //     Outputs:
            //          see manual
            //
            //
            // an XC5V DSP48E
            //     Params:
            //          9
            //          .
            //          .
            //     Inputs:
            //          see manual
            //     Outputs:
            //          see manual
            //
            family.TDESPECIAL(params, inputs, outputs);
            return;
        case SIMREAD:
            // Read a line from a file during simulation.
            // Params:
            //  string  file path name
            // Inputs:
            //  signal  clock
            //  signal  execution signal for the read
            // Outputs: 
            //  signal array driven from input data line
            //  .
            //  .
            //
            // This element is only used during simulation and is ignored
            // with a warning message if encountered during code generation.
            // When the 2nd input signal is high, one line is read from the
            // file, is decoded according to the type of the output signal
            // arrays and the outputs are driven using the decoded values.
            msg("\nwarning - call to inbuilt procedure simread() in generated code!");
            return;
        case SIMWRITE:
            // Write a line to a file during simulation.
            // Params:
            //  string  file path name
            // Inputs:
            //  signal  clock
            //  signal  execution signal for the write
            //  signal array to be written to the data line
            //  .
            //  .
            // Outputs:
            //  none
            //
            // This element is only used during simulation and is ignored
            // with a warning message if encountered during code generation.
            // When the 2nd input signal is high, one line is written to the
            // file, each input signal array contributing one number to the
            // line.
            msg("\nwarning - call to inbuilt procedure simwrite() in generated code!");
            return;
        case SIMVAR:
            // A variable reference (only used by the simulator).
            // Params:
            //  Var class  variable
            // Inputs:
            //  none
            // Outputs:
            //  none
            // If the simulator is being used one of these elements is placed
            // on the list for every target variable.
            return;
        }
        return;
    }

    private static void print (Object o) {
        if (o == null)
            icl("        null");
        else if (o instanceof String)
            icl("        \"" + (String)o + "\"");
        else if (o instanceof Integer)
            icl("        " + ((Integer)o).toString());
        else if (o instanceof TDEVar)
            icl("        " + o);
    }

/*
    private void simple_call (
        String       function, 
        StringBuffer b
    ) {
        Iterator    it;
        TDEVar      tdev;

        b.append("\t");

        if (outputs.size() == 1)
            b.append(getOutput(0) + " = ");

        b.append(function);
        
        if (params.size() != 0) {
            b.append("(");
            it = params.iterator();
            while (it.hasNext()) {
                Object  o = it.next();
                if (o instanceof String) {
                    b.append("\"");
                    b.append(o);
                    b.append("\"");
                } else if (o instanceof Double) {
                    b.append("\"");
                    b.append(o);
                    b.append("\"");
                } else
                    b.append(o);
                if (it.hasNext())
                    b.append(",");
            }
            b.append(")");
        }
        
        b.append("{");
        if (outputs.size() > 1) {
            it = outputs.iterator();
            while (it.hasNext()) {
                if ((tdev=(TDEVar)it.next()) != null)
                    b.append(tdev);
                if (it.hasNext())
                    b.append(",");
            }
            b.append(" <- ");
        }

        it = inputs.iterator();
        while (it.hasNext()) {
            if ((tdev=(TDEVar)it.next()) != null)
                b.append(tdev);
            if (it.hasNext())
                b.append(",");
        }
        b.append("}");
    }

    private void simple_op (
        TDEVar          result, 
        String          operator, 
        StringBuffer    b
    ) {
        b.append(result + " = ");
        b.append("(");
        b.append(getInput(0));
        b.append(" ");
        b.append(operator);
        b.append(" ");
        b.append(getInput(1));
        b.append(")");
    }
    */
}

