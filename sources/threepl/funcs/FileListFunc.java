package threepl.funcs;

import java.io.File;

import threepl.exceptions.ExEx;
import threepl.exec.FileDesc;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.exec.WordSpec;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to list files and directories.
 * This has one argument which is the file path string or open file
 * of the directory to be listed.
 * The function returns an array of strings.
 */
public class FileListFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the file
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        if (args.size() != 1)
            throw new ExEx("filelist() must have 1 argument", loc);
        Val     val = args.getVal(0);
        if (val.getMode() != Mode.IMMEDIATE)
            throw new ExEx("filelist() argument not immediate mode", loc);
        if (val.getPrimType() != Ptype.STR)
            throw new ExEx("filelist() argument not a string", loc);
        File        file = null;
        String[]    va;

        switch (val.getPrimType()) {
        case STR:
            String  path = val.getSingleSval(loc);
            file = new File(path);
            break;
        case FILE:
            FileDesc    of = val.getSingleFileVal(loc);
            if (of == null)
                throw new ExEx("filelist() file argument has not been opened", loc);
            file = of.getFile();
            break;
        default:
            throw new ExEx("filelist() argument not a file or a string", loc);
        }

        try {
            va = file.list();
        } catch (SecurityException e) {
            throw new ExEx("filelist() - access problem", loc);
        }
        if (va == null)
            throw new ExEx("filelist() - argument not a directory (or I/O error)", loc);
        int         n = va.length;
        Type        type = new Type("[" + n + "]str", loc);
        Type        pt = new Type("str", loc);
        WordSpec    ws = type.getWordSpec(null, loc);
        Type[]      ta = new Type[n];
        for (int i=0 ; i<n ; i++)
            ta[i] = pt;

        return(new Val(va, ta, ws, null, null, loc));
    }
}
