package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Ref;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.nodes.Node;
import threepl.nodes.NodeList;
import threepl.nodes.VarNode;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to determine the number of words in a variable,
 * expression or type. This has 1 argument, the variable. The value
 * returned is type int.
 */
public class NumWordsFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the identifier string
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        Type    type;
        Node    n = args.getNode(0);
        Ref     ref = null;
        Val     val = null;

        if (args.size() != 1)
            throw new ExEx("numwords() must have 1 argument", loc);
     
        if (!(n instanceof VarNode)) {
            val = args.getVal(0, true);
            type = val.getType();
        } else {
            ref = args.getRef(0, "numwords()");
            type = ref.getType();
        }

        Ptype   ptype = type.getPrimType();
        
        if (ptype == Ptype.TYPE) {
            if (val == null)
                val = args.getVal(0, true);
            type = val.getSingleTval(loc);
        }
        
        return(new Val(type.numWords(), loc));
    }
}
