package threepl.exec;

import static threepl.ThreePL.popBody;
import static threepl.ThreePL.pushBody;
import static threepl.ThreePL.pushScope;
import java.util.ArrayList;

import threepl.ThreePL;
import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.nodes.Ident;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;
import threepl.parser.Token;

/**
 * This class is a 3PL class definition.
 * The class identifier is 'Group' to avoid conflict and confusion with
 * the Java keyword 'class'
 * It extends class 'Body' which contains most of the fields needed.
 */
public class Group extends Body implements Constant, TDEConstants {
    public int      order;              // integer for ordering leader and trailer modules in lists
    public String   ident;              // identifier for leader and trailer modules
    public boolean  called = false;

    /**
     * Construct a group definition.
     * @param   t is the group identifier token which provides both
     *          the group identifier and the source file location of the
     *          definition
     * @param   b is the enclosing group, named module, procedure or function
     * @param   filescope is the file scope for the group
     */
    public Group (Token t, Body b, Scope filescope) {
        super(t);
        enclosing_body = b;
        file_scope = filescope;
    }
    
    /**
     * Execute a group.
     * @param   inargs is a list of input argument tree nodes
     * @param   outargs is a list of output argument tree nodes
     * @param   call_loc is the source file location of the module call
     */
    public void execute (
            NodeList inargs,
            NodeList outargs,
            SrcLoc call_loc
    ) {
        String      mess = "class '" + name + "' ";
        ArrayList<TDEVar>   bstartl = new ArrayList<TDEVar>();
        ArrayList<TDEVar>   bfinishl = new ArrayList<TDEVar>();
        
        scope = new Scope(this, Btype.GROUP);
        
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
        
        // If there is a common block code tree execute it on the first call but delete
        // the tree so that it is not executed on further calls.
        if (common_tree != null) {
            setClassCommonScope(new Scope(this, Btype.INIT));
            pushScope(class_common_scope, call_loc);
            common_tree.execute(null, bstartl, bfinishl, new QueueRefs(), true, false, null, null, null);
            // scope is already popped on exit from execute()
            if (bstartl.size() != 0)
                throw new ExEx("class common block cannot contain in-line target statements", common_tree.getSrcLoc());
            common_tree = null;
        }
        
        // Create a variable 'this' for the current class scope.
        Immediate   iv = new Immediate(new Ident("this"), Type.CLASS, new Val(scope, loc), false, false, loc);
        ThreePL.addVar(iv, Context.DEFAULT, loc);

        // Execute the code body.
        tree.execute(null, bstartl, bfinishl, new QueueRefs(), true, false, null, null, null);
        
        if (bstartl.size() != 0)
            throw new ExEx("class cannot contain in-line target statements", loc);
        
        // If this Group (3PL class) contains a trailermodule add it to the trailermodule list.
        // The scope of the Group (3PL class) is included in the call. Thus the trailer module
        // can access the class variables and class common variables (as well as file module
        // variables and global variables).
        if (modules.containsKey("t mod")) {
            Module  m = modules.get("t mod");
            ThreePL.addTrailerModule(m, scope, m.name, m.order);
        }

        // Pop off the body stack.
        popBody();
         
        // The scope block has already been popped off the
        // scope block stack in BlockNode.execute().   
    }
    
    /**
     * Call a group (3PL class) as a function and return the Scope of the code
     * body as a Val of type "class".
     * @param   outargs is a list of output argument tree nodes
     * @param   inargs is a list of input argument tree nodes
     * @return  the Scope of the module code block
     */
    public Val getVal (NodeList outargs, NodeList inargs) {
        execute(inargs, outargs, loc);
        return (new Val(scope, loc));
    }
    /**
     * Set the Scope for this Group (3PL class) instance.
     * @param scope is the new Scope
     */
    public void setScope (Scope scope) { this.scope = scope; }
}
