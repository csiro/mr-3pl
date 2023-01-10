/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */

package threepl.simulator;

/**
 */
public interface SimTypes {

   /** Minimum type */
   public static final int MIN_TYPE = 0;
   
   /** null type */
   public static final int NULL_TYPE = MIN_TYPE;

   /** Enum type */
   public static final int ENUM = NULL_TYPE+1;     
   
   /** Signed integer */
   public static final int INT = ENUM+1;       

   /** Unsigned integer */
   public static final int UINT = INT+1; 
   
   /** Collection of bits */
   public static final int BITS = UINT+1;      

   /** Signed fixed point */
   public static final int FIXED = BITS+1;       

   /** Unsigned fixed point */
   public static final int UFIXED = FIXED+1;       

   /** Floating point */
   public static final int FLOAT = UFIXED+1;     

   /** Control */
   public static final int CONTROL = FLOAT+1;       
   
   /** Logical */
   public static final int LOG = CONTROL+1;       

   /** String */
   public static final int STRING = LOG+1; 
   
   /** Array */
   public static final int ARRAY = STRING+1; 
      
   /** Struct */
   public static final int STRUCT = ARRAY+1; 

   /** Group */
   public static final int GROUP = STRUCT+1;
         
   /** Maximum type */
   public static final int MAX_TYPE = GROUP;         
}
