/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */
package threepl.simulator;




/**
 * A SimInteger is a SimNumber containing a non-floating point number. It can contain
 * a signed or unsigned integer or fixed point number.
 * 
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
@SuppressWarnings("all")
public abstract class SimInteger extends SimNumber {

    // The value 2 to the power 32, as a constant
    protected static double BIT32 = 2*(((double)Integer.MAX_VALUE)+1);
    
    /**
     * Add another SimInteger to this.
     * Resize src to width of 'this' as we assume 'this' is big enough to hold the result.
     * If not, upper bits are lost.
     * @param src The SimInteger to add
     * @return this SimInteger
     */
    public SimValue add(SimValue src) {

        // Check src is a SimInteger
	checkType(src);
    
        SimInteger v = (SimInteger)((SimInteger) src).resize(m, n);
        for (int i = 0, carry = 0; i < data.length; i++)
            carry = addWithCarry(i, v.getDataWord(i), carry);

        return this;
    }

    /**
     * Cast another SimValue into this
     * @param src SimValue to cast to this
     * @param sext If true, do sign extension, else zero pad (if relevant)
     * @return this SimValue
     */
    public SimValue cast (SimValue src, boolean sext) {
    
       // Check can cast
       checkCast(src);

       int w = getWidth();
       int ws = src.getWidth();

       // If src is a SimInteger, do a put..
       if (src instanceof SimInteger) {
       
          put(src);
	  
	  // If the non-fractional part of this is wider than the non-fractional part
	  // of src and sext is false, zero pad upper bits
          SimInteger srci = (SimInteger)src;
	  if (m > srci.m && !sext)
	     for (int i=n+srci.m; i<w; i++)
	        put(false, i);        
       }
       
       // ..else src is not a SimInteger, just do bit copy
       else {       
       
          // Copy common bits
          put(src, Math.min(w, ws), 0, 0);
       
          // If this is wider than src, zero pad or sign extend according to sext
          if (w>ws) {
             boolean msb = sext ? src.getBit(ws-1) : false;	  
             for (int i=ws; i<w; i++)
	        put(msb, i);
          }	
       }   
       return this;	     
    }
    
    /**
     * Divide another SimInteger into this
     * @param divisor The SimInteger to divide by
     * @return this SimInteger
     */
    public SimValue div(SimValue divisor) {

       // Check divisor is a SimInteger
       checkType(divisor);

       divrem(divisor);
       return this;
    }

   /**
     * Divide (unsigned) this by another SimInteger, generate remainder also.
     * Quotient is left in this, remainder is returned.
     * @param dor The SimInteger to divide by
     * @return A SimInteger containing the remainder of the division
     */
   public SimValue divrem(SimValue dor) {

      // Check dor is a SimInteger
      checkType(dor);
      
      // Handle some simple cases..
      
      // Divide by zero
      if (((SimInteger)dor).eqZero())
         throw new SimException("divide by zero");
      
      // Divisor == dividend
      if (eq(dor)) {
         put(1);
	 return duplicate().clear();
      }

      // A dividend of a bits and a divisor of b bits results in a quotient
      // of up to a bits and remainder of up to b bits. Resize dividend and divisor
      // and create quotient and remainder to all be the same precision
      // and to accommodate the wider of the dividend and divisor, shifted left by
      // nWider bits. The extra nWider bits are needed so the dividend can be
      // left shifted by nWider bits before the divide, to give nWider binary places
      // precision in the quotient and remainder.
      int nWider = getNWider((SimInteger)dor);      
      int mWider = getMWider((SimInteger)dor) + nWider;      
      SimInteger dividend = (SimInteger) resize(mWider, nWider);
      dividend.shift(nWider);
      SimInteger divisor = (SimInteger)(((SimInteger)dor).resize(mWider, nWider));      
      SimInteger quotient = (SimInteger) create(getType(), mWider, nWider);
      SimInteger remainder = (SimInteger) create(getType(), mWider, nWider);
      SimInteger t = (SimInteger) create(getType(), mWider, nWider);
      int nbits = dividend.getWidth();
      
      // Divisor > dividend
      if (divisor.gt(dividend)) {
         clear();
         return dividend.shift(-nWider).resize(m, n);
      }
       
      // If quantities are <= 32 bits wide, simple integer divide..
      if (nbits <= S) {
         quotient.putDataWord(0, dividend.getInt() / divisor.getInt());
	 put(quotient);
	 remainder.putDataWord(0, dividend.getInt() % divisor.getInt());
	 return remainder.shift(-nWider).resize(m, n);
      }

      // ..else shift and subtract algorithm
      int num_bits = nbits;
      
      while (remainder.lt(divisor)) {
	 remainder.shift(1).putBit(dividend.getBit(nbits-1), 0);
	 t.put(dividend);
	 dividend.shift(1);
	 num_bits--;
      }      	 
      dividend.put(t);
      remainder.shift(-1);
      num_bits++;
      
      boolean q;	 
      for (int i = 0; i < num_bits; i++) {
	 remainder.shift(1).putBit(dividend.getBit(nbits-1), 0);
         t.put(remainder).sub(divisor);
	 q = !t.getBit(nbits-1);
	 dividend.shift(1);
	 quotient.shift(1).putBit(q, 0);
	 if (q)
	    remainder.put(t);
      }
      put(quotient);      
      return remainder.shift(-nWider).resize(m, n);	    
   }

    /**
     * Return value as a double
     * @return The SimInteger value as a double
     */
   public double getDouble() {
    
      // Get abs value
      int s;
      SimInteger v = (SimInteger)duplicate();     
      if ((s = sign()) < 0)
         v.twosComplement();

      // Accumulate unsigned integers
      double f = 0, factor = 1;
      for (int i=0; i<data.length; i++) {
         f += factor * (((long)v.getDataWord(i)) & 0xFFFFFFFFL);
	 factor *= BIT32;
      }
       
      // Put sign back
      if (s < 0)
         f = -f;
	  
      // Scale down by fixed precision
      return f / Math.pow((double)2, (double)n);	  
   }      

    /**
     * Return low order 32 bits to the left of binary point, as an int.
     * @return The low order 32 bits to the left of binary point, as an int
     */
    public int getInt() {
        return resize(S, 0).getDataWord(0);
    }

    /**
     * Return true is the src SimValue is connectable with this.
     * Must be a SimInteger with same precision. Note this allows compatibility
     * between SimSigned and SimUnsigned quantities.
     * @param src The SimValue to test for compatibility
     * @return True if src is connectable with this
     */
    public boolean isConnectable (SimValue src) {
       return (src instanceof SimInteger) &&
              m == ((SimInteger)src).m &&
              n == ((SimInteger)src).n;
    }

    /**
     * Test whether this is less than another SimValue
     * Resize the smaller of 'this' and src to the width of the larger before the
     * comparison. This allows comparison of different width quantities.
     * @param src The SimInteger to compare to this
     * @return True if this is less than the other SimInteger
     */
    public boolean lt(SimValue src) {

       // Check src is a SimInteger
       checkType(src);
    
       int i;
       long jl, kl;
       int mWider = getMWider((SimInteger)src);
       int nWider = getNWider((SimInteger)src);		
       SimInteger vthis = (SimInteger) resize(mWider, nWider);
       SimInteger vsrc = (SimInteger)(((SimInteger) src).resize(mWider, nWider));

       for (i = vthis.data.length - 1; i >= 0; i--) {
           jl = (long)vthis.getDataWord(i) & 0xFFFFFFFFL;
           kl = (long)vsrc.getDataWord(i) & 0xFFFFFFFFL;

           if (jl < kl) {
               return true;
           } else if (jl > kl) {
               return false;
           }
       }

       return false;
    }

    /**
     * Multiply this by another SimInteger.
     * We assume 'this' is big enough to hold the result. If not, upper bits are lost.
     * @param src The SimInteger to multiply by
     * @return this SimInteger
     */
    public SimValue mul(SimValue src) {

       // Check src is a SimInteger
       checkType(src);

       SimInteger multiplier = (SimInteger) src;
       int multWidth = multiplier.getWidth();

       // If multiplier and multiplicand are <= 32 bits wide, simple integer multiply
       if ((multWidth <= S) && (getWidth() <= S)) {
           return put(multiplier.getInt() * getInt());
       }

       // Otherwise, Booth's algorithm. Note we treat as raw integers during multiply
       SimInteger product = (SimInteger) create(getType(),
	                    multiplier.m + multiplier.n + m + n, 0); 
       SimInteger multiplicand = (SimInteger) duplicate();
       multiplicand.m = m + n;
       multiplicand.n = 0;
       boolean sequence = false;

       for (int b = 0; b < multWidth; b++) {
           if (multiplier.testBit(b)) {
               if (!sequence) {
                   sequence = true;
                   product.sub(multiplicand);
               }
           } else if (sequence) {
               sequence = false;
               product.add(multiplicand);
           }

           multiplicand.shift(1);
       }

       // Put in binary point
       int w = product.getWidth();
       product.n = multiplier.n + n;
       product.m = w - product.n;

       // Resize to precision of this
       return put(product.resize(m, n));
    }

    /**
     * Return formatted value
     * @param fmt A printf style format string, or null
     * @return The formatted value
     */
    public String print (String fmt) {
    
       String s;
       
       // If no format, use default format..
       if (fmt==null || fmt=="")
          return toString();
	  
       // ..else try to format accordingly..
       
       // Analyze fmt string to determine if x, X, o, b format.
       // If so, build formatted numeric string word by word,
       // then modify for flags, field width and precision as
       // required.
       
       // Walk through the fmt and pick out the elements
       StringBuffer preconvb = new StringBuffer();
       StringBuffer flagsb = new StringBuffer();
       StringBuffer fieldwidthb = new StringBuffer();
       StringBuffer periodb = new StringBuffer();
       StringBuffer precisionb = new StringBuffer();
       StringBuffer lengthmodb = new StringBuffer();
       StringBuffer convcharb = new StringBuffer();
       StringBuffer postconvb = new StringBuffer();
       int state = 0;
       for (int i=0; i<fmt.length(); i++) {
	  char c = fmt.charAt(i);
	  switch (state) {
	  // Looking for pre-conversion spec chars
	  case 0:	        
	     if (c!='%' || (i==fmt.length()-1 ||
		 (i>0 && fmt.charAt(i-1)=='%') || fmt.charAt(i+1)=='%')) {
		// Any non-% char or a % at the end of the string, or %%
		preconvb.append(c);
		break;
             }
             state = 1;
	     // We skip the % at the start of the conversion spec
	     break;
	  // Looking for flags
	  case 1:
	     if (c=='-'||c=='+'||c==' '||c=='0'||c=='#') {
		flagsb.append(c);
		break;
             }		   
             state = 2;
          // Looking for field width
          case 2:
	     if (c>='0' && c<='9') {
		fieldwidthb.append(c);
		break;
             }
	     state = 3;
          // Looking for period
          case 3:				   
             if (c=='.') {
		periodb.append(c);
		break;
              }		   
              state = 4;
          // Looking for precision
	  case 4:
	     if (c>='0' && c<='9') {
		precisionb.append(c);
		break;
             }
	     state = 5;
          // Looking for length modifier
          case 5:
	     if (c=='h'||c=='l'||c=='L') {
		lengthmodb.append(c);
		break;
             }		   
	     state = 6;
          // Get conversion char
	  case 6:
	     convcharb.append(c);
	     state = 7;
	     break;
          // Looking for post-conversion spec chars
	  case 7:
	     postconvb.append(c);
	     break;		
          }		
       }	     		
       String preconv = preconvb.toString();
       String flags = flagsb.toString();
       String fieldwidth = fieldwidthb.toString();
       String period = periodb.toString();
       String precision = precisionb.toString();
       String lengthmod = lengthmodb.toString();
       String convchar = convcharb.toString();
       String postconv = postconvb.toString();

       /* Diagnostics */
       /*
       System.out.println("preconv=\""+preconv+"\"");
       System.out.println("flags=\""+flags+"\"");
       System.out.println("fieldwidth=\""+fieldwidth+"\"");
       System.out.println("period=\""+period+"\"");
       System.out.println("precision=\""+precision+"\"");
       System.out.println("lengthmod=\""+lengthmod+"\"");
       System.out.println("convchar=\""+convchar+"\"");
       System.out.println("postconv=\""+postconv+"\"");
       */
       
       // Custom formatting for x, X, o, b
       char convcharc;
       if (convchar.length()>0 &&
           ((convcharc = convchar.charAt(0))=='x' ||
	    convcharc=='X'||convcharc=='o'||convcharc=='b')) {

	  // Build the formatted string..
	  
	  // Special case for octal format as octal digits don't fit ints neatly..
	  if (convcharc=='o') {
	     s = "";
	     for (int i=0, octalDigit=0; i<m; i++) {
		int j = i%3;
		octalDigit += testBit(i)?(j==0?1:(j==1?2:4)):0;
		if (j==2 || i==m-1) {
	           s = octalDigit + s;
		   octalDigit = 0;
        	}		 
             }	      	  
	  }
	  
	  // ..simpler for hex and binary
	  else {
	     StringBuffer b = new StringBuffer();
	     b.append(SimPrintf.sprintf("%"+convchar, getDataWord(data.length-1)));
	     for (int i=data.length-2; i>=0; i--)
		b.append(SimPrintf.sprintf("%"+convchar, getDataWord(i)));
	     s = b.toString();
          }
	      
	  // Build prefix consisting of flags related chars
	  StringBuffer prefixb = new StringBuffer();
	  if (flags.length()>0) {
	     // Always print a '+' for the sign flag
	     if (flags.indexOf('+')>=0)
	        prefixb.append('+');
             else if (flags.indexOf(' ')>=0)
	        prefixb.append(' ');
             if (flags.indexOf('#')>=0) {
	        switch (convcharc) {
		case 'x': prefixb.append("0x"); break;
		case 'X': prefixb.append("0X"); break;
		case 'o': prefixb.append('0'); break;
		case 'b': prefixb.append("0b"); break;
		}		   
	     }
          }
	  String prefix = prefixb.toString();	     

          // Get precision and fieldwidth values
	  int ip = precision.length()>0 ? Integer.parseInt(precision) : 0;	     
	  int iw = fieldwidth.length()>0 ? Integer.parseInt(fieldwidth) : 0;
	  int extra0s;

	  // Pad leading zeros if required due to precision
	  if (ip>0) {
	     extra0s = ip-s.length();
	     for (int i=0; i<extra0s; i++)
	        s = "0"+s;
          }

	  // Pad further leading zeros if required due to fieldwidth
	  if (iw>0 && flags.length()>0 && flags.indexOf('0')>=0) {
	      extra0s = iw-prefix.length()-s.length();
	      for (int i=0; i<extra0s; i++)
	         s = "0"+s;
	  }

	  // Add prefix
	  s = prefix + s;

	  // Pad with spaces if required due to fieldwidth
	  if (iw>0) {
	     int extrasp = iw - s.length();
	     for (int i=0; i<extrasp; i++) {
	        if (flags.length()>0 && flags.indexOf('-')>=0)
		   s += " ";
		else
		   s = " " + s;   
             }		   
	  }

	  // Add leading/trailing chars
	  s = preconv + s + postconv;
       }
       
       // ..else print the equivalent double
       else 
          s = SimPrintf.sprintf(fmt, getDouble());
	  
       return s;	           	  
    }

    /**
     * Copy another SimValue into this
     * @param src The SimValue to copy from
     * @return this SimValue
     */
    public SimValue put (SimValue src) {
    
       // If src is a SimInteger, and this and src have same n, do
       // efficient bit range copy. Clear upper bits in this if this has
       // larger m than src..
       SimInteger srcInt;
       if (src instanceof SimInteger && (srcInt=(SimInteger)src).n==n) {
          int nbits = Math.min(m+n, srcInt.m+srcInt.n);
          put(src, nbits, 0, 0);
	  if (m>srcInt.m)
	     clearBits(m+n-nbits, nbits);
          return this;	     
       }
       
       // ..else do number copy
       else
          return super.put(src);	     
    }
        
    /**
     * Put an int into this SimInteger
     * @param ival The value to write
     * @return this SimInteger
     */
    public SimValue put(int ival) {
        return put(resize(S, 0).putDataWord(0, ival));
    }

    /**
     * Put a double into this SimInteger
     * @param fval The value to write
     * @return this SimInteger
     */
    public SimValue put(double fval) {

       // Do conversion with abs(fval) and negate result if sign -ve.
       boolean isNeg = fval<0;
           
       // Scale up fval to match fixed precision and truncate any fractional part towards zero
       double g, f = Math.floor(Math.abs(fval) * Math.pow((double)2, (double)n));
       
       // Generate each int word in the data
       for (int i=0; i<data.length; i++) {
          g = Math.floor(f/BIT32);
          putDataWord(i, (int)(((long)(f-g*BIT32)) & 0xFFFFFFFFL));
          f = g;
       }
       
       // Negate if sign was -ve
       if (isNeg)
          twosComplement();       
       
       return this;
    }

    /**
     * Dvide this by another SimInteger and leave the remainder in this
     * @param divisor The SimInteger to divide by
     * @return this SimInteger
     */
    public SimValue rem(SimValue divisor) {

       // Check divisor is a SimInteger
       checkType(divisor);
       
       return put(divrem(divisor));
    }

    /**
     * Subtract another SimInteger from this
     * @param src The SimInteger to subtract
     * @return this SimInteger
     */
    public SimValue sub(SimValue src) {

       // Check src is a SimInteger
       checkType(src);

        SimInteger v = (SimInteger)(((SimInteger) src).resize(m, n));
        return add(v.twosComplement());
    }

    /**
     * Print value, default format
     * @return Value in default format
     */
    public String toString () {
       String val, fmt;
       
       // If within allowed field width, express as float..
       val = SimPrintf.sprintf(defaultFmt, getDouble());
       if (val.length() <= MAX_PRINT_WIDTH)
          return val;       
	  
       // ..else scientific format
       else {
          fmt = n==0 ? "%.0g" : "%g";
          return SimPrintf.sprintf(fmt, getDouble());
       }	  
    }

    // Default constructor
    protected SimInteger() {
        super();
    }

    // Constructor with precision m, n     
    // @param m Precision to left of binary point
    // @param n Precision to right of binary point
    //
    protected SimInteger(int m, int n) {
        super(m, n);
    }

    // Constructor with precision nbits
    // @param nbits Precision to the left of binary point
    //
    protected SimInteger(int nbits) {
        super(nbits, 0);
    }

    // Add an int to the data
    // @param ival The int to add
    // @return this SimInteger	 */
    //
    protected SimInteger add(int ival) {
        int carry = addWithCarry(0, ival, 0);

        for (int i = 1; i < data.length; i++)
            carry = addWithCarry(i, 0, carry);

        return this;
    }

    // Return true if src SimValue can be cast to this type.
    // @param src The SimValue to test
    // @return True if src can be cast to this type
    //
    protected boolean isCastable (SimValue src) {
    
       // Cast allowed if:
       // src is bits:n, or
       // src is []bits:1, or
       // src is int or uint, or
       // src has same width as this
       // src is boolean

       return super.isCastable(src) ||
              isSameType(src) ||
	      src instanceof SimBoolean;
    }

    // Return true if src SimValue is of same type
    // @param src The SimValue to test
    // @return True if src is of same type
    //
    protected boolean isSameType (SimValue src) {
       return src instanceof SimInteger;
    }

    // Resize to new precision mNew,nNew. If newM larger than m or nNew larger than n,
    // pad new bits with zero. If mNew or nNew smaller than current values, discard extra bits.
    // @param mNew New precision to left of binary point
    // @param nNew New precision to right of binary point
    // @return A new SimInteger containing same value as this, with new required precision
    //
    protected SimNumber resize(int mNew, int nNew) {
    
        // Just clone if already desired size
        if ((n == nNew) && (m == mNew)) {
            return (SimNumber)duplicate();
        }

        SimInteger v = (SimInteger) create(getType(), mNew, nNew);
        int mWidth = (mNew < m) ? mNew : m;
        int nWidth = (nNew < n) ? nNew : n;

        // If new n is larger, pad extra trailing bits with zero
        if (nNew > nWidth) {
            v.clearBits(nNew - nWidth, 0);
        }

        // Copy common bits
        v.putBits(this, nWidth, nNew - nWidth, n - nWidth);
        v.putBits(this, mWidth, nNew, n);

        // If new m is larger, extend sign into upper bits
        if (mNew > mWidth) {
            if (sign() < 0) {
                v.setBits(mNew - mWidth, mWidth + nNew);
            } else {
                v.clearBits(mNew - mWidth, mWidth + nNew);
            }
        }

        return v;
    }

    // Put the two's complement of this SimInteger into its value
    // @return this SimInteger
    //
    protected SimInteger twosComplement() {
        return ((SimInteger) inv()).add(1);
    }

    // Add an int to the desired data word and return carry
    // @param index Index into the data array representing the SimInteger value
    // @param ival The int to add to the desired element in the data array
    // @param carry A carry, 0 or 1, to be included in the addition
    // @return The carry from the addition, 0 or 1
    //
    private int addWithCarry(int index, int ival, int carry) {
        long a = (long) getDataWord(index) & 0xFFFFFFFFL;
        long b = (long) ival & 0xFFFFFFFFL;
        a += (b + carry);
        putDataWord(index, (int) (a & 0xFFFFFFFFL));
        return ((a & 0x100000000L) > 0L) ? 1 : 0;
    }
}
