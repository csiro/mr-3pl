package threepl.funcs;

import java.io.File;

import threepl.exceptions.ExEx;
import threepl.exec.FileDesc;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the time a named file or directory was last modified.
 * This has one argument which is the file path string.
 * The function returns the time of last modification.
 */
public class FileLastModFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the file
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        if (args.size() != 1)
            throw new ExEx("filelastmod() must have 1 argument", loc);
        Val     val = args.getVal(0);
        if (val.getMode() != Mode.IMMEDIATE)
            throw new ExEx("filelastmod() argument not immediate mode", loc);
        File    file = null;
        long    ret;
        switch (val.getPrimType()) {
        case STR:
            String  path = val.getSingleSval(loc);
            file = new File(path);
            break;
        case FILE:
            FileDesc    of = val.getSingleFileVal(loc);
            if (of == null)
                throw new ExEx("filelastmod() file argument has not been opened", loc);
            file = of.getFile();
            break;
        default:
            throw new ExEx("filelastmod() argument not a file or a string", loc);
        }
        try {
            ret = file.lastModified();
        } catch (SecurityException e) {
            throw new ExEx("filelastmod() - access problem", loc);
        }

        return(new Val(ret, loc));
    }
}
