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
 * An inbuilt procedure to declare an immediate file variable.
 * This has 1 or 2 input arguments and no output arguments.
 * The 1st input argument is the variable identifier.
 * The 2nd optional input argument is a string giving the relative
 * or absolute path.
 * The path argument may be omitted where file() is called as a module,
 * procedure or file parameter
 */
public class FileProc extends InbuiltProc implements Constant {

    /**
     * Construct the inbuilt procedure file().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public FileProc () {
        allowed_as_param = true;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
    }
    
    /**
     * Execute the variable declaration procedure file().
     * @param   inargs is a list of input argument tree nodes
     * @param   outargs is a list of output argument tree nodes
     * @param   names is a list of identifiers evaluated via method
     *          getIdents() prior to calling execute()
     * @param   par_arg is a parameter not used for this procedure
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
            throw new ExEx("file() must have 1 or 2 input arguments", loc);
        if (outargs.size() != 0)
            throw new ExEx("file() cannot have output arguments", loc);
        if (dim_des != null)
            throw new ExEx("file() as a parameter cannot have a compound constant argument", loc);

        ArrayList<Ident>    idents = resolveIdents(inargs, names, "file");
        if (idents.size() > 1)
            throw new ExEx("file() call cannot create more than one file variable", loc);
        ArrayList<Var>      vars = new ArrayList<Var>();
        Val                 pathval = null;
                
        // optional path argument
        if (inargs.size() == 2) {
            pathval = inargs.getVal(1);
        }
        
        // construct the variables
        for (Ident id : idents) {
            Immediate ivar = new Immediate(id, new Type(Ptype.FILE, 0), pathval, is_input, is_output, loc);
            ThreePL.addVar(ivar, id.getScopeContext(), loc);
            vars.add(ivar);
        }

        return(vars);
    }
}
