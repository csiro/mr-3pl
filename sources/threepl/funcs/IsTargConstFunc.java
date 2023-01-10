package threepl.funcs;

import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to test a value argument to see if it is a
 * constant.
 */
public class IsTargConstFunc extends InbuiltFunc implements Constant, TDEConstants {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  true if the argument is a target constant
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("istargconst() must have single argument", loc);
        Ref ref = args.getRef(0, "istargconst()");
        if (ref.getPrimType() == Ptype.NONE)
            throw new ExEx("istargconst() argument is not a primitive type", loc);
        if (ref.getMode() != Mode.VALUE)
            return(new Val(false, loc));
        Val     val = args.getVal(0);
        TDEVar  tdev = val.getTDEVar();
        if (tdev == null)
            return(new Val(false, loc));
        return(new Val(tdev.isConst(), loc));
    }
}
