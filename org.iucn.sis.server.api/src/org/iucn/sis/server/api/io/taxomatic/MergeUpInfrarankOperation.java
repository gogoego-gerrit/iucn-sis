package org.iucn.sis.server.api.io.taxomatic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Session;
import org.iucn.sis.server.api.io.TaxomaticIO;
import org.iucn.sis.server.api.utils.TaxomaticException;
import org.iucn.sis.shared.api.models.Synonym;
import org.iucn.sis.shared.api.models.TaxomaticOperation;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonLevel;
import org.iucn.sis.shared.api.models.TaxonStatus;
import org.iucn.sis.shared.api.models.User;

/**
 * Given the speciesID and the infraranks ids, merges the infraranks into 
 * the species and moves the subpopulations of the infraranks to be 
 * subpopulations of the species. Places a synonym in the
 * infrarank, and in the subpopulations.
 * 
 */
public class MergeUpInfrarankOperation extends TaxomaticWorker {

	private final TaxomaticOperation operation;
	private final Taxon species;
	private final Collection<Taxon> infraranks;
	
	public MergeUpInfrarankOperation(Session session, TaxomaticOperation operation) {
		super(session, TaxomaticIO.MERGE_UP_INFRARANK);
		
		this.operation = operation;
		this.species = loadSpecies(operation.getInstructions());
		this.infraranks = loadInfraranks(operation.getInstructions());
	}
	
	public MergeUpInfrarankOperation(Session session, User user, Taxon species, Collection<Taxon> infraranks) {
		super(session, TaxomaticIO.MERGE_UP_INFRARANK);
		
		TaxomaticOperation operation = new TaxomaticOperation();
		operation.setDate(new Date());
		operation.setInstructions(generateInstructions(species, infraranks));
		operation.setDetails(generateDetails(species, infraranks));
		operation.setOperation(TaxomaticIO.MERGE_UP_INFRARANK);
		operation.setUser(user);
		
		this.operation = operation;
		this.species = species;
		this.infraranks = infraranks;
	}

	private String generateInstructions(Taxon species, Collection<Taxon> infraranks) {
		StringBuilder builder = new StringBuilder();
		builder.append(species.getId());
		for (Taxon taxon : infraranks) {
			builder.append(',');
			builder.append(taxon.getId());
		}
		return builder.toString();
	}
	
	private String generateDetails(Taxon species, Collection<Taxon> infraranks) {
		StringBuilder builder = new StringBuilder();
		builder.append("Merged the following infraranks into species " + species.getFullName() + ": ");
		for (Iterator<Taxon> iter = infraranks.iterator(); iter.hasNext(); ) {
			builder.append(iter.next().getFullName());
			if (iter.hasNext())
				builder.append(", ");
		}
		return builder.toString();
	}
	
	private Taxon loadSpecies(String instructions) {
		return taxonIO.getTaxon(Integer.valueOf(instructions.split(",")[0]));
	}
	
	private Collection<Taxon> loadInfraranks(String instructions) {
		List<Taxon> list = new ArrayList<Taxon>();
		String[] split = instructions.split(",");
		for (int i = 1; i < split.length; i++) {
			list.add(taxonIO.getTaxon(Integer.valueOf(split[i])));
		}
		return list;
	}
	
	@Override
	public String getUndoOperationName() {
		return TaxomaticIO.SPLIT;
	}

	@Override
	public void run() throws TaxomaticException {
		List<Taxon> taxaToSave = new ArrayList<Taxon>();
		
		for (Taxon mergedTaxon : infraranks) {
			mergedTaxon.setStatus(TaxonStatus.STATUS_SYNONYM);
			{
				Synonym synonym = Taxon.synonymizeTaxon(mergedTaxon);
				synonym.setStatus(Synonym.MERGE);
				synonym.setTaxon(species);
				
				species.getSynonyms().add(synonym);
			}
			{
				Synonym synonym = Taxon.synonymizeTaxon(species);
				synonym.setStatus(Synonym.MERGE);			
				synonym.setTaxon(mergedTaxon);
				
				mergedTaxon.getSynonyms().add(synonym);
			}
			
			for (Taxon infrarankChild : mergedTaxon.getChildren()) {
				// child.setTaxa(mainTaxa);
				// mainTaxa.getParent().add(child);
				String oldName = infrarankChild.generateFullName();
				infrarankChild.setTaxonLevel(TaxonLevel.getTaxonLevel(TaxonLevel.SUBPOPULATION));
				String newFullName = infrarankChild.generateFullName();
				if (!oldName.equals(newFullName)) {
					Synonym synonym = Taxon.synonymizeTaxon(infrarankChild);
					if (infrarankChild.getTaxonomicAuthority() != null) {
						synonym.setAuthority(infrarankChild.getTaxonomicAuthority(), infrarankChild.getLevel());
					}
					infrarankChild.getSynonyms().add(synonym);
					infrarankChild.setFriendlyName(newFullName);
				}
				taxaToSave.add(infrarankChild);
			}
			taxaToSave.add(mergedTaxon);
		}
		taxaToSave.add(species);

		taxomaticIO.updateAndSave(taxaToSave, operation.getUser(), true);
		
		recordOperationHistory(taxaToSave, operation);
	}

}
