package threepl.mods;

import static threepl.ThreePL.getCurrentClock;
import static threepl.ThreePL.getCurrentClockVar;
import static threepl.ThreePL.tdelist;

import threepl.codegen.TDE;
import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.exec.WordSpec;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * <p>An inbuilt module to resynchronise a pulse (or level)
 * from one clock domain to another.
 *
 * <p>This has one input argument and one output argument. The input
 * argument is value, selectvalue, static, input or priority mode of type
 * log. The output argument is a value variable of type log.
 *
 * <p>When the input rises and is sampled by the clock associated
 * with that variable, the output will produce a single cycle pulse
 * synchronised with the clock associated with the output argument.
 *
 * <p>This is used to clock data to a static or queue variable where
 * the data source has a different clock. The input is a signal that
 * indicates that a data source has received data. The output of this
 * procedure can be used to cause a write of that data to a static or queue.
 * Note that the validity of this depends on the relative clock rates
 * and the input duty cycle. It will not function correctly if the input
 * data is continuous, relying on at least one idle cycle after each datum.
 * If the output clock is slower than the input clock then the input data
 * must be more widely spaced than every 2nd cycle.
 *
 * <p>This is a simple device to avoid the use of a full dual-port
 * resynchronising queue buffer where the data is known to be sparse.
 */
public class ResyncMod extends InbuiltMod implements Constant, TDEConstants {

    /**
     * Construct the inbuilt module resync().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public ResyncMod () {
    }

    /**
     * Execute the module resync().
     * @param   inargs is a list of input argument tree nodes
     * @param   outargs is a list of output argument tree nodes
     */
    public void execute (
        NodeList    inargs,
        NodeList    outargs
    ) {
        SrcLoc  loc = inargs.getCallLoc();
        TDEVar  oclock = getCurrentClock();
        if (inargs.size() != 1)
            throw new ExEx("resync() must have 1 input argument", loc);
        if (outargs.size() != 1)
            throw new ExEx("resync() must have 1 output argument", loc);

        Val     ival = inargs.getVal(0);
        if (!ival.isPrimitive())
            throw new ExEx("resync() argument not a primitive type", loc);
        if (!ival.getQueues().isEmpty())
            throw new ExEx("resync() input argument cannot have queue reads", loc);
        if ((ival.getMode() == Mode.CMEMORY) || (ival.getMode() == Mode.RMEMORY))
            throw new ExEx("resync() input argument cannot be a memory mode", loc);
        if (ival.getMode() == Mode.CLOCK)
            throw new ExEx("resync() input argument cannot be clock mode", loc);
        if (ival.getPrimType() != Ptype.LOG)
            throw new ExEx("resync() input argument must be type log", loc);
        Var     iclockvar = ival.collectClocks(loc);
        if (iclockvar == null)
            throw new ExEx("resync() input argument has no clock domain", loc);
        TDEVar  iclock = iclockvar.getClkSig();

        Ref     oref = outargs.getRef(0, "resync() -");
        if (oref.getMode() != Mode.VALUE)
            throw new ExEx("resync() output argument must be value mode", loc);
        if (!oref.isPrimitive() || (oref.getPrimType() != Ptype.LOG))
            throw new ExEx("resync() output argument must be type log", loc);

        TDEVar      isig = ival.getTDEVar();
        WordSpec    ws = new WordSpec(1, Ptype.LOG);
        TDEVar      osig = tdelist.signal("E", ws, loc);
        Val         rval = new Val(null, Mode.STATIC, osig, loc); // mode is a dummy
        rval.addOVar(getCurrentClockVar()); // special case - clock Var itself
        oref.assignTo(rval, loc);
        TDE     resync = new TDE(TDEType.RESYNC, loc);

        resync.add2i(isig);
        resync.add2ic(iclock);
        resync.add2ic(oclock);
        resync.add2o(osig);

        tdelist.addTDE(resync);
    }
}
