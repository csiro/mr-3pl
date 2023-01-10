package threepl.exec;

import static threepl.ThreePL.*;
import static threepl.codegen.TDEVar.*;

import java.util.HashSet;
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
 * This class represents a PRIORITY mode variable. The variable has a name String,
 * a mode and a Type, which may be complex. If the variable
 * is a module, procedure or function parameter it may have an indirect
 * pointer to another Var, depending on its mode and whether input or
 * output. If the variable is a target mode it
 * will also have an expanded name to ensure that this instance is
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
public final class Priority extends ClockedVar implements Constant, TDEConstants {
    private TDEVar          pri_else;   // else case output for priority encoder
    private HashSet<TDEVar> locksets;
    private HashSet<TDEVar> lockresets;
    private boolean         multiple_in;
    private boolean[]       multiples;

    /**
     * Construct a priority mode variable.
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
    public Priority (
        Ident   ident,
        Type    t,
        Val     init,
        boolean in_par,
        boolean out_par,
        SrcLoc  loc
    ) {
        super(ident, in_par, out_par, t, Ptype.EMPTY, loc);

        // Mode.
        mode = Mode.PRIORITY;
        Type    at = type.getArrayType();
        if ((at == null) || (at.getPrimType() != Ptype.LOG))
            throw new ExEx("priority variable '" + name +
                        "' not a 1 dimensional array of log", loc);

        int     words = type.numWords();
        isInPar = in_par;
        isOutPar = out_par;

        // initialisation
        if (words > 0) {
            val = new Object[words];
            val_type = type.getTypeArray();

            // Optional explicit initialisation.
            if (init != null) {
                if (!type.isPrimitive() && init.isPrimitive())
                    throw new ExEx("compound variable single initialiser is target mode", loc);
                int     iwords = init.getWordSpec().numWords();
                for (int i=0 ; i<iwords ; i++) {
                    int     j = wordspec.getWord(i);
                    TDEVar  tdev = init.getWordTDEVar(wordspec, i, loc);
                    Val     tdev_val = new Val(null, Mode.VALUE, tdev, loc);
                    if (!val_type[j].isEqual(Type.LOG, false))
                       throw new ExEx("priority variable initialisation not log type", loc);
                    tdev_val.addOVars(init);
                    val[j] = tdev_val;
                }
            }
        }
        
        multiples = new boolean[words];
        for (int i=0 ; i<words ; i++)
            multiples[i] = false;

        // add this to the variable queue for processing at the end
        queueVar(this);
    }
    
    /**
     * Get this priority variable's else signal.
     * @param   loc is the source file location
     * @return  the priority variable else signal
     */
    public TDEVar getPriElse (SrcLoc loc) {
        if (indass)
            return(indirect.getPriElse(loc));
        if (pri_else == null) {
            WordSpec ws = new WordSpec(Ptype.LOG);
            pri_else = tdelist.namesignal(ename + ctrail + "PE", ws, loc);
        }
        return(pri_else);
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

        for(String mkey: attr.keySet()) {
            Val     mval = attr.get(mkey);
            mkey = mkey.toLowerCase();

            // Check that attribute is known to 3PL.
            // If it checks out OK, continue processing.
            // If it does not check out skip the rest of the loop.
            if (!checkAttribute(mkey, mval, mode, loc))
                throw new ExEx("attribute \"" + mkey + "\" for variable '" + name + "' is unknown", loc);
                        
            if (mkey.equals("domain")) {
                Clock clockvar = getCurrentClockVar();
                if (mval.getPrimType() != Ptype.NULL) {
                    if (mval.getMode() != Mode.CLOCK)
                        throw new ExEx("domain attribute for variable '" + name + "' is not a clock variable", loc);
                    if (mval.getPrimType() != Ptype.NULL)
                        clockvar = (Clock)mval.getVar();
                }
                setOutputClock(clockvar,  Calloc.DOMAIN, false, loc);
                continue;
            }
            
            if (mval.getPrimType() == Ptype.NULL)
                throw new ExEx("attribute \"" + mkey + "\" for variable '" + name + "' is null", loc);
           
            if (mkey.equals("multiplein"))
                multiple_in = mval.getSingleLval(loc);
            
            if (mkey.equals("readonly")) {
                if (mval.getSingleLval(loc))
                    setReadOnly(loc);
                else {
                    msg(loc.toString());
                    msg("priority variable " + name + " - readonly attribute cannot be false - ignored");
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
        attr.put("multiplein", new Val(multiple_in, loc));
        if (extended) {
            attr.put("identifier", new Val(name, loc));
            attr.put("eidentifier", new Val(ename, loc));
            attr.put("mode", new Val("priority", loc));
            attr.put("type", new Val(type, loc));
            attr.put("typestring", new Val(wordspec.getTypeString(), loc));
            attr.put("decloc", new Val(decloc.toString(), loc));
            attr.put("inputparam", new Val(isInPar, loc));
            attr.put("outputparam", new Val(isOutPar, loc));
            attr.put("matched", new Val(matched, loc));
            attr.put("readonly", new Val(readonly, loc));
            attr.put("assigned", new Val(assigned, loc));
            attr.put("used", new Val(used, loc));
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
     * Add a waitprilock lock signal from codegen/TDEList/pwait().
     * It also sets/checks the clock.
     * @param   tdev is a lock signal
     * @param   loc is the source file location
     */
    public void addlockSignal (TDEVar tdev, SrcLoc loc) {
        if (lockresets == null) {
            locksets = new HashSet<TDEVar>();
            lockresets = new HashSet<TDEVar>();
            if (assigned)
                throw new ExEx("waitpri or waitprilock PRIORITY mode variable '" +
                                                    ename + "' already in use", loc);
        }
        locksets.add(tdev);
        setOutputClock(getCurrentClockVar(), Calloc.WAITPRI, true, loc);
    }
    
    /**
     * Add a waitprilock unlock signal from procs/PriUnlockProc.execute().
     * It also sets/checks the clock.
     * @param   tdev is the unlock signal
     * @param   loc is the source file location
     */
    public void addUnlockSignal (TDEVar tdev, SrcLoc loc) {
        if (lockresets == null) {
            locksets = new HashSet<TDEVar>();
            lockresets = new HashSet<TDEVar>();
            if (assigned)
                throw new ExEx("waitpri or waitprilock PRIORITY mode variable '" +
                                                    ename + "' already in use", loc);
        }
        lockresets.add(tdev);
        setOutputClock(getCurrentClockVar(), Calloc.PRIUNLOCK, true, loc);
    }
    
    public boolean isMultiple (int index) { return(multiples[index]); }

    /**
     * Get the value of a variable. The value includes all the
     * information provided by a variable reference (see getRef()) plus
     * For immediate mode type an ArrayList of values. For target mode
     * (value, static or queue), it returns the TDEVar. The
     * <B>varnode</B> argument is used to get any flags associated with
     * the variable occurence, but it may be null if the reference is
     * being requested in some other context.
     * @param   ref is the reference to this variable
     * @param   loc is the source file location
     * @return  the value or values extracted from the variable
     */
    public Val getVal (Ref ref, SrcLoc loc) {
        Flag flag = ref.getFlag();
        if (flag != Flag.NONE)
            throw new ExEx("PRIORITY mode variable cannot have ++ or --", loc);

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
     * A pointer variable returns a reference to the ultimate variable
     * to which it points.
     * A target reference includes a TDEVar signal for the reference.
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
            throw new ExEx("PRIORITY mode variable '" + ename +
                                            "' cannot have ++ or --", loc);
        
        if (indass) {
            // An indirect via a module, procedure or function parameter.
            if (indirect == null)
                return(null);
            if (ind_sfl != null)
                sfl = ind_sfl.merge(this, sfl);
            return(indirect.getRef(varnode, sfl, is_val, loc));
        }

        if ((sfl.size() > 0) && type.isPrimitive())
            throw new ExEx("subscript or field with primitive variable '" +
                                                        name + "'", loc);

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
            queues.and_set(ws.getSQueues(), loc);
        } else
            // has only immediate subscripts or none
            tdev = TDEVar.makeTDEVar(this, ws, loc);
        Val         tdev_val = new Val(tdev, loc);
        if (is_val) {
            for (int i : ws.getWords()) {
                if (val[i] == null)
                    val[i] = Boolean.valueOf(false);            // mark this entry as referenced
                else if (!(val[i] instanceof Boolean)) {    // skip if already marked
                    Val v = (Val)val[i];
                    tdev_val.addOVars(v);
                }
            }
            tdev_val.andSetQueues(queues, loc);
        }
        return(tdev_val);
    }

    /**
     * Assignment to this variable.
     * @param   lref is the left variable reference.
     * @param   rval is the RHS value to be assigned.
     * @param   loc is a source file location for error messages
     */
    public void assignTo (Ref lref, Val rval, SrcLoc loc) {
        if (readonly)
            throw new ExEx("cannot assign to " + name, loc);
        if (!rval.getQueues().isEmpty())
            throw new ExEx("assignment RHS to priority variable " + name +
                            " contains queue reads", loc);
        WordSpec    lws = lref.getWordSpec();// LHS wordspec
        int         words = lws.numWords();  // rws is same size (prev check)
        TDEVar      tdev;
        Val         tdev_val;
        int         j;
        
        if ((tdev=rval.getWordTDEVar(lws, 0, loc)).getId().startsWith("WPIN")) {
            if (assigned)
                throw new ExEx("waitpri or waitprilock priority variable has been assigned elsewhere", loc);
            j = lws.getWord(0);
            tdev_val = new Val(null, Mode.VALUE, tdev, loc);
            if (multiple_in) {
                if ((val[j] != null) && (!(val[j] instanceof Boolean))) {
                    val[j] = new Val(null, Mode.VALUE, tdelist.or(((Val)val[j]).getTDEVar(), tdev, loc), loc);
                    multiples[j] = true;
                } else
                    val[j] = tdev_val;
            } else {
                if ((val[j] != null) && (!(val[j] instanceof Boolean)))
                    throw new ExEx("'waitpri' or 'waitprilock' statement - priority variable '" + ename +
                                    "' priority " + j + " already used", loc);
                val[j] = tdev_val;
            }
            return;
        }
        
        for (int i=0 ; i<words ; i++) {
            tdev = rval.getWordTDEVar(lws, i, loc);
            if ((lockresets != null) && !tdev.getId().startsWith("WPIN"))
                throw new ExEx("assignment to priority variable " + name +
                                " used by waitpri or waitprilock", loc);
            j = lws.getWord(i);
            if (multiple_in && (val[j] != null) && (!(val[j] instanceof Boolean)))
                val[j] = new Val(null, Mode.VALUE, tdelist.or(((Val)val[j]).getTDEVar(), tdev, loc), loc);
            else {
                tdev_val = new Val(null, Mode.VALUE, tdev, loc);
                val[j] = tdev_val;
            }
        }
        assigned = true;
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

        if (!assigned && !used) {
            rpt("\n\tpriority variable '" + ename + "' unused - deleted");
            return;
        }
        
        if (lockresets != null) {
            if (lockresets.isEmpty()) {
                emsg("priority variable '" + ename +
                        "' used in waitprilock but has no priunlock", decloc);
            }
            TDEVar  dff_out = tdelist.signal("PRILOCK", decloc);
            TDE     sor = new TDE(TDEType.OR, decloc);
            TDE     ror = new TDE(TDEType.OR, decloc);
            for (TDEVar tdev : locksets)
                sor.add2i(tdev);
            for (TDEVar tdev : lockresets)
                ror.add2i(tdev);            
            
            tdelist.fdrse(
                            dff_out,
                            GND,
                            pclk.getClkSig(),
                            GND,
                            ror.finish(),
                            sor.finish(),
                            "R",
                            decloc
                        );
            val[0] = new Val(dff_out, decloc);
            
        }

        int     varsize = wordspec.numBits();
        int     i, j, n;

        // determine number of used entries,
        // checking for references with no inputs
        for (i=0,n=0 ; i<varsize ; i++) {
            if (val[i] instanceof Boolean)
                throw new ExEx("priority variable '" + name +
                                "' entry " + i + " has no input", decloc);
            if (val[i] != null)
                n++;
        }
        if (n == 0)
            return;

        TDE         pe = new TDE(TDEType.PRIORITY, decloc);
        Type        wst = new Type(new Type(Ptype.LOG, 1), n);
        WordSpec    ws = wst.getWordSpec(this, decloc);
        TDEVar      in = tdelist.signal("PRI_IN", ws, decloc);
        TDEVar      out = tdelist.signal("PRI_OUT", ws, decloc);
        TDEVar      p = TDEVar.makeTDEVar(this, wordspec, decloc);
        for (i=0,j=0 ; i<varsize ; i++) {
            if (val[i] == null)
                continue;
            Val     v_in = (Val)val[i];
            TDEVar  tdev_in = v_in.getTDEVar();
            TDEVar  tdev_out = p.getWord(i, decloc);
            tdelist.connect(in.getWord(j, decloc), tdev_in);
            tdelist.connect(tdev_out, out.getWord(j, decloc));
            j++;
        }
        pe.add2i(in);
        pe.add2o(out);
        pe.add2o(pri_else);
        tdelist.addTDE(pe);
    }
}
