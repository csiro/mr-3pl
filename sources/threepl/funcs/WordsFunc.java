package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Ref;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.exec.WordSpec;
import threepl.nodes.Node;
import threepl.nodes.NodeList;
import threepl.nodes.VarNode;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the number of words in a  variable.
 * This has 1 argument, which is the variable.
 */
public class WordsFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the size
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("words() must have a single argument", loc);
        
        Node    n = args.getNode(0);
        if (!(n instanceof VarNode))
            throw new ExEx("words() argument must be a variable", loc);

        Ref     ref = args.getRef(0, "size()");
        Type    type = ref.getWordSpec().getType();
        Mode    mode = ref.getMode();
        
        switch (mode) {
        case CMEMORY:
        case RMEMORY:
            throw new ExEx("words() argument cannot be a memory mode", loc);        
        case CLOCK:
            throw new ExEx("words() argument cannot be clock mode", loc);        
        default:
            switch (type.getPrimType()) {
            case NULL:
                return(new Val(0, loc));
            case TYPE:
                Type    t = args.getVal(0).getSingleTval(loc);
                return(new Val(t.getWordSpec(null, loc).numWords(), loc));
            default:
                WordSpec    ws = ref.getWordSpec();
                if (ws.getNumTargSubs() != 0)
                    return(new Val(ws.getType().numWords(), loc));
                else
                    return(new Val(ws.numWords(), loc));
            }
        }
    }
}
