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

import java.util.HashMap;

import org.w3c.dom.Document;

import com.solertium.vfs.VFS;

public abstract class RegistryWriter {
	
	protected final VFS vfs;
	protected final String path;
	
	public RegistryWriter(VFS vfs, String path) {
		this.vfs = vfs;
		this.path = path;
	}
	
	/**
	 * Get a copy of the registry document
	 * @return
	 */
	public abstract Document getDocument();
	
	/**
	 * Remove an object from the registry with the given ID for 
	 * the given protocol
	 * @param id
	 * @param protocol
	 * @return
	 */
	public abstract boolean removeFromRegistry(final String id, final String protocol);
	
	/**
	 * Update the name and uri for the object from the registry with 
	 * the given ID for the given protocol  
	 * @param id
	 * @param uri
	 * @param name
	 * @param protocol
	 * @return
	 */
	public abstract boolean updateRegistry(final String id, final String uri, final String name, final String protocol);

	/**
	 * Update simple metadata for the object from the registry with 
	 * the given ID for the given protocol.
	 * @param id
	 * @param protocol
	 * @param data
	 * @return
	 */
	public abstract boolean updateRegistryData(final String id, final String protocol, final HashMap<String, String> data);
	
	/**
	 * Update the document with changes for a single object
	 * @param document
	 * @return
	 */
	protected abstract boolean updateDocument(final Document document);
	
	/**
	 * Write the entire registry as a whole
	 * @param document
	 * @return
	 */
	public abstract boolean persistDocument(final Document document);
	
}