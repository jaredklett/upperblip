package com.blipnetworks.upperblip;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import com.sun.mail.smtp.SMTPMessage;

/**
 * This class makes use of the JavaMail set of classes to report unexpected exceptions
 * to blip.tv via e-mail. This implements a send-only system.
 * 
 * @author dsklett
 * @version $Id: ExceptionReporter.java,v 1.3 2011/01/27 19:38:53 jklett Exp $
 */
public final class ExceptionReporter implements Runnable, Observer {
	
	private Thread								exceptionThread = null;
	private Exception							exception = null;
	private String								userName = null;
	private int									buildNumber = 0;
	private Map<Thread, StackTraceElement[]>	traces = null;
	private String								text = null;
	
	public ExceptionReporter(String userName, int buildNumber, Thread thread, Throwable exp, Map<Thread, StackTraceElement[]> traces) {
		this.userName = userName;
		this.buildNumber = buildNumber;
		exceptionThread = thread;
		this.exception = (Exception) exp;
		this.traces = traces;
	}
	
	public void update(Observable obs, Object obj) {
		text = (String) obj;
	}
	
	public void run() {
		Properties		props = new Properties();
		
		InputStream	in = ProblemMailer.class.getClassLoader().getResourceAsStream("mail_properties");
		if (in != null) {
			try {
				props.load(in);
				in.close();
			} catch (IOException e) {
				return;
			}
		}
		else {
			return;
		}

		ProblemMailer	mailer = new ProblemMailer(props);

		try {
			mailer.setUpTransport();
		} catch (AddressException e) {
			return;
		} catch (NoSuchProviderException e) {
			return;
		} catch (MessagingException e) {
			return;
		}
		try {
			mailer.sendMailMessage();
		} catch (MessagingException e) {
			return;
		}
	}
	
	public class ProblemMailer {
		private Properties			props = null;
		private Session				session = null;
		private Transport			trans = null;
		private Address[]			destination = new InternetAddress[1];
		
		public ProblemMailer(Properties props) {
			this.props = props;
		}
		
		public void setUpTransport() throws AddressException, NoSuchProviderException, MessagingException {
			session = Session.getInstance(props);
			destination[0] = new InternetAddress(props.getProperty("mail.to"));
			trans = session.getTransport(destination[0]);
			trans.connect();
		}
		
		public void sendMailMessage() throws MessagingException {
			Message	smtpMessage = new SMTPMessage(session);
			StringBuilder	builder = new StringBuilder();
			
			builder.append("Build Number: " + String.valueOf(buildNumber) + "\n");
			builder.append("Java Version: " + System.getProperty("java.version") + "\n");
			builder.append("[OS: " + System.getProperty("os.name") + "]  [Version: " + System.getProperty("os.version") + "]\n");
			builder.append("Blip User ID: " + userName + "\n\n");
			
			if (text.equals("")) {
				builder.append("No message entered by customer.\n\n");
			}
			else {
				addCustomerMessage(builder);
			}
			
			builder.append("Exception: ");
			builder.append(exception.toString() + "\n");
			addThreadInformation(builder, exceptionThread);
			
			smtpMessage.setSubject("UpperBlip Exception Report");
			
			for (StackTraceElement element : exceptionThread.getStackTrace()) {
				builder.append("\t");
				builder.append(element.toString());
				builder.append("\n");
			}
			builder.append("\n");
			addRemainingThreads(builder);
			
			smtpMessage.setText(builder.toString());
			trans.sendMessage(smtpMessage, destination);
			trans.close();
		}
		
		private void addCustomerMessage(StringBuilder builder) {
			int		scanLimit = 200;
			int		totalLength = 2000;
			int		start = 0;
			int		i = 0;
			String	sub = null;
			
			if (text.length() > totalLength) {
				text = text.substring(0, totalLength);
			}
			
			builder.append("Customer comments:\n");
			while (start < text.length()) {
				i = text.indexOf(" ", start + scanLimit);
				if (i < 0) {
					sub = text.substring(start);
					if (!sub.equals(" ") && !sub.equals("\n")) {
						builder.append(sub);
					}
					builder.append("\n\n");
					return;
				}
				else {
					sub = text.substring(start, i);
					if (!sub.equals(" ") && !sub.equals("\n")) {
						builder.append(sub);
					}
				}
				builder.append("\n");
				start = i;
			}
		}
		
		private void addRemainingThreads(StringBuilder builder) {
			
			for (Iterator<Thread> cursor = traces.keySet().iterator(); cursor.hasNext(); ) {
				Thread	thread = cursor.next();
				if (thread.getId() == exceptionThread.getId()) {
					continue;
				}
				addThreadInformation(builder, thread);
				StackTraceElement[]	elements = traces.get(thread);
				for (StackTraceElement element : elements) {
					builder.append("\t");
					builder.append(element.toString());
					builder.append("\n");
				}
				builder.append("\n");
			}
		}
		
		private void addThreadInformation(StringBuilder builder, Thread thread) {
			builder.append("[Thread ID: ");
			builder.append(Long.toString(thread.getId()));
			builder.append("]  [Name: ");
			builder.append(thread.getName());
			builder.append("]\n");
		}
	}

}
