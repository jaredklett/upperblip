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
import 	org.apache.commons.httpclient.Cookie;

import javax.swing.*;

/**
 * @author Jared Klett
 * @version $Id: UpperBlipModel.java,v 1.22 2009/06/22 21:07:45 jklett Exp $
 */

public class UpperBlipModel extends StaticModel /*implements HelpBroker*/ {
	
    public static final String CVS_REV = "$Revision: 1.22 $";

	private static final String	NONE_TEXT = "general.none.text";
	
    private File[] 		files;
    private File[] 		imageFiles;
    private File[] 		thumbnails;
    private String[] 	imageFilenames;
    private String[] 	titles;
    private String[] 	descriptions;
    private String[]	tags;
    private String[] 	categories;
    private String[] 	licenses;
    private String[] 	languages;
    private String[] 	ratings;
    private String[] 	explicitFlags;
    private String[][] 	crossposts;
    private String[] 	postURLs;
    private String[][] 	crossuploads;
    private String[][]	conversionTargets;
    private String[]	mp3Audios;
    private String[]	mpeg4Videos;
    private String[]	privateFiles;
    private String[]	passwordFiles;
    private String[]	makePublicFiles;
    private String[]	passwordFields;
    private String[]	makePublicFields;
    private String 		username;
    private String 		password;
    private boolean 	remember;
    public Map<String, File> thumbnailFileLookup;
    public Cookie 		authCookie;

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

    public File[] getFiles() {
        return files;
    }

    public File[] getImageFiles() {
        return imageFiles;
    }

    public String[] getImageFilenames() {
        return imageFilenames;
    }

    public String[] getTitles() {
        return titles;
    }

    public String[] getTags() {
        return tags;
    }

    public String[] getCategories() {
        return categories;
    }

    public String[] getLicenses() {
        return licenses;
    }

    public String[] getDescriptions() {
        return descriptions;
    }

    public File[] getThumbnails() {
        return thumbnails;
    }

    public String[] getLanguages() {
        return languages;
    }

    public String[] getRatings() {
        return ratings;
    }

    public String[][] getCrossposts() {
        return crossposts;
    }

    public String[][] getCrossuploads() {
        return crossuploads;
    }

    public String[][] getConversionTargets() {
    	return conversionTargets;
    }
    
    public String[] getMp3Audios() {
    	return mp3Audios;
    }
    
    public String[] getMpeg4Videos() {
    	return mpeg4Videos;
    }
    
    public String[] getPrivateFiles() {
    	return privateFiles;
    }
    
    public String[] getPasswordFiles() {
    	return passwordFiles;
    }
    
    public String[] getMakePublicFiles() {
    	return makePublicFiles;
    }
    
    public String[] getPasswordFields() {
    	return passwordFields;
    }
    
    public String[] getMakePublicFields() {
    	return makePublicFields;
    }

    public String[] getPostURLs() {
        return postURLs;
    }

    public Cookie getAuthCookie() {
        return authCookie;
    }

    public String[] getExplicitFlags() {
        return explicitFlags;
    }

// Mutators ///////////////////////////////////////////////////////////////////

    public void setRemembered(boolean remember) {
        this.remember = remember;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFiles(File[] files) {
        this.files = files;
    }

    public void setPreviousAvailable(boolean enabled) {
    	super.setPreviousAvailable(enabled);
    }
    
    public void setCancelAvailable(boolean enabled) {
    	super.setCancelAvailable(enabled);
    }
    
    public void setImageFiles(File[] imageFiles) {
        this.imageFiles = imageFiles;
        imageFilenames = new String[imageFiles.length + 1];
        thumbnailFileLookup = new HashMap<String, File>();
        imageFilenames[0] = I18n.getString(NONE_TEXT);
        for (int i = 0; i < imageFiles.length; i++) {
            imageFilenames[i + 1] = imageFiles[i].getName();
            thumbnailFileLookup.put(imageFiles[i].getName(), imageFiles[i]);
        }
    }

    public void setTitles(String[] titles) {
        this.titles = titles;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public void setCategories(String[] categories) {
        this.categories = categories;
    }

    public void setLicenses(String[] licenses) {
        this.licenses = licenses;
    }

    public void setDescriptions(String[] descriptions) {
        this.descriptions = descriptions;
    }

    public void setThumbnails(File[] thumbnails) {
        this.thumbnails = thumbnails;
    }

    public void setLanguages(String[] languages) {
        this.languages = languages;
    }

    public void setRatings(String[] ratings) {
        this.ratings = ratings;
    }

    public void setCrossposts(String[][] crossposts) {
        this.crossposts = crossposts;
    }

    public void setCrossuploads(String[][] crossuploads) {
        this.crossuploads = crossuploads;
    }

    public void setConversionTargets(String[][] targets) {
    	this.conversionTargets = targets;
    }
    
    public void setMp3Audios(String[] mp3Audio) {
    	this.mp3Audios = mp3Audio;
    }
    
    public void setMpeg4Videos(String[] mpeg4Video) {
    	this.mpeg4Videos = mpeg4Video;
    }
    
    public void setPrivateFiles(String[] privateFiles) {
    	this.privateFiles = privateFiles;
    }
    
    public void setPasswordFiles(String[] passwordFiles) {
    	this.passwordFiles = passwordFiles;
    }
    
    public void setMakePublicFiles(String[] makePublicFiles) {
    	this.makePublicFiles = makePublicFiles;
    }
    
    public void setPasswordFields(String[] passwordFields) {
    	this.passwordFields = passwordFields;
    }
    
    public void setMakePublicFields(String[] makePublicFields) {
    	this.makePublicFields = makePublicFields;
    }
    
    public void setPostURLs(String[] postURLs) {
        this.postURLs = postURLs;
    }

    public void setAuthCookie(Cookie authCookie) {
        this.authCookie = authCookie;
    }

    public void setExplicitFlags(String[] explicitFlags) {
        this.explicitFlags = explicitFlags;
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
