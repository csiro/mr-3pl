package threepl.funcs;

import threepl.ThreePL;
import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.Ident;
import threepl.nodes.NodeList;
import threepl.nodes.VarNode;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to determine if a variable exists in a variable map.
 * This has one input argument and no output arguments.
 * The input argument is the variable identifier.
 */
public class VarExistsFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  true if the directive exists
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        Ident   ident;
        String  s;
        Context sc;

        if (args.size() != 1)
            throw new ExEx("varexists() must have single argument", loc);
        if (!(args.getNode(0) instanceof VarNode))
            throw new ExEx("varexists() argument must be a variable", loc);

        ident = args.getIdent(0,"varexists() - ");
        s = ident.getId();
        sc = ident.getScopeContext();

        return(new Val(ThreePL.findVar(s, sc, loc) != null, loc));
    }
}
