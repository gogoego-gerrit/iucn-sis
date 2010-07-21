package org.iucn.sis.server.locking;

import java.util.concurrent.ConcurrentHashMap;

public class MemoryLockRepository extends LockRepository {
	
	private ConcurrentHashMap<String, LockRepository.Lock> assessmentLocks;
	
	public MemoryLockRepository() {
		this.assessmentLocks = new ConcurrentHashMap<String, LockRepository.Lock>();
	}
	
	public boolean isAssessmentPersistentLocked(String id, String type) {
		return assessmentLocks.containsKey(id + type);
	}
	
	public LockRepository.Lock getLockedAssessment(String id, String type) {
		return assessmentLocks.get(id + type);
	}
	
	public LockRepository.Lock lockAssessment(String id, String type, String owner, LockType lockType) {
		return lockAssessment(id, type, owner, lockType, null);
	}
	
	public LockRepository.Lock lockAssessment(String id, String type, String owner, LockType lockType, String groupID) {
		final LockRepository.Lock lock = new Lock(id + type, owner, lockType, this);
		assessmentLocks.put(id + type, lock);
		return lock;
	}
	
	public void removeLockByID(String id) {
		assessmentLocks.remove(id);
	}
	
	public void clearGroup(String id) {
		//Groups are supported in memory...
	}
	
}
