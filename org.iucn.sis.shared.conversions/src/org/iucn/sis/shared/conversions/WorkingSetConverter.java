package org.iucn.sis.shared.conversions;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.gogoego.api.plugins.GoGoEgo;
import org.gogoego.api.utils.DocumentUtils;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.utils.FormattedDate;
import org.iucn.sis.server.api.utils.ServerPaths;
import org.iucn.sis.shared.api.models.AssessmentType;
import org.iucn.sis.shared.api.models.Relationship;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.User;
import org.iucn.sis.shared.api.models.WorkingSet;
import org.iucn.sis.shared.helpers.WorkingSetData;
import org.iucn.sis.shared.helpers.WorkingSetParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;

public class WorkingSetConverter extends GenericConverter<VFSInfo> {
	
	@Override
	protected void run() throws Exception {
		convertAllWorkingSets(data.getOldVFS());
	}
	
	public void convertAllWorkingSets(VFS oldVFS) throws Exception {
		List<File> allFiles = FileListing.main(data.getOldVFSPath() + "/HEAD/workingsets");
		Map<String, HashSet<String>> oldWSIDToUserNames = parseSubscribedDocs(oldVFS);
		//long converted = 0;

		for (File file : allFiles) {
			try {
				if (file.getPath().endsWith(".xml")) {
					NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
					ndoc.parse(FileListing.readFileAsString(file));
					WorkingSetData data = new WorkingSetParser().parseSingleWorkingSet(ndoc.getDocumentElement());
					WorkingSet set = convertWorkingSetData(data, oldWSIDToUserNames.get(data.getId()));
					if (set != null) {
						SIS.get().getManager().getSession().save(set);
						//converted++;
					} else {
						print("The set " + file.getPath() + " is null");
					}
/*
					if( converted % 10 == 0 ) {
						SIS.get().getManager().getSession().getTransaction().commit();
						SIS.get().getManager().getSession().beginTransaction();
					}*/
					
				}
			} catch (Exception e) {
				print("Failed on file " + file.getPath());
				throw e;
			}
		}
		
		/*if( converted % 10 == 0 ) {
			SIS.get().getManager().getSession().getTransaction().commit();
			SIS.get().getManager().getSession().beginTransaction();
		}*/
	}
	
	private Map<String, Document> getSubscribedDocs(VFS vfs) throws NotFoundException {
		Map<String, Document> userToDoc = new HashMap<String, Document>();
		String[] usersDir = vfs.list(ServerPaths.getUserRootPath());
		for (String userDir : usersDir) {
			if (vfs.isCollection(ServerPaths.getUserRootPath() + "/" + userDir)) {
				Document doc = DocumentUtils.impl.getVFSFile(ServerPaths.getUserRootPath() + "/" + userDir + "/workingSet.xml", vfs);
				if (doc != null) {
					userToDoc.put(userDir, doc);
				}
			}
		}
		return userToDoc;		
	}
	
	private Map<String, HashSet<String>> parseSubscribedDocs(VFS vfs) throws NotFoundException {
	
		Map<String, HashSet<String>> oldWSIDToUserNames = new HashMap<String, HashSet<String>>();		
		Map<String, Document> userToDoc = getSubscribedDocs(vfs);
		for (Entry<String, Document> entry : userToDoc.entrySet()) {
			String username = entry.getKey();
			Document doc = entry.getValue();
			NodeList workingSets = ((Element)doc.getDocumentElement().getElementsByTagName("public").item(0)).getElementsByTagName("workingSet");
			for (int i = 0; i < workingSets.getLength(); i++) {
				String workingSetID = ((Element)workingSets.item(i)).getAttribute("id");
				if (!oldWSIDToUserNames.containsKey(workingSetID)) {
					oldWSIDToUserNames.put(workingSetID, new HashSet<String>());
				} 
				oldWSIDToUserNames.get(workingSetID).add(username);
				print("added " + workingSetID + " with user " + username);
			}		
		}
		
		return oldWSIDToUserNames;		
	}
	
	public WorkingSet convertWorkingSetData(WorkingSetData data, Set<String> subscribedUsernames) throws Exception {
		
		WorkingSet ws = new WorkingSet();
		ws.setDescription(data.getDescription());
		ws.setName(data.getWorkingSetName());
		ws.setCreatedDate(FormattedDate.impl.getDate(data.getDate()));
		ws.setIsMostRecentPublished(new Boolean(false));
		ws.setNotes(data.getNotes());
		//ADD ASSESSMENT TYPE
		ws.setAssessmentTypes(new HashSet<AssessmentType>());
		if (data.getFilter().isRecentPublished()) {
			AssessmentType type = (AssessmentType)SIS.get().getManager().getSession().merge(AssessmentType.getAssessmentType(AssessmentType.PUBLISHED_ASSESSMENT_STATUS_ID));
			ws.getAssessmentTypes().add(type);
			ws.setIsMostRecentPublished(new Boolean(true));
		} else if (data.getFilter().isAllPublished()) {
			AssessmentType type = (AssessmentType)SIS.get().getManager().getSession().merge(AssessmentType.getAssessmentType(AssessmentType.PUBLISHED_ASSESSMENT_STATUS_ID));
			ws.getAssessmentTypes().add(type);
			ws.setIsMostRecentPublished(new Boolean(false));
		} if (data.getFilter().isDraft()) {
			AssessmentType type = (AssessmentType)SIS.get().getManager().getSession().merge(AssessmentType.getAssessmentType(AssessmentType.DRAFT_ASSESSMENT_STATUS_ID));
			ws.getAssessmentTypes().add(type);
		}
		
		//ADD REGIONS & RELATIONSHIP
		if (data.getFilter().isAllRegions()) {			
			ws.setRelationship(SIS.get().getRelationshipIO().getRelationshipByName(Relationship.ALL));
			ws.getRegion().clear();			
		} else {
			for (String regionID : data.getFilter().getRegions()) {
				ws.getRegion().add(RegionConverter.getNewRegion(Integer.valueOf(regionID)));
			}
			Relationship rel = SIS.get().getRelationshipIO().getRelationshipByName(data.getFilter().getRegionType());
			ws.setRelationship(rel);
		}
		
		

		//ADD USERS
		User creator = SIS.get().getUserIO().getUserFromUsername(data.getCreator());
		if( creator == null ) {
			print("Couldn't find user " + data.getCreator());
			return null;
		}
		ws.setCreator(creator);
		creator.getOwnedWorkingSets().add(ws);
		if (subscribedUsernames != null)
		for (String username : subscribedUsernames) {
			User user = SIS.get().getUserIO().getUserFromUsername(username);
			if (user != null)
				ws.getUsers().add(user);
//			user.getSubscribedWorkingSets().add(ws);
		}
		
		//WORKFLOW
		ws.setWorkflow("draft");
		
		
		//ADD TAXON
		for (String taxonID : data.getSpeciesIDs()) {
			try{
			Taxon taxon = SIS.get().getTaxonIO().getTaxon(Integer.valueOf(taxonID));
			if( taxon != null ) {
					taxon.setWorking_set(new HashSet<WorkingSet>());
					taxon.getWorking_set().add(ws);
					ws.getTaxon().add(taxon);
				}
			} catch (Exception e) {
				print("failed while trying taxonID " + taxonID);
				throw e;
			}
		}

		
//		System.out.println("trying to save:\n" + ws.toXML());
		
		
		return ws;
		
	}

}
