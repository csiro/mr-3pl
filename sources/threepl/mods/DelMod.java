package threepl.mods;

import static threepl.ThreePL.getCurrentClockVar;
import static threepl.ThreePL.longDir;
import static threepl.ThreePL.tdelist;

import threepl.ThreePL;
import threepl.codegen.TDE;
import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.exec.WordSpec;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt module to create a delay line.
 * This has 2 to 6 input arguments and 1 output argument.
 *
 * <p>The 1st input argument is the delay line data input. This is
 * any target mode except queue, memory or clock. It can be any target
 * type.
 *
 * <p>The optional 2nd input argument is the delay line length. This must
 * be an immediate int. This argument may be 0 or omitted, in which case
 * the 5th argument must be given (variable delay line length).
 *
 * <p>The optional 3rd input argument is an optional initialisation.
 * This must be an immediate array argument whose array type matches the
 * delayline input and output target type. The array may be shorter than the
 * line length but not longer.
 *
 * <p>The optional 4th input argument is a clock enable. This argument
 * can be any target mode except queue, memory or clock. It must be type log.
 *
 * <p>The optional 5th input argument is a variable line length. If this
 * argument is given the 2nd argument must be 0 or omitted. This argument can
 * be any target mode except queue, memory or clock. It must be type uint. It
 * must be a maximum of 4 bits, i.e. the line length can vary dynamically
 * between 1 and 16 (argument values 0 to 15).
 *
 * <p>The optional 6th input argument is reset. This argument can be any
 * target mode except queue, memory or clock. It must be type log.
 *
 * <p>The output argument is the delay line data output. This is
 * value mode and must be the same type as the data input.
 */
public class DelMod extends InbuiltMod implements Constant, TDEConstants {

    /**
     * Construct the inbuilt module del().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public DelMod () {
        ipnames.put("in", 0);
        ipnames.put("len", 1);
        ipnames.put("init", 2);
        ipnames.put("step", 3);
        ipnames.put("varlen", 4);
        ipnames.put("reset", 5);
        opnames.put("out", 0);
        allowed_attributes = true;
    }

    /**
     * Execute the module del().
     * @param   inargs is a list of input argument tree nodes
     * @param   outargs is a list of output argument tree nodes
     */
    public void execute (
        NodeList inargs,
        NodeList outargs
    ) {
        SrcLoc  loc = inargs.getCallLoc();
        if ((inargs.size() < 2) || (inargs.size() > 6))
            throw new ExEx("del() must have 2 to 6 input arguments", loc);
        if (outargs.size() != 1)
            throw new ExEx("del() buffer must have one output argument", loc);


        Val         ival = inargs.getVal(0);
        Val         lval = inargs.getVal(1);
        Val         stepval = null;
        Val         lenval = null;
        Val         rval = null;
        TDEVar      itdev = null;
        int         len = 0;
        String[]    init = null;
        TDEVar      steptdev = null;
        TDEVar      lentdev = null;
        TDEVar      rtdev = null;
        boolean     havelength = false;
        boolean     varlen = false;
        long        srladdrwidth = longDir("srlAddrWidth");
        int         blklen = 1 << srladdrwidth;
        
        // data input argument
        checkmode(ival, "del() 1st input", loc);
        itdev = ival.getTDEVar();
        ival.resolveClocks(null, Calloc.VALUE, loc);

        // output argument
        Ref     oref = outargs.getRef(0, "del()");
        if (oref.getMode() != Mode.VALUE)
            throw new ExEx("del() output argument must be value mode", loc);
        if (!oref.checkMatch(ival, true))
            throw new ExEx("del() data output and input types differ", loc);
        oref.resolveClocks(null, Calloc.ASSIGN, loc);
        
        // optional constant length argument
        if (lval != null) {
            if (lval.getMode() != Mode.IMMEDIATE)
                throw new ExEx("del() 2nd input argument not immediate mode", loc);
            if ((lval.getPrimType() != Ptype.INT) && (lval.getPrimType() != Ptype.UINT))
                throw new ExEx("del() 2nd input argument not type int", loc);
            len = (int)lval.getSingleIval(loc);
            havelength = true;
        }

        // optional step argument
        if (inargs.size() > 3) {
            stepval = inargs.getVal(3);
            if (stepval != null) {
                checkmode(stepval, "del() 4th input argument", loc);
                if (stepval.getPrimType() != Ptype.LOG)
                    throw new ExEx("del() 4th input argument not type log", loc);
                steptdev = stepval.getTDEVar();
            }
        }

        // optional variable length argument
        if (inargs.size() > 4) {
            lenval = inargs.getVal(4);
            if (lenval != null) {
                if (havelength)
                    throw new ExEx("del() cannot have both constant and variable length arguments", loc);
                checkmode(lenval, "del() 5th input argument", loc);
                if (lenval.getPrimType() != Ptype.UINT)
                    throw new ExEx("del() 5th input argument not type uint", loc);
                if (lenval.getWordSpec().numBits() > srladdrwidth)
                    throw new ExEx("del() 5th input argument wider than " + srladdrwidth + " bits", loc);
                lentdev = lenval.getTDEVar();
                varlen = true;
                len = blklen;
                lenval.resolveClocks(null, Calloc.VALUE, loc);
            }
        }

        // optional reset argument
        if (inargs.size() > 5) {
            rval = inargs.getVal(5);
            if (rval != null) {
                checkmode(rval, "del() 6th input argument", loc);
                if (rval.getPrimType() != Ptype.LOG)
                    throw new ExEx("del() 6th input argument not type log", loc);
                rtdev = rval.getTDEVar();
            }
        }
        
        // optional initialisation argument
        if (inargs.size() > 2) {
            WordSpec    ws = ival.getWordSpec();
            Val         initval = inargs.getVal(2);
            if (initval != null) {
                if (initval.getMode() != Mode.IMMEDIATE)
                    throw new ExEx("del() - initialisation value not immediate mode", loc);
                init = ws.packInit(initval, len, "del() - ", loc);
            }
        }
        
        if (varlen && (rtdev != null))
            throw new ExEx("del() cannot have variable length and reset signal", loc);
        if (varlen && (longDir("srlAddrWidth") == 0))
            throw new ExEx("del() cannot have variable length as this FPGA has no shift registers", loc);
        if (varlen && (lentdev.getWordSpec().numBits() >= longDir("srlAddrWidth")))
                throw new ExEx("del() variable shift to big for shift register", loc);

        // generate delay element
        TDEVar      clk = ThreePL.getCurrentClock();
        WordSpec    ws = ival.getWordSpec();
        TDEVar      otdev = tdelist.signal("E", ws, loc);
        TDE         tde = new TDE(TDEType.DEL);

        tde.add2p(varlen ? 0 : len);
        if (init != null)
            tde.add2p(init);
        tde.add2ic(clk);
        tde.add2i(itdev);
        if ((lentdev != null) || (steptdev != null) || (rtdev != null))
            tde.add2i(lentdev);
        if ((steptdev != null) || (rtdev != null))
            tde.add2i(steptdev);
        if (rtdev != null)
            tde.add2i(rtdev);
        tde.add2o(otdev);
        tdelist.addTDE(tde);
        
        Val retval = new Val(null, Mode.VALUE, otdev, loc);
        retval.addOVar(getCurrentClockVar()); // special case - clock Var itself
        oref.assignTo(retval, loc);
    }

    // check that signal inputs are not IMMEDIATE, QUEUE, CMEMORY, RMEMORY
    // or CLOCK variables. Make sure value variables do not
    // have queue expressions.
    private void checkmode (Val val, String mess, SrcLoc loc) {
        if (val == null)
            throw new ExEx(mess + " is null", loc);
        switch (val.getMode()) {
        case INPUT:
        case STATIC:
        case SELECTVALUE:
        case PRIORITY:
            return;
        case VALUE:
            if (!val.getQueues().isEmpty())
                throw new ExEx(mess + " contains queue reads", loc);
            return;
        default:
            throw new ExEx(mess + " not allowed mode", loc);
        }
    }
    
    /*
    private String adjust_bin (String s, int len, SrcLoc loc) {
        int slen = s.length();
        if (slen == len)
            return(s);
        if (slen < len)
            return(zeropad(len - slen) + s);
        else
            throw new ExEx("del() - initialisation value too large", loc);
    }
    */
}
