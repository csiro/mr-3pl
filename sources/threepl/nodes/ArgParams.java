package threepl.nodes;

import threepl.codegen.TDEConstants.TDEOp;
import threepl.codegen.TDEConstants.TDEVtype;

import static threepl.ThreePL.getFamily;
import static threepl.ThreePL.tdelist;

import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.RefOrVal;
import threepl.exec.Val;
import threepl.exec.WordSpec;
import threepl.parser.Constant.Ptype;
import threepl.parser.Constant.TreeOp;
import threepl.parser.SrcLoc;

/**
 * A class containing the operand widths and offsets,
 * the result type, width and offset and the operand
 * TDEVars which might have been left-shifted to align them.
 */
public class ArgParams {
    public  int     left_width;
    public  int     right_width;
    public  int     left_offset;
    public  int     right_offset;
    public  int     result_width;
    public  int     result_offset;
    public  boolean nop;
    public  Ptype   result_type;
    public  TDEVar  left_tdevar;
    public  TDEVar  right_tdevar;
    
    static private Ptype[][]    arithtype = {
                                    {  Ptype.UINT,   Ptype.INT, Ptype.UFIXED, Ptype.FIXED},
                                    {   Ptype.INT,   Ptype.INT,  Ptype.FIXED, Ptype.FIXED},
                                    {Ptype.UFIXED, Ptype.FIXED, Ptype.UFIXED, Ptype.FIXED},
                                    { Ptype.FIXED, Ptype.FIXED,  Ptype.FIXED, Ptype.FIXED}
                                };


    /**
     * Determine the result type, width and offset of an operation
     * and align operands where necessary.
     * @param   l is the left operand
     * @param   r is the right operand
     * @param   op is the tree operator
     * @param   top is the associated TDE operator
     * @param   use_left if true indicates that in all cases except
     *          multiplication and division the right operand offset
     *          should be adjusted to match the left operand regardless
     *          of relative offsets
     * @param   loc is the source file location
     */
    public ArgParams (
        RefOrVal    l,
        Val         r,
        TreeOp      op,
        TDEOp       top,
        boolean     use_left,
        SrcLoc      loc
    ) {
        Ptype       l_type = l.getPrimType();
        Ptype       r_type = r.getPrimType();
        boolean     lsigned = (l_type == Ptype.INT) || (l_type == Ptype.FIXED);
        boolean     rsigned = (r_type == Ptype.INT) || (r_type == Ptype.FIXED);
        int         w;
        
        left_width = l.getSingleNumBits();
        right_width = r.getSingleNumBits();
        left_offset = l.getPrimOffset();
        right_offset = r.getPrimOffset();
        
        //------------------------------------------------------------------
        // Result type.
        //
        switch (op) {
        case ADD:
        case SUB:
        case MUL:
        case REM:
        case DIV:
            result_type = arithtype[l_type.ordinal()][r_type.ordinal()];
            break;
        case LS:
        case RS:
        case LR:
        case RR:
            result_type = l_type;
            break;
        case LT:
        case GT:
        case LE:
        case GE:
        case EQ:
        case NE:
            result_type = Ptype.LOG;
            break;
        case BITAND:
//        case BITNAND:     THESE BITWISE OPERATIONS NOT IMPLEMENTED AS SEPARATE OPERATORS.
//        case _BITAND:     THE OTHER BITWISE OPERATORS COMBINED WITH INVERSION WILL
//        case BITAND_:     SUFFICE.
        case BITOR:
//        case BITNOR:
//        case _BITOR:
//        case BITOR_:
        case BITXOR:
//        case BITXNOR:
            if (l_type == Ptype.FIXED) {
                if ((r_type == Ptype.FIXED) || (r_type == Ptype.UFIXED))
                    throw new ExEx("cannot apply bitwise operation to two fixed point operands", loc);
                result_type = Ptype.FIXED;
                result_offset = left_offset;
            } else if (r_type == Ptype.FIXED) {
                if ((l_type == Ptype.FIXED) || (l_type == Ptype.UFIXED))
                    throw new ExEx("cannot apply bitwise operation to two fixed point operands", loc);
                result_type = Ptype.FIXED;
                result_offset = right_offset;
            } else if (l_type == Ptype.UFIXED) {
                if ((r_type == Ptype.FIXED) || (r_type == Ptype.UFIXED))
                    throw new ExEx("cannot apply bitwise operation to two fixed point operands", loc);
                result_type = Ptype.UFIXED;
                result_offset = left_offset;
            } else if (r_type == Ptype.UFIXED) {
                if ((l_type == Ptype.FIXED) || (l_type == Ptype.UFIXED))
                    throw new ExEx("cannot apply bitwise operation to two fixed point operands", loc);
                result_type = Ptype.UFIXED;
                result_offset = right_offset;
            } else if ((l_type == Ptype.INT) || (r_type == Ptype.INT))
                result_type = Ptype.INT;
            else if ((l_type == Ptype.UINT) || (r_type == Ptype.UINT))
                result_type = Ptype.UINT;
            else if ((l_type == Ptype.BITS) || (r_type == Ptype.BITS))
                result_type = Ptype.BITS;
            else
                result_type = Ptype.BITS;
            break;
        case LOGAND:
        case LOGXOR:
        case LOGOR:
            result_type = Ptype.LOG;
            break;
        case CONDIT:
            if ((l_type == Ptype.INT) || (r_type == Ptype.INT))
                result_type = Ptype.INT;
            else
                result_type = l_type;
            break;
        case ASSIGN:
            break;
        default:
            throw new ExEx("ExprNode result type error", loc);
        }

        //------------------------------------------------------------------
        // Result width and offset.
        //
        result_offset = 0;   // default
        switch (op) {
        case ADD:
        case SUB:
            left_width += ((!lsigned && rsigned) ? 1 : 0);
            right_width += ((lsigned && !rsigned) ? 1 : 0);
            result_offset = use_left ? left_offset : Math.max(left_offset, right_offset);
            result_width =  Math.max(
                                    left_width - left_offset,
                                    right_width - right_offset
                            ) + result_offset + 1;
            w = getFamily().AddSubWidth();
            if (w == 0)
                throw new ExEx("adder/subtracter not available in this device", loc);
            if (left_width > w)
                throw new ExEx("left adder/subtracter operand too wide for this device", loc);
            if (right_width > w)
                throw new ExEx("right adder/subtracter operand too wide for this device", loc);
            break;
        case MUL:
            left_width += ((!lsigned && rsigned) ? 1 : 0);
            if (top == TDEOp.LSH) // power-of-2-multiply?
                right_width = (int)((Long)r.getTDEVar().getConst()).longValue();
            else
                right_width += ((lsigned && !rsigned) ? 1 : 0);
            result_width = left_width + right_width;
            result_offset = left_offset + right_offset;
            if (top != TDEOp.MUL)
                break;
            w = getFamily().MultWidth1();
            if (w == 0)
                break; // no hardware multiplier
            if (left_width > w)
                throw new ExEx("multiplier operand too wide for this device", loc);
            w = getFamily().MultWidth2();
            if (right_width > w)
                throw new ExEx("multiplier operand too wide for this device", loc);
            break;
        case DIV:
            left_width += ((!lsigned && rsigned) ? 1 : 0);
            if ((result_type == Ptype.FIXED) || (result_type == Ptype.UFIXED)) {
                if (top == TDEOp.RSH) { // power-of-2-divide?
                    // Eliminate the divide operation and instead change the
                    // offset of the left operand, returning it as the value.
                    // Later adjustment of the offset on assignment will perform
                    // a division where required.
                    nop = true;
                    right_width = (int)((Long)r.getTDEVar().getConst()).longValue();
                    result_width = left_width;
                    result_offset = left_offset + right_width - right_offset;
                    if (result_width < 1)
                        throw new ExEx("division produces zero width result", loc);
                } else {
                    right_width += ((lsigned && !rsigned) ? 1 : 0);
                    result_width = left_width + right_width + left_offset;
                    result_offset = left_offset + right_width - right_offset;
                }
            } else {
                // Integer type so continue to perform a division, but the
                // result offset is zero.
                if (top == TDEOp.RSH)   // power-of-2-divide?
                    right_width = (int)((Long)r.getTDEVar().getConst()).longValue();
                else
                    right_width += ((lsigned && !rsigned) ? 1 : 0);
                result_width = left_width;
                result_offset = 0;
            }
            w = getFamily().DivWidth();
            if (w == 0)
                throw new ExEx("divider not available in this device", loc);
            if (left_width > w)
                throw new ExEx("left divider operand too wide for this device", loc);
            if (right_width > w)
                throw new ExEx("right divider operand too wide for this device", loc);
            break;
        case REM:
            right_width += ((lsigned && !rsigned) ? 1 : 0);
            result_width = right_width;
            result_offset = 0;
            break;
        case LS:
            TDEVtype rt = r.getTDEVar().getType();
            if ((rt == TDEVtype.UINT) || (rt == TDEVtype.INT))
                // constant shift
                result_width = left_width + ((Long)r.getTDEVar().getConst()).intValue();
            else
                // variable shift
                result_width = left_width + (1 << right_width) - 1;
            break;
        case RS:
        case LR:
        case RR:
            result_width = left_width;
            result_offset = left_offset;
            break;
        case LT:
        case GT:
        case LE:
        case GE:
        case EQ:
        case NE:
            result_width = 1;
            break;
        case BITAND:
//        case BITNAND:
//        case _BITAND:
//        case BITAND_:
        case BITOR:
//        case BITNOR:
//        case _BITOR:
//        case BITOR_:
        case BITXOR:
//        case BITXNOR:
            result_width =
                Math.max(left_width - left_offset, right_width - right_offset)
                +
                Math.max(left_offset, right_offset);
            result_offset = left_offset != 0 ? left_offset : right_offset;
            break;
            /*
            if (lw == rw)
                return(lw);
            if ((lw == 1) || (rw == 1))
                return(Math.max(lw, rw));
            if (l.getWordTDEVar(0, loc).getType() != tdeVAR) {
                if (lw > rw)
                    throw new ExEx("bitwise logical left constant operand too large", loc);
                return(rw);
            }
            if (r.getWordTDEVar(0, loc).getType() != tdeVAR) {
                if (rw > lw)
                    throw new ExEx("bitwise logical right constant operand too large", loc);
                return(lw);
            }
            return(Math.max(lw, rw));
            */
            //throw new ExEx("bitwise logical operands different sizes", loc);
        case LOGAND:
        case LOGXOR:
        case LOGOR:
        case CONDIT:
            left_width += ((!lsigned && rsigned) ? 1 : 0);
            right_width += ((lsigned && !rsigned) ? 1 : 0);
            result_width = Math.max(left_width-left_offset, right_width-right_offset) + Math.max(left_offset, right_offset);
            result_offset = Math.max(left_offset, right_offset);
            break;
        case ASSIGN:
            break;
        default:
            throw new ExEx("ExprNode result width error");
        }

        //------------------------------------------------------------------
        // Get operand TDEVars, left shifted if necessary to align offsets.
        //
        switch (op) {
        case ADD:
        case SUB:
        case CONDIT:
        case LT:
        case GT:
        case LE:
        case GE:
        case EQ:
        case NE:
        case ASSIGN:
            if (left_offset == right_offset) {
                left_tdevar = l.getTDEVar();
                right_tdevar = r.getTDEVar();
            } else if ((left_offset > right_offset) || use_left) {
                int shift = left_offset - right_offset;
                left_tdevar = l.getTDEVar();
                if (r_type == Ptype.UINT)
                    r_type = Ptype.UFIXED;
                else if (r_type == Ptype.INT)
                    r_type = Ptype.FIXED;
                WordSpec    rws = new WordSpec(left_width, left_offset, r_type);
                right_tdevar = tdelist.signal("RHS", rws,loc);
                tdelist.lshift(right_tdevar, r.getTDEVar(), shift, loc);
            } else {
                int shift = right_offset - left_offset;
                right_tdevar = r.getTDEVar();
                if (l_type == Ptype.UINT)
                    l_type = Ptype.UFIXED;
                else if (l_type == Ptype.INT)
                    l_type = Ptype.FIXED;
                WordSpec    lws = new WordSpec(right_width, right_offset, l_type);
                left_tdevar = tdelist.signal("RHS", lws,loc);
                tdelist.lshift(left_tdevar, l.getTDEVar(), shift, loc);
            }
            break;
        case DIV:
            if ((result_type == Ptype.FIXED) || (result_type == Ptype.UFIXED)) {
                // fixed or ufixed result
                if (top == TDEOp.RSH) {
                    // power-of-2-divide - is a shift
                    left_tdevar = l.getTDEVar();
                    right_tdevar = new TDEVar((long)0, WordSpec.UINT, 0, loc);
                } else {
                    // not a power of 2 - align numerator with result offset
                    int         lwidth = left_width + result_offset - left_offset;
                    int         shift = right_width;
                    WordSpec    lws = new WordSpec(lwidth, left_offset+shift, l_type);
                    left_tdevar = l.getTDEVar().getWord(lws, 0, loc);
                    right_tdevar = r.getTDEVar();
                }
            } else {
                // int or uint result
                left_tdevar = l.getTDEVar();
                right_tdevar = r.getTDEVar();
            }
            break;
        default:
            left_tdevar = l.getTDEVar();
            right_tdevar = r.getTDEVar();
        }
    }
}
