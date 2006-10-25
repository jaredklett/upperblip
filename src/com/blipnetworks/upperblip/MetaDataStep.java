/* 
 * @(#)MetaDataStep.java
 * 
 * Copyright (c) 2005 by Blip Networks, Inc.
 * 239 Centre St, 3rd Floor
 * New York, NY 10013
 * All rights reserved.
 *
 * This software is the confidential and
 * proprietary information of Blip Networks, Inc.
 */

package com.blipnetworks.upperblip;

import java.awt.*;
import java.io.*;

import javax.swing.*;
import javax.swing.border.*;

import com.blipnetworks.util.I18n;
import com.blipnetworks.util.MetadataLoader;

import org.pietschy.wizard.AbstractWizardStep;
import org.pietschy.wizard.WizardModel;

/**
 * 
 * 
 * @author Jared Klett
 * @version $Id: MetaDataStep.java,v 1.14 2006/10/25 18:51:14 jklett Exp $
 */

public class MetaDataStep extends AbstractWizardStep {

// CVS info ////////////////////////////////////////////////////////////////////

    public static final String CVS_REV = "$Revision: 1.14 $";

// Static variables ////////////////////////////////////////////////////////////

    /** blah */
    private static final String TITLE_KEY = "meta.title";
    /** blah */
    private static final String SUMMARY_KEY = "meta.summary";
    /** blah */
    private static final String FILE_LABEL_KEY = "meta.file.label";
    /** blah */
    private static final String TITLE_LABEL_KEY = "meta.titlefield.label";
    /** blah */
    private static final String DESC_LABEL_KEY = "meta.desc.label";
    /** blah */
    private static final String LICENSE_LABEL_KEY = "meta.license.label";
    /** blah */
    private static final String TAGS_LABEL_KEY = "meta.tags.label";
    /** blah */
    private static final String CATEGORY_LABEL_KEY = "meta.category.label";

// Enumerated types ////////////////////////////////////////////////////////////

    // Enums, if any, go here

// Instance variables //////////////////////////////////////////////////////////

    /** */
    private JPanel view;
    /** */
    private JTextField[] titleList;
    /** */
    private JTextArea[] descList;
    /** */
    private JComboBox[] categoryList;
    /** */
    private JComboBox[] licenseList;
    /** */
    //private JCheckBox[] blogCheckboxList;
    /** */
    private JTextField[] tagsList;
    /** */
    private UpperBlipModel model;

// Constructor /////////////////////////////////////////////////////////////////

    public MetaDataStep() {
        super(I18n.getString(TITLE_KEY), I18n.getString(SUMMARY_KEY));
        view = new JPanel();
        setComplete(true);
    }

    public void init(WizardModel model) {
        this.model = (UpperBlipModel)model;
    }

    public void prepare() {
        view.removeAll();
        // Create and layout components
        JPanel overall = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        overall.setLayout(gbl);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets.top = 2;
        gbc.insets.bottom = 2;

        File[] files = model.getFiles();
        titleList = new JTextField[files.length];
        descList = new JTextArea[files.length];
        categoryList = new JComboBox[files.length];
        licenseList = new JComboBox[files.length];
        tagsList = new JTextField[files.length];
        //blogCheckboxList = new JCheckBox[files.length];

        for (int i = 0; i < files.length; i++) {
            JPanel panel = new JPanel();
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
            JLabel fileLabel = new JLabel(I18n.getString(FILE_LABEL_KEY));
            JLabel fileNameLabel = new JLabel(
                    files[i].getName(),
                    Icons.getIconForFilename(files[i].getName()),
                    SwingConstants.LEFT
            );
            JLabel titleLabel = new JLabel(I18n.getString(TITLE_LABEL_KEY));
            JLabel descLabel = new JLabel(I18n.getString(DESC_LABEL_KEY));
            JLabel licenseLabel = new JLabel(I18n.getString(LICENSE_LABEL_KEY));
            JLabel tagsLabel = new JLabel(I18n.getString(TAGS_LABEL_KEY));
            JLabel categoryLabel = new JLabel(I18n.getString(CATEGORY_LABEL_KEY));
            //TODO: add this functionality
            //JButton titleButton = new JButton("Apply title to all");
            //JButton descButton = new JButton("Apply description to all");
            //JButton removeButton = new JButton("Don't upload");
            JTextField titleField = new JTextField(20);
            JTextArea descArea = new JTextArea(10, 20);
            JComboBox categories = new JComboBox(MetadataLoader.categories.keySet().toArray());
            JComboBox licenses = new JComboBox(MetadataLoader.licenses.keySet().toArray());
            JTextField tagsField = new JTextField(20);
            descArea.setLineWrap(true);
            descArea.setWrapStyleWord(true);
            JScrollPane jsp = new JScrollPane(descArea);
            jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            // Track these components in lists
            titleList[i] = titleField;
            descList[i] = descArea;
            tagsList[i] = tagsField;
            licenseList[i] = licenses;
            categoryList[i] = categories;
            // Layout for the internal panel
            GridBagLayout gbl2 = new GridBagLayout();
            GridBagConstraints gbc2 = new GridBagConstraints();
            panel.setLayout(gbl2);
            gbc2.gridx = 0;
            gbc2.gridy = 0;
            gbc2.insets.top = 2;
            gbc2.insets.bottom = 2;
            gbc2.insets.left = 4;
            gbc2.insets.right = 4;
            gbc2.anchor = GridBagConstraints.NORTHEAST;
            gbl2.setConstraints(fileLabel, gbc2);
            panel.add(fileLabel);
            gbc2.gridx = 1;
            //gbc2.gridy = 0;
            gbc2.anchor = GridBagConstraints.NORTHWEST;
            gbl2.setConstraints(fileNameLabel, gbc2);
            panel.add(fileNameLabel);
            gbc2.gridx = 0;
            gbc2.gridy = 1;
            gbc2.anchor = GridBagConstraints.NORTHEAST;
            gbl2.setConstraints(titleLabel, gbc2);
            panel.add(titleLabel);
            gbc2.gridx = 1;
            //gbc2.gridy = 0;
            gbc2.anchor = GridBagConstraints.NORTHWEST;
            gbc2.fill = GridBagConstraints.HORIZONTAL;
            gbl2.setConstraints(titleField, gbc2);
            panel.add(titleField);
            gbc2.gridx = 0;
            gbc2.gridy = 2;
            gbc2.anchor = GridBagConstraints.NORTHEAST;
            gbc2.fill = GridBagConstraints.NONE;
            gbl2.setConstraints(descLabel, gbc2);
            panel.add(descLabel);
            gbc2.gridx = 1;
            //gbc2.gridy = 1;
            gbc2.anchor = GridBagConstraints.NORTHWEST;
            gbc2.fill = GridBagConstraints.HORIZONTAL;
            gbl2.setConstraints(jsp, gbc2);
            panel.add(jsp);
            gbc2.gridx = 0;
            gbc2.gridy = 3;
            gbc2.anchor = GridBagConstraints.NORTHEAST;
            gbc2.fill = GridBagConstraints.NONE;
            gbl2.setConstraints(licenseLabel, gbc2);
            panel.add(licenseLabel);
            gbc2.gridx = 1;
            //gbc2.gridy = 1;
            gbc2.anchor = GridBagConstraints.NORTHWEST;
            gbl2.setConstraints(licenses, gbc2);
            panel.add(licenses);
            gbc2.gridx = 0;
            gbc2.gridy = 4;
            gbc2.anchor = GridBagConstraints.NORTHEAST;
            gbl2.setConstraints(tagsLabel, gbc2);
            panel.add(tagsLabel);
            gbc2.gridx = 1;
            //gbc2.gridy = 1;
            gbc2.anchor = GridBagConstraints.NORTHWEST;
            gbc2.fill = GridBagConstraints.HORIZONTAL;
            gbl2.setConstraints(tagsField, gbc2);
            panel.add(tagsField);
            gbc2.gridx = 0;
            gbc2.gridy = 5;
            gbc2.anchor = GridBagConstraints.NORTHEAST;
            gbc2.fill = GridBagConstraints.NONE;
            gbl2.setConstraints(categoryLabel, gbc2);
            panel.add(categoryLabel);
            gbc2.gridx = 1;
            //gbc2.gridy = 1;
            gbc2.anchor = GridBagConstraints.NORTHWEST;
            gbl2.setConstraints(categories, gbc2);
            panel.add(categories);

            gbc.gridx = 0;
            gbc.gridy += 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            overall.add(panel, gbc);
        }

        JScrollPane scrollpane = new JScrollPane(overall);

        view.setLayout(new BorderLayout());
        view.add(scrollpane, BorderLayout.CENTER);

        setView(view);
    }

    public void applyState() {
        // this is called when the user clicks "Next"
        // Create string arrays
        String[] titles = new String[titleList.length];
        String[] descriptions = new String[descList.length];
        String[] tags = new String[descList.length];
        String[] categories = new String[descList.length];
        String[] licenses = new String[descList.length];
        // Loop through components and populate the arrays
        for (int i = 0; i < titleList.length; i++) {
            // If the user didn't enter a title, set it to the name of the file
            // minus the dot extension
            if (titleList[i].getText() != null && titleList[i].getText().equals("")) {
                String name = model.getFiles()[i].getName();
                titles[i] = name.substring(0, name.lastIndexOf("."));
            } else {
                titles[i] = titleList[i].getText();
            }
            descriptions[i] = descList[i].getText();
            tags[i] = tagsList[i].getText();
            categories[i] = (String)MetadataLoader.categories.get(categoryList[i].getSelectedItem());
            licenses[i] = (String)MetadataLoader.licenses.get(licenseList[i].getSelectedItem());
        }
        // Put the arrays in our model
        model.setTitles(titles);
        model.setDescriptions(descriptions);
        model.setTags(tags);
        model.setCategories(categories);
        model.setLicenses(licenses);
    }

    public Dimension getPreferredSize() {
        return view.getPreferredSize();
    }

} // class MetaDataStep
