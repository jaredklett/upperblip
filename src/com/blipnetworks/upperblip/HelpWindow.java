package com.pokkari.blip.upper;

import javax.swing.*;
import java.util.Scanner;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: jklett
 * Date: Mar 16, 2006
 * Time: 11:49:46 AM
 * To change this template use File | Settings | File Templates.
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
