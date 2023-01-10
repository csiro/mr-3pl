package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Memory;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the port clock domain variable of
 * the first (memory) argument.
 * The first argument must be cmemory or rmemory mode.
 * The second argument is the port number.
 */
public class PortDomainFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the clock frequency
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        Var     clockvar;

        if (args.size() != 2)
            throw new ExEx("portdomain() must have two arguments", loc);

        Ref ref = args.getRef(0, "readdomain()");
        if ((ref.getMode() != Mode.CMEMORY) && (ref.getMode() != Mode.RMEMORY))
            throw new ExEx("portdomain() argument is not a memory variable", loc);
        
        Val val = args.getVal(1);
        int port = (int)val.getSingleIval(loc);
               
        Memory mvar = (Memory) ref.getVar();
        clockvar = mvar.getMemClkVar(port);
        if (clockvar == null)
            return(new Val(loc));
        return(new Val(clockvar, Mode.CLOCK, clockvar.getClkSig(), loc));
    }
}
