package threepl.procs;

import static threepl.ThreePL.tdelist;

import threepl.codegen.TDE;
import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to connect a 3-state output signal to a port of
 * an element. This has 2 or 3 input arguments and 1 output argument.
 * 
 * The output argument is an output target signal or signal array. It may also
 * be an EDIF port name string.
 * 
 * The first input argument is a string giving the block identifier or is omitted. In the latter case
 * the most recently created element will be addressed.
 * 
 * The second input argument is a string giving the port name.
 * 
 * The third input argument is an optional integer which controls array formatting.
 * If 1, the port is an array but in the form of discrete pins, e.g. A0, A1, A2 etc.
 * If 2, the port is an array in the form {@code (rename A (array "A<5:0>") 6)}
 * This argument may be 1 when the target signal is not an array, in which case
 * this instance of the element port has only the first pin of an array connected.
 * (this case probably only arises in the case of variable width ports such as the parity
 * data pins of a RAMB where the array can be up to 4 bits wide but only a single member
 * may be connected). For a non-array target signal this argument may be omitted and will
 * default to 0.
 * 
 * For port arrays the port dimension is determined from the target signal array. For port array
 * form the port target signal array must always have the same dimension in all instances of the
 * cell. For port arrays with discrete pins the port size may vary among instances of the cell,
 * the port definition for the cell using the highest dimension encountered.
 *  
 *  It seems that ISE will accept all array inputs as discrete pins rather than arrays
 *  but is willing to accept array pins on some cells and not others.
 *  Vivado on the other hand expects array inputs on at least some cells - this is still
 *  being empirically investigated as there is no available documentation on the expected EDIF format.
 *  
 *  Note: I suspect for ISE that array ports are OK on RAMBs for example if the whole port
 *  width is specified and connected. Ports where the full width is not specified give
 *  an error.
 *  *
 *  Note to check: perhaps only RAMB is the problem because of its variable port and address sizes?
 *  Note: for Virtex2Pro each RAMB36 was defined separately, e.g. RAMB4, RAMB8S8 ...
 *  For Virtex 5 there seemed to be only one RAMB36 cell definition. Perhaps the use of this
 *  one definition with different port array sizes in different cell instances causes the
 *  problems with port array form, whereas it does not with discrete pin form.
 */
public class ElementTSOutputProc extends InbuiltProc implements Constant, TDEConstants {

    /**
     * Construct the inbuilt procedure elementtsoutput().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public ElementTSOutputProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
        ipnames.put("id", 0);
        ipnames.put("port_name", 1);
        ipnames.put("arrayformat", 2);
        opnames.put("signal", 0);
    }

    /**
     * Execute the procedure elementtsoutput().
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
        Ref     ref;
        int     width = 0;
        TDEVar  tdev = null;
        String  bname = null;
        String  portName;
        int     arrayformat = 0;
        
        if ((inargs.size() != 2) && (inargs.size() != 3))
            throw new ExEx("elementtsoutput() - must have 2 or 3 input arguments", loc);
        if (outargs.size() != 1)
            throw new ExEx("elementtsoutput() - must have 1 output argument", loc);
        
        if (outargs.isNull(0) || (outargs.getRef(0, "elementtsoutput()") == null))
            return;                     // no output signal argument
        if (inargs.isNull(1) || (inargs.getVal(1) == null))
            return;                     // no port name argument
       
        // block identifier
        if (inargs.getNode(0) != null) {
            val = inargs.getVal(0);       
            if ((val.getMode() != Mode.IMMEDIATE) || (val.getPrimType() != Ptype.STR))
                throw new ExEx("elementtsoutput() - 1st input argument must be a string", loc);
            bname = val.getSingleSval(loc);
        }
        
        // element port name
        val = inargs.getVal(1);
        if ((val.getMode() != Mode.IMMEDIATE) || (val.getPrimType() != Ptype.STR))
            throw new ExEx("elementtsoutput() - 2nd input argument must be a string", loc);
        portName = val.getSingleSval(loc);
        
        // array format
        if (inargs.size() == 3) {
            val = inargs.getVal(2);
            if ((val.getMode() != Mode.IMMEDIATE))
                throw new ExEx("elementtsoutput() - 3rd input argument must be an int", loc);
            arrayformat = (int)val.getSingleIval(loc);
            if ((arrayformat < 0) || (arrayformat > 2))
                throw new ExEx("elementtsoutput() - 3rd input argument must be in range 0 to 2", loc);
            arrayformat = (int)val.getSingleIval(loc);
        }
        
        // 3-state output signal
        ref = outargs.getRef(0, "elementtsoutput()");
        switch (ref.getMode()) {
        case SELECTVALUE:
            tdev = ref.getTDEVar();
            ref.getVar().setAssigned();
            break;
        case IMMEDIATE:
            if (val.getPrimType() == Ptype.STR) {
                String  portname = val.getSingleSval(loc);
                tdev = new TDEVar("PORT_"+portname, loc);
                TDE ptde = new TDE(TDEType.PORT);
                ptde.add2p(portname);
                ptde.add2p(2);
                ptde.add2i(tdev);
                tdelist.addTDE(ptde);
            } else
                throw new ExEx("elementtsoutput() - output argument immediate mode but not a string", loc);
            break;
        default:
            throw new ExEx("elementtsoutput() - output argument for 3-state output signal not selectvalue mode", loc);
        }
        
        width = tdev.numBits();

        if (arrayformat == 0) {
            if (width == 1)
                width = 0;  // single value
            else
                throw new ExEx("elementtsoutput() - array input with no width or array format provided", loc);
        }
        if ((width == 0) && (arrayformat == 2))
            throw new ExEx("elementtsoutput() - array format code 2 cannot be used with a single target value", loc);

        tdelist.addPort(bname, portName, 2, tdev, width, arrayformat);
    }
}
