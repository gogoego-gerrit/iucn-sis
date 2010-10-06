package org.iucn.sis.server.extensions.user.resources;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.IdentifierEqExpression;
import org.hibernate.criterion.IlikeExpression;
import org.hibernate.criterion.LogicalExpression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.extensions.user.application.UserManagementApplication;
import org.iucn.sis.shared.api.criteriacalculator.CriteriaResult;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.User;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
import com.solertium.util.BaseDocumentUtils;

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
		searchable = new String[] { "firstName", "lastName", "affiliation", "userid" };

		getVariants().add(new Variant(MediaType.TEXT_XML));
	}

	@Override
	public Representation represent(Variant variant) throws ResourceException {
		Session session = SISPersistentManager.instance().getSession();
		Criteria criteria = session.createCriteria(User.class);

		final Form form = getRequest().getResourceRef().getQueryAsForm();

		for (int i = 0; i < searchable.length; i++) {
			if (form.getFirstValue(searchable[i].toLowerCase()) != null) {
				int constraintCompare = searchable[i].equals("userid") ? QConstraint.CT_EQUALS : 
					QConstraint.CT_CONTAINS_IGNORE_CASE;
				
				for (String value : form.getValuesArray(searchable[i].toLowerCase())) {
					if (constraintCompare == QConstraint.CT_EQUALS)
						criteria = criteria.add(Restrictions.eq("id", value));
					else
						criteria = criteria.add(Restrictions.ilike(searchable[i], value, MatchMode.ANYWHERE));
				}
			}
		}

		final List<User> users;
		try {
			users = criteria.list();
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		final Document document = BaseDocumentUtils.impl.newDocument();
		final Element root = document.createElement("root");
		
		for (User user : users) {
			try {
				final Element row = document.createElement("row");
				appendField(document, row, "firstName", user.getFirstName());
				appendField(document, row, "lastName", user.getLastName());
				appendField(document, row, "initials", user.getInitials());
				appendField(document, row, "email", user.getEmail());
				appendField(document, row, "userid", user.getId()+"");
				appendField(document, row, "username", user.getUsername());
				appendField(document, row, "affiliation", user.getAffiliation());
				
				root.appendChild(row);
			} catch (Exception e) {
				Debug.println("Could not add user {0}: {1}", user.getUsername(), e);
			}
		}
		
		document.appendChild(root);
		
		return new DomRepresentation(variant.getMediaType(), document);
	}
	
	private void appendField(Document document, Element row, String name, String value) {
		Element fieldEl = 
			BaseDocumentUtils.impl.createCDATAElementWithText(document, "field", value);
		fieldEl.setAttribute("name", name);
		
		row.appendChild(fieldEl);
		
	}

}
