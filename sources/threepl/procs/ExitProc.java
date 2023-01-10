package threepl.procs;

import threepl.ThreePL;
import threepl.exceptions.ExEx;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to terminate execution.
 * This has 0 or 1 input arguments and no output arguments.
 * The optional input argument is a termination message.
 */
public class ExitProc extends InbuiltProc implements Constant {

    /**
     * Construct the inbuilt procedure exit().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public ExitProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
    }

    /**
     * Execute the procedure exit(). This does not generate executable code.
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
        SrcLoc      loc = inargs.getCallLoc();
        if (inargs.size() > 1)
            throw new ExEx("exit() must have 0 or 1 input arguments", loc);
        if (outargs.size() != 0)
            throw new ExEx("exit() cannot have output arguments", loc);
               
        if (inargs.size() > 0)
            System.out.println(inargs.getVal(0).getSingleSval(loc));
        System.out.println(ThreePL.design_name + ": Execution terminated");
        System.exit(1);
    }
}
