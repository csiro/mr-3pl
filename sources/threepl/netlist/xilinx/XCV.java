package threepl.netlist.xilinx;

/**
 * <p>This class implements Xilinx library elements particular to
 * the Virtex and Virtex-E FPGAs. These are low level elements
 * from which the output EDIF netlist  is generated. All higher
 * level functions call these, directly or indirectly.</p>
 */
public class XCV extends XTDECode {   
    static final int[]  ramc_data_widths =  {0, 1, 1, 1, 1, 1, 1, 1,
                                             1, 1, 0, 0, 0, 0, 0, 0,
                                             0, 0, 0, 0};

    public XCV () {
        fname = "XCV (Virtex)";
        lutwidth = 4;
        srladdrwidth = 4;
        addsubinputwidth = 9999;    // no explicit limit
        dspregwidth = 0;            // no multiplier/DSP
        multinputwidth1 = 0;        // no multiplier/DSP
        multinputwidth2 = 0;        // no multiplier/DSP
        divinputwidth = 9999;       // no explicit limit

        ramc_dwidths = ramc_data_widths;
    
        ramc_ports = 2;
        ramb_ports = 2;

        romc_addrmin = new int[ramc_ports];
        romc_addrmin[0] = 4;    // for single port
        romc_addrmin[1] = 0;    // dual port not available
        romc_addrmax = new int[ramc_ports];
        romc_addrmax[0] = 5;    // for single port
        romc_addrmax[1] = 0;    // dual port not available

        ramc_addrmin = new int[ramc_ports];
        ramc_addrmin[0] = 4;    // for single port
        ramc_addrmin[1] = 4;    // for dual port
        ramc_addrmax = new int[ramc_ports];
        ramc_addrmax[0] = 5;    // for single port
        ramc_addrmax[1] = 5;    // for dual port
        
        big_RAMB   = "RAMB4";
        small_RAMB = "NONE";

        ramb_addrmin = new int[ramb_ports];
        ramb_addrmin[0] = 8;  
        ramb_addrmin[1] = 8;  
        ramb_addrmax = new int[ramb_ports];
        ramb_addrmax[0] = 12; 
        ramb_addrmax[1] = 12; 

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
        queue_buffer_max_cram_depth = 32;
        queue_buffer_min_rram_depth = 256;
        queue_buffer_max_rram_depth = 4096;

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
        
        // Initialise element ports for ANDn, ORn, XORn, XNORn and LUTn
        // for n 1 to 4.
        XTDECode.initialise(1, "XCV");
    }
}
