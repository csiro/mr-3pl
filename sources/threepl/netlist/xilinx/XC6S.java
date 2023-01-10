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
 * Spartan 6 FPGAs. These are low level elements
 * from which the output EDIF netlist  is generated. All higher
 * level functions call these, directly or indirectly.</p>
 */
public class XC6S extends XTDECode implements NetConstants {   
    static final int[]  ramc_data_widths =  {0, 1, 1, 1, 1, 1, 1, 1,
                                             1, 1, 1, 1, 1, 0, 0, 0,
                                             0, 0, 0, 0};


    public XC6S () {
        fname = "XC6S (Spartan6)";
        lutwidth = 6;
        srladdrwidth = 5;
        addsubinputwidth = 9999;    // no explicit limit
        dspregwidth = 48;           // 48 bit DSP register and input
        multinputwidth1 = 35;       // 35 bit signed, 34 bit unsigned
        multinputwidth2 = 35;       // 35 bit signed, 34 bit unsigned
        divinputwidth = 9999;       // no explicit limit  

        ramc_dwidths = ramc_data_widths;
    
        ramc_ports = 2;
        ramb_ports = 2;

        romc_addrmin = new int[ramc_ports];
        romc_addrmin[0] = 5;    // for single port
        romc_addrmin[1] = 0;    // dual port not available
        romc_addrmax = new int[ramc_ports];
        romc_addrmax[0] = 8;    // for single port
        romc_addrmax[1] = 0;    // dual port not available

        ramc_addrmin = new int[ramc_ports];
        ramc_addrmin[0] = 5;    // for single port
        ramc_addrmin[1] = 5;    // for dual port
        ramc_addrmax = new int[ramc_ports];
        ramc_addrmax[0] = 8;    // for single port
        ramc_addrmax[1] = 7;    // for dual port
        
        big_RAMB   = "RAMB16BWER";
        small_RAMB = "NONE";

        ramb_addrmin = new int[ramb_ports];
        ramb_addrmin[0] = 10;  
        ramb_addrmin[1] = 10;  
        ramb_addrmax = new int[ramb_ports];
        ramb_addrmax[0] = 15; 
        ramb_addrmax[1] = 15; 

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

        ramc_mux_addr_bits_maxc = 4; // extra address bits allowed using MUXes

        queue_buffer_min_cram_depth = 16;
        queue_buffer_max_cram_depth = 128;
        queue_buffer_min_rram_depth = 512;
        queue_buffer_max_rram_depth = 32768;

        defaultOutputDirect = false;
        defaultContinuous = false;
        deviceHasTBUF = false;
        deviceAllowsQueue = true;
        deviceAllowsCMemory = true;
        deviceAllowsRMemory = true;
        
        deviceFDRSEpatch = false;
        deviceCascadeGates = false;
        deviceCPLDarith = false;
        deviceOBUFFD = true;
        deviceOBUFFTFF = true;

        // Initialise element ports for ANDn, ORn, XORn, XNORn and LUTn
        // for n 1 to 6.
        XTDECode.initialise(1, "XC6S");
    }
    
    /**
     * Change a REG TDE to a DSP48A1 TDE to implement the
     * register using the output register of a DSP block.
     * @param   tde is the register TDE
     */    
    public void regToDSP (TDE tde) {
        TDEVar  opmode  = new TDEVar(0x30L, new Type("uint:8", null), null);

        tde.changeType(TDEType.ELEMENT);
        ArrayList<?>    params = tde.getParams();
        params.clear();
        tde.add2p("DSP48A1");
        tde.add2p((String)null);
        
        tde.addPin2Element("CLK",           3,       null);  // REG clock
        tde.addPin2Element("C",             0,       null);  // REG D
        tde.addPin2Element("CEP",           0,       null);  // REG CE
        tde.addPin2Element("RSTP",          0,       null);  // REG R
        tde.addPin2Element("P",             1,       null, 48, arrayformat);  // REG OUT
        tde.addPin2Element("OPMODE",        0,     opmode,  8, arrayformat);
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
     * Subsume a register into a DSP48A1 block.
     * @param tde is the DSP TDE
     * @param clk is the clock
     * @param ce is the clock enable
     * @param rst is the reset
     * @param out is the output
     */
    public void regIntoDSP (TDE tde, TDEVar clk, TDEVar ce, TDEVar rst, TDEVar out) {
        ArrayList<Object>   params = tde.getParams();
        if (!((String)params.get(0)).equals("DSP48A1"))
            throw new ExEx("CODE GENERATION ERROR - regIntoDSP - element not DSP48A1 ( is " + (String)params.get(0) + ")");
        
        tde.setElementPin ("CLK", 3, clk);
        tde.setElementPin ( "CE", 0,  ce);
        tde.setElementPin ("RST", 0, rst);
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
        TDE     tde = new TDE(TDEType.ELEMENT);
        TDEVar  opmode  = new TDEVar(0x05L, new Type("uint:8", null), null);

        tde.add2p("DSP48A1");
        tde.add2p((String)null);
        
        tde.addPin2Element("CLK",           3, TDEVar.GND);
        tde.addPin2Element("A",             0,          a, 18, arrayformat);
        tde.addPin2Element("B",             0,          b, 18, arrayformat);
        tde.addPin2Element("P",             1,          p, 48, arrayformat);
        tde.addPin2Element("OPMODE",        0,     opmode,  8, arrayformat);
        tde.addPin2Element("CEP",           0, TDEVar.GND, 0, 0);
        tde.addPin2Element("CEA1",          0, TDEVar.GND, 0, 0);
        tde.addPin2Element("CEA2",          0, TDEVar.GND, 0, 0);
        tde.addPin2Element("CEB1",          0, TDEVar.GND, 0, 0);
        tde.addPin2Element("CEB2",          0, TDEVar.GND, 0, 0);
        tde.addPin2Element("CEC",           0, TDEVar.GND, 0, 0);
        tde.addPin2Element("CEM",           0, TDEVar.GND, 0, 0);
        tde.addPin2Element("CEALUMODE",     0, TDEVar.GND, 0, 0);
        tde.addPin2Element("CECTRL",        0, TDEVar.GND, 0, 0);
        tde.addPin2Element("CEMULTCARRYIN", 0, TDEVar.GND, 0, 0);
        tde.addPin2Element("CECARRYIN",     0, TDEVar.GND, 0, 0);
        
        tde.addProperty2Element("USE_MULT", "MULT");
        tde.addProperty2Element("PREG", "0");
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
     * Create a hardware ALU using a DSP.
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
        String  mult = use_mult ? "MULT" : "NONE";
        TDE     tde = new TDE(TDEType.ELEMENT);

        tde.add2p("DSP48A1");
        tde.add2p((String)null);
        
        tde.addPin2Element("CLK",           3, TDEVar.GND);
        tde.addPin2Element("A",             0,    alu_inA, 25, arrayformat);
        tde.addPin2Element("B",             0,    alu_inB,18,  arrayformat);
        tde.addPin2Element("C",             0,    alu_inC, 48, arrayformat);
        tde.addPin2Element("P",             1,        out, 48, arrayformat);
        tde.addPin2Element("OPMODE",        0,     opmode,  8, arrayformat);
        tde.addPin2Element("CEP",           0, TDEVar.GND);
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
        tdelist.addTDE(tde);
        
        tde.addProperty2Element("USE_MULT", mult);
        tde.addProperty2Element("PREG", "0");
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
    }
    
    /**
     * Create an 18X18 hardware multiplier.
     * @param   a is the A input
     * @param   b is the B input
     * @param   p is the output
     */    
    public void Mult (Net[] a, Net[] b, Net[] p) {
        int     arrayformat = 2;
        Element e = new Element("DSP48A1");
        Net[]   opmode = constant(5, 7);
        Net[]   carryinsel = constant(0, 2);
        
        e.addInputArray("A", a, arrayformat);
        e.addInputArray("B", b, arrayformat);
        e.addOutputArray("P", p, arrayformat);
                
        e.addInput("CLK", Net.LO);
        e.addInputArray("OPMODE", opmode, arrayformat);
        e.addInputArray("CARRYINSEL", carryinsel, arrayformat);
        e.addInput("CEP",           Net.LO);
        e.addInput("CEA1",          Net.LO);
        e.addInput("CEA2",          Net.LO);
        e.addInput("CEB1",          Net.LO);
        e.addInput("CEB2",          Net.LO);
        e.addInput("CEC",           Net.LO);
        e.addInput("CEM",           Net.LO);
        e.addInput("CEALUMODE",     Net.LO);
        e.addInput("CECTRL",        Net.LO);
        e.addInput("CEMULTCARRYIN", Net.LO);
        e.addInput("CECARRYIN",     Net.LO);       
        e.addInput("CARRYIN",       Net.LO);     
        e.addInput("SUBTRACT",      Net.LO);     
        e.addInput("CECINSUB",      Net.LO); 
        e.addInput("RSTA",          Net.LO); 
        e.addInput("RSTB",          Net.LO); 
        e.addInput("RSTC",          Net.LO); 
        e.addInput("RSTM",          Net.LO); 
        e.addInput("RSTP",          Net.LO); 
        e.addInput("RSTCTRL",       Net.LO); 
        e.addInput("RSTCARRYIN",    Net.LO); 
        
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

    /**
     * Construct a simple ALU using a DSP48.
     * COMMENTED OUT.
     * NOT SURE WHY THIS IS HERE - IT IS NEVER CALLED, THOUGH IT LOOKS USEFUL!
     * @param   a is the 1st operand
     * @param   b is the 2nd operand
     * @param   p is the output
     * @param   add is true to add, false to subtract
     * @param   iga is is high for add/subtract, low for output = b
     *
    public void ALU (
        Net[]   a,
        Net[]   b,
        Net[]   p,
        Net     add,
        Net     iga
    ) {
        Net[]   opmode = new Net[8];
        Net     sub = INV(add);
        
        opmode[0] = Net.HI;
        opmode[1] = Net.HI;
        opmode[2] = iga;
        opmode[3] = iga;
        opmode[4] = Net.LO;
        opmode[5] = Net.LO;
        opmode[6] = Net.LO;
        opmode[7] = AND(sub, iga);
        
        Element e = new Element("DSP48A1", Gtype.DSP48);
        
        e.addInputArray("A", subarray(b, 0, 17), 18, Net.LO, arrayformat);
        e.addInputArray("B", subarray(b, 18, 35), 18, Net.LO, arrayformat);
        e.addInputArray("C", a, 48, Net.LO, arrayformat);
        e.addInputArray("D", subarray(b, 36, 47), 12, Net.LO, arrayformat);
        e.addInputArray("OPMODE", opmode, arrayformat);
        e.addInput("CIN", Net.LO);
        e.addInput("CEP", Net.LO);
        e.addInput("CEA", Net.LO);
        e.addInput("CEB", Net.LO);
        e.addInput("CEC", Net.LO);
        e.addInput("CED", Net.LO);
        e.addInput("CEM", Net.LO);
        e.addInput("CECY", Net.LO);
        e.addInput("CEO", Net.LO);
        e.addInput("RSTA", Net.LO);
        e.addInput("RSTB", Net.LO);
        e.addInput("RSTC", Net.LO);
        e.addInput("RSTD", Net.LO);
        e.addInput("RSTM", Net.LO);
        e.addInput("RSTP", Net.LO);
        e.addInput("RSTCY", Net.LO);
        e.addInput("RSTO", Net.LO);
    
        e.addOutputArray("P", p, arrayformat);   
        
        e.addProperty("A0REG", "0");
        e.addProperty("A1REG", "0");
        e.addProperty("B0REG", "0");
        e.addProperty("B1REG", "0");
        e.addProperty("CREG", "0");
        e.addProperty("DREG", "0");
        e.addProperty("MREG", "0");
        e.addProperty("PREG", "0");
        e.addProperty("OPMODEREG", "0");
        e.addProperty("CARRYINREG", "0");       
        e.addProperty("CARRYOUTREG", "0");       
        e.addProperty("CARRYINSEL", "CARRYIN");
        e.addProperty("B_INPUT", "DIRECT");
        e.addProperty("RSTTYPE", "SYNC");
    }*/

   
    /*    DSP48A1 prototype
    Element e = new Element("DSP48A1", Gtype.DSP48);
    e.addInputArray("A", a, 18, Net.LO);
    e.addInputArray("B", b, 18, Net.LO);
    e.addInputArray("C", constant(0, 48));
    e.addInputArray("D", constant(0, 18));
    e.addInput("CLK", );
    e.addInput("CIN", Net.LO);
    e.addInputArray("OPMODE", constant(0x0c, 8));
    e.addInput("PCIN", );
    e.addInput("CEP", Net.LO);
    e.addInput("CEA", Net.LO);
    e.addInput("CEB", Net.LO);
    e.addInput("CEC", Net.LO);
    e.addInput("CED", Net.LO);
    e.addInput("CEM", Net.LO);
    e.addInput("CECY", Net.LO);
    e.addInput("CEO", Net.LO);
    e.addInput("RSTA", Net.LO);
    e.addInput("RSTB", Net.LO);
    e.addInput("RSTC", Net.LO);
    e.addInput("RSTD", Net.LO);
    e.addInput("RSTM", Net.LO);
    e.addInput("RSTP", Net.LO);
    e.addInput("RSTCY", Net.LO);
    e.addInput("RSTO", Net.LO);

    e.addOutputArray("P", p);
    e.addOutputArray("BCOUT", );
    e.addOutputArray("PCOUT", );
    e.addOutputArray("MFOUT", );
    e.addOutputArray("CCOUT", );
    e.addOutputArray("CFOUT", );

    e.addConstraint("N", "A0REG", "0");
    e.addConstraint("N", "A1REG", "0");
    e.addConstraint("N", "B0REG", "0");
    e.addConstraint("N", "B1REG", "0");
    e.addConstraint("N", "CREG", "0");
    e.addConstraint("N", "DREG", "0");
    e.addConstraint("N", "MREG", "0");
    e.addConstraint("N", "PREG", "0");
    e.addConstraint("N", "OPMODEREG", "0");
    e.addConstraint("N", "CARRYINREG", "0");       
    e.addConstraint("N", "CARRYOUTREG", "0");       
    e.addConstraint("N", "CARRYINSEL", "CARRYIN");
    e.addConstraint("N", "B_INPUT", "DIRECT");
    e.addConstraint("N", "RSTTYPE", "SYNC");
    */
    
}
