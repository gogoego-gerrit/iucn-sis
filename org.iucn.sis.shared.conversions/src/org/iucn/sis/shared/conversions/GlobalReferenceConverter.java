package org.iucn.sis.shared.conversions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.hibernate.Hibernate;
import org.hibernate.criterion.Restrictions;
import org.iucn.sis.server.api.io.ReferenceIO;
import org.iucn.sis.server.api.io.UserIO;
import org.iucn.sis.server.api.utils.FilenameStriper;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Reference;
import org.iucn.sis.shared.api.models.User;
import org.iucn.sis.shared.helpers.AssessmentData;
import org.iucn.sis.shared.helpers.AssessmentParser;
import org.iucn.sis.shared.helpers.ReferenceUI;

import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.vfs.VFS;

public class GlobalReferenceConverter extends GenericConverter<VFSInfo> {
	
	private ReferenceIO referenceIO;
	private UserIO userIO;
	
	public GlobalReferenceConverter() {
		super();
		setClearSessionAfterTransaction(true);
	}

	@Override
	protected void run() throws Exception {
		referenceIO = new ReferenceIO(session);
		userIO = new UserIO(session);
		
		printf("Processing draft assessments");
		convertAllDrafts(data.getOldVFS(), data.getNewVFS());
		printf("Processing published assessments");
		convertAllPublished(data.getOldVFS(), data.getNewVFS());
	}
	
	public void convertAll(String rootURL, VFS oldVFS, VFS newVFS) throws Exception {
		convertAllFaster(rootURL, oldVFS, newVFS);
	}
	
	public void convertAllPublished(VFS oldVFS, VFS newVFS) throws Exception {
		File cache = new File(data.getOldVFSPath() + "/HEAD/migration/assessments.dat");
		if (cache.exists())
			convertCached(new AssessmentParser(), userIO.getUserFromUsername("admin"), new AtomicInteger(), cache);
		else
			convertAll("/HEAD/browse/assessments", oldVFS, newVFS);
	}
	
	public void convertAllDrafts(VFS oldVFS, VFS newVFS) throws Exception {
		convertAll("/HEAD/drafts", oldVFS, newVFS);
	}
	
	private void convertCached(AssessmentParser parser, User user, AtomicInteger converted, File cache) throws Exception {
		final BufferedReader reader = new BufferedReader(new FileReader(cache));
		String line = null;
		
		HashSet<String> taxa = new HashSet<String>();
		if (parameters.getFirstValue("subset", null) != null)
			for (String taxon : parameters.getValuesArray("subset"))
				taxa.add(taxon);
		
		boolean subset = !taxa.isEmpty();
		boolean found = false;
		boolean canStop = false;
		
		if (subset)
			printf("Converting the subset: %s", taxa);
		
		while ((line = reader.readLine()) != null) {
			String[] split = line.split(":");
			File file = new File(data.getOldVFSPath() + "/HEAD/browse/assessments/" + 
					FilenameStriper.getIDAsStripedPath(split[1]) + ".xml");
			if (!file.exists()) {
				printf("No assessment found on disk for taxon %s at %s", split[0], file.getPath());
				continue;
			}
			
			if (!subset || (found = taxa.contains(split[0])))
				readFile(parser, user, converted, file);
			
			if (subset && found)
				canStop = true;
			
			if (subset && !found && canStop)
				break;
		}
	}
	
	public void convertAllFaster(String rootURL, VFS oldVFS, VFS newVFS) throws Exception {
		final AssessmentParser parser = new AssessmentParser();
		final User user = userIO.getUserFromUsername("admin");
		final AtomicInteger converted = new AtomicInteger(0);
		
		File folder = new File(data.getOldVFSPath() + rootURL);
		
		readFolder(parser, user, converted, folder);
	}
	
	private void readFolder(AssessmentParser parser, User user, AtomicInteger converted, File folder) throws Exception {
		for (File file : folder.listFiles()) {
			if (file.isDirectory())
				readFolder(parser, user, converted, file);
			else if (file.getName().endsWith(".xml"))
				readFile(parser, user, converted, file);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void readFile(AssessmentParser parser, User user, AtomicInteger converted, File file) throws Exception {
		try {
			NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
			ndoc.parse(FileListing.readFileAsString(file));
			
			parser.parse(ndoc);
			
			AssessmentData assessData = parser.getAssessment();
			
			List<Assessment> list = session.createCriteria(Assessment.class)
				.add(Restrictions.eq("internalId", assessData.getAssessmentID()))
				.list();
			
			for (Assessment assessment : list) {
				Hibernate.initialize(assessment.getReference());
				if (assessment.getReference() != null && !assessment.getReference().isEmpty())
					continue;
				
				assessment.setReference(new HashSet<Reference>());
				for (ReferenceUI curRef : assessData.getReferences("Global")) {
					Reference ref = referenceIO.getReferenceByHashCode(curRef.getReferenceID());
					if (ref != null) {
						assessment.getReference().add(ref);
						ref.getAssessment().add(assessment);
					}
					else
						printf(" - No reference found with bib_hash %s", curRef.getReferenceID());
				}
				
				if (!assessment.getReference().isEmpty())
					printf(" - Added %s global references.", assessment.getReference().size());
			}
			
			commitAndStartTransaction();
		} catch (Throwable e) {
			print("Failed on file " + file.getPath());
			e.printStackTrace();
			throw new Exception(e);
		}
	}

}
