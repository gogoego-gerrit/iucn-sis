package org.iucn.sis.server.extensions.user.resources;

import org.iucn.sis.server.extensions.user.application.UserManagementApplication;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.query.QComparisonConstraint;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.QConstraintGroup;
import com.solertium.db.query.QRelationConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.db.utils.QueryUtils;

/**
 * ProfileSearchResource.java
 * 
 * Allows for perusing the profiles of users and returns pertinent information
 * regarding them, but not full data. Has a list of searchable fields from the
 * profile table that can be updated should you want other fields to be
 * searchable.
 * 
 * @author carl.scott <carl.scott@solertium.com>
 * 
 */
public class ProfileSearchResource extends Resource {

	private final String[] searchable;
	private final ExecutionContext ec;

	public ProfileSearchResource(Context context, Request request, Response response) {
		super(context, request, response);
		setModifiable(false);

		ec = UserManagementApplication.getFromContext(context).getExecutionContext();
		searchable = new String[] { "firstname", "lastname", "affiliation", "userid", "quickgroup" };

		getVariants().add(new Variant(MediaType.TEXT_XML));
	}

	@Override
	public Representation represent(Variant variant) throws ResourceException {
		final SelectQuery query = new SelectQuery();
		query.select("profile", "firstname");
		query.select("profile", "lastname");
		query.select("profile", "quickgroup");
		query.select("profile", "userid");
		query.select("profile", "email");
		query.select("user", "username");
		query.join("user", new QRelationConstraint(
			new CanonicalColumnName("profile", "userid"),
			new CanonicalColumnName("user", "id")
		));
		

		final QConstraintGroup group = new QConstraintGroup();

		final Form form = getRequest().getResourceRef().getQueryAsForm();

		for (int i = 0; i < searchable.length; i++) {
			if (form.getFirstValue(searchable[i]) != null) {
				int constraintCompare = searchable[i].equals("userid") ? QConstraint.CT_EQUALS : 
					QConstraint.CT_CONTAINS_IGNORE_CASE;
				
				for (String value : form.getValuesArray(searchable[i])) {
					group.addConstraint(QConstraint.CG_OR, new QComparisonConstraint(new CanonicalColumnName("profile",
							searchable[i]), constraintCompare, value));
				}
			}
		}
		
		if (!group.isEmpty())
			query.constrain(group);

		try {
			final Row.Set rs = new Row.Set();
			ec.doQuery(query, rs);

			return new DomRepresentation(variant.getMediaType(), QueryUtils.writeDocumentFromRowSet(rs.getSet()));
		} catch (DBException e) {
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}

}
