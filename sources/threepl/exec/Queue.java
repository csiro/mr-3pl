package threepl.exec;

import static threepl.ThreePL.*;
import static threepl.parser.Functions.bits;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import threepl.codegen.TDE;
import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.netlist.TDECode;
import threepl.nodes.Ident;
import threepl.nodes.VarNode;
import threepl.parser.Constant;
import threepl.parser.Functions;
import threepl.parser.SrcLoc;

/**
 * This class represents a QUEUE mode variable. The variable has a name String,
 * a mode and a Type, which may be complex. If the variable
 * is a module, procedure or function parameter it may have an indirect
 * pointer to another Var, depending on its mode and whether input or
 * output. If the variable is a target mode it
 * will also have an expanded name to ensure that this instance is
 * unique. The current clock name String and TDEVar is also stored in
 * this class.
 *
 * <p>There is an ArrayList of all assignments
 * to this variable. Each assignment is entered into the list
 * one bit at a time. Entries in this list are triples -
 * <UL>
 * <LI> the bit number, which is an Integer
 * <LI> the source of the bit, which is a TDEVar
 * <LI> the select signal causing the assignment, which is a TDEVar
 *</UL>
 *
 * The following special cases apply -
 * <UL>
 * <LI> If isInPar is true then this variable is a module, procedure
 *      or function input parameter.
 * <LI> If isOutPar is true then this variable is a module or procedure
 *      output parameter.
 * <LI> If iclk is not equal to pclk then the queue buffer is
 *      asynchronous (input and output have different clocks).
 * </UL>
 */
public final class Queue extends ClockedVar implements Constant, TDEConstants {
    private LinkedHashMap<TDEVar,eapair>[]              inputs;      // assignment signals for this variable
    private LinkedHashSet<TDEVar>                       nullassigns; // null queue assignment signals
    private LinkedHashMap<Scope, LinkedHashSet<TDEVar>> pops;        // queue pop signals
    private LinkedHashMap<Scope, TDEVar>                ravails;     // queue read avail signals per module
    private long                            sinks = 0;  // number of divergent destination modules
    private long                            bufsize;    // queue buffer size
    private TDEVar                          queuewords; // queue buffer word count
    private TDEVar                          queuespaces;// queue buffer space count
    private TDEVar                          queueempty; // queue buffer is empty
    private TDEVar                          wstat;      // queue buffer status (write clock)
    private TDEVar                          rstat;      // queue buffer status (read clock)
    private Val                             targ_init;  // target variable initialisation
    private LinkedHashSet<TDEVar>[]         resets;     // static or queue reset signals
    private TDEVar                          push;       // push signal
    private TDEVar                          pop;        // pop signal
    private TDEVar                          ravsig;     // queue read availability signal
    private TDEVar                          wavsig;     // write availability signal
    private TDEVar                          rreset;     // reset output signal
    private Clock                           iclk;       // input clock 
    private String                          iclk_trace; // call trace where iclk set
    private Calloc                          iclk_event; // event setting iclk
    private boolean                         partassign; // assign to part of variable has occurred
    private boolean                         continuous; // continuous assignment
    private boolean                         ignore_modules; // do not generate a queue DIVERGE for multiple reads
    /**
     * Construct a queue mode variable.
     * @param   ident is the variable identifier
     * @param   t is the variable type
     * @param   init is an initialisation value, either a single value
     *          or a compound value
     * @param   in_par is true if this is a module, procedure of function
     *          input parameter
     * @param   out_par is true if this is a module or procedure output
     *          parameter
     * @param   loc is the source file location
     */
    @SuppressWarnings("unchecked")
    public Queue (
        Ident   ident,
        Type    t,
        Val     init,
        boolean in_par,
        boolean out_par,
        SrcLoc  loc
    ) {
        super(ident, in_par, out_par, t, Ptype.EMPTY, loc);
        
        if (!getFamily().allowQueue())
            throw new ExEx("queue mode variable cannot be implemented in this device", loc);

        // Mode.
        mode = Mode.QUEUE;

        int     words = type.numWords();
        isInPar = in_par;
        isOutPar = out_par;
        pops = new LinkedHashMap<Scope, LinkedHashSet<TDEVar>>();
        ravails = new LinkedHashMap<Scope, TDEVar>();
        if (words != 0) {
            inputs = new LinkedHashMap[words];
            for (int i=0 ; i<words ; i++)
                inputs[i] = new LinkedHashMap<TDEVar, eapair>();
        }

        // initialisation
        if (words > 0) {
            val = new Object[words];
            val_type = type.getTypeArray();

            // Optional explicit initialisation.
            if (init != null) {
                if (!type.isPrimitive() && init.isPrimitive())
                    throw new ExEx("compound variable single initialiser is target mode", loc);

                targ_init = init;
            }
        }
        nullassigns = new LinkedHashSet<TDEVar>();
        // Default initial queue buffer size.
        // The application code can change this.
        // For queue of type null buffer is just a counter (sync) or
        // pair of gray code counters (async).
        // Synchronous queues have default of size 2.
        // Queues that later prove to be asynchronous may have their
        // buffer size changed.
        bufsize = (type.getPrimType() == Ptype.NULL) ? getFamily().minCRAMQueueDepth() : 2;
        attributes.put("buffersize", new Val(bufsize, loc));
        attributes.put("depth", new Val(bufsize, loc));
        resets = new LinkedHashSet[(words == 0) ? 1 : words]; // possible "null" queue

        pop    = tdelist.namesignal(ename + ctrail + "POP", decloc);
        push  = tdelist.namesignal(ename + ctrail + "PUSH", decloc);
        wavsig = tdelist.namesignal(ename + ctrail + "NF", loc);
        ravsig = tdelist.namesignal(ename + ctrail + "NE", loc);

        if (getFamily().defaultContinuous())
            setContinuous(true, loc);
        
        // add this to the variable queue for processing at the end
        queueVar(this);
    }
    
   /**
    * Get this queue's write availability signal for an assignment.
    * @param   loc is the source file location
    * @return  the write availability signal
    */
   public TDEVar getWriteAvailSig (SrcLoc loc) {
       if (indass)
           return(((Queue)indirect).getWriteAvailSig(loc));
       return(wavsig);
   }
    
    /**
     * Get the read availability signal for this queue for a destination module.
     * @param   module_scope is the destination module scope
     * @param   loc is the source file location
     * @return  the availability signal
     */
    public TDEVar getReadAvailSig (Scope module_scope, SrcLoc loc) {
        if (module_scope == null)
            module_scope = getModuleScope();
        if (indass)
            return(((Queue)indirect).getReadAvailSig(module_scope, loc));
        if (getCurrentClockVar() == null)
            throw new ExEx("no current clock", loc);
        if (ravails.containsKey(module_scope))
            return ravails.get(module_scope);

        //WordSpec    ws = new WordSpec(1, 0,Ptype.LOG, this);
        TDEVar  tdev = tdelist.signal("MRAV", decloc);
        ravails.put(module_scope, tdev);
        return(tdev);
    }

    /**
     * Get the write signal.
     * The signal is high on the clock cycle of a write to the queue.
     * @return  the write signal
     */
    public TDEVar getWrites () {
        if (bufsize == 0)
            return(tdelist.and(push, wavsig, decloc));
        else
            return(push);
    }

    /**
     * Get the read signal.
     * The signal is high on the clock cycle of a read from the queue.
     * @return  the read signal
     */
    public TDEVar getReads () {
        if (bufsize == 0)
            return(tdelist.and(pop, ravsig, decloc));
        else
            return(pop);
    }
    /**
     * Get the number of divergent sink (destination) modules.
     * @return the number of divergent sink modules
     */
    public long getSinks() { return(sinks); }
    
    /**
     * Set the attributes map.
     * @param   val is the attributes map value
     * @param   loc is the source file location
     */
    @SuppressWarnings("unchecked")
    public void setAttributes (Val val, SrcLoc loc) {
        if (indass) {
            indirect.setAttributes(val, loc);
            return;
        }
      
        TreeMap<String, Val>    attr = (TreeMap<String,Val>)val.getVal(0);

        // check attributes for those applicable to 3PL itself.
        Set<String>         ks = attr.keySet();
        for (String mkey: ks) {
            Val     mval = attr.get(mkey);
            mkey = mkey.toLowerCase();

            // Check that attribute is known to 3PL.
            // If it checks out OK, continue processing.
            // If it does not check out skip the rest of the loop.
            if (!checkAttribute(mkey, mval, mode, loc))
                throw new ExEx("3PL " + mkey + " attribute for queue variable '" + name + "' is unknown", loc);

            if (mkey.equals("domain")) {
                Clock clockvar = getCurrentClockVar();
                if (mval.getPrimType() != Ptype.NULL) {
                    if (mval.getMode() != Mode.CLOCK)
                        throw new ExEx("domain attribute for queue variable '" + name + "' is not a clock variable", loc);
                    if (mval.getPrimType() != Ptype.NULL)
                        clockvar = (Clock)mval.getVar();
                }
                setOutputClock(clockvar, Calloc.READDOMAIN, false, loc);
                setInputClock(clockvar, Calloc.WRITEDOMAIN, loc);
                continue;
            } else if (mkey.equals("readdomain")) {
                Clock clockvar = getCurrentClockVar();
                if (mval.getPrimType() != Ptype.NULL) {
                    if (mval.getMode() != Mode.CLOCK)
                        throw new ExEx("domain attribute for queue variable '" + name + "' is not a clock variable", loc);
                    clockvar = (Clock)mval.getVar();
                }
                setOutputClock(clockvar, Calloc.READDOMAIN, false, loc);
                continue;
            } else if (mkey.equals("writedomain")) {
                Clock clockvar = getCurrentClockVar();
                if (mval.getPrimType() != Ptype.NULL) {
                    if (mval.getMode() != Mode.CLOCK)
                        throw new ExEx("domain attribute for queue variable '" + name + "' is not a clock variable", loc);
                    clockvar = (Clock)mval.getVar();
                }
                setInputClock(clockvar, Calloc.WRITEDOMAIN, loc);
                continue;
            }
            
            if (mval.getPrimType() == Ptype.NULL)
                throw new ExEx("attribute \"" + mkey + "\" for queue variable '" + name + "' is null", loc);
            
            if (mkey.equals("readonly")) {
                if (mval.getSingleLval(loc))
                    setReadOnly(loc);
                else {
                    msg(loc.toString());
                    msg("queue variable " + name + " - readonly attribute cannot be false - ignored");
                }
            } else if (mkey.equals("buffersize") || mkey.equals("depth"))
                setBufferSize((int)mval.getSingleIval(loc), true, loc);
            else if (mkey.equals("minbuffersize") || mkey.equals("mindepth"))
                setBufferSize((int)mval.getSingleIval(loc), false, loc);
            else if (mkey.equals("continuous"))
                setContinuous(mval.getSingleLval(loc), loc);
            else if (mkey.equals("ignoremodules"))
                ignore_modules = mval.getSingleLval(loc);
            
            attributes.put(mkey, mval);
        }
    }
    
    /**
     * Get the attributes map value.
     * @param   loc is the source file location
     * @return  the attributes map value
     */
    public Val getAttributes (boolean extended, SrcLoc loc) {
        if (indass)
            return(indirect.getAttributes(extended, loc));
        
        TreeMap<String, Val> attr = new TreeMap<String, Val>();
        if (attributes != null)
            attr.putAll(attributes);
        if (extended) {
            attr.put("identifier", new Val(name, loc));
            attr.put("eidentifier", new Val(ename, loc));
            attr.put("mode", new Val("queue", loc));
            attr.put("type", new Val(type, loc));
            attr.put("typestring", new Val(wordspec.getTypeString(), loc));
            attr.put("decloc", new Val(decloc.toString(), loc));
            attr.put("inputparam", new Val(isInPar, loc));
            attr.put("outputparam", new Val(isOutPar, loc));
            attr.put("matched", new Val(matched, loc));
            attr.put("readonly", new Val(readonly, loc));
            attr.put("assigned", new Val(assigned, loc));
            attr.put("used", new Val(used, loc));
        }
        Object[]    oa = new Object[1];
        Type[]      ta = new Type[1];
        Type        type = new Type(Ptype.MAP, 0);
        WordSpec    ws = type.getWordSpec(null, loc);
        oa[0] = attr;
        ta[0] = type;
        return(new Val(oa, ta, ws, null, new SubFieldList(), null));
    }
    
    /**
     * Set the 'continuous' flag.
     * @param   d is the value to set the flag
     * @param   loc is the source file location
     */
    public void setContinuous (boolean d, SrcLoc loc) {
        continuous = d;
        attributes.put("continuous", new Val(d, loc));
    }
    
    /**
     * Get the continuous assignment flag.
     * @return  the continuous assignment flag
     *
    */
    public boolean getContinuous () { return(continuous); }
   
    /**
     * Add a reset signal for a queue.
     * Only the first member of the resets array of signal sets
     * is used.
     * @param   r is the reset signal
     * @param   loc is the source file location
     */
    public void addReset (TDEVar r, SrcLoc loc) {
        if (indass) {
            indirect.addReset(r, loc);
            return;
        }
        if (bufsize == 0)
            throw new ExEx("queue '" + name +
                "' cannot reset an unbuffered queue", loc);
        setInputClock(getCurrentClockVar(), Calloc.RESET, loc);
        if (resets[0] == null)
            resets[0] = new LinkedHashSet<TDEVar>();
        resets[0].add(r);
    }
   
    /**
     * Get the output reset signal for a queue.
     * @param   loc is the source file location
     * @return  the output reset signal
     */
    public TDEVar getRReset (SrcLoc loc) {
        if (indass)
            return(indirect.getRReset(loc));
        if (rreset == null) {
            WordSpec    ws = new WordSpec(Ptype.LOG);
            rreset = tdelist.signal("RR", ws, decloc);
        }
        return(rreset);
    }
     
    /**
     * Set the parameter/argument indirect fields for this parameter variable.
     * Field <b>Var indirect</b> is the argument variable and field
     * <b>SubFieldList ind_sfl</b> is a list of any subscripts or fields on the
     * argument variable reference.
     * @param   arg is the argument reference to which this parameter is
     *          to point
     * @param   loc is the source file location of the parameter declaration
     */
    public void setIndirect (RefOrVal arg, SrcLoc loc) {
        super.setIndirect(arg, loc);
                
        Queue p = (Queue)arg.getVar();

        // make parameter clocks same as those of the argument variable.
        iclk = p.iclk;
        iclk_event = p.iclk_event;
        iclk_trace = p.iclk_trace;
        pclk = p.pclk;
        pclk_event = p.pclk_event;
        pclk_trace = p.pclk_trace;
        ind_sfl = arg.getSubFields();
    }
    
    /**
     * Set the input clock.
     * @param   clk is the clock domain variable
     * @param   e is the event setting the clock
     * @param   loc is the source file location
     */
    public void setInputClock (Clock clk, Calloc e, SrcLoc loc) {
        if (clk == null)
            return;

        if (indass) {
            indirect.setInputClock(clk, e, loc);
            return;
        }

        if (iclk == null) {
            if (clockdiag)
                System.out.println(loc + " queue " + ename + " in clk set - " + clk.getID(IDtype.LITERAL) + " " + e.event());
            iclk = clk;
            iclk_trace = loc.toString() + "\n" + stackTrace();
            iclk_event = e;
            Val cv = new Val(clk, Mode.CLOCK, clk.getClkSig(), loc);
            attributes.put("writedomain", cv);
            if ((pclk != null) && (iclk.isEqualTo(pclk)))
                attributes.put("domain", cv);
            if ((pclk != null) && (!iclk.isEqualTo(pclk)) && (bufsize == 2)) {
                bufsize = getFamily().minCRAMQueueDepth();
                attributes.put("buffersize", new Val(bufsize, loc));
                attributes.put("depth", new Val(bufsize, loc));
            }
        } else if (!iclk.isEqualTo(clk)) {
            msg("queue variable '" + name +
                "' - attempt to change assignment clock domain");
            msg("previous assignment clock " + iclk.getID(IDtype.CHAIN) + " allocated by " +
                iclk_event.event() + " at location -");
            msg(iclk_trace);          
            throw new ExEx("second assignment clock " + clk.getID(IDtype.CHAIN) +
                           " allocated by " + e.event() +
                           " at location -", loc);
        } else if (clockdiag)
            System.out.println(loc + " queue " + ename + " in clk checked - " + clk.getID(IDtype.CHAIN) + " " + e.event());
    }

    /**
     * Set the output clock.
     * @param   clk is the clock domain variable
     * @param   e is the event setting the clock
     * @param   used if true indicates that the variable has been evaluated for
     *          an assignment, i.e. it should be marked as 'used'.
     * @param   loc is the source file location
     */
    public void setOutputClock (Clock clk, Calloc e, boolean used, SrcLoc loc) {
        if (indass) {
            indirect.setOutputClock(clk, e, used, loc);
            return;
        }

        if (used)
            this.used = true;

        if (clk == null)
            return;

        if (pclk == null) {
            if (clockdiag)
                System.out.println(loc + " queue " + ename + " out clk set - " + clk.getID(IDtype.LITERAL) + " " + e.event());
            pclk = clk;
            if (type.getPrimType() != Ptype.NULL)
                tdelist.namesignal(ename, wordspec, decloc);
            pclk_trace = loc.toString() + "\n" + stackTrace();
            pclk_event = e;
            Val cv = new Val(clk, Mode.CLOCK, clk.getClkSig(), loc);
            attributes.put("readdomain", cv);
            if ((iclk != null) && (iclk.isEqualTo(pclk)))
                attributes.put("domain", cv);
            if ((iclk != null) && (!iclk.isEqualTo(pclk)) && (bufsize == 2)) {
                bufsize = getFamily().minCRAMQueueDepth();
                attributes.put("buffersize", new Val(bufsize, loc));
                attributes.put("depth", new Val(bufsize, loc));
            }
        } else if (!pclk.isEqualTo(clk)) {
            msg("queue variable '" + name +
                "' - attempt to change output clock domain");
            msg("previous output clock " + pclk.getID(IDtype.CHAIN) + " allocated by " +
                pclk_event.event() + " at location -");
            msg(pclk_trace);
            throw new ExEx("second output clock " + clk.getID(IDtype.CHAIN) +
                           " allocated by " + e.event() +
                           " at location -", loc);
        } else if (clockdiag)
            System.out.println(loc + " queue " + ename +
                    " out clk checked - " + clk.getID(IDtype.CHAIN) + " " + e.event());
    }
    
    /**
     * Get the queue input (write) clock variable associated with this variable.
     * @return  the input clock Var
     */
    public Clock getQueueWriteClkVar () {
        if (indass)
            return(((Queue)indirect).getQueueWriteClkVar());
        return(iclk);
    }
   
    /**
     * Set queue buffer size.
     * @param   i is the buffer size
     * @param   strict requires the buffer size to be an allowed depth, i.e.
     *          it will not be rounded up to match the next available depth
     * @param   loc is the source file location
     */
    private void setBufferSize (int i, boolean strict, SrcLoc loc) {
        if (indass) {
            ((Queue)indirect).setBufferSize(i, strict, loc);
            return;
        }
        if (assigned || !pops.isEmpty())
            throw new ExEx("queue '" + name +
                    "' cannot change queue buffer depth after read or write references have occurred", loc);
        if ((queuewords != null) || (queuespaces != null))
            throw new ExEx("queue '" + name +
                "' cannot change queue buffer depth after call to queuewords() or queuespaces()", loc);
        if ((targ_init != null) && (i != 2))
            throw new ExEx("queue '" + name +
                        "' initialised queue must be depth 2", loc);
        if (i == 1)
            throw new ExEx("queue '" + name +
                        "' queue buffer depth cannot be set to 1", loc);
        
        TDECode family = getFamily();
        if (strict) {
            if ((i != 0) && (i != 2) && (!family.queueDepthUsingCram(i)) && !family.queueDepthUsingRram(i))
                throw new ExEx("queue '" + name +
                        "' queue buffer depth " + i + " is not an allowed depth", loc);
        } else {
            if (i > family.maxRRAMQueueDepth())
                throw new ExEx("queue '" + name +
                        "' queue buffer depth " + i + " is too large", loc);
            if (i > 2) {
                i = 1 << bits(i-1, false); // round up to power of two
                
                if (i < family.minCRAMQueueDepth())
                    i = family.minCRAMQueueDepth();
                else if (i <= family.maxCRAMQueueDepth()) {
                    while (!family.queueDepthUsingCram(i))
                        i <<= 1;
                } else {
                    while (!family.queueDepthUsingRram(i))
                        i <<= 1;
                }
                
            }
        }
        bufsize = i;
        attributes.put("buffersize", new Val(i, loc));
        attributes.put("depth", new Val(i, loc));
    }
    
    /**
     * Get queue buffer depth. Var must be Mode.QUEUE.
     * @return  the buffer depth
     */
    public long getBufferSize () {
        if (indass)
            return(((Queue)indirect).getBufferSize());
        return(bufsize);
    }
    
    /**
     * Set the 'direct' flag - not used for QUEUE mode.
     * @param   d is the value to set the flag
     * @param   loc is the source file location
     */
    public void setDirect (boolean d, SrcLoc loc) {return;}
    
    /**
     * Determine if queue is a null queue.
     * Var must be Mode.QUEUE.
     * @return  true if the queue is a null queue
     */
    public boolean isNullQueue () {
        if (indass)
            return(((Queue)indirect).isNullQueue());
        return(type.getPrimType() == Ptype.NULL);
    }
    
    /**
     * Get the TDEVar for the buffer word count.
     * @param   loc is the source file location
     * @return  the TDEVar for the buffer word count
     */
    public TDEVar getQueueWords (SrcLoc loc) {
        if (indass)
            return(((Queue)indirect).getQueueWords(loc));
        if (queuewords == null) {
            int         width = Functions.bits(bufsize, false);
            WordSpec    ws = new WordSpec(width, Ptype.UINT);
            queuewords  = tdelist.namesignal(ename + ctrail + "CNT", ws, loc);
        }
        return(queuewords);
    }
     
    /**
     * Get the TDEVar for the buffer space count.
     * @param   loc is the source file location
     * @return  the TDEVar for the buffer space count
     */
    public TDEVar getQueueSpaces (SrcLoc loc) {
        if (indass)
            return(((Queue)indirect).getQueueSpaces(loc));
        if (queuespaces == null) {
            int         width = Functions.bits(bufsize, false);
            WordSpec    ws = new WordSpec(width, Ptype.UINT);
            queuespaces  = tdelist.namesignal(ename + ctrail + "SPC", ws, loc);
        }
        return(queuespaces);
    }
     
    /**
     * Get the TDEVar for the buffer empty signal.
     * @param   loc is the source file location
     * @return  the TDEVar for the buffer empty signal
     */
    public TDEVar getQueueEmpty (SrcLoc loc) {
        if (indass)
            return(((Queue)indirect).getQueueEmpty(loc));
        if (queueempty == null) {
            WordSpec    ws = new WordSpec(Ptype.LOG);
            queueempty  = tdelist.signal("EP", ws, loc);
        }
        return(queueempty);
    }
    
    /**
     * Get the TDEVar for the buffer status synchronised to the input
     * output (read) clock.
     * @param   loc is the source file location
     * @return  the TDEVar for the buffer status
     */
    public TDEVar getQueueRStat (SrcLoc loc) {
        if (indass)
            return(((Queue)indirect).getQueueRStat(loc));
        if (rstat == null) {
            Type     t = new Type("(etoq,log,ttoh,log,qtot,log,htof,log,ttof,log)", loc);
            WordSpec ws = t.getWordSpec(this, loc);
            rstat  = tdelist.namesignal(ename + ctrail + "RSTAT", ws, loc);
        }
        return(rstat);
    }
    
    /**
     * Get the TDEVar for the buffer status synchronised to the input
     * (write) clock.
     * @param   loc is the source file location
     * @return  the TDEVar for the buffer status
     */
    public TDEVar getQueueWStat (SrcLoc loc) {
        if (indass)
            return(((Queue)indirect).getQueueWStat(loc));
        if (wstat == null) {
            Type     t = new Type("(etoq,log,ttoh,log,qtot,log,htof,log,ttof,log)", loc);
            WordSpec ws = t.getWordSpec(this, loc);
            wstat  = tdelist.namesignal(ename + ctrail + "WSTAT", ws, loc);
        }
        return(wstat);
    }

    /**
     * Get the value of a variable. The value includes all the
     * information provided by a variable reference (see getRef()) plus
     * For immediate mode type an ArrayList of values. For target mode
     * (value, static or queue), it returns the TDEVar. The
     * <B>varnode</B> argument is used to get any flags associated with
     * the variable occurrence, but it may be null if the reference is
     * being requested in some other context.
     * @param   ref is the reference to this variable
     * @param   loc is the source file location
     * @return  the value or values extracted from the variable
     */
    public Val getVal (Ref ref, SrcLoc loc) {
        Flag flag = ref.getFlag();           
        if (flag != Flag.NONE)
            throw new ExEx("QUEUE mode variable cannot have ++ or --", loc);

        Val v = new Val(ref, false, loc);
        v.addOVar(this);
        v.addOVars(ref);
        v.addOVars(ref.getTDEVar().getWordSpec()); // add any subscript Vars
        v.andSetQueues(ref, loc);
        return(v);
    }

    /**
     * Get a reference to a variable. The reference is generally
     * used for assignment to a variable however the value of a variable
     * can also obtained by starting with the reference. The main
     * information contained in the reference is -
     * <UL>
     * <LI> a list of words in the variable selected by subscripts or
     *      field names
     * <LI> a list of primitive types for each word
     * <LI> for target variables a list of bit pairs associated with
     *      the word list
     * <LI> a multi-dimensioned array giving the dimensionality of
     *      the reference
     * <LI> a type to make sure assignment types match
     * <LI> a TDEVar for a target variable
     * </UL>
     * The <B>varnode</B> argument is used to get any flags
     * associated with the variable occurrence (++ or -- flags
     * of the string indirect operator/flag), but it may be null if the
     * reference is being requested in some other context.
     * The <B>subs</B> argument is a list of subscript or field
     * nodes associated with the variable occurrence - it may be null.
     * A pointer variable returns a reference to the ultimate variable
     * to which it points.
     * A target reference includes a TDEVar signal for the reference.
     * @param   varnode is the tree node for the variable occurrence or
     *          is null if the reference is not associated with a variable
     *          occurrence (used to detect ++, -- or ?)
     * @param   sfl is the list of associated subscript and field nodes
     *          after the subscript and field nodes have been evaluated
     * @param   is_val indicates that we want a value rather than a reference
     * @param   loc is the source file location
     * @return  the reference to the variable
     */
    public Ref getRef (
        VarNode         varnode,
        SubFieldList    sfl,
        boolean         is_val,
        SrcLoc          loc
    ) {
        Flag    flag = (varnode == null) ? Flag.NONE : varnode.getFlag();
        if (flag != Flag.NONE)
            throw new ExEx("QUEUE mode variable cannot have ++ or --", loc);
        
        if (indass) {
            // An indirect via a module, procedure or function parameter.
            if (indirect == null)
                return(null);
            if (ind_sfl != null)
                sfl = ind_sfl.merge(this, sfl);
            return(indirect.getRef(varnode, sfl, is_val, loc));
        }

        if ((sfl.size() > 0) && type.isPrimitive())
            throw new ExEx("subscript or field with primitive variable '" + name + "'", loc);

        WordSpec    ws = type.getWordSpec(this, sfl, loc);
        Val         tdev_val = get_target_sig(ws, is_val, loc);// Val is wrapper!
        TDEVar      v = tdev_val.getTDEVar();
        Var         vv = v.getVar();
        Ref         ref = new Ref(vv != null ? vv : this, mode, sfl, v, loc);
        if (is_val)
            ref.addOVars(tdev_val);
        else
            ref.addIVar(this);
        ref.andSetQueues(tdev_val, loc);
        return(ref);
    }
    
    /**
     * Get a reference to one word of a queue mode variable.
     * @param   index is the component index
     * @param   loc is the source file location
     * @return  the reference to the variable component
     */
    public Ref getWordRef (int index, SrcLoc loc) {
        WordSpec    ws = new WordSpec();
        Type        t = wordspec.getType(index);
        ws.append(index, wordspec.getLower(index), wordspec.getUpper(index), t);
        ws.setCheckType(t);
        Val             tdev_val = get_target_sig(ws, false, loc);// Val is wrapper!
        SubFieldList    sfl = type.indexToSubFieldList(index, loc);
        Ref             ref = new Ref(this, mode, sfl, tdev_val.getTDEVar(), loc);
        ref.addIVar(this);
        return(ref);
    }

    /**
     * Get a Val wrapper containing the TDEVar for the variable occurrence.
     * @param   ws is the word specifier for the occurrence
     * @param   is_val is true if we want a value rather than a reference
     * @param   loc is the source file location
     * @return  a Val wrapper containing the TDEVar, queue references and
     *          maps of any variables referenced
     */
    private Val get_target_sig (
        WordSpec    ws,
        boolean     is_val,
        SrcLoc      loc
    ) {
        TDEVar      tdev = null;
        int         ntsubs = ws.getNumTargSubs();
        QueueRefs    queues = new QueueRefs();

        if (ntsubs > 0) {
            // has target variable subscripts
            if (is_val) {
                // is a value (RHS)
                //queue_read_check(loc);
                tdev = ws.selector(ename, null, loc);
            } else {
                // is a reference (LHS)
                //queue_write_check(loc);
                tdev = TDEVar.makeTDEVar(this, ws, loc);
            }
            queues.and_set(ws.getSQueues(), loc);
        //} else if (type.getPrimType() == Ptype.NULL) {
        //    // Special case of type null - TDEVar must be logic high.
        //    tdev = new TDEVar(Boolean.valueOf(true), Ptype.LOG, loc);
        } else {
            // has only immediate subscripts or none
            tdev = TDEVar.makeTDEVar(this, ws, loc);
        }
        queues.and_set(this, !is_val, loc);
        Val tdev_val = new Val(tdev, loc);
        tdev_val.andSetQueues(queues, loc);
        return(tdev_val);
    }
    

    /**
     * Assignment to this variable.
     * @param   lref is the left variable reference.
     * @param   rval is the RHS expression value.
     * @param   exec is the execution signal
     * @param   asstype is the assignment type
     * @param   toplevel is true if this is the top level in a module
     * @param   loc is a source file location for error messages
     */
    public void assignTo (
        Ref     lref,
        Val     rval,
        TDEVar  exec,
        AST     asstype,
        boolean toplevel,
        SrcLoc  loc
    ) {
        if (readonly)
            throw new ExEx("cannot assign to '" + name + "' - read-only!", loc);

        Clock   curclk = getCurrentClockVar();
        Clock   rclk = (rval != null) ? rval.getReadClkVar() : null;

        if ((rclk != null) && (!rclk.isEqualTo(curclk)))
            throw new ExEx("RHS assigned to variable '" + name + "' (" + rclk.getID(IDtype.CHAIN) +
                    ") has different clock to current context clock (" +
                    curclk.getID(IDtype.CHAIN) + ")", loc);
        
        if ((lref.getPrimType() == Ptype.NULL) || (rval == null)) {
            nullassigns.add(exec);
            assigned = true;
            return;
        }

        WordSpec    lws = lref.getWordSpec();   // LHS wordspec
        int         words = lws.numWords();     // rws is same size (prev check)
        TDEVar      t;
        TDEVar      rtdev = rval.isTarget() ? rval.getTDEVar() : null;
        
        for (int i=0 ; i<words ; i++) {
            if (rtdev != null) {
                // RHS is a target expression
                if (((lws.getPrimType(i) == Ptype.UINT) ||
                     (lws.getPrimType(i) == Ptype.UFIXED)) &&
                    ((rval.getWordSpec().getPrimType(i) == Ptype.INT) ||
                     (rval.getWordSpec().getPrimType(i) == Ptype.FIXED)))
                    throw new ExEx("unsigned assigned from signed", loc);
                t = rtdev.getWord(lws, i, loc);
                rval.addExec(exec);
            } else if (rval.getVal(i) instanceof TDEVar) {
                // RHS contains a target expression in a compound value
                t = ((TDEVar)rval.getVal(i));
            } else {
                // RHS is an immediate expression
                t = new TDEVar(rval.getVal(i), lws, i, loc);
                if (t.numBits() > lws.getWidth(i))
                    throw new ExEx(name + " assigned larger immediate value", loc);
                if (((lws.getPrimType(i) == Ptype.UINT) || (lws.getPrimType(i) == Ptype.UFIXED)) &&
                    (((Long)t.getConst()).longValue() < 0))
                    throw new ExEx("unsigned assigned -ve value", loc);
            }

            add_input(inputs, lws.getWord(i), t, exec, asstype, toplevel);
        }
        if (type.numWords() != words)
            partassign = true;
        assigned = true;
        rval.setUsed();
    }

    /**
     * Output the code to create the variable itself.
     * This method is called on completion of program interpretation at which
     * point all assignments to this variable will have been made.
     */
    @SuppressWarnings("unused")
    public void createVar () {
        current_create = this;
        // If the simulator is to be run, add a TDEType.SIMVAR to the TDE list
        // to notify the simulator about this variable.
        if (sim_implemented && sim) {
            TDE simtdevar = new TDE(TDEType.SIMVAR);
            simtdevar.add2p(this);
            tdelist.addTDE(simtdevar);
        }

        // Nothing to do for indirect (parameter).
        if (indass)
            return;

        // Nothing to do if not assigned and not used, but keep going
        // if is a null queue.
        if (!assigned && !used && (type.getPrimType() != Ptype.NULL)) {
            rpt("\tqueue variable '" + ename + "' unused - deleted");
            return;
        }

        // If has no sources or sinks, ignore it.
        // If it has no sources but has sinks, give an error message.
        if (!assigned && !develop) {
            if ((pops != null) && (pops.size() != 0))
                emsg("\n\tqueue '" + ename + "' has no sources");
            return;
        }
        if (((pops == null) || (pops.size() == 0)) && !develop) {
            emsg("\n\tqueue '" + ename + "' has no sinks");
            return;
        }

        String varname = ename;
        String varDataName = varname + ctrail + "D";
        String varResetName = varname + ctrail + "RES";
        
        // Signal declarations and some TDE initialisation.
        TDEVar  reg    = null;
        TDEVar  reset  = null;
        TDE     tde    = null;
        TDEVar  dest   = null;

        tde = new TDE(TDEType.QUEUEBUFFER, decloc);
        reset  = tdelist.namesignal(varResetName, decloc);

        tde.add2p(Integer.valueOf((int)bufsize));

        if (iclk.isEqualTo(pclk)) {
            // synchronous
            if ((bufsize < getFamily().minCRAMQueueDepth()) &&
                (bufsize != 0) &&
                (type.getPrimType() == Ptype.NULL))
                throw new ExEx("queue '" + name +
                        "' null queue buffer size < " + getFamily().minCRAMQueueDepth(), decloc);
            if (wstat != null)
                throw new ExEx("queue '" + name +
                        "' cannot use queuewritestatus() on synchronous queue", decloc);
            if (rstat != null)
                throw new ExEx("queue '" + name +
                        "' cannot use queuereadstatus() on synchronous queue", decloc);
            if ((queuewords != null) && (bufsize == 0))
                throw new ExEx("queue '" + name +
                        "' cannot use queuewords() on an unbuffered queue", decloc);
            if ((queuespaces != null) && (bufsize == 0))
                throw new ExEx("queue '" + name +
                        "' cannot use queuespaces() on an unbuffered queue", decloc);
            iclk = null;
        } else {
            // asynchronous
            if ((bufsize > 0) && (bufsize < getFamily().minCRAMQueueDepth()))
                throw new ExEx("queue '" + name +
                        "' asynchronous queue buffer size < " + getFamily().minCRAMQueueDepth(), decloc);
            if (queuewords != null)
                throw new ExEx("queue '" + name +
                        "' cannot use queuewords() on asynchronous queue", decloc);
            if (queuespaces != null)
                throw new ExEx("queue '" + name +
                        "' cannot use queuespaces() on asynchronous queue", decloc);
            if (queueempty != null)
                throw new ExEx("queue '" + name +
                        "' cannot use queueisempty() on asynchronous queue", decloc);
        }
        if (bufsize == 0) {
            if (wstat != null)
            throw new ExEx("queue '" + name +
                    "' cannot use queuewritestatus() on unbuffered queue", decloc);
            if (rstat != null)
                throw new ExEx("queue '" + name +
                        "' cannot use queuereadstatus() on unbuffered queue", decloc);
            if (queuewords != null)
                throw new ExEx("queue '" + name +
                        "' cannot use queuewords() on unbuffered queue", decloc);
            if (queuespaces != null)
                throw new ExEx("queue '" + name +
                        "' cannot use queuespaces() on unbuffered queue", decloc);
            if (queueempty != null)
                throw new ExEx("queue '" + name +
                        "' cannot use queueisempty() on unbuffered queue", decloc);
        }
        if (type.getPrimType() != Ptype.NULL)
            reg  = TDEVar.makeTDEVar(this, type.getWordSpec(this, decloc), decloc);
        if (assigned && (wordspec.numBits() != 0))
            dest = tdelist.namesignal(varDataName, wordspec, decloc);

        if ((iclk != null) && (!iclk.isEqualTo(pclk))) {
            // the input side signals have the wrong clock -
            // change their clocks to the input clock
            tde.add2ic(iclk.getClkSig());    // queue input clock
            if (dest != null)
                dest.setClkVar(iclk);
            push.setClkVar(iclk);
            ravsig.setClkVar(iclk);
            // change the default clock temporarily -
            // it will be restored when we have finished
            pushCurrentClock(iclk, decloc);
        } else
            tde.add2i(null);         // no separate queue input clock
        tde.add2ic(pclk.getClkSig()); // queue output clock
        
        // see if there is a 'FIFO' attribute
        Boolean     fifo = null;
        if ((attributes != null) && attributes.containsKey("fifo")) {
            if (bufsize <= getFamily().maxCRAMQueueDepth())
                msg("queue variable " + name + " - FIFO attribute on queue depth < 512 - ignored");
            else if (type.getPrimType() == Ptype.NULL)
                msg("queue variable " + name + " - FIFO attribute on null queue - ignored");
            else
                fifo = Boolean.valueOf(attributes.get("fifo").getSingleLval(decloc));
        }
        tde.add2p(fifo);
        
        // see if there is a 'QUEUEREG' attribute
        Boolean     queuereg = null;
        if ((attributes != null) && attributes.containsKey("queuereg")) {
            if (bufsize <= getFamily().maxCRAMQueueDepth())
                msg("queue variable " + name + " - QUEUEREG attribute on queue depth  512 - ignored");
            else if (type.getPrimType() == Ptype.NULL)
                msg("queue variable " + name + " - QUEUEREG attribute on null queue - ignored");
            else if (iclk != null)
                msg("queue variable " + name + " - QUEUEREG attribute on asynchronous queue - ignored");
            else
                queuereg = Boolean.valueOf(attributes.get("queuereg").getSingleLval(decloc));
        }
        tde.add2p(queuereg);

        int     words = (wordspec.numBits() != 0) ? wordspec.numWords() : 0;

        if (targ_init != null) {
            if (bufsize == 0)
                throw new ExEx("unbuffered queue '" + name +
                        "' (zero depth) cannot be initialised", decloc);
            if (!iclk.isEqualTo(pclk) || (bufsize > 2))
                throw new ExEx("variable '" + name +
                            "': only a synchronous queue of size 2 can be initialised",
                            decloc);
            String bi = initialise(targ_init, words, 0, words-1);
            tde.add2p(Functions.binToHex(bi));
        }

        TDE     orpw = null; // OR to combine enables into single PUSH
        orpw = new TDE(TDEType.OR, decloc);
        orpw.add2o(push);

        boolean queuefieldzero = true;
        
        if ((attributes != null) && attributes.containsKey("queuefieldzero"))
            queuefieldzero = attributes.get("queuefieldzero").getSingleLval(decloc);


        TDEVar[]    writenables = new TDEVar[words];
        if (assigned)
            assign(inputs, writenables, dest, null, words, queuefieldzero && partassign);
        
        // Connect up any iswritten() or waswritten() signals to an OR of the
        // write enable signals.
        for (int i=0 ; i<words ; i++)
            if (writenables[i] != null)
                orpw.add2i(writenables[i]);

        if (nullassigns != null) {
            // null writes
            Iterator<TDEVar>    it = nullassigns.iterator();
            while (it.hasNext())
                orpw.add2i(it.next());
        }
        
        if ((bufsize == 0) && (iclk != null) && (!iclk.isEqualTo(pclk))) {
            throw new ExEx("queue '" + name +
                        "' cannot have an asynchronous unbuffered queue", decloc);
        }
        
        if (bufsize == 0) {
            // For unbuffered queue change signal IDs -
            // write, change "PUSH" to "WPEND" (write pending)
            // ack, change "POP" to "RPEND" (read pending)
            push.setID(ename + ".WPEND");
            pop.setID(ename + ".RPEND");
        }

        tde.add2i(dest);
        tde.add2i(push);

        tde.add2o(reg); // reg is null if a trigger

        tdelist.addTDE(orpw);
        tde.add2i(pop);
        tde.add2o(ravsig);
        tde.add2o(wavsig);
        
        // If the first member of the resets array is not empty, process
        // the set ORing all reset signals together. The resulting OR
        // is the queue reset signal. If the first member of the resets
        // array is empty, connect the queue reset signal to logical low.
        // Add the reset signal to the queue.
        if (resets[0] != null) {
            Iterator<?>    it = resets[0].iterator();
            TDE         or = new TDE(TDEType.OR, decloc);
            while (it.hasNext())
                or.add2i((TDEVar)it.next());
            or.add2o(reset);
            tdelist.addTDE(or);
        } else {
            TDEVar  low = new TDEVar(Boolean.valueOf(false), Ptype.LOG, decloc);
            tdelist.connect(reset, low);
        }

        if (bufsize != 0)
            tde.add2i(reset);

        if (queuewords != null)
            tde.add2o(queuewords);
        else
            tde.add2o(null);
        if (queuespaces != null)
            tde.add2o(queuespaces);
        else
            tde.add2o(null);
         if (wstat != null)
            tde.add2o(wstat);
        else
            tde.add2o(null);
        if (rstat != null)
            tde.add2o(rstat);
        else
            tde.add2o(null);
        if (rreset != null)
            tde.add2o(rreset);

        tdelist.addTDE(tde);

        if ((iclk != null) && (!iclk.isEqualTo(pclk)))
            // restore the current clock now we have finished
            // creating the input-side logic of a queue buffer
            popCurrentClock(null);

        // OR together all pop signals for each
        // destination module and generate a TDEType.DIVERGE. If it turns out
        // there is only one destination module the TDEType.DIVERGE will simply
        // connect the queue .NE and .POP to the module .NE and .POP.
        int         dests = pops.entrySet().size();
        Iterator<?> itm = pops.entrySet().iterator();
        if (dests == 1) {
            // Connect pop from single destination module.
            // Within the module OR together all pop
            // signals (if more than one).
            Map.Entry<?, ?> mem = (Map.Entry<?, ?>)itm.next();
            HashSet<?>      a = (HashSet<?>)mem.getValue();
            TDEVar          modav = ravails.get(mem.getKey());
            Iterator<?>     its   = a.iterator();
            if (a.size() == 1)
                // only 1 read pop - connect it
                tdelist.connect(pop, (TDEVar)its.next());
            else {
                TDE or = new TDE(TDEType.OR, decloc);
                // iterate through queue read pops ORing them
                while (its.hasNext())
                    or.add2i((TDEVar)its.next());
                or.add2o(pop);
                tdelist.addTDE(or);
            }
            tdelist.connect(modav, ravsig);
        } else if (bufsize == 0) {
            // Unbuffered queue. AND the push signals (actually they are
            // pending signals in this case) rather than using a DIVERGE TDE.
            TDE         and = new TDE(TDEType.AND, decloc);
            TDE         popor = new TDE(TDEType.OR, decloc);
            TDEVar      andout = tdelist.signal("A", decloc);
            
            and.add2i(ravsig);
            and.add2o(andout);
            popor.add2o(pop);
            
            while (itm.hasNext()) {
                // iterate through modules
                Map.Entry<?, ?> mem    = (Map.Entry<?, ?>)itm.next();
                HashSet<?>      a      = (HashSet<?>)mem.getValue();
                TDE             modor  = new TDE(TDEType.OR, decloc);
                TDEVar          modav  = ravails.get(mem.getKey());
                Iterator<?>     its    = a.iterator();

                while (its.hasNext()) {
                    // iterate through queue read pops.
                    TDEVar  tdev = (TDEVar)its.next();
                    modor.add2i(tdev);
                    popor.add2i(tdev);
                }

                and.add2i(modor.finish());
                tdelist.connect(modav, andout);
            }
            
            tdelist.addTDE(and);
            tdelist.addTDE(popor);
        } else if (ignore_modules) {
            // If 'ignore_modules' is true then simply OR all
            // POP signals rather than generating a DIVERGE block.
            // This means that any queue read will pop the queue with
            // no dependency on any other potential queue reads.
            TDE or = new TDE(TDEType.OR, decloc);
            while (itm.hasNext()) {
                // iterate through modules
                Map.Entry<?, ?> mem = (Map.Entry<?, ?>)itm.next();
                HashSet<?>      a = (HashSet<?>)mem.getValue();
                TDEVar          modav = ravails.get(mem.getKey());
                Iterator<?>    its = a.iterator();

                // iterate through queue read pops.
                while (its.hasNext())
                    or.add2i((TDEVar)its.next());
                
                tdelist.connect(modav, ravsig);
            }
            or.add2o(pop);
            tdelist.addTDE(or);
        } else {
            TDE diverge = new TDE(TDEType.DIVERGE, decloc);

            diverge.add2ic(pclk.getClkSig());
            diverge.add2i(reset);
            diverge.add2i(ravsig);
            diverge.add2o(pop);

            // Connect all pops from destination modules.
            // Within each such module OR together all pop
            // signals.
            while (itm.hasNext()) {
                // iterate through modules
                Map.Entry<?, ?> mem = (Map.Entry<?, ?>)itm.next();
                HashSet<?>      a = (HashSet<?>)mem.getValue();
                TDE             or = new TDE(TDEType.OR, decloc);
                TDEVar          modav = ravails.get(mem.getKey());
                Iterator<?>    its = a.iterator();

                // iterate through queue read pops.
                while (its.hasNext())
                    or.add2i((TDEVar)its.next());

                diverge.add2i(or.finish());
                diverge.add2o(modav);
            }

            tdelist.addTDE(diverge);
            
            sinks = dests;
        }
        
        // Connect queue read availability operator signals
        // where used.
        Iterator<?>    oit = ravails.entrySet().iterator();
        while (oit.hasNext()) {
            Map.Entry<?, ?>   me = (Map.Entry<?, ?>)oit.next();
            Scope       ms = (Scope)me.getKey();    // module
            TDEVar      a = (TDEVar)me.getValue();  // avail op signal
            TDEVar      pavail;
            if (!ravails.containsKey(ms)) {
                // Avail signal not within a module that reads
                // or writes this queue.
                /*
                // Use the queue source .NE signal directly.
                pavail = ravsig;
                */
                Body        b = ms.getBody();
                if (b == null) {
                    // file module has no body
                    throw new ExEx("read availability tested in file" + 
                        " module \"" + ms.getFileModuleName() +
                        "\" that does not read the queue", decloc);
                }
                
                String      name = b.getName();
                if (name == null) {
                    // un-named module
                    throw new ExEx("read availability tested in an inline" + 
                        " module that does not read the queue, location " +
                        b.getSrcLoc().getFileName() + " line " +
                        b.getSrcLoc().getLineNo(), decloc);
                }
                
                String      btype;
                if (b instanceof Module)
                    btype = "module";
                else if (b instanceof Procedure)
                    btype = "procedure";
                else
                    btype = "function";
                
                SrcLoc  srcloc = b.getSrcLoc();
                SrcLoc  callloc = b.getCallLoc();
                if (callloc != null)
                    throw new ExEx("read availability tested in " + btype +
                            " " + name + " that does not read the queue," +
                            "\nlocation " +
                            srcloc.getFileName() + " line " +
                            srcloc.getLineNo()
                            + "\ncalled at " +
                            callloc.getFileName() + " line " +
                            callloc.getLineNo(), decloc);
                else
                    throw new ExEx("read availability tested in " + btype +
                            " " + name + " that does not read the queue," +
                            "\nlocation " +
                            srcloc.getFileName() + " line " +
                            srcloc.getLineNo());
            } else
                // Otherwise the availability signal is in
                // a destination module. Use the .NE signal that
                // was created when pop signals from
                // the destination module were added.
                pavail = ravails.get(ms);
            tdelist.connect(a, pavail);
        }
        
        if (queueempty != null)
                tdelist.connect(queueempty, tdelist.inv(ravsig, decloc));
    }

    /**
     * Register a pop of this queue variable by a destination module.
     *
     * The pop signal structure is a HashMap to which the
     * Module is the key. The value is a HashSet containing one
     * or more execution TDEVars. Thus for a queue variable we
     * have a map containing one or more modules. For each
     * module we have a set of one or more execution signals.
     * 
     * Note that redundant calls to this method for the same
     * execution signal in the same module do no harm as they will
     * map to the same single entry.
     * will be treated as if from within a single module.
     * 
     * @param    module_scope is the scope of the Module we are in
     * @param    tdev is the pop signal
     * @param    loc is the source file location
     */
    public void addPop (
        Scope   module_scope,
        TDEVar  tdev,
        SrcLoc  loc
    ) {
        if (tdev == null)
            return;
        
        LinkedHashSet<TDEVar> hs;
        if (pops.containsKey(module_scope))
            hs = pops.get(module_scope);
        else {
            hs = new LinkedHashSet<TDEVar>();
            pops.put(module_scope, hs);
        }
        hs.add(tdev);
    }
}
