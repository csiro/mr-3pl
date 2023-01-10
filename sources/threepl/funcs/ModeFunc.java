package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to return the mode of a variable.
 * The mode is a string with the following values -
 * <ul>
 *   <li>"type"
 *   <li>"immediate"
 *   <li>"value"
 *   <li>"selectvalue"
 *   <li>"static"
 *   <li>"queue"
 *   <li>"memory"
 * </ul>
 */
public class ModeFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the mode code
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("mode() must have single argument", loc);

        Ref ref = args.getRef(0, "mode()");
        return(new Val(ref.getMode().modename(), loc));
    }
}
