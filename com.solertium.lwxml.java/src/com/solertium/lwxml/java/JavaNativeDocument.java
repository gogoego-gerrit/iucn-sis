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

package com.solertium.lwxml.java;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilderFactory;

import org.restlet.Client;
import org.restlet.Uniform;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.GWTConflictException;
import com.solertium.lwxml.shared.GWTForbiddenException;
import com.solertium.lwxml.shared.GWTNotFoundException;
import com.solertium.lwxml.shared.GWTUnauthorizedException;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;

/**
 * This is a compatibility library which allows use of the GWT NativeDocument
 * utility classes in a non-GWT environment.  If you need this library in
 * production, your code may need refactoring ...
 *
 * @author rob.heittman@solertium.com
 */
public class JavaNativeDocument extends JavaNativeNode implements NativeDocument {

	private HashMap<String, String> customHeaders;
	/**
	 * Default constructor
	 *
	 */
	public JavaNativeDocument() {
		super();
		customHeaders = new HashMap<String, String>();
		
	}

	/**
	 * Creates a new document based on the peer object of a node.
	 * @param node
	 */
	public JavaNativeDocument(JavaNativeNode node) {
		super(node.peer);
		customHeaders = new HashMap<String, String>();
	}

	// Headers sent
	// NativeDocument.setHeader(SIS_AM_req, "If-Modified-Since", "Sat, 1 Jan 2000 00:00:00 GMT");
	// NativeDocument.setHeader(SIS_AM_req, "Cache-Control", "no-cache");

	public static void errorSignalled(final String errorMessage,
			final GenericCallback<?> callback) {
		Exception e;
		try{
			int code = Integer.parseInt(errorMessage);
			if(code==404) e = new GWTNotFoundException(errorMessage);
			else if(code==403) e = new GWTForbiddenException(errorMessage);
			else if(code==401) e = new GWTUnauthorizedException(errorMessage);
			else if(code==409) e = new GWTConflictException(errorMessage);
			else e = new Exception(errorMessage);
		} catch (Exception defaultHandling) {
			e = new Exception(errorMessage);
		}
		callback.onFailure(e);
	}

	private static Response _fetch(String uriFragment,
			String method, String body, String username, String password, JavaNativeDocument target){
		Request request = new Request();
		if("GET".equals(method)) request.setMethod(Method.GET);
		else if("PUT".equals(method)) request.setMethod(Method.PUT);
		else if("POST".equals(method)) request.setMethod(Method.POST);
		else if("DELETE".equals(method)) request.setMethod(Method.DELETE);
		else if("PROPFIND".equals(method)) request.setMethod(Method.PROPFIND);
		else if("PROPPATCH".equals(method)) request.setMethod(Method.PROPPATCH);
		// custom header stuff for closer GWT NativeDocument emulation
		// setCustomHeader(request,"If-Modified-Since", "Sat, 1 Jan 2000 00:00:00 GMT");
		//setCustomHeader(request,"Cache-Control", "no-cache");
		
		
		
		Form headers = (Form) request.getAttributes().get("org.restlet.http.headers");
		if (headers == null) {
			headers = new Form();
			request.getAttributes().put("org.restlet.http.headers", headers);
		}
		for(String key: target.customHeaders.keySet())
			headers.add(key, target.customHeaders.get(key));
		
		if(body!=null)
		{
			request.setEntity(new StringRepresentation(body));
			request.getEntity().setCharacterSet( CharacterSet.UTF_8 );
		}
		if(username!=null && password!=null){
			request.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_BASIC,username,password));
		}
		Uniform c = null;
		if(uriFragment.startsWith("http://")) {
			c = new Client(Protocol.HTTP);
			request.setResourceRef(uriFragment);
		} else if(uriFragment.startsWith("/")) {
			request.setResourceRef("riap://component"+uriFragment);
		} else {
			request.setResourceRef(uriFragment);
		}
		if (Protocol.RIAP.equals(request.getProtocol()))
		{
			c = ((JavaNativeDocumentFactory) NativeDocumentFactory.getDefaultInstance())
			.getLocalRequestHandler();
		}
		if(c==null) throw new RuntimeException("No client could be selected for "+uriFragment);
		Response response = new Response(request);
		c.handle(request,response);
		return response;
	}

	public static void fetch(String uriFragment,
			String method, String body, String username, String password,
			JavaNativeDocument target, GenericCallback<String> callback) {
		Response res = _fetch(uriFragment, method,body,username,password, target);
		if(res.getStatus().isSuccess()){
			try{
				Representation rep = res.getEntity();
				if( rep != null )
					target.text = res.getEntity().getText();
				else
					target.text = "";

				target.statusText = ""+res.getStatus().getCode();
			} catch (IOException io) {
				io.printStackTrace();
				errorSignalled("IOException fetching entity",callback);
			}
			readySignalled(callback, ""+res.getStatus().getCode());
		} else {
			errorSignalled(""+res.getStatus().getCode(),callback);
		}
	}

	public static void fetchAndParse(String uriFragment, String method, String body, String username,
			String password, JavaNativeDocument target, GenericCallback<String> callback){
		Response res = _fetch(uriFragment,method,body,username,password, target);
		if(res.getStatus().isSuccess()){
			try{
				if( res.getStatus() != Status.SUCCESS_NO_CONTENT ) {
					Representation rep = res.getEntity();
					DomRepresentation dr = null;
					if( rep != null ) {
						dr = new DomRepresentation(rep);
						target.peer = dr.getDocument();
					} else
						target.peer = null;
					
					target.text = dr.getText();
				} else
					target.peer = null;

				target.statusText = ""+res.getStatus().getCode();
			} catch (IOException io) {
				errorSignalled("IOException fetching entity",callback);
			}
			readySignalled(callback, ""+res.getStatus().getCode());
		} else {
			errorSignalled(""+res.getStatus().getCode(),callback);
		}
	}

	public static void readySignalled(final GenericCallback<String> callback, final String status) {
		callback.onSuccess( status );
	}

	private String pass = null;
	public String text;
	public String statusText;

	private String user = null;

	public void delete(final String uri, final GenericCallback<String> callback) {
		JavaNativeDocument.fetchAndParse(uri, "DELETE", null, user, pass,
				this, callback);
	}

	public void get(final String uri, final GenericCallback<String> callback) {
		JavaNativeDocument.fetchAndParse(uri, "GET", null, user, pass, this,
				callback);
	}

	public void getAsText(final String uri, final GenericCallback<String> callback) {
		JavaNativeDocument.fetch(uri, "GET", null, user, pass, this, callback);
	}

	public NativeElement getDocumentElement() {
		return new JavaNativeElement(((Document)peer).getDocumentElement());
	}

	public String getPass() {
		return pass;
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
		JavaNativeDocument.fetchAndParse(uri, "GET", null, user, pass, this,
				callback);
	}

	public void parse(final String xml) {
		try{
			peer = DocumentBuilderFactory.newInstance().newDocumentBuilder()
			.parse(new InputSource(new StringReader(xml)));
		} catch (Exception any) {
			any.printStackTrace();
		}
	}

	public void performOperation(final String method, final String uri, final String body, final GenericCallback<String> callback) {
		JavaNativeDocument.fetchAndParse(uri, method, body, user, pass, this, callback);
	}

	public void post(final String uri, final String body,
			final GenericCallback<String> callback) {
		JavaNativeDocument.fetchAndParse(uri, "POST", body, user, pass, this,
				callback);
	}

	public void postAsText(final String uri, final String body,
			final GenericCallback<String> callback) {
		JavaNativeDocument
		.fetch(uri, "POST", body, user, pass, this, callback);
	}

	public void propfind(final String uri, final GenericCallback<String> callback) {
		JavaNativeDocument.fetchAndParse(uri, "PROPFIND", null, user, pass,
				this, callback);
	}

	public void proppatch(final String uri, final String body,
			final GenericCallback<String> callback) {
		JavaNativeDocument.fetchAndParse(uri, "PROPPATCH", body, user, pass,
				this, callback);
	}

	public void put(final String uri, final String body,
			final GenericCallback<String> callback) {
		JavaNativeDocument.fetchAndParse(uri, "PUT", body, user, pass, this,
				callback);
	}

	public void putAsText(final String uri, final String body,
			final GenericCallback<String> callback) {
		JavaNativeDocument.fetch(uri, "PUT", body, user, pass, this, callback);
	}

	public void setPass(final String pass) {
		this.pass = pass;
	}

	public void setUser(final String user) {
		this.user = user;
	}

	/**
	 * NOT IMPLEMENTED
	 */
	public HashMap<String,String> getHeaders() {
		// TODO Not implemented in Java version
		return null;
	}

	/**
	 * NOT IMPLEMENTED
	 */
	public String getAllResponseHeaders() {
		return customHeaders.toString();
	}

	/**
	 * NOT IMPLEMENTED
	 */
	public HashMap<String,String> getResponseHeaders() {
		return customHeaders;
	}

	public Object getPeer() {
		return peer;
	}

	public void setHeader(String key, String value) {
		customHeaders.put(key, value);
	}

}
