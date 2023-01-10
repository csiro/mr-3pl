package threepl.funcs;

import static threepl.ThreePL.*;

import threepl.exceptions.ExEx;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.exec.WordSpec;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the include directories specified by the
 * -I commmand line option and the directory procedure.
 * This has no arguments.
 * The function returns an array of strings.
 */
public class IncludeDirsFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the file
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        if (args.size() != 0)
            throw new ExEx("includedirs() must have no arguments", loc);
        
        int         len = include_dirs.size();
        Type        type = new Type("[" + len + "]str", loc);
        Type        pt = new Type("str", loc);
        WordSpec    ws = type.getWordSpec(null, loc);
        String[]    va = new String[len];
        Type[]      ta = new Type[len];
        int         i = 0;
        for (String s : include_dirs) {
            va[i] = s;
            ta[i++] = pt;
        }

        return(new Val(va, ta, ws, null, null, loc));
    }
}
