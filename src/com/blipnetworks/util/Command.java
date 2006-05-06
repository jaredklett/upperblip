/* 
 * @(#)Command.java
 * 
 * Copyright (c) 2006 by Blip Networks, Inc.
 * 117 West 25th St, Floor 2
 * New York, NY 10001
 * All rights reserved.
 *
 * This software is the confidential and
 * proprietary information of Blip Networks, Inc.
 */

package com.blipnetworks.blip.util;

/**
 * An interface to assist in implementing the Command design pattern.
 *
 * @author Jared Klett
 * @version	$Id: Command.java,v 1.2 2006/05/06 23:56:46 jklett Exp $
 */

public interface Command {

// CVS info ////////////////////////////////////////////////////////////////////

	public static final String CVS_REV = "$Revision: 1.2 $";

// Interface methods ///////////////////////////////////////////////////////////

	/**
	 * This is the method that will be called by the event listener. It's up to
	 * the implementing class to specify the actions taken.
	 */
	public void execute();

} // interface Command
