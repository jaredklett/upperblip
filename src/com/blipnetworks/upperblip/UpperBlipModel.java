/* 
 * @(#)UpperBlipModel.java
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

import 	java.io.*;
import 	java.util.*;

import	com.blipnetworks.upperblip.wizard.*;
import	com.blipnetworks.upperblip.wizard.models.*;
import com.blipnetworks.util.MetadataLoader;

import 	org.apache.commons.httpclient.Cookie;

import javax.swing.*;

/**
 * @author Jared Klett
 * @version $Id: UpperBlipModel.java,v 1.23 2011/01/27 19:38:53 jklett Exp $
 */

public final class UpperBlipModel extends StaticModel /*implements HelpBroker*/ {
	
    public static final String CVS_REV = "$Revision: 1.23 $";

    /**
     * This enum class represents each Swing component present in the form frame
     * in each tabbed pane panel, for each video file to be uploaded. Each enum
     * value has a specific class assigned. These enum values are used as keys in
     * the HashMap constructed for each video file, that are in turn added to an
     * ArrayList whose size is the same as the number of video files to be uploaded.
     * The class argument is used below to instantiate each form component.
     * 
     * @author dsklett
     *
     */
	public enum MapKeys {
		VIDEOFILE("videofile", File.class),
		TITLE("title", JTextField.class),
		DESCRIPTION("description", JTextArea.class),
		THUMBNAIL("thumbnail", JComboBox.class),
		LICENSE("license", JComboBox.class),
		TAGS("tags", JTextField.class),
		CATEGORY("category", JComboBox.class),
		RATING("rating", JComboBox.class),
		EXPLICIT("explicit", JCheckBox.class),
		LANGUAGE("language", JComboBox.class),
		MP3AUDIO("mp3audio", JCheckBox.class),
		M4VIDEO("m4video", JCheckBox.class),
		PRIVACY("privacy",JCheckBox.class),
		PROTECTED("protected", JCheckBox.class),
		PASSWORD("password", JPasswordField.class),
		MAKEPUBLIC("makepublic", JCheckBox.class),
		DATETIME("datetime", JTextField.class),
		CROSSPOSTING("crossposting", ArrayList.class),				// each list entry contains a JCheckBoxMenuItem
		CROSSPOST_POPUP("crosspostpopup", JPopupMenu.class),		// popup menu contains the above JCheckBoxMenuItem objects
		CROSSUPLOADING("crossuploading", ArrayList.class),			// each list entry contains a JCheckBoxMenuItem
		CROSSUPLOAD_POPUP("crossuploadpopup", JPopupMenu.class);	// popup menu contains the above JCheckBoxMenuItem objects
		
		private String		key = null;
		private Class<?>	type = null;
		
		private MapKeys(String key, Class<?> type) {
			this.key = key;
			this.type = type;
		}
						
		public String getKey() {
			return key;
		}
		
		public Class<?> getType() {
			return type;
		}
	}

	private static final String	NONE_TEXT = "general.none.text";
	
    private List<File> 			imageFiles;
    private ArrayList<String> 	imageFilenames;
    private Map<String, String>	crossposts = null;
    private Map<String, String>	crossuploads = null;
    private String 				username;
    private String 				password;
    private boolean 			remember;
    private Map<String, File> 	thumbnailFileLookup;
    private Cookie 				authCookie;
    private boolean				distribution = false;
    private boolean				privateFiles = false;
    private boolean				macOS = false;

    // This list contains an entry for each video file to be processed and uploaded.
    // The map contained in each entry is constructed using the enum values found in the
    // MapKeys enum class shown above.
    private ArrayList<Map<String, Object>>	fileData = new ArrayList<Map<String, Object>>();
    // This list contains the name of files submitted by the FileDropStep object, and is used
    // to prevent the entry of duplicate files.
    private ArrayList<String>				files = new ArrayList<String>();
    
    public UpperBlipModel() {
    	super();
    }
    
    /**
     * This method is called from the AuthDialog object after the metadata is downloaded
     * from blip. This data is added to the fileData map as each video file is accessed
     * from the FileDrop object.
     */
    public void initializeModel() {
    	
    	if (MetadataLoader.privacySettings.size() > 0) {
    		distribution = true;
    		privateFiles = true;
    	}
    	
    	// There is a one-to-one relation between elements in this list and the list
    	// used to construct the popup menu for crossposting.
    	crossposts = MetadataLoader.blogs;
    	
    	// There is a one-to-one relation between elements in this list and the list
    	// used to construct the popup menu for crossuploading.
    	crossuploads = MetadataLoader.crossuploads;
    }
    
    public boolean isDistribution() {
    	return distribution;
    }
    
    public boolean isPrivateFiles() {
    	return privateFiles;
    }
    
    public boolean isMacOS() {
    	return macOS;
    }
    
    /**
     * This method is called from the FileDropStep for each video file processed.
     * Each JComponent is instantiated and added to the map for the current video
     * file.
     * 
     * @param file
     */
    public void addFileData(File file) {
    	String	fileName = file.getName();
    	
    	// Do not add duplicate files
    	if (files.contains(fileName)) {
    		return;
    	}
    	files.add(fileName);
    	
    	Map<String, Object>	stepData = new HashMap<String, Object>();
    	
    	// Instantiate all the form components
    	try {
	    	for (MapKeys key : MapKeys.values()) {
	    		if (key.getKey().equals(MapKeys.VIDEOFILE.getKey())) {
	    			continue;
	    		}
	    		stepData.put(key.getKey(), (Object) key.getType().newInstance());
	    	}
    	} catch (IllegalAccessException e) {
    		// should never happen
    		// if it does, let the uncaughtException handler in Main take care of it.
    	} catch (InstantiationException e) {
    		// should never happen
    		// if it does, let the uncaughtException handler in Main take care of it.
		}
    	
    	addFormData(stepData);
    	
    	stepData.put(MapKeys.VIDEOFILE.getKey(), file);
    	fileData.add(stepData);
    }
    
    public ArrayList<Map<String, Object>> getFileData() {
    	return fileData;
    }
    
    /**
     * This method preloads certain form fields that are the same for all the video
     * files selected.
     * 
     * @param stepData
     */
    @SuppressWarnings("unchecked")
	private void addFormData(Map<String, Object> stepData) {
    	JComboBox	licenses = (JComboBox) stepData.get(MapKeys.LICENSE.getKey());
    	
    	for (String license : MetadataLoader.licenses.keySet()) {
    		licenses.addItem(license);
    	}
    	
    	JComboBox	categories = (JComboBox) stepData.get(MapKeys.CATEGORY.getKey());
    	
    	for (String category : MetadataLoader.categories.keySet()) {
    		categories.addItem(category);
    	}
    	
    	JComboBox	languages = (JComboBox) stepData.get(MapKeys.LANGUAGE.getKey());
    	
    	for (String	language : MetadataLoader.languages.keySet()) {
    		languages.addItem(language);
    	}
    	
    	JComboBox	ratings = (JComboBox) stepData.get(MapKeys.RATING.getKey());
    	
    	for (String rating : MetadataLoader.ratings.keySet()) {
    		ratings.addItem(rating);
    	}
    	
    	if (crossposts.size() > 0) {
    		ArrayList<JCheckBoxMenuItem>	crosspostings = (ArrayList<JCheckBoxMenuItem>) stepData.get(MapKeys.CROSSPOSTING.getKey());
    	
    		for (String crosspost : crossposts.keySet()) {
    			crosspostings.add(new JCheckBoxMenuItem(crosspost));
    		}
    	}
    	
    	if (crossuploads.size() > 0) {
    		ArrayList<JCheckBoxMenuItem>	uploads = (ArrayList<JCheckBoxMenuItem>) stepData.get(MapKeys.CROSSUPLOADING.getKey());
    		
    		for (String upload : crossuploads.keySet()) {
    			uploads.add(new JCheckBoxMenuItem(upload));
    		}
    	}
    }
    
    // Accessors //////////////////////////////////////////////////////////////////

    public boolean isRemembered() {
        return remember;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
    
    public List<File> getImageFiles() {
        return imageFiles;
    }

    public ArrayList<String> getImageFilenames() {
        return imageFilenames;
    }
    
    public Map<String, File> getThumbNails() {
    	return thumbnailFileLookup;
    }

    public Map<String, String> getCrossposts() {
    	return crossposts;
    }
    
    public Map<String, String> getCrossuploads() {
    	return crossuploads;
    }
    
    public Cookie getAuthCookie() {
        return authCookie;
    }

// Mutators ///////////////////////////////////////////////////////////////////

    public void setRemembered(boolean remember) {
        this.remember = remember;
    }

    public void setUsername(String username) {
        this.username = (username == null)? "" : username;
    }

    public void setPassword(String password) {
        this.password = (password == null)? "" : password;
    }
    
    public void setMacOS(boolean enable) {
    	macOS = enable;
    }
    public void setPreviousAvailable(boolean enabled) {
    	super.setPreviousAvailable(enabled);
    }
    
    public void setCancelAvailable(boolean enabled) {
    	super.setCancelAvailable(enabled);
    }
    
    public void setImageFiles(List<File> imageFiles) {
    	
        this.imageFiles = imageFiles;
        imageFilenames = new ArrayList<String>();
        thumbnailFileLookup = new HashMap<String, File>();
        imageFilenames.add(I18n.getString(NONE_TEXT));
        for (File file : imageFiles) {
            imageFilenames.add(file.getName());
            thumbnailFileLookup.put(file.getName(), file);
        }
    }

    public void setAuthCookie(Cookie authCookie) {
        this.authCookie = authCookie;
    }
    
    public void setNextAvailable(boolean enable) {
    	super.setNextAvailable(enable);
    }
    
    public void setLastAvailable(boolean enable) {
    	super.setLastAvailable(enable);
    }
    
// Instance methods ///////////////////////////////////////////////////////////

    public void activateHelp(JComponent parent, WizardModel model) {
        HelpWindow window = new HelpWindow();
        window.setSize(200, 400);
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

} // class UpperBlipModel
