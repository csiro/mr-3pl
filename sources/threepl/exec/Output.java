package threepl.exec;

import static threepl.ThreePL.*;
import static threepl.codegen.TDEVar.GND;
import static threepl.codegen.TDEVar.VCC;

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
 * This class represents an OUTPUT mode variable. The variable has a name String,
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
public final class Output extends Var implements Constant, TDEConstants {
    private TDEVar[]        out_exec;   // 3-state control for OUTPUT mode
    private boolean         direct;     // direct assignment
    private HashSet<Object> ovars;      // variables assigned to this variable
    private String[]        ids;        // element block identifiers
    private Val             source;     // an unconditional value to be assigned

    /**
     * Construct an output mode variable.
     * @param   ident is the variable identifier
     * @param   t is the variable type
     * @param   source is an unconditional value to be assigned, either a single value
     *          or a compound value
     * @param   in_par is true if this is a module, procedure of function
     *          input parameter
     * @param   out_par is true if this is a module or procedure output
     *          parameter
     * @param   loc is the source file location
     */
    public Output (
        Ident   ident,
        Type    t,
        Val     source,
        boolean in_par,
        boolean out_par,
        SrcLoc  loc
    ) {
        super(ident, in_par, out_par, t, Ptype.EMPTY, loc);

        // Mode.
        mode = Mode.OUTPUT;

        int     words = type.numWords();
        isInPar = in_par;
        isOutPar = out_par;

        this.source = source;

        if (words > 0) {
            val = new Object[words];
            val_type = type.getTypeArray();
            out_exec = new TDEVar[words];
            
            // Add the block id array to the attributes map.
            int         n = wordspec.numBits();
            ids = new String[n];
            for (int i=0 ; i<n ; i++)
                ids[i] = "b" + icount++;                   
            attributes.put("ids", new Val(ids, "str", loc));
        }
        
        if (getFamily().outputDirect())
            setDirect(true, loc);
        else
            setDirect(false, loc);

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
      
        TreeMap<String,Val> attr = (TreeMap<String,Val>)val.getVal(0);

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
                throw new ExEx("output() - '" + mkey + "' attribute not known", loc);
            case WRONG_TYPE:    // Has already thrown exception and never returns here
            case OK:            // OK - add to attributes map.
            }
                        
            if (mval.getPrimType() == Ptype.NULL)
                attributes.remove(mkey);
            
            if (mkey.equals("writedomain")) {
                Clock clockvar = getCurrentClockVar();
                if (mval.getPrimType() != Ptype.NULL) {
                    if (mval.getMode() != Mode.CLOCK)
                        throw new ExEx("output() - writedomain attribute is not a clock variable", loc);
                    clockvar = (Clock)mval.getVar();
                }
                setInputClock(clockvar, Calloc.WRITEDOMAIN, loc);
            } else if (mkey.equals("direct"))
                setDirect(mval.getSingleLval(loc), loc);
            else
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
            attr.put("mode", new Val("output", loc));
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
     * argument variable reference. If the parameter variable as a non=null source
     * it is copied to the argument variable overwriting any source value there.
     * @param   arg is the argument reference to which this parameter is
     *          to point
     * @param   loc is the source file location of the parameter declaration
     */
    public void setIndirect (RefOrVal arg, SrcLoc loc) {
        super.setIndirect(arg, loc);
        
        Output v = (Output)arg.getVar();
 
        // Copy stuff from this parameter output mode variable to the
        // argument output mode variable.
        if (!attributes.isEmpty())              // if any attributes ...
            v.attributes.putAll(attributes);    // copy the attributes
        if (source != null)                     // if source argument ...
            v.source = source;                  // copy it
        
        // copy the argument indirect subscript/field list to this parameter
        ind_sfl = arg.getSubFields(); 
    }
    
    /**
     * Set the input clock, which is the primary clock.
     * @param   clk is the clock domain variable
     * @param   e is the event setting the clock
     * @param   loc is the source file location
     */
    public void setInputClock (Clock clk, Calloc e, SrcLoc loc) { return; }

    /**
     * Set the output clock. This does nothing for mode OUTPUT.
     * @param   clk is the clock domain variable
     * @param   ws specifies which components are being evaluated - used
     *          only for value mode
     * @param   e is the event setting the clock
     * @param   loc is the source file location
     */
    public void setOutputClock (Clock clk, WordSpec ws, Calloc e, SrcLoc loc) { return; }
    
    /**
     * Check/set the primary clock.
     * @param   clk is the clock domain variable
     * @param   e is the event setting the clock
     * @param   loc is the source file location
     */
    public void setClock (Clock clk, Calloc e, SrcLoc loc) { return; }
    
    /**
     * Set the 'direct' flag.
     * @param   d is the value to set the flag
     * @param   loc is the source file location
     */
    public void setDirect (boolean d, SrcLoc loc) {
        direct = d; // use same boolean for each attribute to save space
        attributes.put("direct", new Val(d, loc));
    }
    
    /**
     * Get the direct assignment flag.
     * @return  the direct assignment flag
     *
    */
    public boolean getDirect () {
        return(direct);
    }

    /**
     * Get the value of a variable. The value includes all the
     * information provided by a variable reference (see getRef()) plus
     * For immediate mode type an ArrayList of values. For target mode
     * (value, static or queue), it returns the TDEVar. The
     * <B>varnode</B> argument is used to get any flags associated with
     * the variable occurrence, but it may be null if the reference is
     * being requested in some other context.
     * @param   varnode is the code tree node, may be null (used to
     *          detect variable node flags)
     * @param   ref is the reference to this variable
     * @param   loc is the source file location
     * @return  the value or values extracted from the variable
    public Val getVal (VarNode varnode, Ref ref, SrcLoc loc) {
        SubFieldList    sfl = ref.getSubFields();
        Flag flag = (varnode == null) ? Flag.NONE : varnode.getFlag();           
        if (flag != Flag.NONE)
            throw new ExEx("OUTPUT mode variable cannot have ++ or --", loc);
        if (mode == Mode.OUTPUT && !creating_variables)
            throw new ExEx("cannot get the value of an output mode variable", loc);

        WordSpec    ws = ref.getWordSpec();

        Val v = new Val(ref, false, loc);
        v.addOVar(this);
        v.addOVars(ref);
        return(v);
    }
     */

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
            throw new ExEx("OUTPUT mode variable cannot have ++ or --", loc);

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
        int         ntsubs = ws.getNumTargSubs();

        if ((ntsubs > 0))
                throw new ExEx("OUTPUT mode variable '" + name + "' has target subscripts", loc);
        Val tdev_val = new Val(TDEVar.makeTDEVar(this, ws, loc), loc);
        return(tdev_val);
    }

    /**
     * Target OUTPUT assignment to this variable in general
     * @param   lref is the left variable reference.
     * @param   rval is the RHS value to be assigned.
     * @param   exec is a conditional execution signal or null
     * @param   asstype is the assignment type (unused)
     * @param   toplevel is true if this is the top level in a module (unused)
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
        assignTo(lref, rval, exec, loc);
    }

    /**
     * Target OUTPUT assignment to this variable in general
     * @param   lref is the left variable reference.
     * @param   rval is the RHS value to be assigned.
     * @param   exec is a conditional execution signal or null
     * @param   loc is a source file location for error messages
     */
    public void assignTo (
        Ref     lref,
        Val     rval,
        TDEVar  exec,
        SrcLoc  loc
    ) {
        WordSpec    lws = lref.getWordSpec();// LHS wordspec
        WordSpec    rws = rval.getWordSpec();// RHS wordspec
        int         words = lws.numWords();  // rws is same size (prev check)
        QueueRefs    queues = rval.getQueues();
        if (!queues.isEmpty())
            throw new ExEx("output mode source contains queue reads", loc);
        
        switch (rval.getMode()) {
        case IMMEDIATE:
        case SELECTVALUE:
        case VALUE:
        case STATIC:
        case INPUT:
        case PRIORITY:
        case CLOCK:
            break;
        case QUEUE:
            if (!queues.isEmpty())
                throw new ExEx("output mode source cannot contain a queue read", loc);
            break;
        case CMEMORY:
            throw new ExEx("output mode source cannot be cmemory mode", loc);
        case RMEMORY:
            throw new ExEx("output mode source cannot be rmemory mode", loc);
        case OUTPUT:
            throw new ExEx("output mode source cannot be output mode", loc);
        }

        for (int i=0 ; i<words ; i++) {
            TDEVar  tdev = rval.getWordTDEVar(lws, i, loc);         
            int     j = lws.getWord(i);

            //if (tdev == null)
            //    continue;
            val[j] = new Val(null, Mode.VALUE, tdev, loc);
            val_type[j] = rws.getType(i);
            if (out_exec[j] != null)
                throw new ExEx("output mode variable '" + name +
                    "' attempted re-assignment", loc);
            if (exec == null)
                out_exec[j] = VCC;
            else
                out_exec[j] = exec;
        }
        ovars = rval.getOvars();
        Clock   clk = rval.collectOutputClocks(loc);
        if (clk != null)
            setClock(clk, Calloc.ASSIGN, loc);
        assigned = true;
        rval.setUsed();
    }

    /**
     * Target OUTPUT assignment to this variable from list of Val of
     * type "log". The list members may be logical constants or variables.
     * No Ref is needed as the type is guaranteed to be "[]log" whose
     * dimension equals the list size.
     * @param   vals is an array list of Val
     * @param   loc is a source file location for error messages
     *
    public void assignTo (ArrayList<Val> vals, SrcLoc loc) {
        int i = 0;
        for (Val v : vals) {
            val[i] = v;
            val_type[i] = Type.LOG;
            out_exec[i++] = VCC;
        }
    }*/

    /**
     * Output the code to create the variable itself.
     * This method is called on completion of program interpretation at which
     * point all assignments to this variable will have been made.
     */
    @SuppressWarnings({ "incomplete-switch", "unused" })
    public void createVar () {
        current_create = this;
        // If the simulator is to be run, add a TDEType.SIMVAR to the TDE list
        // to notify the simulator about this variable.
        if (sim_implemented && sim && (ename != null)) {
            TDE simtdevar = new TDE(TDEType.SIMVAR);
            simtdevar.add2p(this);
            tdelist.addTDE(simtdevar);
        }

        // Nothing to do for indirect (parameter).
        if (indass)
            return;
        
        // If there is a source Val assign it.
        if (source != null)
            getRef(null, null, decloc).assignTo(source, null, decloc);

        // Check that there are no clock domain conflicts.
        RefOrVal.collectOutputClocks(ovars, decloc);
        
        TDEVar  default_out = null;
        boolean rename = (ename != null);
        boolean nc = false;

        TDEVar      otdev = TDEVar.makeTDEVar(ename, wordspec, decloc);
        int         words = otdev.numWords();
        int         npins = 0;
        String[]    locs = null;
        Boolean[]   diff = null;
        Boolean[]   noshadow = null;
        Boolean[]   defaultout = null;
        
        if ((attributes != null) && attributes.containsKey("loc")) {
            Val     v = attributes.get("loc");
            npins = v.numWords();
            locs = new String[npins];
            for (int i=0 ; i<npins ; i++)
                locs[i] = v.getArraySval(i, decloc);
        }       
        if (npins == 0)
            throw new ExEx("output() - '" + name + "' has no location attributes", decloc);

        // Get differential, noshadow and defaultout attribute(s)
        diff = getAttribute("differential", npins);
        noshadow = getAttribute("noshadow", npins);
        defaultout = getAttribute("defaultout", npins);
        // If no defaultout attribute use the global "unass_out" directive if defined.
        if ((defaultout == null) && (findDir("unass_out")) != null) {
            defaultout = new Boolean[1];
            defaultout[0] = boolDir("unass_out");
        }
        
        TDEVar      tdo;
        TDEVar      tdod;
        TDE         obuf;
        int         bits = wordspec.numBits();
        Type        otype = new Type("[" + bits + "]log", decloc);
        WordSpec    ows = otype.getWordSpec(this, decloc);
        TDEVar      tdi = tdelist.signal("OUTPUTBIT", ows, decloc);
        int         i = 0;  // id array index
        int         j = 0;  // pin location array index        
        
        for (int word=0 ; word<words ; word++) {
            int     wwidth = otdev.getWidth(word);
            TDEVar  out_exec_ = null;
            boolean isAssigned =  (val[word] != null);
            boolean skip = false;
            
            // Default output enable is GND (OBUFE enable input is inverted!)
            if (isAssigned) {
                // This word has been assigned a value.
                tdelist.connect(otdev.getWord(word, decloc), ((Val) val[word]).getTDEVar());
                // If out_exec[word] is not VCC then create an
                // inverter to drive the OBUFE _enable.
                if (out_exec[word] != VCC) {
                    TDE     invert = new TDE(TDEType.INV, decloc);

                    out_exec_ = tdelist.signal("OBE", decloc);
                    invert.add2i(out_exec[word]);
                    invert.add2o(out_exec_);
                    tdelist.addTDE(invert);
                } 
            } else if (nc) {
                // This word has not been assigned a value and the
                // global default has been explicitly set to "not connected"
                // so skip creating the OBUF.
                skip = true;
            }

            for (int bit=0 ; bit<wwidth ; bit++,i++,j++) {
                Boolean bitdiff = diff == null ? false : (diff.length == 1 ? diff[0] : diff[j]);
                Boolean bitnoshadow = noshadow == null ? false : (noshadow.length == 1 ? noshadow[0] : noshadow[i]);
                Boolean bitdefaultout = defaultout == null ? false : (defaultout.length == 1 ? defaultout[0] : defaultout[i]);

                if ((j >= npins) || (bitdiff && ((j+1) >= npins)))
                    throw new ExEx("output() - '" + name +
                                    "' insufficient pins for variable width", decloc);

                if (!skip) {
                    tdo = new TDEVar("PORT_"+locs[j], decloc);
                    tdod = null;
                    if (bitdiff)
                        tdod = new TDEVar("PORT_"+locs[j+1], decloc);
                    obuf = new TDE(TDEType.OBUF, decloc);

                    obuf.add2p(ids[i]);
                    obuf.add2p(locs[j]);
                    if (bitdiff)
                        obuf.add2p(locs[j+1]);
                    else
                        obuf.add2p();
                    obuf.add2p(bitdiff);
                    obuf.add2p(bitnoshadow);

                    if (isAssigned) {
                        obuf.add2i(tdi.getWord(i, decloc)); // input signal to OBUF
                    } else {
                        if (defaultout != null)
                            obuf.add2i(bitdefaultout ? VCC : GND);
                        else
                            throw new ExEx("output() - '" + name + "' not (fully) assigned", decloc);
                    }
                    obuf.add2i(out_exec_);                  // optional output enable

                    obuf.add2o(tdo);                        // output signal from OBUF to pad (external port)
                    obuf.add2o(tdod);                       // 2nd output signal for differential

                    tdelist.addTDE(obuf);
                }

                if (bitdiff)
                    j++;
            }
        }
        tdelist.connect(tdi, otdev, false);
        if (j != npins)
            throw new ExEx("output() - '" + name + "' excess pins for variable width", decloc);
    }
}
