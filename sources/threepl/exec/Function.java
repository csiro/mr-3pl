package threepl.exec;

import static threepl.ThreePL.popBody;
import static threepl.ThreePL.pushBody;
import static threepl.ThreePL.queueUsed;
import static threepl.ThreePL.pushScope;
import static threepl.ThreePL.tdelist;

import java.util.ArrayList;
import java.util.HashSet;

import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.Token;

/**
 * This class is a source code function definition.
 * It extends class 'Body' which contains most of the fields needed.
 */
public class Function extends Body implements Constant {
    private Val     retval;             // returned function value
    private Val     usedval;            // 'used' value

    /**
     * Construct a function definition.
     * @param   t is the function identifier token which provides both
     *          the function identifier and the source file location of the
     *          definition
     * @param   b is the enclosing module, procedure or function or is null
     *          for a file module
     * @param   filescope is the file scope for the module
     * @param   pvt is true if this procedure is declared as 'private'
     */
    public Function (Token t, Body b, Scope filescope, boolean pvt) {
        super(t);
        enclosing_body = b;
        file_scope = filescope;
        is_private = pvt;
    }
    
    /**
     * Call a source code function and get the return value.
     * @param   args is a list of argument tree nodes
     * @return  the value resulting from the function call
     */
    public Val getVal (NodeList args) {
        String  mess = "function '" + name + "' ";
        scope = new Scope(this, Btype.FUNC);
        
        // evaluate the arguments before we push the new scope or the file scope
        if (args != null) {
            getArgs(args, true, mess);  // inputs
            num_input_args = (in_args == null) ? 0 : in_args.size();
        }
        
        // Push on the body stack.
        pushBody(this);

        // The scope block is created here, not BlockNode.execute(), so it is
        // available early to receive the parameter variables. The scope is
        // initially marked so that searches for variables etc can proceed back
        // up the stack past this scope so that we can evaluate arguments. This
        // is changed at the end of argument evaluation below.
        pushScope(scope, args.getSrcLoc());
        
        // create and link the input parameters
        params(mess, true, loc);        

        // Set the return value to null in case of missing return statement
        // Clear the return request flag.
        retval = null;
        
        // Initialise the usedval field to null.
        usedval = null;

        // Execute the code body.
        ArrayList<TDEVar>   startl = new ArrayList<TDEVar>();
        ArrayList<TDEVar>   finishl = new ArrayList<TDEVar>();
        tree.execute(null, startl, finishl, new QueueRefs(), false, false, null, null, null);
        if (finishl.size() > 0)
            throw new ExEx(mess + "returning target mode value" +
                            " executes target statements", loc);

        // Pop off the body stack.
        popBody();
                
        // The scope block has already been popped off the
        // scope block stack in BlockNode.execute().
        
        // If the function used() has been called within this function
        if (usedval != null) {
            HashSet<TDEVar> hs = new HashSet<TDEVar>();
            queueUsed(usedval, hs);
            retval.getExecSets().add(hs);
        }

        // Return the return value
        if (retval == null)
            throw new ExEx(mess + "has not returned a value", loc);
        return(retval);
    }
    
    /**
     * Save the return value for the function call.
     * @param   r is the value to be returned by the function
     */
    public void setReturn (Val r) {
        retval = r;
    }
    
    /**
     * Set a 'used' value for the function call, i.e. function used() has been called.
     * On first use of this function instance generate a new Val usedval.
     * On further calls to used() return the established Val usedval.
     * @return  a target value which is true when the function is executed in the FPGA
     */
    public Val setUsedFunc () {
        if (usedval == null) {
            WordSpec    logws = new WordSpec(1, Ptype.LOG);
            TDEVar      tdev = tdelist.signal("USED", logws,loc);
            usedval = new Val(null, Mode.VALUE, tdev, loc);
        }
        return(usedval);
    }
}
