package threepl.funcs;

import java.util.ArrayList;
import java.util.TreeMap;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get an array dimension or the size of a
 * list or map.
 * This has 2 arguments. The 1st argument is the array. The 2nd
 * argument is the index of the dimension. For a ist or map
 * the second argument must be omitted.
 */
public class DimensionFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the array dimension
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if ((args.size() > 2) || (args.size() == 0))
            throw new ExEx("dimension() must have one or two arguments", loc);

        
        Val     val = args.getVal(0);

        if (val.getPrimType() == Ptype.MAP) {
            if ((args.size() != 1))
                throw new ExEx("dimension() can't have a second argument for a map", loc);
            @SuppressWarnings("unchecked")
            TreeMap<String,Val> tm = (TreeMap<String, Val>)val.getVal(0);
            return(new Val(tm.size(), loc));
        }
        if (val.getPrimType() == Ptype.LIST) {
            if ((args.size() != 1))
                throw new ExEx("dimension() can't have a second argument for a list", loc);
            @SuppressWarnings("unchecked")
            ArrayList<Val>   al = (ArrayList<Val>)val.getVal(0);
            return(new Val(al.size(), loc));
        }

        int[]   dims = val.getDimDes();

        if (dims == null)
            return(new Val(0, loc)); // not an array!

        int     i = 0;
        if (args.size() == 2)
            i = (int)args.getVal(1).getSingleIval(loc);

        if (i >= dims.length)
            throw new ExEx("dimension() index exceeds dimensionality", loc);
        
        return(new Val(dims[i], loc));
    }
}
