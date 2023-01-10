package threepl.funcs;

import static threepl.ThreePL.tdelist;

import threepl.codegen.TDE;
import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.exec.WordSpec;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the next value of an enumerated type value.
 * This has 1 argument which must be of enumerated type.
 * The type returned is the same as that of the argument.
 * If the argument is immediate fatal error occurs if there is no next value.
 * If the argument is mode value, selectvalue, static or queue a value
 * with an ordinal of 0 is returned if there is no next value.
 */
public class NextFunc extends InbuiltFunc implements Constant, TDEConstants {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the identifier string
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("next() must have 1 argument", loc);

        
        Val     val = args.getVal(0);
        if (val.getPrimType() != Ptype.ENUM)
            throw new ExEx("next() argument is not an enumerated type", loc);
        Mode    mode = val.getMode();
        Type    type = val.getType();
        Ptype   ptype = type.getPrimType();
        if (ptype != Ptype.ENUM)
            throw new ExEx("next() argument is not an enumerated type (" + ptype.typename() + ")", loc);
        switch (mode) {
        case IMMEDIATE:
            long ord = val.enumOrd(loc);
            return(new Val(type.nextEnumOrd(ord, loc), type, loc));
        case VALUE:
        case SELECTVALUE:
        case STATIC:
        case QUEUE:
            WordSpec    ws = val.getWordSpec();
            int         width = ws.numBits();
            int         size = 1 << width;
            long[]      init = new long[size];
            for (int i=0 ; i<size ; i++)
                init[i] = 0;
            long l1 = type.firstEnumOrd();
            long l2 = -1;
            for (;;) {
                if (l2 >= 0)
                    init[(int)l2] = l1;
                if (!type.hasNextEnumOrd(l1, loc))
                    break;
                l2 = l1;
                l1 = type.nextEnumOrd(l1, loc);
            }
            String[]    sa = new String[size];
            for (int i=0 ; i<size ; i++)
                sa[i] = Long.toHexString(init[i]);
            
            TDEVar      atdev = val.getTDEVar();
            TDEVar      otdev = tdelist.signal("EO", ws, loc);
            TDE         cram = new TDE(TDEType.CRAM, loc);
            cram.add2p(1);
            cram.add2p(width);
            cram.add2p(width);
            cram.add2p(sa);
            cram.add2i(null);
            cram.add2i(atdev);
            cram.add2i(null);
            cram.add2i(null);
            cram.add2o(otdev);
            tdelist.addTDE(cram);
            Val retval = new Val(null, Mode.VALUE, otdev, loc);
            retval.andSetQueues(val.getQueues(), loc);
            return(retval);
        default:
            throw new ExEx("next() argument mode " + mode.modename() + " not allowed", loc);
        }
    }
}
