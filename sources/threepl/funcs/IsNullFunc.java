package threepl.funcs;

import threepl.codegen.TDEConstants;
import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to test a value argument to see if it is null.
 */
public class IsNullFunc extends InbuiltFunc implements Constant, TDEConstants {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  true if the argument is a target constant
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("isnull() must have single argument", loc);
        Val     val = args.getVal(0);
        if (val == null)
            return(new Val(true, loc));
        if (!val.isPrimitive())
            return(new Val(false, loc));
        Ptype   ptype = val.getPrimType();
        if (ptype == Ptype.PTR)
            return(new Val(val.getSinglePval(loc) == null, loc));
        return(new Val(val.getPrimType() == Ptype.NULL, loc));
    }
}
