package threepl.parser;


/**
 * This class implements a boolean expression node.
 * This node is used to construct expression trees
 * for boolean expressions used in LUTs.
 */
public class BENode implements Constant {
    Ttype   op;     // type of node
    int     argnum; // argument number if this node is an argument
    BENode  llink;  // left subtree or null
    BENode  rlink;  // right subtree or null

    public BENode (Ttype op) {
        this.op = op;
        argnum = 0;
        llink = null;
        rlink = null;
    }       

    public BENode (Ttype op, BENode l) {
        this.op = op;
        argnum = 0;
        llink = l;
        rlink = null;
    }       
}
