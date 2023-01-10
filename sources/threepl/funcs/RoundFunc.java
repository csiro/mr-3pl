package threepl.funcs;

import static threepl.ThreePL.tdelist;

import threepl.codegen.TDE;
import threepl.codegen.TDEVar;
import threepl.codegen.TDEConstants.TDEOp;
import threepl.codegen.TDEConstants.TDEType;
import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.exec.WordSpec;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to round an immediate floating point
 * value to the nearest integer or a target fixed point value
 * to the nearest integer (with .5 values going to nearest even integer).
 */
public class RoundFunc extends InbuiltFunc implements Constant {
    SrcLoc  loc;

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the rounded value
     */
    public Val getVal (NodeList args) {
        loc = args.getCallLoc();
        
        if (args.size() != 1)
            throw new ExEx("round() - must have one argument", loc);

        Val val = args.getVal(0);
        switch (val.getMode()) {
        case IMMEDIATE:
            if (val.getPrimType() != Ptype.FLOAT)
                throw new ExEx("round() immediate argument not a float", loc);
            double  f = val.getSingleFval(loc);
            if (f < 0.0)
                return(new Val((long)(f - 0.5), loc));
            else
                return(new Val((long)(f + 0.5), loc));
        case SELECTVALUE:
        case VALUE:
        case STATIC:
        case QUEUE:
            switch (val.getPrimType()) {
            case UINT:
            case INT:
                return(val);
            case UFIXED:
            case FIXED:
                boolean     signed;
                WordSpec    ows;    // output WordSpec
                Ptype       opt;
                int         offset;
                int         iwidth;
                int         owidth;
                TDEVar      itdev;      // input
                TDEVar      iftdev;     // input fraction part
                TDEVar      iitdev;     // input integer part
                TDEVar      fractb;     // fraction top bit
                TDEVar      frest;      // fraction remaining bits
                TDEVar      fracz;      // fraction bottom bits are zero
                TDEVar      odd;        // input is odd
                TDEVar      incr;       // increment integer part
                TDEVar      otdev;      // output
                TDE         tde;
                if (val.getPrimType() == Ptype.FIXED) {
                    opt = Ptype.INT;
                    signed = true;
                } else {
                    opt = Ptype.UINT;
                    signed = false;
                }
                iwidth = val.getPrimWidth();
                offset = val.getPrimOffset();
                owidth = iwidth - offset;
                itdev = val.getTDEVar();
                ows = new WordSpec(owidth, opt);
                otdev = tdelist.signal("ROUND", ows, loc);
                
                // extract input fraction part
                iftdev = tdelist.signal("FRAC", offset, loc);
                tdelist.connect(iftdev, itdev, false);
                
                // extract input integer part
                iitdev = tdelist.signal("INT", owidth, loc);
                tdelist.lshift(iitdev, itdev, -offset, loc);
                
                // get top bit of fraction
                fractb = tdelist.signal("FRACTB", loc);
                tdelist.lshift(fractb, iftdev, 1-offset, loc);    // shifts right top bit of fraction
                
                // get rest of fraction
                frest = tdelist.signal("FREST", offset-1, loc);
                tdelist.connect(frest, iftdev, false);            // truncates top bit off fraction
                
                // check if rest of fraction bits are zero
                TDEVar  zero = new TDEVar((long)(0), Ptype.UINT, loc);
                fracz = tdelist.signal("FRACZ", loc);
                tde = new TDE(TDEType.OPERATOR);
                tde.add2p(TDEOp.EQ);
                tde.add2p(false);
                tde.add2p(false);
                tde.add2i(frest);
                tde.add2i(zero);
                tde.add2o(fracz);
                tdelist.addTDE(tde);
                
                odd = tdelist.signal("ODD", loc);
                tdelist.connect(odd, iitdev, false);
                
                // conditionally add one to integer part,
                // if fraction == half and input is odd, or fraction > half
                // then increment integer part
                // incr = odd & top_bit & zero | top_bit & !zero
                incr = or( and(odd, and(fractb, fracz)), and(inv(fracz), fractb));
                
                tde = new TDE(TDEType.OPERATOR);
                tde.add2p(TDEOp.ADD);
                tde.add2p(signed);
                tde.add2p(false);
                tde.add2i(iitdev);
                tde.add2i(incr);
                tde.add2o(otdev);
                tdelist.addTDE(tde);
                
                Val int_bits = new Val(null, Mode.VALUE, otdev, loc);
                return(int_bits);
            default:
                throw new ExEx("round() cannot accept target type " +
                                val.getPrimType().name(), loc);
            }
        default:
            throw new ExEx("round() cannot accept mode " +
                                val.getMode().name(), loc);
        }
    }
    
    private TDEVar  and (TDEVar in1, TDEVar in2) {
        return(tdelist.and(in1,  in2,  loc));
    }
    
    private TDEVar  or (TDEVar in1, TDEVar in2) {
        return(tdelist.or(in1,  in2,  loc));
    }
    
    private TDEVar  inv (TDEVar in) {
        return(tdelist.inv(in,  loc));
    }
}
