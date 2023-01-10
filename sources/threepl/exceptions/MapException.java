package threepl.exceptions;

/**
 * A map entry missing exception.
 */
public class MapException extends RuntimeException {    

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * Construct an error exception with a message but no source file location.
     */
    public MapException () {
        super();
    }
}
