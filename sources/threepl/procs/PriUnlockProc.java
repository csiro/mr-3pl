package threepl.procs;

import static threepl.ThreePL.getCurrentClock;
import static threepl.ThreePL.tdelist;

import java.util.ArrayList;

import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.QueueRefs;
import threepl.exec.Priority;
import threepl.exec.Ref;
import threepl.exec.SubFieldList;
import threepl.exec.Var;
import threepl.exec.Var.IDtype;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to unlock priority access.
 */
public class PriUnlockProc extends InbuiltProc implements Constant, TDEConstants {

    /**
     * Construct the inbuilt procedure PriUnlockProc().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public PriUnlockProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = true;
    }

    /**
     * Execute the procedure priunlock().
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
        if (inargs.size() != 1)
            throw new ExEx("priunlock() must have one input argument", loc);
        if (outargs.size() != 0)
            throw new ExEx("priunlock() must have no output arguments", loc);
        if (pri_out != null)
            throw new ExEx("priunlock() nested within waitpri statement", loc);
        
        Ref pref = inargs.getRef(0, "priunlock()");
        Var pvar = pref.getVar();
        
        if (!(pvar instanceof Priority))
            throw new ExEx("priunlock() - argument variable '" + pvar.getID(IDtype.CHAIN) +
                            "' not priority mode", loc);
        SubFieldList    pvar_sfl = pref.getSubFields();
        if ((pvar_sfl != null) && !pvar_sfl.isEmpty())
            throw new ExEx("priunlock() - argument priority variable '" + pvar.getID(IDtype.CHAIN) +
                            "' has subscipts", loc);
        
        TDEVar  start = tdelist.signal("S", loc);
        TDEVar  finish = tdelist.signal("S", loc);
        
        ((Priority)pvar).addUnlockSignal(start, loc);

        tdelist.del(finish, start, getCurrentClock(), esig);
        startl.add(start);
        finishl.add(finish);
    }
}
