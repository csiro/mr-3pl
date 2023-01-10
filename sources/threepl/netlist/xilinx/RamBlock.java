package threepl.netlist.xilinx;

import static threepl.parser.Functions.binToHex;
import static threepl.parser.Functions.zeropad;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

import threepl.exceptions.ExEx;
import threepl.netlist.Element;
import threepl.netlist.Net;
import threepl.netlist.NetConstants.Gtype;


/**
 * This class contains parameters describing a registered memory block.
 * It is used when allocating and creating block RAMs.
 * 
 * device  type element      amin     amax    dmax    dmin
 *
 * XC2S      1   RAMB4         8       12      16      1
 * XCV       1   RAMB4         8       12      16      1 
 * XC2V      1   RAMB4         8       12      16      1
 *           2   RAMB16        9       14      32+4    1
 * XC2VP     1   RAMB4         8       12      16      1 
 *           2   RAMB16        9       14      32+4    1  
 * XC3S      2   RAMB16        9       14      32+4    1
 * XC4V      2   RAMB16        9       14      32+4    1
 * XC5V      3   RAMB18       10       14      16+2    1          ++
 *           4   RAMB36       10       15      32+4    1          ++
 * XC6S      5   RAMB8BWER     9       13      16+2    1          ++
 *           6   RAMB16BWER    9       14      32+4    1          ++
 * XC6V      3   RAMB18       10       14      16+2    1          ++
 *           4   RAMB36       10       15      32+4    1          ++
 * XC7       3   RAMB18       10       14      16+2    1          ++
 *           4   RAMB36       10       15      32+4    1          ++
 *
 * ++ address inputs padded at LSB end
 */
public class RamBlock {
    int                 awidth;     // address width of block
    int                 dwidth;     // data width of block
    int                 type;       // type of block, e.g. 0 for RAMB18, 1 for RAMB36
    Net                 wea;        // port A write enable
    Net                 ena;        // port A enable
    Net                 clka;       // port A clock
    Net[]               addra;      // port A address
    Net[]               ida;        // port A data input
    Net[]               oda;        // port A data output
    Net                 web;        // port B write enable
    Net                 enb;        // port B enable
    Net                 clkb;       // port B clock
    Net[]               addrb;      // port B address
    Net[]               idb;        // port B data input
    Net[]               odb;        // port B data output
    String[]            init;       // initialisation array binary strings
    int                 arrayformat;
    ArrayList<String>   properties;
   
    public static TreeMap<String,Integer>   RAMB_types = new TreeMap<String,Integer>();
    public static int[][]                   RAMB_dwidths = new int[7][20];
    
    // Data widths of block RAMs indexed by address width.
    //
    public static final int[] NONE_dwidths =       { 0,  0,  0,  0,  0,  0,  0,  0,
                                                     0,  0,  0,  0,  0,  0,  0,  0,
                                                     0,  0,  0,  0};
    public static final int[] RAMB4_dwidths =      { 0,  0,  0,  0,  0,  0,  0,  0,
                                                    16,  8,  4,  2,  1,  0,  0,  0,
                                                     0,  0,  0,  0};
    public static final int[] RAMB16_dwidths =     { 0,  0,  0,  0,  0,  0,  0,  0,
                                                     0, 36, 18,  9,  4,  2,  1,  0,
                                                     0,  0,  0,  0};
    public static final int[] RAMB18_dwidths =     { 0,  0,  0,  0,  0,  0,  0,  0,
                                                     0,  0, 18,  9,  4,  2,  1,  0,
                                                     0,  0,  0,  0};
    public static final int[] RAMB36_dwidths =     { 0,  0,  0,  0,  0,  0,  0,  0,
                                                     0,  0, 36, 18,  9,  4,  2,  1,
                                                     0,  0,  0,  0};
    public static final int[] RAMB8BWER_dwidths =  { 0,  0,  0,  0,  0,  0,  0,  0,
                                                     0, 18,  9,  4,  2,  1,  0,
                                                     0,  0,  0,  0};
    public static final int[] RAMB16BWER_dwidths = { 0,  0,  0,  0,  0,  0,  0,  0,
                                                     0, 36, 18,  9,  4,  2,  1,  0,
                                                     0,  0,  0,  0};

    static {
        RAMB_types.put("NONE", 0);       RAMB_dwidths[0] = NONE_dwidths;    // dummy entry for no BRAM
        RAMB_types.put("RAMB4", 1);      RAMB_dwidths[1] = RAMB4_dwidths;
        RAMB_types.put("RAMB16", 2);     RAMB_dwidths[2] = RAMB16_dwidths;
        RAMB_types.put("RAMB18", 3);     RAMB_dwidths[3] = RAMB18_dwidths;
        RAMB_types.put("RAMB36", 4);     RAMB_dwidths[4] = RAMB36_dwidths;
        RAMB_types.put("RAMB8BWER", 5);  RAMB_dwidths[5] = RAMB8BWER_dwidths;
        RAMB_types.put("RAMB16BWER", 6); RAMB_dwidths[6] = RAMB16BWER_dwidths;
    }
    
    public RamBlock (
        int                 awidth,
        int                 dwidth,
        int                 type,
        Net                 wea,    
        Net                 ena,    
        Net                 clka,     
        Net[]               addra,  
        Net[]               ida,
        Net[]               oda,
        Net                 web,    
        Net                 enb,    
        Net                 clkb,     
        Net[]               addrb,  
        Net[]               idb,
        Net[]               odb,
        String[]            init,
        int                 arrayformat,
        ArrayList<String>   properties
    ) {
        this.awidth = awidth;
        this.dwidth = dwidth;
        this.type = type;
        this.wea = wea;
        this.ena = ena;
        this.clka = clka;
        this.addra = addra;
        this.ida = ida;
        this.oda = oda;
        this.web = web;
        this.enb = enb;
        this.clkb = clkb;
        this.addrb = addrb;
        this.idb = idb;
        this.odb = odb;
        this.init = init;
        this.arrayformat = arrayformat;
        this.properties = properties;
    }
    
    /**
     * Construct a dual port RAMB4.
     * Only the following combinations of widths are allowed -
     * <pre>
     * address data
     *   8      16 (256 deep x 16 bits)
     *   9       8 (512 deep x 8 bits)
     *  10       4 (1024 deep x 4 bits)
     *  11       2 (2048 deep x 2 bits)
     *  12       1 (4096 deep x 1 bit)
     * </pre>
     */
    public void RAMB () {
        switch (type) {
        case 1:
            RAMB4();
            return;
        case 2:
            RAMB16();
            return;
        case 3:
        case 4:
            RAMB18_RAMB36();
            return;
        case 5:
            RAMB8BWER();
            return;
        case 6:
            RAMB16BWER();
            return;
        default:
            throw new ExEx("SYSTEM ERROR: WRONG RAMB TYPE");
        }
    }
    
    
    /**
     * Construct a single or dual port RAMB4.
     * Only the following combinations of widths are allowed -
     * <pre>
     * address data
     *   8     16  (256 deep x 16 bits)
     *   9      8  (512 deep x 8 bits)
     *  10      4  (1024 deep x 4 bits)
     *  11      2  (2048 deep x 2 bits)
     *  12      1  (4096 deep x 1 bit)
     * </pre>
     */
    private void RAMB4 () {
        Gtype   gtype = (init == null) ? Gtype.BLK_RAM : Gtype.NONE;
        int     init_lines   = 64;
        boolean dual_port = clkb != null;
        Element e = new Element("RAMB4_S" + dwidth + (dual_port ? "_S" + dwidth : ""), gtype);
                
        if (dual_port) {
            // port A
            e.addInputArray("DIA", ida, dwidth, Net.LO, arrayformat);
            e.addInputArray("ADDRA",addra, awidth,  Net.LO, arrayformat);
            e.addInput("CLKA", clka);
            e.addInput("WEA", wea != null ? wea : Net.LO);
            e.addInput("ENA", ena != null ? ena : Net.HI);
            e.addInput("RSTA", Net.LO);
            e.addOutputArray("DOA", oda, arrayformat);

            // port B
            e.addInputArray("DIB", idb, dwidth, Net.LO, arrayformat);
            e.addInputArray("ADDRB", addrb, awidth, Net.LO, arrayformat);
            e.addInput("CLKB", clkb);
            e.addInput("WEB", web != null ? web : Net.LO);
            e.addInput("ENB", enb != null ? enb : Net.HI);
            e.addInput("RSTB", Net.LO);
            e.addOutputArray("DOB", odb, arrayformat);
        } else {
            e.addInputArray("DI", ida, dwidth, Net.LO, arrayformat);
            e.addInputArray("ADDR",addra, awidth,  Net.LO, arrayformat);
            e.addInput("CLK", clka);
            e.addInput("WE", wea != null ? wea : Net.LO);
            e.addInput("EN", ena != null ? ena : Net.HI);
            e.addInput("RST", Net.LO);
            e.addOutputArray("DO", oda, arrayformat);
        }

        raminit(e, init_lines, init, dwidth, 0);
        
        Iterator<String>    pit = properties.iterator();
        while (pit.hasNext())
            e.addProperty(pit.next(), pit.next());
    }
    
    /**
     * Construct a Spartan6 dual port RAMB8BWER.
     * Only the following combinations of widths are allowed -
     * <pre>
     * address data
     *   9     18  (512 deep x 18 bits)
     *  10      9  (1024 deep x 9 bits)
     *  11      4  (2048 deep x 4 bits)
     *  12      2  (4096 deep x 2 bits)
     *  13      1  (8192 deep x 1 bit)
     * </pre>
     */
    private void RAMB8BWER () {
        // Set gate type if no initialisation. This is to allow later
        // optimisation specifically for dual-port RAMS used as queue
        // buffers. If the RAM is initialised it is not later optimised.
        Gtype   gtype = (init == null) ? Gtype.BLK_RAM : Gtype.NONE;
        int     init_lines = 32;
        boolean dual_port = clkb != null;
        int     rdwidth = dwidth;   // RAM block data width
        int     rpwidth = 0;        // RAM block parity width
        
        Element e = new Element("RAMB8BWER", gtype);
        Net[]   wea_ = new Net[2];
        Net[]   web_ = new Net[2];
        for (int i=0 ; i<2 ; i++) {
            wea_[i] = wea;
            web_[i] = web;
        }
        
        // port A
        e.addInputArrayTop("ADDRAWRADDR", addra, awidth, 13, Net.LO, arrayformat);
        e.addInput("CLKAWRCLK", clka);
        e.addInputArray("WEAWEL", wea_, arrayformat);
        e.addInput("ENAWREN", ena != null ? ena : Net.HI);
        e.addInput("RSTA", Net.LO);

        if (dual_port) {
            // port B
            e.addInputArrayTop("ADDRBWRADDR", addrb, awidth, 13, Net.LO, arrayformat);
            e.addInputArray("ADDRBRDADDR", addrb, awidth, Net.LO, arrayformat);
            e.addInput("CLKBRDCLK", clkb);
            e.addInputArray("WEBWEU", web_, arrayformat);
            e.addInput("ENBRDEN", enb != null ? enb : Net.HI);
            e.addInput("RSTB", Net.LO);
        }

        switch (dwidth) {
        case 1:
        case 2:
        case 4:
            e.addInputArray("DIADI", ida, dwidth, Net.LO, arrayformat);
            e.addOutputArray("DOADO", oda, arrayformat);
            if (dual_port) {
                e.addInputArray("DIBDI", idb, dwidth, Net.LO, arrayformat);
                e.addOutputArray("DOBDO", odb, arrayformat);
            }
            break;
        case 9:
            rdwidth = 8;
            rpwidth = 1;
            e.addInputArray("DIADI", "DIPADIP", ida, 8, dwidth, Net.LO, arrayformat);
            e.addOutputArray("DOADO", "DOPADOP", 8, oda, arrayformat);
            if (dual_port) {
                e.addInputArray("DIBDI", "DIPBDIP", idb, 8, dwidth, Net.LO, arrayformat);
                e.addOutputArray("DOBDO", "DOPBDOP", 8, odb, arrayformat);
            }
            break;
        case 18:
            rdwidth = 16;
            rpwidth = 2;
            e.addInputArray("DIADI", "DIPADIP", ida, 16, dwidth, Net.LO, arrayformat);
            e.addOutputArray("DOADO", "DOPADOP", 16, oda, arrayformat);
            if (dual_port) {
                e.addInputArray("DIBDI", "DIPBDIP", idb, 16, dwidth, Net.LO, arrayformat);
                e.addOutputArray("DOBDO", "DOPBDOP", 16, odb, arrayformat);
            }
        }
        
        raminit(e, init_lines, init, rdwidth, rpwidth);
        
        Iterator<String>    pit = properties.iterator();
        while (pit.hasNext())
            e.addProperty(pit.next(), pit.next());
    }
        
    /**
     * Construct a Spartan6 dual port RAMB16BWER.
     * Only the following combinations of widths are allowed -
     * <pre>
     * address data
     *   9     36  (512 deep x 36 bits)
     *  10     18  (1024 deep x 18 bits)
     *  11      9  (2048 deep x 9 bits)
     *  12      4  (4096 deep x 4 bits)
     *  13      2  (8192 deep x 2 bits)
     *  14      1  (16384 deep x 1 bit)
     * </pre>
     */
    private void RAMB16BWER () {
        // Set gate type if no initialisation. This is to allow later
        // optimisation specifically for dual-port RAMS used as queue
        // buffers. If the RAM is initialised it is not later optimised.
        Gtype   gtype = (init == null) ? Gtype.BLK_RAM : Gtype.NONE;
        int     init_lines = 64;
        boolean dual_port = clkb != null;
        int     rdwidth = dwidth;   // RAM block data width
        int     rpwidth = 0;        // RAM block parity width

        Element e = new Element("RAMB16BWER", gtype);
        Net[]   wea_ = new Net[4];
        Net[]   web_ = new Net[4];
        for (int i=0 ; i<4 ; i++) {
            wea_[i] = wea != null ? wea : Net.LO;
            web_[i] = web != null ? web : Net.LO;
        }

        // port A
        if (clka == null) {
            e.addInput("ENA", Net.LO);
            e.addProperty("DATA_WIDTH_A", "0");
        } else {
            e.addInput("CLKA", clka);
            e.addInput("ENA", ena != null ? ena : Net.LO);
            e.addInputArray("WEA", wea_, 4, Net.LO, arrayformat);
            e.addInputArrayTop("ADDRA", addra, awidth, 14, Net.LO, arrayformat);
            e.addProperty("DATA_WIDTH_A", Integer.toString(dwidth));
        }
        e.addInput("RSTA", Net.LO);

        if (dual_port) {
            // port B
            if (clkb == null) {
                e.addInput("ENB", Net.LO);
                e.addProperty("DATA_WIDTH_B", "0");
            } else {
                e.addInput("CLKB", clkb);
                e.addInput("ENB", enb != null ? enb : Net.LO);
                e.addInputArray("WEB", web_, 4, Net.LO, arrayformat);
                e.addInputArrayTop("ADDRB", addrb, awidth, 14, Net.LO, arrayformat);
                e.addProperty("DATA_WIDTH_B", Integer.toString(dwidth));
            }
            e.addInput("RSTB", Net.LO);
        }

        switch (dwidth) {
        case 1:
        case 2:
        case 4:
            e.addInputArray("DIA", ida, dwidth, Net.LO, arrayformat);
            e.addOutputArray("DOA", oda, arrayformat);
            if (dual_port) {
                e.addInputArray("DIB", idb, dwidth, Net.LO, arrayformat);
                e.addOutputArray("DOB", odb, arrayformat);
            }
            break;
        case 9:
            rdwidth = 8;
            rpwidth = 1;
            e.addInputArray("DIA", "DIPA", ida, 8, dwidth, Net.LO, arrayformat);
            e.addOutputArray("DOA", "DOPA", 8, oda, arrayformat);
            if (dual_port) {
                e.addInputArray("DIB", "DIPB", idb, 8, dwidth, Net.LO, arrayformat);
                e.addOutputArray("DOB", "DOPB", 8, odb, arrayformat);
            }
            break;
        case 18:
            rdwidth = 16;
            rpwidth = 2;
            e.addInputArray("DIA", "DIPA", ida, 16, dwidth, Net.LO, arrayformat);
            e.addOutputArray("DOA", "DOPA", 16, oda, arrayformat);
            if (dual_port) {
                e.addInputArray("DIB", "DIPB", idb, 16, dwidth, Net.LO, arrayformat);
                e.addOutputArray("DOB", "DOPB", 16, odb, arrayformat);
            }
            break;
        case 36:
            rdwidth = 32;
            rpwidth = 4;
            e.addInputArray("DIA", "DIPA", ida, 32, dwidth, Net.LO, arrayformat);
            e.addOutputArray("DOA", "DOPA", 32, oda, arrayformat);
            if (dual_port) {
                e.addInputArray("DIB", "DIPB", idb, 32, dwidth, Net.LO, arrayformat);
                e.addOutputArray("DOB", "DOPB", 32, odb, arrayformat);
            }
        }

        raminit(e, init_lines, init, rdwidth, rpwidth);
        
        Iterator<String>    pit = properties.iterator();
        while (pit.hasNext())
            e.addProperty(pit.next(), pit.next());

    }
    
    /**
     * Construct a dual port RAMB16.
     * Only the following combinations of widths are allowed -
     * <pre>
     * address data
     *   9     36  (512 deep x 36 bits)
     *  10     18  (1024 deep x 18 bits)
     *  11      9  (2048 deep x 9 bits)
     *  12      4  (4096 deep x 4 bits)
     *  13      2  (8192 deep x 2 bits)
     *  14      1  (16384 deep x 1 bit)
     * </pre>
     */
    private void RAMB16 () {
        // Set gate type if no initialisation. This is to allow later
        // optimisation specifically for dual-port RAMS used as queue
        // buffers. If the RAM is initialised it is not later optimised.
        Gtype   gtype = (init == null) ? Gtype.BLK_RAM : Gtype.NONE;
        int     init_lines   = 64;
        boolean dual_port = clkb != null;
        Element e = new Element("RAMB16_S" + dwidth + (dual_port ? "_S" + dwidth : ""), gtype);
        int     rdwidth = dwidth;   // RAM block data width
        int     rpwidth = 0;        // RAM block parity width


        if (dual_port) {
            // port A
            e.addInputArray("ADDRA", addra, awidth, Net.LO, arrayformat);
            e.addInput("CLKA", clka);
            e.addInput("WEA", wea != null ? wea : Net.LO);
            e.addInput("ENA", ena != null ? ena : Net.HI);
            e.addInput("SSRA", Net.LO);
            
            // port B
            e.addInputArray("ADDRB", addrb, awidth, Net.LO, arrayformat);
            e.addInput("CLKB", clkb);
            e.addInput("WEB", web != null ? web : Net.LO);
            e.addInput("ENB", enb != null ? enb : Net.HI);
            e.addInput("SSRB", Net.LO);
        } else {
            e.addInputArray("ADDR", addra, awidth, Net.LO, arrayformat);
            e.addInput("CLK", clka);
            e.addInput("WE", wea != null ? wea : Net.LO);
            e.addInput("EN", ena != null ? ena : Net.HI);
            e.addInput("SSR", Net.LO);
        }

        switch (dwidth) {
        case 1:
        case 2:
        case 4:
            if (dual_port) {
                e.addInputArray("DIA", ida, dwidth, Net.LO, arrayformat);
                e.addOutputArray("DOA", oda, arrayformat);
                e.addInputArray("DIB", idb, dwidth, Net.LO, arrayformat);
                e.addOutputArray("DOB", odb, arrayformat);
            } else {
                e.addInputArray("DI", ida, dwidth, Net.LO, arrayformat);
                e.addOutputArray("DO", oda, arrayformat);
            }
            break;
        case 9:
            rdwidth = 8;
            rpwidth = 1;
            if (dual_port) {
                e.addInputArray("DIA", "DIPA", ida, 8, dwidth, Net.LO, arrayformat);
                e.addOutputArray("DOA", "DOPA", 8, oda, arrayformat);
                e.addInputArray("DIB", "DIPB", idb, 8, dwidth, Net.LO, arrayformat);
                e.addOutputArray("DOB", "DOPB", 8, odb, arrayformat);
            } else {
                e.addInputArray("DI", "DIP", ida, 8, dwidth, Net.LO, arrayformat);
                e.addOutputArray("DO", "DOP", 8, oda, arrayformat);
            }
            break;
        case 18:
            rdwidth = 16;
            rpwidth = 2;
            if (dual_port) {
                e.addInputArray("DIA", "DIPA", ida, 16, dwidth, Net.LO, arrayformat);
                e.addOutputArray("DOA", "DOPA", 16, oda, arrayformat);
                e.addInputArray("DIB", "DIPB", idb, 16, dwidth, Net.LO, arrayformat);
                e.addOutputArray("DOB", "DOPB", 16, odb, arrayformat);
            } else {
                e.addInputArray("DI", "DIP", ida, 16, dwidth, Net.LO, arrayformat);
                e.addOutputArray("DO", "DOP", 16, oda, arrayformat);
            }
            break;
        case 36:
            rdwidth = 32;
            rpwidth = 4;
            if (dual_port) {
                e.addInputArray("DIA", "DIPA", ida, 32, dwidth, Net.LO, arrayformat);
                e.addOutputArray("DOA", "DOPA", 32, oda, arrayformat);
                e.addInputArray("DIB", "DIPB", idb, 32, dwidth, Net.LO, arrayformat);
                e.addOutputArray("DOB", "DOPB", 32, odb, arrayformat);
            } else {
                e.addInputArray("DI", "DIP", ida, 32, dwidth, Net.LO, arrayformat);
                e.addOutputArray("DO", "DOP", 32, oda, arrayformat);
            }
        }
        
        raminit(e, init_lines, init, rdwidth, rpwidth);

        Iterator<String>    pit = properties.iterator();
        while (pit.hasNext())
            e.addProperty(pit.next(), pit.next());
    }
    
    /**
     * Construct a dual port RAMB18, RAMB18SDP, RAMB36 or RAMB36SDP.
     * Only the following combinations of widths are allowed -
     * <pre>
     * address RAMB18 data  RAMB36 data
     *  10     18           36         (1024 deep)
     *  11      9           18         (2048 deep)
     *  12      4            9         (4096 deep)
     *  13      2            4         (8192 deep)
     *  14      1            2         (16384 deep)
     *  15      -            1         (32768 deep)
     * </pre>
     */
    private void RAMB18_RAMB36 () {
        // Set gate type if no initialisation. This is to allow later
        // optimisation specifically for dual-port RAMS used as queue
        // buffers. If the RAM is initialised it is not later optimised.
        boolean dual_port = clkb != null;
        Gtype   gtype = (init == null) ? Gtype.BLK_RAM : Gtype.NONE;
        boolean ramb36 = (type == 4);
        int     rdwidth = dwidth;   // RAM block data width
        int     rpwidth = 0;        // RAM block parity width
        
        // The 7-series RAMB elements are "RAMB18E1" and "RAMB36E1",
        // but  Vivado will convert the generic forms used here.
        // The port names are very peculiar for the specific elements above
        // so the code here reads better when using the generic elements.
        String  ename        = ramb36 ? "RAMB36" : "RAMB18";
        int     amax         = ramb36 ?       15 :       14;
        int     init_lines   = ramb36 ?      128 :       64;
            
        Element e = new Element(ename, gtype);
        
        Net[]   wea_ = new Net[ramb36 ? 4 : 2];
        Net[]   web_ = new Net[ramb36 ? 4 : 2];
        for (int i=0 ; i<2 ; i++) {
            wea_[i] = wea != null ? wea : Net.LO;
            web_[i] = web != null ? web : Net.LO;
        }
        if (ramb36)
            for (int i=2 ; i<4 ; i++) {
                wea_[i] = wea != null ? wea : Net.LO;
                web_[i] = web != null ? web : Net.LO;
            }
        
        // port A
        e.addInputArrayTop("ADDRA", addra, awidth, amax, Net.LO, arrayformat);
        e.addInput("CLKA", clka);
        if (clka == null) {
            e.addInput("ENA", Net.LO);
            e.addProperty("READ_WIDTH_A", "0");
            e.addProperty("WRITE_WIDTH_A", "0");
        } else {
            e.addInput("ENA", ena != null ? ena : Net.HI);
            e.addProperty("READ_WIDTH_A", (oda == null) ? "0" : Integer.toString(dwidth));
            e.addProperty("WRITE_WIDTH_A", (ida == null) ? "0" : Integer.toString(dwidth));
        }
        e.addInputArray("WEA", wea_, arrayformat);
        e.addProperty("DOA_REG", "0");
        e.addInput("SSRA", Net.LO);
        e.addInput("REGCEA", Net.LO);

        // port B
        e.addInputArrayTop("ADDRB", addrb, awidth, amax, Net.LO, arrayformat);
        e.addInput("CLKB", clkb);
        if (clkb == null) {
            e.addInput("ENB", Net.LO);
            e.addProperty("READ_WIDTH_B", "0");
            e.addProperty("WRITE_WIDTH_B", "0");
        } else {
            e.addInput("ENB", enb != null ? enb : Net.HI);
            e.addProperty("READ_WIDTH_B", (odb == null) ? "0" : Integer.toString(dwidth));
            e.addProperty("WRITE_WIDTH_B", (idb == null) ? "0" : Integer.toString(dwidth));
        }
        e.addInputArray("WEB", web_, arrayformat);
        e.addProperty("DOB_REG", "0");
        e.addInput("SSRB", Net.LO);
        e.addInput("REGCEB", Net.LO);
        
        if (ramb36) {
            e.addInput("CASCADEINREGA", Net.LO);
            e.addInput("CASCADEINREGB", Net.LO);
            e.addInput("CASCADEINLATA", Net.LO);
            e.addInput("CASCADEINLATB", Net.LO);
        }

        switch (dwidth) {
        case 1:
        case 2:
        case 4:
            e.addInputArray("DIA", ida, dwidth, Net.LO, arrayformat);
            e.addOutputArray("DOA", oda, arrayformat);
            if (dual_port) {
                e.addInputArray("DIB", idb, dwidth, Net.LO, arrayformat);
                e.addOutputArray("DOB", odb, arrayformat);
            }
            break;
        case 9:
            rdwidth = 8;
            rpwidth = 1;
            e.addInputArray("DIA", "DIPA", ida, 8, 9, Net.LO, arrayformat);
            e.addOutputArray("DOA", "DOPA", 8, oda, arrayformat);
            if (dual_port) {
                e.addInputArray("DIB", "DIPB", idb, 8, 9, Net.LO, arrayformat);
                e.addOutputArray("DOB", "DOPB", 8, odb, arrayformat);
            }
            break;
        case 18:
            rdwidth = 16;
            rpwidth = 2;
            e.addInputArray("DIA", "DIPA", ida, 16, 18, Net.LO, arrayformat);
            e.addOutputArray("DOA", "DOPA", 16, oda, arrayformat);
            if (dual_port) {
                e.addInputArray("DIB", "DIPB", idb, 16, 18, Net.LO, arrayformat);
                e.addOutputArray("DOB", "DOPB", 16, odb, arrayformat);
            }
            break;
        case 36:
            rdwidth = 32;
            rpwidth = 4;
            e.addInputArray("DIA", "DIPA", ida, 32, 36, Net.LO, arrayformat);
            e.addOutputArray("DOA", "DOPA", 32, oda, arrayformat);
            if (dual_port) {
                e.addInputArray("DIB", "DIPB", idb, 32, 36, Net.LO, arrayformat);
                e.addOutputArray("DOB", "DOPB", 32, odb, arrayformat);
            }
        }
        
        raminit(e, init_lines, init, rdwidth, rpwidth);
        
        Iterator<String>    pit = properties.iterator();
        while (pit.hasNext())
            e.addProperty(pit.next(), pit.next());
    }

    /**
     * Initialise block RAM.
     * The initialisation array is null if there is no initialisation.
     * Trailing entries in the initialisation array may be null.
     * @param   e is the element
     * @param   init_lines is the number of main initialisation lines
     * @param   init is an array of binary strings, one for each word, the
     *          length of each being the block data width
     * @param   dwidth is the block data width
     * @param   pwidth is the block parity width
     */
    private static void raminit (
        Element     e,
        int         init_lines,
        String[]    init,
        int         dwidth,
        int         pwidth
    ) {        
        String      p;
        String      s;
        
        // BRAM data bits.
        int         i = 0;
        int         j;
        int         start = pwidth;
        int         finish = start + dwidth;
        for (j=0 ; j<init_lines ; j++) {
            StringBuffer sb = new StringBuffer();
            for (int k=0 ; k<256 ; k+=dwidth, i++) {
                if ((init != null) && (init[i] != null))
                    sb.insert(0, init[i].substring(start, finish));
                else
                    sb.insert(0, zeropad(dwidth));
            }
            p = "INIT_";
            if (j < 16)
                p += "0";
            p += Integer.toHexString(j).toUpperCase();
            s = binToHex(sb);
            e.addProperty(p, s);
        }
        if (pwidth == 0)
            return;
        
        // BRAM parity bits.
        i = 0;
        start = 0;
        finish = start + pwidth;
        int init_plines = init_lines / 8;
        for (j=0 ; j<init_plines ; j++) {
            StringBuffer sb = new StringBuffer();
            for (int k=0 ; k<256 ; k+=pwidth, i++) {
                if ((init != null) && (init[i] != null))
                    sb.insert(0, init[i].substring(start, finish));
                else
                    sb.insert(0, zeropad(pwidth));
            }
            p = "INITP_0";
            p += Integer.toHexString(j).toUpperCase();
            s = binToHex(sb);
            e.addProperty(p, s);
       }
    }
}
