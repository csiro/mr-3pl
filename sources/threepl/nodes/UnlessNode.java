package threepl.nodes;

import static threepl.ThreePL.tdelist;
import static threepl.codegen.TDEVar.*;

import java.util.ArrayList;

import threepl.ThreePL;
import threepl.codegen.TDE;
import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.QueueRefs;
import threepl.exec.Val;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;
import threepl.parser.Token;

/**
 * Implements an exception construct on a target when, while, do while,
 * seq, par or sync.
 * The subnodes are -
 * <UL>
 * <LI> the exception expression node
 * <LI> an exception block node or null
 * <LI> the subject control structure to which the exception applies
 *</UL>
 * The first two subnodes are appended by the constructor. The last subnode
 * is appended by the associated control structure production.
 */
public final class UnlessNode extends Node implements Constant, TDEConstants {

    /**
     * Construct an exception node for a when, targ while, targ do while, seq,
     * par or sync node.
     * The exception block may be null
     * @param   excep is the exception expression, or is null
     * @param   eblock is the exception block to be executed if exception
     *          value is true, or is null
     * @param   t is a token, from which the file source location is extracted
     */
    public UnlessNode (
        Node excep,
        Node eblock,
        Token t
    ) {
        super(t);
        addSubNode(excep);
        addSubNode(eblock);
    }
    
    /**
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
        if (pri_out != null)
            throw new ExEx("'unless' statement nested within 'waitpri' statement", loc);
        if (clock == null)
            throw new ExEx("No current clock domain for unless construct", loc);
        
        // At the moment nested UNLESS blocks are not allowed.
        if (esig != null)
            throw new ExEx("nested \"unless\" constructs not allowed", loc);

        Node        excep  = subnodes.getNode(0);
        Node        eblock = subnodes.getNode(1);
        Node        sblock = subnodes.getNode(2);
        Val         excep_val = null;
        QueueRefs   e_queues = null;
        TDEVar      etdev = null;
        EXECR       status = EXECR.NONE;
        ArrayList<TDEVar>   bstartl = new ArrayList<TDEVar>();
        ArrayList<TDEVar>   bfinishl = new ArrayList<TDEVar>();
        
        if (excep == null)
            throw new ExEx("exception expression missing", loc);
        excep_val = excep.getVal();
        if (excep_val == null)
            throw new ExEx("exception expression missing", loc);
        e_queues = excep_val.getQueues();
        if (excep_val.getMode() == Mode.IMMEDIATE)
            throw new ExEx("exception expression is immediate", loc);
        if (!excep_val.isPrimitive())
            throw new ExEx("exception expression is not primitive", loc);
        if (excep_val.getPrimType() != Ptype.LOG)
            throw new ExEx("exception expression is not type log", loc);

        etdev = excep_val.getTDEVar();
        if (etdev.isConst())
            throw new ExEx("exception expression is constant", loc);

        TDEVar              estart = null;
        TDEVar              efinish = null;
        ArrayList<TDEVar>   estartl = null;
        ArrayList<TDEVar>   efinishl = null;

        excep_val.resolveClocks(null, Calloc.ASSIGN, loc);
        if (excep_val.collectOutputClocks(loc) == null)
            throw new ExEx("exception expression has no clock domain", loc);

        // execute the exception block
        if (eblock != null) {
            estartl = new ArrayList<TDEVar>();
            efinishl = new ArrayList<TDEVar>();
            status = eblock.execute(null, estartl, efinishl, new QueueRefs(), false, false, e_queues, null, null);
            if (status != EXECR.NONE)
                throw new ExEx("cannot have break, continue or return in exception block", loc);
            if (estartl.size() > 1)
                throw new ExEx("exception block has multiple target statements", loc);
            if (estartl.size() == 1) {
                estart = estartl.get(0);
                efinish = efinishl.get(0);
            }
        }

        // execute the subject block
        TDEVar  resetsig = tdelist.signal("RESET", loc);
        /*
         * At the moment nested UNLESS blocks are not allowed.
         * The hooks (using rs) will be left there in case it is later implemented
         * properly.
        TDEVar  rs = (esig == null) ? resetsig : tdelist.or(esig, resetsig, loc);
        */
        TDEVar  rs = resetsig;
        status = sblock.execute(rs, bstartl, bfinishl, queues, toplevel, false, null, null, null);
        if (bstartl.size() == 0)
            return(status); // no target statements in block
        TDEVar  bstart = bstartl.get(0);
        TDEVar  bfinish = bfinishl.get(0);
        
        // block execution flip-flop
        TDEVar  executing = tdelist.signal("EX", loc);
        TDEVar  exec_reset = tdelist.or(bfinish, rs, loc);
        tdelist.fdrse(executing, GND, clock, GND, exec_reset, bstart, "R", loc);

        TDEVar  startsig = ThreePL.getCurrentClockVar().getStartSig(loc);
        TDEVar  startsigdel = tdelist.signal("SSD", loc);
        TDEVar  exit = tdelist.signal("RS", loc);
        TDEVar  s1 = tdelist.signal("S", loc);
        TDEVar  s2 = tdelist.signal("S", loc);
        TDE     tde_iloop = new TDE(TDEType.ILOOP);
        
        tdelist.del(startsigdel, startsig, clock, null);

        tde_iloop.add2i(startsigdel);   // start signal
        tde_iloop.add2i(s2);            // block finish signal
        tde_iloop.add2o(s1);            // block start signal

        // wait for exception expression to go true
        TDEVar  s3 = tdelist.signal("BS", loc);
        excep_true(s1, excep_val, executing, resetsig, s3, exit, s2, null, clock, loc);
        
        if (estart != null)
            tdelist.connect(estart, s3);    // there is an exception block
        else
            efinish = s3;                   // there is no exception block

        // if there are no queue reads in the exception expression -
        //      wait for the exception expression to go false
        // else
        //      connect exit to efinish
        if (excep_val.getQueues().isEmpty())
            excep_false(efinish, excep_val, executing, exit, null, clock, loc);
        else
            tdelist.connect(exit, efinish);

        tdelist.addTDE(tde_iloop);

        startl.add(bstart);
        finishl.add(tdelist.or(bfinish, exit, loc));

        return(status);
    }
    
    // Wait for the exception expression to be true before resetting the
    // logic and entering the optional exception block
    private static void excep_true (
        TDEVar  start,
        Val     excep_val,
        TDEVar  executing,
        TDEVar  reset_out,
        TDEVar  bstart,
        TDEVar  bfinish,
        TDEVar  finish,
        TDEVar  reset_in,
        TDEVar  clock,
        SrcLoc  loc
    ) {
        // WHEN element to execute the exception
        TDEVar      start_del = tdelist.signal("SD", loc);
        TDEVar      test = tdelist.and(excep_val.getTDEVar(), executing, loc);
        QueueRefs    e_queues = excep_val.getQueues();
        TDE         tde_when = new TDE(TDEType.WHEN);
        TDEVar      fs = tdelist.signal("FS", loc);
        TDEVar      ff = tdelist.signal("FF", loc);
        TDEVar      rpending = tdelist.signal("RP", loc);
        
        // exception expression queue wait where necessary
        tdelist.connect(start_del, tdelist.execp(e_queues, null, false, null, false, start, null, null, reset_in, null, rpending, loc));

        // generate the exception expression queue acknowledges, if any
        e_queues.addPops(null, start_del, rpending, loc);

        tde_when.add2i(start_del);  // start signal
        tde_when.add2i(test);       // test signal
        tde_when.add2i(bfinish);    // true block finish signal
        tde_when.add2i(ff);         // false block finish signal
        tde_when.add2o(reset_out);  // true block start signal
        tde_when.add2o(fs);         // false block start signal
        tde_when.add2o(finish);     // WHEN finish signal
        tdelist.del(bstart, reset_out, clock, reset_in, e_queues);
        tdelist.del(ff, fs, clock, reset_in);
        tdelist.addTDE(tde_when);
    }
    
    // Called only if there are no queue reads in the exception expression.
    // Wait for the exception expression to be false before finally
    // exiting the block.
    private static void excep_false (
        TDEVar  start,
        Val     excep_val,
        TDEVar  executing,
        TDEVar  finish,
        TDEVar  reset_in,
        TDEVar  clock,
        SrcLoc  loc
    ) {
        TDEVar  ts = tdelist.signal("TS", loc);
        TDEVar  contin = tdelist.signal("S", loc);
        TDE     tde_when = new TDE(TDEType.WHEN);
        tde_when.add2i(tdelist.or(start, contin, loc)); // start signal
        tde_when.add2i(excep_val.getTDEVar());          // test signal
        tde_when.add2i(null);                           // true block finish signal
        tde_when.add2i(null);                           // false block finish signal
        tde_when.add2o(ts);                             // true block start signal
        tde_when.add2o(finish);                         // false block start signal
        tde_when.add2o(null);                           // WHEN finish signal
        tdelist.del(contin, ts, clock, reset_in);
        tdelist.addTDE(tde_when);
    }
}
