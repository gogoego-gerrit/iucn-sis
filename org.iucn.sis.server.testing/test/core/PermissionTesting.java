package core;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import junit.framework.Assert;

import org.hibernate.Session;
import org.iucn.sis.server.api.io.TaxonIO;
import org.iucn.sis.server.api.io.WorkingSetIO;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.acl.BasePermissionUtils;
import org.iucn.sis.shared.api.acl.PermissionDataSource;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.acl.feature.AuthorizableFeature;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentType;
import org.iucn.sis.shared.api.models.Permission;
import org.iucn.sis.shared.api.models.PermissionGroup;
import org.iucn.sis.shared.api.models.PermissionResourceAttribute;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.WorkingSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class PermissionTesting extends BasicHibernateTest {
	
	private BasePermissionUtils Oracle;
	private TestingPermissionDataSource source;
	
	@Before
	public void setup() {
		Oracle = new BasePermissionUtils(source = new TestingPermissionDataSource());
	}
	
	/**
	 * When creating permissions for assessments, you can choose to restrict 
	 * access to a particular schema.  The test below check to ensure that 
	 * these restrictions are enforced.
	 */
	@Test
	public void testSchemaRestrictions() {
		final String UT = "org.iucn.sis.server.schema.usetrade";
		
		checkSchemaRestriction(UT, UT, true, true, false);
		checkSchemaRestriction(".*", UT, true, true, false);
		
		checkSchemaRestriction(UT, "", false, false, false);
		checkSchemaRestriction(UT, "org.iucn.sis.server.schema.redlist", false, false, false);
		
	}
	
	/**
	 * Check to see if the schema restriction is being enforced 
	 * properly. 
	 * @param permissionSchema the schema for the permission
	 * @param assessmentSchema the schema for the assessment in question
	 * @param read expected result for attempt to read
	 * @param write expected result for attempt to write
	 * @param delete expected result for attempt to delete
	 */
	private void checkSchemaRestriction(String permissionSchema, String assessmentSchema, boolean read, boolean write, boolean delete) {
		Permission d = new Permission();
		d.setUrl("default");
		d.setRead(true);
		d.setWrite(true);
		d.setDelete(false);
		
		Permission p = new Permission();
		
		PermissionResourceAttribute attribute = 
			new PermissionResourceAttribute("schema", permissionSchema);
		attribute.setPermission(p);
		
		p.setRead(true);
		p.setWrite(true);
		p.setDelete(false);
		p.setAttributes(new HashSet<PermissionResourceAttribute>());
		p.getAttributes().add(attribute);
		p.setUrl("resource/assessment/draft_status");
		
		PermissionGroup group = new PermissionGroup();
		group.addPermission(p);
		group.addPermission(d);
		group.setScopeURI("");
		
		Taxon node = new Taxon();
				
		Assessment assessment = new Assessment();
		assessment.setSchema(assessmentSchema);
		assessment.setAssessmentType(AssessmentType.getAssessmentType(AssessmentType.DRAFT_ASSESSMENT_TYPE));
		assessment.setTaxon(node);
		
		Assert.assertEquals(read, Oracle.hasPermission(group, assessment, AuthorizableObject.READ));
		Assert.assertEquals(write, Oracle.hasPermission(group, assessment, AuthorizableObject.WRITE));
		Assert.assertEquals(delete, Oracle.hasPermission(group, assessment, AuthorizableObject.DELETE));
	}
	
	@Test
	public void testCanUseFeature() {
		Permission d = new Permission();
		d.setUrl("default");
		d.setRead(true);
		d.setWrite(true);
		d.setDelete(false);
		d.setUse(true);
		
		PermissionGroup group = new PermissionGroup();
		group.addPermission(d);
		group.setScopeURI("");
		
		Assert.assertTrue(Oracle.hasPermission(group, AuthorizableFeature.ADD_PROFILE_FEATURE, AuthorizableObject.USE_FEATURE));		
	}
	
	@Test
	public void testAccessToTaxonomicGroup() {
		/*
		 * Check deny the cccess to taxonomic group
		 * 
		 */
	}
	
	@Test
	public void testAccessToWorkingSet() {
		Permission p = new Permission();
		p.setUrl("default");
		p.setRead(true);
		p.setWrite(true);
		p.setDelete(false);
		
		PermissionGroup group = new PermissionGroup();
		group.addPermission(p);
		group.setScopeURI("");
		
		WorkingSet ws = new WorkingSet();	
		ws.setName("Test Name");
		ws.setDescription("Test Description");
		
		Assert.assertTrue(Oracle.hasPermission(group, ws, AuthorizableObject.READ));
		Assert.assertTrue(Oracle.hasPermission(group, ws, AuthorizableObject.WRITE));
		Assert.assertFalse(Oracle.hasPermission(group, ws, AuthorizableObject.DELETE));
		
		System.out.println("-- Done --");
	}
	
	
	@After
	public void tearDown() {
		source.close();
		Oracle = null;
		source = null;
	}
	
	/**
	 * The real implementation of this should scope queries down
	 * for working sets to only those that the users owns or is 
	 * subscribed to.
	 * 
	 * The test one automatically uses "admin".
	 *  
	 * @author carl.scott@solertium.com
	 *
	 */
	private static class TestingPermissionDataSource implements PermissionDataSource {
		
		private final WorkingSetIO workingSetIO;
		private final TaxonIO taxonIO;
		
		private final Session session;
		
		public TestingPermissionDataSource() {
			this.session = SISPersistentManager.instance().openSession();
			this.workingSetIO = new WorkingSetIO(session);
			this.taxonIO = new TaxonIO(session);
		}
		
		@Override
		@SuppressWarnings("deprecation")
		public List<WorkingSet> getAllWorkingSets() {
			try {
				return Arrays.asList(workingSetIO.getSubscribedWorkingSets("admin"));
			} catch (PersistentException e) {
				return new ArrayList<WorkingSet>();
			}
		}
		
		@Override
		public WorkingSet getWorkingSet(int id) {
			for (WorkingSet set : getAllWorkingSets())
				if (set.getId() == id)
					return set;
			return null;
		}
		
		@Override
		public Taxon getTaxon(int id) {
			return taxonIO.getTaxon(id);
		}
		
		public void close() {
			session.close();
		}
		
	}

}
