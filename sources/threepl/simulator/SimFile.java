/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */


package threepl.simulator;

import java.io.File;

/**
 * SimFile handles system specific filename generation and creation.
 * 
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
@SuppressWarnings("all")
public class SimFile {

   /** System specific separation character */
   private static String sep = File.separator;
   
   /** User home directory */
   private static String userHome =  System.getProperty("user.home");
   
   /** User's threepl sub-directory name */
   private static String threeplDir = ".threepl";
   
   /** Full path to .threepl dir */
   private static String threeplPath = userHome+sep+threeplDir;
   
   /** Full path to trace file directory */
   private static String traceFilePath = ".";
   
   /** Extension for trace files */
   private static String traceExt = ".trace";
   
   /** Full path to plot file directory */
   private static String plotFilePath = ".";
   
   /** Extension for plot files */
   private static String plotExt = ".plot";
   
    /**
     * Unique identifier string for this class and build used to help
     * debugging and binary tracking
     */
    private String _class_cvs_id = "$id$";
    
    /**
     * Return the user home directory
     *
     * @return The use home directory
     */
    static public String getUserHome () {
       return userHome;
    }

    /**
     * Return the user's .threepl sub-directory name
     *
     * @return The user's .threepl sub-directory name
     */
    static public String getThreeplDir () {
       return threeplDir;
    }
           
    /**
     * Return the full path to the user's .threepl sub-directory
     *
     * @return The full path to the user's .threepl sub-directory
     */
    static public String getThreeplPath () {
       return threeplPath;
    }
    
    /**
     * Return true if the pathname exists
     *
     * @param pathname The pathname to test for existence
     *
     * @return True if the pathname exists
     */          
    static public boolean exists (String pathname) {
       if (pathname==null)
          throw new SimException("SimFile.exists():null pathname");
       return (new File(pathname)).exists();
    }

    /**
     * Make a path
     *
     * @param pathname The path to create
     */
    static public void makePath (String pathname) {
       if (pathname==null)
          throw new SimException("SimFile.makePath():null pathname");
       if (!exists(pathname))
          if (!(new File(pathname)).mkdirs())
	     throw new SimException("could not create directory "+pathname);
    }	     

    /**
     * Returns true if the trace file path exists
     *
     * @return True if the trace file path exists
     */
    static public boolean existsTraceFilePath () {
       return exists(traceFilePath);
    }
    
    /**
     * Make the trace file path
     */
    static public void makeTraceFilePath () {
       makePath(traceFilePath);
    }
    
    /**
     * Make a full trace file filespec, with path and extension,
     * given the base filename.
     *
     * @param name The base filename
     */
    static public String makeTraceFileName (String name) {
       return traceFilePath+sep+name+traceExt;
    }
    /**
     * Returns true if the plot file path exists
     *
     * @return True if the plot file path exists
     */
    static public boolean existsPlotFilePath () {
       return exists(plotFilePath);
    }
    
    /**
     * Make the plot file path
     */
    static public void makePlotFilePath () {
       makePath(plotFilePath);
    }
    
    /**
     * Make a full plot file filespec, with path and extension,
     * given the base filename.
     *
     * @param name The base filename
     */
    static public String makePlotFileName (String name) {
       return plotFilePath+sep+name+plotExt;
    }
}
