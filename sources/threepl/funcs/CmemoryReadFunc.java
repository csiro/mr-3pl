package threepl.funcs;

import static threepl.ThreePL.getCurrentClockVar;

import java.util.HashSet;

import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Memory;
import threepl.exec.QueueRefs;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt function to read from a combinatorial output RAM block.
 * This has 3 arguments.
 *
 * <p>The 1st argument is the memory mode variable.
 *
 * <p>The 2nd argument is the port.
 *
 * <p>The 3rd argument is the address signal.
 *
 * Expressions containing a call to this function cannot be passed
 * as arguments to modules, procedures or functions.
 */
public class CmemoryReadFunc extends InbuiltFunc implements Constant {
    
    public CmemoryReadFunc () {
        ipnames.put("id", 0);
        ipnames.put("port", 1);
        ipnames.put("address", 2);
    }

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the read output
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        if (args.size() != 3)
            throw new ExEx("cmemoryread() must have 3 input arguments", loc);

        // identifier
        Ref     ref = args.getRef(0, "cmemoryread()");
        if (ref.getMode() != Mode.CMEMORY)
            throw new ExEx("cmemoryread() 1st argument is not a cmemory variable", loc);
        Memory  mvar = (Memory)ref.getVar();
        
        // port
        Val     pval = args.getVal(1);
        int     port = (int)pval.getSingleIval(loc);
        
        // address
        Val     addr = args.getVal(2);
        QueueRefs queues = new QueueRefs();
        HashSet<TDEVar> s = mvar.memRead(port, null, addr, queues, null, "cmemoryread()", loc);
        addr.resolveClocks(null, Calloc.VALUE, loc);

        TDEVar  tdev = mvar.getPortOutput(port, getCurrentClockVar(), "cmemoryread()", loc);
        tdev.setClkVar(addr.getReadClkVar());

        Val     retval = new Val(null, Mode.VALUE, tdev, loc);
        retval.addExecSet(s);
        retval.addExecSets(addr);
        retval.andSetQueues(queues, loc);   // collect queue references
        retval.addOVars(addr);
        return(retval);
    }
}
