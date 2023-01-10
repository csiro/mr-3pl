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


/**
 * This class holds a sample of simulator signal values. It is used for
 * reading and writing trace and plot files.
 * 
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
public class SimSample {

   private SimExprNodeValue[] values;
   private boolean[] isValid;     

   /** To create from an array of types read from a file header */
   public SimSample (int[] types) {
      values = new SimExprNodeValue[types.length];
      isValid = new boolean[types.length];
      for (int i=0; i<types.length; i++)
         values[i] = new SimExprNodeValue(types[i]);
      setAllValid();	  
   }

   /** To create from an array of values */
   public SimSample (SimExprNodeValue[] values) {
      this.values = values;
      setAllValid();
   }
                        
   /** To create from arrays of values and isValid */
   public SimSample (SimExprNodeValue[] values, boolean[] isValid) {
      // check array lengths equal
      this.values = values;
      this.isValid = isValid;
   }
                        
   /** To create from an array of expressions */
   public SimSample (SimExprSimpleNode[] exprs, SimExprNodeValue[] clockEdges) {
      values = new SimExprNodeValue[exprs.length+clockEdges.length];
      for (int i=0; i<exprs.length; i++)
         values[i] = exprs[i].nodeValue;   
      for (int i=0; i<clockEdges.length; i++)
         values[exprs.length+i] = clockEdges[i];   
      setAllValid();	 
   }

   /** Get length */
   public int length () {
      return values.length;
   }
         
   /** Get the ith value */
   public SimExprNodeValue get (int i) {
      // check i validity!!
      return values[i];
   }

   /** Put a SimExprNodeValue into the the ith value */
   public void put (int i, SimExprNodeValue value) {
      // check i validity
      values[i] = value;
   }

   /** Put a double into the the ith value */
   public void put (int i, double fval) {
      // check i validity
      
      // check value is compatible with double

      values[i].put(fval);
   }

   /** Get the ith isValid */
   public boolean isValid (int i) {
      // check i validity
      return isValid[i];
   }
            
   /** Set a value valid or invalid */
   public void setValid (int i, boolean state) {
      // check i validity
      isValid[i] = state;
   }
      
   public static SimSample read (DataInput in) {

      try {  
          
	 /* Read no. of values in sample, create arrays */
	 SimExprNodeValue[] values = new SimExprNodeValue[in.readInt()];
	 boolean[] isValid = new boolean[values.length];

	 /* For each value.. */
	 for (int i=0; i<values.length; i++) {
	    isValid[i] = in.readBoolean();
            values[i] = SimExprNodeValue.read(in);
         }
	 return new SimSample(values, isValid);
      
      } catch (IOException e) {
         throw new SimException(e);
      }
   }
   
   public void write (DataOutput out) {

      try {  
          
	 /* Write no. of values */
	 out.writeInt(values.length);

	 /* Write each isValid state and value in sample.. */
	 for (int i=0; i<values.length; i++) {
	    out.writeBoolean(isValid[i]);
            values[i].write(out);
         }	    
      } catch (IOException e) {
         throw new SimException(e);
      }
   }
   
   /** Create the isValid[] array and set all true */
   private void setAllValid () {
      if (isValid==null)
         isValid = new boolean[values.length];
      for (int i=0; i<isValid.length; i++)
         isValid[i] = true;
   }
   	 	    
      

}      
