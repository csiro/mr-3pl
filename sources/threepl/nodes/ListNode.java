package threepl.nodes;

import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * This is a node which is used to construct a list of nodes as
 * a single subnode to a parent node. This is needed for module,
 * procedure and function argument lists where the input arguments
 * and output arguments are separate lists of nodes which are the
 * 1st and 2nd subnodes respectively of the call node.
 */
public final class ListNode extends Node implements Constant {
    NodeList    nodelist;
    
    /**
     * Construct an empty node list node.
     * @param   l is the file source location
     */
    public ListNode (SrcLoc l) {
        super(l);
    }
    
    /**
     * Construct a node list node.
     * @param nl is the contained node list
     */
    public ListNode (NodeList nl) {
        super(nl.getSrcLoc());
        nodelist = nl;
    }
    
    /**
     * Append a node to the node list contained within this node list node.
     * @param n the node to be appended
     */
    public void addNode (Node n) {
        if (nodelist == null)
                nodelist = new NodeList(loc);
        nodelist.add(n);
        if (n instanceof KeyMatchNode)
            nodelist.setKeyMatch();
    }
    
    /**
     * Get the node list contained within this node list node.
     * @return the node list
     */
    public NodeList getNodeList () {
        return(nodelist);
    }
}
