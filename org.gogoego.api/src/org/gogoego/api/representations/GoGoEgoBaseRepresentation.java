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
package org.gogoego.api.representations;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.gogoego.api.plugins.GoGoEgo;
import org.gogoego.api.scripting.ELEntity;
import org.restlet.data.MediaType;
import org.restlet.representation.StreamRepresentation;
import org.w3c.dom.Document;

import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.CSVTokenizer;
import com.solertium.util.TagFilter;
import com.solertium.util.TrivialExceptionHandler;

/**
 * GoGoEgoBaseRepresentation.java
 * 
 * Simple StreamRepresentation with a bit of intelligence that allows for 
 * caching of the entity for later processing by server-parsed scripts 
 * and the like. 
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *  href="http://www.solertium.com">Solertium Corporation</a>
 */
public abstract class GoGoEgoBaseRepresentation extends StreamRepresentation implements GoGoEgoRepresentation, ELEntity, TrapFactory {
	
	protected Document document;
	protected String contentType;

	public GoGoEgoBaseRepresentation(MediaType mediaType) {
		super(mediaType);
		contentType = mediaType.getName();
	}
	
	public String getContent() {
		try {
			return getText();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public abstract String getText() throws IOException;
	
	public abstract void setContent(String content);
		
	public String getContentType() {
		return contentType;
	}
	
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public MediaType getPreferredMediaType() {
		return getMediaType();
	}
	
	public String getContentById(final String id) {
		final StringWriter writer = new StringWriter();
		final TagFilter tf = new TagFilter(new StringReader(getContent()), writer);
		tf.fullyElide("script");
		tf.fullyElide("style");
		try {
			tf.extractInteriorOfID(id);
		} catch (final Exception e) {
			TrivialExceptionHandler.ignore(this, e);
		}
		return writer.toString();
	}
	
	public String getContentByTagName(final String name) {
		final StringWriter writer = new StringWriter();
		final TagFilter tf = new TagFilter(new StringReader(getContent()), writer);
		tf.fullyElide("script");
		tf.fullyElide("style");
		try {
			tf.extractInteriorOf(name);
		} catch (final Exception e) {
			TrivialExceptionHandler.ignore(this, e);
		}
		return writer.toString();
	}
	
	public Trap<? extends GoGoEgoBaseRepresentation> newTrap() {
		return new GoGoEgoRepresentationTrap(this);
	}
	
	public Document getDocument() {
		if (document == null)
			document = BaseDocumentUtils.impl.createDocumentFromString(getContent());
		return document;
	}
	
	public String listToCSV(List<String> list) {
		String csv = "";
		Iterator<String> iterator = list.listIterator();
		while (iterator.hasNext())
			csv += iterator.next() + (iterator.hasNext() ? "," : "");
		return csv;
	}
	
	public String listObjToCSV(List<? extends Object> list) {
		String csv = "";
		Iterator<? extends Object> iterator = list.listIterator();
		while (iterator.hasNext())
			csv += iterator.next().toString() + (iterator.hasNext() ? "," : "");
		return csv;
	}
	
	protected String reflect(String key) {
		int firstIndex = key.indexOf("(");
		int secondIndex = key.indexOf(")");

		if (firstIndex == -1 || secondIndex == -1)
			return null;

		String methodName = key.substring(0, firstIndex);
		String params = key.substring(firstIndex + 1, secondIndex);
		String[] allParams = null;
		if (firstIndex + 1 != secondIndex) {
			allParams = params.split(",");
			for (int i = 0; i < allParams.length; i++)
				allParams[i] = allParams[i].replace("\"", "").trim();
		}
		else
			allParams = new String[0];

		Object retValue = null;

		java.lang.reflect.Method[] methods = getClass().getMethods();
		for (int i = 0; i < methods.length; i++) {
			java.lang.reflect.Method current = methods[i];
			if (current.getName().equalsIgnoreCase(methodName)
					&& (current.getParameterTypes().length == allParams.length)) {
				try {
					retValue = current.invoke(this, (Object[]) allParams);
					try {
						if (current.isAnnotationPresent(Deprecated.class)) {
							GoGoEgo.debug("warning").println("The method {0} " +
								"is deprecated and may not exist in future builds " +
								"of {1}."
							, getClass().getSimpleName(), methodName);
						}
					} catch (Throwable e) {
						TrivialExceptionHandler.ignore(this, e);
					}
					break;
				} catch (Exception e) {
					e.printStackTrace();
					TrivialExceptionHandler.ignore(this, e);
				}
			}
		}
		
		return (retValue == null) ? null : retValue.toString();
	}
	
	public String resolveEL(String key) {
		int index;
		if ((index = key.indexOf(";")) != -1)
			key = key.substring(index + 1);
		
		String value = reflect(key);
		if (value != null)
			return value;

		if (key.startsWith("#"))
			return getContentById(key.substring(1));
		else
			return getContentByTagName(key);
	}
	
	public String resolveConditionalEL(String template, String keyCSV) {
		final Map<String, String> resolved = new HashMap<String, String>();
		final CSVTokenizer tokenizer = new CSVTokenizer(keyCSV);
		tokenizer.setNullOnEnd(true);
		
		String key;
		while ((key = tokenizer.nextToken()) != null) {
			String value = resolveEL(key);
			if (value == null)
				return "";
			else
				resolved.put(key, value);
		}
		
		return substitute(template, resolved.values());
	}
	
	protected final String substitute(String text, Collection<String> params) {
		if (params == null || params.isEmpty())
			return text;
		int count = 0;		
		for (String p : params) {
			text = text.replaceAll("\\[" + count++ + "]", safeRegexReplacement(p == null ? "" : p));
		}
		return text;
	}
	
	private String safeRegexReplacement(final Object replacement) {
		return replacement == null ? "null" : replacement.toString().replaceAll("\\\\", "\\\\\\\\").replaceAll("\\$",
				"\\\\\\$");
	}
	
	public String toString() {
		return getContent();
	}

}
