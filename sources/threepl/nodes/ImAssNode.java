package threepl.nodes;

import java.util.ArrayList;

import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.QueueRefs;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.parser.Constant;
import threepl.parser.Token;

/**
 * Immediate variable assignment '='.
 */
public final class ImAssNode extends Node implements Constant {
    private AST     asstype;

    /**
     * Construct an immediate variable assignment node.
     * @param   n1 is the LHS <b>VarNode</b>
     * @param   n2 is the RHS expression node
     * @param   t is the operator token from which the assignment type
     *          is determined
     */
    public ImAssNode (Node n1, Node n2, Token t) {
        super(n1.getSrcLoc());
        addSubNode(n1);
        addSubNode(n2);
        if (t.image.equals("="))
            asstype = AST.IMASS;
        else if (t.image.equals("+="))
            asstype = AST.IMPLUSASS;
        else if (t.image.equals("-="))
            asstype = AST.IMMINASS;
        else if (t.image.equals("*="))
            asstype = AST.IMMULASS;
        else if (t.image.equals("/="))
            asstype = AST.IMDIVASS;
        else if (t.image.equals("&="))
            asstype = AST.IMBANDASS;
        else if (t.image.equals("|="))
            asstype = AST.IMBORASS;
        else if (t.image.equals("^="))
            asstype = AST.IMBXORASS;
        else if (t.image.equals("&&="))
            asstype = AST.IMLANDASS;
        else if (t.image.equals("||="))
            asstype = AST.IMLORASS;
        else if (t.image.equals("^^="))
            asstype = AST.IMLXORASS;
        else if (t.image.equals("<<="))
            asstype = AST.IMLSASS;
        else if (t.image.equals(">>="))
            asstype = AST.IMRSASS;
    }

    /**
     * Execute immediate or type assignment.
     * This is an immediate statement and the start execute signal is simply
     * passed back as the finish signal.
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
        // LHS reference
        Node    ln = subnodes.getNode(0);
        if (ln instanceof NullNode)
            throw new ExEx("Assignment to null reference", loc);
        Ref     lref = ln.getRef("immediate assignment");
        Ptype   lpt = lref.getPrimType();
       
        // RHS value
        Node    rn = subnodes.getNode(1);

        if (rn instanceof NullNode) {
            if ((lref.getPrimType() != Ptype.PTR) &&
                (lref.getPrimType() != Ptype.MAP) &&
                (lref.getPrimType() != Ptype.LIST))
                throw new ExEx("Assignment of null value", loc);
        }
        Val     rval;
        if (rn instanceof CompoundValNode) {
            if ((asstype == AST.IMASS) || ((asstype == AST.IMPLUSASS) && (lref.getPrimType() == Ptype.MAP)))
                rval = ((CompoundValNode)rn).getVal(lref.getType());
            else
                throw new ExEx(asstype.assname() + " assignment of non-primitive value", loc);
        } else
            rval = rn.getVal();
       
        // check LHS mode
        if (lref.getMode() != Mode.IMMEDIATE)
            throw new ExEx("immediate assignment to target variable", loc);

        // check for constant value mode on RHS -
        // If so, convert to immediate value.
        if ((rval.getMode() == Mode.VALUE) && !((lpt == Ptype.MAP) || (lpt == Ptype.LIST))) {
            TDEVar  rtdev = rval.getTDEVar();
            if (!rtdev.isConst())
                throw new ExEx(asstype.assname() + " assignment of non-constant value mode variable", loc);
            rval =rtdev.getConstVal();
        }
        
        // Check RHS mode
        Ptype   rpt = rval.getPrimType();
        if (rval.getMode() != Mode.IMMEDIATE) {
            if ((rval.getMode() != Mode.CLOCK) /*|| ((lpt != Ptype.MAP) && (lpt != Ptype.LIST))*/)        
                // Allow clock mode assignments to lists and maps and error on anything else.
                throw new ExEx("immediate assignment from target value", loc);
        }

        // for simple assignment - match check unless special cases -
        //  * assignment of null to pointer variable
        //  * assignment to a map or a list
        //  * assignment to a class
        if ((asstype == AST.IMASS) &&
            !((lpt == Ptype.PTR) && (rpt == Ptype.NULL)) &&
            !((lpt == Ptype.MAP) || (lpt == Ptype.LIST) || (lpt == Ptype.CLASS)))
            lref.checkMatch(rval, false, "assignment", loc);

        lref.assignTo(asstype, rval, loc);

        return(EXECR.NONE);
    }
}
