package threepl.exec;

import static threepl.ThreePL.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import threepl.codegen.TDE;
import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.nodes.Ident;
import threepl.nodes.VarNode;
import threepl.parser.Constant;
import threepl.parser.Functions;
import threepl.parser.SrcLoc;

/**
 * This class represents a STATIC mode variable. The variable has a name String,
 * a mode and a Type, which may be complex. If the variable
 * is a module, procedure or function parameter it may have an indirect
 * pointer to another Var, depending on its mode and whether input or
 * output. If the variable is a target mode it
 * will also have an expanded name to ensure that this instance is
 * unique. The current clock name String and TDEVar is also stored in
 * this class.
 *
 * <p>There is an ArrayList of all assignments
 * to this variable. Each assignment is entered into the list
 * one bit at a time. Entries in this list are triples -
 * <UL>
 * <LI> the bit number, which is an Integer
 * <LI> the source of the bit, which is a TDEVar
 * <LI> the select signal causing the assignment, which is a TDEVar
 *</UL>
 *
 * The following special cases apply -
 * <UL>
 * <LI> If isInPar is true then this variable is a module, procedure
 *      or function input parameter.
 * <LI> If isOutPar is true then this variable is a module or procedure
 *      output parameter.
 * </UL>
 */
public final class Static extends ClockedVar implements Constant, TDEConstants {
    private HashMap<TDEVar,eapair>[]    inputs;             // assignment signals for this variable
    private Val                         init_val = null;    // variable initialisation
    private HashSet<TDEVar>[]           resets;             // reset signals
    private TDEVar[]                    writes;             // requested write signals for words
    private TDEVar                      write;              // requested write signal for any word
    private boolean                     continuous = false; // continuous assignment
    private boolean                     iob = false;        // IOB attribute
    private boolean                     dsp = false;        // DSP attribute
    private String[]                    ids;                // element block identifiers

    /**
     * Construct a STATIC mode variable.
     * @param   ident is the variable identifier
     * @param   t is the variable type
     * @param   init is an initialisation value, either a single value
     *          or a compound value
     * @param   in_par is true if this is a module, procedure of function
     *          input parameter
     * @param   out_par is true if this is a module or procedure output
     *          parameter
     * @param   loc is the source file location
     */
    @SuppressWarnings("unchecked")
    public Static (
        Ident   ident,
        Type    t,
        Val     init,
        boolean in_par,
        boolean out_par,
        SrcLoc  loc
    ) {
        super(ident, in_par, out_par, t, Ptype.EMPTY, loc);

        // Mode.
        mode = Mode.STATIC;

        int     words = type.numWords();
        isInPar = in_par;
        isOutPar = out_par;
        if (words != 0) {
            inputs = new HashMap[words];
            for (int i=0 ; i<words ; i++)
                inputs[i] = new HashMap<TDEVar,eapair>();
        }

        // initialisation
        if (words > 0) {
            val = new Object[words];
            val_type = type.getTypeArray();
            if (init != null)
                setInitVar(init, loc);
        }
        
        resets = new HashSet[words];
        if (getFamily().defaultContinuous())
            setContinuous(true, loc);
        
        // Add the block id array to the attributes map.
        int         n = wordspec.numBits();
        ids = new String[n];
        for (int i=0 ; i<n ; i++)
            ids[i] = "b" + icount++;
        attributes.put("ids", new Val(ids, "str", loc));

        // add this to the variable queue for processing at the end
        queueVar(this);
    }
    
    /**
     * Initialise the static mode variable.
     * @param v is the initial value
     * @param loc is the source file location
     */
    private void setInitVar (Val v, SrcLoc loc) {
        if ((v.getMode() != Mode.IMMEDIATE) && ((v.getMode() != Mode.VALUE) || (v.getVals() == null)))
            throw new ExEx("static variable '" + name + "' initialiser is target mode", loc);
        if (!type.isPrimitive() && v.isPrimitive()) {
            // Duplicate the initial value to a compound the
            // same size as the compound type.
            v = v.expandToCompound(type, loc);
        }
        init_val = v;
        
    }
    
    /**
     * Initialise a state variable.
     * This is a static log variable and must not have been initialised
     * when declared. This method is only called by
     * StateMachineProc.execute() and PetriNetNode.getPlaces() which
     * have already checked the mode and type. If the variable has
     * already been initialised an exception is thrown.
     * @param   v is the logical value to which the state variable
     *          is to be initialised
     * @param   loc is the source file location
     */
    public void setStateVarInit (boolean v, SrcLoc loc) {
        if (init_val != null)
            throw new ExEx("pre-declared state variable initialised", decloc);
        init_val = new Val(v, loc);
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
                throw new ExEx("attribute \"" + mkey + "\" for variable '" + name + "' is unknown", loc);
                                    
            if (mkey.equals("domain")) {
                Clock clockvar = getCurrentClockVar();
                if (mval.getPrimType() != Ptype.NULL) {
                    if (mval.getMode() != Mode.CLOCK)
                        throw new ExEx("domain attribute is not a clock variable", loc);
                    if (mval.getPrimType() != Ptype.NULL)
                        clockvar = (Clock)mval.getVar();
                }
                setOutputClock(clockvar,  Calloc.DOMAIN, false, loc);
                continue;
            }
            
            if (mval.getPrimType() == Ptype.NULL)
                throw new ExEx("attribute \"" + mkey + "\" for variable '" + name + "' is null", loc);
            
            if (mkey.equals("readonly")) {
                if (mval.getSingleLval(loc))
                    setReadOnly(loc);
                else {
                    rpt("\t" + loc.toString());
                    rpt("\tstatic variable " + name + " - readonly attribute cannot be false - ignored\n");
                }
            } else if (mkey.equals("continuous"))
                setContinuous(mval.getSingleLval(loc), loc);
            else if (mkey.equals("alu")) {
                if (assigned) {
                    rpt("\t" + loc.toString());
                    rpt("\tstatic variable " + name + " - alu attribute cannot be set when variable has already been assigned - ignored\n");
                } else if (type.getPrimType() == Ptype.NONE) {
                    rpt("\t" + loc.toString());
                    rpt("\tstatic variable " + name + " - alu attribute cannot be set when variable not primitive type - ignored\n");
                } else
                    attributes.put(mkey, mval);
            } else if (mkey.equals("iob")) {
                    attributes.put(mkey, mval);
                    iob = mval.getSingleLval(loc);
            } else if (mkey.equals("dsp")) {
                /*if (init_val != null) {
                    rpt("\t" + loc.toString());
                    rpt("\tstatic variable " + name + " - dsp attribute cannot be set when variable is initialised - ignored\n");
                } else*/ {
                    attributes.put(mkey, mval);
                    dsp = mval.getSingleLval(loc);
                }
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
            attr.put("mode", new Val("static", loc));
            attr.put("type", new Val(type, loc));
            attr.put("typestring", new Val(wordspec.getTypeString(), loc));
            attr.put("decloc", new Val(decloc.toString(), loc));
            attr.put("inputparam", new Val(isInPar, loc));
            attr.put("outputparam", new Val(isOutPar, loc));
            attr.put("matched", new Val(matched, loc));
            attr.put("readonly", new Val(readonly, loc));
            attr.put("assigned", new Val(assigned, loc));
            attr.put("used", new Val(used, loc));
            attr.put("continuous", new Val(continuous, loc));
            if (init_val != null)
                attr.put("init", init_val);
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
        continuous = d;
        attributes.put("continuous", new Val(d, loc));
    }
    
    /**
     * Get the continuous assignment flag.
     * @return  the continuous assignment flag
     *
    */
    public boolean getContinuous () { return(continuous); }
   
    /**
     * Add a reset signal for a word or words.
     * @param   r is the reset signal
     * @param   ws is the wordspec specifying the words to be reset
     * @param   loc is the source file location
     */
    public void addReset (TDEVar r, WordSpec ws, SrcLoc loc) {
        if (indass) {
            indirect.addReset(r, ws, loc);
            return;
        }
        setOutputClock(getCurrentClockVar(), Calloc.RESET, false, loc);
        for (int i=0 ; i<ws.numWords() ; i++) {
            int j = ws.getWord(i);
            if (resets[j] == null)
                resets[j] = new HashSet<TDEVar>();
            resets[j].add(r);
        }
    }
     
    /**
     * Add a reset signal for a word.
     * @param   r is the reset signal
     * @param   i is the index of the word to be reset
     * @param   loc is the source file location
     */
    public void addReset (TDEVar r, int i, SrcLoc loc) {
        if (indass) {
            indirect.addReset(r, i, loc);
            return;
        }
        setOutputClock(getCurrentClockVar(), Calloc.RESET, false, loc);
        if (resets[i] == null)
            resets[i] = new HashSet<TDEVar>();
        resets[i].add(r);
    }

    /**
     * Get the write signal for a word.
     * The signal is high on the clock cycle of a write to the word.
     * @param   i is the index of the word
     * @return  the write signal
     */
    public TDEVar getWrite (int i) {
        if (writes == null)
            writes = new TDEVar[wordspec.numWords()];
        if (writes[i] == null)
            writes[i] = tdelist.signal("W", WordSpec.TLOG, decloc);
        return(writes[i]);
    }

    /**
     * Get the write signal for any word.
     * The signal is high on the clock cycle of a write to the word.
     * Only used by functions iswritten() and waswritten().
     * @return  the write signal
     */
    public TDEVar getWrites () {
        TDE or = new TDE(TDEType.OR, decloc);
        if (write == null) {
            write = tdelist.signal("W", WordSpec.TLOG, decloc);
            int words = wordspec.numWords();
            for (int i=0 ; i<words ; i++)
                or.add2i(getWrite(i));
            or.add2o(write);
            tdelist.addTDE(or);
        }
        return(write);
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
                        
        Static sv = (Static)arg.getVar();

        // make parameter clocks same as those of the argument variable.
        pclk = sv.pclk;
        pclk_event = sv.pclk_event;
        pclk_trace = sv.pclk_trace;

        ind_sfl = arg.getSubFields();
    }
    
    /**
     * Get the value of a variable. The value includes all the
     * information provided by a variable reference (see getRef()) plus
     * For immediate mode type an ArrayList of values. For target mode
     * (value, static or queue), it returns the TDEVar. The
     * <B>varnode</B> argument is used to get any flags associated with
     * the variable occurrence, but it may be null if the reference is
     * being requested in some other context.
     * @param   ref is the reference to this variable
     * @param   loc is the source file location
     * @return  the value or values extracted from the variable
     */
    public Val getVal (Ref ref, SrcLoc loc) {
        Flag flag = ref.getFlag();           
        if (flag != Flag.NONE)
            throw new ExEx("STATIC mode variable cannot have ++ or --", loc);

        Val v = new Val(ref, false, loc);
        v.addOVar(this);
        v.addOVars(ref);
        v.addOVars(ref.getTDEVar().getWordSpec()); // add any subscript Vars
        v.andSetQueues(ref, loc);
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
     * associated with the variable occurence (++ or -- flags
     * of the string indirect operator/flag), but it may be null if the
     * reference is being requested in some other context.
     * The <B>subs</B> argument is a list of subscript or field
     * nodes associated with the variable occurence - it may be null.
     * Reference includes a TDEVar signal for the reference.
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
        if (flag != Flag.NONE)
            throw new ExEx("STATIC mode variable in expression cannot have ++ or --", loc);

        if (indass) {
            // An indirect via a module, procedure or function parameter.
            if (indirect == null)
                return(null);
            if (ind_sfl != null)
                sfl = ind_sfl.merge(this, sfl);
            return(indirect.getRef(varnode, sfl, is_val, loc));
        }

        if ((sfl.size() > 0) && type.isPrimitive())
            throw new ExEx("subscript or field with primitive variable '" + name + "'", loc);

        WordSpec    ws = type.getWordSpec(this, sfl, loc);
        Val         tdev_val = get_target_sig(ws, is_val, loc);// Val is wrapper!
        TDEVar      v = tdev_val.getTDEVar();
        Var         vv = v.getVar();
        Ref         ref = new Ref(vv != null ? vv : this, mode, sfl, v, loc);
        if (is_val)
            ref.addOVars(tdev_val);
        else
            ref.addIVar(this);
        ref.andSetQueues(tdev_val, loc);
        return(ref);
    }
    
    /**
     * Get a reference to one word of a static mode variable.
     * @param   index is the component index
     * @param   loc is the source file location
     * @return  the reference to the variable component
     */
    public Ref getWordRef (int index, SrcLoc loc) {
        WordSpec    ws = new WordSpec();
        Type        t = wordspec.getType(index);
        ws.append(index, wordspec.getLower(index), wordspec.getUpper(index), t);
        ws.setCheckType(t);
        Val             tdev_val = get_target_sig(ws, false, loc);// Val is wrapper!
        SubFieldList    sfl = type.indexToSubFieldList(index, loc);
        Ref             ref = new Ref(this, mode, sfl, tdev_val.getTDEVar(), loc);
        ref.addIVar(this);
        return(ref);
    }

    /**
     * Get a Val wrapper containing the TDEVar for the variable occurrence.
     * @param   ws is the word specifier for the occurrence
     * @param   is_val is true if we want a value rather than a reference
     * @param   loc is the source file location
     * @return  a Val wrapper containing the TDEVar, queue references and
     *          maps of any variables referenced
     */
    private Val get_target_sig (
        WordSpec    ws,
        boolean     is_val,
        SrcLoc      loc
    ) {
        TDEVar      tdev = null;
        int         ntsubs = ws.getNumTargSubs();
        QueueRefs    queues = new QueueRefs();
        
        if ((ntsubs > 0) && is_val) {
            // has target variable subscripts
            tdev = ws.selector(ename, null, loc);
            //used = true;
            queues.and_set(ws.getSQueues(), loc);
        } else
            // has only immediate subscripts or none
            tdev = TDEVar.makeTDEVar(this, ws, loc);
        Val tdev_val = new Val(tdev, loc);
        tdev_val.andSetQueues(queues, loc);
        return(tdev_val);
    }

    /**
     * Assignment to this variable.
     * @param   lref is the left variable reference.
     * @param   rval is the RHS expression value.
     * @param   exec is the execution signal
     * @param   asstype is the assignment type
     * @param   toplevel is true if this is the top level in a module
     * @param   loc is a source file location for error messages
     */
    @SuppressWarnings("incomplete-switch")
    public void assignTo (
        Ref     lref,
        Val     rval,
        TDEVar  exec,
        AST     asstype,
        boolean toplevel,
        SrcLoc  loc
    ) {
        if (readonly)
            throw new ExEx("cannot assign to '" + name + "' - read-only!", loc);

        Clock   curclk = getCurrentClockVar();
        Clock   rclk = (rval != null) ? rval.getReadClkVar() : null;

        if ((rclk != null) && (!rclk.isEqualTo(curclk)))
            throw new ExEx("RHS assigned to variable '" + name + "' has a different clock (" + rclk.getID(IDtype.CHAIN) +
                    ") to current context clock (" + curclk.getID(IDtype.CHAIN) + ")", loc);

        WordSpec    lws = lref.getWordSpec();   // LHS wordspec
        int         words = lws.numWords();     // rws is same size (prev check)
        TDEVar      t;
        boolean     dsp = attributes.containsKey("dsp") && attributes.get("dsp").getSingleLval(decloc);
        TDEVar      rtdev = rval.isTarget() ? rval.getTDEVar() : null;
        
        for (int i=0 ; i<words ; i++) {
            int     j = lws.getWord(i);
            if (rtdev != null) {
                if (rtdev.isConst()) {
                    // RHS is a target constant; see if it is the same
                    // value as the initial value of the variable.
                    // If so use reset rather than assignment.
                    if (init_val != null &&
                        rtdev.getConst().equals(init_val.getVal(j)) &&
                        asstype == AST.STATICASS && !dsp) {
                        addReset(exec, j, loc);
                        continue;
                    }
                    if ((init_val == null) && !dsp) {
                        switch (rtdev.getType()) {
                        case BITS:
                        case UINT:
                        case INT:
                        case FIXED:
                        case UFIXED:
                            long v = ((Long)rtdev.getConst()).longValue() << lws.getFixOffset(i);
                            if (v == 0 && asstype == AST.STATICASS) {
                                addReset(exec, j, loc);
                                continue;
                            }
                            break;
                        case BOOL:
                            boolean b = ((Boolean)rtdev.getConst()).booleanValue();
                            if (b == false && asstype == AST.STATICASS) {
                                addReset(exec, j, loc);
                                continue;
                            }
                        }
                    }
                }
                // RHS is a target expression
                if (((lws.getPrimType(i) == Ptype.UINT) ||
                     (lws.getPrimType(i) == Ptype.UFIXED)) &&
                    ((rval.getWordSpec().getPrimType(i) == Ptype.INT) ||
                     (rval.getWordSpec().getPrimType(i) == Ptype.FIXED)))
                    throw new ExEx("unsigned assigned from signed", loc);
                t = rtdev.getWord(lws, i, loc);
            } else if (rval.getVal(i) instanceof TDEVar) {
                // RHS contains a target expression in a compound value
                t = ((TDEVar)rval.getVal(i));
            } else {
                /* NOTE: should check if this code is ever executed.
                   TargAssNode.java execute() converts an immediate to a target
                   mode before calling assignTo().
                 */
                // RHS is an immediate expression
                if (init_val != null &&
                    rval.getVal(i).equals(init_val.getVal(j)) &&
                    asstype == AST.STATICASS) {
                    // const RHS value == initialisation value - use reset
                    // instead of assignment    
                    addReset(exec, j, loc);
                    continue;
                }
                if ((init_val == null) && !dsp) {
                    switch (rval.getValPType(i)) {
                    case BITS:
                    case UINT:
                    case INT:
                    case FIXED:
                    case UFIXED:
                        long v = ((Long)rval.getVal(i)).longValue() << lws.getFixOffset(i);
                        if (v == 0 && asstype == AST.STATICASS) {
                            addReset(exec, j, loc);
                            continue;
                        }
                        break;
                    case LOG:
                        boolean b = ((Boolean)rval.getVal(i)).booleanValue();
                        if (b == false && asstype == AST.STATICASS) {
                            addReset(exec, j, loc);
                            continue;
                        }
                        break;
                    case ENUM:
                        long e = ((Long)rval.getVal(i)).longValue();
                        long ei = val_type[j].firstEnumOrd().longValue();
                        if (e == ei && asstype == AST.STATICASS) {
                            addReset(exec, j, loc);
                            continue;
                        }
                    }
                }
                t = new TDEVar(rval.getVal(i), lws, i, loc);
                if (t.numBits() > lws.getWidth(i))
                    throw new ExEx(name + " assigned larger immediate value", loc);
                if (((lws.getPrimType(i) == Ptype.UINT) || (lws.getPrimType(i) == Ptype.UFIXED)) &&
                    (((Long)t.getConst()).longValue() < 0))
                    throw new ExEx("unsigned assigned -ve value", loc);
            }
            add_input(inputs, lws.getWord(i), t, exec, asstype, toplevel);
        }
        assigned = true;
        rval.setUsed();
        rval.addExec(exec);
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

        if (!assigned && (writes != null))
            throw new ExEx("static variable '" + name + "' - passed to waswritten() but is never used");

        if (!assigned && !used && (!develop || (type.getPrimType() == Ptype.NULL))) {
            rpt("\tstatic variable '" + ename + "' unused - deleted");
            return;
        }

        String  varname = ename;
        String  varDataName = varname + ctrail + "D";
        String  varCEName = varname + ctrail + "CE";
        String  varResetName = varname + ctrail + "RES";
        int     words = wordspec.numWords();
        int     varsize = 0;
        if (type.getPrimType() != Ptype.NULL)
            varsize = wordspec.numBits();
        
        // Signal declarations and some TDE initialisation.
        TDEVar      reg    = null;
        TDEVar      data   = null;
        TDE         tde    = null;
        WordSpec    rws = wordspec.getLogWordSpec();

        if (wordspec.numBits() == 1)
            rws.setComplete();

        TDEVar      ce     = null;
        TDEVar      reset  = TDEVar.makeTDEVar(varResetName, rws, decloc);

        if (assigned)
            ce = TDEVar.makeTDEVar(varCEName, rws, decloc);
        else if ((type != null) && (type.getPrimType() != Ptype.NULL)) {
            // Unassigned static variable - implemented as a constant.
            // Declare the signal array as long as it has a type and
            // that type is not NULL.
            tdelist.namesignal(ename, wordspec, decloc);
        }

        if (type.getPrimType() != Ptype.NULL)
            reg  = TDEVar.makeTDEVar(this, type.getWordSpec(this, decloc), decloc);
        if (assigned && (wordspec.numBits() != 0))
            data = tdelist.namesignal(varDataName, wordspec, decloc);

        // If the resets array is not empty, process each member set in turn
        // ORing all reset signals from each set together. For each, check
        // that other words do not have the same set - if so, share the logic.
        // Null members of the resets array result in low reset signals for
        // those words.
        // Add the array of reset signals to the static.
        for (int i=0 ; i<words ; i++) {
            if (resets[i] != null) {
                TDE         or = new TDE(TDEType.OR, decloc);
                for (TDEVar tdev : resets[i])
                    or.add2i(tdev);
                TDEVar      orout = or.finish();
                if (words == 1)
                    reset = orout;
                else
                    tdelist.connect(reset.getWord(i, decloc), orout);
            } else {
                if (words == 1)
                    reset = TDEVar.GND;
                else {
                    TDEVar  rb = reset.getWord(i, decloc);
                    tdelist.connect(rb, TDEVar.GND);
                }
            }
        }
        
        
        TDEVar[]    writenables = new TDEVar[words];
        if (assigned) {
            assign(inputs, writenables, data, null, words, false);
        
            // Connect up any iswritten() or waswritten() signals to the associated
            // write enable signals.
            if (words == 1) {
                ce = writenables[0];
                if ((writes != null) && (writes[0] != null))
                    tdelist.connect(writes[0], writenables[0]);
            } else {
                for (int i=0 ; i<words ; i++) {
                    tdelist.connect(ce.getWord(i, decloc), writenables[i]);
    
                if ((writes != null) && (writes[i] != null))
                    tdelist.connect(writes[i], writenables[i]);
                }
            }
        }
        
        if (dsp && assigned /*&& (init_val == null)*/) {
            // If the DSP flag is true and the static variable is assigned and is not initialised,
            // create separate TDEs for each word.
            int k = 0;
            for (int i=0 ; i<words ; i++) {
                int         n = wordspec.getWidth(i);
                String[]    sids = new String[n];
                
                for (int j=0 ; j<n ; j++,k++)
                    sids[j] = ids[k];
                tde = new TDE(TDEType.REG, decloc);
                tde.add2ic(pclk.getClkSig()); // static clock
                tde.add2p(sids);
                if (init_val == null)
                    tde.add2p("0");
                else {
                    String  bi = initialise(init_val, words, i, i);
                    tde.add2p(Functions.binToHex(bi));
                }
                tde.add2p(iob);     // IOB flag
                tde.add2p(true);    // DSP flag true
                tde.add2i(data.getWord(i, decloc));
                tde.add2i(ce.getWord(i, decloc));
                tde.add2i(reset.getWord(i, decloc));
                tde.add2o(reg.getWord(i, decloc));
                tdelist.addTDE(tde);
            }
        } else {
            // Create one TDE for the whole static variable.
            tde = new TDE(TDEType.REG, decloc);
            tde.add2p(ids);     // block IDs
            if (assigned)
                tde.add2ic(pclk.getClkSig()); // static clock
            else
                tde.add2i((TDEVar)null); // no inputs - ncode will create a constant
            if (init_val == null)
                tde.add2p("0");
            else {
                String  bi = initialise(init_val, words, 0, words-1);
                tde.add2p(Functions.binToHex(bi));
            }
            tde.add2p(iob);     // IOB flag
            tde.add2p(false);   // DSP flag false
            tde.add2i(data);
            tde.add2i(ce);
            tde.add2i(reset);
            tde.add2o(reg);
            tdelist.addTDE(tde);
        }       
    }
}
