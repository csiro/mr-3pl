package threepl.exec;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;

import threepl.parser.Constant;

/**
 * This class contains scope information. Every code block ("if", "while", "for"
 * etc) and every module, procedure or function invocation has an associated
 * scope block created. There is also a scope block for global variables and a
 * scope block for every file module (associated with each source file). The
 * global scope block is not on the scope stack. One or more file scope blocks
 * (source files can be nested) may be on the stack and are used for variable
 * creation however variable evaluation avoids these and instead uses the file
 * scope stack. Following these on the scope stack there may be module, procedure
 * or function scopes each possible trailing one or more code scope blocks.
 * 
 * Scope contexts for variable creation are -
 *  GLOBAL      global scope
 *  FILE        scope associated with the current source file or any nested
 *              include files
 *  CALLING     the module, procedure or function scope of the caller to the
 *              current module, procedure or function
 *  LOCAL       the current module, procedure or function scope
 *  ILMOD       the current inline module
 *  DEFAULT     the current block scope
 *
 * Scope contexts for variable evaluation are -
 *  GLOBAL      as above
 *  FILE        as above
 *  CALLING     not allowed
 *  LOCAL       as above
 *  DEFAULT     all accessible scopes -
 *                  - block scopes nested back to the LOCAL scope,
 *                  - LOCAL scope
 *                  - FILE scope
 *                  - GLOBAL scope
 */
public class Scope implements Constant {
    private Context                         context;    // scope context
    private Btype                           btype;      // block type
    private Body                            body;       // body (if class, mod, proc or func) or null
    private TreeMap<String, Var>            vars;       // local variables
    protected TreeMap<String, Group>        groups = new TreeMap<String, Group>();
    protected TreeMap<String, Module>       modules = new TreeMap<String, Module>();
    protected TreeMap<String, Procedure>    procedures = new TreeMap<String, Procedure>();
    protected TreeMap<String, Function>     functions = new TreeMap<String, Function>();
    private int                             callcount;  // unique integer for variable names
    private String                          fmodname;   // file module name if a file module
    private int                             scopenum;   // unique integer for this scope

    protected static TreeMap<String, Integer>   callcounts = new TreeMap<String, Integer>();
    protected static int                        sn = 0;
    
    /**
     * Construct a global scope.
     */
    public Scope () {
        context = Context.GLOBAL;
        vars = new TreeMap<String, Var>();
        btype = Btype.UNDEF;
        scopenum = sn++;
    }
    
    /**
     * Construct a scope of a specific type.
     * Scope context is DEFAULT.
     * @param   t is the block type
     */
    public Scope (Btype t) {
        context = Context.DEFAULT;
        vars = new TreeMap<String, Var>();
        btype = t;
        callcount = getAndIncrCallCount();
        scopenum = sn++;
    }
    /**
     * Construct a file module scope for a module source file.
     * Scope context is FILE.
     * @param   filename is the file name
     */
    public Scope (String filename) {
        vars = new TreeMap<String, Var>();
        fmodname =  filename.substring(0, filename.length()-4); // strip ".3pl"
        context = Context.FILE;
        btype = Btype.FMOD;
        callcount = getAndIncrCallCount();
        scopenum = sn++;
    }
 
    /**
     * Construct a scope for a group (3PL class), module, procedure or function.
     * Scope context depends on the block type and if it is for a
     * class module, procedure or function or otherwise.
     * @param   b is the group (3PL class), module, procedure or function body
     * @param   t is the block type
     */
     public Scope (Body b, Btype t) {
        vars = new TreeMap<String, Var>();
        switch (t) {
        case ILMOD:
            context = Context.ILMOD;
            break;
        case FMOD:
        case NMOD:
        case PROC:
        case FUNC:
        case GROUP:
            context = Context.LOCAL;
            break;
        default:
            context = Context.LOCAL;
        }
        
        btype = t;
        body = b;
        callcount = getAndIncrCallCount();
        scopenum = sn++;
        // Add the various map contents from the body, assigned during parsing,
        // to the scope for the body.
        groups = b.getGroups();
        modules = b.getModules();
        procedures = b.getProcedures();
        functions = b.getFunctions();
    }
    
    /**
     * Get the unique identifier trailing integer for the block and
     * increment it, returning the new value. If there is not one
     * create it initialised to 0 and return 0.
     * @return  the new integer value
     */
    private int getAndIncrCallCount () {
        int     cc;
        String  id = null;
        if (body != null)
            id = body.getName();
        if (id == null)
            id = fmodname;
        if (id == null)
            id = btype.name();
        if (callcounts.containsKey(id))
            cc = callcounts.get(id).intValue() + 1;
        else
            cc = 0;
        callcounts.put(id, Integer.valueOf(cc));
        return(cc);
    }
    
    public boolean equals (Object o) {
        if (!(o instanceof Scope))
            return(false);
        Scope  s = (Scope)o;
        return(scopenum == s.scopenum);
    }
    
    public int hashCode () { return(scopenum); }
    
    /**
     * Get the variables in this scope.
     * @return  the variables map
     */    
    public TreeMap<String, Var> getVars () {
        return(vars);
    }
    
    /**
     * Get the classes in this scope.
     * @return  the variables map
     */    
    public TreeMap<String, Group> getGroups () {
        return(groups);
    }
    
    /**
     * Get the modules in this scope.
     * @return  the modules map
     */    
    public TreeMap<String, Module> getMods () {
        return(modules);
    }
    
    /**
     * Get the procedures in this scope.
     * @return  the variables map
     */    
    public TreeMap<String, Procedure> getProcs () {
        return(procedures);
    }
    
    /**
     * Get the functions in this scope.
     * @return  the functions map
     */    
    public TreeMap<String, Function> getFuncs () {
        return(functions);
    }
    
    /**
     * Set the context of this scope.
     * @param context is the new Context
     */
    public void setContext (Context context) { this.context = context; }
    
    /**
     * Set this scope block type.
     * @param   t is the scope block type
     */    
    public void setBlockType (Btype t) {
        btype = t;
    }
    
    /**
     * Get this scope block type.
     * @return  the scope block type
     */    
    public Btype getBlockType () {
        return(btype);
    }
    
    /**
     * Get the body for this scope.
     * The body is the <b>Module</b>, <b>Procedure</b> or <b>Function</b>
     * for which this is the scope. All other scopes have null
     * for this field.
     * @return  the scope body
     */    
    public Body getBody () {
        return(body);
    }
    
    /**
     * Get the file module name if this is a file module scope.
     * @return  the file module name or null
     */    
    public String getFileModuleName () {
        return(fmodname);
    }
    
    /**
     * Determine if this scope context is Context.LOCAL.
     * @return  true if this scope context is local
     */
    public boolean isLocal () {
        return(context == Context.LOCAL);
    }
    
    /**
     * Determine if this scope context is Context.FILE.
     * @return  true if this scope context is local
     */
    public boolean isFile () {
        return(context == Context.FILE);
    }
    
    /**
     * Determine if this scope context is Context.GLOBAL.
     * @return  true if this scope context is local
     */
    public boolean isGlobal () {
        return(context == Context.GLOBAL);
    }
    
    /**
     * Determine if this scope context is Context.ILMOD.
     * @return  true if this scope context is inline module
     */
    public boolean isILMod () {
        return(context == Context.ILMOD);
    }
    
    /**
     * Get a list of variables in the specified scope. The variables are
     * in lexicographic order of the key.
     * @return  a list consisting of a variable count, a string naming
     *          the scope context and then the variables
     */
    public LinkedList<Object> listVars () {
        LinkedList<Object>  l = new LinkedList<Object>();
        TreeMap<String, Var>     tm = new TreeMap<String, Var>();
        tm.putAll(vars);
    
        Collection<Var>  c = tm.values();
        Iterator<Var>    it = c.iterator();
        l.add(Integer.valueOf(c.size()));
        l.add(getContext());
        while (it.hasNext())
                l.add(it.next());
        return(l);
    }

    /**
     * Get a group group from this scope.
     * @return  the group module
     */
    public Group findGroup (String name) {
        if (groups.containsKey(name)) {
            Group  g = groups.get(name);
            if (!g.is_private)
                return(g);
        }
        return(null);
    }

    /**
     * Get a group module from this scope.
     * @return  the group module
     */
    public Module findMod (String name) {
        if (modules.containsKey(name)) {
            Module  m = modules.get(name);
            if (!m.is_private)
                return(m);
        }
        return(null);
    }
    
    /**
     * Get a group procedure from this scope.
     * @return  the group procedure
     */
    public Procedure findProc (String name) {
        if (procedures.containsKey(name)) {
            Procedure p = procedures.get(name);
            if (!p.is_private)
                return(procedures.get(name));
        }
        return(null);
    }
    
    /**
     * Get a group function from this scope.
     * @return  the group function
     */
    public Function findFunc (String name) {
        if (functions.containsKey(name)) {
            Function    f = functions.get(name);
            if (!f.is_private)
                return(f);
        }
        return(null);
    }
    
    /*
     * Get a string for the current scope context.
     */
    public String getContext () {
        if (context == Context.GLOBAL)
            return("glob");
        if (context == Context.FILE)
            return("file " + fmodname);
        if (body != null)
            return(btype.bname() + " " + body.getName());
        else
            return(btype.bname());
    }

    /**
     * Get the call string for this scope.
     * This string is used to generate a unique hierarchical name
     * for all variables. This is made up of a string of all
     * the called module, procedure and function identifiers in the call
     * stack with the variable name appended. Components are separated
     * by "/" and each intervening identifier must have a unique integer
     * appended to differentiate multiple calls to the same module, procedure
     * or function.
     *
     * <p>For modules, procedures and functions this is the identifier
     * with constant String ccsep, a unique integer and constant String
     * hsep appended. For all other scopes it is a unique integer with
     * constant String hsep appended. For global scope the String "" is
     * returned.
     * @return  the call string
     */    
    public String getCallString () {
        if (context == Context.GLOBAL)
            return(btype.bname() + hsep);
        if (context == Context.FILE)
            return(fmodname + hsep);
        if (body != null)
            return(body.getName() + (ccsep + callcount) + hsep);
        else
            return(btype.bname() + (ccsep + callcount) + hsep);
    }
    
    /**
     * Get a string identifying the name of the module, procedure or function
     * in which this Scope is located with a '.' appended.
     * If a global scope return "glob.".
     * If a file scope return the file name with a ':' appended.
     * Otherwise return "".
     * @return the scope string
     */
    public String getScopeString () {
        if (context == Context.GLOBAL)
            return("glob:");
        if (context == Context.FILE)
            return(fmodname + ":");
        if (body != null)
            return(body.getName() + "/");
        return("");
    }
}
