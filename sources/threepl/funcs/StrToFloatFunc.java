package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the float value of a string.
 * The argument is the subject string.
 */
public class StrToFloatFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the substring
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("strtofloat() must have 1 argument", loc);

        String  s = args.getVal(0).getSingleSval(loc);
        try {
            return(new Val(Double.parseDouble(s), loc));
        } catch (NumberFormatException e) {
            throw new ExEx("strtofloat() - invalid number format, string '" + s + "'", loc);
        }
    }
}
