package threepl.funcs;

import java.util.Set;
import java.util.TreeMap;

import threepl.exceptions.ExEx;
import threepl.exec.Ref;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.exec.WordSpec;
import threepl.nodes.ClassFuncCallNode;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to list attributes of a variable.
 * There is one argument, the variable.
 */
public class ListAttributesFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the directive list array of structs
     */
    @SuppressWarnings("unchecked")
    public Val getVal (NodeList args) {
        SrcLoc              loc = args.getCallLoc();
        Var                 var = null;

        if (args.size() != 1)
            throw new ExEx("listattributes() must have one argument", loc);

        if (args.getNode(0) instanceof ClassFuncCallNode) {
            // functions such as getclock(), currentclock(), prevclock()
            Val val = args.getVal(0);
            var = val.getVar();
            if (var == null)
                return(new Val("NULL", loc));
        } else {
            Ref ref = args.getRef(0, "listattributes()");
            var = ref.getVar();
        }
        
        if (var == null)
            throw new ExEx("listattributes() argument is not a variable or function returning a variable", loc);
        Val         mval = var.getAttributes(true, loc);
        TreeMap<String, Val>    m = (TreeMap<String, Val>)mval.getVal(0);
        Set<String>             s = m.keySet();
        int                     n = s.size();
        Type                    type = new Type("[" + n + "](key, str, mode, str, value, str)", loc);
        String[]                sa = new String[3*n];
        Type[]                  ta = new Type[3*n];
        Type                    st = new Type("str", loc);
        WordSpec                ws = type.getWordSpec(null, loc);
        int                     i = 0;
        
        for (String key: s) {
            sa[i]   = key;
            ta[i++] = st;
            Val     v = m.get(key);
            switch (v.getMode()) {
            case IMMEDIATE:
                sa[i]   = "immediate";
                ta[i++] = st;
                switch (v.getPrimType()) {
                case UINT:
                case INT:
                    sa[i] = String.valueOf(v.getSingleIval(loc));
                    break;
                case LOG:
                    sa[i] = String.valueOf(v.getSingleLval(loc));
                    break;
                case STR:
                    sa[i] = v.getSingleSval(loc);
                    break;
                case TYPE:
                    sa[i] = v.getSingleTval(loc).getTypeString();
                    break;
                default:
                    throw new ExEx("listattributes() - type error", loc);
                }
                ta[i++] = st;
                break;
            case CLOCK:
                sa[i]   = "clock";
                ta[i++] = st;
                sa[i] = v.getVar().getEname();
                ta[i++] = st;
                break;
            default:
                throw new ExEx("listattributes() - mode error", loc);
            }
        }
        
        return(new Val(sa, ta, ws, null, null, loc));
    }
}
