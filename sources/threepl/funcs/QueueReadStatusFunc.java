package threepl.funcs;

import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Queue;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.exec.Var.IDtype;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the read status of an asynchronous
 * queue buffer.
 */
public class QueueReadStatusFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the queue occupancy status
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("queuereadstatus() must have a single argument", loc);

        Ref     ref = args.getRef(0, "queuereadstatus()");
        Var     var = ref.getVar();
        if (var == null)
            throw new ExEx("queuereadstatus(): argument is not a variable", loc);
        if (var.getMode() != Mode.QUEUE)
            throw new ExEx("queuereadstatus(): '" + var.getID(IDtype.CHAIN) + "' is not a queue variable", loc);

        TDEVar  t = ((Queue)var).getQueueRStat(loc);
        
        Val val = new Val(null, Mode.VALUE, t, loc);
        val.addOVar(var);
        return(val);
    }
}
