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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.restlet.data.Encoding;
import org.restlet.representation.Representation;

public class HashMapRepresentationCache implements RepresentationCache {
	
	private HashMap<String, CacheableRepresentation> reps;
	private List<String> extensionsToEncode;
	
	public HashMapRepresentationCache() {
		this(new ArrayList<String>());
	}
	
	public HashMapRepresentationCache(List<String> extensionsToEncode) {
		reps = new HashMap<String, CacheableRepresentation>();
		setFileExtensionsToEncode(extensionsToEncode);
	}
	
	public boolean contains(String uri) {
		return reps.containsKey(uri);
	}

	public CacheableRepresentation getCachedEntity(String uri) {
		return reps.get(uri);
	}

	public List<String> getFileExtensionsToEncode() {
		return extensionsToEncode;
	}

	public void putCacheableEntity(String uri, CacheableRepresentation representation) {
		reps.put(uri, representation);
	}
	
	public CacheableRepresentation putUnwrappedEntity(Encoding encoding, String uri, Representation representation) throws IOException {
		if( contains(uri) )
			return getCachedEntity(uri);
		
		CacheableRepresentation cRepresentation = new CacheableRepresentation(encoding, representation);
		reps.put(uri, cRepresentation);
		
		return cRepresentation;
	}
	
	public CacheableRepresentation putUnwrappedEntity(String extension, Encoding encoding, String uri, Representation representation) throws IOException {
		if( contains(uri) )
			return getCachedEntity(uri);
		
		if( shouldEncode(extension) )
			reps.put(uri, new CacheableRepresentation(encoding, representation));
		else
			reps.put(uri, new CacheableRepresentation(representation));
		
		return reps.get(uri);
	}

	public void setFileExtensionsToEncode(List<String> extensionsToEncode) {
		this.extensionsToEncode = extensionsToEncode;
	}

	public boolean shouldEncode(String extension) {
		return extensionsToEncode.contains(extension);
	}
	
	public CacheableRepresentation evict(String uri) {
		return reps.remove(uri);
	}

}
