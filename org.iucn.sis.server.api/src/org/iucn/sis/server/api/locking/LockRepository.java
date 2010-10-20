package org.iucn.sis.server.api.locking;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.models.User;

import com.solertium.util.TrivialExceptionHandler;


public abstract class LockRepository {
	
	protected static final int SAVE_LOCK_EXPIRY_MS = 3 * 60 * 1000;
	
	public abstract boolean isAssessmentPersistentLocked(Integer id);
	
	public abstract LockRepository.LockInfo getLockedAssessment(Integer id) throws LockException;
	
	public abstract Map<String, List<Integer>> listGroups() throws LockException;
	
	public abstract List<LockRepository.LockInfo> listLocks() throws LockException;
	
	public abstract LockRepository.LockInfo lockAssessment(Integer id, User owner, LockType lockType) throws LockException;
	
	public abstract LockRepository.LockInfo lockAssessment(Integer id, User owner, LockType lockType, String groupID) throws LockException;
	
	public abstract void removeLockByID(Integer id);
	
	public abstract void clearGroup(String id) throws LockException;
	
	public static class LockInfo {

		private class LockExpiry implements Runnable {

			private LockInfo l;
			private LockRepository owner;

			public LockExpiry(LockInfo lock, LockRepository owner) {
				this.l = lock;
				this.owner = owner;
			}

			public void run() {
				while (l.restart) {
					l.restart = false;

					try {
						Thread.sleep(SAVE_LOCK_EXPIRY_MS);
						owner.removeLockByID(l.id);
						
//						if( verboseOutput )
//							System.out.println("Removing lock: " + l.toString());
					} catch (InterruptedException e) {
						if (!l.restart) {
							owner.removeLockByID(l.id);
							
//							if( verboseOutput )
//								System.out.println("Removing lock: " + l.toString());
						}
					} finally {
						if (!l.restart)
							owner.removeLockByID(l.id);
					}
				}
			}
		}

		Integer userID;
		Integer id;
		LockType lockType;
		long whenLockAcquired;
		Thread expiry;
		String group;
		String username;

		boolean restart = true;

		public LockInfo(Integer id, Integer userID, LockType lockType, String group, LockRepository owner) {
			this(id, userID, lockType, group, new Date(), owner);
		}
		
		public LockInfo(Integer id, Integer userID, LockType lockType, String group, Date lockDate, LockRepository owner) {
			this.userID = userID;
			this.lockType = lockType;
			this.id = id;
			this.group = group;
			this.whenLockAcquired = lockDate.getTime();

//			if( verboseOutput )
//				System.out.println("Acquiring: " + toString());
			if (lockType.equals(LockType.SAVE_LOCK)) {
				expiry = new Thread(new LockExpiry(this, owner));
				expiry.start();
			}
		}

		public void forceExpiration(LockRepository owner) {
			if (expiry != null)
				expiry.interrupt();
			else
				owner.removeLockByID(id);
		}

		public void restartTimer() {
			// No need to restart timer for CHECKED_OUT locks, at the moment.
			if (lockType.equals(LockType.SAVE_LOCK)) {
				restart = true;
				expiry.interrupt();
			}
		}

		@Override
		public String toString() {
			return "Lock " + lockType + ", owned by " + getUsername() + ", for " + id;
		}
		
		public Integer getLockID() {
			return id;
		}
		
		public LockType getLockType() {
			return lockType;
		}
		
		public String getGroup() {
			return group;
		}
		
		public String getUsername() {
			if (username == null)
				try {
					username = SISPersistentManager.instance().getObject(User.class, userID).getUsername();
				} catch (PersistentException e) {
					TrivialExceptionHandler.ignore(this, e);
				}
			return username;
		}
		
		public long getWhenLockAcquired() {
			return whenLockAcquired;
		}
		
		public String toXML() {
			return "<" + getLockType() + " owner=\"" + getUsername() + "\" id=\"" + id + "\" aquired=\"" + whenLockAcquired + "\"/>";
		}
	}
}
