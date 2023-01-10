package threepl.procs;

import java.util.ArrayList;

import threepl.exceptions.ExEx;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to append an entry into a list.
 * it has one to three input arguments and no output arguments.
 * The first input argument is the list. The optional second input
 * argument is the index which is type "int". If it is missing the
 * entry will be appended to the end of the list. The third optional
 * input argument is the value which can be an IMMEDIATE, VALUE or
 * CLOCK value of any type. If it is "null" or missing then the
 * value will be null.
 */
public class AppendProc extends InbuiltProc implements Constant {

    /**
     * Construct the inbuilt procedure append().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public AppendProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
        ipnames.put("l", 0);
        ipnames.put("index", 1);
        ipnames.put("v", 2);
    }

    /**
     * Execute the procedure append(). This does not generate executable code.
     * @param   inargs is a list of input argument tree nodes
     * @param   outargs is a list of output argument tree nodes
     * @param   toplevel is true if we are at the module, procedure or function
     *          level
     */
    @SuppressWarnings("unchecked")
    public void execute (
        NodeList    inargs, 
        NodeList    outargs, 
        boolean     toplevel
    ) {
        SrcLoc      loc = inargs.getCallLoc();
        if ((inargs.size() < 1) || (inargs.size() > 3))
            throw new ExEx("append() must have one to three input arguments", loc);
        if (outargs.size() != 0)
            throw new ExEx("append() cannot have output arguments", loc);

        Val     lval = inargs.getVal(0);
        Val     ival = null;
        int     index = 0;
        Val     rval = null;
        if (lval == null)
            throw new ExEx("append() - 1st argument is missing", loc);
        if (lval.getPrimType() != Ptype.LIST)
            throw new ExEx("append() - 1st argument is not type \"list\"", loc);
        if (inargs.size() > 1)
            ival = inargs.getVal(1);
        if (ival != null) {
            if ((ival.getPrimType() != Ptype.INT) && (ival.getPrimType() != Ptype.UINT))
                throw new ExEx("append() - 2nd argument is not integer type", loc);
            index = (int)ival.getSingleIval(loc);
        }
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
                    throw new ExEx("append() - 3rd argument is not immediate, clock, cmemory or rmemory mode", loc);
                }
            }
        }
        ArrayList<Object>   al = (ArrayList<Object>)lval.getVal(0);
        if ((index < 0) || (index > al.size()))
            throw new ExEx("append() - index is out of range (" + index + " > " + al.size() + ")", loc);
        if (rval != null)
            rval.setDummyVar(false, loc);
        if (ival == null)
            al.add(rval);
        else
            al.add(index, rval);
    }
}
