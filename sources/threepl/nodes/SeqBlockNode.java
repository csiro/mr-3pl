package threepl.nodes;

import static threepl.ThreePL.tdelist;

import java.util.ArrayList;

import threepl.ThreePL;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.QueueRefs;
import threepl.exec.Scope;
import threepl.parser.Constant;
import threepl.parser.Token;

/**
 * Implements a target seq block.
 */
public final class SeqBlockNode extends BlockNode implements Constant {
    
    /**
     * Construct an empty target seq block node.
     * Subnodes are added later by the parser.
     * @param   t is a token, from which the file source location is extracted
     */
    public SeqBlockNode (Token t) {
        super(Btype.SEQ, t, null);
    }

    /**
     * Execute the target seq block.
     * This generates target code.
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
     * @return  EXECR.NONE execution status value
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
        if (pri_out != null)
            throw new ExEx("'seq' statement nested within 'waitpri' statement", loc);
        
        ThreePL.pushScope(new Scope(Btype.SEQ), null);

        ArrayList<TDEVar>   bstartl = new ArrayList<TDEVar>();
        ArrayList<TDEVar>   bfinishl = new ArrayList<TDEVar>();
        EXECR       status;

        for (Node n: subnodes) {
            if (n == null)
                continue;
            QueueRefs p = new QueueRefs();
            status = n.execute(esig, bstartl, bfinishl, p, false, availok, checkedqueues, null, null);
            if (bstartl.size() > 0) {   // have had at least one target statement?
                availok = false;        // false after 1st target statement
                checkedqueues = null;    // null after 1st target statement
            }
            queues.and_set(p, loc);
            if (status != EXECR.NONE)
                throw new ExEx("cannot have break, continue or return in seq block", loc);
        }
        if (bstartl.size() == 0) {
            // No enclosed target statements - nothing to link up.
            ThreePL.popScope();
            return(EXECR.NONE);
        }

        // Connect start signal to 1st enclosed target statement start signal.
        // Chain enclosed target statement start and finish signals.
        // 
        TDEVar  start = tdelist.signal("S", loc);
        TDEVar  t = start;
        for (int i=0 ; i<bstartl.size() ; i++) {
            if (t.equalOrLinked(TDEVar.GND))
                throw new ExEx("stop() (possibly nested) followed by a target statement in seq block", loc);
            if (bfinishl.size() > bstartl.size())
                throw new ExEx("clock() called inside seq block", loc);
            tdelist.connect(bstartl.get(i), t);
            t = bfinishl.get(i);
        }

        ThreePL.popScope();

        if (bstartl.size() > 0) {
            // Add start signal to start signal list.
            // Add last enclosed target statement finish signal to finish
            // signal list.
            startl.add(start);
            finishl.add(t);
        }
        return(EXECR.NONE);
    }
}
