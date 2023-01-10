/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */
package threepl.simulator;

import java.util.ArrayList;
import java.util.LinkedList;


/**
 * There is one SimEventQueue object. It is a queue of SimEvent
 * objects, ordered by time of event execution stored in each
 * SimEvent. When an event reaches the head of the queue it is
 * executed, then removed from the queue. If the event is a clock
 * edge (SimEvent type is eventCLOCK), the time for the next
 * clock edge is calculated and the event (with new execution
 * time) is reinserted in the queue.
 */
@SuppressWarnings("all")
public class SimEventQueue extends LinkedList {

   /** Reference to the single Sim object */
   private Sim sim;

   /** An exception to throw after a trace, plot or breakpoint runtime expression error */
   private SimException exception;
          
   /**
    * Constructor
    *
    * @param sim The single Sim object
    */
   public SimEventQueue (Sim sim) {
      super();
      this.sim = sim;
   }

   /**
    * Add event to queue. Events are automatically ordered by
    * their time field with the earliest event at the head
    * of the queue and the latest event at the end.
    *
    * @param e The SimEvent to add to the queue
    */
   public void add (SimEvent e) {
      double t = e.getTime();
      
      /*
       * Search from tail of queue to head as added
       * events are likely to belong near the queue tail
       */
      for (int i=size()-1; i>=0; i--) {
         if (t >= ((SimEvent)get(i)).getTime()) {
	    add(i+1, e);
	    return;
         }
      }
      add(0, e);	 
   }
                
   /**
    * Execute events in queue until and including next edge of specified clock.
    * Simulator combinatorial behaviour is executed after each group of events
    * with the same timestamp. Events are automatically removed from the queue
    * after exection. Clock events are rescheduled (added back to queue) with new time
    * for next edge.
    *
    * @param clock The SimClock whose next clock edge specifies time up to which to execute events
    */
   public void execNext (SimClock clock) {
      SimEvent e;
      boolean foundClock, doBehaviour;
      double t, currentTime, endTime;
      SimClock cl;
      ArrayList clist = new ArrayList();
      
      exception = null;
      
      // No action if queue empty
      if (size() == 0)
         return;

      // From here on we assume that if the queue becomes empty, there will be no more
      // events queued for the time of the most recent queued events(s), so it is safe
      // to execute simulator behaviour when the last event in the queue is removed.
      // Normally the queue should never become empty because clock edge events always
      // reschedule themselves for the next edge.
      	 
      /*
       * Execute all queue events with a timestamp earlier or equal to that of the
       * next active clock edge
       */
      e = (SimEvent)get(0);
      t = e.getTime();
      currentTime = t;
      endTime = t;
      foundClock = false;
      doBehaviour = false;
      while (size()>0 && !(foundClock && t>endTime)) {
      
         /* Remove next event from queue and execute */
         e = (SimEvent)remove(0);
         e.exec();

	 switch (e.getType()) {
	 
	 // Clock event type
	 case SimEvent.eventCLOCK:
	 
	    cl = e.getClock();
	    
	    // Add the clock to the list of clocks to clearActiveEdge() on after all events
	    // at this time have executed
	    clist.add(cl);
	    
	    // Set the flag to do behaviour if not yet set and the edge just executed was active
	    if (!doBehaviour)
	       doBehaviour = cl.hasActiveEdge();
	       
            // Look for specified clock active edge event. We have
	    // found the event we are waiting for if the active edge
	    // just happened.
            if (!foundClock) {
	       if (cl.equals(clock) && cl.hasActiveEdge()) {
	          foundClock = true;
	          endTime = t;
	       }
            }
	    
	    /* Reschedule for next edge */
            e.setNextClockTime();
	    add(e);
            break;
	 
	 // Non-clock event type   	 
	 default:
	 
	    // Always do behaviour
	    doBehaviour = true;
            break;
         }
	 	    
         /* Look at scheduled time for next queue event */
	 if (size()>0) {
            e = (SimEvent)get(0);
            t = e.getTime();
	 }
	 
	 // If queue now empty, or time of next event is different to current,
	 // do combinatorial behaviour for the concurrent event(s) which have
	 // just been executed, do trace(s), plot(s) if any and check for
	 // breakpoint(s) hit
	 if (size()==0 || t!=currentTime) {
	 
            // Update time
	    sim.setTime(currentTime);
	    
	    // Do simulator behaviour if any event just executed was an active clock edge or a variable set
	    // event. Don't need to do simulator hehaviour if only had clock inactive edge(s).
	    if (doBehaviour) {
	       sim.behaviour(false);
	       sim.inputBehaviour(false);
	       doBehaviour = false;
	    }
	    
	    /* Do trace, plot, check breakpoints as required, capture any exceptions */
	    try {
	       sim.doTracePlot();
            } catch (SimException ex) {
	       appendException(ex);
	    }	       
	    try {
	       sim.checkBreakpointHit();
            } catch (SimException ex) {
	       appendException(ex);
	    }	       

            // Clear active edge indicator from any clock events which just happened together
	    if (clist.size()>0) {
	       for (int i=0; i<clist.size(); i++)
	          ((SimClock)clist.get(i)).clearActiveEdge();
	       clist.clear();
            }	       
	    
            /* If any exceptions occurred above, throw them all in one combined */	       
            if (exception!=null)
	       throw exception;
		  	       
            currentTime = t;	       
         }	    	    
      }	          
   }

   /**
    * Append a SimException to the SimCombinedException. This is a means of combining a
    * number of independent exception into one exception for reporting.
    *
    * @param e The SimException to add
    */   
   private void appendException (SimException e) {
      if (exception==null)
         exception = new SimException();	 
      exception.append(e);	 
   } 
          
}
