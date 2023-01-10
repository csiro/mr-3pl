package threepl.funcs;

import static threepl.ThreePL.*;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the current clock variable.
 * This has no input or output arguments.
 */
public class CurrentElementFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the current clock
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 0)
            throw new ExEx("currentElement() must have no arguments", loc);

        return(new Val(ce_name, loc));
    }
}
