package org.iucn.sis.server.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.naming.NamingException;

import org.iucn.sis.server.crossport.export.DBMirrorManager;
import org.iucn.sis.server.locking.FileLocker;
import org.iucn.sis.server.locking.LockType;
import org.iucn.sis.server.locking.LockRepository.Lock;
import org.iucn.sis.server.ref.ReferenceApplication;
import org.iucn.sis.server.ref.ReferenceCrawler;
import org.iucn.sis.server.simple.SISContainerApp;
import org.iucn.sis.server.utils.DocumentUtils;
import org.iucn.sis.server.utils.ServerPaths;
import org.iucn.sis.server.utils.ServerPathsv2;
import org.iucn.sis.shared.BaseAssessment;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.AssessmentParser;
import org.iucn.sis.shared.io.AssessmentIOMessage;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;
import org.restlet.data.Status;

import com.solertium.db.SystemExecutionContext;
import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.vfs.ConflictException;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;
import com.solertium.vfs.utils.VFSUtils;
import com.solertium.vfs.utils.VFSUtils.VFSPathParseException;

/**
 * Performs file system IO operations for Assessments.
 * 
 * @author adam.schwartz
 *
 */
public class AssessmentIO {

	/**
	 * Contains information about a write that occurs via this helper class. If
	 * a single assessment is written back, the status and last modified fields
	 * will be set to reflect the lock status of the assessment, and if the
	 * writeback was successful, the new last modified date to be sent back to
	 * the client. The result String may be populated with the assessment's ID. <br>
	 * <br>
	 * If a bulk save was performed, the result String will be populated with an
	 * XML document containing information on successful and failed writebacks.
	 * 
	 * @author adam
	 */
	public static class AssessmentIOWriteResult {
		public Status status;
		public String result;
		public long newLastModified;

		public AssessmentIOWriteResult(Status status, String result, long newLastModified) {
			this.status = status;
			this.result = result;
			this.newLastModified = newLastModified;
		}
	}

	/**
	 * Reads in regional draft assessments based on the taxonID. When we shuffle storage to store
	 * globals in with the regionals, this will read in all assessments.
	 * 
	 * @param vfs
	 * @param taxonID
	 * @return a list of Assessment objects. Will never be null, might be empty.
	 */
	public static List<AssessmentData> readAllDraftAssessments(VFS vfs, String taxonID) {
		List<AssessmentData> assessments = new ArrayList<AssessmentData>();

		VFSPath url = null;
		try {
			url = VFSUtils.parseVFSPath(ServerPaths.getDraftAssessmentRootURL(taxonID));
		} catch (VFSPathParseException e) {
			e.printStackTrace();
		}

		if( vfs.exists(url) ) {
			try {
				for( VFSPathToken curToken : vfs.list(url) ) {
					String xml = DocumentUtils.getVFSFileAsString(url.child(curToken).toString(), vfs);
					if (xml != null) {
						long lastMod = vfs.getLastModified(url.child(curToken));
						
						NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
						ndoc.parse(xml);
						AssessmentParser p = new AssessmentParser(ndoc);
						p.getAssessment().setDateModified(lastMod);
						assessments.add(p.getAssessment());
					}
				}
			} catch (NotFoundException e) {
				System.out.println("---- Could not find " + url.toString() + ". This is probably OK.");
			}
		}
		return assessments;
	}
	
	/**
	 * Reads in regional draft assessments based on the taxonID. When we shuffle storage to store
	 * globals in with the regionals, this will read in all assessments.
	 * 
	 * @param vfs
	 * @param taxonID
	 * @return a list of Assessment objects. Will never be null, might be empty.
	 */
	public static List<String> readAllDraftAssessmentsAsStrings(VFS vfs, String taxonID) {
		List<String> assessments = new ArrayList<String>();

		VFSPath url = null;
		try {
			url = VFSUtils.parseVFSPath((ServerPaths.getDraftAssessmentRootURL(taxonID)));
		} catch (VFSPathParseException e) {
			e.printStackTrace();
		}
		
		if( vfs.exists(url) ) {
			try {
				for( VFSPathToken curToken : vfs.list(url) ) {
					String xml = DocumentUtils.getVFSFileAsString(url.child(curToken).toString(), vfs);
					if (xml != null) {
						long lastMod = vfs.getLastModified(url.child(curToken));
						xml = xml.replaceAll("<\\?xml\\s*(version=.*)?\\s*(encoding=.*)?\\?>", "");
						xml = xml.replaceAll("(<dateModified>.*?</dateModified>)|(<dateModified\\s*/>)",
								"<dateModified>" + lastMod + "</dateModified>");
						assessments.add(xml);
					}
				}
			} catch (NotFoundException e) {
				System.out.println("---- Could not find " + url.toString() + ". This is probably OK.");
			}
		}
		return assessments;
	}
	
	/**
	 * This method returns a List of all draft assessments - global and regional - for
	 * one taxon. If no assessments exist, the list will be empty.
	 * 
	 * @param vfs
	 * @param taxonID
	 * @return List&lt;AssessmentData&gt;
	 */
	public static List<AssessmentData> readAllAssessmentsForTaxon(VFS vfs, String taxonID, String user) {
		List<AssessmentData> list = new ArrayList<AssessmentData>();
		list.addAll(readAllDraftAssessments(vfs, taxonID));
		
		TaxonNode taxon = TaxaIO.readNode(taxonID, vfs);
		if( taxon.getAssessments().size() > 0 )
			list.addAll(readList(vfs, taxon.getAssessments(), BaseAssessment.PUBLISHED_ASSESSMENT_STATUS, user, false));
		
		return list;
	}
	
	/**
	 * This method returns a List of all draft assessments - global and regional - for
	 * one taxon. If no assessments exist, the list will be empty.
	 * 
	 * @param vfs
	 * @param taxonID
	 * @return List&lt;AssessmentData&gt;
	 */
	public static List<String> readAllAssessmentsForTaxonAsStrings(VFS vfs, String taxonID, String user) {
		List<String> list = new ArrayList<String>();
		list.addAll(readAllDraftAssessmentsAsStrings(vfs, taxonID));
		
		TaxonNode taxon = TaxaIO.readNode(taxonID, vfs);
		if( taxon.getAssessments().size() > 0 )
			list.addAll(readListAsStrings(vfs, taxon.getAssessments(), BaseAssessment.PUBLISHED_ASSESSMENT_STATUS, user, false));
		
		return list;
	}
	
	public static List<AssessmentData> readPublishedAssessmentsForTaxon(VFS vfs, String taxonID) {
		TaxonNode taxon = TaxaIO.readNode(taxonID, vfs);
		return readPublishedAssessmentsForTaxon(vfs, taxon);
	}

	public static List<AssessmentData> readPublishedAssessmentsForTaxon(VFS vfs, TaxonNode taxon) {
		List<AssessmentData> list = new ArrayList<AssessmentData>();
		
		if( taxon.getAssessments().size() > 0 )
			list.addAll(readList(vfs, taxon.getAssessments(), BaseAssessment.PUBLISHED_ASSESSMENT_STATUS, null, false));
		
		return list;
	}
	
	public static List<String> readPublishedAssessmentsForTaxonAsStrings(VFS vfs, String taxonID) {
		TaxonNode taxon = TaxaIO.readNode(taxonID, vfs);
		return readPublishedAssessmentsForTaxonAsStrings(vfs, taxon);
	}

	public static List<String> readPublishedAssessmentsForTaxonAsStrings(VFS vfs, TaxonNode taxon) {
		List<String> list = new ArrayList<String>();
		
		if( taxon.getAssessments().size() > 0 )
			list.addAll(readListAsStrings(vfs, taxon.getAssessments(), BaseAssessment.PUBLISHED_ASSESSMENT_STATUS, null, false));
		
		return list;
	}
	
	public static AssessmentData readAssessment(VFS vfs, String id, String assessmentType, String user) {
		String uri = "";

		if (assessmentType.equals(BaseAssessment.DRAFT_ASSESSMENT_STATUS)) {
			uri = ServerPaths.getDraftAssessmentURL(id);
		} else if (assessmentType.equals(BaseAssessment.PUBLISHED_ASSESSMENT_STATUS)) {
			uri = ServerPaths.getPublishedAssessmentURL(id);
		} else if (assessmentType.equals(BaseAssessment.USER_ASSESSMENT_STATUS)) {
			uri = ServerPaths.getUserAssessmentUrl(user, id);
		} else {
			return null;
		}

		VFSPath path = null;
		try {
			path = VFSUtils.parseVFSPath(uri);
		} catch (VFSPathParseException e) {
			e.printStackTrace();
		}
		
		if( !vfs.exists(path) )
			return null;
		else {
			String xml = DocumentUtils.getVFSFileAsString(uri, vfs);
			if (xml != null && !xml.equals("")) {
				try {
					long lastMod = vfs.getLastModified(path);
					NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
					ndoc.parse(xml);
					AssessmentParser p = new AssessmentParser(ndoc);
					if( p.getAssessment() != null)
						p.getAssessment().setDateModified(lastMod);

					return p.getAssessment();
				} catch (NotFoundException e) {
					return null;
				}
			} else
				return null;
		}
	}
	
	public static String readAssessmentAsString(VFS vfs, String id, String assessmentType, String user) {
		String uri = "";

		if (assessmentType.equals(BaseAssessment.DRAFT_ASSESSMENT_STATUS)) {
			uri = ServerPaths.getDraftAssessmentURL(id);
		} else if (assessmentType.equals(BaseAssessment.PUBLISHED_ASSESSMENT_STATUS)) {
			uri = ServerPaths.getPublishedAssessmentURL(id);
		} else if (assessmentType.equals(BaseAssessment.USER_ASSESSMENT_STATUS)) {
			uri = ServerPaths.getUserAssessmentUrl(user, id);
		} else {
			return null;
		}

		VFSPath path = null;
		try {
			path = VFSUtils.parseVFSPath(uri);
		} catch (VFSPathParseException e) {
			e.printStackTrace();
		}
		
		if( !vfs.exists(path) )
			return null;
		else {
			String xml = DocumentUtils.getVFSFileAsString(uri, vfs);
			
			try {
				long lastMod = vfs.getLastModified(path);
				xml = xml.replaceAll("<\\?xml\\s*(version=.*)?\\s*(encoding=.*)?\\?>", "");
				xml = xml.replaceAll("(<dateModified>.*?</dateModified>)|(<dateModified\\s*/>)",
						"<dateModified>" + lastMod + "</dateModified>");

				if (xml != null && !xml.equals("")) {
					return xml;
				} else
					return null;
			} catch (NotFoundException e) {
				return null;
			}
		}
	}

	/**
	 * Fetches a list of assessments from the file system. If you expect all
	 * assessments to exist and want a null result if a taxa doesn't have an
	 * assessment, pass in true for the hardFail parameter. WILL NOT FETCH
	 * REGIONAL DRAFT ASSESSMENTS. Use readRegionalDraftAssessments(...) for
	 * that additional behavior.
	 * 
	 * @param vfs
	 * @param ids
	 * @param assessmentType
	 *            - use constants in BaseAssessment; USER, DRAFT or PUBLISHED
	 * @param user
	 * @param hardFail
	 *            - pass in true for a "hard" fail, meaning if an assessment
	 *            unexpectedly does not exist, this function will return a null
	 *            result
	 * @return a list of AssessmentData objects
	 */
	public static List<AssessmentData> readList(VFS vfs, String ids, String assessmentType, String user,
			boolean hardFail) {
		String[] split;
		if (ids.contains(","))
			split = ids.split(",");
		else
			split = new String[] { ids };

		return readList(vfs, Arrays.asList(split), assessmentType, user, hardFail);
	}
	
	public static List<AssessmentData> readList(VFS vfs, List<String> ids, String assessmentType, String user,
			boolean hardFail) {
		ArrayList<AssessmentData> list = new ArrayList<AssessmentData>();
		for (String cur : ids) {
			AssessmentData curAss = readAssessment(vfs, cur, assessmentType, user);
			if (curAss != null)
				list.add(curAss);
			else if (hardFail)
				return null; // FAST FAIL
		}

		return list;
	}

	/**
	 * Fetches a list of assessments from the file system. If you expect all
	 * assessments to exist and want a null result if a taxa doesn't have an
	 * assessment, pass in true for the hardFail parameter. WILL NOT FETCH
	 * REGIONAL DRAFT ASSESSMENTS. Use readRegionalDraftAssessments(...) for
	 * that additional behavior.
	 * 
	 * @param vfs
	 * @param ids
	 * @param assessmentType
	 *            - use constants in BaseAssessment; USER, DRAFT or PUBLISHED
	 * @param user
	 * @param hardFail
	 *            - pass in true for a "hard" fail, meaning if an assessment
	 *            unexpectedly does not exist, this function will return a null
	 *            result
	 * @return a list of AssessmentData objects
	 */
	public static List<String> readListAsStrings(VFS vfs, String ids, String assessmentType, String user,
			boolean hardFail) {
		String[] split;
		if (ids.contains(","))
			split = ids.split(",");
		else
			split = new String[] { ids };

		return readListAsStrings(vfs, Arrays.asList(split), assessmentType, user, hardFail);
	}
	
	public static List<String> readListAsStrings(VFS vfs, List<String> ids, String assessmentType, String user,
			boolean hardFail) {
		
		ArrayList<String> list = new ArrayList<String>();
		for (String cur : ids) {
			String curAss = readAssessmentAsString(vfs, cur, assessmentType, user);
			if (curAss != null)
				list.add(curAss);
			else if (hardFail)
				return null; // FAST FAIL
		}

		return list;
	}
	
	/**
	 * Reads in a list of regional draft assessments for a list of taxon ids.
	 * 
	 * @param vfs
	 * @param ids
	 *            - a list of taxon ids, as a csv
	 * @param region
	 *            - restrict to this region
	 * @return
	 */
	public static List<AssessmentData> readRegionalDraftAssessmentsForTaxonList(VFS vfs, String ids, String region) {
		List<AssessmentData> list = new ArrayList<AssessmentData>();

		String[] split;

		if (ids.contains(","))
			split = ids.split(",");
		else
			split = new String[] { ids };

		for (String curID : split) {
			if( region == null )
				list.addAll(readAllDraftAssessments(vfs, curID));
			else {
				for( AssessmentData cur : readAllDraftAssessments(vfs, curID) ) {
					if (cur.getRegionIDs().contains(region))
						list.add(cur);
				}
			}
		}

		return list;
	}

	public static boolean trashAssessment(VFS vfs, AssessmentData assessment, String username) 
			throws ConflictException, NotFoundException {
		VFSPath path = null;
		VFSPath trashURL = null;
		TaxonNode taxon = null;
		
		try {
			if( assessment.getType().equals(BaseAssessment.DRAFT_ASSESSMENT_STATUS)) {
				path = VFSUtils.parseVFSPath(ServerPathsv2.getPathForDraftAssessment(assessment));
				trashURL = VFSUtils.parseVFSPath(path.toString().replace("/drafts", "/trash/drafts"));
			} else if( assessment.getType().equals(BaseAssessment.PUBLISHED_ASSESSMENT_STATUS)) {
				path = VFSUtils.parseVFSPath(ServerPathsv2.getPublishedAssessmentURL(assessment.getAssessmentID()));
				trashURL = VFSUtils.parseVFSPath(path.toString().replace("/browse", "/trash"));
				taxon = TaxaIO.readNode(assessment.getSpeciesID(), vfs);
			} else {
				path = VFSUtils.parseVFSPath(ServerPathsv2.getUserAssessmentUrl(username, assessment.getAssessmentID()));
				trashURL = VFSUtils.parseVFSPath(path.toString().replace("/user", "/trash/user"));
			}
		} catch (VFSPathParseException e) {
			e.printStackTrace();
		}
		
		Lock lock = FileLocker.impl.getAssessmentPersistentLock(assessment.getAssessmentID(), assessment.getType());
		if( lock != null && !lock.getUsername().equals(username) ) {
			return false;
		} else {
			if (vfs.exists(trashURL))
				vfs.delete(trashURL);

			vfs.move(path, trashURL);
			
			if( taxon != null && taxon.getAssessments().contains(assessment.getAssessmentID())) {
				taxon.getAssessments().remove(assessment.getAssessmentID());
				TaxaIO.writeNode(taxon, vfs);
			}
			
			return true;
		}
	}
	
	/**
	 * Returns an xml document containing the ids of all the Assessments that
	 * were written, null if the write was unsuccessful
	 * @param requireLocking TODO
	 * @param assessmentsToSave
	 * 
	 * @return XML doc of ids
	 */
	public static AssessmentIOWriteResult writeAssessment(AssessmentData assessmentToSave, String user, VFS vfs, boolean requireLocking) {
		Status lockStatus = Status.SUCCESS_OK;
		if( SISContainerApp.amIOnline && requireLocking )
			FileLocker.impl.persistentLockAssessment(assessmentToSave.getAssessmentID(), assessmentToSave
				.getType(), LockType.SAVE_LOCK, user);
		
		String url;
		long lastModified = -1;

		if (!requireLocking || lockStatus == Status.SUCCESS_OK) {
			String id = assessmentToSave.getAssessmentID().replace("offline", "");
			if (assessmentToSave.getType().equals(BaseAssessment.DRAFT_ASSESSMENT_STATUS)) {
				url = ServerPaths.getDraftAssessmentURL(id);
			} else if (assessmentToSave.getType().equals(BaseAssessment.PUBLISHED_ASSESSMENT_STATUS)) {
				url = ServerPaths.getPublishedAssessmentURL(id);
			} else {
				url = ServerPaths.getUserAssessmentUrl(user, id);
			}

			try {
				AssessmentData refAssessment = ReferenceCrawler.updateHashesAndCaptureToDatabase(assessmentToSave,
						new SystemExecutionContext(ReferenceApplication.DBNAME));

				if (refAssessment == null) {
					assessmentToSave.setUserLastUpdated(user);
					DocumentUtils.writeVFSFile(url, vfs, assessmentToSave.toXML());
					DBMirrorManager.impl.assessmentChanged(assessmentToSave);
				} else {
					assessmentToSave.setUserLastUpdated(user);
					DocumentUtils.writeVFSFile(url, vfs, refAssessment.toXML());
					DBMirrorManager.impl.assessmentChanged(refAssessment);
				}
			} catch (Throwable e) {
				e.printStackTrace();
				System.out.println("UNABLE TO INDEX REFERENCES IN ASSESSMENT " + assessmentToSave.getAssessmentID()
						+ ":" + assessmentToSave.getType() + ". Saving anyway...");
				assessmentToSave.setUserLastUpdated(user);
				DocumentUtils.writeVFSFile(url, vfs, assessmentToSave.toXML());
				DBMirrorManager.impl.assessmentChanged(assessmentToSave);
			}

			try {
				lastModified = vfs.getLastModified(VFSUtils.parseVFSPath(url));
			} catch (NotFoundException e) {
				e.printStackTrace();
			} catch (VFSPathParseException e) {
				e.printStackTrace();
			}
		}

		return new AssessmentIOWriteResult(lockStatus, assessmentToSave.getAssessmentID(), lastModified);
	}

	/**
	 * Returns an xml document containing the ids of all the Assessments that
	 * were written, null if the write was unsuccessful
	 * 
	 * @param assessmentsToSave
	 * @param requireLocking TODO
	 * @return XML doc of ids
	 */
	public static AssessmentIOMessage writeAssessments(List<AssessmentData> assessmentsToSave, String user, VFS vfs, boolean requireLocking) {
		AssessmentIOMessage ret = new AssessmentIOMessage();
		String url;

		for (AssessmentData curAss : assessmentsToSave) {
			Status lockStatus = Status.SUCCESS_OK;
			if( requireLocking )
				lockStatus = FileLocker.impl.persistentLockAssessment(curAss.getAssessmentID(), curAss.getType(),
					LockType.SAVE_LOCK, user);

			if( lockStatus == Status.SUCCESS_OK) {
				if (curAss.getType().equals(BaseAssessment.DRAFT_ASSESSMENT_STATUS)) {
					url = ServerPaths.getDraftAssessmentURL(curAss.getAssessmentID());
				} else if (curAss.getType().equals(BaseAssessment.PUBLISHED_ASSESSMENT_STATUS)) {
					url = ServerPaths.getPublishedAssessmentURL(curAss.getAssessmentID());
				} else {
					url = ServerPaths.getUserAssessmentUrl(user, curAss.getAssessmentID());
				}

				try {
					AssessmentData refAssessment = ReferenceCrawler.updateHashesAndCaptureToDatabase(curAss,
							new SystemExecutionContext(ReferenceApplication.DBNAME));

					if (refAssessment == null) {
						curAss.setUserLastUpdated(user);
						DocumentUtils.writeVFSFile(url, vfs, curAss.toXML());
						ret.addSuccessfullySaved(curAss);
					} else {
						curAss.setUserLastUpdated(user);
						DocumentUtils.writeVFSFile(url, vfs, refAssessment.toXML());
						ret.addSuccessfullySaved(refAssessment);
					}
				} catch (NamingException e) {
					System.out.println("UNABLE TO INDEX REFERENCES IN ASSESSMENT " + curAss.getAssessmentID() + ":"
							+ curAss.getType() + ". Saving anyway...");
					curAss.setUserLastUpdated(user);
					DocumentUtils.writeVFSFile(url, vfs, curAss.toXML());
					ret.addSuccessfullySaved(curAss);
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("--- HARD FAILURE WHEN ATTEMPTING TO INDEX REFERENCES IN ASSESSMENT " + curAss.getAssessmentID() + ":"
							+ curAss.getType() + ". Trying to save anyway...");
					curAss.setUserLastUpdated(user);
					DocumentUtils.writeVFSFile(url, vfs, curAss.toXML());
					ret.addSuccessfullySaved(curAss);
				}
			} else if (lockStatus == Status.CLIENT_ERROR_FORBIDDEN) {
				ret.addLocked(curAss);
			}
		}

		DBMirrorManager.impl.assessmentsChanged(ret.getSuccessfullySaved());
		
		return ret;
	}
}
