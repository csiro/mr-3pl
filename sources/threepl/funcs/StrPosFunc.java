package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to find the left-most position of a string in
 * a string. The 1st argument is the subject string and the second
 * argument is the string to be found.
 * The position of the string, or else -1, is returned.
 */
public class StrPosFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the string position
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 2)
            throw new ExEx("strpos() must have 2 arguments", loc);

        String  s1 = args.getVal(0).getSingleSval(loc);
        String  s2 = args.getVal(1).getSingleSval(loc);
        return(new Val(s1.indexOf(s2), loc));
    }
}
