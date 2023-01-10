package threepl.nodes;

import java.util.ArrayList;

import threepl.codegen.TDEVar;
import threepl.exec.QueueRefs;
import threepl.parser.Constant;
import threepl.parser.Token;

/**
 * Implements an if (..) or if (..) else. This is an immediate
 * control structure and is interpreted, not executed in the FPGA.
 */
public final class IfNode extends Node implements Constant {

    /**
     * Construct an if (..) else node.
     * The if or else code blocks may be null
     * @param   test is the test expression
     * @param   tblock is the code block to be executed if the test
     *          value is true, or is null
     * @param   fblock is the code block to be executed if the test
     *          value is false, or is null
     * @param   t is a token, from which the file source location is extracted
     */
    public IfNode (Node test, Node tblock, Node fblock, Token t) {
        super(t);
        addSubNode(test);
        addSubNode(tblock);
        addSubNode(fblock);
    }
    
    /**
     * Execute the if (..) else.
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
     * @return  execution status value
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
        boolean test = subnodes.getNode(0).getSingleLval("if statement test expression", getSrcLoc());
        Node    tblock = subnodes.getNode(1);
        Node    fblock = subnodes.getNode(2);
        EXECR   status = EXECR.NONE;

        if (test) {
            if (tblock != null)
                status = tblock.execute(esig, startl, finishl, queues, toplevel, availok, checkedqueues, pri_in, pri_out);
        } else if (fblock != null)
            status = fblock.execute(esig, startl, finishl, queues, toplevel, availok, checkedqueues, pri_in, pri_out);

        return(status);
    }
}
