package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.RefOrVal;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.nodes.VarNode;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to determine if a type is signed. It returns true
 * for types INT and FIXED. The variable mode must be selectvalue,
 * value, static or queue, or immediate mode with type "type", other
 * modes giving a fatal error.
 * This has 1 argument, which is a value of primitive type. It may be
 * an array member or struct field.
 */
public class IsSignedFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the offset of the primitive variable
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("issigned() must have a single argument", loc);

        if (args.getNode(0) instanceof VarNode)
            return(is_signed(args.getRef(0, "issigned()"), args, loc));
        else        
            return(is_signed(args.getVal(0), args, loc));
    }

    private Val is_signed (RefOrVal rov, NodeList args, SrcLoc loc) {
        if (!rov.isPrimitive())
            throw new ExEx("issigned() argument not a primitive type", loc);

        switch (rov.getMode()) {
        case SELECTVALUE:
        case VALUE:
        case STATIC:
        case QUEUE:
            break;
        case IMMEDIATE:
            if (rov.getPrimType() == Ptype.TYPE) {
                Val val = args.getVal(0);
                Type    t = val.getSingleTval(loc);
                Ptype   pt = t.getPrimType();
                return(new Val((pt == Ptype.INT) || (pt == Ptype.FIXED), loc));
            }
            throw new ExEx("issigned() argument immediate mode but is not type \"type\"", loc);        
        default:
            throw new ExEx("issigned() argument not allowed target mode", loc);        
        }

        Ptype   ptype = rov.getPrimType();
        return(new Val((ptype == Ptype.INT) || (ptype == Ptype.FIXED), loc));
    }
}

