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
package com.solertium.gogoego.server.lib.app.exporter.workers.gge;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.gogoego.api.utils.DocumentUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.solertium.gogoego.server.lib.app.exporter.utils.ExporterConstants;
import com.solertium.util.NodeCollection;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;

/**
 * GGEPastExportData.java
 * 
 * 
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *  href="http://www.solertium.com">Solertium Corporation</a>
 */
public class GGEPastExportData {

	private static final VFSPath uri = new VFSPath(ExporterConstants.CONFIG_DIR + "/gge/lastExport.dat");

	private final HashMap<VFSPath, Date> lastExport;
	private final Date lastExportDate;
	private final VFS vfs;

	public GGEPastExportData(VFS vfs) {
		this.vfs = vfs;
		lastExport = new HashMap<VFSPath, Date>();
		lastExportDate = new Date();
		lastExportDate.setTime(0);
		load();
	}

	public Date getLastExportDate() {
		return lastExportDate;
	}

	public Date getLastExportDate(VFSPath path) {
		return (lastExport.containsKey(path)) ? 
			lastExport.get(path) : new Date(0);
	}

	public void setLastExportDate(VFSPath path, Date date) {
		if (date == null)
			lastExport.remove(path);
		else
			lastExport.put(path, date);
	}

	public void setLastExportDate(Date date) {
		lastExportDate.setTime(date.getTime());
	}

	public Collection<VFSPath> getResourcePaths() {
		return lastExport.keySet();
	}

	public void load() {
		Document document;
		try {
			document = vfs.getDocument(uri);
		} catch (IOException e) {
			document = null;
		}

		if (document != null) {
			final NodeCollection nodes = new NodeCollection(document.getDocumentElement().getChildNodes());

			for (Node node : nodes) {
				if (node.getNodeName().equals("file")) {
					final VFSPath path = new VFSPath(DocumentUtils.impl.getAttribute(node, "uri"));
					final Date date = new Date(Long.parseLong(DocumentUtils.impl.getAttribute(node, "lastExport")));

					lastExport.put(path, date);
				} else if (node.getNodeName().equals("lastExport")) {
					lastExportDate.setTime(Long.parseLong(node.getTextContent()));
				}
			}
		}
	}

	public boolean save() {
		final Document document = DocumentUtils.impl.newDocument();
		final Element root = document.createElement("root");

		root.appendChild(DocumentUtils.impl
				.createElementWithText(document, "lastExport", lastExportDate.getTime() + ""));

		final Iterator<VFSPath> iterator = lastExport.keySet().iterator();
		while (iterator.hasNext()) {
			final VFSPath path = iterator.next();

			final Element newChild = document.createElement("file");
			newChild.setAttribute("uri", path.toString());
			newChild.setAttribute("lastExport", getLastExportDate(path).getTime() + "");

			root.appendChild(newChild);
		}

		document.appendChild(root);

		return DocumentUtils.writeVFSFile(uri.toString(), vfs, document);
	}

}
