/*
 * @(#)SummaryStep.java
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

import org.pietschy.wizard.AbstractWizardStep;
import org.pietschy.wizard.WizardModel;

import javax.swing.*;

import com.blipnetworks.util.I18n;

import java.awt.*;

/**
 *
 * @author Jared Klett
 * @version $Id: SummaryStep.java,v 1.1 2006/11/13 20:42:45 jklett Exp $
 */

public class SummaryStep extends AbstractWizardStep {

// CVS info ////////////////////////////////////////////////////////////////////

    public static final String CVS_REV = "$Revision: 1.1 $";

// Static variables ////////////////////////////////////////////////////////////

    /** blah */
    private static final String TITLE_KEY = "summary.title";
    /** blah */
    private static final String SUMMARY_KEY = "summary.summary";

// Instance variables //////////////////////////////////////////////////////////

    /** */
    private JPanel view;
    /** */
    private UpperBlipModel model;

// Constructor /////////////////////////////////////////////////////////////////

    public SummaryStep() {
        super(I18n.getString(TITLE_KEY), I18n.getString(SUMMARY_KEY));
        view = new JPanel();
        setComplete(true);
    }

    public void init(WizardModel model) {
        this.model = (UpperBlipModel)model;
    }

    public void prepare() {

    }

    public void applyState() {

    }

    public Dimension getPreferredSize() {
        return view.getPreferredSize();
    }

} // class SummaryStep
