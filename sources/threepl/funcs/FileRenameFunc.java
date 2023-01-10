package threepl.funcs;

import java.io.File;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to rename a named file or directory.
 * This has two arguments. The first is the file path string of the file
 * or directory to be renamed. The second is the new path string.
 * The function returns true if it succeeds.
 */
public class FileRenameFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the file
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        if (args.size() != 2)
            throw new ExEx("filerename() must have 2 arguments", loc);
        Val     val0 = args.getVal(0);
        Val     val1 = args.getVal(1);
        if (val0.getMode() != Mode.IMMEDIATE)
            throw new ExEx("filerename() 1st argument not immediate mode", loc);
        if (val0.getPrimType() != Ptype.STR)
            throw new ExEx("filerename() 1st argument not a string", loc);
        if (val1.getMode() != Mode.IMMEDIATE)
            throw new ExEx("filerename() 2nd argument not immediate mode", loc);
        if (val1.getPrimType() != Ptype.STR)
            throw new ExEx("filerename() 2nd argument not a string", loc);
        String  name0 = val0.getSingleSval(loc);
        String  name1 = val1.getSingleSval(loc);

        boolean ret;
        File fileOld = null;
        File fileNew = null;
        try {
            fileOld = new File(name0);
        } catch (SecurityException e) {
            throw new ExEx("filerename() - cannot access file '" + name0 + "'", loc);
        }
        try {
            fileNew = new File(name1);
        } catch (SecurityException e) {
            throw new ExEx("filerename() - cannot access file '" + name1 + "'", loc);
        }
        try {
            ret = fileOld.renameTo(fileNew);
        } catch (SecurityException e) {
            throw new ExEx("filerename() - cannot rename '" + name0 + "' to '" + name1 + "'", loc);
        }
        return(new Val(ret, loc));
    }
}
