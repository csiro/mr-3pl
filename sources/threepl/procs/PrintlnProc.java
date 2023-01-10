package threepl.procs;

import static threepl.ThreePL.stdoutOpenFile;

import java.io.PrintStream;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to print a line of text to standard out.
 */
public class PrintlnProc extends InbuiltProc implements Constant {

    /**
     * Construct the inbuilt procedure println().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public PrintlnProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
    }

    /**
     * Execute the procedure println(). This does not generate executable code.
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
            throw new ExEx("println() must have single argument", loc);
        if (outargs.size() != 0)
            throw new ExEx("println() cannot have output arguments", loc);

        Val     v = inargs.getVal(0);
        if (v.isTarget())
            throw new ExEx("println() cannot print a target variable or expression", loc);
        if (v.numWords() > 1)
            throw new ExEx("println() cannot print a non-primitive", loc);
        Ptype pt = v.getPrimType();
        switch (pt) {
        case STR:
        case INT:
        case UINT:
        case FLOAT:
        case LOG:
        case ENUM:
            PrintStream ps = stdoutOpenFile.getPrintStream("println() - ", loc);
            try {
                ps.println(v.forceSingleSval(loc));
            } catch (Exception e) {
                throw new ExEx("println() - error - " + e.getMessage(), loc);
            }
            return;
        default:
            throw new ExEx("println() unprintable type", loc);
        }
    }
}
