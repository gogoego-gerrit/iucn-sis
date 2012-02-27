package org.iucn.sis.server.extensions.offline;

import java.util.Map;

import org.hibernate.Session;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.utils.AssessmentPersistence;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Notes;
import org.iucn.sis.shared.api.models.Reference;

public class OfflineAssessmentPersistence extends AssessmentPersistence {
	
	private Map<Integer, Integer> offlineNotes;
	private Map<Integer, Integer> offlineReferences;
	
	public OfflineAssessmentPersistence(Session session, Assessment target) {
		super(session, target);
		setAllowForeignData(true);
	}
	
	public void setOfflineNotes(Map<Integer, Integer> offlineNotes) {
		this.offlineNotes = offlineNotes;
	}
	
	public void setOfflineReferences(Map<Integer, Integer> offlineReferences) {
		this.offlineReferences = offlineReferences;
	}
	
	@Override
	protected Notes findNote(Notes sourceNote) throws PersistentException {
		if (sourceNote.getOfflineStatus())
			sourceNote.setId(offlineNotes.get(sourceNote.getId()));
		return super.findNote(sourceNote);
	}
	
	@Override
	protected Reference findReference(Reference sourceReference) throws PersistentException {
		if (sourceReference.getOfflineStatus())
			sourceReference.setId(offlineReferences.get(sourceReference.getId()));
		return super.findReference(sourceReference);
	}

}
