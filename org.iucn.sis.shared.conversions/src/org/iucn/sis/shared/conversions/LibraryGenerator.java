package org.iucn.sis.shared.conversions;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.models.AssessmentType;
import org.iucn.sis.shared.api.models.Infratype;
import org.iucn.sis.shared.api.models.IsoLanguage;
import org.iucn.sis.shared.api.models.Relationship;
import org.iucn.sis.shared.api.models.TaxonLevel;
import org.iucn.sis.shared.api.models.TaxonStatus;

public class LibraryGenerator extends GenericConverter<String> {
	
	/**
	 * Use this to quickie-create a new database from scratch with 
	 * standard baseline library information included.
	 * @param args
	 */
	public static void main(String[] args) { 
		Properties properties = new Properties();
		properties.setProperty("dbsession.sis.uri", "**URL**");
		properties.setProperty("dbsession.sis.driver", "org.postgresql.Driver");
		properties.setProperty("dbsession.sis.user", "**USERNAME**");
		properties.setProperty("dbsession.sis.password", "**PW**");
		properties.setProperty("database_dialect", "org.hibernate.dialect.PostgreSQLDialect");
		
		SISPersistentManager manager = SISPersistentManager.newInstance("sis", properties, true);
		
		LibraryGenerator generator = new LibraryGenerator();
		generator.setData("/var/sis/new_vfs");
		generator.setSession(manager.openSession());
		generator.start();
	}
	
	public LibraryGenerator() {
		super();
	}
	
	@Override
	protected void run() throws Exception {
		generateAssessmentTypes();
		generateInfratypes();
		generateRelationships();
		generateTaxonStatus();
		generateTaxonLevel();
		generateIsoLanguages();
	}

	public void generateIsoLanguages() throws HibernateException, IOException, PersistentException {

		FileInputStream fstream = new FileInputStream(data + "/HEAD/utils/ISO-639-2_utf-8.txt");
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
			session.save(isoLanguage);
			id++;

		}
		in.close();

	}

	public void generateInfratypes() throws PersistentException {
		for (int id : Infratype.ALL)
			session.save(Infratype.getInfratype(id));
	}

	public void generateAssessmentTypes() throws PersistentException {
		session.save(AssessmentType.getAssessmentType(AssessmentType.PUBLISHED_ASSESSMENT_STATUS_ID));
		session.save(AssessmentType.getAssessmentType(AssessmentType.DRAFT_ASSESSMENT_STATUS_ID));
		session.save(AssessmentType.getAssessmentType(AssessmentType.SUBMITTED_ASSESSMENT_STATUS_ID));
		session.save(AssessmentType.getAssessmentType(AssessmentType.FOR_PUBLICATION_ASSESSMENT_STATUS_ID));
	}

	public void generateRelationships() throws PersistentException {
		session.save(Relationship.fromName(Relationship.ALL));
		session.save(Relationship.fromName(Relationship.OR));
		session.save(Relationship.fromName(Relationship.AND));
	}
	
	public void generateTaxonStatus() throws PersistentException {
		for (String code : TaxonStatus.ALL)
			session.save(TaxonStatus.fromCode(code));
	}

	public void generateTaxonLevel() throws PersistentException {
		session.save(TaxonLevel.getTaxonLevel(TaxonLevel.CLASS));
		session.save(TaxonLevel.getTaxonLevel(TaxonLevel.FAMILY));
		session.save(TaxonLevel.getTaxonLevel(TaxonLevel.GENUS));
		session.save(TaxonLevel.getTaxonLevel(TaxonLevel.INFRARANK));
		session.save(TaxonLevel.getTaxonLevel(TaxonLevel.INFRARANK_SUBPOPULATION));
		session.save(TaxonLevel.getTaxonLevel(TaxonLevel.KINGDOM));
		session.save(TaxonLevel.getTaxonLevel(TaxonLevel.ORDER));
		session.save(TaxonLevel.getTaxonLevel(TaxonLevel.PHYLUM));
		session.save(TaxonLevel.getTaxonLevel(TaxonLevel.SPECIES));
		session.save(TaxonLevel.getTaxonLevel(TaxonLevel.SUBPOPULATION));
	}

}
