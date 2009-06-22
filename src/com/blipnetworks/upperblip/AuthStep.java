/* 
 * @(#)AuthStep.java
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

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import com.blipnetworks.util.I18n;

import	com.blipnetworks.upperblip.wizard.*;

/**
 * The first step in the upload process - takes a username and password from
 * the user and authenticates to the site.
 *
 * @author Jared Klett
 * @version $Id: AuthStep.java,v 1.18 2009/06/22 21:07:45 jklett Exp $
 */

public class AuthStep extends AbstractWizardStep {

// CVS info ////////////////////////////////////////////////////////////////////

	public static final String CVS_REV = "$Revision: 1.18 $";

// Static variables ////////////////////////////////////////////////////////////

	/** blah */
	private static final String TITLE_KEY = "auth.title";
	/** blah */
	private static final String SUMMARY_KEY = "auth.summary";
    /** blah */
    private static final String USER_LABEL_KEY = "auth.user.label";
    /** blah */
    private static final String PASS_LABEL_KEY = "auth.pass.label";
    /** blah */
    private static final String BOX_LABEL_KEY = "auth.box.label";
    private static final String BAD_AUTH_KEY = "auth.bad";

// Enumerated types ////////////////////////////////////////////////////////////

	// Enums, if any, go here

// Instance variables //////////////////////////////////////////////////////////

	/** */
	private JPanel 			view;
	/** */
	private UpperBlipModel 	model;
	/** */
	private JTextField 		userField;
	/** */
	private JPasswordField	passField;
	/** */
	private JCheckBox 		remBox;

	private DocumentListener dl = new DocumentListener() {
		public void changedUpdate(DocumentEvent e) { /* ignore */ }
		
		public void insertUpdate(DocumentEvent e) {
			if (userField.getText() != null && !userField.getText().equals("") && passField.getPassword() != null &&
                    !(new String(passField.getPassword())).equals("")) {
				setComplete(true);
			}
		}
		
		public void removeUpdate(DocumentEvent e) {
			if (userField.getText() == null || userField.getText().equals("")) {
				setComplete(false);
			}
			if (passField.getPassword() == null || new String(passField.getPassword()).equals("")) {
				setComplete(false);
			}
		}
	};

// Constructor /////////////////////////////////////////////////////////////////

	public AuthStep() {
		super(I18n.getString(TITLE_KEY), I18n.getString(SUMMARY_KEY));
		
        setIcon(Icons.authIcon);

		// Create and layout components
		JLabel userLabel = new JLabel(I18n.getString(USER_LABEL_KEY));
		JLabel passLabel = new JLabel(I18n.getString(PASS_LABEL_KEY));
		userField = new JTextField(15);
		passField = new JPasswordField(15);
		remBox = new JCheckBox(I18n.getString(BOX_LABEL_KEY));

		view = new JPanel();
		view.setName("AuthStep");
		Grid	grid = new Grid();
		
		view.setLayout(new GridBagLayout());
		
		// Set up a border
		view.setBorder(
			BorderFactory.createCompoundBorder(
				BorderFactory.createCompoundBorder(
					BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
					//BorderFactory.createTitledBorder(files[i].getName()),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)
				),
				view.getBorder()
			)
		);

		grid.setInsets(2, 4, 2, 4);
		grid.setAnchor(Grid.eastAnchor);
		view.add(userLabel, grid);

		view.add(userField, grid.setAnchorIncrX(Grid.westAnchor));

		grid.setX(0);
		view.add(passLabel, grid.setAnchorIncrY(Grid.eastAnchor));

		view.add(passField, grid.setAnchorIncrX(Grid.westAnchor));

		grid.setX(0);
		grid.setColumnSpan(2);
		view.add(remBox, grid.setAnchorIncrY(Grid.eastAnchor));
	}

	public void init(WizardModel model) {
		this.model = (UpperBlipModel)model;
		userField.setText(this.model.getUsername());
		
		if (this.model.isRemembered()) {
			passField.setText(this.model.getPassword());
			remBox.setSelected(true);
		}
		
		userField.getDocument().addDocumentListener(dl);
		passField.getDocument().addDocumentListener(dl);
	}

	public void prepare() {
		setView(view);
		setComplete(remBox.isSelected());
	}

	public void applyState() throws InvalidStateException {
		// this is called when the user clicks "Next"
        setBusy(true);
        AuthDialog ad = new AuthDialog(userField.getText(), new String(passField.getPassword()), model);
        ad.pack();
        Dimension d = ad.getSize();
        ad.setSize(d.width * 2, d.height);
        ad.setLocationRelativeTo(Main.getMainInstance().getMainFrame());
        Thread thread = new Thread(ad);
        thread.start();
        ad.setVisible(true);
        setBusy(false);
        if (!ad.wasSuccessful()) {
            setComplete(false);
            // TODO: better message here
            throw new InvalidStateException(I18n.getString(BAD_AUTH_KEY));
        }
        setComplete(true);
        model.setUsername(userField.getText());
		model.setPassword(new String(passField.getPassword()));
		model.setRemembered(remBox.isSelected());
	}

	public Component getView() {
		return view;
	}
	
	public Dimension getPreferredSize() {
		return view.getPreferredSize();
	}

} // class AuthStep
