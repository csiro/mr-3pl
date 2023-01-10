package threepl.netlist;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * This class implements all the schematic drawing code except
 * the GUI itself.
 */
public class NetListDraw {
    private static final int    BOX_Y_OFFS = 15;
    private static final int    BOX_L_MARGIN = 10;
    private static final int    IO_Y_SPACE = 13;
    private static final int    BLK_WID = 80;
    //private static final int    BLK_WIR_DELY = 15;
    private static final int    BLK_NAME_Y_OFFS = 2;
    private static final int    PINNAME_L_MARGIN = 2;
    private static final int    PINNAME_YOFFS = 5;
    private static final int    NETNAME_SPACE = 5;
    private static final int    NETNAME_L_YOFFS = 10;
    private static final int    ISIGXOFFS = 0;
    private static final int    CHANNEL_SPACE = 10;
    private static final float  FONT_SCALE = (float)9.0;
    private static final int    COLMAX = 50;
    private static final int    ROWMAX = 50;

    private static boolean              initialised = false;
    public static  int                  width = 5000;
    public static  int                  height = 2000;
    private static Gblock               select_block;
    private static int                  select_pin;
    private static Gblock               start_block = null;
    private static int[]                col_x_orig = new int[COLMAX];
    private static int[]                col_x_chan = new int[COLMAX];
    @SuppressWarnings("unchecked")
    public static  ArrayList<Gblock>[]  col_blocks = new ArrayList[COLMAX];
    private static int[]                col_channels = new int[COLMAX];
    private static int[][]              col_y_remap = new int[COLMAX][ROWMAX];
    private static int                  pin_row;
    private static boolean              reloc_toggle = false;
    

    /**
     * Draw the current schematic.
     * @param   g is the graphics context
     */
    public static void draw (Graphics g) {
        if (!initialised) {
	    initialise();
            return;
        }
        for (int col=0 ; col<COLMAX ; col++)
            if (col_blocks[col].size() > 0)
                draw_blocks_in_column(col_blocks[col], g);
    }
    
    /**
     * Draw all the blocks in a column.
     * @param   l is a list of column blocks
     * @param   g is the graphics context
     */
    private static void draw_blocks_in_column (ArrayList<Gblock> l, Graphics g) {
        pin_row = 0;
        for (Gblock gb: l) {
            gb.draw_block(g);
            gb.draw_wires(g);
        }
    }

    /**
     * Given a net identifier locate the source block (or first
     * source block) and display it.
     * @param   signame is the net identifier
     * @param   g is the graphics context
     */
    public static void initiate (String signame, Graphics g) {
        boolean     found;
	found = setup_initial_block(signame);
        if (!found)
            return;
	rescale_all_blocks(g);
	relocate_all_blocks();
    }

    /**
     * Given a logic element, display it.
     * @param   e is the logic element
     * @param   g is the graphics context
     */
    public static void initiate (Element e, Graphics g) {
	setup_initial_block(e);
	rescale_all_blocks(g);
	relocate_all_blocks();
    }

    /**
     * Given a net identifier locate the source block (or first
     * source block) leaving the block in static variable 'start_block'
     * and its pin index in static variable 'start_block_out_pin'.
     * @param   signame is the net identifier
     * @return  true if identifier is non-null and the net is found and
     *          has a source
     */
    private static boolean setup_initial_block (String signame) {
    	initialise();
    	if (signame == null || signame.equals(""))
            return(false);     // no name provided
    	Net n = Net.find(signame);
    	if (n == null)
    	    return(false);     // no such signal
    
    	n.getOutputElementInit();
    	ArrayList<Object>  al = n.getNextOutputElement();
    	if (al == null)
            return(false);	// signal has no source
        
        start_block = new Gblock((Element)al.get(0), 0);
        String pn = (String)al.get(1);
        for (int i=0 ; i<start_block.out_pins ; i++)
            if (start_block.pinname[i].equals(pn))
                return(true);
        return(false);
    }

    /**
     * Given a logic element, locate the associated source block
     * leaving the block in static variable 'start_block'
     * and an output pin index in static variable 'start_block_out_pin'.
     * @param   e is the logic element
     */
    private static void setup_initial_block (Element e) {
	initialise();
        start_block = new Gblock(e, 0);
    }

    /**
     * Initialise static variables for a new schematic display.
     */
    @SuppressWarnings("unchecked")
    private static void initialise () {
        col_x_orig = new int[COLMAX];
        col_x_chan = new int[COLMAX];
        col_blocks = new ArrayList[COLMAX];
        col_channels = new int[COLMAX];
        col_y_remap = new int[COLMAX][ROWMAX];
        for (int i=0 ; i<COLMAX ; i++)
            col_blocks[i] = new ArrayList<Gblock>();
        start_block = null;
        Element.clearAllDIsplayCounts();
        initialised = true;
    }

    /**
     * Expand the schematic to the source of an input pin or all the inputs
     * of a graphics block. If the coordinates are in the
     * vicinity of an input wire the wire and its source block are drawn.
     * If the coordinates are within a block all its input wires
     * and associated source blocks are drawn.
     * @param   x is the cursor x coordinate
     * @param   y is the cursor y coordinate
     * @param   g is the graphics context
     */
    public static void expand (int x, int y, Graphics g) {
        if (!initialised) {
            initialise();
            return;
        }

	select_block = null;
	select_pin = -1;
	get_block_and_pin(x, y);

	if (select_block == null)
            return;

	Gblock gb = select_block;
	if (select_pin == -1) {
            for (int pin=gb.out_pins ; pin<gb.pins ; pin++)
                gb.expand_input(pin);
	} else if (select_pin >= gb.out_pins)
            gb.expand_input(select_pin);
        else
            gb.expand_output(select_pin);
	rescale_all_blocks(g);
	relocate_all_blocks();
    }

    /**
     * Delete a graphics block from the display. The block is found
     * using the cursor coordinates. If the coordinates are in the
     * vicinity of an input wire the wire and its source block are removed.
     * If the coordinates are within a block the block and all its inputs
     * are removed, including all preceding connected blocks.
     * @param   x is the cursor x coordinate
     * @param   y is the cursor y coordinate
     * @param   g is the graphics context
     */
    public static void delete (int x, int y, Graphics g) {
        if (!initialised) {
            initialise();
            return;
        }

	select_block = null;
	select_pin = -1;
	get_block_and_pin(x, y);
	Gblock gb = select_block;
	int pin = select_pin;

	if (gb == null)
	    return;
        gb.delete_block(pin, true);

	rescale_all_blocks(g);
	relocate_all_blocks();
    }

    /**
     * Get the properties of an element or net. The block is found using
     * the cursor coordinates. If the cursor is positioned on an element
     * input or output wire the properties of the associated net are
     * returned, otherwise the properties of the element are returned.
     * The returned string may contain more than one line of text.
     * @param   x is the cursor x coordinate
     * @param   y is the cursor y coordinate
     * @return  an array of 2 strings. The first member is the net
     *          identifier or else the element name followed by the
     *          element identifier. The second member is the text giving
     *          the properties.
     */
    public static String[] properties (int x, int y) {
        if (!initialised) {
            initialise();
            return(null);
        }
        
	select_block = null;
	get_block_and_pin(x, y);
        if (select_block == null)
            return(null);

        String[]                s = new String[2];
        StringBuffer            sb = new StringBuffer();
        Element                 e = select_block.element;
        if (select_pin >= 0) {
            Net     n = select_block.net[select_pin];
            s[0] = n.getIdent();
            sb.append("\n");
            ArrayList<String>    idents = n.getIdents();
            for (String id : idents) {
                if (id != n.getIdent())
                    sb.append(id + "\n");
            }
        } else {
            s[0] = e.getCellName() + " " + e.getIdent();
            if (e.properties.containsKey("INIT")) {
                sb.append("init - ");
                sb.append(e.properties.get("INIT"));
                sb.append("\n");
            }
            if (e.expression != null) {
                sb.append("expression - ");
                sb.append(e.expression);
                sb.append("\n");
            }
        }
        s[1] = sb.toString();
        return(s);
        
    }

    /**
     * Scale all graphics blocks and associated block and signal labels.
     * @param   g is the graphics context
     */
    private static void rescale_all_blocks (Graphics g) {
        for (int i=0 ; i<COLMAX ; i++) {
            for (Gblock gb: col_blocks[i])
                gb.scale_block(g);
        }
    }

    /**
     * Relocate all blocks so that signal wires do not cross.
     */
    private static void relocate_all_blocks () {
        // Traverse each column in turn from right to left arranging
        // graphics blocks in the order of their connections to blocks
        // in the the following column so that connections do not
        // cross.
        reloc_toggle = !reloc_toggle;
        for (int col=0 ; col<COLMAX ; col++) {
            if (col_blocks[col].size() > 0) {
                sort_column_blocks(col);
                relocate_column_blocks(col);
            }
        }

        // Allocate the vertical parts of intercolumn connections to
        // channels between columns.
        for (int col=1 ; col<COLMAX ; col++) {
            if (col_blocks[col].size() > 0)
                map_channels(col);
        }
    }

    /**
     * Traverse a column from top to bottom sorting source blocks
     * for each input in the next column to the left. This
     * ensures that source blocks are generally in order of the
     * associated inputs in the column to the right.
     * @param   col is the column index
     */
    private static void sort_column_blocks (int col) {
        ArrayList<Gblock>   a = col_blocks[col+1];
        int                 index = 0;
        for (Gblock gb: col_blocks[col]) {
            for (int i=gb.out_pins ; i<gb.pins ; i++) {
                if (!gb.expanded[i])
                    continue;
                for (BListElement ble: gb.blist[i]) {
                    Gblock inp_gb = ble.block;
                    if ((inp_gb != null) && (inp_gb.reloc_toggle != reloc_toggle)) {
                        int j = a.indexOf(inp_gb);
                        /*
                        System.out.println("move " + inp_gb.element.getCellName() +
                        " col " + (col+1) +
                        "from " + j +
                        " to " + index);
                        */
                        a.remove(j);
                        a.add(index++, inp_gb);
                        inp_gb.reloc_toggle = reloc_toggle;
                    }
                }
            }
        }
    }

    /**
     * Re-compute column and graphics block origins for a column.
     * @param   col is the column index
     */
    private static void relocate_column_blocks (int col) {
    	int         max_width = 0;
    	int         channels = 0;
    	int         x_origin;
    	int         y_origin = 0;
    
    	if (col == 0) {
            for (Gblock gb: col_blocks[col]) {
    	        col_x_orig[0] = width - gb.width - 1;
    	        col_x_chan[0] = -1;	/* no route channel right of col 0 */
    	        gb.xorg = col_x_orig[0];
    	        gb.yorg = y_origin;
    	        y_origin += gb.height;
    	    }
            return;
    	}

    	// Traverse all blocks in the column, finding
    	// the maximum width.
        for (Gblock gb: col_blocks[col])
            if (gb.width > max_width)
                max_width = gb.width;
    
    	// Traverse all blocks in the previous (to right) column to
    	// get the number of input connections and hence compute the
    	// routing channel width.
        for (Gblock gb: col_blocks[col-1]) {
    		for (int i=gb.out_pins ; i<gb.pins ; i++)
    		    if (gb.expanded[i])
    		        channels++;
        }
    
    	int channel_width = (channels + 1) * CHANNEL_SPACE;
    	col_x_chan[col] = col_x_orig[col - 1] - channel_width;
    	col_x_orig[col] = x_origin = col_x_chan[col] - max_width;
    	col_channels[col] = channels;
    
    	// Traverse all blocks in the column relocating them.
        for (Gblock gb: col_blocks[col]) {
    	    gb.xorg = x_origin;
    	    gb.yorg = y_origin;
    	    y_origin += gb.height;
    	}
    }

    /**
     * Allocate the vertical parts of intercolumn connections to
     * channels between this column and the one to the left.
     * @param   col is the column index
     */
    private static void map_channels (int col) {
    	int     channels = col_channels[col];
    	int[]   max = new int[channels];
    	int[]   right_y = new int[channels];
    	int     pin_row = 0;
    	// Traverse all blocks in the column gathering remap data.
        for (Gblock gb: col_blocks[col-1]) {
    	    for (int i=gb.out_pins ; i<gb.pins ; i++) {
        		if (!gb.expanded[i])
        			continue;
    		
            	int max_y = -1;
        		int y;
        
        		// output wires
                for (BListElement ble: gb.blist[i]) {
                    Gblock inp_gb = ble.block;
        		    if (inp_gb == null)
        			continue;
        		    int pin = ble.pin;
        		    y = inp_gb.yorg+inp_gb.pin_rel_y[pin];
        		    if (y > max_y)
        			max_y = y;
        		}
    
        		// input wire
        		y = gb.yorg+gb.pin_rel_y[i];
        		right_y[pin_row] = y;
        		if (y > max_y)
        		    max_y = y;
        		max[pin_row] = max_y;
        		pin_row++;
    	    }
    	}

    	// Assign a 'ramp' to channel remap array for this column.
    	for (int i=0 ; i<channels ; i++)
    	    col_y_remap[col][i] = i;

    	// Find runs in channel mapping where inputs
    	// are at the bottom of a vertical connecting wire.
    	// Reverse the order of these runs in the channel mapping.
    	for (int i=0 ; i<channels ; i++) {
    	    if (max[i] == right_y[i]) {
                int     j;
        		for (j=i; j<channels ; j++) 
        		    if (max[j] != right_y[j])
        		        break;
        		reverse_map(col, i, j-1);
        		i = j;
    	    }
    	}
    }

    /**
     * Reverse the column entries in the col_y_remap array
     * between two row indices.
     * @param   col is the map column
     * @param   i is the 1st row index
     * @param   j is the 2nd row index
     */
    private static void reverse_map (int col, int i, int j) {
        int temp;
    	while (i<j) {
    	    temp = col_y_remap[col][i];
            col_y_remap[col][i] = col_y_remap[col][j];
            col_y_remap[col][j] = temp;
    	    i++;
    	    j--;
    	}
    }
    
    private static void shift_all_columns_left () {
        // If left-most column is not empty, clear it by removing
        // all output pin expansions and removing all input pin expansions
        // from the next block to the right.
        if (!col_blocks[COLMAX-1].isEmpty()) {
            for (Gblock gb: col_blocks[COLMAX-1])
                for (int pin=0 ; pin<gb.out_pins ; pin++)
                    gb.expanded[pin] = false;
            for (Gblock gb: col_blocks[COLMAX-2])
                for (int pin=gb.out_pins ; pin<gb.pins ; pin++)
                    gb.expanded[pin] = false;
        }
        
        // Move all columns up one.
        for (int col=COLMAX-1 ; col>0 ; col--)
            col_blocks[col] = col_blocks[col-1];
        col_blocks[0] = new ArrayList<Gblock>();

        // Go through all blocks fixing the column numbers.
        for (int col=1 ; col<COLMAX ; col++)
            for (Gblock gb: col_blocks[col])
                gb.column = col;
    }

    /**
     * Find the block and its pin number pointed to at coordinates x,y.
     * These are returned in global variables 'select_block' and
     * 'select_pin'. A null select_block indicates that the mouse cursor
     * was not on a block. A negative select_pin indicates that it was
     * not on a pin or associated wire.
     * @param   x is the x coordinate
     * @param   y is the y coordinate
     */
    private static void get_block_and_pin (int x, int y) {
        for (int col=0 ; col<COLMAX ; col++) {
            for (Gblock gb: col_blocks[col]) {
        		if (x < gb.xorg || x > (gb.xorg+gb.width) ||
        		    y < gb.yorg || y > (gb.yorg+gb.height))
        			// not this block - keep searching
        			continue;
        
        		select_block = gb;
        		select_pin = -1;
        		for (int j=0 ; j<gb.pins ; j++) {
        		    if (x < (gb.xorg+gb.pinreg_xorg[j]) ||
            			x > (gb.xorg+gb.pinreg_xorg[j]+gb.pinreg_width[j]) ||
            			y < (gb.yorg+gb.pinreg_yorg[j]) ||
            			y > (gb.yorg+gb.pinreg_yorg[j]+gb.pinreg_height[j]))
        		        continue;
        		    select_pin = j;
        		    return;
        		}
            }
        }
    }

    /**
     * Get the length of a string when displayed in the graphics
     * window - used to right-justify a string.
     * @param   s is the string
     * @param   g is the graphics context
     * @return  the displayed length in pixels
     */
    private static int gstrlen (String s, Graphics g) {
        //return((int)g.getFontMetrics().getStringBounds(s, g).getWidth());
        return(g.getFontMetrics().stringWidth(s));
    }

    /**
     * This class implements a graphics block. It contains all the
     * parameters associated with drawing an FPGA netlist element.
     */
    static class Gblock {
        // block implicit outer region containing block
        // I/O wires and signal labels
        public int	    xorg;
        public int	    yorg;
        public int	    width;
        public int	    height;
        public int	    pins;
        public int	    out_pins;

        // actual drawn block within above region
        public int	    box_rel_xorg;
        public int	    box_rel_yorg;
        public int	    box_height;

        // pin names, nets etc associated with pins
        public String[]	    pinname;
        public Net[]	    net;
        public int[]	    pinname_xorg;
        public int[]	    pinname_yorg;
        public int[]	    netname_xorg;
        public int[]	    netname_yorg;
        public boolean[]    inverted;
        public boolean[]    expanded;

        // pin regions
        public int[]        pin_rel_x;
        public int[]        pin_rel_y;
        public int[]        pinreg_xorg;
        public int[]        pinreg_yorg;
        public int[]        pinreg_width;
        public int[]        pinreg_height;

        public int          column;
        public Element      element;
        
        public boolean      reloc_toggle;

        public ArrayList<BListElement>[]  blist;

        /**
         * Create a graphics block.
         * @param   e is the hardware element
         * @param   col is the column in which it is to be displayed
         */
        @SuppressWarnings("unchecked")
        public Gblock (Element e, int col) {
            e.count++;
            element = e;

            pins = element.numPins();
            out_pins = element.numOutPins();
            blist = new ArrayList[pins];

            column = col;
            NetListDraw.col_blocks[col].add(this);
            //System.out.println("Gblock " + e.getCellName() + " col " + col);

    	    pinname = new String[pins];
    	    net = new Net[pins];
    	    pinname_xorg = new int[pins];
    	    pinname_yorg = new int[pins];
    	    netname_xorg = new int[pins];
    	    netname_yorg = new int[pins];
    	    pinreg_xorg = new int[pins];
    	    pinreg_yorg = new int[pins];
    	    pinreg_width = new int[pins];
    	    pinreg_height = new int[pins];
    	    pin_rel_x = new int[pins];
    	    pin_rel_y = new int[pins];
    	    inverted = new boolean[pins];
    	    expanded = new boolean[pins];
    	    for (int i=0 ; i<pins ; i++) {
    		    expanded[i] = false;
    		    inverted[i] = false;
                blist[i] = new ArrayList<BListElement>();
    	    }

            int                             index = 0;
            Iterator<Map.Entry<String,Net>> it;
            
            it = e.getOutputsIterator();
            while (it.hasNext()) {
                Map.Entry<String,Net>   me = it.next();
                pinname[index]  = me.getKey();
                net[index] = me.getValue();
                index++;
            }
            it = e.getTSOutputsIterator();
            while (it.hasNext()) {
                Map.Entry<String,Net>   me = it.next();
                pinname[index]  = me.getKey();
                net[index] = me.getValue();
                index++;
            }
            it = e.getInputsIterator();
            while (it.hasNext()) {
                Map.Entry<String,Net>   me = it.next();
                pinname[index]  = me.getKey();
                net[index] = me.getValue();
                index++;
            }
        }

        /**
         * Create a new graphics block that is the logic source for
         * one of the input pins of the current graphics block.
         * @param   pin is the input pin index
         */
        public void expand_input (int pin) {
    	    if (expanded[pin])
    		    return;	// already expanded
    
    	    Net n = net[pin];
            if (n == null)
                return; // ERROR: NOT CONNECTED AFTER ALL!
            if (n.getIdent().equals("GND") || n.getIdent().equals("VCC"))
                return;         // don't expand GND or VCC
    
    	    // Create and link in the input block(s).
    	    // There may be more than one input block per pin (e.g. a bus).
    	    int col = column;
    	    if ((n.getNumOutputs() == 0) && (n.getNumTSOutputs() == 0))
    	        return;	/* signal has no drive */
    	    n.getOutputElementInit();
    	    ArrayList<Object>   al = n.getNextOutputElement();
            while (al != null) {
                Element element = (Element)al.get(0);
    	        Gblock  gb = new Gblock(element, col+1);
                int     src_pin = gb.getPinNo((String)al.get(1));
    	        blist[pin].add(new BListElement(gb, src_pin));
                al = n.getNextOutputElement();
                gb.expanded[src_pin] = true;
    	    }
    	    expanded[pin] = true;
        }

        /**
         * Create new graphics blocks that are the logic sinks for
         * one of the output pins of the current graphics block.
         * @param   pin is the output pin index
         */
        public void expand_output (int pin) {
    	    if (expanded[pin])
    		    return;	// already expanded
    
    	    Net n = net[pin];
    	    if (n == null)
    	        return;	// ERROR: NOT CONNECTED AFTER ALL!
    	    if (n.getNumInputs() == 0)
    	        return;	/* signal has no sinks */
    
            // If we are in the rightmost column, move left all columns by
            // one to free up the rightmost column for the new block(s).
            if (column == 0)
                shift_all_columns_left();
                
    	    // Create and link in the output block(s).
    	    // There will usually be more than one input block per pin.
    	    int col = column;
            n.getInputElementInit();
    	    ArrayList<Object>   al = n.getNextInputElement();
            while (al != null) {
                Element element = (Element)al.get(0);
                Gblock  gb = new Gblock(element, col-1);                
                int     dest_pin = gb.getPinNo((String)al.get(1));
                gb.blist[dest_pin].add(new BListElement(this, pin));
                gb.expanded[dest_pin] = true;
                al = n.getNextInputElement();
    	    }
    	    expanded[pin] = true;
        }

        /**
         * Scale a block and associated block and signal labels.
         * @param   g is the graphics context
         */
        public void scale_block (Graphics g) {
            String  sig_name;
            int     str_len;
            String  pin_name;
            int     row;
            int     box_rows;
    	    int     max_in_sig_name_length = 0;
            int     max_out_sig_name_length = 0;
            
    	    for (int i=out_pins ; i<pins ; i++) {
    	        if ((sig_name = net[i].getIdent()) == null)
    	            continue;
    	        str_len = gstrlen(sig_name, g);
    	        if (str_len > max_in_sig_name_length)
    	            max_in_sig_name_length = str_len;
    	    }
    
    	    for (int i=0 ; i<out_pins ; i++) {
    	        if ((sig_name = net[i].getIdent()) == null)
    	            continue;
    	        str_len = gstrlen(sig_name, g);
    	        if (str_len > max_out_sig_name_length)
    	            max_out_sig_name_length = str_len;
    	    }
    
    	    box_rel_xorg = 2 * NETNAME_SPACE + max_in_sig_name_length;
    	    box_rel_yorg = BOX_Y_OFFS;
    	    width =	4 * NETNAME_SPACE +
    		        max_in_sig_name_length +
    		        max_out_sig_name_length +
    		        BLK_WID;
    
    	    // output wires and signal labels
    	    row = 0;
    	    for (int i=0 ; i<out_pins ; i++) {
    	        if (net[i] != null) {
        		    pin_name = pinname[i];
        		    str_len = gstrlen(pin_name, g);
        		    pinname_xorg[i] = box_rel_xorg+BLK_WID-(str_len+PINNAME_L_MARGIN);
        		    pinname_yorg[i] = box_rel_yorg+(row+1)*IO_Y_SPACE+PINNAME_YOFFS-IO_Y_SPACE/2;
        
        		    sig_name = net[i].getIdent();
        		    netname_xorg[i] = box_rel_xorg+BLK_WID+NETNAME_SPACE;
        		    netname_yorg[i] = box_rel_yorg+(row+1)*IO_Y_SPACE+NETNAME_L_YOFFS-IO_Y_SPACE/2;
        
        		    pinreg_xorg[i] = box_rel_xorg+BLK_WID;
        		    pinreg_yorg[i] = BOX_Y_OFFS + row * IO_Y_SPACE;
        		    pinreg_width[i] = width - pinreg_xorg[i];
        		    pinreg_height[i] = IO_Y_SPACE;
        		    pin_rel_x[i] = width;
        		    pin_rel_y[i] = BOX_Y_OFFS + row * IO_Y_SPACE + IO_Y_SPACE/2;
        
        		    row++;
    	        }  else {
        		    pinreg_xorg[i] = -1;
        		    pinreg_yorg[i] = -1;
        		    pinreg_width[i] = -1;
        		    pinreg_height[i] = -1;
        		    pin_rel_x[i] = 0;
        		    pin_rel_y[i] = 0;
    	        }
    
    	    }
    	    box_rows = row;
    
    	    // input wires and signal labels
    	    row = 0;
    	    for (int i=out_pins ; i<pins ; i++) {
    	        if (net[i] != null) {
        		    pinname_xorg[i] = box_rel_xorg+PINNAME_L_MARGIN;
        		    pinname_yorg[i] = box_rel_yorg+row*IO_Y_SPACE+PINNAME_YOFFS+IO_Y_SPACE/2;
        		    sig_name = net[i].getIdent();
        		    netname_xorg[i] = box_rel_xorg+ISIGXOFFS-gstrlen(sig_name, g);
        		    netname_yorg[i] = box_rel_yorg+row*IO_Y_SPACE+NETNAME_L_YOFFS+IO_Y_SPACE/2;
        
        		    pinreg_xorg[i] = 0;
        		    pinreg_yorg[i] = BOX_Y_OFFS + row * IO_Y_SPACE;
        		    pinreg_width[i] = box_rel_xorg;
        		    pinreg_height[i] = IO_Y_SPACE;
        		    pin_rel_x[i] = 0;
        		    pin_rel_y[i] = BOX_Y_OFFS + row * IO_Y_SPACE + IO_Y_SPACE/2;
        
        		    row++;
    	        }  else {
        		    pinreg_xorg[i] = -1;
        		    pinreg_yorg[i] = -1;
        		    pinreg_width[i] = -1;
        		    pinreg_height[i] = -1;
        		    pin_rel_x[i] = 0;
        		    pin_rel_y[i] = 0;
    	        }
    	    }
    	    if (row > box_rows)
    	        box_rows = row;
    
    	    box_height = box_rows * IO_Y_SPACE;
    	    height = BOX_Y_OFFS + box_height + BOX_L_MARGIN;
        }

        /**
         * Get the pin index associated with a pin identifier.
         * @param   ps is the pin identifier string
         * @return the pin number
         */
        public int getPinNo (String ps) {
            for (int i=0 ; i<pinname.length ; i++)
                if (ps.equals(pinname[i]))
                    return(i);
            return(-1);
        }
    
        /**
         * Draw this block.
         * @param   g is the graphics context
         */
        public void draw_block (Graphics g) {
    	    int     str_len, xs, ys, xf, yf;
    
    	    // Draw box and function name
    	    if (element.count == 1)
    	        g.setColor(Color.black);    // 1st time
    	    else
    	        g.setColor(Color.green);    // has already appeared
            g.setFont(g.getFont().deriveFont(FONT_SCALE));
    
    	    // box
    	    g.drawRect( xorg+box_rel_xorg,
    		            yorg+box_rel_yorg,
    		            BLK_WID,
    		            box_height         );
    
    	    // element name
            g.drawString(   element.getCellName(),
                            xorg+box_rel_xorg,
                            yorg+box_rel_yorg - BLK_NAME_Y_OFFS   );
    
    	    // element identifier
            str_len = gstrlen(element.getCellName(), g);
            g.drawString(   element.getIdent(),
                            xorg+box_rel_xorg + str_len + NETNAME_SPACE,
                            yorg+box_rel_yorg - BLK_NAME_Y_OFFS           );
    
    	    g.setColor(Color.black);
    	    // draw wires signal and labels
    	    for (int i=0 ; i<pins ; i++) {
                g.drawString(   pinname[i],
                                xorg+pinname_xorg[i],
                                yorg+pinname_yorg[i]  );
    	        if (i < out_pins) {
    	            /* outputs */
    	            xs = xorg + box_rel_xorg + BLK_WID;
    	            ys = yorg + pin_rel_y[i];
    	            xf = xorg + width;
    	            yf = ys;
    	            g.drawLine(xs, ys, xf, yf);
                } else {
    	            /* inputs */
    	            xs = xorg;
    	            ys = yorg + pin_rel_y[i];
    	            xf = xorg + box_rel_xorg;
    	            yf = ys;
    	            g.drawLine(xs, ys, xf, yf);
                }
                if ((i < out_pins) || (i >= out_pins) && !expanded[i])
                    g.drawString(   net[i].getIdent(),
	                                xorg+netname_xorg[i],
	                                yorg+netname_yorg[i]  );
            }
        }

        /**
         * Draw all input and output wires for this block.
         * @param   g is the graphics context
         */
        public void draw_wires (Graphics g) {
    	    int     col = column;
    	    int     xs, xf, ys, yf;
    
    	    // Draw wires out from input block to their channel x position.
    	    // Draw wires out from destination block to their channel x position.
    	    // Connect multiple outputs by a vertical wire.
    	    for (int i=out_pins ; i<pins ; i++) {
    	        if (!expanded[i])
    		        continue;	// skip if no pin input connection
    
    	        int	min_y = 99999;
    	        int	max_y = -1;
    
    	        // Input blocks horizontal output wires for this pin.
                int k = blist[i].size();
    	        for (int j=0 ; j<k ; j++) {
        		    if (blist[i] == null)
                        continue;
        		    Gblock  inp_gb = blist[i].get(j).block;
                    int     pin = blist[i].get(j).pin;
        		    xs = inp_gb.xorg+inp_gb.pin_rel_x[pin];
        		    ys = inp_gb.yorg+inp_gb.pin_rel_y[pin];
        		    xf = col_x_chan[col+1] + (col_y_remap[col+1][pin_row] + 1) * CHANNEL_SPACE;
        		    yf = ys;
        		    g.drawLine(xs, ys, xf, yf);
        		    if (ys < min_y)
        		        min_y = ys;
        		    if (ys > max_y)
        		        max_y = ys;
    	        }
    
    	        // Block horizontal pin input wire.
    	        xs = xorg+pin_rel_x[i];
    	        ys = yorg+pin_rel_y[i];
    	        xf = col_x_chan[col+1]  + (col_y_remap[col+1][pin_row] + 1) * CHANNEL_SPACE;
    	        yf = ys;
    	        g.drawLine(xs, ys, xf, yf);
    	        if (ys < min_y)
    	            min_y = ys;
    	        if (ys > max_y)
    	            max_y = ys;
    
    	        // Vertical wire if needed.
    	        if (max_y > min_y) {
        		    xs = col_x_chan[col+1] + (col_y_remap[col+1][pin_row] + 1) * CHANNEL_SPACE;
        		    ys = min_y;
        		    xf = xs;
        		    yf = max_y;
        		    g.drawLine(xs, ys, xf, yf);
    	        }
    	        pin_row++;
    	    }
        }

        /**
         * Recursively delete blocks below this one.
         * If 'pin' is -1, delete all input blocks (descending recursively through
         * all inputs) then delete this block (bgp).
         * If 'pin' is given, recursively delete from that input pin, but not
         * other input pins or the block 'bgp' itself.
         * @param   pin is the input pin index
         * @param   save is true if this itself block is not to be deleted
         */
        public void delete_block (int pin, boolean save) {
    	    // Delete input blocks (where expanded).
    	    // If pin number is given, delete recursively from that pin only.
    	    // If pin number is -1, delete recursively from all input pins.
    	    if (pin >= 0) {
    	        if (!expanded[pin])
    	            return;
    	        int k = blist[pin].size();
    	        for (int j=0 ; j<k ; j++) {
        		    Gblock inp_gb = blist[pin].get(j).block;
        		    if (inp_gb != null)
        		        inp_gb.delete_block(-1, false);
    	        }
                blist[pin].clear();
                expanded[pin] = false;
    	        return;
    	    }
    
    	    for(int i=out_pins ; i<pins ; i++) {
        		if (!expanded[i])
        		    continue;
        		int k = blist[i].size();
        		for (int j=0 ; j<k ; j++) {
        		    Gblock inp_gb = blist[i].get(j).block;
        		    if (inp_gb != null)
        		        inp_gb.delete_block(-1, false);
        		}
                blist[i].clear();
                expanded[i] = false;
    	    }
    
            if (save)
                return;
    	    // delete this block as well.
            element.count--;
            ArrayList<Gblock>   al = col_blocks[column];
    	    al.remove(al.indexOf(this));
        }
    }

    /**
     * This class is a wrapper for a graphics block and associated output pin
     * index for a net source.
     */
    static class BListElement {
        public Gblock  block;
        public int     pin;

        /**
         * Create a graphics block and associated output pin pair.
         * @param   gb is the graphics block
         * @param   p is the pin index
         */
        public BListElement (Gblock gb, int p) {
            block = gb;
            pin = p;
        }
    }
}
