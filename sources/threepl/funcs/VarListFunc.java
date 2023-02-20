package threepl.funcs;

import static threepl.ThreePL.var_queue;

import java.util.ArrayList;

import threepl.exceptions.ExEx;
import threepl.exec.Ref;
import threepl.exec.SubFieldList;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get a list of the currently declared target mode variables.
 * The optional single argument if true specifies that only input, output or clock mode
 * variables will be included in the returned list. Entries in the list are pointers to
 * the variables.
 */
public class VarListFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the identifier of the variable
     */
    public Val getVal (NodeList args) {
        SrcLoc          loc = args.getCallLoc();
        boolean         ios = false;
        ArrayList<Val>  al = new ArrayList<Val>();

        if (args.size() > 1)
            throw new ExEx("varident() must have zero or one arguments", loc);
        
        if (args.size() == 1) {
            Val val = args.getVal(0, true);
            ios = val.getSingleLval(loc);
        }
        
        for (Var v : var_queue) {
            if (!ios || (v.getMode() == Mode.INPUT) || (v.getMode() == Mode.OUTPUT) || (v.getMode() == Mode.CLOCK)) {
                if (v.isIndirect())
                    continue;
                Ref ref = v.getRef(null, new SubFieldList(), false, loc);
                Val vv = new Val(ref, true, loc);
                vv.setDummyVar(true, loc);
                al.add(vv);
            }
        }
        return(new Val(al, loc));
    }
}
