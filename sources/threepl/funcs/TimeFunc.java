package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the current millisecond time. It has no arguments.
 */
public class TimeFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the directive list array of structs
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 0)
            throw new ExEx("time() cannot have any arguments", loc);

        long    timems = System.currentTimeMillis();
        Val         v = new Val(timems, loc);
        
        return(v);
    }
}
