package org.iucn.sis.server.extensions.integrity;

import org.hibernate.Session;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;

/**
 * IntegrityTestResource.java
 * 
 * Used to find values of a particular assessment, just for testing purposes. I
 * want to ensure that validation succeeds or fails appropriately.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 * 
 */
@SuppressWarnings("deprecation")
public class IntegrityTestResource extends IntegrityDBResource {

	public IntegrityTestResource(Context context, Request request,
			Response response) {
		super(context, request, response);

		getVariants().add(new Variant(MediaType.TEXT_XML));
	}
	
	@Override
	public Representation represent(Variant variant, Session session) throws ResourceException {
		final String table = getRequest().getResourceRef().getLastSegment();
		final SelectQuery query = new SelectQuery();
		query.select(table, "*");
		query.constrain(new CanonicalColumnName(table, "uid"),
				QConstraint.CT_EQUALS, getRequest().getResourceRef().getQueryAsForm().getFirstValue("uid", "411398_published_status"));

		return getRowsAsRepresentation(query);

	}

}
