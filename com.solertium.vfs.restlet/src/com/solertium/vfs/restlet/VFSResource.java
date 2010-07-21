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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;

import net.jcip.annotations.NotThreadSafe;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.ReferenceList;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;

import com.solertium.util.restlet.MediaTypeManager;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;
import com.solertium.vfs.utils.VFSUtils;
import com.solertium.vfs.utils.VFSUtils.VFSPathParseException;

@NotThreadSafe
public class VFSResource extends Resource {

	protected final Reference ref;
	protected final VFSPath uri;
	protected final VFS vfs;
	protected final boolean exists;

	public final static boolean ENTITY_HACKS = ("true".equalsIgnoreCase(System.getProperty("ENTITY_HACKS"))
			|| "true".equalsIgnoreCase(System.getProperty("HOSTED_MODE")));
	
	public final static String IS_COLLECTION = "com.solertium.vfs.restlet.isCollection";
	public final static String IS_ROOT = "com.solertium.vfs.restlet.isRoot";
	
	public static VFSPath decodeVFSPath(String encodedUri) throws VFSPathParseException {
		int qindex = encodedUri.indexOf("?");
		if (qindex != -1)
			encodedUri = encodedUri.substring(0, qindex);
		try {
			encodedUri = URLDecoder.decode(encodedUri, "UTF-8");
		    return VFSUtils.parseVFSPath(encodedUri);
		} catch (IllegalArgumentException ix) {
			throw new VFSPathParseException(ix);
		} catch (UnsupportedEncodingException ux) {
			throw new RuntimeException("Expected UTF-8 encoding not found in Java runtime");
		}
	}
	
	public VFSResource(final Context context, final Request request,
			final Response response) {
		super(context, request, response);

		vfs = getVFS(context);
		
		//Get Remaining part includes the query portion of the request
		String internal_uri = request.getResourceRef().getRemainingPart();
		VFSPath uri;
		try {
			uri = decodeVFSPath(internal_uri);
			/*if (request.getMethod().equals(Method.GET) && 
				decodeVFSPath(request.getResourceRef().getPath()).equals(VFSPath.ROOT))
					uri = new VFSPath("/index.html");*/
		} catch (VFSPathParseException x) {
			throw new RuntimeException(
				"The URI "+internal_uri+" could not be used to access the VFS");
		}
		
		this.uri = uri;
		this.ref = new Reference(request.getResourceRef().toString());
		System.out.println("looking for vfs resource " + uri);
		//uri = URLDecoder.decode(uri, "UTF-8");

		exists = vfs.exists(uri);
		System.out.println("the vfs exists is " + exists);
		if(!exists){
			getVariants().add(new Variant(MediaType.ALL));
			return;
		}
		
		try {
			if (vfs.isCollection(uri)) {
				response.getAttributes().put(IS_COLLECTION, Boolean.TRUE);
				getVariants().add(new Variant(MediaType.TEXT_URI_LIST));
				getVariants().add(new Variant(MediaType.TEXT_PLAIN));
			} else {
				getVariants().add(
						new Variant(MediaTypeManager.getMediaType(uri.toString())));
				if (!getVariants().contains(MediaType.APPLICATION_OCTET_STREAM))
					getVariants().add(
							new Variant(MediaType.APPLICATION_OCTET_STREAM));
				getVariants().add(new Variant(MediaType.TEXT_URI_LIST));
			}
		} catch (final NotFoundException nf) {
			throw new RuntimeException(
					"A VFS resource reported as existing could not be found.");
		}
	}

	@Override
	public boolean allowPost(){
		return true;
	}
	
	protected VFS getVFS(final Context context) {
		VFSProvidingApplication va = (VFSProvidingApplication) context.getAttributes().get(VFSProvidingApplication.INITIALIZING_KEY);
		if(va==null) va = (VFSProvidingApplication) Application.getCurrent();
		return va.getVFS();
	}
	
	@Override
	public void handlePost(){
		handleGet();
	}
	
	@Override
	public Representation represent(final Variant variant) throws ResourceException {
		if(!exists) throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		try {
			if (vfs.isCollection(uri)) {
				if (!ref.toString().endsWith("/"))
					ref.addSegment("");
				final VFSPathToken[] vl = vfs.list(uri);
				final ReferenceList rl = new ReferenceList(vl.length);
				for (final VFSPathToken t : vfs.list(uri))
					rl.add(new Reference(ref, t.toString()).getTargetRef());
				final Representation result = rl.getTextRepresentation();
				if (!variant.getMediaType().equals(MediaType.TEXT_URI_LIST))
					result.setMediaType(MediaType.TEXT_PLAIN);
				return result;
			} else { // normal file handling
				// special case for when Directory requests a TEXT_URI_LIST
				if (variant.getMediaType().equals(MediaType.TEXT_URI_LIST)) {
					final ReferenceList rl = new ReferenceList(0);
					rl.add(ref);
					return rl.getTextRepresentation();
				}
				Representation r = getRepresentationForFile();
				return r;
			}
		} catch (final NotFoundException nf) {
			throw new RuntimeException(
					"A VFS resource reported as existing could not be found.");
		}
	}
	
	protected Representation getRepresentationForFile() throws NotFoundException {
		return getRepresentationForFile(vfs, uri);
	}
	
	/**
	 * On Windows platform, work around excessive locking of files in read
	 * mode by using string parses where size allows for it and the type is
	 * known to be textual.
	 */
	public static Representation getRepresentationForFile(VFS vfs, VFSPath uri) throws NotFoundException {
		Representation r = null;
		String s = uri.toString();
		if("\\".equals(File.pathSeparator) &&
				(s.endsWith(".xml")||s.endsWith(".html")||s.endsWith(".txt"))){
			try{
				r = new StringRepresentation(vfs
						.getString(uri), MediaTypeManager
						.getMediaType(s));
			} catch (IOException io) {
				io.printStackTrace();
				r = null;
			}
		}
		if(r==null) {
			r = new InputRepresentation(vfs
				.getInputStream(uri), MediaTypeManager
				.getMediaType(s));
		}
		r.setSize(vfs.getLength(uri));
		r.setModificationDate(new Date(vfs.getLastModified(uri)));
		r.setTag(new Tag(vfs.getETag(uri)));
		return r;
	}
}
