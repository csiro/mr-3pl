package threepl.exec;


/**
 * This wraps an enumerated constant.
 */
public class EnumConst {
    public long    val;
    public Type    type;
    
    public EnumConst (Type t, long v) {
        type = t;
        val = v;
    }
}
