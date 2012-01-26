package org.iucn.sis.shared.conversions;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.iucn.sis.server.api.io.UserIO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.models.Edit;
import org.iucn.sis.shared.api.models.Infratype;
import org.iucn.sis.shared.api.models.Notes;
import org.iucn.sis.shared.api.models.Synonym;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonLevel;
import org.iucn.sis.shared.api.models.User;
import org.iucn.sis.shared.helpers.SynonymData;
import org.iucn.sis.shared.helpers.TaxonNode;
import org.iucn.sis.shared.helpers.TaxonNodeFactory;

import com.solertium.lwxml.java.JavaNativeDocument;
import com.solertium.lwxml.shared.NativeDocument;

public class SynonymConverter extends GenericConverter<String> {
	
	private UserIO userIO;
	
	private AtomicInteger taxaConverted;
	
	public SynonymConverter() {
		super();
		setClearSessionAfterTransaction(true);
	}
	
	public void run() throws Exception {
		/*
		 * First, add 'em all, no relationships.
		 */
		userIO = new UserIO(session);
		taxaConverted = new AtomicInteger(0);
		
		Map<Integer, Integer> childToParent = new HashMap<Integer, Integer>();
		final User user = userIO.getUserFromUsername("admin");
		final AtomicInteger converted = new AtomicInteger(0);
		
		File folder = new File(data + "/HEAD/browse/nodes");
		
		readFolder(childToParent, user, converted, folder);
		
		commitAndStartTransaction();
	}
	
	private void readFolder(Map<Integer, Integer> childToParent, User user, AtomicInteger converted, File folder) throws Exception {
		for (File file : folder.listFiles()) {
			if (file.isDirectory())
				readFolder(childToParent, user, converted, file);
			else if (file.getName().endsWith(".xml"))
				readFile(childToParent, user, converted, file);
		}
	}
	
	private void readFile(Map<Integer, Integer> childToParent, User user, AtomicInteger converted, File file) throws Exception {
		NativeDocument ndoc = new JavaNativeDocument();
		ndoc.parse(FileListing.readFileAsString(file));
		
		TaxonNode node = TaxonNodeFactory.createNode(ndoc);
		Taxon taxon = null;
		try {
			taxon = (Taxon) session.load(Taxon.class, Long.valueOf(node.getId()).intValue());
		} catch (Exception e) {
			printf("No taxon exists for %s, skipping", node.getId());
		}
		
		if (taxon != null) {
			List<Synonym> erroneous = convertTaxonNode(node, new Date(file.lastModified()), user);
			if (!erroneous.isEmpty()) {
				for (Synonym synonym : erroneous) {
					synonym.generateFriendlyName();
					
					taxon.getSynonyms().add(synonym);
					synonym.setTaxon(taxon);
					
					printf("Adding synonym %s to taxon %s", synonym.getFriendlyName(), taxon.getId());
				}
				
				session.update(taxon);
			}
		}
		
		if (taxaConverted.incrementAndGet() % 100 == 0) {
			commitAndStartTransaction();
			printf("Scanned %s taxa...", taxaConverted.get());
		}
	}
	
	public List<Synonym> convertTaxonNode(TaxonNode taxon, Date lastModified, User user) throws PersistentException {
		// ADD SYNONYMS
		int generationID = 1;
		List<Synonym> erroneous = new ArrayList<Synonym>();
		for (SynonymData synData : taxon.getSynonyms()) {
			TaxonLevel tl = TaxonLevel.getTaxonLevel(synData.getLevel());
			if (tl != null)
				continue;
			
			erroneous.add(convertSynonym(synData, taxon, user, generationID++));
		}
		
		return erroneous;
	}
	
	public static Synonym convertSynonym(SynonymData synData, TaxonNode taxon, User user, int generationID) {
		String[] oldVersionSplit = synData.getOldVersionName().split(" ");
		boolean isTrinomal = oldVersionSplit.length > 2;
		int level = isTrinomal ? TaxonLevel.INFRARANK : TaxonLevel.SPECIES;
		
		return convertSynonym(synData, taxon, user, generationID, level);
	}
	
	public static Synonym convertSynonym(SynonymData synData, TaxonNode taxon, User user, int generationID, int level) {
		Synonym synonym = new Synonym();
		synonym.setGenerationID(generationID); //Ensure uniqueness for set
		synonym.setTaxon_level(TaxonLevel.getTaxonLevel(level));
		
		if (level== TaxonNode.INFRARANK) {
			//Adding 1 because SIS 1 starts @ 0, SIS 2 starts @ 1.
			int infrarankLevel;
			if (synData.getInfrarankType() == -1)
				infrarankLevel = Infratype.INFRARANK_TYPE_SUBSPECIES;
			else
				infrarankLevel = synData.getInfrarankType() + 1;
			
			synonym.setInfraTypeObject(Infratype.getInfratype(infrarankLevel));
		}
		
		for (Entry<String, String> entry : synData.getAuthorities().entrySet())
			synonym.setAuthority(entry.getValue(), Integer.valueOf(entry.getKey()));

		synonym.setInfraName(synData.getInfrarank());
		synonym.setSpeciesName(synData.getSpecie());
		synonym.setStockName(synData.getStockName());

		if (synData.getLevel() >= TaxonLevel.GENUS)
			synonym.setGenusName(synData.getGenus());
		else
			synonym.setName(synData.getUpperLevelName());

		//This is now auto-generated.
		//synonym.setFriendlyName(synData.getName());
		synonym.setStatus(synData.getStatus());

		if (synData.getNotes() != null) {
			Edit edit = new Edit("Data migration.");
			edit.setUser(user);
			
			Notes note = new Notes();
			note.setSynonym(synonym);
			note.setValue(synData.getNotes());
			note.setEdit(edit);
			
			edit.getNotes().add(note);
			
			synonym.getNotes().add(note);
		}
		
		if (synData.isOldVersion() && (synonym.getFriendlyName() == null || "".equals(synonym.getFriendlyName()))) {
			//printf("Processing old version %s in %s", synData.getOldVersionName(), taxon.getId());
			String[] split = synData.getOldVersionName().split(" ");
			if (split.length == 3 && split[1].endsWith("ssp."))
				split = new String[] {split[0], split[1].substring(0, split[1].length()-4), "ssp.", split[2] };
			else if (split.length == 3 && split[1].endsWith("var."))
				split = new String[] {split[0], split[1].substring(0, split[1].length()-4), "var.", split[2] };
			
			if (split.length == 1) {
				synonym.setGenusName(split[0]);
				if (Synonym.MERGE.equals(synonym.getStatus()))
					synonym.setTaxon_level(TaxonLevel.getTaxonLevel(taxon.getLevel()));
				else if (Synonym.SPLIT.equals(synonym.getStatus()))
					synonym.setTaxon_level(TaxonLevel.getTaxonLevel(taxon.getLevel()+1));
				else
					synonym.setTaxon_level(TaxonLevel.getTaxonLevel(taxon.getLevel()));
			}
			else if (split.length == 2) {
				synonym.setGenusName(split[0]);
				synonym.setSpeciesName(split[1]);
			}
			else if (split.length == 4) {
				synonym.setGenusName(split[0]);
				synonym.setSpeciesName(split[1]);
				synonym.setInfraName(split[3]);
				
				for (int id : Infratype.ALL) {
					Infratype type = Infratype.getInfratype(id);
					if (split[2].startsWith(type.getCode()))
						synonym.setInfraTypeObject(type);
				}
				
				if (synonym.getInfraType() == null)
					synonym.setInfraTypeObject(Infratype.getInfratype(Infratype.INFRARANK_TYPE_SUBSPECIES));
			}
			else {
				//printf("Could not assign real data to %s in %s", synData.getName(), taxon.getId());
			
				Edit edit = new Edit("Data migration for synonyms, naming error.");
				edit.setUser(user);
				
				Notes note = new Notes();
				note.setSynonym(synonym);
				note.setValue("Failed migrating synonym with name " + synData.getName());
				note.setEdit(edit);
				
				edit.getNotes().add(note);
				
				synonym.setName(synData.getName());
				synonym.getNotes().add(note);
			}
		}
		
		return synonym;
	}

}
