package threepl.funcs;

import java.util.ArrayList;
import java.util.TreeMap;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the number of entries in a map or a list.
 * This has one argument, which is the map or list variable.
 */
public class EntriesFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the number of map or list entries
     */
    @SuppressWarnings("unchecked")
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("entries() must have single argument", loc);
        
        Val val = args.getVal(0);
        if (val.getPrimType() == Ptype.MAP) {
            TreeMap<String,Val> tm = (TreeMap<String, Val>)val.getVal(0);
            return(new Val(tm.size(), loc));
        }
        if (val.getPrimType() == Ptype.LIST) {
            ArrayList<Val>   al = (ArrayList<Val>)val.getVal(0);
            return(new Val(al.size(), loc));
        }
        throw new ExEx("entries() - argument is not type map or type list", loc);
    }
}
