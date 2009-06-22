package com.blipnetworks.upperblip;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.event.ConnectionAdapter;
import javax.mail.event.ConnectionEvent;
import javax.mail.event.TransportAdapter;
import javax.mail.event.TransportEvent;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.swing.JOptionPane;

import com.sun.mail.smtp.SMTPMessage;

/**
 * This class makes use of the JavaMail set of classes to report unexpected exceptions
 * to blip.tv via e-mail. This implements a send-only system.
 * 
 * @author dsklett
 * @version $Id: ExceptionReporter.java,v 1.2 2009/06/22 21:07:45 jklett Exp $
 */
public class ExceptionReporter implements Runnable {
	
	private String				fault = null;
	private StackTraceElement[]	elements = null;
	
	public ExceptionReporter(String fault, StackTraceElement[] elements) {
		this.fault = fault;
		this.elements = elements;		
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

		//private MailAuthentication	ma = null;
		private Properties			props = null;
		private Session				session = null;
		private Transport			trans = null;
		private Address[]			destination = new InternetAddress[1];
		
		public ProblemMailer(Properties props) {
			this.props = props;
			//ma = new MailAuthentication(props.getProperty("mail.user"), props.getProperty("mail.password"));
		}
		
		public void setUpTransport() throws AddressException, NoSuchProviderException, MessagingException {
			//session = Session.getInstance(props, ma);
			session = Session.getInstance(props);
			destination[0] = new InternetAddress(props.getProperty("mail.to"));
			trans = session.getTransport(destination[0]);
			
			trans.addConnectionListener(new ConnectionHandler()); 
			trans.addTransportListener(new TransportHandler());
			
			trans.connect();
		}
		
		public void sendMailMessage() throws MessagingException {
			Message	smtpMessage = new SMTPMessage(session);
			StringBuilder	builder = new StringBuilder();
			
			builder.append("Exception: ");
			builder.append(fault);
			builder.append("\n");
			smtpMessage.setSubject("UpperBlip Exception Report");
			for (StackTraceElement element : elements) {
				builder.append(element.toString());
				builder.append("\n");
			}
			smtpMessage.setText(builder.toString());
			trans.sendMessage(smtpMessage, destination);
			trans.close();
		}
		
		private class TransportHandler extends TransportAdapter {
			
			public void messageNotDelivered(TransportEvent event) {
				JOptionPane.showMessageDialog(null, "Unable to deliver error e-mail");
			}
		}
		
		private class ConnectionHandler extends ConnectionAdapter {
			
			public void disconnected(ConnectionEvent event) {
				JOptionPane.showMessageDialog(null, "E-mail connection disconnected");
			}
		}
	}

}
