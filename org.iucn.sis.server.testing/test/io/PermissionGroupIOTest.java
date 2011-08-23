package io;

import junit.framework.Assert;

import org.hibernate.Session;
import org.iucn.sis.server.api.io.PermissionGroupIO;
import org.iucn.sis.server.api.persistance.PermissionGroupCriteria;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Permission;
import org.iucn.sis.shared.api.models.PermissionGroup;
import org.junit.After;
import org.junit.Test;
import org.restlet.resource.ResourceException;

import core.BasicHibernateTest;

public class PermissionGroupIOTest extends BasicHibernateTest {
	
	/**
	 * Ensure I can add and edit PermissionGroup.  If this 
	 * doesn't work, other tests will clearly fail.
	 */
	@Test
	public void testAddEditPermissionGroup() {
		Session session = openTransaction();
		PermissionGroupIO io = new PermissionGroupIO(session);
		
		Permission permission = new Permission();
		permission.setUrl("default");
		permission.setRead(false);
		permission.setWrite(false);
		permission.setDelete(false);
		permission.setUse(false);
		permission.setType("");		
		
		PermissionGroup newGroup = new PermissionGroup();
		newGroup.setName("JUnitGroup");
		newGroup.addPermission(permission);
		newGroup.setScopeURI("");
		permission.setPermissionGroup(newGroup);
		
		Debug.println("New Group Default ID -"+newGroup.getId());
		
		try {
			io.savePermissionGroup(newGroup);
			session.getTransaction().commit();
		} catch (Exception e) {
			Assert.fail("Exception thrown: " + e.getMessage());
		}
		
		int id = newGroup.getId();
		Debug.println("New Group ID -"+id);
		session.beginTransaction();
		Assert.assertTrue(id != 0);
		
		Permission featurePermission = new Permission();
		featurePermission.setUrl("feature/batchChange");
		featurePermission.setRead(true);
		featurePermission.setWrite(true);
		featurePermission.setDelete(false);
		featurePermission.setUse(true);
		featurePermission.setType("");
		newGroup.addPermission(featurePermission);
		featurePermission.setPermissionGroup(newGroup);
		
		try {
			io.updatePermissionGroup(newGroup);
			session.getTransaction().commit();
		} catch (Exception e) {
			Assert.fail("Exception thrown: " + e.getMessage());
		}
		
		try{
			PermissionGroup existing = io.getPermissionGroup(newGroup.getName());
			Assert.assertNotNull(existing);
			Assert.assertEquals(id, existing.getId());
			Assert.assertEquals("JUnitGroup", existing.getName());
		}catch(Exception e){
			Assert.fail("Exception thrown: " + e.getMessage());
		}
	}
	
	/**
	 * Ensure I can add and delete PermissionGroup.  
	 */
	@Test
	public void testDeletePermissionGroup() {
		Session session = openTransaction();
		PermissionGroupIO io = new PermissionGroupIO(session);
		
		Permission permission = new Permission();
		permission.setUrl("default");
		permission.setRead(false);
		permission.setWrite(false);
		permission.setDelete(false);
		permission.setUse(false);
		permission.setType("");		
		
		PermissionGroup newGroup = new PermissionGroup();
		newGroup.setName("JUnitGroup");
		newGroup.addPermission(permission);
		newGroup.setScopeURI("");
		permission.setPermissionGroup(newGroup);
		
		Debug.println("New Group Default ID -"+newGroup.getId());
		
		try {
			io.savePermissionGroup(newGroup);
			session.getTransaction().commit();
		} catch (Exception e) {
			Assert.fail("Exception thrown: " + e.getMessage());
		}
		
		int id = newGroup.getId();
		String name = newGroup.getName();
		Debug.println("New Group ID -"+id);
		session.beginTransaction();
		Assert.assertTrue(id != 0);
		
	
		try {
			io.deletePermissionGroup(newGroup);
			session.getTransaction().commit();
		} catch (Exception e) {
			Assert.fail("Exception thrown: " + e.getMessage());
		}
		
		try{
			PermissionGroup deleted = io.getPermissionGroup(name);
			Assert.assertNull(deleted);
		}catch(Exception e){
			Assert.fail("Exception thrown: " + e.getMessage());
		}
	}	
	
	/**
	 * Ensure that I can not rename an  
	 * existing PermissionGroup to a PermissionGroup that 
	 * already exists.
	 */
	@Test
	public void testRenameToExisting() {
		Session session = openTransaction();
		PermissionGroupIO io = new PermissionGroupIO(session);
		
		Permission permission = new Permission();
		permission.setUrl("default");
		permission.setRead(false);
		permission.setWrite(false);
		permission.setDelete(false);
		permission.setUse(false);
		permission.setType("");		
		
		PermissionGroup newGroup = new PermissionGroup();
		newGroup.setName("JUnitGroup");
		newGroup.addPermission(permission);
		newGroup.setScopeURI("");
		permission.setPermissionGroup(newGroup);
		
		try {
			io.savePermissionGroup(newGroup);
			session.getTransaction().commit();
		} catch (Exception e) {
			Assert.fail("Exception thrown: " + e.getMessage());
		}
		
		int groupID = newGroup.getId();
		String groupName = newGroup.getName();
		Assert.assertFalse(groupID == 0);
		
		session.beginTransaction();
		
		newGroup = new PermissionGroup();
		newGroup.setName("JUnitGroupExisting");
		
		try {
			io.savePermissionGroup(newGroup);
			closeTransaction(session);
		} catch (Exception e) {
			Assert.fail("Exception thrown: " + e.getMessage());
		}
		
		Assert.assertFalse(newGroup.getId() == 0);
		Assert.assertFalse(newGroup.getId() == groupID);
		
		session = openTransaction();
		io = new PermissionGroupIO(session);
		
		try {
			PermissionGroup existing = io.getPermissionGroup(groupName);
			Assert.assertNotNull(existing);
			
			existing.setName("JUnitGroupExisting");
				
			io.savePermissionGroup(existing);
			closeTransaction(session);
			Assert.fail("Region using the same name got saved successfully");
		} catch (PersistentException e) {
			Assert.fail();
		} catch (ResourceException e) {
			session.getTransaction().rollback();
			Debug.println("Failed to save regions, probably a good thing...: {0}", e.getMessage());
		}
	}
	
	
	/**
	 * Ensure that no duplicates are allowed, that is,
	 * if I add two regions with the same name, it 
	 * should not work.
	 */
	@Test
	public void testNoDuplicatesAllowed() {
		//final String sameName = "JUnit Test Region";
		Session session = openTransaction();
		PermissionGroupIO io = new PermissionGroupIO(session);
		
		Permission permission = new Permission();
		permission.setUrl("default");
		permission.setRead(false);
		permission.setWrite(false);
		permission.setDelete(false);
		permission.setUse(false);
		permission.setType("");		
		
		PermissionGroup newGroup = new PermissionGroup();
		newGroup.setName("JUnitGroup");
		newGroup.addPermission(permission);
		newGroup.setScopeURI("");
		permission.setPermissionGroup(newGroup);
		
		try {
			io.savePermissionGroup(newGroup);
			session.getTransaction().commit();
		} catch (Exception e) {
			Assert.fail("Exception thrown: " + e.getMessage());
		}
		
		int firstID = newGroup.getId();
		closeSession(session);
		
		Assert.assertTrue(firstID != 0);
		
		session = openTransaction();
		io = new PermissionGroupIO(session);
		
		int size = 0;
		try {
			size = io.getPermissionGroups().size();
		} catch (PersistentException e) {
			Assert.fail("Dependency failed: list regions");
		}
		
		PermissionGroup anotherGroup = new PermissionGroup();
		anotherGroup.setName("JUnitGroup");
	
		try {
			io.savePermissionGroup(anotherGroup);
			session.getTransaction().commit();
			Debug.println("This should have failed...");
		} catch (PersistentException e) {
			Assert.fail(e.getMessage());
		} catch (ResourceException e) {
			session.getTransaction().rollback();
			Debug.println("Failed to save regions, probably a good thing...: {0}", e.getMessage());
		}
		
		int newSize = 0;
		try {
			newSize = io.getPermissionGroups().size();
		} catch (PersistentException e) {
			Assert.fail("Dependency failed: list regions");
		}
		
		Assert.assertTrue(anotherGroup.getId() == 0);
		Assert.assertEquals(size, newSize);
	}
	
	
	/**
	 * This method wipes any groups created via this 
	 * JUnit test (named something ilike "JUnitGroup") 
	 * from the database.
	 */
	@After
	public void afterMethod() {
		Session session = openTransaction();
		PermissionGroupCriteria criteria = new PermissionGroupCriteria(session);
		criteria.name.ilike("%JUnitGroup%");
		PermissionGroup[] groups = criteria.listPermission();
		if (groups != null) {
			PermissionGroupIO io = new PermissionGroupIO(session);
			for (PermissionGroup group : groups) {
				try {
					io.deletePermissionGroup(group);
					Debug.println("Deleted test region {0}: {1}", group.getName());
				} catch (Exception e) {
					Debug.println("Failed to delete test region {0}: {1}", group.getName());
				}
			}
			session.getTransaction().commit();
		}
		session.close();
	}

}
