/* 
 * @(#)Uploader.java
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

// Someday we'll parse XML responses...
//import org.xml.sax.*;
//import org.w3c.dom.*;

/**
 * A stateful class to handle uploads to Blip.
 * TODO: use a logging interface for stack traces and println's.
 * 
 * @author Jared Klett
 * @version $Id: Uploader.java,v 1.1 2005/11/03 18:57:23 jklett Exp $
 */

public class Uploader {

// CVS info ///////////////////////////////////////////////////////////////////

	public static final String CVS_REV = "$Revision: 1.1 $";

// Constants //////////////////////////////////////////////////////////////////

	/** The name of the cookie that contains the authcode. */
	public static final String AUTH_COOKIE_NAME = "otter_auth";

	/** The hash key to the file parameter. */
	public static final String FILE_PARAM_KEY = "file";
	/** The hash key to the title parameter. */
	public static final String TITLE_PARAM_KEY = "title";
	/** The hash key to the post ID parameter. */
	public static final String POST_PARAM_KEY = "post";
	/** The hash key to the category ID parameter. */
	public static final String CAT_PARAM_KEY = "categories_id";
	/** The hash key to the username parameter. */
	public static final String USER_PARAM_KEY = "userlogin";
	/** The hash key to the password parameter. */
	public static final String PASS_PARAM_KEY = "lowpassword";
	/** The hash key to the skin parameter. */
	public static final String SKIN_PARAM_KEY = "skin";
	/** The hash key to the description parameter. */
	public static final String DESC_PARAM_KEY = "description";
	/** The hash key to the form cookie GUID parameter. */
	public static final String GUID_PARAM_KEY = "form_cookie";

	/** Default: the title of the post, if none is supplied. */
	public static final String TITLE_PARAM_DEF = "Working title";
	/** Default: the post ID of the post, if none is supplied. */
	public static final String POST_PARAM_DEF = "1";
	/** Default: the category ID of the post, if none is supplied. */
	public static final String CAT_PARAM_DEF = "-1";
	/** Default: the user login - this should be supplied. */
	public static final String USER_PARAM_DEF = "nobody";
	/** Default: the password - this should be supplied. */
	public static final String PASS_PARAM_DEF = "nopass";
	/** Default: the skin for the response, if none is supplied. */
	public static final String SKIN_PARAM_DEF = "xmlhttprequest";
	/** Default: the description of the post - this should be supplied. */
	public static final String DESC_PARAM_DEF = "Working description.";

	private Cookie authCookie;
	private String url;
	private int error;

// Constructor ////////////////////////////////////////////////////////////////

	public Uploader(String url) {
		this.url = url;
	}

// Instance methods ///////////////////////////////////////////////////////////

	/**
	 *
	 */
	public boolean uploadFile(File file, Properties parameters) {

		PostMethod post = new PostMethod(url);
		FilePart fp = null;
		try {
			fp = new FilePart(FILE_PARAM_KEY, file);
		}
		catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
			return false;
		}

		Part[] parts = null;
		// We want to omit the un/pw parts if we have an auth cookie
		if (authCookie == null)
			parts = new Part[] {
				fp, 
				new StringPart(TITLE_PARAM_KEY, parameters.getProperty(TITLE_PARAM_KEY, TITLE_PARAM_DEF)),
				new StringPart(POST_PARAM_KEY, parameters.getProperty(POST_PARAM_KEY, POST_PARAM_DEF)),
				new StringPart(CAT_PARAM_KEY, parameters.getProperty(CAT_PARAM_KEY, CAT_PARAM_DEF)),
				new StringPart(USER_PARAM_KEY, parameters.getProperty(USER_PARAM_KEY, USER_PARAM_DEF)),
				new StringPart(PASS_PARAM_KEY, parameters.getProperty(PASS_PARAM_KEY, PASS_PARAM_DEF)),
				new StringPart(SKIN_PARAM_KEY, parameters.getProperty(SKIN_PARAM_KEY, SKIN_PARAM_DEF)),
				new StringPart(DESC_PARAM_KEY, parameters.getProperty(DESC_PARAM_KEY, DESC_PARAM_DEF))
			};
		else
			parts = new Part[] {
				fp, 
				new StringPart(TITLE_PARAM_KEY, parameters.getProperty(TITLE_PARAM_KEY, TITLE_PARAM_DEF)),
				new StringPart(POST_PARAM_KEY, parameters.getProperty(POST_PARAM_KEY, POST_PARAM_DEF)),
				new StringPart(CAT_PARAM_KEY, parameters.getProperty(CAT_PARAM_KEY, CAT_PARAM_DEF)),
				new StringPart(SKIN_PARAM_KEY, parameters.getProperty(SKIN_PARAM_KEY, SKIN_PARAM_DEF)),
				new StringPart(DESC_PARAM_KEY, parameters.getProperty(DESC_PARAM_KEY, DESC_PARAM_DEF))
			};

		post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));

		boolean succeeded = false;

		try {
			HttpClient client = new HttpClient();
			// Set a tolerant cookie policy
			client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
			// Set our timeout
			client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
			// If we had an auth cookie previously, set it in the client before
			// we send the request
			if (authCookie != null)
				client.getState().addCookie(authCookie);
			// Send the post request
			int responseCode = client.executeMethod(post);
			// Check for an authorization cookie in the response
			if (authCookie == null) {
				Cookie[] cookies = client.getState().getCookies();
				for (int i = 0; i < cookies.length; i++) {
					if (cookies[i].getName().equals(AUTH_COOKIE_NAME)) {
						authCookie = cookies[i];
						break;
					}
				}
			}

			// Check the HTTP response code
			succeeded = responseCode < 400;
			// Read the response
			InputStream responseStream = post.getResponseBodyAsStream();
			String responsePage = null;
			StringBuffer buffer = new StringBuffer(responseStream.available());
			BufferedReader in = new BufferedReader(new InputStreamReader(responseStream));
			String line = in.readLine();
			while (line != null) {
				buffer.append(line).append("\n");
				line = in.readLine();
			}
			responsePage = buffer.toString();
			//System.out.println(responsePage);

			// TODO: we should be parsing the XML here - we need to wait until we
			// agree on a proper schema for responses from Blip.
			if (responseCode == HttpStatus.SC_OK) {
				// But instead we'll have to rely on arbitrary strings
				// FIXME: !!!
				if (responsePage.indexOf("You must") != -1 || responsePage.indexOf("critical error") != -1) {
					succeeded = false;
				}
			} else {
				System.out.println("File upload failed: " + HttpStatus.getStatusText(responseCode));
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
			post.releaseConnection();
		}
		return succeeded;
	} // method uploadFile

// Main method ////////////////////////////////////////////////////////////////

	public static void main(String[] args) {
		if (args.length < 4) {
			System.out.println("Usage: java Uploader <url> <file> <user> <pass>");
			System.out.println("Optional parameters: <title> <desc>");
			return;
		}
		File f = new File(args[1]);
		Properties p = new Properties();
		p.put(Uploader.USER_PARAM_KEY, args[2]);
		p.put(Uploader.PASS_PARAM_KEY, args[3]);
		if (args.length > 4) {
			p.put(Uploader.TITLE_PARAM_KEY, args[4]);
			p.put(Uploader.DESC_PARAM_KEY, args[5]);
		}

		Uploader uploader = new Uploader(args[0]);
		uploader.uploadFile(f, p);
	}

} // class Uploader
