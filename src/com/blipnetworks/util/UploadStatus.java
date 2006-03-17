/* 
 * @(#)UploadStatus.java
 * 
 * Copyright (c) 2005 by Pokkari, Inc.
 * 117 West 25th St, Floor 2
 * New York, NY 10001
 * All rights reserved.
 *
 * This software is the confidential and
 * proprietary information of Pokkari, Inc.
 */

package com.pokkari.blip.util;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.cookie.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.methods.multipart.*;

import org.xml.sax.*;
import org.w3c.dom.*;

/**
 * A stateful class to handle uploads to Blip.
 * TODO: use a logging interface for stack traces and println's.
 * 
 * @author Jared Klett
 * @version $Id: UploadStatus.java,v 1.2 2006/03/17 21:36:19 jklett Exp $
 */

public class UploadStatus {

// CVS info ///////////////////////////////////////////////////////////////////

	public static final String CVS_REV = "$Revision: 1.2 $";

// Constants //////////////////////////////////////////////////////////////////

	private String url;

// Constructor ////////////////////////////////////////////////////////////////

	public UploadStatus(String url) {
		this.url = url;
	}

// Instance methods ///////////////////////////////////////////////////////////

	public void setGuid(String guid) {
		url = url + guid;
	}

	/**
	 *
	 */
	public void check() {

		GetMethod method = new GetMethod(url);

		boolean succeeded = false;

		try {
			HttpClient client = new HttpClient();
			// Set a tolerant cookie policy
			client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
			// Set our timeout
			client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
			// If we had an auth cookie previously, set it in the client before
			// we send the request
			//if (authCookie != null)
				//client.getState().addCookie(authCookie);
			// Send the post request
			int responseCode = client.executeMethod(method);
			// Check for an authorization cookie in the response
/*
			if (authCookie == null) {
				Cookie[] cookies = client.getState().getCookies();
				for (int i = 0; i < cookies.length; i++) {
					if (cookies[i].getName().equals(AUTH_COOKIE_NAME)) {
						authCookie = cookies[i];
						break;
					}
				}
			}
*/
			// Check the HTTP response code
			succeeded = responseCode < 400;
			// Read the response
            //InputStream responseStream = method.getResponseBodyAsStream();
            DocumentBuilder docBuilder = null;
            Document document = null;
            String responsePage = method.getResponseBodyAsString();
            try {
                FileWriter writer = new FileWriter("C:\\response.xml");
                writer.write(responsePage);
                writer.flush();
                writer.close();
                InputStream responseStream = new FileInputStream("C:\\response.xml");
                docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                document = docBuilder.parse(responseStream);
                NodeList list = document.getElementsByTagName("foo");
                list.toString();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (SAXException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

			// TODO: we should be parsing the XML here - we need to wait until we
			// agree on a proper schema for responses from Blip.
			if (responseCode == HttpStatus.SC_OK) {
				// But instead we'll have to rely on arbitrary strings
				// FIXME: !!!
				if (responsePage.indexOf("You must") != -1 || responsePage.indexOf("critical error") != -1) {
					succeeded = false;
				}
			} else {
				System.out.println("Status request failed: " + HttpStatus.getStatusText(responseCode));
				succeeded = false;
			}
		}
		catch (HttpException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			method.releaseConnection();
		}
		//return succeeded;
	} // method uploadFile

// Main method ////////////////////////////////////////////////////////////////

	public static void main(String[] args) {
		
	}

} // class UploadStatus
