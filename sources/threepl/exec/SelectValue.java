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
import threepl.parser.SrcLoc;

/**
 * This class represents a SELECTVALUE mode variable. The variable has a name String,
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
public final class SelectValue extends ClockedVar implements Constant, TDEConstants {
    private HashMap<TDEVar,eapair>[]    inputs; // assignment signals for this variable
    private HashSet<TDEVar>             nullassigns;     // null queue assignment signals
    private boolean                     direct;          // direct assignment
    private Val                         targ_default;    // target variable default

    /**
     * Construct a selectvalue mode variable.
     * @param   ident is the variable identifier
     * @param   t is the variable type
     * @param   init is an initialisation value, either a single value
     *          or a compound value; for selectvalue mode it is
     *          the default output when no input is selected
     * @param   in_par is true if this is a module, procedure of function
     *          input parameter
     * @param   out_par is true if this is a module or procedure output
     *          parameter
     * @param   loc is the source file location
     */
    @SuppressWarnings({ "unchecked" })
    public SelectValue (
        Ident   ident,
        Type    t,
        Val     init,
        boolean in_par,
        boolean out_par,
        SrcLoc  loc
    ) {
        super(ident, in_par, out_par, t, Ptype.EMPTY, loc);

        // Mode.
        mode = Mode.SELECTVALUE;

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

            if (init != null) {
                if (!type.isPrimitive() && init.isPrimitive()) {
                    if (init.getMode() != Mode.IMMEDIATE)
                        throw new ExEx("compound variable single initialiser is target mode", loc);
                    // Duplicate the initial value to a compound the
                    // same size as the compound type.
                    init = init.expandToCompound(type, loc);
                }
                targ_default = init;
            }
        }

        // add this to the variable queue for processing at the end
        queueVar(this);
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
                    msg(loc.toString());
                    msg("selectvalue variable " + name + " - readonly attribute cannot be false - ignored");
                }
            } else if (mkey.equals("direct"))
                setDirect(mval.getSingleLval(loc), loc);

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
            attr.put("mode", new Val("selectvalue", loc));
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
     * Set the 'direct' flag.
     * @param   d is the value to set the flag
     * @param   loc is the source file location
     */
    public void setDirect (boolean d, SrcLoc loc) {
        direct = d;
        attributes.put("direct", new Val(d, loc));
    }
    
    /**
     * Get the direct assignment flag.
     * @return  the direct assignment flag
     *
    */
    public boolean getDirect () { return(direct); }

    /**
     * Get the value of a variable. The value includes all the
     * information provided by a variable reference (see getRef()).
     * It returns the TDEVar. The
     * <B>varnode</B> argument is used to get any flags associated with
     * the variable occurrence, but it may be null if the reference is
     * being requested in some other context. A pointer variable returns
     * a reference to the ultimate variable to which it points.
     * @param   ref is the reference to this variable
     * @param   loc is the source file location
     * @return  the value or values extracted from the variable
     */
    public Val getVal (Ref ref, SrcLoc loc) {
        Flag flag = ref.getFlag();           
        if (flag != Flag.NONE)
            throw new ExEx("SELECTVALUE mode variable cannot have ++ or --", loc);

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
     * associated with the variable occurence, but it may be null if the
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
            throw new ExEx("SELECTVALUE mode variable cannot have ++ or --", loc);

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
     * Get a Val wrapper containing the TDEVar for the variable occurence.
     * @param   ws is the word specifier for the occurence
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
    public void assignTo (
        Ref     lref,
        Val     rval,
        TDEVar  exec,
        AST     asstype,
        boolean toplevel,
        SrcLoc  loc
    ) {
        if (readonly)
            throw new ExEx("cannot assign to '" + name + "' is read-only!", loc);

        Clock   curclk = getCurrentClockVar();
        Clock   rclk = (rval != null) ? rval.getReadClkVar() : null;

        if ((rclk != null) && (!rclk.isEqualTo(curclk)))
            throw new ExEx("RHS assigned to variable '" + name + "' (" + rclk.getID(IDtype.LITERAL) +
                    ") has different clock to current context clock (" +
                    curclk.getID(IDtype.LITERAL) + ")", loc);

        if ((lref.getPrimType() == Ptype.NULL) || (rval == null)) {
            nullassigns.add(exec);
            assigned = true;
            return;
        }

        WordSpec    lws = lref.getWordSpec();   // LHS wordspec
        int         words = lws.numWords();     // rws is same size (prev check)
        TDEVar      t;
        
        for (int i=0 ; i<words ; i++) {
            if (rval.isTarget()) {
                TDEVar  rtdev = rval.getTDEVar();
                // RHS is a target expression
                if (((lws.getPrimType(i) == Ptype.UINT) ||
                     (lws.getPrimType(i) == Ptype.UFIXED)) &&
                    ((rval.getWordSpec().getPrimType(i) == Ptype.INT) ||
                     (rval.getWordSpec().getPrimType(i) == Ptype.FIXED)))
                    throw new ExEx("unsigned assigned from signed", loc);
                t = rtdev.getWord(lws, i, loc);
                rval.addExec(exec);
            } else {
                // RHS is an immediate expression
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

        if (!used || (wordspec.numBits() == 0)) {
            rpt("\tselectvalue variable '" + ename + "' unused - deleted");
            return;
        }

        if (!assigned) {
            if (((inputs.length == 0) || (inputs[0].size() != 0))) {
                emsg("\tselectvalue variable '" + ename + "' not assigned!");
                return;
            }
        }

        int     words = wordspec.numWords();
        TDEVar  dest = TDEVar.makeTDEVar(ename, wordspec, decloc);
        if (!assigned && (dest.getSrcListSize() != 0))  // Already connected to a port so is just a port symbol
            return;
        
        // Default value for SELECTVALUE output when not in low-Z state.
        String  bi = initialise(targ_default, words, 0, words-1);
        
        assign(inputs, null, dest, bi, words, false);
    }    
}
