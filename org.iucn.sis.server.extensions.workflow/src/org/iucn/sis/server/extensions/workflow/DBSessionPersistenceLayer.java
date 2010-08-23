package org.iucn.sis.server.extensions.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentType;
import org.iucn.sis.shared.api.models.WorkingSet;
import org.iucn.sis.shared.api.workflow.WorkflowStatus;

import com.solertium.db.CDateTime;
import com.solertium.db.CInteger;
import com.solertium.db.CString;
import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.RowID;
import com.solertium.db.query.InsertQuery;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.db.query.UpdateQuery;

public class DBSessionPersistenceLayer implements PersistenceLayer {
	
	private final ExecutionContext ec;
	
	public DBSessionPersistenceLayer() {
		ec = SIS.get().getExecutionContext();
	}
	
	public Number insertStatus(String workingSet, WorkflowStatus status) throws WorkflowManagerException {
		final Number id;
		
		final Row row = new Row();
		try {
			row.add(new CInteger("id", id = RowID.get(ec, WorkflowConstants.WORKFLOW_TABLE, "id")));
		} catch (DBException e) {
			throw new WorkflowManagerException(e);
		}
		row.add(new CString("status", status.toString()));
		row.add(new CString("workingsetid", workingSet));
		
		final InsertQuery query = new InsertQuery();
		query.setTable(WorkflowConstants.WORKFLOW_TABLE);
		query.setRow(row);
		
		try {
			ec.doUpdate(query);
		} catch (DBException e) {
			throw new WorkflowManagerException(e);
		}
		
		return id;
	}
	
	@Override
	public void addComment(Number id, WorkflowComment comment) throws WorkflowManagerException {
		final Row row = new Row();
		try {
			row.add(new CInteger("id", RowID.get(ec, WorkflowConstants.WORKFLOW_NOTES_TABLE, "id")));
		} catch (DBException e) {
			throw new WorkflowManagerException(e);
		}
		row.add(new CInteger("workflowstatusid", id));
		row.add(new CString("scope", comment.getScope()));
		row.add(new CString("user", comment.getUser().getName()));
		row.add(new CString("comment", comment.getComment()));
		row.add(new CDateTime("date", comment.getDate()));
		
		final InsertQuery query = new InsertQuery();
		query.setRow(row);
		query.setTable(WorkflowConstants.WORKFLOW_NOTES_TABLE);
		
		try {
			ec.doUpdate(query);
		} catch (DBException e) {
			throw new WorkflowManagerException(e);
		}
	}
	
	public org.iucn.sis.shared.api.models.WorkflowStatus getWorkflowRow(String workingSet) throws WorkflowManagerException {
		final SelectQuery query = new SelectQuery();
		query.select(WorkflowConstants.WORKFLOW_TABLE, "*");
		query.constrain(new CanonicalColumnName(WorkflowConstants.WORKFLOW_TABLE, "workingsetid"), QConstraint.CT_EQUALS, workingSet);
		
		final Row.Loader rl = new Row.Loader();
		
		try {
			ec.doQuery(query, rl);
		} catch (DBException e) {
			throw new WorkflowManagerException("Unexpected server error, please try again later.", e);
		}
		
		org.iucn.sis.shared.api.models.WorkflowStatus model = 
			new org.iucn.sis.shared.api.models.WorkflowStatus();
		model.setId(rl.getRow().get("id").getInteger());
		model.setStatus(rl.getRow().get("status").toString());
		
		return model;
	}
	
	
	@Override
	public void updateStatus(Number id, WorkflowStatus status) throws WorkflowManagerException {
		final Row row = new Row();
		row.add(new CString("status", status.toString()));
		
		final UpdateQuery query = new UpdateQuery();
		query.setTable(WorkflowConstants.WORKFLOW_TABLE);
		query.setRow(row);
		query.constrain(new CanonicalColumnName(WorkflowConstants.WORKFLOW_TABLE, "id"), QConstraint.CT_EQUALS, id);
		
		try {
			ec.doUpdate(query);
		} catch (DBException e) {
			throw new WorkflowManagerException(e);
		}
	}
	
	public void ensureConsistent(Integer workingSetID) throws WorkflowManagerException {
		final Collection<Assessment> assessments = WorkflowManager.getAllAssessments(workingSetID);
		System.out.println("Ensuring consistency on " + assessments.size() + " assessments...");
		
		final String table = "RedListConsistencyCheck";
		final Collection<String> failedSpecies = new ArrayList<String>();
		for (Assessment data : assessments) {
			//final String uid = data.getAssessmentID() + "_" + AssessmentType.DRAFT_ASSESSMENT_TYPE;
			final String uid = data.getId() + "";
			
			final SelectQuery query = new SelectQuery();
			query.select(table, "asm_id");
			query.constrain(new CanonicalColumnName(table, "status"), QConstraint.CT_EQUALS, Integer.valueOf(2));
			query.constrain(QConstraint.CG_AND, new CanonicalColumnName(table, "approval_status"), QConstraint.CT_EQUALS, Integer.valueOf(1));
			query.constrain(QConstraint.CG_AND, new CanonicalColumnName(table, "uid"), QConstraint.CT_EQUALS, uid);
			
			final Row.Loader rl = new Row.Loader();
			
			try {
				System.out.println(query.getSQL(ec.getDBSession()));
				ec.doQuery(query, rl);
			} catch (DBException e) {
				failedSpecies.add(data.getSpeciesName());
				continue;
			}
			
			if (rl.getRow() == null)
				failedSpecies.add(data.getSpeciesName());
		}
		
		if (!failedSpecies.isEmpty()) {
			final StringBuilder builder = new StringBuilder();
			builder.append("The following species have not yet been marked as consistency checked: ");
			for (Iterator<String> iter = failedSpecies.iterator(); iter.hasNext(); )
				builder.append(iter.next() + (iter.hasNext() ? ", " : ""));
			throw new WorkflowManagerException(builder.toString());
		}
	}
	
	public void ensureEvaluated(WorkingSet workingSet) throws WorkflowManagerException {
		final Collection<Assessment> assessments = WorkflowManager.getAllAssessments(workingSet);
		System.out.println("Ensuring evaluation on " + assessments.size() + " assessments...");
		
		final String table = "RedListEvaluated";
		final Collection<String> failedSpecies = new ArrayList<String>(); 
		for (Assessment data : assessments) {
			//final String uid = data.getAssessmentID() + "_" + AssessmentType.DRAFT_ASSESSMENT_TYPE;
			final String uid = "" + data.getId();
			
			final SelectQuery query = new SelectQuery();
			query.select(table, "asm_id");
			query.constrain(new CanonicalColumnName(table, "is_evaluated"), QConstraint.CT_EQUALS, "true");
			query.constrain(QConstraint.CG_AND, new CanonicalColumnName(table, "approval_status"), QConstraint.CT_EQUALS, Integer.valueOf(1));
			query.constrain(QConstraint.CG_AND, new CanonicalColumnName(table, "uid"), QConstraint.CT_EQUALS, uid);
			
			final Row.Loader rl = new Row.Loader();
			
			try {
				System.out.println(query.getSQL(ec.getDBSession()));
				ec.doQuery(query, rl);
			} catch (DBException e) {
				failedSpecies.add(data.getSpeciesName());
				continue;
			}
			
			if (rl.getRow() == null)
				failedSpecies.add(data.getSpeciesName());
		}
		
		if (!failedSpecies.isEmpty()) {
			final StringBuilder builder = new StringBuilder();
			builder.append("The following species have not yet been marked as evaluted: ");
			for (Iterator<String> iter = failedSpecies.iterator(); iter.hasNext(); )
				builder.append(iter.next() + (iter.hasNext() ? ", " : ""));
			throw new WorkflowManagerException(builder.toString());
		}
	}

}
