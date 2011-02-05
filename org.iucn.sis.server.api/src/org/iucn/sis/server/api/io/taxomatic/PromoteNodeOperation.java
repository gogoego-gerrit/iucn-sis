package org.iucn.sis.server.api.io.taxomatic;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.Session;
import org.iucn.sis.server.api.io.TaxomaticIO;
import org.iucn.sis.server.api.utils.TaxomaticException;
import org.iucn.sis.shared.api.models.Synonym;
import org.iucn.sis.shared.api.models.TaxomaticOperation;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonLevel;
import org.iucn.sis.shared.api.models.User;

/**
 * For promotions an infrarank is being moved into a species. The infrarank
 * will just be promoted to be siblings with its previous parent, and all
 * of the subpopulations of the infrarank are moved to be subpopulations of
 * the newly promoted species. Besides this, the information about the the
 * promoted species is unchanged.
 * 
 * if you promote a sub-something to a -something, the -something isn't
 * getting a synonym pointing to the promoted sub-something. Check to make
 * sure it puts the authority in the synonym.
 * 
 * This clas promotes the infrarank to a species. Once promoted, its former
 * grandparent (the genus) will be its parent, and all of its children will
 * have a taxon level of subpopulation.
 * 
 * @author adam.schwartz
 * @author carl.scott
 */
public class PromoteNodeOperation extends TaxomaticWorker {

	private final TaxomaticOperation operation;
	private final Taxon taxonToPromote;
	
	public PromoteNodeOperation(Session session, TaxomaticOperation operation) {
		super(session, TaxomaticIO.PROMOTE);
		
		this.operation = operation;
		this.taxonToPromote = loadTaxonToPromote(operation.getInstructions());
	}
	
	public PromoteNodeOperation(Session session, User user, Taxon taxonToPromote) {
		super(session, TaxomaticIO.PROMOTE);
		
		TaxomaticOperation operation = new TaxomaticOperation();
		operation.setDate(new Date());
		operation.setInstructions(generateInstructions(taxonToPromote));
		operation.setDetails(generateDetails(taxonToPromote));
		operation.setOperation(TaxomaticIO.PROMOTE);
		operation.setUser(user);
		
		this.operation = operation;
		this.taxonToPromote = taxonToPromote;
	}
	
	private String generateInstructions(Taxon taxonToPromote) {
		return Integer.toString(taxonToPromote.getId());
	}
	
	private String generateDetails(Taxon taxonToPromote) {
		return "Promoted infrarank " + taxonToPromote.getFullName() + " to species.";
	}
	
	private Taxon loadTaxonToPromote(String instructions) {
		return taxonIO.getTaxon(Integer.valueOf(instructions));
	}

	@Override
	public String getUndoOperationName() {
		return TaxomaticIO.DEMOTE;
	}

	@Override
	public void run() throws TaxomaticException {
		if (taxonToPromote.getTaxonLevel().getLevel() != (TaxonLevel.INFRARANK))
			throw new TaxomaticException("You should only be promoting an infrarank.");
		
		Set<Taxon> changed = new HashSet<Taxon>();
		changed.add(taxonToPromote);
		changed.addAll(taxonToPromote.getChildren());

		for (Taxon current : changed) {
			Synonym synonym = Taxon.synonymizeTaxon(current);
			synonym.setStatus(Synonym.NEW);
			current.getSynonyms().add(synonym);
			
			taxomaticIO.updateRLHistoryText(current, operation.getUser());

			//Ensure this is called after updating RL History Text
			if (current.getLevel() == TaxonLevel.INFRARANK)
				current.setTaxonLevel(TaxonLevel.getTaxonLevel(TaxonLevel.SPECIES));
			else if (current.getLevel() == TaxonLevel.INFRARANK_SUBPOPULATION)
				current.setTaxonLevel(TaxonLevel.getTaxonLevel(TaxonLevel.SUBPOPULATION));
			else
				throw new TaxomaticException("You should only be promoting an infrarank.");
		}

		taxonToPromote.setInfratype(null);
		
		Taxon oldParent = taxonToPromote.getParent();
		Taxon newParent = oldParent.getParent();
		
		taxonToPromote.setParent(newParent);
		
		oldParent.getChildren().remove(taxonToPromote);
		newParent.getChildren().add(taxonToPromote);
		
		changed.add(oldParent);
		changed.add(newParent);

		taxomaticIO.updateAndSave(changed, operation.getUser(), true);
		
		recordOperationHistory(changed, operation);
	}

}
