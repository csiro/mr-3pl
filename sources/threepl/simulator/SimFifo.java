/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */


package threepl.simulator;

import java.util.Vector;


/**
 * This class provides a general purpose Object queue, or fifo.
 * 
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
@SuppressWarnings("all")
public class SimFifo extends Vector {
    /**
     * Unique identifier string for this class and build used to help
     * debugging and binary tracking
     */
    private String _class_cvs_id = "$id$";

    /**
     * Default constructor. Creates a SimFifo with capacity 2.
     */
    public SimFifo() {
        this(2);
    }


    /**
     * Constructor with fifo capacity
     *
     * @param capacity The number of Objects in the queue when it is full 
     */
    public SimFifo(int capacity) {
        super(capacity);
        clear();
    }

    /**
     * Return true if queue is full
     * 
     * @return True if queue is full
     */
    public boolean isFull() {
        return elementCount == capacity();
    }

    /**
     * Return a reference to the Object at the head of the queue,
     * or null if queue is empty
     *
     * @return A a reference to the Object at the head of the queue, or null
     */
    public Object peek() {
       return peek(0);
    }

    /**
     * Return a reference to the Object at a specified index in the queue,
     * or null if queue is empty
     * @param index Index within queue of desired object
     * @return A a reference to the Object at the head of the queue, or null
     */
    public Object peek (int index) {

        if(isEmpty())
            return null;

        // Check index
	if (index<0 || index>=size())
	   throw new SimException("SimFifo.peek(int): index out of range");
	   
        return get(index);
    }

    /**
     * Remove the Object at the head of the queue from the queue,
     * and return a reference to it, or null if queue is empty.
     * 
     * @return A reference to object that was at head of queue, or null
     */
    public Object pop() {
        if(isEmpty())
            return null;

        return remove(0);
    }


    /**
     * Insert an Object at the tail of the queue if room. Return
     * true if the queue was already full.
     * 
     * @param o The Object to insert in the queue
     * 
     * @return True if the queue was already full before the insert
     */
    public boolean push(Object o) {
        if(isFull())
            return true;

        add(o);
        return false;
    }
}
