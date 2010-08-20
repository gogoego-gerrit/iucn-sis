package org.iucn.sis.server.api.locking;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.iucn.sis.shared.api.models.User;


public abstract class LockRepository {
	
	protected static final int SAVE_LOCK_EXPIRY_MS = 3 * 60 * 1000;
	
	public abstract boolean isAssessmentPersistentLocked(Integer id);
	
	public abstract LockRepository.Lock getLockedAssessment(Integer id);
	
	public abstract Map<String, List<Integer>> listGroups();
	
	public abstract List<LockRepository.Lock> listLocks();
	
	public abstract LockRepository.Lock lockAssessment(Integer id, User owner, LockType lockType);
	
	public abstract LockRepository.Lock lockAssessment(Integer id, User owner, LockType lockType, String groupID);
	
	public abstract void removeLockByID(Integer id);
	
	public abstract void clearGroup(String id);
	
	public static class Lock {

		private class LockExpiry implements Runnable {

			private Lock l;
			private LockRepository owner;

			public LockExpiry(Lock lock, LockRepository owner) {
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

		String username;
		Integer id;
		LockType lockType;
		long whenLockAcquired;
		Thread expiry;

		boolean restart = true;

		public Lock(Integer id, String username, LockType lockType, LockRepository owner) {
			this(id, username, lockType, new Date(), owner);
		}
		
		public Lock(Integer id, String username, LockType lockType, Date lockDate, LockRepository owner) {
			this.username = username;
			this.lockType = lockType;
			this.id = id;
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
				System.out.println("Restarting timer: " + toString());
				restart = true;
				expiry.interrupt();
			}
		}

		@Override
		public String toString() {
			return "Lock " + lockType + ", owned by " + username + ", for " + id;
		}
		
		public Integer getLockID() {
			return id;
		}
		
		public LockType getLockType() {
			return lockType;
		}
		
		public String getUsername() {
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
