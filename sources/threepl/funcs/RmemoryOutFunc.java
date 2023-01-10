package threepl.funcs;

import static threepl.ThreePL.getCurrentClockVar;

import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Clock;
import threepl.exec.Memory;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt function to get the output TDEVar from a registered output
 * memory.
 * This has 2 arguments.
 *
 * <p>The 1st argument is the memory mode variable.
 *
 * <p>The 2nd argument is the port number.
 */
public class RmemoryOutFunc extends InbuiltFunc implements Constant {
    
    public RmemoryOutFunc () {
        ipnames.put("mem", 0);
        ipnames.put("port", 1);
    }

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the registered RAM output
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        if (args.size() != 2)
            throw new ExEx("rmemoryOut() must have 2 input arguments", loc);
        
        // identifier
        Ref     ref = args.getRef(0, "rmemoryOut()");
        if (ref.getMode() != Mode.RMEMORY)
            throw new ExEx("rmemoryOut() 1st argument is not an rmemory variable", loc);
        Memory  var = (Memory)ref.getVar();
        
        // port
        Val     pval = args.getVal(1);
        int     port = (int)pval.getSingleIval(loc);

        // domain clock variable
        Clock dclock = getCurrentClockVar();

        TDEVar  t = var.getPortOutput(port, dclock, "rmemoryOut()", loc);

        Val     oval = new Val(null, Mode.VALUE, t, loc);
        oval.addOVar(dclock);
        return(oval);
    }
}
