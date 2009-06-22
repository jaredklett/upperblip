/*
 * @(#)AuthDialog.java
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

import com.blipnetworks.util.Authenticator;
import com.blipnetworks.util.MetadataLoader;
import com.blipnetworks.util.I18n;

import javax.swing.*;
import java.awt.*;

import org.apache.commons.httpclient.Cookie;

/**
 *
 *
 * @author Jared Klett
 * @version $Id: AuthDialog.java,v 1.7 2009/06/22 21:07:45 jklett Exp $
 */

@SuppressWarnings("serial")
public class AuthDialog extends JDialog implements Runnable {

// CVS info ///////////////////////////////////////////////////////////////////

    public static final String CVS_REV = "$Revision: 1.7 $";

// UI elements ////////////////////////////////////////////////////////////////

    private JLabel			infoLabel = new JLabel(I18n.getString(LOGIN_LABEL_KEY));
    private JProgressBar	progress = new JProgressBar();

// Static variables ////////////////////////////////////////////////////////////

    /** blah */
    private static final String TITLE_KEY = "authdialog.title";
    private static final String PANEL_TITLE_KEY = "authdialog.panel.title";
    private static final String LOGIN_LABEL_KEY = "authdialog.login.label";
    private static final String DATA_LABEL_KEY = "authdialog.data.label";

// Instance variables /////////////////////////////////////////////////////////

    private boolean			success;
    private String 			username;
    private String 			password;
    private UpperBlipModel 	model;

// Constructors ///////////////////////////////////////////////////////////////

    /**
     *
     * @param username
     * @param password
     */
    public AuthDialog(String username, String password, UpperBlipModel model) {
        super(Main.getMainInstance().getMainFrame(), I18n.getString(TITLE_KEY), true);
        
        setUsername(username);
        setPassword(password);
        setModel(model);
        progress.setIndeterminate(true);
        
        JPanel panel = new JPanel();
        
        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createTitledBorder(I18n.getString(PANEL_TITLE_KEY)),
                                BorderFactory.createEmptyBorder(5, 5, 5, 5)
                        ),
                        panel.getBorder()
                )
        );
        
        panel.setLayout(new GridLayout(2, 1));
        panel.add(infoLabel);
        panel.add(progress);
        add(panel);
    }

// Runnable implementation ////////////////////////////////////////////////////

    public void run() {
        while (!this.isVisible()) {
            try { Thread.sleep(10); } catch (Exception e) { /* ignore */ }
        }
        
        // 1. run authentication
        try {
            Cookie authCookie = Authenticator.authenticate(username, password);
            success = authCookie != null;
            if (success) {
                model.setAuthCookie(authCookie);
            }
        }
        catch (Exception e) {
            success = false;
        }
        
        if (success) {
            infoLabel.setText(I18n.getString(DATA_LABEL_KEY));
            // 2. load metadata/user data
            try {
                MetadataLoader.load(model.getAuthCookie());
            } catch (Exception e) {
                throw new IllegalStateException("Could not load metadata!");
            }
        }
        setVisible(false);
    }

// Accessors //////////////////////////////////////////////////////////////////

    public boolean wasSuccessful() {
        return success;
    }

// Mutators ///////////////////////////////////////////////////////////////////

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setModel(UpperBlipModel model) {
        this.model = model;
    }

} // class AuthDialog
