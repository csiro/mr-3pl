package threepl.parser;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import threepl.ThreePL;
import threepl.exceptions.ExEx;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.nodes.Ident;
import threepl.nodes.NodeList;

/**
 * This is just a collection of unrelated miscellaneous methods.
 */
public class Functions implements Constant {
    static private String   sbuf;
    static private String   in;
    static private int      index;
    static private int      nargs;
    static private int      argnum;
    static private int      maxwidth;
    static private Ttype    token;
    static private int[]    args;  

    /**
     * Return a string of zeros.
     * @param   n is the number of zeros
     * @return  the string of zeros
     */
    public static String zeropad (int n) {
        StringBuffer    sb = new StringBuffer();
        while (n-- > 0)
            sb.append("0");
        return(sb.toString());
    }

    /**
     * Return number of bits needed to represent an integer
     * argument. If the integer is flagged as signed an extra
     * sign bit is counted in addition to the number of bits
     * required by the magnitude.
     * @param   n is the integer
     * @param   signed is true if the integer is signed
     * @return  the number of bits necessary to represent the integer
     *          argument.
     */
    public static int bits (long n, boolean signed) {
        int     count = 0;
        boolean negative = false;
        
        if (n == 0)
            return(1);

        if (n < 0) {
            negative = true;
            n = -n;
        }

        while (n != 0) {
            n >>= 1;
            count++;
        }

        if (signed && negative)
            return(count + 1);
        else
            return(count);
    }
    
    /**
     * Convert an integer to a gray code, returning it as a hexadecimal string.
     * @param   v is the integer
     * @param   bits is the number of bits
     * @return  the gray code string.
     */
    public static String intToGrayString (int v, int bits) {
        StringBuffer    sb = new StringBuffer();
        boolean[]       b = new boolean[bits];
        boolean[]       g = new boolean[bits];
        int             i;
        for (i=0 ; i<bits ; i++,v>>=1)
            b[i] = ((v & 1) != 0);
        g[bits-1] = b[bits-1];
        for (i=bits-2 ; i>=0 ; i--)
            g[i] = b[i] ^ b[i+1];
        for (i=0 ; i<bits ; i++)
            sb.append(g[bits-1-i] ? '1' : '0');
        return(binToHex(sb));
    }

    /**
     * Evaluate a string as an integer or the name of a variable
     * returning an integer.
     * This is only used for declaration integer values in type array
     * dimensions and data widths, so the exception messages reflect that.
     * If the string is null or empty, return the value 0.
     * @param   s is the string to be evaluated
     * @param   mess is a string to include in an exception string
     * @param   loc is the source file location
     * @return  the integer value
     */
    public static int intFromString (String s, String mess, SrcLoc loc) {
        int     n;
        
        if ((s == null) || s.equals(""))
            return(0);
            
        try {
            n = Long.decode(s).intValue();
        } catch (NumberFormatException e) {       
            Ident   ident = new Ident(s);
            Context sc = ident.getScopeContext();
            s = ident.getId();        
            Var var = ThreePL.findVar(s, sc, loc);
            if (var == null)
                throw new ExEx("variable '" + s + "' in type " + mess + " not found", loc);
            Val rv = var.getVal(null, new NodeList(null), loc);
            if (rv.getPrimType() != Ptype.INT)
                throw new ExEx("type " + mess + " not integer", loc);
            if (rv.isTarget())
                throw new ExEx("type " + mess + " not immediate", loc);
            if (rv.getDimWords() != 1)
                throw new ExEx("type " + mess + " not single value", loc);
            n = (int)rv.getSingleIval(loc);
        }
        if (n < 0)
            throw new ExEx("type " + mess + " negative", loc);
        return(n);
    }
    
    /**
     * Extract the sign, biased exponent and mantissa from a double
     * and re-pack in a new floating point format with specified biased exponent and
     * mantissa sizes.
     * @param   d the double to be converted
     * @param   mant is the required mantissa width
     * @param   bexp is the required biased exponent width
     * @return  the repacked floating format within an integer type
     */
    public static long longFromDouble (double d, int mant, int bexp) {
        if (d == 0.0)
            return((long)0);
        long    l = Double.doubleToLongBits(d);
        long    m = l & 0xfffffffffffffL;// IEEE754 mantissa - 52 bit
        long    e = (l >> 52) & 0x7ff;   // IEEE754 biased exponent - 11 bit
        long    sign = (l >> 63) & 1;    // sign bit
        long    m_mask = (1 << mant) - 1;
        long    e_mask = (1 << bexp) - 1;
        e = (e - 0x3ff) & 0x7ff; // convert to 2's complement
        if ((e & 0x400) != 0) // if exponent sign bit is 1
            e |= 0xfffffffffffff800L; // propagate the sign
        m = (m >> (52 - mant)) & m_mask;
        e = (e + (1 << (bexp - 1)) - 1) & e_mask;
        return((sign << (bexp + mant)) | (e << mant) | m);
    }

    /**
     * Convert a binary string to a hexadecimal string.
     * @param   b is the binary string
     * @return  a hexadecimal string
     */
    public static String binToHex(StringBuffer b) {
        StringBuffer    sb = new StringBuffer();
        int len = b.length();
        int missing = 3 - (len - 1) % 4;
        while (missing > 0) {
            b.insert(0, '0');
            missing--;
        }
        for (int i=0 ; i<len ; i+=4) {
            int n = Integer.parseInt(b.substring(i, i+4), 2);
            sb.append(Integer.toHexString(n));
        }
        return(sb.toString());
    }

    /**
     * Convert a binary string to a hexadecimal string of specified length.
     * @param   b is the binary string
     * @param   len is the hexadecimal string length
     * @return  a hexadecimal string
     */
    public static String binToHex(StringBuffer b, int len) {
        StringBuffer    sb = new StringBuffer();
        int blen = b.length();
        int missing = 3 - (blen - 1) % 4;
        while (missing > 0) {
            b.insert(0, '0');
            missing--;
        }
        for (int i=0 ; i<blen ; i+=4) {
            int n = Integer.parseInt(b.substring(i, i+4), 2);
            sb.append(Integer.toHexString(n));
            len--;
        }
        while (len-- != 0)
            sb.insert(0, '0');
        return(sb.toString());
    }

    /**
     * Convert a binary string to a hexadecimal string.
     * @param   b is the binary string
     * @return  a hexadecimal string
     */
    public static String binToHex(String b) {
        StringBuffer    sb = new StringBuffer();
        int len = b.length();
        int missing = 3 - (len - 1) % 4;
        while (missing > 0) {
            b = "0" + b;
            missing--;
        }
        for (int i=0 ; i<len ; i+=4) {
            int n = Integer.parseInt(b.substring(i, i+4), 2);
            sb.append(Integer.toHexString(n));
        }
        return(sb.toString());
    }

    /**
     * Convert a hexadecimal string to a binary string.
     * Leading binary zeros are removed.
     * @param   v is the hexadecimal string
     * @return  a binary string
     */
    public static String hexToBin (String v) {
        StringBuffer    bs = new StringBuffer();
        for (int i=v.length()-1 ; i>=0 ; i--) {
            switch (v.charAt(i)) {
            case '0':
                bs.insert(0, "0000");
                break;
            case '1':
                bs.insert(0, "0001");
                break;
            case '2':
                bs.insert(0, "0010");
                break;
            case '3':
                bs.insert(0, "0011");
                break;
            case '4':
                bs.insert(0, "0100");
                break;
            case '5':
                bs.insert(0, "0101");
                break;
            case '6':
                bs.insert(0, "0110");
                break;
            case '7':
                bs.insert(0, "0111");
                break;
            case '8':
                bs.insert(0, "1000");
                break;
            case '9':
                bs.insert(0, "1001");
                break;
            case 'a':
            case 'A':
                bs.insert(0, "1010");
                break;
            case 'b':
            case 'B':
                bs.insert(0, "1011");
                break;
            case 'c':
            case 'C':
                bs.insert(0, "1100");
                break;
            case 'd':
            case 'D':
                bs.insert(0, "1101");
                break;
            case 'e':
            case 'E':
                bs.insert(0, "1110");
                break;
            case 'f':
            case 'F':
                bs.insert(0, "1111");
                break;
            default:
                return(null);
            }
        }
        while ((bs.length() > 1) && (bs.charAt(0) == '0'))
            bs.deleteCharAt(0); // remove leading zeros
        return(bs.toString());
    }
    
    /**
     * Adjust the length of a binary string. To shorten truncate
     * at the left end
     * @param   s is the input expression string
     * @param   len is the required length
     * @return  a 4-digit hexadecimal LUT initialisation string
     */
    public String adjustBin (String s, int len) {
        int slen = s.length();
        if (slen == len)
            return(s);
        if (slen > len)
            return(s.substring(slen-len, slen));
        else
            return(zeropad(len - slen) + s);
    }
    
    /**
     * For an integer 1, 2, 3, 4 etc return "1st", "2nd", "3rd", "4th" etc.
     * @param   i is the integer to be described
     * @return  the description string
     */
    public static String whichArg (int i) {
        switch (i) {
        case 1:
            return("1st");
        case 2:
            return("2nd");
        case 3:
            return("3rd");
        }
        return(Integer.toString(i) + "th");
    }
    
    /**
     * Get the sorted map key corresponding to the nth entry.
     * @param   tm is map
     * @param   index is the map entry index
     * @param   id is the map variable identifier for error messages
     * @param   loc is the source file location
     * @return  the key string
     */
    public static String mapKeyFromIndex (TreeMap<String, Val> tm, int index, String id, SrcLoc loc) {
        if (index < 0)
            throw new ExEx("map index negative - " + id + "[" + index + "]", loc);
        else if (index >= tm.size())
            throw new ExEx("map index too large - " + id + "[" + index + "]", loc);
        Set<String>         ks = tm.keySet();
        Iterator<String>    it = ks.iterator();
        while (index-- > 0)
            it.next();
        return(it.next());
    }
    
    /**
     * Concatenate a string a specified number of times.
     * @param s is the string to be concatenated with itself
     * @param n is the number of concatenated copies of s
     * @return the space string
     */
    public static String concat (String s, int n) {
        StringBuffer    sb = new StringBuffer();
        while (n-- > 0)
            sb.append(s);
        return(sb.toString());
    }

    /**
     * Convert an expression string into a LUT initialisation
     * string. Expressions contain I0, I1 ... etc for input arguments,
     * {@literal '|' or '+' for OR, '\&' or '*' for AND, '^' or '@' for XOR, '!'}
     * or '~' for NOT and constants 0 or 1. Subexpressions may be
     * parenthesised.
     * @param   s is the input expression string
     * @param   lutwidth is the maximum number of input arguments
     * @return  a 4-digit hexadecimal LUT initialisation string
     */
    /*
     * Recursive descent parser for LUT expressions.
     * Grammar:
     *	expr ::= term { (OR | XOR) term }
     *	term ::= negation { AND negation }
     *	negation ::= { NOT } factor
     *	factor ::= ( expr ) | ARG | CON
     *	OR ::= '+' | '|'
     *	AND ::= '*' | '&'
     *	XOR ::= '^' | '@'
     *	NOT ::= '!' | '~'
     *	ARG ::= 'I0' | 'I1' | 'I2' | 'I3' etc.
     *	CON ::= '0' | '1'
     */
    public static String bit_pattern (String s, int lutwidth) {
        long    v = 0;
        BENode  exp = null;
        
        sbuf = s + "\0";
        in = s;
        index = 0;
        nargs = 0;
        args = new int[lutwidth];
        
        maxwidth = lutwidth;
        token = get_token();
        exp = expr(exp);
        if (token != Ttype.END)
            throw new RuntimeException("bit_pattern(): incorrect expression -  " + in);
        int lim = 1 << nargs;
            for (int i=0 ; i<lim ; i++) {
                for (int j=0 ; j<lutwidth ; j++)
                    args[j] = (i >> j) & 1;
                v |= (bit_eval(exp) << i);
        }
        String  vs = Long.toHexString(v);
        int     initlength = (1 << lutwidth) / 4; // number of hex digits
        while (vs.length() < initlength)
            vs = "0" + vs;
        return(vs);
    }

    private static BENode expr (BENode n) {
        n = term(n);
        while ((token == Ttype.OR) || (token == Ttype.XOR)) {
            n = new BENode(token, n);
            token = get_token();
            n.rlink = term(null);
        }
        return(n);
    }

    private static BENode term (BENode n) {
        n = negation(n);
        while (token == Ttype.AND) {
            n = new BENode(token,n);
            token = get_token();
            n.rlink = negation(null);
        }
        return(n);
    }

    private static BENode negation (BENode n) {
        if (token == Ttype.NOT) {
            BENode q = new BENode(token);
            token = get_token();
            q.llink = factor(n);
            return(q);
        } else
            return(factor(n));
    }

    private static BENode factor (BENode n) {
        if (token == Ttype.LPAR) {
            token = get_token();
            BENode q = expr(n);
            if (token != Ttype.RPAR)
                throw new RuntimeException("bit_pattern(): incorrect expression -  " + in);
            token = get_token();
            return(q);
        } else if ((token != Ttype.ARG) && (token != Ttype.CON))
            throw new RuntimeException("bit_pattern(): incorrect expression -  " + in);
        else {
            BENode q = new BENode(token);
            q.argnum = argnum;
            token = get_token();
            return(q);
        }
    }

    private static Ttype get_token () {
        while (sbuf.charAt(index) == ' ' || sbuf.charAt(index) == '\t')
            index++;	/* ignore white space */

        switch (sbuf.charAt(index++)) {
        case '*':
        case '&':
            return(Ttype.AND);
        case '+':
        case '|':
            return(Ttype.OR);
        case '^':
        case '@':
            return(Ttype.XOR);
        case '!':
        case '~':
            return(Ttype.NOT);
        case '(':
            return(Ttype.LPAR);
        case ')':
            return(Ttype.RPAR);
        case '0':
            argnum = 0;
            return(Ttype.CON);
        case '1':
            argnum = 1;
            return(Ttype.CON);
        case 'I':
            argnum = Character.digit(sbuf.charAt(index++), 10);
            if ((argnum < 0) || (argnum >= maxwidth))
                return(Ttype.ERR);
            if (nargs <= argnum)
                nargs = argnum + 1;
            return(Ttype.ARG);
        case '\0':
            return(Ttype.END);
        default:
            return(Ttype.ERR);
        }
    }

    private static long bit_eval (BENode n) {
        if (n.op == Ttype.ARG)
            return(args[n.argnum]);
        else if (n.op == Ttype.CON)
            return(n.argnum);
        else if (n.op == Ttype.OR)
            return(bit_eval(n.llink) | bit_eval(n.rlink));
        else if (n.op == Ttype.XOR)
            return(bit_eval(n.llink) ^ bit_eval(n.rlink));
        else if (n.op == Ttype.AND)
            return(bit_eval(n.llink) & bit_eval(n.rlink));
        else if (n.op == Ttype.NOT)
            return(bit_eval(n.llink) ^ 1);
        else
            return(0);
    }
    
    /**
     * Static method to scan a line of text guided by a very simple format string
     * and assign extracted tokens to variables of matching type.
     * The format string is a string of single characters, one per conversion. Allowed
     * conversions are -
     *  'd' integer
     *  'u' unsigned integer
     *  'x' hexadecimal integer
     *  'o' octal integer
     *  'f' floating point
     *  's' string
     *  '.' convert according to the variable type
     *  '*' skip the token
     *  ' ' ignored
     *  '\t' ignored
     * The format string must not contain any other characters.
     * The number of actual conversions (i.e. not including the skip) must be equal to
     * the number of output variables.
     * 
     * The format argument may be null or "" in which case tokens will be converted and assigned
     * to the variables based on the variable types.
     *
     * Tokens that are to be converted as hexadecimal values must not have a leading 0x. Similarly
     * a leading 0 will not be interpreted as indicating an octal value. The conversion character
     * alone decides which radix will be used.
     * 
     * The input line must consist of fields (tokens) separated by runs of "," or ' '
     * i.e. [, ]+
     * @param line is the input text line
     * @param fmt is a simple format string, one character per conversion
     * @param refs is an array of output variable references
     * @param msg is a leading string to prepend any exception messages
     * @param loc is the source file location
     */
    public static void scan (String line, String fmt, Ref[] refs, String msg, SrcLoc loc) {
        ArrayList<Character>  conversions = new ArrayList<Character>();
        
        // If no format string, construct one containing '.' for each output variable.
        if ((fmt == null) || (fmt.length() == 0)) {
            fmt = "";
            for (int i=0 ; i<refs.length ; i++)
                fmt = fmt + '.';
        }
        
        // Traverse format string extracting conversion characters into a list.
        int j = 0;
        for (int i=0 ; i<fmt.length() ; i++) {
            char    c = fmt.charAt(i);
            switch (c) {
            case '*':
                conversions.add('*');
                break;
            case '.':
                Ptype ptype = refs[j].getPrimType();
                switch(ptype) {
                case INT:
                    conversions.add('d');
                    break;
                case UINT:
                    conversions.add('u');
                    break;
                case FLOAT:
                    conversions.add('f');
                    break;
                case STR:
                    conversions.add('s');
                    break;
                default:
                    throw new ExEx(msg + " - output argument not allowed type, '" + refs[i].getId() + "', type " + ptype.typename(), loc);
                }
                j++;
                break;
            case 'd':
            case 'u':
            case 'x':
            case 'o':
            case 'f':
            case 's':
                conversions.add(c);
                j++;
                break;
            case ' ':
            case '\t':
                break;
            default:
                throw new ExEx(msg + " - illegal format character '" + c + "'", loc);
            }
        }
        
        // Check that the conversions list is the same size as the number of output variables.
        if (conversions.size() != refs.length)
            throw new ExEx(msg + " - output argument count does not match number of format conversions", loc);
        
        Scanner sc = new Scanner(line);
        sc.useDelimiter("[, ]+");
        int i = 0;
        for (char c : conversions) {
            convert(refs[i], c, sc, msg, loc);
            i++;
        }
        sc.close();
    }
    
    private static void convert (Ref ref, char cc, Scanner sc, String msg, SrcLoc loc) {
        long    l;
        double  f;
        
        switch (cc) {
        case 'd':
            if (ref.getPrimType() != Ptype.INT)
                throw new ExEx(msg + " - output argument type for conversion character 'd' is not \"int\"", loc);
            try {
                l = sc.nextLong();
            } catch (InputMismatchException e) {
                throw new ExEx(msg + " - format conversion character 'd' does not match input", loc);
            } catch (NoSuchElementException e) {
                throw new ExEx(msg + " - no more input, conversion character 'd'", loc);
            }
            ref.assignTo(AST.IMASS, new Val(l, loc), loc);
            break;
        case 'u':
            if (ref.getPrimType() != Ptype.UINT)
                throw new ExEx(msg + " - output argument type for conversion character 'u' is not \"uint\"", loc);
            try {
                l = sc.nextLong();
                if (l < 0)
                    throw new ExEx(msg + " - 'u' format conversion has negative result", loc);
            } catch (InputMismatchException e) {
                throw new ExEx(msg + " - format conversion character 'u' does not match input", loc);
            } catch (NoSuchElementException e) {
                throw new ExEx(msg + " - no more input, conversion character 'u'", loc);
            }
            ref.assignTo(AST.IMASS, new Val(l, loc), loc);
            break;
        case 'x':
            if ((ref.getPrimType() != Ptype.INT) && (ref.getPrimType() != Ptype.UINT))
                throw new ExEx(msg + " - output argument type for conversion character 'x' is not \"int\" or \"uint\"", loc);
            try {
                l = sc.nextLong(16);
            } catch (InputMismatchException e) {
                throw new ExEx(msg + " - format conversion character 'x' does not match input", loc);
            } catch (NoSuchElementException e) {
                throw new ExEx(msg + " - no more input, conversion character 'x'", loc);
            }
            ref.assignTo(AST.IMASS, new Val(l, loc), loc);
            break;
        case 'o':
            if ((ref.getPrimType() != Ptype.INT) && (ref.getPrimType() != Ptype.UINT))
                throw new ExEx(msg + " - output argument type for conversion character 'o' is not \"int\" or \"uint\"", loc);
            try {
                l = sc.nextLong(8);
            } catch (InputMismatchException e) {
                throw new ExEx(msg + " - format conversion character 'o' does not match input", loc);
            } catch (NoSuchElementException e) {
                throw new ExEx(msg + " - no more input, conversion character 'o'", loc);
            }
            ref.assignTo(AST.IMASS, new Val(l, loc), loc);
            break;
        case 'f':
            if (ref.getPrimType() != Ptype.FLOAT)
                throw new ExEx(msg + " - output argument type for conversion character 'f' is not \"float\"", loc);
            try {
                f = sc.nextDouble();
            } catch (InputMismatchException e) {
                throw new ExEx(msg + " - format conversion character 'f' does not match input", loc);
            } catch (NoSuchElementException e) {
                throw new ExEx(msg + " - no more input, conversion character 'f'", loc);
            }
            ref.assignTo(AST.IMASS, new Val(f, loc), loc);
            break;
        case 's':
            if (ref.getPrimType() != Ptype.STR)
                throw new ExEx(msg + " - output argument type for conversion character 'u' is not \"str\"", loc);
           ref.assignTo(AST.IMASS, new Val(sc.next(), loc), loc);
           break;
        case '*':
            sc.next();
            break;
        default:
            throw new ExEx(msg + " - unknown format conversion character '" + cc + "'", loc);
        }
    }
}
