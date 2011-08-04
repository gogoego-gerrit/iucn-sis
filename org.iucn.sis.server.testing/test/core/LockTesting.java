package core;

import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.io.UserIO;
import org.iucn.sis.server.api.locking.FileLocker;
import org.iucn.sis.server.api.locking.LockException;
import org.iucn.sis.server.api.locking.LockType;
import org.iucn.sis.server.api.locking.LockRepository.LockInfo;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.User;
import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.Status;

public class LockTesting extends BasicHibernateTest {
	
	@Test
	public void lockAssessment() {
		Session session = openSession();
		UserIO userIO = new UserIO(session);
		
		FileLocker locker = SIS.get().getLocker();
		locker.verboseOutput = true;
		
		User admin = userIO.getUserFromUsername("admin");
		
		Assert.assertFalse(locker.isAssessmentPersistentLocked(12));
		
		Status status = 
			locker.persistentLockAssessment(12, LockType.CHECKED_OUT, admin);
		
		Debug.println("Status of locking is {0}", status);
		
		Assert.assertTrue(locker.isAssessmentPersistentLocked(12));
		
		LockInfo info;
		try {
			info = locker.getAssessmentPersistentLock(12);
		} catch (LockException e) {
			e.printStackTrace();
			Assert.fail("Lock exception thrown trying to get the lock: " + e.getMessage());
			return;
		}
		
		Debug.println("Lock info: {0}", info);
		
		try {
			locker.persistentEagerRelease(12, admin);
			Assert.assertFalse(locker.isAssessmentPersistentLocked(12));
		} catch (LockException e) {
			e.printStackTrace();
			Assert.assertTrue(locker.isAssessmentPersistentLocked(12));	
		}
		
		closeSession(session);
	}
	
	@Test
	public void testLockUnlockGroup() {
		Session session = openSession();
		UserIO userIO = new UserIO(session);
		
		FileLocker locker = SIS.get().getLocker();
		locker.verboseOutput = true;
		
		User admin = userIO.getUserFromUsername("admin");
		
		String groupName = "testing";
		
		for (int i = 1; i < 6; i++) {
			locker.persistentLockAssessment(i, LockType.CHECKED_OUT, admin, groupName);
			Assert.assertTrue(locker.isAssessmentPersistentLocked(i));
		}
		
		try {
			Debug.println("Waiting 10 secs so you can check the dB...");
			Thread.sleep(20000);
		} catch (Exception e) {
		}
		
		try {
			locker.persistentClearGroup(groupName);
			for (int i = 1; i < 6; i++)
				Assert.assertFalse(locker.isAssessmentPersistentLocked(i));
		} catch (LockException e) {
			Debug.println(e);
			Assert.assertTrue(locker.isAssessmentPersistentLocked(1));
		}
	}

}
