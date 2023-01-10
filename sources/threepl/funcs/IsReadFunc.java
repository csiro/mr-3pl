package threepl.funcs;

import static threepl.ThreePL.getCurrentClockVar;
import static threepl.ThreePL.tdelist;

import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Clock;
import threepl.exec.Queue;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.exec.WordSpec;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to generate a pulse when a queue variable is
 * read. The the argument must not have fields or subscripts.
 */
public class IsReadFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the offset of the primitive variable
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("isread() must have a single argument", loc);

        Ref         ref = args.getRef(0, "isread()");
        Var         var = ref.getVar();
        TDEVar      write = tdelist.signal("W", WordSpec.TLOG, loc);
        Clock       wclkvar = getCurrentClockVar();
        if (ref.getMode() != Mode.QUEUE)
            throw new ExEx("isread() argument not allowed queue mode", loc);        
        Queue    p = (Queue)var;
        p.setOutputClock(wclkvar, Calloc.ISREAD, true, loc);
        if (!ref.getSubFields().isEmpty())
            throw new ExEx("isread() queue argument should not have subscripts or fields", loc);
        /*tdelist.connect(write, p.getWrites());*/
        tdelist.connect(write, p.getReads());
        return(new Val(var, Mode.VALUE, write, loc));
    }
}

