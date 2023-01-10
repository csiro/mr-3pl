package threepl.procs;

import java.util.ArrayList;

import threepl.exceptions.ExEx;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to append an items from another list into a list.
 * it has three input arguments and no output arguments.
 * The first input argument is the list. The optional second input
 * argument is the index which is type "int". If it is missing the
 * entry will be appended to the end of the list. The third
 * input argument is the other list.
 */
public class AppendAllProc extends InbuiltProc implements Constant {

    /**
     * Construct the inbuilt procedure appendall().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public AppendAllProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
    }

    /**
     * Execute the procedure appendall(). This does not generate executable code.
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
        if (inargs.size() != 3)
            throw new ExEx("appendall() must have three input arguments", loc);
        if (outargs.size() != 0)
            throw new ExEx("appendall() cannot have output arguments", loc);
        Val     lval = inargs.getVal(0);
        Val     ival = null;
        int     index = 0;
        Val     rval = null;
        if (lval == null)
            throw new ExEx("appendall() - 1st argument is missing", loc);
        if (lval.getPrimType() != Ptype.LIST)
            throw new ExEx("appendall() - 1st argument is not type \"list\"", loc);
        if (inargs.size() > 1)
            ival = inargs.getVal(1);
        if (ival != null) {
            if ((ival.getPrimType() != Ptype.INT) && (ival.getPrimType() != Ptype.UINT))
                throw new ExEx("appendall() - 2nd argument for a list is not integer type", loc);
            index = (int)ival.getSingleIval(loc);
        }
        rval = inargs.getVal(2);
        if (rval == null)
            throw new ExEx("appendall() - 3rd argument is missing", loc);
        if (rval.getPrimType() != Ptype.LIST)
            throw new ExEx("appendall() - 3rd argument is not type \"list\"", loc);
        if ((rval != null) && (rval.getValType(0) == Type.NULL))
            rval = null;
        ArrayList<Object>   al1 = (ArrayList<Object>)lval.getVal(0);
        ArrayList<Object>   al2 = (ArrayList<Object>)lval.getVal(2);
        if ((index < 0) || (index > al1.size()))
            throw new ExEx("appendall() - index is out of range (" + index + " > " + al1.size() + ")", loc);
        if (ival == null)
            al1.addAll(al2);
        else
            al1.addAll(index, al2);
    }
}
