package threepl.procs;

import java.util.ArrayList;
import java.util.ListIterator;

import threepl.ThreePL;
import threepl.exceptions.ExEx;
import threepl.exec.Priority;
import threepl.exec.RefOrVal;
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
 * An inbuilt procedure to declare a PRIORITY variable.
 */
public class PriorityProc extends InbuiltProc implements Constant {

    /**
     * Construct the inbuilt procedure priority().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public PriorityProc () {
        allowed_as_param = true;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
        allowed_attributes = true;
    }

    /**
     * Execute the variable declaration procedure priority().
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
            throw new ExEx("priority() cannot have output arguments", loc);
        if (dim_des != null)
            throw new ExEx("priority() as a parameter cannot have a compound constant argument", loc);

        ListIterator<Node>  nit = inargs.listIterator();
        Node                n = null;
        ArrayList<Ident>    idents = resolveIdents(inargs, names, "priority");
        ArrayList<Var>      vars = new ArrayList<Var>();
        Type                type = null;
        Val                 init = null;
        long                size = 20;
        
        // variable name argument
        if (!nit.hasNext())
            throw new ExEx("priority() - no arguments", loc);
        if (idents.size() != 1)
            throw new ExEx("priority() must have single identifier as first argument", loc);
        nit.next(); // skip 1st argument which has already been processed
        Ident   id = idents.get(0);
        int     ln= nit.nextIndex();
        
        if (nit.hasNext()) {
            n = nit.next();
            if (n != null) {
                if (!(n instanceof KeyMatchNode)) {
                    // type argument
                    Val     val = n.getVal();
                    if (val.getPrimType() == Ptype.UINT)
                        size = val.getSingleIval(loc);
                    else if ((val.getPrimType() == Ptype.TYPE) || (val.getPrimType() == Ptype.STR))
                        type = val.getDeclType(par_arg, Mode.PRIORITY, is_output, null, false, "priority()", loc);
                    else
                        throw new ExEx("priority() - 2nd argument not \"uint\" or \"[]log\"");
                    ln = nit.nextIndex();
                }
            }
        }
        
        if (type == null) {
            // no type argument - use matching type if any
            if (par_arg != null) {
                if (is_input && (par_arg.getMode() != Mode.PRIORITY))
                    throw new ExEx("priority() - argument to priority input parameter is not mode priority", loc);
                if (is_output && (par_arg.getMode() != Mode.PRIORITY) && (par_arg.getMode() != Mode.VALUE))
                    throw new ExEx("priority() - argument to priority output parameter is not modes priority or value", loc);
                type = par_arg.getType();
                if (type.hasImmediateType()) {
                    type = type.copy();
                    type.resolveWidths(((Val)par_arg).getVals(), loc);
                }
            }
        }
        
        // optional initialisation argument
        if (nit.hasNext()) {
            n = nit.next();
            if (!(n instanceof KeyMatchNode)) {
                if (n instanceof CompoundValNode)
                    throw new ExEx("priority() - initial value cannot be a compound constant", loc);
                else
                    init = n.getVal();
                size = init.getDimWords();
                ln = nit.nextIndex();
            }
        }
        
        if (type == null)
            type = new Type("[" + size + "]log", loc);
        
        if (is_input && (inargs.size() > ln))
            throw new ExEx("priority() - as a parameter, cannot have attribute arguments", loc);

        // optional attribute arguments
        Val aval = attributeArguments("selectvalue", inargs, ln, loc);
        
        Priority pvar = new Priority(id, type, init, is_input, is_output, loc);
        ThreePL.addVar(pvar, id.getScopeContext(), loc);
        if (aval != null)
            pvar.setAttributes(aval, loc);
        vars.add(pvar);
        return(vars);
    }
}
