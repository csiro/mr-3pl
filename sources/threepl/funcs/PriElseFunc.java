package threepl.funcs;

import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt function to get the ELSE TDEVar from a priority variable.
 * This has 1 input argument, the priority mode variable identifier.
 */
public class PriElseFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the priority 'else' value
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        if (args.size() != 1)
            throw new ExEx("prielse() must have 1 input argument", loc);
        
        // identifier
        Ref     ref = args.getRef(0, "prielse()");
        if (ref.getMode() != Mode.PRIORITY)
            throw new ExEx("prielse() argument is not a priority variable", loc);
        Var     var = ref.getVar();

        TDEVar  t = var.getPriElse(loc);

        Val val = new Val(null, Mode.VALUE, t, loc);
        val.addOVar(var);
        return(val);
    }
}
