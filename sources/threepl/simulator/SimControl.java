/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */
package threepl.simulator;




/**
 * A SimControl is a SimValue representing a control signal. It contains a single boolean value.
 * 
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
public class SimControl extends SimBoolean {

    /**
     * Print value, default format
     * @return Value in default format
     */
    public String toString () {
       return l ? "high" : "low";
    }

    // Default constructor
    protected SimControl () {
       super();
       type = CONTROL;
    }    
}
