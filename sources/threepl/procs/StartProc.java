package threepl.procs;

import static threepl.ThreePL.*;

import threepl.ThreePL;
import threepl.codegen.TDE;
import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Clock;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.exec.WordSpec;
import threepl.netlist.xilinx.XC2C;
import threepl.netlist.xilinx.XC95;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.Functions;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to drive the execution start level signal.
 * This has 1 or 2 input arguments and no output arguments. The 1st input
 * argument is either a primitive variable of mode 'value' and type 'log' which is
 * to drive the global execution start signal or an immediate constant specifying
 * a delay.
 * The optional 2nd input argument is a clock.
 */
public class StartProc extends InbuiltProc implements Constant, TDEConstants {

    /**
     * Construct the inbuilt procedure start().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public StartProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
    }

    /**
     * Execute the procedure start().
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
        SrcLoc loc = inargs.getCallLoc();
        if ((inargs.size() == 0) || (inargs.size() >= 2))
            throw new ExEx("start() must have 1 or 2 input arguments", loc);
        if (outargs.size() != 0)
            throw new ExEx("start() must have no output arguments", loc);
        if ((family instanceof XC2C) || (family instanceof XC95))
            throw new ExEx("start() cannot be used for XC2C or XC95 CPLDs", loc);
        Val val = inargs.getVal(0);
        if (val.numWords() != 1)
            throw new ExEx("start() 1st argument must be a single value", loc);
        Clock   clock;
        if (inargs.size() == 2) {
            Val cv = inargs.getVal(1);
            if (cv.getMode() == Mode.CLOCK)
                throw new ExEx("start() 2nd argument must be clock mode", loc);
            clock = (Clock)cv.getVar();
        } else
            clock = ThreePL.getCurrentClockVar();
        if (clock == null)
            throw new ExEx("start() - no current clock domain", loc);
        
        if (val.getMode() == Mode.IMMEDIATE) {
            // Current clock domain for primary start pulse and any associated
            // logic.
            long    cycles;
            if ((val.getPrimType() == Ptype.UINT) || (val.getPrimType() == Ptype.INT)) {
                // delay an integer number of clock cycles
                cycles = val.getSingleIval(loc);
            } else if (val.getPrimType() == Ptype.FLOAT) {
                // delay is a time in second
                double  delay = val.getSingleFval(loc);
                double  clkperiod = clock.getClkPeriod(loc);
                cycles = (long)(delay / clkperiod);
            } else
                throw new ExEx("start() immediate 1st argument is not type uint, int or float", loc);
            
            int         width = Functions.bits(cycles, false);
            WordSpec    ws = new WordSpec(width, 0, Ptype.UINT);
            TDE         reg = new TDE(TDEType.REG);
            TDE         m1 = new TDE(TDEType.OPERATOR);
            TDE         eqz = new TDE(TDEType.OPERATOR);
            TDE         _eqz = new TDE(TDEType.OPERATOR);
            TDEVar      count = tdelist.signal("start", ws, loc);
            TDEVar      _count = tdelist.signal("start", ws, loc);
            TDEVar      eqzero = tdelist.signal("start", loc);
            TDEVar      _eqzero = tdelist.signal("start", loc);
            reg.add2p((String[])null);
            reg.add2p(Functions.binToHex(Long.toBinaryString(cycles)));
            reg.add2p(false);
            reg.add2p(false);
            reg.add2ic(clock.getClkSig());
            reg.add2i(_count);
            reg.add2i(_eqzero);
            reg.add2i(TDEVar.GND);
            reg.add2o(count);
            tdelist.addTDE(reg);
            m1.add2p(TDEOp.SUB);
            m1.add2p(false);
            m1.add2p(false);
            m1.add2i(count);
            m1.add2i(new TDEVar((long)1, ws, 0, loc));
            m1.add2o(_count);
            tdelist.addTDE(m1);
            eqz.add2p(TDEOp.EQ);
            eqz.add2p(false);
            eqz.add2p(false);
            eqz.add2i(count);
            eqz.add2i(new TDEVar((long)0, ws, 0, loc));
            eqz.add2o(eqzero);
            tdelist.addTDE(eqz);
            _eqz.add2p(TDEOp.INV);
            _eqz.add2p(false);
            _eqz.add2i(eqzero);
            _eqz.add2o(_eqzero);
            tdelist.addTDE(_eqz);
            startval_tdev = eqzero;
        } else {
            if ((val.getMode() != Mode.VALUE) && (val.getMode() != Mode.INPUT))
                throw new ExEx("start() 1st argument must be mode IMMEDIATE, VALUE or INPUT", loc);
            if (val.getPrimType() != Ptype.LOG)
                throw new ExEx("start() target mode 1st argument must be type log", loc);
            startval_tdev = val.getTDEVar();
            Var var = val.getVar();
            if (var != null)
                var.setUsed();
        }
   }
}
