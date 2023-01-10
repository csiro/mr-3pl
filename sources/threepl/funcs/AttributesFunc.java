package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the attribute map of a variable.
 * The first argument is the variable identifier.
 * The second argument is an optional flag which if true includes the
 * extended attributes.
 */
public class AttributesFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the number of map or list entries
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        boolean extended = false;

        if ((args.size() < 1) || (args.size() > 2))
            throw new ExEx("attributes() must have one or two arguments", loc);
        
        //Val val = args.getVal(0, true); DOESN'T WORK WITH POINTER TO OUTPUT MODE VARIABLE
        Ref ref = args.getRef(0, "");
        //Var var = val.getVar();
        Var var = ref.getVar();
        if (args.size() == 2) {
            Val eval = args.getVal(1);
            if (eval.getPrimType() != Ptype.LOG)
                throw new ExEx("attributes() second argument must be type \"log\"", loc);
            extended = eval.getSingleLval(loc);
        }
        return(var.getAttributes(extended, loc));
    }
}
