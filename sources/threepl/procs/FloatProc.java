package threepl.procs;

import java.util.ArrayList;

import threepl.ThreePL;
import threepl.exceptions.ExEx;
import threepl.exec.Immediate;
import threepl.exec.RefOrVal;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.nodes.Ident;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt procedure to declare an immediate floating point variable.
 */
public class FloatProc extends InbuiltProc implements Constant {

    /**
     * Construct the inbuilt procedure float().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public FloatProc () {
        allowed_as_param = true;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
        allowed_attributes = true;
    }
    
    /**
     * Execute the variable declaration procedure float().
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
        if ((inargs.size() < 1) || (inargs.size() > 2))
            throw new ExEx("float() must have 1 or 2 input arguments", loc);
        if (outargs.size() != 0)
            throw new ExEx("float() cannot have output arguments", loc);
        if (dim_des != null)
            throw new ExEx("float() as a parameter cannot have a compound constant argument", loc);

        ArrayList<Ident>    idents = resolveIdents(inargs, names, "float");
        ArrayList<Var>      vars = new ArrayList<Var>();
        boolean             initialise = (par_arg == null);
        Val                 init = null;
        long                i;
                
        // optional initialisation argument
        if (initialise && (inargs.size() == 2)) {
            init = inargs.getVal(1);
            if (init.getMode() != Mode.IMMEDIATE)
                throw new ExEx("float() initialisation argument is not immediate mode", loc);
            switch (init.getPrimType()) {
            case FLOAT:
                break;
            case INT:
            case UINT:
                i = init.getSingleIval(loc);
                init = new Val((double)i, loc);
                break;
            default:
                throw new ExEx("float() initialisation argument wrong type", loc);
            }
        }
        
        // construct the variables
        for (Ident id : idents) {
            Immediate ivar = new Immediate(id, new Type(Ptype.FLOAT, 0), init, is_input, is_output, loc);
            ThreePL.addVar(ivar, id.getScopeContext(), loc);
            vars.add(ivar);
        }

        return(vars);
    }
}
