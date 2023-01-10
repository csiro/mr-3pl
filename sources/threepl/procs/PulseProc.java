package threepl.procs;

import static threepl.ThreePL.getCurrentClock;
import static threepl.ThreePL.getCurrentClockVar;
import static threepl.ThreePL.tdelist;

import java.util.ArrayList;

import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.QueueRefs;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.exec.WordSpec;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to generate a single clock cycle pulse.
 * This has one optional input argument and one output argument. The output
 * parameter is value mode type log. This will be true
 * for one clock cycle when the procedure executes.
 * If an input argument is provided it must be immediate "log" which if true
 * will OR the output value with the previous output value. If the output
 * value mode variable has no previous assignment the input argument is ignored.
 * 
 * THIS PROCEDURE HAS BEEN DEPRECATED!
 * The name 'pulse' was considered misleading and hardware-oriented.
 * The procedure has be changed to 'assert', the function remaining the same.
 */
public class PulseProc extends InbuiltProc implements Constant, TDEConstants {

    /**
     * Construct the inbuilt procedure pulse().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public PulseProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = true;
        target_inline = true;
    }

    /**
     * Execute the procedure pulse().
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
        
        boolean or = false;
        
        if (inargs.size() > 1)
            throw new ExEx("pulse() must have 0 or 1 input arguments", loc);
        if (outargs.size() != 1)
            throw new ExEx("pulse() must have one output argument", loc);

        Ref     oref = outargs.getRef(0, "pulse() -");
        if (oref.getMode() != Mode.VALUE)
            throw new ExEx("pulse() output argument must be value mode", loc);
        if (oref.getPrimType() != Ptype.LOG)
            throw new ExEx("pulse() output argument must be type log", loc);
        
        if (inargs.size() == 1) {
            Val     or_val = inargs.getVal(0);
            if (or_val.getMode() != Mode.IMMEDIATE)
                throw new ExEx("pulse() input argument must be immediate mode", loc);
            if (or_val.getPrimType() != Ptype.LOG)
                throw new ExEx("pulse() input argument must be type log", loc);
            or = or_val.getSingleLval(loc);
        }

        TDEVar  start_del = tdelist.signal("S", loc);
        TDEVar  start = tdelist.signal("S", loc);
        TDEVar  finish = tdelist.signal("S", loc);
        
        WordSpec    ws = new WordSpec(1, Ptype.LOG);
        TDEVar      tdev = tdelist.signal("P", ws, loc);
        Val         rval = new Val(null, Mode.STATIC, tdev, loc);
        rval.addOVar(getCurrentClockVar()); // special case - clock Var itself
        oref.checkMatch(rval, false, "pulse() ", loc);
        if (or) {
            Var     ovar = oref.getVar();
            int     i = oref.getWordSpec().getWord(0);
            if (ovar.getVal(i) == null) {
                // No initial value so just assign pulse signal.
                oref.assignTo(rval, loc);
            } else {
                // Assign OR of initial value and new pulse signal.
                Val     oval = outargs.getVal(0);
                TDEVar  otdev = oval.getTDEVar();
                oref.assignTo(new Val(ovar, Mode.VALUE, tdelist.or(otdev, tdev, loc), loc), loc);
            }
        } else
            oref.assignTo(rval, loc);

        TDEVar  v = tdelist.execp(null, null, true, null, false, start, pri_in, pri_out, esig, null, null, loc);
        tdelist.connect(start_del, v);

        tdelist.connect(tdev, start_del);

        tdelist.del(finish, start_del, getCurrentClock(), esig);
        startl.add(start);
        finishl.add(finish);
    }
}
