package threepl.procs;

import static threepl.ThreePL.*;

import threepl.codegen.TDEConstants;
import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to create an EDIF element.
 */
public class ElementProc extends InbuiltProc implements Constant, TDEConstants {
    private static int      bname_counter;
    
    /**
     * Construct the inbuilt procedure element().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public ElementProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
    }

    /**
     * Execute the procedure element().
     * The first input argument is the element name string.
     * The second optional argument is a unique block name string for use in the netlist.
     * If this is not supplied the block will be given a unique name automatically.
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
        String  ename;
        
        if (inargs.size() > 2)
            throw new ExEx("element() - must have one or two input arguments", loc);
        if (outargs.size() != 0)
            throw new ExEx("element() - must have no output arguments", loc);
        
        // element name
        if (inargs.isNull(0) || ((val=inargs.getVal(0)) == null))
            return;                     // no element name argument
        if ((val.getMode() != Mode.IMMEDIATE) || (val.getPrimType() != Ptype.STR))
            throw new ExEx("element() - 1st input argument must be a string", loc);
        ename = val.getSingleSval(loc);
        
        // optional block identifier
        if (inargs.size() == 2) {
            val = inargs.getVal(1);
            if ((val.getMode() != Mode.IMMEDIATE) || (val.getPrimType() != Ptype.STR))
                throw new ExEx("element() - 2nd input argument must be a string", loc);
            bname = val.getSingleSval(loc);
        }
        
        if(bname == null)
            bname = "e" + bname_counter++;  // generate an element name

        ce_name = bname;            // save the current element name
        
        tdelist.newElement(bname, ename);
    }
}
