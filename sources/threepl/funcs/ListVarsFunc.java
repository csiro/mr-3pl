package threepl.funcs;

import java.util.LinkedList;

import threepl.ThreePL;
import threepl.exceptions.ExEx;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.exec.WordSpec;
import threepl.exec.Var.IDtype;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to list variables in one or more scopes.
 * There are two optional arguments.
 * The first argument is the scope whose variables are to be listed. This
 * must be one of the strings "global", "file", "calling", "local" or
 * "files".
 * The second argument indicates if all file module scopes are to be listed
 * rather than just the current file scope.
 * The return value is an array of {@code [n](scope, str, ident, str, var, ->)}.
 */
public class ListVarsFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the directive list array of structs
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        Context context = Context.DEFAULT;
        boolean allsegs = false;
        Val     v;

        if ((args.size() == 0) || (args.size() > 2))
            throw new ExEx("listvars() must have 1 or 2 arguments", loc);

        if (args.size() != 0) {
            v = args.getVal(0);
            if (v != null) {
                String  s = v.getSingleSval(loc);
                if (!s.equals("")) {
                    if (s.equals("global"))
                        context = Context.GLOBAL;
                    else if (s.equals("file"))
                        context = Context.FILE;
                    else if (s.equals("files")) {
                        context = Context.FILE;
                        allsegs = true;
                    } else if (s.equals("local"))
                        context = Context.LOCAL;
                    else if (s.equals("calling"))
                        context = Context.CALLING;
                    else
                        throw new ExEx("listvars() unrecognised argument '" + s + "'", loc);
                }
            }
        }
        
        if (args.size() > 1) {
            v = args.getVal(1);
            if (v != null)
                allsegs = v.getSingleLval(loc);
        }

        LinkedList<Object>  l = ThreePL.listVars(context, allsegs, loc);
        int                 n = 0;
        int                 i, j, k;
        
        for (i=0 ; i<l.size() ; ) {
            j = ((Integer)l.get(i)).intValue();
            n += j;
            i += j + 2;
        }
        Type        type = new Type("[" + n + "](scope, str, ident, str, var, ->)", loc);
        Type        st = new Type("str", loc);
        Type        pt = new Type("->", loc);
        WordSpec    ws = type.getWordSpec(null, loc);
        Object[]    oa = new Object[3*n];
        Type[]      ta = new Type[3*n];
        String      scope;
        Var         var;

        for (i=0,k=0 ; i<l.size() ; ) {
            j = ((Integer)l.get(i++)).intValue();
            scope = (String)l.get(i++);
            while (j-- != 0) {
                var = (Var)l.get(i++);
                oa[k] = scope;
                ta[k++] = st;
                oa[k] = var.getID(IDtype.LITERAL);
                ta[k++] = st;
                oa[k] = var.getRef(null, null, loc);
                ta[k++] = pt;
            }
        }
        
        return(new Val(oa, ta, ws, null, null, loc));
    }
}
