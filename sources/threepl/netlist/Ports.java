package threepl.netlist;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;
    
/**
 * This class represents the names of input, output and 3-state output
 * ports of an element.
 * In each of the 3 port maps in class Element the key is the pin name and the corresponding value is class Port.
 * 'width' is the number of pins in the port.
 * 'arrayformat' indicates the required form of ports which are arrays of pins.
 * arrayformat=0 indicates a non-array (single) pin.
 * arrayformat=1 indicates a port where array members are expanded into discrete pins of the form A0, A1 etc.
 * arrayformat=2 indicates a port where cell ports are declared as port arrays and instances use the
 * array 'member' form.
 */
public class Ports {
    static final String    libver = "2.0.0";
    
    public static class portwidth {
        public int     width;
        public int     arrayformat;
        
        public portwidth (int width, int arrayformat) {
            this.width = width;
            this.arrayformat = arrayformat;
        }
    }

    TreeMap<String,portwidth> inports;
    TreeMap<String,portwidth> outports;
    TreeMap<String,portwidth> tsports;
    boolean written = false;

    /**
     * Construct a ports class.
     */
    protected Ports () {
        inports  = new TreeMap<String,portwidth>();
        outports = new TreeMap<String,portwidth>();
        tsports  = new TreeMap<String,portwidth>();
    }

    /**
     * Output the EDIF netlist code for a cell definition for an element to the EDIF netlist.
     * @param   pw is the output interface to the EDIF netlist file
     * @param   name is the cell name
     * @param   view is the cell view
     */
    protected void outputCell (PrintWriter pw, String name, String view) {
        Iterator<Entry<String, portwidth>>     it;

        pw.println("  (cell " + name + " (cellType GENERIC)");
        pw.println("   (view " + view + " (viewType NETLIST)");
        pw.println("    (interface");
        
        it = inports.entrySet().iterator();
        outputPortstrings(pw, it, "INPUT");       

        it = outports.entrySet().iterator();
        outputPortstrings(pw, it, "OUTPUT");

        it = tsports.entrySet().iterator();
        outputPortstrings(pw, it, "INOUT");

        pw.println("    )");
        pw.println("    (property LIBVER (string \"" + libver + "\"))))");
    }
    
    /**
     * Output the EDIF netlist code for a cell definition port to the EDIF netlist.
     * @param   pw is the output interface to the EDIF netlist file
     * @param   it is an iterator through input, output or 3-state ports
     * @param   dir is the string "INPUT", "OUTPUT" or "INOUT"
     */
    private void outputPortstrings (PrintWriter pw, Iterator<Entry<String, portwidth>> it, String dir) {
        while (it.hasNext()) {
            Entry<String, portwidth>    me = it.next();
            String                      port = me.getKey();
            portwidth                   w = me.getValue();
            StringBuffer                sb = new StringBuffer();
            if (!port.startsWith("(m")) {
                switch (w.arrayformat) {
                case 0:
                    sb.append("     (port ");
                    sb.append(port);
                    sb.append(" (direction ");
                    sb.append(dir);
                    sb.append("))");
                    break;
                case 1:
                    continue;
                case 2:
                    sb.append("     (port (array (rename ");
                    sb.append(port);
                    sb.append(" \"");
                    sb.append(port);
                    sb.append("[0:");
                    sb.append(w.width-1);
                    sb.append("]\") ");
                    sb.append(w.width);
                    sb.append(")");
                    sb.append(" (direction ");
                    sb.append(dir);
                    sb.append("))");
                }
                pw.println(sb.toString());
            }
        }
    }
}
