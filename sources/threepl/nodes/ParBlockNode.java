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
import threepl.exec.Scope;
import threepl.parser.Constant;
import threepl.parser.Token;

/**
 * Implements a target par block.
 */
public final class ParBlockNode extends BlockNode implements Constant, TDEConstants {
    
    /**
     * Construct an empty target par block node.
     * Subnodes are added later by the parser.
     * @param   t is a token, from which the file source location is extracted
     */
    public ParBlockNode (Token t) {
        super(Btype.PAR, t, null);
    }

    /**
     * Execute the target par block.
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
        ThreePL.pushScope(new Scope(Btype.PAR), null);

        ArrayList<TDEVar>   bstartl = new ArrayList<TDEVar>();
        ArrayList<TDEVar>   bfinishl = new ArrayList<TDEVar>();
        TDEVar              start_del = tdelist.signal("S", loc);
        boolean             have_stop = false;
        EXECR               status;
        int                 qreads = 0;
        int                 nqreads = 0;


        for (Node n: subnodes) {
            if (n == null)
                continue;
            QueueRefs p = new QueueRefs();
            status = n.execute(esig, bstartl, bfinishl, p, false, availok, checkedqueues, null, pri_out);
            if (p.isEmpty())
                nqreads++;
            else
                qreads++;
            queues.and_set(p, loc);
            if (status != EXECR.NONE)
                throw new ExEx("cannot have break, continue or return in par block", loc);
            if ((bstartl.size() != 0) &&
                (bstartl.get(bstartl.size()-1).equalOrLinked(TDEVar.GND)))
                have_stop = true;
            if ((pri_out != null) && !p.isEmpty())
                throw new ExEx("'par' statement nested within 'waitpri' statement has queue reads or writes", loc);
        }
        if (bstartl.size() == 0) {
            // No enclosed target statements - nothing to link up.
            ThreePL.popScope();
            return(EXECR.NONE);
        }
        
        if ((qreads != 0) && (nqreads != 0)) {
            rpt("\t" + loc.toString() + " - WARNING");
            rpt("\t'par' block at " + loc.toString() + " has mixture of statements with and without queue accesses\n");
        }

        TDE         tde = null;
        TDEVar      clk = ThreePL.getCurrentClock();
        TDEVar      start = tdelist.signal("S", loc);
        TDEVar      finish = tdelist.signal("F", loc);

        if (!have_stop) {
            tde = new TDE(TDEType.WAIT);
            tde.add2ic(clk);
            tde.add2i(esig);
        }

        // Connect start signal to all enclosed target statement start signals.
        // Connect all enclosed target statement finish signals to par wait.
        for (int i=0 ; i<bstartl.size() ; i++) {
            tdelist.connect(bstartl.get(i), start_del);
            if (!have_stop)
                tde.add2i(bfinishl.get(i));
        }
        
        TDEVar  v = tdelist.execp(null, null, true, null, false, start, pri_in, pri_out, esig, null, null, loc);
        tdelist.connect(start_del, v);

        if (!have_stop) {
            tde.add2o(finish);
            tdelist.addTDE(tde);
        }

        ThreePL.popScope();

        if (bstartl.size() > 0) {
            // Add start signal to start signal list.
            // Add parallel wait finish signal to finish signal list.
            startl.add(start);
            if (!have_stop)
                finishl.add(finish);
            else
                finishl.add(TDEVar.GND);
        }

        return(EXECR.NONE);
    }
}
