package threepl.funcs;

import threepl.ThreePL;
import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the number of output arguments to the current module
 * or procedure (returns 0 for a function).
 */
public class NumOutArgsFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the maximum value
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 0)
            throw new ExEx("numoutargs() must not have arguments", loc);
        
        return(new Val(ThreePL.getCurrentBody().numOutArgs(), loc));
    }
}
