package threepl.procs;

import static threepl.ThreePL.tdelist;

import threepl.codegen.TDEConstants;
import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to complete creation of an EDIF element.
 */
public class ElementEndProc extends InbuiltProc implements Constant, TDEConstants {

    /**
     * Construct the inbuilt procedure elementend().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public ElementEndProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
    }

    /**
     * Execute the procedure elementend(), completing construction of a new element and
     * moving its TDE to the TDE list.
     * The input argument is the block name string.
     * If omitted the current uncompleted block will be completed.
     * There are no output arguments.
     * This does not generate in-line executable code.
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
        SrcLoc  loc = inargs.getCallLoc();
        Val     val;
        String  bname = null;
        
        if (inargs.size() > 1)
            throw new ExEx("elementend() - must have zero or one input arguments", loc);
        if (outargs.size() != 0)
            throw new ExEx("elementend() - must have no output arguments", loc);
        
        // optional block identifier
        if (inargs.size() == 1) {
            val = inargs.getVal(0);
            if ((val.getMode() != Mode.IMMEDIATE) || (val.getPrimType() != Ptype.STR))
                throw new ExEx("elementend() - input argument must be a string", loc);
            bname = val.getSingleSval(loc);
        }
        
        tdelist.endElement(bname);
    }
}
