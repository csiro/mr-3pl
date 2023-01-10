package threepl.procs;

import java.util.Iterator;

import threepl.ThreePL;
import threepl.exceptions.ExEx;
import threepl.exec.Immediate;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.exec.Var;
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
 * of which there must be at least 4, are identifier/ordinal
 * pairs. Value identifiers and associated ordinals must not
 * appear more than once.
 */
public class EnumvProc extends InbuiltProc implements Constant {

    /**
     * Construct the inbuilt procedure enumv().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     * 'allowed_as_param' is false for this procedure as it is not
     * allowed to be called as a parameter declaration
     */
    public EnumvProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
    }

    /**
     * Execute the procedure enumv(). This does not generate executable code.
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
        if (inargs.size() < 5)
            throw new ExEx("enumv() must have >= 5 input arguments", loc);
        if ((inargs.size() % 2) != 1)
            // must have odd number of arguments
            // (struct name + pairs)
            throw new ExEx("enumv() arguments not paired", loc);
        if (outargs.size() != 0)
            throw new ExEx("enumv() cannot have output arguments", loc);
        Iterator<Node>  it = inargs.iterator();
        Node            n;
        Val             val;
        Ident           id;
        long            ord;
        StringBuffer    sb = new StringBuffer("{");
        
        // enum name
        n = it.next();
        id = n.getIdentifier("enumv() - 1st argument");
        
        // value identifier/ordinal pairs
        while (it.hasNext()) {  // odd number, so guaranteed pairs here
            // identifier
            n = it.next();
            if (n == null)
                throw new ExEx("enumv() - null argument", loc);
            if (n instanceof VarNode)
                sb.append(n.getIdentifier("enum value identifier").getId());
            else
                throw new ExEx("enumv() - argument not an identifier", loc);
    
            // ordinal
            n = it.next();
            if (n == null)
                throw new ExEx("enumv() - null argument", loc);
            val = n.getVal();
            if (val.getMode() != Mode.IMMEDIATE)
                throw new ExEx("enumv() - ordinal not immediate", loc);
            ord = val.getSingleIval(loc);
            sb.append("," + ord);
            if (it.hasNext())
                sb.append(",");
        }
        sb.append("}");
        Type e = new Type(sb.toString(), loc);
        Val init = new Val(e, loc);
        Var var = new Immediate(id, new Type(Ptype.TYPE, 0), init, false, false, loc);
        ThreePL.addVar(var, id.getScopeContext(), loc);
    }
}
