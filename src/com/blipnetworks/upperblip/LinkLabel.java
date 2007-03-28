/*
 * @(#)LinkLabel.java
 *
 * Copyright (c) 2005-2007 by Blip Networks, Inc.
 * 239 Centre St, 3rd Floor
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
 * @version $Id: LinkLabel.java,v 1.3 2007/03/28 19:12:45 jklett Exp $
 */

public class LinkLabel extends JLabel implements Command {

    private Command command;

    private MouseListener ml = new MouseListener() {
        public void mouseClicked(MouseEvent e) { execute(); }
        public void mousePressed(MouseEvent e) { /* ignored */ }
        public void mouseReleased(MouseEvent e) { /* ignored */ }
        public void mouseEntered(MouseEvent e) { /* ignored */ }
        public void mouseExited(MouseEvent e) { /* ignored */ }
    };

    public LinkLabel(String text, Command command) {
        super("<HTML><u>" + text + "</u>");
        this.command = command;
        setForeground(Color.BLUE);
        List fonts = Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        if (fonts.contains("Verdana"))
            setFont(new Font("Verdana", Font.PLAIN, 9));
        else
            setFont(new Font("SansSerif", Font.PLAIN, 9));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(ml);
    }

    public void execute() {
        command.execute();
    }

} // class LinkLabel
