package threepl.funcs;

import java.io.File;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to delete a named file.
 * This has one argument which is the file path string.
 * The function returns true if it succeeds.
 */
public class FileDeleteFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the file
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        if (args.size() != 1)
            throw new ExEx("filedelete() must have 1 argument", loc);
        Val     val = args.getVal(0);
        if (val.getMode() != Mode.IMMEDIATE)
            throw new ExEx("filedelete() argument not immediate mode", loc);
        if (val.getPrimType() != Ptype.STR)
            throw new ExEx("filedelete() argument not a string", loc);
        String  name = val.getSingleSval(loc);
        boolean ret;
        try {
            File file = new File(name);
            ret = file.delete();
        } catch (SecurityException e) {
            throw new ExEx("filedelete() - access problem", loc);
        }
        return(new Val(ret, loc));
    }
}
