package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the minimum of its integer arguments.
 */
public class MinFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the minimum value
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        
        long    min = Long.MAX_VALUE;
        Val     val;
        long    v;

        for (int i=0 ; i<args.size() ; i++) {
            val = args.getVal(i);
            if (!val.isPrimitive() ||
                ((val.getPrimType() != Ptype.UINT) && (val.getPrimType() != Ptype.INT)))
                throw new ExEx("min() argument not an integer", loc);
            if (val.getMode() != Mode.IMMEDIATE)
                throw new ExEx("min() argument not immediate mode", loc);
            v = val.getSingleIval(loc);
            if (v < min)
                min = v;
        }

        
        return(new Val(min, loc));
    }
}
