package threepl.exec;

import static threepl.ThreePL.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import threepl.codegen.TDE;
import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.netlist.TDECode;
import threepl.nodes.Ident;
import threepl.nodes.VarNode;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * This class represents a CMEMORY or RMEMORY variable. If the variable is
 * a module, procedure or function parameter it may have an indirect
 * pointer to another Var, depending on its mode and whether input or
 * output. It also has an expanded name to ensure that this instance is
 * unique.
 *
 * The following special cases apply -
 * <UL>
 * <LI> If isInPar is true then this variable is a module, procedure
 *      or function input parameter.
 * <LI> If isOutPar is true then this variable is a module or procedure
 *      output parameter.
 * </UL>
 */
public final class Memory extends Var implements Constant, TDEConstants {
    private int                 ports;      // number of ports
    private boolean[]           readable;   // which ports are readable
    private boolean[]           writable;   // which ports are writable
    private Type                atype;      // address type
    private Type                dtype;      // data type
    private WordSpec            memaws;     // bits: WordSpec for address
    private WordSpec            memdws;     // bits: WordSpec for data
    private TDEVar[]            outvars;    // data output signals for ports
    private ArrayList<Object>[] reads;      // port reads
    private ArrayList<TDEVar>[] writes;     // port writes
    private Clock[]             mem_clk_var;// memory clock variables
    private Val                 targ_init;  // target variable initialisation

    /**
     * Construct a CMEMORY or RMEMORY mode variable.
     * @param   ident is the memory identifier
     * @param   registered is true if the RAM output is registered
     * @param   ports is the number of ports
     * @param   dtype is the input and output data type
     * @param   atype is the address type
     * @param   in_par is true if this is a module, procedure of function
     *          input parameter
     * @param   out_par is true if this is a module or procedure output
     *          parameter
     * @param   init is a compound initialisation value
     * @param   loc is the source file location
     */
    @SuppressWarnings("unchecked")
    public Memory (
        Ident       ident,
        boolean     registered,
        int         ports,
        Type        dtype,
        Type        atype,
        boolean     in_par,
        boolean     out_par,
        Val         init,
        SrcLoc      loc
    ) {
        super(ident, in_par, out_par, null, Ptype.NONE, loc);
        String  mode_msg = (registered) ? "rmemory" : "cmemory";
        
        if (registered) {
            if (!getFamily().allowRMemory())
                throw new ExEx("rmemory mode variable cannot be implemented in this device", loc);
            mode = Mode.RMEMORY;
        } else {
            if (!getFamily().allowCMemory())
                throw new ExEx("cmemory mode variable cannot be implemented in this device", loc);
            mode = Mode.CMEMORY;
        }
        //if (out_par)
        //    throw new ExEx(c + "memory mode variable cannot be an output parameter", loc);
        
        if ((in_par || out_par) && (ports <= 0)) // ports may be 0 for a parameter variable
            return;

        if (ports < 1)
            throw new ExEx(mode_msg + " variable '" + name + " must have 1 or more ports", loc);
        if (dtype == null)
            throw new ExEx(mode_msg + " variable '" + name + " must have a data type", loc);
        if (atype == null)
            throw new ExEx(mode_msg + " variable '" + name + " must have an address type", loc);

        this.ports = ports;
        readable = new boolean[ports];
        writable = new boolean[ports];
        if (registered) {
            for (int i=0 ; i<ports ; i++) {
                readable[i] = getFamily().rramReadable(i, loc);
                writable[i] = getFamily().rramWritable(i, loc);
            }
        } else
            for (int i=0 ; i<ports ; i++) {
                readable[i] = getFamily().cramReadable(i, loc);
                writable[i] = getFamily().cramWritable(i, loc);
            }
        targ_init = init;
        this.dtype = dtype;
        this.atype = atype;
        if (ports < 0)
            throw new ExEx("RAM '" + name + "' cannot have -ve # of ports", loc);
        if (ports > 0) {
            int p = registered ?
                        getFamily().rramPorts(loc) :
                        getFamily().cramPorts(loc);
            if (ports > p)
                throw new ExEx(mode_msg + " '" + name + "' limited to " + p + " ports", loc);
            
            mem_clk_var = new Clock[ports];
            outvars = new TDEVar[ports];
            reads = new ArrayList[ports];
            writes = new ArrayList[ports];
            memaws = atype.getWordSpec(this, loc);
            memdws = dtype.getWordSpec(this, loc);
            wordspec = memdws;
            for (int j=0 ; j<ports ; j++) {
                outvars[j]  = tdelist.namesignal(ename + ccsep + j, memdws, loc);
                reads[j] = new ArrayList<Object>();
                writes[j] = new ArrayList<TDEVar>();
            }
        }

        int     awidth = atype.numBits();
        boolean crom = (mode == Mode.CMEMORY) && (ports == 1) && (writes[0].size() == 0);
        int awmax = (mode == Mode.RMEMORY) ?
                        getFamily().rramAwidthMax(ports, loc) :
                        getFamily().cramAwidthMax(ports, crom, loc);
        if (awidth > awmax)
            throw new ExEx(mode_msg + " variable '" + name + "' address too wide (" + awmax + " bits max)", loc);

        // add this to the variable queue for processing at the end
        queueVar(this);
    }
    
    /**
     * Initialise the memory mode variable.
     * @param v is the initial value
     * @param loc is the source file location
     */
    private void setInitVar (Val v, SrcLoc loc) {
        if ((v.getMode() != Mode.IMMEDIATE) &&
            ((v.getMode() != Mode.VALUE) || (v.getVals() == null)))
            throw new ExEx("memory variable '" + name + "' initialiser is target mode", loc);
        if (v.isPrimitive())
            throw new ExEx("memory variable '" + name + "' initialiser is primitive type", loc);
        targ_init = v;
        
    }
    
    /**
     * Set the attributes map.
     * @param   val is the attributes map value
     * @param   loc is the source file location
     */
    @SuppressWarnings("unchecked")
    public void setAttributes (Val val, SrcLoc loc) {
        if (indass) {
            indirect.setAttributes(val, loc);
            return;
        }
      
        TreeMap<String, Val>    attr = (TreeMap<String, Val>)val.getVal(0);
        String                  c = (mode == Mode.CMEMORY) ? "c" : "r";

        for (String mkey: attr.keySet()) {
            Val     mval = attr.get(mkey);
            Clock   clockvar = null;
            mkey = mkey.toLowerCase();
            
            // Check for "init" attribute before calling checkAttribute() below since that
            // method cannot handle arbitrary types.
            if (mkey.equals("init")) {
                setInitVar (mval, loc);
                continue;
            }

            // Check that attribute is known to 3PL.
            // If it checks out OK, continue processing.
            // If it does not check out skip the rest of the loop.
            if (!checkAttribute(mkey, mval, mode, loc))
                throw new ExEx(c+"memory variable '" + name + "' attribute unknown", loc);
            
            if (mkey.startsWith("domain")) {
                clockvar = getCurrentClockVar();
                if (mval.getPrimType() != Ptype.NULL) {
                    if (mval.getMode() != Mode.CLOCK)
                        throw new ExEx(c+"memory variable '" + name + "' domain variable not clock mode", loc);
                    if (mval.getPrimType() != Ptype.NULL)
                        clockvar = (Clock)mval.getVar();
                }
            }
            if (mkey.equals("domain")) {
                for (int i=0 ; i<ports ; i++) {
                    Clock   cv = getMemClkVar(i);
                    if (cv == null)
                        setMemClkVar(i, clockvar);
                    else if (!cv.isEqualTo(clockvar))
                        throw new ExEx(c+"memory variable '" + name + "' clock domain conflict, port " + i, loc);
                }
            } else if (mkey.equals("domain0")) {
                Clock   cv = getMemClkVar(0);
                if (cv == null)
                    setMemClkVar(0, clockvar);
                else if (!cv.isEqualTo(clockvar))
                    throw new ExEx(c+"memory variable '" + name + "' clock domain conflict, port 0", loc);
            } else if (mkey.equals("domain1")) {
                Clock   cv = getMemClkVar(1);
                if (cv == null)
                    setMemClkVar(1, clockvar);
                else if (!cv.isEqualTo(clockvar))
                    throw new ExEx(c+"memory variable '" + name + "' clock domain conflict, port 1", loc);
            }
            
            if (mval.getPrimType() == Ptype.NULL)
                throw new ExEx("3PL attribute \"" + mkey + "\" for variable '" + name + "' is null", loc);
           
            if (mkey.equals("write_mode")) {
                if (mode == Mode.CMEMORY)
                    throw new ExEx(c+"memory variable '" + name + "' write_mode attribute cannot be used on a combinatorial RAM", loc);
                if (ports == 2) {
                    attributes.put("write_mode_a", mval);
                    attributes.put("write_mode_b", mval);                   
                }
            } else if (mkey.equals("write_mode_a")) {
                if (mode == Mode.CMEMORY)
                    throw new ExEx(c+"memory variable '" + name + "' write_mode_a attribute cannot be used on a combinatorial RAM", loc);
                if (ports == 1)
                    throw new ExEx(c+"memory variable '" + name + "' write_mode_a attribute cannot be used on a single port RAM", loc);
                attributes.put(mkey, mval);
            } else if (mkey.equals("write_mode_b")) {
                if (mode == Mode.CMEMORY)
                    throw new ExEx(c+"memory variable '" + name + "' write_mode_b attribute cannot be used on a combinatorial RAM", loc);
                if (ports == 1)
                    throw new ExEx(c+"memory variable '" + name + "' write_mode_b attribute cannot be used on a single port RAM", loc);
                attributes.put(mkey, mval);
            } else if (mkey.equals("continuous")) {
                if (mode == Mode.CMEMORY)
                    throw new ExEx(c+"memory variable '" + name + "' continuous attribute cannot be used on a combinatorial RAM", loc);
                setContinuous(mval.getSingleLval(loc), loc);
            }
            
            attributes.put(mkey, mval);
        }
    }
    
    /**
     * Get the attributes map value.
     * @param   loc is the source file location
     * @return  the attributes map value
     */
    public Val getAttributes (boolean extended, SrcLoc loc) {
        if (indass)
            return(indirect.getAttributes(extended, loc));
        
        TreeMap<String, Val> attr = new TreeMap<String, Val>();
        if (attributes != null)
            attr.putAll(attributes);
        if (extended) {
            attr.put("identifier", new Val(name, loc));
            attr.put("eidentifier", new Val(ename, loc));
            attr.put("mode", new Val(mode.modename(), loc));
            attr.put("type", new Val(type, loc));
            attr.put("typestring", new Val(wordspec.getTypeString(), loc));
            attr.put("decloc", new Val(decloc.toString(), loc));
            attr.put("inputparam", new Val(isInPar, loc));
            attr.put("outputparam", new Val(isOutPar, loc));
            attr.put("matched", new Val(matched, loc));
            attr.put("readonly", new Val(readonly, loc));
            attr.put("assigned", new Val(assigned, loc));
            if (targ_init != null)
                attr.put("init", targ_init);
      }
        Object[]    oa = new Object[1];
        Type[]      ta = new Type[1];
        Type        type = new Type(Ptype.MAP, 0);
        WordSpec    ws = type.getWordSpec(null, loc);
        oa[0] = attr;
        ta[0] = type;
        return(new Val(oa, ta, ws, null, new SubFieldList(), null));
    }
    
    /**
     * Set the 'continuous' flag.
     * @param   d is the value to set the flag
     * @param   loc is the source file location
     */
    public void setContinuous (boolean d, SrcLoc loc) {
        attributes.put("continuous", new Val(d, loc));
    }
     
    /**
     * Set the parameter/argument indirect fields for this parameter variable.
     * Field <b>Var indirect</b> is the argument variable and field
     * <b>SubFieldList ind_sfl</b> is a list of any subscripts or fields on the
     * argument variable reference.
     * @param   arg is the argument reference to which this parameter is
     *          to point
     * @param   loc is the source file location of the parameter declaration
     */
    public void setIndirect (RefOrVal arg, SrcLoc loc) {
        super.setIndirect(arg, loc);
        ind_sfl = arg.getSubFields();
    }
    
    /**
     * Get a memory port clock variable.
     * @param   i is the port index
     * @return  the primary clock Var
     */
    public Clock getMemClkVar (int i) {
        if (indass)
            return(((Memory)indirect).getMemClkVar(i));
        return(mem_clk_var[i]);
    }
    
    /**
     * Set a clock variable associated with this variable. This is only
     * called in CramReadFunc.java and RramOutFunc.java.
     * @param   i is the port index
     * @param   v is the clock Var
     */
    public void setMemClkVar(int i, Clock v) {
        if (indass) {
            ((Memory)indirect).setMemClkVar(i, v);
            return;
        }
        mem_clk_var[i] = v;
        outvars[i].setClkVar(v);
    }
    
    /**
     * Get the number of memory ports available
     * for combinatorial-output RAM.
     * @param   loc is the source file location
     * @return  the number of ports
     */
    public int cramPorts (SrcLoc loc) {
        if (indass)
            return(((Memory)indirect).cramPorts(loc));
        TDECode family = getFamily();
        return(family.cramPorts(loc));
    }
    
    /**
     * Get the address WordSpec for this memory variable.
     * @return  the address word specification
     */
    public WordSpec getMemAddrWordSpec () {
        if (indass)
            return(((Memory)indirect).getMemAddrWordSpec());
        return(memaws);
    }
    
    /**
     * Get the data WordSpec for this memory variable.
     * @return  the data word specification
     */
    public WordSpec getMemDataWordSpec () {
        if (indass)
            return(((Memory)indirect).getMemDataWordSpec());
        return(memdws);
    }
     
    /**
     * Get the address type for this memory variable.
     * @return  the address type
     */
    public Type getMemAddrType () {
        if (indass)
            return(((Memory)indirect).getMemAddrType());
        return(atype);
    }
    
    /**
     * Get the data type for this memory variable.
     * @return  the data type
     */
    public Type getMemDataType () {
        if (indass)
            return(((Memory)indirect).getMemDataType());
        return(dtype);
    }
   
    /**
     * Get the number of ports for this memory variable.
     * @return  the number of ports
     */
    public int getMemPorts () {
        if (indass)
            return(((Memory)indirect).getMemPorts());
        return(ports);
    }
    
    /**
     * For an RMEMORY variable get the number of memory ports available.
     * @param   loc is the source file location
     * @return  the number of ports
     */
    public int rramPorts (SrcLoc loc) {
        if (indass)
            return(((Memory)indirect).rramPorts(loc));
        TDECode family = getFamily();
        return(family.rramPorts(loc));
    }
    
    /**
     * For a MEMORY variable get the signal for a port output.
     * @param   port is the port number
     * @param   clkvar is the associated clock domain
     * @param   mess is a string to prepend to a call error message
     * @param   loc is the source file location
     * @return  the port output TDEVar
     */
    public TDEVar getPortOutput (int port, Clock clkvar, String mess, SrcLoc loc) {
        if ((port < 0) || (port >= ports))
            throw new ExEx(mess + " - illegal port number " + port, loc);
        if (indass)
            return(((Memory)indirect).getPortOutput(port, clkvar, mess, loc));
        if ((mem_clk_var[port] != null) && (mem_clk_var[port] != clkvar))
            throw new ExEx(mess + " - clock domain change for port " + port, loc);
        else if (mem_clk_var[port] != null)
            mem_clk_var[port] = clkvar;
        return(outvars[port]);
    }

    /**
     * Get the value of a CMEMORY or RMEMORY mode variable.
     * @param   ref is the reference to this variable
     * @param   loc is the source file location
     * @return  the value or values extracted from the variable
     */
    public Val getVal (Ref ref, SrcLoc loc) {
        SubFieldList    sfl = ref.getSubFields();
        Flag flag = ref.getFlag();           
        if ((flag != Flag.NONE))
            throw new ExEx("target variable cannot have ++ or --", loc);

        if (sfl.size() != 0)
            throw new ExEx("memory variable reference has subscripts or fields", loc);
        return(new Val(ref.getVar(), loc));
    }

    /**
     * Get a reference to a CMEMORY or RMEMORY mode variable.
     
     * @param   varnode is the tree node for the variable occurence or
     *          is null if the reference is not associated with a variable
     *          occurrence (used to detect ++, -- or ?)
     * @param   sfl is the list of associated subscript and field nodes
     *          after the subscript and field nodes have been evaluated
     * @param   is_val indicates that we want a value rather than a reference
     * @param   loc is the source file location
     * @return  the reference to the variable
     */
    public Ref getRef (
        VarNode         varnode,
        SubFieldList    sfl,
        boolean         is_val,
        SrcLoc          loc
    ) {
        Flag    flag = (varnode == null) ? Flag.NONE : varnode.getFlag();
        switch (flag) {
        case PREINCR:
        case PREDECR:
        case POSTINCR:
        case POSTDECR:
            throw new ExEx("target variable cannot have ++ or --", loc);
        case NONE:
            break;
        }

        if (indass) {
            // An indirect via a module, procedure or function parameter.
            if (indirect == null)
                return(null);
            if (ind_sfl != null)
                sfl = ind_sfl.merge(this, sfl);
            return(indirect.getRef(varnode, sfl, is_val, loc));
        }

        if (sfl.size() != 0)
            throw new ExEx("memory variable reference has subscripts or fields", loc);
        return(new Ref(this, loc));
    }
    
    /**
     * Add the clock, address and execute signals for a read
     * from a memory port.
     * @param   port is the port number
     * @param   clockvar is the clock variable
     * @param   address is the address value
     * @param   queues returns any queue references in the address
     * @param   exec is the execute signal
     * @param   mess is an error message header
     * @param   loc is the source file location
     * @return  a set in which exec signals can be placed
     */
    public HashSet<TDEVar> memRead (
        int         port,
        Clock       clockvar,
        Val         address,
        QueueRefs   queues,
        TDEVar      exec,
        String      mess,
        SrcLoc      loc
    ) {
        if ((port < 0) || (port >= ports))
            throw new ExEx("memory read '" + name + "' - there is no port " +
                                                    port, loc);
        if (!readable[port])
            throw new ExEx("memory read '" + name + "' port " + port +
                                                    " not readable", loc);
        if (mode == Mode.RMEMORY) {
            if (mem_clk_var[port] == null) {
                mem_clk_var[port] = clockvar;
                outvars[port].setClkVar(clockvar);
            } else if (clockvar != mem_clk_var[port])
                throw new ExEx("reads for memory '" + name + "' port " + port +
                                            " have different clocks", loc);
        }

        if (!address.isTarget())
            address.toTarget();
        address.checkMatch(atype, false, "memory read", loc);
        /*
        if (address.getWordSpec().numBits() > memaws.numBits())
            throw new ExEx("read for memory '" + name + "' port " + port +
                                            " has excessive address width", loc);
        */
        
        ArrayList<Object>   a = reads[port];

        WordSpec    aws = atype.getWordSpec(null, loc);
        TDEVar      atdev = tdelist.signal("MADDR", aws, loc);
        tdelist.connect(atdev, address, exec, mess, loc);
        queues.and_set(address.getQueues(), loc);
        a.add(atdev);
        address.setUsed();

        if (mode == Mode.RMEMORY) {
            a.add(exec);
            return(null);
        } else {
            HashSet<TDEVar> s = new HashSet<TDEVar>();
            a.add(s);
            return(s);
        }
    }
    
    /**
     * Add the clock, address, data and execute signals for a write
     * to a memory port.
     * @param   port is the port number
     * @param   clockvar is the clock variable
     * @param   address is the address value
     * @param   data is the data input value
     * @param   queues returns any queue references in the address
     * @param   exec is the execute signal
     * @param   mess is an error message header
     * @param   loc is the source file location
     */
    public void memWrite (
        int         port,
        Clock       clockvar,
        Val         address,
        Val         data,
        QueueRefs   queues,
        TDEVar      exec,
        String      mess,
        SrcLoc      loc
    ) {
        String  mode_msg = (mode == Mode.CMEMORY) ? "cmemory" : "rmemory";
        if ((port < 0) || (port >= ports))
            throw new ExEx(mode_msg+" write '" + name + "' - there is no port " +
                                                    port, loc);
        if (!writable[port])
            throw new ExEx(mode_msg+" write '" + name + "' port " + port +
                                                    " not writable", loc);
        if (mem_clk_var[port] == null) {
            mem_clk_var[port] = clockvar;
            outvars[port].setClkVar(clockvar);
        } else if (clockvar != mem_clk_var[port])
            throw new ExEx("writes for "+mode_msg+" '" + name + "' port " + port +
                                            " have different clocks", loc);
        if (readonly)
            throw new ExEx("cannot write to memory '" + name + "' - read-only!", loc);

        if (!address.isTarget())
            address.toTarget();
        address.checkMatch(atype, false, mode_msg+" write", loc);
        /*
        if (address.getWordSpec().numBits() > memaws.numBits())
            throw new ExEx("write for memory '" + name + "' port " + port +
                                            " has excessive address width", loc);
        */

        if (!data.isTarget())
            data.toTarget();
        data.checkMatch(dtype, false, mode_msg+" write", loc);
        /*
        if (data.getWordSpec().numBits() > memdws.numBits())
            throw new ExEx("write for memory '" + name + "' port " + port +
                                            " has excessive data width", loc);
        */

        ArrayList<TDEVar>   a = writes[port];

        WordSpec    aws = atype.getWordSpec(null, loc);
        TDEVar      atdev = tdelist.signal("MADDR", aws, loc);
        tdelist.connect(atdev, address, exec, mess, loc);
        queues.and_set(address.getQueues(), loc);
        a.add(atdev);

        WordSpec    dws = dtype.getWordSpec(null, loc);
        TDEVar      dtdev = tdelist.signal("MDATA", dws, loc);
        tdelist.connect(dtdev, data, exec, mess, loc);
        queues.and_set(data.getQueues(), loc);
        a.add(dtdev);

        a.add(exec);
        address.setUsed();
        data.setUsed();
    }

    /**
     * Output the code to create the variable itself.
     * This method is called on completion of program interpretation at which
     * point all assignments to this variable will have been made.
     */
    @SuppressWarnings("unused")
    public void createVar () {
        current_create = this;
        // If the simulator is to be run, add a TDEType.SIMVAR to the TDE list
        // to notify the simulator about this variable.
        if (sim_implemented && sim) {
            TDE simtdevar = new TDE(TDEType.SIMVAR);
            simtdevar.add2p(this);
            tdelist.addTDE(simtdevar);
        }

        // Nothing to do for indirect (parameter).
        if (indass)
            return;

        String  mode_msg = (mode == Mode.CMEMORY) ? "cmemory" : "rmemory";
        int awidth = atype.numBits();
        int dwidth = dtype.numBits();
        TDE ram = new TDE((mode == Mode.RMEMORY) ? TDEType.RRAM : TDEType.CRAM, decloc);        
        ram.add2p(ports);
        for (int i=0 ; i<ports ; i++) {            
            // 'reads' and 'writes' have an ArrayList per port.
            // Each ArrayList contains duples for reads and triples for writes
            //      address - TDEVar                        address - TDEVAR
            //      execute - TDEVAR or set of TDEVar       data    - TDEVAR
            //                                              execute - TDEVAR
            //
            int                 rcnt = reads[i].size() / 2;
            int                 wcnt = writes[i].size() / 3;
            TDE                 asel = null;
            TDE                 dsel = null;
            TDE                 ror = null;
            TDEVar              rin = null;
            TDE                 wor = null;
            TDEVar              win = null;
            TDEVar              ain = null;
            TDEVar              din = null;
            Iterator<Object>    rit;
            Iterator<TDEVar>    wit;

            if ((rcnt == 0) && (wcnt == 0)) {
                emsg(mode_msg + " '" + ename + "' does not use port " + i, decloc);
                return;
            }
            if ((rcnt > 0) || (wcnt > 0))
                ain = tdelist.signal("S", memaws, decloc);

            if ((rcnt == 1) && (wcnt == 0)) {
                //
                // special case of 1 reader only
                //
                ain = (TDEVar)reads[i].get(0);
                if (mode == Mode.RMEMORY)
                    rin = readexec(reads[i].get(1));
            } else if ((rcnt == 0) && (wcnt == 1)) {
                //
                // special case of 1 writer only
                //
                ain = writes[i].get(0);
                din = writes[i].get(1);
                win = writes[i].get(2);
            } else {
                //
                // general case
                //
                asel = new TDE(TDEType.SELECT, decloc);
                asel.add2o(ain);

                // reads
                if (rcnt > 0) {
                    if (mode == Mode.RMEMORY) {
                        rin = tdelist.signal("S", decloc);
                        ror = new TDE(TDEType.OR, decloc);
                        ror.add2o(rin);
                    }
                    rit = reads[i].iterator();
                    while (rit.hasNext()) {
                        TDEVar  asig = (TDEVar)rit.next();
                        TDEVar  esig = readexec(rit.next());
                        if (esig == null)  {
                            emsg("a cmemory read cannot be used in this context", asig.getSrcLoc());
                            return;
                        }
                        asel.add2i(esig);
                        asel.add2i(asig);
                        if (mode == Mode.RMEMORY)
                            ror.add2i(esig);
                    }
                }

                // writes
                if (wcnt > 0) {
                    win = tdelist.signal("S", decloc);
                    wor = new TDE(TDEType.OR, decloc);
                    wor.add2o(win);
                    wit = writes[i].iterator();
                    dsel = new TDE(TDEType.SELECT, decloc);
                    din = tdelist.signal("S", memdws, decloc);
                    dsel.add2o(din);
                    while (wit.hasNext()) {
                        TDEVar  asig = wit.next();
                        TDEVar  dsig = wit.next();
                        TDEVar  esig = wit.next();
                        asel.add2i(esig);
                        asel.add2i(asig);
                        dsel.add2i(esig);
                        dsel.add2i(dsig);
                        wor.add2i(esig);
                    }
                }          

                tdelist.addTDE(asel);
                tdelist.addTDE(dsel);
                tdelist.addTDE(wor);
                tdelist.addTDE(ror);
            }

            if (mem_clk_var[i] != null)
                ram.add2ic(mem_clk_var[i].getClkSig());
            else
                ram.add2i(null);
            ram.add2o(outvars[i]);
            ram.add2i(ain);
            ram.add2i(din);
            if (mode == Mode.RMEMORY)
                ram.add2i(rin);
            ram.add2i(win);
        }

        ram.add2p(dwidth);
        ram.add2p(awidth);
        
        // Initialisation
        if (targ_init != null) {
            int         n = 1 << awidth;
            String[]    sinit = memdws.packInit(targ_init, n, "memory variable '" + name + "'", decloc);
            ram.add2p(sinit);
        } else
            ram.add2p();
        
        // Check if there are any 'write_mode' attributes and if so copy them to the
        // param list.
        if (attributes != null) {            
            Set<?>      ks = attributes.keySet();
            Iterator<?> it = ks.iterator();
            String      mkey;
            Val         mval;
            while (it.hasNext()) {
                mkey = (String)it.next();
                if (mkey.startsWith("write_mode")) {
                    mval = attributes.get(mkey);
                    ram.add2p(mkey);
                    ram.add2p(mval.getSingleSval(null));               
                }
            }
        }
         
        tdelist.addTDE(ram);
    }

    // The object is either
    //  a TDEVar exec signal
    //      return the TDEVar
    //  a HashSet of TDEVar exec signals
    //      return the OR of the set of TDEVars
    @SuppressWarnings("unchecked")
    private TDEVar readexec (Object o) {
        if (o instanceof TDEVar)
            return((TDEVar)o);
        HashSet<TDEVar>     s = (HashSet<TDEVar>)o;
        if (s.size() == 0)
            return(null);
        //    throw new ExEx("a memory read of variable " + name +
        //        " (" + ename + ") in a value expression is not executed", decloc);
        Iterator<TDEVar>    it = s.iterator();
        if (s.size() == 1)
            return(it.next());
        TDE     or = new TDE(TDEType.OR, decloc);
        for (TDEVar tdev: s)
            or.add2i(tdev);
        return(or.finish());
    }
}
