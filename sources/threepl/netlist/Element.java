package threepl.netlist;

import static threepl.ThreePL.*;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import threepl.exceptions.ExEx;
import threepl.netlist.Ports.portwidth;
import threepl.parser.Functions;

/**
 * This class represents components.
 *
 * String 'cellname' is the name of the element, e.g. "AND2", "FDRSE" etc.
 * These cell names are those required by the vendor software in the netlist
 * file.
 *
 * String 'ident' is a unique element identifier and is of the form "b123".
 *
 * Static 'celldecls' is a map indexed by cell name containing instances of
 * class Ports. Ports contains 3 sets, inputs, outputs and 3-state outputs.
 * Each of these sets contains Strings giving the port names.
 *
 * Class 'Element' contains 3 sets, 'inportset', 'outportset' and
 * 'tsportset'. These sets are the same sets as those contained in the Ports
 * class in the 'celldecls' map indexed by the cell name of this instance.
 * When a port connection is added to an element the port name is checked
 * against the appropriate set and if not present is added. Since the port
 * sets are shared among elements with the same cell name the port sets for
 * an element type are built up by usage and are not pre-defined. This can be
 * a problem where nested gates are combined into a single gate during
 * optimisation since that element prototype might not have been encountered
 * during netlist building. For that reason AND, OR, XOR, XNOR gates and LUTs
 * are initialised in map celldecls for number of inputs from 1 to the LUT
 * width (XTDECode.initialise())
 *
 * Maps 'inports', 'outports' and 'tsports' represent the input and output
 * connections to the element. The keys to these maps are the port name
 * strings and the associated values are the connected nets (class 'Net').
 *
 * Enumerated type 'element_type' is an element type label used to identify
 * elements which will be subject to later optimisation.
 *
 * Each instance of 'Element' is contained in static list 'instances'.
 * Elements whose type is NONE are added at the end of the list, all others
 * being inserted at the front. Thus the elements that are labeled as being
 * subject to optimisation are located at the head of list 'instances'. The
 * number of such entries in the list is given by 'ocount'.
 *
 * Field 'expression' contains the expression for a LUT element. It is
 * kept in this field for display purposes but is translated into a
 * hexadecimal string and stored in field 'property_init' for later
 * inclusion in the netlist file as an INIT property to initialise the
 * element (LUT contents or flip-flop initial state).
 *
 * Static fields 'gnd' and 'vcc' are pre-defined elements for ground
 * and supply, each having a single output (see XElements constructor).
 * 
 * NOTE: when 3PL is run repeatedly via the GUI, class TDECode and all
 * subclasses down to the class specific to the FPGA family are
 * constructed afresh. Thus all static variables are re-created. Classes
 * Element and Net are NOT reconstructed and hence static variables in
 * these classes must be explicitly re-initialised on each run! In the case
 * of this class this is done by method init().
 */
public class Element implements NetConstants {
    private String                                      cellname;
    private String                                      ident;
    public  TreeMap<String, Net>                        inports = new TreeMap<String, Net>();
    public  TreeMap<String, Net>                        outports = new TreeMap<String, Net>();
    public  TreeMap<String, Net>                        tsports = new TreeMap<String, Net>();
    public  TreeMap<String,String>                      properties = new TreeMap<String,String>();
    public  String                                      expression;
    private TreeMap<String, portwidth>                  inportset;
    private TreeMap<String, portwidth>                  outportset;
    private TreeMap<String,portwidth>                   tsportset;
    private Gtype                                       element_type;
    
    public int          count = 0;  // netlist schematic display counter
    
    public static Element           gnd;
    public static Element           vcc;

    private     static int                              ocount; // count of optimisable elements
    public      static TreeMap<String,Ports>            celldecls;
    private     static ArrayList<Element>               instances;
    private     static TreeMap<String,Element>          elementMap;
    
    /**
     * Construct a general element. These are given the
     * gate type Gtype.NONE and are not subject to netlist
     * optimisation.
     * @param   name is the element name, e.g. IBUF
     */
    public Element (String name) { makeElement(name, null, Gtype.NONE); }
    
    /**
     * Construct a general element. These are given the
     * gate type Gtype.NONE and are not subject to nelist
     * optimisation. The block name must be unique.
     * @param   name is the element name, e.g. IBUF
     * @param   bname is the netlist block name
     */
    public Element (String name, String bname) { makeElement(name, bname, Gtype.NONE); }
    
    /**
     * Construct a gate element. The gate type is used
     * to direct netlist optimisation.
     * @param   name is the element name, e.g. IBUF
     * @param   element_type is the element gate type,
     *          e.g. IBUF Gtype.MUXCY
     */
    public Element (String name, Gtype element_type) { makeElement(name, null, element_type); }
    
    /**
     * Construct a gate element. The gate type is used
     * to direct netlist optimisation.
     * @param   name is the element name, e.g. IBUF
     * @param   bname is the netlist block name
     * @param   element_type is the element gate type,
     *          e.g. IBUF Gtype.MUXCY
     */
    public Element (String name, String bname, Gtype element_type) { makeElement(name, bname, element_type); }
    
    /**
     * Common code used by the constructors.
     * The block name argument is usually omitted and the netlist
     * block identifier is generated. If the block name is provided
     * it must be unique.
     * @param   name is the element name, e.g. IBUF
     * @param   bname is an optional netlist block name
     * @param   element_type is the element gate type,
     */
    private void makeElement (String name, String bname, Gtype element_type) {
        Ports   p;
        ident = (bname == null) ? "b" + icount++ : bname;
        this.element_type = element_type;
        if (element_type == Gtype.NONE)
            instances.add(this);    // non-optimised elements at rear of list
        else {
            instances.add(0, this); // optimisable elements at front of list
            ocount++;               // count the optimisable elements
        }
        cellname = name;
        if (celldecls.containsKey(name))
            p = celldecls.get(name);
        else {
            p = new Ports();
            celldecls.put(name, p);
        }
        elementMap.put(ident, this);
        inportset = p.inports;
        outportset = p.outports;
        tsportset = p.tsports;
    }
    
    /**
     * Initialise static variables.
     */
    public static void init () {
        icount = 0; // unique integer for identifiers
        ocount = 0; // count of optimisable elements
        celldecls = new TreeMap<String,Ports>();
        instances = new ArrayList<Element>(20000);
        elementMap = new TreeMap<String,Element>();
    }

    /**
     * Put a logic gate cell declaration in the cell map.
     * @param   name is the gate element name
     * @param   nports is the number of input ports
     */
    public static void putCelldecl (String name, int nports) {
        celldecls.put(name, gatePorts(nports));
    }
    
    /**
     * Get the size of a port for a special element, i.e. elements created using
     * the TDEELEMENT TDE.
     * @param element is the element name
     * @param port is the port name
     * @return the width of the port
     *
    public int getSpecialElementPortSize (String element, String port) {
        return(special_element_port_sizes.get(element).get(port));
    } */
   
    /**
     * Create a port class for a gate element. The parameter indicates the
     * number of input ports and there is one output port.
     * @param   n is the number of input ports.
     * @return  the port class
     */
    public static Ports gatePorts (int n) {
        Ports   p = new Ports();
        p.outports.put("O", new portwidth(0, 0));
        for (int i=0 ; i<n ; i++)
            p.inports.put("I" + i, new portwidth(0, 0));
        p.inports.put("I", new portwidth(n, 1));
        return(p);
    }
    
    /**
     * Get the type of the element.
     * @return  the element type
     */
    public Gtype getType () { return(element_type); }
    
    /**
     * Set the type of the element.
     * @param   type is the element type
     */
    public void setType (Gtype type) { element_type = type; }
    
    /**
     * Get the input ports map of the element.
     * @return  the input ports map
     */
    public  TreeMap<String, Net> getInPorts () { return(inports); }
    
    /**
     * Get the output ports map of the element.
     * @return  the output ports map
     */
    public  TreeMap<String, Net> getOutPorts () { return(outports); }
    
    /**
     * Get the output Net of the element.
     * If there is not a single output throw an exception.
     * @return  the element output Net
     */
    public Net getOutNet () {
        if (outports.size() != 1)
            throw new ExEx("System error: Element.getOutNet() - not a single output");
        Iterator<Map.Entry<String,Net>> oi = outports.entrySet().iterator();
        Map.Entry<String,Net>   me = oi.next();
        return(me.getValue());
    }
    
    /**
     * Change an element. This is used during optimisation
     * to change an element into another. Previous connections
     * are optionally stripped .
     * @param   name is the new element name, e.g. INV
     * @param   element_type is the element gate type,
     *          e.g. IBUF Gtype.INV
     * @param   strip if true results in the element being stripped of
     *          input and output Nets
     */
    public void changeElement (String name, Gtype element_type, boolean strip) {
        Ports   p;
        if (strip)
            strip();
        cellname = name;
        this.element_type = element_type;
        if (celldecls.containsKey(name))
            p = celldecls.get(name);
        else {
            p = new Ports();
            celldecls.put(name, p);
        }
        if (strip) {
            inports = new TreeMap<String, Net>();
            outports = new TreeMap<String, Net>();
            tsports = new TreeMap<String, Net>();
        }
        inportset = p.inports;
        outportset = p.outports;
        tsportset = p.tsports;
    }
    
    /**
     * Prepare to delete this element by disconnecting all nets.
     */
    public void strip () {
        for (Map.Entry<String,Net> me : inports.entrySet()) {
            String      pinname  = me.getKey();
            Net         net = me.getValue();
            net.disconnect(this, pinname);
        }
        inports.clear();
        for (Map.Entry<String,Net> me : outports.entrySet()) {
            String      pinname  = me.getKey();
            Net         net = me.getValue();
            net.disconnect(this, pinname);
        }
        outports.clear();
        for (Map.Entry<String,Net> me : tsports.entrySet()) {
            String      pinname  = me.getKey();
            Net         net = me.getValue();
            net.disconnect(this, pinname);
        }
        tsports.clear();
    }
    
    /**
     * Get an iterator over a list of input nets to this element.
     * @return  the input net list iterator
     */
    public Iterator<Map.Entry<String,Net>> getInputsIterator () { return(inports.entrySet().iterator()); }

    /**
     * Get an iterator over a list of output nets to this element.
     * @return  the output net list iterator
     */
    public Iterator<Map.Entry<String,Net>> getOutputsIterator () { return(outports.entrySet().iterator());
    }

    /**
     * Get an iterator over a list of 3-state output nets to this element.
     * @return  the 3-state output net list iterator
     */
    public Iterator<Map.Entry<String,Net>> getTSOutputsIterator () { return(tsports.entrySet().iterator()); }
    
    /**
     * Add an expression to a LUT. The expression string is kept
     * for display purposes and is also translated into a property
     * value for LUT initialisation.
     * @param   s is the expression string
     */
    public void addExpression (String s) {
        expression = s;
        properties.put("INIT", Functions.bit_pattern(s, TDECode.lutwidth));
    }
    
    /**
     * Add a property.
     * If the value is null, the property will be removed if present.
     * If the value is not null and the property is already present the
     * new value will overwrite the previous value.
     * @param   n is the property name
     * @param   v is the property value string
     */
    public void addProperty (String n, String v) {
        if (v == null)
            properties.remove(n);
        else
            properties.put(n, v);
    }
    
    /**
     * Get a property
     * @param   n is the property name
     * @return  the property value string
     */
    public String getProperty (String n) { return(properties.get(n)); }
    
    /**
     * Add an input port to the input port set for this element.
     * @param port is the port name
     * @param width is the port width
     * @param arrayformat is 0 for a single bit port, 1 for a port array of the form A0, A1, A2 etc. and 2 for
     * a port expressed explicitly as an array.
     */    
    public void addInPort (String port, Integer width, int arrayformat) {
        TreeMap<String,portwidth> hm = inportset;
        addPort(hm, port, width, arrayformat);
    }
    
    /**
     * Add an output port to the output port set for this element.
     * @param port is the port name
     * @param width is the port width
     * @param arrayformat is 0 for a single bit port, 1 for a port array of the form A0, A1, A2 etc. and 2 for
     * a port expressed explicitly as an array.
     */    
    public void addOutPort (String port, Integer width, int arrayformat) {
        TreeMap<String,portwidth> hm = outportset;
        addPort(hm, port, width, arrayformat);
    }
    
    /**
     * Add a 3-state port to the input port set for this element.
     * @param port is the port name
     * @param width is the port width
     * @param arrayformat is 0 for a single bit port, 1 for a port array of the form A0, A1, A2 etc. and 2 for
     * a port expressed explicitly as an array.
     */    
    public void addTSPort (String port, Integer width, int arrayformat) {
        TreeMap<String,portwidth> hm = tsportset;
        addPort(hm, port, width, arrayformat);
    }
    
    /**
     * Add a port to the input port set for this element.
     * @param hm is the appropriate port map for input, output or 3-state ports.
     * @param port is the port name
     * @param width is the port width
     * @param arrayformat is 0 for a single bit port, 1 for a port array of the form A0, A1, A2 etc. and 2 for
     * a port expressed explicitly as an array.
     */   
    private void addPort (TreeMap<String, portwidth> hm, String port, Integer width, int arrayformat) {
        portwidth   pw = hm.get(port);
        if (pw == null) {
            hm.put(port, new portwidth(width, arrayformat));
            return;
        }
        Integer sprev = pw.width;
        if (arrayformat != pw.arrayformat)
            throw new ExEx("Attempt to redefine array port from/to discrete/array form, element " + ident + " port " + port);
        if ((width > sprev))
            pw.width = width;
    }
    
    /**
     * Add an input net. The Net argument may be null, in which
     * case no operation occurs.
     * @param   port is the input port identifying string
     * @param   n is the net
     */
    public void addInput (String port, Net n) {
        switch (element_type) {
        case AND:
        case OR:
        case XOR:
            // These gates have varying numbers of inputs and are subject
            // to changes to the number of inputs during optimisation,
            // so the input port set should not be touched. It can be assumed
            // that the input port sets for all these gates that are within the
            // accepted number of inputs for the FPGA family will have been
            // already created.
            break;
        default:
            addInPort(port, 0, 0);
        }
        if (n == null)
            return;
        inports.put(port, n);
        n.connect(this, port, PortType.INPORT);
    }

    /**
     * Add an input net array. The Net argument may be null, in which
     * case no operation occurs.
     * @param   port is the input port identifying string head
     * @param   n is the net array
     * @param   arrayformat selects the array form to be used (discrete pins or array form)
     */
    public void addInputArray (String port, Net[] n, int arrayformat) {
        if (n == null)
            return;
        int width = n.length;
        
        switch (arrayformat) {
        case 0:
            throw new ExEx("addInputArray call error");
        case 1:
            for (int i=0 ; i<width ; i++) {
                String  ps = port + i;
                addInput(ps, n[i]);
                addInPort(ps, 0, 0);
            }
        break;
        case 2:
            for (int i=0 ; i<width ; i++)
                addInput("(member " + port + " " + i + ")", n[i]);
        }
        addInPort(port, width, arrayformat);
    }

    /**
     * Add an input net array with MSB padding. The Net argument may be null,
     * in which case the whole port array is padded.
     * @param   port is the input port identifying string head
     * @param   n is the net array
     * @param   portsize is the number of pins in the port
     * @param   pad is the padding signal
     * @param   arrayformat selects the array form to be used (discrete pins or array form)
     */
    public void addInputArray (String port, Net[] n, int portsize, Net pad, int arrayformat) {
        int     asize = (n == null) ? 0 : n.length;
        
        switch (arrayformat) {
        case 0:
            throw new ExEx("addInputArray call error");
        case 1:
            for (int i=0 ; i<asize ; i++)
                addInput(port + i, n[i]);
            for (int i=asize ; i<portsize ; i++)
                addInput(port + i, pad);
            break;
        case 2:
            for (int i=0 ; i<asize ; i++)
                addInput("(member " + port + " " + i + ")", n[i]);
            for (int i=asize ; i<portsize ; i++)
                addInput("(member " + port + " " + i + ")", pad);
        }
        addInPort(port, portsize, arrayformat);
    }

    /**
     * Add an input net array with LSB padding.
     * 'max' gives the number of port pins which are available.
     * 'size' is the number of these pins to be assigned. If 'size'
     * is smaller than 'max' the lower number pins are to be omitted.
     * The input array 'n' may be smaller than 'size' in which case higher
     * number pins are assigned GND. The Net argument may be null, in
     * which the 'size' inputs are all assigned GND.
     * @param   port is the input port identifying string head
     * @param   n is the net array
     * @param   size is the notional number of pins in the port
     * @param   max is the physical number of pins in the port
     * @param   pad is the padding signal
     * @param   arrayformat selects the array form to be used (discrete pins or array form)
     */
    public void addInputArrayTop (String port, Net[] n, int size, int max, Net pad, int arrayformat) {
        int     npad = max - size;
        int     asize = (n == null) ? 0 : n.length;
        int     portnum = 0;
        
        switch (arrayformat) {
        case 0:
            throw new ExEx("addInputArray call error");
        case 1:
            for (int i=0 ; i<npad ; i++)
                addInput(port + portnum++, pad);
            for (int i=0 ; i<size ; i++)
                addInput(port + portnum++, (i < asize) ? n[i] : Net.LO);
            break;
        case 2:
            for (int i=0 ; i<npad ; i++)
                addInput("(member " + port + " " + portnum++ + ")", pad);
            for (int i=0 ; i<size ; i++)
                addInput("(member " + port + " " + portnum++ + ")", (i < asize) ? n[i] : Net.LO);
        }
        addInPort(port, max, arrayformat);
    }

    /**
     * Add an input net array. Integers 0, 1 etc are
     * appended to the port identifier string.
     * @param   port1 is the first input port identifying string head
     * @param   port2 is the second input port identifying string head
     * @param   n is the net array
     * @param   size1 is the number of ports using the first head string
     * @param   arrayformat selects the array form to be used (discrete pins or array form)
     */
    public void addInputArray (
        String  port1,
        String  port2,
        Net[]   n,
        int     size1,
        int     arrayformat
    ) {
        int     portnum1 = 0;
        int     portnum2 = 0;
        int     asize = (n == null) ? 0 : n.length;
        
        switch (arrayformat) {
        case 0:
            throw new ExEx("addInputArray call error");
        case 1:
            for (int i=0 ; i<asize ; i++)
                if (i < size1)
                    addInput(port1 + portnum1++, n[i]);
                else
                    addInput(port2 + portnum2++, n[i]);
            break;
        case 2:
            for (int i=0 ; i<asize ; i++)
                if (i < size1)
                    addInput("(member " + port1 + " " + portnum1++ + ")", n[i]);
                else
                    addInput("(member " + port2 + " " + portnum2++ + ")", n[i]);
        }
        addInPort(port1, portnum1, arrayformat);
        if (portnum2 != 0)
            addInPort(port2, portnum2, arrayformat);
    }

    /**
     * Add an input net array with padding. The Net argument may be null,
     * in which the whole port array is padded. Integers 0, 1 etc are
     * appended to the port identifier string.
     * @param   port1 is the first input port identifying string head
     * @param   port2 is the second input port identifying string head
     * @param   n is the net array
     * @param   size1 is the number of ports using the first head string
     * @param   size is the total number of ports
     * @param   pad is the padding signal
     * @param   arrayformat selects the array form to be used (discrete pins or array form)
     */
    public void addInputArray (
        String  port1,
        String  port2,
        Net[]   n,
        int     size1,
        int     size,
        Net     pad,
        int     arrayformat
    ) {
        int     portnum1 = 0;
        int     portnum2 = 0;
        int     asize = (n == null) ? 0 : n.length;
        
        switch (arrayformat) {
        case 0:
            throw new ExEx("addInputArray call error");
        case 1:
            for (int i=0 ; i<asize ; i++)
                if (i < size1)
                    addInput(port1 + portnum1++, n[i]);
                else
                    addInput(port2 + portnum2++, n[i]);
            for (int i=asize ; i<size ; i++)
                if (i < size1)
                    addInput(port1 + portnum1++, pad);
                else
                    addInput(port2 + portnum2++, pad);
            break;
        case 2:
            for (int i=0 ; i<asize ; i++)
                if (i < size1)
                    addInput("(member " + port1 + " " + portnum1++ + ")", n[i]);
                else
                    addInput("(member " + port2 + " " + portnum2++ + ")", n[i]);
            for (int i=asize ; i<size ; i++)
                if (i < size1)
                    addInput("(member " + port1 + " " + portnum1++ + ")", pad);
                else
                    addInput("(member " + port2 + " " + portnum2++ + ")", pad);
        }
        addInPort(port1, portnum1, arrayformat);
        if (portnum2 != 0)
            addInPort(port2, portnum2, arrayformat);
    }

    /**
     * Add an output net. The Net argument may be null, in which
     * case no operation occurs.
     * @param   port is the output port identifying string
     * @param   n is the net
     */
    public void addOutput (String port, Net n) {
        if (n == null)
            return;
        if (n.getNumOutputs() != 0)
            throw new ExEx("net " + n.getIdent() + " conflicting source " + cellname + " port " + port);
        addOutPort(port, 0, 0);
        outports.put(port, n);
        n.connect(this, port, PortType.OUTPORT);
    }

    /**
     * Add an output net array. The Net argument may be null, in which
     * case no operation occurs. Integers 0, 1 etc are appended to the
     * port identifier string.
     * @param   port is the output port identifying string head
     * @param   n is the net array
     * @param   arrayformat selects the array form to be used (discrete pins or array form)
     */
    public void addOutputArray (String port, Net[] n, int arrayformat) {
        if (n == null)
            return;
        int     width = n.length;
        
        switch (arrayformat) {
        case 0:
            throw new ExEx("addInputArray call error");
        case 1:
            for (int i=0 ; i<width ; i++)
                addOutput(port + i, n[i]);
            break;
        case 2:
            for (int i=0 ; i<width ; i++)
                addOutput("(member " + port + " " + i + ")", n[i]);
        }
        addOutPort(port, width, arrayformat);
    }

    /**
     * Add an output net array. The Net argument may be null, in which
     * case no operation occurs. Integers 0, 1 etc are appended to the
     * port identifier string.
     * @param   port1 is the first input port identifying string
     * @param   port2 is the second input port identifying string
     * @param   size1 is the number of pins in the first port
     * @param   n is the net array
     * @param   arrayformat selects the array form to be used (discrete pins or array form)
     */
    public void addOutputArray (String port1, String port2, int size1, Net[] n, int arrayformat) {
        if (n == null)
            return;
        int     portnum1 = 0;
        int     portnum2 = 0;
        
        switch (arrayformat) {
        case 0:
            throw new ExEx("addInputArray call error");
        case 1:
            for (int i=0 ; i<n.length ; i++)
                if (i < size1)
                    addOutput(port1 + portnum1++, n[i]);
                else
                    addOutput(port2 + portnum2++, n[i]);
            break;
        case 2:
            for (int i=0 ; i<n.length ; i++)
                if (i < size1)
                    addOutput("(member " + port1 + " " + portnum1++ + ")", n[i]);
                else
                    addOutput("(member " + port2 + " " + portnum2++ + ")", n[i]);
        }
        addOutPort(port1, portnum1, arrayformat);
        if (portnum2 != 0)
            addOutPort(port2, portnum2, arrayformat);
    }

    /**
     * Add a 3-state driven net. The Net argument may be null, in which
     * case no operation occurs.
     * @param   port is the 3-state port identifying string
     * @param   n is the net
     */
    public void addTS (String port, Net n) {
        if (n == null)
            return;
        n.connect(this, port, PortType.TSPORT);
        addTSPort(port, 0, 0);
    }
    
    /**
     * Add a 3-state driven net array. The Net argument may be null, in which
     * case no operation occurs. Integers 0, 1 etc are appended to the
     * port identifier string.
     * @param   port is the output port identifying string head
     * @param   n is the net array
     * @param   arrayformat selects the array form to be used (discrete pins or array form)
     */
    public void addTSArray (String port, Net[] n, int arrayformat) {
        if (n == null)
            return;
        int     width = n.length;
        
        switch (arrayformat) {
        case 0:
            throw new ExEx("addInputArray call error");
        case 1:
            for (int i=0 ; i<width ; i++)
                addTS(port + i, n[i]);
            break;
        case 2:
            for (int i=0 ; i<width ; i++)
                addTS("(member " + port + " " + i + ")", n[i]);
        }
        addTSPort(port, width, arrayformat);
    }

    /**
     * Remove a net from an element pin and remove the Element as a
     * termination of the Net.
     * @param   port is the port to which the net is connected
     */
    public void removeNet (String port) {
        if (inports.containsKey(port)) {
            inports.get(port).disconnect(this, port);
            inports.remove(port);
        } else if (outports.containsKey(port)) {
            outports.get(port).disconnect(this, port);
            outports.remove(port);
        } else if (tsports.containsKey(port)) {
            tsports.get(port).disconnect(this, port);
            tsports.remove(port);
        } else
            throw new ExEx("net not connected to element");
    }
    
    /**
     * Remap the inputs of a gate element. The inputs will be
     * remapped to I0, I1 etc.
     */
    public void remapInputs () {
        int                     n = inports.size();
        TreeMap<String, Net>    ports_new = new TreeMap<String, Net>();
        Net                     inet;
        int                     i = 0;
        for (Map.Entry<String,Net> me : inports.entrySet()) {
            String      newport = "I" + i++;
            String      port = me.getKey();
            inet = me.getValue();
            inet.changePort(this, port, newport);
            ports_new.put(newport, inet);
        }

        inports = ports_new;
        cellname = element_type.toString() + n;

        Ports p;
        if (celldecls.containsKey(cellname)) {
            p = celldecls.get(cellname);
            inportset = p.inports;
            outportset = p.outports;
            tsportset = p.tsports;
        } else {
            // Remapped wide gates may not have declared cell
            // set entries - dont worry as wide gates will
            // be converted so will never need these sets anyway.
            inportset = null;
            outportset = null;
            tsportset = null;
        }
    }
    
    /**
     * Get the identifier for this component.
     * @return  the unique identifier string
     */
    public String getIdent () { return(ident); }
    
    /**
     * Get the cell name for this component.
     * @return  the cell name
     */
    public String getCellName () { return(cellname); }
    
    /**
     * Output the cell port definitions to the EDIF netlist.
     * Only cells which have been instantiated are written out.
     * Some cells may have been optimised out such that no instances
     * remain in which case they are not written to the EDIF file cell
     * definition section.
     * @param   pw is the output interface to the EDIF netlist file
     * @param   view is the EDIF view parameter
     */
    static public void outputCells (PrintWriter pw, String view) {
        pw.println("");
        pw.println(" (external " + libref);
        pw.println("  (edifLevel 0)");
        pw.println("  (technology (numberDefinition ))");
        // Traverse the list of cell instances checking each to
        // see if the (shared) element in map celldecls has been
        // written out yet. If not, write it out and mark it.
        for (Element e : instances) {
            String  name = e.cellname;
            Ports   p = celldecls.get(name);
            if (!p.written) {
               p.outputCell(pw, name, view);
               p.written = true;
            }
        }
        pw.println(" )");
    }
    
    /**
     * Get the cell instances list.
     * @return  element instances list
     */
    static public ArrayList<Element> getInstances () { return(instances); }
    
    /**
     * Get the number of elements that are optimisable gates.
     * Elements whose type is not NONE are positioned at the head of the element
     * list to allow multiple iterations of the gate optimiser to avoid
     * passing over other element types.
     * @return  optimisable gate count
     */
    static public int getOptimisableElementCount () { return(ocount); }
    
    /**
     * Output the cell instances to the EDIF netlist.
     * @param   pw is the output interface to the EDIF netlist file
     * @param   view is the EDIF view parameter
     */
    static public void outputInstances (PrintWriter pw, String view) {
        for (Element e : instances)
            e.outputInstance(pw, view);
    }
    
    /**
     * Output an EDIF string for an instance of this element to the EDIF
     * netlist. Ignore if it has no ports.
     * @param   pw is the output interface to the EDIF netlist file
     * @param   view is the EDIF view parameter
     */
    private void outputInstance (PrintWriter pw, String view) {
        if (inports.size() + outports.size() + tsports.size() == 0)
            return;
        StringBuffer b = new StringBuffer();
        
        b.append("     (instance " + ident + " (viewRef " + view + " (cellRef " + cellname);
        if (libref != null)
            b.append(" (libraryRef " + libref + ")");
        b.append("))");
        Set<Map.Entry<String,String>>   mes = properties.entrySet();
        if (mes.isEmpty())
            b.append(")");
        pw.println(b);
        if (mes.isEmpty())
            return;
        Iterator<Entry<String, String>> mesi = mes.iterator();
        while (mesi.hasNext()) {
            Map.Entry<String,String>     me = mesi.next();
            pw.println("      (property " + me.getKey() + " (string \"" + me.getValue() + "\"))");
            //pw.println("     )");
        }
        pw.println("     )");
    }
    
    /**
     * Get the number of pins connected to this element.
     * @return  the number of connected pins
     */
    public int numPins () { return(inports.size() + outports.size() + tsports.size()); }
    
    /**
     * Get the number of input pins connected to this element.
     * @return  the number of connected input pins
     */
    public int numInPins () { return(inports.size()); }
    
    /**
     * Get the number of output pins connected to this element.
     * @return  the number of connected output pins
     */
    public int numOutPins () { return(outports.size() + tsports.size()); }
    
    /**
     * CLear the count of the number of time this element is displayed
     * in the netlist display window.
     */
    public static void clearAllDIsplayCounts () {
        for (Element e : instances)
            e.count = 0;
    }
    
    /**
     * Check all elements for consistency with their connected Nets.
     */
    public static void checkall () {
        for (Element e : instances) {
            for (Map.Entry<String,Net> me : e.inports.entrySet()) {
                String      pinname  = me.getKey();
                Net         net = me.getValue();
                net.checkpin(e, pinname);
            }
            for (Map.Entry<String,Net> me : e.outports.entrySet()) {
                String      pinname  = me.getKey();
                Net         net = me.getValue();
                net.checkpin(e, pinname);
            }
            for (Map.Entry<String,Net> me : e.tsports.entrySet()) {
                String      pinname  = me.getKey();
                Net         net = me.getValue();
                net.checkpin(e, pinname);
            }
        }
    }
    
    /**
     * Check that a NET is connected to the designated pin of an Element.
     * @param   n is the NET
     * @param   pin is the pin
     * @param   pt is the port type, INPORT, OUPORT or TSPORT
     */
    public void checkpin (Net.NET n, String pin, PortType pt) {
        switch (pt) {
        case INPORT:
            for (Map.Entry<String,Net> me : inports.entrySet()) {
                String      pinname  = me.getKey();
                Net         net = me.getValue();
                if ((n == net.getNET()) && (pin.equals(pinname)))
                    return;
            }
            throw new ExEx("pin not connected to net");
        case OUTPORT:
            for (Map.Entry<String,Net> me : outports.entrySet()) {
                String      pinname  = me.getKey();
                Net         net = me.getValue();
                if ((n == net.getNET()) && (pin.equals(pinname)))
                    return;
            }
            throw new ExEx("pin not connected to net");
        case TSPORT:
            for (Map.Entry<String,Net> me : tsports.entrySet()) {
                String      pinname  = me.getKey();
                Net         net = me.getValue();
                if ((n == net.getNET()) && (pin.equals(pinname)))
                    return;
            }
            throw new ExEx("pin not connected to net");
        }
    }
    
    /**
     * Print to standard output a list of all elements and the number
     * of occurrences of each.
     */
    static public void printElementCount () {
        HashMap<String, Integer> counts = new HashMap<String, Integer>();
        
        for (Element e : instances) {
            String  s = e.cellname;
            if (counts.containsKey(s))
                counts.put(s, counts.get(s) + 1);
            else
                counts.put(s, 1);
        }
        rpt("");
        rpt("\tcell occurrences -");
        rpt("");
        for (Map.Entry<String,Integer> me : counts.entrySet()) {
            String      s = me.getKey();
            Integer     i = me.getValue();
            String      c = String.valueOf(i);
            while ((s.length() + c.length()) < 20)
                s = s + " ";
            rpt("\t\t" + s + " " + c);
        }
    }

    /**
     * Get the number of elements.
     * @return  the number of elements
     */
    static public int getNumElements () { return(instances.size()); }
    
    /**
     * Find an element.
     * @param   id is the unique element identifier
     * @return  the Element
     */
    static public Element getElement (String id) { return(elementMap.get(id)); }
}
 
