package extensions;

import java.util.Arrays;

import javax.naming.NamingException;

import junit.framework.Assert;

import org.hibernate.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.application.SISDBAuthenticator;
import org.iucn.sis.server.api.io.TaxonIO;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.TaxonCriteria;
import org.iucn.sis.server.extensions.demimport.DEMImport;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Permission;
import org.iucn.sis.shared.api.models.PermissionGroup;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonHierarchy;
import org.iucn.sis.shared.api.models.TaxonLevel;
import org.iucn.sis.shared.api.models.User;
import org.iucn.sis.shared.conversions.LibraryGenerator;
import org.iucn.sis.shared.helpers.AuthorizableObject;
import org.junit.Test;

import com.solertium.db.DBSessionFactory;

import core.BasicHibernateTest;

public class TestDEMImport extends BasicHibernateTest {
	
	@Test
	public void testSearch() {
		Session session = openSession();
		
		TaxonCriteria criteria = new TaxonCriteria(session);
		criteria.createTaxonLevelCriteria().level.eq(TaxonLevel.KINGDOM);
		
		TaxonIO io = new TaxonIO(session);
		Taxon[] results = io.search(criteria);
		
		for (Taxon taxon : results)
			Debug.println(taxon.getFriendlyName());
		
		TaxonHierarchy hierarchy = new TaxonHierarchy();
		hierarchy.setChildren(Arrays.asList(results));
		
		Debug.println(hierarchy.toXML());
		
		Assert.assertEquals(1, results.length);
	}
	
	@Test
	public void run() throws Exception {
		start();
	}
	
	@Test
	public void scratch() throws Exception {
		SISPersistentManager manager = SISPersistentManager.newInstance("sis", SIS.get().getSettings(null), true);
		
		LibraryGenerator generator = new LibraryGenerator();
		generator.setData(SIS.get().getSettings(null).getProperty("org.iucn.sis.server.vfs", "/var/sis/new_vfs"));
		generator.setSession(manager.openSession());
		generator.start();
		
		SISDBAuthenticator auth = new SISDBAuthenticator(SIS.get().getExecutionContext());
		
		Permission defaultPerm = new Permission(PermissionGroup.DEFAULT_PERMISSION_URI, true, true, true, true, true, true);
		defaultPerm.setType(AuthorizableObject.RESOURCE_TYPE_PATH);
		PermissionGroup perm = new PermissionGroup("admin");
		perm.setScopeURI("");
		defaultPerm.setPermissionGroup(perm);
		perm.getPermissions().add(defaultPerm);
		
		User user = new User();
		user.setAffiliation("SIS");
		user.setEmail("gogoego.tests@gmail.com");
		user.setFirstName("The");
		user.setLastName("Administrator");
		user.setOfflineStatus(false);
		user.setPassword(auth.translatePassword("admin", "changeme"));
		user.setSisUser(true);
		user.setUsername("admin");
		
		Session session = manager.openSession();
		session.beginTransaction();
		
		session.save(perm);
		session.save(user);
		
		perm.getUsers().add(user);
		user.getPermissionGroups().add(perm);
		
		session.update(user);
		session.update(perm);
		
		session.getTransaction().commit();
		session.close();
		manager.shutdown();
		start();
		
		
	}
	
	private void start() throws NamingException {
		DBSessionFactory.registerDataSource("dem", "jdbc:access:////Users/carlscott/Projects/SIS/dem/demfish.mdb", "com.hxtt.sql.access.AccessDriver", "", "");
		
		DEMImport importer = new DEMImport("admin", "dem", true, openSession());
		importer.run();
	}

}
