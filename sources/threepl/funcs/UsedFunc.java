package threepl.funcs;

import static threepl.ThreePL.getCurrentBody;
import threepl.exceptions.ExEx;
import threepl.exec.Body;
import threepl.exec.Function;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function called within a user-defined target function to return a
 * value of type "log" which is true when the user-defined function is
 * evaluated. It has no arguments.
 */
public class UsedFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes (none)
     * @return  a signal of type "log" which is true when this function
     *          actually executes
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 0)
            throw new ExEx("used() must have no arguments", loc);

        Body    body = getCurrentBody();
        if (!(body instanceof Function))
            throw new ExEx("used() must must be called within a function", loc);
        Function    f = (Function)body;

        return(f.setUsedFunc());
    }
}
