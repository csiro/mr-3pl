package threepl.procs;

import threepl.ThreePL;
import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt procedure to change the design name, overriding that derived
 * from the file name but not one entered as a command line argument.
 */
public class DesignNameProc extends InbuiltProc implements Constant {

    /**
     * Construct the inbuilt procedure designname().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public DesignNameProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
    }
    
    /**
     * Execute the procedure designname(). This does not generate in-line
     * executable code.
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
            throw new ExEx("designname() must have 1 input argument", loc);
        if (outargs.size() != 0)
            throw new ExEx("designname() cannot have output arguments", loc);

        Val     v = inargs.getVal(0);
        ThreePL.changeDesign(v.getSingleSval(loc));
    }
}
