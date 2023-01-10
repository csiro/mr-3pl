package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Ref;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the first value of an enumerated
 * type. This has 1 argument which must be either an enumerated
 * type or else a value of enumerated type. The value returned is
 * type enum.
 */
public class FirstFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the identifier string
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("first() must have 1 argument", loc);

        
        Ref     ref = args.getRef(0, "first()");
        Type    t = null;
        switch (ref.getPrimType()) {
        case TYPE:
            Val val = ref.getVal(loc);
            t = val.getSingleTval(loc);
            break;
        case ENUM:
            t = ref.getType();
            break;
        default:
            throw new ExEx("first() argument is not an enumerated type", loc);
        }
        return(new Val(t.firstEnumOrd(), t, loc));
    }
}
