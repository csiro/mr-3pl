package threepl.funcs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import threepl.exceptions.ExEx;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.exec.WordSpec;
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
public class FindFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the converted string
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if ((args.size() < 2) || (args.size() > 3))
            throw new ExEx("find() must have 2 or 3 arguments", loc);

        String  s = args.getVal(0).getSingleSval(loc);
        String  re = args.getVal(1).getSingleSval(loc);
        int     start = (int)((args.size() > 2) ? args.getVal(2).getSingleIval(loc) : 0);
        long    sindex = 0;
        long    eindex = 0;
        Pattern p;
        Matcher m;
        
        try {
            p = Pattern.compile(re);
        } catch (PatternSyntaxException e) {
            throw new ExEx("find() - bad regular expression", loc);
        }
        m = p.matcher(s);

        if (m.find(start)) {
            sindex = m.start();
            eindex = m.end();
        }

        Type        type = new Type("(start, int, end, int)", loc);
        WordSpec    ws = type.getWordSpec(null, loc);
        Type        it = new Type("int", loc);
        Object[]    oa = new Object[2];
        Type[]      ta = new Type[2];

        oa[0] = sindex;
        ta[0] = it;
        oa[1] = eindex;
        ta[1] = it;
        
        return(new Val(oa, ta, ws, null, null, loc));
    }
}
