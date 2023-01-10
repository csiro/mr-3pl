package threepl.netlist.xilinx;


/**
 * This class implements common Xilinx library elements.
 * For that reason the class is empty as the common
 * elements are all implemented in superclasses of this
 * class.
 */
public class UNISIMS extends XTDECode {   
    static final int[]  ramc_data_widths =  {0, 1, 1, 1, 1, 1, 1, 1,
                                             1, 1, 0, 0, 0, 0, 0, 0,
                                             0, 0, 0, 0};

    public UNISIMS () {
        fname = "UNISIMS (default)";
        lutwidth = 4;
        addsubinputwidth = 9999;    // no explicit limit
        multinputwidth1 = 0;        // no hardware
        multinputwidth2 = 0;        // no hardware
        divinputwidth = 9999;       // no explicit limit  

        ramc_dwidths = ramc_data_widths;
    
        ramc_ports = 2;
        ramb_ports = 0;

        romc_addrmin = new int[ramc_ports];
        romc_addrmin[0] = 4;  
        romc_addrmin[1] = 0; // not available
        romc_addrmax = new int[ramc_ports];
        romc_addrmax[0] = 4;
        romc_addrmax[1] = 0; // not available

        ramc_addrmin = new int[ramc_ports];
        ramc_addrmin[0] = 4;  
        ramc_addrmin[1] = 4;  
        ramc_addrmax = new int[ramc_ports];
        ramc_addrmax[0] = 4;
        ramc_addrmax[1] = 4;

        ramc_readable = new boolean[ramc_ports];
        ramc_readable[0] = true;
        ramc_readable[1] = true;
        ramc_writable = new boolean[ramc_ports];
        ramc_writable[0] = true;
        ramc_writable[1] = false;

        ramc_mux_addr_bits_maxc = 3; // extra address bits allowed using MUXes

        queue_buffer_min_cram_depth = 16;
        queue_buffer_max_cram_depth = 16;

        defaultOutputDirect = false;
        defaultContinuous = false;
        deviceHasTBUF = true;
        deviceAllowsQueue = true;
        deviceAllowsCMemory = true;
        deviceAllowsRMemory = true;
        
        deviceFDRSEpatch = false;
        deviceCascadeGates = false;
        deviceCPLDarith = false;

        // Initialise element ports for ANDn, ORn, XORn, XNORn and LUTn
        // for n 1 to 4.
        XTDECode.initialise(1, "UNISIMS");
    }
}
