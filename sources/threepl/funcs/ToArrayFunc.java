package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to convert a simple or compound type to an array.
 */
public class ToArrayFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the identifier string
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() > 2)
            throw new ExEx("toarray() must have 1 or 2 arguments", loc);

        
        Val     val = args.getVal(0);
        if (val.getMode() != Mode.IMMEDIATE)
            throw new ExEx("toarray() 1st argument is not immediate", loc);

        Val     nval = null;
        long    n = 0;
        if (args.size() > 1) {
            nval = args.getVal(1);
            if (val.getMode() != Mode.IMMEDIATE)
                throw new ExEx("toarray() 2nd argument is not immediate", loc);
            n = nval.getSingleIval(loc);
        }

        Type[]  types = val.getValTypes();
        int     words = types.length;
        Ptype   ptype = types[0].getPrimType();
        
        if (n == 0)
            n = words;
        
        for (int i=1 ; i<words ; i++)
            if (ptype != types[i].getPrimType())
                throw new ExEx("toarray() argument is not a collection of a single primitive type", loc);
        if ((words != 1) && (words != n))     // different size array
            throw new ExEx("toarray() cannot expand an array to a different size", loc);
        
        if (n != 0)
            return(val.toArray(ptype, words==1, n, loc));
        
        return(new Val(val.getVals().clone(), ptype.name().toLowerCase(), loc));
    }
}
