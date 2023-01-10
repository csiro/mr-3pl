package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the 1st argument raised to the power of the 2nd.
 */
public class PowFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the power value
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        boolean intresult = true;
        double  f1 = 0.0;
        double  f2 = 0.0;
        
        if (args.size() != 2)
            throw new ExEx("pow() - must have two arguments", loc);

        Val val1 = args.getVal(0);
        if (val1.getMode() != Mode.IMMEDIATE)
            throw new ExEx("pow() 1st argument not immediate mode", loc);
        if ((val1.getPrimType() ==Ptype.INT) || (val1.getPrimType() ==Ptype.UINT)) {
            f1 = val1.getSingleIval(loc);
        } else if (val1.getPrimType() == Ptype.FLOAT) {
            f1 = val1.getSingleFval(loc);
            intresult = false;
        } else
            throw new ExEx("pow() 1st argument not a float or int", loc);

        Val val2 = args.getVal(1);
        if ((val2.getPrimType() ==Ptype.INT) || (val2.getPrimType() ==Ptype.UINT)) {
            f2 = val2.getSingleIval(loc);
            if (f2 < 0.0)
                intresult = false;
        } else if (val1.getPrimType() == Ptype.FLOAT) {
            f2 = val2.getSingleFval(loc);
            intresult = false;
        } else
            throw new ExEx("pow() 2nd argument not a float", loc);
        double  r = Math.pow(f1, f2);
        if (intresult)
            return(new Val((long)r, loc));
        else
            return(new Val(r, loc));
    }
}
