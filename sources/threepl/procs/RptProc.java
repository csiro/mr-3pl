package threepl.procs;

import static threepl.ThreePL.*;

import threepl.exceptions.ExEx;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to print a message to the report file.
 * This has 1 input argument and no output arguments.
 * The input argument is the message.
 */
public class RptProc extends InbuiltProc implements Constant {

    /**
     * Construct the inbuilt procedure texit().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public RptProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
    }

    /**
     * Execute the procedure msg(). This does not generate executable code.
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
        
        if (inargs.size() != 1)
            throw new ExEx("report() must have 1 input argument", loc);
        if (outargs.size() != 0)
            throw new ExEx("report() cannot have output arguments", loc);
               
        String  msg = inargs.getVal(0).getSingleSval(loc);
        
        rpt(msg);
    }
}
