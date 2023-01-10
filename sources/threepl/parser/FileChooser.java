package threepl.parser;

import static threepl.ThreePL.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.concurrent.Semaphore;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import threepl.ThreePL;
import threepl.exec.Val;

@SuppressWarnings("serial")
public class FileChooser extends JPanel implements ActionListener {
    protected           JPanel          fileopen;
    protected           JButton         openButton;
    protected           JPanel          definespanel;
    protected           JPanel          buttons;
    protected           JButton         execButton;
    protected           JButton         exitButton;
    protected           JCheckBox       report;
    protected           JCheckBox       tde;
    protected           JCheckBox       net;
    protected           JCheckBox       force;
    protected           JCheckBox       skip;
    protected           JTextArea       filetext;
    protected           JTextArea       dirtext;
    protected           JTextField      defpantext;
    protected           JTextField      deftext;
    public static       JTextArea       logtext;
    protected           JScrollPane     scrollpane;
    protected           JFileChooser    fc;
    protected           mainThread      mt = new mainThread();
    protected           mainWaitThread  mwt = new mainWaitThread();
    protected static    Semaphore       mainWait = new Semaphore(0, true);
    protected static    Semaphore       finishWait = new Semaphore(0, true);
    
    /**
     * Filter class to restrict files to those with .3pl extension.
     */
    private class threeplFilter extends FileFilter {
        public boolean accept (File f) {
            if (f.isDirectory())
                return(true);
            String  name = f.getName();
            if (name.length() < 5)
                return(false);
            String  extension = name.substring(name.length()-3);
            return(extension.equals("3pl"));
        }
        
        public String getDescription () {
            return(null);
        }
    }
    
    /**
     * Thread class for running mainContinue() repeatedly.
     */
    private class mainThread implements Runnable {
        public void run () {
            for (;;) {
                try {
                    mainWait.acquire();
                } catch (InterruptedException ee) {}
                try {
                    ThreePL.mainContinue();
                } catch(Exception ee) {}
                finishWait.release();
            }
        }
    }
    
    /**
     * Thread class to wait for completion of mainContinue() and append
     * text to logtext.
     */
    private class mainWaitThread implements Runnable {
        public void run () {
            for (;;) {
                try {
                    finishWait.acquire();
                } catch (InterruptedException ee) {}
                logtext.append(ThreePL.msgsb.toString());
                logtext.append("execution completed\n");
                logtext.setCaretPosition(logtext.getDocument().getLength());
            }
        }
    }
    
    public FileChooser () {
        /*
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {}
          catch (InstantiationException ee) {}
          catch (IllegalAccessException ee) {}
          catch (javax.swing.UnsupportedLookAndFeelException ee) {}
        */
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        //Create a file chooser
        fc = new JFileChooser(ThreePL.current_directory);
        fc.setAcceptAllFileFilterUsed(false);
        fc.addChoosableFileFilter(new threeplFilter());

        //fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        //fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        openButton = new JButton("Open input File");
        openButton.addActionListener(this);
        execButton = new JButton("execute");
        execButton.addActionListener(this);
        exitButton = new JButton("exit");
        exitButton.addActionListener(this);
        force = new JCheckBox("ignore file ages");
        skip = new JCheckBox("skip post-processing");
        report = new JCheckBox("generate report file (name.rpt)");
        exitButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        tde = new JCheckBox("generate intermediate code list (name.icl)");
        tde.setAlignmentX(Component.LEFT_ALIGNMENT);
        net = new JCheckBox("generate a listing of nets (name.net)");
        net.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        filetext = new JTextArea(1, 30);
        filetext.setMargin(new Insets(5, 5, 5, 5));
        filetext.setEditable(false);
        filetext.setAlignmentX(Component.LEFT_ALIGNMENT);
        filetext.setForeground(Color.MAGENTA);
        
        dirtext = new JTextArea(2, 30);
        dirtext.setMargin(new Insets(5, 5, 5, 5));
        dirtext.setEditable(false);
        dirtext.setAlignmentX(Component.LEFT_ALIGNMENT);
        dirtext.setForeground(Color.MAGENTA);
                
        defpantext = new JTextField("Definitions");
        defpantext.setEditable(false);
        
        deftext = new JTextField(30);
        deftext.addActionListener(this);
        deftext.setForeground(Color.BLUE);
        deftext.setEditable(true);
        
        logtext = new JTextArea(20, 90);
        logtext.setMargin(new Insets(5, 5, 5, 5));
        logtext.setEditable(false);
        logtext.setAlignmentX(Component.LEFT_ALIGNMENT);
        logtext.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        logtext.setForeground(Color.BLUE);
        scrollpane = new JScrollPane(logtext);
        scrollpane.setAlignmentX(Component.LEFT_ALIGNMENT);

        fileopen = new JPanel();
        fileopen.setLayout(new BoxLayout(fileopen, BoxLayout.LINE_AXIS));
        fileopen.setAlignmentX(Component.LEFT_ALIGNMENT);
        fileopen.add(openButton);
        fileopen.add(filetext);
        
        definespanel = new JPanel();
        definespanel.setLayout(new BoxLayout(definespanel, BoxLayout.LINE_AXIS));
        definespanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        definespanel.add(defpantext);
        definespanel.add(deftext);
        
        buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.LINE_AXIS));
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttons.add(execButton);
        buttons.add(exitButton);

        add(fileopen);
        add(dirtext);
        add(definespanel);
        add(skip);
        add(force);
        add(report);
        add(tde);
        add(net);
        add(buttons);
        add(scrollpane);

        new Thread(mt).start();
        new Thread(mwt).start();

        /*
         * Don't bother listing these.
         * 
        logtext.append("version = " + version + "\n");
        logtext.append("revision = " + revision + "\n");
        logtext.append("builtBy = " + builtBy + "\n");
        logtext.append("buildDate = " + buildDate + "\n");
        logtext.append("lastChangedRev = " + lastChangedRev + "\n");
        logtext.append("lastChangedAuthor = " + lastChangedAuthor + "\n");
        logtext.append("lastChangedDate = " + lastChangedDate + "\n");
        */
    }

    public void actionPerformed (ActionEvent e) {
        if (e.getSource() == openButton) {
            int returnVal = fc.showOpenDialog(FileChooser.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                filetext.setText(null);
                filetext.append(file.getName());
                try {
                    ThreePL.source_file = file.getCanonicalPath();
                } catch (java.io.IOException ee) {}
                dirtext.setText(null);
                dirtext.append(ThreePL.parent_directory);
            }
        } else if (e.getSource() == execButton) {
            if (skip.isSelected())
                fc_skip_post_processing = !fc_skip_post_processing;
            if (force.isSelected())
                fc_force_execution = !fc_force_execution;
            if (report.isSelected())
                fc_report = !fc_report;
            if (tde.isSelected())
                fc_list_tdes = !fc_list_tdes;
            if (net.isSelected())
                fc_list_nets = !fc_list_nets;
            logtext.setText(null);
            logtext.append("executing\n");
            mainWait.release();
        } else if (e.getSource() == exitButton)
            System.exit(0);
        else if (e.getSource() == deftext) {
            String      opts = deftext.getText();
            String[]    optarray = opts.split(" ");
            int         i;
            
            for (i=0 ; i<optarray.length ; i++) {
                String[]    opt = optarray[i].split("=");
                String  dname = null;
                String  dval = null;
                
                switch (opt.length) {
                case 1:
                    dname = opt[0].trim();
                    dval = "true";
                    break;
                case 2:
                    dname = opt[0].trim();
                    dval = opt[1].trim();
                    break;
                default:
                    deftext.setForeground(Color.RED);
                    return;
                }

                try {
                    long l = Long.parseLong(dval);
                    ThreePL.addDir(dname, new Val(l, null), null);
                } catch (NumberFormatException el) {
                    try {
                        double d = Double.parseDouble(dval);
                        ThreePL.addDir(dname, new Val(d, null), null);
                    } catch (NumberFormatException ed) {
                        if (dval.equals("true"))
                            ThreePL.addDir(dname, new Val(Boolean.valueOf(true), null), null);
                        else if (dval.equals("false"))
                            ThreePL.addDir(dname, new Val(Boolean.valueOf(false), null), null);
                        else {
                            dval = dval.replaceAll("\"", "");
                            ThreePL.addDir(dname, new Val(dval, null), null);
                        }
                    }
                }
                
            }
            deftext.setForeground(Color.BLUE);
        }
    }

    /**
     * Create the GUI and show it.
     */
    private static void createAndShowGUI () {
        //Create and set up the window.
        JFrame frame = new JFrame("3PL " + version + " - Parallel Pipeline Programming Language");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add content to the window.
        frame.add(new FileChooser());

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Create the display window.
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
    }
}
