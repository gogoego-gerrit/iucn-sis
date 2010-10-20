package org.iucn.sis.server.api.locking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.iucn.sis.shared.api.models.User;

public class MemoryLockRepository extends LockRepository {
	
	private ConcurrentHashMap<Integer, LockRepository.LockInfo> assessmentLocks;
	
	public MemoryLockRepository() {
		this.assessmentLocks = new ConcurrentHashMap<Integer, LockRepository.LockInfo>();
	}
	
	public boolean isAssessmentPersistentLocked(Integer id) {
		return assessmentLocks.containsKey(id);
	}
	
	public List<LockInfo> listLocks() {
		return new ArrayList<LockInfo>(assessmentLocks.values());
	}
	
	@Override
	public Map<String, List<Integer>> listGroups() {
		//Groups are not supported in memory...
		
		return new HashMap<String, List<Integer>>();
	}
	
	public LockRepository.LockInfo lockAssessment(Integer id, User owner, LockType lockType) {
		return lockAssessment(id, owner, lockType, null);
	}
	
	public LockRepository.LockInfo lockAssessment(Integer id, User owner, LockType lockType, String groupID) {
		String username = "SIS server";
		if (owner != null)
			username = owner.getUsername();
		final LockRepository.LockInfo lock = new LockInfo(id, owner.getId(), lockType, null, this);
		assessmentLocks.put(id, lock);
		return lock;
	}
	
	public void clearGroup(String id) {
		//Groups are not supported in memory...
	}

	@Override
	public LockInfo getLockedAssessment(Integer id) {
		return assessmentLocks.get(id);
	}

	@Override
	public void removeLockByID(Integer id) {
		assessmentLocks.remove(id);		
	}
	
}
