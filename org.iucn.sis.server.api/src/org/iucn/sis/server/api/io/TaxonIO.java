package org.iucn.sis.server.api.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.io.AssessmentIO.AssessmentIOWriteResult;
import org.iucn.sis.server.api.locking.TaxonLockAquirer;
import org.iucn.sis.server.api.persistance.AssessmentDAO;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.TaxonCriteria;
import org.iucn.sis.server.api.persistance.TaxonDAO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.utils.DocumentUtils;
import org.iucn.sis.server.api.utils.ServerPaths;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.CommonName;
import org.iucn.sis.shared.api.models.Edit;
import org.iucn.sis.shared.api.models.Synonym;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.User;

import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.vfs.BoundsException;
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

	public boolean writeTaxon(Taxon taxonToSave, User user) {
		Taxon oldTaxon = getTaxonFromVFS(taxonToSave.getId());
		if (oldTaxon != null) {
			return writeTaxon(taxonToSave, oldTaxon, user);
		} else {
			return writeTaxon(taxonToSave, taxonToSave, user);
		}
	}

	/**
	 * Returns whether the taxa was allowed to be saved. Note that only
	 * non-taxomatic operations on the node can be saved here. To do taxomatic
	 * opertaions, use taxomaticIO
	 * 
	 * @param nodeToSave
	 * @return summary
	 */
	public boolean writeTaxon(Taxon nodeToSave, User user, boolean requireLocking) {
		ArrayList<Taxon> list = new ArrayList<Taxon>();
		list.add(nodeToSave);
		return writeTaxa(list, user, requireLocking);
	}

	boolean writeTaxon(Taxon taxonToSave, Taxon oldTaxon, User user) {

		ArrayList<Taxon> list = new ArrayList<Taxon>();
		list.add(taxonToSave);

		Map<Integer, Taxon> idToOldTaxa = new HashMap<Integer, Taxon>();
		idToOldTaxa.put(taxonToSave.getId(), oldTaxon);
		return writeTaxa(list, idToOldTaxa, user, true);

	}

	/**
	 * Returns whether the taxa was allowed to be saved. Notice that only
	 * non-taxomatic operations on the node can be saved here to do taxomatic
	 * opertaions, use taxomaticIO
	 * 
	 */
	public boolean writeTaxa(Collection<Taxon> nodesToSave, User user, boolean requireLocking) {

		Map<Integer, Taxon> idToOldTaxa = new HashMap<Integer, Taxon>();
		for (Taxon taxon : nodesToSave) {
			idToOldTaxa.put(taxon.getId(), getTaxonFromVFS(taxon.getId()));
		}
		return writeTaxa(nodesToSave, idToOldTaxa, user, requireLocking);

	}

	boolean writeTaxa(Collection<Taxon> taxaToSave, Map<Integer, Taxon> idToOldTaxa, User user, boolean requireLocking) {

		// DO NOT ALLOW ANY TAXOMATIC OPERATION SAVES
		for (Taxon taxon : taxaToSave) {
			if (idToOldTaxa.get(taxon.getId()) != null
					&& SIS.get().getTaxomaticIO().isTaxomaticOperationNecessary(taxon, idToOldTaxa.get(taxon.getId()))) {
				return false;
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
					taxon.getReference();
					if (!TaxonDAO.save(taxon)) {
						break;
					}
				}
				return true;
			} catch (PersistentException e) {
				e.printStackTrace();
			} finally {
				aquirer.releaseLocks();
			}

		} else {

		}
		return false;
	}

	public boolean permenantlyDeleteAllTrashedTaxa() {
		Session session;

		session = SISPersistentManager.instance().getSession();
		Transaction tx = session.beginTransaction();
		try {
			for (Taxon taxon : getTrashedTaxa())
				TaxonDAO.delete(taxon);

			tx.commit();
			session.close();
			return true;
		} catch (PersistentException e) {
			tx.rollback();
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

	public boolean restoreTrashedTaxon(Integer taxonID, User user) {
		Taxon taxon;
		try {
			taxon = TaxonDAO.getTrashedTaxon(taxonID);
		} catch (PersistentException e) {
			e.printStackTrace();
			return false;
		}
		if (taxon != null) {
			taxon.setState(Taxon.ACTIVE);
			return writeTaxon(taxon, user);
		}
		return false;
	}

	public boolean permanentlyDeleteTaxon(Integer taxonID) {
		Taxon taxon = getTrashedTaxon(taxonID);
		if (taxon != null) {
			try {
				return TaxonDAO.delete(taxon);
			} catch (PersistentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}

	public boolean trashTaxon(Taxon taxon, User user) {
		
		try {
			if (taxon.getChildren().size() > 0)
				throw new RuntimeException("You can not delete a taxon that has children.");

			
			taxon.setState(Taxon.DELETED);
			writeTaxon(taxon, user);
			for (Assessment assessment : taxon.getAssessments()) {
				AssessmentIOWriteResult result = SIS.get().getAssessmentIO().trashAssessment(assessment, user);
				if (result.status.isError())
					throw new PersistentException("Unable to save assessment " + assessment.getId() + " because locked");
			}
			
			return true;
		} catch (PersistentException e) {
			
		}
		return false;
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
		DocumentUtils.writeVFSFile(taxonPath, vfs, xml);
		try {
			vfs.setLastModified(taxonPath, edit.getCreatedDate());
		} catch (NotFoundException e) {
			e.printStackTrace();
		}

	}

	public Taxon getTaxonFromVFS(Integer taxonID) {
		String text;
		try {
			text = getXMLFromVFS(taxonID);
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (BoundsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		if (text != null && !text.trim().equalsIgnoreCase("")) {
			NativeDocument ndoc = SIS.get().newNativeDocument(null);
			ndoc.parse(text);
			return Taxon.fromXML(ndoc);
		}
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
