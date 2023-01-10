package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to find the length of the head of a string
 * whose characters appear in a character set.
 * The 1st argument is the subject string and the 2nd argument is
 * a string representing the character set. The same character may appear
 * more than once in the set.
 */
public class SpanFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the string length
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 2)
            throw new ExEx("span() must have 2 arguments", loc);

        String  s1 = args.getVal(0).getSingleSval(loc);
        String  s2 = args.getVal(1).getSingleSval(loc);
        int     len = 0;
        
        for (int i=0 ; i<s1.length() ; i++) {
            if (s2.indexOf(s1.charAt(i)) < 0)
                break;
            len++;
        }
        return(new Val(len, loc));
    }
}
