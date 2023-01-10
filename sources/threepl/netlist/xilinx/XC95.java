package threepl.netlist.xilinx;


/**
 * <p>This class implements Xilinx library elements particular to
 * the XC9500 CPLD. These are low level elements
 * from which the output EDIF netlist  is generated. All higher
 * level functions call these, directly or indirectly.</p>
 */
public class XC95 extends XTDECode {   

    public XC95 () {
        fname = "XC9500";
        lutwidth = 9;
        srladdrwidth = 0;
        addsubinputwidth = 9999;    // no explicit limit
        dspregwidth = 0;            // no multiplier/DSP
        multinputwidth1 = 0;        // no multiplier/DSP
        multinputwidth2 = 0;        // no multiplier/DSP
        divinputwidth = 9999;       // no explicit limit

        ramb_ports = 0;
        ramc_ports = 0;

        defaultOutputDirect = true;
        defaultContinuous = true;
        deviceHasTBUF = false;
        deviceAllowsQueue = false;
        deviceAllowsCMemory = false;
        deviceAllowsRMemory = false;
        
        deviceFDRSEpatch = false;
        deviceCascadeGates = true;
        deviceCPLDarith = true;
        deviceOBUFFD = false;

        // Initialise element ports for ANDn, ORn, XORn, XNORn and LUTn
        // for n 1 to 4.
        XTDECode.initialise(1, "XC95");
    }
}
