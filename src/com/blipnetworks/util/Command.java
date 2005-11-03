/* 
 * @(#)Command.java
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

/**
 * An interface to assist in implementing the Command design pattern.
 *
 * @author Jared Klett
 * @version	$Id: Command.java,v 1.1 2005/11/03 18:57:23 jklett Exp $
 */

public interface Command {

// CVS info ////////////////////////////////////////////////////////////////////

	public static final String CVS_REV = "$Revision: 1.1 $";

// Interface methods ///////////////////////////////////////////////////////////

	/**
	 * This is the method that will be called by the event listener. It's up to
	 * the implementing class to specify the actions taken.
	 */
	public void execute();

} // interface Command
