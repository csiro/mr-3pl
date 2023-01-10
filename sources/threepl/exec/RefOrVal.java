package threepl.exec;

import static threepl.ThreePL.getCurrentClockVar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Var.IDtype;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * This is the super class for Ref (variable reference) or Val (value).
 *
 * Class fields --
 *
 * Mode mode; - the mode of the reference or value. For a target
 * expression it will be VALUE mode.
 *    
 * WordSpec wordspec; - the word specifier.
 *
 * SubFieldList subfields; - a list of subscripts or fields
 * following a variable.
 *
 * subFieldList indsubfields; - a list of subscripts or fields
 * following an indirect operator.
 *
 * SrcLoc loc; - the source file location.
 *
 * Var var; - the variable if the reference or value is for a
 * single variable (not an expression).
 *
 * TDEVar tdevar; - the TDE variable if a target reference or
 * value.
            
 * String pkey; - a key used for matching an argument to a
 * parameter in a module, procedure or function call when this form
 * of argument passing is used.
 *
 * {@code HashSet<Object> ovars;} - a set of source target variables (or
 * VALUE mode/WordSpec pairs) collectively contributing to the output
 * clock domain of a value, including target subscripts. This set is
 * checked to determine the output clock domains and ensure that that
 * clock domain of all variables in the set is the same.
 *
 * {@code HashSet<Var> ivars;} - a set of destination variables to be
 * changed by assignment. This set is checked to determine the input
 * clock domains and ensure that that clock domain of all variables in
 * the set is the same.
 *
 * boolean clks_resolved; - this is true if both the input and
 * output clock domains have been checked and match (see method
 * resolveClocks()).
 *
 * Clock clkvar; - the resolved clock domain for this reference
 * or value, determined by method resolveClocks().
 *
 * QueueRefs queues; - information on any queues read or written.
 *
 * {@code ArrayList<HashSet<TDEVar>> execs;} - a list containing a number of sets. Each inner set
 * is associated with a combinatorial memory read or a used() function.
 * When a Val is evaluated for a target assignment the execution signal
 * for that assignment is added to all inner sets in the 'execs' list
 * of that Val. The field is also carried by a Ref so it can be transferred
 * to a Val derived from that Ref. The inner set propagated from a used()
 * function will only have a single execute signal added. Where a combinatorial
 * output memory read is used multiple times via a Value variable the inner
 * set propagated from that read will have multiple execute signals. Where
 * an expression contains both a used() function call and multiple combinatorial
 * output memory reads the 'execs' list will have several sets.
 */
public abstract class RefOrVal implements Constant, TDEConstants {
    protected Mode                          mode;   // immediate, value, static, queue etc.
    protected WordSpec                      wordspec;
    protected SubFieldList                  subfields;
    protected SubFieldList                  indsubfields;
    protected SrcLoc                        loc;    // source file location
    protected Var                           var;    // the variable if a variable node
    protected TDEVar                        tdevar; // TDE variable if target reference or value
    protected String                        pkey;   // key for param/arg match
    protected HashSet<Object>               ovars;
    protected HashSet<Var>                  ivars;
    protected boolean                       clks_resolved;
    protected Clock                         clkvar;
    protected QueueRefs                     queues;
    protected ArrayList<HashSet<TDEVar>>    execs;
    
    /**
     * Class to wrap a Value/WordSpec pair.
     */
    protected class ValWordSpecPair {
        public Value    val;
        public WordSpec ws;
        
        public ValWordSpecPair(Value v, WordSpec ws) {
            val = v;
            this.ws = ws;
        }
    }
    
    /**
     * Determine if this is a target mode.
     * @return  true if this is a target mode
     */
    public boolean isTarget () {
        return(mode != Mode.IMMEDIATE);
    }
    
    /**
    * Get the number of target subscripts for a variable value or
    * reference. Zero is returned if this is an expression value
    * or variable reference or value with no subscripts.
    * @return   the number of target subscripts
    */
    public boolean hasTargetSubs () {
        return(wordspec.getNumTargSubs() > 0);
    }

    /**
     * Determine the mode of the reference or value
     * (Mode.IMMEDIATE, Mode.VALUE, Mode.STATIC, Mode.QUEUE or Mode.STATE).
     * @return  the mode
     */
    public Mode getMode () {
        return(mode);
    }
    
    /**
     * Get the WordSpec word specifier for this reference or value.
     * @return  the word specifier
     */
    public WordSpec getWordSpec () {
        return(wordspec);
    }
    
    /**
     * Get the number of words in this reference or value.
     * @return  the number of words
     */
    public int numWords () {
        return(wordspec.numWords());
    }

    /**
     * Get the number of bits.
     * @return  the number of bits
     */
    public int getSingleNumBits () {
        return(tdevar.numBits());
    }
    

    /**
     * Determine if this reference or value is a primitive type.
     * @return  true if this is a primitive type
     */
    public boolean isPrimitive() {
        if (wordspec == null)
            return(true);
        if (wordspec.numWords() > 1)
            return(false);
        if (wordspec.getDimDes() != null)
            return(false);
        return(wordspec.getCheckType().isPrimitive());
    }
    
    /**
     * Get the primitive type of this reference or value.
     * If the type is compound, 0 is returned.
     * @return  the primitive type code (int)
     */
    public Ptype getPrimType () {
        if ((wordspec == null) || (wordspec.getDimDes() != null))
            return(Ptype.NONE);
        return(wordspec.getCheckType().getPrimType());
    }

    /**
     * For a reference or value which is a single primitive type,
     * change that type. There is no check that this is indeed
     * a primitive type. This method is only used in one place (Val.toString()).
     * @param   t is the new primitive type
     */
    public void setPrimType (Ptype t) { wordspec.setCheckType(new Type(t, 0)); }
    
    /**
     * Get the primitive width of this reference or value.
     * @return  the primitive type width
     */
    public int getPrimWidth () {
        if ((wordspec == null) || (wordspec.getDimDes() != null))
            return(0);
        return(wordspec.getCheckType().getWidth());
    }
     
    /**
     * Get the primitive type offset of this reference or value.
     * If the type is not UFIXED or FIXED, 0 is returned.
     * @return  the primitive type offset
     */
    public int getPrimOffset () {
        if ((wordspec == null) || (wordspec.getDimDes() != null))
            return(0);
        return(wordspec.getCheckType().getFixOffset());
    }
    
    /**
     * Get the array type of this reference or value.
     * If the type is not an array, null is returned.
     * @return  the array type
     */
    public Type getArrayType () { return(wordspec.getType().getArrayType()); }

    /**
     * Get the check type from the word specification.
     * @return  the word specification
     */
    public Type getCheckType () { return(wordspec.getCheckType()); }

    /**
     * NOT USED!!!!!!
     * Set the subscript/field list for a variable reference or value.
     * @param  sfl is the subscript/field list
     *//*
    public void setSubFields (SubFieldList sfl) { subfields = sfl; }*/

    /**
     * Get the subscript/field list for a variable reference or value.
     * @return  the subscript/field list
     */    
    public SubFieldList getSubFields () { return(subfields); }

    /**
     * Get the subscript/field list for a variable reference or value.
     * @return  the subscript/field list
     */    
    public SubFieldList getIndSubFields () { return(indsubfields); }

    /**
     * Get the number of words implied by the dimensional description for
     * a variable reference or value.
     * For a non-array type, 1 is returned.
     * @return  the size of the dimensionality array
     */    
    public int getDimWords () { return(wordspec.getDimWords()); }
    
    /**
     * Get the dimensionality array for a variable reference or value.
     * @return  the dimensionality array
     */    
    public int[] getDimDes () { return(wordspec.getDimDes()); }
    
    /**
     * Set the dimensionality array for a variable reference or value.
     * @param  dd is the dimensionality array
     */    
    public void setDimDes (int[] dd) { wordspec.setDimDes(dd); }

    /**
     * Set the source file location.
     * @param   l is the source file location
     */    
    public void setSrcLoc (SrcLoc l) { loc = l; }
    
    /**
     * Get the source file location.
     * @return   the source file location
     */    
    public SrcLoc getSrcLoc () { return(loc); }
    
    /**
     * Get the Var from a variable reference or value.
     * @return  the Var class for the variable reference or value
     */
    public Var getVar () { return(var); }

    /**
     * Get the TDEVar for a target reference or value.
     * @return  the TDEVar
     */    
    public TDEVar getTDEVar () {
        /*
        if ((tdevar != null) && (tdevar.getClockMode() == ClkType.NOT)) {
            Var curclk = getCurrentClockVar();
            Var tdevarclk = tdevar.getClkVar();
            if ((tdevarclk != null) && (!tdevarclk.isEqualTo(curclk)))
                throw new ExEx("SYSTEM ERROR - Val.getTDEVar - clock changed");
            tdevar.setClkVar(curclk);
        }
        */
        return(tdevar);
    }
    
    /**
     * Add a variable to the list of output clock domain variables.
     * @param   v is the variable
     */
    @SuppressWarnings("incomplete-switch")
    public void addOVar (Var v) {
        if (v == null)
            return;
        switch (v.getMode()) {
        case SELECTVALUE:
        case INPUT:
        case STATIC:
        case QUEUE:
        case PRIORITY:
        case CLOCK: // special case for value variable generated by pulse()
            if (ovars == null)
                ovars = new HashSet<Object>();
            ovars.add(v);
            //msg("addOvar(" + v.getId() + ")");
        }
    }
    
    /**
     * Add a VALUE mode variable to the list of output clock domain variables.
     * @param   v is the Value variable
     * @param   ws is the WordSpec for the variable
     */
    public void addOVal (Value v, WordSpec ws) {
        if (ovars == null)
            ovars = new HashSet<Object>();
        ovars.add(new ValWordSpecPair(v, ws));
        StringBuffer    sb = new StringBuffer();
        for (int i : ws.getWords())
            sb.append(i + " ");
        //msg("addOval(" + v.getId() + ", " + sb + ")");
    }

    /*
    public void printIOVars () {
	System.out.println("ovars:");
	for (Var v : ovars) {
	    System.out.println(v.getId());
	}
	System.out.println("ivars:");
	for (Var v : ivars) {
	    System.out.println(v.getId());
	}
    }
    */

    /**
     * Get the set of output clock domain variables.
     * @return  the set of output clock domain variables
     */
    public HashSet<Object> getOvars () {
        return(ovars);
    }
 
    /**
     * Get the set of input clock domain variables.
     * @return  the set of input clock domain variables
     */
    public HashSet<Var> getIvars () {
        return(ivars);
    }
   
    /**
     * Add variables from another reference or value to the set of output
     * clock domain variables.
     * @param   rov is the other reference or value
     */
    public void addOVars (RefOrVal rov) {
        if (rov.ovars == null)
            return;
        if (ovars == null)
            ovars = new HashSet<Object>();
        ovars.addAll(rov.ovars);
    }
    
    /**
     * Add variables from another variable set to the set of output
     * clock domain variables.
     * @param   ov is the other variable set
     */
    public void addOVars (HashSet<Object> ov) {
        if (ov == null)
            return;
        if (ovars == null)
            ovars = new HashSet<Object>();
        ovars.addAll(ov);
    }
     
    /**
     * Add variables from target subscript variables in a word specifier
     * to the set of output clock domain variables.
     * @param   ws is the word specifier
     */
    public void addOVars (WordSpec ws) {
        for (Val val: ws.getSubVals()) {
            if ((val != null) && (val.ovars != null)) {
                if (ovars == null)
                    ovars = new HashSet<Object>();
                ovars.addAll(val.ovars);
            }
        }
    }
   
    /**
     * Get/set the read (output) clock variable associated with this value.
     * If it has not been resolved, resolve it.
     * @param   calloc is the clock allocation event type
     * @param   loc is the source file location
     * @return  the primary clock Var
     */
    public Var getReadClkVar (Calloc calloc, SrcLoc loc) {
        if (!clks_resolved)
            resolveClocks(null, calloc, loc);
        return(clkvar);
    }
    
    /**
     * Return the read clock domain variable for this reference or
     * value.
     * @return  the clock Var
     */
    public Clock getReadClkVar () {
        if (var != null)
            return(var.getOutputClkVar(loc));
        if (!clks_resolved)
            return(collectClocks(loc));
        return(clkvar);
    }
    
    /**
     * Add a variable to the set of input clock domain variables.
     * @param   v is the variable
     */
    public void addIVar (Var v) {
        switch (v.getMode()) {
        case SELECTVALUE:
        case INPUT:
        case STATIC:
        case QUEUE:
        case PRIORITY:
            if (ivars == null)
                ivars = new HashSet<Var>();
            ivars.add(v);
            break;
        default:
            break;
        }
    }
    
    /**
     * Add variables from another reference or value to the set of input
     * clock domain variables.
     * @param   rov is the other reference or value
     */
    public void addIVars (RefOrVal rov) {
        if (rov.ivars == null)
            return;
        if (ivars == null)
            ivars = new HashSet<Var>();
        ivars.addAll(rov.ivars);
    }
    
    /**
     * Add variables from another variable set to the set of input
     * clock domain variables.
     * @param   iv is the other variable set
     */
    public void addIVars (HashSet<Var> iv) {
        if (iv == null)
            return;
        if (ivars == null)
            ivars = new HashSet<Var>();
        ivars.addAll(iv);
    }
     
    /**
     * Add variables from target subscript variables in a word specifier
     * to the set of input clock domain variables.
     * @param   ws is the word specifier
     */
    public void addIVars (WordSpec ws) {
        ArrayList<Val>   al = ws.getSubVals();
        for (Val val: al) {
            if ((val != null) && (val.ivars != null)) {
                if (ivars == null)
                    ivars = new HashSet<Var>();
                ivars.addAll(val.ivars);
            }
        }
    }
   
    /**
     * Get/set the write (input) clock variable associated with this value.
     * If it has not been resolved, resolve it.
     * @param   calloc is the clock allocation event type
     * @param   loc is the source file location
     * @return  the primary clock Var
     */
    public Clock getWriteClkVar (Calloc calloc, SrcLoc loc) {
        if (!clks_resolved)
            resolveClocks(null, calloc, loc);
        return(clkvar);
    }
    
    /**
     * Return the write clock domain variable for this reference or
     * value.
     * @return  the clock Var
     */
    public Clock getWriteClkVar () {
        if (var != null) {
            if (var.getMode() == Mode.QUEUE)
                return(((Queue)var).getQueueWriteClkVar());
            else
                return(var.getOutputClkVar(loc));
        } else
            return(clkvar);
    }
    
    /**
     * Resolve the read (output) and write (input) clock variables associated
     * with this value.
     * If either is null, set it to the current clock.
     * If either is not null, verify that it is the same as the current clock.
     * @param   clk is the clock domain variable
     * @param   calloc is the clock allocation event type
     * @param   loc is the source file location
     */
    public void resolveClocks (Clock clk, Calloc calloc, SrcLoc loc) {
        if (clks_resolved)
            return;

        if (clk == null) {
            if (((ivars == null) || (ivars.size() == 0)) &&
                ((ovars == null) || (ovars.size() == 0))) {
                if (mode == Mode.VALUE) {
                    clkvar = getCurrentClockVar();
                    addOVar(clkvar);
                } else
                    clkvar = null;
                clks_resolved = true;
                return;
            }
            clk = getCurrentClockVar();
        }
        clk.setValid(loc);
        
        if (ivars != null)
            for (Var v : ivars)
                v.setInputClock(clk, calloc, loc);
        if (ovars != null) {
            Iterator<Object>    it = ovars.iterator();
            while (it.hasNext()) {
                Object o = it.next();
                if (o instanceof Var) {
                    ((Var)o).setOutputClock(clk, calloc, true, loc);
                    //msg("resolve output clock " +  ((Var)o).getId());
                } else {
                    ValWordSpecPair vp = (ValWordSpecPair)o;
                    Value           val = vp.val;
                    WordSpec        ws = vp.ws;
                    val.setUsed(ws);
                }
            }
        }
        clks_resolved = true;
        clkvar = clk;
        if (mode == Mode.VALUE)
            addOVar(clk);
    }
    
    /**
     * Collect the read (output) and write (input) clock variables associated
     * with this value. Verify that they are all null or all the same. Return
     * null or the clock domain variable.
     * @param   loc is the source file location
     * @return  the clock domain variable
     */
    public Clock collectClocks (SrcLoc loc) {
        if (clks_resolved)
            return(clkvar);
            
        Clock cv = null;
        Clock ccv = null;

        ccv = collectOutputClocks(loc);
        if (ivars != null)
            for (Var v : ivars) {
                cv = v.getOutputClkVar(loc);
                if (cv != null) {
                    if (ccv == null)
                        ccv = cv;
                    else if (!ccv.isEqualTo(cv))
                        throw new ExEx("mixed clock domains in expression (" +
                            cv.getID(IDtype.LITERAL) + ", " + ccv.getID(IDtype.LITERAL) + ")", loc);
                }
            }
        return(ccv);
    }

    /**
     * Collect the read (output) clock variables associated
     * with this value. Verify that they are all null or all the same. Return
     * null or the clock domain variable.
     * @param   loc is the source file location
     * @return  the clock domain variable
     */
    public Clock collectOutputClocks (SrcLoc loc) {
        if (clks_resolved)
            return(clkvar);
        return(collectOutputClocks (ovars, loc));
    }
    
    /**
     * Collect the read (output) clock variables from a set of variables.
     * Verify that they are all null or all the same. Return
     * null or the clock domain variable.
     * @param   ovars is the variable set
     * @param   loc is the source file location
     * @return  the clock domain variable
     */
    static public Clock collectOutputClocks (HashSet<Object> ovars, SrcLoc loc) {
        Clock cv = null;
        Clock ccv = null;

        if (ovars != null) {
            Iterator<Object>    it = ovars.iterator();
            while (it.hasNext()) {
                Object o = it.next();
                if (o instanceof Var) {
                    cv = ((Var)o).getOutputClkVar(loc);
                    if (cv != null) {
                        if (ccv == null)
                            ccv = cv;
                        else if (!ccv.isEqualTo(cv))
                            throw new ExEx("mixed clock domains in expression (" +
                                cv.getID(IDtype.LITERAL) + ", " + ccv.getID(IDtype.LITERAL) + ")", loc);
                    }
                }
            }
        }
        return(ccv);
    }
    
    /**
     * Check for any null clocks. If there are any, throw an exception.
     * @param mess is an exception message
     * @param loc is the source file location
     */
    
    public void checkNullClocks (String mess, SrcLoc loc) {
        Clock           cv = null;
        StringBuffer    sb = new StringBuffer();
        boolean         error = false;
        Var             var;
        
        if (ovars != null) {
            Iterator<Object>    it = ovars.iterator();
            while (it.hasNext()) {
                Object o = it.next();
                if (o instanceof Var) {
                    var = (Var)o;
                    cv = var.getOutputClkVar(loc);
                    if ((var instanceof Input) && (cv == null) && (var.mode != Mode.INPUT)) {
                        sb.append("\t'" + var.getID(IDtype.LITERAL) + "'\n");
                        error = true;
                    }
                }
            }
        }    
        if (error)
            throw new ExEx(mess + "\n" + sb, loc);
    }

    /*
     * Clear the set of input and output clock variables.
     * This is used by externout() to clear the domain of a value variable
     * which will have been set automatically to the current clock domain
     * by getVal().
     */
    public void clearClocks  () {
        if (ivars != null)
            ivars.clear();
        if (ovars != null)
            ovars.clear();
        clkvar = null;
        clks_resolved = true;
    }   
    
    /**
     * Force the clock domain of this value. This is only called
     * by inbuilt function accept().
     * @param   clk is the clock
     */
    public void forceClock (Clock clk) {
        clkvar = clk;
        clks_resolved = true;
    }

    /**
     * If this Val contains any exec link sets, add an exec
     * signal to each set within the list.
     * @param   exec is the read execute signal
     */
    public void addExec (TDEVar exec) {
        if (execs == null)
            return;
        for (HashSet<TDEVar> s: execs)
            s.add(exec);
    }
    
    /**
     * If this Ref or Val contains any exec link sets, get the list
     * containing the exec signal sets.
     * @return  the read exec set
     */
    public ArrayList<HashSet<TDEVar>> getExecSets () {
        if (execs == null)
            execs = new ArrayList<HashSet<TDEVar>>();
        return(execs);
    }
    
    /**
     * Add exec link sets (if any) from the argument Ref or Val
     * to this Ref or Val.
     * @param   rov is the source Ref or Val
     */
    public void addExecSets (RefOrVal rov) {
        if (rov.execs == null)
            return;
        if (execs == null)
            execs = new ArrayList<HashSet<TDEVar>>();
        execs.addAll(rov.execs);
    }
    
    /**
     * Add the argument exec link set to this Ref or Val.
     * @param   hs is the exec link set
     */
    public void addExecSet (HashSet<TDEVar> hs) {
        if (execs == null)
            execs = new ArrayList<HashSet<TDEVar>>();
        execs.add(hs);
    }
    
    /**
     * Get the map of queue mode variables in this reference or
     * value. The variables may be part of an expression or may be
     * target variable array subscripts of a target variable.
     * @return  the map of queue mode variables
     */
    public QueueRefs getQueues () {
        if (queues == null)
            queues = new QueueRefs();
        return(queues);
    }
    
    /**
     * Set the map of queue mode variables in this reference or
     * value. This is only called from ExprNode to add a list of read or
     * write availabilities of queues.
     * @param  p is the map of queue mode variables
     */
    public void setQueues (QueueRefs p) { queues = p; }

    /**
     * And queue dependencies.
     * @param   p is the queue dependencies to be ANDed
     * @param   loc is the source file location
     */
    public void andSetQueues (QueueRefs p, SrcLoc loc) {
        if (queues == null)
            queues = new QueueRefs();
        queues.and_set(p, loc);
    }

    /**
     * And read and write queue dependencies.
     * @param   rov is a reference or value
     * @param   loc is the source file location
     */
    public void andSetQueues (RefOrVal rov, SrcLoc loc) {
        if (queues == null)
            queues = new QueueRefs();
        queues.and_set(rov.getQueues(), loc);
    }
    
    /**
     * Add read and write queue availability checks.
     * @param   rov is a reference or value
     */
    public void addAVChecks (RefOrVal rov) {
        if (queues == null)
            queues = new QueueRefs();
        QueueRefs p = rov.getQueues();
        if (p == null)
            return;
        queues.addReadAVChecks(p);
        queues.addWriteAVChecks(p);
    }
 
    /**
     * Check the type of this Ref or Val against one or more primitive types.
     * @param   loc is the source file location
     * @param   args are the allowed primitive types
     * @return  true if operand included in allowed primitive types
     */
    public boolean typeOK (SrcLoc loc, Object ... args) {
        int     n = args.length;
        boolean ok = false;
        Ptype   type = getPrimType();
        for (int i=0 ; i<n ; i++) {
            if (type == (Ptype)args[i]) {
                ok = true;
                break;
            }
        }
        return(ok);
    }
 
    /**
     * Check the type of two Refs or Vals, this and the first argument,
     * against one or more primitive types.
     * @param   v is the second Ref or Val
     * @param   loc is the source file location
     * @param   args are the allowed primitive types
     * @return  true if both operands included in allowed primitive types
     */
    public boolean typeOK (Val v, SrcLoc loc, Object ... args) {
        int     n = args.length;
        boolean lok = false;
        boolean rok = false;
        Ptype   ltype = getPrimType();
        Ptype   rtype = v.getPrimType();
        for (int i=0 ; i<n ; i++) {
            if (ltype == (Ptype)args[i]) {
                lok = true;
                break;
            }
        }
        for (int i=0 ; i<n ; i++) {
            if (rtype == (Ptype)args[i]) {
                rok = true;
                break;
            }
        }       
        return(lok && rok);
    }
    
    /**
     * Check the dimensional match between this Ref/Val and another.
     * Return true if OK.
     * Return false if check fails.
     * @param   rv  the Ref or Val checked against this WordSpec
     * @param   strict is true if target primitive widths are to be checked
     * @return  success or failure of the type match
     */
    public boolean checkMatch (RefOrVal rv, boolean strict) {
        return(checkMatch(rv, strict, null, null));
    }

    /**
     * Check the dimensional match between this Ref/Val and another.
     * Return true if OK.
     * If the check fails and mess is not null, exit with a message.
     * If the check fails and mess is null, return false.
     * @param   rv is the Ref or Val checked against the WordSpec of
     *          this Ref/Val
     * @param   strict is true if target primitive widths are to be checked
     * @param   mess is an optional failure message for fatal exit
     * @param   loc is the source file location
     * @return  success or failure of the type match
     */
    public boolean checkMatch (
        RefOrVal    rv,
        boolean     strict,
        String      mess,
        SrcLoc      loc
    ) {
        return(wordspec.checkMatch(rv.getWordSpec(), strict, mess, loc));
    }
    
    /**
     * Check the dimensional match between this Ref/Val and a Type.
     * Return true if OK.
     * If the check fails and mess is not null, exit with a message.
     * If the check fails and mess is null, return false.
     * @param   type is the type checked against the WordSpec of
     *          this Ref/Val
     * @param   strict is true if target primitive widths are to be checked
     * @param   mess is an optional failure message for fatal exit
     * @param   loc is the source file location
     * @return  success or failure of the type match
     */
    public boolean checkMatch (
        Type    type,
        boolean strict,
        String  mess,
        SrcLoc  loc
    ) {
        return(wordspec.checkMatch(type.getWordSpec(null, loc), strict, mess, loc));
    }
    
    /**
     * Check the dimensional match between this Ref/Val and a WordSpec.
     * Return true if OK. The check types, total sizes and dimensional
     * descriptions are compared.
     * If the check fails and mess is not null, exit with a message.
     * If the check fails and mess is null, return false.
     * @param   ws is the WordSpec checked against the WordSpec of
     *          this Ref/Val
     * @param   strict is true if target primitive widths are to be checked
     * @param   mess is an optional failure message for fatal exit
     * @param   loc is the source file location
     * @return  success or failure of the type match
     */
    public boolean checkMatch (
        WordSpec    ws,
        boolean     strict,
        String      mess,
        SrcLoc      loc
    ) {
        return(wordspec.checkMatch(ws, strict, mess, loc));
    }
    
    /**
     * Return the type of this value or reference.
     * @return  the type
     */
    public Type getType () {
        if (wordspec == null)
            return(new Type()); // no type
        else
            return(wordspec.getType());
    }

    /**
     * Get the type of this reference or value as a string. This will be
     * zero or more dimensions in brackets followed by either a primitive
     * type name or a struct name.
     * @return  the type String
     */
    public String getTypeString () {
        if ((mode == Mode.CMEMORY) || (mode == Mode.RMEMORY))
            return("-");
        return(wordspec.getTypeString());
    }
    
    /**
     * Set the key for a parameter/argument match.
     * @param   k is the key
     */
    public void setParamKey (String k) {
        pkey = k;
    }
    
    /**
     * Get the key for a parameter/argument match.
     * Null is returned if there is none.
     * @return  the key
     */
    public String getParamKey () {
        return(pkey);
    }
}
