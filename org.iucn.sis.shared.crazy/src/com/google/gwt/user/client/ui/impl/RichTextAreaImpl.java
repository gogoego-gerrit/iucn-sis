/*
 * Copyright 2007 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.user.client.ui.impl;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;

/**
 * Base class for RichText platform implementations. The default version simply
 * creates a text area with no rich text support.
 * 
 * This is not currently used by any user-agent, but will provide a
 * &lt;textarea&gt; fallback in the event a future browser fails to implement
 * rich text editing.
 */
public class RichTextAreaImpl {

	protected Element elem;

	public RichTextAreaImpl() {
		elem = createElement();
	}

	protected Element createElement() {
		return DOM.createTextArea();
	}

	public Element getElement() {
		return elem;
	}

	public String getHTML() {
		return DOM.getElementProperty(elem, "value");
	}

	public String getText() {
		return DOM.getElementProperty(elem, "value");
	}

	protected void hookEvents() {
		DOM.sinkEvents(elem, Event.MOUSEEVENTS | Event.KEYEVENTS | Event.ONCHANGE | Event.ONCLICK | Event.FOCUSEVENTS);
	}

	public void initElement() {
		onElementInitialized();
	}

	public boolean isBasicEditingSupported() {
		return false;
	}

	public boolean isExtendedEditingSupported() {
		return false;
	}

	protected void onElementInitialized() {
		hookEvents();
	}

	public native void setFocus(boolean focused) /*-{
	   if (focused) {
	     this.@com.google.gwt.user.client.ui.impl.RichTextAreaImpl::elem.focus();
	   } else {
	     this.@com.google.gwt.user.client.ui.impl.RichTextAreaImpl::elem.blur();
	   } 
	 }-*/;

	public void setHTML(String html) {
		DOM.setElementProperty(elem, "value", html);
	}

	public void setText(String text) {
		DOM.setElementProperty(elem, "value", text);
	}

	public void uninitElement() {
	}
}
