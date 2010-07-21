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
package com.solertium.gogoego.server.lib.app.tags.utils;

import java.util.ArrayList;
import java.util.List;

import org.gogoego.api.utils.DocumentUtils;
import org.restlet.Context;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.gogoego.server.ServerApplication;
import com.solertium.gogoego.server.lib.app.tags.container.TagApplication;
import com.solertium.gogoego.server.lib.app.tags.resources.TagResource;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;

/**
 * TagApplicationDataUtility.java
 * 
 * @author carl.scott
 * 
 */
public class TagApplicationDataUtility {

	private final ExecutionContext ec;
	private final String siteID;

	public TagApplicationDataUtility(Context context) throws InstantiationException {
		if (!(ServerApplication.getFromContext(context)).isApplicationInstalled(TagApplication.REGISTRATION))
			throw new InstantiationException(TagApplication.REGISTRATION + " is not installed.");

		ec = ((TagApplication)ServerApplication.getFromContext(context, TagApplication.REGISTRATION)).getExecutionContext();
		siteID = ((TagApplication)ServerApplication.getFromContext(context, TagApplication.REGISTRATION)).getSiteID();
	}

	public Document getTagsForDirectory(VFSPath directory, VFS vfs, boolean recursive) {
		return getTagsForDirectory(directory, vfs, null, recursive);
	}

	public Document getTagsForDirectory(VFSPath directory, VFS vfs, Long date, boolean recursive) {
		Document document = DocumentUtils.impl.newDocument();
		document.appendChild(document.createElement("root"));

		buildDocumentFromDirectory(directory, vfs, date, document, recursive);

		return document;
	}

	public List<String> getTagListForFile(VFSPath uri, VFS vfs) {
		final ArrayList<String> tagList = new ArrayList<String>() {
			private static final long serialVersionUID = 1L;

			public boolean add(String e) {
				return !contains(e) && super.add(e);
			}
		};

		final List<Row> list;
		try {
			list = TagResource.getTagsForURI(uri, siteID, ec);
		} catch (DBException e) {
			// System.err.print("Error fetching tags: " + e.getMessage());
			e.printStackTrace();
			return null;
		}

		for (Row row : list) {
			tagList.add(row.get("name").toString());
		}

		return tagList;
	}

	public Document getTagsForFile(VFSPath uri, VFS vfs) {
		Document document = DocumentUtils.impl.newDocument();
		document.appendChild(document.createElement("root"));

		final List<Row> list;
		try {
			list = TagResource.getTagsForURI(uri, siteID, ec);
		} catch (DBException e) {
			System.err.print("Error fetching tags: " + e.getMessage());
			return null;
		}

		final Element file = document.createElement("file");
		file.appendChild(DocumentUtils.impl.createElementWithText(document, "name", uri.getName()));
		file.appendChild(DocumentUtils.impl.createElementWithText(document, "uri", uri.toString()));

		for (Row row : list) {
			Element element = DocumentUtils.impl.createElementWithText(document, "tag", row.get("name").toString());
			file.appendChild(element);
		}

		document.getDocumentElement().appendChild(file);

		return document;
	}

	private void buildDocumentFromDirectory(VFSPath directory, VFS vfs, Long date, Document document, boolean recursive) {
		final VFSPathToken[] files;
		try {
			files = vfs.list(directory);
		} catch (NotFoundException unlikely) {
			TrivialExceptionHandler.ignore(this, unlikely);
			return;
		}

		for (VFSPathToken token : files) {
			final VFSPath uri = directory.child(token);

			boolean isCollection = false;
			try {
				isCollection = vfs.isCollection(uri);
			} catch (NotFoundException e) {
				TrivialExceptionHandler.ignore(this, e);
			}

			if (isCollection) {
				if (recursive)
					buildDocumentFromDirectory(uri, vfs, date, document, recursive);
			} else {
				final List<Row> list;
				try {
					list = TagResource.getTagsForURI(uri, siteID, date, ec);
				} catch (DBException e) {
					System.err.print("Error fetching tags: " + e.getMessage());
					continue;
				}

				final Element file = document.createElement("file");
				file.setAttribute("name", token.toString());
				file.setAttribute("uri", uri.toString());

				for (Row row : list) {
					Element element = DocumentUtils.impl.createElementWithText(document, "tag", row.get("name")
							.toString());
					file.appendChild(element);
				}

				document.getDocumentElement().appendChild(file);
			}
		}
	}
}
