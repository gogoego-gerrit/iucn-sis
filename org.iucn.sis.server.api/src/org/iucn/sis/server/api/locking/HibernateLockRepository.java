package org.iucn.sis.server.api.locking;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Lock;
import org.iucn.sis.shared.api.models.User;

public class HibernateLockRepository extends LockRepository {
	
	private final SISPersistentManager manager;

	public HibernateLockRepository() {
		manager = SISPersistentManager.instance();
	}

	@Override
	public void clearGroup(String id) throws LockException {
		Session session = manager.openSession();
		Criteria criteria = session.createCriteria(Lock.class);
		criteria = criteria.add(Restrictions.eq("group", id));
		
		List<Lock> results;
		try {
			results = criteria.list();
		} catch (HibernateException e) {
			Debug.println(e);
			throw new LockException(e);
		} finally {
			session.close();
		}
		if (results != null && !results.isEmpty()) {
			for (Lock lock : results) {
				try {
					manager.deleteObject(session, lock);
				} catch (PersistentException e) {
					Debug.println(e);
					throw new LockException(e);
				} finally {
					session.close();
				}
			}
		}
		
		session.close();
	}

	@Override
	public LockInfo getLockedAssessment(Integer lockID) {
		Session session = manager.openSession();
		Criteria criteria = session.createCriteria(Lock.class);
		criteria = criteria.add(Restrictions.eq("lockid", lockID));
		
		Lock lock;
		try {
			lock = (Lock)criteria.list().get(0);
		} catch (NullPointerException e) {
			return null;
		} catch (IndexOutOfBoundsException e) {
			return null;
		} catch (ClassCastException e) {
			return null;
		} finally {
			session.close();
		}
		
		LockInfo info = new LockInfo(lock.getLockID(), lock.getUser().getId(), 
				LockType.fromString(lock.getType()), lock.getGroup(), lock.getDate(), this);
		
		session.close();
		
		return info;
	}

	@Override
	public boolean isAssessmentPersistentLocked(Integer id) {
		Session session = manager.openSession();
		boolean result = isAssessmentPersistentLocked(session, id);
		session.close();
		return result;
	}
	
	public boolean isAssessmentPersistentLocked(Session session, Integer id) {
		Criteria criteria = session.createCriteria(Lock.class);
		criteria = criteria.add(Restrictions.eq("lockid", id));
		
		List list = criteria.list();
		
		return list != null && !list.isEmpty();
	}

	@Override
	public Map<String, List<Integer>> listGroups() {
		Session session = manager.openSession();
		List<Lock> list;
		try {
			list = manager.listObjects(Lock.class, session);
		} catch (PersistentException e) {
			return new HashMap<String, List<Integer>>();
		} finally {
			session.close();
		}
		
		final Map<String, List<Integer>> map = 
			new ConcurrentHashMap<String, List<Integer>>();
		
		for (Lock lock : list) {
			final String groupID = lock.getGroup();
			
			List<Integer> l = new ArrayList<Integer>();
			l.add(lock.getId());
			
			map.put(groupID, l);
		}
		
		session.close();
		
		return map;
	}

	@Override
	public List<LockInfo> listLocks() {
		final List<LockInfo> list = new ArrayList<LockRepository.LockInfo>();
		final Session session = manager.openSession();
		final List<Lock> locks;
		try {
			locks = manager.listObjects(Lock.class, session);
		} catch (PersistentException e) {
			return new ArrayList<LockRepository.LockInfo>();
		} finally {
			session.close();
		}
		
		for (Lock lock : locks)
			list.add(new LockInfo(lock.getLockID(), 
					lock.getUser().getId(), 
					LockType.fromString(lock.getType()), 
					lock.getGroup(), lock.getDate(), this));
		
		session.close();
		
		return list;
	}

	@Override
	public LockInfo lockAssessment(Integer id, User owner, LockType lockType) {
		return lockAssessment(id, owner, lockType, null);
	}

	@Override
	public LockInfo lockAssessment(Integer id, User owner, LockType lockType, String groupID) {
		if (isAssessmentPersistentLocked(id))
			return getLockedAssessment(id);
		
		Lock lock = new Lock();
		lock.setLockID(id);
		lock.setUser(owner);
		lock.setDate(new Date());
		lock.setType(lockType.toString());
		lock.setGroup(groupID);
		
		Session session = manager.openSession();
		
		try {
			manager.saveObject(session, lock);
		} catch (PersistentException e) {
			return null;
		} finally {
			session.close();
		}
		
		LockInfo result = new LockInfo(lock.getLockID(), lock.getUser().getId(), 
				LockType.fromString(lock.getType()), groupID, this);
		
		session.close();
		
		return result;
	}

	@Override
	public void removeLockByID(Integer id) {
		Session session = manager.openSession();
		Criteria criteria = session.createCriteria(Lock.class);
		criteria = criteria.add(Restrictions.eq("lockid", id));
		
		Lock lock;
		try {
			lock = (Lock)criteria.list().get(0);
		} catch (NullPointerException e) {
			return;
		} catch (IndexOutOfBoundsException e) {
			return;
		} catch (ClassCastException e) {
			return;
		} finally {
			session.close();
		}
		
		try {
			manager.deleteObject(session, lock);
		} catch (PersistentException e) {
			Debug.println(e);
		} finally {
			session.close();
		}
	}
}
