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
 * An inbuilt function to list directories (not files).
 * This has one argument which is the file path string of the directory to
 * be listed.
 * The function returns an array of strings.
 */
public class FileListDirsFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the file
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        if (args.size() != 1)
            throw new ExEx("filelistdirs() must have 1 argument", loc);
        Val     val = args.getVal(0);
        if (val.getMode() != Mode.IMMEDIATE)
            throw new ExEx("filelistdirs() argument not immediate mode", loc);
        if (val.getPrimType() != Ptype.STR)
            throw new ExEx("filelistdirs() argument not a string", loc);

        File    file = null;
        File[]  files = null;

        switch (val.getPrimType()) {
        case STR:
            String  path = val.getSingleSval(loc);
            file = new File(path);
            break;
        case FILE:
            FileDesc    of = val.getSingleFileVal(loc);
            if (of == null)
                throw new ExEx("filelistdirs() file argument has not been opened", loc);
            file = of.getFile();
            break;
        default:
            throw new ExEx("filelistdirs() argument not a file or a string", loc);
        }

        // files and directories
        try {
            files = file.listFiles();
        } catch (SecurityException e) {
            throw new ExEx("filelistdirs() - access problem", loc);
        }
        
        int len = files.length;
        int n = 0;
        String[]    va = new String[len];
        for (int i=0 ; i<len ; i++)
            if (files[i].isDirectory())
                va[n++] = files[i].getName();

        Type        type = new Type("[" + n + "]str", loc);
        Type        pt = new Type("str", loc);
        WordSpec    ws = type.getWordSpec(null, loc);
        Type[]      ta = new Type[n];
        for (int i=0 ; i<n ; i++)
            ta[i] = pt;

        return(new Val(va, ta, ws, null, null, loc));
    }
}
