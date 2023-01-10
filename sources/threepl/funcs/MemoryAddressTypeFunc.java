package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Memory;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to return the address type of a cmemory or
 * rmemory mode variable.
 */
public class MemoryAddressTypeFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the type
     */
    public Val getVal (NodeList args) {
        SrcLoc loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("memoryaddresstype() must have single argument", loc);

        Ref ref = args.getRef(0, "memoryaddresstype()");
        Mode    m = ref.getMode();
        if ((m != Mode.CMEMORY) && (m != Mode.RMEMORY)) {
            String  s = m.modename();
            throw new ExEx("memoryaddresstype() argument mode " + s + " - should be cmemory or rmemory mode", loc);
        }
        Memory  mem = (Memory)(ref.getVar());
        return(new Val(mem.getMemAddrType(), loc));
    }
}
