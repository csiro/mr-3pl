package threepl.funcs;

import java.util.TreeMap;

import threepl.exceptions.ExEx;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get an attribute from a variable.
 * The first argument is the variable identifier.
 * The second argument is the attribute key as a string.
 */
public class AttributeFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the number of map or list entries
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        if (args.size() != 2)
            throw new ExEx("attribute() must have two arguments", loc);
        
        Ref     ref = args.getRef(0, "attribute()");
        if (ref == null)
            throw new ExEx("attribute() 1st argument missing", loc);
        Var     var = ref.getVar();
        Val     amval = var.getAttributes(true, loc);
        TreeMap<String, Val> map = amval.getSingleMAPval(loc);

        Val     aval = args.getVal(1);
        String  attr = aval.getSingleSval(loc);

        if (map.containsKey(attr))
            return(map.get(attr));
        else
            return(new Val(loc));
    }
}
