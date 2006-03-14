/*
 * @(#)BuildNumber.java
 *
 * Copyright (c) 2006 by Pokkari, Inc.
 * 117 West 25th St.
 * New York, NY 10001
 * All rights reserved.
 *
 * This software is the confidential and
 * proprietary information of Pokkari, Inc.
 */

package com.pokkari.util;

import java.util.Properties;
import java.io.IOException;

/**
 * foobar
 */

public class BuildNumber {

    /** An internal thread to keep an eye on the web server object. */
    private static int buildNumber;
    /** An internal thread to keep an eye on the web server object. */
    private static String cvsTag;

    static {
        Properties buildProps = new Properties();
        try {
            // TODO: break all this out into constants
            buildProps.load(ClassLoader.getSystemResourceAsStream("build.number"));
            buildNumber = Integer.parseInt(buildProps.getProperty("build.number", "-1"));
            cvsTag = buildProps.getProperty("cvs.tag", "unknown");
        } catch (IOException e) {
            buildNumber = 0;
            cvsTag = "Unknown";
        }
    }

    public static int getBuildNumber() {
        return buildNumber;
    }

    public static String getCvsTag() {
        return cvsTag;
    }

} // class BuildNumber
