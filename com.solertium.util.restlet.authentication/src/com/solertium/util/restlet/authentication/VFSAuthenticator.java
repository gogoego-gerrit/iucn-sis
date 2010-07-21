/*
 * Copyright (C) 2000-2005 Cluestream Ventures, LLC
 * Copyright (C) 2006-2009 Solertium Corporation
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
package com.solertium.util.restlet.authentication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

import com.solertium.util.SysDebugger;
import com.solertium.vfs.BoundsException;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;

/**
 * Helper class for performing authentication using a VFS.
 * 
 * @author adam.schwartz
 *
 */
public class VFSAuthenticator extends FileAuthenticator
{
	protected final VFSPath secretsFileUri;
	protected final VFS vfs;
	
	public VFSAuthenticator(VFS vfs, VFSPath secretsFileUri) {
		this(vfs, secretsFileUri, false);
	}
	
	public VFSAuthenticator(VFS vfs, VFSPath secretsFileUri, boolean formatOnLoad) {
		super();
		this.vfs = vfs;
		this.secretsFileUri = secretsFileUri;
		
		if (!vfs.exists(secretsFileUri)) {
			final VFSPath parent = secretsFileUri.getCollection();
			if (!parent.equals(VFSPath.ROOT) && !vfs.exists(parent)) {
				try {
					vfs.makeCollections(parent);
				} catch (IOException e) {
					throw new RuntimeException("Could not create directory " + parent);
				}
			}
			if (writeFile(""))
				SysDebugger.getNamedInstance("fine").println("Successfully wrote authentiation file to {0}", secretsFileUri);
			else
				throw new RuntimeException("Could not initialize authentication file!");
		}
		
		if (formatOnLoad)
			doFormat();
	}
	
	public Writer getWriter() throws IOException {
		return vfs.getWriter(secretsFileUri);
	}
	
	public BufferedReader getReader() throws IOException {
		return new BufferedReader(vfs.getReader(secretsFileUri));
	}
	
	public String getFileContents() {
		String content = null;
		try {
			content = vfs.getString(secretsFileUri);
		} catch (NotFoundException nf) {
			throw new RuntimeException("Could not find " + secretsFileUri, nf);
		} catch (BoundsException retryAsStream) {
			try {
				BufferedReader buf = getReader();
				String line = "";
				String xml = "";
				while ((line = buf.readLine()) != null)
					xml += line;
				buf.close();
				return xml;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

		} catch (IOException serious) {
			serious.printStackTrace();
		}
		
		return content;
	}
}
