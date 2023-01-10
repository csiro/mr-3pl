package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the clock domain variable of
 * the argument. The argument may be value, selectvalue, static or
 * priority mode but not queue mode.
 */
public class DomainFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the clock frequency
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        Var     clockvar;

        if (args.size() != 1)
            throw new ExEx("domain() must have a single argument", loc);

        Ref ref = args.getRef(0, "domain()");
        if ((ref.getMode() == Mode.CMEMORY) || (ref.getMode() == Mode.RMEMORY))
            throw new ExEx("domain() argument is a memory variable", loc);
        if (ref.getMode() == Mode.CLOCK)
            throw new ExEx("domain() argument is a clock variable", loc);
        if (ref.getMode() == Mode.QUEUE)
            throw new ExEx("domain() argument is a queue variable", loc);
        if (ref.getMode() == Mode.VALUE) {
            Val val = ref.getVal(loc);
            clockvar = val.collectOutputClocks(loc);
        } else
            clockvar = ref.getReadClkVar();
        if (clockvar == null)
            return(new Val(loc));   // null
        return(new Val(clockvar, Mode.CLOCK, clockvar.getClkSig(), loc));
    }
}
