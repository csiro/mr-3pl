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
 * An inbuilt function to get the width of the biased exponent of a value of
 * primitive type "float:m.e". Other types result in a fatal error. This has
 * 1 argument which may be a target mode whose type is a target float or it
 * may be an immediate type variable where the type value is target float.
 * Target modes allowed are selectvalue, value, static or queue mode, other
 * modes giving a fatal error. The width of the biased exponent is returned
 * as an immediate "uint".
 */
public class BiasedExponentWidthFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the offset of the primitive variable
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("biasedexponentwidth() must have a single argument", loc);

        if (args.getNode(0) instanceof VarNode)
            return(exponent(args.getRef(0, "biasedexponentwidth()"), args, loc));
        else        
            return(exponent(args.getVal(0, true), args, loc));
    }

    private Val exponent (RefOrVal rov, NodeList args, SrcLoc loc) {
        if (!rov.isPrimitive())
            throw new ExEx("biasedexponentwidth() argument not a primitive type", loc);

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
                if (pt != Ptype.FLOAT)
                    throw new ExEx("biasedexponentwidth() argument is type \"type\" but that type is not \"float\"", loc);        
                return(new Val(t.getExponentWidth(), loc));
            }
            throw new ExEx("biasedexponentwidth() argument immediate mode but is not type \"type\"", loc);        
        default:
            throw new ExEx("biasedexponentwidth() argument not allowed target mode", loc);        
        }

        if (rov.getPrimType() != Ptype.FLOAT)
            throw new ExEx("biasedexponentwidth() argument not type \"float\"", loc);        

        return(new Val(rov.getType().getExponentWidth(), loc));
    }
}

