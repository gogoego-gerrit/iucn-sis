package org.iucn.sis.server.io;

import java.io.Writer;
import java.lang.reflect.InvocationTargetException;

import org.iucn.sis.server.utils.DocumentUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.solertium.vfs.ConflictException;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;

public class ProfileIO {

	public static Document getProfileAsDocument(VFS vfs, String user) {
		final VFSPath uri = new VFSPath("/users/" + user + "/profile.xml");

		if (vfs.exists(uri)) {
			try {
				Document doc = doGetProfileAsDoc(vfs, uri);
				return doc;
			} catch (final Exception e) {
				System.out.println("ERROR in ProfileRestlet.fetchUserProfile()");
				e.printStackTrace();
				return null;
			}
		} else
			return null;
	}
	
	public static void updateUserProfile(final VFS vfs, final String user, final String payload) throws Exception {
		VFSPath uri = new VFSPath("/users/" + user + "/profile.xml");

		Writer writer = vfs.getWriter(uri);
		writer.write(payload);
		writer.close();
	}
	
	public static void putProfile(VFS vfs, String user) throws Exception {
		VFSPath rootURI = new VFSPath("/users/" + user);

		if (!vfs.exists(rootURI))
			vfs.copy(new VFSPath("/users/_userTemplates"), rootURI);
		else
			throw new ConflictException();
	}

	public static boolean deleteProfile(VFS vfs, String user) throws NotFoundException {
		final VFSPath uri = new VFSPath("/users/" + user);
		final VFSPath trashURI = new VFSPath("/trashed_users");

		try {
			if( !vfs.exists(trashURI) )
				vfs.makeCollection(trashURI);
		} catch (ConflictException e) {
			e.printStackTrace();
		} catch (NotFoundException e) {
			e.printStackTrace();
		}

		if (vfs.exists(uri)) {
			try {
				VFSPath trashChild = trashURI.child(new VFSPathToken(user));
				if( vfs.exists(trashChild) )
					vfs.delete(trashChild);
				
				vfs.move(uri, trashURI.child(new VFSPathToken(user)));
				return true;
			} catch (final Exception e) {
				System.out.println("ERROR in ProfileRestlet.deleteUserProfile()");
				e.printStackTrace();
				return false;
			}
		} else
			throw new NotFoundException();
	}
	
	private static Document doGetProfileAsDoc(VFS vfs, final VFSPath uri) {
		Document doc = DocumentUtils.getVFSFileAsDocument(uri.toString(), vfs);
		String curGroup = "";
		Element quickGroupNode = null;

		NodeList quickGroup = doc.getElementsByTagName("quickGroup");
		if (quickGroup.getLength() > 0) {
			quickGroupNode = (Element)quickGroup.item(0);
			curGroup = quickGroupNode.getTextContent();
		}

		// IF I'M OFFLINE, GIVE EVERYONE OFFLINE PERMISSION SCOPE. HUZZAH.
		try {
			if (!iAmOnline())
				if (curGroup.equalsIgnoreCase("'guest'") && quickGroupNode != null)
					quickGroupNode.setTextContent("offline");
		} catch (Exception ignored) {
			// Nothing to do. No JAR was found.
		}

		if( quickGroupNode != null ) {
			String newGroup = determineNewGroup(curGroup);
			if( !newGroup.equals("") ) {
				System.out.println("Converted old permission group from " + curGroup + " to " + newGroup);
				quickGroupNode.setTextContent(newGroup);
			}
		}

		return doc;
	}
	
	private static Boolean iAmOnline() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
	InvocationTargetException {
		Class clazz = Class.forName("com.solertium.util.VerifyOnline");

		java.lang.reflect.Method amIOnline = clazz.getMethod("amIOnline", (Class[]) null);
		Boolean ret = (Boolean) amIOnline.invoke((Object) null, (Object[]) null);
		return ret;
	}
	
	private static String determineNewGroup(String curGroup) {
		String newGroup = "";
		if( curGroup == null )
			newGroup = "sysAdmin";
		else if( curGroup.equals("'guest'") )
			newGroup = "guest";
		else if( curGroup.equals("'workingSet','no_taxomatic'") )
			newGroup = "workingSetAssessor";
		else if( curGroup.equals("'workingSet','no_taxomatic','can_batch','canCreateDraft','reference_replacer'") )
			newGroup = "workingSetFacilitator,batchChangeUser,referenceReplaceUser";
		else if( curGroup.equals("'molluscs','no_taxomatic'") )
			newGroup = "molluscsAssessor";
		else if( curGroup.contains("'bryophyta','no_taxomatic'") )
			newGroup = "bryophytaFacilitator";
		else if( curGroup.equals("'cephalopods','workingSet','no_taxomatic'") )
			newGroup = "cephalopodAssessor,workingSetAssessor";
		else if( curGroup.equals("'Lepidoptera','workingSet','no_taxomatic'") )
			newGroup = "lepidopteraAssessor,workingSetAssessor";
		else if( curGroup.equals("'Lepidoptera','no_taxomatic'") )
			newGroup = "lepidopteraAssessor";
		else if( curGroup.equals("'gaa','gma'") )
			newGroup = "gaaAssessor,gmaAssessor,taxomaticUser";
		else if( curGroup.equals("'gaa','gma','canEditPublished'") )
			newGroup = "gaaAdmin,gmaAdmin,taxomaticUser";
		else if( curGroup.equals("'gaa','gma','canEditPublished','no_taxomatic'") )
			newGroup = "gaaAdmin,gmaAdmin";
		else if( curGroup.contains("'gaa','canCreateDraft','canEditPublished'") )
			newGroup = "gaaAdmin,gmaAdmin";
		else if( curGroup.equals("'rlu','workingSet'"))
			newGroup = "rlu";
		else if( curGroup.equals("'gma','canEditPublished','workingSet'") )
			newGroup = "gmaAdmin,workingSetAdmin";
		else if( curGroup.contains("'workingSet'"))
			newGroup = "workingSetAssessor";
		else if( curGroup.startsWith("'gaa','reptiles','canEditPublished','canCreateDraft','canDeleteDraft'"))
			newGroup = "gaaAdmin,reptilesAdmin";
		else if( curGroup.equals("'reptiles'"))
			newGroup = "reptilesAssessor";
		else if( curGroup.equals("'Cambaridae','workingSet','can_batch','canCreateDraft'"))
			newGroup = "cambaridaeFacilitator,workingSetFacilitator,batchChangeUser";
		else if( curGroup.equals("'Cambaridae','workingSet','canCreateDraft'"))
			newGroup = "cambaridaeFacilitator,workingSetFacilitator";

		if( curGroup.contains("can_batch") )
			newGroup += ",batchChangeUser";
		if( curGroup.contains("reference_replacer") )
			newGroup += ",referenceReplaceUser";
		if( curGroup.contains("find_replace") )
			newGroup += ",findReplaceUser";
		if( curGroup.contains("no_taxomatic") )
			newGroup += ",redactTaxomatic";

		return newGroup;
	}
}
