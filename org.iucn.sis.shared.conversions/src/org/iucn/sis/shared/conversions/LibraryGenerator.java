package org.iucn.sis.shared.conversions;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.gogoego.api.plugins.GoGoEgo;
import org.hibernate.HibernateException;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.persistance.RelationshipDAO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.models.AssessmentType;
import org.iucn.sis.shared.api.models.Infratype;
import org.iucn.sis.shared.api.models.IsoLanguage;
import org.iucn.sis.shared.api.models.Relationship;
import org.iucn.sis.shared.api.models.TaxonLevel;
import org.iucn.sis.shared.api.models.TaxonStatus;


public class LibraryGenerator {

	public static void generateIsoLanguages() throws HibernateException, IOException, PersistentException {

		FileInputStream fstream = new FileInputStream(GoGoEgo.getInitProperties().get("sis_old_vfs") + "/HEAD/utils/ISO-639-2_utf-8.txt");
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		int codeIndex = 0;
		int nameIndex = 3;

		int id = 1;
		while ((strLine = br.readLine()) != null) {
			String[] info = strLine.split("\\Q|\\E");
			IsoLanguage isoLanguage = new IsoLanguage(info[nameIndex], info[codeIndex]);
			isoLanguage.setId(id);
			SIS.get().getManager().getSession().save(isoLanguage);
			id++;

		}
		in.close();

	}

	public static void generateInfratypes() throws PersistentException {
		SIS.get().getManager().getSession().save(Infratype.getInfratype(Infratype.INFRARANK_TYPE_SUBSPECIES));
		SIS.get().getManager().getSession().save(Infratype.getInfratype(Infratype.INFRARANK_TYPE_VARIETY));
	}

	public static void generateAssessmentTypes() throws PersistentException {
		AssessmentType type = AssessmentType.getAssessmentType(AssessmentType.DRAFT_ASSESSMENT_STATUS_ID);
		System.out.println("this is type " + type);
		SIS.get().getManager().getSession().save(type);
		SIS.get().getManager().getSession().save(
				AssessmentType.getAssessmentType(AssessmentType.PUBLISHED_ASSESSMENT_STATUS_ID));
	}

	public static void generateRelationships() throws PersistentException {
		RelationshipDAO.save(Relationship.fromName(Relationship.ALL));
		RelationshipDAO.save(Relationship.fromName(Relationship.OR));
		RelationshipDAO.save(Relationship.fromName(Relationship.AND));
	}

	public static void generateTaxonLevel() throws PersistentException {
		SIS.get().getManager().getSession().save(TaxonStatus.fromCode(TaxonStatus.STATUS_ACCEPTED));
		SIS.get().getManager().getSession().save(TaxonLevel.getTaxonLevel(TaxonLevel.CLASS));
		SIS.get().getManager().getSession().save(TaxonLevel.getTaxonLevel(TaxonLevel.FAMILY));
		SIS.get().getManager().getSession().save(TaxonLevel.getTaxonLevel(TaxonLevel.GENUS));
		SIS.get().getManager().getSession().save(TaxonLevel.getTaxonLevel(TaxonLevel.INFRARANK));
		SIS.get().getManager().getSession().save(TaxonLevel.getTaxonLevel(TaxonLevel.INFRARANK_SUBPOPULATION));
		SIS.get().getManager().getSession().save(TaxonLevel.getTaxonLevel(TaxonLevel.KINGDOM));
		SIS.get().getManager().getSession().save(TaxonLevel.getTaxonLevel(TaxonLevel.ORDER));
		SIS.get().getManager().getSession().save(TaxonLevel.getTaxonLevel(TaxonLevel.PHYLUM));
		SIS.get().getManager().getSession().save(TaxonLevel.getTaxonLevel(TaxonLevel.SPECIES));
		SIS.get().getManager().getSession().save(TaxonLevel.getTaxonLevel(TaxonLevel.SUBPOPULATION));

	}

	public static void generateTaxonStatus() throws PersistentException {
		SIS.get().getManager().getSession().save(TaxonStatus.fromCode(TaxonStatus.STATUS_DISCARDED));
		SIS.get().getManager().getSession().save(TaxonStatus.fromCode(TaxonStatus.STATUS_NEW));
		SIS.get().getManager().getSession().save(TaxonStatus.fromCode(TaxonStatus.STATUS_SYNONYM));
	}

}
