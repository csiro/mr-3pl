package threepl.funcs;

import threepl.ThreePL;
import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the previous clock variable, i.e.
 * the clock variable on the stack below the current (top) entry.
 * This has no input or output arguments.
 */
public class PrevClockFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the previous clock
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        if (args.size() != 0)
            throw new ExEx("prevclock() must have no arguments", loc);

        Var clockvar = ThreePL.getPrevClockVar();
        if (clockvar == null)
            return(new Val(loc));   // null
        return(new Val(clockvar, Mode.CLOCK, clockvar.getClkSig(), loc));
    }
}
