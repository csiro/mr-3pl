package threepl.procs;

import java.util.TreeMap;

import threepl.exceptions.ExEx;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to put an entry into a map.
 * it has two or three input arguments and no output arguments.
 * The first input argument is the map. The second input argument is the
 * key which is type STR. The third optional input argument is the value
 * which can be an IMMEDIATE, VALUE or CLOCK mode value of any type. If
 * it is "null" or missing then the value for the key will be null.
 */
public class PutProc extends InbuiltProc implements Constant {

    /**
     * Construct the inbuilt procedure put().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public PutProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
        ipnames.put("map", 0);
        ipnames.put("key", 1);
        ipnames.put("value", 2);
    }

    /**
     * Execute the procedure put(). This does not generate executable code.
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
        if ((inargs.size() < 2) || (inargs.size() > 3))
            throw new ExEx("put() must have two or three input arguments", loc);
        if (outargs.size() != 0)
            throw new ExEx("put() cannot have output arguments", loc);
        Val     mval = inargs.getVal(0);
        Val     kval = inargs.getVal(1);
        Val     rval = null;
        if (mval == null)
            throw new ExEx("put() - 1st argument is missing", loc);
        if (kval == null)
            throw new ExEx("put() - 2nd argument is missing", loc);
        if (mval.getPrimType() != Ptype.MAP)
            throw new ExEx("put() - 1st argument is not type \"map\"", loc);
        TreeMap<String,Val> tm = (TreeMap<String,Val>)mval.getVal(0);

        String  key;
        switch (kval.getPrimType()) {
        case STR:
            key = kval.getSingleSval(loc);
            break;
        case INT:
        case UINT:
            key = Long.toString(kval.getSingleIval(loc));
            break;
        default:
            throw new ExEx("put() - 2nd argument is not type \"str\" or \"int\"", loc);
        }


        if (inargs.size() > 2) {
            rval = inargs.getVal(2);
            if (rval != null) {
                switch (rval.getMode()) {
                case IMMEDIATE:
                    if (rval.getValType(0) == Type.NULL)
                        rval = null;
                    break;
                case VALUE:
                case CLOCK:
                    break;
                default:
                    throw new ExEx("put() - 3rd argument is not immediate, value or clock mode", loc);
                }
            }
        }
        rval.setDummyVar(false, loc);
        tm.put(key, rval);
    }
}
