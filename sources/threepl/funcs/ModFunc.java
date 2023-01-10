package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the modulus of its immediate
 * integer or floating argument. The returned value is an
 * immediate integer or floating type to match the argument.
 */
public class ModFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the modulud value
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        
        if (args.size() != 1)
            throw new ExEx("mod() - must have one argument", loc);

        Val val = args.getVal(0);
        if (val.getMode() != Mode.IMMEDIATE)
            throw new ExEx("mod() argument not immediate mode", loc);
        switch (val.getPrimType()) {
        case UINT:
        case INT:
            long v = val.getSingleIval(loc);
            if (v < 0)
                v = -v;
            return(new Val(v, loc));
        case FLOAT:
            double f = val.getSingleFval(loc);
            if (f < 0)
                f = -f;
            return(new Val(f, loc));
        default:
            throw new ExEx("mod() argument not an integer or float", loc);
        }
    }
}
