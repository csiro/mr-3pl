package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to return the type of an immediate mode variable as
 * a target type. The type is a string in the same format as is used for
 * type declarations. Type will be converted to target type using widths
 * derived from the immediate value(s).
 */
public class ImToTargTypeFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the type string
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("imtotargtype() must have single argument", loc);
        Val     val = args.getVal(0);
        if (val.getMode() != Mode.IMMEDIATE)
            throw new ExEx("imtotargtype() - argument not immediate mode", loc);
        String  si = val.getTypeString();
        String  st = null;
        
        //
        if (si != null) // DUMMY CONDITIONAL TO FORCE EXCEPTION - DELETE WHEN CODE WRITTEN
            throw new ExEx("imtotargtype() NOT YET IMPLEMENTED!", loc);

        return(new Val(st, loc));
    }
}
