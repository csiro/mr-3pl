package threepl.nodes;

import java.util.ArrayList;

import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.QueueRefs;
import threepl.exec.Val;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;
import threepl.parser.Token;

/**
 * This implements a switch statement. A switch statement is
 * an immediate construct executed during interpretation, not
 * in the FPGA.
 */
public class SwitchNode extends Node implements Constant {
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
    
    public SwitchNode (Node test, Token t) {
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
     * Execute a switch.
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
        EXECR       status = EXECR.NONE;
        Val         test_val = subnodes.getNode(0).getVal();
        BlockNode   block = null;
        
        if (test_val == null)
            throw new ExEx("switch test value is null", loc);
        if (test_val.getMode() != Mode.IMMEDIATE)
            throw new ExEx("switch statement test expression is not immediate", loc);
        if (!test_val.isPrimitive())
            throw new ExEx("switch statement test expression is not primitive", loc);

        for (caseinfo ci : cases) {
            Node    n = ci.caseval;

            if (n instanceof CompoundValNode) {
                CompoundValNode cvn = (CompoundValNode)n;
                ArrayList<Val>  al = cvn.getValList("case value list");
                
                if (al.size() == 0)
                    throw new ExEx("case value list is null", loc);
                for (Val casevalue: al) {
                    if (casevalue.getMode() != Mode.IMMEDIATE)
                        throw new ExEx("case value is not immediate", loc);
                    if (caseEquals(casevalue, test_val, loc)) {
                        // found case match
                        block = ci.b;
                        break;
                    }
                }
            } else {
                Val casevalue = n.getVal();
                if (casevalue == null)
                    throw new ExEx("case value is null", loc);
                if (casevalue.getMode() != Mode.IMMEDIATE)
                    throw new ExEx("case value is not immediate", loc);
                if (caseEquals(casevalue, test_val, loc))
                    // found case match
                    block = ci.b;
            }
        }

        if (block == null)
            block = default_block;

        if (block != null)
            status = block.execute(esig, startl, finishl, queues, toplevel, availok, checkedqueues, pri_in, pri_out);

        return(status);
    }

    static boolean caseEquals(Val casevalue, Val swval, SrcLoc loc) {
        if (casevalue.getMode() != Mode.IMMEDIATE)
            throw new ExEx("case value is not immediate");
        if (!casevalue.isPrimitive())
            throw new ExEx("case value is not primitive");
        switch (casevalue.getValPType(0)) {
        case BITS:
        case UINT:
        case INT:
            switch (swval.getValPType(0)) {
            case BITS:
            case UINT:
            case INT:
                break;
            default:
                throw new ExEx("switch and case types do not match", loc);
            }
            break;
        case ENUM:
            if (swval.getValPType(0) != Ptype.ENUM)
                throw new ExEx("switch and case types do not match", loc);
            break;
        default:
            if (casevalue.getValPType(0) != swval.getValPType(0))
                return(false);
        }
        return(casevalue.getVal(0).equals(swval.getVal(0)));
    }
}
