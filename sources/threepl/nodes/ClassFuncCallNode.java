package threepl.nodes;

import static threepl.ThreePL.isparsing;
import static threepl.ThreePL.pushClassBody;
import static threepl.ThreePL.popClassBody;
import static threepl.ThreePL.pushCallName;
import static threepl.ThreePL.popCallName;

import threepl.ThreePL;
import threepl.exceptions.ExEx;
import threepl.exec.Group;
import threepl.exec.Function;
import threepl.exec.Ref;
import threepl.exec.Scope;
import threepl.exec.SubFieldList;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.funcs.InbuiltFunc;
import threepl.funcs.InbuiltFuncs;
import threepl.parser.Constant;

/**
 * This node class implements a Group (3PL class) or Function call.
 */
public final class ClassFuncCallNode extends Node implements Constant {
    protected Node          name_node;
    protected String        cname = null;       // identifier if class function
    protected NodeList      subfields;
    protected boolean       indirect;
    protected boolean       global;
    protected boolean       isgroupcall = false;
    protected InbuiltFunc   fi = null;
    protected Function      fu = null;
    protected Group         gu = null;
    protected String        name;
    protected Flag          flag;       // may have a ++, --
    /**
     * Construct a function call code tree node.
     * @param   n is the function identifier node.
     *          If it is a VarNode and has SubFields it is a class function call,
     *          the last SubField is the function identifier and the
     *          remainder of the SubFieldList appended to the variable supplies
     *          the class (Scope).
     *          Otherwise the VarNode simply supplies the function identifier
     *          'n' also provides the source file location of the definition
     */
    public ClassFuncCallNode (Node n) {
        super(n.getSrcLoc());
        name_node = n;
        if (n instanceof VarNode) {
            ListNode    ln = (ListNode)n.getSubNode(0);
            NodeList    nl = ln.getNodeList();
            SubFieldList    sfl = new SubFieldList(nl, loc);
            // If there is a subscript/field list then this must be a class function call.
            // The first field is the class function name. Remove it from the list.
            if (!sfl.isEmpty()) {
                cname = sfl.getField(sfl.size()-1);
                nl.remove(nl.size()-1);
            }
        }
        indirect = false;
        global = false;
        fi = null;
        fu = null;
        flag =n.getFlag();
    }
    
    /**
     * Set the pre/post increment/decrement flag for the variable pointed to.
     * @param   f is one of the codes Flag.NONE, Flag.PREINCR, Flag.PREDECR,
     *          Flag.POSTINCR or Flag.POSTDECR
     */
    public void setFlag (Flag f) { flag = f; }
    
    /**
     * Get the flag code for this function call occurrence.
     * @return  the flag code 
     */
    public Flag getFlag () { return(flag); }
        
    /**
     * Set the subscript/field node list.
     * @param  sf is the subscript/field node list
     */
    public void setSubFields (NodeList sf) { subfields = sf; }
    
    /**
     * Call an inbuilt function and get the return value.
     * @return  the value resulting from the function call
     */
    public Val getVal () {
        NodeList    inl = new NodeList(loc);    // default empty in node list
        NodeList    onl = new NodeList(loc);    // default empty out node list
        NodeList    nl;
        ListNode    iln = (ListNode)subnodes.get(0);
        ListNode    oln = (ListNode)subnodes.get(1);
        if ((nl = iln.getNodeList()) != null)
            inl = nl;
        Val             ret_val;
        SubFieldList    sfl1, sfl2, sfl3;
                
        resolve();

        if (fi != null) {
            //
            // An inbuilt function.
            //
            for (Node n: inl) {
                if (n == null)
                    throw new ExEx("function '" + name + "' has a null argument", loc);
            }
            // Sort the nodes in an inbuilt function input NodeList when keymatch
            // arguments may be present.
            inl.sortKeys("procedure", fi.ipnames, fi.allowed_attributes, name, fi.check_null_args, true);
            pushCallName(name);
            ret_val = fi.getVal(inl);
            popCallName();
        } else if (fu != null) {
            //
            // A user-defined function.
            //
            if (isparsing)
                throw new ExEx("cannot call user function during parsing", loc);
            if ((oln.getNodeList()) != null)
                throw new ExEx("function call can not have output arguments", loc);
            pushCallName(null);
            ret_val = fu.getVal(inl);
            popCallName();
        } else if (gu != null) {
            //
            // A group (3PL class).
            //
            if ((nl = oln.getNodeList()) != null)
                onl = nl;
            pushCallName(name);
            ret_val = gu.getVal(onl, inl);
            popClassBody();    // is a class function call
            popCallName();
        } else
            throw new ExEx("SYSTEM ERROR - FuncCallNode.getVal()", loc);

        
        if (!subfields.isEmpty()) {
            //
            // Function call has subscripts or fields.
            //
            Var var = ret_val.getVar();
            if ((var == null) || (var.getType().numWords() == 1))
                throw new ExEx("function call has subscripts/fields on single value", loc);
            sfl1 = ret_val.getSubFields();
            sfl2 = new SubFieldList(subfields, loc);
            if (sfl1 != null)
                sfl3 = sfl1.merge(var, sfl2);
            else
                sfl3 = sfl2;
            return(var.getVal(null, sfl3, loc));
        } else
            return(ret_val);
        
    }
    
    /**
     * Call a function and get a returned variable reference.
     * Note that only two inbuilt functions, nullarg() and get(), do this!
     * In the case of nullarg() this is called to evaluate a nullarg() call
     * as a module or procedure output argument.
     * @return  the reference resulting from the function call or null
     */
    public Ref getRef () {
        NodeList    inl = new NodeList(loc);    // default empty in node list
        NodeList    nl;
        ListNode    iln = (ListNode)subnodes.get(0);
        ListNode    oln = (ListNode)subnodes.get(1);
        if ((nl = iln.getNodeList()) != null)
            inl = nl;
        Val         val = null;
        
        if (oln.getNodeList() != null)
            throw new ExEx("function call can not have output arguments", loc);

        resolve();
        
        if ((fi != null) && (name.equals("nullarg") || name.equals("arglist"))) {
            checkNoKeys(inl);
            return(fi.getRef(inl));
        }
        
        //
        // Is it an inbuilt function?
        //
        if (fi != null) {
            // Sort the nodes in an inbuilt function input NodeLists when keymatch
            // arguments may be present.
            inl.sortKeys("procedure", fi.ipnames, fi.allowed_attributes, name, fi.check_null_args, true);
            val = fi.getVal(inl);
        }
        
        //
        // Is it a group (3PL class)?
        //
        if (gu != null)
            throw new ExEx("class call cannot be on LHS of assignment", loc);
        
        //
        // is it a user-defined function?
        //
        if (fu != null)
            val = fu.getVal(inl);

        Var var = val.getVar();
        if (var == null)
            return(null);
            //throw new ExEx("LHS or output argument function call does not return a variable", loc);

        SubFieldList    sfl1, sfl2, sfl3;
        sfl1 = val.getSubFields();
        sfl2 = new SubFieldList(subfields, loc);
        if (sfl1 != null)
            sfl3 = sfl1.merge(var, sfl2);
        else
            sfl3 = sfl2;

        Ref ref = var.getRef(null, sfl3, false, loc);
        if (cname != null)
            popClassBody();    // is a class function call
        return(ref);
    }
    
    public Ref getRef (String mess) { return(getRef()); }

    //
    // Resolve the inbuilt/user function or class by its identifier.
    //
    private void resolve () {
        
        if (cname != null) {
            //
            // Is a call to a class function.
            //
            VarNode     cvn = (VarNode)name_node;
            if (cvn.getFlag() != Flag.NONE)
                throw new ExEx("cannot use ++ or -- on a function call", loc);
            Val         v = cvn.getVal();
            Scope       class_scope = v.getSingleScopeval(loc);
            fu = class_scope.findFunc(cname);     // try to find defined class function
            if (fu == null)
                throw new ExEx("no class function '" + cname + "'", loc);
            
            // The Group (3PL class) containing the function.
            Group    grp = (Group)class_scope.getBody();    // grp is the Group in which the function is defined.
            grp.setScope(class_scope);  // NOTE: update the Group scope to that of the current instance!!!
            pushClassBody(grp);
            return;
        }
        
        if (name_node instanceof PtrValNode) {
            //
            // Direct call of a class, user-defined function or inbuilt function via a pointer variable.
            // name_node must be class VarNode.
            //
            Object  o = ((PtrValNode)name_node).getClassFuncPtrObject(loc);
            if (o instanceof Group) {
                gu = (Group)o;
                name = gu.getName();
                pushClassBody(gu);
            } else if (o instanceof InbuiltFunc) {
                fi = (InbuiltFunc)o;
                name = InbuiltFuncs.findFuncId(fi);
            } else if (o instanceof Function) {
                fu = (Function)o;
                name = fu.getName();
            } else
                throw new ExEx("FuncCallNode: system error", loc);
        } else {
            //
            // Direct call of a class, user-defined function or inbuilt function.
            // name_node must be class VarNode.
            //
            VarNode     vn = (VarNode)name_node;
            
            if (vn.getFlag() != Flag.NONE)
                throw new ExEx("cannot use ++ or -- on a function call", loc);
            
            name = vn.getId();
            Context context = vn.getScopeContext();
            if (context != Context.DEFAULT)
                throw new ExEx("function call '" + name + "' has illegal context " + context.contextname(), loc);
            
            if ((gu = ThreePL.findGroup(name)) != null) {       // try defined group (3PL class)
                pushClassBody(gu);
                return;
            }
            if ((fu = ThreePL.findFunc(name)) != null)          // try user-defined function
                return;
            if ((fi = ThreePL.findInbuiltFunc(name)) != null)   // try inbuilt function
                return;
            
            throw new ExEx("no function or class '" + name + "'", loc);
        }
    }
    
    /**
     * Check that this inbuilt function has no key-matched arguments.
     * @param   inargs is a list of input argument tree nodes
     */
    private void checkNoKeys (NodeList inargs) {
        for (Node n: inargs) {
            if (n instanceof KeyMatchNode)
                throw new ExEx("inbuilt function '" + name + "' has a keymatch argument", loc);
        }
    }
    
    /**
     * Get the identifier of the called function.
     * @return  the function identifier
     */
    public String getId () {
        return(name);
    }
}
