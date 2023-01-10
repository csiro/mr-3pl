package threepl.procs;

import static threepl.ThreePL.*;

import java.io.PrintStream;

import threepl.exceptions.ExEx;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to print formatted text to standard out.
 */
public class PrintfProc extends InbuiltProc implements Constant {

    /**
     * Construct the inbuilt procedure printlf().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public PrintfProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
    }

    /**
     * Execute the procedure printf(). This does not generate executable code.
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
        if (inargs.size() == 0)
            throw new ExEx("printf() must have one or more input arguments", loc);
        if (outargs.size() != 0)
            throw new ExEx("printf() cannot have output arguments", loc);

        Val     v = inargs.getVal(0);
        if (v.isTarget())
            throw new ExEx("printf() format argument not immediate", loc);
        if (v.getPrimType() != Ptype.STR)
            throw new ExEx("printf() format argument is not a string", loc);
        String  format = v.getSingleSval(loc);

        Object[]   vargs = null;
        if (inargs.size() > 1) {
            v = inargs.getVal(1);
            if ((inargs.size() == 2) && (v.getPrimType() == Ptype.NONE)) {
                if (v.getArrayType().getPrimType() == Ptype.PTR) {
                    // 2nd and last argument is an array of pointers.
                    // Resolve each pointer to print.
                    int n = v.numWords();
                    vargs = new Object[n];
                    for (int i=0 ; i<n ; i++) {
                        Ref ref = (Ref)v.getVal(i);
                        vargs[i] = getArg(ref.getVal(loc), loc);
                    }
                } else
                    throw new ExEx("printf() unprintable type", loc);
            } else {
                vargs = new Object[inargs.size()-1];
                vargs[0]= getArg(v, loc);
                for (int i=2; i<inargs.size() ; i++) {
                    v = inargs.getVal(i);
                    vargs[i-1] = getArg(v, loc);
                }
            }
        }
        
        PrintStream ps = stdoutOpenFile.getPrintStream("printf() - ", loc);
        try {
            ps.printf(format, vargs);
        } catch (Exception e) {
            throw new ExEx("printf() - error - " + e.getMessage(), loc);
        }
    }
    
    private Object getArg (Val v, SrcLoc loc) {
        Ptype pt = v.getPrimType();
        switch (pt) {
        case STR:
            return(v.getSingleSval(loc));
        case INT:
        case UINT:
            return(v.getSingleIval(loc));
        case FLOAT:
            return(v.getSingleFval(loc));
        default:
            throw new ExEx("printf() unprintable type", loc);
        }
    }
}
