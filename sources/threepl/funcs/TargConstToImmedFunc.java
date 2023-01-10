package threepl.funcs;

import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get an immediate constant from a value argument.
 */
public class TargConstToImmedFunc extends InbuiltFunc implements Constant, TDEConstants {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the immediate constant
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("targconsttoimmed() must have single argument", loc);
        Ref ref = args.getRef(0, "targconsttoimmed()");
        if (ref.getMode() != Mode.VALUE)
            throw new ExEx("targconsttoimmed() argument is not value mode", loc);
        if (ref.getPrimType() == Ptype.NONE)
            throw new ExEx("targconsttoimmed() argument is not a primitive type", loc);
        Val     val = args.getVal(0);
        TDEVar  tdev = val.getTDEVar();
        Val     rval = tdev.getConstVal();
        if (rval == null)
            throw new ExEx("targconsttoimmed() argument is not a constant", loc);
        return(rval);
    }
}
