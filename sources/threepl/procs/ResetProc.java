package threepl.procs;

import static threepl.ThreePL.getCurrentClock;
import static threepl.ThreePL.tdelist;

import java.util.ArrayList;
import java.util.Iterator;

import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.QueueRefs;
import threepl.exec.Ref;
import threepl.exec.SubFieldList;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to reset a static or queue.
 * This has one or more input arguments and no output arguments. The input
 * arguments are static or queue variables or static variable array members,
 * or fields. On execution in the target this procedure clears the static
 * or queue variables or specified array members or struct fields of static
 * variables.
 */
public class ResetProc extends InbuiltProc implements Constant, TDEConstants {

    /**
     * Construct the inbuilt procedure reset().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public ResetProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = true;
    }

    /**
     * Execute the procedure reset().
     * @param   inargs is a list of input argument tree nodes
     * @param   outargs is a list of output argument tree nodes
     * @param   esig is an exception/restart signal, or null
     * @param   startl is a list of start signals for target statements
     *          below this node
     * @param   finishl is a list of finish signals for target statements
     *          below this node
     * @param   queues returns all the queue availability signals
     *          accumulated from code below
     * @param   availok is true if a previous sync makes a queue availability
     *          wait unnecessary
     * @param   pri_in is an optional input signal to a priority encoder
     * @param   pri_out is an optional output signal from a priority encoder
     */
    public void execute (
        NodeList            inargs,
        NodeList            outargs,
        TDEVar              esig,
        ArrayList<TDEVar>   startl, 
        ArrayList<TDEVar>   finishl,
        QueueRefs            queues,
        boolean             availok,
        TDEVar              pri_in,
        TDEVar              pri_out
    ) {
        SrcLoc          loc = inargs.getCallLoc();
        int             in = inargs.size();
        Ref             ref;
        SubFieldList    sfl;
        TDEVar          start_del = tdelist.signal("S", loc);
        TDEVar          start = tdelist.signal("S", loc);
        TDEVar          finish = tdelist.signal("F", loc);
        String          mess;
        
        if (in == 0)
            throw new ExEx("reset() must have at least 1 input argument", loc);
        if (outargs.size() != 0)
            throw new ExEx("reset() must have no output arguments", loc);

        for (int i=0 ; i<in ; i++) {
            mess = "reset() argument " + i + " - ";
            ref = inargs.getRef(i, mess);
            if (ref == null)
                continue;
            sfl = ref.getSubFields();
            if ((ref.getMode() != Mode.STATIC) && (ref.getMode() != Mode.QUEUE))
                throw new ExEx(mess + "input argument must be static or queue mode", loc);
            if (ref.getMode() == Mode.QUEUE) {
                if (sfl.size() != 0)
                    throw new ExEx(mess + "queue variable reference has subscripts or fields", loc);
                ref.getVar().addReset(start_del, loc);
            } else {
                if (sfl.hasTargetSubs()) {
                    Iterator<?>    it = ref.targSubIterator();
                    while (it.hasNext()) {
                        Ref partref = (Ref)it.next();
                        partref.resolveClocks(null, Calloc.VALUE, loc);
                        TDEVar  select = partref.getSelect();
                        ref.getVar().addReset(select, partref.getWordSpec(), loc);
                    }
                } else
                    ref.getVar().addReset(start_del, ref.getWordSpec(), loc);
            }
        }

        TDEVar  v = tdelist.execp(null, null, true, null, false, start, pri_in, pri_out, esig, null, null, loc);
        tdelist.connect(start_del, v);

        tdelist.del(finish, start_del, getCurrentClock(), esig);
        startl.add(start);
        finishl.add(finish);
    }
}
