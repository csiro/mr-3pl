/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */


package threepl.simulator;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * A SimClock represents a threepl clock. A SimClock keeps track of
 * the number of clock edges which have occured since the last reset.
 * This value may also be read or manually written. SimClock also
 * stores information about a reference clock (if this clock has a
 * reference clock), or the clock frequency if it has no reference
 * clock. SimClock has a tick() method which applies a clock edge
 * to all the SDEs connected to this clock.
 * 
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
@SuppressWarnings("all")
public class SimClock implements SimIdentifier {

    // Clock count changed since last cycle flag
    private boolean changed;
    
    // List of SDEs connected to this clock
    private ArrayList connectedSDE;
    
    // Number of clock edges since last reset
    private long cycle;
    
    // Clock name
    private String id;
    
    // Clock period, ns
    private double period;

    // Clock frequency, MHz
    private double frequency;
    
    // Clock duty cycle, 0-1
    private double duty_cycle;
    
    // Clock positive flag. If false, negative edge is active
    private boolean mode;
    
    // Current clock value, the high or low state. 
    private boolean val;
    
    // Transient active edge indicator. Goes true during tick() on an active
    // edge and is cleared before the next event in SimEventQueue().
    private boolean hasActiveEdge;
               
    /**
     * Constructor with name, period, frequency, duty cycle and clockpos
     * @param id Clock name
     * @param period Clock period, ns
     * @param frequency Clock frequency, MHz
     * @param duty_cycle Clock duty cycle as proportion high
     * @param mode True if clock active on positive going edge
     */
    public SimClock (String id, double period, double frequency,
                     double duty_cycle, boolean mode) {
       connectedSDE = new ArrayList();
       this.id = id;
       this.period = period;
       this.frequency = frequency;
       this.duty_cycle = duty_cycle;
       this.mode = mode;
       reset ();
    }

   /**
    * Add an element to the list of SDEs that will be clocked by this clock.
    * Each element includes an index which indicates which clock to cycle where
    * an SDE has more than one clock input.
    * @param sde An SDE that receives edges from this clock
    * @param index An index to the clock input (where SDE has more than one clock input)
    */
   public void addSDE (SDE sde, int index) {
      ArrayList pair;
      pair = new ArrayList(2);
      pair.add(0, sde);
      pair.add(1, new Integer(index));
      connectedSDE.add(pair);
   }

    /**
     * Clear the clock active edge indicator
     */
    public void clearActiveEdge () {
       hasActiveEdge = false;
    }
                     
    /** Clear changed status. Required by SimIdentifier interface.  */                               
    public void clearChanged () {
       changed = false;
    }
                       
    /**
     * Return clock cycle count since reset.
     * @return Cycle count since last reset
     */
    public long getCount () {
       return cycle;
    }

    /**
     * Return (boolean) value clock signal. Required by SimIdentifier interface.
     * @return Current clock value
     */
    public boolean getBit () {
       return val;
    }
             
    /**
     * Return (double) value clock signal. Required by SimIdentifier interface.
     * @throws SimException (clock values are boolean)
     */
    public double getDouble () {
      throw new SimException("cannot read a clock value as a double");
    }
             
    /**
     * Return duty cycle
     * @return duty cycle, 0-1
     */
    public double getDutyCycle () {
       return duty_cycle;
    }

    /**
     * Return frequency
     * @return frequency, MHz
     */
    public double getFreq () {
       return frequency;
    }
             
    /**
     * Return id (name). Required by SimIdentifier interface.
     * @return Clock name
     */
    public String getId () {
       return id;
    }
                
    /**
     * Return mode
     * @return Clock mode: true if active on positive edge.
     */
    public boolean getMode () {
       return mode;
    }

    /**
     * Get time of next edge, either active or inactive
     * @return Time of next clock edge , ns
     */
    public double getNextEdgeTime () {
    
       // To avoid cumulative error, do not calculate incrementally 
       return (val ? period*(cycle-1+duty_cycle) : period*cycle) + Sim.START_TIME;
    }
                         
   /**
    * Return period
    * @return Period, ns
    */
   public double getPeriod () {
      return period;
   }

    /**
     * Returns true if the clock currently has an active edge
     * @return True if the clock currently has an active edge
     */
    public boolean hasActiveEdge () {
       return hasActiveEdge;
    }       
            
    /**
     * Returns true, ie whether this is a SimClock. Required by SimIdentifier interface.
     * @return True
     */
    public boolean isClock () {
       return true;
    }

    /**
     * Return true if next clock adge is active
     * @return True if next edge is active
     */
    public boolean isNextEdgeActive () {
       return val!=mode;
    }
         
    /**
     * Returns false, ie whether this is a SimVariable. Required by SimIdentifier interface.
     * @return False
     */
    public boolean isVariable () {
       return false;
    }

    /**
     * Return formatted String representation. Required by SimIdentifier interface.
     * @param fmt A printf style format, or null
     * @return The current clock value as a String, formatted according to fmt
     */
    public String print (String fmt) {

       // If no format, use default format..
       if (fmt==null || fmt.equals(""))
          return toString();
	  
       // ..else try to format as integer 0 or 1
       else
          return SimPrintf.sprintf(fmt, val?1:0);	         	  
    }
        
    /**
     * Return String representation of type. Required by SimIdentifier interface.
     * @return String "clock"
     */
    public String printType () {
       return "clock{f="+frequency+",p="+period+",duty="+duty_cycle+
              ",active="+(mode?"pos":"neg")+"}";
    }

    /**
     * Put a double into the cycle count. Required by SimIdentifier interface.
     * @param fval The value to write to the cycle count
     */
    public void put (double fval) {
    
       if (!changed && fval!=cycle)
          changed = true;
	  
       cycle = (int)fval;
    }

   /** Reset cycle count (set to zero) */
   public void reset () {
      cycle = 0;
      changed = false;

       // Set initial value to low so that all clocks are sync'ed on positive
       // going edge at t=0. Note this means clocks with a negative active edge
       // will clock their SDEs out of sync with positive clocks. (If want to sync
       // all clocks on their active edges, change following line to val=!mode.)
       val = false;
   }

   /**
    * Execute a clock edge. If it is the active edge, clock all connected SDEs.
    */
   public void tick () {

      // Clock connected SDEs if about to do active edge
      if (val!=mode) {

	 ArrayList pair;
	 SDE sde;
	 Iterator it = connectedSDE.iterator();
	 while (it.hasNext()) {
            pair = (ArrayList)it.next();
            sde = (SDE)pair.get(0);
            sde.clock(((Integer)pair.get(1)).intValue());
	 }
	 
	 // Inc cycle count
	 cycle++;
      }
      
      // Toggle value and register change
      val = !val;
      
      // Set active edge indicator if this is an active edge
      hasActiveEdge = val == mode;
      
      // Register change
      changed = true;
   }
      
    /**
     * Return default String representation. Required by SimIdentifier interface.
     * @return The current cycle count as a String
     */
    public String toString () {
      return val ? "high" : "low";
    }

    /**
     * Return changed status. Required by SimIdentifier interface.
     * @return boolean changed status
     */                               
    public boolean wasChanged () {
       return changed;
    }
}
