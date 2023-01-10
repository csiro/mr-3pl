package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to extract a substring.
 * The 1st argument is the subject string, the 2nd argument is
 * an integer value giving the starting position of the substring. The
 * optional 3rd argument is the length of the substring.
 */
public class SubstringFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the substring
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if ((args.size() < 2) || (args.size() > 3))
            throw new ExEx("substring() must have 2 or 3 arguments", loc);

        String  s1 = args.getVal(0).getSingleSval(loc);
        int     i2 = (int)args.getVal(1).getSingleIval(loc);
        String  s;
        if (args.size() == 2) {
            try {
                s = s1.substring(i2);
            } catch (IndexOutOfBoundsException e) {
                throw new ExEx("substring() - substring index out of bounds", loc);
            }
        } else {
            int i3 = (int)args.getVal(2).getSingleIval(loc);
            try {
                s = s1.substring(i2, i2 + i3);
            } catch (IndexOutOfBoundsException e) {
                throw new ExEx("substring() - substring index out of bounds", loc);
            }
        }
        return(new Val(s, loc));
    }
}
