package threepl.netlist;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * This class provides a pop-up scrollable selection list for
 * net identifiers. A single click on a list item selects it
 * and hides the list.
 */
@SuppressWarnings({"serial", "rawtypes", "unchecked"}) // remove for JDK1.7
public class SigList extends JPanel implements ListSelectionListener {
    private static JFrame                   frame;
    private static JList                    list;
    private static DefaultListModel         listModel;

    /**
     * Create the signal list window.
     */
    public SigList() {
        super(new BorderLayout());

        listModel = new DefaultListModel();
        // Create the list and put it in a scroll pane.
        list = new JList(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.clearSelection();
        list.addListSelectionListener(this);
        list.setVisibleRowCount(10);
        JScrollPane listScrollPane = new JScrollPane(list);
        
        add(listScrollPane, BorderLayout.CENTER);
    }

    /**
     * This method is required by ListSelectionListener.
     * It is called when a list entry is selected (Actually
     * when the selection is changed, but the initial condition
     * is with no selection). The selected list entry (netlist
     * identifier) is used to initiate a new schematic display
     * and this window is then rendered invisible until needed again.
     * @param   e is the list selection event
     */
    public void valueChanged(ListSelectionEvent e) {
        int index = -1;
        if (!e.getValueIsAdjusting() && ((index=list.getSelectedIndex()) >= 0)) {
            String  ident = (String) listModel.getElementAt(index);
            NetListDraw.initiate(ident, Display.get_graphics());
            Display.refresh();
            frame.setVisible(false);
        }
    }
    
    /**
     * This static method is called to contruct a new identifier
     * list and make the list window visible. This method is only
     * called if there are alternative net identifiers from which
     * to select. If the 2nd parameter is non-null it is placed
     * at the top of the list.
     * @param   al is a list of identifiers, possibly empty
     * @param   s is non-null if the identifier string exactly
     *          matches one of the net identifiers
     */
    public static void displayList (ArrayList<String> al, String s) {
        listModel.clear();
        if (s != null)
            listModel.addElement(s);
        for (String ss: al)
            listModel.addElement(ss);
        frame.setVisible(true);
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        // Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        // Create and set up the window.
        frame = new JFrame("signal selection");
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        // Create and set up the content pane.
        JComponent newContentPane = new SigList();
        newContentPane.setOpaque(true); // content panes must be opaque
        frame.setContentPane(newContentPane);

        frame.pack();
        frame.setVisible(false);
    }

    /**
     * Create the pop-up net list window.
     */
    public static void sigList () {
        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
