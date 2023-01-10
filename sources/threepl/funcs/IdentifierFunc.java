package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.exec.Var.IDtype;
import threepl.nodes.ClassFuncCallNode;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the identifier of a variable.
 * This has 1 or 2 arguments. The 1st or only argument is the variable.
 * The optional 2nd argument is a string indicating the required format of the
 * reported identifier.
 */
public class IdentifierFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the identifier string
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        Var     var;

        if ((args.size() < 1) || (args.size() > 2))
            throw new ExEx("identifier() must have 1 or 2 arguments", loc);

        if (args.getNode(0) instanceof ClassFuncCallNode) {
            // functions such as getclock(), currentclock(), prevclock()
            Val val = args.getVal(0);
            var = val.getVar();
            if (var == null)
                return(new Val("NULL", loc));
        } else {
            Ref ref = args.getRef(0, "identifier()");
            var = ref.getVar();
        }
        
        if (var == null)
            throw new ExEx("identifier() argument is not a variable or function returning a variable", loc);

        String soption = null;
        if (args.size() == 2) {
            Val     sval = args.getVal(1);
            if (sval.getMode() != Mode.IMMEDIATE)
                throw new ExEx("identifier() 2nd argument is not immediate", loc);
            if (sval.getPrimType() != Ptype.STR)
                throw new ExEx("identifier() 2nd argument is not type str", loc);
            soption = sval.getSingleSval(loc);
        }

        if (soption == null)
            return(new Val(var.getID(IDtype.LITERAL), loc));
        if (soption.equals("ext"))
            return(new Val(var.getEname(), loc));
        if (soption.equals("chain"))
            return(new Val(var.getID(IDtype.CHAIN), loc));
        if (soption.equals("sliteral"))
            return(new Val(var.getID(IDtype.SLITERAL), loc));
        if (soption.equals("real"))
            return(new Val(var.getID(IDtype.REAL), loc));
        throw new ExEx("identifier() - 2nd argument not recognised returned identifier format", loc);
    }
}
