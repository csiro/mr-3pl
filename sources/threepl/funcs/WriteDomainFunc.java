package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the write (input) clock domain variable of
 * the argument. The argument may be selectvalue, static, queue or
 * priority mode. For all these modes except mode queue the read and write
 * clock domains are the same.
 */
public class WriteDomainFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the clock frequency
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("writedomain() must have a single argument", loc);

        Ref ref = args.getRef(0, "writedomain()");
        if ((ref.getMode() == Mode.CMEMORY) || (ref.getMode() == Mode.RMEMORY))
            throw new ExEx("writedomain() argument is a memory variable", loc);
        if (ref.getMode() == Mode.CLOCK)
            throw new ExEx("writedomain() argument is a clock variable", loc);
        if (ref.getMode() == Mode.VALUE)
            throw new ExEx("writedomain() argument is a value variable", loc);
        Var clockvar = ref.getWriteClkVar();
        if (clockvar == null)
            return(new Val(loc));   // null
        return(new Val(clockvar, Mode.CLOCK, clockvar.getClkSig(), loc));
    }
}
