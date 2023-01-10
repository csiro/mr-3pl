package threepl.funcs;

import java.io.File;

import threepl.exceptions.ExEx;
import threepl.exec.FileDesc;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to determine if a named file or an open file
 * is a directory.
 * This has one argument which is the file path string or open file.
 * The function returns true if the file is a directory.
 */
public class FileIsDirFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the file
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        if (args.size() != 1)
            throw new ExEx("fileisdir() must have 1 argument", loc);
        Val     val = args.getVal(0);
        if (val.getMode() != Mode.IMMEDIATE)
            throw new ExEx("fileisdir() argument not immediate mode", loc);
        File    file = null;
        boolean ret;

        switch (val.getPrimType()) {
        case STR:
            String  path = val.getSingleSval(loc);
            file = new File(path);
            break;
        case FILE:
            FileDesc    of = val.getSingleFileVal(loc);
            if (of == null)
                throw new ExEx("fileisdir() file argument has not been opened", loc);
            file = of.getFile();
            break;
        default:
            throw new ExEx("fileisdir() argument not a file or a string", loc);
        }
        
        try {
            ret = file.isDirectory();
        } catch (SecurityException e) {
            throw new ExEx("fileisdir() - access problem", loc);
        }
        return(new Val(ret, loc));
    }
}
