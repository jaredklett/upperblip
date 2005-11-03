/* 
 * @(#)UpperBlipModel.java
 * 
 * Copyright (c) 2005 by Pokkari, Inc.
 * 117 West 25th St, Floor 2
 * New York, NY 10001
 * All rights reserved.
 *
 * This software is the confidential and
 * proprietary information of Pokkari, Inc.
 */

package com.pokkari.blip.upper;

import java.io.*;

import org.pietschy.wizard.*;
import org.pietschy.wizard.models.*;

/**
 * 
 * 
 * @author Jared Klett
 * @version $Id: UpperBlipModel.java,v 1.1 2005/11/03 18:57:23 jklett Exp $
 */

public class UpperBlipModel extends StaticModel {

	private File[] files;
	private String[] titles;
	private String[] descriptions;
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

	public void setDescriptions(String[] descriptions) {
		this.descriptions = descriptions;
	}

} // class UpperBlipModel
