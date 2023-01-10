package threepl.nodes;

import static threepl.ThreePL.getModuleScope;
import static threepl.ThreePL.tdelist;
import static threepl.codegen.TDEVar.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

import threepl.ThreePL;
import threepl.codegen.TDE;
import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Clock;
import threepl.exec.QueueRefs;
import threepl.exec.Ref;
import threepl.exec.Scope;
import threepl.exec.Static;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.exec.Var.IDtype;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;
import threepl.parser.Token;

/**
 * This implements a petrinet statement. A petrinet statement is
 * a target module.
 */
public final class PetriNetNode extends Node implements Constant, TDEConstants {
    private Node                        reset = null;
    private HashSet<String>             inits = new HashSet<String>();
    private ArrayList<ArrayList<Token>> fromplaces = new ArrayList<ArrayList<Token>>();
    private ArrayList<ArrayList<Token>> toplaces = new ArrayList<ArrayList<Token>>();
    private ArrayList<Boolean>          strict = new ArrayList<Boolean>();
    private ArrayList<Node>             predicates = new ArrayList<Node>();
    private ArrayList<Node>             block = new ArrayList<Node>();
    private TreeMap<String,Ref>         places;
    private TreeSet<String>             unreachable_places;
    private TreeSet<String>             instance_inits;
    private Clock                       clock;

    public PetriNetNode (Token t) {
        super(t);
    }
    
    /**
     * Add a reset expression to the Petri net.
     * @param   reset is the reset expression node
     */
    public void addReset (Node reset) {
        this.reset = reset;
    }
    
    /**
     * Add the initial markings to the Petri net.
     * @param   inits is a list of Tokens as places to be initially marked
     */
    public void addInits (ArrayList<Token> inits) {
        for (Token t: inits)
            this.inits.add(t.image);
    }
    
    /**
     * Add a transition to the Petri net.
     * @param   from is an identifier list of 'from' places
     * @param   to is an identifier list of 'to' places
     * @param   strict is true if the transition is strict rather than weak
     * @param   e is a transition predicate (may be null)
     * @param   b is a block of statements to be executed on the
     *          transition (may be null)
     */
    public void addTransition (
        ArrayList<Token>    from,
        ArrayList<Token>    to,
        boolean             strict,
        Node                e,
        Node                b
    ) {
        fromplaces.add(from);
        toplaces.add(to);
        this.strict.add(Boolean.valueOf(strict));
        predicates.add(e);
        block.add(b);
    }
    
    /**
     * Execute a petri net.
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
        if (pri_in != null)
            throw new ExEx("'petrinet' statement nested within 'waitpri' statement", loc);
        
        TDEVar      clock_tdev = ThreePL.getCurrentClock();
        if (clock_tdev == null)
            throw new ExEx("No current clock domain for Petri net", loc);
        clock = ThreePL.getCurrentClockVar();

        // Create all the place variables in the current scope
        ArrayList<Ref[]>   fromreflist = new ArrayList<Ref[]>();
        ArrayList<Ref[]>   toreflist = new ArrayList<Ref[]>();
        
        places = new TreeMap<String,Ref>();
        instance_inits = new TreeSet<String>();
        instance_inits.addAll(inits);
        unreachable_places = new TreeSet<String>();
        for (ArrayList<Token> a: fromplaces)
            fromreflist.add(getplaces(a, false));
        for (ArrayList<Token> a: toplaces)
            toreflist.add(getplaces(a, true));
        
        //------------------------------------------------
        // This is a temporary fix to make queue reads and writes
        // execute in the parent scope. The code should be changed
        // so that there is no petrinet module and all the code
        // is generated in the parent module scope!
        // This would require removing the ILOOP creation code
        // below since the ILOOPS would be created by the parent module
        // block.
        Scope   parent_scope = getModuleScope();

        // Push a new module scope onto the scope stack.
        // This is used to make the whole petri net look like a single
        // module for queue acknowledgement purposes.
        // Although the block type for the scope is Btype.ILMOD (for
        // queue acknowledging in Var.addAck()) the (optional) code
        // block is Btype.PETRINET to avoid BlockNode.execute() from
        // enclosing each target statement in an ILOOP.
        Scope       scope = new Scope(Btype.ILMOD);
        ThreePL.pushScope(scope, loc);

        TDEVar              exstart = ThreePL.getCurrentStart(loc);
        Val                 false_val = new Val(false, loc);
        Val                 true_val = new Val(true, loc);
        TDEVar              reset_tdev = null;
        Iterator<Ref[]>     fit = fromreflist.iterator();
        Iterator<Ref[]>     tit = toreflist.iterator();
        Iterator<Boolean>   sit = strict.iterator();
        Iterator<Node>      pit = predicates.iterator();
        Iterator<Node>      bit = block.iterator();
        // Note: all five ArrayLists have the same length, so only need
        // to test one to control the iterations.
        while (fit.hasNext()) {
            Ref[]       fromrefs = fit.next();
            Ref[]       torefs = tit.next();
            boolean     strict_transition = sit.next().booleanValue();
            Node        e = pit.next();
            Node        b = bit.next();
            Val         predval = null;
            if (e != null) {
                predval = e.getVal();
                if (!predval.isPrimitive())
                    throw new ExEx("Petri net transition expression not primitive", e.loc);
                if (predval.getPrimType() != Ptype.LOG)
                    throw new ExEx("Petri net transition expression not type log", e.loc);
                if (!predval.isTarget())
                    predval.toTarget();
            }

            // Generate an ILOOP.
            // Within the ILOOP:
            // If no transition statement, generate -
            //
            //     when (from1 && from2 && ... && expr) par {
            //          from1 <- false;
            //          from2 <- false;
            //              .
            //              .
            //          to1 <- true;
            //          to2 <- true;
            //              .
            //              .
            //     }
            // The par does not need a par_wait as execution is
            // guaranteed to conclude in 1 cycle.
            //
            // If transition statement, create a local target log variable
            // 'busy', initially false and generate -
            //    
            //     when (!busy && from1 && from2 && ... && expr) seq {
            //          par {
            //              busy <- true;
            //              from1 <- false;
            //              from2 <- false;
            //                  .
            //                  .
            //              to1 <- true;
            //              to2 <- true;
            //                  .
            //                  .
            //              statement
            //          }
            //          reset(busy);
            //     }
            // The par does not need a par_wait as execution is
            // guaranteed to conclude when 'statement' concludes since
            // the assignments only take a single cycle.
            TDEVar      start_del = tdelist.signal("S", loc);
            TDEVar      start = tdelist.signal("S", loc);
            TDEVar      finish = tdelist.signal("F", loc);
            TDEVar      tstart = tdelist.signal("TS", loc);
            TDEVar      fstart = tdelist.signal("FS", loc);
            TDEVar      tfinish = tdelist.signal("TF", loc);
            TDEVar      ffinish = tdelist.signal("FF", loc);
            TDEVar      pntnotbusy = null;
            TDEVar      sstart_del;
            TDEVar      sfinish = null;
            TDE         iloop = new TDE(TDEType.ILOOP);
            TDE         and = new TDE(TDEType.AND);
            TDE         when = new TDE(TDEType.WHEN, loc);
            
            if (b != null) {
                // Have a transition statement.
                ArrayList<TDEVar>   sstartl = new ArrayList<TDEVar>();
                ArrayList<TDEVar>   sfinishl = new ArrayList<TDEVar>();
                QueueRefs            squeues = new QueueRefs();
                EXECR               status;

                status = b.execute(null, sstartl, sfinishl, squeues, false, false, null, null, null);
                if (status != EXECR.NONE)
                    throw new ExEx("Petri net transition statement has rogue 'break', 'continue' or 'return'", b.loc);
                if (sstartl.size() > 1)
                    throw new ExEx("Petri net transition statement not single target statement", b.loc);

                if (sstartl.size() == 1) {
                    TDEVar      pntbusy = tdelist.signal("PNTBUSY", loc);
                    pntnotbusy = tdelist.inv(pntbusy, loc);
                    TDE busyff = new TDE(TDEType.DFF);
                    busyff.add2p("R");          // initially 0
                    busyff.add2p(false);        // an FDRE
                    busyff.add2i(GND);         // D
                    busyff.add2ic(clock_tdev);  // C
                    busyff.add2i(GND);         // CE
                    busyff.add2i(finish);       // R
                    busyff.add2i(tstart);       // S
                    busyff.add2o(pntbusy);      // Q
                    tdelist.addTDE(busyff);
                    sstart_del = sstartl.get(0);
                    sfinish = sfinishl.get(0);
                    // generate possible queue wait on the code block
                    squeues.unbufferedQueues(false, "unbuffered queue in Petrinet statement", loc);
                    squeues.addPops(parent_scope, sstart_del, null, loc);
                    tdelist.connect (
                        sstart_del,
                        tdelist.execp(squeues, null, false, parent_scope, false, tstart, null, null, null, null, null, loc)
                    );
                }
            }

            // infinite loop
            iloop.add2i(exstart);
            iloop.add2i(finish);
            iloop.add2o(start);
            tdelist.addTDE(iloop);

            // AND gate for previous places & transition expression
            for (int i=0 ; i<fromrefs.length ; i++)
                // Strict or weak transition - check source places
                // all have tokens.
                and.add2i(fromrefs[i].getTDEVar());
            if (strict_transition) {
                // Strict transition only - check destination places do
                // not have tokens.
                for (int i=0 ; i<torefs.length ; i++) {
                    TDEVar  invout = tdelist.inv(torefs[i].getTDEVar(), loc);
                    and.add2i(invout);
                }
            }
            if (predval != null)
                and.add2i(predval.getTDEVar());
            if (pntnotbusy != null)
                and.add2i(pntnotbusy);

            // generate possible queue wait on the predicate
            if (predval != null) {
                QueueRefs    tequeues = predval.getQueues();
                tequeues.unbufferedQueues(false, "unbuffered queue in Petrinet statement", loc);
                tequeues.addPops(parent_scope, start_del, null, loc);
                tdelist.connect (
                    start_del,
                    tdelist.execp(tequeues, null, false, parent_scope, false, start, null, null, null, null, null, loc)
                );
            } else
                tdelist.connect(start_del, start);

            // WHEN
            when.add2i(start_del);
            when.add2i(and.finish());
            when.add2i(tfinish);
            when.add2i(ffinish);
            when.add2o(tstart);
            when.add2o(fstart);
            when.add2o(finish);
            tdelist.addTDE(when);

            // false execution path
            tdelist.del(ffinish, fstart, clock_tdev, null);

            // true execution path
            if (sfinish != null)
                tdelist.connect(tfinish, sfinish);
            else
                tdelist.del(tfinish, tstart, clock_tdev, null);

            // assignments to clear (remove tokens from) previous places
            // if not included in next places
            for (int i=0 ; i<fromrefs.length ; i++) {
                boolean same = false;
                for (int j=0 ; j<torefs.length ; j++)
                    if (fromrefs[i].getVar() == torefs[j].getVar()) {
                        same = true;
                        break;
                    }
                if (!same)
                    fromrefs[i].assignTo(false_val, tstart, AST.STATICASS, false, loc);
            }
            
            // assignments to set (add tokens to) next places
            for (int i=0 ; i<torefs.length ; i++)
                torefs[i].assignTo(true_val, tstart, AST.STATICASS, false, loc);
        }

        // Make all place variables read-only.
        // If there is a reset argument, connect this to place variables.
        if (reset != null) {
            Val resetval = reset.getVal();
            if (!resetval.isPrimitive())
                throw new ExEx("Petri net reset expression not primitive", loc);
            if (resetval.getPrimType() != Ptype.LOG)
                throw new ExEx("Petri net reset expression not type log", loc);
            if (!resetval.isTarget())
                resetval.toTarget();
            else
                if (!resetval.getQueues().isEmpty())
                    throw new ExEx("Petri net reset expression contains queues", loc);
            reset_tdev = resetval.getTDEVar();
        }
        for (Ref ref: places.values()) {
            ref.getVar().setReadOnly(loc);
            if (reset_tdev != null)
                ref.getVar().addReset(reset_tdev, ref.getWordSpec(), loc);
        }
        
        // Check for any remaining identifiers in the initial marking set -
        // these have not been used in any transition statements!
        if (instance_inits.size() != 0) {
            for (String s: instance_inits)
                ThreePL.msg("place " + s);
            throw new ExEx("one or more marked Petri net places not used", loc);
        }
        
        // Check for any remaining places in the unreachable place set -
        // these have not been initially marked and do not have any
        // transitions to them!
        if (unreachable_places.size() != 0) {
            for (String s: unreachable_places)
                ThreePL.msg("place " + s);
            throw new ExEx("one or more Petri net places unreachable", loc);
        }
        
        // Pop the fake module scope.
        ThreePL.popScope();

        return(EXECR.NONE);
    }
    
    private Ref[] getplaces (ArrayList<Token> pl, boolean to) {
        Ref[]       refs = new Ref[pl.size()];
        int         i = 0;
        for (Token tok: pl) {
            SrcLoc  loc = new SrcLoc(tok);
            String  id = tok.image;
            boolean init = false;
            if (to && unreachable_places.contains(id))
                unreachable_places.remove(id);
            if (places.containsKey(id)) {
                refs[i++] = places.get(id);
                continue;
            }
            Static  svar;
            Var     var = ThreePL.findVar(id, Context.DEFAULT, loc);
            if (var == null) {
                Type    type = new Type(Ptype.LOG, 1, 0, 0, 0, null, null);
                svar = new Static(new Ident(id, Context.DEFAULT), type, null, false, false, loc);
                svar.setInputClock(clock, Calloc.PETRINET, loc);
                ThreePL.addVar(svar, Context.DEFAULT, loc);
            } else {
                if (var.getMode() != Mode.STATIC)
                    throw new ExEx("Petri net place " + var.getID(IDtype.LITERAL) + " is not static", loc);
                if (var.getType().getPrimType() != Ptype.LOG)
                    throw new ExEx("Petri net place " + var.getID(IDtype.LITERAL) + " is not type log", loc);
                if (var.isAssigned())
                    throw new ExEx("Petri net place " + var.getID(IDtype.LITERAL) + " has assignments", loc);
                svar = (Static)var;
            }
            refs[i] = svar.getRef(null, null, loc);
            if (instance_inits.contains(id)) {
                instance_inits.remove(id);
                init = true;
            }
            svar.setStateVarInit(init, loc);
            places.put(id, refs[i]);
            if (!init && !to)
                unreachable_places.add(id);
            i++;
        }
        return(refs);
    }
}
