package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.exec.WordSpec;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to convert an array of integers into a string.
 * The integer values are the ASCII codes for the string characters.
 */
public class ArrayToStrFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  a string value
     */
    public Val getVal (NodeList args) {
        SrcLoc      loc = args.getCallLoc();
        
        if (args.size() != 1)
            throw new ExEx("arraytostr() must have one argument", loc);

        Val     sval = args.getVal(0);
        if (sval.getMode() != Mode.IMMEDIATE)
            throw new ExEx("arraytostr() argument not immediate mode", loc);       
        Type    at = sval.getArrayType();     
	if (at == null)
            throw new ExEx("arraytostr() argument not integer array", loc);
        Ptype   pat = at.getPrimType();
        if ((pat != Ptype.INT) && (pat != Ptype.UINT))
            throw new ExEx("arraytostr() argument not integer array", loc);
        int n = sval.getVals().length;
        char[]  ca = new char[n];
        for (int i=0 ; i<n ; i++) {
            long l = (long)sval.getVal(i);
            ca[i] = (char)l;
        }
        String      s = new String(ca);
        Type        type = new Type("str", loc);
        WordSpec    ws = type.getWordSpec(null, loc);
        String[]    sa = new String[1];
        Type[]      ta = new Type[1];
        sa[0] = s;
        ta[0] = type;
        
        return(new Val(sa, ta, ws, null, null, loc));
    }
}
