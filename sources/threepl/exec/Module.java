package threepl.exec;

import static threepl.ThreePL.popBody;
import static threepl.ThreePL.pushBody;
import static threepl.ThreePL.pushScope;

import java.util.ArrayList;

import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;
import threepl.parser.Token;

/**
 * This class is a module definition.
 * It extends class 'Body' which contains most of the fields needed.
 */
public class Module extends Body implements Constant, TDEConstants {
    private Btype   btype;              // block type
    public int      order;              // integer for ordering leader and trailer modules in lists
    public String   ident = null;       // identifier for leader and trailer modules
    public boolean  called = false;

    /**
     * Construct a module definition.
     * @param   t is the module identifier token which provides both
     *          the module identifier and the source file location of the
     *          definition
     * @param   type is the block type (Btype.FMOD, Btype.NMOD or Btype.ILMOD)
     * @param   b is the enclosing module, procedure or function or is null
     *          for a file module
     * @param   filescope is the file scope for the module
     * @param   pvt is true if this module is declared as 'private'
     */
    public Module (Token t, Btype type, Body b, Scope filescope, boolean pvt) {
        super(t);
        btype = type;
        enclosing_body = b;
        /*if (filescope == null)
            throw new ExEx("SYSTEM ERROR - MODULE CREATED WITH NULL FILE SCOPE");*/
        file_scope = filescope;
        is_private = pvt;
    }
    
    /**
     * Execute a module.
     * @param   inargs is a list of input argument tree nodes
     * @param   outargs is a list of output argument tree nodes
     * @param   call_loc is the source file location of the module call
     */
    public void execute (
            NodeList inargs,
            NodeList outargs,
            SrcLoc call_loc
    ) {
        String      mess = "module '" + name + "' ";
        ArrayList<TDEVar>   bstartl = new ArrayList<TDEVar>();
        ArrayList<TDEVar>   bfinishl = new ArrayList<TDEVar>();
        
        if (btype == Btype.FMOD) {
            if (called)
                throw new ExEx("File module '" + name + "' called more than once.");
            called = true;
            scope = file_scope;
        } else
            scope = new Scope(this, btype);
        
        // evaluate the arguments before we push the new scope or the file scope
        getArgs(inargs, true, mess);  // input arguments
        getArgs(outargs, false, mess); // output arguments
        num_input_args = (in_args == null) ? 0 : in_args.size();
        num_output_args = (out_args == null) ? 0 : out_args.size();
        
        // Push on the body stack.
        pushBody(this);

        // The scope block is pushed on the stack here, not
        // BlockNode.execute(), so it is available early to receive the
        // parameter variables. The scope is initially marked so that searches
        // for variables etc can proceed back up the stack past this scope so
        // that we can evaluate arguments. This is changed at the end of
        // argument evaluation below.
        pushScope(scope, call_loc);

        // create and link the input and output parameters
        params(mess, true, call_loc);
        params(mess, false, call_loc);

        // Execute the code body.
        tree.execute(null, bstartl, bfinishl, new QueueRefs(), true, false, null, null, null);

        // Pop off the body stack.
        popBody();
         
        // The scope block has already been popped off the
        // scope block stack in BlockNode.execute().   
    }
    
    /**
     * Get the block type for this module.
     * @return The block type
     */
    public Btype getBtype () { return(btype); }
}
