/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */


package threepl.simulator;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

/**
 * This class interacts with the SimPlotLogger class to provide a window to display a number of threepl
 * expressions, each as a separate time series plot. The window contains a vertically scrolling scrollpane
 * which contains the graphs, in a single column. The number of graphs displayed and the title (y axis label)
 * of each is determined from the constructor arguments. There are a number of buttons below the scrollpane:
 * X and Y autoscale, grid control, and window close buttons.
 * 
 * All graphs have a common timebase, being clock edges since the window was opened. The name of the clock
 * used as timebase is passed to the constructor. The timebase, or X axis, is displayed at the top of the
 * scrollpane (in the scrollpane column header). The X axis has Pan and Zoom controls, which are only enabled
 * when X autoscaling is switched off. (Both X and Y autoscaling are enabled by default when the plot window
 * is opened). The left and right arrows on the Pan and Zoom controls allow incrementing and decrementing the
 * X axis offset and range respectively. The Zoom control changes X axis range in logarithmic steps. The major
 * tick interval and number of minor ticks (between major ticks) are determined automatically from the range.
 * The Pan control changes the X axis offset by one minor tick each click. If X autoscaling is enabled (button
 * on main panel under graph scrollpane), the Pan and Zoom controls are disabled and the X axis scales itself
 * automatically to include all data available.
 * 
 * Each graph has its own Y axis, scaled separately. All the Y axes are located in the scrollpane row header.
 * The appearance of a graph is affected by the data type being displayed. Data type (refer interface
 * SimTypes) falls into either numeric or boolean categories. Numeric graphs contain a numerically scaled Y
 * axis, with a Range and Offset control (refer private class Scaler). The up and down arrows on these
 * controls allow incrementing and decrementing these quantities, in a similar way to the X axis Pan and Zoom
 * controls. If Y autoscaling is enabled, (button on main panel under graph scrollpane), all graph Range and
 * Offset controls are disabled and all Y axes scale themselves automatically to fit the available data within
 * the current X axis range. Boolean graphs do not have Range and Offset controls as they display only two
 * values, true or false. Y scaling on boolean graphs never changes. 
 *
 * For numeric axes, both X and Y, major tick marks are numbered in integer format for INT,UINT type data
 * (note X axis is always INT). For FLOAT data, f format is used with the number of decimal places
 * set according to the major tick interval. If the number will not fit into 8 character spaces, e format
 * is used.
 *
 * A grid is superimposed over all graphs, aligned with axes ticks. This may be switched on or off with a
 * button on the main panel.
 * 
 * There are a number of triggers to redrawing the graphs. Firstly, the graphs are redrawn whenever a button
 * click changes the X or Y scaling, or autoscaling state is changed, or the grid is switched on or off.
 * Secondly, redraw occurs when new data becomes available in the logger object (which supplies the data). The
 * logger calls the plot() method to effect this. Thirdly, redraw occurs as a result of automatic screen
 * update, eg when the plot window becomes visible from behind another window.
 * 
 * Data is plotted by the Graph.paintComponent() method. This is called automatically during screen update.
 * This method requests an array of SimSamples samples from the logger object. The range of samples requested
 * corresponds to the current X axis range and offset. If X autoscaling is enabled, then all available samples
 * are requested instead. Each SimSample contains a value for each of the graphs being displayed. If Y
 * autoscaling is enabled, then each numeric Y axis is rescaled according to its corresponding data range
 * before the data is displayed.
 * 
 * Occasionally a numeric datum may be unplottable. This occurs when an expression being plotted contains a
 * runtime error (eg divide by zero), or contains a queue which is unavailable, or contains an array
 * expression which results in a multivalued result, as opposed to a single value. In these cases, the
 * logger marks the datum as invalid within the sample, and a vertical grey bar is plotted instead of a data
 * value for that sample.
 
 * @version $Revision: 8254 $
 * @author Andrew Tulloh
 */
@SuppressWarnings("all")
public class SimPlotWindow extends WindowAdapter implements ActionListener, SimTypes, SwingConstants {

   /* Colours used for axes, data etc. */
   private static Color MAJOR_GRID_COLOR = Color.GRAY;
   private static Color MINOR_GRID_COLOR = Color.LIGHT_GRAY;
   private static Color AXIS_COLOR = Color.BLACK;
   private static Color NUMERIC_DATA_COLOR = Color.GREEN;
   private static Color LOG_DATA_COLOR = Color.MAGENTA;
   private static Color CONTROL_DATA_COLOR = Color.BLUE;
   private static Color INVALID_DATA_COLOR = Color.LIGHT_GRAY;
   private static Color CLOCKTICK_COLOR = Color.BLUE;
      
   /* Some borders */
   private Border emptyBorder = BorderFactory.createEmptyBorder();     
   private Border lowBevelBorder = BorderFactory.createLoweredBevelBorder();       
   private Border raiseBevelBorder = BorderFactory.createRaisedBevelBorder();       

   /* The container frame */
   private JFrame frame;

   /* Graphs, with axes and data */
   private Graph graph[];   
   
   /* Common graph colum header (x axis) */
   private XDescriptor colheader;

   // Clock ticks on/off button
   private JButton clocksButton;
      
   /* Grid on/off button */
   private JButton gridButton;
   
   /* Close button */
   private JButton closeButton;
   
   /* The SimPlotLogger from which data will be displayed */
   private SimPlotLogger logger;

   /**
    * Standalone main test method
    *
    * @param args Not used
    */
   public static void main (String[] args) {
   
      int[] exprTypes = { INT, LOG, INT, FLOAT };
      String[] exprNames = { "int0", "log1", "int2", "float3" };
      new SimPlotWindow(null, exprTypes, exprNames);
   }
      
   /**
    * Constructor. Builds the graphs and prepares plot window for display.
    *
    * @param logger The SimPlotLogger from which data will be displayed
    * @param exprTypes Array of SimTypes types giving the type of data in each graph
    * @param exprNames Array of graph expression names (y axis labels)
    */
   public SimPlotWindow (SimPlotLogger logger, int[] exprTypes, String[] exprNames) {
      this.logger = logger;
      init (exprTypes, exprNames);
   }	

   /*
    * Initialize window
    *
    * @param exprTypes Array of SimTypes types giving the type of data in each graph,
    * followed by array of booleans with (associated) clock active edge indicators.
    * Note the first element is the time (x axis) value.
    * @param exprNames Array of graph expression names (y axis labels)
    */
   private void init (int[] exprTypes, String[] exprNames) {

      // Check arg validity
      if (exprTypes.length != exprNames.length)
         throw new SimException("SimPlotWindow.init(String[], int[]): mismatched arrays");
      int numGraphs = exprNames.length-1;
	 
      /*
       * Suggest that the L&F (rather than the system)
       * decorate all windows.  This must be invoked before
       * creating the JFrame.  Native look and feels will
       * ignore this hint.	
       */
       JFrame.setDefaultLookAndFeelDecorated(true);
       frame = new JFrame();
       frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
       frame.addWindowListener(this);
       
       /* Create and place the display components.. */
       
       /* Graphs and x and y axis descriptors */
       GridBagLayout graphLayout = new GridBagLayout();
       GridBagConstraints constraints = new GridBagConstraints();
       constraints.fill = GridBagConstraints.BOTH;
       constraints.gridwidth = GridBagConstraints.REMAINDER;
       constraints.weightx = 1.0;
       int width = 130;
       int height, totalHeight=0;
       
       JPanel graphContainer = new JPanel(graphLayout);
       JPanel rowheaderContainer = new JPanel(graphLayout);       
       colheader = new XDescriptor("Time since reset (ns)");
       colheader.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
       
       YDescriptor rowheader;
       graph = new Graph[numGraphs];
              
       for (int i=0; i<numGraphs; i++) {
       
          // Make LOG, CONTROL and NULL_TYPE graphs take up less height than other types
          constraints.weighty = exprTypes[i+1]==LOG||exprTypes[i+1]==CONTROL||exprTypes[i+1]==NULL_TYPE? 0.5 : 1.0;
	  height = exprTypes[i+1]==LOG||exprTypes[i+1]==CONTROL||exprTypes[i+1]==NULL_TYPE? 55 : 112;
	  totalHeight += height;
	  
	  rowheader = new YDescriptor(exprNames[i+1], exprTypes[i+1]);
	  rowheader.setMinimumSize(new Dimension(width, height));
	  rowheader.setPreferredSize(new Dimension(width, height));
	  rowheader.setBorder(lowBevelBorder);
          graphLayout.addLayoutComponent(rowheader, constraints);	  	  
	  rowheaderContainer.add(rowheader);

	  graph[i] = new Graph(colheader, rowheader, i);
	  graph[i].setMinimumSize(new Dimension(0, height));
	  graph[i].setPreferredSize(new Dimension(0, height));
	  graph[i].setBorder(lowBevelBorder);
          graphLayout.addLayoutComponent(graph[i], constraints);	  	  
	  graphContainer.add(graph[i]);	  
       }

       /* Scroll pane which holds graphs etc. */       
       JScrollPane scrollPane = new JScrollPane(graphContainer);
       scrollPane.setVerticalScrollBarPolicy(
                       JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
       scrollPane.setHorizontalScrollBarPolicy(
                       JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
       scrollPane.setRowHeaderView(rowheaderContainer);
       scrollPane.setColumnHeaderView(colheader);
       scrollPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
       scrollPane.getViewport().setPreferredSize(new Dimension(500, totalHeight+5*Math.min(3, numGraphs)));
       scrollPane.getViewport().setMinimumSize(new Dimension(300, 0));
       
       /* Close and other buttons */
       JPanel buttonPanel = new JPanel();
       buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS)); 
       buttonPanel.setBorder(BorderFactory.createEmptyBorder(0,10,10,10));
       buttonPanel.add(Box.createHorizontalGlue());
       
       clocksButton = new JButton("Clocks on");
       clocksButton.addActionListener(this);
       setClocksOn();
       buttonPanel.add(clocksButton);
       buttonPanel.add(Box.createHorizontalStrut(10));

       gridButton = new JButton("Grid off");
       gridButton.addActionListener(this);
       setGridOn();
       buttonPanel.add(gridButton);
       buttonPanel.add(Box.createHorizontalStrut(10));
       
       closeButton = new JButton("Close");
       closeButton.addActionListener(this);
       buttonPanel.add(closeButton);

       /* Overall container */
       Container contentPane = frame.getContentPane();
       contentPane.add(scrollPane, BorderLayout.CENTER);
       contentPane.add(buttonPanel, BorderLayout.SOUTH);
       
       /* Pack */
       frame.pack();       
   }              

   /**
    * Display plot window
    *
    * @param title The window title
    * @param n A number controlling window x and y offset on the screen: (x,y) = (50*(n+1), 50*(n+1))
    */
   public void show (String title, int n) {
      frame.setTitle(title);
      frame.setLocation(50*(n+1), 50*(n+1));
      frame.setVisible(true);   
   }
      
   /** Plot graphs. Called from logger when new sample written to logfile */    
   public void plot () {
      for (int i=0; i<graph.length; i++)
	 graph[i].repaint();
   }
                             
   /**
    * Initiate closing the window.
    * Called as a result of user manually closing the window with the Close button,
    * or from logger.closeDisplay().
    */   
   public void close () {
      windowClosing(null);
   }
 
   /**
    * Action listener. Handles main panel button presses.
    *
    * @param e ActionEvent which triggered this call (not used)
    */    
   public void actionPerformed(ActionEvent e) {          
   
      /* Button pressed */
      String s = e.getActionCommand();
      if (s.equals("Close"))
         close(); 
      else if (s.equals("Clocks off"))
         setClocksOn();
      else if (s.equals("Clocks on"))
         setClocksOff();	
      else if (s.equals("Grid off"))
         setGridOn();
      else if (s.equals("Grid on"))
         setGridOff();	
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

   /* Switch on clock ticks for all graphs */
   private void setClocksOn () {
      clocksButton.setText("Clocks on");	 
      for (int i=0; i<graph.length; i++) {
         graph[i].setClocksOn();
         graph[i].repaint();
      }	 
   }

   /* Switch off clock ticks for all graphs */
   private void setClocksOff () {
      clocksButton.setText("Clocks off");	 
      for (int i=0; i<graph.length; i++) {
         graph[i].setClocksOff();
         graph[i].repaint();
      }	 
   }
   
   /* Switch on graph grid for all graphs */
   private void setGridOn () {
      gridButton.setText("Grid on");	 
      for (int i=0; i<graph.length; i++) {
         graph[i].setGridOn();
         graph[i].repaint();
      }	 
   }

   /* Switch off graph grid for all graphs */
   private void setGridOff () {
      gridButton.setText("Grid off");	 
      for (int i=0; i<graph.length; i++) {
         graph[i].setGridOff();
         graph[i].repaint();
      }	 
   }
   
   /* Class which holds all info for a graph */
   private class Graph extends JPanel implements ActionListener, MouseMotionListener {
      
      /* Column header (x axis) */
      private XDescriptor colheader;
      
      /* Row header (y axis) */   
      private YDescriptor rowheader;
      
      /* Data type (from SimTypes) */     
      private int type;
      
      /* X axis range */
      private double xRange;
      
      /* X axis offset */
      private double xOffset;      
      
      /* Y axis range */
      private double yRange;

      /* Y axis offset */
      private double yOffset;
      
      /* X major tick interval (major ticks are numbered) */
      private double xTick;
      
      /* Number of X minor ticks (ticks between major ticks; minor ticks are not numbered) */
      private int xMinorTicks=0;
      
      /* Y major tick interval */
      private double yTick;

      /* Number of X minor ticks */
      private int yMinorTicks=0;

      /* Clock ticks displayed flag */
      private boolean clocksOn=false;
            
      /* Grid displayed flag */
      private boolean gridOn=false;
      
      /* X autoscale enabled flag */
      private boolean autoscaleXOn=false;
      
      /* Y autoscale enabled flag */
      private boolean autoscaleYOn=false;

      /* Variables used during painting.. */
      
      /* Graphics context */
      private Graphics g;
      
      /* New improved graphics context */
      private Graphics2D g2;
      
      /* X length, min and max values (screen coordinates) */
      private int xlength, xmin, xmax;

      /* Y length, min and max values (screen coordinates) */
      private int ylength, ymin, ymax;

      /* Index of this graph in the overall plot */
      private int index;

      /* Constructor
       *
       * @param colheader Column header (X axis) for this graph
       * @param rowheader Row header (Y axis) for this graph
       * @param index Index of this graph in the overall plot
       */
      protected Graph (XDescriptor colheader, YDescriptor rowheader, int index) {
         super();
	 this.index = index;
	 this.colheader = colheader;
         this.rowheader = rowheader;
	 type = rowheader.getType();
	 
	 /* Set ranges and offsets to legal initial values */
	 setXRange(100);
	 setXOffset(0);
	 if (SimType.isNumeric(type)) {
	    setYRange(1);
	    setYOffset(0);
	 }
	 
	 /* Add action listener for X axis pan and zoom buttons */
	 colheader.getZoomScaler().getUpButton().addActionListener(this);
	 colheader.getZoomScaler().getDownButton().addActionListener(this);
	 colheader.getPanScaler().getUpButton().addActionListener(this);
	 colheader.getPanScaler().getDownButton().addActionListener(this);
	 colheader.getAuto().addActionListener(this);

          // Set X autoscaling initially
	 setAutoscaleXOn();
	 
	 /* If graph data is numeric type, add action listener for auto, range and offset buttons */
	 if (SimType.isNumeric(type)) {
	    rowheader.getRangeScaler().getUpButton().addActionListener(this);
	    rowheader.getRangeScaler().getDownButton().addActionListener(this);
	    rowheader.getOffsetScaler().getUpButton().addActionListener(this);
	    rowheader.getOffsetScaler().getDownButton().addActionListener(this);
	    rowheader.getAuto().addActionListener(this);

            // Set Y autoscaling initially
	    setAutoscaleYOn();	 	 
	 }

	 // Listen for mouse movement over graph so can display data coordinates when mouse is near
	 addMouseMotionListener(this);
      }	 	       

      /**
       * Action listener. Handles pan/zoom/range/offset button presses.
       *
       * @param e ActionEvent which triggered this call (not used)
       */
      public void actionPerformed(ActionEvent e) {          
   
         /* Button pressed */
         String s = e.getActionCommand();
	 if (s.equals("ZoomUp"))
	    incXRangeAndCentre();
	 else if (s.equals("ZoomDown"))
	    decXRangeAndCentre();
	 else if (s.equals("PanUp"))
	    incXOffset();
	 else if (s.equals("PanDown"))
	    decXOffset();	    
         else if (s.equals("RangeUp"))
	    incYRangeAndCentre();
         else if (s.equals("RangeDown"))
	    decYRangeAndCentre();
         else if (s.equals("OffsetUp"))
	    incYOffset();
         else if (s.equals("OffsetDown"))
	    decYOffset();
         else if (s.equals("AutoX")) {
	    if (autoscaleXOn)
	       setAutoscaleXOff();
            else
	       setAutoscaleXOn();	
         } else if (s.equals("AutoY")) {
	    if (autoscaleYOn)
	       setAutoscaleYOff();
            else
	       setAutoscaleYOn();	
         }	              	    

         /* Schedule graph for redraw */	    
	 repaint();	    
      }

      /** 
       * Mouse moved over graph area. Set tool tip text to display graph coord if near one.
       * @param e Mouse movement event
       */
      public void mouseMoved (MouseEvent e) {

         String text = null;
	 SimSample sample;
	      
         // Raw screen coords relative to graph origin
         int xRawMouse = e.getX();
	 int yRawMouse = e.getY();
	 
	 // Convert to axis units (x,y)
	 double xMouse = xToVal(xRawMouse);
	 double yMouse = yToVal(yRawMouse);

         // If can read a sample at or before the x time..
	 if ((sample = logger.readSample(xMouse)) != null) {

            boolean ySampleBoolean = false;
	    double ySample = 0;
	    int yRawSample = 0;
	    	 
	    // Get actual x in units
	    double xSample = sample.get(0).getDouble();

            // For non-null types, get y in units, convert to y screen coord
            if (SimType.isBoolean(type)) {
	       ySampleBoolean = sample.get(index+1).getBoolean();
	       yRawSample = ySampleBoolean ? ymin+5 : ymax-5;	    
	    } else if (SimType.isNumeric(type)) {
	       ySample = sample.get(index+1).getDouble();
	       yRawSample = valToY(ySample);
	    }

            // If clock ticks are displayed, and mouse is in the vicinity of a clock tick mark,
	    // display the time
	    int clockTickOffset = (sample.length()+1)/2;
	    if (clocksOn && sample.get(clockTickOffset+index).getBoolean()) {
	       int xRawSample = valToX(xSample);
	       int xRawMin = xRawSample-5;
	       int xRawMax = xRawSample+5;
	       int yRawMin = ymax-7;   // Allow for height of clock tick plus 5
	       int yRawMax = ymax;
	       if (xRawMouse>=xRawMin && xRawMouse<=xRawMax &&
	           yRawMouse>=yRawMin && yRawMouse<=yRawMax) {
                  text = "t="+SimPrintf.sprintf("%.1f", xSample);
		  setToolTipText(text);
		  return;		   	    
               }		  
	    }

            // If data is invalid in this x range, print "unplottable" regardless of y position
	    if (!sample.isValid(index+1)) {
	       setToolTipText("unplottable");
               return;
            }
	    
	    // ..else if data is null type, print "null" regardless of y position
	    else if (type==NULL_TYPE) {
	       setToolTipText("null");
               return;
            }
	       
	    // Otherwise display nearby y value if mouse in vicinity of a y screen coord
	    int yRawMin = yRawSample-5;
	    int yRawMax = yRawSample+5;	    
	    if (yRawMouse>=yRawMin && yRawMouse<=yRawMax) {
               switch (type) {
	       case LOG:
	          text = ySampleBoolean ? "true" : "false";
	          break;
	       case CONTROL:
	          text = ySampleBoolean ? "high" : "low";
	          break;
	       case INT:
	       case UINT:
	       case BITS:
	          text = SimPrintf.sprintf("%d", ySample);
		  break;
	       case FLOAT:
	          int places;
	          double l = ySample!=0 ? Math.log(Math.abs(ySample))/Math.log(10.) : 1;
	          if (l>=0)
	             places = 1;
                  else		  
	             places = -(int)Math.floor(l);                  		     
	          text = SimPrintf.sprintf("%."+places+"f", ySample);
	          if (text.length()>8)
	             text = SimPrintf.sprintf("%.1e", ySample);
	          break;
               }
            }	       	    	       
         }
	    
	 setToolTipText(text);  	 
      }
            
      /** 
       * Mouse dragged over graph area. Dummy function to satisfy MouseMotionListener interface
       * @param e Mouse movement event
       */
      public void mouseDragged (MouseEvent e) {}
      
      /** Repaint x and y axes when repaint graph */
      public void repaint () {
         super.repaint();
	 if (colheader!=null)
	    colheader.repaint();
	 if (rowheader!=null)
   	    rowheader.repaint();
      }
      
      /**
       * Paint the graph panel, with data and possibly grid.
       *
       * @param g Graphics context for drawing
       */            
      public void paintComponent (Graphics g) {

         super.paintComponent(g);
	 this.g = g;
	 g2 = (Graphics2D)g;
      
         Insets insets = getInsets();
	 xlength = getWidth() - insets.left - insets.right;
	 xmin = insets.left;
	 ylength = getHeight() - insets.top - insets.bottom;
	 xmax = xmin + xlength - 1;
	 ymin = insets.top;
	 ymax = ymin + ylength - 1;

         // Get samples from logger
	 SimSample[] samples = null;
	 if (logger!=null) {
	 
	    // Do x autoscale if switched on
	    if (autoscaleXOn) {
	    
	       // Search upwards from START_TIME for the largest xOffset which is not
	       // larger than the first sample time. If the first sample time is less
	       // than START_TIME (as will happen when a plot is started at RESET_TIME),
	       // set xOffset=START_TIME, ie. 0.
	       setXOffset(Sim.START_TIME);
	       double startTime = logger.getStartTime();
	       if (xOffset <= startTime) {
	          while (xOffset <= startTime)
	             incXOffset();
                  decXOffset();		  
	       }
	       
	       // Search upwards for an xRange which, when added to xOffset, is
	       // at least as large as the last sample time. Minimum xRange is 1.
	       setXRange(1);
	       double endTime = logger.getEndTime();
	       while (xRange+xOffset < endTime)
	          incXRange(); 
               colheader.repaint();		  	       
            }
	    
	    // Retrieve samples to plot
	    // Note a sample contains:
	    // element 0: The time (x) value
	    // element 1 to m: The m values being plotted (one per graph)
	    // element m+1 to 2m: The m clock active indicators (one per graph) 
	    samples = logger.readSamples(xOffset, xOffset+xRange);
         }
	 
	 // Autoscale y if switched on, data is numeric and single valued
         if (autoscaleYOn && (SimType.isNumeric(type)) &&
	     samples!=null && samples.length>0) {
	     
	    // Find index in sample array of first plottable value
	    int firstPlottable = -1;
	    for (int i=0; i<samples.length; i++) {
	       if (samples[i].isValid(index+1)) {
	          firstPlottable=i;
	          break;
               }
            }
	    
	    // Can only y autoscale if have a plottable value
	    if (firstPlottable!=-1) {	       		  

	       double yvalLow = samples[firstPlottable].get(index+1).getDouble();
	       double yvalHigh = yvalLow;
	       double yval;
	       for (int i=firstPlottable+1; i<samples.length; i++) {

                  // Ignore invalid values
		  if (!samples[i].isValid(index+1))
		     continue;
		     
		  yval = samples[i].get(index+1).getDouble();

        	  if (yval<yvalLow)
		     yvalLow = yval;

        	  else if (yval>yvalHigh)
		     yvalHigh = yval;
               }

	       // Get range just large enough to contain graph vertically
	       double ydiff = yvalHigh - yvalLow;

	       if (SimType.isInt(type)) {
		  if (ydiff<1)
	             ydiff = 1;
               } else if (ydiff==0)
		  ydiff = 1;             		  

	       if (ydiff > yRange) {
		  while (ydiff > yRange)
	             incYRange();
	       } else if (ydiff < yRange) {
		  while (ydiff < yRange)
		     decYRange();
        	  if (ydiff > yRange)		     
                    incYRange();
               }

	       // Get offset which places graph about in middle vertically, and align with minor tick
	       double minorTickInterval = yTick/(yMinorTicks+1);
	       setYOffset(Math.round((yvalLow+yvalHigh-yRange)/2 / minorTickInterval) * minorTickInterval);

	       // Update y axis
	       rowheader.repaint();
            }	    
	 }
	 	 
         // Draw grid at major and minor tick marks, if enabled. Note no grid for NULL_TYPE data.
	 if (gridOn) {

            // X axis grid..
            drawGrid (xOffset, xRange, xTick, xMinorTicks, VERTICAL);

            // Y axis grid
	    switch (type) {
	    case INT:
	    case UINT:
	    case FLOAT:
	    case BITS:
               drawGrid (yOffset, yRange, yTick, yMinorTicks, HORIZONTAL);
	       break;
            case LOG:
	    case CONTROL:
	       drawLine(false, HORIZONTAL);
	       drawLine(true, HORIZONTAL);
	       break;
            }	       	       
	 }

	 // Draw graph
	 if (samples!=null && samples.length>0) {

            // Set double line thickness for graph
            Color saveColor = g2.getColor();
	    Stroke saveStroke = g2.getStroke();
	    g2.setStroke(new BasicStroke(2));
	    boolean plotting;

            double x, xstart;
	    	 
	    switch (type) {
	    
	    // Numeric data type..
	    case INT:
	    case UINT:
	    case FLOAT:
	    case BITS:
	    
	       // Set numeric graph colour
	       g2.setColor(NUMERIC_DATA_COLOR);	

               // Plot samples..
	       double ystart=0;
	       double y;
	       
	       // Get first sample coordinate and plot the point, if valid data
	       // and if falls within x range
	       xstart = samples[0].get(0).getDouble();
	       if (samples[0].isValid(index+1)) {
	          ystart = samples[0].get(index+1).getDouble();
		  if (xstart>=xOffset)
		     drawLine(xstart, ystart, xstart, ystart);
		  plotting = true;		  
	       } else
	          plotting = false;

               // Adjust xstart to be at least as high as xoffset
	       xstart = Math.max(xstart, xOffset);		  
	       
	       // For remaining samples, plot a horizontal line from the previous
	       // sample coordinate to the current x value, then a vertical line to
	       // the current y value. If the previous sample had an invalid y value,
	       // plot 'invalid' data instead, followed by a point at the current
	       // coordinate, if valid. 
	       for (int i=1; i<samples.length; i++) {
	          x = samples[i].get(0).getDouble();
		  
		  // If last sample y value was valid..
	          if (plotting) {
		     
		     // Draw horizontal line to time of this value from last coord
		     // (y = the previous sample's value)
		     drawLine(xstart, ystart, Math.min(x, xOffset+xRange), ystart);
                  }
		  
		  // ..else last sample y value invalid
		  else
		     // Fill previous coordinate with 'invalid data'
		     plotInvalidData(xstart, Math.min(x, xOffset+xRange));
		  xstart = x;		     
		  
		  // If current sample y value is valid..
		  if (samples[i].isValid(index+1)) {
		  
		     y = samples[i].get(index+1).getDouble();
		     
		     // If graph still falls within x range..
		     if (x <= xOffset+xRange) {
		     
		        // If last sample was valid, draw a vertical line from last coord's y..
		        if (plotting)
		           drawLine(x, ystart, x, y);
			
                        // ..else just draw the current point			
		        else
		           drawLine(x, y, x, y);
                     }			   
                     ystart = y;
		     plotting = true;			
                  } else
		     plotting = false;			
               }	       
	       break;
	      
            // Boolean data type..
	    case LOG:
	    case CONTROL:
	    
	       // Set boolean graph colour
	       g2.setColor(type==LOG?LOG_DATA_COLOR:CONTROL_DATA_COLOR);
	       
               // Plot samples..
	       boolean ystartb=false;
	       boolean yb;
	       
	       // Get first sample coordinate and plot the point, if valid data
	       // and if falls within x range
	       xstart = samples[0].get(0).getDouble();
	       if (samples[0].isValid(index+1)) {
		  if (xstart>=xOffset)
	             ystartb = samples[0].get(index+1).getBoolean();
		  drawLine(xstart, ystartb, xstart, ystartb);
		  plotting = true;		  
	       } else
	          plotting = false;
	       
               // Adjust xstart to be at least as high as xoffset
	       xstart = Math.max(xstart, xOffset);		  
	       
	       // For remaining samples, plot a horizontal line from the previous
	       // sample coordinate to the current x value, then a vertical line to
	       // the current y value. If the previous sample had an invalid y value,
	       // plot 'invalid' data instead, followed by a point at the current
	       // coordinate, if valid. 
	       for (int i=1; i<samples.length; i++) {
	          x = samples[i].get(0).getDouble();
		  
		  // If last sample y value was valid..
	          if (plotting) {
		     
		     // Draw horizontal line to time of this value from last coord
		     // (y = the previous sample's value)
		     drawLine(xstart, ystartb, Math.min(x, xOffset+xRange), ystartb);
                  }
		  
		  // ..else last sample y value invalid
		  else
		     // Fill previous coordinate with 'invalid data'
		     plotInvalidData(xstart, Math.min(x, xOffset+xRange));
		  xstart = x;		     
		  
		  // If current sample y value is valid..
		  if (samples[i].isValid(index+1)) {
		  
		     yb  = samples[i].get(index+1).getBoolean();
		     
		     // If graph still falls within x range..
		     if (x <= xOffset+xRange) {
		     
		        // If last sample was valid, draw a vertical line from last coord's y..
		        if (plotting)
		           drawLine(x, ystartb, x, yb);
			
                        // ..else just draw the current point			
		        else
		           drawLine(x, yb, x, yb);
                     }			   
                     ystartb = yb;
		     plotting = true;			
                  } else
		     plotting = false;			
               }	       
	       break;

            // Null data type..
	    case NULL_TYPE:
	    	       
	       // Get first sample x coordinate and whether it is plottable
	       xstart = samples[0].get(0).getDouble();
	       plotting = samples[0].isValid(index+1);
	       
               // Adjust xstart to be at least as high as xoffset
	       xstart = Math.max(xstart, xOffset);		  
	       
	       // For remaining samples, plot 'invalid data' colour where this is the
	       // case. Valid samples are not actually drawn plotted.
	       for (int i=1; i<samples.length; i++) {
	          x = samples[i].get(0).getDouble();
		  
		  // If last sample y value was invalid, fill previous coordinate with 'invalid data'
	          if (!plotting)
		     plotInvalidData(xstart, Math.min(x, xOffset+xRange));
		  xstart = x;		     
		  
		  // If current sample y value is valid..
                  plotting = samples[i].isValid(index+1);		  
               }	       
	       break;
            }

            // Plot clock tick marks on graphs if enabled
	    if (clocksOn) {
	    
	       // Set clock tick mark colour
	       g2.setColor(CLOCKTICK_COLOR);
	    
               // Calculate the offset in the sample to the clock active boolean
	       // values, which follow the data values.
	       int clockTickOffset = (samples[0].length()+1)/2;	 
          	 	 
	       for (int i=0; i<samples.length; i++) { 
	          x = samples[i].get(0).getDouble();
                  if (x>xOffset+xRange)
		     break;
                  if (x>=xOffset && samples[i].get(clockTickOffset+index).getBoolean())
	             g2.drawLine(valToX(x), ymax, valToX(x), ymax-2);
               }		  
	    }
	    	    
	    // Restore line thickness and colour
	    g2.setStroke(saveStroke);
	    g2.setColor(saveColor);
         }
      }

      /*
       * Set X range to passed value. If x axis is integer type (always is), minimum range
       * is 1.
       *
       * @param xRange The new X range to set
       */                  	 
      private void setXRange (double xRange) {
            
         /* Calculate optimum tick size and number of minor ticks */
         double l = Math.log(xRange)/Math.log(10.);
	 double exp = Math.floor(l);
	 double mant = Math.pow(10., l-exp);
	 int minorTicks=4;

	 if (mant<(double)1.99) {
	    mant = 5;
	    exp--;
         } else if (mant<(double)4.99)
	    mant = 1;
         else if (mant<(double)9.99) {
	    mant = 2;
	    minorTicks = 3;
	 } else
	    mant = 5;

         double majorTickInterval = mant * Math.pow(10., exp);

	 /* If x axis is integer type, minimum xRange is 1 and cannot have minor ticks < 1 */
         if (SimType.isInt(colheader.getType())) {
	    if (xRange<=1) {
	       xRange = 1;
	       majorTickInterval = 1;
	       minorTicks = 0;
	    } else if (majorTickInterval / (minorTicks+1) < 1) {
	       switch (minorTicks) {
	       case 3:
	          minorTicks = 1;
	          break;
	       case 4:
	       default:
	          minorTicks = 0;
	          break;
               }
            }	       
            if (majorTickInterval / (minorTicks+1) < 1)
	       return;
         }

         this.xRange = xRange;
	 setXTicks(majorTickInterval, minorTicks);

         /* Rescale x axis */
	 colheader.setRange(xRange);
      }
      
      /* Increment X range to next step and keep horizontal graph centre in same position */
      private void incXRangeAndCentre () {
         double xCentre = xOffset+xRange/2;
	 incXRange();
	 setXOffset(xCentre-xRange/2);
      }
   	       
      /* Decrement X range to next step and keep horizontal graph centre in same position */
      private void decXRangeAndCentre () {
         double xCentre = xOffset+xRange/2;
	 decXRange();
	 setXOffset(xCentre-xRange/2);
      }
   	       
      /* Increment Y range to next step and keep vertical graph centre in same position */
      private void incYRangeAndCentre () {
         double yCentre = yOffset+yRange/2;
	 incYRange();
	 setYOffset(yCentre-yRange/2);
      }
   	       
      /* Decrement Y range to next step and keep vertical graph centre in same position */
      private void decYRangeAndCentre () {
         double yCentre = yOffset+yRange/2;
	 decYRange();
	 setYOffset(yCentre-yRange/2);
      }
   	       
      /* Increment X range to the next step. Steps are logarithmic, ie 1, 2, 5, 10, 20, .. */
      private void incXRange () {
         double l = Math.log(xRange)/Math.log(10.);
	 double exp = Math.floor(l);
	 double mant = Math.pow(10., l-exp);

         /* Go up to next range */
	 if (mant<(double)1.99)
	    mant = 2;
         else if (mant<(double)4.99)
	    mant = 5;
         else if (mant<(double)9.99) {
	    mant = 1;
	    exp++;
	 } else {
	    mant = 2;
	    exp++;
         }
	 double range =  mant * Math.pow(10., exp);
         setXRange(range);
      }

      /* Decrement X range to the next step. Steps are logarithmic, ie 100, 50, 20, 10, 5, .. */
      private void decXRange () {
         double l = Math.log(xRange)/Math.log(10.);
	 double exp = Math.floor(l);
	 double mant = Math.pow(10., l-exp);
	 	 
         /* Go down to next range */
	 if (mant>(double)5.01)
	    mant = 5;
         else if (mant>(double)2.01)
	    mant = 2;
         else if (mant>(double)1.01)
	    mant = 1;
         else {
	    mant = 5;
	    exp--;
         } 	    	       
	 double range =  mant * Math.pow(10., exp);
         setXRange(range);
      }      

      /* Increment X offset to the next step. A step is one minor tick interval. */
      private void incXOffset () {
         setXOffset(xOffset + xTick/(xMinorTicks+1));	    	       
      }
      
      /* Decrement X offset to the next step. A step is one minor tick interval. */
      private void decXOffset () {
	 setXOffset(xOffset - xTick/(xMinorTicks+1));	          
      }
      
      /*
       * Set X offset to the passed value. Minimum is 0, since the X axis shows clock edges since
       * the plot was opened.
       *
       * @param xOffset The new X offset to set
       */
      private void setXOffset (double xOffset) {
         this.xOffset = xOffset;	 
	 colheader.setOffset(xOffset);
      }
      
      /*
       * Set Y range to passed value. If Y axis is integer type, minimum range is 1.
       *
       * @param yRange The new Y range to set
       */                  	 
      private void setYRange (double yRange) {
      
         /* Calculate optimum tick size and number of minor ticks */
         double l = Math.log(yRange)/Math.log(10.);
	 double exp = Math.floor(l);
	 double mant = Math.pow(10., l-exp);
	 int minorTicks=4;

	 if (mant<(double)1.99) {
	    mant = 5;
	    exp--;
         } else if (mant<(double)4.99)
	    mant = 1;
         else if (mant<(double)9.99) {
	    mant = 2;
	    minorTicks = 3;
	 } else
	    mant = 5;

         double majorTickInterval = mant * Math.pow(10., exp);
	 
	 /* If y axis is integer type, minimum yRange is 1 and cannot have minor ticks < 1 */
         if (SimType.isInt(type)) {
	    if (yRange<=1) {
	       yRange = 1;
	       majorTickInterval = 1;
	       minorTicks = 0;
	    } else if (majorTickInterval / (minorTicks+1) < 1) {
	       switch (minorTicks) {
	       case 3:
	          minorTicks = 1;
	          break;
	       case 4:
	       default:
	          minorTicks = 0;
	          break;
               }
            }	       
            if (majorTickInterval / (minorTicks+1) < 1)
	       return;
         }

         this.yRange = yRange;
	 setYTicks(majorTickInterval, minorTicks);

         /* Rescale y axis */
	 rowheader.setRange(yRange);
      }
      
      /* Increment Y range to the next step. Steps are logarithmic, ie 1, 2, 5, 10, 20, .. */
      private void incYRange () {
         double l = Math.log(yRange)/Math.log(10.);
	 double exp = Math.floor(l);
	 double mant = Math.pow(10., l-exp);

         /* Go up to next range */
	 if (mant<(double)1.99)
	    mant = 2;
         else if (mant<(double)4.99)
	    mant = 5;
         else if (mant<(double)9.99) {
	    mant = 1;
	    exp++;
	 } else {
	    mant = 2;
	    exp++;
         }
	 double range =  mant * Math.pow(10., exp);
         setYRange(range);
      }

      /* Decrement Y range to the next step. Steps are logarithmic, ie 100, 50, 20, 10, 5, .. */
      private void decYRange () {
         double l = Math.log(yRange)/Math.log(10.);
	 double exp = Math.floor(l);
	 double mant = Math.pow(10., l-exp);
	 	 
         /* Go down to next range */
	 if (mant>(double)5.01)
	    mant = 5;
         else if (mant>(double)2.01)
	    mant = 2;
         else if (mant>(double)1.01)
	    mant = 1;
         else {
	    mant = 5;
	    exp--;
         } 	    	       
	 double range =  mant * Math.pow(10., exp);
         setYRange(range);
      }      

      /* Increment Y offset to the next step. A step is one minor tick interval. */
      private void incYOffset () {
         setYOffset(yOffset + yTick/(yMinorTicks+1));	    	       
      }
      
      /* Decrement Y offset to the next step. A step is one minor tick interval. */
      private void decYOffset () {
	 setYOffset(yOffset - yTick/(yMinorTicks+1));	          
      }
      
      /*
       * Set Y offset to the passed value
       *
       * @param yOffset The new Y offset to set
       */
      private void setYOffset (double yOffset) {
         this.yOffset = yOffset;
         /* Rescale y axis */
	 rowheader.setOffset(yOffset);
      }

      /* Switch on clock ticks */
      private void setClocksOn () {
         clocksOn = true;
      }	 
      
      /* Switch off clock ticks */
      private void setClocksOff () {
         clocksOn = false;
      }
      
      /* Switch on grid */
      private void setGridOn () {
         gridOn = true;
      }	 
      
      /* Switch off grid */
      private void setGridOff () {
         gridOn = false;
      }
      
      /* Switch on X autoscaling */
      private void setAutoscaleXOn () {
         autoscaleXOn = true;
	 colheader.getAuto().setText("Auto");
         colheader.enablePanZoom(false);
	 colheader.repaint();
      }	 
      
      /* Switch off X autoscaling */
      private void setAutoscaleXOff () {
         autoscaleXOn = false;
	 colheader.getAuto().setText("Man");
         colheader.enablePanZoom(true);
	 colheader.repaint();
      }

      /* Switch on Y autoscaling */
      private void setAutoscaleYOn () {
         autoscaleYOn = true;
	 rowheader.getAuto().setText("Auto");
         rowheader.enableRangeOffset(false);
	 rowheader.repaint();
      }	 
      
      /* Switch off Y autoscaling */
      private void setAutoscaleYOff () {
         autoscaleYOn = false;
	 rowheader.getAuto().setText("Man");
         rowheader.enableRangeOffset(true);
	 rowheader.repaint();
      }

      /*
       * Set the X major tick interval and number of minor ticks
       *
       * @param xTick The new X major tick interval
       * @param xMinorTicks The new number of X minor ticks (between major ticks)
       */       
      private void setXTicks (double xTick, int xMinorTicks) {
         /* Store ticks locally for grid drawing */
         this.xTick = xTick;
	 this.xMinorTicks = xMinorTicks;
	 /* Rescale x axis */
	 colheader.setTicks(xTick, xMinorTicks);
      }

      /*
       * Set the Y major tick interval and number of minor ticks
       *
       * @param yTick The new Y major tick interval
       * @param yMinorTicks The new number of y minor ticks (between major ticks)
       */       
      private void setYTicks (double yTick, int yMinorTicks) {
         /* Store ticks locally for grid drawing */
         this.yTick = yTick;
	 this.yMinorTicks = yMinorTicks;
         /* Rescale y axis */
	 rowheader.setTicks(yTick, yMinorTicks);
      }

      /*
       * Draw horizontal or vertical grid on graph with different grid colours on major and minor ticks
       *
       * @param offset Lowest data value on the axis
       * @param range Range of data values on the axis
       * @param tick Major tick interval
       * @param minorTicks Number of minor ticks between major ticks
       * @param dir HORIZONTAL for X axis grid, VERTICAL for Y axis grid
       */
      private void drawGrid (double offset, double range, double tick, int minorTicks, int dir) {

         double max = offset + range;
         double minorTickSize = tick / (minorTicks+1);
	 double nextTick, nextMinorTick;
         Color saveColor = g.getColor();
	 
	 for (nextTick = ((int)(Math.floor(offset/tick)))*tick; nextTick <= max; nextTick += tick) {

	    /* Draw grid at major tick */
	    if (nextTick >= offset && nextTick <= max) {
	       g.setColor(MAJOR_GRID_COLOR);
	       drawLine(nextTick, dir);
            }	       

	    /* Draw grid at minor ticks after the major tick */
	    for (int i=0; i<minorTicks; i++) {
	       nextMinorTick = nextTick + (i+1) * minorTickSize;
	       if (nextMinorTick >=offset && nextMinorTick <= max) {
                  g.setColor(MINOR_GRID_COLOR);
	          drawLine(nextMinorTick, dir);
               }		  
	    }
         }
	 g.setColor(saveColor);      
      }
      
      /*
       * Draw a line between two data coordinates on a graph with a numeric y axis
       *
       * @param x1 First x data coordinate
       * @param y1 First y data coordinate
       * @param x2 Second x data coordinate
       * @param y2 Second y data coordinate
       */
      private void drawLine (double x1, double y1, double x2, double y2) {
         g.drawLine(valToX(x1), valToY(y1), valToX(x2), valToY(y2));
      }

      /*
       * Draw a line between two data coordinates on a graph with a boolean y axis
       *
       * @param x1 First x data coordinate
       * @param y1 First y data coordinate (true or false)
       * @param x2 Second x data coordinate
       * @param y2 Second y data coordinate (true or false)
       */
      private void drawLine (double x1, boolean y1, double x2, boolean y2) {
         g.drawLine(valToX(x1), y1?ymin+5:ymax-5, valToX(x2), y2?ymin+5:ymax-5);
      }
      
      /*
       * Draw a horizontal or vertical line intersecting the X or Y axis at the specified numeric value
       *
       * @param val The numeric data value of the axis intersept
       * @param dir HORIZONTAL to intersect the X axis, VERTICAL to intersect the Y axis
       */
      private void drawLine (double val, int dir) {
         switch (dir) {
	 case HORIZONTAL:
	    int y = valToY(val);
	    if (y>=ymin && y<=ymax)
	       g.drawLine(xmin, y, xmax, y);
	    break;
	 case VERTICAL:
	    int x = valToX(val);
	    if (x>=xmin && x<=xmax)
	       g.drawLine(x, ymin, x, ymax);
	    break;
	 default:
	    throw new SimException("SimPlotWindow.Graph.drawLine(double,int): invalid dir("+dir+")");
	 }
      }
      
      /*
       * Draw a horizontal or vertical line intersecting the X or Y axis at the specified boolean value
       *
       * @param val The boolean data value of the axis intersept
       * @param dir HORIZONTAL to intersect the X axis, VERTICAL to intersect the Y axis
       */
      private void drawLine (boolean val, int dir) {
         switch (dir) {    
	 case HORIZONTAL:
	    g.drawLine(xmin, val?ymin+5:ymax-5, xmax, val?ymin+5:ymax-5);
	    break;
	 case VERTICAL:
	    g.drawLine(val?xmax-5:xmin+5, ymin, val?xmax-5:xmin+5, ymax);
	    break;
	 default:
	    throw new SimException("SimPlotWindow.Graph.drawLine(boolean,int): invalid dir("+dir+")");
         }
      }

      /*
       * Fill the rectangle between the two passed X values, and the full Y range, with a non-data colour.
       * This is used to indicate invalid or unavailable data at that or those clock edges.
       *
       * @param x1 The low x limit of the rectangle to fill with a non-data colour
       * @param x2 The high x limit of the rectangle to fill with a non-data colour
       */
      private void plotInvalidData (double x1, double x2) {
         Color saveColor = g2.getColor();
         g2.setColor(INVALID_DATA_COLOR);
	 
         int xlow = valToX(x1);
         g.fillRect(xlow, ymin, valToX(x2)-xlow+1, ylength);
	 
	 g2.setColor(saveColor);
      }
            	 
      /*
       * Convert an X data value to an X screen coordinate value, return the result
       *
       * @param val The X data value to convert
       */
      private int valToX (double val) {
         double f = (val-xOffset)/xRange;
         return (int)((xlength-1)*f+0.5)+xmin;
      }
      	 
      /*
       * Convert a Y data value to a Y screen coordinate value, return the result
       *
       * @param val The Y data value to convert
       */
      private int valToY (double val) {
         double f = (val-yOffset)/yRange;
         return (int)((ylength-1)*(1-f)+0.5)+ymin;
      }
      
      // Convert an x screen coord to a x data value. Note there will usually be error
      // since screen coords only have int resolution.
      //
      private double xToVal (int x) {
         return xOffset+xRange*(x-xmin)/(xlength-1);
      }
            	 
      // Convert a y screen coord to a y data value. Note there will usually be error
      // since screen coords only have int resolution.
      //
      private double yToVal (int y) {
         return yRange+yOffset-yRange*(y-ymin)/(ylength-1);
      }
            	 
   }

   /* Class for x or y axis with ticks and numbering */
   private class Axis extends JPanel {
   
      /* Axis direction: HORIZONTAL or VERTICAL */
      protected int dir;
      
      /* Data type from SimTypes */
      int type;
      
      /* Major tick interval */
      protected double tick;
      
      /* Number of minor ticks between major ticks */
      private int minorTicks;
      
      /* Data range (difference between highest and lowest data values on axis) */
      protected double range;
      
      /* Data offset (lowest value on axis) */
      protected double offset;
      
      /* Variables used during painting.. */
      
      /* Graphics context */
      protected Graphics g;
      
      /* Font for axis labelling */
      protected Font font;
      
      /* Font metrics of font */
      protected FontMetrics fm;
      
      /* Font metrics ascent value */
      protected int ascent;
      
      /* X length, min and max in screen coodinates */
      protected int xlength, xmin, xmax;
      
      /* Y length, min and max in screen coodinates */
      protected int ylength, ymin, ymax;
      
      /*
       * Constructor
       *
       * @param dir Axis direction, HORIZONTAL (X axis) or VERTICAL (Y axis)
       * @param type Data type from SimTypes
       */
      protected Axis (int dir, int type) {
        super();
        font = new Font("default", Font.PLAIN, 12);
	setFont(font);
	FontMetrics fontMetrics = getFontMetrics(font);
	
        /* Set layout */
        switch (dir) {
	 case HORIZONTAL:
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	    /* Make high enough to hold axis and scale font */
	    add(Box.createVerticalStrut(5+2+fontMetrics.getHeight()));
	    break;
	 case VERTICAL:
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));	
	    /* Make room for 8 scale digits. We will use e format if can't be contained within 8 */       
	    add(Box.createHorizontalStrut(5+2+fontMetrics.stringWidth("00000000")));
	    break;
	 default:
	    throw new SimException("SimPlotWindow.Axis.Axis(int,int): invalid dir("+dir+")");
         }
	 this.dir = dir;
	 
	 /* Set some legal initial values */
	 switch (type) {
	 case INT:
	 case UINT:
	 case FLOAT:
	 case BITS:
	    range = 1;
	    offset = 0;
	    tick = 1;
	    minorTicks = 0;
	    break;
	 case LOG:
	 case CONTROL:
	 case NULL_TYPE:
	    break;
	 default:
	    throw new SimException("SimPlotWindow.Axis.Axis(int,int): invalid type");
         }
	 this.type = type;
      }

      /**
       * Paint axis
       *
       * @param g Graphics context
       */                 
      public void paintComponent (Graphics g) {
      
         super.paintComponent(g);
         this.g = g;
      
         /* Get screen coordinate limits for axis */
         Insets insets = getInsets();
	 xlength = getWidth() - insets.left - insets.right;
	 xmin = insets.left;
	 xmax = xmin + xlength - 1;
	 ylength = getHeight() - insets.top - insets.bottom;
	 ymin = insets.top;
	 ymax = ymin + ylength - 1;

         /* Get font metrics */
	 g.setFont(font);
	 fm = g.getFontMetrics();
	 ascent = fm.getAscent();
	 
	 /* Set axis drawing colour */
	 Color saveColor = g.getColor();
	 g.setColor(AXIS_COLOR);
	 
         /* Draw axis */
         drawAxis();
	 
	 /* Draw ticks and numbering/labels */
         switch (type) {
	 case INT:
	 case UINT:
	 case FLOAT:
	 case BITS:
	    double max = offset+range;
            double minorTickSize = tick / (minorTicks+1);
            double nextTick, nextMinorTick;

            /* Calculate optimum number of decimal places for major tick labels */
	    int places;
	    if (SimType.isInt(type))
	       places = 0;
	    else {
	       double l = Math.log(tick)/Math.log(10.);
	       if (l>=0)
	          places = 1;
               else		  
	          places = -(int)Math.floor(l);
            }
	    
	    /* Get value of first major tick on or below offset, move along axis drawing ticks */    	    
	    for (nextTick = ((int)(Math.floor(offset/tick)))*tick; nextTick <= max; nextTick += tick) {

	       /* Draw the major tick with value */
	       if (nextTick >= offset && nextTick <= max) {
		  drawTick(nextTick, 5);
		  drawScaleValue(nextTick, offset, max, places);  
               }	       

	       /* Draw minor ticks after the major tick */
	       for (int i=0; i<minorTicks; i++) {
		  nextMinorTick = nextTick + (i+1) * minorTickSize;
		  if (nextMinorTick >=offset && nextMinorTick <= max)
	             drawTick(nextMinorTick, 3);
	       }
            }
	    break;
	    
         /* Boolean axes have a major tick at each end and no minor ticks */	    
         case LOG:
	 case CONTROL:
	    drawTick(false, 5);
	    drawScaleValue(false, type==LOG?"false":"low");
	    drawTick(true, 5);
	    drawScaleValue(true, type==LOG?"true":"high");
	    break;

         // Null axes have "null" printed in the centre
	 case NULL_TYPE:
	    drawScaleValue("null");
	    break;
         }
	 
	 /* Restore colour */
	 g.setColor(saveColor);	    	       
      }
      
      /*
       * Return the data type for this axis
       *
       * @return The data type for this axis, from SimTypes
       */
      protected int getType () {
         return type;
      }
      
      /*
       * Set data range
       *
       * @param range The new data range
       */	 
      protected void setRange (double range) {
         if (!SimType.isNumeric(type))
	    throw new SimException("SimPlotWindow.Axis.setRange(double): cannot be called for non-NUMERIC type");
         this.range = range;
      }
      
      /*
       * Set data offset
       *
       * @param The new data offset
       */      	 
      protected void setOffset (double offset) {
         if (!SimType.isNumeric(type))
	    throw new SimException("SimPlotWindow.Axis.setOffset(double): cannot be called for non-NUMERIC type");
         this.offset = offset;
      }

      /*
       * Set major tick interval and number of minor ticks between major ticks
       *
       * @param tick The new major tick interval in data units
       * @param minorTicks The number of minor ticks between major ticks
       */
      protected void setTicks (double tick, int minorTicks) {
         if (!SimType.isNumeric(type))
	    throw new SimException("SimPlotWindow.Axis.setTicks(int): cannot be called for non-NUMERIC type");
         this.tick = tick;
	 this.minorTicks = minorTicks;
      }

      /* Draw the base axis */
      private void drawAxis () {
	 switch (dir) {
	 case HORIZONTAL:
            g.drawLine(xmin, ymax, xmax, ymax);
	    break;
	 case VERTICAL:
	    g.drawLine(xmax, ymin, xmax, ymax);
	    break;	 
         default:
	    throw new SimException("SimPlotWindow.Axis.drawAxis(): invalid dir("+dir+")");	    	 
	 }
      
      }
      
      /*
       * Draw a tick on the (numeric) axis at the specified data value, with specified size
       *
       * @param val The data value on the axis at which to draw the tick
       * @param length The desired length of the tick in screen units
       */
      private void drawTick (double val, int length) {
	 
	 switch (dir) {
	 case HORIZONTAL:
	    int x = valToX(val);
	    g.drawLine(x, ymax, x, ymax-length);
	    break;
	 case VERTICAL:
	    int y = valToY(val);
	    g.drawLine(xmax, y, xmax-length, y);
	    break;	 
         default:
	    throw new SimException("SimPlotWindow.Axis.drawTick(double,int): invalid dir("+dir+")");	    	 
	 }      
      }

      /*
       * Draw a tick on the (boolean) axis at the specified data value, with specified size
       *
       * @param val The data value on the axis at which to draw the tick
       * @param length The desired length of the tick in screen units
       */
      private void drawTick (boolean val, int length) {
	 switch (dir) {
	 case HORIZONTAL:
	    g.drawLine(val?xmax-5:xmin+5, ymax, val?xmax-5:xmin+5, ymax-length);
	    break;
	 case VERTICAL:
	    g.drawLine(xmax, val?ymin+5:ymax-5, xmax-length, val?ymin+5:ymax-5);
	    break;	 
         default:
	    throw new SimException("SimPlotWindow.Axis.drawTick(boolean,int): invalid dir("+dir+")");	    	 
	 }      
      }
      
      /*
       * Draw a scale (numeric) value next to the axis at the specified data value.
       * Use is made of the axis extrema (min and max in data units) to adjust the position if necessary
       * so that the number is displayed fully within the axis bounds and so is fully visible.
       * A hint as to the required number of decimal places is given with the rplaces parameter.
       * If the resulting text would occupy more than 8 characters, the number is displayed in e format,
       * occupying 8 characters.
       *
       * @param val The data value at which to draw the scaling number
       * @param min The axis minimum data value
       * @param max The axis maximum data value
       * @param rplaces The requested number of decimal places (0 for integer axes)
       */
      private void drawScaleValue (double val, double min, double max, int rplaces) {
      
	 /* Use default format for value if <8 chars, else e format */
         String s=null;
	 if (rplaces==0)
	    s = SimPrintf.sprintf("%d", val);
         else	    
	    s = SimPrintf.sprintf("%."+rplaces+"f", val);
	 if (s.length()>8)
	    s = SimPrintf.sprintf("%.1e", val);
	 
	 int stringWidth = fm.stringWidth(s);
	 	 
         /* Adjust position so value fits within axis, and display */
	 switch (dir) {
	 case HORIZONTAL:
            int minX = valToX(min)+1;
	    int maxX = valToX(max)-1-stringWidth;
	    g.drawString(s, Math.max(minX, Math.min(maxX, valToX(val)-stringWidth/2)), ymax-5-2);
	    break;
	    
	 case VERTICAL:
	    int minY = valToY(max)+1+ascent;
	    int maxY = valToY(min)-1; 
	    g.drawString(s, xmax-stringWidth-5-2, Math.max(minY, Math.min(maxY, valToY(val)+ascent/2)));
	    break;
         default:
	    throw new SimException("SimPlotWindow.Axis.drawScaleValue(double,double,doube,int): invalid dir("+dir+")");	    	 
	 }      
      }
      
      /*
       * Draw a scale (boolean) value next to the axis at the specified data value.
       *
       * @param val The data value at which to draw the label text
       * @param label The text to display next to the axis
       */
      private void drawScaleValue (boolean val, String label) {

	 int stringWidth = fm.stringWidth(label);

	 switch (dir) {
	 case HORIZONTAL:
	    g.drawString(label, val?(xmax-stringWidth-1-5):(xmin+1+5), ymax-5-2);
	    break;
	 case VERTICAL:
	    g.drawString(label, xmax-stringWidth-5-2, val?(ymin+1+ascent+5):(ymax-1-5));
	    break;
         default:
	    throw new SimException("SimPlotWindow.Axis.drawScaleValue(boolean,String): invalid dir("+dir+")");	    	 
	 }      
      }

      /*
       * Draw a scale value next to the axis, centred.
       * @param label The text to display next to the axis
       */
      private void drawScaleValue (String label) {

	 int stringWidth = fm.stringWidth(label);

	 switch (dir) {
	 case HORIZONTAL:
	    g.drawString(label, (xmax+xmin-stringWidth)/2, ymax-5-2);
	    break;
	 case VERTICAL:
	    g.drawString(label, xmax-stringWidth-5-2, (ymin+ymax+ascent)/2);
	    break;
         default:
	    throw new SimException("SimPlotWindow.Axis.drawScaleValue(String): invalid dir("+dir+")");	    	 
	 }      
      }

      /*
       * Convert an X data value to screen units and return the result
       *
       * @param val The data value to convert
       *
       * @return The X data value converted to screen units
       */ 
      private int valToX (double val) {
         double f = (val-offset)/range;
         return (int)((xlength-1)*f+0.5)+xmin;
      }
      	 
      /*
       * Convert a Y data value to screen units and return the result
       *
       * @param val The data value to convert
       *
       * @return The Y data value converted to screen units
       */ 
      private int valToY (double val) {
         double f = (val-offset)/range;
         return (int)((ylength-1)*(1-f)+0.5)+ymin;
      }
   }

   /* Base class for X and Y axis descriptors */
   private abstract class AxisDescriptor extends JPanel {

      /* The axis */
      protected Axis axis;

      /* The name of this axis */
      protected JLabel nameLabel;
      	 
      /* Constructor */
      protected AxisDescriptor () {
         super();
      }
            
      /*
       * Set the (numeric) data range
       *
       * @param range The new data range
       */	 
      protected void setRange (double range) {
         axis.setRange(range);
      }

      /*
       * Set the (numeric) data offset
       *
       * @param offset The new data offset
       */	 
      protected void setOffset (double offset) {
         axis.setOffset(offset);
      }
      
      /*
       * Set the major tick interval in data units and the number of minor ticks
       * between major ticks
       *
       * @param tick The major tick interval in data units
       * @param minorTicks The number of minor ticks between major ticks
       */
      protected void setTicks (double tick, int minorTicks) {
         axis.setTicks(tick, minorTicks);
      }
      
      /*
       * Return data type, from SimTypes
       *
       * @return Data type, from SimTypes
       */
      protected int getType () {
         return axis.getType();
      }	 
      
   }
    
   /* Class to hold Y axis descriptor */      
   private class YDescriptor extends AxisDescriptor {
   
      // The auto button
      private JButton auto;
      
      /* The range scaler buttons */
      Scaler rangeScaler;
      
      /* The offset scaler buttons */
      Scaler offsetScaler;
         
      /*
       * Constructor
       *
       * @param name Name for labelling the axis
       * @param type Data type, from SimTypes
       */	 
      protected YDescriptor (String name, int type) {

         super();
	       
         // Create
	 nameLabel = new JLabel();
	 // Allow name to be displayed as a tool tip
	 nameLabel.setToolTipText(name);
	 nameLabel.setIcon(new VTextIcon(nameLabel, name, VTextIcon.ROTATE_LEFT));
	 // Make end of text visible if too long to fit vertically
	 nameLabel.setVerticalAlignment(TOP);
         axis = new Axis(VERTICAL, type);
	 
	 /* For a numeric axis, create range and offset scalers */
	 Box rangeOffsetBox = null;
	 if (SimType.isNumeric(type)) {
	    rangeScaler = new Scaler("Range", VERTICAL);
	    offsetScaler = new Scaler("Offset", VERTICAL);

	    auto = new JButton("Auto");
	    auto.setActionCommand("AutoY");
	    auto.setFont(nameLabel.getFont().deriveFont((float)10));
            auto.setBorder(raiseBevelBorder);
	    auto.setAlignmentX(Component.CENTER_ALIGNMENT);
	    	    
	    rangeOffsetBox = new Box(BoxLayout.Y_AXIS);
	    rangeOffsetBox.add(Box.createVerticalStrut(1));
	    rangeOffsetBox.add(auto);
	    rangeOffsetBox.add(Box.createVerticalStrut(1));
	    rangeOffsetBox.add(rangeScaler);
	    rangeOffsetBox.add(Box.createVerticalStrut(1));
	    rangeOffsetBox.add(offsetScaler);	 
	    rangeOffsetBox.add(Box.createVerticalStrut(1));
	    rangeOffsetBox.setAlignmentX(Component.CENTER_ALIGNMENT);
         }
	 	 
	 /* Place components */             
         setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
         add(nameLabel);
	 if (SimType.isNumeric(type))
            add(rangeOffsetBox);
	 add(Box.createHorizontalGlue());
         add(axis);       
      }
      
      /*
       * Return reference to the autoscale button
       * @return Reference to the autoscale button
       */      
      protected JButton getAuto () {
         return auto;
      }
      
      /*
       * Return reference to the range scaler
       *
       * @return Reference to the range scaler
       */      
      protected Scaler getRangeScaler () {
         return rangeScaler;
      }
      
      /*
       * Return reference to the offset scaler
       *
       * @return Reference to the offset scaler
       */      
      protected Scaler getOffsetScaler () {
         return offsetScaler;
      }	
      
      /*
       * Enable or disable the range and offset scalers
       *
       * @param True to enable the range and offset scalers, false to disable
       */
      protected void enableRangeOffset (boolean state) {
         if (rangeScaler!=null)
            rangeScaler.setEnabled(state);
         if (offsetScaler!=null)	    
	    offsetScaler.setEnabled(state);
      } 	        
   }

    
   /* Class to hold Y axis descriptor */      
   private class XDescriptor extends AxisDescriptor {
   
      // The auto button
      private JButton auto;
      
      /* The pan scaler buttons */
      Scaler panScaler;
      
      /* The zoom scaler buttons */
      Scaler zoomScaler;

      /*
       * Constructor
       *
       * @param name Name for labelling the axis
       */               
      protected XDescriptor (String name) {
      
         super();
	 
	 /* Create */
	 nameLabel = new JLabel(name);
	 panScaler = new Scaler("Pan", HORIZONTAL);
	 zoomScaler = new Scaler("Zoom", HORIZONTAL);
         axis = new Axis(HORIZONTAL, INT);
	 
	 auto = new JButton("Auto");
	 auto.setActionCommand("AutoX");
	 auto.setFont(nameLabel.getFont().deriveFont((float)10));
         auto.setBorder(raiseBevelBorder);
	    	    
	 /* Place components */ 
	 setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	 add(new JLabel(name));
	 Box panZoomBox = new Box(BoxLayout.X_AXIS);
	 panZoomBox.add(Box.createHorizontalGlue());
         panZoomBox.add(auto);
	 panZoomBox.add(Box.createHorizontalStrut(10));
         panZoomBox.add(panScaler);
	 panZoomBox.add(Box.createHorizontalStrut(10));
	 panZoomBox.add(zoomScaler);
	 panZoomBox.add(Box.createHorizontalGlue());
	 add(panZoomBox);
	 add(axis);
      } 

      /*
       * Return reference to the autoscale button
       * @return Reference to the autoscale button
       */      
      protected JButton getAuto () {
         return auto;
      }
      
      /*
       * Return reference to the zoom scaler
       *
       * @return Reference to the zoom scaler
       */      
      protected Scaler getZoomScaler () {
         return zoomScaler;
      }
      
      /*
       * Return reference to the pan scaler
       *
       * @return Reference to the pan scaler
       */      
      protected Scaler getPanScaler () {
         return panScaler;
      }	 	        

      /*
       * Enable or disable the pan and zoom scalers
       *
       * @param True to enable the pan and zoom scalers, false to disable
       */
      protected void enablePanZoom (boolean state) {
         if (zoomScaler!=null)
            zoomScaler.setEnabled(state);
         if (panScaler!=null)	    
	    panScaler.setEnabled(state);
      } 	        
   }

   /* Class to hold scaler (for range, offset, pan, zoom) */
   private class Scaler extends JPanel {
   
      /* The button to scale up */
      private JButton upButton;
      
      /* The button to scale down */
      private JButton downButton;
      
      /* The name of this scaler */
      private JLabel nameLabel;
      
      /*
       * Constructor
       *
       * @param name The name of this scaler
       * @param dir The orientation direction, HORIZONTAL or VERTICAL
       */      	 
      protected Scaler (String name, int dir) {

         super();
	 
	 // Make label with a smallish font
	 nameLabel = new JLabel(name);            
	 nameLabel.setFont(nameLabel.getFont().deriveFont((float)10));
	 
	 /* Layout components */
         switch (dir) {
	 case HORIZONTAL:
	    Border hborder = BorderFactory.createEmptyBorder(0, 5, 0, 5);
	    downButton = new JButton(new ArrowIcon(WEST));
	    downButton.setBorder(hborder);
	    downButton.setAlignmentX(Component.CENTER_ALIGNMENT);
	    upButton = new JButton(new ArrowIcon(EAST));
	    upButton.setBorder(hborder);
	    upButton.setAlignmentX(Component.CENTER_ALIGNMENT);

	    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	    add(downButton);
	    add(nameLabel);
	    add(upButton);
	    break;
	 case VERTICAL:
	    Border vborder = BorderFactory.createEmptyBorder(2, 0, 2, 0);
	    downButton = new JButton(new ArrowIcon(SOUTH));
	    downButton.setBorder(vborder);
	    downButton.setAlignmentX(Component.CENTER_ALIGNMENT);
	    upButton = new JButton(new ArrowIcon(NORTH));
	    upButton.setBorder(vborder);
	    upButton.setAlignmentX(Component.CENTER_ALIGNMENT);

	    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	    add(upButton);
	    nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
	    add(nameLabel);
	    add(downButton);
	    break;
         default:
	    throw new SimException("SimPlotWindow.Scaler(String,int): invalid dir("+dir+")");	    
         }
	 
	 /* Set action commands for buttons */
	 downButton.setActionCommand(name+"Down");
	 upButton.setActionCommand(name+"Up");
         setBorder(raiseBevelBorder);
      }
      
      /*
       * Return a reference to the scale up button
       *
       * @return A reference to the scale up button
       */
      protected JButton getUpButton () {
         return upButton;
      }
      
      /*
       * Return a reference to the scale down button
       *
       * @return A reference to the scale down button
       */
      protected JButton getDownButton () {
         return downButton;
      }
      
      /*
       * Enable or disable the up and down buttons
       *
       * @param True to enable the up and down buttons, false to disable
       */
      public void setEnabled (boolean state) {
         super.setEnabled(state);
	 upButton.setEnabled(state);
	 downButton.setEnabled(state);
	 nameLabel.setEnabled(state);
      }	 
   }  
   
   /* Class to hold the arrow icons used by the Scaler class */
   private class ArrowIcon implements Icon, SwingConstants {
   
      /* Height of arrow peak, screen units */
      private int peak = 9;
      
      /* Width of arrow base, screen units */
      private int base = 18;
      
      /* Array of boundary X values */
      private int[] xPoints = new int[4];
      
      /* Array of boundary Y values */
      private int[] yPoints = new int[4];
      
      /* Arrow pointing direction: NORTH, SOUTH, EAST or WEST */
      private int dir;

      /*
       * Constructor
       *
       * @param dir Arrow pointing direction: NORTH, SOUTH, EAST or WEST 
       */
      protected ArrowIcon(int dir) {
      
         /* Build boundary coordinate list */
         switch (dir) {
	 case NORTH:
            xPoints[0] = -1;
            yPoints[0] = peak;
            xPoints[1] = base;
            yPoints[1] = peak;
            xPoints[2] = base/2;
            yPoints[2] = 0;
            xPoints[3] = base/2-1;
            yPoints[3] = 0;
	    break;
	 case SOUTH:
            xPoints[0] = -1;
            yPoints[0] = 0;
            xPoints[1] = base-1;
            yPoints[1] = 0;
            xPoints[2] = base/2-1;
            yPoints[2] = peak;
            xPoints[3] = -1;
            yPoints[3] = 0;
	    break;
	 case EAST:
            xPoints[0] = 0;
            yPoints[0] = -1;
            xPoints[1] = 0;
            yPoints[1] = base;
            xPoints[2] = peak;
            yPoints[2] = base/2;
            xPoints[3] = peak;
            yPoints[3] = base/2 - 1;
	    break;
	 case WEST:
            xPoints[0] = peak;
            yPoints[0] = -1;
            xPoints[1] = peak;
            yPoints[1] = base;
            xPoints[2] = 0;
            yPoints[2] = base/2;
            xPoints[3] = 0;
            yPoints[3] = base/2 - 1;
            break;
         default:
	    throw new SimException("SimPlotWindow.ArrowIcon(int): invalid dir("+dir+")");	    
         }
	
         this.dir = dir;
      }   

      /*
       * Return arrow height in screen units
       *
       * @return Arrow height in screen units
       */
      public int getIconHeight() {
         if (dir==NORTH || dir==SOUTH)
            return peak;
         else
            return base;	
      }

      /*
       * Return arrow width in screen units
       *
       * @return Arrow width in screen units
       */
      public int getIconWidth() {
	 if (dir==NORTH || dir==SOUTH)
            return base;
	 else
            return peak;	  
      }

      /*
       * Paint. Called automatically during screen update.
       *
       * @param c The associated Component
       * @param g Graphics context
       * @param x X offset, screen units
       * @param y Y offset, screen units
       */
      public void paintIcon(Component c, Graphics g, int x, int y) {
	 int length = xPoints.length;
	 int adjustedXPoints[] = new int[length];
	 int adjustedYPoints[] = new int[length];

	 for (int i = 0; i < length; i++) {
            adjustedXPoints[i] = xPoints[i] + x;
            adjustedYPoints[i] = yPoints[i] + y;
	 }

	 if (c.isEnabled()) {
            g.setColor(c.getForeground());
	 } else {
            g.setColor(Color.gray);
	 }

	 g.fillPolygon(adjustedXPoints, adjustedYPoints, length);
      }   
   }

}      
