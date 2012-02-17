package org.iucn.sis.server.extensions.offline;

import java.util.Properties;

import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.io.UserIO;
import org.iucn.sis.server.api.locking.FileLocker;
import org.iucn.sis.server.api.locking.HibernateLockRepository;
import org.iucn.sis.server.api.locking.LockException;
import org.iucn.sis.server.api.locking.LockType;
import org.iucn.sis.server.api.locking.LockRepository.LockInfo;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.utils.SISGlobalSettings;
import org.iucn.sis.server.utils.WorkingSetExporter;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.User;
import org.restlet.data.Status;

import com.solertium.db.DBException;

public class OfflineWorkingSetReExporter extends WorkingSetExporter {
	
	private final SISPersistentManager onlineSourceManager;
	
	public OfflineWorkingSetReExporter(Integer workingSetID, String username, SISPersistentManager online) {
		super(workingSetID, username, true, null, null);
		
		this.onlineSourceManager = online;
	}
	
	protected void afterRun() throws DBException {
		//Nothing to do!
	};
	
	@Override
	protected void execute() throws Throwable {
		SISPersistentManager.instance().shutdown();
		
		Properties properties = new Properties(SIS.get().getSettings(null));
		properties.setProperty(SISGlobalSettings.GENERATOR, "assigned");
		
		SISPersistentManager targetManager = SISPersistentManager.newInstance("sis", properties, true);
		try {
			copy(source = onlineSourceManager.openSession(), target = targetManager.openSession(), true);
		} finally {
			targetManager.shutdown();
			SISPersistentManager.refresh();
		}
	}
	
	protected void lockAssessment(Assessment assessment) throws DBException {
		Session session = onlineSourceManager.openSession();
		User user = new UserIO(session).getUserFromUsername(username);
		
		HibernateLockRepository repository = new HibernateLockRepository(onlineSourceManager);
		FileLocker locker = new FileLocker(repository);
		
		Status ret = locker.persistentLockAssessment(
			assessment.getId(), LockType.CHECKED_OUT, user,
				workingSetID + "");

		if (!ret.isSuccess()) {
			LockInfo lock;
			try {
				lock = locker.getAssessmentPersistentLock(assessment.getId());
			} catch (LockException e) {
				return;
			}
			if (lock.getLockType().equals(LockType.CHECKED_OUT)) {
				write("Global draft assessment for " + assessment.getTaxon().getFullName()
						+ " is already checked out by " + lock.getUsername());
			} else {
				write("Global draft assessment for " + assessment.getTaxon().getFullName()
						+ " is locked for saving by " + lock.getUsername() + ". Save"
						+ " locks are granted for a 2 minute period after each save." 
						+ " Please try again later.");
			}
		} else
			locked.put(assessment.getId(), username);
		
		session.close();
	}
	
	@Override
	public void close() {
		//Don't close the stream
	}

}
