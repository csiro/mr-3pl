package threepl.nodes;

import static threepl.ThreePL.findVar;
import static threepl.ThreePL.isparsing;

import java.util.ArrayList;

import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exceptions.MapException;
import threepl.exec.QueueRefs;
import threepl.exec.Ref;
import threepl.exec.SubFieldList;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.parser.Constant;
import threepl.parser.ParserConstants;
import threepl.parser.SrcLoc;
import threepl.parser.Token;

/**
 * This Node class represents a variable occurrence, possibly with
 * subscripts and fields and a pointer token {@code'->'}.
 *
 * <p>Note that for this Node the first subnode is itself a NodeList
 * of subscript and field nodes. There is a second subnode if
 * this variable node is a pointer reference, the second subnode
 * being a NodeList of the subscript and field nodes to the right
 * of the {@code'->'}.
 *
 * <p>This class is also used for some inbuilt declaration procedure
 * arguments which look like identifiers in parsing but are treated
 * as strings. For example variable modes are passed as "queue"
 * etc having been parsed as variable identifiers and placed in a
 * VarNode class.
 */
public final class VarNode extends Node implements Constant, ParserConstants {
    private String  id;         // variable identifier
    private Flag    flag;       // may have a ++, --
    private Context context;    // scope context

    /**
     * Construct a variable node.
     * @param   id is the variable identifier
     * @param   context is the scope context
     * @param   nodelist is a (possibly empty) list of subscripts and
     *          fields for the variable occurrence
     * @param   t is the operator token, from which the file source location
     *          is extracted
     */
    public VarNode (String id, Context context, NodeList nodelist, Token t) {
        super(t);
        this.id = id;
        this.context = context;
        flag = Flag.NONE;
        NodeList    nl = getSubNodes();
        // Add the NodeList as an entry in the
        // subnodes NodeList (rather than concatenating its contents
        // into the subnodes NodeList).
        nl.add(nodelist);
    }
    
    public VarNode (String id) {
        super((SrcLoc)null);
        this.id = id;
    }
    
    /**
     * Set the pre/post increment/decrement code.
     * @param   f is one of the codes Flag.NONE, Flag.PREINCR, Flag.PREDECR,
     *          Flag.POSTINCR or Flag.POSTDECR
     */
    public void setFlag (Flag f) { flag = f; }
    
    /**
     * Get the flag code for this variable occurrence
     * @return  the flag code 
     */
    public Flag getFlag () { return(flag); }
    
    /**
     * Get the scope context for this variable occurrence.
     * @return  the context of the variable, Context.GLOBAL, Context.FILEMOD,
     *          Context.LOCAL or Context.DEFAULT
     */
    public Context getScopeContext () {
        return(context);
    }
    
    /**
     * Check that this node has no subscripts, fields or pre/post
     * increment/decrement and return the identifier. If the above checks
     * are not met, return null.
     * @return  the identifier or null
     */
    public Ident getIdentifier() {return(getIdentifier(false, null));}
    
    /**
     * Check that this node has no subscripts, fields or pre/post
     * increment/decrement operators and return the identifier. If the
     * above checks are not met, throw an exception with an error message.
     * @param   mess is a string to prepend to a fatal error message
     * @return  the identifier
     */
    public Ident getIdentifier(String mess) {return(getIdentifier(false, mess));}
    
    /**
     * Check that this node has no pre/post increment/decrement operators
     * and return the identifier. If the above checks are not met -
     * if 'mess' is null, return null.
     * if 'mess' is not null, throw an exception with an error message.
     * @param   allow_subs_fields_flags when true skips the
     *          subscript/field/flags check
     * @param   mess is a string to prepend to a fatal error message
     * @return  the identifier
     */
    public Ident getIdentifier(boolean allow_subs_fields_flags, String mess) {
        if (!allow_subs_fields_flags && ((getListSubNode(0).size() != 0) || (getNumSubNodes() > 1))) { 
            if (mess == null)
                return(null);
            else
                throw new ExEx(mess + " is not a single identifier", loc);
        }
        if (!allow_subs_fields_flags && (flag != Flag.NONE)) {
            if (mess == null)
                return(null);
            else
                throw new ExEx(mess + " has increment or decrement operator", loc);
        }
        return(getIdent());
    }
    
    /**
     * Get the identifier for this variable.
     * @return  the identifier for this variable
     */
    public String getId () {
        return(id);
    }
    
    /**
     * Get the Ident class for this variable.
     * @return  the Ident for this variable
     */
    public Ident getIdent () {
        return(new Ident(id, context));
    }
    
    /**
     * Get the <b>Var</b> for this variable.
     * @return  the identifier for this variable
     */
    public Var getVar () {
        return(findVar(id, context, loc));
    }

    /**
     * Get the value of the variable.
     * @return  the value of the variable (Var)
     */
    public Val getVal () {
        if (isparsing)
            throw new ExEx("cannot evaluate variable during parsing", loc);
        Var var = findVar(id, context, loc);
        if (var == null)
            throw new ExEx("undefined variable '" + id + "'", loc);
        Val     val = var.getVal(this, getListSubNode(0), loc);
        return(val);
    }

    /**
     * Get the value of the variable, ignoring pre/post increment/decrement
     * flags.
     * @return  the value of the variable (Var)
     */
    public Val getValNoFlags () {
        if (isparsing)
            throw new ExEx("cannot evaluate variable during parsing", loc);
        Var var = findVar(id, context, loc);
        if (var == null)
            throw new ExEx("undefined variable '" + id + "'", loc);
        Val     val = var.getVal(new SubFieldList(getListSubNode(0), loc), Flag.NONE, loc);
        return(val);
    }

    /**
     * Get a reference to the variable.
     * @return  the reference for the variable (Ref)
     */
    public Ref getRef () {
        Var     var = findVar(id, context, loc);
        
        if (var == null)
            throw new ExEx("undefined variable '" + id + "'", loc);
        
        Ref ref = var.getRef(this, getListSubNode(0), loc);
        return(ref);
    }

    /**
     * Get a reference to the variable. If not found, return null.
     * @return  the reference for the variable (Ref) or null
     */
    public Ref getRefOrNull () {
        Var     var = findVar(id, context, loc);
        
        if (var == null)
            return(null);
        
        Ref ref = var.getRef(this, getListSubNode(0), loc);
        return(ref);
    }

    /**
     * Get a reference to the variable and the identifier.
     * If the variable is not found, all fields will be null
     * except the id field.
     * @return  the reference for the variable (Ref)
     */
    public Ref getRefAndIdent () {
        Ref ref;
        Var var = findVar(id, context, loc);
        
        if (var == null)
            ref = new Ref();
        else try {
            ref = var.getInRef(this, getListSubNode(0), loc);
        } catch (MapException me) {
            return(null);
        }
        ref.setId(new Ident(id, context));
        return(ref);
    }
    
    /**
     * Determine if this variable node is an input parameter to a
     * module or procedure.
     * @return  true if this is an input parameter
     */
    public boolean isInputPar () {
        Var     var = findVar(id, context, loc);
        
        if (var == null)
            throw new ExEx("undefined variable '" + id + "'", loc);
        
        return(var.isInputPar());
    }
    
    /**
     * Determine if this variable node is an output parameter to a
     * module or procedure.
     * @return  true if this is an output parameter
     */
    public boolean isOutputPar () {
        Var     var = findVar(id, context, loc);
        
        if (var == null)
            throw new ExEx("undefined variable '" + id + "'", loc);
        
        return(var.isOutputPar());
    }
    
    /**
     * Determine if this variable node is a matched parameter to a
     * module or procedure.
     * @return  true if this is a matched parameter
     */
    public boolean isMatched () {
        Var     var = findVar(id, context, loc);
        
        if (var == null)
            throw new ExEx("undefined variable '" + id + "'", loc);
        
        return(var.isMatched());
    }
    
    /**
     * Determine if this variable node is an unmatched output parameter to a
     * module or procedure.
     * @return  true if this is an unmatched output parameter
     */
    public boolean isUnmatchedOutputPar () {
        Var     var = findVar(id, context, loc);
        
        if (var == null)
            throw new ExEx("undefined variable '" + id + "'", loc);
        
        return(var.isOutputPar() && !var.isMatched());
    }
    
    /**
     * Execute a variable appearing as a statement - usually ++v, v++,
     * --v or v--.
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
     * @return  EXECR execution status value
     */
    public EXECR execute (
        TDEVar              esig,
        ArrayList<TDEVar>   startl, 
        ArrayList<TDEVar>   finishl,
        QueueRefs            queues,
        boolean             toplevel,
        boolean             availok,
        QueueRefs            checkedqueues,
        TDEVar              pri_in,
        TDEVar              pri_out
    ) {
        Var     var = findVar(id, context, loc);
        
        if (var == null)
            throw new ExEx("undefined variable '" + id + "'", loc);
        
        switch (var.getMode()) {
        case IMMEDIATE:
            getVal();
            break;
        case STATIC:
            Flag    flag_save = flag;
            flag = Flag.NONE;
            staticIncrDecrStat(
                esig,
                startl, 
                finishl,
                queues,
                toplevel,
                availok,
                checkedqueues,
                pri_in,
                pri_out,
                this,
                var,
                flag_save,
                loc
            );
            flag = flag_save;
            break;
        default:
            throw new ExEx("variable '" + id +
                "' has ++ or -- operator but is not immediate or static", loc);
        }
        return(EXECR.NONE);
    }
    
    static public void staticIncrDecrStat (
        TDEVar              esig,
        ArrayList<TDEVar>   startl, 
        ArrayList<TDEVar>   finishl,
        QueueRefs            queues,
        boolean             toplevel,
        boolean             availok,
        QueueRefs            checkedqueues,
        TDEVar              pri_in,
        TDEVar              pri_out,
        VarNode             vn,
        Var                 var,
        Flag                flag,
        SrcLoc              loc
    ) {
        Node    assign;
        Node    one = new IntNode(1);
        Node    plusorminus;
        if ((flag == Flag.PREINCR) || (flag == Flag.POSTINCR))
            plusorminus = new ExprNode(TreeOp.ADD, vn, one, null);
        else if ((flag == Flag.PREDECR) || (flag == Flag.POSTDECR))
            plusorminus = new ExprNode(TreeOp.SUB, vn, one, null);
        else
            throw new ExEx("SYSTEM ERROR!", loc);
        plusorminus.setSrcLoc(loc);
        assign = new TargAssNode(vn, plusorminus);
        assign.execute(esig, startl, finishl, queues, toplevel, availok, checkedqueues, pri_in, pri_out); // will return 0
    }

    /**
     * Determine if this variable occurence is a pointer reference.
     * @return  true if this is a pointer reference
     */
    public boolean isPtrRef () {
        return(subnodes.size() == 2);
    }
}
