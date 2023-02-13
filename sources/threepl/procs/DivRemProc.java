package threepl.procs;

import static threepl.ThreePL.tdelist;

import threepl.codegen.TDE;
import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.QueueRefs;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.exec.WordSpec;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to create a divider/remainder operation.
 *
 * <p>The 1st input argument is the 1st operand.
 *
 * <p>The 2nd input argument is the 2nd operand.
 *
 * <p>The 1st output argument is the quotient.
 *
 * <p>The 2nd ounput argument is the remainder.
 */
public class DivRemProc extends InbuiltProc implements Constant, TDEConstants {

    /**
     * Construct the inbuilt procedure divrem().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public DivRemProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
        ipnames.put("num", 0);
        ipnames.put("denom", 1);
        opnames.put("quot", 0);
        opnames.put("rem", 1);
    }

    /**
     * Execute the memory variable declaration procedure divrem(). This
     * does not generate in-line executable code.
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
        SrcLoc  loc = inargs.getCallLoc();
        if (inargs.size() != 2)
            throw new ExEx("divrem() must have 2 input arguments", loc);
        if (outargs.size() != 2)
            throw new ExEx("divrem() must have 2 output arguments", loc);
        
        Val     aval = inargs.getVal(0);
        Val     bval = inargs.getVal(1);
        Mode    amode = aval.getMode();
        Ptype   atype = aval.getPrimType();
        Mode    bmode = bval.getMode();
        Ptype   btype = bval.getPrimType();
        if (!((amode == Mode.STATIC) || (amode == Mode.VALUE) || (amode == Mode.QUEUE) || (amode == Mode.IMMEDIATE)))
            throw new ExEx("divrem() 1st input argument not allowed mode", loc);
        if ((atype != Ptype.INT) && (atype != Ptype.UINT))
            throw new ExEx("divrem() 1st input argument not integer type", loc);
        if (!((bmode == Mode.STATIC) || (bmode == Mode.VALUE) || (bmode == Mode.QUEUE) || (bmode == Mode.IMMEDIATE)))
            throw new ExEx("divrem() 2nd input argument not allowed mode", loc);
        if ((btype != Ptype.INT) && (btype != Ptype.UINT))
            throw new ExEx("divrem() 2nd input argument not integer type", loc);
        boolean signed =  ((atype == Ptype.INT) || (btype == Ptype.INT));
        aval.resolveClocks(null, Calloc.VALUE, loc);
        bval.resolveClocks(null, Calloc.VALUE, loc);
  
        Ref         qref = outargs.getRef(0, "divrem()"); 
        Ref         rref = outargs.getRef(1, "divrem()"); 
        Mode        qmode = qref.getMode();
        Ptype       qtype = qref.getPrimType();
        WordSpec    qtws = qref.getWordSpec();
        Mode        rmode = rref.getMode();
        Ptype       rtype = rref.getPrimType();
        WordSpec    rtws = rref.getWordSpec();
        if (qmode != Mode.VALUE)
            throw new ExEx("divrem() 1st output argument must be value mode", loc);
        if ((qtype != Ptype.INT) && (qtype != Ptype.UINT))
            throw new ExEx("divrem() 1st output argument not integer type", loc);
        if (rmode != Mode.VALUE)
            throw new ExEx("divrem() 2nd output argument must be value mode", loc);
        if ((rtype != Ptype.INT) && (rtype != Ptype.UINT))
            throw new ExEx("divrem() 2nd output argument not integer type", loc);
        if (signed) {
            if (qtype != Ptype.INT)
                throw new ExEx("divrem() 1st output argument must be type int", loc);
            if (rtype != Ptype.INT)
                throw new ExEx("divrem() 2nd output argument must be type int", loc);
        }
        
        if (aval.getMode() == Mode.IMMEDIATE) {
            if (aval.isN(0)) {
                // division of 0 - output is 0:0
                qref.assignTo(new Val(0, loc), loc);
                rref.assignTo(new Val(0, loc), loc);
                return;
            }
        }

        QueueRefs    queues = new QueueRefs();
        
        if (bval.getMode() == Mode.IMMEDIATE) {
            int     sc;
            TDEVar  atdev = aval.getTDEVar();
            if (bval.isN(0)) {
                // division by 0 - fatal error
                throw new ExEx("divrem() division by zero", loc);
            }
            if (bval.isN(1)) {
                queues.and_set(aval.getQueues(), loc);
                qref.andSetQueues(queues, loc);
                qref.assignTo(aval.cast(qref.getType(), "divrem()", loc), loc);
                rref.assignTo(new Val(0, loc), loc);
                return;
            }
            if ((sc=bval.p2()) != 0) {
                int mask = (1 << sc) - 1;
                TDEVar  qtdev = tdelist.signal("Q", qtws, loc);
                TDEVar  rtdev = tdelist.signal("R", rtws, loc);
                TDE     stde = new TDE(TDEType.OPERATOR);
                TDE     mtde = new TDE(TDEType.OPERATOR);

                stde.add2p(TDEOp.RSH);
                stde.add2p(aval.getPrimType().isSigned());
                stde.add2p(Ptype.UINT.isSigned());
                stde.add2i(atdev);
                stde.add2i(new TDEVar(Long.valueOf(sc), Ptype.UINT, loc));
                stde.add2o(qtdev);
                tdelist.addTDE(stde);

                mtde.add2p(TDEOp.AND);
                mtde.add2p(aval.getPrimType().isSigned());
                mtde.add2p(false);
                mtde.add2i(atdev);
                mtde.add2i(new TDEVar(Long.valueOf(mask), Ptype.UINT, loc));
                mtde.add2o(rtdev);
                tdelist.addTDE(mtde);

                queues.and_set(aval.getQueues(), loc);
                queues.and_set(bval.getQueues(), loc);
                qref.andSetQueues(queues, loc);
                rref.andSetQueues(queues, loc);
                qref.assignTo(new Val(null, Mode.VALUE, qtdev, loc), loc);
                rref.assignTo(new Val(null, Mode.VALUE, rtdev, loc), loc);
                return;
            }
        }

        if (aval.getMode() == Mode.IMMEDIATE)
            aval.toTarget();
        if (bval.getMode() == Mode.IMMEDIATE)
            bval.toTarget();
        
        TDEVar      atdev = aval.getTDEVar();
        TDEVar      btdev = bval.getTDEVar();
        
        TDEVar  qtdev = tdelist.signal("Q", qtws, loc);
        TDEVar  rtdev = tdelist.signal("R", rtws, loc);
        TDE tde = new TDE(TDEType.OPERATOR);
        tde.add2p(TDEOp.DIVREM);
        tde.add2p(aval.getPrimType().isSigned());
        tde.add2p(bval.getPrimType().isSigned());
        tde.add2i(atdev);
        tde.add2i(btdev);
        tde.add2o(qtdev);
        tde.add2o(rtdev);
        tdelist.addTDE(tde);
 
        queues.and_set(aval.getQueues(), loc);
        queues.and_set(bval.getQueues(), loc);
        qref.andSetQueues(queues, loc);
        rref.andSetQueues(queues, loc);
        qref.assignTo(new Val(null, Mode.VALUE, qtdev, loc), loc);
        rref.assignTo(new Val(null, Mode.VALUE, rtdev, loc), loc);
    }
}
