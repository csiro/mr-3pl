package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.exec.WordSpec;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to convert a string into an array of integers.
 * The integer values are the ASCII codes for the string characters.
 */
public class StrToArrayFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the directive list array of structs
     */
    public Val getVal (NodeList args) {
        SrcLoc              loc = args.getCallLoc();
        
        if (args.size() != 1)
            throw new ExEx("strtoarray() must have one argument", loc);

        Val     sval = args.getVal(0);
        if (sval.getMode() != Mode.IMMEDIATE)
            throw new ExEx("strtoarray() argument not immediate mode", loc);
        if (sval.getPrimType() != Ptype.STR)
            throw new ExEx("strtoarray() argument not string type", loc);
        String  s = sval.getSingleSval(loc);
        int     len = s.length();
        char[]  c = s.toCharArray();

        Type        type = new Type("[" + len + "]uint", loc);
        Type        pt = new Type("uint", loc);
        WordSpec    ws = type.getWordSpec(null, loc);
        Long[]      sa = new Long[len];
        Type[]      ta = new Type[len];
        for (int i=0 ; i<len ; i++) {
            sa[i] = Long.valueOf(c[i]);
            ta[i] = pt;
        }
        
        return(new Val(sa, ta, ws, null, null, loc));
    }
}
