/* 
 * @(#)UpperBlipModel.java
 * 
 * Copyright (c) 2006 by Blip Networks, Inc.
 * 117 West 25th St, Floor 2
 * New York, NY 10001
 * All rights reserved.
 *
 * This software is the confidential and
 * proprietary information of Blip Networks, Inc.
 */

package com.blipnetworks.upperblip;

import java.io.*;

import org.pietschy.wizard.*;
import org.pietschy.wizard.models.*;

import javax.swing.*;

/**
 * @author Jared Klett
 * @version $Id: UpperBlipModel.java,v 1.7 2006/05/09 14:54:52 jklett Exp $
 */

public class UpperBlipModel extends StaticModel implements HelpBroker {

    private File[] files;
    private String[] titles;
    private String[] descriptions;
    private String[] tags;
    private String username;
    private String password;
    private boolean remember;

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

    public String[] getTitles() {
        return titles;
    }

    public String[] getTags() {
        return tags;
    }

    public String[] getDescriptions() {
        return descriptions;
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

    public void setTitles(String[] titles) {
        this.titles = titles;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public void setDescriptions(String[] descriptions) {
        this.descriptions = descriptions;
    }

    public void activateHelp(JComponent parent, WizardModel model) {
        System.out.println(parent.getClass());
        HelpWindow window = new HelpWindow();
        window.setSize(200, 400);
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

} // class UpperBlipModel
