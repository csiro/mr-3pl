package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the integer value of a string.
 * The argument is the subject string.
 */
public class StrToIntFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the substring
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("strtoint() must have 1 argument", loc);

        String  s = args.getVal(0).getSingleSval(loc);
        try {
        if (s.equals("0"))
            return(new Val(0, loc));
        else if (s.startsWith("0x") || s.startsWith("0X"))
            return(new Val(Integer.parseInt(s.substring(2), 16), loc));
        else if (s.startsWith("0b") || s.startsWith("0B"))
            return(new Val(Integer.parseInt(s.substring(2), 2), loc));
        else if (s.startsWith("0") || s.startsWith("0"))
            return(new Val(Integer.parseInt(s.substring(1), 8), loc));
        else
            return(new Val(Integer.parseInt(s), loc));
        } catch (NumberFormatException e) {
            throw new ExEx("strtoint() - invalid number format, string '" + s + "'", loc);
        }
    }
}
