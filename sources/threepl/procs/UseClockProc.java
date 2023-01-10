package threepl.procs;

import threepl.ThreePL;
import threepl.exceptions.ExEx;
import threepl.exec.Clock;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.exec.Var.IDtype;
import threepl.nodes.ExprNode;
import threepl.nodes.ClassFuncCallNode;
import threepl.nodes.Node;
import threepl.nodes.NodeList;
import threepl.nodes.NullNode;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to declare the current clock.
 * This has 0 or 1 input arguments and no output arguments.
 * The input argument is an identifier, possibly subscripted,
 * which names a clock signal. That clock must have been
 * previously created using addclock(). The previous declared
 * current clock is pushed onto a stack. If the there are no
 * input arguments the stack is popped and that entry is used as
 * the current clock.
 */
public class UseClockProc extends InbuiltProc implements Constant {
    /**
     * Construct the inbuilt procedure useclock().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public UseClockProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
    }

    /**
     * Execute the procedure useclock(). This does not generate executable code.
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
        Var     svar;
        if (inargs.size() > 1)
            throw new ExEx("useclock() - must have 0 or 1 input arguments", loc);
        if (outargs.size() != 0)
            throw new ExEx("useclock() - cannot have output arguments", loc);

        if (!toplevel)
            throw new ExEx("useclock() - attempt to change clock when not at top target level", loc);
        if (inargs.size() == 0) {
            // pop the clock stack
            ThreePL.popCurrentClock(loc);
        } else if (inargs.getNode(0) instanceof NullNode) {
            // push null onto the clock stack
            ThreePL.pushCurrentClock(null, loc);
        } else {
            // push new clock Var onto the clock stack
            Node    n = inargs.getNode(0);
            if (n instanceof ClassFuncCallNode) {
                Val sval = inargs.getVal(0);
                svar = sval.getVar();
                if (svar == null)
                    throw new ExEx("useclock() - function argument returns null", loc);
            } else if (n instanceof ExprNode) {
                Val sval = inargs.getVal(0);
                svar = sval.getVar();
                if (svar == null)
                    throw new ExEx("useclock() - expression argument does not return clock", loc);
            } else {
                Ref sref = inargs.getRef(0, "useclock()");
                if (sref == null)
                    throw new ExEx("useclock() - variable not found", loc);
                svar = sref.getVar();
                if (svar == null)
                    throw new ExEx("useclock() - argument is not a variable", loc);
            }
            if (svar.getMode() != Mode.CLOCK)
                throw new ExEx("useclock() - '" + svar.getID(IDtype.CHAIN) + "' is not a clock variable", loc);
            ThreePL.pushCurrentClock((Clock)svar, loc);
        }
    }
}
