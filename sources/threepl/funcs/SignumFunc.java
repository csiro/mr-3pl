package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the signum function of its argument.
 */
public class SignumFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the maximum value
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        if (args.size() != 1)
            throw new ExEx("signum() - must have one argument", loc);
        
        Val val = args.getVal(0);
        if (val.getMode() != Mode.IMMEDIATE)
            throw new ExEx("abs() argument not immediate mode", loc);
        switch (val.getPrimType()) {
        case INT:
        case UINT:
            long    v = val.getSingleIval(loc);
            if (v == 0)
                return(new Val(0, loc));
            else if (v < 0)
                return(new Val(-1, loc));
            else
                return(new Val(1, loc));
        case FLOAT:
            return(new Val(Math.signum(val.getSingleFval(loc)), loc));
        default:
            throw new ExEx("abs() argument not an int or float", loc);
        }
    }
}
