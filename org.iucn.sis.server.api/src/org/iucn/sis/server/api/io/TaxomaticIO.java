package org.iucn.sis.server.api.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.TransientObjectException;
import org.hibernate.criterion.Restrictions;
import org.iucn.sis.server.api.io.taxomatic.DemoteNodeOperation;
import org.iucn.sis.server.api.io.taxomatic.LateralMoveOperation;
import org.iucn.sis.server.api.io.taxomatic.MergeNodesOperation;
import org.iucn.sis.server.api.io.taxomatic.MergeUpInfrarankOperation;
import org.iucn.sis.server.api.io.taxomatic.PromoteNodeOperation;
import org.iucn.sis.server.api.io.taxomatic.SplitNodesOperation;
import org.iucn.sis.server.api.locking.TaxonLockAquirer;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.utils.TaxaComparators;
import org.iucn.sis.server.api.utils.TaxomaticException;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.Infratype;
import org.iucn.sis.shared.api.models.Synonym;
import org.iucn.sis.shared.api.models.TaxomaticHistory;
import org.iucn.sis.shared.api.models.TaxomaticOperation;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonLevel;
import org.iucn.sis.shared.api.models.User;
import org.iucn.sis.shared.api.models.fields.ProxyField;
import org.iucn.sis.shared.api.utils.CanonicalNames;

public class TaxomaticIO {

	public final static String MERGE = "MERGE";
	public final static String MERGE_UP_INFRARANK = "MERGE UP INFRARANK";
	public final static String SPLIT = "SPLIT";
	public final static String LATERAL_MOVE = "LATERAL MOVE";
	public final static String PROMOTE = "PROMOTE";
	public final static String DEMOTE = "DEMOTE";

	/*protected String lastOperationUsername;
	protected String lastOperationType;
	protected Date lastOperationDate;
	protected Set<Integer> taxaIDsChanged;*/
	
	private final TaxonIO taxonIO;
	private final AssessmentIO assessmentIO;
	private final Session session;

	public TaxomaticIO(Session session) {
		this.taxonIO = new TaxonIO(session);
		this.assessmentIO = new AssessmentIO(session);
		this.session = session;
	}

	public void demoteSpecies(Taxon taxon, Taxon newParent, User user) throws TaxomaticException {
		DemoteNodeOperation operation = new DemoteNodeOperation(session, user, taxon, newParent);
		operation.run();
	}

	private String generateRLHistoryText(Taxon taxon) {
		String fullName = "<i>";
		boolean isPlant = taxon.getFootprint().length >= 1 ? taxon.getFootprint()[0].equalsIgnoreCase("plantae")
				: false;

		for (int i = 5; i < (taxon.getLevel() >= TaxonLevel.SUBPOPULATION ? taxon.getLevel() - 1 : taxon.getLevel()); i++)
			fullName += taxon.getFootprint()[i] + " ";

		if (taxon.getInfratype() != null) {
			if (isPlant && taxon.getInfratype().getId() == Infratype.INFRARANK_TYPE_SUBSPECIES)
				fullName += "subsp. ";
			else
				fullName += taxon.getInfratype().getCode() + " ";
		}

		if (taxon.getLevel() == TaxonLevel.SUBPOPULATION || taxon.getLevel() == TaxonLevel.INFRARANK_SUBPOPULATION)
			fullName += "</i>" + taxon.getName().replace("ssp.", "").replace("var.", "").replace("fma.", "").trim();
		else
			fullName += taxon.getName().replace("ssp.", "").replace("var.", "").replace("fma.", "").trim() + "</i>";

		if (isPlant) {
			fullName = fullName.replace("subsp.", "</i> subsp. <i>");
			fullName = fullName.replace("var.", "</i> var. <i>");
			fullName = fullName.replace("fma.", "</i> fma. <i>");
		}
		return fullName;
	}

	boolean isTaxomaticOperationNecessary(Taxon taxonToSave, Taxon oldTaxon) {
		
		return !(taxonToSave != null && oldTaxon != null && oldTaxon.getName().equals(taxonToSave.getName()) && 
				oldTaxon.getParentId() == taxonToSave.getParentId() && 
				oldTaxon.getTaxonLevel().getLevel() == taxonToSave.getTaxonLevel().getLevel() );
	}
	
	public List<TaxomaticOperation> getTaxomaticHistory(Integer taxonID) throws TaxomaticException {
		Taxon taxon = taxonIO.getTaxon(taxonID);
		if (taxon == null)
			throw new TaxomaticException("No taxon found for id " + taxonID);
			
		return getTaxomaticHistory(taxon);
	}
	
	@SuppressWarnings("unchecked")
	public List<TaxomaticOperation> getTaxomaticHistory(Taxon taxon) {
		List<TaxomaticOperation> list = new ArrayList<TaxomaticOperation>();
		
		List<TaxomaticHistory> history = 
			session.createCriteria(TaxomaticHistory.class).add(Restrictions.eq("taxon", taxon)).list();
		
		for (TaxomaticHistory current : history)
			list.add(current.getOperation());
		
		Collections.sort(list);
		
		return list;
		
		/*String query = "SELECT * FROM taxomaticoperation " +
		"JOIN taxomatichistory ON taxomaticoperation.id = taxomatichistory.taxomaticoperationid " +
		"WHERE taxomatichistory.taxonid = " + taxon.getId() + " " + 
		"ORDER BY taxomaticoperation.date";
		
		return session.createSQLQuery(query).addEntity(TaxomaticOperation.class).list();*/	
	}

	public void mergeTaxa(List<Taxon> mergedTaxa, Taxon mainTaxa, User user) throws TaxomaticException {
		MergeNodesOperation operation = new MergeNodesOperation(session, user, mainTaxa, mergedTaxa);
		operation.run();
	}

	public void mergeUpInfraranks(List<Taxon> taxa, Taxon species, User user) throws TaxomaticException {
		MergeUpInfrarankOperation operation = new MergeUpInfrarankOperation(session, user, species, taxa);
		operation.run();
	}

	/**
	 * Move the children taxa from current parent to new parent, important that
	 * the new parent and the old parent are of the same taxon level
	 * 
	 * @param newParent
	 * @param children
	 * @param user
	 * @return
	 */
	public void moveTaxa(Taxon newParent, Collection<Taxon> children, User user) throws TaxomaticException {
		LateralMoveOperation operation = new LateralMoveOperation(session, user, newParent, children);
		operation.run();
	}

	/**
	 * 
	 * Moves the child from its current parent to the newParent. If the name has
	 * changed, then a synonym is added with the status provided 
	 * 
	 * Returns a set of taxa that needs to be saved.
	 * 
	 * 
	 * @param newParent
	 * @param child
	 * @param synStatus
	 *            -- if null no synymn is created
	 * @param updateAssessments
	 *            -- if true, then updates teh assessment names
	 * @return
	 */
	public Set<Taxon> moveTaxon(Taxon newParent, Taxon child, User user, String synStatus, boolean updateAssessments) {
		Set<Taxon> changed = new HashSet<Taxon>();
		Taxon oldParent = child.getParent();

		if (synStatus != null) {
			String oldChildName = child.generateFullName();
			child.setParent(newParent);
			String newChildName = child.generateFullName();

			if (!oldChildName.equals(newChildName)) {

				ArrayList<Taxon> footprints = new ArrayList<Taxon>();
				footprints.add(child);
				while (!footprints.isEmpty()) {
					Taxon current = footprints.remove(0);
					footprints.addAll(current.getChildren());

					// DO NOT SWITCH ORDER OF SETTING PARENT, ORDER MATTERS
					child.setParent(newParent);
					String newName = current.generateFullName();
					child.setParent(oldParent);
					String oldName = current.generateFullName();

					if (!oldName.equals(newName)) {
						changed.add(current);
						if (synStatus != null) {
							Synonym syn = Taxon.synonymizeTaxon(current);
							syn.setStatus(synStatus);
							current.getSynonyms().add(syn);
						}
						if (updateAssessments) {
							updateRLHistoryText(current, user);
						}
						child.setFriendlyName(newName);

					}
				}
			}
		}
		changed.add(child);
		changed.add(oldParent);
		changed.add(newParent);

		child.setParent(newParent);
		oldParent.getChildren().remove(child);
		newParent.getChildren().add(child);

		return changed;
	}

	public void performTaxomaticUndo(User user) throws TaxomaticException {
		/*
		 * TODO: this needs to be implemented at the database level. 
		 */
		throw new TaxomaticException("Undo operation not supported.", true);
	}

	/**
	 * Promotes the infrarank to a species. Once promoted, its former
	 * grandparent (the genus) will be its parent, and all of its children will
	 * have a taxon level of subpopulation.
	 * 
	 * @param taxon
	 * @param user
	 * @return
	 */
	public void promoteInfrarank(Taxon taxon, User user) throws TaxomaticException {
		PromoteNodeOperation operation = new PromoteNodeOperation(session, user, taxon);
		operation.run();
	}

	public void saveNewTaxon(Taxon taxon, User user) throws TaxomaticException {
		if (taxon.getId() == 0 || taxonIO.getTaxon(taxon.getId()) == null) {
			if (taxon.getFootprint() != null && taxon.getFootprint().length > 0) {
				final Taxon existing = taxonIO.readTaxonByName(taxon.getKingdomName(), 
						taxon.getFriendlyName(), taxon.getTaxonLevel());
				
				if (existing != null)
					throw new TaxomaticException("The taxon " + existing.getFriendlyName() + 
						" already exists within this kingdom at the " + taxon.getTaxonLevel().getName() + 
						", preventing the creation of new taxon " + taxon.getFriendlyName());
			}
			
			writeTaxon(taxon, null, user, "Taxon created.", false);
		}
		else
			throw new TaxomaticException("This taxon already exists and can not be created.");
	}

	/**
	 * Removes all children of the oldTaxon and places them into the the new
	 * Parent which is stored in the parentTOChildren map
	 * 
	 * @param oldTaxon
	 * @param user
	 * @param parentToChildren
	 * @return
	 */
	public void splitNodes(Taxon oldTaxon, User user, HashMap<Taxon, List<Taxon>> parentToChildren) throws TaxomaticException {
		SplitNodesOperation operation = new SplitNodesOperation(session, user, oldTaxon, parentToChildren);
		operation.run();
	}
	
	public void updateAndSave(Collection<Taxon> taxaToSave, User user, boolean requireLocking) throws TaxomaticException {
		TaxonLockAquirer acquirer = new TaxonLockAquirer(taxaToSave);
		if (requireLocking)
			acquirer.aquireLocks(user.getUsername());

		if (requireLocking && !acquirer.isSuccess())
			throw new TaxomaticException("Failed to acquire lock to save taxa.", false);
		
		ArrayList<Taxon> taxa = new ArrayList<Taxon>(taxaToSave);
		Map<Integer, Taxon> idsToTaxon = new HashMap<Integer, Taxon>();
		for (Taxon taxon : taxaToSave)
			idsToTaxon.put(taxon.getId(), taxon);
		
		Collections.sort(taxa, TaxaComparators.getTaxonComparatorByLevel());
			
		try {
			for (Taxon taxon : taxa) {
				//UPDATE FRIENDLY NAME
				if (idsToTaxon.containsKey(taxon.getParent().getId())) {
					taxon.setParent(idsToTaxon.get(taxon.getParent().getId()));
					taxon.getParent().correctFullName();
					Debug.println("parent was updated with fullname " + taxon.getParent().getName());
				}
				Debug.println("this is full name before " + taxon.getFullName());
				taxon.correctFullName();					
				Debug.println("this is full name after " + taxon.getFullName());
				//ADD EDIT	CS: No need, will happen later...					
				/*Edit edit = new Edit("Taxomatic operation performed. See taxomatic history for details.");
				edit.setUser(user);
				edit.setCreatedDate(date);
				edit.getTaxon().add(taxon);
				
				taxon.getEdits().add(edit);*/
				taxon.toXML();
				
				writeTaxon(taxon, user, "Taxomatic operation performed. See taxomatic history for details.");
			}
		} finally {
			acquirer.releaseLocks();
		}
	}
	
	/**
	 * Updates the RL history on all published assessments associated with the
	 * taxon
	 * 
	 * IMPORTANT -- Must be called before parent changed and before level
	 * changed
	 * 
	 */
	public void updateRLHistoryText(Taxon taxon, User user) {
		for (Assessment current : assessmentIO.readPublishedAssessmentsForTaxon(taxon)) {
			//TODO: pull the field name from CanonicalNames
			Field field = current.getField(CanonicalNames.RedListHistory);
			if (field == null)
				field = new Field(CanonicalNames.RedListHistory, null);
			
			ProxyField proxy = new ProxyField(field);
			String text = proxy.getTextPrimitiveField("value");
			if (text == null || text.equals("")) {
				proxy.setTextPrimitiveField("value", "as " + generateRLHistoryText(taxon));
				if (field.getAssessment() == null) {
					field.setAssessment(current);
					current.getField().add(field);
				}
			}
			
			assessmentIO.writeAssessment(current, user, "RL History Text Updated.", false);
		}
	}
	
	void writeTaxon(Taxon taxonToSave, Taxon oldTaxon, User user, String reason, boolean requireLocking) throws TaxomaticException {
		if (oldTaxon == null || !isTaxomaticOperationNecessary(taxonToSave, oldTaxon)) {
			taxonIO.writeTaxon(taxonToSave, oldTaxon, reason, user);
		} else {
			// TRY TO AQUIRE LOCKS
			List<Taxon> taxaToSave;
			try {
				taxonToSave.toXML();
				taxaToSave = taxonIO.getChildrenOfTaxonRecurisvely(taxonToSave.getId());
			} catch (PersistentException e) {
				throw new TaxomaticException("Failed to find child to write taxon.", e);
			} catch (TransientObjectException e) {
				throw new TaxomaticException("Failed to find child to write taxon.", e);
			}
			taxaToSave.add(0, taxonToSave);
			
			updateAndSave(taxaToSave, user, requireLocking);
		}
	}
	
	public void writeTaxon(Taxon taxonToSave, User user, String reason) throws TaxomaticException {
		Taxon oldTaxon = taxonIO.getTaxon(taxonToSave.getId());
		if (oldTaxon == null)
			throw new TaxomaticException("This taxa could not be found.", false);
		
		writeTaxon(taxonToSave, oldTaxon, user, reason, true);
	}

}
