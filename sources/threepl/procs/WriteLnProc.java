package threepl.procs;

import java.io.PrintStream;

import threepl.exceptions.ExEx;
import threepl.exec.FileDesc;
import threepl.exec.Val;
import threepl.exec.Var.IDtype;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to write a line to an output file.
 * This has 2 input arguments and no output arguments.
 * The 1st input argument is the open output file.
 * The 2nd input argument is the string to be written.
 */
public class WriteLnProc extends InbuiltProc implements Constant {

    /**
     * Construct the inbuilt procedure writeln().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public WriteLnProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
    }

    /**
     * Execute the procedure writeln(). This does not generate executable code.
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
            throw new ExEx("writeln() - must have a 2 input arguments", loc);
        if (outargs.size() != 0)
            throw new ExEx("writeln() - must have no output arguments", loc);

        // file variable
        PrintStream ps = null;
        Val fval = inargs.getVal(0);
        if (fval.getMode() != Mode.IMMEDIATE)
            throw new ExEx("writeln() - argument not a file variable", loc);
        if (fval.getPrimType() != Ptype.FILE)
            throw new ExEx("writeln() - argument not a file variable", loc);
        FileDesc file = fval.getSingleFileVal(loc);
        if (file == null)
            throw new ExEx("writeln() - file is not open", loc);
        ps = file.getPrintStream("writeln() - ", loc);
        if (ps == null)
            throw new ExEx("writeln() - file is not open for writing", loc);
        
        // string to write
        Val sval = inargs.getVal(1);
        if (sval.getMode() != Mode.IMMEDIATE)
            throw new ExEx("writeln() - argument not immediate mode", loc);
        String  s = null;
        switch (sval.getPrimType()) {
        case BITS:
        case UINT:
        case INT:
            s = Long.toString(sval.getSingleIval(loc));
            break;
        case LOG:
            if (sval.getSingleLval(loc))
                s = "true";
            else
                s = "false";
            break;
        case STR:
            s = sval.getSingleSval(loc);
            break;
        case ENUM:
            s = sval.getVar().getID(IDtype.LITERAL);
            break;
        case FLOAT:
            s = Double.toString(sval.getSingleFval(loc));
            break;
        default:
            throw new ExEx("writeln() - argument not allowed type", loc);
        }

        ps.println(s);
    }
}
