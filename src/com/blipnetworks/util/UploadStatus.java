/* 
 * @(#)UploadStatus.java
 * 
 * Copyright (c) 2006 by Blip Networks, Inc.
 * 117 West 25th St, Floor 2
 * New York, NY 10001
 * All rights reserved.
 *
 * This software is the confidential and
 * proprietary information of Blip Networks, Inc.
 */

package com.blipnetworks.blip.util;

import java.io.*;

import javax.xml.parsers.*;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.cookie.*;
import org.apache.commons.httpclient.methods.*;

import org.xml.sax.*;
import org.w3c.dom.*;

/**
 * This class knows how to ask Otter about the status of an upload, and nothing more.
 * It's immutable and should stay that way.
 *
 * @author Jared Klett
 * @version $Id: UploadStatus.java,v 1.6 2006/05/06 23:56:46 jklett Exp $
 */

public class UploadStatus {

// CVS info ///////////////////////////////////////////////////////////////////

	public static final String CVS_REV = "$Revision: 1.6 $";

// Constants //////////////////////////////////////////////////////////////////

    private static final int TIMEOUT = 30000;
    private static final String GUID_TAG = "guid";
    private static final String FILENAME_TAG = "filename";
    private static final String START_TAG = "start";
    private static final String UPDATE_TAG = "update";
    private static final String READ_TAG = "read";
    private static final String TOTAL_TAG = "total";

// Instance variables /////////////////////////////////////////////////////////

    private String guid;
    private String filename;
    private long start;
    private long update;
    private int read;
    private int total;

// Constructor ////////////////////////////////////////////////////////////////

	private UploadStatus() {
        // will never be called outside
    }

// Class methods //////////////////////////////////////////////////////////////

    /**
     * Hits the URL and attempts to read XML back from the response.
     * @param url The URL to hit to load status information.
     * @param guid The GUID for the upload.
     * @return A new object containing the status data.
     */
    public static UploadStatus getStatus(String url, String guid) {

		GetMethod method = new GetMethod(url + guid);
        UploadStatus status = null;
        try {
			HttpClient client = new HttpClient();
			// Set a tolerant cookie policy
			client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
			// Set our timeout
            client.getHttpConnectionManager().getParams().setConnectionTimeout(TIMEOUT);
			int responseCode = client.executeMethod(method);
            if (responseCode != HttpStatus.SC_OK) {
                // TODO: problem! what to do?
                return null;
            }
			// Read the response
            InputStream responseStream = method.getResponseBodyAsStream();
            Document document = null;
            try {
                DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                document = docBuilder.parse(responseStream);
            } catch (ParserConfigurationException e) {
                // TODO: log this?
                e.printStackTrace();
            } catch (SAXException e) {
                // TODO: log this?
                e.printStackTrace();
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
            // TODO: log this?
			e.printStackTrace();
		}
		catch (IOException e) {
            // TODO: log this?
			e.printStackTrace();
		}
		finally {
			method.releaseConnection();
		}
		return status;
	}

// Instance methods ///////////////////////////////////////////////////////////

    /**
     * Retrieves the GUID from the XML response.
     * @return The GUID parsed out of the XML.
     */
    public String getGuid() {
        return guid;
    }

    /**
     * Sets the GUID value in this object.
     * @param guid The new GUID value.
     */
    private void setGuid(String guid) {
        this.guid = guid;
    }

    /**
     * Retrieves the file name from the XML response.
     * @return The file name parsed out of the XML.
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Sets the GUID value in this object.
     * @param filename The new GUID value.
     */
    private void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * Retrieves the start time of the upload from the XML response.
     * @return The start time of the upload parsed out of the XML.
     */
    public long getStart() {
        return start;
    }

    /**
     * Sets the start time of the upload in this object.
     * @param start The new start time of the upload.
     */
    private void setStart(long start) {
        this.start = start;
    }

    /**
     * Retrieves the last update time from the XML response.
     * @return The last update time parsed out of the XML.
     */
    public long getUpdate() {
        return update;
    }

    /**
     * Sets the last update time in this object.
     * @param update The new last update time.
     */
    private void setUpdate(long update) {
        this.update = update;
    }

    /**
     * Retrieves the number of bytes read so far from the XML response.
     * @return The number of bytes read so far parsed out of the XML.
     */
    public int getRead() {
        return read;
    }

    /**
     * Sets the number of bytes read so far in this object.
     * @param read The number of bytes read so far parsed out of the XML.
     */
    private void setRead(int read) {
        this.read = read;
    }

    /**
     * Retrieves the total number of bytes from the XML response.
     * @return The total number of bytes parsed out of the XML.
     */
    public int getTotal() {
        return total;
    }

    /**
     * Sets the total number of bytes in this object.
     * @param total The total number of bytes.
     */
    private void setTotal(int total) {
        this.total = total;
    }

} // class UploadStatus
