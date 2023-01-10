package threepl.procs;

import threepl.ThreePL;
import threepl.exceptions.ExEx;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to print a stack trace.
 * This has no input or output arguments.
 */
public class TraceProc extends InbuiltProc implements Constant {

    /**
     * Construct the inbuilt procedure trace().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public TraceProc () {
        allowed_as_param = false;
        check_null_input_args = false;
        check_null_output_args = false;
        target_inline = false;
    }

    /**
     * Execute the procedure trace(). This does not generate executable code.
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
        if (inargs.size() != 0)
            throw new ExEx("trace() cannot have input arguments", loc);
        if (outargs.size() != 0)
            throw new ExEx("trace() cannot have output arguments", loc);
         
        System.out.println("    " + loc.toString() + ThreePL.stackTrace());
    }
}
