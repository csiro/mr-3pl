package threepl.funcs;

import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Queue;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.exec.Var.IDtype;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get a target value which is true if a
 * synchronous queue is empty.
 */
public class QueueIsEmptyFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the number of free spaces in the queue
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("queueisempty() must have a single argument", loc);

        Val     val = args.getVal(0);
        Var     var = val.getVar();
        if (var == null)
            throw new ExEx("queueisempty(): argument is not a variable", loc);
        if (var.getMode() != Mode.QUEUE)
            throw new ExEx("queueisempty(): '" + var.getID(IDtype.CHAIN) + "' is not a queue variable", loc);
        
        TDEVar  t = ((Queue)var).getQueueEmpty(loc);
        
        Val oval = new Val(null, Mode.VALUE, t, loc);
        oval.addOVar(var);
        return(oval);
    }
}
