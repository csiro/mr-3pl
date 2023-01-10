package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the square root.
 */
public class SqrtFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the square root value
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        
        if (args.size() != 1)
            throw new ExEx("sqrt() - must have one argument", loc);

        Val val = args.getVal(0);
        if (val.getMode() != Mode.IMMEDIATE)
            throw new ExEx("sqrt() argument not immediate mode", loc);
        if (val.getPrimType() != Ptype.FLOAT)
            throw new ExEx("sqrt() argument not a float", loc);
        double  f = val.getSingleFval(loc);
        return(new Val(Math.sqrt(f), loc));
    }
}
