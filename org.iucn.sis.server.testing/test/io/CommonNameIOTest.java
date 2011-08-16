package io;

import junit.framework.Assert;

import org.hibernate.Session;
import org.iucn.sis.server.api.io.CommonNameIO;
import org.iucn.sis.server.api.io.TaxonIO;
import org.iucn.sis.server.api.io.UserIO;
import org.iucn.sis.server.api.persistance.CommonNameCriteria;
import org.iucn.sis.server.api.persistance.CommonNameDAO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.utils.TaxomaticException;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.CommonName;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.User;
import org.junit.After;
import org.junit.Test;

import core.BasicHibernateTest;

public class CommonNameIOTest extends BasicHibernateTest {
	
	private static final int TEST_TAXON = 3;
	
	@Test
	public void testCreate() {
		Session session = openTransaction();
		
		TaxonIO taxonIO = new TaxonIO(session);
		CommonNameIO io = new CommonNameIO(session);
		UserIO userIO = new UserIO(session);
		
		User user = userIO.getUserFromUsername("admin");
		Taxon taxon = taxonIO.getTaxon(TEST_TAXON);
		
		CommonName test = new CommonName();
		test.setName("JUnit Test Name");
		
		try {
			io.add(taxon, test, user);
		} catch (TaxomaticException e) {
			Assert.fail();
		}
	
		closeTransaction(session);
		
		int testID = 0;
		Assert.assertTrue(test.getId() > 0);
		
		testID = test.getId();
		
		session = openSession();
		
		io = new CommonNameIO(session);
		taxonIO = new TaxonIO(session);
		
		test = io.get(testID);
		
		Assert.assertNotNull(test);
		Assert.assertEquals(testID, test.getId());
		
		taxon = taxonIO.getTaxon(TEST_TAXON);
		
		boolean found = false;
		for (CommonName name : taxon.getCommonNames()) {
			if (found = name.getId() == testID)
				break;
		}
		Assert.assertTrue(found);
	}
	
	@Test
	public void testUpdate() {
		Session session = openTransaction();
		
		TaxonIO taxonIO = new TaxonIO(session);
		UserIO userIO = new UserIO(session);
		
		CommonNameIO io = new CommonNameIO(session);
		
		User user = userIO.getUserFromUsername("admin");
		Taxon taxon = taxonIO.getTaxon(TEST_TAXON);
		
		CommonName test = new CommonName();
		test.setName("JUnit Test Name");
		
		try {
			io.add(taxon, test, user);
		} catch (TaxomaticException e) {
			Assert.fail();
		}
	
		closeTransaction(session);
		
		int testID = 0;
		Assert.assertTrue(test.getId() > 0);
		
		testID = test.getId();
		
		session = openTransaction();
		
		io = new CommonNameIO(session);
		taxonIO = new TaxonIO(session);
		
		test = io.get(testID);
		
		Assert.assertNotNull(test);
		Assert.assertEquals(testID, test.getId());
		Assert.assertEquals("JUnit Test Name", test.getName());
		
		test.setName("JUnit Test Name Different");
		
		try {
			io.update(test);
		} catch (PersistentException e) {
			Assert.fail();
		}
		
		Assert.assertNotSame("JUnit Test Name", test.getName());
	}
	
	@Test
	public void testDelete() {
		Session session = openTransaction();
		
		TaxonIO taxonIO = new TaxonIO(session);
		UserIO userIO = new UserIO(session);
		
		CommonNameIO io = new CommonNameIO(session);
		
		User user = userIO.getUserFromUsername("admin");
		Taxon taxon = taxonIO.getTaxon(TEST_TAXON);
		
		CommonName test = new CommonName();
		test.setName("JUnit Test Name");
		
		try {
			io.add(taxon, test, user);
		} catch (TaxomaticException e) {
			Assert.fail();
		}
	
		closeTransaction(session);
		
		int testID = 0;
		Assert.assertTrue(test.getId() > 0);
		
		testID = test.getId();
		
		session = openTransaction();
		
		io = new CommonNameIO(session);
		taxonIO = new TaxonIO(session);
		
		test = io.get(testID);
		
		Assert.assertNotNull(test);
		Assert.assertEquals(testID, test.getId());
		
		try {
			io.delete(testID, user);
		} catch (TaxomaticException e) {
			Assert.fail("Persistent Exception thrown: " + e.getMessage());
		}
		
		closeTransaction(session);
		
		session = openSession();
		
		io = new CommonNameIO(session);
		taxonIO = new TaxonIO(session);
		
		test = io.get(testID);
		
		Assert.assertNull(test);
		
		taxon = taxonIO.getTaxon(TEST_TAXON);
		boolean found = false;
		for (CommonName name : taxon.getCommonNames()) {
			if (found = name.getId() == testID)
				break;
		}
		Assert.assertFalse(found);
	}
	
	@Test
	public void testSetPrimary() {
		Session session = openTransaction();
		
		TaxonIO taxonIO = new TaxonIO(session);
		UserIO userIO = new UserIO(session);
		
		CommonNameIO io = new CommonNameIO(session);
		
		User user = userIO.getUserFromUsername("admin");
		Taxon taxon = taxonIO.getTaxon(TEST_TAXON);
		
		CommonName test = new CommonName();
		test.setName("JUnit Test Name");
		
		CommonName test2 = new CommonName();
		test2.setName("JUnit Test Name 2");
		
		try {
			io.add(taxon, test, user);
			session.flush();
			io.add(taxon, test2, user);
		} catch (TaxomaticException e) {
			Assert.fail();
		}
	
		closeTransaction(session);
		
		int testID = 0, test2ID = 0;
		
		testID = test.getId();
		test2ID = test2.getId();
		
		Debug.println("Created {0} and {1}", test, test2);
		
		session = openTransaction();
		
		io = new CommonNameIO(session);
		taxonIO = new TaxonIO(session);
		
		test = io.get(testID);
		test2 = io.get(test2ID);
		
		
		
		Assert.assertNotNull(test);
		Assert.assertNotNull(test2);
		
		Assert.assertTrue(test.isPrimary());
		Assert.assertFalse(test2.isPrimary());
		
		io.setPrimary(test2ID);
		
		Assert.assertTrue(test2.isPrimary());
		Assert.assertFalse(test.isPrimary());
		
		closeTransaction(session);
		
		session = openSession();
		
		io = new CommonNameIO(session);
		
		test2 = io.get(test2ID);
		
		Assert.assertTrue(test2.isPrimary());
	}
		
	
	@After
	public void afterMethod() {
		Session session = openTransaction();
		CommonNameCriteria criteria = new CommonNameCriteria(session);
		criteria.name.ilike("%JUnit Test%");
		CommonName[] names = criteria.listCommonName();
		if (names != null) {
			for (CommonName name : names) {
				try {
					CommonNameDAO.deleteAndDissociate(name, session);
					Debug.println("Deleted test common name {0}: {1}", name.getId(), name.getName());
				} catch (Exception e) {
					Debug.println("Failed to delete test common name {0}: {1}", name.getId(), name.getName());
				}
			}
			session.getTransaction().commit();
		}
		session.close();
	}

}
