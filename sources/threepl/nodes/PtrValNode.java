package threepl.nodes;

import static threepl.ThreePL.findFunc;
import static threepl.ThreePL.findInbuiltFunc;
import static threepl.ThreePL.findInbuiltMod;
import static threepl.ThreePL.findGroup;
import static threepl.ThreePL.findMod;
import static threepl.ThreePL.findProc;
import static threepl.ThreePL.findVar;

import java.util.ArrayList;

import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.QueueRefs;
import threepl.exec.Ref;
import threepl.exec.SubFieldList;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.exec.Var.IDtype;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;
import threepl.parser.Token;

/**
 * This Node class represents a pointer dereference.
 */
public final class PtrValNode extends Node implements Constant {
    private boolean nullresolve;
    private Flag    flag;       // may have a ++, --
    private boolean direct;

    /**    
     * Construct a pointer dereference node.
     * @param   n is variable node or a function call node
     * @param   nodelist is a subscript or field list
     * @param   nullresolve indicates if a null pointer causes a fatal error
     *          or returns null
     * @param   t is a token, from which the file source location is extracted
     */
    public PtrValNode (Node n, NodeList nodelist, boolean nullresolve, Token t) {
        super(t);
        this.nullresolve = nullresolve;
        flag =n.getFlag();
        addSubNode(n);
        NodeList    nl = getSubNodes();
        // Add the NodeList as an entry in the
        // subnodes NodeList (rather than concatenating its contents
        // into the subnodes NodeList).
        nl.add(nodelist);
    }
    
    /**
     * Set the pre/post increment/decrement flag for the variable pointed to.
     * @param   f is one of the codes Flag.NONE, Flag.PREINCR, Flag.PREDECR,
     *          Flag.POSTINCR or Flag.POSTDECR
     */
    public void setFlag (Flag f) { flag = f; }
    
    /**
     * Get the direct execution flag.
     * @return  the direct execution flag
     */
    public boolean getDirect () { return(direct); }
    
    /**
     * Set the direct execution flag.
     */
    public void setDirect () { direct = true; }
    
    /**
     * Get the flag code for this variable occurrence
     * @return  the flag code 
     */
    public Flag getFlag () { return(flag); }

    /**
     * Get the value pointed to.
     * @return  the value pointed to (Var)
     */
    public Val getVal () {
        Var             var;
        SubFieldList    sfl1;
        SubFieldList    sfl2 = new SubFieldList(getListSubNode(1), loc);
        Node            n = subnodes.getNode(0);
        Val             val;
        if (n instanceof VarNode)
            val = ((VarNode)n).getValNoFlags();
        else
            val = n.getVal();
        if (val.getPrimType() == Ptype.STR) {
            String  s = val.getSingleSval(loc);
            var = findVar(new Ident(s), loc);
            if (var == null) {
                if (nullresolve)
                    return(null);
                else
                    throw new ExEx("string pointer '" + s + "' does not name a variable", loc);
            }
        } else {
            Ref ref = val.getSinglePval(loc);
            if (ref == null) {
                if (nullresolve)
                    return(null);
                else
                    throw new ExEx("attempt to get value from null pointer", loc);
            }
            var = ref.getVar();
            if (ref.getIndSubFields() != null)
                sfl1 = ref.getIndSubFields(); // A HACK! always correct?
            else
                sfl1 = ref.getSubFields();
            if (sfl1 != null)
                sfl2.addAll(0, sfl1);   // prepend sfl1 to sfl2
        }
        return(var.getVal(sfl2, flag, loc));
    }

    /**
     * Get the reference pointed to.
     * @param   mess is a string to be printed ahead of any error message
     * @return  the reference pointed to (Ref)
     */
    public Ref getRef (String mess) {
        Var             var;
        SubFieldList    sfl1;
        SubFieldList    sfl2 = new SubFieldList(getListSubNode(1), loc);
        Node            n = subnodes.getNode(0);
        Val             val;
        
        if (mess == null)
            mess = "";
        if (n instanceof VarNode)
            val = ((VarNode)n).getValNoFlags();
        else
            val = n.getVal();
        if (val.getPrimType() == Ptype.STR) {
            String  s = val.getSingleSval(loc);
            var = findVar(new Ident(s), loc);
            if (var == null)
                throw new ExEx(mess + " - attempt to dereference an invalid string pointer '" + s + "'", loc);
        } else {
            Ref ref = val.getSinglePval(loc);
            if (ref == null) {
                if (nullresolve)
                    return(null);
                else
                    throw new ExEx(mess + " - attempt to dereference a null pointer", loc);
            }
            var = ref.getVar();
            sfl1 = ref.getSubFields();
            if (sfl1 != null)
                sfl2.addAll(0, sfl1);   // prepend sfl1 to sfl2
        }
        if (var == null)
            throw new ExEx(mess + " - attempt to dereference a null pointer", loc);
        return(var.getRef(null, sfl2, false, loc));
    }
    
    /**
     * Get an identifier from this pointer dereference. If the pointer
     * variable is type "str" return the string value as an identifier.
     * If the pointer variable is type {@code '->'} return the identifier of
     * the variable pointed to. In either case no subscripts or fields
     * are allowed on the dereference. If the pointer is null or the
     * dereference has subscripts or fields, null is returned.
     * @return  the identifier or null
     */
    public Ident getIdentifier() {return(getIdentifier(false, null));}
    
    /**
     * Get an identifier from this pointer dereference. If the pointer
     * variable is type "str" return the string value as an identifier.
     * If the pointer variable is type {@code '->'} return the identifier of
     * the variable pointed to. In either case no subscripts or fields
     * are allowed on the dereference. If the pointer is null or the
     * dereference has subscripts or fields, throw an exception with an
     * error message.
     * @param   mess is a string to prepend to a fatal error message
     * @return  the identifier
     */
    public Ident getIdentifier(String mess) {return(getIdentifier(false, mess));}
    
    /**
     * Check that this node has no pre/post increment/decrement operators
     * and return the identifier. If the above checks are not met -
     * if 'mess' is null, return null.
     * if 'mess' is not null, throw an exception with an error message.
     * @param   allow_subs_fields_flags when true, skips the
     *          subscript/field/flags check
     * @param   mess is a string to prepend to a fatal error message
     * @return  the identifier
     */
    public Ident getIdentifier(boolean allow_subs_fields_flags, String mess) {
        Node    n = subnodes.getNode(0);
        Val     val = n.getVal();
        switch (val.getPrimType()) {
        case PTR:
            Ref ref = (Ref)val.getVal(0);
            if (!allow_subs_fields_flags && (ref.getSubFields().size() != 0)) {
                if (mess == null)
                    return(null);
                else
                    throw new ExEx(mess + ": attempt to get identifier from pointer with subscripts or fields", loc);
            }
            return(ref.getVar().getIdent());
        case STR:
            String  s = val.getSingleSval(loc);
            if (s.length() == 0) {
                if (mess == null)
                    return(null);
                else
                    throw new ExEx(mess + ": attempt to get identifier from null string pointer", loc);
            }
            // Should check here for invalid identifier string.
            // Should extract a context from the string if present.
            return(new Ident(s));
        default:
            if (mess == null)
                return(null);
            else
                throw new ExEx(mess + ": attempt to get dereferenced value from non-pointer", loc);
        }
    }
    
    /**
     * Get a module or procedure pointed to.
     * @param   loc is the source file location
     * @return  the module or procedure pointed to
     */
    public Object getModProcPtrObject(SrcLoc loc) {
        Node    n = subnodes.getNode(0);
        Val     val = n.getVal();
        Object  o;
        if (val.getPrimType() == Ptype.STR) {
            String  s = val.getSingleSval(loc);
            if ((o = findInbuiltMod(s)) != null)
                return(o);
            if ((o = findMod(s)) != null)
                return(o);
            if ((o = findInbuiltFunc(s)) != null)
                return(o);
            if ((o = findProc(s)) != null)
                return(o);
            throw new ExEx("string pointer '" + s + "' does not name a module or procedure", loc);
        }
        o = val.getModProcPtrObject(loc);
        if (o == null) {
            if (nullresolve)
                return(null);
            else
                throw new ExEx("attempt to get value from null pointer", loc);
        }
        return(o);
    }
    
    /**
     * Get a class or function pointed to.
     * @param   loc is the source file location
     * @return  the function pointed to
     */
    public Object getClassFuncPtrObject(SrcLoc loc) {
        Node    n = subnodes.getNode(0);
        Val     val = n.getVal();
        Object  o;
        if (val.getPrimType() == Ptype.STR) {
            String  s = val.getSingleSval(loc);
            if ((o = findGroup(s)) != null)
                return(o);
            if ((o = findInbuiltFunc(s)) != null)
                return(o);
            if ((o = findFunc(s)) != null)
                return(o);
            throw new ExEx("string pointer '" + s + "' does not name a class or function", loc);
        }
        o = val.getClassFuncPtrObject(loc);
        if (o == null) {
            if (nullresolve)
                return(null);
            else
                throw new ExEx("attempt to get value from null pointer", loc);
        }
        return(o);
    }

    /**
     * Get the reference pointed to or the identifier.
     * @param   mess is a string to be printed ahead of any error message
     *          (not used)
     * @return  the reference pointed to (Ref)
     */
    public Ref getRefOrIdent (String mess) {
        Var             var;
        SubFieldList    sfl1;
        SubFieldList    sfl2 = new SubFieldList(getListSubNode(1), loc);
        Node            n = subnodes.getNode(0);
        Val             val;
        if (n instanceof VarNode)
            val = ((VarNode)n).getValNoFlags();
        else
            val = n.getVal();
        if (val.getPrimType() == Ptype.STR) {
            String  s = val.getSingleSval(loc);
            var = findVar(new Ident(s), loc);
            if (var == null) {
                Ref ref = new Ref();
                ref.setId(new Ident(s));
                return(ref);
            }
        } else {
            Ref ref = val.getSinglePval(loc);
            if (ref == null) {
                if (nullresolve)
                    return(null);
                else
                    throw new ExEx("attempt to dereference a null pointer", loc);
            }
            var = ref.getVar();
            sfl1 = ref.getSubFields();
            if (sfl1 != null)
                sfl2.addAll(0, sfl1);   // prepend sfl1 to sfl2
        }
        if (var == null)
            throw new ExEx("attempt to dereference a null pointer", loc);
        return(var.getRef(null, sfl2, false, loc));
    }
    
    /**
     * Execute a variable appearing as a statement - {@code 'v->++}' or {@code 'v->--'}.
     * @param   esig is an exception/restart signal, or null
     * @param   startl is a list of start signals for target statements
     *          below this node
     * @param   finishl is a list of finish signals for target statements
     *          below this node
     * @param   queues returns all the queue availability signals
     *          accumulated from code below
     * @param   toplevel is true if this is the top level in a module
     * @param   availok is true if a previous sync makes a queue availability
     *          wait unnecessary
     * @param   checkedqueues gives queue reads which have already been checked
     * @param   pri_in is an optional input signal to a priority encoder
     * @param   pri_out is an optional output signal from a priority encoder
     * @return  EXECR execution status value
     */
    public EXECR execute (
        TDEVar              esig,
        ArrayList<TDEVar>   startl, 
        ArrayList<TDEVar>   finishl,
        QueueRefs            queues,
        boolean             toplevel,
        boolean             availok,
        QueueRefs            checkedqueues,
        TDEVar              pri_in,
        TDEVar              pri_out
    ) {
        Ref ref = getRef("");
        switch (ref.getMode()) {
        case IMMEDIATE:
            getVal();
            break;
        case STATIC:
            TargAssNode.createStaticIncrDecr (
                esig,
                startl, 
                finishl,
                queues,
                toplevel,
                availok,
                checkedqueues,
                pri_in,
                pri_out,
                this,
                ref,
                flag,
                loc
            );
            break;
        default:
            throw new ExEx("variable pointed to by'" + ref.getVar().getID(IDtype.CHAIN) +
                "' has ++ or -- but is not immediate or static", loc);
        }
        return(EXECR.NONE);
    }
}
