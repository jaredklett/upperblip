/* 
 * @(#)UpperBlipModel.java
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

import java.io.*;
import java.util.Map;
import java.util.HashMap;

import org.pietschy.wizard.*;
import org.pietschy.wizard.models.*;

import javax.swing.*;

/**
 * @author Jared Klett
 * @version $Id: UpperBlipModel.java,v 1.14 2006/11/09 17:59:14 jklett Exp $
 */

public class UpperBlipModel extends StaticModel /*implements HelpBroker*/ {

    private File[] files;
    private File[] imageFiles;
    private File[] thumbnails;
    private String[] imageFilenames;
    private String[] titles;
    private String[] descriptions;
    private String[] tags;
    private String[] categories;
    private String[] licenses;
    private String username;
    private String password;
    private boolean remember;
    public Map thumbnailFileLookup;

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

    public void setImageFiles(File[] imageFiles) {
        this.imageFiles = imageFiles;
        imageFilenames = new String[imageFiles.length + 1];
        thumbnailFileLookup = new HashMap();
        // TODO: externalize
        imageFilenames[0] = "None";
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

    public void activateHelp(JComponent parent, WizardModel model) {
        System.out.println(parent.getClass());
        HelpWindow window = new HelpWindow();
        window.setSize(200, 400);
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

} // class UpperBlipModel
