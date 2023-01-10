
package threepl.exec;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;

import threepl.ThreePL;
import threepl.exceptions.ExEx;
import threepl.parser.SrcLoc;

/**
 * This class represents a file.
 */
public class FileDesc {
    private String              path;       // supplied path name
    private File                file;
    
    private boolean             isInput;
    private FileInputStream     fis;
    private BufferedReader      br;
    
    private boolean             isOutput;
    private boolean             append = false;
    private FileOutputStream    fos;
    private PrintStream         ps;
    
    private boolean             eof;        // file at end


    /**
     * Construct a file descriptor.
     * @param   path_arg is the supplied path name
     */
    public FileDesc (String path_arg) {
        if (path_arg.startsWith("/"))
            path = path_arg;
        else
            path = ThreePL.parent_directory + "/" + path_arg;
        file = new File(path);
    }
    
    /**
     * Open a file.
     * For an output file the path is checked for a valid writable file or the file is created.
     * For an input file the path is checked for a valid readable file.
     * @param   mess is a string to prepend to any error message
     * @param   output is true if this is an output file
     * @param   append is true for an output file if appending to an existing file.
     * @param   loc is the source file location
     */
    public void open(String mess, boolean output, boolean append, SrcLoc loc) {
        if (output) {
            if (file.exists()) {
                if (!file.isFile())
                    throw new ExEx(mess + " argument '" + path + "' not a file", loc);
                if (!file.canWrite())
                    throw new ExEx(mess + " argument file '" + path + "' not writable", loc);
            } else {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    throw new ExEx(mess + " cannot create argument file '" + path + "'", loc);
                }
            }
            isOutput = true;
            this.append = append;
        } else {
            if (!file.exists())
                throw new ExEx(mess + " cannot find argument file '" + path + "'", loc);
            if (!file.isFile())
                throw new ExEx(mess + " argument '" + path + "' not a file", loc);
            if (!file.canRead())
                throw new ExEx(mess + " argument file '" + path + "' not readable", loc);
            isInput = true;
        }
    }
    
    /**
     * Create an already open input file for standard input.
     * @param is is the input stream.
     */ 
    public FileDesc (InputStream is) {
        isInput = true;
        InputStreamReader   isr = new InputStreamReader(is);
        br = new BufferedReader(isr);
        eof = false;
    }
    
    /**
     * Create an already open print stream for stdout or stderr.
     * @param ps is the print stream
     */
    public FileDesc (PrintStream ps) {
        isOutput = true;
        this.ps = ps;
        eof = true;
    }
    
    /**
     * Get the BufferedReader class for an input file.
     * @param   mess is a string to prepend to a call error message
     * @param   loc is the source file location
     * @return  the BufferedReader class
     */
    public BufferedReader getBufferedReader (String mess, SrcLoc loc) {
        if (br != null)
            return(br);
        if (fis != null)
            throw new ExEx(mess + "attempt to change input file '" + path + "' from byte to character mode", loc);
        
        InputStream is;
        
        try {
            if (isOutput)
                throw new ExEx("file read on output file '" + path + "'", loc);
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new ExEx(mess + "input file '" + path + "' not found", loc);
        }
        InputStreamReader   isr = new InputStreamReader(is);
        br = new BufferedReader(isr);
        eof = false;
        return(br);
    }
     
    /**
     * Get the FileInputStream class for an input file.
     * @param   mess is a string to prepend to a call error message
     * @param   loc is the source file location
     * @return  the FileInputStream class
     */    
    public FileInputStream getFileInputStream (String mess, SrcLoc loc) {
        if (fis != null)
            return(fis);
        if (br != null)
            throw new ExEx(mess + "attempt to change input file '" + path + "' from character to byte mode", loc);
        try {
            if (isOutput)
                throw new ExEx("file read on output file '" + path + "'", loc);
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new ExEx(mess + "input file '" + path + "' not found", loc);
        }
        eof = false;
        return(fis);
    }
    
    /**
     * Get the PrintStream class for an output file.
     * @param   mess is a string to prepend to a call error message
     * @param   loc is the source file location
     * @return  the PrintStream class
     */
    public PrintStream getPrintStream (String mess, SrcLoc loc) {
        if (ps != null)
            return(ps);
        if (fos != null)
            throw new ExEx(mess + "attempt to change output file '" + path + "' from byte to character mode", loc);
        if (isInput)
            throw new ExEx("file write on input file '" + path + "'", loc);
        
        OutputStream    os;
        
        try {
            os = new FileOutputStream(file, append);
        } catch (FileNotFoundException e) {
            throw new ExEx(mess + "cannot open or create output file '" + path + "'", loc);
        }
        ps = new PrintStream(os);
        eof = true;
        return(ps);
    }
    
    /**
     * Get the FileOutputStream class for an output file.
     * @param   mess is a string to prepend to a call error message
     * @param   loc is the source file location
     * @return  the fileOutputStream class
     */    
    public FileOutputStream getFileOutputStream (String mess, SrcLoc loc) {
        if (fos != null)
            return(fos);
        if (br != null)
            throw new ExEx(mess + "attempt to change output file '" + path + "' from character to byte mode", loc);
        if (isInput)
            throw new ExEx("file write on input file '" + path + "'", loc);
        try {
            fos = new FileOutputStream(file, append);
        } catch (FileNotFoundException e) {
            throw new ExEx(mess + "cannot open or create output file '" + path + "'", loc);
        }
        eof = true;
        return(fos);
    }
    
    /**
     * Close an open file.
     * @throws IOException on an IO error
     */
    public void close () throws IOException {
        if (br != null)
            br.close();
        else if (fis != null)
            fis.close();
        else if (ps != null)
            ps.close();
        else if (fos != null)
            fos.close();
        path = null;
        br = null;
        fis = null;
        ps = null;
        fos = null;
        isInput = false;
        isOutput = false;
    }
    
    /**
     * Determine if an input file is at end-of-file.
     * @return  true if the file is at end-of-file
     */
    public boolean getEOF () {
        return(eof);
    }
    
    /**
     * Set end-of-file flag on this current open file.
     */
    public void setEOF () {
        eof = true;
    }
    
    /**
     * Get the path string of this current open file.
     * @return  the path string
     */
    public String getPath () {
        return(path);
    }
    
    /**
     * Determine if this current open file is opened for read.
     * @return  true if opened for read
     */
    public boolean isInput() { return(isInput); }
    
    /**
     * Determine if this current open file is opened for write.
     * @return  true if opened for read
     */
    public boolean isOutput() { return(isOutput); }
    
    /**
     * Get the open file.
     * @return  the open file
     */
    public File getFile () {
        return(file);
    }
}

