/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */
package threepl.simulator;




/**
 * A SimUnsigned is an unsigned SimInteger. It may be created as
 * a UINT or UFIXED type.
 * 
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
public class SimUnsigned extends SimInteger {

    /**
     * Return sign, 1=positive, 0=zero (cannot be negative)
     * @return sign, 1=positive, 0-zero (cannot be negative)
     */
    public int sign() {
        return eqZero() ? 0 : 1;
    }

    // Default constructor (UINT of default width)
    protected SimUnsigned (int type) {
        super();
    }

    // Constructor for UFIXED with precision m, n
    // @param m Precision to left of binary point
    // @param n Precision to right of binary point
    //
    protected SimUnsigned (int type, int m, int n) {
        super(m, n);
	this.type = type;
    }

    // Constructor for UINT with precision nbits
    // @param nbits Precision to the left of binary point
    //
    protected SimUnsigned(int type, int nbits) {
        super(nbits);
	this.type = type;
    }
}
