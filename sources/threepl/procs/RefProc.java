package threepl.procs;

import java.util.ArrayList;
import java.util.Iterator;

import threepl.exceptions.ExEx;
import threepl.exec.RefOrVal;
import threepl.exec.Var;
import threepl.nodes.*;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to declare a variable of any mode or type.
 * This only differs from var() when used as a parameter constructor
 * in which case it guarantees that the argument will be passed by
 * reference, not by value. If the argument is not a variable an
 * error exit is taken.
 */
public class RefProc extends InbuiltProc implements Constant {

    /**
     * Construct the inbuilt procedure ref().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public RefProc () {
        allowed_as_param = true;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
        ipnames.put("ident", 0);
        ipnames.put("mode", 1);
        ipnames.put("type", 2);
        ipnames.put("init", 3);
    }

    /**
     * Execute the variable declaration procedure ref().
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
            throw new ExEx("ref() cannot have output arguments", loc);
        Iterator<Node>      it = inargs.iterator();
        ArrayList<Ident>    idents = resolveIdents(inargs, names, "ref");

        return(VarProc.exec_var("ref", it, names, idents, par_arg, dim_des, is_input, is_output, loc));
    }
}
