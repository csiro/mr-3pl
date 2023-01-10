package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.nodes.Node;
import threepl.nodes.NodeList;
import threepl.nodes.PtrValNode;
import threepl.nodes.VarNode;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to return the type of a variable or expression as
 * a string.
 * The type is a string in the same format as is used for type declarations.
 */
public class TypeStringFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the type string
     */
    public Val getVal (NodeList args) {
        SrcLoc loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("typestring() must have single argument", loc);

        Node    n = args.getNode(0);
        // Check if special case of "empty" since cannot use getInArg() if so
        // as that calls getVal() which will fail on type "empty".
        if (n instanceof VarNode) {
            Var var = ((VarNode)n).getVar();
            if (var == null)
                throw new ExEx("typestring() argument undefined variable '" + ((VarNode)n).getId() + "'", loc);        
            switch (var.getMode()) {
            case IMMEDIATE:
            case VALUE:
            case SELECTVALUE:
            case STATIC:
            case QUEUE:
            case PRIORITY:
                break;
            default:
                return(new Val("none", loc));
            }
            if (var.getType().getPrimType() == Ptype.EMPTY)
                return(new Val("empty", loc));
        }
        if (n instanceof PtrValNode) {
            Ref ref = args.getRef(0, "typestringfunc() ");
            return(new Val(ref.getTypeString(), loc));
        }
        Val val = args.getVal(0);
        return(new Val(val.getTypeString(), loc));
    }
}
