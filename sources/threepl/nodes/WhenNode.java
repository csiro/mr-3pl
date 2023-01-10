package threepl.nodes;

import static threepl.ThreePL.rpt;
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
 * Implements a target when and when else.
 */
public final class WhenNode extends Node implements Constant, TDEConstants {

    /**
     * Construct a target when (..) or when (..) else node.
     * The when or else code blocks may be null
     * @param   test is the test expression
     * @param   tblock is the code block to be executed if the test
     *          value is true, or is null
     * @param   fblock is the code block to be executed if the test
     *          value is false, or is null
     * @param   t is a token, from which the file source location is extracted
     */
    public WhenNode (
        Node test,
        Node tblock,
        Node fblock,
        Token t
    ) {
        super(t);
        addSubNode(test);
        addSubNode(tblock);
        addSubNode(fblock);
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

        Node                test   = subnodes.getNode(0);
        Node                tblock = subnodes.getNode(1);
        Node                fblock = subnodes.getNode(2);
        QueueRefs            test_queues = null;
        QueueRefs            t_queues = new QueueRefs();
        QueueRefs            f_queues = new QueueRefs();
        QueueRefs            checkedqueues_local = new QueueRefs();
        TDEVar              start = tdelist.signal("S", loc);
        TDEVar              finish = tdelist.signal("F", loc);
        TDEVar              tstart = tdelist.signal("TS", loc);
        TDEVar              tfinish = null;
        TDEVar              fstart = tdelist.signal("FS", loc);
        TDEVar              ffinish = null;
        ArrayList<TDEVar>   tstartl = new ArrayList<TDEVar>();
        ArrayList<TDEVar>   tfinishl = new ArrayList<TDEVar>();
        ArrayList<TDEVar>   fstartl = new ArrayList<TDEVar>();
        ArrayList<TDEVar>   ffinishl = new ArrayList<TDEVar>();
        TDEVar              start_del  = tdelist.signal("SD", loc);
        EXECR               status;
        boolean             true_direct_ok = true;
        boolean             false_direct_ok = true;
        boolean             true_only = false;
        boolean             false_only = false;
        
        // test
        Val     testval = test.getVal();
        if (pri_out != null)
            throw new ExEx("'when' statement nested within 'waitpri' statement", loc);
        if (!testval.isPrimitive())
            throw new ExEx("'when' statement test expression is not primitive", loc);
        if ((testval.getPrimType() != Ptype.LOG) && (testval.getPrimType() != Ptype.NULL))
            throw new ExEx("'when' statement test expression is not type log or null", loc);
        testval.checkNullClocks ("'when' statement test expression contains input variables with no clock domain -", loc);

        if (testval.getPrimType() == Ptype.NULL) {
            true_only = true;
            test_queues = testval.getQueues();
            testval.resolveClocks(null, Calloc.WHEN, loc);
        } else if (testval.getMode() == Mode.IMMEDIATE) {
            if (testval.getSingleLval(loc))
                true_only = true;
            else
                false_only = true;
            test_queues = new QueueRefs();
        } else if (testval.getTDEVar().isConst() && testval.getQueues().getReads().isEmpty()) {
            if (((Boolean)testval.getTDEVar().getConst()).booleanValue())
                true_only = true;
            else
                false_only = true;
            test_queues = new QueueRefs();
        } else {
            testval.resolveClocks(null, Calloc.WHEN, loc);
            if (testval.collectClocks(loc) == null)
                throw new ExEx("'when' statement test expression has no clock domain", loc);
            test_queues = testval.getQueues();
        }
        
        if (true_only && (fblock != null))
            throw new ExEx("false block can never be executed", loc);
        if (false_only && (tblock != null))
            throw new ExEx("true block can never be executed", loc);

        if (checkedqueues != null) {
            checkedqueues_local.and_set(checkedqueues, loc);
            checkedqueues_local.addReadAVChecks(checkedqueues);
            checkedqueues_local.addWriteAVChecks(checkedqueues);
        }
        checkedqueues_local.and_set(test_queues, loc);
        checkedqueues_local.addReadAVChecks(test_queues);
        checkedqueues_local.addWriteAVChecks(test_queues);

        // true block
        if ((tblock != null) && !false_only) {
            status = tblock.execute(esig, tstartl, tfinishl, t_queues, false, availok, checkedqueues_local, pri_in, pri_out);
            if (status != EXECR.NONE)
                throw new ExEx("cannot have break, continue or return in 'when' statement", loc);
            if (tstartl.size() > 1)
                throw new ExEx("'when' true block has multiple target statements", loc);
            true_direct_ok = tblock.getDirect();
            if (true_only && (tstartl.size() > 0) && test_queues.isEmpty()) {
                startl.add(tstartl.get(0));
                finishl.add(tfinishl.get(0));
                return(EXECR.NONE);
            }
        }
        if (tstartl.size() > 0) {
            tdelist.connect(tstartl.get(0), tstart);
            tfinish = tfinishl.get(0);
        } else {
            tfinish  = tdelist.signal("TF", loc);
            tdelist.del(tfinish, tstart, clock, esig);
        }
        
        // false block
        if ((fblock != null) && !true_only) {
            status = fblock.execute(esig, fstartl, ffinishl, f_queues, false, availok, checkedqueues_local, pri_in, pri_out);
            if (status != EXECR.NONE)
                throw new ExEx("cannot have break, continue or return in 'when' statement", loc);
            if (fstartl.size() > 1)
                throw new ExEx("'when' false block has multiple target statements", loc);
            false_direct_ok = fblock.getDirect();
            if (false_only && (fstartl.size() > 0) && test_queues.isEmpty()) {
                startl.add(fstartl.get(0));
                finishl.add(ffinishl.get(0));
                return(EXECR.NONE);
            }
        }
        if (fstartl.size() > 0) {
            tdelist.connect(fstartl.get(0), fstart);
            ffinish = ffinishl.get(0);
        } else {
            ffinish  = tdelist.signal("FF", loc);
            tdelist.del(ffinish, fstart, clock, esig);
        }
        
        // If -
        //  1. the true and false blocks are empty or flagged direct assign
        //  2. the test expression has no queue reads
        //  4. the WHEN is at the top level
        // generate assignments for nonempty true and false blocks which are
        // enabled directly from the test signal with no execution stream!
        // No inline code is generated.
        if (true_direct_ok && false_direct_ok && test_queues.isEmpty() && toplevel) {
            if (tblock != null)
                tdelist.connect(tstart, testval.getTDEVar());
            if (fblock != null) {
                TDE invert = new TDE(TDEType.INV, loc);
                invert.add2i(testval.getTDEVar());
                invert.add2o(fstart);
                tdelist.addTDE(invert);
            }
            return(EXECR.NONE);
        } /* else {
            // Not direct execution, so make sure test expression has a clock domain
            if (testval.collectOutputClocks(loc) == null)
                throw new ExEx("when statement test expression has no clock domain", loc);
        } ALREADY CHECKED PREVIOUSLY! */
                
        // check for any queue reads in the test expression which are also
        // in one of the code bodies along with at least one other queue read
        // or write
        if (!availok) {
            if (test_queues.readOverlap(t_queues)) {
                rpt("\t" + loc.toString() + " - WARNING");
                rpt("\t'when' test expression queue reads set intersects true body queue reads and writes set");
            }
            if (test_queues.readOverlap(f_queues)) {
                rpt("\t" + loc.toString() + " - WARNING");
                rpt("\t'when' test expression queue reads set intersects false body queue reads and writes set");
            }
        }

        TDEVar  rpending = tdelist.signal("P", loc);
        
        // generate the queue pops
        test_queues.addPops(null, start_del, rpending, loc);

        // generate queue and priority waits where necessary
        tdelist.connect (
                    start_del,
                    tdelist.execp(test_queues, checkedqueues, availok, null, false, start, null, null, esig, null, rpending, loc)
        );
        
        if (true_only) {
            // true block execution only
            tdelist.connect(tstart, start_del);
            tdelist.connect(finish, tfinishl.get(0));
       } else if (false_only) {
            // false block execution only
           tdelist.connect(fstart, start_del);
           tdelist.connect(finish, ffinishl.get(0));
       } else {
            // conditional execution
            TDE tde_when = new TDE(TDEType.WHEN, loc);
            tde_when.add2i(start_del);
            tde_when.add2i(testval.getTDEVar());
            tde_when.add2i(tfinish);
            tde_when.add2i(ffinish);
            tde_when.add2o(tstart);
            tde_when.add2o(fstart);
            tde_when.add2o(finish);
            tdelist.addTDE(tde_when);
        }
        
        queues.and_set(test_queues, loc);
        queues.and_set(t_queues, loc);
        queues.and_set(f_queues, loc);
        
        startl.add(start);
        finishl.add(finish);

        return(EXECR.NONE);
    }
}
