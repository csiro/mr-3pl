package threepl.parser;

import static threepl.ThreePL.addPreDir;
import static threepl.ThreePL.findDir;
import static threepl.ThreePL.msg;
import static threepl.ThreePL.parent_directory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Pattern;

import threepl.ThreePL;
import threepl.exceptions.ExEx;
import threepl.exec.Val;

/**
 * This class is a FileReader filter which follows source links.
 * The string "source" or "module", starting in the first column,
 * followed by a file name in double quotes causes the
 * reader to switch temporarily to reading that included file.
 * Included files may be nested to any depth.
 * The 'source' line may contain any other text before or after
 * the quoted file name, however no extra '"' may appear prior
 * to the quoted file name.
 * The constructor is called with the initial input
 * source file name. Any leading directory string on the initial
 * source file name is copied so that it can be tried ahead of
 * source file names if they are not found in the current directory.
 *
 * The string #filename#lineno# is prepended to the first line
 * of any file or the first line after returning from an
 * included file. This is recognised by the parser as a token
 * which is skipped, but not before extracting the file name and
 * line number.
 *
 * A list of included file names is kept so that a 'call' list
 * can be printed for read or file opening errors. The original
 * source file is the first entry in the list.
 *
 * When first called the source file is opened but in addition the
 * file name has any directory path string stripped off the front and
 * saved so that any source files can be sought in the current directory.
 * There is a public ArrayList ThreePL.directories which contains the
 * names of any directories where included files might be found. The
 * directory path of the initial source file name is prepended to that list.
 *
 * The author apologises for the dreadful code! The logic
 * is almost incomprehensible, even to the author. Could
 * not find a neater way to do this even after several hours
 * and rewrites. But it works.
 */
public class SrcReader extends FileReader implements Constant {
    private int                     line;        // line number
    private String                  buffer;      // internal line buffer
    private int                     index;       // current index into buffer
    private SrcReader               src;         // have a nested source file
    private ArrayList<String>       files;       // list of nested files
    private ArrayList<Boolean>      are_modules; // list of booleans for module/source
    private String                  file_entry;  // location token for module/source file
    private String                  error_entry; // location token for error
    private String                  file_exit;   // location token for module/source file
    private static boolean          slash;
    private static boolean          star;
    private static boolean          prevslash;
    private static boolean          prevstar;
    private static boolean          comment;
    private static HashSet<String>  fileset;
    private final static Pattern    modulepatternq   = Pattern.compile("[ \\t]*module[ \\t]*\"[^\"]+\"");
    private final static Pattern    modulepatternd   = Pattern.compile("[ \\t]*module[ \\t]+directive\\(\"[^\"]+\"\\)");
    private final static Pattern    modulepatternidq = Pattern.compile("[ \\t]*module[ \\t]+ifdirective\\(\"[^\"]+\"\\)[ \\t]*\"[^\"]+\"");
    private final static Pattern    modulepatternidd = Pattern.compile("[ \\t]*module[ \\t]+ifdirective\\(\"[^\"]+\"\\)[ \\t]*directive\\(\"[^\"]+\"\\)");
    private final static Pattern    sourcepatternq   = Pattern.compile("[ \\t]*source[ \\t]*\"[^\"]+\"");
    private final static Pattern    sourcepatternd   = Pattern.compile("[ \\t]*source[ \\t]+directive\\(\"[^\"]+\"\\)");
    private final static Pattern    sourcepatternidq = Pattern.compile("[ \\t]*source[ \\t]+ifdirective\\(\"[^\"]+\"\\)[ \\t]*\"[^\"]+\"");
    private final static Pattern    sourcepatternidd = Pattern.compile("[ \\t]*source[ \\t]+ifdirective\\(\"[^\"]+\"\\)[ \\t]*directive\\(\"[^\"]+\"\\)");
    private final static Pattern    psdpattern = Pattern.compile("[ \\t]*presetdirective[ \\t]*\\([^\\)]+\\)[ \\t]*;");
    /**
     * This is the constructor for the input file stream filter.
     * An exception is thrown if the named file cannot be opened.
     * If the source syntax is wrong, a source file cannot be opened
     * or a read error occurs, error messages are written to standard
     * out and an exit is called.
     * @param   file_name is the name of the file to be opened for reading
     * @throws  FileNotFoundException if unable to open the named file. This
     *          exception is thrown rather than being caught because it
     *          is thrown by the super class.
     */
    public SrcReader (String file_name) throws FileNotFoundException, ExEx {
        super(file_name);
        ThreePL.addSourceTime(file_name);
        fileset = new HashSet<String>();
        files = new ArrayList<String>();
        are_modules = new ArrayList<Boolean>();
        files.add(file_name);
        are_modules.add(Boolean.valueOf(true));
        ArrayList<String> a = new ArrayList<String>();
        a.add(file_name);
        fileset.add(file_name);
        line = 0;
        index = -1;
        src = null;
        slash = false;
        star = false;
        prevslash = false;
        prevstar = false;
        comment = false;
        if (file_name.startsWith(parent_directory)) {
            // The file is in the parent directory so remove the parent
            // directory leading path string. This will shorten net
            // identifiers but will not lead to a violation of unique net
            // identifiers.
            file_entry = "#M#" + file_name.substring(parent_directory.length()) + "#1##\n";
        } else {
            // The file is not in the parent directory so use the full
            // path string.
            file_entry = "#M#" + file_name + "#1##\n";
        }
        int p = file_name.lastIndexOf('/');
        if (p >= 0)
            ThreePL.include_dirs.add(0, file_name.substring(0, p+1));
    }
    
    /**
     * This constructor is called recursively to open included files.
     * @param   file_name is the name of an included file to be opened
     *          for reading
     * @param   files is the file name list for nested included files
     * @param   are_modules is a list of module files
     * @param   is_module is true if this is a module file
     * @throws  FileNotFoundException if unable to open the named file.
     */
    @SuppressWarnings("unchecked")
    private SrcReader (
        String              file_name,
        ArrayList<String>   files,
        ArrayList<Boolean>  are_modules,
        boolean             is_module
    ) throws FileNotFoundException, ExEx {
        super(file_name);
        ThreePL.addSourceTime(file_name);
        this.files = (ArrayList<String>)files.clone();
        this.files.add(0, file_name);
        this.are_modules = (ArrayList<Boolean>)are_modules.clone();
        this.are_modules.add(0, Boolean.valueOf(is_module));
        if (fileset.contains(file_name)) {
            String  n = is_module ? "module" : "source";
            msg("file '" + file_name + "' in more than one " + n + " statement");
            Iterator<String>  itf = files.iterator();
            Iterator<Boolean> itt = are_modules.iterator();
            msg("\tmodule \"...\" in " + itf.next());
            itt.next();
            while (itf.hasNext())
                if (itt.next().booleanValue())
                    msg("\tmodule statement in " + itf.next());
                else
                    msg("\tsource statement in " + itf.next());
            throw new ExEx("");
        }
        ArrayList<String> a = new ArrayList<String>();
        a.add(file_name);
        for (String s : files)
            a.add(s);
        line = 0;
        index = -1;
        src = null;
        file_entry = (is_module ? "#M#" : "#S#") + file_name + "#1##\n";
    }
    
    /**
     * This method overrides read() in super class FileReader.
     * It reads characters into an array.
     * Reading will continue until the length requested is
     * satisfied or an end of file is encountered. If any characters
     * were read the character count is returned. If an EOF was
     * encountered immediately on entry, -1 is returned.
     * @param   cbuf the character array in which to write the characters read
     * @param   offset the offset into cbuf at which writing is to start
     * @param   length the number of characters requested
     * @return  number of characters read, or -1 if EOF encountered
     */
    // Simply call read() as many times as necessary or until
    // -1 indicates EOF.
    public int read (char[] cbuf, int offset, int length) {
        int     c;
        int     count = 0;
        
        for (int i=0 ; i<length ; i++) {
            c = read();
            if (c < 0) {
                if (count == 0)
                    return(-1);
                else
                    return(count);
            }
            cbuf[offset+i] = (char)c;
            count++;
        }
        return(count);
    }
    
    /**
     * This method overrides read() in super class FileReader.
     * It reads a single character.
     * If an EOF was encountered, -1 is returned.
     * @return  the character read, or -1 if EOF encountered
     */
    // The character is read from the internal line buffer.
    // If the end of the internal line buffer has been reached
    // nextline() is called to get the next line. If 'SrcReader src'
    // is not null then an source file is currently being read
    // and the nested SrcReader read() method is called to get
    // a character. This nesting may be to any level.
    public int read () {
        int     c = 0;
        
        if (src != null) {
            c = src.read(); // read from included file
            if (c >= 0)
                return(c);
            else {
                src = null;
                index = -1;
            }
        }
        if (index < 0) {
            do {
                if(nextLine())
                    return(-1);     // EOF
                if (src != null) {
                    c = src.read(); // read from included file
                    if (c >= 0)
                        return(c);
                    else
                        src = null;
                }
            } while ((src != null) && (c == -1));   // may be successive files 
        }
        if (buffer.length() == 0)
            return(-1);
        c = buffer.charAt(index++);
        if (index == buffer.length())
            index = -1;
        prevslash = slash;
        prevstar = star;
        slash = (c == '/');
        star = (c == '*');
        if (star && prevslash)
            comment = true;
        if (comment && slash && prevstar)
            comment = false;
        return(c);
    }

    // Read the next line into the internal line buffer.
    // If that line has "source" or "module" as a leading string, get the file
    // name and create a nested SrcReader. Exceptions on opening or reading
    // are caught, resulting in error messages which include a listing
    // of the nested source file names, then an exit.
    private boolean nextLine () {
        index = 0;
        if (error_entry != null) {
            buffer = error_entry;
            error_entry = null;
            return(false);
        }
        if (file_exit != null) {
            buffer = file_exit + files.get(0) + "#" + line + "##\n";
            file_exit = null;
            return(false);
        }
        line++;
        if (file_entry != null) {
            buffer = file_entry;
            file_entry = null;
            return(false);
        }
        try {
            buffer = readLine();
        } catch (IOException e) {
            String              name;
            Iterator<String>    it = files.iterator();
            name = it.next();
            msg("I/O error reading source file '" + name + "'");
            msg("line " + line);
            while (it.hasNext()) {
                name = it.next();
                msg("incorporated in source file '" + name + "'");
            }
            throw new ExEx("");
        }
        if (buffer.length() == 0)
            return(true);   // end of file

        if (!comment && (buffer.length() > 9) && modulepatternq.matcher(buffer).lookingAt()) {
            handle_module_source(true, 0);
        } else if (!comment && (buffer.length() > 20) && modulepatternd.matcher(buffer).lookingAt()) {
            handle_module_source(true, 1);
        } else if (!comment && (buffer.length() > 27) && modulepatternidq.matcher(buffer).lookingAt()) {
            handle_module_source(true, 2);
        } else if (!comment && (buffer.length() > 36) && modulepatternidd.matcher(buffer).lookingAt()) {
            handle_module_source(true, 3);
        } else if (!comment && (buffer.length() > 9) && sourcepatternq.matcher(buffer).lookingAt()) {
            handle_module_source(false, 0);
        } else if (!comment && (buffer.length() > 20) && sourcepatternd.matcher(buffer).lookingAt()) {
            handle_module_source(false, 1);
        } else if (!comment && (buffer.length() > 27) && sourcepatternidq.matcher(buffer).lookingAt()) {
            handle_module_source(false, 2);
        } else if (!comment && (buffer.length() > 36) && sourcepatternidd.matcher(buffer).lookingAt()) {
            handle_module_source(false, 3);
        } else if (!comment && (buffer.length() > 17) && psdpattern.matcher(buffer).lookingAt()) {
            String      key;
            String      value = null;
            String[]    ss = buffer.split("[(),]");
            // should have -
            //  ss[0]   ....presetdirective...
            //  ss[1]   ..."key"...
            //  ss[2]   ...value... or ...;...
            //  ss[3]   missing or ...;...
            if ((ss.length < 3) || (ss.length > 4)) {
                throw new ExEx("presetdirective() syntax error, file '" +
                                    files.get(0) + "' line " + line);
            }
            ss[0] = ss[0].replaceAll(" ", ""); // remove all spaces

            // key
            key = findString(ss[1], 1, true, "presetdirective() key ", files.get(0), line-1);
            Val val = null;
            
            // value, if any
            if (ss.length == 4) {
                value = findString(ss[2], 1, true, null, files.get(0), line-1);
                if (value == null) {
                    ss[2] = ss[2].replaceAll(" ", ""); // remove all spaces
                    if (ss[2].equals("true"))
                        val = new Val(Boolean.valueOf(true), null);
                    else if (ss[2].equals("false"))
                        val = new Val(Boolean.valueOf(false), null);
                    else if (ss[2].indexOf(".") >= 0)
                        try {
                            val = new Val(Double.valueOf(ss[2]), null);
                        } catch (NumberFormatException e1) {
                            throw new ExEx("presetdirective() erroneous value '" + ss[2] + "', file '" +
                                                files.get(0) + "' line " + (line-1));
                        }
                    else
                        try {
                            val = new Val(Long.decode(ss[2]), null);
                        } catch (NumberFormatException e2) {
                            throw new ExEx("presetdirective() erroneous value '" + ss[2] + "', file '" +
                                                files.get(0) + "' line " + (line-1));
                        }
                    addPreDir(key, val, new SrcLoc(files.get(0), line, 0));
                } else
                    addPreDir(key, new Val(value, null), new SrcLoc(files.get(0), line, 0));
            }
            

            buffer = "\n";
        } else if (!comment && (buffer.length() >= 8) && buffer.startsWith("nomodule")) {
            if (are_modules.get(0).booleanValue()) {
                msg("nomodule prohibits module of file '" +
                                    files.get(0) + "' line " + line);
                for (int i=1 ; i<files.size() ; i++)
                    msg("included in " + files.get(i));
                throw new ExEx("");
            }
            buffer = "\n";
        } else if (!comment && (buffer.length() >= 8) && buffer.startsWith("nosource")) {
            if (!are_modules.get(0).booleanValue()) {
                msg("nosource prohibits source of file '" +
                                    files.get(0) + "' line " + line);
                for (int i=1 ; i<files.size() ; i++)
                    msg("included in " + files.get(i));
                throw new ExEx("");
            }
            buffer = "\n";
        }
        return(false);  // not end of file
    }
    
    private void handle_module_source (boolean is_module, int pattern) {
        String  qstring;
        String  iname = null;
        String  type = is_module ? "module" : "source";
        int     n = 1;
        Val     val;
        boolean go = false;

        qstring = findString(buffer, 1, false, type + " statement", files.get(0), line-1);
        
        switch (pattern) {
        case 0: // "fname"
            iname = qstring;
            break;
        case 1: // directive("key")
            val = findDir(qstring);
            if (val == null)
                n = 2;
            else if (val.getPrimType() != Ptype.STR)
                n = 3;
            else
                iname = val.getSingleSval(null);
            break;
        case 2: // ifdirective("key") "fname"
            val = findDir(qstring);
            if (val != null) {
                if (val.getPrimType() != Ptype.LOG)
                    go = true;
                else
                    go = val.getSingleLval(null);
            }
            if (!go) {
                buffer = "\n";
                return;
            }
            iname = findString(buffer, 2, false, type + " statement", files.get(0), line-1);
            break;
        case 3: // ifdirective("key") directive("key")
            val = findDir(qstring);
            if (val != null) {
                if (val.getPrimType() != Ptype.LOG)
                    go = true;
                else
                    go = val.getSingleLval(null);
            }
            if (!go) {
                buffer = "\n";
                return;
            }
            qstring = findString(buffer, 2, false, type + " statement", files.get(0), line-1);
            val = findDir(qstring);
            if (val == null)
                n = 2;
            else if (val.getPrimType() != Ptype.STR)
                n = 3;
            else
                iname = val.getSingleSval(null);
        }

        boolean notopen = true;
        if (iname != null) {
            try {
                src = new SrcReader(iname, files, are_modules, is_module);
                notopen = false;
            } catch (FileNotFoundException e1) {
                Iterator<String>    it = ThreePL.include_dirs.iterator();
                while (it.hasNext()) {
                    String  dir = it.next();
                    try {
                        src = new SrcReader(dir + iname, files, are_modules, is_module);
                        notopen = false;
                        break;
                    } catch (FileNotFoundException e2) {
                    }
                }
            }
        }
        if (notopen) {
            StringBuffer        sb = new StringBuffer();
            Iterator<String>    it = files.iterator();
            while (it.hasNext()) {
                sb.append(type);
                sb.append(" statement in file '");
                sb.append(it.next());
                sb.append("'\n");
            }
            buffer = (is_module ? "#M#" : "#S#") + iname + "#" + n + "#" + sb + "#\n";
        }
        // set up marker for return from module or source file
        file_exit = is_module ? "#m#" : "#s#";
    }
    
    // Read a line using the standard super class read() method.
    // The newline character is included at the end of the returned string.
    // A final line terminated at the EOF rather than with a newline will
    // not have a newline in the returned string.
    // Reading at an EOF simply returns an empty string, "".
    private String readLine() throws IOException {
        StringBuffer    b = new StringBuffer(5000);
        int             c;
        char            ch;

        do {
            c = super.read();
            ch = (char)c;
            if (c > 0)
                b.append(ch);
        } while ((c != -1) && (ch != '\n'));
        return(b.toString());
    }
    
    // Try to find the nth quoted string in argument 's'.
    // If succeeds, return the string with quotes removed.
    // If fails and argument 'mess' is non-null, write an error message
    // and exit. If argument 'mess' is null, return null.
    // If 'clean' is true the quoted string can only be preceded or
    // followed by spaces or tabs.
    private static String findString (String s, int n, boolean clean, String mess, String file, int line) {
        String[]    ss = s.split("\"");
        String      ws = "^[ \\t]*$"; // whole string white space regular expression
        int         len = ss.length;
        if ((len < (2*n)) || clean && (!ss[2*n-2].matches(ws) || ((len > 2*n) && !ss[2*n].matches(ws)))) {
            if (mess == null)
                return(null);
            throw new ExEx(mess + " string format error, file '" + file + "' line " + line);
        }
        
        return(ss[2*n-1]);
    }
}
