package org.iucn.sis.server.extensions.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.filters.AssessmentFilterHelper;
import org.iucn.sis.server.api.locking.LockType;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentFilter;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.WorkingSet;
import org.iucn.sis.shared.api.workflow.WorkflowStatus;
import org.iucn.sis.shared.api.workflow.WorkflowUserInfo;
import org.restlet.data.Status;

import com.solertium.util.TrivialExceptionHandler;

public class WorkflowManager {
	
	private final PersistenceLayer persistence;
//	private final VFS vfs;
	
	public WorkflowManager() {
		//this.persistence = new DBSessionPersistenceLayer();
		this.persistence = new HibernatePersistenceLayer();
//		this.vfs = ServerApplication.getStaticVFS();
	}
	
	private WorkflowComment notifyUsers(Number workflowID, Integer workingSet, WorkflowUserInfo user, WorkflowStatus status, WorkflowComment comment, Collection<WorkflowUserInfo> notify) {
		if (notify.isEmpty())
			return new WorkflowComment(user, "Notified: No users");
		
		final WorkingSet  data = SIS.get().getWorkingSetIO().readWorkingSet(workingSet);
		
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
	
	public Number changeStatus(Integer workingSet, WorkflowUserInfo user, WorkflowStatus status, WorkflowComment comment, Collection<WorkflowUserInfo> notify) throws WorkflowManagerException {
		if (WorkflowStatus.PUBLISH.equals(status)) {
			//TODO: ensure that the submitter has proper permission (are RL Unit).
			//new AssessmentPublisher().publishAssessment(data, pubRef)
		}
		
		WorkflowStatus currentStatus;
		Number id;
		List<WorkflowComment> systemComments = new ArrayList<WorkflowComment>();
		
		final org.iucn.sis.shared.api.models.WorkflowStatus row = getWorkflowRow(workingSet.toString());
		if (row == null) {
			if (WorkflowStatus.DRAFT.equals(status))
				throw new WorkflowManagerException("This working set is already in draft status and has not started the review process yet.", Status.CLIENT_ERROR_CONFLICT);
			else {
				id = null;
				currentStatus = WorkflowStatus.DRAFT;
			}
		}
		else {
			id = row.getId();
			currentStatus = WorkflowStatus.getStatus(row.getStatus());
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
				id = insertStatus(workingSet.toString(), status);
			else
				updateStatus(id, status);
		} catch (WorkflowManagerException e) {
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
		} catch (WorkflowManagerException ignored) {
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
		final org.iucn.sis.shared.api.models.WorkflowStatus row = getWorkflowRow(workingSet);
		if (row == null)
			throw new WorkflowManagerException("This working set has not started the submission process yet.", Status.CLIENT_ERROR_BAD_REQUEST);
		
		try {
			addComment(row.getId(), comment);
		} catch (WorkflowManagerException e) {
			throw new WorkflowManagerException("Unexpected server failure when trying to add comment, please try again later.", e);
		}
	}
	
	
	private void addComment(Number id, WorkflowComment comment) throws WorkflowManagerException {
		persistence.addComment(id, comment);
	}
	
	private org.iucn.sis.shared.api.models.WorkflowStatus getWorkflowRow(String workingSet) throws WorkflowManagerException {
		return persistence.getWorkflowRow(workingSet);
	}
	
	private void updateStatus(Number id, WorkflowStatus status) throws WorkflowManagerException {
		persistence.updateStatus(id, status);
	}
	
	private Number insertStatus(String workingSet, WorkflowStatus status) throws WorkflowManagerException {
		return persistence.insertStatus(workingSet, status);
	}
	
	private void lockWorkingSet(Integer workingSetID) throws Exception {
		final WorkingSet  data = SIS.get().getWorkingSetIO().readWorkingSet(workingSetID);
		
		System.out.println("Looking to lock all unlocked working sets in " + 
			workingSetID + "; there are " + data.getSpeciesIDs().size() + 
			" species in this working set."
		);
		
		SIS.get().getLocker().persistentClearGroup(workingSetID.toString());
		
		for (Assessment assessed : getAllAssessments(data))
			if (!SIS.get().getLocker().isAssessmentPersistentLocked(assessed.getId())) {
				SIS.get().getLocker().persistentLockAssessment(
					assessed.getId(), LockType.UNDER_REVIEW, data.getCreator());
			}
	}
	
	private void unlockWorkingSet(Integer workingSetID) throws Exception {
		WorkingSet data = SIS.get().getWorkingSetIO().readWorkingSet(workingSetID);
		SIS.get().getLocker().persistentClearGroup(workingSetID.toString());
		
		for (Assessment assessed : getAllAssessments(workingSetID))
			SIS.get().getLocker().persistentEagerRelease(assessed.getId(), data.getCreator());
	}

	/**
	 * TODO: grant the users specified access to the working set.
	 */
	private void copyToNotifiedUsers(Integer workingSet, Collection<WorkflowUserInfo> notify) {
		for (WorkflowUserInfo info : notify) {
			if (SIS.get().getWorkingSetIO().subscribeToWorkingset(workingSet, info.getID()))
				System.out.println("Failed to copy working set to notified users " + info.getName());
		}
	}
	
	private void ensureConsistent(final Integer workingSetID) throws WorkflowManagerException {
		persistence.ensureConsistent(workingSetID);
	}
	
	private void ensureEvaluated(final Integer workingSet) throws WorkflowManagerException {
		ensureEvaluated(SIS.get().getWorkingSetIO().readWorkingSet(workingSet));
	}
	
	private void ensureEvaluated(final WorkingSet workingSet) throws WorkflowManagerException {
		persistence.ensureEvaluated(workingSet);
	}
	
	public static Collection<Assessment> getAllAssessments(final Integer workingSetID) {
		WorkingSet ws = SIS.get().getWorkingSetIO().readWorkingSet(workingSetID);
		return getAllAssessments(ws);
	}
	
	public static Collection<Assessment> getAllAssessments(final WorkingSet ws) {
		AssessmentFilter filter = ws.getFilter();
		AssessmentFilterHelper helper = new AssessmentFilterHelper(filter);
		Collection<Assessment> assessments = new ArrayList<Assessment>();
		for (Taxon taxon : ws.getTaxon()) {
			assessments.addAll(helper.getAssessments(taxon.getId()));
		}
		return assessments;
	}
	
}
