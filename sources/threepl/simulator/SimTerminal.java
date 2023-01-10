/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */


package threepl.simulator;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * This class provides a read-only text terminal emulation. It interacts with
 * the SimTraceLogger class to display trace log file data.
 * 
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
public class SimTerminal extends WindowAdapter implements ActionListener {

   /* The container frame for the terminal emulation */
   private JFrame frame;

   // The text area containing a heading line at the top which doesn't scroll   
   private JTextArea headingArea;
   
   /* The text area containing the displayed text */
   private JTextArea textArea;
   
   /* Close button */
   private JButton closeButton;
   
   /* The SimLogger from which data will be displayed */
   private SimLogger logger;
   
   /**
    * Constructor with SimLogger
    *
    * @param logger The SimLogger from which data will be displayed
    */
   public SimTerminal (SimLogger logger) {
      this.logger = logger;
      
      // Pass to terminal init and indicate a heading line will be used	 
      init (true);
   }	

   /* Helper method for constructor. Initialize and display window. */
   private void init (boolean headingLine) {

      /*
       * Suggest that the L&F (rather than the system)
       * decorate all windows.  This must be invoked before
       * creating the JFrame.  Native look and feels will
       * ignore this hint.	
       */
       JFrame.setDefaultLookAndFeelDecorated(true);

       /* Create and set up the window */
       frame = new JFrame();
       frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
       frame.addWindowListener(this);

       // Set up heading area if supplied
       JPanel headingPanel = null;
       if (headingLine) {
          headingArea = new JTextArea(1, 80);
          headingArea.setFont(Font.getFont("Default"));
          headingArea.setLineWrap(false);
          headingArea.setWrapStyleWord(false);
          headingArea.setEditable(false);
	  
	  headingPanel = new JPanel();
          headingPanel.setLayout(new BoxLayout(headingPanel, BoxLayout.X_AXIS)); 
          headingPanel.setBorder(BorderFactory.createEmptyBorder(10,10,0,10));
	  headingPanel.add(headingArea);
	  headingPanel.add(Box.createHorizontalGlue());	  
       }
       
       /* Set up text area, make non-editable */
       textArea = new JTextArea(24, 80);
       textArea.setFont(Font.getFont("Default"));
       textArea.setLineWrap(true);
       textArea.setWrapStyleWord(false);
       textArea.setEditable(false);

       /* Add vertical scrolling */
       JScrollPane scrollPane = new JScrollPane(textArea);
       scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
       scrollPane.setBorder(
          BorderFactory.createEmptyBorder(headingLine?1:10,10,10,10));
       //scrollPane.setPreferredSize(new Dimension(250, 250));

       /* Close button */
       JPanel buttonPanel = new JPanel();
       buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS)); 
       buttonPanel.setBorder(BorderFactory.createEmptyBorder(0,10,10,10));
       buttonPanel.add(Box.createHorizontalGlue());
       closeButton = new JButton("Close");
       closeButton.addActionListener(this);
       buttonPanel.add(closeButton);
       
       /* Put scrollPane, textarea, close button in the frame */
       Container contentPane = frame.getContentPane();
       if (headingLine)
          contentPane.add(headingPanel, BorderLayout.NORTH);
       contentPane.add(scrollPane, BorderLayout.CENTER);
       contentPane.add(buttonPanel, BorderLayout.SOUTH);
       frame.pack();

       /* Display window */
       frame.setVisible(true);
   }
   
   /**
    * Action listener for close button
    *
    * @param e ActionEvent causing this call (not used)
    */
   public void actionPerformed(ActionEvent e) {          
   
      /* Button pressed */
      String s = e.getActionCommand();
      if (s.equals("Close"))
         close(); 
   }
   
   /**
    * Display window
    *
    * @param title The window title
    * @param n A number controlling window x and y offset on the screen: (x,y) = (50*(n+1), 50*(n+1))
    */
   public void show (String title, int n) {
      frame.setTitle(title);
      frame.setLocation(50*(n+1), 50*(n+1));
      frame.setVisible(true);   
   }
   
   /**
    * Print a line of text to the terminal at the current position.
    * Newline is added.
    *
    * @param line The line of text to print (newline is added)
    */
   public void println (String line) {
      if (line!=null) {
         textArea.append(line + "\n"); 
	 
	 // Force window to scroll to latest text entered  
	 textArea.setCaretPosition(textArea.getDocument().getLength());
      }	 
   }

   /**
    * Print a line of text to the terminal in the heading area, if exists.
    * If not, just do println.
    *
    * @param line The line of text to print
    */
   public void printHeading (String line) {
      if (line!=null) {
         if (headingArea!=null)
            headingArea.append(line);   
	 else   
            textArea.append(line + "\n");   
      }	 
   }

   /** 
    * Initiate closing the terminal.
    * Called as a result of user manually closing the window with the Close button,
    * or from logger.closeDisplay().
    */    
   public void close () {
      windowClosing(null);
   }
            
   /**
    * Close the window and dispose of resources. Called by close(), or automatically called as a
    * result of user manually closing the window with the window [x] button.
    * 
    * @param e Details of the window close event (not used)
    */ 	    
   public void windowClosing (WindowEvent e) {

      /* Set frame=null after hiding to avoid infinite loop via windowClosed()..logger.close()..close()*/
      if (frame!=null) {
         frame.setVisible(false);
         frame.dispose();
         frame = null;
      }      
   }

   /**
    * Called automatically as a result of windowClosing().
    * 
    * @param e Details of the window close event (not used)
    */ 	    
   public void windowClosed (WindowEvent e) {
      logger.close();   
   }
}      
