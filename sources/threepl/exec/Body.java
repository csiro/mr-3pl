package threepl.exec;

import static threepl.ThreePL.top_scope_skip;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import threepl.exceptions.ExEx;
import threepl.exec.Var.IDtype;
import threepl.mods.InbuiltMods;
import threepl.procs.InbuiltProcs;
import threepl.funcs.InbuiltFuncs;
import threepl.nodes.BlockNode;
import threepl.nodes.CompoundValNode;
import threepl.nodes.ClassFuncCallNode;
import threepl.nodes.Ident;
import threepl.nodes.KeyMatchNode;
import threepl.nodes.ListNode;
import threepl.nodes.Node;
import threepl.nodes.NodeList;
import threepl.nodes.ProcModCallNode;
import threepl.nodes.PtrValNode;
import threepl.nodes.VarNode;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;
import threepl.ThreePL;
import threepl.parser.Token;
import threepl.procs.IdentProc;
import threepl.procs.InbuiltProc;
import threepl.procs.RefProc;

/**
 * This class is the common superclass of a group (3PL class), module, procedure or function.
 *
 * 'name' is the name of the group (3PL class), module, procedure or function.
 *
 * 'enclosing' is the Body in which this one is nested.
 * 
 * 'scope' is the scope class for this Body.
 * 
 * 'file_scope' is the file module scope for this Body.
 * 
 * 'class_scope' is the scope of the top level module, the one below the file module.
 * 
 * 'groups', 'modules', 'procedures' and 'functions' are maps of groups (3PL classes),
 * modules, procedures and functions declared in this Body.
 * 
 * 'input_nodes' and 'output_nodes' are 'NodeList's of parameter
 * ProcCall nodes calling "var(...)".
 *
 * 'in_args' and 'out_args'  are lists of input and output arguments.
 * 
 * 'in_keys' and 'out_keys' are maps of any keys associated with arguments.
 * 
 * 'num_input_args' and 'num_output_args' specify the arguments counts.
 * 
 * The input and output parameters are created when the
 * module or function is called. Argument modes and types
 * are checked against those of the parameters.
 * Input and output arguments are then substituted in place
 * of parameters.
 * 
 * 'tree' is the module or function body code tree.
 * 
 * 'loc' is the source file location of the module, procedure or function definition
 */
public abstract class Body implements Constant {
    protected String                        name;
    protected Body                          enclosing_body;
    protected Scope                         scope;                  // scope for this Body
    protected Scope                         file_scope;             // associated static file scope
    protected Scope                         class_common_scope;     // associated static class scope
    protected boolean                       is_private;             // only callable within Group (3PL class)
    protected TreeMap<String, Group>        groups = new TreeMap<String, Group>();
    protected TreeMap<String, Module>       modules = new TreeMap<String, Module>();
    protected TreeMap<String, Procedure>    procedures = new TreeMap<String, Procedure>();
    protected TreeMap<String, Function>     functions = new TreeMap<String, Function>();
    protected NodeList                      input_nodes;            // input par declaration call nodes
    protected NodeList                      output_nodes;           // output par declaration call nodes
    protected ArrayList<Object>             in_args;                // input arguments
    protected TreeMap<String, Object>       in_keys;                // input keys
    protected ArrayList<Object>             out_args;               // output arguments
    protected TreeMap<String, Object>       out_keys;               // output keys
    protected int                           num_input_args;
    protected int                           num_output_args;
    protected BlockNode                     common_tree = null;     // Class common block if present
    protected BlockNode                     tree;                   // Class, Module, Procedure or Function code block
    protected SrcLoc                        loc;                    // definition source location

    // Variables used during argument evaluation and passing to parameters.
    protected Immediate         pointers;
    protected Immediate         keys;
    protected Immediate         argmap;
    protected RefOrVal          arg_rov;
    protected Ref               arg_ref;
    protected Val               arg_val;
    protected CompoundValNode   cvnode;
    protected int[]             dim_des;
    protected SrcLoc            arg_loc = loc;
    protected boolean           using_keys = false;
    protected boolean           varargs = false;
    protected int               vararg_index = 0;
    
    static int                  callcount = 0;

    /**
     * Construct a dummy module body for the global module.
     */
    public Body () {
        name = "global";
        this.loc = new SrcLoc("", 0, 0);
        input_nodes = null;
        output_nodes = null;
        tree = null;
    }
 
    /**
     * Construct a new module, procedure or function body.
     * @param   t is the module, procedure or function identifier token
     *          which also provides the source file location of the
     *          definition
     */
    public Body (Token t) {
        name = t.image;
        if (name.startsWith("#")) {
            // a file module body
            /* use abbreviated file name used instead
            String[] a = name.split("#");
            name = a[2].substring(0, a[2].length()-4);
            */
            if (ThreePL.dir_index == 0)
                name = ThreePL.file_name;
            else
                name = "FMOD" + ThreePL.dir_index + "/" + ThreePL.file_name;
        } else if (name.equals("[")) {
            // an inline module body
            name = "ILM" + callcount++;
        }
        loc = new SrcLoc(t);
        input_nodes = new NodeList(loc);
        output_nodes = new NodeList(loc);
        tree = null;
    }

    /**
     * Add a group (3PL class) to the group map for this block.
     * If it is a class group add it to the group map in the associated Scope class.
     * Otherwise add it to the module map in this Body class.
     * 3PL class names must be distinct from function names.
     * @param   group is the group (3PL class)
     */
    public void addGroup (Group group) {
        String  name = group.getName();
        SrcLoc  loc = group.getSrcLoc();
        if (groups.containsKey(name))
            throw new ExEx("class '" + name +
                        "' redefined in current scope", loc);
        if (functions.containsKey(name))
            throw new ExEx("group '" + name + "' duplicates function '" +
                            name + "' in current scope", loc);
        groups.put(name, group);
    }

    /**
     * Add a module to the module map for this block.
     * If it is a class module add it to the module map in the associated Scope class.
     * Otherwise add it to the module map in this Body class.
     * Module names must be distinct from procedure names.
     * @param   module is the module
     */
    public void addModule (Module module) {
        String  name = module.getName();
        SrcLoc  loc = module.getSrcLoc();
        if (InbuiltMods.modDefined(name))
            throw new ExEx("module '" + name + "' duplicates an inbuilt module ", loc);
        if (modules.containsKey(name))
            throw new ExEx("module '" + name +
                        "' redefined in current scope", loc);
        if (procedures.containsKey(name))
            throw new ExEx("module '" + name + "' duplicates procedure '" +
                            name + "' in current scope", loc);
        modules.put(name, module);
    }

    /**
     * Add a procedure to the procedure map for this block.
     * If it is a class procedure add it to the procedure map in the associated Scope class.
     * Otherwise add it to the procedure map in this Body class.
     * Procedure names must be distinct from module names.
     * @param   procedure is the procedure
     */
    public void addProcedure (Procedure procedure) {
        String  name = procedure.getName();
        SrcLoc  loc = procedure.getSrcLoc();
        if (InbuiltProcs.procDefined(name))
            throw new ExEx("procedure '" + name + "' duplicates an inbuilt procedure ", loc);
        if (procedures.containsKey(name))
            throw new ExEx("procedure '" + name +
                        "' redefined in current scope", loc);
        if (modules.containsKey(name))
            throw new ExEx("procedure '" + name + "' duplicates module '" +
                        name + "' in current scope", loc);
        procedures.put(name, procedure);
    }
    
    /**
     * Add a function to the function map for this block.
     * If it is a class function add it to the function map in the associated Scope class.
     * Otherwise add it to the function map in this Body class.
     * Function names must be distinct from 3PL class names.
     * @param   function is the function
     */
    public void addFunction (Function function) {
        String  name = function.getName();
        SrcLoc  loc = function.getSrcLoc();
        if (InbuiltFuncs.funcDefined(name))
            throw new ExEx("function '" + name + "' duplicates an inbuilt function ", loc);
        if (functions.containsKey(name))
            throw new ExEx("function '" + name +
                    "' redefined in current scope", loc);
        if (groups.containsKey(name))
            throw new ExEx("function '" + name + "' duplicates class '" +
                    name + "' in current scope", loc);
        functions.put(name, function);
    }
    
    /**
     * Set the common code tree.
     * @param bn is the common code tree
     */
    public void addCommon (BlockNode bn) {
        common_tree = bn;
    }
    
    /**
     * Set the static class common scope for this Group Body.
     * @param s is the common class Scope
     */
    public void setClassCommonScope (Scope s) {
        class_common_scope = s;
        // Set the class common scope for this Group (3PL class).
        Collection<Module>  mc = modules.values();
        
        // Set the class common scope for this Group nested Module.
        Iterator<Module>    mit = mc.iterator();
        while (mit.hasNext()) {
            Module m = mit.next();
            m.setClassCommonScope(s);
        }
        
        // Set the class common scope for this Group nested Procedure.
        Collection<Procedure>  pc = procedures.values();
        Iterator<Procedure>    pit = pc.iterator();
        while (pit.hasNext()) {
            Procedure p = pit.next();
            p.setClassCommonScope(s);
        }
        
        // Set the class common scope for this Group nested Function.
        Collection<Function>  fc = functions.values();
        Iterator<Function>    fit = fc.iterator();
        while (fit.hasNext()) {
            Function f = fit.next();
            f.setClassCommonScope(s);
        }
    }
    
    /**
     * Get the class common scope for this Group Body.
     * @return the static class Scope
     */
    public Scope getClassCommonScope () {
        return(class_common_scope);
    }
    
    /**
     * Get the group (3PL class) map for this block.
     * @return  the group map
     */
    public TreeMap<String, Group> getGroups () {
        return(groups);
    }
    
    /**
     * Get the module map for this block.
     * @return  the module map
     */
    public TreeMap<String, Module> getModules () {
        return(modules);
    }
    
    /**
     * Get the procedure map for this block.
     * @return  the procedure map
     */
    public TreeMap<String, Procedure> getProcedures () {
        return(procedures);
    }
    
    /**
     * Get the function map for this block.
     * @return  the function map
     */
    public TreeMap<String, Function> getFunctions () {
        return(functions);
    }
   
    /**
     * Set the input parameters node list.
     * @param   nl is a node list
     */
    public void setInputPars (NodeList nl) {
        input_nodes = nl;
    }
    
    /**
     * Set the output parameters node list.
     * @param   nl is a node list
     */
    public void setOutputPars (NodeList nl) {
        output_nodes = nl;
    }
   
    /**
     * Determine if there are input or output parameters.
     * @return   true if there are input or output parameters
     */
    public boolean hasParams () {
        return((input_nodes.size() != 0) || (output_nodes.size() != 0));
    }
    
    /**
     * Set the code tree for this module, procedure or function.
     * @param   tree is the code tree
     */
    public void addTree (BlockNode tree) {
        this.tree = tree;
    }
    
    public Body getEnclosing () {
        return(enclosing_body);
    }
    
    /**
     * Get the source file location this module, procedure or function.
     * @return  the location
     */
    public SrcLoc getSrcLoc () {
        return(loc);
    }
    
    /**
     * Get the source file location of the call to this module, procedure or
     * function.
     * @return  the call location
     */
    public SrcLoc getCallLoc () {
        return(arg_loc);
    }
    
    /**
     * Get the identifier for this module, procedure or function.
     * @return  the identifier
     */
    public String getName () {
        return(name);
    }
    
    /**
     * Get the scope for this module, procedure or function.
     * @return  the scope
     */
    public Scope getScope () {
        return(scope);
    }
    
    /**
     * Get the file scope for this module, procedure or function.
     * @return  the file scope
     */
    public Scope getFileScope () {
        return(file_scope);
    }
    
    /**
     * Get the class scope for this module, procedure or function.
     * @return  the class scope
     */
    /*public Scope getClassScope () {
        if (class_body != null)
            return(class_body.scope);
        else
            return(null);
    }*/

    /**
     * Return the number of input arguments.
     * @return  the number of input arguments
     */
    public int numInArgs () {
        return(num_input_args);
    }

    /**
     * Return the number of output arguments.
     * @return  the number of output arguments
     */
    public int numOutArgs () {
        return(num_output_args);
    }

    /**
     * Get arguments in this module, procedure or function call.
     * The input is a list of the subnodes of the module, procedure or function
     * call. The first subnode is a NodeList of the input arguments. The
     * second subnode is a NodeList of the output arguments (functions do
     * not have one). The argument <b>i</b> selects one of these.
     * The result is a list of argument values.
     * @param   args is a list of argument nodes
     * @param   input is true for input arguments and false for output arguments
     * @param   mess is a header for any error messages during argument
     *          evaluation
     */
    public void getArgs (NodeList args, boolean input, String mess) {
        ArrayList<Object>       al = new ArrayList<Object>();
        TreeMap<String, Object> idents = new TreeMap<String, Object>();
       
        if (input) {
            in_args = al;
            in_keys = idents;
        } else {
            out_args = al;
            out_keys = idents;
        }
        if ((args == null) || (args.size() == 0))
            return;

        SrcLoc      loc = args.getSrcLoc();
        Node        arg;
        String      key = null;
        Object      a;
        Iterator<Node>    ita = args.iterator();
        while (ita.hasNext()) {
            // argument
            arg = ita.next();
            if (arg != null) {
                if (arg instanceof KeyMatchNode) {
                    key = ((KeyMatchNode)arg).getParamKey();
                    arg = arg.getSubNode(0);
                }
                if (arg instanceof VarNode) {
                    VarNode argvn = (VarNode)arg;
                    Ref     ref;
                    if (input)
                        ref = argvn.getRefAndIdent();
                    else
                        ref = argvn.getRef();
                    if (ref == null)
                        a = null;
                    else if (input && ((ref.getPrimType() == Ptype.LIST) ||(ref.getPrimType() == Ptype.MAP)))
                        a = argvn.getVal();
                    else
                        a = ref;
                } else if (arg instanceof PtrValNode) {
                    if (input)
                        // input argument
                        //a = arg.getVal();
                        //a = arg.getRef("input argument");
                        a = ((PtrValNode) arg).getRefOrIdent("input argument");
                    else
                        // output argument
                        a = arg.getRef("output argument");
                } else if (arg instanceof CompoundValNode) {
                    if (input)
                        // input argument
                        a = arg;
                    else
                        throw new ExEx("compound value cannot be an output argument", loc);
                } else if (arg instanceof ClassFuncCallNode) {
                    if (input) {
                        // input argument
                        a = arg.getVal();
                        if (((ClassFuncCallNode) arg).getId().equals("arglist")) {
                            argList(a, al, idents, true);
                            continue;
                        }
                    } else {
                        // output argument
                        a = arg.getRef(null);
                        if (((ClassFuncCallNode) arg).getId().equals("arglist")) {
                            a = arg.getVal();
                            argList(a, al, idents, false);
                            continue;
                        }
                   }
                } else {
                    if (input)
                        // input argument
                        a = arg.getVal();
                    else
                        // output argument not a variable
                        throw new ExEx(mess + " output argument not a variable", loc);
                }
            } else
                a = null;

            if (key != null) {
                if (idents.containsKey(key))
                    throw new ExEx("duplicated parameter assignment '" + key + "'", loc);
                if (a != null) {
                    idents.put(key, a);
                    if (a instanceof Val)
                        ((Val)a).setParamKey(key);
                    else if (a instanceof Ref)
                        ((Ref)a).setParamKey(key);
                    else
                        ((CompoundValNode)a).setParamKey(key);
                }
                key = null;
            }
            al.add(a);
        }
    }
    
    static void argList (Object a, ArrayList<Object> al, TreeMap<String, Object> idents, boolean input) {
        Val v = (Val)a;
        Object[]    oa = v.getVals();
        for(int i=0 ; i<oa.length ; ) {
            String  key = (String)oa[i++];
            RefOrVal     rov = (RefOrVal)oa[i++];
            al.add(rov);
            if (key != null) {
                idents.put(key, rov);
                rov.setParamKey(key);
            }
        }
    }

    /**
     * Create input or output parameters for a module, procedure or function
     * following a call and assign arguments to them.
     * @param   mess is a header for any error messages during parameter
     *          processing
     * @param   input is true for input parameters and false for output parameters
     * @param   loc is the source file location
     */
    public void params (String mess, boolean input, SrcLoc loc) {
        int             params_index = 0;
        SrcLoc          par_loc = null;
        ArrayList<Var>  vars = null;
        Var             var = null;
        Node            in_par = null;
        
        pointers = null;
        keys = null;
        varargs = false;
        using_keys = false;

        // Create input parameters.
        // Evaluate in calling body.
        // Create in current body.
        int                 num_args = input ? in_args.size() : out_args.size();
        Iterator<Node>      itp = input ? input_nodes.iterator() : output_nodes.iterator();
        Iterator<Object>    ita = input ? in_args.iterator() : out_args.iterator();
        // iterate through parameters
        while (itp.hasNext()) {
            in_par = itp.next();
            par_loc = in_par.getSrcLoc();

            // check if parameter is "varargs"
            if (in_par instanceof VarNode) {
                if (((VarNode)in_par).getId().equals("varargs")) {
                    // handle varargs - copy remainder of arguments
                    varargs = true;
                    using_keys = false;
                    vararg_index = 0;
                    if (itp.hasNext())
                        throw new ExEx(mess + " input parameters following varargs",
                                                            par_loc);
                    int     vsize = (params_index <= num_args) ? (num_args - params_index) : 0;
                    
                    Type    ptrtype = new Type("[" + vsize + "]->", loc);
                    String  pname = input ? "inargs" : "outargs";
                    pointers = new Immediate(new Ident(pname, Context.DEFAULT), ptrtype, null, true, false, par_loc);
                    ThreePL.addVar(pointers, Context.DEFAULT, loc);
                    
                    Type    strtype = new Type("[" + vsize + "]str", loc);
                    String  kname = input ? "inkeys" : "outkeys";
                    keys = new Immediate(new Ident(kname, Context.DEFAULT), strtype, null, true, false, par_loc);
                    ThreePL.addVar(keys, Context.DEFAULT, loc);
                    
                    Type    mtype = new Type("map", loc);
                    String  mname = input ? "inmap" : "outmap";
                    argmap = new Immediate(new Ident(mname, Context.DEFAULT), mtype, null, true, false, par_loc);
                    ThreePL.addVar(argmap, Context.DEFAULT, loc);
                    
                    while (ita.hasNext()) {
                        getArg(ita, null, null, input, par_loc);
                    
                        if (arg_rov == null) {
                            if (cvnode != null)
                                throw new ExEx(mess + " compound constant argument to input varargs",
                                                                            par_loc);
                            else
                                throw new ExEx(mess + " null argument to " + (input ? "input" : "output") + " varargs",
                                                                            par_loc);
                        }

                        String  name = (input?"idummy#":"odummy#") + hsep + vararg_index;
                        Mode    mode = arg_rov.getMode();
                        if (mode == null)
                            throw new ExEx("undefined variable '" + ((Ref)arg_rov).getId().getId() + "'", par_loc);
                        Type    type = new Type(arg_rov.getTypeString(), par_loc);
                        switch (mode) {
                        case IMMEDIATE:
                            var = new Immediate(new Ident(name, Context.DEFAULT), type, null, true, false, loc);
                            break;
                        case VALUE:
                            var = new Value(new Ident(name, Context.DEFAULT), type, null, true, false, loc);
                            break;
                        case STATIC:
                            var = new Static(new Ident(name, Context.DEFAULT), type, null, true, false, loc);
                            break;
                        case QUEUE:
                            var = new Queue(new Ident(name, Context.DEFAULT), type, null, true, false, loc);
                            break;
                        case SELECTVALUE:
                            var = new SelectValue(new Ident(name, Context.DEFAULT), type, null, true, false, loc);
                            break;
                        case PRIORITY:
                            var = new Priority(new Ident(name, Context.DEFAULT), type, null, true, false, loc);
                            break;
                        case INPUT:
                            var = new Input(new Ident(name, Context.DEFAULT), type, null, true, false, null, loc);
                            break;
                        case OUTPUT:
                            var = new Output(new Ident(name, Context.DEFAULT), type, null, true, false, loc);
                            break;
                        case CLOCK:
                            var = new Clock(new Ident(name, Context.DEFAULT), null, true, false, loc);
                            break;
                        case RMEMORY:
                            var = new Memory(new Ident(name, Context.DEFAULT), true, 0, null, null, true, false, null, null, null, loc);
                            break;
                        case CMEMORY:
                            var = new Memory(new Ident(name, Context.DEFAULT), false, 0, null, null, true, false, null, null, null, loc);
                            break;
                        default:
                            break;
                        }
                    
                        if (input)
                            inassign(var, arg_rov, false, mess, par_loc);
                        else
                            outassign(var, arg_rov, mess, par_loc);
                    }
                } else
                    throw new ExEx(mess + "input parameter" +
                        " not variable declaration or varargs", par_loc);
                return;
            }
            
            if (!(in_par instanceof ProcModCallNode))
                // input parameter is not a procedure call or "varargs"
                throw new ExEx(mess + " input parameter not variable declaration", par_loc);
            
            // call parameter creation procedure - this may produce
            // more than one parameter, in which case take more arguments
            ProcModCallNode pcn = (ProcModCallNode)in_par;
            InbuiltProc     p = pcn.getParamProc(mess, par_loc);
            String          pname = pcn.getId();
            NodeList        subnodes  = pcn.getSubNodes();
            ListNode        iln = (ListNode)subnodes.get(0);
            NodeList        inargs = iln.getNodeList();
            NodeList        outargs = new NodeList(par_loc);   // empty node list for output args
            boolean         fi = p instanceof RefProc; // force indirect

            ArrayList<String>   idents = p.getIdents(inargs, pname);
            String              ident;
            if (idents.size() == 1) {
                ident = idents.get(0);
                getArg(ita, ident, p, input, par_loc);
                vars = p.execute(inargs, outargs, idents, arg_rov, dim_des, input, !input);
                if (vars != null) {
                    if (input)
                        inassign(vars.get(0), arg_rov, fi, mess, par_loc);
                    else
                        outassign(vars.get(0), arg_rov, mess, par_loc);
                }
                params_index++;
            } else {
                vars = p.execute(inargs, outargs, idents, null, null, input, !input);
                Iterator<Var>    vit = vars.iterator();
                while (vit.hasNext()) {
                    var = vit.next();
                    getArg(ita, var.getID(IDtype.LITERAL), p, input, par_loc);
                    if (input)
                        inassign(var, arg_rov, fi, mess, par_loc);
                    else
                        outassign(var, arg_rov, mess, par_loc);
                    params_index++;
                }
            }
        }

        TreeMap<String, Object> keymap = input ? in_keys : out_keys;
        if (!keymap.isEmpty()) {
            // unmatched key arguments
            Set<String>         ks = keymap.keySet();
            Iterator<String>    ksit = ks.iterator();
            String      s = "";
            while (ksit.hasNext())
                s += " " + ksit.next();
            throw new ExEx("parameter(s) not found matching key(s) " + s, loc);
        }
        if (ita.hasNext())
            // too many arguments
            throw new ExEx(mess + " too many " + (input ? "input" : "output") + " arguments", loc);
    }
    
    private void getArg (
        Iterator<Object>    it,
        String              ident,
        InbuiltProc         p,
        boolean             input,
        SrcLoc              par_loc
    ) {
        String  key = null;
        TreeMap<String, Object> keymap = input ? in_keys : out_keys;
        Object  o = null;

        arg_ref = null;
        arg_val = null;
        arg_rov = null;
        cvnode = null;
        dim_des = null;
        arg_loc = loc;
        if (it.hasNext())
            o = it.next();
        if (o != null) {
            if (o instanceof Val)
                key = ((Val)o).getParamKey();
            else if (o instanceof Ref)
                key = ((Ref)o).getParamKey();
            else if (o instanceof CompoundValNode)
                key = ((CompoundValNode)o).getParamKey();
        }
        if (!varargs && (key != null))
            using_keys = true;
        if (using_keys) {
            if ((o != null) && (key == null) && !varargs)
                throw new ExEx("positional argument follows key arguments", loc);
            o = keymap.get(ident);
        }
        if (ident != null)
            keymap.remove(ident);
        if (o != null) {
            if (o instanceof CompoundValNode) {
                cvnode = (CompoundValNode)o;
                dim_des = cvnode.getDimDes();
                arg_loc = cvnode.getSrcLoc();
            } else {
                arg_rov = (RefOrVal)o;
                arg_loc = arg_rov.getSrcLoc();
                if ((arg_rov != null) &&
                    (arg_rov.getMode() == null) &&
                    ((p == null) || !(p instanceof IdentProc)))
                    throw new ExEx("undefined variable '" + ((Ref)arg_rov).getId().getId() + "'", par_loc);
            }
        }
        if (arg_loc == null)
            arg_loc = loc;
    }


    private void inassign (
        Var         var,
        RefOrVal    arg_rov,
        boolean     force_indirect,
        String      mess,
        SrcLoc      par_loc
    ) {
        // if argument is not null -
        //     if value parameter
        //         assign argument to value parameter
        //     else
        //         assign an indirect reference to the argument to the
        //         parameter
        // else
        //     parameter becomes local variable
        // if argument is immediate Ptype.NULL, no assignment is done
        // Immediate and value modes are by value, the others by reference.
        
        mess = mess + " '" + var.getID(IDtype.LITERAL) + "' ";

        if ((arg_rov != null) && (arg_rov.getPrimType() == Ptype.NULL) &&
            (arg_rov.getMode() == Mode.IMMEDIATE) &&
            (var.getWordSpec().getPrimType(0) != Ptype.PTR)) {
            return;
        }

        if ((arg_rov != null) && (arg_rov.getMode() != null)) {
            Mode    mode = var.getMode();
            Var     avar = arg_rov.getVar();
            
            if ((avar == null) && !((mode == Mode.IMMEDIATE) || (mode == Mode.VALUE) || (cvnode != null)))
                throw new ExEx(mess + " argument to parameter must be a variable", arg_loc);
            
            var.setMatched();
            switch (var.getMode()) {
            case IMMEDIATE:
                // immediate mode
                if (arg_rov instanceof Ref)
                    arg_val = ((Ref)arg_rov).getVal(arg_loc);
                else
                    arg_val = (Val)arg_rov;
                arg_val.setInarg(); // set the inarg flag so Type() will use CALLING scope for types as strings
                if (arg_val.getMode() != var.getMode())
                    throw new ExEx(mess + " target argument to immediate parameter", arg_loc);
                var.checkMatch(arg_val, false, mess + "input parameter assignment ", par_loc);
                if (force_indirect) {
                    if (!(arg_rov instanceof Ref))
                        throw new ExEx(mess + " immediate mode argument to ref() is not a variable", arg_loc);
                    var.setIndirect(arg_rov, arg_loc);
                } else
                    var.assignTo(AST.IMASS, null, arg_val, arg_loc);
                break;
            case VALUE:
                Ref par_ref = var.getRef(null, null, par_loc);
                if (arg_rov instanceof Ref)
                    arg_val = ((Ref)arg_rov).getVal(arg_loc);
                else
                    arg_val = (Val)arg_rov;
                var.checkMatch(arg_val, false, mess + "input parameter assignment ", par_loc);
                if (force_indirect) {
                    if (!(arg_rov instanceof Ref)) {
                        Var v = arg_rov.getVar();
                        if (v != null)
                            arg_rov = new Ref(v, arg_rov.getMode(), arg_rov.getSubFields(), arg_rov.getTDEVar(), par_loc);
                        else
                            throw new ExEx(mess + " value mode argument to ref() is not a variable", arg_loc);
                    }
                    var.setIndirect(arg_rov, arg_loc);
                } else {
                /*
                if ((arg_val.getMode() == Mode.VALUE) && (arg_val.getVar() != null))
                    //var.setIndirect(arg_val, arg_loc); DOESN'T WORK!
                    par_ref.valValAssignTo(arg_val.getVar(), arg_val.getSubFields(), par_loc);
                else
                */
                    par_ref.assignTo(arg_val, arg_loc);
                }
                break;
            case INPUT:
            case SELECTVALUE:
            case STATIC:
            case QUEUE:
            case PRIORITY:
                var.checkMatch(arg_rov, true, mess + "input parameter assignment ", par_loc);
                var.setIndirect(arg_rov, arg_loc);
                if (mode == Mode.INPUT) {
                    // For INPUT mode copy any attributes from the parameter variable to the argument variable
                    avar.getAttributes().putAll(var.getAttributes());
                }
                break;
            case CMEMORY:
            case RMEMORY:
                Memory  pmvar = (Memory)var;
                Memory  amvar = (Memory)avar;
                WordSpec    paws = pmvar.getMemAddrWordSpec();
                WordSpec    pdws = pmvar.getMemDataWordSpec();
                WordSpec    aaws = amvar.getMemAddrWordSpec();
                WordSpec    adws = amvar.getMemDataWordSpec();
                if (paws != null)
                    paws.checkMatch(aaws, true, "input parameter assignment address ", par_loc);
                if (pdws != null)
                    pdws.checkMatch(adws, true, "input parameter assignment data ", par_loc);
                var.setIndirect(arg_rov, arg_loc);
                if (var.getMode() != arg_rov.getMode())
                    throw new ExEx(mess + "argument/parameter mode mismatch", arg_loc);
                break;
            case CLOCK:
                var.setIndirect(arg_rov, arg_loc);
                break;
            case OUTPUT:
                var.checkMatch(arg_rov, true, mess + "input parameter assignment ", par_loc);
                var.setIndirect(arg_rov, arg_loc);
                break;
            default:
                break;
            }
        } else if (cvnode != null) {
            //if (var.getMode() != Mode.IMMEDIATE)
            //    throw new ExEx(mess + "compound value argument to target parameter", arg_loc);
            top_scope_skip = true; // This is a hack, but can't see an easy way to do this.
            arg_val = cvnode.getVal(var.getType());
            top_scope_skip = false;
            var.getWordSpec().checkMatch(arg_val, false, mess + "input parameter assignment ", par_loc);
            switch (var.getMode()) {
            case IMMEDIATE:
                var.assignTo(AST.IMASS, null, arg_val, arg_loc);
                break;
            case VALUE:
                Ref par_ref = var.getRef(null, null, par_loc);
                var.assignTo(par_ref, arg_val, arg_loc);
                break;
            default:
                throw new ExEx(mess + "compound immediate value argument to " + var.getMode().modename() + " parameter not allowed", arg_loc);
            }
            var.setMatched();
        }

        if (varargs) {
            pointers.ptrAssignTo(vararg_index, var, par_loc);
            keys.keyAssignTo(vararg_index, arg_rov.getParamKey(), par_loc);
            argmap.mapAssignTo( arg_rov.getParamKey(), var, loc);
            vararg_index++;
        }
    }

    private void outassign (Var var, RefOrVal arg_rov, String mess, SrcLoc par_loc) {
        // if argument is not null -
        //     if value passed to input, selectvalue, static, queue or priority,
        //         assign parameter to value
        //     else
        //         assign an indirect reference to the argument to the
        //         parameter
        // else
        //     parameter becomes local variable
        arg_ref = (Ref)arg_rov;
        if (arg_ref != null) {
            if ((arg_ref.getMode() == Mode.VALUE) &&
                ((var.getMode() == Mode.INPUT) ||
                 (var.getMode() == Mode.SELECTVALUE) ||
                 (var.getMode() == Mode.STATIC) ||
                 (var.getMode() == Mode.QUEUE) ||
                 (var.getMode() == Mode.PRIORITY))) {
                Val pval = var.getVal(null, (NodeList)null, par_loc);
                arg_ref.assignTo(pval, par_loc);
            } else {
                if (arg_rov.getVar() == null)
                    throw new ExEx(mess + " argument to parameter must be a variable", arg_loc);
                if (var.getMode() == Mode.RMEMORY || var.getMode() == Mode.CMEMORY) {
                    Var     avar = arg_rov.getVar();
                    Memory  pmvar = (Memory)var;
                    Memory  amvar = (Memory)avar;
                    WordSpec    paws = pmvar.getMemAddrWordSpec();
                    WordSpec    pdws = pmvar.getMemDataWordSpec();
                    WordSpec    aaws = amvar.getMemAddrWordSpec();
                    WordSpec    adws = amvar.getMemDataWordSpec();
                    if (paws != null)
                        paws.checkMatch(aaws, true, "input parameter assignment address ", par_loc);
                    if (pdws != null)
                        pdws.checkMatch(adws, true, "input parameter assignment data ", par_loc);
                } else
                    var.checkMatch(arg_rov, true, mess + "output parameter assignment ", par_loc);
                if ((var.getMode() == Mode.OUTPUT) || (var.getMode() == Mode.INPUT)) {
                    // For OUTPUT or INPUT mode copy any attributes from the parameter variable to the argument variable
                    arg_ref.getVar().getAttributes().putAll(var.getAttributes());
                }
                var.setIndirect(arg_ref, par_loc);
            }
            var.setMatched();
        }

        if (varargs) {
            pointers.ptrAssignTo(vararg_index, var, par_loc);
            keys.keyAssignTo(vararg_index, arg_ref.getParamKey(), par_loc);
            argmap.mapAssignTo( arg_rov.getParamKey(), var, loc);
            vararg_index++;
        }
    }
}
