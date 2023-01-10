package threepl.procs;

import static threepl.ThreePL.tdelist;

import threepl.codegen.TDE;
import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to connect an input signal to a port of
 * an element. This has 3 or 4 input arguments.
 * 
 * The first input argument is a string giving the block identifier or is
 * omitted. In the latter case the most recently created element will be
 * addressed.
 * 
 * The second argument is an input target signal or signal array. It may also
 * be an EDIF port name string. If missing or null no port connection is
 * made.
 * 
 * The third input argument is a string giving the port name.
 * 
 * The fourth input argument is an optional integer which controls array
 * formatting.
 * If 1, the port is an array but in the form of discrete pins,
 * e.g. A0, A1, A2 etc.
 * If 2, the port is an array in the form {@code (rename A (array "A<5:0>") 6)}
 * This argument may be 1 when the target signal is not an
 * array, in which case this instance of the element port has only the first
 * pin of an array connected. (this case probably only arises in the case of
 * variable width ports such as the parity data pins of a RAMB where the array
 * can be up to 4 bits wide but only a single member may be connected). For a
 * non-array target signal this argument may be omitted and will default to 0.
 * 
 * For port arrays the port dimension is determined from the target signal
 * array. For port array form the port target signal array must always have
 * the same dimension in all instances of the cell. For port arrays with
 * discrete pins the port size may vary among instances of the cell, the port
 * definition for the cell using the highest dimension encountered.
 * 
 *  It seems that ISE will accept all array inputs as discrete pins rather
 *  than arrays but is willing to accept array pins on some cells and not
 *  others. Vivado on the other hand expects array inputs on at least some
 *  cells - this is still being empirically investigated as there is no
 *  available documentation on the expected EDIF format.
 * 
 *  Note: I suspect for ISE that array ports are OK on RAMBs for example if
 *  the whole port width is specified and connected. Ports where the full
 *  width is not specified give an error. * Note to check: perhaps only RAMB
 *  is the problem because of its variable port and address sizes? Note: for
 *  Virtex2Pro each RAMB36 was defined separately, e.g. RAMB4, RAMB8S8 ... For
 *  Virtex 5 there seemed to be only one RAMB36 cell definition. Perhaps the
 *  use of this one definition with different port array sizes in different
 *  cell instances causes the problems with port array form, whereas it does
 *  not with discrete pin form.
 */
public class ElementInputProc extends InbuiltProc implements Constant, TDEConstants {

    /**
     * Construct the inbuilt procedure elementinput().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public ElementInputProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
        ipnames.put("id", 0);
        ipnames.put("port_name", 1);
        ipnames.put("signal", 2);
        ipnames.put("arrayformat", 3);
    }

    /**
     * Execute the procedure elementinput().
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
        int     width = 0;
        TDEVar  tdev = null;
        String  bname = null;
        String  portName;
        int     arrayformat = 0;
        
        if ((inargs.size() != 3) && (inargs.size() != 4))
            throw new ExEx("elementinput() - must have 3 or 4 input arguments", loc);
        if (outargs.size() != 0)
            throw new ExEx("elementinput() - must have no output arguments", loc);
        
        if (inargs.isNull(1) || (inargs.getVal(1) == null))
            throw new ExEx("elementinput() - no port name argument", loc);
        if (inargs.isNull(2) || (inargs.getVal(2) == null))
            return;                     // no signal argument
       
        // block identifier
        if (inargs.getNode(0) != null) {
            val = inargs.getVal(0);       
            if ((val.getMode() != Mode.IMMEDIATE) || (val.getPrimType() != Ptype.STR))
                throw new ExEx("elementinput() - 1st input argument must be a string", loc);
            bname = val.getSingleSval(loc);
        }
        
        // element port name
        val = inargs.getVal(1);
        if ((val.getMode() != Mode.IMMEDIATE) || (val.getPrimType() != Ptype.STR))
            throw new ExEx("elementinput() - 2nd input argument must be a string", loc);
        portName = val.getSingleSval(loc);
        
        // signal
        val = inargs.getVal(2);
        if ((val.getMode() == Mode.CMEMORY) || (val.getMode() == Mode.RMEMORY))
            throw new ExEx("elementinput() - 3rd input argument for input signal is memory mode", loc);
        if (!val.getQueues().isEmpty())
            throw new ExEx("elementinput() - 3rd input argument for input signal contains queue reads", loc);
        if ((val.getMode() == Mode.IMMEDIATE)) {
            if (val.getPrimType() == Ptype.STR) {
                String  portname = val.getSingleSval(loc);
                tdev = new TDEVar("PORT_"+portname, loc);
                TDE ptde = new TDE(TDEType.PORT);
                ptde.add2p(0);
                ptde.add2p(portname);
                ptde.add2i(tdev);
                tdelist.addTDE(ptde);
            } else {
                val.toTarget();
                tdev = val.getTDEVar();
            }
        } else
            tdev = val.getTDEVar();
        
        width = tdev.numBits();
        
        // array format
        if (inargs.size() == 4) {
            val = inargs.getVal(3);
            if ((val.getMode() != Mode.IMMEDIATE))
                throw new ExEx("elementinput() - 4th input argument must be an int", loc);
            arrayformat = (int)val.getSingleIval(loc);
            if ((arrayformat < 0) || (arrayformat > 2))
                throw new ExEx("elementinput() - 4th input argument must be 0, 1 or 2", loc);
            arrayformat = (int)val.getSingleIval(loc);
        }
        
        if (arrayformat == 0) {
            if (width == 1)
                width = 0;  // single value
            else
                throw new ExEx("elementinput() - array input with no width or array format provided", loc);
        }
        if ((width == 0) && (arrayformat == 2))
            throw new ExEx("elementinput() - array format code 2 cannot be used with a single target value", loc);

        tdelist.addPort(bname, portName, 0, tdev, width, arrayformat);
    }
}
