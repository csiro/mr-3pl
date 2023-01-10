package threepl.netlist;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVarList;
import threepl.exceptions.ExEx;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * <p>This class is the interface between the TDE list
 * and the EDIF netlist. There are subclasses XC2S, XC3S, XCV,
 * XC2V and  XC2VP which contain code specific to a family of
 * FPGAs. The object on which methods in this class are invoked
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
 *
 * For another vendor or product class GenFunctions would be extended, e.g.
 * <pre>
 *
 *        TDECode           implementation of TDE modules - vendor independent
 *           |
 *      GenFunctions        general support methods - vendor independent
 *           |
 *       YElements          product Y... general unified elements
 *           |
 *       YFunctions         product Y... higher level functions
 *           |
 *       YTDECode           product Y... implementation of TDE modules
 *           |
 *     ---------------------
 *    /   /  |   \   \      \
 * Y..  Y.. Y... Y... Y...  Y...      elements specific to Y.. products
 * </pre>
 *
 * The classes above starting with "Y" would be in subdirectory "y...".
 *
 * The following fields are assigned values by the constructor of the family
 * class.
 *
 * String    fname                          name of the FPGA family
 * int       lutwidth                       number of inputs to CLB LUT
 * int       dspregwidth                    effective width of multiplier/DSP
 *                                          block when used as a register
 * int       srladdrwidth                   shift register address width
 * int       addsubinputwidth               maximum adder/subtracter input width
 * int       multinputwidth                 maximum multiplier input width
 * int       divinputwidth                  maximum divider input width
 * int       ramb_ports                     maximum number of block RAM ports
 * boolean[] ramb_readable                  which block RAM ports are readable
 * boolean[] ramb_writable                  which block RAM ports are writable
 * int[]     ramb_dwidths = new int[20]     block RAM data widths for address widths
 * int       ramb_addrmin[]                 block RAM smallest address width for # of ports
 * int       ramb_addrmax[]                 block RAM largest address width for # of ports
 * int       ramc_ports                     maximum number of CLB RAM ports
 * boolean[] ramc_readable                  which CLB RAM ports are readable
 * boolean[] ramc_writable                  which CLB RAM ports are writable
 * int[]     ramc_dwidths = new int[20]     CLB RAM data widths for address widths
 * int       ramc_addrmin[]                 CLB RAM and ROM smallest address width for # of ports
 * int       ramc_addrmax[]                 CLB RAM port max block address width for # of ports
 * int       romc_addrmin[]                 CLB ROM port min block address width for # of ports
 * int       romc_addrmax[]                 CLB ROM port max block address width for # of ports
 * int[]     queue_buffer_cram_depths       allowed depths for CLB RAM queue buffers
 * int       queue_buffer_min_cram_depth    smallest depth for CLB RAM queue buffers
 * int       queue_buffer_max_cram_depth    largest depth for CLB RAM queue buffers
 * int[]     queue_buffer_rram_depths       allowed depths for block RAM queue buffers
 * int       queue_buffer_min_rram_depth    smallest depth for block RAM queue buffers
 * int       queue_buffer_max_rram_depth    largest depth for block RAM queue buffers
 * boolean   defaultOutputDirect            is the output mode direct flag default?
 * boolean   defaultStaticContinuous        is the static mode continuous flag default?
 * boolean   deviceHasTBUF                  does device have 3-state buffers?
 * boolean   deviceAllowsQueue              does device allow queue mode?
 *                                          queues require memory
 * boolean   deviceAllowsCMemory            does device allow cmemory mode?
 *                                          requires CLB RAM
 * boolean   deviceAllowsRMemory            does device allow rmemory mode?
 *                                          requires block RAM
 * int       ramc_mux_addr_bits_maxc        CLB RAM address bits beyond maximum
 *                                          the number of extra address bits for
 *                                          CLB RAM gained by MUXing
 * boolean   deviceFDRSEpatch               change FDRSE to FDCE + logic for CPLD
 * boolean   deviceCascadeGates             skip conversion of cascaded or wide gates
 * boolean   deviceCPLDarith                use CPLD addition/subtraction logic
 *                                          i.e. simple logic instead of carry chain
 *
 * NOTE: when 3PL is run repeatedly via the GUI class TDECode and all
 * subclasses down to the class specific to the FPGA family are
 * constructed afresh. Thus all static variables are re-created. Classes
 * Element and Net are NOT reconstructed and hence static variables in
 * these classes must be explicitly re-initialised on each run!
 * 
 * portmap is a map of all external ports. These may be I/O ports (pads)
 * or ports to on-chip hardware such as high speed serial devices or may
 * be external EDIF ports where this netlist is a logic core.
 * Port types are INPUT, OUTPUT or INOUT.
 * For I/O ports portstring is "PORT_pin" where 'pin' is the PAD/pin designation.
 * I/O ports are single bit ports and have a pad/pin location string.
 * For non-I/O ports portstring is the full line for the port entry in the EDIF file,
 * e.g. (port I (direction INPUT)) for a single bit port or
 * (port (array (rename A "A[0:17]") 18) (direction INPUT)) for a port which is an array.
 * Non-I/O ports have loc == null.
 */
public abstract class TDECode implements Constant, TDEConstants, NetConstants {
    /**
     * Class to hold information for a port.
     */
    public class Port {
        public Net          inet;       // input port net if any
        public Net          onet;       // output port net if any
        public TDEVPtype    type;       // port type - IN, OUT, INOUT
        public String       portstring; // string appearing in EDIF port entry
        public String       loc;        // pin location string if I/O
        
        /**
         * Create a non-I/O port.
         * @param   portstring is the EDIF port name
         * @param   port_type is the type of port - INPUT, OUTUT, INOUT or NONE
         */
        public Port (String portstring, TDEVPtype port_type) {
            type = port_type;
            this.portstring = portstring;
            loc = null;
        }
        
        public Port (String portstring, TDEVPtype port_type, String loc) {
            type = port_type;
            this.portstring = portstring;
            this.loc = loc;
        }
    }

    protected static final TDEVPtype[]      ptypes = {TDEVPtype.INPUT, TDEVPtype.OUTPUT, TDEVPtype.INOUT};
    //protected static ArrayList<Object>      ext_ports;
    //protected static ArrayList<TDEVPtype>   ext_port_dirs;
    //protected static ArrayList<String>      ext_port_pins;
    protected static TreeMap<String,Port>   portmap;

    protected static  String    fname;
    public    static  int       lutwidth;
    public    static  int       dspregwidth;
    public    static  int       srladdrwidth;
    public    static  int       addsubinputwidth;
    public    static  int       multinputwidth1;
    public    static  int       multinputwidth2;
    public    static  int       divinputwidth;
    protected static  int       ramc_ports;
    protected static  boolean[] ramc_readable;
    protected static  boolean[] ramc_writable;
    protected static  int[]     ramc_dwidths;
    protected static  int[]     ramc_addrmin;
    protected static  int[]     ramc_addrmax;
    protected static  int[]     romc_addrmin;
    protected static  int[]     romc_addrmax;
    protected static  String    big_RAMB;
    protected static  String    small_RAMB;
    protected static  String    big_FIFO;
    protected static  String    small_FIFO;
    protected static  int       ramb_ports;
    protected static  boolean[] ramb_readable;
    protected static  boolean[] ramb_writable;
    protected static  int[]     ramb_dwidths;
    protected static  int[]     ramb_addrmin;
    protected static  int[]     ramb_addrmax;
    protected static  int       ramc_mux_addr_bits_maxc;
    protected static  int       queue_buffer_min_cram_depth;
    protected static  int       queue_buffer_max_cram_depth;
    protected static  int       queue_buffer_min_rram_depth;
    protected static  int       queue_buffer_max_rram_depth;
    protected static  boolean   defaultOutputDirect;
    protected static  boolean   defaultContinuous;
    protected static  boolean   deviceHasTBUF;
    protected static  boolean   deviceAllowsQueue;
    protected static  boolean   deviceAllowsCMemory;
    protected static  boolean   deviceAllowsRMemory;
    protected static  boolean   deviceFDRSEpatch;
    protected static  boolean   deviceCascadeGates;
    protected static  boolean   deviceCPLDarith;
    protected static  boolean   deviceOBUFFD;
    protected static  boolean   deviceOBUFFTFF;
    
    public TDECode () {
        //ext_ports = new ArrayList<Object>();
        //ext_port_dirs = new ArrayList<TDEVPtype>();
        //ext_port_pins = new ArrayList<String>();
        portmap = new TreeMap<String, Port>();
    }

    /**
     * Get the LUT width for this FPGA.
     * @return  the LUT width
     */
    public int LUTWidth () {
        return(lutwidth);
    }

    /**
     * Get the effective width of a multiplier or DSP
     * block for this FPGA when used as a register.
     * @return  the width
     */
    public int DSPRegWidth () {
        return(dspregwidth);
    }
       

    /**
     * Get the maximum adder/subtracter input width for this FPGA.
     * @return  the maximum adder/subtracter input width
     */
    public int AddSubWidth () {
        return(addsubinputwidth);
    }

    /**
     * Get the 1st operand multiplier input width for this FPGA.
     * @return  the maximum multiplier input width
     */
    public int MultWidth1 () {
        return(multinputwidth1);
    }

    /**
     * Get the 2nd operand multiplier input width for this FPGA.
     * @return  the maximum multiplier input width
     */
    public int MultWidth2 () {
        return(multinputwidth2);
    }

    /**
     * Get the maximum divider input width for this FPGA.
     * @return  the maximum divider input width
     */
    public int DivWidth () {
        return(divinputwidth);
    }

    /**
     * Get the number of memory ports for combinatorial-output RAM.
     * @param   loc is the source file location
     * @return  the number of ports
     */
    public int cramPorts (SrcLoc loc) {
        if (ramc_ports == 0)
            throw new ExEx("combinatorial output RAM not available in FPGA family " +
                            fname, loc);
        return(ramc_ports);
    }

    /**
     * Get the number of memory ports for registered-output RAM.
     * @param   loc is the source file location
     * @return  the number of ports
     */
    public int rramPorts (SrcLoc loc) {
        if (ramb_ports == 0)
            throw new ExEx("registered output RAM not available in FPGA family " +
                            fname, loc);
        return(ramb_ports);
    }

    /**
     * Get the maximum address width for a combinatorial-output RAM.
     * @param   ports is the number of ports
     * @param   crom is true if this is for a ROM
     * @param   loc is the source file location
     * @return  the mamximum combinatorial-output RAM address width
     */
    public int cramAwidthMax (int ports, boolean crom, SrcLoc loc) {
        if (ramc_ports == 0)
            throw new ExEx("combinatorial output RAM not available in FPGA family " +
                            fname, loc);
        if (crom)
            return(romc_addrmax[ports-1] + ramc_mux_addr_bits_maxc);
        return(ramc_addrmax[ports-1] + ramc_mux_addr_bits_maxc);
    }

    /**
     * Get the maximum address width for a registered-output RAM.
     * @param   ports is the number of ports
     * @param   loc is the source file location
     * @return  the maximum registered-output RAM address width
     */
    public int rramAwidthMax (int ports, SrcLoc loc) {
        if (ramb_ports == 0)
            throw new ExEx("registered output RAM not available in FPGA family " +
                            fname, loc);
        return(ramb_addrmax[ports-1]);  // actually both array members have same value!
    }

    /**
     * Get the minimum address width for a registered-output RAM.
     * @param   ports is the number of ports
     * @param   loc is the source file location
     * @return  the minimum registered-output RAM address width
     */
    public int rramAwidthMin (int ports, SrcLoc loc) {
        if (ramb_ports == 0)
            throw new ExEx("registered output RAM not available in FPGA family " +
                            fname, loc);
        return(ramb_addrmin[ports-1]);  // actually both array members have same value!
    }
    
    /**
     * Get the data width associated with an address width for
     * a registered-output RAM.
     * @param   i is the address width
     * @param   loc is the source file location
     * @return  the data width
     */    
    /* NOT USED!
    public int  rramDwidth (long i, SrcLoc loc) {
        if (ramb_ports == 0)
            throw new ExEx("registered output RAM not available in FPGA family " +
                            fname, loc);
        if (i >= ramb_dwidths.length)
            return(0);
        return(ramb_dwidths[(int)i]);
    }
    */
    
    /**
     * Determine if a combinatorial-output RAM port is readable.
     * @param   i is the port number
     * @param   loc is the source file location
     * @return  true if the port is readable
     */
    public boolean cramReadable (int i, SrcLoc loc) {
        if (ramc_ports == 0)
            throw new ExEx("combinatorial output RAM not available in FPGA family " +
                            fname, loc);
        return(ramc_readable[i]);
    }
    
    /**
     * Determine if a combinatorial-output RAM port is writable.
     * @param   i is the port number
     * @param   loc is the source file location
     * @return  true if the port is writable
     */
    public boolean cramWritable (int i, SrcLoc loc) {
        if (ramc_ports == 0)
            throw new ExEx("combinatorial output RAM not available in FPGA family " +
                            fname, loc);
        return(ramc_writable[i]);
    }
    
    /**
     * Determine if a registered-output RAM port is readable.
     * @param   i is the port number
     * @param   loc is the source file location
     * @return  true if the port is readable
     */
    public boolean rramReadable (int i, SrcLoc loc) {
        if (ramb_ports == 0)
            throw new ExEx("registered output RAM not available in FPGA family " +
                            fname, loc);
        return(ramb_readable[i]);
    }
    
    /**
     * Determine if a registered-output RAM port is writable.
     * @param   i is the port number
     * @param   loc is the source file location
     * @return  true if the port is writable
     */
    public boolean rramWritable (int i, SrcLoc loc) {
        if (ramb_ports == 0)
            throw new ExEx("registered output RAM not available in FPGA family " +
                            fname, loc);
        return(ramb_writable[i]);
    }

    /**
     * Check if a requested queue buffer depth using combinatorial output memory
     * is allowed.
     * @param   d is a requested queue buffer depth.
     * @return  true if the queue buffer depth is allowed using combinatorial output
     *          memory
     */
    public boolean queueDepthUsingCram (int d) {
        return((d >= queue_buffer_min_cram_depth) && (d <= queue_buffer_max_cram_depth));
    }

    /**
     * Check if a requested queue buffer depth using registered output memory
     * is allowed.
     * @param   d is a requested queue buffer depth.
     * @return  true if the queue buffer depth is allowed using registered output
     *          memory
     */
    public boolean queueDepthUsingRram (int d) {
        return((d >= queue_buffer_min_rram_depth) && (d <= queue_buffer_max_rram_depth));
    }

    /**
     * Find the minimum CRAM queue depth.
     * @return  the minimum CRAM queue depth
     */
    public int minCRAMQueueDepth () { return(queue_buffer_min_cram_depth); }

    /**
     * Find the maximum CRAM queue depth.
     * @return  the maximum CRAM queue depth
     */
    public int maxCRAMQueueDepth () { return(queue_buffer_max_cram_depth); }

    /**
     * Find the minimum RRAM queue depth.
     * @return  the minimum RRAM queue depth
     */
    public int minRRAMQueueDepth () { return(queue_buffer_min_rram_depth); }

    /**
     * Find the maximum RRAM queue depth.
     * @return  the maximum RRAM queue depth
     */
    public int maxRRAMQueueDepth () { return(queue_buffer_max_rram_depth); }
    
    /**
     * Should output mode 'direct' attribute be default for this device?
     * @return  true if 'direct' attribute to be default
     */
    public boolean outputDirect () { return(defaultOutputDirect); }
 
    /**
     * Should selectvalue mode 'continuous' attribute be default for this device?
     * @return  true if 'continuous' attribute to be  default
     */
    public boolean defaultContinuous () { return(defaultContinuous); }
 
    /**
     * Does device have 3-state buffers?
     * @return  true if device has 3-state buffers
     */
    public boolean hasTBUF () { return(deviceHasTBUF); }
 
    /**
     * Is 'queue' mode implemented for this device?
     * @return  true if 'queue' mode is implemented
     */
    public boolean allowQueue () { return(deviceAllowsQueue); }
 
    /**
     * Is 'cmemory' mode implemented for this device?
     * @return  true if 'cmemory' mode is implemented
     */
    public boolean allowCMemory () { return(deviceAllowsCMemory); }
 
    /**
     * Is 'rmemory' mode implemented for this device?
     * @return  true if 'rmemory' mode is implemented
     */
    public boolean allowRMemory () { return(deviceAllowsRMemory); }

    /**
     * Do we need to patch the FDRSE implementation for this device?
     * @return  true if we need to patch the FDRSE implementation
     */
    public boolean needFDRSEpatch () { return(deviceFDRSEpatch); }

    /**
     * Do we need to cascade gates for this device?
     * @return  true if we need to cascade gates
     */
    public boolean needCascadeGates () { return(deviceCascadeGates); }

    /**
     * Do we need different add and subtract for this device?
     * @return  true if we need different add and subtract
     */
    public boolean needCPLDarith () { return(deviceCPLDarith); }

    /**
     * Does this device have an optional I flip-flop in the output buffer?
     * @return  true if we can move a data flip-flop to the I/O block
     */
    public boolean hasOBUFFD () { return(deviceOBUFFD); }

    /**
     * Does this device have an optional T flip-flop in the output buffer?
     * @return  true if we can move a 3-state flip-flop to the I/O block
     */
    public boolean hasOBUFFTFF () { return(deviceOBUFFTFF); }
    
    /**
     * Output the external FPGA ports to the EDIF netlist.
     * @param   pw is the output interface to the EDIF netlist file
     */
    static public void outputExterns (PrintWriter pw) {
        Iterator<Map.Entry<String,Port>>    it;
        
        pw.println("    (interface");
        
        // Output non-I/O ports to the EDIF file.
        //
        it = portmap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, Port>     me = it.next();
            Port                    p = me.getValue();
            TDEVPtype               t = p.type;
            String                  pin = p.loc;
            String                  ps = p.portstring;
            
            if (pin == null)
                pw.println("     (port " + ps + " (direction " + t.toString() + "))");
        }
        
        // Output I/O ports to the EDIF file.
        //
        it = portmap.entrySet().iterator();        
        while (it.hasNext()) {
            Entry<String, Port>     me = it.next();
            Port                    p = me.getValue();
            TDEVPtype               t = p.type;
            String                  pin = p.loc;
            String                  ps = p.portstring;
            
            if (pin != null)
                pw.println("     (port " + ps + " (direction " + t.toString() + ") (property LOC (string \"" + pin + "\")))");
        }
        
        pw.println("    )");
    }
    
    /**
     * Check to see there are any GND or VCC nets. If not,
     * delete the GND or VCC element.
     */
    public static void optimiseGNDandVCC () {
        if ((Element.gnd.numOutPins() == 0))
            Element.gnd.strip();
        if ((Element.vcc.numOutPins() == 0))
            Element.vcc.strip();
    }
   
    /**
     * Optimise an INV.
     * If output not connected, delete.
     * Check for a low or high input.
     * If high delete the gate connecting the output net to Net.LO.
     * If low delete the gate connecting the output net to Net.HI.
     * Check for an output which is also an INV (and the output does not
     * go anywhere else), in which case delete both, connecting the input
     * of the 1st to the output of the 2nd. If the intermediate output
     * does go elsewhere, delete the 2nd inverter and connect the signals
     * driven by its output to the input of the 1st inverter.
     * @param   element is the INV element
     * @return  code for unchanged, changed or stripped
     */
    public static Ctype optimiseINV (Element element) {
        Net out = element.getOutPorts().get("O");
        if ((out == null) || (out.getNumInputs() == 0) && !out.isExtOutPort()) {
            element.strip();
            return(Ctype.STRIPPED);
        }
        Map.Entry<String,Net>   me = element.getInPorts().entrySet().iterator().next();
        Net                 in = element.getInPorts().get("I");
        //String              pinname  = me.getKey();
        Net                 net = me.getValue();
        ArrayList<Object>   ea;
        Element             e;
        
        if (net.isConnected(Net.HI)) {
            // inverter has VCC input
            element.strip();
            out.connect(Net.LO);
            return(Ctype.STRIPPED);
        } else if (net.isConnected(Net.LO)) {
            // inverter has GND input
            element.strip();
            out.connect(Net.HI);
            return(Ctype.STRIPPED);
        } else if (out.getNumInputs() == 1) {
            // inverter connects to single destination
            out.getInputElementInit();
            ea = out.getNextInputElement();
            e = (Element)ea.get(0);
            if (e.getType() == Gtype.INV) {
                // 1st inverter output connects ONLY to a 2nd inverter -
                // can eliminate both inverters
                Net out2 = e.getOutPorts().get("O");
                element.strip();
                e.strip();
                out2.connect(in);
                e.setType(Gtype.REMOVED);
                return(Ctype.STRIPPED);
            }
        } else {
            // check if inverter output connects to any other inverters
            // (other than the case above)
            out.getInputElementInit();
            while ((ea = out.getNextInputElement()) != null) {
                e = (Element)ea.get(0);
                if (e.getType() == Gtype.INV) {
                    // eliminate 2nd inverter
                    Net out2 = e.getOutPorts().get("O");
                    e.strip();
                    out2.connect(in);
                    e.setType(Gtype.REMOVED);
                }
            }
        }
        return(Ctype.UNCHANGED);
    }
    
    /**
     * Optimise an AND.
     * If output not connected, delete.
     * Check for any low or high inputs or duplicated inputs.
     * For high inputs, disconnect their nets.
     * For any low inputs delete the gate connecting the
     * output net to Net.LO.
     * For any duplicated inputs delete all except one.
     * If no inputs remain, delete the gate connecting the
     * output net to Net.LO.
     * If one input remains, delete the gate connecting the
     * output net to the remaining input net.
     * Check if the output goes only to another AND gate in
     * which case eliminate this gate and add its inputs to
     * the following gate.
     * @param   element is the AND element
     * @return  code for unchanged, changed or stripped
     */
    public static Ctype optimiseAND (Element element) {
        Ctype   change = Ctype.UNCHANGED;
        Net     out = element.getOutPorts().get("O");

        if ((out == null) || (out.getNumInputs() == 0) && !out.isExtOutPort()) {
            element.strip();
            return(Ctype.STRIPPED);
        }
        
        // If output only goes to the same type of gate, move the
        // inputs from this gate to the following and remove this gate.
        if (unNest(element, out, Gtype.AND))
            return(Ctype.STRIPPED);

        // Check for gate inputs which are GND, VCC or duplicated.
        change = inputEliminate (element, Net.HI, Net.LO, out);

        if (change == Ctype.CHANGED)
            change = remapInputs(change, element, true);
        return(change);
    }
    
    /**
     * Optimise an OR.
     * If output not connected, delete.
     * Check for any low or high inputs or duplicated inputs.
     * For low inputs, disconnect their nets.
     * For any high inputs delete the gate connecting the
     * output net to Net.HI.
     * For any duplicated inputs delete all except one.
     * If no inputs remain, delete the gate connecting the
     * output net to Net.LO.
     * If one input remains, delete the gate connecting the
     * output net to the remaining input net.
     * @param   element is the OR element
     * @return  code for unchanged, changed or stripped
     */
    public static Ctype optimiseOR (Element element) {
        Ctype   change = Ctype.UNCHANGED;
        Net     out = element.getOutPorts().get("O");

        if ((out == null) || (out.getNumInputs() == 0) && !out.isExtOutPort()) {
            element.strip();
            return(Ctype.STRIPPED);
        }
              
        // If output only goes to the same type of gate, move the
        // inputs from this gate to the following and remove this gate.
        if (unNest(element, out, Gtype.OR))
            return(Ctype.STRIPPED);

        // Check for gate inputs which are GND, VCC or duplicated.
        change = inputEliminate (element, Net.LO, Net.HI, out);

        if (change == Ctype.CHANGED)
            change = remapInputs(change, element, true);
        return(change);
    }
    
    /**
     * If output not connected, delete.
     *
     * Check an XOR for any low inputs.
     * If found delete the inputs, disconnecting their nets.
     * If no inputs remain, delete the gate, connecting the
     * output net to Net.LO.
     *
     * Check for any high inputs.
     * If found delete the inputs, disconnecting their nets,
     * counting them in the process.
     *
     * If there were an odd number of deleted high inputs,
     * convert the gate to XNOR.
     * If one input remains, delete the gate connecting the
     * output net to the remaining input net.
     * @return  code for unchanged, changed or stripped
     * @param   element is the XOR element
     */
    public static Ctype optimiseXOR (Element element) {
        Ctype   change = Ctype.UNCHANGED;
        Net     out = element.getOutPorts().get("O");

        if ((out == null) || (out.getNumInputs() == 0) && !out.isExtOutPort()) {
            element.strip();
            return(Ctype.STRIPPED);
        }
                
        // If output only goes to the same type of gate, move the
        // inputs from this gate to the following and remove this gate.
        if (unNest(element, out, Gtype.XOR))
            return(Ctype.STRIPPED);

        // Check for gate inputs which are GND or VCC.
        Iterator<Map.Entry<String,Net>> it = element.getInPorts().entrySet().iterator();
        boolean                         invert = false;

        while (it.hasNext()) {
            Map.Entry<String,Net>   me = it.next();
            String                  pinname  = me.getKey();
            Net                     net = me.getValue();
            if (net.isConnected(Net.LO)) {
                net.disconnect(element, pinname);
                it.remove();
                change = Ctype.CHANGED;
            } else if (net.isConnected(Net.HI)) {
                net.disconnect(element, pinname);
                it.remove();
                change = Ctype.CHANGED;
                invert = !invert;
            }
        }
        if (invert) {
            switch (element.getInPorts().size()) {
            case 0:
                element.changeElement("XNOR"+element.getCellName().substring(3), Gtype.XNOR, false);
                change = remapInputs(change, element, true); // just VCC or GND out
                break;
            case 1:
                element.changeElement("INV", Gtype.INV, false);
                change = remapInputs(change, element, true); // changes input I0 to I
                break;
            default:
                element.changeElement("XNOR"+element.getCellName().substring(3), Gtype.XNOR, false);
            }
            return(change);
        }
        if (change == Ctype.CHANGED)
            change = remapInputs(change, element, true);
        return(change);
    }

    /**
     * If the output of a gate only goes to the same type of gate,
     * remove the input to the following gate from this gate, move
     * inputs from this gate to the following and remove this gate.
     * @param   element is the gate Element
     * @param   out is the output Net of the gate
     * @param   gtype is the gate type, Gtype.AND, Gtype.OR or Gtype.XOR
     * @return  true if this gate has been merged and removed
     */
    public static boolean unNest (Element element, Net out, Gtype gtype) {
       if (deviceCascadeGates || (out.getNumInputs() != 1))
            return(false);

        out.getInputElementInit();
        ArrayList<Object>   ea = out.getNextInputElement();
        Element             e = (Element)ea.get(0);
        String              eport = (String)ea.get(1);
        if (e.getType() != gtype)
            return(false);

        // make a list of all inputs to this gate
        ArrayList<Net>                  a = new ArrayList<Net>();
        Iterator<Map.Entry<String,Net>> iit = element.getInputsIterator();
        while (iit.hasNext())
            a.add((iit.next().getValue()));
        // strip all nets from this gate
        element.strip();
        // remove Net 'out' from following gate inputs
        //out.disconnect(e, null);
        e.removeNet(eport);
        remapInputs(Ctype.IGNORE, e, false);
        // add the 1nputs to the following gate
        int j = e.numInPins();
        for (Net n : a)
            e.addInput("I"+j++, n);
        remapInputs(Ctype.IGNORE, e, false);
        return(true);
    }

    /**
     * Check for AND or OR gate inputs which are GND, VCC or duplicated.
     * If an input is connect to Net l1 it is removed. For AND l1 is Net.HI and
     * for OR l1 is Net.LO.
     * If an input is connect to Net l2, the gate is removed and
     * the output is driven by Net l2 instead. For AND l2 is Net.LO and for OR
     * l2 is Net.HI.
     * If an input is duplicated, any duplicates are removed.
     * @param   element is the gate Element
     * @param   l1 is an input that can be removed, either Net.LO or Net.HI
     * @param   l2 is an input that means the gate can be removed, either
     *          Net.LO or Net.HI
     * @param   out is the gate output Net
     *@return   the change type, Ctype.UNCHANGED, Ctype.CHANGED or Ctype.STRIPPED
     */
    public static Ctype inputEliminate (Element element, Net l1, Net l2, Net out) {
        Ctype                           change = Ctype.UNCHANGED;
        HashSet<Net.NET>                nets = new HashSet<Net.NET>();
        Iterator<Map.Entry<String,Net>> it = element.getInPorts().entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<String,Net>   me = it.next();
            String                  pinname  = me.getKey();
            Net                     net = me.getValue();
            if (net.isConnected(l1)) {
                net.disconnect(element, pinname);
                it.remove();
                change = Ctype.CHANGED;
            } else if (net.isConnected(l2)) {
                element.strip();
                out.connect(l2);
                return(Ctype.STRIPPED);
            } else if (nets.contains(net.getNET())) {
                net.disconnect(element, pinname);
                it.remove();
                change = Ctype.CHANGED;
            } else
                nets.add(net.getNET());
        }
        return(change);
    }
    
    /**
     * Check a LUT for an unconnected output and remove if so.
     * @return  code for unchanged, changed or stripped
     * @param   element is the LUT element
     */
    public static Ctype optimiseLUT (Element element) {
        Net     out = element.getOutPorts().get("O");
        if ((out == null) || (out.getNumInputs() == 0) && !out.isExtOutPort()) {
            element.strip();
            return(Ctype.STRIPPED);
        }
        return(Ctype.UNCHANGED);
    }

    /**
     * Optimise a synchronous set/reset flip-flop.
     * If output not connected, delete.
     * Check for R or S wired high and initial state to match.
     * If so delete the DFF and connect the output net
     * high or low.
     * Check for D input GND or null, SET input GND or null and
     * initial state low, in which case delete and connect
     * output to GND.
     * @param   element is the FDRS element
     * @return  code for unchanged, changed or stripped
     */
    public static Ctype optimiseFDRS (Element element) {
        Net out = element.getOutPorts().get("Q");
        if ((out == null) || (out.getNumInputs() == 0) && !out.isExtOutPort()) {
            element.strip();
            return(Ctype.STRIPPED);
        }
        Net d = element.getInPorts().get("D");
        Net s = element.getInPorts().get("S");
        Net r = element.getInPorts().get("R");
        boolean d_lo = (d == null) || d.isConnected(Net.LO);
        boolean d_hi = (d != null) && d.isConnected(Net.HI);
        boolean r_con = (r != null);
        boolean r_lo = (r == null) || r.isConnected(Net.LO);
        boolean r_hi = r_con && r.isConnected(Net.HI);
        boolean s_con = (s != null);
        boolean s_lo = (s == null) || s.isConnected(Net.LO);
        boolean s_hi = s_con && s.isConnected(Net.HI);
        String  ip = element.properties.get("INIT");
        boolean init_lo = (ip != null) && ip.equals("R");
        boolean init_hi = (ip != null) && ip.equals("S");;

        if (s_hi && init_hi) {
            element.strip();
            out.connect(Net.HI);
            return(Ctype.STRIPPED);
        }
        if (r_hi && init_lo) {
            element.strip();
            out.connect(Net.LO);
            return(Ctype.STRIPPED);
        }
        if (d_lo && init_lo) {
            if (r_lo && s_lo) {
                element.strip();
                out.connect(Net.LO);
                return(Ctype.STRIPPED);
            }
            if (r_hi && init_lo) {
                element.strip();
                out.connect(Net.LO);
                return(Ctype.STRIPPED);
            }
        }
        if (d_hi && init_hi) {
            if (!r_con) {
                element.strip();
                out.connect(Net.HI);
                return(Ctype.STRIPPED);
            }
        }
        return(Ctype.UNCHANGED);
    }

    /**
     * <p>Called to remove any gaps in the input port sequence of gates.
     * Removal of an input may have left the sequence I0, I1 etc with
     * a gap. Where port identifiers are changed the port in the source
     * net is also changed. The return change code is
     * whatever was passed as an argument. For two or more inputs the
     * inputs are remapped.</p>
     *
     * <p>If parameter optimise is true, gates with zero or one inputs are
     * optimised.</p>
     *
     *<p> An XNOR with one input is changed to INV.
     * Other gates with one input have the output and input nets disconnected
     * from the gate and the two nets are then connected.</p>
     *
     * <p>A gate with no inputs has the output net disconnected from the gate
     * and connected to Net.LO for an OR or XOR gate and Net.HI for an AND or
     * XNOR gate. In either case the return is Ctype.STRIPPED to inform the
     * caller that this gate is now defunct.</p>
     * @param   ct is the change type thus far
     * @param   element is the element
     * @param   optimise indicates that gates with no inputs or 1 input
     *          should be optimised
     * @return  the change type after this
     */
    public static Ctype remapInputs (Ctype ct, Element element, boolean optimise) {
        int n = element.getInPorts().size();
        Net inet;
        Net onet;
        if (optimise) {
            switch (n) {
            case 0:
                onet = element.getOutNet();
                element.strip();
                if ((element.getType() == Gtype.AND) ||
                    (element.getType() == Gtype.MULT_AND) ||
                    (element.getType() == Gtype.XNOR))
                    onet.connect(Net.HI);
                else
                    onet.connect(Net.LO);
                return(Ctype.STRIPPED);
            case 1:
                Iterator<Map.Entry<String,Net>> it = element.getInPorts().entrySet().iterator();
                Map.Entry<String,Net>           me1 = it.next();
                inet = me1.getValue();
                onet = element.getOutNet();
                if (element.getType() == Gtype.XNOR) {
                    element.changeElement("INV", Gtype.INV, true);
                    element.addInput("I", inet);
                    element.addOutput("O", onet);
                    return(Ctype.CHANGED);
                } else if (element.getType() == Gtype.INV) {
                    String port = me1.getKey();
                    inet.changePort(element, port, "I"); // change In to I
                    element.getInPorts().put("I", element.getInPorts().remove(port));
                    return(Ctype.CHANGED);
                } else {
                    element.strip();
                    onet.connect(inet);
                    return(Ctype.STRIPPED);
                }
            }
        }

        element.remapInputs();

        return(ct);
    }

    /**
     * Encode a selector.
     * Parameters:
     *  unselected out  - output state when no input selected (hex string)
     *  use ALU         - use an ALU for multiple arithmetic operators (boolean)
     *  DSP             - use a DSP block for an ALU (boolean)
     * The parameters may be omitted.
     * @param   params is a list of parameters
     * @param   inputs is a list of input signals, odd entries are
     *          select signals, even entries are data signals or signal arrays
     * @param   output is a list containing the output signal or signal array
     */
    public abstract void TDESELECT (ArrayList<Object> params, TDEVarList inputs, TDEVarList output);

    /**
     * Encode a three-state selector.
     * Parameters:
     *  unselected out  - output state when no input selected (hex string)
     * @param   params is a list of parameters
     * @param   inputs is a list of input signals, odd entries are
     *          select signals, even entries are data signals or signal arrays
     * @param   output is a list containing the output signal or signal array
     */
    public abstract void TDETSELECT (ArrayList<Object> params, TDEVarList inputs, TDEVarList output);

    /**
     * Encode a static variable (register).
     * The parameter list contains the following parameters -
     * <ul>
     * <li> String - the initial value as a hexadecimal string
     * <li> String - optional attribute key
     * <li> String - optional data for attribute key
     * <li> String - ...     other attribute pairs
     * </ul>
     * The inputs list contains the following -
     * <ul>
     * <li> signal - the clock (may be null if no data inputs)
     * <li> signal array - the data input(s) (may be null)
     * <li> signal array - the data clock-enable(s) (may be null)
     * <li> signal - optional reset
     * </ul>
     * The output list contains the output signal array.
     *
     * If the clock input is null a constant is generated and the other
     * inputs are ignored.
     * The data input must be the same width as the output.
     * The clock enable and reset inputs may be arrays in which case
     * they must be the same width as the input and output. Alternatively
     * they may both be width 1 in which case all bits will share the
     * same clock enable and reset. Either or both may be null.
     * @param   params is a list of parameters
     * @param   inputs is a list of input signals and signal arrays
     * @param   output is a list containing the output signal array
     */
    public abstract void TDEREG (ArrayList<Object> params, TDEVarList inputs, TDEVarList output);
   
    /**
     * Encode an inverter (1's complement).
     * @param   inputs is a list of input signals
     * @param   outputs is a list of output signals
     */
    public abstract void TDEINV (TDEVarList inputs, TDEVarList outputs);
   
    /**
     * Encode an AND.
     * @param   inputs is a list of input signals
     * @param   output is a list containing the output signal
     */
    public abstract void TDEAND (TDEVarList inputs, TDEVarList output);
    
    /**
     * Encode an OR.
     * @param   inputs is a list of input signals
     * @param   output is a list containing the output signal
     */
    public abstract void TDEOR (TDEVarList inputs, TDEVarList output);
    
    /**
     * Encode an XOR.
     * @param   inputs is a list of input signals
     * @param   output is a list containing the output signal
     */
    public abstract void TDEXOR (TDEVarList inputs, TDEVarList output);
    
    /**
     * Encode a D flip-flop. The 1st argument is a list of zero to two
     * parameters. These are -
     * <ul>
     * <li> an optional boolean initialisation state (may be null)
     * <li> an optional Boolean asynchronous set/reset/clr/pre mode selector
     * <li> an optional timing group identifier String
     * </ul>
     * The 1st parameter will be null if the initial state is not specified
     * but the 2nd parameter is present.
     * If the 2nd parameter is present and true the last two inputs are
     * CLR/PRE asynchronous clear and preset. If the 2nd parameter is
     * missing or false  the last two inputs are R/S synchronous reset and
     * set.
     * The 2nd argument contains a list of inputs which are in
     * the following order -
     * <ul>
     * <li> D - the data input
     * <li> C - the clock
     * <li> CE - the clock enable
     * <li> R or CLR - the synchronous reset or asynchronous clear
     * <li> S or PRE - the synchronous set or asynchronous preset
     * <li> optional String - further constraint
     * <li> .               - . etc
     * </ul>
     * Any of these list elements may be null.
     * @param   params is a list of parameters
     * @param   inputs is a list of input signals
     * @param   output is a list containing the output signal
     */
    public abstract void TDEDFF (ArrayList<Object> params, TDEVarList inputs, TDEVarList output);
    
    /**
     * Encode a single bit input buffer.
     * The parameter list contains the following parameters -
     * <ul>
     * <li> String  - optional element identifier
     * <li> String  - element package pin
     * <li> String  - 2nd element package pin for differential input, or null
     * <li> boolean - ibufg indicator
     * <li> boolean - differential indicator
     * </ul>
     * The outputs list contains the output signal, which is one bit wide.
     * @param   params is a list of parameters
     * @param   inputs is a list containing the input signal from the associated pad
     * @param   outputs is a list containing the output signal
     */
    public abstract void TDEIBUF (ArrayList<Object> params, TDEVarList inputs, TDEVarList outputs);
    
    /**
     * Encode a single bit output buffer.
     * The parameter list contains the following parameters -
     * <ul>
     * <li> String  - optional element identifier
     * <li> String  - element package pin
     * <li> String  - 2nd element package pin for differential input, or null
     * <li> boolean - ibufg indicator
     * <li> boolean - differential indicator
     * </ul>
     * The inputs list contains the input signal, 
     * which may be more than one bit wide, followed by an optional 3-state
     * enable signal which may be null.
     * @param   params is a list of parameters
     * @param   inputs is a list containing the input signal
     * @param   outputs is a list containing the output signal to the associated pad
     */
    public abstract void TDEOBUF (ArrayList<Object> params, TDEVarList inputs, TDEVarList outputs);
    
    /**
     * Encode a clock domain start signal.
     * @param   inputs is a list containing 3 signals -
     *          <ul>
     *          <li>the start level
     *          <li>the clock to which the start level is synchronised
     *          <li>the clock of the domain for which the start signal
     *          is destined
     *          </ul>
     * @param   output is a list containing the output signal
     */
    public abstract void TDESTART (TDEVarList inputs, TDEVarList output);
    
    /**
     * Encode an infinite loop.
     * @param   inputs is a list containing either 2 signals -
     *          <ul>
     *          <li>the clock domain start signal
     *          <li>the finish signal for the code contained within the loop
     *          </ul>
     * @param   output is a list containing 1 signal, the start signal for
     *          the code within the loop 
     */
    public abstract void TDEILOOP (TDEVarList inputs, TDEVarList output);
    
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
    public abstract void TDEWHEN (TDEVarList inputs, TDEVarList outputs);
    
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
    public abstract void TDEWHILE (TDEVarList inputs, TDEVarList outputs);

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
    public abstract void TDEDOWHILE (TDEVarList inputs, TDEVarList outputs);

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
    public abstract void TDERESYNC (TDEVarList inputs, TDEVarList output);
    
    /**
     * Encode a sample.
     * @param   param is a list containing 1 Boolean, true if the input clock
     *          frequency is greater than the output clock frequency
     * @param   inputs is a list containing 3 signals -
     *          <ul>
     *          <li>the input data
     *          <li>the clock for the input
     *          <li>the clock for the output
     *          </ul>
     * @param   output is a list containing 1 signal, the output data
     */
    public abstract void TDESAMPLE (ArrayList<Object> param, TDEVarList inputs, TDEVarList output);

    /**
     * Encode an execute via queue availability.
     * @param   params is a parameter list containing a single parameter -
     *          <ul>
     *          <li> a type code, 0 to 3 -
     *          </ul>
     *                                                      pri_in  pri_out bqavail     ubqwavail   ubqravail
     *          0    priority        no queues               Y       Y       null        null        null
     *          1    priority        buffered queues only    Y       Y       Y           null        null
     *          2    no priority     buffered queues only    null    null    av
     *          3    no priority     any queues              null    null    av | VCC    av | VCC    av | VCC
     * @param   inputs is a list containing 3 signals -
     *          <ul>
     *          <li>the clock
     *          <li>the start signal
     *          <li>the queue availability signal (level)
     *          <li>optional reset signal or null
     *          </ul>
     * @param   output is a list containing 1 signal, the delayed start
     */
    public abstract void TDEEXECP (ArrayList<Object> params, TDEVarList inputs, TDEVarList output);

    /**
     * Connect signals.
     *
     * <p>If the optional parameter is provided, this is a cast operation
     * connecting signals representing different data types or widths.
     * The parameter is true for sign extension and false for zero padding.
     * This only occurs if the output is wider than the input. If the converse
     * then truncation occurs.
     *
     * <p>If the input is a constant the outputs are connected to an
     * associated pattern of HIs and LOs representing the constant as
     * weighted binary.
     *
     * <p>If input has width 1 and one output has width 1 they are connected.
     *
     * <p>If input has width 1 and output is wider, all outputs are connected
     * to the single input.
     *
     * <p>If the input and output widths are both greater than one then
     * they must be the same and corresponding pairs will be connected.
     * @param   param is a list optionally containing a single Boolean
     *          indicating sign extension if true or zero padding if false
     * @param   input is a list containing 1 or more signals
     * @param   output is a list containing 1 or more signals
     */
    public abstract void TDECONNECT (ArrayList<Object> param, TDEVarList input, TDEVarList output);
     
    /**
     * Encode an arithmetic operator.
     * @param   params is a list containing -
     *          <ul>
     *          <li>integer operator code
     *          <li>integer 1st operand type
     *          <li>integer 2nd operand type if binary or ternary
     *          <li>integer 3rd operand type if ternary
     *          <li>Integer result type
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
    public abstract void TDEOPERATOR (ArrayList<Object> params, TDEVarList inputs, TDEVarList output);
   
    /**
     * Encode a parallel wait.
     * When the last block finish signal goes high the output goes high.
     * @param   inputs is a list containing clock signal, optional reset signal
     *          and multiple block finish signals
     * @param   output is a list containing the finish signal
     */
    public abstract void TDEWAIT (TDEVarList inputs, TDEVarList output);

    /**
     * Encode a delay.
     * @param   params is a list containing -
     *          <ul>
     *          <li>integer number of cycles to delay
     *          </ul>
     * @param   inputs is a list containing signals -
     *          <ul>
     *          <li>clock signal
     *          <li>input signal array
     *          <li>optional reset signal
     *          </ul>
     * @param   output is a list containing 1 signal -
     *          <ul>
     *          <li>output signal array
     *          </ul>
     */
    public abstract void TDEDEL (ArrayList<Object> params, TDEVarList inputs, TDEVarList output);

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
    public abstract void TDEDIVERGE (TDEVarList inputs, TDEVarList outputs);

    /**
     * Encode a queue buffer.
     * @param   params is a list containing 1 or 2 parameters -
     *          <ul>
     *          <li>the buffer depth
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
     *          <li>the output availability signal net
     *          <li>the register available signal net
     *          <li>optional buffer occupancy count for synchronous queue buffers
     *          <li>optional buffer free space count for synchronous queue buffers
     *          <li>optional buffer status synchronised to the read clock
     *              for asynchronous queue buffers
     *          <li>optional buffer status synchronised to the write clock
     *              for asynchronous queue buffers
     *          <li>an optional reset signal
     *          </ul>
     */
    public abstract void TDEQUEUEBUFFER (ArrayList<Object> params, TDEVarList inputs, TDEVarList outputs);
    
    /**
     * Encode a priority encoder.
     * There are n inputs and either n or n+1 outputs.
     * When a number of input signals are high the corresponding output
     * earliest in the output signal list will be high, all other output
     * signals being low. If there is an extra (trailing) output signal
     * it will be high if none of the inputs are high.
     * @param   inputs is a list containing the input signals
     * @param   outputs is a list containing the input signals
     */
    public abstract void TDEPRIORITY (TDEVarList inputs, TDEVarList outputs);
    
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
     *          <li> String  - optional attribute key
     *          <li> String  - optional data for attribute key
     *          <li> ...     - other attribute pairs
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
    public abstract void TDECRAM (ArrayList<Object> params, TDEVarList inputs, TDEVarList outputs);
    
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
    public abstract void TDERRAM (ArrayList<Object> params, TDEVarList inputs, TDEVarList outputs);

    /**
     * Encode an FPGA basic element.
     * @param   params is a list containing the element name string, the block name string (may be null), followed by triples of -
     *          <ul>
     *          <li>a string - the port name
     *          <li>an integer - 0 for input, 1 for output, 2 for three-state output or
     *                           3 for a clock input
     *          <li>an integer - optional offset for pin numbering
     *          </ul>
     * @param   inputs is a list of input signals
     * @param   outputs is a list of output signals
     */
    public abstract void TDEELEMENT (ArrayList<Object> params, TDEVarList inputs, TDEVarList outputs);
    
    /**
     * Add an EDIF port.
     * If the input is an array the port name will have 0, 1, 2 etc. appended.
     * @param   params is a list containing -
     *          <ul>
     *          <li>a string - the port name
     *          <li>an integer - 0 for input, 1 for output, 2 for three-state output
     *          <li>an integer - optional offset for pin numbering
     *          </ul>
     * @param   input is a list containing -
     *          <ul>
     *          <li>the signal or signal array to be connected
     *          </ul>
     */
    public abstract void TDEPORT (ArrayList<Object> params, TDEVarList input);
   
    /**
     * Process a special function.
     * The first parameter gives the function -
     *  <ul>
     *  <li>0 add line to UCF
     *  <li>1 add line to NCF
     *  <li>2 add UCF line for element
     *  <li>3 add NCF line for element
     *  <li>4 a delay line
     *  <li>5 unused
     *  <li>6 a GT_CUSTOM rocket
     *  <li>7 a GT_AURORA rocket
     *  <li>8 unused
     *  </ul>
     *
     * @param   params is a list or parameters
     * @param   inputs is a list of input signals
     * @param   outputs is a list of output signals
     *
     * <p>For type 0 or 1 -
     * <p>params list contains -
     *          <ul>
     *          <li>integer - 0 or 1
     *          <li>string - line to add to the UCF or NCF file
     *          </ul>
     * <p>inputs list contains -
     *          <ul>
     *          <li>an optional signal
     *          </ul>
     * <p>outputs list is empty
     *
     * <p>For type 2 or 3 -
     *<p> params list contains-
     *          <ul>
     *          <li>integer - 2 or 3
     *          <li>string - line to add to the UCF or NCF file
     *          </ul>
     * <p>inputs list contains -
     *<p> outputs list is empty
     *
     * <p>For type 4 -
     * <p>params list contains -
     *          <ul>
     *          <li>integer - 4
     *          <li>integer - number of clock cycles to delay if constant, else 0
     *          <li>string - 1st initialiser word (hexadecimal)
     *          <li>string - 2nd initialiser word (hexadecimal)
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
     *<p> For types 6 and 7 see the 3PL manual.
     */
    public abstract void TDESPECIAL (ArrayList<Object> params, TDEVarList inputs, TDEVarList outputs);

    /**
     * Apply family-specific optimisation to some TDEs.
     */
    public abstract void optimiseTDEs ();

    /**
     * Iteratively optimise gates and flip-flops.
     * @return  the number of optimisation passes
     */
    public abstract int optimiseNetlist ();

    /**
     * Convert all general AND, OR and XOR gates to appropriate
     * low-level elements.
     */
    public abstract void convertGenericToSpecific ();
    
    /**
     * Verify that there are no element problems. A gate element
     * must not have zero inputs.
     * All elements are examined and false is returned if any violate the
     * above rules. Violations are printed.
     * @return  true if all elements are OK
     */
    public abstract boolean verify ();
    
    /**
     * Find a Net given a pad pin designation.
     * @param   des is the pad designation
     * @return  an input or output Net or null
     */
    public abstract Net netFromPin (String des);
    
    /**
     * Return the EDIF file name extension.
     * @return  the EDIF file name extension String
     */
    public abstract String EDIFFileNameExtension ();
    
    /**
     * Generate any netlist constraints file(s).
     * @param   pw is the print writer to use for the output file
     * @param   author is a string to insert in a comment line in the
     *          constraint file(s)
     * @param   netlistcomments is a string to insert in a comment line
     *          in the constraint file(s)
     */
    public abstract void outputConstraints (PrintWriter pw, String author, String[] netlistcomments);
}
