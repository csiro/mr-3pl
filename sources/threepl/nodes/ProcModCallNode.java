package threepl.nodes;

import static threepl.ThreePL.isparsing;
import static threepl.ThreePL.popClassBody;
import static threepl.ThreePL.pushCallName;
import static threepl.ThreePL.popCallName;
import static threepl.ThreePL.pushClassBody;

import java.util.ArrayList;

import threepl.ThreePL;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.Group;
import threepl.exec.Module;
import threepl.exec.QueueRefs;
import threepl.exec.Scope;
import threepl.exec.Val;
import threepl.exec.Procedure;
import threepl.mods.InbuiltMod;
import threepl.mods.InbuiltMods;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;
import threepl.parser.Token;
import threepl.procs.InbuiltProc;
import threepl.procs.InbuiltProcs;

/**
 * This node class implements a module or procedure call.
 * The module or procedure identifier is looked up in four global
 * maps (inbuilt module, user module, inbuilt procedure, user
 * procedure) since they all have the same call syntax. An
 * inline module "call" is also handled here. In that case the
 * module is in field 'inline'.
 */
public final class ProcModCallNode extends Node implements Constant {
    protected Node          name_node;
    protected String        cname = null;       // identifier if class module or procedure
    protected Module        inline = null;
    protected InbuiltProc   pi = null;
    protected Procedure     pu = null;
    protected InbuiltMod    mi = null;
    protected Module        mu = null;
    protected Group         gu = null;
    protected String        name;
    protected boolean       resolved = false;
    
    /**
     * Construct a module or procedure call code tree node.
     * @param   'n' is the module/procedure identifier node.
     *          If it is a VarNode and has SubFields it is a class module or procedure call,
     *          the last SubField is the module/procedure identifier and the
     *          remainder of the SubFieldList appended to the variable supplies
     *          the class (Scope).
     *          Otherwise the VarNode simply supplies the module/procedure identifier
     *          'n' also provides the source file location of the definition
     */
    public ProcModCallNode (Node n) {
        super(n.getSrcLoc());
        name_node = n;
        
        if (n instanceof VarNode) {
            ListNode    ln = (ListNode)n.getSubNode(0);
            NodeList    nl = ln.getNodeList();
            int         size = nl.size();
            if (size != 0) {
                // If there is a subscript/field list then this must be a class module or procedure call.
                // The VarNode gives the first Group (3PL Class) identifier.
                // The fields, except the last, give intermediate Groups (3PL Classes) and the
                // last field is the class module/procedure name. Remove it from the list.
                Node        last = nl.get(size-1);
                if (!(last instanceof FieldNode))
                    throw new ExEx("", loc);
                StrNode strn = (StrNode)((FieldNode)last).getSubNode(0);
                cname = strn.string;
                nl.remove(size-1);
            }
        }
        if (n.getFlag() != Flag.NONE)
            throw new ExEx(" ++ or -- used on procedure call", loc);
    }
     
    /**
     * Construct an inline module call code tree node.
     * @param   m is the inline module
     * @param   t is a token which provides the source file location of
     *          the call
     */
    public ProcModCallNode (Module m, Token t) {
        super(t);
        name_node = null;
        inline = m;
    }
   
    /**
     * Execute a module or a procedure call.
     * Procedures may be inbuilt or user-defined. The called identifier
     * is firstly searched for in the inbuilt procedure map. If not
     * found then the user-defined procedure map is searched. If that fails
     * the user-defined module map is searched.
     * @param   esig is an exception/restart signal, or null
     * @param   startl is a list of start signals for target statements
     *          below this node
     * @param   finishl is a list of finish signals for target statements
     *          below this node
     * @param   queues returns all the queue availability signals
     *          accumulated from code below
     * @param   toplevel is true if this is the top level in a module
     * @param   availok is true if a previous sync makes a queue availability
     *          wait unnecessary
     * @param   checkedqueues gives queue reads which have already been checked
     * @param   pri_in is an optional input signal to a priority encoder
     * @param   pri_out is an optional output signal from a priority encoder
     * @return  EXECR.NONE execution status value
     */
    public EXECR execute (
        TDEVar              esig,
        ArrayList<TDEVar>   startl, 
        ArrayList<TDEVar>   finishl,
        QueueRefs           queues,
        boolean             toplevel,
        boolean             availok,
        QueueRefs           checkedqueues,
        TDEVar              pri_in,
        TDEVar              pri_out
    ) {
        // Is it an inline module?
        if (inline != null) {
            inline.execute(null, null, loc);
            return(EXECR.NONE);
        }
        
        NodeList    inl = new NodeList(loc);    // default empty in and out node lists
        NodeList    onl = inl;
        NodeList    nl;

        resolve(false);
        
        ListNode    iln = (ListNode)subnodes.get(0);
        ListNode    oln = (ListNode)subnodes.get(1);
        if ((nl = iln.getNodeList()) != null)
            inl = nl;
        if ((nl = oln.getNodeList()) != null)
            onl = nl;
        
        //
        // Is it an inbuilt procedure?
        //
        if (pi != null) {
            pushCallName(name);
            // Sort the nodes in an inbuilt procedure input and output NodeLists when keymatch
            // arguments may be present.
            inl.sortKeys("procedure", pi.ipnames, pi.allowed_attributes, name, pi.check_null_input_args, true);
            onl.sortKeys("procedure", pi.opnames, pi.allowed_attributes, name, pi.check_null_output_args, false);
            if (pi.allowed_as_param) {
                // special call signature for var(), int(), log(), str(),
                // value(), static(), queue(), clock() etc.
                //if (name.equals("clock") && !toplevel)
                //    throw new ExEx("cannot call clock() within target code", loc);
                pi.execute(inl, onl, null, null, null, false, false);
            } else if (pi.target_inline)
                pi.execute(inl, onl, esig, startl, finishl, queues, availok, pri_in, pri_out);
            else
                pi.execute(inl, onl, toplevel);
            popCallName();
            return(EXECR.NONE);
        }
        
        //
        // Is it a user-defined procedure?
        //
        if (pu != null) {
            if (isparsing)
                throw new ExEx("cannot call user procedure during parsing", loc);
            pushCallName(null);
            pu.execute(inl, onl, esig, startl, finishl, queues, toplevel, availok, checkedqueues, pri_in, pri_out);
            if (cname != null)
                popClassBody();    // is a class function call
            popCallName();
            return(EXECR.NONE);
        }
        
        //
        // Is it an inbuilt module?
        //
        if (mi != null) {
            // Sort the nodes in an inbuilt module input and output NodeLists when keymatch
            // arguments may be present.
            inl.sortKeys("module", mi.ipnames, mi.allowed_attributes, name, false, true);
            onl.sortKeys("module", mi.opnames, mi.allowed_attributes, name, false, false);
            pushCallName(name);
            mi.execute(inl, onl);
            popCallName();
            return(EXECR.NONE);
        }
        
        //
        // is it a user-defined module?
        //
        if (mu != null) {
            if (isparsing)
                throw new ExEx("cannot call user module during parsing", loc);
            pushCallName(name);
            mu.execute(inl, onl, loc);
            if (cname != null)
                popClassBody();    // is a class function call
            popCallName();
            return(EXECR.NONE);
        }
        
        //
        // Is it a Group (3PL class)?
        //
        if (gu != null) {
            pushCallName(name);
            gu.getVal(onl, inl);
            popClassBody();    // is a class function call
            popCallName();
            return(EXECR.NONE);
        }

        throw new ExEx("SYSTEM ERROR - ProcModCallNode.execute()", loc);
    }
    
    /**
     * Get an inbuilt procedure which is allowed in a parameter
     * section.
     * @param   mess is a header string for error messages
     * @param   loc is the source file location
     * @return  the inbuilt procedure
     */
    public InbuiltProc getParamProc (String mess, SrcLoc loc) {
        resolve(true);
        if (pi == null) {
            // procedure input parameter is not a var(...), int(...),
            // log(...), str(...) etc call
            if ((pu != null) || (mi != null) || (mu != null))
                throw new ExEx(mess + "'" + name + "' not an inbuilt parameter procedure", loc);
            throw new ExEx(mess + " parameter procedure not found", loc);
        }
        if (!pi.allowed_as_param)
            throw new ExEx(mess + " procedure '" + name + "'not allowed as parameter", loc);
        return(pi);
    }
    
    /**
     * Get the inline module field.
     * @return  the inline module
     */
    public Module getInlineModule() { return(inline); }

    /**
     * Resolve the inbuilt/user module/procedure and its
     * identifier.
     * @param paramproc is true if the call is to an inbuilt procedure which
     * is a parameter in another module or procedure call.
     */
    private void resolve (boolean paramproc) {
        if (resolved)
            return;
        
        if (cname != null) {
            // Is a class module or procedure call.
            VarNode     cvn = (VarNode)name_node;
            if (cvn.getFlag() != Flag.NONE)
                throw new ExEx("cannot use ++ or -- on a module/procedure call", loc);
            Val         v = cvn.getVal();
            Scope       class_scope = v.getSingleScopeval(loc);
            
            if ((!paramproc && ((mu = class_scope.findMod(cname)) != null)) ||   // Try class module
                (!paramproc && ((pu = class_scope.findProc(cname)) != null))) {  // or class procedure
                pushClassBody(class_scope.getBody());
                resolved = true;
                return;
            }
            throw new ExEx("no class, module or procedure '" + cname + "'", loc);
        }
        
        if (name_node instanceof PtrValNode) {
            Object  o = ((PtrValNode)name_node).getModProcPtrObject(loc);
            if (o == null)
                throw new ExEx("null pointer reference", loc);
            if (o instanceof InbuiltMod) {          // Try inbuilt module
                mi = (InbuiltMod)o;
                name = InbuiltMods.findModId(mi);   // then try user-defined module
            } else if (o instanceof Module) {
                mu = (Module)o;
                name = mu.getName();
            } else if (o instanceof InbuiltProc) {  // then try inbuilt procedure
                pi = (InbuiltProc)o;
                name = InbuiltProcs.findProcId(pi);
            } else if (o instanceof Procedure) {    // then try user-defined procedure
                pu = (Procedure)o;
                name = pu.getName();
            } else                                  // otherwise is unknown module/procedure
                throw new ExEx("ProcModCallNode: SYSTEM ERROR", loc);
        } else {
            // name_node must be class VarNode
            VarNode     vn = (VarNode)name_node;
            if (vn.getFlag() != Flag.NONE)
                throw new ExEx("cannot use ++ or -- on a module or procedure call", loc);
            /*NodeList    nl = vn.getListSubNode(0);
            if ((nl != null) && (!nl.isEmpty())) {
                // This may be a call to a class module or procedure.
                // If so VarNode vn will have a SubFieldList whose last item is a field
                // containing the module or procedure identifier. To test this remove the
                // last SubField and call gatVal(). If it returns type "class" then that
                // is the scope of the class module or procedure. Use the removed identifier
                // to resolve the module or procedure. If the getVal() does not return type
                // "class" then continue the search.
                //SubFieldList    sfl = nl.getSubNode(0);
                //name = sf.getField();
                Val vnval = vn.getVal();
                if (vnval.getPrimType() == Ptype.CLASS)
                    scope = vnval.getSingleScopeval(loc);
            }*/
            name = vn.getId();
            Context context = vn.getScopeContext();
            if (context != Context.DEFAULT)
                throw new ExEx("module or procedure call '" + name + "' has illegal context " + context.contextname(), loc);

            //
            // Try inbuilt modules.
            //
            if ((mi = ThreePL.findInbuiltMod(name)) != null) {
                resolved = true;
                return;
            }
            
            //
            // Try user-defined modules.
            //
            if (!paramproc && ((mu = ThreePL.findMod(name)) != null)) {
                if ((mu.getEnclosing() instanceof Group) && (ThreePL.getClassScope() == null))
                    throw new ExEx("class module called with no class instance", loc);
                resolved = true;
                return;
            }
            
            //
            // Try inbuilt procedures.
            //
            if ((pi = ThreePL.findInbuiltProc(name)) != null) {
                resolved = true;
                return;
            }
            //
            // Try user-defined procedures.
            //
            if (!paramproc && ((pu = ThreePL.findProc(name)) != null)) {
                resolved = true;
                return;
            }
            
            //
            // Try defined groups (3PL classes).
            //
            if ((gu = ThreePL.findGroup(name)) != null) {       // 
                pushClassBody(gu);
                resolved = true;
                return;
            }
            
            //
            // Unknown call!
            //
            throw new ExEx("no module, procedure or class '" + name + "'", loc);
        }
    }
    
    /**
     * Check that this inbuilt module or procedure has no key-matched
     * input or output arguments.
     * @param   inl is a list of input argument nodes
     * @param   onl is a list of output argument nodes
     * @param   type is the string "module" or "procedure"
     *
    private void checkNoKeys (NodeList inl, NodeList onl, String type) {
        for (Node n: inl) {
            if (n instanceof KeyMatchNode)
                throw new ExEx("inbuilt " + type + " '" + name + "' has a keymatch input argument", loc);
        }
        for (Node n: onl) {
            if (n instanceof KeyMatchNode)
                throw new ExEx("inbuilt " + type + " '" + name + "' has a keymatch output argument", loc);
        }
    }*/
    
    /**
     * Get the identifier of the called module or procedure.
     * @return  the module or procedure identifier
     */
    public String getId () {
        return(name);
    }
}
