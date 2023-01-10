
package threepl.netlist;

import java.util.ArrayList;

import threepl.codegen.TDEConstants;
import threepl.exceptions.ExEx;
import threepl.parser.Constant;

/**
 * <p>This class contains general methods for handling nets, net and signal
 * variable arguments and lists, net interconnection and some string
 * formatting methods. The 'args' methods convert single Net arguments or
 * an array of Net into an ArrayList so that these functions can be called
 * with these argument types without having 3 different calling signatures
 * for each function.</p>
 *
 * <p>This method is also the super class for EDIF code generation.</p>
 *
 * <p>The arguments and return values are not a consistent scheme
 * through all elements.</p>
 *
 * <pre>
 * The class structure is -
 *
 *        TDECode           implementation of TDE modules - vendor independent
 *           |
 *      GenFunctions        general support methods - vendor independent
 *           |
 *       XElements          Xilinx general unified elements
 *           |
 *       XFunctions         Xilinx higher level functions
 *           |
 *       XTDECode           Xilinx implementation of TDE modules
 *           |
 *     ----------------------------
 *    /   /  |   \   \      \
 * XC2S XCV XC3S XC2V XC2VP XC4V ......     Xilinx elements specific to FPGA families
 * </pre>
 *
 * All classes above starting with "X" are in subdirectory "xilinx".
 *
 * <p>All methods in the lowest subclasses override corresponding
 * methods in class XElements which throw exceptions to pick up
 * the use of special elements in FPGA families which do not
 * have them.</p>
 *
 * <p>TDE code generation methods are invoked using the FPGA
 * family as the object instance.</p>
 */
public abstract class GenFunctions extends TDECode implements Constant, TDEConstants {

    /* NO LONGER USED!
    // Convert a Net argument to an ArrayList.
    public static ArrayList args (Net i0) {
        ArrayList   a = new ArrayList();
        a.add(i0);
        return(a);
    }

    // Convert 2 Net arguments to an ArrayList.
    public static ArrayList args (Net i0, Net i1) {
        ArrayList   a = new ArrayList();
        a.add(i0);
        a.add(i1);
        return(a);
    }

    // Convert 3 Net arguments to an ArrayList.
    public static ArrayList args (Net i0, Net i1, Net i2) {
        ArrayList   a = new ArrayList();
        a.add(i0);
        a.add(i1);
        a.add(i2);
        return(a);
    }

    // Convert 4 Net arguments to an ArrayList.
    public static ArrayList args (Net i0, Net i1, Net i2, Net i3) {
        ArrayList   a = new ArrayList();
        a.add(i0);
        a.add(i1);
        a.add(i2);
        a.add(i3);
        return(a);
    }
    */
    
    /**
     * Convert an array of Net arguments to an ArrayList.
     * @param   a is an array of type Net.
     * @return  an ArrayList of type Net
     */
    public static ArrayList<Net> args (Net[] a) {
        ArrayList<Net>   al = new ArrayList<Net>();
        int         n = a.length;
        
        for (int i=0 ; i<n ; i++)
            al.add(a[i]);
        return(al);
    }
    
    /**
     * Pad a net array at the MS end, if required, to expand it to the
     * specified size. If the input net array is already the specified
     * size it is simply returned. If padding is required it will be
     * zeros for unsigned or the sign bit of the input if signed.
     * @param   s is the input net array
     * @param   n is the required size
     * @param   signed is true if the array is signed
     * @return  the expanded net array
     */
    public static Net[] sig_expand (Net[] s, int n, boolean signed) {
        int     ssize = s.length;
        if (ssize >= n)
            return(s);
        Net[]   o = new Net[n];
        for (int i=0 ; i<n ; i++) {
            if (i < ssize)
                o[i] = s[i];
            else if (signed)
                o[i] = s[ssize-1];
            else
                o[i] = Net.LO;
        }
        return(o);
    }
    
    /**
     * Expand a single net to a net array.
     * If the last argument is true the net is duplicated
     * into an array of the specified size.
     * If the last argument is false the array is padded
     * with Net.LO.
     * @param   s is the input net
     * @param   n is the required size
     * @param   dupl is true the net is duplicated
     * @return  the expanded net array
     */
    public static Net[] sig_expand (Net s, int n, boolean dupl) {
        Net[]   o = new Net[n];
        o[0] = s;
        for (int i=1 ; i<n ; i++) {
            if (dupl)
                o[i] = s;
            else
                o[i] = Net.LO;
        }
        return(o);
    }
    
    /**
     * Return a subarray of an array of Net.
     * @param   a is the the subject array
     * @param   l is the lower subscript of the subarray
     * @param   u is the upper subscript of the subarray
     * @return  the subarray
     */
    public static Net[] subarray (Net[] a, int l, int u) {
        Net[]   sa = new Net[u-l+1];
        int     j = 0;
        for (int i=l ; i<=u ; )
            sa[j++] = a[i++];
        return(sa);
    }

    /**
     * Convert an integer into an 4-digit hexadecimal string.
     * @param   v is the integer
     * @return  the hexadecimal string
     */
    public static String hex4string (long v) {
        String  s = Long.toHexString(v);
        switch (s.length()) {
        case 1:
            return("000" + s);
        case 2:
            return("00" + s);
        case 3:
            return("0" + s);
        }
        return(s);
    }

    /**
     * Pad or truncate a string at the left end to get n characters.
     * @param   s is the string
     * @param   n is the required length
     * @return  the 4 character string
     */
    public static String stringsize (String s, int n) {
        if (s.length() == n)
            return(s);
        if (s.length() > n)
            return(s.substring(s.length() - n));
        for (n-=s.length() ; n > 0 ; n--)
            s = "0" + s;
        return(s);
    }

    /**
     * Convert an integer into an 8-digit hexadecimal string.
     * @param   v is the integer
     * @return  the hexadecimal string
     */
    public static String hex8string (long v) {
        String  s = Long.toHexString(v);
        switch (s.length()) {
        case 1:
            return("0000000" + s);
        case 2:
            return("000000" + s);
        case 3:
            return("00000" + s);
        case 4:
            return("0000" + s);
        case 5:
            return("000" + s);
        case 6:
            return("00" + s);
        case 7:
            return("0" + s);
        case 8:
            return(s);
        }
        return(s);
    }

    /**
     * Get a bit from a hexadecimal string.
     * @param   sinit is the hexadecimal string
     * @param   ibit is the index of the bit, 0 being the LSB (rightmost)
     * @return  the selected bit, 0 or 1
     */
    public static int strhexbit(String sinit, int ibit) {
        int     cindex = ibit / 4;
        int     cbit = ibit % 4;
        if (cindex >= sinit.length())
            return(0);
        cindex = sinit.length() -1 -cindex;
        String  c = sinit.substring(cindex, cindex+1);
        int     l = Integer.parseInt(c, 16);
        return((l >> cbit) & 1);
    }
    
    /**
     * Get a constant net array from a hexadecimal string.
     * @param   sval is the hexadecimal string
     * @param   width is the number of bits required
     * @return  the net array
     */
    public static Net[] hexConstant (String sval, int width) {
        Net[]   o = new Net[width];
        for (int bit=0 ; bit<width ; bit++) {
            if (strhexbit(sval, bit) != 0)
                o[bit] = Net.HI;
            else
                o[bit] = Net.LO;
        }
        return(o);
    }
    
    /**
     * Get a constant net array from a long.
     * @param   val is the long value
     * @param   width is the number of bits required
     * @return  the net array
     */
    public static Net[] constant (long val, int width) {
        Net[]   o = new Net[width];
        for (int bit=0 ; bit<width ; bit++) {
            if (((val >> bit) & 1) != 0)
                o[bit] = Net.HI;
            else
                o[bit] = Net.LO;
        }
        return(o);
    }
    
    /**
     * Connect two Net arrays of equal size.
     * @param   a is the 1st Net array
     * @param   b is the 2nd Net array
     */
    public static void connect (Net[] a, Net[] b) {
        if (a.length != b.length) {
            System.out.println("1st " + a[0].getEDIFIdent() + " width " + a.length);
            System.out.println("2nd " + b[0].getEDIFIdent() + " width " + b.length);
            throw new ExEx("code generation error - connect widths different");
        }
        int     n = a.length;
        for (int i=0 ; i<n ; i++)
            a[i].connect(b[i]);
    }
    
    /**
     * Connect two Net arrays with truncation or padding.
     * If the destination Net array is smaller than the
     * source Net array the most significant source Nets are ignored
     * (truncation). If the destination Net array is larger
     * than the source Net array the extra destination bits are
     * connected to GND if there is no sign extension, or to
     * the most significant source Net array bit if sign extension
     * is required (padding).
     * @param   dest is the destination Net array
     * @param   src is the source Net array
     * @param   signextend is true if sign extension is required
     */
    public static void connect (Net[] dest, Net[] src, boolean signextend) {
        int     n = dest.length;
        Net     e = signextend ? src[src.length-1] : Net.LO;
        for (int i=0 ; i<n ; i++)
            if (i >= src.length)
                dest[i].connect(e);
            else
                dest[i].connect(src[i]);
    }
}
