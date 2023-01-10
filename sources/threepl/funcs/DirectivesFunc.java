package threepl.funcs;

import static threepl.ThreePL.*;

import threepl.exceptions.ExEx;
import threepl.exec.SubFieldList;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.exec.WordSpec;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the directives map.
 * This has no arguments.
 * It returns a map of directives
 */
public class DirectivesFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the directive value
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        if (args.size() != 0)
            throw new ExEx("directives() must have no arguments", loc);
               
        Object[]    oa = new Object[1];
        Type[]      ta = new Type[1];
        Type        type = new Type(Ptype.MAP, 0);
        WordSpec    ws = type.getWordSpec(null, loc);
        oa[0] = directives;
        ta[0] = type;
        return(new Val(oa, ta, ws, null, new SubFieldList(), loc));
    }
}
