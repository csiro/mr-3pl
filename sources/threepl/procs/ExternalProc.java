package threepl.procs;

import static threepl.ThreePL.tdelist;

import threepl.codegen.TDE;
import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Clock;
import threepl.exec.Val;
import threepl.nodes.KeyMatchNode;
import threepl.nodes.Node;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * <p>An inbuilt procedure to connect a signal to an external port.</p>
 *
 * <p>This has 3 input arguments.</p>
 *
 * <p>The 1st input argument is a string and is the name of the external port.</p>
 *
 * <p> The 2nd argument is a target mode variable</p>
 *
 * <p>The 3rd argument is "in", "out" or "inout" to indicate port direction.</p>
 */
public class ExternalProc extends InbuiltProc implements Constant, TDEConstants {

    /**
     * Construct the inbuilt procedure external().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public ExternalProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
        ipnames.put("portname", 0);
        ipnames.put("var", 1);
        ipnames.put("direction", 2);
    }

    /**
     * Execute the procedure external(). This does not generate in-line
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
        Node        n;
        if (inargs.size() != 3)
            throw new ExEx("external() must have 3 input arguments", loc);
        if (outargs.size() != 0)
            throw new ExEx("external() must have no output arguments", loc);
        Val         val;
        String      port;
        String      dir;
        int         tcode;
        
        // port name - 1st input argument
        n = inargs.getNode(0);
        if (n instanceof KeyMatchNode)
            throw new ExEx("external() 1st input argument must be a string value", loc);
        val = inargs.getVal(0);
        if (val.getMode() != Mode.IMMEDIATE)
            throw new ExEx("external() 1st input argument must be immediate", loc);
        if (val.getPrimType() != Ptype.STR)
            throw new ExEx("external() 1st input argument must be type str", loc);
        port = val.getSingleSval(loc);
        
        // port direction - 3rd input argument
        n = inargs.getNode(2);
        if (n instanceof KeyMatchNode)
            throw new ExEx("external() 3rd input argument must be a string value", loc);
        val = inargs.getVal(2);
        if (val.getMode() != Mode.IMMEDIATE)
            throw new ExEx("external() 3rd input argument must be immediate", loc);
        if (val.getPrimType() != Ptype.STR)
            throw new ExEx("external() 3rd input argument must be type str", loc);
        dir = val.getSingleSval(loc);
        
        // target variable - 2nd input argument
        n = inargs.getNode(1);
        if (n instanceof KeyMatchNode)
            throw new ExEx("external() 2nd input argument cannot be of form key=value", loc);
        val = n.getVal();
        if (!val.getQueues().isEmpty())
            throw new ExEx("external() 2nd input argument must not have queue reads", loc);
        TDEVar  tdev = val.getTDEVar();
        if (dir.equals("in")) {
            switch (val.getMode()) {
            case VALUE:
                break;
            case CLOCK:
                ((Clock)val.getVar()).setClkSourced();
                break;
            default:
                throw new ExEx("external() - 2nd input argument for input port must be mode VALUE or CLOCK", loc);
            }
            tcode = 0;
        } else if (dir.equals("out")) {
            switch (val.getMode()) {
            case VALUE:
            case STATIC:
            case CLOCK:
                break;
            default:
                throw new ExEx("external() - 2nd input argument for output port must be mode VALUE, STATIC or CLOCK", loc);
            }
            tcode = 1;
        } else
            throw new ExEx("external() 3rd input argument invalid - '" + dir + "' - must be 'in' or 'out'", loc);
        
        TDE tde = new TDE(TDEType.PORT, loc);
        tde.add2p(tcode);
        tde.add2p(port);
        tde.add2i(tdev);
        tdelist.addTDE(tde);



     }
}
