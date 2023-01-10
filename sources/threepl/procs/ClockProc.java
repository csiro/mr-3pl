package threepl.procs;

import static threepl.ThreePL.addVar;

import java.util.ArrayList;

import threepl.ThreePL;
import threepl.codegen.TDEConstants;
import threepl.exceptions.ExEx;
import threepl.exec.Clock;
import threepl.exec.RefOrVal;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.nodes.AddrNode;
import threepl.nodes.Ident;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * <p>An inbuilt procedure to create a clock variable.
 * This has no output arguments.
 * When creating a clock variable this has 1 or more input arguments.
 * When used to create a module, procedure or function parameter
 * it has a single input argument.
 *
 * <p>The 1st argument is an unsubscripted signal
 * identifier, or a compound value of unsubscripted signal
 * identifiers. These are the identifiers of the clock variables to be
 * created.
 *
 * <p>Each of the optional following arguments can be -
 * <ul>
 *  <li> an attributes map
 *  <li> a key=value argument
 * </ul>
 */
public class ClockProc extends InbuiltProc implements Constant, TDEConstants {

    /**
     * Construct the inbuilt procedure clock().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public ClockProc () {
        allowed_as_param = true;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
        allowed_attributes = true;
    }

    /**
     * Execute the procedure clock(). This does not generate executable
     * code.
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
        SrcLoc  loc = inargs.getCallLoc();
        int     in = inargs.size();
        if (is_input || is_output) {
            if (in != 1)
                throw new ExEx("clock() as parameter must have 1 input argument", loc);
        }
        if (in == 0)
            throw new ExEx("clock() has no input arguments", loc);
        if (outargs.size() != 0)
            throw new ExEx("clock() must have no output arguments", loc);
        if (dim_des != null)
            throw new ExEx("clock() as a parameter cannot have a compound constant argument", loc);

        ArrayList<Ident>    idents = resolveIdents(inargs, names, "clock");
        ArrayList<Var>      vars = new ArrayList<Var>();
        
        if (is_input && (inargs.size() > 2))
            throw new ExEx("clock() - as a parameter, cannot have attribute arguments", loc);

        // optional attribute arguments
        Val aval = attributeArguments("clock", inargs, 1, loc);

        for (Ident id : idents) {
            Clock cvar = new Clock(id, null, is_input, is_output, loc);
            addVar(cvar, id.getScopeContext(), loc);
            vars.add(cvar);
            if (aval != null) {
                cvar.setAttributes(aval, loc);
                if (ThreePL.attributesProc != null) {
                    NodeList    nl = new NodeList(null);
                    AddrNode     vn = new AddrNode(cvar);
                    nl.add(vn);
                    ThreePL.attributesProc.execute(nl, null, null, null, null, null, true, false, null, null, null);
                }
            }
        }
        
        return(vars);
    }
}
