package threepl.funcs;

import java.util.ArrayList;

import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Ref;
import threepl.exec.SubFieldList;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.exec.WordSpec;
import threepl.nodes.Node;
import threepl.nodes.NodeList;
import threepl.nodes.VarNode;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the maximum numeric value of a target
 * variable. This has 1 argument, which is the variable.
 */
public class MaxValueFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the size
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("maxvalue() must have a single argument", loc);
        
        Var     var = null;
        Mode    mode = null;
        Type    type;
        Ptype   ptype;
        int     size;
        
        Node    n = args.getNode(0);
        if (n instanceof VarNode) {
            var = ((VarNode)n).getVar();
            if (var == null)
                throw new ExEx("minvalue() argument undefined variable '" + ((VarNode)n).getId() + "'", loc);
            type = var.getType();
            ptype = type.getPrimType();
            mode = var.getMode();
            switch (mode) {
            case CMEMORY:
            case RMEMORY:
                throw new ExEx("minvalue() - argument cannot be a memory mode", loc);
            case CLOCK:
                throw new ExEx("minvalue() - argument cannot be clock mode", loc);
            case IMMEDIATE:
                if ((ptype != Ptype.TYPE) && (ptype != Ptype.STR))
                    throw new ExEx("minvalue() - immediate argument cannot be type " + ptype.typename(), loc);
                break;
            default:
                // An allowed target mode
            }
            if (ptype == Ptype.TYPE) {
                NodeList        nl = n.getListSubNode(0);
                SubFieldList    sfl = new SubFieldList(nl, loc);
                type = (Type)var.getVal(0);
                WordSpec    ws = type.getWordSpec(var, sfl, loc);
                type = ws.getType();
                size = ws.numBits();
            } else if (ptype == Ptype.STR) {
                String  s = args.getVal(0).getSingleSval(loc);
                type = new Type(s, loc);
                size = type.numBits();
            } else {
                Ref     ref = args.getRef(0, "minvalue()");
                type = ref.getWordSpec().getType();
                size = ref.getWordSpec().numBits();
            }
        } else {
            Val val = args.getVal(0);
            if (val.getMode() != Mode.IMMEDIATE)
                throw new ExEx("minval() argument not a variable or a string literal", loc);
            ptype = val.getType().getPrimType();
            if (ptype == Ptype.STR) {
                String  s = args.getVal(0).getSingleSval(loc);
                type = new Type(s, loc);
                size = type.numBits();
            } else
                throw new ExEx("minval() argument not a variable or a string literal", loc);
        }

        ptype = type.getPrimType();

        if (size > 63)
            throw new ExEx("maxvalue() - argument size greater than 63 bits", loc);
        
        long    lval;
        Val     vval;
        TDEVar  tdev = null;
        
        switch (ptype) {
        case UINT:
        case UFIXED:
            lval = (1 << size) - 1;
            tdev = new TDEVar((long)0, type, loc);
            break;
        case INT:
        case FIXED:
        case FLOAT:
            lval = (1 << (size-1)) - 1;
            tdev = new TDEVar((long)0, type, loc);
            break;
        case ENUM:
            ArrayList<Long> ords = type.getEnumOrds();
            long    v = Long.MIN_VALUE;
            long    vv;
            for (int i=0 ; i<ords.size() ; i++) {
                vv = ords.get(i);
                if (vv > v)
                    v = vv;
            }
            lval = v;
            tdev = new TDEVar((long)0, type, loc);
            break;
        case NONE:
            throw new ExEx("maxvalue() argument is not a primitive type ", loc);
        default:
            throw new ExEx("maxvalue() argument cannot be type " + ptype.typename(), loc);
        }
        tdev.setVal(lval);
        vval = new Val(null, Mode.VALUE, tdev, loc);
        return(vval);
    }
}
