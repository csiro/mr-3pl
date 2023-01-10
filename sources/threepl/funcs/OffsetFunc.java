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
 * An inbuilt function to get the offset of a value of primitive type
 * "fixed" or "ufixed". It also returns a zero offset for types "int"
 * or "uint". Other types result in a fatal error. The variable mode
 * must be selectvalue, value, static or queue, or immediate mode with
 * type "type", other modes giving a fatal error.
 * This has 1 argument, which is a value of primitive type. It may be
 * an array member or struct field.
 */
public class OffsetFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the offset of the primitive variable
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("offset() must have a single argument", loc);

        if (args.getNode(0) instanceof VarNode)
            return(offset(args.getRef(0, "offset()"), args, loc));
        else        
            return(offset(args.getVal(0), args, loc));
    }

    private Val offset (RefOrVal rov, NodeList args, SrcLoc loc) {
        if (!rov.isPrimitive())
            throw new ExEx("offset() argument not a primitive type", loc);

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
                if ((pt != Ptype.FIXED) && (pt != Ptype.UFIXED))
                    throw new ExEx("offset() argument is type \"type\" but that type is not \"fixed\" or \"ufixed\"", loc);        
                return(new Val(t.getFixOffset(), loc));
            }
            throw new ExEx("offset() argument immediate mode but is not type \"type\"", loc);        
        default:
            throw new ExEx("offset() argument not allowed target mode", loc);        
        }

        switch (rov.getPrimType()) {
        case INT:
        case UINT:
        case FIXED:
        case UFIXED:
            break;
        default:
            throw new ExEx("offset() argument not allowed type", loc);        
        }

        return(new Val(rov.getType().getFixOffset(), loc));
    }
}

