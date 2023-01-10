/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */

package threepl.simulator;



/**
 * A SimBreakpoint represents a simulator breakpoint.
 * 
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
public class SimBreakpoint {

   // The breakpoint expression
   protected SimExprSimpleNode expr;
   
   // Trigger type: level or edge
   protected boolean isEdge;
   
   // Active state last time the expression was evaluated
   protected boolean activeLast;
   
   /**
    * Constructor with expression and type
    * @param expr is the breakpoint expression
    * @param isEdge True if this is to be an edge triggered breakpoint,
    * else false for level triggered
    */
   public SimBreakpoint (SimExprSimpleNode expr, boolean isEdge) {
      this.expr = expr;
      this.isEdge = isEdge;
   }    
   
   /**
    * Return true if this breakpoint has triggered since last call.
    * Should be called on every simulator event.
    * @return true If this breakpoint has triggered since last call
    */
   public boolean hasTriggered () {
   
      boolean active = expr.eval().getBoolean();
      boolean triggered = isEdge ? (active && !activeLast) : active;
      activeLast = active;
      return triggered;
   }

   /**
    * Return true if this is an edge type breakpoint
    * (else false for a level type)
    */
   public boolean isEdge () {
      return isEdge;
   }
             
   /**
    * Reset
    */
   public void reset () {
      activeLast = false;
   }
       
   /**
    * Set the breakpoint type to edge or level
    * @param isEdge True if this is to be an edge triggered breakpoint,
    * else false for level triggered
    */
   public void setType (boolean isEdge) {
      this.isEdge = isEdge;
   }       
}
