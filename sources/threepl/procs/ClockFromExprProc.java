package threepl.procs;

import static threepl.ThreePL.tdelist;

import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Clock;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to drive a clock signal from an expression value.
 * This has one input argument and one output argument. The input argument
 * is a value which must be type log and must be INPUT, SELECTVALUE,
 * VALUE, STATIC or PRIORITY mode and have no queue dependencies.
 * The output argument must be a variable whose mode is 'clock'.
 */
public class ClockFromExprProc extends InbuiltProc implements Constant, TDEConstants {

    /**
     * Construct the inbuilt procedure clockfromexpr().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public ClockFromExprProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = true;
        target_inline = false;
    }

    /**
     * Execute the procedure clockfromexpr().
     * @param   inargs is a list of input argument tree nodes
     * @param   outargs is a list of output argument tree nodes
     * @param   toplevel is true if we are at the module, procedure or function
     *          level
     */
    public void execute (
        NodeList    inargs, 
        NodeList    outargs, 
        boolean     toplevel
    ) {
        SrcLoc  loc = inargs.getCallLoc();
        if (inargs.size() != 1)
            throw new ExEx("clockfromexpr() must have 1 input argument", loc);
        if (outargs.size() != 1)
            throw new ExEx("clockfromexpr() must have 1 output argument", loc);

        Val     vali = inargs.getVal(0);
        if (vali.numWords() != 1)
            throw new ExEx("clockfromexpr() input argument must be a single value", loc);
        switch (vali.getMode()) {
        case INPUT:
        case SELECTVALUE:
        case VALUE:
        case STATIC:
        case PRIORITY:
            break;
        default:
            throw new ExEx("clockfromexpr() input argument must be mode VALUE, SELECTVALUE, STATIC or PRIORITY", loc);
        }
        if ((vali.getQueues() != null) && !vali.getQueues().isEmpty())
            throw new ExEx("clockfromexpr() input argument cannot contain queues", loc);
        if (vali.getPrimType() != Ptype.LOG)
            throw new ExEx("clockfromexpr() input argument must be type log", loc);
        TDEVar  tdevi = vali.getTDEVar();
        
        Ref     refo = outargs.getRef(0, "clockfromexpr()");
        if (refo.getMode() != Mode.CLOCK)
            throw new ExEx("clockfromexpr() output argument must be mode CLOCK", loc);
        TDEVar  tdevo = refo.getTDEVar();
        tdelist.connect(tdevo, tdevi);
        Clock   cvar = (Clock)refo.getVar();
        cvar.setAssigned();
        cvar.setClkSourced();
    }
}
