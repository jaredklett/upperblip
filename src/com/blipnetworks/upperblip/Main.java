/* 
 * @(#)Main.java
 * 
 * Copyright (c) 2006 by Pokkari, Inc.
 * 117 West 25th St, Floor 2
 * New York, NY 10001
 * All rights reserved.
 *
 * This software is the confidential and
 * proprietary information of Pokkari, Inc.
 */

package com.pokkari.blip.upper;

import java.awt.*;
import java.io.*;
import java.util.Properties;
import java.util.prefs.*;

import javax.swing.*;

import com.pokkari.blip.util.I18n;
import org.pietschy.wizard.WizardListener;
import org.pietschy.wizard.WizardEvent;
import org.pietschy.wizard.Wizard;

/**
 * The main application class for the UpperBlip app.
 *
 * @author Jared Klett
 * @version $Id: Main.java,v 1.6 2006/03/17 21:36:19 jklett Exp $
 */

public class Main {

// CVS info ////////////////////////////////////////////////////////////////////

    public static final String CVS_REV = "$Revision: 1.6 $";

// Static variables ////////////////////////////////////////////////////////////

    // TODO: cleanup
    private static final String PREFS_NODE = "com.pokkari.blip.upper";
    private static final String PREFS_DIR = "/Library/Preferences/";
    private static final String PROPS_FILE = "com.pokkari.UpperBlip.properties";
    private static final String PREFS_PROPERTIES = System.getProperty("user.home") + PREFS_DIR + PROPS_FILE;
    private static final String PREFS_NAME = "UpperBlip preferences";
    private static final String CANCEL_TITLE_KEY = "main.cancel.title";
    private static final String CANCEL_TEXT_KEY = "main.cancel.text";
    private static final String FRAME_TITLE_KEY = "main.frame.title";

    private static boolean macintosh = System.getProperty("os.name").equals("Mac OS X");
    public static final String APP_PROPERTIES = "com-pokkari-blip-upper.properties";

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
            appProperties.load(ClassLoader.getSystemResource(APP_PROPERTIES).openStream());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

// Enumerated types ////////////////////////////////////////////////////////////

    // Enums, if any, go here

// Instance variables //////////////////////////////////////////////////////////

    private UpperBlipModel model;
    private Preferences prefs;
    private Properties props;

// GUI elements ///////////////////////////////////////////////////////////////

    private JFrame frame;

// Event handlers /////////////////////////////////////////////////////////////

    private WizardListener wl = new WizardListener() {
        public void wizardCancelled(WizardEvent e) {
            //System.out.println("Wizard cancelled");

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
        model.add(new FileDropStep());
        model.add(new MetaDataStep());
        model.add(new AuthStep());
        model.add(new UploadStep());
        Wizard wizard = new Wizard(model);
        wizard.setDefaultExitMode(Wizard.EXIT_ON_FINISH);
        wizard.addWizardListener(wl);
        frame = new JFrame(I18n.getString(FRAME_TITLE_KEY));
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
        // TODO: what should I use here instead?
        frame.show();
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
