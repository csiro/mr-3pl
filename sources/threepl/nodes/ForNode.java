package threepl.nodes;

import java.util.ArrayList;

import threepl.ThreePL;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.QueueRefs;
import threepl.exec.Scope;
import threepl.parser.Constant;
import threepl.parser.Token;

/**
 * Implements a for (..). This is an immediate
 * control structure and is interpreted, not executed in the FPGA.
 */
public final class ForNode extends Node implements Constant {
    /**
     * Construct a for (..) node.
     * @param   init is the initialisation statement
     * @param   test is the test expression
     * @param   iter is the iteration statement
     * @param   block is the code block
     * @param   t is a token, from which the file source location is extracted
     */
    public ForNode (Node init, Node test, Node iter, Node block, Token t) {
        super(t);
        addSubNode(init);
        addSubNode(test);
        addSubNode(iter);
        addSubNode(block);
    }
    
    /**
     * Execute the for (..).
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
        Node                init = subnodes.getNode(0);
        Node                test = subnodes.getNode(1);
        Node                iter = subnodes.getNode(2);
        Node                bnode = subnodes.getNode(3);
        VarNode             iter_var = null;
        Node                ass_st = null;
        EXECR               status = EXECR.NONE;
        ArrayList<TDEVar>   bstartl = new ArrayList<TDEVar>();
        ArrayList<TDEVar>   bfinishl = new ArrayList<TDEVar>();

        if (iter != null) {
            if (iter instanceof VarNode) {
                // a variable with ++ or --
                iter_var = (VarNode)iter;
            } else {
                // an assignment statement
                ass_st = iter;
            }
        }
        
        // Create an extra scope block to hold a possible
        // enclosed loop variable from the 'for (int("name", val)...'
        // form.
        ThreePL.pushScope(new Scope(Btype.FORVAR), null);
        
        // execute the initial statement
        if (init != null) {
            init.execute(null, bstartl, bfinishl, queues, toplevel, availok, null, pri_in, pri_out);
            if (bstartl.size() != 0)
                throw new ExEx("for init statement contains non-immediate code", loc);
        }

        // execute the loop
        while ((test == null) || test.getSingleLval("for", loc)) {
            QueueRefs p = new QueueRefs();
            if (bnode != null)
                status = bnode.execute(esig, startl, finishl, p, toplevel, availok, checkedqueues, null, null);
            queues.and_set(p, loc);
            // If a break has been executed, exit this block
            // with clear status.
            if (status == EXECR.BREAK) {
                ThreePL.popScope(); // Pop the extra scope block
                return(EXECR.NONE);
            }
            // If a return has been executed, exit this block
            // with 'return' status.
            if (status == EXECR.RETURN) {
                ThreePL.popScope(); // Pop the extra scope block
                return(EXECR.RETURN);
            }
            // A 'continue' status has no effect here -
            // the code skipping was done by exiting the
            // nested contained code blocks.

            // execute the iteration statement
            if (iter != null) {
                if (iter instanceof VarNode) {
                    // fetch variable value to cause ++/--
                    iter_var.getVal();
                } else {
                    // assignment statement
                    ass_st.execute(null, bfinishl, bfinishl, queues, toplevel, availok, null, null, null);
                    if (bstartl.size() != 0)
                        throw new ExEx("for iteration statement contains non-immediate code", loc);
                }
            }
        }

        ThreePL.popScope(); // Pop the extra scope block
        return(EXECR.NONE);
    }
}
