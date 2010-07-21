package org.iucn.sis.shared.conversions;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.gogoego.api.plugins.GoGoEgo;
import org.hibernate.HibernateException;
import org.iucn.sis.client.referenceui.ReferenceUI;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.models.CommonName;
import org.iucn.sis.shared.api.models.Edit;
import org.iucn.sis.shared.api.models.Infratype;
import org.iucn.sis.shared.api.models.Notes;
import org.iucn.sis.shared.api.models.Reference;
import org.iucn.sis.shared.api.models.Synonym;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonLevel;
import org.iucn.sis.shared.api.models.User;
import org.iucn.sis.shared.taxonomyTree.CommonNameData;

import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.NativeDocument;

public class TaxonConverter {

	static long taxaConverted = 0;
	
	public static void convertAllNodes() throws Throwable {
		
		List<File> allFiles = FileListing.main(GoGoEgo.getInitProperties().get("sis_old_vfs") + "/HEAD/browse/nodes");
		User user = SIS.get().getUserIO().getUserFromUsername("admin");
		Date date = new Date();
		
		Map<Integer, Taxon> taxa = new HashMap<Integer, Taxon>();
		ArrayList<Taxon> kingdomList = new ArrayList<Taxon>();
		ArrayList<Taxon> phylumList = new ArrayList<Taxon>();
		ArrayList<Taxon> classList = new ArrayList<Taxon>();
		ArrayList<Taxon> orderList = new ArrayList<Taxon>();
		ArrayList<Taxon> familyList = new ArrayList<Taxon>();
		ArrayList<Taxon> genusList = new ArrayList<Taxon>();
		ArrayList<Taxon> speciesList = new ArrayList<Taxon>();
		ArrayList<Taxon> infrarankList = new ArrayList<Taxon>();
		ArrayList<Taxon> subpopulationList = new ArrayList<Taxon>();
		ArrayList<Taxon> infrarankSubpopulationList = new ArrayList<Taxon>();

		
		for (File file : allFiles) {
			try {
				if (file.getPath().endsWith(".xml")) {
					NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
					ndoc.parse(FileListing.readFileAsString(file));
					TaxonNode node = TaxonNodeFactory.createNode(ndoc);
					Taxon taxon = convertTaxonNode(node, new Date(file.lastModified()));
					
					if (taxon != null) {
						
						if (taxon.getLastEdit() == null) {
							Edit edit = new Edit();
							edit.setUser(user);
							edit.setCreatedDate(date);
							edit.getTaxon().add(taxon);
							taxon.getEdits().add(edit);							
						}							
						
						taxa.put(taxon.getId(), taxon);
						Integer nodeLevel = taxon.getLevel();
						if (nodeLevel == TaxonLevel.KINGDOM) {
							kingdomList.add(taxon);
						} else if (nodeLevel == TaxonLevel.PHYLUM) {
							phylumList.add(taxon);
						} else if (nodeLevel == TaxonLevel.CLASS) {
							classList.add(taxon);
						} else if (nodeLevel == TaxonLevel.ORDER) {
							orderList.add(taxon);
						} else if (nodeLevel == TaxonLevel.FAMILY) {
							familyList.add(taxon);
						} else if (nodeLevel == TaxonLevel.GENUS) {
							genusList.add(taxon);
						} else if (nodeLevel == TaxonLevel.SPECIES) {
							speciesList.add(taxon);
						} else if (nodeLevel == TaxonLevel.INFRARANK) {
							infrarankList.add(taxon);
						} else if (nodeLevel == TaxonLevel.SUBPOPULATION) {
							subpopulationList.add(taxon);
						} else if (nodeLevel == TaxonLevel.INFRARANK_SUBPOPULATION) {
							infrarankSubpopulationList.add(taxon);
						}
						
					} else {
						throw new Exception("The taxon " + file.getPath() + " is null");
					}

				}
			} catch (Throwable e) {
				System.out.println("Failed on file " + file.getPath());
				e.printStackTrace();
				throw e;
			}
		}
		
		while (kingdomList.size() > 0) {
			writeTaxon(kingdomList.remove(0), taxa);

		}
		while (phylumList.size() > 0) {
			writeTaxon(phylumList.remove(0), taxa);

		}
		while (classList.size() > 0) {
			writeTaxon(classList.remove(0), taxa);

		}
		while (orderList.size() > 0) {
			writeTaxon(orderList.remove(0), taxa);

		}
		while (familyList.size() > 0) {
			writeTaxon(familyList.remove(0), taxa);

		}
		while (genusList.size() > 0) {
			writeTaxon(genusList.remove(0), taxa);

		}
		while (speciesList.size() > 0) {
			writeTaxon(speciesList.remove(0), taxa);

		}
		while (infrarankList.size() > 0) {
			writeTaxon(infrarankList.remove(0), taxa);

		}
		while (subpopulationList.size() > 0) {
			writeTaxon(subpopulationList.remove(0), taxa);

		}
		while (infrarankSubpopulationList.size() > 0) {
			writeTaxon(infrarankSubpopulationList.remove(0), taxa);

		}

		if( taxaConverted % 100 != 0 ) {
			SIS.get().getManager().getSession().getTransaction().commit();
			SIS.get().getManager().getSession().beginTransaction();
		}
		
	}
	
	protected static void writeTaxon(Taxon taxon, Map<Integer, Taxon> taxa ) throws HibernateException, PersistentException {
		
		if (taxon.getParent() != null) {
			taxon.setParent(taxa.get(taxon.getParentId()));
		}
		SIS.get().getManager().getSession().save(taxon);
		
		taxaConverted++;
		
		if( taxaConverted % 100 == 0 ) {
			SIS.get().getManager().getSession().getTransaction().commit();
			SIS.get().getManager().getSession().beginTransaction();
		}
		
		taxon.getFootprint();
		SIS.get().getTaxonIO().afterSaveTaxon(taxon);
	}

	public static Taxon convertTaxonNode(TaxonNode taxon, Date lastModified) throws PersistentException {

		Taxon newTaxon = new Taxon();
		newTaxon.state = Taxon.ACTIVE;
		newTaxon.setId((int) taxon.getId());
		if (!taxon.getParentId().equals("")) {
			newTaxon.setParentId(Integer.valueOf(taxon.getParentId()));
			//Taxon parent = new Taxon();
			//parent.setId(Integer.valueOf(taxon.getParentId()));
			//parent.setName(taxon.getParentName());
		
		}
		newTaxon.setStatus(taxon.getStatus());
		newTaxon.setTaxonLevel(TaxonLevel.getTaxonLevel(taxon.getLevel()));
		newTaxon.setName(taxon.getName());
		try {
			newTaxon.setFriendlyName(taxon.generateFullName());
		} catch (IndexOutOfBoundsException e) {
			System.out.println("--- ERROR setting friendly name for taxon " + newTaxon.getId());
		}
		newTaxon.setHybrid(taxon.isHybrid());
		newTaxon.setTaxonomicAuthority(taxon.getTaxonomicAuthority());

		// ADD COMMON NAMES
		for (CommonNameData commonNameData : taxon.getCommonNames()) {
			CommonName commonName = new CommonName();
			commonName.setChangeReason(commonNameData.getChangeReason());
			commonName.setName(commonNameData.getName());			
			commonName.setPrincipal(commonNameData.isPrimary());
			commonName.setValidated(commonNameData.isValidated());

			// ADD ISO LANGUAGUE
			commonName.setIso(SIS.get().getIsoLanguageIO().getIsoLanguageByCode(commonNameData.getIsoCode()));
			newTaxon.getCommonNames().add(commonName);
			commonName.setTaxon(newTaxon);
					
		}

		// ADD SYNONYMS
		for (SynonymData synData : taxon.getSynonyms()) {
			Synonym synonym = new Synonym();
			
			for (Entry<String, String> entry : synData.getAuthorities().entrySet())
				synonym.setAuthority(entry.getValue(), Integer.valueOf(entry.getKey()));

			synonym.setInfraName(synData.getInfrarank());
			synonym.setSpeciesName(synData.getSpecie());
			synonym.setStockName(synData.getStockName());

			if (synData.getLevel() == TaxonLevel.GENUS)
				synonym.setName(synData.getGenus());
			else
				synonym.setName(synData.getUpperLevelName());

			synonym.setFriendlyName(synData.getName());
			synonym.setStatus(synData.getStatus());

			if (synData.getNotes() != null) {
				Notes note = new Notes();
				note.setSynonym(synonym);
				note.setValue(synData.getNotes());
				synonym.getNotes().add(note);
			}
			
			newTaxon.getSynonyms().add(synonym);
			synonym.setTaxon(newTaxon);
			
		}

		// ADD INFRARANK
		if (taxon.getInfrarankType() == TaxonNode.INFRARANK_TYPE_SUBSPECIES) {
			Infratype infratype = SIS.get().getInfratypeIO().getInfratype(Infratype.SUBSPECIES_NAME);
			newTaxon.setInfratype(infratype);
		} else if (taxon.getInfrarankType() == TaxonNode.INFRARANK_TYPE_VARIETY) {
			Infratype infratype = SIS.get().getInfratypeIO().getInfratype(Infratype.VARIETY_NAME);
			newTaxon.setInfratype(infratype);
		}


		// ADD REFERENCES
		for (ReferenceUI refUI : taxon.getReferencesAsList()) {
			String hash = refUI.getReferenceID();
			Reference ref = SIS.get().getReferenceIO().getReferenceByHashCode(hash);
			if (ref != null) {
				newTaxon.getReference().add(ref);
			} else {
				System.out.println("ERROR -- Couldn't find reference " + hash + " in taxon " + taxon.getId());
			}
		}

		// ADD LAST EDIT
		if (taxon.getLastUpdatedBy() != null) {
			Edit edit = new Edit();
			edit.setUser(SIS.get().getUserIO().getUserFromUsername(taxon.getLastUpdatedBy()));
			edit.setCreatedDate(lastModified);
		}
		return newTaxon;
	}
}
