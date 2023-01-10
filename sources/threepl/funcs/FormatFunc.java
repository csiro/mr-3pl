package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to format immediate values to a string.
 * The first argument is a format string.
 * Any following arguments are used to fill items in the format.
 */
public class FormatFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the floor value
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        
        if (args.size() < 1)
            throw new ExEx("format() - must have at least one argument", loc);

        Val val = args.getVal(0);
        if (val.getMode() != Mode.IMMEDIATE)
            throw new ExEx("format() 1st argument not immediate mode", loc);
        if (val.getPrimType() != Ptype.STR)
            throw new ExEx("format() 1st argument not a string", loc);
        String      format = val.getSingleSval(loc);
        Object[]    argarray = new Object[20];
        int         j = 0;
        for (int i=1 ; i<args.size(); i++) {
            val = args.getVal(i);
            if (val.getMode() != Mode.IMMEDIATE)
                throw new ExEx("format() argument not immediate mode", loc);
            switch (val.getPrimType()) {
            case UINT:
            case INT:
                argarray[j++] = val.getSingleIval(loc);
                break;
            case FLOAT:
                argarray[j++] = val.getSingleFval(loc);
                break;
            case STR:
                argarray[j++] = val.getSingleSval(loc);
                break;
            case LOG:
                argarray[j++] = val.getSingleLval(loc);
                break;
            default:
                throw new ExEx("format() argument type " + val.getPrimType().name() + " not allowed", loc);
            }
        }
        
        return(new Val(String.format(format, argarray), loc));
    }
}
