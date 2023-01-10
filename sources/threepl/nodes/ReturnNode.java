package threepl.nodes;

import java.util.ArrayList;

import threepl.ThreePL;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Body;
import threepl.exec.Function;
import threepl.exec.QueueRefs;
import threepl.exec.Procedure;
import threepl.exec.Scope;
import threepl.exec.Val;
import threepl.parser.Constant;
import threepl.parser.Token;

/**
 * This implements a return statement.
 */
public final class ReturnNode extends Node implements Constant {
    protected String    id;
    
    /**
     * Construct a return node.
     * @param   n is the argument to the return, if there is one
     * @param   t is the operator token, from which the file source location
     *          is extracted
     */
    public ReturnNode (Node n, Token t) {
        super(t);
        addSubNode(n);
    }
    
    /**
     * Execute a 'return'. If there is a return value, evaluated
     * from the optional argument, this value is stored in the function
     * <b>Body</b>.
     * The start execution signal is returned as the finish execution signal.
     * the 'RETURN' status
     * code is returned so that enclosing codes knows a return
     * has been encountered.
     * @param   esig is an exception/restart signal, or null
     * @param   startl is a list of start signals for target statements
     *          below this node
     * @param   finishl is a list of finish signals for target statements
     *          below this node
     * @param   queues returns all the queue availability signals
     *          accumulated from code below (not used)
     * @param   toplevel is true if this is the top level in a module
     * @param   availok is true if a previous sync makes a queue availability
     *          wait unnecessary
     * @param   checkedqueues gives queue reads which have already been checked
     * @param   pri_in is an optional input signal to a priority encoder
     * @param   pri_out is an optional output signal from a priority encoder
     * @return  the EXECR.RETURN status value
     */
    public EXECR execute (
        TDEVar              esig,
        ArrayList<TDEVar>   startl, 
        ArrayList<TDEVar>   finishl,
        QueueRefs            queues,
        boolean             toplevel,
        boolean             availok,
        QueueRefs            checkedqueues,
        TDEVar              pri_in,
        TDEVar              pri_out
    ) {
        if (pri_in != null)
            throw new ExEx("'return' statement nested within 'waitpri' statement", loc);
        
        Node    n = subnodes.getNode(0);
        Scope   scope = ThreePL.getCurrentBodyScope();
        if (scope == null)
            return(EXECR.RETURN);   // return from main, i.e. exit

        Body    body = scope.getBody();
        if (body instanceof Function) {
            // A function - can have a return value
            if (n != null) {
                Function    f = (Function)body;
                Val         ret = n.getVal();
                f.setReturn(ret);
            }
        } else if (body instanceof Procedure) {
            // A procedure - cannot have a return value
            if (n != null)
                throw new ExEx("procedure cannot return a value", loc);
        } else {
            // Must be a module - cannot have a return value
            if (n != null)
                throw new ExEx("module cannot return a value", loc);
        }
        return(EXECR.RETURN);
    }
}
