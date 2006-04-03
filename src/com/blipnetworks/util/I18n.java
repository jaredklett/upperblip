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

import java.util.ResourceBundle;

/**
 * This class serves as an internationalization helper.
 *
 * @author Jared Klett
 * @version $Id: I18n.java,v 1.4 2006/04/03 20:43:12 jklett Exp $
 */

public class I18n {

// CVS info ////////////////////////////////////////////////////////////////////

	public static final String CVS_REV = "$Revision: 1.4 $";

// Static variables ////////////////////////////////////////////////////////////

	private static ResourceBundle bundle = null;

// Static methods //////////////////////////////////////////////////////////////

	private static ResourceBundle getBundle() {
		if (bundle == null)
            bundle = ResourceBundle.getBundle("com-pokkari-blip-upper");

		return bundle;
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
