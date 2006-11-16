/* 
 * @(#)Uploader.java
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

import java.io.*;
import java.util.*;
import java.net.URL;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.cookie.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.methods.multipart.*;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.blipnetworks.upperblip.Main;

/**
 * A stateful class to handle uploads to Blip.
 * TODO: use a logging interface for stack traces and println's.
 *
 * @author Jared Klett
 * @version $Id: Uploader.java,v 1.18 2006/11/16 17:52:32 jklett Exp $
 */

public class Uploader {

// CVS info ///////////////////////////////////////////////////////////////////

    public static final String CVS_REV = "$Revision: 1.18 $";

// Constants //////////////////////////////////////////////////////////////////

    /** The name of the cookie that contains the authcode. */
    public static final String AUTH_COOKIE_NAME = "otter_auth";

    /** The hash key to the file parameter. */
    public static final String FILE_PARAM_KEY = "file";
    /** The hash key to the file parameter. */
    public static final String THUMB_PARAM_KEY = "thumbnail";
    /** The hash key to the title parameter. */
    public static final String TITLE_PARAM_KEY = "title";
    /** The hash key to the post ID parameter. */
    public static final String POST_PARAM_KEY = "post";
    /** The hash key to the license parameter. */
    public static final String LICENSE_PARAM_KEY = "license";
    /** The hash key to the category ID parameter. */
    public static final String CAT_PARAM_KEY = "categories_id";
    /** The hash key to the topics parameter. */
    public static final String TAGS_PARAM_KEY = "topics";
    /** The hash key to the username parameter. */
    public static final String USER_PARAM_KEY = "userlogin";
    /** The hash key to the password parameter. */
    public static final String PASS_PARAM_KEY = "password";
    /** The hash key to the password parameter. */
    public static final String WEAK_PASS_PARAM_KEY = "lowpassword";
    /** The hash key to the skin parameter. */
    public static final String SKIN_PARAM_KEY = "skin";
    /** The hash key to the description parameter. */
    public static final String DESC_PARAM_KEY = "description";
    /** The hash key to the form cookie GUID parameter. */
    public static final String GUID_PARAM_KEY = "form_cookie";
    /** TODO. */
    public static final String INGEST_PARAM_KEY = "ingest_method";
    /** TODO */
    public static final String CROSSPOST_PARAM_KEY = "crosspost";
    /** TODO */
    public static final String IA_PARAM_KEY = "crossupload_archiveorg";

    /** Default: the title of the post, if none is supplied. */
    public static final String TITLE_PARAM_DEF = "Working title";
    /** Default: the post ID of the post, if none is supplied. */
    public static final String POST_PARAM_DEF = "1";
    /** Default: the license ID of the post, if none is supplied. */
    public static final String LICENSE_PARAM_DEF = "-1";
    /** Default: the topics for the post, if none is supplied. */
    public static final String TAGS_PARAM_DEF = "";
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
    /** Default: TODO. */
    public static final String INGEST_PARAM_DEF = "upperblip";

    public static final int ERROR_UNKNOWN = 0;
    public static final int ERROR_BAD_AUTH = 1;

    private Cookie authCookie;
    private String url;
    private String urlWithGuid;
    private String postURL;
    private int timeout;
    private int errorCode;

// Constructor ////////////////////////////////////////////////////////////////

    public Uploader(String url) {
        this(url, 30000);
    }

    public Uploader(String url, int timeout) {
        this.url = url;
        this.timeout = timeout;
    }

// Instance methods ///////////////////////////////////////////////////////////

    public boolean uploadFile(File file, Properties parameters) {
        return uploadFile(file, null, parameters);
    }

    /**
     *
     */
    public boolean uploadFile(File videoFile, File thumbnailFile, Properties parameters) {
        return uploadFile(videoFile, thumbnailFile, parameters, null);
    }

    public boolean uploadFile(File videoFile, File thumbnailFile, Properties parameters, List crossposts) {
        PostMethod post = new PostMethod(urlWithGuid);
        FilePart videoFilePart;
        try {
            videoFilePart = new FilePart(FILE_PARAM_KEY, videoFile);
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
            return false;
        }

        FilePart thumbnailFilePart = null;
        if (thumbnailFile != null) {
            try {
                thumbnailFilePart = new FilePart(THUMB_PARAM_KEY, thumbnailFile);
            } catch (FileNotFoundException fnfe) {
                fnfe.printStackTrace();
                return false;
            }
        }

        Part[] parts;
        Part[] typeArray = new Part[0];
        List list = new ArrayList();
        list.add(videoFilePart);
        if (thumbnailFilePart != null)
            list.add(thumbnailFilePart);
        list.add(new StringPart(TITLE_PARAM_KEY, parameters.getProperty(TITLE_PARAM_KEY, TITLE_PARAM_DEF)));
        list.add(new StringPart(POST_PARAM_KEY, parameters.getProperty(POST_PARAM_KEY, POST_PARAM_DEF)));
        list.add(new StringPart(CAT_PARAM_KEY, parameters.getProperty(CAT_PARAM_KEY, CAT_PARAM_DEF)));
        list.add(new StringPart(TAGS_PARAM_KEY, parameters.getProperty(TAGS_PARAM_KEY, TAGS_PARAM_DEF)));
        list.add(new StringPart(LICENSE_PARAM_KEY, parameters.getProperty(LICENSE_PARAM_KEY, LICENSE_PARAM_DEF)));
        list.add(new StringPart(SKIN_PARAM_KEY, parameters.getProperty(SKIN_PARAM_KEY, SKIN_PARAM_DEF)));
        list.add(new StringPart(DESC_PARAM_KEY, parameters.getProperty(DESC_PARAM_KEY, DESC_PARAM_DEF)));
        list.add(new StringPart(INGEST_PARAM_KEY, parameters.getProperty(INGEST_PARAM_KEY, INGEST_PARAM_DEF)));
        if (crossposts != null) {
            for (int i = 0; i < crossposts.size(); i++)
                list.add(new StringPart(CROSSPOST_PARAM_KEY, (String)crossposts.get(i)));
        }
        // We want to omit the un/pw parts if we have an auth cookie
        if (authCookie == null) {
            list.add(new StringPart(USER_PARAM_KEY, parameters.getProperty(USER_PARAM_KEY, USER_PARAM_DEF)));
            list.add(new StringPart(PASS_PARAM_KEY, parameters.getProperty(PASS_PARAM_KEY, PASS_PARAM_DEF)));
        }
        parts = (Part[])list.toArray(typeArray);

        post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));

        boolean succeeded = false;

        try {
            HttpClient client = new HttpClient();
            // Set a tolerant cookie policy
            client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            client.getParams().setParameter(HttpMethodParams.USER_AGENT, Main.UA);
            // Set our timeout
            client.getHttpConnectionManager().getParams().setConnectionTimeout(timeout);
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
            DocumentBuilder docBuilder;
            Document document = null;
            try {
                docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                document = docBuilder.parse(responseStream);
            } catch (ParserConfigurationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (SAXException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            // agree on a proper schema for responses from Blip.
            if (responseCode == HttpStatus.SC_OK) {
                if (document != null) {
                    // attempt to discern the status from the respose
                    String responseText = document.getElementsByTagName("response").item(0).getTextContent();
                    String[] lines = responseText.trim().split("\n");
                    if (lines.length >= 2) {
                        try {
                            URL testURL = new URL(lines[1]);
                            postURL = testURL.toString();
                        } catch (Exception e) {
                            // TODO: log message
                            System.out.println("Couldn't find valid URL in response text:\n" + responseText);
                        }
                    }
                    if (responseText.indexOf("couldn't find an account") != -1)
                        errorCode = ERROR_BAD_AUTH;
                    return responseText.indexOf("has been successfully posted") != -1;
/*
                    "username and password combination";
                    "You must";
                    "critical error";
*/
                }
            } else {
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

// Accessors //////////////////////////////////////////////////////////////////

    public int getErrorCode() {
        return errorCode;
    }

    public String getPostURL() {
        return postURL;
    }

// Mutators ///////////////////////////////////////////////////////////////////

    public void setGuid(String guid) {
        urlWithGuid = url + guid;
    }

    public void setAuthCookie(Cookie authCookie) {
        this.authCookie = authCookie;
    }

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
