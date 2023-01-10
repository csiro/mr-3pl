package threepl.nodes;

import threepl.exec.Val;
import threepl.parser.Constant;
import threepl.parser.Token;

/**
 * A string constant node.
 */
public final class StrNode extends Node implements Constant {
    protected String    string;

    /**
     * Construct a string constant node.
     * @param   s is the string
     * @param   t is the constant token from which the value and the
     *          file source location are extracted
     */
    public StrNode (String s, Token t) {
        super(t);
        string = s;
    }
    
    /**
     * Get the value of the constant.
     * @return  the value of the constant (Var)
     */
    public Val getVal () {
        string = string.replaceAll("\\\\n", "\n");
        string = string.replaceAll("\\\\t", "\t");
        string = string.replaceAll("\\\\b", "\b");
        string = string.replaceAll("\\\\r", "\r");
        string = string.replaceAll("\\\\f", "\f");
        string = string.replaceAll("\\\\'", "'");
        string = string.replaceAll("\\\\\"", "\"");
        string = string.replaceAll("\\\\\\\\",  "\\\\");
        String[]        sa = string.split("\\\\", -1);
        StringBuffer    sb = new StringBuffer(sa[0]);
        for (int i=1 ; i<sa.length ; i++) {
            if (sa[i].startsWith("0")) {
                Byte    b = Byte.valueOf(sa[i].substring(1, 4), 8);
                sb.append((char)b.byteValue());
                if (sa[i].length() > 4)
                    sb.append(sa[i].substring(4));
            } else if (sa[i].startsWith("x")) {
                Byte    b = Byte.valueOf(sa[i].substring(1, 3), 16);
                sb.append((char)b.byteValue());
                if (sa[i].length() > 3)
                    sb.append(sa[i].substring(3));
            } else {
                sb.append("\\");
                sb.append(sa[i]);
            }
        }
        
        Val val = new Val(sb.toString(), loc);
        val.setSrcLoc(loc);
        return(val);
    }
}
