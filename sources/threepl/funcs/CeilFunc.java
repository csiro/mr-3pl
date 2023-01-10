package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to round an immediate floating point
 * value to the nearest integer towards +ve infinity.
 */
public class CeilFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the ceiling value
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        
        if (args.size() != 1)
            throw new ExEx("ceil() - must have one argument", loc);

        Val val = args.getVal(0);
        if (val.getMode() != Mode.IMMEDIATE)
            throw new ExEx("ceil() argument not immediate mode", loc);
        if (val.getPrimType() != Ptype.FLOAT)
            throw new ExEx("ceil() argument not a float", loc);
        double  f = val.getSingleFval(loc);
        return(new Val((long)(Math.ceil(f)), loc));
    }
}
