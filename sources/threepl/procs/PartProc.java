package threepl.procs;

import static threepl.ThreePL.*;

import threepl.ThreePL;
import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.netlist.TDECode;
import threepl.netlist.xilinx.XC2C;
import threepl.netlist.xilinx.XC2S;
import threepl.netlist.xilinx.XC2V;
import threepl.netlist.xilinx.XC2VP;
import threepl.netlist.xilinx.XC3S;
import threepl.netlist.xilinx.XC4V;
import threepl.netlist.xilinx.XC5V;
import threepl.netlist.xilinx.XC6S;
import threepl.netlist.xilinx.XC6V;
import threepl.netlist.xilinx.XC7;
import threepl.netlist.xilinx.XC95;
import threepl.netlist.xilinx.XCV;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to set the FPGA or CPLD part.
 */
public class PartProc extends InbuiltProc implements Constant {

    /**
     * Construct the inbuilt procedure part().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public PartProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
    }

    /**
     * Execute the procedure part(). This does not generate executable code.
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
        SrcLoc      loc = inargs.getCallLoc();
        if (inargs.size() != 1)
            throw new ExEx("part() must have single argument", loc);
        if (outargs.size() != 0)
            throw new ExEx("part() cannot have output arguments", loc);

        Val     v = inargs.getVal(0);
        if (v.getMode() != Mode.IMMEDIATE)
            throw new ExEx("part() argument must be immediate mode", loc);
        if ((v.numWords() > 1) || v.getPrimType() != Ptype.STR)
            throw new ExEx("part() argument not a string", loc);
        partstring = v.getSingleSval(loc).toLowerCase();
        ThreePL.addDir("part", partstring);

        if (partstring.startsWith("xcv"))
            family = new XCV();
        else if (partstring.startsWith("xc2vp"))
            family = new XC2VP();
        else if (partstring.startsWith("xc2v"))
            family = new XC2V();
        else if (partstring.startsWith("xc2c"))
            family = new XC2C();
        else if (partstring.startsWith("xc2s"))
            family = new XC2S();
        else if (partstring.startsWith("xc3s"))
            family = new XC3S();
        else if (partstring.startsWith("xc4v"))
            family = new XC4V();
        else if (partstring.startsWith("xc5v"))
            family = new XC5V();
        else if (partstring.startsWith("xc6v"))
            family = new XC6V();
        else if (partstring.startsWith("xc6s"))
            family = new XC6S();
        else if (partstring.startsWith("xc7"))
            family = new XC7();                                 // common class for series 7
        else if (partstring.startsWith("xc95"))
            family = new XC95();
        else
            throw new ExEx("FPGA part '" + partstring + "' not known FPGA type");


        // If the device is a CPLD instead of an FPGA then -
        // 1. don't use static variable ALU mode
        // 2. don't use LUTs explicitly
        if (family.needCPLDarith()) {
            ALU = false;
            oack_use_lut = false;
            as_fifo_read_use_lut = false;
            as_fifo_empty_use_lut = false;
            add_sub_use_lut = false;
            ge_use_lut = false;
            mux_use_lut = false;
            incrdecr_use_lut = false;
        }
        
        if (family.defaultContinuous())
            continuous = true;

        addDir("srlAddrWidth", (long)TDECode.srladdrwidth);
    }
}
