/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */


package threepl.simulator;



/**
 * This class returns help text for threepl simulator command topics.
 * 
 * @version $Revision: 8254 $
 * @author Andrew Tulloh
 */
@SuppressWarnings("all")
public class SimHelp {
    /**
     * Unique identifier string for this class and build used to help
     * debugging and binary tracking
     */
    private String _class_cvs_id = "$id$";

   /**
    * Entry point for retrieval of help topic text.
    * Return help text for the hierarchically ordered topics specified.
    *
    * @param topics An array of hierarchically ordered help topics
    *
    * @return Help text for the specified topics
    */
   public static String help (String[] topics) {
   
      if (topics==null || topics.length==0) {
         return helpAll();
	  
      } else if (topics[0].equals("break")) {   
         if (topics.length>2)
	    helpError(topics);
         else if (topics.length==1)
	    return helpBreak();
	 else if (topics[1].equals("add"))
	    return helpBreakAdd();
	 else if (topics[1].equals("level"))
	    return helpBreakLevel();
	 else if (topics[1].equals("edge"))
	    return helpBreakEdge();
	 else if (topics[1].equals("rem"))
	    return helpBreakRem();
	 else if (topics[1].equals("list"))
	    return helpBreakList();
	 else if (topics[1].equals("clear"))
	    return helpBreakClear();
         else
	    helpError(topics);	    
	 
      } else if (topics[0].equals("call")) {
         if (topics.length>1)
	    helpError(topics);
         return helpCall();	    
      
      } else if (topics[0].equals("clock")) {
         if (topics.length>1)
	    helpError(topics);
         return helpClock();	    
      
      } else if (topics[0].equals("cont")) {
         if (topics.length>1)
	    helpError(topics);
         return helpCont();	    
      
      } else if (topics[0].equals("display")) {
         if (topics.length>2)
	    helpError(topics);
         else if (topics.length==1)
	    return helpDisplay();
	 else if (topics[1].equals("add"))
	    return helpDisplayAdd();
	 else if (topics[1].equals("rem"))
	    return helpDisplayRem();
	 else if (topics[1].equals("list"))
	    return helpDisplayList();
	 else if (topics[1].equals("clear"))
	    return helpDisplayClear();
         else
	    helpError(topics);
	    	    
      } else if (topics[0].equals("format")) {
         if (topics.length>2)
	    helpError(topics);
         else if (topics.length==1)
	    return helpFormat();
	 else if (topics[1].equals("add"))
	    return helpFormatAdd();
	 else if (topics[1].equals("rem"))
	    return helpFormatRem();
	 else if (topics[1].equals("list"))
	    return helpFormatList();
	 else if (topics[1].equals("clear"))
	    return helpFormatClear();
         else
	    helpError(topics);	    
	    
      } else if (topics[0].equals("help")) {
         if (topics.length>1)
	    helpError(topics);
         return helpHelp();
	 
      } else if (topics[0].equals("plot")) {
         if (topics.length>2)
	    helpError(topics);
         else if (topics.length==1)
	    return helpPlot();
	 else if (topics[1].equals("add"))
	    return helpPlotAdd();
	 else if (topics[1].equals("rem"))
	    return helpPlotRem();
	 else if (topics[1].equals("list"))
	    return helpPlotList();
	 else if (topics[1].equals("clear"))
	    return helpPlotClear();
         else
	    helpError(topics);	    

      } else if (topics[0].equals("print") ||
                 topics[0].equals("pannot")) {
         if (topics.length>1)
	    helpError(topics);
         return helpPrint();
	 
      } else if (topics[0].equals("exit") ||
                 topics[0].equals("quit")) {
         if (topics.length>1)
	    helpError(topics);
         return helpQuit();
	    
      } else if (topics[0].equals("reset")) {
         if (topics.length>1)
	    helpError(topics);
         return helpReset();
	 
      } else if (topics[0].equals("run")) {
         if (topics.length>1)
	    helpError(topics);
         return helpRun();
	 
      } else if (topics[0].equals("set")) {
         if (topics.length>1)
	    helpError(topics);
         return helpSet();
	 
      } else if (topics[0].equals("show")) {
         if (topics.length>1)
	    helpError(topics);
         return helpShow();
	 
      } else if (topics[0].equals("step")) {
         if (topics.length>1)
	    helpError(topics);
         return helpStep();	 

      } else if (topics[0].equals("trace")) {
         if (topics.length>2)
	    helpError(topics);
         else if (topics.length==1)
	    return helpTrace();
	 else if (topics[1].equals("add"))
	    return helpTraceAdd();
	 else if (topics[1].equals("rem"))
	    return helpTraceRem();
	 else if (topics[1].equals("list"))
	    return helpTraceList();
	 else if (topics[1].equals("clear"))
	    return helpTraceClear();
	 else if (topics[1].equals("on"))
	    return helpTraceOn();
	 else if (topics[1].equals("off"))
	    return helpTraceOff();
         else
	    helpError(topics);	    

      } else if (topics[0].equals("expressions")) {
         if (topics.length>1)
	    helpError(topics);
         return helpExpressions();	 

      } else if (topics[0].equals("constants")) {
         if (topics.length>1)
	    helpError(topics);
         return helpConstants();	 

      } else if (topics[0].equals("files")) {
         if (topics.length>1)
	    helpError(topics);
         return helpFiles();	 

      } else if (topics[0].equals("initialisers")) {
         if (topics.length>1)
	    helpError(topics);
         return helpInitialisers();	 

      } else if (topics[0].equals("operators")) {
         if (topics.length>1)
	    helpError(topics);
         return helpOperators();	 

      } else if (topics[0].equals("functions")) {
         if (topics.length>1)
	    helpError(topics);
         return helpFunctions();	 

      } else if (topics[0].equals("wildcards")) {
         if (topics.length>1)
	    helpError(topics);
         return helpWildcards();	 

      } else if (topics[0].equals("formats")) {
         if (topics.length>1)
	    helpError(topics);
         return helpFormats();	 

      } else if (topics[0].equals("filters")) {
         if (topics.length>1)
	    helpError(topics);
         return helpFilters();	 

      } else if (topics[0].equals("arrays")) {
         if (topics.length>1)
	    helpError(topics);
         return helpArrays();	 

      } else if (topics[0].equals("ranges")) {
         if (topics.length>1)
	    helpError(topics);
         return helpRanges();	 

      } else if (topics[0].equals("timing")) {
         if (topics.length>1)
	    helpError(topics);
         return helpTiming();	 

      } else
         helpError(topics);
	 
      return "";	 
   }
   
   /**
    * Return help text for all main topics
    *
    * @return Help text for all main topics
    */
   private static String helpAll () {
      return
      "Simulator commands:						\n"+
      "									\n"+
      "break	:add, change, remove, list, clear breakpoint expressions\n"+
      "call	:execute a list of simulator commands in a file		\n"+
      "cont	:continue execution until breakpoint hit		\n"+
      "clock	:set or display the default clock for stepping		\n"+
      "display	:add, remove, list, clear expressions to display at halt\n"+
      "exit	:exit simulator						\n"+
      "format	:add, remove, list, clear default expression formats	\n"+
      "help	:print this help message				\n"+
      "plot	:add, remove, list clear expression plots		\n"+
      "print	:print values of expressions				\n"+
      "pannot	:print values of expressions in annotated table		\n"+
      "quit	:exit simulator						\n"+
      "reset	:reset threepl program					\n"+
      "run	:continue execution until breakpoint hit		\n"+
      "set	:assign a value to a threepl variable or clock		\n"+
      "show	:print threepl variable or clock details		\n"+
      "step	:step threepl program					\n"+
      "trace	:add, remove, list, clear, enable, disable expression traces\n"+
      "									\n"+
      "Other topics:							\n"+
      "									\n"+
      "expressions							\n"+
      "constants							\n"+
      "files (ie. command files, incl. startup command file)		\n"+
      "initialisers							\n"+    
      "operators							\n"+
      "functions							\n"+
      "wildcards							\n"+
      "formats								\n"+
      "filters								\n"+
      "arrays								\n"+ 
      "ranges								\n"+    
      "timing								\n"+    
      "									\n"+
      "All commands and sub-commands may be abbreviated.		\n"+     
      "Type \"help command\" for more detail.				";
   }

   /**
    * Return help text for the break command
    *
    * @return Help text for the break command
    */
   private static String helpBreak () {
      return
      "break - add, change, remove, list and clear breakpoint expressions\n"+
      "									\n"+            
      "SYNOPSIS								\n"+
      "   break add expr [ expr ... ]					\n"+
      "   break edge expr|index [ expr|index ...]			\n"+
      "   break level expr|index [ expr|index ...]			\n"+
      "   break rem expr|index [ expr|index ... ]			\n"+
      "   break [list]							\n"+
      "   break clear							\n"+
      "									\n"+            
      "DESCRIPTION							\n"+	 
      "   Adds, changes, removes, lists, clears breakpoints. A breakpoint\n"+
      "   is specified with a boolean expression. It can be of 'level' or\n"+
      "   'edge' type and is edge type by default when created (with add).\n"+
      "   A level breakpoint hit occurs when the expression is true,	\n"+
      "   meaning the hit can occur on successive clock edges. An edge	\n"+
      "   breakpoint hit occurs when the expression becomes true after	\n"+
      "   being false, meaning the hit cannot occur on successive clock	\n"+
      "   edges. The command break with no operand is equivalent to	\n"+
      "   break list.							\n"+
      "									\n"+                  
      "OPERANDS								\n"+
      "   add    Indicates following expressions are to be added as	\n"+
      "          (edge type) breakpoints.				\n"+
      "   edge   Indicates following existing breakpoint expressions or	\n"+
      "          breakpoint indices are to be changed to edge type.	\n"+
      "   level  Indicates following existing breakpoint expressions or	\n"+
      "          breakpoint indices are to be changed to level type.	\n"+
      "   rem    Indicates following existing breakpoint expressions or	\n"+
      "          breakpoint indices are to be removed.			\n"+
      "   list   Lists all breakpoint expressions.			\n"+
      "   clear  Clears (removes) all breakpoints.			\n"+
      "									\n"+                        		
      "Type \"help break operand\" for more detail.			";
   }

   /**
    * Return help text for the break add command
    *
    * @return Help text for the break add command
    */
   private static String helpBreakAdd () {
      return
      "break add - add one or more breakpoint expressions		\n"+
      "									\n"+
      "SYNOPSIS								\n"+
      "   break add expr [ expr ... ]					\n"+
      "									\n"+            
      "DESCRIPTION							\n"+	 
      "   Adds one or more breakpoint expressions, which are separated by\n"+
      "   white space. A breakpoint expression has a boolean result, and\n"+
      "   a breakpoint hit occurs when the expression becomes true. All	\n"+
      "   breakpoint expressions are evaluated after every clock edge.	\n"+
      "									\n"+                  
      "OPERANDS								\n"+
      "   expr  A breakpoint expression to add. Must have a boolean result.\n"+
      "									\n"+                  
      "EXAMPLES								\n"+
      "   break add glob.a      (where glob.a is a logical threepl variable)\n"+
      "   break add glob.b==10  (where glob.b is a numeric threepl variable)\n"+
      "   break add count(C.c100)=30 (where C.c100 is a threepl clock)	\n"+
      "   break add 'glob.d*.PUSH'   (where glob.d* are threepl queues)	\n"+
      "   b a !>glob.a&&glob.b>=glob.d*(glob.e+4) glob.f==20 C.c100>1000";
   }

   /**
    * Return help text for the break level command
    *
    * @return Help text for the break level command
    */
   private static String helpBreakLevel () {
      return
      "break level - change type of one or more breakpoint expressions	\n"+
      "              to level type					\n"+
      "									\n"+
      "SYNOPSIS								\n"+
      "   break level expr|index [ expr|index ... ]			\n"+
      "									\n"+            
      "DESCRIPTION							\n"+
      "   Changes one or more existing breakpoints to type level. Each	\n"+
      "   breakpoint expression is specified by the expression itself	\n"+
      "   or its integer index in the breakpoint list (viewed with the	\n"+
      "   break list command). A hit on a level type breakpoint occurs	\n"+
      "   whenever the expression is true, and may occur on successive	\n"+
      "   clock edges.							\n"+
      "									\n"+                  
      "OPERANDS								\n"+
      "   expr   An existing breakpoint expression to change.		\n"+
      "   index  The index in the breakpoint list of a breakpoint to	\n"+
      "          change.						\n"+
      "									\n"+                  
      "EXAMPLES								\n"+
      "   break level glob.a						\n"+
      "   break level C.c100=30 4 6 <glob.d				";
   }

   /**
    * Return help text for the break edge command
    *
    * @return Help text for the break edge command
    */
   private static String helpBreakEdge () {
      return
      "break edge - change type of one or more breakpoint expressions	\n"+
      "              to edge type					\n"+
      "									\n"+
      "SYNOPSIS								\n"+
      "   break edge expr|index [ expr|index ... ]			\n"+
      "									\n"+            
      "DESCRIPTION							\n"+
      "   Changes one or more existing breakpoints to type edge. Each	\n"+
      "   breakpoint expression is specified by the expression itself	\n"+
      "   or its integer index in the breakpoint list (viewed with the	\n"+
      "   break list command). A hit on an edge type breakpoint occurs	\n"+
      "   whenever the expression becomes true after being false, and 	\n"+
      "   cannot occur on successive clock edges.			\n"+
      "									\n"+                  
      "OPERANDS								\n"+
      "   expr   An existing breakpoint expression to change.		\n"+
      "   index  The index in the breakpoint list of a breakpoint to	\n"+
      "          change.						\n"+
      "									\n"+                  
      "EXAMPLES								\n"+
      "   break edge glob.a						\n"+
      "   break edge C.c100=30 4 6 <glob.d				";
   }

   /**
    * Return help text for the break rem command
    *
    * @return Help text for the break rem command
    */
   private static String helpBreakRem () {
      return
      "break rem - remove one or more breakpoint expressions		\n"+
      "									\n"+
      "SYNOPSIS								\n"+
      "   break rem expr|index [ expr|index ... ]			\n"+
      "									\n"+            
      "DESCRIPTION							\n"+	 
      "   Removes one or more breakpoint expressions, each of which is	\n"+
      "   specified by the expression to remove or its integer index in the\n"+
      "   breakpoint list (viewed with the break list command).		\n"+
      "									\n"+                  
      "OPERANDS								\n"+
      "   expr   An existing breakpoint expression to remove.		\n"+
      "   index  The index in the breakpoint list of a breakpoint to	\n"+
      "          remove.						\n"+
      "									\n"+                  
      "EXAMPLES								\n"+
      "   break rem glob.a						\n"+
      "   break rem glob.b==10						\n"+
      "   break rem C.c100=30 4 6 <glob.d				";
   }

   /**
    * Return help text for the break list command
    *
    * @return Help text for the break list command
    */
   private static String helpBreakList () {
      return
      "break list - list all current breakpoint expressions		\n"+
      "									\n"+
      "SYNOPSIS								\n"+
      "   break [list]							\n"+
      "									\n"+            
      "DESCRIPTION							\n"+
      "   Lists all breakpoints, if any exist. Each line of the listing	\n"+
      "   contains the breakpoint index and its expression. Note that	\n"+
      "   indices may change as breakpoints are added and removed because\n"+
      "   the list is always ordered lexicographically by breakpoint	\n"+
      "   expression.							";                 
   }

   /**
    * Return help text for the break clear command
    *
    * @return Help text for the break clear command
    */
   private static String helpBreakClear () {
      return
      "break clear - clear all breakpoints				\n"+
      "									\n"+
      "SYNOPSIS								\n"+
      "   break clear							\n"+
      "									\n"+            
      "DESCRIPTION							\n"+
      "   Removes all breakpoints, if any exist. 			";
   }

   /**
    * Return help text for the clock command
    *
    * @return Help text for the clock command
    */
   private static String helpClock () {
      return
      "clock - print or set the default threepl clock for program stepping\n"+
      "									\n"+
      "SYNOPSIS								\n"+
      "   clock								\n"+
      "   clock	clockname						\n"+
      "									\n"+            
      "DESCRIPTION							\n"+
      "   With no parameter, the clock command prints the current default\n"+
      "   clock for threepl program stepping (refer step command).	\n"+
      "   Alternatively the current default clock may be set to a new	\n"+
      "   clock if a single valid threepl clock name parameter is supplied.\n"+
      "									\n"+                  
      "OPERANDS								\n"+
      "   clockname  A valid threepl clock name.			\n"+
      "									\n"+                  
      "EXAMPLES								\n"+
      "   clock C.c50							";
   }

   /**
    * Return help text for the call command
    *
    * @return Help text for the call command
    */
   private static String helpCall () {
      return
      "call - execute a list of commands in a file			\n"+
      "									\n"+
      "SYNOPSIS								\n"+
      "   call \"file\"							\n"+
      "									\n"+            
      "DESCRIPTION							\n"+
      "   Executes the list of simulator commands in file \"file\".	\n"+
      "   There must be one command per line in the file. Blank lines,	\n"+
      "   or lines beginning with the character '#' are ignored.	\n"+
      "   The call command may be used within a file (to call another	\n"+
      "   file), but a file must not call itself, even indirectly.	\n"+
      "   Note that if the file \"name.sim\" is present in the current	\n"+
      "   directory, where name is the name of the 3pl program under	\n"+
      "   test, then name.sim will be called automatically at simulator	\n"+
      "   startup.							\n"+
      "									\n"+
      "   If an error occurs during a command executed from a command	\n"+
      "   file, an error message is displayed and the simulator falls	\n"+
      "   back to the command prompt. All commands in the file prior to	\n"+
      "   the error will have taken effect.				"; 	      
   }

   /**
    * Return help text for the cont command
    *
    * @return Help text for the cont command
    */
   private static String helpCont () {
      return
      "cont - continue threepl program execution			\n"+
      "									\n"+
      "SYNOPSIS								\n"+
      "   cont								\n"+
      "									\n"+            
      "DESCRIPTION							\n"+
      "   Continues threepl program execution until interrupted by a	\n"+
      "   breakpoint hit.				                ";
   }

   /**
    * Return help text for the display command
    *
    * @return Help text for the display command
    */
   private static String helpDisplay () {
      return
      "display - add, remove, list and clear display expressions.	\n"+
      "									\n"+            
      "SYNOPSIS								\n"+
      "   display add [format] expr [ [format] expr ... ]		\n"+
      "   display rem expr|index [ expr|index ... ]			\n"+
      "   display [list]						\n"+
      "   display clear							\n"+
      "									\n"+            
      "DESCRIPTION							\n"+	 
      "   Adds, removes, lists, clears display expressions. A display	\n"+
      "   expression has its value printed at each simulator halt, in a	\n"+
      "   formatted table with other display expressions. A simulator	\n"+
      "   halt occurs after a step command or when a breakpoint hit occurs.\n"+
      "   The command display with no operand is equivalent to display	\n"+
      "   list.								\n"+
      "									\n"+                  
      "OPERANDS								\n"+
      "   add    Indicates following display expressions are to be added.\n"+
      "          Print format is the default for each expression type,	\n"+
      "          unless a format spec precedes the expression.		\n"+
      "   rem    Indicates following display expressions or display	\n"+
      "          indices are to be removed.				\n"+
      "   list   Lists all display expressions.				\n"+
      "   clear  Clears (removes) all display expressions.		\n"+
      "									\n"+                        		
      "Type \"help display operand\" for more detail.			";
   }

   /**
    * Return help text for the display add command
    *
    * @return Help text for the display add command
    */
   private static String helpDisplayAdd () {
      return
      "display add - add one or more display expressions		\n"+
      "									\n"+
      "SYNOPSIS								\n"+
      "   display add [format] expr [ [format] expr ... ]		\n"+
      "									\n"+            
      "DESCRIPTION							\n"+	 
      "   Adds one or more display expressions with optional formats, all\n"+
      "   of which are separated by white space. The expressions are	\n"+
      "   printed in a formatted table at each simulator halt. A format,\n"+
      "   where specified, overrides an existing default format for the	\n"+
      "   following expression.						\n"+
      "									\n"+                  
      "OPERANDS								\n"+
      "   expr    Any expression except an assignment (assignment 	\n"+
      "           expressions may only appear in the set command).	\n"+
      "   format  A printf style format string, surrounded by double	\n"+
      "           quotes. Only one conversion specification may appear,	\n"+
      "           ie. \"%5d\" is legal but \"%x %x\" is not. The format	\n"+
      "           is applied only to the expression immediately following.\n"+
      "           In the case of a format preceding a wildcard, the	\n"+
      "           format is applied only to the first threepl identifier\n"+
      "           (ie. variable or clock) matching the wildcard.	\n"+
      "									\n"+                  
      "EXAMPLES								\n"+
      "   display add glob.a						\n"+
      "   display add glob.b glob.d==50 ln(glob.e)+20 changed(glob.f)	\n"+
      "   display add \"%20d\" glob.a '*.D' glob.b&glob.c		\n"+
      "   display add \"%-20.4d\" glob.a+glob.b \"%0b\" glob.c		";
   }

   /**
    * Return help text for the display rem command
    *
    * @return Help text for the display rem command
    */
   private static String helpDisplayRem () {
      return
      "display rem - remove one or more display expressions		\n"+
      "									\n"+
      "SYNOPSIS								\n"+
      "   display rem expr|index [ expr|index ... ]			\n"+
      "									\n"+            
      "DESCRIPTION							\n"+	 
      "   Removes one or more display expressions, each of which is	\n"+
      "   specified by the expression to remove or its integer index in the\n"+
      "   display list (viewed with the display list command).		\n"+
      "									\n"+                  
      "OPERANDS								\n"+
      "   expr   An existing display expression to remove.		\n"+
      "   index  The index in the display list of a display to	\n"+
      "          remove.						\n"+
      "									\n"+                  
      "EXAMPLES								\n"+
      "   display rem glob.a						\n"+
      "   display rem glob.b==10					\n"+
      "   display rem C.c100=30 4 6 <glob.d				";
   }

   /**
    * Return help text for the display list command
    *
    * @return Help text for the display list command
    */
   private static String helpDisplayList () {
      return
      "display list - list all current display expressions		\n"+
      "									\n"+
      "SYNOPSIS								\n"+
      "   display [list]						\n"+
      "									\n"+            
      "DESCRIPTION							\n"+
      "   Lists all display expressions, if any exist. Each line of the	\n"+
      "   listing contains the display index and its expression. Note that\n"+
      "   indices may change as display expressions are added and removed\n"+
      "   because the list is always ordered lexicographically by display\n"+
      "   expression.							";                 
   }

   /**
    * Return help text for the display clear command
    *
    * @return Help text for the display clear command
    */
   private static String helpDisplayClear () {
      return
      "display clear - clear all display expressions			\n"+
      "									\n"+
      "SYNOPSIS								\n"+
      "   display clear							\n"+
      "									\n"+            
      "DESCRIPTION							\n"+
      "   Removes all display expressions, if any exist. 		";
   }

   /**
    * Return help text for the format command
    *
    * @return Help text for the format command
    */
   private static String helpFormat () {
      return
      "format - add, remove, list and clear default expression formats.	\n"+
      "									\n"+            
      "SYNOPSIS								\n"+
      "   format add fmt expr [ fmt expr ... ]				\n"+
      "   format rem expr|index [ expr|index ... ]			\n"+
      "   format [list]							\n"+
      "   format clear							\n"+
      "									\n"+            
      "DESCRIPTION							\n"+	 
      "   Adds, removes, lists, clears default expression formats. A	\n"+
      "   default expression format defines how that expression's value	\n"+
      "   will be printed unless overridden with a format specification	\n"+
      "   in a print or display command. If no default format exists for\n"+
      "   an expression, the expression is printed in a default format	\n"+
      "   based on its result type. The command format with no operand	\n"+
      "   is equivalent to format list.					\n"+
      "									\n"+                  
      "OPERANDS								\n"+
      "   add    Indicates following fmt,expression pairs are to be added.\n"+
      "          Each fmt becomes the default format for its corresponding\n"+
      "          expression when that expression appears in a print or	\n"+
      "          display command.					\n"+
      "   rem    Indicates following expressions or their indices are to\n"+
      "          be removed from the default expression	format list.	\n"+
      "   list   Lists all default expression formats.			\n"+
      "   clear  Clears (removes) all default expression formats.	\n"+
      "									\n"+                        		
      "Type \"help format operand\" for more detail.			";
   }

   /**
    * Return help text for the format add command
    *
    * @return Help text for the format add command
    */
   private static String helpFormatAdd () {
      return
      "format add - add one or more default expression formats		\n"+
      "									\n"+
      "SYNOPSIS								\n"+
      "   format add fmt expr [ fmt expr ... ]				\n"+
      "									\n"+            
      "DESCRIPTION							\n"+	 
      "   Adds one or more default expression formats. The desired format\n"+
      "   precedes each expression. All parameters are separated by white\n"+
      "   space. Wildcards expressions are not allowed in this context.	\n"+
      "									\n"+                  
      "OPERANDS								\n"+
      "   fmt     A printf style format string, surrounded by double	\n"+
      "           quotes. Only one conversion specification may appear,	\n"+
      "           ie. \"%5d\" is legal but \"%x %x\" is not.		\n"+
      "   expr    Any expression except an assignment (assignment 	\n"+
      "           expressions may only appear in the set command).	\n"+
      "           Wildcards are not allowed in this context.		\n"+
      "									\n"+                  
      "EXAMPLES								\n"+
      "   format add \"%20d\" glob.a \"%o\" glob.a[10]			\n"+
      "   format add \"%-20.4d\" glob.b+glob.c \"%0b\" glob.d		";
   }

   /**
    * Return help text for the format rem command
    *
    * @return Help text for the format rem command
    */
   private static String helpFormatRem () {
      return
      "format rem - remove one or more default expression formats	\n"+
      "									\n"+
      "SYNOPSIS								\n"+
      "   format rem expr|index [ expr|index ... ]			\n"+
      "									\n"+            
      "DESCRIPTION							\n"+	 
      "   Removes one or more default expression formats, each of which	\n"+
      "   is specified by the expression to remove or its integer index	\n"+
      "   in the format list (viewed with the format list command).	\n"+
      "									\n"+                  
      "OPERANDS								\n"+
      "   expr   An existing expression to remove from the default	\n"+
      "          expression format list.				\n"+
      "   index  The index in the format list of a default expression	\n"+
      "          format to remove.					\n"+
      "									\n"+                  
      "EXAMPLES								\n"+
      "   format rem glob.a						\n"+
      "   format rem glob.b==10						\n"+
      "   format rem C.c100==30 4 6 >glob.d				";
   }

   /**
    * Return help text for the format list command
    *
    * @return Help text for the format list command
    */
   private static String helpFormatList () {
      return
      "format list - list all default expressions formats		\n"+
      "									\n"+
      "SYNOPSIS								\n"+
      "   format [list]							\n"+
      "									\n"+            
      "DESCRIPTION							\n"+
      "   Lists all default expression formats, if any exist. Each line	\n"+
      "   of the listing contains the format index and its expression.	\n"+
      "   Note that indices may change as default expression formats are\n"+
      "   added and removed because the list is always ordered		\n"+
      "   lexicographically by expression.				";
   }

   /**
    * Return help text for the format clear command
    *
    * @return Help text for the format clear command
    */
   private static String helpFormatClear () {
      return
      "format clear - clear all default expression formats		\n"+
      "									\n"+
      "SYNOPSIS								\n"+
      "   format clear							\n"+
      "									\n"+            
      "DESCRIPTION							\n"+
      "   Removes all default expression formats, if any exist. 	";
   }

   /**
    * Return help text for the help command
    *
    * @return Help text for the help command
    */
   private static String helpHelp () {
      return
      "help - print this help message";
   }

   /**
    * Return help text for the plot command
    *
    * @return Help text for the plot command
    */
   private static String helpPlot () {
      return
      "plot - add, remove, list and clear expression plots.		\n"+
      "									\n"+            
      "SYNOPSIS								\n"+
      "   plot add expr|\"name\" [ expr|\"name\".. ]			\n"+
      "   plot rem index|\"name\" [index|\"name\"..]			\n"+
      "   plot [list]							\n"+
      "   plot clear							\n"+
      "									\n"+            
      "DESCRIPTION							\n"+	 
      "   Adds, removes, lists, clears expression plots. An expression	\n"+
      "   plot contains a time series of one or more expression values.	\n"+
      "   These  are stored in a log file and are displayed in a plot	\n"+
      "   window. The expressions that form the plot are specified in the\n"+
      "   plot add command. A plot is closed with the plot rem command,	\n"+
      "   or by closing the plot window with a mouse click, which has the\n"+
      "   same effect. The currently open plots can be listed with plot	\n"+
      "   list, and all plots are removed with plot clear. Plot data is	\n"+
      "   persistent: once created in its log file, it remains even when\n"+
      "   the plot is closed, and may be viewed later with plot add.	\n"+
      "   A plot file exists in the current directory and its name has  \n"+
      "   the form \"<3pl_program_name>.plotn.plot\" where		\n"+
      "   <3pl_program_name> refers to the current 3pl program, plotn is\n"+
      "   a unique identifier for that program, eg. plot0, plot1 etc.	\n"+
      "   and the extension .plot always indicates a plot file.		\n"+
      "   Eventually the old data may be overwritten, as plot logfile	\n"+
      "   names are recycled. This may be avoided by manually renaming	\n"+
      "   plot files to something other than 				\n"+
      "   \"<3pl_program_name>.plotn.plot\".				\n"+
      "									\n"+                  
      "OPERANDS								\n"+
      "   add    Indicates the plot is to be created with the expressions\n"+
      "          which follow. Alternatively, a pre-existing plot log	\n"+
      "          file (ie. a plot that was captured earlier, and was	\n"+
      "          subsequently closed) can be displayed by passing the log\n"+
      "          file name, in double quotes, to the plot add command. If\n"+
      "          the name contains a slash character, it is taken as a	\n"+
      "          fully specified path to the file; otherwise the filename\n"+
      "          is constructed using the unique \"plotn\" as a base.	\n"+
      "          Expressions and names may be mixed in the plot add	\n"+
      "          command; all expressions will be combined into a new	\n"+
      "          plot with a single plot window, and each name will open\n"+
      "          a separate plot window to display the pre-existing log	\n"+
      "          file. Each plot has a unique name and index in the plot\n"+
      "          list, viewed with plot list. In the case of a new plot	\n"+
      "          (created with expressions, not a pre-existing filename),\n"+
      "          the new log file name is based on this unique name.	\n"+
      "   rem    Indicates the plots specified by the arguments are to be\n"+
      "          closed. The corresponding windows are closed and the	\n"+
      "          entries removed from the plot list. The plots to remove\n"+
      "          are specified either by index in the plot list or by	\n"+
      "          name. Note that when a plot closes, the corresponding	\n"+
      "          log file remains, and may be subsequently viewed using	\n"+
      "          plot add. Closing a plot window manually with a mouse	\n"+
      "          click has the same effect as plot rem.			\n"+
      "   list   Prints the plot list, ie. all currently active plots.	\n"+
      "   clear  Clears (removes) all currently active plots.		\n"+
      "									\n"+                        		
      "Type \"help plot operand\" for more detail.			";
   }
         
   /**
    * Return help text for the plot add command
    *
    * @return Help text for the plot add command
    */
   private static String helpPlotAdd () {
      return
      "plot add - create plots with new expressions or pre-existing data\n"+
      "									\n"+
      "SYNOPSIS								\n"+
      "   plot add expr|\"name\" [ expr|\"name\".. ]			\n"+
      "									\n"+            
      "DESCRIPTION							\n"+	 
      "   Indicates the plot is to be created with the expressions which\n"+
      "   follow. Alternatively, a pre-existing plot log file (ie. a plot\n"+
      "   that was captured earlier, and was subsequently closed) can be\n"+
      "   displayed by passing the log file name, in double quotes, to	\n"+
      "   the plot add command. If the name contains a slash character,	\n"+
      "   it is taken as a fully specified path to the file; otherwise the\n"+
      "   filename is constructed using the unique \"plotn\" name as a	\n"+
      "   base. Expressions and names may be mixed in the plot add	\n"+
      "   command; all expressions will be combined into a new plot with\n"+
      "   a single plot window, and each name will open a separate plot	\n"+
      "   window to display the pre-existing log file. Each plot has a	\n"+
      "   unique name and index in the plot list, viewed with plot list.\n"+
      "   In the case of a new plot (created with expressions, not a	\n"+
      "   pre-existing filename), the new log file name is based on this\n"+
      "   unique name.							\n"+
      "									\n"+                  
      "OPERANDS								\n"+
      "   expr   Any expression except an assignment (assignment 	\n"+
      "          expressions may only appear in the set command).	\n"+
      "          Wildcards are allowed.                                 \n"+
      "   name   A name which represents a pre-existing plot log file.	\n"+
      "          This may be a fully specified path name (contains a	\n"+
      "          slash) or a base name from which the filename is	\n"+
      "          constructed. For example, the name \"plot0\" would be	\n"+
      "          expanded to \"<3pl_program_name>.plot0.plot\", which	\n"+
      "          was the file created when the plot data existed as the	\n"+
      "          plot with name \"plot0\" in an earlier session.	\n"+
      "									\n"+                  
      "EXAMPLES								\n"+
      "   plot add '*.POP' '*.PUSH'					\n"+
      "   plot add main.a C.c50						\n"+
      "   plot add count(C.c50) main.a					\n"+
      "   plot add \"plot0\" \"plot3\" \"/savedplots/plot2.plot\"	\n"+
      "   plot add glob.a glob.b[1][2] \"plot0\" \"/savedplots/plot2.plot\"";
   }

   /**
    * Return help text for the plot rem command
    *
    * @return Help text for the plot rem command
    */
   private static String helpPlotRem () {
      return
      "plot rem - close one or more expression plots			\n"+
      "									\n"+
      "SYNOPSIS								\n"+
      "   plot rem index|\"name\" [index|\"name\"..]			\n"+
      "									\n"+            
      "DESCRIPTION							\n"+	 
      "   Removes (closes) the plots specified by the arguments. The	\n"+
      "   corresponding windows are closed and the entries removed from	\n"+
      "   the plot list. The plots to remove are specified either by index\n"+
      "   in the plot list or by name. Note that when a plot closes, the\n"+
      "   corresponding log file remains, and may be subsequently viewed\n"+
      "   using plot add. Closing a plot window manually with a mouse	\n"+
      "   click has the same effect as plot rem.			\n"+
      "									\n"+                  
      "OPERANDS								\n"+
      "   index  The index in the plot list of a plot to remove.	\n"+
      "   name   The name of a plot in the plot list to remove.		\n"+
      "									\n"+                  
      "EXAMPLES								\n"+
      "   plot rem 0 1 2						\n"+
      "   plot rem 2 \"plot0\" 4					";
   }

   /**
    * Return help text for the plot list command
    *
    * @return Help text for the plot list command
    */
   private static String helpPlotList () {
      return
      "plot list - list all currently active plots			\n"+
      "									\n"+
      "SYNOPSIS								\n"+
      "   plot [list]							\n"+
      "									\n"+            
      "DESCRIPTION							\n"+
      "   Lists all plots, if any exist. Each line of the listing	\n"+
      "   contains the plot index, its unique name, the expressions it	\n"+
      "   contains and its log file name. Note that indices change as	\n"+
      "   plots are added and removed.					";
   }

   /**
    * Return help text for the plot clear command
    *
    * @return Help text for the plot clear command
    */
   private static String helpPlotClear () {
      return
      "plot clear - clear all plots					\n"+
      "									\n"+
      "SYNOPSIS								\n"+
      "   plot clear							\n"+
      "									\n"+            
      "DESCRIPTION							\n"+
      "   Removes all plots, if any exist. Corresponding log files are	\n"+
      "   not affected.							";
   }

   /**
    * Return help text for the print command
    *
    * @return Help text for the print command
    */
   private static String helpPrint () {
      return
      "print and pannot - print expression values			\n"+
      "									\n"+
      "SYNOPSIS								\n"+
      "   print [format] expr [ [format] expr ... ]			\n"+
      "   pannot [format] expr [ [format] expr ... ]			\n"+
      "									\n"+            
      "DESCRIPTION							\n"+
      "   Prints the value of each expression, one per line. If no	\n"+
      "   format precedes an expression, the value is printed in its	\n"+
      "   default format. Custom default formats may be defined with the\n"+
      "   format add command. The pannot command prints expression values\n"+
      "   as for the print command, but places them in a formatted table\n"+
      "   with the expression on the left (useful for wildcard prints).	\n"+
      "									\n"+
      "OPERANDS								\n"+
      "   expr    Any expression except an assignment (assignment 	\n"+
      "           expressions may only appear in the set command).	\n"+
      "   format  A printf style format string, surrounded by double	\n"+
      "           quotes. Only one conversion specification may appear,	\n"+
      "           ie. \"%5d\" is legal but \"%x %x\" is not. The format	\n"+
      "           is applied only to the expression immediately following.\n"+
      "           In the case of a format preceding a wildcard, the	\n"+
      "           format is applied only to the first threepl identifier\n"+
      "           (ie. variable or clock) matching the wildcard.	\n"+
      "									\n"+                  
      "EXAMPLES								\n"+
      "   print glob.a							\n"+
      "   print glob.a+log(~glob.a[1])				\n"+
      "   pannot glob.b glob.c==50 ln(glob.d)+20 changed(glob.e)	\n"+
      "   pr \"%20d\" glob.a '*.D' glob.b&glob.c[3]			\n"+
      "   pa \"%-20.4d\" glob.a+glob.b \"%0b\" glob.c			";
   }

   /**
    * Return help text for the quit command
    *
    * @return Help text for the quit command
    */
   private static String helpQuit () {
      return
      "quit or exit - exit simulator";
   }

   /**
    * Return help text for the reset command
    *
    * @return Help text for the reset command
    */
   private static String helpReset () {
      return
      "reset - reset threepl program					\n"+
      "									\n"+
      "SYNOPSIS								\n"+
      "   reset								\n"+
      "									\n"+
      "DESCRIPTION							\n"+
      "   Sets the simulated threepl program to a state just before the	\n"+
      "   first clock edge. Variable initialisation values, where	\n"+
      "   applicable, are visible after reset. Current simulator time is\n"+
      "   set to -1 ns. Note the first clock edge occurs at time=0, which\n"+
      "   happens at the first step, run or cont command after a reset.	\n"+
      "   A reset occurs automatically when the simulator is first	\n"+
      "   entered.							\n"+
      "									\n"+                        		
      "Also see help timing.						";
   }

   /**
    * Return help text for the run command
    *
    * @return Help text for the run command
    */
   private static String helpRun () {
      return
      "run - continue threepl program execution				\n"+
      "									\n"+
      "SYNOPSIS								\n"+
      "   run								\n"+
      "									\n"+            
      "DESCRIPTION							\n"+
      "   Continues threepl program execution until interrupted by a	\n"+
      "   breakpoint hit                                                ";
   }

   /**
    * Return help text for the set command
    *
    * @return Help text for the set command
    */
   private static String helpSet () {
      return
      "set - assign values to threepl variables				\n"+
      "									\n"+
      "SYNOPSIS								\n"+
      "   set assignment_expr [ assignment_expr ..]			\n"+
      "									\n"+            
      "DESCRIPTION							\n"+
      "   For each assignment expression, assigns the value of the	\n"+
      "   expression on the right-hand side of the = sign to the	\n"+
      "   threepl variable on the left-hand side. Note clock identifiers\n"+
      "   are read-only and cannot be assigned to.			\n"+
      "									\n"+            
      "EXAMPLES								\n"+
      "   set glob.a=glob.b+4						\n"+
      "   set glob.a=glob.b[glob.c]+glob.d				";	      
   }

   /**
    * Return help text for the show command
    *
    * @return Help text for the show command
    */
   private static String helpShow () {
      return
      "show - print identifier (variable or clock) details		\n"+
      "									\n"+
      "SYNOPSIS								\n"+
      "   show [ filter [ filter .. ]] [ identifier_spec [ identifier_spec ..]]\n"+ 
      "									\n"+            
      "DESCRIPTION							\n"+
      "   For each identifier defined by the command parameters,	\n"+
      "   print name, group (data, control, or clock), and for variables,\n"+
      "   type. Identifiers may be specified by name or with wildcards.	\n"+
      "   In addition, filters may be used to select sub-groups of	\n"+
      "   identifiers. Available filters are:				\n"+
      "									\n"+            
      "        Filter     Description					\n"+
      "       -threepl    Only threepl variables and clocks		\n"+
      "       -hidden     Only vars which do not appear in the threepl program\n"+
      "       -variables  Only variables (threepl and hidden)		\n"+
      "       -outputs    Only output variables				\n"+
      "       -inputs     Only internal input variables			\n"+
      "       -ext        Only external input variables			\n"+
      "       -control    Only control variables			\n"+
      "       -data       Only data variables				\n"+
      "       -clocks     Only threepl clocks				\n"+
      "									\n"+            
      "   Where filters are combined the result is their intersection,	\n"+
      "   eg. -data -inputs specifies only data input variables. Filters\n"+
      "   may be supplied with or without identifier_specs. When 	\n"+
      "   identifier_spec(s) are supplied, the filters act on the list	\n"+
      "   of identifiers they represent, rather than on all identifiers.\n"+
      "   Filters and identifier_specs may appear in any order. The show\n"+
      "   command with no parameters is equivalent to show '*'.		\n"+
      "									\n"+           
      "EXAMPLES								\n"+
      "   show 								\n"+
      "   show glob.a							\n"+
      "   show glob.a 'main.b*'						\n"+
      "   show -clocks							\n"+
      "   show -control -outputs					\n"+
      "   show 'a*.' -control -ext					";
   }

   /**
    * Return help text for the step command
    *
    * @return Help text for the step command
    */
   private static String helpStep () {
      return
      "step - step threepl program through one or more clock cycles	\n"+
      "									\n"+
      "SYNOPSIS								\n"+
      "   step [ nsteps [ clock ]]					\n"+ 
      "									\n"+            
      "DESCRIPTION							\n"+
      "   If no parameters are supplied, step the threepl program through\n"+
      "   one clock cycle of the default clock. The default clock is set\n"+
      "   when the simulator starts, or with the clock command. (If the	\n"+
      "   threepl program contains only one clock, it is the default	\n"+
      "   clock.) If the integer nsteps is supplied, the program is	\n"+
      "   stepped through nsteps default clock cycles. If the clock	\n"+
      "   identifier is supplied, then nsteps of that clock (which may be\n"+
      "   different to the default clock) are executed.			\n"+
      "									\n"+            
      "EXAMPLES								\n"+
      "   step								\n"+
      "   step 100							\n"+
      "   step 2 C.c50							\n"+
      "   step glob.a*2+3 C.c100					";      
   }

   /**
    * Return help text on timing
    *
    * @return Help text on timing
    */
   private static String helpTiming () {
      return
      "Timing - simulator time						\n"+
      "									\n"+
      "During simulation of a threepl program, the simulator maintains	\n"+
      "the time since \"start\", ie. since the first positive going clock\n"+
      "edge. The first clock edge occurs at the first step, cont or run	\n"+
      "command after a reset. Time is in nanoseconds. Current time can	\n"+
      "be obtained with the t() function; eg. pr t() prints the current	\n"+
      "time. Any events (like clock edges) which were scheduled to happen\n"+
      "at a particular time have just happened when that time is current.\n"+
      "									\n"+
      "At simulator initial entry and after a reset command, time is set\n"+
      "to -1. This is an arbitrary way of representing time before the	\n"+
      "first clock edge at time=0. Note variable initialisation values,	\n"+
      "where applicable, are visible at time=-1. Variables displayed in	\n"+
      "a plot normally display from time=0, but plots may be panned left\n"+
      "to show variable states at time=-1, ie. at reset time.		\n"+
      "									\n"+
      "Also see help reset.						";
   }
   /**
    * Return help text for the trace command
    *
    * @return Help text for the trace command
    */
   private static String helpTrace () {
      return
      "trace - add, remove, list, clear, enable and disable expression traces\n"+
      "									\n"+            
      "SYNOPSIS								\n"+
      "   trace add expr|\"name\" [ expr|\"name\".. ]			\n"+
      "   trace rem index|\"name\" [index|\"name\"..]			\n"+
      "   trace [list]							\n"+
      "   trace clear							\n"+
      "   trace on index|\"name\" [index|\"name\"..] [expr]		\n"+
      "   trace off index|\"name\" [index|\"name\"..]			\n"+
      "									\n"+            
      "DESCRIPTION							\n"+	 
      "   Adds, removes, lists, clears, enables, disables expression traces.\n"+
      "   An expression trace contains a time series of one or more	\n"+
      "   expression values. These are stored in a log file and are 	\n"+
      "   displayed in a terminal emulation window. The expressions that\n"+
      "   form the trace are specified in the trace add command. A trace\n"+
      "   is closed with the trace rem command, or by closing the trace	\n"+
      "   window with a mouse click, which has the same effect. The	\n"+
      "   currently open traces can be listed with trace list, and all	\n"+
      "   traces are removed with trace clear. Trace data is persistent:\n"+
      "   once created in its log file, it remains even when the trace is\n"+
      "   closed, and may be viewed later with trace add.  A trace file	\n"+
      "   exists in the current directory and its name has the form	\n"+
      "   \"<3pl_program_name>.tracen.trace\" where <3pl_program_name>	\n"+
      "   refers to the current 3pl program, tracen is a unique identifier\n"+
      "   for that program, eg. trace0, trace1 etc. and the extension	\n"+
      "   .trace always indicates a trace file. Eventually the old data	\n"+
      "   may be overwritten, as trace logfile names are recycled. This	\n"+
      "   may be avoided by manually renaming trace files to something	\n"+
      "   other than \"<3pl_program_name>.tracen.trace\".		\n"+
      "									\n"+
      "   A trace is enabled by default when created with the trace add	\n"+
      "   command, ie. the trace expressions are logged at each clock	\n"+
      "   edge. A trace may be disabled with trace off: the trace remains\n"+
      "   open but expression value logging ceases. A trace may be	\n"+
      "   re-enabled with trace on. If a (boolean) expression is supplied\n"+
      "   as the last argument to trace on, the trace(s) will become	\n"+
      "   enabled when the expression is true. This feature allows a	\n"+
      "   trace to trigger under the desired conditions, avoiding the 	\n"+
      "   overhead of trace logging before the condition becomes true.	\n"+
      "									\n"+                  
      "OPERANDS								\n"+
      "   add    Indicates the trace is to be created with the expressions\n"+
      "          which follow. Alternatively, a pre-existing trace log	\n"+
      "          file (ie. a trace that was captured earlier, and was	\n"+
      "          subsequently closed) can be displayed by passing the log\n"+
      "          file name, in double quotes, to the trace add command. If\n"+
      "          the name contains a slash character, it is taken as a\n"+
      "          fully specified path to the file; otherwise the filename\n"+
      "          is constructed using the unique \"tracen\" as a base.	\n"+
      "          Expressions and names may be mixed in the trace add	\n"+
      "          command; all expressions will be combined into a new	\n"+
      "          trace with a single trace window, and each name will open\n"+
      "          a separate trace window to display the pre-existing log\n"+
      "          file. Each trace has a unique name and index in the trace\n"+
      "          list, viewed with trace list. In the case of a new trace\n"+
      "          (created with expressions, not a pre-existing filename),\n"+
      "          the new log file name is based on this unique name.	\n"+
      "   rem    Indicates the traces specified by the arguments are to be\n"+
      "          closed. The corresponding windows are closed and the	\n"+
      "          entries removed from the trace list. The traces to remove\n"+
      "          are specified either by index in the trace list or by	\n"+
      "          name. Note that when a trace closes, the corresponding	\n"+
      "          log file remains, and may be subsequently viewed using	\n"+
      "          trace add. Closing a trace window manually with a mouse\n"+
      "          click has the same effect as trace rem.		\n"+
      "   list   Prints the trace list, ie. all currently active traces.\n"+
      "   clear  Clears (removes) all currently active traces.		\n"+
      "   on     Enables the specified existing traces, such that trace	\n"+
      "          expression values will be logged and displayed at each	\n"+
      "          clock edge. If the last argument is a boolean expression,\n"+
      "          the trace(s) become enabled when the expression is true.\n"+
      "          Traces are enabled by default when created.		\n"+
      "   off    Disables the specified existing traces. The traces	\n"+
      "          remain open but logging ceases until re-enabled.	\n"+
      "									\n"+                        		
      "Type \"help trace operand\" for more detail.			";
   }
         
   /**
    * Return help text for the trace add command
    *
    * @return Help text for the trace add command
    */
   private static String helpTraceAdd () {
      return
      "trace add - create traces with new expressions or pre-existing data\n"+
      "									\n"+
      "SYNOPSIS								\n"+
      "   trace add expr|\"name\" [ expr|\"name\".. ]			\n"+
      "									\n"+            
      "DESCRIPTION							\n"+	 
      "   Indicates the trace is to be created with the expressions which\n"+
      "   follow. Alternatively, a pre-existing trace log file (ie. a trace\n"+
      "   that was captured earlier, and was subsequently closed) can be\n"+
      "   displayed by passing the log file name, in double quotes, to	\n"+
      "   the trace add command. If the name contains a slash character,\n"+
      "   it is taken as a fully specified path to the file; otherwise the\n"+
      "   filename is constructed using the unique \"tracen\" as a base.\n"+
      "   Expressions and names may be mixed in the trace add command; all\n"+
      "   expressions will be combined into a new trace with a single trace\n"+
      "   window, and each name will open a separate trace window to	\n"+
      "   display the pre-existing log file. Each trace has a unique name\n"+
      "   and index in the trace list, viewed with trace list. In the case\n"+
      "   of a new trace (created with expressions, not a pre-existing	\n"+
      "   filename), the new log file name is based on this unique name.\n"+
      "   A new trace line is logged on every clock edge, both active   \n"+
      "   and inactive. Only a single trace line is logged where multiple\n"+
      "   clock edges fall on the same time. 				\n"+
      "									\n"+                  
      "OPERANDS								\n"+
      "   expr   Any expression except an assignment (assignment 	\n"+
      "          expressions may only appear in the set command).	\n"+
      "          Wildcards are allowed. 				\n"+
      "   name   A name which represents a pre-existing trace log file.	\n"+
      "          This may be a fully specified path name (contains a	\n"+
      "          slash) or a base name from which the full path is	\n"+
      "          constructed. For example, the name \"trace0\" would be	\n"+
      "          expanded to \"<3pl_program_name>.trace0.trace\", which	\n"+
      "          was the file created when the trace data existed as the\n"+
      "          trace with name \"trace0\" in an earlier session.	\n"+
      "									\n"+                  
      "EXAMPLES								\n"+
      "   trace add '*.POP' '*.PUSH'					\n"+
      "   trace add C.c50 main.a					\n"+
      "   trace add main.a main.b					\n"+
      "   trace add \"trace0\" \"trace3\" \"/savedtraces/trace2.trace\"	\n"+
      "   trace add glob.a glob.b[1][2] \"trace0\" \"/savedtraces/trace2.trace\"";
   }

   /**
    * Return help text for the trace rem command
    *
    * @return Help text for the trace rem command
    */
   private static String helpTraceRem () {
      return
      "trace rem - close one or more expression traces			\n"+
      "									\n"+
      "SYNOPSIS								\n"+
      "   trace rem index|\"name\" [index|\"name\"..]			\n"+
      "									\n"+            
      "DESCRIPTION							\n"+	 
      "   Removes (closes) the traces specified by the arguments. The	\n"+
      "   corresponding windows are closed and the entries removed from	\n"+
      "   the trace list. The traces to remove are specified either by index\n"+
      "   in the trace list or by name. Note that when a trace closes, the\n"+
      "   corresponding log file remains, and may be subsequently viewed\n"+
      "   using trace add. Closing a trace window manually with a mouse	\n"+
      "   click has the same effect as trace rem.			\n"+
      "									\n"+                  
      "OPERANDS								\n"+
      "   index  The index in the trace list of a trace to remove.	\n"+
      "   name   The name of a trace in the trace list to remove.	\n"+
      "									\n"+                  
      "EXAMPLES								\n"+
      "   trace rem 0 1 2						\n"+
      "   trace rem 2 \"trace0\" 4					";
   }

   /**
    * Return help text for the trace list command
    *
    * @return Help text for the trace list command
    */
   private static String helpTraceList () {
      return
      "trace list - list all currently active traces			\n"+
      "									\n"+
      "SYNOPSIS								\n"+
      "   trace [list]							\n"+
      "									\n"+            
      "DESCRIPTION							\n"+
      "   Lists all traces, if any exist. Each line of the listing	\n"+
      "   contains the trace index, its unique name, the expressions	\n"+
      "   it contains and its log file name. Note that indices change as\n"+
      "   traces are added and removed.					";
   }

   /**
    * Return help text for the trace clear command
    *
    * @return Help text for the trace clear command
    */
   private static String helpTraceClear () {
      return
      "trace clear - clear all traces					\n"+
      "									\n"+
      "SYNOPSIS								\n"+
      "   trace clear							\n"+
      "									\n"+            
      "DESCRIPTION							\n"+
      "   Removes all traces, if any exist. Corresponding log files are	\n"+
      "   not affected.							";
   }

   /**
    * Return help text for the trace on command
    *
    * @return Help text for the trace on command
    */
   private static String helpTraceOn () {
      return
      "trace on - enable traces						\n"+
      "									\n"+
      "SYNOPSIS								\n"+
      "   trace on index|\"name\" [index|\"name\"..] [expr]		\n"+
      "									\n"+            
      "DESCRIPTION							\n"+
      "   Enables the specified existing traces, such that trace	\n"+
      "   expression values will be logged and displayed at each clock	\n"+
      "   edge. If the last argument is a boolean expression, the trace(s)\n"+
      "   become enabled when the expression is true. Traces are enabled\n"+
      "   by default when created.					\n"+
      "									\n"+
      "EXAMPLES								\n"+
      "   trace on 1 2 3						\n"+
      "   trace on 1 C.c100==1000					\n"+
      "   trace on 1 \"trace2\" glob.a==glob.b+4			";
   }
   
   /**
    * Return help text for the trace off command
    *
    * @return Help text for the trace off command
    */
   private static String helpTraceOff () {
      return
      "trace off - disable traces					\n"+
      "									\n"+
      "SYNOPSIS								\n"+
      "   trace off index|\"name\" [index|\"name\"..]			\n"+
      "									\n"+            
      "DESCRIPTION							\n"+
      "   Disables the specified existing traces, such that trace	\n"+
      "   expression values will not be logged and displayed at each clock\n"+
      "   edge. This command is used to disable a trace before		\n"+
      "   conditionally enabling it with trace enable. Traces are enabled\n"+
      "   by default when created.					\n"+
      "									\n"+
      "EXAMPLES								\n"+
      "   trace off 1 2 3						\n"+
      "   trace on 1 \"trace2\"						";
   }
   
   /**
    * Return help text on expressions
    *
    * @return Help text on expressions
    */
   private static String helpExpressions () {
      return
      "Expressions -							\n"+
      "									\n"+
      "Expressions are used in most commands requiring parameters.	\n"+
      "									\n"+
      "An expression may contain constants, including threepl initialisers,\n"+
      "threepl variable or clock identifiers, operators and defined	\n"+
      "functions. An expression may consist of a wildcard identifier spec.\n"+
      "An expression may be an assignment, where the assignment operator is\n"+
      "the = sign and a	threepl identifier must appear on the left hand	\n"+
      "side. Parentheses may be used to override natural operator precedence.\n"+
      "An expression cannot contain white space.			\n"+
      "									\n"+
      "An expression has a type which is the type of the expression	\n"+
      "result. This may be int, float, boolean or string. For numeric	\n"+
      "expressions, the type is int where all operands are of type int.	\n"+
      "The type is float any if operands are float. For logical		\n"+
      "expressions, the type is boolean. An expression returning an	\n"+
      "identifier name has type string.  The type of an assignment	\n"+
      "expression is taken from the variable expression on the left-hand\n"+
      "side. Boolean expressions are required for breakpoint expressions.\n"+
      "Different types may not be combined with an operator.		\n"+
      "									\n"+
      "A special type of expression is the var expression. This contains\n"+
      "a threepl identifier and possibly trailing array subscripts and/or\n"+
      "struct field references. Var expressions may be included in larger\n"+ 
      "expressions. Var expression examples: glob.a, glob.a[][5..7].x	\n"+
      "									\n"+
      "The type of a var expression is int for all int and uint threepl	\n"+
      "variables, float for fixed and float variables, and boolean for	\n"+
      "log and control variables. If the identifier is a threepl clock,	\n"+
      "the type is control (representing the clock level, high or low).	\n"+
      " 								\n"+           
      "The precision of the value of an expression is the same as the	\n"+
      "var expression if the expression contains only the var expression;\n"+
      "otherwise the precision is effectively infinite as in that case	\n"+
      "the expression result is stored as a double. For example, the	\n"+
      "precision of the expression glob.a (where glob.a is uint:8) is 8	\n"+
      "bits, and printing glob.a in an integer format results in output	\n"+
      "reflecting this. The expression glob.a*1 has the same value, but	\n"+
      "this is stored as a double. Printing this expression in an integer\n"+
      "format results in an effective 32 bit output (or 16 or 64 bits	\n"+
      "respectively if format length modifiers h or l are used).	\n"+
      " 								\n"+          
      "Type \"help constants\" for more detail on constants.		\n"+    
      "Type \"help initialisers\" for more detail on initialisers.	\n"+    
      "Type \"help operators\" for more detail on operators.		\n"+    
      "Type \"help functions\" for more detail on functions.		\n"+    
      "Type \"help wildcards\" for more detail on wildcards.		";    
   }

   /**
    * Return help text on constants
    *
    * @return Help text on constants
    */
   private static String helpConstants () {
      return
      "Constants - used in expressions					\n"+
      "									\n"+
      "Integer constants may be decimal, hexadecimal (leading 0x or 0X),\n"+
      "octal (leading 0) or binary (leading 0b or 0B).			\n"+
      "									\n"+
      "Floating point constants may be specified in any of the standard	\n"+
      "formats, eg. 4., .3, 5.6, 6e-7 etc.				\n"+
      "									\n"+
      "Logical constants are true and false.				\n"+ 
      "									\n"+
      "Control constants are high and low, which are interchangeable with\n"+
      "logical constants, ie. true==high and false==low.		\n"+     
      "									\n"+
      "Compound constants follow the threepl initialiser syntax. See	\n"+
      "help initialisers.						\n"+
      "									\n"+
      "In addition the following symbolic constants are defined:	\n"+
      "PI   the ratio of a circle's circumference to its diameter;	\n"+
      "E    Euler's number: the base of the natural logarithms.		";      
   }

   /**
    * Return help text on command files
    *
    * @return Help text on (command) files
    */
   private static String helpFiles () {
      return
      "Command files - used to execute a list of commands		\n"+
      "									\n"+
      "A command file is a text file containing a list of simulator	\n"+
      "commands, as they would be typed at the command line. A command	\n"+
      "file is a mechanism for automated setup of breakpoints, plots,	\n"+
      "display formats, etc. A command file is executed with the call	\n"+
      "command. Files may contain any simulator command, including call.\n"+
      "A command file must not call itself, even indirectly.		\n"+
      "									\n"+
      "If a command file \"name.sim\" exists in the current directory,	\n"+
      "where name is the name of the 3pl program under test, then that	\n"+
      "file is executed automatically on simulator startup.		\n"+
      "									\n"+
      "Also see help call.						";
   }

   /**
    * Return help text on operators
    *
    * @return Help text on operators
    */
   private static String helpOperators () {
      return
      "Operators - used in expressions					\n"+
      "									\n"+
      "All threepl operators are available for use in expressions, except\n"+
      "--, ++ and assignment operators other than =. In addition, the cast\n"+
      "operators (int) and (float) are available. Note the threepl sizeof\n"+
      "operator is implemented as a function; see help functions.	\n"+
      "Operators are listed below in decreasing order of precedence.	\n"+
      "									\n"+
      "Operator    Description						\n"+
      "									\n"+
      "  n1..n2    array subscript range (..n2 optional)		\n"+
      "  .         struct field prefix					\n"+
      "  [range]   array subscript					\n"+
      "  !         unary prefix, logical negation			\n"+
      "  ~         unary prefix, bitwise inversion			\n"+
      "  ?         unary prefix, queue examine				\n"+
      "  <         unary prefix, queue read availability			\n"+
      "  >         unary prefix, queue write availability		\n"+
      "  @         unary prefix, queue/ram internal buffer view          \n"+
      "  (int)     unary prefix, cast numeric value to int		\n"+
      "  (float)   unary prefix, cast numeric value to float		\n"+
      "  *         binary, multiplication				\n"+
      "  /         binary, division					\n"+
      "  %         binary, remainder					\n"+
      "  +         unary prefix, binary, addition			\n"+
      "  -         unary prefix, negation or binary, subtraction	\n"+
      "  <<        binary, left shift					\n"+
      "  >>        binary, right shift					\n"+
      "  <         binary, less than					\n"+
      "  >         binary, greater than					\n"+
      "  <=        binary, less than or equal to			\n"+
      "  >=        binary, greater than or equal to			\n"+
      "  ==        binary, equal to					\n"+
      "  !=        binary, not equal to					\n"+
      "  &         binary, bitwise and					\n"+
      "  |         binary, bitwise or					\n"+
      "  ^         binary, bitwise xor					\n"+
      "  &&        binary, logical and					\n"+
      "  ||        binary, logical or					\n"+
      "  ^^        binary, logical xor					\n"+
      "  ?:        ternary, conditional					\n"+
      "  =         binary, assignment					\n"+      
      "									\n"+
      "Queue operators							\n"+
      "									\n"+
      "There are four unary prefix operators associated with queues. The \n"+
      "<, >, and ? operators follow the threepl syntax. The @ operator is\n"+
      "only used in the simulator, and returns a view of the queue internal\n"+
      "buffer, as an array. The the first (index 0) element of the array\n"+
      "is the last value written to the queue. The last (highest index)	\n"+ 
      "element of the array is equivalent to the queue output. The length\n"+
      "of the array varies according to the number of elements stored in\n"+
      "the queue, and is zero when the queue is unavailable for read. The	\n"+
      "elements of the array have the same structure as the queue variable.\n"+
      "If the queue variable is a compound variable (array or struct etc.),\n"+
      "then a subset of the internal buffer array can be viewed by	\n"+
      "specifying a portion of the buffer, eg.				\n"+
      "   @a								\n"+
      "   @a[]								\n"+
      "   @a[1..2].b.c							\n"+
      "are all valid expressions. Expressions prefixed by @ may appear on\n"+
      "the left hand side of an assignment, so queue buffer values can be\n"+
      "manually modified.  						\n"+
      "									\n"+
      "Ram operators							\n"+
      "									\n"+
      "The @ operator may be used with any variable representing a data	\n"+
      "port output of a combinatorial or registered output ram, in order\n"+
      "to access the ram contents directly, as an array. Syntax and usage\n"+
      "are as for queues (see above).					\n";
   }

   /**
    * Return help text on functions
    *
    * @return Help text on functions
    */
   private static String helpFunctions () {
      return
      "Functions - used in expressions					\n"+
      "									\n"+
      "Available functions are:						\n"+
      "									\n"+
      "Function           Description					\n"+
      "									\n"+
      "acc(varexp)        boolean true if var was accessed last clock cycle\n"+
      "changed(varexp)    boolean true if var changed since last halt	\n"+
      "dimension(varexp,i) int, length of the ith dimension of this var expression\n"+
      "dimensions(varexp) int, number of dimensions of this var expression\n"+
      "freq(clock)        float, frequency, MHz, of this clock		\n"+
      "ln(number)         float, log base e				\n"+
      "log(number)        float, log base 10				\n"+
      "max(x,y)           int or float, maximum of x and y		\n"+
      "min(x,y)           int or float, minimum of x and y		\n"+
      "period(clock)      float, period, ns of this clock		\n"+
      "pow(x,y)           float, x raised to the power y		\n"+
      "read(varexp)       boolean true if var was read last clock cycle	\n"+
      "sizeof(varexp)     int, number of primitive words within a var expression\n"+
      "t()                float, time in ns since reset                 \n"+
      "count(clock)       int, current active edge count of this clock	\n"+
      "queuereadstatus(queue) struct describing async queue read status    \n"+
      "queuewords(varexp)  int, buffer occupancy count for this queue var	\n"+
      "queuewritestatus(queue) struct describing async queue write status  \n"+
      "pw(varexp)         int, same as queuewords			\n"+
      "queuespaces(varexp) int, buffer empty count for this queue var	\n"+
      "ps(varexp)         int, same as queuespaces			\n"+
      "writ(varexp)       boolean, true if var written last clock cycle	\n"; 
   }

   /**
    * Return help text on wildcards
    *
    * @return Help text on wildcards
    */
   private static String helpWildcards () {
      return
      "Wildcards - used to specify multiple identifiers			\n"+
      "									\n"+
      "A wildcard may be used in place of a single identifier (threepl	\n"+
      "variable or clock name) to represent multiple identifiers, where	\n"+
      "the identifier would not appear as part of a larger expression.	\n"+
      "Wildcards must be enclosed in single quotes. The wildcard	\n"+
      "characters \"*\" and \"?\" are used to specify any number of matching\n"+
      "characters and any single matching character, respectively.	\n"+
      "									\n"+
      "EXAMPLES								\n"+
      "   print 'a_*'							\n"+
      "   show 'glob.*' 'glob.b??' -control				\n"+
      "   display add '*.NE'						";
   }

   /**
    * Return help text on formats
    *
    * @return Help text on formats
    */
   private static String helpFormats () {
      return
      "Formats - used to specify output formats for display and print	\n"+
      "									\n"+
      "Refer to print and display commands for context. A format is a	\n"+
      "printf style format string enclosed in double quotes. It may	\n"+
      "contain only one conversion specification (ie. % surrounded by	\n"+
      "other characters). The standard printf flags, field width, precision\n"+
      "and length modifiers are allowed. The conversion	character b is	\n"+
      "also allowed, for binary format output.				\n"+
      "									\n"+
      "Integer type conversion characters are allowed with float	\n"+
      "expressions and vice-versa. An integer type conversion character	\n"+
      "specified with a boolean expression is allowed, and results in an\n"+
      "output of 1 (true) or 0 (false), formatted accordingly. Float type\n"+
      "conversion characters are not allowed with boolean expressions.	\n"+
      "									\n"+
      "EXAMPLES								\n"+
      "   pr \"%20.10d\" 10						\n"+
      "   pr \"%d\" 4.5							\n"+
      "   pr \"%x\" glob.a \"%5b\" glob.b \"%d\" C.c100			\n"+
      "   display add \"%d\" glob.a.NE					";
   }

   /**
    * Return help text on filters
    *
    * @return Help text on filters
    */
   private static String helpFilters () {
      return
      "Filters - used in show command					\n"+
      "									\n"+
      "Filters are used in the show command to specify subsets of threepl\n"+
      "identifiers (variables or clocks). A filter has a name which has a\n"+
      "leading hyphen. Filters available are:				\n"+
      "									\n"+
      "        Filter     Description					\n"+
      "       -threepl    Only threepl variables and clocks		\n"+
      "       -hidden     Only vars which do not appear in the threepl program\n"+
      "       -variables  Only variables (threepl and hidden)		\n"+
      "       -outputs    Only output variables				\n"+
      "       -inputs     Only internal input variables			\n"+
      "       -ext        Only external input variables			\n"+
      "       -control    Only control variables			\n"+
      "       -data       Only data variables				\n"+
      "       -clocks     Only threepl clocks				\n"+
      "									\n"+            
      "Where filters are combined the result is their intersection, eg.	\n"+
      "-data -inputs specifies only data input variables. By default	\n"+
      "filters act on the full set of available threepl identifiers, but\n"+
      "if they appear with an identifier specification (eg. a wildcard)	\n"+
      "they act only on the set of identifiers specified.		\n"+
      "									\n"+
      "EXAMPLES								\n"+
      "   show -data							\n"+
      "   show -control -inputs						\n"+
      "   show '*.PUSH' '??b*' -threepl					";
   }

   /**
    * Return help text on arrays
    *
    * @return Help text on arrays
    */
   private static String helpArrays () {
      return
      "Arrays -								\n"+
      "									\n"+
      "Array expressions are used when a subset of a threepl array	\n"+
      "variable is required in an expression. An array expression consists\n"+
      "of the variable name followed by the dimensional ranges in square\n"+
      "brackets, and follows the same syntax rules as for threepl. If a \n"+
      "range is not specified, either with empty brackets [] or by omitting\n"+
      "less significant ranges altogether, the range is taken to be the same\n"+
      "as the width of the corresponding array dimension.		\n"+
      "									\n"+
      "When assigning to an array or part thereof, the expression on the\n"+
      "right hand side of the assignment must result in an array of the	\n"+
      "number of elements specified by the array expression on the left	\n"+
      "hand side.							\n"+
      "									\n"+
      "EXAMPLES								\n"+
      "   pr glob.a[2][C.c100+1][4..5]					\n"+
      "   pr glob.a[][0..7]						\n"+
      "   set glob.a[0..2][3..5][5..7]=glob.b[0..26]			";
   }

   /**
    * Return help text on ranges
    *
    * @return Help text on ranges
    */
   private static String helpRanges () {
      return
      "Ranges -								\n"+
      "									\n"+
      "A range expression is used to refer to a range of indices within	\n"+
      "an array subscript. A range expression follows the same syntax rules\n"+
      "as for threepl. A single integer expression within square brackets\n"+
      "specifies a single index, and two integers separated by ellipses .. \n"+
      "within square brackets specify an index range. A pair of square	\n"+
      "brackets with no index is equivalent to the full range of that dimension.\n"+     
      "									\n"+
      "EXAMPLES								\n"+
      "   pr glob.a[3]							\n"+
      "   pr glob.a[2][][4..55						\n"+
      "   set glob.a[][3][5][2..3]=glob.b				";
   }

   /**
    * Return help text on initialisers
    *
    * @return Help text on initialisers
    */
   private static String helpInitialisers () {
      return
      "Initialisers -							\n"+
      "									\n"+
      "An initialiser is used to set a compound variable, or part thereof\n"+
      "to a value, or to test for equality to a compound value or another\n"+
      "initialiser. Initialisers follow the same syntax as for threepl.	\n"+
      "									\n"+
      "In assignment, where fields are omitted within the initialiser, the\n"+
      "corresponding fields in the assigned variable are not modified.	\n"+
      "In a test for equality, corresponding fields in the compound structures\n"+
      "on each side of the compare must be equal, except where fields are\n"+
      "omitted in the initialiser.					\n"+
      "									\n"+
      "EXAMPLES								\n"+
      "   set glob.a={1,2,3}						\n"+
      "   set glob.b={,{1,{2,glob.c}}}					\n"+
      "   pr glob.a=={1,2,3,4}						\n"+
      "   pr glob.b=={,{,{,3}}}						\n"+
      "   pr {1,2,3}=={,2,3}      (not useful but syntax allows)	";
   }

   /**
    * Throw exception where no help available on specified topics
    *
    * @param topics An array of help topics
    * @throws SimException
    */
   private static void helpError (String[] topics) {
  
      String s = "no help available";
      if (topics!=null && topics.length>0) {
         s += " for";
         for (int i=0; i<topics.length; i++)
	    s += " "+topics[i];
      }	    
      throw new SimException(s);
   }    
}
