package threepl.exec;

import static threepl.ThreePL.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exceptions.MapException;
import threepl.nodes.Ident;
import threepl.nodes.NodeList;
import threepl.nodes.VarNode;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * This class represents an immediate mode variable. The variable has a name String,
 * a mode and a Type, which may be complex. If the variable
 * is a module, procedure or function parameter it may have an indirect
 * pointer to another Var, depending on its mode and whether input or
 * output. If the variable is a target mode it
 * will also have an expanded name to ensure that this instance is
 * unique.
 * <p>There is an array containing values for all
 * words of the variable. There is also an associated array containing
 * the Type of each word (this array is present for all modes). The
 * values are objects of suitable type for the primitive type (see
 * threepl.parser.Constant.java) -
 * <UL>
 * <LI> Long for Ptype.INT or Ptype.UINT
 * <LI> Boolean for Ptype.LOG
 * <LI> String for Ptype.STR
 * <LI> Ref for Ptype.PTR (a pointer)
 * </UL>
 *
 * The following special cases apply -
 * <UL>
 * <LI> If isInPar is true then this variable is a module, procedure
 *      or function input parameter.
 * <LI> If isOutPar is true then this variable is a module or procedure
 *      output parameter.
 * </UL>
 */
public final class Immediate extends Var implements Constant, TDEConstants {
    /**
     * Construct an immediate mode variable.
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
    public Immediate (
        Ident   ident,
        Type    t,
        Val     init,
        boolean in_par,
        boolean out_par,
        SrcLoc  loc
    ) {
        super(ident, in_par, out_par, t, Ptype.NULL, loc);

        // Mode.
        mode = Mode.IMMEDIATE;
        
        // initialisation
        int     words = type.numWords();
        if (words > 0) {
            val = new Object[words];
            val_type = type.getTypeArray();

            if (type.getPrimType() == Ptype.FILE) {
                String  path = null;
                if (init != null) {
                    FileDesc  fd = null;
                    switch (init.getPrimType()) {
                    case STR:
                        path = init.getSingleSval(loc);            
                        fd = new FileDesc(path);
                        break;
                    case FILE:
                        fd = init.getSingleFileVal(loc);
                        break;
                    default:
                        throw new ExEx("file variable '" + name + "' - incorrect path specification", loc);
                    }
                    val[0] = fd;
                    return;
                }
                if (in_par)
                    return;
                throw new ExEx("file variable '" + name + "' - no path specification", loc);
            }

            // Default initialisation.
            // Create the variable value array.
            // Initialise the value array to defaults.
            for (int i=0 ; i<words ; i++) {
                switch (val_type[i].getPrimType()) {
                case INT:
                case UINT:
                    val[i] = Long.valueOf(0);
                    break;
                case LOG:
                    val[i] = Boolean.valueOf(false);
                    break;
                case ENUM:
                    val[i] = Long.valueOf(val_type[i].firstEnumOrd());
                    break;
                case FIXED:
                    throw new ExEx("fixed is a target type which is not implemented for immediate mode", loc);
                case UFIXED:
                    throw new ExEx("ufixed is a target type which is not implemented for immediate mode", loc);
                case FLOAT:
                    val[i] = Double.valueOf(0);
                    break;
                case STR:
                    val[i] = new String("");
                    break;
                case MAP:
                    val[i] = new TreeMap<Object, Object>();
                    break;
                case LIST:
                    val[i] = new ArrayList<Object>();
                    break;
                default:
                    break;
                }
            }
            if (init != null) {
                if (!type.isPrimitive() && init.isPrimitive()) {
                    // Duplicate the initial value to a compound the
                    // same size as the compound type.
                    init = init.expandToCompound(type, loc);
                }
                assignTo(AST.IMASS, null, init, loc);
            }
        }
    }
    
    /**
     * Construct an immediate variable as a wrapper for a Val.
     * This is used for Val classes assigned to maps or lists as it allows a Ref to be generated
     * for the resulting map or list value. This in turn allows assignment to a field or array map or list
     * entry and also allows a pointer to a map or list entry, or field or array member of an entry, to
     * be generated.
     * @param o is the array of primitive values in the associated Val
     * @param t is the array or primitive types
     * @param ws is the WordSpec for the Val
     * @param loc is the source file location
     */
    public Immediate (Object[] o, Type[] t, WordSpec ws, SrcLoc loc) {
        super(null, false, false, ws.getType(), Ptype.NULL, loc);
        mode = Mode.IMMEDIATE;
        val = o;
        val_type = t;
        wordspec = ws;
        if (ws.getType().hasTargetType())
            throw new ExEx("!!!!!");
    }
    
    /**
     * Get the map stored in val[i].
     * @return  the map
     */
    /*@SuppressWarnings({ "rawtypes", "unchecked" })
    public TreeMap getMap (int i) { return((TreeMap)val[i]); }*/
    
    /**
     * Set the attributes map.
     * @param   val is the attributes map value
     * @param   loc is the source file location
     */
    public void setAttributes (Val val, SrcLoc loc) {
        if (indass) {
            indirect.setAttributes(val, loc);
            return;
        }
      
        TreeMap<?, ?>         attr = (TreeMap<?, ?>)val.getVal(0);

        Set<?>         ks = attr.keySet();
        Iterator<?>    it = ks.iterator();
        while (it.hasNext()) {
            String  mkey = (String)it.next();
            Val     mval = (Val)attr.get(mkey);

            // Check that attribute is known to 3PL.
            // If it checks out OK, continue processing.
            // If it does not check out skip the rest of the loop.
            if (!checkAttribute(mkey, mval, mode, loc))
                throw new ExEx("attribute \"" + mkey + "\" for variable '" + name + "' is unknown", loc);         
            
            if (mval.getPrimType() == Ptype.NULL)
                throw new ExEx("attribute \"" + mkey + "\" for variable '" + name + "' is null", loc);         
            
            if (mkey.equals("readonly")) {
                // Is internal to 3PL - handle but dont copy to attribute map.
                if (mval.getSingleLval(loc))
                    setReadOnly(loc);
                else {
                    msg(loc.toString());
                    msg("immediate variable " + name + " - readonly attribute cannot be false - ignored");
                }
            }
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
            attr.put("mode", new Val("immediate", loc));
            attr.put("type", new Val(type, loc));
            attr.put("typestring", new Val(wordspec.getTypeString(), loc));
            attr.put("decloc", new Val(decloc.toString(), loc));
            attr.put("inputparam", new Val(isInPar, loc));
            attr.put("outputparam", new Val(isOutPar, loc));
            attr.put("matched", new Val(matched, loc));
            attr.put("readonly", new Val(readonly, loc));
            attr.put("assigned", new Val(assigned, loc));
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
        ind_sfl = arg.getIndSubFields();
    }
        
    /**
     * Set the input clock - does nothing for IMMEDIATE mode.
     * @param   clk is the clock domain variable
     * @param   e is the event setting the clock
     * @param   loc is the source file location
     */
    public void setInputClock (Clock clk, Calloc e, SrcLoc loc) {return;}

    /**
     * Set the output clock - does nothing for IMMEDIATE mode.
     * @param   clk is the clock domain variable
     * @param   ws specifies which components are being evaluated - used
     *          only for value mode
     * @param   e is the event setting the clock
     * @param   loc is the source file location
     */
    public void setOutputClock (Clock clk, WordSpec ws, Calloc e, SrcLoc loc) {
        return;
    }

    /**
     * Check/set the input and output clocks - does nothing for IMMEDIATE mode.
     * @param   clk is the clock domain variable
     * @param   e is the event setting the clock
     * @param   loc is the source file location
     */
    public void setClock (Clock clk, Calloc e, SrcLoc loc) {return;}
    
    /**
     * Determine if this variable is type ENUM.
     * @return  true if this variable is type ENUM
     */
    public boolean isEnumType () {
        if (indass)
            return(((Immediate)indirect).isEnumType());
        if (type.getPrimType() != Ptype.TYPE)
            return(false);
        Type    t = (Type)val[0];
        if (t == null)
            return(false);
        return(t.getPrimType() == Ptype.ENUM);
    }
        
    /**
     * Get the read (output) clock variable associated with this variable.
     * It is immediate mode so the clock domain is null.
     * @return  null
     */
    public Clock getReadClkVar () {
        return(null);
    }

    /**
     * Get the read (output) clock variable associated with this variable.
     * It is immediate mode so the clock domain is null.
     * @param   ws is the WordSpec of the components to be checked
     * @param   loc is the source file location
     * @return  null
     */
    public Clock getReadClkVar (WordSpec ws, SrcLoc loc) {
        return(null);
    }
    
    /**
     * Get the read (output) clock variable associated with this variable.
     * It is immediate mode so the clock domain is null.
     * @param   calloc is the clock allocation event type
     * @param   loc is the source file location
     * @return  null
     */
    public Clock getOutputClkVar (Calloc calloc, SrcLoc loc) {
        return(null);
    }

    /**
     * Get the value of a variable. The value includes all the
     * information provided by a variable reference (see
     * getRef()) plus an ArrayList of values.
     * 
     * The <B>subs</B> argument is a list of subscript or field
     * nodes associated with the variable occurrence.
     * A pointer variable returns a reference to the ultimate variable
     * to which it points.
     * @param   ref is the reference to this variable
     * @param   loc is the source file location
     * @return  the value or values extracted from the variable
     */
    public Val getVal (Ref ref, SrcLoc loc) {
        SubFieldList    sfl = ref.getSubFields();
        WordSpec        ws = ref.getWordSpec();
        Flag            flag = ref.getFlag();
        Var             var = ref.getVar();
        return(var.getVal(ws, sfl, var.val, var.val_type, flag, loc));
    }

    public Val getVal (
        WordSpec        ws,
        SubFieldList    sfl,
        Object[]        vals,
        Type[]          types,
        Flag            flag,
        SrcLoc          loc
    ) {
        if (sfl.hasTargetSubs())
            throw new ExEx("target subscript for immediate variable", loc);

        WordSpec    new_ws = new WordSpec();
        int         n = ws.numWords();
        Object[]    oa = new Object[n];
        Type[]      ta = new Type[n];
        SubField    sf = null;
        if ((sfl.size() == 1) && (type.getPrimType() == Ptype.TYPE) && (val[0] instanceof Type)) {
            //
            // Value is an ENUM. sf contains a field entry which is the value name.
            //
            sf = sfl.getEntry(0);
            Type    t = (Type)val[0];
            if (t.getPrimType() != Ptype.ENUM) {
                if (sf.getType() == SFType.FIELD)
                    throw new ExEx("type variable has field", loc);
                else
                    throw new ExEx("type variable has subscript(s)", loc);
            } else if (sf.getType() != SFType.FIELD)
                throw new ExEx("enum type variable has subscript(s)", loc);
            oa[0] = t.enumOrd(sf.getField(), loc);
            ta[0] = t;
            new_ws.setCheckType(t);
            new_ws.append(0, t);
        } else if (!sfl.scanIndexAtEnd() && (ws.getCheckType().getPrimType() == Ptype.LIST)) {
            //
            // Value is a list entry. sf contains a subscript entry which is the list index
            //
            Val lval = null;
            sf = sfl.getIndexedEntry();
            sfl.incrementScanIndex();   // increment index as index (subscript) has been used
            sfl.trimToScanIndex();    // trim off item from list
            if (sf.getType() != SFType.IMSUB)
                throw new ExEx("list index is not a single subscript", loc);
            int index = sf.getLower();
            @SuppressWarnings("unchecked")
            ArrayList<Val>   al = (ArrayList<Val>)val[ws.getWord(0)];
            if ((index < 0) || (index >= al.size()))
                throw new ExEx("list index is out of range (" + index + " >= " + al.size() + ")", loc);
            lval = al.get(index);
            if (lval == null)
                return(new Val(loc));   // null
            if (!sfl.scanIndexAtEnd()) {
                WordSpec lws = lval.getType().getWordSpec(this, sfl, loc);
                return(getVal(lws, sfl, lval.getVals(), lval.getValTypes(), flag, loc));
            } else
                return(lval);
        } else if (!sfl.scanIndexAtEnd() && (ws.getCheckType().getPrimType() == Ptype.MAP)) {
            //
            // Value is map entry. sf contains either a key or an index (subscript)
            //
            Val mval = null;
            sf = sfl.getIndexedEntry();
            sfl.incrementScanIndex();   // increment index as index/key has been used
            sfl.trimToScanIndex();      // trim off item from list
            @SuppressWarnings("unchecked")
            TreeMap<String,Val> tm = (TreeMap<String, Val>)vals[ws.getWord(0)];
            if (sf.getType() == SFType.KEY) {
                // map entry using key - returns a value
                String  key = sf.getKey();
                if (key == null)
                    throw new ExEx("map key is null", loc);
                mval = tm.get(key);
                if (mval == null)
                    return(new Val(loc));   // null
            } else {
                // map entry using index - returns a key
                int index = sf.getLower();
                if (sf.getType() != SFType.IMSUB)
                    throw new ExEx("map index has subscript range", loc);
                Set<String> ks = tm.keySet();
                oa = ks.toArray();
                if ((index < 0) || (index >= oa.length))
                    throw new ExEx("map index is out of range (" + index + " >= " + oa.length + ")", loc);
                mval = new Val((String)oa[index], loc);
            }
            if (!sfl.scanIndexAtEnd()) {
                WordSpec mws = mval.getType().getWordSpec(this, sfl, loc);
                return(getVal(mws, sfl, mval.getVals(), mval.getValTypes(), flag, loc));
            } else
                return(mval);
        } else if (!sfl.scanIndexAtEnd() && (ws.getCheckType().getPrimType() == Ptype.CLASS)) {
            //
            // Value is a 3PL class. sf must contain a field string.
            //
            sf = sfl.getIndexedEntry();
            sfl.incrementScanIndex();   // increment index as index/key has been used
            sfl.trimToScanIndex();      // trim off item from list
            int     word = ws.getWord(0);
            Scope   sc = (Scope)val[word];
            
            if (sf.getType() != SFType.FIELD)
                throw new ExEx("class reference has no field name", loc);
            String  field = sf.getField();
            if (field == null)
                throw new ExEx("class field is null", loc);
            TreeMap<String,Var> vars = sc.getVars();
            Var var = vars.get(field);
            if (var == null)
                throw new ExEx("class variable '" + field + "' not found", loc);
            Val val = var.getVal(sfl, flag, loc);
            if (!sfl.scanIndexAtEnd()) {
                WordSpec mws = val.getType().getWordSpec(this, sfl, loc);
                return(getVal(mws, sfl, val.getVals(), val.getValTypes(), flag, loc));
            } else
                return(val);
        } else {
            new_ws.setDimDes(ws.getDimDes());
            new_ws.setVar(ws.getVar());
            new_ws.setCheckType(ws.getCheckType());
            int         i = 0;
            checkPreIncrDecr(this, flag, ws, loc);
            for (int k : ws.getWords()) {
                oa[i] = vals[k];
                ta[i] = types[k];
                if ((oa[i] == null) && (types[k].getPrimType() == Ptype.STR))
                    throw new ExEx("NULL STRING VALUE!", loc);
                new_ws.append(i, ta[i]);
                i++;
            }
        }
        
        Val valret = new Val(oa, ta, new_ws, null, sfl, loc);
        valret.setDummyVar(true, loc);  // overwrite the Var field with
                                        // a dummy Var as this current Var
                                        // may change - see line 722
        checkPostIncrDecr(this, flag, ws, loc);
        return(valret);
    }

    // check for ++v or --v and execute it
    private void checkPreIncrDecr (Var var, Flag flag, WordSpec ws, SrcLoc loc) {
        if ((flag != Flag.PREINCR) && (flag != Flag.PREDECR))
            return;
        if (ws.numWords() != 1)
            throw new ExEx("++ or -- on non-primitive type", loc);
        if (ws.getCheckType().getPrimType() != Ptype.INT)
            throw new ExEx("++ or -- on non-integer type", loc);
        int     index = ws.getWord(0);
        long    i = ((Long)var.val[index]).longValue();
        if (flag == Flag.PREINCR)
            var.val[index] = Long.valueOf(i + 1);
        else if (flag == Flag.PREDECR)
            var.val[index] = Long.valueOf(i - 1);
    }
    
    // check for v++ or v-- and execute it
    private void checkPostIncrDecr (Var var, Flag flag, WordSpec ws, SrcLoc loc) {
        if ((flag != Flag.POSTINCR) && (flag != Flag.POSTDECR))
            return;
        if (ws.numWords() != 1)
            throw new ExEx("++ or -- on non-primitive type", loc);
        if ((ws.getCheckType().getPrimType() != Ptype.INT) && (ws.getCheckType().getPrimType() != Ptype.UINT))
            throw new ExEx("++ or -- on non-integer type", loc);
        int     index = ws.getWord(0);
        long    i = ((Long)var.val[index]).longValue();
        if (flag == Flag.POSTINCR)
            var.val[index] = Long.valueOf(i + 1);
        else if (flag == Flag.POSTDECR)
            var.val[index] = Long.valueOf(i - 1);
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
        if (indass) {
            // An indirect via a module, procedure or function parameter.
            if (indirect == null)
                return(null);
            if (ind_sfl != null)
                sfl = ind_sfl.merge(this, sfl);
            return(indirect.getRef(varnode, sfl, is_val, loc));
        }
        return(getRef(wordspec, sfl, is_val, flag, loc));
    }
    
    public Ref getRef (
        WordSpec        ws,
        SubFieldList    sfl,
        boolean         is_val,
        Flag            flag,
        SrcLoc          loc
    ) throws MapException {
        if (sfl.hasTargetSubs())
            throw new ExEx("target subscript for immediate variable" +
                                                    name + "'", loc);

        if ((sfl.size() > 0) && isEnumType()){
            Type    t = (Type)val[0];
            
            // Return the Ref to the enum type Var (this).
            return(new Ref(this, t, sfl, Flag.NONE, is_val, loc));
        }
        
        Val mval = null;
        Var cvar = null;
        //
        // If this Ref is to allow assignment then a Ref to a Var must be
        // returned. A list or map entry is not itself a Var therefore a
        // dummy Var is created whose val[] field is the value of the list
        // or map entry. When that Var is assigned the list or map entry will
        // be changed via the Var. For this to be valid the last 'sfl' entry,
        // which is the subscript or key, must not be consumed here but must
        // be passed back as a SubField to the Var to be processed by the
        // Immediate/assignTo() from which getRef() was called. To achieve
        // this the variable 'x' is assigned 1 to be added to the scan index
        // to avoid the last SubField being consumed here.
        // 
        // If this Ref is called from getVal() the entire SubFieldList is to be
        // consumed here so 'x' is 0.
        //
        // For an enum or class type these considerations do not apply so 'x'
        // is not used. A Var is directly available and so a Ref to that Var is
        // returned.
        //
        int x = is_val ? 0 : 1;
                
        if (sfl.size() > 0) {
            // Have subscripts, fields, keys etc.
            // If 'is_val is false, skip list or map types in this if() chain
            // and process and return using the code following he if() chain.
            //
            // If 'is_val' is true, or for enum or class types,  evaluate
            // using the SubField list and directly return the Ref.
            //
            // Keep a copy of the sfl index as it may be changed below but if a list or map is not
            // found then we need to restore the index.
            int         old_index = sfl.getScanIndex();
            WordSpec    refws = type.getWordSpec(this, sfl, loc);
            Ptype       pt = refws.getCheckType().getPrimType();
            SubField    sf = null;
            
            if (!sfl.scanIndexAtEnd())
                sf = sfl.getIndexedEntry();
           
            if ((sf != null) && (pt == Ptype.LIST) && (sfl.size() > (sfl.getScanIndex() + x))) {
                //
                // List entry.
                //
                if (sf.getType() != SFType.IMSUB)
                    throw new ExEx("reference with subscript range or field/key for type \"list\", variable '" +
                                                            name + "'", loc);
                
                @SuppressWarnings("unchecked")
                ArrayList<Val> al = (ArrayList<Val>)val[refws.getWord(0)];
                // list entry using index
                int index = sf.getLower();
                if (sf.getType() != SFType.IMSUB)
                    throw new ExEx("list index has subscript range", loc);
                if ((index < 0) || (index >= al.size()))
                    throw new ExEx("list index is out of range (" + index + " >= " + al.size() + ")", loc);
                mval = al.get(index);
                sfl.incrementScanIndex();   // increment index as subscript (list index) has been used
                sfl.trimToScanIndex();      // trim off subscript item from list
                
                // Break from the if() chain and execute the following code.
            } else if ((sf != null) && (pt == Ptype.MAP) && (sfl.size() > (sfl.getScanIndex() + x))) {
                //
                // Map entry.
                //
                if ((sf.getType() != SFType.KEY) && (sf.getType() != SFType.IMSUB))
                    throw new ExEx("reference with subscript range for type \"map\", variable '" +
                            name + "'", loc);
                
                @SuppressWarnings("unchecked")
                TreeMap<String,Val> tm = (TreeMap<String, Val>)val[refws.getWord(0)];
                if (sf.getType() == SFType.KEY) {
                    // map entry using key - returns a value
                    String  key = sf.getKey();
                    if (key == null)
                        mval = new Val(loc);    // return null value
                        //throw new ExEx("map key is null", loc);   // WANT TO RETURN NULL VALUE!
                    else
                        mval = tm.get(key);
                    if (mval == null)
                        mval = new Val(loc);    // return null value
                        //throw new ExEx("map entry is null", loc);   // WANT TO RETURN NULL VALUE!
                    mval.setDummyVar(false, loc);
                } else {
                    // map entry using index - returns a key
                    int index = sf.getLower();
                    if (sf.getType() != SFType.IMSUB)
                        throw new ExEx("map index has subscript range", loc);
                    Set<String> ks = tm.keySet();
                    Object[] ka = ks.toArray();
                    if ((index < 0) || (index >= ka.length))
                        throw new ExEx("map index is out of range (" + index + " >= " + ka.length + ")", loc);
                    mval = new Val((String)ka[index], loc);
                    mval.setDummyVar(false, loc);
                }
                sfl.incrementScanIndex();   // increment index as map index/key has been used
                sfl.trimToScanIndex();      // trim off key or index item from list
                
                // Break from the if() chain and execute the following code.
            } else if ((sf != null) && (pt == Ptype.CLASS) && (sfl.size() > (sfl.getScanIndex()))) {
                //
                // 3PL class variable.
                // Since this is a Var a Ref for it can be directly returned.
                if (sf.getType() != SFType.FIELD)
                    throw new ExEx("reference with subscript range for type \"class\" variable '" +
                            name + "'", loc);
                String  field = sf.getField();
                if (field == null)
                    throw new ExEx("class variable name is null", loc);
                Scope   sc = (Scope)val[refws.getWord(0)];
                TreeMap<String,Var> vars = sc.getVars();
                cvar = vars.get(field);
                if (cvar == null)
                    throw new ExEx("variable '" + field + "' not found in \"class\" variable '" + name + "'", loc);
                sfl.incrementScanIndex();   // increment index as class field has been used
                sfl.trimToScanIndex();      // trim off field item from list
                
                // Return the ref for the Var in the class.
                //return(new Ref(var, var.getType(), sfl, Flag.NONE, is_val, loc));
            } else if (type.isPrimitive()) {
                //
                // Primitive type, but has a subscript or field.
                //
                throw new ExEx("reference with subscript or field for primitive variable '" +
                        name + "'", loc);
            } else {
                //
                // Have a subscript or field for list or map but leave it
                // intact for Immediate/assignTo() to later get the list
                // or map entry using the last remaining SubField.
                //
                sfl.setScanIndex(old_index);    // have not extracted anything - restore the previous index
            }
        }

        if (mval != null) {
            // Have a map or list entry value.
            // Get a Ref for this and return it.
            WordSpec    mvalws = mval.getWordSpec();
            Var   mvar = mval.getVar(); // Var mvar is a dummy variable needed
                                        // to get a Ref for the Val -
                                        // see line 493
            return(mvar.getRef(mvalws, sfl, is_val, flag, loc));
        } else if (cvar != null) {
            return(cvar.getRef(null, sfl, is_val, loc));
        } else
            return(new Ref(this, type, sfl, flag, is_val, loc));
    }
    
    /**
     * Assignment to this variable.
     * @param   asstype  is the immediate assignment operator type.
     * @param   lref is the left variable reference.
     * @param   rval is the RHS value to be assigned.
     * @param   loc is a source file location for error messages
     */
    @SuppressWarnings({ "incomplete-switch", "unchecked" })
    public void assignTo (AST asstype, Ref lref, Val rval, SrcLoc loc) {
        // execute the assignment
        // LHS Ref WordSpec <- RHS Val value array
        if (readonly)
            throw new ExEx("cannot assign to '" + name + "' is read-only!", loc);
        if (lref == null)
            lref = getRef(null, new NodeList(null), loc);
        WordSpec    lws = lref.getWordSpec();
        int         lwords = lws.numWords();
        assigned = true;

        switch (lref.getPrimType()) {
        case MAP:
            TreeMap<String, Object> tm = (TreeMap<String, Object>)val[lws.getWord(0)];
            val[lws.getWord(0)] = mapAssign(asstype, tm, lref, rval, loc);
            return;
        case LIST:
            ArrayList<Object>   al = (ArrayList<Object>)val[lws.getWord(0)];
            val[lws.getWord(0)] = listAssign(asstype, al, lref, rval, loc);
            return;
        }

        // not list or map - prohibit target mode assignment except for case
        // of constant value mode -
        // If so, convert to immediate value.
        if (rval.getMode() == Mode.VALUE) {
            TDEVar  rtdev = rval.getTDEVar();
            if (!rtdev.isConst())
                throw new ExEx(asstype.assname() + " assignment of non-constant value mode variable", loc);
            rval =rtdev.getConstVal();
        }
        if (rval.getMode() != Mode.IMMEDIATE)
            throw new ExEx("immediate assignment from mode " + rval.getMode().name() + " - must be immediate mode", loc);

        if (asstype == AST.IMASS) {
            // simple immediate assignment
            int ri = 0; // RHS index - RHS value array is single contiguous
            for (int index = 0 ; index<lwords ; index++,ri++) {
                int     li = lws.getWord(index);  // LHS index
                Type    ltype = val_type[li];
                Type    rtype = rval.getValType(ri);
                Ptype   lptype = ltype.getPrimType();
                Ptype   rptype = rtype.getPrimType();
                
                if (rptype == Ptype.NULL) {
                    if (lwords == 1) {
                        if (lws.getPrimType(index) == Ptype.PTR)
                            val[li] = null;
                        else
                            throw new ExEx("assignment of null", loc);
                    }
                } else if (rptype == Ptype.NONE) {
                    // Trap on illegal null assignment - lwords > 1 or
                    // (dim_des != null) means null is from a missing
                    // compound constant entry which means assignment should
                    // be simply skipped.
                    if ((lwords == 1) && (rval.getWordSpec().getDimDes() == null))
                        throw new ExEx("assignment of null", loc);
                } else {
                    Object  o = rval.getVal(ri);
                    if ((lptype == Ptype.TYPE) && (rptype == Ptype.STR)) {
                        // special case: type = str
                        val[li] = new Type((String)o, false, rval.isArg(), loc);
                    } else if ((lptype == Ptype.STR) && (rptype == Ptype.TYPE)) {
                        // special case: str = type
                        val[li] = ((Type)o).getTypeString();
                    } else {
                        // all other assignments
                        if (o != null) {
                            if (lptype == Ptype.STR) {
                                switch (rptype) {
                                case UINT:
                                case INT:
                                    o = ((Long)o).toString();
                                    break;
                                case LOG:
                                    o = ((Boolean)o).toString();
                                    break;
                                case FLOAT:
                                    o = ((Double)o).toString();
                                    break;
                                case STR:
                                    break;
                                default:
                                    throw new ExEx("assignment types do not match", loc);
                                }
                            } else if (!val_type[li].isEqual(rval.getValType(ri), false) && 
                                       ((lptype != Ptype.INT) || (rptype != Ptype.UINT)))
                                throw new ExEx("assignment types do not match", loc);
                        }
                        if ((lptype == Ptype.FILE) && (val[li] != null)) {
                            // The type is FILE and it is currently open.
                            // Close it before assigning the new file.
                            try {
                                ((FileDesc)val[li]).close();
                            } catch (IOException e) {
                                throw new ExEx("close() error closing previous file " + ((FileDesc)val[li]).getPath(), loc);
                            }
                        }
                        val[li] = o;
                    }
                }
            }
        } else {
            // Other immediate assignment operators
            if (lwords != 1)
                throw new ExEx(asstype.assname() + " assignment to non-primitive", loc);
            int li = lws.getWord(0);  // LHS index
            Ptype   lpt = lws.getPrimType(0);
            val[li] = opAssign(asstype, val[li], lpt, rval, loc);
        }
    }
    
    /**
     * Pointer assignment to this pointer list variable specifically for varargs.
     * This is used only by body.inputParams() and
     * body.outputParams().
     * @param   i is the word index into this variable
     * @param   var is the RHS variable to be pointed to
     * @param   loc is a source file location for error messages
     */
    public void ptrAssignTo (int i, Var var, SrcLoc loc) {
        Ref         lref = getRef(null, new NodeList(null), loc);
        Ref         rref = var.getRef(null, new NodeList(null), loc);
        WordSpec    ws = lref.getWordSpec();
        int         li = ws.getWord(i);
        val[li] = rref;
        val_type[li] = Type.PTR;
    }
    
    /**
     * Key assignment to this string list variable specifically for varargs.
     * This is used only by body.inputParams() and
     * body.outputParams().
     * @param   i is the word index into this variable
     * @param   key is the RHS key string to be assigned
     * @param   loc is a source file location for error messages
     */
    public void keyAssignTo (int i, String key, SrcLoc loc) {
        Ref         lref = getRef(null, new NodeList(null), loc);
        WordSpec    ws = lref.getWordSpec();
        int         li = ws.getWord(i);
        val[li] = (key == null) ? "" : key;
        val_type[li] = Type.STR;
    }
    
    
    /**
     * Key/Var assignment to this map variable specifically for varargs.
     * This is used only by body.inputParams() and
     * body.outputParams().
     * @param   key is the RHS key string to be assigned
     * @param   var is the RHS pointer variable to be assigned
     * @param   loc is a source file location for error messages
     */
    public void mapAssignTo (String key, Var var, SrcLoc loc) {
        Ref     rref = var.getRef(null, new NodeList(null), loc);
        Val     v = new Val(null, rref, loc);
        @SuppressWarnings("unchecked")
        TreeMap<String,Val> tm = (TreeMap<String,Val>)val[0];
        if (key != null)
            tm.put(key, v);
    }

    @SuppressWarnings("unchecked")
    private static ArrayList<Object> listAssign (
        AST                 asstype,
        ArrayList<Object>   al,
        Ref                 lref,
        Val                 rval,
        SrcLoc              loc
    ) {
        switch (rval.getMode()) {
        case IMMEDIATE:
        case CLOCK:
        case RMEMORY:
        case CMEMORY:
            break;
        default: // May remove this restriction later.
                throw new ExEx("Disallowed target mode assigment to a list", loc);
        }
        boolean     rnull = (rval.getPrimType() == Ptype.NULL);
        switch (asstype) {
        case IMASS:
            // whole list assignment
            if (rnull) {
                // clear this list
                al.clear();
            } else {
                SubFieldList    sfl = lref.getSubFields();
                if (sfl.scanIndexAtEnd() && (rval.getPrimType() == Ptype.LIST)) {
                    // assign a list to this list
                    if (rval.getVal(0) == null)
                        throw new ExEx("Assignment of null value to list variable", loc);
                    if (rval.getVal(0) instanceof ArrayList<?>)
                        al = (ArrayList<Object>)rval.getVal(0);
                    else
                        throw new ExEx("Assignment of non-list value to list variable", loc);
                } else {
                    // insert into this list at a specific position
                    if (sfl.size() == 0)
                        throw new ExEx("assignment to list position has no index (subscript)", loc);
                    SubField    sf = sfl.getIndexedEntry(); // sf contains a key or a subscript entry which (list index)
                    if (sf.getType() == SFType.IMSUB) {
                        // sf contains a single subscript (list index)
                        int index = sf.getLower();
                        if ((index < 0) || (index > al.size()))
                            throw new ExEx("assignment to list position - index is out of range (" + index + " >= " + al.size() + ")", loc);
                        if (al.isEmpty())
                            throw new ExEx("assignment to list position - list is empty", loc);
                        rval.setDummyVar(false, loc);
                        al.set(index, rval);
                    } else {
                        // sf does not contain a subscript
                        throw new ExEx("assignment to list position does not have a single index (subscript)", loc);
                    }
                }
            }
            break;
        default:
            throw new ExEx(asstype.assname() + " to list", loc);
        }
        return(al);
    }

    @SuppressWarnings("unchecked")
    private static TreeMap<String, Object> mapAssign (
        AST                     asstype,
        TreeMap<String, Object> tm,
        Ref                     lref,
        Val                     rval,
        SrcLoc                  loc
    ) {
        switch (rval.getMode()) {
        case IMMEDIATE:
        case CLOCK:
        case RMEMORY:
        case CMEMORY:
            break;
        default: // May remove this restriction later.
                throw new ExEx("Disallowed target mode assigment to a map", loc);
        }
        boolean rnull = (rval.getPrimType() == Ptype.NULL);
        switch (asstype) {
        case IMASS:
            // whole map assignment
            if (rnull) {
                // clear this map
                tm.clear();
            } else {
                SubFieldList    sfl = lref.getSubFields();
                if (sfl.scanIndexAtEnd() && (rval.getPrimType() == Ptype.MAP)) {
                    // assign a map to this map
                    if (rval.getVal(0) == null)
                        throw new ExEx("Assignment of null value to map variable", loc);
                    if (rval.getVal(0) instanceof TreeMap<?, ?>)
                        tm = (TreeMap<String, Object>)rval.getVal(0);
                    else
                        throw new ExEx("Assignment of non-map value to map variable", loc);
                } else {
                    // insert into this map
                    if (sfl.isEmpty())
                        throw new ExEx("assignment of non-map to map", loc);
                    SubField    sf = sfl.getIndexedEntry(); // sf contains a key or a subscript entry (index)
                    if (sf.getType() == SFType.KEY) {
                        // sf contains a map key
                        String  key = sf.getKey();
                        if (key == null)
                            throw new ExEx("map key is null", loc);
                        rval.setDummyVar(false, loc);
                        tm.put(key, rval);
                        
                    } else {
                        // sf contains a subscript
                        throw new ExEx("map reference has a subcript or subscript range, not a key", loc);
                    }
                }
            }
            break;
        default:
            throw new ExEx(asstype.assname() + " to map", loc);
        }
        return(tm);
    }
    
    /**
     * Execute an immediate non-simple assignment, i.e. +=, -= etc.
     * @param   asstype is the assignment type
     * @param   lo is the LHS previous value object
     * @param   lpt is the LHS primitive type
     * @param   rval is the RHS value object
     * @param   loc is the source file location
     * @return  the new LHS value object
     */
    @SuppressWarnings("incomplete-switch")
    public static Object opAssign (AST asstype, Object lo, Ptype lpt, Val rval, SrcLoc loc) {
        Ptype   rpt = rval.getValPType(0);
	if (rpt == Ptype.NULL)
            throw new ExEx(asstype.assname() + " assignment of null", loc);
        Object  ro = rval.getVal(0);
        switch (asstype) {
        case IMPLUSASS:
            if (((lpt == Ptype.INT) || (lpt == Ptype.UINT)) &&
                ((rpt == Ptype.INT) || (rpt == Ptype.UINT))) {
                return(Long.valueOf((Long)lo + (Long)ro));
            }
            if ((lpt == Ptype.FLOAT) && (rpt == Ptype.FLOAT)) {
                return(Double.valueOf((Double)lo + (Double)ro));
            }
            if ((lpt == Ptype.FLOAT) && ((rpt == Ptype.INT) || (rpt == Ptype.UINT))) {
                return(Double.valueOf((Double)lo + (Long)ro));
            }
            if (lpt == Ptype.STR) {
                if ((rpt == Ptype.STR) ||
                    (rpt == Ptype.INT) ||
                    (rpt == Ptype.UINT) ||
                    (rpt == Ptype.FLOAT)) {
                    return(new String((String)lo + String.valueOf(ro)));
                }
            }
            throw new ExEx(asstype.assname() + " assignment type error", loc);
        case IMMINASS:
            if (((lpt == Ptype.INT) || (lpt == Ptype.UINT)) &&
                ((rpt == Ptype.INT) || (rpt == Ptype.UINT))) {
                return(Long.valueOf((Long)lo - (Long)ro));
            }
            if ((lpt == Ptype.FLOAT) && (rpt == Ptype.FLOAT)) {
                return(Double.valueOf((Double)lo - (Double)ro));
            }
            if ((lpt == Ptype.FLOAT) && ((rpt == Ptype.INT) || (rpt == Ptype.UINT))) {
                return(Double.valueOf((Double)lo - (Long)ro));
            }
            throw new ExEx(asstype.assname() + " assignment type error", loc);
        case IMMULASS:
            if (((lpt == Ptype.INT) || (lpt == Ptype.UINT)) &&
                ((rpt == Ptype.INT) || (rpt == Ptype.UINT))) {
                return(Long.valueOf((Long)lo * (Long)ro));
            }
            if ((lpt == Ptype.FLOAT) && (rpt == Ptype.FLOAT)) {
                return(Double.valueOf((Double)lo * (Double)ro));
            }
            if ((lpt == Ptype.FLOAT) && ((rpt == Ptype.INT) || (rpt == Ptype.UINT))) {
                return(Double.valueOf((Double)lo * (Long)ro));
            }
            throw new ExEx(asstype.assname() + " assignment type error", loc);
        case IMDIVASS:
            if (((lpt == Ptype.INT) || (lpt == Ptype.UINT)) &&
                ((rpt == Ptype.INT) || (rpt == Ptype.UINT))) {
                return(Long.valueOf((Long)lo / (Long)ro));
            }
            if ((lpt == Ptype.FLOAT) && (rpt == Ptype.FLOAT)) {
                return(Double.valueOf((Double)lo / (Double)ro));
            }
            if ((lpt == Ptype.FLOAT) && ((rpt == Ptype.INT) || (rpt == Ptype.UINT))) {
                return(Double.valueOf((Double)lo / (Long)ro));
            }
            throw new ExEx(asstype.assname() + " assignment type error", loc);
        case IMREMASS:
            if (((lpt == Ptype.INT) || (lpt == Ptype.UINT)) &&
                ((rpt == Ptype.INT) || (rpt == Ptype.UINT))) {
                return(Long.valueOf((Long)lo % (Long)ro));
            }
            if ((lpt == Ptype.FLOAT) && ((rpt == Ptype.INT) || (rpt == Ptype.UINT))) {
                return(Double.valueOf((Double)lo % (Long)ro));
            }
            throw new ExEx(asstype.assname() + " assignment type error", loc);
        case IMBANDASS:
            if (((lpt == Ptype.INT) || (lpt == Ptype.UINT) || (lpt == Ptype.BITS)) &&
                ((rpt == Ptype.INT) || (rpt == Ptype.UINT) || (rpt == Ptype.BITS))) {
                return(Long.valueOf((Long)lo & (Long)ro));
            }
            throw new ExEx(asstype.assname() + " assignment type error", loc);
        case IMBORASS:
            if (((lpt == Ptype.INT) || (lpt == Ptype.UINT) || (lpt == Ptype.BITS)) &&
                ((rpt == Ptype.INT) || (rpt == Ptype.UINT) || (rpt == Ptype.BITS))) {
                return(Long.valueOf((Long)lo | (Long)ro));
            }
            throw new ExEx(asstype.assname() + " assignment type error", loc);
        case IMBXORASS:
            if (((lpt == Ptype.INT) || (lpt == Ptype.UINT) || (lpt == Ptype.BITS)) &&
                ((rpt == Ptype.INT) || (rpt == Ptype.UINT) || (rpt == Ptype.BITS))) {
                return(Long.valueOf((Long)lo ^ (Long)ro));
            }
            throw new ExEx(asstype.assname() + " assignment type error", loc);
        case IMLANDASS:
            if ((lpt == Ptype.LOG) && (rpt == Ptype.LOG)) {
                return(Boolean.valueOf((Boolean)lo && (Boolean)ro));
            }
            throw new ExEx(asstype.assname() + " assignment type error", loc);
        case IMLORASS:
            if ((lpt == Ptype.LOG) && (rpt == Ptype.LOG)) {
                return(Boolean.valueOf((Boolean)lo || (Boolean)ro));
            }
            throw new ExEx(asstype.assname() + " assignment type error", loc);
        case IMLXORASS:
            if ((lpt == Ptype.LOG) && (rpt == Ptype.LOG)) {
                return(Boolean.valueOf((Boolean)lo ^ (Boolean)ro));
            }
            throw new ExEx(asstype.assname() + " assignment type error", loc);
        case IMLSASS:
            if (((lpt == Ptype.INT) || (lpt == Ptype.UINT) || (lpt == Ptype.BITS)) &&
                ((rpt == Ptype.INT) || (rpt == Ptype.UINT))) {
                return(Long.valueOf((Long)lo << (Long)ro));
            }
            throw new ExEx(asstype.assname() + " assignment type error", loc);
        case IMRSASS:
            if (((lpt == Ptype.INT) || (lpt == Ptype.UINT) || (lpt == Ptype.BITS)) &&
                ((rpt == Ptype.INT) || (rpt == Ptype.UINT))) {
                return(Long.valueOf((Long)lo >> (Long)ro));
            }
            throw new ExEx(asstype.assname() + " assignment type error", loc);
        }
        return(null);   // to keep compiler happy - should never get here!
    }
}
