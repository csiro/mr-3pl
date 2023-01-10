package threepl.mods;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import threepl.exceptions.ExEx;


/**
 * This class is only instantiated once as a static class in main.
 * It is a map of all inbuilt modules.
 */
public class InbuiltMods {
    static TreeMap<String, InbuiltMod>     mods;

    /**
     * Construct the map of all inbuilt modules.
     * Each inbuilt module has an identifier and
     * a class with a <b>execute</b> method which implements the module.
     * This class contains a map whose indices are the module identifiers
     * and whose values are the module classes.
     * The explicit module identifiers and associated classes are coded
     * into the body of this constructor.
     */
    public InbuiltMods () {
        mods = new TreeMap<String, InbuiltMod>();
        
        mods.put("resync", new ResyncMod());
        mods.put("del", new DelMod());
    }
    
    /**
     * Search the inbuilt module map for a called module.
     * @param   id is the module identifier
     * @return  the inbuilt module
     */
    public InbuiltMod findMod (String id) {
        return mods.get(id);
    }
    
    /**
     * Check if an inbuilt module with the given identifier exits.
     * @param id is the identifier
     * @return  true if the inbuilt module exists
     */
    public static boolean modDefined(String id) {
        return(mods.containsKey(id));
    }
    
    /**
     * Search the inbuilt module map for the identifier of a module.
     * This is only used to recover the module identifier when invoking
     * execution via a pointer.
     * @param   m is the module
     * @return  the module identifier
     */
    public static String findModId (InbuiltMod m) {
        Set<Entry<String, InbuiltMod>>         es = mods.entrySet();
        Iterator<Entry<String, InbuiltMod>>    it = es.iterator();
        while (it.hasNext()) {
            Map.Entry<String, InbuiltMod>   me = it.next();
            if (m.equals(me.getValue()))
                return(me.getKey());
        }
        throw new ExEx("InbuiltMods: SYSTEM ERROR");
    }
}
