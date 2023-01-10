package threepl.funcs;

import threepl.ThreePL;
import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.nodes.Ident;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to determine if a parameter has been
 * passed an argument.
 * The argument is the parameter identifier.
 */
public class MatchedFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  true if the parameter was passed an argument
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("matched() must have single argument", loc);

        //Ref     ref = args.getRef(0, "matched()");
        
        Ident   id = args.getIdent(0, "matched()");
        String  name = id.getId();
        Context sc = id.getScopeContext();
        Var     var = ThreePL.findVar(name, sc, loc);
        if (var == null)
             throw new ExEx("matched() cannot find variable '" + name + "'", loc);
        
        //Var     var = ref.getVar();
        if (!var.isInputPar() && !var.isOutputPar())
            // Not a parameter - return 'true'.
            return(new Val(true, loc));
            //throw new ExEx("matched() argument is not a parameter", loc);
        
        return(new Val(var.isMatched(), loc));
    }
}
