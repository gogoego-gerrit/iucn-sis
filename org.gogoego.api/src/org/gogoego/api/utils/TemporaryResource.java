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
package org.gogoego.api.utils;

import java.io.IOException;
import java.util.Calendar;

import org.w3c.dom.Document;

import com.solertium.util.MD5Hash;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSMetadata;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;
import com.solertium.vfs.utils.VFSUtils;

/**
 * TemporaryResource.java
 * 
 * Creates a temporary resource in the tmp folder.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class TemporaryResource {

	private static final VFSPath BASE_URL = new VFSPath("/tmp");

	/**
	 * Creates a temporary resource from a file on the VFS, set it as generated
	 * and unversioned in the metadata, sets its expiration to a day.
	 * 
	 * @param uri
	 * @param vfs
	 * @param document
	 * @return
	 */
	public static String create(String uri, VFS vfs, Document document) {
		if (uri == null || uri.equals(""))
			return null;

		final VFSPath path;
		try {
			path = VFSUtils.parseVFSPath(uri);
		} catch (Exception e) {
			return null;
		}

		if (!vfs.exists(path))
			return null;

		final VFSPath newURI = translatePath(path);

		try {
			if (!vfs.exists(newURI)) {
				vfs.copy(path, newURI);
				VFSMetadata md = new VFSMetadata();
				md.setGenerated(true);
				md.setVersioned(false);
				Calendar c = Calendar.getInstance();
				c.add(Calendar.DATE, 1);
				md.setExpires(c.getTimeInMillis());

				vfs.setMetadata(newURI, md);
			}
		} catch (Exception e) {
			return null;
		}

		return newURI.toString();
	}

	public static VFSPath translate(String uri) {
		try {
			return translatePath(VFSUtils.parseVFSPath(uri));
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Translates a normal URI to a "temporary" uri. It's just a hash, but it
	 * shows that this resource is NOT to be dependable.
	 * 
	 * @param uri
	 * @return
	 */
	public static VFSPath translatePath(VFSPath uri) {
		VFSPath newURI = BASE_URL;
		VFSPathToken[] split = uri.getTokens();
		for (int i = 0; i < split.length; i++) {
			newURI = newURI.child(new VFSPathToken(new MD5Hash(split[i].toString()).toString()));
		}

		return newURI;
	}

	/**
	 * Gets a temporary resource given a raw uri. It translates it, then looks
	 * for the temporary file.
	 * 
	 * @param uri
	 * @param vfs
	 * @return
	 */
	public static Document get(String uri, VFS vfs) {
		try {
			return DocumentUtils.getReadWriteDocument(translate(uri), vfs);
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes a temporary file to disk
	 * 
	 * @param uri
	 * @param vfs
	 * @return
	 */
	public static boolean write(String uri, Document doc, VFS vfs) {
		return DocumentUtils.writeVFSFile(translate(uri).toString(), vfs, doc);
	}

	/**
	 * Explicitly deletes a temporary resource.
	 * 
	 * @param uri
	 * @param vfs
	 * @return
	 */
	public static boolean delete(String uri, VFS vfs) {
		try {
			vfs.delete(translate(uri));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
