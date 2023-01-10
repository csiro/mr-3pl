package threepl.procs;

import threepl.exceptions.ExEx;
import threepl.exec.FileDesc;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to close a file.
 */
public class OpenProc extends InbuiltProc implements Constant {

    /**
     * Construct the inbuilt procedure open().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public OpenProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
    }

    /**
     * Execute the procedure open(). This does not generate executable code.
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
            throw new ExEx("open() must have two arguments", loc);
        if (outargs.size() != 0)
            throw new ExEx("open() cannot have output arguments", loc);
        
        String  rw;
        boolean output = false;
        boolean append = false;

        Val v1 = inargs.getVal(1);
        if (v1.getPrimType() != Ptype.STR)
            throw new ExEx("open() 2nd argument not string type", loc);
        rw = v1.getArraySval(0, loc);
        if (rw.equals("r"))
            output = false;
        else if (rw.equals("w"))
            output = true;
        else if (rw.equals("a")) {
            output = true;
            append = true;
        } else
            throw new ExEx("open() 2nd argument not \"r\" or \"w\"", loc);

        Val v0 = inargs.getVal(0);
        switch (v0.getPrimType()) {
        case FILE:
            FileDesc fd = v0.getSingleFileVal(loc);
            fd.open("open() - ", output, append, loc);
            return;
        default:
            throw new ExEx("open() argument not a file type", loc);
        }
    }
}
