package threepl.procs;

import static threepl.ThreePL.sim;
import static threepl.ThreePL.tdelist;

import threepl.ThreePL;
import threepl.codegen.TDE;
import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to write a line to a file during simulation from one
 * or more combinatorial (value) variables. This has 3 or more input
 * arguments and no output arguments. The 1st input argument is the file
 * name. The 2nd input argument is a variable or expression of type log
 * which is true when a write is required. The 3rd and following arguments
 * are variables or expressions whose binary values are are to be written to
 * a line in the output file. These data arguments must not contain queues.
 */
public class SimWriteProc extends InbuiltProc implements Constant, TDEConstants {

    /**
     * Construct the inbuilt procedure simwrite().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public SimWriteProc () {
        allowed_as_param = false;
        check_null_input_args = false;
        check_null_output_args = false;
        target_inline = false;
    }

    /**
     * Execute the procedure simwrite(). This generates executable code,
     * but not an in-line statement.
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
        if (!sim)
            return;
            
        SrcLoc  loc = inargs.getCallLoc();
        Val     val;
        if (inargs.size() < 3)
            throw new ExEx("simwrite() must have 3 or more input arguments", loc);
        if (outargs.size() != 0)
            throw new ExEx("simwrite() must have no output arguments", loc);
        
        // file path string
        val = inargs.getVal(0);
        if (val.getMode() != Mode.IMMEDIATE)
            throw new ExEx("simwrite() 1st argument not immediate mode", loc);
        if (val.getPrimType() != Ptype.STR)
            throw new ExEx("simwrite() 1st argument not a string", loc);
        String name = val.getSingleSval(loc);
        
        // step
        val = inargs.getVal(1);
        if (val.getPrimType() != Ptype.LOG)
            throw new ExEx("simwrite() 2nd argument not type log", loc);
        if (!val.getQueues().isEmpty())
            throw new ExEx("simwrite() 2nd argument cannot contain queues", loc);
        TDEVar  itdev = val.getTDEVar();
        

        TDEVar  clk = ThreePL.getCurrentClock();
        TDE     tde = new TDE(TDEType.SIMWRITE);
        tde.add2p(name);
        tde.add2i(clk);
        tde.add2i(itdev);
        // data inputs
        for (int i=2 ; i<inargs.size() ; i++) {
            Val ival = inargs.getVal(i); 
            if (!ival.getQueues().isEmpty())
                throw new ExEx("simwrite() data argument cannot contain queues", loc);
            itdev = ival.getTDEVar();      
            tde.add2i(itdev);
        }
        tdelist.addTDE(tde);
    }
}
