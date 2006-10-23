/* 
 * @(#)MetadataLoader.java
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

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.apache.commons.httpclient.Cookie;

import javax.xml.parsers.ParserConfigurationException;
import java.util.Map;
import java.util.TreeMap;
import java.io.IOException;

/**
 * This is a placeholder description of this class.
 *
 * @author Jared Klett
 * @version $Id: MetadataLoader.java,v 1.6 2006/10/23 20:49:44 jklett Exp $
 */

public class MetadataLoader {

// CVS info ///////////////////////////////////////////////////////////////////

    public static final String CVS_ID = "$Id: MetadataLoader.java,v 1.6 2006/10/23 20:49:44 jklett Exp $";
    public static final String CVS_REV = "$Revision: 1.6 $";

// Constants //////////////////////////////////////////////////////////////////

    private static final String CATEGORY_TAG = "category";
    private static final String LICENSE_TAG = "license";
    private static final String BLOG_TAG = "blog";
    private static final String LANGUAGE_TAG = "language";
    private static final String RATING_TAG = "rating";

// Class variables ////////////////////////////////////////////////////////////

    public static Map licenses;
    public static Map categories;
    public static Map blogs;
    public static Map languages;
    public static Map ratings;

// Constructor ////////////////////////////////////////////////////////////////

    private MetadataLoader() {
        // should never be called outside
    }

// Class methods //////////////////////////////////////////////////////////////

    public synchronized static void load(String url, Cookie authCookie) {
        if (licenses == null && categories == null && blogs == null) {
            licenses = new TreeMap();
            categories = new TreeMap();
            blogs = new TreeMap();
            languages = new TreeMap();
            ratings = new TreeMap();
            Document document = null;
            try {
                document = XmlUtils.loadDocumentFromURL(url, authCookie);
                // TODO: better handling!
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (SAXException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            if (document != null) {
                addToMap(document, CATEGORY_TAG, categories);
                addToMap(document, LICENSE_TAG, licenses);
                addToMap(document, BLOG_TAG, blogs);
                addToMap(document, LANGUAGE_TAG, languages);
                addToMap(document, RATING_TAG, ratings);
            }
        }
    }

    private static void addToMap(Document document, String tag, Map map) {
        NodeList nodes = document.getElementsByTagName(tag);
        for (int i = 0; i < nodes.getLength(); i++) {
            NodeList children = nodes.item(i).getChildNodes();
            String id = null;
            String name = null;
            for (int x = 0; x < children.getLength(); x++) {
                Node node = children.item(x);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    String content = node.getFirstChild().getNodeValue();
                    if (children.item(x).getNodeName().equals("id"))
                        id = content;
                    else if (children.item(x).getNodeName().equals("name"))
                        name = content;
                }
            }
            map.put(name, id);
        }
    }

} // class MetadataLoader
