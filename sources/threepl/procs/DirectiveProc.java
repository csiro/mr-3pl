package threepl.procs;

import threepl.ThreePL;
import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.Ident;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to set a directive in the directive map.
 * This has one or two input arguments and no output arguments.
 * The 1st input argument is the directive key which is a string.
 * The 2nd optional input argument is the directive value which can be an
 * immediate value of type int, log, float or str. If the 2nd argument is
 * not provided the directive will be type "log" and value 'true'.
 */
public class DirectiveProc extends InbuiltProc implements Constant {

    /**
     * Construct the inbuilt procedure directive().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public DirectiveProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
    }

    /**
     * Execute the procedure directive(). This does not generate executable code.
     * @param   inargs is a list of input argument tree nodes
     * @param   outargs is a list of output argument tree nodes
     * @param   toplevel is true if we are at the module, procedure or function
     *          level
     */
    public void execute (
        NodeList    inargs, 
        NodeList    outargs, 
        boolean     toplevel
    ) {
        SrcLoc      loc = inargs.getCallLoc();
        if ((inargs.size() < 1) || (inargs.size() > 2))
            throw new ExEx("directive() must have one or two input arguments", loc);
        if (outargs.size() != 0)
            throw new ExEx("directive() cannot have output arguments", loc);

        Ident   ident = new Ident(inargs.getVal(0).getSingleSval(loc));
        switch (ident.getScopeContext()) {
        case DEFAULT:
        case GLOBAL:    // DEPRECATED - there is only global scope now!
            break;
        default:
            throw new ExEx("directive() - directive identifier cannot have a scope context", loc);
        }
        String  key = ident.getId();
        
        Val     valval;
        if (inargs.size() == 2) {
            valval = inargs.getVal(1);
            if (valval.isTarget())
                throw new ExEx("directive() value cannot be a target variable or expression", loc);
            if (valval.numWords() > 1)
                throw new ExEx("directive() value cannot be an array", loc);
            switch (valval.getPrimType()) {
            case STR:
            case UINT:
            case INT:
            case FLOAT:
            case LOG:
                break;
            default:
                throw new ExEx("directive() value type " + valval.getPrimType().typename() + " not allowed", loc);
            }
        } else
            valval = new Val(Boolean.valueOf(true), loc);
        
        valval.setDummyVar(false, loc);
        ThreePL.addDir(key, valval, loc);
    }
}
