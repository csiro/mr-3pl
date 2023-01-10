package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to extract a leading string.
 * The 1st argument is the subject string and the 2nd argument is
 * an integer value giving the number of characters required.
 */
public class HeadFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the head string
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 2)
            throw new ExEx("head() must have 2 arguments", loc);

        String  s1 = args.getVal(0).getSingleSval(loc);
        int     i2 = (int)args.getVal(1).getSingleIval(loc);
        String  s;
        try {
            s = s1.substring(0, i2);
        } catch (IndexOutOfBoundsException e) {
            throw new ExEx("head() - substring index out of bounds", loc);
        }
        return(new Val(s, loc));
    }
}
