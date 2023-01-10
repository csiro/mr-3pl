/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */


package threepl.simulator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * This class handles plot data storage and retrieval from a logfile, and data display in a
 * gui. A read stream is always opened on the logfile, for display. If the logfile is being
 * created, ie not preexisting, a write stream is also opened. The logfile data is read
 * under control of a separate class, SimPlotWindow. Reads are  asynchronous with writes
 * because reads occur as a result of gui events like button clicks and automatic screen
 * updates. Because of this, all log file access methods are synchronized.
 * 
 * The logfile is a binary file, with a header followed by a number of samples (these are
 * written and read as SimSamples). The order of samples in the file corresponds to the X
 * axis in the plotted data (each sample corresponds to an event at a particular time).
 * Each sample contains the time and the values for the plot expressions, at that time. All samples
 * in the file are the same size, allowing random access by sample index. This allows
 * efficient panning and zooming on the sample data, without having to load the entire log
 * file into memory. However a sample cache is used to avoid unnecessary file access,
 * particularly for when multiple Graphs in SimPlotWindow are requesting the same block of
 * samples from the log file for display.
 * 
 * @version $Revision: 6003 $
 * @author Andrew Tulloh
 */
@SuppressWarnings("all")
public class SimPlotLogger extends SimLogger {

   // Plot file identifier, written at start of log file header
   private static final int PLOT_FILE_ID = 0xABCDABCD;
   
   // Sizes used in calculating file header offsets
   private static final int SIZEOF_INT = 4;
   private static final int SIZEOF_LONG = 8;
   private static final int SIZEOF_DOUBLE = 8;

   // File header offsets..
   
   // Plot file identifier offset
   private static final long FILE_ID = 0L;
   // Offset to time of first sample
   private static final long START_TIME = FILE_ID+SIZEOF_INT;
   // Offset to time of last sample
   private static final long END_TIME = START_TIME+SIZEOF_DOUBLE;
   // Number of samples in file offset
   private static final long SAMPLE_COUNT = END_TIME+SIZEOF_DOUBLE;
   // Size of sample, bytes offset
   private static final long SAMPLE_SIZE = SAMPLE_COUNT+SIZEOF_LONG;
   // Number of expressions per sample offset
   private static final long SAMPLE_LENGTH = SAMPLE_SIZE+SIZEOF_INT;
   // First sample file offset offset
   private static long FIRST_SAMPLE_OFFSET = SAMPLE_LENGTH+SIZEOF_INT; 
   // Expression names offset
   private static final long EXPR_DETAILS = FIRST_SAMPLE_OFFSET+SIZEOF_LONG; 

   // Time of first sample
   private double startTime;
   
   // Time of last sample
   private double endTime;
      
   // Number of samples in log file
   private long sampleCount;
   
   // Size of a sample in log file, bytes
   private int sampleSize;
   
   // Number of expressions in a sample
   private int sampleLength;
   
   // File offset to first sample
   private long firstSampleOffset;
   
   // Plot expression result types (from SimTypes)
   private int[] exprTypes;

   // Map which maps time (ns) to file sample index
   private TreeMap sampleIndexMap;
     
   // Max sample cache extension at start and end of samples in sample cache
   private static final int CACHE_EXTRA = 200;
   
   // Sample cache
   private SampleCache sampleCache;

   // RandomAccessFile for writing log file
   private RandomAccessFile out=null;
   
   //  RandomAccessFile for reading log file
   private RandomAccessFile in=null;
  
   // SimPlotWindow to display plot data
   private SimPlotWindow plotWindow; 

   /**
    * Constructor with timebase clock and expressions to plot
    * @param sim The Sim object
    * @param name The plot name, which is used to construct a new logfile name
    * @param exprs An array of expressions to be plotted
    */
   public SimPlotLogger (Sim sim, String name, SimExprSimpleNode[] exprs) {
      super(sim, name, exprs);
      init(exprs, null);   
   }

   /**
    * Constructor with existing filename
    * @param sim The Sim object
    * @param name The plot name, which may be used to construct a logfile name
    * @param filename The logfile name, or null if name should be used as base filename
    */    
   public SimPlotLogger (Sim sim, String name, String filename) {
      super(sim, name);
      init(null, filename);
   }
   
   /** Close logger and the display window */
   public void close () {
   
      // Close in reverse order to which opened 
      closeDisplay();
      closeReader();
      closeWriter();
   }	
   
   /** Close the file writer */
   public synchronized void closeWriter () {
      if (out==null)
         return;
      try {
         out.close();
	 out = null;
      } catch (IOException e) {
         throw new SimException(e.getMessage());
      }
   }
   
   /**
    * Get current read pointer
    * @return The current file reader file pointer
    */
   public synchronized long getReadPointer () {
      try {
         return in.getFilePointer();
      } catch (IOException e) {
         throw new SimException(e);
      }	 
   }

   /**
    * Get current write pointer
    * @return The current file writer file pointer
    */
   public synchronized long getWritePointer () {
      try {
         return out.getFilePointer();
      } catch (IOException e) {
         throw new SimException(e);
      }	 
   }

   /**
    * Return the time of the last sample
    * @return Time of last sample, ns
    */
   public synchronized double getEndTime () {
      return endTime;
   }
      
   /**
    * Return the time of the first sample
    * @return Time of first sample, ns
    */
   public synchronized double getStartTime () {
      return startTime;
   }
      
   /** Open the file reader */
   public synchronized void openReader () {
      try {
         in = new RandomAccessFile(filename, "r");	 
      } catch (FileNotFoundException e) {
         throw new SimException(e.getMessage());
      }
   }
   
   /** Open the file writer  */
   public synchronized void openWriter () {
      try {
         out = new RandomAccessFile(filename, "rw");
      } catch (FileNotFoundException e) {
         throw new SimException(e.getMessage());
      }
   }

   /** 
    * Seek on file reader
    * @param pos Byte offset within file to seek to
    */
   public synchronized void readSeek (long pos) {
      try {
         in.seek(pos);
      } catch (IOException e) {
         throw new SimException(e);
      }	 
   }

   /**
    * Write an int to the file writer at the current file position
    * @param ival The value to write
    */
   public synchronized void write (int ival) {   
      try {
         out.writeInt(ival);
      } catch (IOException e) {
         throw new SimException(e);
      }	 	 
   }

   /**
    * Write an int to the file writer at the desired file position
    * @param ival The value to write
    * @param offset The file offset at which to write the value
    */
   public synchronized void write (int ival, long offset) {   
      try {
         out.seek(offset);
         out.writeInt(ival);
      } catch (IOException e) {
         throw new SimException(e);
      }	 	 
   }
   
   /**
    * Write a long to the file writer at the desired file position
    * @param lval The value to write
    * @param offset The file offset at which to write the value
    */
   public synchronized void write (long lval, long offset) {   
      try {
         out.seek(offset);
         out.writeLong(lval);
      } catch (IOException e) {
         throw new SimException(e);
      }	 	 
   }
   
   /**
    * Write a double to the file writer at the desired file position
    * @param fval The value to write
    * @param offset The file offset at which to write the value
    */
   public synchronized void write (double fval, long offset) {   
      try {
         out.seek(offset);
         out.writeDouble(fval);
      } catch (IOException e) {
         throw new SimException(e);
      }	 	 
   }
   
   /**
    * Append a plot sample to the logfile
    * @param sample A plot sample to log
    */
   public synchronized void write (SimSample sample) {

      // Get sample time
      double time = (long)sample.get(0).getDouble();

      // If sampleCount>0 and time==RESET_TIME, a simulator reset has happened. Reset the graph.
      if (sampleCount>0 && time==Sim.RESET_TIME) {
         sampleCount = 0;
	 
	 // Have to clear the sample cache in case starting time of plot was originally greater
	 // than RESET_TIME.
	 sampleCache = new SampleCache();
	 
	 // Start collecting samples anew
	 sampleIndexMap = new TreeMap();
      }
      
      // Append sample
      // Must do this before calculating sampleCount==0 block below so that sampleSize
      // is calculated correctly
      writeSeek(firstSampleOffset+sampleCount*sampleSize);
      sample.write(out);
      
      // If this was first sample, calculate sample size and length and write to header.
      // Also write 'start time' to header. It is the time of the earliest sample.
      if (sampleCount==0) {
	 sampleSize = (int)(getWritePointer() - firstSampleOffset);
	 sampleLength = sample.length();
	 write(sampleSize, SAMPLE_SIZE);
	 write(sampleLength, SAMPLE_LENGTH);
	 startTime = time;
	 write(startTime, START_TIME);
      }

      // Write 'end time' to header. It is the time of the most recent sample.
      endTime = time;
      write(endTime, END_TIME);

      // Update sample index map before incrementing sample count
      sampleIndexMap.put(new Double(time), new Long(sampleCount));
            
      // Increment and write sample count to file header
      write(++sampleCount, SAMPLE_COUNT);

      // Notify plot window to redraw graphs
      plotWindow.plot();
   }	

   /**
    * Write a string at the current file position
    * @param s The String to write    
    */
   public synchronized void write (String s) {
      try {  
         out.writeUTF(s);
      } catch (IOException e) {
         throw new SimException(e);
      }	 
   }
         
   /**
    * Seek on file writer
    * @param pos Byte offset within file to seek to
    */
   public synchronized void writeSeek (long pos) {
      try {
         out.seek(pos);
      } catch (IOException e) {
         throw new SimException(e);
      }	 
   }

   /**
    * Read the latest sample at or earlier than the passed nominal time.
    * If no such sample exists return null.
    * @param time Nominal time of desired sample
    */
   public synchronized SimSample readSample (double time) {
   
      // Return null where time is before or more than one sample after existing file data
      if (sampleCount==0 || time>this.endTime || time<this.startTime)
         return null;

      Double[] arr = (Double[])sampleIndexMap.keySet().toArray(new Double[0]);
      int high, low, probe;
      long sample;
      	          
      // Find sample index in file corresponding to time.
      // If time maps directly to a sample index, use that index;
      // else find the most recent sample which is earlier than time
      // (decrement time and try again until a matching sample is
      // found).

      // Do a binary search in sampleIndexMap for the latest time earlier or equal to startTime.
      high = arr.length;
      low = -1;
      while (high - low > 1) {
         probe = (high + low) / 2;
         if (arr[probe].doubleValue() > time)
	    high = probe;
         else
	    low = probe;
      }

      if (low<0)
         return null;

      long sIndex = ((Long)sampleIndexMap.get(arr[low])).longValue();
      return sampleCache.getSamples(sIndex, sIndex+1)[0];      
   }
   
   
   /**
    * Return an array of samples. Limits of array are defined by the requested times of the
    * first and last sample (parameters startTime and endTime respectively).
    * If startTime and endTime define a period before or after existing samples, null is returned.
    * Otherwise the first sample in the returned array is the sample corresponding to startTime if
    * it exists, else the earliest sample if startTime is earlier than the first sample, else the
    * latest sample which is not later than startTime (ie the first array sample can be earlier than
    * startTime). The last sample in the returned array is the sample corresponding to endTime if it
    * exists, else the last sample if endTime is later than the last sample, else the earliest sample
    * which is not earlier than end time.
    * @param startTime The requested time since reset of the first sample to return, ns.
    * @param endTime The requested time since reset of the last sample to return, ns.
    * @return An array of SimSamples corresponding to the requested times
    */
   public synchronized SimSample[] readSamples (double startTime, double endTime) {

      // Return null where times define a period before or after existing file data
      if (sampleCount==0 || startTime>endTime || startTime>this.endTime || endTime<this.startTime)
         return null;

      Double[] arr = (Double[])sampleIndexMap.keySet().toArray(new Double[0]);
      int high, low, probe;
      long startSample, endSample;
      	          
      // Find sample index in file corresponding to startTime.
      // If startTime is earlier than the time of the first file sample,
      // use the first file sample index (0);
      // else if startTime maps directly to a sample index, use that index;
      // else find the most recent sample which is earlier than startTime
      // (decrement startTime and try again until a matching sample is
      // found).
      if (startTime<this.startTime)
         startSample = 0L;
      else {

         // Do a binary search in sampleIndexMap for the latest time earlier or equal to startTime.
         high = arr.length;
	 low = -1;
         while (high - low > 1) {
            probe = (high + low) / 2;
            if (arr[probe].doubleValue() > startTime)
	       high = probe;
            else
	       low = probe;
         }
	 startSample = low>=0 ? ((Long)sampleIndexMap.get(arr[low])).longValue() : 0L;
      }
      
      // Find sample index in file corresponding to endTime.
      // If endTime is later than the time of the last file sample,
      // use the last file sample index;
      // else if endTime maps directly to a sample index, use that index;
      // else find the earliest sample which is later than endTime
      // (increment endTime and try again until a matching sample is
      // found).
      if (endTime>this.endTime)
         endSample = sampleCount-1;
      else {

         // Do a binary search in sampleIndexMap for the earliest time later or equal to endTime.
         high = arr.length;
	 low = -1;
         while (high - low > 1) {
            probe = (high + low) / 2;
            if (arr[probe].doubleValue() < endTime)
	       low = probe;
            else
	       high = probe;
         }
	 endSample = high<arr.length ? ((Long)sampleIndexMap.get(arr[high])).longValue() : sampleCount-1;
      }

      // Return samples
      return sampleCache.getSamples(startSample, endSample+1);      
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
   
   /** Prepare to display the contents of the logfile in a SimPlotWindow */
   public void openDisplay () {   
      plotWindow = new SimPlotWindow(this, exprTypes, exprNames);
   }
   
  /**
   * Display the logfile data in a window
   * @param n An index used to offset the initial window position
   */
   public void show (String title, int n) {
      plotWindow.show(title, n);
   }
       
   /** Close the plot display */       
   public synchronized void closeDisplay () {
      
      plotWindow.close();
      
      // Inform the sim object the display is closing so plot can be removed
      sim.closeNotify(this);
   }

   // Helper method for constructors. Open new logfile for writing (unless reading from
   // preexisting plot logfile), open logfile separately for reading and open the plot display.    
   // @param exprs An array of expressions to be plotted, or null if using preexisting logfile
   // @param filename Logfile name: to create if exprs!=null, else a preexisting logfile to open
   //
   private void init (SimExprSimpleNode[] exprs, String filename) {

      // If filename is null, use name as base filename
      filename = filename!=null ? filename : name;      
      // If filename contains a "/", use as is else convert to a full plot file name, with path
      filename = nameToFilename(filename);

      // Create plot file path if doesn't yet exist
      if (!SimFile.existsPlotFilePath())
         SimFile.makePlotFilePath();

      this.filename = filename;
      
      // Open new file if creating
      if (exprs!=null) {
         openWriter();
	 
	 // Write plot file identifier to header
	 write(PLOT_FILE_ID, FILE_ID);
	 
	 // Write initial sample count to header
         sampleCount = 0;
         write(sampleCount, SAMPLE_COUNT);
	 	 
         // (Leave setting/writing of sampleSize, sampleLength, startTime, endTime
	 // until first write(SimSample) call)
	 
	 // Create expr names,types and write to header
	 exprTypes = new int[exprs.length];   
	 writeSeek(EXPR_DETAILS);
	 for (int i=0; i<exprs.length; i++) {
	    exprTypes[i] = exprs[i].nodeValue.getType();
	    write(exprTypes[i]);
	    write(exprNames[i]);
         }
	 
	 // Get first sample offset and write to header
         firstSampleOffset = getWritePointer();
	 write(firstSampleOffset, FIRST_SAMPLE_OFFSET);	 	 
      }

      // Open file for reading
      openReader();
      
      // If a preexisting plot file..
      if (exprs==null) {
      
         // Check is a plot file
	 if (readInt(FILE_ID) != PLOT_FILE_ID)
	    throw new SimException(filename+" is not a plot file");
	    
         // Read other header items
	 startTime = readDouble(START_TIME);
	 endTime = readDouble(END_TIME);
	 sampleCount = readLong(SAMPLE_COUNT);
         sampleSize = readInt(SAMPLE_SIZE);
         sampleLength = readInt(SAMPLE_LENGTH);
	 firstSampleOffset = readLong(FIRST_SAMPLE_OFFSET);
	 
	 // Read expr names from header
	 // Allow for fact that the number of expressions (including time, x value)
	 // is smaller than sample which includes appended clock active values
	 int numExprs = (sampleLength+1)/2;
	 exprTypes = new int[numExprs];
	 exprNames = new String[numExprs];    
	 readSeek(EXPR_DETAILS);
	 for (int i=0; i<exprNames.length; i++) {
	    exprTypes[i] = readInt();
	    exprNames[i] = readString();
         }	    
      }	

      // Create sample cache
      sampleCache = new SampleCache();

      // Create sample index map
      sampleIndexMap = new TreeMap();
      SimSample[] samples;
      double time;
      for (long i=0; i<sampleCount; i++) {
         samples = sampleCache.getSamples(i, i+1); 
	 time = samples[0].get(0).getDouble();        
         sampleIndexMap.put(new Double(time), new Long(i));
      }	 
	             
      // Open plot window
      openDisplay(); 
   
   }
            
   // Convert name to plot filename with path. No change if name contains "/".
   // @param name The base name used to construct the filename
   // @return Full pathname to log file, based on name
   //
   protected String nameToFilename (String name) {
      if (!name.matches(".*/.*")) 
	 name = SimFile.makePlotFileName(sim.getSourceName()+"."+name);
      return name;	    
   }
   
   // Read an int from the file reader at the current file position
   // @return The read value
   //
   private int readInt () {   
      try {
         return in.readInt();
      } catch (IOException e) {
         throw new SimException(e);
      }	 	 
   }

   // Read an int from the file reader at the desired file position
   // @param offset File offset from which to read the value
   // @return The read value
   //
   private int readInt (long offset) {   
      try {
         in.seek(offset);
         return in.readInt();
      } catch (IOException e) {
         throw new SimException(e);
      }	 	 
   }

   // Read a long from the file reader at the desired file position
   // @param offset File offset from which to read the value
   // @return The read value
   //
   private long readLong (long offset) {   
      try {
         in.seek(offset);
         return in.readLong();
      } catch (IOException e) {
         throw new SimException(e);
      }	 	 
   }

   // Read a double from the file reader at the desired file position
   // @param offset File offset from which to read the value
   // @return The read value
   //
   private double readDouble (long offset) {   
      try {
         in.seek(offset);
         return in.readDouble();
      } catch (IOException e) {
         throw new SimException(e);
      }	 	 
   }

   // Read a String from the file reader at the current file position
   // @return The read String
   //
   public synchronized String readString () {
      try {  
         return in.readUTF();
      } catch (IOException e) {
         throw new SimException(e);
      }	 
   }

   // Inner class to handle caching of samples from log file
   //
   private class SampleCache extends ArrayList {
   
      // First sample number in cache
      private long startSample;

      // Default constructor
      protected SampleCache () {
         super();
      }
      	       
      // Read an array of samples from the cache, renew cache from log file if necessary.
      //  Return as many samples as are available in requested range, always starting at
      //  startSample, or null if startSample is greater than the highest available sample
      //  index. Never blocks.
      //  If the request can be satisfied from samples already in the cache, those samples are
      //  returned immediately. If the request can be satisfied by appending some samples
      //  from file to the existing cache, the extra samples are appended to the cache and
      //  the requested samples are returned. Otherwise the cache is renewed with the requested
      //  samples, which are then returned. Whenever a number of samples are read from file,
      //  extra samples are prepended and appended, up to CACHE_EXTRA samples on each side
      //  of the requested samples. This tends to reduce file i/o. To prevent the cache growing
      //  without limit, the edges of the cache are trimmed during each call to getCacheSamples().
      //  @param startSample The starting requested sample index
      //  @param endSample The last sample requested index plus one
      // @return A SimSample[] array of some or all of requested samples, or null if not available
      //
      protected SimSample[] getSamples (long startSample, long endSample) {
      
         long size = (long)size();
	 
	 // If all requested samples lie within cache, return them
	 if (size>0 && startSample>=this.startSample &&
             endSample<=this.startSample+size)
            return getCacheSamples(startSample, endSample);

	 // Get sample count in logfile
	 long endSampleFile = out!=null?readLong(SAMPLE_COUNT):sampleCount;

	 // Return null if request cannot be satisfied
	 if (startSample>=endSampleFile)
            return null;

	 // If first requested sample lies within cache..
	 if (size>0L && startSample>=this.startSample && startSample<this.startSample+size) {

            // If last sample in cache is highest file sample, return that portion
            if (this.startSample+size==endSampleFile)
               return getCacheSamples(startSample, endSampleFile);	  

            // Otherwise read extra requested samples from file, append to cache and return them
            endSample = Math.min(endSample+CACHE_EXTRA, endSampleFile);
	    long firstSampleRead = this.startSample + size; 
            long toRead = endSample - firstSampleRead; 
            readSeek(firstSampleOffset+firstSampleRead*sampleSize);	   
            for (long i=0; i<toRead; i++)
               add(SimSample.read(in));
            return getCacheSamples(startSample, endSample);    	 
	 }

	 // Read as many of requested samples as possible into new cache, and return them
	 this.startSample = Math.max(startSample-CACHE_EXTRA, 0L);
	 endSample = Math.min(endSample+CACHE_EXTRA, endSampleFile);
	 long toRead = endSample - this.startSample;      
	 clear();
	 readSeek(firstSampleOffset+this.startSample*sampleSize);
	 for (long i=0; i<toRead; i++)
            add(SimSample.read(in));
	 return getCacheSamples(startSample, endSample);    
      }
      
      // Return a requested sublist from the cache sample list, as an array.
      // Also trim the extra samples at the beginning and end of the cache so
      // no more than CACHE_EXTRA samples are stored on each end. This prevents
      // the cache growing without limit.
      // @param startSample The starting requested sample index
      // @param endSample The last requested sample index plus one
      // @return A SimSample[] array of some or all of requested samples, or null if not available
      //
      private SimSample[] getCacheSamples (long startSample, long endSample) {

	 int iStart, iEnd, toRemove;

	 // Trim unused ends of cache, otherwise it just keeps growing..

	 // Trim high end first
	 iEnd = (int)(endSample - this.startSample);
	 int size = size();
	 if (size-iEnd > CACHE_EXTRA) {
            toRemove = size-iEnd-CACHE_EXTRA;
	    removeRange(iEnd+CACHE_EXTRA, size);
	 }	

	 // Trim low end
	 iStart = (int)(startSample - this.startSample);
	 if (iStart > CACHE_EXTRA) {
            toRemove = iStart-CACHE_EXTRA;
            removeRange(0, toRemove);
	    this.startSample += toRemove;
	 }   

	 // Get new cache indices of first and last sample
	 iStart = (int)(startSample - this.startSample);
	 iEnd = (int)(endSample - this.startSample);

	 // Return desired samples
	 return (SimSample[])subList(iStart, iEnd).toArray(new SimSample[0]);	    
      }
   }           
}      
