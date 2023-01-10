package threepl.procs;

import static threepl.ThreePL.getCurrentClockVar;
import static threepl.ThreePL.getModuleScope;
import static threepl.ThreePL.tdelist;

import java.util.ArrayList;

import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Clock;
import threepl.exec.Memory;
import threepl.exec.QueueRefs;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to write to a combinatorial output RAM block.
 * This has 4 input arguments and no output arguments.
 *
 * <p>The 1st input argument is the memory mode variable.
 *
 * <p>The 2nd input argument is the number of ports.
 *
 * <p>The 3rd input argument is the address signal.
 *
 * <p>The 4th input argument is the data input signal.
 */
public class CmemoryWriteProc extends InbuiltProc implements Constant, TDEConstants {

    /**
     * Construct the inbuilt procedure cmemoryWrite().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public CmemoryWriteProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = true;
        ipnames.put("m", 0);
        ipnames.put("port", 1);
        ipnames.put("addr", 2);
        ipnames.put("data", 3);
    }

    /**
     * Execute the procedure cmemoryWrite(). This does not generate in-line
     * executable code.
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
        if (inargs.size() != 4)
            throw new ExEx("cmemoryWrite() must have 4 input arguments", loc);
        if (outargs.size() != 0)
            throw new ExEx("cmemoryWrite() must have no output arguments", loc);

        Clock   clockvar = getCurrentClockVar();
        TDEVar  clock = clockvar.getClkSig();
        TDEVar  start = tdelist.signal("S", loc);
        TDEVar  start_del = tdelist.signal("S", loc);
        TDEVar  finish = tdelist.signal("S", loc);
        
        // identifier
        Ref     ref = inargs.getRef(0, "rmemoryWrite()");
        if (ref.getMode() != Mode.CMEMORY)
            throw new ExEx("cmemoryWrite() 1st argument is not a cmemory variable", loc);
        Memory  var = (Memory)ref.getVar();
        
        // port
        Val     pval = inargs.getVal(1);
        int     port = (int)pval.getSingleIval(loc);
              
        Val     addr = inargs.getVal(2);        
        Val     data = inargs.getVal(3);    
        addr.resolveClocks(null, Calloc.VALUE, loc);
        data.resolveClocks(null, Calloc.VALUE, loc);
        
        var.memWrite (port, clockvar, addr, data, queues, start_del, "cmemoryWrite() -", loc);

        TDEVar  rpending = tdelist.signal("S", loc);
        
        // generate the queue acknowledges
        queues.addPops(getModuleScope(), start_del, rpending, loc);

        // generate queue and priority waits where necessary
        TDEVar  v = tdelist.execp(queues, null, availok, null, false, start, pri_in, pri_out, esig, null, rpending, loc);
        tdelist.connect(start_del, v);
        tdelist.del(finish, start_del, clock, esig, queues);
        
        startl.add(start);
        finishl.add(finish);
    }
}
