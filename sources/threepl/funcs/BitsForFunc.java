package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the number of bits required to represent a
 * +ve or -ve integer.
 */
public class BitsForFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the number of bits
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("bitsfor() must have a single argument", loc);

        
        Val     val = args.getVal(0);
        if (!val.isPrimitive() ||
            ((val.getPrimType() != Ptype.UINT) && (val.getPrimType() != Ptype.INT)))
            throw new ExEx("bitsfor() argument not an integer", loc);
        if (val.getMode() != Mode.IMMEDIATE)
            throw new ExEx("bitsfor() argument not immediate mode", loc);
        long    i = val.getSingleIval(loc);
        int     j = 0;
        if (i == 0)
            throw new ExEx("bitsfor() argument is 0", loc);
        if (i < 0) {
            i = -1 - i;
            j = 1;
        }
        while (i != 0) {
            i >>= 1;
            j++;
        }
        return(new Val(j, loc));
    }
}
