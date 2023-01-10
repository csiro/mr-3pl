package threepl.funcs;

import threepl.ThreePL;
import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.Ident;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get a directive from the directive map.
 * This has one input argument and no output arguments.
 * The input argument is a string which is the directive key.
 */
public class DirectiveFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the directive value
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        if (args.size() != 1)
            throw new ExEx("directive() must have single argument", loc);
       
        Ident   ident = new Ident(args.getVal(0).getSingleSval(loc));
        switch (ident.getScopeContext()) {
        case DEFAULT:
        case GLOBAL:    // DEPRECATED - there is only global scope now!
            break;
        default:
            throw new ExEx("directive() - directive identifier cannot have a scope context", loc);
        }
        String  key = ident.getId();        
        
        Val val = ThreePL.findDir(key);
        if (val == null)
            throw new ExEx("directive() - no entry for key \"" + key + "\"", loc);
        return(val);
    }
}
