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

package com.solertium.gogoego.server.lib.templates;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.gogoego.api.applications.TemplateDataAPI;
import org.gogoego.api.representations.GoGoEgoBaseRepresentation;
import org.gogoego.api.representations.GoGoEgoStringRepresentation;
import org.restlet.Application;
import org.restlet.Context;

import com.solertium.gogoego.server.ServerApplication;
import com.solertium.util.BaseTagListener;
import com.solertium.util.PageMetadata;
import com.solertium.util.TagFilter;
import com.solertium.util.TagFilter.Tag;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.utils.VFSUtils;

/**
 * TemplateData.java
 * 
 * Implementation of template data api that uses templates 
 * in a given location on the file system.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class TemplateData implements TemplateDataAPI {

	private String contentType;
	private String displayName;
	private String uri;

	private String allowBaseURI = null;
	
	private String cachedRepresentation = null;
	private Date modificationDate = null;

	// private String allowBaseMimeType = null;

	public TemplateData(final Reader reader, final String uri) throws IOException {
		this.uri = uri;

		final PageMetadata pm = new PageMetadata(reader);
		if (pm.getMeta().containsKey("contentType"))
			contentType = pm.getMeta().get("contentType");

		if (pm.getMeta().containsKey("allow-base-uri"))
			allowBaseURI = pm.getMeta().get("allow-base-uri");
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final TemplateData other = (TemplateData) obj;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}

	public String getContentType() {
		return contentType;
	}

	public String getDisplayName() {
		return displayName;
	}
	
	public GoGoEgoBaseRepresentation getRepresentation() {
		if (cachedRepresentation == null) {
			final Context context = Application.getCurrent().getContext();
			final VFS vfs = ServerApplication.getFromContext(context).getVFS();
			final VFSPath path = new VFSPath(uri);
			try {
				cachedRepresentation = vfs.getString(path);
			} catch (IOException e) {
				cachedRepresentation = "Error trying to fetch template at " + uri + ": " + e.getMessage();
			}
			
			try {
				modificationDate = new Date(vfs.getLastModified(path));
			} catch (IOException e) {
				modificationDate = new Date();
			}
		}
		final GoGoEgoStringRepresentation rep = 
			new GoGoEgoStringRepresentation(cachedRepresentation);
		rep.setModificationDate(modificationDate);
		return rep;
	}

	public String getUri() {
		return uri;
	}

	public boolean isAllowed(String uri) {
		return allowBaseURI == null || uri.matches(allowBaseURI);
	}

	@Override
	public int hashCode() {
		return uri.hashCode();
	}

	public void invalidate() {
		cachedRepresentation = null;
	}
	
	public void setContentType(final String contentType) {
		this.contentType = contentType;
	}

	public void setDisplayName(final String name) {
		displayName = name;
	}

	public void setUri(final String uri) {
		this.uri = uri;
	}

	public String toString() {
		return displayName;
	}

	public boolean updateTemplateAttributes(VFS vfs, final HashMap<String, String> attributes)
			throws IndexOutOfBoundsException {
		String docAsString;
		try {
			docAsString = vfs.getString(VFSUtils.parseVFSPath(uri));
		} catch (IOException e) {
			docAsString = null;
		}
		if (docAsString == null)
			return false;

		if (docAsString.indexOf("<head") == -1)
			throw new IndexOutOfBoundsException("Head element not found");

		final StringWriter writer = new StringWriter();
		TagFilter tf = null;
		try {
			tf = new TagFilter(new BufferedReader(new StringReader(docAsString)), writer);
		} catch (Exception e) {
			return false;
		}

		final ArrayList<String> found = new ArrayList<String>();

		tf.shortCircuitClosingTags = false;
		tf.registerListener(new BaseTagListener() {
			public List<String> interestingTagNames() {
				final ArrayList<String> list = new ArrayList<String>();
				list.add("meta");
				return list;
			}

			public void process(final Tag tag) throws IOException {
				Iterator<String> iterator = attributes.keySet().iterator();
				while (iterator.hasNext()) {
					String current = iterator.next();
					if (tag.getAttribute("name").equals(current)) {
						String content = attributes.get(current);
						if (content != null) {
							tag.setAttribute("content", content);
							tag.rewrite();
							found.add(current);
						}
					}
				}
			}
		});
		try {
			tf.parse();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		if (attributes.containsKey("contentType"))
			setContentType(attributes.get("contentType"));

		final String out;
		if (found.containsAll(attributes.keySet()))
			out = writer.toString();
		else {
			for (int i = 0; i < found.size(); i++)
				attributes.remove(found.get(i));

			int index = docAsString.indexOf("<head");
			index += "<head>".length();

			String prefix = docAsString.substring(0, index);
			String suffix = docAsString.substring(index + 1);

			StringBuilder newMetaTags = new StringBuilder();

			Iterator<Map.Entry<String, String>> iterator = attributes.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, String> current = iterator.next();
				newMetaTags.append("<meta name=\"" + current.getKey() + "\" content=\"" + current.getValue()
						+ "\" />\r\n");
			}

			out = prefix + "\r\n" + newMetaTags.toString() + suffix;
		}

		try {
			vfs.getOutputStream(VFSUtils.parseVFSPath(uri)).write(out.getBytes());
			cachedRepresentation = null;
			modificationDate = null;
			return true;
		} catch (Exception e) {
			return false;
		}

	}

}
