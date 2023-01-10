package threepl.procs;

import java.io.PrintStream;

import threepl.exceptions.ExEx;
import threepl.exec.FileDesc;
import threepl.exec.Ref;
import threepl.exec.SubFieldList;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to print formatted text to a file.
 */
public class FprintfProc extends InbuiltProc implements Constant {

    /**
     * Construct the inbuilt procedure fprintlf().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public FprintfProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
    }

    /**
     * Execute the procedure fprintf(). This does not generate executable code.
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
        if (inargs.size() < 1)
            throw new ExEx("fprintf() - must have 1 or more input arguments", loc);
        if (outargs.size() != 1)
            throw new ExEx("fprintf() - must have one output argument", loc);

        Val     v = null;
        Ref oref = outargs.getRef(0, "fprintf() - ");
        if (oref.isTarget())
            throw new ExEx("fprintf() - file argument not immediate", loc);
        if (oref.getPrimType() != Ptype.FILE)
            throw new ExEx("fprintf() file argument is not a file", loc);
        v = oref.getVar().getVal(null, new SubFieldList(), loc);
        FileDesc  file = v.getSingleFileVal(loc);
        if (file == null)
            throw new ExEx("fprintf() - file is not open", loc);
        PrintStream ps = file.getPrintStream("fprint() - ", loc);
        if (ps == null)
            throw new ExEx("fprintf() - file is not open for writing", loc);
        
        v = inargs.getVal(0);
        if (v.isTarget())
            throw new ExEx("fprintf() - format argument not immediate", loc);
        if (v.getPrimType() != Ptype.STR)
            throw new ExEx("fprintf() - format argument is not a string", loc);
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
                    throw new ExEx("fprintf() - unprintable type", loc);
            } else {
                vargs = new Object[inargs.size()-1];
                vargs[0] = getArg(v, loc);
                for (int i=2 ; i<inargs.size() ; i++) {
                    v = inargs.getVal(i);
                    vargs[i-1] = getArg(v, loc);
                }
            }
        }
        
        try {
            ps.printf(format, vargs);
        } catch (Exception e) {
            throw new ExEx("fprintf() - format conversion error - " + e.getMessage(), loc);
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
            throw new ExEx("fprintf() - unprintable type", loc);
        }
    }
}
