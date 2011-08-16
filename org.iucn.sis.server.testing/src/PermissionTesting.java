import java.util.HashSet;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.acl.feature.AuthorizableFeature;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentFilter;
import org.iucn.sis.shared.api.models.AssessmentType;
import org.iucn.sis.shared.api.models.Permission;
import org.iucn.sis.shared.api.models.PermissionGroup;
import org.iucn.sis.shared.api.models.PermissionResourceAttribute;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.WorkingSet;


public class PermissionTesting extends QuickTest {
	
	public static void main(String[] args) {
		PermissionTesting t = new PermissionTesting();
		
		Debug.setInstance(t);
		
		final String UT = "org.iucn.sis.server.schema.usetrade";
		
		Debug.println("Should match RW");
		t.test(UT, UT);
		Debug.println("Should match RW");
		t.test(".*", UT);
		Debug.println("Should NOT match RW");
		t.test(UT, "");
		t.test(UT, "org.iucn.sis.server.schema.redlist");
		
		t.testCanUseFeature();
		t.testAccessToWorkingSet();
	}
	
	public void test(String permissionSchema, String assessmentSchema) {
		Permission d = new Permission();
		d.setUrl("default");
		d.setRead(true);
		d.setWrite(true);
		d.setDelete(false);
		
		Permission p = new Permission();
		
		PermissionResourceAttribute attribute = new PermissionResourceAttribute("schema", permissionSchema);
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
		
		System.out.println("-- Start --");
		
		System.out.println("Checking assessment with permission URL " + assessment.getFullURI());
		
		System.out.println("Read Assessment? " + SharedPermissionUtils.checkMe(group, assessment, AuthorizableObject.READ));
		System.out.println("----");
		System.out.println("Write Assessment? " + SharedPermissionUtils.checkMe(group, assessment, AuthorizableObject.WRITE));
		System.out.println("----");
		System.out.println("Delete Assessment? " + SharedPermissionUtils.checkMe(group, assessment, AuthorizableObject.DELETE));
		
		System.out.println("-- Done --");
	}
	
	public void testCanUseFeature(){
		Permission d = new Permission();
		d.setUrl("default");
		d.setRead(true);
		d.setWrite(true);
		d.setDelete(false);
		d.setUse(true);
		
		PermissionGroup group = new PermissionGroup();
		group.addPermission(d);
		group.setScopeURI("");
		
		System.out.println("Authorized to use Feature? " + SharedPermissionUtils.checkMe(group, AuthorizableFeature.ADD_PROFILE_FEATURE, AuthorizableObject.USE_FEATURE));
		
	}
	
	public void testAccessToTaxonomicGroup(){
		/*
		 * Check deny the cccess to taxonomic group
		 * 
		 */
	}
	
	public void testAccessToWorkingSet(){
		Permission p = new Permission();
		p.setRead(true);
		p.setWrite(true);
		p.setDelete(false);
		
		PermissionGroup group = new PermissionGroup();
		group.addPermission(p);
		group.setScopeURI("");
		
		WorkingSet ws = new WorkingSet();	
		ws.setName("Test Name");
		ws.setDescription("Test Description");
		
		System.out.println("-- Start --");
		System.out.println("Checking Working Set " + ws.getDescription());
		
		System.out.println("Read Working Set? " + SharedPermissionUtils.checkMe(group, ws, AuthorizableObject.READ));
		System.out.println("----");
		System.out.println("Write Working Set? " + SharedPermissionUtils.checkMe(group, ws, AuthorizableObject.WRITE));
		System.out.println("----");
		System.out.println("Delete Working Set? " + SharedPermissionUtils.checkMe(group, ws, AuthorizableObject.DELETE));
		
		System.out.println("-- Done --");
	}

}
