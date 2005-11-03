/* 
 * @(#)Main.java
 * 
 * Copyright (c) 2005 by Pokkari, Inc.
 * 117 West 25th St, Floor 2
 * New York, NY 10001
 * All rights reserved.
 *
 * This software is the confidential and
 * proprietary information of Pokkari, Inc.
 */

package com.pokkari.blip.upper;

import java.awt.*;
import java.awt.event.*;
import java.util.prefs.*;

import javax.swing.*;

import org.pietschy.wizard.*;
import org.pietschy.wizard.models.*;

/**
 * The main application class for the UpperBlip app.
 * 
 * @author Jared Klett
 * @version $Id: Main.java,v 1.1 2005/11/03 18:57:23 jklett Exp $
 */

public class Main {

// CVS info ////////////////////////////////////////////////////////////////////

	public static final String CVS_REV = "$Revision: 1.1 $";

// Static variables ////////////////////////////////////////////////////////////

	private static final String PREFS_NODE = "com.pokkari.blip.upper";

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

	private static Main m;

// Enumerated types ////////////////////////////////////////////////////////////

	// Enums, if any, go here

// Instance variables //////////////////////////////////////////////////////////

	private boolean noPrefs;
	private UpperBlipModel model;
	private Preferences prefs;
	private JFrame frame;

	private WizardListener wl = new WizardListener() {
		public void wizardCancelled(WizardEvent e) {
			//System.out.println("Wizard cancelled");

			int choice = JOptionPane.showConfirmDialog(
				Main.getMainInstance().getMainFrame(),
				"Canceling will exit this application.\nAre you sure you want to cancel?",
				"Please confirm cancel",
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


	/** An initialized but not running thread for when the JVM exits. */
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
		prefs = Preferences.userRoot().node(PREFS_NODE);
		int x = prefs.getInt(X_KEY, 0);
		int y = prefs.getInt(Y_KEY, 0);
		int w = prefs.getInt(WIDTH_KEY, 0);
		int h = prefs.getInt(HEIGHT_KEY, 0);
		String username = prefs.get(USER_KEY, null);
		String password = prefs.get(PASS_KEY, null);
		boolean remembered = Boolean.valueOf(prefs.get(REM_KEY, null)).booleanValue();

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
		frame = new JFrame("UpperBlip");
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(wizard, BorderLayout.CENTER);
		if (x == 0 && y == 0 && w == 0 && h == 0) {
			frame.pack();
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			frame.setSize((int)(d.width / 1.75), (int)(d.height / 1.75));
			frame.setLocationRelativeTo(null);
		} else {
			frame.setBounds(x, y, w, h);
		}
		frame.show();
		//wizard.showInFrame("UpperBlip");
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

// Main method ////////////////////////////////////////////////////////////////

	public static void main(String[] args) {
		m = new Main();
	}

} // class Main
