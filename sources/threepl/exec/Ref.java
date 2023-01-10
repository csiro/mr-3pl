package threepl.exec;

import static threepl.ThreePL.tdelist;

import java.util.Iterator;

import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.nodes.Ident;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * This class represents a LHS variable reference. It is generally used
 * for the LHS of an assignment.
 *
 * This class extends class RefOrVal which contains fields shared
 * by this class and the similar Val class.
 *
 * Flag flag; -  a flag indicating if this variable reference
 * has a pre or post increment or decrement operation associated with it.
 * 
 * TDEVar select; - a temporary variable holding a signal representing a decoded
 * target subscript value for a partial reference.
 *
 * Ident id; - an identifier used for arguments passed to modules, procedures
 * and functions. It allows procedures to be written which can create
 * variables using the identifier of the argument.
 */
public final class Ref extends RefOrVal implements Constant {
    private Flag    flag;   // ++, -- or * flag in this reference?
    private TDEVar  select; // select signal
    private Ident   id;     // identifier
 
    /**
     * Construct a reference for an undefined variable.
     */
    public Ref () {
    }

    /**
     * Construct a reference to an immediate variable occurrence.
     * @param   var is the variable
     * @param   type is the variable type
     * @param   sfl is the subscript/field list
     * @param   flag indicates ++, -- or *
     * @param   is_val indicates that we want a value rather than a reference
     * @param   loc is the source file location
     */
    public Ref (Var var, Type type, SubFieldList sfl, Flag flag, boolean is_val, SrcLoc loc) {
        mode = Mode.IMMEDIATE;
        this.var = var;
        this.flag = flag;
        this.loc = loc;
        wordspec = type.getWordSpec(var, sfl, loc);
        subfields = (sfl == null) ? null : (SubFieldList) sfl.clone();      // copy the sfl
        indsubfields = (sfl == null) ? null : (SubFieldList) sfl.clone();
        if (wordspec.numWords() == 0)
            return;
        Ptype   pt = type.getPrimType();
        if (((pt == Ptype.MAP) || (pt == Ptype.LIST) || (pt == Ptype.CLASS)) && (flag != Flag.NONE))
            throw new ExEx("list, map or class reference cannot have ++ or --", loc);
    }
    
    /**
     * Construct a reference to a target variable occurrence.
     * @param   v is the variable
     * @param   m is the variable mode
     * @param   subs is the subscript/field list
     * @param   tdev is the TDEVar for the variable occurrence
     * @param   loc is the source file location
     */
    public Ref (Var v, Mode m, SubFieldList subs, TDEVar tdev, SrcLoc loc) {
        mode = m;
        if ((v.getMode() != Mode.CMEMORY) && (v.getMode() != Mode.RMEMORY))
            var = v;
        wordspec = tdev.getWordSpec();
        subfields = (subs == null) ? null : (SubFieldList) subs.clone();
        tdevar = tdev;
        andSetQueues(wordspec.getSQueues(), loc);
        flag = Flag.NONE;
        this.loc = loc;
        // get any target subscript variables
        for (Val val : wordspec.getSubVals()) {
            if (val != null)
                addOVar(val.getVar());
        }
    }
    
    /**
     * Construct a reference to a memory variable occurrence.
     * @param   v is the variable
     * @param   loc is the source file location
     */
    public Ref (Var v, SrcLoc loc) {
        mode = v.getMode();
        var = v;
        subfields = new SubFieldList();
        flag = Flag.NONE;
        this.loc = loc;
    }
    
    /**
     * Return a TDEVar for one word out of this target reference.
     * @param   ws is the word specification for the required word
     * @param   word is the index of the required word
     * @param   loc is the source file location
     * @return  the Ref containing the extracted TDEVar for one word
     */
    public TDEVar getWordTDEVar (WordSpec ws, int word, SrcLoc loc) {
        if (mode == Mode.IMMEDIATE)
            throw new ExEx("cannot reference a constant", loc);
        return(tdevar.getWord(ws, word, loc));
    }
   
    /**
     * Get the pre/post increment/decrement, indirect, examine code for this
     * variable reference.
     * @return  the flag code
     */
    public Flag getFlag() {
        return(flag);
    }
    
    /**
     * Set the pre/post increment/decrement, indirect, examine code for this
     * variable reference.
     * @param  f is the flag code
     */
    public void setFlag(Flag f) {
        flag = f;
    }
    
    /**
     * Get the select TDEVar for a target variable subscript partial Ref.
     * @return  the select signal
     */
    public TDEVar getSelect () {
        return(select);
    }
    
    /**
     * Get the value of a variable given the reference.
     * @param   loc is the source file location
     * @return  the variable value
     */
    public Val getVal (SrcLoc loc) {
        return(var.getVal(subfields, flag, loc));
    }
     
    /**
     * Get the queue write availability signal as a logical <b>Val</b>.
     * @return  the queue write availability signal Val
     */
    public Val getWriteAvailVal () {
        if ((mode == Mode.IMMEDIATE) || (mode == Mode.STATIC))
            return(new Val(true, loc));
        if ((mode == Mode.CMEMORY) || (mode == Mode.RMEMORY))
            throw new ExEx("unary > operand is mode memory", loc);
        if (var == null)
            throw new ExEx("unary > operand is not a variable", loc);
        if ((queues == null) || queues.isEmpty())
            return(new Val(true, loc));  // no queues - return immediate true

        if (!queues.isSingleWrite())
            throw new ExEx("unary > operand has queue subscript(s)", loc);

        Queue       q = (Queue)var;
        WordSpec    ws = new WordSpec(1, Ptype.LOG);
        TDEVar      tdev = tdelist.signal("WAV", ws, loc);
        Val val = new Val(null, Mode.VALUE, tdev, loc);
        tdelist.connect(tdev, q.getWriteAvailSig(loc));
        val.addOVar(q.getQueueWriteClkVar());
        return(val);
    }
    
    /**
     * Execute an immediate mode assignment to this variable reference
     * @param   asstype  is the immediate assignment operator type.
     * @param   rval is the RHS value to be assigned
     * @param   loc is the source file location
     */
    public void assignTo (AST asstype, Val rval, SrcLoc loc) {
        var.assignTo(asstype, this, rval, loc);
    }
    
    /**
     * Execute a value, output or priority mode assignment to this variable reference.
     * This is the general case - if the RHS is also a value
     * variable use method valValAssignTo() instead.
     * @param   rval is the RHS value to be assigned
     * @param   loc is the source file location
     */
    public void assignTo(Val rval, SrcLoc loc) {
        var.assignTo(this, rval, loc);
    }
    
    /**
     * Execute an output mode assignment to this variable reference
     * @param   rval is the RHS value to be assigned
     * @param   exec is the execute signal for the assignment
     * @param   loc is the source file location
     */
    public void assignTo(Val rval, TDEVar exec, SrcLoc loc) {
        var.assignTo(this, rval, exec, loc);
    }
    
    /**
     * Execute a static or queue mode assignment to this variable reference
     * @param   rval is the RHS value to be assigned
     * @param   exec is the execute signal for the assignment
     * @param   asstype is the assignment type
     * @param   toplevel is true if this is the top level in a module
     * @param   loc is the source file location
     */
    public void assignTo(
        Val     rval,
        TDEVar  exec,
        AST     asstype,
        boolean toplevel,
        SrcLoc  loc) {
        var.assignTo(this, rval, exec, asstype, toplevel, loc);
    }
    
    /*
     * Execute a value mode assignment to this variable reference
     * from a RHS value variable.
     * @param   rvar is the RHS value variable to be assigned
     * @param   subs is the subscript/field description for the occurrence
     * @param   loc is the source file location
    public void valValAssignTo(Var rvar, NodeList subs, SrcLoc loc) {
        var.valValAssignTo(this, rvar, subs, loc);
    }
     */
    
    /**
     * Set the identifier field.
     * @param   id is the identifier
     */
    public void setId (Ident id) { this.id = id; }
    
    /**
     * Get the identifier field.
     * @return  the identifier
     */
    public Ident getId () { return(id); }

    /*
     * Find the key to the nth entry in a sorted map.
     * If the index is out-of-bounds return null.
     * @param   tm is the sorted map
     * @param   index is the index
     * @return  the entry key or null
    private String getEntryKey (TreeMap tm, int index) {
        if ((index < 0) || (index >= tm.size()))
            return(null);
        int         i = 0;
        String      key;
        Set         ks = tm.keySet();
        Iterator    it = ks.iterator();
        while (it.hasNext()) {
            key = (String)it.next();
            if (i == index)
                return(key);
            else
                i++;
        }
        return(null); // to keep compiler happy - actually never get here
    }
     */
    
    /**
     * Creates an iterator for extracting partial variable references
     * for LHS target variable subscripts. The iterator extracts
     * successive WordSpec runs of bit pairs with the same select signal
     * and wraps them in a new Ref.
     * @return  an iterator
     */
    public Iterator<?> targSubIterator () {
        return(new TargSubIterator());
    }


    // This class implements an iterator for extracting partial variable
    // references for LHS target variable subscripts.
    private class TargSubIterator implements Iterator<Object> {
        private boolean     avail;
        private int         lower_word_index;
        private int         upper_word_index;
        private TDEVar      sel;

        public TargSubIterator () {
            avail = true;
            lower_word_index = 0;
            int     i = lower_word_index + 1;
            int     lim = wordspec.numWords();
            sel = wordspec.getSelect(lower_word_index);
            while ((i < lim) && (wordspec.getSelect(i) == sel))
                i++;
            upper_word_index = i - 1;
        }

        // Implements java.Iterator.hasNext() for partial Refs.
        public boolean hasNext () {
            return(avail);
        }

        // Implements java.Iterator.next() for partial Refs.
        public Object next () {
            WordSpec    ws = new WordSpec();
            ws.setCheckType(wordspec.getCheckType());
            ws.setDimDes(wordspec.getDimDes());
            for (int i=lower_word_index ; i<=upper_word_index ; i++) {
                int     word = wordspec.getWord(i);
                int     lower = wordspec.getLower(i);
                int     upper = wordspec.getUpper(i);
                Type    type = wordspec.getType(i);
                ws.append(word, lower, upper, type);
            }
            TDEVar  tdev = TDEVar.makeTDEVar(tdevar.getId(), ws, loc);
            Ref     ref = new Ref(var, mode, null, tdev, loc);
            ref.flag = flag;
            ref.loc = loc;
            ref.select = sel;
            if (upper_word_index == (wordspec.numWords() - 1))
                avail = false;
            else {
                lower_word_index = upper_word_index + 1;
                int     i = lower_word_index + 1;
                int     lim = wordspec.numWords();
                sel =  wordspec.getSelect(lower_word_index);
                while ((i < lim) && (wordspec.getSelect(i) == sel))
                    i++;
                upper_word_index = i - 1;
            }
            return(ref);
        }

        public void remove () {
        }
    }
}
