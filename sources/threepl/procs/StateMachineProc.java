package threepl.procs;

import static threepl.ThreePL.getCurrentClockVar;
import static threepl.ThreePL.getCurrentStart;
import static threepl.ThreePL.getModuleScope;
import static threepl.ThreePL.tdelist;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import threepl.ThreePL;
import threepl.codegen.TDE;
import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.QueueRefs;
import threepl.exec.Ref;
import threepl.exec.Static;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.exec.Var.IDtype;
import threepl.nodes.Ident;
import threepl.nodes.Node;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * <p>An inbuilt procedure to declare a state machine. No in-line code is
 * created - the state machine is effectively a module however the state
 * variables are in the scope in which this procedure is called. It may
 * be called in any context including the global context. Although not an
 * error, it should not be called from within a target control structure
 * such as WHEN, WHILE or DO WHILE as it does not generate in-line code
 * and hence would not appear within the body of the control structure or
 * be mediated by it in any way.
 *
 * <p>States are represented by static logical variables, one and only
 * one of which is true at any time (this is called 'one-hot encoding').
 * 
 * <p>The procedure has a multiple of 3 input arguments, with an optional
 * extra trailing input argument, and no output arguments. The input
 * arguments are triples, each of which describes a state transition. The
 * 1st and 2nd arguments of a triple are the previous and next state of
 * the transition and the 3rd argument of the triple is the logical
 * expression causing the transition. The optional trailing input
 * argument is a reset logical expression which, when it evaluates as
 * true, will reset the state machine to its initial state.
 * 
 * <p>The states are unsubscripted variable identifiers. For each state
 * appearing in a transition triple a target static variable of type log
 * will be created in the current scope. If such a variable already
 * exists it will be used instead (this allows variables to be created in
 * advance for use in previous expressions prior to the call which
 * creates the state machine). State variable identifiers must not have
 * been previously used in the local or global scope except as described
 * above. In addition if a state variable is pre-declared it must not be
 * assigned a value or be initialised. State variables created by this
 * procedure can never be assigned by user code but otherwise can be used
 * as normal static logical variables. State variables for one state
 * machine cannot be used as state variables in another state machine
 * (the same identifiers can be used where the scope is not shared).
 * 
 * <p>The transition expressions may be immediate or target expressions.
 * They may contain queue reads. Transition expressions must evaluate to
 * type log.
 *
 * <p>The optional trailing reset expression must evaluate to type log
 * and must not contain queues.
 *
 * <p>The first state encountered in the transition list is the initial
 * state and hence will be initialised to true. This applies even when the
 * initial state variable is pre-declared in which case this procedure
 * will initialise that variable.
 *
 * <p>At the moment there is little checking of the validity of the
 * state transitions. The only check is that all states can be entered.
 * Invalid state transitions may result in multiple or no state variables
 * being true. Validation checking may be improved later.
 */
public class StateMachineProc extends InbuiltProc implements Constant, TDEConstants {

    /**
     * Construct the inbuilt procedure statemachine().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public StateMachineProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
    }

    /**
     * Execute the procedure statemachine().
     * @param   inargs is a list of input argument tree nodes
     * @param   outargs is a list of output argument tree nodes
     * @param   toplevel is true if we are at the module, procedure or function
     *          level
     */
    public void execute (
        NodeList    inargs, 
        NodeList    outargs, 
        boolean     toplevel
    ) {
        SrcLoc      loc = inargs.getCallLoc();
        boolean     have_reset;
        if (outargs.size() != 0)
            throw new ExEx("statemachine() cannot have output arguments", loc);
        int             num_triples = inargs.size() / 3;
        Iterator<Node>  it = inargs.iterator();
        Node            n;
        String          s_f;
        String          s_t;
        Ref             ref_f;  // from state variable reference
        Ref             ref_t;  // to state variable reference
        Val             val;    // transition expression value
        TreeMap<String,Ref>     vars = new TreeMap<String,Ref>();
        TreeMap<String,HashSet<Transition>>     into = new TreeMap<String,HashSet<Transition>>();
        boolean     init = true;    // 1st state initialised true
        Val         false_val = new Val(false, loc);
        Val         true_val = new Val(true, loc);
        boolean     first = true;
        String      init_id = null;
        TDEVar      exstart = getCurrentStart(loc);
        TDEVar      reset_tdev = null;

        if (num_triples == 0)
            throw new ExEx("statemachine() has no transitions", loc);
        switch (inargs.size() % 3) {
        case 0:
            have_reset = false;
            break;
        case 1:
            have_reset = true;
            break;
        default:
            // must have multiple of 3 arguments or else
            // multiple of 3 arguments + 1
            // (state, state, expression)+ with optional trailing reset
            // expression
            throw new ExEx("statemachine() arguments not multiple of 3", loc);
        }        
        
        // number of arguments is a multiple of 3, so guaranteed triples here
        for (int i=0 ; i<num_triples ; i++) {
            n = it.next();
            s_f = n.getIdentifier("statemachine() - state from argument").getId();
            ref_f = get_state_ref(vars, s_f, into, init, loc);
            if (first) {    // 1st state encountered is initial state
                first = false;
                init_id = s_f;
                init = false;   // other states intitialised false
            }

            n = it.next();
            s_t = n.getIdentifier("statemachine() - state to argument").getId();
            ref_t = get_state_ref(vars, s_t, into, init, loc);

            n = it.next();
            val = n.getVal();
            if (!val.isPrimitive())
                throw new ExEx("statemachine() transition expression not primitive", loc);
            if (val.getPrimType() != Ptype.LOG)
                throw new ExEx("statemachine() transition expression not type log", loc);
            if (!val.isTarget())
                val.toTarget();

            into.get(s_t).add(new Transition(ref_f, ref_t, val));
        }

        if (have_reset) {
            n = it.next();
            val = n.getVal();
            if (!val.isPrimitive())
                throw new ExEx("statemachine() reset expression not primitive", loc);
            if (val.getPrimType() != Ptype.LOG)
                throw new ExEx("statemachine() reset expression not type log", loc);
            if (!val.isTarget())
                val.toTarget();
            else
                if (!val.getQueues().isEmpty())
                    throw new ExEx("statemachine() reset expression contains queues", loc);
            reset_tdev = val.getTDEVar();
        }
                
        // check for any states that cannot be entered, except
        // initial state
        for (Map.Entry<String,HashSet<Transition>> me: into.entrySet()) {
            String              toid = me.getKey();
            HashSet<Transition> s = me.getValue();
            if ((s.size() == 0) && (!toid.equals(init_id)))
                throw new ExEx("state " + toid + " never entered", loc);
        }
        
        Var     clockvar = getCurrentClockVar();
        TDEVar  clock = clockvar.getClkSig();
        // generate code for state transitions
        for (HashSet<Transition> s: into.values()) {
            for (Transition tr: s) {
                Ref         from = tr.getFromState();
                Ref         to = tr.getToState();
                Val         expr = tr.getTransExpr();
                // generate -
                //
                //     when (expr && from) {
                //          from <- false;
                //          to <- true;
                //     }
                //
                // within an ILOOP
                TDEVar  andexpr = tdelist.signal("A", loc);
                TDEVar  start = tdelist.signal("S", loc);
                TDEVar  finish = tdelist.signal("F", loc);
                TDEVar  tstart = tdelist.signal("TS", loc);
                TDEVar  fstart = tdelist.signal("FS", loc);
                TDEVar  tfinish = tdelist.signal("TF", loc);
                TDEVar  ffinish = null;
                TDE     iloop = new TDE(TDEType.ILOOP);
                TDE     and = new TDE(TDEType.AND);
                TDE     when = new TDE(TDEType.WHEN, loc);
                
                // infinite loop
                iloop.add2i(exstart);
                iloop.add2i(finish);
                iloop.add2o(start);
                tdelist.addTDE(iloop);

                // AND gate for previous state & transition expression
                and.add2i(from.getTDEVar());
                QueueRefs tequeues = expr.getQueues();
                if ((tequeues != null) && (!tequeues.isEmpty())) {
                    Val     availval = tequeues.getBQAvail(getModuleScope(), null, loc);
                    TDEVar  avail = availval.getTDEVar();
                    tequeues.addPops(getModuleScope(), andexpr, null, loc);
                    and.add2i(avail);
                    availval.resolveClocks(null, Calloc.QUEUEAVAIL, loc);
                }
                and.add2i(expr.getTDEVar());
                and.add2o(andexpr);
                tdelist.addTDE(and);
                 
                // false execution path
                ffinish  = tdelist.signal("FF", loc);
                tdelist.del(ffinish, fstart, clock, null);
                
                // WHEN
                when.add2i(start);
                when.add2i(andexpr);
                when.add2i(tfinish);
                when.add2i(ffinish);
                when.add2o(tstart);
                when.add2o(fstart);
                when.add2o(finish);
                tdelist.addTDE(when);
                
                // true execution path
                tdelist.del(tfinish, tstart, clock, null);
                
                // assignment to clear previous state
                from.assignTo(false_val, tstart, AST.STATICASS, false, loc);

                // assignment to set next state
                to.assignTo(true_val, tstart, AST.STATICASS, false, loc);
                
                from.addOVar(from.getVar());
                from.resolveClocks(null, Calloc.ASSIGN, loc);
                to.addOVar(to.getVar());
                to.resolveClocks(null, Calloc.ASSIGN, loc);
                
                expr.resolveClocks(null, Calloc.ASSIGN, loc);
            }
        }

        // Make all state variables read-only
        // If there is a reset argument, connect this to state variables.
        for (HashSet<Transition> s: into.values()) {
            for (Transition tr: s) {
                Ref         from = tr.getFromState();
                Ref         to = tr.getToState();
                from.getVar().setReadOnly(loc);
                to.getVar().setReadOnly(loc);
                if (have_reset) {
                    from.getVar().addReset(reset_tdev, loc);
                    to.getVar().addReset(reset_tdev, loc);
                }
            }
        }
    }
    
    private Ref get_state_ref (
        TreeMap<String,Ref>                 vars,
        String                              id,
        TreeMap<String,HashSet<Transition>> m,
        boolean                             init,
        SrcLoc                              loc
    ) {
        if (vars.containsKey(id))
            return(vars.get(id));
        
        Var     var = ThreePL.findVar(id, Context.DEFAULT, loc);
        Ref     ref;
        if (var == null) {
            Type    type = new Type(Ptype.LOG, 1, 0, 0, 0, null, null);
            var = new Static(new Ident(id, Context.DEFAULT), type, null, false, false, loc);
            ThreePL.addVar(var, Context.DEFAULT, loc);
        } else {
            if (var.getMode() != Mode.STATIC)
                throw new ExEx("state variable " + var.getID(IDtype.LITERAL) + " is not static", loc);
            if (var.getType().getPrimType() != Ptype.LOG)
                throw new ExEx("state variable " + var.getID(IDtype.LITERAL) + " is not type log", loc);
            if (var.isAssigned())
                throw new ExEx("state variable " + var.getID(IDtype.LITERAL) + " has assignments", loc);
        }
        if (!m.containsKey(id))
            m.put(id, new HashSet<Transition>());
        ref = var.getRef(null, null, loc);
        ((Static)var).setStateVarInit(init, loc);
        vars.put(id, ref);
        return(ref);
    }
    
    private class Transition {
        private Ref     from;
        private Ref     to;
        private Val     expr;
        
        public Transition (Ref from, Ref to, Val expr) {
            this.from = from;
            this.to = to;
            this.expr = expr;
        }
        
        public Ref getFromState() {
            return(from);
        }
        
        public Ref getToState() {
            return(to);
        }
        
        public Val getTransExpr() {
            return(expr);
        }
    }
}
