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

import org.gogoego.api.utils.DocumentUtils;
import org.w3c.dom.Document;

import com.solertium.vfs.VFS;
import com.solertium.vfs.utils.VFSUtils;

/**
 * ImmediateRegistryWriter.java
 * 
 * This writer will write all updates to file immediately.  It will 
 * work with a fresh version of the registry document each time, so 
 * changes outside the context of this writer will be picked up for 
 * each write operation.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, 
 * <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class ImmediateRegistryWriter extends DeferredRegistryWriter {
	
	public ImmediateRegistryWriter(VFS vfs, String path) {
		super(vfs, path);
	}
	
	/**
	 * This should get the freshest version of the document available 
	 * from the VFS.
	 */
	public Document getDocument() {
		Document document;
		try {
			document = DocumentUtils.getReadWriteDocument(VFSUtils.parseVFSPath(path), vfs);
		} catch (IOException e) {
			document = null;
		}
		if (document == null)
			document = DocumentUtils.impl.createDocumentFromString("<registry></registry>");

		return document;
	}
	
	/**
	 * This should write to the file immediately.
	 */
	protected boolean updateDocument(Document document) {
		return DocumentUtils.writeVFSFile(path, vfs, document);
	}
	
	/**
	 * This is essentially a no-op b/c it would just pull 
	 * the document from the file system and write it back 
	 * immediately.  Let's just skip it and return true.
	 */
	public boolean persistDocument(Document document) {
		return true;
	}

}
