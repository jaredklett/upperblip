/* 
 * @(#)AuthStep.java
 * 
 * Copyright (c) 2006 by Blip Networks, Inc.
 * 117 West 25th St, Floor 2
 * New York, NY 10001
 * All rights reserved.
 *
 * This software is the confidential and
 * proprietary information of Blip Networks, Inc.
 */

package com.blipnetworks.blip.upper;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import com.pokkari.blip.util.I18n;

import org.pietschy.wizard.AbstractWizardStep;
import org.pietschy.wizard.WizardModel;

/**
 * 
 * 
 * @author Jared Klett
 * @version $Id: AuthStep.java,v 1.5 2006/05/06 23:56:46 jklett Exp $
 */

public class AuthStep extends AbstractWizardStep {

// CVS info ////////////////////////////////////////////////////////////////////

	public static final String CVS_REV = "$Revision: 1.5 $";

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
			if (userField.getText() != null && !userField.getText().equals("") && passField.getPassword() != null && !(new String(passField.getPassword())).equals("")) {
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
		//setComplete(true);
	}

	public void init(WizardModel model) {
		this.model = (UpperBlipModel)model;
		userField.setText(this.model.getUsername());
		if (this.model.isRemembered()) {
			passField.setText(this.model.getPassword());
			remBox.setSelected(true);
			//setComplete(true);
		}
		userField.getDocument().addDocumentListener(dl);
		passField.getDocument().addDocumentListener(dl);
	}

	public void prepare() {
		setView(view);
		setComplete(remBox.isSelected());
	}

	public void applyState() {
		// this is called when the user clicks "Next"
		model.setUsername(userField.getText());
		model.setPassword(new String(passField.getPassword()));
		model.setRemembered(remBox.isSelected());
	}

	public Dimension getPreferredSize() {
		return view.getPreferredSize();
	}

} // class AuthStep
