package threepl.funcs;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import static threepl.ThreePL.getCallName;

import threepl.exceptions.ExEx;
import threepl.exec.RefOrVal;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.exec.WordSpec;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to pass lists, arrays or maps of arguments to the input or output
 * argument list of a module, procedure or function. It should only be called within
 * a module, procedure or function argument list.
 * There are two arguments.
 * The first argument is a list or array of key strings for the arguments to be passed.
 * Blank entries signify an argument with no associated key. This argument may be omitted.
 * The second argument is a list or array of arguments.
 * If both arguments are present then they must be the same length.
 * If a single argument is given it must be a map. The map keys and associated entries
 * will be inserted as a key=value pair of arguments to the called subroutine.
 */
public class ArgListFunc extends InbuiltFunc implements Constant {
    private final static Pattern    strpattern = Pattern.compile("\\[.*\\]str");
    private final static Pattern    ptrpattern = Pattern.compile("\\[.*\\]->");

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the array of structs
     */
    public Val getVal (NodeList args) {
        SrcLoc      loc = args.getCallLoc();
        String      typestring;
        Val         keys;
        Val         vals;
        String[]    ka = null;
        Object[]    va = null;
        String      callerName;

        callerName = getCallName(1);
        if (callerName != null)
            throw new ExEx("arglist() called as argument to builtin module, procedure or function '" + callerName + "'", loc);
        if (args.size() == 1 ) {
            Val v = args.getVal(0);
            typestring = v.getTypeString();
            if (!typestring.equals("map"))
                throw new ExEx("arglist() single argument is not type 'map'", loc);
            @SuppressWarnings("unchecked")
            TreeMap<String,Val> tm = (TreeMap<String,Val>)v.getVal(0);
            Set<String>         ks = tm.keySet();
            ka = new String[ks.size()];
            va = new Object[ks.size()];
            int i = 0;
            for (String s : ks) {
                Val kv = tm.get(s);
                ka[i]   = s;
                va[i++] = kv;
            }
            return(makeRetVal(ka.length, ka, va, loc));
        }
        if (args.size() !=2)
            throw new ExEx("arglist() must have 1 or 2 arguments", loc);

        keys = args.getVal(0);
        typestring = keys.getTypeString();
        if (typestring.equals("list")) {
            @SuppressWarnings("unchecked")
            ArrayList<Val>   al = (ArrayList<Val>)keys.getVal(0);
            int i = 0;
            ka = new String[al.size()];
            for (Val v : al)
                ka[i++] = v.getSingleSval(loc);
        } else if (strpattern.matcher(typestring).matches()) {
            Object[] o = keys.getVals();
            ka = new String[o.length];
            for (int i=0 ; i<o.length ; i++)
                ka[i] = (String)o[i];
        } else if (!(typestring.equals("null")))
            throw new ExEx("arglist() 1st argument is not NULL, a list or an array of strings", loc);
        
        vals = args.getVal(1);
        if (vals == null)
             throw new ExEx("arglist() 2nd argument is null", loc);
        typestring = vals.getTypeString();
        if (typestring.equals("list")) {
            @SuppressWarnings("unchecked")
            ArrayList<Object>   al = (ArrayList<Object>)vals.getVal(0);
            int i = 0;
            va = new Object[al.size()];
            for (Object o : al)
                va[i++] = o;
        } else if (ptrpattern.matcher(typestring).matches()) {
            va = vals.getVals();
        } else
            throw new ExEx("arglist() 2nd argument is not a list or an array of pointers", loc);
        int n = va.length;
        if ((ka != null) && (ka.length != n))
            throw new ExEx("arglist() arguments are of different length", loc);        
        
        return(makeRetVal(n, ka, va, loc));
    }
    
    private static Val makeRetVal (int n, String[] ka, Object[] va, SrcLoc loc) {
        Type        type = new Type("[" + n + "](key, str, value, ->)", loc);
        Type        st = new Type("str", loc);
        Type        pt = new Type("->", loc);
        WordSpec    ws = type.getWordSpec(null, loc);
        Object[]    oa = new Object[2*n];
        Type[]      ta = new Type[2*n];

        int i, j;
        for (i=0,j=0 ; i<n ; i++) {
            if (ka != null)
                oa[j] = ka[i];
            else
                oa[j] = null;
            ta[j++] = st;
            RefOrVal rov = (RefOrVal)va[i];
            oa[j] = rov;
            ta[j++] = pt;
        }
        return(new Val(oa, ta, ws, null, null, loc));
    }
}
