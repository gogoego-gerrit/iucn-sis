package org.iucn.sis.server.api.locking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.locking.LockRepository.Lock;
import org.iucn.sis.server.api.utils.OnlineUtil;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Edit;
import org.iucn.sis.shared.api.models.User;
import org.restlet.data.Status;

public class FileLocker {
	public class SimpleLock {

		Date date;

		public SimpleLock() {
			date = new Date();
		}
	}


	ConcurrentHashMap<String, SimpleLock> locks;
	
	private LockRepository assessmentLocks;

	public boolean verboseOutput = true;
	
	/**
	 * Should only be one instantiation of this, called from SIS API
	 */
	public FileLocker() {
		locks = new ConcurrentHashMap<String, SimpleLock>();
		//assessmentLocks = new ConcurrentHashMap<String, Lock>();
		
		if(OnlineUtil.amIOnline())
			assessmentLocks = new PersistentLockRepository();
		else
			assessmentLocks = new MemoryLockRepository();
	}

	public boolean aquireLock(String url) {
		boolean aquiredLock = false;

		synchronized (locks) {
			if (!locks.contains(url)) {
				locks.put(url, new SimpleLock());
				aquiredLock = true;
			}
		}

		return aquiredLock;
	}

	public boolean aquireWithRetry(String url, int maxTries) {
		boolean locked = aquireLock(url);
		if (!locked) {
			int count = 0;
			do {
				try {
					Thread.sleep(250);
				} catch (Exception e) {
				}
				locked = aquireLock(url);
				count++;
			} while (count < maxTries && !locked);
		}
		return locked;
	}

	/**
	 * Checks if the assessment is locked.
	 * 
	 * @param id
	 * @param type
	 * @return true, if it's locked
	 */
	public boolean isAssessmentPersistentLocked(Integer id) {
		return assessmentLocks.isAssessmentPersistentLocked(id);
	}
	
	/**
	 * Checks if the assessment is locked.
	 * 
	 * @param id
	 * @param type
	 * @return true, if it's locked
	 */
	public Lock getAssessmentPersistentLock(Integer assessmentID) {
		return assessmentLocks.getLockedAssessment(assessmentID);		
	}

	/**
	 * Checks if the assessment is locked or has been changed, based on its id,
	 * type and modDate. Returns the XML of the updated version, if needed.
	 * Otherwise, returns null.
	 * 
	 * @param id
	 * @param type
	 * @param modDate
	 * @param username
	 * @return either lock XML if locked,
	 * * assessment XML if not locked and user version out of date, or null if not locked and user version is valid
	 */
	public String checkAssessmentAvailability(Integer id, String modDate, User user) {
		if (isAssessmentPersistentLocked(id)) {
			LockRepository.Lock lock = assessmentLocks.getLockedAssessment(id);
			return lock.toXML();
		} else {
						
			Assessment ass = SIS.get().getAssessmentIO().getAssessment(id);
			if (ass != null) {
				Edit lastEdit = ass.getLastEdit();
				long lastModified = lastEdit == null ? 0 : lastEdit.getCreatedDate().getTime();
				if (lastModified > Long.parseLong(modDate)) {
					String ret = "<update owner=\""
							+ lastEdit.getUser().getUsername() + "\">\n";
					String assXML = ass.toXML();
					assXML = assXML.replaceAll(
							"<\\?xml\\s*(version=.*)?\\s*(encoding=.*)?\\?>",
							"");
					assXML = assXML
							.replaceAll(
									"(<dateModified>.*?</dateModified>)|(<dateModified\\s*/>)",
									"<dateModified>" + lastModified
											+ "</dateModified>");
					ret += assXML;
					ret += "</update>";

					return ret;
				} else {
					return null;
				}
			} else {
				return null;
			}

		}
	}
	
	/**
	 * Tries to lock all assessments.  If unable to lock all of them, then returns status returned by persistentLockAssessment.  
	 * Otherwise returns success ok.
	 * 
	 * 
	 * @param assessments
	 * @param lockType
	 * @param owner
	 * @return
	 */
	public synchronized Status persistentLockAssessments(Collection<Assessment> assessments, LockType lockType, User owner) {
		List<Assessment> locked = new ArrayList<Assessment>();
		Status status = null;
		for (Assessment assessment : assessments) {
			Status assStatus = persistentLockAssessment(assessment.getId(), lockType, owner);
			if (assStatus.isError()) {
				status = assStatus;
				break;
			} else {
				locked.add(assessment);
			}
		}
		
		if (status != null) {
			for (Assessment ass : locked)
				persistentEagerRelease(ass.getId(), owner);
		} else {
			status = Status.SUCCESS_OK;
		}
		return status;
	}

	/**
	 * Actually does locking. Checks to make sure lock type is valid (fail:
	 * return Bad Request), that it's not already locked by someone else (fail:
	 * return Forbidden), then locks it (or restarts the timer) and returns
	 * Success_OK.
	 * 
	 * @param id
	 * @param assessmentType
	 * @param lockType
	 * @param owner
	 * @return a Status - see above
	 */
	public synchronized Status persistentLockAssessment(Integer assessmentID, LockType lockType, User owner) {
		return persistentLockAssessment(assessmentID, lockType, owner, null);
	}
	
	public synchronized Status persistentLockAssessment(Integer assessmentID, LockType lockType, User owner, String group) {
		if (assessmentLocks.isAssessmentPersistentLocked(assessmentID)) {
			LockRepository.Lock l = assessmentLocks.getLockedAssessment(assessmentID);
			if (l.getUsername().equalsIgnoreCase(owner.getUsername())) {
				l.restartTimer();
				return Status.SUCCESS_OK;
			} else {
				if( verboseOutput )
					System.out.println("You can't have the lock " + owner + ", it's already owned by " + l.username);
				return Status.CLIENT_ERROR_FORBIDDEN;
			}
		} else {
			//assessmentLocks.put(id + assessmentType, new Lock(id + assessmentType, owner, lockType));
			assessmentLocks.lockAssessment(assessmentID, owner, lockType, group);
			return Status.SUCCESS_OK;
		}
	}
	
	

	/**
	 * Eagerly releases a lock.
	 * 
	 * @param id
	 * @param assessmentType
	 * @param owner
	 * @return a Status - see above
	 */
	public synchronized Status persistentEagerRelease(Integer id, User owner) {
		if (assessmentLocks.isAssessmentPersistentLocked(id)) {
			LockRepository.Lock l = assessmentLocks.getLockedAssessment(id);
			if (l.getUsername().equalsIgnoreCase(owner.getUsername())) {
				l.forceExpiration(assessmentLocks);
				return Status.SUCCESS_OK;
			} else {
				return Status.CLIENT_ERROR_FORBIDDEN;
			}
		} else {
			return Status.CLIENT_ERROR_NOT_FOUND;
		}
	}
	
	public void persistentClearGroup(String groupID) {
		assessmentLocks.clearGroup(groupID);
	}

	public void releaseLock(String url) {
		synchronized (locks) {
			locks.remove(url);
		}
	}

}
