package threepl.nodes;

import java.util.ArrayList;

import threepl.codegen.TDEVar;
import threepl.exec.QueueRefs;
import threepl.parser.Constant;
import threepl.parser.Token;

/**
 * This implements a continue statement.
 * This does nothing and is only used as a marker in the code tree.
 */
public final class ContinNode extends Node implements Constant {

    /**
     * Construct a contine node.
     * @param   t is the operator token, from which the file source location
     *          is extracted
     */
    public ContinNode (Token t) {
        super(t);
    }

    /**
     * There is no execution, however the 'CONTINUE' status
     * code is returned so that enclosing codes knows a continue
     * has been encountered.
     * The start execution signal is returned as the finish execution signal.
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
     * @return  the EXECR.CONTINUE status value
     */
    public EXECR execute (
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
        return(EXECR.CONTINUE);
    }
}
