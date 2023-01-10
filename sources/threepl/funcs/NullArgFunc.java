package threepl.funcs;

import threepl.ThreePL;
import threepl.exceptions.ExEx;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.nodes.Ident;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to return its argument value unless
 * the argument is an un-matched parameter in which case it returns null.
 * The argument is the parameter identifier.
 */
public class NullArgFunc extends InbuiltFunc implements Constant {

    /**
     * Get a Val via a nullarg() call.
     * @param   args is a list of argument tree nodes
     * @return  the null or the argument Val
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("nullarg() must have single argument", loc);

        Ident   id = args.getIdent(0, true, "nullarg()");
        String  name = id.getId();
        Context sc = id.getScopeContext();
        Var     var = ThreePL.findVar(name, sc, loc);
        if (var == null)
             throw new ExEx("nullarg() cannot find variable '" + name + "'", loc);
        if (!var.isInputPar() && !var.isOutputPar())
            throw new ExEx("nullarg() argument is not a parameter", loc);
        
        if (var.isMatched()) {
            return(args.getVal(0));
        } else
            return(null);
    }
    
    /**
     * Get a Ref via a nullarg() call.
     * @param   args is a list of argument tree nodes
     * @return  the null or the argument Ref
     */
    public Ref getRef (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("nullarg() must have single argument", loc);

        Ident   id = args.getIdent(0, true, "nullarg()");
        String  name = id.getId();
        Context sc = id.getScopeContext();
        Var     var = ThreePL.findVar(name, sc, loc);
        if (var == null)
             throw new ExEx("nullarg() cannot find variable '" + name + "'", loc);
        if (!var.isInputPar() && !var.isOutputPar())
            throw new ExEx("nullarg() argument is not a parameter", loc);
        
        if (var.isMatched()) {
            return(args.getRef(0, "nullarg()"));
        } else
            return(null);
    }
}
