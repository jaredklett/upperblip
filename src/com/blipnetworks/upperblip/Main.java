/* 
 * @(#)Main.java
 * 
 * Copyright (c) 2005-2009 by Blip Networks, Inc.
 * 407 Broome St., 5th Floor
 * New York, NY 10013
 * All rights reserved.
 *
 * This software is the confidential and
 * proprietary information of Blip Networks, Inc.
 */

package com.blipnetworks.upperblip;

import java.awt.event.*;
import java.io.*;
import java.util.Properties;
import java.util.prefs.*;
import java.net.URL;

import javax.swing.*;

import com.blipnetworks.util.I18n;
import com.blipnetworks.util.BuildNumber;
import com.blipnetworks.util.Parameters;
import	com.blipnetworks.upperblip.wizard.*;
import edu.stanford.ejalbert.BrowserLauncher;

/**
 * The main application class for the UpperBlip app.
 *
 * @author Jared Klett
 * @version $Id: Main.java,v 1.31 2009/07/26 13:07:31 dsk Exp $
 */

public class Main implements Thread.UncaughtExceptionHandler {

// CVS info ////////////////////////////////////////////////////////////////////

    public static final String CVS_REV = "$Revision: 1.31 $";

// Static variables ////////////////////////////////////////////////////////////

    // TODO: cleanup
    private static final String BUILD_NUMBER_URI = "/upperblip.build";
    private static final String PREFS_NODE = "com.blipnetworks.upperblip";
    //private static final String PREFS_DIR = "/Library/Preferences/";
    //private static final String PROPS_FILE = "com.blipnetworks.upperblip.properties";
    //private static final String PREFS_PROPERTIES = System.getProperty("user.home") + PREFS_DIR + PROPS_FILE;
    //private static final String PREFS_NAME = "UpperBlip preferences";
    private static final String CANCEL_TITLE_KEY = "main.cancel.title";
    private static final String CANCEL_TEXT_KEY = "main.cancel.text";
    private static final String UPDATE_TITLE_KEY = "main.update.title";
    private static final String UPDATE_TEXT_KEY = "main.update.text";
    private static final String FRAME_TITLE_KEY = "main.frame.title";

    private static final String	ABORT_TEXT = "main.abort.text";
    
    //private static boolean macintosh = System.getProperty("os.name").equals("Mac OS X");
    public static final String APP_PROPERTIES = "upperblip.properties";
    public static final String PROPERTY_BASE_URL = "base.url";
    public static final String PROPERTY_USER_AGENT = "user.agent";

    /** blah */
    private static final String USER_KEY = "user";
    /** blah */
    private static final String PASS_KEY = "pass";
    /** blah */
    private static final String REM_KEY = "pass.rem";

    private static Main m;

    public static Properties 	appProperties = new Properties();
    public static String 		userAgent;

// Enumerated types ////////////////////////////////////////////////////////////

    // Enums, if any, go here

// Instance variables //////////////////////////////////////////////////////////

    private UpperBlipModel 	model;
    private Preferences 	prefs;
    //private Properties 		props;
    private Wizard 			wizard;
    private MovementHandler	mover = null;

// GUI elements ///////////////////////////////////////////////////////////////

    private JFrame frame;

// Event handlers /////////////////////////////////////////////////////////////

    private WindowHandler wndl = new WindowHandler();
    
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
    	InputStream	upperblipProps = Main.class.getClassLoader().getResourceAsStream(APP_PROPERTIES);
    	if (upperblipProps != null) {
            try {
                appProperties.load(upperblipProps);
            } catch (IOException e) {
            	abortApplication();
            }    		
    	}
    	else {
    		abortApplication();
    	}
    	
    	InputStream	bliplibProps = Main.class.getClassLoader().getResourceAsStream(Parameters.BLIPLIB_PROPERTIES);
    	if (bliplibProps != null) {
            try {
                Parameters.loadConfig(Main.class.getClassLoader().getResourceAsStream(Parameters.BLIPLIB_PROPERTIES));
                userAgent = Parameters.config.getProperty(PROPERTY_USER_AGENT) + " (" + System.getProperty("os.name") + " " 
                				+ System.getProperty("os.version") + "; http://blip.tv)";
            } catch (IOException e) {
            	abortApplication();
            }    		
    	}
    	else {
    		abortApplication();
    	}
    	
    	Thread.currentThread().setUncaughtExceptionHandler(this);
    	
        // Set system properties for Mac LAF guidelines
        //System.setProperty("apple.laf.useScreenMenuBar", "true");
        // Load prefs
        String username = null;
        String password =  null;
        boolean remembered = false;
        // The Java Preferences API doesn't work when run through an application
        // bundle on Mac OS X 10.4.3 with the latest Java 1.4.2_09 JVM.
        // It works fine run from a straight up jar.
        // So here's a workaround, using the trusty Properties object.
        prefs = Preferences.userRoot().node(PREFS_NODE);
        username = prefs.get(USER_KEY, null);
        password = prefs.get(PASS_KEY, null);
        remembered = prefs.getBoolean(REM_KEY, false);
        
        model = new UpperBlipModel();
        model.setLastVisible(false);
        model.setUsername(username);
        model.setPassword(password);
        model.setRemembered(remembered);
        model.add(new AuthStep());
        model.add(new FileDropStep());
        model.add(new MetaDataStep());
        model.add(new UploadStep());
        
        wizard = new Wizard(model);
        wizard.setDefaultExitMode(Wizard.EXIT_ON_FINISH);
        wizard.addWizardListener(wizl);
        
        frame = new JFrame(I18n.getString(FRAME_TITLE_KEY));
        mover = new MovementHandler(frame);
        
        wizard.setFrame(frame);
        
        frame.addWindowListener(wndl);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setIconImage(Icons.frameIcon.getImage());
        frame.setResizable(false);        
        frame.setContentPane(wizard);
        
        // Check for new version
        try {
            BuildNumber remoteBuild = BuildNumber.loadRemote(new URL(Parameters.config.getProperty(PROPERTY_BASE_URL) + BUILD_NUMBER_URI));
            BuildNumber localBuild = BuildNumber.loadLocal();
            if (remoteBuild.getBuildNumber() > localBuild.getBuildNumber()) {
                int choice = JOptionPane.showConfirmDialog(frame, I18n.getString(UPDATE_TEXT_KEY), I18n.getString(UPDATE_TITLE_KEY),
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (choice == JOptionPane.YES_OPTION) {
                    BrowserLauncher bl = new BrowserLauncher(null);
                    bl.openURLinBrowser(Parameters.config.getProperty(PROPERTY_BASE_URL) + "/tools");
                }
            }
        } catch (Exception e) { /* ignored */ }
        
        frame.pack();
        mover.positionFrame(true);
        frame.setVisible(true);
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

// Class methods //////////////////////////////////////////////////////////////

    public static Main getMainInstance() {
        return m;
    }

// Instance methods ///////////////////////////////////////////////////////////

    /**
     * There are many exceptions that can occur rather deep in the code structure,
     * and it is difficult to handle these exceptions at that level. These uncaught
     * exceptions are handled here and reported to the customer and blip.tv.
     */
    public void uncaughtException(Thread t, Throwable e) {
    	// Don't care which thread.
    	ExceptionReporter	app = new ExceptionReporter(e.toString(), e.getStackTrace());
    	Thread	thread = new Thread(app);
    	thread.start();
    	abortApplication();
    }
    
    public JFrame getMainFrame() {
        return frame;
    }

    public MovementHandler getMover() {
    	return mover;
    }
    
    public void abortApplication(Exception e) {
    	ExceptionReporter	app = new ExceptionReporter(e.toString(), e.getStackTrace());
    	Thread	thread = new Thread(app);
    	thread.start();
    	abortApplication();
    }
        
    public void abortApplication() {
    	JOptionPane.showMessageDialog(null, I18n.getString(ABORT_TEXT));
    	savePrefs();
    	System.exit(0);
    }
    
    private void savePrefs() {
    	
        if (model != null) {
            if (model.getUsername() != null) {
                prefs.put(USER_KEY, model.getUsername());
                if (model.isRemembered()) {
                    prefs.put(PASS_KEY, model.getPassword());
                }
                prefs.putBoolean(REM_KEY, model.isRemembered());
            }
            try {
				prefs.flush();
			} catch (BackingStoreException e) {
				// Ignore exception
			}
        }
    }

// Main method ////////////////////////////////////////////////////////////////

    public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createApplication();
			}
		});
    }

    private static void createApplication() {
    	m = new Main();
    }
    
    private class WindowHandler extends WindowAdapter {
        public void windowClosing(WindowEvent windowEvent) {
        	wizard.close(); 
        }
    }
    
} // class Main
