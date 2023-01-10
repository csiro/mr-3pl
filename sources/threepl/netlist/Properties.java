package threepl.netlist;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * This class provides a pop-up list of net or element properties
 * if any.
 */
@SuppressWarnings("serial")
public class Properties extends JPanel {
    private static JFrame       frame;
    private static JTextArea    text;
    public static final int     rows = 10;
    public static final int     columns = 60;

    /**
     * Create the signal list window.
     */
    public Properties() {
        super(new BorderLayout());
        text = new JTextArea(rows, columns);        
        JScrollPane textScrollPane = new JScrollPane(text);
        add(textScrollPane, BorderLayout.CENTER);
    }
    
    /**
     * This static method is called to display new text.
     * @param   s is an array of 2 strings. The first member is the
     *          element name followed by the element identifier.
     *          The second member is the text giving the properties.
     */
    public static void displayText (String[] s) {
        frame.setTitle(s[0]);
        text.setText(s[1]);
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
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        // Create and set up the content pane.
        JComponent newContentPane = new Properties();
        newContentPane.setOpaque(true);
        frame.setContentPane(newContentPane);

        frame.pack();
        frame.setVisible(false);
    }

    /**
     * Create the pop-up net list window.
     */
    public static void propertiesText () {
        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
