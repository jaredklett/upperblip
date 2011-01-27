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
import 	java.util.regex.Matcher;
import 	java.util.regex.Pattern;

import 	javax.swing.*;

import 	com.blipnetworks.util.I18n;
import 	com.blipnetworks.util.Command;

import 	com.blipnetworks.upperblip.UpperBlipModel.MapKeys;
import	com.blipnetworks.upperblip.wizard.*;

/**
 *
 *
 * @author Jared Klett
 * @version $Id: MetaDataStep.java,v 1.36 2011/01/27 19:38:53 jklett Exp $
 */

public final class MetaDataStep extends AbstractWizardStep {

// CVS info ////////////////////////////////////////////////////////////////////

    public static final String CVS_REV = "$Revision: 1.36 $";

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
    private static final String	CROSSPOST_WINDOWS_KEY = "meta.crosspostpopup.windows";
    private static final String	CROSSPOST_MAC_KEY = "meta.crosspostpopup.mac";
    private static final String	CROSSUPLOAD_WINDOWS_KEY = "meta.crossuploadpopup.windows";
    private static final String	CROSSUPLOAD_MAC_KEY = "meta.crossuploadpopup.mac";
    private static final String	SELECTALL_LABEL_KEY = "meta.selectall.label";
    
    private static final String	DEFAULT_TEXT = "general.default.text";
    
// Instance variables //////////////////////////////////////////////////////////

    private JTabbedPane		pane = null;
    private UpperBlipModel 	model = null;
    
    private LinkLabel		applyLabel = null;
    
    // These components are used in the Privacy Settings panel
    
	private JButton			completeButton = null;
	private boolean			retry = false;
	
    private JFrame			wizardFrame = null;
    private JPanel			viewPanel = null;
    private MovementHandler	mover = null;
    
    // This map is used to control the display of the distribution section of the
    // form for each video file. Not used for a non-pro account.
    private Map<JLabel, Set<JPanel>>		distributionMap = null;
    // This list contains all the data for each video file to be uploaded.
    private ArrayList<Map<String, Object>>	fileData = null;
    // This map tracks all the cross posting popup menus. Used when the apply all
    // label is selected.
    private Map<JLabel, JPopupMenu>			crosspostPopups = null;
    // This map tracks all the cross uploading popup menus. Used when the apply all
    // label is selected.
    private Map<JLabel, JPopupMenu>			crossuploadPopups = null;
    
// Constructor /////////////////////////////////////////////////////////////////

    public MetaDataStep() {
        super(I18n.getString(TITLE_KEY), I18n.getString(SUMMARY_KEY));
        setIcon(Icons.metadataIcon);
    }

    public void init(WizardModel model) {
        this.model = (UpperBlipModel)model;
        fileData = this.model.getFileData();
    }

    public void prepare() {
    	
    	viewPanel = new JPanel(new GridBagLayout());
    	Grid	viewGrid = new Grid();
    	
    	model.setNextAvailable(false);
        wizardFrame = Main.getMainInstance().getMainFrame();
        mover = Main.getMainInstance().getMover();
        
        pane = new JTabbedPane();
        viewPanel.add(pane, viewGrid);
        
        viewGrid.incrY();
        completeButton = new JButton("Confirm Form Complete");
        viewPanel.add(completeButton, viewGrid);
        
        completeButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent event) {
        		if (!retry) {
        			executeForm();
        		}
        		
        		if (model.isPrivateFiles()) {
        			for (Map<String, Object> map : fileData) {
        				JCheckBox	check = (JCheckBox) map.get(MapKeys.PROTECTED.getKey());
        				if (check != null && check.isSelected()) {
        					if (!checkPasswordField((JPasswordField) map.get(MapKeys.PASSWORD.getKey()))) {
	        					retry = true;
	        					return;
	        				}
        				}
        				JCheckBox	box = (JCheckBox) map.get(MapKeys.MAKEPUBLIC.getKey());
        				if (box != null && box.isSelected()) {
        					if (!checkPublicField((JTextField) map.get(MapKeys.DATETIME.getKey()))) {
        						retry = true;
        						return;
        					}
	        			}
        			}
	        	}
        		setComplete(true);
        	}
        });
        
        // This should never be true, but accidents happen.
		if (fileData.size() <= 0) {
			JOptionPane.showMessageDialog(null, I18n.getString("upload.error.nofiles"));
			System.exit(0);
		}
		
		// Create a tabbed pane with a pane for each video file selected in the previous step.
		// Each pane is composed of a top panel with basic data displayed and an optional distribution
		// panel, displayed only if the customer has a pro-account.
        for (Map<String, Object> formData : fileData) {
        	JPanel	panePanel = new JPanel(new GridBagLayout());
            JPanel	topPanel = new JPanel(new GridBagLayout());
            Grid	paneGrid = new Grid();
            
            panePanel.add(topPanel, paneGrid);
            
        	Grid	topPanelGrid = new Grid();

            topPanelGrid.setInsets(2, 4, 2, 4);
            
            // Display the name of the current video file plus an file icon.
            JLabel	fileLabel = new JLabel(I18n.getString(FILE_LABEL_KEY));
            String	fileName = ((File) formData.get(MapKeys.VIDEOFILE.getKey())).getName();
            JLabel	fileNameLabel = new JLabel(fileName, Icons.getIconForFilename(fileName), SwingConstants.LEFT);
            
            topPanel.add(fileLabel, topPanelGrid.setAnchor(Grid.northEastAnchor));
            topPanel.add(fileNameLabel, topPanelGrid.setAnchorIncrX(Grid.northWestAnchor));
            
            // Add the title field. If the customer leaves the title field empty, it is later
            // filled in with the name of the video file minus the file extension.
            final JTextField	titleField = (JTextField) formData.get(MapKeys.TITLE.getKey());
            titleField.setColumns(10);
            applyLabel = new LinkLabel(I18n.getString(APPLY_LABEL_KEY), new Command() {
                public void execute() {
                    String title = titleField.getText();
                    for (Map<String, Object> map : fileData) {
                    	JTextField	field = (JTextField) map.get(MapKeys.TITLE.getKey());
                    	field.setText(title);
                    }
                }
            });
            
            JLabel titleLabel = new JLabel(I18n.getString(TITLE_LABEL_KEY));
            topPanel.add(titleLabel, topPanelGrid.setAnchorIncrY(Grid.northEastAnchor));
            topPanelGrid.setFill(Grid.horizontalFill);
            topPanel.add(titleField, topPanelGrid.setAnchorIncrX(Grid.northWestAnchor));
            topPanel.add(applyLabel, topPanelGrid.incrX());
            
            // Add the video description area. The description area is embedded in a scroll pane,
            // used if the text overflows the visible field.
            // The area is set for word and line wrapping.
            final JTextArea	description = (JTextArea) formData.get(MapKeys.DESCRIPTION.getKey());
            description.setRows(5);
            description.setColumns(10);
            description.setLineWrap(true);
            description.setWrapStyleWord(true);
            
            JScrollPane jsp = new JScrollPane(description);
            jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            
            applyLabel = new LinkLabel(I18n.getString(APPLY_LABEL_KEY), new Command() {
                public void execute() {
                    String desc = description.getText();
                    for (Map<String, Object> map : fileData) {
                    	((JTextArea) map.get(MapKeys.DESCRIPTION.getKey())).setText(desc);
                    }
                }
            });
            
            JLabel descLabel = new JLabel(I18n.getString(DESC_LABEL_KEY));
            topPanelGrid.setFill(Grid.noneFill);
            topPanel.add(descLabel, topPanelGrid.setAnchorIncrY(Grid.northEastAnchor));
            topPanelGrid.setFill(Grid.horizontalFill);
            topPanel.add(jsp, topPanelGrid.setAnchorIncrX(Grid.northWestAnchor));
            topPanel.add(applyLabel, topPanelGrid.incrX());
            
            // Add the drop-down for any thumbnail files selected in the previous step.
            final JComboBox	thumbnails = (JComboBox) formData.get(MapKeys.THUMBNAIL.getKey());
        	for (String item : model.getImageFilenames()) {
        		thumbnails.addItem(item);
        	}

            applyLabel = new LinkLabel(I18n.getString(APPLY_LABEL_KEY), new Command() {
                public void execute() {
                    int index = thumbnails.getSelectedIndex();
                    for (Map<String, Object> map : fileData) {
                    	((JComboBox) map.get(MapKeys.THUMBNAIL.getKey())).setSelectedIndex(index);
                    }
                }
            });
            
            JLabel thumbLabel = new JLabel(I18n.getString(THUMB_LABEL_KEY));
            topPanelGrid.setFill(Grid.noneFill);
            topPanel.add(thumbLabel, topPanelGrid.setAnchorIncrY(Grid.eastAnchor));
            topPanel.add(thumbnails, topPanelGrid.setAnchorIncrX(Grid.westAnchor));
            topPanel.add(applyLabel, topPanelGrid.incrX());
            
            // Add the drop-down for all the available licenses. Data obtained from blip.tv.
            final JComboBox	licenses = (JComboBox) formData.get(MapKeys.LICENSE.getKey());
            applyLabel = new LinkLabel(I18n.getString(APPLY_LABEL_KEY), new Command() {
                public void execute() {
                    int index = licenses.getSelectedIndex();
                    for (Map<String, Object> map : fileData) {
                    	((JComboBox) map.get(MapKeys.LICENSE.getKey())).setSelectedIndex(index);
                    }
                }
            });
            
            JLabel licenseLabel = new JLabel(I18n.getString(LICENSE_LABEL_KEY));
            topPanel.add(licenseLabel, topPanelGrid.setAnchorIncrY(Grid.eastAnchor));
            topPanel.add(licenses, topPanelGrid.setAnchorIncrX(Grid.westAnchor));
            topPanel.add(applyLabel, topPanelGrid.incrX());
            
            // Add the tags field.
            final JTextField	tagsField = (JTextField) formData.get(MapKeys.TAGS.getKey());
            tagsField.setColumns(10);
            applyLabel = new LinkLabel(I18n.getString(APPLY_LABEL_KEY), new Command() {
                public void execute() {
                    String tags = tagsField.getText();
                    for (Map<String, Object> map : fileData) {
                    	((JTextField) map.get(MapKeys.TAGS.getKey())).setText(tags);
                    }
                }
            });
            
            JLabel tagsLabel = new JLabel(I18n.getString(TAGS_LABEL_KEY));
            topPanel.add(tagsLabel, topPanelGrid.setAnchorIncrY(Grid.northEastAnchor));
            topPanelGrid.setFill(Grid.horizontalFill);
            topPanel.add(tagsField, topPanelGrid.setAnchorIncrX(Grid.northWestAnchor));
            topPanel.add(applyLabel, topPanelGrid.setAnchorIncrX(Grid.centerAnchor));
            
            // Add the drop-down containing the available file categories. Data obtained 
            // from blip.tv.
            final JComboBox	categories = (JComboBox) formData.get(MapKeys.CATEGORY.getKey());
            categories.setSelectedItem(I18n.getString(DEFAULT_TEXT));
            applyLabel = new LinkLabel(I18n.getString(APPLY_LABEL_KEY), new Command() {
                public void execute() {
                    int index = categories.getSelectedIndex();
                    for (Map<String, Object> map : fileData) {
                    	((JComboBox) map.get(MapKeys.CATEGORY.getKey())).setSelectedIndex(index);
                    }
                }
            });
            
            JLabel categoryLabel = new JLabel(I18n.getString(CATEGORY_LABEL_KEY));
            topPanelGrid.setFill(Grid.noneFill);
            topPanel.add(categoryLabel, topPanelGrid.setAnchorIncrY(Grid.eastAnchor));
            topPanel.add(categories, topPanelGrid.setAnchorIncrX(Grid.westAnchor));
            topPanel.add(applyLabel, topPanelGrid.incrX());
            
            // Add the drop-down for film ratings. Data obtained from blip.tv.
            final JComboBox	ratings = (JComboBox) formData.get(MapKeys.RATING.getKey());
            applyLabel = new LinkLabel(I18n.getString(APPLY_LABEL_KEY), new Command() {
                public void execute() {
                    int index = ratings.getSelectedIndex();
                    for (Map<String, Object> map : fileData) {
                    	((JComboBox) map.get(MapKeys.RATING.getKey())).setSelectedIndex(index);
                    }
                }
            });
            JLabel ratingLabel = new JLabel(I18n.getString(RATING_LABEL_KEY));
            topPanelGrid.setFill(Grid.noneFill);
            topPanel.add(ratingLabel, topPanelGrid.setAnchorIncrY(Grid.eastAnchor));
            topPanel.add(ratings, topPanelGrid.setAnchorIncrX(Grid.westAnchor));
            topPanel.add(applyLabel, topPanelGrid.incrX());

            // Add a check box for explicit content.
            final JCheckBox	explicit = (JCheckBox) formData.get(MapKeys.EXPLICIT.getKey());
            explicit.setText(I18n.getString(EXPLICIT_LABEL_KEY));
            applyLabel = new LinkLabel(I18n.getString(APPLY_LABEL_KEY), new Command() {
                public void execute() {
                    boolean selected = explicit.isSelected();
                    for (Map<String, Object> map : fileData) {
                    	((JCheckBox) map.get(MapKeys.EXPLICIT.getKey())).setSelected(selected);
                    }
                }
            });
            topPanelGrid.setAnchorIncrY(Grid.eastAnchor);            
            topPanelGrid.setAnchorIncrX(Grid.westAnchor);
            topPanel.add(explicit, topPanelGrid);
            topPanel.add(applyLabel, topPanelGrid.incrX());

            // Add a drop-down for film languages. Data obtained from blip.tv.
            final JComboBox	languages = (JComboBox) formData.get(MapKeys.LANGUAGE.getKey());
            languages.setSelectedItem("English");
            applyLabel = new LinkLabel(I18n.getString(APPLY_LABEL_KEY), new Command() {
                public void execute() {
                    int index = languages.getSelectedIndex();
                    for (Map<String, Object> map : fileData) {
                    	((JComboBox) map.get(MapKeys.LANGUAGE.getKey())).setSelectedIndex(index);
                    }
                }
            });
            JLabel languageLabel = new JLabel(I18n.getString(LANGUAGE_LABEL_KEY));
            topPanelGrid.setFill(Grid.noneFill);
            topPanel.add(languageLabel, topPanelGrid.setAnchorIncrY(Grid.eastAnchor));
            topPanel.add(languages, topPanelGrid.setAnchorIncrX(Grid.westAnchor));
            topPanel.add(applyLabel, topPanelGrid.incrX());

            JLabel	distribution = null;
            
            paneGrid.setFill(Grid.horizontalFill);
            paneGrid.setRelativeY();
            
            // If the customer account is a pro-account, then the distribution area is added to the
            // form.
            if (model.isDistribution()) {
            	if (distributionMap == null) {
            		distributionMap = new HashMap<JLabel, Set<JPanel>>();
            	}
            	Set<JPanel>	panelSet = new HashSet<JPanel>();

            	// This label heads the distribution set of panels and has a triangular arrow icon which
            	// is active and used to make this area visible or not. A mouse handler is attached to this
            	// distribution label to manage opening and closing the section.
            	distribution = new JLabel(I18n.getString(DIST_LABEL_KEY), Icons.collapsedIcon, JLabel.HORIZONTAL);
            	distribution.addMouseListener(new MouseHandler());
            	distributionMap.put(distribution, panelSet);
            	
                topPanelGrid.incrY();
                topPanelGrid.setX(0);
                topPanel.add(distribution, topPanelGrid);
                
                addCrosspostingPanel(formData, panePanel, paneGrid, panelSet);
                addCrossuploadingPanel(formData, panePanel, paneGrid, panelSet);
                
                JPanel	targetPanel = addTargetsPanel(formData);
	                
                targetPanel.setVisible(false);
                panelSet.add(targetPanel);
                panePanel.add(targetPanel, paneGrid);
                
            	JPanel	privacyPanel = addPrivacyPanel(formData);
	                
	            privacyPanel.setVisible(false);
	            panelSet.add(privacyPanel);
	            panePanel.add(privacyPanel, paneGrid);
            }
            
            String	tabName = ((File) formData.get(MapKeys.VIDEOFILE.getKey())).getName();
            // Very long video file names are contracted down to twenty characters, with an ellipsis
            // inserted in the middle.
            if (tabName.length() > 20) {
            	tabName = tabName.substring(0, 10) + Character.toString('\u2026') + tabName.substring(tabName.length() - 9);
            }
            pane.addTab(tabName, panePanel);
        }

        setView(viewPanel);
        wizardFrame.pack();
        mover.positionFrame(true);
    }

    public void executeForm() {
    	
    	// If the title fields are empty, then the video file name is inserted
    	// minus the file extension.
    	for (Map<String, Object> map : fileData) {
    		JTextField	field = (JTextField) map.get(MapKeys.TITLE.getKey());
    		String	title = field.getText().trim();
    		if (title.equals("")) {
                String name = ((File) map.get(MapKeys.VIDEOFILE.getKey())).getName();
                field.setText(name.substring(0, name.lastIndexOf(".")));
    		}
    	}
    }
    
    public void applyState() {
    }
    
    public Dimension getPreferredSize() {
        return pane.getPreferredSize();
    }
            
    /**
     * This method handles the Conversion Targets panel under the Distribution label
     * 
     * @return
     */
    private JPanel addTargetsPanel(Map<String, Object> formData) {
    	JPanel	target = new JPanel();
    	
		target.setLayout(new BoxLayout(target, BoxLayout.LINE_AXIS));
		target.setVisible(false);

		final JCheckBox	mp3Audio = (JCheckBox) formData.get(MapKeys.MP3AUDIO.getKey());
		mp3Audio.setText(I18n.getString(MP3AUDIO_LABEL_KEY));
		
		final JCheckBox	mpeg4Video = (JCheckBox) formData.get(MapKeys.M4VIDEO.getKey());
		mpeg4Video.setText(I18n.getString(MPEG4VIDEO_LABEL_KEY));
		
		applyLabel = new LinkLabel(I18n.getString(APPLY_LABEL_KEY), new Command() {
            public void execute() {
                boolean audioSelected = mp3Audio.isSelected();
                boolean	videoSelected = mpeg4Video.isSelected();
                for (Map<String, Object> map : fileData) {
                	((JCheckBox) map.get(MapKeys.MP3AUDIO.getKey())).setSelected(audioSelected);
                	((JCheckBox) map.get(MapKeys.M4VIDEO.getKey())).setSelected(videoSelected);
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
     * This method creates the panel containing the list of cross posting sites. The list
     * is implemented as a popup menu containing JCheckBoxMenuItem components. The top element
     * is a "Select All" entry that when selected negates all the other entries in the popup
     * menu. The panel uses the Box layout manager. The popup menu is attached to a JLabel
     * component in the panel.
     * 
     * @param stepData
     * @param panePanel
     * @param paneGrid
     * @param panelSet
     */
    @SuppressWarnings("unchecked")
	private void addCrosspostingPanel(Map<String, Object> formData, JPanel panePanel, Grid paneGrid, Set<JPanel> panelSet) {
        ArrayList<JCheckBoxMenuItem>	crosspostings = (ArrayList<JCheckBoxMenuItem>) formData.get(MapKeys.CROSSPOSTING.getKey());
        
        if (crosspostings.size() == 0) {
        	return;
        }
        
        JPanel				crosspostingPanel = new JPanel();
        final JPopupMenu	popup = (JPopupMenu) formData.get(MapKeys.CROSSPOST_POPUP.getKey());
        
        if (crosspostPopups == null) {
        	crosspostPopups = new HashMap<JLabel, JPopupMenu>();
        }
        
        crosspostingPanel.setLayout(new BoxLayout(crosspostingPanel, BoxLayout.LINE_AXIS));
        crosspostingPanel.setVisible(false);
		panelSet.add(crosspostingPanel);
        
        LinkLabel applyBlogLabel = new LinkLabel(I18n.getString(APPLY_LABEL_KEY), new Command() {
            public void execute() {
            	ArrayList<JCheckBoxMenuItem>	crossposts = null;
            	for (Map<String, Object> map : fileData) {
            		crossposts = (ArrayList<JCheckBoxMenuItem>) map.get(MapKeys.CROSSPOSTING.getKey());
            		MenuElement[]	elements = popup.getSubElements();
            		for (int i = 1; i < elements.length; i++) {
            			JCheckBoxMenuItem	item = (JCheckBoxMenuItem) elements[i];
            			crossposts.get(i - 1).setSelected(item.getState());
            		}
            	}
            }
        });

        JCheckBoxMenuItem	selectAll = new JCheckBoxMenuItem(I18n.getString(SELECTALL_LABEL_KEY));
        
        selectAll.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent event) {
        		MenuElement[] elements = popup.getSubElements();
        		for (MenuElement element : elements) {
        			JCheckBoxMenuItem	item = (JCheckBoxMenuItem) element;
        			item.setState(!item.getState());
        		}
        	}
        });
        
		popup.add(selectAll);
		popup.addSeparator();
        for (JCheckBoxMenuItem box : crosspostings) {
        	popup.add(box);
        }

        JLabel	addView = new JLabel(I18n.getString((model.isMacOS())? CROSSPOST_MAC_KEY : CROSSPOST_WINDOWS_KEY));
        
		crosspostingPanel.add(addView);
		crosspostPopups.put(addView, popup);
		addView.setForeground(Color.blue);
		addView.addMouseListener(new PopupHandler());
		
        crosspostingPanel.add(Box.createHorizontalGlue());
        crosspostingPanel.add(applyBlogLabel);
        
        setPanelBorder(crosspostingPanel, BLOGS_LABEL_KEY);
        panePanel.add(crosspostingPanel, paneGrid);
    }

    /**
     * This method is exactly the same as addCrosspostingPanel() above, except for the difference
     * in various variables. A TODO could be trying to merge the two methods into a single one. The
     * only problem is the potential length of the parameter list (too long).
     * 
     * @param stepData
     * @param panePanel
     * @param paneGrid
     * @param panelSet
     */
	@SuppressWarnings("unchecked")
	private void addCrossuploadingPanel(Map<String, Object> formData, JPanel panePanel, Grid paneGrid, Set<JPanel> panelSet) {
        ArrayList<JCheckBoxMenuItem>	crossuploadings = (ArrayList<JCheckBoxMenuItem>) formData.get(MapKeys.CROSSUPLOADING.getKey());
        
        if (crossuploadings.size() == 0) {
        	return;
        }
        
        JPanel				crossuploadingPanel = new JPanel();
        final JPopupMenu	popup = (JPopupMenu) formData.get(MapKeys.CROSSUPLOAD_POPUP.getKey());
        
        if (crossuploadPopups == null) {
        	crossuploadPopups = new HashMap<JLabel, JPopupMenu>();
        }
        
        crossuploadingPanel.setLayout(new BoxLayout(crossuploadingPanel, BoxLayout.LINE_AXIS));
        crossuploadingPanel.setVisible(false);
		panelSet.add(crossuploadingPanel);
        
        LinkLabel applyBlogLabel = new LinkLabel(I18n.getString(APPLY_LABEL_KEY), new Command() {
            public void execute() {
            	ArrayList<JCheckBoxMenuItem>	crossuploads = null;
            	for (Map<String, Object> map : fileData) {
            		crossuploads = (ArrayList<JCheckBoxMenuItem>) map.get(MapKeys.CROSSUPLOADING.getKey());
            		MenuElement[]	elements = popup.getSubElements();
            		for (int i = 1; i < elements.length; i++) {
            			JCheckBoxMenuItem	item = (JCheckBoxMenuItem) elements[i];
            			crossuploads.get(i - 1).setSelected(item.getState());
            		}
            	}
            }
        });

        JCheckBoxMenuItem	selectAll = new JCheckBoxMenuItem(I18n.getString(SELECTALL_LABEL_KEY));
        
        selectAll.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent event) {
        		MenuElement[] elements = popup.getSubElements();
        		for (MenuElement element : elements) {
        			JCheckBoxMenuItem	item = (JCheckBoxMenuItem) element;
        			item.setState(!item.getState());
        		}
        	}
        });
        
		popup.add(selectAll);
		popup.addSeparator();
        for (JCheckBoxMenuItem box : crossuploadings) {
        	popup.add(box);
        }

        JLabel	addView = new JLabel(I18n.getString((model.isMacOS())? CROSSUPLOAD_MAC_KEY : CROSSUPLOAD_WINDOWS_KEY));
        
        crossuploadingPanel.add(addView);
        crossuploadPopups.put(addView, popup);
		addView.setForeground(Color.blue);
		addView.addMouseListener(new PopupHandler());
		
		crossuploadingPanel.add(Box.createHorizontalGlue());
		crossuploadingPanel.add(applyBlogLabel);
        
        setPanelBorder(crossuploadingPanel, XUPLOADS_LABEL_KEY);
        panePanel.add(crossuploadingPanel, paneGrid);
    }

    /**
     * This method creates a JPanel for the customer privacy settings. This is the
     * last panel to be added to the tabbed pane panel.
     * 
     * @param index this is the current file number and tabbed pane tab index
     * @return JPanel the newly created privacy panel
     */
    private JPanel addPrivacyPanel(Map<String, Object> formData) {
    	JPanel	privacyPanel = new JPanel(new GridBagLayout());
    	Grid	grid = new Grid();
    	
    	grid.setAnchor(Grid.westAnchor);
    	grid.setFill(Grid.noneFill);
    	grid.setRelativeX();
    	
    	final JCheckBox	privateFile = (JCheckBox) formData.get(MapKeys.PRIVACY.getKey());
    	privateFile.setText(I18n.getString(PRIVATE_LABEL_KEY));
		privacyPanel.add(privateFile, grid);
		
        applyLabel = new LinkLabel(I18n.getString(APPLY_LABEL_KEY), new Command() {
            public void execute() {
                boolean selected = privateFile.isSelected();
                for (Map<String, Object> map : fileData) {
                	((JCheckBox) map.get(MapKeys.PRIVACY.getKey())).setSelected(selected);
                }
            }
        });
        grid.setAnchor(Grid.eastAnchor);
        grid.setRemainderX();
		privacyPanel.add(applyLabel, grid);
		grid.setColumnSpan(1);
		
		// The Private File checkbox controls the state of the password checkbox and the
		// make public checkbox. Selecting the private file activates the password and
		// make public checkboxes. It also enables and disables the associated text fields.
		privateFile.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent event) {
				boolean		selected = ((JCheckBox) event.getSource()).isSelected();
				for (Map<String, Object> map : fileData) {
					if (selected) {
						((JCheckBox) map.get(MapKeys.PROTECTED.getKey())).setEnabled(selected);
						((JCheckBox) map.get(MapKeys.MAKEPUBLIC.getKey())).setEnabled(selected);
						continue;
					}
					else {
						((JCheckBox) map.get(MapKeys.PROTECTED.getKey())).setSelected(selected);
						((JCheckBox) map.get(MapKeys.PROTECTED.getKey())).setEnabled(selected);
						((JPasswordField) map.get(MapKeys.PASSWORD.getKey())).setText("");
						((JCheckBox) map.get(MapKeys.MAKEPUBLIC.getKey())).setSelected(selected);
						((JCheckBox) map.get(MapKeys.MAKEPUBLIC.getKey())).setEnabled(selected);
					}
				}
			}
		});
		
		grid.setAnchor(Grid.westAnchor);
		grid.setInsets(0, 20, 0, 0);
		grid.incrY();
		
		// The privacy password is added next.
		final JCheckBox	visiblePassword = (JCheckBox) formData.get(MapKeys.PROTECTED.getKey());
		visiblePassword.setText(I18n.getString(PASSWORD_LABEL_KEY));
		visiblePassword.setEnabled(false);
		privacyPanel.add(visiblePassword, grid);
		
        applyLabel = new LinkLabel(I18n.getString(APPLY_LABEL_KEY), new Command() {
            public void execute() {
                boolean selected = visiblePassword.isSelected();
                for (Map<String, Object> map : fileData) {
					((JCheckBox) map.get(MapKeys.PROTECTED.getKey())).setSelected(selected);
                }
            }
        });
        
        // The password checkbox controls the enabled state of the password text field.
        visiblePassword.addItemListener(new ItemListener() {
        	public void itemStateChanged(ItemEvent event) {
        		JCheckBox	box = (JCheckBox) event.getSource();
        		for (Map<String, Object> map : fileData) {
        			if (box == (JCheckBox) map.get(MapKeys.PROTECTED.getKey())) {
        				((JTextField) map.get(MapKeys.PASSWORD.getKey())).setEnabled(box.isSelected());
        				break;
        			}
        		}
        	}
        });
        
        
		JPasswordField	visiblePasswordField = (JPasswordField) formData.get(MapKeys.PASSWORD.getKey());
		visiblePasswordField.setColumns(15);
		visiblePasswordField.setEnabled(false);
		grid.setInsets(0, -100, 0, 0);
		grid.setAnchor(Grid.eastAnchor);
		privacyPanel.add(visiblePasswordField, grid);
		privacyPanel.add(applyLabel, grid);

		final JCheckBox	makePublic = (JCheckBox) formData.get(MapKeys.MAKEPUBLIC.getKey());
		makePublic.setText(I18n.getString(PUBLIC_LABEL_KEY));
        applyLabel = new LinkLabel(I18n.getString(APPLY_LABEL_KEY), new Command() {
            public void execute() {
                boolean selected = makePublic.isSelected();
                for (Map<String, Object> map : fileData) {
                	((JCheckBox) map.get(MapKeys.MAKEPUBLIC.getKey())).setSelected(selected);
                }
            }
        });
        
        // The make public checkbox controls the enabled state of the date/time text field.
        makePublic.addItemListener(new ItemListener() {
        	public void itemStateChanged(ItemEvent event) {
        		JCheckBox	box = (JCheckBox) event.getSource();
        		for (Map<String, Object> map : fileData) {
        			if (box == (JCheckBox) map.get(MapKeys.MAKEPUBLIC.getKey())) {
        				((JTextField) map.get(MapKeys.DATETIME.getKey())).setEnabled(box.isSelected());
        				break;
        			}
        		}
        	}
        });
        
		grid.setInsets(0, 20, 0, 0);
		grid.incrY();
		privacyPanel.add(makePublic, grid);
		makePublic.setEnabled(false);
		
		Calendar	cal = new GregorianCalendar();
		
		JTextField	makePublicField = (JTextField) formData.get(MapKeys.DATETIME.getKey());
		makePublicField.setColumns(15);
		// The date/time field is initialized with the date set to today plus one day.
		Formatter	dateFormatter = new Formatter();
		makePublicField.setText(dateFormatter.format("%4d/%02d/%02d %02d:%02d", cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH) + 1), 
				(cal.get(Calendar.DAY_OF_MONTH) + 1), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)).toString());
		
		makePublicField.setEnabled(false);
		privacyPanel.add(makePublicField, grid);
		privacyPanel.add(applyLabel, grid);
		
		setPanelBorder(privacyPanel, PRIVACY_LABEL_KEY);
		
		return privacyPanel;
    }
    
    /**
     * This method checks the password fields to make sure they are filled if the password
     * checkbox is selected.
     * 
     * @param checkboxList array of JCheckBox components controlling password fields
     * @param passwordFields array of String values for the password fields.
     * @return true if selected password fields are filled, else false
     */
    private boolean checkPasswordField(JPasswordField passwordField) {
    	if (passwordField.getPassword().length == 0) {
    		JOptionPane.showMessageDialog(null, "One or more password fields are empty.");
    		return false;
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
    private boolean checkPublicField(JTextField dateTime) {
		Pattern	pat = Pattern.compile("^(\\d{4})/(\\d{1,2})/(\\d{1,2})[ ]+(\\d{1,2}):(\\d{2})$");
	    Matcher	matcher = pat.matcher(dateTime.getText());
	    
    	if (!matcher.matches()) {
    		JOptionPane.showMessageDialog(null, "One of more date/time fields are entered incorrectly.");
    		return false;
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
    
    /**
     * This mouse handler is used to display the popup menus associated with the cross posting
     * and cross upload sections of the form. Only the mouse clicked method is of interest and
     * displays the popup menu.
     * 
     * @author dsklett
     *
     */
    private final class PopupHandler extends MouseAdapter {
    	
    	public void mouseClicked(MouseEvent event) {
    		JLabel	popupLabel = (JLabel) event.getSource();
    		
        	if (crosspostPopups != null) {
	        	for (JLabel label : crosspostPopups.keySet()) {
	        		if (label.equals(popupLabel)) {
	        			JPopupMenu popup = crosspostPopups.get(label);
	        			popup.show(label, event.getX(), event.getY());
	                    return;
	        		}
	        	}
        	}
        	
        	if (crossuploadPopups != null) {
	        	for (JLabel label : crossuploadPopups.keySet()) {
	        		if (label.equals(popupLabel)) {
	        			crossuploadPopups.get(label).show(label, event.getX(), event.getY());
	                    return;
	        		}
	        	}
        	}
    		
    	}
    }
    /*
    private final class PopupListener extends MouseAdapter {
    	
    	private JPopupMenu	popup = null;
    	
        public void mousePressed(MouseEvent e) {
        	JLabel	addView = (JLabel) e.getSource();
        	
        	if (crosspostPopups != null) {
	        	for (JLabel label : crosspostPopups.keySet()) {
	        		if (label.equals(addView)) {
	        			popup = crosspostPopups.get(label);
	                    maybeShowPopup(e);
	                    return;
	        		}
	        	}
        	}
        	
        	if (crossuploadPopups != null) {
	        	for (JLabel label : crossuploadPopups.keySet()) {
	        		if (label.equals(addView)) {
	        			popup = crossuploadPopups.get(label);
	                    maybeShowPopup(e);
	                    return;
	        		}
	        	}
        	}
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
*/
} // class MetaDataStep
