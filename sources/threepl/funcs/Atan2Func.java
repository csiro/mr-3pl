package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the arc tangent of two arguments.
 */
public class Atan2Func extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the arctangent value
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        
        if (args.size() != 2)
            throw new ExEx("atan2() - must have two arguments", loc);

        Val val1 = args.getVal(0);
        if (val1.getMode() != Mode.IMMEDIATE)
            throw new ExEx("atan2() 1st argument not immediate mode", loc);
        if (val1.getPrimType() != Ptype.FLOAT)
            throw new ExEx("atan2() 1st argument not a float", loc);
        double  f1 = val1.getSingleFval(loc);

        Val val2 = args.getVal(1);
        if (val2.getMode() != Mode.IMMEDIATE)
            throw new ExEx("atan2() 2nd argument not immediate mode", loc);
        if (val2.getPrimType() != Ptype.FLOAT)
            throw new ExEx("atan2() 2nd argument not a float", loc);
        double  f2 = val2.getSingleFval(loc);

        return(new Val(Math.atan2(f1, f2), loc));
    }
}
