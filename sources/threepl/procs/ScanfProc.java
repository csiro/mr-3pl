package threepl.procs;

import static threepl.ThreePL.*;

import java.io.IOException;

import threepl.exceptions.ExEx;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.Functions;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to scan standard input converting delimited fields to types specified
 * by a format string.
 */
public class ScanfProc extends InbuiltProc implements Constant {

    /**
     * Construct the inbuilt procedure scanf().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public ScanfProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = true;
        target_inline = false;
    }

    /**
     * Execute the variable declaration procedure scanf().
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
            throw new ExEx("scanf() must have one input argument", loc);
        if (outargs.size() == 0)
            throw new ExEx("scanf() must have one or more output arguments", loc);        
        
        Val     fval = inargs.getVal(0);
        if (fval.getMode() != Mode.IMMEDIATE)
            throw new ExEx("scanf() format input argument not immediate mode", loc);
        if (fval.getPrimType() != Ptype.STR)
            throw new ExEx("scanf() format input argument not a string", loc);
        String format = fval.getSingleSval(loc);

        Ref[]   refs = new Ref[outargs.size()];
        for (int i=0 ; i<outargs.size(); i++)
            refs[i] = outargs.getRef(i, "scanf() - ");
        
        String  line = null;
        try {
            line = stdinOpenFile.getBufferedReader("scanf() - ", loc).readLine();
        } catch (IOException e) {
            throw new ExEx("scanf() read eroor on stdin", loc);
        }
        
        if (line == null) {
            stdinOpenFile.setEOF();
            return;
        }
        
        Functions.scan (line, format, refs, "fscanf() - ", loc);
    }
}
