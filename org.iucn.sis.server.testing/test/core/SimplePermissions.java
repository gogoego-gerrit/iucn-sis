package core;

import junit.framework.Assert;

import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.acl.feature.AuthorizableFeature;
import org.iucn.sis.shared.api.models.PermissionGroup;
import org.iucn.sis.shared.api.models.User;
import org.junit.Test;

import com.solertium.lwxml.java.JavaNativeDocument;
import com.solertium.lwxml.shared.NativeDocument;

public class SimplePermissions extends BasicTest {
	
	@Test
	public void canNotCreateProfiles() {
		boolean canUse = false;
		User user = getBadUser();
		for (PermissionGroup group : user.getPermissionGroups())
			canUse |= SharedPermissionUtils.checkMe(group, AuthorizableFeature.ADD_PROFILE_FEATURE, AuthorizableObject.USE_FEATURE);
		
		Assert.assertFalse(canUse);
	}
	
	@Test
	public void canCreateProfiles() {
		boolean canUse = false;
		User user = getGoodUser();
		for (PermissionGroup group : user.getPermissionGroups())
			canUse |= SharedPermissionUtils.checkMe(group, AuthorizableFeature.ADD_PROFILE_FEATURE, AuthorizableObject.USE_FEATURE);
		
		Assert.assertTrue(canUse);
	}
	
	public User getBadUser() {
		NativeDocument document = new JavaNativeDocument();
		document.parse("<user><id>16291896</id><state>0</state>" +
			"<username><![CDATA[j@j.com]]></username><firstName><![CDATA[James]]></firstName>" +
			"<lastName><![CDATA[Spader]]></lastName><nickname><![CDATA[James]]></nickname>" +
			"<initials><![CDATA[JDS]]></initials><affiliation><![CDATA[IUCN]]></affiliation>" +
			"<sisUser><![CDATA[true]]></sisUser><rapidListUser><![CDATA[false]]></rapidListUser>" +
			"<email><![CDATA[j@j.com]]></email><permGroup><id>16291897</id><scopeURI/>" +
			"<name><![CDATA[CREATEPROFILEONLY]]></name><perm><id>16291898</id><url>" +
			"<![CDATA[default]]></url><type/><read>false</read><write>false</write>" +
			"<create>false</create><delete>false</delete><grant>false</grant><use>false</use></perm>" +
			"<perm><id>16291899</id><url><![CDATA[feature/addProfile]]></url><type/><read>false</read>" +
			"<write>false</write><create>false</create><delete>false</delete><grant>false</grant>" +
			"<use>false</use></perm><parent id=\"812\"><![CDATA[assessor]]></parent></permGroup></user>");
		
		return User.fromXML(document.getDocumentElement());
	}
	

	public User getGoodUser() {
		NativeDocument document = new JavaNativeDocument();
		document.parse("<user><id>16291896</id><state>0</state>" +
			"<username><![CDATA[j@j.com]]></username><firstName><![CDATA[James]]></firstName>" +
			"<lastName><![CDATA[Spader]]></lastName><nickname><![CDATA[James]]></nickname>" +
			"<initials><![CDATA[JDS]]></initials><affiliation><![CDATA[IUCN]]></affiliation>" +
			"<sisUser><![CDATA[true]]></sisUser><rapidListUser><![CDATA[false]]></rapidListUser>" +
			"<email><![CDATA[j@j.com]]></email><permGroup><id>16291897</id><scopeURI/>" +
			"<name><![CDATA[CREATEPROFILEONLY]]></name><perm><id>16291898</id><url>" +
			"<![CDATA[default]]></url><type/><read>false</read><write>false</write>" +
			"<create>false</create><delete>false</delete><grant>false</grant><use>false</use></perm>" +
			"<perm><id>16291899</id><url><![CDATA[feature/addProfile]]></url><type/><read>false</read>" +
			"<write>false</write><create>false</create><delete>false</delete><grant>false</grant>" +
			"<use>true</use></perm><parent id=\"812\"><![CDATA[assessor]]></parent></permGroup></user>");
		
		return User.fromXML(document.getDocumentElement());
	}

}
