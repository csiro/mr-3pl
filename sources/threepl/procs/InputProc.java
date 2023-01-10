package threepl.procs;

import java.util.ArrayList;

import threepl.ThreePL;
import threepl.codegen.TDEConstants;
import threepl.exceptions.ExEx;
import threepl.exec.Input;
import threepl.exec.RefOrVal;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.exec.Value;
import threepl.exec.Var;
import threepl.nodes.AddrNode;
import threepl.nodes.Ident;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to create an input mode variable.
 * This has two or more input arguments and no output arguments. The 1st
 * input argument is the variable identifier. The 2nd input argument is
 * the type or type string.
 *
 * <p>Each of the optional following arguments can be -
 * <ul>
 *  <li> an attributes map
 *  <li> a key=value argument
 * </ul>
 */
public class InputProc extends InbuiltProc implements Constant, TDEConstants {
    static private int      uid = 0;  // use and increment for unique IDs

    /**
     * Construct the inbuilt procedure input().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public InputProc () {
        allowed_as_param = true;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
        allowed_attributes = true;
    }

    /**s
     * Execute the procedure input(). This does not generate in-line
     * executable code.
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
        SrcLoc              loc = inargs.getCallLoc();
        int                 in = inargs.size();
        Ident               id1;
        Ident               id2 = null;
        ArrayList<Var>      vars = new ArrayList<Var>();
        boolean             is_par = is_input || is_output;
        Type                type = null;
        Type                atype = null;
        
        if (is_par) {
            if (in == 0)
                throw new ExEx("input() called to create a parameter must have one or more arguments", loc);
        } else {
            if (in < 2)
                throw new ExEx("input() must have 2 or more input arguments", loc);
        }
        if (outargs.size() != 0)
            throw new ExEx("input() must have no output arguments", loc);

        ArrayList<Ident>    idents = resolveIdents(inargs, null, "int");
        
        switch (idents.size()) {
        case 2:
            if (is_par)
                throw new ExEx("input() called as a parameter cannot have two identifiers", loc);
            id1 = idents.get(0);
            id2 = idents.get(1);
            break;
        case 1:
            id1 = idents.get(0);
            break;
        default:
            throw new ExEx("input() cannot have more than two identifiers", loc);
        }
        
        // type argument
        Val     typeval = inargs.getVal(1);
        if (typeval != null)
            type = typeval.getDeclType(null, Mode.INPUT, false, null, false, "input()", loc);

        // argument type if any
        atype = par_arg == null ? null : par_arg.getType();
        
        if (is_par) {
            // is a module, procedure or function parameter
            if (par_arg.getVar().getMode() != Mode.INPUT)
                throw new ExEx("input() called as a parameter - argument is not input mode", loc);
            if (par_arg != null) {
                if ((type != null) && !type.isEqual(atype, true))
                    throw new ExEx("input() - parameter type differs from that of matching argument");
            }
        }

        if (type == null) {
            if (atype != null)
                type = atype;
            else
                throw new ExEx("input() must have either a type (2nd argument) or an argument type", loc);
        }
        
        if (is_input && (inargs.size() > 2))
            throw new ExEx("input() - as a parameter, cannot have attribute arguments", loc);

        // optional attribute arguments
        Val aval = attributeArguments("input() - ", inargs, 2, loc);
        
        // If second identifier is provided, create a value mode variable for it.
        Value   vvar = null;
        if (id2 != null) {
            vvar = new Value(id2, type, null, false, false, loc);
            ThreePL.addVar(vvar, id2.getScopeContext(), loc);
        }

        // create the input mode variable
        if (id1 == null)
            id1 = new Ident("INPUT" + uid++);
        Input ivar = new Input(id1, type, null, is_input, is_output, vvar, loc);
        if (id1 != null)
            ThreePL.addVar(ivar, id1.getScopeContext(), loc);

        // If there are attribute arguments add them to the attribute map
        if (aval != null)
            ivar.setAttributes(aval, loc);
        
        // Process any pin attributes.
        if (ThreePL.attributesProc != null) {
            NodeList    nl = new NodeList(null);
            AddrNode     vn = new AddrNode(ivar);
            nl.add(vn);
            ThreePL.attributesProc.execute(nl, null, null, null, null, null, true, false, null, null, null);
        }
        
        // add this variable to the list of variables.
        vars.add(ivar);
        return(vars);
    }
}
