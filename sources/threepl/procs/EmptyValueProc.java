package threepl.procs;

import java.util.ArrayList;

import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.QueueRefs;
import threepl.exec.Ref;
import threepl.exec.Value;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to change a value variable to type "empty".
 * There may be any number of arguments but each must be a value variable.
 */
public class EmptyValueProc extends InbuiltProc implements Constant, TDEConstants {

    /**
     * Construct the inbuilt procedure emptyvalue().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public EmptyValueProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
    }

    /**
     * Execute the procedure rmemoryread(). This does not generate in_line
     * executable code.
     * @param   inargs is a list of input argument tree nodes
     * @param   outargs is a list of output argument tree nodes
     * @param   esig is an exception/restart signal, or null
     * @param   startl is a list of start signals for target statements
     *          below this node
     * @param   finishl is a list of finish signals for target statements
     *          below this node
     * @param   queues returns all the queue availability signals
     *          accumulated from code below
     * @param   availok is true if a previous sync makes a queue availability
     *          wait unnecessary
     */
    public void execute (
        NodeList            inargs,
        NodeList            outargs,
        TDEVar              esig,
        ArrayList<TDEVar>   startl, 
        ArrayList<TDEVar>   finishl,
        QueueRefs            queues,
        boolean             availok
    ) {
        SrcLoc  loc = inargs.getCallLoc();
        
        if (outargs.size() != 0)
            throw new ExEx("emptyvalue() must have no output arguments", loc);

        for (int i=0 ; i<inargs.size() ; i++) {
            Ref ref = inargs.getRef(i, "emptyvalue() - ");
            if (ref.getMode() != Mode.VALUE)
                throw new ExEx("emptyvalue() input argument " + (i+1) + " must be mode VALUE", loc);
            Value v = (Value)ref.getVar();
            v.empty();
        }
    }
}
