package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the type value of a string.
 * The argument is the subject string.
 */
public class StrToTypeFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the substring
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("strtotype() must have 1 argument", loc);

        String  s = args.getVal(0).getSingleSval(loc);
        Type    t = new Type(s, false, false, loc);
        return(new Val(t, loc));
    }
}
