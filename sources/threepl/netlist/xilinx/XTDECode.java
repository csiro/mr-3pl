package threepl.netlist.xilinx;

import static threepl.ThreePL.*;
import static threepl.netlist.Net.netArray;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.codegen.TDEVarList;
import threepl.exceptions.ExEx;
import threepl.exec.Priority;
import threepl.exec.Var;
import threepl.exec.WordSpec;
import threepl.netlist.Element;
import threepl.netlist.Net;
import threepl.parser.Constant;

/**
 * <p>This class is the interface between the TDE list
 * and the Xilinx EDIF netlist. There are subclasses XC2C, XC2S, XC3S, XC6S, XCV,
 * XC2V, XC2VP, XC4V, XC5V, XC6V, XC7 which contain code specific
 * to a family of FPGAs or CPLDs. The object on which methods in this class are invoked
 * must be one of these subclasses.</p>
 *
 * <p>There is a method for each TDE and this is the
 * interface to the TDE list. These methods do not generate EDIF
 * netlist code directly but call methods in the super classes
 * to do so.</p>
 *
 * <p>The arguments and return values are not a consistent scheme
 * through all elements.</p>
 *
 * <pre>
 * The class structure is -
 *
 *        TDECode           implementation of TDE modules - vendor independent
 *           |
 *      GenFunctions        general support methods - vendor independent
 *           |
 *       XElements          Xilinx general unified elements
 *           |
 *       XFunctions         Xilinx higher level functions
 *           |
 *       XTDECode           Xilinx implementation of TDE modules
 *           |
 *     ----------------------------
 *    /   /  |   \   \      \
 * XC2S XCV XC3S XC2V XC2VP XC4V ......     Xilinx elements specific to FPGA families
 * </pre>
 *
 * All classes above starting with "X" are in subdirectory "xilinx".
 *
 * <p>All methods in the lowest subclasses override corresponding
 * methods in class XElements which throw exceptions to pick up
 * the use of special elements in FPGA families which do not
 * have them.</p>
 *
 * <p>TDE code generation methods are invoked using the FPGA
 * family as the object instance.</p>
 */
public abstract class XTDECode extends XFunctions implements Constant, TDEConstants {
    // Map of elements created using the TDEELEMENT TDE
    public static Element                       current_element;
    public static ArrayList<Constraint>         ncf;    // NCF constraints for ISE (other than net or element constraints)
    public static ArrayList<Constraint>         xdc;    // XDC constraints for Vivado
    
    private class Constraint {
        String          format;
        String          portname;
        ArrayList<Net>  nets;
        
        public Constraint (String format, String portname) {
            this.format = format;
            this.portname = portname;
            nets = new ArrayList<Net>();
        }
        
        /**
         * Add a net to the list of nets for the constraint.
         * @param n is the net to be added
         */
        public void addNet (Net n) {
            if (n != null)
                nets.add(n);
        }
        
        public boolean validNets () {
            for (Net n : nets)
                if ((n.getNumInputs() == 0) && (n.getNumOutputs() == 0))
                    return(false);
            return(true);
        }
        
        /**
         * Generate a constraint string from a format, extracting signals from the constraint net list
         * as required.
         * The format string may contain %n and %N conversions. %n is replaced by the identifier of the next
         * net in the list. %N is similar to the above but changes the identifier to upper case and
         * removes any leading "GLOB_" string  This is used for generating clock timing group names from the
         * clock signal identifier. The size of the list of nets needs to be sufficient for the number of
         * conversions in the format and any excess nets will be ignored.
         *  A \% followed by any other character is unchanged.
         * @return the constraint string.
         */
        public String getConstraint () {
            StringBuffer    sb = new StringBuffer();
            int             i;
            int             iprev = 0;
            int             in = 0;
            
            for (;; iprev=i+2) {
                i = format.indexOf('%', iprev);
                if (i < 0) {
                    sb.append(format.substring(iprev));
                    return(sb.toString());
                }
                sb.append(format.substring(iprev, i));
                char    c = format.charAt(i+1);
                if (c == 'n')
                    sb.append(nets.get(in++).getEDIFIdent());
                else if (c == 'N') {
                    String  tnm = nets.get(in++).getEDIFIdent().toUpperCase();
                    if (tnm.startsWith("GLOB_"))
                        tnm = tnm.substring(5);
                    sb.append(tnm);
                } else
                    sb.append("%" + c);
            }
        }
    }
    

    
    public XTDECode () {
        current_element = null;
    }
    
    /**
     * Initialise common CLB cell declarations to ensure that the basic
     * gates are there. This avoids problems when the full set
     * is not created by code instances and gate optimisation
     * reduces a gate to a lower number of inputs not represented
     * in celldecls.
     * @param   arrayformat is 1 or 2 to determine how arrays are to be formatted in the EDIF file
     * @param   familyString indicates the FPGA family, e.g. XC5V
     */
    static public void initialise (int arrayformat, String familyString) {
        for (int i=1 ; i<=lutwidth ; i++) {
            Element.putCelldecl("AND"+i, i);
            Element.putCelldecl("OR"+i, i);
            Element.putCelldecl("XOR"+i, i);
            Element.putCelldecl("XNOR"+i, i);
            Element.putCelldecl("LUT"+i, i);
            ncf = new ArrayList<Constraint>();
            xdc = new ArrayList<Constraint>();
        }
            
        // Set the Xilinx place-and-route tools according to FPGA type.
        // For all devices earlier than series 7 ISE must used and postProcessTool
        // is "ise" and arrayformat is 1.
        // For series 7 devices Vivado is used and postProcessTool is "vivado"
        // and arrayformat is 2.
        // For series 7 ISE can in principle be used but 3PL does not support this!
        XElements.arrayformat = arrayformat;
        
        if (findDir("postProcessTool") == null)
            throw new ExEx("Directive 'postProcessTool' has not been defined by presetdirective()");
        String tool = stringDir("postProcessTool");
        if (! (tool.equals("ise") || tool.equals("vivado")))
            throw new ExEx("Directive 'postProcessTool' must be either \"ise\" or \"vivado\"");

        addDir("family", familyString);
        libref = "xi" + familyString.toLowerCase();
    }
    
    /**
     * Add a line to the ISE NCF constraints.
     * @param s is the constraint line string
     * @param portname is the name of the FPGA port
     * @return  the constraint
     */
    public static Constraint addNCFConstraint (String s, String portname) {
        Constraint  c = ((XTDECode)family).new Constraint(s, portname);
        ncf.add(c);
        return(c);
    }        

    /**
     * Add a line to the Vivado XDC constraints.
     * @param s is the constraint line string
     * @param portname is the name of the FPGA port
     * @return  the constraint
     */
    public static Constraint addXDCConstraint (String s, String portname) {
        Constraint  c = ((XTDECode)family).new Constraint(s, portname);
        if (s.startsWith("create_clock"))
            xdc.add(0, c);
        else
            xdc.add(c);
        return(c);
    }
    
    /**
     * Write out the accumulated lines to the ISE NCF file
     * @param   pw is the open NCF file
     */
    static public void outputNCFConstraints (PrintWriter pw) {
        for (Constraint c : ncf) {
            String  pname = c.portname;
            if (pname != null) {
                Port p = portmap.get(pname);    // there is a port name on this constraint
                if (p == null)
                    continue;                   // there is no such port in the port map
                if ((p != null) && (p.inet == null) && (p.onet == null))
                    continue;                   // the port has no signal connections
            }
            if (!c.validNets())
                continue;                       // one or more of the constraint signals is not connected
            pw.println(c.getConstraint());
        }
    }
    
    /**
     * Write out the accumulated lines to the Vivado XDC file
     * @param   pw is the open XDC file
     */
    static public void outputXDCConstraints (PrintWriter pw) {
        for (Constraint c : xdc) {
            String  pname = c.portname;
            if (pname != null) {
                Port p = portmap.get(pname);    // there is a port name on this constraint
                if (p == null)
                    continue;                   // there is no such port in the port map
                if ((p != null) && (p.inet == null) && (p.onet == null))
                    continue;                   // the port has no signal connections
            }
            if (!c.validNets())
                continue;                       // one or more of the constraint signals is not connected
            pw.println(c.getConstraint());
        }
    }
    
    /**
     * Determine if the ISE NCF constraints list is non-empty.
     * @return  true if there are NCF constraints
     */
    static public boolean haveNCFConstraints () { return(ncf.size() != 0); }
    
    /**
     * Determine if the Vivado XDC constraints list is non-empty.
     * @return  true if there are XDC constraints
     */
    static public boolean haveXDCConstraints () { return(xdc.size() != 0); }

    
    /**
     * Encode a selector.
     * Parameters:
     *  unselected out  - output state when no input selected
     *  use ALU         - use an ALU for multiple arithmetic operators
     *  DSP             - use a DSP block for an ALU
     * The 2nd and 3rd parameters are used in previous optimisation and are
     * not user here.
     * The parameters may be omitted.
     * Data inputs narrower than the output are padded or sign extended as appropriate.
     *  
     *  NOTE: this creates a dummy element that is later optimised and converted to
     *  basic logic. Duplicate inputs cannot be resolved here because we have TDEVars, not Nets,
     *  so cannot determine that inputs are duplicated. Once the internal netlist has been constructed
     *  all net names will have been resolved and duplicate entries can be identified and merged.
     *  
     * @param   params is a list of parameters
     * @param   inputs is a list of input signals, even entries are data
     *          signals or signal arrays, odd entries are select signals.
     * @param   output is a list containing the output signal or signal array
     */
    public void TDESELECT (
        ArrayList<Object>   params,
        TDEVarList          inputs,
        TDEVarList          output
    ) {
        int     width = output.getWidth(0);
        int     numinputs = inputs.size() / 2;
        Net     o = null;
        String  defaultout = "0";
        if (params.size() != 0) // optional parameter
            defaultout = (String)params.get(0);

        // If no inputs, drive output using the default.
        if (numinputs == 0) {
            for (int bit=0 ; bit<width ; bit++) {
                o = (strhexbit(defaultout, bit) == 1) ? Net.HI : Net.LO;
                output.getNet(0, bit).connect(o);
            }
            return;
        }
        
        Net[]   sin = new Net[numinputs];
        Net[]   din = new Net[numinputs];
        
        // Collect select inputs.
        for (int i=0 ; i<numinputs ; i++)
            sin[i] = inputs.getNet(i*2);
        
        // Handle each data bit separately.
        // NOTE: Since each bit is handled separately any optimisation of duplicated inputs
        // resulting in ORed select signals will be repeated for each bit. It is not clear
        // that they could be common since data inputs may be identical for some bits and not for
        // others, in which case the optimisation has to be per bit.
        for (int bit=0 ; bit<width ; bit++) {            
            // Collect data inputs for this bit.
            for (int i=0 ; i<numinputs ; i++)
                din[i] = inputs.getNet(i*2+1, bit);
            
            selector (output.getNet(0, bit), sin, din, strhexbit(defaultout, bit));
        }
    }

    /**
     * Encode a three-state selector.
     * Parameters:
     *  unselected out  - output state when no input selected
     * @param   params is a list of parameters
     * @param   inputs is a list of input signals, odd entries are
     *          select signals, even entries are data signals or signal arrays
     * @param   output is a list containing the output signal or signal array
     */
    public void TDETSELECT (
            ArrayList<Object>   params,
            TDEVarList          inputs,
            TDEVarList          output
    ) {
        int     width = output.getWidth(0);
        int     numinputs = inputs.size();
        String  defaultout = "0";
        if (params.size() != 0) // optional parameter
            defaultout = (String)params.get(0);

        // handle each data bit separately
        for (int bit=0 ; bit<width ; bit++) {
            Net o = output.getNet(0, bit);
            for (int i=0 ; i<numinputs ; i+=2) {
                Net s = inputs.getNet(i);
                Net d = inputs.getNet(i+1, bit);
                o.connect(BUFE(d, s));
            }
            if ((strhexbit(defaultout, bit) & 1) == 0)
                o.addProperty("PULLDOWN", "TRUE");
            else
                o.addProperty("PULLUP", "TRUE");
        }
    }

    /**
     * Encode a static variable (register).
     * The parameter list contains the following parameters -
     * <ul>
     * <li> String[] - block IDs
     * <li> String - the initial value as a hexadecimal string
     * <li> boolean - IOB flag
     * <li> boolean - DSP flag
     * </ul>
     * The inputs list contains the following -
     * <ul>
     * <li> signal - the clock (may be null if no data inputs)
     * <li> signal array - data input(s) (may be null)
     * <li> signal or signal array - clock-enable(s)
     * <li> signal or signal array - reset
     * </ul>
     * The output list contains the output signal array.
     *
     * If the clock input is null a constant is generated and the other
     * inputs are ignored.
     * The data input must be the same width as the output if not null.
     * The clock enable and reset inputs may be arrays in which case
     * they must be the same width as the input and output. Alternatively
     * either or both may be width 1 in which case all bits will share the
     * same clock enable or reset.
     * @param   params is a list of parameters
     * @param   inputs is a list of input signals and signal arrays
     * @param   output is a list containing the output signal array
     */
    public void TDEREG (
            ArrayList<Object>   params,
            TDEVarList          inputs,
            TDEVarList          output
    ) {
        String[]    ids = (String[])params.get(0);
        String      sinit = (String)params.get(1);
        boolean     iob = (Boolean)params.get(2);

        int     width = output.getWidth(0);
        int     cewidth = inputs.getWidth(2);
        int     rwidth = inputs.getWidth(3);
        if (inputs.getWidth(0) == 0) {
            // no inputs - generate constant
            connect(output.getNetArray(0), hexConstant(sinit, width));
            return;
        }
        
        Net     c = inputs.getNet(0);
        Net[]   in = inputs.getNetArray(1);
        Net[]   ce = null;
        Net[]   r = null;
        Net[]   out= output.getNetArray(0);
        
        if ((width != 1) && (cewidth == 1)) {
            ce = new Net[width];
            for (int i=0 ; i<width ; i++)
                ce[i] = inputs.getNet(2, 0);
        } else
            ce = inputs.getNetArray(2);
        
        if ((width != 1) && (rwidth == 1)) {
            r = new Net[width];
            for (int i=0 ; i<width ; i++)
                r[i] = inputs.getNet(3, 0);
        } else
            r = inputs.getNetArray(3);

        connect(out, reg(in, c, ce, r, sinit, iob, ids));
    }
   
    /**
     * Encode an inverter (1's complement).
     * @param   inputs is a list of input signals
     * @param   outputs is a list of output signals
     */
    public void TDEINV (TDEVarList inputs, TDEVarList outputs) {
        int width = inputs.size();
        for (int i=0 ; i<width ; i++)
            outputs.getNet(i).connect(INV(inputs.getNet(i)));
    }
   
    /**
     * Encode an AND.
     * @param   inputs is a list of input signals
     * @param   output is a list containing the output signal
     */
    public void TDEAND (TDEVarList inputs, TDEVarList output) {
        ArrayList<Net>  a = new ArrayList<Net>();
        int             width = inputs.size();
        for (int i=0 ; i<width ; i++)
            a.add(inputs.getNet(i));
        Net o = AND(a);
        output.getNet(0).connect(o);
    }
    
    /**
     * Encode an OR.
     * @param   inputs is a list of input signals
     * @param   output is a list containing the output signal
     */
    public void TDEOR (TDEVarList inputs, TDEVarList output) {
        ArrayList<Net>  a = new ArrayList<Net>();
        int             width = inputs.size();
        for (int i=0 ; i<width ; i++)
            a.add(inputs.getNet(i));
        Net o = OR(a);
        output.getNet(0).connect(o);
    }
    
    /**
     * Encode an XOR.
     * @param   inputs is a list of input signals
     * @param   output is a list containing the output signal
     */
    public void TDEXOR (TDEVarList inputs, TDEVarList output) {
        ArrayList<Net>  a = new ArrayList<Net>();
        int             width = inputs.size();
        for (int i=0 ; i<width ; i++)
            a.add(inputs.getNet(i));
        Net o = XOR(a);
        output.getNet(0).connect(o);
    }
    
    /**
     * Encode a D flip-flop. The 1st argument is a list of zero to two
     * parameters. These are -
     * <ul>
     * <li> boolean initialisation state (may be null)
     * <li> boolean asynchronous false for synchronous set/reset, true for asynchronous clr/pre
     * </ul>
     * The 1st parameter must be "R" or "S"
     * The 2nd parameter selects the type of flip-flop and the function of the
     * 4th and 5th input arguments
     * 
     * The 2nd argument is a list of inputs which are in
     * the following order -
     * <ul>
     * <li> D - the data input
     * <li> C - the clock
     * <li> CE - the clock enable
     * <li> R or CLR - the synchronous reset or asynchronous clear
     * <li> S or PRE - the synchronous set or asynchronous preset
     * 
     * The 3rd argument is a list containing a single output.
     * </ul>
     * Any of these list elements may be null.
     * @param   params is a list of parameters
     * @param   inputs is a list of input signals
     * @param   output is a list containing the output signal
     */
    public void TDEDFF (
            ArrayList<Object>   params,
            TDEVarList          inputs,
            TDEVarList          output
    ) {
        if (inputs.size() != 5)
            throw new ExEx("TDEDFF error - does not have 5 inputs");
        String      init = (String)params.get(0);
        boolean     async = ((Boolean)params.get(1)).booleanValue();
        Net         q = null;
        Net         d = inputs.getNet(0);
        Net         c = inputs.getNet(1);
        Net         ce = inputs.getNet(2);
        Net         r_clr = inputs.getNet(3);
        Net         s_pre = inputs.getNet(4);
        String      ename = async ? "FDCPE" : "FDRSE";
    
        if (c == null)
            throw new ExEx("DFF " + ename + " generation error - has no clock input");
        if (init == null)
            throw new ExEx("DFF " + ename + " generation error - has no initialisation");
        if (!(init.equals("R") || init.equals("S")))
            throw new ExEx("DFF " + ename + " generation error - has erroneous initialisation");

        if (d == null)
            throw new ExEx("DFF " + ename + " generation error - has no D input");
        if (ce == null)
            throw new ExEx("DFF " + ename + " generation error - has no CE input");
        
        if (async)
            q = FDCPE(null, d, c, ce, r_clr, s_pre, init, false);
        else
            q = FDRSE(null, d, c, ce, r_clr, s_pre, init, false);
    
        output.getNet(0).connect(q);
    }
    
    /**
     * Encode a single bit input buffer.
     * The parameter list contains the following parameters -
     * <ul>
     * <li> String  - optional element identifier or null
     * <li> String  - element package pin
     * <li> String  - 2nd element package pin for differential input, or null
     * <li> boolean - ibufg indicator
     * <li> boolean - differential indicator
     * </ul>
     * The outputs list contains the output signal, which is one bit wide.
     * @param   params is a list of parameters
     * @param   inputs is a list containing the input signal from the associated pad
     * @param   outputs is a list containing the output signal plus an optional differential output signal or null
     */
    public void TDEIBUF (ArrayList<Object> params, TDEVarList inputs, TDEVarList outputs) {
        String  id = (String)params.get(0);
        String  loc = (String)params.get(1);
        String  locd = (String)params.get(2);
        boolean ibufg = (Boolean)params.get(3);
        boolean diff = (Boolean)params.get(4);
        Net     in = inputs.getNet(0);
        Net     ind = inputs.getNet(1);
        Net     out = outputs.getNet(0);
        Net     outd = outputs.getNet(1);
        
        addInputPort(loc, in);
        if (ibufg) {
            if (diff) {
                addInputPort(locd, ind);
                IBUFGDS(out, in, ind, id);
            } else 
                IBUFG(out, in, id);
        } else {
            if (diff) {
                addInputPort(locd, ind);
                if (outd != null)
                    IBUFDS_DIFF_OUT(out, outd, in, ind, id);
                else
                    IBUFDS(out, in, ind, id);
            } else
                IBUF(out, in, id);
        }
    }
    
    /**
     * Encode a single bit output buffer.
     * The parameter list contains parameter pairs -
     * <ul>
     * <li> String   - optional element identifier or null
     * <li> String   - element package pin
     * <li> String   - 2nd element package pin for differential output, or null
     * <li> boolean  - differential output
     * <li> boolean  - no shadow
     * </ul>
     * @param   params is a list of parameters
     * @param   inputs is a list containing the input signal and an optional enable signal
     * @param   outputs is a list containing the output signal(s) (to the associated pad(s))
     */
    public void TDEOBUF (ArrayList<Object> params, TDEVarList inputs, TDEVarList outputs) {
        String      id = (String)params.get(0);
        String      loc = (String)params.get(1);
        String      locd = (String)params.get(2);
        boolean     diff = (Boolean)params.get(3);
        boolean     no_shadow = (Boolean)params.get(4);
        Net         in = inputs.getNet(0);
        Net         enable = inputs.getNet(1);
        Net         out = outputs.getNet(0);
        Net         outd = outputs.getNet(1);

        Element e;
        addOutputPort(loc, out);
        if (diff) {
            addOutputPort(locd, outd);
            e = OBUFDS(out, outd, in, enable, id);
        } else
            e = OBUF(out, in, enable, id);
        
        if (no_shadow)
            e.setType(Gtype.NOSHADOW);
    }
    
    /**
     * Encode a clock domain start signal.
     * @param   inputs is a list containing 2 signals -
     *          <ul>
     *          <li>the start level. This may be VCC.
     *          <li>the clock of the domain for which the start pulse is destined
     *          </ul>
     * @param   output is a list containing the output signal
     */
    public void TDESTART (TDEVarList inputs, TDEVarList output) {
        Net sl = inputs.getNet(0);
        Net clk = inputs.getNet(1);
        Net sp = new Net();
        Net lock = new Net();
    
        if (sl.isConnected(Net.HI)) {
            sp.connect(FDRE(sl, clk, Net.HI, lock, "R"));
            lock.connect(FDRE(sl, clk, Net.HI, Net.LO, "R"));
            //lock.connect(FDRE(sl, clk, sp, Net.LO, "R"));
            //lock.connect(FDRE(sp, clk, Net.HI, Net.LO, "R"));
            output.getNet(0).connect(sp);
        } else {
            Net sl_clean = new Net();
            sl_clean.connect(FDRE(sl, clk, Net.HI, Net.LO, "R"));
            sp.connect(FDRE(sl_clean, clk, Net.HI, lock, "R"));
            lock.connect(FDRE(sl_clean, clk, Net.HI, Net.LO, "R"));
            output.getNet(0).connect(sp);
        }
    }
    
    /**
     * Encode an infinite loop.
     * @param   inputs is a list containing 2 signals -
     *          <ul>
     *          <li>the clock domain start signal
     *          <li>the finish signal for the code contained within the loop
     *          </ul>
     * @param   output is a list containing 1 signal, the start signal for
     *          the code within the loop 
     */
    public void TDEILOOP (TDEVarList inputs, TDEVarList output) {
        Net clock_start = inputs.getNet(0);
        Net finish = inputs.getNet(1);
        Net start;
        start = OR(clock_start, finish);
        output.getNet(0).connect(start);
    }
    
    /**
     * Encode a when conditional statement.
     * @param   inputs is a list containing 4 signals -
     *          <ul>
     *          <li>the start signal for the when
     *          <li>the test signal
     *          <li>the finish signal for the 'true' block or null
     *          <li>the finish signal for the 'false' block or null
     *          </ul>
     * @param   outputs is a list containing 3 signals -
     *          <ul>
     *          <li>the start signal for the 'true' block
     *          <li>the start signal for the 'false' block
     *          <li>the finish signal for the 'when' or null
     *          </ul>
     */
    public void TDEWHEN (TDEVarList inputs, TDEVarList outputs) {
        Net start = inputs.getNet(0);
        Net test = inputs.getNet(1);
        Net startt = null;
        Net startf = null;
        Net finish;

        if (outputs.getNet(0) != null) {
            startt = AND(start, test);
            outputs.getNet(0).connect(startt);
        }
        if (outputs.getNet(1) != null) {
            startf = AND(start, INV(test));
            outputs.getNet(1).connect(startf);
        }
        if (outputs.getNet(2) != null) {
            Net finisht = inputs.getNet(2);
            Net finishf = inputs.getNet(3);
            finish = OR(finisht, finishf);
            outputs.getNet(2).connect(finish);
        }
    }
    
    /**
     * Encode a while loop.
     * @param   inputs is a list containing 4 signals -
     *          <ul>
     *          <li>the start signal for the 'while' via an availability test
     *          of the conditional signal
     *          <li>the 'while' conditional signal
     *          <li>the contained block finish signal via an availability test
     *          of the conditional signal
     *          <li>the clock
     *          <li>optional reset signal or null
     *          </ul>
     * @param   outputs is a list containing 2 signals
     *          <ul>
     *          <li>the start signal for the contained code block
     *          <li>the finish signal from the 'while' loop
     *          </ul>
     */
    public void TDEWHILE (TDEVarList inputs, TDEVarList outputs) {
        Net start_del = inputs.getNet(0);
        Net test = inputs.getNet(1);
        Net contin_del = inputs.getNet(2);
        Net clock = inputs.getNet(3);
        Net reset = (inputs.size() > 4) ? inputs.getNet(4) : null;
        Net startb;
        Net finish;

        startb = AND(OR(start_del, contin_del), test);
        finish = FDRE(OR(AND(contin_del, INV(test)), AND(start_del, INV(test))), clock, Net.HI, reset, "R");
        outputs.getNet(0).connect(startb);
        outputs.getNet(1).connect(finish);
    }

    /**
     * Encode a do-while loop.
     * @param   inputs is a list containing 3 signals -
     *          <ul>
     *          <li>the start signal for the 'do-while'
     *          <li>the conditional signal
     *          <li>the finish signal for the contained block via an
     *          availability delay on the conditional signal
     *          <li>the clock
     *          <li>optional reset signal or null
     *          </ul>
     * @param   outputs is a list containing 2 signals -
     *          <ul>
     *          <li>the start signal for the contained block
     *          <li>the finish signal for the 'do_while'
     *          </ul>
     */
    public void TDEDOWHILE (TDEVarList inputs, TDEVarList outputs) {
        Net start = inputs.getNet(0);
        Net test = inputs.getNet(1);
        Net finishb_del = inputs.getNet(2);
        Net clock = inputs.getNet(3);
        Net reset = (inputs.size() > 4) ? inputs.getNet(4) : null;
        Net startb;
        Net finish;

        startb = OR(start, AND(finishb_del, test));
        finish = FDRE(AND(finishb_del, INV(test)), clock, null, reset, null);
        outputs.getNet(0).connect(startb);
        outputs.getNet(1).connect(finish);
    }

    /**
     * Encode a resync.
     * @param   inputs is a list containing 3 signals -
     *          <ul>
     *          <li>the input level
     *          <li>the clock for the input level
     *          <li>the clock for the output
     *          </ul>
     * @param   output is a list containing 1 signal, the output
     */
    public void TDERESYNC (TDEVarList inputs, TDEVarList output) {
        Net i1 = inputs.getNet(0);
        Net c1 = inputs.getNet(1);
        Net c2 = inputs.getNet(2);

        output.getNet(0).connect(resync(i1, c1, c2));
    }
    
    /**
     * Encode a sample.
     * @param   params is a parameter list (empty)
     * @param   inputs is a list containing 3 signals -
     *          <ul>
     *          <li>the input data
     *          <li>the clock for the input
     *          <li>the clock for the output
     *          </ul>
     * @param   output is a list containing 1 signal, the output data
     */
    public void TDESAMPLE (
        ArrayList<Object>   params,
        TDEVarList          inputs,
        TDEVarList          output
    ) {
        Net[]   out = output.getNetArray(0);
        int     n = out.length;
        Net[]   in = inputs.getNetArray(0, n);
        Net     cw = inputs.getNet(1);
        Net     cr = inputs.getNet(2);
        Net[]   o;
        Net     ne = new Net();
        Net     nf = new Net();        
        Var     wcvar = inputs.getClkVar(1);
        Var     rcvar = inputs.getClkVar(2);
        
        if (n == 1) {
            // Single bit - use a flip-flop clocked by the read clock.
            out[0].connect(FDRE(in[0], cr, Net.HI, null, null));
        } else {        
            o = async_buffer(in, nf, ne, cw, cr, ne, nf, null, null, null, null, false, 4, wcvar, rcvar);
            for (int i=0 ; i<n ; i++)
                out[i].connect(o[i]);
        }
    }

    /**
     * Encode an execute via queue availability.
     * This will not have been placed in the TDE list if there are no queues
     * and no priority signals.
     * @param   params is a parameter list containing a single parameter -
     *          <ul>
     *          <li> a type code, 0 to 3 -
     *          </ul>
     *                                                      pri_in  pri_out bqavail     ubqwavail   ubqravail
     *          0    priority        no queues               Y       Y       null        null        null
     *          1    priority        buffered queues only    Y       Y       Y           null        null
     *          2    no priority     buffered queues only    null    null    av
     *          3    no priority     any queues              null    null    av | VCC    av | VCC    av | VCC
     * @param   inputs is a list containing 5 signals -
     *          <ul>
     *          <li>the clock
     *          <li>the start signal
     *          <li>the buffered queue read/write availability signal
     *          <li>the unbuffered queue write availability signal
     *          <li>the unbuffered queue read availability signal
     *          <li>a priority encoder output or null
     *          <li>optional reset signal or null
     *          </ul>
     * @param   outputs is a list containing 3 signals -
     *          <ul>
     *          <li>the delayed start
     *          <li>a priority encoder input or null
     *          <li>execution write pending signal or null
     *          <li>execution read pending signal or null
     *          </ul>
     */
    public void TDEEXECP (ArrayList<Object> params, TDEVarList inputs, TDEVarList outputs) {
        int type = (int)params.get(0);
        Net c = inputs.getNet(0);
        Net start_in = inputs.getNet(1);
        Net bqavail = inputs.getNet(2);
        Net ubqwavail = inputs.getNet(3);
        Net ubqravail = inputs.getNet(4);
        Net pri_out = inputs.getNet(5);
        Net reset = inputs.getNet(6);
        Net start_del = outputs.getNet(0);
        Net pri_in = outputs.getNet(1);
        Net wpending = outputs.getNet(2);
        Net rpending = outputs.getNet(3);
        Net q;
        boolean multiple = false;
        
       if (reset == null)
            reset = start_del;
        else
            reset = OR(reset, start_del);
        
        if (pri_out != null) {
            // This EXECP is used by waitpri statements. Determine if there
            // are multiple waits on this same priority value.
            TDEVar      tdev = inputs.get(5);
            WordSpec    ws = tdev.getWordSpec();
            int         index = ws.getWord(0);
            Priority    pv = (Priority)ws.getVar();
            multiple = pv.isMultiple(index);
        }

        q = FDRE(Net.HI, c, start_in, reset, "R");
        Net pending = OR(start_in, q);
        
        switch (type) {
        case 0: // priority, no queues
            pri_in.connect(pending);
            if (multiple)
                start_del.connect(AND(pri_out, pri_in));
            else
                start_del.connect(AND(pri_out));
            return;
        case 1: // priority, buffered queues only
            pri_in.connect(AND(bqavail, pending));
            if (multiple)
                start_del.connect(AND(pri_out, pri_in));
            else
                start_del.connect(AND(pri_out));
            return;
        case 2: // no priority, buffered queues only
            start_del.connect(AND(bqavail, pending));
            return;
        case 3: // no priority, buffered and/or unbuffered queues
            start_del.connect(AND(pending, bqavail, ubqwavail, ubqravail));
            if (wpending != null)
                wpending.connect(AND(pending, bqavail, ubqravail));
            if (rpending != null)
                rpending.connect(AND(pending, bqavail, ubqwavail));
        }
    }

    /**
     * Connect signals.
     *
     * If the optional 1st parameter is provided then this is a cast operation.
     * The value of the parameter indicates if sign extension rather than zero padding is to be
     * used if the output is wider than the input. If the output is narrower
     * than the input then truncation occurs,
     * 
     * If the input and output have the same widths then they are simply connected
     * and the cast parameter is not relevant.
     *
     * If the input is a single bit and there is no cast parameter it is connected
     * to all the outputs. This is usually used to connect all bits of a signal array
     * to GND or VCC or to an execution signal.
     * 
     * If the input is a constant the outputs are connected to an
     * associated pattern of HIs and LOs representing the constant as
     * weighted binary.
     *
     *
     * @param   param is a parameter list, see above
     * @param   input is a list containing 1 or more signals
     * @param   output is a list containing 1 or more signals
     */
    public void TDECONNECT (ArrayList<Object> param, TDEVarList input, TDEVarList output) {
        boolean sign_extend = false;    // true if there is a parameter and its value is 'true'
        boolean cast = false;           // true if there is a parameter
        int     iwidth = input.getWidth(0);
        int     owidth = output.getWidth(0);
        Net[]   in = null;
        Net[]   out = null;
        int     bits = 0;
        
        if ((param.size() != 0) && (param.get(0) != null)) {
            sign_extend = ((Boolean)param.get(0)).booleanValue();
            cast = true;
        }
        
        switch (input.getType(0)) {
        case VAR:
        case POS_CLK:
        case NEG_CLK:
        case BOOL:
            break;      // a normal variable, a clock or a single logical constant
                        // (GND or VCC) - continue
        default:
            // constant to output array
            long    v = input.getVal(0);
            for (int i=0 ; i<owidth ; i++) {
                if ((v & 1) != 0)
                    output.getNet(0, i).connect(Net.HI);
                else
                    output.getNet(0, i).connect(Net.LO);
                v >>= 1;
            }
            return;
        }

        // Special case of single input and one or more outputs
        // and not a cast -
        // connect the input to all the outputs.
        if ((iwidth == 1) && (owidth > 1) && !cast) {
            // connect 1 in to many out
            Net ins = input.getNet(0);
            for (int i=0 ; i<owidth ; i++)
                ins.connect(output.getNet(0, i));
            return;
        }
        
        // Except for the one-to-many case above, should never encounter
        // different sized inputs and outputs without the cast parameter.
        if ((iwidth != owidth) && !cast)
            msg("XTDECODE - TDECONNECT ERROR - no cast for width change");
            //throw new ExEx("XTDECODE - TDECONNECT ERROR");

        
        in = input.getNetArray(0);
        out = output.getNetArray(0);
        bits = Math.min(iwidth, owidth);
        
        // A cast - input and output arrays may be different sizes
        int     j = 0;
        int     ilast = iwidth - 1;
        for (int i=0 ; i<bits ; i++)
            out[j++].connect(in[i]);
        for (j=bits ; j<owidth ; j++) {
            if (sign_extend)
                out[j].connect(in[ilast]); // highest input bit is sign
            else
                out[j].connect(Net.LO);
        }
    }
     
    /**
     * Encode an arithmetic operator.
     * @param   params is a list containing -
     *          <ul>
     *          <li>integer operator code
     *          <li>boolean 1st operand is signed
     *          <li>boolean 2nd operand is signed (if binary or ternary)
     *          <li>boolean 3rd operand is signed (if ternary)
     *          </ul>
     * @param   inputs is a list containing signals -
     *          <ul>
     *          <li>1st operand net array
     *          <li>2nd operand net array if binary or ternary
     *          <li>3rd operand net array if ternary
     *          </ul>
     * @param   output is a list containing 1 entry -
     *          <ul>
     *          <li>the data output net array
     *          </ul>
     */
    public void TDEOPERATOR (ArrayList<Object> params, TDEVarList inputs, TDEVarList output) {
        TDEOp   op = (TDEOp)params.get(0);
        boolean signed1 = false;
        boolean signed2 = false;
        boolean signed3 = false;
        boolean dsp = false;
        switch (op) {
        case INV:
        case NEG:
            signed1 = (Boolean)params.get(1);
            break;
        case ADD:
        case SUB:
        case ADDSUB:
        case MUL:
        case DIV:
        case REM:
        case EQ:
        case NE:
        case LT:
        case LE:
        case GT:
        case GE:
        case AND:
        case OR:
        case XOR:
        case LSH:
        case RSH:
            signed1 = (Boolean)params.get(1);
            signed2 = (Boolean)params.get(2);
            break;
        case DIVREM:
            signed1 = (Boolean)params.get(1);
            signed2 = (Boolean)params.get(2);
            break;
        case COND:
            signed2 = (Boolean)params.get(2);
            signed3 = (Boolean)params.get(3);
            break;
        case ALU:
            signed1 = (Boolean)params.get(1);
            signed2 = (Boolean)params.get(2);
            dsp = ((Boolean)params.get(3)).booleanValue();
            break;
        default:
            break;
        }

        int         owidth1 = inputs.getWidth(0);
        Net[]       oper1 = inputs.getNetArray(0);
        int         owidth2 = inputs.getWidth(1);
        Net[]       oper2 = null;
        TDEVtype    ivtype2 = null;
        long        ioper2 = 0;
        if (owidth2 != 0) {
            oper2 = inputs.getNetArray(1);
            ivtype2 = inputs.getType(1);
            ioper2 = inputs.getVal(1);
        }

        int     owidth3 = inputs.getWidth(2);
        Net[]   oper3 = (owidth3 != 0) ? inputs.getNetArray(2) : null;
        Net[]   oper4 = (inputs.getWidth(3) != 0) ? inputs.getNetArray(3) : null;
        Net[]   oper5 = (inputs.getWidth(4) != 0) ? inputs.getNetArray(4) : null;
        Net[]   res = output.getNetArray(0);
        Net[]   rem = output.getNetArray(1);
        int     width;
        boolean signed;

        switch (op) {
        case INV:
            connect(res, inv(oper1));
            return;
        case NEG:
            int     size = oper1.length;
            Net[]   zero = new Net[size];
            for (int i=0 ; i<size ; i++)
                zero[i] = Net.LO;
            connect(res, sub(zero, oper1, false, signed1, size), signed1);
            return;
        case ADD:
            signed = signed1 || signed2;
            connect(res, add(oper1, oper2, signed1, signed2, res.length), signed);
            return;
        case SUB:
            signed = signed1 || signed2;
            connect(res, sub(oper1, oper2, signed1, signed2, res.length), signed);
            return;
        case ADDSUB:
            signed = signed1 || signed2;
            connect(res, addsub(oper1, oper2, oper3[0], oper4[0], oper5[0], signed1, signed2), signed);
            return;
        case ALU:
            signed = signed1 || signed2;
            connect(res, alu(oper1, oper2, oper3[0], oper4[0], signed1, signed2, dsp), signed);
            return;
        case MUL:
            signed = signed1 || signed2;
            if (oper1.length > oper2.length)
                connect(res, mul(oper1, oper2, signed1, signed2), signed);
            else
                connect(res, mul(oper2, oper1, signed2, signed1), signed);
            return;
        case DIV:
            signed = signed1 || signed2;
            connect(res, div(oper1, oper2, signed1, signed2), signed);
            return;
        case REM:
            signed = signed1 || signed2;
            connect(res, rem(oper1, oper2, signed1, signed2), signed);
            return;
        case DIVREM:
            signed = signed1 || signed2;
            ArrayList<Net[]> al = quotrem(oper1, oper2, signed1, signed2);
            connect(res, al.get(0), signed);
            connect(rem, al.get(1), signed);
            return;
        case EQ:
            res[0].connect(eq(oper1, oper2, signed1, signed2));
            return;
        case NE:
            res[0].connect(ne(oper1, oper2, signed1, signed2));
            return;
        case LT:
            // check for test '< 0'
            if (((ivtype2 == TDEVtype.UINT) || (ivtype2 == TDEVtype.INT) ||
                 (ivtype2 == TDEVtype.UFIXED) || (ivtype2 == TDEVtype.FIXED)) &&
                (ioper2 == 0)) {
                res[0].connect(oper1[oper1.length-1]); // sign bit
                return;
            }
            res[0].connect(INV(ge(oper1, oper2, signed1, signed2)));
            return;
        case LE:
            res[0].connect(ge(oper2, oper1, signed2, signed1));
            return;
        case GT:
            res[0].connect(INV(ge(oper2, oper1, signed2, signed1)));
            return;
        case GE:
            // check for test '>= 0'
            if (((ivtype2 == TDEVtype.UINT) || (ivtype2 == TDEVtype.INT) ||
                 (ivtype2 == TDEVtype.UFIXED) || (ivtype2 == TDEVtype.FIXED)) &&
                (ioper2 == 0)) {
                res[0].connect(INV(oper1[oper1.length-1])); // inverted sign bit
                return;
            }
            res[0].connect(ge(oper1, oper2, signed1, signed2));
            return;
        case AND:
            width = Math.max(owidth1, owidth2);
            oper1 = sig_expand(oper1, width, signed1);
            oper2 = sig_expand(oper2, width, signed2);
            for (int i=0 ; i<res.length ; i++)   
                res[i].connect(AND(oper1[i], oper2[i]));
            return;
        /*case NAND:
            width = Math.max(owidth1, owidth2);
            oper1 = sig_expand(oper1, width, signed1);
            oper2 = sig_expand(oper2, width, signed2);
            for (int i=0 ; i<res.length ; i++)   
                res[i].connect(INV(AND(oper1[i], oper2[i])));
            return;*/
        /*case _AND:
            width = Math.max(owidth1, owidth2);
            oper1 = sig_expand(oper1, width, signed1);
            oper2 = sig_expand(oper2, width, signed2);
            for (int i=0 ; i<res.length ; i++)   
                res[i].connect(AND(INV(oper1[i]), oper2[i]));
            return;*/
        /*case AND_:
            width = Math.max(owidth1, owidth2);
            oper1 = sig_expand(oper1, width, signed1);
            oper2 = sig_expand(oper2, width, signed2);
            for (int i=0 ; i<res.length ; i++)   
                res[i].connect(AND(oper1[i], INV(oper2[i])));
            return;*/
        case OR:
            width = Math.max(owidth1, owidth2);
            oper1 = sig_expand(oper1, width, signed1);
            oper2 = sig_expand(oper2, width, signed2);
            for (int i=0 ; i<res.length ; i++)   
                res[i].connect(OR(oper1[i], oper2[i]));
            return;
        /*case NOR:
            width = Math.max(owidth1, owidth2);
            oper1 = sig_expand(oper1, width, signed1);
            oper2 = sig_expand(oper2, width, signed2);
            for (int i=0 ; i<res.length ; i++)   
                res[i].connect(INV(OR(oper1[i], oper2[i])));
            return;*/
        /*case _OR:
            width = Math.max(owidth1, owidth2);
            oper1 = sig_expand(oper1, width, signed1);
            oper2 = sig_expand(oper2, width, signed2);
            for (int i=0 ; i<res.length ; i++)   
                res[i].connect(OR(INV(oper1[i]), oper2[i]));
            return;*/
        /*case OR_:
            width = Math.max(owidth1, owidth2);
            oper1 = sig_expand(oper1, width, signed1);
            oper2 = sig_expand(oper2, width, signed2);
            for (int i=0 ; i<res.length ; i++)   
                res[i].connect(OR(oper1[i], INV(oper2[i])));
            return;*/
        case XOR:
            width = Math.max(owidth1, owidth2);
            oper1 = sig_expand(oper1, width, signed1);
            oper2 = sig_expand(oper2, width, signed2);
            for (int i=0 ; i<res.length ; i++)   
                res[i].connect(XOR(oper1[i], oper2[i]));
            return;
        /*case XNOR:
            width = Math.max(owidth1, owidth2);
            oper1 = sig_expand(oper1, width, signed1);
            oper2 = sig_expand(oper2, width, signed2);
            for (int i=0 ; i<res.length ; i++)   
                res[i].connect(INV(XOR(oper1[i], oper2[i])));
            return;*/
        case LSH:
            if (ivtype2 != TDEVtype.VAR) 
                // constant shift
                connect(res, lshift(oper1, (int)ioper2, signed1, res.length));
            else
                // variable shift
                connect(res, var_shift_l (oper1, oper2, res.length, signed1));
            return;
        case RSH:
            if (ivtype2 != TDEVtype.VAR)
                // constant shift
                connect(res, rshift(oper1, (int)ioper2, signed1, res.length));
            else
                // variable shift
                connect(res, var_shift_r (oper1, oper2, res.length, signed1));
            return;
        case LROT:
            connect(res, lrot(oper1, (int)ioper2, res.length));
            return;
        case RROT:
            connect(res, rrot(oper1, (int)ioper2, res.length));
            return;
        case COND:
            owidth2 += ((!signed2 && signed3) ? 1 : 0);
            owidth3 += ((signed2 && !signed3) ? 1 : 0);
            width = Math.max(owidth2, owidth3);
            Net[]   n = new Net[width];
            oper2 = sig_expand(oper2, width, signed2);
            oper3 = sig_expand(oper3, width, signed3);
            for (int i=0 ; i<width ; i++)
                n[i] = OR(AND(oper1[0], oper2[i]), AND(INV(oper1[0]), oper3[i]));
            connect(res, n, signed2 || signed3);
            return;
        case ASSIGN:
            break;
        default:
            break;
        }
        return;
    }
   
    /**
     * Encode a parallel wait.
     * When the last block finish signal goes high the output goes high.
     * @param   inputs is a list containing clock signal, optional reset signal
     *          and multiple block finish signals
     * @param   output is a list containing the finish signal
     */
    public void TDEWAIT (TDEVarList inputs, TDEVarList output) {
        Net     out = output.getNet(0);
        int     n = inputs.size() - 2;  // 1st input is clock
        if (n == 1) {
            inputs.getNet(2, 0).connect(out);
            return;
        }
        Net     c = inputs.getNet(0);
        Net     r = inputs.getNet(1);
        Net[]   o = new Net[n];
        Net[]   q = netArray(n);
        if (r == null)
            r = out;
        else
            r = OR(r, out);
        for (int i=0 ; i<n ; i++) {
            o[i] = OR(inputs.getNet(i+2), q[i]);
            q[i].connect(FDRE(o[i], c, null, r, null));
        }
        out.connect(AND(args(o)));
    }

    /**
     * Encode a delay.
     * <p>params list contains -
     *          <ul>
     *          <li>integer - number of clock cycles to delay if constant, else 0
     *          <li>string array - initialiser words (hexadecimal)
     *          <li>.
     *          <li>. etc.
     *          </ul>
     *<p> inputs list contains -
     *          <ul>
     *          <li>clock signal
     *          <li>input signal array
     *          <li>delay signal array if variable delay, else null
     *          <li>enable signal or null
     *          <li>reset signal or null
     *          </ul>
     * <p>outputs list contains -
     *          <ul>
     *          <li>output signal array
     *          </ul>
     */
    public void TDEDEL (ArrayList<Object> params, TDEVarList inputs, TDEVarList outputs) {
        int                 n = ((Integer)params.get(0)).intValue();
        String[]   init = null;
        if (present(params, 1))
            init = (String[])params.get(1);
        int     i;
        Net     c = inputs.getNet(0);
        Net[]   in = inputs.getNetArray(1);
        Net[]   len = null;
        Net     step = Net.HI;
        Net     r = Net.LO;
        if (present(inputs, 2)) {
            Net[]   len_ = inputs.getNetArray(2);
            if (len_.length > srladdrwidth)
                throw new ExEx("variable delay line too long");
            len = new Net[srladdrwidth];
            for (i=0 ; i<len_.length ; i++)
                len[i] = len_[i];
            for ( ; i<srladdrwidth ; i++)
                len[i] = Net.LO;
        }
        if (present(inputs, 3))
            step = inputs.getNet(3);
        if (present(inputs, 4))
            r = inputs.getNet(4);
        Net[]   out = outputs.getNetArray(0);
        connect(out, del(n, in, len, c, step, r, init));
    }

    /**
     * Encode a queue diverge.
     * @param   inputs is a list containing 4 or more signals -
     *          <ul>
     *          <li>the clock
     *          <li>the reset signal
     *          <li>the queue availability signal from the source module
     *          <li>2 or more acknowledge signals from destination modules
     *          </ul>
     * @param   outputs is a list containing 3 or more signals -
     *          <ul>
     *          <li>the queue acknowledge signal to the source module
     *          <li>2 or more availability signals to destination modules
     *          </ul>
     */
    public void TDEDIVERGE (TDEVarList inputs, TDEVarList outputs) {
        Net c = inputs.getNet(0);
        Net reset = inputs.getNet(1);
        Net av = inputs.getNet(2);
        int modules = inputs.size() - 3;
        /*
        if (modules == 1) {
            outputs.getNet(1).connect(av);
            outputs.getNet(0).connect(inputs.getNet(2));
            return;
        }
        */
        Net[]   o = new Net[modules];
        Net[]   q = new Net[modules];
        Net     mav;
        Net     ack = new Net();
        Net     res = OR(ack, reset);
        for (int i=0 ; i<modules ; i++) {
            q[i] = new Net();
            o[i] = OR(inputs.getNet(i+3), q[i]);
            q[i].connect(FDRE(o[i], c, null, res, null));
            mav = AND(av, INV(q[i]));
            mav.connect(outputs.getNet(i+1));
        }
        ack.connect(AND(args(o)));
        outputs.getNet(0).connect(ack);
    }

    /**
     * Encode a queue buffer.
     * @param   params is a list containing 1 or 2 parameters -
     *          <ul>
     *          <li>the buffer depth
     *          <li>boolean - use FIFO hardware, or null for default action
     *          <li>boolean - use an extra output buffer register, or null
     *              for default action
     *          <li>an optional initial value
     *          </ul>
     * @param   inputs is a list containing 5 or more signals -
     *          <ul>
     *          <li>the input clock net or null if the same as the output clock
     *          <li>the output (or only) clock net
     *          <li>the input data array net
     *          <li>the write signal net
     *          <li>the output acknowledge signal net
     *          <li>an optional reset signal
     *          </ul>
     * @param   outputs is a list containing 4 signals -
     *          <ul>
     *          <li>the data signal net array
     *          <li>the read availability signal net
     *          <li>the write available signal net
     *          <li>optional buffer occupancy count for synchronous queue buffers
     *          <li>optional buffer free space count for synchronous queue buffers
     *          <li>optional buffer status synchronised to the read clock
     *              for asynchronous queue buffers
     *          <li>optional buffer status synchronised to the write clock
     *              for asynchronous queue buffers
     *          <li>an optional reset output signal
     *          </ul>
     */
    public void TDEQUEUEBUFFER (ArrayList<Object> params, TDEVarList inputs, TDEVarList outputs) {
        int         depth = ((Integer)params.get(0)).intValue();
        boolean     fifo = fifos_ok;
        boolean     queuereg = queuereg_ok;
        String      sinit = null;
        if (params.get(1) != null)
            fifo = ((Boolean)params.get(1)).booleanValue();
        if (params.get(2) != null)
            queuereg = ((Boolean)params.get(2)).booleanValue();
        if ((params.size() > 3) && (params.get(3) != null))
            sinit = (String)params.get(3);
        Net         ci = inputs.getNet(0);
        Net         co = inputs.getNet(1);
        int         width = inputs.getWidth(2);
        Net[]       in = inputs.getNetArray(2);
        Net         push = inputs.getNet(3);
        Net         pop = inputs.getNet(4);
        Net[]       out;
        Net         ne = outputs.getNet(1);
        Net         nf = outputs.getNet(2);
        Net[]       count = outputs.getNetArray(3);
        Net[]       free = outputs.getNetArray(4);
        Net[]       wstat = outputs.getNetArray(5);
        Net[]       rstat = outputs.getNetArray(6);
        Net         rr = (outputs.size() > 7) ? outputs.getNet(7) : null;
        Net         r = null;
        if ((inputs.size() > 5) && (inputs.get(5) != null))
            r = inputs.getNet(5);
        
        if (width == 0) {
            triggerbuffer(push, pop, ci, co, ne, nf, count, free, wstat, rstat, r, rr, depth);
            return;
        }
        
        Var     rcvar = inputs.getClkVar(1);
        Var     wcvar = (ci != null) ? inputs.getClkVar(0) : rcvar;
        
        if (depth == 0)
            out = queueunbuffered(in, push, pop, ne, nf);
        else if (depth == 2)
            out = queuebuffer(in, push, pop, co, ne, nf, r, rr, count, free, sinit);
        else if (ci == null)
            // Synchronous FIFO
            out = sync_buffer (in, push, pop, co, ne, nf, count, free, r, rr, fifo, queuereg, depth, wcvar, rcvar);
        else
            // Asynchronous FIFO
            out = async_buffer (in, push, pop, ci, co, ne, nf, wstat, rstat,r, rr, fifo, depth, wcvar, rcvar);
        if (width > 0)
            connect(out, outputs.getNetArray(0));
    }
    
    /**
     * Encode a priority encoder.
     * There are n inputs and outputs.
     * When a number of input signals are high the corresponding output
     * earliest in the output signal list will be high, all other output
     * signals being low. If there is a 2nd output signal
     * it will be high if none of the inputs are high.
     * @param   inputs is a list containing the input signals
     * @param   outputs is a list containing the output signals
     */
    public void TDEPRIORITY (TDEVarList inputs, TDEVarList outputs) {
        Net[]   i = inputs.getNetArray(0);
        Net[]   o = outputs.getNetArray(0);
        Net     e = outputs.getNet(1);
        int     ins = i.length;

        o[0].connect(i[0]);
        if (needCascadeGates()) {
            // Must use simple gates (CPLD).
            if (ins > 36)
                throw new ExEx("CPLD CODE ERROR - priority encoder > 36 inputs");
            for (int j=1 ; j<ins ; j++) {
                ArrayList<Net>   al = new ArrayList<Net>();
                for (int k=0 ; k<j ; k++)
                    al.add(INV(i[k]));
                al.add(i[j]);                
                o[j].connect(AND(al));
            }
            if (e != null)
                e.connect(INV(OR((Object)i)));
        } else if (ins <= lutwidth) {
            // Number of inputs <= LUT width - use LUTs.
            for (int j=1 ; j<ins ; j++) {
                StringBuffer    sb = new StringBuffer();
                for (int k=0 ; k<=j ; k++) {
                    if (k != 0)
                        sb.append("&");
                    if (k != j)
                        sb.append("~");
                    sb.append("I" + k);
                }
                o[j].connect(LUT(sb.toString(), (Object)subarray(i, 0, j)));
            }
            if (e != null)
                e.connect(INV(OR((Object)i)));
        } else {
            // Number of inputs > LUT width - use carry chain.
            /*
             * Old code.
             * AND used in the LUT so that the XORCY generates the
             * correct AND output between the carry and the input.
             * But the connection from the carry back to the LUT (AND)
             * input adds extra delay in each link of the chain. This
             * extra delay rapidly accumulates with chain length and so
             * is impractical - a pity since the code was quite neat!
             * Instead the AND must be added externally. This adds extra
             * delay but only to each stage in parallel, so it does not
             * grow with chain length.
            Net     carry;
            Net     s;
            carry = INV(i[0]);
            for (int j=1 ; j<ins ; j++) {
                s = AND(INV(i[j]), carry);
                if (j != 0)
                    o[j].connect(XORCY(s, carry));
                carry = MUXCY(s, Net.LO, carry);
            }
            if (e != null)
                e.connect(carry);
            */

            Net     carry;
            Net     s;
            carry = INV(i[0]);
            for (int j=1 ; j<ins ; j++) {
                s = INV(i[j]);
                o[j].connect(AND(i[j], carry));
                carry = MUXCY(s, Net.LO, carry);
            }
            if (e != null)
                e.connect(carry);
        }
    }
    
    /**
     * Encode a combinatorial-output RAM. The input and output signals
     * for port 1 will be null if only one port is used. Clock, data
     * input and write signals will be null if a port is not written. The
     * data initialisation parameter will be null if there is no
     * initialisation data.
     * The data may be any width. The address may be any width but in
     * practice should be 1 to 9 bits (maximum depth of 512) to avoid
     * excessive multiplexing logic and resultant speed limitation.
     * @param   params is a list containing 4 or more entries -
     *          <ul>
     *          <li>an integer giving the number of ports
     *          <li>an integer giving the data width in bits
     *          <li>an integer giving the address width in bits
     *          <li>an optional initialisation string array
     *          </ul>
     * @param   inputs is a list containing 8 signals -
     *          <ul>
     *          <li>the clock for port 0, or null
     *          <li>the address signal array for port 0
     *          <li>the data input signal array for port 0, or null
     *          <li>the write signal for port 0, or null
     *          <li>the clock for port 1, or null
     *          <li>the address signal array for port 1, or null
     *          <li>the data input signal array for port 1, or null
     *          <li>the write signal for port 1, or null
     *          </ul>
     * @param   outputs is a list containing 2 signals -
     *          <ul>
     *          <li>the data output signal array for port 0
     *          <li>the data output signal array for port 1, or null
     *          </ul>
     */
    public void TDECRAM (ArrayList<Object> params, TDEVarList inputs, TDEVarList outputs) {
        int         ports = ((Integer)params.get(0)).intValue();
        int         dwidth = ((Integer)params.get(1)).intValue();
        String[]    sinit = (params.size() > 3) ? (String[])params.get(3) : null;
        Net         clk0 = inputs.getNet(0);
        Net[]       addr0 = inputs.getNetArray(1);
        Net[]       idata0 = inputs.getNetArray(2);
        Net         write0 = inputs.getNet(3);


        if (ports == 1) {
            Net[]   odata0 = outputs.getNetArray(0);
            sram1(dwidth, clk0, write0, addr0, idata0, odata0, sinit);
            return;
        }

        Net     clk1 = inputs.getNet(4);
        Net[]   addr1 = inputs.getNetArray(5);
        Net[]   idata1 = inputs.getNetArray(6);
        Net     write1 = inputs.getNet(7);
        Net[]   odata0 = outputs.getNetArray(0);
        Net[]   odata1 = outputs.getNetArray(1);
        sram2(dwidth, clk0, write0, addr0, idata0, odata0, clk1, write1, addr1, idata1, odata1, sinit);
    }
    
    /**
     * Encode a registered-output RAM. The input and output signals
     * for port 1 will be null if only one port is used. Clock, data
     * input and write signals will be null if a port is not written. The
     * data initialisation parameter will be null if there is no
     * initialisation data.
     * Data may be any width.
     * Only the following address widths are allowed -
     * <pre>
     * XC2S, XCV - 8, 9, 10, 11 or 12 bits
     * XC3S, XC2V, XC2VP - 9, 10, 11, 12, 13 or 14 bits
     * </pre>
     * @param   params is a list containing 4 or more entries -
     *          <ul>
     *          <li>an integer giving the number of ports
     *          <li>an integer giving the block RAM data width in bits
     *          <li>an integer giving the block RAM address width in bits
     *          <li>an optional initialisation string array
     *          <li> String  - optional attribute key
     *          <li> String  - optional data for attribute key
     *          <li> ...     - other attribute pairs
     *          </ul>
     * @param   inputs is a list containing 10 signals -
     *          <ul>
     *          <li>the clock for port 0, or null
     *          <li>the address signal array for port 0
     *          <li>the data input signal array for port 0, or null
     *          <li>the read signal for port 0, or null
     *          <li>the write signal for port 0, or null
     *          <li>the clock for port 1, or null
     *          <li>the address signal array for port 1, or null
     *          <li>the data input signal array for port 1, or null
     *          <li>the read signal for port 1, or null
     *          <li>the write signal for port 1, or null
     *          </ul>
     * @param   outputs is a list containing 2 signals -
     *          <ul>
     *          <li>the data output signal array for port 0
     *          <li>the data output signal array for port 1, or null
     *          </ul>
     */
    public void TDERRAM (ArrayList<Object> params, TDEVarList inputs, TDEVarList outputs) {
        int         ports = ((Integer)params.get(0)).intValue();
        int         dwidth = ((Integer)params.get(1)).intValue();
        int         awidth = ((Integer)params.get(2)).intValue();
        String[]    sinit = (params.size() > 3) ? (String[])params.get(3) : null;
        Net         clk0 = inputs.getNet(0);
        Net[]       addr0 = inputs.getNetArray(1);
        Net[]       idata0 =  inputs.getNetArray(2);
        Net         read0 = inputs.getNet(3);
        Net         write0 = inputs.getNet(4);      
        
        ArrayList<String>   properties = new ArrayList<String>();
        boolean             continuous = false;

        for (int i=4 ; i<params.size() ; ) {
            String  key = (String)params.get(i++);
            if (key.startsWith("write_mode")) {
                String svalue = (String)params.get(i++);
                properties.add(key.toUpperCase());
                properties.add(svalue.toUpperCase());
            } else if (key.equals("continuous"))
                continuous = (Boolean)params.get(i++);
        }
        
        if (ports == 1) {
            Net[]   odata0 = netArray(dwidth);
            rram1(dwidth, awidth, read0, write0, clk0, addr0, idata0, odata0, sinit, properties, continuous);
            connect(outputs.getNetArray(0), odata0);
            return;
        }

        Net     clk1 = inputs.getNet(5);
        Net[]   addr1 = inputs.getNetArray(6);
        Net[]   idata1 = inputs.getNetArray(7);
        Net     read1 = inputs.getNet(8);
        Net     write1 = inputs.getNet(9);
        Net[]   odata0 = netArray(dwidth);
        Net[]   odata1 = netArray(dwidth);
        rram2(
                dwidth, awidth,
                read0, write0, clk0, addr0, idata0, odata0,
                read1, write1, clk1, addr1, idata1, odata1,
                sinit, properties, continuous
            );
        if (odata0 != null)
            connect(outputs.getNetArray(0), odata0);
        if (odata1 != null)
            connect(outputs.getNetArray(1), odata1);
    }
    
    
    /**
     * Encode a new EDIF element.
     * NEED TO SORT OUT width, swidth, port width convention etc.
     * @param   params is a list containing the element name string, the block name string (may be null), followed by
     *          quads of -
     *          <ul>
     *          <li>a string   - the port name
     *          <li>an integer - 0 for input, 1 for output, 2 for three-state output or
     *                           3 for a clock input
     *          <li>an integer - port array size - 0 for non-array
     *          <li>an integer - port array format - 0 for non-array, 1 for discrete pins, 2 for array form
     *          </ul>
     *          If the type entry is 4 then the triple is a property value instead of a port.
     *          The quad is then -
     *          <ul>
     *          <li>a string   - the property name
     *          <li>an integer - 4
     *          <li>a string   - the property value
     *          <li>an integer - 0
     *          </ul>
     * @param   inputs is a list of input signals
     * @param   outputs is a list of output signals
     */
    public void TDEELEMENT (ArrayList<Object> params, TDEVarList inputs, TDEVarList outputs) {
        int     i = 0;
        int     indexi = 0;
        int     indexo = 0;
        boolean signed;
        Net[]   na = null;
        Net     n = null;
        
        String  ename = (String)params.get(i++);    // element name
        String  bname = (String)params.get(i++);    // block name or null
        Element e = new Element(ename, bname);
        current_element = e;
        
        for (i=2 ; i<params.size() ; i+=4) {
            String  name = (String)params.get(i);                       // port or property name
            int     portType = ((Integer)params.get(i+1)).intValue();   // port type (0, 1, 2, 3) or property indicator (4)
            
            if (portType == 4) {
                // Is a property, not a port.
                String  value = (String)params.get(i+2);
                e.addProperty(name, value);
                continue;
            }
            
            int     width = ((Integer)params.get(i+2)).intValue();          // array size
            int     arrayformat = ((Integer)params.get(i+3)).intValue();    // array format
            signed = false;
            if ((portType == 0) || (portType == 3)) {
                if (width != 0) {
                    signed = inputs.isSigned(indexi);
                    na = inputs.getNetArray(indexi++);
                } else
                    n = inputs.getNet(indexi++);
            } else {
                if (width != 0)
                    na = outputs.getNetArray(indexo++);
                else
                    n = outputs.getNet(indexo++);
            }

            if (width != 0) {
                switch (portType) {
                case 0:
                case 3:
                    na = sig_expand(na, width, signed);
                    e.addInputArray(name, na, arrayformat);
                    e.addInPort(name, width, arrayformat);
                    break;
                case 1:
                    e.addOutputArray(name, na, arrayformat);
                    e.addOutPort(name, width, arrayformat);
                    break;
                case 2:
                    e.addTSArray(name, na, arrayformat);
                    e.addTSPort(name, width, arrayformat);
                }
            } else {
                switch (portType) {
                case 0:
                case 3:
                    e.addInput(name, n);
                    break;
                case 1:
                    e.addOutput(name, n);
                    break;
                case 2:
                    e.addTS(name, n);
                }
            }
        }
    }
    
    /**
     * Add an EDIF port.
     * If the signal input is an array the port name will have 0, 1, 2 etc. appended.
     * @param   params is a list containing -
     *          <ul>
     *          <li>an integer - 0 for input, 1 for output, 2 for three-state output
     *          <li>a string - the port name
     *          </ul>
     * @param   input is a list containing -
     *          <ul>
     *          <li>the signal or signal array to be connected
     *          </ul>
     */
    public void TDEPORT (ArrayList<Object> params, TDEVarList input) {
        int     port_type = ((Integer)params.get(0)).intValue();
        String  port_name = (String)params.get(1);
        int     width = input.getWidth(0);
        Net     n;
        String  s;
        if (width > 1) {
            Net[]   na = input.getNetArray(0);
            for (int i=0 ; i<width ; i++) {
                n = na[i];
                s = "(member " + port_name + " " + i + ")";
                n.setPort(s, ptypes[port_type]);
            }
            s = "(array (rename " + port_name + " \"" + port_name +
                    "[0:" + (width-1) + "]\") " + width + ")";
            portmap.put(port_name, new Port(s, ptypes[port_type]));
        } else {
            n = input.getNet(0);
            n.setPort(port_name, ptypes[port_type]);
            portmap.put(port_name, new Port(port_name, ptypes[port_type]));
        }
    }
   
    /**
     * Process a special function.
     * The first parameter gives the function -
     *  <ul>
     *  <li>0 add a line to NCF
     *  <li>1 add a line to XDC file
     *  </ul>
     *
     * @param   params is a list or parameters
     * @param   inputs is a list of input signals
     * @param   outputs is a list of output signals
     *
     *
     * <p>For type 0 or 1-
     * <p>params list contains -
     *          <ul>
     *          <li>integer - 0 or 1
     *          <li>string - a format string
     *          <li>string - an optional port name
     *          </ul>
     * <p>inputs list contains -
     *          <ul>
     *          <li>an optional signal
     *          <li>an optional signal
     *          <li>.
     *          <li>.
     *          </ul>
     * <p>outputs list is empty
     *
     * The format string may contain %n and %N conversions. %n is replaced by the identifier of the next
     * input signal argument. %N is similar to the above but changes the identifier to upper case and
     * removes any leading "GLOB_" string  This is used for generating clock timing group names from the
     * clock signal identifier.
     */
    public void TDESPECIAL (ArrayList<Object> params, TDEVarList inputs, TDEVarList outputs) {
        int     i;
        int     code;
        
        switch (code=((Integer)params.get(0)).intValue()) {
        case 0: // add a line to the NCF
        case 1: // add a line to the XDC file
            String      format = (String)params.get(1);
            String      portname = (params.size() > 2) ? (String)params.get(2) : null;
            Constraint  con = (code == 0) ? addNCFConstraint(format, portname) : addXDCConstraint(format, portname);
            for (i=0 ; i<inputs.size(); i++)
                con.addNet(inputs.getNet(i));
            return;
        /*
        case 100:   // diagnostic signals - compiler debugging only!
            for (int j=0 ; j<outputs.size() ; j++) {
                if (outputs.getWidth(j) > 1)
                    outputs.getNetArray(j);
                else
                    outputs.getNet(j);
            }
        */
        default:
            throw new ExEx("TDE special operation unused code " + code);
        }
    }
    
    /**
     * Apply family-specific optimisation to some TDEs.
     */   
    public void optimiseTDEs () {
        XTDEOptimise.optimiseTDEs();
    }

    /**
     * Iteratively optimise gates and flip-flops.
     * @return  the number of optimisation passes
     */
    public int optimiseNetlist () {
        return(XNetlistOptimise.optimiseNetlist());
    }

    /**
     * Convert SELECTOR elements to gates.
     * Convert all general AND, OR and XOR gates to appropriate
     * low-level elements.
     */
    public void convertGenericToSpecific () {
        XNetlistOptimise.convertSelectors();
        XNetlistOptimise.convertGenericToSpecific();
    }
   
    /**
     * Verify that there are no element problems. A gate element
     * must not have zero inputs.
     * All elements are examined and false is returned if any violate the
     * above rules. Violations are printed.
     * @return  true if all elements are OK
     */
    public boolean verify () {
        boolean     success = true;
        for (Element e : Element.getInstances()) {
            switch (e.getType()) {
            case INV:
            case AND:
            case OR:
            case XOR:
            case XNOR:
            case MUXCY:
            case MUXF5:
            case MUXF6:
            case MUXF7:
                if (e.getInPorts().size() == 0) {
                    msg(e.getCellName() + " " + e.getIdent() + " has no inputs");
                    success = false;
                    break;
                }
                for (Map.Entry<String,Net> me : e.getInPorts().entrySet()) {
                    String      pinname  = me.getKey();
                    Net         net = me.getValue();
                    if (net == null) {
                        msg(e.getCellName() + " " + e.getIdent() + " has null input on pin " + pinname);
                        success = false;
                        break;
                    }
                }
                break;
            /*
            case FDRSE:
            case FDSE:
            case FDRE:
            case FDCPE:
            case FDPE:
            case FDCE:
            default:
            */
            case BLK_RAM:
                break;
            case CLB_RAM:
                break;
            case DSP48:
                break;
            case FDRE:
                break;
            case FDRSE:
                break;
            case FDSE:
                break;
            case LUT:
                break;
            case MULREG:
                break;
            case MULT18X18:
                break;
            case MULT18X18S:
                break;
            case MULT25X18:
                break;
            case MULT25X18S:
                break;
            case MULT_AND:
                break;
            case MUXF8:
                break;
            case NONE:
                break;
            case ORCY:
                break;
            case REMOVED:
                break;
            case SRL:
                break;
            case XORCY:
                break;
            default:
                break;
            }
        }
        if (!success)
            msg(" ");
        return(success);
    }
    
    /**
     * Find a Net given a port pin designation.
     * @param   des is the port designation
     * @return  an input or output Net or null
     */
    public Net netFromPin (String des) {
        if (portmap.containsKey(des)) {
            Port p = portmap.get(des);
            if (p.inet != null)
                return(p.inet);
            
            return(p.onet);
        }
        return(null);
    }
    
    /**
     * Add an I/O input port to the port map.
     * @param   loc is the I/O port location (pad/pin)
     * @param   n is the output net from the input port
     */
    public void addInputPort (String loc, Net n) {
        String  portname = "PORT_" + loc;
        Port     p = null;
        if (portmap.containsKey(portname)) {
            p = portmap.get(portname);
            switch (p.type) {
            case NONE:
                throw new ExEx("XTDECode/addInputPort() - SYSTEM ERROR! - should not get TDEVPtype.NONE here");
            case INPUT:
                throw new ExEx("Input port " + portname + " duplicated (already an input port)");
            case OUTPUT:
                p.type = TDEVPtype.INOUT;
                p.inet = n;
                n.setPort(portname, TDEVPtype.INOUT);
                return;
            case INOUT:
                throw new ExEx("Input port " + portname + " duplicated (already a bidirectional port)");
            }
        }
        p = new Port(portname, TDEVPtype.INPUT, loc);
        p.inet = n;
        n.setPort(portname, TDEVPtype.INPUT);
        portmap.put(portname, p);
    }

    /**
     * Add an I/O output port to the port map.
     * @param   loc is the port designation
     * @param   n is the input net to the output port
     */
    public void addOutputPort (String loc, Net n) {
        String  portname = "PORT_" + loc;
        Port     p = null;
        if (portmap.containsKey(portname)) {
            p = portmap.get(portname);
            switch (p.type) {
            case NONE:
                throw new ExEx("XTDECode/addOutputPort() - SYSTEM ERROR! - should not get TDEVPtype.NONE here");
            case INPUT:
                p.type = TDEVPtype.INOUT;
                p.onet = n;
                n.setPort(portname, TDEVPtype.INOUT);
                return;
            case OUTPUT:
                throw new ExEx("Output port " + portname + " duplicated (already an output port)");
            case INOUT:
                throw new ExEx("Output port " + portname + " duplicated (already a bidirectional port)");
            }
        }
        p = new Port(portname, TDEVPtype.OUTPUT, loc);
        p.onet = n;
        n.setPort(portname, TDEVPtype.OUTPUT);
        portmap.put(portname, p);
    }
    
    /**
     * Return the EDIF file name extension.
     * @return  the EDIF file name extension String
     */
    public String EDIFFileNameExtension () {
        return(".edn");
    }
    
    /**
     * Generate any netlist constraints file(s).
     * @param   author is a string to insert in a comment line in the
     *          constraint file(s)
     * @param   netlistcomments is a string to insert in a comment line
     *          in the constraint file(s)
     */
    // ISE NCF constraints are contained in 3 places -
    //  Element.ncf_constraints which is a TreeMap whose keys are constraint names
    //      and associated map values are constraint value strings
    //  Net.ncf_constraints which is a TreeMap whose keys are constraint names
    //      and associated map values are constraint value strings
    //  XTDECode in ArrayList XTDECode.ncf containing constraint
    //      strings not associated with elements or nets
    // Vivado XDC constraints are contained in ArrayList XTDECODE.xdc.
    public void outputConstraints (PrintWriter pw, String author, String[] netlistcomments) {
        
        pw.println("# program 3PL version " + fullVersion);
        pw.println("# timestamp " + currentDate);
        if ((author != null) && (author.length() > 0))
            pw.println("# Author \"" + author + "\"");
        if ((netlistcomments != null) && (netlistcomments.length > 0)) {
            for (int i=0 ; i<netlistcomments.length ; i++)
                pw.println("# " + netlistcomments[i]);
        }

	String tool = stringDir("postProcessTool");
        if (tool.equals("ise") && haveNCFConstraints()) {
            // If using ISE and have constraints, generate an ISE NCF constraints file.
            outputNCFConstraints(pw);
            return;
        }
        if (tool.equals("vivado") && haveXDCConstraints()) {
            // If using Vivado and have constraints, generate a Vivado XDC constraints file.
            outputXDCConstraints(pw);
            return;
        }
    }

    /**
     * Determine if a list entry at a specified index is present.
     * @param   a is the list
     * @param   index specifies the position in the list
     * @return  true if a non-null item is present at the specified
     *          position in the list
     */
    private static boolean present (ArrayList<?> a, int index) {
        if (index >= a.size())
            return(false);
        return(a.get(index) != null);
    }
}
