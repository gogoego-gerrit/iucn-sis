package org.iucn.sis.server.extensions.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Session;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.User;
import org.iucn.sis.shared.api.models.WorkflowNote;
import org.iucn.sis.shared.api.models.WorkingSet;
import org.iucn.sis.shared.api.models.fields.RedListConsistencyCheckField;
import org.iucn.sis.shared.api.models.fields.RedListEvaluatedField;
import org.iucn.sis.shared.api.utils.CanonicalNames;
import org.iucn.sis.shared.api.workflow.WorkflowStatus;

public class HibernatePersistenceLayer implements PersistenceLayer {
	
	private final SISPersistentManager manager;
	private final Session session;
	
	public HibernatePersistenceLayer(Session session) {
		manager = SISPersistentManager.instance();
		this.session = session; 
	}
	
	@Override
	public Number insertStatus(Integer workingSetID, WorkflowStatus status) throws WorkflowManagerException {
		final org.iucn.sis.shared.api.models.WorkflowStatus model = 
			new org.iucn.sis.shared.api.models.WorkflowStatus();
		model.setStatus(status.toString());
		
		try {
			
			model.setWorkingset(manager.getObject(session, WorkingSet.class, workingSetID));
			manager.saveObject(session, model);
		} catch (PersistentException e) {
			throw new WorkflowManagerException(e);
		}
		
		return model.getId();
	}
	
	@Override
	public void updateStatus(Number id, WorkflowStatus status) throws WorkflowManagerException {
		final org.iucn.sis.shared.api.models.WorkflowStatus model; 
		try {
			model = manager.getObject(session, org.iucn.sis.shared.api.models.WorkflowStatus.class, id);
		} catch (PersistentException e) {
			throw new WorkflowManagerException(e);
		}
		
		model.setStatus(status.toString());
		
		try {
			manager.saveObject(session, model);
		} catch (PersistentException e) {
			throw new WorkflowManagerException(e);
		}
	}
	
	@Override
	public void addComment(Number id, WorkflowComment comment) throws WorkflowManagerException {
		WorkflowNote note = new WorkflowNote();
		note.setComment(comment.getComment());
		note.setScope(comment.getScope());
		note.setDate(comment.getDate());
		try {
			note.setUser(manager.getObject(session, User.class, comment.getUser().getID()));
			note.setWorkflowStatus(manager.getObject(session, org.iucn.sis.shared.api.models.WorkflowStatus.class, id));
		} catch (PersistentException e) {
			throw new WorkflowManagerException(e);
		}
		
		try {
			manager.saveObject(session, note);
		} catch (PersistentException e) {
			throw new WorkflowManagerException(e);
		}
	}
	
	@Override
	public void ensureConsistent(WorkingSet workingSet) throws WorkflowManagerException {
		final Collection<Assessment> assessments = WorkflowManager.getAllAssessments(session, workingSet);
		Debug.println("Ensuring consistency on " + assessments.size() + " assessments...");
		
		final Collection<String> failedSpecies = new ArrayList<String>();
		for (Assessment data : assessments) {
			RedListConsistencyCheckField rlCC = new RedListConsistencyCheckField(data);
			rlCC.load(data.getField(CanonicalNames.RedListConsistencyCheck));
			
			if (!rlCC.isProgressComplete() || !rlCC.hasPassed())
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
	
	@Override
	public void ensureEvaluated(WorkingSet workingSet) throws WorkflowManagerException {
		final Collection<Assessment> assessments = WorkflowManager.getAllAssessments(session, workingSet);
		Debug.println("Ensuring evaluation on " + assessments.size() + " assessments...");
		
		final Collection<String> failedSpecies = new ArrayList<String>();
		for (Assessment data : assessments) {
			RedListEvaluatedField field = new RedListEvaluatedField(data);
			field.load(data.getField(CanonicalNames.RedListEvaluated));
			
			if (!(field.isEvaluated() && field.hasPassed()))
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
	
	@Override
	public org.iucn.sis.shared.api.models.WorkflowStatus getWorkflowRow(Integer workingSetID) throws WorkflowManagerException {
		org.iucn.sis.shared.api.models.WorkflowStatus model = 
			new org.iucn.sis.shared.api.models.WorkflowStatus();
		try {
			model.setWorkingset(manager.getObject(session, WorkingSet.class, workingSetID));
		} catch (PersistentException e) {
			throw new WorkflowManagerException(e);
		}
		
		final List<? extends org.iucn.sis.shared.api.models.WorkflowStatus> list;
		try {
			list = manager.listObjects(model.getClass(), session);
		} catch (PersistentException e) {
			throw new WorkflowManagerException(e);
		}
		
		return list.isEmpty() ? null : list.get(0);
	}

}
