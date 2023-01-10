package threepl.exec;

import threepl.exceptions.ExEx;
import threepl.nodes.Node;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

/**
 * This class is a subscript or field descriptor.
 * This is used for subscript/field descriptor lists and can be -
 * <UL>
 * <LI> a missing subscript - type SFType.NULL
 * <LI> a single immediate subscript - type SFType.IMSUB
 * <LI> an immediate subscript range - type SFType.IMSUBS
 * <LI> a target subscript expression - type SFType.TARSUB
 * <LI> a field name - type SFType.FIELD
 * </UL>
 */
public class SubField implements Constant {
    private SFType  type;   // descriptor type
    private int     lower;  // immediate constant lower or only subscript
    private int     upper;  // immediate constant upper subscript
    private String  field;  // field or map key identifier
    private Val     subval; // target subscript value
    private SrcLoc  loc;    // source file location

    /**
     * Construct a subscript/field descriptor from one or two Nodes, n1 and n2.
     * <UL>
     * <LI> If evaluation of 'n1' gives an integer, it is the lower or only subscript.
     * <LI> If evaluation of 'n1' gives a string, it is a field name.
     * <LI> If evaluation of 'n2' gives an integer, it is the upper subscript for a
     *      range, otherwise 'n2' is null.
     * <LI> If both 'n1' and 'n2' are null then this is a missing subscript for which
     *      the full dimension will be used.
     * </UL>
     * @param   n1 is the first or only subscript or field string
     * @param   n2 is the upper subscript of a range or else null
     * @param   sub is true if this is from subscript syntax [...]
     * @param   l is the source file location
     */
    public SubField (Node n1, Node n2, boolean sub, SrcLoc l) {
        loc = l;
        Val v1 = (n1 == null) ? null : n1.getVal();
        Val v2 = (n2 == null) ? null : n2.getVal();

        if (sub) {
            // is an array subscript, array subscript pair or map key
            if (v2 == null) {
                if (v1 == null) {
                    type = SFType.NULL;
                    return;
                }
                if (v1.getMode()== Mode.IMMEDIATE) {
                    switch (v1.getPrimType()) {
                    case INT:
                    case UINT:
                        //  single immediate subscript
                        type = SFType.IMSUB;
                        lower = (int)v1.getSingleIval(loc);
                        upper = lower;
                        return;
                    case STR:
                        //  map key
                        type = SFType.KEY;
                        lower = upper = 0;
                        field = v1.getSingleSval(loc);
                        return;
                    default:
                        throw new ExEx("subscript type error", loc);
                    }
                } else {
                    //  target subscript TDE variable
                    switch (v1.getPrimType()) {
                    case UINT:
                    case INT:
                        type = SFType.TARSUB;
                        lower = upper = 0;
                        subval = v1;
                        return;
                    default:
                        throw new ExEx("subscript type error", loc);
                    }
                }
            } else {
                // must be immediate subscript pair
                if (v1.getMode() != Mode.IMMEDIATE ||
                    (v1.getPrimType() != Ptype.INT) && (v1.getPrimType() != Ptype.UINT) ||
                    v2.getMode() != Mode.IMMEDIATE ||
                    (v2.getPrimType() != Ptype.INT) && (v1.getPrimType() != Ptype.UINT) ) {
                    throw new ExEx("subscript pair type error", loc);
                }
                type = SFType.IMSUBS;
                lower = (int)v1.getSingleIval(loc);
                upper = (int)v2.getSingleIval(loc);
            }
        } else {
            // is a struct field
            type = SFType.FIELD;
            lower = upper = 0;
            field = v1.getSingleSval(loc);
            return;
        }
    }

    /**
     * Construct a subscript descriptor from one or two integers, n1 and n2.
     * If n2 is 0, n1 is a single subscript, otherwise n1 and n2 are a
     * subscript range
     * @param   n1 is the first or only subscript
     * @param   n2 is the upper subscript of a range or else 0
     * @param   l is the source file location
     */
    public SubField (int n1, int n2, SrcLoc l) {
        loc = l;
        lower = n1;
        if (n2 == 0) {
            type = SFType.IMSUB;
        } else {
            upper = n2;
            type = SFType.IMSUBS;
        }
    }

    /**
     * Construct a field descriptor from a field name.
     * @param   s is the field identifier
     * @param   l is the source file location
     */
    public SubField (String s, SrcLoc l) {
        loc = l;
        type = SFType.FIELD;
        field = s;
    }
    
    /**
     * Get the subscript/field descriptor type, i.e
     * SFType.NULL, SFType.IMSUB, SFType.IMSUBS, SFType.TARSUB or
     * SFType.FIELD.
     * @return  the SubField type
     */
    public SFType getType () {
        return(type);
    }
    
    /**
     * Get a field name string.
     * @return  the field name string
     * @throws  ExEx if the SubField is not a field name
     */
    public String getField() {
        if (type != SFType.FIELD)
            throw new ExEx("SubField.getField() type error", loc);
        return(field);
    }
    
    /**
     * Get a map key string.
     * @return  the map key string
     * @throws  ExEx if the SubField is not a map key
     */
    public String getKey() {
        if (type != SFType.KEY)
            throw new ExEx("map key expected", loc);
        return(field);
    }
    
    /**
     * Get the lower or only subscript.
     * @return  the subscript
     * @throws  ExEx if the SubField is not a subscript
     */
    public int getLower() {
        if (type != SFType.IMSUB && type != SFType.IMSUBS)
            throw new ExEx("immediate subscript expected", loc);
        if (lower < 0)
            throw new ExEx("-ve subscript or index", loc);
        return(lower);
    }
    
    /**
     * Get the upper subscript.
     * @return  the upper subscript
     * @throws  ExEx if the SubField is not a subscript pair
     */
    public int getUpper() {
        if (type != SFType.IMSUB && type != SFType.IMSUBS)
            throw new ExEx("immediate subscript expected", loc);
        if (upper < 0)
            throw new ExEx("-ve upper subscript", loc);
        return(upper);
    }
    
    /**
     * Get the target variable subscript value.
     * @return  the target variable subscript value
     * @throws  ExEx if the SubField is not a target subscript
     */
    public Val getSubVal () {
        if (type != SFType.TARSUB)
            throw new ExEx("target subscript expected", loc);
        return(subval);
    }
    
    /**
     * Get the source file location.
     * @return  the source file location
     */
    public SrcLoc getSrcLoc () {
        return(loc);
    }
}
