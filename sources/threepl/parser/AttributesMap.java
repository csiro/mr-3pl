package threepl.parser;

import java.util.TreeMap;

import threepl.exec.Type;
import threepl.exec.Val;
import threepl.parser.Constant.Ptype;

/**
 * This class is an extension of class HashMap and contains maps of
 * attributes allowed for each mode. This map has entries which are
 * themselves maps and whose keys are variable modes. Thus there
 * is a map for each mode for which there are attributes. Each
 * mode map contains entries whose keys are the attribute name and
 * whose values are the attribute value primitive type. Using this
 * class an attribute can be checked to see if it is recognised for
 * the variable mode and the attribute type can be checked.
 *
 * There is only one static instance of this class, in exec.Var().
 * This holds attributes handled explicitly by 3PL but not constraints
 * processed by library procedures.
 */
@SuppressWarnings("serial")
public class AttributesMap extends TreeMap<Constant.Mode,TreeMap<String,Ptype>> implements Constant {

    public AttributesMap () {
        put(Mode.IMMEDIATE, new TreeMap<String,Ptype>());
        put(Mode.VALUE, new TreeMap<String,Ptype>());
        put(Mode.SELECTVALUE, new TreeMap<String,Ptype>());
        put(Mode.STATIC, new TreeMap<String,Ptype>());
        put(Mode.QUEUE, new TreeMap<String,Ptype>());
        put(Mode.PRIORITY, new TreeMap<String,Ptype>());
        put(Mode.INPUT, new TreeMap<String,Ptype>());
        put(Mode.OUTPUT, new TreeMap<String,Ptype>());
        put(Mode.CMEMORY, new TreeMap<String,Ptype>());
        put(Mode.RMEMORY, new TreeMap<String,Ptype>());
        put(Mode.CLOCK, new TreeMap<String,Ptype>());
    }

    
    public void addAttribute (Mode mode, String key, Ptype ptype) {
        TreeMap<String,Ptype> hm = get(mode);
        hm.put(key, ptype);
    }

    /**
     * Return a string describing the allowed primitive type for an
     * attribute for use in a message.
     * @param   mode is the variable mode
     * @param   key is the attribute key
     * @return  a string describing the required attribute primitive type
     */
    public String getAttributePrimType (Mode mode, String key) {
        TreeMap<String, Ptype> hm = get(mode);
        if (hm.containsKey(key)) {
                return(hm.get(key).typename());
        }
        return("none");
    }
    
    /**
     * Check that an attribute is allowed for this
     * variable mode and its value is the correct type.
     * The allowed type may be an array as well as a single value.
     * If the type is NULL assume it is OK.
     * @param   key is the attribute key
     * @param   val is the attribute value
     * @param   mode is the variable mode
     * @return  the attribute check result
     */
    public ACT checkAttribute (String key, Val val, Mode mode) {
        TreeMap<String,Ptype> hm = get(mode);
        if (hm == null)                                         // no entry for mode
            return(ACT.UNRECOGNISED);
        if (!hm.containsKey(key))                               // no entry for key
            return(ACT.UNRECOGNISED);
        Ptype ptype = hm.get(key);
        if (ptype == Ptype.NULL)
            return(ACT.OK);                                     // OK if type given as NULL
        if (key.endsWith("domain") && (val.getWordSpec().getCheckType().getPrimType() == Ptype.NULL))
            return(ACT.OK);                                     // OK if domain, readdomain or writedomain are null
        Type    t = val.getType();
        if (t.getArrayType() != null) {
            // Value is an array - see if the array type matches the allowed type
            Ptype pt = t.getArrayType().getPrimType();
            if (!Type.checkPrimType(hm.get(key), pt))
                return(ACT.WRONG_TYPE);                         // wrong array type
        } else if (!Type.checkPrimType(hm.get(key), val.getPrimType()))
            return(ACT.WRONG_TYPE);                             // wrong type
        return(ACT.OK);
    }
}
