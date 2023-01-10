package threepl.procs;

import static threepl.ThreePL.*;

import threepl.exceptions.ExEx;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to print a message to standard output and to the report file.
 * This has 1 or 2 input arguments and no output arguments.
 * The first input argument is the message. The optional second argument is a boolean
 * specifying that a stack trace should also be printed.
 */
public class MsgProc extends InbuiltProc implements Constant {

    /**
     * Construct the inbuilt procedure texit().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public MsgProc () {
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
        boolean     trace = false;
        
        if ((inargs.size() == 0) || (inargs.size() > 2))
            throw new ExEx("msg() must have 1 or 2 input arguments", loc);
        if (outargs.size() != 0)
            throw new ExEx("msg() cannot have output arguments", loc);
               
        String  msg = inargs.getVal(0).getSingleSval(loc);
        if (inargs.size() > 1)
            trace = inargs.getVal(0).getSingleLval(loc);
        
        if (trace)
            wmsg(msg, loc);
        else
            msg(msg);
    }
}
