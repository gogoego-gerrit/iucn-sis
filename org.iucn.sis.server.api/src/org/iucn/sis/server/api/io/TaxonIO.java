package org.iucn.sis.server.api.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.io.AssessmentIO.AssessmentIOWriteResult;
import org.iucn.sis.server.api.locking.TaxonLockAquirer;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.TaxonCriteria;
import org.iucn.sis.server.api.persistance.TaxonDAO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.utils.DocumentUtils;
import org.iucn.sis.server.api.utils.ServerPaths;
import org.iucn.sis.server.api.utils.TaxomaticException;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Edit;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.User;

import com.solertium.lwxml.java.JavaNativeDocument;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.vfs.BoundsException;
import com.solertium.vfs.ConflictException;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.provider.VersionedFileVFS;

public class TaxonIO {

	protected VersionedFileVFS vfs;

	public TaxonIO(VersionedFileVFS vfs) {
		this.vfs = vfs;
	}

	public TaxonCriteria getCriteria() {
		try {
			return new TaxonCriteria();
		} catch (PersistentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public Taxon[] search(TaxonCriteria criteria) {
		return TaxonDAO.getTaxonByCriteria(criteria);
	}

	public Taxon getTaxon(Integer id) {
		try {
			return TaxonDAO.getTaxon(id);
		} catch (PersistentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public Taxon getTaxonNonLazy(Integer id) {
		try {
			return TaxonDAO.getTaxonNonLazily(id);
		} catch (PersistentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}

	public List<Taxon> getTaxa(String idCSV) {
		List<Taxon> taxa = new ArrayList<Taxon>();
		for (String id : idCSV.split(",")) {
			taxa.add(getTaxon(Integer.valueOf(id)));
		}
		return taxa;
	}

	public String getTaxonXML(Integer id) {
		try {
			return getXMLFromVFS(id);
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Reads taxa in from the file system based on an ID and parses it using the
	 * TaxonFactory. If a taxon cannot be read in, nullFail determines whether
	 * it immediately returns a null response or if it skips over it and returns
	 * what it can; if nullFail is true, null is returned.
	 * 
	 * @param ids
	 *            of the taxon
	 * @param nullFail
	 *            - if a taxon cannot be read in, e.g. if it's missing, nullFail
	 *            == true will cause the returned list to be null
	 * @return a list of Taxon objects
	 */
	public List<Taxon> getTaxa(Integer[] ids, boolean nullFail) {
		List<Taxon> list = new ArrayList<Taxon>();
		for (Integer id : ids) {
			Taxon taxon;
			try {
				taxon = TaxonDAO.getTaxon(id);
			} catch (PersistentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				taxon = null;
			}
			if (taxon == null) {
				if (nullFail)
					return null;
			} else {
				list.add(taxon);
			}
		}

		return list;
	}

	public void writeTaxon(Taxon taxonToSave, User user) throws TaxomaticException {
		Taxon oldTaxon = getTaxonFromVFS(taxonToSave.getId());
		if (oldTaxon != null)
			writeTaxon(taxonToSave, oldTaxon, user);
		else
			writeTaxon(taxonToSave, taxonToSave, user);
	}

	/**
	 * Returns whether the taxa was allowed to be saved. Note that only
	 * non-taxomatic operations on the node can be saved here. To do taxomatic
	 * opertaions, use taxomaticIO
	 * 
	 * @param nodeToSave
	 * @return summary
	 */
	public void writeTaxon(Taxon nodeToSave, User user, boolean requireLocking) throws TaxomaticException {
		ArrayList<Taxon> list = new ArrayList<Taxon>();
		list.add(nodeToSave);
		
		writeTaxa(list, user, requireLocking);
	}

	void writeTaxon(Taxon taxonToSave, Taxon oldTaxon, User user) throws TaxomaticException {
		ArrayList<Taxon> list = new ArrayList<Taxon>();
		list.add(taxonToSave);

		Map<Integer, Taxon> idToOldTaxa = new HashMap<Integer, Taxon>();
		idToOldTaxa.put(taxonToSave.getId(), oldTaxon);
		
		writeTaxa(list, idToOldTaxa, user, true);
	}

	/**
	 * Returns whether the taxa was allowed to be saved. Notice that only
	 * non-taxomatic operations on the node can be saved here to do taxomatic
	 * opertaions, use taxomaticIO
	 * 
	 */
	public void writeTaxa(Collection<Taxon> nodesToSave, User user, boolean requireLocking) throws TaxomaticException {
		Map<Integer, Taxon> idToOldTaxa = new HashMap<Integer, Taxon>();
		for (Taxon taxon : nodesToSave)
			idToOldTaxa.put(taxon.getId(), getTaxonFromVFS(taxon.getId()));
		
		writeTaxa(nodesToSave, idToOldTaxa, user, requireLocking);
	}

	void writeTaxa(Collection<Taxon> taxaToSave, Map<Integer, Taxon> idToOldTaxa, User user, boolean requireLocking) throws TaxomaticException {

		// DO NOT ALLOW ANY TAXOMATIC OPERATION SAVES
		for (Taxon taxon : taxaToSave) {
			if (idToOldTaxa.get(taxon.getId()) != null
					&& SIS.get().getTaxomaticIO().isTaxomaticOperationNecessary(taxon, idToOldTaxa.get(taxon.getId()))) {
				throw new TaxomaticException("Taxomatic operation necessary, can not save.", false);
			}
		}

		// TRY TO AQUIRE LOCKS
		TaxonLockAquirer aquirer = new TaxonLockAquirer(taxaToSave);
		if (requireLocking)
			aquirer.aquireLocks();

		if (!requireLocking || aquirer.isSuccess()) {
			try {
				Date date = new Date();
				for (Taxon taxon : taxaToSave) {
					Edit edit = new Edit();
					edit.setUser(user);
					edit.setCreatedDate(date);
					edit.getTaxon().add(taxon);
					taxon.getEdits().add(edit);
					taxon.toXML();
					
					TaxonDAO.save(taxon);
				}
			} catch (PersistentException e) {
				Debug.println("Failed to save taxa list: \n{0}", e);
				throw new TaxomaticException("Failed to save taxa changes due to server error.", false);
			} finally {
				aquirer.releaseLocks();
			}
		}
		else
			throw new TaxomaticException("Failed to acquire lock to save taxa.", false);
	}

	public boolean permenantlyDeleteAllTrashedTaxa() {
		
			try {
				for (Taxon taxon : getTrashedTaxa())
					if (!TaxonDAO.delete(taxon)) {
						throw new PersistentException("Unable to delete taxon " + taxon);
					}
				return true;
			} catch (PersistentException e) {
				// TODO Auto-generated catch block
				Debug.println(e);
			}
			
			return false;
		
	}

	public Taxon getTrashedTaxon(Integer id) {
		try {
			return TaxonDAO.getTrashedTaxon(id);
		} catch (PersistentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public Taxon[] getTrashedTaxa() throws PersistentException {
		return TaxonDAO.getTrashedTaxa();
	}

	public void restoreTrashedTaxon(Integer taxonID, User user) throws TaxomaticException {
		Taxon taxon;
		try {
			taxon = TaxonDAO.getTrashedTaxon(taxonID);
		} catch (PersistentException e) {
			taxon = null;
		}
		
		if (taxon == null)
			throw new TaxomaticException("Could not find taxon " + taxonID + " in the trash to restore.");
		
		taxon.setState(Taxon.ACTIVE);
		
		writeTaxon(taxon, user);
	}

	public boolean permanentlyDeleteTaxon(Integer taxonID) {
		Taxon taxon = getTrashedTaxon(taxonID);
		if (taxon != null) {
			try {
				return TaxonDAO.deleteAndDissociate(taxon);
			} catch (PersistentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}

	public void trashTaxon(Taxon taxon, User user) throws TaxomaticException {
		if (taxon.getChildren().size() > 0)
			throw new TaxomaticException("You can not delete a taxon that has children.");
		
		taxon.setState(Taxon.DELETED);
		
		writeTaxon(taxon, user);
		
		for (Assessment assessment : taxon.getAssessments()) {
			AssessmentIOWriteResult result = SIS.get().getAssessmentIO().trashAssessment(assessment, user);
			if (result.status.isError())
				throw new TaxomaticException("Unable to save assessment " + assessment.getId() + ", it may be locked");
		}
	}

	public Taxon readTaxonByName(String kingdomName, String name) {
		try {
			TaxonCriteria criteria = new TaxonCriteria();
			criteria.friendlyName.eq(name);
			Taxon[] taxa = TaxonDAO.getTaxonByCriteria(criteria);
			for (Taxon taxon : taxa) {
				if (taxon.getKingdomName().equalsIgnoreCase(kingdomName))
					return taxon;
			}
		} catch (PersistentException e) {
			e.printStackTrace();
		}
		return null;
	}


	/**
	 * SHOULD ONLY BE CALLED FROM THE SISHIBERNATELISTENER, 
	 * OR WHEN NEEDED TO WRITE OUT TO VFS
	 * 
	 * @param taxon
	 */
	public void afterSaveTaxon(Taxon taxon) {
		Edit edit = taxon.getLastEdit();
		String xml = taxon.getGeneratedXML();
		String taxonPath = ServerPaths.getTaxonURL(taxon.getId());
		if (xml != null) {
			DocumentUtils.writeVFSFile(taxonPath, vfs, xml);
		} else {
			try {
				vfs.delete(taxonPath);
			} catch (NotFoundException e) {
				Debug.println(e);
			} catch (ConflictException e) {
				Debug.println(e);
			}
		}
		
		try {
			vfs.setLastModified(taxonPath, edit.getCreatedDate());
		} catch (NotFoundException e) {
			e.printStackTrace();
		}

	}

	public Taxon getTaxonFromVFS(Integer taxonID) {
		final String text;
		try {
			text = getXMLFromVFS(taxonID);
		} catch (IOException e) {
			Debug.println("Failed to load taxon {0} from VFS: \n{1}", taxonID, e);
			return null;
		}
		if (text != null && !text.trim().equalsIgnoreCase("")) {
			NativeDocument ndoc = new JavaNativeDocument();
			ndoc.parse(text);
			return Taxon.fromXML(ndoc);
		}
		else
			return null;
	}

	public Taxon[] getChildrenOfTaxon(Integer parentTaxonID) throws PersistentException {
		TaxonCriteria criteria = new TaxonCriteria();
		TaxonCriteria parentCriteria = criteria.createParentCriteria();
		parentCriteria.id.eq(parentTaxonID);
		return TaxonDAO.getTaxonByCriteria(criteria);
	}

	public List<Taxon> getChildrenOfTaxonRecurisvely(Integer parentTaxonID) throws PersistentException {
		ArrayList<Taxon> children = new ArrayList<Taxon>();
		children.addAll(Arrays.asList(getChildrenOfTaxon(parentTaxonID)));
		for (int i = 0; i < children.size(); i++) {
			children.addAll(children.get(i).getChildren());
		}
		return children;
	}

	protected String getXMLFromVFS(Integer id) throws NotFoundException, BoundsException, IOException {
		return vfs.getString(ServerPaths.getTaxonURL(id));
	}

}
