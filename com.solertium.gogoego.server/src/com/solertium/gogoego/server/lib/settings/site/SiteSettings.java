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
package com.solertium.gogoego.server.lib.settings.site;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.gogoego.api.utils.DocumentUtils;
import org.restlet.data.MediaType;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.w3c.dom.Document;

import com.solertium.gogoego.server.lib.settings.base.SimpleSettingsWorker;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;

/**
 * SiteSettings.java
 * 
 * Simple Settings implementation that handles fetching and writing files
 * related to site settings.
 * 
 * @author carl.scott
 * 
 */
public final class SiteSettings extends SimpleSettingsWorker {

	private static final VFSPath SITE_SETTINGS_ROOT = new VFSPath("/(SYSTEM)/settings/site");
	private final ArrayList<String> valid;

	public SiteSettings(VFS vfs) {
		super(vfs);
		valid = new ArrayList<String>();
		valid.add("general");

	}

	public Representation getAuthority(String key) throws SimpleSettingsWorkerException {
		InputStream stream = getClass().getResourceAsStream(key + ".xml");
		if (stream == null)
			throw new SimpleSettingsWorkerException(key + " not found.");
		return new InputRepresentation(stream, MediaType.TEXT_XML);
	}

	public Representation getData(String key) throws SimpleSettingsWorkerException {
		if (!isValid(key))
			throw new SimpleSettingsWorkerException("Invalid key " + key);

		try {
			return new DomRepresentation(MediaType.TEXT_XML, DocumentUtils.getReadOnlyDocument(SITE_SETTINGS_ROOT
					.child(new VFSPathToken(key + ".xml")), vfs));
		} catch (IOException e) {
			throw new SimpleSettingsWorkerException(key + " not found", e);
		}
	}

	public final Representation getInit() throws SimpleSettingsWorkerException {
		InputStream stream = getClass().getResourceAsStream("site.xml");
		if (stream == null)
			throw new SimpleSettingsWorkerException("Init not found.");
		return new InputRepresentation(stream, MediaType.TEXT_XML);
	}

	public void setData(String key, Document document) throws SimpleSettingsWorkerException {
		if (!isValid(key))
			throw new SimpleSettingsWorkerException("Invalid key " + key);

		if (!DocumentUtils.writeVFSFile(SITE_SETTINGS_ROOT.child(new VFSPathToken(key + ".xml")).toString(), vfs,
				document))
			throw new SimpleSettingsWorkerException("Could not save " + key);
	}

	public boolean isValid(String key) {
		return valid.contains(key);
	}

}
