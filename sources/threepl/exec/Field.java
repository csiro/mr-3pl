package threepl.exec;


/**
 * A field entry in a Type class.
 */
public class Field {
    private String  name;
    private Type    type;
    
    /**
     * Construct a field entry for a Type.
     * @param   id is the field identifier
     * @param   t is the field Type
     */
    public Field (String id, Type t) {
        name = id;
        type = t;
    }
    
    /**
     * Get the name of this field.
     * @return  the field identifier string
     */
    public String getName () {
        return(name);
    }
    
    /**
     * Get the type of this field.
     * @return  the field Type
     */
    public Type getType () {
        return(type);
    }
    
    /**
     * Compare two struct fields for equality.
     * The field identifiers must be the same and the types must pass the
     * type equality method. Types 'int' and 'uint' are considered equal.
     * Widths are not checked since operands of different widths can be
     * assigned, added etc. Arithmetic operators (ExprNode.execute()) do
     * not use this method and check their operands explicitly. Assignment
     * operations (both explicit and parameter/argument assignment) check
     * their operands for an 'int'/'uint' mismatch and implicitly cast
     * where necessary.
     * @param   f is the Field to be compared with this Field
     * @param   strict is true if target primitive widths are to be checked
     * @return  true if the Fields are equal
     */
    public boolean isEqual (Field f, boolean strict) {
        if (!name.equals(f.name))
            return(false);
        return(type.isEqual(f.type, strict));
    }
}
