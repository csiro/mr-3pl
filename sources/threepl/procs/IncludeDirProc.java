package threepl.procs;

import threepl.ThreePL;
import threepl.exceptions.ExEx;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to append a directory name to the list of
 * directories searched for included files.
 * This has 1 input argument and no output arguments.
 * The input argument is a string giving the full directory path.
 * A trailing "/" on the directory path string is optional.
 * If the directory path string does not have a leading "/" the
 * path will be relative to the directory containing the initial
 * source file.
 */
public class IncludeDirProc extends InbuiltProc implements Constant {
    /**
     * Construct the inbuilt procedure includedir().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public IncludeDirProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
    }

    /**
     * Execute the procedure includedir(). This does not generate executable code.
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
            throw new ExEx("includedir() must have 1 input argument", loc);
        if (outargs.size() != 0)
            throw new ExEx("includedir() cannot have output arguments", loc);

        String  dir = inargs.getVal(0).getSingleSval(loc);
        
        if (dir.charAt(dir.length()-1) != '/')
            dir += "/";
        ThreePL.include_dirs.add(dir);
    }
}
