package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to convert an integer value to a binary string.
 * This has one or two arguments. The first argument is an immediate
 * bits, int or uint value. The optional second argument is an immediate
 * uint or int giving the minimum number of binary digits.
 */
public class IntToBinStringFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the hexadecimal string
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if ((args.size() < 1) || (args.size() > 2))
            throw new ExEx("inttobinstring() must have 1 or 2 arguments", loc);

        long    v = args.getVal(0).getSingleIval(loc);
        String  s = Long.toBinaryString(v);

        if (args.size() == 2) {
            long    l = args.getVal(1).getSingleIval(loc);
            while (s.length() < l)
                s = "0" + s;
        }

        return(new Val(s, loc));
    }
}
