package threepl.procs;

import java.util.ArrayList;
import java.util.Iterator;

import threepl.ThreePL;
import threepl.exceptions.ExEx;
import threepl.exec.Clock;
import threepl.exec.Immediate;
import threepl.exec.Input;
import threepl.exec.Memory;
import threepl.exec.Output;
import threepl.exec.Queue;
import threepl.exec.Priority;
import threepl.exec.RefOrVal;
import threepl.exec.SelectValue;
import threepl.exec.Static;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.exec.Value;
import threepl.exec.Var;
import threepl.nodes.CompoundValNode;
import threepl.nodes.Ident;
import threepl.nodes.Node;
import threepl.nodes.NodeList;
import threepl.nodes.VarNode;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to declare a variable of any mode or type.
 */
public class VarProc extends InbuiltProc implements Constant {

    /**
     * Construct the inbuilt procedure var().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public VarProc () {
        allowed_as_param = true;
        check_null_input_args = false;
        check_null_output_args = false;
        target_inline = false;
        allowed_attributes = true;
    }

    /**
     * Execute the variable declaration procedure var().
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
            throw new ExEx("var() cannot have output arguments", loc);
        Iterator<Node>      it = inargs.iterator();
        ArrayList<Ident>    idents = resolveIdents(inargs, names, "var");

        return(exec_var("var", it, names, idents, par_arg, dim_des, is_input, is_output, loc));
    }
    
    public static ArrayList<Var> exec_var (
        String              pname,
        Iterator<Node>      it,
        ArrayList<String>   names,
        ArrayList<Ident>    idents,
        RefOrVal            par_arg,
        int[]               dim_des,
        boolean             is_input,
        boolean             is_output,
        SrcLoc              loc
    ) {
        Node                n = null;
        Node                ni = null;
        ArrayList<Var>      vars = new ArrayList<Var>();
        Mode                mode = null;
        Type                type = null;
        Val                 typeval = null;
        boolean             hasmode = true; // explicitly assigned a mode?
        boolean             initialise = (par_arg == null) && (dim_des == null);
        Val                 init = null;
        Var                 var = null;
        
        // variable name argument
        if (!it.hasNext())
            throw new ExEx(pname + "() - no arguments", loc);
        it.next(); // skip first argument as has already been processed
        
        // optional mode argument
        if (it.hasNext()) {
            n = it.next();
            if (n instanceof VarNode) {
                Ident  id = n.getIdentifier(pname + "() - 2nd argument");
                String s = id.getId();
                if (s.equals("immediate"))
                    mode = Mode.IMMEDIATE;
                else if (s.equals("value"))
                    mode = Mode.VALUE;
                else if (s.equals("selectvalue"))
                    mode = Mode.SELECTVALUE;
                else if (s.equals("static"))
                    mode = Mode.STATIC;
                else if (s.equals("queue"))
                    mode = Mode.QUEUE;
                else if (s.equals("priority"))
                    mode = Mode.PRIORITY;
                else
                    hasmode = false;    // no mode explicitly assigned
            } else
                hasmode = false;
        } else
            hasmode = false;
        if (hasmode) {
            // just got mode from node n - advance to next node if any
            if (it.hasNext())
                n = it.next();
            else
                n = null;
        }
        if (!hasmode && (par_arg != null)) {
            mode = par_arg.getMode();
            hasmode = true;     // mode is now fixed, assigned from matching argument
        } else if (mode == null)
            mode = Mode.IMMEDIATE;  // no mode assigned, so assume IMMEDIATE for the present - override if initialisation
        
        if (n == null) {
            // no type argument - use matching type if any (null otherwise)
            if (par_arg != null) {
                if (names.size() > 1)
                    throw new ExEx(pname + "() - a variable list cannot use the argument type", loc);
                type = par_arg.getType();
                if (mode == null)
                    mode = par_arg.getMode();
                if (type.hasImmediateType() && (mode != Mode.IMMEDIATE)) {
                    type = type.copy();
                    type.resolveWidths(((Val)par_arg).getVals(), loc);
                }
            }
        } else {
            // type argument
            typeval = n.getVal();
            if (it.hasNext())
                ni = it.next(); // initialisation argument
            if (mode == null)
                mode = Mode.IMMEDIATE;
            // If we have a compound constant initialiser and the mode is immediate,
            // get the dimensional description in case one or more type arrays
            // are undimensioned. Note that if dim_des is already non-null then
            // the parameter has an argument and this will be used instead of
            // the dim_des from the initialiser.
            if (    (mode == Mode.IMMEDIATE) &&
                    (ni != null) &&
                    (ni instanceof CompoundValNode) &&
                    (dim_des == null) )
                dim_des = ((CompoundValNode)ni).getDimDes();
            type = typeval.getDeclType(par_arg, mode, is_output, dim_des, true, pname + "()", loc);

            // optional initialisation argument
            if (ni != null) {
                if ((mode == Mode.QUEUE) && (type != null) && (type.getPrimType() == Ptype.NULL))
                    throw new ExEx(pname + "() - null queue cannot be initialised", loc);
                if (mode == Mode.SELECTVALUE)
                    throw new ExEx(pname + "() - selectvalue mode cannot be initialised", n);
                if (mode == Mode.PRIORITY)
                    throw new ExEx(pname + "() - priority mode cannot be initialised", n);
                if (initialise) {
                    if (ni instanceof CompoundValNode)
                        init = ((CompoundValNode)ni).getVal(type);
                    else if (ni != null)
                        init = ni.getVal();
                }
                if (init != null)
                    switch (init.getMode()) {
                    case IMMEDIATE:
                    case CLOCK:
                    case VALUE:
                        if (!hasmode)
                            mode = init.getMode();
                        else if (mode != init.getMode())
                            throw new ExEx(pname + "() - initialisation mode conflicts with declared or inferred variable mode", n);
                        break;
                    default:
                        throw new ExEx(pname + "() - initialisation value can only be IMMEDIATE, CLOCK or VALUE mode", n);
                    }
            }
            if ((typeval != null) && (par_arg == null) && (init != null) &&
                ((type.getPrimType() == Ptype.EMPTY) || (type.hasUndimArray()))) {
                // redo type to change from 'empty' to initialisation type
                type = typeval.getDeclType(init, mode, false, null, false, pname + "()", loc);
            }
        }
        
        // If there are any undimensioned arrays that have not been given
        // dimensions from an associated argument or initialiser, set the
        // dimension to 0.
        if (type != null)
            type.zeroUndimArray();

        if (!is_input && !is_output && (type == null))
            throw new ExEx(pname + "() has no type argument", loc);

        if ((mode == Mode.IMMEDIATE) && (type != null) && type.hasTargetType())
            throw new ExEx(pname + "() - immediate mode has target type", loc);
        if ((mode != Mode.IMMEDIATE) && (type != null) && type.hasImmediateType())
            throw new ExEx(pname + "() - target mode has immediate type", loc);

        // should be no more arguments
        if (it.hasNext())
            throw new ExEx(pname + "() - too many arguments", loc);
        
        for (Ident id : idents) {
            switch (mode) {
            case IMMEDIATE:
                var = new Immediate(id, type, init, is_input, is_output, loc);
                break;
            case VALUE:
                var = new Value(id, type, init, is_input, is_output, loc);
                break;
            case SELECTVALUE:
                var = new SelectValue(id, type, init, is_input, is_output, loc);
                break;
            case STATIC:
                var = new Static(id, type, init, is_input, is_output, loc);
                break;
            case QUEUE:
                var = new Queue(id, type, init, is_input, is_output, loc);
                break;
            case PRIORITY:
                var = new Priority(id, type, init, is_input, is_output, loc);
                break;
            case INPUT:
                if (idents.size() != 1)
                    throw new ExEx(pname + "() - cannot create an input mode variable with differential output by using var() or ref()", loc);
                var = new Input(id, type, init, is_input, is_output, null, loc);
                break;
            case OUTPUT:
                var = new Output(id, type, init, is_input, is_output, loc);
                break;
            case CLOCK:
                var = new Clock(id, init, is_input, is_output, loc);
                break;
            case CMEMORY:
                if ((type != null) && (type.getPrimType() != Ptype.NONE))
                    throw new ExEx(pname + "() - creating a parameter passed a cmemory argument cannot have a type", loc);
                var = new Memory(id, false, 0, null, null, is_input, is_output, null, loc);
                break;
            case RMEMORY:
                if ((type != null) && (type.getPrimType() != Ptype.NONE))
                    throw new ExEx(pname + "() - creating a parameter passed an rmemory argument cannot have a type", loc);
                var = new Memory(id, true, 0, null, null, is_input, is_output, null, loc);
                break;
            default:
                throw new ExEx(pname + "() - argument mode " + mode.name() + " not allowed", loc);
            }            
            ThreePL.addVar(var, id.getScopeContext(), loc);
            vars.add(var);
        }

        return(vars);
    }
}
