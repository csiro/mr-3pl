package threepl.nodes;

import threepl.ThreePL;
import threepl.exceptions.ExEx;
import threepl.exec.Function;
import threepl.exec.Group;
import threepl.exec.Module;
import threepl.exec.Procedure;
import threepl.exec.Ref;
import threepl.exec.Scope;
import threepl.exec.SubFieldList;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.funcs.InbuiltFunc;
import threepl.mods.InbuiltMod;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;
import threepl.parser.Token;
import threepl.procs.InbuiltProc;

/**
 * This Node class represents an address operation.
 */
public final class AddrNode extends Node implements Constant {
    Var var = null;    // direct variable reference for procedure calls constructed during execution
    
    /**    
     * Construct an address operation node.
     * @param   n is the argument node
     * @param   t is a token, from which the file source location is extracted
     */
    public AddrNode (Node n, Token t) {
        super(t);
        addSubNode(n);
    }
    
    /**
     * Construct an address node which points to a variable directly.
     * @param v is the variable
     */
    public AddrNode (Var v) {
        super((SrcLoc)null);
        var = v;
    }

    /**
     * Get the address of the argument.
     * @return  the value pointed to (Var)
     */
    public Val getVal () {
        if (var != null) {
            // Direct pointer to variable.
            Ref ref = var.getRef(null, null, loc);
            Val ival = new Val(ref, true, null);
            return(ival);
        }
        
        Node    n = subnodes.getNode(0);
        // NOT SURE WHY PtrValNode here -
        // Seems to be something like &var-> which does not look correct?
        if (!(n instanceof VarNode) /*&& !(n instanceof PtrValNode)*/)
            throw new ExEx("address operator has non-variable argument", loc);
        VarNode vn = (VarNode)n;
        Ref ref = null;
        try {
            // If fails, will get null or else an exception, caught below.
            ref = vn.getRefOrNull();
        } catch (ExEx e) {
            // Assume exception means that we have not found a variable
            // as the pointer operand.
            // Skip down to look for a class, module, procedure or function
            // as the operand.
            ref = null;
        }
        if (ref != null) {
            // Operand is a variable.
            Val val = new Val(ref, true, loc);
            val.setDummyVar(true, loc);
            return(val);
        }
        
        // See if the operand is a group(class), module, procedure or function.
        String      cname = null;
        Group       gu = null;  // user-defined group (class)
        InbuiltMod  mi = null;  // inbuilt module
        Module      mu = null;  // user-defined module
        InbuiltProc pi = null;  // inbuilt procedure
        Procedure   pu = null;  // user-defined procedure
        InbuiltFunc fi = null;  // inbuilt function
        Function    fu = null;  // user-defined function

        int     fields = vn.getNumListSubNodes(0);
        if (fields != 0) {
            ListNode        ln = (ListNode)vn.getSubNode(0);
            NodeList        nl = ln.getNodeList();
            SubFieldList    sfl = new SubFieldList(nl, loc);
            // If there is a subscript/field list then this must be a class module or procedure reference.
            // The VarNode gives the first Group (3PL Class) identifier.
            // The fields, except the last, give intermediate Groups (3PL Classes) and the
            // last field is the class module/procedure name. Remove it from the list.
            if (!sfl.isEmpty()) {
                cname = sfl.getField(sfl.size()-1);
                sfl.remove(sfl.size()-1);
            }
            Var     var = vn.getVar();
            new Type();
            Type    type = Type.CLASS;
            Ref     vref = new Ref(var, type, sfl, Flag.NONE, true, loc);
            
            Val     v = vref.getVal(loc);
            Scope   class_scope = v.getSingleScopeval(loc);
            if ((gu = class_scope.findGroup(cname)) != null)
                return(new Val(PtrType.UCPTR, gu, loc));
            if ((mu = class_scope.findMod(cname)) != null)
                return(new Val(PtrType.UMPTR, mu, loc));
            if ((pu = class_scope.findProc(cname)) != null)
                return(new Val(PtrType.UPPTR, pu, loc));
            if ((fu = class_scope.findFunc(cname)) != null)
                return(new Val(PtrType.UFPTR, fu, loc));
            throw new ExEx("no variable, class, module, procedure or function '" + cname + "' found", loc);
        }
        
        String  name = vn.getId();

        // is it a user-defined class?
        gu = ThreePL.findGroup(name);
        if (gu != null)
            return(new Val(PtrType.UCPTR, gu, loc));

        // is it an inbuilt module?
        mi = ThreePL.findInbuiltMod(name);
        if (mi != null)
            return(new Val(PtrType.IMPTR, mi, loc));

        // is it a user-defined module?
        mu = ThreePL.findMod(name);
        if (mu != null)
            return(new Val(PtrType.UMPTR, mu, loc));

        // is it an inbuilt procedure?
        pi = ThreePL.findInbuiltProc(name);
        if (pi != null)
            return(new Val(PtrType.IPPTR, pi, loc));
        
        // is it a user-defined procedure?
        pu = ThreePL.findProc(name);
        if (pu != null)
            return(new Val(PtrType.UPPTR, pu, loc));
        
        // is it an inbuilt function?
        fi = ThreePL.findInbuiltFunc(name);
        if (fi != null)
            return(new Val(PtrType.IFPTR, fi, loc));
        
        // is it a user-defined function?
        fu = ThreePL.findFunc(name);
        if (fu != null)
            return(new Val(PtrType.UFPTR, fu, loc));

        throw new ExEx("class, module, procedure or function '" + name + "' not found", loc);
    }
}
