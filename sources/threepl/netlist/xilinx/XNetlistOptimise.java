package threepl.netlist.xilinx;

import static threepl.ThreePL.family;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import threepl.exceptions.ExEx;
import threepl.netlist.*;
import threepl.netlist.Net.NET;
import threepl.netlist.NetConstants.Ctype;
import threepl.netlist.NetConstants.Gtype;

/**
 * This class contains static methods to optimise generic gates and family-specific
 * components in the netlist.
 * Many of these optimisations check for unconnected outputs and eliminate
 * the element where found. These are probably unnecessary as the
 * place-and-route will eliminate them anyway. I guess it makes for
 * a shorter and neater netlist file.
 */
public class XNetlistOptimise {
    
    /**
     * Iteratively optimise gates and flip-flops.
     * @return  the number of optimisation passes
     */
    public static int optimiseNetlist () {
        // If an OBUF or OBUFT has a flip-flop whose output is used elsewhere internally,
        // duplicate the flip-flop, otherwise the single flip-flop will not be placed in an IOB.
        if (family.hasOBUFFD()) {
            optimiseOBUFFD();
            optimiseOBUFTFD();
        }
        
        // Do 2 passes through elements optimising gates.
        int     passes = optimisegates();       
        passes += optimisegates();  // 2nd pass to get rid of resulting unconnected FD or INV
        TDECode.optimiseGNDandVCC(); // generic - in ../TDECode
        return(passes);
    }
    
    /** 
     * Loop through optimisations of the element list until no further changes occur.
     * Some optimisations are generic and hence are performed in TDECode.
     * Family-specific optimisations are performed by private methods in this class.
     * 
     * @return the number of passes performed
     */
    private static int optimisegates () {
        boolean was_changed;
        Ctype   effect;
        int     oc = Element.getOptimisableElementCount();
        int     passes = 0;
        do {
            // diagnostic output for debugging - commented out until needed
            //msg("");
            was_changed = false;
            Iterator<Element>   it = Element.getInstances().iterator();
            int                 i = 0;
            int                 rmcount = 0;
            while (it.hasNext() && (i++ < oc)) {
                Element e = it.next();
                switch (e.getType()) {
                case INV:
                    effect = TDECode.optimiseINV(e);    // generic
                    break;
                case AND:
                    effect = TDECode.optimiseAND(e);    // generic
                    break;
                case OR:
                    effect = TDECode.optimiseOR(e);     // generic
                    break;
                case XOR:
                    effect = TDECode.optimiseXOR(e);    // generic
                    break;
                case LUT:
                    effect = TDECode.optimiseLUT(e);    // generic
                    break;
                case MUXCY:
                    effect = optimiseMUXCY(e);
                    break;
                case MUXF5:
                case MUXF6:
                case MUXF7:          
                case MUXF8:          
                    effect = optimiseMUXF(e);
                    break;
                case XORCY:
                    effect = optimiseXORCY(e);
                    break;
                case SRL:
                    effect = optimiseSRL(e);
                    break;
                case MULT_AND:
                    effect = optimiseMULT_AND(e);
                    break;
                /*
                 * These optimisations were not worth doing, particularly
                 * as the RAM data width was not reduced.
                case CLB_RAM:
                    effect = optimiseCLB_RAM(e);
                    break;
                case BLK_RAM:
                    effect = optimiseBLK_RAM(e);
                    break;
                */
                case FDRSE:
                case FDSE:
                case FDRE:
                    effect = TDECode.optimiseFDRS(e);   // generic
                    break;
                case REMOVED:
                    it.remove();
                    effect = Ctype.UNCHANGED;
                    break;
                default:
                    effect = Ctype.UNCHANGED;
                }
                switch (effect) {
                case CHANGED:
                    was_changed = true;
                    // diagnostic output for debugging - commented out until needed
                    //msg("\telement " + e.getIdent() +
                    //        " type " +  e.getCellName() + " changed");
                    break;
                case STRIPPED:
                    was_changed = true;
                    it.remove();
                    rmcount++;
                    // diagnostic output for debugging - commented out until needed
                    //msg("\telement " + e.getIdent() +
                    //        " type " +  e.getCellName() + " removed");
                    break;
                case IGNORE:
                    break;
                case UNCHANGED:
                    break;
               }
            }
            oc -= rmcount;
            passes++;
            //checkall();   // use if suspect optimisation has damaged netlist
        } while (was_changed);
        return(passes);
    }

    /**
     * Find any selector elements and convert them to basic gates, removing
     * the converted selector element. Duplicate data inputs will be combined.
     */   
    public static void convertSelectors () {
        ArrayList<Element> selectorinstances = new ArrayList<Element>();

        // Make a list of selector elements, removing them from the element list.
        Iterator<Element>    it = Element.getInstances().iterator();
        while (it.hasNext()) {
            Element e = it.next();            
            if (e.getType() == Gtype.SELECTOR) {
                selectorinstances.add(e);
                it.remove();
            }
        }            
        
        // Traverse the selector list constructing logic to implement the selectors.
        for (Element e : selectorinstances) {           
            Net out = e.getOutNet();
            TreeMap<String,Net> ins = e.getInPorts();
            int inputs = ins.size() / 2;
            Net[]   sel = new Net[inputs];
            Net[]   data = new Net[inputs];
            // Traverse the input port TreeMap in order which will return the data inputs ("Dn") in order
            // followed by the select inputs ("Sn") in order.
            Collection<Net>  c = e.getInPorts().values();
            int i = 0;
            int j = 0;
            int k = 0;
            for (Net n : c) {
                if (i++ < inputs)
                    data[j++] = n;
                else
                    sel[k++] = n;
            }
            
            // Have collected all output and input nets for the selector element.
            // Strip these off that element prior to connecting them to replacement logic
            // to avoid net drive conflicts.
            e.strip();
            
            // Construct a map of data NETs, the entry values being sets of input indices.
            // This will combine duplicated data inputs.
            HashMap<NET,ArrayList<Integer>>    m = new HashMap<NET,ArrayList<Integer>>();
            for (i=0 ; i<inputs ; i++) {
                NET N = data[i].getNET();
                ArrayList<Integer>    al;
                if (m.containsKey(N))
                    al = m.get(N);
                else {
                    al = new ArrayList<Integer>();
                    m.put(N, al);
                }
                al.add(i);
            }
            
            // Create new input data and select Net arrays using entries from the map.
            int     oinputs = m.size(); // number of selector inputs after optimisation
            Net[]   osel = new Net[oinputs];
            Net[]   odata = new Net[oinputs];
            
            i = 0;
            for (Map.Entry<NET,ArrayList<Integer>> me : m.entrySet()) {
                Net                 n = me.getKey().getNet();
                ArrayList<Integer>  al = me.getValue();
                odata[i] = n;
                if (al.size() == 1)
                    osel[i++] = sel[al.get(0)];
                else {
                    Element or = new Element("OR"+al.size(), Gtype.OR);
                    Net     nn = new Net();
                    j = 0;
                    or.addOutput("O", nn);
                    for (Integer ii : al)
                        or.addInput("I"+j++, sel[ii]);
                    osel[i++] = nn;
                }
            }
            
            // Construct a selector using gate logic from the input NET map.
            // Ensure for single inputs no 1-input gates are created as the general
            // netlist optimisation that would eliminate these has already been done!
            if (e.getProperty("DEFAULT").equals("0")) {
                if (oinputs == 1) {
                    // single input
                    Element and = new Element("AND2", Gtype.AND);
                    and.addInput("I0", osel[0]);
                    and.addInput("I1", odata[0]);
                    and.addOutput("O", out);
                } else {
                    Element or = new Element("OR"+oinputs, Gtype.OR);
                    for (i=0 ; i<oinputs ; i++) {
                        Element and = new Element("AND2", Gtype.AND);
                        Net     nn = new Net();
                        and.addInput("I0", osel[i]);
                        and.addInput("I1", odata[i]);
                        and.addOutput("O", nn);
                        or.addInput("I"+i, nn);
                    }
                    or.addOutput("O", out);
                }
            } else {
                if (oinputs == 1) {
                    // single input
                    Element or = new Element("OR2", Gtype.OR);
                    Element inv = new Element("INV", Gtype.INV);
                    Net     inn = new Net();
                    inv.addOutput("O", inn);
                    inv.addInput("I",  osel[0]);
                    or.addInput("I0", inn);
                    or.addInput("I1", odata[0]);
                    or.addOutput("O", out);                
                } else {
                    Element and = new Element("AND"+oinputs, Gtype.AND);
                    for (i=0 ; i<oinputs ; i++) {
                        Element or = new Element("OR2", Gtype.OR);
                        Element inv = new Element("INV", Gtype.INV);
                        Net     nn = new Net();
                        Net     inn = new Net();
                        inv.addOutput("O", inn);
                        inv.addInput("I",  osel[i]);
                        or.addInput("I0", inn);
                        or.addInput("I1", odata[i]);
                        or.addOutput("O", nn);
                        and.addInput("I"+i, nn);
                    }
                    and.addOutput("O", out);
                }
            }
        }
    }
            
    /**
     * Convert generic elements to family-specific elements where
     * necessary.
     * Up until this point generic gates, AND, OR, XOR and XNOR, have an
     * arbitrary number of inputs. These must be mapped into
     * elements of maximum width available in the FPGA family.
     */
    public static void convertGenericToSpecific () {
        ArrayList<Element> gateinstances = new ArrayList<Element>();
        
        // Examine all generic gates.
        // If wide, remove to a new list for later expansion (below).
        Iterator<Element>    it = Element.getInstances().iterator();
        while (it.hasNext()) {
            Element e = it.next();
            switch (e.getType()) {
            case AND:
            case OR:
            case XOR:
            case XNOR:
                if (e.getInPorts().size() > family.LUTWidth()) {
                    gateinstances.add(e);
                    it.remove();
                }
                break;
            default:
                break;
            }
        }
        
        // Create wide gates
        for (Element e : gateinstances) {
            Gtype           etype = e.getType();
            int             n = e.getInPorts().size();
            Net[]           ins = new Net[n];
            Net             lut;
            Net             out = null;
            Net             o = null;
            int             k = 0;
            ArrayList<Net>   al = null;
            for (Map.Entry<String,Net> me : e.getInPorts().entrySet())
                ins[k++] = me.getValue();
            for (Map.Entry<String,Net> me : e.getOutPorts().entrySet())
                out = me.getValue();
            e.strip();
            if (((XTDECode)family).needCascadeGates()) {
                int     l2 = (n - 1) / family.LUTWidth() + 1;
                int     sl = 0;
                int     su = family.LUTWidth();
                al = new ArrayList<Net>();
                for (int i=0 ; i<l2 ; i++) {
                    if (su > n)
                        su = n;
                    switch (etype) {
                    case AND:
                        al.add(((XTDECode)family).AND(subarraytolist(ins, sl, su)));
                        break;
                    case OR:
                        al.add(((XTDECode)family).OR(subarraytolist(ins, sl, su)));
                        break;
                    default:
                        break;
                    }
                    sl += family.LUTWidth();
                    su += family.LUTWidth();
                }
                switch (etype) {
                case AND:
                    out.connect(((XTDECode)family).AND(al));
                    break;
                case OR:
                    out.connect(((XTDECode)family).OR(al));
		    break;
                default:
                    break;
                }
            } else if (e.getType() == Gtype.XOR) {
                // For XOR create a tree.
                out.connect(XORTree(ins, false));
            } else if (e.getType() == Gtype.XNOR) {
                // For XNOR create a tree.
                out.connect(XORTree(ins, true));
            } else if (n == (TDECode.lutwidth + 1)) {
                // For (lutwidth + 1 inputs use two LUTs and a MUXF5 (for AND
                // and OR - one of the LUTs will supply a HI or LO).
                al = subarraytolist(ins, 0, family.LUTWidth());
                switch (e.getType()) {
                case AND:
                    lut = ((XTDECode)family).AND(al);
                    o = ((XTDECode)family).MUXF5(ins[family.LUTWidth()], Net.LO, lut);
                    out.connect(o);
                    break;
                case OR:
                    lut = ((XTDECode)family).OR(al);
                    o = ((XTDECode)family).MUXF5(ins[family.LUTWidth()], lut, Net.HI);
                    out.connect(o);
                    break;
                default:
                    break;
                }
            } else {
                // For >(lutwidth+1) inputs construct a carry chain of ANDs
                // or ORs with a LUT with up to lutwidth inputs at each link.
                int rem = n % TDECode.lutwidth;
                int incr = (n - 1) % TDECode.lutwidth + 1;
                int j = 0;
                Net ci;
                Net s;
                if (rem == 1) {
                    // Use the carry-in as an extra input at no cost.
                    // This input has no inbuilt inverter so try to
                    // re-arrange input order to ensure the 1st input is
                    // not from an inverter!
                    int l;
                    for (l=0 ; l<n ; l++) {
                        if (ins[l].getOutputElement().getType() != Gtype.INV)
                            break;
                    }
                    if ((l != 0) && (l < n)) {
                        Net[]   new_ins = new Net[n];
                        for (int m=0 ; m<n ; m++) {
                            new_ins[m] = ins[l++];
                            if (l == n)
                                l = 0;
                        }
                        ins = new_ins;
                    }
                    j = 1;
                    incr = TDECode.lutwidth;
                }
                switch (e.getType()) {
                case AND:
                    ci = (rem == 1) ? ins[0] : Net.HI;
                    for (int i=j ; i<n ;) {
                        s = ((XTDECode)family).AND(subarraytolist(ins, i, i+incr));
                        o = ((XTDECode)family).MUXCY(s, Net.LO, ci);
                        ci = o;
                        i += incr;
                        incr = family.LUTWidth(); 
                    }
                    out.connect(o);
                    break;
                case OR:
                    ci = (rem == 1) ? ins[0] : Net.LO;
                    for (int i=j ; i<n ;) {
                        s = ((XTDECode)family).INV(((XTDECode)family).OR(subarraytolist(ins, i, i+incr)));
                        o = ((XTDECode)family).MUXCY(s, Net.HI, ci);
                        ci = o;
                        i += incr;
                        incr = family.LUTWidth(); 
                    }
                    out.connect(o);
                    break;
                default:
                    break;
                }
            }
        }

        if (family.LUTWidth() == 6) {
            // Special case for Virtex5. (WHAT ABOUT XC6V, XC6S, XC7?)
            // Does not allow AND6 or OR6, so convert to LUT6 with
            // appropriate equation.
            it = Element.getInstances().iterator();
            while (it.hasNext()) {
                Element e = it.next();
                if (e.getInPorts().size() == family.LUTWidth()) {
                    switch (e.getType()) {
                    case AND:
                        e.addExpression("I0&I1&I2&I3&I4&I5");
                        e.changeElement("LUT6", Gtype.LUT, false);
                        break;
                    case OR:
                        e.addExpression("I0|I1|I2|I3|I4|I5");
                        e.changeElement("LUT6", Gtype.LUT, false);
                        break;
                    default:
                        break;
                    }
                }
            }
        }
    }
    
    private static Net XORTree (Net[] in, boolean is_xnor) {
        int         n = in.length;
        ArrayList<Net>   al = null;

        if (n == 1) {
            if (is_xnor)
                return(((XTDECode)family).INV(in[0])); // actually should never occur!
            
            return(in[0]);
        }

        if (n <= family.LUTWidth()) {
            al = subarraytolist(in, 0, n);
            if (is_xnor)
                return(((XTDECode)family).XNOR(al));
            
            return(((XTDECode)family).XOR(al));
        }

        if (in.length == (family.LUTWidth()+1)) {
            Net lut1, lut2;
            al = subarraytolist(in, 0, family.LUTWidth());
            if (is_xnor) {
                lut1 = ((XTDECode)family).XNOR(al);
                lut2 = ((XTDECode)family).XOR(al);
            } else {
                lut1 = ((XTDECode)family).XOR(al);
                lut2 = ((XTDECode)family).XNOR(al);
            }
            return(((XTDECode)family).MUXF5(in[family.LUTWidth()], lut1, lut2));
        }
        
        Net[]   xx = new Net[(n-1) / family.LUTWidth() + 1];
        int     j;
        int     k = 0;
        for (int i=0 ; i<n ; i+=family.LUTWidth()) {
            j = ((n - i) < family.LUTWidth()) ? n : i + family.LUTWidth();
            xx[k++] = XORTree(GenFunctions.subarray(in, i, j-1), false);
        }
        return(XORTree(xx, is_xnor));
    }
    
    private static ArrayList<Net> subarraytolist (Net[] a, int i, int j) {
        ArrayList<Net>  al = new ArrayList<Net>();
        while (i < j)
            al.add(a[i++]);
        return(al);
    }
    
    /**
     * Check a MUXCY for output not connected, in which case
     * remove it.
     * Check for constant s input, in which case delete the
     * MUXCY and connect the output net to the appropriate
     * input net.
     * Check for inputs CI and DI both constant, in which case
     * output is HI, LO, S or !S.
     * Check for input CI LO and DI connected to S, in which case
     * output is LO.
     * @param   element is the MUXCY element
     * @return  code for unchanged, changed or stripped
     */
    private static Ctype optimiseMUXCY (Element element) {
        Net s = element.getInPorts().get("S");
        Net ci = element.getInPorts().get("CI");
        Net di = element.getInPorts().get("DI");
        Net out = element.getOutPorts().get("O");
       
        if ((out == null) || (out.getNumInputs() == 0) && !out.isExtOutPort()) {
            element.strip();
            return(Ctype.STRIPPED);
        }
        if (s.isConnected(Net.HI)) {
            element.strip();
            out.connect(ci);
            return(Ctype.STRIPPED);
        }
        if (s.isConnected(Net.LO)) {
            element.strip();
            out.connect(di);
            return(Ctype.STRIPPED);
        }
        if ((ci.isConnected(Net.HI)) && (di.isConnected(Net.HI))) {
            element.strip();
            out.connect(Net.HI);
            return(Ctype.STRIPPED);
        }
        if ((ci.isConnected(Net.LO)) && (di.isConnected(Net.LO))) {
            element.strip();
            out.connect(Net.LO);
            return(Ctype.STRIPPED);
        }
        if ((ci.isConnected(Net.HI)) && (di.isConnected(Net.LO))) {
            element.strip();
            out.connect(s);
            return(Ctype.STRIPPED);
        }
        if ((ci.isConnected(Net.LO)) && (di.isConnected(Net.HI))) {
            element.changeElement("INV", Gtype.INV, true);
            element.addInput("I", s);
            element.addOutput("O", out);
            return(Ctype.CHANGED);
        }
        if ((ci.isConnected(Net.LO)) && (di.isConnected(s))) {
            element.strip();
            out.connect(Net.LO);
            return(Ctype.STRIPPED);
        }
        return(Ctype.UNCHANGED);
    }
    
    /**
     * Check a MUXF5, MUXF6 etc for constant s input.
     * If so delete the MUXF and connect the output net
     * to the appropriate input net.
     * Check for inputs I0 and I1 both constant, in which case
     * output is HI, LO, S or !S.
     * @param   element is the MUXF element
     * @return  code for unchanged, changed or stripped
     */
    private static Ctype optimiseMUXF (Element element) {
        Net s = element.getInPorts().get("S");
        Net i0 = element.getInPorts().get("I0");
        Net i1 = element.getInPorts().get("I1");
        Net out = element.getOutPorts().get("O");
       
        if ((out == null) || (out.getNumInputs() == 0) && !out.isExtOutPort()) {
            element.strip();
            return(Ctype.STRIPPED);
        }
        if (s.isConnected(Net.HI)) {
            element.strip();
            out.connect(i1);
            return(Ctype.STRIPPED);
        }
        if (s.isConnected(Net.LO)) {
            element.strip();
            out.connect(i0);
            return(Ctype.STRIPPED);
        }
        if ((i0.isConnected(Net.HI)) && (i1.isConnected(Net.HI))) {
            element.strip();
            out.connect(Net.HI);
            return(Ctype.STRIPPED);
        }
        if ((i0.isConnected(Net.LO)) && (i1.isConnected(Net.LO))) {
            element.strip();
            out.connect(Net.LO);
            return(Ctype.STRIPPED);
        }
        if ((i0.isConnected(Net.HI)) && (i1.isConnected(Net.LO))) {
            element.strip();
            out.connect(s);
            return(Ctype.STRIPPED);
        }
        if ((i0.isConnected(Net.LO)) && (i1.isConnected(Net.HI))) {
            element.changeElement("INV", Gtype.INV, true);
            element.addInput("I", s);
            element.addOutput("O", out);
            return(Ctype.CHANGED);
        }
        return(Ctype.UNCHANGED);
    }
    
    /**
     * Check an SRL for an unconnected output and remove if so.
     * @param   element is the SRL element
     * @return  code for unchanged, changed or stripped
     */
    private static Ctype optimiseSRL (Element element) {
        Net     out_q = element.getOutPorts().get("Q");
        Net     out_q31 = element.getOutPorts().get("Q31");
        boolean q_unused = (out_q == null) ||
                           (out_q.getNumInputs() == 0) && !out_q.isExtOutPort();
        boolean q31_unused = (out_q31 == null) ||
                             (out_q31.getNumInputs() == 0) && !out_q31.isExtOutPort();

        if (q_unused && q31_unused) {
            element.strip();
            return(Ctype.STRIPPED);
        }
        return(Ctype.UNCHANGED);
    }
    
    /**
     * Check a MULT_AND for an unconnected output and remove if so.
     * If any inputs are GND, remove and connect output to GND.
     * If any inputs are VCC, remove the input.
     * @param   element is a MULTAND element
     * @return  code for unchanged, changed or stripped
     */
    private static Ctype optimiseMULT_AND (Element element) {
        Ctype   change = Ctype.UNCHANGED;
        Net     out = element.getOutPorts().get("LO");

        if ((out == null) || (out.getNumInputs() == 0) && !out.isExtOutPort()) {
            element.strip();
            return(Ctype.STRIPPED);
        }

        // Check for gate inputs which are GND, VCC or duplicated.
        change = TDECode.inputEliminate (element, Net.HI, Net.LO, out);

        if (change == Ctype.CHANGED)
            change = TDECode.remapInputs(change, element, true);
        return(change);
    }
   
    /**
     * Check a dual-port CLB RAM for an unconnected output and remove if so.
     * If data input is GND and RAM is not initialised, remove and connect
     * used outputs to GND.
     * PROBABLY NOT WORTH DOING.
     * @param   element is a CLB_RAM element
     * @return  code for unchanged, changed or stripped
     *
    private static Ctype optimiseCLB_RAM (Element element) {
        Net     out1 = element.getOutPorts().get("SPO");
        Net     out2 = element.getOutPorts().get("DPO");

        if (((out1 == null) || (out1.getNumInputs() == 0) && !out1.isExtOutPort()) &&
            ((out2 == null) || (out2.getNumInputs() == 0) && !out2.isExtOutPort())) {
            element.strip();
            return(Ctype.STRIPPED);
        }
        Net     din = element.getInPorts().get("D");
        if ((din != null)) {
            if (din.isConnected(Net.LO)) {
                element.strip();
                if (out1 != null)
                    out1.connect(Net.LO);
                if (out2 != null)
                    out2.connect(Net.LO);
                return(Ctype.STRIPPED);
            }
        }
        return(Ctype.UNCHANGED);
    }
    */
   
    /**
     * If a data input is GND or unconnected on both ports and RAM is not
     * initialised, disconnect both associated outputs if connected.
     * 
     * Not sure this is worth doing or indeed if these cases ever occur!
     * 
     * @param   element is a BLK_RAM element
     * @return  code for unchanged, changed or stripped
     *
    private static Ctype optimiseBLK_RAM (Element element) {
        Ctype   change = Ctype.UNCHANGED;
        // Note: we cannot tell what format the block RAM is or which
        // data input or output pins are connected, so just search for
        // all possible data pins DIAn, DIBn, DIPAm and DIPBm
        // for n = 0 to 32 and m = 0 to 3.
        // RAMs with fewer inputs will return null and will have
        // no associated outputs so the code will do nothing.
        //
        // NOTE: THIS CAN ONLY WORK IF THE NETLIST CELL FORMAT HAS DISCRETE
        // PINS FOR DATA INPUT AND OUTPUT PORTS, NOT EDIF PIN ARRAYS.
        change = optimiseBLK_RAM_(element, 32, "", change);
        change = optimiseBLK_RAM_(element, 4, "P", change);
        return(change);
    }
    
    private static Ctype optimiseBLK_RAM_ (Element element, int n, String p, Ctype change) {
        for (int i=0 ; i<n ; i++) {
            String  dia = "DI" + p + "A" + i;
            String  dib = "DI" + p + "B" + i;
            String  doa = "DO" + p + "A" + i;
            String  dob = "DO" + p + "B" + i;
            // These nets will be null if not connected or the pin does
            // not exist for this RAM format.
            Net ina = element.getInPorts().get(dia);
            Net inb = element.getInPorts().get(dib);
            Net outa = element.getOutPorts().get(doa);
            Net outb = element.getOutPorts().get(dob);
            //
            // Seemed a good idea but XILINX bitgen DRC flags these as
            // errors!
            //if (((outa == null) || (outa.getNumInputs() == 0) && !outa.isExtOutPort()) &&
            //    ((outb == null) || (outb.getNumInputs() == 0) && !outb.isExtOutPort())) {
            //    if (ina != null) {
            //        element.removeNet(dia);
            //        change = Ctype.CHANGED;
            //    }
            //    if (inb != null) {
            //        element.removeNet(dib);
            //        change = Ctype.CHANGED;
            //    }
            //    continue;
            //}
            
            // Note: for pins which do not exist for this RAM format the
            // output nets will be null so nothing happens.
            if (((ina == null) || ina.isConnected(Net.LO)) && 
                ((inb == null) || inb.isConnected(Net.LO))) {
                if (outa != null) {
                    element.removeNet(doa);
                    outa.connect(Net.LO);
                    change = Ctype.CHANGED;
                }
                if (outb != null) {
                    element.removeNet(dob);
                    outb.connect(Net.LO);
                    change = Ctype.CHANGED;
                }
            }
        }
        return(change);
    }
    */
    
    /*
    // Check for multiplier with no output connections and eliminate if so. NO LONGER CALLED!
    // Not worth doing? Place-and-route will eliminate anyway.
    private static boolean optimiseMULT_o (Element element) {
        Iterator<Map.Entry<String,Net>> it = element.getOutputsIterator();
        while (it.hasNext()) {
            Net n = it.next().getValue();
            if ((n != null) && (n.getNumInputs() != 0) || n.isExtOutPort())
                return(false);
        }
        element.strip();
        return(true);
    }
    
    // Check for multiplier with inputs GND and eliminate if so. NO LONGER CALLED!
    // Not worth doing? Place-and-route will probably eliminate anyway.
    private static boolean optimiseMULT_i (Element element, int awidth) {
        boolean a0 = true;
        boolean b0 = true;
        for (int i=0 ; i<awidth ; i++) {
            Net ina = element.getInPorts().get("A" + i);
            if (!ina.isConnected(Net.LO)) {
                a0 = false;
                break;
            }
        }
        for (int i=0 ; i<18 ; i++) {
            Net inb = element.getInPorts().get("B" + i);
            if (!inb.isConnected(Net.LO)) {
                b0 = false;
                break;
            }
        }
        if (a0 || b0) {
            int ow = awidth + 18;
            for (int i=0 ; i<ow ; i++) {
                String  pin = "P" + i;
                Net     out = element.getOutPorts().get(pin);
                if (out != null) {
                    element.removeNet(pin);
                    out.connect(Net.LO);
                }
            }            
            element.strip();
            return(true);
        }
        return(false);
    }
    */
       
    /**
     * Check an XORCY for an unconnected output and remove if so.
     * If the CI input is GND, eliminate the XORCY and connect the
     * output net to the LI input.
     * If the CI input is VCC, strip the XORCY, change it to an INV and
     * connect the LI input as input I and reconnect the output.
     * @param   element is the XORCY element
     * @return  code for unchanged, changed or stripped
     */
    private static Ctype optimiseXORCY (Element element) {
        Net out = element.getOutPorts().get("O");
        if ((out == null) || (out.getNumInputs() == 0) && !out.isExtOutPort()) {
            element.strip();
            return(Ctype.STRIPPED);
        }
        Net in_ci = element.getInPorts().get("CI");
        Net in_li = element.getInPorts().get("LI");
        if (in_ci.isConnected(Net.LO)) {
            element.strip();
            out.connect(in_li);
            return(Ctype.CHANGED);
        }
        if (in_ci.isConnected(Net.HI)) {
            element.changeElement("INV", Gtype.INV, true);
            element.addInput("I", in_li);
            element.addOutput("O", out);
            return(Ctype.CHANGED);
        }
        return(Ctype.UNCHANGED);
    }

    /**
     * Check for an FD whose output goes to an OBUF and also goes elsewhere.
     * Replicate the FD for each OBUF plus one extra for all other sinks.
     */
    private static void optimiseOBUFFD () {
        ArrayList<Element>  ol = new ArrayList<Element>();
        Iterator<Element>   it = Element.getInstances().iterator();
        while (it.hasNext()) {
            Element e = it.next();
            String  p = e.getProperty("IOB");
            if ((p == null) || !p.equals("TRUE"))
                continue;
            if (e.getCellName().equals("FDRSE") || e.getCellName().equals("FDRE") || e.getCellName().equals("FDSE"))
                ol.add(e);
        }
                
        for (Element e : ol) {
            Net t = e.getOutPorts().get("Q");
            t.getOutputElementInit();
            if (t.getNumOutputs() == 1)
                continue;   // only goes to a single OBUF - don't bother duplicating
            
            ArrayList<Object> a = null;
            while ((a = t.getNextOutputElement()) != null) {
                Element oe = (Element)(a.get(0));
                if (oe.getCellName().equals("OBUF")) {
                    Net newfd = fdDuplicate(e);
                    oe.removeNet("I");
                    oe.addInput("I", newfd);
                }
            }
            if (t.getNumOutputs() == 0)
                e.strip();
        }
    }

    /**
     * Check for an FD whose output goes to an OBUFT I input and also goes elsewhere.
     * Replicate the FD for each OBUFT plus one extra for all other sinks.
     * Check for an FD whose output goes to an OBUFT T input and also goes elsewhere.
     * Replicate the FD for each OBUFT plus one extra for all other sinks.
     * 
     * NOTE: ULTRASCALE I/O BLOCK DO NOT HAVE A T FLIPFLOP! IN THAT CASE SHOULD DUPICATE
     * FD FOR EACH OBUFT BUT EACH WILL RESIDE IN A CLB. REMOVE IOB PROPERTY.
     */
    private static void optimiseOBUFTFD () {
        ArrayList<Element>  ol = new ArrayList<Element>();
        Iterator<Element>   it = Element.getInstances().iterator();
        while (it.hasNext()) {
            Element e = it.next();
            String  p = e.getProperty("IOB");
            if ((p == null) || !p.equals("TRUE"))
                continue;
            if (e.getCellName().equals("FDRSE") || e.getCellName().equals("FDRE") || e.getCellName().equals("FDSE"))
                ol.add(e);
        }
        
        for (Element e : ol) {
            Net t = e.getOutPorts().get("Q");
            if (t.getNumInputs() == 1)
                continue;   // only goes to a single OBUF - don't bother duplicating
            
            t.getInputElementInit();
            ArrayList<Object> a1 = null;
            while ((a1 = t.getNextInputElement()) != null) {
                Element oe1 = (Element)(a1.get(0));
                String  port1 = (String)a1.get(1);
                if (oe1.getCellName().equals("OBUFT") && (port1 == "I")) {
                    // I input
                    Net newfd = fdDuplicate(e);
                    oe1.removeNet("I");
                    oe1.addInput("I", newfd);
                } else if (family.hasOBUFFTFF()) {
                    if (oe1.getCellName().equals("INV")) {
                        // check all destinations of inverter output to see if any
                        // are OBUFT T inputs
                        Net invout = oe1.getOutPorts().get("O");
                        invout.getInputElementInit();
                        ArrayList<Object> a2 = null;
                        while ((a2 = invout.getNextInputElement()) != null) {
                            Element oe2 = (Element)(a2.get(0));
                            String  port2 = (String)a2.get(1);
                            if (oe2.getCellName().equals("OBUFT") && (port2 == "T")) {
                                // inverted FD goes to an OBUFT "T" port
                                // create an inverted shadow flip-flop, remove inverter
                                // and connect the inverted flip-flop output direct to the OBUFT T input
                                Net newfd = fdDuplicateInv(e);
                                oe2.removeNet("T");
                                oe2.addInput("T", newfd);
                            }
                        }
                    } else if (oe1.getCellName().equals("OBUFT") && (port1 == "T")) {
                        // direct T input
                        Net newfd = fdDuplicate(e);
                        oe1.removeNet("T");
                        oe1.addInput("T", newfd);
                    }
                }
            }
            if (t.getNumOutputs() == 0)
                e.strip();
        }
    }
    
    /**
     * Duplicate an FDRSE, FDRE or FDSE and connect the inputs of the
     * new one to those of the old one.
     * @param e is the flip-flop element to be duplicated
     * @return the output signal of the new flip-flop
     */
    private static Net fdDuplicate (Element e) {
        Net q = new Net();
        Element enew = new Element(e.getCellName());
        enew.addInput("D", e.getInPorts().get("D"));
        enew.addInput("C", e.getInPorts().get("C"));
        enew.addInput("CE", e.getInPorts().get("CE"));
        if ((e.getType() == Gtype.FDRSE) || (e.getType() == Gtype.FDRE))
            enew.addInput("R", e.getInPorts().get("R"));
        if ((e.getType() == Gtype.FDRSE) || (e.getType() == Gtype.FDSE))
            enew.addInput("S", e.getInPorts().get("S"));
        enew.addProperty("INIT", e.getProperty("INIT"));
        enew.addOutput("Q", q);
        // Move the IOB property from the old FD to the new FD.
        enew.addProperty("IOB", "true");
        e.addProperty("IOB", null);
        return(q);
    }
    
    /**
     * Duplicate a flip-flop inverting its output.
     * The D input of the old flip-flop will be inverted for the new flip-flop.
     * An FDRSE is duplicated as an FDRSE with the R and S inputs swapped.
     * An FDRE is duplicated as an FDSE with the old R input connected to the new S input
     * and similarly an FDSE becomes an FDRE with S input connect to the new R input.
     * The initial state is inverted via the INIT property.
     * @param e is the flip-flop element to be duplicated
     * @return the output signal of the new flip-flop
     */
    private static Net fdDuplicateInv (Element e) {
        Net q = new Net();
        Element enew = null;
        Net inv_d = new Net();
        Element inv = new Element("INV", Gtype.INV);
        switch (e.getType()) {
        case FDRSE:
            enew = new Element("FDRSE", Gtype.FDRSE);
            break;
        case FDRE:
            enew = new Element("FDSE", Gtype.FDSE);
            break;
        case FDSE:
            enew = new Element("FDRE", Gtype.FDRE);
            break;
        default:
        }
        inv.addInput("I", e.getInPorts().get("D"));
        inv.addOutput("O", inv_d);
        enew.addInput("D", inv_d);
        enew.addInput("C", e.getInPorts().get("C"));
        enew.addInput("CE", e.getInPorts().get("CE"));
        switch (enew.getType()) {
        case FDRSE:
            enew.addInput("R", e.getInPorts().get("S"));
            enew.addInput("S", e.getInPorts().get("R"));
            break;
        case FDRE:
            enew.addInput("R", e.getInPorts().get("S"));
            break;
        case FDSE:
            enew.addInput("S", e.getInPorts().get("R"));
            break;
        default:
        }
        enew.addOutput("Q", q);
        String  init = e.getProperty("INIT");
        if (init.equals("R"))
            enew.addProperty("INIT", "S");
        else if (init.equals("S"))
            enew.addProperty("INIT", "R");
        else
            throw new ExEx("ERROR OPTIMISING NETLIST - illegal flip-flop initialisation '" + init + "'");
        // Move the IOB property from the old FD to the new FD.
        enew.addProperty("IOB", "TRUE");
        e.addProperty("IOB", null);
        return(q);
    }
}
