package threepl.procs;

import static threepl.ThreePL.tdelist;

import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to connect two signals. This is intended solely
 * for use where the outputs of basic library elements are to be connected
 * on 3-state nets, e.g. BUFT, BUFE, PULLUP, PULLDOWN, KEEPER.
 * The input argument is the source and the output argument is the sink.
 */
public class ConnectProc extends InbuiltProc implements Constant, TDEConstants {

    /**
     * Construct the inbuilt procedure connect().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public ConnectProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = true;
        target_inline = false;
    }

    /**
     * Execute the procedure connect(). This does not generate in-line
     * executable code.
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
            throw new ExEx("connect() must have one input arguments", loc);
        if (outargs.size() != 1)
            throw new ExEx("connect() must have one output arguments", loc);

        Val valsink = outargs.getVal(0);
        if (valsink.getMode() != Mode.VALUE)
            throw new ExEx("connect() input argument is not value mode", loc);
        Val valsrc = inargs.getVal(0);
        if (valsrc.getMode() != Mode.VALUE)
            throw new ExEx("connect() output argument is not value mode", loc);
        TDEVar  tdevsink = valsink.getTDEVar();
        TDEVar  tdevsrc = valsrc.getTDEVar();
        tdelist.connect(tdevsink, tdevsrc);
    }
}
