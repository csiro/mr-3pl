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
 * An inbuilt function to determine if a variable or directive
 * has been created.
 * The argument is the variable or directive identifier.
 * An integer value is returned which is -
 * <ul>
 * <li>0 for unknown
 * <li>1 for a variable
 * <li>2 for a directive
 * </ul>
 * If more than one of these is the case the designating bits are ORed.
 */
public class ExistsFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the 2-bit code
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        Ident   ident;
        String  s;
        Context sc;
        int     result = 0;

        if (args.size() != 1)
            throw new ExEx("exists() must have single argument", loc);
        if (!(args.getNode(0) instanceof VarNode))
            throw new ExEx("exists() argument must be a variable", loc);

        ident = args.getIdent(0,"exists() - ");
        s = ident.getId();
        sc = ident.getScopeContext();

        // try a variable
        if (s.indexOf('[') == -1) { // only check the others if no subscripts!
            if (ThreePL.findVar(s, sc, loc) != null)
                result |= 1;

            // try a directive
            if ((sc == Context.DEFAULT) && ThreePL.findDir(s) != null)
                result |= 2;
        }

        return(new Val(result, loc));
    }
}
