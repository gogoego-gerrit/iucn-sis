package org.iucn.sis.server.api.locking;

import java.util.List;
import java.util.Map;

import org.iucn.sis.shared.api.models.User;

public class HibernateLockRepository extends LockRepository {

	public HibernateLockRepository() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void clearGroup(String id) {
		// TODO Auto-generated method stub

	}

	@Override
	public Lock getLockedAssessment(Integer id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAssessmentPersistentLocked(Integer id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Map<String, List<Integer>> listGroups() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Lock> listLocks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Lock lockAssessment(Integer id, User owner, LockType lockType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Lock lockAssessment(Integer id, User owner, LockType lockType,
			String groupID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeLockByID(Integer id) {
		// TODO Auto-generated method stub

	}

}
