package threepl.procs;

import threepl.ThreePL;
import threepl.exceptions.ExEx;
import threepl.nodes.Ident;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to remove a directive in the directive map.
 * This has one input argument and no output arguments.
 * The input argument is the directive key which is a string.
 */
public class RemoveDirectiveProc extends InbuiltProc implements Constant {

    /**
     * Construct the inbuilt procedure RemoveDirective().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public RemoveDirectiveProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
    }

    /**
     * Execute the procedure setdirective(). This does not generate executable code.
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
        if (inargs.size() != 2)
            throw new ExEx("removedirective() must have one input argument", loc);
        if (outargs.size() != 0)
            throw new ExEx("removedirective() cannot have output arguments", loc);

        Ident   ident = new Ident(inargs.getVal(0).getSingleSval(loc));
        if(ident.getScopeContext() != Context.DEFAULT)
            throw new ExEx("removedirective() identifier cannot have a scope context", loc);
        String  key = ident.getId();        
        
        ThreePL.remDir(key, loc);
    }
}
