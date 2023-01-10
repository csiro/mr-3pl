package threepl.netlist.xilinx;

import java.util.TreeMap;

import threepl.exceptions.ExEx;
import threepl.netlist.Element;
import threepl.netlist.Net;


/**
 * This class contains parameters describing a FIFO block.
 */
public class FifoBlock
{
    int                 depth;      // depth of block
    int                 dwidth;     // data width of block
    int                 type;       // type of block, e.g. 0 for FIFO16, 1 for FIFO18 etc.
    Net                 push;       // push signal
    Net                 pop;        // pop signal
    Net[]               in;         // data input
    Net[]               out;        // data output
    Net                 empty;      // empty output signal
    Net                 full;       // full output signal
    Net[]               rdcount;    // read count output
    Net[]               wrcount;    // write count output
    Net                 reset;      // reset signal
    Net                 wclk;       // write clock
    Net                 rclk;       // read clock
    int                 arrayformat;
    boolean             first;
    public static TreeMap<String,Integer>   FIFO_types = new TreeMap<String,Integer>();
    public static int[][]                   FIFO_dwidths = new int[4][16];
    
    // Data widths of BRAM FIFOs indexed by inferred address width.
    //
    public static final int[] NONE_dwidths =       { 0,  0,  0,  0,  0,  0,  0,  0,
                                                     0,  0,  0,  0,  0,  0,  0,  0};
    public static final int[] FIFO16_dwidths =     { 0,  0,  0,  0,  0,  0,  0,  0,
                                                     0, 36, 18,  9,  4,  0,  0,  0};
    public static final int[] FIFO36_dwidths =     { 0,  0,  0,  0,  0,  0,  0,  0,
                                                     0, 36, 18,  9,  4,  0,  0,  0};
    public static final int[] FIFO72_dwidths =     { 0,  0,  0,  0,  0,  0,  0,  0,
                                                     0, 72, 36, 18,  9,  4,  0,  0};

    static {
        FIFO_types.put("NONE", 0);      FIFO_dwidths[0] = NONE_dwidths;    // dummy entry for no FIFO
        FIFO_types.put("FIFO16", 1);    FIFO_dwidths[1] = FIFO16_dwidths;
        FIFO_types.put("FIFO36", 2);    FIFO_dwidths[3] = FIFO36_dwidths;
        FIFO_types.put("FIFO72", 3);    FIFO_dwidths[4] = FIFO72_dwidths;
    }
     
    public FifoBlock (
        int                 depth,
        int                 dwidth,
        int                 type,
        Net                 push,
        Net                 pop,
        Net[]               in,
        Net[]               out,
        Net                 empty,
        Net                 full,
        Net[]               rdcount,
        Net[]               wrcount,
        Net                 reset,
        Net                 wclk,
        Net                 rclk,
        int                 arrayformat,
        boolean             first
    ) {
        this.depth = depth;
        this.dwidth = dwidth;
        this.type = type;
        this.push = push;
        this.pop = pop;
        this.in = in;
        this.out = out;
        this.empty = empty;
        this.full = full;
        this.rdcount = rdcount;
        this.wrcount = wrcount;
        this.reset = reset;
        this.wclk = wclk;
        this.rclk = rclk;
        this.arrayformat = arrayformat;
        this.first = first;
    }
    
    /**
     * Construct a FIFO block.
     * Only the following combinations of widths are allowed -
     *
     * address data
     *   8      16 (256 deep x 16 bits)
     *   9       8 (512 deep x 8 bits)
     *  10       4 (1024 deep x 4 bits)
     *  11       2 (2048 deep x 2 bits)
     *  12       1 (4096 deep x 1 bit)
     *
     */
    public void FIFO () {
        switch (type) {
        case 1:
            FIFO16();
            return;
        case 2:
            FIFO36();
            return;
        case 3:
            FIFO72();
            return;
        default:
            throw new ExEx("SYSTEM ERROR: WRONG FIFO TYPE");
        }
    }
       
    /**
     * Construct a FIFO16 for Virtex4.
     * Only the following combinations of widths are allowed -
     *
     * address data
     *   9      36 (512 deep x 36 bits)
     *  10      18 (1024 deep x 18 bits)
     *  11      9  (2048 deep x 9 bits)
     *  12      4  (4096 deep x 4 bit)
     */
    private void FIFO16 () {
        Element e = new Element("FIFO16");
        
        e.addInput("WREN", push);
        e.addInput("RDEN", pop);
        e.addInput("WRCLK", wclk);
        e.addInput("RDCLK", rclk);
        e.addInput("RST", reset);

        switch (dwidth) {
        case 4:
            e.addInputArray("DI", in, 4, Net.LO, arrayformat);
            e.addOutputArray("DO", out, arrayformat);
            break;
        case 9:
            e.addInputArray("DI", "DIP", in, 8, 9, Net.LO, arrayformat);
            e.addOutputArray("DO", "DOP", 8, out, arrayformat);
            break;
        case 18:
            e.addInputArray("DI", "DIP", in, 16, 18, Net.LO, arrayformat);
            e.addOutputArray("DO", "DOP", 16, out, arrayformat);
            break;
        case 36:
            e.addInputArray("DI", "DIP", in, 32, 36, Net.LO, arrayformat);
            e.addOutputArray("DO", "DOP", 32, out, arrayformat);
        }

        if (first) {
            e.addOutput("EMPTY", empty);
            e.addOutput("FULL", full);
            e.addOutputArray("RDCOUNT", rdcount, arrayformat);
            e.addOutputArray("WRCOUNT", wrcount, arrayformat);
        }

        e.addProperty("FIRST_WORD_FALL_THROUGH", "TRUE");
        e.addProperty("EN_SYN", "FALSE");
        e.addProperty("DO_REG", "1");
        if ((dwidth != 36) || (depth != 512))
            e.addProperty("DATA_WIDTH", Integer.toString(dwidth));
    }

    /**
     * Construct a FIFO18 or FIFO_36 for Virtex5, Virtex6 or series 7
     * or a FIFO18_36 or FIFO36_72 for Virtex5.
     * Only the following combinations of widths are allowed -
     *
     * address data
     *  9       36 (512 deep x 36 bits)
     *  10      18 (1024 deep x 18 bits)
     *  11      9  (2048 deep x 9 bits)
     *  12      4  (4096 deep x 4 bit)
     */
    private void FIFO36 () {
        Element e = new Element(dwidth == 36 ? "FIFO18_36" : "FIFO18");
        
        e.addInput("WREN", push);
        e.addInput("RDEN", pop);
        e.addInput("WRCLK", wclk);
        e.addInput("RDCLK", rclk);
        e.addInput("RST", reset);

        switch (dwidth) {
        case 4:
            e.addInputArray("DI", in, 4, Net.LO, arrayformat);
            e.addOutputArray("DO", out, arrayformat);
            break;
        case 9:
            e.addInputArray("DI", "DIP", in, 8, 9, Net.LO, arrayformat);
            e.addOutputArray("DO", "DOP", 8, out, arrayformat);
            break;
        case 18:
            e.addInputArray("DI", "DIP", in, 16, 18, Net.LO, arrayformat);
            e.addOutputArray("DO", "DOP", 16, out, arrayformat);
            break;
        case 36:
            e.addInputArray("DI", "DIP", in, 32, 36, Net.LO, arrayformat);
            e.addOutputArray("DO", "DOP", 32, out, arrayformat);
        }

        if (first) {
            e.addOutput("EMPTY", empty);
            e.addOutput("FULL", full);
            e.addOutputArray("RDCOUNT", rdcount, arrayformat);
            e.addOutputArray("WRCOUNT", wrcount, arrayformat);
        }

        e.addProperty("FIRST_WORD_FALL_THROUGH", "TRUE");
        e.addProperty("EN_SYN", "FALSE");
        e.addProperty("DO_REG", "1");
        e.addProperty("DATA_WIDTH", Integer.toString(dwidth));
    }

    /**
     * Construct a FIFO36 or FIFO36_72 for Virtex5.
     * Only the following combinations of widths are allowed -
     *
     * address data
     *  9       72 (512 deep x 72 bits)
     *  10      36 (1024 deep x 36 bits)
     *  11      18 (2048 deep x 18 bits)
     *  12      9  (4096 deep x 9 bit)
     *  13      4  (4096 deep x 4 bit)
     */
    private void FIFO72 () {
        Element e = new Element(dwidth == 72 ? "FIFO36_72" : "FIFO36");
        
        e.addInput("WREN", push);
        e.addInput("RDEN", pop);
        e.addInput("WRCLK", wclk);
        e.addInput("RDCLK", rclk);
        e.addInput("RST", reset);

        switch (dwidth) {
        case 4:
            e.addInputArray("DI", in, 4, Net.LO, arrayformat);
            e.addOutputArray("DO", out, arrayformat);
            break;
        case 9:
            e.addInputArray("DI", "DIP", in, 8, 9, Net.LO, arrayformat);
            e.addOutputArray("DO", "DOP", 8, out, arrayformat);
            break;
        case 18:
            e.addInputArray("DI", "DIP", in, 16, 18, Net.LO, arrayformat);
            e.addOutputArray("DO", "DOP", 16, out, arrayformat);
            break;
        case 36:
            e.addInputArray("DI", "DIP", in, 32, 36, Net.LO, arrayformat);
            e.addOutputArray("DO", "DOP", 32, out, arrayformat);
            break;
        case 72:
            e.addInputArray("DI", "DIP", in, 64, 72, Net.LO, arrayformat);
            e.addOutputArray("DO", "DOP", 72, out, arrayformat);
        }

        if (first) {
            e.addOutput("EMPTY", empty);
            e.addOutput("FULL", full);
            e.addOutputArray("RDCOUNT", rdcount, arrayformat);
            e.addOutputArray("WRCOUNT", wrcount, arrayformat);
        }

        e.addProperty("FIRST_WORD_FALL_THROUGH", "TRUE");
        e.addProperty("EN_SYN", "FALSE");
        e.addProperty("DO_REG", "1");
        e.addProperty("DATA_WIDTH", Integer.toString(dwidth));
    }
}
