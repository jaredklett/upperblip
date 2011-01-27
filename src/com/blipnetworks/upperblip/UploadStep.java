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
import 	java.util.concurrent.ScheduledThreadPoolExecutor;
import 	java.util.concurrent.TimeUnit;

import 	javax.swing.*;
import 	javax.xml.parsers.ParserConfigurationException;

import 	com.blipnetworks.util.*;
import 	com.blipnetworks.util.I18n;
import 	com.blipnetworks.upperblip.UpperBlipModel.MapKeys;
import	com.blipnetworks.upperblip.wizard.*;
import 	edu.stanford.ejalbert.BrowserLauncher;
import 	org.xml.sax.SAXException;

/**
 *
 *
 * @author Jared Klett
 * @version $Id: UploadStep.java,v 1.36 2011/01/27 19:38:53 jklett Exp $
 */

public final class UploadStep extends AbstractWizardStep implements Runnable {

// CVS info ////////////////////////////////////////////////////////////////////

	public static final String CVS_REV = "$Revision: 1.36 $";

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

    private JFrame			frame = null;
    private MovementHandler	mover = null;
    
    protected boolean		statusFailure = false;
    
    private ArrayList<Map<String, Object>>	fileData = null;
    
// Constructor ////////////////////////////////////////////////////////////////

	public UploadStep() {
		super(I18n.getString(TITLE_KEY), I18n.getString(SUMMARY_KEY));

        setIcon(Icons.uploadIcon);
        postList = new ArrayList<String>();
        
        // Create and layout components
		progress = new JProgressBar();
		progress.setIndeterminate(true);
	}
	
// WizardStep implementation //////////////////////////////////////////////////

	public void init(WizardModel model) {
		this.model = (UpperBlipModel)model;
        fileData = this.model.getFileData();
	}

	public void prepare() {
				
		model.setOverridePreviousState(true);
		model.setPreviousAvailable(false);
		
		// Should never happen.
		if (fileData.size() <= 0) {
			JOptionPane.showMessageDialog(null, I18n.getString("upload.error.nofiles"));
			System.exit(0);
		}
		
		details = new UploadDetails(fileData.size());
		
		frame = Main.getMainInstance().getMainFrame();
		mover = Main.getMainInstance().getMover();
		JPanel	panel = createViewPanel();
		
		view = new JPanel();
		view.add(panel);
		setView(view);

		frame.pack();
		frame.setVisible(true);
		mover.positionFrame(true);
		
        new Thread(this).start();
    }
	
	public void applyState() {
	}

	public Dimension getPreferredSize() {
		return view.getPreferredSize();
	}

// Runnable implementation ////////////////////////////////////////////////////

    @SuppressWarnings("unchecked")
	public void run() {
        setBusy(true);
        
        Uploader uploader = new Uploader(model.getAuthCookie());
         
        // Create a properties object
		Properties props = new Properties();
		int	fileNumber = 0;
		for (Map<String, Object> map : fileData) {
            
            // Generate a new GUID
            guid = new RandomGUID();
            uploader.setGuid(guid.toString());
            
			// put in the current data
            String	item = null;
			props.put(Parameters.TITLE_PARAM_KEY, ((JTextField) map.get(MapKeys.TITLE.getKey())).getText().trim());
			props.put(Parameters.DESC_PARAM_KEY, ((JTextArea) map.get(MapKeys.DESCRIPTION.getKey())).getText().trim());
			props.put(Parameters.TAGS_PARAM_KEY, ((JTextField) map.get(MapKeys.TAGS.getKey())).getText().trim());
			String	key = (String) (((JComboBox) map.get(MapKeys.LICENSE.getKey())).getSelectedItem());
            props.put(Parameters.LICENSE_PARAM_KEY, MetadataLoader.licenses.get(key));
            key = (String) (((JComboBox) map.get(MapKeys.CATEGORY.getKey())).getSelectedItem());
            props.put(Parameters.CAT_PARAM_KEY, MetadataLoader.categories.get(key));
            key = (String) (((JComboBox) map.get(MapKeys.RATING.getKey())).getSelectedItem());
            props.put(Parameters.RATING_PARAM_KEY, MetadataLoader.ratings.get(key));
            key = (String) (((JComboBox) map.get(MapKeys.LANGUAGE.getKey())).getSelectedItem());
            props.put(Parameters.LANGUAGE_PARAM_KEY, MetadataLoader.languages.get(key));
            item = (((JCheckBox) map.get(MapKeys.EXPLICIT.getKey())).isSelected())? "1" : "0";
            props.put(Parameters.EXPLICIT_PARAM_KEY, item);
            
            List<String>	crossposts = new ArrayList<String>();
            List<String>	conversions = new ArrayList<String>();
            boolean 		blogsFound = false;
            boolean			convsFound = false;
            
            ArrayList<JCheckBoxMenuItem>	crosspostList = (ArrayList<JCheckBoxMenuItem>) map.get(MapKeys.CROSSPOSTING.getKey());
            if (crosspostList.size() > 0) {
            	blogsFound = true;
            	Map<String, String>	crosspostMap = model.getCrossposts();
            	for (JCheckBoxMenuItem menuItem : crosspostList) {
            		if (menuItem.isSelected()) {
            			crossposts.add(crosspostMap.get(menuItem.getText()));
            		}
            	}
            }
            
            ArrayList<JCheckBoxMenuItem>	uploads = (ArrayList<JCheckBoxMenuItem>) map.get(MapKeys.CROSSUPLOADING.getKey());
            if (uploads.size() > 0) {
            	Map<String, String>	crossuploadMap = model.getCrossuploads();
            	for (JCheckBoxMenuItem menuItem : uploads) {
            		if (menuItem.isSelected()) {
            			props.put(Parameters.IA_PARAM_KEY, crossuploadMap.get(menuItem.getText()));
            		}
            	}
            }
            
            if (((JCheckBox) map.get(MapKeys.MP3AUDIO.getKey())).isSelected()) {
            	conversions.add("mp3:web");
            	convsFound = true;
            }
	            
            if (((JCheckBox) map.get(MapKeys.M4VIDEO.getKey())).isSelected()) {
            	conversions.add("m4v:web");
            	convsFound = true;
            }
            
            if (model.isPrivateFiles()) {
            	props.put("hidden", (((JCheckBox) map.get(MapKeys.PRIVACY.getKey())).isSelected())? "1" : "0");
	            
	            if (((JCheckBox) map.get(MapKeys.PROTECTED.getKey())).isSelected()) {
	            	props.put("hidden_visible_password", "1");
	            
		            JPasswordField	field = ((JPasswordField) map.get(MapKeys.PASSWORD.getKey()));
		            String	password = field.getPassword().toString().trim();
		            if (!password.equals("")) {
		            	props.put("hidden_password", password);
		            }
	            }
	            else {
	            	props.put("hidden_visible_password", "0");
	            	props.put("hidden_password", "");
	            }
	            
		        if (((JCheckBox) map.get(MapKeys.MAKEPUBLIC.getKey())).isSelected()) {
		        	props.put("enable_next_hidden_state", "1");
	            
		        	JTextField	datetime = (JTextField) map.get(MapKeys.DATETIME.getKey());
		        	if (!datetime.getText().equals("")) {
		        		props.put("next_hidden_date", datetime.getText());
		        	}
		        }
		        else {
		        	props.put("enable_next_hidden_state", "0");
		        	props.put("next_hidden_date", "");
		        }
            }
            //System.out.println("UploadStep: properties = " + props);
            //System.out.println("UploadStep: crossposts = " + crossposts);
            //System.out.println("UploadStep: conversions = " + conversions);
            
            // This thread pool is to handle the upload status updates.
            ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(5);
            UploadWatchTask				timerTask = new UploadWatchTask(fileNumber);
            
            stpe.scheduleAtFixedRate(timerTask, STATUS_INTERVAL, STATUS_INTERVAL, TimeUnit.MILLISECONDS);

			boolean success = false;
			statusFailure = false;
			JLabel	failReason = null;
			
        	File		videoFile = (File) map.get(MapKeys.VIDEOFILE.getKey());
        	File		thumbnailFile = null;
        	JComboBox	thumbnail = (JComboBox) map.get(MapKeys.THUMBNAIL.getKey());
        	
            if (thumbnail.getSelectedIndex() != 0) {
                String filename = (String) thumbnail.getSelectedItem();
                thumbnailFile = model.getThumbNails().get(filename);
            }
            
            if (blogsFound || convsFound) {
                try {
                    success = uploader.uploadFile(videoFile, thumbnailFile, props, crossposts, conversions);
                } catch (IOException e) {
                	failReason = new JLabel(I18n.getString("upload.error.commerror"));
                } catch (ParserConfigurationException e) {
                	failReason = new JLabel(I18n.getString("upload.error.commerror"));
                } catch (SAXException e) {
                	failReason = new JLabel(I18n.getString("upload.error.commerror"));
                }
            }
            else {
                try {
                    success = uploader.uploadFile(videoFile, thumbnailFile, props);
                } catch (IOException e) {
                	failReason = new JLabel(I18n.getString("upload.error.commerror"));
                } catch (ParserConfigurationException e) {
                	failReason = new JLabel(I18n.getString("upload.error.commerror"));
                } catch (SAXException e) {
                	failReason = new JLabel(I18n.getString("upload.error.commerror"));
                }
            }
            
            // Halt the timer thread
            stpe.shutdownNow();
            try {
            	while (!stpe.awaitTermination(STATUS_INTERVAL + 100, TimeUnit.MILLISECONDS)) {
            	}
            } catch (InterruptedException e) {
            	// Don't care; ignore
            }
            
            if (success) {
            	progress.setValue(progress.getMaximum());
                postList.add(uploader.getPostURL());
                details.setTimeLeft("0 sec", fileNumber);
                details.setBytesLoaded(timerTask.getTotalBytes(), timerTask.getTotalBytes(), fileNumber);
                details.setSuccess(fileNumber);
                
                final int index = fileNumber;

                LinkLabel linkLabel = new LinkLabel(I18n.getString("upload.link.view"), new Command() {
                    public void execute() {
                        try {
                            BrowserLauncher bl = new BrowserLauncher(null);
                            bl.openURLinBrowser(postList.get(index));
                        }
                        catch (Exception e) {
                        	JOptionPane.showMessageDialog(null, I18n.getString("upload.error.browser"));
                        }
                    }
                });
                
                details.setSummaryPost(fileNumber, "", null);
                details.setComment(fileNumber, "", null);
                details.setSummaryPost(fileNumber, linkLabel, Color.blue);
            } else {
            	if (failReason == null) {
                	failReason = new JLabel(I18n.getString("upload.error.commerror"));
            	}
            	details.setFailure(fileNumber);
            	details.setSummaryPost(fileNumber, "", null);
            	details.setComment(fileNumber, "", null);
            	details.setSummaryPost(fileNumber, failReason, Color.red);
            }
            details.repaint();
            frame.pack();
            
			// take out the old data
            props.clear();
            fileNumber++;
		}
		
        progress.setValue(progress.getMaximum());
        setBusy(false);
		setComplete(true);
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
	
	/**
	 * This class produces a table of data in the form of sections for each
	 * video file scheduled for upload. There are three columns and four rows
	 * per section and one section for each upload file.
	 * 
	 * @author dsklett
	 *
	 */
	@SuppressWarnings("serial")
	private final class UploadDetails extends JPanel {
		
		private int			sectionsPerRow = 2;
		private Grid		grid = new Grid();
		private JLabel[][]	labels = null;
		private int			numberFiles = 0;

		private Color		darkRed = new Color(0xff, 0x00, 0x15);
		private Color		darkGreen = new Color(0x00, 0x7f, 0x00);
		
		public UploadDetails(int numberFiles) {			
			super();
			
			this.numberFiles = numberFiles;
			if (this.numberFiles <= 0) {
				return;
			}
			
			setLayout(new GridBagLayout());
			grid.setAnchor(Grid.westAnchor);
			grid.setInsets(0, 5, 0, 5);
			
			labels = new JLabel[numberFiles * 4 + numberFiles - 1][sectionsPerRow * 4];
			initializeTable(numberFiles);
		}
		
		/**
		 * This method fills in the File: field with the current video file name.
		 * 
		 * @param fileName name of current video file
		 * @param fileNumber the index of the current video file
		 */
		public void setFile(String fileName, int fileNumber) {
			
			if (numberFiles <= 0) {
				return;
			}
			
			if (fileName.length() <= 20) {
				setValueAt(fileName, calculateX(fileNumber) + 1, calculateY(fileNumber), null);
				return;
			}
			
			StringBuilder	name = new StringBuilder();
			name.append(fileName.substring(0, 10));
			name.append(Character.toString('\u2026'));	// u2026 is an ellipses
			name.append(fileName.substring(fileName.length() - 9));
			setValueAt(name.toString(), calculateX(fileNumber) + 1, calculateY(fileNumber), null);
		}
		
		/**
		 * This method fills in Estimated Time Left: field.
		 * 
		 * @param timeLeft calculated from the status data
		 * @param fileNumber index for table section
		 */
		public void setTimeLeft(String timeLeft, int fileNumber) {
			
			if (numberFiles <= 0) {
				return;
			}
			setValueAt(timeLeft, calculateX(fileNumber) + 1, calculateY(fileNumber) + 1, null);
		}
		
		/**
		 * This method fills in the field.
		 * 
		 * @param read bytes uploaded so far
		 * @param total size of file to upload
		 * @param fileNumber index of current upload file
		 */
		public void setBytesLoaded(int read, int total, int fileNumber) {
			
			if (numberFiles <= 0) {
				return;
			}
			
			Formatter	bytesLoaded = new Formatter();
			setValueAt(bytesLoaded.format("%.1f MB of %.1f MB", read / 1000000f, total / 1000000f).toString(), 
					calculateX(fileNumber) + 1, calculateY(fileNumber) + 2, null);
		}
		
		/**
		 * This method fills in the field.
		 * 
		 * @param bitsPerSec current transfer rate in kB per sec
		 * @param fileNumber index of current upload file
		 */
		public void setTransferRate(float bitsPerSec, int fileNumber) {
			
			if (numberFiles <= 0) {
				return;
			}
			
			Formatter	transferRate = new Formatter();
			setValueAt(transferRate.format("%.1f KB/s", bitsPerSec / 1000f).toString(), calculateX(fileNumber) + 1, 
									calculateY(fileNumber) + 3, null);
		}
		
		/**
		 * This method marks the current upload section of table as a successful status.
		 * 
		 * @param fileNumber
		 */
		public void setSuccess(int fileNumber) {
			
			if (numberFiles <= 0) {
				return;
			}
			
			JLabel	label = new JLabel(new String(Character.toString('\u2714')) + " Success");	// u2714 is a bold check mark
			setValueAt(label, calculateX(fileNumber) + 2, calculateY(fileNumber), darkGreen);
		}
		
		/**
		 * This method marks the current upload section of the table with a failure status.
		 * 
		 * @param fileNumber
		 */
		public void setFailure(int fileNumber) {
			
			if (numberFiles <= 0) {
				return;
			}
			
			JLabel	label = new JLabel(new String(Character.toString('\u2718')) + " Failed");	// u2718 is a bold X
			setValueAt(label, calculateX(fileNumber) + 2, calculateY(fileNumber), darkRed);
		}
		
		/**
		 * This method controls the content of the summary field. This field can contain information
		 * about the upload, such as a link to start the local browser to view the uploaded file.
		 * Other information such as status failures can also be displayed.
		 * 
		 * @param fileNumber
		 * @param label
		 * @param color
		 */
		public void setSummaryPost(int fileNumber, JLabel label, Color color) {
			setValueAt(label, calculateX(fileNumber) + 2, calculateY(fileNumber) + 1, color);
		}

		/**
		 * This method controls the content of the summary field. This field can contain information
		 * about the upload, such as a link to start the local browser to view the uploaded file.
		 * Other information such as status failures can also be displayed.
		 * 
		 * @param fileNumber
		 * @param text
		 * @param color
		 */
		public void setSummaryPost(int fileNumber, String text, Color color) {
			setValueAt(text, calculateX(fileNumber) + 2, calculateY(fileNumber) + 1, color);
		}

		/**
		 * This method controls the contents of a general comment field.
		 * 
		 * @param fileNumber
		 * @param text
		 * @param color
		 */
		public void setComment(int fileNumber, String text, Color color) {
			setValueAt(text, calculateX(fileNumber) + 2, calculateY(fileNumber) + 2, color);
		}
		
		/**
		 * This utility method either updates the text (with color) in the JLabel
		 * component contained at the coordinates (x,y) or adds the label into the
		 * layout at the coordinates (x,y) and updates the array of JLabel components.
		 * 
		 * @param text String used to update text portion of JLabel
		 * @param x GridBagLayout gridx value
		 * @param y GridBagLayout gridy value
		 * @param color the color for the text if not null
		 */
		private void setValueAt(String text, int x, int y, Color color) {
			JLabel	label = labels[y][x];
			
			if (label != null) {
				if (color != null) {
					label.setForeground(color);
				}
				label.setText(text);
				return;
			}
			
			labels[y][x] = new JLabel(text);
			if (color != null) {
				labels[y][x].setForeground(color);
			}
			else {
				labels[y][x].setForeground(Color.black);
			}
			
			grid.setXY(x, y);
			add(labels[y][x], grid);
		}

		/**
		 * This utility method either updates the text (with color) in the JLabel
		 * component contained at the coordinates (x,y) or adds the label into the
		 * layout at the coordinates (x,y) and updates the array of JLabel components.
		 * The content component can also be a JSeparator, used to clarify the table
		 * contents.
		 * 
		 * @param content a JLabel
		 * @param x GridBagLayout gridx value
		 * @param y GridBagLayout gridy value
		 * @param color the color for the text if not null
		 */
		private void setValueAt(Component content, int x, int y, Color color) {
			JLabel	label = labels[y][x];
			
			if (content instanceof LinkLabel) {
				labels[y][x] = (JLabel) content;
				grid.setXY(x, y);
				add(content, grid);
				return;
			}
			
			if (label != null) {
				if (color != null) {
					label.setForeground(color);
				}
				label.setText(((JLabel)content).getText());
				return;
			}
			
			grid.setXY(x, y);
			if (color != null) {
				content.setForeground(color);
			}
			add(content, grid);
			if (content instanceof JSeparator) {
				return;
			}
			
			labels[y][x] = (JLabel) content;
		}
		
		/**
		 * This method calculates the GridBagConstraints x-value for the section
		 * of the table containing the status for the parameter file number.
		 * 
		 * @param fileNumber
		 * @return grid x-value
		 */
		private int calculateX(int fileNumber) {
			return fileNumber % sectionsPerRow * 4;
		}
		
		/**
		 * This method calculates the GridBagConstraints y-value for the section
		 * of the table containing the status for the parameter file number.
		 * 
		 * @param fileNumber
		 * @return grid y-value
		 */
		private int calculateY(int fileNumber) {
			return 5 * (fileNumber / sectionsPerRow);
		}
		
		/**
		 * Given the number of files that require status information, this method
		 * initializes the grid with the fixed fields.
		 * 
		 * @param numberFiles number of files to monitor
		 */
		private void initializeTable(int numberFiles) {
			
			for (int i = 0; i < numberFiles; i++) {
				int xValue = calculateX(i);
				int yValue = calculateY(i);
				
				if (i > sectionsPerRow - 1 && xValue == 0) {
					grid.setColumnSpan(4 * sectionsPerRow);
					grid.setFill(Grid.horizontalFill);
					JSeparator	separator = new JSeparator();
					separator.setForeground(Color.blue);
					setValueAt(separator, xValue, yValue - 1, null);
					grid.setColumnSpan(1);
					grid.setFill(Grid.noneFill);
				}
				
				setValueAt(new JLabel(I18n.getString("upload.detail.file")), xValue, yValue++, null);
				setValueAt(new JLabel(I18n.getString("upload.detail.time")), xValue, yValue++, null);
				setValueAt(new JLabel(I18n.getString("upload.detail.bytes")), xValue, yValue++, null);
				setValueAt(new JLabel(I18n.getString("upload.detail.rate")), xValue, yValue, null);
			}
		}
	}
	
	/**
	 * This class retrieves the latest upload status data from blip.tv and updates the
	 * detail table information.
	 * 
	 * @author dsklett
	 *
	 */
	private final class UploadWatchTask implements Runnable {
		
		private	int				fileNumber = 0;
		private UploadStatus	status = null;
		private boolean			initialized = false;
		private int				totalBytes = 0;
				
		public UploadWatchTask(int fileNumber) {
			this.fileNumber = fileNumber;
		}
		
		public void run() {
			if (statusFailure) {
				details.setSummaryPost(fileNumber, "", null);
				statusFailure = false;
			}
			
			/*if (Math.random() < 0.15d) {
				setStatus();
				return;
			}*/
			
			try {
				status = UploadStatus.getStatus(guid.toString(), model.getAuthCookie());
			} catch(SAXException e) {
				setStatus();
				return;
			} catch (IOException e) {
				setStatus();
				return;
			} catch (ParserConfigurationException e) {
				setStatus();
				return;
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
				details.repaint();
				frame.pack();
				mover.positionFrame(true);
			}
		}
		
		public int getTotalBytes() {
			return totalBytes;
		}
		
		private void setStatus() {
			statusFailure = true;
			details.setSummaryPost(fileNumber, I18n.getString("upload.error.statusfailure"), Color.red);
			details.setComment(fileNumber, I18n.getString("upload.error.uploadcontinues"), null);
			details.repaint();
			frame.pack();
		}
	}

} // class UploadStep
