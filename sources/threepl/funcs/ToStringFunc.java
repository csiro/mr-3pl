package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to convert a value to a string.
 */
public class ToStringFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the identifier string
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        String  s = null;

        if (args.size() != 1)
            throw new ExEx("tostring() must have 1 argument", loc);

        
        Val     val = args.getVal(0);
        if (val.getMode() != Mode.IMMEDIATE)
            throw new ExEx("tostring() argument is not immediate", loc);

        switch (val.getPrimType()) {
        case STR:
        case INT:
        case UINT:
        case FLOAT:
        case LOG:
        case ENUM:
            s = val.forceSingleSval(loc);
            break;
        case BITS:
            s = Long.toHexString(val.getSingleIval(loc));
            break;
        default:
            throw new ExEx("tostring() argument not convertible type", loc);
        }
        
        return(new Val(s, loc));
    }
}
