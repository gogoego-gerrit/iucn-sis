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
package com.solertium.gogoego.server.lib.representations;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.gogoego.api.collections.Constants;
import org.gogoego.api.representations.GoGoEgoBaseRepresentation;
import org.gogoego.api.representations.GoGoEgoDomRepresentation;
import org.gogoego.api.representations.GoGoEgoRepresentation;
import org.gogoego.api.representations.HasURI;
import org.gogoego.api.representations.TouchListener;
import org.gogoego.api.representations.Trap;
import org.gogoego.api.representations.TrapFactory;
import org.gogoego.api.scripting.ScriptedPages;
import org.gogoego.api.utils.LastModifiedDisablingFilter;
import org.gogoego.api.utils.MagicDisablingFilter;
import org.restlet.data.CharacterSet;
import org.restlet.data.CookieSetting;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import com.solertium.gogoego.server.DynamicContext;
import com.solertium.gogoego.server.GoGoDebug;
import com.solertium.gogoego.server.GoGoMagicFilter;
import com.solertium.gogoego.server.ScriptTagListener;
import com.solertium.gogoego.server.ViewFilter;
import com.solertium.gogoego.server.lib.resources.PageTreeNode;
import com.solertium.util.BaseTagListener;
import com.solertium.util.Replacer;
import com.solertium.util.TagFilter;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.util.TagFilter.Tag;
import com.solertium.util.restlet.CookieUtility;

/**
 * RepresentationUtils.java
 * 
 * Utilities for updating representations and processing 
 * dynamic content.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class RepresentationUtils {
	
	// Maximum number of rounds to recurse EL resolution for this Representation
	private final static int MAX_EL_RECURSION = 4;
	
	public static String processDynamic(final GoGoEgoRepresentation template, final TrapFactory base, final DynamicContext dc) {
		final String content = template.getContent();
		final Collection<GoGoEgoBaseRepresentation> objects = new HashSet<GoGoEgoBaseRepresentation>();
		if (base != null) {
			Trap trap = base.newTrap();
			trap.addTouchListener(new TouchListener() {
				public void touched(Object representation) {
					if (representation instanceof GoGoEgoBaseRepresentation && 
						representation instanceof HasURI)
						objects.add((GoGoEgoBaseRepresentation)representation);
				}
			});
			dc.put("base", trap);
		}
		final StringWriter writer = new StringWriter(content.length());
		final TagFilter scriptFilter = new TagFilter(new StringReader(content), writer);
		scriptFilter.fullyElide("script");
		scriptFilter.fullyElide("style");
		scriptFilter.shortCircuitClosingTags = false;
		scriptFilter.registerListener(new ScriptTagListener(dc));

		String result;
		try {
			scriptFilter.parse();
			result = writer.toString();
		} catch (final IOException x) {
			x.printStackTrace();
			result = content;
		}
		
		int triesRemaining = MAX_EL_RECURSION;
		while(result.contains("${") && triesRemaining>0){
			triesRemaining--;
			result = replace(result, dc, true, "");
		}
		
		final Collection<PageTreeNode> nodes = new ArrayList<PageTreeNode>();
		for (GoGoEgoBaseRepresentation rep : objects) {
			Date modified = rep.getModificationDate();
			if (modified == null)
				modified = new Date();
			long lm = modified.getTime();
			nodes.add(new PageTreeNode(((HasURI)rep).getURI(), rep.getContentType(), "scripted", lm));
		}
		
		for (ScriptedPages scripted : dc.getScriptedPages()) {
			nodes.add(new PageTreeNode(scripted.getURI(), scripted.getContentType(), "scripted", scripted.getLastModifiedDate()));
		}
		
		dc.updatePageTree(nodes);

		return result;
	}
	
	public static String replace(final String buf, final DynamicContext mapping, final boolean hungry, final String hungrySubst) {
		// short circuit eliminates NPEs here
		if ((buf == null) || (mapping == null))
			return buf;
		int found;
		int rhs;
		int last = 0;
		String key;
		// guess at a reasonable new string buffer size
		final StringBuffer newsb = new StringBuffer(buf.length() + 128);
		do {
			found = buf.indexOf("${", last);
			if (found > -1) {
				newsb.append(buf.substring(last, found));
				rhs = buf.indexOf('}', found + 2);
				last = rhs + 1;
				key = buf.substring(found + 2, rhs);
				final Object value = mapping.resolveEL(key);
				if (value != null)
					newsb.append(value.toString());
				else if (hungry == false)
					newsb.append(buf.substring(found, rhs + 1));
				else
					newsb.append(hungrySubst);
			}
		} while (found > -1);
		newsb.append(buf.substring(last));
		return newsb.toString();
	}
	
	public static String listToCSV(List<String> list) {
		String csv = "";
		Iterator<String> iterator = list.listIterator();
		while (iterator.hasNext())
			csv += iterator.next() + (iterator.hasNext() ? "," : "");
		return csv;
	}
	
	public static String listObjToCSV(List<? extends Object> list) {
		String csv = "";
		Iterator<? extends Object> iterator = list.listIterator();
		while (iterator.hasNext())
			csv += iterator.next().toString() + (iterator.hasNext() ? "," : "");
		return csv;
	}
	
	public static String reflect(String key, Class<?> clazz) {
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

		Object retValue = null;

		java.lang.reflect.Method[] methods = clazz.getMethods();
		for (int i = 0; i < methods.length; i++) {
			java.lang.reflect.Method current = methods[i];
			if (current.getName().equalsIgnoreCase(methodName)
					&& ((current.getParameterTypes().length == 0 && allParams == null) || current.getParameterTypes().length == allParams.length)) {
				try {
					retValue = current.invoke(clazz, (Object[]) allParams);
					break;
				} catch (Exception e) {
					e.printStackTrace();
					TrivialExceptionHandler.ignore(clazz, e);
				}
			}
		}
		
		return (retValue == null) ? null : retValue.toString();
	}
	
	public static void processCharSetAndExpiration(Request request, Response response) {
		final Representation finalRepresentation = response.getEntity();
		if (finalRepresentation != null) {
			final MediaType mt = finalRepresentation.getMediaType();
			if (mt != null) {
				if (mt.equals(MediaType.TEXT_HTML) || mt.equals(MediaType.TEXT_PLAIN) || mt.equals(MediaType.TEXT_XML))
					finalRepresentation.setCharacterSet(CharacterSet.UTF_8);
			}

			/*
			 * Last-modified and expiration stuff
			 */
			if (!request.getAttributes().containsKey(LastModifiedDisablingFilter.LAST_MODIFIED_DISABLING_KEY)) {
				final PageTreeNode node = 
					(PageTreeNode)request.getAttributes().get(ViewFilter.PAGE_TREE);
				if (node != null)
					finalRepresentation.setModificationDate(new Date(node.getLastModified()));
			}
			else
				GoGoDebug.get("fine").println("No last modified date sent with this response");
			if (finalRepresentation.getExpirationDate() == null
					&& !response.getAttributes().containsKey(Constants.DISABLE_EXPIRATION)) {
				final Calendar cal = Calendar.getInstance();
				if (mt.equals(MediaType.TEXT_HTML) || mt.equals(MediaType.TEXT_PLAIN) || mt.equals(MediaType.TEXT_XML))
					cal.add(Calendar.HOUR, 24);
				else
					cal.add(Calendar.MONTH, 2);
				finalRepresentation.setExpirationDate(cal.getTime());
			} else {
				GoGoDebug.get("fine").println("No expiry sent with this response");
			}
			if(response.getAttributes().containsKey(Constants.DISABLE_EXPIRATION)){
				// Great!  Do not send last-modified either.
				finalRepresentation.setModificationDate(null);
			}
		}
	}
	
	public static void processFinalEntity(final Request request, final Response response) {
		boolean hasMagic = (request.getAttributes().get(MagicDisablingFilter.MAGIC_DISABLING_KEY) == null);
		// request
		CookieUtility.associateHTTPBrowserID(request, response);
		List<CookieSetting> cookieSettings = GoGoMagicFilter.getCookies();
		if (cookieSettings != null) {
			for (CookieSetting cookieSetting : cookieSettings)
				response.getCookieSettings().add(cookieSetting);
			GoGoMagicFilter.removeCookies();
		}

		if (request.getAttributes().containsKey(ViewFilter.SHOW_TREE)) {
			PageTreeNode node = 
				(PageTreeNode)request.getAttributes().get(ViewFilter.PAGE_TREE);
			if (node == null)
				response.setEntity(new StringRepresentation(
					"Page tree could not be built for " + request.getResourceRef()
				));
			else {
				GoGoEgoDomRepresentation entity = new GoGoEgoDomRepresentation(node.toXML());
				entity.setModificationDate(new Date(node.getLastModified()));
				
				response.setEntity(entity);
			}
		}
		else if (hasMagic && response.getEntity() != null) {
			// none of this applies for null entities or when magic is off
			final Representation entity = response.getEntity();
			if (MediaType.TEXT_HTML.equals(entity.getMediaType())) {
				// and only for HTML
				try {
					String content = unescapeEL(response.getEntity().getText());
					if (request.getAttributes().get(Constants.EDITMODE) != null)
						content = insertEditingMarkup(content, request.getResourceRef().getRemainingPart());
					else if (request.getAttributes().get(ViewFilter.SURFMODE) != null)
						content = insertSurfingMarkup(content);
					final Representation rep = new StringRepresentation(content, response.getEntity()
							.getMediaType());
					rep.setModificationDate(response.getEntity().getModificationDate());
					response.setEntity(rep);
				} catch (IOException e) {
					response.setStatus(Status.SERVER_ERROR_INTERNAL);
				}
			}
			else
				response.setEntity(entity);
		}
	}
	
	private static String unescapeEL(String content) {
		if (content != null) {
			if (content.indexOf("\\$\\{") > -1)
				content = Replacer.replace(content, "\\$\\{", "${");
			if (content.indexOf("\\\\$\\\\{") > -1)
				content = Replacer.replace(content, "\\\\$\\\\{", "\\$\\{");
		}
		return content;
	}

	/**
	 * Adds in GoGoEgo editing markup to the content. Puts the edit.js reference
	 * in the head, and adds gogo_initEdit() to the body's onload tag. This
	 * brings up the references tags in /admin/editmode.
	 * 
	 * @param fullContent
	 *            the full content
	 * @return the fullContent plus the script references.
	 */
	private static String insertEditingMarkup(final String content, final String uri) {
		final StringWriter writer = new StringWriter();
		final TagFilter tf = new TagFilter(new StringReader(content), writer);
		tf.shortCircuitClosingTags = false;
		tf.registerListener(new BaseTagListener() {
			public List<String> interestingTagNames() {
				final ArrayList<String> list = new ArrayList<String>();
				list.add("/head");
				list.add("gogo:wysiwyg");
				list.add("body");
				return list;
			}

			public void process(final Tag tag) throws IOException {
				if (tag.name.equalsIgnoreCase("/head"))
					parent.write("<script type=\"text/javascript\" src=\"/admin/js/edit.js\"></script>");
				else if (tag.name.equalsIgnoreCase("body")) {
					final String onload = tag.getAttribute("onload");
					tag.setAttribute("onload", onload == null ? "gogo_initEdit()" : onload + ";gogo_initEdit()");
					tag.rewrite();
				}
				else if ("gogo:wysiwyg".equals(tag.name)) {
					if (!tag.attr.containsKey("src")) {
						tag.setAttribute("src", uri);
						tag.rewrite();
					}
				}
			}
		});
		try {
			tf.parse();
			return writer.toString();
		} catch (final Exception e) {
			return content;
		}
	}

	private static String insertSurfingMarkup(final String content) {
		final StringWriter writer = new StringWriter();
		final TagFilter tf = new TagFilter(new StringReader(content), writer);
		tf.shortCircuitClosingTags = false;
		tf.registerListener(new BaseTagListener() {
			public List<String> interestingTagNames() {
				final ArrayList<String> list = new ArrayList<String>();
				list.add("/head");
				list.add("body");
				list.add("a");
				return list;
			}

			public void process(final Tag tag) throws IOException {
				if (tag.name.equalsIgnoreCase("/head"))
					parent.write("<script type=\"text/javascript\" src=\"/admin/js/surf.js\"></script>");
				else if (tag.name.equalsIgnoreCase("body")) {
					final String onload = tag.getAttribute("onload");
					tag.setAttribute("onload", onload == null ? "addGoGoEventListeners()" : onload
							+ ";addGoGoEventListeners()");
					tag.rewrite();
				} else if (tag.name.equalsIgnoreCase("a")) {
					final String href = tag.getAttribute("href");
					if (href != null) {
						tag.setAttribute("href", href + (href.indexOf("?") == -1 ? "?" : "&") + "ggeviewmode="
								+ ViewFilter.SURFMODE);
						tag.rewrite();
					}
					// Disallow popup windows
					final String target = tag.getAttribute("target");
					if (target != null && target.toUpperCase().equals("_BLANK")) {
						tag.attr.remove("target");
						tag.rewrite();
					}
				}
			}
		});
		try {
			tf.parse();
			return writer.toString();
		} catch (final Exception e) {
			return content;
		}
	}
	
}
