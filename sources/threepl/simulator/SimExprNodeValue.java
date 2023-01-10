/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */

package threepl.simulator;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;

/**
 * A SimExprNodeValue object contains the evaluated value of a node in an expression
 * tree, where the nodes are of type SimExprSimpleNode. The type of a
 * SimExprNodeValue indicates the type of value it contains. VAR means the value is
 * a SimValue (a reference to a threepl variable's value, or part thereof). CLK
 * means the value is a SimClock (a reference to a threepl clock). A type of CONTROL, INT,
 * UINT, FIXED, UFIXED, LOG, FLOAT or STRING indicates the node value contains an
 * expression with a primitive type result. Note all numeric primitive types are
 * stored as a double. RANGE means the value is a SimRange, representing an array
 * subscript range. INITIALISER means the value is a compound constant used to assign
 * to or compare to compound types (arrays, structs, groups). CMD means the node
 * represents a simulator command.
 * 
 * The getType() method returns the 'type',value, except when type is VAR or CLK.
 * If type is VAR, getType() returns the equivalent type of the
 * underlying SimValue. (Note Strings are not yet implemented by SimValue.) If type
 * is CLK, getType() returns CONTROL, being the type of the clock level value.
 * 
 * Methods exist for getting and putting values from/to a SimExprNodeValue, and for
 * some logical operations between SimExprNodeValues.
 */
@SuppressWarnings("all")
public class SimExprNodeValue implements SimTypes {

   /**
    * Constant for type is a SimValue which corresponds to a full variable.
    * It has an associated SimVariable referred to by var.
    */
   public static final int VAR = SimTypes.MAX_TYPE+1;      
   
   /** Constant for type is a SimClock */
   public static final int CLK = VAR+1;      

   /** Constant for type is a SimRange */
   public static final int RANGE = CLK+1;
   
   /** Constant for type is an initialiser */
   public static final int INITIALISER = RANGE+1;
   
   /** Constant for type is a command*/
   public static final int CMD = INITIALISER+1;
      
   // Override SimTypes.MAX_TYPE
   private static final int MAX_TYPE = CMD;  
   
   // Type: one of INT .. CMD 
   private int type;
   
   // Used by numeric types
   private double fval;  
         
   // Used by LOG, CONTROL types
   private boolean bval;  
       
   // Used by STRING types
   private String str; 
           
   // Reference to associated SimVariable or SimClock for type==VAL or CLK
   private SimIdentifier sid; 
   
   // Reference to SimVariable for type==VAR
   private SimVariable var; 
   
   // Reference to SimValue for type==VAR
   private SimValue val; 
       
   // Reference to SimClock where type==CLK
   private SimClock clk; 
       
   // Reference to SimRange where type==RANGE
   private SimRange range; 
   
   // Reference to array of nodeValues where type==INITIALISER
   private SimExprNodeValue[] nodeValues;   
   
   /**
    * Constructor with SimIdentifier
    * @param sid A SimIdentifier which will be the underlying value
    */
   public SimExprNodeValue (SimIdentifier sid) {
   
      // Check sid is non-null. Note a variable may have a null value.
      if (sid==null)
         throw new SimException(
	    "SimExprNodeValue.SimExprNodeValue(SimIdentifier):null sid");
      
      this.sid = sid;
      if (sid.isVariable()) {
         var = (SimVariable)sid;
         val = var.getValue();
         type = VAR;
      } else {
         clk = (SimClock)sid;
         type = CLK;	 
      }	 
   }

   /**
    * Constructor with type
    * @param type The desired type (from SimTypes) of the SimExprNodeValue
    */
   public SimExprNodeValue (int type) {
   
      // Check for valid type
      if (type<MIN_TYPE || type>MAX_TYPE)
         throw new SimException(
	    "SimExprNodeValue.SimExprNodeValue(int):invalid type");
     
      this.type = type;
      
      // Create any required internal objects
      switch (type) {
      case RANGE:
         range = new SimRange(0, 0);
	 break;
      }	 
   }

   /**
    * Constructor with an array of SimExprNodeValues. Used to represent
    * initialisers eg, { 1, 2, true, { 3, 4 }}
    * @param nodeValues An array of SimExprNodeValues
    */
   public SimExprNodeValue (SimExprNodeValue[] nodeValues) {
      type = INITIALISER;
      this.nodeValues = nodeValues;
   }
                                 	 
   /**
    * Bitwise AND the value of this SimExprNodeValue with the value
    * of another SimExprNodeValue, and leave the result in this.
    * Valid only where both SimExprNodeValues are integer, ie
    * have SimType.isInt(type) true.
    * @param src An integer type SimExprNodeValue to bitwise AND with this
    */
   public void and (SimExprNodeValue src) {
   
      // Check both SimExprNodeValues are integers
      if (!(isInt() && src.isInt()))
         throw new SimException(
	    "SimExprNodeValue.and(SimExprNodeValue): both must be integers");

      // This algorithm depends on there being no fractional part in either value.
      // This should be the case for integer types.
      double lhs = getDouble();
      double rhs = src.getDouble();
      
      // If either operand is zero, return zero
      if (rhs==0)
         put(rhs);
      if (lhs==0 || rhs==0)
         return;
      	 
      // Find highest bit involved in operation, not counting sign bits
      int hibit = Math.max((int)log2(Math.abs(lhs)), (int)log2(Math.abs(rhs)));
      
      // Find the highest value plus one that can be represented with bits 0 to hibit
      double hival = Math.pow(2.0, hibit+1);
      
      // Remove extended sign bits from operands, if any. Record whether both operands
      // are negative.
      boolean neg, lneg=false, rneg=false;
      if (lhs < 0) {
         lhs += hival;
	 lneg = true;
      }	 
      if (rhs < 0) {
         rhs += hival;
	 rneg = true;
      }
      neg = lneg && rneg;
      
      // Do the AND operation on the operands with the extended sign bits removed
      double fact = hival/2;
      double result = 0;
      boolean l, r;
      for (int bit=hibit; bit>=0; bit--) {
         l = lhs >= fact;
         r = rhs >= fact;
         if (l && r)
            result += fact;
         if (l)
            lhs-=fact;
         if (r)
            rhs-=fact;
         fact /= 2;
      }
      
      // OR the extended sign bits back into the result if both operands were negative
      if (neg)
         result -= hival;
	 
      put(result);	       	  
	 
   }

   /**
    * Return true if the src value equals this.
    * @param src The SimExprNodeValue to test for equality with this
    * @return True if the values are equal
    */
   public boolean eq (SimExprNodeValue src) {
   
      // If both nodes represent a SimValue, compare the values. This handles 
      // cases i) where the type may be compound (array or struct), and
      // ii) where comparing between bits and another variable type
      if (type==VAR && src.type==VAR) {
         if (val!=null && src.val!=null)
            return val.eq(src.val);
         else if (val==null && src.val==null)
	    return true;
         else
	    throw new SimException(
	       "SimExprNodeValue.put(SimExprNodeValue):incompatible types");
      }	    	    	    
      
      // ..else if src is an initialiser and this represents a SimValue,
      // or vice versa, compare SimValue to ArrayList..
      else if (type==VAR && val!=null && src.type==INITIALISER)
         return val.eq(src.getList());
      else if (type==INITIALISER && src.type==VAR && src.val!=null)
         return src.val.eq(getList());
      
      // ..else compare appropriately for type..
      else if (isNumeric() && src.isNumeric())
         return getDouble()==src.getDouble();
      else if (isBoolean() && src.isBoolean())
         return getBoolean()==src.getBoolean();
      else if (type==STRING && src.type==STRING)
         return getString().equals(src.getString());
      else if (type==RANGE && src.type==RANGE)
         return getRange().equals(src.getRange());
      else if (type==INITIALISER && src.type==INITIALISER)
         return getList().equals(src.getList());
      else if (isNull() && src.isNull())
         return true;	 	 
	 
      // ..else incompatible types
      else
         throw new SimException(	 
	       "SimExprNodeValue.put(SimExprNodeValue):incompatible types"); 
   }	            

   /**
    * Get the value as a boolean , if valid for this type
    * @return The value as a boolean
    */
   public boolean getBoolean () {
      switch (type) {
      case VAR:
         if (val==null)
	    throw new SimException("SimExprNodeValue.getBoolean(): cannot read a null type as a boolean");
         return val.getBit();
      case CLK:
         return clk.getBit();	 
      default:
         if (SimType.isBoolean(type))
	    return bval;
         else
            throw new SimException(
	       "SimExprNodeValue.getBoolean():type mismatch ("+type+")");
      }	 
   }
         
   /**
    * Return a reference to the SimClock (valid for type==CLK)
    * @return A reference to the SimClock
    */
   public SimClock getClock () {
   
      // Check is type CLK
      if (type!=CLK)
         throw new SimException("SimExprNodeValue.getClock():does not contain a clock");
      
      return clk;
   }    
       
   /**
    * Get the value as a double, if valid for this type
    * @return The value as a double
    */
   public double getDouble () {
      switch (type) {
      case VAR:
         if (val==null)
	    throw new SimException("SimExprNodeValue.getBoolean(): cannot read a null type as a number");
         return val.getDouble();
      default:
         if (SimType.isNumeric(type))
	    return fval;
         throw new SimException(
	    "SimExprNodeValue.getDouble():type mismatch ("+type+")");
      }
   }
                  	    
   /**
    * Return a reference to the SimIdentifier (valid for type==VAR or CLOCK)
    * @return A reference to the SimIdentifier
    */
   public SimIdentifier getIdentifier () {
   
      // Check is an identifier
      if (sid==null)
         throw new SimException("SimExprNodeValue.getIdentifier():not an identifier");
      
      return sid;
   }    

   /**
    * Return the SimClock which drives the expression for this node; ie. the
    * clock driving the variable for this node. If there is no such clock, return null.
    * An expression only has a driving clock if it is a single variable corresponding
    * to the output of a clocked SDE.
    * @return The clock associated with this (var) expression, or null if none
    */
   public SimClock getAssociatedClock () {

      // If a VAR node, return the clock associated with the variable (or null if none)..
      if (isVariable())
         return var.getClock();	 
	 
      // ..else if a CLK node, return the clock itself..
      else if (isClock())
         return clk;
	 
      // ..else no associated clock
      return null;	 
   }
              
   /**
    * Get the value as a SimRange, if valid for this type
    * @return The value as a SimRange
    */
   public SimRange getRange () {
      switch (type) {
      case RANGE:
         return range;
      default:
         throw new SimException(	 
            "SimExprNodeValue.getRange():type mismatch ("+type+")");
      }	    
   }	    

   /**
    * Get the value as a String, if valid for this type
    * @return The value as a String  
    */  
   public String getString () {
      switch (type) {
      case STRING:
         return str;
      default:
	 // (note SimValues do not support strings yet)
         throw new SimException(
	    "SimExprNodeValue.getString():type mismatch ("+type+")");
      }	 
   }

   /**
    * Get node type. Just return type, except when type==VAR or CLK.
    * If VAR, return the equivalent type of the associated SimValue.
    * If CLK, return UINT.
    * @return type, or equivalent if type==VAL or CLK
    */
   public int getType () {

      switch (type) {
      
      // Type VAR: return the SimValue type
      case VAR:
         return val!=null ? val.getType() : NULL_TYPE;

      // Clock value is type CONTROL
      case CLK:
         return CONTROL;

      // Return type for all others
      default:
         return type;	 	 	    	 
      }
   }

   /**
    * Return a reference to the SimValue (valid for type==VAR)
    * @return A reference to the SimValue within the SimVariable
    */
   public SimValue getValue () {
   
      // Check is type VAR
      if (type!=VAR)
         throw new SimException("SimExprNodeValue.getValue():does not contain a value");
      
      return val;
   }    
       
   /**
    * Return a reference to the SimVariable (valid for type==VAR)
    * @return A reference to the SimVariable
    */
   public SimVariable getVariable () {
   
      // Check is type VAR
      if (type!=VAR)
         throw new SimException("SimExprNodeValue.getVariable():not a variable");
      
      return var;
   }    
       
   /**
    * Return true if this represents a SimVariable
    * which is the data output of an asynchronous queue
    * @return True if this represents a SimVariable which is the data output
    * of an asynchronous queuebuffer
    */
   public boolean isAsyncQueue () {
      return isQueue() && var.isAsyncQueue();
   }

   /**
    * Return true if this represents a boolean value
    * @return True if this represents a boolean value
    */
   public boolean isBoolean () {
      return SimType.isBoolean(getType());
   }      

   /**
    * Return true if the src value can be cast to this type
    * @return True if the src value can be cast to this type
    */
   public boolean isCastable (SimExprNodeValue src) {
     
      // If both types represent SimValues, test compatibility
      if (type==VAR && src.type==VAR) {
         if (val!=null && src.val!=null)
            return val.isCastable(src.val);
         else if (val==null && src.val==null)
	    return true;
         else
	    return false;	    	    
      }
      	 
      // ..else if src is an initialiser and this represents a SimValue,
      // or vice versa, or types are compatible, then are castable
      else if ((type==VAR && val!=null && src.type==INITIALISER) ||
               (type==INITIALISER && src.type==VAR && src.val!=null) ||
               (isNumeric() && src.isNumeric()) ||
               (isBoolean() && src.isBoolean()) ||
	       (isNull() && src.isNull()) ||
               (type==STRING && src.type==STRING) ||
               (type==RANGE && src.type==RANGE) ||
               (type==INITIALISER && src.type==INITIALISER))	       
         return true;	 
	 
      // ..else incompatible types
      else
         return false;
   }
                       
   /**
    * Return true if this represents a SimClock
    * @return True if type==CLK
    */
   public boolean isClock () {
      return type==CLK;
   }

   /**
    * Return true if this represents a SimVariable
    * which is a data output of a combinatorial ram
    * @return True if this represents a SimVariable which is a data output
    * of a combinatorial ram
    */
   public boolean isCram () {
      return isVariable() && var.isCram();
   }

   /**
    * Return true if this represents an integer boolean
    * @return True if this represents an integer value
    */
   public boolean isInt () {
      return SimType.isInt(getType());
   }      

   /**
    * Return true if this represents a null value
    * @return True if this represents a null value
    */
   public boolean isNull () {
      return SimType.isNull(getType());
   }      

   /**
    * Return true if this represents a numeric value
    * @return True if this represents a numeric value
    */
   public boolean isNumeric () {
      return SimType.isNumeric(getType());
   }      

   /**
    * Return true if this node represents a primitive value
    * @return True if this node represents a primitive value
    */
   public boolean isPrimitive () {   
      return isNumeric() || isBoolean() || isNull();
   }
                       
   /**
    * Return true if this represents a SimVariable
    * which is the data output of a queue
    * @return True if this represents a SimVariable which is the data output
    * of a queuebuffer
    */
   public boolean isQueue () {
      return isVariable() && var.isQueue();
   }

   /**
    * Return true if this represents a SimVariable
    * which is a data output of a registered ram
    * @return True if this represents a SimVariable which is a data output
    * of a registered ram
    */
   public boolean isRram () {
      return isVariable() && var.isRram();
   }

   /**
    * Return true if this represents a SimVariable
    * which is the data output of a static
    * @return True if this represents a SimVariable which is the data output of a static
    */
   public boolean isStatic () {
      return isVariable() && var.isStatic();
   }

   /**
    * Return true if this represents a SimVariable
    * @return True if type==VAR
    */
   public boolean isVariable () {
      return type==VAR;
   }

   /**
    * Bitwise OR the value of this SimExprNodeValue with the value
    * of another SimExprNodeValue, and leave the result in this.
    * Valid only where both SimExprNodeValues are integer, ie
    * have SimType.isInt(type) true.
    * @param src An integer type SimExprNodeValue to bitwise OR with this
    */
   public void or (SimExprNodeValue src) {

      // Check both SimExprNodeValues are integers
      if (!(isInt() && src.isInt()))
         throw new SimException(
	    "SimExprNodeValue.or(SimExprNodeValue): both must be integers");

      // This algorithm depends on there being no fractional part in either value.
      // This should be the case for integer types.
      double lhs = getDouble();
      double rhs = src.getDouble();
                
      // If either operand is zero, return other operand
      if (lhs==0)
         put(rhs);
      if (lhs==0 || rhs==0)
         return;
	 
      // Find highest bit involved in operation, not counting sign bits
      int hibit = Math.max((int)log2(Math.abs(lhs)), (int)log2(Math.abs(rhs)));
      
      // Find the highest value plus one that can be represented with bits 0 to hibit
      double hival = Math.pow(2.0, hibit+1);
      
      // Remove extended sign bits from operands, if any. Record whether either operand
      // is negative.
      boolean neg = false;
      if (lhs < 0) {
         neg = true;
         lhs += hival;
      }	 
      if (rhs < 0) {
         neg = true;
         rhs += hival;
      }
      
      // Do the OR operation on the operands with the extended sign bits removed
      double fact = hival/2;
      double result = 0;
      boolean l, r;
      for (int bit=hibit; bit>=0; bit--) {
         l = lhs >= fact;
         r = rhs >= fact;
         if (l || r)
            result += fact;
         if (l)
            lhs-=fact;
         if (r)
            rhs-=fact;
         fact /= 2;
      }
      
      // OR the extended sign bits back into the result if either operand was negative
      if (neg)
         result -= hival;
	 
      put(result);	       	  
	 
   }

   /**
    * Return formatted value
    * @return The value formatted according to fmt specification
    */
   public String print (String fmt) {   

      switch (type) {
      case VAR:
         return val!=null ? val.print(fmt) : "null";
      case CLK:
         return clk.print(fmt);
      case INT:
      case UINT:
	 if (fmt==null || fmt=="")
	    fmt = "%.0f";
         return SimPrintf.sprintf(fmt, fval);	  	  
      case FIXED:
      case UFIXED:
      case FLOAT:
	 if (fmt==null || fmt=="")
	    fmt = "%g";
         return SimPrintf.sprintf(fmt, fval);
      case LOG:
      case CONTROL:
	 if (fmt==null || fmt=="")
            return bval ? (type==LOG?"true":"high") : (type==LOG?"false":"low");
	 // Try to format as integer 0 or 1
         return SimPrintf.sprintf(fmt, bval ? 1 : 0);
      case NULL_TYPE:
         return "null";	 
      case STRING:
	 if (fmt==null || fmt=="")
            return str;
         return SimPrintf.sprintf(fmt, str);
      case RANGE:
         return range.print(fmt);	  
      case INITIALISER:
         StringBuffer b = new StringBuffer();
	 b.append("{");
         for (int i=0; i<nodeValues.length; i++)
	    b.append((i>0?",":"")+nodeValues[i].print(fmt));
         b.append("}");
	 return b.toString();	     	  
      // Unprintable type	  
      default:
         throw new SimException(
	   "SimExprNodeValue.print(String): unprintable type ("+type+")");
      }
   }

    /**
     * Return type of this SimExprNodeValue as a String
     * @return Type of this SimExprNodeValue as a String
     */
   public String printType () {
      switch (type) {
      case VAR:
         return val!=null ? val.printType() : "null";
      case CLK:
         return clk.printType();
      case RANGE:
         return "range";
      case INITIALISER:
	 return "initialiser";	     	  
      case CMD:
         return "command";
      default:
         return SimType.printType(type);
      }
   }      	 

   /**
    * Copy another SimExprNodeValue's value into the value of this
    * @param src The source SimExprNodeValue
    */
   public void put (SimExprNodeValue src) {
   
      // If both nodes represent a SimValue, copy the value. This handles 
      // cases i) where the type may be compound (array or struct), and
      // ii) where casting between bits and another variable type
      if (type==VAR && src.type==VAR) {
         if (val!=null && src.val!=null)
            val.put(src.val);
         else if (val==null && src.val==null)
	    ;  // no action for both null type
         else
	    throw new SimException("SimExprNodeValue.put(SimExprNodeValue):incompatible types");	    	    
	 return;
      }
      
      // ..else if src is an initialiser and this represents a SimValue,
      // get a compound value from the initialiser and copy to the value..
      else if (type==VAR && val!=null && src.type==INITIALISER) {
         val.putObj(src.getList());
	 return;
      }
      
      // ..else copy appropriately for type..
      else if (isNumeric() && src.isNumeric())
         put(src.getDouble());
      else if (isBoolean() && src.isBoolean())
         put(src.getBoolean());
      else if (isNull() && src.isNull())
         ; // No action
      else if (type==STRING && src.type==STRING)
         put(src.getString());
      else if (type==RANGE && src.type==RANGE)
         put(src.getRange());
	 
      // ..else incompatible types
      else
         throw new SimException(	 
	       "SimExprNodeValue.put(SimExprNodeValue):incompatible types");      
   }   

   /** 
    * Copy a double into the value of this
    * @param fval The value to copy
    */
   public void put (double fval) {   
      switch (type) {
      case VAR:
         if (val==null)
	    throw new SimException("SimExprNodeValue.put(double): cannot write a number to a null type");
         val.put(fval);
	 break;
     default:
         if (isInt())	 
            // Copying to an integer, so truncate towards zero as we store it
            this.fval = fval >= 0 ? Math.floor(fval) : Math.ceil(fval);
         else if (isNumeric())
            this.fval = fval;
         else	 	    
            throw new SimException(
	       "SimExprNodeValue:put(double):type mismatch ("+type+")");
      }	 
   }
   
   /** 
    * Copy a boolean into the value of this
    * @param bval The value to copy
    */
   public void put (boolean bval) {
      switch (type) {
      case VAR:
         if (val==null)
	    throw new SimException("SimExprNodeValue.put(boolean): cannot write a boolean to a null type");
         val.put(bval);
	 break;
      case CLK:
         throw new SimException("clock value is read only");
       default:
         if (SimType.isBoolean(type))	 
            this.bval = bval;
         else	 	    
            throw new SimException(
	       "SimExprNodeValue:put(boolean):type mismatch ("+type+")");
      }
   }      	 

   /** 
    * Put a String into the value of this
    * @param str The source String
    */
   public void put (String str) {
      if (type==STRING)
         this.str = str;
      else
         // Note SimValues do not support strings yet
         throw new SimException(
	    "SimExprNodeValue:put(String):type mismatch ("+type+")");
   }

   /**
    * Put a SimValue into the value of this
    * @param val The SimValue to put
    */
   public void put (SimValue val) {
   
      // Throw exception if type!=VAR
      if (type != VAR)
         throw new SimException("SimExprNodeValue.put(SimValue):type mismatch");

      if (this.val!=null && val!=null)
         this.val.put(val);
      else if (this.val==null && val==null)
         ;  // No action
      else
         throw new SimException("SimExprNodeValue.put(SimValue):type mismatch");	 	 
   }
       
   /**
    * Put a SimRange into the value of this
    * @param range The SimRange to put
    */
   public void put (SimRange range) {
   
      // Throw exception if type!=RANGE
      if (type != RANGE)
         throw new SimException("SimExprNodeValue.put(SimRange):type mismatch");

      this.range = range;
   }
       
   /**
    * Read value from a DataInput object. Reads the SimExprNodeValue value from a
    * file written with write().
    * @param in is the DataInput to read from
    * @return The read value as a SimExprNodeValue
    */
   public static SimExprNodeValue read (DataInput in) {

      if (in==null)
         throw new SimException("SimExprSimpleNode.read(DataInput): no input file defined");

      try {

         // Read type, create value of same type
	 int type = in.readInt();
	 SimExprNodeValue value = new SimExprNodeValue(type);
         int len;

	 // Read value (expect only primitive types)
	 if (value.isNumeric())
	    value.put(in.readDouble());
         else if (value.isBoolean())
	    value.put(in.readBoolean());	    
         else if (type==STRING)
	    value.put(in.readUTF());
         else if (value.isNull())
	    ;  // nothing actually stored for null type	    
         else
	   throw new SimException(
	       "SimExprNodeValue.read(DataInput): invalid expr type ("+type+")");
         return value;

      } catch (IOException e) {
         throw new SimException(e);
      }
   }

   /**
    * For a VAR type node, replace the underlying SimValue field with the
    * passed one. This is used for nodes which represent a part of a variable,
    * ie. struct fields and array elements.
    * @param val The replacement Simvalue
    */
   public void replaceVal (SimValue val) {
   
      if (type!= VAR)
         throw new SimException("SimExprNodeValue.replaceVal(SimValue):type mismatch");
      this.val = val;	 
   }
       
   /**
    * Return sizeof this. If an array, returns number of elements.
    * Otherwise returns 1.
    *
    * @return Number of elements if an array, else 1.
    */
   public int sizeof () {
      switch (type) {
      case VAR:
         return val.sizeof();
      default:
         return 1;	 	 	 	 
      }
   }
       
   /**
    * Return value as a String, default format
    * @return Value as a String, default format
    */
   public String toString () {
      return print(null);
   }       

   /**
    * Write to a DataOutput object. Symmetrical wrth read().
    * @param out The DataOutput to write to
    */
   public void write (DataOutput out) {

      if (out==null)
         throw new SimException("SimExprSimpleNode.write(DataOutput): no output file defined");

      try {

         // Write type first
	 int type = getType();
	 out.writeInt(type);

	 // Only handle primitive types
	 if (isNumeric())
	    out.writeDouble(getDouble());
         else if (isBoolean())
	    out.writeBoolean(getBoolean());
         else if (type==STRING)
	    out.writeUTF(getString());
         else if (isNull())
	    ;  // Nothing actually stored for null type
         else	    
	   throw new SimException(
	       "SimExprNodeValue.write(DataOutput): invalid expr type ("+type+")");
      } catch (IOException e) {
         throw new SimException(e);
      }
   }
    
   /**
    * Bitwise XOR the value of this SimExprNodeValue with the value
    * of another SimExprNodeValue, and leave the result in this.
    * Valid only where both SimExprNodeValues are integer, ie
    * have SimType.isInt(type) true.
    * @param src An integer type SimExprNodeValue to bitwise XOR with this
    */
   public void xor (SimExprNodeValue src) {

      // Check both SimExprNodeValues are integers
      if (!(isInt() && src.isInt()))
         throw new SimException(
	    "SimExprNodeValue.xor(SimExprNodeValue): both must be integers");

      // This algorithm depends on there being no fractional part in either value.
      // This should be the case for integer types.
      double lhs = getDouble();
      double rhs = src.getDouble();

      // If either operand is zero, return other operand
      if (lhs==0)
         put(rhs);
      if (lhs==0 || rhs==0)
         return;
	 
      // Find highest bit involved in operation, not counting sign bits
      int hibit = Math.max((int)log2(Math.abs(lhs)), (int)log2(Math.abs(rhs)));
      
      // Find the highest value plus one that can be represented with bits 0 to hibit
      double hival = Math.pow(2.0, hibit+1);
      
      // Remove extended sign bits from operands, if any. Record whether either but
      // not both operands are negative.
      boolean neg, lneg=false, rneg=false;
      if (lhs < 0) {
         lhs += hival;
	 lneg = true;
      }	 
      if (rhs < 0) {
         rhs += hival;
	 rneg = true;
      }
      neg = (lneg || rneg) && !(lneg && rneg);
      
      // Do the XOR operation on the operands with the extended sign bits removed
      double fact = hival/2;
      double result = 0;
      boolean l, r;
      for (int bit=hibit; bit>=0; bit--) {
         l = lhs >= fact;
         r = rhs >= fact;
         if ((l || r) && !(l && r))
            result += fact;
         if (l)
            lhs-=fact;
         if (r)
            rhs-=fact;
         fact /= 2;
      }
      
      // OR the extended sign bits back into the result if either but not both operands
      // were negative
      if (neg)
         result -= hival;
	 
      put(result);	       	  	 
   }

   // For an INITIALISER type, build and return an ArrayList of values corresponding
   // to the values in the initialiser.
   // @return An ArrayList of values corresponding to the values in the initialiser
   //
   private ArrayList getList () {
   
      // Check type
      if (type!=INITIALISER)
         throw new SimException("SimExprNodeValue.getList(): not INITIALISER type");

      // Build and return an ArrayList of values corresponding to the initialiser
      SimExprNodeValue nodeValue;	 
      ArrayList l = new ArrayList();
      for (int i=0; i<nodeValues.length; i++) {
         if ((nodeValue = nodeValues[i])==null)
	    l.add(null);
	 else if (nodeValue.type==VAR)
	    l.add(nodeValue.getValue());
         else if (nodeValue.type==CLK)
	    l.add(nodeValue.getClock());	 	 
	 else if (nodeValue.isNumeric())
	    l.add(new Double(nodeValue.getDouble()));
         else if (nodeValue.isBoolean())
	    l.add(new Boolean(nodeValue.getBoolean()));	    
         else if (nodeValue.type==STRING)
            l.add(nodeValue.getString());
         else if (nodeValue.type==INITIALISER)
            l.add(nodeValue.getList());
         else
	    throw new SimException(
	       "SimExprNodeValue.getList(): invalid initialiser type ("+
	       nodeValue.type+")");
      }
      return l;	 	    	 
   }
   
   // Return log base 2
   // @param a A double for which to return log base 2
   // @return Log base 2 of the parameter
   //
   private static double log2 (double a) {

      return Math.log(a)/Math.log(2);
   }
}
