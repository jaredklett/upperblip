/* 
 * @(#)MetaDataStep.java
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

import 	java.awt.*;
import 	java.awt.event.*;
import 	java.io.*;
import	java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import 	javax.swing.*;

import 	com.blipnetworks.util.I18n;
import 	com.blipnetworks.util.MetadataLoader;
import 	com.blipnetworks.util.Command;

import	com.blipnetworks.upperblip.wizard.*;

/**
 *
 *
 * @author Jared Klett
 * @version $Id: MetaDataStep.java,v 1.35 2009/06/22 21:07:45 jklett Exp $
 */

public class MetaDataStep extends AbstractWizardStep {

// CVS info ////////////////////////////////////////////////////////////////////

    public static final String CVS_REV = "$Revision: 1.35 $";

// Static variables ////////////////////////////////////////////////////////////

    // These are the key values used to access various entries in the properties
    // file, currently the English version: com-blipnetworks-upperblip_en_US.properties
    private static final String TITLE_KEY = "meta.title";
    private static final String SUMMARY_KEY = "meta.summary";
    private static final String FILE_LABEL_KEY = "meta.file.label";
    private static final String TITLE_LABEL_KEY = "meta.titlefield.label";
    private static final String DESC_LABEL_KEY = "meta.desc.label";
    private static final String THUMB_LABEL_KEY = "meta.thumb.label";
    private static final String LICENSE_LABEL_KEY = "meta.license.label";
    private static final String RATING_LABEL_KEY = "meta.rating.label";
    private static final String EXPLICIT_LABEL_KEY = "meta.explicit.label";
    private static final String LANGUAGE_LABEL_KEY = "meta.language.label";
    private static final String TAGS_LABEL_KEY = "meta.tags.label";
    private static final String CATEGORY_LABEL_KEY = "meta.category.label";
    private static final String APPLY_LABEL_KEY = "meta.apply.label";
    private static final String DIST_LABEL_KEY = "meta.dist.label";
    private static final String BLOGS_LABEL_KEY = "meta.blogs.label";
    private static final String XUPLOADS_LABEL_KEY = "meta.xuploads.label";
    private static final String	PRIVATE_LABEL_KEY = "meta.privatefile.label";
    private static final String	TARGETS_LABEL_KEY = "meta.targets.label";
    private static final String	MP3AUDIO_LABEL_KEY = "meta.mp3audio.label";
    private static final String	MPEG4VIDEO_LABEL_KEY = "meta.mpeg4video.label";
    private static final String	PASSWORD_LABEL_KEY = "meta.password.label";
    private static final String	PUBLIC_LABEL_KEY = "meta.public.label";
    private static final String	PRIVACY_LABEL_KEY= "meta.privacy.label";
    
    private static final String	DEFAULT_TEXT = "general.default.text";
    
// Instance variables //////////////////////////////////////////////////////////

    private JTabbedPane		pane;
    private JTextField[] 	titleList;
    private JTextArea[] 	descList;
    private JComboBox[] 	thumbList;
    private JComboBox[] 	categoryList;
    private JComboBox[] 	licenseList;
    private JComboBox[] 	languageList;
    private JComboBox[] 	ratingList;
    private JCheckBox[] 	explicitList;
    private JCheckBox[][]	blogCheckboxList;
    private JCheckBox[][]	destCheckboxList;
    private JTextField[]	tagsList;
    private UpperBlipModel 	model;
    
    private LinkLabel		applyLabel;
    
    private Map<JLabel, Set<JPanel>>	distributionMap = null;
    
    private JCheckBox[]		mp3AudioCheckboxList = null;
    private JCheckBox[]		mpeg4VideoCheckboxList = null;
    
    // These components are used in the Privacy Settings panel
	private JCheckBox[]			privateFileCheckboxList = null;
	private JCheckBox[]			visiblePasswordCheckboxList = null;
	private JCheckBox[]			makePublicCheckboxList = null;
	private JPasswordField		visiblePasswordField = null;
	private JPasswordField[]	visiblePasswordFieldList = null;
	private JTextField			makePublicField = null;
	private JTextField[]		makePublicFieldList = null;
    
	private JButton			completeButton = null;
	
    private JFrame			wizardFrame = null;
    private JPanel			viewPanel = null;
    private MovementHandler	mover = null;
        
// Constructor /////////////////////////////////////////////////////////////////

    public MetaDataStep() {
        super(I18n.getString(TITLE_KEY), I18n.getString(SUMMARY_KEY));
        setIcon(Icons.metadataIcon);
    }

    public void init(WizardModel model) {
        this.model = (UpperBlipModel)model;
    }

    public void prepare() {
    	viewPanel = new JPanel(new GridBagLayout());
    	Grid	viewGrid = new Grid();
    	
    	model.setNextAvailable(false);
        wizardFrame = Main.getMainInstance().getMainFrame();
        mover = Main.getMainInstance().getMover();
        viewPanel.removeAll();
        
        pane = new JTabbedPane();
        viewPanel.add(pane, viewGrid);
        
        viewGrid.incrY();
        completeButton = new JButton("Confirm Form Complete");
        viewPanel.add(completeButton, viewGrid);
        
        completeButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent event) {
        		executeForm();
        		
        		String[]	makePrivate = model.getPrivateFiles();
        		String[]	makePassword = model.getPasswordFiles();
        		String[]	makePublic = model.getMakePublicFiles();
        		String[]	privatePasswords = model.getPasswordFields();
        		String[]	makePublicFields = model.getMakePublicFields();
        		
        		for (int i = 0; i < makePrivate.length; i++) {
        			if (makePrivate[i].equals("1")) {
        				if (makePassword[i].equals("1")) {
        					if (!checkPasswordFields(visiblePasswordCheckboxList, privatePasswords)) {
        						return;
        					}
        				}
        				if (makePublic[i].equals("1")) {
        					if (!checkPublicFields(makePublicCheckboxList, makePublicFields)) {
        						return;
        					}
        				}
        			}
        		}
        		setComplete(true);
        	}

        });
                
        File[] files = model.getFiles();
        
        createModelData(files);
                
        // TODO: break type array out
        String[] 	blogNames = MetadataLoader.blogs.keySet().toArray(new String[0]);
        String[] 	destinations = MetadataLoader.crossuploads.keySet().toArray(new String[0]);
        
        blogCheckboxList = new JCheckBox[files.length][blogNames.length];
        destCheckboxList = new JCheckBox[files.length][destinations.length];
        mp3AudioCheckboxList = new JCheckBox[files.length];
        mpeg4VideoCheckboxList = new JCheckBox[files.length];
        privateFileCheckboxList = new JCheckBox[files.length];
        makePublicCheckboxList = new JCheckBox[files.length];
        visiblePasswordCheckboxList = new JCheckBox[files.length];

        for (int i = 0; i < files.length; i++) {
        	JPanel	panePanel = new JPanel(new GridBagLayout());
            JPanel	topPanel = new JPanel(new GridBagLayout());
            Grid	paneGrid = new Grid();
            
            panePanel.add(topPanel, paneGrid);
                                    
            for (int j = 0; j < blogCheckboxList[i].length; j++) {
                blogCheckboxList[i][j] = new JCheckBox(blogNames[j], false);
            }
            
            for (int j = 0; j < destCheckboxList[i].length; j++) {
                destCheckboxList[i][j] = new JCheckBox(destinations[j], false);
            }
            
        	Grid	topPanelGrid = new Grid();

            topPanelGrid.setInsets(2, 4, 2, 4);
            
            JLabel fileLabel = new JLabel(I18n.getString(FILE_LABEL_KEY));
            JLabel fileNameLabel = new JLabel(files[i].getName(), Icons.getIconForFilename(files[i].getName()), SwingConstants.LEFT);
            topPanel.add(fileLabel, topPanelGrid.setAnchor(Grid.northEastAnchor));
            topPanel.add(fileNameLabel, topPanelGrid.setAnchorIncrX(Grid.northWestAnchor));
            
            final JTextField	titleField = new JTextField(10);
            titleList[i] = titleField;
            applyLabel = new LinkLabel(I18n.getString(APPLY_LABEL_KEY), new Command() {
                public void execute() {
                    String title = titleField.getText();
                    for (int i = 0; i < titleList.length; i++) {
                        titleList[i].setText(title);
                    }
                }
            });
            JLabel titleLabel = new JLabel(I18n.getString(TITLE_LABEL_KEY));
            topPanel.add(titleLabel, topPanelGrid.setAnchorIncrY(Grid.northEastAnchor));
            topPanelGrid.setFill(Grid.horizontalFill);
            topPanel.add(titleField, topPanelGrid.setAnchorIncrX(Grid.northWestAnchor));
            topPanel.add(applyLabel, topPanelGrid.incrX());
            
            final JTextArea	descArea = new JTextArea(5, 10);
            descArea.setLineWrap(true);
            descArea.setWrapStyleWord(true);
            JScrollPane jsp = new JScrollPane(descArea);
            jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            descList[i] = descArea;
            applyLabel = new LinkLabel(I18n.getString(APPLY_LABEL_KEY), new Command() {
                public void execute() {
                    String desc = descArea.getText();
                    for (int i = 0; i < descList.length; i++) {
                        descList[i].setText(desc);
                    }
                }
            });
            JLabel descLabel = new JLabel(I18n.getString(DESC_LABEL_KEY));
            topPanelGrid.setFill(Grid.noneFill);
            topPanel.add(descLabel, topPanelGrid.setAnchorIncrY(Grid.northEastAnchor));
            topPanelGrid.setFill(Grid.horizontalFill);
            topPanel.add(jsp, topPanelGrid.setAnchorIncrX(Grid.northWestAnchor));
            topPanel.add(applyLabel, topPanelGrid.incrX());
            
            final JComboBox	thumbnails = new JComboBox(model.getImageFilenames());
            thumbList[i] = thumbnails;
            applyLabel = new LinkLabel(I18n.getString(APPLY_LABEL_KEY), new Command() {
                public void execute() {
                    int index = thumbnails.getSelectedIndex();
                    for (int i = 0; i < thumbList.length; i++) {
                        thumbList[i].setSelectedIndex(index);
                    }
                }
            });
            JLabel thumbLabel = new JLabel(I18n.getString(THUMB_LABEL_KEY));
            topPanelGrid.setFill(Grid.noneFill);
            topPanel.add(thumbLabel, topPanelGrid.setAnchorIncrY(Grid.eastAnchor));
            topPanel.add(thumbnails, topPanelGrid.setAnchorIncrX(Grid.westAnchor));
            topPanel.add(applyLabel, topPanelGrid.incrX());
            
            final JComboBox	licenses = new JComboBox(MetadataLoader.licenses.keySet().toArray());
            licenseList[i] = licenses;
            applyLabel = new LinkLabel(I18n.getString(APPLY_LABEL_KEY), new Command() {
                public void execute() {
                    int index = licenses.getSelectedIndex();
                    for (int i = 0; i < licenseList.length; i++) {
                        licenseList[i].setSelectedIndex(index);
                    }
                }
            });
            JLabel licenseLabel = new JLabel(I18n.getString(LICENSE_LABEL_KEY));
            topPanel.add(licenseLabel, topPanelGrid.setAnchorIncrY(Grid.eastAnchor));
            topPanel.add(licenses, topPanelGrid.setAnchorIncrX(Grid.westAnchor));
            topPanel.add(applyLabel, topPanelGrid.incrX());
            
            final JTextField	tagsField = new JTextField(10);
            tagsList[i] = tagsField;
            applyLabel = new LinkLabel(I18n.getString(APPLY_LABEL_KEY), new Command() {
                public void execute() {
                    String tags = tagsField.getText();
                    for (int i = 0; i < tagsList.length; i++)
                        tagsList[i].setText(tags);
                }
            });
            JLabel tagsLabel = new JLabel(I18n.getString(TAGS_LABEL_KEY));
            topPanel.add(tagsLabel, topPanelGrid.setAnchorIncrY(Grid.northEastAnchor));
            topPanelGrid.setFill(Grid.horizontalFill);
            topPanel.add(tagsField, topPanelGrid.setAnchorIncrX(Grid.northWestAnchor));
            topPanel.add(applyLabel, topPanelGrid.setAnchorIncrX(Grid.centerAnchor));
            
            final JComboBox	categories = new JComboBox(MetadataLoader.categories.keySet().toArray());
            categories.setSelectedItem(I18n.getString(DEFAULT_TEXT));
            categoryList[i] = categories;
            applyLabel = new LinkLabel(I18n.getString(APPLY_LABEL_KEY), new Command() {
                public void execute() {
                    int index = categories.getSelectedIndex();
                    for (int i = 0; i < categoryList.length; i++) {
                        categoryList[i].setSelectedIndex(index);
                    }
                }
            });
            JLabel categoryLabel = new JLabel(I18n.getString(CATEGORY_LABEL_KEY));
            topPanelGrid.setFill(Grid.noneFill);
            topPanel.add(categoryLabel, topPanelGrid.setAnchorIncrY(Grid.eastAnchor));
            topPanel.add(categories, topPanelGrid.setAnchorIncrX(Grid.westAnchor));
            topPanel.add(applyLabel, topPanelGrid.incrX());
            
            final JComboBox	ratings = new JComboBox(MetadataLoader.ratings.keySet().toArray());
            ratings.setSelectedIndex(5);
            ratingList[i] = ratings;
            applyLabel = new LinkLabel(I18n.getString(APPLY_LABEL_KEY), new Command() {
                public void execute() {
                    int index = ratings.getSelectedIndex();
                    for (int i = 0; i < ratingList.length; i++)
                        ratingList[i].setSelectedIndex(index);
                }
            });
            JLabel ratingLabel = new JLabel(I18n.getString(RATING_LABEL_KEY));
            topPanelGrid.setFill(Grid.noneFill);
            topPanel.add(ratingLabel, topPanelGrid.setAnchorIncrY(Grid.eastAnchor));
            topPanel.add(ratings, topPanelGrid.setAnchorIncrX(Grid.westAnchor));
            topPanel.add(applyLabel, topPanelGrid.incrX());

            final JCheckBox	explicit = new JCheckBox(I18n.getString(EXPLICIT_LABEL_KEY));
            explicitList[i] = explicit;
            applyLabel = new LinkLabel(I18n.getString(APPLY_LABEL_KEY), new Command() {
                public void execute() {
                    boolean selected = explicit.isSelected();
                    for (int i = 0; i < explicitList.length; i++)
                        explicitList[i].setSelected(selected);
                }
            });
            topPanelGrid.setAnchorIncrY(Grid.eastAnchor);            
            topPanelGrid.setAnchorIncrX(Grid.westAnchor);
            topPanel.add(explicit, topPanelGrid);
            topPanel.add(applyLabel, topPanelGrid.incrX());

            final JComboBox	languages = new JComboBox(MetadataLoader.languages.keySet().toArray());
            languages.setSelectedItem("English");
            languageList[i] = languages;
            applyLabel = new LinkLabel(I18n.getString(APPLY_LABEL_KEY), new Command() {
                public void execute() {
                    int index = languages.getSelectedIndex();
                    for (int i = 0; i < languageList.length; i++)
                        languageList[i].setSelectedIndex(index);
                }
            });
            JLabel languageLabel = new JLabel(I18n.getString(LANGUAGE_LABEL_KEY));
            topPanelGrid.setFill(Grid.noneFill);
            topPanel.add(languageLabel, topPanelGrid.setAnchorIncrY(Grid.eastAnchor));
            topPanel.add(languages, topPanelGrid.setAnchorIncrX(Grid.westAnchor));
            topPanel.add(applyLabel, topPanelGrid.incrX());

            boolean noBlogs = (blogNames.length == 0);
            boolean noDests = (destinations.length == 0);
            
            JLabel	distribution = null;
            
            paneGrid.setFill(Grid.horizontalFill);
            paneGrid.setRelativeY();
            
            if (!noBlogs || !noDests) {
            	if (distributionMap == null) {
            		distributionMap = new HashMap<JLabel, Set<JPanel>>();
            	}
            	Set<JPanel>	panelSet = new HashSet<JPanel>();
            	            	            	
            	distribution = new JLabel(I18n.getString(DIST_LABEL_KEY), Icons.collapsedIcon, JLabel.HORIZONTAL);
            	distribution.addMouseListener(new MouseHandler());
            	distributionMap.put(distribution, panelSet);
                
                topPanelGrid.incrY();
                topPanelGrid.setX(0);
                topPanel.add(distribution, topPanelGrid);
                
                if (!noBlogs) {
                    JPanel blogPanel = new JPanel();
                    
            		blogPanel.setLayout(new BoxLayout(blogPanel, BoxLayout.LINE_AXIS));
            		blogPanel.setVisible(false);
            		panelSet.add(blogPanel);
                    
                    final JCheckBox[] boxes = blogCheckboxList[i];
                    LinkLabel applyBlogLabel = new LinkLabel(I18n.getString(APPLY_LABEL_KEY), new Command() {
                        public void execute() {
                            for (int x = 0; x < blogCheckboxList.length; x++) {
                                for (int y = 0; y < blogCheckboxList[x].length; y++) {
                                    blogCheckboxList[x][y].setSelected(boxes[y].isSelected());
                                }
                            }
                        }
                    });
                                        
                    for (int j = 0; j < blogCheckboxList[i].length; j++) {
                        blogPanel.add(blogCheckboxList[i][j]);
                        if (j < (blogCheckboxList[i].length - 1)) {
                        	blogPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                        }
                    }
                    blogPanel.add(Box.createHorizontalGlue());
                    blogPanel.add(applyBlogLabel);
                    
                    setPanelBorder(blogPanel, BLOGS_LABEL_KEY);
                    panePanel.add(blogPanel, paneGrid);
                }
                
                if (!noDests) {
                    JPanel uploadPanel = new JPanel();
                    
            		uploadPanel.setLayout(new BoxLayout(uploadPanel, BoxLayout.LINE_AXIS));
            		uploadPanel.setVisible(false);
            		panelSet.add(uploadPanel);
            		setPanelBorder(uploadPanel, XUPLOADS_LABEL_KEY);
                    
                    final JCheckBox[] boxes = destCheckboxList[i];
                    LinkLabel applyDestLabel = new LinkLabel(I18n.getString(APPLY_LABEL_KEY), new Command() {
                        public void execute() {
                            for (int x = 0; x < destCheckboxList.length; x++)
                                for (int y = 0; y < destCheckboxList[x].length; y++)
                                    destCheckboxList[x][y].setSelected(boxes[y].isSelected());
                        }
                    });
                    
                    for (int j = 0; j < destCheckboxList[i].length; j++) {
                        uploadPanel.add(destCheckboxList[i][j]);
                		uploadPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                    }
                    uploadPanel.add(Box.createHorizontalGlue());
                    uploadPanel.add(applyDestLabel);
                    panePanel.add(uploadPanel, paneGrid);
                }
                
                if (MetadataLoader.conversionTargets.size() != 0) {
	                JPanel	targetPanel = addTargetsPanel(i);
	                targetPanel.setVisible(false);
	                panelSet.add(targetPanel);
	                panePanel.add(targetPanel, paneGrid);
                }
                
                if (MetadataLoader.privacySettings.size() != 0) {
	                JPanel	privacyPanel = addPrivacyPanel(i);
	                privacyPanel.setVisible(false);
	                panelSet.add(privacyPanel);
	                panePanel.add(privacyPanel, paneGrid);
                }
            }

            pane.addTab(files[i].getName(), panePanel);
        }

        setView(viewPanel);
        wizardFrame.pack();
        mover.positionFrame(true);
    }

    public void executeForm() {
        // this is called when the user clicks "Next"
        // Create string arrays
        String[] 	titles = new String[titleList.length];
        // TODO: why descList? why not match up?
        String[] 	descriptions = new String[descList.length];
        String[] 	tags = new String[descList.length];
        String[] 	categories = new String[descList.length];
        String[] 	licenses = new String[descList.length];
        String[] 	ratings = new String[descList.length];
        String[]	explicitFlags = new String[descList.length];
        String[] 	languages = new String[descList.length];
        // TODO: this could result in an NPE, right? maybe not.
        String[][] 	blogs = new String[blogCheckboxList.length][blogCheckboxList[0].length];
        String[][] 	destinations = new String[destCheckboxList.length][destCheckboxList[0].length];
        String[]	mp3Audios = new String[mp3AudioCheckboxList.length];
        String[]	mpeg4Videos = new String[mpeg4VideoCheckboxList.length];
        String[]	privateFiles = new String[privateFileCheckboxList.length];
        String[]	passwordFiles = new String[visiblePasswordCheckboxList.length];
        String[]	passwordFields = new String[passwordFiles.length];
        String[]	makePublicFiles = new String[makePublicCheckboxList.length];
        String[]	makePublicFields = new String[passwordFiles.length];
        
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
            languages[i] = MetadataLoader.languages.get(languageList[i].getSelectedItem());
            categories[i] = MetadataLoader.categories.get(categoryList[i].getSelectedItem());
            licenses[i] = MetadataLoader.licenses.get(licenseList[i].getSelectedItem());
            // Content rating dropdowns
            ratings[i] = "";
            if (ratingList[i].getSelectedIndex() != 0) {
                ratings[i] = MetadataLoader.ratings.get(ratingList[i].getSelectedItem());
            }
            // Explicit checkboxes
            explicitFlags[i] = explicitList[i].isSelected() ? "1" : "0";
            // Thumbnail file dropdowns
            thumbnails[i] = null;
            if (thumbList[i].getSelectedIndex() != 0) {
                String filename = (String)thumbList[i].getSelectedItem();
                Object obj = model.thumbnailFileLookup.get(filename);
                if (obj != null) {
                    thumbnails[i] = (File)obj;
                }
            }
            
            // Cross-post destination dropdowns
            for (int j = 0; j < blogs[i].length; j++) {
                if (blogCheckboxList[i][j].isSelected()) {
                    blogs[i][j] = MetadataLoader.blogs.get(blogCheckboxList[i][j].getText());
                }
            }
            
            // Cross-upload destination dropdowns
            for (int j = 0; j < destinations[i].length; j++) {
                if (destCheckboxList[i][j].isSelected()) {
                    destinations[i][j] = MetadataLoader.crossuploads.get(destCheckboxList[i][j].getText());
                }
            }
            
            mp3Audios[i] = (mp3AudioCheckboxList[i].isSelected())? "1" : "0";
            mpeg4Videos[i] = (mpeg4VideoCheckboxList[i].isSelected())? "1" : "0";
            
            privateFiles[i] = "0";
            passwordFiles[i] = "0";
            makePublicFiles[i] = "0";
            passwordFields[i] = "";
            makePublicFields[i] = "";
            if (privateFileCheckboxList[i].isSelected()) {
            	privateFiles[i] = "1";
            	if (visiblePasswordCheckboxList[i].isSelected()) {
            		passwordFiles[i] = "1";
            		passwordFields[i] = String.valueOf(visiblePasswordFieldList[i].getPassword());
            	}
            	if (makePublicCheckboxList[i].isSelected()) {
            		makePublicFiles[i] = "1";
            		makePublicFields[i] = makePublicFieldList[i].getText();
            	}
            }
        }
                
        // Put the arrays in our model
        model.setTitles(titles);
        model.setDescriptions(descriptions);
        model.setTags(tags);
        model.setCategories(categories);
        model.setLicenses(licenses);
        model.setRatings(ratings);
        model.setExplicitFlags(explicitFlags);
        model.setLanguages(languages);
        model.setThumbnails(thumbnails);
        model.setCrossposts(blogs);
        model.setCrossuploads(destinations);
        model.setMp3Audios(mp3Audios);
        model.setMpeg4Videos(mpeg4Videos);
        model.setPrivateFiles(privateFiles);
        model.setPasswordFiles(passwordFiles);
        model.setMakePublicFiles(makePublicFiles);
        model.setPasswordFields(passwordFields);
        model.setMakePublicFields(makePublicFields);
    }
    
    public void applyState() {
    }
    
    public Dimension getPreferredSize() {
        return pane.getPreferredSize();
    }
    
    private void createModelData(File[] files) {
    	
        titleList = new JTextField[files.length];
        descList = new JTextArea[files.length];
        thumbList = new JComboBox[files.length];
        categoryList = new JComboBox[files.length];
        licenseList = new JComboBox[files.length];
        ratingList = new JComboBox[files.length];
        explicitList = new JCheckBox[files.length];
        languageList = new JComboBox[files.length];
        tagsList = new JTextField[files.length];
        
        privateFileCheckboxList = new JCheckBox[files.length];
        visiblePasswordCheckboxList = new JCheckBox[files.length];
        makePublicCheckboxList = new JCheckBox[files.length];
        visiblePasswordFieldList = new JPasswordField[files.length];
        makePublicFieldList = new JTextField[files.length];
        mp3AudioCheckboxList = new JCheckBox[files.length];
        mpeg4VideoCheckboxList = new JCheckBox[files.length];
    }
        
    /**
     * This method handles the Conversion Targets panel under the Distribution label
     * 
     * @return
     */
    private JPanel addTargetsPanel(int index) {
    	JPanel	target = new JPanel();
    	
		target.setLayout(new BoxLayout(target, BoxLayout.LINE_AXIS));
		target.setVisible(false);

		final JCheckBox	mp3Audio = new JCheckBox(I18n.getString(MP3AUDIO_LABEL_KEY));
		mp3AudioCheckboxList[index] = mp3Audio;
		final JCheckBox	mpeg4Video = new JCheckBox(I18n.getString(MPEG4VIDEO_LABEL_KEY));
		mpeg4VideoCheckboxList[index] = mpeg4Video;
		applyLabel = new LinkLabel(I18n.getString(APPLY_LABEL_KEY), new Command() {
            public void execute() {
                boolean selected = mp3Audio.isSelected();
                for (int i = 0; i < mp3AudioCheckboxList.length; i++) {
                    mp3AudioCheckboxList[i].setSelected(selected);
                }
                selected = mpeg4Video.isSelected();
                for (int i = 0; i < mpeg4VideoCheckboxList.length; i++) {
                	mpeg4VideoCheckboxList[i].setSelected(selected);
                }
            }
		});
		
		target.add(mp3Audio);
    	target.add(Box.createRigidArea(new Dimension(0, 5)));
		target.add(mpeg4Video);
		target.add(Box.createHorizontalGlue());
		target.add(applyLabel);
		
		setPanelBorder(target, TARGETS_LABEL_KEY);

		return target;
    }
    
    /**
     * This method creates a JPanel for the customer privacy settings. This is the
     * last panel to be added to the tabbed pane panel.
     * 
     * @param index this is the current file number and tabbed pane tab index
     * @return JPanel the newly created privacy panel
     */
    private JPanel addPrivacyPanel(int index) {
    	JPanel	privacy = new JPanel(new GridBagLayout());
    	Grid	grid = new Grid();
    	
    	grid.setAnchor(Grid.westAnchor);
    	grid.setRelativeX();
    	
    	final JCheckBox	privateFileCheckbox = new JCheckBox(I18n.getString(PRIVATE_LABEL_KEY));
        applyLabel = new LinkLabel(I18n.getString(APPLY_LABEL_KEY), new Command() {
            public void execute() {
                boolean selected = privateFileCheckbox.isSelected();
                for (int i = 0; i < privateFileCheckboxList.length; i++) {
                    privateFileCheckboxList[i].setSelected(selected);
                }
            }
        });
        privateFileCheckboxList[index] = privateFileCheckbox;
		privacy.add(privateFileCheckbox, grid);
		privacy.add(applyLabel, grid);
		
		// The Private File checkbox controls the state of the password checkbox and the
		// make public checkbox. Selecting the private file activates the password and
		// make public checkboxes.
		privateFileCheckbox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent event) {
				JCheckBox	box = (JCheckBox)event.getSource();
				boolean		selected = box.isSelected();
				int			i = 0;
				
				for (i = 0; i < privateFileCheckboxList.length; i++) {
					if (box == privateFileCheckboxList[i]) {
						break;
					}
				}
				
				if (!selected) {
					visiblePasswordCheckboxList[i].setSelected(selected);
					makePublicCheckboxList[i].setSelected(selected);
				}
				visiblePasswordCheckboxList[i].setEnabled(!visiblePasswordCheckboxList[i].isEnabled());
				makePublicCheckboxList[i].setEnabled(!makePublicCheckboxList[i].isEnabled());
			}
		});
		
		grid.setInsets(0, 20, 0, 0);
		final JCheckBox	visiblePasswordCheckbox = new JCheckBox(I18n.getString(PASSWORD_LABEL_KEY));
        applyLabel = new LinkLabel(I18n.getString(APPLY_LABEL_KEY), new Command() {
            public void execute() {
                boolean selected = visiblePasswordCheckbox.isSelected();
                for (int i = 0; i < visiblePasswordCheckboxList.length; i++) {
                    visiblePasswordCheckboxList[i].setSelected(selected);
                }
                
            }
        });
        visiblePasswordCheckboxList[index] = visiblePasswordCheckbox;
        
        // The password checkbox controls the enabled state of the password text field.
        visiblePasswordCheckbox.addItemListener(new ItemListener() {
        	public void itemStateChanged(ItemEvent event) {
        		JCheckBox	box = (JCheckBox)event.getSource();
        		int			i = 0;
        		
        		for (i = 0; i < visiblePasswordCheckboxList.length; i++) {
        			if (box == visiblePasswordCheckboxList[i]) {
        				break;
        			}
        		}
        		
        		if (!box.isSelected()) {
        			visiblePasswordFieldList[i].setText("");
        		}
        		visiblePasswordFieldList[i].setEnabled(!visiblePasswordFieldList[i].isEnabled());
        	}
        });
                
		grid.incrY();
		privacy.add(visiblePasswordCheckbox, grid);
		visiblePasswordCheckbox.setEnabled(false);
		visiblePasswordField = new JPasswordField(15);
		visiblePasswordFieldList[index] = visiblePasswordField;
		visiblePasswordField.setEnabled(false);
		grid.setInsets(0, -100, 0, 0);
		grid.setAnchor(Grid.eastAnchor);
		privacy.add(visiblePasswordField, grid);
		privacy.add(applyLabel, grid);

		final JCheckBox	makePublicCheckbox = new JCheckBox(I18n.getString(PUBLIC_LABEL_KEY));
        applyLabel = new LinkLabel(I18n.getString(APPLY_LABEL_KEY), new Command() {
            public void execute() {
                boolean selected = makePublicCheckbox.isSelected();
                for (int i = 0; i < makePublicCheckboxList.length; i++) {
                    makePublicCheckboxList[i].setSelected(selected);
                }
            }
        });
        makePublicCheckboxList[index] = makePublicCheckbox;
        
        // The make public checkbox controls the enabled state of the date/time text field.
        makePublicCheckbox.addItemListener(new ItemListener() {
        	public void itemStateChanged(ItemEvent event) {
        		JCheckBox	box = (JCheckBox)event.getSource();
        		int			i = 0;
        		
        		for (i = 0; i < makePublicCheckboxList.length; i++) {
        			if (box == makePublicCheckboxList[i]) {
        				break;
        			}
        		}
        		
        		makePublicFieldList[i].setEnabled(!makePublicFieldList[i].isEnabled());
        	}
        });
        
		grid.setInsets(0, 20, 0, 0);
		grid.incrY();
		privacy.add(makePublicCheckbox, grid);
		makePublicCheckbox.setEnabled(false);
		
		makePublicField = new JTextField(15);
		Calendar	cal = new GregorianCalendar();
		
		// The date/time field is initialized with the date set to today plus one day.
		Formatter	dateFormatter = new Formatter();
		makePublicField.setText(dateFormatter.format("%4d/%02d/%02d %02d:%02d", cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH) + 1), 
				(cal.get(Calendar.DAY_OF_MONTH) + 1), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)).toString());
		
		makePublicFieldList[index] = makePublicField;
		makePublicField.setEnabled(false);
		privacy.add(makePublicField, grid);
		privacy.add(applyLabel, grid);
		
		setPanelBorder(privacy, PRIVACY_LABEL_KEY);
		
		return privacy;
    }
    
    /**
     * This method checks the password fields to make sure they are filled if the password
     * checkbox is selected.
     * 
     * @param checkboxList array of JCheckBox components controlling password fields
     * @param passwordFields array of String values for the password fields.
     * @return true if selected password fields are filled, else false
     */
    private boolean checkPasswordFields(JCheckBox[] checkboxList, String[] passwordFields) {
    	for (int i = 0; i < passwordFields.length; i++) {
    		if (checkboxList[i].isSelected() && passwordFields[i].trim().equals("")) {
    			JOptionPane.showMessageDialog(null, "One or more password fields are empty.");
    			return false;
    		}
    	}
    	
    	return true;
    }
    
    /**
     * This method checks the date/time fields for the privacy panel, if the make public checkbox
     * is selected.
     * 
     * @param checkboxList array of JCheckBox components for the make public items
     * @param publicFields array of String values from the date/time fields
     * @return true if the date/time fields are valid, otherwise false.
     */
    private boolean checkPublicFields(JCheckBox[] checkboxList, String[] publicFields) {
		Pattern	pat = Pattern.compile("^(\\d{4})/(\\d{1,2})/(\\d{1,2})[ ]+(\\d{1,2}):(\\d{2})$");
	
    	for (int i = 0; i < publicFields.length; i++) {
    		String	field = publicFields[i].trim();
    		
    		if (checkboxList[i].isSelected() && field.equals("")) {
    			JOptionPane.showMessageDialog(null, "One or more date/time fields are empty.");
    			return false;
    		}
    		
    		Matcher	matcher = pat.matcher(field);
    		if (checkboxList[i].isSelected()) {
    			if (!matcher.matches()) {
    				JOptionPane.showMessageDialog(null, "One of more date/time fields are entered incorrectly.");
    				return false;
    			}
    		}
    		else {
    			continue;
    		}
    		
    		Calendar	cal = new GregorianCalendar(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)) - 1,
    								Integer.parseInt(matcher.group(3)), Integer.parseInt(matcher.group(4)), 
    								Integer.parseInt(matcher.group(5)));
    		Calendar	now = new GregorianCalendar();
    		
    		now.setTimeInMillis(System.currentTimeMillis());
    		if (!cal.after(now)) {
    			JOptionPane.showMessageDialog(null, "One or more date/time fields must be later than current date/time.");
    			return false;
    		}
    	}
    	
    	return true;
    }
    
    private void setPanelBorder(JPanel panel, String title) {
        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createTitledBorder(I18n.getString(title)),
                                BorderFactory.createEmptyBorder(2, 2, 2, 2)
                        ),
                        panel.getBorder()
                )
        );
    }
        
    /**
     * There is only one method of interest for the MouseListener interface, so we make use
     * of the MouseAdapter class and only implement the mouseClicked method.
     * 
     * This mouse event pertains to the Distribution set of panels at the bottom of the complete
     * video file form. If the customer opens one of the areas, all the areas are opened. The
     * same is true for closing one of the areas.
     * 
     * @author dsklett
     *
     */
    private class MouseHandler extends MouseAdapter {
        public void mouseClicked(MouseEvent event) {
        	
        	for (Set<JPanel> panels : distributionMap.values()) {
        		for (JPanel panel : panels) {
        			panel.setVisible(!panel.isVisible());
        		}
        	}
        	
        	for (JLabel label : distributionMap.keySet()) {
            	label.setIcon(label.getIcon().equals(Icons.collapsedIcon) ? Icons.expandedIcon : Icons.collapsedIcon);
        	}
        	
        	wizardFrame.pack();
        	mover.positionFrame(true);
        }
    }
    
} // class MetaDataStep
