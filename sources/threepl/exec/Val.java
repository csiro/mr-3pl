package threepl.exec;

import static threepl.ThreePL.tdelist;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

import threepl.ThreePL;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.funcs.InbuiltFunc;
import threepl.mods.InbuiltMod;
import threepl.parser.Constant;
import threepl.parser.Functions;
import threepl.parser.SrcLoc;
import threepl.procs.InbuiltProc;

/**
 * This class represents a RHS value. A value can be the
 * result of evaluation of an expression which in turn may
 * be the result of applying operators to values obtained from
 * variables or function calls.
 *
 * This class extends class RefOrVal which contains fields shared
 * by this class and the similar Ref class.
 *
 * For immediate modes the value contains an array of objects holding
 * the actual values of primitive or compound types. Expressions will
 * have single primitive values but variables or functions may have an
 * array of values from a compound type.
 *
 * The value also contains an array of primitive type codes
 * matching the value array. This type array is present for target
 * values also, however target values do not use a value array.
 *
 * Target values contain a TDEVar which is a signal representing
 * the value computed in the FPGA (this is in the super class RefOrVal).
 *
 * Object[] val - an array containing the immediate or target values.
 * The 'wordspec' field inherited from class RefOrVal describes the entries
 * in the 'val' array; each word in 'wordspec' matches the corresponding
 * entry in 'val'.
 *
 * Type[] val_type - the type of each word. The type will always be
 * primitive.
 *
 * inarg indicates that the value is that of an argument to a module, procedure or function.
 * 
 * PtrType mpfcode; - indicates the type if the value is a pointer to a module,
 * procedure or function.
 */
public final class Val extends RefOrVal implements Constant {
    protected Object[]      val;
    protected Type[]        val_type;
    protected boolean       inarg;
    private   PtrType       mpfcode = PtrType.NONE;

    /**    
     * Construct an immediate value which is a type.
     * @param   t is the type
     * @param   loc is the source file location
     */
    public Val (Type t, SrcLoc loc) {
        mode = Mode.IMMEDIATE;
        wordspec = new WordSpec(0, Ptype.TYPE);
        wordspec.setCheckType(Type.TYPE);
        val = new Object[1];
        val[0] = t;
        val_type = new Type[1];
        val_type[0] = Type.TYPE;
        this.loc = loc;
    }

    /**    
     * Construct an immediate value which is a single integer constant.
     * If the constant is -ve, make it type INT, otherwise
     * make it type UINT.
     * @param   i is the integer value
     * @param   loc is the source file location
     */
    public Val (long i, SrcLoc loc) {
        Ptype   ptype = (i < 0) ? Ptype.INT : Ptype.UINT;
        Type    type = new Type(ptype, 0);
        type.setWidth(Functions.bits(i, i < 0));
        mode = Mode.IMMEDIATE;
        wordspec = ptype.getWordSpec();
        //wordspec = new WordSpec(0, ptype, null);
        //wordspec.setCheckType(type);
        val = new Object[1];
        val[0] = Long.valueOf(i);
        val_type = new Type[1];
        val_type[0] = type;
        this.loc = loc;
    }

    /**    
     * Construct an immediate value which is a single integer constant where
     * the primitive type is specified (BITS, UINT, INT).
     * @param   i is the integer value
     * @param   ptype is the primitive type code (Ptype.BITS, Ptype.UINT
     *          or Ptype.INT)
     * @param   loc is the source file location
     */
    public Val (Long i, Ptype ptype, SrcLoc loc) {
        Type    type = new Type(ptype, 0);
        type.setWidth(Functions.bits(i, ptype==Ptype.INT));
        mode = Mode.IMMEDIATE;
        wordspec = ptype.getWordSpec();
        //wordspec = new WordSpec(0, ptype, null);
        //wordspec.setCheckType(type);
        val = new Object[1];
        val[0] = i;
        val_type = new Type[1];
        val_type[0] = type;
        this.loc = loc;
    }

    /**    
     * Construct an immediate value which is a single floating point constant.
     * @param   f is the floating point value
     * @param   loc is the source file location
     */
    public Val (double f, SrcLoc loc) {
        mode = Mode.IMMEDIATE;
        wordspec = Ptype.FLOAT.getWordSpec();
        //wordspec = new WordSpec(0, Ptype.FLOAT, null);
        //wordspec.setCheckType(Type.FLOAT);
        val = new Object[1];
        val[0] = Double.valueOf(f);
        val_type = new Type[1];
        val_type[0] =Type.FLOAT;
        this.loc = loc;
    }

    /**    
     * Construct an immediate value which is a scope from a 3PL class.
     * @param   s is the scope
     * @param   loc is the source file location
     */
    public Val (Scope s, SrcLoc loc) {
        mode = Mode.IMMEDIATE;
        wordspec = new WordSpec(Ptype.CLASS);
        val = new Object[1];
        val[0] = s;
        val_type = new Type[1];
        val_type[0] =Type.CLASS;
        this.loc = loc;
    }
    
    /**    
     * Construct an immediate value which is a single logical constant.
     * @param   l is the boolean value
     * @param   loc is the source file location
     */
    public Val (boolean l, SrcLoc loc) {
        mode = Mode.IMMEDIATE;
        wordspec = Ptype.LOG.getWordSpec();
        //wordspec = new WordSpec(0, Ptype.LOG, null);
        //wordspec.setCheckType(Type.LOG);
        val = new Object[1];
        val[0] = Boolean.valueOf(l);
        val_type = new Type[1];
        val_type[0] = Type.LOG;
        this.loc = loc;
    }
    
    /**    
     * Construct an immediate value which is a single string constant.
     * @param   s is the string value
     * @param   loc is the source file location
     */
    public Val (String s, SrcLoc loc) {
        mode = Mode.IMMEDIATE;
        wordspec = Ptype.STR.getWordSpec();
        //wordspec = new WordSpec(0, Ptype.STR, null);
        //wordspec.setCheckType(Type.STR);
        val = new Object[1];
        val[0] = s;
        val_type = new Type[1];
        val_type[0] = Type.STR;
        this.loc = loc;
    }
    
    /**    
     * Construct an immediate value which is a string array constant.
     * @param   sa is the string array value
     * @param   loc is the source file location
     */
    public Val (String[] sa, SrcLoc loc) {
        int         n = sa.length;
        Type        type = new Type("[" + n + "]str", loc);
        Type        pt = new Type("str", loc);
        
        mode = Mode.IMMEDIATE;
        wordspec = type.getWordSpec(null, loc);
        val = sa;
        val_type = new Type[n];
        for (int i=0 ; i<n ; i++)
            val_type[i] = pt;
        this.loc = loc;
    }
        
    /**    
     * Construct an immediate value which is an array constant.
     * @param   sa is the Object array value
     * @param   t is the array type string
     * @param   loc is the source file location
     */
    public Val (Object[] sa, String t, SrcLoc loc) {
        int         n = sa.length;
        Type        type = new Type("[" + n + "]" + t, loc);
        Type        pt = new Type(t, loc);
        
        mode = Mode.IMMEDIATE;
        wordspec = type.getWordSpec(null, loc);
        val = sa.clone();
        val_type = new Type[n];
        for (int i=0 ; i<n ; i++)
            val_type[i] = pt;
        this.loc = loc;
    }
    
    /**    
     * Construct an immediate value which is a single enum constant.
     * @param   l is the ordinal value
     * @param   t is the enum type
     * @param   loc is the source file location
     */
    public Val (Long l, Type t, SrcLoc loc) {
        mode = Mode.IMMEDIATE;
        wordspec = new WordSpec(t, null);
        wordspec.setCheckType(t);
        val = new Object[1];
        val[0] = Long.valueOf(l);
        val_type = new Type[1];
        val_type[0] = t;
        this.loc = loc;
    }
    
    /**    
     * Construct a target value of type UFIXED or FIXED from a floating point
     * constant.
     * @param   d is the floating point value
     * @param   t is a Type giving the target type
     * @param   loc is the source file location
     *
    public Val (double d, Type t, SrcLoc loc) {
        WordSpec    ws = t.getWordSpec(null, loc);
        tdevar = new TDEVar(d, ws, 0, loc);
        mode = Mode.VALUE;
        wordspec = tdevar.getWordSpec(); // get proper target WordSpec 
    }*/
    
    /**    
     * Construct a value which is a pointer to a variable reference or
     * construct a target value from a Ref.
     * @param   ref is the variable reference
     * @param   isptr is true if this is a pointer reference, otherwise
     *          it is a target value from a Ref.
     * @param   loc is the source file location
     */
    public Val (Ref ref, boolean isptr, SrcLoc loc) {
        if (isptr) {
            var = ref.getVar();
            mode = Mode.IMMEDIATE;
            wordspec = new WordSpec(0, Ptype.PTR);
            wordspec.setCheckType(Type.PTR);
            val = new Object[1];
            val[0] = ref;
            val_type = new Type[1];
            val_type[0] = Type.PTR;
        } else {
            var = ref.getVar();
            mode = ref.getMode();
            wordspec = ref.getWordSpec();
            val = null; // target value so no val[] field
            subfields = ref.getSubFields();
            tdevar = ref.getTDEVar();
            andSetQueues(wordspec.getSQueues(), loc);
            addIVars(ref);
            addOVars(ref);
            if ((var != null) && (mode == Mode.STATIC))
                tdevar.setClkVar(var.getOutputClkVar(loc));
        }
        this.loc = loc;
    }
   
    /**    
     * Construct a value which is a null.
     * @param   loc is the source file location
     */
    public Val (SrcLoc loc) {
        var = null;
        mode = Mode.IMMEDIATE;
        wordspec = new WordSpec(0, Ptype.NULL);
        wordspec.setCheckType(Type.NULL);
        val = new Object[1];
        val[0] = null;
        val_type = new Type[1];
        val_type[0] = Type.NULL;
        this.loc = loc;
    }

    /**    
     * Construct a value which is a pointer to a module, procedure,
     * function or variable.
     * @param   t is a pointer type for module, procedure or function
     *          or is null for a variable pointer.
     * @param   o is the inbuilt or user module, procedure or function
     * @param   loc is the source file location
     */
    public Val (PtrType t, Object o, SrcLoc loc) {
        var = null;
        mode = Mode.IMMEDIATE;
        wordspec = new WordSpec(0, Ptype.PTR);
        wordspec.setCheckType(Type.PTR);
        val = new Object[1];
        val[0] = o;
        val_type = new Type[1];
        val_type[0] = Type.PTR;
        mpfcode = t;
        this.loc = loc;
    }

    /**    
     * Construct a value which is a file.
     * @param   file is a file descriptor
     * @param   loc is the source file location
     */
    public Val (FileDesc file, SrcLoc loc) {
        mode = Mode.IMMEDIATE;
        wordspec = new WordSpec(0, Ptype.FILE);
        wordspec.setCheckType(Type.FILE);
        val = new Object[1];
        val[0] = file;
        val_type = new Type[1];
        val_type[0] = Type.FILE;
        this.loc = loc;
    }

    /**    
     * Construct a value which is a list of Val.
     * @param   al is an ArrayList
     * @param   loc is the source file location
     */
    public Val (ArrayList<Val> al, SrcLoc loc) {
        Type    lt = new Type(Ptype.LIST, 0);
        mode = Mode.IMMEDIATE;
        wordspec = new WordSpec(0, Ptype.LIST);
        wordspec.setCheckType(lt);
        val = new Object[1];
        val[0] = al;
        val_type = new Type[1];
        val_type[0] = lt;
        this.loc = loc;
    }

    /**    
     * Construct an IMMEDIATE value. It is constructed
     * from a word specification, an array of values, an array of
     * associated primitive types and a list of subscript/field constants.
     * Note that at the moment the entire argument array is copied even
     * though the pair list will only select some of the entries.
     * @param   o is the array of values
     * @param   t is the array of types (primitive)
     * @param   ws is the word specifier
     * @param   v is the associated variable or null if none
     * @param   subs is the list of subscripts/fields or null
     * @param   loc is the source file location
     */
    public Val (Object[] o, Type[] t, WordSpec ws, Var v, SubFieldList subs, SrcLoc loc) {
        /*
        if (ws.getCheckType().hasTargetType())
            throw new ExEx("Target type(s) in compound constant", loc);
        */
        if (ws.getCheckType().hasTargetType())
            mode = Mode.VALUE;
        else
            mode = Mode.IMMEDIATE;
        wordspec = ws;
        val = new Object[o.length];
        val_type = new Type[t.length];
        System.arraycopy(o, 0, val, 0, o.length);
        System.arraycopy(t, 0, val_type, 0, t.length);
        var = v;
        subfields = (subs == null) ? null : (SubFieldList) subs.clone();
        this.loc = loc;
    }

    /**    
     * Construct a target value. This is done after a TDEVar representing
     * the target value has been created.
     * @param   v is the variable if this value is a variable (null otherwise)
     * @param   m is the target mode
     * @param   tdev is the value TDEVar signal
     * @param   loc is the source file location
     */
    public Val (Var v, Mode m, TDEVar tdev, SrcLoc loc) {
        var = v;
        mode = m;
        wordspec = tdev.getWordSpec();
        tdevar = tdev;
        this.loc = loc;
    }

    /**    
     * Construct a wrapper for a TDEVar, queue references and
     * any referenced variables.
     * @param   tdev is the value TDEVar signal
     * @param   loc is the source file location
     */
    public Val (TDEVar tdev, SrcLoc loc) {
        tdevar = tdev;
        if (tdev != null)
            wordspec = tdev.getWordSpec();
        this.loc = loc;
    }

    /**    
     * Construct a memory variable 'value'. There is no value, simply
     * a reference to the Var for the memory variable.
     * @param   v is the variable if this value is a variable (null otherwise)
     * @param   loc is the source file location
     */
    public Val (Var v, SrcLoc loc) {
        mode = v.getMode();
        subfields = new SubFieldList();
        var = v;
        this.loc = loc;
    }
   
    /**
     * Set the clock domain variable in the TDEVar for this Val.
     * @param   v is the clock variable
     */
    public void setClkVar (Clock v) {
        tdevar.setClkVar(v);
    }
    
    /**
     * Set a dummy (unnamed) variable for this value.
     * This is used to ensure that a Val has an associated Var wrapper
     * so that a Ref can be generated.
     * @param force the dummy variable overwriting any existing Var field
     * @param loc the source file location
     */   
    public void setDummyVar (boolean force, SrcLoc loc) {
        if ((var == null) || force)
            if (mode != Mode.IMMEDIATE)
                throw new ExEx("immediate assignment RHS value is not immediate mode ", loc);
            var = new Immediate(val, val_type, wordspec, loc);
    }
        
    /**    
     * Convert a single primitive immediate value to a target TDEVar
     * representing the constant. Note that this does NOT generate a
     * TDEVar in which the bits are a pattern of GND and VCC representing
     * the constant but simply creates a TDEVar containing the constant.
     * This allows arithmetic operators access to an original constant operand
     * for optimisation purposes. There is no check that this is indeed a
     * primitive type.
     */
    public void toTarget () {
        if (mode != Mode.IMMEDIATE)
            return;
        if (wordspec.numWords() > 1)
            throw new ExEx("convertToTarget() - not single word", loc);
        int index = wordspec.getWord(0);
        tdevar = new TDEVar(val[index], val_type[index], loc);
        mode = Mode.VALUE;
        wordspec = tdevar.getWordSpec(); // get proper target WordSpec 
    }
        
    /**    
     * Convert a single primitive immediate FLOAT value to a target TDEVar
     * representing the constant as an integer with offset. Note that this
     * does NOT generate a TDEVar in which the bits are a pattern of GND
     * and VCC representing the constant but simply creates a TDEVar
     * containing the constant. This allows arithmetic operators access to
     * an original constant operand for optimisation purposes. There is no
     * check that this is indeed a primitive type.
     * @param   rov is a target Ref or Val whose FIXED/UFIXED type controls
     *          the number of bits generated from the floating point value
     */
    public void toTarget (RefOrVal rov) {
        if (mode != Mode.IMMEDIATE)
            return;
            //throw new ExEx("System error! toTarget() - not immediate", loc);
        if (wordspec.numWords() > 1)
            throw new ExEx("System error! toTarget() - not single word", loc);

        WordSpec    ws = rov.getWordSpec();
        //if (val_type[wordspec.getWord(0)].getPrimType() != Ptype.FLOAT)
        //    throw new ExEx("System error! toTarget() - not float type", loc);

        int index = wordspec.getWord(0);
        tdevar = new TDEVar(val[index], ws, 0, loc);
        mode = Mode.VALUE;
        wordspec = tdevar.getWordSpec(); // get proper target WordSpec 
    }

    /**
     * Expand a single primitive immediate value to a compound type.
     * This is only called at one place in constructor Exec.Var.Var().
     * @param   type is the compound type
     * @param   loc is the source file location
     * @return  the compound value
     */
    public Val expandToCompound (Type type, SrcLoc loc) {
        // This value has already been checked and is primitive.
        int n = type.numWords();
        Val v = new Val(type, loc);
        v.mode = mode;
        v.var = var;
        v.addOVars(this);
        v.wordspec = type.getWordSpec(var, loc);
        v.val = new Object[n];
        v.val_type = new Type[n];
        for (int i=0 ; i<n ; i++) {
            v.val[i] = val[0];
            v.val_type[i] = val_type[0];
        }
        return(v);
    }

    /**
     * Compare this immediate value with another for equality. The
     * dimensionality and check type are first compared. Then all entries in
     * the value array and value type arrays are checked.
     * @param   v is the Val to compare with this
     * @return  true if the two values are equal
     */
    public boolean isEqual (Val v) {
        if (v == this)
            return(true);
        
        if (!checkMatch(v, false))
            return(false);
            
        for (int i=0 ; i<val.length ; i++) {
            if (!val_type[i].isEqual(v.val_type[i], false))
                return(false);
            if (!val[i].getClass().isInstance(v.getVal(i).getClass()))
                return(false);
            if (!val[i].equals(v.getVal(i)))
                return(false);
        }
        return(true);
    }

    /**
     * Get the integer value from a single immediate Ptype.INT or Ptype.UINT
     * Val. If the type is compound or is not an integer type an ExEx
     * exception is thrown.
     * @param   loc is the source file location
     * @return  the integer value (long)
     */
    public long getSingleIval (SrcLoc loc) {
        if (mode != Mode.IMMEDIATE)
            throw new ExEx("value is not immediate", loc);
        if (val[0] == null)
            throw new ExEx("value is null", loc);
        if (!isPrimitive())
            throw new ExEx("single value required", loc);
        if ((getPrimType() != Ptype.INT) && (getPrimType() != Ptype.UINT))
            throw new ExEx("integer type required", loc);
        return(((Long)val[0]).longValue());
    }

    /**
     * Get the file object from a single Ptype.FILE Val. If the type
     * is compound or is not a file type an ExEx exception is thrown.
     * @param   loc is the source file location
     * @return  the open file
     */
    public FileDesc getSingleFileVal (SrcLoc loc) {
        if (mode != Mode.IMMEDIATE)
            throw new ExEx("value is not immediate", loc);
        if (val[0] == null)
            throw new ExEx("value is null", loc);
        if (!isPrimitive())
            throw new ExEx("single value required", loc);
        if (getPrimType() != Ptype.FILE)
            throw new ExEx("file type required", loc);
        return((FileDesc)val[0]);
    }

    /**
     * Get the floating point value from a single immediate Ptype.FLOAT
     * Val. If the type is compound or is not a floating point type an ExEx
     * exception is thrown.
     * @param   loc is the source file location
     * @return  the floating point value (double)
     */
    public double getSingleFval (SrcLoc loc) {
        if (mode != Mode.IMMEDIATE)
            throw new ExEx("value is not immediate", loc);
        if (val[0] == null)
            throw new ExEx("value is null", loc);
        if (!isPrimitive())
            throw new ExEx("single value required", loc);
        if (getPrimType() != Ptype.FLOAT)
            throw new ExEx("floating point type required", loc);
        return(((Double)val[0]).doubleValue());
    }

    /**
     * Get the logical value from a single immediate Ptype.LOG Val.
     * If the type is compound or is not Ptype.LOG type an ExEx
     * exception is thrown.
     * @param   loc is the source file location
     * @return  the logical value (boolean)
     */
    public boolean getSingleLval (SrcLoc loc) {
        if (mode != Mode.IMMEDIATE)
            throw new ExEx("value is not immediate", loc);
        if (val[0] == null)
            throw new ExEx("value is null", loc);
        if (!isPrimitive())
            throw new ExEx("single value required", loc);
        if (getPrimType() != Ptype.LOG)
            throw new ExEx("logical type required", loc);
        return(((Boolean)val[0]).booleanValue());
    }
    
    /**
     * Get a logical value from an immediate array type Ptype.LOG.
     * If the value array is not type Boolean then an ExEx exception is thrown.
     * @param   i is the array subscript
     * @param   loc is the source file location
     * @return  the logical value (boolean)
     */
    public boolean getArrayLval (int i, SrcLoc loc) {
        if (!(val[i] instanceof Boolean))
            throw new ExEx("Attempt to get a value of type 'log' from an array of another type", loc);
        return((boolean)val[i]);
    }

    /**
     * Get the ordinal value from a single immediate Ptype.ENUM
     * Val. If the type is compound or is not an enum type an ExEx
     * exception is thrown.
     * @param   loc is the source file location
     * @return  the ordinal value (long)
     */
    public long enumOrd (SrcLoc loc) {
        if (mode != Mode.IMMEDIATE)
            throw new ExEx("value is not immediate", loc);
        if (!isPrimitive())
            throw new ExEx("single value required", loc);
        if (getPrimType() != Ptype.ENUM)
            throw new ExEx("enumerated type required", loc);
        return(((Long)val[0]).longValue());
    }

    /**
     * Get an enumerated type single immediate ordinal value.
     * If the type is compound or not type enum an ExEx exception is thrown.
     * @param   loc is the source file location
     * @return  the ordinal
     */
    public EnumConst getSingleEval (SrcLoc loc) {
        if (mode != Mode.IMMEDIATE)
            throw new ExEx("value is not immediate", loc);
        if (val[0] == null)
            throw new ExEx("value is null", loc);
        if (!isPrimitive())
            throw new ExEx("single value required", loc);
        if (getPrimType() != Ptype.ENUM)
            throw new ExEx("enumerated type required", loc);
        return(new EnumConst(val_type[0], ((Long)val[0]).longValue()));
    }
    
    /**
     * If the value is a single immediate INT, UINT or constant
     * target  INT, UINT, FIXED or UFIXED whose numeric
     * value is equal to the argument, return true, otherwise return false.
     * @param   v is the numeric value to be tested
     * @return  this value is numerically equal to the argument
     */
    public boolean isN (long v) {
        if (!isPrimitive())
            return(false);
        switch (mode) {
        case IMMEDIATE:
            if ((getPrimType() != Ptype.INT) &&
                (getPrimType() != Ptype.UINT))
                return(false);
            return(((Long)val[0]).longValue() == v);
        case VALUE:
            if (!tdevar.isConst())
                return(false);
            Object  o = tdevar.getConst();
            if (o instanceof Long) {
                int fixoffset = wordspec.getFixOffset(0);
                return(((Long)o).longValue() == (v << fixoffset));
            } else
                return(false);
        default:
            return(false);
        }
    }
    
    /**
     * If the value is a single LOG whose logical
     * value is equal to the argument, return true, otherwise return false.
     * @param   b is the logical value to be tested
     * @return  this value is numerically equal to the argument
     */
    public boolean isL (boolean b) {
        if (mode != Mode.IMMEDIATE)
            return(false);
        if (!isPrimitive())
            return(false);
        if (getPrimType() != Ptype.LOG)
            return(false);
        return(((Boolean)val[0]).booleanValue() == b);
    }
    
    /**
     * If the value is a single BITS, INT, UINT, FIXED or UFIXED whose numeric
     * value is a power of 2, return the power, otherwise
     * return zero.
     * @return  the log 2 of the value, or zero
     */
    public int p2 () {
        long    v;
        int     log2 = 0;

        if (!isPrimitive())
            return(0);
        switch (mode) {
        case IMMEDIATE:
            if ((getPrimType() != Ptype.INT) &&
                (getPrimType() != Ptype.UINT))
                return(0);
            v = ((Long)val[0]).longValue();
            for (;;) {
                if ((v & 1) != 0)
                    return((v == 1) ? log2 : 0);
                log2++;
                v >>= 1;
            }
        case VALUE:
            if (!tdevar.isConst())
                return(0);
            Object  o = tdevar.getConst();
            if (!(o instanceof Long))
                return(0);
            int fixoffset = wordspec.getFixOffset(0);
            v = ((Long)o).longValue();
            for (;;) {
                if ((v & 1) != 0) {
                    if (v != 1)
                        return(0);
                    return(log2 - fixoffset);
                }
                log2++;
                v >>= 1;
            }
        default:
            return(0);
        }
    }

    /**
     * Get the string value from a single immediate Val.
     * If the type is compound or not type str an ExEx exception is thrown.
     * @param   loc is the source file location
     * @return  the String value
     */
    public String getSingleSval (SrcLoc loc) {
        if (mode != Mode.IMMEDIATE)
            throw new ExEx("value is not immediate", loc);
        if (val[0] == null)
            throw new ExEx("value is null", loc);
        if (!isPrimitive())
            throw new ExEx("single value required", loc);
        if (getPrimType() != Ptype.STR)
            throw new ExEx("string type required", loc);
        return((String)val[0]);
    }

    /**
     * Get the string value from a single immediate Val.
     * If the primitive type is not a string, convert it to a string.
     * If the type is compound an ExEx exception is thrown.
     * @param   loc is the source file location
     * @return  the String value
     */
    public String forceSingleSval (SrcLoc loc) {
        if (mode != Mode.IMMEDIATE)
            throw new ExEx("value is not immediate", loc);
        if (!isPrimitive())
            throw new ExEx("single value required", loc);
        switch (getPrimType()) {
        case STR:
            return((String)val[0]);
        case INT:
        case UINT:
            return(String.valueOf(val[0]));        
        case FIXED:
            throw new ExEx("fixed point type not yet implemented", loc);
        case FLOAT:
            return(String.valueOf(val[0]));        
        case LOG:
            if (((Boolean)val[0]).booleanValue())
                return("true"); 
            else
                return("false"); 
        case ENUM:
            Long    i = (Long)val[0];
            Type    t = val_type[0];
            return(t.enumIdent(i, loc));
        default:
            throw new ExEx("string type required", loc);
        }
    }
    
    /**
     * Get a string value from an immediate array type Ptype.STR.
     * If the value array is not type String then an ExEx exception is thrown.
     * @param   i is the array index
     * @param   loc is the source file location
     * @return  the string value
     */
    public String getArraySval (int i, SrcLoc loc) {
        if (!(val[i] instanceof String))
            throw new ExEx("Attempt to get a value of type 'str' from an array of another type", loc);
        return((String)val[i]);
    }

    /**
     * Get the pointer reference from a single immediate Ptype.PTR Val.
     * If the type is compound or is not Ptype.PTR type an ExEx
     * exception is thrown.
     * @param   loc is the source file location
     * @return  the pointer reference
     */
    public Ref getSinglePval (SrcLoc loc) {
        if (mode != Mode.IMMEDIATE)
            throw new ExEx("value is not immediate", loc);
        /*
         * NO! allow null return for pointer comparison
        if (val[0] == null)
            throw new ExEx("value is null", loc);*/
        if (!isPrimitive())
            throw new ExEx("single value required", loc);
        if ((getPrimType() != Ptype.PTR) && (getPrimType() != Ptype.NULL))
            throw new ExEx("pointer type or null required", loc);
        if ((mpfcode != PtrType.NONE) || (val[0] != null) && !(val[0] instanceof Ref))
            throw new ExEx("pointer is not a variable pointer", loc);
        return((Ref)val[0]);
    }

    /**
     * Get the Type value from a single immediate Ptype.TYPE Val.
     * @param   loc is the source file location
     * @return  the 'type' value (Type)
     */
    public Type getSingleTval (SrcLoc loc) {
        if (mode != Mode.IMMEDIATE)
            throw new ExEx("mode is not immediate", loc);
        if (val[0] == null)
            throw new ExEx("value is null", loc);
        if (!(val[0] instanceof Type))
            throw new ExEx("type 'type' required", loc);
        return((Type)val[0]);
    }

    /**
     * Get the Scope value from a single immediate Ptype.SCOPE Val.
     * @param   loc is the source file location
     * @return  the 'Scope' value
     */
    public Scope getSingleScopeval (SrcLoc loc) {
        if (mode != Mode.IMMEDIATE)
            throw new ExEx("mode is not immediate", loc);
        if (val[0] == null)
            throw new ExEx("value is null", loc);
        if (!(val[0] instanceof Scope))
            throw new ExEx("type 'type' required", loc);
        return((Scope)val[0]);
    }

    /**
     * Get the TreeMap value from a single immediate Ptype.MAP Val.
     * @param   loc is the source file location
     * @return  the map
     */
    @SuppressWarnings("unchecked")
    public TreeMap<String,Val> getSingleMAPval (SrcLoc loc) {
        if (mode != Mode.IMMEDIATE)
            throw new ExEx("mode is not immediate", loc);
        if (getPrimType() != Ptype.MAP)
            throw new ExEx("type 'map' required", loc);
        return((TreeMap<String,Val>)val[0]);
    }

    /**
     * Get the ArrayList value from a single immediate Ptype.LIST Val.
     * @param   loc is the source file location
     * @return  the map
     */
    @SuppressWarnings("unchecked")
    public ArrayList<Val> getSingleLISTval (SrcLoc loc) {
        if (mode != Mode.IMMEDIATE)
            throw new ExEx("mode is not immediate", loc);
        if (getPrimType() != Ptype.LIST)
            throw new ExEx("type 'list' required", loc);
        return((ArrayList<Val>)val[0]);
    }
    
    /**
     * Get the pointer type code.
     * @return  the pointer type code
     */
    public PtrType getMPFCode () {return(mpfcode);}

    /**
     * Get a module or procedure pointer object from a single
     * immediate typePTR Val. If the type is compound or is not Ptype.PTR
     * type an ExEx exception is thrown.
     * @param   loc is the source file location
     * @return  the module or procedure pointer object
     */
    public Object getModProcPtrObject (SrcLoc loc) {
        if (mode != Mode.IMMEDIATE)
            throw new ExEx("value is not immediate", loc);
        if (!isPrimitive())
            throw new ExEx("single value required", loc);
        if (getPrimType() != Ptype.PTR)
            throw new ExEx("pointer type required", loc);
        if ((val[0] == null) ||
            !((val[0] instanceof InbuiltMod) ||
              (val[0] instanceof Module) ||
              (val[0] instanceof InbuiltProc) ||
              (val[0] instanceof Procedure)))
            throw new ExEx("pointer is not a class, module or procedure pointer", loc);
        return(val[0]);
    }
    
    /**
     * Get a class or function pointer object from a single
     * immediate Ptype.PTR Val. If the type is compound or is not typePTR
     * type an ExEx exception is thrown.
     * @param   loc is the source file location
     * @return  the function pointer object
     */
    public Object getClassFuncPtrObject (SrcLoc loc) {
        if (mode != Mode.IMMEDIATE)
            throw new ExEx("value is not immediate", loc);
        if (!isPrimitive())
            throw new ExEx("single value required", loc);
        if (getPrimType() != Ptype.PTR)
            throw new ExEx("pointer type required", loc);
        if ((val[0] == null) ||
            !((val[0] instanceof Group) ||
              (val[0] instanceof InbuiltFunc) ||
              (val[0] instanceof Function)))
            throw new ExEx("pointer is not a class or function pointer", loc);
        return(val[0]);
    }

    /**
     * Get the value array.
     * @return  the value array
     */
    public Object[] getVals () { return(val); }

    /**
     * Get one value from the value array.
     * @param   i is the index of the required value
     * @return  the value
     */
    public Object getVal (int i) { return(val == null ? null : val[i]); }

    /**
     * Get the value type array.
     * @return  the value type array
     */
    public Type[] getValTypes () { return(val_type); }

    /**
     * Get one value type from the value type array.
     * @param   i is the index of the required value type
     * @return  the value type
     */
    public Type getValType (int i) { return(val_type[i]); }

    /**
     * Get one value primitive type from the value type array.
     * @param   i is the index of the required value type
     * @return  the value type code
     */
    public Ptype getValPType (int i) { return(val_type[i].getPrimType()); }
    
    /**
     * Get the primitive type of this reference or value.
     * An integer value of Ptype.INT which is not -ve returns a type
     * Ptype.UINT.
     * If the type is not primitive, Ptype.NONE is returned.
     * @return  the primitive type
     */
    public Ptype getPrimType () {
        if ((wordspec == null) || (wordspec.getDimDes() != null))
            return(Ptype.NONE);
        Ptype t = wordspec.getCheckType().getPrimType();
        if ((t == Ptype.INT) && (val != null) && (val[0] != null) && (((Long)val[0]).longValue() >= 0))
            return(Ptype.UINT);
        return(t);
    }
    
    /**
     * Determine if this value is a target constant.
     * @return  true if this value is a target constant
     */
    public boolean isTargConst () {
        if (tdevar == null)
            return(false);
        return(tdevar.isConst());
    }
     
    /**
     * Determine if this value is immediate mode or is a target constant.
     * @return  true if this value is immediate mode or is a target constant
     */
    public boolean isImedOrTargConst () {
        if (mode == Mode.IMMEDIATE)
            return(true);
        if (tdevar == null)
            return(false);
        return(tdevar.isConst());
    }
  
    /**
     * Get a type from a declaration value. The value must be either
     * type 'type' or else type 'str'
     * @param   par_arg is an argument passed to a parameter which can be used
     *          to resolve missing array dimensions, null otherwise
     * @param   vmode is the variable mode
     * @param   is_output is true if this type declaration is for a module
     *          or procedure output parameter
     * @param   dim_des is an optional dimension description array for setting
     *          missing array dimensions
     * @param   allow_undim_array is true if an undimensioned array is to
     *          be allowed (called from procs/VarProc.java)
     * @param   mess is a string to prepend to any error messages
     * @param   loc is the source file location
     * @return the declared type
     */
    public Type getDeclType (
        RefOrVal    par_arg,
        Mode        vmode,
        boolean     is_output,
        int[]       dim_des,
        boolean     allow_undim_array,
        String      mess,
        SrcLoc      loc
    ) {
        Type    type;
        if (mode != Mode.IMMEDIATE)
            throw new ExEx(mess + " - type argument is not immediate mode", loc);
        if (getPrimType() == Ptype.TYPE)
            // a type variable
            type = getSingleTval(loc);
        else if (getPrimType() == Ptype.STR)
            // a type string
            type = new Type(getSingleSval(loc), par_arg!=null, false, loc);
        else
            throw new ExEx(mess + " - type argument is not string or type", loc);

        if ((type.getPrimType() == Ptype.EMPTY) && (par_arg != null))
                type = par_arg.getType();

        if (type.hasImmediateType() && !((vmode == Mode.IMMEDIATE) || (vmode == Mode.VALUE)))
            throw new ExEx(mess + " - immediate type for target mode", loc);
        if (type.hasPointerType() && (vmode != Mode.IMMEDIATE))
            throw new ExEx(mess + " - pointer type for target mode", loc);
        if ((type.getPrimType() == Ptype.NULL) && (vmode != Mode.QUEUE))
            throw new ExEx(mess + " - null type can only be used for queues", loc);

        // now check if there are undimensioned arrays or
        // primitive target types with no width -
        // only allowed for parameters!
        if (type.hasUndimArray()) {
            // Missing dimension(s) -
            // * check that we have a matching argument
            // * Make sure the types are compatible
            // * copy missing dimensions
            // We only need dimensions, not primitive type widths
            // (e.g. when immediate arg to target param)
            if (par_arg == null) {
                if (dim_des != null)
                    type.matchArray(
                        dim_des,
                        0,
                        null,
                        mess + " - param/arg type mismatch",
                        loc
                    );
                else if (!allow_undim_array)
                    type.zeroUndimArray();
                    //throw new ExEx(mess + " - undimensioned array", loc);
            } else
                type.matchArray(
                    par_arg.getWordSpec().getDimDes(),
                    0,
                    par_arg.getCheckType(),
                    mess + " - param/arg type mismatch",
                    loc
                );
        } else if (type.hasMissingWidth() && (par_arg != null)) {
            // primitive target type with missing width (0)
            if (par_arg.getMode() == Mode.IMMEDIATE) {
                if (is_output)
                    throw new ExEx(mess + " - immediate argument to output parameter", loc);
                type.setWidth(((Val)par_arg).getSingleNumBits());
            } else
                type.matchArray(null, 0, par_arg.getCheckType(), mess + " - parameter/argument type mismatch", loc);
        }
        return(type);
    }
    
    /*
     * Return a TDEVar for one word out of this value. If the value
     * is immediate then a constant TDEVar is returned.
     * @param   word indicates the word required
     * @param   loc is the source file location
     * @return  the Ref containing the extracted TDEVar for one word
    public TDEVar getWordTDEVar (int word, SrcLoc loc) {
        if (mode == Mode.IMMEDIATE) {
            if (val_type[word].getPrimType() == Ptype.STR)
                throw new ExEx("cannot use a string in target code", loc);
            TDEVar  tdev = new TDEVar(val[word], val_type[word].getPrimType(), 0, loc);
            return(tdev);
        } else
            return(tdevar.getWord(word, loc));
    }
     */
    
    /**
     * Return a TDEVar for one word out of this value. If the value
     * is immediate then a constant TDEVar is returned.
     * @param   ws is the word specification for the required word
     * @param   word is the index of the required word
     * @param   loc is the source file location
     * @return  the Ref containing the extracted TDEVar for one word
     */
    public TDEVar getWordTDEVar (WordSpec ws, int word, SrcLoc loc) {
        if ((mode == Mode.IMMEDIATE) || (tdevar == null)) {
            if (val_type[word].getPrimType() == Ptype.STR)
                throw new ExEx("cannot use a string in target code", loc);
            if (val_type[word].getPrimType() == Ptype.NONE)
                return(null);
            //TDEVar  tdev = new TDEVar(val[word], val_type[word].getPrimType(), rwidth, offset, 0, 0, loc);
            if (val[word] instanceof TDEVar)
                return((TDEVar)val[word]);
            TDEVar  tdev = new TDEVar(val[word], ws, word, loc);
            if (tdev.numBits() > ws.getWidth(word))
                throw new ExEx("immediate too large for target width", loc);
            return(tdev);
        } else
            return(tdevar.getWord(ws, word, loc));
    }
   
    /**
     * Get the number of bits required for a single value.
     * For an immediate INT or UINT this is calculated from
     * the actual value, allowing for a sign bit in the case of INT.
     * For a target value it is just the number of bits in the TDEVar.
     * For other types an ExEx exception is thrown.
     * @return  the number of bits
     */
    public int getSingleNumBits () {
        if (mode != Mode.IMMEDIATE)
            return(tdevar.numBits());
        if (val.length != 1)
            throw new ExEx("system error: getSingleNumBits() not single type!");
        if (val[0] instanceof Long) {
            int     n;
            long    v = ((Long)val[0]).longValue();
            n = Functions.bits(v, val_type[0].getPrimType() == Ptype.INT);
            return(n);
        } else if (val[0] instanceof Double) {
            throw new ExEx("system error: getSingleNumBits() floating point type not implemented!");
        } else
            throw new ExEx("system error: getSingleNumBits() type not coded!");
    }

    /**
     * Cast to a different type.
     *
     * <p>For immediate types this will convert only primitive types as follows -
     * <ul>
     *  <li>{@code uint -> str}
     *  <li>{@code int -> str}
     *  <li>{@code log -> str}
     *  <li>{@code bits -> str}
     *  <li>{@code str -> bits, uint, int, log}
     * </ul>
     *
     * <p>For primitive target types the conversions are -
     * <ul>
     *  <li>{@code uint -> int}     - an additional 0 bit is appended to the most
     *                        significant end
     *  <li>{@code int -> uint}     - simply copies bits - if -ve int, result is
     *                        large +ve unsigned integer
     *  <li> {@code * -> bits:n}    - match bits with truncation or zero padding
     *  <li> {@code bits:n -> *}    - match bits with truncation or zero padding
     *  <li> {@code * -> [n]bits:1} - match array of bits with truncation or zero padding
     *  <li> {@code [n]bits:1 -> *} - match array of bits with truncation or zero padding
     *  <li> {@code * -> *}          - anything to anything as long as they are the
     *                        same size.
     * </ul>
     *
     * <p>The only compound target conversion allowed is to or from bits. The
     * compound type is treated as a single word equivalent to concatenation
     * of the individual words of the compound type. Where the result width
     * is larger than the argument there will be zero padding at the most
     * significant bit end. If the result width is smaller than the argument
     * it will be truncated at the most significant bit end.
     * @param   type is the type to cast to
     * @param   mess is a message header string to prepend to any error messages
     * @param   loc is the source file location
     * @return  the new value
     */
    public Val cast (Type type, String mess, SrcLoc loc) {
        long    v;
        int     i;
        
        if (!isTarget()) {
            //
            // an immediate value
            //-------------------
            if (!isPrimitive())
                throw new ExEx(mess + " cannot cast from an immediate compound type", loc);
            if (!type.isPrimitive())
                throw new ExEx(mess + " cannot cast to an immediate compound type", loc);
            switch (getPrimType()) {
            case BITS:
                switch (type.getPrimType()) {
                case BITS:
                    return(this);
                case UINT:
                case INT:
                case ENUM:
                    return(new Val((Long)val[0], type.getPrimType(), loc));
                case STR:
                    return(new Val(((Long)val[0]).toString(), loc));
                default:
                    throw new ExEx(mess +
                        " cast from bits to this type not allowed", loc);
                }
            case UINT:
                switch (type.getPrimType()) {
                case BITS:
                case INT:
                case ENUM:
                    return(new Val((Long)val[0], type.getPrimType(), loc));
                case UINT:
                    return(this);
                case FLOAT:
                    return(new Val(((Long)val[0]).doubleValue(), loc));
                case STR:
                    return(new Val(((Long)val[0]).toString(), loc));
                default:
                    throw new ExEx(mess +
                        " cast from uint to this type not allowed", loc);
                }
            case INT:
                switch (type.getPrimType()) {
                case BITS:
                case UINT:
                case ENUM:
                    if (((Long)val[0]).longValue() < 0)
                        throw new ExEx(mess +
                            " cast from -ve int to bits, uint or enum not allowed", loc);
                    return(new Val((Long)val[0], type.getPrimType(), loc));
                case INT:
                    return(this);
                case FLOAT:
                    return(new Val(((Long)val[0]).doubleValue(), loc));
                case STR:
                    return(new Val(((Long)val[0]).toString(), loc));
                default:
                    throw new ExEx(mess +
                        " cast from int to this type not allowed", loc);
                }
            case FLOAT:
                switch (type.getPrimType()) {
                case UINT:
                    v = ((Double)val[0]).longValue();
                    if (v < 0)
                        throw new ExEx(mess +
                            " cast from -ve float to uint not allowed", loc);
                    return(new Val(Long.valueOf(v), type.getPrimType(), loc));
                case INT:
                    return(new Val(((Double)val[0]).longValue(), loc));
                case FLOAT:
                    return(this);
                case STR:
                    return(new Val(((Double)val[0]).toString(), loc));
                /* The following could be included, but for consistency then numerous
                 * similar cases should also be included, e.g. INT to FIXED etc.
                case UFIXED:
                    return(new Val((Double)val[0], type, loc));
                case FIXED:
                    return(new Val((Double)val[0], type, loc));
                */
                default:
                    throw new ExEx(mess +
                        " cast from float to this type not allowed", loc);
                }
            case LOG:
                return(new Val((Boolean)val[0] ? "true" : "false", loc));
            case STR:
                switch (type.getPrimType()) {
                case BITS:
                case UINT:
                case INT:
                    try {
                        if (((String)val[0]).startsWith("0b") ||
                            ((String)val[0]).startsWith("0B"))
                            v = Long.parseLong(((String)val[0]).substring(2), 2);
                        else if (((String)val[0]).startsWith("0x") ||
                                 ((String)val[0]).startsWith("0X"))
                            v = Long.parseLong(((String)val[0]).substring(2), 16);
                        else if (((String)val[0]).startsWith("0"))
                            v = Long.parseLong((String)val[0], 8);
                        else if ((i=((String)val[0]).indexOf('.')) >= 0)
                            // truncate real string value to integer
                            v = Long.parseLong(((String)val[0]).substring(0, i));
                        else
                            v = Long.parseLong((String)val[0]);
                    } catch (NumberFormatException e) {
                        throw new ExEx(mess +
                            " string bad number format", loc);
                    }
                    return(new Val(Long.valueOf(v), type.getPrimType(), loc));
                case FLOAT:
                    return(new Val(Double.parseDouble((String)val[0]), loc));
                case LOG:
                    return(new Val(((String)val[0]).toLowerCase().equals("true"), loc));
                case STR:
                    return(this);
                default:
                    throw new ExEx(mess +
                        " cast from str to this type not allowed", loc);
                }
            default:
                throw new ExEx(mess + " cast not allowed for this type", loc);
            }
        }
        
        //
        // a target value
        //---------------
        boolean     primcast = isPrimitive() && type.isPrimitive();
        Ptype       optype = type.getPrimType();
        Ptype       iptype = getPrimType();
        WordSpec    ws_new = type.getWordSpec(null, loc);
        int         iwidth = wordspec.numBits();
        int         owidth = type.numBits();
        int         ioffset = primcast ? wordspec.getFixOffset(0) : 0;
        int         ooffset = primcast ? ws_new.getFixOffset(0) : 0;
        if (owidth == 0)
            throw new ExEx(mess + " cannot cast to type " + optype.typename(), loc);
        boolean     sign_extend = false;
        boolean     use_offsets = false;
        TDEVar      tdev_old = tdevar;
        TDEVar      tdev_new = tdelist.signal("E", ws_new, loc);
        Val         retval;
                
        if ((iptype == Ptype.BITS) || (optype == Ptype.BITS)) {
            // If casting to or from BITS, any target type is OK.
            sign_extend = (iptype == Ptype.INT) || (iptype == Ptype.FIXED) ||
                          (optype == Ptype.INT) || (optype == Ptype.FIXED);
        } else if (!isPrimitive() || !type.isPrimitive()) {
            // If casting to/from compound types, is OK as long as the sizes
            // are equal.
            if (iwidth != owidth)
                throw new ExEx(mess + " target casts to or from compound type of different size are not allowed", loc);
        } else {
            // Other cases - check explicitly.
            // Check for types we cannot cast from.
            switch (iptype) {
            case UINT:
            case INT:
            case UFIXED:
            case FIXED:
            case LOG:
            case FLOAT:
            case ENUM:
                break;  // these are OK
            default:
                if (optype != Ptype.LOG)
                    throw new ExEx(mess + " target casts from " +
                    iptype.name() +
                    " are not allowed", loc);
            }

            // Specifically check other combinations.
            switch (optype) {
            case UINT:
                switch (iptype) {
                case UINT:
                    break;
                case INT:
                    sign_extend = true;
                    break;
                case UFIXED:
                    use_offsets = true;
                    break;
                case FIXED:
                    sign_extend = true;
                    use_offsets = true;
                    break;
                case LOG:
                    break;
                case ENUM:
                    break;
                default:
                    throw new ExEx(mess + " target casts from " +
                        iptype.name() + " to " + optype.name() + " are not allowed", loc);
                }
                break;
            case INT:
                switch (iptype) {
                case UINT:
                    break;
                case INT:
                    sign_extend = true;
                    break;
                case UFIXED:
                    use_offsets = true;
                    break;
                case FIXED:
                    sign_extend = true;
                    use_offsets = true;
                    break;
                default:
                    throw new ExEx(mess + " target casts from " +
                        iptype.name() + " to " + optype.name() + " are not allowed", loc);
                }
                break;
            case UFIXED:
                use_offsets = true;
                switch (iptype) {
                case UINT:
                case UFIXED:
                    break;
                case INT:
                case FIXED:
                    sign_extend = true;
                    break;
                default:
                    throw new ExEx(mess + " target casts from " +
                        iptype.name() + " to " + optype.name() + " are not allowed", loc);
                }
                break;
            case FIXED:
                use_offsets = true;
                switch (iptype) {
                case UINT:
                case UFIXED:
                    break;
                case INT:
                case FIXED:
                    sign_extend = true;
                    break;
                default:
                    throw new ExEx(mess + " target casts from " +
                        iptype.name() + " to " + optype.name() + " are not allowed", loc);
                }
                break;
            case LOG:
                switch (iptype) {
                case UINT:
                case LOG:
                    /*
                    if (iwidth != 1)
                        throw new ExEx(mess + " cannot cast from type " +
                                        type.getPrimType().name() + " to type " +
                                        getPrimType().name() +
                                        " unless equal sizes", loc);
                    */
                    break;
                case NULL:
                    tdev_new = new TDEVar(Boolean.valueOf(true), Ptype.LOG, loc);
                    retval = new Val(null, Mode.VALUE, tdev_new, loc);
                    retval.addExecSets(this);
                    if (queues != null)
                        retval.andSetQueues(queues, loc);
                    retval.addOVars(this);
                    return(retval);
              default:
                    throw new ExEx(mess + " target casts from " +
                        iptype.name() + " to " + optype.name() + " are not allowed", loc);
                }
                break;
            case FLOAT:
                switch (iptype) {
                case FLOAT:
                    tdev_old = tdevar.getWord(ws_new, 0, loc);
                    break;
                default:
                    throw new ExEx(mess + " target casts from " +
                        iptype.name() + " to " + optype.name() + " are not allowed", loc);
                }
                break;
            case ENUM:
                switch (iptype) {
                case UINT:
                case ENUM:
                    break;
                default:
                    throw new ExEx(mess + " target casts from " +
                        iptype.name() + " to " + optype.name() + " are not allowed", loc);
                }
                break;
            default:
                throw new ExEx(mess + " target casts from " +
                        iptype.name() + " to " + optype.name() + " are not allowed", loc);
            }
        }

        int         lshift = use_offsets ? ooffset - ioffset : 0;
        if (lshift == 0)
            tdelist.connect(tdev_new, tdev_old, sign_extend);
        else {
            TDEVar  stdev = new TDEVar(Long.valueOf(Math.abs(lshift)), Ptype.UINT, loc);
            tdelist.lshift(tdev_new, stdev, lshift, loc);
        }

        retval = new Val(null, Mode.VALUE, tdev_new, loc);
        retval.addExecSets(this);
        if (queues != null)
            retval.andSetQueues(queues, loc);
        retval.addOVars(this);
        return(retval);
    }
    
    /**
     * Mark any variable in the value as used.
     */
    public void setUsed () {
        if (ovars != null) {
            Iterator<?>    it = ovars.iterator();
            while (it.hasNext()) {
                Object o = it.next();
                if (o instanceof Var)
                    ((Var)o).setUsed();
                else {
                    ValWordSpecPair vp = (ValWordSpecPair)o;
                    Value           val = vp.val;
                    WordSpec        ws = vp.ws;
                    val.setUsed(ws);
                }
            }
        }
    }
    
    /**
     * Set the inarg flag indicating that this value is a module, procedure or function argument.
     */  
    public void setInarg () { inarg = true; }
    
    /**
     * Get the inarg flag which indicating that this value is a module, procedure or function argument.
     * @return the inarg flag
     */  
    public boolean isArg () { return(inarg); }
    
    /**
     * Get the queue read availability signal as a logical <b>Val</b>.
     * This is only called in one place in nodes/ExprNode.java when
     * evaluating the read availability operator.
     * @return  the queue read availability signal Val
     */
    public Val getReadAvailVal () {
        switch (mode) {
        case IMMEDIATE:
            break;
        case CMEMORY:
        case RMEMORY:
        case CLOCK:
            throw new ExEx("cannot get read availability of mode " + mode.modename(), loc);
        default:
            Val v = queues.getRAvail(ThreePL.getModuleScope(), loc);
            if (v != null)
                return(v);
        }
        return(new Val(true, loc));
    }
   
    /**
     * Get the Val as an examine, i.e. any queues in the value will
     * be examined, not read.
     * @return  the queue availability signal Val
     */
    public Val getExamVal () {
        if (tdevar == null)
            return(this);   // immediate arg - return unchanged
        if ((queues == null) || queues.isEmpty())
            return(this);   // no queues - return unchanged
        // empty the queue map
        queues = new QueueRefs();
        mode = Mode.VALUE;
        return(this);
    }
    
    
    /**
     * Convert a single value or a compound type to a one-dimensional array.
     * For a single value the array members are the same type and the single value
     * is duplicated to fill the array. For a compound type the type of the members
     * must all be the same.
     * @param ptype is the primitive value type
     * @param single is true if the input is a single value
     * @param n is the size of the resultant array
     * @param loc is the source file location
     * @return the value as a one-dimensional array
     */
    public Val toArray(Ptype ptype, boolean single, long n, SrcLoc loc) {
        Type        t = new Type("["+n+"]"+ptype.name().toLowerCase(), loc);
        WordSpec    ws = t.getWordSpec(null, loc);
        Object[]    nv = new Object[(int)n];
        Type[]      nt = new Type[(int)n];
        for (int i=0 ; i<n ; i++) {
            nv[i] = val[single ? 0 : i];
            nt[i] = val_type[single ? 0 : i];
        }
        return(new Val(nv, nt, ws, null, null, loc));
    }

}
