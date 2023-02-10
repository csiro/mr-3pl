package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to return the type of a target mode variable as an
 * immediate type. The type is a string in the same format as is used for
 * type declarations. Type will be converted to immediate type by removing
 * width fields. "bits" will be converted to "uint"
 */
public class TargToImTypeFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the type string
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("targtoimtype() must have single argument", loc);
        Val     val = args.getVal(0);
        String  si;

        if (val.getPrimType() == Ptype.STR) // is a string
            si = val.getSingleSval(loc);
        else if (val.getPrimType() == Ptype.TYPE)   // is a type
            si = val.getSingleTval(loc).getTypeString();
        else
            si = val.getTypeString();   // variable - get its type

        String  st = null;
        String  re1 = ":[0-9]+";
        String  re2 = "bits";
        
        st = si.replaceAll(re1, "");
        st = st.replaceAll(re2, "uint");
        return(new Val(st, loc));
    }
}
