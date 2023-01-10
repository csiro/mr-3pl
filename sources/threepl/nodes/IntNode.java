package threepl.nodes;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.parser.Constant;
import threepl.parser.Token;

/**
 * An integer constant node. Integers are stored as <b>Long</b>s.
 */
public final class IntNode extends Node implements Constant {
    protected long  integer;

    /**
     * Construct an integer constant node.
     * @param   t is the constant token from which the value and the
     *          file source location are extracted
     */
    public IntNode (Token t) {
        super(t);
        try {
            if (t.image.startsWith("0b") || t.image.startsWith("0B"))
                integer = Long.parseLong(t.image.substring(2), 2);
            else if (t.image.startsWith("0x") || t.image.startsWith("0X"))
                integer = Long.parseLong(t.image.substring(2), 16);
            else if (t.image.startsWith("0") || t.image.startsWith("0"))
                integer = Long.parseLong(t.image, 8);
            else
                integer = Long.parseLong(t.image);
        } catch (NumberFormatException e) {
            throw new ExEx("integer bad number format", loc);
        }
    }

    /**
     * Construct an integer constant node.
     * @param   i is the integer constant
     */
    public IntNode (long i) {
        super((Token)null);
        integer = i;
    }
    
    /**
     * Get the value of the integer constant.
     * @return  the value of the integer constant
     */
    public Val getVal () {
        Val val = new Val(integer, loc);
        val.setSrcLoc(loc);
        return(val);
    }
}
