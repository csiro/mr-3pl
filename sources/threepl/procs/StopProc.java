package threepl.procs;

import static threepl.ThreePL.tdelist;

import java.util.ArrayList;

import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.QueueRefs;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to halt execution of this thread.
 * This has no input or output arguments.
 */
public class StopProc extends InbuiltProc implements Constant {

    /**
     * Construct the inbuilt procedure stop().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public StopProc () {
        allowed_as_param = false;
        check_null_input_args = false;
        check_null_output_args = false;
        target_inline = true;
    }

    /**
     * Execute the procedure stop(). This generates a start signal,
     * which is unused, and a stop signal which is GND.
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
        SrcLoc  loc = inargs.getCallLoc();
        if (pri_in != null)
            throw new ExEx("stop() procedure within 'waitpri' statement", loc);
        
        if (inargs.size() != 0)
            throw new ExEx("stop() cannot have input arguments", loc);
        if (outargs.size() != 0)
            throw new ExEx("stop() cannot have output arguments", loc);
         
        TDEVar      start = tdelist.signal("S", loc);
        TDEVar      finish = tdelist.signal("F", loc);
        startl.add(start);
        finishl.add(finish);
        tdelist.connect(finish, TDEVar.GND);
    }
}
