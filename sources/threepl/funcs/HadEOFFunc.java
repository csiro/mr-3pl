package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.FileDesc;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * See if an end-of-file resulted from the last read.
 */
public class HadEOFFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  true if the last read was at end-of-file
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        Val     val;

        val = args.getVal(0);
        if (val.getMode() != Mode.IMMEDIATE)
            throw new ExEx("hadeof() argument not immediate mode", loc);
        if (val.getPrimType() != Ptype.FILE)
            throw new ExEx("hadeof() argument not a file", loc);
        FileDesc file = val.getSingleFileVal(loc);
        if (file == null)
            throw new ExEx("hadeof() file is not open", loc);
        return(new Val(file.getEOF(), loc));
    }
}
