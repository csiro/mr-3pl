package threepl.procs;

import java.util.TreeMap;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to put all entry from another map into a map.
 * it has two input arguments and no output arguments.
 * The first input argument is the map. The second input argument is the
 * second map from which to copy entries.
 */
public class PutAllProc extends InbuiltProc implements Constant {

    /**
     * Construct the inbuilt procedure putall().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public PutAllProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
    }

    /**
     * Execute the procedure putall(). This does not generate executable code.
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
        if (inargs.size() != 2)
            throw new ExEx("putall() must have two input arguments", loc);
        if (outargs.size() != 0)
            throw new ExEx("putall() cannot have output arguments", loc);
        Val     m1val = inargs.getVal(0);
        Val     m2val = inargs.getVal(1);
        if (m1val == null)
            throw new ExEx("putall() - 1st argument is missing", loc);
        if (m2val == null)
            throw new ExEx("putall() - 2nd argument is missing", loc);
        if (m1val.getPrimType() != Ptype.MAP)
            throw new ExEx("putall() - 1st argument is not type \"map\"", loc);
        if (m2val.getPrimType() != Ptype.MAP)
            throw new ExEx("putall() - 2nd argument is not type \"map\"", loc);
        TreeMap<String,Val> tm1 = (TreeMap<String,Val>)m1val.getVal(0);
        TreeMap<String,Val> tm2 = (TreeMap<String,Val>)m2val.getVal(0);
        tm1.putAll(tm2);
    }
}
