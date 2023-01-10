package threepl.funcs;

import java.util.regex.PatternSyntaxException;

import threepl.exceptions.ExEx;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.exec.WordSpec;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to split a string into substrings.
 * The 1st argument is the subject string, the 2nd argument is
 * a regular expression string selecting describing the split positions.
 * The substrings are returned in an array of strings.
 */
public class SplitFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the converted string
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 2)
            throw new ExEx("split() must have 2 arguments", loc);

        String      s1 = args.getVal(0).getSingleSval(loc);
        String      s2 = args.getVal(1).getSingleSval(loc);
        String[]    sa;
        try {
            sa = s1.split(s2);
        } catch (PatternSyntaxException e) {
            throw new ExEx("split() - bad regular expression", loc);
        }
        
        int         len = sa.length;
        Type        type = new Type("[" + len + "]str", loc);
        Type        pt = new Type("str", loc);
        WordSpec    ws = type.getWordSpec(null, loc);
        Type[]      ta = new Type[len];
        for (int i=0 ; i<len ; i++)
            ta[i] = pt;
        
        return(new Val(sa, ta, ws, null, null, loc));

    }
}
