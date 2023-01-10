package threepl.nodes;

import static threepl.ThreePL.tdelist;

import java.util.ArrayList;

import threepl.ThreePL;
import threepl.codegen.TDE;
import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.QueueRefs;
import threepl.exec.Scope;
import threepl.parser.Constant;
import threepl.parser.Token;

/**
 * Implements a target sync block.
 */
public class SyncBlockNode extends BlockNode implements Constant, TDEConstants {
    
    /**
     * Construct an empty target sync block node.
     * Subnodes are added later by the parser.
     * @param   t is a token, from which the file source location is extracted
     */
    public SyncBlockNode (Token t) {
        super(Btype.SYNC, t, null);
    }

    /**
     * Execute the target sync block.
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
        TDEVar  clk = ThreePL.getCurrentClock();
        if (clk == null)
            throw new ExEx("No current clock domain for 'sync'", loc);

        ThreePL.pushScope(new Scope(Btype.SYNC), null);
        
        ArrayList<TDEVar>   bstartl = new ArrayList<TDEVar>();
        ArrayList<TDEVar>   bfinishl = new ArrayList<TDEVar>();
        QueueRefs            squeues = new QueueRefs();
        boolean             have_stop = false;
        EXECR               status;

        for (Node n: subnodes) {
            if (n == null)
                continue;
            QueueRefs qs = new QueueRefs();
            status = n.execute(esig, bstartl, bfinishl, qs, false, true, null, null, pri_out);
            qs.unbufferedQueues(false, "unbuffered queue in sync statement", loc);
            squeues.and_set(qs, loc);
            if (status != EXECR.NONE)
                throw new ExEx("cannot have break, continue or return in sync block", loc);
            if ((bstartl.size() != 0) &&
                (bstartl.get(bstartl.size()-1).equalOrLinked(TDEVar.GND)))
                have_stop = true;   // have a stop() in the sync block
        }
        if (bstartl.size() == 0) {
            // No enclosed target statements - nothing to link up.
            ThreePL.popScope();
            return(EXECR.NONE);
        }
        

        TDE     tde = null;
        TDEVar  start = tdelist.signal("S", loc);
        TDEVar  start_del = tdelist.signal("S", loc);
        TDEVar  finish = tdelist.signal("F", loc);

        if (!have_stop) {
            tde = new TDE(TDEType.WAIT);
            tde.add2ic(clk);
            tde.add2i(esig);
        }

        // Connect start signal to all enclosed target statement start signals.
        // Coonect all enclosed target statement finish signals to par wait.
        for (int i=0 ; i<bstartl.size() ; i++) {
            if (bfinishl.size() > bstartl.size())
                throw new ExEx("clock() called inside sync block", loc);
            tdelist.connect(bstartl.get(i), start_del);
            if (!have_stop)
                tde.add2i(bfinishl.get(i));
        }

        if (!have_stop) {
            tde.add2o(finish);
            tdelist.addTDE(tde);
        }

        ThreePL.popScope();

        if (availok/* && (pri_in == null)*/) // EXTRA TERM SEEMS TO BE AN ERROR!
            // already inside a sync, so do not need another sync!
            // just connect the start and conditional start together
            tdelist.connect(start_del, start);
        else {
            // generate a conditional (queue and priority wait) start
            tdelist.connect(
                    start_del,
                    tdelist.execp(  squeues,
                                    checkedqueues,
                                    false,
                                    null,
                                    true,
                                    start,
                                    pri_in,
                                    pri_out,
                                    esig,
                                    null,
                                    null,
                                    loc )
            );
        }

        if (bstartl.size() > 0) {
            // Add start signal to start signal list.
            // Add parallel wait finish signal to finish signal list.
            startl.add(start);
            if (!have_stop)
                finishl.add(finish);
            else
                finishl.add(TDEVar.GND);
        }
        
        queues.and_set(squeues, loc);

        return(EXECR.NONE);
    }
}
