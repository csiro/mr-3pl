package threepl.funcs;

import static threepl.ThreePL.tdelist;

import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.exec.WordSpec;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to determine if there is a next ordinal of an
 * enumerated type value. This has 1 argument which must be
 * of enumerated type. The value returned is type log.
 */
public class HasNextFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the identifier string
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("hasnext() must have 1 argument", loc);

        
        Val     val = args.getVal(0);
        Mode    mode = val.getMode();
        Type    type = val.getType();
        Ptype   ptype = type.getPrimType();
        if (ptype != Ptype.ENUM)
            throw new ExEx("hasnext() argument is not an enumerated type", loc);
        switch (mode) {
        case IMMEDIATE:
            long    ord = val.enumOrd(loc);
            Type    t = val.getType();
            return(new Val(t.hasNextEnumOrd(ord, loc), loc));
        case VALUE:
        case SELECTVALUE:
        case STATIC:
        case QUEUE:
            WordSpec    lws = new WordSpec(1, Ptype.LOG);
            long        l = type.lastEnumOrd();
            TDEVar      dt = tdelist.signal("S", lws, loc);
            tdelist.decode(l, val.getTDEVar(), dt, loc);
            TDEVar      ndt = tdelist.inv(dt, loc);
            ndt.setWordSpec(lws);
            Val retval = new Val(null, Mode.VALUE, ndt, loc);
            retval.andSetQueues(val.getQueues(), loc);
            return(retval);
        default:
            throw new ExEx("hasnext() argument mode " + mode.modename() + " not allowed", loc);
        }
    }
}
