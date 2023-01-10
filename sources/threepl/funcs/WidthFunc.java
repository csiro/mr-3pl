package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.nodes.VarNode;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the width of a value of primitive type.
 * This has 1 argument, which is the value of primitive type. If the
 * argument is a target variable the result is the width of the
 * variable, array member or field. If the argument is an immediate
 * variable or expression the result is the number of bits required
 * to represent the value.
 */
public class WidthFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the width of the primitive variable
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        Val     val;

        if (args.size() != 1)
            throw new ExEx("width() must have a single argument", loc);

        if (args.getNode(0) instanceof VarNode) {
            Ref ref = args.getRef(0, "width()");

            if (!ref.isPrimitive())
                throw new ExEx("width() argument not a primitive type", loc);
            if ((ref.getMode() == Mode.CMEMORY) || (ref.getMode() == Mode.RMEMORY) || (ref.getMode() == Mode.CLOCK))
                throw new ExEx("width() argument not allowed target mode", loc);        

            if (ref.getMode() != Mode.IMMEDIATE)
                return(new Val(ref.getWordSpec().getWidth(0), loc));
            val = ref.getVal(loc);
        } else        
            val = args.getVal(0);

        if (val.getPrimType() == Ptype.LOG)
            return(new Val(1, loc));
        if ((val.getPrimType() != Ptype.UINT) && (val.getPrimType() != Ptype.INT))
            throw new ExEx("width() argument is not an integer or log", loc);
        long    i = val.getSingleIval(loc);
        int     j = 0;
        if (i == 0)
            throw new ExEx("width() argument is immediate 0", loc);
        if (i < 0) {
            i = -1 - i;
            j = 1;
        }
        while (i != 0) {
            i >>= 1;
            j++;
        }
        return(new Val(j, loc));
    }
}
