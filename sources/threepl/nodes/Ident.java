package threepl.nodes;

import threepl.parser.Constant;

/**
 * This class is just a wrapper for a variable identifier string and
 * the associated scope context.
 *
 * Scope contexts for variable creation are -
 *  GLOBAL      global scope
 *  FILE        scope associated with the current source file or any nested
 *              include files
 *  CALLING     the module, procedure or function scope of the caller to the
 *              current module, procedure or function
 *  LOCAL       the current module, procedure or function scope
 *  DEFAULT     the current block scope
 *
 * Scope contexts for variable evaluation are -
 *  GLOBAL      as above
 *  FILE        as above
 *  CALLING     as above
 *  LOCAL       as above
 *  DEFAULT     all accessible scopes -
 *                  block scopes nested back to the LOCAL scope,
 *                  LOCAL scope
 *                  FILE scope
 *                  GLOBAL scope
 */
public final class Ident implements Constant {
    private String  id;
    private Context context;
      
    /**
     * Construct an identifier wrapper.
     * @param   s is the identifier
     */
    public Ident (String s) {
        if (s.startsWith("global.")) {
            context = Context.GLOBAL;
            id = s.substring(7);
        } else if (s.startsWith("file.")) {
            context = Context.FILE;
            id = s.substring(5);
        } else if (s.startsWith("calling.")) {
            context = Context.CALLING;
            id = s.substring(8);
        } else if (s.startsWith("local.")) {
            context = Context.LOCAL;
            id = s.substring(6);
        } else {
            context = Context.DEFAULT;
            id = s;
        }
    }

    /**
     * Construct an identifier wrapper.
     * @param   s is a plain identifier
     * @param   context is the variable scope context, Context.GLOBAL, Context.LOCAL
     *          or Context.DEFAULT
     */
    public Ident (String s, Context context) {
        id = s;
        this.context = context;
    }
    
    /**
     * Get the identifier string from an identifier wrapper.
     * @return  the identifier string
     */
    public String getId () {
        return(id);
    }
    
    /**
     * Get the scope context from an identifier wrapper.
     * @return  the scope context
     */
    public Context getScopeContext () {
        return(context);
    }
}
