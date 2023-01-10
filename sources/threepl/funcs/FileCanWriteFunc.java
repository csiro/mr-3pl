package threepl.funcs;

import java.io.File;

import threepl.exceptions.ExEx;
import threepl.exec.FileDesc;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to determine if a named file or open file can be written.
 * This has one argument which is the file path string or an open file.
 * The function returns true if the file can be read.
 */
public class FileCanWriteFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the file
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        if (args.size() != 1)
            throw new ExEx("filecanwrite() must have 1 argument", loc);
        Val     val = args.getVal(0);
        if (val.getMode() != Mode.IMMEDIATE)
            throw new ExEx("filecanwrite() argument not immediate mode", loc);
        File    file = null;
        boolean ret;

        switch (val.getPrimType()) {
        case STR:
            String  path = val.getSingleSval(loc);
            file = new File(path);
            try {
                ret = file.canWrite();
            } catch (SecurityException e) {
                throw new ExEx("filecanwrite() - access problem", loc);
            }
            break;
        case FILE:
            FileDesc    of = val.getSingleFileVal(loc);
            if (of == null)
                throw new ExEx("filecanwrite() file argument has not been opened", loc);
            ret = of.isOutput();
            break;
        default:
            throw new ExEx("filecanwrite() argument not a file or a string", loc);
        }

        return(new Val(ret, loc));
    }
}
