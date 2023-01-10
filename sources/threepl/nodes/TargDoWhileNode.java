package threepl.nodes;

import static threepl.ThreePL.rpt;
import static threepl.ThreePL.tdelist;

import java.util.ArrayList;
import java.util.HashSet;

import threepl.ThreePL;
import threepl.codegen.TDE;
import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.*;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;
import threepl.parser.Token;

/**
 * Implements a target do while block.
 */
public final class TargDoWhileNode extends Node implements Constant, TDEConstants {
    
    /**
     * Construct an empty target do while node.
     * Subnodes are added later by the parser.
     * @param   t is a token, from which the file source location is extracted
     */
    public TargDoWhileNode (Token t) {
        super(t);
    }

    /**
     * Execute the target do while (..).
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
        if (pri_out != null)
            throw new ExEx("target 'do while' statement nested within 'waitpri' statement", loc);

        TDEVar      clock = ThreePL.getCurrentClock();
        if (clock == null)
            throw new ExEx("No current clock domain for target do while loop", loc);

        Node                block = subnodes.getNode(0);
        Node                test   = subnodes.getNode(1);
        QueueRefs            test_queues;
        QueueRefs            body_queues = new QueueRefs();
        TDEVar              start = tdelist.signal("S", loc);
        TDEVar              startb = tdelist.signal("SB", loc);
        TDEVar              finishb_del = tdelist.signal("FD", loc);
        TDEVar              finishb = null;
        TDEVar              finish = tdelist.signal("F", loc);
        ArrayList<TDEVar>   bstartl = new ArrayList<TDEVar>();
        ArrayList<TDEVar>   bfinishl = new ArrayList<TDEVar>();
        EXECR               status;

        // block
        status = block.execute(esig, bstartl, bfinishl, body_queues, false, false, null, null, null);
        /*
        if ((block != null) && (status != 0))
            throw new ExEx("cannot have break, continue or return in target 'do while' statement", loc);
        */
        if (bstartl.size() > 0) {
            tdelist.connect(bstartl.get(0), startb);
            finishb = bfinishl.get(0);
        } else {
            finishb = tdelist.signal("FB", loc);
            tdelist.del(finishb, startb, ThreePL.getCurrentClock(), esig, body_queues);
        }
        
        // test
        Val     testval = test.getVal();
        testval.resolveClocks(null, Calloc.ASSIGN, loc);
        if (testval.getMode() == Mode.IMMEDIATE)
            throw new ExEx("target 'do while' statement test expression is immediate", loc);
        if (!testval.isPrimitive())
            throw new ExEx("target 'do while' statement test expression is not primitive", loc);
        if (testval.getPrimType() != Ptype.LOG)
            throw new ExEx("target 'do while' statement test expression is not type log", loc);
        if (testval.getTDEVar().isConst())
            throw new ExEx("target 'do while' statement test expression is constant", loc);
        if (testval.collectClocks(loc) == null)
            throw new ExEx("target do-while statement test expression has no clock domain", loc);
        testval.checkNullClocks ("target 'do-while' statement test expression contains input variables with no clock domain -", loc);
        test_queues = testval.getQueues();
        
        // check for any queue reads in the test expression which are also
        // in the code body
        queue_test(test_queues, body_queues, loc);
        
        TDEVar  rpending = tdelist.signal("P", loc);
        
        // generate the queue pops
        test_queues.addPops(null, finishb_del, rpending, loc);

        // generate queue wait
        tdelist.connect (
                    finishb_del,
                    tdelist.execp(test_queues, null, availok, null, false, finishb, null, null, esig, null, rpending, loc)
        );
        
        TDE     tde_dowhile = new TDE(TDEType.DOWHILE, loc);
        tde_dowhile.add2i(start);
        tde_dowhile.add2i(testval.getTDEVar());
        tde_dowhile.add2i(finishb_del);
        tde_dowhile.add2ic(clock);
        tde_dowhile.add2i(esig);
        tde_dowhile.add2o(startb);
        tde_dowhile.add2o(finish);
        tdelist.addTDE(tde_dowhile);

        startl.add(start);
        finishl.add(finish);

        queues.and_set(test_queues, loc);
        queues.and_set(body_queues, loc);
        return(status);
    }

    @SuppressWarnings("unchecked")
    private void queue_test (QueueRefs tpr, QueueRefs pr, SrcLoc loc) {
        HashSet<Queue>     s = pr.getReads();
        int         init_size = s.size();
        if (init_size == 0)
            return; // body has no queue reads
        s = (HashSet<Queue>)s.clone();
        HashSet<Queue>     ts = tpr.getReads();
        if (ts.size() == 0)
            return; // test expression has no queue reads
        for (Queue p:ts)  // iterate through test expression queue reads
            s.remove(p);    // remove from the body set if present
        // if the set of body queue reads is unchanged, OK
        if (s.size() == init_size)
            return;
        // if the set of body queue reads is now empty and
        // the body queue writes set is empty, OK
        if ((s.size() == 0) && (pr.getWrites().size() == 0))
            return;
        
        rpt("\t" + loc.toString() + " - WARNING");
        rpt("\ttarget do while() body queue reads and writes set intersects test expression queue reads set");
    }
}

