/* 
 * @(#)FileDropStep.java
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import	java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import com.blipnetworks.util.I18n;

import javax.swing.*;

import net.iharder.dnd.*;
import	com.blipnetworks.upperblip.wizard.*;

/**
 * 
 * 
 * @author Jared Klett
 * @version $Id: FileDropStep.java,v 1.20 2009/06/22 21:07:45 jklett Exp $
 */

public class FileDropStep extends AbstractWizardStep {

// CVS info ////////////////////////////////////////////////////////////////////

    public static final String CVS_REV = "$Revision: 1.20 $";

// Static variables ////////////////////////////////////////////////////////////

    /** blah */
    private static final String TITLE_KEY = "filedrop.title";
    /** blah */
    private static final String SUMMARY_KEY = "filedrop.summary";
    /** blah */
    private static final String ADD_TOOLTIP_KEY = "filedrop.add.tooltip";
    /** blah */
    private static final String REMOVE_TOOLTIP_KEY = "filedrop.remove.tooltip";
    /** blah */
    private static final String DISALLOWED_TITLE_KEY = "filedrop.disallowed.title";
    /** blah */
    private static final String DISALLOWED_MESSAGE_KEY = "filedrop.disallowed.message";
    private static final String	DRAG_TEXT = "filedrop.drag.text";

// Enumerated types ////////////////////////////////////////////////////////////

    // Enums, if any, go here

// Instance variables //////////////////////////////////////////////////////////

    /** */
    private UpperBlipModel 	model;
    /** */
    private JPanel 			view;
    /** */
    private JList 			list;
    /** */
    private List<File> 		fileList;
    private boolean			drawn = false;

    @SuppressWarnings("unused")
	private FileDrop dropper;
    /** */
    
    private FileDrop.Listener fdl = new FileDrop.Listener() {
    	
        public void filesDropped(File[] files) {
            if (fileList == null) {
                fileList = new ArrayList<File>(files.length + 10);
            }
            
            for (File file : files) {
            	fileList.add(file);
            }

            DefaultListModel dlm = (DefaultListModel)list.getModel();
            for (File file : files) {
            	dlm.addElement(file.getName());
            	drawn = true;
            }
            
            if (isDisallowedPresent()) {
                JOptionPane.showMessageDialog(Main.getMainInstance().getMainFrame(), I18n.getString(DISALLOWED_MESSAGE_KEY),
                        						I18n.getString(DISALLOWED_TITLE_KEY), JOptionPane.WARNING_MESSAGE);
                setComplete(false);
                return;
            }
            else {
                setComplete(true);
            }
        }
    };

    private ActionListener adder = new ActionListener() {
    	
        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            
            chooser.setDragEnabled(true);
            chooser.setMultiSelectionEnabled(true);
            
            int retval = chooser.showOpenDialog(Main.getMainInstance().getMainFrame());
            if (retval == JFileChooser.APPROVE_OPTION) {
                if (fileList == null) {
                    fileList = new ArrayList<File>();
                }
                
            	for (File file : chooser.getSelectedFiles()) {
            		fileList.add(file);
            		((DefaultListModel)list.getModel()).addElement(file.getName());
            		drawn = true;
            	}
            	
        		if (isDisallowedPresent()) {
        			JOptionPane.showMessageDialog(
                        Main.getMainInstance().getMainFrame(),
                        I18n.getString(DISALLOWED_MESSAGE_KEY),
                        I18n.getString(DISALLOWED_TITLE_KEY),
                        JOptionPane.WARNING_MESSAGE
        			);
        			setComplete(false);
        			return;
        		}
                setComplete(true);
            }
        }
    };

    private ActionListener remover = new ActionListener() {
    	
        public void actionPerformed(ActionEvent e) {
            if (list.getSelectedIndex() != -1) {
                // Remove the representation from the list
                Object obj = ((DefaultListModel)list.getModel()).remove(list.getSelectedIndex());
                // Remove the file object from the list
                for (File file : fileList) {
                    if (file.getName().equals(obj)) {
                        fileList.remove(file);
                        break;
                    }
                }
                if (list.getModel().getSize() == 0) {
                    setComplete(false);
                }
                else setComplete((isDisallowedPresent()) ? false : true);
            }
        }
    };

// Constructor /////////////////////////////////////////////////////////////////
    
    @SuppressWarnings("serial")
	public FileDropStep() {
        super(I18n.getString(TITLE_KEY), I18n.getString(SUMMARY_KEY));

        setIcon(Icons.filedropIcon);
        // Create and layout components
        list = new JList(new DefaultListModel()) {
        	public void paintComponent(Graphics g) {
        		super.paintComponent(g);
        		
        		if (drawn) {
        			return;
        		}
				Graphics2D	g2 = (Graphics2D)g;
				g2.setColor(Color.gray);
				g2.drawString(I18n.getString(DRAG_TEXT), 200, 20);
        	}
        };
        list.setDragEnabled(true);
        list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setVisibleRowCount(10);
        list.setFixedCellWidth(-1);
        list.setCellRenderer(new MyCellRenderer());
        list.setPreferredSize(new Dimension(530, 180));

        JScrollPane scrollpane = new JScrollPane(list);
        scrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        // Set up our file drop detection object
        dropper = new FileDrop(scrollpane, fdl);

        JPanel panel = new JPanel();
        ((FlowLayout)panel.getLayout()).setAlignment(FlowLayout.LEFT);
        JButton addButton = new JButton(Icons.addIcon);
        addButton.addActionListener(adder);
        addButton.setToolTipText(I18n.getString(ADD_TOOLTIP_KEY));
        JButton removeButton = new JButton(Icons.removeIcon);
        removeButton.addActionListener(remover);
        removeButton.setToolTipText(I18n.getString(REMOVE_TOOLTIP_KEY));
        panel.add(addButton);
        panel.add(removeButton);

        view = new JPanel();
        //view.setName("FileDropStep");
        view.setLayout(new BorderLayout());
        view.add(scrollpane, BorderLayout.CENTER);
        view.add(panel, BorderLayout.SOUTH);
    }

    public void init(WizardModel model) {
        this.model = (UpperBlipModel)model;
    }

    public void prepare() {
        setView(view);
        Main.getMainInstance().getMainFrame().pack();
        Main.getMainInstance().getMover().positionFrame(true);
        model.setNextAvailable(false);
    }

    public void applyState() {
        // this is called when the user clicks "Next"
        // Create an empty type array
        File[] filetyper = new File[0];
        // Transform and cast the list to an array
        File[] files = fileList.toArray(filetyper);
        List<File> imageList = new ArrayList<File>();
        List<File> list = new ArrayList<File>();
        for (int i = 0; i < files.length; i++) {
            if (Icons.isImage(files[i].getName())) {
                imageList.add(files[i]);
            }
            else {
                list.add(files[i]);
            }
        }
        model.setFiles(list.toArray(filetyper));
        model.setImageFiles(imageList.toArray(filetyper));
    }

    public Dimension getPreferredSize() {
        return view.getPreferredSize();
    }

    private boolean isDisallowedPresent() {
        
        for (File file : fileList) {
            if (Icons.isDisallowed(file.getName())) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("serial")
	class MyCellRenderer extends JLabel implements ListCellRenderer {
        // This is the only method defined by ListCellRenderer.
        // We just reconfigure the JLabel each time we're called.
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String s = value.toString();
            
            setText(s);
            setIcon(Icons.getIconForFilename(s));
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            
            setEnabled(list.isEnabled());
            setFont(list.getFont());
            setOpaque(true);
            return this;
        }
    }

} // class FileDropStep
