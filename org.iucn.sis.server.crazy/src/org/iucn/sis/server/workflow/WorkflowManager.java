package org.iucn.sis.server.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.iucn.sis.server.filters.AssessmentFilterHelper;
import org.iucn.sis.server.io.WorkingSetIO;
import org.iucn.sis.server.locking.FileLocker;
import org.iucn.sis.server.locking.LockType;
import org.iucn.sis.server.simple.SISContainerApp;
import org.iucn.sis.server.simple.workers.WorkingSetRestletWorker;
import org.iucn.sis.shared.BaseAssessment;
import org.iucn.sis.shared.data.WorkingSetData;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.workflow.WorkflowStatus;
import org.iucn.sis.shared.workflow.WorkflowUserInfo;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

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
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.VFS;

public class WorkflowManager {
	
	private final ExecutionContext ec;
	private final VFS vfs;
	
	public WorkflowManager(ExecutionContext ec) {
		this.ec = ec;
		this.vfs = SISContainerApp.getStaticVFS();
	}
	
	private WorkflowComment notifyUsers(Number workflowID, String workingSet, WorkflowUserInfo user, WorkflowStatus status, WorkflowComment comment, Collection<WorkflowUserInfo> notify) {
		if (notify.isEmpty())
			return new WorkflowComment(user, "Notified: No users");
		
		final WorkingSetData data = 
			WorkingSetIO.readPublicWorkingSetAsWorkingSetData(vfs, workingSet);
		
		final Collection<WorkflowUserInfo> recipients = new HashSet<WorkflowUserInfo>();
		for (WorkflowUserInfo current : notify) {
			if (!current.getEmail().equals("")) {
				recipients.add(current);
			}
		}
		
		final WorkflowStatusMailer mailer = 
			new WorkflowStatusMailer(user, status, data, comment);
		mailer.send(recipients);
		
		/*
		 * FIXME: should we only log records of users who actually got 
		 * e-mailed, or do we log all that were requested to be notified?
		 */
		final StringBuilder builder = new StringBuilder();
		builder.append("Notified: ");
		
		for (Iterator<WorkflowUserInfo> iter = recipients.iterator(); iter.hasNext(); )
			builder.append(iter.next().getName() + (iter.hasNext() ? ", " : ". "));
		
		if (notify.size() > recipients.size()) {
			builder.append("Some users marked for notification did not provide valid " +
				"e-mail addresses: ");
			final List<WorkflowUserInfo> badUsers = new ArrayList<WorkflowUserInfo>();
			for (WorkflowUserInfo current : notify)
				if (!recipients.contains(current))
					badUsers.add(current);
			
			for (Iterator<WorkflowUserInfo> iter = badUsers.iterator(); iter.hasNext(); )	
				builder.append(iter.next().getName() + (iter.hasNext() ? ", " : "."));
		}
		
		copyToNotifiedUsers(workingSet, notify);
		
		return new WorkflowComment(user, builder.toString());
	}
	
	public Number changeStatus(String workingSet, WorkflowUserInfo user, WorkflowStatus status, WorkflowComment comment, Collection<WorkflowUserInfo> notify) throws WorkflowManagerException {
		if (WorkflowStatus.PUBLISH.equals(status)) {
			//TODO: ensure that the submitter has proper permission (are RL Unit).
		}
		
		WorkflowStatus currentStatus;
		Number id;
		List<WorkflowComment> systemComments = new ArrayList<WorkflowComment>();
		
		final Row row = getWorkflowRow(workingSet);
		if (row == null) {
			if (WorkflowStatus.DRAFT.equals(status))
				throw new WorkflowManagerException("This working set is already in draft status and has not started the review process yet.", Status.CLIENT_ERROR_CONFLICT);
			else {
				id = null;
				currentStatus = WorkflowStatus.DRAFT;
			}
		}
		else {
			id = row.get("id").getInteger();
			currentStatus = WorkflowStatus.getStatus(row.get("status").toString());
		}
		
		// No need to block this
		//if (currentStatus.equals(status))
		//	throw new WorkflowManagerException("Working Set status is already " + currentStatus, Status.CLIENT_ERROR_CONFLICT);
		
		if (currentStatus.equals(WorkflowStatus.PUBLISH))
			throw new WorkflowManagerException("Working Set is already published.", Status.CLIENT_ERROR_CONFLICT);
		
		if (WorkflowStatus.FINAL.equals(currentStatus)) {
			//It can move to any status
			systemComments.add(new WorkflowComment(user, "Status: Working Set status reverted to " + status));
		}
		else {
			//It can only move up or down.
			System.out.println("Current status is " + currentStatus + "; can only move up to " + currentStatus.getNextStatus() + " or back to " + currentStatus.getPreviousStatus());
			if (status.equals(currentStatus.getNextStatus())) {
				//Moved up in the chain
				systemComments.add(new WorkflowComment(user, "Status: Working Set status promoted to " + status));
			}
			else if (status.equals(currentStatus.getPreviousStatus())) {
				systemComments.add(new WorkflowComment(user, "Status: Working Set status reverted to " + status));
			}
			else {
				throw new WorkflowManagerException("The status of this working set can only be changed to " +
					writePossibleStatusValues(currentStatus) + ".", Status.CLIENT_ERROR_CONFLICT);
			}
		}
		
		/*
		 * Check special cases where further evaluation is needed
		 */
		if (WorkflowStatus.REVIEW.equals(currentStatus) && WorkflowStatus.REVIEW.getNextStatus().equals(status)) {
			ensureEvaluated(workingSet);
		}
		else if (WorkflowStatus.CONSISTENCY_CHECK.equals(currentStatus) && WorkflowStatus.CONSISTENCY_CHECK.getNextStatus().equals(status)) {
			ensureEvaluated(workingSet);
			ensureConsistent(workingSet);
		}
		
		//If here, try to do work.
		try {
			if (id == null)
				id = insertStatus(workingSet, status);
			else
				updateStatus(id, status);
		} catch (DBException e) {
			throw new WorkflowManagerException("Unexpected server failure when trying to update status, please try again later.", e);
		}
		
		systemComments.add(notifyUsers(id, workingSet, user, currentStatus, comment, notify));
		
		systemComments.add(new WorkflowComment(user, "Notes: " + comment.getComment()));
		
		final StringBuilder builder = new StringBuilder();
		for (WorkflowComment sysComment : systemComments) {
			builder.append("<br/>" + sysComment.getComment());
		}
		
		try {
			addComment(id, new WorkflowComment(user, builder.toString()));
		} catch (DBException ignored) {
			TrivialExceptionHandler.ignore(this, ignored);
		}
		
		if (WorkflowStatus.DRAFT.equals(status)) {
			try {
				unlockWorkingSet(workingSet);
			} catch (Exception ignored) {
				TrivialExceptionHandler.ignore(this, ignored);
			}
		}
		else {
			try {
				lockWorkingSet(workingSet);
			} catch (Exception ignored) {
				TrivialExceptionHandler.ignore(this, ignored);
			}
		}
		
		return id;
	}
	
	private String writePossibleStatusValues(WorkflowStatus status) {
		StringBuilder possible = new StringBuilder();
		if (status.getPreviousStatus() != null)
			possible.append(status.getPreviousStatus());
		if (status.getNextStatus() != null) {
			if (!possible.toString().equals(""))
				possible.append(" or ");
			possible.append(status.getNextStatus());
		}
		return possible.toString();
	}
	
	public void addComment(String workingSet, WorkflowComment comment) throws WorkflowManagerException {
		final Row row = getWorkflowRow(workingSet);
		if (row == null)
			throw new WorkflowManagerException("This working set has not started the submission process yet.", Status.CLIENT_ERROR_BAD_REQUEST);
		
		try {
			addComment(row.get("id").getInteger(), comment);
		} catch (DBException e) {
			throw new WorkflowManagerException("Unexpected server failure when trying to add comment, please try again later.", e);
		}
	}
	
	
	private void addComment(Number id, WorkflowComment comment) throws DBException {
		final Row row = new Row();
		row.add(new CInteger("id", RowID.get(ec, WorkflowConstants.WORKFLOW_NOTES_TABLE, "id")));
		row.add(new CInteger("workflowstatusid", id));
		row.add(new CString("scope", comment.getScope()));
		row.add(new CString("user", comment.getUser().getName()));
		row.add(new CString("comment", comment.getComment()));
		row.add(new CDateTime("date", comment.getDate()));
		
		final InsertQuery query = new InsertQuery();
		query.setRow(row);
		query.setTable(WorkflowConstants.WORKFLOW_NOTES_TABLE);
		
		ec.doUpdate(query);
	}
	
	private Row getWorkflowRow(String workingSet) throws WorkflowManagerException {
		final SelectQuery query = new SelectQuery();
		query.select(WorkflowConstants.WORKFLOW_TABLE, "*");
		query.constrain(new CanonicalColumnName(WorkflowConstants.WORKFLOW_TABLE, "workingsetid"), QConstraint.CT_EQUALS, workingSet);
		
		final Row.Loader rl = new Row.Loader();
		
		try {
			ec.doQuery(query, rl);
		} catch (DBException e) {
			throw new WorkflowManagerException("Unexpected server error, please try again later.", e);
		}
		
		return rl.getRow();
	}
	
	private void updateStatus(Number id, WorkflowStatus status) throws DBException {
		final Row row = new Row();
		row.add(new CString("status", status.toString()));
		
		final UpdateQuery query = new UpdateQuery();
		query.setTable(WorkflowConstants.WORKFLOW_TABLE);
		query.setRow(row);
		query.constrain(new CanonicalColumnName(WorkflowConstants.WORKFLOW_TABLE, "id"), QConstraint.CT_EQUALS, id);
		
		ec.doUpdate(query);
	}
	
	private Number insertStatus(String workingSet, WorkflowStatus status) throws DBException {
		final Number id;
		
		final Row row = new Row();
		row.add(new CInteger("id", id = RowID.get(ec, WorkflowConstants.WORKFLOW_TABLE, "id")));
		row.add(new CString("status", status.toString()));
		row.add(new CString("workingsetid", workingSet));
		
		final InsertQuery query = new InsertQuery();
		query.setTable(WorkflowConstants.WORKFLOW_TABLE);
		query.setRow(row);
		
		ec.doUpdate(query);
		
		return id;
	}
	
	private void lockWorkingSet(String workingSetID) throws Exception {
		final WorkingSetData data = 
			WorkingSetIO.readPublicWorkingSetAsWorkingSetData(vfs, workingSetID);
		
		System.out.println("Looking to lock all unlocked working sets in " + 
			workingSetID + "; there are " + data.getSpeciesIDs().size() + 
			" species in this working set."
		);
		
		FileLocker.impl.persistentClearGroup(workingSetID);
		
		for (AssessmentData assessed : getAllAssessments(vfs, data))
			if (!FileLocker.impl.isAssessmentPersistentLocked(assessed.getAssessmentID(), BaseAssessment.DRAFT_ASSESSMENT_STATUS)) {
				FileLocker.impl.persistentLockAssessment(
					assessed.getAssessmentID(), BaseAssessment.DRAFT_ASSESSMENT_STATUS, 
					LockType.UNDER_REVIEW, data.getCreator()
				);
			}
	}
	
	private void unlockWorkingSet(String workingSetID) throws Exception {
		final WorkingSetData data = 
			WorkingSetIO.readPublicWorkingSetAsWorkingSetData(vfs, workingSetID);
		
		FileLocker.impl.persistentClearGroup(workingSetID);
		
		for (AssessmentData assessed : getAllAssessments(vfs, data))
			FileLocker.impl.persistentEagerRelease(assessed.getAssessmentID(), 
				BaseAssessment.DRAFT_ASSESSMENT_STATUS, data.getCreator());
	}

	/**
	 * TODO: grant the users specified access to the working set.
	 */
	private void copyToNotifiedUsers(String workingSet, Collection<WorkflowUserInfo> notify) {
		final WorkingSetRestletWorker worker = new WorkingSetRestletWorker(vfs);
		for (WorkflowUserInfo info : notify) {
			try {
				worker.subscribeToPublicWorkingSet(workingSet, info.getID());
			} catch (ResourceException e) {
				System.out.println("Failed to copy working set to notified users: " + e.getMessage());
				e.printStackTrace();
				TrivialExceptionHandler.ignore(this, e);
			}
		}
	}
	
	private void ensureConsistent(final String workingSet) throws WorkflowManagerException {
		final Collection<AssessmentData> assessments = getAllAssessments(vfs, workingSet);
		System.out.println("Ensuring consistency on " + assessments.size() + " assessments...");
		
		final String table = "RedListConsistencyCheck";
		final Collection<String> failedSpecies = new ArrayList<String>();
		for (AssessmentData data : assessments) {
			final String uid = data.getAssessmentID() + "_" + BaseAssessment.DRAFT_ASSESSMENT_STATUS;
			
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
	
	private void ensureEvaluated(final String workingSet) throws WorkflowManagerException {
		ensureEvaluated(
			WorkingSetIO.readPublicWorkingSetAsWorkingSetData(vfs, workingSet)
		);
	}
	
	private void ensureEvaluated(final WorkingSetData workingSet) throws WorkflowManagerException {
		final Collection<AssessmentData> assessments = getAllAssessments(vfs, workingSet);
		System.out.println("Ensuring evaluation on " + assessments.size() + " assessments...");
		
		final String table = "RedListEvaluated";
		final Collection<String> failedSpecies = new ArrayList<String>(); 
		for (AssessmentData data : assessments) {
			final String uid = data.getAssessmentID() + "_" + BaseAssessment.DRAFT_ASSESSMENT_STATUS;
			
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
	
	public static Collection<AssessmentData> getAllAssessments(final VFS vfs, final String workingSetID) {
		return getAllAssessments(vfs, 
			WorkingSetIO.readPublicWorkingSetAsWorkingSetData(vfs, workingSetID)
		);
	}
	
	public static Collection<AssessmentData> getAllAssessments(final VFS vfs, final WorkingSetData data) {
		final AssessmentFilterHelper helper = 
			new AssessmentFilterHelper(data.getFilter());
		
		final Collection<AssessmentData> list = new ArrayList<AssessmentData>();
		
		for (String speciesID : data.getSpeciesIDs())
			for (AssessmentData assessmentData : helper.getAssessments(speciesID, vfs))
				list.add(assessmentData);
		
		return list;
	}
	
}
