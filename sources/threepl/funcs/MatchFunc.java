package threepl.funcs;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to attempt to match a regular expression string
 * against a subject string and returns a struct containing the starting
 * and ending indices of the match in the subject string. If there is no
 * match the returned indices are zero. The third argument is an optional
 * starting index for the search and is 0 by default.
 * the return value is a struct type "(start, int, end, int)" where
 * 'start' and 'end' are the indices of the match in the subject string.
 */
public class MatchFunc extends InbuiltFunc implements Constant {
    private static Pattern  pattern = null;
    private static Matcher  matcher;

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the converted string
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if ((args.size() == 1) || (args.size() > 3))
            throw new ExEx("match() must have 0, 2 or 3 arguments", loc);

        String  subject = null;
        String  re =  null;
        int     start = 0;
        boolean ok = false;
        
        if (args.size() > 0)
            subject = args.getVal(0).getSingleSval(loc);
        if (args.size() > 1)
            re = args.getVal(1).getSingleSval(loc);
        if (args.size() > 2)
            start = (int)(args.getVal(2).getSingleIval(loc));
        
        if (args.size() != 0) {
            try {
                pattern = Pattern.compile(re);
            } catch (PatternSyntaxException e) {
                throw new ExEx("match() - bad regular expression", loc);
            }
            if (subject == null)
                throw new ExEx("match() - not initialised with subject or subject is null", loc);
            matcher = pattern.matcher(subject);
        } else
            if (pattern == null)
                throw new ExEx("match() - not initialised with regular expression", loc);
        if (start != 0) {
            try {
                ok = matcher.find(start);
            } catch (IndexOutOfBoundsException e) {
                throw new ExEx("match() - start index out-of-bounds", loc);
            }
            start = 0;
        } else
            ok = matcher.find();
        start = 0;
        
        int             n = matcher.groupCount() + 1;
        ArrayList<Val>  al = new ArrayList<Val>();
        if (ok) {
            for (int i=0 ; i<n ; i++) {
                String  s = matcher.group(i);
                Val     v = s == null ? new Val("", loc) : new Val(s, loc);
                v.setDummyVar(true, loc);
                al.add(v);
            }
        }
        
        return(new Val(al, loc));
    }
}
