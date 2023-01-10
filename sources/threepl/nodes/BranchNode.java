package threepl.nodes;

import static threepl.ThreePL.rpt;
import static threepl.ThreePL.tdelist;

import java.util.ArrayList;
import java.util.HashSet;

import threepl.ThreePL;
import threepl.codegen.TDE;
import threepl.codegen.TDEConstants.TDEType;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.QueueRefs;
import threepl.exec.Val;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;
import threepl.parser.Token;

/**
 * This implements a branch statement. A branch statement is
 * a target statement similar in function to an immediate switch statement.
 *
 * This has a minimum of 3 subnodes. The first subnode is the subject of the
 * branch, the value used to determine which 'case' statement to jump to.
 * Following this are one or more pairs of nodes, one for each case.
 * The first subnode of each pair is an immediate expression node giving
 * the test value. The second subnode of the pair is a target statement to
 * execute when the test value matches the branch subject value.
 */
public class BranchNode extends Node implements Constant {
    protected   ArrayList<caseinfo> cases = new ArrayList<caseinfo>();
    private     BlockNode           current_block;
    private     BlockNode           default_block;
    
    private class caseinfo {
        public BlockNode    b;
        public Node         caseval;
        
        public caseinfo (Node n, Token t) {
            b = new BlockNode(Btype.CASE, t, null);
            caseval = n;
            current_block = b;
        }
    }
    
    public BranchNode (Node test, Token t) {
        super(t);
        addSubNode(test);
    }
    
    public boolean addCase (Node n, Token t) {
        if (n == null) {
            if (default_block != null)
                return(true);
            default_block = new BlockNode(Btype.CASE, t, null);
            current_block = default_block;
        } else
            cases.add(new caseinfo(n, t));
        return(false);
    }
    
    public void addStatement (Node n) {
        current_block.addSubNode(n);
    }
    
    /**
     * Execute a branch.
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
            throw new ExEx("'branch' statement nested within 'waitpri' statement", loc);
        if (clock == null)
            throw new ExEx("No current clock domain for 'branch' statement", loc);

        QueueRefs    checkedqueues_local = new QueueRefs();
        QueueRefs    test_queues = null;
        TDEVar      start = tdelist.signal("S", loc);
        TDEVar      start_del  = tdelist.signal("SD", loc);
        TDEVar      finish = tdelist.signal("F", loc);
        
        // branch test
        Val branch_val = subnodes.getNode(0).getVal();
        if (branch_val == null)
            throw new ExEx("'branch' test value is null", loc);
        if (branch_val.getMode() == Mode.IMMEDIATE)
            throw new ExEx("'branch' statement test expression is immediate", loc);
        if (!branch_val.isPrimitive())
            throw new ExEx("'branch' statement test expression is not primitive", loc);
        Ptype   ptype = branch_val.getPrimType();
        int     max;
        int     min;
        TDEVar  stest = branch_val.getTDEVar();
        if (stest.isConst())
            throw new ExEx("'branch' statement test expression is target constant", loc);

        if (ptype.isSigned()) {
            min = -(1 << (branch_val.getPrimWidth() - 1));
            max = (1 << (branch_val.getPrimWidth() - 1)) - 1;
        } else {
            min = 0;
            max = (1 << branch_val.getPrimWidth()) - 1;
        }
 
        branch_val.resolveClocks(null, Calloc.BRANCH, loc);
        branch_val.checkNullClocks ("'branch' statement test expression contains input variables with no clock domain -", loc);
        
        test_queues = branch_val.getQueues();
        if (checkedqueues != null) {
            checkedqueues_local.and_set(checkedqueues, loc);
            checkedqueues_local.addReadAVChecks(checkedqueues);
            checkedqueues_local.addWriteAVChecks(checkedqueues);
        }
        checkedqueues_local.and_set(test_queues, loc);
        checkedqueues_local.addReadAVChecks(test_queues);
        checkedqueues_local.addWriteAVChecks(test_queues);

        queues.and_set(test_queues, loc);

        HashSet<Long>   case_set = new HashSet<Long>();
        ArrayList<TDEVar>   case_exec_list = new ArrayList<TDEVar>();
        ArrayList<TDEVar>   case_start_list = new ArrayList<TDEVar>();
        ArrayList<TDEVar>   case_finish_list = new ArrayList<TDEVar>();
        EXECR           status;

        for (caseinfo ci : cases) {
            Node            n = ci.caseval;
            HashSet<Long>   hs = new HashSet<Long>();
            Long            l;

            if (n instanceof CompoundValNode) {
                CompoundValNode cvn = (CompoundValNode)n;
                ArrayList<Val>  al = cvn.getValList("'branch' case value list");
                if (al.size() == 0)
                    throw new ExEx("'branch' case value list is null", loc);
                for (Val casevalue: al) {
                    if (casevalue.getMode() != Mode.IMMEDIATE)
                        throw new ExEx("'branch' case value is not immediate", loc);
                    l = caseLongVal(casevalue, ptype, loc);
                    if (case_set.contains(l))
                        throw new ExEx("'branch' case value duplicated", loc);
                    if (l > max)
                        throw new ExEx("'branch' case value greater than maximum value for type (" +
                                        l + " > " + max + ")", loc);
                    if (l < min)
                        throw new ExEx("'branch' case value less than minimum value for type (" +
                                        l + " < " + min + ")", loc);
                    case_set.add(l);
                    hs.add(l);
                }
            } else {
                Val casevalue = n.getVal();
                if (casevalue == null)
                    throw new ExEx("'branch' case value is null", loc);
                if (casevalue.getMode() != Mode.IMMEDIATE)
                    throw new ExEx("'branch' case value is not immediate", loc);
                l = caseLongVal(casevalue, ptype, loc);
                if (case_set.contains(l))
                    throw new ExEx("'branch' case value duplicated", loc);
                if (l > max)
                    throw new ExEx("'branch' case value greater than maximum value for type (" +
                                    l + " > " + max + ")", loc);
                if (l < min)
                    throw new ExEx("'branch' case value less than minimum value for type (" +
                                    l + " < " + min + ")", loc);
                case_set.add(l);
                hs.add(l);
            }

            TDE                 and = new TDE(TDEType.AND);
            QueueRefs           squeues = new QueueRefs();
            ArrayList<TDEVar>   cstartl = new ArrayList<TDEVar>();
            ArrayList<TDEVar>   cfinishl = new ArrayList<TDEVar>();
            TDEVar              cstart = tdelist.signal("CS", loc);
            BlockNode           case_block = ci.b;

            status = case_block.execute(esig, cstartl, cfinishl, squeues, false, availok, checkedqueues_local, pri_in, pri_out);
            if (status != EXECR.NONE)
                throw new ExEx("cannot have 'break', 'continue' or 'return' in 'branch' statement", loc);
            tdelist.connect(cstartl.get(0), cstart);
            case_finish_list.add(cfinishl.get(0));

            // check for any queue reads in the test expression which are also
            // in one of the code bodies along with at least one other queue read
            // or write
            if (!availok)
                if (test_queues.readOverlap(squeues)) {
                    rpt("\t" + loc.toString() + " - WARNING");
                    rpt("\t'branch' statement body queue reads and writes set intersects test expression queue reads set\n");
            }
            
            TDE or = new TDE(TDEType.OR); // for single value OR optimised out later
            for (Long v : hs) {
                TDEVar  decodeout = tdelist.signal("DO", loc);
                tdelist.decode(v, stest, decodeout, loc);
                or.add2i(decodeout);
                case_exec_list.add(decodeout);
            }
            
            and.add2i(start_del);
            and.add2i(or.finish());
            and.add2o(cstart);
            tdelist.addTDE(and);
            case_start_list.add(cstart);            
            
            queues.and_set(squeues, loc);
        }

        // Handle default case.
        // If there is none, generate an empty default.

        TDEVar      def_start = tdelist.signal("DS", loc);
        TDE         def_or = new TDE(TDEType.OR);
        TDE         def_or_inv = new TDE(TDEType.INV);
        TDE         def_or_inv_and = new TDE(TDEType.AND);

        for (TDEVar tdev : case_exec_list)
            def_or.add2i(tdev);

        def_or_inv.add2i(def_or.finish());
        
        def_or_inv_and.add2i(start);
        def_or_inv_and.add2i(def_or_inv.finish());
        def_or_inv_and.add2o(def_start);
        tdelist.addTDE(def_or_inv_and);
        case_start_list.add(def_start);
        
        

        if (default_block == null) {
            TDEVar  dfinish  = tdelist.signal("TF", loc);
            tdelist.del(dfinish, def_start, clock, esig, test_queues);
            case_finish_list.add(dfinish);
        } else {
            QueueRefs            squeues = new QueueRefs();
            ArrayList<TDEVar>   dstartl = new ArrayList<TDEVar>();
            ArrayList<TDEVar>   dfinishl = new ArrayList<TDEVar>();

            status = default_block.execute(esig, dstartl, dfinishl, squeues, false, availok, checkedqueues, pri_in, pri_out);
            if (status != EXECR.NONE)
                throw new ExEx("cannot have 'break', 'continue' or 'return' in 'branch' statement", loc);
            tdelist.connect(dstartl.get(0), def_start);
            case_finish_list.add(dfinishl.get(0));

            // check for any queue reads in the test expression which are also
            // in one of the code bodies along with at least one other queue read
            // or write
            if (!availok)
                if (test_queues.readOverlap(squeues)) {
                    rpt("\t" + loc.toString() + " - WARNING");
                    rpt("\t'branch' statement body queue reads and writes set intersects test expression queue reads set");
            }

            //tdelist.connect(def_start, dstartl.get(0));
            
            queues.and_set(squeues, loc);
        }

        // Finish signal - OR of all case block finish signals including default.
        TDE         sf_or = new TDE(TDEType.OR);
        for (TDEVar tdev : case_finish_list)
            sf_or.add2i(tdev);
        sf_or.add2o(finish);
        tdelist.addTDE(sf_or);                   
        
        // generate the queue pops
        TDEVar  rpending = tdelist.signal("P", loc);
        test_queues.addPops(null, start_del, rpending, loc);

        // generate queue and priority wait where necessary
        tdelist.connect (
                    start_del,
                    tdelist.execp(test_queues, checkedqueues, availok, null, false, start, pri_in, pri_out, esig, null, rpending, loc)
        );

        startl.add(start);
        finishl.add(finish);
        
        tdelist.newBranch(start_del, case_start_list, sf_or, finish);

        return(EXECR.NONE);
    }

    static Long caseLongVal(Val casevalue, Ptype ptype, SrcLoc loc) {
        long    lval;
        if (casevalue.getMode() != Mode.IMMEDIATE)
            throw new ExEx("'branch' statement case value is not immediate");
        if (!casevalue.isPrimitive())
            throw new ExEx("'branch' statement case value is not primitive");
        switch (casevalue.getValPType(0)) {
        case BITS:
        case UINT:
            lval = casevalue.getSingleIval(loc);
            switch (ptype) {
            case BITS:
            case UINT:
            case INT:
                break;
            default:
                throw new ExEx("branch and case types do not match", loc);
            }
            return(Long.valueOf(lval));
        case INT:
            lval = casevalue.getSingleIval(loc);
            switch (ptype) {
            case BITS:
            case UINT:
                if (lval < 0)
                    throw new ExEx("branch has -ve case for type BITS or UINT", loc);
                break;
            case INT:
                break;
            default:
                throw new ExEx("branch and case types do not match", loc);
            }
            return(Long.valueOf(lval));
        case ENUM:
            if (ptype != Ptype.ENUM)
                throw new ExEx("switch and case types do not match", loc);
            return(Long.valueOf(casevalue.getSingleEval(loc).val));
        default:
            throw new ExEx("case value not an allowed type", loc);
        }
    }
}
