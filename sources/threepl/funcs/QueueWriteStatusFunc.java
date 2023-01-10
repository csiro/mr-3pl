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
 * An inbuilt function to get the write status of an asynchronous
 * queue buffer.
 */
public class QueueWriteStatusFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the queue occupancy status
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("queuewritestatus() must have a single argument", loc);

        Ref     ref = args.getRef(0, "queuewritestatus()");
        Var     var = ref.getVar();
        if (var == null)
            throw new ExEx("queuewritestatus(): argument is not a variable", loc);
        if (var.getMode() != Mode.QUEUE)
            throw new ExEx("queuewritestatus(): '" + var.getID(IDtype.CHAIN) + "' is not a queue variable", loc);

        TDEVar  t = ((Queue)var).getQueueWStat(loc);
        
        Val val = new Val(null, Mode.VALUE, t, loc);
        val.addIVar(var);
        return(val);
    }
}
