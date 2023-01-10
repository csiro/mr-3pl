package threepl.funcs;

import java.io.File;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to create a directory.
 * This has one argument which is the file path string.
 * The function returns the true if the directory was created.
 */
public class FileMkDirFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the file
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        if (args.size() != 1)
            throw new ExEx("filemkdir() must have 1 argument", loc);
        Val     val = args.getVal(0);
        if (val.getMode() != Mode.IMMEDIATE)
            throw new ExEx("filemkdir() argument not immediate mode", loc);
        if (val.getPrimType() != Ptype.STR)
            throw new ExEx("filemkdir() argument not a string", loc);
        String  name = val.getSingleSval(loc);
        boolean ret;
        try {
            File file = new File(name);
            ret = file.mkdir();
        } catch (SecurityException e) {
            throw new ExEx("filemkdir() - access problem", loc);
        }
        return(new Val(ret, loc));
    }
}
