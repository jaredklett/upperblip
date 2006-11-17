/*
 * @(#)BuildNumber.java
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

import java.util.Properties;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * A class to encapsulate build number and CVS tag information.
 * It attempts to load a system resource when the class loads,
 * and can load a build number from a URL.
 *
 * This class is and should always be immutable.
 *
 * @author Jared Klett
 * @version $Id: BuildNumber.java,v 1.8 2006/11/17 19:50:21 jklett Exp $
 */

public class BuildNumber {

// CVS info ///////////////////////////////////////////////////////////////////

    public static final String CVS_ID = "$Id: BuildNumber.java,v 1.8 2006/11/17 19:50:21 jklett Exp $";
    public static final String CVS_REV = "$Revision: 1.8 $";

// Constants //////////////////////////////////////////////////////////////////

    private static final String BUILD_NUMBER = "build.number";
    private static final String DEFAULT_NUMBER = "0";
    private static final String TAG = "cvs.tag";
    private static final String DEFAULT_TAG = "none";

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
    protected BuildNumber(int buildNumber, String tag) {
        this.buildNumber = buildNumber;
        this.tag = tag;
    }

// Class methods //////////////////////////////////////////////////////////////

    /**
     * Loads build number info from the passed URL and returns a new instance.
     *
     * @param url The complete URL to load the info from.
     * @return A new instance with the info from the remote source.
     * @throws IOException If a network error occurs.
     */
    public static BuildNumber loadRemote(URL url) throws IOException {
        return load(url.openStream());
    }

    /**
     *
     * @return
     * @throws IOException
     */
    public static BuildNumber loadLocal() throws IOException {
        return load(BuildNumber.class.getClassLoader().getResourceAsStream(BUILD_NUMBER));
    }

    /**
     *
     * @param in
     * @return
     * @throws IOException
     */
    private static BuildNumber load(InputStream in) throws IOException {
        Properties props = new Properties();
        props.load(in);
        int number = Integer.parseInt(props.getProperty(BUILD_NUMBER, DEFAULT_NUMBER));
        String tag = props.getProperty(TAG, DEFAULT_TAG);
        return new BuildNumber(number, tag);
    }

// Instance methods ///////////////////////////////////////////////////////////

    /**
     *
     * @param obj
     * @return foo
     */
    public boolean equals(Object obj) {
        if (obj instanceof BuildNumber)
            return ((BuildNumber)obj).getBuildNumber() == buildNumber;
        else
            return false;
    }

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

    public static void main(String[] args) {
        try {
            BuildNumber localBn = new BuildNumber(168, DEFAULT_TAG);
            BuildNumber bn = BuildNumber.loadRemote(new URL(args[0]));
            System.out.println(bn.equals(localBn));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

} // class BuildNumber
