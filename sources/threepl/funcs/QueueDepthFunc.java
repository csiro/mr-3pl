package threepl.funcs;

import threepl.ThreePL;
import threepl.exceptions.ExEx;
import threepl.exec.Queue;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.exec.Var.IDtype;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the depth of a queue buffer.
 */
public class QueueDepthFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the number of entries in the queue
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("queuedepth() must have a single argument", loc);

        Val     val = args.getVal(0, true);
        Var     var = val.getVar();
        if (var == null)
            throw new ExEx("queuedepth(): argument is not a variable", loc);
        if (var.getMode() != Mode.QUEUE)
            throw new ExEx("queuedepth(): '" + var.getID(IDtype.CHAIN) + "' is not a queue variable", loc);
        long    depth = ((Queue)var).getBufferSize();
        String  family = ThreePL.findDir("family").getSingleSval(loc);
        if (family == null)
            throw new ExEx("FPGA family has not been defined");
        if (family.equals("XC4V"))
            depth += 2;
        return(new Val(depth, loc));
    }
}
