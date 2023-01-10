package threepl.nodes;

import static threepl.ThreePL.tdelist;

import java.util.ArrayList;

import threepl.ThreePL;
import threepl.codegen.TDE;
import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.QueueRefs;
import threepl.exec.Val;
import threepl.parser.Constant;
import threepl.parser.Token;

/**
 * Implements a target 'sync when' () {}.
 */
public final class SyncWhenNode extends Node implements Constant, TDEConstants {

    /**
     * Construct a target 'sync when' (..) {...} node.
     * @param   test is the test expression
     * @param   tblock is the code block to be executed if the test
     *          value is true
     * @param   t is a token, from which the file source location is extracted
     */
    public SyncWhenNode (
        Node test,
        Node tblock,
        Token t
    ) {
        super(t);
        addSubNode(test);
        addSubNode(tblock);
    }
    
    /**
     * Execute the when (..) else.
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
        TDEVar      clock = ThreePL.getCurrentClock();
        if (clock == null)
            throw new ExEx("No current clock domain for when", loc);
        if (availok)
            throw new ExEx("sync when nested within sync or sync when", loc);

        Node                test   = subnodes.getNode(0);
        Node                tblock = subnodes.getNode(1);
        QueueRefs           test_queues = null;
        QueueRefs           block_queues = new QueueRefs();
        TDEVar              start = tdelist.signal("S", loc);
        TDEVar              finish = tdelist.signal("F", loc);
        TDEVar              tstart = tdelist.signal("TS", loc);
        TDEVar              tfinish = null;
        TDEVar              fstart = tdelist.signal("FS", loc);
        TDEVar              ffinish = tdelist.signal("FF", loc);
        ArrayList<TDEVar>   tstartl = new ArrayList<TDEVar>();
        ArrayList<TDEVar>   tfinishl = new ArrayList<TDEVar>();
        EXECR               status;
        boolean             true_direct_ok = true;
        boolean             true_only = false;
        
        // test
        Val     testval = test.getVal();
        if (!testval.isPrimitive())
            throw new ExEx("'sync when' statement test expression is not primitive", loc);
        if ((testval.getPrimType() != Ptype.LOG) && (testval.getPrimType() != Ptype.NULL))
            throw new ExEx("'sync when' statement test expression is not type log or null", loc);

        if (testval.getPrimType() == Ptype.NULL) {
            true_only = true;
            test_queues = testval.getQueues();     
        } else if (testval.getMode() == Mode.IMMEDIATE) {
            if (testval.getSingleLval(loc))
                true_only = true;
            else
                return(EXECR.NONE);
            test_queues = new QueueRefs();
        } else if (testval.getTDEVar().isConst()) {
            if (((Boolean)testval.getTDEVar().getConst()).booleanValue())
                true_only = true;
            else
                return(EXECR.NONE);
            test_queues = new QueueRefs();
        } else {
            testval.resolveClocks(null, Calloc.WHEN, loc);
            if (testval.collectClocks(loc) == null)
                throw new ExEx("'sync when' statement test expression has no clock domain", loc);
            testval.checkNullClocks ("'sync when' statement test expression contains input variables with no clock domain -", loc);
            test_queues = testval.getQueues();     
        }
        test_queues.unbufferedQueues(false, "unbuffered queue reads in ''sync when'' test expression", loc);

        if (checkedqueues == null)
            checkedqueues = test_queues;
        else {
            checkedqueues = checkedqueues.copy();
            checkedqueues.and_set(test_queues, loc);
        }

        // code block
        status = tblock.execute(esig, tstartl, tfinishl, block_queues, false, true, checkedqueues, null, null);
        if (status != EXECR.NONE)
            throw new ExEx("cannot have break, continue or return in 'sync when' statement", loc);
        if (tstartl.size() == 0)
            throw new ExEx("'sync when' code block has no target statements", loc);
        block_queues.unbufferedQueues(false, "unbuffered queue reads in ''sync when'' code block", loc);
        true_direct_ok = tblock.getDirect();
        tfinish = tfinishl.get(0);
        
        // get the queue read/write availability signals for queue reads
        // in both the test expression and the code block and
        // for queue writes in the code block
        block_queues.and_set(test_queues, loc);
        Val full_avail_val = block_queues.getSyncAvail(null, null, loc);
        TDEVar  full_avail = full_avail_val != null ? full_avail_val.getTDEVar() : null;
        
        TDEVar  testv = testval.getTDEVar();
        
        if (pri_out != null)
            tdelist.connect(pri_in, start);
        /*if (test_avail_val != null) {
            TDEVar  tdev;
            if (pri_out != null)
                tdev = tdelist.and(pri_out, test_avail, loc);
            else
                tdev = tdelist.and(start, test_avail, loc);
            test_queues.addPops(null, tdev, null, loc);
        }*/

        TDEVar  go_tdev;
        if (true_only && (full_avail == null) && (pri_out == null)) {
            startl.add(tstartl.get(0));
            finishl.add(tfinishl.get(0));
            return(EXECR.NONE);
        } else if (true_only) {
            if (pri_out != null) {
                if (full_avail == null)
                    go_tdev = pri_out;
                else
                    go_tdev = tdelist.and(pri_out, full_avail, loc);
            } else
                go_tdev = full_avail;
        } else if (full_avail == null)
            if (pri_out != null)
                go_tdev = tdelist.and(pri_out, testv, loc);
            else
                go_tdev = testval.getTDEVar();
        else
            if (pri_out != null)
                go_tdev = tdelist.and(pri_out, testv, full_avail, loc);
            else
                go_tdev = tdelist.and(testv, full_avail, loc);
        tdelist.connect(tstartl.get(0), tstart);
        test_queues.addPops(null, tstart, null, loc);
        
        // If -
        //  1. the code block is flagged direct assign
        //  2. the test expression has no queue reads
        //  4. the 'sync when' is at the top level
        // generate assignment for code block which is
        // enabled directly from the test signal with no execution stream!
        // No inline code is generated.
        if (true_direct_ok && block_queues.isEmpty() && toplevel) {
            tdelist.connect(go_tdev, tstart);
            return(EXECR.NONE);
        } else {
            TDE tde_when = new TDE(TDEType.WHEN, loc);
            tde_when.add2i(start);
            tde_when.add2i(go_tdev);
            tde_when.add2i(tfinish);
            tde_when.add2i(ffinish);
            tde_when.add2o(tstart);
            tde_when.add2o(fstart);
            tde_when.add2o(finish);
            tdelist.addTDE(tde_when);
            
            // One cycle delay for failure case not needed.
            //tdelist.del(ffinish, fstart, clock, esig);
            tdelist.del(ffinish, fstart, clock, esig);
        }
        
        queues.and_set(block_queues, loc);
        
        startl.add(start);
        finishl.add(finish);

        return(EXECR.NONE);
    }
}
