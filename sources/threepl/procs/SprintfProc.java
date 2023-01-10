package threepl.procs;

import threepl.exceptions.ExEx;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to copy formatted text to a string.
 */
public class SprintfProc extends InbuiltProc implements Constant {

    /**
     * Construct the inbuilt procedure sprintlf().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public SprintfProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
    }

    /**
     * Execute the procedure sprintf(). This does not generate executable code.
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
            throw new ExEx("sprintf() must have 1 or more input arguments", loc);
        if (outargs.size() != 1)
            throw new ExEx("sprintf() must have one output argument", loc);

        String  s = null;
        Val     v = null;
        
        v = inargs.getVal(0);
        if (v.isTarget())
            throw new ExEx("sprintf() format argument not immediate", loc);
        if (v.getPrimType() != Ptype.STR)
            throw new ExEx("sprintf() format argument is not a string", loc);
        String  format = v.getSingleSval(loc);

        Ref oref = outargs.getRef(0, "sprintf() - ");
        if (oref.isTarget())
            throw new ExEx("sprintf() output argument not immediate", loc);
        if (oref.getPrimType() != Ptype.STR)
            throw new ExEx("sprintf() output argument is not a string", loc);
        
        Object[]   vargs = null;
        if (inargs.size() > 1) {
            v = inargs.getVal(1);
            if ((inargs.size() == 2) && (v.getPrimType() == Ptype.NONE)) {
                v = inargs.getVal(1);
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
                    throw new ExEx("sprintf() unprintable type", loc);
            } else {
                vargs = new Object[inargs.size()-1];
                for (int i=1 ; i<inargs.size() ; i++) {
                    v = inargs.getVal(i);
                    vargs[i-1] = getArg(v, loc);
                }
            }
        }
        
        try {
            s = String.format(format, vargs);
        } catch (Exception e) {
            throw new ExEx("sprintf() - format conversion error - " + e.getMessage(), loc);
        }
        oref.assignTo(AST.IMASS, new Val(s, loc), loc);
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
            throw new ExEx("sprintf() unprintable type", loc);
        }
    }
}
