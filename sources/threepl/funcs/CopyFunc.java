package threepl.funcs;

import java.util.ArrayList;
import java.util.TreeMap;

import threepl.exceptions.ExEx;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to return a shallow copy of a list or map.
 */
public class CopyFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the radian value
     */
    @SuppressWarnings("unchecked")
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        
        if (args.size() != 1)
            throw new ExEx("copy() - must have one argument", loc);

        Object[]    oa = new Object[1];
        Type[]      ta = new Type[1];
        Val         val = args.getVal(0);
        if (val.getMode() != Mode.IMMEDIATE)
            throw new ExEx("copy() argument not immediate mode", loc);
        switch (val.getPrimType()) {
        case LIST:
            ArrayList<Val>   al = new ArrayList<Val>();
            al.addAll((ArrayList<Val>)val.getVal(0));
            oa[0] = al;
            ta[0] = new Type(Ptype.LIST, 0);
            break;
        case MAP:
            TreeMap<String,Val> tm = new TreeMap<String, Val>();
            tm.putAll((TreeMap<String,Val>)val.getVal(0));
            oa[0] = tm;
            ta[0] = new Type(Ptype.MAP, 0);
            break;
        default:
            throw new ExEx("copy() argument not a list or map", loc);
        }
        return(new Val(oa, ta, ta[0].getWordSpec(null, loc), null, null, loc));
    }
}
