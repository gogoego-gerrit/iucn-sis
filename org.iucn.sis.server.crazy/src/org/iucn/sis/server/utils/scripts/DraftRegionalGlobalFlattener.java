package org.iucn.sis.server.utils.scripts;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.server.io.AssessmentIO;
import org.iucn.sis.server.utils.DocumentUtils;
import org.iucn.sis.server.utils.ServerPaths;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.AssessmentParser;
import org.iucn.sis.shared.data.assessments.CanonicalNames;

import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.vfs.ConflictException;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;
import com.solertium.vfs.utils.VFSUtils;
import com.solertium.vfs.utils.VFSUtils.VFSPathParseException;

public class DraftRegionalGlobalFlattener {

	private static AssessmentParser parser;
	private static long counter = 0;
	
	public static void flattenAllAssessments(VFS vfs) throws NotFoundException, ConflictException, VFSPathParseException {
		final VFSPath assessPath = VFSUtils.parseVFSPath(ServerPaths.getPublishedAssessmentURL());
		recursePublished(vfs, assessPath);
	}
	
	private static void recursePublished(VFS vfs, VFSPath curPath) throws NotFoundException, ConflictException {
		if( vfs.isCollection(curPath)) {
			VFSPathToken [] curTokens = vfs.list(curPath); 
			for( VFSPathToken cur : curTokens ) {
				if( cur.toString().matches(".*\\d+.*") )
					recursePublished(vfs, curPath.child(cur));
			}
		} else if( curPath.toString().endsWith(".xml")) {
			processPublishedAssessment(vfs, curPath);
		} 
	}
	
	private static void processPublishedAssessment(VFS vfs, VFSPath curPath) {
		String xml = DocumentUtils.getVFSFileAsString(curPath.toString(), vfs);
		NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
		ndoc.parse(xml);
		AssessmentData assessment = new AssessmentParser(ndoc).getAssessment();
		
		if( updateRegionInfo(assessment) )
			AssessmentIO.writeAssessment(assessment, assessment.getUserLastUpdated(), vfs, false);
	}

	private static boolean updateRegionInfo(AssessmentData assessment) {
		counter++;
		if( counter % 25 == 0 ) {
			try {
				Thread.sleep(2500);
			} catch (InterruptedException e) {}
		}
		
		List<String> regionInfo = (List<String>)assessment.getDataMap().get(CanonicalNames.RegionInformation);
		if( regionInfo == null ) {
			regionInfo = new ArrayList<String>();
			regionInfo.add("-1");
			regionInfo.add("true");
			
			assessment.getDataMap().put(CanonicalNames.RegionInformation, regionInfo);
			
			return true;
		} else if( regionInfo.size() == 3 ) {
			if( regionInfo.get(0).equals("false") )
				regionInfo.set(1, "-1");
			regionInfo.remove(0);
			
			return true;
		} else
			return false;
	}
	
	public static void flattenDrafts(VFS vfs) throws NotFoundException, ConflictException, VFSPathParseException {
		parser = new AssessmentParser();
		
		final VFSPath draftRegURI = VFSUtils.parseVFSPath("/drafts/regional");
		recurse(vfs, draftRegURI, 0);
		
		final VFSPath draftURI = VFSUtils.parseVFSPath("/drafts");
		recurse(vfs, draftURI, 1);
	}

	private static void recurse(VFS vfs, VFSPath curPath, int pass) throws NotFoundException, ConflictException {
		if( vfs.isCollection(curPath)) {
			if( pass == 1 && (curPath.toString().endsWith("regional") || curPath.toString().endsWith("_")) ) {
				System.out.println("Skipping path " + curPath);
			} else {
				VFSPathToken [] curTokens = vfs.list(curPath); 
				for( VFSPathToken cur : curTokens ) {
					if( cur.toString().matches(".*\\d+.*") )
						recurse(vfs, curPath.child(cur), pass);
					else
						System.out.println("Eliding non-digit path " + curPath.child(cur));
				}
			}

			if( vfs.list(curPath).length == 0 )
				vfs.delete(curPath);
			else
				System.out.println("COULD NOT REMOVE PATH " + curPath);
		} else if( curPath.toString().endsWith(".xml")) {
			processAssessmentPath(vfs, curPath);
		} else if( curPath.toString().contains("CONFLICT") ) {
			vfs.delete(curPath);
		}
	}

	private static void processAssessmentPath(VFS vfs, VFSPath curPath) throws NotFoundException {
		try {
			String assessStr = DocumentUtils.getVFSFileAsString(curPath.toString(), vfs);
			NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
			ndoc.parse(assessStr);
			parser.parse(ndoc);

			AssessmentData curAssess = parser.getAssessment();
			
			VFSPath rootURL = VFSUtils.parseVFSPath(ServerPaths.getDraftAssessmentRootURL(curAssess.getSpeciesID()));
			if( !vfs.exists(rootURL) )
				vfs.makeCollections(rootURL);
			
			if( !curAssess.getAssessmentID().contains("_") ) {
				curAssess.setAssessmentID(getNextDraftID(vfs, curAssess.getSpeciesID()));
			}
			
			updateRegionInfo(curAssess);

			if( AssessmentIO.writeAssessment(curAssess, curAssess.getUserLastUpdated(), vfs, false).status.isSuccess())
				vfs.delete(curPath);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("ERROR PROCESSING ASSESSMENT AT PATH " + curPath);
		}
	}
	
	private static String getNextDraftID(VFS vfs, String speciesID) {
		int nextID = 0;
		VFSPath rootURI;

		try {
			rootURI = VFSUtils.parseVFSPath(ServerPaths.getDraftAssessmentRootURL(speciesID));
		} catch (VFSPathParseException e) {
			e.printStackTrace();
			return null;
		}

		if (vfs.exists(rootURI)) { // If there's already regionals for this guy,
			// get the next available.
			while ( vfs.exists(rootURI.child(new VFSPathToken(speciesID + "_" + nextID))) ||
					vfs.exists(rootURI.child(new VFSPathToken(speciesID + "_offline" + nextID)))) {
				nextID++;
			}
		}

		String assessmentID = speciesID + "_" + nextID;
		return assessmentID;
	}
}
