package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the number of dimensions of an array.
 * This has one argument, which is the array.
 */
public class DimensionsFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the array dimensionality
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("dimensions() must have single argument", loc);
        
        //Ref ref = args.getRef(0, "dimensions()");
        //int dims = ref.getDimDes().length;
        Val val = args.getVal(0, true);
        int[]   dim_des = val.getDimDes();
        if (dim_des == null)
            return(new Val(0, loc));
        return(new Val(dim_des.length, loc));
    }
}
