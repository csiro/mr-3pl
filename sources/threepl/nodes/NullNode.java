package threepl.nodes;

import threepl.exec.Val;
import threepl.parser.Constant;
import threepl.parser.Token;

/**
 * A null node.
 */
public final class NullNode extends Node implements Constant {
    /**
     * Construct a null node.
     * @param   t is the constant token from which the 
     *          file source location is extracted
     */
    public NullNode (Token t) {
        super(t);
    }
    
    /**
     * Get the value of the null.
     * @return  the value of the null
     */
    public Val getVal () {
        Val val = new Val(loc);
        val.setSrcLoc(loc);
        return(val);
    }
}
