/* 
 * @(#)I18n.java
 * 
 * Copyright (c) 2005 by Pokkari, Inc.
 * 117 West 25th St, Floor 2
 * New York, NY 10001
 * All rights reserved.
 *
 * This software is the confidential and
 * proprietary information of Pokkari, Inc.
 */

package com.pokkari.blip.util;

import com.pokkari.blip.upper.Main;

import java.util.*;

/**
 * This class serves as an internationalization helper.
 *
 * @author Jared Klett
 * @version $Id: I18n.java,v 1.3 2006/03/17 21:36:19 jklett Exp $
 */

public class I18n {

// CVS info ////////////////////////////////////////////////////////////////////

	public static final String CVS_REV = "$Revision: 1.3 $";

// Static variables ////////////////////////////////////////////////////////////

	private static ResourceBundle bundle = null;

// Static methods //////////////////////////////////////////////////////////////

	private static ResourceBundle getBundle() {
		if (bundle == null)
            bundle = ResourceBundle.getBundle("com-pokkari-blip-upper");
			//bundle = ResourceBundle.getBundle(Constants.I18N_NAME);

		return bundle;
	}

	/**
	 *
	 * @param _bundle The key to the requested property.
	 */
	public static void setBundle(ResourceBundle _bundle) {
		bundle = _bundle;
	}

	/**
	 *
	 * @param key The key to the requested property.
	 * @return The value of the requested property.
	 */
	public static String getString(String key) {
		return getBundle().getString(key);
	}

	/**
	 *
	 */
	public static Object getObject(String key) {
		return getBundle().getObject(key);
	}

	/**
	 *
	 * @param key The key to the requested property.
	 * @return The value of the requested property.
	 */
	public static String[] getStringArray(String key) {
		return getBundle().getStringArray(key);
	}

} // class I18n
