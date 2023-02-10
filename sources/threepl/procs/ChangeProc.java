package threepl.procs;

import java.util.ArrayList;

import threepl.exceptions.ExEx;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to change an entry in a list.
 * it has two or three input arguments and no output arguments.
 * The first input argument is the list. The second input
 * argument is the index which is type "int". The third optional
 * input argument is the value which can be any immediate type. If
 * it is 'null' or missing then the list entry will be changed to null.
 */
public class ChangeProc extends InbuiltProc implements Constant {

    /**
     * Construct the inbuilt procedure change().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public ChangeProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
        ipnames.put("list", 0);
        ipnames.put("index", 1);
        ipnames.put("value", 2);
    }

    /**
     * Execute the procedure change(). This does not generate executable code.
     * @param   inargs is a list of input argument tree nodes
     * @param   outargs is a list of output argument tree nodes
     * @param   toplevel is true if we are at the module, procedure or function
     *          level
     */
    public void execute (
        NodeList    inargs, 
        NodeList    outargs, 
        boolean     toplevel
    ) {
        SrcLoc      loc = inargs.getCallLoc();
        if ((inargs.size() < 2) || (inargs.size() > 3))
            throw new ExEx("change() must have two or three input arguments", loc);
        if (outargs.size() != 0)
            throw new ExEx("change() cannot have output arguments", loc);
        Val     lval = inargs.getVal(0);
        Val     ival = null;
        int     index = 0;
        Val     rval = null;
        if (lval == null)
            throw new ExEx("change() - 1st argument is missing", loc);
        if (lval.getPrimType() != Ptype.LIST)
            throw new ExEx("append() - 1st argument is not type \"list\"", loc);
        if (inargs.size() > 1)
            ival = inargs.getVal(1);
        if (ival == null)
            throw new ExEx("change() - 2nd argument is missing", loc);
        if ((ival.getPrimType() != Ptype.INT) && (ival.getPrimType() != Ptype.UINT))
            throw new ExEx("change() - 2nd argument for a list is not integer type", loc);
        index = (int)ival.getSingleIval(loc);
        if (inargs.size() > 2) {
            rval = inargs.getVal(2);
            if (rval != null) {
                switch (rval.getMode()) {
                case IMMEDIATE:
                    if (rval.getValType(0) == Type.NULL)
                        rval = null;
                    break;
                case CLOCK:
                case CMEMORY:
                case RMEMORY:
                    break;
                default:
                    throw new ExEx("change() - 3rd argument is not immediate, clock, cmemory or rmemory mode", loc);
                }
            }
        }
        @SuppressWarnings("unchecked")
        ArrayList<Val>   al = (ArrayList<Val>)lval.getVal(0);
        if ((index < 0) || (index > al.size()))
            throw new ExEx("change() - index is out of range (" + index + " > " + al.size() + ")", loc);
        rval.setDummyVar(false, loc);
        al.set(index, rval);
    }
}
