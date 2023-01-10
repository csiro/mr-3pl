package threepl.nodes;

import java.util.ArrayList;

import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.QueueRefs;
import threepl.parser.Constant;
import threepl.parser.Token;

/**
 * Implements a do { ...} while (..). This is an immediate
 * control structure and is interpreted, not executed in the FPGA.
 */
public final class DoWhileNode extends Node implements Constant {

    /**
     * Construct a do { ...} while (..) node.
     * @param   block is the code block
     * @param   test is the test expression
     * @param   t is a token, from which the file source location is extracted
     */
    public DoWhileNode (Node block, Node test, Token t) {
        super(t);
        addSubNode(block);
        addSubNode(test);
    }
    
    /**
     * Execute the do { ...} while (..).
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
     * @return  EXECR.NONE or EXECR.RETURN status value
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
        if (pri_in != null)
            throw new ExEx("'do while' statement nested within 'waitpri' statement", loc);
        
        Node    block = subnodes.getNode(0);
        Node    test = subnodes.getNode(1);
        EXECR   status = EXECR.NONE;
        
        do {
            QueueRefs p = new QueueRefs();
            if (block != null)
                status = block.execute(esig, startl, finishl, p, toplevel, availok, checkedqueues, null, null);
            queues.and_set(p, loc);
            // If a break has been executed, exit this block
            // with clear status.
            if (status == EXECR.BREAK)
                return(EXECR.NONE);
            // If a return has been executed, exit this block
            // with 'return' status.
            if (status == EXECR.RETURN)
                return(EXECR.RETURN);
            // A 'continue' status has no effect here -
            // the code skipping was done by exiting the
            // nested contained code blocks.
        } while (test.getSingleLval("do while", loc));

        return(EXECR.NONE);
    }
}
