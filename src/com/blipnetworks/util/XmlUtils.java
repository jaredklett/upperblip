/* 
 * @(#)XmlUtils.java
 * 
 * Copyright (c) 2006 by Blip Networks, Inc.
 * 239 Centre St, 3rd Floor
 * New York, NY 10013
 * All rights reserved.
 *
 * This software is the confidential and
 * proprietary information of Blip Networks, Inc.
 */

package com.blipnetworks.util;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;

import com.blipnetworks.upperblip.Main;

/**
 * This is a placeholder description of this class.
 *
 * @author Jared Klett
 * @version $Id: XmlUtils.java,v 1.5 2006/10/19 18:12:17 jklett Exp $
 */

public class XmlUtils {

// CVS info ///////////////////////////////////////////////////////////////////

    public static final String CVS_REV = "$Revision: 1.5 $";

// Constants //////////////////////////////////////////////////////////////////

    public static final int TIMEOUT = 30000;
    public static final String XML_TYPE = "xml";
    public static final String UTF8_TYPE = "UTF-8";
    public static final String NEWLINE = "\n";

// Class methods //////////////////////////////////////////////////////////////

    /**
     *
     * @param url
     * @return
     * @throws IOException
     */
    public static Document loadDocumentFromURL(String url) throws IOException, ParserConfigurationException, SAXException {
        return loadDocumentFromURL(url, null);
    }

    /**
     *
     * @param url
     * @param authCookie
     * @return
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public static Document loadDocumentFromURL(String url, Cookie authCookie) throws IOException, ParserConfigurationException, SAXException {
        Document document = null;
        GetMethod method = new GetMethod(url);
        try {
            HttpClient client = new HttpClient();
            // Set a tolerant cookie policy
            client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            client.getParams().setParameter(HttpMethodParams.USER_AGENT, Main.UA);
            // Set our timeout
            client.getHttpConnectionManager().getParams().setConnectionTimeout(TIMEOUT);
            if (authCookie != null)
                client.getState().addCookie(authCookie);
            int responseCode = client.executeMethod(method);
            if (responseCode == HttpStatus.SC_OK) {
                // Read the response
                InputStream responseStream = method.getResponseBodyAsStream();
                DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                document = docBuilder.parse(responseStream);
            }
        }
        finally {
            method.releaseConnection();
        }
        return document;
    }


    /**
     * Parses an XML string into at DOM Document.
     *
     * @param xml The (presumably XML) string that's to be turned into a DOM document.
     * @return A W3C DOM document that holds the contents of the given XML.
     */
    public static Document makeDocumentFromString(String xml) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xml)));
    }

    /**
     * Parses an (XML) File into a DOM document.
     *
     * @param file The (presumably XML) file that's to be turned into a DOM document.
     * @return a W3C DOM document that holds the contents of the given File
     */
    public static Document makeDocumentFromFile(File file) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        return builder.parse(file);
    }

    /**
     * Serializes the given W3C DOM document into an XML string.
     *
     * @param doc The W3C DOM document to be serialized.
     * @return A string representation of the DOM document's data in XML.
     */
    public static String makeStringFromDocument(Document doc) throws IOException {
        StringWriter writer = new StringWriter();
        XMLSerializer output = new XMLSerializer(writer, new OutputFormat(XML_TYPE, UTF8_TYPE, true));

        output.serialize(doc);

        return writer.toString();
    }

    /**
     * Serializes the given W3C DOM document into a File.
     *
     * @param doc The W3C DOM document to be serialized.
     * @return A string representation of the DOM document's data in XML.
     */
    public static File makeFileFromDocument(Document doc) throws IOException {
        OutputFormat format = new OutputFormat(doc, UTF8_TYPE, true);

        format.setIndenting(false);
        format.setLineWidth(0);
        format.setLineSeparator(NEWLINE);

        // TODO: redo this
        File file = File.createTempFile("tmp", ".xml");
        file.deleteOnExit();

        XMLSerializer serializer = new XMLSerializer(new FileOutputStream(file.getAbsolutePath()), format);
        serializer.asDOMSerializer();
        serializer.serialize(doc);

        return file;
    }

} // class XmlUtils

