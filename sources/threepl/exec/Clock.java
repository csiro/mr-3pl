package threepl.exec;

import static threepl.ThreePL.*;

import java.util.Set;
import java.util.TreeMap;

import threepl.codegen.TDE;
import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.nodes.Ident;
import threepl.nodes.VarNode;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * This class represents a CLOCK mode variable. The variable has a name String,
 * a mode and a Type (which is "log"). If the variable is a module, procedure
 * or function parameter it may have an indirect pointer to another Var,
 * depending on its mode and whether input or output. The variable will also
 * have an expanded name to ensure that this instance is unique.
 *
 * The following special cases apply -
 * <UL>
 * <LI> If isInPar is true then this variable is a module, procedure
 *      or function input parameter.
 * <LI> If isOutPar is true then this variable is a module or procedure
 *      output parameter.
 * </UL>
 */
public final class Clock extends Var implements Constant, TDEConstants {
    private TDEVar          clock_sig;   // clock signal
    private TDEVar          start_sig;   // clock start signal
    private boolean         clk_sourced; // clock variable has a source
    private long            clk_sinks;   // clock variable destination count
    private Clock           associated;  // associated clock variable if any
    private String[]        ids;         // element block identifiers
    private boolean         valid;       // clock drives logic

    /**
     * Construct a clock mode variable.
     * @param   ident is the variable identifier
     * @param   init is an associated clock signal to which this is linked
     * @param   in_par is true if this is a module, procedure of function
     *          input parameter
     * @param   out_par is true if this is a module or procedure output
     *          parameter
     * @param   loc is the source file location
     */
    public Clock (
        Ident   ident,
        Val     init,
        boolean in_par,
        boolean out_par,
        SrcLoc  loc
    ) {
        super(ident, in_par, out_par, null, Ptype.NONE, loc);

        // Mode.
        mode = Mode.CLOCK;
        
        // Type.
        type = new Type(Ptype.LOG, 1);
        wordspec = type.getWordSpec(this, loc);
        //wordspec = new WordSpec(1, 0, 0, 0, null, null, Ptype.LOG, this); ??????????

        isInPar = in_par;
        isOutPar = out_par;
        
        // Add the block id array to the attributes map.
        ids = new String[1];
        ids[0] = "b" + icount++;
        attributes.put("ids", new Val(ids, "str", loc));
        
        //WordSpec lws = new WordSpec(1, 0, 0, 0, null, null, Ptype.LOG, this);  ????????
        //clock_sig = tdelist.namesignal(ename, lws, loc);                      ?????????
        clock_sig = tdelist.namesignal(ename, wordspec, loc);
        clock_sig.setClockMode(ClkType.POS);
        clock_sig.setClkVar(this);
        start_sig = tdelist.namesignal(ename + hsep + "start", loc);
        
        if (init != null)
            setIndirect(init, loc);
        
        attributes.put("valid", new Val(false, loc));

        // add this to the variable queue for processing at the end
        queueVar(this);
    }
    
    /**
     * Compare two clocks for equality, taking into account indirection.
     * @param   c is the clock to be compared with this
     * @return  true if they are the same clock
     */
    public boolean isEqualTo (Clock c) {
        if (indirect != null)
            return(((Clock)indirect).isEqualTo(c));
        if (c.indirect != null)
            return(this.isEqualTo((Clock)c.indirect));
        return(this == c);
    }
    
    /**
     * Increment the destination count.
     */
    public void incrClkSinks () {
        if (indass) {
            ((Clock)indirect).incrClkSinks();
            return;
        }
        
        clk_sinks++;
    }
    
    /**
     * Increment the clocked element count.
     * CURRENTLY DOES NOTHING!
     * Its previous function might be reinstated in the future.
     */
    public void incrCE () {
        if (indass) {
            ((Clock)indirect).incrCE();
            return;
        }
    }

    /**
     * Set the clk_sourced flag.
     */
    public void setClkSourced () {
        if (indass) {
            ((Clock)indirect).setClkSourced();
            return;
        }

        if (clk_sourced)
            throw new ExEx("clock variable '" + name + " duplicate source");
        clk_sourced = true;
    }
    
    /**
     * Decrement the destination count.
     */
    public void decrClkSinks () {
        if (indass) {
            ((Clock)indirect).decrClkSinks();
            return;
        }

        if (clk_sinks != 0) {
            clk_sinks--;
        }
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
      
        TreeMap<String, Val>    attr = (TreeMap<String,Val>)val.getVal(0);

        Set<String>         ks = attr.keySet();
        for (String mkey: ks) {
            Val     mval = attr.get(mkey);
            mkey = mkey.toLowerCase();

            // Check that attribute is known to 3PL.
            // If it checks out OK, continue processing.
            // If it does not check out skip the rest of the loop.
            switch (checkIOAttribute(mkey, mval, mode, loc)) {
            case DONE:          // locs already added to map by checkIOAttribute()
                continue;
            case UNRECOGNISED:  // Not known - fatal error.
                throw new ExEx("clock() - '" + mkey + "' attribute not known", loc);
            case WRONG_TYPE:    // Has already thrown exception and never returns here
            case OK:            // OK - add to attributes map.
            }
                        
            if (mval.getPrimType() == Ptype.NULL)
                attributes.remove(mkey);
            
            if (mkey.equals("frequency"))
                setClkFreq(mval.getSingleFval(loc), loc);
            else if (mkey.equals("period"))
                setClkFreq(1.0 / mval.getSingleFval(loc), loc);
            else if (mkey.equals("dutycycle"))
                setClkDutyCycle(mval.getSingleFval(loc), loc);
            else {
                attributes.put(mkey, mval);
            }
        }

        // Make sure clock mode frequency, period and duty cycle
        // attributes in the attributes map match the values in the Var.
        //attributes.put("frequency",    new Val(getClkFreq(loc),      loc));
        //attributes.put("period",       new Val(getClkPeriod(loc),    loc));
        //attributes.put("dutycycle",    new Val(getClkDutyCycle(loc), loc));        
    }
    
    /**
     * Get the attributes map value.
     * @param   loc is the source file location
     * @return  the attributes map value
     */
    public Val getAttributes (boolean extended, SrcLoc loc) {
        if (indass)
            return(indirect.getAttributes(extended, loc));
        
        TreeMap<String,Val> attr = null;
        if (extended) {
            attr = new TreeMap<String, Val>();
            attr.put("identifier", new Val(name, loc));
            attr.put("eidentifier", new Val(ename, loc));
            attr.put("mode", new Val("clock", loc));
            attr.put("type", new Val(type, loc));
            attr.put("invertedclock", new Val(clock_sig.getClockMode() == ClkType.NEG, loc));            attr.put("typestring", new Val(wordspec.getTypeString(), loc));
            attr.put("decloc", new Val(decloc.toString(), loc));
            attr.put("inputparam", new Val(isInPar, loc));
            attr.put("outputparam", new Val(isOutPar, loc));
            attr.put("matched", new Val(matched, loc));
            attr.put("assigned", new Val(assigned, loc));
            attr.put("used", new Val(used, loc));
            attr.put("indirect", new Val(indass, loc));
            if (attributes != null)
                attr.putAll(attributes);
        }
        Object[]    oa = new Object[1];
        Type[]      ta = new Type[1];
        Type        type = new Type(Ptype.MAP, 0);
        WordSpec    ws = type.getWordSpec(null, loc);
        oa[0] = extended ? attr : attributes;
        ta[0] = type;
        return(new Val(oa, ta, ws, null, new SubFieldList(), null));
    }

    /**
     * Set this variable as having been assigned.
     */
    public void setAssigned () {
        if (associated != null)
            associated.assigned = true; // avoid recursion as mutual pointers
        else if (indass) {
            indirect.setAssigned();
            return;
        }
        assigned = true;
    }
    
    /**
     * Set this variable as having been used to clock an element, i.e. it is
     * a valid logic clock.
     * @param   loc is the source file location of the parameter declaration
     */
    public void setValid (SrcLoc loc) {
        valid = true;
        attributes.put("valid",  new Val(true, loc));
    }
    
    /**
     * Get the valid flag
     * @return the valid flag
     */
    public boolean getValid() { return(valid); }
     
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

        Clock v = (Clock)arg.getVar();

        //assigned = indirect.assigned; ????
        assigned = true;

        setClkFreq(      v.getClkFreq(loc),      loc);
        //setClkPeriod(    v.getClkPeriod(loc),    loc);
        setClkDutyCycle( v.getClkDutyCycle(loc), loc);
        // Clock signals are not aliased in the TDE list and do not
        // have the Value mode variable for use prior to assignment.
        // The following netlist connections ensure that clock
        // variable TDEVars later assigned to another net are
        // connected (and the same for the associated start signal).
        tdelist.connect(clock_sig, v.clock_sig);
        tdelist.connect(start_sig, v.start_sig);
    }
    
        
    /**
     * Get the read (output) clock variable associated with this variable.
     * @param   loc is the source file location
     * @return  the output (or only) clock Var
     */
    public Clock getOutputClkVar (SrcLoc loc) {
        if (indass)
            return(indirect.getOutputClkVar(loc));
        return(this);
    }

    /**
     * Get the clock signal from a clock mode variable.
     * @return  the clock TDEVar
     */
    public TDEVar getClkSig () {
        if (indass)
            return(indirect.getClkSig());
        else
            return(clock_sig);
    }
    
    /**
     * Get the execution start signal for the clock signal associated
     * with this variable.
     * @param   loc is the source file location
     * @return  the start signal
     */
    public TDEVar getStartSig (SrcLoc loc) {
        if (indass)
            return(indirect.getStartSig(loc));
        return(start_sig);
    }
    
    /**
     * Set the (maximum) frequency of a clock variable.
     * @param   f is the clock frequency in Hz
     * @param   loc is the source file location
     */
    public void setClkFreq (double f, SrcLoc loc) {
        if (indass)
            indirect.setClkFreq(f, loc);
        else {
            if (associated != null) {
                associated.attributes.put("frequency",    new Val(f, loc));
                associated.attributes.put("period",       new Val(1.0 / f, loc));
            }
            attributes.put("frequency",    new Val(f, loc));
            attributes.put("period",       new Val(1.0 / f, loc));
        }
    }
    
    /**
     * Set the period of a clock variable.
     * @param   p is the clock period in second
     * @param   loc is the source file location
     */
    /*public void setClkPeriod (double p, SrcLoc loc) {
        if (indass)
            indirect.setClkPeriod(p, loc);
        else {
            if (associated != null) {
                associated.setClkPeriod(p, loc);
            }
            attributes.put("frequency",    new Val(1.0 / p, loc));
            attributes.put("period",       new Val(p, loc));
        }
    }*/
    
    /*
     * Set the frequency and period of a clock variable.
     * This can be used instead of setClkFreq() and setClkPeriod()
     * so that repeated decimal places generated by rounding can be
     * avoided.
     * @param   f is the clock frequency in Hz
     * @param   p is the clock period in ns
     * @param   loc is the source file location
    public void setClkFreqAndPeriod (double f, double p, SrcLoc loc) {
        if (indass)
            indirect.setClkFreqAndPeriod(f, p, loc);
        else {
            frequency = f;
            period = p;
            if (associated != null) {
                associated.setClkFreqAndPeriod(frequency, period, loc);
                associated.setClkDutyCycle(1.0 - duty_cycle, loc);
            }
            attributes.put("frequency",    new Val(frequency, loc));
            attributes.put("period",       new Val(period, loc));
            attributes.put("dutycycle",    new Val(duty_cycle, loc));
        }
    }
     */
    
    /**
     * Set the duty cycle of a clock variable.
     * @param   dc is the clock duty cycle as a fraction
     * @param   loc is the source file location
     */
    public void setClkDutyCycle (double dc, SrcLoc loc) {
        if (indass)
            indirect.setClkDutyCycle(dc, loc);
        else {
            if (associated != null)
                associated.attributes.put("dutycycle", new Val(dc, loc));
            attributes.put("dutycycle", new Val(dc, loc));
        }
    }
     
    /**
     * Get the (maximum) frequency of a clock variable.
     * @param   loc is the source file location
     * @return  the clock frequency in Hz
     */
    public double getClkFreq (SrcLoc loc) {
        if (indass)
            return(indirect.getClkFreq(loc));
        Val v = attributes.get("frequency");
        if (v == null)
            return(0.0);
        else
            return(attributes.get("frequency").getSingleFval(loc));
    }
     
    /**
     * Get the (minimum) period of a clock variable.
     * @param   loc is the source file location
     * @return  the clock period in second
     */
    public double getClkPeriod (SrcLoc loc) {
        if (indass)
            return(indirect.getClkPeriod(loc));
        Val v = attributes.get("period");
        if (v == null)
            return(0.0);
        else
            return(attributes.get("period").getSingleFval(loc));
    }
    
    /**
     * Get the duty cycle of a clock variable.
     * @param   loc is the source file location
     * @return  the clock duty cycle (ratio high)
     */
    public double getClkDutyCycle (SrcLoc loc) {
        if (indass)
            return(indirect.getClkDutyCycle(loc));
        Val v = attributes.get("duty_cycle");
        if (v == null)
            return(0.0);
        else
            return(v.getSingleFval(loc));
    }
    
    /**
     * Set the negative clock associated with this clock.
     * @param   v is the negative clock variable
     */
    public void setAssociatedClock (Clock v) { associated = v; }

    /**
     * Get the value of a clock variable. The value includes all the
     * information provided by a variable reference (see getRef()) plus the
     * TDEVar. The <B>varnode</B> argument is used to get any flags
     * associated with the variable occurence, but it may be null if the
     * reference is being requested in some other context. There must be no
     * flags for a  clock mode variable. A pointer variable returns a
     * reference to the ultimate variable to which it points.
     * @param   ref is the reference to this variable
     * @param   loc is the source file location
     * @return  the value or values extracted from the variable
     */
    public Val getVal (Ref ref, SrcLoc loc) {
        SubFieldList    sfl = ref.getSubFields();
        if (!sfl.isEmpty())
            throw new ExEx("CLOCK mode variable cannot have subscripts or fields", loc);
        Flag flag = ref.getFlag();           
        if (flag != Flag.NONE)
            throw new ExEx("CLOCK mode variable cannot have ++ or --", loc);

        Val v = new Val(ref, false, loc);
        v.addOVar(this);
        return(v);
    }

    /**
     * Get a reference to a variable. The reference is generally
     * used for assignment to a variable however the value of a variable
     * can also obtained by starting with the reference. The main
     * information contained in the reference is -
     * <UL>
     * <LI> a list of words in the variable selected by subscripts or
     *      field names
     * <LI> a list of primitive types for each word
     * <LI> for target variables a list of bit pairs associated with
     *      the word list
     * <LI> a multi-dimensioned array giving the dimensionality of
     *      the reference
     * <LI> a type to make sure assignment types match
     * <LI> a TDEVar for a target variable
     * </UL>
     * The <B>varnode</B> argument is used to get any flags
     * associated with the variable occurrence (++ or -- flags
     * of the string indirect operator/flag), but it may be null if the
     * reference is being requested in some other context.
     * The <B>subs</B> argument is a list of subscript or field
     * nodes associated with the variable occurrence - it may be null.
     * A pointer variable returns a reference to the ultimate variable
     * to which it points.
     * Reference includes a TDEVar signal for the reference.
     * @param   varnode is the tree node for the variable occurrence or
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
        if (flag != Flag.NONE)
            throw new ExEx("CLOCK mode variable cannot have ++ or --", loc);

        if (indass) {
            // An indirect via a module, procedure or function parameter.
            if (indirect == null)
                return(null);
            if (ind_sfl != null)
                sfl = ind_sfl.merge(this, sfl);
            return(indirect.getRef(varnode, sfl, is_val, loc));
        }
        
        if (sfl.size() > 0)
            throw new ExEx("CLOCK mode variable cannot have subscripts or fields", loc);

        Val         tdev_val = new Val(clock_sig, loc);
        TDEVar      v = tdev_val.getTDEVar();
        Var         vv = v.getVar();
        Ref         ref = new Ref(vv != null ? vv : this, mode, sfl, v, loc);
        if (is_val)
            ref.addOVars(tdev_val);
        else
            ref.addIVar(this);
        return(ref);
    }
    
    public Ref getRef (WordSpec mvalws, SubFieldList sfl, boolean is_val, Flag flag, SrcLoc loc) {
        return getRef(null, sfl, is_val, loc);     
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
        if (indass) {
            rpt("\t\tclock variable '" + ename +
                    "' is linked to clock variable '" + indirect.ename + "'");
            return;
        }

        if (!used) {
            rpt("\t\tclock variable '" + ename + "' not used");
            return;
        }
        
        double  frequency = 0.0;
        if (attributes.get("frequency") == null)
            rpt("\t\tclock mode variable '" + name + "' has no frequency attribute");
        else
            frequency = attributes.get("frequency").getSingleFval(decloc);
        
        if (frequency < 1.0e6)
            rpt("\t\tclock mode variable '" + name + "' has low frequency attribute - " + frequency + "Hz");

        if (start_sig.getDestList().size() != 0) {
            TDE     tde = new TDE(TDEType.START, decloc);
            tde.add2i(startval_tdev);   // is VCC by default
            tde.add2ic(clock_sig);
            tde.add2o(start_sig);
            tdelist.addTDE(tde);
        }
        
        // Inversion of an existing clock. Nothing more to do.
        if (clock_sig.getClockMode() == ClkType.NEG)
            return;
        
        // Clock has been assigned so not an external clock, so no IBUF or IBUFG needed.
        if (assigned)
            return;

        boolean     ibufg = false;  // ibufg attribute
        boolean     diff = false;   // differential attribute
        boolean     hasloc = false; // have location attribute
        int         npins = 0;      // number of input pins (1 or 2)
        String[]    locs = null;    // location attribute

        if ((attributes != null) && attributes.containsKey("ibufg")) {
            Val v = attributes.get("ibufg");
            ibufg = v.getSingleLval(decloc);
        }
        
        if ((attributes != null) && attributes.containsKey("differential")) {
            Val v = attributes.get("differential");
            diff = v.getSingleLval(decloc);
        }
        
        if ((attributes != null) && attributes.containsKey("loc")) {
            Val v = attributes.get("loc");
            npins = v.numWords();
            locs = new String[npins];
            for (int i=0 ; i<npins ; i++)
                locs[i] = v.getArraySval(i, decloc);
            hasloc = true;
        } else {
            if (ibufg || diff)
                throw new ExEx("clock() - clock mode variable '" + name + "' seems to be an input clock but has no loc attribute", decloc);
        }
        
        if (hasloc) {
            if (npins != (diff ? 2 : 1))
                throw new ExEx("clock() - clock mode variable '" + name + "' has wrong number of pins");

            TDE     ibuf = new TDE(TDEType.IBUF, decloc);
            TDEVar  tdi = new TDEVar("PORT_"+locs[0], decloc);
            TDEVar  tdid = null;
            if (diff)
                tdid = new TDEVar("PORT_"+locs[1], decloc);
           
            ibuf.add2p(ids[0]);
            ibuf.add2p(locs[0]);
            if (diff)
                ibuf.add2p(locs[1]);
            else
                ibuf.add2p();
            ibuf.add2p(ibufg);
            ibuf.add2p(diff);    

            ibuf.add2i(tdi);        // input signal from pad to IBUF (external port)
            if (diff)
                ibuf.add2i(tdid);   // 2nd input signal for differential

            ibuf.add2o(clock_sig);  // output signal
            ibuf.add2o(null);       // no inverted output signal at the moment

            tdelist.addTDE(ibuf);
            /*
            msg("\n\tclock variable '" + ename + "' is a clock input from an external port");
            */
        } else if (!sim && !develop) {
            if (!clk_sourced)
                throw new ExEx("clock mode variable '" + name + "' has no source");
            if (ibufg)
                msg("clock mode variable '" + name + "' has ibufg attribute but no pins");
            if (diff)
                msg("clock mode variable '" + name + "' has differential attribute but no pins");
        }
    }
}
