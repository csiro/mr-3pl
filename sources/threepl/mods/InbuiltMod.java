package threepl.mods;

import java.util.TreeMap;

import threepl.exceptions.ExEx;
import threepl.nodes.NodeList;


/**
 * This is the calling interface for inbuilt procedures.
 */
public class InbuiltMod {
    public boolean                  allowed_attributes;
    public TreeMap<String,Integer>  ipnames;
    public TreeMap<String,Integer>  opnames;
    
    public InbuiltMod () {
        ipnames = new TreeMap<String,Integer>();
        opnames = new TreeMap<String,Integer>();
        allowed_attributes = false;
    }

    /**
     * Execute an inbuilt module.
     * @param   inargs is a list of input argument tree nodes
     * @param   outargs is a list of output argument tree nodes
     */
    public void execute (NodeList inargs, NodeList outargs) {
        throw new ExEx("InbuiltMod sys error 1");
    }
}
