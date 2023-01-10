package threepl.funcs;

import java.util.ArrayList;

import threepl.exceptions.ExEx;
import threepl.exec.Field;
import threepl.exec.SubFieldList;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the nth field from a struct
 * This has 2 arguments. The first is an expression which has a struct type.
 * The second is an integer index of the required field.
 * The value returned is the field from the first argument variable indexed by the
 * second argument.
 */
public class GetFieldFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the identifier string
     */
    public Val getVal (NodeList args) {
        SrcLoc              loc = args.getCallLoc();
    
        if (args.size() != 2)
            throw new ExEx("getfield() must have 2 arguments", loc);

        Val     arg2 = args.getVal(1);
        int     fi = (int)arg2.getSingleIval(loc);
     
        Val     val = args.getVal(0);
        Type    type = val.getType();
        Var     var = val.getVar();
        if (var == null)
            throw new ExEx("getfield() 1st argument is not a variable", loc);
        
        /*
        if (ptype == Ptype.TYPE) {
            val = val.getVal(0);
            type = val.getSingleTval(loc);
        }
        */
        
        ArrayList<Field>    fields = type.getFieldIndex();
        if (fields == null)
            throw new ExEx("getfield() 1st argument is not a struct", loc);

        if (fi < 0)
            throw new ExEx("getfield() 2nd argument is negative", loc);
        if (fi >= fields.size())
            throw new ExEx("getfield() 2nd argument is larger than size of struct", loc);

        Field           f = fields.get(fi);
        String          key = f.getName();
        SubFieldList    sfl = new SubFieldList(key, loc);
        return(var.getVal(sfl, Flag.NONE, loc));
    }
}
