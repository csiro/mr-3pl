package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the ordinal of an enumerated type value
 * or a character.
 * This has 1 argument which must be of enumerated type or a string or
 * length 1.
 * If the argument is immediate an immediate int is returned (actually
 * a uint).
 * If the argument is mode value, selectvalue, static or queue a value
 * mode result of type uint is returned.
 */
public class OrdFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the identifier string
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("ord() must have 1 argument", loc);

        
        Val     val = args.getVal(0);
        Ptype   ptype = val.getPrimType();
        switch (ptype) {
        case ENUM:
            Mode    mode = val.getMode();
            switch (mode) {
            case IMMEDIATE:
                return(new Val(val.enumOrd(loc), loc));
            case VALUE:
            case SELECTVALUE:
            case STATIC:
            case QUEUE:
                int width = val.getSingleNumBits();
                return(val.cast(new Type(Ptype.UINT, width, 0, 0, 0, null, null), "ord()", loc));
            default:
                throw new ExEx("ord() argument mode " + mode.modename() + " not allowed", loc);
            }
        case STR:
            String  s = val.getSingleSval(loc);
            if (s.length() != 1)
                throw new ExEx("ord() string argument not a single character", loc);
            char    c = s.charAt(0);
            return(new Val(c, loc));
        default:
            throw new ExEx("ord() argument not an enum or string (" + ptype.typename() + ")", loc);
        }
    }
}
