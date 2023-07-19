package threepl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import javax.swing.JFileChooser;

import threepl.codegen.TDE;
import threepl.codegen.TDEConstants;
import threepl.codegen.TDEList;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Body;
import threepl.exec.Clock;
import threepl.exec.FileDesc;
import threepl.exec.Function;
import threepl.exec.Immediate;
import threepl.exec.Group;
import threepl.exec.Module;
import threepl.exec.Procedure;
import threepl.exec.Queue;
import threepl.exec.Scope;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.exec.Var.IDtype;
import threepl.funcs.InbuiltFunc;
import threepl.funcs.InbuiltFuncs;
import threepl.mods.InbuiltMod;
import threepl.mods.InbuiltMods;
import threepl.netlist.Display;
import threepl.netlist.Element;
import threepl.netlist.Net;
import threepl.netlist.TDECode;
import threepl.netlist.xilinx.UNISIMS;
import threepl.nodes.BlockNode;
import threepl.nodes.Ident;
import threepl.parser.FileChooser;
import threepl.parser.ParseException;
import threepl.parser.Parser;
import threepl.parser.SrcLoc;
import threepl.parser.SrcReader;
import threepl.parser.Token;
import threepl.parser.TokenMgrError;
import threepl.parser.Functions;
import threepl.procs.InbuiltProc;
import threepl.procs.InbuiltProcs;

/**
 * This is the compiler main class.
 */
public class ThreePL implements TDEConstants {
    public static  long nanotime0 = System.nanoTime();// start of 3PL
    public static  long nanotime1;                    // start of execution
    public static  long nanotime2;                    // start of TDE list optimisation
    public static  long nanotime3;                    // start of netlist generation
    public static  long nanotime4;                    // start of netlist optimisation
    public static  long nanotime5;                    // start of netlist output
    public static  long nanotime6;                    // finish
    public static  Runtime          main_rt = Runtime.getRuntime();

    // The file and directory strings below are all absolute paths.
    protected static JFileChooser   fc;
    public    static String         source_file = null;
    public    static String         current_directory = null;
    public    static String         parent_directory = null;
    public    static String         netlist_file = null;
    public    static String         report_file = null;
    public    static String         icl_file = null;
    public    static String         net_file = null;
    public    static StringBuffer   msgsb;

    private static  StringBuffer    cloptions = new StringBuffer();
    private static  SrcReader       input_stream = null;
    private static  Parser          parser;

    public static TreeMap<String, Group>        groups;
    public static TreeMap<String, Module>       modules;
    public static TreeMap<String, Procedure>    procedures;
    public static TreeMap<String, Function>     functions;
    protected static InbuiltMods                inbuilt_mods;
    protected static InbuiltProcs               inbuilt_procs;
    protected static InbuiltFuncs               inbuilt_funcs;
    protected static HashSet<Clock>             clocks;
    protected static ArrayList<Clock>           clockvarstack;
    protected static Clock                      currentclockvar;
    protected static ArrayList<Module>          leader_modules;     // list of leader modules
    protected static Module                     main;               // main module
    protected static ArrayList<Trailer>         trailer_modules;    // list of trailer modules
    protected static ArrayList<Module>          post_modules;       // list of post-processing modules
    protected static ArrayList<BlockNode>       init_class;         // list of initialisation blocks for classes
    protected static LinkedList<Body>           body_stack;         // group(3PL class)/module/procedure/function bodies during execution
    protected static LinkedList<Scope>          scope_stack;        // block scopes during execution
    protected static LinkedList<Body>           class_body_stack;   // class bodies during execution
    protected static LinkedList<String>         call_name_stack;    // stack for call names - null for user-defined
    
    protected static LinkedList<SrcLoc>         call_loc_stack;
    protected static ArrayList<ThreePL.ExecPair> execList;
    public static String                        file_path;      // current input file path
    public static String                        file_name;      // current input file name
    public static String                        file_dir;       // current input file directory
    public static String                        abbrev_path;
    public static int                           dir_index = -1;
    public static ArrayList<String>             file_paths;     // list of file paths
    public static ArrayList<String>             file_dirs;      // list of file directories
    public static ArrayList<String>             files;          // list of file names
    public static ArrayList<Integer>            file_depths;    // list of depth of file inclusions
    public static ArrayList<Integer>            file_indices;   // list of module file indices or -1
    public static int                           file_depth;     // depth of file inclusion
    public static int                           line_no;        // current line number
    public static String                        prev_file_name;
    public static String                        prev_dir;
    public static int                           prev_line_no;
    protected static int                        warnings;
    public static  FileDesc                     stdinOpenFile;
    public static  FileDesc                     stdoutOpenFile;
    public static  FileDesc                     stderrOpenFile;
    public static  PrintStream                  rptps;          // report file
    public static  PrintStream                  iclps;          // intermediate code list file
    public static  LinkedList<Var>              var_queue;
    public static  Procedure                    attributesProc;
    public static  TreeMap<String, Val>         directives;
    public static  int                          directive_max_key_length;
    public static  Scope                        global_scope;
    public static  int                          errors;
    public static  TDEList                      tdelist;
    public static  boolean                      tdelist_optimisation_started;
    public static  boolean                      tdelist_optimisation_finished;
    public static  TDEVar                       startval_tdev;
    public static  SrcLoc                       errloc;
    public static  int                          icount;     // unique integer for element identifiers
    public static  boolean                      creating_variables;
    public static  ArrayList<String>            include_dirs = new ArrayList<String>();
    public static  boolean                      readonly = false;
    public static  boolean                      isparsing;
    public static  boolean                      postProcessing;
    public static  boolean                      top_scope_skip;
    public static final boolean                 sim_implemented = false;    // simulator disabled
    public static  boolean                      sim;
    public static  boolean                      valuelabel = false;         // see Value.combine()
    public static  boolean                      develop;
    public static  boolean                      clockdiag;
    public static  boolean                      codeopt = true;
    public static  boolean                      varerrors = false;
    public static  boolean                      continuous = false;
    public static  boolean                      ALU = false;
    public static  boolean                      oack_use_lut = true;
    public static  boolean                      as_fifo_read_use_lut = true;
    public static  boolean                      as_fifo_empty_use_lut = true;
    public static  boolean                      add_sub_use_lut = true;
    public static  boolean                      ge_use_lut = true;
    public static  boolean                      mux_use_lut = false;
    public static  boolean                      incrdecr_use_lut = true;
    public static  boolean                      cpld_use_lut = false;
    public static  boolean                      gates_not_luts = false;
    public static  boolean                      fifos_ok = false; // BRAM FIFOS slower!
    public static  boolean                      queuereg_ok = false;
    public static  boolean                      file_chooser_used = false;
    public static  boolean                      fc_skip_post_processing = false;
    public static  boolean                      fc_force_execution = false;
    public static  boolean                      fc_list_tdes = false;
    public static  boolean                      fc_list_nets = false;
    public static  boolean                      fc_report = false;
    public static  String                       ce_name;        // current element name
    public static  String                       outdir_opt;     // output directory option
    public static  String                       outdir;         // output directory
    public static  String                       design_name;    // design name
    public static  String                       cldn;           // command line design name
    public static  String                       cell_name;      // EDIF cell name
    public static  String                       partstring;     // part specification
    public static  TDECode                      family;         // device family class
    public static  String                       libref;         // EDIF library reference string
    public static  long                         currentTime = 0;// current millisecond time
    public static  SimpleDateFormat             dateFormat;     // format for most dates
    public static  String                       currentDate;    // current date string
    public static  long                         compilerTime = 0;// build millisecond time
    public static  String                       fullVersion;    // long version of version
    public static  long                         sourceTime = 0; // maximum of millisecond time of all source files
    public static  long                         netlistTime = 0;// millisecond time of netlist file
    public static  long                         reportTime = 0; // millisecond time of report file
    public static  long                         netTime = 0;    // millisecond time of net file
    public static  ArrayList<String>            shells = new ArrayList<String>();   // list of available shells
    public static  Map<String,String>           environment;    // environment variable map
    public static  String                       os_name = System.getProperty("os.name").toLowerCase();
    public static  String                       arch_name = System.getProperty("os.arch").toLowerCase();
    public static  String                       version;           // release version number
    public static  String                       revision;          // git revision string
    public static  String                       lastChangedAuthor; // git last changed author
    public static  String                       lastChangedRev;    // git last changed revision
    public static  String                       lastChangedDate;   // git last changed date
    public static  String                       buildDate;         // build date/time
    public static  String                       builtBy;           // user that ran build
    
    static private class ExecPair {
        Val             val;
        HashSet<TDEVar> execs;

        private ExecPair (Val val, HashSet<TDEVar> execs) {
            this.val = val;
            this.execs = execs;
        }
    }
    
    static private class Trailer {
        Module  module = null;
        Scope   scope = null;
        Integer order = 0;
    }

    /**
     * Compiler main method.
     * @param   args contains the call line arguments
     * @throws Exception on an error
     */
    public static void main (String args[]) throws Exception {

        // ResourceBundle is normally used for internationalisation of
        // properties files, but provides a convenient one stop shop to
        // locate and load a properties file either from a jar file or the
        // filesystem ... the for loop then goes through the key/value pairs
        // and sets the static class variable with the same name as the key
        // to the loaded value (the variable must already exist)
        ResourceBundle rb = ResourceBundle.getBundle("threepl.ThreePL");
        for (Enumeration<String> keys = rb.getKeys(); keys.hasMoreElements(); ) {
            final String key = keys.nextElement();
            final String value = rb.getString(key);
            ThreePL.class.getDeclaredField(key).set(null, value);
        }

        environment = System.getenv();
        String  sh = environment.get("SHELL");
        if (sh != null)
            shells.add(sh);
        shells.add("/usr/bin/bash");
        shells.add("/bin/bash");

        // standard date format for 3PL
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

        // backwards compatibility - derive "compiler time" from the build date
        compilerTime = dateFormat.parse(buildDate).getTime();

        // Construct long version of version string.
        if (version.endsWith("M"))
            fullVersion = version + " (devel git " + revision + ", " +
                                (revision.endsWith("-dirty") ? lastChangedAuthor : builtBy) + ")";
        else
            fullVersion = version;

        //------------------------------------------------------------------
        // Process call arguments
        //
        //  -C      print clock resolution information
        //  -c      compile only, no execution
        //  -D d=s  define a global directive d whose value is string s
        //  -d      EDIF netlist display window after EDIF code generation
        //  -e      export cell name
        //  -f      force execution regardless of file modification times
	    //  -F      error messages list files as absolute paths
        //  -G      generate gates instead of LUTs
        //  -h      print usage message
        //  -I dir  included directory or directories
        //  -i      intermediate code only, no simulation or code generation
        //  -N name use 'name' as design name instead of deriving from file name
        //  -n      list net identifiers
        //  -O      suppress merging of otherwise suitable CONNECTs and deletion of trailing TDEVars
        //  -o dir  output files placed in this directory
        //  -p      print pre-optimisation TDE list
        //  -R      just print token generator output
        //  -T      print TDE list with TDESIGs
        //  -t      print TDE list without TDESIGs
        //  -l      suppress location in TDE list
        //  -V      print all known 3PL version and Git revision information
        //  -S      run simulator instead of code generation
        //  -s      skip post-processing
        //  -w      give warnings of integer variables truncated on assignment
        //  -X      development mode
        //  -x      suppress netlist code optimisation
        //  -Y      print clock resolution information
        //  file    source file (3pl extension not required)
        //
        global_scope = new Scope();
        directives = new TreeMap<String, Val>();
        initialiseDirectives();

        for (int i=0 ; i<args.length ; i++) {
            if (args[i].startsWith("-I")) {
                String      s = (args[i].length() == 2) ?
                                            args[++i] : args[i].substring(2);
                String[]    dirs = s.split(":");
                for (int j=0 ; j<dirs.length ; j++)
                    if (dirs[j].charAt(dirs[j].length()-1) == '/')
                        include_dirs.add(dirs[j]);
                    else
                        include_dirs.add(dirs[j] + "/");
            } else if (args[i].startsWith("-o")) {
                outdir_opt = (args[i].length() == 2) ? args[++i] : args[i].substring(2);
                outdir_opt += "/";
            } else if (args[i].startsWith("-e")) {
                cell_name = (args[i].length() == 2) ? args[++i] : args[i].substring(2);
            } else if (args[i].startsWith("-N")) {
                cldn = (args[i].length() == 2) ? args[++i] : args[i].substring(2);
            } else if (args[i].startsWith("-D")) {
                String      direc = (args[i].length() == 2) ? args[++i] : args[i].substring(2);
                direc = direc.trim();
                if (direc.indexOf('=') == -1) {
                    addDir(direc, new Val(Boolean.valueOf(true), null), null);
                } else {
                    String[]    s = direc.split("=");
                    long        l;
                    double      d;
                    if (s.length != 2) {
                        msg("-D argument incorrect syntax");
                        System.exit(1);
                    }
                    String  dname = s[0].trim();
                    String  dval = s[1].trim();
                    try {
                        l = Long.parseLong(dval);
                        addDir(dname, new Val(l, null), null);
                    } catch (NumberFormatException el) {
                        try {
                            d = Double.parseDouble(dval);
                            addDir(dname, new Val(d, null), null);
                        } catch (NumberFormatException ed) {
                            if (dval.equals("true"))
                                addDir(dname, new Val(Boolean.valueOf(true), null), null);
                            else if (dval.equals("false"))
                                addDir(dname, new Val(Boolean.valueOf(false), null), null);
                            else {
                                dval = dval.replaceAll("\"", "");
                                addDir(dname, new Val(dval, null), null);
                            }
                        }
                    }
                }
            } else if (args[i].startsWith("-"))
                cloptions.append(args[i].substring(1));
            else if (args[i].startsWith("?"))
                usage();
            else if (source_file != null) {
                System.out.println("more than one source file");
                System.exit(1);
            } else
                source_file = new String(args[i]);
        }
        for (int i=0 ; i<cloptions.length() ; i++) {
            switch (cloptions.charAt(i)) {
            case 'c':
                addDir("compileOnly", true);
                break;
            case 'd':
                addDir("netlistDisplay", true);
                break;
            case 'F':
                addDir("filePath", true);
                break;
            case 'f':
                addDir("forceExec", true);
                break;
            case 'G':
                addDir("gatesNotLUTs", true);
                break;
            case 'i':
                addDir("intermediateOnly", true);
                break;
            case 'l':
                addDir("locations", true);
                break;
            case 'n':
                addDir("listNets", true);
                break;
            case 'O':
                addDir("optimiseConnect", true);
                break;
            case 'p':                
                addDir("listTDEs", true);
                addDir("listTDEsSel", 1);
                break;
            case 'r':
                addDir("rptToFile", true);
                break;
            case 'R':
                readonly = true;
                break;
            case 's':
                addDir("skipPostProc", true);
                break;
            /*case 'S':
                sim = true;
                break;*/
            case 'T':
                addDir("sigList", true);
                addDir("listTDEs", true);
                if (((i+1) < cloptions.length()) && Character.isDigit(cloptions.charAt(i+1))) {
                    addDir("listTDEsSel", new Val(Character.digit(cloptions.charAt(i+1), 10), null), null);
                    i++;
                }
                break;
            case 't':
                addDir("listTDEs", true);
                if (((i+1) < cloptions.length()) && Character.isDigit(cloptions.charAt(i+1))) {
                    addDir("listTDEsSel", new Val(Character.digit(cloptions.charAt(i+1), 10), null), null);
                    i++;
                }
                break;
            case 'V':
                System.out.println("version = " + version);
                System.out.println("revision = " + revision);
                System.out.println("builtBy = " + builtBy);
                System.out.println("buildDate = " + buildDate);
                System.out.println("lastChangedRev = " + lastChangedRev);
                System.out.println("lastChangedAuthor = " + lastChangedAuthor);
                System.out.println("lastChangedDate = " + lastChangedDate);
                System.exit(0);
                break;
            case 'w':
                addDir("intTruncWarning", true);
                break;
            case 'X':
                develop = true;
                addDir("intermediateOnly", true);
                addDir("listTDEs", true);
                break;
            case 'x':
                codeopt = false;
                break;
            case 'Y':
                clockdiag = true;
                break;
            case '?':
            case 'h':
                usage();    // exits
                break;
            default:
                System.out.println("unknown option -" + cloptions.charAt(i));
            }   
        }
        
        //
        // If environment variable THREEPL_INCLUDE_DIRS exists add
        // colon-separated directories to the include_dirs list.
        //
        String  idirs = environment.get("THREEPL_INCLUDE_DIRS");
        if (idirs != null) {
            String      s = (idirs.length() == 2) ? idirs : idirs.substring(2);
            String[]    dirs = s.split(":");
            for (int j=0 ; j<dirs.length ; j++)
                if (dirs[j].charAt(dirs[j].length()-1) == '/')
                    include_dirs.add(dirs[j]);
                else
                    include_dirs.add(dirs[j] + "/");
        }
        
        //------------------------------------------------------------------
        // If 3pl is called with no source file name, start up an interactive
        // file chooser from which the continuation of the main program can
        // be called repeatedly.
        // If there is a source file name, just call the continuation of the main
        // program once and then exit.
        current_directory = new java.io.File(".").getCanonicalPath();
        if (source_file == null) {
            // Start up a file chooser GUI which can repeatedly call
            // mainContinue().
            file_chooser_used = true;
            fc = new JFileChooser(current_directory);
            FileChooser.display();
        } else {
            // Just continue execution - mainContinue().
            file_chooser_used = false;
            int ret = mainContinue();
            if (ret != 0)
                System.exit(ret);
        }
    }

    
    
    //------------------------------------------------------------------
    // A continuation of the main code.
    // A wrapper for mainBody() which closes open files after its
    // execution.
    //
    public static int mainContinue () throws Exception {
        int ret = mainBody();
        if (rptps != null) {
            rptps.flush();
            rptps.close();
        }
        if (iclps != null) {
            iclps.flush();
            iclps.close();
        }
        return(ret);
    }
    
    public static int mainBody () throws Exception {
        msgsb = new StringBuffer();
        if (source_file == null) {
            msg("no source input file");
            if (fc == null) {
                msg("3PL version " + fullVersion);
                usage();
            }
            return(1);
        }
        
        // Build data structures here rather than initialising
        // them when the class is instantiated as the code may
        // be executed more than once.
        files = new ArrayList<String>();
        file_paths = new ArrayList<String>();
        file_dirs = new ArrayList<String>();
        file_depths = new ArrayList<Integer>();
        file_indices = new ArrayList<Integer>();
        groups = new TreeMap<String, Group>();
        modules = new TreeMap<String, Module>();
        procedures = new TreeMap<String, Procedure>();
        functions = new TreeMap<String, Function>();
        inbuilt_mods = new InbuiltMods();
        inbuilt_procs = new InbuiltProcs();
        inbuilt_funcs = new InbuiltFuncs();
        clocks = new HashSet<Clock>();
        clockvarstack = new ArrayList<Clock>();
        leader_modules = new ArrayList<Module>();
        trailer_modules = new ArrayList<Trailer>();
        post_modules = new ArrayList<Module>();
        init_class = new ArrayList<BlockNode>();
        body_stack = new LinkedList<Body>();
        scope_stack = new LinkedList<Scope>();
        class_body_stack = new LinkedList<Body>();
        call_name_stack = new LinkedList<String>();
        call_loc_stack = new LinkedList<SrcLoc>();
        var_queue = new LinkedList<Var>();
        execList = new ArrayList<ExecPair>();

        // Initialise class variables on each execution.
        line_no = 1;// current line number
        prev_line_no = 1;
        warnings = 0;
        tdelist = new TDEList(2000);
        rptps = null;
        iclps = null;
        errors = 0;
        family = null;
        tdelist_optimisation_started = false;
        tdelist_optimisation_finished = false;
        creating_variables = false;
        isparsing = true;
        postProcessing = false;
        top_scope_skip = false;
        oack_use_lut = true;
        as_fifo_read_use_lut = true;
        as_fifo_empty_use_lut = true;
        add_sub_use_lut = true;
        ge_use_lut = true;
        mux_use_lut = false; // might optimise better if LUT not used
        incrdecr_use_lut = true;
        cpld_use_lut = false;   // always false - CPLDs seem to ignore LUTs!
        fifos_ok = false;
        queuereg_ok = false;
        if (file_chooser_used) {
            global_scope = new Scope();
            directives = new TreeMap<String, Val>();
            initialiseDirectives();
            
            readonly = false;
            varerrors = false;
            continuous = false;
            ALU = false;
            fifos_ok = false;
            addDir("skipPostProc", fc_skip_post_processing);
            addDir("forceExec", fc_force_execution);
            if (fc_list_tdes) {
                addDir("listTDEs", true);
                addDir("rptToFile", true);
            }
            if (fc_report)
                addDir("rptToFile", true);
            if (fc_list_nets)
                    addDir("listNets", true);
        }
        
        // initialise static variables in other classes.
        TDEVar.init();
        TDEList.init();
        Element.init();
        Net.init();
        startval_tdev = TDEVar.VCC; // default
        
        // If called with no file name extension on input file (as usual)
        // add the ".3pl" extension.
        if (!source_file.endsWith(".3pl"))
            source_file = source_file + ".3pl";
        
        // Change the input file name to absolute.
        File    f = new File(source_file);
        source_file = f.getAbsolutePath();
        
        // Get the absolute source file path minus extension.
        String  smext;
        smext = source_file.substring(0, source_file.length()-4);
        
        // Extract the design name and parent directory from the
        // absolute file name minus extension.
        // Construct a copy of the file name minus the directory path.
        int     p = smext.lastIndexOf("/"); // Unix, Linux, OSX
        if (p == -1)
            p = smext.lastIndexOf("\\");    // Windows
        
        parent_directory = smext.substring(0, p+1);
        design_name = cleanEDIFIdent(smext.substring(p+1));

	// Create output directory if specified.
        if  (outdir_opt == null)
            outdir = parent_directory;
        else if (!outdir_opt.startsWith("/"))
            outdir = parent_directory + outdir_opt;

        File    outputDirectory = new File(outdir);
        if (!outputDirectory.exists())
            if (!outputDirectory.mkdirs()) {
                emsg("Cannot create output directory '" + outdir);
                return(1);
            }

        // Change design name if -N option used.
        if (cldn != null)
            design_name = cleanEDIFIdent(cldn);
        
        if (cell_name != null) {
	    cell_name = cleanEDIFIdent(cell_name);
            netlist_file = outdir + cell_name + ".edn";
	} else
            netlist_file = outdir + design_name + ".edn";
        
        net_file = outdir + design_name + ".net";
        report_file = outdir + design_name + ".rpt";
        icl_file = outdir + design_name + ".icl";

        // If a report file is required (-r option), create a PrintWriter
        // for it.
        if (boolDir("rptToFile")) {
            File        rptfile;
            rptfile = new File(report_file);
            try {
                rptps = new PrintStream(rptfile);
            } catch(IOException e) {
                emsg("Cannot create report file '" + report_file);
                return(1);
            }
        }

        // If an intermediate code list file is required (-t option), create a PrintWriter
        // for it.
        if (boolDir("listTDEs")) {
            File        iclfile;
            iclfile = new File(icl_file);
            try {
                iclps = new PrintStream(iclfile);
            } catch(IOException e) {
                emsg("Cannot create intermediate code list file '" + icl_file);
                return(1);
            }
        }

        // get the current date/time.
        currentTime = System.currentTimeMillis();
        currentDate = dateFormat.format(new Date(currentTime));

        // Create some global directives which can be used to control immediate execution and
        // which also appear as comments in some output files.
        addDir("version", new Val(fullVersion, null), null);
        addDir("compilerMakeDate", new Val(buildDate, null), null);
        addDir("date", currentDate);
        addDir("OS", new Val(os_name, null), null);
        addDir("arch", new Val(arch_name, null), null);
        addDir("sourceFile", new Val(design_name + ".3pl", null), null);
        addDir("currentDirectory", new Val(current_directory, null), null);
        addDir("parentDirectory", new Val(parent_directory, null), null);        
        addDir("outputDirectory", new Val(outdir, null), null);        
        addDir("designName", new Val(design_name, null), null);
        if (cell_name != null)
            addDir("cellName", new Val(cell_name, null), null);
        
        // Get the millisecond time the previous netlist file was made.
        // Time is 0 if no file.
        addDir("netlistFile", new Val(netlist_file, null), null);
        File file = new File(netlist_file);
        netlistTime = file.lastModified();
        //String netlistDate = dateFormat.format(new Date(netlistTime));
        //msg("prev netlist file time " + netlistDate);

        // Get the millisecond time the previous report file was made.
        // Time is 0 if no file.
        addDir("reportFile", new Val(report_file, null), null);
        File rptf = new File(report_file);
        reportTime = rptf.lastModified();
        //String reportDate = dateFormat.format(new Date(reportTime));
        //msg("prev report file time "+ reportDate );

        // Get the millisecond time the previous net file was made.
        // Time is 0 if no file.
        addDir("netFile", new Val(net_file, null), null);
        File netf = new File(net_file);
        netTime = netf.lastModified();
        //String netDate = dateFormat.format(new Date(netTime));
        //msg("prev net file time " + netDate);
        
        // Create defined mathematical constants.
        Ident   iii;
        Type    ttt = new Type("float", false, false, null);
        Var     vvv;        
        iii = new Ident("math_pi", Context.GLOBAL);
        vvv = new Immediate(iii, ttt, new Val(Math.PI, null), false, false, null);
        addVar(vvv, Context.GLOBAL, null);
        vvv.setReadOnly(null);
        iii = new Ident("math_e", Context.GLOBAL);
        vvv = new Immediate(iii, ttt, new Val(Math.E, null), false, false, null);
        addVar(vvv, Context.GLOBAL, null);
        vvv.setReadOnly(null);
        
        // Create global files stdin, stdout, stderr and rpt.
        Immediate ivar = null;
        iii = new Ident("stdin", Context.GLOBAL);
        stdinOpenFile = new FileDesc(System.in);
        ivar = new Immediate(iii, new Type(Ptype.FILE, 0), new Val(stdinOpenFile, null), false, false, null);
        addVar(ivar, Context.GLOBAL, null);
        iii = new Ident("stdout", Context.GLOBAL);
        stdoutOpenFile = new FileDesc(System.out);
        ivar = new Immediate(iii, new Type(Ptype.FILE, 0), new Val(stdoutOpenFile, null), false, false, null);
        addVar(ivar, Context.GLOBAL, null);
        iii = new Ident("stderr", Context.GLOBAL);
        stderrOpenFile = new FileDesc(System.err);
        ivar = new Immediate(iii, new Type(Ptype.FILE, 0), new Val(stderrOpenFile, null), false, false, null);
        addVar(ivar, Context.GLOBAL, null);
        iii = new Ident("rpt", Context.GLOBAL);
        if (rptps != null)
            ivar = new Immediate(iii, new Type(Ptype.FILE, 0), new Val(new FileDesc(rptps), null), false, false, null);
        else
            ivar = new Immediate(iii, new Type(Ptype.FILE, 0), new Val(new FileDesc(System.out), null), false, false, null);
        addVar(ivar, Context.GLOBAL, null);

        //----------------------------------------------------------------------------
        // List all directive initial values to report file and intermediate code list
        // file if these are active.
        //
        printDirectives("Directives - initial values.\n----------------------------\n");

        //------------------------------------------------------------------
        // Parse the 3PL source code.
        //
        int pret = parse();
        if (pret != 0)
            return(pret);
        if (boolDir("compileOnly"))
            return(0);
        //------------------------------------------------------------------
        // Execute (interpret) the code tree.
        //
        int eret = execute();
        switch (eret) {
        case 1:         // OK, but execution skipped on file ages -
            return(0);  // return OK
        case 2:         // error -
            return(2);  // return error
        default:        // OK -
            break;      // keep going
        }

        //--------------------------------------------------------------------------
        // List all directive final values to report file and intermediate code list
        // file if these are active.
        //
        printDirectives("Directives - final values.\n-------------------------\n");
        
        nanotime2 = System.nanoTime();  // end time of execution
        
        //------------------------------------------------------------------
        // If no target code just return as nothing more to do.
        //
        if (tdelist.size() == 0) {
            nanotime6 = System.nanoTime();  // final time
            double  parsetime    = (nanotime1 - nanotime0) / 1.0e9;
            double  exectime     = (nanotime2 - nanotime1) / 1.0e9;
            double  elapsedtime  = (nanotime6 - nanotime0) / 1.0e9;
            String  pt   = String.format("Parse time                               %1$8.3fs", parsetime);
            String  et   = String.format("Immediate execution time                 %1$8.3fs", exectime);
            String  tt   = String.format("Total elapsed time                       %1$8.3fs", elapsedtime);
            rpt("");
            rpt("No target code - exiting");
            rpt(pt);
            rpt(et);
            rpt(tt);
            return(0);
        }

        //------------------------------------------------------------------
        // Optional print TDE list prior to optimisation.
        //
        if (boolDir("listTDEs") && (longDir("listTDEsSel") == 1)) {
            tdelist.trimTDEListVariables();
            tdelist.dump(boolDir("locations"));
        }
        
        //------------------------------------------------------------------
        // Optimise the TDE list.
        // Optionally print the TDE list after optimisation.
        //
        int oret = optimiseTDEList ();
        if (oret != 0)
            return(oret);
        nanotime3 = System.nanoTime();  // end of TDE optimisation time
        
        if (boolDir("intermediateOnly")) {
            double  parsetime    = (nanotime1 - nanotime0) / 1.0e9;
            double  exectime     = (nanotime2 - nanotime1) / 1.0e9;
            double  tdeoptimtime = (nanotime3 - nanotime2) / 1.0e9;
            double  elapsedtime  = (nanotime6 - nanotime0) / 1.0e9;
            String  pt   = String.format("Parse time                               %1$8.3fs", parsetime);
            String  et   = String.format("Immediate execution time                 %1$8.3fs", exectime);
            String  icot = String.format("Intermediate code list optimisation time %1$8.3fs", tdeoptimtime);
            String  tt   = String.format("Total elapsed time                       %1$8.3fs", elapsedtime);
            rpt("");
            rpt("Immediate execution only - exiting");
            rpt(pt);
            rpt(et);
            rpt(icot);
            rpt(tt);
            return(0);
        }

        //------------------------------------------------------------------
        // Run simulator if implemented and if requested
        //
//        if (sim_implemented && sim) {
//            Sim s = new Sim(design_name, tdelist);
//            s.start("cli");
//            return(0);
//        }
        
        //------------------------------------------------------------------
        // Generate EDIF netlist.
        //
        int gret = generateNetlist();
        if (gret != 0)
            return(gret);
        
        //------------------------------------------------------------------
        // Output netlist constraints file.
        //
        writeConstraintsFile();

        //------------------------------------------------------------------
        // optionally print list of netlist identifiers
        //
        if (boolDir("listNets")) {
            File   nlfile = null;
            FileWriter  nlfw;
            PrintWriter nlpw = null;
            String netfile = outdir + ((cell_name != null) ? cell_name : design_name) + ".net";
            nlfile = new File(netfile);
            try {
                nlfw = new FileWriter(nlfile);
                nlpw    = new PrintWriter(nlfw);
            } catch(IOException e) {
                System.out.println("Cannot create output file '" + netfile);
            }
            rpt("\twriting list of equivalent nets");
            Net.outputNets(nlpw);
            nlpw.flush();
            nlpw.close();
        }
        
        //------------------------------------------------------------------
        // Print element statistics to report file
        //
        Element.printElementCount();
        
        //------------------------------------------------------------------
        // Optionally start the interactive netlist display GUI.
        //
        if (boolDir("netlistDisplay")) {
            // start the interactive GUI netlist display
            msg("Starting netlist display.");
            Display.display();
        
        }

        //------------------------------------------------------------------
        // Optionally report CPU times and memory usage.
        //
        nanotime6 = System.nanoTime();  // finish time
        double  parsetime    = (nanotime1 - nanotime0) / 1.0e9;
        double  exectime     = (nanotime2 - nanotime1) / 1.0e9;
        double  tdeoptimtime = (nanotime3 - nanotime2) / 1.0e9;
        double  netgentime   = (nanotime4 - nanotime3) / 1.0e9;
        double  netoptimtime = (nanotime5 - nanotime4) / 1.0e9;
        double  netouttime   = (nanotime6 - nanotime5) / 1.0e9;
        double  elapsedtime  = (nanotime6 - nanotime0) / 1.0e9;
        String  pt   = String.format("Parse time                               %1$8.3fs", parsetime);
        String  et   = String.format("Immediate execution time                 %1$8.3fs", exectime);
        String  icot = String.format("Intermediate code list optimisation time %1$8.3fs", tdeoptimtime);
        String  ngt  = String.format("Netlist generation time                  %1$8.3fs", netgentime);
        String  nopt = String.format("Netlist optimisation time                %1$8.3fs", netoptimtime);
        String  nout = String.format("Netlist output time                      %1$8.3fs", netouttime);
        String  tt   = String.format("Total elapsed time                       %1$8.3fs", elapsedtime);
        rpt("");
        rpt(pt);
        rpt(et);
        rpt(icot);
        rpt(ngt);
        rpt(nopt);
        rpt(nout);
        rpt(tt);
            
        // Report memory usage.
        rpt("Memory usage: max " + main_rt.maxMemory() + " total " + main_rt.totalMemory() + " free " + main_rt.freeMemory());
        
        //------------------------------------------------------------------
        // Execute any post-processing modules.
        //
        int ppret = 0;
        if (boolDir("skipPostProc"))
            msg("-s option - skipping post-processing");
        else
            ppret = postProcess();
        
        return(ppret);
    }
    
    /**
     * Initialise directives.
     */
    private static void initialiseDirectives () {
        addDir("compileOnly", false);
        addDir("forceExec", false);
        addDir("sigList", false);
        addDir("listNets", false);
        addDir("netlistDisplay", false);
        addDir("filePath", false);
        addDir("listTDEs", false);
        addDir("rptToFile", false);
        addDir("intTruncWarning", false);
        addDir("skipPostProc", false);
        addDir("intermediateOnly", false);
        addDir("gatesNotLuts", false);
        addDir("locations", false);
        addDir("optimiseConnect", true);
        addDir("listTDEsSel", 0);
        addDir("ALU", false);
        addDir("continuous", false);
        addDir("FIFO", false);
        addDir("QUEUEREG", false);
        addDir("unassOut", "fatal");
        addDir("postProcessTool", "ise");
    }
 

    /**
     * Parse the 3PL program.
     * @return 1 on a parse error, 0 otherwise
     * @throws IOException on an IO error
     */
    private static int parse () throws IOException {
        //------------------------------------------------------------------
        // Parse the 3PL source code.
        //
        //------------------------------------------------------------------
        rpt("Memory requested " + main_rt.maxMemory() + " bytes");
        if (readonly) {
            // This is only used for debugging the compiler. It outputs tokens.
            //
            char[]  rbuf = new char[200];
            int     rindex = 0;
            
            try {
                input_stream = new SrcReader(source_file);
            } catch (FileNotFoundException e) {
                msg("unable to open source file '" + source_file + "'");
                return(1);
            }                          
            while (input_stream.read(rbuf, rindex, 1) >= 0) {
                if (rbuf[rindex] == '\n') {
                    System.out.println(new String(rbuf, 0, rindex));
                    rindex = 0;
                } else
                    rindex++;
            }
            return(0);
        }
        rpt("3PL version " + fullVersion + ".");
        rpt("Source file " + source_file);
        rpt("command line options - " + cloptions);
        rpt(currentDate);
        rpt("\nParsing source.");
        rpt("---------------\n");
        if (boolDir("listTDEs")) {
            icl("3PL version " + fullVersion + ".");
            icl("Source file " + source_file);
            icl("command line options - " + cloptions);
            icl(currentDate);
        }
        try {
            input_stream = new SrcReader(source_file);
        } catch (FileNotFoundException e) {
            msg(source_file + ": Unable to open source file");
            return(1);
        }
        try {
            if (parser == null)
                parser = new Parser(input_stream); // first use
            else
                Parser.ReInit(input_stream);        // subsequent uses
            main = Parser.Filemodule().getInlineModule();
        } catch (ParseException e) {
            msg(file_path + ":" + line_no + ":" + e.currentToken.next.beginColumn +
                ": " + e.getMessage());
            msg(design_name + ": Parse error - execution omitted");
            return(1);
        } catch (TokenMgrError e) {
            msg(file_path + ":" + line_no + ":" + e.column +
                ": " + e.getMessage());
            msg(design_name + ": Parse error - execution omitted");
            return(1);
        } catch (ExEx e) {
            msg(e.getMessage());
            msg(design_name + ": Parse error - execution omitted");
            return(1);
        }
        input_stream.close();
        if (warnings > 0)
            msg(design_name + ": " + warnings + " warnings");
        if (errors > 0) {
            msg(design_name + ": " + errors + " errors - execution omitted");
            if (boolDir("rptToFile")) {
                rpt(errors + " errors - execution omitted");
                rptps.flush();
                rptps.close();
            }
            return(1);
        }
        
        isparsing = false;
        
        // Print a list of files read by the compiler to the report file and
        // the intermediate code list file.
        rpt("\n    source files");
        rpt("    ------------\n");
        icl("\nsource files");
        icl("------------\n");
        for (int i=0 ; i< files.size() ; i++) {
            StringBuffer    sb = new StringBuffer();
            int indent = file_depths.get(i);
            int index = file_indices.get(i);
            for (int j=0 ; j<indent ; j++)
                sb.append("    ");
            if (i == 0)
                sb.append(file_dirs.get(i) + "   " + files.get(i));
            else if (index < 0)
                sb.append("SRC      " + file_dirs.get(i) + "   " + files.get(i));
            else
                sb.append("FMOD" + index + "    " + file_dirs.get(i) + "   " + files.get(i));
            rpt(sb.toString());
            icl(sb.toString());
        }
        rpt("");
        icl("");

        return(0);
    }
    

    /**
     * Execute the 3PL code tree except for the post-processing section.
     * @return 0 for correct execution, 1 for execution skipped or 2 for execution error
     */
   private static int execute () {
        // Check file times to see if execution necessary.
       //String sourceDate = dateFormat.format(new Date(sourceTime));
       //msg("source time " + sourceDate);
       //msg("compiler time " + compilerDate);

        boolean netlist_ok = sourceTime < netlistTime;
        boolean build_ok = compilerTime < netlistTime;
        boolean report_ok = !boolDir("rptToFile") || sourceTime < reportTime;
        boolean net_ok = !boolDir("listNets") || sourceTime < netTime;
        if (!boolDir("forceExec")) {
            if (netlist_ok && build_ok && report_ok && net_ok) {
                msg("files up-to-date - skipping immediate execution");
                // Execute any post-processing modules unless skip option given.
                if (!boolDir("skipPostProc"))
                    postProcess();
                else {
                    msg("-s option - skipping post-processing");
                    return(1);
                }
            }
            
            if (!netlist_ok)
                msg("netlist file out-of-date");
            if (!build_ok)
                msg("newer compiler build");
            if (!report_ok)
                msg("report file out-of-date");
            if (!net_ok)
                msg("net file out-of-date");
            msg("immediate execution");
        }
        nanotime1 = System.nanoTime();  // start of execution
        try {
            rpt("\nImmediate execution.");
            rpt("--------------------\n");
            
            // Execute any leader modules.
            rpt("\nExecuting leader modules.");
            rpt("-------------------------\n");
            
            for (Module m: leader_modules) {
                Scope   s = m.getFileScope();
                pushScope(s, null);
                String  mm = "module " + m.ident + " order " + m.order;
                rpt("\t" + mm);
                rpt("\t" + Functions.concat("-", mm.length()) + "\n");
                m.execute(null, null, null);
                popScope();
            }
            rpt("\n");

            // execute main file module
            rpt("\nExecuting main module.");
            rpt("----------------------\n");
            
            main.execute(null,  null,  null);
            //scopePop();
            rpt("\n");

            // Execute any trailer modules.
            rpt("\nExecuting trailer modules.");
            rpt("--------------------------\n");
            for (Trailer t : trailer_modules) {
                Scope   s = t.scope;
                Module  m = t.module;
                if (s != null) {
                    // Is a trailermodule in a 3PL class.
                    // Call using the class scope.
                    //
                    Group   g = (Group)m.getEnclosing();
                    Scope   fs = m.getFileScope();
                    pushScope(fs, null);
                    pushClassBody(g);
                    String  mm = "class " + g.getName() + " order " + m.order;
                    rpt("\t" + mm);
                    rpt("\t" + Functions.concat("-", mm.length()) + "\n");
                    m.execute(null, null, null);
                    popScope();
                } else {
                    // Is a trailermodule NOT in a 3PL class.
                    //
                    Scope   fs = m.getFileScope();
                    pushScope(fs, null);
                    String  mm = "module " + m.ident + " order " + m.order;
                    rpt("\t" + mm);
                    rpt("\t" + Functions.concat("-", mm.length()) + "\n");
                    m.execute(null, null, null);
                    popScope();
                }
            }
            
            rpt("\n");
        } catch (ExEx e) {
            msg(e.getMessage());
            msg(stackTrace());
            msg(design_name + ": Execution error - code generation omitted");
            return(2);
        }
        
        // Connect up any function used values or combinatorial memory read
        // or write values with associated execution
        // signals - these are required by instances of inbuilt function
        // used() or Memory/memRead() or Memory/memWrite().
        for (ExecPair ep: execList) {
            Val                 val = ep.val;
            TDEVar              tdev = val.getTDEVar();
            HashSet<TDEVar>     hs = ep.execs;
            Iterator<TDEVar>    hsit = hs.iterator();
            TDEVar              exec;
            switch (hs.size()) {
            case 0:
                //continue;
                emsg("function used() or memory read or write called in illegal context", tdev.getSrcLoc());
                return(2);
            case 1:
                exec = hsit.next();
                tdelist.connect(tdev, exec);
                continue;
            default:
                TDE     or = new TDE(TDEType.OR);
                while (hsit.hasNext()) {
                    exec = hsit.next();
                    or.add2i(exec);
                }
                tdelist.connect(tdev, or.finish());
            }
        }
        
        // Determine some netlist generation control parameters that may have been set via
        // directives, using defaults otherwise.
        // dsp_threshold - threshold for static mode variable width at or above which it will be
        //                  implemented using the output register of a DSP block instead of flip-flops.
        //                  this has proved to be counter-productive and is not used. For this reason it
        //                  is not documented in the manual. There is an equivalent flag to use on a
        //                  per-instance basis but it also has not proved useful. Note that using a DSP
        //                  output register has much longer clock-to-output time than a CLB flip-flop
        // fifos_ok - use inbuilt block RAM FIFO hardware where available
        // queuereg_ok - add an extra register to queue outputs to improve timing without adding latency (
        //              can also be controlled per instance).
        creating_variables = true;
        /*
        if (dto != null) {
            if (dto.getPrimType() != Ptype.UINT)
                msg("directive \"dspThreshold\" is not type int - ignored!");
            else {
                Long  l = dto.getSingleIval(null);
                dsp_threshold = l;
            }
        }
        */
        Val  ufo = findDir("FIFO");
        if (ufo != null) {
            if (ufo.getPrimType() != Ptype.LOG)
                msg("directive \"FIFO\" is not type log - ignored!");
            else {
                Boolean  b = ufo.getSingleLval(null);
                fifos_ok = b;
            }
        }
        Val  rpo = findDir("QUEUEREG");
        if (rpo != null) {
            if (rpo.getPrimType() != Ptype.LOG)
                msg("directive \"QUEUEREG\" is not type log - ignored!");
            else {
                Boolean  b = rpo.getSingleLval(null);
                queuereg_ok = b;
            }
        }
        
        rpt("\nCreating target-mode variable logic.");
        rpt("------------------------------------\n");
        
        // Create all target variables except clocks
        try {
            for (Var vq: var_queue) {
                if (vq.getMode() != Mode.CLOCK)
                    vq.createVar();
            }
            creating_variables = false;
            
        } catch (ExEx e) {
            String  s = e.getMessage();
            msg(s);
            msg("Variable finalisation error - code generation omitted");
            icl(s);
            return(2);
        }
        
        if (rptps != null) {
            // If report file is being produced, list the queue variables that
            // have multiple divergent sinks (destinations).
            rpt("\nQueue variables with multiple sink modules");
            rpt("------------------------------------------\n");
            for (Var vq: var_queue) {
                if (vq.getMode() == Mode.QUEUE) {
                    Queue q = (Queue)vq;
                    long    n = q.getSinks();
                    if (n != 0) {
                        String  id = q.getID(IDtype.CHAIN);
                        SrcLoc  loc = q.getLoc();
                        rpt("\t" + id);
                        rpt("\t\t" + loc + " " + n + " sinks");
                    }
                }
            }
            rpt("\n");
            
            // If report file is being produced, list the clock variables that
            // drive logic and those that do not.
            ArrayList<Clock>    alv = new ArrayList<Clock>();
            ArrayList<Clock>    alnv = new ArrayList<Clock>();
            for (Var vq: var_queue) {
                if (vq.getMode() == Mode.CLOCK) {
                    Clock   c = (Clock)vq;
                    if (c.isIndirect())
                        continue;
                    if (c.getValid())
                        alv.add(c);
                    else
                        alnv.add(c);
                }
            }
            rpt("\nClock variables driving clocked logic.");
            rpt("-------------------------------------\n");
            for (Clock c : alv)
                rpt("\t" + c.getID(IDtype.SLITERAL));
            rpt("\nClock variables not driving clocked logic.");
            rpt("------------------------------------------\n");
            for (Clock c : alnv)
                rpt("\t" + c.getID(IDtype.SLITERAL));
            rpt("\n");
        }
        
        // Check if any errors were encountered.
        if (errors > 0) {
            if (boolDir("listTDEs"))
                tdelist.dump(boolDir("locations"));  // diagnostic dump
            msg(errors + " errors - code generation omitted");
            return(2);
        }
        return(0);
    }

    /**
     * Optimise the TDE list intermediate code.
     * @return 1 for an error, 0 otherwise
     */
    private static int optimiseTDEList () {
        tdelist_optimisation_started = true;
        
        // Merge duplicate entries in the TDEVar catalog.
        //TDEVar.resolveCatalog();
        
        //------------------------------------------------------------------
        // Traverse the TDE list finding all DELs which terminate EXECPs
        // which wait on asynchronous unbuffered queues and change the delay
        // parameter from 1 to 2.
        tdelist.resolveDELs();

        // Optimise the TDE list.
        //
        rpt("Optimising intermediate code.");
        rpt("-----------------------------\n");

        Val  conto = findDir("continuous");
        if (conto != null) {
            if (conto.getPrimType() != Ptype.LOG)
                rpt("\tdirective 'continuous' is not type log - ignored");
            else
                continuous = conto.getSingleLval(null);
            if (continuous)
                rpt("\t'continuous' directive true");
        }

        // Resolve connected source code nets where possible.
        /*if (resolve)
            tdelist.netresolve();*/
        
        // Perform generic optimisation of the signaling logic in the TDE list.
        // Continue passes until no changes occur.
        //
        boolean changes = false;
        int     opasses = 0;
        tdelist.linkCheck(); // use if optimisation problems
        do {
            // tdelist.linkCheck(); use if optimisation problems
            changes = tdelist.optimiseTDEList(continuous);
            opasses++;
        } while (changes);
        rpt("\toptimisation passes: " + opasses);
        tdelist.trimTDEListVariables();
        tdelist.linkCheck(); // use if optimisation problems
        
        // Optional diagnostic dump after 1st general optimisation.
        if (boolDir("listTDEs") && (longDir("listTDEsSel") == 2)) {
            tdelist.trimTDEListVariables();
            tdelist.dump(boolDir("locations"));
        }
        
        // Perform some platform-specific optimisations on the TDE list.
        //
        rpt("\tPlatform-specific optimisation pass.");
        if (family == null) {
            rpt("Device type not specified. Code generation skipped.");
            msg("Device type not specified. Code generation skipped.");
            return(1);
        }
        family.optimiseTDEs();
        
        // Optional diagnostic dump after family-specific optimisation.
        if (boolDir("listTDEs") && (longDir("listTDEsSel") == 3)) {
            tdelist.trimTDEListVariables();
            tdelist.dump(boolDir("locations"));
        }

        // Do more generic optimisation passes to cleanup after platform-specific
        // optimisation.
        rpt("\tIntermediate code cleanup optimisation.");
        opasses = 0;
        do {
            changes = tdelist.optimiseTDEList(continuous);
            opasses++;
        } while (changes);
        rpt("\tcleanup optimisation passes: " + opasses);

        // Create all clock variables
        rpt("\n\tclock variables - \n");
        try {
            creating_variables = true;
            Iterator<Var>    vit = var_queue.iterator();
            while (vit.hasNext()) {
                Var v = (vit.next());
                if (v.getMode() == Mode.CLOCK)
                    v.createVar();
            }
            creating_variables = false;
        } catch (ExEx e) {
            msg(e.getMessage());
            msg(stackTrace());
            msg(design_name + ": Execution error");
            rpt("");
            rpt("Execution error");
            rpt(e.getMessage());
            rpt(stackTrace());
            return(1);
        }
        
        //------------------------------------------------------------------
        // Check the logic (an overstatement! - it only does one very minor
        // check for something that I think has never happened).
        //
        tdelist.logicCheck();
        
        tdelist_optimisation_started = false;
        tdelist_optimisation_finished = true;
        
        //------------------------------------------------------------------
        // Optionally list intermediate code.
        //
        if (boolDir("listTDEs")) {
            rpt("\nPrinting TDE list to .icl file");
            rpt("------------------------------\n");
            addDir("listTDEsSel",  0);
            tdelist.dump(boolDir("locations"));
        }

        if (varerrors) {
            msg("Code generation omitted due to variable attribute errors!");
            rpt("Code generation omitted due to variable attribute errors!");
            return(1);
        }
        return(0);
    }  
    

    /**
     * Generate the EDIF netlist from the TDE list intermediate code.
     * @return 1 for an error, 0 otherwise
     */
    private static int generateNetlist () {
        Val  luto = findDir("gatesNotLUTs");
        if (luto != null) {
            if (luto.getPrimType() != Ptype.LOG) {
                rpt("directive 'gatesNotLUTs' is not type log");
                return(1);
            }
            gates_not_luts = luto.getSingleLval(null);
            if (gates_not_luts)
                rpt("'gatesnotluts' directive ");
        } else if (gates_not_luts)
            rpt("'gates not luts' option (-G)");
        if (gates_not_luts) {
            oack_use_lut = false;
            as_fifo_read_use_lut = false;
            as_fifo_empty_use_lut = false;
            add_sub_use_lut = false;
            ge_use_lut = false;
            mux_use_lut = false;
            incrdecr_use_lut = false;
            cpld_use_lut = false; // always false anyway - CPLDs seem to ignore LUTs!
        }

        try {
            rpt("Generating EDIF code.");
            rpt("---------------------\n");
            
            tdelist.ncode();
        } catch (ExEx e) {
            String  em = e.getMessage();
            msg(em);
            msg("Code generation terminated due to error(s)");
            rpt(em);
            rpt("Code generation terminated due to error(s)");
            return(1);
        }
        return(0);
    }
    

    /**
     * Generate an ISE NCF constraints file or a Vivado XDC constraints file.
     */
    private static void writeConstraintsFile () {
        String      author = null;
        String      netlistcomment = null;
        Val         val;

        val = findDir("author");
        if (val != null) {
            if (val.getPrimType() != Ptype.STR)
                rpt("directive \"author\" is not type str - ignored!"); 
            else
                author = val.getSingleSval(null);
        }     

        val = findDir("netlistComment");
        if (val != null) {
            if (val.getPrimType() != Ptype.STR)
                msg("directive \"netlistComment\" is not type str - ignored!");   
            else
                netlistcomment = val.getSingleSval(null);
        }   

        String[] netlistcomments = null;
        if (netlistcomment != null)
            netlistcomments = netlistcomment.split("\n");
        
        // generate any netlist constraints file(s)
        //
        String      tool = stringDir("postProcessTool");
        String      filename = outdir + ((cell_name != null) ? cell_name : design_name) + (tool.equals("ise") ? ".ncf" : ".xdc");
        
        File        cfile;
        FileWriter  cfw;
        PrintWriter cpw = null;
        cfile = new File(filename);
        try {
            cfw = new FileWriter(cfile);
            cpw = new PrintWriter(cfw);
        } catch(IOException e) {
            System.out.println("Cannot create output file '" + filename);
        }
        rpt("\twriting " + tool + " constraints file " + filename);
        family.outputConstraints(cpw, author, netlistcomments);
        cpw.flush();
        cpw.close();
    }

    /**
     * Execute any post-processing modules.
     * @return 1 for an error, 0 otherwise
     */
    private static int postProcess () {
        postProcessing = true;
        try {
            rpt("Execute post-processing modules.");
            for (Module m: post_modules) {
                Scope   s = m.getFileScope();
                pushScope(s, null);
                rpt("\t" + m.ident + " (" + m.order + ")");
                m.execute(null, null, null);
            }
            post_modules = null;
        } catch (ExEx e) {
            msg(e.getMessage());
            msg(stackTrace());
            msg("Post-processing code error");
            return(1);
        }
        return(0);
    }

    /**
     * Print a usage message and exit
     */
    private static void usage () {
        System.out.println("3PL - CSIRO Australia");
        System.out.println("Commonwealth Scientific and Industrial Research Organisation");
        System.out.println("Usage: 3pl {-Iincludes} {-odirname} {-Dirname} {-Ndesname} {-dilptv} name");
        System.out.println("Options and 'name' may be in any order.");
        System.out.println("-c       - compile only - do not execute");
        System.out.println("-D d=s   - define a global directive d whose value is string s");
        System.out.println("-d       - display EDIF netlist window");
        System.out.println("-f       - force execution regardless of file modification times");
        System.out.println("-F       - error messages list files as absolute paths");
        System.out.println("-G       - generate gates instead of LUTs");
        System.out.println("-h       - print this usage message");
        System.out.println("-I       - 'includes' is a \":\"-separated list of include directories");
        System.out.println("-l       - include location comments in TDE list");
        System.out.println("-N dname - supplies a design name 'dname' instead of deriving it from the file name");
        System.out.println("-n       - list net equivalent identifiers");
        System.out.println("-o       - specifies that all output files are to be placed in directory 'dirname'");
        System.out.println("-r       - print a report file to name.rpt");
        //System.out.println("-S       - run the simulator instead of generating code");
        System.out.println("-s       - skip post-processing");
        System.out.println("-t       - print the TDE list without signal declaration TDEs");
        System.out.println("-V       - print all known 3PL version and Git revision information");
        System.out.println("Except for -I, multiple options may be combined in one string.");
        System.out.println("There may be multiple -I options.");
        System.out.println("-I may be separated from its list by spaces.");
        System.out.println("-o may be separated from its directory name by spaces.");
        System.out.println("-D may be separated from the directive assignment by spaces.");
        System.out.println("-N may be separated from its design name by spaces.");
        System.out.println("-t may be immediately folowed by a single digit -.");
        System.out.println("              1 also list TDEs prior to optimisation");
        System.out.println("              2 also list TDEs after 1st optimisation");
        System.out.println("              3 also list TDEs after family-specific optimisation");
        System.out.println("'name' is the source file name - the .3pl extension is accepted but not required.");
        System.out.println("\n\nThe following options are for 3PL testing only -");
        System.out.println("-i       - intermediate code only, no simulation or code generation");
        System.out.println("-O       - suppress merging of suitable CONNECT TDE signals");
        System.out.println("-p       - print the TDE list before optimisation");
        System.out.println("-R       - just print token generator output");
        System.out.println("-T       - print the TDE list with signal declaration TDEs");
        System.out.println("-w       - print a warning for any target int or uint truncated on assignment");
        System.out.println("-X       - development mode");
        System.out.println("-x       - suppress netlist code optimisation");
        System.out.println("-Y       - print clock resolution information");
        System.exit(1);
    }
    
    /**
     * Null out variables no longer needed so garbage collection
     * can regain some memory. Called in TDEList.ncode().
     * 
     * POST PROCESSING HAS BEEN ADDED, SO THESE VARIABLES CAN NO LONGER
     * BE CLEARED PRIOR TO NETLIST GENERATION!
     */
    public static void clear_parse_and_exec_vars () {
        /*
        modules = null;
        procedures = null;
        functions = null;
        inbuilt_mods = null;
        inbuilt_procs = null;
        inbuilt_funcs = null;
        leader_modules = null;
        trailer_modules = null;
        main = null;
        body_stack = null;
        scope_stack = null;
        call_loc_stack = null;
        global_scope = null;
        clocks = null;
        clockvarstack = null;
        var_queue = null;
        func_used = null;
        */
    }
    
    public static void logText (String s) {
        msgsb.append(s + "\n");
    }
    
    /**
     * Print a message.
     * If GUI is being used, log message to it, otherwise print to standard output.
     * If a report file has been specified, print to that as well.
     * @param   s is the string to be printed
     */
    public static void msg (String s) {
        if (fc != null)
            logText(s);
        else
            System.out.println(s);
        if (rptps != null)
            rptps.println(s);
    }
    
    /**
     * Print a string to the report file.
     * @param   s is the string to be printed
     */
    public static void rpt (String s) {
        if (rptps != null)
            rptps.println(s);
    }
    
    /**
     * Print a string buffer to the report file.
     * @param   sb is the string buffer to be printed
     */
    public static void rpt (StringBuffer sb) {
        if (rptps != null)
            rptps.println(sb);
    }
    
    /**
     * Print a string to the intermediate code list file.
     * @param   s is the string to be printed
     */
    public static void icl (String s) {
        if (iclps != null)
            iclps.println(s);
    }
    
    /**
     * Print a string buffer to the intermediate code list file.
     * @param   sb is the string buffer to be printed
     */
    public static void icl (StringBuffer sb) {
        if (iclps != null)
            iclps.println(sb);
    }

    /**
     * Print a parser non-fatal warning message preceded by the current
     * token location. The token location may be slightly in advance
     * of the error position due to parser lookahead.
     * @param   s is the string to be printed
     */
    public static void wmsg (String s) {
        String  m = file_path + ":" + line_no + ": Warning: " + s;
        
        if (fc != null)
            logText(m);
        else
            System.out.println(m);
        if (rptps != null)
            rptps.println(m);
        warnings++;
    }

    /* NOT CURRENTLY USED
    // Print a parser warning message preceded by a
    // token location. The token whose location is prepended is
    // passed explicitly.
    public static void wmsg (Token t, String s) {
        String  m = t.fileName + ":" + t.lineNo + ": Warning: " + s;
        System.out.println(m);
    }
    */

    /**
     * Print a parser error message preceded by the current
     * token location. The token location may be slightly in advance
     * of the error position due to parser lookahead.
     * Execution will not proceed following parsing.
     * @param   s is the string to be printed
     */
    public static void emsg (String s) {
        String  m;
        if (isparsing)
            m = file_path + ":" + line_no + "\nParse error: " + s;
        else if (creating_variables)
            m = file_path + ": - \nVariable creation error: " + s;
        else
            m = file_path + ":" + line_no + "\nError: " + s;
        if (fc != null)
            logText(m);
        else
            System.out.println(m);
        if (rptps != null)
            rptps.println(m);
        errors++;
    }

    /**
     * Print an error message preceded by a
     * location.
     * @param   s is the string to be printed
     * @param   loc is the location
     */
    public static void emsg (String s, SrcLoc loc) {
        String  m;
        if (isparsing)
            m = loc.getFileName() + ":" + loc.getLineNo() + ": Parse error: " + s;
        else
            m = loc.getFileName() + ":" + loc.getLineNo() + ": Error: " + s;
        if (fc != null)
            logText(m);
        else
            System.out.println(m);
        if (rptps != null)
            rptps.println(m);
        errors++;
    }

    /**
     * Print a warning message preceded by a
     * location.
     * @param   s is the string to be printed
     * @param   loc is the location
     */
    public static void wmsg (String s, SrcLoc loc) {
        String  m = loc.getFileName() + ":" + loc.getLineNo() + ": Warning: " + s;
        if (fc != null)
            logText(m);
        else
            System.out.println(m);
        if (rptps != null)
            rptps.println(m);
    }

    /**
     * Print a parser error message preceded by the current
     * token location and then exit. The token location may
     * be slightly in advance of the error position due to
     * parser lookahead.
     * Execution will not proceed following parsing.
     * @param   s is the string to be printed
    public static void femsg (String s) {
        String  m = file_name + ":" + line_no + ": ";
        if (fc != null) {
            logText(m);
            logText(s);
        } else {
            System.out.println(m);
            System.out.println(s);
        }
        if (rptpw != null) {
            rptpw.println(m);
            rptpw.println(s);
        }
        System.exit(1);
    }
     */
    
    /**
     * Get a file parent directory path with trailing "/" (or "\" for Windows).
     * @param  fp is the file path
     * @return the file directory path
     *
    public static String getFileDirectoryPath (String fp) {
        int i = fp.lastIndexOf("/");  // Unix, Linux, OSX
        if (i == -1)
            i = fp.lastIndexOf("\\"); // Windows
        if (i != -1)
            return(fp.substring(0, i+1));
        else
            return("");
    }*/
    
    /**
     * Examine the time of last modification of a source file.
     * If it is more recent than 'sourceTime' then update 'sourceTime'.
     * @param fname is the file name
     */
    public static void addSourceTime (String fname) {
        File file = new File(fname);
        long    t = file.lastModified();
        if (t > sourceTime)
            sourceTime = t;
    }
    
    /**
     * Change the design name from that derived from the file name but
     * not one given as a command line argument.
     * @param   s is the new design name
     */
    public static void changeDesign (String s) {
        if (cldn == null) {// dont override if command line design name supplied
            design_name = cleanEDIFIdent(s);
            addDir("designName", new Val(s, null), null);
        }
    }
    
    /**
     * Get the FPGA part family class.
     * @return  the FPGA part family class
     */
    public static TDECode getFamily () {
        if (family != null)
            return(family);

        msg("directive 'part' is not defined - using generic part");
        family = new UNISIMS();
        return(family);
    }
    
    /**
     * Find a user-defined group (3PL class) searching back through
     * nested scopes to a LOCAL scope enclosing the shallowest
     * LOCAL scope. if not found search the global scope. Each
     * scope contains a group map. Returns null if not found.
     * @param   id is the user-defined group identifier
     * @return  the inbuilt user-defined
     */
    public static Group findGroup (String id) {
        int local_count = 0;
        if (!body_stack.isEmpty()) {
            Body    body = body_stack.getFirst();
            Group   g;
        // search back through static chain of nested
        // modules/procedures/functions as far as LOCAL scope
        // enclosing shallowest LOCAL scope.
            do {
                if ((g=body.getGroups().get(id)) != null)
                    return(g);
                body = body.getEnclosing();
            } while ((body != null) && (local_count <= 2));
        }
        // not found - try global
        return(groups.get(id));
    }

    /**
     * Find an inbuilt module.
     * Returns null if not found.
     * @param   id is the inbuilt module identifier
     * @return  the inbuilt module
     */
    public static InbuiltMod findInbuiltMod (String id) {
        return(inbuilt_mods.findMod(id));
    }
    
    /**
     * Find a user-defined module searching back through nested
     * scopes to a LOCAL scope enclosing the shallowest LOCAL
     * scope. If not found search the file scope then the global scope.
     * Each scope contains a function map. Returns null if not found.
     * @param   id is the module identifier
     * @return  the Module
     */
    public static Module findMod (String id) {
        int local_count = 0;
        if (body_stack.isEmpty())
            return(null);
        Scope                   fmscope = getFileScope();   // file module scope
        TreeMap<String,Module>  fmmods = null;              // file module scope modules
        Body                    body = body_stack.getFirst();
        Module                  m;
        
        if (fmscope != null)
            fmmods = fmscope.getMods();
            
        // search back through static chain of nested
        // modules/procedures/functions as far as LOCAL scope
        // enclosing shallowest LOCAL scope.
        do {
            if ((body.getScope() != null) && (body.getScope()).isLocal())
                local_count++;
            if ((m=body.getModules().get(id)) != null)
                return(m);
            body = body.getEnclosing();
        } while ((body != null) && (local_count <= 2));
        
        // not found - try file scope
        if ((fmmods != null) && fmmods.containsKey(id))
            return(fmmods.get(id));
        
        // not found - try global
        return(modules.get(id));
    }
    
    /**
     * Find an inbuilt procedure.
     * Returns null if not found.
     * @param   id is the inbuilt procedure identifier
     * @return  the inbuilt procedure
     */
    public static InbuiltProc findInbuiltProc (String id) {
        return(inbuilt_procs.findProc(id));
    }
    
    /**
     * Find a user-defined procedure searching back through nested
     * scopes to a LOCAL scope enclosing the shallowest LOCAL
     * scope. If not found search the file scope then the global scope.
     * Each scope contains a function map. Returns null if not found.
     * @param   id is the user-defined procedure identifier
     * @return  the user-defined procedure
     */
    public static Procedure findProc (String id) {
        int local_count = 0;
        if (body_stack.isEmpty())
            return(null);
        Scope                       fmscope = getFileScope();   // file module scope
        TreeMap<String,Procedure>   fmprocs = null;             // file module scope procedures
        Body                        body = body_stack.getFirst();
        Procedure                   p;
        
        if (fmscope != null)
            fmprocs = fmscope.getProcs();
            
        // search back through static chain of nested
        // modules/procedures/functions as far as LOCAL scope
        // enclosing shallowest LOCAL scope.
        do {
            if ((body.getScope() != null) && (body.getScope()).isLocal())
                local_count++;
            if ((p=body.getProcedures().get(id)) != null)
                return(p);
            body = body.getEnclosing();
        } while ((body != null) && (local_count <= 2));
        
        // not found - try file scope
        if ((fmprocs != null) && fmprocs.containsKey(id))
            return(fmprocs.get(id));
        
        // not found - try global
        return(procedures.get(id));
    }
    
    /**
     * Find an inbuilt function.
     * Returns null if not found.
     * @param   id is the inbuilt function identifier
     * @return  the inbuilt function
     */
    public static InbuiltFunc findInbuiltFunc (String id) {
        return((inbuilt_funcs.findFunc(id)));
    }
    
    /**
     * Find a user-defined function searching back through nested
     * scopes to a LOCAL scope enclosing the shallowest LOCAL
     * scope. If not found search the file scope then the global scope.
     * Each scope contains a function map. Returns null if not found.
     * @param   id is the user-defined function identifier
     * @return  the inbuilt user-defined
     */
    public static Function findFunc (String id) {
        int local_count = 0;
        if (body_stack.isEmpty())
            return(null);
        Scope                       fmscope = getFileScope();   // file module scope
        TreeMap<String,Function>    fmfuncs = null;             // file module scope functions
        Body                        body = body_stack.getFirst();
        Function                    f;
        
        if (fmscope != null)
            fmfuncs = fmscope.getFuncs();
            
        // search back through static chain of nested
        // modules/procedures/functions as far as LOCAL scope
        // enclosing shallowest LOCAL scope.
        do {
            if ((body.getScope() != null) && (body.getScope()).isLocal())
                local_count++;
            if ((f=body.getFunctions().get(id)) != null)
                return(f);
            body = body.getEnclosing();
        } while ((body != null) && (local_count <= 2));
        
        // not found - try file scope
        if ((fmfuncs != null) && fmfuncs.containsKey(id))
            return(fmfuncs.get(id));
        
        // not found - try global
        return(functions.get(id));
    }
    
    /**
     * Find a directive.
     * @param   key is the directive key
     * @return  the directive value or null
     */
    public static Val findDir (String key) {
        if (directives.containsKey(key))
            return(directives.get(key));
        return(null);
    }
    
    /**
     * Find a boolean directive.
     * @param   key is the directive key
     * @return  the directive boolean value
     */
    public static boolean boolDir (String key) {
        Val v = findDir(key);
        if (v == null)
            throw new ExEx("attempt to get missing \'log\' directive '" + key + "'");
        return(v.getSingleLval(null));
    }

    
    /**
     * Find a long directive.
     * @param   key is the directive key
     * @return  the directive long value
     */
    public static long longDir (String key) {
        Val v = findDir(key);
        if (v == null)
            throw new ExEx("attempt to get missing \"int\" directive '" + key + "'");
        return(v.getSingleIval(null));
    }

    
    /**
     * Find a string directive.
     * @param   key is the directive key
     * @return  the directive String value
     */
    public static String stringDir (String key) {
        Val v = findDir(key);
        if (v == null)
            throw new ExEx("attempt to get missing \"str\" directive '" + key + "'");
        return(v.getSingleSval(null));
    }
    
    /**
     * Add a directive.
     * If one of the same name is found, the new value will overwrite
     * the old value, but the type of the value must be the same.
     * @param   key is the directive key string
     * @param   value is the directive value
     * @param   loc is the source file location
     */
    public static void addDir (String key, Val value, SrcLoc loc) {
        // Do not allow redefinition of "part"
        if (key.equals("part") && directives.containsKey(key)) {
            wmsg("Attempt to redefine directive 'part' - ignored!");
            return;
        }
        
        // Do not allow change of type.
        if (directives.containsKey(key)) {
            Val dval = findDir(key);
            if (dval.getPrimType() != value.getPrimType())
                throw new ExEx("attempt to change type of existing directive");
        }
        
        // Special case for  "ALU"
        if (key.equals("ALU")) {
            if (value.getPrimType() != Ptype.LOG) {
                wmsg("attempt to set directive \"ALU\" to non-\"log\" ignored!");
                return;
            }
            ALU = value.getSingleLval(loc);
        }
        value.setDummyVar(false, loc);
        directives.put(key, value);
        int klen = key.length();
        if (klen > directive_max_key_length)
            directive_max_key_length = klen;
    }
    
    /**
     * Add a boolean directive.
     * If one of the same name is found, the new value will overwrite
     * the old one.
     * @param   key is the directive key string
     * @param   b is the boolean value
     */
    public static void addDir (String key, boolean b) { addDir(key, new Val(b, null), null); }
    
    /**
     * Add a string directive.
     * If one of the same name is found, the new value will overwrite
     * the old one.
     * @param   key is the directive key string
     * @param   s is the String value
     */
    public static void addDir (String key, String s) { addDir(key, new Val(s, null), null); }
    
    /**
     * Add a long directive.
     * If one of the same name is found, the new value will overwrite
     * the old one.
     * @param   key is the directive key string
     * @param   l is the long value
     */
    public static void addDir (String key, long l) { addDir(key, new Val(l, null), null); }

    /**
     * Add a directive during parsing.
     * If one of the same name is found, the new directive will overwrite
     * the old one.
     * @param   key is the directive key string
     * @param   value is the directive value or null
     * @param   loc is the source file location
     */
    public static void addPreDir (String key, Val value, SrcLoc loc) {
        Ident   ident = new Ident(key);
        if (ident.getScopeContext() != Context.DEFAULT)
            throw new ExEx("presetdirective() - directive identifiers do not have context ", loc);
        if (value == null)
            value = new Val(Boolean.valueOf(true), loc);
        // Special case for  "ALU"
        if (key.equals("ALU")) {
            if (value.getPrimType() != Ptype.LOG) {
                wmsg("attempt to set directive \"ALU\" to non-\"log\" ignored!");
                return;
            }
            ALU = value.getSingleLval(loc);
        }
        value.setDummyVar(false, loc);
        addDir(ident.getId(), value, loc);
    }
       
    /**
     * Remove a directive from the current scope.
     * If the directive is not present throw an exception.
     * @param   key is the directive key
     * @param   loc is the source file location
     */
    public static void remDir (String key, SrcLoc loc) {
        if (!directives.containsKey(key))
            throw new ExEx("directive '" + key + "' not found", loc);
        directives.remove(key);
    }
    
    /**
     * List all directive initial values to report file and intermediate code list
     * file if these are active.
     * @param   heading is a string to write as a heading to the list
     */
    public static void printDirectives (String heading) {
        rpt(heading);
        icl(heading);
        StringBuffer    sb = new StringBuffer();
        String          spaces = null;
        for (int i=-1 ; i<directive_max_key_length ; i++)
            sb.append(' ');
        spaces = sb.toString();
        for(Map.Entry<String, Val> me : directives.entrySet()) {
            String          id = me.getKey();
            Val             v = me.getValue();
            int             kl = id.length();
            String          insert = spaces.substring(0, directive_max_key_length - kl);
            if (!id.equals("netlistComment")) {
                String  s = "\t" + id + insert + " = " + v.getVal(0).toString();
                rpt(s);
                icl(s);
            }
        }
        rpt("\n");
        icl("\n");
    }
      
    /**
     * Find a variable.
     * Local scope is searched first using the scope stack.
     * Each scope contained immediately within a module, procedure
     * or function is marked so that the local scope search stops with
     * that block.
     * Each scope contains a variable map.
     * If not found in local scope, global scope is searched
     * (the last entry in the scope stack).
     * @param   id is the variable identifier
     * @param   loc is the source file location
     * @return  the variable
     */
    public static Var findVar (String id, SrcLoc loc) {
        return(findVar(id, Context.DEFAULT, loc));
    }
       
    /**
     * Find a variable.
     * Local scope is searched first using the scope stack.
     * Each scope contained immediately within a module, procedure
     * or function is marked so that the local scope search stops with
     * that block.
     * Each scope contains a variable map.
     * If not found in local scope, global scope is searched
     * (the last entry in the scope stack).
     * @param   id is the variable Ident
     * @param   loc is the source file location
     * @return  the variable
     */
    public static Var findVar (Ident id, SrcLoc loc) {
        return(findVar(id.getId(), id.getScopeContext(), loc));
    }
   
    /**
     * Find a variable.
     * Local scope is searched first using the scope stack.
     * Each scope contained immediately within a module, procedure
     * or function is marked so that the local scope search stops with
     * that block.
     * Each scope contains a variable map.
     * If not found in local scope, global scope is searched.
     * @param   id is the variable identifier
     * @param   context is the scope context if the search is to be limited to
     *          Context.GLOBAL, Context.FILE, Context.LOCAL or Context.CALLING
     * @param   loc is the source file location
     * @return  the variable
     */
    @SuppressWarnings("incomplete-switch")
    public static Var findVar (String id, Context context, SrcLoc loc) {
        Scope               scope = null;
        Scope               fmscope = getFileScope();           // file module scope
        Scope               clscope = getClassScope();          // class scope
        Scope               sclscope = getCommonClassScope();   // common class scope
        TreeMap<String,Var> gvars = global_scope.getVars();     // global variables
        TreeMap<String,Var> fmvars = null;                      // file module scope variables
        TreeMap<String,Var> cvars = null;                       // class scope variables
        TreeMap<String,Var> scvars = null;                      // static class scope variables
        Iterator<Scope> it;
        
        if (fmscope != null)
            fmvars = fmscope.getVars();
        
        if (clscope != null)
            cvars = clscope.getVars();

        if (sclscope != null)
            scvars = sclscope.getVars();

        // If GLOBAL or FILE context only is requested, go directly
        // to those scopes.
        switch (context) {
        case GLOBAL:
            if (gvars.containsKey(id))
                return(gvars.get(id));
            return(null);
        case FILE:
            if (fmvars.containsKey(id))
                return(fmvars.get(id));
            return(null);
        case GROUP:     // 3PL CLASS
            if ((cvars != null) && cvars.containsKey(id))
                return(cvars.get(id));
            else if ((scvars != null) && cvars.containsKey(id))
                return(scvars.get(id));
            else
                return(null);
         }

        // Prepare to scan scope stack.
        it = scope_stack.listIterator();
        scope = it.next();
        
        // Terrible hack to get around problem when assigning arguments
        // to parameters. Variables in a CompoundValNode are evaluated
        // when the time comes to do the parameter assignment by which time
        // the new scope has been pushed on the stack. This means that if
        // a parameter has the same identifier as one of the argument variables
        // in the CompoundValNode then the wrong variable will be selected.
        // The boolean top_scope_skip is set true when a CompoundValNode
        // is to be evaluated in exec/Body.inputParams(), causing the top
        // scope to be skipped when searching for variables.
        if (top_scope_skip)
            scope = it.next();
        
        // If want calling scope, skip back past local scope.
        if (context == Context.CALLING) {
            while (!scope.isLocal() && !scope.isFile())
                scope = it.next();
            scope = it.next();
        }

        // Search current context back to current module, procedure or
        // function scope, but stop at a file scope and examine that
        // explicitly via getFileScope().
        // If context is LOCAL, skip non-local scopes.
        while (!scope.isFile()) {
            if (((context != Context.LOCAL) || scope.isLocal()) && 
                                               scope.getVars().containsKey(id))
                return(scope.getVars().get(id));
            if (scope.isLocal())
                break;
            if (!it.hasNext())
                break;
            scope = it.next();
        }
        
        // 'scope' should now be a LOCAL scope.
        // See if the variable is there.
        if (scope.getVars().containsKey(id))
            return(scope.getVars().get(id));
        
        // If there is a class scope try that.
        // This is only the case when in the code body of a class.
        if ((cvars != null) && cvars.containsKey(id))
                return(cvars.get(id));
        
        // If there is a static class scope try that.
        // This is only the case when in the code body of a class.
        if ((scvars != null) && scvars.containsKey(id))
                return(scvars.get(id));
        
        // Try the current file module scope.
        if ((fmvars != null) && fmvars.containsKey(id))
            return(fmvars.get(id));

        // Finally try global scope.
        if ((gvars != null) && gvars.containsKey(id))
            return(gvars.get(id));

        // Not found.
        return(null);
    }
    
    /**
     * Add a variable to the current scope.
     * If one of the same name is found, an error message
     * is printed, followed by immediate exit.
     * @param   var is the variable
     * @param   context is the scope to which the variable is to be added,
     *          Context.GLOBAL, Context.LOCAL (current class, module,
     *          procedure or function), Context.CLASS for current class
     *          or Context.DEFAULT for the current code block
     * @param   loc is the source file location
     */
    public static void addVar (Var var, Context context, SrcLoc loc) {
        Scope   scope = findScope(context, loc);
        String  id = var.getID(IDtype.LITERAL);
        if (scope == null)
            throw new ExEx("no '" + context.contextname() + "' scope found", loc);
        if (scope.getVars().containsKey(id)) {
            Var     v = scope.getVars().get(id);
            SrcLoc  decloc = v.getLoc();
            String  file = decloc.getFileName();
            int     line = decloc.getLineNo();
            throw new ExEx("attempt to redefine variable '" + id + "'\r\n" +
                            "previously defined file " + file + " line " +
                            line, loc);
        }
        scope.getVars().put(id, var);
        var.setScope(scope);
    }
   
    /**
     * List variables in one or more scopes.
     * Local scope is searched first using the scope stack.
     * Each scope contained immediately within a module, procedure
     * or function is marked so that the local scope search stops with
     * that block.
     * Each scope contains a variable map.
     * @param   context is the scope context if the list is to be limited to
     *          Context.GLOBAL, Context.FILE, Context.LOCAL or Context.CALLING
     * @param   allsegs is true if the variables of all file scopes are to be listed
     * @param   loc is the source file location
     * @return  a list of variable scope/identifier string pairs
     */
    @SuppressWarnings("incomplete-switch")
    public static LinkedList<Object> listVars (Context context, boolean allsegs, SrcLoc loc) {
        Scope               scope = null;
        Iterator<Scope>     it;
        LinkedList<Object>  l;

        switch (context) {
        case GLOBAL:
            return(global_scope.listVars());
        case FILE:
            if (allsegs) {
                l = new LinkedList<Object>();
                it = scope_stack.iterator();
                while (it.hasNext()) {
                    scope = it.next();
                    if (scope.isFile())
                        l.addAll(scope.listVars());
                }
                return(l);
            } else
                return(getFileScope().listVars());
        }        

        // Scan scope stack.
        it = scope_stack.listIterator();
        scope = it.next();

        /*
        // If are evaluating arguments, skip current scope to get back to
        // calling scope.
        if (((Scope)scope_stack.getFirst()).isCalling())
            scope = (Scope)it.next();
        */
        
        // If want just local context, scan  to it.
        if (context == Context.LOCAL) {
            while (!scope.isLocal())
                scope = it.next();
            return(scope.listVars());
        }
        
        // If want calling scope, skip back past local scope.
        if (context == Context.CALLING) {
            while (!scope.isLocal() && !scope.isFile())
                scope = it.next();
            if (scope.isFile())
                throw new ExEx("no calling scope", loc);
            scope = it.next();
        }

        l = new LinkedList<Object>();

        // Search back through scope stack listing directives in each scope.
        // Only list directives from first file scope encountered unless
        // 'allsegs' is true, in which case list all file scope directives.
        // Once the local scope or a file scope has been found only list
        // directives from file scopes henceforth and then only if 'allsegs'
        // is true.
        int     nsf = 0;
        boolean lsfound = false;
        boolean isfile;
        for (;;) {
            isfile = scope.isFile();
            if (!isfile && !lsfound || isfile && (allsegs || (nsf++ == 0))) {
                l.addAll(scope.listVars());
                if (scope.isLocal())
                    lsfound = true;
            }
            if (it.hasNext())
                scope = it.next();
            else
                break;
        }

        // Finally the global scope.
        l.addAll(global_scope.listVars());

        return(l);
    }
    
    /**
     * Get the variable map from the local scope.
     * @return  the local scope variable map
     */
    public static TreeMap<String, Var> getLocalVars () {
        Iterator<Scope> it = scope_stack.listIterator();
        Scope           scope = it.next();
        while (!scope.isLocal())
            scope = it.next();
        return(scope.getVars());
    }
    
    @SuppressWarnings("incomplete-switch")
    private static Scope findScope (Context context, SrcLoc loc) {
        Scope           scope = null;
        Iterator<Scope> it;

        switch (context) {
        case GLOBAL:
            scope = global_scope;
            break;
        case FILE:
            scope = getFileScope();
            break;
        case CALLING:
            it = scope_stack.listIterator();
            do {
                if (!it.hasNext())
                    throw new ExEx("no calling scope", loc);
                scope = it.next();
            } while (!scope.isLocal() && !scope.isFile());
            if (scope.isFile())
                throw new ExEx("no calling scope", loc);
            /*do {      Previously searched for next LOCAL scope. Should be next scope, whatever!
                scope = it.next();
            } while (!scope.isLocal() && !scope.isFile());*/
            scope = it.next();
            break;
        case LOCAL:
            // scan for the next LOCAL or ILMOD scope but break if we find
            // a FILE scope (we are in file module)
            it = scope_stack.listIterator();
            scope = it.next();
            while (!scope.isLocal() && !scope.isILMod() && !scope.isFile())
                scope = it.next();
            if (scope.isFile()) // may be wrong file module, so get it explicitly
                scope = getFileScope();
            break;
        case GROUP:
            scope = getClassScope();
            break;
        case DEFAULT:
            scope = scope_stack.getFirst();
        }
        return(scope);
    }
    
    /*
     * Diagnostic function - to be completed if required.
     * 
    static void printScopeStack () {
        Scope           scope = null;
        Iterator<Scope> it;
        it = scope_stack.listIterator();
        while (it.hasNext()) {
            scope = it.next();
            switch (scope.getBlockType()) {
            case FMOD:
                
            case ILMOD:
                
            case NMOD:
                
            case PROC:
                
            case FUNC:
                
            default:
                
            }
        }
    }*/
    
    /**
     * Get the set of available clocks.
     * @return  the set of available clocks
     */
    public static HashSet<Clock> getClocks() {
        return(clocks);
    }

    /**
     * Set the current clock variable.
     * The previous clock variable is pushed on a stack and the
     * specified variable becomes the current clock clock variable.
     * If the location argument is not null a check is made that we are
     * at the top level of target logic.
     * @param   var is the clock variable or null
     * @param   loc is the source file location
     */
    public static void pushCurrentClock (Clock var, SrcLoc loc) {
        if ((var != null) && !clocks.contains(var))
            clocks.add(var);
        clockvarstack.add(0, currentclockvar);
        currentclockvar = var;
    }
    
    /**
     * Pop the current clock variable.
     * If the clock stack is not empty the previous current
     * clock variable is restored from a stack. if the clock stack
     * is empty the current clock becomes null (no current clock).
     * If the location argument is not null a check is made that we are
     * at the top level of target logic.
     * @param   loc is the source file location
     */
    public static void popCurrentClock (SrcLoc loc) {
        if (clockvarstack.size() == 0) {
            currentclockvar = null;
            return;
        }
        currentclockvar = clockvarstack.get(0);
        clockvarstack.remove(0);
        return;
    }
    
    /**
     * Get the current clock signal.
     * Returns null if there is no current clock.
     * @return  the current clock signal or null
     */
    public static TDEVar getCurrentClock () {
        if (currentclockvar == null)
            return(null);
        return(currentclockvar.getClkSig());
    }
    
    /**
     * Get the current clock variable.
     * Returns null if there is no current clock.
     * @return  the current clock variable
     */
    public static Clock getCurrentClockVar () {
        return(currentclockvar);
    }
    
    /**
     * Get the previous clock variable, i.e. the one
     * below the current one on the stack.
     * Returns null if there is no current clock.
     * @return  the previous clock variable
     */
    public static Clock getPrevClockVar () {
        if (clockvarstack.size() == 0)
            return(null);
        return(clockvarstack.get(0));
    }
    
    /**
     * Get the start signal associated with the current clock.
     * Exits with error if there is no current clock.
     * @param   loc is the source file location
     * @return  the current clock start signal or null
     */
    public static TDEVar getCurrentStart (SrcLoc loc) {
        if (currentclockvar == null)
            if (loc == null)
                throw new ExEx("no current clock");
            else
                throw new ExEx("no current clock", loc);
        TDEVar  start = currentclockvar.getStartSig(loc);
        if (start == null)
            throw new ExEx("clock '" + currentclockvar.getID(IDtype.CHAIN) + "' has not been added using addclock()", loc);
        return(start);
    }
    
    /**
     * Push an initialisation block for a Group (3PL class) onto the
     * Group initialisation list.
     * @param   bn is the BlockNode containing initialisation code
     */
    public static void pushInit (BlockNode bn) {
        /*if (bn.getGroup() == null) {
            emsg("initialise block not within a class");
            throw new ExEx("");
        }*/
        init_class.add(bn);
    }
    
    /**
     * Get the current file module scope from the file module scope stack.
     * @return  the current file module scope
     */
    public static Scope getFileScope () {
        Body    body = getCurrentBody();
        if (body == null)
            return(null);
        return(body.getFileScope());
    }

    /**
     * Push a class body onto the class stack.
     * @param   b is a class body
     */
    public static void pushClassBody (Body b) {
        class_body_stack.addFirst(b);
    }
    
    /**
     * Get the current class scope from the class body stack.
     * @return  the class scope
     */
    public static Scope getClassScope () {
        if (class_body_stack.isEmpty())
            return(null);
        return(class_body_stack.getFirst().getScope());
    }
    
    /**
     * Get the current common class scope from the class body stack.
     * @return  the class scope
     */
    public static Scope getCommonClassScope () {
        if (class_body_stack.isEmpty())
            return(null);
        Body b = class_body_stack.getFirst();
            return(b.getClassCommonScope());
    }
    
    /**
     * Pop the class stack.
     */
    public static void popClassBody () {
        class_body_stack.removeFirst().getName();
    }

    /**
     * Push the current module, procedure or function body during execution on
     * the body stack.
     * @param   body is the current module, procedure or function body
     */
    public static void pushBody (Body body) {
        body_stack.addFirst(body);
    }
    
    /**
     * Pop the current module, procedure or function body during execution from
     * the body stack.
     */
    public static void popBody () {
        body_stack.removeFirst();
    }

    /**
     * Get the current module, procedure or function body during execution from
     * the body stack.
     * @return the current body
     */
    public static Body getCurrentBody () {
        if (body_stack.isEmpty())
            return(null);
        return(body_stack.getFirst());
    }
    
    /**
     * Push a scope on the scope stack.
     * @param   scope is the scope
     * @param   loc is the source file location
     */
    public static void pushScope (Scope scope, SrcLoc loc) {
        scope_stack.addFirst(scope);
        call_loc_stack.addFirst(loc);
    }
    
    /**
     * Pop a scope off the scope stack.
     */
    public static void popScope () {
        scope_stack.removeFirst();
        call_loc_stack.removeFirst();
    }
    
    /**
     * Get the call hierarchy string from the scope stack
     * @param   context is the scope context
     * @param   loc is the source file location
     * @return  the call hierarchy string
     */
    public static String scopeHierarchy (Context context, SrcLoc loc) {
        if (context == Context.GLOBAL)
            return("glob" + hsep);

        boolean         found = false;
        Scope           s = findScope(context, loc);
        String          h = "";
        Iterator<Scope> it = scope_stack.listIterator();
        while (it.hasNext()) {
            Scope   scope = it.next();
            if (!found)
                found = (s == scope);
            if (found)
                h = scope.getCallString() + h;
        }
        return(h);
    }
    
    /**
     * Construct a stack trace.
     * @return the stack trace string
     */
    public static String stackTrace () {
        Iterator<Scope>     its = scope_stack.listIterator();
        Iterator<SrcLoc>    itl = call_loc_stack.listIterator();
        SrcLoc              prev_loc = null;
        SrcLoc              loc = errloc;
        StringBuffer        sb = new StringBuffer();
        int                 depth = 0;
        while (its.hasNext()) {
            String  bname = null;
            String  name = null;
            Scope   scope = its.next();
            switch (scope.getBlockType()) {
            case FMOD:
                bname = "file module ";
                name = scope.getFileModuleName();
                prev_loc = loc;
                loc = itl.next();
                break;
            case NMOD:
                bname = "called module ";
                name = scope.getBody().getName();
                prev_loc = loc;
                loc = itl.next();
                break;
            case ILMOD:
                bname = "inline module ";
                prev_loc = loc;
                loc = itl.next();
                break;
            case PROC:
                bname = "procedure ";
                name = scope.getBody().getName();
                prev_loc = loc;
                loc = itl.next();
                break;
            case FUNC:
                bname = "function ";
                name = scope.getBody().getName();
                prev_loc = loc;
                loc = itl.next();
                break;
            default:
                itl.next();
            }
            if (bname != null) {
                if (depth > 0)
                    sb.append("\n");
                if (prev_loc != null) {
                    sb.append("    ");
                    sb.append(prev_loc.getFileName());
                    sb.append(":");
                    sb.append(prev_loc.getLineNo());
                    sb.append(": in ");
                
                    if (name != null) {
                        sb.append(bname);
                        sb.append("'");
                        sb.append(name);
                        sb.append("'");
                    } else 
                        sb.append(bname);
                }
                ++depth;
            }
        }
        return(sb.toString());
    }
    
    /**
     * Get a scope from the scope stack.
     * The scope stack is unchanged.
     * @param   index is the index into the stack where 0 is the top entry
     * @return  the top scope
     */
    public static Scope getCurrentScope (int index) { return(scope_stack.get(index)); }

    /**
     * Get the top module, procedure or function scope on the scope stack.
     * The scope stack is unchanged.
     * @return  the top module, procedure or function scope
     */
    public static Scope getCurrentBodyScope () {
        return(getCurrentBody().getScope());
    }

    /**
     * Find the top module scope on the scope stack.
     * This is used for working out queue acknowledgments.
     * The stack is unchanged.
     * @return   the top module scope
     */
    @SuppressWarnings("incomplete-switch")
    public static Scope getModuleScope () {
        for (Body body: body_stack) {
            Scope   scope = body.getScope();
            switch (scope.getBlockType()) {
            case FMOD:
            case NMOD:
            case ILMOD:
                return(scope);
            }
        }
        throw new ExEx("NO MODULE SCOPE");
    }
    
    /**
     * For a group (3PL class), module, procedure or function call
     * push the call name if inbuilt, or null otherwise, on the call name stack.
     * This stack is only used to check if a caller is an inbuilt module/procedure/function.
     * At the moment it is only used to ensure that function arglist() is not called as
     * a parameter function for an inbuilt.
     * @param   id is the identifier of the group (3PL class), module,
     *          procedure or function to be pushed
     */
    public static void pushCallName (String id) {
        call_name_stack.addFirst(id);
    }
    
    /**
     * Pop the call name from the call name stack.
     */
    public static void popCallName () {
        call_name_stack.removeFirst();
    }
    
    
    /**
     * Return an entry from the call name stack.
     * @param   index is the index of the entry to return, 0 being the top
     *          of the stack.
     * @return  the name if inbuilt, null otherwise
     */
    public static String getCallName (int index) {
        return(call_name_stack.get(index));
    }
    
    /**
     * Add a target variable to the variable queue.
     * These variables are all created at the end of
     * 3PL execution.
     * @param   v is the target variable
     */
    public static void queueVar (Var v) {
        var_queue.add(v);
    }
    
    /**
     * Add a function 'used' value and a set containing the execution
     * TDEVar to be connected to it to the func_used queue.
     * @param   uv is the 'used' value
     * @param   hs is the set containing an execution TDEVar
     */
    public static void queueUsed (Val uv, HashSet<TDEVar> hs) {
        execList.add(new ExecPair(uv, hs));
    }
   
    /**
     * Increment the source file line number.
     */
    public static void incrLineNo () {
        line_no++;
    }
    
    /**
     * Get the current source file line number.
     * @return  the current source file line number
     */
    public static int getLineNo () {
        return(line_no);
    }
    
    /**
     * Get the current source file path.
     * @return  the current source file path
     */
    public static String getFilePath () {
        return(file_path);
    }
    
    /**
     * Get the current source file name.
     * @return  the current source file name
     */
    public static String getFileName () {
        return(file_name);
    }
    
    /**
     * Set the current source file name and line number on entering or returning
     * to an included file.
     * @param   pos is the current source file position
     */
    public static void setPosition (StringBuilder pos) {
        //msg(pos.toString());
        String[] a = pos.toString().split("#");
        // a[0] is ""
        // a[1] is  "M" for the start of a module file
        //          "m" for the end of a module file
        //          "S" for the start of a source file
        //          "s" for the end of a source file
        // a[2] is the file name
        // a[3] is the line number
        // a[4] is "" if the file was opened or is an error message string
        //      if the file could not be opened
        prev_file_name = file_path;
        prev_line_no = line_no;
        prev_dir = file_dir;
        file_path = a[2];
        // The file path is split into 'file_dir' and 'file_name'.
        if (file_path != null) {
            int i = file_path.lastIndexOf('/');
            if (i < 0) {
                file_name = file_path;
                file_dir = "";
            } else {
                file_name = file_path.substring(i+1);
                file_dir = file_path.substring(0, i);
            }
        }
        // 'file_path', 'file_dir' and 'file_name' are
        // appended to lists. 'dir_index' keeps track of the
        // list entry indices.
        // 'abbrev_path' is the file path with the directory up to the source
        // file replaced with "FMOD..." where "..." is 'dir_index'.
        // The abbreviated path are used to shorten signal identifiers while
        // avoiding inadvertent identifier duplication.
        // 'file_depth' is only used for indenting the list of source files
        // in the optional report file and is also saved in a list
        // Error messages still use full path names.
        if (a[1].equals("M")) {
            dir_index++;    // initially -1
            if (dir_index == 0)
                abbrev_path = file_name;
            else
                abbrev_path = "FMOD" + dir_index + "/" + file_name;
        }
        if (a[1].equals("M") || a[1].equals("S")) {
            files.add(file_name);
            file_paths.add(file_path);
            file_dirs.add(file_dir);
            file_depth++;
        } else if (a[1].equals("m") || a[1].equals("s"))
            file_depth--;
        if (a[1].equals("M"))
            file_indices.add(dir_index);
        else if (a[1].equals("S"))
            file_indices.add(-1);
        if (a[1].equals("M") || a[1].equals("S"))
            file_depths.add(file_depth);
        line_no = Integer.parseInt(a[3]);
        if (a[4].length() != 0) {
            String  type = a[1].equals("M") ? "module" : "source";
            switch (line_no) {
            case 1:
                msg("unable to open " + type + " file '" + file_path + "'");
                break;
            case 2:
                msg("unable to open " + type + " file due to missing directive '" + file_path + "'");
                break;
            case 3:
                msg("unable to open " + type + " file due to wrongly typed directive '" + file_path + "'");
            }
            throw new ExEx(a[4]);
        }
    }

    public static void checkGroupClash (String name, boolean global) {
        if (global) {
            if (groups.containsKey(name))
                emsg(file_path + ":" + line_no + ": group '" + name +
                        "' attempt to override a previously declared global class");
        }
    }

    public static void checkModClash (String name, boolean global) {
        if (global) {
            if (modules.containsKey(name))
                emsg(file_path + ":" + line_no + ": module '" + name +
                        "' attempt to override a previously declared global module");
            if (procedures.containsKey(name))
                emsg(file_path + ":" + line_no + ": module '" + name +
                        "' duplicates a declared global procedure name");
        }
        if (findInbuiltMod(name) != null)
            emsg(file_path + ":" + line_no + ": module '" + name +
                    "' attempt to override an inbuilt module");
        if (findInbuiltProc(name) != null)
            emsg(file_path + ":" + line_no + ": module '" + name +
                    "' duplicates an inbuilt procedure name");
    }

    public static void checkProcClash (String name, boolean global) {
        if (global) {
            if (modules.containsKey(name))
                emsg("\n\tprocedure '" + name +
                        "' duplicates a global module name");
            if (procedures.containsKey(name))
                emsg("\n\tprocedure '" + name +
                        "' attempt to override a previously declared global procedure");
        }
        if (findInbuiltMod(name) != null)
            emsg("\n\tprocedure '" + name +
                    "' duplicates an inbuilt module name");
        if (findInbuiltProc(name) != null)
            rpt("\n\tprocedure '" + name +
                    "' attempt to override an inbuilt procedure");
    }

    public static void checkFuncClash (String name, boolean global) {
        if (global) {
            if (functions.containsKey(name))
                emsg(file_path + ":" + line_no + ": function '" + name +
                        "' duplicates a declared global function name");
        }
        if (findInbuiltFunc(name) != null)
            emsg(file_path + ":" + line_no + ": function '" + name +
                    "' overrides an inbuilt function");
    }
    
    /**
     * Add a module to the leader module queue.
     * @param m is the module
     * @param ident is an identifier for the module
     * @param t is a literal constant token from which an ordering integer
     * can be determined. It may be null.
     */
    public static void addLeaderModule (Module m, Token ident, Token t) {
        int     n = 0;
        int     i = 0;
        if (t != null) {
            String  is = t.toString();
            try {
                n = Integer.valueOf(is);
            } catch (NumberFormatException nfe) {
                emsg("leader module order integer format error");
            }
        }
        m.order = n;
        m.ident = ident.toString();
        for (i=0 ; i<leader_modules.size() ; i++) {
            Module  mod = leader_modules.get(i);
            if (mod.order > n)
                break;
        }
        leader_modules.add(i, m);
    }
    
    /**
     * Add a module to the trailer module queue - called during parsing.
     * @param m is the module
     * @param s is the scope for a 3PL class trailermodule
     * @param ident is an identifier for the module
     * @param token is a literal constant token from which an ordering integer
     * can be determined. It may be null.
     */
    public static void addTrailerModule (Module m, Scope s, Token ident, Token token) {
        int n = 0;
        if (token != null) {
            String  is = token.toString();
            try {
                n = Integer.valueOf(is);
            } catch (NumberFormatException nfe) {
                emsg("trailer module order integer format error");
            }
        }
        addTrailerModule(m, s, ident.image, n);
    }
    
    /**
     * Add a module to the trailer module queue - called during 3PL class execution.
     * @param m is the module
     * @param s is the scope for a 3PL class trailermodule
     * @param name is an identifier for the module
     * @param n is an ordering integer.
     */
    public static void addTrailerModule (Module m, Scope s, String name, int n) {
        m.order = n;
        m.ident = name;
        
        // If this trailer module is defined within a 3PL class code body
        // enter the module into the class module map during parsing with identifier "t mod"
        // The space in the identifier ensures that it cannot be called by application code.
        // The scope of a 3PL class instance will be added to 'trailer_modules' every time
        // the 3PL class (java class Group) is called.
        Body    ebody = m.getEnclosing();   // Enclosing body, i.e. the 3PL class (Group).
        TreeMap<String,Module>  modules = ebody.getModules();
        if ((ebody instanceof Group) && !modules.containsKey("t mod")) {
            // Called from parser (know that because module "t mod" not yet defined).
            // Just enter the trailer module as "t mod" in the module map of the
            // 3PL class (Group) containing the trailer module then return.
            //
            modules.put("t mod", m);
            m.ident = name;
            m.order = n;    // need to save the order for later call to addTrailerModule()
            return;
        }
        
        int     i = 0;
        Trailer trailer = new Trailer();
        trailer.order = n;
        for (i=0 ; i<trailer_modules.size() ; i++) {
            if (trailer_modules.get(i).order > n)
                break;
        }
        trailer.module = m;
        trailer.scope = s;
        trailer_modules.add(i, trailer);
        
    }
    
    /**
     * Add a module to the post-processing module queue.
     * @param m is the module
     * @param ident is an identifier for the module
     * @param t is a literal constant token from which an ordering integer
     * can be determined. It may be null.
     */
    public static void addPostProcessingModule (Module m, Token ident, Token t) {
        int     n = 0;
        int     i = 0;
        if (t != null) {
            String  is = t.toString();
            try {
                n = Integer.valueOf(is);
            } catch (NumberFormatException nfe) {
                emsg("post-processing module order integer format error");
            }
        }
        m.order = n;
        m.ident = ident.toString();
        for (i=0 ; i<post_modules.size() ; i++) {
            Module  mod = post_modules.get(i);
            if (mod.order > n)
                break;
        }
        post_modules.add(i, m);
        
    }
    
    public static void listStacks (SrcLoc loc) {
        msg("\nList Stacks\n");
        msg(loc.toString());
        msg(stackTrace());
        msg("\nbody stack\n__________\n");
        for (Body b: body_stack) {
            String  bname = b.getName() != null ? b.getName() : "";
            msg("block " + bname);
            Body   eb = b.getEnclosing();
            if (eb != null) {
                String  ebname = eb.getName() != null ? eb.getName() : "";
                msg("enclosing " + ebname);
            }
            Scope   s = b.getScope();
            Scope   fs = b.getFileScope();
            msg("location " + b.getSrcLoc().toString());

            msg("scope call " + s.getCallString());
            msg("scope file module " + s.getFileModuleName());
            msg("scope context " + s.getContext());
            msg("scope block type " + s.getBlockType().bname());
            msg("file scope call " + fs.getCallString());
            msg("file scope file module " + fs.getFileModuleName());
            msg("file scope context " + fs.getContext());
            msg("file scope block type " + fs.getBlockType().bname());
            msg("");
        }
        msg("scope stack\n__________\n");
        for (Scope s: scope_stack) {
            msg("scope call " + s.getCallString());
            msg("scope file module " + s.getFileModuleName());
            msg("scope context " + s.getContext());
            msg("scope block type " + s.getBlockType().bname());
            if (s.getBody() != null)
                msg("scope block name " + s.getBody().getName());
            msg("");
        }
    }

    /**
     * Transform a name to an EDIF-acceptable identifier.
     * Converts all non-alphanumeric characters, except "_", to "_".
     * @param   name is the identifier string to be transformed
     * @return the modified name
     */
    public static String cleanEDIFIdent(String name) {
	String s = name.replaceAll("[^A-Za-z0-9_]", "_");
	return(s);
    }
}

