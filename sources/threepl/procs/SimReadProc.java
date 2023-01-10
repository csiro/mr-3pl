package threepl.procs;

import static threepl.ThreePL.sim;
import static threepl.ThreePL.tdelist;

import threepl.ThreePL;
import threepl.codegen.TDE;
import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.exec.WordSpec;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to read a line from a file during simulation
 * and drive one or more combinatorial (value) variables. This has 2
 * input arguments and 1 or more output arguments. The 1st input
 * argument is a variable or expression of type str which gives the
 * file name. The 2nd input argument is a variable or expression of
 * type log which is true when a read is required. The output
 * arguments are value variables which will point to logic sources
 * whose binary values are derived from the line read from the input
 * file.
 */
public class SimReadProc extends InbuiltProc implements Constant, TDEConstants {

    /**
     * Construct the inbuilt procedure simread().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public SimReadProc () {
        allowed_as_param = false;
        check_null_input_args = false;
        check_null_output_args = false;
        target_inline = false;
    }

    /**
     * Execute the procedure simread(). This generates executable code,
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
        if (inargs.size() != 2)
            throw new ExEx("simread() must have 2 input arguments", loc);
        if (outargs.size() == 0)
            throw new ExEx("simread() must have 1 or more output arguments", loc);
        
        // file path string
        val = inargs.getVal(0);
        if (val.getMode() != Mode.IMMEDIATE)
            throw new ExEx("simread() 1st argument not immediate mode", loc);
        if (val.getPrimType() != Ptype.STR)
            throw new ExEx("simread() 1st argument not a string", loc);
        String name = val.getSingleSval(loc);
        
        // step
        val = inargs.getVal(1);
        if (val.getPrimType() != Ptype.LOG)
            throw new ExEx("simread() 2nd argument not type log", loc);
        if (!val.getQueues().isEmpty())
            throw new ExEx("simread() 2nd argument cannot contain queues", loc);
        TDEVar  itdev = val.getTDEVar();
        

        TDEVar  clk = ThreePL.getCurrentClock();
        TDE     tde = new TDE(TDEType.SIMREAD);
        tde.add2p(name);
        tde.add2i(clk);
        tde.add2i(itdev);
        // output value variables
        for (int i=0 ; i<outargs.size() ; i++) {
            Ref         oref = outargs.getRef(i, "simread()"); 
            if (oref.getMode() != Mode.VALUE)
                throw new ExEx("simread() output argument " + (i+1) + " must be value mode", loc);
            WordSpec    ws = oref.getWordSpec();
            TDEVar      otdev = tdelist.signal("E", ws, loc);      
            tde.add2o(otdev);
            oref.assignTo(new Val(null, Mode.VALUE, otdev, loc), loc);
        }
        tdelist.addTDE(tde);
    }
}
