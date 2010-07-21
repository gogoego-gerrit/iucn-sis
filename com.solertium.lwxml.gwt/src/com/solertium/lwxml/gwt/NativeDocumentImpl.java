/*
 * Copyright (C) 2007-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, you may only modify or redistribute
 * this code under the terms of one of the following licenses:
 * 
 * 1) The Eclipse Public License, v.1.0
 *    http://www.eclipse.org/legal/epl-v10.html
 *
 * 2) The GNU General Public License, version 2 or later
 *    http://www.gnu.org/licenses
 */

package com.solertium.lwxml.gwt;

import java.util.HashMap;
import java.util.Iterator;

import com.google.gwt.core.client.JavaScriptObject;
import com.solertium.lwxml.shared.GWTConflictException;
import com.solertium.lwxml.shared.GWTForbiddenException;
import com.solertium.lwxml.shared.GWTNotFoundException;
import com.solertium.lwxml.shared.GWTResponseException;
import com.solertium.lwxml.shared.GWTUnauthorizedException;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;

/**
 * GWT DOM-like implementation that peers the browser-parsed DOM returning from
 * an XMLHttpRequest operation. For simple access to server-side XML resources,
 * this can result in extremely fast parse/access times vis-a-vis full treatment
 * with pure GWT mechanisms (10X-100X improvement) and significant Javascript
 * memory heap savings.
 * <p>
 * These classes do not follow the DOM API exactly and should not be coerced to
 * do so. The purpose of this library is to achieve a very specific performance
 * optimization task, not to provide full DOM standard compliance and
 * portability; that already exists in GWT with the attendant costs.
 *
 * @author rob.heittman@solertium.com
 */
public class NativeDocumentImpl extends NativeNodeImpl implements NativeDocument {

	private final HashMap<String, String> headers = new HashMap<String, String>();
	private static final HashMap<String, String> responseHeaders = new HashMap<String, String>();
	public static String allResponseHeaders = null;

	/**
	 * Default constructor
	 *
	 */
	public NativeDocumentImpl() {
		super();
		headers.put("If-Modified-Since", "Sat, 1 Jan 2000 00:00:00 GMT");
		headers.put("Cache-Control", "no-cache");
	}

	/**
	 * Creates a new document based on the peer object of a node.
	 * @param node
	 */
	public NativeDocumentImpl(NativeNodeImpl node) {
		super(node.peer);
	}

	/**
	 * Returns a HashMap containing response headers. CURRENTLY only stores the
	 * location header, if it exists; add all the other headers you want to
	 * _parseResponseHeaders()
	 *
	 * @return HashMap of response headers
	 */
	public HashMap<String, String> getResponseHeaders() {
		return responseHeaders;
	}

	public static void _addResponseHeader(String name, String value) {
		if( name != null && value != null )
			responseHeaders.put(name, value);
	}

	private static native JavaScriptObject _parse(String xml) /*-{
		if (typeof DOMParser != "undefined") {
	        return (new DOMParser()).parseFromString(xml, "application/xml");
	    }

	    //SOURCE: http://www.w3schools.com/xml/xml_parser.asp - for IE5+
	    else if (typeof ActiveXObject != "undefined") {
	    	var xmlDoc=new ActiveXObject("Microsoft.XMLDOM");
			xmlDoc.async="false";
			xmlDoc.loadXML(xml);
			return xmlDoc;
	    }

	    else {
	        var url = "data:text/xml;charset=utf-8," + encodeURIComponent(xml);
	        var request = new XMLHttpRequest();
	        request.open("GET", url, false);
	        return request.responseXML;
	    }
	}-*/;



	/**
	 * This method is called via JSNI and is needed to simply the method call and
	 * catch the JSNI call
	 * @param target the native document
	 * @param LWXML_req the request object
	 */
	protected static void _addHeaders(final NativeDocumentImpl target, JavaScriptObject LWXML_req) {
		target.insertHeaders(LWXML_req);
	}

	/**
	 * In order to get this method to do something useful, you'll need to override it,
	 * call a JSNI function passing it the request object from the parameter, and then
	 * do your business.  By default, it adds our default standard headers.
	 * @param LWXML_req the XMLHTTP request object
	 */
	private void insertHeaders(JavaScriptObject LWXML_req) {
		Iterator<String> iterator = headers.keySet().iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			NativeDocumentImpl._setHeader(LWXML_req, key, headers.get(key));
		}
	}

	public void setHeader(String key, String value) {
		headers.put(key, value);
	}

	public static void assignPeer(final NativeDocumentImpl target,
			final JavaScriptObject peer) {
		target.peer = peer;
	}

	public static void assignText(final NativeDocumentImpl target, final String text) {
		target.text = text;
	}

	public static void assignStatusText(final NativeDocumentImpl target, final String statusText) {
		target.statusText = statusText;
	}

	public static void errorSignalled(final String errorMessage,
			final GenericCallback<String> callback) {
		Exception e;
		try{
			int code = Integer.parseInt(errorMessage);
			if(code==404) e = new GWTNotFoundException(errorMessage);
			else if(code==403) e = new GWTForbiddenException(errorMessage);
			else if(code==401) e = new GWTUnauthorizedException(errorMessage);
			else if(code==409) e = new GWTConflictException(errorMessage);
			else e = new GWTResponseException(code, errorMessage);
		} catch (Exception defaultHandling) {
			e = new Exception(errorMessage);
		}
		System.out.println("NativeDocument in error signalled.");
		callback.onFailure(e);
	}

	public static void _setAllResponseHeaders(String headerString) {
		allResponseHeaders = headerString;
		responseHeaders.clear();
		String[] lines = headerString.split("\n");
		for (String l : lines) {
			int index = l.indexOf(":");
			if (index != -1)
				responseHeaders.put(
					l.substring(0, index).trim(),
					l.substring(index+1).trim()
				);
		}
	}

	public String getAllResponseHeaders() {
		return allResponseHeaders;
	}

	@SuppressWarnings("unused")
	private static native void _parseResponseHeaders(JavaScriptObject LWXML_req) /*-{
		try {
			@com.solertium.lwxml.gwt.NativeDocumentImpl::_setAllResponseHeaders(Ljava/lang/String;)(
				  LWXML_req.getAllResponseHeaders());

		    var location = LWXML_req.getResponseHeader("location");
		    if(!(location===undefined)){
				@com.solertium.lwxml.gwt.NativeDocumentImpl::_addResponseHeader(Ljava/lang/String;Ljava/lang/String;)(
				  "location", location);
			}
		}
		catch(e) {}
	}-*/;

	public static native void fetch(JavaScriptObject LWXML_req, String uri,
			String method, String body, String username, String password,
			NativeDocumentImpl target, GenericCallback<String> callback) /*-{
	    function LWXML_partProcess(){
		  if(LWXML_req.readyState == 4){
	  		@com.solertium.lwxml.gwt.NativeDocumentImpl::assignStatusText(Lcom/solertium/lwxml/gwt/NativeDocumentImpl;Ljava/lang/String;)(target,LWXML_req.status+"");
		  	@com.solertium.lwxml.gwt.NativeDocumentImpl::_parseResponseHeaders(Lcom/google/gwt/core/client/JavaScriptObject;)(LWXML_req);
		    if(LWXML_req.status<200 || LWXML_req.status>299){
		      @com.solertium.lwxml.gwt.NativeDocumentImpl::assignText(Lcom/solertium/lwxml/gwt/NativeDocumentImpl;Ljava/lang/String;)(target,LWXML_req.responseText);
		      // handle error
		      @com.solertium.lwxml.gwt.NativeDocumentImpl::errorSignalled(Ljava/lang/String;Lcom/solertium/lwxml/shared/GenericCallback;)(
		       ""+LWXML_req.status,
		       callback
		      );
		    } else {
		    	@com.solertium.lwxml.gwt.NativeDocumentImpl::assignText(Lcom/solertium/lwxml/gwt/NativeDocumentImpl;Ljava/lang/String;)(target,LWXML_req.responseText);
		      	@com.solertium.lwxml.gwt.NativeDocumentImpl::readySignalled(Lcom/solertium/lwxml/shared/GenericCallback;Ljava/lang/String;)(
		       	callback,""+LWXML_req.status
		      );
		    }
		  }
	    }
	    LWXML_req.open(method,uri,true,username,password);
		try {
			@com.solertium.lwxml.gwt.NativeDocumentImpl::_addHeaders(Lcom/solertium/lwxml/gwt/NativeDocumentImpl;Lcom/google/gwt/core/client/JavaScriptObject;)(target,LWXML_req);
		} catch (e) {}
	    LWXML_req.onreadystatechange = LWXML_partProcess;
		LWXML_req.send(body);
	}-*/;

	public static native void fetchAndParse(JavaScriptObject LWXML_req,
			String uri, String method, String body, String username,
			String password, NativeDocumentImpl target, GenericCallback<String> callback) /*-{
	    function LWXML_partProcess(){
		  if(LWXML_req.readyState == 4){
	  		@com.solertium.lwxml.gwt.NativeDocumentImpl::assignStatusText(Lcom/solertium/lwxml/gwt/NativeDocumentImpl;Ljava/lang/String;)(target,LWXML_req.status+"");
		  	@com.solertium.lwxml.gwt.NativeDocumentImpl::_parseResponseHeaders(Lcom/google/gwt/core/client/JavaScriptObject;)(LWXML_req);
		    if(LWXML_req.status<200 || LWXML_req.status>299){
		      @com.solertium.lwxml.gwt.NativeDocumentImpl::assignText(Lcom/solertium/lwxml/gwt/NativeDocumentImpl;Ljava/lang/String;)(target,LWXML_req.responseText);
		      // handle error
		      @com.solertium.lwxml.gwt.NativeDocumentImpl::errorSignalled(Ljava/lang/String;Lcom/solertium/lwxml/shared/GenericCallback;)(
		       ""+LWXML_req.status,
		       callback
		      );
		    } else {
		        @com.solertium.lwxml.gwt.NativeDocumentImpl::assignText(Lcom/solertium/lwxml/gwt/NativeDocumentImpl;Ljava/lang/String;)(target,LWXML_req.responseText);
		    	@com.solertium.lwxml.gwt.NativeDocumentImpl::assignPeer(Lcom/solertium/lwxml/gwt/NativeDocumentImpl;Lcom/google/gwt/core/client/JavaScriptObject;)(target,LWXML_req.responseXML);
		      	@com.solertium.lwxml.gwt.NativeDocumentImpl::readySignalled(Lcom/solertium/lwxml/shared/GenericCallback;Ljava/lang/String;)(
		       	callback,""+LWXML_req.status
		      );
		    }
		  }
	    }
	    LWXML_req.open(method,uri,true,username,password);
		try {
			@com.solertium.lwxml.gwt.NativeDocumentImpl::_addHeaders(Lcom/solertium/lwxml/gwt/NativeDocumentImpl;Lcom/google/gwt/core/client/JavaScriptObject;)(target,LWXML_req);
		} catch (e) {}
	    LWXML_req.onreadystatechange = LWXML_partProcess;
		LWXML_req.send(body);
	}-*/;

	private static native JavaScriptObject getXMLHttpRequest() /*-{
		  var req;
		  if($wnd.XMLHttpRequest){
		    req = new XMLHttpRequest();
		  } else {
		    try{
		      req = new ActiveXObject("Msxml2.XMLHTTP");
		    } catch(e) {
		      req = new ActiveXObject("Microsoft.XMLHTTP");
		    }
		  }
		  return req;
	}-*/;

	public static native void peerAlert(JavaScriptObject peer) /*-{
		alert("Peer is ["+peer+"]");
		var output = '';
		for(var i in peer)
		  output += i+"\n";
		alert("Peer properties:\n"+output);
	}-*/;

	public static void readySignalled(final GenericCallback<String> callback, final String status) {
		callback.onSuccess( status );
	}

	private String pass = null;
	public String text;
	public String statusText;

	private String user = null;

	public JavaScriptObject xhr;

	private native JavaScriptObject _getDocumentElement(JavaScriptObject peer) /*-{
	    if(peer.documentElement) return peer.documentElement
	      else return peer;
	}-*/;

	public void delete(final String uri, final GenericCallback<String> callback) {
		xhr = getXMLHttpRequest();
		NativeDocumentImpl.fetchAndParse(xhr, uri, "DELETE", null, user, pass,
				this, callback);
	}

	public void get(final String uri, final GenericCallback<String> callback) {
		xhr = getXMLHttpRequest();
		NativeDocumentImpl.fetchAndParse(xhr, uri, "GET", null, user, pass, this,
				callback);
	}

	public void getAsText(final String uri, final GenericCallback<String> callback) {
		xhr = getXMLHttpRequest();
		NativeDocumentImpl.fetch(xhr, uri, "GET", null, user, pass, this, callback);
	}

	public NativeElement getDocumentElement() {
		JavaScriptObject jso = _getDocumentElement(peer);
		if(jso == peer) return null;
		  else return new NativeElementImpl(jso);
	}

	public HashMap<String, String> getHeaders() {
		return headers;
	}

	public String getPass() {
		return pass;
	}

	public Object getPeer() {
		return peer;
	}

	public String getStatusText() {
		return statusText;
	}

	public String getText() {
		return text;
	}

	public String getUser() {
		return user;
	}

	public void load(final String uri, final GenericCallback<String> callback) {
		xhr = getXMLHttpRequest();
		NativeDocumentImpl.fetchAndParse(xhr, uri, "GET", null, user, pass, this,
				callback);
	}

	public void parse(final String xml) {
		peer = _parse(xml);
	}

	public void performOperation(final String method, final String uri, final String body, final GenericCallback<String> callback) {
		xhr = getXMLHttpRequest();
		NativeDocumentImpl.fetchAndParse(xhr, uri, method, body, user, pass, this, callback);
	}

	public void post(final String uri, final String body,
			final GenericCallback<String> callback) {
		xhr = getXMLHttpRequest();
		NativeDocumentImpl.fetchAndParse(xhr, uri, "POST", body, user, pass, this,
				callback);
	}

	public void postAsText(final String uri, final String body,
			final GenericCallback<String> callback) {
		xhr = getXMLHttpRequest();
		NativeDocumentImpl
				.fetch(xhr, uri, "POST", body, user, pass, this, callback);
	}

	public void propfind(final String uri, final GenericCallback<String> callback) {
		xhr = getXMLHttpRequest();
		NativeDocumentImpl.fetchAndParse(xhr, uri, "PROPFIND", null, user, pass,
				this, callback);
	}

	public void proppatch(final String uri, final String body,
			final GenericCallback<String> callback) {
		xhr = getXMLHttpRequest();
		NativeDocumentImpl.fetchAndParse(xhr, uri, "PROPPATCH", body, user, pass,
				this, callback);
	}

	public void put(final String uri, final String body,
			final GenericCallback<String> callback) {
		xhr = getXMLHttpRequest();
		NativeDocumentImpl.fetchAndParse(xhr, uri, "PUT", body, user, pass, this,
				callback);
	}

	public void putAsText(final String uri, final String body,
			final GenericCallback<String> callback) {
		xhr = getXMLHttpRequest();
		NativeDocumentImpl.fetch(xhr, uri, "PUT", body, user, pass, this, callback);
	}

	/**
	 * Sets an arbitrary header for a request
	 * @param LWXML_req the request object
	 * @param key the header name
	 * @param value the header value
	 */
	private static native void _setHeader(JavaScriptObject LWXML_req, String key, String value) /*-{
		try {
			LWXML_req.setRequestHeader(key, value);
		} catch (e) { }
	}-*/;

	public void setPass(final String pass) {
		this.pass = pass;
	}

	public void setUser(final String user) {
		this.user = user;
	}

	/* Should parse fail, try this code snippet.
	 *  var xmlDoc = null;
	   	var output = "";
	   	try {
	   		xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
			for(var i in xmlDoc)
		  		output += i+"\n";
  			xmlDoc.async = "false";
  			xmlDoc.loadXML(xml);
  		} catch (e) {
  			try {
  				var parser = new DOMParser();
  				output = "";
				for(var i in xmlDoc)
		  			output += i+"\n";
  				xmlDoc = parser.parseFromString(xml, "text/xml");
  			} catch (f) { }
  		}
  		return xmlDoc;
	 *
	 */

}

