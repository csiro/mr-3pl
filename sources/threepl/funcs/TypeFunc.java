package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Ref;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.nodes.Node;
import threepl.nodes.NodeList;
import threepl.nodes.VarNode;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to return the type of a variable or expression.
 */
public class TypeFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the type
     */
    public Val getVal (NodeList args) {
        SrcLoc loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("type() must have single argument", loc);

        Node    n = args.getNode(0);
        if (n instanceof VarNode) {
            Var var = ((VarNode)n).getVar();
            if (var == null)
                throw new ExEx("type() argument undefined variable '" + ((VarNode)n).getId() + "'", loc);        
            if (var.getType().getPrimType() == Ptype.EMPTY)
                return(new Val(new Type(Ptype.EMPTY, 0), loc));
            Ref ref = args.getRef(0, "type()");
            if ((ref.getPrimType() == Ptype.LIST) || (ref.getPrimType() == Ptype.MAP)) {
                Val val = args.getVal(0);
                return(new Val(val.getType(), loc));
            }
            return(new Val(ref.getType(), loc));
        } else {
            Val val = args.getVal(0);
            return(new Val(val.getType(), loc));
        }
    }
}
