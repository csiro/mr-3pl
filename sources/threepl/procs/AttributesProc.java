package threepl.procs;

import java.util.ArrayList;
import java.util.TreeMap;

import threepl.codegen.TDEConstants;
import threepl.exceptions.ExEx;
import threepl.exec.Ref;
import threepl.exec.SubFieldList;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.exec.WordSpec;
import threepl.nodes.CompoundValNode;
import threepl.nodes.KeyMatchNode;
import threepl.nodes.Node;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * <p>An inbuilt procedure to set the attributes of a variable.
 *
 * <p>The 1st argument is an unsubscripted signal
 * identifier, or a compound value of unsubscripted signal
 * identifiers. These are the identifiers of the variables whose
 * attributes are to be set.
 *
 * <p>The 2nd argument can be -
 * <ul>
 *  <li> an attributes map
 *  <li> a one-dimensional compound constant containing key=value entries
 *  <li> a key=value argument
 * </ul>
 * In the first and second cases the argument contains the
 * attributes map. In the last case there may be any number of additional
 * arguments, each one being a keymatch argument representing an attribute.
 * The attributes map is assigned to the variable and any previous attribute map
 * is discarded.
 */
public class AttributesProc extends InbuiltProc implements Constant, TDEConstants {

    /**
     * Construct the inbuilt procedure attributes().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public AttributesProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
    }

    /**
     * Execute the procedure attributes(). This does not generate in-line
     * executable code.
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
        int     in = inargs.size();
        if (outargs.size() != 0)
            throw new ExEx("attributes() must have no output arguments", loc);

        ArrayList<Ref>  refs = null;
        Val             aval = null;

        Node n = inargs.getNode(0);
        if (n instanceof CompoundValNode)
            refs = ((CompoundValNode)n).getRefList("attributes()");
        else {
            refs = new ArrayList<Ref>();
            refs.add(n.getRef("attributes() - 1st argument"));
        }
        
        Node    na = inargs.getNode(1);
        if (na instanceof CompoundValNode) {
            if (inargs.size() > 2)
                throw new ExEx("attributes() cannot have input arguments after the map", loc);
            CompoundValNode cvn = (CompoundValNode)na;
            Type    mtype = new Type(Ptype.MAP, 0);
            aval = cvn.getVal(mtype);
        } else if (na instanceof KeyMatchNode) {
            TreeMap<String, Val>    attr = new TreeMap<String, Val>();
            Object[]                oa = new Object[1];
            Type[]                  ta = new Type[1];
            Type                    type = new Type(Ptype.MAP, 0);
            WordSpec                ws = type.getWordSpec(null, loc);
            
            oa[0] = attr;
            ta[0] = type;
            aval = new Val(oa, ta, ws, null, new SubFieldList(), null);
            for (int i=1 ; i<in ; i++) {
                na = inargs.getNode(i);
                if (!(na instanceof KeyMatchNode))
                    throw new ExEx("attributes() argument is not key=value", loc);
                String key = ((KeyMatchNode)na).getParamKey();
                na = na.getSubNode(0);
                attr.put(key, na.getVal());
            }
        } else {
            aval = inargs.getVal(1);
            if (aval == null)
                throw new ExEx("attributes() - no attributes arguments", loc);
            if (aval.getPrimType() != Ptype.MAP)
                throw new ExEx("attributes() - 2nd argument not type \"map\"", loc);
        }
        
        for (Ref ref : refs) {
            Var             var = ref.getVar();
            
            /* Allow partial variable reference.
            SubFieldList    sfl = ref.getSubFields();
            if ((sfl != null) && (sfl.size() != 0))
                throw new ExEx("attributes() - 1st argument has reference to partial variable", loc);
            */
            var.setAttributes(aval, loc);
        }
    }
}
