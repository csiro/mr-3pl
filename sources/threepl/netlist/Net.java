package threepl.netlist;

import static threepl.ThreePL.getFamily;
import static threepl.ThreePL.msg;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import threepl.codegen.TDEConstants;
import threepl.exceptions.ExEx;

/**
 * This class implements a named net.
 * The inner class NET represents a net while the outer class
 * represents an identifier. Where several identifiers belong to
 * the same net because they have been connected each of the Net
 * instances will have the same instance of NET.
 *
 * The static map 'identmap' maps identifiers to instances of class Net.
 * The static set 'instances' contains all the instances of class NET.
 *
 * The NET class contains lists of identifiers ('idents') and Nets
 * ('nets') which pertain to it. 'prefident' is one of the identifiers
 * which has been selected as the prefered single identifier. The
 * aligned lists 'elements', 'ports' and 'porttypes' give the elements
 * and their port identifiers and types (in, out or 3-state) to which
 * this NET is connected.
 *
 * NOTE: when 3PL is run repeatedly via the GUI class TDECode and all
 * subclasses down to the class specific to the FPGA family are
 * constructed afresh. Thus all static variables are re-created. Classses
 * Element and Net are NOT reconstructed and hence static variables in
 * these classes must be explicitly re-initialised on each run! In the case
 * of this class this is done by method init().
 */
public class Net implements NetConstants, TDEConstants {
    private NET             net;          // actual net
    private String          id;           // identifier
    
    // The following 4 lists and 2 integers are only used as temporary shared variables
    // by methods getInputElementInit(), getNextInputElement(),
    // getOutputElementInit() and getNextOutputElement().
    private ArrayList<Element>          input_elements;
    private ArrayList<String>           input_ports;
    private ArrayList<Element>          output_elements;
    private ArrayList<String>           output_ports;
    private int                         input_index;  // temporary index for traversing inputs
    private int                         output_index; // temporary index for traversing outputs

    private static int                  icount; // unique integer for un-named nets
    private static HashMap<String, Net> identmap;
    private static LinkedHashSet<NET>   instances;
    public  static Net                  LO;
    public  static Net                  HI;
    
    public class NET {
        ArrayList<String>               idents;
        ArrayList<Net>                  nets;
        String                          prefident;
        ArrayList<Element>              elements;
        ArrayList<String>               ports;
        ArrayList<PortType>             porttypes;
        int                             inports;
        int                             outports;
        int                             tsoutports;
        public  TreeMap<String,String>  properties = new TreeMap<String,String>();
        public  TDEVPtype               extport = TDEVPtype.NONE;
        public  String                  extportname;
        
        
        /**
         * Construct a new NET for a Net.
         * @param   id is the Net idenifier
         * @param   n is the Net
         * @param   large if true indicates that the initial size of the list of NETs should be
         *          set at 10000
         */
        public NET (String id, Net n, boolean large) {
            if (large) {
                idents = new ArrayList<String>(10000);
                nets = new ArrayList<Net>(10000);
                elements = new ArrayList<Element>(10000);
                ports = new ArrayList<String>(10000);
                porttypes = new ArrayList<PortType>(10000);
            } else {
                idents = new ArrayList<String>();
                nets = new ArrayList<Net>();
                elements = new ArrayList<Element>();
                ports = new ArrayList<String>();
                porttypes = new ArrayList<PortType>();
            }
            idents.add(id);
            nets.add(n);
            prefident = id;
        }
        
        /**
         * Scan the list of identifiers and select the most suitable
         * to be used as the preferred identifier.
         */
        public void setPrefIdent () {
            int         gl = 9999;
            int         ucl = 9999;
            int         l = 9999;
            String      gpref = null;
            String      ucpref = null;
            String      pref = null;
            for (String i : idents) {
                int len = i.length();
                if (i.equals("GND")) {
                    prefident = i;
                    return;
                } else if (i.equals("VCC")) {
                    prefident = i;
                    return;
                } else if (i.startsWith("PORT")) {
                    prefident = i;
                    return;
                } else if (i.startsWith("glob")) {
                    if (len < gl) {
                        gpref = i;
                        gl = len;
                    }
                } else if (Character.isUpperCase(i.charAt(0))) {
                    if (len < ucl) {
                        ucpref = i;
                        ucl = len;
                    }
                } else {
                    if (len < l) {
                        pref = i;
                        l = len;
                    }
                }
            }
            if (gpref != null)
                prefident = gpref;
            else if (ucpref != null)
                prefident = ucpref;
            else
                prefident = pref;
        }
    
        /**
         * Get the net identifier translated for EDIF. All non alphanumeric
	 * characters other than "_" are changed to "_", and all "]" are
	 * removed. If the identifier does not start with a alpha then a
	 * {@code "&"} is prepended, apparently as per EDIF standard.
	 * Note that EDIF identifiers are case-insensitive, but we don't
	 * flatten the case here.
	 *
         * @return  the translated EDIF net identifier
         */
        public String getEDIFIdent () {
            String s;
            s = prefident.replaceAll("\\]", "");
            s = s.replaceAll("[^A-Za-z0-9_]", "_");
	    if (! s.matches("^[A-Za-z].+$"))
	    	s = "&" + s;
            return(s);
        }
        
        /**
         * Get a Net representing this NET.
         * The Net returned is arbitrary and is the first in the list of connected
         * Nets. Since all Nets in the list are connected any one will suffice in the netlist
         * as the preferred ID is used when the final netlist is written out.
         * @return  a Net
         */
        public Net getNet () {
            return(nets.get(0));
        }
    
        /**
         * Determine if this net is an external output port.
         * @return  true if this net is an external output port
         */
        public boolean isExtOutPort () { return((extport == TDEVPtype.OUTPUT) || (extport == TDEVPtype.INOUT)); }
    }
    
    /**
     * Construct an unnamed net. A unique identifier string
     * of the form "n123" will be created.
     */
    public Net () {
        id = "n" + icount++;
        identmap.put(id, this);
        net = new NET(id, this, false);
        instances.add(net);
    }
    
    /**
     * Construct net with a unique name. A unique integer is appended
     * to the supplied header string to form the net identifier.
     * @param   header is the leading identifier string for the net
     */
    public Net (String header) {
        id = header + icount++;
        identmap.put(id, this);
        net = new NET(id, this, false);
        instances.add(net);
    }
    
    /**
     * Construct a named net.
     * @param   ident is the identifier string for the net
     * @param   large true indicates the associated NET should have
     *          lists which are initially large as the number of connected
     *          Nets sharing the NET may be large
     */
    public Net (String ident, boolean large) {
        id = ident;
        identmap.put(ident, this);
        net = new NET(ident, this, large);
        instances.add(net);
    }
    
    /**
     * Initialise static variables. This is used, rather than
     * placing initialisers on the static variables themselves,
     * so that static variables can be re-initialised on successive
     * runs of 3PL when it is used via the GUI.
     */
    public static void init () {
        icount = 0; // unique integer for un-named nets
        identmap = new HashMap<String, Net>(20000);
        instances = new LinkedHashSet<NET>(20000);
        // LO and HI are assigned in the constructor XElements() because
        // they are then connected there to newly constructed Elements GND
        // and VCC.
    }
    
    /**
     * Compare this Net with an argument. If they are identical or
     * are connected return true.
     * @param   o is the object to be compared
     * @return  true if the argument is the same Net or a connected Net
     */
    public boolean equals (Object o) {
        if ((o == null) || !(o instanceof Net))
            return(false);
        if (o == this)
            return(true);
        return(net == ((Net)o).net);
    }
    
    public int hashcode (Object o) {
        return(hashcode(id) + icount);
    }
    
    /**
     * Return the NET for a Net.
     * @return  the NET
     */
    public NET getNET () { return(net); }
    
    /**
     * Return an array of unnamed Nets.
     * @param   size is the size of the array
     * @return  the Net array
     */
    static public Net[] netArray (int size) {
        Net[]   n = new Net[size];
        for (int i=0 ; i<size ; i++)
            n[i] = new Net();
        return(n);
    }
        
    /**
     * Add an XDC property file string line.
     * @param   p is the property name string
     * @param   v is the property value string
     */
    public void addProperty (String p, String v) {
        net.properties.put(p,  v);
    }
    
    /**
     * Get a net given its identifier. If it is not
     * known, construct a new one using the identifier.
     * @param   ident is the net identifier
     * @return  the net
     */
    static public Net get (String ident) {
        if (identmap.containsKey(ident))
            return(identmap.get(ident));
        else
            return(new Net(ident, false));
    }
    
    /**
     * Get a net given its identifier. If it is not
     * known, return null.
     * @param   ident is the net identifier
     * @return  the net
     */
    static public Net find (String ident) {
        if (identmap.containsKey(ident))
            return(identmap.get(ident));
        else
            return(null);
    }
    
    /**
     * Get a net given an output pin designation. If it is not
     * found, return null.
     * @param   pin is the pin identifier
     * @return  the net
     */
    static public Net findFromPin (String pin) { return(getFamily().netFromPin(pin)); }

    /**
     * Set an external port name and type.
     * @param   name is the external port name
     * @param   type is the external port type
     */
    public void setPort (String name, TDEVPtype type) {
        net.extportname = name;
        net.extport = type;
    }
    
    /**
     * Get a list of all net identifiers that contain the substring s,
     * but exclusive of s itself.
     * @param   s is the search string
     * @return  the list of identifiers
     */
    static public ArrayList<String> getSigList (String s) {
        ArrayList<String>   al = new ArrayList<String>();
        TreeSet<String>     ts = new TreeSet<String>(identmap.keySet());
        for (String ss : ts)
            if ((ss.indexOf(s) != -1) && !s.equals(ss))
                al.add(ss);
        return(al);
    }
    
    /**
     * Initialise extraction of elements for which this net is an input.
     */
    public void getInputElementInit () {
        input_elements = new ArrayList<Element>();
        input_ports = new ArrayList<String>();
        for (int i=0 ; i<net.porttypes.size() ; i++)
            if (net.porttypes.get(i) == PortType.INPORT) {
                input_elements.add(net.elements.get(i));
                input_ports.add(net.ports.get(i));
            }
        input_index = 0;
    }
    
    /**
     * Get the next element for for which this net is an input.
     * @return  the next element of a series as a list of 2 items, the 1st
     *          being the Element and the 2nd the port string
     */
    public ArrayList<Object> getNextInputElement () {
        ArrayList<Object>   al = new ArrayList<Object>();
        if (input_index < input_elements.size()) {
            al.add(input_elements.get(input_index));
            al.add(input_ports.get(input_index));
            input_index++;
            return(al);
        } else
            return(null);
    }

    /**
     * Initialise extraction of elements for which this net is an output.
     */
    public void getOutputElementInit () {
        output_elements = new ArrayList<Element>();
        output_ports = new ArrayList<String>();
        for (int i=0 ; i<net.porttypes.size() ; i++)
            if (net.porttypes.get(i) != PortType.INPORT) {
                output_elements.add(net.elements.get(i));
                output_ports.add(net.ports.get(i));
            }
        output_index = 0;
    }
    
    /**
     * Get the next element for for which this net is an output.
     * @return  the next element of a series as a list of 2 items, the 1st
     *          being the Element and the 2nd the port string
     */
    public ArrayList<Object> getNextOutputElement () {
        ArrayList<Object>   al = new ArrayList<Object>();
        if (output_index < output_elements.size()) {
            al.add(output_elements.get(output_index));
            al.add(output_ports.get(output_index));
            output_index++;
            return(al);
        } else
            return(null);
    }
    
    /**
     * Get the element whose output drives the Net.
     * If there is not a single Element output throw an exception.
     * @return  the element driving the Net
     */
    public Element getOutputElement () {
        for (int i=0 ; i<net.porttypes.size() ; i++)
            if (net.porttypes.get(i) != PortType.INPORT)
                return(net.elements.get(i));
        throw new ExEx("System error: Net.getOutputElement() - not a single driver");
    }
    
    /**
     * Get the number of elements to which this net is an input.
     * @return  the number of elements
     */
    public int getNumInputs () { return(net.inports); }
    
    /**
     * Get the number of elements to which this net is an output.
     * @return  the number of elements
     */
    public int getNumOutputs () { return(net.outports); }
    
    /**
     * Get the number of elements to which this net is a 3-state output.
     * @return  the number of elements
     */
    public int getNumTSOutputs () { return(net.tsoutports); }
    
    /**
     * Determine if this net is an external output port.
     * @return  true if this net is an external output port
     */
    public boolean  isExtOutPort () { return(net.isExtOutPort()); }
    
    /**
     * Get the preferred net identifier.
     * @return  the net identifier
     */
    public String getIdent () {
        return(net.prefident);
    }
    
    /**
     * Get the actual net identifier.
     * @return  the actual net identifier
     */
    public String getLocalIdent () { return(id); }
    
    /**
     * Get the list of net identifiers.
     * @return  the net identifier list
     */
    public ArrayList<String> getIdents () { return(net.idents); }
    
    /**
     * Get the net identifier translated for EDIF. All '.' are changed
     * to "_", all "[" to "_" and all "]" are removed.
     * @return  the translated EDIF net identifier
     */
    public String getEDIFIdent () { return(net.getEDIFIdent()); }
    
    /*
     * Set the padding bit to indicate that this net is connected to GND or VCC
     * as a result of a cast to a greater width.
    public void setPadding () { padding = true; }
     */
    
    /*
     * Get the padding bit which indicates if this net is connected to GND or VCC
     * as a result of a cast to a greater width.
     * @return  the padding bit
    public boolean getPadding () { return(padding); }
     */
    
    /**
     * Connect the net to an element port. This does not modify the
     * associated fields in the Element. It is only called from
     * addInput(), addOutput() or addTS()} in class Element which
     * handle the related changes in that class.
     * @param   e is the element
     * @param   port is the element port
     * @param   type is INPORT, OUTPORT or TSPORT
     */
    public void connect (Element e, String port, PortType type) {
        net.elements.add(e);
        net.ports.add(port);
        net.porttypes.add(type);
        switch (type) {
        case INPORT:
            net.inports++;
            break;
        case OUTPORT:
            net.outports++;
            break;
        case TSPORT:
            net.tsoutports++;
        }
        if ((net.outports > 1) || (net.outports != 0) && (net.tsoutports != 0)) {
            throw new ExEx("!!!");
        }
    }
    
    /**
     * Disconnect the net from an element port. If the port argument is null
     * the first port found to which the net connects is disconnected.
     * @param   e is the element
     * @param   port is the element port or null
     */
    public void disconnect (Element e, String port) {
        for (int i=0 ; i<net.elements.size() ; i++) {
            if ((net.elements.get(i) == e) &&
                ((port == null) || (net.ports.get(i).equals(port)))) {
                switch (net.porttypes.get(i)) {
                case INPORT:
                    net.inports--;
                    break;
                case OUTPORT:
                    net.outports--;
                    break;
                case TSPORT:
                    net.tsoutports--;
                }
                net.elements.remove(i);
                net.ports.remove(i);
                net.porttypes.remove(i);
                return;
            }
        }
        
        msg("net " + this.id);
        msg("element cellname " + e.getCellName());
        msg("element port " + port);
        throw new ExEx("System error: Net.disconnect() failed");
    }
    
    /**
     * Determine if two nets are connected.
     * @param   n is the net to compare with this net.
     * @return  true if the nets are connected
     */
    public boolean isConnected (Net n) { return(net == n.net); }
    
    /**
     * Connect this net to another net.
     * @param   n is the net to connect to
     */
    public void connect(Net n) {
        if (this.equals(n)) {
            msg("SYSTEM ERROR - netlist self-connect");
            msg(id + " to " + n.id);
            StringBuffer    sb = new StringBuffer();
            for (String s: net.idents)
                sb.append(s + " ");
            msg(sb.toString());
            msg("This could be caused by the assignment to a value mode variable of an \n"
                    + "expression containing the same variable where it has not been\n"
                    + "initialised and has not been assigned a value.");
            msg("This can also result in nets with no source. ");
            return;
        }
        
        if (n.equals(LO) || n.equals(HI)) {
            n.connect(this);
            return;
        }
        
        if ((getNumOutputs() + n.getNumOutputs()) > 1) {
            msg("connection of nets will result in multiple sources");
            StringBuffer    sb = new StringBuffer();
            for (String s: net.idents)
                sb.append(s + " ");
            for (String s: n.net.idents)
                sb.append(s + " ");
            msg(sb.toString());
            throw new ExEx("");
        }
                
        NET     nnet = n.net;
        /*
        if ((net.elements.size() != 0) && net.elements.equals(nnet.elements) &&
            net.ports.equals(nnet.ports))
            throw new ExEx("SYSTEM ERROR - netlist self-connect");
        if (net.prefident.equals(nnet.prefident))
            throw new ExEx("SYSTEM ERROR - netlist self-connect");
        */
        
        // Add all element connections of the new Net to this Net.
        net.nets.addAll(nnet.nets);
        net.elements.addAll(nnet.elements);
        net.ports.addAll(nnet.ports);
        net.porttypes.addAll(nnet.porttypes);
        net.inports += nnet.inports;
        net.outports += nnet.outports;
        net.tsoutports += nnet.tsoutports;
        if (nnet.extport != TDEVPtype.NONE)
            net.extport = nnet.extport;
        
        // Add in the new ident list.
        net.idents.addAll(nnet.idents);

        // Choose the new preferred ident from the expanded ident list.
        // net.setPrefIdent();
        
        instances.remove(n.net);
        
        // Change all Nets associated with n.net to reference net.
        for (Net nn : nnet.nets)
            nn.net = net;
    }
    
    /**
     * Change a port identifier. This is used when gate optimisation
     * removes some inputs leaving a gap in the sequence I0, I1 etc.
     * @param   e is the element
     * @param   old_port is the current port identifier
     * @param   new_port is the new port identifier
     */
    public void changePort (Element e, String old_port, String new_port) {
        for (int i=0 ; i<net.elements.size() ; i++) {
            if ((net.elements.get(i) == e) && net.ports.get(i).equals(old_port)) {
                net.ports.set(i, new_port);
                return;
            }
        }
        throw new ExEx("changePort() failure!");
    }
        
    /**
     * If signal 'startex' has no source, connect it to VCC.
     */
    public static void fixstartex () {
        Net         startex = get("startex");
        for (PortType t : startex.net.porttypes)
            if (t == PortType.OUTPORT)
                return;
        startex.connect(HI);
    }
    
    /**
     * Scan all nets selecting the preferred identifier from the
     * identifiers of the connected subnets.
     */
    static public void resolveNetIdents () {
        for (NET n : instances)
            n.setPrefIdent();
    }
    
    /**
     * Output the netlist instances to the EDIF netlist.
     * @param   pw is the output interface to the EDIF netlist file
     */
    static public void outputInstances (PrintWriter pw) {
        for (NET n : instances) {
            if ((n.elements.size() == 0) || (n.inports == 0) && !n.isExtOutPort())
                continue;
            StringBuffer    b = new StringBuffer();
            String          id = n.getEDIFIdent();
            if (id.length() > 250)
                // EDIF wont allow identifiers longer than about 255 characters,
                // so mangle the identifier with a unique integer in the middle
                id = id.substring(0, 100) + "_____" + icount++ + "_____" + id.substring(id.length()-100);
            pw.println("     (net " + id + "");
            pw.println("      (joined");
            if (n.extport != TDEVPtype.NONE) {
                b.append("       (portRef ");
                if (n.extportname != null)
                    b.append(n.extportname);
                else
                    b.append(id);
                b.append(")");
                pw.println(b);
            }
            for (int i=0 ; i<n.elements.size() ; i++) {
                Element e = n.elements.get(i);
                b = new StringBuffer();
                String  s = n.ports.get(i);
                b.append("       (portRef ");
                b.append(s);
                b.append(" (instanceRef ");
                b.append(e.getIdent());
                b.append("))");
                pw.println(b);
            }
            pw.println("      )");
            pw.println("     )");
        }
    }

    /**
     * Verify that there are no netlist problems. A net may have no sinks
     * or may have multiple 3-state drivers. There is an error if a net
     * has no source or has any additional source to a single output driver.
     * All nets are examined and false is returned if any violate the above
     * rules.
     * @return  true if all nets are OK
     */
    public static boolean verify () {
        boolean     success = true;
        for (NET n : instances) {
            if ((n.outports == 0) && (n.tsoutports == 0) && (n.inports != 0) &&
                (n.extport == TDEVPtype.NONE)) {
                msg("net " + n.prefident + " has no source");
                success = false;
            }
            if ((n.outports > 1) || (n.outports != 0) && (n.tsoutports != 0)) {
                msg("net " + n.prefident + " has multiple sources");
                success = false;
            }
        }
        return(success);
    }
    
    /**
     * Check all Nets for consistency with their connected Elements.
     */
    public static void checkall () {
        for (NET n : instances) {
            for (int i=0 ; i<n.elements.size() ; i++) {
                Element e = n.elements.get(i);
                e.checkpin(n, n.ports.get(i), n.porttypes.get(i));
            }
        }
    }
    
    /**
     * Check that the Net is connected to the designated pin of an Element.
     * If it is not, throw an exception.
     * @param   e is the element
     * @param   pin is the pin identifier
     */
    public void checkpin (Element e, String pin) {
        for (int i=0 ; i<net.elements.size() ; i++)
            if ((net.elements.get(i) == e) && (pin.equals(net.ports.get(i))))
                return;
        throw new ExEx("net not connected to pin");
    }
    
    /**
     * Output netlist equivalent identifiers. This is invoked if the
     * -n option is used and it writes to a .net file.
     * @param   pw is the output to which to write
     */
    static public void outputNets (PrintWriter pw) {
        TreeMap<String, NET>    tm = new TreeMap<String, NET>();
        int                     linelength = 80;
        int                     indent = 8;
        int                     len, l;
        boolean                 first;
        for (NET n : instances) {
            String edif_ident = n.getEDIFIdent();
            if (n.elements.size() != 0)
                tm.put(edif_ident, n);
        }
        Set<String> ks = tm.keySet();
        for (String edif_ident : ks) {
            NET     n = tm.get(edif_ident);
            StringBuffer sb = new StringBuffer();
            sb.append(edif_ident);
            sb.append(":");
            pw.println(sb);

            sb = new StringBuffer();
            len = 0;
            first = true;
            while (len < indent) {
                sb.append(" ");
                len++;
            }
            for (String s : n.idents) {
                l = s.length();
                if (!first && (len + l) > linelength) {
                    pw.println(sb);
                    sb = new StringBuffer();
                    len = 0;
                    while (len < indent) {
                        sb.append(" ");
                        len++;
                    }
                    first = true;
                } else
                    len += l;
                sb.append(s);
                sb.append(" ");
                first = false;
            }
            pw.println(sb);
        }
    }
    
    /**
     * Get the total number of net identifiers.
     * @return  the total number of net identifiers
     */
    static public int getNumNetIds () { return(identmap.size()); }

    /**
     * Get the number of nets.
     * @return  the number of nets
     */
    static public int getNumNets () { return(instances.size()); }
}
