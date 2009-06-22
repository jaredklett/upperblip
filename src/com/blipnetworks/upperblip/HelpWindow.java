/*
 * @(#)HelpWindow.java
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

import javax.swing.*;
import java.util.Scanner;
import java.awt.*;

/**
 * Help window, yea!
 * 
 * @author Jared Klett
 */

@SuppressWarnings("serial")
public class HelpWindow extends JWindow {
	
    public static final String CVS_REV = "$Revision: 1.7 $";

    public HelpWindow() {
        super();
        Scanner s = new Scanner(ClassLoader.getSystemResourceAsStream("help.html"));
        String html = s.useDelimiter("\\A").next();
        s.close();
        JEditorPane editor = new JEditorPane("text/html", html);
        editor.setEditable(false);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(BorderLayout.CENTER, editor);
    }

} // class HelpWindow
