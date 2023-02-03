package threepl.netlist.xilinx;

import static threepl.ThreePL.*;
import static threepl.netlist.Net.netArray;
import static threepl.parser.Functions.binToHex;
import static threepl.parser.Functions.bits;
import static threepl.parser.Functions.hexToBin;
import static threepl.parser.Functions.intToGrayString;
import static threepl.parser.Functions.zeropad;
import static threepl.netlist.xilinx.RamBlock.RAMB_types;
import static threepl.netlist.xilinx.RamBlock.RAMB_dwidths;
import static threepl.netlist.xilinx.FifoBlock.FIFO_types;
import static threepl.netlist.xilinx.FifoBlock.FIFO_dwidths;

import java.util.ArrayList;
import threepl.codegen.TDEConstants;
import threepl.exceptions.ExEx;
import threepl.exec.Var;
import threepl.netlist.Net;
import threepl.parser.Constant;

/**
 * <p>This class contains useful library methods that are more
 * flexible or have higher functionality than basic FPGA components.
 * While this functionality is fairly general this class must be
 * considered Xilinx-specific. A class for another vendor would
 * probably be very similar.
 * These functions take an ArrayList of input Nets as argument
 * and return an output Net.
 *
 * These methods do not generate EDIF netlist code directly but
 * call methods in super class XElements, or in one of the bottom
 * subclasses, to do so.</p>
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
 */
public abstract class XFunctions extends XElements implements Constant, TDEConstants {
 
    /**
     * Encode a 2-input multiplexer for single nets.
     * @param   sel is the selector
     * @param   d0 is the data input net selected when sel is low
     * @param   d1 is the data input net selected when sel is high
     * @return  the output net
     */
    public Net mux2 (Net sel, Net d0, Net d1) {
        if (mux_use_lut)
            return(LUT("!I0*I1+I0*I2", sel, d0, d1));
        else
            return(OR(AND(INV(sel), d0), AND(sel, d1)));
    }
 
    /**
     * Encode a 2-input multiplexer for net arrays.
     * @param   sel is the selector
     * @param   d0 is the data input net array selected when sel is low
     * @param   d1 is the data input net array selected when sel is high
     * @return  the output net array
     */
    public Net[] mux2 (Net sel, Net[] d0, Net[] d1) {
        int     width = d0.length;
        Net[]   o = new Net[width];
        for (int i=0 ; i<width ; i++)
            if (mux_use_lut)
                o[i] = LUT("!I0*I1+I0*I2", sel, d0[i], d1[i]);
            else
                o[i] = OR(AND(INV(sel), d0[i]), AND(sel, d1[i]));
        return(o);
    }

    /**
     * Encode a register with a single clock enable net and reset net and
     * a hexadecimal initialisation string.
     * This is only called from XTDECode.TDEREG() for a single bit static mode
     * variable. In that case a DSP constraint is ignored.
     * This is also called from other methods in this class in which case
     * the constraints argument will be null.
     * @param   in is the data input net array
     * @param   c is the clock net
     * @param   ce is the clock enable net
     * @param   r is the reset net
     * @return  the output net array
     */
    public Net[] reg (
            Net[]               in,
            Net                 c,
            Net                 ce,
            Net                 r
    ) {
            return(reg(in, c, ce, r, null, false, null));
    } 

    /**
     * Encode a register with a single clock enable net and reset net and
     * a hexadecimal initialisation string.
     * This is only called from XTDECode.TDEREG() for a single bit static mode
     * variable. In that case a DSP constraint is ignored.
     * This is also called from other methods in this class in which case
     * the constraints argument will be null.
     * @param   in is the data input net array
     * @param   c is the clock net
     * @param   ce is the clock enable net
     * @param   r is the reset net
     * @param   sinit is the hexadecimal initialisation string or null
     * @param   iob is true if the register is to be located in IO blocks
     * @param   ids is an array of strings to be used as the netlist block IDs
     * @return  the output net array
     */
    public Net[] reg (
        Net[]       in,
        Net         c,
        Net         ce,
        Net         r,
        String      sinit,
        boolean     iob,
        String[]    ids
    ) {
        int     width = in.length;
        Net[]   out = new Net[width];
        String  bname;
 
        if (sinit == null)
            sinit = "0";
        for (int bit=0 ; bit<width ; bit++) {
            bname = (ids != null) ? ids[bit] : null;
            if ((strhexbit(sinit, bit) & 1) == 0)
                out[bit] = FDRSE(bname, in[bit], c, ce, r, Net.LO, "R", iob);
            else
                out[bit] = FDRSE(bname, in[bit], c, ce, Net.LO, r, "S", iob);
        }
        return(out);
    }
 
    /**
     * Encode a register with one clock enable and one reset per bit and
     * a hexadecimal initialisation string.
     * @param   in is the data input net array
     * @param   c is the clock net
     * @param   ce is the clock enable net array
     * @param   r is the reset net array
     * @param   sinit is the hexadecimal initialisation string
     * @param   iob is true if the register is to be located in IO blocks
     * @param   ids is an array of strings to be used as the netlist block IDs
     * @return  the output net array
     */
    public Net[] reg (
        Net[]       in,
        Net         c,
        Net[]       ce,
        Net[]       r,
        String      sinit,
        boolean     iob,
        String[]    ids
    ) {
        int     width = in.length;
        Net[]   out = netArray(width);
        String  bname;
 
        if (sinit == null)
            sinit = "0";
        out = new Net[width];
        for (int bit=0 ; bit<width ; bit++) {
            bname = (ids != null) ? ids[bit] : null;
            if ((strhexbit(sinit, bit) & 1) == 0)
                out[bit] = FDRSE(bname, in[bit], c, ce[bit], r[bit], Net.LO, "R", iob);
            else
                out[bit] = FDRSE(bname, in[bit], c, ce[bit], Net.LO, r[bit], "S", iob);
        }
        return(out);
    }
 
    /**
     * Encode a resync.
     * @param   in is the input net
     * @param   ci is the input clock net
     * @param   co is the output clock net
     * @return  the output net
     */
    public Net resync (Net in, Net ci, Net co) {
        Net en;
        Net block;
        Net clr1 = new Net();
        Net clr1_del = new Net();
        Net clr2;
 
        block = FDRE(in, ci, Net.HI, Net.LO, "R");
        en = FDCE(Net.HI, ci, AND(in, INV(block)), clr1_del, "R");
        clr2 = INV(en);
        clr1.connect(FDCE(INV(clr1), co, null, clr2, "R"));
        clr1_del.connect(FDCE(clr1, co, null, clr2, "R"));
        return(clr1);
        
        
        /*
         * The asynchronous design above failed in one isolated case, the reason for which
         * seemed to be violation of hold time. It has not failed previously.
         * The synchronous code below may be substituted. An extra cycle of delay
         * has been added to the loop to avoid another hold time problem.
         *
        Net en;
        Net block1;
        Net block2;
        Net r1 = null;
        Net out;
        
        block1 = FDRE(in, ci, Net.HI, Net.LO, "R");
        en = FDRE(Net.HI, ci, AND(in, INV(block1)), r1, "R");     
        r1 = FDRE(en, co, Net.HI, Net.LO, "R");
        block2 = FDRE(r1, co, Net.HI, Net.LO, "R");
        out = FDRE(AND(r1, INV(block2)), co, Net.HI, Net.LO, "R");
        return(out);
        */
        
    }


    /**
     * Encode a standard 2-deep synchronous queue buffer.
     * @param   in is the data input
     * @param   push is the push-to-queue input
     * @param   pop is the pop-from-queue input
     * @param   c is the clock
     * @param   ne is the returned not empty
     * @param   nf is the returned not full
     * @param   r is the buffer reset
     * @param   rr is the buffer reset output
     * @param   count is the returned buffer data count or null
     * @param   free is the returned free space count or null
     * @param   sinit is a buffer initialisation word (hexadecimal string)
     *          or null
     * @return  the buffer data output
     */
    @SuppressWarnings("unused")
    public Net[] queuebuffer (
        Net[]   in,
        Net     push,
        Net     pop,
        Net     c,
        Net     ne,
        Net     nf,
        Net     r,
        Net     rr,
        Net[]   count,
        Net[]   free,
        String  sinit
    ) {
        int     width = in.length;
        String  id = (sinit != null) ? sinit : "";
        String  io =  (sinit != null) ? "S" : "R";

        Net[][] data = new Net[2][width];
        for (int i=0 ; i<2 ; i++)
            for (int j=0 ; j<width ; j++)
                data[i][j] = new Net();
        Net[]   av = new Net[2];
        Net[]   ce = new Net[2];

        av[0] = new Net("av0");
        av[1] = new Net("av1");
        /*
        ce[0] = OR(     AND(INV(push),     pop ,     av[0] ,     av[1] ),
                        AND(    push , INV(pop), INV(av[0]),     av[1] )   );
        ce[1] = OR(     AND(INV(push),     pop ,                 av[1]) ,
                        AND(    push ,           INV(av[0]), INV(av[1])),
                        AND(    push ,     pop , INV(av[0]),     av[1] )   );
        */
        ce[0] = LUT("(!I0*I1*I2*I3)+(I0*!I1*!I2*I3)", push, pop , av[0] , av[1]);
        ce[1] = LUT("(!I0*I1*I3)+(I0*!I2*!I3)+(I0*I1*!I2*I3)",
                                                push, pop , av[0] , av[1]);
        if (in != null)
            data[0] = reg(in, c, ce[0], null);
        av[0].connect(FDRE(push, c, ce[0], r, "R"));
        if (in != null)
            data[1] = reg(mux2(av[0], in, data[0]), c, ce[1], r, id, false, null);
        av[1].connect(FDRE(OR(push, av[0]), c, ce[1], r, io));

        ne.connect(av[1]);
        nf.connect(INV(av[0]));
 
        if (count != null) {
            count[0].connect(AND(av[1], INV(av[0])));
            count[1].connect(AND(av[1], av[0]));
        }
        if (free != null) {
            if (count != null)
                free[0].connect(count[0]);
            else
                free[0].connect(AND(av[1], INV(av[0])));
            free[1].connect(AND(INV(av[1]), INV(av[0])));
        }

        if (rr != null) {
            if (r != null)
                rr.connect(r);
            else
                rr.connect(Net.LO);
        }
 
        if (in != null)
            return(data[1]);
        else
            return(null);
    }
 
    /**
     * Encode a deep synchronous queue buffer.
     * @param   in is the data input
     * @param   push is the write-to-queue input
     * @param   pop is the pop-from-queue input
     * @param   c is the clock
     * @param   ne is the returned not empty
     * @param   nf is the returned not full
     * @param   count is the returned buffer data count or null
     * @param   free is the returned free space count or null
     * @param   r is the buffer reset
     * @param   rr is the buffer reset output
     * @param   fifo true indicates that FIFO hardware should be used if
     *          the FPGA has it
     * @param   queuereg true indicates that an extra output register
     *          should be used to improve timing
     * @param   depth is the buffer depth which is a power of 2
     *          constrained by the FPGA family
     * @param   wcvar is the write clock variable
     * @param   rcvar is the read clock variable
     * @return  the buffer data output
     */
    // empty   NE    PUSH  POP  waddr   raddr   fcnt    write writer read new NE
    //
    // 1       0     1     0                            0     1      0    1
    // 0       1     1     0    ++              ++      1     0      0    1
    // 1       1     1     0    ++              ++      1     0      0    1
    // 0       1     1     1    ++      ++              1     0      1    1
    // 1       1     1     1                            0     1      0    1
    // 0       1     0     1            ++      --      0     0      1    1
    // 1       1     0     1                            0     0      0    0
    // All other combinations are illegal or do nothing.
    //
    // empty
    // full
    // NF = !full
    // NE = !(empty && NE && !PUSH && POP)
    // read = POP && !empty
    // write = PUSH && NE && !(empty && POP)
    // writer = PUSH && empty && !(NE && !POP)
    // raddr++ = read
    // waddr++ = write
    // fcnt++ = write && !read
    // fcnt-- = read && !write
    public Net[] sync_buffer (
        Net[]   in,
        Net     push,
        Net     pop,
        Net     c,
        Net     ne,
        Net     nf,
        Net[]   count,
        Net[]   free,
        Net     r,
        Net     rr,
        boolean fifo,
        boolean queuereg,
        int     depth,
        Var     wcvar,
        Var     rcvar
    ) {
        //int     dwidth = (in == null) ? 0 : in.length;
        // surely 'in' cannot be null? Use triggerbuffer() for that!
        int     dwidth = in.length;
        Net     empty = new Net("EMPTY");
        Net     full = new Net("FULL");
 
        if (queuereg) {
            // Add an extra output buffer register to improve clock-to-output
            // timing.
            return(sync_buffer_r(in, push, pop, c, ne, nf, count, free, r, rr, depth));
        }
        if (((this instanceof XC4V) || (this instanceof XC5V) || (this instanceof XC6V) || (this instanceof XC7)) && (depth <= 8192) && fifo) {
            // For Virtex4, Virtex5, Virtex6 or 7 series, block RAM FIFO can use inbuilt FIFO logic.
            if (rr != null) {
                if (r != null)
                    rr.connect(r);
                else
                    rr.connect(Net.LO);
            }
            ne.connect(INV(empty));
            nf.connect(INV(full));
            Net[]   out = new Net[dwidth];
            for (int i=0 ; i<dwidth ; i++)
                out[i] = new Net("out" + i + "_");
            queuefifo(depth, in, push, pop, out, empty, full, count, free, r, c, c, wcvar, rcvar);
            return(out);
        }
 
        int     awidth = bits(depth-1, false);
        Net     read = new Net("READ");
        Net     write = new Net("WRITE");
        Net     writer;
        Net[]   raddr, waddr, out;
 
        read.connect(AND(pop, INV(empty)));
        write.connect(AND(push, ne, INV(AND(empty, pop))));
        writer = AND(push, empty, INV(AND(ne, INV(pop))));
        ne.connect(FDRE(INV(AND(empty, ne, INV(push), pop)), c, OR(push, pop), r, "R"));
        nf.connect(INV(full));

        raddr = cb(awidth, false, c,  read, r, null, null);
        waddr = cb(awidth, false, c, write, r, null, null);

        int     cwidth = awidth + 1;
        Net[]   fcnt;
        Net     ed;
        Net     fd;

        fcnt = cb(awidth, false, c, XOR(read, write), r, read, null);

        ed = AND(INV(write), eqzeros(fcnt, 1, awidth-1), OR(INV(fcnt[0]), read));
        empty.connect(FDSE(ed, c, Net.HI, r, "S"));
        fd = AND(INV(read), eqones(fcnt, 1, awidth-1), OR(fcnt[0], write));
        full.connect(FDRE(fd, c, Net.HI, r, "R"));
 
        if (count != null) {
            Net[]   fcntr;
            fcntr = cb(cwidth, false, c, XOR(pop, push), r, pop, null);
            connect(count, fcntr);
        }
        if (free != null) {
            Net[]   ecntr;
            String  sinit = Integer.toHexString(1 << awidth);
            ecntr = cb(cwidth, false, c, XOR(pop, push), r, push, sinit);
            connect(free, ecntr);
        }
 
        //Net mread = OR(read, writer); the following should be faster because
        //                              of logic duplication
        Net mread = OR(AND(pop, INV(empty)), writer);
        //Net mread = LUT("I0*!I1+I2", pop, empty, writer);
        out = queuemem(depth, dwidth, awidth, in, raddr, waddr, mread, write, writer, c, c);
        if (rr != null) {
            if (r != null)
                rr.connect(r);
            else
                rr.connect(Net.LO);
        }
        return(out);
    }
    
    /**
     * Encode a deep synchronous queue buffer with extra output register
     * to improve clock-to-output timing.
     * @param   in is the data input
     * @param   push is the write-to-queue input
     * @param   pop is the pop-from-queue input
     * @param   c is the clock
     * @param   ne is the returned not empty
     * @param   nf is the returned not full
     * @param   count is the returned buffer data count or null
     * @param   free is the returned free space count or null
     * @param   r is the buffer reset
     * @param   rr is the buffer reset output
     * @param   depth is the buffer depth which is a power of 2
     *          constrained by the FPGA family
     * @return  the buffer data output
     */
    public Net[] sync_buffer_r (
        Net[]   in,
        Net     push,
        Net     pop,
        Net     c,
        Net     ne,
        Net     nf,
        Net[]   count,
        Net[]   free,
        Net     r,
        Net     rr,
        int     depth
    ) {
        int     dwidth = in.length;
        int     awidth = bits(depth-1, false);
        Net     read = new Net("READ");
        Net     w1 = new Net("W1");
        Net     w2 = new Net("W2");
        Net     w3 = new Net("W3");
        Net     s1 = new Net("S1_");
        Net     s2 = new Net("S2_");
        Net     s3 = new Net("S3_");
        Net     s4 = new Net("S4_");
        Net     fcnt_eq_0 = new Net("fcnteq0");
        Net     fcnt_eq_1 = new Net("fcnteq1");
        Net[]   raddr, waddr, fcnt, rout, out;
 
        w1.connect(LUT("I0*I1+I2", s1, push, pop));
        w2.connect(LUT("I0*I2*!I3+I1*I2*I3", s2, s3, push, pop));
        w3.connect(LUT("I0*I2*!I3+I1*I2", s3, s4, push, pop));
        

        Net s1d = LUT("I0*!I2+I1*!I2*I3", s1, s2, push, pop);
        s1.connect(FDSE(s1d, c, Net.HI, r, "S"));
        Net s2d = LUT("I0*I3+I1*(!I3*!I4+I3*I4)+I2*!I3*I4", s1, s2, s3, push, pop);
        s2.connect(FDRE(s2d, c, Net.HI, r, "R"));
        Net s3d = LUT("I0*I4*!I5+I1*(!I4*!I5+I4*I5)+I2*I3*!I4*I5",
                        s2, s3, s4, fcnt_eq_1, push, pop);
        s3.connect(FDRE(s3d, c, Net.HI, r, "R"));
        Net s4d = LUT("I0*I3*!I4+I1*!(I2*!I3*I4)",
                        s3, s4, fcnt_eq_1, push, pop);
        s4.connect(FDRE(s4d, c, null, r, "R"));
        
        Net mux = OR(s1, s2);
        
        //s1.connect(Net.get("glob.s1[0]"));
        //s2.connect(Net.get("glob.s2[0]"));
        //s3.connect(Net.get("glob.s3[0]"));
        //s4.connect(Net.get("glob.s4[0]"));

        read.connect(AND(s4, pop));
        
        Net waddr_incr = LUT("I0*I2*!I3+I1*I2", s3, s4, push, pop);
        Net raddr_incr = AND(s4, pop);
        Net fcnt_ce = LUT("I0*I2*!I3+I1*(I2*!I3+!I2*I3)", s3, s4, push, pop);
        Net fcnt_decr = LUT("I0*!I1*I2", s4, push, pop);

        waddr = cb(awidth, false, c, waddr_incr, r, null, null);
        raddr = cb(awidth, false, c, raddr_incr, r, null, null);
        fcnt = cb(awidth, false, c, fcnt_ce, r, fcnt_decr, null);
        fcnt_eq_0.connect(decode(fcnt, 0));
        fcnt_eq_1.connect(decode(fcnt, 1));

        ne.connect(INV(s1));

        long    k1 = (1 << awidth) - 2;
        long    k2 = (1 << awidth) - 3;
        Net fd = AND(INV(pop), OR(decode(fcnt, k1), AND(decode(fcnt, k2), w3)));
        nf.connect(INV(FDRE(fd, c, Net.HI, r, "R")));

        int cwidth = awidth + 1;
 
        if (count != null) {
            Net[]   fcntr;
            fcntr = cb(cwidth, false, c, XOR(pop, push), r, pop, null);
            connect(count, fcntr);
        }
        if (free != null) {
            Net[]   ecntr;
            String  sinit = Integer.toHexString(1 << awidth);
            ecntr = cb(cwidth, false, c, XOR(pop, push), r, push, sinit);
            connect(free, ecntr);
        }
 
        //Net mread = OR(read, w2); the following should be faster because
        //                              of logic duplication
        Net mread = OR(AND(s4, pop), w2);
        //Net mread = LUT("I0*I1+I2", s4, pop, w2);
        rout = queuemem(depth, dwidth, awidth, in, raddr, waddr, mread, w3, w2, c, c);
        if (rr != null) {
            if (r != null)
                rr.connect(r);
            else
                rr.connect(Net.LO);
        }
        
        out = reg(mux2(mux, rout, in), c, w1, null);
        
        return(out);
    }

    /**
     * Encode a deep asynchronous queue buffer.
     * @param   in is the data input
     * @param   push is the push-to-queue input
     * @param   pop is the pop-from-queue input
     * @param   ci is the input clock
     * @param   co is the output clock
     * @param   ne is the returned not empty
     * @param   nf is the returned not full
     * @param   rstat is the returned status synchronised to the read clock
     * @param   wstat is the returned status synchronised to the write clock
     * @param   r is a reset input synchronised to the write clock or null
     * @param   rr is a reset output synchronised to the read clock or null
     * @param   fifo true indicates that FIFO hardware should be used if
     *          the FPGA has it
     * @param   depth is the buffer depth which is a power of 2
     *          constrained by the FPGA family
     * @param   wcvar is the write clock variable
     * @param   rcvar is the read clock variable
     * @return  the buffer data output
     */
    public Net[] async_buffer (
        Net[]   in,
        Net     push,
        Net     pop,
        Net     ci,
        Net     co,
        Net     ne,
        Net     nf,
        Net[]   wstat,
        Net[]   rstat,
        Net     r,
        Net     rr,
        boolean fifo,
        int     depth,
        Var     wcvar,
        Var     rcvar
    ) {
        Net     full = new Net("FULL");
        Net     empty = new Net("EMPTY");
        int     dwidth = in.length;
 
        if (((this instanceof XC4V) || (this instanceof XC5V) || (this instanceof XC6V) || (this instanceof XC7)) && (depth <= 8192) &&
            (wstat == null) && (rstat == null) && fifo) {
            // For Virtex4, Virtex5, Virtex6 or series 7 block RAM FIFO can use inbuilt FIFO logic.
            if (rr != null) {
                if (r != null)
                    rr.connect(r);
                else
                    rr.connect(Net.LO);
            }
            ne.connect(INV(empty));
            nf.connect(INV(full));
            Net[]   out = new Net[dwidth];
            for (int i=0 ; i<dwidth ; i++)
                out[i] = new Net("out" + i + "_");
            queuefifo(depth, in, push, pop, out, empty, full, null, null, r, ci, co, wcvar, rcvar);
            return(out);
        }
 
        int     awidth = bits(depth-1, false);
        Net[]   waddr = new Net[awidth];
        Net[]   raddr = new Net[awidth];
        Net     read;
        Net     write = push;
        Net[]   out;
        Net     oack;

        if ((rr == null) && (r != null))
            rr = new Net("rr");

        nf.connect(INV(full));
 
        for (int i=0 ; i<awidth ; i++) {
            waddr[i] = new Net("WADDR" + i + "_");
            raddr[i] = new Net("RADDR" + i + "_");
        }
 
        if (oack_use_lut)
            oack = LUT("(I0*I1)+(!I0*!I2)", ne, pop, empty);
        else
            oack = OR(AND(ne, pop), AND(INV(ne), INV(empty)));

        asynch_fifo_control (
            write, ne, pop, ci, co, r,                      // input nets
            waddr, raddr, rstat, wstat, full, empty, rr,    // output nets
            awidth, depth
        );

        ne.connect(FDRE(INV(empty), co, oack, rr, "R"));
 
        read = AND(OR(INV(ne), pop), INV(empty));
        //read = LUT("(!I0+I1)*!I2", ne, pop, empty);
        out = queuemem(depth, dwidth, awidth, in, raddr, waddr, read, write, null, ci, co);
        return(out);
    }

    /**
     * Encode a queue buffer with null type.
     * @param   push    push-to-queue input
     * @param   pop     pop-from-queue input
     * @param   ci      input clock
     * @param   co      output clock
     * @param   ne      returned not empty
     * @param   nf      returned not full
     * @param   count   number of available entries
     * @param   free    number of available free spacess
     * @param   wstat   returned status synchronised to the write clock
     * @param   rstat   returned status synchronised to the read clock
     * @param   r       reset input synchronised to the write clock or null
     * @param   rr      reset output synchronised to the read clock or null
     * @param   depth   buffer depth which is a power of 2 - constrained by 
     *                  the FPGA family
     */
    public void triggerbuffer (
        Net     push,
        Net     pop,
        Net     ci,
        Net     co,
        Net     ne,
        Net     nf,
        Net[]   count,
        Net[]   free,
        Net[]   wstat,
        Net[]   rstat,
        Net     r,
        Net     rr,
        int     depth
    ) {
        int     awidth = bits(depth-1, false);
        Net     full = new Net("FULL");
        Net     empty = new Net("EMPTY");
        Net     oack;
        
        if (depth == 0) {
            if (ci != null)
                throw new ExEx("cannot have asynchronous unbuffered queue");
            queueunbuffered(null, push, pop, ne, nf);
            return;
        }

        nf.connect(INV(full));
 
        if (ci != null) {
            Net[]   waddr = new Net[awidth];
            Net[]   raddr = new Net[awidth];
            Net     write = push;

            if ((rr == null) && (r != null))
                rr = new Net("rr");

            for (int i=0 ; i<awidth ; i++) {
                waddr[i] = new Net("WADDR" + i + "_");
                raddr[i] = new Net("RADDR" + i + "_");
            }

            if (oack_use_lut)
                oack = LUT("(I0*I1)+(!I0*!I2)", ne, pop, empty);
            else
                oack = OR(AND(ne, pop), AND(INV(ne), INV(empty)));

            asynch_fifo_control (
                write, ne, pop, ci, co, r,                      // input nets
                waddr, raddr, rstat, wstat, full, empty, rr,    // output nets
                awidth, depth
            );

            ne.connect(FDRE(INV(empty), co, oack, rr, "R"));
        } else {
            int     cwidth = awidth + 1;
            Net     incr = push;
            Net     decr = pop;
            Net[]   fcnt;
            Net     ed;
            Net     fd;
 
            if (rr != null) {
                if (r != null)
                    rr.connect(r);
                else
                    rr.connect(Net.LO);
            }

            fcnt = cb(cwidth, false, co, XOR(decr, incr), r, decr, null);

            ed = AND(INV(incr), eqzeros(fcnt, 1, cwidth-1), OR(INV(fcnt[0]), decr));
            empty.connect(FDSE(ed, co, Net.HI, r, "S"));
            fd = AND(INV(decr), OR(AND(eqones(fcnt, 0, cwidth-2), incr), fcnt[cwidth-1]));
            full.connect(FDRE(fd, co, Net.HI, r, "R"));

            if (count != null)
                connect(count, fcnt);
            if (free != null) {
                Net[]   ecnt;
                String  sinit = Integer.toHexString(1 << awidth);
                ecnt = cb(cwidth, false, co, XOR(decr, incr), r, incr, sinit);
                connect(free, ecnt);
            }
            ne.connect(INV(empty));
        }
    }


    /**
     * Encode an unbuffered queue.
     * @param   in is the data input
     * @param   wpending is the write pending input
     * @param   rpending is the read pending input
     * @param   ravail is the returned read available
     * @param   wavail is the returned write available
     * @return  the data output
     */
    public Net[] queueunbuffered (
        Net[]   in,
        Net     wpending,
        Net     rpending,
        Net     ravail,
        Net     wavail
    ) {
        ravail.connect(wpending);
        wavail.connect(rpending);
        return(in);
    }
 
    /**
     * Control logic for an asynchronous FIFO.
     * Code adapted from Xilinx application note 131.
     * Parameter 'depth' is a power of 2 and is a minimum of 4.
     * @param   write is the write enable
     * @param   ne is true if the asynchronous FIFO is not empty
     * @param   pop is true to pop a datum
     * @param   wc is the write clock
     * @param   rc is the read clock
     * @param   r is a reset synchronised to the write clock or null
     * @param   waddr is the write address
     * @param   raddr is the read address
     * @param   rstat is the read buffer status
     * @param   wstat is the write buffer status
     * @param   full is the net to return the full condition
     * @param   empty is the net to return the empty condition
     * @param   rr is a reset synchronised to the read clock
     * @param   awidth is the address width
     * @param   depth is the FIFO depth
     */
    private void asynch_fifo_control (
        Net write, Net ne, Net pop, Net wc, Net rc, Net r,      // input nets
        Net[] waddr, Net[] raddr,                               // output nets
        Net[] rstat, Net[] wstat, Net full, Net empty, Net rr,  // output nets
        int awidth, int depth
    ) {
        Net[]   wg0, wg1, rg0, rg1, rg2;
        Net     emptyg, almostemptyg, fullg, almostfullg;
        Net     wr = null;  // extended write reset
        Net     writelock = null;
        Net     read;
        // Initial gray code values of pipelined read and write address registers.
        // These are address sequences leading up to the initial read and
        // write addresses of 0, hence they are gray code values of -1, -2 and -3.
        String  igm1 = intToGrayString(-1, awidth);
        String  igm2 = intToGrayString(-2, awidth);
        String  igm3 = intToGrayString(-3, awidth);
 
        if ((r != null) && r.isConnected(Net.LO))
            r = null;
        if (r != null) {
            r = AND(r, INV(FDRE(r, wc, Net.HI, Net.LO, "R")));
            rr.connect(resync(r, wc, rc));
            writelock = FDRSE(null, Net.LO, wc, Net.LO, resync(rr, rc, wc), r, "R", false);
            wr = OR(r, writelock);
        } else if (rr != null)
            rr.connect(Net.LO);

        if (as_fifo_read_use_lut)
            read = LUT("(!I0+I1)*!I2", ne, pop, empty);
        else
            read = AND(OR(INV(ne), pop), INV(empty));

        connect(raddr, cb(awidth, false, rc, read, rr, null, null));
        rg0 = reg(bin_to_gray(raddr), rc, read, rr, igm1, false, null);
        rg1 = reg(rg0, rc, read, rr, igm2, false, null);
        rg2 = reg(rg1, rc, read, rr, igm3, false, null);

        connect(waddr, cb(awidth, false, wc, write, wr, null, null));
        wg0 = reg(bin_to_gray(waddr), wc, write, wr, igm1, false, null);
        wg1 = reg(wg0, wc, write, wr, igm2, false, null);

        emptyg       = eq(wg1, rg1, false, false);
        almostemptyg = eq(wg1, rg0, false, false);
        fullg        = eq(wg1, rg2, false, false);
        almostfullg  = eq(wg0, rg2, false, false);
        //Net empty_d = OR(AND(read, almostemptyg), emptyg)
        Net empty_d;
        if (lutwidth >= 5) {
            if (as_fifo_empty_use_lut)
                empty_d = LUT("(!I0+I1)*!I2*I3+I4",
                                        ne, pop, empty, almostemptyg, emptyg);
            else
                empty_d = OR(AND(OR(INV(ne), pop), INV(empty), almostemptyg), emptyg);
        } else {
            Net n;
            if (as_fifo_empty_use_lut)
                n = LUT("(!I0+I1)*!I2*I3", ne, pop, empty, almostemptyg);
            else
                n = AND(OR(INV(ne), pop), INV(empty), almostemptyg);
            empty_d = MUXF5(emptyg, n, Net.HI);
        }
        empty.connect(FDSE(empty_d, rc, Net.HI, rr, "S"));
        if (r != null) {
            Net full_;
            full_ = FDRE(OR(fullg, AND(almostfullg, write, INV(full))), wc, Net.HI, wr, "R");
            full.connect(OR(full_, wr));
        } else
            full.connect(FDRE(OR(fullg, AND(almostfullg, write, INV(full))), wc, Net.HI, Net.LO, "R"));

        if ((rstat != null) || (wstat != null)) {
            Net[]   rgtop = new Net[2];
            Net[]   wgtop = new Net[2];
            rgtop[0] = rg1[awidth-2];
            rgtop[1] = rg1[awidth-1];
            wgtop[0] = wg1[awidth-2];
            wgtop[1] = wg1[awidth-1];
            if (rstat != null)
                asynch_fifo_control_status(rstat, rgtop, wgtop, rc, rr);
            if (wstat != null)
                asynch_fifo_control_status(wstat, rgtop, wgtop, wc, wr);
        }
    }

    private void asynch_fifo_control_status (Net[] stat, Net[] rgtop, Net[] wgtop, Net c, Net r) {
        Net     n, n1, n2, n3, n4;
        Net[]   rgt = reg(rgtop, c, Net.HI, Net.LO);
        Net[]   wgt = reg(wgtop, c, Net.HI, Net.LO);
        // Empty to 1/4 Full - quads equal
        n = AND(eq(rgt, wgt, false, false), INV(OR(stat[3], stat[4])));
        stat[0].connect(FDSE(n, c, Net.HI, r, "S"));
        // 1 word to 1/2 Full - quad 1 ahead
        n1 = AND(decode(rgt, 0), decode(wgt, 1));
        n2 = AND(decode(rgt, 1), decode(wgt, 3));
        n3 = AND(decode(rgt, 3), decode(wgt, 2));
        n4 = AND(decode(rgt, 2), decode(wgt, 0));
        n = OR(n1, n2, n3, n4);
        stat[1].connect(FDRE(n, c, Net.HI, r, "R"));
        // 1/4 full to 3/4 Full - quad 2 ahead
        n1 = AND(decode(rgt, 0), decode(wgt, 3));
        n2 = AND(decode(rgt, 1), decode(wgt, 2));
        n3 = AND(decode(rgt, 3), decode(wgt, 0));
        n4 = AND(decode(rgt, 2), decode(wgt, 1));
        n = OR(n1, n2, n3, n4);
        stat[2].connect(FDRE(n, c, Net.HI, r, "R"));
        // 1/2 Full to Full - quad 3 ahead
        n1 = AND(decode(rgt, 0), decode(wgt, 2));
        n2 = AND(decode(rgt, 1), decode(wgt, 0));
        n3 = AND(decode(rgt, 3), decode(wgt, 1));
        n4 = AND(decode(rgt, 2), decode(wgt, 3));
        n = OR(n1, n2, n3, n4);
        stat[3].connect(FDRE(n, c, Net.HI, r, "R"));
        // 3/4 Full to Full - quad 4 ahead/equal
        n = AND(eq(rgt, wgt, false, false), OR(stat[3], stat[4]));
        stat[4].connect(FDRE(n, c, Net.HI, r, "R"));
    }

    /**
     * Registered-output memory for queue buffers of {@code depth > 2}.
     * For depth {@code >=} queue_buffer_max_cram_depth, CLB RAMs with an associated
     * output register are used.
     * For larger sizes block RAMs are used.
     * @param   depth is the buffer depth
     * @param   dwidth is the data width
     * @param   awidth is the address width
     * @param   in is the data input
     * @param   raddr is the read address
     * @param   waddr is the write address
     * @param   read is the read enable
     * @param   write is the write enable
     * @param   writer is a write enable for the read port or null
     * @param   ci is the write clock
     * @param   co is the read clock
     * @return  the output net array
     */
    private Net[] queuemem (
        int     depth,
        int     dwidth,
        int     awidth,
        Net[]   in,
        Net[]   raddr,
        Net[]   waddr,
        Net     read,
        Net     write,
        Net     writer,
        Net     ci,
        Net     co
    ) {
        Net[]   out;
        boolean is_sync = (ci == co);
 
        if (depth <= queue_buffer_max_cram_depth) {
            // use CLB RAM
            Net[] o = new Net[dwidth];
            for (int i=0 ; i<dwidth ; i++)
                o[i] = new Net("o" + i + "_");
            sram2(  dwidth,
                      ci, write, waddr,   in, null,
                    null,  null, raddr, null,    o,
                    null );
            if (is_sync)
                out = reg(mux2(writer, o, in), co, read, null);
            else
                out = reg(o, co, read, null);
            return(out);
        }

        // use block RAM
        out = new Net[dwidth];
        for (int i=0 ; i<dwidth ; i++)
            out[i] = new Net("out" + i + "_");
        Net[]   rin = is_sync ? in : null;
        rram2 ( dwidth, awidth,
                write,  write, ci, waddr,  in, null,
                read, writer, co, raddr, rin,  out,
                null, null, null, new ArrayList<String>(), false );
        return(out);
    }
 
    /**
     * Queue buffer using Virtex FIFO element.
     * @param   depth is the buffer depth
     * @param   in is the data input
     * @param   push is true to push a datum
     * @param   pop is true to pop a datum
     * @param   out is the data output
     * @param   empty is true if the FIFO is empty
     * @param   full is true if the FIFO is full
     * @param   count is the number of contained data
     * @param   free is the number of free spaces
     * @param   r is a reset signal or null
     * @param   cw is the write clock
     * @param   cr is the read clock
     * @param   wcvar is the write clock variable
     * @param   rcvar is the read clock variable
     */
    private void queuefifo(
        int     depth,
        Net[]   in,
        Net     push,
        Net     pop,
        Net[]   out,
        Net     empty,
        Net     full,
        Net[]   count,
        Net[]   free,
        Net     r,
        Net     cw,
        Net     cr,
        Var     wcvar,
        Var     rcvar
    ) {
        int     dwidth = in.length;
        //int     awidth = bits(depth-1, false);
        Net[]   rdcount = null;
        Net[]   wrcount = null;
 
        // Net 'r3' is the FIFO reset. This must be high for at least 3 clock
        // cycles of both the read and the write clock. A reset must be done
        // after power-up so is done after configuration. 'rcycles' is the
        // number of cycles of the write clock 'cw' necessary for at least 3
        // cycles of both clocks. The reset pulse is generated using one or
        // more CLB shift registers. The initial condition is 01...1, so the
        // output is initially 1. The output is connected to the CE and the
        // data input so the contents will shift until the 0 is at the output
        // at which point it will stop. The external reset 'r' is ORed in to
        // the CE to start the shift sequence of 'rcycles' 1s and a 0 each
        // time.
        Net                 r3 = new Net();
        double              minfreq = Math.min(wcvar.getClkFreq(null), rcvar.getClkFreq(null));
        double              wcfreq = wcvar.getClkFreq(null);
        int                 rcycles = (int)(Math.ceil(wcfreq / minfreq)) * 4;
        Net[]               srl16in = new Net[1];
        Net[]               srl16out = null;
        String[]   srl16init = new String[rcycles];
        srl16in[0] = r3;
 
        for (int i=0 ; i<rcycles ; i++)
            srl16init[i] = "1";
        srl16init[rcycles] = "0";

        if (r != null)
            srl16out = del(rcycles+1, srl16in, null, cw, OR(r, r3), null, srl16init);
        else
            srl16out = del(rcycles+1, srl16in, null, cw, r3, null, srl16init);
        srl16out[0].connect(r3);
 

        fifoallocate(
            depth, dwidth,
            push, pop, in, out, empty, full, rdcount, wrcount, r3, cw, cr
        );

        // count and free only used for synchronous queues
        if (count != null) {
            Net[] xx = sub(wrcount, rdcount, false, false, 0);
            for (int i=0 ; i<(count.length-1) ; i++)
                count[i].connect(xx[i]);
            count[count.length-1].connect(full);
        }
        if (free != null) {
            Net[] yy = sub(rdcount, wrcount, false, false, 0);
            for (int i=0 ; i<(free.length-1) ; i++)
                free[i].connect(yy[i]);
            free[free.length-1].connect(full);
        }
    }
 
    /**
     * Convert a weighted binary to gray code.
     * @param   in is the binary input net array
     * @return  the gray code net array
     */
    private Net[] bin_to_gray (Net[] in) {
        int     size = in.length;
        Net[]   out = new Net[size];

        out[size-1] = in[size-1];
        for (int i=size-2 ; i>=0 ; i--)
            out[i] = XOR(in[i+1], in[i]);
        return(out);
    }

    /**
     * Determine if part of a net array is all zeros.
     * @param   a is the net array
     * @param   l is the lower subscript of the range to be tested
     * @param   u is the upper subscript of the range to be tested
     * @return  true if the subrange is all zeros
     */
    private Net eqzeros (Net[] a, int l, int u) {
        ArrayList<Net>   al = new ArrayList<Net>();
        for (int i=l ; i<=u ; i++)
            al.add(INV(a[i]));
        return(AND(al));
    }

    /**
     * Determine if part of a net array is all ones.
     * @param   a is the net array
     * @param   l is the lower subscript of the range to be tested
     * @param   u is the upper subscript of the range to be tested
     * @return  true if the subrange is all ones
     */
    private Net eqones (Net[] a, int l, int u) {
        ArrayList<Net>   al = new ArrayList<Net>();
        for (int i=l ; i<=u ; i++)
            al.add(a[i]);
        return(AND(al));
    }
 
    /**
     * Encode a delay line.
     * If n==1 uses a flip-flop. If r is not null must use flip-flops as a
     * shift register cannot be reset. If len is not null must use a shift
     * register. Cannot use both r and len!
     * @param   n is the line length
     * @param   in is the input net array
     * @param   len is an optional variable length selection input
     * @param   c is the clock
     * @param   ce is an optional clock enable
     * @param   r is an optional reset
     * @param   init is the initial delay line contents as an array of
     *          hexadecimal strings
     * @return  the output net array
     */
    public Net[] del (
        int         n,
        Net[]       in,
        Net[]       len,
        Net         c,
        Net         ce,
        Net         r,
        String[]    init
    ) {
        if ((n == 0) && (len == null))
            return(in);

        int             blklen = 1 << srladdrwidth;
        int             width = in.length;
        Net[]           out = new Net[width];
        boolean         flip_flops = (n == 1) || ((r != null) && (!r.isConnected(Net.LO))) || (srladdrwidth == 0);
        boolean         varlen = (len != null);
        StringBuffer    binit = null;
        int             del_len;
        int             initlen = (init == null) ? 0 : init.length;
        String[]        tinit = null;
        
        if ((r != null) && (!r.isConnected(Net.LO)))    // if reset used use flip-flops
            flip_flops = true;
        if (srladdrwidth == 0)                          // if FPGA has no shift registers use flip-flops
            flip_flops = true;
        if ((n <= 3) && !varlen)                        // for small fixed delay use flip-flops
            flip_flops = true;
 
        if (varlen)
            n = blklen;

        if (initlen != 0) {
            // Reverse order of initialisation so lowest subscript appears at output first
            tinit = new String[n];
            int j = 0;
            for (int i=n-1 ; i>=0 ; i--)
                if (j < initlen)
                    tinit[i] = init[j++];
                else
                    tinit[i] = "0";
        }

        for (int k=0 ; k<width ; k++) {
            // if delay of < 3 and not variable length, use flip-flops
            // if reset input is used, must use flip-flops
            if (flip_flops) {
                Net[]   s = new Net[n+1];
                s[0] = in[k];
                for (int i=0 ; i<n ; i++)
                    if ((tinit == null) || strhexbit(tinit[i], k) == 0)
                        s[i+1] = FDRE(s[i], c, ce, r, "R");
                    else
                        s[i+1] = FDSE(s[i], c, ce, r, "S");
                out[k] = s[n];
                continue;
            }

            // use delay lines
            String sinit;
            if (tinit != null) {
                binit = new StringBuffer();
                for (int i=0 ; i<n ; i++) {
                    int b = strhexbit(tinit[i], k);
                    binit.insert(0, (b == 0) ? "0" : "1");
                }
                sinit = binToHex(binit);
            } else
                sinit = "0";
            if (varlen) {
                // variable length
                switch (srladdrwidth) {
                case 4:
                    out[k] = SRL16E(in[k], c, ce, len, sinit);
                    break;
                case 5:
                    out[k] = SRL32E(in[k], c, ce, len, sinit, false);
                }
            } else {
                // Constant length.
                // Insert a flip-flop at the output to improve timing,
                // so use n-1 as length.
                int     blocks = (n - 2) / blklen + 1;
                Net[]   s = new Net[blocks+1];
                boolean q31;
                int     ffinit = 0;
                if ((binit != null) && (binit.charAt(binit.length() - n) == '1')) {
                     // remove 1 bit - used with flip-flop
                    ffinit = 1;
                    binit.deleteCharAt(binit.length() - n);
                }
                len = new Net[srladdrwidth];
                s[0] = in[k];
                for (int i=0, count=n-1 ; i<blocks ; i++, count-=blklen) {
                    int m = (count < blklen) ? count-1 : blklen-1;
                    for (int j=0 ; j<srladdrwidth ; j++)
                        len[j] = (((m >> j) & 1) == 1) ? Net.HI : Net.LO;
                    if (binit != null)
                        sinit = binToHex(binit);
                    switch (srladdrwidth) {
                    case 4:
                        s[i+1] = SRL16E(s[i], c, ce, len, stringsize(sinit, srladdrwidth));
                        break;
                    case 5:
                        q31 = (i != (blocks - 1));
                        s[i+1] = SRL32E(s[i], c, ce, len, stringsize(sinit, srladdrwidth), q31);
                    }
                    del_len = 1 << srladdrwidth;
                    if (binit != null) {
                        if (binit.length() >= del_len)
                            binit.delete(binit.length() - del_len, binit.length());
                        else
                            binit.delete(0, binit.length());
                    }
                }
 
                if ((tinit == null) || ffinit == 0)
                    out[k] = FDRE(s[blocks], c, ce, r, "R");
                else
                    out[k] = FDSE(s[blocks], c, ce, r, "S");
            }
        }
        return(out);
    }
 
    /**
     * Encode a constant left shifter.
     * @param   val is the value to be left shifter
     * @param   shift is the shift count
     * @param   signed is true if the value is signed
     * @param   len is the required size of the output net array
     * @return  the output net array
     */
    public Net[] lshift (Net[] val, int shift, boolean signed, int len) {
        Net[]   res = netArray(len);
 
        for (int i=0 ; i<len ; i++) {
            if ((i-shift) < 0)
                res[i].connect(Net.LO);
            else if ((i-shift) >= val.length) {
                if (signed)
                    res[i].connect(val[val.length-1]);
                else
                    res[i].connect(Net.LO);
            } else
                res[i].connect(val[i-shift]);
        }
        return(res);
    }

    /**
     * Encode a constant right shifter.
     * @param   val     value to be right shifted
     * @param   shift   shift count
     * @param   len     required size of the output net array
     * @param   signed  true if the operand to be shifted is signed
     * @return  the output net array
     */
    public Net[] rshift (Net[] val, int shift, boolean signed, int len) {
        Net[]   res = netArray(len);

        for (int i=0 ; i<len ; i++) {
            if ((i+shift) < 0)
                res[i].connect(Net.LO);
            else if ((i+shift) >= val.length) {
                if (signed)
                    res[i].connect(val[val.length-1]);
                else
                    res[i].connect(Net.LO);
            } else
                res[i].connect(val[i+shift]);
        }
        return(res);
    }

    /**
     * Encode a constant left rotator.
     * @param   val is the value to be left rotated
     * @param   shift is the shift count
     * @param   len is the required size of the output net array
     * @return  the output net array
     */
    public Net[] lrot (Net[] val, int shift, int len) {
        Net[]   res = netArray(len);
        int     j = 0;
        
        for (int i=0 ; i<len ; i++) {
            j = i - shift;
            if (j < 0)
                j = j + len;
            res[i].connect(val[j]);
        }
        return(res);
    }

    /**
     * Encode a constant right rotator.
     * @param   val is the value to be right rotated
     * @param   shift is the shift count
     * @param   len is the required size of the output net array
     * @return  the output net array
     */
    public Net[] rrot (Net[] val, int shift, int len) {
        Net[]   res = netArray(len);
        int     j = 0;
        
        for (int i=0 ; i<len ; i++) {
            j = i + shift;
            if (j > (len -1))
                j = j - len;
            res[i].connect(val[j]);
        }
        return(res);
    }
 
 
    /**
     * Encode a variable left shifter.
     * This uses log2(n) stages where n is the number of bits in the shift value.
     * Each stage left shifts a power of 2 bits. A 2-dimensional array v[][] is used,
     * the first column being the input, intervening columns successive shifts by increasing
     * powers of 2 and the last column is the shifted result.
     * @param   a is the input argument - signed or unsigned
     * @param   s is the shift argument - unsigned
     * @param   owidth is the output bit width
     * @param   signeda is true if the input is signed
     * @return  the output net array
     */
    public Net[] var_shift_l (
        Net[]   a,
        Net[]   s,
        int     owidth,
        boolean signeda
    ) {
        int     iwidth = a.length;                  // input width
        int     shiftmax = (1 << s.length) - 1;     // maximum shift distance
        long    ssize = Math.min(shiftmax, owidth); // truncate shift max if output width smaller than shift max
        int     shiftdim = bits(ssize, false);      // resulting size of shift array
        Net[]   shift = new Net[shiftdim];          // shift value array
        int     stages = shiftdim;                  // n number of shift stages, each a power of 2
        Net[][] v = new Net[stages+1][owidth];      // shift array

        // Prune the shift array to match the output width if too big.
        shift = subarray(s, 0, shiftdim-1);
        
        // Assign the input array to the first column of v[][], filling
        // the most significant bits with 0 or sign bit as appropriate.
        for (int i=0 ; i<iwidth ; i++)
            v[0][i] = a[i];
        for (int i=iwidth ; i<owidth ; i++)
            v[0][i] = signeda ? a[iwidth-1] : Net.LO;

        // Create the shift logic.
        for (int j=1 ; j<=stages ; j++) {
            int down = (1 << (j-1));
            for (int i=0 ; i<owidth ; i++) {
                int l = i - down;
                v[j][i] = mux2(shift[j-1], v[j-1][i],  l<0 ? Net.LO : v[j-1][l]);
            }
        }

        // Return the last column of v[][] as the shifted value.
        return(v[stages]);
    }
    
    /**
     * Encode a variable right shifter.
     * This uses log2(n) stages where n is the number of bits in the shift value.
     * Each stage left shifts a power of 2 bits. A 2-dimensional array v[][] is used,
     * the first column being the input, intervening columns successive shifts by increasing
     * powers of 2 and the last column is the shifted result.
     * @param   a is the input argument - signed or unsigned
     * @param   s is the shift argument - unsigned
     * @param   owidth is the output bit width
     * @param   signeda is true if the input is signed
     * @return  the output net array
     */
    public Net[] var_shift_r (
        Net[]   a,
        Net[]   s,
        int     owidth,
        boolean signeda
    ) {
        int     iwidth = a.length;                  // input width
        int     shiftmax = (1 << s.length) - 1;     // maximum shift distance
        long    ssize = Math.min(shiftmax, owidth); // truncate shift max if output width smaller than shift max
        int     shiftdim = bits(ssize, false);      // resulting size of shift array
        Net[]   shift = new Net[shiftdim];          // shift value array
        int     stages = shiftdim;                  // n number of shift stages, each a power of 2
        Net[][] v = new Net[stages+1][owidth];      // shift array
        Net     msboff = signeda ? a[iwidth-1] : Net.LO;    // bit off MSB end - sign or 0

        // prune the shift array to match the output width if too big
        shift = subarray(s, 0, shiftdim-1);
        
        // Assign the input array to the first column of v[][], filling
        // the most significant bits with 0 or sign bit as appropriate.
        for (int i=0 ; i<owidth ; i++) {
            if (i < iwidth)
                v[0][i] = a[i];
            else if (signeda)
                v[0][i] = a[iwidth-1];
            else
                v[0][i] = Net.LO;
        }

        for (int j=1 ; j<=stages ; j++) {
            int up = (1 << (j-1));
            for (int i=0 ; i<owidth ; i++) {
                int l = i + up;
                v[j][i] = mux2(shift[j-1], v[j-1][i],  l>=owidth ? msboff : v[j-1][l]);
            }
        }

        // Return the last column of v[][] as the shifted value.
        return(v[stages]);
    }

    /**
     * Encode an adder. Input nets may be different sizes.
     * If one operand is signed and the other is not, the unsigned operand
     * is extended by one most significant bit (zero) and treated as signed.
     * If the output size is not explicitly given (osize == 0) then
     * the output size will be the size of the widest operand plus 1.
     * @param   a is the 1st input operand net array
     * @param   b is the 2nd input operand net array
     * @param   asigned is true if the 1st operand is signed
     * @param   bsigned is true if the 2nd operand is signed
     * @param   osize is the output width or 0
     * @return  the output net array
     */
    public Net[] add (Net[] a, Net[] b, boolean asigned, boolean bsigned, int osize) {
        int     awid = a.length + ((!asigned && bsigned) ? 1 : 0);
        int     bwid = b.length + ((asigned && !bsigned) ? 1 : 0);
        int     isize = Math.max(awid, bwid);
        int     psize = isize + 1;
        
        if (osize == 0)
            osize = psize;
        
        Net[]   ps = new Net[psize];    // partial sum
        Net[]   s = new Net[psize];     // sum
        Net[]   c = new Net[psize];     // carries

        a = sig_expand(a, psize, asigned);
        b = sig_expand(b, psize, bsigned);
 
        int i = 0;

        // Starting at the LSB, while either operand is GND simply copy
        // the other operand to the output. When neither is GND, exit
        // the loop and proceed to normal addition logic for the rest of the
        // bits.
        for ( ; i<psize ; i++) {
            if (a[i].isConnected(Net.LO))
                s[i] = b[i];
            else if (b[i].isConnected(Net.LO))
                s[i] = a[i];
            else
                break;
        }
        if (i < psize)
            c[i] = Net.LO;  // start carry chain if adder still needed
 
        // Adder stages (if required; i < size).
        int ilast = psize - 1;
        if (!needCPLDarith()) {
            // FPGAs
            for ( ; i<psize ; i++) {
                if (add_sub_use_lut)
                    ps[i] = LUT("I0^I1", a[i], b[i]);
                else
                    ps[i] = XOR(a[i], b[i]);
                s[i] = XORCY(ps[i], c[i]);
                if (i != ilast)
                    c[i+1] = MUXCY(ps[i], a[i], c[i]);
            }
        } else {
            // CPLDs
            for ( ; i<psize ; i++) {
                if (cpld_use_lut)
                    s[i] = LUT("I0^I1^I2", a[i], b[i], c[i]);
                else
                    s[i] = OR(  AND(    a[i],  INV(b[i]), INV(c[i])),
                                AND(INV(a[i]),     b[i],  INV(c[i])),
                                AND(INV(a[i]), INV(b[i]),     c[i]),
                                AND(    a[i],      b[i],      c[i])   );
                if (i != ilast) {
                    if (cpld_use_lut)
                        c[i+1] = LUT("I0*I1*!I2+I0*!I1*I2+!I0*I1*I2+I0*I1*I2",
                                                            a[i], b[i], c[i]);
                    else
                        c[i+1] = OR(    AND(    a[i],      b[i], INV(c[i])),
                                        AND(    a[i],  INV(b[i]),    c[i]),
                                        AND(INV(a[i]),     b[i],     c[i]),
                                        AND(    a[i],      b[i],     c[i])  );
                }
            }
        }
        
        if (osize < psize)
            s = subarray(s, 0, osize-1);
        else
            s = sig_expand(s, osize, asigned || bsigned);
        return(s);
    }
 
    /**
     * Encode an inverter.
     * The output net is the same size as the input net.
     * @param   a is the input operand net array
     * @return  the output net array
     */
    public Net[] inv (Net[] a) {
            int     size = a.length;
            Net[]   o = new Net[size];

        for (int i=0 ; i<size ; i++)
            o[i] = INV(a[i]);
        return(o);
    }
 
    /**
     * Encode an incrementer.
     * The output net array is the same size as the input net array.
     * @param   a is the input operand net array
     * @param   enable is an optional enable (may be null)
     * @return  the output net array
     */
    public Net[] incr (Net[] a, Net enable) {
        if ((enable != null) && enable.isConnected(Net.LO))
            return(a);
        int     size = a.length;
        Net[]   is = new Net[size]; // internal sum
        Net[]   s = new Net[size];  // sum
        Net[]   c = new Net[size];  // carries

        if (enable != null)
            c[0] = enable;
        else
            c[0] = Net.HI;

        int ilast = size - 1;
        if (!needCPLDarith()) {
            // FPGAs
            for (int i=0 ; i<size ; i++) {
                is[i] = a[i];
                if (i != ilast)
                    c[i+1] = MUXCY(is[i], a[i], c[i]);
                s[i] = XORCY(is[i], c[i]);
            }
        } else {
            // CPLDs
            c[0] = Net.HI;
            for (int i=0 ; i<size ; i++) {
                s[i]   = XOR(a[i], c[i]);
                if (i != ilast)
                    c[i+1] = AND(a[i], c[i]);
            }
        }
        return(s);
    }

    /**
     * Encode a subtracter. Input nets may be different sizes.
     * If one operand is signed and the other is not, the unsigned operand
     * is extended by one most significant bit (zero) and treated as signed.
     * If the output size is not explicitly given (osize == 0) then
     * the output size will be the size of the widest operand plus 1.
     * @param   a is the 1st input operand net array
     * @param   b is the 2nd input operand net array
     * @param   asigned is true if operand a is signed
     * @param   bsigned is true if operand b is signed
     * @param   osize is the output width or 0
     * @return  the output net array
     */
    public Net[] sub (Net[] a, Net[] b, boolean asigned, boolean bsigned, int osize) {
        int     awid = a.length + ((!asigned && bsigned) ? 1 : 0);
        int     bwid = b.length + ((asigned && !bsigned) ? 1 : 0);
        int     isize = Math.max(awid, bwid);
        int     psize = isize + 1;
        
        if (osize == 0)
            osize = psize;
        
        Net[]   pd = new Net[psize]; // partial difference
        Net[]   d = new Net[psize];  // difference
        Net[]   c = new Net[psize];  // carries

        a = sig_expand(a, psize, asigned);
        b = sig_expand(b, psize, bsigned);
 
        int i = 0;

        // Starting at the LSB, while 2nd operand is GND simply copy
        // the 1st operand to the output. When 2nd operand is no
        // longer GND, exit the loop and proceed to normal subtraction
        // logic for the rest of the bits. 2nd operand should never be
        // all zeros as the subtraction would have been optimised out
        // prior to this.
        for ( ; i<psize ; i++) {
            if (b[i].isConnected(Net.LO))
                d[i] = a[i];
            else
                break;
        }
        c[i] = Net.HI;

        int ilast = psize - 1;
        if (!needCPLDarith()) {
            // FPGAs
            for ( ; i<psize ; i++) {
                if (add_sub_use_lut)
                    pd[i] = LUT("I0^!I1", a[i], b[i]);
                else
                    pd[i] = XOR(a[i], INV(b[i]));
                d[i] = XORCY(pd[i], c[i]);
                if (i != ilast)
                    c[i+1] = MUXCY(pd[i], a[i], c[i]);
            }
        } else {
            // CPLDs
            for ( ; i<psize ; i++) {
                if (cpld_use_lut)
                    d[i] = LUT("I0^!I1^I2", a[i], b[i], c[i]);
                else
                    d[i] = OR(  AND(    a[i],      b[i],   INV(c[i])),
                                AND(INV(a[i]), INV(b[i]),  INV(c[i])),
                                AND(INV(a[i]),     b[i],       c[i]),
                                AND(    a[i],  INV(b[i]),      c[i])   );
                if (i != ilast)
                    if (cpld_use_lut)
                        c[i+1] = LUT("I0*!I1*!I2+!I0*!I1*I2+I0*I1*I2+I0*!I1*I2",
                                                            a[i], b[i], c[i]);
                    else
                        c[i+1] = OR(    AND(    a[i],  INV(b[i]), INV(c[i])),
                                        AND(    a[i],      b[i],      c[i]),
                                        AND(INV(a[i]), INV(b[i]),     c[i]),
                                        AND(    a[i],  INV(b[i]),     c[i])  );
            }
        }
        
        if (osize < psize)
            d = subarray(d, 0, osize-1);
        else
            d = sig_expand(d, osize, asigned || bsigned);
        return(d);
    }
 
    /**
     * Encode a decrementer.
     * The output net array is the same size as the input net array.
     * @param   a is the input operand net array
     * @param   enable is an optional enable (may be null)
     * @return  the output net array
     */
    public Net[] decr (Net[] a, Net enable) {
        if ((enable != null) && enable.isConnected(Net.LO))
            return(a);
        int     size = a.length;
        Net[]   id = new Net[size]; // internal difference
        Net[]   d = new Net[size];  // difference
        Net[]   c = new Net[size];  // carries

        if (enable != null)
            c[0] = INV(enable);
        else
            c[0] = Net.HI;

        int ilast = size - 1;
        if (!needCPLDarith()) {
            for (int i=0 ; i<size ; i++) {
                if (enable != null)
                    id[i] = XOR(a[i], enable);
                else
                    id[i] = INV(a[i]);
                d[i] = XORCY(id[i], c[i]);
                if (i != ilast)
                    c[i+1] = MUXCY(id[i], a[i], c[i]);
            }
        } else {
            for (int i=0 ; i<size ; i++) {
                if (enable != null)
                    d[i] = OR(  AND(INV(c[i]),     a[i],      enable ),
                                AND(    c[i],  INV(a[i]),     enable ),
                                AND(               a[i],  INV(enable))  );
                else
                    d[i] = OR(  AND(INV(c[i]),     a[i]),
                                AND(    c[i],  INV(a[i]))  );
                if (i != ilast) {
                    if (enable != null)
                        c[i+1] = AND(c[i], INV(a[i]), enable);
                    else
                        c[i+1] = AND(c[i], INV(a[i]));
                }
            }
        }
        return(d);
    }
 
    /**
     * Encode an adder/subtracter. Input nets may be different sizes.
     * The output net array is one larger than the largest
     * input net array.
     * @param   a is the 1st input operand net array
     * @param   b is the 2nd input operand net array
     * @param   add is an input net which selects addition (high) or subtraction
     * @param   igb is high for add/subtract, low for output = a
     * @param   xci is XORed with the carry input
     * @param   asigned is true if operand a is signed
     * @param   bsigned is true if operand b is signed
     * @return  the output net array
     */
    public Net[] addsub (Net[] a, Net[] b, Net add, Net igb, Net xci, boolean asigned, boolean bsigned) {
        if (needCPLDarith())
                throw new ExEx("CPLD CODE ERROR - addsub() used");

        int     awid = a.length + ((!asigned && bsigned) ? 1 : 0);
        int     bwid = b.length + ((asigned && !bsigned) ? 1 : 0);
        int     size = Math.max(awid, bwid) + 1;
        Net[]   di = new Net[size];
        Net[]   psd = new Net[size]; // partial sum/difference
        Net[]   sd = new Net[size];  // sum/difference
        Net[]   c = new Net[size];   // carries

        a = sig_expand(a, size, asigned);
        b = sig_expand(b, size, bsigned);
        if (xci.isConnected(Net.LO))
            c[0] = AND(INV(add), igb);
        else
            c[0] = AND(XOR(INV(add), xci), igb);
        for (int i=0 ; i<size ; i++) {
            if (igb.isConnected(Net.LO))
                psd[i] = a[i];
            else if (igb.isConnected(Net.HI)) {
                if (add.isConnected(Net.LO))
                    psd[i] = XOR(a[i], INV(b[i]));
                else if (add.isConnected(Net.HI))
                    psd[i] = XOR(a[i], b[i]);
                else
                    psd[i] = OR(AND(add, XOR(a[i], b[i])), AND(INV(add), XOR(a[i], INV(b[i]))));
            } else {
                if (add_sub_use_lut)
                    psd[i] = LUT("(!I0*I1)+(I0*(I3*(I1^I2)+!I3*(I1^!I2)))", igb, a[i], b[i], add);
                else
                    psd[i] = OR(    AND(INV(igb), a[i]),
                                    AND(igb, OR(    AND(add, XOR(a[i], b[i])),
                                                    AND(INV(add), XOR(a[i], INV(b[i]))))));
            }
            if (igb.isConnected(Net.LO))
                di[i] = Net.LO;
            else if (igb.isConnected(Net.HI))
                di[i] = a[i];
            else
                di[i] = MULT_AND(a[i], igb);
            if (i != (size-1))
                c[i+1] = MUXCY(psd[i], di[i], c[i]);
            sd[i] = XORCY(psd[i], c[i]);
        }
        return(sd);
    }
 
    /**
     * Encode a static variable ALU. Input nets may be different sizes.
     * The output net array is one larger than the largest
     * input net array.
     * @param   a is the 1st input operand net array
     * @param   b is the 2nd input operand net array
     * @param   add is an input net which selects addition (high) or subtraction
     * @param   iga is high for add/subtract, low for output = b
     * @param   asigned is true if operand a is signed
     * @param   bsigned is true if operand b is signed
     * @param   dsp is true if the output connects to a static with DSP attribute
     * @return  the output net array
     */
    public Net[] alu (Net[] a, Net[] b, Net add, Net iga, boolean asigned, boolean bsigned, boolean dsp) {
        if (needCPLDarith())
                throw new ExEx("CPLD CODE ERROR - ALU used");

        int     awid = a.length + ((!asigned && bsigned) ? 1 : 0);
        int     bwid = b.length + ((asigned && !bsigned) ? 1 : 0);
        int     size = Math.max(awid, bwid) + 1;
        Net[]   sd = null; // sum:difference

        a = sig_expand(a, size, asigned);
        b = sig_expand(b, size, bsigned);
 
        Net[]   di = new Net[size];
        Net[]   is = new Net[size]; // internal sum/difference
        Net[]   c  = new Net[size]; // carries

        sd = new Net[size];
        c[0] = AND(INV(add), iga);
        for (int i=0 ; i<size ; i++) {
            if (iga.isConnected(Net.LO))
                is[i] = b[i];
            else if (iga.isConnected(Net.HI)) {
                if (add.isConnected(Net.LO))
                    is[i] = XOR(a[i], INV(b[i]));
                else if (add.isConnected(Net.HI))
                    is[i] = XOR(a[i], b[i]);
                else
                    is[i] = OR( AND(add, XOR(a[i], b[i])),
                                AND(INV(add), XOR(a[i], INV(b[i]))) );
            } else {
                if (add_sub_use_lut)
                    is[i] = LUT("(!I0*I2)+(I0*(I3*(I1^I2)+!I3*(I1^!I2)))",
                                                        iga, a[i], b[i], add);
                else
                    is[i] = OR(
                                AND(INV(iga), b[i]),
                                AND(
                                        iga,
                                        OR(
                                            AND( add, XOR(a[i], b[i])),
                                            AND( INV(add), XOR(a[i], INV(b[i])))
                                        )
                                )
                            );
            }
            if (iga.isConnected(Net.LO))
                di[i] = Net.LO;
            else if (iga.isConnected(Net.HI))
                di[i] = a[i];
            else
                di[i] = MULT_AND(a[i], iga);
            if (i != (size-1))
                c[i+1] = MUXCY(is[i], di[i], c[i]);
            sd[i] = XORCY(is[i], c[i]);
        }
        return(sd);
    }
 
    /**
     * Encode an incrementer/decrementer.
     * The output net array is the same size as the input net array.
     * @param   a is the input operand net array
     * @param   decr is an input net which selects increment (low) or
     *          decrement (high)
     * @return  the output net array
     */
    public Net[] incrdecr (Net[] a, Net decr) {
        if (needCPLDarith())
            throw new ExEx("CPLD CODE ERROR - incrdecr() used");

        int     size = a.length;
        Net[]   pr = new Net[size]; // partial result
        Net[]   r = new Net[size];  // result
        Net[]   c = new Net[size];  // carries
        int     ilast = size - 1;

        c[0] = INV(decr);
        for (int i=0 ; i<size ; i++) {
            pr[i] = XOR(a[i], decr);
            r[i] = XORCY(pr[i], c[i]);
            if (i != ilast)
                c[i+1] = MUXCY(pr[i], a[i], c[i]);
        }
        return(r);
    }
 
    /**
     * Encode a multiplier. Input nets may be different sizes, in which case
     * input a must be the larger.
     * The width of the output net is the sum of the width of the input nets.
     * @param   a is the 1st input operand net array
     * @param   b is the 2nd input operand net array
     * @param   asigned is true if the 1st input operand is signed
     * @param   bsigned is true if the 2nd input operand is signed
     * @return  the output net
     */
    public Net[] mul (Net[] a, Net[] b, boolean asigned, boolean bsigned) {
        int     na = a.length;
        int     nb = b.length;
        
        switch (family.MultWidth1()) {
        case 0:
            break;
        case 35:
            // 18x18 multiplier hardware - XC3S, XC2V, XC2VP, XC4V
            if ((asigned && (na <= 18)) || (!asigned && (na <= 17))) {
                // Only need a single 18x18 signed multiplier.
                Net[]   p = netArray(na + nb);
                a = sig_expand(a, 18, asigned);
                b = sig_expand(b, 18, bsigned);
                ((XTDECode)family).Mult(a, b, p);
                return(p);
            }
            if ((bsigned && (nb <= 18)) || (!bsigned && (nb <= 17))) {
                // 35x18 multiplier
                // Need two 18x18 signed multipliers and an adder.
                Net[]   p = netArray(na + nb);
                Net[]   aupper = sig_expand(subarray(a, na-18, na-1), 18, asigned);
                Net[]   alower = sig_expand(subarray(a, 0, na-19), 18, false);
                Net[]   c = netArray(nb+18);
                Net[]   d = netArray(na+nb-18);
                Net[]   e = netArray(nb+18);

                b = sig_expand(b, 18, bsigned);
                ((XTDECode)family).Mult(aupper, b, c);
                ((XTDECode)family).Mult(alower, b, d);
                e = add(c, subarray(d, na-18, na+nb-19), asigned||bsigned, bsigned, 0);
                connect(subarray(p, 0, na-19), subarray(d, 0, na-19));
                connect(subarray(p, na-18, na+nb-1), subarray(e, 0, nb+18-1));
                return(p);
            } else {
                // 35x35 multiplier
                // Need four 18x18 signed multipliers and two adders.
                Net[]   a1 = sig_expand(subarray(a, 0, 16), 18, false);
                Net[]   a2 = sig_expand(subarray(a, 17, na-1), 18, asigned);
                Net[]   a3 = sig_expand(subarray(a, 0, 16), 18, false);
                Net[]   a4 = sig_expand(subarray(a, 17, na-1), 18, asigned);
                Net[]   b1 = sig_expand(subarray(b, 17, nb-1), 18, bsigned);
                Net[]   b2 = sig_expand(subarray(b, 0, 16), 18, false);
                Net[]   b3 = sig_expand(subarray(b, 0, 16), 18, false);
                Net[]   b4 = sig_expand(subarray(b, 17, nb-1), 18, bsigned);
                Net[]   p1 = netArray(36);
                Net[]   p2 = netArray(36);
                Net[]   p3 = netArray(36);
                Net[]   p4 = netArray(36);
                Net[]   s = new Net[37];
                Net[]   aa1 = netArray(53);
                Net[]   aa2 = netArray(70);
                Net[]   p;

                for (int i=0 ; i<17 ; i++)
                    aa1[i] = Net.LO;

                ((XTDECode)family).Mult(a1, b1, p1);
                ((XTDECode)family).Mult(a2, b2, p2);
                ((XTDECode)family).Mult(a3, b3, p3);
                ((XTDECode)family).Mult(a4, b4, p4);
                s = add(p1, p2, false, false, 0);
                connect(subarray(aa1, 17, 52), subarray(s, 0, 35));
                connect(subarray(aa2, 0, 33), subarray(p3, 0, 33));
                connect(subarray(aa2, 34, 69), subarray(p4, 0, 35));
                p = add(aa1, aa2, true, true, 0);
                return(p);
            }
        case 25:
            // 25x18 multiplier hardware - XC5V, XC6V, XC6S, series 7, Zynq
            // No combined multiplier logic yet - coming later.
            Net[]   p = netArray(na + nb);
            a = sig_expand(a, 25, asigned);
            b = sig_expand(b, 18, bsigned);
            ((XTDECode)family).Mult(a, b, p);   // replace with a hardware multiplier
            return(p);
        default:
            throw new ExEx("Netlist optimisation error - no known multiplier");
        }
        
        // Default (XC2S or XCV or ....)
        // use cascaded adders
        if (!asigned && bsigned)
            a = sig_expand(a, nb+1, false);
        if (asigned && !bsigned)
            b = sig_expand(b, nb+1, false);
        if (asigned || bsigned) {
            // signed
            return(muls(a, b));
        } else {
            // unsigned
            return(mulu(a, b));
        }
    }
 
    /**
     * Encode a divider. Input nets may be different sizes.
     * The width of the output net is the width of the 1st operand
     * minus the width of the 2nd operand.
     * @param   a is the 1st input operand net array
     * @param   b is the 2nd input operand net array
     * @param   asigned is true if the 1st input operand is signed
     * @param   bsigned is true if the 2nd input operand is signed
     * @return  the output net array
     */
    public Net[] div (Net[] a, Net[] b, boolean asigned, boolean bsigned) {
        if (!asigned && bsigned)
            a = sig_expand(a, a.length+1, false);
        if (asigned && !bsigned)
            b = sig_expand(b, b.length+1, false);
        ArrayList<Net[]> al = quotrem(a, b, asigned, bsigned);
        return(al.get(0));
    }
 
    /**
     * Encode a remainder. Input nets may be different sizes.
     * The width of the output net is the same as the width of the 2nd
     * operand.
     * @param   a is the 1st input operand net array
     * @param   b is the 2nd input operand net array
     * @param   asigned is true if the 1st input operand is signed
     * @param   bsigned is true if the 2nd input operand is signed
     * @return  the output net array
     */
    public Net[] rem (Net[] a, Net[] b, boolean asigned, boolean bsigned) {
        if (!asigned && bsigned)
            a = sig_expand(a, a.length+1, false);
        if (asigned && !bsigned)
            b = sig_expand(b, b.length+1, false);
        ArrayList<Net[]> al = quotrem(a, b, asigned, bsigned);
        return(al.get(1));
    }
 
    /**
     * Encode a quotient/remainder. Input nets may be different sizes.
     * The width of the quotient output net is the width of the 1st operand
     * minus the width of the 2nd operand.
     * The width of the remainder output net is the same as the width of the 2nd
     * operand.
     * @param   a is the 1st input operand (dividend) net array
     * @param   b is the 2nd input operand (divisor) net array
     * @param   asigned is true if the 1st input operand is signed
     * @param   bsigned is true if the 2nd input operand is signed
     * @return  a list of two output nets (quotient and remainder)
     */
    public ArrayList<Net[]> quotrem (Net[] a, Net[] b, boolean asigned, boolean bsigned) {
        if (!asigned && bsigned)
            a = sig_expand(a, a.length+1, false);
        if (asigned && !bsigned)
            b = sig_expand(b, b.length+1, false);
        if (asigned || bsigned)
            // signed
            return(quotrems(a, b));
        else
            // unsigned
            return(quotremu(a, b));
    }
 
    /**
     * Signed multiplier.
     * Input arrays a and b may be of different sizes. The multiplier consists
     * of b.length-1 adders and one subtracter each of width a.length.
     * @param   a is the multiplicand
     * @param   b is the multiplier
     * @return  the product
     */
    private Net[] muls (Net[] a, Net[] b) {
        int     awid = a.length;
        int     bwid = b.length;
        int     owid = awid + bwid;
        Net[][] p = new Net[bwid][awid+1];
        Net[]   last = new Net[awid];
        Net[]   out = new Net[awid + bwid];
        int     k = 0;

        for (int i=0 ; i<bwid ; i++) {
            if (i == 0) {
                for (int j=0 ; j<awid ; j++)
                    p[0][j] = AND(a[j], b[0]);
                p[0][awid] = p[0][awid-1];
            } else
                p[i] = mul_add(true, subarray(p[i-1], 1, awid), a, b[i]);
            out[i] = p[i][0];
        }

        last = mul_sub(true, subarray(p[bwid-1], 1, awid), a, b[bwid-1]);

        for (int i=bwid ; i<owid ; i++)
            out[i] = last[k++];

        return(out);
    }

    /**
     * Unsigned multiplier.
     * Input arrays a and b may be of different sizes. The multiplier consists
     * of b.length-1 adders each of width a.length.
     * @param   a is the multiplicand
     * @param   b is the multiplier
     * @return  the product
     */
    private Net[] mulu (Net[] a, Net[] b) {
        int     awid = a.length;
        int     bwid = b.length;
        int     owid = awid + bwid;
        Net[][] p = new Net[bwid][awid+1];
        Net[]   out = new Net[awid + bwid];
        int     k = 0;

        for (int i=0 ; i<bwid ; i++) {
            if (i == 0) {
                for (int j=0 ; j<awid ; j++)
                    p[0][j] = AND(a[j], b[0]);
                p[0][awid] = Net.LO;
            } else
                p[i] = mul_add(false, subarray(p[i-1], 1, awid), a, b[i]);
            if (i < (bwid-1))
                out[i] = p[i][0];
        }
        for (int i=bwid-1 ; i<owid ; i++)
            out[i] = p[bwid-1][k++];
        return(out);
    }

    /**
     * Unsigned quotient/remainder.
     * Input arrays a and b may be of different sizes.
     * @param   a is the dividend
     * @param   b is the divisor
     * @return  a list containing the quotient and remainder net arrays
     */
    private ArrayList<Net[]> quotremu (Net[] a, Net[] b) {
        ArrayList<Net[]>    al;
        int                 numwid = a.length;
        int                 denwid = b.length;
        int                 shift = numwid - 1;
        int                 bits = numwid + denwid;
        Net[]               num = new Net[bits];
        Net[]               den = new Net[bits];
        Net[]               diff = new Net[bits];

        num = sig_expand(a, bits, false);

        int i = 0;
        int j = 0;
        for ( ; i<(numwid-1) ; i++)
            den[i] = Net.LO;
        for ( ; i<(bits-1) ; i++)
            den[i] = b[j++];
        den[bits-1] = Net.LO;
 
        diff = sub(num, den, true, true, 0);
 
        al = part_udiv(numwid, denwid, bits, bits-1, shift-1, diff, subarray(den, 1, bits-1));

        return(al);
    }

    /**
     * Signed quotient/remainder.
     * Input arrays a and b may be of different sizes.
     * @param   a is the dividend
     * @param   b is the divisor
     * @return  a list containing the quotient and remainder net arrays
     */
    private ArrayList<Net[]> quotrems (Net[] a, Net[] b) {
        ArrayList<Net[]>    al;
        int                 numwid = a.length;
        int                 denwid = b.length;
        int                 shift = numwid - 1;
        int                 rbits = numwid + denwid;
        Net[]               num = new Net[rbits];
        Net[]               denom = b;
        Net[]               sumdiff = new Net[rbits];
        Net[]               rem = new Net[rbits-1];
        Net[]               top = new Net[1];
        Net                 add;

        num = sig_expand(a, rbits, true);
 
        add = XOR(a[numwid-1], b[denwid-1]);
        sumdiff = addsub(num, lshift(denom, shift, true, rbits), add, Net.HI, Net.LO, true, true);
        top[0] = INV(XOR(sumdiff[rbits-1], denom[denwid-1]));
        rem = subarray(sumdiff, 0, rbits-2);
 
        al = part_sdiv(1, rbits-1, denwid, shift-1, top, rem, denom, a[numwid-1], null);

        return(al);
    }
 
    private Net[] mul_add (boolean signed, Net[] a, Net[] b, Net igb) {
        int     awid = a.length + 1;
            a = sig_expand(a, awid, signed);
            if (igb.isConnected(Net.LO))
                return(a);
            b = sig_expand(b, awid, signed);
            Net[]       di = new Net[awid]; // data input to carry MUX
            Net[]       ps = new Net[awid]; // partial sum
            Net[]       s = new Net[awid];  // sum
            Net[]       c = new Net[awid];  // internal carries
 
        c[0] = Net.LO;
        for (int i=0 ; i<awid ; i++) {
            if (igb.isConnected(Net.HI)) {
                ps[i] = XOR(a[i], b[i]);
                di[i] = a[i];
            } else {
                if (add_sub_use_lut)
                    ps[i] = LUT("((I2*(I0^I1)+!I2*I0))", a[i], b[i], igb);
                else
                    ps[i] = OR(AND(igb, XOR(a[i], b[i])), AND(INV(igb), a[i]));
                di[i] = MULT_AND(a[i], igb);
            }
            s[i] = XORCY(ps[i], c[i]);
            if (i != (awid-1))
                c[i+1] = MUXCY(ps[i], di[i], c[i]);
        }

        return(s);
    }
 
    private Net[] mul_sub (boolean signed, Net[] a, Net[] b, Net igb) {
        int     awid = a.length + 1;
            a = sig_expand(a, awid, signed);
            if (igb.isConnected(Net.LO))
                return(a);
            b = sig_expand(b, awid, signed);
            Net[]       di = new Net[awid]; // data input to carry MUX
            Net[]       ps = new Net[awid]; // partial sum
            Net[]       s = new Net[awid];  // sum
            Net[]       c = new Net[awid];  // internal carries
 
        c[0] = Net.LO;
        for (int i=0 ; i<awid ; i++) {
            if (igb.isConnected(Net.HI)) {
                ps[i] = XOR(a[i], b[i]);
                di[i] = a[i];
            } else {
                if (add_sub_use_lut)
                    ps[i] = LUT("((I2*(I0^!I1)+!I2*I0))", a[i], b[i], igb);
                else
                    ps[i] = OR(AND(igb, XOR(a[i], INV(b[i]))), AND(INV(igb), a[i]));
                di[i] = MULT_AND(a[i], igb);
            }
            s[i] = XORCY(ps[i], c[i]);
            if (i != (awid-1))
                c[i+1] = MUXCY(ps[i], di[i], c[i]);
        }

        return(s);
    }
 
    private ArrayList<Net[]> part_udiv (
        int qbits, int rbits, int bits, int nbits, int shift,
        Net[] n, Net[] d
    ) {
        ArrayList<Net[]>    al;
        Net[]               q = new Net[bits];
        Net[]               r = new Net[nbits-1];
        Net[]               xx = new Net[nbits];
        int         j = 0;
        xx = addsub(subarray(n, 0, nbits-1), d, n[nbits], Net.HI, Net.LO, false, false);
        for (int i=0 ; i<nbits ; i++)
            q[j++] = xx[i];
        for (int i=nbits ; i<bits ; i++)
            q[j++] = n[i];
        if (shift != 0) {
            r = subarray(d, 1, nbits-1);
            al = part_udiv(qbits, rbits, bits, nbits-1, shift-1, q, r);
            return(al);
        } else {
            Net[]   qq;
            Net[]   rr;
            r = subarray(d, 0, nbits-2);
            qq = inv(subarray(q, rbits, bits-1));
            rr = mul_add(false, subarray(q, 0, rbits-1), subarray(r, 0, rbits-1), q[rbits]);
            al = new ArrayList<Net[]>();
            al.add(qq);
            al.add(subarray(rr, 0, rbits-1));
            return(al);
        }
    }
 
    private ArrayList<Net[]> part_sdiv (
        int qbits, int rbits, int dbits, int shift,
        Net[] top, Net[] rem, Net[] denom, Net numneg, Net[] remzero
    ) {
        ArrayList<Net[]>    al;
        int                 nextrbits = rbits - ((shift ==0 ) ? 0 : 1);
        Net                 add = INV(top[0]);
 
        if (shift != -1) {
            Net[]   nexttop = new Net[qbits+1];
            Net[]   nextrem = new Net[nextrbits];
            Net[]   nextremzero;
            Net[]   sumdiff = new Net[rbits];
            Net     newquotbit;
            if (remzero == null)
                nextremzero = new Net[1];
            else {
                nextremzero = new Net[remzero.length+1];
                for (int i=0 ; i<remzero.length ; i++)
                    nextremzero[i+1] = remzero[i];
            }
            sumdiff = addsub(rem, lshift(denom, shift, true, nextrbits), add, Net.HI, Net.LO, true, true);
            newquotbit = XOR(sumdiff[rbits-1], INV(denom[dbits-1]));
            for (int i=0 ; i<qbits ; i++)
                nexttop[i+1] = top[i];
            nexttop[0] = newquotbit;
            nextrem = subarray(sumdiff, 0, nextrbits-1);
            nextremzero[0] = decode(rem, 0);
            al =    part_sdiv(
                        qbits+1, nextrbits, dbits, shift-1,
                        nexttop, nextrem, denom, numneg, nextremzero
                    );
        } else {
            Net[]   quot = new Net[top.length+1];
            Net[]   finrem = new Net[rem.length];
            Net     lastop;
            Net     incrquot;
            Net     remz = decode(rem, 0);
            Net     anyremz = OR((Object)remzero);
 
            lastop = OR(AND(XOR(rem[rem.length-1], numneg), INV(remz)), anyremz);
            incrquot = OR (
                            AND(remz, numneg, denom[dbits-1]),
                            AND(INV(anyremz), top[qbits-1], INV(AND(remz, numneg))),
                            AND(anyremz, denom[dbits-1])
                       );
            quot = incr(sig_expand(top, top.length+1, true), incrquot);
            finrem = addsub(rem, denom, add, lastop, Net.LO, true, true);

            al = new ArrayList<Net[]>();
            al.add(quot);
            al.add(finrem);
        }

        return(al);
    }

    /**
     * Encode a decoder.
     * @param   a is the input net array
     * @param   val is the constant value to be decoded
     * @return  the result net (high for equals)
     */
    private Net decode (Net[] a, long val) {
        int             width = a.length;
        ArrayList<Net>  al = new ArrayList<Net>();

        for (int i=0 ; i<width ; i++) {
            if ((val & 1) == 0)
                al.add(INV(a[i]));
            else
                al.add(a[i]);
            val >>= 1;
        }

        return(AND(al));
    }

    /**
     * Encode a comparator. Input nets may be different sizes.
     * The output net is the result of the comparison.
     * @param   a is the 1st input operand net array
     * @param   b is the 2nd input operand net array
     * @param   asigned is true if the 1st operand is signed
     * @param   bsigned is true if the 2nd operand is signed
     * @return  the output net, high if {@code a >= b}
     */
    public Net ge (Net[] a, Net[] b, boolean asigned, boolean bsigned) {
        int     awid = a.length + ((!asigned && bsigned) ? 1 : 0);
        int     bwid = b.length + ((asigned && !bsigned) ? 1 : 0);
        int     size = Math.max(awid, bwid);
        Net[]   id = new Net[size];     /* internal difference */
        Net[]   c = new Net[size+1];    /* carries */

        a = sig_expand(a, size, asigned);
        b = sig_expand(b, size, bsigned);
        c[0] = Net.HI;

        if (needCPLDarith()) {
            for (int i=0 ; i<size ; i++) {
                c[i+1] = OR(    AND(INV(c[i]),    a[i],  INV(b[i])),
                                AND(    c[i], INV(a[i]), INV(b[i])),
                                AND(    c[i],     a[i],      b[i]),
                                AND(    c[i],     a[i],  INV(b[i]))  );
            }
            return(c[size]);
        }

        for (int i=0 ; i<size ; i++) {
            id[i] = XOR(a[i], INV(b[i]));
            c[i+1] = MUXCY(id[i], a[i], c[i]);
        }
        if (asigned || bsigned)
            if (ge_use_lut)
                return(LUT("!I0*!I1*!I2*I3+!I0*I1+I0*I1*(!(I2@I3))",
                                    c[size], c[size-1], a[size-1], b[size-1]));
            else
                return(
                    OR(
                        AND(INV(c[size]), INV(c[size-1]), INV(a[size-1]), b[size-1]),
                        AND(INV(c[size]), c[size-1]),
                        AND(c[size], c[size-1], INV(XOR(a[size-1], b[size-1])))
                    )
                );
        else
            return(c[size]);
    }
 
    /**
     * Encode an equality comparator. Input nets may be different sizes.
     * The output net is the result of the comparison.
     * @param   a is the 1st input operand net array
     * @param   b is the 2nd input operand net array
     * @param   asigned is true if the 1st operand is signed
     * @param   bsigned is true if the 2nd operand is signed
     * @return  the output net, high if a == b
     */
    public Net eq (Net[] a, Net[] b, boolean asigned, boolean bsigned) {
        int             awid = a.length + ((!asigned && bsigned) ? 1 : 0);
        int             bwid = b.length + ((asigned && !bsigned) ? 1 : 0);
        int             size = Math.max(awid, bwid);
        ArrayList<Net>  al = new ArrayList<Net>();

        a = sig_expand(a, size, asigned);
        b = sig_expand(b, size, bsigned);
        for (int i=0 ; i<size ; i++)
            al.add(INV(XOR(a[i], b[i])));
        return(AND(al));
    }
 
    /**
     * Encode an inequality comparator. Input nets may be different sizes.
     * The output net is the result of the comparison.
     * @param   a is the 1st input operand net array
     * @param   b is the 2nd input operand net array
     * @param   asigned is true if the 1st operand is signed
     * @param   bsigned is true if the 2nd operand is signed
     * @return  the output net, high if a != b
     */
    public Net ne (Net[] a, Net[] b, boolean asigned, boolean bsigned) {
        int             awid = a.length + ((!asigned && bsigned) ? 1 : 0);
        int             bwid = b.length + ((asigned && !bsigned) ? 1 : 0);
        int             size = Math.max(awid, bwid);
        ArrayList<Net>  al = new ArrayList<Net>();

        a = sig_expand(a, size, asigned);
        b = sig_expand(b, size, bsigned);
        for (int i=0 ; i<size ; i++)
            al.add(XOR(a[i], b[i]));
        return(OR(al));
    }
 
    /**
     * Encode an up/down counter. If the counter direction argument is null
     * the counter will count upwards.
     * @param   n is the size of the counter in bits
     * @param   dwn indicates the direction if parameter 'down' is not used
     * @param   c is the clock net
     * @param   ce is the clock enable net
     * @param   r is the reset net
     * @param   down is an optional count direction, low for up and high for down
     * @param   sinit is an optional hexadecimal initialisation string
     * @return  the output net array
     */
    private Net[] cb (int n, boolean dwn, Net c, Net ce, Net r, Net down, String sinit) {
        if (((r == null) || (r.isConnected(Net.LO))) && (down == null) && (n <= srladdrwidth)) {
            int     blklen = 1 << srladdrwidth;
            // For a simple up or down counter of 'srladdrwidth' bits or fewer
            // use LUT shift registers to generate the count sequence. For
            // counter bits <= 'srladdrwidth' need 1 SRL per address bit so is
            // better than using flip-flops since uses fewer resources and is
            // faster. When bits = 'srladdrwidth' + 1 the number of SRLs
            // doubles and flip-flops plus adder use fewer CLBs.
            int     len = 1 << n;
            int     mask = len - 1;
            int     ini = (sinit != null) ? Integer.parseInt(sinit, blklen) : 0;
            int[]   init = new int[len];
            for (int i=0 ; i<len ; i++)
                init[i] = (dwn ? ini-- : ini++) & mask;
            return(seq(len, n, c, ce, init));
        }

        Net[]   s = netArray(n);
        Net[]   s_;
        Net[]   out;
        out = reg(s, c, ce, r, sinit, false, null);
        if (down != null)
            s_ = incrdecr(out, down);
        else if (dwn)
            s_ = decr(out, null);
        else
            s_ = incr(out, null);
        for (int bit=0 ; bit<n ; bit++)
            s_[bit].connect(s[bit]);
        return(out);
    }
 
    /**
     * Encode a sequence generator using LUT shift registers.
     * @param   len is the length of the sequence
     * @param   wid is the width of sequence integers
     * @param   c is the clock net
     * @param   ce is the clock enable net
     * @param   init is an int array of length 'len' providing the sequence
     *          values
     * @return  the output net array
     */
    private Net[] seq (int len, int wid, Net c, Net ce, int[] init) {
        len--; // reduce length by 1 to allow final flip-flop output stage
        int     blklen = 1 << srladdrwidth;
        Net[]   o = netArray(wid);
        Net[]   ffd = new Net[wid];
        int     blocks = (len - 1) / blklen + 1;
        long[]   ini = new long[blocks];
        int     k, count, bit, v;

        for (int i=0 ; i<wid ; i++) {
            count = 0;
            v = 0;
            k = blocks - 1;
            for (int j=len-1 ; j>=0 ; j--) {
                // len has been reduced by 1 to allow for the trailing flip-flop
                // so use init[j+1], rather than init[j], to leave init[0] for
                // the flip-flop
                bit = (init[j+1] >> i) & 1;
                v |= bit << count++;
                if (count == blklen) {
                    ini[k--] = v;
                    count = 0;
                    v = 0;
                }
            }
            if (count > 0)
                ini[k] = v;
            ffd[i] = srl_(len, o[i], c, ce, ini);
            bit = (init[0] >> i) & 1;
            o[i].connect(FDRE(ffd[i], c, ce, Net.LO, bit != 0 ? "S" : "R"));
        }
        return(o);
    }

    private Net srl_ (int len, Net in, Net c, Net ce, long[] init) {
        int     blklen = 1 << srladdrwidth;
        if (len == 0)
            return(in);
        else
            return(srle_(len, in, c, ce, init, (len-1)/blklen, false));
    }

    private Net srle_ (int len, Net in, Net c, Net ce, long[] init, int index, boolean q31) {
        int     blklen = 1 << srladdrwidth;
        Net     o = new Net();
        Net     out;
        long    ini = 0;

        if (len > blklen) {
            out = srle_(len-blklen, o, c, ce, init, index-1, true);
            len = blklen;
        } else
            out = o;
        if (index < init.length)
            ini = init[index];
        switch (srladdrwidth) {
        case 4:
            o.connect(SRL16E(in, c, ce, constant(len-1, 4), Long.toHexString(ini)));
            break;
        case 5:
            o.connect(SRL32E(in, c, ce, constant(len-1, 5), Long.toHexString(ini), q31));
        }
        return(out);
    }
 
    /**
     * Construct an mxn combinatorial-output single-port memory.
     * The data may be any width. The address may be any width but in
     * practice should be 2 to 9 bits (maximum depth of 512) to avoid
     * excessive multiplexing logic and resultant speed limitation. The
     * memory depth is determined from the address size. The data input
     * must be at least as wide as specified by the width argument. The
     * initialisation string is a hexadecimal string which represents
     * packed binary words, the words being the width of the data. The
     * right end of the string contains the memory word at the lowest
     * address and the right-most bits of that word are the least
     * significant bits of the memory word. The initialisation string
     * may be shorter than the memory in which case the most significant
     * words and bits will be zero padded. If the initialisation string
     * is longer than the memory the extra bits are ignored. If the
     * memory is not written the data, write enable and clock inputs may
     * be null. The data output array is the size specified by the width
     * argument.
     * @param   dwidth is the data width
     * @param   wclk is the write clock net or null
     * @param   we is the write enable net or null
     * @param   a is the address input net array
     * @param   din is the data input net array or null
     * @param   dout is the data output net array or null
     * @param   sinit is the initialisation data hexadecimal string
     */
    public void sram1 (
        int                 dwidth,
        Net                 wclk,
        Net                 we,
        Net[]               a,
        Net[]               din,
        Net[]               dout,
        String[]            sinit
    ) {
        if ((wclk == null) || (din == null)) {
            // Is not written - implement as a ROM.
            srom1(dwidth, a, dout, sinit);
            return;
        }

        int     ram_block_asize;
        Net[]   a_in_blk;
        int     blocks;
        int     asize;

        if (a.length < ramc_addrmin[0]) {
            // Address smaller than that for smallest block -
            // expand address connecting extra bits to GND.
            ram_block_asize = ramc_addrmin[0];
            asize = ramc_addrmin[0];
            Net[]   a_new = new Net[ramc_addrmin[0]];
            int     i = 0;
            for ( ; i<a.length ; i++)
                a_new[i] = a[i];
            for (; i<ramc_addrmin[0] ; i++)
                a_new[i] = Net.LO;
            a = a_new;
            a_in_blk = (asize > ram_block_asize) ? subarray(a, 0, (ram_block_asize - 1)) : a;
            blocks = 1;
        } else if (a.length > ramc_addrmax[0]) {
            // Address larger than that for largest block -
            // truncate address for blocks and add MUXes for output and WEs using
            // excess address bits.
            ram_block_asize = ramc_addrmax[0];
            asize = a.length;
            a_in_blk = subarray(a, 0, (ramc_addrmax[0] - 1));
            blocks = 1 << (a.length - ramc_addrmax[0]);
        } else {
            // Address fits an available block size.
            ram_block_asize = a.length;
            asize = a.length;
            a_in_blk = a;
            blocks = 1;
        }

        int     block_depth = 1 << ram_block_asize;
        Net[]   wes = write_enables(a, we, ram_block_asize);

        for (int bit=0 ; bit<dwidth ; bit++) {
            Net             di = din[bit];
            Net[]           o = netArray(blocks);
            String[]        vinit = new String[blocks];

            if (sinit != null) {
                for (int b=0 ; b<blocks ; b++) {
                    StringBuffer    sb = new StringBuffer();
                    for (int i=0 ; i<block_depth ; i++) {
                        int j = block_depth * b + i;
                        boolean insert_one = (j < sinit.length) && (strhexbit(sinit[j], bit) == 1);
                        sb.insert(0, insert_one ? "1" : "0");
                    }
                    vinit[b] = binToHex(sb.toString());
                }
            }

            for (int b=0 ; b<blocks ; b++)
                RAM_1(wclk, wes[b], a_in_blk, di, o[b], vinit[b]);

            dout[bit].connect(out_selects(a, o, ram_block_asize));
        }
    }
 
    /**
     * Construct an mxn combinatorial-output single-port read-only memory.
     * The data may be any width. The address may be any width but in
     * practice should be 1 to 9 bits (maximum depth of 512) to avoid
     * excessive multiplexing logic and resultant speed limitation. The
     * memory depth is determined from the address size. The data input
     * must be at least as wide as specified by the width argument. The
     * initialisation string is a hexadecimal string which represents
     * packed binary words, the words being the width of the data. The
     * right end of the string contains the memory word at the lowest
     * address and the right-most bits of that word are the least
     * significant bits of the memory word. The initialisation string
     * may be shorter than the memory in which case the most significant
     * words and bits will be zero padded. If the initialisation string
     * is longer than the memory the extra bits are ignored. If the
     * memory is not written the data, write enable and clock inputs may
     * be null. The data output array is the size specified by the width
     * argument.
     * @param   dwidth is the data width
     * @param   a is the address input net array
     * @param   dout is the data output net array or null
     * @param   sinit is the initialisation data hexadecimal string
     */
    public void srom1 (
        int                 dwidth,
        Net[]               a,
        Net[]               dout,
        String[]            sinit
    ) {
        int     rom_block_asize;
        Net[]   a_in_blk;
        int     blocks;
        
        if (a.length < romc_addrmin[0]) {
            // Address smaller than that for smallest block -
            // expand address connecting extra bits to GND.
            rom_block_asize = romc_addrmin[0];
            Net[]   a_new = new Net[romc_addrmin[0]];
            int     i = 0;
            for ( ; i<a.length ; i++)
                a_new[i] = a[i];
            for (; i<romc_addrmin[0] ; i++)
                a_new[i] = Net.LO;
            a = a_new;
            a_in_blk = a;
            blocks = 1;
        } else if (a.length > romc_addrmax[0]) {
            // Address larger than that for largest block -
            // truncate address for blocks and add MUXes for output using
            // excess address bits.
            rom_block_asize = romc_addrmax[0];
            a_in_blk = subarray(a, 0, (romc_addrmax[0] - 1));
            blocks = 1 << (a.length - romc_addrmax[0]);
        } else {
            // Address fits an available block size.
            rom_block_asize = a.length;
            a_in_blk = a;
            blocks = 1;
        }

        int     block_depth = 1 << rom_block_asize;
        
        for (int bit=0 ; bit<dwidth ; bit++) {
            String[]    vinit = new String[blocks];
            Net[]       o = netArray(blocks);

            if (sinit != null) {
                for (int b=0 ; b<blocks ; b++) {
                    StringBuffer    sb = new StringBuffer();
                    for (int i=0 ; i<block_depth ; i++) {
                        int j = block_depth * b + i;
                        boolean insert_one = (j < sinit.length) && (strhexbit(sinit[j], bit) == 1);
                        sb.insert(0, insert_one ? "1" : "0");
                    }
                    vinit[b] = binToHex(sb, block_depth/4);
                }
            }

            for (int b=0 ; b<blocks ; b++)
                ROM_1(a_in_blk, o[b], vinit[b]);

            dout[bit].connect(out_selects(a, o, rom_block_asize));
        }
    }

    /**
     * Construct an mxn combinatorial-output dual-port memory.
     * The data may be any width. The address may be any width but in
     * practice should be 1 to 9 bits (maximum depth of 512) to avoid
     * excessive multiplexing logic and resultant speed limitation. The
     * memory depth is determined from the address size. The data input
     * must be at least as wide as specified by the width argument. The
     * initialisation string is a hexadecimal string which represents
     * packed binary words, the words being the width of the data. The
     * right end of the string contains the memory word at the lowest
     * address and the right-most bits of that word are the least
     * significant bits of the memory word. The initialisation string
     * may be shorter than the memory in which case the most significant
     * words and bits will be zero padded. If the initialisation string
     * is longer than the memory the extra bits are ignored. If a port
     * is not written the data, write enable and clock inputs for that
     * port may be null - note that for Xilinx port 1 cannot be written
     * and hence the data, clock and write enable inputs for that port
     * are ignored. The data output arrays are the size specified by the
     * width argument.
     * @param   dwidth is the data width
     * @param   wclk0 is the write clock net or null for port 0
     * @param   we0 is the write enable net or null for port 0
     * @param   a0 is the address input net array for port 0
     * @param   din0 is the data input net array or null for port 0
     * @param   dout0 is the data output net array or null for port 0
     * @param   wclk1 is the write clock net or null for port 1
     * @param   we1 is the write enable net for port 1
     * @param   a1 is the address input net array for port 1
     * @param   din1 is the data input net array for port 1
     * @param   dout1 is the data output net array for port 1
     * @param   sinit is the initialisation data hexadecimal string
     */
    public void sram2 (
        int                 dwidth,
        Net                 wclk0,  // may be NULL (dual port ROM)
        Net                 we0,    // may be NULL (dual port ROM)
        Net[]               a0,
        Net[]               din0,   // may be NULL (dual port ROM)
        Net[]               dout0,
        Net                 wclk1,  // null for Xilinx CLB RAM
        Net                 we1,    // null for Xilinx CLB RAM
        Net[]               a1,
        Net[]               din1,   // null for Xilinx CLB RAM
        Net[]               dout1,
        String[]            sinit
    ) {
        if ((din1 != null) || (we1 != null))
            throw new ExEx("For Xilinx FPGA cannot write to 2nd port of logic block RAM");

        if ((wclk0 == null) || (we0 == null) || (din0 == null)) {
            // A two-port ROM -
            // Implement as two single-port ROMs. This will place-and-route
            // much better and does not use any more logic resources.
            srom1( dwidth, a0, dout0, sinit);
            srom1( dwidth, a1, dout1, sinit);
            return;
        }
 
        int     ram_block_asize;
        Net[]   a0_in_blk;
        Net[]   a1_in_blk;
        int     blocks;
        
        if (a0.length < ramc_addrmin[1]) {
            // Address smaller than that for smallest block -
            // expand address connecting extra bits to GND.
            ram_block_asize = ramc_addrmin[1];
            Net[]   a0_new = new Net[ramc_addrmin[1]];
            Net[]   a1_new = new Net[ramc_addrmin[1]];
            int     i = 0;
            for ( ; i<a0.length ; i++) {
                a0_new[i] = a0[i];
                a1_new[i] = a1[i];
            }
            for (; i<ramc_addrmin[1] ; i++) {
                a0_new[i] = Net.LO;
                a1_new[i] = Net.LO;
            }
            a0 = a0_new;
            a1 = a1_new;
            a0_in_blk = a0;
            a1_in_blk = a1;
            blocks = 1;
        } else if (a0.length > ramc_addrmax[1]) {
            // Address larger than that for largest block -
            // truncate address for blocks and add MUXes for output and WEs using
            // excess address bits.
            ram_block_asize = ramc_addrmax[1];
            a0_in_blk = subarray(a0, 0, (ramc_addrmax[1] - 1));
            a1_in_blk = subarray(a1, 0, (ramc_addrmax[1]- 1));
            blocks = 1 << (a0.length - ramc_addrmax[1]);
        } else {
            // Address fits an available block size.
            ram_block_asize = a0.length;
            a0_in_blk = a0;
            a1_in_blk = a1;
            blocks = 1;
        }

        int     block_depth = 1 << ram_block_asize;
        Net[]   wes = write_enables(a0, we0, ram_block_asize);
                                    // only one since can only write port 0, not port 1

        for (int bit=0 ; bit<dwidth ; bit++) {
            Net         di = (din0 == null) ? null : din0[bit];
            Net[]       o0 = netArray(blocks);
            Net[]       o1 = netArray(blocks);
            String[]    vinit = new String[blocks];

            if (sinit != null) {
                for (int b=0 ; b<blocks ; b++) {
                    StringBuffer    sb = new StringBuffer();
                    for (int i=0 ; i<block_depth ; i++) {
                        int j = block_depth * b + i;
                        boolean insert_one = (j < sinit.length) && (strhexbit(sinit[j], bit) == 1);
                        sb.insert(0, insert_one ? "1" : "0");
                    }
                    vinit[b] = binToHex(sb, block_depth/4);
                }
            }

            for (int b=0 ; b<blocks ; b++)
                RAM_2(wclk0, wes[b], a0_in_blk, di, o0[b], a1_in_blk, o1[b], vinit[b]);

            if (dout0 != null)
                dout0[bit].connect(out_selects(a0, o0, ram_block_asize));
            if (dout1 != null)
                dout1[bit].connect(out_selects(a1, o1, ram_block_asize));
        }
    }

    /**
     * Construct a registered-output single-port memory.
     * Data may be any width.
     * Only the following address widths are allowed -
     * <pre>
     * XC2S, XCV
     * address     data per block
     *   8         16  (256 deep x 16 bits)
     *   9          8  (512 deep x 8 bits)
     *  10          4  (1024 deep x 4 bits)
     *  11          2  (2048 deep x 2 bits)
     *  12          1  (4096 deep x 1 bit)
     *
     * XC3S, XC2V, XC2VP
     * address     data per block
     *   9         36  (512 deep x 36 bits)
     *  10         18  (1024 deep x 18 bits)
     *  11          9  (2048 deep x 9 bits)
     *  12          4  (4096 deep x 4 bits)
     *  13          2  (8192 deep x 2 bits)
     *  14          1  (16384 deep x 1 bit)
     * </pre>
     * The initialisation string is a
     * hexadecimal string which represents packed binary words, the words
     * being the width of the data. The right end of the string contains the
     * memory word at the lowest address and the right-most bits of that
     * word are the least significant bits of the memory word. The
     * initialisation string may be shorter than the memory in which case
     * the most significant words and bits will be zero padded. If the
     * initialisation string is longer than the memory the extra bits are
     * ignored. If the memory is not written the data, write enable and clock
     * inputs may be null. The data output array is the size specified
     * by the width argument.
     * The address net array may be smaller than specified by the address
     * width in which case it will be zero padded.
     * @param   dwidth      data width
     * @param   awidth      address width
     * @param   read        read enable net or null
     * @param   write       write enable net or null
     * @param   clk         clock net
     * @param   a           address input net array
     * @param   din         data input net array or null
     * @param   dout        data output net array or null
     * @param   sinit       array of hexadecimal strings, one for each word, or null
     * @param   init0       initial hexadecimal string value of output register port 0 (rmemory only)
     * @param   init1       initial hexadecimal string value of output register port 1 (rmemory only, 2 ports)
     * @param   properties  properties (attributes such as writemode)
     * @param   continuous  if true causes the RAM ENABLE inputs to be tied high
     *                      rather than asserted only upon access
     */
    public void rram1 (
        int                 dwidth,
        int                 awidth,
        Net                 read,
        Net                 write,
        Net                 clk,
        Net[]               a,
        Net[]               din,
        Net[]               dout,
        String[]            sinit,
        String              sinit0,
        ArrayList<String>   properties,
        boolean             continuous
    ) {
        Net                 en = null;

        if (continuous)
            en = Net.HI;
        else {
            if ((write != null) && (read != null))
                en = OR(write, read);
            else if (write != null)
                en = write;
            else if (read != null)
                en = read;
        }
        
        // Convert hex string to binary string for initialisation of RAM
        // and for initial value of output register.
        String[]    sinits = null;
        if (sinit != null) {
            sinits = new String[sinit.length];
            for (int i=0 ; i<sinit.length ; i++) {
                String  binit = hexToBin(sinit[i]);
                sinits[i] = zeropad(dwidth - binit.length()) + binit;
            }
        }
        String      sinits0 = null;
        if (sinit0 != null) {
            String  binit = hexToBin(sinit0);
            sinits0 = zeropad(dwidth - binit.length()) + binit;
        }
  
        // Allocate block RAM elements and connect their inputs and outputs.
        // ramallocate() is defined in XElements but is overridden in FPGA family classes.
        ramallocate(
            awidth, dwidth,
            write, en, clk, a, din, dout,
            null, null, null, null, null, null,
            sinits, sinits0, null, properties
        ); 
    }

    /**
     * Construct a registered-output dual-port memory.
     * Data may be any width.
     * Only the following address widths are allowed -
     * <pre>
     * XC2S, XCV
     * address     data per block
     *   8         16  (256 deep x 16 bits)
     *   9          8  (512 deep x 8 bits)
     *  10          4  (1024 deep x 4 bits)
     *  11          2  (2048 deep x 2 bits)
     *  12          1  (4096 deep x 1 bit)
     *
     * XC3S, XC2V, XC2VP, XC4V, XC5V, XC6S, XC6V, XC7
     * address     data per block
     *   9         36  (512 deep x 36 bits)
     *  10         18  (1024 deep x 18 bits)
     *  11          9  (2048 deep x 9 bits)
     *  12          4  (4096 deep x 4 bits)
     *  13          2  (8192 deep x 2 bits)
     *  14          1  (16384 deep x 1 bit)
     * </pre>
     * The initialisation string is a
     * hexadecimal string which represents packed binary words, the words
     * being the width of the data. The right end of the string contains the
     * memory word at the lowest address and the right-most bits of that
     * word are the least significant bits of the memory word. The
     * initialisation string may be shorter than the memory in which case
     * the most significant words and bits will be zero padded. If the
     * initialisation string is longer than the memory the extra bits are
     * ignored. If the memory is not written the data, write enable and clock
     * inputs may be null. The data output array is the size specified
     * by the width argument.
     * The address net array may be smaller than specified by the address
     * width in which case it will be zero padded.
     * @param   dwidth      data width
     * @param   awidth      address width
     * @param   read0       port 0 read enable net or null
     * @param   write0      port 0 write enable net or null
     * @param   clk0        port 0 clock net
     * @param   a0          port 0 address input net array
     * @param   din0        port 0 data input net array or null
     * @param   dout0       port 0 data output net array or null
     * @param   read1       port 1 read enable net or null
     * @param   write1      port 1 write enable net or null
     * @param   clk1        port 1 clock net
     * @param   a1          port 1 address input net array
     * @param   din1        port 1 data input net array or null
     * @param   dout1       port 1 data output net array or null
     * @param   sinit       array of hexadecimal strings, one for each word
     * @param   init0       initial hexadecimal string value of output register port 0 (rmemory only)
     * @param   init1       initial hexadecimal string value of output register port 1 (rmemory only)
     * @param   properties  properties (attributes such as writemode)
     * @param   continuous  if true causes the RAM ENABLE inputs to be tied high
     *                      rather than asserted only upon access
     */
    public void rram2 (
        int                 dwidth,
        int                 awidth,
        Net                 read0,
        Net                 write0,
        Net                 clk0,
        Net[]               a0,
        Net[]               din0,
        Net[]               dout0,
        Net                 read1,
        Net                 write1,
        Net                 clk1,
        Net[]               a1,
        Net[]               din1,
        Net[]               dout1,
        String[]            sinit,
        String              sinit0,
        String              sinit1,
        ArrayList<String>   properties,
        boolean             continuous
    ) {
        Net                 en0 = null;
        Net                 en1 = null;
 
        if (continuous) {
            en0 = Net.HI;
            en1 = Net.HI;
        } else {
            if ((write0 != null) && (read0 != null)) {
                if (lutwidth > 4)
                    en0 = OR(write0, read0);
                else
                    en0 = MUXF5(write0, read0, Net.HI);
            } else if (write0 != null)
                en0 = write0;
            else if (read0 != null)
                en0 = read0;
            if ((write1 != null) && (read1 != null)) {
                if (lutwidth > 4)
                    en1 = OR(write1, read1);
                else
                    en1 = MUXF5(write1, read1, Net.HI);
            } else if (write1 != null)
                en1 = write1;
            else if (read1 != null)
                en1 = read1;
        }
 
        // Convert hex string to binary string for initialisation of RAM
        // and for initial values of output registers.
        String[]    sinits = null;
        if (sinit != null) {
            sinits = new String[sinit.length];
            for (int i=0 ; i<sinit.length ; i++) {
                String  binit = hexToBin(sinit[i]);
                sinits[i] = zeropad(dwidth - binit.length()) + binit;
            }
        }
        String      sinits0 = null;
        if (sinit0 != null) {
            String  binit = hexToBin(sinit0);
            sinits0 = zeropad(dwidth - binit.length()) + binit;
        }
        String      sinits1 = null;
        if (sinit1 != null) {
            String  binit = hexToBin(sinit1);
            sinits1 = zeropad(dwidth - binit.length()) + binit;
        }
 
        // Allocate block RAM elements and connect their inputs and outputs.
        // ramallocate() is defined in XElements but is overridden in FPGA family classes.
        ramallocate(
            awidth, dwidth,
            write0, en0, clk0, a0, din0, dout0,
            write1, en1, clk1, a1, din1, dout1,
            sinits, sinits0, sinits1, properties
        ); 
    }
    
    /**
     * Partition block RAM memory into available elements.
     * Multiple elements are used where the data width exceeds
     * that available from one element. The depth is constrained
     * by the maximum depth of an element.
     *
     * @param   awidth      address width requested
     * @param   dwidth      data width
     * @param   wea         port A write enable
     * @param   ena         port A enable
     * @param   clka        port A clock
     * @param   addra       port A address
     * @param   dina        port A data input
     * @param   douta       port A data output
     * @param   web         port B write enable
     * @param   enb         port B enable
     * @param   clkb        port B clock
     * @param   addrb       port B address
     * @param   dinb        port B data input
     * @param   doutb       port B data output
     * @param   init        initialisation array (binary strings) for requested address width.
     * @param   init0       initial binary string value of output register port 0 (rmemory only)
     * @param   init1       initial binary string value of output register port 1 (rmemory only)
     * @param   properties  properties (attributes such as writemode)
     */
    public void ramallocate (
        int                 awidth, 
        int                 dwidth, 
        Net                 wea,
        Net                 ena,
        Net                 clka,
        Net[]               addra,
        Net[]               dina,
        Net[]               douta,
        Net                 web,
        Net                 enb,
        Net                 clkb,
        Net[]               addrb,
        Net[]               dinb,
        Net[]               doutb,
        String[]            sinit,
        String              sinit0,
        String              sinit1,
        ArrayList<String>   properties
    ) {
        ArrayList<RamBlock>   ar = new ArrayList<RamBlock>();
        int minaw = rramAwidthMin(rramPorts(null), null);
        int maxaw = rramAwidthMax(rramPorts(null), null);
        if (awidth < minaw)
            awidth = minaw;
        else if (awidth > maxaw)
            throw new ExEx("block RAM memory address width exceeds maximum");
        //
        // There are 2 types of BRAM available in general.
        // lbw is block data width for larger BRAM
        // sbw is block data width for smaller BRAM
        // Where the address width is too great for one of these
        // the data width returned will be zero so it will be skipped
        // in the code below.
        // For devices where only one BRAM type is available sbw will be
        // set to zero.
        // Address widths greater than that provided by the larger BRAM should
        // not get this far (filtered out in exec/Memory.java)/
        //
        int         ltype = RAMB_types.get(big_RAMB);   // type index of 1st BRAM type
        int         stype = RAMB_types.get(small_RAMB); // type index of 2nd BRAM type
        int[]       lwidths = RAMB_dwidths[ltype];      // data width array for 1st BRAM type
        int[]       swidths = RAMB_dwidths[stype];      // data width array for 2nd BRAM type or null
        int         lbw = lwidths[awidth];              // data width for 1st BRAM type
        int         sbw = swidths[awidth];              // data width for 2nd BRAM type - zero if no 2nd BRAM type
        int         bdwidth;
        int         depth = 1 << awidth;
        int         type;
        
        int s;
        int f = dwidth;
        int l = 0;
        int u;
        
        for (int i=dwidth ; i>0 ; i-=bdwidth) {
            String[]    binit = null;
            String      binit0 = null;
            String      binit1 = null;
            
            if (i <= sbw) {
                bdwidth = sbw;  // use smaller (1st) BRAM type
                type = stype;
            } else {
                bdwidth = lbw;  // use larger (2nd) BRAM type
                type = ltype;
            }
            
            s = f - bdwidth;
            if (s < 0)
                s = 0;
            if (sinit != null) {
                binit = new String[depth];
                for (int j=0 ; j<depth ; j++) {
                    if (j >= sinit.length)
                        binit[j] = zeropad(bdwidth);
                    else {
                        String  bs = sinit[j].substring(s, f);
                        binit[j] = zeropad(bdwidth - bs.length()) + bs;
                    }
                }
            }
            if (sinit0 != null) {
                String  bs = sinit0.substring(s, f);
                binit0 = zeropad(bdwidth - bs.length()) + bs;
            }
            if (sinit1 != null) {
                String  bs = sinit1.substring(s, f);
                binit1 = zeropad(bdwidth - bs.length()) + bs;
            }
            u = l + bdwidth - 1;
            if (u >= dwidth)
                u = dwidth - 1;
            Net[]   ida = (dina != null) ? subarray(dina, l, u) : null;
            Net[]   idb = (dinb != null) ? subarray(dinb, l, u) : null;
            Net[]   oda = (douta != null) ? subarray(douta, l, u) : null;
            Net[]   odb = (doutb != null) ? subarray(doutb, l, u) : null;
            RamBlock    rb = new RamBlock(
                                    awidth, bdwidth, type,
                                    wea, ena, clka, addra, ida, oda,
                                    web, enb, clkb, addrb, idb, odb,
                                    binit, binit0, binit1, arrayformat, properties
                                );
            ar.add(rb);
            f = s;
            l += bdwidth;
        }
        
        // Generate the RAMB elements.
        for (RamBlock rb: ar)
            rb.RAMB();
    }
    
    /**
     * Partition a block RAM FIFO into available elements.
     * Multiple elements are used where the data width exceeds
     * that available from one element. The depth is constrained
     * by the maximum depth of an element.
     * address and data width determine FIFO type (18, 36 or 72)
     *
     * @param   depth   depth requested
     * @param   dwidth  data width
     * @param   push    push input signal
     * @param   pop     pop input signal
     * @param   in      data input signal array
     * @param   out     data output signal  signal array
     * @param   empty   empty output signal
     * @param   full    full output signal
     * @param   rdcount port B enable
     * @param   wrcount port B reset
     * @param   reset   reset input signal
     * @param   wclk    write clock
     * @param   rclk    read clock
     */
    public void fifoallocate (
        int     depth, 
        int     dwidth, 
        Net     push,
        Net     pop,
        Net[]   in,
        Net[]   out,
        Net     empty,
        Net     full,
        Net[]   rdcount,
        Net[]   wrcount,
        Net     reset,
        Net     wclk,
        Net     rclk
    ) {
        if (depth < 512)
            depth = 512;
        else if (depth > 8192)
            throw new ExEx("block RAM FIFO depth exceeds maximum for FIFO36");
        ArrayList<FifoBlock>   ar = new ArrayList<FifoBlock>();
        int         inferred_awidth = bits(depth-1, false);

        int         ltype = FIFO_types.get(big_FIFO);   // type index of 1st FIFO type
        int         stype = FIFO_types.get(small_FIFO); // type index of 2nd FIFO type
        int[]       lwidths = FIFO_dwidths[ltype];      // data width array for 1st FIFO type
        int[]       swidths = FIFO_dwidths[stype];      // data width array for 2nd FIFO type or null
        int         lbw = lwidths[inferred_awidth];     // data width for 1st FIFO type
        int         sbw = swidths[inferred_awidth];     // data width for 2nd FIFO type - zero if no 2nd FIFO type
        int         bdwidth;
        int         type;
        boolean     first = true;
        
        int l = 0;
        int u;
        
        for (int i=dwidth ; i>0 ; i-=bdwidth) {
            if (i <= sbw) {
                bdwidth = sbw;
                type = stype;
            } else {
                bdwidth = lbw;
                type = ltype;
            }
            
            u = l + bdwidth - 1;
            if (u >= dwidth)
                u = dwidth - 1;
            Net[]   id = subarray(in, l, u);
            Net[]   od = subarray(out, l, u);
            FifoBlock    rb = new FifoBlock(
                                    depth, bdwidth, type,
                                    push, pop, id, od,
                                    empty, full, rdcount, wrcount,
                                    reset, wclk, rclk, arrayformat, first
                                );
            ar.add(rb);
            l += bdwidth;
            first = false;
        }
        
        // Generate the FIFO elements.
        for (FifoBlock rb: ar)
            rb.FIFO();
    }

    private Net[] write_enables (Net[] a, Net we, int min_size) {
        int     size = Math.max(a.length, min_size);
        int     blocks = 1 << (size - min_size);
        Net[]   o = new Net[blocks];
        if (we == null) {
            for (int i=0 ; i<blocks ; i++)
                o[i] = Net.LO;
            return(o);
        }
        if (a.length <= min_size)
            o[0] = we;
        else {
            for (int i=0 ; i<blocks ; i++) {
                ArrayList<Net> al = new ArrayList<Net>();
                for (int j=min_size ; j<size ; j++) {
                    if (((i >> (j-min_size)) & 1) == 0)
                        al.add(INV(a[j]));
                    else
                        al.add(a[j]);
                }
                al.add(we);
                o[i] = AND(al);
            }
        }
        return(o);
    }
 
    private Net out_selects (Net[] a, Net[] o, int min_size) {
        int             size = Math.max(a.length, min_size);
        int             blocks = 1 << (size - min_size);
        ArrayList<Net>  ol = new ArrayList<Net>();
        if (a.length <= min_size)
            return(o[0]);
        else {
            for (int i=0 ; i<blocks ; i++) {
                ArrayList<Net> al = new ArrayList<Net>();
                for (int j=min_size ; j<size ; j++) {
                    if (((i >> (j-min_size)) & 1) == 0)
                        al.add(INV(a[j]));
                    else
                        al.add(a[j]);
                }
                al.add(o[i]);
                ol.add(AND(al));
            }
            return(OR(ol));
        }
    }
 
    /*
     * Create an OBUF to bring out an internal signal to a PAD
     * for compiler diagnostic purposes.
     * @param   in is the signal
     * @param   pad_name is the pad designation
    private void diag_pad (Net in, String pad_name) {
        Net n = new Net("OPAD_" + in.getIdent() + "_" + pad_name, false);
        Element e = OBUF(n, in, null);
 
        Pad     p = null;
        if (portmap.containsKey(pad_name)) {
            p = (Pad)portmap.get(pad_name);
            if (p.onet != null) {
                ThreePL.msg("Output pad " + pad_name + " duplicated");
            }
        } else {
            p = new Pad();
            portmap.put(pad_name, p);
        }
        p.onet = n;
    }
     */
}
