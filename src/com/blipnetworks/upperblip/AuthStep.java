/* 
 * @(#)AuthStep.java
 * 
 * Copyright (c) 2006 by Blip Networks, Inc.
 * 239 Centre St, 3rd Floor
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

import org.pietschy.wizard.AbstractWizardStep;
import org.pietschy.wizard.WizardModel;
import org.pietschy.wizard.InvalidStateException;

/**
 * The first step in the upload process - takes a username and password from
 * the user and authenticates to the site.
 *
 * @author Jared Klett
 * @version $Id: AuthStep.java,v 1.15 2006/11/17 20:50:33 jklett Exp $
 */

public class AuthStep extends AbstractWizardStep {

// CVS info ////////////////////////////////////////////////////////////////////

	public static final String CVS_REV = "$Revision: 1.15 $";

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
	private JPanel view;
	/** */
	private UpperBlipModel model;
	/** */
	private JTextField userField;
	/** */
	private JPasswordField passField;
	/** */
	private JCheckBox remBox;

	private DocumentListener dl = new DocumentListener() {
		public void changedUpdate(DocumentEvent e) { /* ignore */ }
		public void insertUpdate(DocumentEvent e) {
			if (userField.getText() != null &&
                    !userField.getText().equals("") &&
                    passField.getPassword() != null &&
                    !(new String(passField.getPassword())).equals(""))
            {
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

		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel panel = new JPanel();
		panel.setLayout(gbl);
		// Set up a border
		panel.setBorder(
			BorderFactory.createCompoundBorder(
				BorderFactory.createCompoundBorder(
					BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
					//BorderFactory.createTitledBorder(files[i].getName()),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)
				),
				panel.getBorder()
			)
		);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets.top = 2;
		gbc.insets.bottom = 2;
		gbc.insets.left = 4;
		gbc.insets.right = 4;
		gbc.anchor = GridBagConstraints.EAST;
		gbl.setConstraints(userLabel, gbc);
		panel.add(userLabel);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbl.setConstraints(userField, gbc);
		panel.add(userField);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.EAST;
		gbl.setConstraints(passLabel, gbc);
		panel.add(passLabel);

		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbl.setConstraints(passField, gbc);
		panel.add(passField);

		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.EAST;
		gbl.setConstraints(remBox, gbc);
		panel.add(remBox);

		view = new JPanel();
		//view.setLayout(new BorderLayout());
		view.add(panel);
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
        AuthDialog ad = new AuthDialog(userField.getText(), new String(passField.getPassword()));
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

	public Dimension getPreferredSize() {
		return view.getPreferredSize();
	}

} // class AuthStep
