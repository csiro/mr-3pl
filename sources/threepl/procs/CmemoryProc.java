package threepl.procs;

import java.util.ArrayList;

import threepl.codegen.TDEConstants;
import threepl.exec.RefOrVal;
import threepl.exec.Var;
import threepl.nodes.NodeList;
import threepl.parser.Constant;


/**
 * An inbuilt procedure to declare a combinatorial output RAM block.
 * This has 1, 4 or 5 input arguments and no output arguments.
 *
 * <p>The 1st input argument is the memory mode variable.
 *
 * <p>The 2nd input argument is the number of ports.
 *
 * <p>The 3rd input argument is the address type.
 *
 * <p>The 4th input argument is the data type.
 *
 * <p>The 5th input argument is an immediate array of initialisation
 * values.
 *
 * For a module, procedure or function parameter only the 1st argument
 * is required. For a variable declaration the first 4 arguments must
 * be provided but the 5th is optional. Trailing attribute key-value arguments
 * may be appended.
 */
public class CmemoryProc extends InbuiltProc implements Constant, TDEConstants {

    /**
     * Construct the inbuilt procedure cmemory().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public CmemoryProc () {
        allowed_as_param = true;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
        allowed_attributes = true;
        ipnames.put("id", 0);
        ipnames.put("ports", 1);
        ipnames.put("atype", 2);
        ipnames.put("dtype", 3);
        ipnames.put("init", 4);
    }

    /**
     * Execute the memory variable declaration procedure cmemory().
     * @param   inargs is a list of input argument tree nodes
     * @param   outargs is a list of output argument tree nodes
     * @param   names is a list of identifiers evaluated via method
     *          getIdents() prior to calling execute()
     * @param   par_arg is an argument that has been 
     *          passed to a parameter and is used to get the parameter
     *          type if none is declared
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
        return(MemoryCommon.process(inargs, outargs, names, par_arg, dim_des,
                                is_input, is_output, false));
    }
}
