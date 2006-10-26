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
 * @version $Id: Icons.java,v 1.7 2006/10/26 00:11:08 jklett Exp $
 */

public class Icons {

// CVS info ////////////////////////////////////////////////////////////////////

    public static final String CVS_REV = "$Revision: 1.7 $";

// Constants //////////////////////////////////////////////////////////////////

    public static final String ADD_ICON_PATH = "icons/new/add.png";
    public static final String DELETE_ICON_PATH = "icons/new/delete.png";
    public static final String IMAGE_ICON_PATH = "icons/new/image.png";
    public static final String SOUND_ICON_PATH = "icons/new/sound.png";
    public static final String VIDEO_ICON_PATH = "icons/new/film.png";
    public static final String UNKNOWN_ICON_PATH = "icons/new/page.png";
    public static final String FRAME_ICON_PATH = "icons/new/upperblip.png";

    public static final ImageIcon imageIcon = new ImageIcon(ClassLoader.getSystemResource(IMAGE_ICON_PATH));
    public static final ImageIcon soundIcon = new ImageIcon(ClassLoader.getSystemResource(SOUND_ICON_PATH));
    public static final ImageIcon videoIcon = new ImageIcon(ClassLoader.getSystemResource(VIDEO_ICON_PATH));
    public static final ImageIcon unknownIcon = new ImageIcon(ClassLoader.getSystemResource(UNKNOWN_ICON_PATH));
    public static final ImageIcon frameIcon = new ImageIcon(ClassLoader.getSystemResource(FRAME_ICON_PATH));

    public static final String[] imageFormats = {
            "gif",
            "jpg",
            "jpe",
            "jpeg",
            "png",
            "tiff",
            "tif",
            "bmp"
    };

    public static final String[] soundFormats = {
            "aif",
            "aiff",
            "mp3",
            "wav",
            "wma",
            "m4a"
    };

    public static final String[] videoFormats = {
            "mov",
            "wmv",
            "mpg",
            "mpe",
            "mpeg",
            "avi",
            "rmvb",
            "qt",
            "3gp",
            "3g2",
            "flv",
            "mp4",
            "m4v"
    };

// Class variables ////////////////////////////////////////////////////////////

    private static Map iconMap = new HashMap();

// Class initializer //////////////////////////////////////////////////////////

    static {
        for (int i = 0; i < imageFormats.length; i++)
            iconMap.put(imageFormats[i], imageIcon);

        for (int i = 0; i < soundFormats.length; i++)
            iconMap.put(soundFormats[i], soundIcon);

        for (int i = 0; i < videoFormats.length; i++)
            iconMap.put(videoFormats[i], videoIcon);
    }

// Constructor ////////////////////////////////////////////////////////////////

    private Icons() {
        // will never be called outside
    }

// Class methods //////////////////////////////////////////////////////////////

    public static boolean isImage(String filename) {
        String extension = extractExtension(filename);
        List list = Arrays.asList(imageFormats);
        return list.contains(extension);
    }

    /**
     * Looks up dot-extension for the passed filename and returns an
     * appropriate icon for the file type.
     *
     * @param filename The filename of a candidate for icon-ing.
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

    private static String extractExtension(String filename) {
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex == -1)
            throw new IllegalArgumentException("No dot-extension in this filename: " + filename);
        return filename.substring(dotIndex + 1, filename.length());
    }

} // class Icons
