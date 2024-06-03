package threepl.procs;

import static threepl.ThreePL.tdelist;

import java.util.HashSet;

import threepl.codegen.TDE;
import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.*;
import threepl.exec.Var.IDtype;
import threepl.nodes.KeyMatchNode;
import threepl.nodes.Node;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * <p>An inbuilt procedure to connect signals to interface ports.
 * This is necessary if the program is to create an EDIF core. The
 * interface gives the external connections to the core.</p>
 *
 * <p>This has a 1 or more input arguments and optional output arguments.</p>
 *
 * <p>The 1st input argument is a string and is the name of this macro
 * element.</p>
 *
 * <p>The 2nd and following input arguments, if any, are input connections from the
 * interface. Each argument must be of the form port=var where the LHS
 * is the port name and the RHS is a value or clock variable.</p>
 *
 * <p>The output arguments, if any, are output connections to the
 * interface. Each argument must be of the form port=rhs where the LHS
 * is the port name and the RHS is an expression or a variable of mode value,
 * selectvalue, static, priority or clock.</p>
 * The expression must not contain queue reads.
 *
 * <p>There must be no null arguments.</p>
 */
public class ExportProc extends InbuiltProc implements Constant, TDEConstants {

    /**
     * Construct the inbuilt procedure export().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public ExportProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = true;
        target_inline = false;
    }

    /**
     * Execute the procedure export(). This does not generate in-line
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
        SrcLoc          loc = inargs.getCallLoc();
        Node            n;
        String          portname = null;
        Ref             ref = null;
        Val             val = null;
        Var             var = null;
        HashSet<String> ports = new HashSet<String>();

        // Input arguments - inputs from interface
        for (int i=0 ; i<inargs.size() ; i++) {
            n = inargs.getNode(i);
            if (n == null)
                throw new ExEx("export() null argument", loc);
            if (n instanceof KeyMatchNode) {
                portname = ((KeyMatchNode)n).getParamKey();
                n = n.getSubNode(0);
                ref = n.getRef("export()");
                var = ref.getVar();
            } else {
                ref = n.getRef("export()");
                var = ref.getVar();
                if (var != null)
                    portname = var.getID(IDtype.LITERAL);
            }
            if (var == null)
                throw new ExEx("export() input argument for port '" + portname + "' is not a variable", loc);
            if (ports.contains(portname))
                throw new ExEx("port name '" + portname + "' duplicated", loc);
            else
                ports.add(portname);
            TDEVar  tdev;

            switch (ref.getMode()) {
            case VALUE:
                WordSpec    ws = ref.getWordSpec();
                tdev = tdelist.signal("IN", ws, loc);
                ref.assignTo(new Val(null, Mode.VALUE, tdev, loc), loc);
                break;
            case CLOCK:
                tdev = var.getClkSig();
                ((Clock)var).setClkSourced();
                break;
            default:
                throw new ExEx("export() input argument for port '" + portname + "' must be mode VALUE or CLOCK", loc);
            }

            TDE tde = new TDE(TDEType.PORT, loc);
            tde.add2p(0);   // should this be 3 for a clock?
            tde.add2p(portname);
            tde.add2i(tdev);
            tdelist.addTDE(tde);
        }

        // Output arguments - outputs to interface
        for (int i=0 ; i<outargs.size() ; i++) {
            n = outargs.getNode(i);
            if (n instanceof KeyMatchNode) {
                portname = ((KeyMatchNode)n).getParamKey();
                n = n.getSubNode(0);
                val = n.getVal();
            } else {
                val = n.getVal();
                var = val.getVar();
                if (var == null)
                    throw new ExEx("export() non-keymatch output argument '" + (i+1) + " is not a variable", loc);
                portname = var.getID(IDtype.LITERAL);
            }
            if (!val.getQueues().isEmpty())
                throw new ExEx("export() output argument for port '" + portname + "' must not have queue reads", loc);
            if (ports.contains(portname))
                throw new ExEx("port name '" + portname + "' duplicated", loc);
            else
                ports.add(portname);
            TDEVar  tdev = val.getTDEVar();
            int tcode = 1;
            switch (val.getMode()) {
            case VALUE:
            case STATIC:
            case PRIORITY:
            case CLOCK:
                break;
            case SELECTVALUE:
                tcode = 2;
                break;
            default:
                throw new ExEx("export() output argument for port '" + portname + "' must be mode VALUE, SELECTVALUE, STATIC, PRIORITY or CLOCK", loc);
            }
            TDE tde = new TDE(TDEType.PORT, loc);
            tde.add2p(tcode);
            tde.add2p(portname);
            tde.add2i(tdev);
            tdelist.addTDE(tde);
        }
    }
}
