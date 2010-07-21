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
package com.solertium.vfs.restlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

import com.solertium.util.restlet.MediaTypeManager;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;

/**
 * VFSFileDataSource.java
 * 
 * Simple DataSource implementation for VFS files.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class VFSFileDataSource implements DataSource {
	
	private final VFSPath uri;
	private final VFS vfs;
	
	public VFSFileDataSource(VFSPath uri, VFS vfs) {
		this.uri = uri;
		this.vfs = vfs;
	}

	public String getContentType() {
		return MediaTypeManager.getMediaType(uri.toString()).getName();
	}

	public InputStream getInputStream() throws IOException {
		return vfs.getInputStream(uri);
	}

	public String getName() {
		return uri.getName();
	}

	public OutputStream getOutputStream() throws IOException {
		return vfs.getOutputStream(uri);
	}

}
