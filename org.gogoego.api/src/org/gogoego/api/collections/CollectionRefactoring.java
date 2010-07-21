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
package org.gogoego.api.collections;

import java.io.IOException;
import java.util.HashMap;

import org.gogoego.api.plugins.GoGoEgo;
import org.gogoego.api.utils.DocumentUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.solertium.util.ElementCollection;
import com.solertium.util.NodeCollection;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;

/**
 * CollectionRefactoring.java
 * 
 * When a view changes, appropriate field types in the collections 
 * used by that view may need to be updated.  This will accomplish 
 * this task.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *  href="http://www.solertium.com">Solertium Corporation</a>
 */
public class CollectionRefactoring implements Runnable {

	private final String view;
	private final VFS vfs;

	public CollectionRefactoring(String view, VFS vfs) {
		this.view = view;
		this.vfs = vfs;
	}

	public void run() {
		parse(new ViewMap(view, vfs), vfs, new VFSPath("/collections"));
	}

	private void parse(ViewMap view, VFS vfs, VFSPath path) {
		final Document document;
		try {
			document = vfs.getDocument(path.child(new VFSPathToken(Constants.COLLECTION_ROOT_FILENAME)));
		} catch (IOException e) {
			TrivialExceptionHandler.ignore(this, e);
			return;
		}

		final NodeCollection nodes = new NodeCollection(document.getDocumentElement().getChildNodes());

		/*
		 * Refactor items if necessary
		 */
		if (view.view.equals(document.getDocumentElement().getAttribute("type"))) {
			for (Node node : nodes)
				if ("item".equals(node.getNodeName()))
					refactor(view, vfs, new VFSPath(DocumentUtils.impl.getAttribute(node, "uri") + ".xml"));
		}

		/*
		 * Go to child collections
		 */
		for (Node node : nodes)
			if ("subcollection".equals(node.getNodeName()))
				parse(view, vfs, new VFSPath(DocumentUtils.impl.getAttribute(node, "uri")));
	}

	private void refactor(ViewMap view, VFS vfs, VFSPath item) {
		final Document document;
		try {
			document = vfs.getMutableDocument(item);
		} catch (IOException e) {
			TrivialExceptionHandler.ignore(this, e);
			return;
		}

		boolean hasChanges = false;
		final NodeList nodes = document.getDocumentElement().getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			final Node current = nodes.item(i);
			if ("custom".equals(current.getNodeName())) {
				String name = DocumentUtils.impl.getAttribute(current, "name");
				String type = DocumentUtils.impl.getAttribute(current, "type");
				String definedType = view.get(name);
				if (!"".equals(name) && definedType != null && !definedType.equals(type)) {
					GoGoEgo.debug("fine").println(
							"Found out-of-date item type for {0}: {1} is now {2} for field {3}", item, type,
							definedType, name);
					((Element) current).setAttribute(name, definedType);
					hasChanges = true;
				}
			}
		}

		if (hasChanges) {
			GoGoEgo.debug("fine").println("Refactoring " + "found item {0} as out of date, saving...", item);
			DocumentUtils.writeVFSFile(item.toString(), vfs, document);
		} else
			GoGoEgo.debug("fine").println("No changes found for {0}", item);
	}

	private static class ViewMap extends HashMap<String, String> {

		private static final long serialVersionUID = 1L;
		private final String view;

		public ViewMap(String view, VFS vfs) {
			super();
			this.view = view;
			try {
				parse(vfs.getDocument(new VFSPath("/(SYSTEM)/views/" + view + ".xml")));
			} catch (IOException e) {
				TrivialExceptionHandler.ignore(this, e);
			}
		}

		private void parse(Document document) {
			final ElementCollection nodes = new ElementCollection(document.getDocumentElement().getElementsByTagName(
					"field"));
			for (Element node : nodes)
				put(node.getAttribute("name"), node.getAttribute("type"));
		}
	}
}
