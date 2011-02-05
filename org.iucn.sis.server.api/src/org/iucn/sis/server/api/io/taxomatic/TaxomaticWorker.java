package org.iucn.sis.server.api.io.taxomatic;

import java.util.Collection;

import org.hibernate.Session;
import org.iucn.sis.server.api.io.TaxomaticIO;
import org.iucn.sis.server.api.io.TaxonIO;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.utils.TaxomaticException;
import org.iucn.sis.shared.api.models.TaxomaticHistory;
import org.iucn.sis.shared.api.models.TaxomaticOperation;
import org.iucn.sis.shared.api.models.Taxon;

public abstract class TaxomaticWorker {
	
	protected final Session session;
	protected final TaxonIO taxonIO;
	protected final TaxomaticIO taxomaticIO;
	protected final String operationName;
	
	public TaxomaticWorker(Session session, String operationName) {
		this.session = session;
		this.taxonIO = new TaxonIO(session);
		this.operationName = operationName;
		this.taxomaticIO = new TaxomaticIO(session);
	}

	public abstract void run() throws TaxomaticException;
	
	public abstract String getUndoOperationName();
	
	public String getOperationName() {
		return operationName;
	}
	
	public void undo() throws TaxomaticException {
		throw new TaxomaticException("This operation can not be undone.  Please manually perform the appropriate " + getUndoOperationName() + " operation.");
	}
	
	/*
	 * TODO: create a history record and save it.  Must happen, obviously, 
	 * after the run has successfully completed.
	 */
	public void recordOperationHistory(Collection<Taxon> taxa, TaxomaticOperation operation) throws TaxomaticException {
		for (Taxon taxon : taxa) {
			TaxomaticHistory history = new TaxomaticHistory();
			history.setOperation(operation);
			history.setTaxon(taxon);
			
			operation.getHistory().add(history);
		}
		
		try {
			SISPersistentManager.instance().saveObject(session, operation);
		} catch (PersistentException e) {
			throw new TaxomaticException("Operation succeeded, but could not record history.  Please try again later.");
		}
	}
	
	/*
	 * TODO: get all children, remove them, and then remove 
	 * the operation itself.
	 */
	public void removeOperationHistory(TaxomaticOperation operation) {
		
	}
	
}
