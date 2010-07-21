package org.iucn.sis.server.api.locking;

public enum LockType {
	
	SAVE_LOCK("save_lock"), 
	CHECKED_OUT("checked_out"),
	UNDER_REVIEW("under_review");
	
	public static boolean isValid(String lock) {
		return fromString(lock) != null;
	}
	
	public static LockType fromString(String lockType) {
		for (LockType lock : LockType.values())
			if (lock.toString().equals(lockType.toLowerCase()))
				return lock;
		return null;
	}
	
	private final String desc;
	
	private LockType(String desc) {
		this.desc = desc;
	}
	
	public String toString() {
		return desc;
	}

}
