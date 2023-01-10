package threepl.funcs;

import threepl.codegen.TDEConstants;
import threepl.exceptions.ExEx;
import threepl.exec.QueueRefs;
import threepl.exec.Val;
import threepl.nodes.ExprNode;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt function to create an adder/subtracter.
 *
 * <p>The 1st input argument is the 1st operand.
 *
 * <p>The 2nd input argument is the 2nd operand.
 *
 * <p>The 3rd input argument is type log and selects addition when true.
 *
 * <p>The 4th optional input argument is type log and gates the 2nd operand.
 *
 * <p>The 5th optional input argument is type log and is XORed to the carry in.
 */
public class AddSubFunc extends InbuiltFunc implements Constant, TDEConstants {
    
    public AddSubFunc () {
        ipnames.put("in1", 0);
        ipnames.put("in2", 1);
        ipnames.put("add", 2);
        ipnames.put("gate", 3);
        ipnames.put("xci", 4);
    }

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the result
     */
    public Val getVal (NodeList args) {
        SrcLoc      loc = args.getCallLoc();

        if ((args.size() < 3) && (args.size() > 5))
            throw new ExEx("addsub() must have 3 to 5 input arguments", loc);
        
        Val     aval = args.getVal(0);

        if ((aval.getMode() == Mode.CMEMORY) || (aval.getMode() == Mode.RMEMORY) || (aval.getMode() == Mode.CLOCK))
            throw new ExEx("addsub() 1st input argument not allowed mode", loc);

        Val     bval = args.getVal(1);

        if ((bval.getMode() == Mode.CMEMORY) || (bval.getMode() == Mode.RMEMORY) || (bval.getMode() == Mode.CLOCK))
            throw new ExEx("addsub() 2nd input argument not allowed mode", loc);

        Val     addval = args.getVal(2);

        if ((addval.getMode() == Mode.CMEMORY) || (addval.getMode() == Mode.RMEMORY) || (addval.getMode() == Mode.CLOCK))
            throw new ExEx("addsub() 3rd input argument not allowed mode", loc);
        if (addval.getPrimType() != Ptype.LOG)
            throw new ExEx("addsub() 3rd input argument not log type", loc);
        
        /*
        if ((bval.getMode() == Mode.IMMEDIATE) && (bval.getSingleIval(loc) == 0))
            // add or subtract 0 - return 1st operand
            return(aval);
        */

        switch (aval.getPrimType()) {
        case INT:
        case UINT:
        case FIXED:
        case UFIXED:
            break;
        default:
            throw new ExEx("addsub() 1st input argument not int, uint, fixed or ufixed", loc);
        }
        switch (bval.getPrimType()) {
        case INT:
        case UINT:
        case FIXED:
        case UFIXED:
            break;
        default:
            throw new ExEx("addsub() 2nd input argument not int, uint, fixed or ufixed", loc);
        }

        if (aval.getMode() == Mode.IMMEDIATE)
            aval.toTarget();
        if (bval.getMode() == Mode.IMMEDIATE)
            bval.toTarget();
        aval.resolveClocks(null, Calloc.VALUE, loc);
        bval.resolveClocks(null, Calloc.VALUE, loc);
          
        Val     gateval = null;
        if (args.size() > 3) {
            gateval = args.getVal(3);
            if ((gateval.getMode() == Mode.CMEMORY) || (gateval.getMode() == Mode.RMEMORY) || (gateval.getMode() == Mode.CLOCK))
                throw new ExEx("addsub() 4th input argument not allowed mode", loc);
            if (gateval.getPrimType() != Ptype.LOG)
                throw new ExEx("addsub() 4th input argument not log type", loc);
        } else
            gateval = new Val(true, loc);
        
        Val     xcival = null;
        if (args.size() > 4) {
            xcival = args.getVal(4);
            if ((xcival.getMode() == Mode.CMEMORY) || (xcival.getMode() == Mode.RMEMORY) || (xcival.getMode() == Mode.CLOCK))
                throw new ExEx("addsub() 5th input argument not allowed mode", loc);
            if (xcival.getPrimType() != Ptype.LOG)
                throw new ExEx("addsub() 5th input argument not log type", loc);
        } else
            xcival = new Val(false, loc);
        
        Val retval = ExprNode.target_quin(aval, bval, addval, gateval, xcival, loc);
        QueueRefs    queues = new QueueRefs();
        queues.and_set(aval.getQueues(), loc);
        queues.and_set(bval.getQueues(), loc);
        queues.and_set(addval.getQueues(), loc);
        queues.and_set(gateval.getQueues(), loc);
        queues.and_set(xcival.getQueues(), loc);
        retval.andSetQueues(queues, loc);
        return(retval);
    }
}
