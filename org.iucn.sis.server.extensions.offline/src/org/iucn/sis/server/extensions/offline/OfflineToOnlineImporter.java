package org.iucn.sis.server.extensions.offline;

import java.sql.BatchUpdateException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.iucn.sis.server.api.filters.AssessmentFilterHelper;
import org.iucn.sis.server.api.io.AssessmentIO;
import org.iucn.sis.server.api.io.ReferenceIO;
import org.iucn.sis.server.api.io.UserIO;
import org.iucn.sis.server.api.io.WorkingSetIO;
import org.iucn.sis.server.api.io.AssessmentIO.AssessmentIOWriteResult;
import org.iucn.sis.server.api.locking.FileLocker;
import org.iucn.sis.server.api.locking.HibernateLockRepository;
import org.iucn.sis.server.api.locking.LockException;
import org.iucn.sis.server.api.persistance.FieldDAO;
import org.iucn.sis.server.api.persistance.PrimitiveFieldDAO;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.utils.RegionConflictException;
import org.iucn.sis.server.utils.AssessmentPersistence;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;
import org.iucn.sis.shared.api.models.Reference;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.User;
import org.iucn.sis.shared.api.models.WorkingSet;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import com.solertium.util.DynamicWriter;
import com.solertium.util.events.ComplexListener;

public class OfflineToOnlineImporter extends DynamicWriter implements Runnable {

	private final Properties targetProperties;
	private final User loggedInUser;
	private final Integer workingSetID;
	
	private SISPersistentManager targetManager;
	private boolean unlock;

	protected Session source;
	protected Session target;

	public OfflineToOnlineImporter(Integer workingSetID, User user, Properties properties) {
		super();
		this.workingSetID = workingSetID;
		this.targetProperties = properties;
		this.loggedInUser = user;
		this.unlock = true;
	}
	
	public void setUnlock(boolean unlock) {
		this.unlock = unlock;
	}

	public final void run() {
		Date start = Calendar.getInstance().getTime();

		write("Export started at %s", start);

		// The offline database.
		source = SISPersistentManager.instance().openSession();

		// Live DB will NEVER be from scratch...
		targetManager = SISPersistentManager.newInstance("sis_target", targetProperties, false);
		target = targetManager.openSession();

		try {
			execute();
		} catch (Throwable e) {
			Debug.println(e);
			write("Failed unexpectedly: %s", e.getMessage());
			
			if (e.getCause() instanceof BatchUpdateException) {
				((BatchUpdateException)e.getCause()).getNextException().printStackTrace();
			}
		} finally {
			Date end = Calendar.getInstance().getTime();

			long time = end.getTime() - start.getTime();
			int secs = (int) (time / 1000);
			int mins = (int) (secs / 60);

			write("Export completed at %s in %s minutes, %s seconds.", end, mins, secs);

			close();
		}
	}

	@Override
	public void close() {
		super.close();
		source.close();
		target.close();
		targetManager.shutdown();
	}

	protected void execute() throws PersistentException {
		// Update User profiles created Offline
		UserIO userIO = new UserIO(source);
		User[] offlineUsers = userIO.getOfflineCreatedUsers();
		if (offlineUsers.length > 0) {
			write("Writing %s Offline Users", offlineUsers.length);
			insertOfflineUsers(offlineUsers);
		} else
			write("Detected no users created offline");

		// Update References created Offline
		ReferenceIO referenceIO = new ReferenceIO(source);
		Reference[] offlineReferences = referenceIO.getOfflineCreatedReferences();
		if (offlineReferences.length > 0) {
			write("Writing %s Offline References", offlineReferences.length);
			insertOfflineReference(offlineReferences);
		} else
			write("Detected no references created offline");

		// Update Assessments created Offline
		AssessmentIO assessmentIO = new AssessmentIO(source);
		Assessment[] offlineAssessments = assessmentIO.getOfflineCreatedAssessments();
		if (offlineAssessments.length > 0) {
			write("Writing Offline Assessments");
			insertOfflineAssessments(offlineAssessments);
		} else
			write("Detected no assessments created offline");

		final WorkingSet workingSet = new WorkingSetIO(target).readWorkingSet(workingSetID);
		Hibernate.initialize(workingSet.getUsers());
		Hibernate.initialize(workingSet.getAssessmentTypes());
		// workingSet.toXML();

		final AssessmentFilterHelper helper = new AssessmentFilterHelper(target, workingSet.getFilter());
		final int size = workingSet.getTaxon().size();
		int count = 0;
		for (Taxon taxon : workingSet.getTaxon()) {
			Collection<Assessment> assessments = helper.getAssessments(taxon.getId());
			write("Copying %s eligible assessments for %s, (%s/%s)", assessments.size(), taxon.getFullName(), ++count,
					size);

			for (Assessment targetAsm : assessments) {
				// Fetch Assessments
				Assessment sourceAsm = assessmentIO.getAssessment(targetAsm.getId());
				if (sourceAsm == null)
					write(" - Could not find offline version of assesment #%s", targetAsm.getId());
				else {
					try {
						syncAssessment(sourceAsm, targetAsm);
						write(" + Write successful!");
					} catch (ResourceException e) {
						write(" - Write failed.");
					}
				}
			}
		}

		if (offlineUsers.length > 0) {
			write("Updating Offline Users ");
			updateOfflineCreatedUser(offlineUsers);
		}

		if (offlineReferences.length > 0) {
			write("Updating Offline References ");
			updateOfflineCreatedReference(offlineReferences);
		}

		write("Sync data to Online Complete.");
	}

	private void insertOfflineAssessments(Assessment[] offlineAssessments) {
		try {
			if (offlineAssessments.length > 0) {
				target.clear();
				target.beginTransaction();
				for (Assessment assessment : offlineAssessments) {
					assessment.toXML();
					if (assessment.getOnlineId() == null) {
						write("  Writing new assessment #%s", assessment.getId());
						Assessment newAssessment = assessment.deepCopy();
						newAssessment.setId(0);

						target.save(newAssessment);

						updateAssessmentOnlineId(assessment, newAssessment.getId());
					} else {
						write("  Merging offline assessment #%s with online assessment #%s", 
								assessment.getId(), assessment.getOnlineId());
						int onlineId = assessment.getOnlineId();

						AssessmentIO assessmentIO = new AssessmentIO(target);
						Assessment targetAssessment = assessmentIO.getAssessment(onlineId);

						syncAssessment(assessment, targetAssessment);
					}
				}
				target.getTransaction().commit();
			}
		} catch (Exception e) {
			Debug.println(e);
		}
	}

	private void insertOfflineReference(Reference[] offlineReferences) {
		try {
			if (offlineReferences.length > 0) {
				target.clear();
				target.beginTransaction();
				for (Reference reference : offlineReferences) {
					Reference newRef = reference.deepCopy();
					newRef.setId(0);
					target.save(newRef);
				}
				target.getTransaction().commit();
			}
		} catch (Exception e) {
			Debug.println(e);
		}
	}

	private void insertOfflineUsers(User[] offlineUsers) {
		try {
			if (offlineUsers.length > 0) {
				target.clear();
				target.beginTransaction();
				for (User user : offlineUsers) {
					User newUser = user.deepCopy();
					newUser.setId(0);
					target.save(newUser);
				}
				target.getTransaction().commit();
			}
		} catch (Exception e) {
			Debug.println(e);
		}
	}

	private void updateOfflineCreatedReference(Reference[] offlineReferences) {
		try {
			if (offlineReferences.length > 0) {
				source.clear();
				source.beginTransaction();
				for (Reference reference : offlineReferences) {
					Reference newRef = reference.deepCopy();
					newRef.setOfflineStatus(false);
					source.update(newRef);
				}
				source.getTransaction().commit();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void updateOfflineCreatedUser(User[] offlineUsers) {
		try {
			if (offlineUsers.length > 0) {
				source.clear();
				source.beginTransaction();
				for (User user : offlineUsers) {
					User newUser = user.deepCopy();
					newUser.setOfflineStatus(false);
					source.update(newUser);
				}
				source.getTransaction().commit();
			}
		} catch (Exception e) {
			Debug.println(e);
		}
	}
	
	private void updateAssessmentOnlineId(Assessment assessment, Integer onlineId) {
		try {
			source.clear();
			source.beginTransaction();

			assessment.setOnlineId(onlineId);

			source.update(assessment);
			source.getTransaction().commit();
		} catch (Exception e) {
			Debug.println(e);
		}
	}
	
	private void write(String template, Object... args) {
		write(String.format(template, args));
	}

	private synchronized void syncAssessment(Assessment sourceAsm, Assessment targetAsm) throws PersistentException,
			ResourceException {
		/*
		 * Initialize everything this way because it's lazy & recursive.
		 */
		sourceAsm.toXML();

		source.clear();
		target.clear();

		AssessmentIO assessmentIO = new AssessmentIO(target);

		final Map<Integer, Reference> targetRefs = new HashMap<Integer, Reference>();
		for (Reference reference : targetAsm.getReference())
			targetRefs.put(reference.getId(), reference);

		for (Reference sourceRef : sourceAsm.getReference()) {
			if (sourceRef.getId() == 0)
				continue;

			Reference targetRef = targetRefs.remove(sourceRef.getId());
			if (targetRef == null) {
				Reference ref = SISPersistentManager.instance().getObject(source, Reference.class, sourceRef.getId());
				if (ref != null)
					targetAsm.getReference().add(ref);
			}
		}

		targetAsm.getReference().removeAll(targetRefs.values());
		targetAsm.toXML();

		final AssessmentPersistence saver = new AssessmentPersistence(target, targetAsm);
		saver.setAllowAdd(true);
		saver.setAllowDelete(true);
		saver.setAllowManageNotes(false);
		saver.setAllowManageReferences(false);
		saver.setDeleteFieldListener(new ComplexListener<Field>() {
			public void handleEvent(Field field) {
				try {
					FieldDAO.deleteAndDissociate(field, target);
				} catch (PersistentException e) {
					Debug.println(e);
				}
			}
		});
		saver.setDeletePrimitiveFieldListener(new ComplexListener<PrimitiveField<?>>() {
			public void handleEvent(PrimitiveField<?> field) {
				try {
					PrimitiveFieldDAO.deleteAndDissociate(field, target);
				} catch (PersistentException e) {
					Debug.println(e);
				}
			}
		});

		try {
			saver.sink(sourceAsm);
		} catch (PersistentException e) {
			Debug.println(e);
			write(" - Error merging changes, could not save: %s", e.getMessage());
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		
		Hibernate.initialize(targetAsm.getEdit());

		// This may or may not need to happen for hibernate reasons...
		targetAsm.toXML();

		if (!assessmentIO.allowedToCreateNewAssessment(targetAsm)) {
			write(" - Another assessment in this region already exists, could not save");
			throw new ResourceException(Status.CLIENT_ERROR_CONFLICT, new RegionConflictException());
		}

		AssessmentIOWriteResult result = assessmentIO.writeAssessment(targetAsm, loggedInUser, "Sync'd from Offline",
				true);

		target.flush();

		if (!result.status.isSuccess()) {
			write(" - Error saving merged changes, could not save.");
			throw new ResourceException(result.status, "AssessmentIOWrite threw exception when saving.");
		}

		if (result.edit == null)
			Debug.println("Error: No edit associated with this change. Not backing up changes.");
		else
			saver.saveChanges(targetAsm, result.edit, targetManager);

		/*
		 * After successful merge, unlock online assessment.
		 */
		if (unlock) {
			try {
				// Create objects
				HibernateLockRepository repository = new HibernateLockRepository(targetManager);
				FileLocker locker = new FileLocker(repository);
	
				// Unlock Live Assessment
				locker.persistentEagerRelease(sourceAsm.getId(), loggedInUser);
			} catch (LockException e) {
				write(" - Unable to unlock assessment %s: %s", sourceAsm.getId(), e.getMessage());
			}
		}
	}

}
