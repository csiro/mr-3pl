package threepl.nodes;

import static threepl.ThreePL.tdelist;

import java.util.ArrayList;

import threepl.ThreePL;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.QueueRefs;
import threepl.exec.Priority;
import threepl.exec.Ref;
import threepl.exec.SubFieldList;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.exec.WordSpec;
import threepl.exec.Var.IDtype;
import threepl.parser.Constant;
import threepl.parser.Token;

/**
 * Implements a target priority wait statement.
 */
public class WaitPriNode extends BlockNode implements Constant {
    private Node    pv;
    private Node    index;
    private boolean lock;
        
    /**
     * Construct an empty target priority wait block node.
     * Subnodes are added later by the parser.
     * @param   t is a token, from which the file source location is extracted
     * @param   v is a Node which must evaluate to a reference to a
     *          priority-mode variable
     * @param   lock if true indicates that the wait will hold priority until
     *          released, i.e. usually for more than one clock cycle
     * @param   i is a Node which must evaluate to an immediate constant
     */
    public WaitPriNode (Token t, Node v, boolean lock, Node i) {
        super(Btype.SYNC, t, null);
        pv = v;
        index = i;
        this.lock = lock;
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
        String  w = lock ? "waitprilock" : "waitpri";
        if (pri_out != null)
            throw new ExEx("'" + w + "' statement nested within 'waitpri' or 'waitprilock' statement", loc);

        // This check was intended to pick up enclosing sync blocks as they
        // set availok and place entries in checkedqueues. This is not
        // correct as when and branch statements (and probably others) also
        // may place entries in checkedqueues. Since an enclosing sync is
        // not really an error, just perhaps undesirable, this check has
        // been commented out, but remains for possible later reconsideration.
        //if (availok || (checkedqueues != null) && !checkedqueues.isEmpty())
        //    throw new ExEx("'" + w + "' statement nested within 'sync' or 'sync when' block", loc);

        Ref             pref = pv.getRef("waitpri - ");
        Var             pvar = pref.getVar();
        int             i = (int)index.getVal().getSingleIval(loc);
        if (!(pvar instanceof Priority))
            throw new ExEx("'" + w + "' statement - variable '" + pvar.getID(IDtype.SLITERAL) +
                            "' not priority mode", loc);
        SubFieldList    pvar_sfl = pref.getSubFields();
        if ((pvar_sfl != null) && !pvar_sfl.isEmpty())
            throw new ExEx("'" + w + "' statement - priority variable '" + pvar.getID(IDtype.SLITERAL) +
                            "' has subscipts", loc);
        if (i < 0)
            throw new ExEx("'" + w + "' statement - priority variable '" + pvar.getID(IDtype.SLITERAL) +
                            "' -ve priority", loc);
        if (i == 0)
            throw new ExEx("'" + w + "' statement - priority variable '" + pvar.getID(IDtype.SLITERAL) +
                            "' 0 priority (must be > 0)", loc);

        pri_in = tdelist.signal("WPIN", new WordSpec(Ptype.LOG, true, lock ? pvar : null), loc);
        
        Val             val_pri_in = new Val(pvar, Mode.PRIORITY, pri_in, loc);
        SubFieldList    sfl = new SubFieldList(i, loc);
        Val             out_val = pvar.getVal(null, sfl, loc);
        Ref             in_ref = pvar.getRef(null, sfl, false, loc);
        pri_out = out_val.getTDEVar();
        in_ref.assignTo(val_pri_in, loc);

        Node        n = subnodes.getNode(0);
        QueueRefs   q = new QueueRefs();
        if (n != null) {
            EXECR       status;
            status = n.execute(esig, startl, finishl, q, false, false, null, pri_in, pri_out);
            q.unbufferedQueues(false, "unbuffered queue read or write within '" + w + "' statement", loc);
            if (status != EXECR.NONE)
                throw new ExEx("cannot have break, continue or return in '" + w + "' statement", loc);
        }
        if ((n == null) || (startl.size() == 0)) {
            if (!lock)
                throw new ExEx("'waitpri' statement has no enclosed target statement", loc);
            // No enclosed target statement - make a 1-cycle nop().
            TDEVar  start_del = tdelist.signal("SD", loc);
            TDEVar  start = tdelist.signal("S", loc);
            TDEVar  finish = tdelist.signal("F", loc);
            TDEVar  v = tdelist.execp(null, null, true, null, false, start, pri_in, pri_out, esig, null, null, loc);
            tdelist.connect(start_del, v);
            tdelist.del(finish, start_del, ThreePL.getCurrentClock(), esig);
            startl.add(start);
            finishl.add(finish);
            
        } else {
            if (pri_in != null) {
                // determine if a waitprilock rather than a waitpri -
                // the var field in the WordSpec is set
                // to null if a waitpri and to the priority Var if
                // a waitprilock
                if (pri_in.getVar() != null)
                    ((Priority)pvar).addlockSignal(pri_out, loc);
            }
            queues.and_set(q, loc);
        }
        
        // If there is an exception signal and this is a waitprilock then add te exception
        // as an unlock signal.
        if (lock && (esig != null))
                ((Priority)pvar).addUnlockSignal(esig, loc);
        
        return(EXECR.NONE);
    }
}
