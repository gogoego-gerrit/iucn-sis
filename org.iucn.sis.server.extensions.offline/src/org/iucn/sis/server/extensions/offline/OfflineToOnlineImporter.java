package org.iucn.sis.server.extensions.offline;

import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;

import org.hibernate.Hibernate;
import org.hibernate.ReplicationMode;
import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.filters.AssessmentFilterHelper;
import org.iucn.sis.server.api.io.WorkingSetIO;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.utils.DatabaseExporter;
import org.iucn.sis.server.api.utils.ServerPaths;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.WorkingSet;

import com.solertium.db.DBException;

public class OfflineToOnlineImporter extends DatabaseExporter {
	
	private final Properties targetProperties;
	
	protected Session source;
	protected Session target;
	
	public OfflineToOnlineImporter(Integer workingSetID, Properties properties) {
		super(null, workingSetID);
		this.targetProperties = properties;
	}
	
	@Override
	protected void afterRun() throws DBException {
		super.afterRun();

		write("Write complete");
		
		try {
			write("--- Complete ---");
		} catch (Exception e) {
			write("Failed!");
		}
	}

	@Override
	protected void insertAssessment(Session session, Assessment assessment) throws DBException {

		String url = ServerPaths.getAssessmentURL(assessment);
		SIS.get().getLocker().releaseLock(url);
		
		/*
		 * Initialize everything this way because it's lazy & recursive.
		 */
		assessment.toXML();
		
		source.clear();
		target.replicate(assessment, ReplicationMode.OVERWRITE);
	}
	
	protected void execute() throws Throwable {
		String name = "sis_target";				
		//String location = "localhost:5432";
		
		//The offline database.
		source = SIS.get().getManager().openSession();
		
		//Live DB will NEVER be from scratch...
		final SISPersistentManager targetManager = SISPersistentManager.
			newInstance(name, targetProperties, false);
		target = targetManager.openSession();
		
		//Since not from scratch, we don't need to initialize metadata
		/*
		try {
			if (fromScratch) {
				List<Class<?>> copyAll = new ArrayList<Class<?>>();
				copyAll.add(AssessmentType.class);
				copyAll.add(Definition.class);
				copyAll.add(Infratype.class);
				copyAll.add(IsoLanguage.class);
				copyAll.add(PermissionGroup.class);
				copyAll.add(Permission.class);
				copyAll.add(Region.class);
				copyAll.add(Relationship.class);
				copyAll.add(TaxonLevel.class);
				copyAll.add(TaxonStatus.class);
				copyAll.add(User.class);
				copyAll.add(Virus.class);
				//copyAll.add(Reference.class);
							
				for (Class<?> clazz : copyAll) {
					int i = 0;
					List<?> list = SISPersistentManager.instance().listObjects(clazz, source);
					source.clear();
					write("Copying %s...", clazz.getSimpleName());
					boolean started = false;
					for (Object obj : list) {
						if (!started) {
							started = true;
							target.beginTransaction();
						}
						target.replicate(obj, ReplicationMode.IGNORE);
						
						if (++i % BATCH_SIZE == 0) {
							write("  %s...", i);
							target.getTransaction().commit();
							target.clear();
							started = false;
						}
					}
					if (started) {
						write("  %s...", i);
						target.getTransaction().commit();
						target.clear();
					}
					target.clear();
				}
			}
		} catch (Exception e) {
			source.close();
			target.close();
			targetManager.shutdown();
			throw e;
		}
		*/
		
		final WorkingSet workingSet = new WorkingSetIO(source).readWorkingSet(workingSetID);
		Hibernate.initialize(workingSet.getUsers());
		
		try {
			final AssessmentFilterHelper helper = new AssessmentFilterHelper(source, workingSet.getFilter());
			final int size = workingSet.getTaxon().size();
			final HashSet<Integer> seen = new HashSet<Integer>();
			
			int count = 0;
			for (Taxon taxon : workingSet.getTaxon()) {
				target.beginTransaction();
				insertTaxa(taxon, seen);
				target.getTransaction().commit();
				
				Collection<Assessment> assessments = helper.getAssessments(taxon.getId());
				write("Copying %s eligible assessments for %s, (%s/%s)", 
					assessments.size(), taxon.getFullName(), ++count, size);
				target.beginTransaction();
				for (Assessment assessment : assessments) {
					write("  Writing assessment #%s", assessment.getId());
					insertAssessment(null, assessment);
				}
				target.getTransaction().commit();
				target.clear();
			}
			
			target.clear();
			target.beginTransaction();
			target.replicate(workingSet, ReplicationMode.OVERWRITE);
			target.getTransaction().commit();
		} finally {
			source.close();
			target.close();
			targetManager.shutdown();
		}
		afterRun();
	}
	
	protected void insertTaxa(Taxon taxon, HashSet<Integer> seen) throws DBException {
		if (taxon != null && !seen.contains(taxon.getId())) {
			taxon = (Taxon)source.load(Taxon.class, taxon.getId());
			
			//Load any lazy data so it will replicate
			taxon.toXML();
			
			source.clear();
			target.replicate(taxon, ReplicationMode.OVERWRITE);
			
			seen.add(taxon.getId());
			
			//No need for recursion here, replicate will catch it...
		}
	}

}
