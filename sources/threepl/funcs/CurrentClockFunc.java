package threepl.funcs;

import static threepl.ThreePL.getCurrentClockVar;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the current clock variable.
 * This has no input or output arguments.
 */
public class CurrentClockFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the current clock
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 0)
            throw new ExEx("currentclock() must have no arguments", loc);

        Var clockvar = getCurrentClockVar();
        if (clockvar == null)
            return(new Val(loc));   // null
        return(new Val(clockvar, Mode.CLOCK, clockvar.getClkSig(), loc));
    }
}
