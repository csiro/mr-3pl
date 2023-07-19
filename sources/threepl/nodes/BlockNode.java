package threepl.nodes;

import static threepl.ThreePL.tdelist;

import java.util.ArrayList;
import threepl.ThreePL;
import threepl.codegen.TDE;
import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Body;
import threepl.exec.Group;
import threepl.exec.QueueRefs;
import threepl.exec.Scope;
import threepl.exec.Var;
import threepl.parser.Constant;
import threepl.parser.Token;

/**
 * This implements a code block. The statements within it are
 * subnodes.
 *
 * <p>Execution of statements within the block is carried out sequentially,
 * however detailed execution control of break and continue statements is
 * done here. Block execution is done through a call to method
 * <b>execute()</b>.
 *
 * <p>If method <b>execute()</b> encounters a break, continue or return,
 * the nodes for these (BreakNode, ContinNode and ReturnNode) return a status
 * code which is detected. On getting a return status <b>execute()</b>
 * itself returns immediately with that return status. The break and continue return status codes
 * are eventually intercepted by an enclosing loop node (WhileNode, DoWhileNode
 * or ForNode) which reacts accordingly. For a break the loop node will
 * return (with a zero status code). For a continue the loop node will
 * execute the next iteration. A return status code will cause <b>execute</b>
 * methods to return with the same status code all the way back to a
 * containing Module, Procedure or function.
 */
public class BlockNode extends Node implements Constant, TDEConstants {
    private Btype       btype;
    @SuppressWarnings("unused")
    private Group       group = null;   // associated Group body for Btype.INIT
    private Scope       scope;          // temporary location to allow scope to be passed on
    @SuppressWarnings("unused")
    private Scope       fmscope;        // file module scope
    
    /**
     * Construct a code block.
     * @param   btype is the block type
     * @param   t is the operator token, from which the file source location
     *          is extracted
     * @param   body is the associated Group (3PL class) Body
     */
    public BlockNode (Btype btype, Token t, Body body) {
        super(t);
        this.btype = btype;
        if (btype == Btype.INIT) {
            if (body instanceof Group)
                group = (Group)body;
            else
                throw new ExEx("initial block not within class");
        }
    }
    
    /**
     * Add a file module scope to the block.
     * @param scope is the file module scope
     */
    public void addFileScope (Scope scope) {
        fmscope = scope;
    }
    
    /**
     * Execute a code block.
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
     * @return  the EXECR.NONE, EXECR.break, EXECR.continue or EXECR.return
     *          status value
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
        switch (btype) {
        case GROUP:
        case FMOD:      // file module
        case NMOD:      // called module
        case ILMOD:     // inline module
        case PROC:      // procedure
        case FUNC:      // function
        case INIT:      // class initial block
        case PETRINET:  // petri net
        case FSRC:
            break;
        default:
            // If this is a Group (3PL class), Module, Procedure or Function block the
            // scope block will already have been created and pushed
            // onto the scope stack in exec/Module.execute(),
            // exec/Procedure.execute() or exec/Function.getVal().
            // If an INIT block the scope block will already have been created and pushed
            // onto the scope stack.
            // For a Petrinet nodes/PetriNetNode.execute() creates and
            // pushes a new scope.
            // For source blocks a new scope is not created.
            // For all others, do it here.
            scope = new Scope(btype);
            ThreePL.pushScope(scope, null);
        }

        EXECR       status = EXECR.NONE;
        QueueRefs    p;
        
        for (Node n: subnodes) {
            if (n == null)
                continue;
            
            // execute the statement
            p = new QueueRefs();
            status = n.execute(esig, startl, finishl, p, toplevel, availok, checkedqueues, pri_in, pri_out);
            if (!toplevel)
                queues.and_set(p, loc);
                        
            // If we are a module block, embed each target statement in an
            // infinite loop. The start signal for each ILOOP, "startsigdel", has
            // been delayed from the clock domain start signal, "startsig", to
            // help fanout since now "startsig" has no combinatorial logic.
            if ((btype == Btype.NMOD) ||
                (btype == Btype.FMOD) ||
                (btype == Btype.ILMOD)) {
                if (startl.size() != 0) {
                    for (int i=0 ; i<startl.size() ; i++) {
                        TDEVar  start = startl.get(i);
                        TDEVar  finish = finishl.get(i);
                        Var     clkvar = start.getClkVar();
                        if (clkvar == null)
                            throw new ExEx("no clock domain for target statement", loc);
                        TDEVar  clk = clkvar.getClkSig();
                        TDEVar  startsig = clkvar.getStartSig(loc);
                        TDEVar  startsigdel = tdelist.signal("SSD", loc);
                        TDE tde = new TDE(TDEType.ILOOP);
                        tdelist.del(startsigdel, startsig, clk, null);
                        tde.add2i(startsigdel);
                        tde.add2i(finish);
                        tde.add2o(start);
                        tdelist.addTDE(tde);
                    }
                }
                // Clear statement from start and finish lists ready for next one
                // (not passing any back up either).
                startl.clear();
                finishl.clear();
            }

            // If we have a break, continue or return, stop executing
            // this block.
            if (status != EXECR.NONE)
                break;
        }
        
        // Pop the block from the scope block stack except for -
        // - Petri net blocks, where the scope stays on the scope stack during
        //   expansion of all transitions and associated code blocks,
        // - source blocks where a new scope has not been pushed.
        // Exit this code block, returning the status
        // ('none' if no 'break', 'continue' or 'return')
        // but check that the non-zero status is legal!
        if ((btype != Btype.PETRINET) && (btype != Btype.FSRC))
            ThreePL.popScope();
        if (status != EXECR.NONE) {
            switch (btype) {
            case FORVAR:
            case UNDEF:
            case WHEN:
            case SEQ:  
            case PAR:  
            case SYNC:  
                throw new ExEx(status.toString().toLowerCase() + " in wrong context", loc);
            case FMOD:     // file module
            case NMOD:     // called module
            case ILMOD:    // inline module
            case PROC:     // procedure
            case FUNC:     // function
                if (status == EXECR.RETURN)
                    return(status);
                throw new ExEx(status.toString().toLowerCase() + " in wrong context", loc);
            case WHILE:
            case DOWHILE:
            case FOR: 
            case CASE:
            case IF:
                return(status);
            default:
                throw new ExEx("BlockNode CODE ERROR - MISSING CASE", loc);
            }
        }
        return(EXECR.NONE);
    }
}
