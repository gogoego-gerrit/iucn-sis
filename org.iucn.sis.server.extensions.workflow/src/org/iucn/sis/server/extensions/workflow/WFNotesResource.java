package org.iucn.sis.server.extensions.workflow;

import org.hibernate.Session;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.query.QComparisonConstraint;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.QConstraintGroup;
import com.solertium.db.query.QRelationConstraint;
import com.solertium.db.query.SelectQuery;

public class WFNotesResource extends WFDBResource {
	
	private final String workingSet, protocol;

	public WFNotesResource(Context context, Request request, Response response) {
		super(context, request, response);
		this.workingSet = (String)request.getAttributes().get("working-set");
		this.protocol = (String)request.getAttributes().get("protocol");
	}
	
	@Override
	public Representation represent(Variant variant, Session session) throws ResourceException {
		final SelectQuery query = new SelectQuery();
		query.select(WorkflowConstants.WORKFLOW_NOTES_TABLE, "comment");
		query.select(WorkflowConstants.WORKFLOW_NOTES_TABLE, "date");
		query.select(WorkflowConstants.WORKFLOW_NOTES_TABLE, "userid");
		query.select("user", "firstname");
		query.select("user", "lastname");
		query.select("user", "username");
		query.join(WorkflowConstants.WORKFLOW_TABLE, new QRelationConstraint(
			new CanonicalColumnName(WorkflowConstants.WORKFLOW_NOTES_TABLE, "workflowstatusid"), 
			new CanonicalColumnName(WorkflowConstants.WORKFLOW_TABLE, "id")
		));
		query.join("user", new QRelationConstraint(
			new CanonicalColumnName(WorkflowConstants.WORKFLOW_NOTES_TABLE, "userid"),
			new CanonicalColumnName("user", "id")
		));
		query.constrain(new CanonicalColumnName(WorkflowConstants.WORKFLOW_TABLE, "workingsetid"), QConstraint.CT_EQUALS, workingSet);
		if (protocol != null && !WorkflowConstants.IS_GLOBAL_STATUS.equals(protocol)) {
			QConstraintGroup additional = new QConstraintGroup();
			additional.addConstraint(new QComparisonConstraint(
				new CanonicalColumnName(WorkflowConstants.WORKFLOW_NOTES_TABLE, "scope"), 
				QConstraint.CT_EQUALS, WorkflowConstants.IS_GLOBAL_STATUS
			));
			additional.addConstraint(QConstraint.CG_OR, new QComparisonConstraint(
				new CanonicalColumnName(WorkflowConstants.WORKFLOW_NOTES_TABLE, "scope"), 
				QConstraint.CT_EQUALS, protocol
			));
			query.constrain(QConstraint.CG_AND, additional);
		}
		
		try {
			return getRowsAsRepresentation(query);
		} catch (ResourceException e) {
			e.printStackTrace();
			throw e;
		}	 
	}

}
