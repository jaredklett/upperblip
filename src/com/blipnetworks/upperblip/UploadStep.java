/* 
 * @(#)UploadStep.java
 * 
 * Copyright (c) 2006 by Blip Networks, Inc.
 * 117 West 25th St, Floor 2
 * New York, NY 10001
 * All rights reserved.
 *
 * This software is the confidential and
 * proprietary information of Blip Networks, Inc.
 */

package com.blipnetworks.upperblip;

import java.awt.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import com.blipnetworks.util.*;

import org.pietschy.wizard.AbstractWizardStep;
import org.pietschy.wizard.WizardModel;

/**
 * 
 * 
 * @author Jared Klett
 * @version $Id: UploadStep.java,v 1.13 2006/05/09 14:54:52 jklett Exp $
 */

public class UploadStep extends AbstractWizardStep implements Runnable {

// CVS info ////////////////////////////////////////////////////////////////////

	public static final String CVS_REV = "$Revision: 1.13 $";

// Static variables ////////////////////////////////////////////////////////////

	/** blah */
	private static final String TITLE_KEY = "upload.title";
	/** blah */
	private static final String SUMMARY_KEY = "upload.summary";
    /** blah */
    private static final String BORDER_LABEL_KEY = "upload.border.label";
    /** blah */
    private static final String RATE_LABEL_KEY = "upload.rate.label";
    /** blah */
    private static final String TIME_LABEL_KEY = "upload.time.label";
    /** blah */
    private static final String FILE_LABEL_KEY = "upload.file.label";
    /** blah */
    private static final String OVERALL_TIME_LABEL_KEY = "upload.overall.time.label";
	/** blah */
	public static final String PROP_BASE_URL = "base.url";

// Enumerated types ////////////////////////////////////////////////////////////

	// Enums, if any, go here

// Instance variables //////////////////////////////////////////////////////////

    private int totalUploadSamples;
    private int statusHitCount;
    private long startTime;
    private long refreshRate = 1000;
    private long statusHitRate = 10000;
    /** */
	private boolean running;
    /** */
    private String statusURL;
    /** */
    private String filename;
    /** */
    private RandomGUID guid;
	/** */
	private JPanel view;
	/** */
	private UpperBlipModel model;
	/** */
	private JLabel rateLabel;
	/** */
	private JLabel timeLabel;
	/** */
	private JProgressBar currentProgress;
    /** */
    private JLabel fileLabel;
    /** */
    private JLabel overallTimeLabel;
    /** */
    private JProgressBar overallProgress;

    private Runnable timer = new Runnable() {
        public void run() {
            long accum = 0;
            long start = 0;
            long update = 0;
            long delta = 0;
            int read = 0;
            int total = 0;
            float bps = 0;
            currentProgress.setMinimum(0);
            try { Thread.sleep(5000); } catch (Exception e) { /* ignored */ }
            while (running) {
                if (accum == 0 || accum >= statusHitRate) {
                    UploadStatus status = UploadStatus.getStatus(statusURL, guid.toString());
                    statusHitCount++;
                    start = status.getStart();
                    update = status.getUpdate();
                    read = status.getRead();
                    total = status.getTotal();
                    bps = read / (update - start);
                    totalUploadSamples += bps;
                    if (currentProgress.isIndeterminate())
                        currentProgress.setIndeterminate(false);
                } else {
                    // add the refresh sleep time
                    update += refreshRate;
                    // project how many bytes have been uploaded
                    // while we were sleeping
                    read += bps * (refreshRate / 1000);
                }
                currentProgress.setValue(read);
                currentProgress.setMaximum(total);
                delta = update - start;
                if (delta == 0)
                    continue;
                int time = (int) (total / bps);
                String kbpstr = Float.toString(bps / 1000);
                // TODO: externalize
                rateLabel.setText("Uploading \"" + filename + "\" at " + kbpstr.substring(0, kbpstr.indexOf(".") + 2) + " KB/s");

                String str = " (" + parseSize(read) + " of " + parseSize(total) + " uploaded)";
                // TODO: externalize
                if (time < 60) {
                    timeLabel.setText("Time remaining: less than a minute" + str);
                } else {
                    int minutes = time / 60;
                    if (minutes > 1)
                        timeLabel.setText("Time remaining: about " + minutes + " minutes" + str);
                    else if (minutes > 0)
                        timeLabel.setText("Time remaining: about a minute" + str);
                    else
                        timeLabel.setText("Time remaining: less than a minute" + str);
                }
                try { Thread.sleep(refreshRate); } catch (Exception e) { /* ignored */ }
                accum += refreshRate;
            }
        }
    };

// Constructor ////////////////////////////////////////////////////////////////

	public UploadStep() {
		super(I18n.getString(TITLE_KEY), I18n.getString(SUMMARY_KEY));

		// Create and layout components
		rateLabel = new JLabel(I18n.getString(RATE_LABEL_KEY));
		timeLabel = new JLabel(I18n.getString(TIME_LABEL_KEY));
		currentProgress = new JProgressBar();
		currentProgress.setIndeterminate(true);

        // TODO: change layout manager
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        view = new JPanel();
        view.setLayout(gbl);
        JPanel panel = buildPanel(rateLabel, timeLabel, currentProgress);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbl.setConstraints(panel, gbc);
        view.add(panel);
    }

// Instance methods ///////////////////////////////////////////////////////////

    private JPanel buildPanel(JLabel topLabel, JLabel bottomLabel, JProgressBar progress) {
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		JPanel panel = new JPanel();
		panel.setLayout(gbl);
		// Set up a border
		panel.setBorder(
			BorderFactory.createCompoundBorder(
				BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder(I18n.getString(BORDER_LABEL_KEY)),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)
				),
				panel.getBorder()
			)
		);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbl.setConstraints(topLabel, gbc);
		panel.add(topLabel);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbl.setConstraints(progress, gbc);
		panel.add(progress);

		gbc.gridx = 0;
		gbc.gridy = 2;
		gbl.setConstraints(bottomLabel, gbc);
		panel.add(bottomLabel);

        return panel;
    }

// Runnable implementation ////////////////////////////////////////////////////

	public void run() {
        startTime = System.currentTimeMillis();
        setBusy(true);
		String url = Main.appProperties.getProperty(PROP_BASE_URL) + "/file/post?form_cookie=";
		statusURL = Main.appProperties.getProperty(PROP_BASE_URL) + "/upload/status?skin=xmlhttprequest&form_cookie=";
		Uploader uploader = new Uploader(url, Integer.parseInt(Main.appProperties.getProperty("http.timeout", "30000")));
		File[] files = model.getFiles();
		//currentProgress.setMinimum(0);
		//currentProgress.setMaximum(files.length);
		String[] titles = model.getTitles();
		String[] tags = model.getTags();
		String[] descriptions = model.getDescriptions();
		// Create a properties object
		Properties props = new Properties();
		props.put(Uploader.USER_PARAM_KEY, model.getUsername());
		props.put(Uploader.PASS_PARAM_KEY, model.getPassword());
		for (int i = 0; i < files.length; i++) {
            filename = files[i].getName();
            // Generate a new GUID
			guid = new RandomGUID(false);
			uploader.setGuid(guid.toString());
			// put in the current data
			props.put(Uploader.TITLE_PARAM_KEY, titles[i]);
			//props.put(Uploader.TAGS_PARAM_KEY, tags[i]);
			props.put(Uploader.DESC_PARAM_KEY, descriptions[i]);
			// calculate the approximate transfer time for the next file
			// do the upload
			Thread thread = new Thread(timer);
			running = true;
			thread.start();
			boolean success = uploader.uploadFile(files[i], props);
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
			// take out the old data
			props.remove(Uploader.TITLE_PARAM_KEY);
			props.remove(Uploader.DESC_PARAM_KEY);
			//if (currentProgress.isIndeterminate())
				//currentProgress.setIndeterminate(false);
			//currentProgress.setValue(currentProgress.getValue() + 1);
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
        if (model.getFiles().length > 1) {
//            fileLabel = new JLabel("crap");
//            overallTimeLabel = new JLabel("garbage");
            fileLabel = new JLabel(I18n.getString(FILE_LABEL_KEY));
            overallTimeLabel = new JLabel(I18n.getString(OVERALL_TIME_LABEL_KEY));
            overallProgress = new JProgressBar();
            GridBagLayout gbl = (GridBagLayout)view.getLayout();
            GridBagConstraints gbc = new GridBagConstraints();
            JPanel panel = buildPanel(fileLabel, overallTimeLabel, overallProgress);
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbl.setConstraints(panel, gbc);
            view.add(panel);
        }
        setView(view);
		new Thread(this).start();
	}

	public void applyState() {
		// this is called when the user clicks "Next"
	}

	public Dimension getPreferredSize() {
		return view.getPreferredSize();
	}

// Class methods //////////////////////////////////////////////////////////////

    public static String parseSize(int bytes) {
        StringBuilder builder = new StringBuilder();
        // is it smaller than 1K?
        // TODO: externalize
        if (bytes < 1024)
            builder.append(bytes).append(" bytes");
        // is it smaller than 1M?
        else if(bytes < 1024000)
            builder.append(bytes / 1024).append(" KB");
        // is it smaller than 1G?
        else if (bytes < 1024000000)
            builder.append(bytes / 1024000).append(" MB");
        //else if (bytes < 1024000000000)
            //builder.append(bytes / 1024000000).append(" GB");
        return builder.toString();
    }

    public static String parseTime(long time) {
		long t;
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
