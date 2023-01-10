package threepl.nodes;

import static threepl.ThreePL.family;
import static threepl.ThreePL.tdelist;

import java.util.ArrayList;

import threepl.codegen.TDE;
import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Queue;
import threepl.exec.QueueRefs;
import threepl.exec.Ref;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.exec.WordSpec;
import threepl.parser.Constant;
import threepl.parser.Functions;
import threepl.parser.SrcLoc;
import threepl.parser.Token;

/**
 * This class represents an operator code tree node. It handles
 * all operators except the unary operators ++, --, * and ? which
 * are implemented as flags or codes in the VarNode (variable node).
 * {@code ->} is not treated as an operator but rather a separator between
 * subscript/field tokens.
 */
public final class ExprNode extends Node implements Constant, TDEConstants {
    protected TreeOp op;

    /**
     * Construct a unary code tree node.
     * @param   op is the operator
     * @param   n is the argument node
     * @param   t is the operator token, from which the file source location
     *          is extracted
     */
    @SuppressWarnings("incomplete-switch")
    public ExprNode (TreeOp op, Node n, Token t) {
        super(t);
/*
        if ((op == TreeOp.BITNOT) && (n instanceof ExprNode)) {
            switch (((ExprNode)n).op) {
            case BITAND:
                this.op = TreeOp.BITNAND;
                addSubNode(n.getSubNode(0));
                addSubNode(n.getSubNode(1));
                return;
            case BITOR:
                this.op = TreeOp.BITNOR;
                addSubNode(n.getSubNode(0));
                addSubNode(n.getSubNode(1));
                return;
            case BITXOR:
                this.op = TreeOp.BITXNOR;
                addSubNode(n.getSubNode(0));
                addSubNode(n.getSubNode(1));
                return;
            }
        }
*/
        this.op = op;
        addSubNode(n);
    }

    /**
     * Construct a binary code tree node.
     * @param   op is the operator
     * @param   n1 is the left argument node
     * @param   n2 is the right argument node
     * @param   t is the operator token, from which the file source location
     *          is extracted
     */
    public ExprNode (TreeOp op, Node n1, Node n2, Token t) {
        super(t);
/*
        switch (op) {
        case BITAND:
            if ((n1 instanceof ExprNode) && (((ExprNode)n1).op == TreeOp.BITNOT)) {
                this.op = TreeOp._BITAND;
                addSubNode(n1.getSubNode(0));
                addSubNode(n2);
                return;
            } else if ((n2 instanceof ExprNode) && (((ExprNode)n2).op == TreeOp.BITNOT)) {
                this.op = TreeOp.BITAND_;
                addSubNode(n1);
                addSubNode(n2.getSubNode(0));
                return;
            }
            break;
        case BITOR:
            if ((n1 instanceof ExprNode) && (((ExprNode)n1).op == TreeOp.BITNOT)) {
                this.op = TreeOp._BITOR;
                addSubNode(n1.getSubNode(0));
                addSubNode(n2);
                return;
            } else if ((n2 instanceof ExprNode) && (((ExprNode)n2).op == TreeOp.BITNOT)) {
                this.op = TreeOp.BITOR_;
                addSubNode(n1);
                addSubNode(n2.getSubNode(0));
                return;
            }
        }
*/
        this.op = op;
        addSubNode(n1);
        addSubNode(n2);
    }

    /**
     * Construct a ternary code tree node.
     * @param   op is the operator
     * @param   n1 is the 1st argument node
     * @param   n2 is the 2nd argument node
     * @param   n3 is the 3rd argument node
     * @param   t is the operator token, from which the file source location
     *          is extracted
     */
    public ExprNode (TreeOp op, Node n1, Node n2, Node n3, Token t) {
        super(t);
        this.op = op;
        addSubNode(n1);
        addSubNode(n2);
        addSubNode(n3);
    }

    /**
     * Get the value from an expression operator.
     * For immediate arguments a single constant <b>Val</b> is returned.
     * If one or more arguments are target variables or expressions
     * containing target variables, a TDEVar <b>Val</b> will be returned.
     *
     * <p>There are two cases where type conversion occurs -
     * <ol>
     * <li>The '+' operator will accept string operands, performing
     * concatenation. If one operand is an int, uint or log it will be
     * converted to a string
     * <li>Arithmetic binary operators accept mixed int-uint pairs,
     * converting the uint to int. For target operands the int-uint
     * type mixing is handled by the low-level code generator.
     * </ol>
     *
     * <p>For immediate operands int and uint are treated the same, the
     * immediate uint being implemented as a long but restricted to 63
     * bits so that it is always positive as signed 64bit.
     *
     * <p>For target operands the types are passed through to the TDE
     * and then on to the low level code generator. Allowed types (and
     * mixtures of types) must be handled there.
     * 
     * <p>The width of an arithmetic operation is such as to
     * maintain precision in the result. This may be truncated
     * on later assignment.
     * @return  the value
     */
    public Val getVal () {
        Val     a1 = null;
        Val     a2 = null;
        Val     a3 = null;
        Val     retval = null;
        
        Node    n = subnodes.getNode(0);
        if (n == null) {
            switch (subnodes.size()) {
            case 1:
                throw new ExEx("unary operator has null operand", this);
            case 2:
                throw new ExEx("binary operator has null 1st operand", this);
            case 3:
                throw new ExEx("?: operator has null test operand", this);
            }
        }
        
        if (op == TreeOp.WAV) {
            // Handle write availability operator here to avoid
            // getting the argument value since that would set the
            // queue variable output clock
            Node    wavn = subnodes.getNode(0);
            Ref     ref = wavn.getRef("write availability - ");
            retval = ref.getWriteAvailVal();
            if (ref.getMode() != Mode.QUEUE)
                throw new ExEx("unary operator > applied to non-queue variable", this);
            Queue   qv = (Queue)ref.getVar();
            retval.setSrcLoc(loc);
            retval.addOVar(qv.getQueueWriteClkVar());
            retval.addIVar(qv);
            QueueRefs    queues = new QueueRefs();
            queues.unbufferedQueues(true, "> unary operator used on unbuffered queue", loc);
            queues.setWriteAVCheck(ref.getQueues());
            retval.setQueues(queues);
            return(retval);
        }
        
        a1 = n.getVal();
        if (!a1.isPrimitive() && (op != TreeOp.EXAM) && (op != TreeOp.RAV)
                               && (op != TreeOp.EQ) && (op != TreeOp.NE)) {
            switch (subnodes.size()) {
            case 1:
                throw new ExEx("unary operand is not primitive", this);
            case 2:
                throw new ExEx("binary 1st operand is not primitive", this);
            case 3:
                throw new ExEx("ternary test operand is not primitive", this);
            }
        }

        // special case of logical AND with immediate 1st operand
        // which is false - short-circuit evaluation - return false
        if ((subnodes.size() == 2) && (op == TreeOp.LOGAND) &&
            !a1.isTarget() && !a1.getSingleLval(loc))
            return(new Val(false, loc));

        // special case of logical OR with immediate 1st operand
        // which is true - short-circuit evaluation - return true
        if ((subnodes.size() == 2) && (op == TreeOp.LOGOR) &&
            !a1.isTarget() && a1.getSingleLval(loc))
            return(new Val(true, loc));

        // special case of ?: with immediate 1st operand
        // handle here to avoid generating unused target code
        if ((subnodes.size() == 3) && (op == TreeOp.CONDIT) && !a1.isTarget()) {
            //test = arg;
            //test_is_target = arg_is_target;
            typeCHECK(op, a1, " left", loc, Ptype.LOG);
            retval = subnodes.getNode(a1.getSingleLval(loc) ? 1 : 2).getVal();
            retval.setSrcLoc(loc);
            return(retval);
        }

        // check other operands
        // get other operand modes
        if (subnodes.size() == 2) {
            Node    nr = subnodes.getNode(1);
            if (nr == null)
                throw new ExEx("binary operator has null 2nd operand", this);
            a2 = nr.getVal();
            if (!a2.isPrimitive() && (op != TreeOp.EQ) && (op != TreeOp.NE))
                throw new ExEx("binary 2nd operand is not primitive", this);
        } else if (subnodes.size() == 3) {
            a2 = subnodes.getNode(1).getVal();
            a3 = subnodes.getNode(2).getVal();
        }
        
        return(evaluate(op, a1, a2, a3, loc));
    }

    public static Val evaluate (TreeOp op, Val a1, SrcLoc loc) {
        return(evaluate(op, a1, null, null, loc));
    }

    public static Val evaluate (TreeOp op, Val a1, Val a2, SrcLoc loc) {
        return(evaluate(op, a1, a2, null, loc));
    }

    @SuppressWarnings("incomplete-switch")
    public static Val evaluate (TreeOp op, Val a1, Val a2, Val a3, SrcLoc loc) {
        Ptype   a1_type = a1.getPrimType();
        Ptype   a2_type = (a2 != null) ? a2.getPrimType() : Ptype.NONE;
        boolean a1_is_target = a1.isTarget();
        boolean a2_is_target = (a2 != null) ? a2.isTarget() : false;
        boolean typeok = false;
        Val     retval = null;

        switch (op) {
        case ADD:
            if (a1_is_target || a2_is_target) {
                if (a1_is_target && !a2_is_target && 
                    a1.typeOK(loc, Ptype.FIXED, Ptype.UFIXED) &&
                    a2.typeOK(loc, Ptype.FLOAT)) {
                    // target UFIXED/FIXED + const FLOAT
                    // convert float to integer constant with offset
                    a2.toTarget(a1);
                    typeok = true;
                }
                if (!a1_is_target && a2_is_target && 
                    a1.typeOK(loc, Ptype.FLOAT) &&
                    a2.typeOK(loc, Ptype.FIXED, Ptype.UFIXED)) {
                    // const FLOAT + target UFIXED/FIXED
                    // convert float to integer constant with offset
                    a1.toTarget(a2);
                    typeok = true;
                }
                if (!typeok)
                    typeCHECK(op, a1, a2, loc, Ptype.INT, Ptype.UINT, Ptype.FIXED, Ptype.UFIXED);
                if (a1.isN(0))
                    return(a2);  // left operand 0 - return right operand
                else if (a2.isN(0))
                    return(a1);  // right operand 0 - return left operand
                else
                    return(target_bin(a1, a2, op, TDEOp.ADD, loc));
            } else {
                if ((a1_type == Ptype.STR) || (a2_type == Ptype.STR)) {
                    // is a string concatenation
                    // convert type if necessary
                    typeCHECK(op, a1, a2, loc, Ptype.INT, Ptype.UINT, Ptype.STR, Ptype.LOG, Ptype.FLOAT);
                    retval = new Val(String.valueOf(a1.getVal(0)) + String.valueOf(a2.getVal(0)), loc);
                } else if ((a1_type == Ptype.FLOAT) || (a2_type == Ptype.FLOAT)) {
                    if (a1_type != Ptype.FLOAT)
                        a1 = a1.cast(new Type(Ptype.FLOAT, 0), "", loc);
                    if (a2_type != Ptype.FLOAT)
                        a2 = a2.cast(new Type(Ptype.FLOAT, 0), "", loc);
                    retval = new Val(a1.getSingleFval(loc) + a2.getSingleFval(loc), loc);
                } else {
                    typeCHECK(op, a1, " left", loc, Ptype.INT, Ptype.UINT);
                    typeCHECK(op, a2, " right", loc, Ptype.INT, Ptype.UINT);
                    // must be integer addition
                    retval = new Val(a1.getSingleIval(loc) + a2.getSingleIval(loc), loc);
                }
            }
            break;
        case SUB:
            if (a1_is_target || a2_is_target) {
                if (a1_is_target && !a2_is_target && 
                    a1.typeOK(loc, Ptype.FIXED, Ptype.UFIXED) &&
                    a2.typeOK(loc, Ptype.FLOAT)) {
                    // target UFIXED/FIXED + const FLOAT
                    // convert float to integer constant with offset
                    a2.toTarget(a1);
                    typeok = true;
                }
                if (!a1_is_target && a2_is_target && 
                    a1.typeOK(loc, Ptype.FLOAT) &&
                    a2.typeOK(loc, Ptype.FIXED, Ptype.UFIXED)) {
                    // const FLOAT + target UFIXED/FIXED
                    // convert float to integer constant with offset
                    a1.toTarget(a2);
                    typeok = true;
                }
                if (!typeok)
                    typeCHECK(op, a1, a2, loc, Ptype.INT, Ptype.UINT, Ptype.FIXED, Ptype.UFIXED);
                if (a2.isN(0))
                    return(a1);  // right operand 0 - return left operand
                else
                    return(target_bin(a1, a2, op, TDEOp.SUB, loc));
            } else {
                typeCHECK(op, a1, a2, loc, Ptype.INT, Ptype.UINT, Ptype.FLOAT);
                if ((a1_type == Ptype.FLOAT) || (a2_type == Ptype.FLOAT)) {
                    if (a1_type != Ptype.FLOAT)
                        a1 = a1.cast(new Type(Ptype.FLOAT, 0), "", loc);
                    if (a2_type != Ptype.FLOAT)
                        a2 = a2.cast(new Type(Ptype.FLOAT, 0), "", loc);
                    retval = new Val(a1.getSingleFval(loc) - a2.getSingleFval(loc), loc);
                } else
                    retval = new Val(a1.getSingleIval(loc) - a2.getSingleIval(loc), loc);
            }
            break;
        case MUL:
            if (a1_is_target || a2_is_target) {
                if (family.MultWidth1() == 0)
                    throw new ExEx("Multiplication is not implemented for this device", loc);
                if (a1_is_target && !a2_is_target && 
                    a1.typeOK(loc, Ptype.FIXED, Ptype.UFIXED) &&
                    a2.typeOK(loc, Ptype.FLOAT)) {
                    // target UFIXED/FIXED * const FLOAT
                    // convert float to integer constant with offset
                    a2.toTarget(a1);
                    typeok = true;
                }
                if (!a1_is_target && a2_is_target && 
                    a1.typeOK(loc, Ptype.FLOAT) &&
                    a2.typeOK(loc, Ptype.FIXED, Ptype.UFIXED)) {
                    // const FLOAT * target UFIXED/FIXED
                    // convert float to integer constant with offset
                    a1.toTarget(a2);
                    typeok = true;
                }
                if (!typeok)
                    typeCHECK(op, a1, a2, loc, Ptype.INT, Ptype.UINT, Ptype.FIXED, Ptype.UFIXED);
                int     sc;
                if (a1.isImedOrTargConst() || a2.isImedOrTargConst()) {
                    // If left operand is constant swap operands so right one
                    // is the constant.
                    if (a1.isImedOrTargConst()) {
                        Val temp = a1;
                        a1 = a2;
                        a2 = temp;
                    }
                    if (a2.isN(0))
                        // multiplication by 0 - return the zero (right operand)
                        return(a2);
                    else if (a2.isN(1))
                        // multiplication by 1 - return the left operand
                        return(a1);
                    else if (a2.isN(-1)) {
                        // multiplication by -1 - return minus a1
                        return(target_un(a1, TDEOp.NEG, loc));
                    } else if ((sc=a2.p2()) > 0) {
                        //multiplication by power of 2 - use left shift
                        if (sc < 0)
                            return(target_bin(a1, new Val(-sc, loc), TreeOp.DIV, TDEOp.RSH, loc));
                        else
                            return(target_bin(a1, new Val(sc, loc), op, TDEOp.LSH, loc));
                    } else
                        // multiply by the constant
                        return(target_bin(a1, a2, op, TDEOp.MUL, loc));
                } else
                    return(target_bin(a1, a2, op, TDEOp.MUL, loc));
            } else {
                typeCHECK(op, a1, a2, loc, Ptype.INT, Ptype.UINT, Ptype.FLOAT);
                if ((a1_type == Ptype.FLOAT) || (a2_type == Ptype.FLOAT)) {
                    if (a1_type != Ptype.FLOAT)
                        a1 = a1.cast(new Type(Ptype.FLOAT, 0), "", loc);
                    if (a2_type != Ptype.FLOAT)
                        a2 = a2.cast(new Type(Ptype.FLOAT, 0), "", loc);
                    retval = new Val(a1.getSingleFval(loc) * a2.getSingleFval(loc), loc);
                } else
                    retval = new Val(a1.getSingleIval(loc) * a2.getSingleIval(loc), loc);
            }
            break;
        case DIV:
            if (a1_is_target || a2_is_target) {
                if (family.needFDRSEpatch())
                    throw new ExEx("Division is not implemented for this device", loc);
                typeCHECK(op, a1, a2, loc, Ptype.INT, Ptype.UINT, Ptype.FIXED, Ptype.UFIXED);
                if (a1.isImedOrTargConst() || a2.isImedOrTargConst()) {
                    int     sc;
                    if (a1.isN(0))
                        // division of zero - return the dividend (zero)
                        return(a1);
                    if (a2.isN(0))
                        // division by 0 - error
                        throw new ExEx("divide by 0", loc);
                    if (a2.isN(1))
                        // division by 1 - return dividend
                        return(a1);
                    if ((sc=a2.p2()) != 0) {
                        //division by power of 2 - just change offset
                        if (sc < 0)
                            return(target_bin(a1, new Val(-sc, loc), TreeOp.MUL, TDEOp.LSH, loc));
                        else
                            return(target_bin(a1, new Val(sc, loc), op, TDEOp.RSH, loc));
                    } else
                        // divide using the constant
                        return(target_bin(a1, a2, op, TDEOp.DIV, loc));
               } else
                    return(target_bin(a1, a2, op, TDEOp.DIV, loc));
            } else {
                typeCHECK(op, a1, a2, loc, Ptype.INT, Ptype.UINT, Ptype.FLOAT);
                if ((a1_type == Ptype.FLOAT) || (a2_type == Ptype.FLOAT)) {
                    if (a1_type != Ptype.FLOAT)
                        a1 = a1.cast(new Type(Ptype.FLOAT, 0), "", loc);
                    if (a2_type != Ptype.FLOAT)
                        a2 = a2.cast(new Type(Ptype.FLOAT, 0), "", loc);
                    retval = new Val(a1.getSingleFval(loc) / a2.getSingleFval(loc), loc);
                } else {
                    long    den = a2.getSingleIval(loc);
                    if (den == 0)
                        throw new ExEx("divide by 0", loc);
                    retval = new Val(a1.getSingleIval(loc) / den, loc);
                }
            }
            break;
        case REM:
            typeCHECK(op, a1, a2, loc, Ptype.INT, Ptype.UINT);
            if (a1_is_target || a2_is_target) {
                if (family.needFDRSEpatch())
                    throw new ExEx("Remainder is not implemented for this device", loc);
                int     sc;
                if (a1_is_target && a2_is_target)
                    return(target_bin(a1, a2, op, TDEOp.REM, loc));
                if (a1.isN(0))
                    // division of zero - return the dividend (zero)
                    return(a1);
                if (a2.isN(0))
                    // division by 0 - error
                    throw new ExEx("divide by 0", loc);
                if (a2.isN(1))
                    // division by 1 - return zero
                    return(new Val(0, loc));
                if ((sc=a2.p2()) != 0)
                    // remainder by power of 2 - AND a mask
                    return(target_bin(a1, new Val((1<<sc)-1, loc), op, TDEOp.AND, loc));
                else
                    // remainder using the constant
                    return(target_bin(a1, a2, op, TDEOp.REM, loc));
            } else
                retval = new Val(a1.getSingleIval(loc) % a2.getSingleIval(loc), loc);
            break;
        case MIN:
            if (a1_is_target) {
                typeCHECK(op, a1, "", loc, Ptype.INT, Ptype.UINT, Ptype.FIXED, Ptype.UFIXED);
                return(target_un(a1, TDEOp.NEG, loc));
                /*
                switch (a1.getTDEVar().getType()) {
                case UINT:
                case INT:
                case UFIXED:
                case FIXED:
                    // For a constant just apply the unary operator.
                    return(target_un(a1, TDEOp.NEG, loc));
                default:
                    // For a variable create 0-v to allow for possible ALU
                    // optimisation (it may be this is never used).
                    // To create a zero Val that is INT rather than UINT create
                    // -1 first since that is signed, then overwrite the value
                    // with zero. If created with 0 it will be UINT regardless
                    // of the type argument!
                    
                    // THIS DOESN'T WORK - THE ZERO IS STILL UNISIGNED!
                    TDEVar  zerotdev = new TDEVar(Long.valueOf(0), Ptype.INT, loc);
                    zerotdev.setVal(Long.valueOf(0));
                    Val     zeroval = new Val(null, Mode.VALUE, zerotdev, loc);
                    retval = target_bin(zeroval, a1, TreeOp.SUB, TDEOp.SUB, 0, loc);
                    return(retval);
                }
                */
            } else {
                typeCHECK(op, a1, "", loc, Ptype.INT, Ptype.UINT, Ptype.FLOAT);
                if (a1_type == Ptype.FLOAT)
                    retval = new Val(-a1.getSingleFval(loc), loc);
                else
                    retval = new Val(-a1.getSingleIval(loc), loc);
            }
            break;
        case BITNOT:
            typeCHECK(op, a1, "", loc, Ptype.INT, Ptype.UINT, Ptype.BITS, Ptype.FIXED, Ptype.UFIXED);
            if (a1_is_target)
                return(target_un(a1, TDEOp.INV, loc));
            else
                retval = new Val(~(a1.getSingleIval(loc)), loc);
            break;
        case LOGNOT:
            typeCHECK(op, a1, "", loc, Ptype.LOG);
            if (a1_is_target)
                return(target_un(a1, TDEOp.INV, loc));
            else
                retval = new Val(!a1.getSingleLval(loc), loc);
            break;
        case LS:
            typeCHECK(op, a1, " left", loc, Ptype.BITS, Ptype.INT, Ptype.UINT, Ptype.FIXED, Ptype.UFIXED);
            if (a1_is_target || a2_is_target) {
                if (a2_is_target && family.needFDRSEpatch())
                    throw new ExEx("Variable left shift is not implemented for this device", loc);
                typeCHECK(op, a2, " right", loc, Ptype.UINT);
                return(target_bin(a1, a2, op, TDEOp.LSH, loc));
            } else {
                typeCHECK(op, a2, " right", loc, Ptype.INT, Ptype.UINT);
                long s = a2.getSingleIval(loc);
                if (s < 0)
                    throw new ExEx("-ve shift value", loc);
                if (s > 64)
                    throw new ExEx("shift value > 64", loc);
                retval = new Val(a1.getSingleIval(loc) << s, loc);
           }
            break;
        case RS:
            typeCHECK(op, a1, " left", loc, Ptype.BITS, Ptype.INT, Ptype.UINT, Ptype.FIXED, Ptype.UFIXED);
            if (a1_is_target || a2_is_target) {
                if (a2_is_target && family.needFDRSEpatch())
                    throw new ExEx("Variable right shift is not implemented for this device", loc);
                typeCHECK(op, a2, " right", loc, Ptype.UINT);
                return(target_bin(a1, a2, op, TDEOp.RSH, loc));
            } else {
                typeCHECK(op, a2, " right", loc, Ptype.INT, Ptype.UINT);
                long s = a2.getSingleIval(loc);
                if (s < 0)
                    throw new ExEx("-ve shift value", loc);
                if (s > 64)
                    throw new ExEx("shift value > 64", loc);
                retval = new Val(a1.getSingleIval(loc) >> s, loc);
            }
            break;
        case LR:
            if (!a1.isTarget())
                throw new ExEx("left rotate left operand is not target mode");
            if (a2.isTarget())
                throw new ExEx("left rotate right operand is not immediate mode");
            return(target_bin(a1, a2, op, TDEOp.LROT, loc));
        case RR:
            if (!a1.isTarget())
                throw new ExEx("right rotate left operand is not target mode");
            if (a2.isTarget())
                throw new ExEx("right rotate right operand is not immediate mode");
            return(target_bin(a1, a2, op, TDEOp.RROT, loc));
        case LT:
            if (a1_is_target || a2_is_target) {
                typeCHECK(op, a1, a2, loc, Ptype.INT, Ptype.UINT, Ptype.FIXED, Ptype.UFIXED);
                if (!a1_is_target)
                    check_constant_size(a1, a2, "< operator", loc);
                if (!a2_is_target)
                    check_constant_size(a2, a1, "< operator", loc);
                return(target_bin(a1, a2, op, TDEOp.LT, loc));
            } else {
                typeCHECK(op, a1, a2, loc, Ptype.INT, Ptype.UINT, Ptype.STR, Ptype.FLOAT);
                if ((a1_type == Ptype.FLOAT) || (a2_type == Ptype.FLOAT)) {
                    if (a1_type != Ptype.FLOAT)
                        a1 = a1.cast(new Type(Ptype.FLOAT, 0), "", loc);
                    if (a2_type != Ptype.FLOAT)
                        a2 = a2.cast(new Type(Ptype.FLOAT, 0), "", loc);
                    retval = new Val(a1.getSingleFval(loc) < a2.getSingleFval(loc), loc);
                } else if ((a1_type == Ptype.STR) || (a2_type == Ptype.STR)) {
                    if (a1_type != Ptype.STR)
                        a1 = a1.cast(new Type(Ptype.STR, 0), "", loc);
                    if (a2_type != Ptype.STR)
                        a2 = a2.cast(new Type(Ptype.STR, 0), "", loc);
                    int cv = a1.getSingleSval(loc).compareTo(a2.getSingleSval(loc));
                    retval = new Val(cv < 0, loc);
                } else
                    retval = new Val(a1.getSingleIval(loc) < a2.getSingleIval(loc), loc);
            }
            break;
        case GT:
            if (a1_is_target || a2_is_target) {
                typeCHECK(op, a1, a2, loc, Ptype.INT, Ptype.UINT, Ptype.FIXED, Ptype.UFIXED);
                if (!a1_is_target)
                    check_constant_size(a1, a2, "> operator", loc);
                if (!a2_is_target)
                    check_constant_size(a2, a1, "> operator", loc);
                return(target_bin(a1, a2, op, TDEOp.GT, loc));
            } else {
                typeCHECK(op, a1, a2, loc, Ptype.INT, Ptype.UINT, Ptype.STR, Ptype.FLOAT);
                if ((a1_type == Ptype.FLOAT) || (a2_type == Ptype.FLOAT)) {
                    if (a1_type != Ptype.FLOAT)
                        a1 = a1.cast(new Type(Ptype.FLOAT, 0), "", loc);
                    if (a2_type != Ptype.FLOAT)
                        a2 = a2.cast(new Type(Ptype.FLOAT, 0), "", loc);
                    retval = new Val(a1.getSingleFval(loc) > a2.getSingleFval(loc), loc);
                } else if ((a1_type == Ptype.STR) || (a2_type == Ptype.STR)) {
                    if (a1_type != Ptype.STR)
                        a1 = a1.cast(new Type(Ptype.STR, 0), "", loc);
                    if (a2_type != Ptype.STR)
                        a2 = a2.cast(new Type(Ptype.STR, 0), "", loc);
                    int cv = a1.getSingleSval(loc).compareTo(a2.getSingleSval(loc));
                    retval = new Val(cv > 0, loc);
                } else
                    retval = new Val(a1.getSingleIval(loc) > a2.getSingleIval(loc), loc);
                }
            break;
        case LE:
            if (a1_is_target || a2_is_target) {
                typeCHECK(op, a1, a2, loc, Ptype.INT, Ptype.UINT, Ptype.FIXED, Ptype.UFIXED);
                if (!a1_is_target)
                    check_constant_size(a1, a2, "<= operator", loc);
                if (!a2_is_target)
                    check_constant_size(a2, a1, "<= operator", loc);
                return(target_bin(a1, a2, op, TDEOp.LE, loc));
            } else {
                typeCHECK(op, a1, a2, loc, Ptype.INT, Ptype.UINT, Ptype.STR, Ptype.FLOAT);
                if ((a1_type == Ptype.FLOAT) || (a2_type == Ptype.FLOAT)) {
                    if (a1_type != Ptype.FLOAT)
                        a1 = a1.cast(new Type(Ptype.FLOAT, 0), "", loc);
                    if (a2_type != Ptype.FLOAT)
                        a2 = a2.cast(new Type(Ptype.FLOAT, 0), "", loc);
                    retval = new Val(a1.getSingleFval(loc) <= a2.getSingleFval(loc), loc);
                } else if ((a1_type == Ptype.STR) || (a2_type == Ptype.STR)) {
                    if (a1_type != Ptype.STR)
                        a1 = a1.cast(new Type(Ptype.STR, 0), "", loc);
                    if (a2_type != Ptype.STR)
                        a2 = a2.cast(new Type(Ptype.STR, 0), "", loc);
                    int cv = a1.getSingleSval(loc).compareTo(a2.getSingleSval(loc));
                    retval = new Val(cv <= 0, loc);
                } else
                    retval = new Val(a1.getSingleIval(loc) <= a2.getSingleIval(loc), loc);
            }
            break;
        case GE:
            if (a1_is_target || a2_is_target) {
                typeCHECK(op, a1, a2, loc, Ptype.INT, Ptype.UINT, Ptype.FIXED, Ptype.UFIXED);
                if (!a1_is_target)
                    check_constant_size(a1, a2, ">= operator", loc);
                if (!a2_is_target)
                    check_constant_size(a2, a1, ">= operator", loc);
                return(target_bin(a1, a2, op, TDEOp.GE, loc));
            } else {
                typeCHECK(op, a1, a2, loc, Ptype.INT, Ptype.UINT, Ptype.FLOAT);
                if ((a1_type == Ptype.FLOAT) || (a2_type == Ptype.FLOAT)) {
                    if (a1_type != Ptype.FLOAT)
                        a1 = a1.cast(new Type(Ptype.FLOAT, 0), "", loc);
                    if (a2_type != Ptype.FLOAT)
                        a2 = a2.cast(new Type(Ptype.FLOAT, 0), "", loc);
                    retval = new Val(a1.getSingleFval(loc) >= a2.getSingleFval(loc), loc);
                } else if ((a1_type == Ptype.STR) || (a2_type == Ptype.STR)) {
                    if (a1_type != Ptype.STR)
                        a1 = a1.cast(new Type(Ptype.STR, 0), "", loc);
                    if (a2_type != Ptype.STR)
                        a2 = a2.cast(new Type(Ptype.STR, 0), "", loc);
                    int cv = a1.getSingleSval(loc).compareTo(a2.getSingleSval(loc));
                    retval = new Val(cv >= 0, loc);
                } else
                    retval = new Val(a1.getSingleIval(loc) >= a2.getSingleIval(loc), loc);
            }
            break;
        case EQ:
            // special case for clocks
            if ((a1.getMode() == Mode.CLOCK) && (a2.getMode() == Mode.CLOCK)) {
                retval = new Val(a1.getTDEVar().equals(a2.getTDEVar()), loc);
                retval.setSrcLoc(loc);
                return(retval);
            }
            if ((a1.getMode() == Mode.CLOCK) && (a2_type == Ptype.NULL) ||
                (a1_type == Ptype.NULL) && (a2.getMode() == Mode.CLOCK))
                return(new Val(false, loc));
            if ((a1_type == Ptype.NULL) && (a2_type == Ptype.NULL))
                return(new Val(true, loc));
            if (((a1_type == Ptype.NULL) || (a2_type == Ptype.NULL)) && (a1_type != Ptype.PTR) && (a2_type != Ptype.PTR))
                return(new Val((a1_type == Ptype.NULL) && (a2_type == Ptype.NULL), loc));

            if (a1_is_target || a2_is_target) {
                if (!a1.typeOK(a2, loc, Ptype.BITS, Ptype.INT, Ptype.UINT, Ptype.FIXED, Ptype.UFIXED) &&
                    !a1.typeOK(a2, loc, Ptype.ENUM) &&
                    !a1.typeOK(a2, loc, Ptype.LOG))
                    typeERR(op, a1_type, a2_type, loc);
                if (!a1_is_target)
                    check_constant_size(a1, a2, "== operator", loc);
                if (!a2_is_target)
                    check_constant_size(a2, a1, "== operator", loc);
                return(target_bin(a1, a2, op, TDEOp.EQ, loc));
            } else {
                if (!a1.typeOK(a2, loc, Ptype.BITS, Ptype.INT, Ptype.UINT) &&
                    !a1.typeOK(a2, loc, Ptype.STR) &&
                    !a1.typeOK(a2, loc, Ptype.PTR, Ptype.NULL) &&
                    !a1.typeOK(a2, loc, Ptype.ENUM) &&
                    !a1.typeOK(a2, loc, Ptype.TYPE, Ptype.NONE) &&
                    !a1.typeOK(a2, loc, Ptype.LOG) &&
                    !a1.typeOK(a2, loc, Ptype.FLOAT, Ptype.INT, Ptype.UINT) &&
                    !a1.typeOK(a2, loc, Ptype.MAP))
                    typeERR(op, a1_type, a2_type, loc);
                if (a1_type == Ptype.ENUM)
                    return(new Val(a1.enumOrd(loc) == a2.enumOrd(loc), loc));
                if (a1_type == Ptype.STR) {
                    String  sl = a1.getSingleSval(loc);
                    String  sr = a2.getSingleSval(loc);
                    return(new Val(sl.equals(sr), loc));
                }
                if (a1_type == Ptype.LOG)
                    return(new Val(a1.getSingleLval(loc) == a2.getSingleLval(loc), loc));
                if (((a1_type == Ptype.TYPE) || (a1_type == Ptype.NONE)) &&
                    ((a2_type == Ptype.TYPE) || (a2_type == Ptype.NONE)))
                    return(new Val(a1.getSingleTval(loc).isEqual(a2.getSingleTval(loc), true), loc));
                if (((a1_type == Ptype.PTR) || (a1_type == Ptype.NULL)) && ((a2_type == Ptype.PTR) || (a2_type == Ptype.NULL)))
                    return(new Val(ptreq(a1, a2, loc), loc));
                if ((a1_type == Ptype.FLOAT) || (a2_type == Ptype.FLOAT)) {
                    if (a1_type != Ptype.FLOAT)
                        a1 = a1.cast(new Type(Ptype.FLOAT, 0), "", loc);
                    if (a2_type != Ptype.FLOAT)
                        a2 = a2.cast(new Type(Ptype.FLOAT, 0), "", loc);
                    return(new Val(a1.getSingleFval(loc) == a2.getSingleFval(loc), loc));
                }

                if ((a1_type == Ptype.LOG) && (a2_type == Ptype.LOG))
                    return(new Val(a1.getSingleLval(loc) == a2.getSingleLval(loc), loc));
                
                if (a1.typeOK(a2, loc, Ptype.MAP))
                    return(new Val(a1.getSingleMAPval(loc).equals(a2.getSingleMAPval(loc)), loc));
                retval = new Val(a1.getSingleIval(loc) == a2.getSingleIval(loc), loc);
            }
            break;
        case NE:
            // special case for clocks
            if ((a1.getMode() == Mode.CLOCK) && (a2.getMode() == Mode.CLOCK)) {
                retval = new Val(!a1.getTDEVar().getId().equals(a2.getTDEVar().getId()), loc);
                retval.setSrcLoc(loc);
                return(retval);
            }
            if ((a1.getMode() == Mode.CLOCK) && (a2_type == Ptype.NULL) ||
                (a1_type == Ptype.NULL) && (a2.getMode() == Mode.CLOCK))
                return(new Val(true, loc));
            if ((a1_type == Ptype.NULL) && (a2_type == Ptype.NULL))
                return(new Val(false, loc));
            if (((a1_type == Ptype.NULL) || (a2_type == Ptype.NULL)) && (a1_type != Ptype.PTR) && (a2_type != Ptype.PTR))
                return(new Val((a1_type != Ptype.NULL) || (a2_type != Ptype.NULL), loc));

            if ((a1_type == Ptype.STR) && (a2_type == Ptype.STR)) {
                String  sl = a1.getSingleSval(loc);
                String  sr = a2.getSingleSval(loc);
                return(new Val(!(sl.equals(sr)), loc));
            }
            if (a1_is_target || a2_is_target) {
                if (!a1.typeOK(a2, loc, Ptype.BITS, Ptype.INT, Ptype.UINT, Ptype.FIXED, Ptype.UFIXED) &&
                    !a1.typeOK(a2, loc, Ptype.ENUM) &&
                    !a1.typeOK(a2, loc, Ptype.LOG))
                    typeERR(op, a1_type, a2_type, loc);
                if (!a1_is_target)
                    check_constant_size(a1, a2, "!= operator", loc);
                if (!a2_is_target)
                    check_constant_size(a2, a1, "!= operator", loc);
                return(target_bin(a1, a2, op, TDEOp.NE, loc));
            } else {
                if (!a1.typeOK(a2, loc, Ptype.BITS, Ptype.INT, Ptype.UINT) &&
                    !a1.typeOK(a2, loc, Ptype.STR) &&
                    !a1.typeOK(a2, loc, Ptype.PTR, Ptype.NULL) &&
                    !a1.typeOK(a2, loc, Ptype.ENUM) &&
                    !a1.typeOK(a2, loc, Ptype.TYPE, Ptype.NONE) &&
                    !a1.typeOK(a2, loc, Ptype.LOG) &&
                    !a1.typeOK(a2, loc, Ptype.FLOAT, Ptype.INT, Ptype.UINT))
                    typeERR(op, a1_type, a2_type, loc);

                if (((a1_type == Ptype.TYPE) || (a1_type == Ptype.NONE)) &&
                    ((a2_type == Ptype.TYPE) || (a2_type == Ptype.NONE)))
                    return(new Val(!a1.getSingleTval(loc).isEqual(a2.getSingleTval(loc), true), loc));

                if (((a1_type == Ptype.PTR) || (a1_type == Ptype.NULL)) && ((a2_type == Ptype.PTR) || (a2_type == Ptype.NULL)))
                    return(new Val(!ptreq(a1, a2, loc), loc));

                if ((a1_type == Ptype.ENUM) && (a2_type == Ptype.ENUM))
                    return(new Val(a1.enumOrd(loc) != a2.enumOrd(loc), loc));

                if ((a1_type == Ptype.LOG) && (a2_type == Ptype.LOG))
                    return(new Val(a1.getSingleLval(loc) != a2.getSingleLval(loc), loc));
                
                if ((a1_type == Ptype.FLOAT) || (a2_type == Ptype.FLOAT)) {
                    if (a1_type != Ptype.FLOAT)
                        a1 = a1.cast(new Type(Ptype.FLOAT, 0), "", loc);
                    if (a2_type != Ptype.FLOAT)
                        a2 = a2.cast(new Type(Ptype.FLOAT, 0), "", loc);
                    return(new Val(a1.getSingleFval(loc) != a2.getSingleFval(loc), loc));
                }
                retval = new Val(a1.getSingleIval(loc) != a2.getSingleIval(loc), loc);
            }
            break;
        case BITAND:
            typeCHECK(op, a1, a2, loc, Ptype.BITS, Ptype.INT, Ptype.UINT, Ptype.FIXED, Ptype.UFIXED);
            if (a1_is_target || a2_is_target)
                return(target_bin(a1, a2, op, TDEOp.AND, loc));
            else
                retval = new Val(a1.getSingleIval(loc) & a2.getSingleIval(loc), loc);
            break;
        /*case BITNAND:
            typeCHECK(op, a1, a2, loc, Ptype.BITS, Ptype.INT, Ptype.UINT);
            if (a1_is_target || a2_is_target)
                return(target_bin(a1, a2, op, TDEOp.NAND, 0, loc));
            else
                retval = new Val(~(a1.getSingleIval(loc) & a2.getSingleIval(loc)), loc);
            break;*/
        /*case _BITAND:
            typeCHECK(op, a1, a2, loc, Ptype.BITS, Ptype.INT, Ptype.UINT);
            if (a1_is_target || a2_is_target)
                return(target_bin(a1, a2, op, TDEOp._AND, 0, loc));
            else
                retval = new Val(~a1.getSingleIval(loc) & a2.getSingleIval(loc), loc);
            break;*/
        /*case BITAND_:
            typeCHECK(op, a1, a2, loc, Ptype.BITS, Ptype.INT, Ptype.UINT);
            if (a1_is_target || a2_is_target)
                return(target_bin(a1, a2, op, TDEOp.AND_, 0, loc));
            else
                retval = new Val(a1.getSingleIval(loc) & ~a2.getSingleIval(loc), loc);
            break;*/
        case BITOR:
            typeCHECK(op, a1, a2, loc, Ptype.BITS, Ptype.INT, Ptype.UINT, Ptype.FIXED, Ptype.UFIXED);
            if (a1_is_target || a2_is_target)
                return(target_bin(a1, a2, op, TDEOp.OR, loc));
            else
                retval = new Val(a1.getSingleIval(loc) | a2.getSingleIval(loc), loc);
            break;
        /*case BITNOR:
            typeCHECK(op, a1, a2, loc, Ptype.BITS, Ptype.INT, Ptype.UINT);
            if (a1_is_target || a2_is_target)
                return(target_bin(a1, a2, op, TDEOp.NOR, 0, loc));
            else
                retval = new Val(~(a1.getSingleIval(loc) | a2.getSingleIval(loc)), loc);
            break;*/
        /*case _BITOR:
            typeCHECK(op, a1, a2, loc, Ptype.BITS, Ptype.INT, Ptype.UINT);
            if (a1_is_target || a2_is_target)
                return(target_bin(a1, a2, op, TDEOp._OR, 0, loc));
            else
                retval = new Val(~a1.getSingleIval(loc) | a2.getSingleIval(loc), loc);
            break;*/
        /*case BITOR_:
            typeCHECK(op, a1, a2, loc, Ptype.BITS, Ptype.INT, Ptype.UINT);
            if (a1_is_target || a2_is_target)
                return(target_bin(a1, a2, op, TDEOp.OR_, 0, loc));
            else
                retval = new Val(a1.getSingleIval(loc) | ~a2.getSingleIval(loc), loc);
            break;*/
        case BITXOR:
            typeCHECK(op, a1, a2, loc, Ptype.BITS, Ptype.INT, Ptype.UINT, Ptype.FIXED, Ptype.UFIXED);
            if (a1_is_target || a2_is_target)
                return(target_bin(a1, a2, op, TDEOp.XOR, loc));
            else
                retval = new Val(a1.getSingleIval(loc) ^ a2.getSingleIval(loc), loc);
            break;
        /*case BITXNOR:
            typeCHECK(op, a1, a2, loc, Ptype.BITS, Ptype.INT, Ptype.UINT);
            if (a1_is_target || a2_is_target)
                return(target_bin(a1, a2, op, TDEOp.XNOR, 0, loc));
            else
                retval = new Val(~(a1.getSingleIval(loc) ^ a2.getSingleIval(loc)), loc);
            break;*/
        case LOGAND:
            typeCHECK(op, a1, a2, loc, Ptype.LOG);
            if (a1_is_target && !a2_is_target) {
                if (a2.getSingleLval(loc))
                    retval = a1;
                else
                    retval = new Val(false, loc);
            } else if (!a1_is_target && a2_is_target) {
                if (a1.getSingleLval(loc))
                    retval = a2;
                else
                    retval = new Val(false, loc);
            } else if (a1_is_target && a2_is_target)
                return(log_bin(a1, a2, TDEType.AND, loc));
            else
                retval = new Val(a1.getSingleLval(loc) && a2.getSingleLval(loc), loc);
            break;
        case LOGOR:
            typeCHECK(op, a1, a2, loc, Ptype.LOG);
            if (a1_is_target && !a2_is_target) {
                if (a2.getSingleLval(loc))
                    retval = new Val(true, loc);
                else
                    retval = a1;
            } else if (!a1_is_target && a2_is_target) {
                if (a1.getSingleLval(loc))
                    retval = new Val(true, loc);
                else
                    retval = a2;
            } else if (a1_is_target && a2_is_target)
                return(log_bin(a1, a2, TDEType.OR, loc));
            else
                retval = new Val(a1.getSingleLval(loc) || a2.getSingleLval(loc), loc);
            break;
        case LOGXOR:
            typeCHECK(op, a1, a2, loc, Ptype.LOG);
            if (a1_is_target && !a2_is_target) {
                if (a2.getSingleLval(loc))
                    retval = target_un(a1, TDEOp.INV, loc);
                else
                    retval = a1;
            } else if (!a1_is_target && a2_is_target) {
                if (a1.getSingleLval(loc))
                    retval = target_un(a2, TDEOp.INV, loc);
                else
                    retval = a2;
            } else if (a1_is_target && a2_is_target)
                return(log_bin(a1, a2, TDEType.XOR, loc));
            else {
                boolean b1 = a1.getSingleLval(loc);
                boolean b2 = a2.getSingleLval(loc);
                retval = new Val((b1 && !b2) || (!b1 && b2), loc);
            }
            break;
        case CONDIT:
            typeCHECK(op, a1, " left", loc, Ptype.LOG);
            Type    tt = a2.getType();
            Type    ft = a3.getType();
            if (!tt.isEqual(ft, tt.getPrimType() == Ptype.NONE))
                throw new ExEx("?: types differ", loc);
            if (a1_is_target)
                return(target_ter(a1, a2, a3, op, loc));
            else {
                if (a1.getSingleLval(loc))
                    retval = a2;
                else
                    retval = a3;
            }
            break;
        case RAV:
            if (!family.allowQueue())
                throw new ExEx("Queue availability is not implemented for this device", loc);
            retval = a1.getReadAvailVal();
            QueueRefs    queues = new QueueRefs();
            queues.unbufferedQueues(true, "< unary operator used on unbuffered queue", loc);
            queues.setReadAVCheck(a1.getQueues());
            retval.setQueues(queues);
            break;
        case EXAM:
            if (!family.allowQueue())
                throw new ExEx("Queue examine is not implemented for this device", loc);
            if (a1_is_target) {
                QueueRefs a1queues = a1.getQueues();
                a1queues.unbufferedQueues(true, "? operator used on unbuffered queue", loc);
            }
            retval = a1.getExamVal();
	    break;
        case ASSIGN:    // not reached - handled elsewhere
            break;
        case WAV:       // not reached - handled above around line 173
            break;
        }

        retval.setSrcLoc(loc);
        return(retval);
    }

    private static Val target_un (Val arg, TDEOp tdeop, SrcLoc loc) {
        arg.toTarget();

        TDEVar      arg_tdev = arg.getTDEVar();
        Ptype       arg_type = arg_tdev.getWordSpec().getPrimType(0);
        int         result_width = arg.getSingleNumBits();
        int         result_offset = arg_tdev.getWordSpec().getFixOffset(0);
        Ptype       res_type = arg_type;
        
        if (tdeop == TDEOp.NEG) {
            switch (arg_type) {
            case UINT:
                res_type = Ptype.INT;
                break;
            case UFIXED:
                res_type = Ptype.FIXED;
                break;
            default:
                break;
            }
        }
        
        TDE         tde    = new TDE(TDEType.OPERATOR, loc);
        WordSpec    ws = new WordSpec(result_width, result_offset, res_type);
        TDEVar      result = tdelist.signal("E", ws, loc);

        tde.add2p(tdeop);
        tde.add2p(arg_type.isSigned());
        tde.add2i(arg_tdev);
        tde.add2o(result);
        tdelist.addTDE(tde);

        Val val = new Val(null, Mode.VALUE, result, loc);
        val.addExecSets(arg);
        val.andSetQueues(arg, loc);
        val.addAVChecks(arg);
        val.addOVars(arg);
        val.addIVars(arg);
        return(val);
    }

    private static Val target_bin (Val l, Val r, TreeOp op, TDEOp tdeop, SrcLoc loc) {
        int rwidth;
        l.toTarget();
        r.toTarget();

        ArgParams   ap = new ArgParams(l, r, op, tdeop, false, loc);
        rwidth = ap.result_width;
        WordSpec    ws = new WordSpec(rwidth, ap.result_offset, ap.result_type);
        TDEVar      arg1_tdev = ap.left_tdevar;
        TDEVar      result = tdelist.signal("E", ws, loc);
        
        if (ap.nop) {
            tdelist.connect(result, arg1_tdev, false);
            Val     v = new Val(null, Mode.VALUE, result, loc);
            v.addExecSets(l);
            v.andSetQueues(l, loc);
            v.addOVars(l);
            v.addIVars(l);
            return(v);
        }
                
        TDEVar      arg2_tdev = ap.right_tdevar;
        Ptype       l_type = l.getPrimType();
        Ptype       r_type = r.getPrimType();
        TDE         tde = null;
        //Var         dclock = checkclocks(a1, a2, tdeop.opstring(), loc);

        tde = new TDE(TDEType.OPERATOR, loc);
        tde.add2p(tdeop);
        tde.add2p(l_type.isSigned());
        tde.add2p(r_type.isSigned());
        tde.add2i(arg1_tdev);
        tde.add2i(arg2_tdev);
        tde.add2o(result);
        tdelist.addTDE(tde);

        Val val = new Val(null, Mode.VALUE, result, loc);
        val.addExecSets(l);
        val.addExecSets(r);
        val.andSetQueues(l, loc);
        val.andSetQueues(r, loc);
        val.addAVChecks(l);
        val.addAVChecks(r);
        val.addOVars(l);
        val.addIVars(l);
        val.addOVars(r);
        val.addIVars(r);
        return(val);
    }
    
    // The ?: operator (TreeOp.CONDIT) is the only ternary operator implemented.
    // This code is specific to that operator.
    private static Val target_ter (Val a1, Val a2, Val a3, TreeOp op, SrcLoc loc) {
        a1.toTarget();
        a2.toTarget();
        a3.toTarget();

        WordSpec    ws;
        Type        rtype = a2.getType();
        TDEVar      a2_tdev = null;
        TDEVar      a3_tdev = null;
        if (rtype.isPrimitive()) {
            ArgParams  ap = new ArgParams(a2, a3, op, TDEOp.COND, false, loc);
            a2_tdev = ap.left_tdevar;
            a3_tdev = ap.right_tdevar;
            ArrayList<String>   eids = null;
            ArrayList<Long>     eords = null;
            if ((a2.getPrimType() == Ptype.ENUM) |
                (a3.getPrimType() == Ptype.ENUM)) { // MAYBE SHOULD CHECK HERE THAT a2 AND a3 HAVE SAME ENUM TYPE
                WordSpec    w = a2.getWordSpec();
                eids = w.getEnumIDs(0);
                eords = w.getEnumOrds(0);
            }
            ws = new WordSpec(0, 0, ap.result_width-1, ap.result_offset, 0, 0, eids, eords, ap.result_type, null);
        } else
            ws = rtype.getWordSpec(null, loc);
        
        TDEVar      result = tdelist.signal("E", ws, loc);
        TDEVar      a1_tdev = a1.getTDEVar();
        Ptype       a1_type = a1.getPrimType();
        int         words = rtype.numWords();
        Val         val = new Val(null, Mode.VALUE, result, loc);
        for (int i=0 ; i<words ; i++) {
            if (!rtype.isPrimitive()) {
                a2_tdev = a2.getTDEVar().getWord(i, loc);
                a3_tdev = a3.getTDEVar().getWord(i, loc);
            }
            TDEVar      r_tdev  = result.getWord(i, loc);
            Ptype       a2_type = a2_tdev.getWordSpec().getPrimType(0);
            Ptype       a3_type = a3_tdev.getWordSpec().getPrimType(0);
            TDE         tde     = new TDE(TDEType.OPERATOR, loc);

            tde.add2p(TDEOp.COND);
            tde.add2p(a1_type.isSigned());
            tde.add2p(a2_type.isSigned());
            tde.add2p(a3_type.isSigned());
            tde.add2i(a1_tdev);
            tde.add2i(a2_tdev);
            tde.add2i(a3_tdev);
            tde.add2o(r_tdev);
            tdelist.addTDE(tde);
            val.addExecSets(a1);
            val.addExecSets(a2);
            val.addExecSets(a3);
        }
        
        QueueRefs a1queues = a1.getQueues().copy(); // copy as may be locally changed
        QueueRefs a2queues = a2.getQueues().copy(); // " " "
        QueueRefs a3queues = a3.getQueues().copy(); // " " "

        a1queues.unbufferedQueues(false, "?: operator test expression has unbuffered queue reads", loc);
        a2queues.unbufferedQueues(false, "?: operator 2nd argument has unbuffered queue reads", loc);
        a3queues.unbufferedQueues(false, "?: operator 3rd argument has unbuffered queue reads", loc);
        
        val.andSetQueues(a1queues, loc);
        if (a2queues.isEmpty())
            a2queues.or(a1_tdev, false, loc);
        else
            a2queues.and(a1_tdev, false, loc);
        if (a3queues.isEmpty())
            a3queues.or(a1_tdev, true, loc);
        else
            a3queues.and(a1_tdev, true, loc);
        a2queues.or(a3queues, loc);
        val.andSetQueues(a2queues, loc);        
        val.addAVChecks(a1);
        val.addAVChecks(a2);
        val.addAVChecks(a3);
        val.addOVars(a1);
        val.addIVars(a1);
        val.addOVars(a2);
        val.addIVars(a2);
        val.addOVars(a3);
        val.addIVars(a3);
        return(val);
    }

    // The add/subtract operator (TreeOp.ADDSUB) is the only quinary operator
    // implemented. It does not appear in expressions and is only invoked via
    // the addsub() or abs() inbuilt functions. This code is specific to
    // that operator.
    public static Val target_quin (Val l, Val r, Val incr, Val gate, Val xci, SrcLoc loc) {
        l.toTarget();
        r.toTarget();
        incr.toTarget();
        gate.toTarget();
        xci.toTarget();
        // incr, gate and xci are required to be a target mode and type "log"
 
        ArgParams   ap = new ArgParams(l, r, TreeOp.ADD, TDEOp.ADDSUB, false, loc);
        WordSpec    ws = new WordSpec(ap.result_width, ap.result_offset, ap.result_type);
        TDEVar      arg1_tdev = ap.left_tdevar;
        TDEVar      arg2_tdev = ap.right_tdevar;
        TDEVar      incr_tdev = incr.getTDEVar();
        TDEVar      gate_tdev = gate.getTDEVar();
        TDEVar      xci_tdev = xci.getTDEVar();
        Ptype       l_type = l.getPrimType();
        Ptype       r_type = r.getPrimType();
        TDE         tde = null;
        //Var         dclock = checkclocks(a1, a2, tdeop.opstring(), loc);
        TDEVar      result = tdelist.signal("E", ws, loc);

        tde = new TDE(TDEType.OPERATOR, loc);
        tde.add2p(TDEOp.ADDSUB);
        tde.add2p(l_type.isSigned());
        tde.add2p(r_type.isSigned());
        tde.add2i(arg1_tdev);
        tde.add2i(arg2_tdev);
        tde.add2i(incr_tdev);
        tde.add2i(gate_tdev);
        tde.add2i(xci_tdev);
        tde.add2o(result);
        tdelist.addTDE(tde);

        Val val = new Val(null, Mode.VALUE, result, loc);
        val.addExecSets(l);
        val.addExecSets(r);
        val.addExecSets(incr);
        val.addExecSets(gate);
        val.addExecSets(xci);
        val.andSetQueues(l, loc);
        val.andSetQueues(r, loc);
        val.andSetQueues(incr, loc);
        val.andSetQueues(gate, loc);
        val.andSetQueues(xci, loc);
        val.addOVars(l);
        val.addIVars(l);
        val.addOVars(r);
        val.addIVars(r);
        val.addOVars(incr);
        val.addIVars(incr);
        val.addOVars(gate);
        val.addIVars(gate);
        val.addOVars(xci);
        val.addIVars(xci);
        return(val);
    }

    private static Val log_bin (Val l, Val r, TDEType tdecode, SrcLoc loc) {
        WordSpec    ws = new WordSpec(1, Ptype.LOG);
        TDEVar      arg1_tdev = l.getTDEVar();
        TDEVar      arg2_tdev = r.getTDEVar();
        TDE         tde    = new TDE(tdecode, loc);
        //Var         dclock = checkclocks(a1, a2, tdecode.typename(), loc);
        TDEVar      result = tdelist.signal("E", ws, loc);

        tde.add2i(arg1_tdev);
        tde.add2i(arg2_tdev);
        tde.add2o(result);
        tdelist.addTDE(tde);

        Val val = new Val(null, Mode.VALUE, result, loc);
        val.andSetQueues(l, loc);
        val.andSetQueues(r, loc);
        val.addOVars(l);
        val.addOVars(r);
        if (tdecode == TDEType.AND) {
            // Can only propagate queue availability read or write checks
            // on operation AND since only valid to regard them as
            // checked if all are true.
            val.addAVChecks(l);
            val.addAVChecks(r);
        }
        return(val);
    }

    /**
     * Check the type of a Val against one or more primitive types.
     * @param   op is the operator
     * @param   v is the operand
     * @param   mess is a message string to be used in an error message.
     * @param   loc is the source file location
     * @param   args are the allowed primitive types
     */
    private static void typeCHECK (TreeOp op, Val v, String mess, SrcLoc loc, Object ... args) {
        int     n = args.length;
        Ptype   ptype = v.getPrimType();
        for (int i=0 ; i<n ; i++) {
            if (ptype == (Ptype)args[i])
                return;
        }
        typeERR(op, ptype, mess, args, loc);
    }

    /**
     * Check the type of two Vals (left and right) against one or more
     * primitive types. Throw an exception if either operand is not
     * included in the allowed types.
     * @param   op is the operator
     * @param   l is the left operand
     * @param   r is the right operand
     * @param   loc is the source file location
     * @param   args are the allowed primitive types
     */
    private static void typeCHECK (TreeOp op, Val l, Val r, SrcLoc loc, Object ... args) {
        int     n = args.length;
        boolean lok = false;
        boolean rok = false;
        Ptype   ltype = l.getPrimType();
        Ptype   rtype = r.getPrimType();
        for (int i=0 ; i<n ; i++) {
            if (ltype == (Ptype)args[i]) {
                lok = true;
                break;
            }
        }
        if (!lok)
            typeERR(op, ltype, "left", args, loc);
        for (int i=0 ; i<n ; i++) {
            if (rtype == (Ptype)args[i]) {
                rok = true;
                break;
            }
        }       
        if (!rok)
            typeERR(op, rtype, "right", args, loc);
        return;
    }
   
    private static void typeERR (TreeOp op, Ptype ptype, String mess, Object[] ta, SrcLoc loc) {
        int             n = ta.length;
        StringBuffer    sb = new StringBuffer("'"+ptype.typename()+"'");
        sb.append(" wrong type for ");
        sb.append(op.opname());
        sb.append(" " + mess);
        sb.append(" operand\nshould be ");
        for (int i=0 ; i<n ; i++) {
            if (i != 0) {
                if (i  == (n - 1))
                    sb.append(" or ");
                else
                    sb.append(", ");
            }
            sb.append(((Ptype)ta[i]).typename());
        }
        throw new ExEx(sb.toString(), loc);
    }
    
    private static void typeERR (TreeOp op, Ptype ltype, Ptype rtype, SrcLoc loc) {
        StringBuffer    sb = new StringBuffer("'"+ltype.typename()+"'");
        sb.append(" and '"+rtype.typename()+"'");
        sb.append(" wrong types for ");
        sb.append(op.opname());
        throw new ExEx(sb.toString(), loc);
    }

    private static void check_constant_size (Val immed, Val target, String mess, SrcLoc loc) {
        int     n;
        switch (immed.getPrimType()) {
        case UINT:
            n = Functions.bits(immed.getSingleIval(loc), false);
            break;
        case INT:
            long    v = immed.getSingleIval(loc);
            if (v >= 0)
                n = Functions.bits(v, false);
            else
                n = Functions.bits(v, true);
            break;
        case ENUM:
            n = immed.getValType(0).numBits();
            break;
        default:
            throw new ExEx("disallowed type for " + mess, loc);
        }
        if (n >  target.getSingleNumBits())
            throw new ExEx("constant operand larger than target operand for " + mess, loc);
    }
    
    private static boolean ptreq (Val v1, Val v2, SrcLoc loc) {
        Ref ref1 = v1.getSinglePval(loc);
        Ref ref2 = v2.getSinglePval(loc);
        
        if (ref1 == ref2)
            return(true);
        if ((ref1 == null) || (ref2 == null))
            return(false);
        
        if (ref1.getVar() != ref2.getVar())
            return(false);
        WordSpec    ws1 = ref1.getWordSpec();
        WordSpec    ws2 = ref2.getWordSpec();
        
        return(ws1.equals(ws2, true));
    }
    
    /*
    private Var checkclocks (Val v1, Val v2, String mess, SrcLoc loc) {
        TDEVar  tdev1 = v1.getTDEVar();
        TDEVar  tdev2 = v2.getTDEVar();
        Var     dclock1 = tdev1.getClkVar();
        Var     dclock2 = tdev2.getClkVar();
        if (dclock1 == dclock2)
            return(dclock1);
        if (dclock1 == null)
            return(dclock2);
        if (dclock2 == null)
            return(dclock1);
        throw new ExEx(mess, v1, v2, loc);
    }
    
    private Var checkclocks (Val v1, Val v2, Val v3, String mess, SrcLoc loc) {
        TDEVar  tdev1 = v1.getTDEVar();
        TDEVar  tdev2 = v2.getTDEVar();
        TDEVar  tdev3 = v3.getTDEVar();
        Var     dclock1 = tdev1.getClkVar();
        Var     dclock2 = tdev2.getClkVar();
        Var     dclock3 = tdev3.getClkVar();
        boolean ignore1 = (dclock1 == null);
        boolean ignore2 = (dclock2 == null);
        boolean ignore3 = (dclock3 == null);
           
        if ((dclock1 == dclock2) && (dclock2 == dclock3))
            return(dclock1);
        if (ignore1 && (dclock2 == dclock3))
            return(dclock2);
        if (ignore2 && (dclock1 == dclock3))
            return(dclock1);
        if (ignore3 && (dclock1 == dclock2))
            return(dclock1);
        if (ignore1 && ignore2)
            return(dclock3);
        if (ignore2 && ignore3)
            return(dclock1);
        if (ignore1 && ignore3)
            return(dclock2);
        throw new ExEx(mess, v1, v2, v3, loc);
    }
    */
}
