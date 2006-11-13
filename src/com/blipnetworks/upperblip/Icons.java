/*
 * @(#)Icons.java
 *
 * Copyright (c) 2006 by Blip Networks, Inc.
 * 239 Centre St, 3rd Floor
 * New York, NY 10013
 * All rights reserved.
 *
 * This software is the confidential and
 * proprietary information of Blip Networks, Inc.
 */

package com.blipnetworks.upperblip;

import javax.swing.*;
import java.util.*;

/**
 * Icons, yea!
 *
 * @author Jared Klett
 * @version $Id: Icons.java,v 1.11 2006/11/13 22:27:29 jklett Exp $
 */

public class Icons {

// CVS info ////////////////////////////////////////////////////////////////////

    public static final String CVS_REV = "$Revision: 1.11 $";

// Constants //////////////////////////////////////////////////////////////////

    private static final String IMAGE_FORMATS_KEY = "image.formats";
    private static final String SOUND_FORMATS_KEY = "sound.formats";
    private static final String VIDEO_FORMATS_KEY = "video.formats";
    private static final String DISALLOWED_FORMATS_KEY = "disallowed.formats";

    public static final String ADD_ICON_PATH = "icons/add.png";
    public static final String DELETE_ICON_PATH = "icons/delete.png";
    public static final String IMAGE_ICON_PATH = "icons/image.png";
    public static final String SOUND_ICON_PATH = "icons/sound.png";
    public static final String VIDEO_ICON_PATH = "icons/film.png";
    public static final String UNKNOWN_ICON_PATH = "icons/error.png";
    public static final String DISALLOWED_ICON_PATH = "icons/stop.png";
    public static final String FRAME_ICON_PATH = "icons/upperblip.png";
    public static final String SUCCESS_ICON_PATH = "icons/accept.png";
    public static final String ERROR_ICON_PATH = "icons/exclamation.png";

    public static final ImageIcon imageIcon = new ImageIcon(ClassLoader.getSystemResource(IMAGE_ICON_PATH));
    public static final ImageIcon soundIcon = new ImageIcon(ClassLoader.getSystemResource(SOUND_ICON_PATH));
    public static final ImageIcon videoIcon = new ImageIcon(ClassLoader.getSystemResource(VIDEO_ICON_PATH));
    public static final ImageIcon unknownIcon = new ImageIcon(ClassLoader.getSystemResource(UNKNOWN_ICON_PATH));
    public static final ImageIcon frameIcon = new ImageIcon(ClassLoader.getSystemResource(FRAME_ICON_PATH));
    public static final ImageIcon successIcon = new ImageIcon(ClassLoader.getSystemResource(SUCCESS_ICON_PATH));
    public static final ImageIcon errorIcon = new ImageIcon(ClassLoader.getSystemResource(ERROR_ICON_PATH));
    public static final ImageIcon disallowedIcon = new ImageIcon(ClassLoader.getSystemResource(DISALLOWED_ICON_PATH));

    public static String[] imageFormats;
    public static String[] soundFormats;
    public static String[] videoFormats;
    public static String[] disallowedFormats;

// Class variables ////////////////////////////////////////////////////////////

    private static Map iconMap = new HashMap();

// Class initializer //////////////////////////////////////////////////////////

    static {
        // TODO: use a default here?
        String property = Main.appProperties.getProperty(IMAGE_FORMATS_KEY);
        if (property != null) {
            imageFormats = property.split(",");
            for (int i = 0; i < imageFormats.length; i++)
                iconMap.put(imageFormats[i], imageIcon);
        }
        property = Main.appProperties.getProperty(SOUND_FORMATS_KEY);
        if (property != null) {
            soundFormats = property.split(",");
            for (int i = 0; i < soundFormats.length; i++)
                iconMap.put(soundFormats[i], soundIcon);
        }
        property = Main.appProperties.getProperty(VIDEO_FORMATS_KEY);
        if (property != null) {
            videoFormats = property.split(",");
            for (int i = 0; i < videoFormats.length; i++)
                iconMap.put(videoFormats[i], videoIcon);
        }
        property = Main.appProperties.getProperty(DISALLOWED_FORMATS_KEY);
        if (property != null) {
            disallowedFormats = property.split(",");
            for (int i = 0; i < disallowedFormats.length; i++)
                iconMap.put(disallowedFormats[i], disallowedIcon);
        }
    }

// Constructor ////////////////////////////////////////////////////////////////

    private Icons() {
        // will never be called outside
    }

// Class methods //////////////////////////////////////////////////////////////

    /**
     * foo
     *
     * @param filename The name of the file to be checked.
     * @return True if the passed filename is that of an image, false otherwise.
     */
    public static boolean isImage(String filename) {
        return check(filename, imageFormats);
    }

    public static boolean isDisallowed(String filename) {
        return check(filename, disallowedFormats);
    }

    private static boolean check(String filename, String[] array) {
        String extension = extractExtension(filename);
        List list = Arrays.asList(array);
        return list.contains(extension);
    }

    /**
     * Looks up dot-extension for the passed filename and returns an
     * appropriate icon for the file type.
     *
     * @param filename The name of a candidate for icon-ing.
     * @return An icon suitable for use in Swing.
     * @throws IllegalArgumentException If the passed filename has no dot-extension.
     */
    public static ImageIcon getIconForFilename(String filename) {
        String extension = extractExtension(filename);
        Object obj = iconMap.get(extension);
        ImageIcon retval;
        if (obj == null)
            retval = unknownIcon;
        else
            retval = (ImageIcon)obj;
        return retval;
    }

    /**
     * Takes a substring of the passed filename.
     *
     * @param filename The name of the file to be worked on.
     * @return The filename extension which usually holds the key to the type.
     */
    private static String extractExtension(String filename) {
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex == -1)
            throw new IllegalArgumentException("No dot-extension in this filename: " + filename);
        return filename.substring(dotIndex + 1, filename.length());
    }

} // class Icons
