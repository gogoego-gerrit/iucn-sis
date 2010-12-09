package org.iucn.sis.server.api.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.NonUniqueObjectException;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.locking.LockType;
import org.iucn.sis.server.api.persistance.AssessmentCriteria;
import org.iucn.sis.server.api.persistance.AssessmentDAO;
import org.iucn.sis.server.api.persistance.AssessmentTypeCriteria;
import org.iucn.sis.server.api.persistance.EditDAO;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.TaxonCriteria;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.utils.DocumentUtils;
import org.iucn.sis.server.api.utils.OnlineUtil;
import org.iucn.sis.server.api.utils.RegionConflictException;
import org.iucn.sis.server.api.utils.ServerPaths;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.io.AssessmentIOMessage;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentType;
import org.iucn.sis.shared.api.models.Edit;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.User;
import org.restlet.data.Status;

import com.solertium.db.DBException;
import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.vfs.BoundsException;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.provider.VersionedFileVFS;

/**
 * Performs file system IO operations for Assessments.
 * 
 * @author adam.schwartz
 * 
 */
public class AssessmentIO {

	protected VersionedFileVFS vfs;

	public AssessmentIO(VersionedFileVFS vfs) {
		this.vfs = vfs;
	}

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
		public Integer id;
		public long newLastModified;

		public AssessmentIOWriteResult(Status status, Integer assessmentID, long newLastModified) {
			this.status = status;
			this.id = assessmentID;
			this.newLastModified = newLastModified;
		}
	}

	/**
	 * The default call to getAssessment. Should get from cached then get from
	 * VFS, then get from DB. This version will not be merged into database so
	 * if you need to update and then save you should call
	 * getAttachedAssessment. If you don't know if it exists, then call exists
	 * first.
	 * 
	 * @param id
	 * @return
	 */
	public Assessment getAssessment(Integer id) {
		Assessment assessment = null;

		// TODO: GET FROM CACHE FIRST
		assessment = getFromVFS(id);
		if (assessment == null)
			assessment = getNonCachedAssessment(id);

		return assessment;

	}

	public String getAssessmentXML(Integer id) {
		try {
			return getXMLFromVFS(id);
		} catch (NotFoundException e) {
			Assessment assessment = getNonCachedAssessment(id);
			Edit edit = assessment.getLastEdit();
			String xml = assessment.toXML();
			String serverPaths = ServerPaths.getAssessmentURL(assessment);
			DocumentUtils.writeVFSFile(serverPaths, vfs, xml);
			try {
				if (edit != null)
					vfs.setLastModified(serverPaths, edit.getCreatedDate());
			} catch (NotFoundException e1) {
				e.printStackTrace();
			}

			return xml;
		} catch (BoundsException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * The way to get an "attached" assessment if you need to do operations on
	 * the assessment and then save.
	 * 
	 * @param id
	 * @return
	 * @throws PersistentException
	 * @throws HibernateException
	 */
	public Assessment getAttachedAssessment(Integer id) {
		Assessment assessment = getFromVFS(id);
		try {
			assessment = SIS.get().getManager().mergeObject(assessment);
		} catch (PersistentException e) {
			//Guess we're getting it uncached...
			assessment = null;
		}
		
		if (assessment == null)
			assessment = getNonCachedAssessment(id);
		
		return assessment;
	}

	/**
	 * The last case call for the Assessment. This is called when building an
	 * assessment object from the DB only. Will take minutes. By default call
	 * getAssessment
	 * 
	 * @param id
	 * @return
	 */
	public Assessment getNonCachedAssessment(Integer id) {
		try {
			return AssessmentDAO.getAssessment(id);
		} catch (PersistentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Reads in regional draft assessments based on the taxonID. When we shuffle
	 * storage to store globals in with the regionals, this will read in all
	 * assessments.
	 * 
	 * @param vfs
	 * @param taxonID
	 * @return a list of Assessment objects. Will never be null, might be empty.
	 * @throws PersistentException
	 */
	public List<Assessment> readDraftAssessmentsForTaxon(Integer taxonID) {

		AssessmentCriteria criteria;
		try {
			criteria = new AssessmentCriteria();
		} catch (PersistentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		AssessmentTypeCriteria assTypeCriteria = criteria.createAssessment_typeCriteria();
		assTypeCriteria.id.eq(AssessmentType.DRAFT_ASSESSMENT_STATUS_ID);
		TaxonCriteria taxonCriteria = criteria.createTaxonCriteria();
		taxonCriteria.id.eq(taxonID);
		return Arrays.asList(AssessmentDAO.getAssessmentsByCriteria(criteria));
	}

	public List<Assessment> readPublishedAssessmentsForTaxon(Integer taxonID) {
		AssessmentCriteria criteria;
		try {
			criteria = new AssessmentCriteria();
		} catch (PersistentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		AssessmentTypeCriteria assTypeCriteria = criteria.createAssessment_typeCriteria();
		assTypeCriteria.id.eq(AssessmentType.PUBLISHED_ASSESSMENT_STATUS_ID);
		TaxonCriteria taxonCriteria = criteria.createTaxonCriteria();
		taxonCriteria.id.eq(taxonID);
		return Arrays.asList(AssessmentDAO.getAssessmentsByCriteria(criteria));
	}

	/**
	 * This method returns a List of all draft assessments - global and regional
	 * - for one taxon. If no assessments exist, the list will be empty.
	 * 
	 * @param taxonID
	 */
	public List<Assessment> readAssessmentsForTaxon(Integer taxonID) {

		AssessmentCriteria criteria;
		try {
			criteria = new AssessmentCriteria();
		} catch (PersistentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		TaxonCriteria taxonCriteria = criteria.createTaxonCriteria();
		taxonCriteria.id.eq(taxonID);
		return new ArrayList<Assessment>(Arrays.asList(AssessmentDAO.getAssessmentsByCriteria(criteria)));
	}

	public List<Assessment> readPublishedAssessmentsForTaxon(Taxon taxon) {
		return readPublishedAssessmentsForTaxon(taxon.getId());
	}

	public List<Assessment> getAssessments(List<Integer> ids, boolean hardFail) {
		ArrayList<Assessment> list = new ArrayList<Assessment>();
		for (Integer cur : ids) {
			Assessment ass = getAssessment(cur);
			if (ass == null) {
				if (hardFail)
					return null;
			} else {
				list.add(ass);
			}
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
	public List<Assessment> readRegionalDraftAssessmentsForTaxonList(List<Integer> taxonIDs, List<Integer> regionIDs) {
		List<Assessment> list = new ArrayList<Assessment>();
		for (Integer taxonID : taxonIDs) {
			for (Assessment ass : readDraftAssessmentsForTaxon(taxonID)) {
				for (Integer regionID : regionIDs)
					if (ass.getRegionIDs().contains(regionID)) {
						list.add(ass);
						break;
					}
			}
		}

		return list;

	}

	public Assessment[] getTrashedAssessments() throws PersistentException {
		return AssessmentDAO.getTrashedAssessments();
	}

	/**
	 * Returns either the restored Deleted assessment or null if failure
	 * 
	 * @param assessmentID
	 * @param user
	 * @return
	 * @throws PersistentException
	 */
	public AssessmentIOWriteResult restoreTrashedAssessments(Integer assessmentID, User user) throws PersistentException {
		Assessment ass = AssessmentDAO.getTrashedAssessment(assessmentID);
		if (ass != null) {
			ass.setState(Assessment.ACTIVE);
			return writeAssessment(ass, user, true);
		}
		return null;

	}

	public AssessmentIOWriteResult trashAssessment(Assessment assessment, User user) {
		assessment.setState(Assessment.DELETED);
		return writeAssessment(assessment, user, true);
	}

	public boolean permenantlyDeleteAssessment(Integer assessmentID, User user) {
		Assessment ass = getDeletedAssessment(assessmentID);
		if (ass != null) {
			try {
				return AssessmentDAO.deleteAndDissociate(ass);
			} catch (PersistentException e) {
				Debug.println(e);
			}
		}
		return false;
	}

	public boolean permenantlyDeleteAllTrashedAssessments() {
		
		try {
			for (Assessment assessmentToSave : getTrashedAssessments())
				if (!AssessmentDAO.deleteAndDissociate(assessmentToSave)) {
					throw new PersistentException("Unable to delete assessment " + assessmentToSave.getId());
				}
			return true;
		} catch (PersistentException e) {
			Debug.println(e);
		}
		return false;
		
	}

	public Assessment getDeletedAssessment(Integer assessmentID) {
		try {
			return AssessmentDAO.getTrashedAssessment(assessmentID);
		} catch (PersistentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public AssessmentIOMessage restoreDeletedAssessmentsAssociatedWithTaxon(Integer taxonID, User user)
			throws PersistentException {
		AssessmentCriteria criteria = new AssessmentCriteria();
		TaxonCriteria taxCriteria = criteria.createTaxonCriteria();
		taxCriteria.id.eq(taxonID);
		Assessment[] assessments = AssessmentDAO.getTrashedAssessmentsByCriteria(criteria);
		for (Assessment ass : assessments) {
			ass.setState(Assessment.ACTIVE);
		}
		return writeAssessments(Arrays.asList(assessments), user, true);
	}

	public AssessmentIOWriteResult writeAssessment(Assessment assessmentToSave, User user, boolean requireLocking) {

		Status lockStatus = Status.SUCCESS_OK;
		if (OnlineUtil.amIOnline() && requireLocking)
			lockStatus = SIS.get().getLocker().persistentLockAssessment(assessmentToSave.getId(), LockType.SAVE_LOCK,
					user);

		if (lockStatus.isSuccess()) {
			Edit edit = new Edit();
			edit.setUser(user);
			edit.getAssessment().add((assessmentToSave));
			assessmentToSave.getEdit().add(edit);
			assessmentToSave.toXML();

			
				try {
					AssessmentDAO.save(assessmentToSave);
				} catch (PersistentException e) {
					e.printStackTrace();
					return new AssessmentIOWriteResult(lockStatus, 0, 0);
					
				}

			return new AssessmentIOWriteResult(lockStatus, assessmentToSave.getId(), edit.getCreatedDate().getTime());

		}
		return new AssessmentIOWriteResult(lockStatus, 0, 0);

	}

	/**
	 * Best effort to write assessments. If some were locked then it will be
	 * noted in in the assessmentIOmessage
	 * 
	 * @param assessments
	 * @param user
	 * @param requireLocking
	 * @return
	 */
	public AssessmentIOMessage writeAssessments(List<Assessment> assessments, User user, boolean requireLocking) {
		AssessmentIOMessage ret = new AssessmentIOMessage();
		for (Assessment ass : assessments) {
			AssessmentIOWriteResult result = writeAssessment(ass, user, requireLocking);
			if (result.status == Status.CLIENT_ERROR_FORBIDDEN) {
				ret.addLocked(ass);
			} else if (result.status.isServerError()) {
				ret.addFailed(ass);
			} else if (result.status.isSuccess()) {
				ret.addSuccessfullySaved(ass);
			} else if (result.status.isClientError()) {
				ret.addInsufficientPermissions(ass);
			}
		}
		return ret;
	}

	/**
	 * FIXME: this is bad for obvious reasons.
	 * @param assessments
	 * @param user
	 * @return
	 */
	public boolean saveAssessmentsWithNoFail(Collection<Assessment> assessments, User user) {
		Status lockStatus = Status.SUCCESS_OK;
		boolean success = false;
		if (SIS.get().amIOnline()) {
			lockStatus = SIS.get().getLocker().persistentLockAssessments(assessments, LockType.SAVE_LOCK, user);
		}

		if (lockStatus.isSuccess()) {
			try {
				for (Assessment assessmentToSave : assessments) {
					Edit edit = new Edit();
					edit.setUser(user);
					edit.getAssessment().add((assessmentToSave));
					assessmentToSave.getEdit().add(edit);
					assessmentToSave.toXML();
					AssessmentDAO.save(assessmentToSave);
					EditDAO.save(edit);
				}
				success = true;
			} catch (PersistentException e) {	
				Debug.println(e);
			}
		}

		return success;
	}

	public boolean moveAssessments(Taxon newParent, Collection<Assessment> assessments, User user) {
		for (Assessment assessment : assessments) {
			assessment.setTaxon(newParent);
		}
		return saveAssessmentsWithNoFail(assessments, user);
	}

	public boolean allowedToCreateNewAssessment(Assessment assessment) {
		if (assessment.getType().equals(AssessmentType.DRAFT_ASSESSMENT_TYPE)) {
			List<Assessment> compareTo = SIS.get().getAssessmentIO().readDraftAssessmentsForTaxon(
					assessment.getTaxon().getId());
			String defaultSchema = SIS.get().getDefaultSchema();
			for (Assessment cur : compareTo) {
				if (!cur.getSchema(defaultSchema).equals(assessment.getSchema(defaultSchema)))
					continue;
				
				if ((cur.isGlobal() && assessment.isGlobal())
						|| cur.getRegionIDs().containsAll(assessment.getRegionIDs())) {
					if (cur.getId() != assessment.getId())
						return false;

				}
			}

			try {
				SIS.get().getManager().getSession().clear();
			} catch (PersistentException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	public AssessmentIOWriteResult saveNewAssessment(Assessment assessent, User user) throws RegionConflictException {
		if (!allowedToCreateNewAssessment(assessent))
			throw new RegionConflictException();
		return writeAssessment(assessent, user, true);
	}

	/**
	 * ONLY CALLED BY THE SISHIBERNATE LISTENER, OR WHEN SOMETHING 
	 * BAD HAPPENS AND YOU NEED TO WRITE IT OUT TO THE VFS
	 * 
	 * @param assessment
	 */
	public void afterSaveAssessment(Assessment assessment) {
		Edit edit = assessment.getLastEdit();
		String xml = assessment.toXML();
		String serverPaths = ServerPaths.getAssessmentURL(assessment);
		DocumentUtils.writeVFSFile(serverPaths, vfs, xml);
		try {
			vfs.setLastModified(serverPaths, edit.getCreatedDate());
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
	}

	protected Assessment getFromVFS(Integer id) {
		try {
			String assessmentXML = getXMLFromVFS(id);
			NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
			ndoc.parse(assessmentXML);
			return Assessment.fromXML(ndoc);
		} catch (NotFoundException e) {
			Debug.println("--- Assessment " + id + " not found on File System. Serving DB copy.");
		} catch (BoundsException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected String getXMLFromVFS(Integer id) throws NotFoundException, BoundsException, IOException {
		return vfs.getString(ServerPaths.getAssessmentUrl(id));
	}

}
