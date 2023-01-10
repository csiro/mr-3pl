/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */

package threepl.simulator;

/**
 */
public class SimType implements SimTypes {

   /**
    * Return true if type is INT, UINT or BITS
    * @param type A SimTypes type
    * @return True if type is INT, UINT or BITS
    */
   public static boolean isInt (int type) {
      return type==INT || type==ENUM || type==UINT || type==BITS;
   }
   
   /**
    * Return true if type is FIXED, UFIXED or FLOAT
    *
    * @param type A SimTypes type
    *
    * @return True if type is integer or floating
    */
   public static boolean isFloat (int type) {
      return type==FIXED || type==UFIXED || type==FLOAT;
   }
   
   /**
    * Return true if type is null
    * @param type A SimTypes type
    * @return True if type is null
    */
   public static boolean isNull (int type) {
      return type==NULL_TYPE;
   }

   /**
    * Return true if type is numeric
    * @param type A SimTypes type
    * @return True if type is numeric
    */
   public static boolean isNumeric (int type) {
      return isInt(type) || isFloat(type);
   }

   /**
    * Return true if type is boolean, ie LOG or CONTROL
    * @param type A SimTypes type
    * @return True if type is LOG or CONTROL
    */
   public static boolean isBoolean (int type) {
      return type==LOG || type==CONTROL;
   }

   /**
    * Return true if the first type argument may be cast to the second type.
    * @param lhsType First type
    * @param rhsType Second type
    * @return True if lhsType may be cast to rhsType
    */
   public static boolean isCastable (int lhsType, int rhsType) {
   
      return lhsType==rhsType ||
             (isNumeric(lhsType) && isNumeric(rhsType)) ||
             (isBoolean(lhsType) && isBoolean(rhsType));
   }   
             
   /**
    * Return FLOAT if both arguments are numeric and at least
    * one is FLOAT. Otherwise, both arguments must be same type;
    * return type of first argument.
    * @param lhsType First operand type
    * @param rhsType Second operand type
    * @return The result type
    */
   public static int resultType (int lhsType, int rhsType) {

      // If at least one is FLOAT, return FLOAT for numeric types..
      if (isFloat(lhsType) || isFloat(rhsType))
         return FLOAT;

      // ..else if at least one is LOG, return LOG for boolean types..
      else if (lhsType==LOG || rhsType==LOG)
         return LOG;
      
      // ..else check compatibility..
      if (!isCastable(lhsType, rhsType))
         throw new SimException(
	    "SimType.resultType(int,int): incompatible types");

      // ..else return first type (both are same) 	 
      else
         return lhsType;
   }
       
   /**
    * Return type as a String
    * @param type int type
    * @return Type as a String
    */    
   public static String printType (int type) {
      switch (type) {
      case NULL_TYPE: return "null";
      case ENUM:   return "enum";
      case INT:    return "int";       
      case UINT:   return "uint";
      case BITS:   return "bits";
      case FIXED:  return "fixed";
      case UFIXED: return "ufixed";   
      case FLOAT:  return "float";
      case CONTROL: return "control";
      case LOG:    return "log";
      case STRING: return "string";
      case ARRAY:  return "array";
      case STRUCT: return "struct";
      case GROUP:  return "group";
      default:
         throw new SimException(
	    "SimType.printType(int): invalid type ("+type+")");
      }
   }         
}
