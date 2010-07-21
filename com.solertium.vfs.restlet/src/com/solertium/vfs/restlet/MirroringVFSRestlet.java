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

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

import com.solertium.vfs.ConflictException;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;

/**
 * Accepts incoming requests from a MirroringVFS and commits them via the VFS 
 * supplied in the constructor. Provides a very rudimentary synchronizing flag
 * that will guarantee nothing but that if two or more requests arrive concurrently,
 * only one will be given access to the underlying VFS streams at a time. This
 * does NOTHING to guarantee any sort of service order, and is only useful if
 * your underlying file system is not thread safe. 
 * 
 * @author adam.schwartz
 *
 */
public class MirroringVFSRestlet extends Restlet {
	
	private VFS vfs;
	private boolean synchronize;
	
	public MirroringVFSRestlet(Context context, VFS vfs, boolean synchronize) {
		super(context);
		this.vfs = vfs;
		this.synchronize = synchronize;
	}
	
	@Override
	public void handle(Request request, Response response) {
		super.handle(request, response);
		
		if( synchronize )
			synchronizedCommitOperation(request, response);
		else
			commitOperation(request, response);
	}

	private void commitOperation(Request request, Response response) {
		Method method = request.getMethod();
		String path = request.getResourceRef().getRemainingPart();
		
		try {
			VFSPath uri = new VFSPath(path);

			if( method == Method.DELETE ) {
				vfs.delete(uri);
			} else if( method == Method.MKCOL ) {
				vfs.makeCollections(uri);
			} else {
				if( !vfs.exists(uri) )
					vfs.makeCollections(uri);
				
				OutputStream out = vfs.getOutputStream(uri);
				InputStream in = request.getEntity().getStream();
				
				int bytesRead;
		        final byte[] buffer = new byte[4096];
		        while ((bytesRead = in.read(buffer)) > 0) {
		        	out.write(buffer, 0, bytesRead);
		        }
		        in.close();
		        out.close();
			}
			
			response.setStatus(Status.SUCCESS_OK);
		} catch (ConflictException e) {
			e.printStackTrace();
			response.setStatus(Status.CLIENT_ERROR_CONFLICT);
		} catch (NotFoundException e) {
			e.printStackTrace();
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		} catch (IOException e) {
			e.printStackTrace();
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}
	}
	
	private synchronized void synchronizedCommitOperation(Request request, Response response) {
		Method method = request.getMethod();
		String path = request.getResourceRef().getRemainingPart();
		
		try {
			VFSPath uri = new VFSPath(path);

			if( method == Method.DELETE ) {
				vfs.delete(uri);
			} else if( method == Method.MKCOL ) {
				vfs.makeCollections(uri);
			} else {
				if( !vfs.exists(uri) )
					vfs.makeCollections(uri);
				
				OutputStream out = vfs.getOutputStream(uri);
				InputStream in = request.getEntity().getStream();
				
				int bytesRead;
		        final byte[] buffer = new byte[4096];
		        while ((bytesRead = in.read(buffer)) > 0) {
		        	out.write(buffer, 0, bytesRead);
		        }
		        in.close();
		        out.close();
			}
			
			response.setStatus(Status.SUCCESS_OK);
		} catch (ConflictException e) {
			e.printStackTrace();
			response.setStatus(Status.CLIENT_ERROR_CONFLICT);
		} catch (NotFoundException e) {
			e.printStackTrace();
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		} catch (IOException e) {
			e.printStackTrace();
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}
	}
	
}
