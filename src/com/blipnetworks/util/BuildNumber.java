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
import java.net.URL;

/**
 * A class to encapsulate build number and CVS tag information.
 * It attempts to load a system resource when the class loads,
 * and can load a build number from a URL.
 *
 * This class is and should always be immutable.
 *
 * @author Jared Klett
 * @version $Id: BuildNumber.java,v 1.2 2006/03/16 16:23:09 jklett Exp $
 */

public class BuildNumber {

// CVS info ///////////////////////////////////////////////////////////////////

    public static final String CVS_ID = "$Id: BuildNumber.java,v 1.2 2006/03/16 16:23:09 jklett Exp $";
    public static final String CVS_REV = "$Revision: 1.2 $";

// Constants //////////////////////////////////////////////////////////////////

    private static final String BUILD_NUMBER = "build.number";
    private static final String DEFAULT_NUMBER = "-1";
    private static final String TAG = "cvs.tag";
    private static final String DEFAULT_TAG = "unknown";

// Class variables ////////////////////////////////////////////////////////////

    private static BuildNumber appBuildNumber;

// Class initializer //////////////////////////////////////////////////////////

    static {
        Properties appProps = new Properties();
        try {
            appProps.load(ClassLoader.getSystemResourceAsStream(BUILD_NUMBER));
        } catch (IOException e) {
            e.printStackTrace();
        }
        int number = Integer.parseInt(appProps.getProperty(BUILD_NUMBER, DEFAULT_NUMBER));
        String tag = appProps.getProperty(TAG, DEFAULT_TAG);
        appBuildNumber = new BuildNumber(number, tag);
    }

// Class methods //////////////////////////////////////////////////////////////

    /**
     * Retrieves the application build number, loaded as a system resource.
     *
     * @return The application build number instance.
     */
    public static BuildNumber getAppBuildNumber() {
        return appBuildNumber;
    }

    /**
     * Loads build number info from the passed URL and returns a new instance.
     *
     * @param url The complete URL to load the info from.
     * @return A new instance with the info from the remote source.
     * @throws IOException If a network error occurs.
     */
    public static BuildNumber loadRemote(URL url) throws IOException {
        Properties remoteProps = new Properties();
        remoteProps.load(url.openStream());
        int number = Integer.parseInt(remoteProps.getProperty(BUILD_NUMBER, DEFAULT_NUMBER));
        String tag = remoteProps.getProperty(TAG, DEFAULT_TAG);
        return new BuildNumber(number, tag);
    }

// Instance variables /////////////////////////////////////////////////////////

    /** The numerical ID of this build. */
    private int buildNumber;
    /** The associated tag from CVS. */
    private String tag;

// Constructor ////////////////////////////////////////////////////////////////

    /**
     * Creates a new instance of our class to hold build number information.
     *
     * @param buildNumber The numerical ID of this build.
     * @param tag The associated tag from CVS.
     */
    public BuildNumber(int buildNumber, String tag) {
        this.buildNumber = buildNumber;
        this.tag = tag;
    }

// Instance methods ///////////////////////////////////////////////////////////

    /**
     * Retrieves the build number for this instance.
     *
     * @return The build number.
     */
    public int getBuildNumber() {
        return buildNumber;
    }

    /**
     * Retrieves the CVS tag for this instance.
     *
     * @return The tag from CVS.
     */
    public String getTag() {
        return tag;
    }

} // class BuildNumber
