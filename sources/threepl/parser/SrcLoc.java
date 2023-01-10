package threepl.parser;

import static threepl.ThreePL.*;

import threepl.ThreePL;
import threepl.exec.Var;
import threepl.exec.Var.IDtype;

/**
 * A class containing token location information. The file reader
 * SrcReader inserts file name token strings at the start of any
 * input file and before the first line when reverting from an
 * included file back to the including file. These tokens contain the
 * file name and  line number. The parser extracts this information
 * and skips the tokens. The newline token is also skipped by the
 * parser, but the line counter is incremented. The file name, line
 * number and column number are copied into every token. Each tree
 * node when constructed contains a field of this class. The location
 * is that of the most relevant associated token.
 */
public class SrcLoc {
    private String  file_name;
    private int     line_no;
    private int     column;  


    /**
     * Construct a source location using current file name and line
     * number. Current column position is unknown.
     */
    public SrcLoc () {
        file_name = ThreePL.getFilePath();
        line_no = ThreePL.getLineNo();
        column = -1;
    }

    /**
     * Construct a source location using explicit file name, line
     * number and column.
     * @param   file file name
     * @param   line line number
     * @param   col column number
     */
    public SrcLoc (String file, int line, int col) {
        file_name = new String(file);
        line_no = line;
        column = col;
    }

    /**
     * Construct a source location using the information from a
     * token. Note that because input files have been switched back
     * and forth as include files have been encountered the Token
     * fields beginLine and endLine are wrong. Field lineNo should
     * be used instead (this assumes that the token does not extend
     * over more than one line, but for this compiler that is true
     * except for comments, which we skip anyway)
     * @param   t a parser token
     */
    public SrcLoc (Token t) {
        if (t == null) {
            file_name = null;
            line_no = -1;
            column = -1;
        } else {
            if (boolDir("filePath"))
                file_name = t.filePath;
            else
                file_name = t.fileName;
            line_no = t.lineNo;
            column = t.beginColumn;
        }
    }

    /**
     * Return a string giving the location information stored
     * in this class.
     * @return location string.
     */
    public String toString () {
        String retval = new String();
        retval = file_name + ":" + line_no + ":" + column + ": ";
        if (ThreePL.creating_variables) {
            Var v = Var.current_create;
            String  name = v.getID(IDtype.LITERAL);
            if (name.length() == 0) {
                retval += "unnamed ";
                retval += v.getMode().modename();
                retval += " mode variable.";
            } else {
                retval += v.getMode().modename();
                retval += " mode variable '";
                retval += name;
                retval += "'.";
            }
        }
        return(retval);
    }
    
    /**
     * Return the file name from the location information stored
     * in this class.
     * @return  source file name
     */
    public String getFileName () {
        return(file_name);
    }
    
    /**
     * Return the line number from the location information stored
     * in this class.
     * @return  source file line number
     */
    public int getLineNo () {
        return(line_no);
    }
}
