package org.iucn.sis.server.api.io;

import org.hibernate.Session;
import org.iucn.sis.server.api.persistance.CommonNameDAO;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.utils.TaxomaticException;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.CommonName;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.User;

public class CommonNameIO {
	
	private final Session session;
	private final TaxonIO taxonIO;
	private final ReferenceIO referenceIO;
	
	public CommonNameIO(Session session) {
		this.session = session;
		this.taxonIO = new TaxonIO(session);
		this.referenceIO = new ReferenceIO(session);
	}
	
	public void add(Taxon taxon, CommonName commonName, User user) throws TaxomaticException {
		if (taxon.getCommonNames().isEmpty())
			commonName.setPrincipal(true);
		
		commonName.setTaxon(taxon);
		taxon.getCommonNames().add(commonName);
		
		taxonIO.writeTaxon(taxon, user);
	}
	
	/**
	 * Returns the common name with the given ID, or null 
	 * if no common name with that ID was found or a 
	 * persistent exception was thrown.
	 * @param id
	 * @return
	 */
	public CommonName get(int id) {
		try {
			return CommonNameDAO.getCommonNameByORMID(session, id);
		} catch (PersistentException e) {
			Debug.println(e);
			return null;
		}
	}
	
	public void update(CommonName commonName) throws PersistentException {
		commonName.toXML();
		SISPersistentManager.instance().updateObject(session, commonName);
	}
	
	public void setPrimary(int id)  {
		final CommonName primary = get(id);
		
		final Taxon taxon = primary.getTaxon();
		for (CommonName name : taxon.getCommonNames()) {
			boolean isPrimary = name.getId() == primary.getId();
			if (isPrimary != name.isPrimary()) {
				name.setPrincipal(isPrimary);
				session.save(name);
			}
		}
	}

	/**
	 * Delete the common name with the given ID and 
	 * dissociate it with any notes, references, and 
	 * taxa.
	 * @param id
	 * @throws PersistentException if a hibernate issue occurs
	 */
	public void delete(int id, User user) throws TaxomaticException {
		CommonName commonName = get(id);
		Taxon taxon = commonName.getTaxon();
		
		try {
			CommonNameDAO.deleteAndDissociate(commonName, session);
		} catch (PersistentException e) {
			throw new TaxomaticException(e);
		}
		
		taxonIO.writeTaxon(taxon, user);
	}

}
