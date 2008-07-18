package com.blipnetworks.upperblip;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.multipart.*;
import org.apache.commons.httpclient.methods.PostMethod;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.net.URL;
import java.net.MalformedURLException;

import com.blipnetworks.util.Parameters;
import com.blipnetworks.util.XmlUtils;
import com.blipnetworks.util.Authenticator;
import com.blipnetworks.util.RandomGUID;

import org.apache.commons.net.ftp.*;
import java.util.*;
import java.text.DateFormat;


/**
 * Created by IntelliJ IDEA.
 * User: jackie
 * Date: Jul 13, 2008
 * Time: 1:13:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class PodUploader {

// CVS info ///////////////////////////////////////////////////////////////////

    public static final String CVS_REV = "$Revision: 1.1 $";

// Constants //////////////////////////////////////////////////////////////////

    /** The name of the cookie that contains the authcode. */
    public static final String AUTH_COOKIE_NAME = "otter_auth";

    /** TODO */
    public static final int ERROR_UNKNOWN = 10;
    public static final int ERROR_BAD_AUTH = 11;
    public static final int ERROR_SERVER = 12;
    /** Default timeout for an HTTP request: 30 seconds. */
    protected static final int TIMEOUT = 30000;

// Instance variables /////////////////////////////////////////////////////////

    private Cookie authCookie;
    private String url;
    private String urlWithGuid;
    private String postURL;
    private String userAgent;
    private int timeout;
    private int errorCode;


    //

    private String ftpServer="localhost";
    private String username="admin";
    private String password="admin";
    private String folder="c:\\ftpfolder";
    private String remotefolder="rftp";
    private String identifer="jz";
    List fileItemsList=null;
    Properties params;
    List croposts;
    File vidFile;
    File thumbFile;
    File filexml=null;
    File metaxml=null;
                      // String destinationFolder,

// Constructor ////////////////////////////////////////////////////////////////

    /**
     * Creates an uploader instance with the default timeout, a URL loaded from
     * the configuration file (bliplib.properties) and no auth cookie (be sure
     * to pass a username and password in your parameters object when
     * you call <code>uploadFile()</code>).
     */
    public PodUploader() {
        this(TIMEOUT);
    }

    /**
     * Creates an uploader instance with the passed timeout, a URL loaded from
     * the configuration file (bliplib.properties) and no auth cookie (be sure
     * to pass a username and password in your parameters object when
     * you call <code>uploadFile()</code>).
     *
     * @param timeout A value, in milliseconds, after which the HTTP request should time out.
     */
    public PodUploader(int timeout) {
        this(null, timeout, null);
    }

    /**
     * Creates an PodUploader instance with the the default timeout, a URL loaded from
     * the configuration file (bliplib.properties) and the passed <code>Cookie</code>
     * as the authentication cookie.
     *
     * @param authCookie The authentication cookie that will be set in the HTTP request.
     */
    public PodUploader(Cookie authCookie) {
        this(null, TIMEOUT, authCookie);
    }

    /**
     * Creates an PodUploader instance with the the passed timeout, passed URL,
     * and the passed <code>Cookie</code> as the authentication cookie.
     *
     * @param url The URL that will be posted to.
     * @param timeout A value, in milliseconds, after which the HTTP request should time out.
     * @param authCookie The authentication cookie that will be set in the HTTP request.
     */
    public PodUploader(String url, int timeout, Cookie authCookie) {
        String fullURL;
        if (url == null) {
            fullURL = Parameters.config.getProperty(Parameters.UPLOAD_URL, Parameters.UPLOAD_URL_DEF);
        } else {
            fullURL = url;
        }
        // check the URL and throw a runtime exception if we fail
        try {
            new URL(fullURL);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("URL is invalid: " + fullURL);
        }
        // okay, on with the show...
        this.url = fullURL;
        this.timeout = timeout;
        this.authCookie = authCookie;
    }


    public boolean putDataandMetaFiles(/* Calendar start,  Calendar end*/ )
      {
          FTPClient ftp=null;
        try
        {
          // Connect and logon to FTP Server
          ftp = new FTPClient();
          ftp.connect( ftpServer );
          ftp.login( username, password );
          System.out.println("Connected to " +
               ftpServer + ".");
          System.out.print(ftp.getReplyString());
            ftp.setSoTimeout(10000);
          // List the files in the directory
          boolean res=ftp.changeWorkingDirectory( remotefolder );
            if(res)
            {
                boolean uploadfilexml=ftp.storeFile(filexml.getName(), new FileInputStream(filexml));
                boolean uploadmetaxml=ftp.storeFile(metaxml.getName(), new FileInputStream(metaxml));
                boolean uploadfile=ftp.storeFile(vidFile.getName(), new FileInputStream(vidFile));
                if(uploadfilexml && uploadmetaxml && uploadfile)
                        return true;
                else
                        return false;
            }
            else
            {
                ftp.makeDirectory(remotefolder);
                ftp.changeWorkingDirectory( remotefolder );
                boolean uploadfilexml=ftp.storeFile(filexml.getName(), new FileInputStream(filexml));
                boolean uploadmetaxml=ftp.storeFile(metaxml.getName(), new FileInputStream(metaxml));
                boolean uploadfile=ftp.storeFile(vidFile.getName(), new FileInputStream(vidFile));
                if(uploadfilexml && uploadmetaxml && uploadfile)
                        return true;
                else
                        return false;
            }

      /*    FTPFile[] files = ftp.listFiles();
          System.out.println( "Number of files in dir: " + files.length );
          DateFormat df = DateFormat.getDateInstance( DateFormat.SHORT );
          for( int i=0; i<files.length; i++ )
          {
            Date fileDate = files[ i ].getTimestamp().getTime();
        //    if( fileDate.compareTo( start.getTime() ) >= 0 &&
          //    fileDate.compareTo( end.getTime() ) <= 0 )
            {
              // Download a file from the FTP Server
              System.out.print( df.format( files[ i ].getTimestamp().getTime() ) );
              System.out.println( "\t" + files[ i ].getName() );
              File file = new File( folder +
                   File.separator + files[ i ].getName() );
              FileOutputStream fos = new FileOutputStream( file );
              ftp.retrieveFile( files[ i ].getName(), fos );
              fos.close();
              file.setLastModified( fileDate.getTime() );
            }
          }                      */

          // Logout from the FTP Server and disconnect



        }
        catch( Exception e )
        {
          e.printStackTrace();
        }
          finally
        {
            try
            {
                if(ftp!=null)
                {
                    ftp.logout();
                    ftp.disconnect();
                }
            }
            catch(Exception ee)
            {
                ee.printStackTrace();
            }
        }
        return false;
      }
// Instance methods ///////////////////////////////////////////////////////////

    /**
     * Uploads the passed file with the passed parameters as the form data.
     *
     * @param videoFile The video file to be uploaded.
     * @param parameters A collection of key-value paired form data.
     * @return True on confirmation of a successful upload, false otherwise.
     * @throws FileNotFoundException If the passed file doesn't exist.
     * @throws HttpException If an error occurs while talking to the server.
     * @throws IOException If an error occurs while talking to the server.
     * @throws ParserConfigurationException If we can't create an XML parser.
     * @throws SAXException If an error occurs while parsing the XML response.
     */
    public boolean uploadFile(File videoFile, Properties parameters) throws FileNotFoundException, HttpException, IOException, ParserConfigurationException, SAXException {
        return uploadFile(videoFile, null, parameters);
    }

    /**
     * Uploads the passed file with the passed parameters as the form data.
     *
     * @param videoFile The video file to be uploaded.
     * @param thumbnailFile The image file to be used as a thumbnail.
     * @param parameters A collection of key-value paired form data.
     * @return True on confirmation of a successful upload, false otherwise.
     * @throws FileNotFoundException If the passed file doesn't exist.
     * @throws HttpException If an error occurs while talking to the server.
     * @throws IOException If an error occurs while talking to the server.
     * @throws ParserConfigurationException If we can't create an XML parser.
     * @throws SAXException If an error occurs while parsing the XML response.
     */
    public boolean uploadFile(File videoFile, File thumbnailFile, Properties parameters) throws FileNotFoundException, HttpException, IOException, ParserConfigurationException, SAXException {
        return uploadFile(videoFile, thumbnailFile, parameters, null);
    }

    /**
     * Uploads the passed file with the passed parameters as the form data.
     *
     * @param videoFile The video file to be uploaded.
     * @param thumbnailFile The image file to be used as a thumbnail.
     * @param parameters A collection of key-value paired form data.
     * @param crossposts A list of cross-post destinations.
     * @return True on confirmation of a successful upload, false otherwise.
     * @throws FileNotFoundException If the passed file doesn't exist.
     * @throws HttpException If an error occurs while talking to the server.
     * @throws IOException If an error occurs while talking to the server.
     * @throws ParserConfigurationException If we can't create an XML parser.
     * @throws SAXException If an error occurs while parsing the XML response.
     */
    public boolean uploadFile(File videoFile, File thumbnailFile, Properties parameters, List crossposts) throws FileNotFoundException, HttpException, IOException, ParserConfigurationException, SAXException {

        this.params=parameters;
        this.croposts=crossposts;
        this.vidFile=videoFile;
        this.thumbFile=thumbnailFile;
        
        filexml = new File(folder, identifer+"_files.xml");
        FileOutputStream fStream = new FileOutputStream(filexml);
        fStream.write(transMetafile().getBytes());
        fStream.close();

        metaxml = new File(folder,identifer+"_meta.xml");
        fStream = new FileOutputStream(metaxml);
        fStream.write(transMetadata().getBytes());
        fStream.close();
        
        return this.putDataandMetaFiles();


    }

     private String transMetafile()
     {
         try {

              DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
              DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
              Document doc = docBuilder.newDocument();


              // create the root element and add it to the document
              Element root = doc.createElement("files");
              doc.appendChild(root);
            int j=0;
      //      for (Object item : fileItemsList)
            {
           //     FileItem file_item = (FileItem) item;

         //      if(file_item.getFieldName().trim().toLowerCase().startsWith("videofile"))
                {
                    String fn=vidFile.getName();
                  //  fn=validateFilename(fn);
                    if(fn!=null)
                    {
                        Element child = doc.createElement("file");
                        child.setAttribute("name", fn);
                        child.setAttribute("source", "original");
                        root.appendChild(child);
                        Element child2=doc.createElement("MD5");
                        Text text = doc.createTextNode("this is not a md5");
                        //j++;
                        child2.appendChild(text);
                        child.appendChild(child2);
                        Element child3=doc.createElement("role");
                        Text text2 = doc.createTextNode("file role "+j);
                        //j++;
                        child3.appendChild(text2);
                        child.appendChild(child3);

                    }
                    else
                    {
                     //   logger.error("invalid filename: "+file_item.getName());
                        System.out.println("invalid filename: "+vidFile.getName());

                    }


                }
            }

              // set up a transformer
              TransformerFactory transfac = TransformerFactory.newInstance();
              Transformer trans = transfac.newTransformer();
              trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
              trans.setOutputProperty(OutputKeys.INDENT, "yes");

              // create string from xml tree
              StringWriter sw = new StringWriter();
              StreamResult result = new StreamResult(sw);
              DOMSource source = new DOMSource(doc);
              trans.transform(source, result);
              return sw.toString();

              // print xml
              //System.out.println("Here's the xml:\n\n" + xmlString);

          } catch (Exception e) {
              System.out.println(e);
          }
           return null;
     }


     private String transMetadata()
     {
         try {

              DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
              DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
              Document doc = docBuilder.newDocument();

              Element root = doc.createElement("metadata");
              doc.appendChild(root);
             Enumeration<Object> keys=this.params.keys();
             while(keys.hasMoreElements())
             {
                 Object key=keys.nextElement();
                 Object value=this.params.get(key);
                 Element child=null;
                    if((key.toString().indexOf("about_event_upcoming_id")>=0)||(key.toString().indexOf("conversions")>=0))
                    {
                        //child=doc.createElement(key.toString());
                        child=doc.createElementNS("http://blip.tv","blip:"+key.toString());

                    }
                    else
                    {
                        child = doc.createElement(key.toString());

                    }

                root.appendChild(child);
                 if(value!=null)
                 {
                    Text text = doc.createTextNode(value.toString());
                    child.appendChild(text);
                 }

             }
              TransformerFactory transfac = TransformerFactory.newInstance();
              Transformer trans = transfac.newTransformer();
              trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
              trans.setOutputProperty(OutputKeys.INDENT, "yes");

              // create string from xml tree
              StringWriter sw = new StringWriter();
              StreamResult result = new StreamResult(sw);
              DOMSource source = new DOMSource(doc);
              trans.transform(source, result);
              return sw.toString();

              // print xml
              //System.out.println("Here's the xml:\n\n" + xmlString);

          } catch (Exception e) {
              System.out.println(e);
          }
           return null;
     }

    /**
     * Uploads the passed file with the passed parameters as the form data.
     *
     * @param videoFilePartSource The video file to be uploaded wrapped in a PartSource object.
     * @param thumbnailFilePartSource The image file to be used as a thumbnail wrapped in a PartSource object.
     * @param parameters A collection of key-value paired form data.
     * @param crossposts A list of cross-post destinations.
     * @return True on confirmation of a successful upload, false otherwise.
     * @throws FileNotFoundException If the passed file doesn't exist.
     * @throws HttpException If an error occurs while talking to the server.
     * @throws IOException If an error occurs while talking to the server.
     * @throws ParserConfigurationException If we can't create an XML parser.
     * @throws SAXException If an error occurs while parsing the XML response.
     */
    public boolean uploadFile(PartSource videoFilePartSource, PartSource thumbnailFilePartSource, Properties parameters, List crossposts) throws FileNotFoundException, HttpException, IOException, ParserConfigurationException, SAXException {
        if (urlWithGuid == null)
            throw new IllegalStateException("No GUID has been set");
        PostMethod post = new PostMethod(urlWithGuid);
        FilePart videoFilePart = new FilePart(Parameters.FILE_PARAM_KEY, videoFilePartSource);
        FilePart thumbnailFilePart = null;
        if (thumbnailFilePartSource != null)
            thumbnailFilePart = new FilePart(Parameters.THUMB_PARAM_KEY, thumbnailFilePartSource);
        Part[] parts = setRequestParts(videoFilePart, thumbnailFilePart, parameters, crossposts);
        post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));
        boolean succeeded = false;
        try {
            HttpClient client = new HttpClient();
            // Set a tolerant cookie policy
            client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            if (userAgent != null)
                client.getParams().setParameter(HttpMethodParams.USER_AGENT, userAgent);
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
            Document document = XmlUtils.makeDocumentFromString(post.getResponseBodyAsString());
            if (responseCode == HttpStatus.SC_OK) {
                if (document != null) {
                    // avoid NPE below
                    String responseText = "";
                    try {
                        // attempt to discern the status from the response
                        responseText = document.getElementsByTagName("response").item(0).getFirstChild().getNodeValue();
                        if (responseText.indexOf("has been successfully posted") != -1) {
                            // Extract the post URL from the child node of response
                            postURL = document.getElementsByTagName("post_url").item(0).getFirstChild().getNodeValue().trim();
                            succeeded = true;
                        }
                    }
                    catch (Exception e) {
                        // we want to catch anything since we're making some assumptions above
                        // regarding indices and whatnot
                        System.out.println("Couldn't find valid URL in response text:\n" + responseText);
                        e.printStackTrace();
                    }
                    if (responseText.indexOf("couldn't find an account") != -1)
                        errorCode = ERROR_BAD_AUTH;
                    return responseText.indexOf("has been successfully posted") != -1;
/*
Other possible response strings:
                    "username and password combination";
                    "You must";
                    "critical error";
*/
                }
            } else {
                succeeded = false;
                errorCode = ERROR_SERVER;
            }
        }
        finally {
            post.releaseConnection();
        }
        return succeeded;
    } // method uploadFile

    private Part[] setRequestParts(FilePart videoFilePart, FilePart thumbnailFilePart, Properties parameters, List crossposts) {
        Part[] typeArray = new Part[0];
        List list = new ArrayList();
        list.add(videoFilePart);
        if (thumbnailFilePart != null)
            list.add(thumbnailFilePart);
        list.add(Parameters.getStringPart(parameters, Parameters.TITLE_PARAM_KEY));
        list.add(Parameters.getStringPart(parameters, Parameters.POST_PARAM_KEY));
        list.add(Parameters.getStringPart(parameters, Parameters.CAT_PARAM_KEY));
        list.add(Parameters.getStringPart(parameters, Parameters.TAGS_PARAM_KEY));
        list.add(Parameters.getStringPart(parameters, Parameters.LICENSE_PARAM_KEY));
        list.add(Parameters.getStringPart(parameters, Parameters.SKIN_PARAM_KEY));
        list.add(Parameters.getStringPart(parameters, Parameters.DESC_PARAM_KEY));
        list.add(Parameters.getStringPart(parameters, Parameters.INGEST_PARAM_KEY));
        list.add(Parameters.getStringPart(parameters, Parameters.EXPLICIT_PARAM_KEY));
        list.add(Parameters.getStringPart(parameters, Parameters.LANGUAGE_PARAM_KEY));
        list.add(Parameters.getStringPart(parameters, Parameters.RATING_PARAM_KEY));
        if (crossposts != null) {
            for (int i = 0; i < crossposts.size(); i++)
                list.add(new StringPart(Parameters.CROSSPOST_PARAM_KEY, (String)crossposts.get(i)));
        }
        String ia = parameters.getProperty(Parameters.IA_PARAM_KEY);
        if (ia != null)
            list.add(new StringPart(Parameters.IA_PARAM_KEY, ia));
        // We want to omit the un/pw parts if we have an auth cookie
        if (authCookie == null) {
            // if the caller hasn't populated their parameters with a un/pw
            // an exception will be thrown from within the Parameters class
            list.add(Parameters.getStringPart(parameters, Parameters.USER_PARAM_KEY));
            list.add(Parameters.getStringPart(parameters, Parameters.PASS_PARAM_KEY));
        }
        return (Part[])list.toArray(typeArray);
    }

// Accessors //////////////////////////////////////////////////////////////////

    /**
     * Upon upload failure, an error code will be recorded.
     * @return The error code.
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * Upon upload success, the XML response will be parsed for the URL to the
     * post on Blip.tv
     * @return The post URL.
     */
    public String getPostURL() {
        return postURL;
    }

    /**
     * Retrieves the currently set user-agent string that will be sent in the
     * HTTP request.
     * @return The user-agent string.
     */
    public String getUserAgent() {
        return userAgent;
    }

// Mutators ///////////////////////////////////////////////////////////////////

    /**
     * Validates the passed GUID/UUID, and sets it to be used upon success.
     * @param guid The GUID/UUID to be used when uploading.
     */
    public void setGuid(String guid) {
        urlWithGuid = url + guid;
    }

    /**
     * Sets the authentication cookie to be used when uploading.
     * @param authCookie The cookie received from the <code>Authenticator</code> class.
     */
    public void setAuthCookie(Cookie authCookie) {
        this.authCookie = authCookie;
    }

    /**
     * Sets the user-agent string that will be sent in the HTTP request.
     * @param userAgent The new user-agent string to be used.
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

// Main method ////////////////////////////////////////////////////////////////

    /**
     * Main method will run a bare-bones upload using the passed arguments.
     *
     * @param args Command-line arguments.
     */
    /*public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java PodUploader <file> <user> <pass>");
            System.out.println("Optional parameters: <title> <desc>");
            return;
        }
        try {
            File file = new File(args[0]);
            Properties props = new Properties();
            if (args.length > 4) {
                props.put(Parameters.TITLE_PARAM_KEY, args[3]);
                props.put(Parameters.DESC_PARAM_KEY, args[4]);
            }

            Cookie cookie = Authenticator.authenticate(args[1], args[2]);
            PodUploader podUploader = new PodUploader(cookie);
            String guid = new RandomGUID().toString();
            podUploader.setGuid(guid);
            podUploader.uploadFile(file, props);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }   */
}
