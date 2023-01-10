package threepl.procs;

import java.util.Iterator;

import threepl.ThreePL;
import threepl.exceptions.ExEx;
import threepl.exec.Immediate;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.nodes.Ident;
import threepl.nodes.Node;
import threepl.nodes.NodeList;
import threepl.nodes.VarNode;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to create an enum type.
 * The 1st argument is the identifier of the resulting type
 * variable created in the current scope. The following arguments,
 * of which there must be at least 2, must be identifiers which will
 * be used as the enum value identifiers. The ordinal values will
 * be ascending integers starting at 0. Value identifiers must not
 * appear more than once.
 */
public class EnumProc extends InbuiltProc implements Constant {

    /**
     * Construct the inbuilt procedure enum().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     * 'allowed_as_param' is false for this procedure as it is not
     * allowed to be called as a parameter declaration
     */
    public EnumProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
    }

    /**
     * Execute the procedure enum(). This does not generate executable code.
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
        if (inargs.size() < 3)
            throw new ExEx("enum() must have >= 3 input arguments", loc);
        if (outargs.size() != 0)
            throw new ExEx("enum() cannot have output arguments", loc);
        Iterator<Node>  it = inargs.iterator();
        Node            n;
        Ident           id;
        long            ord = 0;
        StringBuffer    sb = new StringBuffer("{");
        
        // enum name
        n = it.next();
        id = n.getIdentifier("enum() - 1st argument");
        
        // value identifiers
        while (it.hasNext()) {
            n = it.next();
            if (n == null)
                throw new ExEx("enum() - null argument", loc);
            if (n instanceof VarNode)
                sb.append(n.getIdentifier("enum value identifier").getId());
            else
                throw new ExEx("enum() - argument not an identifier", loc);
            sb.append("," + ord++);
            if (it.hasNext())
                sb.append(",");
        }
        sb.append("}");
        Type        e = new Type(sb.toString(), loc);
        Val         init = new Val(e, loc);
        Immediate   ivar = new Immediate(id, new Type(Ptype.TYPE, 0), init, false, false, loc);
        ThreePL.addVar(ivar, id.getScopeContext(), loc);
    }
}
