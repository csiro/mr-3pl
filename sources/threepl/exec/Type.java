package threepl.exec;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import threepl.ThreePL;
import threepl.exceptions.ExEx;
import threepl.exec.Var.IDtype;
import threepl.nodes.Ident;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.Functions;
import threepl.parser.SrcLoc;

/**
 * This class represents variable types.
 * There are 4 entities that are represented -
 * <UL>
 * <LI> a primitive type
 * <LI> one dimension of an array
 * <LI> a struct
 * <LI> an enumerated type
 * </UL>
 *
 * <p>A primitive type has an integer code in <b>prim_type</b>. If the
 * primitive type is a pointer, <b>prim_type</b> will have the value
 * <b>typePTR</b>.
 *
 * <p>An array has the dimension in <b>dim</b>. <b>array_type</b> points to
 * the array <b>Type</b>. For multi-dimensioned arrays the <b>Type</b>
 * pointed to will itself be an array.
 *
 * <p>A struct has field identifiers mapped to <b>Field</b>s through
 * <b>field_map</b>. <b>field_index</b> locates <b>Field</b>s by position
 * index. a <b>Field</b> contains both the field name and its <b>Type</b>.
 * Using both a map indexed by field identifier and a list indexed by
 * field position allows structs to be mapped in the order of occurence
 * of the fields in the source code.
 *
 * <p>.An enum has the value identifiers mapped to associated ordinals
 * via the paired lists <b>enum_ids</b> and <b>enum_ords</b>.
 */
public class Type implements Constant {
    private Ptype                   prim_type = Ptype.NONE; // primitive type
    private int                     width;          // primitive type width (bits)
    private byte                    fixoffset;      // fixed type fraction bits
    private byte                    mant;           // floating point mantissa bits
    private byte                    bexp;           // floating point biased exponent bits
    private int                     dim;            // array dimension
    private TreeMap<String,Field>   field_map;      // struct fields by name
    private ArrayList<Field>        field_index;    // struct fields by index
    private Type                    array_type;     // array type
    private ArrayList<String>       enum_ids;       // enum identifiers
    private ArrayList<Long>         enum_ords;      // enum ordinals
    private boolean                 has_i_type;     // has immediate type (primitive or nested)
    private boolean                 has_t_type;     // has target type (primitive or nested)
    private boolean                 has_p_type;     // has pointer type (primitive or nested)
    private boolean                 has_e_type;     // has enum type (primitive or nested)

    public static Type              NONE = new Type(Ptype.NONE, 0);
    public static Type              LOG = new Type(Ptype.LOG, 0);
    public static Type              UINT = new Type(Ptype.UINT, 0);
    public static Type              STR = new Type(Ptype.STR, 0);
    public static Type              FLOAT = new Type(Ptype.FLOAT, 0);
    public static Type              PTR = new Type(Ptype.PTR, 0);
    public static Type              CLASS = new Type(Ptype.CLASS, 0);
    public static Type              TYPE = new Type(Ptype.TYPE, 0);
    public static Type              FILE = new Type(Ptype.FILE, 0);
    public static Type              NULL = new Type(Ptype.NULL, 0);

    /**
     * Construct an empty Type.
     */
    public Type () {
    }

    /**
     * Construct a primitive Type. If a target type the width is non-zero, otherwise
     * the type is assumed to be immediate.
     * @param   pt is the primitive type
     * @param   width is the type width for a target type or 0 for an immediate type.
     */
    public Type (Ptype pt, int width) {
        prim_type = pt;
        if (width != 0)
            this.width = width;
        else
            has_i_type = true;
        /*if (pt == Ptype.LOG)
            width = 1;
        else
            has_i_type = true;*/
    }

    /**
     * Construct a primitive Type. If the type is Ptype.LOG
     * the width is ignored.
     * @param   pt is the primitive type
     * @param   w is the width
     * @param   o is the fixed point offset
     * @param   m is the mantissa width
     * @param   e is the biased exponent width
     * @param   eids is the list of enum ID strings
     * @param   eords is the list of enum ordinals
     */
    public Type (Ptype pt, int w, int o, int m, int e, ArrayList<String> eids, ArrayList<Long> eords) {
        prim_type = pt;
        if (pt == Ptype.LOG) {
            width = 1;
            return;
        }
        width = w;
        fixoffset = (byte)o;
        mant = (byte)m;
        bexp = (byte)e;
        enum_ids = eids;
        enum_ords = eords;
        has_t_type = true;
    }
    
    /**
     * Construct an array.
     * @param   type is the type of the array
     * @param   n is the size of the array
     */
    public Type (Type type, int n) {
        prim_type = Ptype.NONE;
        dim = n;
        array_type = type;
    }

    /**
     * Construct a type from a type specification string.
     * Arrays and structs are built up by recursive calls to this
     * constructor. A type string such as "int" will result in
     * construction of a primitive type. "[2][3]int" will produce
     * two array Types and a primitive type linked together. "[3]st",
     * where <b>st</b> has been declared as a struct, will produce
     * an array type and a struct type.
     * @param   spec is the string specifying the type
     * @param   loc is the source file location
     */
    public Type (String spec, SrcLoc loc) {
        if (spec.equals("-"))
            return;
        makeType(spec, false, false, false, false, loc);
    }

    /**
     * Construct a type from a type specification string.
     * Arrays and structs are built up by recursive calls to this
     * constructor. A type string such as "int" will result in
     * construction of a primitive type. "[2][3]int" will produce
     * two array Types and a primitive type linked together. "[3]st",
     * where <b>st</b> has been declared as a struct, will produce
     * wo array Types and a struct type.
     * @param   spec is the string specifying the type
     * @param   isparam is true if this type is for a module, procedure
     *          or function parameter
     * @param   isarg is true if this type is a module, procedure
     *          or function argument string
     * @param   loc is the source file location
     */
    public Type (String spec, boolean isparam, boolean isarg,  SrcLoc loc) {
        makeType(spec, isparam, isarg, false, false, loc);
    }

    private void makeType (String spec, boolean isparam, boolean isarg, boolean isi, boolean ist, SrcLoc loc) {
        int     w = 0;
        int     i;
        spec = spec.replaceAll(" ", ""); // remove all spaces
        if (spec.startsWith("[")) {
            // an array dimension
            i = spec.indexOf("]");
            if (i < 0)
                throw new ExEx("type format error", loc); // no ']'
            else if (i == 1)
                dim = -1;   // missing dimension
            else
                dim = Functions.intFromString(spec.substring(1, i), "dimension", loc);
            spec = spec.substring(i+1); // remove leading dimension
            array_type = new Type(spec, isparam, isarg, loc);
            if (array_type.hasTargetType())
                has_t_type = true;
            if (array_type.hasImmediateType())
                has_i_type = true;
            if (array_type.hasPointerType())
                has_p_type = true;
            if (array_type.hasEnumType())
                has_e_type = true;
        } else if (spec.startsWith("(")) {
            // a struct
            // check and remove "(" and ")"
            spec = spec.substring(1); // remove "("
            if (!spec.endsWith(")"))
                throw new ExEx("type string - unmatched \"(\"", loc);
            spec = spec.substring(0, spec.length()-1); // remove ")"
            String[] parts = fieldsplit(spec, loc); // break out ','separated fields
            if ((parts.length%2) != 0)
                throw new ExEx("struct specification has odd number of elements", loc);
            for (int j=0 ; j<parts.length ; j+=2) {
                String      fieldname = parts[j];
                ArrayList<String>   al = null;
                //if (fieldname.startsWith("/"))
                //    throw new ExEx("struct field name has leading \"/\"", loc);
                if (fieldname.startsWith("{")) {
                    fieldname = fieldname.substring(1); // remove "{"
                    // check and remove "}"
                    if (!fieldname.endsWith("}"))
                        throw new ExEx("type string - unmatched \"}\"", loc);
                    fieldname = fieldname.substring(0, fieldname.length()-1); // remove "}"
                    String[] ids = fieldname.split(",");
                    al = new ArrayList<String>();
                    for (int k=0 ; k<ids.length ; k++)
                        al.add(ids[k]);
                }

                String  fieldtype = parts[j+1];
                Ident   ident = new Ident(fieldtype);
                Context sc = ident.getScopeContext();
                fieldtype = ident.getId();        
                // see if is a type variable
                Type    t;
                Var     tv = ThreePL.findVar(fieldtype, sc, loc);
                if (tv != null) {
                    Val tval = tv.getVal(null, new NodeList(loc), loc);
                    if (tval.getMode() != Mode.IMMEDIATE)
                        throw new ExEx("struct field type variable not immediate mode", loc);
                    if (tval.getPrimType() == Ptype.TYPE)
                        // type variable
                        t = tval.getSingleTval(loc);
                    else if (tval.getPrimType() == Ptype.STR) {
                        // string variable
                        fieldtype = tval.getSingleSval(loc);
                    } else
                        throw new ExEx("struct field type variable not string or type", loc);
                }
                t = new Type(fieldtype, isparam, isarg, loc);
                
                if (fieldname.equals("extends")) {
                    ArrayList<Field>   fal = t.getFieldIndex();
                    if (fal == null)
                        throw new ExEx("\"extends\" type not a struct", loc);
                    addFields(fal, loc);
                } else {
                    if (al != null) {
                        Iterator<String>    lit = al.iterator();
                        while (lit.hasNext())
                            addField(lit.next(), t, loc);
                    } else
                        addField(fieldname, t, loc);
                }
            }
        } else if (spec.startsWith("{")) {
            // an enum
            prim_type = Ptype.ENUM;
            has_e_type = true;
            // check and remove "{" and "}"
            spec = spec.substring(1); // remove "{"
            if (!spec.endsWith("}"))
                throw new ExEx("type string - unmatched \"{\"", loc);
            spec = spec.substring(0, spec.length()-1); // remove "}"
            String[] parts = fieldsplit(spec, loc); // break out ','separated fields
            if ((parts.length%2) != 0)
                throw new ExEx("enum specification has odd number of elements", loc);
            if (parts.length < 4)
                throw new ExEx("enum specification has fewer than two entries", loc);
            enum_ids = new ArrayList<String>();
            enum_ords = new ArrayList<Long>();
            long    maxenumival = 0;
            for (int j=0 ; j<parts.length ; j+=2) {
                String  enumident = parts[j];
                long    enumival;
                try {
                    enumival = Long.parseLong(parts[j+1]);
                } catch (NumberFormatException e) {
                    throw new ExEx("badly formed enum integer", loc);
                }
                if (enumival < 0)
                    throw new ExEx("enum integer negative", loc);
                if (enum_ids.contains(enumident))
                    throw new ExEx("enum identifier repeated", loc);
                if (enum_ords.contains(enumival))
                    throw new ExEx("enum ordinal repeated", loc);
                enum_ids.add(enumident);
                enum_ords.add(enumival);
                if (enumival > maxenumival)
                    maxenumival = enumival;
            }
            width = Functions.bits(maxenumival, false);
        } else if (spec.startsWith("->")) {
            // a pointer
            prim_type = Ptype.PTR;
            spec = spec.substring(2); // remove '->'
            if (spec.length() != 0)
                throw new ExEx("characters follow '->'", loc);
            has_i_type = true;
        } else {
            // A primitive type string or a type identifier.
            // Extract width specification from end, if any.
            String[] parts = spec.split(":", 3);
            switch (parts.length) {
            case 1:
                // no width spec
                w = -1;
                break;
            case 2:
                // have a width spec
                // empty string after ":" will give width 0
                spec = parts[0];            // trailing :... removed
                // check for "."
                String[] subparts = parts[1].split("\\.", 3);
                switch (subparts.length) {
                case 1:
                    w = Functions.intFromString(subparts[0], "width", loc);
                    break;
                case 2:
                    // target fixed point
                    if (spec.equals("fixed") || spec.equals("ufixed")) {
                        i = Functions.intFromString(subparts[1], "fixed point fraction width", loc);
                        if (i > 255)
                            throw new ExEx("fixed point fraction width too large (" + i + " > 255)", loc);
                        fixoffset = (byte)i;
                        if (fixoffset > 63)
                            throw new ExEx("fixed or ufixed fractional part > 63 bits", loc);
                        w = Functions.intFromString(subparts[0], "fixed point integer width", loc) + fixoffset;
                        break;
                    }
                    // target floating point
                    if (spec.equals("float")) {
                        i = Functions.intFromString(subparts[0], "floating point exponent width", loc);
                        if (i > 255)
                            throw new ExEx("floating point exponent width too large (" + i + " > 255)", loc);
                        bexp = (byte)i;
                        i = Functions.intFromString(subparts[1], "floating point mantissa width", loc);
                        if (i > 255)
                            throw new ExEx("floating point mantissa width too large (" + i + " > 255)", loc);
                        mant = (byte)i;
                        w = mant + bexp + 1;
                        break;
                    }
                    throw new ExEx("'.' in type which is not float or fixed", loc);
                default:
                    throw new ExEx("more than one '.' in type", loc);
                }
                has_t_type = true;
                break;
            default:
                throw new ExEx("more than one ':' in type", loc);
            }
            // extract type
            if (spec.equals("")) {
                throw new ExEx("type string is empty", loc);
            } else if (spec.equals("int")) {
                prim_type = Ptype.INT;
                if (w < 0) {
                    has_i_type = true;
                } else {
                    if ((w < 2) && !isparam)
                        throw new ExEx("target int type must be >= 2 bits", loc);
                    width = w;
                }
            } else if (spec.equals("uint")) {
                prim_type = Ptype.UINT;
                if (w < 0) {
                    has_i_type = true;
                } else {
                    if ((w < 1) && !isparam)
                        throw new ExEx("target uint type must be >= 1 bits", loc);
                    width = w;
                }
                /* old - when immediate uint was not allowed!
                if (w == -999999)
                    throw new ExEx("cannot use uint with immediate mode (width missing)", loc);
                else if ((w < 1) && !isparam)
                    throw new ExEx("target uint type must be >= 1 bits", loc);
                else
                    width = w;
                */
            } else if (spec.equals("bits")) {
                prim_type = Ptype.BITS;
                if (w < 0) {
                    has_i_type = true;
                } else {
                    if ((w < 1) && !isparam)
                        throw new ExEx("target bits type must be >= 1 bits", loc);
                    width = w;
                }
            } else if (spec.equals("str")) {
                prim_type = Ptype.STR;
                if (w >= 0)
                    throw new ExEx("str must not have a width specification", loc);
                has_i_type = true;
            } else if (spec.equals("fixed")) {
                if (w < 0)
                    throw new ExEx("fixed must have a width specification", loc);
                width = w;
                prim_type = Ptype.FIXED;
            } else if (spec.equals("ufixed")) {
                if (w < 0)
                    throw new ExEx("ufixed must have a width specification", loc);
                width = w;
                prim_type = Ptype.UFIXED;
            } else if (spec.equals("float")) {
                if (w < 0)
                     has_i_type = true;
                else
                    width = w;
                prim_type = Ptype.FLOAT;
            } else if (spec.equals("log")) {
                prim_type = Ptype.LOG;
                if (w >= 0)
                    throw new ExEx("log must not have a width specification", loc);
                width = 1;
            } else if (spec.equals("type")) {
                prim_type = Ptype.TYPE;
                if (w >= 0)
                    throw new ExEx("type must not have a width specification", loc);
                has_i_type = true;
            } else if (spec.equals("map")) {
                prim_type = Ptype.MAP;
                if (w >= 0)
                    throw new ExEx("map must not have a width specification", loc);
                has_i_type = true;
            } else if (spec.equals("list")) {
                prim_type = Ptype.LIST;
                if (w >= 0)
                    throw new ExEx("list must not have a width specification", loc);
                has_i_type = true;
            } else if (spec.equals("class")) {
                prim_type = Ptype.CLASS;
                if (w >= 0)
                    throw new ExEx("class must not have a width specification", loc);
                has_i_type = true;
            } else if (spec.equals("file")) {
                prim_type = Ptype.FILE;
                if (w >= 0)
                    throw new ExEx("file must not have a width specification", loc);
                has_i_type = true;
            } else if (spec.equals("empty")) {
                prim_type = Ptype.EMPTY;
                if (w >= 0)
                    throw new ExEx("empty must not have a width specification", loc);
                has_i_type = true;
            } else if (spec.equals("null")) {
                prim_type = Ptype.NULL;
                if (w >= 0)
                    throw new ExEx("null must not have a width specification", loc);
                has_t_type = true;
            } else {
                // A type identifier
                if (w >= 0)
                    throw new ExEx("embedded type identifier variable must not have a width specification", loc);
                for (int index=0 ; index<spec.length() ; index++) {
                    Character    c = spec.charAt(index);
                    if (!Character.isLetterOrDigit(c) && !c.equals('_'))
                        throw new ExEx("embedded type identifier variable contains illegal character", loc);
                }
                Ident   ident = new Ident(spec);
                Context sc = ident.getScopeContext();
                // If the type string being parsed is a module, procedure or function argument
                // and a type identifier has no scope specified, make it CALLING to get the scope
                // of the argument.
                if (isarg && (sc == Context.DEFAULT))
                    sc = Context.CALLING;
                spec = ident.getId();        
                Var     tv = ThreePL.findVar(spec, sc, loc);
                if (tv == null)
                    throw new ExEx("unknown type '" + spec + "'", loc);
                Type    t = tv.getVal(null, new NodeList(loc), loc).getSingleTval(loc);
                prim_type   = t.prim_type;
                width       = t.width;
                fixoffset   = t.fixoffset;
                mant        = t.mant;
                bexp        = t.bexp;
                dim         = t.dim;
                field_map   = t.field_map;
                field_index = t.field_index;
                array_type  = t.array_type;
                enum_ids    = t.enum_ids;
                enum_ords   = t.enum_ords;
                has_i_type  = t.has_i_type;
                has_t_type  = t.has_t_type;
                has_p_type  = t.has_p_type;
                has_e_type  = t.has_e_type;
            }
        }
        if (has_t_type && (has_i_type || has_p_type))
            throw new ExEx("type has both target and immediate or pointer types", loc);
    }

    // split a string into comma-separated fields balanced
    // wrt parentheses
    private String[] fieldsplit (String spec, SrcLoc loc) {
        int countp = 0;
        int countb = 0;
        int i = 0;
        ArrayList<String>   al = new ArrayList<String>();
        for (int j=0 ; j<spec.length() ; j++) {
            char    c = spec.charAt(j);
            switch (c) {
            case '(':
                countp++;
                break;
            case ')':
                countp--;
                break;
            case '{':
                countb++;
                break;
            case '}':
                countb--;
                break;
            case ',':
                if ((countp == 0) && (countb == 0)) {
                    al.add(spec.substring(i, j));
                    i = j + 1;
                }
            }
        }
        if (countp != 0)
            throw new ExEx("struct specification has unbalanced parentheses", loc);
        if (countb != 0)
            throw new ExEx("struct specification has unbalanced braces", loc);
        al.add(spec.substring(i));
        String[]    sa = new String[al.size()];
        for (int k=0 ; k<al.size() ; k++)
            sa[k] = al.get(k);
        return(sa);
    }
       
    /**
     * Copy this Type. The copy is 1 level deeper than a shallow copy.
     * @return  the copy
     */
    @SuppressWarnings("unchecked")
    public Type copy () {
        Type    t = new Type();
        t.prim_type  = prim_type;
        t.width      = width;
        t.fixoffset  = fixoffset;
        t.mant       = mant;
        t.bexp       = bexp;
        t.has_t_type = has_t_type;
        t.has_i_type = has_i_type;
        t.has_p_type = has_p_type;
        t.has_e_type = has_e_type;
        if (array_type != null) {
            t.dim = dim;
            t.array_type = array_type.copy();
        } else if (field_index != null) {
            t.field_map = new TreeMap<String,Field>();
            t.field_index = new ArrayList<Field>();
            for (Field f : field_index)
                t.addField(f.getName(), f.getType().copy(), null);
        } else if (enum_ids != null) {
            t.enum_ids = (ArrayList<String>)enum_ids.clone();
            t.enum_ords = (ArrayList<Long>)enum_ords.clone();
        }
        return(t);
    }
    
    /**
     * Copy the ENUM identifiers and ordinals from another instance.
     * @param   t is the other instance from which to copy the ENUM
     *          identifiers and ordinals
     */
    public void copyEnums (Type t) {
        enum_ids = t.enum_ids;
        enum_ords = t.enum_ords;
    }
    
    /**
     * Set the width field.
     * @param   n is the width
     */
    public void setWidth (int n) {
        width = n;
    }
    
    /**
     * Get the width field.
     * @return   the width
     */
    public int getWidth () {
        return(width);
    }
    
    /**
     * Get the fixed point offset field.
     * @return   the offset
     */
    public int getFixOffset () {
        return(fixoffset);
    }
    
    /**
     * Get the mantissa field.
     * @return   the mantissa width
     */
    public int getMantissaWidth () {
        return(mant);
    }
    
    /**
     * Get the biased exponent field.
     * @return   the biased exponent width
     */
    public int getExponentWidth () {
        return(bexp);
    }
    
    /**
     * Get the enum ID list.
     * @return   the enum ID list
     */
    public ArrayList<String> getEnumIDs () {
        return(enum_ids);
    }
    
    /**
     * Get the enum ordinal list.
     * @return   the enum ordinal list
     */
    public ArrayList<Long> getEnumOrds () {
        return(enum_ords);
    }

    /**
     * Append a struct field to this Type.
     * A new struct field is appended to any existing fields
     * in this struct Type.
     * @param   name is the field identifier
     * @param   type is the field type
     * @param   loc is the source file location
     */
    public void addField (String name, Type type, SrcLoc loc) {
        if (field_map == null) {
            field_map = new TreeMap<String,Field>();
            field_index = new ArrayList<Field>();
        } else if (field_map.containsKey(name))
            throw new ExEx("struct duplicate field '" + name + "'", loc);
        Field   f = new Field(name, type);
        field_map.put(name, f);
        field_index.add(f);
        if (type.hasTargetType())
            has_t_type = true;
        if (type.hasImmediateType())
            has_i_type = true;
        if (type.hasPointerType())
            has_p_type = true;
        if (type.hasEnumType())
            has_e_type = true;
    }

    /**
     * Append a list of struct fields to this Type.
     * A list of new struct fields is appended to any existing fields
     * in this struct Type. Each list item is class <b>Field</b> which
     * is a wrapper for the identifier string and the <b>Type</b> for
     * the field.
     * @param   l is the list of fields
     * @param   loc is the source file location
     */
    public void addFields (ArrayList<Field> l, SrcLoc loc) {
        if (l == null)
            return;
        if (field_map == null) {
            field_map = new TreeMap<String,Field>();
            field_index = new ArrayList<Field>();
        } else {
            for (Field f: l) {
                String  name = f.getName();
                if (field_map.containsKey(name))
                    throw new ExEx("duplicate field " + name, loc);
            }
        }
        for (Field f: l) {
            String  name = f.getName();
            field_map.put(name, f);
            field_index.add(f);
            if (f.getType().hasTargetType())
                has_t_type = true;
            if (f.getType().hasImmediateType())
                has_i_type = true;
            if (f.getType().hasPointerType())
                has_p_type = true;
            if (f.getType().hasEnumType())
                has_e_type = true;
        }
    }
    
    /**
     * Determine if this Type is primitive.
     * @return  true if this is a primitive type
     */
    public boolean isPrimitive() {
        switch (prim_type) {
        case NONE:
        case CLASS:
        case MAP:
        case LIST:
            return(false);
        default:
            return(true);
        }
    }
    
    /**
     * Get the field map, which links the field name to the
     * field type.
     * @return  the field map
     */
    public TreeMap<String,Field> getFieldMap () {
        return(field_map);
    }
    
    /**
     * Get the field index, which lists fields in order of appearance.
     * @return  the field index
     */
    public ArrayList<Field> getFieldIndex () {
        return(field_index);
    }
    
    /**
     * Get the field offset given the field identifier.
     * @param   fname is the field identifier
     * @param   mess is a message string for use in an exception
     * @param   loc is the source file location
     * @return  the field offset
     */
    public int getFieldOffset (String fname, String mess, SrcLoc loc) {
        Field f = field_map.get(fname);
        if (f == null)
            throw new ExEx(mess + " - field '" + fname + "' not found", loc);
        return(field_index.indexOf(f));
    }
    
    /**
     * Get the field type given the field identifier.
     * @param   fname is the field identifier
     * @param   mess is a message string for use in an exception
     * @param   loc is the source file location
     * @return  the field type
     */
    public Type getFieldType (String fname, String mess, SrcLoc loc) {
        Field f = field_map.get(fname);
        if (f == null)
            throw new ExEx(mess + " - field '" + fname + "' not found", loc);
        return(f.getType());
    }
    
    /**
     * get the primitive type. If this is an array or a struct
     * the code returned will be Ptype.NONE.
     * @return  the primitive type
     */
    public Ptype getPrimType () {
        return(prim_type);
    }
    
    /**
     * Get the Type of an array.
     * @return  the array Type
     */
    public Type getArrayType () {
        return(array_type);
    }
    
    /**
     * Get the dimension of an array.
     * @return  the array dimension
     */
    public int getArrayDim () {
        return(dim);
    }
    
    /**
     * Get the ordinal of an enum given the value identifier.
     * @param   id is the value identifier
     * @param   loc is the source file location
     * @return  the ordinal 
     */
    public Long enumOrd (String id, SrcLoc loc) {
        int i = enum_ids.indexOf(id);
        if (i < 0)
            throw new ExEx("enum unknown identifier", loc);
        return(enum_ords.get(i));
    }
    
    /**
     * Get the identifier of an enum given the ordinal.
     * @param   ord is the ordinal
     * @param   loc is the source file location
     * @return  the identifier 
     */
    public String enumIdent (Long ord, SrcLoc loc) {
        int i = enum_ords.indexOf(ord);
        if (i < 0)
            throw new ExEx("enum illegal ordinal", loc);
        return(enum_ids.get(i));
    }
    
    /**
     * Get the first ordinal of an enum.
     * @return  the enum value 
     */
    public Long firstEnumOrd () {
        return(enum_ords.get(0));
    }
    
    /**
     * Get the last ordinal of an enum.
     * @return  the enum value 
     */
    public Long lastEnumOrd () {
        return(enum_ords.get(enum_ords.size()-1));
    }
    
    /**
     * Get the next ordinal of an enum.
     * @param   ord is the current element ordinal
     * @param   loc is the source file location
     * @return  the next ordinal
     */
    public Long nextEnumOrd (long ord, SrcLoc loc) {
        int i = enum_ords.indexOf(ord) + 1;
        if (i == enum_ords.size())
            throw new ExEx("no next enum ordinal", loc);
        return(enum_ords.get(i));
    }
    
    /**
     * Get the previous ordinal of an enum.
     * @param   ord is the current element ordinal
     * @param   loc is the source file location
     * @return  the next ordinal
     */
    public Long prevEnumOrd (long ord, SrcLoc loc) {
        int i = enum_ords.indexOf(ord) - 1;
        if (i < 0)
            throw new ExEx("no previous enum ordinal", loc);
        return(enum_ords.get(i));
    }
    
    /**
     * Determine if there is a next ordinal of an enum.
     * @param   ord is the current element ordinal
     * @param   loc is the source file location
     * @return  true if there is a next ordinal
     */
    public boolean hasNextEnumOrd (long ord, SrcLoc loc) {
        return(enum_ords.indexOf(ord) != (enum_ords.size() - 1));
    }
    
    /**
     * Determine if there is a previous ordinal of an enum.
     * @param   ord is the current element ordinal
     * @param   loc is the source file location
     * @return  true if there is a previous ordinal
     */
    public boolean hasPrevEnumOrd (long ord, SrcLoc loc) {
        return(enum_ords.indexOf(ord) != 0);
    }
     
    /**
     * Determine if any nested Types have omitted widths on primitive
     * types since these are only allowed on immediate types. This is
     * only used by VarProc.java to check that no target types
     * have widths omitted (except 'log').
     * @return  true if any nested primitive types have widths omitted
     */
    public boolean hasImmediateType () {
        return(has_i_type);
    }
     
    /**
     * Determine if any nested Types have pointer
     * types since these are only allowed on immediate types.
     * @return  true if any nested primitive types have pointer types
     */
    public boolean hasPointerType () {
        return(has_p_type);
    }
    
    /**
     * Determine if any nested Types have widths on primitive
     * types since these are only allowed on target types. This is
     * only used by VarProc.java to check that no immediate types
     * have widths specified.
     * @return  true if any nested primitive types have widths specified
     */
    public boolean hasTargetType () {
        return(has_t_type);
    }
     
    /**
     * Determine if any nested Types have enum
     * types.
     * @return  true if any nested primitive types have enum types
     */
    public boolean hasEnumType () {
        return(has_e_type);
    }
   
    /**
     * Check this type and any nested Types, for any missing dimensions.
     * @return  true if any arrays are undimensioned
     */
    public boolean hasUndimArray () {
        if (array_type == null)
            return(false);  // not an array

        // is an array
        if (dim == -1)
            return(true);   // this dimension is missing
 
        // check following dimensions if any
        return(array_type.hasUndimArray());
    }
    
    /**
     * Set any undimensioned array to zero size.
     */
    public void zeroUndimArray () {
        if (array_type != null) {
            if (dim == -1)
                dim = 0;
            // check following dimensions if any
            array_type.zeroUndimArray();
        }
    }
   
    /**
     * Check if this is a primitive target type with missing width.
     * @return  true if this is a primitive target type with missing width
     */
    public boolean hasMissingWidth () {
        if (!has_t_type)
            return(false);
        switch (prim_type) {
        case BITS:
        case UINT:
        case INT:
        case UFIXED:
        case FIXED:
            return(width == 0);
        default:
            return(false);
        }
    }
    
    /**
     * Add primitive type widths to a type.
     * This is used where a parameter has been created with missing primitive widths
     * and these widths are to be determined from the passed argument.
     * @param   values is an array of constants which is used
     *          to determine primitive type widths
     * @param   loc is the source file location
     */
    public void resolveWidths (Object[] values, SrcLoc loc) {
        resolveWidths(0, values, loc);
    }
    
    private void resolveWidths (int offset, Object[] values, SrcLoc loc) {
        has_t_type = true;
        has_i_type = false;
        if (array_type != null) {
            // array
            int     lower = 0;
            int     upper = dim - 1;
            int     n = array_type.numWords();
            for (int i=lower ; i<=upper ; i++)
                array_type.resolveWidths(offset+n*i, values, loc);
        } else if (field_index != null) {
            // struct
            int         size = 0;
            for (Field f : field_index) {
                Type    t = f.getType();
                t.resolveWidths(offset+size, values, loc);
                size += t.numWords();
            }
        } else {
            long   v;
            // primitive type
            switch (prim_type) {
                case BITS:
                    v = ((Long)values[offset]).longValue();
                    width = Functions.bits(v, false);
                    break;
                case UINT:
                    v = ((Long)values[offset]).longValue();
                    width = Functions.bits(v, false);
                    break;
                case INT:
                    v = ((Long)values[offset]).longValue();
                    width = Functions.bits(v, true);
                    break;
                case UFIXED:
                case FIXED:
                    throw new ExEx("target fixed or ufixed type cannot be determined from floating constant", loc);
                case FLOAT:
                    throw new ExEx("target float type cannot yet be determined from floating constant", loc);
                case LOG:
                    width = 1;
                    break;
                case ENUM:
                    break;
                case NULL:
                    width = 0;
                    break;
                default:
                    throw new ExEx("illegal target type", loc);
            }
        }
    }
    
    /**
     * For a parameter which has one or more missing array dimensions
     * check or supply missing dimensions from the dimensional description
     * provided (dimdes). Check correct dimensionality as this proceeds.
     * If a check type is supplied, check the final type. If the
     * final type is a primitive type with missing width and a check
     * type is supplied, correct the width.
     * This method is called recursively for each dimension.
     * @param   dimdes is the array dimensionality - if null an exception
     *          is thrown
     * @param   index is the index into dimdes to match this the current Type
     * @param   ct is the optional argument check type (primitive or struct)
     * @param   mess is a fatal message to be printed if types do not match
     * @param   loc is the source file location
     */
    public void matchArray (
        int[]   dimdes,
        int     index,
        Type    ct,
        String  mess,
        SrcLoc  loc
    ) {
        boolean type_check = (ct != null);
        
        //if (dimdes == null)
        //    throw new ExEx(mess + " - no dimension information for missing dimensions", loc);

        if (!type_check && ((dimdes == null) || (index >= dimdes.length)))
            return;
            
        if ((prim_type != Ptype.NONE)) {
            // Not an array or a struct
            if ((dimdes != null) && (index < dimdes.length))
                throw new ExEx(mess + " - number of dimensions does not match", loc);
            if (!type_check)
                return;
            if (prim_type == ct.getPrimType()) {
                switch (prim_type) {
                case BITS:
                case UINT:
                case INT:
                case UFIXED:
                case FIXED:
                    if (has_t_type && (width == 0)) {
                        if (ct != null) {
                            width = ct.width;
                            fixoffset = ct.fixoffset;
                        }
                    }
                    break;
                case FLOAT:
                    throw new ExEx("target type float not yet implemented", loc);
                default:
                    break;
                }
            } else
                throw new ExEx(mess, loc);
            return;
        }

        if (field_map != null) {
            // a struct
            if ((ct != null) && type_check && !this.isEqual(ct, true))
                throw new ExEx(mess, loc);
            return;
        }
        
        if (array_type != null) {
            // An array
            if (dim <= 0) {
                // has undimensioned array -
                if ((dimdes == null) || (index >= dimdes.length)) {
                    // but there are insufficient missing dimensions supplied
                    throw new ExEx(mess + " - insufficient information for missing dimensions", loc);
                }
                // copy dimension
                dim = dimdes[index];
            } else {
                if (type_check) {
                    // exact dimension check
                    if (dim != dimdes[index])
                        // is dimensioned and dimensions do not match
                        throw new ExEx(mess + " dimensions do not match", loc);
                } else
                    // dimension must be adequate rather than exact
                    if (dim < dimdes[index])
                        // is dimensioned and dimensions is too small for data
                        throw new ExEx(mess + " dimension too small for data", loc);
            }

            // check further dimensions or primitive type
            array_type.matchArray(dimdes, index+1, ct, mess, loc);
        }
    }
    
    /**
     * Compare two types for equality.
     * Structs are compared field by field, so structs with identical
     * fields are considered equal. Types 'int' and 'uint' are considered
     * equal. Widths are not checked since operands of different widths can
     * be assigned, added etc. Arithmetic operators (ExprNode.execute()) do
     * not use this method and check their operands explicitly. Assignment
     * operations (both explicit and parameter/argument assignment) check
     * their operands for an 'int'/'uint' mismatch and implicitly cast
     * where necessary.
     * @param   t is the Type to be compared with this Type
     * @param   strict is true if target primitive widths are to be checked
     * @return  true if the Types are equal
     */
    public boolean isEqual (Type t, boolean strict) {
        if (t == null)
            return(false);
        if (t == this)
            return(true);

        if ((prim_type == Ptype.MAP) && (t.prim_type == Ptype.MAP))
            return(true);

        if ((prim_type == Ptype.LIST) && (t.prim_type == Ptype.LIST))
            return(true);

        if ((prim_type == Ptype.CLASS) && (t.prim_type == Ptype.CLASS))
            return(true);

        if (((prim_type == Ptype.BITS) || (t.prim_type == Ptype.BITS)) && !strict) {
            if (prim_type == Ptype.BITS)
                // for non-strict -
                //  bits : bits or
                //  bits : uint or
                //  bits : int
                // OK
                return((t.prim_type == Ptype.BITS) ||
                       (t.prim_type == Ptype.UINT) ||
                       (t.prim_type == Ptype.INT));
            else
                // for non-strict -
                //  bits : bits or
                //  uint : bits or
                //  int : bits
                // OK
                return((prim_type == Ptype.BITS) ||
                       (prim_type == Ptype.UINT) ||
                       (prim_type == Ptype.INT));
        }

        if (prim_type != Ptype.NONE) {
            if (strict) {
                switch (prim_type) {
                case BITS:
                case UINT:
                case INT:
                    if (width != t.width)
                        return(false);
                    break;
                case UFIXED:
                case FIXED:
                    if (width != t.width)
                        return(false);
                    if (fixoffset != t.fixoffset)
                        return(false);
                    if (mant != t.mant)
                        return(false);
                    if (bexp != t.bexp)
                        return(false);
                    break;
                default:
                    break;
                }
            }
            if (prim_type != t.prim_type) {
                if ((prim_type == Ptype.FIXED) && (t.prim_type == Ptype.UFIXED) ||
                    (prim_type == Ptype.FIXED) && (t.prim_type == Ptype.INT) ||
                    (prim_type == Ptype.FIXED) && (t.prim_type == Ptype.UINT) ||
                    (prim_type == Ptype.UFIXED) && (t.prim_type == Ptype.UINT) ||
                    (prim_type == Ptype.INT) && (t.prim_type == Ptype.FIXED) ||
                    (prim_type == Ptype.INT) && (t.prim_type == Ptype.UINT) /*&& t.has_i_type*/ ||
                    (prim_type == Ptype.UINT) && (t.prim_type == Ptype.INT) /*&& has_i_type*/)
                    // special cases -
                    //      FIXED  : UFIXED
                    //      FIXED  : INT
                    //      FIXED  : UINT
                    //      UFIXED : UINT
                    //      INT    : FIXED
                    //      INT    : UINT
                    //      UINT   : INT
                    // The last should not be allowed but is needed for the
                    // case target uint assigned immediate int which is OK
                    // as long as the immediate value is positive.
                    return(true);
                else
                    return(false);
            }
            if (prim_type == Ptype.ENUM) {
                if (!enum_ids.equals(t.enum_ids))
                    return(false);
                if (!enum_ords.equals(t.enum_ords))
                    return(false);
            }
            return(true);
        }
        if (t.prim_type != Ptype.NONE)
            return(false);
        if (array_type != null) {
            if (dim != t.dim)
                return(false);
            return(array_type.isEqual(t.array_type, strict));
        }
        if (field_map != null) {
            if (t.field_map == null)
                return(false);
            if (field_map.size() != t.field_map.size())
                return(false);

            for (Map.Entry<String,Field> me : field_map.entrySet()) {
                String  id = me.getKey();
                Field   f1 = me.getValue();
                Field   f2 = t.field_map.get(id);
		        if (f2 == null)
			        return(false);
                if (f1 == f2)
                    continue;
                if (!f1.isEqual(f2, strict))
                    return(false);
            }
            return(true);
        }
        return(false);
    }
    
    /**
     * Check primitive types for equality allowing for INT===UINT.
     * @param   p1 is one primitive type
     * @param   p2 is the other primitive type
     * @return  true if the primitve types are 'equal'
     */
    public static boolean checkPrimType (Ptype p1, Ptype p2) {
        if (p1 == p2)
            return(true);
        if ((p1 == Ptype.INT) && (p2 == Ptype.UINT) ||
            (p1 == Ptype.UINT) && (p2 == Ptype.INT))
            return(true);
        return(false);
    }
    
    /**
     * Get the total number of bits for a Type.
     * This is used for target variable assignment.
     * The number of bits for an immediate primitive Type is determined
     * from the type itself and is 64 for typeINT, 63 for typeUINT and
     * 1 for typeLOG (Ptype.STR cannot be assigned to a target variable
     * so the concept of the number of bits is not applicable).
     * The number of bits for a target primitive Type is
     * given by its <b>width</b> field.
     * @return  the number of bits
     */
    public int numBits () {
        // primitive type
        if (prim_type != Ptype.NONE)
            return(width);

        // array
        if (array_type != null)
            return(dim * array_type.numBits());

        // struct
        int         size = 0;
        for (Field f : field_index) {
            Type    t = f.getType();
            size += t.numBits();
        }

        return(size);
    }
    
    /**
     * Get the total number of words for a type. A word is a primitive
     * Type.
     * @return  the number of words
     */
    public int numWords () {
        // primitive type
        if (prim_type != Ptype.NONE) {
            switch (prim_type) {
            case EMPTY:
            //case NULL:
                return(0);
            default:
                return(1);
            }
        }

        // array
        if (array_type != null)
            return(dim * array_type.numWords());

        // struct
        if (field_index != null) {
            int         size = 0;
            for (Field f : field_index) {
                Type    t = f.getType();
                size += t.numWords();
            }
            return(size);
        }
        
        // Ptype.NONE but not array or struct!
        throw new ExEx("SYSTEM ERROR - Type.numWords() typeNONE");
    }

    /**
     * Get the word specification for a type.
     * The parameter <b>spec</b> is a <b>SubFieldList</b> specifying
     * subscripts or fields for a variable reference in the code tree. The
     * length of 'spec' must match the depth of the type.
     * The returned <b>WordSpec</b> contains bit ranges. Each
     * bit range gives the lower and upper offsets of a word field.
     * @param   var is the associated variable or null
     * @param   sfl is a list of subscripts and fields or null
     * @param   loc is the source file location
     * @return  the word specification
     */
    public WordSpec getWordSpec (Var var, SubFieldList sfl, SrcLoc loc) {
        WordSpec    ws = getWordSpec(var, sfl, 0, loc);
        ws.setVar(var);
        ws.processVarSubs(loc); // variable subscript decoder logic, if any
        return(ws);
    }

    /**    
     * Get word specification for a type where no subscripts
     * or fields have been given.
     * The returned <b>WordSpec</b> contains one bit range from 0 to last word offset.
     * @param   var is the associated variable if any
     * @param   loc is the source file location
     * @return  the word specification
     */
    public WordSpec getWordSpec (Var var, SrcLoc loc) {
        WordSpec    ws = getWordSpec(var, null, 0, loc);
        ws.setComplete();
        ws.setVar(var);
        return(ws);
    }
    
    /**
     * Get the word specification for a type.
     * The parameter <b>sfl</b> is a <b>SubFieldList</b> specifying
     * subscripts or fields for a variable reference in the code tree. The
     * length of 'sfl' must match the depth of the type.
     * The returned <b>WordSpec</b> contains bit ranges. Each
     * bit range gives the lower and upper offsets of a word field.
     * @param   var is the associated variable or null
     * @param   sfl is a list of subscripts and fields or null
     * @param   sfoffset gives the current reference point in 'sfl'. In the
     *          top level call this is zero.
     * @param   loc is the source file location
     * @return  the word specification
     */
    private WordSpec getWordSpec (Var var, SubFieldList sfl, int sfoffset, SrcLoc loc) {
        WordSpec    wordspec = null;
        SubField    sf = null;
        int         sizew = 0;
        int         sizeb = 0;
        Val         subval = null;
        
        if (prim_type == Ptype.ENUM) {
            // if there is a subfield check that it is a field
            // and that the field is a key in the enum map
            sf = ((sfl == null) || (sfl.size() <= sfoffset)) ? null : sfl.getEntry(sfoffset);
            if (sf != null) {
                if (sf.getType() != SFType.FIELD)
                    throw new ExEx("Variable '" + var.getID(IDtype.SLITERAL) + "' - enum type has subscript(s)", loc);
                String      name = sf.getField();
                if (!enum_ids.contains(name))
                    throw new ExEx("Variable '" + var.getID(IDtype.SLITERAL) + "' - enum unknown value", loc);
            }
            wordspec = new WordSpec(this, var);
            if (sfl != null)
                sfl.setScanIndex(sfoffset+1);
            
            return(wordspec);
        }

        if ((prim_type == Ptype.MAP) || (prim_type == Ptype.LIST) || (prim_type == Ptype.CLASS)) {
            wordspec = new WordSpec(prim_type, false, var);
            wordspec.setVar(var);
            return(wordspec);
        } else if ((sfl == null) || (sfoffset >= sfl.size())) {
            if (prim_type != Ptype.NONE) {
                // primitive element
                if (has_i_type)
                    // immediate mode
                    wordspec = new WordSpec(prim_type, false, var);
                else {
                    // a target mode
                    if (width == 0)
                        switch (prim_type) {
                        case BITS:
                        case UINT:
                        case INT:
                            throw new ExEx("Variable '" + var.getID(IDtype.SLITERAL) + "' - target bits, uint or int has zero width", loc);
                        case FIXED:
                            throw new ExEx("Variable '" + var.getID(IDtype.SLITERAL) + "' - target fixed has zero width", loc);
                        case FLOAT:
                            throw new ExEx("Variable '" + var.getID(IDtype.SLITERAL) + "' - target float has zero width", loc);
                        default:
                            break;
                        }
                    wordspec = new WordSpec(0, 0, width-1, this.fixoffset, mant, bexp, enum_ids, enum_ords, prim_type, var);
                }
                wordspec.setVar(var);
                return(wordspec);
            } else {
                // trailing elements omitted
                sf = null;
            }
        } else
            sf = sfl.getEntry(sfoffset);

        if (array_type != null) {
            //
            // array subscript or range
            //
            int     lower = 0;
            int     upper = 0;
            boolean reversed = false;
            if ((sf == null) || (sf.getType() == SFType.NULL)) {
                lower = 0;
                upper = dim - 1;
            } else if (sf.getType() == SFType.IMSUB) {
                lower = sf.getLower();
                upper = lower;
            } else if (sf.getType() == SFType.IMSUBS) {
                lower = sf.getLower();
                upper = sf.getUpper();
                if (lower > upper) {
                    int temp = lower;
                    lower = upper;
                    upper = temp;
                    reversed = true;
                }
            } else if (sf.getType() == SFType.TARSUB) {
                lower = 0;
                upper = dim - 1;
                subval = sf.getSubVal();
            } else
                throw new ExEx("Variable '" + var.getID(IDtype.SLITERAL) + "' - field name instead of subscript", loc);
            if (lower < 0)
                throw new ExEx("Variable '" + var.getID(IDtype.SLITERAL) + "' - subscript negative (" + dim + ")", loc);
            if (upper >= dim)
                throw new ExEx("Variable '" + var.getID(IDtype.SLITERAL) + "' - subscript out of range - " + upper + " in [" + dim + "]", loc);
            if (sfl != null)
                sfl.setScanIndex(sfoffset+1);   
            wordspec = new WordSpec();
            int nw = array_type.numWords();
            int nb = array_type.numBits();
            sizeb = (reversed ? upper : lower) * nb;
            sizew = (reversed ? upper : lower) * nw;
            WordSpec ws = array_type.getWordSpec(var, sfl, sfoffset+1, loc);
            wordspec.setDimDes(ws.getDimDes());
            wordspec.setCheckType(ws.getCheckType());
            if (((sf == null) || (sf.getType() != SFType.IMSUB)) &&
                (subval == null))
                // Have a subscript range.
                // A target variable subscript is not a range as far
                // as dimensionality checks are concerned, even though
                // we have a range on WordSpec
                wordspec.prependToDimDes(upper-lower+1);
            int first = reversed ? upper : lower;
            int last = reversed ? lower-1 : upper+1;
            int incrdecr = reversed ? -1 : 1;
            for (int i=first ; i!=last ; i+=incrdecr) {
                // adjust pairs from recursive call
                WordSpec wsc = new WordSpec(ws);
                for (int j=0 ; j<wsc.numWords() ; j++) {
                    wsc.addToWord(j, sizew);
                    wsc.addToSubs(j, sizeb);
                    wsc.addIndex(j, Integer.valueOf(i));
                }
                wordspec.append(wsc);
                sizew += reversed ? -nw : nw;
                sizeb += reversed ? -nb : nb;
            }
            wordspec.addSubVal(subval);
            wordspec.appendSubVal(ws);
        } else if (field_map != null) {
            //
            // struct field
            //
            if ((sf != null) && (sf.getType() != SFType.FIELD))
                throw new ExEx("subscript instead of field name", loc);
            if (sfl != null)
                sfl.setScanIndex(sfoffset+1);
            wordspec = new WordSpec();
            wordspec.addSubVal(null);   // no target subscript for a field
            if (sf == null) {
                // no field name - get all fields
                for (Field f : field_index) {
                    String  id = f.getName();
                    Type    t = f.getType();
                    if (sf == null) {
                        WordSpec ws = t.getWordSpec(var, sfl, sfoffset+1, loc);
                        for (int j=0 ; j<ws.numWords() ; j++) {
                            ws.addToWord(j, sizew);
                            ws.addToSubs(j, sizeb);
                            ws.addIndex(j, id);
                        }
                        wordspec.append(ws);
                        wordspec.appendSubVal(ws);
                    }
                    sizew += t.numWords();
                    sizeb += t.numBits();
                }
                wordspec.setCheckType(this);
            } else {
                // field name - search struct for it
                String      name = sf.getField();
                for (Field f : field_index) {
                    String  id = f.getName();
                    Type    t = f.getType();
                    if (id.equals(name)) {
                        WordSpec ws = t.getWordSpec(var, sfl, sfoffset+1, loc);
                        for (int j=0 ; j<ws.numWords() ; j++) {
                            ws.addToWord(j, sizew);
                            ws.addToSubs(j, sizeb);
                            ws.addIndex(j, id);
                        }
                        wordspec.append(ws);
                        wordspec.appendSubVal(ws);
                        wordspec.setDimDes(ws.getDimDes());
                        wordspec.setCheckType(ws.getCheckType());
                        wordspec.setVar(var);
                        return(wordspec);
                    }
                    sizew += t.numWords();
                    sizeb += t.numBits();
                }
                throw new ExEx("Variable '" + var.getID(IDtype.CHAIN) + "' - field '" + name + "' not found", loc);
            }
        } else {
            if (sf.getType() == SFType.FIELD)
                throw new ExEx("Variable '" + var.getID(IDtype.CHAIN) + "' - struct field used on non-struct", loc);
            else
                throw new ExEx("Variable '" + var.getID(IDtype.CHAIN) + "' - subscript used on non-array", loc);
        }

        return(wordspec);
    }
    
    /**
     * Given a component index return the equivalent SubFieldList.
     * @param   index is the component index
     * @param   loc is the source file location
     * @return  the equivalent SubFieldList
     */
    public SubFieldList indexToSubFieldList (int index, SrcLoc loc) {
        SubFieldList    sfl = new SubFieldList();
        indexToSubFieldList_(sfl, index, loc);
        return(sfl);
    }
    
    private void indexToSubFieldList_ (SubFieldList sfl, int index, SrcLoc loc) {
        if (array_type != null) {
            int n = array_type.numWords();
            sfl.add(new SubField(index / n, 0, loc));
            array_type.indexToSubFieldList_(sfl, index % n, loc);
        } else if (field_index != null) {
            int lo = 0;
            int hi;
            for (Field f: field_index) {
                hi = lo + f.getType().numWords();
                if (index < hi) {
                    sfl.add(new SubField(f.getName(), loc));
                    f.getType().indexToSubFieldList_(sfl, index - lo, loc);
                    return;
                }
                lo = hi;
            }
        }
    }
    
    /**
     * Convert any immediate float entries in this type
     * to target float structs.
     * @param   loc is the source file location
     */
    public void floatsToTarget (SrcLoc loc) {
        if (prim_type != Ptype.NONE) {
            // primitive type
            if (prim_type != Ptype.FLOAT)
                return;
            // convert float to float:23e8
            mant = 23;
            bexp = 8;
            width = 32;
            has_i_type = false;
            has_t_type = true;
            return;
        } else if (dim != 0) {
            // array
            array_type.floatsToTarget(loc);
            return;
        } else {
            // must be a struct
            for (Field f : field_index)
                f.getType().floatsToTarget(loc);
        }
    }
    
    /**
     * Get the type as a string. This will be zero or more
     * dimensions in brackets followed by either a primitive type
     * name, a type mode identifier or a struct.
     * @return  the type String
     */
    public String getTypeString () {
        String      typestring;
        if (dim != 0)
            typestring = "[" + dim + "]" + array_type.getTypeString();
        else if (enum_ids != null) {
            StringBuffer    sb = new StringBuffer("{");
            for (int i=0 ; i<enum_ids.size() ; i++) {
                if (i != 0)
                    sb.append(",");
                sb.append(enum_ids.get(i));
                sb.append(",");
                sb.append(enum_ords.get(i));
            }
            sb.append("}");
            typestring = sb.toString();
        } else if (prim_type != Ptype.NONE)
            typestring = getPrimTypeName();
        else {
            int i = 0;
            typestring = "(";
            for (Field f: field_index) {
                if (i++ != 0)
                    typestring += ", ";
                typestring += f.getName();
                typestring += ", ";
                typestring += f.getType().getTypeString();
            }
            typestring += ")";
        }

        return(typestring);
    }
    
    /**
     * Check that this type contains only "str" primitives, as a single type or
     * in an array or struct or any such nested compound type.
     * @return  true if this type contains only "str" primitives.
     *
     * NO LONGER USED!
    public boolean hasOnlyStr () {
        if (prim_type != Ptype.NONE)
            // A primitive - return true if "str", else false.
            return(prim_type == Ptype.STR);
        if (dim != 0)
            // An array - return check on array type.
            return(array_type.hasOnlyStr());
        else {
            // A struct - check each field. Return false if any fail, else true.
            for (Field f : field_index)
                if (!f.getType().hasOnlyStr())
                    return(false);
            return(true);
        }
    }*/
    
    /**
     * get the primitive type as a string. If this is not a primitive
     * type, "no type" will be returned. If the primitive type is a
     * target type, this will have a ":" appended.
     * @return  the primitive type as a string
     */
    public String getPrimTypeName () {
        if ((prim_type != Ptype.LOG) && (prim_type != Ptype.PTR))
            if (width != 0) {
                if ((prim_type == Ptype.FIXED) || (prim_type == Ptype.UFIXED))
                    return(prim_type.typename() + ":" + (width - fixoffset) + "." + fixoffset);
                else if ((prim_type == Ptype.FLOAT) && has_t_type)
                    return("float:" + mant + "." + bexp);
                else
                    return(prim_type.typename() + ":" + width);
            } else
                return(prim_type.typename());
        else
            return(prim_type.typename());

        /* old - when immediate uint could not be declared!
        if (has_t_type && (prim_type != Ptype.LOG) && (prim_type != Ptype.PTR))
            if (width != 0)
                return(prim_type.typename() + ":" + width);
            else
                return(prim_type.typename());
        else if (has_i_type && (prim_type == Ptype.UINT))
            return("int");
        else
            return(prim_type.typename());
        */
    }
    
    /**
     * Get an array of primitive type codes for this Type.
     * @return  the array of type codes
     */
    public Ptype[] getPTypeArray () {
        Ptype[] ta = new Ptype[numWords()];
        getPTypeArray(ta, 0);
        return(ta);
    }

    private void getPTypeArray (Ptype[] ta, int offset) {
        if (array_type != null) {
            // array
            int     lower = 0;
            int     upper = dim - 1;
            int     n = array_type.numWords();
            for (int i=lower ; i<=upper ; i++)
                array_type.getPTypeArray(ta, offset+n*i);
        } else if (field_index != null) {
            // struct
            int         size = 0;
            for (Field f : field_index) {
                Type    t = f.getType();
                t.getPTypeArray(ta, offset+size);
                size += t.numWords();
            }
        } else {
            // primitive type
            ta[offset] = prim_type;
        }
    }
    
    /**
     * Get an array of primitive types for this Type.
     * @return  the array of type codes
     */
    public Type[] getTypeArray () {
        Type[] ta = new Type[numWords()];
        getTypeArray(ta, 0);
        return(ta);
    }

    private void getTypeArray (Type[] ta, int offset) {
        if (array_type != null) {
            // array
            int     lower = 0;
            int     upper = dim - 1;
            int     n = array_type.numWords();
            for (int i=lower ; i<=upper ; i++)
                array_type.getTypeArray(ta, offset+n*i);
        } else if (field_index != null) {
            // struct
            int         size = 0;
            for (Field f : field_index) {
                Type    t = f.getType();
                t.getTypeArray(ta, offset+size);
                size += t.numWords();
            }
        } else {
            // primitive type
            ta[offset] = this;
        }
    }
    
    /*
     * Unwrap a single array member or single field struct.
     * @param   loc is the source file location
     * @return  the primitive type
    public Type unwrap (SrcLoc loc) {
        if (prim_type != Ptype.NONE)
            // a primitive
            return(this);
        if (field_map != null) {
            // a struct
            if (field_index.size() != 1)
                throw new ExEx("SYSTEM ERROR: Type.unwrap() struct", loc);
            Field   f = (Field)field_index.get(0);
            return(f.getType().unwrap(loc));
        }
        // an array
        if (dim != 1)
            throw new ExEx("SYSTEM ERROR: Type.unwrap() array", loc);
        return(array_type.unwrap(loc));
    }
     */
}
