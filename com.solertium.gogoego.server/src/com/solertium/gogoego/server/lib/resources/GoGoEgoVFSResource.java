/*
 * Copyright (C) 2007-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * GoGoEgo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * GoGoEgo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GoGoEgo.  If not, see http://www.gnu.org/licenses/.
 * 
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, only the GNU General Public License
 * grants you rights to modify or redistribute this code.
 */
package com.solertium.gogoego.server.lib.resources;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;

import org.gogoego.api.plugins.GoGoEgo;
import org.gogoego.api.representations.GoGoEgoBaseRepresentation;
import org.gogoego.api.representations.GoGoEgoBytesRepresentation;
import org.gogoego.api.representations.GoGoEgoInputRepresentation;
import org.gogoego.api.representations.GoGoEgoRepresentation;
import org.gogoego.api.representations.GoGoEgoStringRepresentation;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Tag;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import com.solertium.util.BoundedHashMap;
import com.solertium.util.restlet.MediaTypeManager;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.restlet.VFSResource;

/**
 * GoGoEgoVFSResource.java
 * 
 * Returns GoGoEgoRepresentations for VFS resources
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class GoGoEgoVFSResource extends VFSResource {
	
	public GoGoEgoVFSResource(final Context context, final Request request, final Response response) {
		super(context, request, response);
	}
	
	protected Representation getRepresentationForFile() throws NotFoundException {
		GoGoEgoBaseRepresentation r = null;
		final String s = uri.toString();
		final MediaType mediaType = MediaTypeManager.getMediaType(s);
		
		//Attempt to use string representation for text-based resources
		if (MediaType.TEXT_ALL.includes(mediaType)) {
			try {
				r = new GoGoEgoStringRepresentation(
					vfs.getString(uri), mediaType);
			} catch (IOException io) {
				GoGoEgo.debug("fine").println("Could not represent {0} " +
					"as a string, attempting input representation instead", s);
				r = null;
			}
		}
		if (r == null) {
			r = getCachedRepresentationForFile(vfs, uri, mediaType);
		}
		r.setSize(vfs.getLength(uri));
		r.setModificationDate(new Date(vfs.getLastModified(uri)));
		r.setTag(new Tag(vfs.getETag(uri)));
		return r;
	}

	/**
	 * Use representation cache for static assets where possible
	 */
	public static GoGoEgoBaseRepresentation getCachedRepresentationForFile(VFS vfs, VFSPath uri, MediaType mediaType) throws NotFoundException {
		String s = uri.toString();
		// first, do I have a cache?
		Context curr = Context.getCurrent();
		Map<String,GoGoEgoBytesRepresentation> representationCache = (Map<String,GoGoEgoBytesRepresentation>) curr.getAttributes().get("representation.cache");
		if(representationCache == null){
			// Double check locking; I don't think this is the broken version
			synchronized(curr){
				representationCache = (Map<String,GoGoEgoBytesRepresentation>) curr.getAttributes().get("representation.cache");
				if(representationCache == null){
					representationCache = new BoundedHashMap<String,GoGoEgoBytesRepresentation>(64);
					curr.getAttributes().put("representation.cache",representationCache);
				}
			}
		}
		// do I have this in cache?
		GoGoEgoBytesRepresentation r = representationCache.get(s);
		if(r!=null){
			// yes, is it stale?
			if(r.getModificationDate().getTime()<vfs.getLastModified(uri)){
				// yes
				representationCache.remove(s);
				r = null;
			} else {
				// return it
				// System.out.println("Returning cached representation for "+s);
				GoGoEgoBytesRepresentation cached = new GoGoEgoBytesRepresentation(r.getBytes(), r.getMediaType());
				cached.setSize(r.getSize());
				cached.setModificationDate(r.getModificationDate());
				cached.setTag(r.getTag());
				return cached;
			}
		}
		// is the representation eligible to be captured in cache?
		long l = vfs.getLength(uri);
		if(l<65535){ // less than 64K
			InputStream is = vfs.getInputStream(uri);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try{
				GoGoEgoBytesRepresentation.copyStream(is,baos);
			} catch (IOException iox) {
				throw new RuntimeException(iox);
			} finally {
				try{
					is.close();
				} catch (IOException unlikely) {
					unlikely.printStackTrace();
				}
			}
			
			r = new GoGoEgoBytesRepresentation(baos.toByteArray(),MediaTypeManager.getMediaType(s));
			r.setSize(vfs.getLength(uri));
			r.setModificationDate(new Date(vfs.getLastModified(uri)));
			r.setTag(new Tag(vfs.getETag(uri)));
			representationCache.put(s, r);
			return r;
		} else {
			// old school processing
			return new GoGoEgoInputRepresentation(vfs.getInputStream(uri),mediaType);
		}
	}


}