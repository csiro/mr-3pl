package threepl.funcs;

import threepl.ThreePL;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to return the stack trace. It has no arguments.
 */
public class StackTraceFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the identifier string
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();        
        return(new Val(ThreePL.stackTrace(), loc));
    }
}
