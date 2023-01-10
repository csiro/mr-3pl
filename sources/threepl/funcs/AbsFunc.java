package threepl.funcs;

import threepl.exceptions.ExEx;

import static threepl.ThreePL.tdelist;

import threepl.codegen.*;
import threepl.codegen.TDEConstants.TDEOp;
import threepl.codegen.TDEConstants.TDEType;
import threepl.exec.*;
import threepl.nodes.*;
import threepl.parser.*;

/**
 * An inbuilt function to get the absolute value of its argument.
 */
public class AbsFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the absolute value
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("abs() - must have one argument", loc);
        
        Val val = args.getVal(0);
        switch (val.getMode()) {
        case IMMEDIATE:
            switch (val.getPrimType()) {
            case INT:
            case FIXED:
                return(new Val(Math.abs(val.getSingleIval(loc)), loc));
            case FLOAT:
                return(new Val(Math.abs(val.getSingleFval(loc)), loc));
            default:
                throw new ExEx("abs() argument not an int, fixed or float", loc);
            }
        case VALUE:
        case STATIC:
        case QUEUE:
        case SELECTVALUE:
            Val         retval = null;
            QueueRefs   queues = new QueueRefs();
            TDEVar      tdev = val.getWordTDEVar(val.getWordSpec(), 0, loc);
            val.resolveClocks(null, Calloc.VALUE, loc);
            switch (val.getPrimType()) {
            case INT:
            case FIXED:
                Val         zval = new Val(0, loc);
                Val         gateval = new Val(true, loc);
                Val         xcival = new Val(false, loc);
                WordSpec    lws = new WordSpec(Ptype.LOG);
                TDEVar      tdevs = tdelist.signal("SIGN", lws, loc);
                TDEVar      _tdevs = tdelist.inv(tdevs, loc);
                Val         addval = new Val(_tdevs, loc);
                
                tdelist.lshift(tdevs, tdev, 1-tdev.numBits(), loc);
                retval = ExprNode.target_quin(zval, val, addval, gateval, xcival, loc);
                break;
            case FLOAT:
                WordSpec    ws = val.getWordSpec();
                int     bits = tdev.getWidth(0);
                long    mask = (1 << (bits-1)) - 1;
                TDEVar  tdevr = tdelist.signal("ABS", ws, loc);
                TDEVar  tdevm = new TDEVar(mask, Ptype.BITS, loc);
                TDEVar  tdevb = tdelist.signal("BITS", bits, loc);
                tdelist.connect(tdevb, tdev);
                
                TDE     and = new TDE(TDEType.OPERATOR);
                and.add2p(TDEOp.AND);
                and.add2p(true);
                and.add2p(false);
                and.add2i(tdevb);
                and.add2i(tdevm);
                and.add2o(tdevr);
                tdelist.addTDE(and);
                
                tdevr.setWordSpec(ws);
                retval = new Val(tdevr, loc);
                break;
            default:
                throw new ExEx("abs() input argument not int, fixed or float", loc);
            }
            queues.and_set(val.getQueues(), loc);
            retval.andSetQueues(queues, loc);
            return(retval);
        default:
            throw new ExEx("abs() argument not allowed mode", loc);
        }
    }
}
