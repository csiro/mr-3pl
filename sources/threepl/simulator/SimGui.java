/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */


package threepl.simulator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;

/**
 * SimGui provides the GUI interface to the threepl simulator.
 * 
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
@SuppressWarnings("all")
public class SimGui implements SimUserInt {

    /** Reference to the single Sim object */
    private Sim sim;

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
    public SimGui(Sim sim) {
        this.sim = sim;

        /* Build main window.. */
	
        /* Set default window decorations */
        JFrame.setDefaultLookAndFeelDecorated(true);

        /* Create and set up the window */
        JFrame frame = new JFrame(Sim.SIMULATOR_NAME);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        /* Create the menu bar.  Make it have a cyan background. */
        JMenuBar cyanMenuBar = new JMenuBar();
        cyanMenuBar.setOpaque(true);
        cyanMenuBar.setBackground(Color.cyan);
        cyanMenuBar.setPreferredSize(new Dimension(200, 20));

        /* Create a yellow label to put in the content pane. */
        JLabel yellowLabel = new JLabel();
        yellowLabel.setOpaque(true);
        yellowLabel.setBackground(Color.yellow);
        yellowLabel.setPreferredSize(new Dimension(200, 180));

        /* Set the menu bar and add the label to the content pane. */
        frame.setJMenuBar(cyanMenuBar);
        frame.getContentPane().add(yellowLabel, BorderLayout.CENTER);

        /* Display the window */
        frame.pack();
        frame.setVisible(true);
    }

   /**
    * Return true if this is a SimGui.
    * Required by SimUserInt interface.
    *
    * @return True
    */
   public boolean isGui () {
      return true;
   }      
       
   /**
    * Return true if this is a SimCli.
    * Required by SimUserInt interface.
    *
    * @return False
    */
   public boolean isCli () {
      return false;
   }      
   
   /**
    * Print a string to standard output.
    * Required by SimUserInt interface.
    *
    * @param s The String to print
    */
   public void print (String s) {
      System.out.print(s);
   }
   
   /**
    * Print a string with appended newline to standard output.
    * Required by SimUserInt interface.
    *
    * @param s The String to print
    */
   public void println (String s) {
      System.out.println(s);
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

   /** Close user interface */
   public void close () {
   
   }            

   /** Print a prompt */
   public void prompt () {
      // No action yet
   }         
                  
}
