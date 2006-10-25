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
import java.util.Map;
import java.util.HashMap;

/**
 * Icons, yea!
 *
 * @author Jared Klett
 * @version $Id: Icons.java,v 1.6 2006/10/25 17:49:09 jklett Exp $
 */

public class Icons {

// CVS info ////////////////////////////////////////////////////////////////////

    public static final String CVS_REV = "$Revision: 1.6 $";

// Constants //////////////////////////////////////////////////////////////////

    public static final String ADD_ICON_PATH = "icons/new/add.png";
    public static final String DELETE_ICON_PATH = "icons/new/delete.png";
    public static final String IMAGE_ICON_PATH = "icons/new/image.png";
    public static final String SOUND_ICON_PATH = "icons/new/sound.png";
    public static final String VIDEO_ICON_PATH = "icons/new/film.png";
    public static final String UNKNOWN_ICON_PATH = "icons/new/page.png";

    public static final ImageIcon imageIcon = new ImageIcon(ClassLoader.getSystemResource(IMAGE_ICON_PATH));
    public static final ImageIcon soundIcon = new ImageIcon(ClassLoader.getSystemResource(SOUND_ICON_PATH));
    public static final ImageIcon videoIcon = new ImageIcon(ClassLoader.getSystemResource(VIDEO_ICON_PATH));
    public static final ImageIcon unknownIcon = new ImageIcon(ClassLoader.getSystemResource(UNKNOWN_ICON_PATH));

// Class variables ////////////////////////////////////////////////////////////

    private static Map map = new HashMap();

// Class initializer //////////////////////////////////////////////////////////

    static {
        map.put("gif", imageIcon);
        map.put("jpg", imageIcon);
        map.put("jpe", imageIcon);
        map.put("jpeg", imageIcon);
        map.put("png", imageIcon);
        map.put("tiff", imageIcon);
        map.put("tif", imageIcon);
        map.put("bmp", imageIcon);

        map.put("aif", soundIcon);
        map.put("aiff", soundIcon);
        map.put("mp3", soundIcon);
        map.put("wav", soundIcon);
        map.put("wma", soundIcon);
        map.put("m4a", soundIcon);
        map.put("snd", soundIcon);

        map.put("mov", videoIcon);
        map.put("wmv", videoIcon);
        map.put("mpg", videoIcon);
        map.put("mpe", videoIcon);
        map.put("mpeg", videoIcon);
        map.put("avi", videoIcon);
        map.put("rmvb", videoIcon);
        map.put("qt", videoIcon);
        map.put("3gp", videoIcon);
        map.put("3g2", videoIcon);
        map.put("flv", videoIcon);
        map.put("mp4", videoIcon);
        map.put("m4v", videoIcon);
    }

// Constructor ////////////////////////////////////////////////////////////////

    private Icons() {
        // will never be called outside
    }

// Class methods //////////////////////////////////////////////////////////////

    /**
     * Looks up dot-extension for the passed filename and returns an
     * appropriate icon for the file type.
     *
     * @param filename The filename of a candidate for icon-ing.
     * @return An icon suitable for use in Swing.
     * @throws IllegalArgumentException If the passed filename has no dot-extension.
     */
    public static ImageIcon getIconForFilename(String filename) {
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex == -1)
            throw new IllegalArgumentException("No dot-extension in this filename: " + filename);
        String extension = filename.substring(dotIndex + 1, filename.length());
        Object obj = map.get(extension);
        ImageIcon retval;
        if (obj == null)
            retval = unknownIcon;
        else
            retval = (ImageIcon)obj;
        return retval;
    }

} // class Icons
