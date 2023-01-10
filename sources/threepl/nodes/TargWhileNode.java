package threepl.nodes;

import static threepl.ThreePL.msg;
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
 * Implements a target while block.
 */
public final class TargWhileNode extends Node implements Constant, TDEConstants {
    
    /**
     * Construct an empty target while node.
     * Subnodes are added later by the parser.
     * @param   t is a token, from which the file source location is extracted
     */
    public TargWhileNode (Token t) {
        super(t);
    }

    /**
     * Execute the target while (..).
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
            throw new ExEx("target 'while' statement nested within 'waitpri' statement", loc);
        
        TDEVar      clock = ThreePL.getCurrentClock();
        if (clock == null)
            throw new ExEx("No current clock domain for target while loop", loc);

        Node                test   = subnodes.getNode(0);
        Node                block = subnodes.getNode(1);
        boolean             generate_iloop = false;
        QueueRefs           test_queues = null;
        QueueRefs           body_queues = new QueueRefs();
        TDEVar              start = tdelist.signal("S", loc);
        TDEVar              startb = tdelist.signal("SB", loc);
        TDEVar              finishb;
        TDEVar              finish;
        ArrayList<TDEVar>   bstartl = new ArrayList<TDEVar>();
        ArrayList<TDEVar>   bfinishl = new ArrayList<TDEVar>();
        EXECR               status = EXECR.NONE;
        
        // test
        Val     testval = test.getVal();
        if (!testval.isPrimitive())
            throw new ExEx("target 'while' statement test expression is not primitive", loc);
        if (testval.getPrimType() != Ptype.LOG)
            throw new ExEx("target 'while' statement test expression is not type log", loc);
        if (testval.getMode() == Mode.IMMEDIATE) {
            if (testval.getSingleLval(loc)) {
                generate_iloop = true;
                test_queues = new QueueRefs();
            } else
                return(status);
        } else if (testval.getTDEVar().isConst()) {
            if (((Boolean)testval.getTDEVar().getConst()).booleanValue()) {
                generate_iloop = true;
                test_queues = new QueueRefs();
            } else
                return(status);
        } else {
            testval.resolveClocks(null, Calloc.ASSIGN, loc);
            if (testval.collectClocks(loc) == null)
                throw new ExEx("target while statement test expression has no clock domain", loc);
            testval.checkNullClocks ("target 'while' statement test expression contains input variables with no clock domain -", loc);
            test_queues = testval.getQueues();     
        }

        
        // block
        if (block != null) {
            status = block.execute(esig, bstartl, bfinishl, body_queues, false, false, test_queues, null, null);
            /*
            if (status != 0)
                throw new ExEx("cannot have break, continue or return in target 'while' statement", loc);
            */
            if (bstartl.size() > 1)
                throw new ExEx("target 'while' block has multiple target statements", loc);
        }
        if (bstartl.size() > 0) {
            tdelist.connect(bstartl.get(0), startb);
            finishb = bfinishl.get(0);
        } else {
            finishb = tdelist.signal("FB", loc);
            tdelist.del(finishb, startb, ThreePL.getCurrentClock(), esig, test_queues);
        }
        
        // check for any queue reads in the test expression which are also
        // in the code body along with at least one other queue read or write
        queue_test(test_queues, body_queues, loc);
            
        if (generate_iloop) {
            TDE tde_iloop = new TDE(TDEType.ILOOP, loc);
            tde_iloop.add2i(start);
            tde_iloop.add2i(finishb);
            tde_iloop.add2o(startb);
            tdelist.addTDE(tde_iloop);
            finish = TDEVar.GND;
        } else {
            TDEVar      start_del  = tdelist.signal("SD", loc);
            TDEVar      contin_del  = tdelist.signal("CD", loc);
            finish  = tdelist.signal("F", loc);
            TDE tde_while = new TDE(TDEType.WHILE, loc);
            tde_while.add2i(start_del);
            tde_while.add2i(testval.getTDEVar());
            tde_while.add2i(contin_del);
            tde_while.add2ic(clock);
            tde_while.add2i(esig);
            tde_while.add2o(startb);
            tde_while.add2o(finish);
            tdelist.addTDE(tde_while);

            TDEVar  rpendingi = tdelist.signal("P", loc);
            TDEVar  rpendingc = tdelist.signal("P", loc);

            // generate initial test expression queue where necessary
            tdelist.connect (
                        start_del,
                        tdelist.execp(test_queues, null, availok, null, false, start, null, null, esig, null, rpendingi, loc)
            );
            // generate continuation test expression queue wait
            tdelist.connect (
                        contin_del,
                        tdelist.execp(test_queues, null, false, null, false, finishb, null, null, esig, null, rpendingc, loc)
            );
            TDEVar  orout = tdelist.signal("OR", loc);
            TDE     or = new TDE(TDEType.OR, loc);
            if (rpendingi.getSrcListSize() != 0) {
                or.add2i(rpendingi);
                or.add2i(rpendingc);
                or.add2o(orout);
                tdelist.addTDE(or);
                
                test_queues.addPops(null, startb, orout, loc);
            }
        }

        startl.add(start);
        finishl.add(finish);

        if (!generate_iloop) {
            queues.and_set(test_queues, loc);
            queues.and_set(body_queues, loc);
        }
        return(status);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void queue_test (QueueRefs tpr, QueueRefs pr, SrcLoc loc) {
        HashSet<Queue>   s = pr.getReads();
        int             init_size = s.size();
        if (init_size == 0)
            return; // body has no queue reads
        s = (HashSet)s.clone();
        HashSet<Queue>   ts = tpr.getReads();
        if (ts.size() == 0)
            return; // test expression has no queue reads
        for (Queue p: ts)  // iterate through test expression queue reads
            s.remove(p);    // remove from the body set if present
        // if the set of body queue reads is unchanged, OK
        if (s.size() == init_size)
            return;
        // if the set of body queue reads is now empty and
        // the body queue writes set is empty, OK
        if ((s.size() == 0) && (pr.getWrites().size() == 0))
            return;
        
        msg("\t" + loc.toString() + " - WARNING");
        msg("\ttarget while() body queue reads and writes set intersects test expression queue reads set");
    }
}
