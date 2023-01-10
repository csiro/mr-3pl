package threepl.procs;

import static threepl.ThreePL.tdelist;

import threepl.codegen.TDE;
import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Clock;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.exec.WordSpec;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to connect a clock input signal to the clock port of
 * an element. This has 3 input arguments.
 * 
 * The first input argument is a string giving the block identifier or is
 * omitted. In the latter case the most recently created element will be
 * addressed.
 * 
 * The second argument is a target signal. It may also be an EDIF port name
 * string. If missing or null the port is connected to GND.
 *
 * The third input argument is a string giving the port name.
 * 
 */
public class ElementClockInputProc extends InbuiltProc implements Constant, TDEConstants {

    /**
     * Construct the inbuilt procedure elementclockinput().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public ElementClockInputProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
        ipnames.put("id", 0);
        ipnames.put("port_name", 1);
        ipnames.put("signal", 2);
    }

    /**
     * Execute the procedure elementclockinput().
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
        SrcLoc  loc = inargs.getCallLoc();
        Val     val;
        TDEVar  tdev = null;
        String  bname = null;
        String  portName;
        
        if (inargs.size() != 3)
            throw new ExEx("elementclockinput() - must have 3 input arguments", loc);
        if (outargs.size() != 0)
            throw new ExEx("elementclockinput() - must have no output arguments", loc);
        
        if (inargs.isNull(1) || (inargs.getVal(1) == null))
            throw new ExEx("elementclockinput() - no port name argument", loc);
       
        // block identifier
        if (inargs.getNode(0) != null) {
            val = inargs.getVal(0);       
            if ((val.getMode() != Mode.IMMEDIATE) || (val.getPrimType() != Ptype.STR))
                throw new ExEx("elementclockinput() - 1st input argument must be a string", loc);
            bname = val.getSingleSval(loc);
        }
        
        // element port name
        val = inargs.getVal(1);
        if ((val.getMode() != Mode.IMMEDIATE) || (val.getPrimType() != Ptype.STR))
            throw new ExEx("elementclockinput() - 2nd input argument must be a string", loc);
        portName = val.getSingleSval(loc);
        
        // clock input
        if (inargs.isNull(2) || (inargs.getVal(2) == null))
            tdev = TDEVar.GND;                     // no clock signal argument
        else {
            val = inargs.getVal(2);
            switch (val.getMode()) {
            case CLOCK:
                tdev = val.getTDEVar();
                Var cvar = val.getVar();
                ((Clock)cvar).incrClkSinks();
                break;
            case IMMEDIATE:
                WordSpec    ws = val.getWordSpec();
                switch (ws.getPrimType(0)) {
                case LOG:
                    if (val.getSingleLval(loc) != false)
                        throw new ExEx("elementclockinput() - 3rd input immediate argument type is not \"log\"", loc);
                    tdev = TDEVar.GND;
                    break;
                case STR:
                    String  portname = val.getSingleSval(loc);
                    tdev = new TDEVar("PORT_"+portname, loc);
                    TDE ptde = new TDE(TDEType.PORT);
                    ptde.add2p(portname);
                    ptde.add2p(3);
                    ptde.add2i(tdev);
                    tdelist.addTDE(ptde);
                    break;
                default:
                    throw new ExEx("elementclockinput() - 3rd input immediate argument not \"LOG\" \"false\" or port string", loc);
                }
                break;
            default:
                throw new ExEx("elementclockinput() - 3rd input argument not mode CLOCK or immediate", loc);
            }
        }
                
        tdelist.addPort(bname, portName, 3, tdev, 0, 0);
    }
}
