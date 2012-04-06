package org.iucn.sis.server.api.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.io.AssessmentIO.AssessmentIOWriteResult;
import org.iucn.sis.server.api.locking.TaxonLockAquirer;
import org.iucn.sis.server.api.persistance.TaxonCriteria;
import org.iucn.sis.server.api.persistance.TaxonDAO;
import org.iucn.sis.server.api.persistance.TaxonStatusCriteria;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.utils.TaxomaticException;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Edit;
import org.iucn.sis.shared.api.models.SearchQuery;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonLevel;
import org.iucn.sis.shared.api.models.TaxonStatus;
import org.iucn.sis.shared.api.models.User;

public class TaxonIO {

	private final Session session;

	public TaxonIO(Session session) {
		this.session = session;
	}

	public TaxonCriteria getCriteria() {
		return new TaxonCriteria(session);
	}

	public Taxon getTaxon(Integer id) {
		try {
			return TaxonDAO.getTaxon(session, id);
		} catch (PersistentException e) {
			Debug.println(e);
			return null;
		}
	}
	
	public Taxon getTaxonNonLazy(Integer id) {
		try {
			return TaxonDAO.getTaxon(session, id);
		} catch (PersistentException e) {
			// TODO Auto-generated catch block
			Debug.println(e);
			return null;
		}
		
	}

	public List<Taxon> getTaxa(String idCSV) {
		List<Taxon> taxa = new ArrayList<Taxon>();
		for (String id : idCSV.split(",")) {
			taxa.add(getTaxon(Integer.valueOf(id)));
		}
		return taxa;
	}

	/**
	 * Reads taxa in from the file system based on an ID and parses it using the
	 * TaxonFactory. If a taxon cannot be read in, nullFail determines whether
	 * it immediately returns a null response or if it skips over it and returns
	 * what it can; if nullFail is true, null is returned.
	 * 
	 * @param ids
	 *            of the taxon
	 * @param nullFail
	 *            - if a taxon cannot be read in, e.g. if it's missing, nullFail
	 *            == true will cause the returned list to be null
	 * @return a list of Taxon objects
	 */
	public List<Taxon> getTaxa(Integer[] ids, boolean nullFail) {
		List<Taxon> list = new ArrayList<Taxon>();
		for (Integer id : ids) {
			Taxon taxon;
			try {
				taxon = TaxonDAO.getTaxon(session, id);
			} catch (PersistentException e) {
				// TODO Auto-generated catch block
				Debug.println(e);
				taxon = null;
			}
			if (taxon == null) {
				if (nullFail)
					return null;
			} else {
				list.add(taxon);
			}
		}

		return list;
	}

	public void writeTaxon(Taxon taxonToSave, User user, String reason) throws TaxomaticException {
		Taxon oldTaxon = getTaxon(taxonToSave.getId());
		if (oldTaxon != null)
			writeTaxon(taxonToSave, oldTaxon, reason, user);
		else
			writeTaxon(taxonToSave, taxonToSave, reason, user);
	}

	/**
	 * Returns whether the taxa was allowed to be saved. Note that only
	 * non-taxomatic operations on the node can be saved here. To do taxomatic
	 * opertaions, use taxomaticIO
	 * 
	 * @param nodeToSave
	 * @return summary
	 */
	public void writeTaxon(Taxon nodeToSave, User user, String reason, boolean requireLocking) throws TaxomaticException {
		ArrayList<Taxon> list = new ArrayList<Taxon>();
		list.add(nodeToSave);
		
		writeTaxa(list, user, reason, requireLocking);
	}

	void writeTaxon(Taxon taxonToSave, Taxon oldTaxon, String reason, User user) throws TaxomaticException {
		ArrayList<Taxon> list = new ArrayList<Taxon>();
		list.add(taxonToSave);

		Map<Integer, Taxon> idToOldTaxa = new HashMap<Integer, Taxon>();
		idToOldTaxa.put(taxonToSave.getId(), oldTaxon);
		
		writeTaxa(list, idToOldTaxa, user, reason, true);
	}

	/**
	 * Returns whether the taxa was allowed to be saved. Notice that only
	 * non-taxomatic operations on the node can be saved here to do taxomatic
	 * opertaions, use taxomaticIO
	 * 
	 */
	public void writeTaxa(Collection<Taxon> nodesToSave, User user, String reason, boolean requireLocking) throws TaxomaticException {
		Map<Integer, Taxon> idToOldTaxa = new HashMap<Integer, Taxon>();
		for (Taxon taxon : nodesToSave)
			idToOldTaxa.put(taxon.getId(), getTaxon(taxon.getId()));
		
		writeTaxa(nodesToSave, idToOldTaxa, user, reason, requireLocking);
	}

	void writeTaxa(Collection<Taxon> taxaToSave, Map<Integer, Taxon> idToOldTaxa, User user, String reason, boolean requireLocking) throws TaxomaticException {
		TaxomaticIO taxomaticIO = new TaxomaticIO(session);
		// DO NOT ALLOW ANY TAXOMATIC OPERATION SAVES
		for (Taxon taxon : taxaToSave) {
			Taxon oldTaxon = idToOldTaxa.get(taxon.getId());
			if (oldTaxon != null && taxomaticIO.
					isTaxomaticOperationNecessary(taxon, oldTaxon)) {
				throw new TaxomaticException("Server Error: Taxon can not be saved until taxomatic operations are complete.", false);
			}
		}

		// TRY TO AQUIRE LOCKS
		TaxonLockAquirer aquirer = new TaxonLockAquirer(taxaToSave);
		if (requireLocking)
			aquirer.aquireLocks(user.getUsername());

		if (!requireLocking || aquirer.isSuccess()) {
			try {
				Date date = new Date();
				for (Taxon taxon : taxaToSave) {
					Edit edit = new Edit(reason);
					edit.setUser(user);
					edit.setCreatedDate(date);
					edit.getTaxon().add(taxon);
					taxon.getEdits().add(edit);
					
					SIS.get().getManager().saveObject(session, taxon);
					
					taxon.toXML();
				}
			} catch (PersistentException e) {
				Debug.println("Failed to save taxa list: \n{0}", e);
				throw new TaxomaticException("Failed to save taxa changes due to server error.", false);
			} finally {
				aquirer.releaseLocks();
			}
		}
		else
			throw new TaxomaticException("Failed to acquire lock to save taxa.", false);
	}

	public boolean permenantlyDeleteAllTrashedTaxa() {		
		try {
			for (Taxon taxon : getTrashedTaxa()) {
				SIS.get().getManager().deleteObject(session,taxon); 
			}
			return true;
		} catch (PersistentException e) {
			// TODO Auto-generated catch block
			Debug.println(e);
		}
		
		return false;
	}

	public Taxon getTrashedTaxon(Integer id) {
		try {
			return TaxonDAO.getTrashedTaxon(session, id);
		} catch (PersistentException e) {
			// TODO Auto-generated catch block
			Debug.println(e);
			return null;
		}
	}

	public Taxon[] getTrashedTaxa() throws PersistentException {
		return TaxonDAO.getTrashedTaxa(session);
	}

	public void restoreTrashedTaxon(Integer taxonID, User user) throws TaxomaticException {
		Taxon taxon;
		try {
			taxon = TaxonDAO.getTrashedTaxon(session, taxonID);
		} catch (PersistentException e) {
			taxon = null;
		}
		
		if (taxon == null)
			throw new TaxomaticException("Could not find taxon " + taxonID + " in the trash to restore.");
		
		taxon.setState(Taxon.ACTIVE);
		
		writeTaxon(taxon, user, "Taxon restored from trash.");
	}

	public boolean permanentlyDeleteTaxon(Integer taxonID) {
		Taxon taxon = getTrashedTaxon(taxonID);
		if (taxon != null) {
			try {
				return TaxonDAO.deleteAndDissociate(taxon, session);
			} catch (PersistentException e) {
				// TODO Auto-generated catch block
				Debug.println(e);
				return false;
			}
		}
		return false;
	}

	public void trashTaxon(Taxon taxon, User user) throws TaxomaticException {
		if (taxon.getChildren().size() > 0)
			throw new TaxomaticException("You can not delete a taxon that has children.");
		
		taxon.setState(Taxon.DELETED);
		
		writeTaxon(taxon, user, "Taxon trashed.");
		AssessmentIO assessmentIO = new AssessmentIO(session);
		for (Assessment assessment : taxon.getAssessments()) {
			AssessmentIOWriteResult result = assessmentIO.trashAssessment(assessment, user);
			if (result.status.isError())
				throw new TaxomaticException("Unable to save assessment " + assessment.getId() + ", it may be locked");
		}
	}
	
	public List<Taxon> search(SearchQuery query) {
		Disjunction disjunction = Restrictions.disjunction(); 
		boolean hasQuery = false;
								
		if (query.isCommonName()) {
			hasQuery = true;
			disjunction.add(Restrictions.ilike("CommonNames.name", clean(query.getQuery()), MatchMode.ANYWHERE));
		}
		
		if (query.isSynonym()) {
			hasQuery = true;
			disjunction.add(Restrictions.ilike("Synonyms.friendlyName", clean(query.getQuery()), MatchMode.ANYWHERE));
		}			
			
		if (query.isScientificName()) {
			hasQuery = true;
			disjunction.add(Restrictions.ilike("friendlyName", clean(query.getQuery()), MatchMode.ANYWHERE));
		}
		
		//TODO: add support for countries and assessors
		
		final List<Taxon> taxa;
		if (hasQuery) {
			TaxonCriteria search = new TaxonCriteria(session.createCriteria(Taxon.class)
				.createAlias("CommonNames", "CommonNames", Criteria.LEFT_JOIN)
				.createAlias("Synonyms", "Synonyms", Criteria.LEFT_JOIN)
				.add(disjunction)
				.addOrder(Order.asc("friendlyName"))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY));
			search.createTaxonLevelCriteria().level.ge(query.getLevel());
			
			taxa = Arrays.asList(search(search));
		}
		else
			taxa = new ArrayList<Taxon>();
		
		return taxa;
	}

	public Taxon[] search(TaxonCriteria criteria) {
		return TaxonDAO.getTaxonByCriteria(criteria);
	}
	
	private String clean(String value) {
		return SIS.get().getQueries().cleanSearchTerm(value);
	}

	public Taxon readTaxonByName(String kingdomName, String name) {
		return readTaxonByName(kingdomName, name, null);
	}
	
	public Taxon readTaxonByName(String kingdomName, String name, TaxonLevel level) {
		return readTaxonByName(kingdomName, name, level, true);
	}
	
	public Taxon readTaxonByName(String kingdomName, String name, TaxonLevel level, boolean isCaseSensitive) {
		TaxonCriteria criteria = new TaxonCriteria(session);
		if (isCaseSensitive)
			criteria.friendlyName.eq(name);
		else
			criteria.friendlyName.ilike(name);
		
		if (level != null)
			criteria.createTaxonLevelCriteria().level.eq(level.getLevel());
		
		Taxon[] taxa = TaxonDAO.getTaxonByCriteria(criteria);
		for (Taxon taxon : taxa)
			if (taxon.getKingdomName().equalsIgnoreCase(kingdomName))
				return taxon;
		
		return null;
	}
	
	public boolean hasDuplicate(Taxon taxon) {
		if (taxon.getTaxonStatus() != null && TaxonStatus.STATUS_NOT_ASSIGNED.equals(taxon.getStatusCode()))
			return false;
		
		TaxonCriteria criteria = new TaxonCriteria(session);
		criteria.id.ne(taxon.getId());
		criteria.friendlyName.eq(taxon.getFriendlyName());
		criteria.createTaxonLevelCriteria().level.eq(taxon.getLevel());
		criteria.createTaxonStatusCriteria().code.ne(TaxonStatus.STATUS_NOT_ASSIGNED);
		
		Taxon[] duplicates = TaxonDAO.getTaxonByCriteria(criteria);
		for (Taxon possibleDuplicate : duplicates)
			if (possibleDuplicate.getKingdomName().equalsIgnoreCase(taxon.getKingdomName()))
				return true;
		
		return false;
	}

	public Taxon[] getTaxaByStatus(String code) {
		return getTaxaByStatus(TaxonStatus.fromCode(code));
	}
	
	public Taxon[] getTaxaByStatus(TaxonStatus status) {
		TaxonCriteria criteria = new TaxonCriteria(session);
		TaxonStatusCriteria c = criteria.createTaxonStatusCriteria();
		c.id.eq(status.getId());
		
		criteria.addOrder(Order.asc("friendlyName"));
		
		return criteria.listTaxon();
	}

	public Taxon[] getChildrenOfTaxon(Integer parentTaxonID) throws PersistentException {
		TaxonCriteria criteria = new TaxonCriteria(session);
		TaxonCriteria parentCriteria = criteria.createParentCriteria();
		parentCriteria.id.eq(parentTaxonID);
		return TaxonDAO.getTaxonByCriteria(criteria);
	}

	public List<Taxon> getChildrenOfTaxonRecurisvely(Integer parentTaxonID) throws PersistentException {
		ArrayList<Taxon> children = new ArrayList<Taxon>();
		children.addAll(Arrays.asList(getChildrenOfTaxon(parentTaxonID)));
		for (int i = 0; i < children.size(); i++) {
			children.addAll(children.get(i).getChildren());
		}
		return children;
	}

}
