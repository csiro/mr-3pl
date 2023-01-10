package threepl.netlist;


/**
 * This class contains constants used by Elements.
 * Gtype labels logic gate elements for recognition by optimisation
 * code. The enumerated constants apply to all FPGA families and all vendors
 * and where shared may be treated differently in each case.
 * Ctype describes the state of an element following an optimisation pass.
 * PortType indicates whether an element pin is an input, an output or
 * a 3-state output.
 */
public interface NetConstants {
    public enum Gtype {
        NONE,    // element is not subject to optimisation
        REMOVED, // element has been stripped during optimisation - delete from list
        // generic gates - apply to all FPGA families
        INV, AND, OR, XOR, XNOR, LUT,
        FDRSE, FDSE, FDRE, /*FDCPE, FDPE, FDCE, not subject to optimisation!*/
        // Xilinx-specific gates
        MUXCY, MUXF5, MUXF6, MUXF7, MUXF8, XORCY, ORCY, SRL, MULT_AND,
        CLB_RAM, BLK_RAM, MULT, MULT18X18, MULT18X18S, MULT25X18, MULT25X18S, MULREG,
        DSP48,
        // Fiddle for OBUF or OBUFT to prohibit flip-flop duplication
        NOSHADOW,
        // Selector - to be converted to basic gates for later optimisation 
        SELECTOR
        // ....-specific gates
    };
    public enum Ctype {IGNORE, UNCHANGED, CHANGED, STRIPPED};
    public enum PortType {INPORT, OUTPORT, TSPORT};
}
