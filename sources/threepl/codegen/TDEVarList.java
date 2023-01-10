package threepl.codegen;

import java.util.ArrayList;

import threepl.exec.Var;
import threepl.netlist.Element;
import threepl.netlist.Net;
import threepl.netlist.NetConstants;
import threepl.parser.Constant;

/**
 * This class implements a list of TDEVars.
 */
@SuppressWarnings("serial")
public class TDEVarList extends ArrayList<TDEVar>
                        implements Constant, TDEConstants, NetConstants {
    
    /**
     * Get a net from a single bit signal in the list.
     * @param   i is the index into the list
     * @return  the net
     */
    public Net getNet (int i) {
        if (i >= size())
            return(null);
        TDEVar  tdev = get(i);
        if (tdev == null)
            return(null);
        Net n = Net.get(tdev.getIdBit());
        if (tdev.getClockMode() == ClkType.NEG) {
            // a negative clock - insert an inverter
            Element c = new Element("INV", Gtype.INV);
            Net     o = new Net();
            c.addInput("I", n);
            c.addOutput("O", o);
            return(o);
        }
        return(n);
    }
    
    /**
     * Get a net array from a multi-bit signal in the list.
     * If the list element at that index is a single signal,
     * return it as an array of size 1. Return null if the
     * index is off the end of the list.
     * @param   i is the index into the list
     * @return  the net array
     */
    public Net[] getNetArray (int i) {
        if (i >= size())
            return(null);

        TDEVar  tdev = get(i);

        if (tdev == null)
            return(null);

        if (tdev.getWordSpec() == null) {
            Net[]   ret = new Net[1];
            ret[0] = Net.get(tdev.getIdBit());
            return(ret);
        }

        int width = tdev.numBits();
        Net[]   ret = new Net[width];
        for (int bit=0 ; bit<width ; bit++)
            ret[bit] = Net.get(tdev.getIdBit(bit));
        return(ret);
    }
    
    /**
     * Get a net array of a specified size from a multi-bit signal in
     * the list. Truncate or zero pad the array to fit the required
     * size. Return null if the index is off the end of the list.
     * @param   i is the index into the list
     * @param   size is the required size of the array
     * @return  the net array
     */
    public Net[] getNetArray (int i, int size) {
        if (i >= size())
            return(null);

        TDEVar  tdev = get(i);
        if (tdev == null)
            return(null);

        Net[]   ret = new Net[size];
        int     bit;

        if (tdev.getWordSpec() == null) {
            ret[0] = Net.get(tdev.getIdBit());
            for (bit=1 ; bit<size ; bit++)
                ret[bit] = Net.LO;
            return(ret);
        }

        int width = tdev.numBits();
        for (bit=0 ; bit<width ; bit++)
            ret[bit] = Net.get(tdev.getIdBit(bit));
        while (bit < size)
            if (tdev.isSigned())
                ret[bit++] = Net.get(tdev.getIdBit(tdev.numBits()-1));
        return(ret);
    }
    
    /**
     * Get a net from one bit of a signal or signal array in the
     * list. If the bit specified is off the end of the array and
     * the TDEVar represents a signed value then the sign bit is returned,
     * otherwise a GND net is returned
     * @param   i is the index into the list
     * @param   bit is the bit number
     * @return  the net
     */
    public Net getNet (int i, int bit) {
        if (i >= size())
            return(null);
        TDEVar  tdev = get(i);
        if (tdev == null)
            return(null);
        if (bit < tdev.numBits())
            return(Net.get(tdev.getIdBit(bit)));
        else {
            if (tdev.isSigned())
                return(Net.get(tdev.getIdBit(tdev.numBits()-1)));
            else
                return(Net.LO);
        }
    }
    
    /**
     * Get the width of a signal in the list.
     * @param   i is the index into the list
     * @return  the width
     */
    public int getWidth (int i) {
        if (i >= size())
            return(0);
        TDEVar  tdev = get(i);
        if (tdev == null)
            return(0);
        return(tdev.numBits());
    }
    
    /**
     * Get the TDEVar type of a signal in the list.
     * @param   i is the index into the list
     * @return  the TDEVar type
     */
    public TDEVtype getType (int i) {
        TDEVar  tdev = get(i);
        return(tdev.getType());
    }
    
    /**
     * Get a constant TDEVar.
     * @param   i is the index into the list
     * @return  the constant TDEVar value
     */
    public long getVal (int i) {
        TDEVar  tdev = get(i);
        Object  o = tdev.getConst();
        switch (tdev.getType()) {
            case BITS:
            case UINT:
            case INT:
            case UFIXED:
            case FIXED:
            case FLOAT:
                return(((Long)o).longValue());
            case BOOL:
                if (((Boolean)o).booleanValue())
                    return(1);
                else
                    return(0);
        case NEG_CLK:
            break;
        case POS_CLK:
            break;
        case VAR:
            break;
        }
        return(0);
    }
    
    /**
     * Get a clock variable.
     * @param   i is the index into the list
     * @return  the clock variable
     */
    public Var getClkVar (int i) {
        TDEVar  tdev = get(i);
        return(tdev.getClkVar());
    }

    /**
     * Check if a list entry is a single signed value.
     * @param i is the index into the list
     * @return true if the list entry is a single signed value
     */
    public boolean isSigned (int i) {
        TDEVar  tdev = get(i);
        return(tdev.isSigned());
    }
}
