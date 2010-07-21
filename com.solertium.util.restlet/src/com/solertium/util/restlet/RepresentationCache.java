/*******************************************************************************
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
 *     http://www.gnu.org/licenses
 ******************************************************************************/
package com.solertium.util.restlet;

import java.io.IOException;
import java.util.List;

import org.restlet.data.Encoding;
import org.restlet.representation.Representation;

public interface RepresentationCache {

	public List<String> getFileExtensionsToEncode();
	
	public void setFileExtensionsToEncode(List<String> extensionsToEncode);
	
	public boolean contains(String uri);
	
	public CacheableRepresentation getCachedEntity(String uri);
	
	public void putCacheableEntity(String uri, CacheableRepresentation representation);
	
	/**
	 * Adds an encoded version of the representation to the cache. If encoding type is null,
	 * representation will not be encoded.
	 *  
	 * @param encoding - Restlet encoding type, or null for no encoding
	 * @param uri - requested uri of the file
	 * @param representation - representation to wrap and cache
	 * @return CachedRepresentation
	 * 
	 * @throws IOException, might occur during encoding
	 */
	public CacheableRepresentation putUnwrappedEntity(Encoding encoding, String uri, Representation representation) throws IOException;
	
	/**
	 * Supplying the extension to this method will cause the Cache to perform encoding checks.
	 * If the extension is not found in the List of file extensions to encode, a non-encoded
	 * version will be cached. 
	 *  
	 * @param extension - extension of the file, without '.' prefix
	 * @param encoding - Restlet encoding type
	 * @param uri - requested uri of the file
	 * @param representation - representation to wrap and cache
	 * @return CachedRepresentation
	 * 
	 * @throws IOException, might occur during encoding
	 */
	public CacheableRepresentation putUnwrappedEntity(String extension, Encoding encoding, String uri, Representation representation) throws IOException;
	
	public boolean shouldEncode(String extension);

	public CacheableRepresentation evict(String uri);
}
