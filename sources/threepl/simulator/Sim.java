/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */


package threepl.simulator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import threepl.codegen.TDE;
import threepl.codegen.TDEConstants;
import threepl.codegen.TDEList;
import threepl.codegen.TDEVar;
import threepl.exec.Var;
import threepl.exec.WordSpec;
import threepl.parser.Constant;
import threepl.ThreePL;


/**
 * Sim is the main class used in the Threepl simulator. A Sim object is
 * the first simulator related object created when the simulator is run.
 * Only one Sim object exists. The constructor creates all objects and
 * references required for use by the simulator, including all SDEs,
 * SimVariables and variable maps. Many of the command line actions are
 * performed by Sim methods, such as setting/clearing breakpoints and
 * variable displays, stepping the simulator, resetting, and printing
 * variable information. Sim also contains behaviour() and inputBehaviour()
 * methods, called after each clock edge, which call the corresponding
 * methods in the SDEs.
 * 
 * @version $Revision: 9464 $
 * @author Andrew Tulloh
 * @since October 2002
 */
@SuppressWarnings("all")
public class Sim implements TDEConstants, Constant, SimTypes {

    /** Simulator name for startup id */
    public static final String SIMULATOR_NAME = "ThreePL Simulator V1.0";

    /** Simulator time which applies at first clock edge(s), ns */
    public static final double START_TIME = 0;
        
    /** Simulator time which applies just after reset, ns */
    public static final double RESET_TIME = START_TIME - 1;
    
    /** Default clock to use for stepping, if present */
    public static final String DEFAULT_CLOCK = "glob.c100";

    // Source 3pl file name, without extension
    private String sourceName;
    
   // User interface (either gui or cli console)
    private SimUserInt userInt;

    // Set of user clock names
    private HashSet clocknames;
            
    // Set true when ready to exit simulator
    private boolean quitRequest;

    // Maps each start variable (eg C.c100.start) to its associated SimClock (eg C.c100)
    private LinkedMap<String,SimClock> startMap;
    
    // Maps each identifier name (variable or clock) to its SimIdentifier
    private LinkedMap<String,SimIdentifier>  identifierMap;
        
    // Maps each threepl source code identifier name to its SimVariable
    private LinkedMap<String,SimIdentifier> threeplMap;
    
    // Maps each non-threepl source code identifier name to its SimVariable
    private LinkedMap<String,SimIdentifier> hiddenMap;
    
    // Maps each variable name to its SimVariable
    private LinkedMap<String,SimVariable> variableMap;

    // Maps each output signal name to its SimIdentifier
    // (SimVariables and SimClocks can be in output map)
    private LinkedMap<String,SimIdentifier> outputMap;
    
    // Maps each internal input signal name to its SimVariable
    private LinkedMap<String,SimVariable> inputMap;

    // Maps each external input signal name to its SimIdentifier
    // (SimVariables and SimClocks can be in ext map)
    private LinkedMap<String,SimIdentifier> extMap; 

    // Maps each control signal name to its SimVariable
    private LinkedMap<String,SimVariable> controlMap;

    // Maps each data signal name to its SimVariable
    private LinkedMap<String,SimVariable> dataMap;

    // Maps each clock name to its SimClock
    private LinkedMap<String,SimClock> clockMap;

    // Maps each display expression name to its (SimExprSimpleNode, [formatString])
    private LinkedMap<String,ArrayList> displayMap;

    // Maps each formatted expression name to its formatString
    private LinkedMap<String,String> formatMap;

    // Maps each breakpoint expression to its SimBreakpoint
    private LinkedMap<String,SimBreakpoint> breakMap;

    // breakMap in array form for efficient runtime access within checkBreakpoints()
    private SimBreakpoint[] breakArray;
    
    // Maps each trace name to its trace details LinkedMap
    private LinkedMap<String,SimTraceDetails> traceMap;

    // Maps each plot name to its plot details LinkedMap
    private LinkedMap<String,SimPlotDetails> plotMap;

    // identifierMap in array form for efficient runtime access. The map may have multiple
    // keys per value (where different variable names point to the same SimVariable), but
    // the array has no duplicates.
    private SimIdentifier[] identifierArray;
    
    // variableMap in array form for efficient runtime access. The map may have multiple
    // keys per value (where different variable names point to the same SimVariable), but
    // the array has no duplicates.
    private SimVariable[] variableArray;

    // clockMap in array form for efficient runtime access
    private SimClock[] clockArray;

    // traceMap in array form for efficient runtime access
    private SimTraceDetails[] traceArray;
            
    // plotMap in array form for efficient runtime access
    private SimPlotDetails[] plotArray;
    
    // Array of all SDEs
    private SDE[] sdeArray;

    // Maps each unique signal name which appears on the LHS of a CONNECT
    // to an ArrayList. The ArrayList contains TDEVar pairs. The first
    // element of each pair is the CONNECT LHS TDEVar, with bit range. The
    // second element of each pair is the CONNECT RHS TDEVar, with 
    // corresponding bit range.
    private HashMap connectMap;
    
    // Current default clock for stepping
    private SimClock clockDefault;

    // Reference to the tdelist
    private TDEList tdelist;

    // Time in ns since start
    private double time;

    // Event queue (for clock edges and variable setting)
    private SimEventQueue eventQueue;

    // Index used for trace display window positioning
    private int traceDisplayIndex = 0;
             
    // Index used for plot display window positioning
    private int plotDisplayIndex = 0;

    /**
     * Constructor with TDEList
     * @param tdelist The TDEList for the associated threepl program
     */
    public Sim (String sourceName, TDEList tdelist) {
    
        this.sourceName = sourceName;
        this.tdelist = tdelist;

        // Create clocknames set
	clocknames = new HashSet();
        Iterator it = ThreePL.getClocks().iterator();
        while (it.hasNext())
           clocknames.add(((Var)it.next()).getId());

        // Create maps
	startMap = new LinkedMap<String,SimClock>();
        identifierMap = new LinkedMap<String,SimIdentifier>();
	threeplMap = new LinkedMap<String,SimIdentifier>();
	hiddenMap = new LinkedMap<String,SimIdentifier>();
        variableMap = new LinkedMap<String,SimVariable>();
	outputMap = new LinkedMap<String,SimIdentifier>();
	inputMap = new LinkedMap<String,SimVariable>();
	extMap = new LinkedMap<String,SimIdentifier>(); 
	controlMap = new LinkedMap<String,SimVariable>();
	dataMap = new LinkedMap<String,SimVariable>();
	clockMap = new LinkedMap<String,SimClock>();
	displayMap = new LinkedMap<String,ArrayList>();
	formatMap = new LinkedMap<String,String>();
	breakMap = new LinkedMap<String,SimBreakpoint>();
	traceMap = new LinkedMap<String,SimTraceDetails>();
	plotMap = new LinkedMap<String,SimPlotDetails>();
        connectMap = new HashMap();

        // Following order is important
        buildSDEs();      // Create the SDEs (and connectMap and initial variableMap)
        buildOutputs();   // Create all SDE outputs
        buildInputs();    // Create all SDE inputs
        buildVariables(); // Create all SDE variables 
        buildHidden();    // Create hiddenMap 
	buildArrays();    // Make identifierArray, variableArray from maps
	connectValues();  // Connect as yet unconnected 3pl values
	mapVarToClock();  // Find clock associated with each variable, where has one
	
        // Diagnostics
	//System.out.println("variableMap:\n"+variableMap.toString());
	//System.out.println("outputMap:\n"+outputMap.toString());
	//System.out.println("inputMap:\n"+inputMap.toString());
	//System.out.println("clockMap:\n"+clockMap.toString());
	//System.out.println("dataMap:\n"+dataMap.toString());
	//System.out.println("controlMap:\n"+controlMap.toString());
	//System.out.println("extMap:\n"+extMap.toString());
	//System.out.println("threeplMap:\n"+threeplMap.toString());
	//System.out.println("identifierMap:\n"+identifierMap.toString());
	//System.out.println("hiddenMap:\n"+hiddenMap.toString());
        //dumpConnectMap();
	
        // Create event queue
	eventQueue = new SimEventQueue(this);
	
	// Other init
	quitRequest = false;
    }

    /**
     * Add a breakpoint (overwrite if already exists)
     * @param name A logical expression on which to break when true
     * @param expr The compiled expression tree
     */
    public void addBreak (String name, SimExprSimpleNode expr) {
       breakMap.put(name, new SimBreakpoint(expr, true));
       breakArray = (SimBreakpoint[])breakMap.values().toArray(new SimBreakpoint[0]);
    }

    /**
     * Add a display (overwrite if already exists)
     * @param name An expression to display at each simulator halt
     * @param expr The compiled expression tree
     * @param fmt A printf style format for the display, or null
     */
    public void addDisplay (String name, SimExprSimpleNode expr, String fmt) {
    
       // Check for well formed fmt if supplied
       if (fmt!=null) {
          SimPrintf.check(fmt);
       }
       
       // Add to map
       ArrayList a = new ArrayList();
       a.add(expr);
       a.add(fmt);
       displayMap.put(name, a);
    }

    /**
     * Add a format (overwrite if already exists)
     * @param name An expression
     * @param fmt A printf style format to use when displaying the expression value
     */
    public void addFormat (String name, String fmt) {

       // Check for well formed fmt if supplied
       if (fmt!=null) {
          SimPrintf.check(fmt);
       }

       // Add to map
       formatMap.put(name, fmt);
    }

    /**
     * Display the expressions in a single plot window with expression graphs in a column
     * @param exprs The array of expressions to plot
     */
    public void addPlot (SimExprSimpleNode[] exprs) {
    
       // No action if no exprs. If only one expr, still no action because the first
       // expr is always a FLOAT representing time since start (x plot coordinate).
       if (exprs==null || exprs.length==1)
          return;

       // Construct a unique name (key for plotMap) for this plot. This will also be used as
       // the base filename to store the plot data.
       String name = getNextPlotName();
       
       // If this name corresponds to a plot filename which is already open, add a unique
       // suffix to the name such that a new filename will be created.
       if (plotArray!=null) {	
	  String basename = name;	
	  int suffix = 0;
	  for (boolean filenameInUse=true; filenameInUse;) {
	     filenameInUse = false;
             for (int i=0; i<plotArray.length; i++) {
        	if (plotArray[i].hasFilename(name)) {
	           filenameInUse = true;	
		   name = basename+(suffix++);
		   break;
        	}
             }
	  }	  	     
       }
       			  	
       // Create new plot
       SimPlotDetails pd = new SimPlotDetails(this, name, exprs);
       plotMap.put(name, pd);
       plotArray = (SimPlotDetails[])plotMap.values().toArray(new SimPlotDetails[0]);
       
       // Show the plot
       pd.show((plotDisplayIndex++)%10); 
       
       // Plot current values. Do this last so plot will still exist even if there are
       // runtime error(s) when evaluating current values in next().
       try {
          pd.next(time);
       } catch (SimException e) {
          throw new SimException(e.getMessage()+", plot \""+name+"\"");
       }	    
    }	

    /**
     * Display a separate plot window for each plot filename
     * @param filenames The array of plot filenames to plot
     */
    public void addPlot (String[] filenames) {
    
       if (filenames==null)
          return;

       // Create a plot window for each filename/
       for (int i=0; i<filenames.length; i++) {
              
          // Construct a unique name (key for plotMap) for this plot
	  String name = getNextPlotName();

	  // Create new plot
	  SimPlotDetails pd = new SimPlotDetails(this, name, filenames[i]);
	  plotMap.put(name, pd);
	  plotArray = (SimPlotDetails[])plotMap.values().toArray(new SimPlotDetails[0]);

	  // Show the plot
	  pd.show((plotDisplayIndex++)%10);   
       }
    }

    /**
     * Display the expressions in a single trace window, in columns
     * @param exprs is the array of expressions to trace
     */
    public void addTrace (SimExprSimpleNode[] exprs) {
    
       if (exprs==null)
          return;

       // Construct a unique name (key for traceMap) for this trace. This will also be used as
       // the base filename to store the trace data.
       String name = getNextTraceName();
       
       // If this name corresponds to a trace filename which is already open, add a unique
       // suffix to the name such that a new filename will be created.
       if (traceArray!=null) {	
	  String basename = name;	
	  int suffix = 0;
	  for (boolean filenameInUse=true; filenameInUse;) {
	     filenameInUse = false;
             for (int i=0; i<traceArray.length; i++) {
        	if (traceArray[i].hasFilename(name)) {
	           filenameInUse = true;	
		   name = basename+(suffix++);
		   break;
        	}
             }
	  }	  	     
       }

       // Create new trace
       SimTraceDetails td = new SimTraceDetails(this, name, exprs);
       traceMap.put(name, td);
       traceArray = (SimTraceDetails[])traceMap.values().toArray(new SimTraceDetails[0]);
       
       // Show the trace
       td.show((traceDisplayIndex++)%10);   

       // Trace current values. Do this last so trace will still exist even if there are
       // runtime error(s) when evaluating current values in next().
       try {
          td.next(time);
       } catch (SimException e) {
          throw new SimException(e.getMessage()+", trace \""+name+"\"");
       }	    
    }	

    /**
     * Display a separate trace window for each trace filename
     * @param filenames is the array of trace filenames to show
     */
    public void addTrace (String[] filenames) {
    
       if (filenames==null)
          return;

       // Create a trace window for each filename
       for (int i=0; i<filenames.length; i++) {
              
          // Construct a unique name (key for traceMap) for this trace
	  String name = getNextTraceName();

	  // Create new trace
	  SimTraceDetails td = new SimTraceDetails(this, name, filenames[i]);
	  traceMap.put(name, td);
	  traceArray = (SimTraceDetails[])traceMap.values().toArray(new SimTraceDetails[0]);

	  // Show the trace
	  td.show((traceDisplayIndex++)%10);   
       }
    }

    /**
     * Execute an assignment which is part of a 'set' command.
     */
    public void assign (SimExprSimpleNode lhsNode, SimExprSimpleNode rhsNode) {

       SimExprNodeValue lhs = lhsNode.nodeValue;
       SimExprNodeValue rhs = rhsNode.nodeValue;           
       SimVariable var = lhs.getVariable();
       SimValue val = lhs.getValue();
       SDE sourceSDE;

       // Check variable is a registered output of a clocked sde (these
       // are the only variables which van be set)
       if ((sourceSDE = var.getSourceSDE()) == null)
          throw new SimException("cannot assign "+var.getId()+"; it is not an output variable");       

       // Save copy of lhs value in case need to restore original value
       SimValue saveVal = val==null ? null : val.duplicate();
              
       // Change lhs variable value (this also throws an exception if lhs is
       // incompatible with rhs)
       lhs.put(rhs);
       
       // If this is not an assingment to a queue internal buffer,
       // prime associated source sde output with the new value.
       // Restore variable original value if fails.
       if (!lhsNode.isBufferView()) {
	  try {
             sourceSDE.primeOutput(var.getOIndex());
	  } catch (SimException e) {
             if (saveVal!=null)
        	val.put(saveVal);
	     throw e;
	  }	
       }  	  
              
       // Force simulator behaviour (won't happen unless forced because
       // simulator time hasn't changed)
       behaviour(true);       
    }
         
    /** Do combinatorial behaviour to get all output variable values. */
    public void behaviour (boolean force) {
	    
	// Get output variable values
        SimIdentifier si;
        Iterator it = outputMap.values().iterator();
        while(it.hasNext()) {
	    si = (SimIdentifier)it.next();
	    if (si instanceof SimVariable)
               ((SimVariable)si).behaviour(time, force);
        }	       
    }
    
    /**
     * Return an array of names from breakMap which match the wildcard
     * @return String[] of names from breakMap which match the wildcard
     */
    public String[] breakMatch (String wildcard) {
       return matchHelper(wildcard, breakMap);    
    }
    
    /**
     * Check for breakpoint expression(s) hit and for runtime errors
     * while evaluating breakpoint expressions (eg divide by zero).
     * Throws a new SimException which contains information about
     * the breakpoints hit and/or run time errors encountered.
     * Must be efficient as is executed every clock cycle.
     */
    public void checkBreakpointHit () {

       // breakArray exists only if breakpoints exist
       if (breakArray==null)
          return;

       SimException ex = null;
       SimBreakException bex = null;
       
       for (int i=0; i<breakArray.length; i++) {
          try {
	     if (breakArray[i].hasTriggered()) {
	        // Create and append to a break exception
	        if (bex==null)
		   bex = new SimBreakException();
                bex.append(i);		   
             }
	     
          } catch (SimAvailException e) {
	  
	     // Ignore queue unavailability
	     	     	
          } catch (SimException e) {
	  
	     // Create and append other runtime errors to an exception
	     if (ex==null)
	        ex = new SimException();
	     ex.append(e.getMessage()+" in breakpoint expression "+i);
	  }
       }
       
       // Point ex to whichever exception is non-null, or combine both into ex if both non-null
       if (ex!=null) {
         if (bex!=null)
	   ex.append(bex); 
       } else
          ex = bex;
       
       // Throw exception if there
       if (ex!=null)
          throw ex;
    }       
       	  		        
    /** Clear all breakpoints */
    public void clearBreak () {
       breakMap.clear();
       breakArray = null;
    }                       
    
    /** Clear changed status in all clocks and variables */
    public void clearChanged () {
       for (int i=0; i<identifierArray.length; i++)
          identifierArray[i].clearChanged();
    }
    
    /** Clear all displays */
    public void clearDisplay () {
       displayMap.clear();
    }                       
    
    /** Clear all formats */
    public void clearFormat () {
       formatMap.clear();
    }                       
    
    /** Clear all plots */
    public void clearPlot() {

       if (plotMap.size()==0)
          return;
	  
       // Hide and remove each plot
       remPlot((String[])plotMap.keySet().toArray(new String[0]));
       plotArray = null;
   }       
        
    /** Clear all traces */
    public void clearTrace() {

       if (traceMap.size()==0)
          return;
	  
       // Hide and remove each trace
       remTrace((String[])traceMap.keySet().toArray(new String[0]));
       traceArray = null;
   }       
        
    /**
     * Remove the trace or plot for the specified SimLogger. This method is called automatically
     * when the trace or plot window for the logger closes.
     * @param logger The SimLogger whose window has just closed
     */
    public void closeNotify (SimLogger logger) {
       String name = logger.getName();

       // If trace, remove from traceMap, update traceArray
       if (logger instanceof SimTraceLogger) {
	  if (traceMap.containsKey(name)) {
             traceMap.remove(name);       
             traceArray = (SimTraceDetails[])traceMap.values().toArray(new SimTraceDetails[0]);
	     if (traceArray.length==0)
		traceArray = null;
	  }
       }	  
	  
       // else if plot, remove from plotMap, update plotArray
       else if (logger instanceof SimPlotLogger) {
	  if (plotMap.containsKey(name)) {
             plotMap.remove(name);       
             plotArray = (SimPlotDetails[])plotMap.values().toArray(new SimPlotDetails[0]);
	     if (plotArray.length==0)
		plotArray = null;
	  }	  
       }
       
       else
          throw new SimException("Sim.closeNotify(SimLogger):non-handled logger type");
    }       	
                  
    /** cont: continue 3pl execution until breakpoint hit or exception */
    public void cont() {
       try {
          inputBehaviour(false);
	  clearChanged();
          while (true)
             eventQueue.execNext(clockDefault);
       } catch (SimException e) {
          halt(e);
       }	  	  
    }

    /**
     * Disable trace(s)
     * @param names The trace names or indices to disable
     */   
    public void disableTrace (String[] names) {

       // Build array of traceMap keys for all names which correspond to an entry in traceMap
       String name;
       String errstr=null;
       ArrayList nameList = new ArrayList();
       for (int i=0; i<names.length; i++) {
          if ((name = Sim.nameToKey(names[i], traceMap))==null)
             errstr = (errstr==null?"":errstr+", ")+names[i]; 
	  else
	     nameList.add(name);
       }

       // Disable each trace
       for (int i=0; i<nameList.size(); i++)          
          ((SimTraceDetails)traceMap.get((String)nameList.get(i))).disable();

       // Report any names which were not in traceMap       
       if (errstr!=null) {
          boolean one = errstr.indexOf(",")==-1;
          userInt.errorOutLn("trace"+(one?" ":"s ")+errstr+(one?" does":" do")+" not exist");
       }	  
    }

    /**
     * Return an array of expressions from displayMap which match the wildcard
     * @param wildcard A wildcard display expression
     * @return An array of matching display expressions
     */
    public String[] displayMatch (String wildcard) {
       return matchHelper(wildcard, displayMap);    
    }
    
    /**
     * Do trace and plot iterations. Throw any exceptions as one combined exception.
     */
    public void doTracePlot () {
    
	SimException exception = null;
	try {
	   trace();
        } catch (SimException ex) {
	   exception = new SimException();
	   exception.append(ex);
	}
	try {
	   plot();
        } catch (SimException ex) {
	   if (exception==null)
	      exception = new SimException();
           exception.append(ex);	      
	}
	if (exception!=null)
	   throw exception;	   	   
    }
        
    /**
     * Register enable condition for trace(s). (A trace is only logged to the logfile, and new values
     * appended to its trace window, while it is enabled.) A null expression means the trace(s) are
     * enabled immediately.
     * @param names The trace names or indices for which to register the enable condition
     * @param expr The expression on which to enable the trace(s), when true
     */
    public void enableTrace (String[] names, SimExprSimpleNode expr) {

       // Build array of traceMap keys for all names which correspond to an entry in traceMap
       String name;
       String errstr=null;
       ArrayList nameList = new ArrayList();
       for (int i=0; i<names.length; i++) {
          if ((name = Sim.nameToKey(names[i], traceMap))==null)
             errstr = (errstr==null?"":errstr+", ")+names[i]; 
	  else
	     nameList.add(name);
       }

       // Register enable condition for each trace
       for (int i=0; i<nameList.size(); i++)          
          ((SimTraceDetails)traceMap.get((String)nameList.get(i))).enable(expr);

       // Report any names which were not in traceMap       
       if (errstr!=null) {
          boolean one = errstr.indexOf(",")==-1;
          userInt.errorOutLn("trace"+(one?" ":"s ")+errstr+(one?" does":" do")+" not exist");
       }	  
    }

    /**
     * Execute a list of commands in a file
     * @param file is the command file name
     */
    public void callFile (String file) {
         
       String cmd;
       int line=0;
       try {
	  BufferedReader in = new BufferedReader(new FileReader(file));
	  while (true) {
	  
	     // Do commands until eof
	     if ((cmd = in.readLine())==null)
	        break;
	     line++;
             cmd = cmd.trim();
	     
             // Ignore blank lines and those starting with #
             if (!cmd.equals("") && !(cmd.charAt(0)=='#'))
               SimExpr.newCommand(this, cmd).eval();               		  
          }		  
       } catch (Exception e) {
          throw new SimException((line>0?file+", line "+line+": ":"")+e.getMessage());
       } catch (Error e) {
          throw new SimException((line>0?file+", line "+line+": ":"")+e.getMessage());
       }
    }   
    	 
     /**
     * Return true if the clock name exists
     * @param name The clock name
     * @return True if the clock exists
     */
    public boolean existsClock (String name) {
       return clockMap.containsKey(name) ? true : false;
    }

   /**
    * Locate the source(s) of a SimVariable. Create if necessary and enter
    * in appropriate maps.
    * @param tdeVar A TDEVar describing the variable
    * @return The corresponding SimVariable
    */
   public SimVariable findSignal(TDEVar tdeVar) {
    
      SimVariable s = null;
      String id;
      WordSpec w = tdeVar.getWordSpec();

      // If signal type is not tdeVAR it represents a constant, create
      // variable to hold it..
      if (tdeVar.getType()!=TDEVtype.VAR)
	 s = new SimVariable(SimValue.createFromTDEVar(tdeVar));

      // ..else is a named signal..
      else {
         id = tdeVar.getAliasId();
         if (variableMap.containsKey(id)) {
	 
	    s = (SimVariable) variableMap.get(id);
	    
	    // If signal name appears on LHS of a connect, locate source signal(s)
	    // recursively..
	    if (connectMap.containsKey(id)) {

               ArrayList tdeVarPairs;
               TDEVar[] tdeVarPair;
               SimVariable src;
               WordSpec wSrc, wDst;
               SimRange rangeSrc, rangeDst;
	       int widthSrc, widthDst;

               tdeVarPairs = (ArrayList) connectMap.get(id);

	       // Map the components of the src signal to s
               for (int i=0; !s.isConnected() && i<tdeVarPairs.size(); i++) {
		  tdeVarPair = (TDEVar[])tdeVarPairs.get(i);

        	  // Compile dst variable info
        	  wDst = tdeVarPair[0].getWordSpec();
		  rangeDst = wDst!=null ? new SimRange(wDst.getLower(0), wDst.getUpper(0)) :
	                        	  new SimRange(0, 0);
		  widthDst = rangeDst.getWidth();

        	  // Compile src variable info..

		  // If src TDEVar represents a non-boolean constant, force generated constant to be same
		  // width as dst variable..
		  TDEVtype type = tdeVarPair[1].getType();
        	  if (type!=TDEVtype.VAR && type!=TDEVtype.BOOL) {
	             src = new SimVariable(SimValue.createFromTDEVar(tdeVarPair[1], widthDst));
		     rangeSrc = new SimRange(0, widthDst-1);
		     widthSrc = widthDst;	       
		  }

		  // ..else if not a non-boolean constant, TDEVar defines its own width
		  else {
        	     src = findSignal(tdeVarPair[1]);
        	     wSrc = tdeVarPair[1].getWordSpec();

                     // If src wordspec is null, src range is 1 bit wide..
		     if (wSrc==null)
			rangeSrc = new SimRange(0, 0);

                     // ..else if src is a primitive type, adjust src range so it's based at zero
		     // (because src was located with findSignal()); if src is not primitive then
		     // use the src range returned by findSignal().
		     else
			rangeSrc = src.getValue().isPrimitive() ? 
		                   new SimRange(0, wSrc.getUpper(0)-wSrc.getLower(0)) :
				   new SimRange(wSrc.getLower(0), wSrc.getUpper(0));

		     widthSrc = rangeSrc.getWidth();	       
		  }

        	  // If rangeSrc and rangeDst have same width, connect them..
		  if (widthSrc==widthDst)			       
                     s.referTo(src, rangeSrc, rangeDst);

        	  // ..else if rangeSrc has width of 1, connect each bit separately
		  // (will happen with multibit .CE dst signals)..
        	  else if (widthSrc==1) {
		  
                     // If widthDst>1 and the value represented by rangeDst is primitive,
		     // then flag that all bits in s will follow state of single bit src..
		     if (s.getValue().getPart(rangeDst).isPrimitive() && widthDst>1)
		        s.getValue().setSingleBitSrc(src.getValue());

                     // ..else connect src bit to each dst bit			
		     else {
	                int low = rangeDst.getLow();
	                for (int j=0; j<widthDst; j++)
			   s.referTo(src, rangeSrc, new SimRange(low+j, low+j));	        		  
                     }			
        	  }

		  // ..else ranges are incompatible
		  else
	             throw new SimException("Sim.findSignal(TDEVar): incompatible connect ranges");		     
	       }

	       // Flag s as being connected so we don't do it again
	       s.setConnected();
            }
	 
            // If it's a partial var reference, make a new (unnamed) data SimVariable
	    // with a subset of s's SimValue, according to WordSpec w.
	    if (w!=null && w.numBits()!=s.getValue().getWidth()) {
	       SimVariable simVar = new SimVariable(SimValue.createFromWordSpec(w));
	       int high, low, start=0, end;
	       for (int i=0; i<w.numWords(); i++) {
	          low = w.getLower(i);
		  high = w.getUpper(i);
		  end = start+high-low;
	          simVar.referTo(s, new SimRange(low, high), new SimRange(start, end));
		  start = end+1;
               }
	       s = simVar;		  
            }	       
         }
	 
	 // ..else it doesn't appear in variable map, must be an external input
	 // which is not defined in the signal list (g. VCC, GND). Must be a control
	 // signal. Create a variable and enter in appropriate maps.
	 else {
	    if (w!=null)
	       throw new SimException(
	          "Sim.findSignal(TDEVar): signal \""+id+"\" does not appear in signal list"+
		  " but has a non-null WordSpec");

	    s = new SimVariable(id, SimValue.create(CONTROL));
	    controlMap.put(id, s);
	    extMap.put(id, s);
	    variableMap.put(id, s);
	    identifierMap.put(id, s);
	 }
      }
      return s;
   }

    /**
     * Return an array of names from formatMap which match the wildcard
     * @param wildcard A wildcard expression
     * @return An array of matching expressions which have existing display formats 
     */
    public String[] formatMatch (String wildcard) {
       return matchHelper(wildcard, formatMap);    
    }

    /**
     * Get reference to a SimClock by name
     * @param name is a clock name
     * @return The corresponding SimClock, or null if does not exist
     */
    public SimClock getClock(String name) {
       if (!existsClock(name))
          return null;
       return((SimClock)clockMap.get(name));
    }
    
    /**
     * Return the SimVariable input map
     * @return The SimVariable input map
     */
    public LinkedMap getInputMap () {
       return inputMap;
    }
                           
    /**
     * Return the SimVariable output map
     * @return The SimVariable output map
     */
    public LinkedMap getOutputMap () {
       return outputMap;
    }

    /**
     * Return 3pl source file name
     * @return 3pl source file name
     */
    public String getSourceName () {
       return sourceName;
    }
               
    /**
     * Return current time (since start), ns.
     * @return Time since start in ns
     */
    public double getTime () {
       return time;
    }
                                           
    /**
     * Return the user interface object
     * @return The SimUserInt user interface object
     */
    public SimUserInt getUserInt () {
       return userInt;
    }

    /**
     * Get reference to a SimVariable by name
     * @param name is a variable name
     * @return The corresponding SimVariable, or null if does not exist
     */
    public SimVariable getVariable(String name) {
       if (!variableMap.containsKey(name))
          return null;
       return((SimVariable)variableMap.get(name));
    }

    /**
     * Simulator halted: carry out appropriate action
     * @param The SimException causing the halt, or null
     */
    private void halt (SimException e) {
    
       // Report exception if any
       if (e!=null)
          userInt.errorOutLn(e.getMessage());
       
       /* Print display expressions if any */
       printDisplay();	  
    }
    
    /**
     * Print help on selected topic(s)
     * @param topics An array of help topic names (hierarchical)
     */
    public void help (String[] topics) {
       userInt.println("\n"+SimHelp.help(topics)+"\n");
    }
             
    /**
     * Return an array of names from identifierMap which match the wildcard
     * @param wildcard A wildcard identifier name
     * @return Array of Strings of matching identifier names
     */
    public String[] identifierMatch (String wildcard) {
       return matchHelper(wildcard, identifierMap);    
    }
    
    /**
     * Prime SDEs in preparation for next clock edge
     * @param force True if behaviour should happen regardless of time
     */
    public void inputBehaviour (boolean force) {
       for (int i=0; i<sdeArray.length; i++)
          sdeArray[i].inputBehaviour(time, force);
    }
                          
    /** List all breakpoints */
    public void listBreak () {

       if (breakMap.size()==0)
          return;
	  
       String[] keys = (String[])breakMap.keySet().toArray(new String[0]);

       userInt.println("Bpt-- Type- Expression----------");
       for (int i=0; i<keys.length; i++)
          userInt.println(SimPrintf.sprintf("%5d", i)+" "+
	                  ((breakMap.get(keys[i]).isEdge()) ? "edge  " : "level ")+
	                  SimPrintf.sprintf("%-20s", keys[i]));
    }

    /** Print the default clock name */
    public void listClockDefault () {
       userInt.println(clockDefault.getId());
    }

    /** List all displays */
    public void listDisplay () {

       if (displayMap.size()==0)
          return;
	  
       Set entries = displayMap.entrySet();
       Iterator it = entries.iterator();
       Map.Entry entry;
       ArrayList a;
       String key, fmt;
       
       userInt.println("Disp- Expression---------- Format----");
       for (int i=0; it.hasNext(); i++) {
          entry = (Map.Entry)it.next();
	  key = (String)entry.getKey();
	  a = (ArrayList)entry.getValue();
	  fmt = (String)a.get(1);
          userInt.println(SimPrintf.sprintf("%5d", i)+" "+
	              SimPrintf.sprintf("%-20s", key) +" "+
		      (fmt==null?"":SimPrintf.sprintf("%-10s", fmt)));
       }		      
    }

    /** List all formats */
    public void listFormat () {

       if (formatMap.size()==0)
          return;
	  
       Set entries = formatMap.entrySet();
       Iterator it = entries.iterator();
       Map.Entry entry;
       String key, fmt;

       userInt.println("Fmt-- Expression---------- Format----");
       for (int i=0; it.hasNext(); i++) {
          entry = (Map.Entry)it.next();
	  key = (String)entry.getKey();
	  fmt = (String)entry.getValue();
          userInt.println(SimPrintf.sprintf("%5d", i)+" "+
	              SimPrintf.sprintf("%-20s", key) +" "+
		      SimPrintf.sprintf("%-10s", fmt));
       }		      
   }

    /** List plot details */
    public void listPlot () {

       if (plotMap.size()==0)
          return;
	  
       String[] keys = (String[])plotMap.keySet().toArray(new String[0]);
       String exprNames;
       SimPlotDetails pd;
       
       userInt.println(
          "Plot- Name---------------- "+
	  "Expression(s)--------------------------- "+
	  "File------------------------------------");
       for (int i=0; i<keys.length; i++) {   
          pd = (SimPlotDetails)plotMap.get(keys[i]);
	  
	  // Get expression names and remove the first which represents the
	  // time value
	  exprNames = pd.getExprNames(40, " ");
	  exprNames = exprNames.substring(exprNames.indexOf(" ")+1);
	  
	  // Print the line
          userInt.println(SimPrintf.sprintf("%5d", i)+" "+
	                  SimPrintf.sprintf("%-20s", pd.getName())+" "+
			  SimPrintf.sprintf("%-40s", exprNames)+" "+
			  SimPrintf.sprintf("%-40s", pd.getFilename()));
       }			  
    }       	  	     
                    	
    /** List all trace details */
    public void listTrace () {

       if (traceMap.size()==0)
          return;
	  
       String[] keys = (String[])traceMap.keySet().toArray(new String[0]);
       SimTraceDetails td;
       SimExprSimpleNode enableExpr;
       
       userInt.println(
          "Trace Name---------------- Expression(s)----------------- "+
	  "On-------- State File----------------");
       for (int i=0; i<keys.length; i++) {   
          td = (SimTraceDetails)traceMap.get(keys[i]);

          // If not a pre-existing file, print all details, else just name and filename 
	  if (td.isNew()) {
	     enableExpr = td.getEnableExpr();
             userInt.println(
		SimPrintf.sprintf("%5d", i)+" "+
		SimPrintf.sprintf("%-20s", td.getName())+" "+
		SimPrintf.sprintf("%-30s", td.getExprNames(30, " "))+" "+
		SimPrintf.sprintf("%-10s", enableExpr!=null?enableExpr.expr():"Now")+" "+
		SimPrintf.sprintf("%-5s",  td.getEnableState()?"On":"Off")+" "+
		SimPrintf.sprintf("%-20s", td.getFilename()));
          } else {
             userInt.println(
		SimPrintf.sprintf("%5d", i)+" "+
		SimPrintf.sprintf("%-20s", td.getName())+" "+
		SimPrintf.sprintf("%-49s", " ")+
		SimPrintf.sprintf("%-20s", td.getFilename()));
	  }	  	     
       }			  
    }       	  	     
                    	
    /**
     * Make a null data variable. If the id is non-null (expect it always will
     * be), enter new variable in identifier, variable, data and threepl maps.
     * Used for creating dummy data in/out variables for null queues.
     * @param id The variable name, or null
     * @return The new Simvariable
     */
    public SimVariable makeNullData (String id) {
   
       SimVariable s;
      
       if (id != null) {
          s = new SimVariable(id, null);
	  identifierMap.put(id, s);
	  variableMap.put(id, s);
	  dataMap.put(id, s);
	  threeplMap.put(id, s);
       } else
          s = new SimVariable(null); 
       return s;	  	 
    }
   
    /** Handle plot(s) at clock step */
    public void plot () {
    
       // No action if no plots defined
       if (plotArray==null)
          return;
       
       // For each plot..
       SimException ex = null;	  
       SimPlotDetails pd;
       for (int i=0; i<plotArray.length; i++) {
          try {
             plotArray[i].next(time);
          } catch (SimException e) {
	     if (ex==null)
	        ex = new SimException();	     
	     ex.append(e.getMessage()+", plot "+i);
          }
       }
       
       // Throw exception if there were expression evaluation error(s)
       if (ex!=null)
          throw ex;
    }

    /** Print the display expressions */
    public void printDisplay () {
    
       if (displayMap.size()==0)
          return;

       Set entries = displayMap.entrySet();
       Iterator it = entries.iterator();
       Map.Entry entry;
       ArrayList a;
       String key, fmt;
       SimExprSimpleNode expr;

       // Print heading followed by details
       userInt.println("Disp- Expression---------- Value----");
       for (int i=0; it.hasNext(); i++) {
          entry = (Map.Entry)it.next();
	  key = (String)entry.getKey();
	  a = (ArrayList)entry.getValue();
	  expr = (SimExprSimpleNode)a.get(0);
	  fmt = (String)a.get(1);
	  
	  // If no fmt, look for registered format for this expression
	  if (fmt==null) {
             String s = expr.expr();
             if (existsFormat(s))
	        fmt = (String)formatMap.get(s);	  
	  }
	  
	  // Print display#, expression, value. Handle runtime expression errors.
	  String displayLine = SimPrintf.sprintf("%5d", i)+" "+
	                       SimPrintf.sprintf("%-20s", key) +" ";
          try {		      
             displayLine += expr.eval().print(fmt);
          } catch (SimException e) {
	     displayLine += e.getMessage();
          }
	  userInt.println(displayLine);	     	     
       }		      
    }
         
    /**
     * Print a table of expression values with annotation.
     * Each list in the pairs array contains a SimExprSimpleNode and a (String) format.
     * @param pairs Array list of (expression, print format) pairs
     */
   public void printExprAnnot (ArrayList[] pairs) {
   
      if (pairs==null || pairs.length==0)
         return;
	 
      SimExprSimpleNode expr;
      String fmt;
      
      // Print heading
      userInt.println("Expression---------- Value-----");
      
      // Print each expression
      for (int i=0; i<pairs.length; i++) {
	 expr = (SimExprSimpleNode)pairs[i].get(0);
	 fmt = (String)pairs[i].get(1);
	 try {
	    userInt.println(SimPrintf.sprintf("%-20s", expr.expr()) +" "+
	          expr.eval().print(fmt));
         } catch (SimAvailException e) {
	    userInt.println(SimPrintf.sprintf("%-20s", expr.expr()) +" "+
	          e.getMessage());
	 }		    
      }	 
   }
  
    /**
     * Print an expression value
     * @param expr The compiled expression tree
     * @param fmt A printf style format to use, or null
     */
    public void printExprValue (SimExprSimpleNode expr, String fmt) {

       // If no format supplied, use the one registered for this expression, if it exists   
       if (fmt==null) {
          String s = expr.expr();
          if (existsFormat(s))
	     fmt = (String)formatMap.get(s);
       }
       
       // Print
       try {
          userInt.println(expr.eval().print(fmt));
       } catch (SimAvailException e) {
          userInt.println(e.getMessage());
       }	  
    }

    /** Prepare to exit simulator. Carry out any pre-exit tasks here. */
    public void quitRequest () {
       quitRequest = true;

       // Shutdown any active traces/plots 
       clearTrace();
       clearPlot();
       
       // Close user interface
       userInt.close();
    }                    
       
    /**
     * Remove breakpoint by expression or number
     * @param names Array of breakpoint expressions and/or existing breakpoint indices to remove
     */
    public void remBreak (String[] names) {
       String errstr;
       if ((errstr = Sim.remKeys(breakMap, names)) != null) {
          boolean one = errstr.indexOf(",")==-1;
          userInt.errorOutLn("breakpoint"+(one?" ":"s ")+errstr+(one?" does":" do")+" not exist");
       }
       breakArray = (SimBreakpoint[])breakMap.values().toArray(new SimBreakpoint[0]);
    }

    /**
     * Remove displays by expression or number
     * @param names Array of display expressions and/or existing display indices to remove
     */
    public void remDisplay (String[] names) {
       String errstr;
       if ((errstr = Sim.remKeys(displayMap, names)) != null) {
          boolean one = errstr.indexOf(",")==-1;
          userInt.errorOutLn("display"+(one?" ":"s ")+errstr+(one?" does":" do")+" not exist");
       }
    }

    /**
     * Remove format by expression or number
     * @param names Array of expressions and/or existing format indices for which to remove formats
     */
    public void remFormat (String[] names) {
       String errstr;
       if ((errstr = Sim.remKeys(formatMap, names)) != null) {
          boolean one = errstr.indexOf(",")==-1;
          userInt.errorOutLn("format"+(one?" ":"s ")+errstr+(one?" does":" do")+" not exist");
       }
    }

    /**
     * Remove (close window and delete) plot by plot index number or name.
     * Plot is removed from plotMap as SimPlotDetails.hide() calls closeNotify() indirectly.
     * @param names The array of plot index numbers (as Strings), or plot names, to remove
     */
    public void remPlot (String[] names) {

       // Build array of plotMap keys for all names which correspond to an entry in plotMap
       String name;
       String errstr=null;
       ArrayList nameList = new ArrayList();
       for (int i=0; i<names.length; i++) {
          if ((name = Sim.nameToKey(names[i], plotMap))==null)
             errstr = (errstr==null?"":errstr+", ")+names[i]; 
	  else
	     nameList.add(name);
       }

       // Remove each plot
       for (int i=0; i<nameList.size(); i++)          
          ((SimPlotDetails)plotMap.get((String)nameList.get(i))).close();

       // Report any names which were not in plotMap      
       if (errstr!=null) {
          boolean one = errstr.indexOf(",")==-1;
          userInt.errorOutLn("plot"+(one?" ":"s ")+errstr+(one?" does":" do")+" not exist");
       }	  
    }
                  
    /**
     * Remove (close window and delete) trace by trace index number or name.
     * Trace is removed from traceMap as SimTraceDetails.hide() calls closeNotify() indirectly.
     * @param names The array of trace index numbers (as Strings), or trace names, to remove
     */
    public void remTrace (String[] names) {

       // Build array of traceMap keys for all names which correspond to an entry in traceMap
       String name;
       String errstr=null;
       ArrayList nameList = new ArrayList();
       for (int i=0; i<names.length; i++) {
          if ((name = Sim.nameToKey(names[i], traceMap))==null)
             errstr = (errstr==null?"":errstr+", ")+names[i]; 
	  else
	     nameList.add(name);
       }

       // Remove each trace
       for (int i=0; i<nameList.size(); i++)          
          ((SimTraceDetails)traceMap.get((String)nameList.get(i))).close();

       // Report any names which were not in traceMap       
       if (errstr!=null) {
          boolean one = errstr.indexOf(",")==-1;
          userInt.errorOutLn("trace"+(one?" ":"s ")+errstr+(one?" does":" do")+" not exist");
       }	  
    }

    /** Reset */
    public void reset () {

        // Reset all SimVariables, clocks, SDEs, breakpoints.
	// SDE's times will be RESET_TIME after reset.
	for (int i=0; i<variableArray.length; i++)
	   variableArray[i].reset();
	for (int i=0; i<clockArray.length; i++)
	   clockArray[i].reset();
        for (int i=0; i<sdeArray.length; i++)
	   sdeArray[i].reset();	    
        if (breakArray!=null)
           for (int i=0; i<breakArray.length; i++)
	      breakArray[i].reset();	   

        // Set time=RESET_TIME and force behaviour. Also force input behaviour in preparation
	// for first clock edge.
        time = RESET_TIME;
	behaviour(true);
	inputBehaviour(true);

        // Do trace, plot as required. Throw exception if error(s).
        doTracePlot();
	
        // Clear event queue
	eventQueue.clear();

        // Schedule clock start signals to rise and fall on 1st and 2nd active edges respectively
	// of their associated clocks
	String startName;
	SimVariable startVar;
	SimClock clock;
	Set startSet = startMap.keySet();
	Iterator it = startSet.iterator();
	while (it.hasNext()) {
	   startName = (String) it.next();
	   startVar = (SimVariable) variableMap.get(startName);
	   clock = (SimClock) startMap.get(startName);
	   eventQueue.add(new SimEvent(START_TIME, startVar, true));
	   eventQueue.add(new SimEvent(START_TIME+clock.getPeriod(), startVar, false));
	}
	
	// Schedule all clocks to start ticking at t==START_TIME
	for (int i=0; i<clockArray.length; i++)
	   eventQueue.add(new SimEvent(START_TIME, clockArray[i]));	
   }    

    /** Run: begin 3pl program execution, continue until breakpoint hit or exception */
    public void run() {
        cont();
    }

    /**
     * Set breakpoint(s) type to edge or level
     */
    public void setBreak (String[] names, boolean isEdge) {

       String name;
       String errstr=null;
       ArrayList textNames = new ArrayList();
       for (int i=0; i<names.length; i++) {
          if ((name = Sim.nameToKey(names[i], breakMap))==null)
             errstr = (errstr==null?"":errstr+", ")+names[i]; 
	  else
	     textNames.add(name);
       }
       
       // Set the validly named breakpoints to the new type
       for (int i=0; i<textNames.size(); i++)
          (breakMap.get((String)textNames.get(i))).setType(isEdge);
             
       // Report breakpoints with invalid names
       if (errstr!=null) {
          boolean one = errstr.indexOf(",")==-1;
          userInt.errorOutLn("breakpoint"+(one?" ":"s ")+errstr+(one?" does":" do")+" not exist");
       }       
    }
    
    /**
     * setClockDefault: set the default clock by name
     * @param id Name of clock to set as the default for stepping
     */
    public void setClockDefault(String id) {
       if (existsClock(id))          
          setClockDefault((SimClock)clockMap.get(id));
    }

    /**
     * setClockDefault: set the default clock
     * @param clock The SimClock to set as the default for stepping
     */
    public void setClockDefault(SimClock clock) {
       clockDefault = clock;
    }

   /**
    * Set current time (time since start, in ns.
    * @param time Time since start in ns
    */
   public void setTime (double time) {
      this.time = time;
   }
             
   /**
    * Print the type of an array of expressions, filtered by qualifiers. The
    * expressions are identifiers or var expressions (array or struct expression).
    * @param exprs The expression node array to show types for
    * @param qualifiers Predefined Strings which filter the exprs, eg "-threepl"
    */      
   public void showExp (SimExprSimpleNode[] exprs, String[] qualifiers) {

      TreeSet showSet;  

      // If exprs null or empty, use qualifiers to find set of identifier names to show..
      if (exprs==null || exprs.length==0) {
         showSet = filterIdentifierNames(null, qualifiers);        
         Iterator it = showSet.iterator();
	 String name;
         while (it.hasNext()) {
	    name = (String)it.next();
	    userInt.println(SimPrintf.sprintf("%-20s", name) +" "+
	       SimPrintf.sprintf("%-20s", ((SimIdentifier)identifierMap.get(name)).printType()));
         }	       
      }
      
      // ..else extract the identifier names from the exprs (which could be var expressions)
      // and filter these with qualifiers. Use these to select which exprs to show.
      else {
         String[] identNames = new String[exprs.length];
         for (int i=0; i<exprs.length; i++)
	    identNames[i] = exprs[i].nodeValue.getIdentifier().getId();
         showSet = filterIdentifierNames(identNames, qualifiers); 	    
         for (int i=0; i<exprs.length; i++) {
	    SimExprSimpleNode expr = exprs[i];
	    if (showSet.contains(identNames[i])) {
	       // If buffer view, eval node to get correct value type
	       SimExprNodeValue nv = expr.isBufferView() ? expr.eval() : expr.nodeValue;
	       userInt.println(SimPrintf.sprintf("%-20s", exprs[i].expr()) +" "+     
                  SimPrintf.sprintf("%-20s", nv.printType()));
            }		  
         }	    
      }		  
   }		  
      
    /**
     * Simulator entry point. First method called after constructor.
     * If the argument is "gui", the simulator graphical interface is opened,
     * else the console interface is opened.
     * @param mode "gui" to open the graphical interface, else the console interface is used
     */
    public void start (String mode) {

        // Create user interface
	if (mode!=null && mode.compareToIgnoreCase("gui")==0)
	   userInt = new SimGui(this);
        else
	   userInt = new SimCli(this);	   
	
        // Set default clock. Use DEFAULT_CLOCK if it exists, else take first
	// clock in list
	if (existsClock(DEFAULT_CLOCK))
           setClockDefault(DEFAULT_CLOCK);
        else {
	   Iterator it;
	   it = clockMap.keySet().iterator();
	   if (it.hasNext())
	      setClockDefault((String)it.next());
        }
	
        // Reset
        reset();

        // Execute init file if present in the current directory.
	// Name is sourceName.sim (ie. name of 3pl program plus .sim extension).
	String initFile = "./"+sourceName+".sim";
	if (SimFile.exists(initFile)) {
	   try {
	      callFile(initFile);
           } catch (Exception e) {
	      userInt.println(e.getMessage());
           } catch (Error f) {
	      userInt.println(f.getMessage());
	   }	   
        }	   	      
	
        // If the user interface is cli, enter command interpreter
	if (userInt.isCli())
           while (!quitRequest)
              ((SimCli)userInt).doCommand();
    }

    /**
     * step: execute a number of cycles of the default clock
     * @param n The number of clock cycles to execute
     */
    public void step(int n) {
       step(n, clockDefault);
    }

    /**
     * step: execute a number of cycles of a selected clock
     * @param n The number of clock cycles to execute
     * @param clock The clock to use
     */
    public void step(int n, SimClock clock) {
       try {
          inputBehaviour(false);
	  clearChanged();
          for (; n>0; n--)
             eventQueue.execNext(clock);
          halt(null);
       } catch (SimException e) {
          halt(e);
       }
    }
    
    /** Handle trace(s) at clock step */
    public void trace () {
    
       // No action if no traces defined
       if (traceArray==null)
          return;
       
       // For each trace..
       SimException ex = null;	  
       for (int i=0; i<traceArray.length; i++) {
          try {
             traceArray[i].next(time);
          } catch (SimException e) {
	     if (ex==null)
	        ex = new SimException();	     
	     ex.append(e.getMessage()+", trace "+i);
          }
       }
       
       // Throw exception if there were expression evaluation error(s)
       if (ex!=null)
          throw ex;
    }

    // Create an entry in connectMap which describes a connection between a source
    // and dest TDEVar. The source TDEVar is referenced by the srcIndex'th entry
    // in the inputs list of the passed TDE. The dest TDEVar is referenced by the
    // dstIndex'th entry in the  TDE's outputs list.
    // @param tde TDE referencing the src and dst TDEVars
    // @param srcIndex Index in the TDE inputs list of the src TDEVar
    // @param dstIndex Index in the TDE outputs list of the dst TDEVar
    //
    private void addConnectPair (TDE tde, int srcIndex, int dstIndex) {
                
       ArrayList tdeVarPairs;
       TDEVar tdeVarSrc = (TDEVar) tde.getInput(srcIndex);
       TDEVar tdeVarDst = (TDEVar) tde.getOutput(dstIndex);
       TDEVar[] tdeVarPair = new TDEVar[2];
       String id = tdeVarDst.getAliasId();
       tdeVarPair[0] = tdeVarDst;
       tdeVarPair[1] = tdeVarSrc;

       if((tdeVarPairs = (ArrayList) connectMap.get(id)) == null) {
	   tdeVarPairs = new ArrayList();
	   connectMap.put(id, tdeVarPairs);
       }
       tdeVarPairs.add(tdeVarPair);
    }
    
    // Build some arrays from corresponding maps (eliminates duplicate values and
    // improves runtime efficiency).
    //
    private void buildArrays () {
       identifierArray = (SimIdentifier[])
          (new HashSet(identifierMap.values())).toArray(new SimIdentifier[0]);
       variableArray = (SimVariable[])
          (new HashSet(variableMap.values())).toArray(new SimVariable[0]);
       clockArray = (SimClock[])
          (new HashSet(clockMap.values())).toArray(new SimClock[0]);
    }

    // Build hiddenMap. It contains mappings to all identifiers which are not threepl program identifiers.
    //
    private void buildHidden() {
       hiddenMap.putAll(identifierMap);
       (hiddenMap.keySet()).removeAll(threeplMap.keySet());
    }

    // Build all SDE input signals
    //
    private void buildInputs() {
       for (int i=0; i<sdeArray.length; i++)
          sdeArray[i].buildInputs();
    }

    // Build all SDE output signals
    //
    private void buildOutputs() {
       for (int i=0; i<sdeArray.length; i++)
          sdeArray[i].buildOutputs();
    }

    // Create the clocks, SDEs and connectMap and initial variableMap
    // Note: it is assumed tdeSIGs appear at the top of the tdelist so that all variables
    // are defined, and referenced in variableMap, before any other TDE types are encountered
    // in tdelist.
    //
    private void buildSDEs() {
        TDE tde;
        SDE sde;
        String id;
        TDEVar tdeVar;
        ArrayList sdeList = new ArrayList();
        Iterator it = tdelist.iterator();
        ClkType clockmode;
	
        while(it.hasNext()) {
            tde = (TDE) it.next();

            // Ignore inactive TDEs
            if(!tde.isActive())
                continue;

            switch(tde.getType()) {
	    
	    // tdeSIGs do not have a corresponding SDE. However we use the tdeSIGs to
            // generate the initial variableMap and identifierMap.
	    // These maps then contain all threepl variables and intermediate variables (ie expression
	    // results like E23), and control variables. After the call to buildInputs() some further
	    // external inputs (VCC, GND) may be added.
            case SIG:
	        tdeVar = (TDEVar) tde.getInput(0);		
		id = tdeVar.getAliasId();	

                // Handle clock related signals..
		if ((clockmode = tdeVar.getClockMode())!=ClkType.NOT) {
		
		   Var var = tdeVar.getClkVar();
		   
		   // If negative edge clock, refer to the associated var
		   // to find the clock name
		   if (clockmode==ClkType.NEG)
		      id = var.getId();

                   // Get clock name without prefixes to look up in clocknames set
		   int lastdot = id.lastIndexOf(".");
                   String clockname =  lastdot<0 ? id : id.substring(lastdot+1, id.length());		   

                   // If the clock name represents a user clock, create it.
		   // (The user clocks are defined in the HashSet ThreePL.clocks.) 
                   SimClock clock;
		   if (clocknames.contains(clockname)) {
		      		      
                      // Create the clock and enter in maps..
							 
				// Check for valid clock period							 
				double period;
				if ((period = var.getClkPeriod(null))==0.0)
			      throw new SimException("Sim.buildSDEs(): zero clock period specified");		     

		      clock = new SimClock(id, period, var.getClkFreq(null),
		                           var.getClkDutyCycle(null), clockmode!=ClkType.NEG); 
        	      clockMap.put(id, clock);
        	      identifierMap.put(id, clock);
        	      threeplMap.put(id, clock);
                   }		
		   continue;
		}
		   
                // Otherwise it's a variable..
				
		WordSpec w = tdeVar.getWordSpec();
		SimVariable s;

		// If WordSpec w!=null, create data or control variable (normally data,
		// but .CE signals are multibit control signals)
		if (w!=null) {

		   Var var = w.getVar();
		   
		   // If var is associated with a memory, its var.wordspec will be null so handle separately..
		   if (var!=null && ((var.getMode()==Mode.CMEMORY) || (var.getMode()==Mode.RMEMORY))) {
		      s = new SimVariable(id, SimValue.createFromType(w.getType()));
		      dataMap.put(id, s);
		      threeplMap.put(id, s);
		   }
		   
		   // ..else if w.var!=null and w.var.wordspec==w, it's a 3pl data variable,
		   // use var.Type to build..
		   else if (var!=null && var.getWordSpec().equals(w)) {
		      s = new SimVariable(id, SimValue.createFromType(var.getType()));
		      dataMap.put(id, s);
		      threeplMap.put(id, s);
		   }
		   
		   // ..else either w.var==null, or w.var!=null and the var wordspec is different
		   // to w. It's an expression result or a derived variable.
		   // like .CE, .PUSH etc., use w to build.
		   else {
		      s = new SimVariable(id, SimValue.createFromWordSpec(w));
		      if (w.getPrimType(0)==Ptype.NONE)
			 controlMap.put(id, s);
		      else
			 dataMap.put(id, s);
                   }		      
                }
		
		// ..else WordSpec w==null, create single bit control signal		   
		else {
		   s = new SimVariable(id, SimValue.create(CONTROL));
		   controlMap.put(id, s);
		}
		
		// Put in variable and identifier maps
                variableMap.put(id, s);
                identifierMap.put(id, s);
                break;

            // tdeCONNECTS with a parameter are really casts. They generate
            // a corresponding SDE.
	    // Ordinary tdeCONNECTs (no parameter) do not have a
            // corresponding SDE and we use tdeCONNECTs to generate
            // connectMap which maps every signal appearing on the LHS of a
            // connect to its constituent signals.      
            //
            case CONNECT:
               if (tde.getParams().size() != 0) {
                   sde = SDE.create(this, tde);
                   sde.registerWithTDE(tde);
                   sdeList.add(sde);
               } else
	           addConnectPair(tde, 0, 0);
               break;

            // tdeIBUFs do not have a corresponding SDE. Just enter its output signal
	    // name in inputMap and extMap
	    case IBUF:
	       mapExtInput(tde);
	       break;
	       
            // tdeOBUFs do not have a corresponding SDE. Just enter its input signal(s)
	    // name in outputMap and extMap
	    case OBUF:
	       mapExtOutput(tde);
	       break;

            // tdeSTARTs do not have acorresponding SDE. Instead we enter in a map the name of
	    // each start signal (eg "C.c100.start") with a reference to the corresponding
	    // SimClock. At simulator start this map is used to pulse each start signal for one
	    // period of its associated clock.
            case START:
	       startMap.put(tde.getOutput(0).getAliasId(), clockMap.get(tde.getInput(2).getAliasId()));
	       break;

            // tdeDIVERGEs have a corresponding SDE, unless the number of destination
	    // modules is 1. In this case, no SDE is required: the src POP input is
	    // directly connected to the dst NE output, and dst POP input is connected
	    // directly to src NE. To achieve this we put entries in connectMap for
	    // these signals.
	    // (This case is an optimisation and could be safely removed.)
	    case DIVERGE:
	       
	       // If number of dst modules == 1, connect signals appropriately..
	       if (tde.getInputs().size()==4) {
	          addConnectPair(tde, 3, 0);
	          addConnectPair(tde, 2, 1);
	       }
	       
	       // ..else number of dst modules > 1, create SDE
	       else {
                  sde = SDE.create(this, tde);
                  sde.registerWithTDE(tde);
                  sdeList.add(sde);
	       }
	       break;
	       	    	       	       	       
            // These TDEs ignored
	    case SIMVAR:
	    case ELEMENT:
	       break;
	       
            // Other TDEs have a corresponding SDE
            default:
	    
	        // If returned sde is non-null, register it else just ignore
                if ((sde = SDE.create(this, tde)) != null) {
                   sde.registerWithTDE(tde);
                   sdeList.add(sde);
		}
                break;
            }
        }
			
	// Convert sdeList to array sdeArray for more efficient runtime access
	sdeArray = (SDE[])sdeList.toArray(new SDE[0]);
    }

    // Build SDE internal variables as required
    //
    private void buildVariables() {
       for (int i=0; i<sdeArray.length; i++)
          sdeArray[i].buildVariables();
    }

    // Connect as yet unconnected 3pl values. These are not SDE inputs/outputs but
    // may appear in the connect list.
    //
    private void connectValues () {
    
       SimVariable s;
       String id;
       ArrayList tdeVarPairs;
       TDEVar tdeVar;
       WordSpec w;
       Var v;

       for (int i=0; i<variableArray.length; i++) {
          s = variableArray[i];
	  if ((id = s.getId())!=null && connectMap.containsKey(id) && !s.isConnected()) {
             tdeVarPairs = (ArrayList) connectMap.get(id);
	     tdeVar = ((TDEVar[])tdeVarPairs.get(0))[0];
	     if ((w = tdeVar.getWordSpec())!=null && (v = w.getVar())!=null && v.getMode()==Mode.VALUE)
	        findSignal(tdeVar);
          }
       }	  	     
    }
    
    // Print the contents of connectMap (diagnostic)
    //
    private void dumpConnectMap() {
        TreeSet       set         = new TreeSet(connectMap.keySet());
        Iterator      it          = set.iterator();
        Iterator      it2;
        String        lhs;
        ArrayList     tdeVarPairs;
        TDEVar[] tdeVarPair;
        WordSpec      wSrc;
        WordSpec      wDst;
        int           startBit;
        int           stopBit;
        StringBuffer  b;

        System.out.println("*****connectMap*****");

        while(it.hasNext()) {
            lhs = (String) it.next();


            // Print lhs signal name
            System.out.println(lhs);
            tdeVarPairs = (ArrayList) connectMap.get(lhs);
            it2         = tdeVarPairs.iterator();

            // Print component parts..
            while(it2.hasNext()) {
                b = new StringBuffer();
                b.append("   "); // indent components
                tdeVarPair = (TDEVar[]) it2.next();
                wDst       = tdeVarPair[0].getWordSpec();
                wSrc       = tdeVarPair[1].getWordSpec();

                // Generate lhs bit range for this component
                if(wDst != null) {
                    b.append("[");
                    startBit = wDst.getLower(0);
                    stopBit  = wDst.getUpper(0);

                    if(startBit == stopBit)
                        b.append(startBit);
                    else
                        b.append(startBit + ".." + stopBit);

                    b.append("]");
                }


                // Generate rhs signal name for this component
                b.append(" = " + tdeVarPair[1].getAliasId());

                // Generate rhs bit range for this component
                if(wSrc != null) {
                    b.append("[");
                    startBit = wSrc.getLower(0);
                    stopBit  = wSrc.getUpper(0);

                    if(startBit == stopBit)
                        b.append(startBit);
                    else
                        b.append(startBit + ".." + stopBit);

                    b.append("]");
                }


                // Print this component's details
                System.out.println(b);
            }
        }

        System.out.println("");
    }

    // Return true if the breakpoint exists
    // @param name A logical expression
    // @return True if the expression is an existing breakpoint expression
    //
    private boolean existsBreak (String name) {
       return breakMap.containsKey(name) ? true : false;
    }

    // Return true if the expression has an existing format
    // @param name An expression
    // @return True if a format exists for displaying the expression value
    //
    private boolean existsFormat (String name) {
       return formatMap.containsKey(name) ? true : false;
    }

   // Return a set of identifier names, filtered according to the filters embedded
   // in the name array, if any. If names is null, return all identifier names.
   // @param names Array of identifier names and keyword filter names
   // @return The set of identifer names which were not filtered out
   //
   private TreeSet filterIdentifierNames (String[] names, String[] qualifiers) {

      TreeSet showSet = new TreeSet();
      String name;
      boolean filter_threepl = false;
      boolean filter_hidden = false;
      boolean filter_variables = false;
      boolean filter_outputs = false;
      boolean filter_inputs = false;
      boolean filter_ext = false;
      boolean filter_data = false;
      boolean filter_control = false;
      boolean filter_clocks = false;
      
      // Concatenate names and qualifiers into one array
      if (names==null)
         names = qualifiers;
      else if (qualifiers!=null) {
         String[] s = new String[names.length+qualifiers.length];
	 System.arraycopy(names, 0, s, 0, names.length);
	 System.arraycopy(qualifiers, 0, s, names.length, qualifiers.length);
	 names = s;
      }	 
         	 
      // Add each specified identifier name to the list to show.
      // If a filter keyword, flag its presence and filter set below.
      if (names!=null) for (int i=0; i<names.length; i++) {
         name = names[i];
	 if (name.equals("-threepl"))
	    filter_threepl = true;
	 else if (name.equals("-hidden"))
	    filter_hidden = true;
	 else if (name.equals("-variables"))
	    filter_variables = true;
	 else if (name.equals("-outputs"))
	    filter_outputs = true;
	 else if (name.equals("-inputs"))
	    filter_inputs = true;
	 else if (name.equals("-ext"))
	    filter_ext = true;
	 else if (name.equals("-control"))
	    filter_control = true;
	 else if (name.equals("-data"))
	    filter_data = true;
	 else if (name.equals("-clocks"))
	    filter_clocks = true;
         else 
	    showSet.add(name);	    
      }

      // If no specific identifiers specified, start with the full set
      if (showSet.size()==0)
         showSet.addAll(identifierMap.keySet());

      // Apply filters
      if (filter_threepl)
         showSet.retainAll(threeplMap.keySet());	 
      if (filter_hidden)
         showSet.retainAll(hiddenMap.keySet());	 
      if (filter_variables)
         showSet.retainAll(variableMap.keySet());	 
      if (filter_outputs)
         showSet.retainAll(outputMap.keySet());	 
      if (filter_inputs)
         showSet.retainAll(inputMap.keySet());	 
      if (filter_ext)
         showSet.retainAll(extMap.keySet());	 
      if (filter_control)
         showSet.retainAll(controlMap.keySet());	 
      if (filter_data)
         showSet.retainAll(dataMap.keySet());	 
      if (filter_clocks)
         showSet.retainAll(clockMap.keySet());	 

      return showSet;   
   }
      
    // Return the next (unique) plot name for use as key in plotMap
    // @return A name suitable for use as a new plotMap key
    //
    private String getNextPlotName () {
       String baseName = "plot";
       int suffix = 0;
       String name = baseName+suffix;
       SimPlotDetails pd;
       
       // Get unique name
       while ((pd = (SimPlotDetails)plotMap.get(name)) != null)
          name = baseName+(++suffix);

       return name;	      
    }
    
    // Return the next (unique) trace name for use as key in traceMap
    // @return A name suitable for use as a new traceMap key
    //
    private String getNextTraceName () {
       String baseName = "trace";
       int suffix = 0;
       String name = baseName+suffix;
       SimTraceDetails td;
       
       // Get unique name
       while ((td = (SimTraceDetails)traceMap.get(name)) != null)
          name = baseName+(++suffix);

       return name;	      
    }

    // Enter the variable name corresponding to an external input (where tde is
    // type tdeIBUF or tdeIOBUF) into inputMap and extMap.
    // @param tde The TDE which must be type tdeIBUF or IOBUF
    //
    private void mapExtInput (TDE tde) {
       TDEVar tdeVar = (TDEVar) tde.getOutput(0);
       String id = tdeVar.getAliasId();
       SimVariable s = (SimVariable) variableMap.get(id);
       inputMap.put(id, s);
       extMap.put(id, s);
    }
        
    // Enter the variable names corresponding to an external output and its enable
    // signal if it exists (where tde is type TDEType.OBUF or TDEType.IOBUF) into outputMap and extMap.
    // @param tde The TDE which must be type TDEType.OBUF or IOBUF
    //
    private void mapExtOutput (TDE tde) {
    
       // We use SimIdentifier rather than SimVariable for the mapped object because
       // a clock can be mapped to an external output.
       ArrayList inputs = tde.getInputs();
       TDEVar tdeVar;
       String id;
       SimIdentifier si;
       for (int i=0; i<inputs.size(); i++) {
	  tdeVar = (TDEVar) tde.getInput(i);
	  id = tdeVar.getAliasId();
	  si = (SimIdentifier) identifierMap.get(id);
	  outputMap.put(id, si);
	  extMap.put(id, si);
       }		  
    }

    // Where relevant, find the clock associated with each variable and store with the variable.
    // 
    private void mapVarToClock () {
    
       SDE sde;
       SimVariable s;
       ArrayList sinkSDEList, iIndexList;
       SimClock clock;
       int iIndex;
           
       for (int i=0; i<variableArray.length; i++) {
          s = variableArray[i];
	  
	  // If this variable is sourced by an SDE which has a clock,
	  // that clock is this variable's clock
	  if ((sde = s.getSourceSDE()) != null) {
	     if ((clock = sde.getOutputClock(s.getOIndex())) != null) {
                s.setClock(clock);
	        continue;
             }		
          }
	  	     
	  // Otherwise search the sink SDEs, if any, of this variable; this variable's
	  // associated clock is taken from the first SDE found with an input clock
	  // (all sink SDEs for a variable should have the same clock, or no clock)..
	  sinkSDEList = s.getSinkSDEList();
	  iIndexList = s.getIIndexList();
	  for (int j=0; j<sinkSDEList.size(); j++) {
	     sde = (SDE)sinkSDEList.get(j);
	     iIndex = ((Integer)iIndexList.get(j)).intValue();
	     if ((clock = sde.getInputClock(iIndex)) != null) {
	        s.setClock(clock);
		break;
	     }
	  }
	  	  
	  // (Otherwise this variable has no associated clock)
       }	     
    }
    
    // Return an array of names from the map which match the wildcard
    // @param wildcard A wildcard map key
    // @param map The LinkedMap to search
    // @return An array of matching keys
    //
    private static String[] matchHelper (String wildcard, LinkedMap map) {
       if (wildcard==null)
          return null;

       // Convert the wildcard expression to a regular expression.
       // - replace . with \.
       // - replace * with .*
       // - replace ? with .
       // in that order.
       wildcard = wildcard.replaceAll("\\.", "\\\\.").replaceAll("\\*", ".*").replaceAll("\\?", ".");    

       // Create array of matching names
       Iterator it = map.keySet().iterator();
       String name;
       ArrayList list = new ArrayList();
       while (it.hasNext()) {
          name = (String)it.next();
          if (name.matches(wildcard))
	     list.add(name);
       }
       return (String[])list.toArray(new String[0]);
    }
             
    // Return a key to the passed map, or null. A key is returned if the passed name is an
    // existing key to the map, or if name is an integer index to a key in the map (indexed
    // with the map keys sorted alphabetically).
    // @param name A potential key to the map, or an integer index to a key in the map
    // @param map The LinkedMap to search
    // @return The (String) key if it exists, or null
    //
    private static String nameToKey (String name, LinkedMap map) {
       try {
	  int n = Integer.parseInt(name);
	  if (n<0 || n>=map.size())
	     return null;	     
	  return (String)((map.keySet().toArray())[n]);
       }
       catch (NumberFormatException e) {	  
	  if (!map.containsKey(name))
             return null;
	  return name;	     
       }
    }

    // Remove an array of names (potential keys), or integer indices to keys, from a LinkedMap.
    // Returns a String containing names which could not be removed, comma separated,
    // else returns null if all names were successfully removed.
    // @param map The LinkedMap from which to remove entries
    // @param names An array of potential keys to the map, or integer indices to keys in the map
    // @return A String containing a comma-separated list of keys which did not exist, or null
    //
    private static String remKeys (LinkedMap map, String[]names) {
       String name;
       String errstr=null;
       ArrayList textNames = new ArrayList();
       for (int i=0; i<names.length; i++) {
          if ((name = Sim.nameToKey(names[i], map))==null)
             errstr = (errstr==null?"":errstr+", ")+names[i]; 
	  else
	     textNames.add(name);
       }
       
       // Remove the names from the map
       for (int i=0; i<textNames.size(); i++)
          map.remove((String)textNames.get(i));
             
       // Return comma separated list of names/indices which could not be removed    
       return errstr;
    }
}
