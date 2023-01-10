package threepl.procs;

import java.util.ArrayList;
import java.util.ListIterator;

import threepl.ThreePL;
import threepl.exceptions.ExEx;
import threepl.exec.Ref;
import threepl.exec.RefOrVal;
import threepl.exec.SelectValue;
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
 * An inbuilt procedure to declare a SELECTVALUE variable.
 */
public class SelectValueProc extends InbuiltProc implements Constant {

    /**
     * Construct the inbuilt procedure selectvalue().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public SelectValueProc () {
        allowed_as_param = true;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
        allowed_attributes = true;
        ipnames.put("ident", 0);
        ipnames.put("type", 1);
        ipnames.put("default", 2);
    }

    /**
     * Execute the variable declaration procedure selectvalue().
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
            throw new ExEx("selectvalue() cannot have output arguments", loc);
        if (dim_des != null)
            throw new ExEx("selectvalue() as a parameter cannot have a compound constant argument", loc);

        ListIterator<Node>  nit = inargs.listIterator();
        Node                n;
        ArrayList<Ident>    idents = resolveIdents(inargs, names, "value");
        ArrayList<Var>      vars = new ArrayList<Var>();
        Type                type = null;
        boolean             initialise = (par_arg == null);
        Val                 init = null;
        
        // variable name argument
        if (!nit.hasNext())
            throw new ExEx("selectvalue() - no arguments", loc);
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
                throw new ExEx("selectvalue() - a parameter list must have a declared type", loc);
            if (par_arg != null) {
                if (is_input && (par_arg.getMode() != Mode.SELECTVALUE))
                    throw new ExEx("selectvalue() - argument to selectvalue input parameter is not mode selectvalue", loc);
                if (is_output && (par_arg.getMode() != Mode.SELECTVALUE) && (par_arg.getMode() != Mode.VALUE))
                    throw new ExEx("selectvalue() - argument to selectvalue output parameter is not modes selectvalue or value", loc);
                type = par_arg.getType();
                if (type.hasImmediateType()) {
                    Val v;
                    type = type.copy();
                    if (par_arg instanceof Ref)
                        v = ((Ref)par_arg).getVal(loc);
                    else
                        v = (Val)par_arg;
                    type.resolveWidths(v.getVals(), loc);
                }
            } else
                throw new ExEx("selectvalue() - variable has no type argument", loc);
        } else {
            Val     val = n.getVal();
            type = val.getDeclType(par_arg, Mode.VALUE, is_output, null, false, "selectvalue()", loc);
        }

        if (!is_input && !is_output && (type == null))
            throw new ExEx("selectvalue() - variable has no type argument", loc);

        int ln = nit.nextIndex();

        // optional default value argument
        if (nit.hasNext()) {
            n = nit.next();
            if (initialise) {
                if (!(n instanceof KeyMatchNode)) {
                    if (n instanceof CompoundValNode)
                        init = ((CompoundValNode)n).getVal(type);
                    else
                        init = n.getVal();
                    ln++;
                }
            }
        }
        
        if (is_input && (inargs.size() > 2))
            throw new ExEx("selectvalue() - as a parameter, cannot have attribute arguments", loc);

        // optional attribute arguments
        Val aval = attributeArguments("selectvalue", inargs, ln, loc);
        
        for (Ident id : idents) {
            SelectValue svvar = new SelectValue(id, type, init, is_input, is_output, loc);
            ThreePL.addVar(svvar, id.getScopeContext(), loc);
            if (aval != null)
                svvar.setAttributes(aval, loc);
            vars.add(svvar);
        }

        return(vars);
    }
}
