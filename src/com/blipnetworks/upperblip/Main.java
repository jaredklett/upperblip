/* 
 * @(#)Main.java
 * 
 * Copyright (c) 2005-2007 by Blip Networks, Inc.
 * 239 Centre St, 3rd Floor
 * New York, NY 10013
 * All rights reserved.
 *
 * This software is the confidential and
 * proprietary information of Blip Networks, Inc.
 */

package com.blipnetworks.upperblip;

import java.awt.*;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.Properties;
import java.util.prefs.*;
import java.net.URL;

import javax.swing.*;

import com.blipnetworks.util.I18n;
import com.blipnetworks.util.BuildNumber;
import com.blipnetworks.util.Parameters;
import org.pietschy.wizard.WizardListener;
import org.pietschy.wizard.WizardEvent;
import org.pietschy.wizard.Wizard;
import edu.stanford.ejalbert.BrowserLauncher;

/**
 * The main application class for the UpperBlip app.
 *
 * @author Jared Klett
 * @version $Id: Main.java,v 1.28 2007/06/25 23:04:26 jklett Exp $
 */

public class Main {

// CVS info ////////////////////////////////////////////////////////////////////

    public static final String CVS_REV = "$Revision: 1.28 $";

// Static variables ////////////////////////////////////////////////////////////

    // TODO: cleanup
    private static final String BUILD_NUMBER_URI = "/upperblip.build";
    private static final String PREFS_NODE = "com.blipnetworks.upperblip";
    private static final String PREFS_DIR = "/Library/Preferences/";
    private static final String PROPS_FILE = "com.blipnetworks.upperblip.properties";
    private static final String PREFS_PROPERTIES = System.getProperty("user.home") + PREFS_DIR + PROPS_FILE;
    private static final String PREFS_NAME = "UpperBlip preferences";
    private static final String CANCEL_TITLE_KEY = "main.cancel.title";
    private static final String CANCEL_TEXT_KEY = "main.cancel.text";
    private static final String UPDATE_TITLE_KEY = "main.update.title";
    private static final String UPDATE_TEXT_KEY = "main.update.text";
    private static final String FRAME_TITLE_KEY = "main.frame.title";

    // TODO: fix
    public static final String UA = "UpperBlip/1.4.1 (" + System.getProperty("os.name") + " " + System.getProperty("os.version") + "; http://blip.tv)";
    private static boolean macintosh = System.getProperty("os.name").equals("Mac OS X");
    public static final String APP_PROPERTIES = "upperblip.properties";
    public static final String PROPERTY_BASE_URL = "base.url";

    /** blah */
    private static final String X_KEY = "x.pos";
    /** blah */
    private static final String Y_KEY = "y.pos";
    /** blah */
    private static final String WIDTH_KEY = "width";
    /** blah */
    private static final String HEIGHT_KEY = "height";
    /** blah */
    private static final String USER_KEY = "user";
    /** blah */
    private static final String PASS_KEY = "pass";
    /** blah */
    private static final String REM_KEY = "pass.rem";
    /** blah */
    private static final String ZERO = "0";

    private static Main m;

    public static Properties appProperties = new Properties();

    static {
        try {
            appProperties.load(Main.class.getClassLoader().getResourceAsStream(APP_PROPERTIES));
        } catch (IOException e) {
            throw new IllegalStateException("Could not UpperBlip configuration: " + APP_PROPERTIES, e);
        }
        try {
            //Parameters.loadConfig();
            Parameters.loadConfig(Main.class.getClassLoader().getResourceAsStream(Parameters.BLIPLIB_PROPERTIES));
        } catch (IOException e) {
            throw new IllegalStateException("Could not BlipLib configuration: " + Parameters.BLIPLIB_PROPERTIES, e);
        }
    }

// Enumerated types ////////////////////////////////////////////////////////////

    // Enums, if any, go here

// Instance variables //////////////////////////////////////////////////////////

    private UpperBlipModel model;
    private Preferences prefs;
    private Properties props;
    private Wizard wizard;

// GUI elements ///////////////////////////////////////////////////////////////

    private JFrame frame;

// Event handlers /////////////////////////////////////////////////////////////

    private WindowListener wndl = new WindowListener() {
        public void windowClosing(WindowEvent windowEvent) { wizard.close(); }
        public void windowOpened(WindowEvent windowEvent) { /* does nothing */ }
        public void windowIconified(WindowEvent windowEvent) { /* does nothing */ }
        public void windowDeiconified(WindowEvent windowEvent) { /* does nothing */ }
        public void windowActivated(WindowEvent windowEvent) { /* does nothing */ }
        public void windowDeactivated(WindowEvent windowEvent) { /* does nothing */ }
        public void windowClosed(WindowEvent windowEvent) { /* does nothing */ }
    };

    private WizardListener wizl = new WizardListener() {
        public void wizardCancelled(WizardEvent e) {
            int choice = JOptionPane.showConfirmDialog(
                    Main.getMainInstance().getMainFrame(),
                    I18n.getString(CANCEL_TEXT_KEY),
                    I18n.getString(CANCEL_TITLE_KEY),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (choice == JOptionPane.YES_OPTION) {
                frame.dispose();
                System.exit(0);
            }
        }

        public void wizardClosed(WizardEvent e) {
            frame.dispose();
            System.exit(0);
        }
    };

    /**
     * An initialized but not running thread for when the JVM exits.
     */
    private Thread shutdownHook = new Thread("Main.shutdownHook") {
        public void run() {
            savePrefs();
        }
    };

// Constructor /////////////////////////////////////////////////////////////////

    private Main() {
        // Set system properties for Mac LAF guidelines
        //System.setProperty("apple.laf.useScreenMenuBar", "true");
        // Load prefs
        int x, y, w, h;
        String username;
        String password;
        boolean remembered;
        // The Java Preferences API doesn't work when run through an application
        // bundle on Mac OS X 10.4.3 with the latest Java 1.4.2_09 JVM.
        // It works fine run from a straight up jar.
        // So here's a workaround, using the trusty Properties object.
        if (macintosh) {
            props = new Properties();
            try {
                props.load(new FileInputStream(PREFS_PROPERTIES));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            x = Integer.parseInt(props.getProperty(X_KEY, ZERO));
            y = Integer.parseInt(props.getProperty(Y_KEY, ZERO));
            w = Integer.parseInt(props.getProperty(WIDTH_KEY, ZERO));
            h = Integer.parseInt(props.getProperty(HEIGHT_KEY, ZERO));
            username = props.getProperty(USER_KEY, null);
            password = props.getProperty(PASS_KEY, null);
            remembered = Boolean.valueOf(props.getProperty(REM_KEY, null)).booleanValue();
        } else {
            prefs = Preferences.userRoot().node(PREFS_NODE);
            x = prefs.getInt(X_KEY, 0);
            y = prefs.getInt(Y_KEY, 0);
            w = prefs.getInt(WIDTH_KEY, 0);
            h = prefs.getInt(HEIGHT_KEY, 0);
            username = prefs.get(USER_KEY, null);
            password = prefs.get(PASS_KEY, null);
            remembered = Boolean.valueOf(prefs.get(REM_KEY, null)).booleanValue();
        }
        model = new UpperBlipModel();
        model.setLastVisible(false);
        model.setUsername(username);
        model.setPassword(password);
        model.setRemembered(remembered);
        model.add(new AuthStep());
        model.add(new FileDropStep());
        model.add(new MetaDataStep());
        model.add(new UploadStep());
        model.add(new SummaryStep());
        wizard = new Wizard(model);
        wizard.setDefaultExitMode(Wizard.EXIT_ON_FINISH);
        wizard.addWizardListener(wizl);
        frame = new JFrame(I18n.getString(FRAME_TITLE_KEY));
        frame.addWindowListener(wndl);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setIconImage(Icons.frameIcon.getImage());
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(wizard, BorderLayout.CENTER);
        if (x == 0 && y == 0 && w == 0 && h == 0) {
            frame.pack();
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            frame.setSize((int) (d.width / 1.75), (int) (d.height / 1.75));
            frame.setLocationRelativeTo(null);
        } else {
            frame.setBounds(x, y, w, h);
        }
        // Check for new version
        try {
            BuildNumber remoteBuild = BuildNumber.loadRemote(new URL(Parameters.config.getProperty(PROPERTY_BASE_URL) + BUILD_NUMBER_URI));
            BuildNumber localBuild = BuildNumber.loadLocal();
            if (remoteBuild.getBuildNumber() > localBuild.getBuildNumber()) {
                int choice = JOptionPane.showConfirmDialog(
                        frame,
                        I18n.getString(UPDATE_TEXT_KEY),
                        I18n.getString(UPDATE_TITLE_KEY),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

                if (choice == JOptionPane.YES_OPTION) {
                    BrowserLauncher bl = new BrowserLauncher(null);
                    bl.openURLinBrowser(Parameters.config.getProperty(PROPERTY_BASE_URL) + "/tools");
                }
            }
        } catch (Exception e) { e.printStackTrace(); /* ignored */ }
        frame.setVisible(true);
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

// Class methods //////////////////////////////////////////////////////////////

    public static Main getMainInstance() {
        return m;
    }

// Instance methods ///////////////////////////////////////////////////////////

    public JFrame getMainFrame() {
        return frame;
    }

    private void savePrefs() {
        Rectangle r = frame.getBounds();
        if (macintosh) {
            props.setProperty(X_KEY, Integer.toString(r.x));
            props.setProperty(Y_KEY, Integer.toString(r.y));
            props.setProperty(WIDTH_KEY, Integer.toString(r.width));
            props.setProperty(HEIGHT_KEY, Integer.toString(r.height));
            if (model != null) {
                if (model.getUsername() != null) {
                    props.setProperty(USER_KEY, model.getUsername());
                    if (model.isRemembered())
                        props.setProperty(PASS_KEY, model.getPassword());
                    props.setProperty(REM_KEY, Boolean.toString(model.isRemembered()));
                }
            }
            try {
                props.store(new FileOutputStream(PREFS_PROPERTIES), PREFS_NAME);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            prefs.putInt(X_KEY, r.x);
            prefs.putInt(Y_KEY, r.y);
            prefs.putInt(WIDTH_KEY, r.width);
            prefs.putInt(HEIGHT_KEY, r.height);
            if (model != null) {
                if (model.getUsername() != null) {
                    prefs.put(USER_KEY, model.getUsername());
                    if (model.isRemembered())
                        prefs.put(PASS_KEY, model.getPassword());
                    prefs.put(REM_KEY, Boolean.toString(model.isRemembered()));
                }
            }
        }
    }

// Main method ////////////////////////////////////////////////////////////////

    public static void main(String[] args) {
        m = new Main();
    }

} // class Main
