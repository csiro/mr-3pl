package threepl.funcs;

import static threepl.ThreePL.environment;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import threepl.exceptions.ExEx;
import threepl.exec.SubFieldList;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.exec.WordSpec;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the environment variables. These are returned
 * in a map.
 */
public class EnvFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the number of map or list entries
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 0)
            throw new ExEx("env() must have no arguments", loc);

        TreeMap<String,Val>             env = new TreeMap<String, Val>();
        Set<Map.Entry<String,String>>   map_set = environment.entrySet();
        
        for (Map.Entry<String,String> me : map_set)
            env.put(me.getKey(), new Val(me.getValue(), loc));
        Object[]    oa = new Object[1];
        Type[]      ta = new Type[1];
        Type        type = new Type(Ptype.MAP, 0);
        WordSpec    ws = type.getWordSpec(null, loc);
        oa[0] = env;
        ta[0] = type;
        return(new Val(oa, ta, ws, null, new SubFieldList(), null));
    }
}
