package threepl.nodes;

import java.util.ArrayList;
import java.util.TreeMap;

import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.EnumConst;
import threepl.exec.Field;
import threepl.exec.Function;
import threepl.exec.Module;
import threepl.exec.Procedure;
import threepl.exec.Ref;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.funcs.InbuiltFunc;
import threepl.mods.InbuiltMod;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;
import threepl.parser.Token;
import threepl.procs.InbuiltProc;

/**
 * This implements a compound value node.
 * A compound value is a nested construction of expressions
 * used to initialise a compound type. It can also be used
 * to assign multiple constant values to a compound type.
 * In the source code this might appear as {{1, 2}, {3, 4}}
 * to initialise or assign to a 2x2 integer array.
 *
 * <p>The getVal() method for this node class has its own signature
 * since it differs from other expression nodes. To generate a
 * compound value we need a compound type. The type must be
 * extracted from the context -
 * <ul>
 * <li> For an assignment statement the LH variable type is used.
 * <li> For the initialisation argument in the var() procedure the type
 *      argument is used.
 * <li> For arguments in all other function or procedure
 *      arguments the type of the matching parameter must be used.
 * </ul>
 */
public final class CompoundValNode extends Node implements Constant {
    protected String    key;

    private static Val          dummyval;
    
    private class KeyField {
        protected String    key;
        protected Object    value; 
    }

    /**
     * Construct an empty compound constant value node.
     * @param   t a token from which the file source location is extracted
     */
    public CompoundValNode (Token t) {
        super(t);
        key = null;
    }
    
    /**
     * Set the parameter identifier string (key) for a parameter/argument
     * match.
     * @param  p is the parameter identifier
     */
    public void setParamKey (String p) {
        key = p;
    }
       
    /**
     * Get the parameter identifier string (key) for a parameter/argument
     * match.
     * @return  the parameter identifier
     */
    public String getParamKey () {
        return(key);
    }
    
    /**
     * Get the dimensional description.
     * Called from -
     * <ul>
     * <li> exec/Body.inputParams()
     * <li> procs/VarProc.execute()
     * </ul>
     * @return  the dimensional description
     */
    public int[] getDimDes () {
        int[]   dims = new int[100];
        int     maxindex;
        for (int i=0 ; i<100 ; i++)
            dims[i] = 0;
        maxindex = getDimDesList(dims, 0);
        int[]   rdims = new int[maxindex+1];
        for (int i=0 ; i<=maxindex ; i++)
            rdims[i] = dims[i];
        return(rdims);
    }
    
    private int getDimDesList (int[] dims, int index) {
        int dim = 0;
        int maxindex = index;
        for (Node n: subnodes) {
            if (n != null) {
                if (n instanceof CompoundValNode) {
                    // a nested {...}
                    int i = ((CompoundValNode)n).getDimDesList(dims, index+1);
                    if (i > maxindex)
                        maxindex = i;
                } else if (n instanceof KeyMatchNode) {
                    // a key=value entry
                    KeyMatchNode    kmnode = (KeyMatchNode)n;
                    n = kmnode.getSubNode(0);
                    if (n instanceof CompoundValNode) {
                        // a nested {...}
                        int i = ((CompoundValNode)n).getDimDesList(dims, index+1);
                        if (i > maxindex)
                            maxindex = i;
                    }
                }
            }
            dim++;
        }
        if (dim > dims[index])
            dims[index] = dim;
        return(maxindex);
    }
    
    /**
     * Get a one-dimensional identifier list from a compound argument.
     * Any nested lists give a fatal error.
     * Null entries are detected and give a fatal error.
     * Called from -
     * <ul>
     * <li> exec/Body.java.getArgs()
     * <li> procs/BufferSizeProc.execute()
     * <li> procs/ClockProc.execute()
     * <li> procs/FileProc.execute()
     * <li> procs/FloatProc.execute()
     * <li> procs/IntProc.execute()
     * <li> procs/LogProc.execute()
     * <li> procs/QueueProc.execute()
     * <li> procs/StaticProc.execute()
     * <li> procs/StrProc.execute()
     * <li> procs/StructProc.execute()
     * <li> procs/TypeProc.execute()
     * <li> procs/ValueProc.execute()
     * <li> procs/VarProc.execute()
     * </ul>
     * @param   mess is a string to prepend to an error message
     * @return  the identifier list
     */
    public ArrayList<Ident> getList (String mess) {
        ArrayList<Ident>   list = new ArrayList<Ident>();
        for (Node n: subnodes) {
            if (n == null)
                throw new ExEx(mess + " - null entry in compound list", loc);
            if (n instanceof CompoundValNode)
                throw new ExEx(mess + " - cannot nest compound lists in this context", loc);
            if (n instanceof KeyMatchNode)
                throw new ExEx(mess + " - this compound list cannot have key=value entries", loc);
            list.add(n.getIdentifier(mess));
        }
        return(list);
    }
    
    /**
     * Get a one-dimensional value list from a compound argument.
     * Any nested lists give a fatal error.
     * Key-match entries give a fatal error.
     * Null entries are detected and give a fatal error.
     * Called from nodes/CaseNode.iterator().
     * @param   mess is a string to prepend to an error message
     * @return  the value list
     */
    public ArrayList<Val> getValList (String mess) {
        ArrayList<Val>   list = new ArrayList<Val>();
        for (Node n: subnodes) {
            if (n == null)
                throw new ExEx(mess + " - null entry in compound list", loc);
            if (n instanceof CompoundValNode)
                throw new ExEx(mess + " - cannot nest compound lists in this context", loc);
            if (n instanceof KeyMatchNode)
                throw new ExEx(mess + " - this compound list cannot have key=value entries", loc);
            list.add(n.getVal());
        }
        return(list);
    }
    
    /**
     * Get a one-dimensional reference list from a compound argument.
     * Any nested lists give a fatal error.
     * Key-match entries give a fatal error.
     * Null entries are detected and give a fatal error.
     * Called from procs/AttributesProc().
     * @param   mess is a string to prepend to an error message
     * @return  the value list
     */
    public ArrayList<Ref> getRefList (String mess) {
        ArrayList<Ref>   list = new ArrayList<Ref>();
        for (Node n: subnodes) {
            if (n == null)
                throw new ExEx(mess + " - null entry in compound list", loc);
            if (n instanceof CompoundValNode)
                throw new ExEx(mess + " - cannot nest compound lists in this context", loc);
            if (n instanceof KeyMatchNode)
                throw new ExEx(mess + " - this compound list cannot have key=value entries", loc);
            Ref ref = n.getRef(mess);
            if (ref.getSubFields().size() != 0)
                throw new ExEx(mess + " - variable in this compound list cannot have subscripts or fields", loc);
            list.add(ref);
        }
        return(list);
    }
    
    /**
     * Get a 1-dimensional compound constant from this node.
     * The element type must be all "str", "log" or "uint".
     * @return  the array Val
     */
    public Val getValArray () {
        // convert nested StructValNodes into nested ArrayLists,
        // evaluating each expression encountered.
        ArrayList<Object>   l = makeList();

        // assign the values in the nested ArrayLists into array 'val'
        Object[]    val = new Object[l.size()];
        Object      o1 = l.get(0);
        Ptype       ptype = typeCheck(o1);
                
        int i = 0;
        for (Object o: l) {
            if (!ptype.equals(typeCheck(o)))
                throw new ExEx("element of compound constant not all same type", loc);
            val[i++] = o;
        }
        
        return(new Val(val, ptype.name().toLowerCase(), loc));
    }
    
    /**
     * Return the primitive type of an object. Only
     * types String, Boolean and Long are allowed.
     * @param o is the object whose type is required
     * @return the primitive type
     */
    private Ptype typeCheck (Object o) {
        if (o instanceof String)
            return(Ptype.STR);
        else if (o instanceof Boolean)
            return(Ptype.LOG);
        else if (o instanceof Long)
            return(Ptype.UINT);
        else if (o instanceof ArrayList)
            throw new ExEx("cannot have nested compound type in this context", loc);
        else if (o instanceof KeyField)
            throw new ExEx("cannot have key=value within this compound constant", loc);
        else
            throw new ExEx("constant type not allowed in this context", loc);
    }
    
    /**
     * ERROR - cannot use a general getVal() on a compound node
     * as it requires a type to make sense of the multidimensional data.
     */
    public Val getVal () {
        throw new ExEx("compound value in unexpected context", loc);
    }
    
    /**
     * Get the compound constant from this node as a <b>Val</b>.
     * Called from -
     * <ul>
     * <li> exec/Body.inputParams()
     * <li> nodes/ImAssNode.execute()
     * <li> nodes/TargAssNode.execute()
     * <li> procs/CramProc.execute()
     * <li> procs/ExternOutProc.execute()
     * <li> procs/InputProc.execute()
     * <li> procs/QueueProc.execute()
     * <li> procs/PriorityProc.execute()
     * <li> procs/RramProc.execute()
     * <li> procs/StaticProc.execute()
     * <li> procs/ValueProc.execute()
     * <li> procs/VarProc.execute()
     * </ul>
     * @param   type is the <b>Type</b> the compound constant must conform to
     * @return  the Val containing the value array
     */
    public Val getVal (Type type) {
        if (type.getPrimType() == Ptype.LIST)
            return(getListList());
        if (type.getPrimType() == Ptype.MAP)
            return(getMapList());
        
        int[]   dimdes = getDimDes();
        type.matchArray(dimdes, 0, null, "compound value ", loc);
        
        int         n = type.numWords();
        if (n <= 0)
            throw new ExEx("compound value type array has zero length", loc);
        Object[]    val = new Object[n];
        Type[]      val_type = new Type[n];
        
        // Fill type array with Ptype.NONE so these entries can be
        // ignored if not filled.
        for (int i=0 ; i<n ; i++)
            val_type[i] = Type.NONE;

        // Convert nested StructValNodes into nested ArrayLists,
        // evaluating each expression encountered.
        ArrayList<Object>   l = makeList();

        // Assign the values in the nested ArrayLists into array 'val'.
        dummyval = new Val((TDEVar)null, loc);
        ccAssign(val, val_type, type, 0, l, loc);
        
        Val v = new Val(val, val_type, type.getWordSpec(null, loc), null, null, loc);
        v.addExecSets(dummyval);
        v.addOVars(dummyval);
        v.andSetQueues(dummyval, loc);
        return(v);
    }
    
    /**
     * Get a one-dimensional list initialiser list from a compound argument.
     * Key-match entries give a fatal error.
     * Any nested lists give a fatal error.
     * Null entries are detected and give a fatal error.
     * @return  a value of type "list"
     */
    private Val getListList () {
        ArrayList<Val> al = new ArrayList<Val>();
        for (Node n: subnodes) {
            if (n == null)
                throw new ExEx("null entry in list initialiser list", loc);
            if (n instanceof CompoundValNode)
                throw new ExEx("cannot nest list initialiser list", loc);
            if (n instanceof KeyMatchNode)
                throw new ExEx("list initialiser entry is key=value", loc);            
            Val v = n.getVal();
            v.setDummyVar(false, loc);
            al.add(v);
        }
        Type        type = new Type(Ptype.LIST, 0);
        Object[]    oa = new Object[1];
        Type[]      ta = new Type[1];
        oa[0] = al;
        ta[0] = type;
        Val     val = new Val(oa, ta, type.getWordSpec(null, loc), null, null, loc);
        return(val);
    }
    
    /**
     * Get a one-dimensional map initialiser list from a compound argument.
     * All entries must be Key-match.
     * Any nested lists give a fatal error.
     * Null entries are detected and give a fatal error.
     * @return  a value of type "map"
     */
    private Val getMapList () {
        TreeMap<String, Val> tm = new TreeMap<String, Val>();
        for (Node n: subnodes) {
            if (n == null)
                throw new ExEx("null entry in map initialiser list", loc);
            if (n instanceof CompoundValNode)
                throw new ExEx("cannot nest map initialiser list", loc);
            if (n instanceof KeyMatchNode) {
                KeyMatchNode    kmnode = (KeyMatchNode)n;
                String          key = kmnode.getParamKey();
                n = kmnode.getSubNode(0);
                if (n instanceof CompoundValNode)
                    throw new ExEx("cannot nest map initialiser lists", loc);
                if (tm.containsKey(key))
                    throw new ExEx("duplicated key in map initialiser list", loc);
                Val v = n.getVal();
                v.setDummyVar(false, loc);
                tm.put(key, v);
            } else
                throw new ExEx("map initialiser entry is not key=value", loc);            
        }
        Type        type = new Type(Ptype.MAP, 0);
        Object[]    oa = new Object[1];
        Type[]      ta = new Type[1];
        oa[0] = tm;
        ta[0] = type;
        Val     val = new Val(oa, ta, type.getWordSpec(null, loc), null, null, loc);
        return(val);
    }
    
    private ArrayList<Object> makeList () {
        ArrayList<Object>   list = new ArrayList<Object>();
        for (Node n: subnodes) {
            if (n == null) {
                list.add(null);
                continue;
            }
            if (n instanceof CompoundValNode) {
                // a nested {...}
                list.add(((CompoundValNode)n).makeList());
            } else if (n instanceof KeyMatchNode) {
                // key=value
                KeyMatchNode    kmnode = (KeyMatchNode)n;
                KeyField        kf = new KeyField();
                kf.key = kmnode.getParamKey();
                n = kmnode.getSubNode(0);
                if (n instanceof CompoundValNode)
                    kf.value = ((CompoundValNode)n).makeList();
                else {
                    Val v = n.getVal();
                    v.setDummyVar(false, loc);
                    kf.value = v;
                }
                list.add(kf);
            } else {
                // a single value
                list.add(listValue(n));
            }
        }
        return(list);
    }
    
    private Object listValue (Node n) {
        Val v = n.getVal();
        if (v.isTarget())
            return(v);
            //throw new ExEx("compound value component cannot be target mode", loc);
        if (!v.isPrimitive())
            throw new ExEx("compound value component not single value", loc);
        switch (v.getPrimType()) {
        case BITS:
        case UINT:
        case INT:
            return(Long.valueOf(v.getSingleIval(loc)));
        case ENUM:
            return(v.getSingleEval(loc));
        case FIXED:
            throw new ExEx("fixed point compound value component not yet implemented", loc);
        case FLOAT:
            return(Double.valueOf(v.getSingleFval(loc)));
        case LOG:
            return(Boolean.valueOf(v.getSingleLval(loc)));
        case STR:
            return(v.getSingleSval(loc));
        case PTR:
            switch(v.getMPFCode()) {
            case IMPTR:
            case UMPTR:
            case IPPTR:
            case UPPTR:
                return(v.getModProcPtrObject(loc));
            case UCPTR:
            case IFPTR:
            case UFPTR:
                return(v.getClassFuncPtrObject(loc));
            case NONE:
                return(v.getSinglePval(loc));
            default:
                throw new ExEx("unknown MPFCode for PTR", loc);
            }
        case TYPE:
            return(v.getSingleTval(loc));
        default:
            throw new ExEx("compound value component not allowed type", loc);
        }
    }
    
    // Assign constant values from 'o' into the Object[] 'val' using
    // compound Type type. 'o' is either nested ArrayList matching
    // the nested types, or it is a final primitive value (Integer,
    // Boolean or String). 'index' is the insertion point in 'val'
    // which is advanced during recursive calls.
    // 'loc' is a source file location for error messages.
    @SuppressWarnings("unchecked")
    private void ccAssign (
        Object[]    val,
        Type[]      val_type,
        Type        type,
        int         index,
        Object      o,
        SrcLoc      loc
    ) {
        Type                array_type = type.getArrayType();
        ArrayList<Field>    fl = type.getFieldIndex();

        if (o == null)
            return;     // empty entry
        if (array_type != null) {
            if (!(o instanceof ArrayList))
                throw new ExEx("compound value array mismatch", loc);
            ArrayList<Object>   l = (ArrayList<Object>)o;
            int                 dim = type.getArrayDim();
            int                 size = array_type.numWords();
            if (l.size() > dim)
                throw new ExEx("compound value array dimension too large ("
                                + dim + "/" + l.size() + ")", loc);
            for (int i=0 ; i<dim ; i++)
                if (i < l.size())
                    ccAssign(val, val_type, array_type, index+size*i, l.get(i), loc);                  
        } else if (fl != null) {
            if (!(o instanceof ArrayList))
                throw new ExEx("compound value array mismatch", loc);
            ArrayList<Object>   l = (ArrayList<Object>)o;
            if (l.size() > fl.size())
                throw new ExEx("compound value struct dimension too large ("
                                + fl.size() + "/" + l.size() + ")", loc);
            int         offset = 0;
            int         i = 0;
            boolean     km = false;
            for (Field f: fl) {
                if (i >= l.size())
                    break;
                Type    t = f.getType();
                Object  oo = l.get(i++);
                if (oo instanceof KeyField) {
                    String  key = ((KeyField)oo).key;
                    Object  kf = ((KeyField)oo).value;
                    offset = type.getFieldOffset(key, "compound value ", loc);
                    t = type.getFieldType(key, "compound value ", loc);
                    ccAssign(val, val_type, t, index+offset, kf, loc);
                    km = true;
                } else {
                    if (km)
                        throw new ExEx("compound value struct keymatch follows position match", loc);
                    ccAssign(val, val_type, t, index+offset, oo, loc);                  
                    offset += t.numWords();
                }
            }            
        } else {
            if (o instanceof KeyMatchNode)
                throw new ExEx("compound value - keymatch where primitive expected", loc);
            if (!type.isPrimitive())
                throw new ExEx("compound value type mismatch - type not primitive", loc);
            if (o instanceof Val) {
                if (!type.hasTargetType() && (type.getPrimType() != Ptype.LOG))
                    throw new ExEx("compound value mode mismatch - target value in immediate type", loc);
                Val v = (Val)o;
                v.checkMatch(type, false, "compound value", loc);
                dummyval.addExecSets(v);
                dummyval.addOVars(v);
                dummyval.andSetQueues(v, loc);
                val[index] = v.getTDEVar();
            } else {
                Double  d;
                switch (type.getPrimType()) {
                case UINT:
                    if (!(o instanceof Long))
                        throw new ExEx("compound value type mismatch - integer expected", loc);
                    if (((Long)o).longValue() < 0)
                        throw new ExEx("compound value -ve value for uint", loc);
                    if (type.hasTargetType())
                        val[index] = new TDEVar(o, type, loc);
                    else
                        val[index] = o;
                    break;
                case ENUM:
                    if (!(o instanceof EnumConst))
                        throw new ExEx("compound value type mismatch - enum expected", loc);
                    EnumConst   ec = (EnumConst)o;
                    if (!ec.type.isEqual(type, false))
                        throw new ExEx("compound value type mismatch", loc);
                    if (type.hasTargetType())
                        val[index] = new TDEVar(o, type, loc);
                    else
                        val[index] = Long.valueOf(ec.val);
                    break;
                case BITS:
                case INT:
                    if (!(o instanceof Long))
                        throw new ExEx("compound value type mismatch - integer expected", loc);
                    if (type.hasTargetType())
                        val[index] = new TDEVar(o, type, loc);
                    else
                        val[index] = o;
                    break;
                case UFIXED:
                    if (o instanceof Long)
                        d = ((Long)o).doubleValue();
                    else if (o instanceof Double)
                        d = (Double)o;
                    else
                        throw new ExEx("compound value type mismatch - integer or float expected", loc);
                    if (d < 0.0)
                        throw new ExEx("compound value type mismatch - -ve value for ufixed", loc);
                    if (type.hasTargetType()) {
                        int w = type.getWidth();
                        int offs = type.getFixOffset();
                        val[index] = new TDEVar(d, type.getPrimType(), w, offs, 0, 0, loc);
                    } else
                        val[index] = d;                   
                    break;
                case FIXED:
                    if (o instanceof Long)
                        d = ((Long)o).doubleValue();
                    else if (o instanceof Double)
                        d = (Double)o;
                    else
                        throw new ExEx("compound value type mismatch - integer or float expected", loc);
                    if (type.hasTargetType()) {
                        int w = type.getWidth();
                        int offs = type.getFixOffset();
                        val[index] = new TDEVar(d, type.getPrimType(), w, offs, 0, 0, loc);
                    } else
                        val[index] = d;                   
                    break;
                case FLOAT:
                    if (o instanceof Long)
                        d = ((Long)o).doubleValue();
                    else if (!(o instanceof Double))
                        throw new ExEx("compound value type mismatch - floating point type expected", loc);
                    else
                        d = (Double)o;
                    if (type.hasTargetType()) {
                        int mw = type.getMantissaWidth();
                        int ew = type.getExponentWidth();
                        int w = type.getWidth();
                        val[index] = new TDEVar(d, type.getPrimType(), w, 0, mw, ew, loc);
                    } else
                        val[index] = d;
                    break;
                case LOG:
                    if (!(o instanceof Boolean))
                        throw new ExEx("compound value type mismatch - boolean expected", loc);
                    if (type.hasTargetType())
                        val[index] = new TDEVar(o, type, loc);
                    else
                        val[index] = o;
                    break;
                case STR:
                    if (!(o instanceof String)) {
                        if ((o instanceof Long) || (o instanceof Boolean) || (o instanceof Double)) {
                            // special case - convert to string!
                            val[index] = String.valueOf(o);
                        } else
                            throw new ExEx("compound value type mismatch - string expected", loc);
                    } else
                        val[index] = o;
                    break;
                case PTR:
                    if (!(  (o instanceof Ref) ||
                            (o instanceof InbuiltMod) ||
                            (o instanceof Module) ||
                            (o instanceof InbuiltProc) ||
                            (o instanceof Procedure) ||
                            (o instanceof InbuiltFunc) ||
                            (o instanceof Function)        ))
                        throw new ExEx("compound value type mismatch - pointer expected", loc);
                    val[index] = o;
                    break;
                case TYPE:
                    val[index] = o;
                    break;
                default:
                    throw new ExEx("compound value component not allowed type", loc);
                }
            }            
            val_type[index] = type;
        }
    }
}
