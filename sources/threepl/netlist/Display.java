package threepl.netlist;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.SwingConstants;

import threepl.ThreePL;

/**
 * This class implements a graphics window for displaying the netlist.
 * A text window is provided for entry of net identifiers. Identifiers are
 * typed into the text window followed by a return. If the typed string
 * is an unambiguous net identifier the element which is the source of
 * the net is displayed. If the string is a substring of a number of
 * net identifiers a pop-up list is displayed. If the string is equal to one
 * of the list identifiers that identifier will appear at the head of the list,
 * the remainder of the list being sorted. Selection of a list entry
 * will result in the list disappearing and the driving element being
 * displayed (the drawing window automatically scrolls back to the top
 * right corner).
 *
 * The input signal to a displayed element can be sourced by left clicking
 * on the input wire. A left click to the body of a displayed element will
 * expand all its inputs. This process can be reversed, i.e. input
 * connections can be removed, by middle clicking.
 *
 * An element that appears more than once in the schematic will be displayed
 * in green instead of black.
 */
@SuppressWarnings("serial")
public class Display extends JPanel implements MouseListener, ActionListener {
    public static final int     scroll_width = 1500;
    public static final int     scroll_height = 1000;
    private static JScrollPane  scroller;
    private JPanel              paneli;
    private static JPanel       drawingPane;
    private static JPanel       panelr;
    private static JRadioButton sigbutton;
    private static JRadioButton pinbutton;
    private static JRadioButton blockbutton;
    private static ButtonGroup  bg;
    private JTextField          signal;
    private JLabel              siglabel;
    private boolean             pinmode = false;
    private boolean             blockmode = false;

    /**
     * Create the netlist display window.
     */
    public Display () {
        super(new BorderLayout());
        
        sigbutton = new JRadioButton("signal");
        sigbutton.setSelected(true);
        sigbutton.setActionCommand("s");
        sigbutton.addActionListener(this);
        pinbutton = new JRadioButton("pin");
        pinbutton.setActionCommand("p");
        pinbutton.addActionListener(this);
        blockbutton = new JRadioButton("block");
        blockbutton.setActionCommand("b");
        blockbutton.addActionListener(this);
        bg = new ButtonGroup();
        bg.add(sigbutton);
        bg.add(pinbutton);
        bg.add(blockbutton);
        panelr = new JPanel(new GridLayout(0, 1));
        panelr.add(sigbutton);
        panelr.add(pinbutton);
        panelr.add(blockbutton);

        siglabel = new JLabel("signal", SwingConstants.LEFT);
        siglabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        signal = new JTextField(2);
        signal.addActionListener(this);
        signal.setActionCommand("t");

        paneli = new JPanel(new GridLayout(1, 2));
        paneli.add(panelr);
        paneli.add(siglabel);
        paneli.add(signal);

        drawingPane = new DrawingPane();
        drawingPane.setBackground(new Color(249, 249, 255));
        drawingPane.addMouseListener(this);
        drawingPane.setPreferredSize(new Dimension(NetListDraw.width, NetListDraw.height));
        
        scroller = new JScrollPane(drawingPane);
        scroller.setPreferredSize(new Dimension(scroll_width, scroll_height));

        add(scroller, BorderLayout.NORTH);
        add(paneli, BorderLayout.SOUTH);
        setOpaque(true); //content panes must be opaque

        resetScroll();  // scroll to top right corner
    }
    
    /**
     * Pane class for drawing the schematic.
     */
    public class DrawingPane extends JPanel {
        /**
         * Overrides the JPanel method to paint the component.
         * @param   g is the graphics context
         */
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            NetListDraw.draw(g);
        }
    }

    /**
     * Overriden method to get mouse button presses.
     * @param   e is the mouse event
     */
    public void mousePressed (MouseEvent e) {
        if (e.getButton() == 1) {
            NetListDraw.expand(e.getX(), e.getY(), drawingPane.getGraphics());
            drawingPane.repaint();
            return;
        } else if (e.getButton() == 2) {
            NetListDraw.delete(e.getX(), e.getY(), drawingPane.getGraphics());
            drawingPane.repaint();
            return;
        } else if (e.getButton() == 3) {
            String  s[];
            s = NetListDraw.properties(e.getX(), e.getY());
            if (s != null)
                Properties.displayText(s);
        }
    }
    
    public void mouseReleased (MouseEvent e) {}
    public void mouseClicked (MouseEvent e) {}
    public void mouseEntered (MouseEvent e) {}
    public void mouseExited (MouseEvent e) {}

    /**
     * Overriden method to get the return key event from
     * the signal identifier text window.
     * @param   event is the text window event
     */
    public void actionPerformed (ActionEvent event) {
        if (event.getActionCommand().equals("t")) {
            if (pinmode) {
                Net n = Net.findFromPin(signal.getText());
                if (n == null)
                    return;
                String id = n.getIdent();
                resetScroll();
                NetListDraw.initiate(id, Display.get_graphics());
                Display.refresh();
            } else if (blockmode) {
                Element e = Element.getElement(signal.getText());
                if (e == null)
                    return;
                resetScroll();
                NetListDraw.initiate(e, Display.get_graphics());
                Display.refresh();
            } else {
                Net         match_net;
                String      match_name = null;
                match_net = Net.find(signal.getText());
                if (match_net != null)
                    match_name = match_net.getIdent();
                ArrayList<String> al = Net.getSigList(signal.getText());
                if ((match_net == null) && (al.size() == 0))
                    return;
                if ((match_net != null) && (al.size() == 0)) {
                    NetListDraw.initiate(match_name, drawingPane.getGraphics());
                    refresh();
                    return;
                }
                resetScroll();
                SigList.displayList(al, match_name);
            }
        } else if (event.getActionCommand().equals("s")) {
            siglabel.setText("signal");
            pinmode = false;
            blockmode = false;
        } else if (event.getActionCommand().equals("p")) {
            siglabel.setText("pin");
            pinmode = true;
            blockmode = false;
        } else if (event.getActionCommand().equals("b")) {
            siglabel.setText("block");
            pinmode = false;
            blockmode = true;
        }
    }
    
    /**
     * Overriden method to repaint the frame contents.
     */
    public static void refresh () {
        drawingPane.repaint();
    }
    
    /**
     * Get the graphics context for the drawing pane.
     * @return  the graphics context for the drawing pane
     */
    public static Graphics get_graphics () {
        return(drawingPane.getGraphics());
    }

    /**
     * Create the GUI and show it. For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI () {
        // Make sure we have nice window decorations.
        //JFrame.setDefaultLookAndFeelDecorated(true);

        // Create and set up the window.
        JFrame frame = new JFrame("Netlist " + ThreePL.design_name);
        if (ThreePL.file_chooser_used)
            frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        else
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create and set up the content pane.
        JComponent newContentPane = new Display();
        frame.setContentPane(newContentPane);

        // Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Create the netlist display window.
     */
    public static void display () {
        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    createAndShowGUI();
                }
            }
        );
        
        // Schedule the signal list window similarly.
        // This is not initially visible but pops up when
        // an identifier that matches more than one net is entered.
        SigList.sigList();
        
        // Schedule the properties text window similarly.
        // This is not initially visible but pops up when
        // button 3 is clocked on an element or element pin.
        Properties.propertiesText();
    }

    /**
     * Reset the scroll pane around the netlist drawing pane to
     * the top right corner.
     */
    public static void resetScroll () {
        int bars = 2 * (int)scroller.getVerticalScrollBar().getMaximumSize().getWidth();
        JViewport    vp = scroller.getViewport();
        vp.setViewPosition(new Point(NetListDraw.width-scroll_width+bars, 0));
    }
}
