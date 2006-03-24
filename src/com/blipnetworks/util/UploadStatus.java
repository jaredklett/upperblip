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
 *
 * @author Jared Klett
 * @version $Id: UploadStatus.java,v 1.3 2006/03/24 22:00:29 jklett Exp $
 */

public class UploadStatus {

// CVS info ///////////////////////////////////////////////////////////////////

	public static final String CVS_REV = "$Revision: 1.3 $";

// Constants //////////////////////////////////////////////////////////////////

// Instance variables /////////////////////////////////////////////////////////

    private String guid;
    private String filename;
    private long start;
    private long update;
    private int read;
    private int total;
    private static final String GUID_TAG = "guid";
    private static final String FILENAME_TAG = "filename";
    private static final String START_TAG = "start";
    private static final String UPDATE_TAG = "update";
    private static final String READ_TAG = "read";
    private static final String TOTAL_TAG = "total";

// Constructor ////////////////////////////////////////////////////////////////

	private UploadStatus() {
        // will never be called outside
    }

// Class methods //////////////////////////////////////////////////////////////

    /**
	 *
	 */
	public static UploadStatus getStatus(String url, String guid) {

		GetMethod method = new GetMethod(url + guid);
        UploadStatus status = null;
        try {
			HttpClient client = new HttpClient();
			// Set a tolerant cookie policy
			client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
			// Set our timeout
            // TODO: externalize
            client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
			int responseCode = client.executeMethod(method);
            if (responseCode != HttpStatus.SC_OK) {
                // TODO: problem!
                return null;
            }
			// Read the response
            InputStream responseStream = method.getResponseBodyAsStream();
            Document document = null;
            try {
                DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                document = docBuilder.parse(responseStream);
            } catch (ParserConfigurationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (SAXException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            if (document != null) {
                status = new UploadStatus();
                status.setGuid(document.getElementsByTagName(GUID_TAG).item(0).getTextContent());
                status.setFilename(document.getElementsByTagName(FILENAME_TAG).item(0).getTextContent());
                status.setStart(Integer.parseInt(document.getElementsByTagName(START_TAG).item(0).getTextContent()));
                status.setUpdate(Integer.parseInt(document.getElementsByTagName(UPDATE_TAG).item(0).getTextContent()));
                status.setRead(Integer.parseInt(document.getElementsByTagName(READ_TAG).item(0).getTextContent()));
                status.setTotal(Integer.parseInt(document.getElementsByTagName(TOTAL_TAG).item(0).getTextContent()));
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
		return status;
	}

// Instance methods ///////////////////////////////////////////////////////////

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getUpdate() {
        return update;
    }

    public void setUpdate(long update) {
        this.update = update;
    }

    public int getRead() {
        return read;
    }

    public void setRead(int read) {
        this.read = read;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

} // class UploadStatus
