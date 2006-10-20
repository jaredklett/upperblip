/* 
 * @(#)Command.java
 * 
 * Copyright (c) 2006 by Blip Networks, Inc.
 * 239 Centre St, 3rd Floor
 * New York, NY 10013
 * All rights reserved.
 *
 * This software is the confidential and
 * proprietary information of Blip Networks, Inc.
 */

package com.blipnetworks.util;

/**
 * An interface to assist in implementing the Command design pattern.
 *
 * @author Jared Klett
 * @version	$Id: Command.java,v 1.5 2006/10/20 17:26:46 jklett Exp $
 */

public interface Command {

// CVS info ////////////////////////////////////////////////////////////////////

	public static final String CVS_REV = "$Revision: 1.5 $";

// Interface methods ///////////////////////////////////////////////////////////

	/**
	 * This is the method that will be called by the event listener. It's up to
	 * the implementing class to specify the actions taken.
	 */
	public void execute();

} // interface Command
