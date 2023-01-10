package threepl.procs;

import static threepl.ThreePL.getCurrentClockVar;
import static threepl.ThreePL.tdelist;

import java.util.HashSet;

import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Clock;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.exec.WordSpec;
import threepl.exec.Var.IDtype;
import threepl.nodes.KeyMatchNode;
import threepl.nodes.Node;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * <p>An inbuilt procedure to connect signals to a compiled module (core).
 * This is necessary if the program is to use an EDIF core. The
 * interface sets up the external connections to the core.</p>
 *
 * <p>This has 1 or more input arguments and optional output arguments.</p>
 *
 * <p>The 1st input argument is a string and is the name of the external macro
 * element.</p>
 *
 * <p> The 2nd argument is a string and provides the netlist block name for
 * the imported module. This argument may be omitted, in which case an arbitrary unique
 * block name will be generated</p>
 *
 * <p>The following input arguments, if any, drive the inputs
 * of the external element. Each argument must be of the form port=var where the
 * LHS is the port name and the RHS is an expression or a value, static, priority
 * or clock variable.</p>
 *
 * <p>The output arguments, if any, are output connections to the
 * external element. Each argument must be of the form port=rhs where the LHS
 * is the port name and the RHS is a variable of mode value, selectvalue
 * or clock.</p>
 * The expression must not contain queue reads.
 */
public class ImportProc extends InbuiltProc implements Constant, TDEConstants {

    /**
     * Construct the inbuilt procedure import().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public ImportProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = true;
        target_inline = false;
    }

    /**
     * Execute the procedure import(). This does not generate in-line
     * executable code.
     * @param   inargs is a list of input argument tree nodes
     * @param   outargs is a list of output argument tree nodes
     * @param   toplevel is true if we are at the module, procedure or function
     *          level
     */
    @SuppressWarnings("incomplete-switch")
    public void execute (
        NodeList    inargs, 
        NodeList    outargs, 
        boolean     toplevel
    ) {
        SrcLoc      loc = inargs.getCallLoc();
        Node        n;
        if (inargs.size() == 0)
            throw new ExEx("import() must have at least 1 input argument", loc);
        Val         val;
        String          cname;
        String          bname = null;
        int             tcode;
        String          portname = null;
        Ref             ref = null;
        Var             var = null;
        HashSet<String> ports = new HashSet<String>();
        
        // core name - 1st input argument
        n = inargs.getNode(0);
        if (n instanceof KeyMatchNode)
            throw new ExEx("import() 1st input argument must be a string value", loc);
        val = inargs.getVal(0);
        if (val.getMode() != Mode.IMMEDIATE)
            throw new ExEx("import() 1st input argument must be immediate", loc);
        if (val.getPrimType() != Ptype.STR)
            throw new ExEx("import() 1st input argument must be type str", loc);
        cname = val.getSingleSval(loc);
        
        // 2nd input argument is optional netlist block name or null
        if (inargs.size() > 1) {
            n = inargs.getNode(1);
            if (n != null) {
                val = inargs.getVal(1);
                if (val.getMode() != Mode.IMMEDIATE)
                    throw new ExEx("import() optional 2nd input argument must be immediate", loc);
                if (val.getPrimType() != Ptype.STR)
                    throw new ExEx("import() optional 2nd input argument must be type str", loc);
                bname = val.getSingleSval(loc);
            }
        }
        
        tdelist.newElement(bname, cname);

        // Remaining input arguments - inputs to interface
        for (int i=2 ; i<inargs.size() ; i++) {
            n = inargs.getNode(i);
            if (n == null)
                throw new ExEx("import() null argument", loc);
            if (n instanceof KeyMatchNode) {
                portname = ((KeyMatchNode)n).getParamKey();
                n = n.getSubNode(0);
                val = n.getVal();
            } else {
                val = n.getVal();
                var = val.getVar();
                if (var == null)
                    throw new ExEx("import() non-keymatch input argument " + i + " not a variable");
                portname = var.getID(IDtype.LITERAL);
            }
            if (!val.getQueues().isEmpty())
                throw new ExEx("import() input argument " + i + " must not have queue reads", loc);
            if (ports.contains(portname))
                throw new ExEx("port name '" + portname + "' duplicated", loc);
            else
                ports.add(portname);
            TDEVar  tdev = val.getTDEVar();
            boolean isarray = tdev.getWordSpec().getType().getPrimType() != Ptype.LOG;
            switch (val.getMode()) {
            case INPUT:
            case VALUE:
            case SELECTVALUE:
            case STATIC:
            case PRIORITY:
                tcode = 0;
                break;
            case CLOCK:
                tcode = 3;
                isarray = false;
                break;
            default:
                throw new ExEx("import() - input argument " + i + " must be mode INPUT, VALUE, SELECTVALUE, STATIC, PRIORITY or CLOCK", loc);
            }

            int arraysize = isarray ? tdev.numBits() : 0;
            int arrayformat = isarray ? 2 : 0;
            tdelist.addPort(null, portname, tcode, tdev, arraysize, arrayformat);
        }

        // Output arguments - outputs from interface
        for (int i=0 ; i<outargs.size() ; i++) {
            n = outargs.getNode(i);
            if (n instanceof KeyMatchNode) {
                portname = ((KeyMatchNode)n).getParamKey();
                if (ports.contains(portname))
                    throw new ExEx("port name '" + portname + "' duplicated", loc);
                else
                    ports.add(portname);
                n = n.getSubNode(0);
                ref = n.getRef("import()");
                var = ref.getVar();
                if (var == null)
                    throw new ExEx("import() input argument for port '" + portname + "' is not a variable", loc);
            } else {
                ref = n.getRef("import()");
                var = ref.getVar();
                if (var == null)
                    throw new ExEx("import() input argument " + (i+1) + " is not a variable", loc);
                portname = var.getID(IDtype.LITERAL);
            }
            var = ref.getVar();
            if (var == null)
                throw new ExEx("import() output argument is not a variable", loc);
            TDEVar  tdev;
            switch (ref.getMode()) {
            case VALUE:
            case SELECTVALUE:
                Clock dclock = ref.getReadClkVar();
                if (dclock == null) {
                    dclock = getCurrentClockVar();
                    switch (ref.getMode()) {
                    case VALUE:
                        var.setOutputClock(dclock, Calloc.IMPORT, false, loc);
                        break;
                    case SELECTVALUE:
                        var.setOutputClock(dclock, Calloc.IMPORT, false, loc);
                        break;
                    }
                }
                break;
            case CLOCK:
                break;
            default:
                throw new ExEx("import() output argument, port '" + portname + "', not allowed mode", loc);
            }
            tdev = null;
            switch (ref.getMode()) {
            case VALUE:
                WordSpec    ws = ref.getWordSpec();
                tdev = tdelist.signal("IN", ws, loc);
                ref.assignTo(new Val(null, Mode.VALUE, tdev, loc), loc);
                tcode = 1;
                break;
            case SELECTVALUE:
                tdev = ref.getTDEVar();
                tcode = 2;
                break;
            case CLOCK:
                tdev = var.getClkSig();
                tcode = 1;
                break;
            default:
                throw new ExEx("import() output argument, port '" + portname + "', not allowed mode", loc);
            }
            boolean isarray = tdev.getWordSpec().getType().getPrimType() != Ptype.LOG;
            int arraysize = isarray ? tdev.numBits() : 0;
            int arrayformat = isarray ? 2 : 0;
            tdelist.addPort(null, portname, tcode, tdev, arraysize, arrayformat);
        }
        tdelist.endElement(null);
    }
}
