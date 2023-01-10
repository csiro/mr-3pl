package threepl.nodes;

import java.util.ArrayList;
import java.util.Iterator;

import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.QueueRefs;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;
import threepl.parser.Token;

/**
 * This class is the super class of parser tree nodes.
 * Each node has a list of nodes below it in the tree.
 * This list is a NodeList, which extends ArrayList.
 * Each node also has a pointer to the node above it.
 * A source location (file name, line number and column
 * number) is also included in a SrcLoc instance.
 *
 * <p>Nodes have either a <b>getVal()</b> or an <b>execute()</b>
 * method. Nodes for variables (VarNode), expressions (ExprNode)
 * and function calls (FuncCallNode) use the <b>getVal()</b> method to
 * return a value. Expressions return a single primitive type value but
 * variable instances or function calls may also return compound values.
 * All other tree nodes, such as assignment statements, module and
 * procedure calls or control structures, use the <b>execute()</b>
 * method since they do not return a value.
 *
 * <p>The <b>getVal()</b> method is passed an execute signal. This is
 * a single bit TDEVar control signal that is asserted when the value
 * is used during execution in the FPGA. It is used to generate the logic
 * for queue acknowledgements.
 *
 * <p>The <b>execute()</b> method is passed empty lists in which both
 * start execution signals and finish execution signals can be returned
 * from enclosed nodes. This scheme allows for nested immediate loops
 * which generate multiple target statements whose serial/parallel
 * execution is determined back up the tree in an enclosing seq or par
 * block. The lists allow the decision as to how to link the execution of
 * these target statements to be postponed until the return to the
 * enclosing seq or par block. This method returns an enumerated type which
 * indicates if a break, continue or return statement has been encountered
 * (see {@link threepl.parser.Constant});
 *
 * <p>Note that immediate constructs must nevertheless adhere to
 * this start/finish signal scheme as they may contain embedded
 * target code. Immediate constructs which simply return the start signal
 * as the finish signal are recognised as having generated no target execution
 * code and the execution signals have no inserted delays and simply pass
 * through unchanged.
 */
public abstract class Node implements Constant {
    protected NodeList  subnodes;   // nodes below this in the tree
    protected Node      supernode;  // the node above this in the tree
    protected SrcLoc    loc;        // the source location

    /**
     * This constructor creates an unlinked node with location
     * SrcLoc taken from a token passed as the argument.
     * @param   t is a token from which the source location can be obtained
     */
    public Node (Token t) {
        if (t != null)
            loc = new SrcLoc(t);
        subnodes = new NodeList(loc);
    }

    /**
     * This constructor creates an unlinked node with location
     * SrcLoc passed as the argument.
     * @param   l is the source location
     */
    public Node (SrcLoc l) {
        subnodes = new NodeList(l);
        loc = l;
    }
    
    /**
     * Get the subnode list, or null if a leaf node.
     * @return  subnode list
     */
    public NodeList getSubNodes () {
        return(subnodes);
    }
    
    /**
     * Get the number of subnodes.
     * @return  the number of subnodes
     */
    public int getNumSubNodes () {
        return(subnodes.size());
    }
    
    /**
     * Get a single subnode.
     * @param   i the subnode index
     * @return  subnode
     */
    public Node getSubNode (int i) {
        return(subnodes.get(i));
    }
    
    /**
     * Get a subnode which is a ListNode containing a NodeList.
     * @param   i the subnode index
     * @return  node list
     */
    public NodeList getListSubNode (int i) {
        ListNode    ln = (ListNode)subnodes.get(i);
        return(ln.getNodeList());
    }
    
    /**
     * Get the number of nodes in a subnode which is itself a NodeList.
     * @param   i the subnode index
     * @return  the number of nodes in the subnode list
     */
    public int getNumListSubNodes (int i) {
        ListNode    ln = (ListNode)subnodes.get(i);
        NodeList    nl = ln.getNodeList();
        return(nl.size());
    }
    
    /**
     * Add a subnode. The subnode as added at the end of
     * the subnode list.
     * @param   sub the subnode to be added
     */
    public void addSubNode (Node sub) {
        subnodes.add(sub);
        if (sub != null)
            sub.setSupNode(this);
    }
    
    /**
     * Add a list of subnodes. The subnodes are added at the end of
     * the subnode list.
     * @param   subs the subnode list to be added
     */
    public void addSubNodes (NodeList subs) {
        if (subs == null)
            return;
        Iterator<?>    it = subs.iterator();
        subnodes.addAll(subs);
        while (it.hasNext())
            ((Node)it.next()).setSupNode(this);
    }
    
    /**
     * Get the super node (the one above this in the code tree).
     * @return  the super node
     */
    public Node getSupNode () {
        return(supernode);
    }
    
    /**
     * Set the super node.
     * @param   n the super node
     */
    public void setSupNode (Node n) {
        supernode = n;
    }
    
    /**
     * Get the source location information from this node.
     * @return  the location
     */
    public SrcLoc getSrcLoc () {
        return(loc);
    }
    
    /**
     * Set the source location for this node.
     * @param   loc is the location
     */
    public void setSrcLoc (SrcLoc loc) {
        this.loc = loc;
    }
    
    /**
     * Method to return the identifier of a variable. This method is
     * overridden in VarNode and PtrValNode. Here it returns null as
     * the node is not one of the above.
     * @return  null
     */
    public Ident getIdentifier() {return(null);}
    
    /**
     * Method to return the identifier of a variable. This method is
     * overridden in VarNode and PtrValNode. Here it throws an exception
     * the node is not one of the above.
     * @param   mess is a string to prepend to the fatal error message
     * @return  does not return - throws an exception
     */
    public Ident getIdentifier(String mess) {
        throw new ExEx(mess + " is not an identifier", loc);
    }
    
    /**
     * Method to return the identifier of a variable. This method is
     * overridden in VarNode and PtrValNode. Here it throws an exception
     * if the node is not one of the above.
     * @param   allow_subs_fields_flags when true skips the
     *          subscript/field/flags check
     * @param   mess is a string to prepend to the fatal error message
     * @return  does not return - throws an exception
     */
    public Ident getIdentifier(boolean allow_subs_fields_flags, String mess) {
        throw new ExEx(mess + " is not an identifier", loc);
    }
    
    /**
     * Execute a node which represents a statement or control structure.
     *
     * <p>This method MUST be overridden by the subclass, hence the exception!
     * @param   esig is an exception/restart signal, or null
     * @param   startl is a list of start signals for target statements
     *          below this node
     * @param   finishl is a list of finish signals for target statements
     *          below this node
     * @param   queues returns all the queue availability signals
     *          accumulated from code below
     * @param   toplevel is true if this is the top level in a module
     * @param   availok is true if a previous sync makes a queue availability
     *          wait unnecessary
     * @param   checkedqueues gives queue reads which have already been checked
     * @param   pri_in is an optional input signal to a priority encoder
     * @param   pri_out is an optional output signal from a priority encoder
     * @return  the execution status value
     */
    public EXECR execute (
        TDEVar              esig,
        ArrayList<TDEVar>   startl, 
        ArrayList<TDEVar>   finishl,
        QueueRefs           queues,
        boolean             toplevel,
        boolean             availok,
        QueueRefs           checkedqueues,
        TDEVar              pri_in,
        TDEVar              pri_out
    ) {
        throw new ExEx("CODE ERROR - " + getClass().getName() + ".execute() called");
    }
    
    /**
     * Evaluate a node which represents a constant, variable or expression,
     * returning a value. For target expressions a 'queues' QueueRefs is
     * returned within the TDEVar in the method return value, so we do not
     * need to pass a QueueRefs argument as we did in 'execute()' above.
     *
     * <p>This method MUST be overridden by the subclass, hence the exception!
     * @return  the value
     */
    public Val getVal () {
        throw new ExEx("CODE ERROR - " + getClass().getName() + ".getVal() called");
    }

    /**
     * Evaluate a node which represents a single immediate integer constant,
     * variable or expression, returning the value as an int.
     * @param   mess is a string to be printed ahead of any error message
     * @param   loc is the source file location
     * @return  the value
     */
    public int getSingleIval (String mess, SrcLoc loc) {
        Val     v = getVal();
        Ptype   t = v.getPrimType();
        if ((v == null) || (t != Ptype.UINT) && (t != Ptype.INT) || v.isTarget())
            throw new ExEx(mess + " not an immediate integer", loc);
        return((int)v.getSingleIval(loc));
    }

    /**
     * Evaluate a node which represents a single floating point constant,
     * variable or expression, returning the value as a double.
     * @param   mess is a string to be printed ahead of any error message
     * @param   loc is the source file location
     * @return  the value
     */
    public double getSingleFval (String mess, SrcLoc loc) {
        Val     v = getVal();
        Ptype   t = v.getPrimType();
        if ((v == null) || (t != Ptype.FLOAT) || v.isTarget())
            throw new ExEx(mess + " not an immediate floating point value", loc);
        return(v.getSingleFval(loc));
    }

    /**
     * Evaluate a node which represents a single immediate logical constant,
     * variable or expression, returning the value as a boolean.
     * @param   mess is a string to be printed ahead of any error message
     * @param   loc is the source file location
     * @return  the value
     */
    public boolean getSingleLval (String mess, SrcLoc loc) {
        Val     v = getVal();
        if ((v == null) || (v.getPrimType() != Ptype.LOG) || v.isTarget())
            throw new ExEx(mess + " not an immediate logical", loc);
        return(v.getSingleLval(loc));
    }

    /**
     * Evaluate a node which represents a single immediate string constant,
     * variable or expression, returning the value as a String.
     * @param   mess is a string to be printed ahead of any error message
     * @param   loc is the source file location
     * @return  the value
     */
    public String getSingleSval (String mess, SrcLoc loc) {
        Val     v = getVal();
        if ((v == null) || (v.getPrimType() != Ptype.STR) || v.isTarget())
            throw new ExEx(mess + " not an immediate string", loc);
        return(v.getSingleSval(loc));
    }
    
    /**
     * Evaluate a node which represents a variable returning a reference.
     * For target expressions a 'queues' QueueRefs is returned within the
     * TDEVar in the method return value.
     * @param   mess is a string to be printed ahead of any error message
     * @return  the reference
     */
    public Ref getRef (String mess) {
        if (!(this instanceof VarNode))
            throw new ExEx(mess + " not a variable", loc);
        VarNode vn = (VarNode)this;
        return(vn.getRef());
    }
    
    /**
     * Determine if this node is an input parameter to a module or
     * procedure. This method is overridden by subclass VarNode but for
     * all other nodes returns false.
     * @return  false
     */
    public boolean isInputPar () {
        return(false);
    }
    
    /**
     * Determine if this node is an output parameter to a module or
     * procedure. This method is overridden by subclass VarNode but for
     * all other nodes returns false.
     * @return  false
     */
    public boolean isOutputPar () {
        return(false);
    }
    
    /**
     * Determine if this node is a matched parameter to a module or
     * procedure. This method is overridden by subclass VarNode but for
     * all other nodes returns false.
     * @return  false
     */
    public boolean isMatched () {
        return(false);
    }

    /**
     * Get the exception value if any. This is called in BlocNode.java to
     * modify the ILOOP logic surrounding a WhenNode, TargWhileNode or
     * TargDOWhileNode. This method is overridden in those classes and returns
     * null in all other.
     * @return  the exception value
     */
    public Val getExcepVal () {
        return(null);
    }

    /**
     * Get the exception code block if any. This is called in BlocNode.java to
     * modify the ILOOP logic surrounding a WhenNode, TargWhileNode or
     * TargDOWhileNode. This method is overridden in those classes and returns
     * null in all other.
     * @return  the exception code block
     */
    public Node getExcepBlock () {
        return(null);
    }
    
    /**
     * Set the direct execution flag.
     */
    public void setDirect () {
        throw new ExEx("SYSTEM ERROR! - NO setDirect() FROM THIS NODE", loc);
    }
    
    /**
     * Get the direct execution flag. This method is overridden in
     * TargAssNode.java and PtrValNode.java but otherwise returns false.
     * @return  the direct execution flag
     */
    public boolean getDirect () {
        return(false);
    }

    /**
     * Set the pre/post increment/decrement flag for the variable pointed to.
     * @param   f is one of the codes Flag.NONE, Flag.PREINCR, Flag.PREDECR,
     *          Flag.POSTINCR or Flag.POSTDECR
     */
    public void setFlag (Flag f) {
        throw new ExEx("SYSTEM ERROR! - NO setFlag() FROM THIS NODE", loc);
    }
    
    /**
     * Get the flag code for this variable occurrence
     * @return  the flag code 
     */
    public Flag getFlag () {
        throw new ExEx("SYSTEM ERROR! - NO getFlag() FROM THIS NODE", loc);
    }
}
