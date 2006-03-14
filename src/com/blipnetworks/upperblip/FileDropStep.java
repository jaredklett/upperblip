/* 
 * @(#)FileDropStep.java
 * 
 * Copyright (c) 2005 by Pokkari, Inc.
 * 117 West 25th St, Floor 2
 * New York, NY 10001
 * All rights reserved.
 *
 * This software is the confidential and
 * proprietary information of Pokkari, Inc.
 */

package com.pokkari.blip.upper;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import net.iharder.dnd.*;
import org.pietschy.wizard.*;

/**
 * 
 * 
 * @author Jared Klett
 * @version $Id: FileDropStep.java,v 1.3 2006/03/14 22:59:10 jklett Exp $
 */

public class FileDropStep extends AbstractWizardStep {

// CVS info ////////////////////////////////////////////////////////////////////

	public static final String CVS_REV = "$Revision: 1.3 $";

// Static variables ////////////////////////////////////////////////////////////

	/** blah */
    // TODO: externalize
	private static final String TITLE_TEXT = "Select video files to upload";
	/** blah */
    // TODO: externalize
	private static final String SUMMARY_TEXT = "Drag and drop video files from your desktop into the window.";

// Enumerated types ////////////////////////////////////////////////////////////

	// Enums, if any, go here

// Instance variables //////////////////////////////////////////////////////////

	/** */
	private UpperBlipModel model;
	/** */
	private JPanel view;
	/** */
	private JList list;
	/** */
	private List fileList;
	/** */
	private File[] files;
	/** */
	private FileDrop dropper;
	/** */
	private ListCellRenderer lcr;
	/** */
	private FileDrop.Listener fdl = new FileDrop.Listener() {
		public void filesDropped(File[] files) {
			if (fileList == null)
				fileList = new ArrayList(files.length + 10);
			for (int i = 0; i < files.length; i++)
				fileList.add(files[i]);

			DefaultListModel dlm = (DefaultListModel)list.getModel();
			for (int i = 0; i < files.length; i++)
				dlm.addElement(files[i].getName());
			setComplete(true);
		}
	};

	private ActionListener adder = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			JFileChooser chooser = new JFileChooser();
			int retval = chooser.showOpenDialog(Main.getMainInstance().getMainFrame());
			if (retval == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				if (fileList == null)
					fileList = new ArrayList();
				fileList.add(file);
				((DefaultListModel)list.getModel()).addElement(file.getName());
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
				for (int i = 0; i < fileList.size(); i++) {
					File f = (File)fileList.get(i);
					if (f.getName().equals((String)obj)) {
						fileList.remove(f);
						break;
					}
				}
				if (list.getModel().getSize() == 0)
					setComplete(false);
			}
		}
	};

// Constructor /////////////////////////////////////////////////////////////////

	public FileDropStep() {
		super(TITLE_TEXT, SUMMARY_TEXT);

		// Create and layout components
		list = new JList(new DefaultListModel());
		list.setDragEnabled(true);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setCellRenderer(new MyCellRenderer());

		JScrollPane scrollpane = new JScrollPane(list);
		scrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		// Set up our file drop detection object
		dropper = new FileDrop(scrollpane, fdl);

		JPanel panel = new JPanel();
		((FlowLayout)panel.getLayout()).setAlignment(FlowLayout.LEFT);
        // TODO: externalize
        JButton addButton = new JButton(new ImageIcon(ClassLoader.getSystemResource("icons/Add16.gif")));
		addButton.addActionListener(adder);
        // TODO: externalize
		JButton removeButton = new JButton(new ImageIcon(ClassLoader.getSystemResource("icons/Remove16.gif")));
		removeButton.addActionListener(remover);
		panel.add(addButton);
		panel.add(removeButton);

		view = new JPanel();
		view.setLayout(new BorderLayout());
		view.add(scrollpane, BorderLayout.CENTER);
		view.add(panel, BorderLayout.SOUTH);
	}

	public void init(WizardModel model) {
		this.model = (UpperBlipModel)model;
	}

	public void prepare() {
		setView(view);
	}

	public void applyState() {
		// this is called when the user clicks "Next"
		// Create an empty type array 
		File[] filetyper = new File[1];
		// Transform and cast the list to an array
		model.setFiles((File[])fileList.toArray(filetyper));
	}

	public Dimension getPreferredSize() {
		return view.getPreferredSize();
	}

	class MyCellRenderer extends JLabel implements ListCellRenderer {
		ImageIcon smallIcon = new ImageIcon(ClassLoader.getSystemResource("icons/Movie16.gif"));
		//final static ImageIcon shortIcon = new ImageIcon("short.gif");
		
		// This is the only method defined by ListCellRenderer.
		// We just reconfigure the JLabel each time we're called.
		
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			String s = value.toString();
			setText(s);
			setIcon(smallIcon);
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
