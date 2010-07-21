package org.iucn.sis.server.locking;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import org.iucn.sis.server.locking.LockRepository.Lock;
import org.iucn.sis.server.simple.SISContainerApp;
import org.iucn.sis.server.utils.DocumentUtils;
import org.iucn.sis.server.utils.ServerPaths;
import org.iucn.sis.shared.BaseAssessment;
import org.restlet.data.Status;

import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;

public class FileLocker {
	public class SimpleLock {

		Date date;

		public SimpleLock() {
			date = new Date();
		}
	}

	public final static FileLocker impl = new FileLocker();

	ConcurrentHashMap<String, SimpleLock> locks;
	
	private LockRepository assessmentLocks;

	public boolean verboseOutput = true;
	
	private FileLocker() {
		locks = new ConcurrentHashMap<String, SimpleLock>();
		//assessmentLocks = new ConcurrentHashMap<String, Lock>();
		
		if(SISContainerApp.amIOnline)
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
		boolean locked = FileLocker.impl.aquireLock(url);
		if (!locked) {
			int count = 0;
			do {
				try {
					Thread.sleep(250);
				} catch (Exception e) {
				}
				locked = FileLocker.impl.aquireLock(url);
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
	public boolean isAssessmentPersistentLocked(String id, String type) {
		return assessmentLocks.isAssessmentPersistentLocked(id, type);
	}
	
	/**
	 * Checks if the assessment is locked.
	 * 
	 * @param id
	 * @param type
	 * @return true, if it's locked
	 */
	public Lock getAssessmentPersistentLock(String id, String type) {
		return assessmentLocks.getLockedAssessment(id, type);		
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
	 * @return either updated version's XML, or null
	 */
	public String checkAssessmentAvailability(String id, String type, String modDate, String username, VFS vfs) {
		if (isAssessmentPersistentLocked(id, type)) {
			LockRepository.Lock lock = assessmentLocks.getLockedAssessment(id, type);
			String ret = "<" + lock.getLockType() + " owner=\"" + lock.getUsername() + "\"/>";
			return ret;
		} else {

			VFSPath uri;

			if (type.equals(BaseAssessment.PUBLISHED_ASSESSMENT_STATUS)) {
				uri = new VFSPath(ServerPaths.getPublishedAssessmentURL(id));
			} else if (type.equals(BaseAssessment.DRAFT_ASSESSMENT_STATUS)) {
				String path = ServerPaths.getDraftAssessmentURL(id);
				uri = new VFSPath(path);
			} else {
				uri = new VFSPath(ServerPaths.getUserAssessmentUrl(username, id));
			}

			try {
				long lastModified = vfs.getLastModified(uri);
				if (lastModified > Long.parseLong(modDate)) {
					String ret = "<update owner=\"" + vfs.getMetadata(uri).getArbitraryData().get("owner") + "\">\n";
					String assXML = DocumentUtils.getVFSFileAsString(uri.toString(), vfs);
					long lastMod = vfs.getLastModified(uri);
					assXML = assXML.replaceAll("<\\?xml\\s*(version=.*)?\\s*(encoding=.*)?\\?>", "");
					assXML = assXML.replaceAll("(<dateModified>.*?</dateModified>)|(<dateModified\\s*/>)",
							"<dateModified>" + lastMod + "</dateModified>");
					ret += assXML;
					ret += "</update>";

					return ret;
				} else {
					return null;
				}
			} catch (NotFoundException e) {
				return null;
			}

		}
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
	public synchronized Status persistentLockAssessment(String id, String assessmentType, LockType lockType, String owner) {
		return persistentLockAssessment(id, assessmentType, lockType, owner, null);
	}
	
	public synchronized Status persistentLockAssessment(String id, String assessmentType, LockType lockType, String owner, String group) {
		if (assessmentLocks.isAssessmentPersistentLocked(id, assessmentType)) {
			LockRepository.Lock l = assessmentLocks.getLockedAssessment(id, assessmentType);
			if (l.getUsername().equalsIgnoreCase(owner)) {
				l.restartTimer();
				return Status.SUCCESS_OK;
			} else {
				if( verboseOutput )
					System.out.println("You can't have the lock " + owner + ", it's already owned by " + l.username);
				return Status.CLIENT_ERROR_FORBIDDEN;
			}
		} else {
			//assessmentLocks.put(id + assessmentType, new Lock(id + assessmentType, owner, lockType));
			assessmentLocks.lockAssessment(id, assessmentType, owner, lockType, group);
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
	public synchronized Status persistentEagerRelease(String id, String assessmentType, String owner) {
		if (assessmentLocks.isAssessmentPersistentLocked(id, assessmentType)) {
			LockRepository.Lock l = assessmentLocks.getLockedAssessment(id, assessmentType);
			if (l.getUsername().equalsIgnoreCase(owner)) {
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
