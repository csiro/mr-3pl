package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.nodes.Node;
import threepl.nodes.NodeList;
import threepl.nodes.VarNode;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to compare two types.
 */
public class TypeIsEqualFunc extends InbuiltFunc implements Constant {
    
    public TypeIsEqualFunc () {
        ipnames.put("t1", 0);
        ipnames.put("t2", 1);
        ipnames.put("strict", 2);
    }

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the type
     */
    public Val getVal (NodeList args) {
        SrcLoc loc = args.getCallLoc();

        if ((args.size() < 2) || (args.size() > 3))
            throw new ExEx("typeisequal() must have 2 or 3 arguments", loc);
        
        boolean strict = false;

        Node    n0 = args.getNode(0);
        Node    n1 = args.getNode(1);
        Var     var0 = null;
        Var     var1 = null;
        Val     val0 = null;
        Val     val1 = null;
        Type    type0;
        Type    type1;
        
        if (n0 instanceof VarNode) {
            var0 = args.getRef(0, "typeisequal() - ").getVar();
            type0 = var0.getType();
        } else {
            val0 = args.getVal(1);
            type0 = val0.getType();
        }
        if (n1 instanceof VarNode) {
            var1 = args.getRef(1, "typeisequal() - ").getVar();
            type1 = var1.getType();
        } else {
            val1 = args.getVal(1);
            type1 = val1.getType();
        }

        if (args.size() == 3) {
            Val     val3 = args.getVal(2);
            strict = val3.getSingleLval(loc);
        }
        
        Val     retval = new Val(type0.isEqual(type1, strict), loc);
        return(retval);
    }
}
