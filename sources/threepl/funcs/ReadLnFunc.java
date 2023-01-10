package threepl.funcs;

import java.io.BufferedReader;
import java.io.IOException;

import threepl.exceptions.ExEx;
import threepl.exec.FileDesc;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to read a line from an input file.
 * This has a single argument which must be an open file variable.
 */
public class ReadLnFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the read line string
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        if (args.size() != 1)
            throw new ExEx("readln() - must have single argument", loc);

        Val val = args.getVal(0);
        if (val.getMode() != Mode.IMMEDIATE)
            throw new ExEx("readln() - argument not a file variable", loc);
        if (val.getPrimType() != Ptype.FILE)
            throw new ExEx("readln() - argument not a file variable", loc);
        FileDesc file = val.getSingleFileVal(loc);
        if (file == null)
            throw new ExEx("readln() - file is not open", loc);
        BufferedReader br = file.getBufferedReader("readln() - ", loc);
        if (br == null)
            throw new ExEx("readln() - file is not open for reading", loc);

        String  s = null;
        try {
            s = br.readLine();
        } catch (IOException e) {
            throw new ExEx("readln() - file read error on file '" + file.getPath() + "'", loc);
        }
        if (s == null) {
            file.setEOF();
            s = "";
        }
        return(new Val(s, loc));
    }
}
