package threepl.procs;

import static threepl.ThreePL.getFamily;

import java.util.ArrayList;
import java.util.ListIterator;

import threepl.ThreePL;
import threepl.codegen.TDEConstants;
import threepl.exceptions.ExEx;
import threepl.exec.Memory;
import threepl.exec.RefOrVal;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.exec.WordSpec;
import threepl.nodes.CompoundValNode;
import threepl.nodes.Ident;
import threepl.nodes.KeyMatchNode;
import threepl.nodes.Node;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * Common code for CmemoryProc() and RmemoryProc().
 */
public class MemoryCommon implements Constant, TDEConstants {

    public static ArrayList<Var> process (
        NodeList            inargs,
        NodeList            outargs,
        ArrayList<String>   names,
        RefOrVal            par_arg,
        int[]               dim_des,
        boolean             is_input,
        boolean             is_output,
        boolean             is_rmemory
    ) {
        String      name = is_rmemory ? "rmemory" : "cmemory";
        SrcLoc      loc = inargs.getCallLoc();
        boolean     is_param = is_input || is_output;
        
        if (outargs.size() != 0)
            throw new ExEx(name + "() must have no output arguments", loc);
        if (dim_des != null)
            throw new ExEx(name + "() as a parameter cannot have a compound constant argument", loc);
        
        ListIterator<Node>  nit = inargs.listIterator();
        Node                n;
        ArrayList<Ident>    idents = InbuiltProc.resolveIdents(inargs, names, name + "()");
        ArrayList<Var>      vars = new ArrayList<Var>();
        int                 ports = -1;
        Val                 dtval = null;
        Type                dtype = null;
        Val                 atval = null;
        Type                atype = null;
        Val                 init = null;
        Val                 aval = null;
        Memory              mvar;
        
        // variable name argument
        if (!nit.hasNext())
            throw new ExEx(name + "() has no arguments");
        nit.next(); // skip 1st argument which has already been processed
        
        if (!is_param) {
            // number of ports
            if (!nit.hasNext())
                throw new ExEx(name + "() - ports argument missing", loc);
            n = nit.next();
            Val pval = n.getVal();
            ports = (int)pval.getSingleIval(loc);
            int np = getFamily().rramPorts(loc);
            if ((ports < 1) || (ports > np))
                throw new ExEx(name + "() - must be 1 to " + np + " ports", loc);
                
            // address type
            if (!nit.hasNext())
                throw new ExEx(name + "() - address type argument missing", loc);
            n = nit.next();
            atval = n.getVal();
            atype = atval.getDeclType(null, Mode.IMMEDIATE, false, null, false, name + "()", loc);
        
            // data type
            if (!nit.hasNext())
                throw new ExEx(name + "() - data type argument missing", loc);
            n = nit.next();
            dtval = n.getVal();
            dtype = dtval.getDeclType(null, Mode.IMMEDIATE, false, null, false, name + "()", loc);

            int ln = nit.nextIndex();

            // optional initialisation
            if (nit.hasNext()) {
                Node    ni = nit.next();
                if (!(n instanceof KeyMatchNode)) {
                    if (ni instanceof CompoundValNode) {
                        int     depth = 1 << atype.numBits();
                        Type    type = new Type(dtype, depth);
                        init = ((CompoundValNode)ni).getVal(type);
                    } else
                        init = ni.getVal();
                    ln++;
                }
            }
            
            if (is_input && (inargs.size() > 2))
                throw new ExEx(name + "() - as a parameter, cannot have attribute arguments", loc);
        
            // optional attribute arguments
            aval = InbuiltProc.attributeArguments(name, inargs, ln, loc);
        }

        for (Ident id : idents) {
            if ((is_input || is_output) && (par_arg != null)) {
                // is a module, procedure or function parameter
                Var argvar = par_arg.getVar();
                if (is_rmemory) {
                    if (argvar.getMode() != Mode.RMEMORY)
                        throw new ExEx("argument to " + name + "() parameter is not rmemory mode", loc);
                } else {
                    if (argvar.getMode() != Mode.CMEMORY)
                        throw new ExEx("argument to " + name + "() parameter is not cmemory mode", loc);
                }
                Memory argmvar = (Memory)argvar;
                if ((ports >= 0) && (ports != argmvar.getMemPorts()))
                    throw new ExEx("argument to " + name + "() parameter has different number of ports", loc);
                if (atype != null) {
                    WordSpec    arg_addr_ws = argmvar.getMemAddrWordSpec();
                    WordSpec    par_addr_ws = atype.getWordSpec(null, loc);
                    par_addr_ws.checkMatch(arg_addr_ws, false, "parameter " + name + "() address", loc);
                }
                if (dtype != null) {
                    WordSpec    arg_data_ws = argmvar.getMemDataWordSpec();
                    WordSpec    par_data_ws = dtype.getWordSpec(null, loc);
                    par_data_ws.checkMatch(arg_data_ws, false, "parameter " + name + "() data", loc);
                }
                mvar = new Memory(id, is_rmemory, 0, null, null, is_input, is_output, null, loc);
            } else
                mvar = new Memory(id, is_rmemory, ports, dtype, atype, is_input, is_output, init, loc);

            ThreePL.addVar(mvar, id.getScopeContext(), loc);
            if (aval != null)
                mvar.setAttributes(aval, loc);
            vars.add(mvar);
        }
        
        return(vars);
    }
}
