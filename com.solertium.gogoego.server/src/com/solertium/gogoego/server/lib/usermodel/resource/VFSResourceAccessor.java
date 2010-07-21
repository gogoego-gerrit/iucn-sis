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

package com.solertium.gogoego.server.lib.usermodel.resource;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import com.solertium.vfs.VFS;
import com.solertium.vfs.utils.VFSUtils;

public class VFSResourceAccessor extends ResourceAccessValidator {

	VFS vfs = null;

	public VFSResourceAccessor(final VFS vfs) {
		this.vfs = vfs;
	}

	/**
	 * This version of fetchResource only works correctly with URIResources!!
	 */
	protected Document fetchResource(final Resource resource) {

		final URIResource uriResource = (URIResource) resource;

		try {
			return vfs.getDocument(VFSUtils.parseVFSPath(uriResource.getURI()));
		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * This version of putResource only works correctly with URIResources!!
	 */
	protected boolean putResource(final Resource resource, final Object obj) {

		final URIResource uriResource = (URIResource) resource;

		try {
			final TransformerFactory tfac = TransformerFactory.newInstance();
			final Transformer t = tfac.newTransformer();
			t.transform(new DOMSource((Document) obj), new StreamResult(vfs.getOutputStream(VFSUtils
					.parseVFSPath(uriResource.getURI()))));
			return true;
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}
