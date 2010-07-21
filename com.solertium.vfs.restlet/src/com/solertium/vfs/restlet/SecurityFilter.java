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
package com.solertium.vfs.restlet;

import java.util.ArrayList;
import java.util.Iterator;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.routing.Filter;

import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSMetadata;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;
import com.solertium.vfs.utils.VFSUtils;

/**
 * SecurityFilter.java
 * 
 * Checks the VFS Metadata for a particular uri to determine what, if any 
 * securty conditions apply
 * 
 * @author carl.scott
 *
 */
public class SecurityFilter extends Filter {
	
	public static final String SECURITY_FILTER = "com.solertium.restletfoundation.util.SecurityFilter";
	
	protected VFS vfs;
	protected ArrayList<String> warnings;
	
	/**
	 * Constructs a new filter.  The application should implement VFSProvidingApplication
	 * @param context
	 */
	public SecurityFilter(Context context) {
		super(context);
		this.vfs = ((VFSProvidingApplication) context.getAttributes().get(VFSProvidingApplication.INITIALIZING_KEY)).getVFS();
		warnings = new ArrayList<String>();
		warnings.add(VFSMetadata.SECURE_REJECT_ALL);
		warnings.add(VFSMetadata.SECURE_REJECT_PUBLIC);
		warnings.add(VFSMetadata.PASSWORD_PROTECTED);
	}
	
	public int beforeHandle(Request request, Response response) {
		String path = request.getResourceRef().getRemainingPart();
		int index = path.indexOf("?");
		if (index != -1)
			path = path.substring(0, index);
		
		final VFSPathToken[] split;
		try {
			split = VFSUtils.parseVFSPath(path).getTokens();
		} catch (Exception e) {
			//Let this be handled more smartly later...
			return Filter.CONTINUE;
		}
		
		VFSPath cur = VFSPath.ROOT;
		for (int i = 0; i < split.length; i++) {
			cur = cur.child(split[i]);
			VFSMetadata md = vfs.getMetadata(cur);
			
			//TODO: we may want to just pass back the securityProperties with the SC file
			//and let an implementing class handle it
			try {
				if (!validate(md)) {
					SecurityConditions sc = new SecurityConditions(path, md);
					sc.setFailurePath(cur.toString());
					sc.setError(new ArrayList<String>(md.getSecurityProperties().keySet()));
					
					response.getAttributes().put(SECURITY_FILTER, sc);
					
					if (isInsecure(sc, request, response) == Filter.STOP)
						return Filter.STOP;
				}
			} catch (NullPointerException e) {
				e.printStackTrace();
				TrivialExceptionHandler.ignore(this, e);
			}
		}
		return Filter.CONTINUE;
	}
	
	/**
	 * Returns true of the security properties don't contain any warning, false otherwise
	 * @param metadata
	 * @return
	 */
	protected boolean validate(VFSMetadata metadata) {
		boolean success = true;
		Iterator<String> it = metadata.getSecurityProperties().keySet().iterator();		
		while (it.hasNext() && (success = !warnings.contains(it.next())));
		return success;
	}
	
	/**
	 * When an insecure path is found, this function interrogates it, and tells the 
	 * filter whether or not to continue.
	 * @param sc
	 * @param request
	 * @param response
	 * @return
	 */
	public int isInsecure(SecurityConditions sc, Request request, Response response) {
		response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
		return Filter.STOP;
	}

	protected static class SecurityConditions {
		
		private String path, failurePath;
		private ArrayList<String> errors;
		private VFSMetadata metadata;
		
		public SecurityConditions(String path, VFSMetadata metadata) {
			this.path = path;
			this.metadata = metadata;
			errors = new ArrayList<String>();
		}
		
		public VFSMetadata getMetadata() {
			return metadata;
		}
		
		public void setError(ArrayList<String> errors) {
			this.errors = errors;
		}
		
		public ArrayList<String> getErrors() {
			return errors;
		}
		
		public boolean isValid() {
			return errors.isEmpty();
		}
		
		public String getPath() {
			return path;
		}
		
		public String getFailurePath() {
			return failurePath;
		}
		
		public void setFailurePath(String failurePath) {
			this.failurePath = failurePath;
		}
	}
}
