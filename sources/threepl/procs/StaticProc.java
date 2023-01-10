package threepl.procs;

import java.util.ArrayList;
import java.util.ListIterator;

import threepl.ThreePL;
import threepl.exceptions.ExEx;
import threepl.exec.RefOrVal;
import threepl.exec.Static;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.nodes.CompoundValNode;
import threepl.nodes.Ident;
import threepl.nodes.KeyMatchNode;
import threepl.nodes.Node;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to declare a STATIC variable.
 */
public class StaticProc extends InbuiltProc implements Constant {

    /**
     * Construct the inbuilt procedure static().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public StaticProc () {
        allowed_as_param = true;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
        allowed_attributes = true;
    }

    /**
     * Execute the variable declaration procedure static().
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
            throw new ExEx("static() cannot have output arguments", loc);
        if (dim_des != null)
            throw new ExEx("static() as a parameter cannot have a compound constant argument", loc);

        ListIterator<Node>  nit = inargs.listIterator();
        Node                n;
        ArrayList<Ident>    idents = resolveIdents(inargs, names, "static");
        ArrayList<Var>      vars = new ArrayList<Var>();
        Type                type = null;
        boolean             initialise = (par_arg == null);
        Val                 init = null;
        
        // variable name argument
        if (!nit.hasNext())
            throw new ExEx("static() - no arguments", loc);
        nit.next(); // skip 1st argument which has already been processed
        
        if (!nit.hasNext())
            // no type argument - make n null, which we pick up below
            n = null;
        else
            // OK, just get next argument
            n = nit.next();

        if (n == null) {
            // no type argument - use matching type if any (null otherwise)
            if (idents.size() > 1)
                throw new ExEx("static() - a parameter list must have a declared type", loc);
            if (par_arg != null) {
                if (is_input && (par_arg.getMode() != Mode.STATIC))
                    throw new ExEx("static() - argument to static input parameter is not mode static", loc);
                if (is_output && (par_arg.getMode() != Mode.STATIC) && (par_arg.getMode() != Mode.VALUE))
                    throw new ExEx("static() - argument to static output parameter is not modes static or value", loc);
                type = par_arg.getType();
                if (type.hasImmediateType()) {
                    type = type.copy();
                    type.resolveWidths(((Val)par_arg).getVals(), loc);
                }
            }
        } else {
            // type argument
            Val     val = n.getVal();
            type = val.getDeclType(par_arg, Mode.STATIC, is_output, null, false, "static()", loc);
        }


        if (!is_input && !is_output && (type == null))
            throw new ExEx("static() - variable has no type argument", loc);

        int ln = nit.nextIndex();

        // optional initialisation argument
        if (nit.hasNext()) {
            n = nit.next();
            if (!(n instanceof KeyMatchNode)) {
                if (initialise) {
                    if (n instanceof CompoundValNode)
                        init = ((CompoundValNode)n).getVal(type);
                    else
                        init = n.getVal();
                }
                ln++;
            }
        }
        
        if (is_input && (inargs.size() > 3))
            throw new ExEx("static() - as a parameter, cannot have attribute arguments", loc);
        
        // optional attribute arguments
        Val aval = attributeArguments("static", inargs, ln, loc);

        for (Ident id : idents) {
            Static svar = new Static(id, type, init, is_input, is_output, loc);
            ThreePL.addVar(svar, id.getScopeContext(), loc);
            if (aval != null)
                svar.setAttributes(aval, loc);
            vars.add(svar);
        }

        return(vars);
    }
}
