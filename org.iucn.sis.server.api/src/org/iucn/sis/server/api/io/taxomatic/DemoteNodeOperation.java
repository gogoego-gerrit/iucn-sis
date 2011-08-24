package org.iucn.sis.server.api.io.taxomatic;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.Session;
import org.iucn.sis.server.api.io.InfratypeIO;
import org.iucn.sis.server.api.io.TaxomaticIO;
import org.iucn.sis.server.api.utils.TaxomaticException;
import org.iucn.sis.shared.api.models.Infratype;
import org.iucn.sis.shared.api.models.Synonym;
import org.iucn.sis.shared.api.models.TaxomaticOperation;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonLevel;
import org.iucn.sis.shared.api.models.User;

/**
 * For demotions, a species is being demoted (Demoted1) to infrarank. The
 * user selects a species for which the species will be the new parent of
 * Demoted1. The children of Demoted1. The subpopulations of Demoted1 is
 * moved to the status of an infrarank subpopulation. If the species had
 * infrarank, the species can not be demoted until the infrarank has already
 * been moved out.
 * 
 * when demoting a -something to a sub-something, the sub-something gets a
 * synonym pointing to the demoted -something
 * 
 */
public class DemoteNodeOperation extends TaxomaticWorker {

	private final TaxomaticOperation operation;
	private final Taxon demotedSpecies;
	private final Taxon newParent;
	private final InfratypeIO infratypeIO;
	
	public DemoteNodeOperation(Session session, TaxomaticOperation operation) {
		super(session, TaxomaticIO.DEMOTE);
		
		this.operation = operation;
		this.demotedSpecies = loadDemotedSpecies(operation.getInstructions());
		this.newParent = loadNewParent(operation.getInstructions());
		this.infratypeIO = new InfratypeIO(session);
	}
	
	public DemoteNodeOperation(Session session, User user, Taxon demotedSpecies, Taxon newParent) {
		super(session, TaxomaticIO.DEMOTE);
		
		TaxomaticOperation operation = new TaxomaticOperation();
		operation.setDate(new Date());
		operation.setInstructions(generateInstructions(demotedSpecies, newParent));
		operation.setDetails(generateDetails(demotedSpecies, newParent));
		operation.setOperation(TaxomaticIO.DEMOTE);
		operation.setUser(user);
		
		this.operation = operation;
		this.demotedSpecies = demotedSpecies;
		this.newParent = newParent;
		this.infratypeIO = new InfratypeIO(session);
	}
	
	private String generateDetails(Taxon demotedSpecies, Taxon newParent) {
		return "Demoted Species " + demotedSpecies.getFullName() + " to Infrarank of " + newParent.getFullName() + "."; 
	}
	
	private String generateInstructions(Taxon demotedSpecies, Taxon newParent) {
		return demotedSpecies.getId() + "," + newParent.getId();
	}
	
	private Taxon loadDemotedSpecies(String instructions) {
		return taxonIO.getTaxon(Integer.valueOf(instructions.split(",")[0]));
	}
	
	private Taxon loadNewParent(String instructions) {
		return taxonIO.getTaxon(Integer.valueOf(instructions.split(",")[1]));
	}

	@Override
	public String getUndoOperationName() {
		return TaxomaticIO.PROMOTE;
	}

	@Override
	public void run() throws TaxomaticException {
		if (demotedSpecies.getLevel() != TaxonLevel.SPECIES || newParent.getLevel() != TaxonLevel.SPECIES)
			throw new TaxomaticException("You can only demote a species to an infrarank");

		User user = operation.getUser();
		
		Set<Taxon> taxa = new HashSet<Taxon>();
		Collection<Taxon> children = demotedSpecies.getChildren();
		for (Taxon child : children) {
			if (child.getLevel() != TaxonLevel.SUBPOPULATION)
				throw new TaxomaticException("The demoted species is not allowed to have a " + TaxonLevel.getDisplayableLevel(child.getLevel()).toLowerCase());

			Synonym synonym = Taxon.synonymizeTaxon(child);
			synonym.setStatus(Synonym.NEW);
			child.getSynonyms().add(synonym);
			
			taxomaticIO.updateRLHistoryText(child, user);
			
			//Ensure this is called after upading the RL History Text
			child.setTaxonLevel(TaxonLevel.getTaxonLevel(TaxonLevel.INFRARANK_SUBPOPULATION));
			
			taxa.add(child);
		}

		Synonym synonym = Taxon.synonymizeTaxon(demotedSpecies);
		synonym.setStatus(Synonym.NEW);
		demotedSpecies.getSynonyms().add(synonym);
		
		taxomaticIO.updateRLHistoryText(demotedSpecies, user);
		
		//Ensure this is called after upading the RL History Text
		demotedSpecies.setTaxonLevel(TaxonLevel.getTaxonLevel(TaxonLevel.INFRARANK));
		demotedSpecies.setInfratype(infratypeIO.getInfratype(Infratype.INFRARANK_TYPE_SUBSPECIES));
		demotedSpecies.setParent(newParent);
		
		newParent.getChildren().add(demotedSpecies);
		demotedSpecies.getParent().getChildren().remove(demotedSpecies);
		
		taxa.add(demotedSpecies);
		taxa.add(newParent);

		taxomaticIO.updateAndSave(taxa, user, true);
			
		recordOperationHistory(taxa, operation);
	}

}
