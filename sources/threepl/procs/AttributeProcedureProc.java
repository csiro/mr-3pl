package threepl.procs;

import threepl.ThreePL;
import threepl.codegen.TDEConstants;
import threepl.exceptions.ExEx;
import threepl.exec.Procedure;
import threepl.nodes.Ident;
import threepl.nodes.Node;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to notify 3PL of an attribute processing procedure
 * to be called after execution of inbuilt procedures input(), output() and clock().
 */
public class AttributeProcedureProc extends InbuiltProc implements Constant, TDEConstants {

    /**
     * Construct the inbuilt procedure attributeprocedure().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public AttributeProcedureProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
    }

    /**
     * Execute the procedure attributeprocedure(). This does not generate in-line
     * executable code.
     * @param   inargs is a list of input argument tree nodes
     * @param   outargs is a list of output argument tree nodes
     * @param   toplevel is true if we are at the module, procedure or function
     *          level
     */
    public void execute (
        NodeList    inargs, 
        NodeList    outargs, 
        boolean     toplevel
    ) {
        SrcLoc      loc = inargs.getCallLoc();
        if (inargs.size() != 1)
            throw new ExEx("attributeprocedure() must have 1 input argument", loc);
        if (outargs.size() != 0)
            throw new ExEx("attributeprocedure() must have no output arguments", loc);

        Node        n = inargs.getNode(0);
        Ident   id = n.getIdentifier("attributeprocedure() - argument");
        Procedure   p = ThreePL.findProc(id.getId());
        ThreePL.attributesProc = p;
    }
}
