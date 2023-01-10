package threepl.funcs;

import threepl.ThreePL;
import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.Ident;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to determine if a directive exists in a directive map.
 * This has one input argument and no output arguments.
 * The input argument is a string which is the directive key.
 */
public class DirExistsFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  true if the directive exists
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        if (args.size() != 1)
            throw new ExEx("direxists() must have single argument", loc);
       
        Ident   ident = new Ident(args.getVal(0).getSingleSval(loc));
        if (ident.getScopeContext() != Context.DEFAULT)
            throw new ExEx("direxists() - identifier cannot have a scope context");
        String  key = ident.getId();        
        
        return(new Val(ThreePL.findDir(key) != null, loc));
    }
}
