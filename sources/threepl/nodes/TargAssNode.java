package threepl.nodes;

import static threepl.ThreePL.postProcessing;
import static threepl.ThreePL.tdelist;
import static threepl.nodes.ExprNode.evaluate;

import java.util.ArrayList;
import java.util.Iterator;

import threepl.ThreePL;
import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Queue;
import threepl.exec.QueueRefs;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.exec.Var.IDtype;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;
import threepl.parser.Token;

/**
 * Target assignment -
 * <ul>
 * <li> <b>{@code :=}</b> for a value, priority or clock LHS
 * <li> <b>{@code|=}</b> for a selectvalue LHS
 * <li> <b>{@code<-}</b> for a static LHS
 * <li> <b>{@code<<-}</b> for a queue LHS
 * </ul>
 */
public final class TargAssNode extends Node implements Constant, TDEConstants {
    private AST     asstype;
    private boolean direct;
    private boolean incrdecr;   // is for a a ++ or -- statement

    /**
     * Construct a target variable assignment node.
     * @param   n1 is the LHS <b>VarNode</b>
     * @param   n2 is the RHS expression node
     * @param   t is the operator token from which the assignment type
     *          is determined
     */
    public TargAssNode (Node n1, Node n2, Token t) {
        super(n1.getSrcLoc());
        addSubNode(n1);
        addSubNode(n2);
        if (t.image.equals(":="))
            asstype = AST.VALPRI;
        else if (t.image.equals("|-"))
            asstype = AST.SELVAL;
        else if (t.image.equals("<-"))
            asstype = AST.STATICASS;
        else if (t.image.equals("+<-"))
            asstype = AST.STATICADDASS;
        else if (t.image.equals("-<-"))
            asstype = AST.STATICSUBASS;
        else if (t.image.equals("*<-"))
            asstype = AST.STATICMULASS;
        else // must be <<-
            asstype = AST.QUEUE;
    }

    /**
     * Construct a target variable assignment node for a
     * ++ or -- statement.
     * @param   n1 is the LHS <b>VarNode</b>
     * @param   n2 is the RHS expression node
     * @param   ast is the assignment type AST.STATICADDASS or AST.STATICSUBASS
     */
    public TargAssNode (Node n1, Node n2, AST ast) {
        super(n1.getSrcLoc());
        addSubNode(n1);
        addSubNode(n2);
        asstype = ast;
        incrdecr = true;
    }

    /**
     * Construct a static variable assignment node during execution.
     * @param   n1 is the LHS <b>VarNode</b>
     * @param   n2 is the RHS expression node
     */
    public TargAssNode (Node n1, Node n2) {
        super(n1.getSrcLoc());
        addSubNode(n1);
        addSubNode(n2);
        asstype = AST.STATICASS;
    }
    
    /**
     * Get the direct execution flag
     * @return  the direct execution flag
     */
    public boolean getDirect () { return(direct); }

    /**
     * Execute the target assignment statement.
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
        if (postProcessing)
            throw new ExEx("Target variable assignment during post-processing");
        
        TDEVar  clock = ThreePL.getCurrentClock();
        TDEVar  start = tdelist.signal("S", loc);
        TDEVar  start_del = tdelist.signal("S", loc);
        TDEVar  finish = tdelist.signal("F", loc);
        Val     newval;
        AST     new_asstype = asstype;

        // LHS variable
        Node    ln = subnodes.getNode(0);
        SrcLoc  lvloc = ln.getSrcLoc();
        String  lvid = null;
        Var     lvar = null;
        Ref     lref = null;
        Mode    lmode;
        boolean lnull = false;  // LHS is null
        boolean lnullt = false; // LHS is null type
        if (ln instanceof NullNode) {
            lnull = true;
            lmode = Mode.QUEUE;
        } else {
            lref = ln.getRef("target assignment LHS");
            lmode = lref.getMode();
            lvar = lref.getVar();
            lvid = lvar.getID(IDtype.LITERAL);
            /*
             * Restriction removed!
            if (
                ln.isInputPar() &&
                (
                    (lvar.getMode() == Mode.QUEUE) || ln.isMatched() &&
                    !(
                        (lvar.getMode() == Mode.IMMEDIATE) ||
                        (lvar.getMode() == Mode.VALUE)
                    )
                )
            )
                // An input parameter can be assigned if any of -
                //  - is not matched by an argument and is not queue mode
                //  - is immediate mode
                //  - is value mode
                throw new ExEx("assignment to matched target input parameter '" + lvid + "'", ln);
            */
        }
        
        if (!lnull && (lref.getPrimType() == Ptype.NULL))
            lnullt = true;
        
        if (lmode == Mode.IMMEDIATE)
            throw new ExEx("target assignment to immediate variable", loc);

        // RHS value
        Node    n = subnodes.getNode(1);
        Var     rvar = null;
        Val     rval = null;
        VarNode rvn = null;
        Mode    rmode = null;
        boolean rnull = false;  // RHS is null
        boolean rnullt = false; // RHS is null type
        boolean valval = false; // LHS and RHS are value variables
        if (n instanceof NullNode) {
            rnull = true;
        } else if ((n instanceof VarNode) && (lmode == Mode.VALUE)) {
            rvn = (VarNode)n;
            rvar = rvn.getVar();
            /*
             * value := value treated as special case
             *
             */
            if ((rvar != null) && (rvar.getMode() == Mode.VALUE)) {
                // value := value
                valval = true;
                rmode = Mode.VALUE;
            } else if ((rvar != null) && (rvar.getMode() == Mode.CLOCK)) {
                if (asstype != AST.VALPRI)
                    throw new ExEx("RHS clock identifier can only be used with := assignment", loc);
                // value := clock
                // Special case - handle this here then return.
                if (rvn.getListSubNode(0).size() != 0)
                    throw new ExEx("RHS clock identifier cannot have fields or subscripts", loc);
                rval = rvar.getVal(n.getRef(null), loc);
                lref.assignTo(rval, lvloc);
                return(EXECR.NONE);
            }
        }
        
        if (!rnull/* && !valval*/) {
            if (n instanceof CompoundValNode)
                rval = ((CompoundValNode)n).getVal(lref.getType());
            else {
                rval = n.getVal();
                if (rval.getPrimType() == Ptype.NULL)
                    rnullt = true;
                else
                    rmode = rval.getMode();
            }
        }
        
        if (rnullt && (lref != null)) {
            if (lref.getMode() == Mode.CLOCK)
                throw new ExEx("null type assigned to clock variable", loc);
            else if (!lnullt)
                throw new ExEx("queue of null type assigned to LHS which is not null", loc);
        }
        if (lnull && (rval == null))
            throw new ExEx("null assigned null not allowed", loc);
        if (!valval && (rval == null) && (lmode != Mode.QUEUE))
            throw new ExEx("null can only be assigned to a pointer type or a queue mode variable of type \"null\"", loc);
        if (!valval && (rval == null) && (lmode == Mode.QUEUE) && (lvar.getType().getPrimType() != Ptype.NULL))
            throw new ExEx("null cannot be assigned to a queue mode variable whose type is not \"null\"", loc);

        // Check LHS type against RHS type except -
        //  * LHS is "null"
        //  * RHS is "null"
        //  * value to value assignment - checked in Var.valValAssignTo()
        if ((lvar != null) && (lvar.getType().getPrimType() == Ptype.NULL)) {
            if ((rval != null) && !rnullt)
                throw new ExEx("a queue mode variable of type null can only be assigned null", loc);
        } else if (!lnull && (rval != null) && !lref.hasTargetSubs())
            lref.checkMatch(rval, false, "assignment ", loc);

        if (asstype == AST.VALPRI) {
            //
            // VALUE, PRIORITY, CLOCK or OUTPUT mode assignment
            //
            if ((lmode != Mode.VALUE) &&
                (lmode != Mode.PRIORITY) &&
                (lmode != Mode.CLOCK) &&
                (lmode != Mode.OUTPUT))
                throw new ExEx(":= LHS variable '" +
                                lvid + "' not VALUE, PRIORITY, CLOCK or OUTPUT mode", ln);
            if (lref.hasTargetSubs())
                throw new ExEx("value assignment LHS, '" +
                                lvid + "' has variable subscript", ln);
            
            // do assignment
            /*if (valval)
                // value to value assignment - special case
                lref.valValAssignTo(rvar, rvn.getListSubNode(0), lvloc);
            else*/ if (lmode == Mode.VALUE)
                // value mode assignment
                lref.assignTo(rval, lvloc);
            else if (lmode == Mode.OUTPUT) {
                // unconditional output mode assignment
                lref.assignTo(rval, null, lvloc);
                rval.setUsed();
            } else if (lmode == Mode.PRIORITY) {
                // priority mode assignment
                rval.resolveClocks(null, Calloc.VALUE, loc);
                lref.resolveClocks(null, Calloc.ASSIGN, loc);
                lref.assignTo(rval, lvloc);
            } else {
                // clock mode assignment
                if (rmode != Mode.CLOCK)
                    throw new ExEx(":= LHS clock variable '" +
                                lvid + "'RHS not clock mode ", ln);
                rvar = rval.getVar();
                double  rfreq = rvar.getClkFreq(loc);
                //double  rperiod = rvar.getClkPeriod();
                double  lfreq = lvar.getClkFreq(loc);
                //double  lperiod = lvar.getClkPeriod(loc);
                if ((rfreq == 0.0) && (lfreq != 0.0)) {
                    rvar.setClkFreq(lfreq, loc);
                    //rvar.setClkPeriod(lperiod, loc);
                }
                lvar.setIndirect(rval, loc);
            }
        } else {
            //
            // SELECTVALUE, STATIC, QUEUE or conditional OUTPUT mode assignment
            //
            TDEVar  rpending = tdelist.signal("RP", loc);
            TDEVar  wpending = tdelist.signal("WP", loc);
            if (clock == null)
                throw new ExEx("No current clock domain for target assignment", loc);

            if (((asstype == AST.STATICASS) ||
                 (asstype == AST.STATICADDASS) ||
                 (asstype == AST.STATICSUBASS) ||
                 (asstype == AST.STATICMULASS)) && (lmode != Mode.STATIC))
                throw new ExEx("<-, +<-, -<- or *<- LHS variable '" +
                                    lvid + "' not STATIC mode", ln);
            else if ((asstype == AST.SELVAL) &&
                     !((lmode == Mode.SELECTVALUE) || (lmode == Mode.OUTPUT)))
                throw new ExEx("|- LHS variable '" +
                                    lvid + "' not SELECTVALUE or OUTPUT mode", ln);
            else if ((asstype == AST.QUEUE) && (lmode != Mode.QUEUE))
                throw new ExEx("<<- LHS variable '" +
                                    lvid + "' not QUEUE mode", ln);
            
            // resolve the clocks
            if (rval != null)
                rval.resolveClocks(null, Calloc.VALUE, loc);  // RHS variable(s) and subscript(s) if any
            if (!lnull)
                lref.resolveClocks(null, Calloc.ASSIGN, loc); // LHS variable and subscript(s) if any

            // do assignment
            if (!lnull) {
                ArgParams   ap;
                boolean     err = false;
                boolean     integer_assign =
                                    (lref.getPrimType() == Ptype.UINT) ||
                                    (lref.getPrimType() == Ptype.INT);
                boolean     numeric_assign =
                                    integer_assign ||
                                    (lref.getPrimType() == Ptype.UFIXED) ||
                                    (lref.getPrimType() == Ptype.FIXED);
                TreeOp      treeop = TreeOp.ASSIGN;
                TDEOp       tdeop = TDEOp.ASSIGN;
                
                if (incrdecr && !integer_assign)
                    throw new ExEx("++ or -- statement on non-integer variable", loc);
                
                switch (asstype) {
                case STATICADDASS:
                    err = !numeric_assign;
                    treeop = TreeOp.ADD;
                    tdeop = TDEOp.ADD;
                    break;
                case STATICSUBASS:
                    err = !numeric_assign;
                    treeop = TreeOp.SUB;
                    tdeop = TDEOp.SUB;
                    break;
                case STATICMULASS:
                    err = !numeric_assign;
                    treeop = TreeOp.MUL;
                    tdeop = TDEOp.MUL;
                    break;
                default:
                    break;
                }
                if (err)
                    throw new ExEx("+<-, -<- or *<- used on non-numeric type", loc);
                                
                // If the operator is +<-, -<-  or *<-, generate a
                // new RHS expression using the LHS variable and the RHS
                // expression and change the assignment to <-.
                if ((asstype == AST.STATICADDASS) ||
                    (asstype == AST.STATICSUBASS) ||
                    (asstype == AST.STATICMULASS)) {
                    // not using an ALU -
                    // generate a RHS expression using the LHS variable
                    Val     lval = lref.getVal(loc);
                    rval = evaluate (treeop, lval, rval, loc);
                    new_asstype = AST.STATICASS;
                }
                
                if (numeric_assign) {
                    if (lref.typeOK(loc, Ptype.FIXED, Ptype.UFIXED) &&
                        rval.typeOK(loc, Ptype.FLOAT) &&
                        rmode == Mode.IMMEDIATE) {
                        // assigning const FLOAT to UFIXED/FIXED
                        // convert float to integer constant with offset
                        rval.toTarget(lref);
                    } else
                        rval.toTarget(lref);
                    ap = new ArgParams(lref, rval, treeop, tdeop, false, loc);
                    newval = new Val(null, Mode.VALUE, ap.right_tdevar, loc);
                    newval.addExecSets(rval);
                    newval.andSetQueues(rval.getQueues(), loc);
                    newval.addOVars(rval);
                    newval.addIVars(rval);
                    rval = newval;
                }
                
                TDEVar  write;
                if (pri_in != null)
                    checkedqueues = null;
                if ((lmode == Mode.QUEUE) && (((Queue)lvar).getBufferSize() == 0))
                    write = wpending;
                else
                    write = start_del;
                
                if (lref.hasTargetSubs()) {
                    // break lref up into selected chunks
                    // AND the exec signal with the select signal for the chunk
                    Iterator<?>    it = lref.targSubIterator();
                    while (it.hasNext()) {
                        Ref partlref = (Ref)it.next();
                        partlref.addIVars(lref);
                        partlref.addOVars(lref);
                        TDEVar  select = partlref.getSelect();
                        partlref.checkMatch(rval, false, "assignment ", loc);
                        TDEVar  vsexec = tdelist.and(write, select, loc);
                        partlref.assignTo(rval, vsexec, new_asstype, toplevel, lvloc);
                    }
                } else {
                    lref.assignTo(rval, write, new_asstype, toplevel, lvloc);
                    if ((lvar.getDirect() || lvar.getContinuous() || ThreePL.continuous) &&
                        ((asstype == AST.STATICASS) ||
                         (asstype == AST.STATICADDASS) ||
                         (asstype == AST.STATICSUBASS) ||
                         (asstype == AST.STATICMULASS) ||
                         (asstype == AST.SELVAL)) &&
                        rval.getQueues().isEmpty()) {
                        direct = true;
                        if (toplevel) {
                            tdelist.connect(start_del, TDEVar.VCC);
                            return(EXECR.NONE);
                        }
                    }
                }
            } else {
                // null queue read - mark the queue(s) as used
                rval.getQueues().setUsed();
            }
            
            // collect queue references
            if ((rval != null) /*&& (rval.getTDEVar() != null)*/)
                queues.and_set(rval.getQueues(), loc);
            if (!lnull)
                queues.and_set(lref.getQueues(), loc);

            // generate the queue pops
            queues.addPops(null, start_del, rpending, loc);

            // generate queue and priority wait where necessary
            TDEVar  v = tdelist.execp(queues, checkedqueues, availok, null, false, start, pri_in, pri_out, esig, wpending, rpending, loc);
            tdelist.connect(start_del, v);
            tdelist.del(finish, start_del, clock, esig, queues);

            startl.add(start);
            finishl.add(finish);
        }
        
        return(EXECR.NONE);
    }

    static public void createStaticIncrDecr (
        TDEVar              esig,
        ArrayList<TDEVar>   startl, 
        ArrayList<TDEVar>   finishl,
        QueueRefs           queues,
        boolean             toplevel,
        boolean             availok,
        QueueRefs           checkedqueues,
        TDEVar              pri_in,
        TDEVar              pri_out,
        Node                n,
        Ref                 lref,
        Flag                f,
        SrcLoc              loc
    ) {
        TDEVar  clock = ThreePL.getCurrentClock();
        TDEVar  start = tdelist.signal("S", loc);
        TDEVar  start_del = tdelist.signal("S", loc);
        TDEVar  finish = tdelist.signal("F", loc);
        Val     newval;
        Var     lvar = lref.getVar();
        String  lvid = lvar.getID(IDtype.LITERAL);
        Val     rval = new Val(1, loc);

        // Check LHS type.
        if ((lref.getPrimType() != Ptype.UINT) && (lref.getPrimType() != Ptype.INT))
                throw new ExEx(":= LHS variable '" +
                                lvid + "' not type \"uint:\" or \"int:\"", loc);

        if (clock == null)
            throw new ExEx("No current clock domain for target assignment", loc);


        // resolve the clocks
        lref.resolveClocks(null, Calloc.ASSIGN, loc); // LHS variable and subscript(s) if any

        // do assignment
        ArgParams   ap;
        TreeOp      treeop = TreeOp.ASSIGN;
        TDEOp       tdeop = TDEOp.ASSIGN;

        switch (f) {
        case PREINCR:
        case POSTINCR:
            treeop = TreeOp.ADD;
            tdeop = TDEOp.ADD;
            break;
        case PREDECR:
        case POSTDECR:
            treeop = TreeOp.SUB;
            tdeop = TDEOp.SUB;
            break;
        case NONE:
            break;
        }
        
        // If the operator is +<- or -<- and the ALU directive is not
        // set and true and the alu attribute for the LHS variable is
        // not set and true, or if the operator is *<-, generate a
        // new RHS expression using the LHS variable and the RHS
        // expression and change the assignment to <-.
        
        // generate a RHS expression using the LHS variable
        Val     lval = lref.getVal(loc);
        rval = evaluate (treeop, lval, rval, loc);

        rval.toTarget(lref);
        ap = new ArgParams(lref, rval, treeop, tdeop, false, loc);
        newval = new Val(null, Mode.VALUE, ap.right_tdevar, loc);
        newval.addExecSets(rval);
        newval.andSetQueues(rval.getQueues(), loc);
        newval.addOVars(rval);
        newval.addIVars(rval);
        rval = newval;

        if (lref.hasTargetSubs()) {
            // break lref up into selected chunks
            // AND the exec signal with the select signal for the chunk
            Iterator<?>    it = lref.targSubIterator();
            while (it.hasNext()) {
                Ref partlref = (Ref)it.next();
                partlref.addIVars(lref);
                partlref.addOVars(lref);
                TDEVar  select = partlref.getSelect();
                partlref.checkMatch(rval, false, "assignment ", loc);
                TDEVar  vsexec = tdelist.and(start_del, select, loc);
                partlref.assignTo(rval, vsexec, AST.STATICASS, toplevel, loc);
            }
        } else {
            lref.assignTo(rval, start_del, AST.STATICASS, toplevel, loc);
            if ((lvar.getDirect() || lvar.getContinuous() || ThreePL.continuous) &&
                rval.getQueues().isEmpty()) {
                if (toplevel) {
                    tdelist.connect(start_del, TDEVar.VCC);
                    if ((n instanceof PtrValNode) || (n instanceof GroupRefNode))
                        n.setDirect();
                    return;
                }
            }
        }

        // collect queue references
        queues.and_set(lref.getQueues(), loc);

        // if priority used ignore previous queue checks
        if (pri_in != null)
            checkedqueues = null;
        
        // generate the queue pops
        queues.addPops(null, start_del, null, loc);

        // generate queue and priority wait where necessary
        TDEVar  v = tdelist.execp(queues, checkedqueues, availok, null, false, start, pri_in, pri_out, esig, null, null, loc);
        tdelist.connect(start_del, v);
        tdelist.del(finish, start_del, clock, esig);

        startl.add(start);
        finishl.add(finish);
        
        return;
    }
}
