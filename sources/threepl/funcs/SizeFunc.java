package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Ref;
import threepl.exec.RefOrVal;
import threepl.exec.SubFieldList;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.nodes.Node;
import threepl.nodes.NodeList;
import threepl.nodes.VarNode;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * An inbuilt function to get the total size in bits of a target
 * variable or expression. This has 1 argument, which is the
 * variable or expression.
 */
public class SizeFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the size
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
        Ref     ref = null;
        Val     val = null;
        Type    type;
        Mode    mode;

        if (args.size() != 1)
            throw new ExEx("size() must have a single argument", loc);
        
        Node    n = args.getNode(0);
        if (n instanceof VarNode) {
            Var     var = ((VarNode)n).getVar();
            if (var == null)
                throw new ExEx("size() argument undefined variable '" + ((VarNode)n).getId() + "'", loc);        
            Type    t = var.getType();
            if (t == null)
                throw new ExEx("size() argument undefined type '" + ((VarNode)n).getId() + "'", loc);        
            if (t.getPrimType() == Ptype.TYPE) {
                //WordSpec        ws = t.getWordSpec(null, loc);
                NodeList        nl = n.getListSubNode(0);
                SubFieldList    sfl = new SubFieldList(nl, loc);
                t = (Type)var.getVal(0);
                if (t == null)
                    throw new ExEx("size() argument undefined type '" + ((VarNode)n).getId() + "'", loc);        
                //if (!t.hasTargetType() && (t.getPrimType() != Ptype.ENUM) && (t.getPrimType() != Ptype.LOG))
                //    throw new ExEx("size() argument cannot be type \"type\" with immediate members", loc);        
                //if (!t.hasTargetType() && (t.getPrimType() != Ptype.LOG))
                if (t.hasImmediateType() || t.hasPointerType())
                    throw new ExEx("size() immediate argument of type 'type' must be a target type (" + t.getTypeString() + ")", loc);        
                return(new Val(t.getWordSpec(var, sfl, loc).numBits(), loc));
            }
            ref = args.getRef(0, "size()");
            type = ref.getWordSpec().getType();
            mode = ref.getMode();
        } else {
            val = args.getVal(0);
            type = val.getWordSpec().getType();
            mode = val.getMode();
        }
        
        if (type.getPrimType() == Ptype.NULL)
            throw new ExEx("size() - argument has null type", loc);        
        
        switch (mode) {
        case CMEMORY:
        case RMEMORY:
            throw new ExEx("size() argument cannot be a memory mode", loc);        
        case CLOCK:
            throw new ExEx("size() argument cannot be clock mode", loc);        
        case IMMEDIATE:
            if (val == null)
                val = ref.getVal(loc);
            if (type.getArrayDim() != 0)
                throw new ExEx("size() argument cannot be immediate array", loc);        
            if (type.getFieldIndex() != null)
                throw new ExEx("size() argument cannot be immediate struct", loc);        
            switch (type.getPrimType()) {
            case UINT:
            case INT:
                long    i = val.getSingleIval(loc);
                int     j = 0;
                if (i == 0)
                    return(new Val(1, loc));
                if (i == -1)
                    return(new Val(2, loc));
                if (i < 0) {
                    i = -1 - i;
                    j = 1;
                }
                while (i != 0) {
                    i >>= 1;
                    j++;
                }
                return(new Val(j, loc));
            case LOG:
                return(new Val(1, loc));
            case FLOAT:
                return(new Val(64, loc));
            case TYPE:
                Type    t = val.getSingleTval(loc);
                if (t.hasImmediateType() || t.hasPointerType())
                    throw new ExEx("size() immediate argument of type 'type' must be a target type (" + t.getTypeString() + ")", loc);        
                return(new Val(t.getWordSpec(null, loc).numBits(), loc));
            default:
                throw new ExEx("size() immediate argument cannot be type " + ref.getPrimType().typename(), loc);        
            }
        default:
            // An allowed target mode
            RefOrVal    rov = (ref != null) ? ref : val;
            return(new Val(rov.getWordSpec().numBits(), loc));
        }
    }
}
