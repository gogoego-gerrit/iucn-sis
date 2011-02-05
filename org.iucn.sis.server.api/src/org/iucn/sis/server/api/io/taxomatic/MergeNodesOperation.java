package org.iucn.sis.server.api.io.taxomatic;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Session;
import org.iucn.sis.server.api.io.TaxomaticIO;
import org.iucn.sis.server.api.utils.TaxomaticException;
import org.iucn.sis.shared.api.models.Synonym;
import org.iucn.sis.shared.api.models.TaxomaticOperation;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonStatus;
import org.iucn.sis.shared.api.models.User;

/**
 * For merge the taxomatic is designed so that you select one or more
 * species ( say Old1 and Old2) to merge into a species (New1, which has
 * already been previously created). Old1 and Old 2 will then receive a
 * synonym to New1 and Old1 and Old2 will both be marked as deprecated. None
 * of the information that was associated with Old1 or Old2 are moved into
 * New1. New1 will receive deprecated synonyms to both Old1 and Old2.
 * 
 * @author adam.schwartz
 * @author carl.scott
 */
public class MergeNodesOperation extends TaxomaticWorker {
	
	private final TaxomaticOperation operation;
	private final Taxon mergeTarget;
	private final List<Taxon> taxaToMerge;
	
	public MergeNodesOperation(Session session, TaxomaticOperation operation) {
		super(session, TaxomaticIO.MERGE);
		this.operation = operation;
		this.mergeTarget = loadMergeTarget(operation.getInstructions());
		this.taxaToMerge = loadTaxaToMerge(operation.getInstructions());
	}
	
	public MergeNodesOperation(Session session, User user, Taxon mergeTarget, List<Taxon> taxaToMerge) {
		super(session, TaxomaticIO.MERGE);
		TaxomaticOperation operation = new TaxomaticOperation();
		operation.setDate(new Date());
		operation.setInstructions(generateInstructions(mergeTarget, taxaToMerge));
		operation.setDetails(generateDetails(mergeTarget, taxaToMerge));
		operation.setOperation(TaxomaticIO.MERGE);
		operation.setUser(user);
		
		this.operation = operation;
		this.mergeTarget = mergeTarget;
		this.taxaToMerge = taxaToMerge;
	}
	
	private String generateDetails(Taxon mergeTarget, List<Taxon> taxaToMerge) {
		StringBuilder builder = new StringBuilder();
		builder.append("Merged the following taxa into " + mergeTarget.getFullName() + ": ");
		for (Iterator<Taxon> iter = taxaToMerge.iterator(); iter.hasNext(); ) {
			builder.append(iter.next().getFullName());
			if (iter.hasNext())
				builder.append(", ");
		}
		return builder.toString();
	}
	
	/*
	 * mergeTarget,toMerge,toMerge,toMerge
	 */
	private String generateInstructions(Taxon mergeTarget, List<Taxon> taxaToMerge) {
		StringBuilder builder = new StringBuilder();
		builder.append(mergeTarget.getId());
		for (Taxon taxon : taxaToMerge) {
			builder.append(',');
			builder.append(taxon.getId());
		}
		return builder.toString();
	}
	
	private Taxon loadMergeTarget(String instructions) {
		return taxonIO.getTaxon(Integer.valueOf(instructions.split(",")[0]));
	}
	
	private List<Taxon> loadTaxaToMerge(String instructions) {
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
		for (Taxon mergedTaxon : taxaToMerge) {
			mergedTaxon.setStatus(TaxonStatus.STATUS_SYNONYM);
			{
				Synonym synonym = Taxon.synonymizeTaxon(mergedTaxon);
				synonym.setStatus(Synonym.MERGE);
				synonym.setTaxon(mergeTarget);
				
				mergeTarget.getSynonyms().add(synonym);
			}
			{
				Synonym synonym = Taxon.synonymizeTaxon(mergeTarget);
				synonym.setStatus(Synonym.MERGE);
				synonym.setTaxon(mergedTaxon);
				
				mergedTaxon.getSynonyms().add(synonym);
			}
			for (Taxon child : mergedTaxon.getChildren()) {
				child.setParent(mergeTarget);
				mergeTarget.getChildren().add(child);
				taxaToSave.add(child);
			}
			taxaToSave.add(mergedTaxon);
		}
		taxaToSave.add(mergeTarget);
		
		taxomaticIO.updateAndSave(taxaToSave, operation.getUser(), true);
		
		recordOperationHistory(taxaToSave, operation);
	}

}
