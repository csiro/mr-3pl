/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */
package threepl.simulator;



/**
 * A SimEvent represents a scheduled event that resides in a SimEventQueue.
 * There are two SimEvent types: an eventCLOCK type represents a clock edge,
 * and an eventSETVAR represents a variable being set to a value. An
 * eventSETVAR is used to set a variable to a value in sync with some other
 * event, like a clock edge. This is needed, for example, when setting and
 * clearing the global_start variable in sync with the clock when a threepl
 * target program starts executing. Each SimEvent stores a "time" for event
 * execution, ie when a clock edge will occur or when a variable will 
 * be set. SimEvents are ordered in the SimEventQueue by this time, with
 * the earlier times at the head of the queue. Units for time are tick increments,
 * refer SimClock.
 */
@SuppressWarnings("all")
public class SimEvent {

   /** Clock edge event type */
   public final static int eventCLOCK = 0;
   
   /** Set variable event type */
   public final static int eventSETVAR = 1;
   
   /** boolean value for an eventSETVAR event */
   private final static int varBOOL = 0;
   
   /** int value for an eventSETVAR event */
   private final static int varINT = 1;
   
   /** double value for an eventSETVAR event */
   private final static int varDOUBLE = 2;
   
   /** SimVariable value for an eventSETVAR event */
   private final static int varVAR = 3;
   
   /** This event type */
   private int eventType; 
   
   /** Variable type for eventSETVAR event */
   private int varType;
   
   /** Clock for eventCLOCK event */
   private SimClock clock;

   /** dst variable for eventSETVAR event */
   private SimVariable dstVar; 

   /** src variable for eventSETVAR event, SimVariable */
   private SimVariable srcVar;

   /** src variable for eventSETVAR event, boolean */
   private boolean srcBool;

   /** src variable for eventSETVAR event, int */
   private int srcInt;

   /** src variable for eventSETVAR event, double */
   private double srcDouble;
   
   /** Scheduled time for this event to occur */
   private double time;
       
   /**   
    * Constructor for clock edge event
    *
    * @param time The time in tick increments for this clock edge to be executed
    * @param clock The clock for which to execute a clock edge
    */
   public SimEvent (double time, SimClock clock) {
      eventType = eventCLOCK;
      this.time = time;
      this.clock = clock;
   }
   
   /**
    * Constructor for set variable event, with SimVariable as source
    *
    * @param time The time in tick increments for the dst variable to be set
    * @param dst The SimVariable to set
    * @param src The SimVariable from which to obtain the value
    */
   public SimEvent (double time, SimVariable dst, SimVariable src) {
   
      // Check null type compatibility
      SimValue dstVal = dst.getValue();
      SimValue srcVal = src.getValue();
      if (dstVal!=null && srcVal==null || dstVal==null && srcVal!=null)
         throw new SimException(
	    "SimEvent(double,SimVariable,SimVariable): null type incompatibility");
      
      SimEventHelper(time, dst);
      this.srcVar = src;
   } 
      
   /**
    * Constructor for set variable event, with boolean value as source
    *
    * @param time The time in tick increments for the dst variable to be set
    * @param dst The SimVariable to set
    * @param src The boolean value to write to the dst variable
    */
   public SimEvent (double time, SimVariable dst, boolean src) {

      // Check null type compatibility
      SimValue dstVal;
      if ((dstVal = dst.getValue()) == null)
         throw new SimException(
	    "SimEvent(double,SimVariable,boolean): cannot assign a boolean to a null type");

      SimEventHelper(time, dst);
      varType = varBOOL;
      srcBool = src;      
   }  
     
   /**
    * Constructor for set variable event, with int value as source
    *
    * @param time The time in tick increments for the dst variable to be set
    * @param dst The SimVariable to set
    * @param src The int value to write to the dst variable
    */
   public SimEvent (double time, SimVariable dst, int src) {

      // Check null type compatibility
      SimValue dstVal;
      if ((dstVal = dst.getValue()) == null)
         throw new SimException(
	    "SimEvent(double,SimVariable,int): cannot assign an int to a null type");

      SimEventHelper(time, dst);
      varType = varINT;
      srcInt = src;     
   }  
     
   /**
    * Constructor for set variable event, with double value as source
    *
    * @param time The time in tick increments for the dst variable to be set
    * @param dst The SimVariable to set
    * @param src The double value to write to the dst variable
    */
   public SimEvent (double time, SimVariable dst, double src) {

      // Check null type compatibility
      SimValue dstVal;
      if ((dstVal = dst.getValue()) == null)
         throw new SimException(
	    "SimEvent(double,SimVariable,double): cannot assign a double to a null type");

      SimEventHelper(time, dst);
      varType = varDOUBLE;
      srcDouble = src;        
   }    

   /**
    * Helper for eventSETVAR constructors
    *
    * @param time The time in tick increments for the dst variable to be set
    * @param dst The SimVariable to set
    */
   private void SimEventHelper(double time, SimVariable dst) {
      eventType = eventSETVAR;
      this.time = time;
      this.dstVar = dst;   
   }

   /**
    * Return event type
    *
    * @return Event type
    */
   public int getType () {
      return eventType;
   }

   /**
    * Return event time
    *
    * @return Event time in tick increments
    */
   public double getTime () {
      return time;
   }         

   /**
    * Return SimClock, if an eventCLOCK type, or null
    *
    * @return SimClock, if an eventCLOCK type, or null
    */
   public SimClock getClock () {
      return clock;
   }
                             
   /**
    * Execute event
    */
   public void exec () {
      switch (eventType) {      
      case eventCLOCK:
         clock.tick();
	 break;
      case eventSETVAR:
         switch (varType) {
	 case varVAR:    dstVar.getValue().put(srcVar.getValue());    break;
	 case varBOOL:   dstVar.getValue().put(srcBool);   break;
	 case varINT:    dstVar.getValue().put(srcInt);    break;
	 case varDOUBLE: dstVar.getValue().put(srcDouble); break;
	 default:
	    throw new SimException("SimEvent.exec():invalid variable type");
	 }
	 break;
      default:
	 throw new SimException("SimEvent.exec():invalid event type");
      }	 
   }
   
   /**
    * For an eventCLOCK, increment time with clock period
    */
   public void setNextClockTime () {
   
      /* Throw exception if not eventCLOCK */
      if (eventType != eventCLOCK)
         throw new SimException("SimEvent.setNextClockTime():not an eventCLOCK");
      
      // Get next clock edge time
      time = clock.getNextEdgeTime();   
   }
   
   /**
    * Print event contents
    * @return Event as a String
    */
   public String toString () {
   
      StringBuffer b = new StringBuffer();

      switch (eventType) {      
      case eventCLOCK:
         b.append(clock.getId()+" edge");
         break;
      case eventSETVAR:
	 b.append(dstVar.getId()+"=");
         switch (varType) {
	 case varBOOL:
	    b.append(srcBool);
	    break;
	 case varINT:
	    b.append(srcInt);
	    break;	 
	 case varDOUBLE:
	    b.append(srcDouble);
	    break;
	 case varVAR:
	    b.append(srcVar.getId());
	    break;
         default:
	    throw new SimException(
	       "SimEvent.toString(): invalid varType ("+varType+")");	    
	 }
         break;
      default:
         throw new SimException(
	    "SimEvent.toString(): invalid event type ("+eventType+")");
      }	 
      b.append(" at t="+time);
      return b.toString();
   }
       
}
