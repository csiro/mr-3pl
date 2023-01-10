package threepl.funcs;

import java.io.File;

import threepl.exceptions.ExEx;
import threepl.exec.FileDesc;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the parent directory of a named file or directory.
 * This has one argument which is an open file.
 * The function returns the parent directory path or empty string.
 */
public class FileGetParentFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the file
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        if (args.size() != 1)
            throw new ExEx("filegetparent() must have 1 argument", loc);
        Val     val = args.getVal(0);
        if (val.getMode() != Mode.IMMEDIATE)
            throw new ExEx("filegetparent() argument not immediate mode", loc);
        if (val.getPrimType() != Ptype.FILE)
            throw new ExEx("filegetparent() argument not open file", loc);

        File    file = null;
        String  ret;

        FileDesc    of = val.getSingleFileVal(loc);
        if (of == null)
            throw new ExEx("filegetparent() file argument has not been opened", loc);
        file = of.getFile();

        try {
            ret = file.getParentFile().getAbsolutePath();
        } catch (SecurityException e) {
            throw new ExEx("filegetparent() - access problem", loc);
        }
        if (ret == null)
            ret = "";
        
        return(new Val(ret, loc));
    }
}
