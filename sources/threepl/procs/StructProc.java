package threepl.procs;

import java.util.ArrayList;
import java.util.Iterator;

import threepl.ThreePL;
import threepl.exceptions.ExEx;
import threepl.exec.Field;
import threepl.exec.Immediate;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.nodes.CompoundValNode;
import threepl.nodes.Ident;
import threepl.nodes.Node;
import threepl.nodes.NodeList;
import threepl.nodes.VarNode;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to create a struct type.
 */
public class StructProc extends InbuiltProc implements Constant {

    /**
     * Construct the inbuilt procedure struct().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     * 'allowed_as_param' is false for this procedure as it is not
     * allowed to be called as a parameter declaration
     */
    public StructProc () {
        allowed_as_param = false;
        check_null_input_args = true;
        check_null_output_args = false;
        target_inline = false;
    }

    /**
     * Execute the procedure struct(). This does not generate executable code.
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
        if (outargs.size() != 0)
            throw new ExEx("struct() cannot have output arguments", loc);
        Iterator<Node>  it = inargs.iterator();
        Node            n;
        Node            n1;
        Node            n2;
        Ident           id;
        Type            struct = new Type();

        if (!it.hasNext())
            throw new ExEx("struct() has no arguments", loc);
        if ((inargs.size() % 2) != 1)
            // must have odd number of arguments
            // (struct name + pairs)
            throw new ExEx("struct() arguments not paired", loc);
        
        // struct name
        n = it.next();
        id = n.getIdentifier("struct() - 1st argument");
        
        // argument pairs
        while (it.hasNext()) {  // odd number, so guaranteed pairs here
            ArrayList<Ident>    l1 = null;
            String              s1 = null;
            boolean             extends1 = false;
            Val                 val2;
            String              s2 = null;
            Type                t2;
            
            // field identifier - s1
            // identifier list - l1
            // "extends" - extends1 true
            n1 = it.next();
            if (n1 == null)
                throw new ExEx("struct() - null argument", loc);
            if (n1 instanceof CompoundValNode) {
                l1 = ((CompoundValNode)n1).getList("struct()");
            } else if (n1 instanceof VarNode) {
                s1 = n1.getIdentifier("struct field name").getId();
                extends1 = s1.equals("extends");
            } else
                throw new ExEx("struct() - field name not identifier or \"extends\"", loc);
    
            // str Val or type Val
            n2 = it.next();
            if (n2 == null)
                throw new ExEx("struct() - null argument", loc);
            val2 = n2.getVal();
            if (val2.getMode() != Mode.IMMEDIATE)
                throw new ExEx("struct() - field type not immediate", loc);
            if (val2.getPrimType() == Ptype.STR) {
                // type string
                s2 = val2.getSingleSval(loc);
                t2 = new Type(s2, loc);
            } else if (val2.getPrimType() == Ptype.TYPE) {
                // type 'type'
                t2 = val2.getSingleTval(loc);
            } else
                throw new ExEx("struct() - field type not string or mode type", loc);

            if (t2.hasUndimArray())
                throw new ExEx("struct() - field type contains undimensioned array", loc);

            if (extends1) {
                ArrayList<Field>   al = t2.getFieldIndex();
                if (al == null)
                    throw new ExEx("struct() - \"extends\" type not a struct", loc);
                struct.addFields(al, loc);
            } else {
                if (l1 != null) {
                    for (Ident fid: l1) {
                        s1 = fid.getId();
                        if ((fid.getScopeContext() == Context.GLOBAL))
                            throw new ExEx("struct field name cannot have global '/'", loc);
                        struct.addField(s1, t2, loc);
                    }
                } else
                    struct.addField(s1, t2, loc);
            }
        }

        Val init = new Val(struct, loc);
        Immediate ivar = new Immediate(id, new Type(Ptype.TYPE, 0), init, false, false, loc);
        ThreePL.addVar(ivar, id.getScopeContext(), loc);
    }
}
