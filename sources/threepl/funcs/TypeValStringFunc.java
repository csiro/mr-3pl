package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to return the type value of a type variable
 * as a string.
 */
public class TypeValStringFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the type
     */
    public Val getVal (NodeList args) {
        SrcLoc loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("typevalstring() must have single argument", loc);

        Val     val = args.getVal(0);
        String  si;

        if (val.getPrimType() == Ptype.STR) // is a string
            si = val.getSingleSval(loc);
        else if (val.getPrimType() == Ptype.TYPE)   // is a type
            si = val.getSingleTval(loc).getTypeString();
        else
            si = val.getTypeString();   // variable - get its type
        return(new Val(si, loc));

        /*
        Val     val = args.getVal(0);
        if (val.getMode() != Mode.IMMEDIATE) {
            type = val.getType();
        } else
            type = val.getSingleTval(loc);
        String  s = type.getTypeString();
        return(new Val(s, loc));*/
    }
}
