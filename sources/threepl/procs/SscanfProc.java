package threepl.procs;

import threepl.exceptions.ExEx;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;
import threepl.parser.Functions;


/**
 * An inbuilt procedure to scan a string converting delimited fields to types specified
 * by a format string.
 */
public class SscanfProc extends InbuiltProc implements Constant {

    /**
     * Construct the inbuilt procedure scanf().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public SscanfProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = true;
        target_inline = false;
    }

    /**
     * Execute the variable declaration procedure sscanf().
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
        if (inargs.size() != 2)
            throw new ExEx("sscanf() must have two input arguments", loc);
        if (outargs.size() == 0)
            throw new ExEx("sscanf() must have one or more output arguments", loc);        
        
        Val     strval = inargs.getVal(0);
        if (strval.getMode() != Mode.IMMEDIATE)
            throw new ExEx("sscanf() file input argument not immediate mode", loc);
        if (strval.getPrimType() != Ptype.STR)
            throw new ExEx("sscanf() file input argument not type 'str'", loc);
        String line = strval.getSingleSval(loc);
       
        String format = null;
        Val     fval = inargs.getVal(1);
        if (fval != null) {
            if (fval.getMode() != Mode.IMMEDIATE)
                throw new ExEx("sscanf() format input argument not immediate mode", loc);
            if (fval.getPrimType() != Ptype.STR)
                throw new ExEx("sscanf() format input argument not a string", loc);
            format = fval.getSingleSval(loc);
        }

        Ref[]   refs = new Ref[outargs.size()];
        for (int i=0 ; i<outargs.size(); i++)
            refs[i] = outargs.getRef(i, "sscanf() - ");
        
        Functions.scan (line, format, refs, "sscanf()", loc);
    }
}
