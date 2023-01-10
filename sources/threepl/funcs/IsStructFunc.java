package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Ref;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to determine if a type is a struct.  This
 * has 1 argument, the variable.
 * The value returned is type log.
 */
public class IsStructFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the identifier string
     */
    public Val getVal (NodeList args) {
        SrcLoc                  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("isstruct() must have 1 argument", loc);
     
        Ref     ref = args.getRef(0, "isstruct()");
        Type    type = ref.getType();
        Ptype   ptype = type.getPrimType();
        
        if (ptype == Ptype.TYPE) {
            Val val = ref.getVal(loc);
            type = val.getSingleTval(loc);
        }
        
        return(new Val(type.getFieldMap() != null, loc));
    }
}
