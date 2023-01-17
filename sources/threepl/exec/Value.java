package threepl.exec;

import static threepl.ThreePL.*;

import java.util.ArrayList;
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
 * This class represents a Value mode variable. The variable has a name String,
 * a mode and a Type, which may be complex. If the variable
 * is a module, procedure or function parameter it may have an indirect
 * pointer to another Var, depending on its mode and whether input or
 * output. It
 * also has an expanded name to ensure that this instance is
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
public final class Value extends Var implements Constant, TDEConstants {
    private Val[]           pre_val;    // value used prior to assignment
    private boolean[]       val_used;   // value has been used
    private int             valnameext; // unique identifier extension int

    /**
     * Construct a VALUE mode variable.
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
    @SuppressWarnings("incomplete-switch")
    public Value (
        Ident   ident,
        Type    t,
        Val     init,
        boolean in_par,
        boolean out_par,
        SrcLoc  loc
    ) {
        super(ident, in_par, out_par, t, Ptype.EMPTY, loc);

        // Mode.
        mode = Mode.VALUE;

        int     words = type.numWords();
        isInPar = in_par;
        isOutPar = out_par;

        // initialisation
        if (words > 0) {
            val = new Object[words];
            val_type = type.getTypeArray();

            // Default initialisation.
            valnameext = 1;
            pre_val = new Val[words];
            val_used = new boolean[words];

            // Optional explicit initialisation.
            if (init != null) {
                if (type.isPrimitive() && !init.isPrimitive())
                    throw new ExEx("variable is primitive but initialiser is compound", loc);
                if (!type.isPrimitive() && init.isPrimitive()) {
                    if (init.getMode() != Mode.IMMEDIATE)
                        throw new ExEx("array single initialiser is target mode", loc);
                    if (type.getFieldIndex() != null)
                        throw new ExEx("struct variable cannot have a single initialiser", loc);
                    t = type;
                    while (t.getArrayType() != null)
                        t = t.getArrayType();
                    if (!Type.checkPrimType(t.getPrimType(), init.getPrimType()))
                        throw new ExEx("array single initialiser is not matching type (" +
                                        t.getPrimType().name() + "/" + init.getPrimType().name() + ")", loc);
                    // Duplicate the initial value to a compound the
                    // same size as the compound type.
                    init = init.expandToCompound(type, loc);
                }
                int     iwords = init.getWordSpec().numWords();
                wordspec.checkMatch(init, false, "value initialisation", loc);
                Object  o;
                TDEVar  tdev;
                for (int i=0 ; i<iwords ; i++) {
                    if (init.getVals() == null)
                        tdev = init.getWordTDEVar(wordspec, i, loc);
                    else if ((o = init.getVal(i)) != null) {
                        if (o instanceof TDEVar)
                            tdev = (TDEVar)init.getVal(i);
                        else
                            tdev = new TDEVar(o, wordspec, i, loc);
                    } else
                        continue;   // no list entry; skip initialisation
                    
                    QueueRefs    queues = init.getQueues();
                    Val         tdev_val = new Val(null, Mode.VALUE, tdev, loc);
                    tdev_val.addExecSets(init);
                    tdev_val.addOVars(init);
                    tdev_val.andSetQueues(queues, loc);
                    Ptype   rhs_type = tdev.getWordSpec().getPrimType(0);
                    switch (val_type[i].getPrimType()) {
                    case BITS:
                    case UINT:
                    case INT:
                    case ENUM:
                        if (rhs_type == Ptype.LOG)
                            throw new ExEx("cannot initialise numeric to type log", loc);
                        break;
                    case LOG:
                        if (rhs_type != Ptype.LOG)
                            throw new ExEx("cannot initialise log to numeric type", loc);
                    }
                    val[i] = tdev_val;
                }
            }
        }

        // add this to the variable queue for processing at the end
        queueVar(this);
    }

    /**
     * Generate a unique identifier from the variable extended
     * identifier by appending an underscore and an integer.
     * @param   loc is the source file location
     * @return  the identifier string
     */
    public String uniqueValueId (SrcLoc loc) {
        String uid = ename + "_" + valnameext++;
        tdelist.namesignal(uid, wordspec, loc);
        return(uid);
    }
    
    /**
     * Convert the variable to type "empty".
     */
    public void empty () {
        type = new Type(Ptype.EMPTY, 0);
        wordspec = type.getWordSpec(this, decloc);
        val = null;
        val_type = null;
        pre_val = null;
        val_used = null;
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

        // check attributes for those applicable to 3PL itself.
        Set<String>         ks = attr.keySet();
        for (String mkey: ks) {
            Val     mval = attr.get(mkey);
            mkey = mkey.toLowerCase();

            // Check that attribute is known to 3PL.
            // If it checks out OK, continue processing.
            // If it does not check out skip the rest of the loop.
            if (!checkAttribute(mkey, mval, mode, loc)) {
                rpt(loc.toString());
                rpt("static variable " + name + " - unknown attribute " + mkey + " - ignored");
                continue;
            }
                       
            if (mkey.equals("domain")) {
                Clock clockvar = getCurrentClockVar();
                if (mval.getPrimType() != Ptype.NULL) {
                    if (mval.getMode() != Mode.CLOCK)
                        throw new ExEx("domain attribute is not a clock variable", loc);
                    if (mval.getPrimType() != Ptype.NULL)
                        clockvar = (Clock)mval.getVar();
                }
                setClock(clockvar,  Calloc.DOMAIN, loc);
                continue;
            }
            
            if (mval.getPrimType() == Ptype.NULL)
                throw new ExEx("3PL attribute \"" + mkey + "\" for variable '" + name + "' is null", loc);
            
            if (mkey.equals("readonly")) {
                if (mval.getSingleLval(loc))
                    setReadOnly(loc);
                else {
                    msg(loc.toString());
                    msg("value variable " + name + " - readonly attribute cannot be false - ignored");
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
        
        TreeMap<String,Val> attr = new TreeMap<String,Val>();
        if (attributes != null)
            attr.putAll(attributes);
        if (extended) {
            attr.put("identifier", new Val(name, loc));
            attr.put("eidentifier", new Val(ename, loc));
            attr.put("mode", new Val("value", loc));
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
     * Set the used flags for this VALUE mode variable.
     * @param   ws is the WordSpec indicating which words are used
     */
    public void setUsed (WordSpec ws) {
        if (indass) {
            ((Value)indirect).setUsed(ws);
            return;
        }
        for (int i : ws.getWords())
            val_used[i] = true;
    }
    
    /**
     * Resolve the clocks for any variables which are included in val[]
     * or pre_val[].
     * @param   clk is the clock domain variable
     * @param   e is the event setting the clock
     * @param   loc is the source file location
     */
    public void setClock (Clock clk, Calloc e, SrcLoc loc) {
        if (clk == null)
            return;

        if (indass) {
            ((Value)indirect).setClock(clk, e, loc);
            return;
        }
        
        // Resolve the clocks of any values.
        String      uid = null;
        for (int i : wordspec.getWords()) {
            if (val[i] != null) {
                // already have val[i] - must be null domain to allow change
                Val v = (Val)val[i];
                v.resolveClocks(clk, Calloc.DOMAIN, loc);
                v.addOVar(clk);
            } else if (pre_val[i] != null) {
                // already have pre_val[i] - must be null domain to allow change
                Val v = pre_val[i];
                v.resolveClocks(clk, Calloc.DOMAIN, loc);
                v.addOVar(clk);
            } else {
                if (uid == null)
                    uid = uniqueValueId(loc);
                WordSpec    ws = wordspec.getWordWordSpec(i);
                TDEVar  t = TDEVar.makeTDEVar(uid, ws, loc);
                pre_val[i] = new Val(null, Mode.VALUE, t, loc);
                pre_val[i].addOVar(clk);
            }
        }
    }
        
    /**
     * Get the read (output) clock variable associated with this variable.
     * The WordSpec argument indicates which variable components are to be
     * checked.
     * @param   loc is the source file location
     * @return  the output clock Var
     */
    public Clock getOutputClkVar (SrcLoc loc) {
        if (indass)
            return(indirect.getOutputClkVar(loc));

        Clock       vclk;
        Clock       clk = null;
        for (int i : wordspec.getWords()) {
            Val v;
            if (val[i] != null)
                v = (Val)val[i];
            else if (pre_val[i] != null)
                v = pre_val[i];
            else
                continue;
            
            vclk = v.getReadClkVar();
            if (clk == null)
                clk = vclk;
            else if ((vclk != null) && (!clk.isEqualTo(vclk)))
                throw new ExEx(
                    "value '" + ename + "' has multiple clock domains (" +
                    clk.getID(IDtype.SLITERAL) + ", " + vclk.getID(IDtype.SLITERAL) + ")",
                    loc
                );                    
        }

        return(clk);
    }

    /**
     * Get the value of the variable. The value includes all the
     * information provided by a variable reference (see getRef()).
     * It returns the TDEVar. The <B>varnode</B> argument is used to get any flags
     * associated with the variable occurrence, but it may be null. For value mode
     * if <B>varnode</B> has a ++ or -- flag that is an error.
     * A pointer variable returns a reference to the ultimate variable to which it points.
     * @param   varnode is the code tree node, may be null (used to
     *          detect variable node flags)
     * @param   ref is the reference to this variable
     * @param   loc is the source file location
     * @return  the value or values extracted from the variable
     */
    public Val getVal (VarNode varnode, Ref ref, SrcLoc loc) {
        Flag flag = (varnode == null) ? Flag.NONE : varnode.getFlag();           
        if (flag != Flag.NONE)
            throw new ExEx("VALUE mode variable cannot have ++ or --", loc);

        Val v = new Val(ref, false, loc);
        v.var = this;
        v.addExecSets(ref);
        v.addOVar(this);
        v.addOVars(ref);
        v.addOVars(ref.getTDEVar().getWordSpec()); // add any subscript Vars
        v.andSetQueues(ref, loc);
        return(v);
    }
    
    /**
     * Get the value of the variable. The value includes all the
     * information provided by a variable reference (see getRef()).
     * It returns the TDEVar.
     * A pointer variable returns a reference to the ultimate variable to which it points.
     * 
     * @param   ref is the reference to this variable
     * @param   loc is the source file location
     * @return  the value or values extracted from the variable
     */
    public Val getVal (Ref ref, SrcLoc loc) {
        return(getVal(null, ref, loc));
    }

    /**
     * Get a reference to the variable. The reference is generally
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
     * Value mode variable references must not have a flag
     * The <B>subs</B> argument is a list of subscript or field
     * nodes associated with the variable occurrence - it may be null.
     * A pointer variable returns a reference to the ultimate variable
     * to which it points.
     * The reference includes a TDEVar signal for the reference.
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
            throw new ExEx("VALUE mode variable cannot have ++ or --", loc);

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
        ref.andSetQueues(tdev_val, loc);
        if (is_val)
            ref.addOVars(tdev_val);
        else
            ref.addIVar(this);
        ref.addExecSets(tdev_val);
        return(ref);
    }
    
    /**
     * Get a reference to one word of a value mode variable.
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
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Val get_target_sig (
        WordSpec    ws,
        boolean     is_val,
        SrcLoc      loc
    ) {
        TDEVar              tdev = null;
        int                 ntsubs = ws.getNumTargSubs();
        QueueRefs           queues = new QueueRefs();
        
        if (is_val) {
            int     i;
            String  uid = uniqueValueId(loc);

            // The  WordSpec 'ws' describes a primitive which selects a single
            // entry from val[].
            // Return that entry.
            if (ws.getCheckType().isPrimitive() && (ws.getDimDes() == null)) {
                Val tdev_val;
                
                i = ws.getWord(0);
                if (val[i] != null) {
                    // has been assigned
                    tdev_val = (Val)val[i];
                    if (ntsubs > 0) {
                        // has target subscripts
                        tdev = ws.selector(null, val, loc);
                        queues.and_set(ws.getSQueues(), loc);
                        queues.and_set(tdev_val.getQueues(), loc);
                        tdev_val = new Val(tdev, loc);
                        uid = uniqueValueId(loc);
                        combine(uid, ws, false, false);

                    }
                } else {
                    // has not been assigned
                    if (pre_val[i] == null) {
                        // unassigned value has not been evaluated previously
                        if (ntsubs > 0) {
                            // has target subscripts
                            tdev = ws.selector(null, pre_val, loc);
                            queues.and_set(ws.getSQueues(), loc);
                            pre_val[i] = new Val(tdev, loc);
                            uid = uniqueValueId(loc);
                            combine(uid, ws, true, false);
                        } else {
                            // does not have target subscripts
                            tdev = TDEVar.makeTDEVar(uid, ws, loc);
                            tdev_val = new Val(null, Mode.VALUE, tdev, loc);
                            tdev_val.addOVar(getCurrentClockVar());
                            pre_val[i] = tdev_val;
                        }
                    }
                    tdev_val = pre_val[i];
                }

                tdev_val.addOVal(this, ws);
                tdev_val.andSetQueues(queues, loc);

                return(tdev_val);
            }

            // The WordSpec 'ws' selects multiple entries from val[].
            // Iterate through 'ws' looking each bit range up in
            // 'wordspec'. The index in 'wordspec', 'j', gives us the bit
            // index into the variable type which is used to get an entry
            // from 'val[]'. This TDEVar describes the real 'word' being
            // referenced. Create a new TDEVar of the same width,
            // connecting it to the referenced TDEVar. Also append a bit
            // range describing the position in this variable to a new
            // WordSpec, 'bnew'. A new TDEVar is finally created using the
            // same identifier as the partial TDEVars but with the complete
            // WordSpec just constructed from all the parts. This is
            // returned as the value.
            HashSet<Object> ovars = new HashSet<Object>();
            HashSet         execs = new HashSet();
            Val             val_prev = null;
            WordSpec        wsnew = new WordSpec();
            
            wsnew.setCheckType(ws.getCheckType());
            wsnew.setDimDes(ws.getDimDes());
            wsnew.setVar(this);

            for (int word : ws.getWords()) {
                Val         t_val = (Val)(val[word]);
                WordSpec    nws = wordspec.getWordWordSpec(word);
                //TDEVar      nt = new TDEVar(uid, nws, loc);
                TDEVar      nt = TDEVar.makeTDEVar(uid, nws, loc);
                wsnew.append(nws);
                TDEVar  t = null;
                
                if (t_val != null) {
                    Ptype       ptype = t_val.getPrimType();
                    boolean     signed = (ptype == Ptype.INT) || (ptype == Ptype.FIXED);
                    val_prev = t_val;
                    t = t_val.getTDEVar();
                    tdelist.connect(nt, t, signed);
                    if (t_val.getQueues() != null) {
                        queues.and_set(t_val.getQueues(), loc);
                    }
                    if (t_val.getOvars() != null)
                        ovars.addAll(t_val.getOvars());
                    execs.addAll(t_val.getExecSets());
                } else {
                    if (pre_val[word] == null) {
                        t = nt;
                        pre_val[word] = new Val(null, Mode.VALUE, t, loc);
                        pre_val[word].addOVar(getCurrentClockVar());
                    } else {
                        tdelist.connect(nt, pre_val[word].getTDEVar());
                        execs.addAll(pre_val[word].getExecSets());
                    }
                    val_prev = pre_val[word];
                    if (val_prev.getOvars() != null)
                        ovars.addAll(val_prev.getOvars());
                }
            }

            //tdev = new TDEVar(uid, wsnew, loc);
            tdev = TDEVar.makeTDEVar(uid, wsnew, loc);
            if (ws.getNumTargSubs() > 0)
                tdev = ws.selector(null, val, loc);

            Val tdev_val = new Val(tdev, loc);
            tdev_val.getExecSets().addAll(execs);
            tdev_val.andSetQueues(queues, loc);
            tdev_val.addOVars(ovars);
            
            tdev_val.addOVal(this, ws);

            return(tdev_val);
        } else {
            //
            // is to be directly referenced
            //
            tdev = TDEVar.makeTDEVar(this, ws, loc);
            Val tdev_val = new Val(tdev, loc);
            return(tdev_val);
        }
    }

    /**
     * Assignment to this variable in general
     * (the special case of a RHS value variable was to be handled by
     * valValAssignTo() below for more efficient code, however this was
     * never completed).
     * @param   lref is the left variable reference.
     * @param   rval is the RHS value to be assigned.
     * @param   loc is a source file location for error messages
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void assignTo (Ref lref, Val rval, SrcLoc loc) {
        if (rval.getMode() == Mode.CLOCK) {
            // Special case of a RHS clock mode variable.
            Clock       rvar = (Clock)rval.getVar();
            if (readonly)
                throw new ExEx("cannot assign to " + name, loc);
            WordSpec    lws = lref.getWordSpec();   // LHS wordspec
            if (lws.getNumTargSubs() > 0)
                throw new ExEx("LHS VALUE variable has target subscript(s)", loc);

            if (lref.getPrimType() != Ptype.LOG)
                throw new ExEx("LHS variable '" + name +
                                            "' must be type log", loc);
            if (lws.numWords() != 1 || lws.numBits() != 1)
                throw new ExEx("LHS variable '" + name +
                                            "' must be single component", loc);

            int         lindex = lws.getWord(0);

            TDEVar  tdev = rvar.getClkSig();
            Val     tdev_val = new Val(null, Mode.VALUE, tdev, loc);
            tdev_val.addOVar(rvar);
            val[lindex] = tdev_val;
            val_type[lindex] = Type.LOG;
            if (pre_val[lindex] != null) {
                Val pv = pre_val[lindex];
                pv.resolveClocks(rvar, Calloc.VALUE, loc);
                tdelist.connect(pre_val[lindex].getTDEVar(), tdev);
                pre_val[lindex] = null;
            }
            assigned = true;
            return;
        }
        
        if (readonly)
            throw new ExEx("cannot assign to '" + name + "' - read-only!", loc);
        
        WordSpec    lws;
        int         words;
        QueueRefs    queues = rval.getQueues();
        if (type.getPrimType() == Ptype.EMPTY) {
            // Special case of empty type - can assign anything to it.
            words = rval.getWordSpec().numWords();
            type = rval.getType();
            wordspec = rval.getWordSpec();
            val = new Val[words];
            val_type = new Type[words];
            pre_val = new Val[words];
            val_used = new boolean[words];
            lws = wordspec;
        } else {
            lws = lref.getWordSpec();// LHS wordspec
            words = lws.numWords();  // rws is same size (prev check)
        }
        
        String  uid = uniqueValueId(loc);

        for (int i=0 ; i<words ; i++) {
            TDEVar              tdev = rval.getWordTDEVar(lws, i, loc);

            if (tdev == null)
                continue;

            int         j = lws.getWord(i);
            int         lower = lws.getLower(i);
            int         upper = lws.getUpper(i);
            Ptype       ptype = lws.getPrimType(i);
            ArrayList   execs = rval.getExecSets();
            Type        nt_type;
            WordSpec    ntws;
            if (ptype == Ptype.ENUM) {
                nt_type = lws.getType(i);
                ntws = nt_type.getWordSpec(null, loc);
                ntws.addToSubs(0, lower);
            } else {
                nt_type = new Type(ptype, upper-lower+1);
                ntws = lws.getWordWordSpec(i);
            }
            TDEVar  ntdev = TDEVar.makeTDEVar(uid, ntws, loc);            
            Val     ntdev_val = new Val(null, Mode.VALUE, ntdev, loc);
            ntdev_val.getExecSets().addAll(execs);
            
            ntdev_val.andSetQueues(queues, loc);
            ntdev_val.addOVars(rval);
            val[j] = ntdev_val;
            val_type[j] = nt_type;
            if (pre_val[j] != null) {
                if (!rval.getQueues().isEmpty())
                    throw new ExEx(name + " - value post-assignment has queue reads", loc);
                if (!rval.getWordSpec().getSQueues().isEmpty())
                    throw new ExEx(name + " - value post-assignment has target subscript queue reads", loc);
                if (tdev.getType() == TDEVtype.VAR) {
                    if (execs.size() != 0)
                        throw new ExEx(name + " - value post-assignment has cmemory reads", loc);
                    Clock pvdclock = pre_val[j].collectClocks(loc);
                    if (pvdclock != null) {
                        ntdev_val.addOVar(pvdclock);
                        ntdev_val.resolveClocks(pvdclock, Calloc.VALUE, loc);
                    }
                }
                TDEVar  pre_val_tdev = pre_val[j].getTDEVar();
                tdelist.connect(pre_val_tdev, tdev);
                if (!pre_val_tdev.toString().equals(ntdev.toString()))
                    tdelist.connect(ntdev, tdev);
                //if (tdev.isConst())
                //    pre_val_tdev.setConst(tdev);
                pre_val[j] = null;
            } else if (tdev.isConst()) {
                // If the assigned value is a constant then simply
                // connecting ntdev to tdev will not be sufficient.
                // Need to make sure ntdev also looks like a constant.
                ntdev.type = tdev.type;
                ntdev.val = tdev.val;
            } else {
                // It would be good to check for logic loops here.
                // tdev should not represent an expression which contains the current value
                // variable unless it has been initialised or already assigned a value.
                // I have not yet thought of a way to implement that.
                tdelist.connect(ntdev, tdev);
            }
        }
        assigned = true;
    }

    /*
     * NO LONGER USED
     * 
     * Target VALUE assignment to this variable from a value variable.
     * @param   lref is the left variable reference.
     * @param   rvar is the RHS value variable to be assigned.
     * @param   subs is the subscript/field description for the occurence
     * @param   loc is a source file location for error messages
    public void valValAssignTo (Ref lref, Var rvar, NodeList subs, SrcLoc loc) {
        SubFieldList    sfl = new SubFieldList(subs, loc);
        WordSpec        lws = lref.getWordSpec();               // LHS wordspec
        Type            rtype = rvar.getType();
        WordSpec        rws = rtype.getWordSpec(this, sfl, loc);// RHS wordspec
        lws.checkMatch(rws, false, "assignment mismatch - ", loc);
        int         words = lws.numWords();  // rws is same size (just checked)
        
        for (int i=0 ; i<words ; i++) {
            int lindex = lws.getWord(i);
            int rindex = rws.getWord(i);
            // In general copy the value from RHS to LHS.
            if (rvar.val[rindex] == null) {
                if (rvar.pre_val[rindex] == null) {
                    int l = rws.getLower(i);
                    int u = rws.getUpper(i);
                    int ptype = rvar.val_type[rindex];
                    rvar.pre_val[rindex] = new TDEVar(this, l, u, ptype, loc);
                }
                val[lindex] = rvar.pre_val[rindex];
            } else
                val[lindex] = rvar.val[rindex];
            val_type[lindex] = rvar.val_type[rindex];
            if (pre_val[lindex] != null) {
                tdelist.connect(pre_val[lindex], (TDEVar)val[lindex]);
                pre_val[lindex] = null;
            }
        }
    }
     */

    /**
     * For a target variable output the code to create the variable itself.
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

        if (!assigned && !used && (type.getPrimType() != Ptype.NULL)) {
            /*
             * Often assigned to another compiler-generated expression
             * so deletion is not necessarily significant!
            msg("\n\tvalue variable '" + ename + "' unused - deleted");
            */
            return;
        }
        
        int     words = wordspec.numWords();
        boolean ucr_err = false;

        if (pre_val != null) {
            // pre_val == null for type Ptype.NULL even though words == 1 -
            // this is because type.numWords() == 0 but wordspec.numWords() == 1 !
            for (int i=0 ; i<words ; i++) {
                int j = wordspec.getWord(i);
                if ((pre_val[j] != null) && val_used[j]) {
                    ucr_err = true;
                    if (boolDir("rptToFile"))
                        emsg("\nValue mode variable '" + ename + "' word " + i + " bit " + j + " unassigned\n");
                }
            }
            if (ucr_err) {
                emsg("\nvalue mode variable '" + ename +
                                "' has unassigned components referenced!\n");
            }
        }

        combine(ename, wordspec, false, wordspec.numWords() == 1);
    }
    
    // Create a new TDEVar with identifier 'comb_name' and connect
    // current values selected by WordSpec 'ws'.
    // The intent of this is to create a TDE list TDEVar variable which has the simple
    // variable ID with no added trailing unique integer and connect it to the last assigned
    // value of this value mode variable. This is to provide a simple label that can be
    // seen in the simulator. This is of limited use in that of course value mode variables
    // can be assigned multiple times and thus this label will only reflect the last assignment
    // in immediate execution. In addition the simulator has atrophied over the years as it has
    // not been updated following 3PL changes and so is not currently operable.
    private void combine (String comb_name, WordSpec ws, boolean pre, boolean complete) {
        if (!valuelabel)
            return;
        int     words = ws.numWords();
        int     w;
        for (int i=0 ; i<words ; i++) {
            w = ws.getWord(i);
            Val     t_val = pre ? pre_val[w] : (Val)val[w];

            if (t_val != null) {
                TDEVar      t = t_val.getTDEVar();
                WordSpec    part_ws = ws.getWordWordSpec(i);
                TDEVar      tdev = TDEVar.makeTDEVar(comb_name, part_ws, decloc);

                tdelist.connect(tdev, t);
            }
        }
        tdelist.namesignal(comb_name, ws, decloc);
    }
}
