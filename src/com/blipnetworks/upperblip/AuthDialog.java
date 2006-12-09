/*
 * @(#)AuthDialog.java
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

import com.blipnetworks.util.Authenticator;
import com.blipnetworks.util.MetadataLoader;
import com.blipnetworks.util.I18n;

import javax.swing.*;
import java.awt.*;

/**
 *
 *
 * @author Jared Klett
 * @version $Id: AuthDialog.java,v 1.3 2006/12/09 22:17:02 jklett Exp $
 */

public class AuthDialog extends JDialog implements Runnable {

// CVS info ///////////////////////////////////////////////////////////////////

    public static final String CVS_REV = "$Revision: 1.3 $";

// UI elements ////////////////////////////////////////////////////////////////

    private JLabel infoLabel = new JLabel(I18n.getString(LOGIN_LABEL_KEY));
    private JProgressBar progress = new JProgressBar();

// Static variables ////////////////////////////////////////////////////////////

    /** blah */
    private static final String TITLE_KEY = "authdialog.title";
    private static final String PANEL_TITLE_KEY = "authdialog.panel.title";
    private static final String LOGIN_LABEL_KEY = "authdialog.login.label";
    private static final String DATA_LABEL_KEY = "authdialog.data.label";

// Instance variables /////////////////////////////////////////////////////////

    private boolean success;
    private String username;
    private String password;

// Constructors ///////////////////////////////////////////////////////////////

    /**
     *
     * @param username
     * @param password
     */
    public AuthDialog(String username, String password) {
        super(Main.getMainInstance().getMainFrame(), I18n.getString(TITLE_KEY), true);
        setUsername(username);
        setPassword(password);
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
        getContentPane().add(panel);
    }

// Runnable implementation ////////////////////////////////////////////////////

    public void run() {
        while (!this.isVisible()) {
            try { Thread.sleep(10); } catch (Exception e) { /* ignore */ }
        }
        // 1. run authentication
        try {
            success = Authenticator.authenticate(username, password);
        }
        catch (Exception e) {
            success = false;
        }
        if (success) {
            infoLabel.setText(I18n.getString(DATA_LABEL_KEY));
            // 2. load metadata/user data
            String url = Main.appProperties.getProperty("base.url");
            // TODO FIXME bad default!!
            String uri = Main.appProperties.getProperty("metadata.uri", "/liccat.xml");
            MetadataLoader.load(url + uri, Authenticator.authCookie);
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

} // class AuthDialog
