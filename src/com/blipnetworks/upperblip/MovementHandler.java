/* 
 * @(#)MovementHandler.java
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

import	java.awt.*;
import	java.awt.event.*;
import	javax.swing.*;

/**
 * The goal for this class is to handle possible movement of the containing frame by 
 * the user. Normally, the parent application will center any frames that are created.
 * This handler must deal with both the application moving the frame (centering) and
 * the user repositioning the frame. Also, since the user can move the frame, this class
 * must make sure the frame is still completely visible after the user moves the frame.
 * 
 * @author dsklett
 * @version $Id: MovementHandler.java,v 1.2 2009/06/22 21:07:45 jklett Exp $
 */
public class MovementHandler implements ComponentListener {
	
    public static final String CVS_REV = "$Revision: 1.2 $";

	private JFrame		frame = null;
	
	// movedLocation only gets set when the movement listener detects user activity.
	// lastLocation is always set to the last position of the frame.
	private Point		movedLocation = null;
	private Point		lastLocation = null;
	private boolean		centered = true;
	
	// frameMargins are obtained from the GraphicsConfiguration and show any padding
	// the native OS maintains for a window. This is particularly important for the
	// Mac OS X. The screen simply shows the actual dimensions of the entire display area.
	private Insets		frameMargins = null;
	private Dimension	screen = null;
	
	public MovementHandler(JFrame frame) {
		Toolkit	kit = Toolkit.getDefaultToolkit();
		
		this.frame = frame;
		frameMargins = kit.getScreenInsets(this.frame.getGraphicsConfiguration());
		screen = kit.getScreenSize();
	}

	/**
	 * This method is normally called by the containing application to center the frame
	 * shown as a parameter to the constructor. To distinguish the method being called
	 * by the application and called by the movement handler, the centered parameter is 
	 * used.
	 * 
	 * @param centered true is called by application, else false
	 */
	public void positionFrame(boolean centered) {
		Dimension	frameSize = frame.getSize();

		this.centered = centered;
		frame.removeComponentListener(this);
		
		if (centered && movedLocation == null) {
			frame.setLocation((screen.width - frameSize.width) / 2, (screen.height - frameSize.height) / 2);
			lastLocation = frame.getLocation();
			frame.addComponentListener(this);
			return;
		}
		
		int	x = movedLocation.x;
		int	y = movedLocation.y;

		// If the frame has been moved off the screen to the left, then bring it back on
		// the screen at x = 20;
		if (movedLocation.x < 0) {
			x = 20;
		}
		
		// If the frame has been moved off the screen to the right, then bring it back
		// on the screen with a padding of 20. If the OS is Mac OS X, and the user has
		// turned on Spaces, then movement off the right side can move into the next space.
		// Surprise!
		if (movedLocation.x + frameSize.width > screen.width) {
			x = screen.width - frameSize.width - 20;
		}
		
		// If the frame has been moved past the bottom of the screen, bring it back
		if (movedLocation.y + frameSize.height > screen.height - frameMargins.bottom) {
			y = screen.height - frameMargins.bottom - frameSize.height - 20;
		}
		
		// Check for no adjustments to the position.
		if (x == movedLocation.x && y == movedLocation.y) {
			frame.addComponentListener(this);
			return;
		}
		
		// Reposition the frame.
		frame.setLocation(x, y);
		lastLocation = frame.getLocation();
		frame.addComponentListener(this);
	}

	/**
	 * Through careful use of the addComponentListener() method, this method will
	 * only be called when the user has moved the frame. Any frame movements due to
	 * recalculating the centering of the frame will be ignored.
	 */
	public void componentMoved(ComponentEvent event) {
		JFrame 	frame = (JFrame)event.getSource();
		Point	location = frame.getLocation();

		if (lastLocation == null) {
			lastLocation = location;
			return;
		}
		
		if (lastLocation.equals(location)) {
			return;
		}
				
		// The x coordinate should be zero only when the frame is initially packed.
		// So ignore that moved event. Does not seem to ever occur.
		if (location.x == 0) {
			return;
		}
		
		// Setting the centered value true signals that the moved event is due to calling
		// the positionFrame() method and the move can be ignored.
		if (centered && movedLocation == null) {
			centered = false;
			return;
		}
		
		// Once the user moves the frame, then calls to positionFrame() uses the
		// location of the frame, as is. Centering will not be done.
		movedLocation = location;
		positionFrame(false);
	}
	
	public void componentShown(ComponentEvent event) {
		// ignore
	}
	
	public void componentHidden(ComponentEvent event) {
		// ignore
	}
	
	public void componentResized(ComponentEvent event) {
		// ignore
	}
	
}
