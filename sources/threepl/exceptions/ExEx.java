package threepl.exceptions;

import threepl.ThreePL;
import threepl.nodes.Node;
import threepl.parser.SrcLoc;

/**
 * An execution exception.
 * This exception class extends RuntimeException and hence avoids
 * the need for other classes or their methods to catch it or declare
 * that they throw it. This class handles all 3PL interpretation-time
 * exceptions and is caught in the main method in threepl.parser.ThreePL.jj.
 */
@SuppressWarnings("serial")
public class ExEx extends RuntimeException {    

    /**
     * Construct an error exception with a message but no source file location.
     * @param   message is the error message to be printed
     */
    public ExEx (String message) {
        super(message);
    }
    
    /**
     * Construct an error exception with a message and source file location.
     * The source file location is prepended to the error message.
     * @param   message is the error message to be printed
     * @param   loc is the source file location
     */
    public ExEx (String message, SrcLoc loc) {
        super(loc.toString() + message);
        ThreePL.errloc = loc;
    }
    
    /**
     * Construct an error exception with a message and source file location.
     * The source file location is prepended to the error message.
     * @param   message is the error message to be printed
     * @param   n is a code tree node, which contains a source file location
     */
    public ExEx (String message, Node n) {
        super(n.getSrcLoc().toString() + message);
        ThreePL.errloc = n.getSrcLoc();
    }
}
