package org.iucn.sis.server.extensions.offline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.filters.AssessmentFilterHelper;
import org.iucn.sis.server.api.io.AssessmentIO;
import org.iucn.sis.server.api.io.ReferenceIO;
import org.iucn.sis.server.api.io.UserIO;
import org.iucn.sis.server.api.io.WorkingSetIO;
import org.iucn.sis.server.api.io.AssessmentIO.AssessmentIOWriteResult;
import org.iucn.sis.server.api.locking.FileLocker;
import org.iucn.sis.server.api.locking.HibernateLockRepository;
import org.iucn.sis.server.api.persistance.FieldDAO;
import org.iucn.sis.server.api.persistance.PrimitiveFieldDAO;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.utils.DatabaseExporter;
import org.iucn.sis.server.api.utils.RegionConflictException;
import org.iucn.sis.server.utils.AssessmentPersistence;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.io.AssessmentChangePacket;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.Reference;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.User;
import org.iucn.sis.shared.api.models.WorkingSet;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import com.solertium.db.DBException;
import com.solertium.util.events.ComplexListener;

public class OfflineToOnlineImporter extends DatabaseExporter {
	
	private final Properties targetProperties;
	private final User loggedInUser;
	private SISPersistentManager targetManager;
	
	protected Session source;
	protected Session target;
	
	public OfflineToOnlineImporter(Integer workingSetID, User user, Properties properties) {
		super(null, workingSetID);
		this.targetProperties = properties;
		this.loggedInUser = user;
	}
	
	protected void execute() throws Throwable {
		
		String name = "sis_target";				
		
		//The offline database.
		source = SIS.get().getManager().openSession();
		
		//Live DB will NEVER be from scratch...
		targetManager = SISPersistentManager.
			newInstance(name, targetProperties, false);
		target = targetManager.openSession();
			
		final WorkingSet workingSet = new WorkingSetIO(source).readWorkingSet(workingSetID);
		Hibernate.initialize(workingSet.getUsers());
		
		try {			
			// Update User profiles created Offline
			write("  Writing Offline Users ");
			UserIO userIO = new UserIO(source);
			User[] offlineUsers = userIO.getOfflineCreatedUsers();
			insertOfflineUsers(offlineUsers);
					
			// Update References created Offline
			write("  Writing Offline References ");
			ReferenceIO referenceIO = new ReferenceIO(source);
			Reference[] offlineReferences = referenceIO.getOfflineCreatedReferences();
			insertOfflineReference(offlineReferences);		
			
			final AssessmentFilterHelper helper = new AssessmentFilterHelper(source, workingSet.getFilter());
			final int size = workingSet.getTaxon().size();
			int count = 0;
			for (Taxon taxon : workingSet.getTaxon()) {
				
				Collection<Assessment> assessments = helper.getAssessments(taxon.getId());
				write("Copying %s eligible assessments for %s, (%s/%s)", 
					assessments.size(), taxon.getFullName(), ++count, size);
				
				for (Assessment assessment : assessments) {
					// Fetch Assessments
					write("  Writing assessment #%s", assessment.getId());
					insertAssessment(null, assessment);
				}
			}
			
			write("  Updating Offline Users ");
			updateOfflieCreatedUser(offlineUsers);
			
			write("  Updating Offline References ");
			updateOfflieCreatedReference(offlineReferences);
			
			write("  Sync data to Online Completed Successfully!");

		} finally {
			source.close();
			target.close();
			targetManager.shutdown();
		}
	}
	
	private void insertOfflineUsers(User[] offlineUsers) {
		try{			
			if(offlineUsers.length > 0){
				target.clear();
				target.beginTransaction();
				for(User user : offlineUsers){
					User newUser = user.deepCopy();
					newUser.setId(0);
					target.save(newUser);	
				}
				target.getTransaction().commit();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	private void updateOfflieCreatedUser(User[] offlineUsers) {
		try{			
			if(offlineUsers.length > 0){
				source.clear();
				source.beginTransaction();
				for(User user : offlineUsers){
					User newUser = user.deepCopy();
					newUser.setOfflineStatus(false);
					source.update(newUser);	
				}
				source.getTransaction().commit();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void insertOfflineReference(Reference[] offlineReferences){	
		try{			
			if(offlineReferences.length > 0){
				target.clear();
				target.beginTransaction();
				for(Reference reference : offlineReferences){
					Reference newRef = reference.deepCopy();					
					newRef.setId(0);
					target.save(newRef);
				}	
				target.getTransaction().commit();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void updateOfflieCreatedReference(Reference[] offlineReferences){	
		try{			
			if(offlineReferences.length > 0){
				source.clear();
				source.beginTransaction();
				for(Reference reference : offlineReferences){
					Reference newRef = reference.deepCopy();					
					newRef.setOfflineStatus(false);
					source.update(newRef);
				}	
				source.getTransaction().commit();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void insertAssessment(Session session, Assessment assessment) throws DBException {

		AssessmentIO assessmentIO;
		final AssessmentChangePacket packet;
		
		/*
		 * Initialize everything this way because it's lazy & recursive.
		 */
		assessment.toXML();
		
		source.clear();
		target.clear();
		
		try {			
			assessmentIO = new AssessmentIO(target);			
			
			//Create objects
			HibernateLockRepository repository = new HibernateLockRepository(targetManager);
			FileLocker locker = new FileLocker(repository);

			//Unlock Live Assessment
			locker.persistentEagerRelease(assessment.getId(), loggedInUser);
			
			final Map<Integer, Reference> targetRefs = new HashMap<Integer, Reference>();			
			for (Reference reference : assessment.getReference())
				targetRefs.put(reference.getId(), reference);
			
			for (Reference sourceRef : assessment.getReference()) {
				if (sourceRef.getId() == 0)
					continue;
				
				Reference targetRef = targetRefs.remove(sourceRef.getId());
				if (targetRef == null) {
					Reference ref = SISPersistentManager.instance().getObject(session, Reference.class, sourceRef.getId());
					if (ref != null)
						assessment.getReference().add(ref);
				}
			}
				
			assessment.getReference().removeAll(targetRefs.values());
			assessment.toXML();
			
			packet = new AssessmentChangePacket(assessment.getId());
		
			final AssessmentPersistence saver = new AssessmentPersistence(target, assessment);
			saver.setAllowAdd(false);
			saver.setAllowDelete(false);
			saver.setAllowManageNotes(false);
			saver.setAllowManageReferences(false);
			saver.setDeleteFieldListener(new ComplexListener<Field>() {
				public void handleEvent(Field field) {
					packet.addXMLNote("<info>Removing field " + field.getName() + " with ID " + field.getId() + "</info>" + field.toXML());
					try {
						FieldDAO.deleteAndDissociate(field, target);
					} catch (PersistentException e) {
						Debug.println(e);
					}
				}
			});
			saver.setDeletePrimitiveFieldListener(new ComplexListener<PrimitiveField<?>>() {
				public void handleEvent(PrimitiveField<?> field) {
					packet.addXMLNote("<info>Removing primitive field " + field.getField().getName() + "." + field.getName() + " with ID " + field.getId() + "</info>" + field.toXML());
					try {
						PrimitiveFieldDAO.deleteAndDissociate(field, target);
					} catch (PersistentException e) {
						Debug.println(e);
					}
				}
			});
			
			for (Field field : packet.getAdditions()) {
				List<Field> toRemove = new ArrayList<Field>();
				for (Field existing : assessment.getField())
					if (field.getName().equals(existing.getName()))
						toRemove.add(existing);
				
				for (Field existing : toRemove) {
					assessment.getField().remove(existing);
					try {
						FieldDAO.deleteAndDissociate(existing, target);
					} catch (PersistentException e) {
						Debug.println(e);
						throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
					}
				}
				
				field.setAssessment(assessment);
				assessment.getField().add(field);
				
				saver.addChange(saver.createAddChange(field));
				
			}
			
			for (Field field : packet.getDeletions()) {
				final Field toDelete;
				try {
					toDelete = (Field)target.get(Field.class, field.getId());
				} catch (HibernateException e) {
					continue;
				}
				
				saver.addChange(saver.createDeleteChange(toDelete));
				
				try {
					FieldDAO.deleteAndDissociate(toDelete, target);
				} catch (PersistentException e) {
					Debug.println(e);
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
				}
				assessment.getField().remove(toDelete);
				
			}
			
			try {
				saver.sink(packet.getEdits());
			} catch (PersistentException e) {
				Debug.println(e);
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
			
			Hibernate.initialize(assessment.getEdit());
			
			assessment.toXML();
			
			if (!assessmentIO.allowedToCreateNewAssessment(assessment))
				throw new ResourceException(Status.CLIENT_ERROR_CONFLICT, new RegionConflictException());
			
			AssessmentIOWriteResult result = 
				assessmentIO.writeAssessment(assessment, loggedInUser, "Sync'd from Offline", true);
			
			if (!result.status.isSuccess())
				throw new ResourceException(result.status, "AssessmentIOWrite threw exception when saving.");
			
			target.flush();
			if (result.edit == null)
				Debug.println("Error: No edit associated with this change. Not backing up changes.");
			else
				saver.saveChanges(assessment, result.edit, targetManager);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}	
	

}
