package threepl.funcs;

import java.util.regex.PatternSyntaxException;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to substitute characters from one set with those from
 * another set.
 * The 1st argument is the subject string, the 2nd argument is
 * a string giving the set of characters to be replaced and
 * the 3rd argument is a string giving the substitute characters.
 * If the 3rd argument string is shorter than the 2nd then those
 * trailing characters in the 1st set will be eliminated.
 * If the 3rd argument string is longer than the 2nd then those
 * trailing characters in the 2nd set will be ignored.
 * If a character is repeated in the 2nd argument string the left-most
 * substitution will occur.
 */
public class SubstituteFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the substituted string
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 3)
            throw new ExEx("substitute() must have 3 arguments", loc);

        String  s1 = args.getVal(0).getSingleSval(loc);
        String  s2 = args.getVal(1).getSingleSval(loc);
        String  s3 = args.getVal(2).getSingleSval(loc);
        
        for (int i=0 ; i<s2.length() ; i++) {
            if (i < s3.length())
                s1 = s1.replace(s2.charAt(i), s3.charAt(i));
            else {
                try {
                    s1 = s1.replaceAll(s2.substring(i, i+1), "");
                } catch (PatternSyntaxException e) {
                    throw new ExEx("substitute() - bad 2nd argument", loc);
                }
            }
        }
        return(new Val(s1, loc));
    }
}
