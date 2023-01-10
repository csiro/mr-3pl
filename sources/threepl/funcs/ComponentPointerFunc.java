package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Queue;
import threepl.exec.Ref;
import threepl.exec.Static;
import threepl.exec.Val;
import threepl.exec.Value;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to return a pointer to a component of a value, static
 * or queue mode variable. This is provided to allow access to a single
 * component of a static variable of compound type which has nested structs
 * and/or arrays by means of a single integer index instead of field names
 * and arrays subscripts. It has two arguments. The first argument is a
 * static variable reference. The second argument is an integer index. If
 * the static variable type is primitive the index must be zero. The index
 * must be non-negative and less than the number of components in the type.
 */
public class ComponentPointerFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the offset of the primitive variable
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 2)
            throw new ExEx("componentpointer() must have two arguments", loc);

        Ref ref = args.getRef(0, "componentpointer() ");
        int index = (int)args.getVal(1).getSingleIval(loc);
        if (index < 0)
            throw new ExEx("componentpointer() second argument is negative", loc);
        if (index >= ref.numWords())
            throw new ExEx("componentpointer() second argument is too large", loc);
        int i = ref.getWordSpec().getWord(index);
        switch (ref.getMode()) {
        case VALUE:
            Value v = (Value)ref.getVar();
            return(new Val(v.getWordRef(i, loc), true, loc));
        case STATIC:
            Static s = (Static)ref.getVar();
            return(new Val(s.getWordRef(i, loc), true, loc));
        case QUEUE:
            Queue p = (Queue)ref.getVar();
            return(new Val(p.getWordRef(i, loc), true, loc));
        default:
            throw new ExEx("componentpointer() first argument " + ref.getMode().name() + " mode not allowed", loc);
        }


    }
}

