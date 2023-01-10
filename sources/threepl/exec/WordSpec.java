package threepl.exec;

import static threepl.ThreePL.tdelist;
import static threepl.parser.Functions.binToHex;
import static threepl.parser.Functions.bits;
import static threepl.parser.Functions.zeropad;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

import threepl.codegen.TDE;
import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.parser.Constant;
import threepl.parser.Functions;
import threepl.parser.SrcLoc;

/**
 * This class implements a list of pairs and a list of Integers to
 * select words from any type.
 *
 * <p>The Integer list 'words' contains the indices of words selected from the type.
 *
 * <p>The pairs list represent ranges of bits associated with the words. Each pair
 * also contains a list of subscript values and field identifiers that
 * select that particular word and bit pair.
 * 
 * <p> The types list contains the type of each word.
 *
 * <p>Pairs can be extracted explicitly or can be iterated through.
 * Bit pairs can also be extracted explicitly or the complete bit sequence
 * can be stepped through by means of an associated iterator.
 *
 * <p>The class also contains a dimensional description array
 * and a Type for type checking purposes. In the case of a variable reference
 * 'checktype' will differ from the variable type if the variable is a compound
 * type and the whole variable is not referenced (e.g. a subarray).
 *
 * <p>The dimensional description array describes the dimensionality
 * of the value or reference to which the pair list applies. The
 * dimensional description array has one dimension for each subscript
 * range (including missing subscripts) in a variable reference. Single
 * subscripts do not contribute to the dimensionality.
 *
 * <p>At the end of a reference there must be a single type, either a primitive
 * type or else a struct. This is referred to as the "check type" as it is used
 * to check WordSpec compatibility for compound assignment.
 *
 * <p>Values and references are only compatible for purposes such as
 * assignment or parameter/argument matching if the total number
 * of bits in their pair lists are the same, the dimensional descriptions
 * are the same and their check types are the same (unless type conversion
 * is to be catered for).
 *
 * <p>A WordSpec created directly from a Type, or from an immediate variable,
 * need not have bit pairs (bitpairs == null).
 *
 * <p>A wordspec created from a target variable will have bit pairs which are
 * set from the component widths.
 *
 * <p>A wordspec for a primitive variable or expression value will have a single
 * word list entry (0), and if a target variable or expression a single Pair
 * list entry giving the bit range for the single word (0 to dimension-1).
 *
 * <p>Where a variable instance has target variable subscripts, these are stored
 * in this class in a list.
 */
public class WordSpec implements Constant, TDEConstants {
    private ArrayList<Integer>  words;
    private ArrayList<Pair>     bitpairs;
    private ArrayList<Type>     types;
    private int[]               dim_des;    // dimensional description
    private Type                check_type; // type to check for assignment
    private Type                type;       // type
    private boolean             complete;   // this is a complete variable specification, not a part
    private Var                 var;        // associated variable or null
    
    private ArrayList<Val>      subvals;    // variable subscripts
    private QueueRefs           squeues;    // queues from target subscripts
    
    // fields used for target variable subscript processing
    private TDEVar[]    tsselects;  // target subscript permutation selects
    private int[]       tsdim;      // dimensions for each variable subscript
    private int[]       tswidths;   // min widths for each variable subscript
    private int[]       tsdvals;    // decoder values for each permutation
    private int         tsnperms;   // number of permutations

    // Static variables for frequently used primitive types 
    public static WordSpec BITS = new WordSpec(Ptype.BITS);
    public static WordSpec UINT = new WordSpec(Ptype.UINT);
    public static WordSpec INT = new WordSpec(Ptype.INT);
    public static WordSpec FLOAT = new WordSpec(Ptype.FLOAT);
    public static WordSpec ILOG = new WordSpec(Ptype.LOG);      // immediate type LOG
    public static WordSpec TLOG = new WordSpec(1, Ptype.LOG);   // target type LOG
    public static WordSpec STR = new WordSpec(Ptype.STR);
    
    /**
     * Construct an empty word specification.
     */
    public WordSpec () {
        words = new ArrayList<Integer>();
        bitpairs = new ArrayList<Pair>();
        types = new ArrayList<Type>();
        subvals = new ArrayList<Val>();
        squeues = new QueueRefs();
    }
 
    /**
     * Construct a target word specification containing one word.
     * @param   ptype is the element primitive type
     */
    public WordSpec (Ptype ptype) {
        this();
        Type    t = new Type(ptype, 0);
        check_type = t;
        add(true, 0, 0, 0, t);
        complete = true;
    }

    /**
     * Construct a target word specification containing one word and one
     * associated bit pair given lower and upper bit bounds.
     * @param   w is the word index
     * @param   l is the lower bit index of the pair
     * @param   u is the upper bit index of the pair
     * @param   fixoffset is the number of fraction bits in UFIXED or FIXED type
     * @param   mant is the number of mantissa bits in FLOAT type
     * @param   bexp is the number of exponent bits in FLOAT type
     * @param   enumIDs is a list of identifiers for an enumerated type
     * @param   enumOrds is a list of ordinal values for an enumerated type
     * @param   ptype is the element primitive type
     * @param   v is the associated variable if any
     */
    public WordSpec (int w, int l, int u, int fixoffset, int mant, int bexp, ArrayList<String> enumIDs, ArrayList<Long> enumOrds, Ptype ptype, Var v) {
        this();
        Type    t = new Type(ptype, u-l+1, fixoffset, mant, bexp, enumIDs, enumOrds);
        check_type = t;
        var = v;
        add(true, w, l, u, t);
    }

    /**
     * Construct a target word specification containing one word and one
     * associated bit pair given a width (bit pair from 0 to width-1).
     * If the width is {@code <= 0} a bit pair is not added. If the primitive
     * type is Ptype.LOG a width of 1 is used. For Ptype.LOG or
     * Ptype.ENUM it is not known whether the mode is immediate or not,
     * so a bit pair is created.
     * @param   width is the number of bits
     * @param   ptype is the element primitive type
     */
    public WordSpec (int width, Ptype ptype) {
        this();
        int     upper = width > 0 ? width-1 : 0;
        Type    t = new Type(ptype, width, 0, 0, 0, null, null);
        check_type = t;
        add(true, 0, 0, upper, t);
        complete = true;
    }

    /**
     * Construct a target word specification containing one word and one
     * associated bit pair given a width (bit pair from 0 to width-1)
     * and an offset (used by UFIXED or FIXED).
     * If the width is {@code <= 0} a bit pair is not added. If the primitive
     * type is Ptype.LOG a width of 1 is used. For Ptype.LOG or
     * Ptype.ENUM it is not known whether the mode is immediate or not,
     * so a bit pair is created.
     * @param   width is the number of bits
     * @param   fixoffset is the number of fraction bits in UFIXED or FIXED type
     * @param   ptype is the element primitive type
     */
    public WordSpec (int width, int fixoffset, Ptype ptype) {
        this();
        int     upper = width > 0 ? width-1 : 0;
        Type    t = new Type(ptype, width, fixoffset, 0, 0, null, null);
        check_type = t;
        add(true, 0, 0, upper, t);
        complete = true;
    }
 
    /**
     * Construct an immediate word specification containing one word.
     * @param   ptype is the element primitive type
     * @param   target is true if target mode
     * @param   v is the associated variable if any
     */
    public WordSpec (Ptype ptype, boolean target, Var v) {
        this();
        Type    t = new Type(ptype, 0);
        check_type = t;
        var = v;
        add(target, 0, 0, 0, t);
        types.add(t);
    }
   
    /**
     * Construct a word specification from a primitive type.
     * This is used for type ENUM. This is used instead
     * of one of the other constructors because the check type
     * must contain the enum map as it is supplied via 'type' and
     * cannot be reconstructed from just a Ptype.
     * @param   type is the type (primitive)
     * @param   v is the associated type variable
     */
    public WordSpec (Type type, Var v) {
        this();
        check_type = type;
        var = v;
        add(true, 0, 0, type.getWidth()-1, type);
        complete = true;
    }

    /**
     * Construct a word specification that is a copy of another.
     * If the width is greater than 0, also add an associated
     * bit pair from 0 to width-1. The copy is mostly a deep
     * clone as the copy will have internal components altered.
     * Target variable subscript fields are not copied.
     * @param   ws is the word specification to copy
     */
    public WordSpec (WordSpec ws) {
        words = new ArrayList<Integer>();
        for (Integer i : ws.words)
            words.add(Integer.valueOf(i.intValue()));
        
        bitpairs = new ArrayList<Pair>();
        for (Pair p : ws.bitpairs)
            bitpairs.add(new Pair(p));
                
        types = new ArrayList<Type>();
        for (Type type : ws.types)
            types.add(type);
        
        subvals = null; // don't bother about this for the copy - not used!
        
        if (ws.dim_des != null) {
            int     len = ws.dim_des.length;
            int[]   i = new int[len];
            for (int j=0 ; j<len ; j++)
                i[j] = ws.dim_des[j];
            dim_des = i;
        }
        check_type = ws.check_type;
        squeues = new QueueRefs();
        var = ws.getVar();
    }
    
    /**
     * Add a word, bitpair and type to the appropriate lists in this
     * WordSpec.
     * @param   target is true if this is a target mode WordSpec
     * @param   word is the word index
     * @param   lower is the lower bit number for target mode
     * @param   upper is the upper bit number for target mode
     * @param   type is the word type
     */
    private void add (boolean target, int word, int lower, int upper, Type type) {
        words.add(Integer.valueOf(word));
        if (target) {
            bitpairs.add(new Pair(lower, upper));
        }
        types.add(type);
    }
    
    /**
     * Check if this WordSpec is a complete variable specification, i.e. is not one word
     * of several or only part of a word.
     * @return true if this WordSpec is a complete variable specification
     */   
    public boolean isComplete () {
        return(complete);
    }
    
    /**
     * Set the 'complete' field.
     */    
    public void setComplete () { complete = true; }
    
    /**
     * Get the number of words in the word specification.
     * @return  number of words in the word specification
     */
    public int numWords () {return(words.size());}

    /**
     * Set the Var field.
     * @param   v is the Var to be associated with this WordSpec
     */
    public void setVar (Var v) {var = v;}

    /**
     * Get the variable associated with this word specification.
     * If the word specification is not that of a variable, return
     * null.
     * @return  the associated Var or null
     */
    public Var getVar () {return(var);}
  
    /**
     * Append a single word, bit pair and type to this target WordSpec
     * @param   i is the word to be appended to the word list
     * @param   lower is the lower index of the bit pair
     * @param   upper is the upper index of the bit pair
     * @param   type is the type (primitive)
     */
    public void append (int i, int lower, int upper, Type type) {
        words.add(Integer.valueOf(i));
        bitpairs.add(new Pair(lower, upper));
        types.add(type);
    }
    
    /**
     * Append a single word and type to this immediate WordSpec.
     * @param   i is the word to be appended to the word list
     * @param   type is the type (primitive)
     */
    public void append (int i, Type type) {
        words.add(Integer.valueOf(i));
        types.add(type);
    }
    
    /**
     * Append words, bit pairs and types to this target WordSpec.
     * @param   ws contains word and bit pair lists to be appended
     */
    public void append (WordSpec ws) {
        words.addAll(ws.words);
        bitpairs.addAll(ws.bitpairs);
        types.addAll(ws.types);
    }
    
    /**
     * Append a list of variable subscript values to the variable
     * subscript list.
     * @param   ws contains a list of variable subscript values to
     *          be appended
     */
    public void appendSubVal (WordSpec ws) {
        subvals.addAll(ws.subvals);
    }
    
    /**
     * Prepend a list of variable subscript TDEVars to the variable
     * subscript list.
     * @param   ws contains a list of variable subscript values to
     *          be prepended
     */
    public void prependSubVal (WordSpec ws) {
        subvals.addAll(0, ws.subvals);
    }
    
    public ArrayList<Integer> getWords () {
        return(words);
    }
    
    /**
     * Get a word from the word array.
     * @param   i is the index of the required word
     * @return  the word required
     */
    public int getWord (int i) {
        return((words.get(i)).intValue());
    }
    
    /**
     * Get a WordSpec representing one word from this WordSpec instance.
     * @param   i is the index of the required word
     * @return  the single word WordSpec
     */
    public WordSpec getWordWordSpec (int i) {
        if (words.size() == 1)
            return(this);   // there is only one word so return complete WordSpec
        
        int                 lower = this.getLower(i);
        int                 upper = this.getUpper(i);
        int                 old_offset = this.getFixOffset(i);
        int                 old_mant = this.getMantissaWidth(i);
        int                 old_bexp = this.getExponentWidth(i);
        ArrayList<String>   enumIDs = this.getEnumIDs(i);
        ArrayList<Long>     enumOrds = this.getEnumOrds(i);
        Ptype               ptype = this.getPrimType(i);
        WordSpec            nws = new WordSpec(i, lower, upper, old_offset, old_mant, old_bexp, enumIDs, enumOrds, ptype, var);
        return(nws);
    }
    
    /**
     * Get a cut-down copy of this WordSpec instance transformed so that the
     * 'types' ArrayList now contains "[]log" for each word, the array matching
     * the word size in bits.
     * 
     * NOTE:
     * This is only used to create TDEVars for the CE and R inputs of a register (static mode).
     * For this reason only the 'words', 'bitpairs' and 'types' fields matter.
     * The 'words' and 'bitpairs' fields are copied from the original and
     * the 'types' entries are now all "[]log" where each array size matches
     * the word size in bits.
     * @return  the transformed WordSpec
     */
    public WordSpec getLogWordSpec () {
        Type        lt;
        WordSpec    nws = new WordSpec(this);
        for (int i=0 ; i<words.size() ; i++) {
            lt = new Type("log", null);
            nws.types.add(i, lt);
        }
        // THE FOLLOWING BREAKS CONNECTIVITY - DON'T KNOW WHY
        //if (nws.numBits() == 1)
        //    nws.complete = true;
        return(nws);
    }
    
    /**
     * Add to a word in the word array.
     * @param   i is the index of the selected word
     * @param   n is the value to add
     */
    public void addToWord (int i, int n) {
        if (i == 64)
            System.out.println("64");
       words.set(i, Integer.valueOf((words.get(i)).intValue() + n));
    }
    
    /**
     * Prepend a dimension to the dimensional description array.
     * This increases its size by one.
     * @param   d is the extra dimension to prepend
     */
    public void prependToDimDes (int d) {
        if (dim_des == null) {
            dim_des = new int[1];
            dim_des[0] = d;
            return;
        }
        int     len = dim_des.length;
        int[]   i = new int[len + 1];
        System.arraycopy(dim_des, 0, i, 1, len);
        i[0] = d;
        dim_des = i;
    }

    /**
     * Set the dimensional description array.
     * @param  dd is a dimensional description array
     */
    public void setDimDes (int[] dd) {
        dim_des = dd;
    }
    
    /**
     * Get the dimensional description array.
     * @return  the dimensional description array, or null
     */
    public int[] getDimDes () {
        return(dim_des);
    }
    
    /**
     * Get the number of words implied by the dimensional description.
     * For a non-array type, 1 is returned.
     * @return  the number of words implied by the dimensional description
     */
    public int getDimWords () {
        if (dim_des == null)
            return(1);

        int size = 1;        
        for (int i=0 ; i<dim_des.length ; i++)
            size *= dim_des[i];
        return(size);
    }
    
    /**
     * Set the check type associated with this WordSpec.
     * @param  t is the type
     */
    public void setCheckType (Type t) {
        check_type = t;
    }
    
    /**
     * Get the type. This will be zero or more
     * dimensions in brackets followed by either a primitive type
     * name, a type mode identifier or a struct.
     * If the type has not yet been assigned, construct it from
     * the check type by prepending dimensions if required.
     * @return  the type
     */
    public Type getType() {
        if (type == null) {
            type = check_type;
            if (dim_des != null)
                for (int i=dim_des.length-1 ; i>=0 ; i--)
                    type = new Type(type, dim_des[i]);
        }
        return(type);
    }
        
    /**
     * Get the type (which will be primitive) for one word from the
     * word array.
     * @param   i is the index of the required word
     * @return  the type
     */
    public Type getType (int i) {
        return(types.get(i));
    }
    
    /**
     * Get the primitive type for a word from the word array.
     * @param   i is the index of the required word
     * @return  the primitive type code
     */
    public Ptype getPrimType (int i) {
        return(types.get(i).getPrimType());
    }
    
    /**
     * Get the check type associated with this WordSpec.
     * @return  the type
     */
    public Type getCheckType () {
        return(check_type);
    }
        
    /**
     * Get any target subscript queues.
     * @return  the queues
     */
    public QueueRefs getSQueues () {
        return(squeues);
    }
    
    /**
     * Determines the number of bits implied by the WordSpec.
     * This iterates through the sequences of integers delimited by
     * the pairs counting these.
     * @return   the number of entries
     */
    public int numBits () {
        int     n = 0;
        for (Pair p : bitpairs)
            n += p.getUpper() - p.getLower() + 1;
        if (tsnperms != 0)
            return(n / tsnperms);
        else
            return(n);
    }
    
    /**
     * Get the 'i'th bit in the word specification.
     * This iterates through the sequences of integers delimited by
     * the pairs until it determines the i'th' in the sequence, which
     * is returned.
     * @param   i is the index of the required entry
     * @return  the selected entry
     */
    public int getBit (int i) {
        int     count = 0;
        int     lower;
        int     upper;
        int     span;
        for (Pair p : bitpairs) {
            lower = p.getLower();
            upper = p.getUpper();
            span = upper - lower + 1;
            if ((i >= count) && (i < (count + span)))
                return(lower + i - count);
            count += span;
        }
        return(-1);
    }
    
    /**
     * Get the highest entry in the pair list, i.e. the upper
     * index of the last pair.
     * @return  the highest entry
     */
    public int getHighestBit () {
        int     n = 0;
        for (Pair p : bitpairs) {
            if (p.getUpper() > n)
                n = p.getUpper();
        }
        return(n);
    }
    
    /**
     * Determine if there is a non-empty PairList.
     * @return  true if there is a non-empty PairList
     */    
    public boolean hasPairs () {
        return((bitpairs != null) && (bitpairs.size() != 0));
    }

    /**
     * Get a Pair from the PairList.
     * @param   n is the index of the Pair required
     * @return  the Pair
     */    
    public Pair getPair (int n) {
        return(bitpairs.get(n));
    }

    /**
     * Get the number of target bits in a particular word.
     * @param   i is the index of the required word
     * @return  the number of bits in the word
     */
    public int getWidth (int i) {
        if (bitpairs.size() == 0)
            return(0);
        Pair    p = bitpairs.get(i);
        return(p.getWidth());
    }

    /**
     * Get the offset of a particular target word.
     * @param   i is the index of the required word
     * @return  the offset
     */
    public int getFixOffset (int i) {
        if (bitpairs.size() == 0)
            return(0);
        return(types.get(i).getFixOffset());
    }

    /**
     * Get the mantissa width of a particular target word.
     * @param   i is the index of the required word
     * @return  the mantissa width
     */
    public int getMantissaWidth (int i) {
        if (bitpairs.size() == 0)
            return(0);
        return(types.get(i).getMantissaWidth());
    }

    /**
     * Get the biased exponent width of a particular target word.
     * @param   i is the index of the required word
     * @return  the biased exponent width
     */
    public int getExponentWidth (int i) {
        if (bitpairs.size() == 0)
            return(0);
        return(types.get(i).getExponentWidth());
    }
    
    /**
     * Get the enum ID list.
     * @param   i is the index of the required word
     * @return  the enum ID list
     */
    public ArrayList<String> getEnumIDs (int i) {
        if (bitpairs.size() == 0)
            return(null);
        return(types.get(i).getEnumIDs());
    }
    
    /**
     * Get the enum ordinal list.
     * @param   i is the index of the required word
     * @return  the enum ordinal list
     */
    public ArrayList<Long> getEnumOrds (int i) {
        if (bitpairs.size() == 0)
            return(null);
        return(types.get(i).getEnumOrds());
    }
    
    /**
     * Determine if the type contains a zero width word.
     * @return  true if the type contains a zero width word
     */
    public boolean hasZeroWidthWord () {
        for (Pair p: bitpairs) {
            if (p.getWidth() == 0)
                return(true);
        }
        return(false);
    }
    
    /**
     * Get the lower target bit index for a word.
     * This is the lower of a pair of indices which map
     * the word into the 1-dimensional array representing a target
     * variable.
     * @param   i is the index of the required word
     * @return  the bit index for the word
     */
    public int getLower (int i) {
        Pair    p = bitpairs.get(i);
        return(p.getLower());
    }
    
    /**
     * Get the upper target bit index for a word.
     * This is the upper of a pair of indices which map
     * the word into the 1-dimensional array representing a target
     * variable.
     * @param   i is the index of the required word
     * @return  the bit index for the word
     */
    public int getUpper (int i) {
        Pair    p = bitpairs.get(i);
        return(p.getUpper());
    }
     
    /**
     * Get the select signal for a word.
     * The select signal will be non-null if the word specification
     * is of a variable which had target variable subscripts.
     * @param   i is the index of the required word
     * @return  the select signal
     */
    public TDEVar getSelect (int i) {
        Pair    p = bitpairs.get(i);
        return(p.getSelect());
    }
   
    /**
     * Add to the pair of indices for a target word.
     * No operation if this is not a target type (no bit pairs).
     * @param   index is the index of the required word
     * @param   n is the integer value to be added
     */
    public void addToSubs (int index, int n) {
        if (bitpairs.size() == 0)
            return;
        Pair    p = bitpairs.get(index);
        p.addToSubs(n);
    }
    
    /**
     * Append an index for a bit pair. This is a subscript value
     * (Integer) or a field identifier (String). Each bit pair
     * has a list of subscripts and field names corresponding to
     * that particular word and bit pair. This method appends one
     * of these values to that list. Field names in this list are not used
     * and just space out the subscript entries. Strings are used, rather
     * than null entries, as a diagnostic aid when debugging the compiler.
     * No operation if this is not a target type (no bit pairs).
     * @param   i is the index of the required word
     * @param   o is the subscript (Integer) or field name (String)
     *          to be appended
     */
    public void addIndex (int i, Object o) {
        if (bitpairs.size() == 0)
            return;
        Pair    p = bitpairs.get(i);
        p.addIndex(o);
    }
    
    /*
     * Unwrap a single array member or single field struct.
     * @param   var is variable
     * @param   loc is the source file location
     * @return  the WordSpec of the primitive type
    public WordSpec unwrap (Var var, SrcLoc loc) {
        Type    t = check_type;
        return(t.unwrap(loc).getWordSpec(var, loc));
    }
     */
    
    /**
     * Pack initialisation data into an array of strings, one per initialisation word.
     * This is called from exec.Memory.creatVar() and mods.DelMod.execute().
     * The initialisation data Val must be an immediate array whose member type matches
     * the target type in this WordSpec class
     * @param targ_init is a Val containing the initialisation data
     * @param len is the number of words to initialise
     * @param mess is an error message header
     * @param loc is the source file location
     * @return an array of hexadecimal strings of packed data
     */
    public String[] packInit (Val targ_init, int len, String mess, SrcLoc loc) {
        
        int         words_per_entry = numWords();
        int         valwords = targ_init.numWords();// number of words in the initialisation compound value
        int         iwords = valwords / words_per_entry;
        int         k = 0;
        int         bits;
        long        int_val;
        String[]    sinit = new String[iwords];
        String      s;

        if (iwords > len)
            throw new ExEx(mess + ": too many initialisation values", loc);

        for (int i=0 ; i<iwords ; i++) {
            TDEVar          tdev = null;
            StringBuffer    sb = new StringBuffer();
            
            for (int j=0 ; j<words_per_entry ; j++) {
                Ptype   prim_type = targ_init.getValPType(k);
                int     width = getWidth(j);
                int     fixoffset = getFixOffset(j);
                Object  o = targ_init.getVal(k++);
                if (o instanceof TDEVar) {
                    tdev = (TDEVar)o;
                    if (tdev.getType() == TDEVtype.VAR)
                        throw new ExEx(mess +
                                "': initialisation value is non-constant target variable", loc);
                    o = tdev.getConst();
                }
                boolean signed;
                
                switch (prim_type) {
                case NONE:
                    sb.insert(0, zeropad(width));
                    break;
                case BITS:
                case UINT:
                case INT:
                case UFIXED:
                case FIXED:
                    if (o instanceof Double) {
                        double  d = ((Double)o).doubleValue();
                        int_val = Math.round(d * ((long)1 << fixoffset));
                    } else if (o instanceof Long)
                        int_val = ((Long)o).longValue();
                    else
                        throw new ExEx(mess +
                            "': initialisation value address " + i +
                            " field index " + j + " wrong type", loc);
                    s = Long.toBinaryString(int_val);
                    signed = getPrimType(j)==Ptype.INT ||
                             getPrimType(j)==Ptype.FIXED;
                    bits = bits(int_val, signed);
                    if (bits > width)
                        throw new ExEx(mess + "': initialisation value " + int_val + " too large", loc);
                    if (s.length() > width)
                        // -ve - truncate extra bits from front
                        s = s.substring(s.length() - width);
                    else if (s.length() < width)
                        // +ve - add extra 0 bits at front
                        s = zeropad(width - s.length()) + s;
                    sb.insert(0, s);
                    break;
                case ENUM:
                    if (!(o instanceof Long))
                        throw new ExEx(mess +
                            "': initialisation value address " + i +
                            " field index " + j + " wrong type", loc);
                    int_val = ((Long)o).longValue();
                    s = Long.toBinaryString(int_val);
                    if (s.length() < width)
                        // add extra 0 bits at front
                        s = zeropad(width - s.length()) + s;
                    sb.insert(0, s);
                    break;
                case LOG:
                    if (!(o instanceof Boolean))
                        throw new ExEx(mess +
                            "': initialisation value address " + i +
                            " field index " + j + " wrong type", loc);
                    if (((Boolean)o).booleanValue())
                        sb.insert(0, "1");
                    else
                        sb.insert(0, "0");
                    break;
                case FLOAT:
                    throw new ExEx(mess + "': initialisation value unimplemented type (float)", loc);
                default:
                    throw new ExEx(mess + "': initialisation value wrong type", loc);
                }
            }
            sinit[i] = binToHex(sb);
        }
        return(sinit);
    }
    
    /**
     * Check the dimensional match between this WordSpec and a Ref or Val.
     * Return true if OK. The check types, total sizes and dimensional
     * descriptions are compared.
     * If the check fails and mess is not null, exit with a message.
     * If the check fails and mess is null, return false.
     * @param   rv  the Ref or Val checked against this WordSpec
     * @param   strict is true if target primitive widths are to be checked
     * @param   mess an optional failure message for fatal exit
     * @param   loc the source file location
     * @return  success or failure of the type match
     */
    public boolean checkMatch (
        RefOrVal    rv,
        boolean     strict,
        String      mess,
        SrcLoc      loc
    ) {
        return(checkMatch(rv.getWordSpec(), strict, mess, loc));
    }
    
    /**
     * Check the dimensional match between this WordSpec and another.
     * Return true if OK. The check types, total sizes and dimensional
     * descriptions are compared.
     * If the check fails and mess is not null, exit with a message.
     * If the check fails and mess is null, return false.
     * @param   ws  the WordSpec checked against this WordSpec
     * @param   strict is true if target primitive widths are to be checked
     * @param   mess an optional failure message for fatal exit
     * @param   loc the source file location
     * @return  success or failure of the type match
     */
    public boolean checkMatch (
        WordSpec    ws,
        boolean     strict,
        String      mess,
        SrcLoc      loc
    ) {
        int     l1;
        int     l2;
        // Special case -
        //  str = type
        //  type = str
        if ((check_type.getPrimType() == Ptype.STR) &&
            (ws.getCheckType().getPrimType() == Ptype.TYPE) ||
            (check_type.getPrimType() == Ptype.TYPE) &&
            (ws.getCheckType().getPrimType() == Ptype.STR))
            return(true);
       
        // Special case -
        //  fixed = immediate float or ufixed = immediate float
        if (((check_type.getPrimType() == Ptype.FIXED) ||
             (check_type.getPrimType() == Ptype.UFIXED)) &&
            (ws.getCheckType().getPrimType() == Ptype.FLOAT) &&
            ws.getCheckType().hasImmediateType())
            return(true);

        // Special case -
        //  fixed = immediate int or
        //  fixed = immediate uint or
        //  ufixed = immediate uint
        if ((((check_type.getPrimType() == Ptype.FIXED) &&
             ((ws.getCheckType().getPrimType() == Ptype.INT) ||
              (ws.getCheckType().getPrimType() == Ptype.UINT))) ||
             (check_type.getPrimType() == Ptype.UFIXED) &&
             (ws.getCheckType().getPrimType() == Ptype.UINT)) &&
            ws.getCheckType().hasImmediateType())
            return(true);
        // Special case of assignment to value variable type "empty"
        if (check_type.getPrimType() == Ptype.EMPTY)
            return(true);
        // Special case of assignment of "null" to  type "->"
        if ((check_type.getPrimType() == Ptype.PTR) &&
            (ws.getCheckType().getPrimType() == Ptype.NULL))
            return(true);
        // Special case of "null" queue seen as "log" - NO LONGER ALLOWED!
        //if (!strict && (check_type.getPrimType() == Ptype.NULL) && (ws.getCheckType().getPrimType() == Ptype.LOG))
        //    return(true);

        // check that check types agree
        if (!check_type.isEqual(ws.getCheckType(), strict)) {
            if (mess == null)
                return(false);
            else {
                String  s1 = getTypeString();
                String  s2 = ws.getTypeString();
                throw new ExEx(mess + " types do not agree - " +
                                                        s1 + " != " + s2, loc);
            }
        }
   
        // check that total sizes agree
        l1 = words.size();
        l2 = ws.numWords();
        if (ws.tsnperms != 0)
            l2 /= ws.tsnperms;
        if (l1 != l2) {
            if (mess == null)
                return(false);
            else {
                String  s1 = getTypeString();
                String  s2 = ws.getTypeString();
                throw new ExEx(mess + " type sizes do not agree - " +
                        s1 + " != " + s2 + "   " + l1 + " != " + l2, loc);
            }
        }
  
        // check that dimensional descriptions agree
        int[]   dd2 = ws.getDimDes();
        if ((dim_des == null) && (dd2 != null) ||
            (dim_des != null) && (dd2 == null)) {
            if (mess == null)
                return(false);
            else {
                String  s1 = getTypeString();
                String  s2 = ws.getTypeString();
                throw new ExEx(mess + " dimensionalities do not agree - " +
                                                        s1 + " != " + s2, loc);
            }
        }
        if (dim_des != null) {
            if (dim_des.length != dd2.length) {
                if (mess == null)
                    return(false);
                else {
                    String  s1 = getTypeString();
                    String  s2 = ws.getTypeString();
                    throw new ExEx(mess + " dimensionalities do not agree - " +
                                                        s1 + " != " + s2, loc);
                }
            }
            for (int i=0 ; i<dim_des.length ; i++)
                if (dim_des[i] != dd2[i]) {
                    if (mess == null)
                        return(false);
                    else {
                        String  s1 = getTypeString();
                        String  s2 = ws.getTypeString();
                        throw new ExEx(mess + " dimensions do not agree - " +
                                                        s1 + " != " + s2, loc);
                    }
                }
        }
        return(true);
    }

    
    /**
     * Check to see if the argument WordSpec pair list would be
     * contiguous with this pair list in this WordSpec, i.e. the
     * lower index of the first pair of the argument list is one
     * greater than the higher index of the last pair of this pair
     * list.
     * @param   ws word specification containing the pair list
     * @return  true if they are contigous or false if not
     */
    public boolean isConsecutive (WordSpec ws) {
        return((getHighestBit() + 1) == ws.getPair(ws.bitpairs.size()-1).getLower());
    }
    
    /**
     * Compare this WordSpec with another for equality.
     * This is only used for target mode WordSpecs.
     * @param   ws is the WordSpec to be compared with this one
     * @param   strict is true for a more exact comparison
     * @return  true if the two WordSpecs are the same
     */
    public boolean equals (WordSpec ws, boolean strict) {
        if (this == ws)
            return(true);
        if ((bitpairs == null) || (ws.bitpairs == null))
            throw new ExEx("WordSpec.isEqual() called for immediate WordSpec");
        
        if (words.size() != ws.words.size())
            return(false);
        if (bitpairs.size() != ws.bitpairs.size())
            return(false);
            
        for (int i=0 ; i<words.size() ; i++) {
            if (!words.get(i).equals(ws.words.get(i)))
                return(false);
            if (!bitpairs.get(i).equals((ws.bitpairs.get(i))))
                return(false);
        }
        
        Type    ct1 = check_type;
        Type    ct2 = ws.check_type;
        
        if ((ct1 == null) && (ct2 != null) || (ct1 != null) && (ct2 == null))
            return(false);
        if (ct1 != null) {
            if (ct1.getPrimType() != ct2.getPrimType())
                return(false);
            TreeMap<String,Field>   field_map1 = ct1.getFieldMap();
            TreeMap<String,Field>   field_map2 = ct2.getFieldMap();
            boolean fm_null1 = field_map1 == null;
            boolean fm_null2 = field_map2 == null;
            if (fm_null1 && !fm_null2 || !fm_null1 && fm_null2)
                return(false);
            ArrayList<Long>         enum_ords1 = ct1.getEnumOrds();
            ArrayList<Long>         enum_ords2 = ct2.getEnumOrds();
            boolean eo_null1 = enum_ords1 == null;
            boolean eo_null2 = enum_ords2 == null;
            if (eo_null1 && !eo_null2 || !eo_null1 && eo_null2)
                return(false);
        }
        
        // This test may not work properly but
        // should fail safe (not equal).
        if (!subvals.equals(ws.subvals))
            return(false);

        if (strict) {
            if ((check_type != null) && (ws.check_type != null)) {        
                if (!check_type.isEqual(ws.check_type, false))
                    return(false);
            } else if (check_type != ws.check_type)
                return(false);
        }
            
        if ((dim_des == null) && (ws.dim_des == null))
            return(true);
        if ((dim_des == null) || (ws.dim_des == null))
            return(false);
        for (int i=0 ; i<dim_des.length ; i++)
            if (dim_des[i] != ws.dim_des[i])
                return(false);

        return(true);
    }
    
    /**
     * Return a hash code for a WordSpec.
     * @return  the hash code
     */
    public int hashCode () {
        int hc = 0;
        for (Pair p : bitpairs)
            hc = (hc << 4) + p.hashCode();
        return(hc);
    }
    
    /**
     * Determine if the bit pairs in this WordSpec are the same as those in another WordSpec.
     * @param ws the WordSpec to compare with this
     * @return true if the bit pairs are the same
     */
    public boolean bitFieldsEqual (WordSpec ws) {
        int n = bitpairs.size();
        if (n != ws.bitpairs.size())
            return(false);
        for (int i=0 ; i<n ; i++)
            if (!bitpairs.get(i).equals(ws.bitpairs.get(i)))
                return(false);
        return(true);
    }
    
    /**
     * Diagnostic dump of a WordSpec.
     * A description is printed to standard output.
     */
    public void dump () {
        int n = words.size();
        for (int i=0 ; i<n ; i++) {
            String  l = words.get(i).toString();
            Pair    p = bitpairs.get(i);
            l += "  " + p.getLower() + ".." + p.getUpper();
            ArrayList<Object>   a = p.getIndices();
            if (a != null) {
                Iterator<Object>    iti = a.iterator();
                while (iti.hasNext()) {
                    Object  o = iti.next();
                    if (o instanceof Integer)
                        l += " " + o;
                    else
                        l += " " + (String)o;
                }
            }
            l += "  ";
            if (subvals != null) {
                for (Val val : subvals) {
                    if (val == null)
                        l += "  - ";
                    else
                        l += " " + val.getTDEVar().toString();
                }
            }
            
            System.out.println(l);
        }
    }
        
    /**
     * Create a selector for a compound type with target variable subscripts.
     * @param   id is the identifier of the TDEVar of the compound type to be
     *          selected
     * @param   val is an optional array of values for value mode variables only
     * @param   loc is the source file location
     * @return  the TDEVar of the selector output
     */
    public TDEVar selector (String id, Object[] val, SrcLoc loc) {
        // dim is an array of dimensions over which target subscripts range
        // n is the number of target subscripts
        // widths gives the number of bits needed for each target subscript
        // nperms is the number of subscript permutations
        int     n = tsdim.length;

        TDEVar  value = tdelist.signal("E", selectorWordspec(tswidths, n), loc);

        // Iterate through the bit pairs accumulating selected data
        // permutation WordSpecs
        WordSpec[]  w = new WordSpec[tsnperms]; // WordSpec for each permutation
        int[]       k = new int[tsnperms];      // word indices for appending
        int         i;
        for (i=0 ; i<tsnperms ; i++) {
            w[i] = new WordSpec();
            k[i] = 0;
        }
        i = 0;
        for (Pair p : bitpairs) {
            int     j = perm_index(p, tswidths);
            w[j].append(k[j], p.getLower(), p.getUpper(), types.get(i++));
            k[j]++;
        }

        // Iterate through words of the selector output
        WordSpec    vws = value.getWordSpec();
        int         lim = vws.numWords();
        for (i=0 ; i<lim ; i++) {
            TDE     tde_s = new TDE(TDEType.SELECT, loc);
            int     word = vws.getWord(i);
        
            // Iterate through permutations adding data and select inputs
            // to the selector
            for (int j=0 ; j<tsnperms ; j++) {
                TDEVar  tdev;
                if (val != null) {
                    // for value mode get the actual values, not a constructed signal
                    if (!(val[j] instanceof Val))
                        throw new ExEx("SYSTEM ERROR IN WordSpec.selector()");
                    Val v = (Val)val[j];
                    tdev = v.getTDEVar();
                } else {
                    TDEVar  datain = TDEVar.makeTDEVar(id, w[j], loc);
                    tdev = datain.getWord(word, loc);
                }
                tde_s.add2i(tsselects[j]);
                tde_s.add2i(tdev);
            }

            tde_s.add2o(value.getWord(word, loc));

            tdelist.addTDE(tde_s);
        }

        value.getWordSpec().subvals = subvals; // add target subscript Vals

        return(value);
    }
    
    /**
     * If there are variable subscripts, generate a decoder
     * and associated selector signal for each word.
     * Only called from exec/Type.java.getWordSpec().
     * @param   loc is the source file location
     */
    public void processVarSubs (SrcLoc loc) {
        if (getNumTargSubs() == 0)
            return;
        
        // dim is an array of dimensions over which target subscripts range
        // n is the number of target subscripts
        // widths gives the number of bits needed for each target subscript
        // nperms is the number of subscript permutations
        tsdim = getTargSubDims();
        int     n = tsdim.length;
        tswidths = new int[n];
        for (int i=0 ; i<n ; i++)
            tswidths[i] = Functions.bits(tsdim[i]-1, false);
        tsnperms = 1;
        for (int i=0 ; i<n ; i++)
            tsnperms *= tsdim[i];

        TDEVar  decodein = combine_tdes(tswidths, n, loc);
        
        // iterate through permutations creating the combined decoder values
        tsdvals = combine_indices(tsdim, tswidths, n, tsnperms);
        // iterate through permutations creating select decoders and signals
        tsselects = new TDEVar[tsnperms];
        for (int i=0 ; i<tsnperms ; i++) {
            tsselects[i] = tdelist.signal("S", loc);
            tdelist.decode(tsdvals[i], decodein, tsselects[i], loc);
        }

        // Iterate through the bit pairs writing the appropriate
        // selector signal to each
        for (int i=0 ; i<bitpairs.size() ; i++) {
            Pair    p = bitpairs.get(i);
            int     perm = perm_index(p, tswidths);
            p.setSelect(tsselects[perm]);
        }

        // Iterate through variable subscripts collecting queues
        // and clock domains.
        for (Val val : subvals) {
            if (val != null)
                squeues.and_set(val.getQueues(), loc);
        }
    }
    
    // Get the target subscript dimensions in this PairList.
    private int[] getTargSubDims () {
        int         n = getNumTargSubs();
        int[]       dim = new int[n];
        
        for (int i=0 ; i<n ; i++)
            dim[i] = 0;

        for (Pair p: bitpairs) {
            ArrayList<Object>   a = p.getIndices();
            int                 k = 0;
            // for each bit pair iterate through the subscript/field
            // indices
            for (int i=0 ; i<a.size() ; i++) {
                // for this index, is this a variable subscript?
                if (subvals.get(i) != null) {
                    // get the index value and check against
                    // the variable subscript dimension array entry,
                    // then bump the entry subscript
                    int j = ((Integer)(a.get(i))).intValue() + 1;
                    if (j > dim[k])
                        dim[k] = j;
                    k++;
                }
            }
        }
        return(dim);
    }
    
    // Create a WordSpec for the selector output.
    // Iterate through all bit pairs. Find bit pairs for which
    // all target subscripts are zero. For each of these, add
    // a word of that width to the new WordSpec.
    private WordSpec selectorWordspec (int[] widths, int n) {
        int         lower = 0;
        int         upper;
        int         i = 0;
        WordSpec    ws = new WordSpec();
        ws.check_type = check_type;

        Iterator<Type>    itt = types.iterator();
        // iterate through the bit pairs
        for (Pair p: bitpairs) {
            if (perm_index(p, widths) == 0) {
                upper = lower + p.getWidth() - 1;
                ws.append(i++, lower, upper, itt.next());
                lower = upper + 1;
            }
        }
        ws.dim_des = dim_des;
        return(ws);
    }

    // Combine multiple variable subscripts into a single
    // packed signal array, which is returned.
    private TDEVar combine_tdes (int[] widths, int n, SrcLoc loc) {
        WordSpec    sws = new WordSpec();
        int         width = 0;
        int         lower = 0;
        int         upper;
        for (int i=0 ; i<n ; i++) {
            width += widths[i];
            upper = lower + widths[i] - 1;
            sws.append(i, lower, upper, new Type(Ptype.UINT, upper-lower+1));
            lower += widths[i];
        }
        sws.setCheckType(new Type("uint:"+Functions.bits(width, false), loc));
        TDEVar  merge = tdelist.signal("SC", sws, loc);
        int     j = widths.length-1;
        for (int i=0 ; i<subvals.size() ; i++) {
            Val val = subvals.get(i);
            if (val == null)
                continue;
            TDEVar      t = val.getTDEVar();
            WordSpec    ws = t.getWordSpec();
            if (ws.numWords() != 1)
                throw new ExEx("target variable subscript '" + t.getId() +
                            "' not a single element", loc);
            if (ws.numBits() < widths[j])
                throw new ExEx("target variable subscript '" + t.getId() +
                            "' too small", loc);
            tdelist.connect(merge.getWord(j--, loc), t, false);
        }

        TDEVar  result = tdelist.signal("SS", new WordSpec(width, Ptype.BITS), loc);
        tdelist.connect(result, merge, false);
        return(result);
    }

    // Get the permutation index from the variable subscript index
    private int perm_index (Pair p, int[] widths) {
        int         n = 0;
        Iterator<Object>    iti = p.getIndices().iterator();
        Iterator<Val>       itv = subvals.iterator();
        int         i = 0;
        while (iti.hasNext()) {
            Val     val = itv.next();
            Object  o = iti.next();
            if (val != null) {
                int v = (Integer)o;
                n <<= widths[i++];
                n |= v;
            }
        }
        for (int j=0 ; j<tsnperms ; j++)
            if (tsdvals[j] == n)
                return(j);
        return(-1);
    }

    // Form the array of decoder values for the permutations.
    // n is the number of target variable subscripts
    // dim[n] gives the range (dimension) of each subscript
    // widths[n] gives the number of bits needed for each subscript value
    // nperms is the number of permutations
    private int[] combine_indices (int[] dim, int[] widths, int n, int nperms) {
        tsdvals = new int[tsnperms];
        int[]   perm = new int[n];
        for (int i=0 ; i<n ; i++)
            perm[i] = 0;
        for (int i=0 ; i<nperms ; i++) {
            int val = 0;
            for (int j=0 ; j<n ; j++) {
                val <<= widths[j];
                val |= perm[j];
            }
            tsdvals[i] = val;
            next_perm(perm, dim, n);
        }

        return(tsdvals);
    }
    
    private void next_perm (int[] perm, int[] dim, int n) {
        for (int i=n-1 ; i>=0 ; i--) {
            if (perm[i] < (dim[i] - 1)) {
                perm[i]++;
                return;
            } else {
                perm[i] = 0;
            }
        }
    }

    /**
     * Append a target variable subscript to the list of these
     * subscripts.
     * @param   v is the target subscript value or null if not
     *          a target subscript
     */
    public void addSubVal (Val v) {
        subvals.add(v);
    }

    /**
     * Get the target variable subscript list.
     * @return  the target variable subscript list
     */
    public ArrayList<Val> getSubVals () {
        return(subvals);
    }

    /**
     * Get the number of target variable subscripts.
     * @return  the number of target variable subscripts
     */
    public int getNumTargSubs () {
        int         n = 0;
        for (Val val : subvals) {
            if (val != null)
                n++;
        }    
        return(n);
    }
    
    /**
     * Get the type as a string. This will be zero or more
     * dimensions in brackets followed by either a primitive type
     * name, a type mode identifier or a struct.
     * @return  the type String
     */
    public String getTypeString() {
        String      typestring = "";
        if (dim_des != null) {
            // an array
            for (int i=0 ; i<dim_des.length ; i++) 
                typestring += "[" + dim_des[i] + "]";
        }
        typestring += check_type.getTypeString();

        return(typestring);
    }

    /**
     * Return a string containing all the bit pairs in this
     * word specification. Members of each bit pair are separated by ".."
     * and bit pairs are separated by ",".
     * @return  the complete bit pair String
     */    
    public String toString () {
        String          retval = "";
        Iterator<Pair>  it = bitpairs.iterator();
        while (it.hasNext()) {
            Pair    p = it.next();
            int     l = p.getLower();
            int     u = p.getUpper();
            retval += l;
            if (u != l)
                retval += ".." + u;
            if (it.hasNext())
                retval += ",";
        }
        return(retval);
    }

    
    /**
     * A bit pair.
     * These pairs specify bit ranges for target variables.
     * Where an array index is itself a target variable, rather than
     * an immediate (constant in the target environment), each pair
     * specifies a bit range along with a list of target variable subscript
     * TDEVars and an associated subscript value for that bit range.
     * 
     * Where a compound type includes nested structs the field identifier strings
     * are included in the indices list. These strings are not used and they
     * serve as space out the subscript indices. Strings are used rather than
     * null entries as the former may assist in compiler debugging.
     */ 
    private class Pair {
        private int                 lower;      // lower bit index
        private int                 upper;      // upper bit index
        private ArrayList<Object>   indices;    // list of subscripts/fields for this word
        private TDEVar              select;

        public Pair (int l, int u) {
            lower = l;
            upper = u;
            indices = null;
            select = null;
        }

        @SuppressWarnings("unchecked")
        public Pair (Pair p) {
            lower = p.lower;
            upper = p.upper;
            if (p.indices == null)
                indices = null;
            else
                indices = (ArrayList<Object>)(p.indices.clone());
            select = null;
        }

        public boolean equals (Pair p) {
            if (lower != p.lower)
                return(false);
            if (upper != p.upper)
                return(false);
            return(true);
        }

        public int hashCode () { return((lower << 2) + upper); }

        public int getLower () { return(lower); }

        public int getUpper () { return(upper); }

        public int getWidth () { return(upper - lower + 1); }
        
        public void addToSubs (int i) {
            lower += i;
            upper += i;
        }

        public void addIndex (Object o) {
            if (indices == null)
                indices = new ArrayList<Object>();
            indices.add(0, o);
        }

        public ArrayList<Object> getIndices () { return(indices); }
        
        public void setSelect (TDEVar sel) { select = sel; }
        
        public TDEVar getSelect () { return(select); }
    }

}
