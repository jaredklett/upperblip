/*
 * @(#)HelpWindow.java
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

import javax.swing.*;
import java.util.Scanner;
import java.awt.*;

/**
 * Help window, yea!
 * 
 * @author Jared Klett
 */

public class HelpWindow extends JWindow {

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
