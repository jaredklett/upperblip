package com.blipnetworks.upperblip;

import com.blipnetworks.util.Command;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

/**
 *
 *
 * @author Jared Klett
 * @version $Id: LinkLabel.java,v 1.1 2006/11/13 22:18:55 jklett Exp $
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
        setFont(new Font("SansSerif", Font.PLAIN, 9));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(ml);
    }

    public void execute() {
        command.execute();
    }

} // class LinkLabel
