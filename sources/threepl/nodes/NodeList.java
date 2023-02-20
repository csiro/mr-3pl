package threepl.nodes;

import java.util.ArrayList;
import java.util.TreeMap;

import threepl.exceptions.ExEx;
import threepl.exec.QueueRefs;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * This class is just a convenient extension to ArrayList to handle
 * lists of <b>Node</b>s and enables us to get entries without needing
 * a cast. It stores the source file location as well.
 */
@SuppressWarnings("serial")
public final class NodeList extends ArrayList<Node> implements Constant {
    private SrcLoc  loc;        // source file location
    private boolean keymatch;   // the list includes at least one keymatch node

    /**
     * Construct a node list.
     * @param   loc is a source file location
     */
    public NodeList (SrcLoc loc) {
        super();
        this.loc = loc;
        keymatch = false;
    }
    
    /**
     * Add a ListNode node to nest a NodeList.
     * @param   nl is a node list to be added
     */
    public void add (NodeList nl) {
        add(new ListNode(nl));
    }

    /**
     * Get a node from the node list.
     * @param   i is the index of the node required
     * @return  the Node
     */
    public Node getNode (int i) {
        return(super.get(i));
    }
    
    /**
     * Get the source file location.
     * @return  the source file location
     */
    public SrcLoc getSrcLoc () {
        return(loc);
    }
    
    /**
     * Get the call source file location.
     * @return  the source file location
     */
    public SrcLoc getCallLoc () {
        //if (size() == 0)
            return(loc);
        //return(getSupNode().getSrcLoc());
    }
    
    /**
     * Get an argument identifier. The argument must be an
     * identifier. It must not have subscripts or struct field identifiers
     * appended or pre/post increment/decrement or examine operators. No
     * maps are searched for this identifier. If the error message string
     * header is null, errors return null rather than throwing an exception.
     * @param   i is the argument index
     * @param   mess is string to be prepended to any error message
     * @return  the identifier
     */
    public Ident getIdent (int i, String mess) {return(getIdent(i, false, mess));}
    
    /**
     * Get an argument identifier. The argument must be an identifier.
     * It must not have pre/post increment/decrement operators. No maps are
     * searched for this identifier. If the error message string header is
     * null, errors return null rather than throwing an exception.
     * @param   i is the argument index
     * @param   allow_subs_fields_flags when true, skips the subscript/field check
     * @param   mess is string to be prepended to any error message
     * @return  the identifier
     */
    public Ident getIdent (int i, boolean allow_subs_fields_flags, String mess) {
        Node    n = get(i);
        if (mess != null)
            mess = mess + " input argument ";
        return(n.getIdentifier(allow_subs_fields_flags, mess));
    }
    
    /**
     * Evaluate a list member. If the item is missing,
     * null is returned. The value is checked for unbuffered queue reads
     * which result in an exception being thrown.
     * @param   i is the list member index
     * @return  the list member value
     */
    public Val getVal (int i) {
        return(getVal(i, false));
    }
    
    /**
     * Evaluate a list member. If the item is missing,
     * null is returned.
     * @param   i is the list member index
     * @param   aupr allows unbuffered queue reads in the item if true
     * @return  the list member value
     */
    public Val getVal (int i, boolean aupr) {
        if (i >= size())
            return(null);
        Node    n = get(i);
        if (n == null)
            return(null);
        Val val = n.getVal();
        if (val == null)
            return(null);
        QueueRefs    p = val.getQueues();
        p.unbufferedQueues(aupr, "unbuffered queue not allowed", loc);
        return(val);
    }
    
    /**
     * Evaluate a list member reference. If the item is missing,
     * null is returned.
     * @param   i is the list member index
     * @param   mess is a message to precede any error messages
     * @return  the list member reference
     */
    public Ref getRef (int i, String mess) {
        Node    n = get(i);
        if (n == null)
            return(null);
        return(n.getRef(mess + " input"));
    }
    
    /**
     * Determine if a list member is null. If the item is null,
     * true is returned.
     * @param   i is the list member index
     * @return  true if the list member is null
     */
    public boolean isNull (int i) {
        if (i >= size())
            return(true);
        return (get(i) == null);
    }
    
    /**
     * Sort the nodes in this NodeList when keymatch arguments are present.
     * @param mode is "module", "procedure" or "function" as appropriate and is used for exception text
     * @param keys is a map of expected keys and their parameter position in the list
     * @param allowed_attributes if true means that a key match argument may be an attribute assignment
     * @param nullcheck if true check for null inputs (error)
     * @param name is the identifier of the module, procedure or function and is used for exception text
     * @param isInput is true if this is an input node list
     */  
    public void sortKeys (String mode, TreeMap<String,Integer> keys, boolean allowed_attributes, String name, boolean nullcheck, boolean isInput) {
        if (!keymatch)
            return; // if no keymatch nodes leave list unsorted
        if (keys.isEmpty())
            return; // if no key mapping available leave list unsorted
        
        String      io = isInput?"input":"output";
        Node[]      nodes = new Node[size()];
        nodes = toArray(nodes);
        int         n = nodes.length;
        //Node[]      nn = new Node[keys.size()];
        ArrayList<Node> nn = new ArrayList<Node>();
        boolean     haveKeyMatches = false;
        int         i;
        String      key;
        int         kindex;
        
        for (i=0 ; i<keys.size() ; i++)
            nn.add(null);
        
        for (i=0 ; i<n ; i++) {
            // check for null arguments if required
            if ((nodes[i] == null) && nullcheck)
                throw new ExEx("procedure '" + name + "' has a null " + io + " argument", loc);
            if (!(nodes[i] instanceof KeyMatchNode)) {
                if (haveKeyMatches)
                    throw new ExEx("inbuilt " + mode + " '" + name + "' has a positional " + io +
                            " argument following a keymatch argument", loc);
                nn.set(i,  nodes[i]);
            } else {
                haveKeyMatches = true;
                if (keys.isEmpty())
                    throw new ExEx("inbuilt " + mode + " '" + name + "' does not allow " + io +
                            " keymatch arguments", loc);
                KeyMatchNode    kmn = (KeyMatchNode) nodes[i];
                key = kmn.getParamKey();
                if (key == null)
                    throw new ExEx("inbuilt " + mode + " '" + name + "' " + io + " argument key null", loc);
                if (!keys.containsKey(key))
                    if (!allowed_attributes) // must be parameter=argument only
                        throw new ExEx("inbuilt " + mode + " '" + name + "' " + io + " argument key '" + key + "' not recognised", loc);
                    else {
                        nn.add(nodes[i]); // assume is a property=value and add it to the end
                        continue;
                    }
                kindex = keys.get(key);
                if (nn.get(kindex) != null)
                    throw new ExEx("inbuilt " + mode + " '" + name + "' " + io + " argument key '" + key + "' already matched", loc);
                nn.set(kindex, kmn.getSubNode(0));
            }
        }
        clear();
        for (i=0 ; i<n ; i++) {
            add(nn.get(i));
        }
    }
    
    /**
     * Set the keymatch flag to indicate that at least one node in the list
     * is a KeyMatch node.
     */ 
    public void setKeyMatch() {
        keymatch = true;
    }
}
