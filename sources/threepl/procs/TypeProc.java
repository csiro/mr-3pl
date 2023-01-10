package threepl.procs;

import java.util.ArrayList;
import java.util.Iterator;

import threepl.ThreePL;
import threepl.exceptions.ExEx;
import threepl.exec.Immediate;
import threepl.exec.RefOrVal;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.nodes.Ident;
import threepl.nodes.Node;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to declare a variable of mode 'type'.
 */
public class TypeProc extends InbuiltProc implements Constant {

    /**
     * Construct the inbuilt procedure var().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public TypeProc () {
        allowed_as_param = true;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
    }

    /**
     * Execute the variable declaration procedure type().
     * @param   inargs is a list of input argument tree nodes
     * @param   outargs is a list of output argument tree nodes
     * @param   names is a list of identifiers evaluated via method
     *          getIdents() prior to calling execute()
     * @param   par_arg is an argument passed to this variable as a parameter in a call
     * @param   dim_des is a dimensional description for a compound constant
     *          argument or is null - it must be null here!
     * @param   is_input is true if this variable is the input parameter
     *          for a module, procedure or function
     * @param   is_output is true if this variable is the output parameter
     *          for a module or procedure 
     * @return  list containing the variable(s)
     */
    public ArrayList<Var> execute (
        NodeList            inargs,
        NodeList            outargs,
        ArrayList<String>   names,
        RefOrVal            par_arg,
        int[]               dim_des,
        boolean             is_input,
        boolean             is_output
    ) {
        SrcLoc      loc = inargs.getCallLoc();
        if (outargs.size() != 0)
            throw new ExEx("type() cannot have output arguments", loc);
        if (dim_des != null)
            throw new ExEx("type() as a parameter cannot have a compound constant argument", loc);
        
        Iterator<Node>          it = inargs.iterator();
        Node                    n;
        ArrayList<Ident>        idents = resolveIdents(inargs, names, "type");
        ArrayList<Var>          vars = new ArrayList<Var>();
        boolean                 initialise = (par_arg == null);
        Val                     init = null;
        
        // variable name argument
        if (!it.hasNext())
            throw new ExEx("type() - no arguments", loc);
        it.next(); // skip 1st argument which has already been processed

        // optional initialisation argument
        if (it.hasNext()) {
            n = it.next();
            if (n == null)
                throw new ExEx("type() - initialisation argument null", n);
            if (initialise) {
                init = n.getVal();
                if (init.getMode() != Mode.IMMEDIATE)
                    throw new ExEx("type() - initialisation must be string or type", n);
            }
        }

        // should be no more arguments
        if (it.hasNext())
            throw new ExEx("type() - too many arguments", loc);
        
        for (Ident id : idents) {
            Immediate ivar = new Immediate(id, new Type(Ptype.TYPE, 0), init, is_input, is_output, loc);
            ThreePL.addVar(ivar, id.getScopeContext(), loc);
            vars.add(ivar);
        }

        return(vars);
    }
}
