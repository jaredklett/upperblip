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

/**
 *
 *
 * @author Jared Klett
 * @version $Id: Authenticator.java,v 1.3 2006/10/20 17:26:46 jklett Exp $
 */

public class Authenticator {

// CVS info ///////////////////////////////////////////////////////////////////

    public static final String CVS_REV = "$Revision: 1.3 $";

// Class variables ////////////////////////////////////////////////////////////

    public static Cookie authCookie;

// Constructors ///////////////////////////////////////////////////////////////

    private Authenticator() {
        // will never be called
    }

// Class methods //////////////////////////////////////////////////////////////

    public static boolean authenticate(String username, String password) {
        boolean okay = false;
        // TODO: clean up these strings
        PostMethod post = new PostMethod("http://blip.tv/posts");
        NameValuePair[] nvp = {
                new NameValuePair("userlogin", username),
                new NameValuePair("password", password),
                new NameValuePair("skin", "xmlhttprequest")
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
                if (cookies[i].getName().equals("otter_auth")) {
                    myCookie = cookies[i];
                    break;
                }
            }
            // Check the HTTP response code
            boolean succeeded = responseCode < 400;
            if (succeeded && myCookie != null) {
                authCookie = myCookie;
                okay = true;
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
