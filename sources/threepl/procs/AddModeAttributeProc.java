package threepl.procs;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to add an attribute to the mode attributes map.
 * It has 4 arguments -
 *  STR mode, one of "clock", "input" or "output"
 *  STR key
 *  LOG is an array
 *  STR primitive type, one of "log", "uint", "float" or "str"
 */
public class AddModeAttributeProc extends InbuiltProc implements Constant {

    /**
     * Construct the inbuilt procedure addmodeattribute().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public AddModeAttributeProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
        ipnames.put("mode", 0);
        ipnames.put("attribute", 1);
        ipnames.put("isarray", 2);
        ipnames.put("type", 3);
    }

    /**
     * Execute the procedure addmodeattribute(). This does not generate executable code.
     * @param   inargs is a list of input argument tree nodes
     * @param   outargs is a list of output argument tree nodes
     * @param   toplevel is true if we are at the module, procedure or function
     *          level
     */
    public void execute (
        NodeList    inargs, 
        NodeList    outargs, 
        boolean     toplevel
    ) {
        SrcLoc      loc = inargs.getCallLoc();
        if (inargs.size() != 3)
            throw new ExEx("addmodeattribute() must have three input arguments", loc);
        if (outargs.size() != 0)
            throw new ExEx("addmodeattribute() cannot have output arguments", loc);

        Val v = null;
        String  modestring;
        String  key;
        String  ptypestring;
        Mode    mode;
        Ptype   ptype;
        
        v = inargs.getVal(0);
        if (v.isTarget())
            throw new ExEx("addmodeattribute() 1st argument must be an immediate variable or expression", loc);
        if (v.getPrimType() != Ptype.STR)
            throw new ExEx("addmodeattribute() 1st argument not a string", loc);
        modestring = v.getSingleSval(loc);
        
        if (modestring.equalsIgnoreCase("clock"))
            mode = Mode.CLOCK;
        else if (modestring.equalsIgnoreCase("input"))
            mode = Mode.INPUT;
        else if (modestring.equalsIgnoreCase("output"))
            mode = Mode.OUTPUT;
        else
            throw new ExEx("addmodeattribute() 1st argument not \"clock\", \"input\" or \"output\"", loc);
        
        v = inargs.getVal(1);
        if (v.isTarget())
            throw new ExEx("addmodeattribute() 2nd argument must be an immediate variable or expression", loc);
        if (v.getPrimType() != Ptype.STR)
            throw new ExEx("addmodeattribute() 2nd argument not a string", loc);
        key = v.getSingleSval(loc);
        
        v = inargs.getVal(2);
        if (v.isTarget())
            throw new ExEx("addmodeattribute() 3rd argument must be an immediate variable or expression", loc);
        if (v.getPrimType() != Ptype.STR)
            throw new ExEx("addmodeattribute() 3rd argument not a string", loc);
        ptypestring = v.getSingleSval(loc);

        if (ptypestring.equalsIgnoreCase("uint"))
            ptype = Ptype.UINT;
        else if (ptypestring.equalsIgnoreCase("log"))
            ptype = Ptype.LOG;
        else if (ptypestring.equalsIgnoreCase("float"))
            ptype = Ptype.FLOAT;
        else if (ptypestring.equalsIgnoreCase("str"))
            ptype = Ptype.STR;
        else
            throw new ExEx("addmodeattribute() 4th argument not \"log\", \"uint\", \"float\" or \"str\"", loc);
        
        Var.addModeAttribute(mode, key, ptype);

    }
}
