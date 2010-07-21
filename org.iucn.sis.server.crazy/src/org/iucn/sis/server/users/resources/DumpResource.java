package org.iucn.sis.server.users.resources;

import org.iucn.sis.server.users.container.UserManagementApplication;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;

import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.query.SelectQuery;
import com.solertium.db.utils.QueryUtils;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.restlet.VFSResource;
import com.solertium.vfs.utils.VFSUtils;

/**
 * DumpResource.java
 * 
 * Performs a select * on a given table, or analyzes the database structure and
 * returns it.
 * 
 * @author carl.scott <carl.scott@solertium.com>
 * 
 */
public class DumpResource extends Resource {

	private final ExecutionContext ec;
	private final VFSPath uri;

	/**
	 * @param context
	 * @param request
	 * @param response
	 */
	public DumpResource(Context context, Request request, Response response) {
		super(context, request, response);
		setModifiable(false);

		VFSPath path;
		try {
			path = VFSResource.decodeVFSPath(request.getResourceRef().getRemainingPart());
		} catch (VFSUtils.VFSPathParseException e) {
			path = VFSPath.ROOT;
		}

		this.uri = path;

		this.ec = UserManagementApplication.getFromContext(context).getExecutionContext();

		getVariants().add(new Variant(MediaType.TEXT_XML));
	}

	@Override
	public Representation represent(Variant variant) throws ResourceException {
		if (VFSPath.ROOT.equals(uri)) {
			try {
				return new DomRepresentation(variant.getMediaType(), ec.analyzeExistingStructure());
			} catch (DBException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
		} else {
			final SelectQuery query = new SelectQuery();
			query.select(uri.getName(), "*");

			final Row.Set rs = new Row.Set();

			try {
				ec.doQuery(query, rs);
			} catch (DBException e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}

			return new DomRepresentation(variant.getMediaType(), QueryUtils.writeDocumentFromRowSet(rs.getSet()));
		}
	}

}
