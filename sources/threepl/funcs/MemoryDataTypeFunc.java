package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Memory;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to return the data type of a cmemory or
 * rmemory mode variable.
 */
public class MemoryDataTypeFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the type
     */
    public Val getVal (NodeList args) {
        SrcLoc loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("memorydatatype() must have single argument", loc);

        Ref ref = args.getRef(0, "memoryaddresstype()");
        if ((ref.getMode() != Mode.CMEMORY) && (ref.getMode() != Mode.RMEMORY))
            throw new ExEx("memorydatatype() argument not mode cmemory or rmemory", loc);
        Memory  mem = (Memory)(ref.getVar());
        return(new Val(mem.getMemDataType(), loc));
    }
}
