package threepl.codegen;

import static threepl.ThreePL.*;
import static threepl.parser.Functions.longFromDouble;

import java.util.ArrayList;
import java.util.TreeMap;

import threepl.exceptions.ExEx;
import threepl.exec.Clock;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.exec.WordSpec;
import threepl.exec.Var.IDtype;
import threepl.parser.Constant;
import threepl.parser.Functions;
import threepl.parser.SrcLoc;

/**
 * This class implements signals and signal arrays. It does not
 * create signal variables. At its simplest a TDEVar is an identifier
 * with an optional bit range.
 *
 * <p>A TDEVar allows us to specify one or more bits from a signal or
 * signal array.
 *
 * <p>To allow late optimisation a TDEVar can contain a constant rather than
 * a signal implementing a constant. This allows the code generator to make
 * optimisation decisions based on the value of the constant, a value which
 * would not be available if converted to a signal array.
 *
 * <p>For optimisation of control signals there are lists of TDEs to which
 * these signals are connected. These lists are in subclass Common. An
 * instance of this subclass is shared by all control signals which are
 * connected to the same net. The TDEs in turn have lists of the TDEVars
 * connected to their inputs and outputs. These bi-directional links are
 * only used for control signals, which are not derived from source code
 * variables and are unsubscripted.
 */
public class TDEVar implements Constant, TDEConstants {
    private String              id;         // The variable identifier if TDEVtype.VAR
    public  TDEVtype            type;       // var type - TDEVtype.VAR, TDEVtype.BITS, TDEVtype.INT etc.
    public  WordSpec            wordspec;
    public  Object              val;        // value if a constant
    private SrcLoc              loc;        // source file location
    private Common              common;     // sources and destination TDEs for connected signals


    // Static variables for GND and VCC. 
    public static TDEVar GND;
    public static TDEVar VCC;
    private static int   ccount = 0;    // integer used in creating identifier for unique constant
    
    
    // This map is of all TDEVars where the key is the ID and the entries are lists of TDEVars with that ID.
    // It enables TDEVars to have their IDs changed when TDEVars are merged.
    // On a merge TDEVars with the same ID as the merge destination TDEVar adopt
    // the ID of the merge source TDEVar.
    private static TreeMap<String,ArrayList<TDEVar>>   catalog = new TreeMap<String, ArrayList<TDEVar>>();
    
    /**
     * This class groups TDEVars which are connected.
     * The aliases variable is a list of the connected TDEVars.
     * The first entry in this list is the source TDEVar and hence
     * contains the preferred signal identifier.
     */
    public class Common {
        public Clock                clock_var;                          // clock domain variable
        public ArrayList<TDE>       dest = new ArrayList<TDE>();        // destination TDE(s) for this signal array
        public ArrayList<TDE>       src = new ArrayList<TDE>();         // source TDE(s) for this signal array
        private ArrayList<TDEVar>   links = new ArrayList<TDEVar>();    // TDEVars which are linked to this

        /**
         * Construct a new Common class.
         * @param   clk is the applicable clock domain
         * @param   tdev is the initial linked TDEVar
         */
        public Common (Clock clk, TDEVar tdev) {
            clock_var = clk;
            links.add(tdev);
        }

    }

    /**
     * Construct a TDEVar given a word specifier.
     * @param   id is the variable identifier.
     * @param   wordspec is the bit list
     * @param   loc is the source file location
     */
    public TDEVar (String id, WordSpec wordspec, SrcLoc loc) {
        this.id = id;
        this.loc = loc;
        this.wordspec = wordspec;
        common = new Common(getCurrentClockVar(), this);
        type = TDEVtype.VAR;
        enterInCatalog();
        //if (wordspec.numBits() == 0)
        //    throw new ExEx("SYSTEM ERROR - TDEVar zero width");
    }

    /**
     * Construct a TDEVar for a variable given a word specifier.
     * @param   var is the variable
     * @param   wordspec is the bit list
     * @param   loc is the source file location
     */
    public TDEVar (Var var, WordSpec wordspec, SrcLoc loc) {
        this.id = var.getEname();
        this.loc = loc;
        this.wordspec = wordspec;
        if (var.getMode() == Mode.VALUE)
            common = new Common(getCurrentClockVar(), this);
        else
            common = new Common(var.getOutputClkVar(loc), this);
        if (var.getMode() == Mode.CLOCK)
            type = TDEVtype.POS_CLK;
        else
            type = TDEVtype.VAR;
        if (wordspec.hasZeroWidthWord() && (wordspec.getCheckType().getPrimType() != Ptype.NULL) && (wordspec.getCheckType().getPrimType() != Ptype.EMPTY))
            throw new ExEx("Target variable '" + var.getID(IDtype.SLITERAL) + "' contains array of dimension 0");
        enterInCatalog();
    }

    /**
     * Construct a signal from a string.
     * @param   id is the variable identifier.
     * @param   loc is the source file location
     */
    public TDEVar (String id, SrcLoc loc) {
        this.id  = id;
        this.loc = loc;
        common = new Common(getCurrentClockVar(), this);
        type = TDEVtype.VAR;
        enterInCatalog();
    }

    /**
     * Construct a new TDEVar which represents a simple constant of type
     * LOG, BITS, INT, UINT, FIXED, UFIXED, FLOAT or ENUM.
     * @param   o is a Long, or Boolean whose value is the constant
     * @param   ptype is the primitive type of the constant
     * @param   loc is the source file location
     */
    public TDEVar (Object o, Ptype ptype, SrcLoc loc) {
        construct(o, ptype, 0, 0, 0, 0, null, null, loc);
    }

    /**
     * Construct a new TDEVar which represents a constant of type
     * LOG, BITS, INT, UINT, FIXED, UFIXED, FLOAT or ENUM. This really
     * only differs from the constructor above in that the full Type,
     * rather than primitive type, is passed, and this caters for the
     * FIXED, UFIXED, FLOAT and ENUM types where the ENUM IDs and ordinals
     * must be included.
     * @param   o is a Long, or Boolean whose value is the constant
     * @param   type is the type of the constant
     * @param   loc is the source file location
     */
    public TDEVar (Object o, Type type, SrcLoc loc) {
        int                 width = type.getWidth();
        int                 fixoffset = type.getFixOffset();
        int                 mant = type.getMantissaWidth();
        int                 bexp = type.getExponentWidth();
        Ptype               ptype = type.getPrimType();
        ArrayList<String>   eids = type.getEnumIDs();
        ArrayList<Long>     eords = type.getEnumOrds();
        construct(o, ptype, width, fixoffset, mant, bexp, eids, eords, loc);
    }
    
    /**
     * Construct a new TDEVar which represents a constant.
     * For type LOG the width argument is ignored.
     * For types BITS, UINT or INT the width argument may be 0 in which
     * case the minimum width for the constant will be used. If a
     * non-zero width is supplied it is checked against the actual
     * constant width to ensure it is large enough.
     * For types FIXED and UFIXED the constant may be integer or floating point.
     * For integers they will be left shifted by the offset. For floating
     * point the value is derived using the offset.
     * @param   o is a Long, Boolean or Double whose value is the constant
     * @param   ws is the word specification for the required word
     * @param   word is the index of the required word
     * @param   loc is the source file location
     */
    public TDEVar (Object o, WordSpec ws, int word, SrcLoc loc) {
        Ptype               ptype = ws.getPrimType(word);
        int                 width = ws.getWidth(word);
        int                 fixoffset = ws.getFixOffset(word);
        int                 mant = ws.getMantissaWidth(word);
        int                 bexp = ws.getExponentWidth(word);
        ArrayList<String>   eids = ws.getEnumIDs(word);
        ArrayList<Long>     eords = ws.getEnumOrds(word);
        construct(o, ptype, width, fixoffset, mant, bexp, eids, eords, loc);
    }
    
    /**
     * Construct a new TDEVar which represents a constant of type
     * FLOAT, FIXED or UFIXED.
     * @param   d is a Double whose value is the constant
     * @param   ptype is the primitive type, Ptype.FLOAT, Ptype.FIXED or Ptype.UFIXED
     * @param   width is the total width
     * @param   fixoffset is the FIXED or UFIXED binary point offset
     * @param   mant is the FLOAT mantissa
     * @param   bexp is the FLOAT biased exponent
     * @param   loc is the source file location
     */
    public TDEVar (Double d, Ptype ptype, int width, int fixoffset, int mant, int bexp, SrcLoc loc) {
        construct(d, ptype, width, fixoffset, mant, bexp, null, null, loc);
    }
    
    private void construct (Object o, Ptype ptype, int width, int fixoffset, int mant, int bexp, ArrayList<String> eids, ArrayList<Long> eords, SrcLoc loc) {
        long        ival;
        TDEVtype    t;
        switch (ptype) {
        case BITS:
            t = TDEVtype.BITS;
            ival = ((Long)o).longValue();
            if (width == 0)
                width = Functions.bits(ival, false);
            else if (width < Functions.bits(ival, false))
                throw new ExEx("constant too large for bits variable (" +
                                width + ":" + Functions.bits(ival, false) + ")", loc);
            break;
        case UINT:
            t = TDEVtype.UINT;
            ival = ((Long)o).longValue() << fixoffset;
            if (ival < 0)
                throw new ExEx("-ve constant for uint variable", loc);
            if (width == 0)
                width = Functions.bits(ival, false);
            else if (width < Functions.bits(ival, false))
                throw new ExEx("constant too large for uint variable (" +
                                width + ":" + Functions.bits(ival, false) + ")", loc);
            o = ival;
            break;
        case ENUM:
            t = TDEVtype.UINT;
            ival = ((Long)o).longValue();
            if (width == 0)
                width = Functions.bits(ival, false);
            break;
        case INT:
            boolean signed;
            ival = ((Long)o).longValue() << fixoffset;
            if (ival >= 0) {
                t = TDEVtype.UINT;
                ptype = Ptype.UINT;
                signed = false;
            } else {
                t = TDEVtype.INT;
                signed = true;
            }
            if (width == 0)
                width = Functions.bits(ival, signed);
            else if (width < Functions.bits(ival, signed))
            throw new ExEx("constant too large for int variable (" +
                            width + ":" + Functions.bits(ival, signed) + ")", loc);
            o = ival;
            break;
        case UFIXED:
            t = TDEVtype.UFIXED;
            if (o instanceof Double) {
                double  d = ((Double)o).doubleValue();
                ival = Math.round(d * ((long)1 << fixoffset));
            } else
                ival = (Long)o;
            if (ival < 0)
                throw new ExEx("-ve constant for ufixed variable", loc);
            if (width == 0)
                width = Functions.bits(ival, false);
            else if (width < Functions.bits(ival, false))
                throw new ExEx("constant too large for ufixed variable (" +
                                width + ":" + Functions.bits(ival, false) + ")", loc);
            o = ival;
            break;
        case FIXED:
            if (o instanceof Double) {
                double  d = ((Double)o).doubleValue();
                ival = Math.round(d * ((long)1 << fixoffset));
            } else
                ival = (Long)o;
            if (ival >= 0) {
                t = TDEVtype.UFIXED;
                ptype = Ptype.UFIXED;
                signed = false;
            } else {
                t = TDEVtype.FIXED;
                signed = true;
            }
            if (width == 0)
                width = Functions.bits(ival, true);
            else if (width < Functions.bits(ival, true))
                throw new ExEx("constant too large for fixed variable (" +
                            width + ":" + Functions.bits(ival, true) + ")", loc);
            o = ival;
            break;
        case FLOAT:
            t = TDEVtype.FLOAT;
            double  d;
            if (o instanceof Long)
                d = ((Long)o).doubleValue();
            else
                d = ((Double)o).doubleValue();
            ival = longFromDouble(d, mant, bexp);
            o = ival;
            break;
        case LOG:
            t = TDEVtype.BOOL;
            width = 1;
            break;
        case STR:
            throw new ExEx("target string type not allowed", loc);
        case PTR:
            throw new ExEx("target pointer type not allowed", loc);
        default:
            throw new ExEx("TDEVar() - type " + ptype.typename() + " ?", loc);
        }
        Type    newt = new Type(ptype, 1, fixoffset, mant, bexp, eids, eords);
        newt.setWidth(width);
        id = "CONST" + ccount++;
        type = t;
        this.loc = loc;
        common = new Common(getCurrentClockVar(), this);
        wordspec = new WordSpec(newt, null);
        val = o;
        wordspec.setComplete();
        enterInCatalog();
    }
    
    /**
     * Return a TDEVar given an identifier string and WordSpec. If an equivalent TDEVar
     * is already in the catalog, return that, otherwise construct a new TDEVar.
     * @param   id is the variable identifier.
     * @param   wordspec is the bit list
     * @param   loc is the source file location
     * @return the TDEVar
     */
    static public TDEVar makeTDEVar (String id, WordSpec wordspec, SrcLoc loc) {
        TDEVar  tdev = checkCatalog(id, wordspec);
        if (tdev != null)
            return(tdev);
        return(new TDEVar(id, wordspec, loc));
    }

    /**
     * Return a TDEVar given a Var and a WordSpec. If an equivalent TDEVar
     * is already in the catalog, return that, otherwise construct a new TDEVar.
     * @param   var is the variable
     * @param   wordspec is the bit list
     * @param   loc is the source file location
     * @return the TDEVar
     */
    static public TDEVar makeTDEVar (Var var, WordSpec wordspec, SrcLoc loc) {
        String  id = var.getEname();
        TDEVar  tdev = checkCatalog(id, wordspec);
        if (tdev != null)
            return(tdev);
        return(new TDEVar(var, wordspec, loc));
    }
    
    /**
     * See if this identifier is a catalog key.
     * If not, make a new TDEVar (which will be entered into the catalog).
     * If the id is found, scan the associated list entries. If none has the same bit pair list
     * make a new TDEVar.
     * If a matching TDEVar with the same bit pair list is found, get the type of the
     * requested WordSpec (t1) and the type of the catalog entry WordSpec (t2).
     * If t1 is null, use the catalog entry
     * If t2 is null, make a new TDEVar.
     * If t1 and t2 specify different word counts, make a new TDEVar.
     * If the primitive types of t1 and t2 differ, make a new TDEVar.
     * A type mismatch can occur legitimately with say a field from a struct
     * and a primitive which is the same as the type field or with an array with target
     * subscripts.
     * It is not important if a new TDEVar duplicates a catalog entry as the TDEVars will
     * match by name and by bit pairs when converted to the netlist. The advantage of re-using
     * TDEVars in the catalog where convenient to do so is that the intermediate code list is
     * simpler and the printed form is easier to read. Either way conversion of the intermediate
     * code list into the final EDIF netlist is not affected.
     *
     * @param id    the TDEVar identifier, either the target variable identifier or a generated
     *              signal name
     * @param ws    the WordSpec describing the TDEVar
     * @return      the catalog entry TDEVar, or null if no appropriate entry found
     */
    static private TDEVar checkCatalog (String id, WordSpec ws) {
        ArrayList<TDEVar>  al = catalog.get(id);
        if (al != null)
            for (TDEVar tdev : al) {
                if (tdev.getWordSpec().bitFieldsEqual(ws)) {
                    Type    t1 = ws.getType();
                    Type    t2 = tdev.getWordSpec().getType();
                    if (t1 == null)
                        return(tdev);
                    if (t2 == null)
                        continue;
                    if ((t1.numWords() > 1) || (t2.numWords() > 1))
                        continue;
                    if (t1.getPrimType() != t2.getPrimType())
                        continue;
                    return(tdev);
                }
            }
        return(null);
    }
    
    /**
     * Initialise static variables.
     */
    public static void init () {
        GND = new TDEVar(false, Ptype.LOG, null);
        GND.id = "GND";
        VCC = new TDEVar(true, Ptype.LOG, null);
        VCC.id = "VCC";
    }
    
    /**
     * Change the identifier.
     * Used in procs/NegClockProc.java. Also called in exec/Queue to change signal
     * IDs *.PUSH to *.WPEND and *.POP to *.RPEND for better intermediate code
     * readability for unbuffered queues.
     * @param   name is the new identifier
     */
    public void setID (String name) {
        ArrayList<TDEVar>   al = catalog.get(id);
        catalog.remove(id);
        for (TDEVar tdev : al)
            tdev.id = name;
        catalog.put(name, al);
    }
    
    /**
     * Get the source file location attached to this signal.
     * @return  the source file location
     */
    public SrcLoc getSrcLoc () {
        return(loc);
    }
    
    /**
     * Check a TDEVar to see if it is linked to this TDEVar.
     * @param   tdev is the TDEVar
     * @return  true if they are linked control signals
     */
    public boolean equalOrLinked (TDEVar tdev) { return((tdev == this) || (tdev.common == common)); }
    
    /**
     * Get the clock variable associated with this clock signal.
     * Only called by simulator/Sim.java to get parameters for
     * a clock signal.
     * @return  the clock variable
     */
    public Clock getClkVar () { return(common.clock_var); }

    /**
     * Get the clock signal associated with this signal.
     * @return  the clock signal
     */
    public TDEVar getClkSig () { return(common.clock_var.getClkSig()); }
    
    /**
     * Set the clock variable associated with this signal.
     * The clock variable is automatically set by the constructor
     * to the current clock. In a few cases the clock associated
     * with a signal is not the current clock, in which case it must
     * be explicitly set using this method.
     * @param   v is the new clock variable to be associated with this signal
     */
    public void setClkVar (Clock v) { common.clock_var = v; }
    
    /**
     * Set the clock mode.
     * @param  cmode is the clock mode - POS, NEG or NOT
     */
    public void setClockMode (ClkType cmode) {
        switch (cmode) {
        case POS:
            type = TDEVtype.POS_CLK;
            break;
        case NEG:
            type = TDEVtype.NEG_CLK;
            break;
        case NOT:
            break;
        }
    }
    
    /**
     * Get the clock mode.
     * @return  the clock mode
     */
    public ClkType getClockMode () {
        switch (type) {
        case POS_CLK:
            return(ClkType.POS);
        case NEG_CLK:
            return(ClkType.NEG);
        default:
            return(ClkType.NOT);
        }
    }
    
    /**
     * Construct a new TDEVar which specifies one word from the
     * instance TDEVar.
     * @param   index is the word index
     * @param   loc is the source file location
     * @return  a TDE variable with the same identifier as the class instance
     *          but with a single word
     */
    public TDEVar getWord (int index, SrcLoc loc) {
        // If this TDEVar is a constant it will be just one word
        // and can be returned as the requested word TDEVar, 'index'
        // being ignored.
        if (type != TDEVtype.VAR)
            // a constant or a clock
            return(this);
        
        // If there is no wordSpec or the wordSpec for this TDEVar only contains one word
        // then just return this TDEVar.
        // The index will always be zero, but it is tested anyway just in case.
        if ((wordspec == null) || ((wordspec.numWords() == 1) && (index == 0)))
            return(this);

        TDEVar  v = makeTDEVar(id, wordspec.getWordWordSpec(index), loc);
        v.type = type; // in case POS_CLK or NEG_CLK
        return(v);
    }
    
    /**
     * Get one word from a TDEVar, adjusting the width, offset, exponent or mantissa
     * if required.
     * If the entire variable is required unchanged then this TDEVar is returned unchanged.
     * If one word is required unchanged then a new TDEVar with single word WordSpec is constructed
     * and returned, 'complete' on the WordSpec being false.
     * Otherwise a new signal array "E..." is generated with the required WordSpec with a suitable
     * CONNECT or OP TDE to do conversion. The WordSpec will have 'complete' false.
     * @param   ws is the word specification for the required word
     * @param   word is the index of the required word
     * @param   loc is the source file location
     * @return  a TDE variable representing one word of the class instance
     *          TDE variable, possibly truncated or padded
     *          
     */
    public TDEVar getWord (WordSpec ws, int word, SrcLoc loc) {
        int         old_offset = wordspec.getFixOffset(word);
        int         new_width = ws.getWidth(word);
        int         new_offset = ws.getFixOffset(word);
        int         new_mant = ws.getMantissaWidth(word);
        int         new_bexp = ws.getExponentWidth(word);

        if ((type != TDEVtype.VAR) && (type != TDEVtype.POS_CLK) && (type != TDEVtype.NEG_CLK)) {
            // Is a constant.
            if (old_offset == new_offset)
                return(this);

            // Have (unequal) offsets so must be FIXED or UFIXED -
            // adjust the constant.
            long    ival = ((Long)val).longValue();
            if (old_offset > new_offset)
                ival >>= old_offset - new_offset;
            else
                ival <<= new_offset - old_offset;
            return(new TDEVar(Long.valueOf(ival), ws, word, loc)); // ID is CONST....
        }
        
        // If ws has only one word of the required size simply return this TDEVar.
        if ((ws.numWords() == 1) && (ws.numBits() == wordspec.numBits()) && (word == 0))
            return(this);

        WordSpec    nws = wordspec.getWordWordSpec(word);   // WordSpec for single word
        TDEVar      otdev = makeTDEVar(id, nws, loc);
        otdev.type = type; // in case POS_CLK or NEG_CLK
        
        // If an enum type, simply return as will not be resized.
        if (wordspec.getPrimType(word) == Ptype.ENUM) {
            return(otdev);
        }
        
        return(otdev.resize(new_width, new_offset, new_bexp, new_mant, loc));
    }
    
    /**
     * Resize a single range signal array. This TDEVar has already been tested
     * for a match with the required width, offset etc in which case resize()
     * will not have been called.
     * The TDEVar of the new signal array is returned and a cast TDE is created
     * and entered into the TDE list.
     * @param   new_width is the required new width
     * @param   new_offset is the required new fixed point offset
     * @param   new_bexp is the new floating point biased exponent
     * @param   new_mant is the new floating point mantissa
     * @param   loc is the source file location
     * @return  the resized signal array
     */
    private TDEVar resize (int new_width, int new_offset, int new_bexp, int new_mant, SrcLoc loc) {
        if (numWords() > 1)
            throw new ExEx("system error - cannot resize multiword TDEVar", loc);

        WordSpec    old_ws = wordspec;
        int         old_width = wordspec.numBits();
        int         old_offset = wordspec.getFixOffset(0);
        int         old_mant = wordspec.getMantissaWidth(0);
        int         old_bexp = wordspec.getExponentWidth(0);

        Ptype   ptype = old_ws.getPrimType(0);

        Var     var = old_ws.getVar();
        if ((type != TDEVtype.VAR) && (old_ws.getWidth(0) > new_width))
            throw new ExEx("constant is too large", loc);
        WordSpec    nws = new WordSpec(0, 0, new_width-1, new_offset, new_mant, new_bexp, null, null, ptype, var);   // new word
        TDEVar      ntdev = tdelist.signal("S", nws, loc);
        ntdev.type = type;
        
        if (new_mant != 0) {
            //----------------------------------------------------------------------------------------------
            // A floating point resize is required.
            // Connect sign bits - old to new.
            //----------------------------------------------------------------------------------------------
            Type    old_type;
            Type    new_type;
            int     bexp_exp = new_bexp - old_bexp;
            
            old_type = new Type("(m, uint:" + old_mant + ", e, uint:" + (old_bexp-1) + ", es, uint:1, s, log)", loc);
            if (bexp_exp > 0)
                new_type = new Type("(m, uint:" + new_mant + ", e, uint:" + (old_bexp-1) + ", ef, uint:" + bexp_exp + ", es, uint:1, s, log)", loc);
            else
                new_type = new Type("(m, uint:" + new_mant + ", e, uint:" + (new_bexp-1) + ", es, uint:1, s, log)", loc);
            WordSpec    tmp_old_ws = old_type.getWordSpec(null, loc);
            WordSpec    tmp_new_ws = new_type.getWordSpec(null, loc);
            TDEVar      tmp_old_tdev = tdelist.signal("FPCONVIN", tmp_old_ws, loc);
            TDEVar      tmp_new_tdev = tdelist.signal("FPCONVOUT", tmp_new_ws, loc);
            
            // connect input TDEVar to intermediate TDEVar
            tdelist.connect(tmp_old_tdev, this);
            
            // connect sign bit from old to new
            tdelist.connect(tmp_new_tdev.getWord((bexp_exp > 0) ? 4 : 3, loc), tmp_old_tdev.getWord(3, loc));
            
            // connect biased exponent sign bit from old to new
            tdelist.connect(tmp_new_tdev.getWord((bexp_exp > 0) ? 3 : 2, loc), tmp_old_tdev.getWord(2, loc));
            
            // Connect biased exponent excluding sign, possibly resized.
            // Treat as signed to replicate top bit if padding required.
            // For smaller biased exponent keep MSB and delete following bits.
            // For larger biased exponent keep MSB and and insert copies of
            // inverted MSB.
            //
            // Examples -
            // 5 bits - bias 01111     4 bits bias 0111
            // 
            //     2's comp    +bias
            // +15  01111      11110
            // +14  01110      11101
            // +13  01101      11100
            // +12  01100      11011
            // +11  01011      11010
            // +10  01010      11001
            //  +9  01001      11000
            //  +8  01000      10111  2's comp    +bias
            //  +7  00111      10110   0111    1110
            //  +6  00110      10101   0110    1101
            //  +5  00101      10100   0101    1100
            //  +4  00100      10011   0100    1011
            //  +3  00011      10010   0011    1010
            //  +2  00010      10001   0010    1001
            //  +1  00001      10000   0001    1000
            //   0  00000      01111   0000    0111
            //  -1  11111      01110   1111    0110
            //  -2  11110      01101   1110    0101
            //  -3  11101      01100   1101    0100
            //  -4  11100      01011   1100    0011
            //  -5  11011      01010   1011    0010
            //  -6  11010      01001   1010    0001
            //  -7  11001      01000
            //  -8  11000      00111
            //  -9  10111      00110
            // -10  10110      00101
            // -11  10101      00100
            // -12  10100      00011
            // -13  10011      00010
            // -14  10010      00001
            
            if (bexp_exp > 0)
                tdelist.connect(tmp_new_tdev.getWord(2, loc), tdelist.inv(tmp_old_tdev.getWord(2, loc), loc), true);
            tdelist.connect(tmp_new_tdev.getWord(1, loc), tmp_old_tdev.getWord(1, loc), false);
            
            // Connect mantissa, possibly resized.
            // Must be truncated or padded at the least significant end.
            Type        nmt = new Type("uint:"+new_mant, loc);
            WordSpec    nmws = nmt.getWordSpec(null, loc);
            TDEVar      nmtdev = tdelist.signal("FPMANT", nmws, loc);
            int mshift = new_mant - old_mant;   // +ve for left shift, -ve for right shift
            tdelist.lshift(tmp_new_tdev.getWord(0, loc), nmtdev.getWord(0, loc), mshift, loc);
            
            // connect intermediate TDEVar to output TDEVar
            tdelist.connect(ntdev, tmp_new_tdev);

            return(ntdev); 
        }
        
        //-----------------------------------------------------
        // Not floating point - must be integer or fixed point.
        //-----------------------------------------------------
        
        // Check for potential lost fixed point data.
        if ((ptype == Ptype.UFIXED) || (ptype == Ptype.FIXED)) {
            if ((new_width - new_offset) < (old_width - old_offset)) {
                rpt("\t" + loc.toString() + " - WARNING");
                rpt("\tfixed point truncation of most significant bits\n");
            }
            if (new_offset < old_offset) {
                rpt("\t" + loc.toString() + " - WARNING");
                rpt("\tfixed point truncation of least significant bits\n");
            }
        } else if ((new_width < old_width) && boolDir("intTruncWarning")) {
            rpt("\t" + loc.toString() + " - WARNING");
            rpt("\tnumerical truncation\n");
        }
        
        if (new_offset == old_offset) {
            //----------------------------------------------------------------------------------------------
            // No fixed point shift required, just a resize.
            // UINT, INT, UFIXED or FIXED
            //----------------------------------------------------------------------------------------------
            boolean signed = (ptype == Ptype.INT) || (ptype == Ptype.FIXED);
            tdelist.connect(ntdev,  this, signed);
            return(ntdev); 
        }
        
        //----------------------------------------------------------------------------------------------
        // Fixed point shift, and possible resize, required.
        // UFIXED or FIXED
        //----------------------------------------------------------------------------------------------
        int     lshift = new_offset - old_offset;
        tdelist.lshift(ntdev, this, lshift, loc);
        return(ntdev); 
    }
   
    /**
     * Get the word specification for this signal
     * @return  word specification
     */
    public WordSpec getWordSpec () { return(wordspec); }
    
    /**
     * Set the word specification for this signal
     * @param  ws id the word specification
     */
    public void setWordSpec (WordSpec ws) { wordspec = ws; }
    
    /**
     * Get the number of words in the WordSpec of this signal.
     * @return  the number of words
     */
    public int numWords () {
        if (wordspec == null)
            return(1);
        return(wordspec.numWords());
    }
    
    /**
     * Get the total number of bits in the WordSpec of this signal.
     * @return  the number of bits
     */
    public int numBits () {
        if (wordspec == null)
            return(1);
        return(wordspec.numBits());
    }

    /**
     * Get the number of bits in a particular word of the WordSpec
     * of this signal.
     * @param   i is the index of the required word
     * @return  the number of bits in the word
     */
    public int getWidth (int i) {
        if (wordspec == null)
            return(1);
        return(wordspec.getWidth(i));
    }

    /**
     * Get one (the only) destination TDE. Return null if the destination list
     * is empty or has more than one entry.
     * @return the destination TDE
     */
    public TDE getSingleDest() {
        if (common.dest.size() == 1)
            return(common.dest.get(0));
        return(null);
    }

    /**
     * Get one (the only) source TDE. Return null if the source list is empty
     * or has more than one entry.
     * @return  the source TDE
     */
    public TDE getSingleSrc() {
        if (common.src.size() == 1)
            return(common.src.get(0));
        return null;
    }

    /**
     * Get the source TDE list.
     * @return  the source TDE ArrayList
     */
    public ArrayList<TDE> getSrcList () { return(common.src); }

    /**
     * Get the source TDE list size.
     * @return  the source TDE ArrayList size
     */
    public int getSrcListSize () { return(common.src.size()); }

    /**
     * Get the destination TDE list.
     * @return  the destination TDE ArrayList
     */
    public ArrayList<TDE> getDestList () { return(common.dest); }

    /**
     * Get the destination TDE list size.
     * @return  the destination TDE ArrayList size
     */
    public int getDestListSize () { return(common.dest.size()); }

    /**
     * Add a destination TDE to the destination list of the TDEVar
     * @param   tde is the destination TDE
     */
    public void addDest(TDE tde) {
        common.dest.add(tde);
    }

    /**
     * Add a source TDE to the source set of the TDEVar
     * If the TDE is not a CONNECT make this ID the aliased ID in the catalog.
     * @param   tde is the source TDE
     */
    public void addSrc(TDE tde) {
        if (common.src.size() != 0)
            throw new ExEx("'" + id + "' has more than one source");
        common.src.add(tde);
    }
    
    /**
     * Merge a destination TDEVar with this source TDEVar. This method is only called during
     * TDE list optimisation following immediate execution. The destination is linked to the
     * source so that instances of the destination TDEVar appear with the ID of the source
     * TDEVar in the TDE list.
     * 
     * Both the source TDEVar (this) and destination TDEVar (dtdev) may already have
     * other TDEVars linked via their respective Commons as a result of previous
     * merges. As a minimum every TDEVar has a link to itself in its Common.
     * 
     * The merge looks at the source TDE of the destination TDEVar and if it is not
     * CONNECT then it is assumed to be a data source TDE and that TDEVar ID is used
     * as the preferred ID. Otherwise the source TDEVar ID is the preferred ID.
     * NOTE: this actually never occurs so is commented out!
     * 
     * The source TDEVar will already have the same ID, type, WordSpec, Common class
     * instance and val field in all linked TDEVars as result of previous merges.
     * All linked destination TDEVars now have the source ID, type, WordSpec, Common
     * and val field written into them. The new merged TDEVar will now have the same
     * is, type, WordSpec, Common and val fields.
     * 
     * Now that the destination TDEVars have had their IDs changed, any other instances
     * of a TDEVar with the same ID, regardless of linkages or WordSpec, must be have
     * their IDs changed as well. This is done using the catalog map.
     * 
     * @param   dtdev is the destination TDEVar
     */
    public void merge (TDEVar dtdev) {
        //if (!complete)
        //    System.out.println("SRC incomplete " + id);
        //if (!dtdev.complete)
        //    System.out.println("DEST incomplete " + dtdev.id);
        if (!tdelist_optimisation_started)
            throw new ExEx("TDEVar merge prior to TDE list optimisation!");

        Common  dc = dtdev.common;   // destination common
        Common  sc = common;         // source common
        String  old_dest_id = dtdev.id;
        String  sid = id;
        // The following never occurs, so it is commented out
        //if (!dc.src.isEmpty() && (dc.src.get(0).getType() != TDEType.CONNECT))
        //    sid = dtdev.getId();
        
        sc.src.addAll(dc.src);                  // add source TDEs
        sc.dest.addAll(dc.dest);                // add destination TDEs
        sc.links.addAll(dc.links);              // link destination TDEVar links to the source of this TDEVar
        
        for (TDEVar t : dc.links) {
            t.id = sid;
            t.type = type;
            t.wordspec = wordspec;
            t.common = sc;
            t.val = val;
        }
        
        // Update all occurrences of destination TDEVars with the same id
        // with the new source id.
        ArrayList<TDEVar>   al = catalog.get(old_dest_id);
        for (TDEVar tdev : al) {
            if (tdev == dtdev)
                continue;
            tdev.id = sid;
        }
    }
    
    /**
     * Check if this TDEVar is for a single signed value.
     * A compound value returns false.
     * @return true if signed
     */    
    public boolean isSigned () {
        if (numWords() != 1)
            return(false);
        Ptype   ptype = wordspec.getPrimType(0);
        switch (ptype) {
        case INT:
        case FIXED:
            return(true);
        default:
            return(false);
        }
    }
    
    /**
     * Check if this TDEVar is connected to GND or is constant boolean false.
     * @return  true if connected to GND
     */
    public boolean isGND () {
        if (common == TDEVar.GND.common)
            return(true);
        if (id.equals("GND"))
            return(true);
        // Is it constant TRUE.
        return((type == TDEVtype.BOOL) && (!(boolean)val));
    }
    
    /**
     * Check if this TDEVar is connected to VCC.
     * @return  true if connected to VCC
     */
    public boolean isVCC () {
        if (common == TDEVar.VCC.common)
            return(true);
        if (id.equals("VCC"))
            return(true);
        // Is it constant TRUE.
        return((type == TDEVtype.BOOL) && (boolean)val);
    }
    
    /**
     * Check if this TDEVar is connected to one or more other TDEVars.
     * This is the case if common.links has more than one entry (there is a self entry).
     * @return  true if this TDEVar is connected to one or more other TDEVars
     */
    public boolean isConnected () { return(common.links.size() > 1); }
    
    /**
     * Determine if this TDEVar represents an entire variable.
     * @return this TDEVar represents an entire variable.
     */
    public boolean isComplete () {
        if (wordspec == null)
            return(true);
        return(wordspec.isComplete());
    }

    /**
     * Get the source TDEVar ID of a signal.
     * @return the source TDEVar ID.
     */
    public String getId () { return(id); }
    
    /**
     * Clear the source TDE list.
     */
    public void clearSrcList() {common.src.clear();}

    /**
     * Clear the destination TDE list.
     */
    public void clearDestList() { common.dest.clear(); }
    
    /**
     * Determine if the bit pairs in this TDEVar are the same as those in another TDEVar.
     * @param tdev the TDEVar to compare with this
     * @return true if the bit pairs are the same
     */
    public boolean bitFieldsEqual (TDEVar tdev) {
        return(wordspec.bitFieldsEqual(tdev.getWordSpec()));
    }
    
    /**
     * Check if this TDEVar is a constant.
     * @return  true if this TDEVar is a constant
     */
    public boolean isConst () {
        return((type != TDEVtype.VAR) && (type != TDEVtype.POS_CLK) && (type != TDEVtype.NEG_CLK));
    }

    /**
     * Construct a string representing a one bit signal.
     * @return  a string which is the signal identifier followed by the
     *          bit number in parentheses
     */
    public String getIdBit () { return(getIdBit(0)); }

    /**
     * Construct a string representing one bit of a signal.
     * @param   bit is the absolute bit number
     * @return  a string which is the signal identifier followed by the
     *          bit number in parentheses
     */
    public String getIdBit (int bit) {
        if (!tdelist_optimisation_finished)
            throw new ExEx("Netlist extraction from TDEVar prior to end of optimisation");
        if (wordspec == null) {
            if (bit == 0)
                return(id);
            else
                throw new ExEx("getIdBit() error!");
        }

        switch(type) {
        case POS_CLK:
        case NEG_CLK:
            if (bit == 0)
                return(id);
            else
                throw new ExEx("getIdBit() error!");
        case VAR:
            return(id + "[" + (wordspec.getLower(0) + bit) + "]");
        case BITS:
        case UINT:
        case INT:
        case UFIXED:
        case FIXED:
        case FLOAT:
            long    v = ((Long)val).longValue();
            v >>= bit;
            if ((v & 1) == 0)
                return("GND");
            else
                return("VCC");
        case BOOL:
            if (((Boolean)val).booleanValue())
                return ("VCC");
            else
                return ("GND");
        default:
            throw new ExEx("getIdBit() error!");
        }
    }

    /**
     * Get the type.
     * @return the type, which is TDEVtype.VAR, TDEVtype.BITS, TDEVtype.UINT,
     *          TDEVtype.INT, TDEVtype.UFIXED, TDEVtype.FIXED or TDEVtype.BOOL.
     */
    public TDEVtype getType () { return(type); }
    
    /**
     * Set the constant value field.
     * @param  o is the object to assign to the value field
     */
    public void setVal (Object o) { val = o; }
    
    /**
     * Get the constant value, i.e. the val field. If the TDEVar represents a
     * constant this field is an Object holding the value. If the TDEVar is a
     * variable this field is null.
     * @return  the constant value field
     */
    public Object getConst () { return(val); }
    
    /**
     * Get the constant value as an immediate mode Val.
     * @return   the constant Val
     */
    public Val getConstVal () {
        Object      o;
        TDEVtype    t;
        o = val;
        t = type;
        switch (t) {
        case BITS:
        case UINT:
            return(new Val((Long)o, Ptype.UINT, loc));
        case INT:
            return(new Val((Long)o, Ptype.INT, loc));
        case UFIXED:
        case FIXED:
            int     fixoffset = wordspec.getFixOffset(0);
            long    l = ((Long)o).longValue();
            double  d = (double)l / (double)(1 << fixoffset);
            return(new Val(d, loc));
        case FLOAT:
            return(new Val((Double)o, loc));
        case BOOL:       
            return(new Val((Boolean)o, loc));
        case NEG_CLK:
            break;
        case POS_CLK:
            break;
        case VAR:
            break;
        }
        return(null);
    }

    /**
     * Remove a TDE from the destination TDE list.
     * @param tde the TDE to be removed
     */
    public void removeDest(TDE tde) {
        int     i = common.dest.indexOf(tde);
        if (i < 0)
            throw new ExEx("SYSTEM ERROR");
        common.dest.remove(i);
    }

    /**
     * Remove a TDE from the source TDE list.
     * @param tde the TDE to be removed
     */
    public void removeSrc(TDE tde) {
        int     i = common.src.indexOf(tde);
        if (i < 0)
            throw new ExEx("SYSTEM ERROR");
        common.src.remove(i);
    }
 
    /**
     * Enter this TDEVar in the catalog.
     */
    private void enterInCatalog () {
        //if (tdelist_optimisation_started)
        //    throw new ExEx("TDEVar creation during TDE list optimisation!");
        ArrayList<TDEVar>   al;
        if (!catalog.containsKey(id)) {
            al = new ArrayList<TDEVar>();
            catalog.put(id, al);
        } else {
            al = catalog.get(id);
            //id = al.get(0).id;
        }
        al.add(this);
    }
    
    /**
     * Generate a string version of the TDeVar for printing in a
     * declaration. A variable subscript range is printed explicitly. It must
     * be of type tdeVAR and have no range or a single range. The upper range
     * limit is incremented to form the dimension, the lower limit being
     * ignored.
     * @return a string representation of the TDE variable.
     */
    public String toDecString() {
        StringBuffer b = new StringBuffer();
        int          dimension;

        b.append(id); // actually this will just be the TDEVar id when this method is called
        if ((type != TDEVtype.POS_CLK) && (type != TDEVtype.NEG_CLK) && (getWordSpec() != null)) {
            dimension = getWordSpec().getHighestBit() + 1;
            b.append("[" + dimension + "]");
        }
        return (b.toString());
    }

    /**
     * Generate a string version of the TDE variable for printing. A variable
     * subscript range where the upper and lower limits are equal is printed
     * as a single subscript. If there is a source link, follow the links
     * to get the actual source signal.
     * @return a string representation of the TDE variable.
     */
    public String toString () {
        StringBuffer b = new StringBuffer();
        switch(type) {
        case NEG_CLK:
            b.append("-");
            b.append(id);
            return (b.toString());
        case POS_CLK:
            b.append(id);
            return (b.toString());
        case VAR:
            b.append(id);
            if (wordspec != null) {
                b.append("[");
                b.append(wordspec.toString());
                b.append("]");
            }
            return(b.toString());
        case BITS:
        case UINT:
        case INT:
            return(val.toString());
        case UFIXED:
        case FIXED:
            long    fixoffset = wordspec.getFixOffset(0);
            long    fscale = (long)1 << fixoffset;
            long    v = ((Long)val).longValue();
            long    ipart = v >> fixoffset;
            long    fpart = v & (fscale - 1);
            return(String.valueOf(ipart) + ".(" + String.valueOf(fpart) + "/" + fscale + ")");
        case FLOAT:
            return (val.toString());
        case BOOL:
            if (((Boolean)val).booleanValue())
                return("VCC");
            else
                return("GND");
        }

        return("");
    }
    
    /**
     * Get the associated target mode variable.
     * @return the associated target mode variable
     */
    public Var getVar () {
        if (wordspec == null)
            return(null);
        return(wordspec.getVar());
    }
}
