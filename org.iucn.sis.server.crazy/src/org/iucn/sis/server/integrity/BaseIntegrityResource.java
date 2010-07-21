package org.iucn.sis.server.integrity;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

import com.solertium.db.restlet.DBResource;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.restlet.VFSProvidingApplication;

/**
 * BaseIntegrityResource.java
 * 
 * Base resource that loads the VFS and the integrity VFS root
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 * 
 */
public abstract class BaseIntegrityResource extends DBResource {

	protected final static VFSPath ROOT_PATH = new VFSPath("/integrity/rulesets");
	protected final VFS vfs;

	public BaseIntegrityResource(Context context, Request request,
			Response response) {
		super(context, request, response);

		vfs = ((VFSProvidingApplication) Application.getCurrent()).getVFS();
	}

}
