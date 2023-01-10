package threepl.exec;

import static threepl.ThreePL.clockdiag;
import static threepl.ThreePL.getCurrentClockVar;
import static threepl.ThreePL.msg;
import static threepl.ThreePL.stackTrace;
import static threepl.ThreePL.tdelist;

import threepl.codegen.TDEConstants;
import threepl.exceptions.ExEx;
import threepl.nodes.Ident;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * This class holds the primary clock fields common to clocked target
 * variable classes 'SelectValue', 'Static', 'Queue', 'Priority', 'Input'
 * and 'Output'.
 *
 * For class 'Queue' the primary clock is the output (read) clock. The class
 * contains another clock, 'iclk', for input (write).
 *
 * For class 'Output' the primary clock is the input (write) clock.
 *
 * For class 'Input' the primary clock is the output (read) clock.
 *
 * For the other classes, 'SelectValue', 'Static' and 'Priority', the
 * primary clock applies to both input (write) and output (read).
 * 
 */
public class ClockedVar extends Var implements Constant, TDEConstants {
    protected Clock           pclk;       // primary (output) clock
    protected String          pclk_trace; // call trace where pclk set
    protected Calloc          pclk_event; // event setting pclk

    /**
     * Construct a clocked target mode variable.
     * @param   ident is the variable identifier
     * @param   in_par is true if this is a module, procedure of function
     *          input parameter
     * @param   out_par is true if this is a module or procedure output
     *          parameter
     * @param   type is the variable type or null
     * @param   etype is the type to use if type is null, or is NONE to skip
     *          setting the type field at all
     * @param   loc is the source file location
     */
    public ClockedVar (
        Ident   ident,
        boolean in_par,
        boolean out_par,
        Type    type,
        Ptype   etype,
        SrcLoc  loc
    ) {
        super(ident, in_par, out_par, type, etype, loc);
    }
        
    /**
     * Get the output clock variable associated with this variable.
     * @param   loc is the source file location
     * @return  the output (or only) clock Var
     */
    public Clock getOutputClkVar (SrcLoc loc) {
        if (indass)
            return(indirect.getOutputClkVar(loc));
        return(pclk);
    }
    
    /**
     * Get/set the output clock variable associated with this variable.
     * If it is null, set it to the current clock before returning it.
     * @param   calloc is the clock allocation event type
     * @param   loc is the source file location
     * @return  the primary clock Var
     */
    public Clock getOutputClkVar (Calloc calloc, SrcLoc loc) {
        if (indass)
            return(indirect.getOutputClkVar(calloc, loc));
        setOutputClock(getCurrentClockVar(), calloc, true, loc);
        return(pclk);
    }

    /**
     * Set the input clock, which is the primary clock - input and output use the same primary clock.
     * Class Queue overrides this method as input and output clocks may differ.
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
        setClock(clk, e, loc);
    }

    /**
     * Set the output clock, which is the primary clock - input and output use the same primary clock.
     * Class Queue overrides this method as input and output clocks may differ.
     * @param   clk is the clock domain variable
     * @param   e is the event setting the clock
     * @param   used if true sets the used field of the variable
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
        setClock(clk, e, loc);
    }
    
    /**
     * Check/set the primary clock.
     * Not called for class Queue.
     * @param   clk is the clock domain variable
     * @param   e is the event setting the clock
     * @param   loc is the source file location
     */
    private void setClock (Clock clk, Calloc e, SrcLoc loc) {
        if (clk == null)
            return;
        
        String  m = mode.name();
        if ((clk != null) && (pclk != null)) {
            if (!pclk.isEqualTo(clk)) {
                msg(m + " variable '" + name +
                    "' - attempt to change clock domain");
                msg("previous clock " + pclk.getID(IDtype.LITERAL) + " allocated by " +
                    pclk_event.event() + " at location -");
                msg(pclk_trace);
                throw new ExEx("second clock " + clk.getID(IDtype.LITERAL) +
                               " allocated by " + e.event() +
                               " at location -", loc);
            } else if (clockdiag)
                System.out.println(loc + " " + m + " " + ename +
                            " clks checked - " + clk.getID(IDtype.LITERAL) + " " + e.event());
        } else {
            if (clockdiag)
                System.out.println(loc + " " + m + " " + ename +
                            " clks set - " + clk.getID(IDtype.LITERAL) + " " + e.event());
            pclk = clk;
            pclk_trace = loc.toString() + "\n" + stackTrace();
            pclk_event = e;
            Val cv = new Val(clk, Mode.CLOCK, clk.getClkSig(), loc);
            attributes.put("domain", cv);
            // Declare the signal array for the variable, as long as
            // it has a type and that type is not NULL.
            // A value mode signal name will only be used if it is
            // referenced prior to assignment, for which reason it
            // is declared. In most cases that declaration is not needed.
            if ((type != null) && (type.getPrimType() != Ptype.NULL))
                tdelist.namesignal(ename, wordspec, loc);
        }
    }
    
    /**
     * Get the queue input (write) clock variable associated with this variable.
     * This method can only be called for subclass 'Queue' where
     * the method here in super class 'Var' is overridden!
     * @return  the input clock Var
     */
    public Clock getQueueWriteClkVar () {
        throw new ExEx("CODE ERROR - " + getClass().getName() + ".getQueueWriteClkVar() called");
    }
    
    /**
     * Get a memory port clock variable.
     * This method can only be called for subclass 'Memory' where
     * the method here in super class 'Var' is overridden!
     * @param   i is the port index
     * @return  the memory port clock Var
     */
    public Clock getMemClkVar (int i) {
        throw new ExEx("CODE ERROR - " + getClass().getName() + ".getMemClkVar(int) called");
    }
    
    /**
     * Set a memory port clock variable.
     * This method can only be called for subclass 'Memory' where
     * the method here in super class 'Var' is overridden!
     * @param   i is the port index
     * @param   v is the memory port clock Var
     */
    public void setMemClkVar(int i, Clock v) {
        throw new ExEx("CODE ERROR - " + getClass().getName() + ".setMemClkVar(int,Clock) called");
    }
}

