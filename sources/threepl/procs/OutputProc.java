package threepl.procs;

import java.util.ArrayList;

import threepl.ThreePL;
import threepl.codegen.TDEConstants;
import threepl.exceptions.ExEx;
import threepl.exec.Output;
import threepl.exec.RefOrVal;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.nodes.AddrNode;
import threepl.nodes.CompoundValNode;
import threepl.nodes.Ident;
import threepl.nodes.Node;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to create an output mode variable.
 * This has two or more input arguments and no output arguments. The 1st input
 * argument is optional and is the variable identifier. The 2nd input argument
 * is an optional type or type string. The 3rd argument is optional and is the
 * source driving the output unconditionally. This may be omitted and instead
 * a value is assigned to the output mode variable using := assignment for
 * unconditional output and when (l) o |- v for 3-state conditional output. If
 * the 3rd argument is supplied this output mode variable need not be given an
 * identifier or type, i.e. the 1st and 2nd arguments can be omitted.
 * 
 * The 3rd argument (source) may be a single constant of type "log", i.e. true or false,
 * in the case of a single pin output. For a multi-pin output a source 1-dimensional
 * compound constant of booleans whose dimension matches the number of pins may be used.
 * In future a single unsigned immediate integer may be implemented, the pattern of bits
 * driving the output pins.
 * 
 * If type is missing and the source argument is provided the type can be
 * determined from the source value, however in this case the source value can
 * not be immediate unless it is type "log".
 * 
 * Note that either the 1st argument (variable identifier) or the 3rd argument
 * (source) must be provided otherwise this output variable would have
 * no source and could not be assigned a value.
 *
 * <p>Following the first three arguments further optional arguments may be
 * supplied to specify attributes. Each of these arguments can be -
 * <ul>
 *  <li> an attributes map
 *  <li> a key=value argument
 * </ul>
 * 
 * If output() is called to create a module, procedure or function parameter the
 * 1st (identifier) argument must be present but the 2nd argument (type)
 * is optional. If omitted the matching argument type will be used. If provided
 * it must be identical to that of the argument. The 3rd argument (source) must
 * be omitted. Attributes may follow as above and may add to or override attributes
 * already assigned to the argument variable.
 */
public class OutputProc extends InbuiltProc implements Constant, TDEConstants {
    static private int      uid = 0;  // use and increment for unique IDs

    /**
     * Construct the inbuilt procedure output().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public OutputProc () {
        allowed_as_param = true;
        check_null_input_args = false;
        check_null_output_args = false;
        target_inline = false;
        allowed_attributes = true;
   }

    /**
     * Execute the procedure output(). This does not generate in-line code.
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
        int                 nargs = inargs.size();
        Ident               id = null;
        Val                 srcval = null;
        Type                type = null;
        Type                atype = null;
        Type                srctype = null;
        ArrayList<Var>      vars = new ArrayList<Var>();
        boolean             is_par = is_input || is_output;

        if (is_par) {
            if (in == 0)
                throw new ExEx("output() called to create a parameter must have one or more arguments", loc);
        } else {
            if (in < 2)
                throw new ExEx("output() must have 2 or more input arguments", loc);
        }
        if (outargs.size() != 0)
            throw new ExEx("output() must have no output arguments", loc);

        // optional variable identifier argument
        if (inargs.getNode(0) != null)
            id = inargs.getIdent(0, "output(): ");
        
        // type argument
        Val     typeval = inargs.getVal(1); // null if argument not provided
        if (typeval != null)
            type = typeval.getDeclType(null, Mode.OUTPUT, false, null, false, "output()", loc);

        // argument type if any
        atype = par_arg == null ? null : par_arg.getType();

        // optional 3rd argument - source
        if (nargs > 2) {
            Node    n = inargs.getNode(2);
            
            if (n != null) {
                if (n instanceof CompoundValNode)
                    srcval = ((CompoundValNode)n).getVal(type);
                else
                    srcval = n.getVal();
            
                if ((srcval.getMode() == Mode.IMMEDIATE) && (type == null)) {
                    type = srcval.getCheckType();
                    if (type.getPrimType() != Ptype.LOG)
                        throw new ExEx("output() - constant source requires type argument if not \"log\"", loc);
                    if (type.getPrimType() == Ptype.LOG) {
                        srcval.toTarget();
                        type = new Type(Ptype.LOG, 1);  // change type from immediate log to target log
                    }
                }
                if (srcval.getPrimType() == Ptype.CLASS)
                    throw new ExEx("output() source (3rd) argument cannot be type \"class\"", loc);
                if (srcval.getPrimType() == Ptype.LIST)
                    throw new ExEx("output() source (3rd) argument cannot be type \"list\"", loc);
                if (srcval.getPrimType() == Ptype.MAP)
                    throw new ExEx("output() source (3rd) argument cannot be type \"map\"", loc);
                if (!srcval.getQueues().isEmpty())
                    throw new ExEx("output() source (3rd) argument cannot have queue reads", loc);
                srcval.collectOutputClocks(loc);
                srcval.setUsed();
                srctype = srcval.getType();
            }
        }
        
        if (is_par) {
            // is a module, procedure or function parameter
            if (par_arg.getVar().getMode() != Mode.OUTPUT)
                throw new ExEx("output() called as a parameter - argument is not output mode", loc);
            if (par_arg != null) {
                if ((type != null) && !type.isEqual(atype, true))
                    throw new ExEx("output() - parameter type differs from that of matching argument");
            }
        }
        
        if (type == null) {
            if (srctype != null)
                type = srctype;
            else if (atype != null)
                type = atype;
            else
                throw new ExEx("output() must have either a type (2nd argument), a source (3rd argument) or an argument type", loc);
        }
        
        if (is_input && (inargs.size() > 2))
            throw new ExEx("output() - as a parameter, cannot have attribute arguments", loc);
        
        // optional attribute arguments
        Val aval = attributeArguments("output", inargs, 3, loc);

        // create the output mode variable
        if (id == null)
            id = new Ident("OUTPUT" + uid++);
        Output ovar = new Output(id, type, srcval, is_input, is_output, loc);
        if (id != null)
            ThreePL.addVar(ovar, id.getScopeContext(), loc);
        
        // If there are attribute arguments add them to the attribute map
        if (aval != null)
            ovar.setAttributes(aval, loc);
        
        // Process any pin attributes.
        if (ThreePL.attributesProc != null) {
            NodeList    nl = new NodeList(null);
            AddrNode     vn = new AddrNode(ovar);
            nl.add(vn);
            ThreePL.attributesProc.execute(nl, null, null, null, null, null, true, false, null, null, null);
        }
        
        // add this variable to the list of variables.
        vars.add(ovar);
        return(vars);
    }
}
