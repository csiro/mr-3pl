package threepl.funcs;

import static threepl.ThreePL.getFamily;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the maximum allowed depth for a queue buffer.
 */
public class QueueMaxDepthFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the number of entries in the queue
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 0)
            throw new ExEx("queuemaxdepth() must have no arguments", loc);

        return(new Val(getFamily().maxRRAMQueueDepth(), loc));
    }
}
