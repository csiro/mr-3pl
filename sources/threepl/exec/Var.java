package threepl.exec;

import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;

import threepl.codegen.TDE;
import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.nodes.Ident;
import threepl.nodes.NodeList;
import threepl.nodes.VarNode;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

import static threepl.ThreePL.*;
import static threepl.parser.Functions.longFromDouble;
import static threepl.parser.Functions.zeropad;

import java.util.HashMap;
import java.util.Map;
import threepl.exceptions.MapException;
import threepl.parser.AttributesMap;
import threepl.parser.Functions;

/**
 * This class represents a variable. The variable has a name String,
 * a mode and a Type, which may be complex. If the variable
 * is a module, procedure or function parameter it may have an indirect
 * pointer to another Var, depending on its mode and whether input or
 * output. If the variable is a target mode it
 * will also have an expanded name to ensure that this instance is
 * unique.
 *
 * If isInPar is true then this variable is a module, procedure or
 * function input parameter. If isOutPar is true then this variable is a
 * module or procedure output parameter.
 * 
 * Subclass 'ClockedVar' contains fields associated with the primary
 * clock of clocked variables. 'Immediate' variables are not clocked
 * since they are not target variables. 'Value' variables do not have
 * clocks, though their values usually will. 'Memory' variables have
 * their own clock fields since there is a separate clock associated
 * with each memory port. 'Clock' variables are not themselves clocked.
 * 'Queue' variables have a separate input clock.
 *
 *        | -------------------- Immediate
 *        | -------------------- Value
 *        |                 |--- SelectValue
 *        |                 |--- Static
 *        |                 |--- Queue
 *  Var --|--- ClockedVar --|--- Priority
 *        |                 |--- Input
 *        |                 |--- Output
 *        | -------------------- Memory
 *        | -------------------- Clock
 */
public abstract class Var implements Constant, TDEConstants {
    protected String                    name;       // variable name
    protected Context                   context;    // variable scope context
    protected String                    ename;      // expanded variable name
    protected Mode                      mode;       // variable mode
    protected Type                      type;       // variable type
    protected SrcLoc                    decloc;     // declaration location
    protected Scope                     scope;      // Scope in which created
    protected boolean                   isInPar;    // is an input parameter
    protected boolean                   isOutPar;   // is an output parameter
    protected boolean                   matched;    // parameter has matching argument
    protected boolean                   readonly;   // do not assign to this variable
    protected boolean                   indass;     // indirect pointer has been assigned
    protected SubFieldList              ind_sfl;    // indirect SubFieldList
    protected WordSpec                  wordspec;   // wordspec for the whole type
    protected Object[]                  val;        // value array
    protected Type[]                    val_type;   // value type array
    protected boolean                   assigned;   // has been assigned
    protected TreeMap<String, Val>      attributes; // attribute map
    protected Var                       indirect;   // pointer from parameter var to argument var
    protected boolean                   used;       // has been used
  
    public    static AttributesMap modeAttributes = new AttributesMap();
    protected static int           outcount = 0;
    public    static Var           current_create;
    
    // Type of identifier returned by getID().
    public enum IDtype {
        LITERAL,    // the ID in the Var class
        SLITERAL,   // the ID in the Var class with scope prepended
        REAL,       // the ID of the last Var in a chain
        CHAIN       // a string showing the scope and IDs of the whole chain from the head end
    }

    static {
        // Enter recognised generic attributes.
        // Xilinx-specific attributes are added to this map in netlist/xilinx/XTDECode.initialise().
        // Ptype.NULL indicates that the type is not to be checked.
        // This is used where the attribute might be a string or an integer
        // for example. It is assumed that the code that handles this attribute
        // will itself check the type as appropriate.
        modeAttributes.addAttribute(Mode.IMMEDIATE, "readonly", Ptype.LOG);

        modeAttributes.addAttribute(Mode.SELECTVALUE, "direct", Ptype.LOG);
        modeAttributes.addAttribute(Mode.SELECTVALUE, "domain", Ptype.LOG);
        modeAttributes.addAttribute(Mode.SELECTVALUE, "readonly", Ptype.LOG);
        modeAttributes.addAttribute(Mode.SELECTVALUE, "alu", Ptype.LOG);
        modeAttributes.addAttribute(Mode.SELECTVALUE, "dsp", Ptype.LOG);
        modeAttributes.addAttribute(Mode.SELECTVALUE, "threestate", Ptype.LOG);

        modeAttributes.addAttribute(Mode.VALUE, "domain", Ptype.LOG);
        modeAttributes.addAttribute(Mode.VALUE, "readonly", Ptype.LOG);
        
        modeAttributes.addAttribute(Mode.STATIC, "domain", Ptype.LOG);
        modeAttributes.addAttribute(Mode.STATIC, "continuous", Ptype.LOG);
        modeAttributes.addAttribute(Mode.STATIC, "readonly", Ptype.LOG);
        modeAttributes.addAttribute(Mode.STATIC, "iob", Ptype.LOG);
        modeAttributes.addAttribute(Mode.STATIC, "alu", Ptype.LOG);
        modeAttributes.addAttribute(Mode.STATIC, "dsp", Ptype.LOG);
        modeAttributes.addAttribute(Mode.STATIC, "loc", Ptype.STR); // checked explicitly for "str", "[]str"
        // attributes not in the map -
        // init attribute handled separately in Memory.setAttributes() because type is not fixed
        // ids attribute is read-only

        modeAttributes.addAttribute(Mode.QUEUE, "buffersize", Ptype.UINT);
        modeAttributes.addAttribute(Mode.QUEUE, "minbuffersize", Ptype.UINT);
        modeAttributes.addAttribute(Mode.QUEUE, "depth", Ptype.UINT);
        modeAttributes.addAttribute(Mode.QUEUE, "mindepth", Ptype.UINT);
        modeAttributes.addAttribute(Mode.QUEUE, "readdomain", Ptype.LOG);
        modeAttributes.addAttribute(Mode.QUEUE, "writedomain", Ptype.LOG);
        modeAttributes.addAttribute(Mode.QUEUE, "domain", Ptype.LOG);
        modeAttributes.addAttribute(Mode.QUEUE, "continuous", Ptype.LOG);
        modeAttributes.addAttribute(Mode.QUEUE, "readonly", Ptype.LOG);
        modeAttributes.addAttribute(Mode.QUEUE, "alu", Ptype.LOG);
        modeAttributes.addAttribute(Mode.QUEUE, "dsp", Ptype.LOG);
        modeAttributes.addAttribute(Mode.QUEUE, "queuefieldzero", Ptype.LOG);
        modeAttributes.addAttribute(Mode.QUEUE, "fifo", Ptype.LOG);
        modeAttributes.addAttribute(Mode.QUEUE, "queuereg", Ptype.LOG);
        modeAttributes.addAttribute(Mode.QUEUE, "ignoremodules", Ptype.LOG);

        modeAttributes.addAttribute(Mode.PRIORITY, "domain", Ptype.LOG);
        modeAttributes.addAttribute(Mode.PRIORITY, "readonly", Ptype.LOG);
        modeAttributes.addAttribute(Mode.PRIORITY, "multiplein", Ptype.LOG);

        modeAttributes.addAttribute(Mode.CMEMORY, "domain", Ptype.LOG);
        // attributes not in the map -
        // init attribute handled separately in Memory.setAttributes() because type is not fixed

        modeAttributes.addAttribute(Mode.RMEMORY, "continuous", Ptype.LOG);
        modeAttributes.addAttribute(Mode.RMEMORY, "domain", Ptype.LOG);
        modeAttributes.addAttribute(Mode.RMEMORY, "domain0", Ptype.LOG);
        modeAttributes.addAttribute(Mode.RMEMORY, "domain1", Ptype.LOG);
        modeAttributes.addAttribute(Mode.RMEMORY, "write_mode", Ptype.STR);
        modeAttributes.addAttribute(Mode.RMEMORY, "write_mode_a", Ptype.STR);
        modeAttributes.addAttribute(Mode.RMEMORY, "write_mode_b", Ptype.STR);
        // attributes not in the map -
        // init attribute handled separately in Memory.setAttributes() because type is not fixed

        modeAttributes.addAttribute(Mode.INPUT, "differential", Ptype.LOG);
        // attributes not in the map -
        // loc handled separately in checkIOAttribute()
        // ids attribute is read-only

        modeAttributes.addAttribute(Mode.OUTPUT, "direct", Ptype.LOG);
        modeAttributes.addAttribute(Mode.OUTPUT, "defaultout", Ptype.LOG);
        modeAttributes.addAttribute(Mode.OUTPUT, "noshadow", Ptype.LOG);
        modeAttributes.addAttribute(Mode.OUTPUT, "differential", Ptype.LOG);
        // attributes not in the map -
        // loc handled separately in checkIOAttribute()
        // ids attribute is read-only

        modeAttributes.addAttribute(Mode.CLOCK, "frequency", Ptype.FLOAT);
        modeAttributes.addAttribute(Mode.CLOCK, "dutycycle", Ptype.FLOAT);
        modeAttributes.addAttribute(Mode.CLOCK, "differential", Ptype.LOG);
        modeAttributes.addAttribute(Mode.CLOCK, "ibufg", Ptype.LOG);
        modeAttributes.addAttribute(Mode.CLOCK, "valid", Ptype.LOG);
        // attributes not in the map -
        // loc handled separately in checkIOAttribute()
        // clk_sinks attribute is read-only
        // clk_sourced attribute is read-only
        // clk_CE attribute is read-only
    }
    
    protected class eapair {
        private HashSet<TDEVar>         execs;
        private HashMap<TDEVar, AST>    asstypes;
        private boolean                 toplevel;
        
        public eapair () {
            execs = new HashSet<TDEVar>();
            asstypes = new HashMap<TDEVar, AST>();
        }
        
        public void put (TDEVar tdev, AST ast, Boolean toplevel) {
            execs.add(tdev);
            asstypes.put(tdev, ast);
            this.toplevel = toplevel;
        }
        
        public HashSet<TDEVar> getExecs () { return(execs); }

        public AST getAssType (TDEVar tdev) { return(asstypes.get(tdev)); }
        
        public boolean isTopLevel() { return(toplevel); }
    }

    /**
     * Construct a variable.
     * @param   ident is the variable identifier
     * @param   in_par is true if this is a module, procedure of function
     *          input parameter
     * @param   out_par is true if this is a module or procedure output
     *          parameter
     * @param   type is the variable type or null
     * @param   etype is the type to use if type is null (skip if NONE)
     * @param   loc is the source file location
     */
    public Var (
        Ident   ident,
        boolean in_par,
        boolean out_par,
        Type    type,
        Ptype   etype,
        SrcLoc  loc
    ) {
        decloc = loc;
        if (ident != null) {
            name = ident.getId();
            context = ident.getScopeContext();
            ename = scopeHierarchy(context, loc) + name;
        } else
            name = "";
        isInPar = in_par;
        isOutPar = out_par;
        attributes = new TreeMap<String, Val>();
        
        if (etype != Ptype.NONE) {
            if (type == null) 
                type = new Type(etype, 0, 0, 0, 0, null, null);
            else if (type.hasUndimArray())
                type.zeroUndimArray();
            /* else if (type.hasEnumType() && (ident.getScopeContext() != Context.DEFAULT))
                throw new ExEx("enum variable identifier cannot have / or ./", loc);*/

            this.type = type;
            wordspec = type.getWordSpec(this, loc);
        }
    }

    /**
     * Get the identifier String for this variable.
     * @param   t is the type of identifier report required
     * @return  the identifier string
     */
    public String getID (IDtype t) {
        Var v = this;
        
        switch (t) {
        case LITERAL:
            return(name);
        case SLITERAL:
            return(scope.getScopeString() + name);
        case REAL:
            if (indass)
                v = indirect;
            return(v.scope.getScopeString() + v.name);
        case CHAIN:
            String  s = "";
            do {
                s += v.scope.getScopeString() + v.name;
                v = v.indirect;
                if (v != null)
                    s += "->";
            } while (v != null);
            return(s);
        }
        return(name);
    }

    /**
     * Get the Ident class for this variable.
     * @return  the identifier scope context
     */
    public Ident getIdent () {
        return(new Ident(name, context));
    }

    /**
     * Get the identifier String for this variable. If it is indirect,
     * follow the pointer.
     * @return  the identifier string
     */
    public String getRealId () {
        if (indass)
            return(indirect.getRealId());
        return(name);
    }
    
    /**
     * Assign the scope within which this Var is contained.
     * @param scope is the scope
     */
    public void setScope (Scope scope) { this.scope = scope; }
    
    /**
     * Assign the scope within which this Var is contained.
     * @return the scope
     */
    public Scope getScope () { return(scope); }
 
    /**
     * Get the source file location for the declaration of this variable.
     * @return  the source file location
     */
    public SrcLoc getLoc () {
        return(decloc);
    }
  
    /**
     * Get the expanded name for this variable. This
     * is the identifier preceded by a "/"-separated list
     * of identifiers of any preceding modules, procedures or functions
     * within which the variable declaration is nested. Each module,
     * procedure or function identifier has an underscore and an integer
     * appended to render the variable instance unique.
     * @return  the expanded variable name
     */
    public String getEname () { return(ename); }
       
    /**
     * Get the mode of the variable.
     * @return  the mode
     */
    public Mode getMode () { return(mode); }

    /**
     * Get the variable Type.
     * @return  the variable Type
     */
    public Type getType () { return(type); }
    
    /**
     * Get the variable WordSpec.
     * @return  the variable WordSpec
     */
    public WordSpec getWordSpec () { return(wordspec); }

    /**
     * Get the map stored in val[i].
     * This method can only be called for subclass 'Immediate' where
     * the method here in super class 'Var' is overridden!
     * @param   i is the val array subscript
     * @return  the map
     */
    public TreeMap<String, Val> getMap (int i) {
        throw new ExEx("CODE ERROR - " + getClass().getName() + ".getMap(int) called");
    }
    
    /**
     * Compute the hash code for this variable.
     * Use the hash code of the extended name string as that is unique.
     * @return the hash code value.
     */
    public int hashCode() { return(ename.hashCode()); }
    
    /**
     * Equality check. Var instances are unique so simply
     * check for identity.
     * @return true if the variables are equal
     */
    public boolean equals(Object o) {
        if (!(o instanceof Var))
            return(false);
        Var   v = (Var)o;
        return(this == v);
    }

    /**
     * Get this priority variable's else signal.
     * This method can only be called for subclass 'Priority' where
     * the method here in super class 'Var' is overridden!
     * @param   loc is the source file location
     * @return  the priority variable else signal
     */
    public TDEVar getPriElse (SrcLoc loc) {
        throw new ExEx("CODE ERROR - " + getClass().getName() + ".getPriElse(SrcLoc) called", loc);
    }
    
    /**
     * Mark this variable as being used, i.e. the value of this
     * variable has been used somewhere.
     * This is used during variable creation to eliminate from the intermediate
     * code list variables whose outputs have not been used. This is not essential
     * because logic branches whose leaves have no output connections are pruned during
     * netlist generation. It is nevertheless implemented to shorten the intermediate code
     * list to make it more readable and to slightly shorten code generation time (probably
     * not significant). Of necessity it is conservative so that it does not mark
     * valid variables while occasionally failing to mark unused variables.
     */
    public void setUsed () {
        if (indass) {
            indirect.setUsed();
            return;
        }
        used = true;
    }
    
    /**
     * Get the used flag.
     * @return  the used flag
     */
    public boolean getUsed () { return(used); }
    
    /**
     * Mark this variable as read-only.
     * This can be set via attributes.
     * The read_only field is also used for state machine logical variables
     * and for Petri net variables as these are not allowed to be be
     * assigned explicitly by user code. It is also used to enforce single
     * assignments to value variables which are output arguments to inbuilt
     * procedures pulse() and queue().
     * @param   loc is the source file location
     */
    public void setReadOnly (SrcLoc loc) {
        readonly = true;
        attributes.put("readonly", new Val(true, loc));
    }
    
    /**
     * Determine if this variable is an input parameter to a
     * module, procedure or function.
     * @return  true if this is an input parameter
     */
    public boolean isInputPar () { return(isInPar); }
    
    /**
     * Determine if this variable is an output parameter to a
     * module or procedure.
     * @return  true if this is an output parameter
     */
    public boolean isOutputPar () { return(isOutPar); }
    
    /**
     * Determine if a variable has been assigned.
     * @return  true if the variable has been assigned
     */
    public boolean isAssigned () {
        if (indass)
            return(indirect.isAssigned());
        return(assigned);
    }
    
    /**
     * Determine if a variable is indirect, i.e.
     * is a pointer to the actual variable.
     * @return  true if the variable is indirect
     */
    public boolean isIndirect () { return(indass); }
    
    /**
     * Set a variable as having been assigned.
     */
    public void setAssigned () {
        if (indass)
            indirect.setAssigned();
        else
            assigned = true;
    }
    
    /**
     * Get the direct assignment flag. Called here in Var for classes which
     * do not use this flag. Always return false.
     * @return  false
     */
    public boolean getDirect () {
        return(false);
    }
    
    /**
     * Get the continuous assignment flag. Called here in Var for classes which
     * do not use this flag. Always return false.
     * @return  false
     */
    public boolean getContinuous () {
        return(false);
    }

    /**
     * Add a reset signal for a word or words.
     * This method can only be called for subclass 'Static' where
     * the method here in super class 'Var' is overridden!
     * @param   r is the reset signal
     * @param   ws is the wordspec specifying the words to be reset
     * @param   loc is the source file location
     */
    public void addReset (TDEVar r, WordSpec ws, SrcLoc loc) {
        throw new ExEx("CODE ERROR - " + getClass().getName() + ".addReset(TDEVar,WordSpec,SrcLoc) called", loc);
    }
    
    /**
     * Add a reset signal for a word.
     * This method can only be called for subclass 'Static' where
     * the method here in super class 'Var' is overridden!
     * @param   r is the reset signal
     * @param   i is the index of the word to be reset
     * @param   loc is the source file location
     */
    public void addReset (TDEVar r, int i, SrcLoc loc) {
        throw new ExEx("CODE ERROR - " + getClass().getName() + ".addReset(TDEVar,int,SrcLoc) called", loc);
    }
    
    /**
     * Add a reset signal for a queue.
     * Only the first member of the resets array of signal sets
     * is used.
     * This method can only be called for subclass 'Queue' where
     * the method here in super class 'Var' is overridden!
     * @param   r is the reset signal
     * @param   loc is the source file location
     */
    public void addReset (TDEVar r, SrcLoc loc) {
        throw new ExEx("CODE ERROR - " + getClass().getName() + ".addReset(TDEVar,SrcLoc) called", loc);
    }
    
    /**
     * Get the output reset signal for a queue.
     * This method can only be called for subclass 'Queue' where
     * the method here in super class 'Var' is overridden!
     * @param   loc is the source file location
     * @return  the output reset signal
     */
    public TDEVar getRReset (SrcLoc loc) {
        throw new ExEx("CODE ERROR - " + getClass().getName() + ".getRReset(SrcLoc) called", loc);
    }

    /**
     * Set the attributes map.
     * This method can be called for most subclasses where
     * the method here in super class 'Var' is overridden!
     * @param   val is the attributes map value
     * @param   loc is the source file location
     */
    public void setAttributes (Val val, SrcLoc loc) {
        throw new ExEx("CODE ERROR - " + getClass().getName() + ".setAttributes(Val,SrcLoc) called", loc);
    }
    
    /**
     * Add an attribute to the mode attributes map.
     * This is used to add attributes used for constraint generation.
     * These are not inbuilt as they are not used by 3PL itself but are used by library
     * software when generating constraints. Adding them to the mode attributes map allows them to
     * be checked along with the 3P internal attributes when being set for a variable.
     * @param mode is the variable mode
     * @param key is the attribute name
     * @param ptype is the primitive type of the attribute value
     */
    public static void addModeAttribute(Mode mode, String key, Ptype ptype) {
        modeAttributes.addAttribute(mode, key, ptype);
    }
    
    /**
     * Check that attribute is known to 3PL
     * itself or to the code generator for the FPGA family.
     * If it checks out OK, return true.
     * If it does not check out issue a warning and return false.
     * @param   mkey is the attribute key
     * @param   mval is the attribute value
     * @param   mode is the variable mode
     * @param   loc is the source file location
    * @return  true if check OK
     */
    @SuppressWarnings("incomplete-switch")
    public boolean checkAttribute(
        String          mkey,
        Val             mval,
        Mode            mode,
        SrcLoc          loc
    ) {
        switch (modeAttributes.checkAttribute(mkey, mval, mode)) {
        case WRONG_TYPE:
            // Attribute is internal to 3PL but is wrong type!
            msg(loc.toString());
            msg(mode.modename() + " variable " + name + " - '" + mkey +
                "' attribute value is not type " +
                    modeAttributes.getAttributePrimType(mode, mkey) +
                    " - ignored");
            return(false);
        case UNRECOGNISED:
            // Attribute NOT internal to 3PL - see if known to code generator.
            msg(loc.toString());
            msg(mode.modename() + " variable " + name + " - '" + mkey +
                "' attribute is not known - ignored");
            return(false);
        }
        return(true);
    }
    
    /**
     * Check that attribute for input, output or clock mode is known to 3PL
     * itself or to the code generator for the FPGA family.
     * If it checks out OK, return true.
     * If it does not check out issue a warning and return false.
     * @param   mkey is the attribute key
     * @param   mval is the attribute value
     * @param   mode is the variable mode
     * @param   loc is the source file location
     * @return  the result of the check
     */
    @SuppressWarnings("incomplete-switch")
    public ACT checkIOAttribute(
        String          mkey,
        Val             mval,
        Mode            mode,
        SrcLoc          loc
    ) {
        // Treat loc attribute as special case.
        // Can be any type as long as members are all type "str".
        // Construct a 1-dimensional string array and add it to the
        // map then return DONE.
        if (mkey.equals("loc")) {
            if (mval.isTarget())
                throw new ExEx("Attribute \"loc\" for " + mode.name().toLowerCase() + " mode variable '" + name + "' is not immediate mode", loc);  
            Type[]  types = mval.getValTypes();
            if (types == null)
                throw new ExEx("Attribute \"loc\" for " + mode.name().toLowerCase() + " mode variable '" + name + "' is null", loc);  
            String[]    locs = new String[types.length];
            for (int i=0 ; i<types.length ; i++) {
                if (types[i].getPrimType() != Ptype.STR)
                    throw new ExEx("Attribute \"loc\" for " + mode.name().toLowerCase() + " mode variable '" + name + "' contains types other than str", loc);  
                locs[i] = (String)mval.getVal(i);
            }
            Val nval = new Val(locs, loc);
            attributes.put("loc", nval);
            return(ACT.DONE);
        }
        
        // Check other attributes.
        switch (modeAttributes.checkAttribute(mkey, mval, mode)) {
        case WRONG_TYPE:
            // Attribute is internal to 3PL but is wrong type!
            throw new ExEx(mode.modename().toLowerCase() + " variable " + name + " - '" + mkey +
                "' attribute value is not type " +
                    modeAttributes.getAttributePrimType(mode, mkey), loc);
        case UNRECOGNISED:
            // Attribute NOT internal to 3PL - may be used by constraint generating library software.
            return(ACT.UNRECOGNISED);
        }
        return(ACT.OK);
    }
    
    /**
     * Get the attributes map.
     * @return  the attributes map
     */
    public TreeMap<String,Val> getAttributes() {
        if (indirect != null)
            return(indirect.getAttributes());
        return(attributes);
    }

    /**
     * Get the attributes map value.
     * This method can be called for most subclasses where
     * the method here in super class 'Var' is overridden!
     * @param   extended indicates extra attributes are to be temporarily created for additional state information
     * @param   loc is the source file location
     * @return  the attributes map value
     */
    public Val getAttributes (boolean extended, SrcLoc loc) {
        throw new ExEx("CODE ERROR - " + getClass().getName() + ".getAttributes(boolean,SrcLoc) called", loc);
    }
    
    /**
     * Get a boolean attribute array from the attributes map.
     * If the attribute is a single value then return a boolean array of dimension 1.
     * @param   dname is the attribute name
     * @param   pins is the number of PAD locations in this output mode variable
     * @return  the attribute array
     */
    public Boolean[] getAttribute (String dname, int pins) {
        if (indirect != null)
            return(indirect.getAttribute(dname, pins));
        
        Boolean[]   b = null;
        if ((attributes != null) && attributes.containsKey(dname)) {
            Val     a = attributes.get(dname);
            Type    atype = a.getType();
            Type    aatype = atype.getArrayType();
            if (aatype != null) {
                if (aatype.getPrimType() == Ptype.LOG) {
                    if (atype.getArrayDim() != pins)
                        throw new ExEx("output() - '" + name + "' " + dname + " attribute dimension incorrect", decloc);
                    b = (Boolean[])a.getVals();
                } else
                    throw new ExEx("output() - '" + name + "' " + dname + " attribute is not log or []log", decloc);
            } else {
                if (atype.getPrimType() == Ptype.LOG) {
                    b = new Boolean[1];
                    b[0] = a.getSingleLval(decloc);
                } else
                    throw new ExEx("output() - '" + name + "' " + dname + " attribute is not log or []log", decloc);
            }
        }
        return(b);
    }

    /**
     * Set the parameter/argument indirect fields for this parameter variable.
     * Field <b>Var indirect</b> is the argument variable and field
     * <b>SubFieldList ind_sfl</b> is a list of any subscripts or fields on the
     * argument variable reference.
     * This method can be called for most subclasses where
     * the method here in super class 'Var' is overridden but the overriding
     * method calls this super method.
     * @param   arg is the argument reference to which this parameter is
     *          to point
     * @param   loc is the source file location of the parameter declaration
     */
    public void setIndirect (RefOrVal arg, SrcLoc loc) {
        if (arg == null) {
            // Not sure why null argument would ever be allowed!
            throw new ExEx("Variable set indirect with null subject", loc);
            //indass = true;
            //return; // no argument
        }
            
        Ref     var_ref = getRef(null, new NodeList(null), loc);
        Var     v = arg.getVar();
        
        if (v == this)
            return; // self assignment - nothing to do
        if ((mode != Mode.CMEMORY) && (mode != Mode.RMEMORY))
                var_ref.checkMatch(arg, true, "Parameter '" + name +
                                    "' type mismatch with argument '" +
                                    arg.getVar().getID(IDtype.LITERAL) + "'\n", loc);

        if (mode != arg.getMode())
            throw new ExEx("Parameter '" + name +
                                    "' mode mismatch with argument '" +
                                    arg.getVar().getID(IDtype.LITERAL) + "'\n modes queue " +
                                    arg.getMode().modename(), loc);
        
        indirect = v;
        indass = true;
     }
    
    /**
     * Mark a parameter that has been passed an argument.
     */
    public void setMatched () { matched = true; }
    
    /**
     * Determine if a parameter has been passed an argument.
     * @return  true if the parameter has been passed an argument
     */
    public boolean isMatched () { return(matched); }
    
    /**
     * Check for a type match of a reference or value against the full variable.
     * @param   rov is the reference or value
     * @param   strict is true for exact type matching, i.e. same field widths
     * @param   mess is a string to be included in an error message
     * @param   loc is the source file location
     */
    public void  checkMatch (RefOrVal rov, boolean strict, String mess, SrcLoc loc) {
        if (indass) {
            indirect.checkMatch(rov, strict, mess, loc);
            return;
        }
        wordspec.checkMatch(rov, strict, mess, loc);
    }
    
    /**
     * Get the clock signal from a clock variable.
     * This method is only applicable in class Clock and overrides the method
     * here in class Var. It causes a fatal error if called for any other
     * class.
     * @return the clock signal
     */
    public TDEVar getClkSig () {
        throw new ExEx("CODE ERROR - " + getClass().getName() + ".getClkSig() called");
    }
    
    /**
     * Set the (maximum) clock frequency for a clock variable.
     * This method is only applicable in class Clock and overrides the method
     * here in class Var. It causes a fatal error if called for any other
     * class.
     * @param   f is the clock frequency
     * @param   loc is the source file location
     */
    public void setClkFreq (double f, SrcLoc loc) {
        throw new ExEx("CODE ERROR - " + getClass().getName() + ".setClkFreq(double,SrcLoc) called", loc);
    }
    
    /**
     * Set the (minimum) clock period for a clock variable.
     * This method is only applicable in class Clock and overrides the method
     * here in class Var. It causes a fatal error if called for any other
     * class.
     * @param   p is the clock period
     * @param   loc is the source file location
     */
     public void setClkPeriod (double p, SrcLoc loc) {
         throw new ExEx("CODE ERROR - " + getClass().getName() + ".setClkPeriod(double,SrcLoc) called", loc);
     }
     
     /**
     * Set the minimum clock frequency for a clock variable.
     * This method is only applicable in class Clock and overrides the method
     * here in class Var. It causes a fatal error if called for any other
     * class.
     * @param   f is the minimum clock frequency
     * @param   loc is the source file location
     */
    public void setClkMinFreq (double f, SrcLoc loc) {
        throw new ExEx("CODE ERROR - " + getClass().getName() + ".setClkMinFreq(double,SrcLoc) called", loc);
    }
    
    /**
     * Set the maximum clock period for a clock variable.
     * This method is only applicable in class Clock and overrides the method
     * here in class Var. It causes a fatal error if called for any other
     * class.
     * @param   p is the maximum clock period
     * @param   loc is the source file location
     */
    public void setClkMaxPeriod (double p, SrcLoc loc) {
        throw new ExEx("CODE ERROR - " + getClass().getName() + ".setClkMaxPeriod(double,SrcLoc) called", loc);
    }
    
    /**
     * Set the duty cycle for a clock variable.
     * This method is only applicable in class Clock and overrides the method
     * here in class Var. It causes a fatal error if called for any other
     * class.
     * @param   dc is the duty cycle
     * @param   loc is the source file location
     */
    public void setClkDutyCycle (double dc, SrcLoc loc) {
        throw new ExEx("CODE ERROR - " + getClass().getName() + ".setClkDutyCycle(double,SrcLoc) called", loc);
    }
    
    /**
     * Get the (maximum) clock frequency for a clock variable.
     * This method is only applicable in class Clock and overrides the method
     * here in class Var. It causes a fatal error if called for any other
     * class.
     * @param   loc is the source file location
     * @return the clock frequency
     */
    public double getClkFreq (SrcLoc loc) {
        throw new ExEx("CODE ERROR - " + getClass().getName() + ".getClkFreq() called");
    }
    
    /**
     * Get the (minimum) clock period for a clock variable.
     * This method is only applicable in class Clock and overrides the method
     * here in class Var. It causes a fatal error if called for any other
     * class.
     * @param   loc is the source file location
     * @return the clock period
     */
    public double getClkPeriod (SrcLoc loc) {
        throw new ExEx("CODE ERROR - " + getClass().getName() + ".getClkPeriod() called");
    }
    
    /**
     * Get the duty cycle for a clock variable.
     * This method is only applicable in class Clock and overrides the method
     * here in class Var. It causes a fatal error if called for any other
     * class.
     * @param   loc is the source file location
     * @return the duty cycle
     */
    public double getClkDutyCycle (SrcLoc loc) {
        throw new ExEx("CODE ERROR - " + getClass().getName() + ".getClkDutyCycle() called");
    }
    /**
     * Set the input clock. This will set both input and output clocks.
     * This method can be called for most subclasses where
     * the method here in super class 'Var' is overridden!
     * @param   clk is the clock domain variable
     * @param   e is the event setting the clock
     * @param   loc is the source file location
     */
    public void setInputClock (Clock clk, Calloc e, SrcLoc loc) {
        throw new ExEx("CODE ERROR - " + getClass().getName() + ".setInputClock(Clock,Calloc,SrcLoc) called", loc);
    }

    /**
     * Set the output clock. This will set both input and output clocks.
     * This method can be called for most subclasses where
     * the method here in super class 'Var' is overridden!
     * Class Input, Output and Clock do not have clock domains so the method here is used
     * which does nothing except conditionally set the used boolean.
     * @param   clk is the clock domain variable
     * @param   e is the event setting the clock
     * @param   used if true sets the used field of the variable
     * @param   loc is the source file location
     */
    public void setOutputClock (Clock clk, Calloc e, boolean used, SrcLoc loc) {
        //throw new ExEx("CODE ERROR - " + getClass().getName() + ".setOutputClock(Clock,Calloc,boolean,SrcLoc) called", loc);
        if (used)
            this.used = true;
    }
    
    public Clock getOutputClkVar (Calloc calloc, SrcLoc loc) {
        //throw new ExEx("CODE ERROR - " + getClass().getName() + ".getReadClkVar(Calloc,SrcLoc) called", loc);
        return(null);
    }
    
    public Clock getOutputClkVar (SrcLoc loc) {
        //throw new ExEx("CODE ERROR - " + getClass().getName() + ".getReadClkVar(SrcLoc) called", loc);
        return(null);
    }
    
    /**
     * Set the negative clock associated with this clock.
     * This method is only applicable in class Clock and overrides the method
     * here in class Var. It causes a fatal error if called for any other
     * class.
     * @param   v is the negative clock variable
     */
    public void setAssociatedClock (Clock v) {
        throw new ExEx("CODE ERROR - " + getClass().getName() + ".setAssociatedClock(Clock) called");
    }
    
    /**
     * Get the execution start signal for the clock signal associated
     * with a variable.
     * This method is only applicable in class Clock and overrides the method
     * here in class Var. It causes a fatal error if called for any other
     * class.
     * @param   loc is the source file location
     * @return the start signal for the clock associated with the variable
     */
    public TDEVar getStartSig (SrcLoc loc) {
        throw new ExEx("CODE ERROR - " + getClass().getName() + ".getStartSig(SrcLoc) called", loc);
    }
    
    /**
     * Get a member of the val[] array.
     * @param   i is the array index
     * @return  the object at val[i]
     */
    public Object getVal (int i) {
        return(val[i]);
    }

    /**
     * Get the value of a variable. The value includes
     * all the information provided by a variable reference
     * (see getRef())
     * plus For immediate mode type an ArrayList of values.
     * For target mode (value, static or queue), it returns the TDEVar.
     * The <B>varnode</B> argument is used to get any flags
     * associated with the variable occurrence, but it may be null if the
     * reference is being requested in some other context.
     * The <B>subs</B> argument is a list of subscript or field
     * nodes associated with the variable occurrence.
     * A pointer variable returns a reference to the ultimate variable
     * to which it points.
     * @param   varnode is the code tree node, may be null (used to
     *          detect variable node flags)
     * @param   subs is the list of associated subscript and field nodes
     * @param   loc is the source file location
     * @return  the value or values extracted from the variable
     */
    public Val getVal (VarNode varnode, NodeList subs, SrcLoc loc) {
        if ((type != null) && (type.getPrimType() == Ptype.EMPTY))
            throw new ExEx("cannot get the value from type \"" + type.getPrimType().name() + "\"", loc);

        SubFieldList    sfl = new SubFieldList(subs, loc);
        return(getVal(varnode, sfl, loc));
    }

    /**
     * Get the value of a variable. The value includes
     * all the information provided by a variable reference
     * (see getRef())
     * plus For immediate mode type an ArrayList of values.
     * For target mode (value, static or queue), it returns the TDEVar.
     * The <B>varnode</B> argument is used to get any flags
     * associated with the variable occurrence, but it may be null if the
     * reference is being requested in some other context.
     * The <B>sfl</B> argument is a list of subscript or field
     * nodes associated with the variable occurrence.
     * A pointer variable returns a reference to the ultimate variable
     * to which it points.
     * @param   varnode is the code tree node, may be null (used to
     *          detect variable node flags)
     * @param   sfl is the list of associated subscript and field nodes
     * @param   loc is the source file location
     * @return  the value or values extracted from the variable
     */
    public Val getVal (VarNode varnode, SubFieldList sfl, SrcLoc loc) throws MapException {
        if ((type != null) && (type.getPrimType() == Ptype.EMPTY))
            throw new ExEx("cannot get the value from type \"empty\"", loc);
        if ((mode != Mode.CMEMORY) && (mode != Mode.RMEMORY) && (type == null))
            throw new ExEx("cannot get value of typeless variable '" +
                                                            name + "'", loc);
        Ref ref;
        try {
            ref = getRef(varnode, sfl, true, loc);
        } catch (MapException me) {
            return(new Val(loc));   // null
        }
        Var var = ref.getVar();
        if (var == null)// is CMEMORY or RMEMORY mode
            return(getVal(ref, loc));
        return(var.getVal(ref, loc));
    }

    /**
     * Get the value of a variable. The value includes
     * all the information provided by a variable reference
     * (see getRef())
     * plus For immediate mode type an ArrayList of values.
     * For target mode (value, static or queue), it returns the TDEVar.
     * The <B>varnode</B> argument is used to get any flags
     * associated with the variable occurrence, but it may be null if the
     * reference is being requested in some other context.
     * The <B>sfl</B> argument is a list of subscript or field
     * nodes associated with the variable occurrence.
     * A pointer variable returns a reference to the ultimate variable
     * to which it points.
     * This method can be called for most subclasses where
     * the method here in super class 'Var' is overridden!
     * @param   sfl is the list of associated subscript and field nodes
     * @param   flag is any ++, -- or * flag from a VarNode
     * @param   loc is the source file location
     * @return  the value or values extracted from the variable
     */
    public Val getVal (SubFieldList sfl, Flag flag, SrcLoc loc) {
        if ((type != null) && (type.getPrimType() == Ptype.EMPTY))
            throw new ExEx("cannot get the value from type \"empty\"", loc);
        if ((mode != Mode.CMEMORY) && (mode != Mode.RMEMORY) && (type == null))
            throw new ExEx("cannot get value of typeless variable '" +
                                                            name + "'", loc);
        Ref ref = getRef(null, sfl, true, loc);
        ref.setFlag(flag);
        return(getVal(ref, loc));
    }

    /**
     * Get the value of a variable. The value includes all the
     * information provided by a variable reference (see getRef()) plus
     * For immediate mode type an ArrayList of values. For target mode
     * (value, static or queue), it returns the TDEVar. The
     * <B>varnode</B> argument is used to get any flags associated with
     * the variable occurrence, but it may be null if the reference is
     * being requested in some other context.
     * @param   ref is the reference to this variable
     * @param   loc is the source file location
     * @return  the value or values extracted from the variable
     */
    public Val getVal (Ref ref, SrcLoc loc) {
        throw new ExEx("cannot evaluate a " + getClass().getName() + " mode variable", loc);
    }

    /**
     * Get a reference to a variable. This reference is only called when
     * processing an input argument variable. Since it is a RHS reference it
     * can set is_val true when calling getRef. This is only done for
     * immediate mode so that when  handling immediate map or list types
     * trailing field or subscripts are accepted, something which cannot be
     * done on a LHS variable.
     * The main information contained in the reference is -
     * <UL>
     * <LI> a list of words in the variable selected by subscripts or
     *      field names
     * <LI> a list of primitive types for each word
     * <LI> for target variables a list of bit pairs associated with
     *      the word list
     * <LI> a multi-dimensioned array giving the dimensionality of
     *      the reference
     * <LI> a type to make sure assignment types match
     * <LI> a TDEVar for a target variable
     * </UL>
     * The <B>varnode</B> argument is used to get any flags
     * associated with the variable occurrence (++ or -- flags
     * of the string indirect operator/flag), but it may be null if the
     * reference is being requested in some other context.
     * The <B>subs</B> argument is a list of subscript or field
     * nodes associated with the variable occurrence - it may be null.
     * A pointer variable returns a reference to the ultimate variable
     * to which it points.
     * A target reference includes a TDEVar signal for the reference.
     * @param   varnode is the tree node for the variable occurrence or
     *          is null if the reference is not associated with a variable
     *          occurrence (used to detect ++, -- or ?)
     * @param   subs is the list of associated subscript and field nodes or null
     * @param   loc is the source file location
     * @return  the reference to the variable
     */
    public Ref getInRef (VarNode varnode, NodeList subs, SrcLoc loc) {
        if ((type != null) && (type.getPrimType() == Ptype.EMPTY))
            throw new ExEx("cannot get the reference for type \"empty\"", loc);

        SubFieldList    sfl = new SubFieldList(subs, loc); // subscript values
        return(getRef(varnode, sfl, mode==Mode.IMMEDIATE, loc));
    }

    /**
     * Get a reference to a variable. The reference is generally
     * used for assignment to a variable however the value of a variable
     * can also obtained by starting with the reference. The main
     * information contained in the reference is -
     * <UL>
     * <LI> a list of words in the variable selected by subscripts or
     *      field names
     * <LI> a list of primitive types for each word
     * <LI> for target variables a list of bit pairs associated with
     *      the word list
     * <LI> a multi-dimensioned array giving the dimensionality of
     *      the reference
     * <LI> a type to make sure assignment types match
     * <LI> a TDEVar for a target variable
     * </UL>
     * The <B>varnode</B> argument is used to get any flags
     * associated with the variable occurrence (++ or -- flags
     * of the string indirect operator/flag), but it may be null if the
     * reference is being requested in some other context.
     * The <B>subs</B> argument is a list of subscript or field
     * nodes associated with the variable occurrence - it may be null.
     * A pointer variable returns a reference to the ultimate variable
     * to which it points.
     * A target reference includes a TDEVar signal for the reference.
     * @param   varnode is the tree node for the variable occurrence or
     *          is null if the reference is not associated with a variable
     *          occurrence (used to detect ++, -- or ?)
     * @param   subs is the list of associated subscript and field nodes or null
     * @param   loc is the source file location
     * @return  the reference to the variable
     */
    public Ref getRef (VarNode varnode, NodeList subs, SrcLoc loc) {
        SubFieldList    sfl = new SubFieldList(subs, loc); // subscript values
        return(getRef(varnode, sfl, false, loc));
    }

    /**
     * Get a reference to a variable. The reference is generally
     * used for assignment to a variable however the value of a variable
     * can also obtained by starting with the reference. The main
     * information contained in the reference is -
     * <UL>
     * <LI> a list of words in the variable selected by subscripts or
     *      field names
     * <LI> a list of primitive types for each word
     * <LI> for target variables a list of bit pairs associated with
     *      the word list
     * <LI> a multi-dimensioned array giving the dimensionality of
     *      the reference
     * <LI> a type to make sure assignment types match
     * <LI> a TDEVar for a target variable
     * </UL>
     * The <B>varnode</B> argument is used to get any flags
     * associated with the variable occurrence (++ or -- flags
     * of the string indirect operator/flag), but it may be null if the
     * reference is being requested in some other context.
     * The <B>subs</B> argument is a list of subscript or field
     * nodes associated with the variable occurrence - it may be null.
     * Reference includes a TDEVar signal for the reference.
     * This method can be called for most subclasses where
     * the method here in super class 'Var' is overridden!
     * @param   varnode is the tree node for the variable occurrence or
     *          is null if the reference is not associated with a variable
     *          occurrence (used to detect ++, -- or ?)
     * @param   sfl is the list of associated subscript and field nodes
     *          after the subscript and field nodes have been evaluated
     * @param   is_val indicates that we want a value rather than a reference
     * @param   loc is the source file location
     * @return  the reference to the variable
     */
    public Ref getRef (
        VarNode         varnode,
        SubFieldList    sfl,
        boolean         is_val,
        SrcLoc          loc
    ) {
        throw new ExEx("SYSTEM ERROR - Var.getRef() not overridden by subclass");
    }

    public Ref getRef(
        WordSpec        mvalws,
        SubFieldList    sfl,
        boolean         is_val,
        Flag            flag,
        SrcLoc          loc
    ) {
        throw new ExEx("SYSTEM ERROR - Var.getRef() not overridden by subclass");
    }

    /**
     * Assignment to this variable.
     * This method can be called for subclass 'Immediate' where
     * the method here in super class 'Var' is overridden!
     * @param   asstype  is the immediate assignment operator type.
     * @param   lref is the left variable reference.
     * @param   rval is the RHS value to be assigned.
     * @param   loc is a source file location for error messages
     */
    public void assignTo (
        AST     asstype,
        Ref     lref,
        Val     rval,
        SrcLoc  loc
    ) {
        throw new ExEx("SYSTEM ERROR - Var.assignTo() not overridden by subclass");
    }

    /**
     * Assignment to this variable.
     * This method can be called for subclass 'Value', 'Priority' where
     * the method here in super class 'Var' is overridden!
     * @param   lref is the left variable reference.
     * @param   rval is the RHS value to be assigned.
     * @param   loc is a source file location for error messages
     */
    public void assignTo (Ref lref, Val rval, SrcLoc loc) {
        throw new ExEx("SYSTEM ERROR - Var.assignTo() not overridden by subclass");
    }

    /**
     * Assignment to this variable.
     * This method can be called for subclass 'Output' where the method
     * here in super class 'Var' is overridden!
     * @param   ref is the left variable reference.
     * @param   rval is the RHS expression value.
     * @param   exec is the execution signal
     * @param   loc is a source file location for error messages
     */
    public void assignTo (Ref ref, Val rval, TDEVar exec, SrcLoc loc) {
        throw new ExEx("SYSTEM ERROR - Var.assignTo() not overridden by subclass");
    }

    /**
     * Assignment to this variable.
     * This method can be called for subclasses 'SelectValue', 'Static',
     * 'Queue' or 'Output' where the method here in super class 'Var' is overridden!
     * @param   ref is the left variable reference.
     * @param   rval is the RHS expression value.
     * @param   exec is the execution signal
     * @param   asstype is the assignment type
     * @param   toplevel is true if this is the top level in a module
     * @param   loc is a source file location for error messages
     */
    public void assignTo (
        Ref     ref,
        Val     rval,
        TDEVar  exec,
        AST     asstype,
        boolean toplevel,
        SrcLoc  loc
    ) {
        throw new ExEx("SYSTEM ERROR - Var.assignTo() not overridden by subclass");
    }

    /**
     * Create a variable, i.e. output code to the TDE list.
     * This method can be called for all subclasses where
     * the method here in super class 'Var' is overridden!
     */
    public void createVar () {
        throw new ExEx("SYSTEM ERROR - Var.createVar() not overridden by subclass");
    }

    /**
     * TDE default value for SELECTVALUE and initialisation data for STATIC
     * and QUEUE modes.
     * @param   targ_init is the default/initialisation value
     * @param   words is the total number of words in the variable
     * @param   word0 is the index of the first word to be initialised
     * @param   wordn is the index of the last word to be initialised
     * @return  the binary default/initialisation string
     */
    @SuppressWarnings("incomplete-switch")
    public String initialise (Val targ_init, int words, int word0, int wordn) {
        if (targ_init == null)
            return(zeropad(wordspec.numBits()));
        
        if ((mode == Mode.STATIC) && attributes.containsKey("DSP"))
            throw new ExEx("variable '" + name +
                "': initialisation not allowed with 'DSP' attribute",
                decloc);
        
        StringBuffer    b = new StringBuffer();
        int             valwords = targ_init.numWords();
        int             width;
        int             fixoffset;
        Ptype           prim_type;
        Type            init_type;
        String          s = null;
        int             bits;
        long            int_val;
        double          d;
        Object          o = null;
        Boolean         Bool_val;

        if (valwords > words)
            throw new ExEx("variable '" + name +
                        "': too many initialisation values", decloc);

        for (int i=word0 ; i<=wordn ; i++) {
            TDEVar  tdev = null;
            
            width = wordspec.getWidth(i);
            prim_type = val_type[i].getPrimType();
            if ((i >= valwords) || (targ_init.getVal(i) == null)) {
                if (prim_type == Ptype.ENUM)
                    s = Long.toBinaryString(val_type[i].firstEnumOrd());
                else
                    s = zeropad(width);
            } else {
                o = targ_init.getVal(i);
                if (o instanceof TDEVar) {
                    tdev = (TDEVar)o;
                    if (tdev.getType() == TDEVtype.VAR)
                        throw new ExEx("variable '" + name +
                                "': initialisation value is non-constant target variable", decloc);
                }
                init_type = targ_init.getValType(i);
                switch (prim_type) {
                case UINT:
                case INT:
                case BITS:
                    if ((init_type.getPrimType() != Ptype.UINT) &&
                        (init_type.getPrimType() != Ptype.INT) &&
                        (init_type.getPrimType() != Ptype.BITS))
                        throw new ExEx("variable '" + name +
                                    "': initialisation value wrong type", decloc);
                    if (tdev != null)
                        int_val = ((Long)tdev.getConst()).longValue();
                    else
                        int_val = ((Long)targ_init.getVal(i)).longValue();
                    if ((prim_type == Ptype.UINT) && (int_val < 0))
                        throw new ExEx("variable '" + name +
                                    "': -ve value for uint", decloc);
                    s = Long.toBinaryString(int_val);
                    bits = Functions.bits(int_val, prim_type==Ptype.INT);
                    if (bits > width)
                        throw new ExEx("variable '" + name +
                            "': initialisation value too large", decloc);
                    if (s.length() > width)
                        // -ve - truncate extra sign bits from front
                        s = s.substring(s.length() - width);
                    else if (s.length() < width)
                        // +ve - add extra 0 bits at front
                        s = zeropad(width - s.length()) + s;
                    break;
                case UFIXED:
                case FIXED:
                    if ((init_type.getPrimType() != Ptype.UINT) &&
                        (init_type.getPrimType() != Ptype.INT) &&
                        (init_type.getPrimType() != Ptype.FLOAT))
                        throw new ExEx("variable '" + name +
                                    "': initialisation value wrong type", decloc);
                    fixoffset = wordspec.getFixOffset(i);
                    if (tdev != null) {
                        d = ((Double)tdev.getConst()).doubleValue();
                        int_val = Math.round(d * ((long)1 << fixoffset));
                    } else if (o instanceof Double) {
                        d = ((Double)o).doubleValue();
                        int_val = Math.round(d * ((long)1 << fixoffset));
                    } else
                        int_val = ((Long)targ_init.getVal(i)).longValue() << fixoffset;
                    if ((prim_type == Ptype.UFIXED) && (int_val < 0))
                        throw new ExEx("variable '" + name +
                                    "': -ve value for ufixed", decloc);
                    s = Long.toBinaryString(int_val);
                    bits = Functions.bits(int_val, prim_type==Ptype.INT);
                    if (bits > width)
                        throw new ExEx("variable '" + name +
                            "': initialisation value too large", decloc);
                    if (s.length() > width)
                        // -ve - truncate extra sign bits from front
                        s = s.substring(s.length() - width);
                    else if (s.length() < width)
                        // +ve - add extra 0 bits at front
                        s = zeropad(width - s.length()) + s;
                    break;
                case ENUM:
                    if (init_type.getPrimType() != Ptype.ENUM)
                        throw new ExEx("variable '" + name +
                                    "': initialisation value wrong type", decloc);
                    if (tdev != null)
                        int_val = ((Long)tdev.getConst()).longValue();
                    else
                        int_val = ((Long)targ_init.getVal(i)).longValue();
                    s = Long.toBinaryString(int_val);
                    if (s.length() < width)
                        // add extra 0 bits at front
                        s = zeropad(width - s.length()) + s;
                    break;
                case FLOAT:
                    int     mant = wordspec.getMantissaWidth(i);
                    int     bexp = wordspec.getExponentWidth(i);
                    Object  fo = targ_init.getVal(i);
                    if (tdev != null)
                        d = ((Double)tdev.getConst()).doubleValue();
                    else if (fo instanceof Long)
                        d = ((Long)fo).doubleValue();
                    else
                        d = ((Double)fo).doubleValue();
                    int_val = longFromDouble(d, mant, bexp);
                    s = Long.toBinaryString(int_val);
                    break;
                case LOG:
                    if (!init_type.isEqual(Type.LOG, false))
                        throw new ExEx("variable '" + name +
                                    "': initialisation value wrong type", decloc);
                    if (tdev != null)
                        Bool_val = (Boolean)tdev.getConst();
                    else
                        Bool_val = ((Boolean)targ_init.getVal(i));
                    if (Bool_val.booleanValue())
                        s = "1";
                    else
                        s = "0";
                }
            }
            b.insert(0, s);
        }
        
        return(b.toString());
    }
    
    /**
     * Create assignment logic for SELECTVALUE, STATIC and QUEUE modes.
     * @param   inputs is an array of maps of input signals to the variable, one
     *          entry for each word
     * @param   writenables is an array of the word write signals
     * @param   dest is a TDEVar representing the data input
     * @param   svi is a default value for a selectvalue variable, else null
     * @param   words is the number of words
     * @param   queuefieldzero is true if unassigned queue fields are to
     *          be explicitly zeroed when a write (push) occurs and the queue
     *          has had one or more assignments that were not full width
     */
    public void assign (
        HashMap<TDEVar,eapair>[]    inputs,
        TDEVar[]                    writenables,
        TDEVar                      dest,
        String                      svi,
        int                         words,
        boolean                     queuefieldzero
    ) {
        // Use a HashMap whose key is a set of execute signals
        // and whose value is a TDEVar of the OR of those signals.
        // This is to avoid duplication of common sets of ORed
        // signals for selector inputs or enable signals.
        HashMap<HashSet<TDEVar>,TDEVar>     ormap = new HashMap<HashSet<TDEVar>,TDEVar>(); 

        // In the case of a queue, if the number of words is > 1, check
        // the execute signals in the sets for each word. If there is
        // only one word, or if there are multiple words but all
        // have the same single execute signal, an input selector
        // is not needed. If each word has only one input but the words
        // have separate execute signals then selectors are necessary,
        // even if single input.

        int queue_inputs = 0;
        if (mode == Mode.QUEUE) {
            for (int i=0 ; i<words ; i++) {
                int j = inputs[i].entrySet().size();
                if (j > queue_inputs)
                    queue_inputs = j;
            }
        }

        // iterate through each word creating an input selector
        // if that word has been assigned
        boolean         assign_conflict = false;
        boolean         use_alu = false;
        boolean         use_dsp = false;
        if ((mode == Mode.STATIC) && attributes.containsKey("alu"))
            use_alu = attributes.get("alu").getSingleLval(decloc);
        if ((mode == Mode.STATIC) && attributes.containsKey("dsp"))
            use_dsp = attributes.get("dsp").getSingleLval(decloc);
        
        for (int i=0 ; i<words ; i++) {
            if (inputs[i].isEmpty()) {
                // no inputs -
                // for static, connect .D to zero/false and .CE to GND
                // for selectvalue or queue, connect .D to zero/false
                TDEVar  zero = null;
                switch (val_type[i].getPrimType()) {
                case BITS:
                case UINT:
                case INT:
                case UFIXED:
                case FIXED:
                case ENUM:
                case FLOAT:
                    zero = new TDEVar(Long.valueOf(0), val_type[i], decloc);
                    break;
                case LOG:
                    zero = new TDEVar(Boolean.valueOf(false), Ptype.LOG, decloc);
                    break;
                default:
                    break;
                }
                // SELECTVALUE, STATIC or QUEUE data input connected to a zero TDEVar
                tdelist.connect(dest.getWord(i, decloc), zero);
                
                // STATIC CE connected to GND
                if (mode == Mode.STATIC)
                    writenables[i] = TDEVar.GND;
                
                continue;
            }
            
            TDEVar          d = dest.getWord(i, decloc);
            Iterator<?>     mit;
            TDE             sel;
            boolean         force_3_state = false;
            if (attributes.containsKey("threestate"))
                force_3_state = attributes.get("threestate").getSingleLval(decloc);
            
            // The data input selector for the STATIC, QUEUE or SELECTVALUE variable.
            if ((mode == Mode.SELECTVALUE) && getFamily().hasTBUF() && force_3_state)
                sel = new TDE(TDEType.TSELECT, decloc);
            else
                sel = new TDE(TDEType.SELECT, decloc);

            // Iterate through input sources for this word
            // collecting all exec signals into a set.
            HashSet<TDEVar>     all_execs = new HashSet<TDEVar>();
            mit = inputs[i].entrySet().iterator();
            while (mit.hasNext()) {
                Map.Entry<?, ?> me = (Map.Entry<?, ?>)mit.next();
                eapair          eap = (eapair)me.getValue();
                HashSet<TDEVar> word_execs = eap.getExecs();
                if (!ormap.containsKey(word_execs)) {
                    // Make an OR gate for all exec signals driving
                    // the word selector input and put it in the
                    // OR map.
                    TDE         or = new TDE(TDEType.OR, decloc);
                    Iterator<TDEVar>    it = word_execs.iterator();
                    while (it.hasNext())
                        or.add2i(it.next());
                    ormap.put(word_execs, or.finish());
                }
                all_execs.addAll(word_execs);
            }

            if (!ormap.containsKey(all_execs)) {
                // Make an OR gate for all exec signals for this word
                // to drive the word enable or PUSH and put it in the
                // OR map.
                TDE     or = new TDE(TDEType.OR, decloc);
                Iterator<TDEVar>    it = all_execs.iterator();
                while (it.hasNext())
                    or.add2i(it.next());
                ormap.put(all_execs, or.finish());
            }

            // Iterate again through input sources for this word
            // adding inputs to the input data selector.
            int     sources = inputs[i].size();
            mit = inputs[i].entrySet().iterator();
            while (mit.hasNext()) {
                Map.Entry<?, ?> me = (Map.Entry<?, ?>)mit.next();
                TDEVar          src = (TDEVar)me.getKey();
                eapair          eap = (eapair)me.getValue();
                HashSet<TDEVar> word_execs = eap.getExecs();
                TDEVar          selin = ormap.get(word_execs);
                
                if (eap.isTopLevel() && sources > 1)
                    assign_conflict = true;

                sel.add2i(selin);   // add the select input signal to the input data selector
                sel.add2i(src);     // add the matching data input signal to the input data selector
            }

            if (mode == Mode.SELECTVALUE) {
                // Mark a selectvalue mode select element so that
                // it is not eliminated.
                sel.setNoDelete();
            } else {
                // Get the write signal(s) for the STATIC or QUEUE TDE.
                writenables[i] = ormap.get(all_execs);

                if ((mode == Mode.QUEUE) && queuefieldzero)
                    // Mark a queue input selector element so that it is not
                    // eliminated if input is not full width and there is not a
                    // single execute signal. this ensures that components of a
                    // queue that are not explicitly assigned a value will be
                    // assigned 0 or false as appropriate rather than being
                    // unchanged by assignment.
                    sel.setNoDelete();  // Retain any inputs with no data connected.
            }

            // Add the output signal to the input data selector - this is the data input
            // to the STATIC, QUEUE or SELECTVALUE TDE.
            sel.add2o(d);
            
            if (svi != null) {
                // For selectvalue with optional default selector output state, mask word bits
                // from RH side of binary string and add them as the default for the selector
                // when not in low-Z state (1st parameter of SELECT TDE).
                int width = wordspec.getWidth(i);
                int j = svi.length() - width;
                sel.add2p(Functions.binToHex(svi.substring(j)));    // default output value for selector
                // remove the RH bits from svi
                if (j > 0)
                    svi = svi.substring(0, j);
            } else {
                // For STATIC or QUEUE or SELECTVALUE with no default output specification
                // 
                sel.add2p("0");     // zero default output for selector
            }
            
            if (mode == Mode.STATIC) {
                sel.add2p(use_alu); // add the optional ALU parameter
                sel.add2p(use_dsp); // add the optional DSP parameter
            }
            
            tdelist.addTDE(sel);    // add the input data selector to the TDE list
        }
            
        if (assign_conflict) {
            if (boolDir("rptToFile")) {
                rpt("\n\t" + decloc.toString() + " - WARNING");
                rpt("\tpossible assignment conflict\n");
            }
        }
    }

    /**
     * Add an input to the variable.
     * @param   inputs is a map of input signals to the variable
     * @param   i is the word index
     * @param   tdev is the data input signal
     * @param   exec is the signal which is used both as a selector and
     *          an executor
     * @param   asstype is the assignment type
     * @param   toplevel is true if this is the top level in a module
     */
    public void add_input(
        HashMap<TDEVar,eapair>[]    inputs,
        int                         i,
        TDEVar                      tdev,
        TDEVar                      exec,
        AST                         asstype,
        boolean                     toplevel
    ) {
        //if (tdev.numBits() != wordspec.numBits())
        //    throw new ExEx("INPUT ASSIGNMENT MISMATCH");
        HashMap<TDEVar, eapair>     m;
        eapair     eap;
        m = inputs[i];
        if (!m.containsKey(tdev)) {
            eap = new eapair();
            m.put(tdev, eap);
        } else
            eap = m.get(tdev);
        eap.put(exec, asstype, toplevel);
        assigned = true;
    }

    public Val getVal(WordSpec ws, SubFieldList sfl, Object[] val2,
            Type[] val_type2, Flag flag, SrcLoc loc) {
        throw new ExEx("SYSTEM ERROR - Var.getVal() not overridden by subclass");
    }
}
