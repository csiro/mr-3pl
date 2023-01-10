package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.nodes.ClassFuncCallNode;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the identifier of a variable.
 * This has 1 argument, which is the variable.
 * NOT USED - WHY? IS IT THE SAME AS identifier() ?
 */
public class VarIdentFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the identifier of the variable
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        Var     var;

        if (args.size() != 1)
            throw new ExEx("varident() must have a single argument", loc);
        
        if (args.getNode(0) instanceof ClassFuncCallNode) {
            // functions such as getclock(), currentclock(), prevclock()
            Val val = args.getVal(0);
            var = val.getVar();
        } else {
            Ref ref = args.getRef(0, "varident()");
            var = ref.getVar();
        }
        
        if (var == null)
            throw new ExEx("varident() argument is not a variable", loc);
        
        return(new Val(var.getEname(), loc));
    }
}
