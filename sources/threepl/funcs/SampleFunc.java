package threepl.funcs;

import static threepl.ThreePL.getCurrentClockVar;
import static threepl.ThreePL.tdelist;

import threepl.codegen.TDE;
import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.exec.WordSpec;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to make an argument acceptable to a clock domain.
 * The first or only argument is the target value to be accepted. It
 * cannot contain queues. If there is only one argument the relevant clock
 * domain is the current clock domain. If a second argument is provided
 * it must be a clock variable which gives the clock domain into which
 * the first argument is to be accepted.
 */
public class SampleFunc extends InbuiltFunc implements Constant, TDEConstants {
    private         SrcLoc  loc;
    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the clock frequency
     */
    public Val getVal (NodeList args) {
        loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("sample() must have 1 argument", loc);

        Val     val = args.getVal(0);
        TDEVar  tdevi = val.getTDEVar();
        if (!val.getQueues().isEmpty())
            throw new ExEx("sample() 1st argument contains queue reads", loc);
        if ((val.getMode() == Mode.CMEMORY) || (val.getMode() == Mode.RMEMORY))
            throw new ExEx("sample() 1st argument is a memory variable", loc);
        if (val.getMode() == Mode.CLOCK)
            throw new ExEx("sample() 1st argument is a clock variable", loc);
        Var iclockvar = val.collectClocks(loc);
        if (iclockvar == null)
            throw new ExEx("sample() input argument has no clock domain", loc);
        TDEVar  iclock = iclockvar.getClkSig();
        Var oclockvar = getCurrentClockVar(); // destination clock
        if (iclockvar == oclockvar)
            return(val);    // argument is already in current clock domain
        if (oclockvar == null)
            throw new ExEx("sample() current clock domain is null", loc);
        TDEVar      oclock = oclockvar.getClkSig();
        WordSpec    ws = val.getWordSpec();
        TDE     sample = new TDE(TDEType.SAMPLE, loc);
        TDEVar  out = tdelist.signal("SAMPLEOUT", ws, loc);
        sample.add2i(tdevi);
        sample.add2ic(iclock);
        sample.add2ic(oclock);
        sample.add2o(out);
        tdelist.addTDE(sample);
        Val     oval = new Val(null, Mode.VALUE, out, loc);
        oval.addOVar(oclockvar);
        return(oval);
    }
    
    /*
     private void fdrs (
        TDEVar  o,
        TDEVar  d,
        TDEVar  c,
        TDEVar  ce,
        TDEVar  r,
        TDEVar  s,
        String  tnm
    ) {
        TDEVar  q = tdelist.fdrs(d, c, ce, r, s, tnm, loc);
        tdelist.connect(o, q);
    }
    
    private TDEVar inv (TDEVar in) {
        return(tdelist.inv(in, loc));
    }
    */
}
