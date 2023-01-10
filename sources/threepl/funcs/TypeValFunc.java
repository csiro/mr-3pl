package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.exec.WordSpec;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to return the type value of a type variable.
 */
public class TypeValFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the type
     */
    public Val getVal (NodeList args) {
        SrcLoc loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("typeval() must have single argument", loc);
        
        Type    type;

        Val     val = args.getVal(0);
        if (val.getPrimType() == Ptype.STR)
            type = new Type(val.getSingleSval(loc), loc);
        else
            type = val.getSingleTval(loc);
        Val     retval = new Val(type, loc);
        WordSpec    ws = retval.getWordSpec();
        ws.setCheckType(type);
        return(retval);
    }
}
