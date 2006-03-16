/* 
 * @(#)UploadStep.java
 * 
 * Copyright (c) 2006 by Pokkari, Inc.
 * 117 West 25th St, Floor 2
 * New York, NY 10001
 * All rights reserved.
 *
 * This software is the confidential and
 * proprietary information of Pokkari, Inc.
 */

package com.pokkari.blip.upper;

import java.awt.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import com.pokkari.blip.util.*;
import com.pokkari.util.*;
import org.pietschy.wizard.*;

/**
 * 
 * 
 * @author Jared Klett
 * @version $Id: UploadStep.java,v 1.4 2006/03/16 04:30:53 jklett Exp $
 */

public class UploadStep extends AbstractWizardStep implements Runnable {

// CVS info ////////////////////////////////////////////////////////////////////

	public static final String CVS_REV = "$Revision: 1.4 $";

// Static variables ////////////////////////////////////////////////////////////

	/** blah */
    // TODO: externalize
	private static final String TITLE_TEXT = "Upload!";
	/** blah */
    // TODO: externalize
	private static final String SUMMARY_TEXT = "Please wait while your videos are uploaded.";
	/** blah */
	public static final String PROP_BASE_URL = "base.url";

// Enumerated types ////////////////////////////////////////////////////////////

	// Enums, if any, go here

// Instance variables //////////////////////////////////////////////////////////

	/** */
	private boolean running;
	/** */
	private int time;
	/** */
	private JPanel view;
	/** */
	private UpperBlipModel model;
	/** */
	private JLabel rateLabel;
	/** */
	private JLabel timeLabel;
	/** */
	private JProgressBar progress;
	/** */
	private Thread thread;
	/** */
	private UploadStatus status;

	private Runnable timer = new Runnable() {
		public void run() {
			status.check();
/*
            // TODO: externalize
			if (time < 60) {
				timeLabel.setText("Time remaining: less than a minute");
			} else {
				int minutes = time / 60;
				while (minutes > 0 && running) {
					if (minutes > 1)
						timeLabel.setText("Time remaining: about " + minutes + " minutes");
					else if (minutes > 0)
						timeLabel.setText("Time remaining: about a minute");
					else
						timeLabel.setText("Time remaining: less than a minute");
*/
					try {
						Thread.sleep(2000);
					}
					catch (Exception e) {
						// ignore
					}
/*
					minutes--;				
				}
			}
*/
		}
	};

// Constructor ////////////////////////////////////////////////////////////////

	public UploadStep() {
		super(TITLE_TEXT, SUMMARY_TEXT);

		// Create and layout components
        // TODO: externalize
		rateLabel = new JLabel("Calculating transfer rate, please wait...");
		timeLabel = new JLabel("Time remaining: unknown");
		progress = new JProgressBar();
		progress.setIndeterminate(true);

		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		JPanel panel = new JPanel();
		panel.setLayout(gbl);
		// Set up a border
		panel.setBorder(
			BorderFactory.createCompoundBorder(
				BorderFactory.createCompoundBorder(
                        // TODO: externalize
					BorderFactory.createTitledBorder("Upload progress"),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)
				),
				panel.getBorder()
			)
		);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbl.setConstraints(rateLabel, gbc);
		panel.add(rateLabel);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbl.setConstraints(progress, gbc);
		panel.add(progress);

		gbc.gridx = 0;
		gbc.gridy = 2;
		gbl.setConstraints(timeLabel, gbc);
		panel.add(timeLabel);

		view = new JPanel();
		view.add(panel);
	}

// Runnable implementation ////////////////////////////////////////////////////

	public void run() {
		setBusy(true);
		float bps = 38400.0f;
		String url = Main.appProperties.getProperty(PROP_BASE_URL) + "/file/post?form_cookie=";
		String statusUrl = Main.appProperties.getProperty(PROP_BASE_URL) + "/file/upload_status?skin=xmlhttprequest&form_cookie=";
		Uploader uploader = new Uploader(url);
		// The reference to our status object is global so the thread can access it
		status = new UploadStatus(statusUrl);
		File[] files = model.getFiles();
		progress.setMinimum(0);
		progress.setMaximum(files.length);
		String[] titles = model.getTitles();
		String[] tags = model.getTags();
		String[] descriptions = model.getDescriptions();
		// Create a properties object
		Properties props = new Properties();
		props.put(Uploader.USER_PARAM_KEY, model.getUsername());
		props.put(Uploader.PASS_PARAM_KEY, model.getPassword());
		for (int i = 0; i < files.length; i++) {
			// Generate a new GUID
			RandomGUID guid = new RandomGUID(false);
			uploader.setGuid(guid.toString());
			status.setGuid(guid.toString());
			// put in the current data
			props.put(Uploader.TITLE_PARAM_KEY, titles[i]);
			//props.put(Uploader.TAGS_PARAM_KEY, tags[i]);
			props.put(Uploader.DESC_PARAM_KEY, descriptions[i]);
			// calculate the approximate transfer time for the next file
			time = (int)(files[i].length() / bps);
			String kbpstr = Float.toString(bps / 1000);
            // TODO: externalize
			rateLabel.setText("Uploading \"" + files[i].getName() + "\" at " + kbpstr.substring(0, kbpstr.indexOf(".") + 2) + " KB/s");
			// do the upload
			long start = System.currentTimeMillis();
			Thread thread = new Thread(timer);
			running = true;
			thread.start();
			boolean success = uploader.uploadFile(files[i], props);
			long delta = System.currentTimeMillis() - start;
			// Halt the timer thread
			running = false;
			thread.interrupt();
			if (!success) {
				int choice = JOptionPane.showConfirmDialog(
					Main.getMainInstance().getMainFrame(),
                        // TODO: externalize
					"It seems that a problem arose while uploading.\nDo you wish to continue attempting to upload?",
					"Error occurred during upload",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.ERROR_MESSAGE
				);

				if (choice == JOptionPane.NO_OPTION)
					break;
			}
			bps = files[i].length() / (delta / 1000);
			// take out the old data
			props.remove(Uploader.TITLE_PARAM_KEY);
			props.remove(Uploader.DESC_PARAM_KEY);
			if (progress.isIndeterminate())
				progress.setIndeterminate(false);
			progress.setValue(progress.getValue() + 1);
		}
		setBusy(false);
        // TODO: externalize
		rateLabel.setText("All uploads complete!");
		timeLabel.setText("");
		setComplete(true);
	}

// WizardStep implementation //////////////////////////////////////////////////

	public void init(WizardModel model) {
		this.model = (UpperBlipModel)model;
	}

	public void prepare() {
		setView(view);
		thread = new Thread(this);
		thread.start();
	}

	public void applyState() {
		// this is called when the user clicks "Next"
		//FontItem fi = (FontItem)dropdown.getSelectedItem();
		//model.setFontSize(fi.getFont().getSize());
	}

	public Dimension getPreferredSize() {
		return view.getPreferredSize();
	}

// Instance methods ///////////////////////////////////////////////////////////

	public static String parseTime(long time) {
		long t = 0L;
		StringBuffer buffer = new StringBuffer();

        // TODO: externalize
		if(time < 60000) {
			t = time / 1000;
			buffer.append(t).append(" second");
		} else if(time > 60000 && time < 3600000) {
			t = time / 60000;
			buffer.append(t).append(" minute");
		} else if(time > 3600000 && time < 86400000) {
			t = time / 3600000;
			buffer.append(t).append(" hour");
		} else if(time > 86400000 && time < 604800000L) {
			t = time / 86400000;
			buffer.append(t).append(" day");
		} else if(time > 604800000L && time < 2419200000L) {
			t = time / 604800000L;
			buffer.append(t).append(" week");
		} else if(time > 2419200000L && time < 29030400000L) {
			t = time / 2419200000L;
			buffer.append(t).append(" month");
		} else {
			t = time / 29030400000L;
			buffer.append(t).append(" year");
		}

		if(t > 1)
			buffer.append("s");

		return buffer.toString();
	}

} // class UploadStep
