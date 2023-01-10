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
 * This class represents an INPUT mode  variable. The variable has a name String,
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
public final class Input extends Var implements Constant, TDEConstants {
    private String[]    ids;    // element block identifiers
    private Value       diffvar;

    /**
     * Construct an input mode variable.
     * @param   ident is the variable identifier
     * @param   t is the variable type
     * @param   init is an initialisation value, either a single value
     *          or a compound value
     * @param   in_par is true if this is a module, procedure of function
     *          input parameter
     * @param   out_par is true if this is a module or procedure output
     *          parameter
     * @param   v2 is an optional 2nd output for input buffer with differential output
     * @param   loc is the source file location
     */
    public Input (
        Ident   ident,
        Type    t,
        Val     init,
        boolean in_par,
        boolean out_par,
        Value   v2,
        SrcLoc  loc
    ) {
        super(ident, in_par, out_par, t, Ptype.EMPTY, loc);

        // Mode.
        mode = Mode.INPUT;

        isInPar = in_par;
        isOutPar = out_par;


        if (init != null)
                throw new ExEx("INPUT mode variable cannot be initialised", loc);
        
        if ((type != null) && (type.getPrimType() != Ptype.NULL)) {
            tdelist.namesignal(ename, wordspec, loc);
            
            // Add the block id array to the attributes map.
            int         n = wordspec.numBits();
            ids = new String[n];
            for (int i=0 ; i<n ; i++)
                ids[i] = "b" + icount++;                   
            attributes.put("ids", new Val(ids, "str", loc));
        }
        readonly = true;
        
        diffvar = v2;

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
      
        TreeMap<String,Val> attr = (TreeMap<String, Val>)val.getVal(0);

        Set<String> ks = attr.keySet();
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
                throw new ExEx("attribute \"" + mkey + "\" for variable '" + name + "' is unknown", loc);
            case WRONG_TYPE:    // Has already thrown exception and never returns here
            case OK:            // OK - add to attributes map.
            }
                        
            if (mval.getPrimType() == Ptype.NULL)
                attributes.remove(mkey);
            
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
        
        TreeMap<String, Val> attr = null;
        if (extended) {
            attr = new TreeMap<String, Val>();
            attr.put("identifier", new Val(name, loc));
            attr.put("eidentifier", new Val(ename, loc));
            attr.put("mode", new Val("input", loc));
            attr.put("type", new Val(type, loc));
            attr.put("typestring", new Val(wordspec.getTypeString(), loc));
            attr.put("decloc", new Val(decloc.toString(), loc));
            attr.put("inputparam", new Val(isInPar, loc));
            attr.put("outputparam", new Val(isOutPar, loc));
            attr.put("matched", new Val(matched, loc));
            attr.put("assigned", new Val(assigned, loc));
            attr.put("used", new Val(used, loc));
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
        
        Input v = (Input)arg.getVar();
        
        // Copy stuff from this parameter input mode variable to the
        // argument input mode variable.
        if (!attributes.isEmpty())              // if any attributes ...
            v.attributes.putAll(attributes);    // copy the attributes
        
        // copy the argument indirect subscript/field list to this parameter
        ind_sfl = arg.getSubFields();    
    }
    
    /**
     * Set the input clock. Does nothing for INPUT mode.
     * @param   clk is the clock domain variable
     * @param   e is the event setting the clock
     * @param   loc is the source file location
     */
    public void setInputClock (Clock clk, Calloc e, SrcLoc loc) { return; }

    /**
     * Set the output clock, which is the primary clock.
     * This is called when the input variable is evaluated, but we don't want to set the clock domain
     * this way so do nothing.
     * @param   clk is the clock domain variable
     * @param   e is the event setting the clock
     * @param   loc is the source file location
     */
    public void setOutputClock (Clock clk, Calloc e, SrcLoc loc) { used = true; }

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
            throw new ExEx("INPUT mode variable cannot have ++ or --", loc);

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
     * associated with the variable occurrence (++ or -- flags
     * of the string indirect operator/flag), but it may be null if the
     * reference is being requested in some other context.
     * The <B>subs</B> argument is a list of subscript or field
     * nodes associated with the variable occurrence - it may be null.
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
            throw new ExEx("INPUT mode variable cannot have ++ or --", loc);
        
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
            tdev = TDEVar.makeTDEVar(ename, ws, loc);
        Val tdev_val = new Val(tdev, loc);
        tdev_val.andSetQueues(queues, loc);
        return(tdev_val);
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

        boolean retain = false;
        if ((attributes != null) && attributes.containsKey("retain"))
            retain = attributes.get("retain").getSingleLval(decloc);

        if (!used && !retain) {
            rpt("\tinput variable '" + ename + "' unused - deleted");
            return;
        }

        Ref         ref = getRef(null, null, decloc);
        TDEVar      tdev = ref.getTDEVar();
        Ref         refd = null;
        TDEVar      tdevd = null;
        int         words = tdev.numWords();
        int         npins = 0;
        String[]    locs = null;
        Boolean[]   diff = null;
        
        if ((attributes != null) && attributes.containsKey("loc")) {
            Val v = attributes.get("loc");
            npins = v.numWords();
            locs = new String[npins];
            for (int i=0 ; i<npins ; i++)
                locs[i] = v.getArraySval(i, decloc);
        }
        if (npins == 0)
            throw new ExEx("input() - '" + name + "' has no location attributes", decloc);

        // differential attribute(s)
        diff = getAttribute("differential", npins);
        
        if (diffvar != null) {
            if (diff == null)
                throw new ExEx("input() - differential output variable supplied but input is not differential", decloc);
            tdevd = tdelist.signal("DIFFOUT", wordspec, decloc);
            Val v = new Val(null, Mode.VALUE, tdevd, decloc);
            refd = diffvar.getRef(null, null, decloc);
            diffvar.assignTo(refd, v, decloc);
        }

        int         bits = wordspec.numBits();
        Type        otype = new Type("[" + bits + "]log", decloc);
        WordSpec    ows = otype.getWordSpec(this, decloc);
        TDEVar      tdo = tdelist.signal("INPUT", ows, decloc);
        TDEVar      tdo2 = tdelist.signal("INPUT", ows, decloc);
        int         i = 0;  // id array index
        int         j = 0;  // pin location array index
        
        for (int word=0 ; word<words ; word++) {
            int         wwidth = tdev.getWidth(word);
            for (int bit=0 ; bit<wwidth ; bit++,i++,j++) {
                Boolean bitdiff = diff == null ? false : (diff.length == 1 ? diff[0] : diff[j]);

                if ((j >= npins) || (bitdiff && ((j+1) >= npins)))
                    throw new ExEx("input() - '" + name + "' insufficient pins for variable width", decloc);

                TDE     ibuf = new TDE(TDEType.IBUF, decloc);
                TDEVar  tdi = new TDEVar("PORT_"+locs[j], decloc);
                TDEVar  tdid = null;
                if (bitdiff)
                    tdid = new TDEVar("PORT_"+locs[j+1], decloc);
                ibuf.add2p(ids[i]);
                ibuf.add2p(locs[j]);
                if (bitdiff)
                    ibuf.add2p(locs[j+1]);
                else
                    ibuf.add2p();
                ibuf.add2p(false);                      // not allowing ibufg
                ibuf.add2p(bitdiff);
                
                ibuf.add2i(tdi);                        // input signal from pad to IBUF (external port)
                if (bitdiff)
                    ibuf.add2i(tdid);                   // 2nd input signal for differential

                ibuf.add2o(tdo.getWord(i, decloc));     // output signal from IBUF
                if (diffvar != null)
                    ibuf.add2o(tdo2.getWord(i, decloc));// optional differential output signal from IBUFDS_DIFF_OUT
                else
                    ibuf.add2o(null);

                tdelist.addTDE(ibuf);
                
                if (bitdiff)
                    j++;
            }
        }
        tdelist.connect(tdev, tdo, false);
        if (tdevd != null)
            tdelist.connect(tdevd, tdo2, false);

        if (j != npins)
            throw new ExEx("input() - '" + name + "' excess pins for variable width", decloc);
    }
}
