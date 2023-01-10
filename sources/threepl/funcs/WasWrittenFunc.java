package threepl.funcs;

import static threepl.ThreePL.getCurrentClock;
import static threepl.ThreePL.getCurrentClockVar;
import static threepl.ThreePL.tdelist;
import static threepl.codegen.TDEVar.*;

import threepl.codegen.TDE;
import threepl.codegen.TDEConstants.TDEType;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Clock;
import threepl.exec.Queue;
import threepl.exec.Ref;
import threepl.exec.Static;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.exec.WordSpec;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to generate a pulse after a static or queue variable
 * is written. For a static variable this may be applied to one or more
 * components in the case of a compound type. For a queue variable the whole
 * variable is always written so the argument should not have fields or
 * subscripts.
 */
public class WasWrittenFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the offset of the primitive variable
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("waswritten() must have a single argument", loc);

        Ref         ref = args.getRef(0, "waswritten()");
        WordSpec    ws = ref.getWordSpec();
        Var         var = ref.getVar();
        TDEVar      write = tdelist.signal("W", WordSpec.TLOG, loc);
        Clock       wclkvar = getCurrentClockVar();
        TDEVar      wclk = getCurrentClock();

        switch (ref.getMode()) {
        case STATIC:
            Static  s = (Static)var;
            s.setInputClock(wclkvar, Calloc.WASWRITTEN, loc);
            if (ref.getSubFields().size() == 0) {
                // whole variable - no subscripts or fields
                tdelist.fdrse(write, s.getWrites(), wclk, VCC, GND, GND, "R", loc);
                return(new Val(var, Mode.VALUE, write, loc));
            } else if (ws.numWords() == 1) {
                // single subscript or field
                TDEVar              tdev = s.getWrite(ws.getWord(0));
                tdelist.fdrse(write, tdev, wclk, VCC, GND, GND, "R", loc);
                return(new Val(var, Mode.VALUE, write, loc));
            } else {
                // multiple subscripts or fields
                TDE                 or = new TDE(TDEType.OR, loc);
                for (int i : ws.getWords())
                    or.add2i(s.getWrite(i));
                tdelist.fdrse(write, or.finish(), wclk, VCC, GND, GND, "R", loc);
                return(new Val(var, Mode.VALUE, write, loc));
            }
        case QUEUE:
            Queue    p = (Queue)var;
            p.setInputClock(wclkvar, Calloc.ISWRITTEN, loc);
            if (!ref.getSubFields().isEmpty())
                throw new ExEx("waswritten() queue argument should not have subscripts or fields", loc);
            /*tdelist.connect(write, p.getWrites());*/
            tdelist.fdrse(write, p.getWrites(), wclk, VCC, GND, GND, "R", loc);
            return(new Val(var, Mode.VALUE, write, loc));
        default:
            throw new ExEx("waswritten() argument not allowed target mode", loc);        
        }
    }
}

