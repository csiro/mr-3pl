package threepl.procs;

import static threepl.ThreePL.addVar;

import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Clock;
import threepl.exec.Ref;
import threepl.exec.Var;
import threepl.exec.Var.IDtype;
import threepl.nodes.Ident;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to create a clock variable.
 * This has 2 input arguments and no output arguments.
 * The 1st input is an unsubscripted signal
 * identifier, or a compound value of unsubscripted signal identifiers.
 * These are the identifiers of the clock variables to be created.
 * The 2nd input argument is the clock to be inverted.
 */
public class NegClockProc extends InbuiltProc implements Constant, TDEConstants {

    /**
     * Construct the inbuilt procedure negclock().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public NegClockProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
    }

    /**
     * Execute the procedure negclock(). This does not generate executable code.
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
        if (inargs.size() != 2)
            throw new ExEx("negclock() must have 2 input arguments", loc);
        if (outargs.size() != 0)
            throw new ExEx("negclock() must have no output arguments", loc);

        Ident   ncid = inargs.getIdent(0, "negclock() - 1st argument");
        Clock   ncvar = new Clock(ncid, null, false, false, loc);
        addVar(ncvar, ncid.getScopeContext(), loc);
        TDEVar  nclock = ncvar.getClkSig();

        Ref     cref = inargs.getRef(1, "negclock() - 2nd argument");
        Var     cvar = cref.getVar();
        if (cvar == null)
            throw new ExEx("negclock(): 2nd input argument is not a variable", loc);
        if (cvar.getMode() != Mode.CLOCK)
            throw new ExEx("negclock(): '" + cvar.getID(IDtype.CHAIN) + "' is not a clock variable", loc);
        TDEVar  clock = cvar.getClkSig();
        if (clock.getClockMode() == ClkType.NEG)
            throw new ExEx("negclock() clock signal '" + cvar.getID(IDtype.CHAIN) + "' is itself inverted", loc);

        nclock.setID(cvar.getEname());
        nclock.setClockMode(ClkType.NEG);
        ncvar.setClkFreq(cvar.getClkFreq(loc), loc);
        ncvar.setClkDutyCycle(1.0 - cvar.getClkDutyCycle(loc), loc);
        ncvar.setAssociatedClock((Clock)cvar);
        cvar.setAssociatedClock(ncvar);
        if (cvar.isAssigned())
            ncvar.setAssigned();
    }
}
