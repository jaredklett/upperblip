/* 
 * @(#)UploadStep.java
 *
 * Copyright (c) 2005-2007 by Blip Networks, Inc.
 * 407 Broome St., 5th Floor
 * New York, NY 10013
 * All rights reserved.
 *
 * This software is the confidential and
 * proprietary information of Blip Networks, Inc.
 */

package com.blipnetworks.upperblip;

import 	java.awt.*;
import 	java.io.*;
import 	java.util.*;
import 	java.util.List;
import 	java.util.Timer;

import 	javax.swing.*;
import 	javax.xml.parsers.ParserConfigurationException;

import 	com.blipnetworks.util.*;
import 	com.blipnetworks.util.I18n;
import	com.blipnetworks.upperblip.wizard.*;
import 	edu.stanford.ejalbert.BrowserLauncher;
import 	org.xml.sax.SAXException;

/**
 *
 *
 * @author Jared Klett
 * @version $Id: UploadStep.java,v 1.35 2009/06/22 21:07:45 jklett Exp $
 */

public class UploadStep extends AbstractWizardStep implements Runnable {

// CVS info ////////////////////////////////////////////////////////////////////

	public static final String CVS_REV = "$Revision: 1.35 $";

// Static variables ////////////////////////////////////////////////////////////

    private static final int STATUS_INTERVAL = 3000;

	/** blah */
	private static final String TITLE_KEY = "upload.title";
	/** blah */
	private static final String SUMMARY_KEY = "upload.summary";
    /** blah */
    private static final String BORDER_LABEL_KEY = "upload.border.label";
	/** blah */
	public static final String PROP_BASE_URL = "base.url";
    /** blah */
    public static final String ERROR_TITLE = "upload.error.title";
    public static final String DONE_LABEL_KEY = "upload.done.msg";

// Instance variables //////////////////////////////////////////////////////////

    /** */
    private RandomGUID 		guid;
	/** */
	private JPanel 			view;
	/** */
	private UpperBlipModel 	model;
	/** */
	private JProgressBar 	progress;
	private UploadDetails	details = null;
	private JScrollPane		scrollPane = null;
    private List<String> 	postList;
    private boolean 		done;

    private JFrame			frame = null;
    private MovementHandler	mover = null;
    
// Constructor ////////////////////////////////////////////////////////////////

	public UploadStep() {
		super(I18n.getString(TITLE_KEY), I18n.getString(SUMMARY_KEY));

        done = false;
        setIcon(Icons.uploadIcon);
        postList = new ArrayList<String>();
        
        // Create and layout components
		progress = new JProgressBar();
		progress.setIndeterminate(true);
	}
	
// WizardStep implementation //////////////////////////////////////////////////

	public void init(WizardModel model) {
		this.model = (UpperBlipModel)model;
	}

	public void prepare() {
		model.setOverridePreviousState(true);
		model.setPreviousAvailable(false);
		
		details = new UploadDetails(model.getFiles().length);
		
		frame = Main.getMainInstance().getMainFrame();
		mover = Main.getMainInstance().getMover();
		JPanel	panel = createViewPanel();
		
		view = new JPanel();
		view.add(panel);
		setView(view);

		frame.pack();
		frame.setVisible(true);
		mover.positionFrame(true);
		
        if (!done)
            new Thread(this).start();
    }

	public void applyState() {
	}

	public Dimension getPreferredSize() {
		return view.getPreferredSize();
	}

// Runnable implementation ////////////////////////////////////////////////////

    public void run() {
        setBusy(true);
        
        Uploader uploader = new Uploader(model.getAuthCookie());
        
        File[] 		files = model.getFiles();
		String[] 	titles = model.getTitles();
		String[] 	tags = model.getTags();
		String[] 	descriptions = model.getDescriptions();
        String[] 	licenses = model.getLicenses();
        String[] 	categories = model.getCategories();
        String[] 	languages = model.getLanguages();
        String[] 	explicitFlags = model.getExplicitFlags();
        String[] 	ratings = model.getRatings();
        String[][] 	blogs = model.getCrossposts();
        String[][] 	destinations = model.getCrossuploads();
        String[]	mp3Audios = model.getMp3Audios();
        String[]	mpeg4Videos = model.getMpeg4Videos();
        String[]	privateFiles = model.getPrivateFiles();
        String[]	passwordFiles = model.getPasswordFiles();
        String[]	passwordFields = model.getPasswordFields();
        String[]	makePublicFiles = model.getMakePublicFiles();
        String[]	makePublicFields = model.getMakePublicFields();
        File[] 		thumbnails = model.getThumbnails();
 
        // Create a properties object
		Properties props = new Properties();
		for (int i = 0; i < files.length; i++) {
            
            // Generate a new GUID
            guid = new RandomGUID();
            uploader.setGuid(guid.toString());
            
			// put in the current data
			props.put(Parameters.TITLE_PARAM_KEY, titles[i]);
			props.put(Parameters.TAGS_PARAM_KEY, tags[i]);
			props.put(Parameters.DESC_PARAM_KEY, descriptions[i]);
            props.put(Parameters.LICENSE_PARAM_KEY, licenses[i]);
            props.put(Parameters.CAT_PARAM_KEY, categories[i]);
            props.put(Parameters.RATING_PARAM_KEY, ratings[i]);
            props.put(Parameters.LANGUAGE_PARAM_KEY, languages[i]);
            props.put(Parameters.EXPLICIT_PARAM_KEY, explicitFlags[i]);
            
            List<String>	crossposts = new ArrayList<String>();
            List<String>	conversions = new ArrayList<String>();
            boolean 		blogsFound = false;
            boolean			convsFound = false;
            
            for (int j = 0; j < blogs[i].length; j++) {
                if (blogs[i][j] != null) {
                    crossposts.add(blogs[i][j]);
                    blogsFound = true;
                }
            }
            for (int j = 0; j < destinations[i].length; j++) {
                if (destinations[i][j] != null) {
                    props.put(Parameters.IA_PARAM_KEY, destinations[i][j]);
                }
            }
            
            if (mp3Audios[i].equals("1")) {
            	conversions.add("mp3:web");
            	convsFound = true;
            }
            
            if (mpeg4Videos[i].equals("1")) {
            	conversions.add("m4v:web");
            	convsFound = true;
            }
            
            if (privateFiles[i].equals("1")) {
            	props.put("hidden", "1");
            }
            
            if (passwordFiles[i].equals("1")) {
            	props.put("hidden_visible_password", "1");
            }
            
            if (!passwordFields[i].equals("")) {
            	props.put("hidden_password", passwordFields[i]);
            }
            
            if (makePublicFiles[i].equals("1")) {
            	props.put("enable_next_hidden_state", "1");
            }
            
            if (!makePublicFields[i].equals("")) {
            	props.put("next_hidden_date", makePublicFields[i]);
            }
                        
            Timer	timer = new Timer(false);
            UploadWatchTask	timerTask = new UploadWatchTask(i);
            timer.scheduleAtFixedRate(timerTask, STATUS_INTERVAL, STATUS_INTERVAL);
			
			boolean success = false;
            if (blogsFound || convsFound)
                try {
                    success = uploader.uploadFile(files[i], thumbnails[i], props, crossposts, conversions);
                } catch (IOException e) {
                	Main.getMainInstance().abortApplication(e);
                } catch (ParserConfigurationException e) {
                	Main.getMainInstance().abortApplication(e);
                } catch (SAXException e) {
                	Main.getMainInstance().abortApplication(e);
                }
            else
                try {
                    success = uploader.uploadFile(files[i], thumbnails[i], props);
                } catch (IOException e) {
                	Main.getMainInstance().abortApplication(e);
                } catch (ParserConfigurationException e) {
                	Main.getMainInstance().abortApplication(e);
                } catch (SAXException e) {
                	Main.getMainInstance().abortApplication(e);
                }
            
            // Halt the timer thread
            timer.cancel();
            
            if (success) {
            	progress.setValue(progress.getMaximum());
                postList.add(uploader.getPostURL());
                details.setTimeLeft("0 sec", i);
                details.setBytesLoaded(timerTask.getTotalBytes(), timerTask.getTotalBytes(), i);
                details.setSuccess(i);
                final int index = i;

                LinkLabel linkLabel = new LinkLabel("Click to view", new Command() {
                    public void execute() {
                        try {
                            BrowserLauncher bl = new BrowserLauncher(null);
                            bl.openURLinBrowser(postList.get(index));
                        }
                        catch (Exception e) {
                        	Main.getMainInstance().abortApplication(e);
                        }
                    }
                });
                
                details.setSummaryPost(i, linkLabel);
                frame.pack();
            } else {
            	details.setFailure(i);
            	frame.pack();
            }
			// take out the old data
            props.clear();
		}
		
        progress.setValue(progress.getMaximum());
        setBusy(false);
		setComplete(true);
        done = true;
        model.setLastAvailable(false);
    }

// Class methods //////////////////////////////////////////////////////////////
    
	private JPanel createViewPanel() {
		JPanel	panel = new JPanel(new GridBagLayout());
		Grid	grid = new Grid();
		
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createTitledBorder(I18n.getString(BORDER_LABEL_KEY)),BorderFactory.createEmptyBorder(5, 5, 5, 5)),
						panel.getBorder()));
		
		grid.setRelativeY();
		grid.setFill(Grid.horizontalFill);
		panel.add(progress, grid);
		panel.add(details, grid);
		scrollPane = new JScrollPane(panel);
		scrollPane.setPreferredSize(new Dimension(750, 550));
		
		return panel;
	}
	
	/**
	 * Given a time interval in seconds, this method calculates this
	 * interval is seconds, minutes, hours, etc.
	 * 
	 * @param time
	 * @return
	 */
	private static String calculateTime(long time) {
		long[]			factors = { 60L, 60L, 24L, 365L };
		String[]		units = { " sec", " min", " hr", " day", " yr" };
		ArrayList<Long>	parts = new ArrayList<Long>();
		long			result = 0L;
		long			remainder = 0L;
		long			interval = time;
		StringBuilder	str = new StringBuilder();
		
		for (int i = 0; i < factors.length; i++) {
			result = interval / factors[i];
			remainder = interval % factors[i];
			parts.add(remainder);
			if (result == 0) {
				break;
			}
			if (result < factors[i]) {
				parts.add(result);
				break;
			}
			interval = result;
		}
		
		for (int i = 0; i < parts.size(); i++) {
			str.insert(0, units[i]);
			str.insert(0, parts.get(i));
			str.insert(0, " ");
		}
		return str.toString();
	}
	
//// Inner classes ////
	
	@SuppressWarnings("serial")
	private final class UploadDetails extends JPanel {
		
		private int				sectionsPerRow = 2;
		private Grid			grid = new Grid();
		private JLabel[][]		labels = null;

		public UploadDetails(int numberFiles) {			
			super();
			
			setLayout(new GridBagLayout());
			grid.setAnchor(Grid.westAnchor);
			grid.setInsets(0, 5, 0, 5);
			
			labels = new JLabel[numberFiles * 4 + numberFiles - 1][sectionsPerRow * 4];
			initializeTable(numberFiles);
		}
		
		public void setFile(String fileName, int fileNumber) {
			if (fileName.length() <= 20) {
				setValueAt(fileName, calculateX(fileNumber) + 1, calculateY(fileNumber));
				return;
			}
			
			StringBuilder	name = new StringBuilder();
			name.append(fileName.substring(0, 10));
			name.append(Character.toString('\u2026'));	// u2026 is an ellipses
			name.append(fileName.substring(fileName.length() - 9));
			setValueAt(name.toString(), calculateX(fileNumber) + 1, calculateY(fileNumber));
		}
		
		public void setTimeLeft(String timeLeft, int fileNumber) {
			setValueAt(timeLeft, calculateX(fileNumber) + 1, calculateY(fileNumber) + 1);
		}
		
		public void setBytesLoaded(int read, int total, int fileNumber) {
			Formatter	bytesLoaded = new Formatter();
			setValueAt(bytesLoaded.format("%.1f MB of %.1f MB", read / 1000000f, total / 1000000f).toString(), 
					calculateX(fileNumber) + 1, calculateY(fileNumber) + 2);
		}
		
		public void setTransferRate(float bitsPerSec, int fileNumber) {
			Formatter	transferRate = new Formatter();
			setValueAt(transferRate.format("%.1f KB/s", bitsPerSec / 1000f).toString(), calculateX(fileNumber) + 1, 
									calculateY(fileNumber) + 3);
		}
		
		public void setSuccess(int fileNumber) {
			JLabel	label = new JLabel(new String(Character.toString('\u2714')) + " Success");	// u2714 is a bold check mark
			label.setForeground(new Color(0x00, 0x7f, 0x00));
			setValueAt(label, calculateX(fileNumber) + 2, calculateY(fileNumber));
		}
		
		public void setFailure(int fileNumber) {
			JLabel	label = new JLabel(new String(Character.toString('\u2718')) + " Failed");	// u2718 is a bold X
			setForeground(new Color(0xff, 0x00, 0x15));
			setValueAt(label, calculateX(fileNumber) + 2, calculateY(fileNumber));
		}
		
		public void setSummaryPost(int fileNumber, JLabel label) {
			setValueAt(label, calculateX(fileNumber) + 2, calculateY(fileNumber) + 1);
		}
		
		private void setValueAt(String text, int x, int y) {
			JLabel	label = labels[y][x];
			
			if (label != null) {
				label.setText(text);
				return;
			}
			
			labels[y][x] = new JLabel(text);
			grid.setXY(x, y);
			add(labels[y][x], grid);
		}

		private void setValueAt(Component content, int x, int y) {
			JLabel	label = labels[y][x];
			
			if (label != null) {
				label.setText(((JLabel)content).getText());
				return;
			}
			
			grid.setXY(x, y);
			add(content, grid);
			if (content instanceof JSeparator) {
				return;
			}
			
			labels[y][x] = (JLabel)content;
		}
		
		private int calculateX(int fileNumber) {
			return fileNumber % sectionsPerRow * 4;
		}
		
		private int calculateY(int fileNumber) {
			return 5 * (fileNumber / sectionsPerRow);
		}
		
		private void initializeTable(int numberFiles) {
			
			for (int i = 0; i < numberFiles; i++) {
				int xValue = calculateX(i);
				int yValue = calculateY(i);
				
				if (i > sectionsPerRow - 1 && xValue == 0) {
					grid.setColumnSpan(4 * sectionsPerRow);
					grid.setFill(Grid.horizontalFill);
					JSeparator	separator = new JSeparator();
					separator.setForeground(Color.blue);
					setValueAt(separator, xValue, yValue - 1);
					grid.setColumnSpan(1);
					grid.setFill(Grid.noneFill);
				}
				
				setValueAt(new JLabel("File:"), xValue, yValue++);
				setValueAt(new JLabel("Estimated time left:"), xValue, yValue++);
				setValueAt(new JLabel("Bytes loaded:"), xValue, yValue++);
				setValueAt(new JLabel("Transfer rate:"), xValue, yValue++);
			}
		}
	}
	
	private final class UploadWatchTask extends TimerTask {
		
		private	int				fileNumber = 0;
		private UploadStatus	status = null;
		private boolean			initialized = false;
		private int				totalBytes = 0;
		
		public UploadWatchTask(int fileNumber) {
			this.fileNumber = fileNumber;
		}
		
		public void run() {
			try {
				status = UploadStatus.getStatus(guid.toString(), model.getAuthCookie());
			} catch(SAXException e) {
				Main.getMainInstance().abortApplication(e);
			} catch (IOException e) {
				Main.getMainInstance().abortApplication(e);
			} catch (ParserConfigurationException e) {
				Main.getMainInstance().abortApplication(e);
			}
			
			if (status != null) {
				if (!initialized) {
					details.setFile(status.getFilename(), fileNumber);
					progress.setIndeterminate(false);
					progress.setMaximum(status.getTotal());
					frame.pack();
					mover.positionFrame(true);
					initialized = true;
				}
				
				long	start = status.getStart();
				long	update = status.getUpdate();
				int		read = status.getRead();
				int		total = status.getTotal();
				
				progress.setValue(read);
				totalBytes = total;
				
				long	delta = update - start;	// elapsed time in secs
				if (delta == 0) {
					return;
				}
				
				float	bitsPerSec = read / delta;
				long	time = (long)((total - read) / bitsPerSec);
				details.setTimeLeft(calculateTime(time).trim(), fileNumber);
				details.setBytesLoaded(read, total, fileNumber);
				details.setTransferRate(bitsPerSec, fileNumber);
				frame.pack();
				mover.positionFrame(true);
			}
		}
		
		public int getTotalBytes() {
			return totalBytes;
		}
	}

} // class UploadStep
