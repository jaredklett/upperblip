/*
 * @(#)SummaryStep.java
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

import org.pietschy.wizard.AbstractWizardStep;
import org.pietschy.wizard.WizardModel;

import javax.swing.*;
import javax.swing.border.EtchedBorder;

import com.blipnetworks.util.I18n;
import com.blipnetworks.util.Command;

import java.awt.*;
import java.io.File;

import edu.stanford.ejalbert.BrowserLauncher;

/**
 *
 * @author Jared Klett
 * @version $Id: SummaryStep.java,v 1.7 2007/03/28 19:12:45 jklett Exp $
 */

public class SummaryStep extends AbstractWizardStep {

// CVS info ////////////////////////////////////////////////////////////////////

    public static final String CVS_REV = "$Revision: 1.7 $";

// Static variables ////////////////////////////////////////////////////////////

    /** blah */
    private static final String TITLE_KEY = "summary.title";
    /** blah */
    private static final String SUMMARY_KEY = "summary.summary";
    private static final String POST_TITLE_KEY = "summary.post.title";
    private static final String POST_FILE_KEY = "summary.post.file";
    private static final String SUCCESS_KEY = "summary.success.label";
    private static final String ERROR_KEY = "summary.error.label";

// Instance variables //////////////////////////////////////////////////////////

    /** */
    private JPanel view;
    /** */
    private UpperBlipModel model;

// Constructor /////////////////////////////////////////////////////////////////

    public SummaryStep() {
        super(I18n.getString(TITLE_KEY), I18n.getString(SUMMARY_KEY));
        view = new JPanel();
        setIcon(Icons.summaryIcon);
        setComplete(true);
    }

    public void init(WizardModel model) {
        this.model = (UpperBlipModel)model;
    }

    public void prepare() {
        view.removeAll();
        JPanel overall = new JPanel();
        //JLabel successLabel = new JLabel(I18n.getString(SUCCESS_KEY), Icons.successIcon, JLabel.HORIZONTAL);
        //JLabel errorLabel = new JLabel(I18n.getString(ERROR_KEY), Icons.errorIcon, JLabel.HORIZONTAL);
        Font linkFont = new Font("SansSerif", Font.PLAIN, 12);
        File[] files = model.getFiles();
        String[] titles = model.getTitles();
        final String[] postURLs = model.getPostURLs();
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        overall.setLayout(gbl);
        for (int i = 0; i < files.length; i++) {
            final int index = i;
            JPanel panel = new JPanel();
            panel.setBorder(
                BorderFactory.createCompoundBorder(
                    BorderFactory.createCompoundBorder(
                        BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)
                    ),
                    panel.getBorder()
                )
            );
            GridBagLayout gbl2 = new GridBagLayout();
            GridBagConstraints gbc2 = new GridBagConstraints();
            panel.setLayout(gbl2);
            JLabel fileLabel = new JLabel(I18n.getString(POST_FILE_KEY) + " " + files[i].getName());
            JLabel titleLabel = new JLabel(I18n.getString(POST_TITLE_KEY) + " " + titles[i]);
            LinkLabel linkLabel = new LinkLabel("Go to the post", new Command() {
                public void execute() {
                    try {
                        BrowserLauncher bl = new BrowserLauncher(null);
                        System.out.println(postURLs[index]);
                        bl.openURLinBrowser(postURLs[index]);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            linkLabel.setFont(linkFont);
            JLabel successLabel = new JLabel(I18n.getString(SUCCESS_KEY), Icons.successIcon, JLabel.HORIZONTAL);
            gbc2.gridx = 0;
            gbc2.gridy = 0;
            gbc2.insets.top = 2;
            gbc2.insets.bottom = 2;
            gbc2.insets.left = 2;
            gbc2.insets.right = 2;
            gbc2.gridheight = 1;
            gbc2.weightx = 1.0;
            gbc2.fill = GridBagConstraints.HORIZONTAL;
            gbc2.anchor = GridBagConstraints.WEST;
            gbl2.setConstraints(fileLabel, gbc2);
            panel.add(fileLabel);
            gbc2.gridy = 1;
            gbl2.setConstraints(titleLabel, gbc2);
            panel.add(titleLabel);
            gbc2.gridy = 2;
            gbl2.setConstraints(linkLabel, gbc2);
            panel.add(linkLabel);
            gbc2.gridx = 1;
            gbc2.gridy = 0;
            gbc2.gridheight = 3;
            gbc2.weightx = 0.0;
            gbc2.anchor = GridBagConstraints.CENTER;
            gbl2.setConstraints(successLabel, gbc2);
            panel.add(successLabel);
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbl.setConstraints(panel, gbc);
            overall.add(panel);
        }
        JScrollPane scrollpane = new JScrollPane(overall);

        view.setLayout(new BorderLayout());
        view.add(scrollpane, BorderLayout.CENTER);

        setView(view);
    }

    public void applyState() {

    }

    public Dimension getPreferredSize() {
        return view.getPreferredSize();
    }

} // class SummaryStep
