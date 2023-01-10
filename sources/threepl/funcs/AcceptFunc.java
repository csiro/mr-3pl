package threepl.funcs;

import static threepl.ThreePL.getCurrentClockVar;
import static threepl.ThreePL.popCurrentClock;
import static threepl.ThreePL.pushCurrentClock;
import static threepl.ThreePL.tdelist;

import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Clock;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.exec.WordSpec;
import threepl.nodes.Node;
import threepl.nodes.NodeList;
import threepl.nodes.NullNode;
import threepl.nodes.VarNode;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to make an argument acceptable to a clock domain.
 * The first or only argument is the target value to be accepted. It
 * cannot contain queues. If there is only one argument the relevant clock
 * domain is the current clock domain. If a second argument is provided
 * it must be a clock variable which gives the clock domain into which
 * the first argument is to be accepted.
 */
public class AcceptFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the clock frequency
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if ((args.size() < 1) || (args.size() > 2))
            throw new ExEx("accept() must have 1 or 2 arguments", loc);

        pushCurrentClock(null, null); // avoid forcing clock domain
        Val val = args.getVal(0);
        popCurrentClock(null);
        if (!val.getQueues().isEmpty())
            throw new ExEx("accept() 1st argument contains queue reads", loc);
        if ((val.getMode() == Mode.CMEMORY) || (val.getMode() == Mode.RMEMORY))
            throw new ExEx("accept() 1st argument is a memory variable", loc);
        if (val.getMode() == Mode.CLOCK)
            throw new ExEx("accept() 1st argument is a clock variable", loc);

        Clock dclock = null;
        if (args.size() == 2) {
            Node    n = args.getNode(1);
            if (n instanceof VarNode) {
                Ref cdref = args.getRef(1, "accept() - 2nd argument");
                if ((cdref != null) && (cdref.getMode() != Mode.CLOCK))
                    throw new ExEx("accept() - clock domain argument not CLOCK mode", loc);
                if (cdref != null)
                    dclock = (Clock)cdref.getVar();
            } else if (!(n instanceof NullNode)) {
                Val cdval = args.getVal(1);
                if ((cdval != null) && (cdval.getMode() != Mode.CLOCK))
                    throw new ExEx("accept() - clock domain argument not CLOCK mode", loc);
                if (cdval != null)
                    dclock = (Clock)cdval.getVar();
            }
        } else
            dclock = getCurrentClockVar();
        
        Var var = val.getVar(); // may be null!
        if ((var != null)) {
            val.setUsed();
            if (var.getMode() == Mode.CLOCK)
                ((Clock)val.getVar()).incrClkSinks();
        }
        /*
         * Commented out as this check barfs in the case of a compound
         * value mode argument where the elements have different clock domains.
         *
        if (val.getReadClkVar() == dclock)
            return(val);    // argument is already in desired clock domain
        */

        WordSpec    ws = val.getWordSpec();
        String      name = val.getTDEVar().getId();
        TDEVar      out = tdelist.namesignal(name, ws, loc);
        Val         vnew = new Val(null, val.getMode(), out, loc);
        out.setClockMode(val.getTDEVar().getClockMode());
        vnew.forceClock(dclock);
        return(vnew);
    }
}
