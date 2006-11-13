/* 
 * @(#)MetaDataStep.java
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
import java.io.*;

import javax.swing.*;
import javax.swing.border.*;

import com.blipnetworks.util.I18n;
import com.blipnetworks.util.MetadataLoader;
import com.blipnetworks.util.Command;

import org.pietschy.wizard.AbstractWizardStep;
import org.pietschy.wizard.WizardModel;

/**
 *
 *
 * @author Jared Klett
 * @version $Id: MetaDataStep.java,v 1.19 2006/11/13 19:33:54 jklett Exp $
 */

public class MetaDataStep extends AbstractWizardStep {

// CVS info ////////////////////////////////////////////////////////////////////

    public static final String CVS_REV = "$Revision: 1.19 $";

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
    private static final String THUMB_LABEL_KEY = "meta.thumb.label";
    /** blah */
    private static final String LICENSE_LABEL_KEY = "meta.license.label";
    /** blah */
    private static final String TAGS_LABEL_KEY = "meta.tags.label";
    /** blah */
    private static final String CATEGORY_LABEL_KEY = "meta.category.label";

// Instance variables //////////////////////////////////////////////////////////

    /** */
    private JPanel view;
    /** */
    private JTextField[] titleList;
    /** */
    private JTextArea[] descList;
    /** */
    private JComboBox[] thumbList;
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
        thumbList = new JComboBox[files.length];
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
            JLabel thumbLabel = new JLabel(I18n.getString(THUMB_LABEL_KEY));
            JLabel licenseLabel = new JLabel(I18n.getString(LICENSE_LABEL_KEY));
            JLabel tagsLabel = new JLabel(I18n.getString(TAGS_LABEL_KEY));
            JLabel categoryLabel = new JLabel(I18n.getString(CATEGORY_LABEL_KEY));
            final JTextField titleField = new JTextField(10);
            ApplyLabel applyTitleLabel = new ApplyLabel(
                    "Apply to all",
                    new Command() {
                        public void execute() {
                            String title = titleField.getText();
                            for (int i = 0; i < titleList.length; i++)
                                titleList[i].setText(title);
                        }
                    }
            );
            final JTextArea descArea = new JTextArea(10, 10);
            ApplyLabel applyDescLabel = new ApplyLabel(
                    "Apply to all",
                    new Command() {
                        public void execute() {
                            String desc = descArea.getText();
                            for (int i = 0; i < descList.length; i++)
                                descList[i].setText(desc);
                        }
                    }
            );
            final JComboBox thumbnails = new JComboBox(model.getImageFilenames());
            ApplyLabel applyThumbnailLabel = new ApplyLabel(
                    "Apply to all",
                    new Command() {
                        public void execute() {
                            int index = thumbnails.getSelectedIndex();
                            for (int i = 0; i < thumbList.length; i++)
                                thumbList[i].setSelectedIndex(index);
                        }
                    }
            );
            final JComboBox categories = new JComboBox(MetadataLoader.categories.keySet().toArray());
            ApplyLabel applyCatLabel = new ApplyLabel(
                    "Apply to all",
                    new Command() {
                        public void execute() {
                            int index = categories.getSelectedIndex();
                            for (int i = 0; i < categoryList.length; i++)
                                categoryList[i].setSelectedIndex(index);
                        }
                    }
            );
            final JComboBox licenses = new JComboBox(MetadataLoader.licenses.keySet().toArray());
            ApplyLabel applyLicenseLabel = new ApplyLabel(
                    "Apply to all",
                    new Command() {
                        public void execute() {
                            int index = licenses.getSelectedIndex();
                            for (int i = 0; i < licenseList.length; i++)
                                licenseList[i].setSelectedIndex(index);
                        }
                    }
            );
            final JTextField tagsField = new JTextField(10);
            ApplyLabel applyTagsLabel = new ApplyLabel(
                    "Apply to all",
                    new Command() {
                        public void execute() {
                            String tags = tagsField.getText();
                            for (int i = 0; i < tagsList.length; i++)
                                tagsList[i].setText(tags);
                        }
                    }
            );
            descArea.setLineWrap(true);
            descArea.setWrapStyleWord(true);
            JScrollPane jsp = new JScrollPane(descArea);
            jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            // Track these components in lists
            titleList[i] = titleField;
            descList[i] = descArea;
            thumbList[i] = thumbnails;
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
            gbc2.gridx = 2;
            gbl2.setConstraints(applyTitleLabel, gbc2);
            panel.add(applyTitleLabel);
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
            gbc2.gridx = 2;
            gbl2.setConstraints(applyDescLabel, gbc2);
            panel.add(applyDescLabel);
            gbc2.gridx = 0;
            gbc2.gridy = 3;
            gbc2.anchor = GridBagConstraints.EAST;
            gbc2.fill = GridBagConstraints.NONE;
            gbl2.setConstraints(thumbLabel, gbc2);
            panel.add(thumbLabel);
            gbc2.gridx = 1;
            //gbc2.gridy = 1;
            gbc2.anchor = GridBagConstraints.WEST;
            gbl2.setConstraints(thumbnails, gbc2);
            panel.add(thumbnails);
            gbc2.gridx = 2;
            gbl2.setConstraints(applyThumbnailLabel, gbc2);
            panel.add(applyThumbnailLabel);
            gbc2.gridx = 0;
            gbc2.gridy = 4;
            gbc2.anchor = GridBagConstraints.EAST;
            gbc2.fill = GridBagConstraints.NONE;
            gbl2.setConstraints(licenseLabel, gbc2);
            panel.add(licenseLabel);
            gbc2.gridx = 1;
            //gbc2.gridy = 1;
            gbc2.anchor = GridBagConstraints.WEST;
            gbl2.setConstraints(licenses, gbc2);
            panel.add(licenses);
            gbc2.gridx = 2;
            gbl2.setConstraints(applyLicenseLabel, gbc2);
            panel.add(applyLicenseLabel);
            gbc2.gridx = 0;
            gbc2.gridy = 5;
            gbc2.anchor = GridBagConstraints.NORTHEAST;
            gbl2.setConstraints(tagsLabel, gbc2);
            panel.add(tagsLabel);
            gbc2.gridx = 1;
            //gbc2.gridy = 1;
            gbc2.anchor = GridBagConstraints.NORTHWEST;
            gbc2.fill = GridBagConstraints.HORIZONTAL;
            gbl2.setConstraints(tagsField, gbc2);
            panel.add(tagsField);
            gbc2.gridx = 2;
            gbc2.anchor = GridBagConstraints.CENTER;
            gbl2.setConstraints(applyTagsLabel, gbc2);
            panel.add(applyTagsLabel);
            gbc2.gridx = 0;
            gbc2.gridy = 6;
            gbc2.anchor = GridBagConstraints.EAST;
            gbc2.fill = GridBagConstraints.NONE;
            gbl2.setConstraints(categoryLabel, gbc2);
            panel.add(categoryLabel);
            gbc2.gridx = 1;
            //gbc2.gridy = 1;
            gbc2.anchor = GridBagConstraints.WEST;
            gbl2.setConstraints(categories, gbc2);
            panel.add(categories);
            gbc2.gridx = 2;
            gbl2.setConstraints(applyCatLabel, gbc2);
            panel.add(applyCatLabel);

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
        File[] thumbnails = new File[descList.length];
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
            thumbnails[i] = null;
            if (thumbList[i].getSelectedIndex() != 0) {
                String filename = (String)thumbList[i].getSelectedItem();
                Object obj = model.thumbnailFileLookup.get(filename);
                if (obj != null)
                    thumbnails[i] = (File)obj;
            }
        }
        // Put the arrays in our model
        model.setTitles(titles);
        model.setDescriptions(descriptions);
        model.setTags(tags);
        model.setCategories(categories);
        model.setLicenses(licenses);
        model.setThumbnails(thumbnails);
    }

    public Dimension getPreferredSize() {
        return view.getPreferredSize();
    }

} // class MetaDataStep
