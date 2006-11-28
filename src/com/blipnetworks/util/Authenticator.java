/*
 * @(#)Authenticator.java
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

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.PostMethod;

import java.io.IOException;

import com.blipnetworks.upperblip.Main;

/**
 *
 *
 * @author Jared Klett
 * @version $Id: Authenticator.java,v 1.5 2006/11/28 20:53:54 jklett Exp $
 */

public class Authenticator {

// CVS info ///////////////////////////////////////////////////////////////////

    public static final String CVS_REV = "$Revision: 1.5 $";

// Class variables ////////////////////////////////////////////////////////////

    public static final String PROPERTY_LOGIN_URI = "login.uri";

    public static Cookie authCookie;

// Constructors ///////////////////////////////////////////////////////////////

    private Authenticator() {
        // will never be called
    }

// Class methods //////////////////////////////////////////////////////////////

    /**
     *
     * @param username
     * @param password
     * @return True we got an auth cookie, false otherwise.
     * @throws IllegalStateException If the server returns an error code.
     */
    public static boolean authenticate(String username, String password) {
        if (username == null || password == null)
            throw new IllegalArgumentException("Neither username nor password can be null");
        boolean okay = false;
        String url = Main.appProperties.getProperty(Main.PROPERTY_BASE_URL);
        String uri = Main.appProperties.getProperty(PROPERTY_LOGIN_URI);
        PostMethod post = new PostMethod(url + uri);
        NameValuePair[] nvp = {
                new NameValuePair(Uploader.USER_PARAM_KEY, username),
                new NameValuePair(Uploader.PASS_PARAM_KEY, password),
                new NameValuePair(Uploader.SKIN_PARAM_KEY, Uploader.SKIN_PARAM_DEF)
        };
        post.setRequestBody(nvp);
        try {
            HttpClient client = new HttpClient();
            // Set a tolerant cookie policy
            client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            // Set our timeout
            client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
            // If we had an auth cookie previously, set it in the client before
            // we send the request
            // Send the post request
            int responseCode = client.executeMethod(post);
            // Check for an authorization cookie in the response
            Cookie[] cookies = client.getState().getCookies();
            Cookie myCookie = null;
            for (int i = 0; i < cookies.length; i++) {
                if (cookies[i].getName().equals(Uploader.AUTH_COOKIE_NAME)) {
                    myCookie = cookies[i];
                    break;
                }
            }
            // Check the HTTP response code
            boolean succeeded = responseCode < 400;
            if (succeeded && myCookie != null) {
                authCookie = myCookie;
                okay = true;
            } else {
                // TODO: error message here
                throw new IllegalStateException("Received bad response from the server, HTTP response code " + responseCode);
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
        return okay;
    }

} // class Authenticator
