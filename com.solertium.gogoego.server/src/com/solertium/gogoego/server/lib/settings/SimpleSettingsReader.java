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
package com.solertium.gogoego.server.lib.settings;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.gogoego.api.applications.ServerApplicationAPI;
import org.gogoego.api.plugins.GoGoEgo;
import org.restlet.Application;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.solertium.util.ElementCollection;
import com.solertium.vfs.VFSPath;

public abstract class SimpleSettingsReader {
	
	private final ServerApplicationAPI api;
	private Map<String, String> fields;
	
	public SimpleSettingsReader() {
		this(GoGoEgo.get().getFromContext(Application.getCurrent().getContext()));
	}
	
	public SimpleSettingsReader(final ServerApplicationAPI api) {
		this.api = api;
		this.fields = new HashMap<String, String>();
	}
	
	public void load(final VFSPath path) throws IOException {
		fields.clear();
		
		load(api.getVFS().getDocument(path));
	}
	
	public void load(final Document document) {
		final ElementCollection nodes = new ElementCollection(
			document.getDocumentElement().getElementsByTagName("field")
		);
		for (Element el : nodes)
			fields.put(el.getAttribute("name"), el.getTextContent());
	}
	
	public String getField(String name) {
		return fields.get(name);
	}
	
	public String getField(String name, String defaultValue) {
		return fields.get(name) != null ? fields.get(name) : defaultValue;
	}
	
	private boolean isEmpty(String name) {
		return fields.get(name) == null || "".equals(fields.get(name));
	}

}
