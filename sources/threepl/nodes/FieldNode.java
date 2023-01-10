package threepl.nodes;

import threepl.parser.Constant;
import threepl.parser.Token;

/**
 * A struct field identifier on a variable occurrence.
 */
public final class FieldNode extends Node implements Constant {

    /**
     * Construct a struct field identifier node.
     * @param   n is a string node containing the field identifier
     * @param   t is a token from which the 
     *          file source location is extracted
     */
    public FieldNode (StrNode n, Token t) {
        super(t);
        addSubNode(n);
    }
}
