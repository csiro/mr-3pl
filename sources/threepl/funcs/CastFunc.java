package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.nodes.Node;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt function to change the type of an expression.
 * This has 2 input arguments.
 *
 * <p>The 1st input argument is an expression.
 *
 * <p>The 2nd input argument is a string type specification, or
 * a type, giving the output type required.
 *
 * <p>For immediate types this will convert only primitive types as follows -
 * <ul>
 *  <li>{@code uint -> int}
 *  <li>{@code uint -> str}
 *  <li>{@code int -> str}
 *  <li>{@code log -> str}
 * </ul>
 *
 * <p>For primitive target types the conversions are -
 * <ul>
 *  <li>{@code uint -> int} - an additional 0 bit is appended to the most
 *  significant end
 *  <li>{@code * -> bits} - match bits with truncation or zero padding
 *  <li>{@code  bits -> *} - match bits with truncation or zero padding
 * </ul>
 *
 * <p>The only compound target conversion allowed is to or from bits. The
 * compound type is treated as a single word equivalent to concatenation
 * of the individual words of the compound type. Where the result width
 * is larger than the argument there will be zero padding at the most
 * significant bit end. If the result width is smaller than the argument
 * it will be truncated at the most significant bit end.
 */
public class CastFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the result
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        Type    type = null;
        if (args.size() != 2)
            throw new ExEx("cast() must have 2 input arguments", loc);
        
        // expression value
        Val     val = args.getVal(0);
        
        // type
        // If a variable, see if it is type 'type', otherwise
        // evaluate the argument as a string.
        Node    n = args.getNode(1);
        Val     tval = n.getVal();
        if (tval.getMode() != Mode.IMMEDIATE)
            throw new ExEx("cast type argument is not immediate mode", loc);
        if (tval.getPrimType() == Ptype.TYPE)
            type = tval.getSingleTval(loc);
        else if (tval.getPrimType() == Ptype.STR) {
            String  s = args.getVal(1).getSingleSval(loc);
            type = new Type(s, loc);
        } else
            throw new ExEx("cast type argument is not string or type", loc);

        return(val.cast(type, "cast()", loc));
    }
}
