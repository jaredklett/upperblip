/* 
 * @(#)Grid.java
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

import 	java.awt.*;

/**
 * Instead of embedding control statements directly into the Java code,
 * this class handles setting GridBagConstraints and hiding most of the
 * details.
 * 
 * Several methods return a Grid reference such that adding a component
 * to the layout can be accomplished with a single statement:
 * 
 *     panel.add(component, Grid.setAnchorIncrX(Grid.eastAnchor));
 *     
 * @author dsklett
 * @version $Id: Grid.java,v 1.2 2009/06/22 21:07:45 jklett Exp $
 */
@SuppressWarnings("serial")
public class Grid extends GridBagConstraints {
	
    public static final String CVS_REV = "$Revision: 1.2 $";

	public static final int	centerAnchor = GridBagConstraints.CENTER;
	public static final int	northAnchor = GridBagConstraints.NORTH;
	public static final int	southAnchor = GridBagConstraints.SOUTH;
	public static final int	westAnchor = GridBagConstraints.WEST;
	public static final int	eastAnchor = GridBagConstraints.EAST;
	public static final int	northWestAnchor = GridBagConstraints.NORTHWEST;
	public static final int	northEastAnchor = GridBagConstraints.NORTHEAST;
	
	public static final int	noneFill = GridBagConstraints.NONE;
	public static final int	bothFill = GridBagConstraints.BOTH;
	public static final int	horizontalFill = GridBagConstraints.HORIZONTAL;
	public static final int	verticalFill = GridBagConstraints.VERTICAL;
	
	/**
	 * Sets the no-arg constructor with gridx and gridy set to zero.
	 */
	public Grid() {
		super();
		resetConstraints(0, 0);
	}
	
	/**
	 * This constructor sets gridx and gridy to general values.
	 * 
	 * @param x gridx value
	 * @param y gridy value
	 */
	public Grid(int x, int y) {
		super();
		resetConstraints(x, y);
	}

	/**
	 * By setting the gridx value to RELATIVE, components are added as
	 * if the gridx value is incremented by 1 for each call.
	 */
	public void setRelativeX() {
		gridx = GridBagConstraints.RELATIVE;
	}
	
	/**
	 * By setting the gridy value to RELATIVE, components are added as
	 * if the gridy value is incremented by 1 for each call.
	 */
	public void setRelativeY() {
		gridy = GridBagConstraints.RELATIVE;
	}
	
	/**
	 * Setting the gridwidth to REMAINDER, the added component occupies
	 * all the remaining grid cells for the current row.
	 */
	public void setRemainderX() {
		gridwidth = GridBagConstraints.REMAINDER;
	}
	
	/**
	 * Setting the gridheight to REMAINDER, the added component occupies
	 * all the remaining grid cells for the current column.
	 */
	public void setRemainderY() {
		gridheight = GridBagConstraints.REMAINDER;
	}
	
	/**
	 * Sets the gridx value.
	 * 
	 * @param x gridx
	 */
	public void setX(int x) {
		gridx = x;
	}
	
	/**
	 * Increments the gridx value by 1.
	 * 
	 * @return Grid reference
	 */
	public Grid incrX() {
		gridx++;
		
		return this;
	}
	
	/**
	 * Sets the gridy value.
	 * 
	 * @param y gridy
	 */
	public void setY(int y) {
		gridy = y;
	}
	
	/**
	 * Increments the gridy value by 1.
	 * 
	 * @return Grid reference
	 */
	public Grid incrY() {
		gridy++;
		
		return this;
	}
	
	/**
	 * Sets both the gridx and gridy values.
	 * 
	 * @param x gridx
	 * @param y gridy
	 */
	public void setXY(int x, int y) {
		gridx = x;
		gridy = y;
	}
	
	/**
	 * Sets the cell anchor and the gridx value.
	 * 
	 * @param anchor
	 * @param x
	 */
	public Grid setAnchorX(int anchor, int x) {
		this.anchor = anchor;
		gridx = x;
		
		return this;
	}
	
	/**
	 * Sets the cell anchor, the gridx value and the gridy value.
	 * 
	 * @param anchor
	 * @param x
	 * @param y
	 */
	public Grid setAnchorXY(int anchor, int x, int y) {
		this.anchor = anchor;
		gridx = x;
		gridy = y;
		
		return this;
	}
	
	/**
	 * Sets the cell anchor and increments gridx by 1.
	 * 
	 * @param anchor
	 * @return
	 */
	public Grid setAnchorIncrX(int anchor) {
		this.anchor = anchor;
		gridx++;
		
		return this;
	}
	
	/**
	 * Sets the cell anchor and increments gridy by 1.
	 * 
	 * @param anchor
	 * @return
	 */
	public Grid setAnchorIncrY(int anchor) {
		this.anchor = anchor;
		gridx = 0;
		gridy++;
		
		return this;
	}
	
	/**
	 * Sets the cell anchor and fill values.
	 * 
	 * @param anchor
	 * @param fill
	 */
	public void setAnchorFill(int anchor, int fill) {
		this.anchor = anchor;
		this.fill = fill;
	}
	
	/**
	 * Sets the cell anchor and returns the Grid reference.
	 * 
	 * @param anchor
	 * @return
	 */
	public Grid setAnchor(int anchor) {
		this.anchor = anchor;
		return this;
	}
	
	/**
	 * Sets the cell fill value.
	 * 
	 * @param fill
	 */
	public void setFill(int fill) {
		this.fill = fill;
	}

	/**
	 * Adds padding in the x direction for the cell.
	 * 
	 * @param padding
	 */
	public void addPaddingX(int padding) {
		ipadx = padding;
	}
	
	/**
	 * Adds padding in the y direction for the cell.
	 * 
	 * @param padding
	 */
	public void addPaddingY(int padding) {
		ipady = padding;
	}
	
	/**
	 * Initializes the Grid properties and sets the gridx and gridy value.
	 * 
	 * @param x
	 * @param y
	 */
	public void resetConstraints(int x, int y) {
		gridx = x;
		gridy = y;
		anchor = GridBagConstraints.CENTER;
		fill = GridBagConstraints.NONE;
		gridwidth = 1;
		gridheight = 1;
	}
	
	/**
	 * Clears the cell insets.
	 */
	public void clearInsets() {
		insets = new Insets(0, 0, 0, 0);
	}
	
	/**
	 * Sets the cell inset values.
	 * 
	 * @param top
	 * @param left
	 * @param bottom
	 * @param right
	 */
	public void setInsets(int top, int left, int bottom, int right) {
		insets = new Insets(top, left, bottom, right);
	}
	
	/**
	 * Sets the gridwidth and gridheight values.
	 * 
	 * @param width
	 * @param height
	 */
	public void setRowAndColumnSpan(int width, int height) {
		gridwidth = width;
		gridheight = height;
	}
	
	/**
	 * Sets the gridwidth value.
	 * 
	 * @param width
	 */
	public void setColumnSpan(int width) {
		gridwidth = width;
	}
	
	/**
	 * Sets the gridheight value.
	 * 
	 * @param height
	 */
	public void setRowSpan(int height) {
		gridheight = height;
	}
	
}
