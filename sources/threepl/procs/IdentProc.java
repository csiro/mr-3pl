package threepl.procs;

import java.util.ArrayList;

import threepl.ThreePL;
import threepl.exceptions.ExEx;
import threepl.exec.Immediate;
import threepl.exec.Ref;
import threepl.exec.RefOrVal;
import threepl.exec.SubFieldList;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.exec.WordSpec;
import threepl.nodes.Ident;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt procedure to get an identifier from an argument.
 */
public class IdentProc extends InbuiltProc implements Constant {

    /**
     * Construct the inbuilt procedure ident().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public IdentProc () {
        allowed_as_param = true;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
    }
    
    /**
     * Execute the variable declaration procedure ident().
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
        if (inargs.size() != 1)
            throw new ExEx("ident() must have 1 input argument", loc);
        if (outargs.size() != 0)
            throw new ExEx("ident() cannot have output arguments", loc);
        if (dim_des != null)
            throw new ExEx("ident() cannot have a compound constant argument", loc);
        if (!is_input)
            throw new ExEx("ident() can only be an input parameter", loc);

        ArrayList<Ident>    idents = resolveIdents(inargs, names, "ident");
        Ident               id = idents.get(0);
        Ident               arg_id = null;
        Ref                 ref;
        Val                 val = null;
        
        if (idents.size() != 1)
            throw new ExEx("ident() must have a single identifier as argument", loc);
        if (par_arg == null)
            return(null);

        // Check to see if the argument is a string value rather than an undefined reference.
        if (par_arg instanceof Val) {
            val = (Val)par_arg;
            if ((val.getMode() == Mode.IMMEDIATE) && (val.getPrimType() == Ptype.STR)) {
                String  sname = val.getSingleSval(loc);
                arg_id = new Ident(sname);
            } else
                throw new ExEx("ident() argument is a value, not a variable identifier", loc);
        }
        ref = (Ref)par_arg;
        if (ref.getVar() != null) {
            val = ref.getVal(loc);
            if ((val.getMode() == Mode.IMMEDIATE) && (val.getPrimType() == Ptype.STR)) {
                String  sname = val.getSingleSval(loc);
                arg_id = new Ident(sname);
            }
        }
        
        if (arg_id == null){       
            // Is an undefined reference.
            // Check it has no subscripts/fields.
            SubFieldList    sfl = ref.getSubFields();
            if ((sfl != null) && (sfl.size() != 0))
                throw new ExEx("ident() matching argument cannot have subscripts or fields", loc);
            arg_id = ref.getId();
            if (arg_id == null)
                throw new ExEx("ident() matching argument is not an identifier", loc);
        }
        
        // Construct an identifier context/name structure to return
        Type        type = new Type("(id, str, context, str)", loc);
        Type        st = new Type("str", loc);
        WordSpec    ws = type.getWordSpec(null, loc);
        Object[]    oa = new Object[2];
        Type[]      ta = new Type[2];
        
        oa[0] = arg_id.getId();
        oa[1] = arg_id.getScopeContext().contextname();
        ta[0] = st;
        ta[1] = st;
        Val init = new Val(oa, ta, ws, null, null, loc);
        
        // construct the variable
        Immediate ivar = new Immediate(id, type, init, true, false, loc);
        ThreePL.addVar(ivar, id.getScopeContext(), loc);

        return(null); // to suppresses any assignment!
    }
}
