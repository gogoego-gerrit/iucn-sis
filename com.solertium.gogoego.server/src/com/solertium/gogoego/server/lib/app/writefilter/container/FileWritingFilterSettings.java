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
package com.solertium.gogoego.server.lib.app.writefilter.container;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.gogoego.api.utils.DocumentUtils;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.solertium.gogoego.server.cm.PluginAgent;
import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.NodeCollection;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.util.portable.XMLWritingUtils;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;

public class FileWritingFilterSettings {
	
	public static final VFSPath SETTINGS_FILE = new VFSPath("/(SYSTEM)/filewritingfilter/settings.xml");
	
	private final List<String> filterKeys;
	private final VFS vfs;
	
	public FileWritingFilterSettings(VFS vfs) {
		this.vfs = vfs;
		
		filterKeys = new ArrayList<String>();
		
		Document document = null;
		try {
			document = vfs.getDocument(SETTINGS_FILE);
		} catch (IOException e) {
			TrivialExceptionHandler.ignore(this, e);
		}
		
		if (document != null && document.getDocumentElement() != null) {
			final NodeCollection nodes = new NodeCollection(document.getDocumentElement().getChildNodes());
			for (Node node : nodes) {
				if ("filter".equals(node.getNodeName()))
					filterKeys.add(node.getTextContent());
			}
		}
	}
	
	public void addFilter(String id) throws ResourceException {
		if (PluginAgent.getFileWritingFilterBroker().getPlugin(id) == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, id + " is not a valid filter ID");
		
		if (!filterKeys.contains(id))
			filterKeys.add(id);
	}
	
	public void removeFilter(String id) {
		filterKeys.remove(id);
	}
	
	public List<String> getFilterKeys() {
		return filterKeys;
	}
	
	public boolean save() {
		return DocumentUtils.impl.writeVFSFile(SETTINGS_FILE.toString(), vfs, BaseDocumentUtils.impl.createDocumentFromString(toXML()));
	}
	
	public String toXML() {
		final StringBuilder builder = new StringBuilder();
		builder.append("<root>");
		
		for (String id : filterKeys)
			builder.append(XMLWritingUtils.writeCDATATag("filter", id));
		
		builder.append("</root>");
		return builder.toString();
	}

}
