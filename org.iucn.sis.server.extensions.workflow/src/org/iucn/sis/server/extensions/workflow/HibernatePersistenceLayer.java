package org.iucn.sis.server.extensions.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
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
	
	public HibernatePersistenceLayer() {
		manager = new SISPersistentManager();
	}
	
	@Override
	public Number insertStatus(String workingSet, WorkflowStatus status) throws WorkflowManagerException {
		final WorkingSet fauxWorkingSet = new WorkingSet();
		fauxWorkingSet.setId(Integer.parseInt(workingSet));
		
		final org.iucn.sis.shared.api.models.WorkflowStatus model = 
			new org.iucn.sis.shared.api.models.WorkflowStatus();
		model.setStatus(status.toString());
		model.setWorkingset(fauxWorkingSet);
		
		try {
			manager.saveObject(model);
		} catch (PersistentException e) {
			throw new WorkflowManagerException(e);
		}
		
		return model.getId();
	}
	
	@Override
	public void updateStatus(Number id, WorkflowStatus status) throws WorkflowManagerException {
		final org.iucn.sis.shared.api.models.WorkflowStatus model; 
		try {
			model = manager.getObject(org.iucn.sis.shared.api.models.WorkflowStatus.class, id);
		} catch (PersistentException e) {
			throw new WorkflowManagerException(e);
		}
		
		model.setStatus(status.toString());
		
		try {
			manager.saveObject(model);
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
			note.setUser(manager.getObject(User.class, comment.getUser().getID()));
			note.setWorkflowStatus(manager.getObject(org.iucn.sis.shared.api.models.WorkflowStatus.class, id));
		} catch (PersistentException e) {
			throw new WorkflowManagerException(e);
		}
		
		try {
			manager.saveObject(note);
		} catch (PersistentException e) {
			throw new WorkflowManagerException(e);
		}
	}
	
	@Override
	public void ensureConsistent(Integer workingSetID) throws WorkflowManagerException {
		final Collection<Assessment> assessments = WorkflowManager.getAllAssessments(workingSetID);
		System.out.println("Ensuring consistency on " + assessments.size() + " assessments...");
		
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
		final Collection<Assessment> assessments = WorkflowManager.getAllAssessments(workingSet);
		System.out.println("Ensuring evaluation on " + assessments.size() + " assessments...");
		
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
	public org.iucn.sis.shared.api.models.WorkflowStatus getWorkflowRow(String workingSet) throws WorkflowManagerException {
		org.iucn.sis.shared.api.models.WorkflowStatus model = 
			new org.iucn.sis.shared.api.models.WorkflowStatus();
		try {
			model.setWorkingset(manager.getObject(WorkingSet.class, workingSet));
		} catch (PersistentException e) {
			throw new WorkflowManagerException(e);
		}
		
		final List<? extends org.iucn.sis.shared.api.models.WorkflowStatus> list;
		try {
			list = manager.listObjects(model.getClass());
		} catch (PersistentException e) {
			throw new WorkflowManagerException(e);
		}
		
		return list.isEmpty() ? null : list.get(0);
	}

}
