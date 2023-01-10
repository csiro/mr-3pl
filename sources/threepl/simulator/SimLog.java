/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */
package threepl.simulator;




/**
 * A SimLog is a SimValue containing a single boolean value. It may
 * be created as a LOG type.
 * 
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
public class SimLog extends SimBoolean {

    /**
     * Print value, default format
     * @return Value in default format
     */
    public String toString () {
       return l ? "true" : "false";
    }

    // Default constructor
    protected SimLog() {
        super();
	type = LOG;
    }
    
}
