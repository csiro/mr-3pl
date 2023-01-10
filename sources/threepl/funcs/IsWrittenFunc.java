package threepl.funcs;

import static threepl.ThreePL.getCurrentClockVar;
import static threepl.ThreePL.tdelist;

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
 * An inbuilt function to generate a pulse when a static or queue variable
 * is written. For a static variable this may be applied to one or more
 * components in the case of a compount type. For a queue variable the whole
 * variable is always written so the argument should not have fields or
 * subscripts.
 */
public class IsWrittenFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the offset of the primitive variable
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("iswritten() must have a single argument", loc);

        Ref         ref = args.getRef(0, "iswritten()");
        WordSpec    ws = ref.getWordSpec();
        Var         var = ref.getVar();
        TDEVar      write = tdelist.signal("W", WordSpec.TLOG, loc);
        Clock       wclkvar = getCurrentClockVar();
        switch (ref.getMode()) {
        case STATIC:
            Static  s = (Static)var;
            s.setInputClock(wclkvar, Calloc.ISWRITTEN, loc);
            if (ref.getSubFields().size() == 0) {
                // whole variable - no subscripts or fields
                return(new Val(var, Mode.VALUE, s.getWrites(), loc));
            } else if (ws.numWords() == 1) {
                // single subscript or field
                return(new Val(var, Mode.VALUE, s.getWrite(ws.getWord(0)), loc));
            } else {
                // multiple subscripts or fields
                TDE or = new TDE(TDEType.OR, loc);
                for (int i : ws.getWords())
                    or.add2i(s.getWrite(i));
                tdelist.connect(write, or.finish());
                return(new Val(var, Mode.VALUE, write, loc));
            }
        case QUEUE:
            Queue    p = (Queue)var;
            p.setInputClock(wclkvar, Calloc.ISWRITTEN, loc);
            if (!ref.getSubFields().isEmpty())
                throw new ExEx("iswritten() queue argument should not have subscripts or fields", loc);
            /*tdelist.connect(write, p.getWrites());*/
            tdelist.connect(write, p.getWrites());
            return(new Val(var, Mode.VALUE, write, loc));
        default:
            throw new ExEx("iswritten() argument not allowed target mode", loc);        
        }
    }
}

