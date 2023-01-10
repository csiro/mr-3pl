package threepl.procs;

import java.util.ArrayList;
import java.util.TreeMap;

import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.QueueRefs;
import threepl.exec.RefOrVal;
import threepl.exec.SubFieldList;
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
import static threepl.parser.Functions.whichArg;


/**
 * This is the calling interface for inbuilt procedures.
 */
public class InbuiltProc implements Constant {
    public boolean                  check_null_input_args;
    public boolean                  check_null_output_args;
    public boolean                  target_inline;
    public boolean                  allowed_as_param;
    public boolean                  allowed_attributes;
    public TreeMap<String,Integer>  ipnames;
    public TreeMap<String,Integer>  opnames;
    
    public InbuiltProc () {
        ipnames = new TreeMap<String,Integer>();
        opnames = new TreeMap<String,Integer>();
        check_null_input_args = false;
        check_null_output_args = false;
        allowed_as_param = false;
        allowed_attributes = false;
    }
    
    /**
     * Execute the variable declaration inbuilt procedures that can also
     * be called to create module/procedure/function parameters.
     * @param   inargs is a list of input argument tree nodes
     * @param   outargs is a list of output argument tree nodes
     * @param   names is a list of identifiers evaluated via method
     *          getIdents() prior to calling execute()
     * @param   par_arg is optional and is an argument that has been 
     *          passed to a parameter and is used to get the parameter
     *          type if none is declared
     * @param   dim_des is a dimensional description for a compound constant
     *          argument or is null
     * @param   is_input is true if this variable is the input parameter
     *          for a module, procedure or function
     * @param   is_output is true if this variable is the output parameter
     *          for a module or procedure 
     * @return  the created declared variables
     */
    public ArrayList<Var> execute (
        NodeList            inargs,
        NodeList            outargs,
        ArrayList<String>   names,
        RefOrVal            par_arg,
        int[]               dim_des,
        boolean             is_input,
        boolean             is_output
    ) {
        throw new ExEx("InbuiltProc sys error 1");
    }

    /**
     * Execute an inbuilt procedure which does not generate in-line code.
     * @param   inargs is a list of input argument tree nodes
     * @param   outargs is a list of output argument tree nodes
     * @param   toplevel is true if we are at the module, procedure or function
     *          level
     */
    public void execute (NodeList inargs, NodeList outargs, boolean toplevel) {
        throw new ExEx("InbuiltProc sys error 2");
    }

    /**
     * Execute an in-line inbuilt procedure.
     * @param   inargs is a list of input argument tree nodes
     * @param   outargs is a list of output argument tree nodes
     * @param   esig is an exception/restart signal, or null
     * @param   startl is a list of start signals for target statements
     *          below this node
     * @param   finishl is a list of finish signals for target statements
     *          below this node
     * @param   queues returns all the queue availability signals
     *          accumulated from code below
     * @param   availok is true if a previous sync makes a queue availability
     *          wait unnecessary
     * @param   pri_in is an optional input signal to a priority encoder
     * @param   pri_out is an optional output signal from a priority encoder
     */
    public void execute (
        NodeList            inargs,
        NodeList            outargs,
        TDEVar              esig,
        ArrayList<TDEVar>   startl, 
        ArrayList<TDEVar>   finishl,
        QueueRefs           queues,
        boolean             availok,
        TDEVar              pri_in,
        TDEVar              pri_out
    ) {
        throw new ExEx("InbuiltProc sys error 3");
    }

    /**
     * Evaluate the identifier(s) prior to calling the main entry, execute() for
     * the variable declaration inbuilt procedures var(...),
     * int(...), log(...) or str(...).
     * @param   inargs is a list of input argument tree nodes
     * @param   pn is the procedure name (for error messages)
     * @return  the list of variable identifiers
     */
    public ArrayList<String> getIdents (NodeList inargs, String pn) {
        SrcLoc      loc = inargs.getCallLoc();
        if (inargs.size() == 0)
            throw new ExEx(pn + "() has no input arguments", loc);
        ArrayList<String>   names = new ArrayList<String>();
        Node        n = inargs.getNode(0);
        if (n instanceof CompoundValNode) {
            ArrayList<Ident>   idents = ((CompoundValNode)n).getList(pn + "()");
            for (Ident id: idents) {
                if (id.getScopeContext() != Context.DEFAULT)
                    throw new ExEx(pn + "() as parameter cannot have identifier scope context", loc);
                names.add(id.getId());
            }          
        } else {
            Ident   id = n.getIdentifier(pn + "() - 1st argument");
            if (id.getScopeContext() != Context.DEFAULT)
                throw new ExEx(pn + "() as parameter cannot have identifier scope context", loc);
            names.add(id.getId());
        }
        return(names);
    }

    /***
     * Resolve the list of input variable identifiers for a variable creation procedure.
     * If 'names' is not null then it contains a list of identifiers already
     * extracted from the first argument to the procedure. These are strings only
     * since they are module, procedure or function parameters and cannot have a
     * scope context specified. Convert these to a list of Idents with scope
     * context DEFAULT.
     * If 'names' is null then process the first argument to the procedure to get
     * the identifiers. These may have scope context.
     * @param   inargs is the procedure input argument list
     * @param   names is a list of identifiers already extracted from the
     *          first argument to the procedure
     * @param   pn is the procedure name (for error messages)
     * @return  the Ident list
     */
    public static ArrayList<Ident> resolveIdents (NodeList inargs, ArrayList<String> names, String pn) {
        ArrayList<Ident>   idents;
        if (names != null) {
            idents = new ArrayList<Ident>();
            for (String s: names) {
                idents.add(new Ident(s, Context.DEFAULT));
            }
        } else {
            Node n = inargs.getNode(0);
            if (n == null)
                idents = new ArrayList<Ident>(); // return empty list
            else if (n instanceof CompoundValNode)
                idents = ((CompoundValNode)n).getList(pn + "()");
            else {
                Ident id = n.getIdentifier(pn + "() - 1st argument");
                idents = new ArrayList<Ident>();
                idents.add(id);
            }
        }
        return(idents);
    }
    /**
     * Collect attribute arguments from the input argument list into
     * a map.
     * @param   name is a variable identifier to prepend to an error message
     * @param   nl is the input argument node list
     * @param   sn is the starting index for key/value attribute arguments
     * @param   loc is the source file location
     * @return  the attribute map or null
     */
    @SuppressWarnings("unchecked")
    public static Val attributeArguments(String name, NodeList nl, int sn, SrcLoc loc) {
        Val     aval = null;
        int     in = nl.size();
        if (sn >= in)
            return(null);
        TreeMap<String, Val>    attr = new TreeMap<String, Val>();
        Object[]                oa = new Object[1];
        Type[]                  ta = new Type[1];
        Type                    atype = new Type(Ptype.MAP, 0);
        WordSpec                aws = atype.getWordSpec(null, loc);
        oa[0] = attr;
        ta[0] = atype;
        aval = new Val(oa, ta, aws, null, new SubFieldList(), null);
        for (int n=sn ; n<in ; n++) {
            Node    na = nl.getNode(n);
            if (na instanceof KeyMatchNode) {
                String key = ((KeyMatchNode)na).getParamKey();
                na = na.getSubNode(0);
                if (na instanceof CompoundValNode)
                    attr.put(key, ((CompoundValNode)na).getValArray());
                else
                    attr.put(key, na.getVal());
            } else {
                Val v = nl.getVal(n);
                if (v.getPrimType() != Ptype.MAP)
                    throw new ExEx(name + "() - " + whichArg(n+1) + " argument not key/value or type \"map\"", loc);
                attr.putAll((TreeMap<String, Val>)v.getVal(0));
            }
        }
        return(aval);
    }

    /**
     * Determine if an inbuilt procedure is allowed to be called as
     * a parameter declarator in a module, procedure or function
     * definition.
     * @return  true if this inbuilt procedure can be a parameter declaration
     */
    /*public boolean canBeParam () {
        return(allowed_as_param);
    }*/
    
    /**
     * Determine if an inbuilt procedure is to be checked for null
     * input arguments. If so, and a null argument is found, an ExEx exception
     * is thrown.
     * @return  true if this inbuilt procedure is to be checked for null
     *          input arguments
     */
    /*public boolean checkForNullInArgs () {
        return(check_null_input_args);
    }*/
    
    /**
     * Determine if an inbuilt procedure is to be checked for null
     * output arguments. If so, and a null argument is found, an ExEx exception
     * is thrown.
     * @return  true if this inbuilt procedure is to be checked for null
     *          output arguments
     */
    /*public boolean checkForNullOutArgs () {
        return(check_null_output_args);
    }*/
    
    /**
     * Determine if an inbuilt procedure generates inline target code.
     * @return  true if this inbuilt procedure generates inline target code
     */
    /*public boolean isTargetInline () {
        return(target_inline);
    }*/
}
