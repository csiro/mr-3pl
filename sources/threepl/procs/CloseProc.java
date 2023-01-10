package threepl.procs;

import java.io.IOException;

import threepl.exceptions.ExEx;
import threepl.exec.FileDesc;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to close a file.
 */
public class CloseProc extends InbuiltProc implements Constant {

    /**
     * Construct the inbuilt procedure close().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public CloseProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
    }

    /**
     * Execute the procedure close(). This does not generate executable code.
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
            throw new ExEx("close() must have single argument", loc);
        if (outargs.size() != 0)
            throw new ExEx("close() cannot have output arguments", loc);

        Val v = inargs.getVal(0);
        if (v.isTarget())
            throw new ExEx("close() argument cannot be a target mode", loc);
        if (v.numWords() > 1)
            throw new ExEx("close() argument cannot be an array", loc);
        switch (v.getPrimType()) {
        case FILE:
            FileDesc file = v.getSingleFileVal(loc);
            try {
                file.close();
            } catch (IOException e) {
                throw new ExEx("close() error closing file " + file.getPath(), loc);
            }
            return;
        default:
            throw new ExEx("close() argument not a file type", loc);
        }
    }
}
