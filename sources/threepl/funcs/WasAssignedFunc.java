package threepl.funcs;


import threepl.exceptions.ExEx;
import threepl.exec.Ref;
import threepl.exec.SubFieldList;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to determine if a variable has been assigned.
 * The argument can be any mode except input, cmemory or rmemory.
 * The argument can be any type but must not have a subscript or field,
 * i.e. the test applies to the whole variable, not just a member.
 */
public class WasAssignedFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the arsine value
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        
        if (args.size() != 1)
            throw new ExEx("wasassigned() - must have one argument", loc);

        //Val val = args.getVal(0);
        Ref ref = args.getRef(0, "assigned() - ");
        switch (ref.getMode()) {
        case IMMEDIATE:
        case STATIC:
        case QUEUE:
        case OUTPUT:
        case SELECTVALUE:
        case VALUE:
        case PRIORITY:
        case CLOCK:
            break;
        default:
            throw new ExEx("wasassigned() - mode " + ref.getMode().name() + " not allowed", loc);
        }
        SubFieldList    sfl = ref.getSubFields();
        if ((sfl == null) || sfl.isEmpty()) {
            Var var = ref.getVar();
            return(new Val(var.isAssigned(), loc));
        }
        throw new ExEx("wasassigned() - argument cannot have subscripts or fields", loc);
    }
}
