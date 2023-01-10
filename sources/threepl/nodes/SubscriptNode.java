package threepl.nodes;

import threepl.parser.Constant;
import threepl.parser.Token;

/**
 * A subscript or subscript range pair on a variable occurence.
 */
public final class SubscriptNode extends Node implements Constant {

    /**
     * Construct a subscript or subscript range pair node.
     * @param   lower is the lower or only subscript expression node
     * @param   upper is the upper subscript expression node or null
     * @param   t is a token from which the 
     *          file source location is extracted
     */
    public SubscriptNode (Node lower, Node upper, Token t) {
        super(t);
        addSubNode(lower);
        addSubNode(upper);
    }
}
