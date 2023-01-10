package threepl.funcs;

import java.util.TreeMap;

import threepl.exceptions.ExEx;
import threepl.exec.Field;
import threepl.exec.Ref;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to determine the number of fields in a struct. This
 * has 1 argument, the variable.
 * The value returned is type int.
 */
public class NumFieldsFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the identifier string
     */
    public Val getVal (NodeList args) {
        SrcLoc                  loc = args.getCallLoc();
        TreeMap<String, Field>  fm;

        if (args.size() != 1)
            throw new ExEx("numfields() must have 1 argument", loc);
     
        Ref     ref = args.getRef(0, "numfields()");
        Type    type = ref.getType();
        Ptype   ptype = type.getPrimType();
        
        if (ptype == Ptype.TYPE) {
            Val val = ref.getVal(loc);
            type = val.getSingleTval(loc);
        }
        
        fm = type.getFieldMap();
        if (fm == null)
            throw new ExEx("numfields() argument is not a struct", loc);
        return(new Val(fm.size(), loc));
    }
}
