package threepl.funcs;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get an entry from a list or map.
 * This has two arguments. The first is the list or map variable.
 * The second is an index into a list or map or a key into a map.
 */
public class GetFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the number of map or list entries
     */
    @SuppressWarnings("unchecked")
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 2)
            throw new ExEx("get() must have two arguments", loc);
        
        int     index;
        String  key;
        Val     rval;
        Val     val = args.getVal(0);
        Val     val2 = args.getVal(1);
        if (val == null)
            throw new ExEx("get() - 1st argument is missing", loc);
        if (val2 == null)
            throw new ExEx("get() - 2nd argument is missing", loc);
        switch (val.getPrimType()) {
        case LIST:
            ArrayList<Val>   al = (ArrayList<Val>)val.getVal(0);
            switch (val2.getPrimType()) {
            case INT:
            case UINT:
                index = (int)val2.getSingleIval(loc);
                if ((index < 0) || (index >= al.size()))
                    throw new ExEx("get() - index is out of range (" + index + " >= " + al.size() + ")", loc);
                rval = al.get(index);
                if (rval == null)
                    return(new Val(loc));   // null
                else
                    return(rval);
            default:
                throw new ExEx("get() - 2nd argument for a list is not integer type", loc);
            }
        case MAP:
            TreeMap<String,Val> tm = (TreeMap<String, Val>)val.getVal(0);
            switch (val2.getPrimType()) {
            case INT:
            case UINT:
                index = (int)val2.getSingleIval(loc);
                Set<String> ks = tm.keySet();
                Object[] oa = ks.toArray();
                if ((index < 0) || (index >= oa.length))
                    throw new ExEx("get() - index is out of range (" + index + " >= " + oa.length + ")", loc);
                return(new Val((String)oa[index], loc));
            case STR:
                key = val2.getSingleSval(loc);
                if (key == null)
                    throw new ExEx("get() - key is null", loc);
                rval = tm.get(key);
                if (rval == null)
                    return(new Val(loc));   // null
                else
                    return(rval);
            default:
                throw new ExEx("get() - 2nd argument for a map is neither integer nor string type", loc);
            }
        default:
            throw new ExEx("get() - 1st argument is not type map or type list", loc);
        }
    }
}
