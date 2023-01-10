package threepl.funcs;

import java.util.ArrayList;
import java.util.TreeMap;

import threepl.codegen.TDEConstants;
import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to test a lst or map argument to see if it is empty.
 */
public class IsEmptyFunc extends InbuiltFunc implements Constant, TDEConstants {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  true if the argument is a target constant
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("isempty() must have a single argument", loc);
        Val     val = args.getVal(0);
        if (val == null)
            throw new ExEx("isempty() has null argument", loc);
        Ptype   ptype = val.getPrimType();
        switch (ptype) {
        case LIST:
            @SuppressWarnings("unchecked") ArrayList<Object> al = (ArrayList<Object>)val.getVal(0);
            return(new Val(al.isEmpty(), loc));
        case MAP:
            @SuppressWarnings("unchecked") TreeMap<String,Object> tm = (TreeMap<String,Object>)val.getVal(0);
            return(new Val(tm.isEmpty(), loc));
        default:
            throw new ExEx("isempty() argument not list or map", loc);
        }
    }
}
