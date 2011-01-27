package com.blipnetworks.upperblip;

import	java.awt.*;
import	java.awt.event.*;
import java.util.Observable;

import	javax.swing.*;
import	javax.swing.border.*;

import com.blipnetworks.util.I18n;

/**
 * This class displays a specialized dialog window used to solicit input from the
 * customer related to an unexpected exception. The calling class is notified when
 * the customer has finished entering their comments, using the Observer/Observable
 * classes. There is a limit of 2000 characters on input and there is a keyboard
 * handler that checks the number of characters entered. The dialog window can be
 * closed only by selecting the OK button.
 * 
 * @author dsklett
 * @version $Id: ExceptionDialog.java,v 1.2 2011/01/27 19:38:53 jklett Exp $
 */
@SuppressWarnings("serial")
public final class ExceptionDialog extends JDialog {

	private Grid		grid = new Grid();
	private JPanel		contentPane = new JPanel(new GridBagLayout());
	private JTextArea	text = new JTextArea(10, 60);
	private JScrollPane	scroll = new JScrollPane(text);
	private Font		font = new Font("Courier", Font.PLAIN, 12);
	private Font		titleFont = new Font("Helvetica", Font.BOLD + Font.ITALIC, 18);
	private JLabel		main = new JLabel(I18n.getString("main.exception.text"));
	private JLabel		report = new JLabel(I18n.getString("main.report.text"));
	private JLabel		activity = new JLabel(I18n.getString("main.activity.text"));
	private JButton		okay = new JButton("OK");
	
	private TextObservable		observable = new TextObservable();
	private KeyHandler			keyHandler = new KeyHandler();
	private ExceptionReporter	reporter = null;
	
	public ExceptionDialog(JFrame parent) {
		super(parent, true);

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setContentPane(contentPane);
		setTitle("Exception");
		
		parent.dispose();
	}
	
	/**
	 * This is the only callable method. It constructs and displays the
	 * dialog window. The listener for the OK button is contained in this method.
	 * When the customer has finished entering information and selects the OK
	 * button, the ExceptionReporter class is called to construct the exception
	 * report e-mail and send same.
	 * 
	 * @param reporter
	 */
	public void displayDialog(ExceptionReporter reporter) {
		this.reporter = reporter;
		observable.addObserver(reporter);
		
		contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		text.setLineWrap(true);
		text.setWrapStyleWord(true);
		text.setFont(font);
		text.addKeyListener(keyHandler);
		text.setDragEnabled(false);
		
		grid.setRelativeY();
		grid.setAnchor(Grid.westAnchor);
		main.setForeground(Color.red);
		main.setFont(titleFont);
		contentPane.add(main, grid);
		contentPane.add(report, grid);
		contentPane.add(activity, grid);
		grid.setInsets(5, 0, 5, 0);
		contentPane.add(scroll, grid);
		grid.setAnchor(Grid.eastAnchor);
		contentPane.add(okay, grid);
		
		okay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				observable.update();
				observable.notifyObservers(text.getText().trim());
				sendExceptionReport();
				dispose();
				System.exit(0);
			}
		});
		
		Toolkit	kit = Toolkit.getDefaultToolkit();
		Dimension	screen = kit.getScreenSize();
		Dimension	dialogSize = getPreferredSize();
		setLocation((screen.width - dialogSize.width) / 2, (screen.height - dialogSize.height) / 2);
		
		pack();
		setVisible(true);
	}
	
	/**
	 * This method is the interface to the ExceptionReporter class. The
	 * reporter is executed as a separate thread.
	 */
	private void sendExceptionReport() {
    	Thread	thread = new Thread(reporter);
    	
    	thread.start();
    	try {
			thread.join();
		} catch (InterruptedException e1) {
			// ignore; don't care
		}
	}
	
	/**
	 * This private class extends the Observable class and is used to
	 * signal that a notification is armed and ready to notify observers.
	 * 
	 * @author dsklett
	 *
	 */
	private final class TextObservable extends Observable {
		
		public void update() {
			setChanged();
		}
	}
	
	/**
	 * There is a limit of 2000 characters that can be entered into the
	 * dialog text area. This keyboard class counts the number of entered
	 * characters, and automatically terminates the input process if the
	 * maximum limit is reached, and notifies the observer. This handler
	 * utilizes the KeyAdapter class and only implements the keyTyped() method.
	 * 
	 * @author dsklett
	 *
	 */
	private final class KeyHandler extends KeyAdapter {
		
		private int		textLimit = 2000;
		private int		textCount = 0;
		
		public void keyTyped(KeyEvent event) {
			if (++textCount > textLimit) {
				observable.update();
				observable.notifyObservers(text.getText().trim());
				dispose();
			}
		}
	}
}
