package threepl.funcs;

import java.util.TreeMap;

import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.nodes.NodeList;

/**
 * This is the calling interface for inbuilt functions.
 */
public abstract class InbuiltFunc {
    public boolean                  check_null_args;
    public boolean                  allowed_attributes;
    public TreeMap<String,Integer>  ipnames;
    
    public InbuiltFunc () {
        ipnames = new TreeMap<String,Integer>();
        check_null_args = false;
        allowed_attributes = false;
    }

    /**
     * Call an inbuilt function and get the return value.
     * @param   args is a list of argument tree nodes
     * @return  the value resulting from the function call
     */
    public Val getVal (NodeList args) {
        return(null);
    }

    /**
     * Call an inbuilt function and get a returned reference.
     * @param   args is a list of argument tree nodes
     * @return  the reference resulting from the function call or null
     */
    public Ref getRef (NodeList args) {
        return(null);
    }
}
