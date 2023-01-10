package threepl.exec;

import static threepl.ThreePL.popBody;
import static threepl.ThreePL.pushBody;
import static threepl.ThreePL.pushScope;
import java.util.ArrayList;

import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.Token;

/**
 * This class is a procedure definition.
 * It extends class 'Body' which contains most of the fields needed.
 */
public class Procedure extends Body implements Constant {

    /**
     * Construct a procedure definition.
     * @param   t is the procedure identifier token which provides both
     *          the procedure identifier and the source file location of the
     *          definition
     * @param   b is the enclosing module, procedure or function or is null
     *          for a file module
     * @param   filescope is the file scope for the module
     * @param   pvt is true if this procedure is declared as 'private'
     */
    public Procedure (Token t, Body b, Scope filescope, boolean pvt) {
        super(t);
        enclosing_body = b;
        file_scope = filescope;
        is_private = pvt;
    }
    
    /**
     * Execute a user-defined procedure.
     * @param   inargs is a list of input argument tree nodes
     * @param   outargs is a list of output argument tree nodes
     * @param   esig is an exception/restart signal, or null
     * @param   startl is a list of start signals for target statements
     *          below this node
     * @param   finishl is a list of finish signals for target statements
     *          below this node
     * @param   queues returns all the queue availability signals
     *          accumulated from code below     
     * @param   toplevel is true if this is the top level in a module
     * @param   availok is true if a previous sync makes a queue availability
     *          wait unnecessary
     * @param   checkedqueues gives queue reads which have already been checked
     * @param   pri_in is an optional input signal to a priority encoder
     * @param   pri_out is an optional output signal from a priority encoder
     */
    public void execute (
        NodeList            inargs,
        NodeList            outargs,
        TDEVar              esig,
        ArrayList<TDEVar>   startl, 
        ArrayList<TDEVar>   finishl,
        QueueRefs           queues,
        boolean             toplevel,
        boolean             availok,
        QueueRefs           checkedqueues,
        TDEVar              pri_in,
        TDEVar              pri_out
    ) {
        String      mess = "procedure '" + name + "' ";
        ArrayList<TDEVar>   bstartl = new ArrayList<TDEVar>();
        ArrayList<TDEVar>   bfinishl = new ArrayList<TDEVar>();

        scope = new Scope(this, Btype.PROC);

        // evaluate the arguments before we push the new scope or the file scope
        getArgs(inargs, true, mess);  // inputs
        getArgs(outargs, false, mess); // outputs
        num_input_args = (in_args == null) ? 0 : in_args.size();
        num_output_args = (out_args == null) ? 0 : out_args.size();
        
        // Push on the body stack.
        pushBody(this);

        // The scope block is created here, not BlockNode.execute(), so it is
        // available early to receive the parameter variables. The scope is
        // initially marked so that searches for variables etc can proceed back
        // up the stack past this scope so that we can evaluate arguments. This
        // is changed at the end of argument evaluation below.
        pushScope(scope, inargs.getSrcLoc());
        
        // create and link the input and output parameters
        params(mess, true, loc);        
        params(mess, false, loc);               

        // Execute the code body.
        tree.execute(esig, bstartl, bfinishl, queues, toplevel, availok, checkedqueues, pri_in, pri_out);
        
        // A procedure must contain no more than 1 in-line target
        // statement
        if (bstartl.size() > 0) {
            if (bstartl.size() > 1)
                throw new ExEx(mess +
                            "has more than 1 in-line target statement", loc);
            // Add start and finish signals from in-line statement to
            // start and finish signal lists.
            startl.add(bstartl.get(0));
            finishl.add(bfinishl.get(0));
        }
        
        // Pop off the body stack.
        popBody();
         
        // The scope block has already been popped off the
        // scope block stack in BlockNode.execute().
    }
    
    /*public SrcLoc getSrcLoc () {
        return(loc);
    }*/
}
