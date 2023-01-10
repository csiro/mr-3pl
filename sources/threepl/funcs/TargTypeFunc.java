package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to return the type of a variable as a target type.
 * The type is a string in the same format as is used for
 * type declarations. If the argument is immediate the type will be
 * converted to target type using widths derived from the immediate value.
 */
public class TargTypeFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the type string
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("targtype() must have single argument", loc);
        
        Val     val = args.getVal(0);
        if (val.getMode() == Mode.IMMEDIATE) {
            throw new ExEx("targtype() NOT YET FULLY IMPLEMENTED!", loc);
        } else
            return(new Val(val.getTypeString(), loc));
    }
}
