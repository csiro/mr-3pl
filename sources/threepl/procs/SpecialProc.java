package threepl.procs;

import static threepl.ThreePL.tdelist;

import threepl.codegen.TDE;
import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.exec.Var.IDtype;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to implement special FPGA-specific code.
 * The first argument is an integer giving the special function type code.
 * The second argument is a format string.
 * The third argument is a port name or null.
 * The remaining arguments are target mode and from these are derived
 * nets to which the constraint applies.
 * 
 * The recognised types so far are -
 *  0 - add a line to the NCF constraints
 *  1 - add a line to the XCD constraints
 */
public class SpecialProc extends InbuiltProc implements Constant, TDEConstants {

    /**
     * Construct the inbuilt procedure special().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public SpecialProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
    }

    /**
     * Execute the procedure special(). This does not generate in-line executable
     * code.
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
        if (inargs.size() < 2)
            throw new ExEx("special() - must have at least 2 arguments", loc);
        TDE         tde = new TDE(TDEType.SPECIAL, loc);
        Val         val;
        Var         var;
        int         function_type;
        String      format;
        String      port = null;
        int         n = inargs.size();
        int         i;
        int         nnets = 0;
        TDEVar      tdev = null;

        // Inputs       
        val = inargs.getVal(0);
        if (val.getMode() != Mode.IMMEDIATE)
                    throw new ExEx("special() - 1st argument not immediate mode", loc);
        if (val.getPrimType() != Ptype.UINT)
                    throw new ExEx("special() - 1st argument not integer", loc);
        function_type = (int)(val.getSingleIval(loc));       
        if (function_type == 100)
            throw new ExEx("special() - function type 100 is debug - not currently active", loc);
        if ((function_type < 0) || (function_type > 1))
            throw new ExEx("special() - invalid function type " + function_type, loc);
        
        switch (function_type) {
        case 0:
        case 1:
            // NCF or XDC constraint - continue
            break;
        case 100:
            throw new ExEx("special() - function type 100 is debug - not currently active", loc);
        default:
            throw new ExEx("special() - invalid function type " + function_type, loc);
        }
        
        // NCF or XDC constraint

        if (outargs.size() != 0)
            throw new ExEx("special() type 0 or 1 - cannot have output arguments");

        val = inargs.getVal(1);
        if (val.getMode() != Mode.IMMEDIATE)
                    throw new ExEx("special() - 2nd argument not immediate mode", loc);
        if (val.getPrimType() != Ptype.STR)
                    throw new ExEx("special() - 2nd argument not string", loc);
        format = val.getSingleSval(loc);       

        val = inargs.getVal(2);
        if (val != null) {
            if (val.getMode() != Mode.IMMEDIATE)
                        throw new ExEx("special() - 3rd argument not immediate mode", loc);
            if (val.getPrimType() != Ptype.STR)
                        throw new ExEx("special() - 3rd argument not string", loc);
            port = val.getSingleSval(loc);
        }
        
        tde.add2p(function_type);
        tde.add2p(format);
        tde.add2p(port);

        for (i=3 ; i<n ; i++) {
            val = inargs.getVal(i);
            if (val == null)
                throw new ExEx("special() - argument " + (i+1) + " is null", loc);
            var = val.getVar();
            /*if ((var != null) && !var.isMatched() && var.isInputPar())
                throw new ExEx("special() - variable '" + var.getId() + "' is unmatched input parameter", loc);*/
            if (!val.getQueues().isEmpty())
                throw new ExEx("special() target argument " + (i+1) + " has queue reads", loc);
            switch (val.getMode()) {
            case IMMEDIATE:
                throw new ExEx("special() - argument " + (i+1) + " not target type", loc);
            case CLOCK:
                var = val.getVar();
                tdev = var.getClkSig();
                if (tdev == null)
                    throw new ExEx("special() - clock variable '" + var.getID(IDtype.CHAIN) + "' is null", loc);
                tde.add2i(tdev);
                nnets++;
                break;
            case STATIC:
            case VALUE:
            case SELECTVALUE:
                tdev = val.getTDEVar();
                tde.add2i(val.getTDEVar());
                nnets++;
                break;
            default:
                throw new ExEx("special() - target argument " + (i+1) + " is disallowed mode" + val.getMode().toString(), loc);
            }
        }       

        /*
        // Outputs
        // Not used for current special functions 0 and 1 but
        // may be used in future.
        WordSpec    ows;
        TDEVar      otdev;
        Val         oval;
        for (i=0 ; i<outargs.size() ; i++) {
            Ref oref = outargs.getRef(i, "special()");
            if ((oref == null) || ((var=oref.getVar()) == null) || var.isOutputPar()) {
                tde.add2o(null);
                continue;
            }
            switch (var.getMode()) {
            case CLOCK:
                tde.add2o(oref.getTDEVar());
                break;
            case VALUE:
                ows = var.getWordSpec();
                otdev = tdelist.signal("E", ows, loc);
                tde.add2o(otdev);
                oval = new Val(null, Mode.VALUE, otdev, loc);
                oref.assignTo(oval, loc);
                break;
            default:
                throw new ExEx("special() - output mode not CLOCK or VALUE", loc);
            }
        }*/
        
        
        // Check the occurrences of %n against the associated input arguments,
        // ensuring that they match in number.
        int iprev = 0;
        int in = 0;
        
        for (;; iprev=i+2) {
            i = format.indexOf('%', iprev);
            if (i < 0) {
                break;
            }
            char    c = format.charAt(i+1);
            if ((c == 'n') || (c == 'N'))
                in++;
        }
        if (in != nnets)
            throw new ExEx("special() type 0 or 1 - %n and %N conversions not matched by same number of target input arguments");
        
        tdelist.addTDE(tde);
    }
}
