/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */

package threepl.simulator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This class handles trace data storage and retrieval from a logfile, and data display
 * in a terminal emulation window. A read stream is always opened on the logfile, for display.
 * If the logfile is being created, ie not preexisting, a write stream is also opened.
 * A separate thread (refer DisplayThread) handles reading the logfile via the read
 * stream. Because logfile reads and writes are asynchronous, all log file access methods
 * are synchronized. The logfile is a text file.
 * 
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
public class SimTraceLogger extends SimLogger {

   /* BufferedWriter for writing logfile */
   private BufferedWriter out=null;
   
   /* BufferedReader for reading logfile */
   private BufferedReader in=null;
   
   /* Thread for asynchronous logfile display in terminal emulation */
   private DisplayThread displayThread;
   
   /* Flag for displayThread running */
   private boolean displayRunning=false;
      
   /**
    * Constructor with expressions to trace
    * @param sim The Sim object
    * @param name The trace name, which is used to construct a new logfile name
    * @param exprs An array of expressions to be traced
    */
   public SimTraceLogger (Sim sim, String name, SimExprSimpleNode[] exprs) {
      super(sim, name, exprs);
      init(exprs, null);
   }
   
   /**
    * Constructor with existing filename
    * @param sim The Sim object
    * @param name The trace name, which may be used to construct a logfile name
    * @param filename The logfile name, or null if name should be used as base filename
    */    
   public SimTraceLogger (Sim sim, String name, String filename) {
      super(sim, name);
      init(null, filename);
   }

   /*
    * Helper method for constructors. Open new logfile for writing (unless reading from preexisting trace
    * logfile), open logfile separately for reading and open the trace display.
    * 
    * @param exprs An array of expressions to be traced, or null if using preexisting logfile
    * @param filename Logfile name: to create if exprs!=null, else a preexisting logfile to open
    */         
   private void init (SimExprSimpleNode[] exprs, String filename) {
         
      /* If filename is null, use name */
      filename = filename!=null ? filename : name;      
      /* If filename contains a "/", use as is else convert to a full trace file name, with path */
      filename = nameToFilename(filename);
      
      /* Create trace file path if doesn't yet exist */      
      if (!SimFile.existsTraceFilePath())
         SimFile.makeTraceFilePath();

      this.filename = filename;
      
      /* Open new file if creating */ 
      if (exprs!=null)
         openWriter();
      
      /* Open file for reading */
      openReader();
      
      /* Open terminal emulation to display trace */
      openDisplay();	 
   }	

   /**
    * Convert name to trace filename with path. No change if name contains "/".
    *
    * @param name The base name used to construct the filename
    *
    * @return Full pathname to log file, based on name
    */
   protected String nameToFilename (String name) {
      if (!name.matches(".*/.*")) 
	 name = SimFile.makeTraceFileName(sim.getSourceName()+"."+name);
      return name;	    
   }
   
   /** Close logger and the display window */
   public void close () {
      /* Close in reverse order to which opened */
      closeDisplay();
      closeReader();
      closeWriter();
   }
   	
   /** Open the log file writer  */
   public synchronized void openWriter () {
      try {
         out = new BufferedWriter(new FileWriter(filename));
      } catch (IOException e) {
         throw new SimException(e.getMessage());
      }
   }
   
   /**
    * Write a line of text to the file writer. Newline is added.
    *
    * @param line A line of text to log (newline is added)
    */
   public synchronized void write (String line) {
      try {
	 out.write(line + "\n");
	 out.flush();
	 notifyAll();
      } catch (IOException e) {
         throw new SimException(e.getMessage());
      }
   }	

   /** Close the file writer */
   public synchronized void closeWriter () {
      if (out==null)
         return;
      try {
         out.close();
	 out = null;
	 notifyAll();
      } catch (IOException e) {
         throw new SimException(e.getMessage());
      }
   }
   
   /** Open the file reader */
   public synchronized void openReader () {
      try {
         in = new BufferedReader(new FileReader(filename));	 
      } catch (IOException e) {
         throw new SimException(e.getMessage());
      }
   }
   
   /**
    * Read and return a line of text from the file reader.
    * Blocks until there is a line available.
    *
    * @return The next line of text available from the log file (not newline terminated)
    */
   public synchronized String readString () {
       String s=null;

       while(true) {
	  if (in==null || !displayRunning)
	     return null;
	  try {   
             if ((s = in.readLine())!=null)
	        break;
	     try {
	        wait();		   
             } catch (InterruptedException e) { }
          } catch (IOException e) {		
             throw new SimException(e.getMessage());
          }	      
       }
       return s;
   }
   
   /** Close the file reader */
   public synchronized void closeReader () {
      if (in==null)
         return;
      try {
         in.close();
	 in = null;
      } catch (IOException e) {
         throw new SimException(e.getMessage());
      }
   }
   
   /**
    * Prepare to display the contents of the logfile in a terminal emulation
    */
   public void openDisplay () {
      /* Only allow one logger display, it's not reentrant */
      if (displayRunning)
         return;
	 
      displayThread = new DisplayThread(this);
      if (displayRunning)
         displayThread.start();
   }

  /**
   * Display the logfile data in a window
   *
   * @param n An index used to offset the initial window position
   */
   public void show (String title, int n) {
      displayThread.show(title, n);
   }
     
   /** Close the logfile display */       
   public synchronized void closeDisplay () {

      /* Stop displayThread */
      displayRunning = false;
      notifyAll();
      
      /* Inform the sim object the display is closing so trace can be removed */
      sim.closeNotify(this);
   }

   /* Inner class which provides a thread to handle the logfile display */
   private class DisplayThread extends Thread {
   
      /* Terminal emulator for display */
      private SimTerminal terminal;
      
      // Lines read from file so far
      private int lineCount;
                     
      /*
       * Constructor with SimLogger, window title and window offset index
       *
       * @param logger The SimLogger for which to display the logfile
       */	       
      protected DisplayThread (SimLogger logger) {
         super();
         terminal = new SimTerminal(logger);
	 displayRunning = true;	
	 lineCount = 0;
      }

      /*
       * Show the logfile data in a window
       *
       * @param n An index used to offset the initial window position
       */
      protected void show (String title, int n) {
         terminal.show(title, n);
      }
            
      /* Run thread */
      public void run () {
	 
         /* Read and display logfile lines until closeDisplay() is called */
         while (displayRunning) {
	    try {
	    
	       // If first line in file, print as heading..
	       if (lineCount==0)
	          terminal.printHeading(readString());
	       
	       // ..else print in main text area
	       else		  
                  terminal.println(readString());
	       lineCount++;
	       
            } catch (SimException e) {
	       sim.getUserInt().errorOutLn(e.getMessage());  
	    }	       
         }	    
	 terminal.close();    
      }
   }   

}      
