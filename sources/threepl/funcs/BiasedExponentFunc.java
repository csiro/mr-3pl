package threepl.funcs;

import static threepl.ThreePL.tdelist;

import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.exec.WordSpec;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the biased exponent of a value of primitive
 * type "float". Other types result in a fatal error. This has 1 argument.
 * which may be an array member or struct field. The variable mode must be
 * selectvalue, value, static or queue, other modes giving a fatal error.
 * The exponent is returned as a target value type "uint:n" where 'n' is
 * the number of bits in the biased exponent.
 */
public class BiasedExponentFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the offset of the primitive variable
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("biasedexponent() must have a single argument", loc);

        Val val = args.getVal(0);
        if (!val.isPrimitive())
            throw new ExEx("biasedexponent() argument not a primitive type", loc);
        if (val.getPrimType() != Ptype.FLOAT)
            throw new ExEx("biasedexponent() argument not type \"float\"", loc);        

        switch (val.getMode()) {
        case SELECTVALUE:
        case VALUE:
        case STATIC:
        case QUEUE:
            break;
        case IMMEDIATE:
            double  d = val.getSingleFval(loc);
            long    l = Double.doubleToLongBits(d);
            return(new Val((l >> 52) & 0x7ff, loc));
        default:
            throw new ExEx("biasedexponent() argument not allowed target mode", loc);        
        }

        Var         var = val.getVar(); // may be null?
        int         ewidth = val.getType().getExponentWidth();
        int         mwidth = val.getType().getMantissaWidth();
        TDEVar      itdev = val.getTDEVar();
        Type        tmp_type = new Type("(s, log, bexp, uint:" + ewidth + ", mant, uint:" + mwidth + ")", loc);
        WordSpec    tmp_ws = tmp_type.getWordSpec(null, loc);
        TDEVar      tmp_tdev = tdelist.signal("FPCONVIN", tmp_ws, loc);
        Type        otype = new Type("uint:" + ewidth, loc);
        WordSpec    ows = otype.getWordSpec(null, loc);
        TDEVar      otdev = tdelist.signal("BEXP", ows, loc);
        tdelist.connect(tmp_tdev, itdev);
        tdelist.connect(otdev, tmp_tdev.getWord(1, loc));
        
        return(new Val(var, Mode.VALUE, otdev, loc));
    }
}

