package threepl.funcs;

import static threepl.ThreePL.tdelist;

import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.exec.WordSpec;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the sign bit of a value of primitive type
 * "float". Other types result in a fatal error. This has 1 argument.
 * which may be an array member or struct field. The variable mode must
 * be selectvalue, value, static or queue, other modes giving a fatal
 * error. The sign bit is returned as a target value type "log".
 */
public class FloatIsNegativeFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the offset of the primitive variable
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("floatisnegative() must have a single argument", loc);

        Val val = args.getVal(0);
        if (!val.isPrimitive())
            throw new ExEx("floatisnegative() argument not a primitive type", loc);
        if (val.getPrimType() != Ptype.FLOAT)
            throw new ExEx("floatisnegative() argument not type \"float\"", loc);        

        switch (val.getMode()) {
        case SELECTVALUE:
        case VALUE:
        case STATIC:
        case QUEUE:
            break;
        default:
            throw new ExEx("floatisnegative() argument not allowed mode", loc);        
        }

        Var         var = val.getVar(); // may be null?
        TDEVar      tdev = val.getTDEVar();
        WordSpec    lws = new WordSpec(Ptype.LOG);
        TDEVar      mtdev = tdelist.signal("FIN", lws, loc);
        tdelist.lshift(mtdev, tdev.getWord(0, loc), tdev.numBits(), loc);
        return(new Val(var, Mode.VALUE, mtdev, loc));
    }
}

