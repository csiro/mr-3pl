package threepl.nodes;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.parser.Constant;
import threepl.parser.Token;

/**
 * A floating point constant node. Floating point constants are
 * stored as <b>Double</b>s.
 */
public final class FloatNode extends Node implements Constant {
    protected double    fval;

    /**
     * Construct a floating point constant node.
     * @param   t is the constant token from which the value and the
     *          file source location are extracted
     */
    public FloatNode (Token t) {
        super(t);
        try {
            fval = Double.parseDouble(t.image);
        } catch (NumberFormatException e) {
            throw new ExEx("floating point bad number format", loc);
        }
    }

    /**
     * Construct a floating point constant node.
     * @param   f is the floating point constant
     */
    public FloatNode (double f) {
        super((Token)null);
        fval = f;
    }
    
    /**
     * Get the value of the floating point constant.
     * @return  the value of the floating point constant
     */
    public Val getVal () {
        Val val = new Val(fval, loc);
        val.setSrcLoc(loc);
        return(val);
    }
}
