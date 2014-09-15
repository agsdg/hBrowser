package com.example.dsf;


import java.net.*;
import java.io.*;
import java.util.*;
import java.text.*;

import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;

    /**
     * CookieManager is a simple utilty for handling cookies when working
     * with java.net.URL and java.net.URLConnection
     * objects.
     * 
     * 
     *     Cookiemanager cm = new CookieManager();
     *     URL url = new URL("http://www.hccp.org/test/cookieTest.jsp");
     *     
     *      . . . 
     *
     *     // getting cookies:
     *     URLConnection conn = url.openConnection();
     *     conn.connect();
     *
     *     // setting cookies
     *     cm.storeCookies(conn);
     *     cm.setCookies(url.openConnection());
     * 
     *     @author Ian Brown
     *      
     **/

public class CookiesManager {
        
    public Map store;

    public static final String SET_COOKIE = "Set-Cookie";
    public static final String COOKIE_VALUE_DELIMITER = ";";
    public static final String PATH = "path";
    public static final String EXPIRES = "expires";
    public static final String DATE_FORMAT = "EEE, dd-MMM-yyyy hh:mm:ss z";
    public static final String SET_COOKIE_SEPARATOR="; ";
    public static final String COOKIE = "Cookie";

    public static final char NAME_VALUE_SEPARATOR = '=';
    public static final char DOT = '.';
    
    CookieSyncManager cookieSyncManager ;
    CookieManager cookieManager;
    
    public DateFormat dateFormat;

    public CookiesManager(WebView webView) {

    	store = new HashMap();
    	dateFormat = new SimpleDateFormat(DATE_FORMAT);
    	
    	cookieSyncManager= CookieSyncManager.createInstance(webView.getContext());
		cookieManager  = CookieManager.getInstance();
		 cookieManager.setAcceptCookie(true);
		 cookieSyncManager.sync();
    }
    

    /**
     * Retrieves and stores cookies returned by the host on the other side
     * of the the open java.net.URLConnection.
     *
     * The connection MUST have been opened using the connect()
     * method or a IOException will be thrown.
     *
     * @param conn a java.net.URLConnection - must be open, or IOException will be thrown
     * @throws java.io.IOException Thrown if conn is not open.
     */
    @SuppressWarnings("unchecked")
	public void storeCookies(URLConnection conn) throws IOException {
	
	// let's determine the domain from where these cookies are being sent
	String domain = getDomainFromHost(conn.getURL().getHost());
	
	
	Map domainStore; // this is where we will store cookies for this domain
	
	// now let's check the store to see if we have an entry for this domain
	if (store.containsKey(domain)) {
	    // we do, so lets retrieve it from the store
	    domainStore = (Map)store.get(domain);
	} else {
	    // we don't, so let's create it and put it in the store
	    domainStore = new HashMap();
	    store.put(domain, domainStore);    
	}
	
	
	
	
	// OK, now we are ready to get the cookies out of the URLConnection
	
	String headerName=null;
	for (int i=1; (headerName = conn.getHeaderFieldKey(i)) != null; i++) {
	    if (headerName.equalsIgnoreCase(SET_COOKIE)) {
		Map cookie = new HashMap();
		StringTokenizer st = new StringTokenizer(conn.getHeaderField(i), COOKIE_VALUE_DELIMITER);
		
		 cookieManager.setCookie(domain,conn.getHeaderField(i));
		   cookieSyncManager.sync();
		    System.out.println(domain+"-COOOKEIEI:-"+conn.getHeaderField(i));
		// the specification dictates that the first name/value pair
		// in the string is the cookie name and value, so let's handle
		// them as a special case: 
		
		if (st.hasMoreTokens()) {
		    String token  = st.nextToken();
		    String name = token.substring(0, token.indexOf(NAME_VALUE_SEPARATOR));
		    String value = token.substring(token.indexOf(NAME_VALUE_SEPARATOR) + 1, token.length());
		    domainStore.put(name, cookie);
		    cookie.put(name, value);
		   
		}
    
		while (st.hasMoreTokens()) {
		    String token  = st.nextToken();
		   
		       System.out.println("token;"+token);
		   // try{
		    System.out.println();
		    if(token.indexOf("=")>0&&token.indexOf("=")<(token.length()-1)){
		   System.out.println(token.substring(token.indexOf(NAME_VALUE_SEPARATOR) + 1, token.length()));
		    cookie.put(token.substring(0, token.indexOf(NAME_VALUE_SEPARATOR)).toLowerCase(),
		     token.substring(token.indexOf(NAME_VALUE_SEPARATOR) + 1, token.length()));
		   // }catch(StringIndexOutOfBoundsException e){}
		    }
		    }
	    }
	}
    }
 

    /**
     * Prior to opening a URLConnection, calling this method will set all
     * unexpired cookies that match the path or subpaths for thi underlying URL
     *
     * The connection MUST NOT have been opened 
     * method or an IOException will be thrown.
     *
     * @param conn a java.net.URLConnection - must NOT be open, or IOException will be thrown
     * @throws java.io.IOException Thrown if conn has already been opened.
     */
    public void setCookies(URLConnection conn) throws IOException {
	
	// let's determine the domain and path to retrieve the appropriate cookies
	URL url = conn.getURL();
	String domain = getDomainFromHost(url.getHost());
	String path = url.getPath();
	
	Map domainStore = (Map)store.get(domain);
	if (domainStore == null) return;
	StringBuffer cookieStringBuffer = new StringBuffer();
	
	Iterator cookieNames = domainStore.keySet().iterator();
	while(cookieNames.hasNext()) {
	    String cookieName = (String)cookieNames.next();
	    Map cookie = (Map)domainStore.get(cookieName);
	    // check cookie to ensure path matches  and cookie is not expired
	    // if all is cool, add cookie to header string 
	    if (comparePaths((String)cookie.get(PATH), path) && isNotExpired((String)cookie.get(EXPIRES))) {
		System.out.println(0);
	    	cookieStringBuffer.append(cookieName);
		cookieStringBuffer.append("=");
		cookieStringBuffer.append((String)cookie.get(cookieName));
		if (cookieNames.hasNext()) cookieStringBuffer.append(SET_COOKIE_SEPARATOR);
	    }
	}
	try {
	    conn.setRequestProperty(COOKIE, cookieStringBuffer.toString());
	} catch (java.lang.IllegalStateException ise) {
	    IOException ioe = new IOException("Illegal State! Cookies cannot be set on a URLConnection that is already connected. " 
	    + "Only call setCookies(java.net.URLConnection) AFTER calling java.net.URLConnection.connect().");
	    throw ioe;
	}
    }

    public String getDomainFromHost(String host) {
	if (host.indexOf(DOT) != host.lastIndexOf(DOT)) {
	    return host.substring(host.indexOf(DOT) + 1);
	} else {
	    return host;
	}
    }

    public boolean isNotExpired(String cookieExpires) {
	if (cookieExpires == null) return true;
	Date now = new Date();
	Date sdf ;
	try {
	
		sdf=new SimpleDateFormat().parse(cookieExpires);
	    return (now.compareTo(sdf)) <= 0;
	    //dateFormat.parse(cookieExpires)
	} catch (ParseException pe) {
		 pe.printStackTrace();
		return true;
	   
	    
	}
    }

    public boolean comparePaths(String cookiePath, String targetPath) {
	if (cookiePath == null) {
	    return true;
	} else if (cookiePath.equals("/")) {
	    return true;
	} else if (targetPath.regionMatches(0, cookiePath, 0, cookiePath.length())) {
	    return true;
	} else {
	    return false;
	}
	
    }
    
    /**
     * Returns a string representation of stored cookies organized by domain.
     */

    public String toString() {
	return store.toString();
    }
    
   /* public static void main(String[] args) { 
	CookieManager cm = new CookieManager();
	try {
	    URL url = new URL("http://www.hccp.org/test/cookieTest.jsp");
	    URLConnection conn = url.openConnection();
	    conn.connect();
	    cm.storeCookies(conn);
	    System.out.println(cm);
	    cm.setCookies(url.openConnection());
	} catch (IOException ioe) {
	    ioe.printStackTrace();
	}
    }*/
    
}
	
	