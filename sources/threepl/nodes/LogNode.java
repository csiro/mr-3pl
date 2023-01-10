package threepl.nodes;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.parser.Constant;
import threepl.parser.Token;

/**
 * A logical constant node.
 */
public final class LogNode extends Node implements Constant {
    protected boolean   log;

    /**
     * Construct a logical constant node.
     * @param   t is the constant token from which the value and the
     *          file source location are extracted
     */
    public LogNode (Token t) {
        super(t);
        if (t.image.equals("false"))
            log = false;
        else if (t.image.equals("true"))
            log = true;
        else
            throw new ExEx("logical constant format error", loc);
    }
    
    /**
     * Get the value of the constant.
     * @return  the value of the constant (Var)
     */
    public Val getVal () {
        Val val = new Val(log, loc);
        val.setSrcLoc(loc);
        return(val);
    }
}
