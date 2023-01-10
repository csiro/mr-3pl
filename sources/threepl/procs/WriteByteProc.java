package threepl.procs;

import java.io.FileOutputStream;
import java.io.IOException;
import threepl.exceptions.ExEx;
import threepl.exec.FileDesc;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to write a byte to an output file.
 * This has 2 input arguments and no output arguments.
 * The 1st input argument is the open output file.
 * The 2nd input argument is the integer to be written.
 */
public class WriteByteProc extends InbuiltProc implements Constant {

    /**
     * Construct the inbuilt procedure writeln().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public WriteByteProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
    }

    /**
     * Execute the procedure writebyte(). This does not generate executable code.
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
            throw new ExEx("writebyte() - must have a 2 input arguments", loc);
        if (outargs.size() != 0)
            throw new ExEx("writebyte() - must have no output arguments", loc);

        // file variable
        Val fval = inargs.getVal(0);
        if (fval.getMode() != Mode.IMMEDIATE)
            throw new ExEx("writebyte() - argument not a file variable", loc);
        if (fval.getPrimType() != Ptype.FILE)
            throw new ExEx("writebyte() - argument not a file variable", loc);
        FileDesc file = fval.getSingleFileVal(loc);
        if (file == null)
            throw new ExEx("writebyte() - file is not open", loc);
        FileOutputStream    fos = file.getFileOutputStream("writebyte() - ", loc);
        if (fos == null)
            throw new ExEx("writebyte() - file is not open for writing", loc);
        
        // string to write
        Val sval = inargs.getVal(1);
        if (sval.getMode() != Mode.IMMEDIATE)
            throw new ExEx("writebyte() - argument not immediate mode", loc);
        long  l;
        switch (sval.getPrimType()) {
        case BITS:
        case UINT:
        case INT:
            l = sval.getSingleIval(loc);
            break;
        case LOG:
            if (sval.getSingleLval(loc))
                l = 1;
            else
                l = 0;
            break;
        default:
            throw new ExEx("writebyte() - argument not allowed type", loc);
        }
        if (l < 0)
            throw new ExEx("writebyte() - argument value negative", loc);
        if (l > 255)
            throw new ExEx("writebyte() - argument value > 255", loc);

        try {
            fos.write((int)l);
        } catch (IOException e) {
            throw new ExEx("writebyte() - write error", loc);
        }
    }
}
