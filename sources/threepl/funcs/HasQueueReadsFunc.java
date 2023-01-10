package threepl.funcs;

import static threepl.ThreePL.getLocalVars;

import java.util.Set;
import java.util.TreeMap;

import threepl.exceptions.ExEx;
import threepl.exec.QueueRefs;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to determine if a value contains queue reads or if
 * any input parameters have queue reads.
 */
public class HasQueueReadsFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  true if the argument has queue reads
     */
    public Val getVal (NodeList args) {
        SrcLoc      loc = args.getCallLoc();
        Val         val;
        QueueRefs    queues;

        if (args.size() == 0) {
            // No arguments - check all input parameters.
            TreeMap<String, Var>    vars = getLocalVars();
            Set<String>             ks = vars.keySet();
            for(String ident: ks) {
                Var     var = vars.get(ident);
                if (!var.isInputPar())
                    continue;
                if (var.getMode() == Mode.QUEUE)
                    return(new Val(true, loc));
                if (var.getMode() == Mode.VALUE) {
                    int words = var.getType().numWords();
                    for (int i=0 ; i<words ; i++) {
                        if (!var.isMatched())
                            continue;
                        val = (Val)var.getVal(i);
                        queues = val.getQueues();
                        if (!queues.getReads().isEmpty())
                            return(new Val(true, loc));
                    }
                }
            }                
            return(new Val(false, loc));
        }

        if (args.size() != 1)
            throw new ExEx("hasqueuereads() cannot have more than one argument", loc);
        
        val = args.getVal(0);
        queues = val.getQueues();
        if (queues.getReads().isEmpty())
            return(new Val(false, loc));
        else
            return(new Val(true, loc));
    }
}
