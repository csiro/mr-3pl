package threepl.funcs;

import java.io.FileInputStream;
import java.io.IOException;

import threepl.exceptions.ExEx;
import threepl.exec.FileDesc;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to read a byte from an input file.
 * This has a single argument which must be an open file variable.
 */
public class ReadByteFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the read line string
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        if (args.size() != 1)
            throw new ExEx("readbyte() - must have single argument", loc);

        Val val = args.getVal(0);
        if (val.getMode() != Mode.IMMEDIATE)
            throw new ExEx("readbyte() - argument not a file variable", loc);
        if (val.getPrimType() != Ptype.FILE)
            throw new ExEx("readbyte() - argument not a file variable", loc);
        FileDesc file = val.getSingleFileVal(loc);
        if (file == null)
            throw new ExEx("readbyte() - file is not open", loc);
        FileInputStream fis = file.getFileInputStream("readbyte() - ", loc);
        if (fis == null)
            throw new ExEx("readbyte() - file is not open for reading", loc);

        int  b = 0;
        try {
            b = fis.read();
        } catch (IOException e) {
            throw new ExEx("readbyte() - file read error on file '" + file.getPath() + "'", loc);
        }
        if (b == -1)
            file.setEOF();
        return(new Val(b, loc));
    }
}
