package threepl.procs;

import static threepl.ThreePL.tdelist;

import java.util.ArrayList;

import threepl.ThreePL;
import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.QueueRefs;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to generate a null operation.
 * This has one optional input argument and no output arguments.
 * The optional argument gives the number of clock cycles to delay,
 * otherwise the procedure executes in one clock cycle.
 * The argument should not be too large (say no more than 80) as the
 * control delay is via 16-bit shift registers if {@code >} 1.
 */
public class NopProc extends InbuiltProc implements Constant, TDEConstants {

    /**
     * Construct the inbuilt procedure NopProc().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public NopProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = true;
    }

    /**
     * Execute the procedure nop().
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
        int     n = 1;
        SrcLoc  loc = inargs.getCallLoc();
        if (inargs.size() > 1)
            throw new ExEx("nop() must have 0 or 1 input arguments", loc);
        if (outargs.size() > 0)
            throw new ExEx("nop() must have no output arguments", loc);

        if (inargs.size() == 1) {
            Val val = inargs.getVal(0);
            n = (int)val.getSingleIval(loc);
        }
        
        TDEVar  start_del = tdelist.signal("S", loc);
        TDEVar  start = tdelist.signal("S", loc);
        TDEVar  finish = tdelist.signal("S", loc);

        if (availok)
            tdelist.connect(start_del, start);
        else {
            if ((pri_out != null) && (n != 1))
                throw new ExEx("multi-cycle nop() nested within 'waitpri' statement", loc);
            TDEVar  v = tdelist.execp(null, null, true, null, false, start, pri_in, pri_out, esig, null, null, loc);
            tdelist.connect(start_del, v);
        }
        
        tdelist.del(finish, start_del, ThreePL.getCurrentClock(), n, esig);
        startl.add(start);
        finishl.add(finish);
    }
}
