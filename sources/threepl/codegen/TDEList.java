package threepl.codegen;

import static threepl.ThreePL.*;
import static threepl.codegen.TDEVar.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;

import threepl.exceptions.ExEx;
import threepl.exec.QueueRefs;
import threepl.exec.Scope;
import threepl.exec.Priority;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.exec.WordSpec;
import threepl.exec.Var.IDtype;
import threepl.netlist.Element;
import threepl.netlist.Net;
import threepl.netlist.TDECode;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * A TDE (Topological Description Element) list is the intermediate form of
 * generated code. A TDE list has variable declarations at the front followed
 * by logic using those variables. An index, 'variables', is used to maintain
 * the insertion point for variables at the head of the list. Methods
 * addTDE() append code TDEs to the tail of the list. Methods addVTDE insert
 * TDEs at the end of the leading variable section of the list. Field 'index'
 * provides a unique integer to append to the end of generated signal names.
 * It is incremented after each use.
 */
@SuppressWarnings("serial")
public class TDEList extends ArrayList<TDE> implements Constant, TDEConstants {
    private int         index;
    private int         variables;
    private boolean     changes;    // changes during an optimisation iteration

    public static  WordSpec    logws = new WordSpec(1, Ptype.LOG);

    static  ArrayList<branch_optimise>      branches;
    static  ArrayList<TDE>                  connects;
    static  ArrayList<TDE>                  dels;
    static  ArrayList<TDE>                  regs;
    static  ArrayList<TDE>                  ops;
    static  ArrayList<TDE>                  whens;
    static  ArrayList<TDE>                  iloops;
    static  ArrayList<TDE>                  waits;
    static  ArrayList<TDE>                  gates;
    static  ArrayList<TDE>                  sels;
    static  ArrayList<TDE>                  execps;
    static  ArrayList<TDE>                  dffs;
    static  ArrayList<TDE>                  start_dffs;
    
    static  TreeMap<String,TDE>             uelements = new TreeMap<String,TDE>();  // user-defined elements
    static  TDE                             current_uelement;                       // current or unnamed user-defined element

    // Class contains elements and signals for a branch statement.
    // These are used in optimisation.
    class branch_optimise {
        public TDEVar               start;      // start signal for branch
        public ArrayList<TDEVar>    and_outs;   // selection AND gate output signals
        public TDE                  or;         // finish signal OR gate
        public TDEVar               finish;     // finish signal
        
        public branch_optimise (
            TDEVar              start,
            ArrayList<TDEVar>   case_start_list,
            TDE                 or,
            TDEVar              finish
        ) {
            this.start = start;
            and_outs = case_start_list;
            this.or = or;
            this.finish = finish;
        }
    }
    
    /**
     * Initialise static variables.
     */
    public static void init () {
        branches = new ArrayList<branch_optimise>();
        connects = new ArrayList<TDE>();
        dels = new ArrayList<TDE>();
        regs = new ArrayList<TDE>();
        ops = new ArrayList<TDE>();
        whens = new ArrayList<TDE>();
        iloops = new ArrayList<TDE>();
        waits = new ArrayList<TDE>();
        gates = new ArrayList<TDE>();
        sels = new ArrayList<TDE>();
        execps = new ArrayList<TDE>();
        dffs = new ArrayList<TDE>();
        start_dffs = new ArrayList<TDE>();    
    }
    
    /**
     * Construct the TDE list.
     * @param   size is the initial size of the list
     */
    public TDEList (int size) {
        super(size);
    }
    
    public void newBranch (
        TDEVar              start,
        ArrayList<TDEVar>   case_start_list,
        TDE                 or,
        TDEVar              finish
    ) {
        branches.add(new branch_optimise(start, case_start_list, or, finish));
    }
    
    public void newConnect (TDE connect) {connects.add(connect);}

    public void newREG (TDE reg) {regs.add(reg);}
    
    public void newOP (TDE op) {ops.add(op);}
    
    public void newWHEN (TDE when) {whens.add(when);}

    public void newILOOP (TDE iloop) {iloops.add(iloop);}

    public void newWAIT (TDE wait) {waits.add(wait);}

    public void newGate (TDE gate) {gates.add(gate);}
 
    public void newSEL (TDE sel) {sels.add(sel);}
   
    public void newEXECP (TDE execp) {execps.add(execp);}

    public void newDFF (TDE dff) {dffs.add(dff);}
    
    /**
     * Start a new element TDE.
     * @param bname is an optional block name to be used in the netlist
     * @param ename is the element name
     */  
    public void newElement (String bname, String ename) {
        current_uelement = new TDE(TDEType.ELEMENT);
        if (bname != null) {
            if (uelements.containsKey(bname))
                throw new ExEx("element() - duplicate block name '" + bname + "'");
            uelements.put(bname, current_uelement);
        }
        current_uelement.add2p(ename);
        current_uelement.add2p(bname);
    }
    
    /**
     * Add a port to a new element.
     * If no block name is given the block currently under construction
     * will be addressed.
     * @param bname is an optional block name
     * @param portName is the port name
     * @param portType is the port type, 0 for input, 1 for output, 2 for 3-state output, 3 for clock input
     * @param tdev is the associated signal
     * @param arraysize is the size for an array, 0 otherwise
     * @param arrayformat is the array format indicator: 0 for single signal, 1 for discrete ports, 2 for a port array
     */
    public void addPort (String bname, String portName, int portType, TDEVar tdev, int arraysize, int arrayformat) {
        TDE tde = (bname == null) ? current_uelement : uelements.get(bname);
        if (tde == null)
            throw new ExEx("addPort() - no TDE element");
        tde.add2p(portName);
        tde.add2p(portType);
        tde.add2p(arraysize);
        tde.add2p(arrayformat);
        switch (portType) {
        case 0:
            tde.add2i(tdev);
            break;
        case 3:
            tde.add2ic(tdev);
            break;
        default:
            tde.add2o(tdev);
        }
    }
    
    /**
     * Add a property to a new element.
     * If no block name is given the block currently under construction
     * will be addressed.
     * @param bname is an optional block name
     * @param propertyName is the property name
     * @param propertyValue is the property value
     */
    public void addProperty (String bname, String propertyName, String propertyValue) {
        TDE tde = ((bname == null) || bname.length() == 0) ? current_uelement : uelements.get(bname);
        tde.add2p(propertyName);
        tde.add2p(4);
        tde.add2p(propertyValue);
        tde.add2p(0);
    }
    
    /**
     * Complete an element.
     * If no block name is given the block currently under construction
     * will be completed.
     * @param bname is an optional block name
     */ 
    public void endElement (String bname) {
        TDE tde;
        if (bname == null)
            tde = current_uelement;
        else {
            tde = uelements.get(bname);
            uelements.put(bname,  null); // clear entry, leaving key to allow detection of duplication
        }
        addTDE(tde);
    }

    /**
     * Append a TDE to the end of the TDElist. If the argument
     * is null or the TDE is determined to be redundant the method does nothing.
     * @param   tde is the topological description element to be appended,
     *          or is null
     */
    public void addTDE (TDE tde) {
        if (postProcessing)
            throw new ExEx("Target variable assignment during post-processing");
        
       if ((tde == null) || (tde.redundant()))
            return;
            
        add(tde);
        switch (tde.getType()) {
        case REG:
            newREG(tde);
            break;
        case OPERATOR:
            newOP(tde);
            break;
        case CONNECT:
            newConnect(tde);
            TDEVar  cin = tde.getInput(0);
            TDEVar  cout = tde.getOutput(0);
            boolean cast = (tde.getParams().size() != 0) && (tde.getParam(0) != null);
            if ((cin.numBits() != 1) && (cin.numBits() != cout.numBits() && !cast))
                throw new ExEx("Intermediate code list error - CONNECT has different size input to output");
            break;
        case WHEN:
            newWHEN(tde);
            break;
        case ILOOP:
            newILOOP(tde);
            break;
        case WAIT:
            newWAIT(tde);
            break;
        case INV:
        case AND:
        case OR:
        case XOR:
            newGate(tde);
            break;
        case DFF:
            newDFF(tde);
            break;
        case EXECP:
            newEXECP(tde);
            break;
        case SELECT:
            newSEL(tde);

            break;
        default:
            break;
       }
    }

    /**
     * Create a signal declaration TDE and append it to the end of the initial variable
     * declaration part of the TDElist. This is ignored if the simulator is not
     * being used and the -T option has not been given since the code generator
     * does not use these TDEs.
     * @param   tdev is the signal TDEVar
     */
    public void addVTDE (TDEVar tdev) {
        if (postProcessing)
            throw new ExEx("Target variable assignment during post-processing");
        
        if (!sim && !boolDir("sigList"))
            return; // don't bother if not using simulator or listing signals
        TDEVarList   a = new TDEVarList();
        a.add(tdev);
        add(variables++, new TDE(TDEType.SIG, a));
    }
    
    /**
     * Get the list of SELECT elements.
     * @return  the list of SELECT elements
     */
    public ArrayList<TDE> getSelects () {return(sels);}
    
    /**
     * Get the list of REG elements.
     * @return  the list of REG elements
     */
    public ArrayList<TDE> getRegs () {return(regs);}
    
    /**
     * Get the list of OPERATOR elements.
     * @return  the list of OPERATOR elements
     */
    public ArrayList<TDE> getOps () {return(ops);}

    /**
     * Copy a TDEVar with a substituted name and append it to the end of
     * the initial variable declaration part of the TDElist.
     * @param   name new signal name
     * @param   v old signal
     * @param   loc source file location
     * @return  the new TDE variable
    public TDEVar copysignal (String name, TDEVar v, SrcLoc loc) {
        TDEVar      nv = new TDEVar(name, v, loc);
        addVTDE(nv);
        return nv;
    }
     */

    /**
     * Create a decoder (OPERATOR == TDEVar) and append it to the end of
     * the TDElist.
     * @param   val decoded value
     * @param   in input signal
     * @param   out output signal
     * @param   loc source file location
     */
    public void decode (long val, TDEVar in, TDEVar out, SrcLoc loc) {
        TDE     tde = new TDE(TDEType.OPERATOR);
        Ptype   val_ptype = (val < 0) ? Ptype.INT : Ptype.UINT;

        tde.add2p(TDEOp.EQ);
        tde.add2p(in.getWordSpec().getPrimType(0).isSigned());
        tde.add2p(val_ptype.isSigned());
        tde.add2i(in);
        tde.add2i(new TDEVar(val, val_ptype, loc));
        tde.add2o(out);
        addTDE(tde);
        /*
        TDE tde = new TDE(TDEType.DECODE);

        tde.add2p(val);
        tde.add2i(in);
        tde.add2o(out);
        addTDE(tde);
        */
    }

    /**
     * Create a 1-cycle delay TDE and append it to the end of the
     * TDElist.
     * @param   out output signal
     * @param   in input signal
     * @param   clock clock input signal
     * @param   reset input signal
     */
    public void del (TDEVar out, TDEVar in, TDEVar clock, TDEVar reset) {
        del(out, in, clock, 1, reset, null);
    }

    /**
     * Create a fixed delay TDE and append it to the end of the
     * TDElist.
     * @param   out is the output signal
     * @param   in is the input signal
     * @param   clock is the clock input signal
     * @param   delay is the number of cycles to delay
     * @param   reset input signal
     */
    public void del (TDEVar out, TDEVar in, TDEVar clock, int delay, TDEVar reset) {
        del(out, in, clock, delay, reset, null);
    }

    /**
     * Create a delay TDE whose delay must be determined at the end of
     * immediate execution and append it to the end of the TDElist.
     * The QueueRef parameter is scanned for asynchronous unbuffered
     * queues and if found the delay is set to 2, otherwise it is set to 1.
     * @param   out is the output signal
     * @param   in is the input signal
     * @param   clock is the clock input signal
     * @param   reset input signal
     * @param   pr is a queue reference which determines the delay required
     */
    public void del (TDEVar out, TDEVar in, TDEVar clock, TDEVar reset, QueueRefs pr) {
        del(out, in, clock, 0, reset, pr);
    }
    
    /**
     * Create a fixed delay TDE or one whose delay may be determined at the end of
     * immediate execution, and append it to the end of the TDElist.
     * If the QueueRefs parameter 'pr' is null a fixed delay is created using the
     * value supplied by parameter 'delay'.
     * The QueueRef parameter 'pr', if not null, is scanned for asynchronous unbuffered
     * queues and if found the delay is set to 2, otherwise it is set to 1. In this case
     * parameter 'delay' is ignored.
     * @param   out is the output signal
     * @param   in is the input signal
     * @param   clock is the clock input signal
     * @param   delay input signal
     * @param   reset input signal
     * @param   pr is a queue reference which determines the delay required
     */
    private void del (TDEVar out, TDEVar in, TDEVar clock, int delay, TDEVar reset, QueueRefs pr) {
        TDE tde = new TDE(TDEType.DEL);

        if (pr != null)
            tde.add2p(pr);  // replaced later by integer 1 or 2 - see tdelist.resolveDELs()
        else
            tde.add2p(delay);
        tde.add2ic(clock);
        tde.add2i(in);
        tde.add2i(null); // default - not variable length
        tde.add2i(null); // default - enable high
        tde.add2i(reset);
        tde.add2o(out);
        addTDE(tde);
        WordSpec    iws = in.getWordSpec();
        if (((pr != null) || (delay == 1)) && ((iws == null) || (iws.numBits() == 1)))
            dels.add(tde); // add to list of control thread DELs
    }

    /**
     * Print the TDE list.
     * @param dump_loc is true if source file locations are to be listed
     */
    public void dump (boolean dump_loc) {
        int i = boolDir("sigList") ? 0 : variables;

        icl("");
        icl("");
        switch ((int)longDir("listTDEsSel")) {
        case 1:
            icl("TDEList prior to 1st general optimisation");
            icl("-----------------------------------------\n\n");
            break;
        case 2:
            icl("TDEList after 1st general optimisation prior to family-specific optimisation");
            icl("----------------------------------------------------------------------------\n\n");
            break;
        case 3:
            icl("TDEList after family-specific optimisation");
            icl("------------------------------------------\n\n");
            break;
       default:
            icl("TDEList after all optimisation");
            icl("------------------------------\n\n");
        }

        while (i < size())
            get(i++).dump(dump_loc);

        icl("\n\n---------------------------------------------------------------------------");
        icl("end of TDEList\n\n\n");
    }

    /**
     * Get the current index value, post-incrementing it.
     * @return the index value
     */
    public int index () {
        return index++;
    }
    
    /**
     * Create a TDEVar with a unique name for a multi-bit signal and
     * append it to the end of the initial variable declaration part of the
     * TDElist.
     * @param   header is the leading string for the signal name to which is appended a
     *          unique integer
     * @param   size is the number of bits
     * @param   loc is the source file location
     * @return the new TDE variable
     */
    public TDEVar signal (String header, int size, SrcLoc loc) {
        WordSpec    wordspec = new WordSpec(size, Ptype.NONE);
        TDEVar      v = TDEVar.makeTDEVar(header + index++, wordspec, loc);
        addVTDE(v);
        return(v);
    }

    /**
     * Create a TDEVar with a unique name for a single bit signal and
     * append it to the end of the initial variable declaration part of the
     * TDElist.
     * @param   header is the leading string for the signal name to which is appended a
     *          unique integer
     * @param   loc is the source file location
     * @return the new TDE variable
     */
    public TDEVar signal (String header, SrcLoc loc) {
        TDEVar      v = new TDEVar(header + index++, loc);
        addVTDE(v);
        return(v);
    }

    /**
     * Create a TDEVar with a unique name for a signal array and append
     * it to the end of the initial variable declaration part of the TDElist.
     * @param   header is the leading string for the signal name to which is
     *          appended a unique integer
     * @param   wordspec is a word specification
     * @param   loc is the source file location
     * @return  the new TDE variable
     */
    public TDEVar signal (String header, WordSpec wordspec, SrcLoc loc) {
        TDEVar      v = TDEVar.makeTDEVar(header + index++, wordspec, loc);
        addVTDE(v);
        return(v);
    }

    /**
     * Create a named TDEVar for a single bit signal and append it to the
     * end of the initial variable declaration part of the TDElist.
     * @param   name is the signal name
     * @param   loc is the source file location
     * @return  the new TDE variable
     */
    public TDEVar namesignal (String name, SrcLoc loc) {
        TDEVar      v = new TDEVar(name, loc);
        addVTDE(v);
        return(v);
    }

    /**
     * Create a named TDEVar for a signal array and append it to the end
     * of the initial variable declaration part of the TDElist.
     * This is only called from exec/Static.java to create the CE and
     * RES signal arrays. The Type has a width of 'size' but has a Ptype
     * of NONE.
     * @param   name signal name
     * @param   size number of bits in signal array
     * @param   loc is the source file location
     * @return  the new TDE variable
     */
    public TDEVar namesignal (String name, int size, SrcLoc loc) {
        WordSpec    wordspec = new WordSpec(size, Ptype.NONE);
        TDEVar      v = TDEVar.makeTDEVar(name, wordspec, loc);
        addVTDE(v);
        return(v);
    }

    /**
     * Create a named TDEVar for a signal array and append it to the end
     * of the initial variable declaration part of the TDElist.
     * @param   name signal name
     * @param   b a wordspec
     * @param   loc is the source file location
     * @return  the new TDE variable
     */
    public TDEVar namesignal (String name, WordSpec b, SrcLoc loc) {
        TDEVar      v = TDEVar.makeTDEVar(name, b, loc);
        addVTDE(v);
        return(v);
    }
    
    /**
     * Create a 2-input AND gate and append it to the TDElist.
     * @param   in1 is the 1st input signal
     * @param   in2 is the 2nd input signal
     * @param   loc is the source file location
     * @return  the gate output
     */
    public TDEVar and (TDEVar in1, TDEVar in2, SrcLoc loc) {
        TDEVar  out;
        if ((in1.getWordSpec() != null) || (in2.getWordSpec() != null))
            out = signal("AND", logws, loc);
        else
            out = signal("AND", loc);
        TDE and = new TDE(TDEType.AND, loc);
        and.add2i(in1);
        and.add2i(in2);
        and.add2o(out);
        addTDE(and);
        return(out);
    }
    
    /**
     * Create a 3-input AND gate and append it to the TDElist.
     * @param   in1 is the 1st input signal
     * @param   in2 is the 2nd input signal
     * @param   in3 is the 3rd input signal
     * @param   loc is the source file location
     * @return  the gate output
     */
    public TDEVar and (TDEVar in1, TDEVar in2, TDEVar in3, SrcLoc loc) {
        TDEVar  out;
        if ((in1.getWordSpec() != null) || (in2.getWordSpec() != null))
            out = signal("AND", logws, loc);
        else
            out = signal("AND", loc);
        TDE and = new TDE(TDEType.AND, loc);
        and.add2i(in1);
        and.add2i(in2);
        and.add2i(in3);
        and.add2o(out);
        addTDE(and);
        return(out);
    }
    
    /**
     * Create a 2-input OR gate and append it to the TDElist.
     * @param   in1 is the 1st input signal
     * @param   in2 is the 2nd input signal
     * @param   loc is the source file location
     * @return  the gate output
     */
    public TDEVar or (TDEVar in1, TDEVar in2, SrcLoc loc) {
        TDEVar  out;
        if ((in1.getWordSpec() != null) || (in2.getWordSpec() != null))
            out = signal("OR", logws, loc);
        else
            out = signal("OR", loc);
        TDE or = new TDE(TDEType.OR, loc);
        or.add2i(in1);
        or.add2i(in2);
        or.add2o(out);
        addTDE(or);
        return(out);
    }
     
    /**
     * Create a multi-input OR gate and append it to the TDElist.
     * @param   in is the set of input signals, each signal being a single bit
     * @param   loc is the source file location
     * @return  the gate output
     */
    public TDEVar or (HashSet<TDEVar> in, SrcLoc loc) {
        switch (in.size()) {
        case 0:
            return(TDEVar.GND);
        case 1:
            return(((TDEVar[])in.toArray())[0]);   // return the single entry in the set
        default:
            TDEVar  out = signal("OR", loc);
            TDE     or = new TDE(TDEType.OR, loc);
            for (TDEVar tdev : in)
                or.add2i(tdev);
            or.add2o(out);
            addTDE(or);
            return(out);
        }
    }
    
    /**
     * Create an inverter and append it to the TDElist.
     * @param   in is the input signal
     * @param   loc is the source file location
     * @return  the inverter output
     */
    public TDEVar inv (TDEVar in, SrcLoc loc) {
        TDEVar  out;
        if (in.getWordSpec() != null)
            out = signal("INV", logws, loc);
        else
            out = signal("INV", loc);
        TDE inv = new TDE(TDEType.INV, loc);
        inv.add2i(in);
        inv.add2o(out);
        addTDE(inv);
        return(out);
    }
    
    /**
     * Left or right shift.
     * @param out       output TDEVar
     * @param in        input TDEVar
     * @param lshift    the shift amount, +ve for left, -ve for right
     * @param loc       the source file location
     */
    public void lshift (TDEVar out, TDEVar in, int lshift, SrcLoc loc) {
        Ptype   ptype = in.getWordSpec().getPrimType(0);
        TDE     stde = new TDE(TDEType.OPERATOR, loc);
        TDEVar  stdev = new TDEVar(Long.valueOf(Math.abs(lshift)), Ptype.UINT, loc);
        stde.add2p(lshift < 0 ? TDEOp.RSH : TDEOp.LSH);
        stde.add2p(ptype.isSigned());
        stde.add2p(false);
        stde.add2i(in);
        stde.add2i(stdev);
        stde.add2o(out);
        tdelist.addTDE(stde);        
    }
    
    /**
     * Create an FDRS and append it to the TDElist.
     * @param   q is the data output signal
     * @param   d is the data input signal
     * @param   c is the clock
     * @param   ce is the clock enable or null
     * @param   r is the synchronous reset signal or null
     * @param   s is the synchronous set signal or null
     * @param   init is the initial state, "R" or "S"
     * @param   loc is the source file location
     */
    public void fdrse (
        TDEVar  q,
        TDEVar  d,
        TDEVar  c,
        TDEVar  ce,
        TDEVar  r,
        TDEVar  s,
        String  init,
        SrcLoc  loc
    ) {
        TDE df = new TDE(TDEType.DFF, loc);
        df.add2p(init);             // initial state
        df.add2p(false);            // sync R/S
        df.add2i(d);                // D
        df.add2ic(c);               // C
        df.add2i(ce);               // CE
        df.add2i(r);                // R
        df.add2i(s);                // S
        df.add2o(q);
        tdelist.addTDE(df);
    }
    
    /**
     * Create an FDCE and append it to the TDElist.
     * @param   d is the data input signal
     * @param   c is the clock
     * @param   ce is the clock enable or null
     * @param   clr is the asynchronous clear signal or null
     * @param   init is the initial state, "R" or "S"
     * @param   loc is the source file location
     * @return  the data output signal
     */
    public TDEVar fdce (
        TDEVar  d,
        TDEVar  c,
        TDEVar  ce,
        TDEVar  clr,
        String  init,
        SrcLoc  loc
    ) {
        TDEVar  out = signal("FDCE", loc);
        TDE df = new TDE(TDEType.DFF, loc);
        df.add2p(init);             // initial state
        df.add2p(true);             // async CLR/PRE
        df.add2i(d);                // D
        df.add2ic(c);               // C
        df.add2i(ce);               // CE
        df.add2i(clr);              // CLR
        df.add2i(null);             // PRE
        df.add2o(out);
        tdelist.addTDE(df);
        return(out);
    }    
    
    /**
     * Create an FDPE and append it to the TDElist.
     * @param   d is the data input signal
     * @param   c is the clock
     * @param   ce is the clock enable or null
     * @param   pre is the asynchronous preset signal or null
     * @param   init is the initial state, "R" or "S"
     * @param   loc is the source file location
     * @return  the data output signal
     */
    public TDEVar fdpe (
        TDEVar  d,
        TDEVar  c,
        TDEVar  ce,
        TDEVar  pre,
        String  init,
        SrcLoc  loc
    ) {
        TDEVar  out = signal("FDPE", loc);
        TDE df = new TDE(TDEType.DFF, loc);
        df.add2p(init);             // initial state
        df.add2p(true);             // async CLR/PRE
        df.add2i(d);                // D
        df.add2ic(c);               // C
        df.add2i(ce);               // CE
        df.add2i(null);             // CLR
        df.add2i(pre);              // PRE
        df.add2o(out);
        tdelist.addTDE(df);
        return(out);
    }    

    /**
     * Conditionally generate a delayed start signal for a queue or priority wait.
     * If the queue availability argument is null (no queue references)
     * and pri_in is null (there is no enclosing priwait) simply return
     * the start signal argument.
     * @param   queues is the queue references
     * @param   checkedqueues is the queue references that have been checked already
     * @param   availok true means that availability has already been checked and
     *          hence it is not necessary to check here
     * @param   scope is the relevant variable scope
     * @param   sync is true if this the control for a sync block
     * @param   start is the start signal
     * @param   pri_in is an optional input signal to a priority encoder
     * @param   pri_out is an optional output signal from a priority encoder
     * @param   reset is the reset input signal or null
     * @param   ubqwpending is the unbuffered queue execution pending write output signal or null
     * @param   ubqrpending is the unbuffered queue execution pending read output signal or null
     * @param   loc is the source file location
     * @return  the TDE variable of the conditionally delayed start signal
     */
    public TDEVar execp (
        QueueRefs   queues,
        QueueRefs   checkedqueues,
        boolean     availok,
        Scope       scope,
        boolean     sync,
        TDEVar      start,
        TDEVar      pri_in,
        TDEVar      pri_out,
        TDEVar      reset,
        TDEVar      ubqwpending,
        TDEVar      ubqrpending,
        SrcLoc      loc
    ) {
        Val     bqavail = null;
        TDEVar  ubqwavail = null;
        TDEVar  ubqravail = null;
        boolean ubq = false;
        int     type = 0;
        
        // Type -                                       pri_in  pri_out bqavail     ubqwavail   ubqravail
        // 0    priority        no queues               Y       Y       null        null        null
        // 1    priority        buffered queues only    Y       Y       Y           null        null
        // 2    no priority     buffered queues only    null    null    av          null        null
        // 3    no priority     unbuffered queues       null    null    av or VCC   av or VCC   av or VCC
        
        if (queues != null) {
            // availability signal for all queues in assignment
            if (scope == null)
                scope = getModuleScope();
            if (sync) {
                // Sync block queue availability value for queue references,
                // null if there are no queue references. Note that the
                // queue availability does not include any conditional signals
                // (from queues in the 2nd or 3rd arguments of a ?: operator).
                bqavail = queues.getSyncAvail(getModuleScope(), checkedqueues, loc);
            } else {
                // availability signal for all buffered queues in assignment
                bqavail = queues.getBQAvail(scope, checkedqueues, loc);
                ubq = queues.haveUnbufferedQueues();
                if (ubq) {
                    // availability signals for all unbuffered queues in assignment
                    ubqravail = queues.getUBQRAvail(scope, null, loc);
                    ubqwavail = queues.getUBQWAvail(scope, null, loc);
                }
            }
        }
        
        // If buffered queue availability has already been checked, e.g. in a sync, there
        // is no need for the EXECP to be conditional on buffered queue availability.
        if (availok)
            bqavail = null;

        // No queues and no priority wait - connect start_del to start and return.
        if ((bqavail == null) && !ubq && (pri_in == null))
            return(start);
        
        if (pri_out == null) {
            // Have no priority wait signals.
            if ((ubqwavail == null) && (ubqravail == null))
                type = 2;
            else
                type = 3;
        } else {
            // Have priority wait signals.
            if ((ubqwavail != null) || (ubqravail != null))
                throw new ExEx("priority wait contains unbuffered queues", loc);
            if (bqavail == null)
                type = 0;
            else
                type = 1;
        }

        if (bqavail != null)
            bqavail.resolveClocks(null, Calloc.QUEUEAVAIL, loc);

        TDEVar  start_del = signal("SD", null);
        TDE     tde_execp = new TDE(TDEType.EXECP, loc);
        tde_execp.add2p(type);
        tde_execp.add2ic(getCurrentClock());
        tde_execp.add2i(start);
        tde_execp.add2i(bqavail != null ? bqavail.getTDEVar() : TDEVar.VCC);
        tde_execp.add2i(ubqwavail != null ? ubqwavail : TDEVar.VCC);
        tde_execp.add2i(ubqravail != null ? ubqravail : TDEVar.VCC);
        tde_execp.add2i(pri_out);
        tde_execp.add2i(reset);
        tde_execp.add2o(start_del);
        tde_execp.add2o(pri_in);
        tde_execp.add2o(ubqwpending);
        tde_execp.add2o(ubqrpending);
        addTDE(tde_execp);
        
        if (pri_in != null) {
            Var pvar = pri_in.getVar();
            // determine if a waitprilock rather than a waitpri -
            // the var field in the WordSpec is set
            // to null if a waitpri and to the priority Var if
            // a waitprilock
            if (pvar != null)
                ((Priority)pvar).addlockSignal(pri_out, loc);
        }

        return(start_del);
    }
    
    /**
     * Connect a RHS value to a TDEVar. It is
     * assumed that a type check has already been performed. Components
     * will be expanded or truncated as required.
     * This is only called from memRead() and memWrite() where exec signals are required.
     * @param   tdev is the TDEVar to be connected to
     * @param   val is the value to connect to the TDEVar
     * @param   exec is the execute signal
     * @param   mess is an error message header
     * @param   loc is the source file location
     */
    public void connect (TDEVar tdev, Val val, TDEVar exec, String mess, SrcLoc loc) {
        WordSpec    lws = tdev.getWordSpec();
        int         words = lws.numWords();
        TDEVar      t;
        boolean     signed = false;
        
        // Connect each word of the RHS source value to the matching
        // word of the LHS TDEVar.
        for (int i=0 ; i<words ; i++) {
            if (val.isTarget()) {
                // RHS is a target TDEVar
                TDEVar  rtdev = val.getTDEVar();
                Ptype   rptype = val.getWordSpec().getPrimType(i);
                
                signed = (rptype == Ptype.INT) || (rptype == Ptype.FIXED);
                if (((lws.getPrimType(i) == Ptype.UINT) || (lws.getPrimType(i) == Ptype.UFIXED)) && signed)
                    throw new ExEx(mess + "unsigned type assigned signed value", loc);
                t = rtdev.getWord(lws, i, loc);
                if (exec != null)
                    val.addExec(exec);
                
            } else {
                // RHS is a constant TDEVar
                t = new TDEVar(val.getVal(i), lws, i, loc);
                long    rcv = ((Long)t.getConst()).longValue();
                if (t.numBits() > lws.getWidth(i))
                    throw new ExEx(mess + " assigned larger immediate value", loc);
                if (((lws.getPrimType(i) == Ptype.UINT) || (lws.getPrimType(i) == Ptype.UFIXED)) && (rcv < 0))
                    throw new ExEx(mess + "unsigned type assigned -ve value", loc);
                if (rcv < 0)
                    signed = true;
            }
            connect(tdev.getWord(i, loc), t, signed);
        }
    }

    /**
     * Connect two TDEVars.
     * If either argument is null or they are already connected, just return.
     * Otherwise a CONNECT TDE is generated.
     * If the two TDEVars are each complete, i.e. do not represent a part of a variable such
     * as an array member or a struct field, then the CONNECT TDE may be optimised out later
     * and the TDEVars merged (see method optimiseTDEList()).
     * @param dtdevar sink TDEVar
     * @param stdevar source TDEVar
     */
    public void connect (TDEVar dtdevar, TDEVar stdevar) {
        if ((dtdevar == null) || (stdevar == null))
            return;
            
        if (dtdevar.equalOrLinked(stdevar))
            return;
        
        //WordSpec    ws1 = dtdevar.getWordSpec();
        //WordSpec    ws2 = stdevar.getWordSpec();
        
        // Generate a CONNECT TDE.
        // After execution has completed connects will occur by merging TDEVars
        // unless the sizes are different, in which case a CAST will be generated.
        // A CAST is a CONNECT with sign-extend parameter added.
        TDE         tde = new TDE(TDEType.CONNECT);
        
        if ((stdevar.numBits() != 1) && (dtdevar.numBits() != stdevar.numBits()))
            throw new ExEx("CONNECT SIZE MISMATCH");
        
        tde.add2o(dtdevar);
        tde.add2i(stdevar);
        addTDE(tde);
        return;
    }
    
    /**
     * Connect two TDEVars with all the available options.
     * @param dtdevar sink TDEVar
     * @param stdevar source TDEVar
     * @param signed if non-null is true for sign extension and false for zero padding
     */
    public void connect (TDEVar dtdevar, TDEVar stdevar, boolean signed) {
        TDE         tde = new TDE(TDEType.CONNECT);
        tde.add2p(signed);
        tde.add2o(dtdevar);
        tde.add2i(stdevar);
        addTDE(tde);
        return;
    }
    
    /*
     * 
     */
    public boolean isGND (TDEVar tdev) {
        TDE stde = tdev.getSingleSrc();
        if (stde.getType() != TDEType.CONNECT)
            return(false);
        TDEVar  stdev = stde.getInput(0);
        return (stdev.isGND());
    }
    
    /**
     * Traverse the TDE list eliminating unused declarations.
     */
    public void trimTDEListVariables () {
        /*
         * Traverse the variable section of the TDE list eliminating
         * those declarations which are signals which are aliased
         * or have no source or destination.
         */
        for (int i=0; i<variables; i++) {
            TDE                 tde = get(i);
            ArrayList<TDEVar>   l = tde.getInputs();
            TDEVar              t = l.get(0);

            if (tde.isActive() &&
                (t.getWordSpec() == null) &&
                (t.isConnected() || ((t.getSrcListSize() == 0) &&
                                   (t.getDestListSize() == 0)))) {
                /*
                if (debug)
                    System.out.println("delete  " + t);
                */
                tde.deactivate();
            }
        }
    }
    
    /**
     * Traverse the TDE list finding all DELs which wait on
     * asynchronous unbuffered queues. The parameter of these is a
     * QueueRefs class rather than an Integer. The parameter in that
     * case is changed to an Integer whose value is 2 if the QueueRefs
     * contains an asynchronous unbuffered queue, otherwise the value
     * is 1.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void resolveDELs () {
        for (TDE del : dels) {
            if (!del.isActive())
                continue;
            ArrayList   al = del.getParams();
            if (!(al.get(0) instanceof QueueRefs))
                continue;
            QueueRefs    qr = (QueueRefs)al.get(0);
            al.clear();
            al.add(Integer.valueOf(qr.asyncUnbufferedQueue() ? 2 : 1));
        }
    }

    /**
     * Traverse the TDE list optimising some redundant TDEs.
     * This optimisation is generic, not family-specific
     * @param   continuous if true finds any simple ILOOPs with one EXECP driving
     * one DEL and removes the ILOOP, EXECP and DEl. The resulting start signal is
     * now the avail input to the removed EXECP looped through pri_in and pri_out
     * if they are non-null.
     * @return  true if the pass resulted in any changes
     */
    public boolean optimiseTDEList (boolean continuous) {
        changes = false;
         
        // Examine CONNECT TDEs and eliminate suitable ones, merging the TDEVars.
        // This will only merge very simple cases -
        //      * clock signals
        //      * single control signals (no WordSpec)
        //      * arrays where the TDE output is a complete variable and the WordSpecs
        //          of input and output are equal
        // This is nevertheless a significant number of cases and results in a simpler
        // and more human-readable TDE list.
        //
        if (boolDir("optimiseConnect")) {
            for (TDE tde : connects) {
                // If CONNECT TDE is not active, skip.
                if (!tde.isActive())
                    continue;
                TDEVar      in = tde.getInput(0);
                TDEVar      out = tde.getOutput(0);
                
                TDE     intde = in.getSingleSrc();
                if ((intde != null) && (intde.getType() == TDEType.CONNECT))
                    continue;
                
                // If 'out' or 'in' do not specify complete variables, do not merge.
                if (!out.isComplete() || !in.isComplete())
                    continue;
                
                // if 'in' and 'out' have different sizes do not merge.
                if (in.numBits() != out.numBits())
                    continue;
                
                // Remove the CONNECT TDE and Merge the TDEVars.
                tde.deactivate();
                in.merge(out);
                changes = true;
            }
        }
        // Find any CONNECT TDEs where the input TDEVar is a single signal and the
        // output TDEvar connects only to a single REG TDE CE, RES, or D input.
        // Remove the CONNECT TDE and connect the single signal TDEVar directly
        // to the REG TDE input.
        //
        if (boolDir("optimiseConnect")) {
            for (TDE tde : connects) {
                // If CONNECT TDE is not active, skip.
                if (!tde.isActive())
                    continue;
                TDEVar      in = tde.getInput(0);
                TDEVar      out = tde.getOutput(0);
                
                // If CONNECT output not single destination, skip.
                if (out.getDestListSize() != 1)
                    continue;
                TDE     outtde = out.getSingleDest();
                // If destination TDE not REG, skip.
                if (outtde.getType() != TDEType.REG)
                    continue;
                // If CONNECT TDE cast parameter is present, skip.
                if ((tde.getParams().size() != 0))
                    continue;
                // If input signal is not a single bit, skip.
                if (in.numBits() != 1)
                    continue;
                
                // Remove the CONNECT TDE.
                tde.deactivate();
                
                in.addDest(outtde);
                outtde.replaceInput(out, in);
                changes = true;
            }
        }


        // Combine any EXECPs with a common start and identical avail signal
        // and no priority input signals.
        for (TDE execp : execps) {
            // Pass every active EXECP to optimise1().
            if (execp.isActive())
                optimise1(execp);
        }

        // Combine any parallel DEL blocks driving a common WAIT.
        for (TDE del : dels) {
            // skip inactive DELs or DELs with delay more than 1 or DELs more than 1 bit wide
            if (!del.isActive())
                continue;
            int    n = del.getInput(1).numBits();
            if (((int)del.getParam(0) != 1) || (n != 1))
                continue;
            // Pass active DELs to optimise2().
            TDEVar  start = del.getInput(1);
            optimise2(del, start);
        }

        // Eliminate any DEL block in parallel with one or more EXECPs driving a common WAIT.
        for (TDE execp : execps) {
            // Pass every active EXECP to optimise3().
            if (execp.isActive())
                optimise3(execp);
        }
        
        // Eliminate any DEL block in parallel with another DEL block
        // where they have the same start signal, combining the destinations.
        for (TDE del : dels) {
            // skip inactive DELs or DELs with delay more than 1 or DELs more than 1 bit wide
            if (!del.isActive())
                continue;
            int    n = del.getInput(1).numBits();
            if (((int)del.getParam(0) != 1) || (n != 1))
                continue;
            // Pass active DELs to optimise8().
            TDEVar  start = del.getInput(1);
            optimise8(del, start);
        }
        
        // Eliminate any WAIT that has a single input.
        for (TDE wait : waits) {
            // wait     the WAIT TDE
            // tde1     the TDE driving the WAIT
            // v1       the signal between tde1 and tde2
            // v2       the output signal of tde2
            if (wait.isActive() && (wait.getInputs().size() == 3)) {
                TDEVar  v1;
                TDEVar  v2;
                v2   = wait.getOutput(0);
                v1   = wait.getInput(2);
                // make the WAIT TDE inactive and connect
                // its input and output signals
                wait.deactivate();
                v1.merge(v2);
                changes = true;
            }
        }
        
        // Check for any SELECTs with duplicated data inputs.
        // Where found, eliminate duplicate inputs and OR the
        // select signals to select the first.
        for (TDE sel: sels) {
            if ((sel.isActive() && sel.redundant()))
                changes = true;
        }

        // Find any AND or OR gates which have duplicated inputs
        // and remove the duplicates. If this results in a single
        // input remaining, render the TDE inactive and connect
        // the input to the output.
        Iterator<TDE>    git = gates.iterator();
        while (git.hasNext()) {
            TDE gate = git.next();
            if (!gate.isActive() ||
                (gate.getType() != TDEType.AND) && (gate.getType() != TDEType.OR))
                continue;

            TDEVar              out = gate.getOutput(0);
            ArrayList<TDEVar>   inlist = gate.getInputs();

            for (int j=0 ; j<inlist.size() ; j++) {
                TDEVar  v1 = inlist.get(j);
                switch (gate.getType()) {
                case AND:
                    if (v1.isGND()) {
                        gate.deactivate();
                        git.remove();
                        TDEVar.GND.merge(out);
                        changes = true;
                    } else if (v1.isVCC())
                        gate.removeInput(v1);
                    break;
                case OR:
                    if (v1.isVCC()) {
                        gate.deactivate();
                        git.remove();
                        TDEVar.VCC.merge(out);
                        changes = true;
                    } else if (v1.isGND())
                        gate.removeInput(v1);
                    break;
                default:
                    break;
                }
                for (int k=j+1 ; k<inlist.size() ; k++) {
                    TDEVar  v2 = (inlist.get(k));
                    if (v1.equalOrLinked(v2)) {
                        inlist.remove(k);
                        v1.removeDest(gate);
                        changes = true;
                    }
                }
            }

            if (inlist.size() == 1) {
                TDEVar  gin = gate.getInput(0);
                TDEVar  gout = gate.getOutput(0);
                gate.deactivate();
                git.remove();
                tdelist.connect(gout, gin); // can't use TDEVar.connect() here as might have WordSpecs
                changes = true;
            }
        }

        // Find any INV gates which have GND or VCC in.
        // Remove and connect the output to the complement.
        git = gates.iterator();
        while (git.hasNext()) {
            TDE gate = git.next();
            if (!gate.isActive() || (gate.getType() != TDEType.INV))
                continue;

            TDEVar  in = gate.getInput(0);
            TDEVar  out = gate.getOutput(0);
            if (in.equalOrLinked(TDEVar.VCC)) {
                gate.deactivate();
                git.remove();
                TDEVar.GND.merge(out);
                changes = true;
            } else if (in.equalOrLinked(TDEVar.GND)) {
                gate.deactivate();
                git.remove();
                TDEVar.VCC.merge(out);
                changes = true;
            }
        }

        // Find any nested AND, OR or XOR gates and consolidate them.
        for (TDE gate : gates) {
            if (!gate.isActive() ||
                (gate.getType() != TDEType.AND) &&
                (gate.getType() != TDEType.OR) &&
                (gate.getType() != TDEType.XOR))
                continue;

            ArrayList<TDEVar> a1 = gate.getInputs();

            for (int j=0 ; j<a1.size() ; j++) {
                TDEVar  v1 = a1.get(j);
                if (v1.isConst())
                    break;  // a constant
                if (v1.getDestListSize() != 1)
                    break;  // Nested gate goes to more than 1 destination!
                            // Could choose to duplicate the logic here to
                            // reduce the logic level.
                TDE     tde2 = v1.getSingleSrc();
                if (tde2 == null)
                    break;  // is null where source is a program variable
                if (tde2.getType() == gate.getType()) {
                    gate.removeInput(v1);
                    ArrayList<TDEVar>   a2 = tde2.getInputs();
                    for (int k=0 ; k<a2.size() ; k++) {
                        TDEVar  v2 = a2.get(k);
                        v2.addDest(gate);
                        gate.add2i(v2);
                    }
                    changes = true;
                    tde2.deactivate();
                }
            }
        }

        // Find any control (not source code) AND, OR or INV gates
        // whose outputs are not connected and make them
        // inactive.
        for (TDE gate : gates) {
            if (!gate.isActive() ||
                (gate.getType() != TDEType.AND) &&
                (gate.getType() != TDEType.OR) &&
                (gate.getType() != TDEType.INV) )
                continue;

            TDEVar          ov = gate.getOutput(0);
            ArrayList<TDE>  oa = ov.getDestList();

            if ((ov.getWordSpec() == null) && (oa.size() == 0)) {
                gate.deactivate();
                changes = true;
            }
        }
 
        // Find any DEL blocks whose outputs are not connected
        // and make them inactive.
        for (TDE del : dels)
            if (del.isActive())
                optimise6(del);
       
        // Find any WHENs which have a single DEL in both the true block
        // and the false block. Remove these and instead connect a single
        // DEL from the start input to both the true block finish input
        // and the false block finish input. This will result in an OR
        // with identical inputs which will be eliminated by another
        // optimisation.
        for (TDE when : whens) {
            if (!when.isActive())
                continue;

            if (when.getInput(2) == null)
                continue;   // already handled in previous iteration

            TDEVar          tbs = when.getOutput(0);
            TDEVar          fbs = when.getOutput(1);
            TDEVar          finish = when.getOutput(2);
            TDEVar          start = when.getInput(0);
            TDEVar          tbf = when.getInput(2);
            TDEVar          fbf = when.getInput(3);
            TDE             tbs_tde = null;
            TDE             fbs_tde = null;
            ArrayList<TDE>  a;

            a = tbf.getSrcList();
            if (a.size() == 0)
                continue;
            TDE tt = a.get(0);
            if (tt.getType() != TDEType.DEL)
                continue;   // not a DEL - give up
            if (fbf == null)
                continue;
            a = fbf.getSrcList();
            if (a.size() == 0)
                continue;
            TDE ft = a.get(0);
            if (ft.getType() != TDEType.DEL)
                continue;   // not a DEL - give up
            a = tbs.getDestList();
            for (int j=0; j<a.size(); j++) {
                tbs_tde = a.get(j);
                if (tbs_tde == tt) {
                    a = fbs.getDestList();
                    for (int k = 0; k < a.size(); k++) {
                        fbs_tde = a.get(k);
                        if (fbs_tde == ft) {
                            // disconnect true block finish TDEVar from WHEN input
                            tbf.removeDest(when);
                            //when.replaceInput(tbf, null); // we know the index so use it (below)
                            when.replaceInput(2, null);
                            // disconnect false block finish TDEVar from WHEN input
                            fbf.removeDest(when);
                            //when.replaceInput(fbf, null); // we know the index so use it (below)
                            when.replaceInput(3, null);
                            // switch false block DEL input from fbs to start
                            //ft.replaceInput(fbs, start); // we know the index so use it (below)
                            ft.replaceInput(1, start);
                            fbs.removeDest(ft);
                            start.addDest(ft);
                            // switch false block DEL output from fbf to finish
                            //ft.replaceOutput(fbf, finish);
                            ft.replaceOutput(0, finish);
                            fbf.removeSrc(ft);
                            finish.removeSrc(when);
                            finish.addSrc(ft);
                            //when.replaceOutput(finish, null);
                            when.replaceOutput(2, null);
                            // if true block DEL has no other destinations, remove it
                            if (tbf.getDestListSize() == 0)
                                tt.deactivate();
                            changes = true;
                        }
                    }
                }
            }
        }

        // Find any ILOOPs which have the same output start signal and
        // input finish signal. These infinite loops are empty, probably
        // because they contain only value equivalence statements.
        for (TDE iloop : iloops) {
            if (!iloop.isActive())
                continue;
            if (iloop.getInput(1) == iloop.getOutput(0)) {
                iloop.deactivate();
                changes = true;
            }
        }
        
        // Find any ILOOPs which have a single DEL from the output start signal
        // to the input finish signal. Replace the pair with a DFF whose
        // SET input is driven by the start signal.
        for (TDE iloop : iloops) {
            // Pass active ILOOP to optimise4().
            if (iloop.isActive())
                optimise4(iloop);
        }

        // If 'continuous' is true, find any ILOOPs which have an EXECP
        // driving a single DEL from the output start signal to the input
        // finish signal. Drive the output start signal from the EXECP
        // avail signal directly, removing the ILOOP, EXECP and DEL.
        if (continuous)
            for (TDE iloop : iloops) {
                // Pass active ILOOP to optimise7().
                if (iloop.isActive())
                    optimise7(iloop);
            }
        
        // Find multiple DFFs driven by a common START and eliminate
        // all but one.
        for (TDE start_dff : start_dffs) {
            // Find every (active) DFF (FDRS) and pass it to optimise5().
            // param[1] true means that it cannot be an FDRS
            // input 4 non-null means that it must have an S input
            if (!start_dff.isActive() ||                  // not active
                (start_dff.getType() != TDEType.DFF) ||   // not a DFF
                (boolean)start_dff.getParam(1) ||         // an FDCE or FDPE, not FDRS
                (start_dff.getInput(0) != null) ||        // has D input!
                (start_dff.getInput(2) != null) ||        // has CE input!
                (start_dff.getInput(3) != null) ||        // has R input!
                (start_dff.getInput(4) == null))          // has no S input
                continue;   // skip it

            optimise5(start_dff);
        }
        
        // Find any branch statements in which all branches, including the
        // default, have a single DEL. Remove all the DELs and the final associated
        // OR and replace with a single DEL from the start signal.
        Iterator<branch_optimise>    boit = branches.iterator();
        while (boit.hasNext()) {
            branch_optimise bo = boit.next();
            boolean         skip = false;
            ArrayList<TDE>  dels = new ArrayList<TDE>();
            for (TDEVar tdev : bo.and_outs) {
                TDE     del = null;
                for (TDE tde : tdev.getDestList()) {
                    if (tde.getType() == TDEType.DEL) {
                        del = tde;
                        break;
                    }
                }
                if (del == null) {
                    skip = true;    // did not find a DEL -
                    break;          // give up on this branch statement
                }
                TDEVar  tdev_dout = del.getOutput(0);
                if (tdev_dout.getSingleDest() != bo.or) {
                    skip = true;    // DEL does not go to final OR
                    break;          // give up on this branch statement
                }
                dels.add(del);      // save DEL for possible deletion
            }
            if (skip)
                continue;

            boolean first = true;
            TDE     d = null;
            for (TDE del : dels) {
                if (first)
                    d = del;
                else
                    del.deactivate();
                first = false;
            }
            bo.or.deactivate();
                       
            TDEVar  delin = d.getInput(1);
            TDEVar  delout = d.getOutput(0);

            //d.replaceInput(delin, bo.start);
            d.replaceInput(1, bo.start);
            //d.replaceOutput(delout, bo.finish);
            d.replaceOutput(0, bo.finish);
            delin.removeDest(d);
            delout.removeSrc(d);
            bo.start.addDest(d);
            bo.finish.addSrc(d);
            
            changes = true;
            boit.remove();  // remove from list to avoid future attempts
        }
        
        // Find any DFFs which -.
        //  * have no output connections - remove
        //  * have VCC reset - remove and connect output to GND
        Iterator<TDE>    dffit = dffs.iterator();
        while (dffit.hasNext()) {
            TDE             dfftde = dffit.next();
            TDEVar          ov = dfftde.getOutput(0);
            ArrayList<TDE>  oa = ov.getDestList();
            TDEVar          r = dfftde.getInput(3);

            if (oa.size() == 0) {
                dfftde.deactivate();
                dffit.remove();
                changes = true;
            } else if ((r != null) && r.equalOrLinked(TDEVar.VCC)) {
                dfftde.deactivate();
                dffit.remove();
                TDEVar.GND.merge(ov);
                changes = true;
            }
        }
        
        // Find any CONNECT TDEs whose outputs go nowhere and eliminate them.
        // THIS DOES NOT WORK - IT BREAKS CONNECTIONS!
        //
        /*if (optimiseConnect) {
            for (TDE tde : connects) {
                if (!tde.isActive())
                    continue;
                TDEVar  out = tde.getOutput(0);
                //if (out.isComplete() && (out.numWords() == 1) && (out.getDestListSize() == 0)) {
                if (!out.outputIsConnected()) {
                    tde.deactivate();
                    changes = true;
                }
            }
        }*/
        
        // Find any operator TDEs whose outputs go nowhere and eliminate them.
        // THIS DOES NOT WORK - IT BREAKS CONNECTIONS!
        /*for (TDE tde : ops) {
            if (!tde.isActive())
                continue;
            if (tde.getOutputs().size() == 0) {
                tde.deactivate();
                changes = true;
            } else {
                TDEVar  out = tde.getOutput(0);
                if (out.getDestListSize() == 0) {
                    tde.deactivate();
                    changes = true;
                }
            }
        }*/
        
        // Examine REG TDEs for CE or R inputs connected to a single source via a CONNECT TDE.
        // Replace the CE or R input with the single source end remove the CONNECT TDE.
        // CODE SEEMS CORRECT BUT CREATES ERRORS ELSEWHERE!
        /*ArrayList<TDE>  ral = getRegs();
        for (TDE rtde : ral) {
            TDEVar  ce = rtde.getInput(2);
            if (ce == null)
                continue;   // constant REG (no inputs)
            TDE     cetde = ce.getSingleSrc();
            if (cetde == null)
                continue;   // a constant
            if (cetde.getType() != TDEType.CONNECT)
                continue;
            TDEVar  tdevs = cetde.getInput(0);
            if (tdevs.numBits() != 1)
                continue;
            cetde.deactivate();
            rtde.replaceInput(2, tdevs);
            tdevs.addDest(rtde);
            //ce.clearDestList();
        }*/

        // Find any divide and remainder operators that have the same
        // data and control inputs and combine them into a single
        // divide-remainder.
        
        return(changes);
    }

    /**
     * Traverse the TDE list checking for any signal linking inconsistencies.
     */
    // WARNING - this has not been used for a long time. Its call in
    // ThreePL.java is only uncommented when a problem with the TDE list
    // arises after source code changes. It may have atrophied due to
    // changes in the signal aliasing scheme that have occurred since then!
    public void linkCheck () {
        // Traverse the TDE list checking for consistency in the linking
        // between TDEs and TDEVars for control logic.
        // A for () loop is used rather than an iterator because
        // we want to traverse the list starting at a point after
        // the variable declarations.
        for (int i=variables; i<size(); i++) {
            TDE tde = get(i);
            tde.checkLinks();
        }
    }

    /**
     * Traverse the TDE list checking for any logic inconsistencies.
     */
    // Hardly a logic check! it just looks for one small error, which I
    // think has never occurred.
    public void logicCheck () {
        // A for () loop is used rather than an iterator because
        // we want to traverse the list starting at a point after
        // the variable declarations.

        // Traverse the TDE list checking if any SELECTs have the same
        // select input signal repeated.
        for (int i=variables; i<size(); i++) {
            TDE tde = get(i);
            // Find every (active) SELECT and pass it to check1().
            if (tde.isActive() && (tde.getType() == TDEType.SELECT))
                check1(tde);
        }
    }
    
    private void check1 (TDE tde) {
        TDEVar  v1;
        TDEVar  v2;
        int     size = tde.getInputs().size();
        for (int i=0 ; i<size ; i+=2) {
            v1 = tde.getInput(i);
            for (int j=i+2 ; j<size ; j+=2) {
                v2 = tde.getInput(j);
                if (v1.equalOrLinked(v2)) {
                    TDEVar  t = tde.getOutput(0);
                    Var     v = t.getVar();
                    msg("");
                    if (v != null)
                        msg("error - variable '" +
                                    v.getID(IDtype.SLITERAL) +
                                    "' defined " +
                                    v.getLoc() +
                                    " has conflicting assignments");
                    else
                        msg("error - selector with output " + t.getId() + " has conflicting assignments");
                    errors++;
                }
            }
        }
    }

    private void optimise1 (TDE tde1) {
        TDEVar          v1 = tde1.getInput(1);
        ArrayList<TDE>  a = v1.getDestList();

        for (int i = 0; i < a.size(); i++) {
            TDE tde2 = a.get(i);

            if (tde2.isActive() && (tde2.getType() == TDEType.EXECP) &&
                (tde2 != tde1) && tde1.availEquals(tde2) &&
                (tde1.getOutput(1) == null) && (tde2.getOutput(1) == null) &&   // no PRI_IN output
                (tde1.getOutput(2) == null) && (tde2.getOutput(2) == null) &&   // no UBQ write pending
                (tde1.getOutput(3) == null) && (tde2.getOutput(3) == null)) {   // no UBQ read pending
                
                TDEVar  v2 = tde1.getOutput(0);
                TDEVar  v3 = tde2.getOutput(0);
                tde2.deactivate();
                v2.merge(v3);
                changes = true;
            }
        }
    }

    // Parallel DELs into a WAIT.
    // Check that we have a WAIT driven by this TDEVar.
    // If so, call optimise2_1
    private void optimise2 (TDE del, TDEVar start) {
        TDEVar  vo = del.getOutput(0);
        for (TDE tdeo : vo.getDestList()) {
            if (tdeo.isActive() && (tdeo.getType() == TDEType.WAIT)) {
                optimise2_1(del, start, tdeo);
                break;
            }
        }
    }
        
    // Parallel DELs into a WAIT.
    // Iterate through other TDEs driven by same input signal -
    // for each DEL we find, call optimise2_2.
    //
    private void optimise2_1 (TDE tde_del1, TDEVar start, TDE tde_wait) {
        ArrayList<TDE> al = start.getDestList();
        for (int i=0 ; i<al.size() ; i++) {
            TDE tde2 = al.get(i);
            if (tde2.isActive() && (tde2.getType() == TDEType.DEL) && (tde2 != tde_del1))
                optimise2_2(tde_del1, tde2, tde_wait);
        }
    }
  
    // Parallel DELs into a WAIT.
    // Make sure the DELs have the same delay.
    // Make sure the second DEL drives the same WAIT as the first.
    // If so, call optimise2_3
    private void optimise2_2 (TDE tde_del1, TDE tde_del2, TDE tde_wait) {
        if ((int)tde_del1.getParam(0) != (int)tde_del2.getParam(0))
            return; // give up if different delays
        TDEVar      vo = tde_del2.getOutput(0);
        for (TDE tdeo : vo.getDestList()) {
            if (tdeo == tde_wait) {
                optimise2_3(tde_del1, tde_del2, tde_wait);
                break;
            }
        }
    }
    
    
    // Parallel DELs into a WAIT.
    // Remove the second DEL.
    private void optimise2_3 (TDE tde_del1, TDE tde_del2, TDE tde_wait) {
        TDEVar  dv1   = tde_del1.getOutput(0);
        TDEVar  dv2   = tde_del2.getOutput(0);

        // remove the 2nd DEL signal from the WAIT
        tde_wait.removeInput(dv2);
        // remove the 2nd DEL
        tde_del2.deactivate();
        // connect the two DEL output signals
        dv1.merge(dv2);
        changes = true;
    }

    // DEL block in parallel with one or more EXECPs driving a common WAIT.
    private void optimise3 (TDE tde1) {
        TDEVar      v1    = tde1.getInput(1);
        TDEVar      v2    = tde1.getOutput(0);
        TDE         tde3  = null;
        TDEVar      v3;
        TDE         dtde1;
        
        for (TDE t: v2.getDestList()) {
            if (t.getType() == TDEType.DEL) {
                tde3 = t;
                break;
            }
        }
        if (tde3 == null)
            return;

        v3    = tde3.getOutput(0);
        dtde1 = v3.getSingleDest();

        if ((dtde1 == null) || (dtde1.getType() != TDEType.WAIT))
            return;

        ArrayList<TDE>  a = v1.getDestList();

        for (int i = 0; i < a.size(); i++) {
            TDE tde2 = a.get(i);

            if (tde2.isActive() && (tde2.getType() == TDEType.DEL)) {
                TDEVar  v4    = tde2.getOutput(0);
                TDE     dtde2 = v4.getSingleDest();

                if ((dtde2 != null) && (dtde1 == dtde2)) {
                    // Render the DEL inactive and remove its output
                    // TDEVar from the input ArrayList of the WAIT.

                    // remove the 2nd DEL signal from the WAIT
                    dtde2.removeInput(v4);
                    // remove the 2nd DEL
                    tde2.deactivate();
                    // connect the two DEL output signals
                    v3.merge(v4);         //??????????????????????????????????????????????????????
                    
                    changes = true;
                }
            }
        }
    }

    // ILOOP block with single DEL feedback.
    private void optimise4 (TDE tde1) {
        TDEVar      v1    = tde1.getInput(1);   // 'finish' input
        TDEVar      v2    = tde1.getOutput(0);
        TDE         tde2  = null;
        
        for (TDE tde: v2.getDestList()) {
            tde2 = tde;
            if (tde.getType() == TDEType.DEL)
                break;
        }
        if (tde2 == null)
            return;
        TDEVar      v3 = tde2.getOutput(0);
        if (v1.equals(v3)) {
            TDEVar  v4 = tde1.getInput(0);  // 'start' input
            TDEVar  clk = tde2.getInput(0); // clock (from the DEL)
            tde1.deactivate();
            tde2.deactivate();
            if (v1.getDestListSize() != 0)
                TDEVar.VCC.merge(v1);
            if (continuous) {
                TDEVar.VCC.merge(v2);
            } else {
                TDEVar  v5 = tdelist.signal("SDD", null);
                fdrse(v5, GND, clk, GND, GND, v4, "R", null);
                start_dffs.add(v5.getSingleSrc());
                v5.merge(v2);
            }
            changes = true;
       }
    }
    
    
    // ILOOP block with EXECP and single DEL feedback.
    private void optimise7 (TDE tde1) {
        TDEVar          v1    = tde1.getInput(1);   // 'finish' input
        TDEVar          v2    = tde1.getOutput(0);
        TDE             tde2  = null;
        TDE             tde3  = null;
        ArrayList<TDE>  a;
        
        a = v2.getDestList();
        if (a.size() != 1)
            return;
        tde2 = a.get(0);
        if (tde2.getType() != TDEType.EXECP)
            return;
        TDEVar      v3 = tde2.getOutput(0);

        a = v3.getDestList();
        if (a == null)
            return;
        if (a.size() != 1)
            return;
        tde3 = a.get(0);
        if (tde3.getType() != TDEType.DEL)
            return;

        TDEVar  v4 = tde3.getOutput(0);
        if (v1.equals(v4)) {
            TDEVar  v5 = tde2.getInput(2);   // 'avail' input to EXECP
            TDEVar  vpo = tde2.getInput(6);  // 'pri_out' input to EXECP
            TDEVar  vpi = tde2.getOutput(1); // 'pri_in' output to EXECP
            tde1.deactivate();
            tde2.deactivate();
            tde3.deactivate();
            if (vpi != null) {
                vpi.merge(v5);
                vpo.merge(v3);
            } else
                v5.merge(v3);
            changes = true;
       }
    }

    // Parallel start DFFs.
    // Find another start DFF and call optimise5_1.
    private void optimise5 (TDE tde1) {
        TDEVar          v1 = tde1.getInput(4);  // S input
        ArrayList<TDE>  a = v1.getDestList();
        
        for (int i=0; i<a.size(); i++) {
            TDE tde2 = a.get(i);
            if (!tde2.isActive() ||                 // not active
                (tde2.getType() != TDEType.DFF) ||  // not a DFF
                (tde2 == tde1) ||                   // same DFF
                (tde2.getInputs().size() < 5) ||    // has no S input
                (tde2.getInput(0) != null) ||       // has D input!
                (tde2.getInput(2) != null) ||       // has CE input!
                (tde2.getInput(3) != null))         // has R input!
                continue;   // skip it
            optimise5_1(v1, tde1, tde2);
        }
    }

    // Parallel DFFs.
    // Remove the second DFF.
    private void optimise5_1 (TDEVar v1, TDE tde1, TDE tde2) {
        TDEVar      v2= tde1.getOutput(0);
        TDEVar      v3 = tde2.getOutput(0);
        
        v1.removeDest(tde2);
        
        ArrayList<TDE>   a = v3.getDestList();
        for (int i=0; i<a.size(); i++) {
            TDE     tde3 = a.get(i);
            //tde3.replaceInput(v3, v2);
            tde3.replaceInput(0, v2);
            v2.addDest(tde3);
        }
        v3.clearSrcList();
        v3.clearDestList();
        tde2.deactivate();
        changes = true;
    }
    
    // Make inactive a DEL block whose output is not connected.
    private void optimise6 (TDE tde) {
        TDEVar          ov = tde.getOutput(0);
        ArrayList<TDE>  oa = ov.getDestList();

        if (oa.size() == 0) {
            tde.deactivate();
            changes = true;
        }
    }

    // Parallel DELs.
    // Iterate through other DEL TDEs driven by same input signal -
    // for each DEL we find, call optimise8_1.
    private void optimise8 (TDE del, TDEVar start) {
        ArrayList<TDE> al = start.getDestList();
        for (int i=0 ; i<al.size() ; i++) {
            TDE tde2 = al.get(i);
            if (tde2.isActive() && (tde2.getType() == TDEType.DEL) && (tde2 !=del) && (start.equalOrLinked(tde2.getInput(1))))
                optimise8_1(del, tde2, start);
        }
    }
    
    // Two parallel DELs to different destinations.
    //
    private void optimise8_1 (TDE tde_del1, TDE tde_del2, TDEVar start) {
        TDEVar  dv1   = tde_del1.getOutput(0);
        TDEVar  dv2   = tde_del2.getOutput(0);
        // remove the 2nd DEL
        tde_del2.deactivate();
        // connect the two DEL output signals
        dv1.merge(dv2);
        changes = true;
    }

    /*-------------------------------------------------------------------------
     * NEW OPTIMISATION PATTERN MATCHER.
     * COMPLETE LATER!
     * Early idea never advanced - may or may not be still appropriate.
     *
    // pattern match.
    //  patt        the pattern to find
    //  achor       specific first node or null
    //  circular    if true, terminate the match on the starting point
    private int[] match (int[] patt, TDE anchor, boolean circular) {
        int[]       m = new int[patt.length];
        ArrayList   al;
        Iterator    it;
        if (patt[0] == -99) {
            // 1st pattern element is a TDEVar
            if (anchor != null) {
                // pattern is anchored
            } else {
                // pattern is not anchored
            }
        } else {
            // 1st pattern element is a TDE
            if (anchor != null) {
                // pattern is anchored
                m[0] = anchor;
                if (patt[1] == 1)
                    // input
                    al = anchor.getInputs.iterator();
                else
                    // output
            } else {
                // pattern is not anchored
            }
        }
        for (int i=variables; i<size(); i++) {
            TDE tde = (TDE) get(i);
            if (anchor != null)
            else
                if (tde.isActive() && (tde.getType() == patt[0]))
    }

    private boolean match_tde (int[] patt, int[] match, int index, TDE first, TDE last) {
    }
    
    private boolean match_tdevar (int[] patt, int[] match, int index, TDE first, TDE last) {
    }
    
    // Convert a pattern string to an int[] for use with 'match()'.
    // Pattern must start with a TDE. If circular, must end with a TDE.
    // TDEs alternate with variables.
    // Each tde is either "TDE" or is the name of an explicit TDE, e.g. "DEL",
    // "EXECP', "WAIT" etc.
    // Each variable must be explicitly "V"
    // Separators must be either "<" or ">" to indicate an input or output
    // from the adjacent TDE.
    private int[] stringToPat (String patt) {
        String[]    split;
        split = patt.split("[<>]");
        int     n = 2 * split.length + 1;
        int[]   list = new int[n];
        int     i1;
        int     i2;
        int     i;
        int     dir;    //0 none, 1 input, 2 output
        int     code;
        String  string;
        
        for (int j=0 ; ; j) {
            // TDE
            i1 = patt.indexOf('<');
            if (i1 < 0)
                i1 = 9999;
            i2 = patt.indexOf('>');
            if (i2 < 0)
                i2 = 9999;
            i = (i1 < i2) i1 : i2;
            if (i == 9999)
                string = patt;
            else {
                string = patt.subSequence(0, i-1);
                patt = patt.subSequence(i+1);
            }

            // code is -
            //  -99 for a variable
            //  99 for a generic TDE
            //  TDE code for an explicit TDE            
            if (patt.equals("V"))
                code = -99;
            else if (patt.equals("TDE"))
                code = 99;
            else
                code = ThreePL.TDE.getTypeCode(patt);

            if (i == 9999) {
                list[j] = code;
                return(list);
            }
            
            // If this is a TDE -
            //      following '<' (i==i1) is an input (1)
            //      following '>' (i==i2) is an output (2)
            // If this is a TDEVar -
            //      following '<' (i==i1) is an output (2)
            //      following '>' (i==i2) is an input (1)
            if (i == i1)    // have '<'
                dir = (code == -99) ? 2 : 1;
            else            // have '>'
                dir = (code == -99) ? 1 : 2;
            list[j++] = tde_code;

            // direction
            list[j++] = dir;
        }
        
        return(null);
    }
    */
    
    /**
     * Traverse the TDE list generating EDIF code.
     * Note that outdir, source_name and design_name
     * are static variables in class ThreePL and msg() is
     * a method in that class.
     */
    public void ncode () {
        File        file;
        FileWriter  fw;
        PrintWriter pw = null;
        String      author = null;
        String      netlistcomment = null;
        Val         val;
            
        nanotime3 = System.nanoTime();  // start of netlist generation

        // create netlist structure
        rpt("\tgenerating netlist from intermediate code");

        if (family == null)
            getFamily();

        val = findDir("author");
        if (val != null) {
            if (val.getPrimType() != Ptype.STR)
                rpt("directive \"author\" is not type str - ignored!"); 
            else
                author = val.getSingleSval(null);
        }     

        val = findDir("netlistComment");
        if (val != null) {
            if (val.getPrimType() != Ptype.STR)
                msg("directive \"netlistComment\" is not type str - ignored!");   
            else
                netlistcomment = val.getSingleSval(null);
        }   
        
        // clear parsing and immediate execution variables to free
        // memory
        //msg("before GC: max " + main_rt.maxMemory() + " total " + main_rt.totalMemory() + " free " + main_rt.freeMemory());
        clear_parse_and_exec_vars();
        System.gc();
        //msg("after GC: max " + main_rt.maxMemory() + " total " + main_rt.totalMemory() + " free " + main_rt.freeMemory());
        
        // eliminate signal declarations from the TDE list as the
        // EDIF code generator does not use them
        removeRange(0, variables);

        rpt("\tintermediate list elements - " + size());
        rpt("\tconvert intermediate list to netlist");
        rpt("\t\tconverting logic");

        /* code to print message every 1% of TDE list processed
        int ss = size();
        int ii = 0;
        int pc;
        int pcl = 0;
        */
        Iterator<TDE>   it   = iterator();
        TDE             tde  = null;
        boolean         f = true;
        while (it.hasNext()) {
            /*
            pc = ((ii++ * 100) / ss);
            if (pcl != pc) {
                msg("\t" + pc + "%    " + main_rt.freeMemory());
                pcl = pc;
            }
            */
            tde = it.next();
            if (f && ((tde.getType() == TDEType.REG) || (tde.getType() == TDEType.QUEUEBUFFER))) {
                f = false;
                rpt("\t\tconverting static and queue variables");
            }
            if (tde.isActive())
                tde.ncode(family);
            it.remove();
        }

        rpt("\tElements before optimisation - " + Element.getNumElements());
        rpt("\tNet IDs before optimisation - " + Net.getNumNetIds());
        rpt("\tNets before optimisation - " + Net.getNumNets());

        String  view = "view_1";
        
        // design_name is used here for output file names
        // cell_name is non-null if this design is a core.
        //
        // If cell_name is null this is a 'main' program and the
        // design is top-level. The output file names are derived from the
        // design name which in turn is the last component of the source
        // file name. Various netlist sections are labelled arbitrarily
        // using the design name.
        //
        // If cell_name is not null the design creates a 'core'. The output
        // file names are derived from the cell name. Various netlist
        // sections are labelled using the design name.
        String  library = (cell_name == null) ? "top_level" : cell_name + "_arch";

        // Open the netlist file
        file = new File(netlist_file);
        try {
            fw = new FileWriter(file);
            pw    = new PrintWriter(fw);
        } catch(IOException e) {
            msg("Cannot create output file '" + netlist_file + "' - exiting");
            System.exit(1);
        }
        
        // if signal 'startex' has no source, connect it to VCC
        Net.fixstartex();

        // do some checks on netlists and elements
        rpt("\tchecking net and element validity before optimisation");
        if (!Net.verify() || !family.verify()) {
            throw new ExEx ("");
        }

        nanotime4 = System.nanoTime();  // start of netlist optimisation
        
        // optimise gates and flip-flops
        if (codeopt) {
            int passes = 0;
            rpt("\toptimising netlist gates");
            passes = family.optimiseNetlist();
            rpt("\t\toptimisation passes: " + passes);
            // convert generic elements to family-specific ones
            // where necessary
            family.convertGenericToSpecific();

            // do some checks again on netlists and elements after optimisation
            rpt("\tchecking net and element validity after optimisation");
            if (!Net.verify() || !family.verify()) {
                // optionally print list of netlist identifiers
                if (boolDir("listnets"))
                    printNetIDs();
                throw new ExEx ("");
            }

            rpt("\tElements after optimisation - " + Element.getNumElements());
            rpt("\tNet IDs after optimisation - " + Net.getNumNetIds());
            rpt("\tNets after optimisation - " + Net.getNumNets());
        }
        
        rpt("\tresolving net identifiers");
        Net.resolveNetIdents();
        
        nanotime5 = System.nanoTime();  // start of netlist output

	// get top-level netlist name, follow strange EDIF name rules
        String  cname = (cell_name != null) ? cell_name : design_name;
        if (! cname.matches("^[A-Za-z].+$"))
            cname = "&" + cname;

        // write out the netlist header
        rpt("\twriting netlist to .edn file");
        pw.println("(edif " + cname);
        pw.println(" (edifVersion 2 0 0)");
        pw.println(" (edifLevel 0)");
        pw.println(" (keywordMap (keywordLevel 0))");
        pw.println(" (status");
        pw.println("  (written");

        SimpleDateFormat edifFormat = new SimpleDateFormat("yyyy M d H m s");
        edifFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String edifDate = edifFormat.format(new Date(currentTime));
        pw.println("   (timestamp " + edifDate + ")");

        if ((author != null) && (author.length() > 0))
            pw.println("   (Author \"" + author + "\")");
        pw.println("   (program \"3PL\" (version \"" + fullVersion + "\"))");
        if ((netlistcomment != null) && (netlistcomment.length() > 0))
            pw.println("   (comment \"" + netlistcomment + "\")");
        pw.println("  )");
        pw.println(" )");

        // write out the netlist cells (elements)
        Element.outputCells(pw, view);

        // write out the design -
        pw.println("");
        pw.println(" (library " + library);
        pw.println("  (edifLevel 0)");
        pw.println("  (technology (numberDefinition ))");
        pw.println("  (cell " + cname + " (cellType GENERIC)");
        pw.println("   (view " + view + " (viewType NETLIST)");

        // write out the module interface ports (external connections)
        TDECode.outputExterns(pw);

        // write out the module contents
        pw.println("    (contents");
        // write out the element instances
        Element.outputInstances(pw, view);

        // write out the nets
        Net.outputInstances(pw);
        pw.println("    )");

        // write out the netlist trailer
        pw.println("   )");
        pw.println("  )");
        pw.println(" )");
        
        // write out the part specification
        pw.println("");
        pw.println(" (design " + cname);
        pw.println("  (cellRef " + cname);
        pw.println("   (libraryRef " + library + ")");
        pw.println("  )");
        if (partstring != null)
            pw.println("  (property PART (string \"" + partstring + "\"))");
        pw.println(" )");
        
        // write out the closing parenthesis to the netlist header
        pw.println(")");

        // close the netlist file
        pw.flush();
        pw.close();
    }

    private void printNetIDs () {
        File   file;
        FileWriter  fw;
        PrintWriter pw = null;
        file = new File(net_file);
        try {
            fw = new FileWriter(file);
            pw    = new PrintWriter(fw);
        } catch(IOException e) {
            System.out.println("Cannot create output file '" + net_file);
        }
        rpt("\twriting list of equivalent nets");
        Net.outputNets(pw);
        pw.flush();
        pw.close();
        
    }
}
