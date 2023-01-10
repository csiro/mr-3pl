package threepl.funcs;

import java.text.SimpleDateFormat;
import java.util.Date;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the current date and time as a string. It has 1 optional argument.
 * If the argument is supplied it must be a valid date format string.
 */
public class TimeStringFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the directive list array of structs
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        String  date;
        String  fmt = "yyyy-MM-dd HH:mm:ss Z";

        if (args.size() > 1)
            throw new ExEx("timestring() cannot have more than 1 argument", loc);

        long    timems = System.currentTimeMillis();
        if (args.size() != 0) {
            String s = args.getVal(0).getSingleSval(loc);
            if ((s == null) || (s.length() == 0))
                throw new ExEx("timestring() - null or \"\" date format");
            fmt = s;
        }
        try {
            date = new SimpleDateFormat(fmt).format(new Date(timems));
        } catch (IllegalArgumentException e) {
            throw new ExEx("timestring() - illegal date format");
        }
        
        return(new Val(date, loc));
    }
}
