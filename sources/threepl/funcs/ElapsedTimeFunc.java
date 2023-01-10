package threepl.funcs;

import static threepl.ThreePL.nanotime0;
import static threepl.ThreePL.nanotime1;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the elapsed time. It has an optional single argument.
 * The elapsed time in second (float) is returned. The optional argument is
 * type "log" and if present and if true, the elapsed time is relative to the
 * start of execution of the 3PL compiler, otherwise it is relative to the
 * start of immediate execution.
 */
public class ElapsedTimeFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the directive list array of structs
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() > 1)
            throw new ExEx("elapsedtime() cannot have more than 1 argument", loc);

        long    it;
        if ((args.size() != 0) && args.getVal(0).getSingleLval(loc))
            it = System.nanoTime() - nanotime0;
        else
            it = System.nanoTime() - nanotime1;
        double  t = it / 1.0e9;
        return(new Val(t, loc));
    }
}
