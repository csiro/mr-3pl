package threepl.procs;

import threepl.codegen.TDEConstants;
import threepl.exceptions.ExEx;
import threepl.exec.Ref;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.nodes.ArgParams;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to take two operands and an operator and return
 * the aligned operands and the result type.
 *
 * <p>The 1st and 2nd input arguments are the target operands.
 *
 * <p>The 3rd input argument is the operator as a string - "+", "-",
 * "*" or "/".
 *
 * <p>The 4th input argument is an immediate logical which if true
 * indicates that in the case of "+" the right operand is to be
 * adjusted to match the left operand regardless of relative offsets
 * the aligned operands and the result type.
 *
 * <p>The 1st and 2nd output arguments are the adjusted operands.
 *
 * <p>The 3rd output argument is the result type.
 */
public class ArgAdjustProc extends InbuiltProc implements Constant, TDEConstants {

    /**
     * Construct the inbuilt procedure argadjust().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public ArgAdjustProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = true;
        target_inline = false;
        ipnames.put("in1", 0);
        ipnames.put("in2", 1);
        ipnames.put("op", 2);
        ipnames.put("matchleft", 3);
        opnames.put("out1", 0);
        opnames.put("out2", 1);
        opnames.put("rt", 2);
    }

    /**
     * Execute the procedure argadjust(). This does not generate in_line
     * executable code.
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
        Ref     oref1 = null;
        Ref     oref2 = null;
        Ref     oref3 = null;
        Val     ival1 = null;
        Val     ival2 = null;
        Val     ival3 = null;
        Val     ival4 = null;
        
        if (inargs.size() != 4)
            throw new ExEx("argadjust() must have 4 input arguments", loc);
        if (outargs.size() != 3)
            throw new ExEx("argadjust() must have 3 output arguments", loc);
        
        oref1 = outargs.getRef(0, "argadjust() -");
        if (oref1 != null) {
            if (oref1.getMode() != Mode.VALUE)
                throw new ExEx("argadjust() 1st output argument must be value mode", loc);
            if (oref1.getPrimType() != Ptype.EMPTY)
                throw new ExEx("argadjust() 1st output argument must be type \"empty\"", loc);
        }
        
        oref2 = outargs.getRef(1, "argadjust() -");
        if (oref2 == null)
            throw new ExEx("argadjust() 2nd output argument cannot be null", loc);
        if (oref2.getMode() != Mode.VALUE)
            throw new ExEx("argadjust() 2nd output argument must be value mode", loc);
        if (oref2.getPrimType() != Ptype.EMPTY)
            throw new ExEx("argadjust() 3nd output argument must be type \"empty\"", loc);
        
        oref3 = outargs.getRef(2, "argadjust() -");
        if (oref3 == null)
            throw new ExEx("argadjust() 3rd output argument cannot be null", loc);
        if (oref3.getMode() != Mode.IMMEDIATE)
            throw new ExEx("argadjust() 3rd output argument must be immediate mode", loc);
        if (oref3.getPrimType() != Ptype.TYPE)
            throw new ExEx("argadjust() 3rd output argument must be type \"type\"", loc);

        ival1 = inargs.getVal(0);
        switch (ival1.getMode()) {
        case IMMEDIATE:
        case VALUE:
        case STATIC:
        case QUEUE:
            break;
        default:
            throw new ExEx("argadjust() 1st input argument cannot be mode " +
                            ival1.getMode().name(), loc);
        }
        
        ival2 = inargs.getVal(1);
        switch (ival2.getMode()) {
        case IMMEDIATE:
        case VALUE:
        case STATIC:
        case QUEUE:
            break;
        default:
            throw new ExEx("argadjust() 2nd input argument cannot be mode " +
                            ival2.getMode().name(), loc);
        }
                
        ival3 = inargs.getVal(2);
        if (ival3.getMode() != Mode.IMMEDIATE)
            throw new ExEx("argadjust() 3rd input argument must be immediate mode", loc);      
        if (ival3.getPrimType() != Ptype.STR)
            throw new ExEx("argadjust() 3rd output argument must be type \"str\"", loc);
        
        ival4 = inargs.getVal(3);
        if (ival4.getMode() != Mode.IMMEDIATE)
            throw new ExEx("argadjust() 4th input argument must be immediate mode", loc);      
        if (ival4.getPrimType() != Ptype.LOG)
            throw new ExEx("argadjust() 4th output argument must be type \"log\"", loc);

        TreeOp      treeop;
        TDEOp       tdeop;
        String      sop = ival3.getSingleSval(loc);
        if (sop.equals("+")) {
            treeop = TreeOp.ADD;
            tdeop = TDEOp.ADD;
        } else if (sop.equals("-")) {
            treeop = TreeOp.SUB;
            tdeop = TDEOp.SUB;
        } else if (sop.equals("*")) {
            treeop = TreeOp.MUL;
            tdeop = TDEOp.MUL;
        } else if (sop.equals("/")) {
            treeop = TreeOp.DIV;
            tdeop = TDEOp.DIV;
        } else
            throw new ExEx("argadjust() unknown operator \"" + sop + "\"", loc);
        boolean     forceleft = ival4.getSingleLval(loc);

        ArgParams   ap = new ArgParams(ival1, ival2, treeop, tdeop, forceleft, loc);

        Val ov1 = new Val(null, Mode.VALUE, ap.left_tdevar, loc);
        Val ov2 = new Val(null, Mode.VALUE, ap.right_tdevar, loc);
        ov1.addExecSets(ival1);
        ov1.andSetQueues(ival1.getQueues(), loc);
        ov1.addOVars(ival1);
        ov1.addIVars(ival1);
        ov2.addExecSets(ival2);
        ov2.andSetQueues(ival2.getQueues(), loc);
        ov2.addOVars(ival2);
        ov2.addIVars(ival2);
        if (oref1 != null)
            oref1.assignTo(ov1, loc);
        oref2.assignTo(ov2, loc);
        StringBuffer    sb = new StringBuffer();
        sb.append(ap.result_type.typename());
        sb.append(":");
        sb.append(ap.result_width - ap.result_offset);
        if (ap.result_type.typename().endsWith("fixed")) {
            sb.append(".");
            sb.append(ap.result_offset);
        }
        Type    rtype = new Type(sb.toString(), loc);
        oref3.assignTo(AST.IMASS, new Val(rtype, loc), loc);
    }
}
