package threepl.funcs;

import java.util.regex.PatternSyntaxException;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to replace a substring with another.
 * The 1st argument is the subject string, the 2nd argument is
 * a regular expression string selecting the substring to be replaced and
 * the 3rd argument is the replacement string.
 * All occurences selected by the 2nd argument, if any, will
 * be replaced.
 */
public class ReplaceAllFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the converted string
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 3)
            throw new ExEx("replaceall() must have 3 arguments", loc);

        String  s1 = args.getVal(0).getSingleSval(loc);
        String  s2 = args.getVal(1).getSingleSval(loc);
        String  s3 = args.getVal(2).getSingleSval(loc);
        String  s;
        try {
            s = s1.replaceAll(s2, s3);
        } catch (PatternSyntaxException e) {
            throw new ExEx("replaceall() - bad regular expression", loc);
        }
        return(new Val(s, loc));
    }
}
