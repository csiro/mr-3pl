package threepl.netlist.xilinx;

import static threepl.ThreePL.tdelist;

import java.util.ArrayList;

import threepl.codegen.TDE;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Type;
import threepl.netlist.Element;
import threepl.netlist.Net;
import threepl.netlist.NetConstants;
import threepl.parser.SrcLoc;

/**
 * <p>This class implements Xilinx library elements particular to
 * VirtexII FPGAs. These are low level elements
 * from which the output EDIF netlist  is generated. All higher
 * level functions call these, directly or indirectly.</p>
 */
public class XC2V extends XTDECode implements NetConstants {   
    static final int[]  ramc_data_widths =  {0, 1, 1, 1, 1, 1, 1, 1,
                                             1, 1, 0, 0, 0, 0, 0, 0,
                                             0, 0, 0, 0};

    public XC2V () {
        fname = "XC2V (Virtex2)";
        lutwidth = 4;
        srladdrwidth = 4;
        addsubinputwidth = 9999;    // no explicit limit
        dspregwidth = 36;           // 36 bit register
        multinputwidth1 = 35;       // 35 bit signed, 34 bit unsigned
        multinputwidth2 = 35;       // 35 bit signed, 34 bit unsigned
        divinputwidth = 9999;       // no explicit limit  

        ramc_dwidths = ramc_data_widths;
    
        ramc_ports = 2;
        ramb_ports = 2;

        romc_addrmin = new int[ramc_ports];
        romc_addrmin[0] = 4;    // for single port
        romc_addrmin[1] = 0;    // dual port not available
        romc_addrmax = new int[ramc_ports];
        romc_addrmax[0] = 8;    // for single port
        romc_addrmax[1] = 0;    // dual port not available

        ramc_addrmin = new int[ramc_ports];
        ramc_addrmin[0] = 4;    // for single port
        ramc_addrmin[1] = 4;    // for dual port
        ramc_addrmax = new int[ramc_ports];
        ramc_addrmax[0] = 7;    // for single port
        ramc_addrmax[1] = 6;    // for dual port

        big_RAMB   = "RAMB16";
        small_RAMB = "RAMB4";

        ramb_addrmin = new int[ramb_ports];
        ramb_addrmin[0] = 9;  
        ramb_addrmin[1] = 9;  
        ramb_addrmax = new int[ramb_ports];
        ramb_addrmax[0] = 14; 
        ramb_addrmax[1] = 14; 

        ramc_readable = new boolean[ramc_ports];
        ramc_readable[0] = true;    // can read port 0
        ramc_readable[1] = true;    // can read port 1
        ramc_writable = new boolean[ramc_ports];
        ramc_writable[0] = true;    // can write port 0
        ramc_writable[1] = false;   // cannot write port 1


        ramb_readable = new boolean[ramb_ports];
        ramb_writable = new boolean[ramb_ports];
        ramb_readable[0] = true;
        ramb_readable[1] = true;
        ramb_writable[0] = true;
        ramb_writable[1] = true;

        ramc_mux_addr_bits_maxc = 3; // extra address bits allowed using MUXes

        queue_buffer_min_cram_depth = 16;
        queue_buffer_max_cram_depth = 64;
        queue_buffer_min_rram_depth = 512;
        queue_buffer_max_rram_depth = 16384;

        defaultOutputDirect = false;
        defaultContinuous = false;
        deviceHasTBUF = true;
        deviceAllowsQueue = true;
        deviceAllowsCMemory = true;
        deviceAllowsRMemory = true;
        
        deviceFDRSEpatch = false;
        deviceCascadeGates = false;
        deviceCPLDarith = false;
        deviceOBUFFD = true;
        deviceOBUFFTFF = true;

        // Initialise element ports for ANDn, ORn, XORn, XNORn and LUTn
        // for n 1 to 4.
        XTDECode.initialise(1, "XC2V");
    }
    
    /**
     * Construct an ORCY.
     * @param   i is the general input net
     * @param   ci is the carry input net
     * @return  the output net
     */
    public Net ORCY (Net i, Net ci) {
        Element c = new Element("ORCY", Gtype.ORCY);
        Net     o = new Net();
        if (i == null)
            throw new ExEx("ORCY null i");
        if (ci == null)
            throw new ExEx("ORCY null ci");
        c.addInput("I", i);
        c.addInput("CI", ci);
        c.addOutput("O", o);
        return(o);
    }
    
    /**
     * Change a REG TDE to a MULT18X18S TDE to implement the
     * register using the output register of a multiplier.
     * @param   tde is the register TDE
     * @param   loc is the source file location
     */    
    public void regToDSP (TDE tde, SrcLoc loc) {
        int     arrayformat = 2;
        TDEVar  one = new TDEVar(1L, new Type("uint:18", loc), loc);

        tde.changeType(TDEType.ELEMENT);
        ArrayList<?>    params = tde.getParams();
        params.clear();
        tde.add2p("MULT18X18S");
        tde.add2p((String)null);
        
        tde.addPin2Element("C",  3, null);                  // REG clock
        tde.addPin2Element("A",  0, null, 18, arrayformat); // REG D
        tde.addPin2Element("CE", 0, null);                  // REG CE
        tde.addPin2Element("R",  0, null);                  // REG R
        tde.addPin2Element("P",  1, null, 36, arrayformat); // REG OUT
        tde.addPin2Element("B",  0,  one, 18, arrayformat);
    }
    
    /**
     * Subsume a register into a multiplier block.
     * @param tde is the multiplier TDE
     * @param clk is the clock
     * @param ce is the clock enable
     * @param rst is the reset
     * @param out is the output
     */
    public void regIntoDSP (TDE tde, TDEVar clk, TDEVar ce, TDEVar rst, TDEVar out) {
        int                 arrayformat = 2;
        ArrayList<Object>   params = tde.getParams();

        if (!((String)params.get(0)).equals("MUL18X18"))
            throw new ExEx("CODE GENERATION ERROR - regIntoDSP - element not MULT18X18 ( is " + (String)params.get(0) + ")");
        
        params.set(0, "MULT18X18S");    // change to a registered multiplier
        tde.setElementPin ( "C", 3, clk);
        tde.setElementPin ("CE", 0,  ce);
        tde.setElementPin ( "R", 0, rst);
        tde.setElementPin ( "P", 1, out, 36, arrayformat);
    
    }
    
    /**
     * Create an 18X18 hardware multiplier TDE.
     */    
    public void Mult (TDEVar a, TDEVar b, TDEVar p) {
        int arrayformat = 2;
        TDE tde = new TDE(TDEType.ELEMENT);       

        tde.add2p("MULT18X18");
        tde.add2p((String)null);
        
        tde.addPin2Element("A",  0,  a, 18, arrayformat);
        tde.addPin2Element("B",  0,  b, 18, arrayformat);
        tde.addPin2Element("P",  1,  p, 36, arrayformat);
        tdelist.addTDE(tde);
    }
    
    /**
     * Create an 18X18 hardware multiplier Element.
     */    
    public void Mult (Net[] a, Net[] b, Net[] p) {
        int     arrayformat = 2;
        Element e = new Element("MULT18X18", Gtype.MULT18X18);
        e.addInputArray("A", a, arrayformat);
        e.addInputArray("B", b, arrayformat);
        e.addOutputArray("P", p, arrayformat);
    }
    
}
   
