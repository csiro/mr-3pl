/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */


package threepl.simulator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


/**
 * SimCli provide the command line interface to the threepl simulator.
 * It provides a command loop with prompt, and various methods for
 * printing normal output and errors.
 * 
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
@SuppressWarnings("all")
public class SimCli implements SimUserInt {

    /** Prompt string */
    private static final String PROMPT = "> ";

    /** Reference to the single Sim object */
    private Sim sim;

    /** Keyboard interface */
    private BufferedReader stdIn;

    /** Message queue */
    private MessageQueue messageQueue;
        
    /**
     * Unique identifier string for this class and build used to help
     * debugging and binary tracking
     */
    private String _class_cvs_id = "$id$";

    /**
     * Constructor
     * 
     * @param sim The single Sim object
     */
    public SimCli(Sim sim) {
        this.sim = sim;
	
	/* Open standard input */
        stdIn    = new BufferedReader(new InputStreamReader(System.in));
	
	/* Set up message queue and thread for asynchronous output */
	messageQueue = new MessageQueue();
	messageQueue.start();
	
	/* Print version info */
        println("\n *** "+Sim.SIMULATOR_NAME+" ***\n");
    }

    /** Command loop to prompt for and execute user commands */
    public void doCommand() {
        String line;
	
        // Display prompt
        prompt();
	
	// Wait for and execute next command
        try {
           if (((line = stdIn.readLine()) != null) && !line.equals(""))
              SimExpr.newCommand(sim, line).eval();
	      
        } catch (SimExprParseException e) {
	   println(e.getMessage());	   
        } catch (SimExprTokenMgrError e) {
	   println(e.getMessage());	   
        } catch (SimException e) {
	   println(e.getMessage());	   
        } catch (IOException e) {
	   println(e.getMessage());
        }
    }

   /**
    * Return true if this is a SimGui.
    * Required by SimUserInt interface.
    *
    * @return False
    */
   public boolean isGui () {
      return false;
   }      
       
   /**
    * Return true if this is a SimCli.
    * Required by SimUserInt interface.
    *
    * @return True
    */
   public boolean isCli () {
      return true;
   }      
   
   /**
    * Print a string to standard output.
    * Required by SimUserInt interface.
    *
    * @param s The String to print
    */
   public void print (String s) {
      messageQueue.add(s);
   }
   
   /**
    * Print a string with appended newline to standard output.
    * Required by SimUserInt interface.
    *
    * @param s The String to print
    */
   public void println (String s) {
      print(s +"\n");
   }
   
   /**
    * Print a string to standard error.
    * Required by SimUserInt interface.
    *
    * @param s The String to print
    */
   public void errorOut (String s) {
      print(s);
   }
   
   /**
    * Print a string with appended newline to standard error.
    * Required by SimUserInt interface.
    *
    * @param s The String to print
    */
   public void errorOutLn (String s) {
      println(s);
   } 

   /** Close the SimCli */
   public void close () {
      messageQueue.close();
      print("");  // need this to wake up message queue so it can close
   }

   /** Print a prompt */
   public void prompt () {
      print(PROMPT);
   }         
                  
   /**
    * Inner class to queue messages and print them when available,
    * so can different threads can all print to the same SimCli
    */
   private class MessageQueue extends Thread {
   
      ArrayList queue;
      boolean running;
      
      public MessageQueue () {
         queue= new ArrayList();
	 running = true;
      }
      
      public synchronized void add (String s) {
         queue.add(s);	       
	 notifyAll();
      }
      
      public synchronized String get () {      
         while (queue.isEmpty()) {
	    try {
	       wait();
            } catch (InterruptedException e) { }	       
         }	    
	 return (String)queue.remove(0);
      }

      public void run () {
         while (running)
	    print(messageQueue.get());
      }
      
      public synchronized void close () {
         running = false;
	 notifyAll();
      }
      	 
      private void print (String s) {
         System.out.print(s);      
      }
   }
                                
}
