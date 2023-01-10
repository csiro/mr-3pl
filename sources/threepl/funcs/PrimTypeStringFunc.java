package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to return the primitive type of a variable or
 * expression as a string.
 * The type string does not include a bit width if a target type.
 */
public class PrimTypeStringFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the type string
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("primtypstringe() must have single argument", loc);

        Val val = args.getVal(0);
        String  s = val.getTypeString();
        if (s.startsWith("[") || s.startsWith("(") || s.startsWith("{"))
            return(new Val("none", loc));
        int i = s.indexOf(":");
        if (i < 0)
            return(new Val(s, loc));
        else
            return(new Val(s.substring(0, i), loc));
    }
}
