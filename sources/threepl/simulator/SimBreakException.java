/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */

package threepl.simulator;

import java.util.ArrayList;


/**
 * A SimBreakException is created when a breakpoint hit occurs.
 * It contains indices into the breakpoint list for
 * any breakpoints hit.
 * 
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
@SuppressWarnings("all")
public class SimBreakException extends SimException {

   /** List of breakpoint indices */
   ArrayList bpList;
   
   /**
    * Default constructor
    */
   public SimBreakException () {
      super();
      bpList = new ArrayList();
   }
             
   /**
    * Constructor with breakpoint number
    *
    * @param index The breakpoint number hit
    */
   public SimBreakException(int index) {
      this();
      append(index);
   }

   /**
    * Append a breakpoint number
    */
   public void append (int index) {   
      bpList.add(new Integer(index));
   }
                
   /**
    * Return the message. Starts with a line describing breakpoints included in this
    * exception, followed by any other message lines which exist.
    *
    * @return A message describing the breakpoint(s) and any other message lines which exist
    */
   public String getMessage() {       
      String b = buildBreakpointString();
      String m = super.getMessage();
      if (b==null)
         return m;
      else if (m==null)
         return b;
      else return b+"\n"+m;	 
   }
   
   /**
    * Return a String list of breakpoint numbers, comma separated, with a "hit" prefix
    *
    * @return A String list of breakpoint numbers, comma separated, with a "hit" prefix
    */
   private String buildBreakpointString () {
      int size;
      if ((size = bpList.size())==0)
         return null;
	 
      String s = "";	 
      for (int i=0; i<size; i++)
         s += (i>0?", ":"")+((Integer)bpList.get(i)).intValue();
      return "hit breakpoint"+(size>1?"s ":" ")+s;            
   }                 
}
