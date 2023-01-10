package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.exec.Var.IDtype;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the (maximum) frequency of a clock variable
 * in MHz. This has 1 argument, which is the variable.
 */
public class FrequencyFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the clock frequency
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("frequency() must have a single argument", loc);

        Val     val = args.getVal(0);
        Var     var = val.getVar();
        if (var == null) {
            if (val.getVals() == null)
                throw new ExEx("frequency(): argument is null", loc);
            if (val.getVal(0) == null)
                throw new ExEx("frequency(): argument is null", loc);
            throw new ExEx("frequency(): argument is not a variable", loc);
        }
        if (var.getMode() != Mode.CLOCK)
            throw new ExEx("frequency(): '" + var.getID(IDtype.CHAIN) + "' is not a clock variable", loc);
        return(new Val(var.getClkFreq(loc), loc));
    }
}
