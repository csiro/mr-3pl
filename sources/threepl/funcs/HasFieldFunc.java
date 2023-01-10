package threepl.funcs;

import java.util.TreeMap;

import threepl.exceptions.ExEx;
import threepl.exec.Field;
import threepl.exec.Ref;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.nodes.Ident;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to determine if a type is a struct containing a
 * designated field. This has 2 arguments, the first being a variable
 * and the second a field identifier. If the first variable is
 * a type variable that type will be examined, otherwise the type
 * of the variable will be examined, i.e. you can pass a struct type
 * or a variable of struct type.
 * The value returned is type log.
 */
public class HasFieldFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the identifier string
     */
    public Val getVal (NodeList args) {
        SrcLoc                  loc = args.getCallLoc();
        TreeMap<String, Field>  fm;

        if (args.size() != 2)
            throw new ExEx("hasfield() must have 2 arguments", loc);

        Ident   id = args.getIdent(1, "hasfield()");
        String  fieldname = id.getId();
     
        Ref     ref = args.getRef(0, "hasfield()");
        Type    type = ref.getType();
        Ptype   ptype = type.getPrimType();
        
        if (ptype == Ptype.TYPE) {
            Val val = ref.getVal(loc);
            type = val.getSingleTval(loc);
        }
        
        fm = type.getFieldMap();
        if (fm != null)
            return(new Val(fm.containsKey(fieldname), loc));
        return(new Val(false, loc));
    }
}
