/*
 * @(#)LinkLabel.java
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

import com.blipnetworks.util.Command;

import javax.swing.*;
import java.awt.Font;
import java.awt.Cursor;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

/**
 *
 *
 * @author Jared Klett
 * @version $Id: LinkLabel.java,v 1.4 2009/06/22 21:07:45 jklett Exp $
 */

@SuppressWarnings("serial")
public class LinkLabel extends JLabel implements Command {
	
    public static final String CVS_REV = "$Revision: 1.4 $";

    private Command command;

    private MouseListener ml = new MouseListener() {
        public void mouseClicked(MouseEvent e) { execute(); }
        public void mousePressed(MouseEvent e) { /* ignored */ }
        public void mouseReleased(MouseEvent e) { /* ignored */ }
        public void mouseEntered(MouseEvent e) { /* ignored */ }
        public void mouseExited(MouseEvent e) { /* ignored */ }
    };

    public LinkLabel(String text, Command command) {
    	super(text);
        //super("<HTML><u>" + text + "</u></HTML>");
        this.command = command;
        setForeground(Color.BLUE);
        List<String> fonts = Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        if (fonts.contains("Verdana"))
            setFont(new Font("Verdana", Font.PLAIN, 12));
        else
            setFont(new Font("SansSerif", Font.PLAIN, 12));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(ml);
    }

    public void execute() {
        command.execute();
    }

} // class LinkLabel
