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

import net.jcip.annotations.NotThreadSafe;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import com.solertium.vfs.ConflictException;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFSPath;

@NotThreadSafe
public class WritableVFSResource extends VFSResource {

	private static final int BUFFER_SIZE = 65536;

	public static void copyStream(final InputStream is, final OutputStream os)
			throws IOException {
		final byte[] buf = new byte[BUFFER_SIZE];
		int i = 0;
		while ((i = is.read(buf)) != -1)
			os.write(buf, 0, i);
	}

	public WritableVFSResource(final Context context, final Request request,
			final Response response) {
		super(context, request, response);
	}

	@Override
	public boolean allowDelete() {
		return true;
	}

	@Override
	public boolean allowPut() {
		return true;
	}

	public void handlePropfind() {
	}

	public void handleProppatch() {
	}
	
	public void handlePut() {
		try {
			storeRepresentation(getRequest().getEntity());
		} catch (ResourceException e) {
			getResponse().setStatus(e.getStatus());
		}
	}

	@Override
	public void removeRepresentations() {
		try {
			vfs.delete(uri);
		} catch (final NotFoundException nf) {
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		} catch (final ConflictException conflict) {
			getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT);
		}
	}

	@Override
	public void storeRepresentation(final Representation entity) throws ResourceException {
		try {
			if (vfs.exists(uri))
				writeFile(entity);
			else {
				final VFSPath parent = uri.getCollection();
				if (VFSPath.ROOT.equals(parent) || vfs.exists(parent))
					writeFile(entity);
				
				else {
					try {
						vfs.makeCollections(parent);
					} catch (IOException e) {
						throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not create directories.");
					}
					writeFile(entity);
				}
			}
					
			/*//if (!vfs.isCollection(uri)) {
				final OutputStream os = vfs.getOutputStream(uri);
				try {
					final InputStream is = entity.getStream();
					WritableVFSResource.copyStream(is, os);
					is.close();
					os.close();
				} catch (final IOException exception) {
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, exception);
				}
			//} else
			//	getResponse().setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);*/
		} catch (final NotFoundException nf) {
			nf.printStackTrace();
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, nf);
		} catch (final ConflictException conflict) {
			throw new ResourceException(Status.CLIENT_ERROR_CONFLICT, conflict);
		} catch (final Exception part) {
			throw new ResourceException(Status.SERVER_ERROR_INSUFFICIENT_STORAGE, part);
		}
	}
	
	private void writeFile(final Representation entity) throws ResourceException, IOException {
		final OutputStream os = vfs.getOutputStream(uri);
		try {
			final InputStream is = entity.getStream();
			WritableVFSResource.copyStream(is, os);
			is.close();
			os.close();
		} catch (final IOException exception) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, exception);
		}
	}
}
