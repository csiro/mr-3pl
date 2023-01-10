package threepl.nodes;

import java.util.ArrayList;
import java.util.TreeMap;

import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.QueueRefs;
import threepl.exec.Ref;
import threepl.exec.Scope;
import threepl.exec.SubFieldList;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.exec.Var.IDtype;
import threepl.parser.Constant;
import threepl.parser.Token;

/**
 * This Node class represents a group member dereference.
 */
public final class GroupRefNode extends Node implements Constant {
    private Flag    flag;       // may have a ++, --
    private boolean direct;
    
    /**    
     * Construct a group (3PL class) variable dereference node.
     * @param   n is a variable node or a function call node providing the scope (class)
     * @param   nr is a variable node designating the group field (group variable)
     * @param   nodelist is a subscript or field list for nr
     * @param   t is a token, from which the file source location is extracted
     */
    public GroupRefNode (Node n, Node nr, NodeList nodelist, Token t) {
        super(t);
        flag = nr.getFlag();
        addSubNode(n);          // scope (group)
        addSubNode(nr);         // field reference
        NodeList    nl = getSubNodes();
        // Add the NodeList as an entry in the
        // subnodes NodeList (rather than concatenating its contents
        // into the subnodes NodeList).
        nl.add(nodelist);       // subscripts/fields for field reference
    }
    
    /**
     * Get the value pointed to.
     * @return  the value pointed to (Val)
     */
    public Val getVal () {
        Var             var = null;
        SubFieldList    sfl2 = new SubFieldList(getListSubNode(2), loc);
        Node            n = subnodes.getNode(0);            // scope (class)
        Node            nr = subnodes.getNode(1);           // field reference
        Val             val;
        
        if (n instanceof VarNode)
            val = ((VarNode)n).getValNoFlags();
        else
            val = n.getVal();
        if (val.getPrimType() != Ptype.CLASS)
            throw new ExEx("variable with class operator is not type 'class'");
        // Get the scope from the first variable.
        Scope  s = val.getSingleScopeval(loc);
        // Get the variable id from the second variable.
        if (!(nr instanceof VarNode))
            throw new ExEx("TEMPORARY ERROR - GROUP FIELD NOT VARNODE");
        VarNode vn = (VarNode)nr;
        Ident   id = vn.getIdent();
        if (id.getScopeContext() != Context.DEFAULT)
            throw new ExEx("class variable reference is not default context");
        String  field = id.getId();
        TreeMap<String,Var> vars = s.getVars();
        if (vars.containsKey(field))
            var = vars.get(field);
        else
            throw new ExEx("class variable not found");
        
        return(var.getVal(sfl2, flag, loc));
    }

    /**
     * Get a reference to the value pointed to.
     * @param   mess is a string to be printed ahead of any error message
     * @return  the value pointed to (Var)
     */
    public Ref getRef (String mess) {
        Var             var = null;
        SubFieldList    sfl2 = new SubFieldList(getListSubNode(2), loc);
        Node            n = subnodes.getNode(0);
        Node            nr = subnodes.getNode(1);           // field reference
        Val             val = n.getVal();
        
        if (mess == null)
            mess = "";
        if (!(n instanceof VarNode))
            throw new ExEx(mess + " - class operator used on non-variable");
        if (val.getPrimType() != Ptype.CLASS)
            throw new ExEx(mess + " - variable with class operator is not type 'class'");
        // Get the scope from the first variable.
        Scope  s = val.getSingleScopeval(loc);
        // Get the variable id from the second variable.
        if (!(nr instanceof VarNode))
            throw new ExEx(mess + " - TEMPORARY ERROR - GROUP FIELD NO VARNODE");
        VarNode vn = (VarNode)nr;
        Ident   id = vn.getIdent();
        if (id.getScopeContext() != Context.DEFAULT)
            throw new ExEx(mess = " - class variable reference is not default context");
        String  field = id.getId();
        TreeMap<String,Var> vars = s.getVars();
        if (vars.containsKey(field))
            var = vars.get(field);
        else
            throw new ExEx(mess + " - class variable not found");
        
        return(var.getRef(null, sfl2, false, loc));
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
     * Get the flag code for this variable occurrence
     * @return  the flag code 
     */
    public Flag getFlag () { return(flag); }
    
    
    /**
     * Set the direct execution flag.
     */
    public void setDirect () { direct = true; }
    
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
