package threepl.nodes;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.parser.Constant;
import threepl.parser.Token;

/**
 * Parameter/argument match '='.
 */
public final class KeyMatchNode extends Node implements Constant {
    private String  key;    // parameter identifier (key)

    /**
     * Construct a parameter/argument match node.
     * @param   t is the LHS identifier token
     * @param   n is the RHS expression node
     */
    public KeyMatchNode (Token t, Node n) {
        super(n.getSrcLoc());
        key = t.toString();
        addSubNode(n);
    }

    /**
     * If this is called it is an error. It can only be called
     * when evaluating an argument for an inbuilt module, procedure or
     * function call and for these key matching of arguments against
     * parameters is not possible since the parameters are implicit
     * and hence have no identifiers. For user modules, procedures and
     * functions the subtree of this node is explicitly evaluated
     * (in Body.getArgs()), not the node itself.
     */
    public Val getVal () {
        throw new ExEx("cannot use argument/parameter matching by key here", loc);
    }
    
    /**
     * Get the parameter key string.
     * @return  the parameter key string
     */
    public String getParamKey () {
        return(key);
    }
}
