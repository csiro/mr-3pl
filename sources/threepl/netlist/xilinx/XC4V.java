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

/**
 * <p>This class implements Xilinx library elements particular to
 * Virtex 4 FPGAs. These are low level elements
 * from which the output EDIF netlist  is generated. All higher
 * level functions call these, directly or indirectly.</p>
 */
public class XC4V extends XTDECode implements NetConstants {   
    static final int[]  ramc_data_widths =  {0, 1, 1, 1, 1, 1, 1, 1,
                                             1, 1, 0, 0, 0, 0, 0, 0,
                                             0, 0, 0, 0};

    public XC4V () {
        fname = "XC4V (Virtex4)";
        lutwidth = 4;
        srladdrwidth = 4;
        addsubinputwidth = 9999;    // no explicit limit
        dspregwidth = 48;           // 48 bit DSP register and input
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
        ramc_addrmax[0] = 6;    // for single port
        ramc_addrmax[1] = 4;    // for dual port
        
        big_RAMB   = "RAMB16";
        small_RAMB = "NONE";

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
        queue_buffer_max_cram_depth = 16;
        queue_buffer_min_rram_depth = 512;
        queue_buffer_max_rram_depth = 16384;
        
        big_FIFO = "FIFO16";
        small_FIFO = null;

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
        XTDECode.initialise(1, "XC4V");
    }
    
    /**
     * Change a REG TDE to a DSP48 TDE to implement the
     * register using the output register of a DSP block.
     * @param   tde is the register TDE
     */    
    public void regToDSP (TDE tde) {
        ArrayList<?>    params = tde.getParams();
        TDEVar          opmode  = new TDEVar(0x30L, new Type("uint:7", null), null);

        tde.changeType(TDEType.ELEMENT);
        params.clear();
        tde.add2p("DSP48");
        tde.add2p((String)null);
        
        tde.addPin2Element("CLK",           3,       null);  // REG clock
        tde.addPin2Element("C",             0,       null);  // REG D
        tde.addPin2Element("CEP",           0,       null);  // REG CE
        tde.addPin2Element("RSTP",          0,       null);  // REG R
        tde.addPin2Element("P",             1,       null, 48, arrayformat);  // REG OUT
        tde.addPin2Element("OPMODE",        0,     opmode,  7, arrayformat);
        tde.addPin2Element("CEA1",          0, TDEVar.GND); 
        tde.addPin2Element("CEA2",          0, TDEVar.GND); 
        tde.addPin2Element("CEB1",          0, TDEVar.GND); 
        tde.addPin2Element("CEB2",          0, TDEVar.GND); 
        tde.addPin2Element("CEC",           0, TDEVar.GND); 
        tde.addPin2Element("CEM",           0, TDEVar.GND); 
        tde.addPin2Element("CEALUMODE",     0, TDEVar.GND); 
        tde.addPin2Element("CECTRL",        0, TDEVar.GND); 
        tde.addPin2Element("CEMULTCARRYIN", 0, TDEVar.GND); 
        tde.addPin2Element("CECARRYIN",     0, TDEVar.GND); 
        
        tde.addProperty2Element("USE_MULT", "NONE");
        tde.addProperty2Element("PREG", "1");
        tde.addProperty2Element("AREG", "0");
        tde.addProperty2Element("ACASCREG", "0");
        tde.addProperty2Element("BREG", "0");
        tde.addProperty2Element("BCASCREG", "0");
        tde.addProperty2Element("CREG", "0");
        tde.addProperty2Element("MREG", "0");
        tde.addProperty2Element("OPMODEREG", "0");
        tde.addProperty2Element("ALUMODEREG", "0");
        tde.addProperty2Element("CARRYINREG", "0");
        tde.addProperty2Element("MULTCARRYINREG", "0");
        tde.addProperty2Element("CARRYINSELREG", "0");
        
        tdelist.addTDE(tde);
    }
      
    /**
     * Subsume a register into a DSP48 block.
     * @param tde is the DSP TDE
     * @param clk is the clock
     * @param ce is the clock enable
     * @param rst is the reset
     * @param out is the output
     */
    public void regIntoDSP (TDE tde, TDEVar clk, TDEVar ce, TDEVar rst, TDEVar out) {
        ArrayList<Object>   params = tde.getParams();

        if (!((String)params.get(0)).equals("DSP48"))
            throw new ExEx("CODE GENERATION ERROR - regIntoDSP - element not DSP48 ( is " + (String)params.get(0) + ")");
        
        tde.setElementPin ("CLK", 3, clk, 0, 0);
        tde.setElementPin ("CEP", 0,  ce, 0, 0);
        tde.setElementPin ("RSTP", 0, rst, 0, 0);
        tde.setElementPin (  "P", 1, out, 48, arrayformat);
        tde.changePropertyOfElement("PREG", "1");
    }
    
    /**
     * Create an 18X18 hardware multiplier TDE.
     * @param   a is the A input
     * @param   b is the B input
     * @param   p is the output
     */    
    public void Mult (TDEVar a, TDEVar b, TDEVar p) {
        TDE tde = new TDE(TDEType.ELEMENT);
        TDEVar  opmode = new TDEVar(0x05L, new Type("uint:7", null), null);
        TDEVar  carryinsel = new TDEVar(0L, new Type("uint:2", null), null);
        
        tde.add2p("DSP48");
        tde.add2p((String)null);
        
        tde.addPin2Element("CLK",        3, TDEVar.GND);
        tde.addPin2Element("A",          0,          a, 18, arrayformat);
        tde.addPin2Element("B",          0,          b, 18, arrayformat);
        tde.addPin2Element("P",          1,          p, 48, arrayformat);
        tde.addPin2Element("OPMODE",     0,     opmode,  7, arrayformat);
        tde.addPin2Element("CARRYINSEL", 0, carryinsel,  2, arrayformat);
        tde.addPin2Element("CARRYIN",    0, TDEVar.GND);        
        tde.addPin2Element("SUBTRACT",   0, TDEVar.GND);        
        tde.addPin2Element("CEA",        0, TDEVar.GND);
        tde.addPin2Element("CEB",        0, TDEVar.GND);
        tde.addPin2Element("CEC",        0, TDEVar.GND);
        tde.addPin2Element("CEM",        0, TDEVar.GND);
        tde.addPin2Element("CEP",        0, TDEVar.GND);
        tde.addPin2Element("CECTRL",     0, TDEVar.GND);
        tde.addPin2Element("CECINSUB",   0, TDEVar.GND);
        tde.addPin2Element("CECARRYIN",  0, TDEVar.GND);
        tde.addPin2Element("RSTA",       0, TDEVar.GND);
        tde.addPin2Element("RSTB",       0, TDEVar.GND);
        tde.addPin2Element("RSTC",       0, TDEVar.GND);
        tde.addPin2Element("RSTM",       0, TDEVar.GND);
        tde.addPin2Element("RSTP",       0, TDEVar.GND);
        tde.addPin2Element("RSTCTRL",    0, TDEVar.GND);
        tde.addPin2Element("RSTCARRYIN", 0, TDEVar.GND);
        
        tde.addProperty2Element("AREG", "0");
        tde.addProperty2Element("BREG", "0");
        tde.addProperty2Element("CREG", "0");
        tde.addProperty2Element("MREG", "0");
        tde.addProperty2Element("PREG", "0");
        tde.addProperty2Element("OPMODEREG", "0");
        tde.addProperty2Element("CARRYINREG", "0");
        tde.addProperty2Element("SUBTRACTREG", "0");
        tde.addProperty2Element("CARRYINSELREG", "0");
        tde.addProperty2Element("B_INPUT", "DIRECT");
        tde.addProperty2Element("LEGACY_MODE", "MULT18X18");
        
        tdelist.addTDE(tde);
    }
    
    /**
     * Create an 18X18 hardware multiplier.
     * @param   a is the A input
     * @param   b is the B input
     * @param   p is the output
     */    
    public void Mult (Net[] a, Net[] b, Net[] p) {
        int     arrayformat = 2;
        Element e = new Element("DSP48");
        Net[]   opmode = constant(5, 7);
        Net[]   carryinsel = constant(0, 2);

        e.addInputArray("A", a, arrayformat);
        e.addInputArray("B", b, arrayformat);
        e.addOutputArray("P", p, arrayformat);
                
        e.addInput("CLK", Net.LO);
        e.addInputArray("OPMODE", opmode, arrayformat);
        e.addInputArray("CARRYINSEL", carryinsel, arrayformat);
        e.addInput("CARRYIN",    Net.LO);        
        e.addInput("SUBTRACT",   Net.LO);        
        e.addInput("CEA",        Net.LO);
        e.addInput("CEB",        Net.LO);
        e.addInput("CEC",        Net.LO);
        e.addInput("CEM",        Net.LO);
        e.addInput("CEP",        Net.LO);
        e.addInput("CECTRL",     Net.LO);
        e.addInput("CECINSUB",   Net.LO);
        e.addInput("CECARRYIN",  Net.LO);
        e.addInput("RSTA",       Net.LO);
        e.addInput("RSTB",       Net.LO);
        e.addInput("RSTC",       Net.LO);
        e.addInput("RSTM",       Net.LO);
        e.addInput("RSTP",       Net.LO);
        e.addInput("RSTCTRL",    Net.LO);
        e.addInput("RSTCARRYIN", Net.LO);
        
        e.addProperty("AREG", "0");
        e.addProperty("BREG", "0");
        e.addProperty("CREG", "0");
        e.addProperty("MREG", "0");
        e.addProperty("PREG", "0");
        e.addProperty("OPMODEREG", "0");
        e.addProperty("CARRYINREG", "0");
        e.addProperty("SUBTRACTREG", "0");
        e.addProperty("CARRYINSELREG", "0");
        e.addProperty("B_INPUT", "DIRECT");
        e.addProperty("LEGACY_MODE", "MULT18X18");
        
    }
}
