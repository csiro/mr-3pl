package threepl.funcs;

import static threepl.ThreePL.*;

import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.exec.WordSpec;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to truncate an immediate floating point
 * value to an integer or a target fixed point value
 * to an integer. Truncation is towards zero.
 */
public class TruncFunc extends InbuiltFunc implements Constant {


    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the rounded value
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        
        if (args.size() != 1)
            throw new ExEx("trunc() - must have one argument", loc);

        Val val = args.getVal(0);
        switch (val.getMode()) {
        case IMMEDIATE:
            if (val.getPrimType() != Ptype.FLOAT)
                throw new ExEx("trunc() immediate argument not a float", loc);
            double  f = val.getSingleFval(loc);
            return(new Val((long)(f), loc));
        case SELECTVALUE:
        case VALUE:
        case STATIC:
        case QUEUE:
            WordSpec    ows;
            Ptype       opt;
            int         offset;
            int         iwidth;
            int         owidth;
            TDEVar      itdev;
            TDEVar      otdev;
            switch (val.getPrimType()) {
            case UINT:
            case INT:
                return(val);
            case UFIXED:
            case FIXED:
                if (val.getPrimType() == Ptype.FIXED)
                    opt = Ptype.INT;
                else
                    opt = Ptype.UINT;
                
                iwidth = val.getPrimWidth();
                offset = val.getPrimOffset();
                owidth = iwidth - offset;
                itdev = val.getTDEVar();
                ows = new WordSpec(owidth, opt);
                otdev = tdelist.signal("TRUNC", ows, loc);
                tdelist.lshift(otdev, itdev, -offset, loc);
                Val int_bits = new Val(null, Mode.VALUE, otdev, loc);
                return(int_bits);
            default:
                throw new ExEx("trunc() cannot accept target type " +
                                val.getPrimType().name(), loc);
            }
        default:
            throw new ExEx("trunc() cannot accept mode " +
                                val.getMode().name(), loc);
        }
    }
}
