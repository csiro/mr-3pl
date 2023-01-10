package threepl.procs;

import static threepl.ThreePL.tdelist;

import threepl.codegen.TDEConstants;
import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to add a property to the
 * the current created element. This has 3 input arguments.
 * The first input argument is a string giving the block identifier or is omitted. In the latter case
 * the most recently created element will be addressed.
 * The second input argument is the property name string.
 * The third input argument the property value as a string.
 * If the property name or property value are null then
 * the procedure simply returns.
 */
public class ElementPropertyProc extends InbuiltProc implements Constant, TDEConstants {

    /**
     * Construct the inbuilt procedure elementproperty().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public ElementPropertyProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
        ipnames.put("id", 0);
        ipnames.put("property", 1);
        ipnames.put("value", 2);
    }

    /**
     * Execute the procedure special(). This does not generate in-line executable
     * code.
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
        SrcLoc  loc = inargs.getCallLoc();
        Val     idval;
        Val     pnval;
        Val     pvval;
        String  bname = null;
        String  propertyName;
        String  propertyValue;
        
        if ((inargs.size() != 3))
            throw new ExEx("elementproperty() - must have 3 input arguments", loc);
        if (outargs.size() != 0)
            throw new ExEx("elementproperty() - must have no output arguments", loc);

        pnval = inargs.getVal(1);
        pvval = inargs.getVal(2);
        if ((pnval == null) || (pvval == null))    // Ignore call with null property name or property value.
            return;
       
        // block identifier
        if (inargs.getNode(0) != null) {
            idval = inargs.getVal(0);       
            if ((idval.getMode() != Mode.IMMEDIATE) || (idval.getPrimType() != Ptype.STR))
                throw new ExEx("elementproperty() - 1st input argument must be a string", loc);
            bname = idval.getSingleSval(loc);
        }
        
        // property name
        if ((pnval.getMode() != Mode.IMMEDIATE) || (pnval.getPrimType() != Ptype.STR))
            throw new ExEx("elementproperty() - 2nd input argument, property name, must be a string", loc);
        propertyName = pnval.getSingleSval(loc);
        
        // property value
        if ((pvval.getMode() != Mode.IMMEDIATE) || (pvval.getPrimType() != Ptype.STR))
            throw new ExEx("elementproperty() - 3rd input argument, property value, must be a string", loc);
        propertyValue = pvval.getSingleSval(loc);
        
        tdelist.addProperty(bname, propertyName, propertyValue);
    }
}
