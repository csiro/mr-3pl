package threepl.procs;

import java.io.BufferedReader;
import java.io.IOException;

import threepl.exceptions.ExEx;
import threepl.exec.FileDesc;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;
import threepl.parser.Functions;


/**
 * An inbuilt procedure to scan an input file converting delimited fields to types specified
 * by a format string.
 */
public class FscanfProc extends InbuiltProc implements Constant {

    /**
     * Construct the inbuilt procedure scanf().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public FscanfProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = true;
        target_inline = false;
    }

    /**
     * Execute the variable declaration procedure fscanf().
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
            throw new ExEx("fscanf() - must have two input arguments", loc);
        if (outargs.size() == 0)
            throw new ExEx("fscanf() - must have one or more output arguments", loc);        
        
        Val     fileval = inargs.getVal(0);
        if (fileval.getMode() != Mode.IMMEDIATE)
            throw new ExEx("fscanf() - file input argument not immediate mode", loc);
        if (fileval.getPrimType() != Ptype.FILE)
            throw new ExEx("fscanf() - file input argument not a file", loc);
        FileDesc infile = fileval.getSingleFileVal(loc);
        if (infile == null)
            throw new ExEx("fscanf() - file is not open", loc);
        BufferedReader br = infile.getBufferedReader("fscanf() - ", loc);
        if (br == null)
            throw new ExEx("fscanf() - file is not open for reading", loc);
        
        String format = null;
        Val     fval = inargs.getVal(1);
        if (fval != null) {
            if (fval.getMode() != Mode.IMMEDIATE)
                throw new ExEx("fscanf() - format input argument not immediate mode", loc);
            if (fval.getPrimType() != Ptype.STR)
                throw new ExEx("fscanf() - format input argument not a string", loc);
            format = fval.getSingleSval(loc);
        }

        Ref[]   refs = new Ref[outargs.size()];
        for (int i=0 ; i<outargs.size(); i++)
            refs[i] = outargs.getRef(i, "fscanf() - ");
        
        String  line = null;
        try {
            line = br.readLine();
        } catch (IOException e) {
            throw new ExEx("fscanf() - file read error on file '" + infile.getPath() + "'", loc);
        }
        if (line == null) {
            infile.setEOF();
            return;
        }
        
        Functions.scan (line, format, refs, "fscanf() - ", loc);
    }
}
