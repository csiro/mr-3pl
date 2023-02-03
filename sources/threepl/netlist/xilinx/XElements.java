package threepl.netlist.xilinx;

import java.util.ArrayList;

import threepl.codegen.TDE;
import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.netlist.Element;
import threepl.netlist.GenFunctions;
import threepl.netlist.Net;
import threepl.netlist.NetConstants;
import threepl.parser.Constant;


/**
 * <p>This class contains methods which implement Xilinx common
 * unified library elements. These are the low level elements from
 * which the output EDIF netlist is generated. All higher level
 * functions call these, directly or indirectly.</p>
 * 
 * <p>Elements specific to a particular FPGA family are contained in
 * subclasses X2S, X3S, XCV, XC2V, XC2VP etc.</p>
 * 
 * <p>The arguments and return values are not a consistent scheme
 * through all elements.</p>
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
public abstract class XElements extends GenFunctions implements NetConstants, Constant, TDEConstants {
    static int                      arrayformat;    // Format of element ports which are arrays, 
                                                    // Value 1 causes element port arrays to be defined as A0, A1, A2, A3 etc.
                                                    // as seems to be required for ISE.
                                                    // Value 2 causes element port arrays to be defined as  A[0:n]  as seems
                                                    // to be required for Vivado.
    

    public XElements () {
        // create sources for VCC and GND signals
        Net.LO = new Net("GND", true);
        Net.HI = new Net("VCC", true);
        Element.gnd = new Element("GND");
        Element.gnd.addOutput("G", Net.LO);
        Element.vcc = new Element("VCC");
        Element.vcc.addOutput("P", Net.HI);
    }
    
    /**
     * Construct a LUT.
     * @param   expr is the LUT boolean expression
     * @param   args is an array of two or more of type Net
     * @return  the output Net
     */
    public Net LUT (String expr, Object ... args) {
        return(gate(Gtype.LUT, expr, args));
    }

    /**
     * Construct an INV.
     * @param   i is the input net
     * @return  the output net
     */
    public Net INV (Net i) {
        Element c = new Element("INV", Gtype.INV);
        Net     o = new Net();
        if (i == null)
            throw new ExEx("INV null input");
        c.addInput("I", i);
        c.addOutput("O", o);
        return(o);
    }

    /**
     * Construct an AND.
     * @param   args is an array of
     * <ul>
     * <li> two or more of type Net
     * <li> a single ArrayList which contains two or more Nets
     * <li> a single array of two or more Net
     * </ul>
     * @return  the output Net
     */
    public Net AND (Object... args) {
        return(gate(Gtype.AND, args));
    }

    /**
     * Construct an OR.
     * @param   args is an array of
     * <ul>
     * <li> two or more of type Net
     * <li> a single ArrayList which contains two or more Nets
     * <li> a single array of two or more Net
     * </ul>
     * @return  the output Net
     */
    public Net OR (Object... args) {
        return(gate(Gtype.OR, args));
    }

    /**
     * Construct an XOR.
     * @param   args is an array of
     * <ul>
     * <li> two or more of type Net
     * <li> a single ArrayList which contains two or more Nets
     * <li> a single array of two or more Net
     * </ul>
     * @return  the output Net
     */
    public Net XOR (Object... args) {
        return(gate(Gtype.XOR, args));
    }
 
    /**
     * Construct an XNOR.
     * @param   args is an array of
     * <ul>
     * <li> two or more of type Net
     * <li> a single ArrayList which contains two or more Nets
     * <li> a single array of two or more Net
     * </ul>
     * @return  the output Net
     */
    public Net XNOR (Object... args) {
        return(gate(Gtype.XNOR, args));
    }
   
    // Common code for an AND, OR or XOR gate.
    private Net gate (Gtype gtype, Object[] args) {
        return(gate(gtype, null, args));
    }

    // Common code for an AND, OR or XOR gate or a LUT.
    // It is implemented as an elemental gate, I.E. a single
    // LUT. Gates will later be expanded to use an F5 MUX or
    // gates threaded onto a carry chain.
    @SuppressWarnings("unchecked")
    private Net gate (Gtype gtype, String expr, Object[] args) {
        int     n;
        Element e;
        Net     o = new Net();
        int     i = 0;
        if (args[0] instanceof ArrayList) {
            ArrayList<Net>   al = (ArrayList<Net>)args[0];
            n = al.size();
            if (n == 1)
                return(al.get(0));
            e = new Element(gtype.toString()+n, gtype);
            for (Net in : al) {
                if (in == null)
                    throw new ExEx("AND null input");
                e.addInput("I"+i++, in);
            }
        } else if (args[0] instanceof Net[]) {
            Net[]   ins = (Net[])args[0];
            n = ins.length;
            if (n == 1)
                return(ins[0]);
            e = new Element(gtype.toString()+n, gtype);
            for ( ; i<n ; i++)
                e.addInput("I"+i, ins[i]);
        } else {
            n = args.length;
            if (n == 1)
                return((Net)args[0]);
            e = new Element(gtype.toString()+n, gtype);
            for ( ; i<n ; i++)
                e.addInput("I"+i, (Net)args[i]);
        }
        e.addOutput("O", o);
        if (expr != null)
            e.addExpression(expr);
        return(o);
    }

    /**
     * Construct an SRL16E shift register.
     * @param   d is the data input net
     * @param   c is the clock net
     * @param   ce is the clock enable net
     * @param   a is the address input net array of maximum size 4
     * @param   sinit is the 16 bit initialisation data - 4 hexadecimal digits
     * @return  the data output Net
     */
    public Net SRL16E (Net d, Net c, Net ce, Net[] a, String sinit) {
        Element e = new Element("SRL16E", Gtype.SRL);
        Net     o = new Net();
        
        if (a.length < 4) {
            Net[] aa = new Net[4];
            for (int i=0 ; i<a.length ; i++)
                aa[i] = a[i];
            for (int i=a.length ; i<4 ; i++)
                aa[i] = Net.LO;
            a = aa;
        }
        
        e.addInput("D", d);
        e.addInput("CLK", c);
        if (ce != null)
            e.addInput("CE", ce);
        else
            e.addInput("CE", Net.HI);
        e.addInputArray("A", a, arrayformat);
        e.addOutput("Q", o);
	e.addProperty("INIT", stringsize(sinit, 4));
        return(o);
    }

    /**
     * Construct an SRL32E shift register.
     * @param   d is the data input net
     * @param   c is the clock net
     * @param   ce is the clock enable net
     * @param   a is the address input net array of maximum size 5
     * @param   sinit is the 32 bit initialisation data - 5 hexadecimal digits
     * @param   q31 is true if the Q31 output is to be used instead of the
     *          normal Q output
     * @return  the data output Net
     */
    public Net SRL32E (Net d, Net c, Net ce, Net[] a, String sinit, boolean q31) {
        Element e = new Element("SRLC32E", Gtype.SRL);
        Net     o = new Net();
        Net     dummy = new Net();
        
        if (a.length < 5) {
            Net[] aa = new Net[5];
            for (int i=0 ; i<aa.length ; i++)
                aa[i] = a[i];
            for (int i=aa.length ; i<5 ; i++)
                aa[i] = Net.LO;
            a = aa;
        }
        
        e.addInput("D", d);
        e.addInput("CLK", c);
        if (ce != null)
            e.addInput("CE", ce);
        else
            e.addInput("CE", Net.HI);
        e.addInputArray("A", a, arrayformat);
        if (q31) {
            e.addOutput("Q", dummy);    // to ensure port is in cell definition and keep vivado happy
            e.addOutput("Q31", o);
        } else {
            e.addOutput("Q", o);
            e.addOutput("Q31", dummy);  // as above
        }
        e.addProperty("INIT", stringsize(sinit, 8));
        return(o);
    }
    
    /*
     * Code for creating a flip-flop with synchronous reset.
     * For earlier FPGAs an FDRSE will be generated, and for later
     * FPGAs an FDRE or FDSE.
     * A unique identifier will be generated for the element.
     * @param   d is the data input net
     * @param   c is the clock input net
     * @param   ce is the clock enable input net
     * @param   r is the synchronous reset input - may be null
     * @param   sinit is an initialisation character, "R" or "S" or null (default "R")
     * @return  the flip-flop output net
     */
    public Net FDRE (
        Net     d,
        Net     c,
        Net     ce,
        Net     r,
        String  sinit
    ) {
        return(FDRSE(null, d, c, ce, r, Net.LO, sinit, false));        
    }
    
    /*
     * Code for creating a flip-flop with synchronous set.
     * For earlier FPGAs an FDRSE will be generated, and for later
     * FPGAs an FDRE or FDSE.
     * A unique identifier will be generated for the element.
     * @param   d is the data input net
     * @param   c is the clock input net
     * @param   ce is the clock enable input net
     * @param   s is the synchronous set input
     * @param   sinit is an initialisation character, "R" or "S"
     * @return  the flip-flop output net
     */
    public Net FDSE (
        Net     d,
        Net     c,
        Net     ce,
        Net     s,
        String  sinit
    ) {
        return(FDRSE(null, d, c, ce, Net.LO, s, sinit, false));        
    }
    
    /*
     * Code for creating a flip-flop with asynchronous clear.
     * For earlier FPGAs an FDCPE will be generated, and for later
     * FPGAs an FDCE or FDPE.
     * A unique identifier will be generated for the element.
     * @param   d is the data input net
     * @param   c is the clock input net
     * @param   ce is the clock enable input net
     * @param   clr is the asynchronous clear input
     * @param   sinit is an initialisation character, "R" or "S"
     * @return  the flip-flop output net
     */
    public Net FDCE (
        Net     d,
        Net     c,
        Net     ce,
        Net     clr,
        String  sinit
    ) {
        return(FDCPE(null, d, c, ce, clr, Net.LO, sinit, false));        
    }
    
    /*
     * Code for creating a synchronous flip-flop with synchronous reset and set.
     * For earlier FPGAs an FDRSE will be generated, and for later
     * FPGAs an FDRE or FDSE. In the latter case where both R and S inputs are used
     * the S input will be ORed with the D and CE inputs for an FDRE. Where R is not used
     * an FDSE will be generated.
     * @param   bname is the netlist element instance identifier - may be null in which case a unique
     * identifier will be generated
     * @param   d is the data input net
     * @param   c is the clock input net
     * @param   ce is the clock enable input net
     * @param   r is the synchronous reset input
     * @param   s is the synchronous set input
     * @param   sinit is an initialisation character, "R" or "S"
     * @param   iob is true if the flip-flop should be placed in an I/O block rather than a CLB
     * @return  the flip-flop output net
     */
    public Net FDRSE (
        String              bname,
        Net                 d,
        Net                 c,
        Net                 ce,
        Net                 r,
        Net                 s,
        String              sinit,
        boolean             iob
    ) {
        Net     q = new Net();
        Element e;
        if (needFDRSEpatch() && (r != Net.LO) && (s != Net.LO)) {
            d = OR(d, s);
            ce = OR(ce, s);
        }
        if (needFDRSEpatch()) {
            if ((r == Net.LO) && (s != Net.LO)) {
                e = new Element("FDSE", bname, Gtype.FDSE);
                e.addInput("D", d);
                e.addInput("C", c);
                e.addInput("CE", ce);
                e.addInput("S", s);
            } else {
                e = new Element("FDRE", bname, Gtype.FDRE);
                e.addInput("D", d);
                e.addInput("C", c);
                e.addInput("CE", ce);
                e.addInput("R", r);
            }
        } else {
            e = new Element("FDRSE", bname, Gtype.FDRSE);
            e.addInput("D", d);
            e.addInput("C", c);
            e.addInput("CE", ce);
            e.addInput("R", r);
            e.addInput("S", s);
        }
        e.addOutput("Q", q);
         if (sinit != null)
            e.addProperty("INIT", sinit);
        if (iob)
            e.addProperty("IOB", "TRUE");
        return(q);
    }
    
    /*
     * Code for creating an asynchronous clear/preset flip-flop.
     * @param   bname is the netlist element instance identifier - may be null in which case a unique
     * identifier will be generated
     * @param   d is the data input net
     * @param   c is the clock input net
     * @param   ce is the clock enable input net
     * @param   clr is the asynchronous clear input
     * @param   pre is the asynchronous preset input
     * @param   sinit is an initialisation character, "R" or "S"
     * @param   iob is true if the flip-flop should be placed in an I/O block rather than a CLB
     * @return  the flip-flop output net
     */
    public Net FDCPE (
        String              bname,
        Net                 d,
        Net                 c,
        Net                 ce,
        Net                 clr,
        Net                 pre,
        String              sinit,
        boolean             iob
    ) {
        Net     q = new Net();
        if (ce == null)
            ce = Net.HI;
        Element e;
        if (needFDRSEpatch() && (clr != Net.LO) && (pre != Net.LO))
            throw new ExEx("DFF with asynchronous clr/pre cannot have both these inputs connected");
        if (needFDRSEpatch()) {
            if ((clr == Net.LO) && (pre != Net.LO)) {
                e = new Element("FDPE", bname);
                e.addInput("D", d);
                e.addInput("C", c);
                e.addInput("CE", ce);
                e.addInput("PRE", pre);
            } else {
                e = new Element("FDCE", bname);
                e.addInput("D", d);
                e.addInput("C", c);
                e.addInput("CE", ce);
                e.addInput("CLR", clr);
            }
        } else {
            e = new Element("FDCPE", bname);
            e.addInput("D", d);
            e.addInput("C", c);
            e.addInput("CE", ce);
            e.addInput("CLR", clr);
            e.addInput("PRE", pre);
        }
        e.addOutput("Q", q);
        if (sinit != null)
            e.addProperty("INIT", sinit);
        if (iob)
            e.addProperty("IOB", "TRUE");
        return(q);
    }
    
    /**
     * Construct a MULT_AND.
     * @param   i0 is the associated LUT I0 input
     * @param   i1 is the associated LUT I1 input
     * @return  the output net
     */
    public Net MULT_AND (Net i0, Net i1) {
        Element c = new Element("MULT_AND", Gtype.MULT_AND);
        Net     lo = new Net();
        if (i0 == null)
            throw new ExEx("MULT_AND null i0");
        if (i1 == null)
            throw new ExEx("MULT_AND null i1");
        c.addInput("I0", i0);
        c.addInput("I1", i1);
        c.addOutput("LO", lo);
        return(lo);
    }
    
    /**
     * Construct a MUXCY.
     * @param   s is the control net
     * @param   di is the carry input net when s is low
     * @param   ci is the carry input net when s is high
     * @return  the output net
     */
    public Net MUXCY (Net s, Net di, Net ci) {
        if (s == null)
            throw new ExEx("MUXCY null s");
        if (ci == null)
            throw new ExEx("MUXCY null ci");
        if (di == null)
            throw new ExEx("MUXCY null di");

        if (ci== di)
            return(ci);
        if (s == Net.LO)
            return(di);
        if (s == Net.HI)
            return(ci);

        Element c = new Element("MUXCY", Gtype.MUXCY);
        Net     o = new Net();
        c.addInput("S", s);
        c.addInput("CI", ci);
        c.addInput("DI", di);
        c.addOutput("O", o);
        return(o);
    }
    
    /**
     * Construct a MUXF5.
     * @param   s is the control net
     * @param   i0 is the input net when s is low
     * @param   i1 is the input net when s is high
     * @return  the output net
     */
    public Net MUXF5 (Net s, Net i0, Net i1) {
        Element c = new Element("MUXF5", Gtype.MUXF5);
        Net     o = new Net();
        if (s == null)
            throw new ExEx("MUXF5 null s");
        if (i0 == null)
            throw new ExEx("MUXF5 null i0");
        if (i1 == null)
            throw new ExEx("MUXF5 null i1");
        c.addInput("S", s);
        c.addInput("I0", i0);
        c.addInput("I1", i1);
        c.addOutput("O", o);
        return(o);
    }
    
    /**
     * Construct a MUXF6.
     * @param   s is the control net
     * @param   i0 is the input net when s is low
     * @param   i1 is the input net when s is high
     * @return  the output net
     */
    public Net MUXF6 (Net s, Net i0, Net i1) {
        Element c = new Element("MUXF6", Gtype.MUXF6);
        Net     o = new Net();
        if (s == null)
            throw new ExEx("MUXF6 null s");
        if (i0 == null)
            throw new ExEx("MUXF6 null i0");
        if (i1 == null)
            throw new ExEx("MUXF6 null i1");
        c.addInput("S", s);
        c.addInput("I0", i0);
        c.addInput("I1", i1);
        c.addOutput("O", o);
        return(o);
    }
    
    /**
     * Construct an XORCY.
     * @param   li is the LUT input net
     * @param   ci is the carry input net
     * @return  the output net
     */
    public Net XORCY (Net li, Net ci) {
        Element c = new Element("XORCY", Gtype.XORCY);
        Net     o = new Net();
        if (li == null)
            throw new ExEx("XORCY null li");
        if (ci == null)
            throw new ExEx("XORCY null ci");
        c.addInput("LI", li);
        c.addInput("CI", ci);
        c.addOutput("O", o);
        return(o);
    }

    /**
     * Construct a BUFG.
     * @param   i is the input net
     * @param   loc is the source file location
     * @return  the output net
     */
    public Net BUFG (Net i, String loc) {
        Element c = new Element("BUFG");
        Net     o = new Net();
        if (i == null)
            throw new ExEx("BUFG null i");
        c.addInput("I", i);
        c.addOutput("O", o);
        if ((loc != null) && (loc.length() != 0))
            c.addProperty("LOC", loc);
        return(o);
    }

    /**
     * Construct a BUFE.
     * @param   i is the input net
     * @param   e is the enable net
     * @return  the output net
     */
    public Net BUFE (Net i, Net e) {
        Element c = new Element("BUFE");
        Net     o = new Net();
        if (i == null)
            throw new ExEx("BUFE null i");
        if (e == null)
            throw new ExEx("BUFE null e");
        c.addInput("I", i);
        c.addInput("E", e);
        c.addTS("O", o);
        return(o);
    }

    /**
     * Construct a BUFT.
     * @param   i is the input net
     * @param   t is the high-Z select net
     * @return  the output net
     */
    public Net BUFT (Net i, Net t) {
        Element c = new Element("BUFT");
        Net     o = new Net();
        if (i == null)
            throw new ExEx("BUFT null i");
        if (t == null)
            throw new ExEx("BUFT null t");
        c.addInput("I", i);
        c.addInput("T", t);
        c.addOutput("O", o);
        return(o);
    }

    /**
     * Construct an IBUF.
     * @param   o is the output net
     * @param   i is the input net
     * @param   id is the element block identifier or null
     * @return  the element
     */
    public Element IBUF (Net o, Net i, String id) {
        Element c = new Element("IBUF", id);
        if (i == null)
            throw new ExEx("IBUF null i");
        c.addInput("I", i);
        c.addOutput("O", o);
        return(c);
    }

    /**
     * Construct an IBUFDS.
     * @param   o is the output net
     * @param   i is the input net
     * @param   ib is the negative input net
     * @param   id is the element block identifier or null
     * @return  the element
     */
    public Element IBUFDS (Net o, Net i, Net ib, String id) {
        Element c = new Element("IBUFDS", id);
        if (i == null)
            throw new ExEx("IBUFDS null I");
        if (ib == null)
            throw new ExEx("IBUFDS null IB");
        c.addInput("I", i);
        c.addInput("IB", ib);
        c.addOutput("O", o);
        return(c);
    }

    /**
     * Construct an IBUFDS_DIFF_OUT.
     * @param   o is the differential +ve output net
     * @param   od is the differential -ve output net
     * @param   i is the input net
     * @param   ib is the negative input net
     * @param   id is the element block identifier or null
     * @return  the element
     */
    public Element IBUFDS_DIFF_OUT (Net o, Net od, Net i, Net ib, String id) {
        Element c = new Element("IBUFDS_DIFF_OUT", id);
        if (i == null)
            throw new ExEx("IBUFDS_DIFF_OUT null I");
        if (ib == null)
            throw new ExEx("IBUFDS_DIFF_OUT null IB");
        c.addInput("I", i);
        c.addInput("IB", ib);
        c.addOutput("O", o);
        c.addOutput("OB", od);
        return(c);
    }

    /**
     * Construct an IBUFG with a specified element identifier.
     * @param   o is the output net
     * @param   i is the input net
     * @param   id is the element block identifier or null
     * @return  the element
     */
    public Element IBUFG (Net o, Net i, String id) {
        Element c = new Element("IBUFG", id);
        if (i == null)
            throw new ExEx("IBUFG null I");
        c.addInput("I", i);
        c.addOutput("O", o);
        return(c);
    }

    /**
     * Construct an IBUFGDS.
     * @param   o is the output net
     * @param   i is the positive input net
     * @param   ib is the negative input net
     * @param   id is the element block identifier or null
     * @return  the element
     */
    public Element IBUFGDS (Net o, Net i, Net ib, String id) {
        Element c = new Element("IBUFGDS", id);
        if (i == null)
            throw new ExEx("IBUFGDS null I");
        if (ib == null)
            throw new ExEx("IBUFGDS null IB");
        c.addInput("I", i);
        c.addInput("IB", ib);
        c.addOutput("O", o);
        return(c);
    }

    /**
     * Construct an OBUF or OBUFT.
     * @param   o is the output net
     * @param   i is the input net
     * @param   t is an optional three-state enable net (high for high-Z)
     * @param   id is the element block identifier or null
     * @return  the element
     */
    public Element OBUF (Net o, Net i, Net t, String id) {
        Element c = new Element(t == null ? "OBUF" : "OBUFT", id);
        if (i == null)
            throw new ExEx("OBUF null i");
        c.addInput("I", i);
        if (t != null) {
            c.addInput("T", t);
            c.addTS("O", o);
        } else
            c.addOutput("O", o);
        return(c);
    }

    /**
     * Construct an OBUFDS or OBUFTDS.
     * @param   o is the +ve output net
     * @param   ob is the -ve output net
     * @param   i is the input net
     * @param   t is an optional three-state enable net (high for high-Z)
     * @param   id is the element block identifier or null
     * @return  the element
     */
    public Element OBUFDS (Net o, Net ob, Net i, Net t, String id) {
        Element c = new Element(t == null ? "OBUFDS" : "OBUFTDS", id);
        if (i == null)
            throw new ExEx("OBUF null i");
        c.addInput("I", i);
        if (t != null) {
            c.addInput("T", t);
            c.addTS("O", o);
            c.addTS("OB", ob);
        } else {
            c.addOutput("O", o);
            c.addOutput("OB", ob);
        }
        return(c);
    }

    /**
     * Construct a PULLDOWN.
     * @param   o is the output net
     * @param   dbl indicates a double pulldown (internal pulldowns only)
     */
    public void PULLDOWN (Net o, boolean dbl) {
        Element c = new Element("PULLDOWN");
        c.addTS("O", o);
        if (dbl)
            c.addProperty("DOUBLE", "TRUE");
    }

    /**
     * Construct a PULLUP.
     * @param   o is the output net
     * @param   dbl indicates a double pullup (internal pullups only)
     */
    public void PULLUP (Net o, boolean dbl) {
        Element c = new Element("PULLUP");
        c.addTS("O", o);
        if (dbl)
            c.addProperty("DOUBLE", "TRUE");
    }
    
    /**
     * Construct a selector.
     * This is not a basic element and must be converted to gates
     * later. It is used as a go-between from a TDESELECT to gate elements.
     * The TDESELECT cannot optimise duplicated data inputs as later TDECONNECT
     * elements may later join nets that are not initially recognised as connected.
     * Once the netlist has been built and is to be optimised these selector elements
     * can have duplicate inputs removed as all nets will have been fully resolved.
     * The select element can then be converted to basic gate form which may then be later
     * optimised to family-specific elements (e.g. OR gates into a carry chain).
     * @param   out is the output net
     * @param   sel is as array of select signals
     * @param   data is as array of data signals
     * @param   def is the default output if no selectors are true
     */
    public void selector (Net out, Net[] sel, Net[] data, int def) {
        Element c = new Element("selector", Gtype.SELECTOR);
        c.addOutput("O", out);
        for (int i=0 ; i<sel.length ; i++) {
            c.addInput("S" + i, sel[i]);
            c.addInput("D" + i, data[i]);
        }
        c.addProperty("DEFAULT", def==1 ? "1" : "0");
    }
    
    /**
     * A generic multiplier.
     * This is replaced by one or more hardware multipliers in FPGAs that have
     * multipliers or DSP blocks. In other cases this is replaced by a series
     * of CLB adders.
     * The replacement is performed in XNetlistOptimise.optimisegates().
     * @param a is the 1st input operand
     * @param b is the 2nd input operand
     * @param p is the product
     * @param asigned is true if the 1st operand is signed
     * @param bsigned is true if the 2nd operand is signed
     */
    public void MULT (
            Net[]   a,
            Net[]   b,
            Net[]   p,
            boolean asigned,
            boolean bsigned
        ) {
            Element e = new Element("MULT", Gtype.MULT);

            e.addInputArray("A", a, arrayformat);
            e.addInputArray("B", b, arrayformat);
            e.addOutputArray("O", p, arrayformat);
            if (asigned)
                e.addProperty("ASIGNED", "TRUE");
            if (bsigned)
                e.addProperty("BSIGNED", "TRUE");
        }
    
    /**
     * Construct a hardware multiplier.
     * This method is overwritten in the FPGA family class.
     * @param   a is the 1st operand (multiplicand) net
     * @param   b is the 2nd operand (multiplier) net
     * @param   p is the product net
     */
    public void Mult (Net[] a, Net[] b, Net[] p) {
        throw new ExEx("element Mult not overridden");
    }

    /**
     * Construct a MULT18X18 signed 18 bit multiplier.
     * This method is overwritten in the FPGA family class.
     * @param   a is the 1st operand (multiplicand) net
     * @param   b is the 2nd operand (multiplier) net
     * @param   p is the product net
     */
    //public void MULT18X18 (Net[] a, Net[] b, Net[] p) {
    //    throw new ExEx("element MULT18X18 not available in this FPGA");
    //}

    /**
     * Construct a MULT18X18S signed 18 bit multiplier with register output.
     * This method is overwritten in the FPGA family class.
     * @param   a is the 1st operand (multiplicand) net
     * @param   b is the 2nd operand (multiplier) net
     * @param   p is the product net
     * @param   c is the clock net
     * @param   ce is the clock enable net
     * @param   r is the reset net
     */
    //public void MULT18X18S (Net[] a, Net[] b, Net[] p, Net c, Net ce, Net r) {
    //    throw new ExEx("element MULT18X18S not available in this FPGA");
    //}

    /**
     * Construct a MULT25X18 signed 25 by 18 bit multiplier.
     * This method is overwritten in the FPGA family class.
     * In the XC5V this element is later converted to a DSP48E.
     * @param   a is the 1st operand (multiplicand) net
     * @param   b is the 2nd operand (multiplier) net
     * @param   p is the product net
     */
    //public void MULT25X18 (Net[] a, Net[] b, Net[] p) {
    //    throw new ExEx("element MULT25X18 not available in this FPGA");
    //}

    /**
     * Construct a MULT25X18S signed 25 by 18 bit multiplier with
     * registered output.
     * This method is overwritten in the FPGA family class.
     * In the XC5V this element is later converted to a DSP48E.
     * @param   a is the 1st operand (multiplicand) net
     * @param   b is the 2nd operand (multiplier) net
     * @param   p is the product net
     * @param   c is the clock net
     * @param   ce is the clock enable net
     * @param   r is the reset net
     */
    //public void MULT25X18S (Net[] a, Net[] b, Net[] p, Net c, Net ce, Net r) {
    //    throw new ExEx("element MULT25X18S not available in this FPGA");
    //}
    
    /*
     * Construct a register using a MULT18X18S.
     * @param   in is the input net
     * @param   out is the output net
     * @param   c is the clock net
     * @param   ce is the clock enable net
     * @param   r is the reset net
     *
    public void MULREG (Net[] in, Net[] out, Net c, Net ce, Net r) {
        throw new ExEx("element MULREG not available in this FPGA");
    }*/

    /**
     * Construct a RAM16X1S, RAM32X1S, RAM64X1S etc.
     * @param   wclk is the write clock net
     * @param   we is the write enable net
     * @param   a is the address input net array of size 4 to 7 bits
     * @param   id is the data input net
     * @param   od is the data output net
     * @param   init is the initialisation data as a hex string of length 4, 8, 16
     *          or 32 digits to match the RAM depth
     */
    public void RAM_1 (
        Net     wclk,
        Net     we,
        Net[]   a,
        Net     id,
        Net     od,
        String  init
    ) {
        int     arrayformat = 1;
        int     depth = 1 << a.length;
        Element e = new Element("RAM" + depth + "X1S");

        e.addInputArray("A", a, arrayformat);
        e.addInput("D", id);
        e.addInput("WCLK", wclk);
        e.addInput("WE", we != null ? we : Net.LO);
        e.addOutput("O", od);
        if (init != null)
            e.addProperty(" INIT", init);
    }
    
    /**
     * Construct a ROM16X1, ROM32X1, ROM64X1 etc.
     * @param   a is the address input net array of size 4 to 8 bits
     * @param   od is the data output net
     * @param   init is the initialisation data as a hex string of length 4, 8, 16,
     *          32 or 64 digits to match the RAM depth
     */
    public void ROM_1 (
        Net[]   a,
        Net     od,
        String  init
    ) {
        int     depth = 1 << a.length;
        int     arrayformat = 1;
        Element e = new Element("ROM" + depth + "X1");
        e.addInputArray("A", a, arrayformat);
        e.addOutput("O", od);
        if (init != null)
            e.addProperty(" INIT", init);
    }
    
    /**
     * Construct a RAM16X1D, RAM32X1D, RAM64X1D etc.
     * @param   wclk is the write clock net
     * @param   we is the write enable net
     * @param   a is the read-write port address input net array of
     *          size 4 to 7 bits
     * @param   di is the data input net
     * @param   spo is the read/write data output net
     * @param   dpra is the read-only port address input net array of
     *          size 4 to 7 bits
     * @param   dpo is the read-only data output net
     * @param   init is the initialisation data as a hex string of length 4, 8, 16
     *          or 32 digits to match the RAM depth
     */
    public void RAM_2 (
        Net     wclk,
        Net     we,
        Net[]   a,
        Net     di,
        Net     spo,
        Net[]   dpra,
        Net     dpo,
        String  init
    ) {
        int     arrayformat = 1;
        int     depth = 1 << a.length;
        Gtype   gtype = (init == null) ? Gtype.CLB_RAM : Gtype.NONE;
        
        Element e = new Element("RAM" + depth + "X1D", gtype);

        e.addInput("D", di != null ? di : Net.LO);
        e.addInputArray("A", a, arrayformat);
        e.addInput("WCLK", wclk != null ? wclk : Net.LO);
        e.addInput("WE", we != null ? we : Net.LO);
        e.addOutput("SPO", spo);
        e.addInputArray("DPRA", dpra, arrayformat);
        e.addOutput("DPO", dpo);
        if (init != null)
            e.addProperty(" INIT", init);
    }
    
    /**
     * Partition block RAM memory into available elements and instantiate them.
     * Multiple elements are used where the data width exceeds
     * that available from one element. The depth is constrained
     * by the maximum depth of an element.
     * address and data width determine RAMB type.
     *
     * This method is overwritten in most FPGA family classes
     *
     * @param   awidth      address width requested
     * @param   dwidth      data width
     * @param   wea         port A write enable
     * @param   ena         port A enable
     * @param   rsta        port A reset
     * @param   clka        port A clock
     * @param   addra       port A address
     * @param   dina        port A data input
     * @param   douta       port A data output
     * @param   web         port B write enable
     * @param   enb         port B enable
     * @param   rstb        port B reset
     * @param   clkb        port B clock
     * @param   addrb       port B address
     * @param   dinb        port B data input
     * @param   doutb       port B data output
     * @param   init        initialisation array (binary strings) for requested address width.
     * @param   init0       initial binary string value of output register port 0 (rmemory only)
     * @param   init1       initial binary string value of output register port 1 (rmemory only)
     * @param   properties  properties (attributes such as writemode)
     */
    public void ramallocate (
        int                 awidth, 
        int                 dwidth, 
        Net                 wea,
        Net                 ena,
        Net                 rsta,
        Net                 clka,
        Net[]               addra,
        Net[]               dina,
        Net[]               douta,
        Net                 web,
        Net                 enb,
        Net                 rstb,
        Net                 clkb,
        Net[]               addrb,
        Net[]               dinb,
        Net[]               doutb,
        String[]            init,
        String              init0,
        String              init1,
        ArrayList<String>   properties
    ) {
        throw new ExEx("SYSTEM ERROR - Block RAM not available in this FPGA");
    }
    
    /**
     * Partition a block RAM FIFO into available elements.
     * Multiple elements are used where the data width exceeds
     * that available from one element. The depth is constrained
     * by the maximum depth of an element.
     * address and data width determine FIFO type (18, 18_36, 36 or 36_72)
     *
     * @param   depth   depth requested
     * @param   dwidth  data width
     * @param   push    push input signal
     * @param   pop     pop input signal
     * @param   in      data input signal array
     * @param   out     data output signal  signal array
     * @param   empty   empty output signal
     * @param   full    full output signal
     * @param   rdcount port B enable
     * @param   wrcount port B reset
     * @param   reset   reset input signal
     * @param   wclk    write clock
     * @param   rclk    read clock
     */
    public void fifoallocate (
        int     depth, 
        int     dwidth, 
        Net     push,
        Net     pop,
        Net[]   in,
        Net[]   out,
        Net     empty,
        Net     full,
        Net[]   rdcount,
        Net[]   wrcount,
        Net     reset,
        Net     wclk,
        Net     rclk
    ) {
        throw new ExEx("SYSTEM ERROR - Block RAM FIFO not available in this FPGA");
    }
    
    /**
     * Convert a register to a DSP or multiplier whose output register
     * implements the register. Some subclasses override this method,
     * but by default it does nothing.
     * @param   tde is the REG TDE
     */
    public void regToDSP (TDE tde) {}
    
    /**
     * Subsume a register into a DSP or multiplier block. Some subclasses override this method,
     * but by default it does nothing.
     * @param tde is the DSP or multiplier TDE
     * @param clk is the clock
     * @param ce is the clock enable
     * @param rst is the reset
     * @param out is the output
     */
    public void regIntoDSP (TDE tde, TDEVar clk, TDEVar ce, TDEVar rst, TDEVar out) {}
    
    /**
     * Create a hardware multiplier. Some subclasses override this method.
     * @param   a is the A input
     * @param   b is the B input
     * @param   p is the output
     */
    public void Mult (TDEVar a, TDEVar b, TDEVar p) {
        throw new ExEx("Hardware multiplier element not available in this FPGA");
    }
    
    /**
     * Create a hardware ALU using a DSP block. Some subclasses override this method.
     * @param   use_mult selects the multiplier
     * @param   out is the output
     * @param   alu_inA is the DSP A input
     * @param   alu_inB is the DSP A input
     * @param   alu_inC is the DSP A input
     * @param   opmode is the DSP operating mode
     * @param   alumode is the DSP ALU mode
     */
    public void dsp48_alu (
            boolean use_mult,
            TDEVar  out,
            TDEVar  alu_inA,
            TDEVar  alu_inB,
            TDEVar  alu_inC,
            TDEVar  opmode,
            TDEVar  alumode
        ) {
        throw new ExEx("DSP element not available in this FPGA");
    }

}
