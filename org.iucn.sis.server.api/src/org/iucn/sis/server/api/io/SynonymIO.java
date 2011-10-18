package org.iucn.sis.server.api.io;

import org.hibernate.Session;
import org.iucn.sis.server.api.persistance.SynonymDAO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.utils.TaxomaticException;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Synonym;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.User;

public class SynonymIO {
	
	private final Session session;
	private final TaxonIO taxonIO;
	private final ReferenceIO referenceIO;
	
	public SynonymIO(Session session) {
		this.session = session;
		this.taxonIO = new TaxonIO(session);
		this.referenceIO = new ReferenceIO(session);
	}
	
	public Synonym get(int id) {
		try {
			return SynonymDAO.getSynonymByORMID(session, id);
		} catch (PersistentException e) {
			Debug.println(e);
			return null;
		}
	}
	
	public void delete(int id, User user) throws TaxomaticException {
		Synonym synonym = get(id);
		Taxon taxon = synonym.getTaxon();
		
		try {
			SynonymDAO.deleteAndDissociate(synonym, session);
		} catch (PersistentException e) {
			throw new TaxomaticException(e);
		}
		
		taxonIO.writeTaxon(taxon, user, "Synonym removed from taxon.");
	}

}
