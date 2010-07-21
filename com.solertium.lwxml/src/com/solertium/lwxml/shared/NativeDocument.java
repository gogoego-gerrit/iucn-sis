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

package com.solertium.lwxml.shared;

import java.util.HashMap;


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
public interface NativeDocument extends NativeNode {

	public void delete(final String uri, final GenericCallback<String> callback);

	public void get(final String uri, final GenericCallback<String> callback);

	public void getAsText(final String uri, final GenericCallback<String> callback);

	public NativeElement getDocumentElement();

	public HashMap<String, String> getHeaders();

	public HashMap<String, String> getResponseHeaders();

	public String getAllResponseHeaders();

	public Object getPeer();

	public String getPass();

	public String getStatusText();

	public String getText();

	public String getUser();

	public void load(final String uri, final GenericCallback<String> callback);

	public void parse(final String xml);

	public void performOperation(final String method, final String uri, final String body, final GenericCallback<String> callback);

	public void post(final String uri, final String body, final GenericCallback<String> callback);

	public void postAsText(final String uri, final String body, final GenericCallback<String> callback);

	public void propfind(final String uri, final GenericCallback<String> callback);

	public void proppatch(final String uri, final String body, final GenericCallback<String> callback);

	public void put(final String uri, final String body, final GenericCallback<String> callback);

	public void putAsText(final String uri, final String body, final GenericCallback<String> callback);

	public void setHeader(final String key, final String value);

	public void setPass(final String pass);

	public void setUser(final String user);

}
