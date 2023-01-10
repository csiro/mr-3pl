package threepl.procs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to remove an entry from a map or list.
 * it has one or two input arguments and no output arguments.
 * The first input argument is the map or list. The second optional
 * input argument is a map key which is type STR or a map or list
 * index which is type INT or UINT. If the second argument is omitted
 * then all entries are removed.
 */
public class RemoveProc extends InbuiltProc implements Constant {

    /**
     * Construct the inbuilt procedure remove().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public RemoveProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
    }

    /**
     * Execute the procedure remove(). This does not generate executable code.
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
        if ((inargs.size() < 1) || (inargs.size() > 2))
            throw new ExEx("remove() must have two or three input arguments", loc);
        if (outargs.size() != 0)
            throw new ExEx("remove() cannot have output arguments", loc);
        int     index;
        String  key = null;
        Val     val = inargs.getVal(0);
        Val     val2 = null;
        if (val == null)
            throw new ExEx("remove() - 1st argument is missing", loc);
        if (inargs.size() > 1)
            val2 = inargs.getVal(1);
        switch (val.getPrimType()) {
        case LIST:
            ArrayList<Val>   al = (ArrayList<Val>)val.getVal(0);
            if (val2 == null) {
                al.clear();
                return;
            }
            switch (val2.getPrimType()) {
            case INT:
            case UINT:
                index = (int)val2.getSingleIval(loc);
                if ((index < 0) || (index >= al.size()))
                    throw new ExEx("remove() - index is out of range (" + index + " >= " + al.size() + ")", loc);
                al.remove(index);
                return;
            default:
                throw new ExEx("remove() - 2nd argument for a list is not integer type", loc);
            }
        case MAP:
            TreeMap<String,Val> tm = (TreeMap<String,Val>)val.getVal(0);
            if (val2 == null) {
                tm.clear();
                return;
            }
            switch (val2.getPrimType()) {
            case INT:
            case UINT:
                index = (int)val2.getSingleIval(loc);
                Set<String> ks = tm.keySet();
                if ((index < 0) || (index >= ks.size()))
                    throw new ExEx("remove() - index is out of range (" + index + " >= " + ks.size() + ")", loc);
                Iterator<String>    it = ks.iterator();
                while (it.hasNext()) {
                    if (index-- == 0) {
                        key = it.next();
                        break;
                    }
                }
                tm.remove(key);
                return;
            case STR:
                key = val2.getSingleSval(loc);
                tm.remove(key);
                return;
            default:
                throw new ExEx("remove() - 2nd argument for a map is neither integer nor string type", loc);
            }
        default:
            throw new ExEx("remove() - 1st argument is not type map or type list", loc);
        }
    }
}
